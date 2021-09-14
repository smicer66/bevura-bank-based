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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.BankStaffStatus;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
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
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.AutoDebit;
import com.probase.probasepay.models.AutoDebitMandate;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/AutoDebitServicesV2")
public class AutoDebitServicesV2 {
	private static Logger log = Logger.getLogger(AutoDebitServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	

	
	
	@POST
	@Path("/addNewAutoDebit")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewAutoDebit(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("autoDebitList") String autoDebitList, 
			@FormParam("token") String token)
	{
		Bank bank = new Bank();
		System.out.println("token ==" + token);
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
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
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

		
		
			System.out.println(autoDebitList);
			System.out.println(token);
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			
			
			String autoDebitList_ = (String)UtilityHelper.decryptData(autoDebitList, bankKey);
			
			JSONArray jsonArray = new JSONArray(autoDebitList_);
			JSONArray successfulAutoDebit = new JSONArray();
			for(int i=0; i<jsonArray.length(); i++)
			{
				JSONObject js = jsonArray.getJSONObject(i);
				String userMobile = js.getString("userMobile");
				String userFullName = js.getString("userFullName");
				Double amountDue = js.getDouble("amountDue");
				String dateDue = js.getString("dateDue");
				Date dateDue_ = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateDue + " 23:59");
				hql = "Select tp from Account tp where tp.customer.user.mobileNo = '"+userMobile+"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal()  +
						 "";
				Account sourceAccount = (Account)swpService.getUniqueRecordByHQL(hql);
				Long cardId = null;
				Boolean isProcessed = false;
				Boolean isLocked = false;
				String requestData = null;
				String responseData = null;
				Long transactionId = null;
				
				
				AutoDebit autoDebit = new AutoDebit(acquirer.getId(), userMobile, userFullName, amountDue, dateDue_, sourceAccount.getId(), cardId, isProcessed, isLocked, 
						requestData, responseData, transactionId);
				autoDebit = (AutoDebit)swpService.createNewRecord(autoDebit);
				
				successfulAutoDebit.put(autoDebit);
			}
			
			if(successfulAutoDebit.length()!= jsonArray.length())
			{
				for(int i=0; i<successfulAutoDebit.length(); i++)
				{
					AutoDebit autoDebit = (AutoDebit)successfulAutoDebit.get(i);
					swpService.deleteRecord(autoDebit);
				}
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Auto debits could not be set successfully for the requests");
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Auto debits set successfully for the requests");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Auto debits could not be set successfully for the requests");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/customerEnableAutoDebit")
	@Produces(MediaType.APPLICATION_JSON)
	public Response customerEnableAutoDebit(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("accountNumber") String accountNumber, 
			@FormParam("cardPanNumber") String cardPanNumber, 
			@FormParam("merchantCode") String merchantCode, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("isEnabled") Integer isEnabled, 
			@FormParam("token") String token)
	{
		Bank bank = new Bank();
		System.out.println("token ==" + token);
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
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
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

		
		
			System.out.println(accountNumber);
			System.out.println(token);
			Device device = null;
			Merchant merchant = null;
			if(merchantCode!=null)
			{
				String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' "
						+ "AND tp.status  = "+ MerchantStatus.ACTIVE.ordinal();
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			}
			
			if(deviceCode!=null)
			{
				String hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' "
						+ "AND tp.status  = "+ DeviceStatus.ACTIVE.ordinal();
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			}

			//accountNumber = (String)UtilityHelper.decryptData(accountNumber, bankKey);
			String hql = "Select tp from Account tp where tp.accountIdentifier = '"+accountNumber+"' "
					+ "AND tp.customer.user.id  = "+ user.getId() +" AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal();
			Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
			if(account==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid account provided as source account to be debited during auto-debits");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			AutoDebitMandate adm = null;

			System.out.println("device ==" + device.getId());
			System.out.println("device ==" + device.getMerchant().getId());
			System.out.println("merchant ==" + merchant.getId());
			if(merchant!=null && device!=null && device.getMerchant().getId().equals(merchant.getId()))
			{
				if(isEnabled!=null && isEnabled==1)
				{
					hql = "Select tp from AutoDebitMandate tp where tp.merchantId = " + merchant.getId() + 
							" AND tp.deviceId = " + device.getId() + " AND tp.accountId = " + account.getId() + " AND tp.isActive = 1";
					adm =(AutoDebitMandate)swpService.getUniqueRecordByHQL(hql);
					if(adm==null)
					{
						adm = new AutoDebitMandate(account.getId(), null, true, merchant.getId(), device.getId(), user.getId());
						swpService.createNewRecord(adm);
					}
					
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Auto debits set successfully. Your merchant will submit appropriate bills to be paid for using auto-debits. You can turn this feature off at anytime");
				}
				else
				{
					hql = "Select tp from AutoDebitMandate tp where tp.merchantId = " + merchant.getId() + 
							" AND tp.deviceId = " + device.getId() + " AND tp.accountId = " + account.getId() + " AND tp.isActive = 1";
					adm =(AutoDebitMandate)swpService.getUniqueRecordByHQL(hql);
					adm.setIsActive(false);
					swpService.updateRecord(adm);
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Auto debits set successfully. Your merchant will submit appropriate bills to be paid for using auto-debits. You can turn this feature off at anytime");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Auto debits could not be setup successfully. ");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Auto debits could not be set successfully for the requests");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	

}
