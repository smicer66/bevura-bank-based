package com.probase.probasepay.services;

import java.text.DateFormat;
import java.text.ParseException;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.TutukaHelper;
import com.probase.probasepay.util.UtilityHelper;

@Path("/DeviceServices")
public class DeviceServices {
	/*private static Logger log = Logger.getLogger(DeviceServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	@POST
	@Path("/createNewMerchantDevice")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createNewMerchantDevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("deviceId") String deviceId, 
			@FormParam("deviceType") String deviceType, 
			@FormParam("domainUrl") String domainUrl, 
			@FormParam("forwardSuccessUrl") String forwardSuccessUrl, 
			@FormParam("forwardFailureUrl") String forwardFailureUrl, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("deviceSerialNo") String deviceSerialNo, 
			@FormParam("notifyEmail") String notifyEmail, 
			@FormParam("notifyMobile") String notifyMobile, 
			@FormParam("token") String token, 
			@FormParam("switchToLive") Integer switchToLive, 
			@FormParam("mastercardVisaAccept") Integer mastercardVisaAccept, 
			@FormParam("eagleCardAccept") Integer eagleCardAccept, 
			@FormParam("bankOnlineAccept") Integer bankOnlineAccept, 
			@FormParam("mobileMoneyAccept") Integer mobileMoneyAccept, 
			@FormParam("walletAccept") Integer walletAccept, 
			@FormParam("zicbAuthKey") String zicbAuthKey, 
			@FormParam("zicbServiceKey") String zicbServiceKey, 
			@FormParam("cybersourceLiveAccessKey") String cybersourceLiveAccessKey, 
			@FormParam("cybersourceLiveProfileId") String cybersourceLiveProfileId, 
			@FormParam("cybersourceLiveSecretKey") String cybersourceLiveSecretKey, 
			@FormParam("cybersourceDemoAccessKey") String cybersourceDemoAccessKey,
			@FormParam("cybersourceDemoProfileId") String cybersourceDemoProfileId, 
			@FormParam("cybersourceDemoSecretKey") String cybersourceDemoSecretKey, 
			@FormParam("ubaServiceKey") String ubaServiceKey, 
			@FormParam("ubaMerchantId") String ubaMerchantId, 
			@FormParam("mpqrDeviceCode") String mpqrDeviceCode, 
			@FormParam("mpqrDeviceSerialNo") String mpqrDeviceSerialNo, 
			@FormParam("mpqrAcquirerId") String mpqrAcquirerId, 
			@FormParam("mpqrCardSchemeId") Long mpqrCardSchemeId, 
			@FormParam("mpqrCurrencyCode") Long mpqrCurrencyCode, 
			@FormParam("mpqrPoolAccountId") Long mpqrPoolAccountId)
	{
		//updateStatus = 0 : create
		//updateStatus = 0 : update
		Device device = null;
		Merchant merchant = null;
		JSONObject jsonObject = new JSONObject();
		boolean continueProcess = false;
		
		if(deviceId == null)
		{
			device = new Device();
			continueProcess = true;
		}else
		{
			try {
				
				this.swpService = this.serviceLocator.getSwpService();
				String bankKey = UtilityHelper.getBankKey(null, swpService);
				Integer deviceIdI = (Integer) UtilityHelper.decryptData(deviceId, bankKey);
				String hql = "Select tp from Device tp where tp.id = " + deviceIdI;
				device = (Device)swpService.getUniqueRecordByHQL(hql);
				continueProcess = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
		}
		
		try{
			if(continueProcess == true)
			{
				try {
					
					this.swpService = this.serviceLocator.getSwpService();
					Application app = Application.getInstance(swpService);
					JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
					System.out.println("verifyJ ==" + verifyJ.toString());
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
					
					String subject = verifyJ.getString("subject");
					User usr = new Gson().fromJson(subject, User.class);
					System.out.println("verifyJ ==" + verifyJ.toString());
					String issuerBankCode = verifyJ.getString("issuerBankCode");
					System.out.println("issuerBankCode ==" + issuerBankCode);
					String branch_code = verifyJ.getString("branchCode");
					System.out.println("branch_code ==" + branch_code);
					String staff_bank_code = verifyJ.getString("staff_bank_code");
					System.out.println("staff_bank_code ==" + staff_bank_code);
					
					String bankKey = UtilityHelper.getBankKey(null, swpService);
					Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
					String hql = "Select tp from Merchant tp where tp.id = " + merchantIdI;
					merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Bank tp where tp.bankCode = '"+ issuerBankCode+"'";
					Bank issuer = (Bank)this.swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Acquirer tp where tp.id = " + mpqrAcquirerId;
					Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
					
					device.setCreated_at(new Date());
					if(deviceId == null)
						device.setDeviceCode(deviceCode!=null && deviceCode.length()>0 ? deviceCode : RandomStringUtils.randomNumeric(10));
					if(deviceId == null)
						device.setDeviceSerialNo(deviceSerialNo!=null && deviceSerialNo.length()>0 ? deviceSerialNo : RandomStringUtils.randomAlphanumeric(10));
					
					device.setDeviceType(DeviceType.valueOf(deviceType));
					device.setDomainUrl(domainUrl);
					device.setEmailNotify(notifyEmail);
					device.setMobileNotify(notifyMobile);
					device.setSuccessUrl(forwardSuccessUrl);
					device.setFailureUrl(forwardFailureUrl);
					
					device.setSwitchToLive(switchToLive!=null && switchToLive==1 ? true : false);
					device.setMastercardVisaAccept(mastercardVisaAccept!=null && mastercardVisaAccept==1 ? true : false);
					device.setEagleCardAccept(eagleCardAccept!=null && eagleCardAccept==1 ? true : false);
					device.setBankOnlineAccept(bankOnlineAccept!=null && bankOnlineAccept==1 ? true : false);
					device.setMobileMoneyAccept(mobileMoneyAccept!=null && mobileMoneyAccept==1 ? true : false);
					device.setWalletAccept(walletAccept!=null && walletAccept==1 ? true : false);
					
					
					
					if(deviceId == null)
						device.setMerchant(merchant);
					if(deviceId == null)
						device.setStatus(DeviceStatus.ACTIVE);
					
					
					device.setUpdated_at(new Date());
					device.setSetupByUser(usr);
					if(zicbAuthKey!=null && zicbServiceKey!=null)
					{
						device.setZicbAuthKey(zicbAuthKey);
						device.setZicbServiceKey(zicbServiceKey);
					}
					
					if(cybersourceDemoAccessKey!=null && cybersourceDemoProfileId!=null && cybersourceDemoSecretKey!=null && 
							cybersourceLiveAccessKey!=null && cybersourceLiveProfileId!=null && cybersourceLiveSecretKey!=null)
					{
						device.setCybersourceDemoAccessKey(cybersourceDemoAccessKey);
						device.setCybersourceDemoProfileId(cybersourceDemoProfileId);
						device.setCybersourceDemoSecretKey(cybersourceDemoSecretKey);
						device.setCybersourceLiveAccessKey(cybersourceLiveAccessKey);
						device.setCybersourceLiveProfileId(cybersourceLiveProfileId);
						device.setCybersourceLiveSecretKey(cybersourceLiveSecretKey);
					}
					
					if(ubaMerchantId!=null && ubaServiceKey!=null)
					{
						device.setUbaMerchantId(ubaMerchantId);
						device.setUbaServiceKey(ubaServiceKey);
					}
					
					if(mpqrDeviceCode!=null && mpqrDeviceSerialNo!=null)
					{
						device.setMpqrDeviceCode(mpqrDeviceCode);
						device.setMpqrDeviceSerialNo(mpqrDeviceSerialNo);
					}
					
					
					this.swpService.createNewRecord(device);
					//this.swpService.getCommitTransaction(txn);
					
					if(mpqrDeviceCode!=null && mpqrDeviceSerialNo!=null && deviceId == null)
					{
						String firstName = merchant.getUser().getFirstName();
						String lastName = merchant.getUser().getLastName();
						String otherName = merchant.getUser().getOtherName();
						String addressLine1 = merchant.getAddressLine1();
						String addressLine2 = merchant.getAddressLine2();
						District locationDistrict = null;
						User user = merchant.getUser();
						String contactMobile = merchant.getUser().getMobileNo();
						String altContactMobile = merchant.getUser().getMobileNo();
						String contactEmail = merchant.getUser().getUserEmail();
						String altContactEmail = merchant.getUser().getUserEmail();
						String verificationNumber = RandomStringUtils.randomNumeric(10);
			        	Date dateOfBirth = null;
			        	Gender gender = null;
			        	CustomerStatus status = CustomerStatus.ACTIVE;
			        	String customerImage = null;
			        	CustomerType customerType = CustomerType.CORPORATE;
			        	String mobileMoneyPassword = null;
			        	MeansOfIdentificationType meansOfIdentificationType = null;
			        	String meansOfIdentificationNumber = null;
			        	String currencyCode = ProbasePayCurrency.ZMW.name();
			        	User setUpByUser = usr;
						TutukaServicesV2 tutukaServices = new TutukaServicesV2();
						JSONObject deviceData = new JSONObject();
						deviceData.put("deviceId", device.getId());
						//deviceData.put("customerId", );
						deviceData.put("acquirerId", mpqrAcquirerId);
						deviceData.put("cardSchemeId", mpqrCardSchemeId);
						deviceData.put("currencyCodeId", mpqrCurrencyCode);
						deviceData.put("poolAccountId", mpqrPoolAccountId);
						
						
						String encryptedData = deviceData.toString();
						encryptedData = (String) UtilityHelper.encryptData(encryptedData, bankKey);
						tutukaServices.createQRData(httpHeaders, requestContext, encryptedData, token);
					}
						
					if(deviceId == null)
						this.swpService.createNewRecord(device);
					else
						this.swpService.updateRecord(device);
					

					jsonObject.put("merchantCode", device.getMerchant().getMerchantCode());
					jsonObject.put("deviceCode", device.getDeviceCode());
					jsonObject.put("status", ERROR.DEVICE_ADD_SUCCESS);
					jsonObject.put("notifyMerchantMobile", device.getMerchant().getContactMobile());
					jsonObject.put("message", "New Merchant Device " + (deviceId == null ? "Added" : "Updated")+ " Successfully");
					System.out.println("Create New merchant device = " + jsonObject.toString());
					
					return jsonObject;
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.warn(e);
					jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
					jsonObject.put("message", "Merchant Device Update Failed");
				}
			}else
			{
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				log.warn(e);
				System.out.println("Create New merchant Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			
			return jsonObject;
		}

		return jsonObject;
		
		
		
	}
	
	
	@GET
	@Path("/listDevice")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listDevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") String status, 
			@QueryParam("merchantId") String merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		Merchant merchant = null;
		try {
			this.swpService = serviceLocator.getSwpService();
			List<Device> deviceList = null;
			
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			
			
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
			
			String subject = verifyJ.getString("subject");
			User usr = new Gson().fromJson(subject, User.class);
			System.out.println("usrid ==>" + usr.getId());
			Merchant merchantDecryptor = null;
			String bankKey = null;
			
			if(merchantId!=null)
			{
				
				
				if(usr.getRoleType().equals(RoleType.MERCHANT))
				{
					String hql = "Select tp from Merchant tp where tp.user.id = " + usr.getId();
					System.out.println("HQL ==>" + hql);
					merchantDecryptor = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
					bankKey = merchantDecryptor.getMerchantDecryptKey();
					
				}
				else{
					System.out.println("else");
					bankKey = UtilityHelper.getBankKey(null, swpService);
					System.out.println("bankKey ==>" + bankKey);
				}
				
				System.out.println("bankKey = " + bankKey + " && merchantId = " + merchantId);
				Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
				
				String hql = "Select tp.id, tp.merchant.merchantName, tp.deviceType, tp.deviceCode, tp.successUrl, " +
						"tp.failureUrl, tp.emailNotify, tp.mobileNotify, tp.status, tp.merchant.id as merchantId from Device tp";
				int and_ = 0;
				if(status!=null)
				{
					hql = hql + " where tp.status = "+ DeviceStatus.valueOf(status).ordinal();
					and_ = 1;
				}
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql +" WHERE";
				}
				hql = hql + " tp.merchant.id = " + merchantIdI;
				deviceList = (List<Device>)swpService.getAllRecordsByHQL(hql);
				
				String hqlMerchant = "Select tp from Merchant tp where tp.id = " + merchantIdI;
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hqlMerchant);

				JSONObject merchantData = new JSONObject();
				merchantData.put("id", merchant.getId());
				merchantData.put("name", merchant.getMerchantName());
				jsonObject.put("merchant", merchantData);
			}
			else
			{
				
				String hql = "Select tp.id, tp.merchant.merchantName, tp.deviceType, tp.deviceCode, tp.successUrl, " +
						"tp.failureUrl, tp.emailNotify, tp.mobileNotify, tp.status, tp.merchant.id as merchantId from Device tp";
				int and_ = 0;
				if(status!=null)
				{
					hql = hql + " where tp.status = "+ DeviceStatus.valueOf(status).ordinal();
					and_ = 1;
				}
				
				deviceList = (List<Device>)swpService.getAllRecordsByHQL(hql);
				
				if(usr.getRoleType().equals(RoleType.MERCHANT))
				{
					hql = "Select tp from Merchant tp where tp.user.id = " + usr.getId();
					System.out.println("HQL ==>" + hql);
					merchantDecryptor = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
					bankKey = merchantDecryptor.getMerchantDecryptKey();
					
				}
				else{
					System.out.println("else");
					bankKey = UtilityHelper.getBankKey(null, swpService);
					System.out.println("bankKey ==>" + bankKey);
				}
			}
			
			
			
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Device list fetched");
			jsonObject.put("devicelist", new Gson().toJson(deviceList));
			return jsonObject;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Device fetch Failed");
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	@GET
	@Path("/getADevice")
	@Produces(MediaType.APPLICATION_JSON)

	public JSONObject getADevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("deviceId") String deviceId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		Device device = null;
		try {
			swpService = serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer deviceIdI = (Integer) UtilityHelper.decryptData(deviceId, bankKey);
			
			String hqlDevice = "Select tp from Device tp where tp.id = " + deviceIdI;
			device = (Device)this.swpService.getUniqueRecordByHQL(hqlDevice);
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			
			
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
				
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Device fetched");
			jsonObject.put("device", new Gson().toJson(device));
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Device creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
	}
	
	
	
	@GET
	@Path("/listDeviceTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listDeviceTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") String status, 
			@QueryParam("deviceIdS") String deviceIdS, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		Merchant merchant = null;
		Device device = null;
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			
			
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
			
			
			
			Collection<Transaction> transactionList = null;
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			
			String hql = "Select tp from Transaction tp";
			int and_ = 0;
			if(status!=null)
			{
				hql = hql + " where tp.status = "+(DeviceStatus.valueOf(status).ordinal());
				and_ = 1;
			}
			if(deviceIdS!=null)
			{
				Integer deviceId = (Integer) UtilityHelper.decryptData(deviceIdS, bankKey);
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " WHERE";
				}
				hql = hql + " tp.device.id = " + deviceId;
				
				String hqlDevice = "Select tp from Device tp where tp.id = " + deviceId;
				device = (Device)this.swpService.getUniqueRecordByHQL(hqlDevice);
			}
			
			transactionList = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql);
				
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Device list fetched");
			jsonObject.put("transactionList", new Gson().toJson(new ArrayList<Transaction>(transactionList)));
			jsonObject.put("merchant", new Gson().toJson(merchant));
			jsonObject.put("device", new Gson().toJson(device));
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Device creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	

	
	@POST
	@Path("/updateDeviceStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateDeviceStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("newStatus") String newStatus, 
			@FormParam("deviceId") String deviceId, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();

		Device device = null;
		try {
			swpService = serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer deviceIdI = (Integer) UtilityHelper.decryptData(deviceId, bankKey);
			
			String hqlDevice = "Select tp from Device tp where tp.id = " + deviceIdI;
			device = (Device)this.swpService.getUniqueRecordByHQL(hqlDevice);
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			
			
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				device.setStatus(DeviceStatus.valueOf(newStatus));
				this.swpService.updateRecord(device);
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
				
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Device status updated successfully");
			jsonObject.put("device", new Gson().toJson(device));
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Device status update Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}*/
	

}
