package com.probase.probasepay.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.AcquirerType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.TutukaHelper;
import com.probase.probasepay.util.UtilityHelper;


@Path("/CardServices")
public class CardServices {

	private static Logger log = Logger.getLogger(CardServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/*
	 * @params	status	
	 * 	CardStatus.ACTIVE	0
	 *	CardStatus.DELETED	1
	 *	CardStatus.DISABLED	2
	 *	CardStatus.INACTIVE	3
	 */
	
	@POST
	@Path("/updateCardStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateCardStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("status") Integer status, 
			@FormParam("cardIdS") String cardIdS, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String issuerbankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerbankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			Integer cardId = (Integer) UtilityHelper.decryptData(cardIdS, bankKey);
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard ecard = (ECard)this.swpService.getUniqueRecordByHQL(hql);

			if(status!=null && status>-1 && status<3)
			{
				ecard.setStatus(CardStatus.values()[status]);
				this.swpService.updateRecord(ecard);
			}
			
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Card status updated Successfully");
			jsonObject.put("ecard", new Gson().toJson(ecard));
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	
	@GET
	@Path("/listCardSchemes")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listCardSchemes(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			
			Collection<ECard> eCardList = null;
			String hql = "Select tp from CardScheme tp";
			Collection<CardScheme> cardSchemeList = (Collection<CardScheme>)swpService.getAllRecordsByHQL(hql);
				
			jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_SUCCESS);
			jsonObject.put("message", "Card SCheme list fetched successfully");
			jsonObject.put("cardSchemeList", new Gson().toJson(new ArrayList<CardScheme>(cardSchemeList)));
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_FAIL);
				jsonObject.put("message", "Card Scheme Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	
	

	
	@POST
	@Path("/createUpdateScheme")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createUpdateScheme(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("token") String token, 
			@FormParam("cardSchemeIdS") String cardSchemeIdS, 
			@FormParam("schemeName") String schemeName, 
			@FormParam("overrideFixedFee") Double overrideFixedFee, 
			@FormParam("overrideTransactionFee") Double overrideTransactionFee, 
			@FormParam("minimumBalance") Double minimumBalance)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			CardScheme cardScheme = new CardScheme();
			if(cardSchemeIdS!=null)
			{
				Integer cardSchemeId = (Integer) UtilityHelper.decryptData(cardSchemeIdS, bankKey);
				
				Collection<ECard> eCardList = null;
				String hql = "Select tp from CardScheme tp Where tp.id = " + cardSchemeId;
				cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
				System.out.println("cardSchemeIds = " + cardSchemeIdS);
			}
			
			System.out.println("schemeName = " + schemeName);
			System.out.println("overrideFixedFee = " + overrideFixedFee);
			System.out.println("overrideTransactionFee = " + overrideTransactionFee);
			
			cardScheme.setOverrideFixedFee(overrideFixedFee);
			cardScheme.setOverrideTransactionFee(overrideTransactionFee);
			cardScheme.setMinimumBalance(minimumBalance);
			cardScheme.setSchemeName(schemeName);
			cardScheme.setUpdated_at(new Date());
			if(cardSchemeIdS==null)
				cardScheme.setSchemeCode(RandomStringUtils.randomNumeric(5));
			if(cardSchemeIdS==null)
				cardScheme.setCreated_at(new Date());
			if(cardSchemeIdS!=null)
				swpService.updateRecord(cardScheme);
			if(cardSchemeIdS==null)
				swpService.createNewRecord(cardScheme);
			
			if(cardSchemeIdS==null)
				jsonObject.put("status", ERROR.CARD_SCHEME_CREATED_SUCCESS);
			if(cardSchemeIdS!=null)
				jsonObject.put("status", ERROR.CARD_SCHEME_UPDATED_SUCCESS);
			
			jsonObject.put("message", "Card SCheme Created/Updated Successfully");
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_FAIL);
				jsonObject.put("message", "Card Scheme Creation/Update Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	
	
	@GET
	@Path("/listCorporateSubAccountsEcards")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listCorporateSubAccountsEcards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("corporateAcctId") String corporateAcctId, 
			@QueryParam("index") Integer index, 
			@QueryParam("count") Integer count)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			if(count==null)
				count=Application.BASE_LIST_COUNT;
			if(index==null)
				index = 0;
			
			Collection<ECard> corporateCustomerAccountEcardList = null;
			Account account = null;
			if(corporateAcctId!=null)
			{
				Integer corporateAcctIdI = (Integer) UtilityHelper.decryptData(corporateAcctId, bankKey);
				String hql = "Select tp from ECard tp WHERE tp.corporateCustomerAccountId = " + corporateAcctIdI;
				corporateCustomerAccountEcardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql, (index*count), count);
				jsonObject.put("corporateCustomerAccountEcardList", new Gson().toJson(corporateCustomerAccountEcardList));
				
				hql = "Select tp from Account tp where tp.id = " + corporateAcctIdI;
				account = (Account)this.swpService.getUniqueRecordByHQL(hql);
				jsonObject.put("corporateAccount", new Gson().toJson(account));
			}

			
				
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Accounts list fetched successfully");
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Customer Account Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
	}
	
	
	


	
	
	
	
	
	
	
	
	
	
	
	
}
