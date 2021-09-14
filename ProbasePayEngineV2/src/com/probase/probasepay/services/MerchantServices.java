package com.probase.probasepay.services;

import java.math.BigInteger;
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
import javax.ws.rs.core.Response;

import javassist.expr.NewArray;

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
import com.probase.probasepay.models.Country;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantBankAccount;
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


@Path("/MerchantServices")
public class MerchantServices {
	/*private static Logger log = Logger.getLogger(MerchantServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	@POST
	@Path("/deleteMerchantBankAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject deleteMerchantBankAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantBankAccountId") Long merchantBankAccountId, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			String hql = "Select tp from MerchantBankAccount tp where tp.id = " + merchantBankAccountId;
			System.out.println("1. hql_ = " + hql);
			MerchantBankAccount merchantBankAccount = (MerchantBankAccount)swpService.getUniqueRecordByHQL(hql);
			
			if(merchantBankAccount!=null)
			{
				merchantBankAccount.setDeleted_at(new Date());
				this.swpService.updateRecord(merchantBankAccount);
			}
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Merchant bank account deleted successfully");
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			return jsonObject;
		}
	}
	
	
	@POST
	@Path("/createNewMerchantBankAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createNewMerchantBankAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("bankAccount") String bankAccount, 
			@FormParam("bankId") Long bankId, 
			@FormParam("bankAccountName") String bankAccountName, 
			@FormParam("bankBranchCode") String bankBranchCode, 
			@FormParam("merchantId") String token, Long merchantId)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ ==null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			String hql = "Select tp from Merchant tp where tp.id = " + merchantId;
			System.out.println("1. hql_ = " + hql);
			Merchant merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			
			if(merchant!=null)
			{	
				hql = "Select tp from Bank tp where tp.id = " + bankId + " and tp.deleted_at IS NULL";
				Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
				MerchantBankAccount mba = new MerchantBankAccount();
				mba.setMerchant(merchant);
				mba.setBankAccountName(bankAccountName);
				mba.setBankAccountNumber(bankAccount);
				mba.setBankBranchCode(bankBranchCode);
				mba.setCreated_at(new Date());
				mba.setMerchant(merchant);
				mba.setMerchantBank(bank);
				mba.setStatus(Boolean.TRUE);
				mba.setUpdated_at(new Date());
				this.swpService.createNewRecord(mba);
			}
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Merchant bank account created successfully");
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			return jsonObject;
		}
	}
	
	
	
	
	@GET
	@Path("/listMerchantBankAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listMerchantBankAccounts(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantId") Long merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			Collection<MerchantBankAccount> merchantBankAccountList = null;
			
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
			
			if(merchantId==null)
				merchantBankAccountList = (Collection<MerchantBankAccount>)swpService.getAllRecords(MerchantBankAccount.class);
			else
				merchantBankAccountList = (Collection<MerchantBankAccount>)swpService.getAllRecordsByHQL("Select tp from MerchantBankAccount tp where tp.merchant.id = "+(merchantId));
				
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Merchant bank account list fetched");
			jsonObject.put("merchantBankAccountList", new Gson().toJson(new ArrayList<MerchantBankAccount>(merchantBankAccountList)));
			
			if(verifyJ.has("active") && verifyJ.getInt("active")==1)
			{
				System.out.println("Token ==" + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	@POST
	@Path("/createNewMerchant")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewMerchant(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantCode") String merchantCode, @FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, 
			@FormParam("altContactEmail") String altContactEmail, @FormParam("altContactMobile") String altContactMobile, @FormParam("bankAccount") String bankAccount, 
			@FormParam("bankAccountName") String bankAccountName, @FormParam("bankBranchCode") String bankBranchCode, @FormParam("certificateOfIncorporation") String certificateOfIncorporation, 
			@FormParam("companyData") String companyData, @FormParam("companyLogo") String companyLogo, @FormParam("companyName") String companyName, 
			@FormParam("companyRegNo") String companyRegNo, @FormParam("contactEmail") String contactEmail, @FormParam("contactMobile") String contactMobile, 
			@FormParam("merchantBankId") Integer merchantBankId, @FormParam("merchantName") String merchantName, @FormParam("merchantSchemeId") Integer merchantSchemeId,
			@FormParam("deviceType") String deviceType, @FormParam("domainUrl") String domainUrl, @FormParam("forwardSuccessUrl") String forwardSuccessUrl, 
			@FormParam("forwardFailureUrl") String forwardFailureUrl, @FormParam("deviceCode") String deviceCode, @FormParam("deviceSerialNo") String deviceSerialNo, 
			@FormParam("notifyEmail") String notifyEmail, @FormParam("notifyMobile") String notifyMobile, @FormParam("merchantId") String merchantId, 
			@FormParam("token") String token, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName, 
			@FormParam("otherName") String otherName, @FormParam("autoReturnToMerchantSite") Boolean autoReturnToMerchantSite, @FormParam("manualReturnUrlLink") String manualReturnUrlLink, 
			@FormParam("operationCountry") Long operationCountry, @FormParam("zicbAuthKey") String zicbAuthKey, @FormParam("zicbServiceKey") String zicbServiceKey, 
			@FormParam("cybersourceLiveAccessKey") String cybersourceLiveAccessKey, @FormParam("cybersourceLiveProfileId") String cybersourceLiveProfileId, @FormParam("cybersourceLiveSecretKey") String cybersourceLiveSecretKey, 
			@FormParam("cybersourceDemoAccessKey") String cybersourceDemoAccessKey, @FormParam("cybersourceDemoProfileId") String cybersourceDemoProfileId, @FormParam("cybersourceDemoSecretKey") String cybersourceDemoSecretKey, 
			@FormParam("ubaServiceKey") String ubaServiceKey, @FormParam("ubaMerchantId") String ubaMerchantId, @FormParam("mpqrDeviceCode") String mpqrDeviceCode, 
			@FormParam("mpqrDeviceSerialNo") String mpqrDeviceSerialNo, @FormParam("mpqrAcquirerId") Long mpqrAcquirerId, @FormParam("mpqrCardSchemeId") Long mpqrCardSchemeId, 
			@FormParam("mpqrCurrencyCode") String mpqrCurrencyCode, @FormParam("mpqrPoolAccountId") Long mpqrPoolAccountId, @FormParam("mpqrMeansOfIdentificationType") String mpqrMeansOfIdentificationType, 
			@FormParam("mpqrMeansOfIdentificationNumber") String mpqrMeansOfIdentificationNumber)
	{
		//updateStatus = 0 : create
		//updateStatus = 1 : update
		Merchant merchant = null;
		org.hibernate.Transaction txn = null;
		if(merchantId== null)
		{
			merchant = new Merchant();
		}else
		{
			try {
				this.swpService = this.serviceLocator.getSwpService();
				String bankKey = UtilityHelper.getBankKey(null, swpService);
				Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
				String hql_ = "Select tp from Merchant tp where tp.id = " + merchantIdI;
				System.out.println("1. hql_ = " + hql_);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql_);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warn(e);
				merchant = new Merchant();
			}
		}
		JSONObject jsonObject = new JSONObject();
		System.out.println("Create New merchant");
		String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
		String mobileActivationCode = RandomStringUtils.randomNumeric(6);
		try {
			this.swpService = this.serviceLocator.getSwpService();
			//txn = this.swpService.getStartTransaction();
			Application app = Application.getInstance(swpService);
			System.out.println("token111 ==" + token);
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Merchant tp where lower(tp.companyName) = '" + companyName + "'";
			System.out.println("hql = " + hql);
			Merchant mer = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Bank tp where tp.bankCode = '"+ issuerBankCode+"'";
			Bank issuer = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where tp.id = " + mpqrAcquirerId;
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from User tp where lower(tp.username) = '" + contactEmail.toLowerCase() + "'";
			System.out.println("hql = " + hql);
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			System.out.println("user = " + user);
			
			hql = "Select tp from Country tp where lower(tp.id) = " + operationCountry;
			System.out.println("hql = " + hql);
			Country country = (Country)this.swpService.getUniqueRecordByHQL(hql);
			if(country==null)
			{
				jsonObject.put("status", ERROR.COUNTRY_OF_OPERATION_NOT_PROVIDED);
				jsonObject.put("message", "Country of operation not provided");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			System.out.println("country = " + country.getName());
			
			if(mer==null && user==null)
			{
				Bank bank = null;
				String hql_ = "Select tp from Bank tp where tp.id = " + merchantBankId;
				System.out.println("2. hql_ = " + hql_);
				bank = (Bank)this.swpService.getUniqueRecordByHQL(hql_);
				MerchantScheme merchantScheme = null;
				hql_ = "Select tp from MerchantScheme tp where tp.id = " + merchantSchemeId;
				System.out.println("3. hql_ = " + hql_);
				merchantScheme = (MerchantScheme)this.swpService.getUniqueRecordByHQL(hql_);
				
				
				
				
				
				
				merchant.setAddressLine1(addressLine1);
				merchant.setAddressLine2(addressLine2);
				merchant.setAltContactEmail(altContactEmail);
				merchant.setAltContactMobile(altContactMobile);
				merchant.setBankAccount(bankAccount);
				merchant.setCertificateOfIncorporation(certificateOfIncorporation);
				merchant.setCompanyData(companyData);
				merchant.setCompanyLogo(companyLogo);
				merchant.setCompanyRegNo(companyRegNo);
				merchant.setContactMobile(contactMobile);
				merchant.setContactEmail(contactEmail);
				if(merchantId== null)
				{
					merchant.setMerchantCode(merchantCode);
					merchant.setStatus(MerchantStatus.ACTIVE);
					merchant.setCompanyName(companyName);
					merchant.setMerchantName(merchantName);
					merchant.setApiKey(RandomStringUtils.randomAlphanumeric(32));
					merchant.setMerchantDecryptKey(RandomStringUtils.randomAlphanumeric(32));
				}
				merchant.setManualReturnUrlLink(manualReturnUrlLink);
				merchant.setAutoReturnToMerchantSite(autoReturnToMerchantSite);
				merchant.setMerchantBank(bank);
				merchant.setMerchantScheme(merchantScheme);
				merchant.setCreated_at(new Date());
				merchant.setUpdated_at(new Date());
				merchant.setMobileActivationCode(mobileActivationCode);
				merchant.setWebActivationCode(webActivationCode);
				merchant.setCountryOfOperation(country);
				merchant.setBankBranchCode(bankBranchCode);
				
				
				
				
				MerchantBankAccount mba = null;
				if(merchantId== null)
				{
					System.out.println("NULL is merchantId");
					merchant = (Merchant)this.swpService.createNewRecord(merchant);
					
					mba = new MerchantBankAccount();
					mba.setMerchant(merchant);
					mba.setBankAccountName(bankAccountName);
					mba.setBankAccountNumber(bankAccount);
					mba.setBankBranchCode(bankBranchCode);
					mba.setCreated_at(new Date());
					mba.setMerchant(merchant);
					mba.setMerchantBank(bank);
					mba.setStatus(Boolean.TRUE);
					mba.setUpdated_at(new Date());
					this.swpService.createNewRecord(mba);
					
				}else
				{
					this.swpService.updateRecord(merchant);
				}
				if(merchantId== null)
				{
					
					
					if(user==null)
					{
						System.out.println("user is null ");
						user = new User();
						
						user.setStatus(UserStatus.ACTIVE);
						user.setRoleType(RoleType.MERCHANT);
						String pswd = RandomStringUtils.randomAlphanumeric(8);
						
							user.setPassword(pswd);
							user.setWebActivationCode(webActivationCode);
							user.setMobileActivationCode(mobileActivationCode);
							user.setFailedLoginCount(0);
							user.setUsername(contactEmail);
							user.setLockOut(Boolean.FALSE);
							user.setCreated_at(new Date());
						
						user.setFirstName(firstName);
						user.setLastName(lastName);
						user.setOtherName(otherName);
						user.setMobileNo(contactMobile);
						user = (User)this.swpService.createNewRecord(user);
						
						merchant.setUser(user);
						this.swpService.updateRecord(merchant);
						
						if(mba!=null)
						{
							mba.setAddedByUser(user);
							this.swpService.updateRecord(mba);
						}
						
						Device device = new Device();
						device.setCreated_at(new Date());
						device.setUpdated_at(new Date());
						device.setDeviceCode(deviceCode);
						device.setDeviceSerialNo(deviceSerialNo);
						device.setDeviceType(DeviceType.valueOf(deviceType));
						device.setDomainUrl(domainUrl);
						device.setEmailNotify(notifyEmail);
						device.setMobileNotify(notifyMobile);
						device.setSuccessUrl(forwardSuccessUrl);
						device.setFailureUrl(forwardFailureUrl);
						device.setMerchant(merchant);
						device.setStatus(DeviceStatus.ACTIVE);
						device.setSetupByUser(user);
						device.setBankOnlineAccept(Boolean.TRUE);
						device.setEagleCardAccept(Boolean.TRUE);
						device.setMastercardVisaAccept(Boolean.TRUE);
						device.setMobileMoneyAccept(Boolean.TRUE);
						device.setSwitchToLive(Boolean.FALSE);
						device.setWalletAccept(Boolean.TRUE);
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
						
						if(mpqrDeviceCode!=null && mpqrDeviceSerialNo!=null)
						{
							String verificationNumber = RandomStringUtils.randomNumeric(10);
				        	Date dateOfBirth = null;
				        	Gender gender = null;
				        	District locationDistrict = null;
				        	CustomerStatus status = CustomerStatus.ACTIVE;
				        	String customerImage = null;
				        	CustomerType customerType = CustomerType.CORPORATE;
				        	String mobileMoneyPassword = null;
				        	MeansOfIdentificationType meansOfIdentificationType = null;
				        	String meansOfIdentificationNumber = null;
				        	String currencyCode = ProbasePayCurrency.ZMW.name();
				        	User setUpByUser = tokenUser;
				        	
				        	JSONObject dataJSON = new JSONObject();
				        	dataJSON.put("deviceId", device.getId());
				        	dataJSON.put("acquirerId", mpqrAcquirerId);
				        	dataJSON.put("cardSchemeId", mpqrCardSchemeId);
				        	dataJSON.put("currencyCodeId", mpqrCurrencyCode);
				        	dataJSON.put("poolAccountId", mpqrPoolAccountId);
				        	dataJSON.put("customerType", CustomerType.CORPORATE.name());
				        	dataJSON.put("meansOfIdentificationType", mpqrMeansOfIdentificationType);
							dataJSON.put("meansOfIdentificationNumber", mpqrMeansOfIdentificationNumber);
							String encryptedData = dataJSON.toString();
							encryptedData = (String) UtilityHelper.encryptData(encryptedData, bankKey);
							TutukaServicesV2 tutukaServices = new TutukaServicesV2();
							JSONObject qrResp = tutukaServices.createQRData(httpHeaders, requestContext, encryptedData, token);
									
							
							ECard card = (ECard)qrResp.get("card");
							Customer settlementCustomer = (Customer)qrResp.get("customer");
							device.setSettlementCardId(card.getId());
							this.swpService.updateRecord(device);
							
							merchant.setSettlementCustomerId(settlementCustomer.getId());
							this.swpService.updateRecord(merchant);
						}
						
						jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
						jsonObject.put("message", "New merchant created");
						jsonObject.put("merchantCode", merchant.getMerchantCode());
						jsonObject.put("deviceCode", device.getDeviceCode());
						jsonObject.put("password", pswd);
						jsonObject.put("contactMobile", contactMobile);
						jsonObject.put("message", "New merchant created");
						
						System.out.println("Create New merchant = " + jsonObject.toString());
					}
					else
					{
						txn.rollback();
						jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS_NO_USER_ACCOUNT);
						jsonObject.put("message", "New Merchant Account Created Successfully. Merchant cant use web interface due to " +
								"a matching email was found with the customers email.");
					}
				}
				else
				{
					if(merchantId== null)
					{
						user = merchant.getUser();
						user.setFirstName(firstName);
						user.setLastName(lastName);
						user.setOtherName(otherName);
						user.setMobileNo(contactMobile);
						this.swpService.updateRecord(user);
					}
					txn.rollback();
					jsonObject.put("merchantCode", merchant.getMerchantCode());
					jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
					jsonObject.put("message", "Merchant Profile Update Successful");
				}
				
				
				jsonObject.put("token", verifyJ.getString("token"));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("status", ERROR.MERCHANT_SETUP_COMPANY_NAME_EXIST);
				jsonObject.put("message", "Company Name or users email address already exist. Provide another company name or useremail address.");
				jsonObject.put("token", verifyJ.getString("token"));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
			txn.rollback();
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
		}catch (Exception e) {
			// TODO Auto-generated catch block
			txn.rollback();
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
	@Path("/getMerchantByMerchantName")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMerchantByMerchantName(@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantName") String merchantName, 
			@QueryParam("merchantId") Long merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			List<BigInteger> merchantList = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(merchantId==null)
			{
				String merchantSql = "Select tp.id from merchants tp where tp.merchantName = '"+ merchantName +"'";
				merchantList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			else
			{
				String merchantSql = "Select tp.id from merchants tp where tp.merchantName = '"+ merchantName +"' AND tp.id != " + merchantId;
				merchantList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			
				
			
			if(merchantList.size()>0)
			{
				jsonObject.put("message", "Merchant matching merchant name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			}
			else
			{
				jsonObject.put("message", "No merchant matching merchant name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
			}
			
			
			if(verifyJ.has("active") && verifyJ.getInt("active")==1)
			{
				System.out.println("Token ==" + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
		
	
	
	@GET
	@Path("/listMerchants")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listMerchants(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") String status, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			Collection<Merchant> merchantList = null;
			
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
			
			if(status==null)
				merchantList = (Collection<Merchant>)swpService.getAllRecords(Merchant.class);
			else
				merchantList = (Collection<Merchant>)swpService.getAllRecordsByHQL("Select tp from Merchant tp where tp.status = "+(MerchantStatus.valueOf(status).ordinal())+"");
			
			Iterator<Merchant> iterMerchant = merchantList.iterator();
			JSONArray allMerchants = new JSONArray();
			while(iterMerchant.hasNext())
			{
				Merchant merchant = iterMerchant.next();
				JSONObject oneMerchant = new JSONObject();
				oneMerchant.put("id", merchant.getId());
				oneMerchant.put("merchantName", merchant.getMerchantName());
				oneMerchant.put("contactEmail", merchant.getContactEmail());
				oneMerchant.put("merchantCode", merchant.getMerchantCode());
				oneMerchant.put("bankAccount", merchant.getBankAccount());
				oneMerchant.put("bank", merchant.getMerchantBank()==null ? "" : merchant.getMerchantBank().getBankName());
				oneMerchant.put("primaryContactPerson", merchant.getUser()==null ? "" : (merchant.getUser().getLastName() + " " + merchant.getUser().getFirstName()));
				oneMerchant.put("status", merchant.getStatus());
				allMerchants.put(oneMerchant);
			}
				
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant list fetched");
			jsonObject.put("merchantlist", allMerchants);
			
			
			if(verifyJ.has("active") && verifyJ.getInt("active")==1)
			{
				System.out.println("Token ==" + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	
	
	
	
	@POST
	@Path("/updateMerchantStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateMerchantStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("status") String status, 
			@FormParam("token") String token)
	{
		Merchant merchant;
		JSONObject jsonObject = new JSONObject();
		try {
			
			this.swpService = this.serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
			
			String hql = "Select tp from Merchant tp where tp.id = "+(merchantIdI);
			
			merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			Gson gson = new Gson();
			String obj = gson.toJson(merchant);
			if(merchant!=null)
			{
				
				
				merchant.setStatus(MerchantStatus.valueOf(status));
				try {
					this.swpService.updateRecord(merchant);
					
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
					jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_SUCCESS);
					jsonObject.put("message", "Merchant status update was Successful");
					jsonObject.put("merchant", obj);
					return jsonObject;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.warn(e);
					jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_FAIL);
					jsonObject.put("message", "Merchant status update failed");
					return jsonObject;
				}
				
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
		}
		
		try {
			jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_FAIL_NO_MERCHANT);
			jsonObject.put("message", "Invalid merchant provided");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
		}
		
		return jsonObject;
	}
	
	
	
	@POST
	@Path("/activateMerchant")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject activateMerchant(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("webTrue") Boolean webTrue, 
			@FormParam("activationCode") String activationCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("token") String token)
	{
		Merchant merchant;
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
			merchant = (Merchant)this.swpService.getRecordById(Merchant.class, Long.valueOf(merchantIdI));
			if(merchant!=null)
			{
				if((webTrue.equals(Boolean.TRUE) && merchant.getWebActivationCode().equals(activationCode)) || 
				(webTrue.equals(Boolean.FALSE) && merchant.getMobileActivationCode().equals(activationCode)))
				{
					merchant.setStatus(MerchantStatus.ACTIVE);
					merchant.setWebActivationCode(null);
					merchant.setMobileActivationCode(null);
					try {
						this.swpService.updateRecord(merchant);
						
						
						
						
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
						
						
						
						jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_SUCCESS);
						jsonObject.put("message", "Merchant status update was Successful. Merchant activation successful");
						return jsonObject;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.warn(e);
						jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_FAIL);
						jsonObject.put("message", "Merchant status update failed. Merchant activation failed");
						return jsonObject;
					}
				}else
				{
					jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_FAIL);
					jsonObject.put("message", "Merchant status update failed. Merchant activation failed");
					return jsonObject;
				}
				
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
		}
		
		try {
			jsonObject.put("status", ERROR.MERCHANT_UPDATE_STATUS_FAIL_NO_MERCHANT);
			jsonObject.put("message", "Invalid merchant provided");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
		}
		
		return jsonObject;
	}
	
	
	
	
	
	
	
	
	@GET
	@Path("/getMerchantAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMerchantAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantId") String merchantId, 
			@QueryParam("token") String token)
	{
		
		
		
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
			
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer merchantIdL = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
			
			
			Merchant merchant = null;
			String hql = "Select tp from Merchant tp where tp.id = "+(merchantIdL)+"";
			
			merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			Gson gson = new Gson();
			String obj = gson.toJson(merchant);
				
			
			

			
			
			
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
			
			
			
			
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant fetched");
			jsonObject.put("merchant", obj);
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	@POST
	@Path("/updateMerchantAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateMerchantAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("status") String status, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Merchant merchant = null;
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
			String hql = "Select tp from Merchant tp where tp.id = "+(merchantIdI)+"";
			
			merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			merchant.setStatus(MerchantStatus.valueOf(status));
			swpService.updateRecord(merchant);
			
			Gson gson = new Gson();
			String obj = gson.toJson(merchant);
				
			
			

			
			
			
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
			
			
			
			
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant Update Successful");
			jsonObject.put("merchant", obj);
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant Updated Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	@GET
	@Path("/getMerchantDevices")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getMerchantDevices(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantId") String merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Merchant merchant = null;
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
			String hql = "Select tp from Merchant tp where tp.id = "+(merchantIdI)+"";
			
			merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			Gson gson = new Gson();
			String obj = gson.toJson(merchant);
			
			hql = "Select tp from Device tp where tp.merchant.id = "+(merchantIdI)+"";
			Collection<Device> merchantdevicelist = (Collection<Device>)swpService.getAllRecordsByHQL(hql);
			
				
			

			
			
			
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
			
			
			
			
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant devices fetched");
			jsonObject.put("merchant", obj);
			jsonObject.put("merchantdevicelist", new Gson().toJson(new ArrayList(merchantdevicelist)));
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant transactions fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	*/

}
