package com.probase.probasepay.services;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Response;

import org.json.JSONArray;
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
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.BevuraToken;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardBin;
import com.probase.probasepay.models.Acquirer;
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


@Path("/CardServicesV2")
public class CardServicesV2 {

	private static Logger log = Logger.getLogger(CardServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	@GET
	@Path("/listCardSchemes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCardSchemes(
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			
			Collection<ECard> eCardList = null;
			String hql = "Select tp.* from cardschemes tp where tp.deleted_at IS NULL";
			List<Map<String, Object>> cardSchemeList = (List<Map<String, Object>>)swpService.getQueryBySQLResults(hql);
				
			jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_SUCCESS);
			jsonObject.put("message", "Card Scheme list fetched successfully");
			jsonObject.put("cardSchemeList", cardSchemeList);
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("", e);
			try {
				jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_FAIL);
				jsonObject.put("message", "Card Scheme Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	@GET
	@Path("/pullCardScheme")
	@Produces(MediaType.APPLICATION_JSON)
	public Response pullCardScheme(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("cardSchemeId") Long cardSchemeId)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
			Collection<ECard> eCardList = null;
			String hql = "Select tp.* from cardschemes tp Where tp.id = " + cardSchemeId;
			List<Map<String, Object>> cardSchemes = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			Map<String, Object> cardScheme = cardSchemes.get(0);
				
			jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_SUCCESS);
			jsonObject.put("message", "Card SCheme list fetched successfully");
			jsonObject.put("cardScheme", cardScheme);
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@GET
	@Path("/listCards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCustomerCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") Long customerId, 
			@QueryParam("status") String status, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			System.out.println("token ..." + token);
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println(verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);

			
			List<Map<String, Object>> customerCardList = null;
			String hql = "Select tp.id, c.lastName, c.firstName, a.accountIdentifier, tp.serialNo, SUBSTRING(tp.pan, 1, 4) as pan1, SUBSTRING(tp.pan, LENGTH(tp.pan)-3, 4) as pan2, " +
					"cs.schemeName, tp.cardType, tp.status, tp.stopFlag from ecards tp, cardschemes cs, customers c, accounts a where "
					+ "tp.cardscheme_id = cs.id AND tp.accountId = a.id AND a.customer_id = c.id";
			int and_ = 0;
			Map<String, Object> customer = null;
			if(status!=null)
			{
				hql = hql + " AND tp.status = '"+(CardStatus.valueOf(status).ordinal() + 1)+"'";
				and_ = 1;
			}
			if(customerId!=null)
			{
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " AND";
				}
				hql = hql + " tp.customerId = " + customerId;
				
				String hqlDevice = "Select tp.* from customers tp where tp.id = " + customerId;
				List<Map<String, Object>> customer_ = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hqlDevice);
				if(customer_.size()>0)
				{
					customer = customer_.get(0);
				}
			}
			
			

			customerCardList = (List<Map<String, Object>>)swpService.getQueryBySQLResults(hql);
			System.out.println("customerCardList count .." + customerCardList.size());
			/*Iterator<ECard> customerCardIter = customerCardList.iterator();
			JSONArray customerArray = new JSONArray();
			while(customerCardIter.hasNext())
			{
				ECard card = customerCardIter.next();
				JSONObject oneCustomer = new JSONObject();
				oneCustomer.put("id", card.getId());
				oneCustomer.put("full_name", (card.getAccount().getCustomer().getLastName()==null ? "" : card.getAccount().getCustomer().getLastName()) + (card.getAccount().getCustomer().getFirstName()==null ? "" : " " + card.getAccount().getCustomer().getFirstName()));
				oneCustomer.put("accountIdentifier", card.getAccount().getAccountIdentifier()==null ? "" : card.getAccount().getAccountIdentifier());
				oneCustomer.put("serialNo", card.getSerialNo()==null ? "" : card.getSerialNo());
				oneCustomer.put("pan", UtilityHelper.formatPan(card.getPan()));
				oneCustomer.put("schemeName", card.getCardScheme().getSchemeName()==null ? "" : card.getCardScheme().getSchemeName());
				oneCustomer.put("cardType", card.getCardType()==null ? "" : card.getCardType().name());
				oneCustomer.put("status", card.getStatus()==null ? "" : card.getStatus().name());
				customerArray.put(oneCustomer);
			}*/
			
			if(customer!=null)
			{
				jsonObject.put("customer", customer);
			}	
			
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Card list fetched successfully");
			jsonObject.put("customercardlist", (customerCardList));
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Customer Card Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/listStoppedCards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listStoppedCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			System.out.println("token ..." + token);
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println(verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);

			
			List<Map<String, Object>> customerCardList = null;
			hql = "Select tp.id, c.lastName, c.firstName, tp.trackingNumber, tp.serialNo, SUBSTRING(tp.pan, 1, 4) as pan1, SUBSTRING(tp.pan, LENGTH(tp.pan)-3, 4) as pan2, " +
					"cs.schemeName, tp.cardType, tp.status, tp.stopFlag from ecards tp, cardschemes cs, customers c, accounts a, users u where "
					+ "tp.cardscheme_id = cs.id AND tp.accountId = a.id AND a.customer_id = c.id AND c.user_id = u.id AND tp.stopFlag = 1 "
					+ "AND tp.status = " + CardStatus.STOPPED.ordinal() + " "
					+ "AND u.id = " + user.getId();
			int and_ = 0;
			Map<String, Object> customer = null;
			
			
			

			customerCardList = (List<Map<String, Object>>)swpService.getQueryBySQLResults(hql);
			System.out.println("customerCardList count .." + customerCardList.size());
			
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Stopped Customer Card list fetched successfully");
			jsonObject.put("customerStoppedCardList", (customerCardList));
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Customer Card Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/createUpdateScheme")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUpdateScheme(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("token") String token, 
			@FormParam("cardSchemeId") Long cardSchemeId, 
			@FormParam("schemeName") String schemeName, 
			@FormParam("overrideFixedFee") Double overrideFixedFee, 
			@FormParam("overrideTransactionFee") Double overrideTransactionFee, 
			@FormParam("minimumBalance") Double minimumBalance,
			@FormParam("currency") String currency)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			CardScheme cardScheme = new CardScheme();
			if(cardSchemeId!=null)
			{
				
				Collection<ECard> eCardList = null;
				String hql = "Select tp from CardScheme tp Where tp.id = " + cardSchemeId;
				cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
				System.out.println("cardSchemeId = " + cardSchemeId);
			}
			
			System.out.println("schemeName = " + schemeName);
			System.out.println("overrideFixedFee = " + overrideFixedFee);
			System.out.println("overrideTransactionFee = " + overrideTransactionFee);
			System.out.println("currency = " + currency);
			
			cardScheme.setOverrideFixedFee(overrideFixedFee);
			cardScheme.setOverrideTransactionFee(overrideTransactionFee);
			cardScheme.setMinimumBalance(minimumBalance);
			cardScheme.setSchemeName(schemeName);
			cardScheme.setCurrency(ProbasePayCurrency.valueOf(currency));
			cardScheme.setUpdated_at(new Date());
			if(cardSchemeId==null)
				cardScheme.setSchemeCode(RandomStringUtils.randomNumeric(5));
			if(cardSchemeId==null)
				cardScheme.setCreated_at(new Date());
			if(cardSchemeId!=null)
				swpService.updateRecord(cardScheme);
			if(cardSchemeId==null)
				swpService.createNewRecord(cardScheme);
			
			if(cardSchemeId==null)
				jsonObject.put("status", ERROR.CARD_SCHEME_CREATED_SUCCESS);
			if(cardSchemeId!=null)
				jsonObject.put("status", ERROR.CARD_SCHEME_UPDATED_SUCCESS);
			
			jsonObject.put("message", "Card SCheme Created/Updated Successfully");
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Formerly uploadTutukaCompanionPhysicalCardBin
	@POST
	@Path("/uploadPhysicalCardBin")
	@Produces(MediaType.APPLICATION_JSON)
	public String uploadPhysicalCardBin(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("acquirerId") String acquirerId, @FormParam("issuerId") Long issuerId, 
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject.toString();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String hql = "Select tp from Issuer tp where tp.id = '"+issuerId+"'";
			Acquirer issuer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerId+"'";
			Issuer acquirer = (Issuer)swpService.getUniqueRecordByHQL(hql);
			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			System.out.println("decryptedData..." + decryptedData);
			String cardBatchCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
			
			JSONArray data = new JSONArray(decryptedData);
			
			JSONArray respData = new JSONArray();
			for(int i=0; i<data.length(); i++)
			{
				JSONObject cardDataObject = data.getJSONObject(i);
				System.out.println("cardDataObj .." + cardDataObject.toString());
				String cardNumber = cardDataObject.getString("cardNumber").replaceAll(" ", "");
				cardNumber = UtilityHelper.formatPan(cardNumber);
				System.out.println("cardNumber .." + cardNumber.toString());
				String serialNo = cardDataObject.getString("serialNo");
				String trackingNumber = cardDataObject.getString("trackingNumber");
				
				hql = "Select tp from ECardBin tp where tp.trackingNumber = '"+trackingNumber+"' OR tp.pan = '"+cardNumber+"' OR tp.serialNo = '"+serialNo+"'";
				Collection<ECardBin> allEcardBinCards = (Collection<ECardBin>)swpService.getAllRecordsByHQL(hql);
				if(allEcardBinCards!=null && allEcardBinCards.size()>0)
				{
					
				}
				else
				{
					ECardBin eCardBin = new ECardBin(cardBatchCode, cardNumber, issuer, acquirer, CardStatus.NOT_ISSUED, CardType.TUTUKA_PHYSICAL_CARD, serialNo, trackingNumber, null);
					swpService.createNewRecord(eCardBin);
					
					respData.put(trackingNumber);
				}
			}
			jsonObject.put("trackingNumbers", respData);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "The following card bin information have been stored successfully");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("eerror", e);
		}
		

		System.out.println("jsonObject .." + jsonObject.toString());
		return jsonObject.toString();
	}
	
	
	
	@POST
	@Path("/getCardByCardBinId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCardByCardBinId(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token,
			@FormParam("binId") String binId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(this.swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			System.out.println("token...." + token);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			System.out.println("tokenUser.getRoleType()....=" + tokenUser.getRoleType().name());

			
			String hql = "Select iss.issuerName, iss.issuerCode, b.bankName, b.bankCode, acq.acquirerCode, acq.acquirerName, "
					+ "ec.nameOnCard, ec.pan as cardPan, ec.trackingNumber as cardTrackingNumber, ec.serialNo as cardSerialNo, tp.* from ecard_bin tp, issuers iss, banks b, acquirer acq, ecards ec where tp.issuer_id = iss.id AND iss.bank_id = b.id AND "
					+ "tp.acquirer_id = acq.id AND tp.cardIssued_id = ec.id AND tp.deleted_at IS NULL";
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				hql = hql + " AND b.bankCode = '"+staff_bank_code+"'";
			}
			
			System.out.println(hql);
			List<Map<String, Object>> allEcardBins = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			if(allEcardBins!=null && allEcardBins.size()>0)
			{
				Map<String, Object> cardBin = allEcardBins.get(0);
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Card Bin pulled successfully");
				jsonObject.put("cardBin", cardBin);
			}
			else
			{
				jsonObject.put("status", ERROR.CARD_BIN_NOT_FOUND);
				jsonObject.put("message", "Card Bin not found mapped to any physical cards at the moment");
			}
			/*Iterator<Map<String, Object>> it = allEcardBins.iterator();
			
			JSONArray jsArray = new JSONArray();
			while(it.hasNext())
			{
				Map<String, Object> cardBin = it.next();
				JSONObject js = new JSONObject();
				js.put("card_no", (String)cardBin.get("pan"));
				//js.put("serial_no", cardBin.getSerialNo());
				js.put("card_type", CardType.values()[((Integer)cardBin.get("cardType"))].name().replaceAll("_", " "));
				js.put("tracking_number", (String)cardBin.get("trackingNumber"));
				js.put("acquirer", (String)cardBin.get("acquirerName"));
				js.put("issuer", (String)cardBin.get("bankName"));
				js.put("status", CardStatus.values()[((Integer)cardBin.get("status"))].name().replaceAll("_", " "));
				
				jsArray.put(js);
			}*/
			
			

			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("error", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	
	@GET
	@Path("/listBatchCards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listBatchCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(this.swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			System.out.println("tokenUser.getRoleType()....=" + tokenUser.getRoleType().name());

			
			String hql = "Select tp.*, b.bankName, acq.acquirerName, iss.issuerName from ecard_bin tp, issuers iss, banks b, acquirer acq where tp.issuer_id = iss.id AND iss.bank_id = b.id AND "
					+ "tp.acquirer_id = acq.id AND tp.deleted_at IS NULL";
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				hql = hql + " AND b.bankCode = '"+staff_bank_code+"'";
			}
			
			System.out.println(hql);
			List<Map<String, Object>> allEcardBins = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			/*Iterator<Map<String, Object>> it = allEcardBins.iterator();
			
			JSONArray jsArray = new JSONArray();
			while(it.hasNext())
			{
				Map<String, Object> cardBin = it.next();
				JSONObject js = new JSONObject();
				js.put("card_no", (String)cardBin.get("pan"));
				//js.put("serial_no", cardBin.getSerialNo());
				js.put("card_type", CardType.values()[((Integer)cardBin.get("cardType"))].name().replaceAll("_", " "));
				js.put("tracking_number", (String)cardBin.get("trackingNumber"));
				js.put("acquirer", (String)cardBin.get("acquirerName"));
				js.put("issuer", (String)cardBin.get("bankName"));
				js.put("status", CardStatus.values()[((Integer)cardBin.get("status"))].name().replaceAll("_", " "));
				
				jsArray.put(js);
			}*/
			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Card batch pulled successfully");
			jsonObject.put("cardBins", allEcardBins);

			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("error", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}


	
	@POST
	@Path("/cardBalance")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cardBalance(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("cardId") Long cardId, 
			@FormParam("merchantCode") String merchantCode, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("bankcode") String bankcode, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			
			//String hql = "Select tp.* from ecards tp where tp.id = '"+cardId+"'";
			String hql = "Select tp.*, cs.minimumBalance, ac.status as account_status, ac.probasePayCurrency, c.status as customer_status from ecards tp, accounts ac, customers c, cardschemes cs where "
	        			+ "tp.account_id = ac.id AND ac.customer_id = c.id AND tp.cardScheme_id = cs.id AND "
	        			+ "tp.deleted_at IS NULL AND (tp.id = "+cardId+") ";
			List<Map<String, Object>> ecard_ = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			Map<String, Object> ecard = null;
			if(ecard_!=null && ecard_.size()>0)
			{
				ecard = ecard_.get(0);
			}

			if(ecard!=null)
			{
				hql = "Select tp.* from accounts tp where tp.id = " + (BigInteger)ecard.get("account_id");
				System.out.println(hql);
				List<Map<String, Object>> acct_ =  (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				Map<String, Object> account = acct_.get(0);

				hql = "Select tp.* from customers tp where tp.id = " + (BigInteger)account.get("customer_id");
				System.out.println(hql);
				List<Map<String, Object>> customer_ =  (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				Map<String, Object> customer = acct_.get(0);
				
				if(CardType.values()[(Integer)ecard.get("cardType")].equals(CardType.TUTUKA_PHYSICAL_CARD) || CardType.values()[(Integer)ecard.get("cardType")].equals(CardType.TUTUKA_VIRTUAL_CARD))
				{
					TutukaServicesV2 tutukaService = new TutukaServicesV2();
					String logId = RandomStringUtils.randomNumeric(20) + " PROBASEPAY";
					String terminalId = "";
					String terminalPassword = "";
					if(CardType.values()[(Integer)ecard.get("cardType")].equals(CardType.TUTUKA_PHYSICAL_CARD))
					{
						terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
						terminalPassword  = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
					}
					else if(CardType.values()[(Integer)ecard.get("cardType")].equals(CardType.TUTUKA_VIRTUAL_CARD))
					{
						terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
						terminalPassword  = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
					}
					
					DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
					String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
					//transactionDateFormatted = "20200812T02:05:18";
					String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
					String reference = (String)ecard.get("serialNo");
					String narration = "PROBASEWALLET_CARD_BALANCE_CHECK";
					String klvData = "0260401042520022003MAG";
					String txnId = RandomStringUtils.randomNumeric(6);
					String checksum = "Balance"+TutukaHelper.formatDataForTutuka(terminalId)+""+TutukaHelper.formatDataForTutuka(reference)+""+
							TutukaHelper.formatDataForTutuka(narration)+""+TutukaHelper.formatDataForTutuka(klvData)+""+
							TutukaHelper.formatDataForTutuka(txnId)+""+transactionDateFormatted;
					checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
					
					String xml = "<methodCall>";
					xml = xml + "<methodName>Balance</methodName>";
					xml = xml + "<params>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>" + terminalId + "</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>"+reference+"</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>"+narration+"</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>"+klvData+"</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>"+txnId+"</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<dateTime.iso8601>"+transactionDateFormatted+"</dateTime.iso8601>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "<param>";
					xml = xml + "<value>";
					xml = xml + "<string>"+checksum+"</string>";
					xml = xml + "</value>";
					xml = xml + "</param>";
					xml = xml + "</params>";
					xml = xml + "</methodCall>";
					JSONObject res = tutukaService.getTutukaCompanionCardBalance(xml, logId);
					if(res.has("status") && res.getInt("status")==ERROR.GENERAL_OK)
					{
						/*AccountServicesV2 as = new AccountServicesV2();
						JSONObject acctBalJs = as.getAccountBalance(httpHeaders, requestContext, ecard.getAccount().getId(), token, merchantCode, deviceCode);
						if(acctBalJs!=null)
						{
							if(acctBalJs.has("status") && acctBalJs.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
							{
								jsonObject.put("account_current_balance", acctBalJs.has("currentBalance") ? acctBalJs.getDouble("currentBalance") : 0.00);
								jsonObject.put("account_available_balance", acctBalJs.has("availableBalance") ? acctBalJs.getDouble("availableBalance") : 0.00);
								jsonObject.put("account_floating_balance", acctBalJs.has("floatingBalance") ? acctBalJs.getDouble("floatingBalance") : 0.00);
							}
						}*/
						jsonObject.put("status", ERROR.CARD_BALANCE_SUCCESS);
						jsonObject.put("message", "Balance Obtained");
						jsonObject.put("current_balance", res.getDouble("currentBalance")/100);
						jsonObject.put("available_balance", res.getDouble("balance")/100);
						jsonObject.put("cardCurrency", res.getString("cardCurrency"));
						jsonObject.put("account", account);
						jsonObject.put("customer", customer);
						jsonObject.put("card", ecard);
					}
				}
				else
				{
					
					Double balance = (Double)ecard.get("cardBalance");
				    if(balance!=null)
				    {
							
						jsonObject.put("status", ERROR.CARD_BALANCE_SUCCESS);
						jsonObject.put("message", "Balance Obtained");
						jsonObject.put("current_balance", balance);
						jsonObject.put("available_balance", balance - (Double)ecard.get("minimumBalance"));
						jsonObject.put("cardCurrency", (String)ProbasePayCurrency.values()[(Integer)ecard.get("probasePayCurrency")].name());
						jsonObject.put("account", account);
						jsonObject.put("customer", customer);
						jsonObject.put("card", ecard);
				    }
				    else
				    {
				    	jsonObject.put("status", ERROR.CARD_BALANCE_SUCCESS);
						jsonObject.put("message", "Balance Obtained");
						jsonObject.put("balance_amount", 0.00);
						jsonObject.put("account", account);
						jsonObject.put("customer", customer);
						jsonObject.put("card", ecard);
				    }
						
				}
    		}else
    		{
    			//Card does not exist
    			jsonObject.put("status", ERROR.CARD_NOT_VALID);
				jsonObject.put("message", "Invalid Card Detail Provided");
			}
			
		}catch(Exception e)
		{
			log.warn(e);
			e.printStackTrace();
			log.error("err", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	
	
	
	

	
	@POST
	@Path("/cardBalanceByTokenization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cardBalanceByTokenization(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("bevuraTokenCard") String bevuraTokenCard, 
			@FormParam("merchantCode") String merchantCode, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("hash") String hash, 
			@FormParam("acquirerCode") String acquirerCode)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			//String acquirerKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			//bevuraTokenCard = (String)UtilityHelper.decryptData(bevuraTokenCard, acquirerKey);
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"'";
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from BevuraToken tp where tp.token = '" + bevuraTokenCard + "' AND tp.type = 'CARD' AND "
					+ "tp.merchantId = " + merchant.getId() + "  AND tp.deviceId = " + device.getId();
			BevuraToken bevuraToken = (BevuraToken)this.swpService.getUniqueRecordByHQL(hql);
			
			if(bevuraToken==null)
			{
    			//Card does not exist
    			jsonObject.put("status", ERROR.CARD_NOT_VALID);
				jsonObject.put("message", "Invalid Card Detail Provided");
			
			}
			else
			{
				return this.cardBalance(httpHeaders, requestContext, bevuraToken.getCardId(), merchantCode, deviceCode, null, null);
			}
			
		}catch(Exception e)
		{
			log.warn(e);
			e.printStackTrace();
			log.error("err", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	
	@GET
	@Path("/getCardTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCardTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("cardId") Long cardId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);

			
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			System.out.println("verifyJ1 ==" + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			System.out.println("verify2 ==");
			
			
			String hql = "Select tp.*, ec.trackingNumber from transactions tp, ecards ec where tp.card_id = ec.id AND tp.card_id IS NOT NULL ";
			if(cardId!=null)
			{
				hql = hql + " AND ec.id = "+(cardId)+"";
				
			}
			System.out.println(hql);
			List<Map<String, Object>> cardtransactionslist = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			
			if(cardId!=null)
			{
				hql = "Select tp.* from ecards tp where tp.id = "+(cardId)+"";
				System.out.println(hql);
				
				List<Map<String, Object>> card_ = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				Map<String, Object> card = card_.get(0);
				
				JSONObject cardJson = new JSONObject();
				cardJson.put("id", (BigInteger)card.get("id"));
				cardJson.put("pan", (String)card.get("pan"));
				cardJson.put("serialNo", (String)card.get("serialNo"));
				cardJson.put("trackingNumber", (String)card.get("trackingNumber"));
				jsonObject.put("card", cardJson);
			}
			
			
			
			
			
			
			jsonObject.put("status", ERROR.TRANSACTION_SUCCESS);
			jsonObject.put("message", "Card transactions fetched");
			jsonObject.put("cardtransactionslist", cardtransactionslist);
			System.out.println("verify3 ==");
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
				jsonObject.put("message", "Card transactions fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}

}
