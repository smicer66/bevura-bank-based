package com.probase.probasepay.services;

import java.text.DateFormat;
import java.text.ParseException;
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
import com.probase.probasepay.enumerations.MPQRDataStatus;
import com.probase.probasepay.enumerations.MPQRDataType;
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
import com.probase.probasepay.models.MPQRData;
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

@Path("/DeviceServicesV2")
public class DeviceServicesV2 {
	private static Logger log = Logger.getLogger(DeviceServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	@POST
	@Path("/createNewMerchantDevice")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewMerchantDeviceV2(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("deviceId") String deviceId, 
			@FormParam("deviceType") Integer deviceType, 
			@FormParam("domainUrl") String domainUrl, 
			@FormParam("forwardSuccessUrl") String forwardSuccessUrl, 
			@FormParam("forwardFailureUrl") String forwardFailureUrl, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("deviceSerialNo") String deviceSerialNo, 
			@FormParam("notifyEmail") String notifyEmail, 
			@FormParam("notifyMobile") String notifyMobile, 
			@FormParam("token") String token, 
			@FormParam("switchToLive") Integer switchToLive, 
			@FormParam("mastercardAccept") Integer mastercardAccept, 
			@FormParam("visaAccept") Integer visaAccept, 
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
			@FormParam("mpqrCurrencyCode") String mpqrCurrencyCode, 
			@FormParam("mpqrDataType") String mpqrDataType, 
			
			@FormParam("walletNumber") String walletNumber
			)
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
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					else
					{
						jsonObject.put("token", verifyJ.getString("token"));
					}
					
					String subject = verifyJ.getString("subject");
					User usr = new Gson().fromJson(subject, User.class);
					System.out.println("verifyJ ==" + verifyJ.toString());
					
					
					String acquirerCode = verifyJ.getString("acquirerCode");
					System.out.println("acquirerCode ==" + acquirerCode);
					
					String bankKey = UtilityHelper.getBankKey(null, swpService);
					//Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
					String hql = "Select tp from Merchant tp where tp.merchantCode = '" + merchantId + "'";
					merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
					
					device.setCreated_at(new Date());
					device.setUpdated_at(new Date());
					device.setDeviceType(DeviceType.values()[deviceType]);
					device.setSwitchToLive(switchToLive!=null && switchToLive==1 ? 1 : 0);
					device.setUpdated_at(new Date());
					device.setSetupByUser(usr);
					if(notifyEmail!=null && notifyEmail.length()>0)
						device.setEmailNotify(notifyEmail);
					else
						device.setEmailNotify(null);
					
					if(notifyMobile!=null && notifyMobile.length()>0)
						device.setMobileNotify(notifyMobile);
					else
						device.setMobileNotify(null);
					
					if(deviceId == null)
						device.setMerchant(merchant);
					if(deviceId == null)
						device.setStatus(DeviceStatus.ACTIVE);
					
					
					
					if(deviceType.equals(DeviceType.ATM.ordinal()))
					{
						if(deviceId == null)
						{
							device.setDeviceCode(RandomStringUtils.randomNumeric(10).toUpperCase());
						}
						device.setDeviceSerialNo(deviceSerialNo!=null && deviceSerialNo.length()>0 ? deviceSerialNo : RandomStringUtils.randomAlphanumeric(10));
						device.setSuccessUrl(forwardSuccessUrl);
						if(deviceId==null)
							device = (Device)this.swpService.createNewRecord(device);
						else
							this.swpService.updateRecord(device);
					}
					else if(deviceType.equals(DeviceType.POS.ordinal()))
					{
						if(deviceId == null)
						{
							device.setDeviceCode(RandomStringUtils.randomNumeric(10).toUpperCase());
						}
						device.setDeviceSerialNo(deviceSerialNo!=null && deviceSerialNo.length()>0 ? deviceSerialNo : RandomStringUtils.randomAlphanumeric(10));
						device.setSuccessUrl(forwardSuccessUrl);
						if(deviceId==null)
							device = (Device)this.swpService.createNewRecord(device);
						else
							this.swpService.updateRecord(device);
					}
					else if(deviceType.equals(DeviceType.MPQR.ordinal()))
					{

						hql = "Select tp from Account tp where tp.accountIdentifier = '"+walletNumber+"'";
						Account wallet = (Account)this.swpService.getUniqueRecordByHQL(hql);
						if(mpqrDataType.equals(MPQRDataType.PERSONAL.name()))
						{
							hql = "Select tp from MPQRData tp where tp.walletAccount.id = " + wallet.getId() + " AND tp.deleted_at IS NULL AND tp.status = " + MPQRDataStatus.ACTIVE.ordinal() 
							+ " AND tp.mpqrDataType = " + MPQRDataType.PERSONAL.ordinal();
							Collection<MPQRData> mpqrDataExisting = (Collection<MPQRData>)this.swpService.getAllRecordsByHQL(hql);
							if(mpqrDataExisting.size()>0)
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "You can not create a personal MasterCard QR Pass because you already have an active MasterCard QR Pass");
								return Response.status(200).entity(jsonObject.toString()).build();
							}
						}
						device.setDeviceSerialNo(deviceSerialNo!=null && deviceSerialNo.length()>0 ? deviceSerialNo : RandomStringUtils.randomAlphanumeric(10));
						if(mpqrDeviceSerialNo!=null)
							device.setMpqrDeviceSerialNo(mpqrDeviceSerialNo);
						else
							device.setMpqrDeviceSerialNo(RandomStringUtils.randomNumeric(16).toUpperCase());
						
						if(deviceId == null)
						{
							device.setDeviceCode(RandomStringUtils.randomNumeric(10).toUpperCase());
							if(mpqrDeviceCode!=null)
							{
								device.setMpqrDeviceCode(mpqrDeviceCode);
							}
							else
							{
								device.setMpqrDeviceCode(RandomStringUtils.randomNumeric(16).toUpperCase());
							}
							
							if(device.getMpqrDeviceSerialNo()!=null && device.getMpqrDeviceCode()!=null)
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
					        	//CustomerType customerType = CustomerType.valueOf(customerTypeStr);
					        	String mobileMoneyPassword = null;
					        	//MeansOfIdentificationType meansOfIdentificationType = null;
					        	//String meansOfIdentificationNumber = null;
					        	String currencyCode = ProbasePayCurrency.ZMW.name();
					        	User setUpByUser = usr;
								TutukaServicesV2 tutukaServices = new TutukaServicesV2();
								JSONObject deviceData = new JSONObject();
								
								device = (Device)this.swpService.createNewRecord(device);
								
								deviceData.put("deviceId", device.getId());
								//deviceData.put("customerId", );
								
								if(usr.getRoleType().equals(RoleType.BANK_STAFF) || usr.getRoleType().equals(RoleType.POTZR_STAFF))
								{
									
									deviceData.put("acquirerId", mpqrAcquirerId);
									deviceData.put("cardSchemeId", mpqrCardSchemeId);
									deviceData.put("currencyCodeId", mpqrCurrencyCode);
									deviceData.put("walletNumber", walletNumber);
									deviceData.put("mpqrDataType", mpqrDataType);
								}
								else if(usr.getRoleType().equals(RoleType.CUSTOMER))
								{
									/*hql = "Select tp from Acquirer tp where tp.mpqr_enabled = 1 AND tp.deleted_at IS NULL";
									Issuer acquirer = (Issuer)this.swpService.getUniqueRecordByHQL(hql);
									if(acquirer==null)
									{
										jsonObject.put("status", ERROR.ACQUIRER_NOT_FOUND);
										jsonObject.put("message", "Device could not be added to the merchant profile");
										return Response.status(200).entity(jsonObject.toString()).build();
									}
									
									hql = "Select tp from CardScheme tp where tp.default_customer = 1 AND tp.deleted_at IS NULL";
									CardScheme cardScheme = (CardScheme)this.swpService.getUniqueRecordByHQL(hql);
									if(cardScheme==null)
									{
										jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_FAIL);
										jsonObject.put("message", "Device could not be added to the merchant profile");
										return Response.status(200).entity(jsonObject.toString()).build();
									}*/
									
									deviceData.put("acquirerId", mpqrAcquirerId);
									deviceData.put("cardSchemeId", mpqrCardSchemeId);
									deviceData.put("currencyCodeId", mpqrCurrencyCode);
									deviceData.put("walletNumber", walletNumber);
									deviceData.put("mpqrDataType", mpqrDataType);
								}
									
								
								
								String encryptedData = deviceData.toString();
								log.info(encryptedData);
								encryptedData = (String) UtilityHelper.encryptData(encryptedData, bankKey);
								JSONObject dataCreated = tutukaServices.createQRData(httpHeaders, requestContext, encryptedData, token);
								
								if(dataCreated!=null && dataCreated.getInt("status")==ERROR.MPQR_ACCOUNT_CREATED_SUCCESSFULLY)
								{
									String mpqrDataFileName = dataCreated.getString("mpqrDataFileName");
									jsonObject.put("merchantCode", device.getMerchant().getMerchantCode());
									jsonObject.put("deviceCode", device.getDeviceCode());
									jsonObject.put("mpqrDeviceCode", device.getMpqrDeviceCode());
									jsonObject.put("mpqrDataFileName", mpqrDataFileName);
									jsonObject.put("status", ERROR.DEVICE_ADD_SUCCESS);
									jsonObject.put("notifyMerchantMobile", device.getMerchant().getContactMobile());
									jsonObject.put("message", "New Merchant Device " + (deviceId == null ? "Added" : "Updated")+ " Successfully");
									System.out.println("Create New merchant device = " + jsonObject.toString());
								}
								else
								{
									jsonObject.put("status", dataCreated.has("status") ? dataCreated.getInt("status") : ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Creation/Update of MPQR was not successful");
									return Response.status(200).entity(jsonObject.toString()).build();
								}
								
								return Response.status(200).entity(jsonObject.toString()).build();
							}
						}
						else
						{
							this.swpService.updateRecord(device);
						}
					}
					else if(deviceType.equals(DeviceType.WEB.ordinal()))
					{
						if(deviceId == null)
						{
							device.setDeviceCode(RandomStringUtils.randomNumeric(10).toUpperCase());
						}
						device.setDeviceSerialNo(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
						device.setDomainUrl(domainUrl);
						device.setSuccessUrl(forwardSuccessUrl);
						device.setFailureUrl(forwardFailureUrl);
						device.setMastercardAccept(mastercardAccept!=null && mastercardAccept==1 ? true : false);
						device.setVisaAccept(visaAccept!=null && visaAccept==1 ? true : false);
						device.setBankOnlineAccept(bankOnlineAccept!=null && bankOnlineAccept==1 ? true : false);
						device.setMobileMoneyAccept(mobileMoneyAccept!=null && mobileMoneyAccept==1 ? true : false);
						device.setWalletAccept(walletAccept!=null && walletAccept==1 ? true : false);

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
						
						if(deviceId==null)
							device = (Device)this.swpService.createNewRecord(device);
						else
							this.swpService.updateRecord(device);
					}
					
					
					

					jsonObject.put("merchantCode", device.getMerchant().getMerchantCode());
					jsonObject.put("deviceCode", device.getDeviceCode());
					jsonObject.put("status", ERROR.DEVICE_ADD_SUCCESS);
					jsonObject.put("notifyMerchantMobile", device.getMerchant().getContactMobile());
					jsonObject.put("message", "New Merchant Device " + (deviceId == null ? "Added" : "Updated")+ " Successfully");
					System.out.println("Create New merchant device = " + jsonObject.toString());
					
					return Response.status(200).entity(jsonObject.toString()).build();
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.error(e);
					jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
					jsonObject.put("message", "Merchant Device Update Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}else
			{
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				return Response.status(200).entity(jsonObject.toString()).build();
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
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
		
		
	}
	
	
	
	@GET
	@Path("/getADevice")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getADevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("deviceId") Long deviceId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		List<Map<String, Object>> deviceList = null;
		try {
			swpService = serviceLocator.getSwpService();
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
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
			
			
			String subject = verifyJ.getString("subject");
			User usr = new Gson().fromJson(subject, User.class);
			
			String hqlDevice = "Select tp.*, m.merchantName, m.merchantCode from devices tp, merchants m where tp.merchant_id = m.id"
			+ " AND tp.id = " + deviceId;
			if(usr.getRoleType().equals(RoleType.CUSTOMER))
			{
				hqlDevice = hqlDevice + " AND m.user_id = " + usr.getId();
			}
			System.out.println(hqlDevice);
			deviceList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hqlDevice);
			if(deviceList!=null && deviceList.size()>0)
			{
				Map<String, Object> device = deviceList.get(0);
				
				if(!usr.getRoleType().equals(RoleType.CUSTOMER))
				{
					String key = "cybersourceDemoAccessKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "cybersourceDemoProfileId";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "cybersourceDemoSecretKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "cybersourceLiveAccessKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "cybersourceLiveProfileId";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "cybersourceLiveSecretKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "ubaMerchantId";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "ubaServiceKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "zicbAuthKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
					key = "zicbServiceKey";
					if(device.get(key)!=null)
					{
						device.put(key, "************");
					}
				}
				
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
				jsonObject.put("message", "Device fetched");
				jsonObject.put("device", device);
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{

				
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Device fetched failed");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}

	
	
	
	@GET
	@Path("/listDevice")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") String status, 
			@QueryParam("merchantId") Long merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		Merchant merchant = null;
		try {
			this.swpService = serviceLocator.getSwpService();
			List<Device> deviceList = null;
			
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("...verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
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
			
			String subject = verifyJ.getString("subject");
			User usr = new Gson().fromJson(subject, User.class);
			System.out.println("usrid ==>" + usr.getId());
			
			String merchantName = null;
			
			List<Object[]> deviceList_ = new ArrayList<Object[]>();
			if(merchantId!=null)
			{
				
				
				String hql = "Select tp.merchantName from merchants tp where tp.id = " + merchantId;
				System.out.println(hql);
				List<Map<String, Object>> merchants_ = (List<Map<String, Object>>)swpService.getQueryBySQLResults(hql);
				merchantName = (String)(merchants_.get(0).get("merchantName"));
				System.out.println(merchantName);
				
				hql = "Select tp.id, m.merchantName, tp.deviceType, tp.deviceCode, tp.successUrl, " +
						"tp.failureUrl, tp.emailNotify, tp.mobileNotify, tp.status, tp.merchant_id as merchantId from devices tp, merchants m where "
						+ "tp.merchant_id = m.id ";
				int and_ = 0;
				if(status!=null)
				{
					hql = hql + " AND tp.status = "+ DeviceStatus.valueOf(status).ordinal();
					and_ = 1;
				}
				hql = hql + " AND tp.merchant_id = " + merchantId;
				System.out.println(hql);
				deviceList_ = (List<Object[]>)swpService.getQueryBySQLResults(hql);
				
				
				String hqlMerchant = "Select tp from Merchant tp where tp.id = " + merchantId;
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hqlMerchant);

				JSONObject merchantData = new JSONObject();
				merchantData.put("id", merchant.getId());
				merchantData.put("name", merchant.getMerchantName());
				jsonObject.put("merchant", merchantData);
				jsonObject.put("merchantName", (merchantName));
			}
			else
			{
				
				String hql = "Select tp.id, m.merchantName, tp.deviceType, tp.deviceCode, tp.successUrl, " +
						"tp.failureUrl, tp.emailNotify, tp.mobileNotify, tp.status, tp.merchant_id as merchantId from devices tp, merchants m";
				int and_ = 0;
				if(status!=null)
				{
					hql = hql + " where tp.status = "+ DeviceStatus.valueOf(status).ordinal();
					and_ = 1;
				}
				System.out.println(hql);
				
				deviceList_ = (List<Object[]>)swpService.getQueryBySQLResults(hql);
			}
			
			/*Iterator<Device> deviceIter = deviceList.iterator();
			JSONArray allDevices = new JSONArray();
			while(deviceIter.hasNext())
			{
				Device device = deviceIter.next();
				JSONObject oneDevice = new JSONObject();
				oneDevice.put("merchantName", device.getMerchant().getMerchantName());
				oneDevice.put("deviceType", device.getDeviceType().name());
				oneDevice.put("deviceCode", device.getDeviceCode());
				oneDevice.put("successUrl", device.getSuccessUrl());
				oneDevice.put("failureUrl", device.getFailureUrl());
				oneDevice.put("emailNotify", device.getEmailNotify());
				oneDevice.put("mobileNotify", device.getMobileNotify());
				oneDevice.put("status", device.getStatus().name());
				oneDevice.put("id", device.getId());
				oneDevice.put("merchantName", device.getMerchant().getMerchantName());
				allDevices.put(oneDevice);
			}*/
			
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Device list fetched");
			jsonObject.put("devicelist", (deviceList_));
			
			return Response.status(200).entity(jsonObject.toString()).build();
			
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	@POST
	@Path("/validateQRCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateQRCode(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("token") String token, 
			@FormParam("merchantCode") String merchantCode,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("qrCode") String qrCode)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
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
			
			
			int isLive = 0;
			if(merchantCode!=null && deviceCode!=null)
			{
				String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
				
	
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				device = (Device)swpService.getUniqueRecordByHQL(hql);

				if(device!=null && device.getSwitchToLive().equals(1))
				{
					isLive = 1;
				}
			}
			if(device==null || merchant==null)
			{

				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide all necessary information to create a wallet and card");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			Collection<Transaction> transactionList = null;
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			
			String hql = "Select tp from MPQRData tp where tp.qrDataString = '"+ qrCode +"' AND tp.status = " + MPQRDataStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
			MPQRData mpqrData = (MPQRData)this.swpService.getUniqueRecordByHQL(hql);
			if(mpqrData!=null)
			{
				log.info(mpqrData.getId());
				hql = "Select tp from Device tp where tp.id = '"+ mpqrData.getDeviceId() +"' AND tp.status = " + DeviceStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL AND tp.switchToLive = " + isLive;
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
				if(device!=null)
				{
					merchant = device.getMerchant();
					
					jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
					jsonObject.put("message", "MPQR Merchant Found");
					jsonObject.put("merchantName", merchant.getMerchantName());
					jsonObject.put("mpqrDeviceCode", device.getMpqrDeviceCode());
					jsonObject.put("deviceCode", device.getDeviceCode());
					
				}
				else
				{
					jsonObject.put("status", ERROR.INVALID_DEVICE_DETAIL);
					jsonObject.put("message", "Active device matching QR Code not found");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INVALID_QR_CODE);
				jsonObject.put("message", "Invalid code provided");
			}
				
			
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "We experienced an issue reading the QR Code scanned");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	@POST
	@Path("/listMPQR")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMPQR(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("token") String token)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
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
			
			
			
			Collection<Transaction> transactionList = null;
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			
			String hql = "Select mpqr_data.*, "
					//+ "ecards.nameOnCard, "
					+ "accounts.accountIdentifier, "
					+ "customers.firstName, "
					+ "customers.lastName, "
					+ "devices.deviceCode, devices.deviceSerialNo, merchants.merchantName from mpqr_data "
					//+ "left join ecards ON mpqr_data.cardId = ecards.id "
					+ "left join accounts ON mpqr_data.walletAccount_id = accounts.id "
					+ "left join customers ON accounts.customer_id = customers.id "
					+ "left join devices ON mpqr_data.deviceId = devices.id "
					+ "left join merchants ON devices.merchant_id = merchants.id "
					+ "WHERE mpqr_data.status = " + MPQRDataStatus.ACTIVE.ordinal();
			List<Map<String, Object>> mpqr_data_list = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
				
			jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "MPQR list fetched");
			jsonObject.put("data", mpqr_data_list);
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "We experienced an issue getting the list of MPQR data");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
}
