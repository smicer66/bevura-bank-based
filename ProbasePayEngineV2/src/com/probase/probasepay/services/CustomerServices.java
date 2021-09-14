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
import javax.ws.rs.POST;
import javax.ws.rs.GET;
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
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardBin;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/CustomerServices")
public class CustomerServices {

	/*
	private static Logger log = Logger.getLogger(CustomerServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	
	@POST
	@Path("/createNewCustomerAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createNewCustomerAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerId") String customerId, 
			@FormParam("addressLine1") String addressLine1, 
			@FormParam("addressLine2") String addressLine2, 
			@FormParam("altContactEmail") String altContactEmail, 
			@FormParam("altContactMobile") String altContactMobile, 
			@FormParam("contactEmail") String contactEmail, 
			@FormParam("contactMobile") String contactMobile, 
			@FormParam("dateOfBirth") String dateOfBirth, 
			@FormParam("customerImage") String customerImage,
			@FormParam("firstName") String firstName, 
			@FormParam("gender") String gender, 
			@FormParam("lastName") String lastName, 
			@FormParam("otherName") String otherName, 
			@FormParam("locationDistrict_id") Integer locationDistrict_id, 
			@FormParam("cardSchemeId") Integer cardSchemeId, 
			@FormParam("nameOnCard") String nameOnCard, 
			@FormParam("countryCode") String countryCode, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("eWalletAccountCreateTrue") Boolean eWalletAccountCreateTrue, 
			@FormParam("mobileMoneyCreateTrue") Boolean mobileMoneyCreateTrue, 
			@FormParam("token") String token, 
			@FormParam("customerType") String customerType, 
			@FormParam("parentCustomerId") Long parentCustomerId, 
			@FormParam("parentAccountId") Long parentAccountId, 
			@FormParam("cardType") String cardType, 
			@FormParam("meansOfIdentificationType") String meansOfIdentificationType, 
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber, 
			@FormParam("acquirerId") String acquirerId)
	{
		 //acquirerId= "PROBASE";
		
		JSONObject jsonObject = new JSONObject();
		System.out.println("Create New Customer");
		try {
			int mpin = Integer.valueOf(RandomStringUtils.randomNumeric(4));
			int pin = Integer.valueOf(RandomStringUtils.randomNumeric(4));
			this.swpService = this.serviceLocator.getSwpService();
			
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
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			
			District locationDistrict = null;
			String hql = "Select tp from District tp where tp.id = " + locationDistrict_id;
			System.out.println("hql ==" + hql);
			locationDistrict = (District)this.swpService.getUniqueRecordByHQL(hql);
			Date dob = null;
			if(dateOfBirth!=null && dateOfBirth.length()>0)
			{
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				dob = df.parse(dateOfBirth);
			}
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			System.out.println("hql ==" + hql);
			CardScheme cardScheme = (CardScheme)this.swpService.getUniqueRecordByHQL(hql);
			if(cardScheme!=null)
			{
				hql = "Select tp from Bank tp where tp.bankCode = '" + bankCode + "'";
				System.out.println("hql ==" + hql);
				Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
				Bank issuer = bank;
				hql = "Select tp from Acquirer tp where lower(tp.acquirerName) = '" + acquirerId.toLowerCase() + "'";
				System.out.println("hql ==" + hql);
				Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
				WalletServicesV2 walletServices = new WalletServicesV2();
				hql = "Select tp from Device tp where tp.probaseOwned = 1 AND tp.deleted_at IS NULL";
				Collection<Device> probaseDevices = (Collection<Device>)this.swpService.getAllRecordsByHQL(hql);
				Device probaseDevice = probaseDevices.iterator().next();
				
				String merchantCode = probaseDevice.getMerchant().getMerchantCode();
				String deviceCode = probaseDevice.getDeviceCode();
				String addressLine3 = locationDistrict.getName();
				String addressLine4 = locationDistrict.getProvinceName();
				String addressLine5 = locationDistrict.getCountryName();
				String uniqueType = meansOfIdentificationType;
				String uniqueValue = meansOfIdentificationNumber;
				String dateOfBirth1 = null;
				String sex = gender.toUpperCase().substring(0, 1);
				String accType = "WA";
				String idFront = null;
				String idBack = null;
				String custImg = null;
				String custSig = null;
				
				Customer customer = null;
				if(customerId!=null)
				{
					System.out.println("customerId ==" + customerId);
					Integer customerIdI = (Integer) UtilityHelper.decryptData(customerId, bankKey);
					hql = "Select tp from Customer tp where tp.id = " + customerIdI;
					System.out.println("hql ==" + hql);
					customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
					customer.setAddressLine1(addressLine1);
					customer.setAddressLine2(addressLine2);
					
					customer.setAltContactMobile(altContactMobile);
					customer.setContactMobile(contactMobile);
					customer.setDateOfBirth(dob);
					customer.setAltContactEmail(altContactEmail);
					customer.setFirstName(firstName);
					customer.setGender(gender==null ? null : Gender.valueOf(gender));
					customer.setLastName(lastName);
					customer.setOtherName(otherName);
					customer.setVerificationNumber(RandomStringUtils.randomNumeric(10));
					customer.setLocationDistrict(locationDistrict);
					customer.setStatus(CustomerStatus.ACTIVE);
					customer.setCreated_at(new Date());
					if(customerType!=null && customerType.length()>0)
						customer.setCustomerType(CustomerType.valueOf(customerType));
					else
						customer.setCustomerType(CustomerType.INDIVIDUAL);
					
					if(customerImage!=null)
						customer.setCustomerImage(customerImage);
					
					this.swpService.updateRecord(customer);
					System.out.println("customer updated");

					jsonObject.put("customerName", customer.getFirstName()+" "+customer.getLastName());
					jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_UPDATE_SUCCESSFUL);
					jsonObject.put("message", "Customer Account Updated Successfully");
					jsonObject.put("mobileContact", contactMobile);
				}
				else
				{
					JSONObject zicbWalletRespJSON = walletServices.createZicbWallet(httpHeaders, requestContext, token, merchantCode, deviceCode, firstName, lastName, addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, 
						uniqueType, uniqueValue, dateOfBirth1, contactEmail, sex, contactMobile, accType, currencyCode, idFront, idBack, custImg, custSig, bank, 
						eWalletAccountCreateTrue, mobileMoneyCreateTrue, parentCustomerId, parentAccountId);
					System.out.println("zicbWalletResp..." + zicbWalletRespJSON.toString());
					if(zicbWalletRespJSON!=null)
					{
						if(zicbWalletRespJSON.has("status") && zicbWalletRespJSON.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
						{	
							String customerNumber = zicbWalletRespJSON.getString("customerNumber");
							Long customerIdL = zicbWalletRespJSON.getLong("customerId");
							Long accountIdL = zicbWalletRespJSON.getLong("accountId");
							String walletCode = zicbWalletRespJSON.getString("walletNumber");
							Long transactionId = zicbWalletRespJSON.getLong("transactionId");
							
							
							hql = "Select tp from Account tp where tp.id = " + accountIdL;
							Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
							hql = "Select tp from Transaction tp where tp.id = " + transactionId;
							Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
							hql = "Select tp from Customer tp where tp.id = " + customerIdL;
							customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
							
							if(CardType.valueOf(cardType).equals(CardType.EAGLE_CARD))
							{
								String accountNo = UtilityHelper.generateAccountNo(cardScheme, account);
								String pan = UtilityHelper.generatePan(countryCode, bank.getBankCode(), branch_code,  accountNo);
								int cvi = new Random().nextInt(999);
								String cvv = cvi<10 ? ("00" + cvi) : ((cvi>9 && cvi<100) ? ("0" + cvi) : (cvi + ""));
								
								Calendar expiryDate = Calendar.getInstance();
								expiryDate.add(Calendar.YEAR, 1);
								Date expDate = expiryDate.getTime();
								
								ECard ecard = new ECard();
								ecard.setAccount(account);
								ecard.setAcquirer(acquirer);
								ecard.setCardScheme(cardScheme);
								ecard.setCardType(CardType.valueOf(cardType));
								ecard.setCreated_at(new Date());
								ecard.setCustomerId(customer.getId());
								ecard.setAccountId(account.getId());
								ecard.setCvv(cvv);
								ecard.setExpiryDate(expDate);
								ecard.setIssuer(issuer);
								ecard.setNameOnCard(nameOnCard);
								ecard.setPan(pan);
								ecard.setPin(pin+"");
								ecard.setSerialNo((RandomStringUtils.randomNumeric(16)));
								ecard.setStatus(CardStatus.ACTIVE);
								
								if(parentAccountId!=null)
									ecard.setCorporateCustomerAccountId(parentAccountId);
								if(parentCustomerId!=null)
									ecard.setCorporateCustomerId(parentCustomerId);
								
								ecard = (ECard)this.swpService.createNewRecord(ecard);
								System.out.println("ecard Id ==" + ecard.getId());
								JSONObject pinJSON = new JSONObject();
								pinJSON.put("pin", pin);
								pinJSON.put("cvv", cvv);
								pinJSON.put("pan", ecard.getPan().substring(0,  4) + "****" + ecard.getPan().substring(ecard.getPan().length() - 4));
								SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
								pinJSON.put("expire", sdf_.format(expDate));
								


								MobileAccount mobileAccount = null;
								if(mobileMoneyCreateTrue.equals(Boolean.TRUE))
								{
									System.out.println("create mobileMoneyAccount");
									
									mobileAccount = new MobileAccount();
									mobileAccount.setCreated_at(new Date());
									mobileAccount.setCustomerId(customer.getId());
									mobileAccount.setDateActivated(null);
									mobileAccount.setEcard(ecard);
									mobileAccount.setMobileNumber(contactMobile);
									mobileAccount.setStatus(MobileAccountStatus.ACTIVATED);
									mobileAccount.setPin(mpin+"");
									mobileAccount.setAccountId(account.getId());
									JSONObject mobAcctJSON = new JSONObject();
									mobAcctJSON.put("mpin", mpin);
									mobAcctJSON.put("mobileContact", contactMobile);
									
									
									//String encryptedmobdet = UtilityHelper.encryptData(mobAcctJSON.toString(), bankKey);
									//System.out.println("encryptedmobdet ===>" + encryptedmobdet);
									jsonObject.put("mobacctdetail", mobAcctJSON.toString());
									mobileAccount = (MobileAccount)this.swpService.createNewRecord(mobileAccount);
									ecard.setMobilemoneyaccount(true);
									this.swpService.updateRecord(ecard);
									account.setMobilemoneyaccount(true);
									this.swpService.updateRecord(account);
									System.out.println("finishMobileMoney creation");
								}
								
								jsonObject.put("ecarddetail", pinJSON.toString());
								System.out.println("jsonObject ==" + jsonObject.toString());
								
								jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
								jsonObject.put("message", "New Customer Account Added Successfully");
							}
							else if(CardType.valueOf(cardType).equals(CardType.TUTUKA_PHYSICAL_CARD))
							{
								
								TutukaServicesV2 tutukaServices = new TutukaServicesV2();
								hql = "Select tp from ECardBin tp where tp.status = " + CardStatus.NOT_ISSUED.ordinal() + " AND tp.deleted_at IS NOT NULL";
								Collection<ECardBin> ecardBins = (Collection<ECardBin>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
								ECardBin ecardBin = ecardBins.iterator().next();
								JSONObject cardData = new JSONObject();
								cardData.put("cardBinId", ecardBin.getId());
								cardData.put("accountId", account.getId());
								cardData.put("poolAccountId", account.getDefaultPoolAccount().getId());
								cardData.put("currencyCodeId", currencyCode);
								cardData.put("cardSchemeId", cardScheme.getId());
								String encryptedData = (String) UtilityHelper.encryptData(cardData.toString(), bankKey);
								JSONObject linkResp = tutukaServices.linkCustomerToTutukaCompanionPhysicalCard(httpHeaders, requestContext, encryptedData, token);
								if(linkResp!=null && linkResp.getInt("status")==ERROR.GENERAL_OK)
								{
									ECard cardReturned = (ECard)linkResp.get("card");
									JSONObject pinJSON = new JSONObject();
									pinJSON.put("pin", cardReturned.getPin());
									pinJSON.put("cvv", cardReturned.getCvv());
									pinJSON.put("pan", cardReturned.getPan().substring(0,  4) + "****" + cardReturned.getPan().substring(cardReturned.getPan().length() - 4));
									SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
									pinJSON.put("expire", sdf_.format(cardReturned.getExpiryDate()));


									MobileAccount mobileAccount = null;
									if(mobileMoneyCreateTrue.equals(Boolean.TRUE))
									{
										System.out.println("create mobileMoneyAccount");
										
										mobileAccount = new MobileAccount();
										mobileAccount.setCreated_at(new Date());
										mobileAccount.setCustomerId(customer.getId());
										mobileAccount.setDateActivated(null);
										mobileAccount.setEcard(cardReturned);
										mobileAccount.setMobileNumber(contactMobile);
										mobileAccount.setStatus(MobileAccountStatus.ACTIVATED);
										mobileAccount.setPin(mpin+"");
										mobileAccount.setAccountId(account.getId());
										JSONObject mobAcctJSON = new JSONObject();
										mobAcctJSON.put("mpin", mpin);
										mobAcctJSON.put("mobileContact", contactMobile);
										
										
										//String encryptedmobdet = UtilityHelper.encryptData(mobAcctJSON.toString(), bankKey);
										//System.out.println("encryptedmobdet ===>" + encryptedmobdet);
										jsonObject.put("mobacctdetail", mobAcctJSON.toString());
										mobileAccount = (MobileAccount)this.swpService.createNewRecord(mobileAccount);
										cardReturned.setMobilemoneyaccount(true);
										this.swpService.updateRecord(cardReturned);
										account.setMobilemoneyaccount(true);
										this.swpService.updateRecord(account);
										System.out.println("finishMobileMoney creation");
									}
									
									jsonObject.put("ecarddetail", pinJSON.toString());
									System.out.println("jsonObject ==" + jsonObject.toString());
									jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
									jsonObject.put("message", "New Customer Account Added Successfully");
								}
								else
								{
									swpService.deleteRecord(transaction);
									swpService.deleteRecord(account);
									swpService.deleteRecord(customer);
									jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
									jsonObject.put("message", "New Customer Account Creation Failed. Issuing a card to the customer failed. " + (linkResp.has("message") ? ("Reason - " + linkResp.getString("message")) : ""));
								}
							}
							else if(CardType.valueOf(cardType).equals(CardType.TUTUKA_VIRTUAL_CARD))
							{
								TutukaServicesV2 tutukaServices = new TutukaServicesV2();
								JSONObject cardData = new JSONObject();
								cardData.put("customerId", customer.getId());
								cardData.put("acquirerId", acquirer.getId());
								cardData.put("cardSchemeId", cardScheme.getId());
								cardData.put("currencyCodeId", currencyCode);
								cardData.put("poolAccountId", account.getDefaultPoolAccount().getId());
								cardData.put("accountId", account.getId());
								if(account.getCorporateCustomerId()!=null)
									cardData.put("corporateCustomerId", account.getCorporateCustomerId());
								if(account.getCorporateCustomerAccountId()!=null)
									cardData.put("corporateCustomerAccountId", account.getCorporateCustomerAccountId());
								
								
								String encryptedData = (String) UtilityHelper.encryptData(cardData.toString(), bankKey);
								JSONObject linkResp = tutukaServices.createLinkedVirtualCard(httpHeaders, requestContext, encryptedData, token);
								if(linkResp!=null && linkResp.getInt("status")==ERROR.GENERAL_OK)
								{
									ECard cardReturned = (ECard)linkResp.get("card");
									JSONObject pinJSON = new JSONObject();
									pinJSON.put("cvv", cardReturned.getCvv());
									pinJSON.put("pan", cardReturned.getPan().substring(0,  4) + "****" + cardReturned.getPan().substring(cardReturned.getPan().length() - 4));
									SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
									pinJSON.put("expire", sdf_.format(cardReturned.getExpiryDate()));

									
									MobileAccount mobileAccount = null;
									if(mobileMoneyCreateTrue.equals(Boolean.TRUE))
									{
										System.out.println("create mobileMoneyAccount");

										mobileAccount = new MobileAccount();
										mobileAccount.setCreated_at(new Date());
										mobileAccount.setCustomerId(customer.getId());
										mobileAccount.setDateActivated(null);
										mobileAccount.setEcard(cardReturned);
										mobileAccount.setMobileNumber(contactMobile);
										mobileAccount.setStatus(MobileAccountStatus.ACTIVATED);
										mobileAccount.setPin(mpin+"");
										mobileAccount.setAccountId(account.getId());
										JSONObject mobAcctJSON = new JSONObject();
										mobAcctJSON.put("mpin", mpin);
										mobAcctJSON.put("mobileContact", contactMobile);
										
										
										//String encryptedmobdet = UtilityHelper.encryptData(mobAcctJSON.toString(), bankKey);
										//System.out.println("encryptedmobdet ===>" + encryptedmobdet);
										jsonObject.put("mobacctdetail", mobAcctJSON.toString());
										mobileAccount = (MobileAccount)this.swpService.createNewRecord(mobileAccount);
										cardReturned.setMobilemoneyaccount(true);
										this.swpService.updateRecord(cardReturned);
										account.setMobilemoneyaccount(true);
										this.swpService.updateRecord(account);
										System.out.println("finishMobileMoney creation");
									}
									
									
									jsonObject.put("ecarddetail", pinJSON.toString());
									System.out.println("jsonObject ==" + jsonObject.toString());
									jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
									jsonObject.put("message", "New Customer Account Added Successfully");
								}
								else
								{
									swpService.deleteRecord(transaction);
									swpService.deleteRecord(account);
									swpService.deleteRecord(customer);
									jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
									jsonObject.put("message", "New Customer Account Creation Failed. Issuing a card to the customer failed. " + (linkResp.has("message") ? ("Reason - " + linkResp.getString("message")) : ""));
								}
							}
						}
					}
				}
				
				
				
			}
			else
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New Customer Account Creation Failed. Please ensure you specify a card scheme.");
			}
			System.out.println("Create New customer = " + jsonObject.toString());
			return jsonObject;
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("message", "New customer creation Failed. Value for Date of birth provided invalid. Date should be in format YYYY-MM-DD");
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_DOB_FAILED);
				log.warn(e);
				System.out.println("Create New customer Failed = " + jsonObject.toString());
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.debug(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.debug(e);
				System.out.println("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}
	
	
	
	
	@POST
	@Path("/createNewBulkCustomerAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createNewBulkCustomerAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("bulkCustomerData") String bulkCustomerData, 
			@FormParam("token") String token, 
			@FormParam("customerType") String customerType, 
			@FormParam("parentCustomerId") Long parentCustomerId, 
			@FormParam("parentAccountId") Long parentAccountId, 
			@FormParam("cardSchemeId") Integer cardSchemeId, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("countryCode") String countryCode)
	{
		//String acquirerId = "PROBASE";
		JSONObject jsonObject = new JSONObject();
		
		String customerId;
		String addressLine1;
		String addressLine2;
		String altContactEmail; 
		String altContactMobile;
		String contactEmail;
		String contactMobile;
		String dateOfBirth;
		String firstName;
		String gender;
		String lastName;
		String otherName;
		String nameOnCard;
		String accountType;
		String cardType;
		Double openingAccountAmount;
		Boolean eWalletAccountCreateTrue;
		Boolean mobileMoneyCreateTrue; 
		String meansOfIdentificationType; 
		String meansOfIdentificationNumber;
		Long locationDistrict_id;
		String acquirerId;
		
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			
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

			
			boolean successIndicator = false;
			try{
				JSONArray jsonArray = new JSONArray(bulkCustomerData);
				System.out.println("jsonArray length = " + jsonArray.length());
				JSONArray jsonReturnInfo = new JSONArray();
				
				for(int c=0; c<jsonArray.length(); c++)
				{
					System.out.println("jsonArray.get(c) = " + jsonArray.get(c));
					//JSONObject jsonObject_1 = new JSONObject(jsonArray.get(c));
					JSONObject jsonObject_1 = jsonArray.getJSONObject(c);
					System.out.println("jsonObject_1 str = " + jsonObject_1.toString());
					addressLine1 = jsonObject_1.getString("addressLine1");
					addressLine2 = jsonObject_1.has("addressLine2") ? jsonObject_1.getString("addressLine2") : null;
					altContactEmail = jsonObject_1.has("altContactEmail") ? jsonObject_1.getString("altContactEmail") : null;
					altContactMobile = jsonObject_1.has("altContactMobile") ? jsonObject_1.getString("altContactMobile") : null;
					contactEmail = jsonObject_1.getString("contactEmail");
					contactMobile = jsonObject_1.getString("contactMobile");
					dateOfBirth = jsonObject_1.has("dateOfBirth") ? jsonObject_1.getString("dateOfBirth") : null;
					firstName = jsonObject_1.getString("firstName");
					gender = jsonObject_1.getString("gender");
					lastName = jsonObject_1.getString("lastName");
					otherName = jsonObject_1.has("otherName") ? jsonObject_1.getString("otherName") :null;
					nameOnCard = jsonObject_1.getString("nameOnCard");
					countryCode = jsonObject_1.getString("countryCode");
					currencyCode = jsonObject_1.getString("currencyCode");
					accountType = jsonObject_1.getString("accountType");
					cardType = jsonObject_1.getString("cardType");
					locationDistrict_id = jsonObject_1.getLong("locationDistrict_id");
					openingAccountAmount = Double.valueOf(jsonObject_1.getString("openingAccountAmount"));
					eWalletAccountCreateTrue = jsonObject_1.getString("eWalletAccountCreateTrue").equalsIgnoreCase("true") ? true : false;
					mobileMoneyCreateTrue = jsonObject_1.getString("mobileMoneyCreateTrue").equalsIgnoreCase("true") ? true : false; 
					meansOfIdentificationType = jsonObject_1.getString("meansOfIdentificationType");
					meansOfIdentificationNumber = jsonObject_1.getString("meansOfIdentificationNumber");
					acquirerId = jsonObject_1.getString("acquirerId");
					
					JSONObject js = this.createNewCustomerAccount(httpHeaders, requestContext, null, addressLine1, addressLine2, altContactEmail, 
							altContactMobile, contactEmail, contactMobile, dateOfBirth, null,
							firstName, gender, lastName, otherName,  null, 
							 cardSchemeId, nameOnCard, countryCode, currencyCode, accountType,
							 openingAccountAmount,  eWalletAccountCreateTrue,  mobileMoneyCreateTrue, token, 
							customerType, parentCustomerId, parentAccountId, cardType, meansOfIdentificationType, meansOfIdentificationNumber, 
							acquirerId);
					if(js.has("status") && (js.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS || js.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT))
					{
						successIndicator = true;
						
						String accountNo = js.has("accountNo") ? js.getString("accountNo") : null;
						String useracctid = js.has("useracctid") ? js.getString("useracctid") : null;
						String mobacctdetail = js.has("mobacctdetail") ? js.getString("mobacctdetail") : null;
						String mpin =null;
						String mobileContact = null;
						String pin = null;
						String pan = null;
						JSONObject j_s = null;
								
						
						if(mobacctdetail!=null)
						{
							j_s = new JSONObject(mobacctdetail);
							mpin = j_s.has("mpin") ? Integer.toString(j_s.getInt("mpin")) : null;
							mobileContact = j_s.has("mobileContact") ? j_s.getString("mobileContact") : null;
						}
						String ecarddetail = js.has("ecarddetail") ? js.getString("ecarddetail") : null;
						if(ecarddetail!=null)
						{
							j_s = new JSONObject(ecarddetail);
							pin = j_s.has("pin") ? Integer.toString(j_s.getInt("pin")) : null;
							pan = j_s.has("pan") ? j_s.getString("pan") : null;
						}
						
						j_s = new JSONObject();
						if(accountNo!=null)
							j_s.put("accountNo", accountNo);
						if(useracctid!=null)
							j_s.put("useracctid", useracctid);
						if(mpin!=null)
							j_s.put("mpin", mpin);
						if(mobileContact!=null)
							j_s.put("mobileContact", mobileContact);
						if(pin!=null)
							j_s.put("pin", pin);
						if(pan!=null)
							j_s.put("pan", pan);
						
						
						jsonReturnInfo.put(j_s);
						jsonObject.put("jsonReturnInfo", jsonReturnInfo);
					}
					
					
					
					
					
				}
				
				Collection<Account> corporateCustomerSubAccountList = null;
				Account parentAccount = null;
				Customer parentCustomer = null;
				
				if(parentCustomerId!=null)
				{
					String hql = "Select tp from Customer tp where tp.id = " + parentCustomerId;
					parentCustomer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Account tp where tp.id = " + parentAccountId;
					parentAccount = (Account)this.swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Account tp where tp.corporateCustomerAccountId = " + parentAccountId;
					corporateCustomerSubAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
					jsonObject.put("parentCustomer", new Gson().toJson(parentCustomer));
					jsonObject.put("parentAccount", new Gson().toJson(parentAccount));
					jsonObject.put("corporateCustomerSubAccountList", new Gson().toJson(corporateCustomerSubAccountList));
				}
				
				
				if(successIndicator==true)
				{
					jsonObject.put("status", ERROR.BATCH_CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Accounts Created Successfully. Review below the list of customer accounts under the Corporate Customer Account.");
				}
				else
				{
					jsonObject.put("status", ERROR.BATCH_CUSTOMER_CREATE_FAIL);
					jsonObject.put("message", "New Customer Accounts Creation Failed.");
				}
				return jsonObject;
	
			}catch(JSONException e)
			{
				e.printStackTrace();
				jsonObject.put("status", ERROR.BATCH_CUSTOMER_CREATE_FAIL);
				jsonObject.put("message", "New Customer Accounts Creation Failed.");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			try
			{
				jsonObject.put("status", ERROR.BATCH_CUSTOMER_CREATE_FAIL);
				jsonObject.put("message", "New Customer Accounts Creation Failed.");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
			
		
		
		
		
		
	}
	
	
	
	
	
	@GET
	@Path("/listCustomers")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listCustomers(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("status") String status, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Collection<Customer> customerList = null;
			String hql = "Select tp from Customer tp";
			System.out.println(hql);
			
			
			
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
				//Applicable to bankCodes that are PROBASEPAY
				if(status!=null)
				{
					hql = hql + " where tp.status = "+(CustomerStatus.valueOf(status).ordinal());
				}
				
				//Applicable to bankCodes that are not PROBASEPAY
				System.out.println(hql);
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			customerList = (Collection<Customer>)swpService.getAllRecordsByHQL(hql);
			Iterator<Customer> customerIter = customerList.iterator();
			JSONArray customerArray = new JSONArray();
			while(customerIter.hasNext())
			{
				Customer cust = customerIter.next();
				JSONObject oneCustomer = new JSONObject();
				oneCustomer.put("id", cust.getId());
				oneCustomer.put("firstName", cust.getFirstName()==null ? "" : cust.getFirstName());
				oneCustomer.put("lastName", cust.getLastName()==null ? "" : cust.getLastName());
				oneCustomer.put("otherName", cust.getOtherName()==null ? "" : cust.getOtherName());
				oneCustomer.put("verificationNumber", cust.getVerificationNumber()==null ? "" : cust.getVerificationNumber());
				oneCustomer.put("contactMobile", cust.getContactMobile()==null ? "" : cust.getContactMobile());
				oneCustomer.put("contactEmail", cust.getContactEmail()==null ? "" : cust.getContactEmail());
				oneCustomer.put("customerType", cust.getCustomerType()==null ? "" : cust.getCustomerType().name());
				oneCustomer.put("status", cust.getStatus()==null ? "" : cust.getStatus().name());
				customerArray.put(oneCustomer);
			}
			System.out.println(customerArray.toString());	
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer list fetched successfully");
			jsonObject.put("customerlist", customerArray);
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Customer list fetched Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}


	@GET
	@Path("/getCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") Long customerId, 
			@QueryParam("token") String token)
	{
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();
		try {
			this.swpService = this.serviceLocator.getSwpService();
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
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
			Long customerIdI = customerId;
			
			String hql = "Select tp from Customer tp where tp.id = " + customerIdI;
			customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			
			JSONObject oneCustomer = new JSONObject();
			System.out.println("oneCustomer ..." + customer.getId());
			oneCustomer.put("id", customer.getId());
			oneCustomer.put("firstName", UtilityHelper.getValue(customer.getFirstName()));
			oneCustomer.put("lastName", UtilityHelper.getValue(customer.getLastName()));
			oneCustomer.put("otherName", UtilityHelper.getValue(customer.getOtherName()));
			oneCustomer.put("gender", UtilityHelper.getValue(customer.getGender()==null ? null : customer.getGender().name()));
			oneCustomer.put("addressLine1", UtilityHelper.getValue(customer.getAddressLine1()));
			oneCustomer.put("addressLine2", UtilityHelper.getValue(customer.getAddressLine2()));
			oneCustomer.put("locationDistrict", UtilityHelper.getValue(customer.getLocationDistrict()==null ? "" : customer.getLocationDistrict().getName()));
			oneCustomer.put("locationDistrictId", customer.getLocationDistrict()==null ? null : customer.getLocationDistrict().getId());
			oneCustomer.put("locationProvince", customer.getLocationDistrict()==null ? "" : customer.getLocationDistrict().getProvinceName());
			oneCustomer.put("locationProvinceId", customer.getLocationDistrict()==null ? null : customer.getLocationDistrict().getProvinceId());
			oneCustomer.put("dateOfBirth", UtilityHelper.getDateOfBirth(customer.getDateOfBirth()));
			oneCustomer.put("contactMobile", UtilityHelper.getValue(customer.getContactMobile()));
			oneCustomer.put("altContactMobile", UtilityHelper.getValue(customer.getAltContactMobile()));
			oneCustomer.put("contactEmail", UtilityHelper.getValue(customer.getContactEmail()));
			oneCustomer.put("altContactEmail", UtilityHelper.getValue(customer.getAltContactEmail()));
			oneCustomer.put("verificationNumber", UtilityHelper.getValue(customer.getVerificationNumber()));
			oneCustomer.put("status", UtilityHelper.getValue(customer.getStatus().name()));
			oneCustomer.put("customerType", UtilityHelper.getValue(customer.getCustomerType()==null ? null : customer.getCustomerType().name()));
			oneCustomer.put("customerImage", UtilityHelper.getValue(customer.getCustomerImage()));
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "New customer creation Failed");
			jsonObject.put("customer", oneCustomer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			e.printStackTrace();
			System.out.println("error id..." + customer.getId());
		}
		return jsonObject;
		
		
	}

	
	@POST
	@Path("/addAccountToCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject addAccountToCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerVerificationNo") String customerVerificationNo, 
			@FormParam("cardSchemeId") String cardSchemeId, 
			@FormParam("nameOnCard") String nameOnCard, 
			@FormParam("countryCode") String countryCode, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("eWalletAccountCreateTrue") Boolean eWalletAccountCreateTrue, 
			@FormParam("mobileMoneyCreateTrue") Boolean mobileMoneyCreateTrue, 
			@FormParam("token") String token)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		Customer customer = null;
		String acquirerDef = "PROBASE";
		String mpin = (RandomStringUtils.randomNumeric(4));
		String pin = (RandomStringUtils.randomNumeric(4));
		
		JSONObject jsonObject = new JSONObject();
		System.out.println("Create New Customer");
		try {
			this.swpService = this.serviceLocator.getSwpService();
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
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
			Integer cardSchemeIdI = Integer.valueOf(cardSchemeId);
			

			String hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeIdI;
			System.out.println("hql ---> " + hql);
			CardScheme cardScheme = (CardScheme)this.swpService.getUniqueRecordByHQL(hql);
			System.out.println("cardScheme ---> " + cardScheme);
			hql = "Select tp from Bank tp where tp.bankCode = '" + staff_bank_code + "'";
			Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where lower(tp.acquirerName) = '" + acquirerDef.toLowerCase() + "'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Bank tp where lower(tp.bankCode) = '" + issuerBankCode.toLowerCase() + "'";
			Bank issuer = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNo + "'";
			customer  = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from PoolAccount tp where tp.status = '" + AccountStatus.ACTIVE + 
					"' ORDER BY rand()";
			Collection<PoolAccount> poolAccountList = (Collection<PoolAccount>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
			Iterator<PoolAccount> it = poolAccountList.iterator();
			PoolAccount poolAccount = (PoolAccount)it.next();
			
			hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
			Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			
			Account account = new Account();
			account.setCreated_at(new Date());
			account.setCustomer(customer);
			account.setStatus(AccountStatus.ACTIVE);
			account.setBank(bank);
			account.setBranchCode(branch_code);
			account.setCurrencyCode(currencyCode);
			account.setAccountCount(customerAccountList.size());
			account.setAccountType(AccountType.valueOf(accountType));
			account.setDefaultPoolAccount(poolAccount);
			
			String identifier1 = "0" + (AccountType.valueOf(accountType).ordinal() + 1) + "" + 
					RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
			account.setAccountIdentifier(identifier1);
			
			
			account = (Account)this.swpService.createNewRecord(account);
			jsonObject.put("accountNo", identifier1);
			jsonObject.put("customerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
			
			
			
			Transaction transaction = new Transaction();

			transaction.setChannel(Channel.OTC);
			transaction.setCreated_at(new Date());
			transaction.setAmount(openingAccountAmount);
			transaction.setCustomerId(customer.getId());
			transaction.setFixedCharge(null);
			transaction.setMessageRequest(null);
			transaction.setServiceType(ServiceType.DEPOSIT_OTC);
			transaction.setStatus(TransactionStatus.SUCCESS);
			transaction.setPayerEmail(customer.getContactEmail());
			transaction.setPayerMobile(customer.getContactMobile());
			transaction.setPayerName(customer.getLastName() + ", " + customer.getFirstName() + " " + customer.getOtherName());
			transaction.setResponseCode(TransactionCode.transactionSuccess);
			transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
			transaction.setTransactionDate(new Date());
			transaction.setTransactionCode(TransactionCode.transactionSuccess);
			transaction.setAccount(account);
			transaction.setCreditAccountTrue(true);
			transaction.setPoolAccount(poolAccount);
			transaction.setCreditPoolAccountTrue(true);
			transaction.setTransactingBankId(account.getBank().getId());
			

			transaction.setCustomerUserId(account.getCustomer().getUser().getId());
			transaction.setTransactingBankId(account.getBank().getId());
			transaction.setReceipientChannel(Channel.OTC);
			transaction.setTransactionDetail("Account Opening: Deposit " + openingAccountAmount + " into Account #" + account.getAccountIdentifier());
			transaction.setReceipientEntityId(account.getId());

			hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
					"tp.account.id = " + account.getId() + " ORDER BY tp.updated_at DESC";
			Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
			Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
			transaction.setClosingBalance((lastTransaction!=null ? lastTransaction.getTotalCreditSum() : 0.0) + openingAccountAmount);
			transaction.setTotalCreditSum((lastTransaction!=null ? lastTransaction.getTotalCreditSum() : 0.0) + openingAccountAmount);
			transaction.setTotalCreditSum((lastTransaction!=null ? lastTransaction.getTotalDebitSum() : 0.0) + openingAccountAmount);
			transaction.setUpdated_at(new Date());
			transaction.setPaidInByBankUserAccountId(tokenUser.getId());
			
			this.swpService.createNewRecord(transaction);
			jsonObject.put("amountDeposited", openingAccountAmount);
			
			String accountNo = UtilityHelper.generateAccountNo(cardScheme, account);
			String pan = UtilityHelper.generatePan(countryCode, bank.getBankCode(), branch_code,  accountNo);
			int cvi = new Random().nextInt(999);
			String cvv = cvi<10 ? ("00" + cvi) : ((cvi>9 && cvi<100) ? ("0" + cvi) : (cvi + ""));
			
			Calendar expiryDate = Calendar.getInstance();
			expiryDate.add(Calendar.YEAR, 1);
			Date expDate = expiryDate.getTime();
			ECard ecard = new ECard();
			ecard.setAccount(account);
			ecard.setAcquirer(acquirer);
			ecard.setCardScheme(cardScheme);
			ecard.setCardType(CardType.EAGLE_CARD);
			ecard.setCreated_at(new Date());
			ecard.setCustomerId(customer.getId());
			ecard.setCvv(cvv);
			ecard.setExpiryDate(expDate);
			ecard.setIssuer(issuer);
			ecard.setNameOnCard(nameOnCard);
			ecard.setPan(pan);
			ecard.setPin(pin+"");
			ecard.setSerialNo((RandomStringUtils.randomNumeric(16)));
			ecard.setStatus(CardStatus.ACTIVE);
			ecard.setAccountId(account.getId());
			ecard = (ECard)this.swpService.createNewRecord(ecard);
			
			JSONObject pinJSON = new JSONObject();
			pinJSON.put("pin", pin);
			pinJSON.put("cvv", cvv);
			pinJSON.put("expire", expDate);
			
			//String encryptedcarddet = UtilityHelper.encryptData(pinJSON.toString(), bankKey);
			//System.out.println("encryptedcarddet ===>" + encryptedcarddet);
			jsonObject.put("ecarddetail", pinJSON.toString());
			
			account.setAccountIdentifier(accountNo);
			this.swpService.updateRecord(account);
			
			MobileAccount mobileAccount = null;
			if(mobileMoneyCreateTrue.equals(Boolean.TRUE))
			{
				
				mobileAccount = new MobileAccount();
				mobileAccount.setCreated_at(new Date());
				mobileAccount.setCustomerId(customer.getId());
				mobileAccount.setDateActivated(null);
				mobileAccount.setEcard(ecard);
				mobileAccount.setMobileNumber(customer.getContactMobile());
				mobileAccount.setStatus(MobileAccountStatus.INACTIVE);
				mobileAccount.setAccountId(account.getId());
				mobileAccount.setPin(mpin);
				
				JSONObject mobAcctJSON = new JSONObject();
				mobAcctJSON.put("mpin", mpin);
				mobAcctJSON.put("mobileContact", mobileAccount.getEcard().getAccount().getCustomer().getContactMobile());
				//String encryptedmobdet = UtilityHelper.encryptData(mobAcctJSON.toString(), bankKey);
				//System.out.println("encryptedmobdet ===>" + encryptedmobdet);
				jsonObject.put("mobacctdetail", mobAcctJSON.toString());
				mobileAccount = (MobileAccount)this.swpService.createNewRecord(mobileAccount);
				ecard.setMobilemoneyaccount(true);
				this.swpService.updateRecord(ecard);
				account.setMobilemoneyaccount(true);
				this.swpService.updateRecord(account);
			}
			
			
			if(eWalletAccountCreateTrue.equals(Boolean.TRUE))
			{
				String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
				String mobileActivationCode = RandomStringUtils.randomAlphanumeric(6);
				
				hql = "Select tp from User tp where lower(tp.username) = '" + customer.getContactEmail().toLowerCase() + "'";
				User user = (User)this.swpService.getUniqueRecordByHQL(hql);
				if(user==null)
				{
					user = new User();
					user.setWebActivationCode(webActivationCode);
					user.setMobileActivationCode(mobileActivationCode);
					user.setFailedLoginCount(0);
					user.setUsername(accountNo);
					user.setUserEmail(customer.getContactEmail());
					user.setLockOut(Boolean.FALSE);
					user.setCreated_at(new Date());
					user.setStatus(UserStatus.INACTIVE);
					user.setRoleType(RoleType.CUSTOMER);
					user.setFirstName(customer.getFirstName());
					user.setLastName(customer.getLastName());
					user.setOtherName(customer.getOtherName());
					user.setMobileNo(customer.getContactMobile());
					String pswd = RandomStringUtils.randomAlphanumeric(8);
					user.setPassword(pswd);
					//String encryptedpswd = UtilityHelper.encryptData(pswd, bankKey);
					//System.out.println("encryptedpswd ===>" + encryptedpswd);
					jsonObject.put("useracctid", pswd);
					user = (User)this.swpService.createNewRecord(user);

					if(mobileAccount!=null)
					{
						mobileAccount.setUserId(user.getId());
						this.swpService.updateRecord(mobileAccount);
					}
					
					account.setUserId(user.getId());
					this.swpService.updateRecord(account);
					
					ecard.setUserId(user.getId());
					this.swpService.updateRecord(ecard);

					if(mobileAccount!=null)
					{
						mobileAccount.setUserId(user.getId());
						this.swpService.updateRecord(mobileAccount);
					}
					

					transaction.setCustomerUserId(user.getId());
					this.swpService.updateRecord(transaction);
					

					ecard.setUserId(user.getId());
					this.swpService.updateRecord(ecard);
					
					customer.setUser(user);
					this.swpService.updateRecord(customer);
					
					
					
					Wallet wallet = null;
					hql = "Select tp from Wallet tp where tp.user.id = " + user.getId();
					wallet = (Wallet)this.swpService.getUniqueRecordByHQL(hql);
					
					if(wallet==null)
					{
						wallet = new Wallet();
						//wallet.setAccount(account);
						wallet.setCreated_at(new Date());
						wallet.setCustomer(customer);
						wallet.setStatus(WalletStatus.INACTIVE);
						wallet.setWalletUniqueId(account.getAccountIdentifier());
						String walletCode = RandomStringUtils.randomNumeric(4);
						wallet.setWalletCode(walletCode);
						wallet.setUser(user);
						wallet.setUserId(user.getId());
						//String encryptedwalletdet = UtilityHelper.encryptData(walletCode, bankKey);
						//System.out.println("encryptedwalletdet ===>" + encryptedwalletdet);
						jsonObject.put("walletcodedetail", walletCode);
						this.swpService.createNewRecord(wallet);
						account.setEwalletaccount(true);
						this.swpService.updateRecord(account);
					}

					
					WalletAccount walletAccount = new WalletAccount();
					walletAccount.setWallet(wallet);
					walletAccount.setAccount(account);
					walletAccount.setCreated_at(new Date());
					walletAccount.setCustomerId(customer.getId());
					walletAccount.setUserId(user.getId());
					this.swpService.createNewRecord(walletAccount);
					
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Account Created Successfully");
				}
				else
				{
					Wallet wallet = new Wallet();
					//wallet.setAccount(account);
					wallet.setCreated_at(new Date());
					wallet.setCustomer(customer);
					wallet.setStatus(WalletStatus.INACTIVE);
					wallet.setWalletUniqueId(account.getAccountIdentifier());
					String walletCode = RandomStringUtils.randomNumeric(4);
					wallet.setWalletCode(walletCode);
					wallet.setUser(user);
					wallet.setUserId(user.getId());
					//String encryptedwalletdet = UtilityHelper.encryptData(walletCode, bankKey);
					//System.out.println("encryptedwalletdet ===>" + encryptedwalletdet);
					jsonObject.put("walletcodedetail", walletCode);
					this.swpService.createNewRecord(wallet);
					account.setEwalletaccount(true);
					this.swpService.updateRecord(account);
					

					
					WalletAccount walletAccount = new WalletAccount();
					walletAccount.setWallet(wallet);
					walletAccount.setAccount(account);
					walletAccount.setCreated_at(new Date());
					walletAccount.setCustomerId(customer.getId());
					walletAccount.setUserId(user.getId());
					this.swpService.createNewRecord(walletAccount);
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
					jsonObject.put("message", "New Customer Account Created Successfully.");
				}
			}
			
			
			System.out.println("Create New customer account = " + jsonObject.toString());
			return jsonObject;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.warn(e);
				System.out.println("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}
	
	
	

	
	@GET
	@Path("/listCustomerAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listCustomerAccounts(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") String customerId, 
			@QueryParam("status") String status, 
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
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			
			Collection<Account> customerAccountList = null;
			String hql = "Select tp from Account tp";
			int and_ = 0;
			Customer customer = null;
			if(status!=null)
			{
				hql = hql + " where tp.status = '"+(AccountStatus.valueOf(status).ordinal() + 1)+"'";
				and_ = 1;
			}
			if(customerId!=null)
			{
				Integer customerIdI = (Integer) UtilityHelper.decryptData(customerId, bankKey);
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " WHERE";
				}
				hql = hql + " tp.customer.id = " + customerIdI;
				
				String hqlDevice = "Select tp from Customer tp where tp.id = " + customerIdI;
				customer = (Customer)this.swpService.getUniqueRecordByHQL(hqlDevice);
				jsonObject.put("customer", new Gson().toJson(customer));
			}

			customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			Iterator<Account> customerAccountIter = customerAccountList.iterator();
			JSONArray customerAccountArray = new JSONArray();
			while(customerAccountIter.hasNext())
			{
				Account acct = customerAccountIter.next();
				JSONObject oneCustomer = new JSONObject();
				oneCustomer.put("id", acct.getId());
				oneCustomer.put("accountIdentifier", UtilityHelper.getValue(acct.getAccountIdentifier()));
				oneCustomer.put("accountType", UtilityHelper.getValue(acct.getAccountType().name()));
				oneCustomer.put("bankName", UtilityHelper.getValue(acct.getBank().getBankName()));
				oneCustomer.put("ewalletaccount", (acct.getEwalletaccount()));
				oneCustomer.put("customerverficationnumber", (acct.getCustomer().getVerificationNumber()));
				oneCustomer.put("customerFullName", (acct.getCustomer().getLastName() + " " + acct.getCustomer().getFirstName() + (acct.getCustomer().getOtherName()==null ? "" : " " + acct.getCustomer().getOtherName())));
				oneCustomer.put("status", UtilityHelper.getValue(acct.getStatus().name()));
				customerAccountArray.put(oneCustomer);
			}
			
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Accounts list fetched successfully");
			jsonObject.put("customeracctlist", customerAccountArray);
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
		
	}*/

	
}
