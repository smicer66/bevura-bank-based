package com.probase.probasepay.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AcquirerType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.MPQRDataType;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.StopCardReason;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.TutukaCardStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.DeviceBankAccount;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardBin;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.AppDevice;
import com.probase.probasepay.models.MPQRData;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.TutukaHelper;
import com.probase.probasepay.util.UtilityHelper;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;


@Path("/TutukaServicesV2")
public class TutukaServicesV2 {
	private static Logger log = Logger.getLogger(CardServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	@POST
	@Path("/createTutukaWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createTutukaWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, @FormParam("encryptedData") String encryptedData)
	{
		JSONObject jsonObject = new JSONObject();
		String logId = RandomStringUtils.randomAlphanumeric(10);
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			Customer customer = null;
			Acquirer issuer = null;
			Issuer acquirer = null;
			CardScheme cardScheme = null;
			String currencyCode = null;
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
				//return Response.status(200).entity(jsonObject).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			JSONObject accessKeys = app.getAccessKeys();
			String bankKey = accessKeys.getString("PROBASEWALLET");
			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);


			String merchantCode = data.getString("merchantCode");
			String deviceCode = data.getString("deviceCode");
			String firstName = data.getString("firstName");
			String lastName = data.getString("lastName");
			String addressLine1 = data.getString("addressLine1");
			String addressLine2 = data.getString("addressLine2");
			String addressLine3 = data.getString("addressLine3");
			String addressLine4 = data.getString("addressLine4");
			String addressLine5 = data.getString("addressLine5");
			String uniqueType = data.getString("uniqueType");
			String uniqueValue = data.getString("uniqueValue");
			String dateOfBirth = data.getString("dateOfBirth");
			String email = data.getString("email");
			String sex = data.getString("sex");
			String mobileNumber = data.getString("mobileNumber");
			String accType = data.getString("accType");
			String currency = data.getString("currency");
			String idFront = data.has("idFront") ? data.getString("idFront") : null;
			String idBack = data.has("idBack") ? data.getString("idBack") : null;
			String custImg = data.has("custImg") ? data.getString("custImg") : null;
			String custSig = data.has("custSig") ? data.getString("custSig") : null;
			String issuerCode = data.getString("issuerCode");
			Long cardSchemeId = data.getLong("cardSchemeId");
			Long districtId = data.getLong("districtId");
			String accountType = data.getString("accountType");
			Integer isSettlementAccount = data.has("isSettlementAccount") ? data.getInt("isSettlementAccount") : null;
			Integer isTokenize = data.has("isTokenize") ? data.getInt("isTokenize") : null;
			
			Boolean eWalletAccountCreateTrue = true;
			Boolean mobileMoneyCreateTrue = false;
			Long parentCustomerId = null;
			Long parentAccountId = null;
			
			String hql = "Select tp from Bank tp where tp.issuerCode = '"+issuerCode+"'";
			Bank bank = (Bank)swpService.getUniqueRecordByHQL(hql);
			District district = (District)swpService.getRecordById(District.class, districtId);
			
			JSONObject createWalletRespJS = UtilityHelper.createZICBWallet(swpService, logId, token, merchantCode, deviceCode, firstName, 
					lastName, addressLine1, addressLine2, addressLine3, addressLine4, 
					addressLine5, uniqueType, uniqueValue, dateOfBirth, email, 
					sex, mobileNumber, accType, currency, idFront, idBack, 
					custImg, custSig, issuer, parentCustomerId, parentAccountId, null, district, isSettlementAccount, isTokenize);
			
			
			
			if(createWalletRespJS!=null)
			{
				try
				{
					System.out.println("createWalletResp .. " + createWalletRespJS.toString());
					
					if(createWalletRespJS!=null && createWalletRespJS.has("status") && createWalletRespJS.getInt("status")==(ERROR.CUSTOMER_CREATE_SUCCESS))
					{
						String password = createWalletRespJS.has("useracctid") ? createWalletRespJS.getString("useracctid") : null;
						if(password!=null)
						{
							String walletNumber = createWalletRespJS.has("walletNumber") ? createWalletRespJS.getString("walletNumber") : null;
							/*String smsMessage = "Welcome to Probase Wallet. We've setup your wallet - " + walletNumber + ".\nYour login credentials are:\n" +
								"Username: " + mobileNumber + "\nPassword: " + password;
							new Thread(new SmsSender(this.swpService, smsMessage, mobileNumber)).start();*/
						}
						hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+AcquirerType.TUTUKA+"'";
						acquirer = (Issuer)swpService.getUniqueRecordByHQL(hql);
						Long customerId = createWalletRespJS.getLong("customerId");
						Long accountId = createWalletRespJS.getLong("accountId");
						JSONObject cardData = new JSONObject();
						cardData.put("customerId", customerId);
						cardData.put("acquirerId", acquirer.getId());
						cardData.put("cardSchemeId", cardSchemeId);
						cardData.put("currencyCodeId", currency);
						//data.setLong("corporateCustomerId") : null;
						//data.setLong("corporateCustomerAccountId") : null;
						String cardDataStr = cardData.toString();
						String encryptedDataForCard = (String) UtilityHelper.encryptData(cardDataStr, bankKey);
						JSONObject cardResp = this.createLinkedVirtualCard(httpHeaders, requestContext, encryptedDataForCard, token);
						if(cardResp!=null && cardResp.has("status") && cardResp.getInt("status")==ERROR.GENERAL_OK)
						{
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "Your new Probase Wallet and a Virtual Card has been setup for you. Please log in with your credentials.");
							return jsonObject;
						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "Your new Probase Wallet has been setup for you. Please log in with your credentials");
							return jsonObject;
						}
					}
					jsonObject.put("status", createWalletRespJS.has("status") ? createWalletRespJS.getInt("status") : ERROR.GENERAL_FAIL);
					jsonObject.put("message", createWalletRespJS.has("message") ? createWalletRespJS.getInt("message") : "We experienced issues setting up a wallet for you");
					return jsonObject;
				}
				catch(JSONException e)
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete details provided. Please provide the required details to setup your wallet");
					return jsonObject;
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return jsonObject;
		}
		return jsonObject;
	}
	
	
	
	@POST
	@Path("/createLinkedVirtualCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createLinkedVirtualCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			Customer customer = null;
			Account account = null;
			Bank bank = null;
			Acquirer acquirer = null;
			CardScheme cardScheme = null;
			String currencyCode = null;
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
				//return Response.status(200).entity(jsonObject).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String customerVerificationNo = data.has("customerVerificationNo") ? data.getString("customerVerificationNo") : null;
			String accountIdentifier = data.has("accountIdentifier") ? data.getString("accountIdentifier") : null;
			Long acquirerId = data.getLong("acquirerId");
			Long cardSchemeId = data.getLong("cardSchemeId");
			String currencyCodeId = data.getString("currencyCodeId");
			Long corporateCustomerId = data.has("corporateCustomerId") ? data.getLong("corporateCustomerId") : null;
			Long corporateCustomerAccountId = data.has("corporateCustomerAccountId") ? data.getLong("corporateCustomerAccountId") : null;
			Integer isTokenize = data.has("isTokenize") ? data.getInt("isTokenize") : null;
			String tokenizeMerchantCode = data.has("tokenizeMerchantCode") ? data.getString("tokenizeMerchantCode") : null;
			String tokenizeDeviceCode = data.has("tokenizeDeviceCode") ? data.getString("tokenizeDeviceCode") : null;
			log.info("data...." + data.toString());
			
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+ acquirerCode +"'";
			Acquirer acquirer_ = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			if(customerVerificationNo!=null)
			{
				hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNo + "'";
				customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(accountIdentifier!=null)
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "'";
				account = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(account==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
				jsonObject.put("message", "You need a valid wallet created first before you can create a virtual card");
				return jsonObject;
			}
			hql = "Select tp from Bank tp where tp.bankCode = '"+acquirer_.getBank().getBankCode()+"'";
			bank = (Bank)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where tp.id = " + acquirerId;
			acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);

			hql = "Select tp from Issuer tp where tp.id = " + cardScheme.getIssuer().getId();
			Issuer issuer =(Issuer)swpService.getUniqueRecordByHQL(hql);
			
			if(corporateCustomerId!=null && corporateCustomerAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = " + corporateCustomerId;
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				hql = "Select tp from Account tp where tp.id = " + corporateCustomerAccountId;
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(TutukaHelper.modeTest==true && customer.getMeansOfIdentificationType()==null && customer.getMeansOfIdentificationNumber()==null)
			{
				//customer.setMeansOfIdentificationType(MeansOfIdentificationType.NRC);
				//customer.setMeansOfIdentificationNumber("8910-9943-39");
				swpService.updateRecord(customer);
			}
			
			
			JSONObject resp = TutukaHelper.createNewTutukaCompanionVirtualCard(customer, account, acquirer, issuer, cardScheme, null, currencyCodeId, 
					corporateCustomer, corporateCustomerAccount, swpService, isTokenize, tokenizeMerchantCode, tokenizeDeviceCode, account.getIsLive());
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_CREATED_SUCCESSFULLY))
			{
				ECard card = (ECard)resp.get("card");
				if(isTokenize!=null && isTokenize==1)
				{
					
					jsonObject.put("cardBevuraToken", resp.getString("cardBevuraToken"));
				}
				String smsMessage = "Hey, we've created a new virtual card for you. Your card ends with ***" + card.getPan().substring(card.getPan().length() - 4) + 
					". You can load your card and start using your card.";
				//new Thread(new SmsSender(this.swpService, smsMessage, card.getAccount().getCustomer().getContactMobile())).start();
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("card", card.getSummary());
				jsonObject.put("cardId", card.getId());
				jsonObject.put("serialNo", card.getSerialNo());
				jsonObject.put("cardType", card.getCardType().name());
				jsonObject.put("nameOnCard", card.getNameOnCard());
				jsonObject.put("message", "New virtual card created successfully");
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "New virtual card could not be created");
				return jsonObject;
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		return jsonObject;
	}

	
	@POST
	@Path("/orderNewTutukaCompanionPhysicalCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject orderNewTutukaCompanionPhysicalCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String customerVerificationNo = data.has("customerVerificationNo") ? data.getString("customerVerificationNo") : null;
			String accountIdentifier = data.has("accountIdentifier") ? data.getString("accountIdentifier") : null;
			Long acquirerId = data.getLong("acquirerId");
			Long cardSchemeId = data.getLong("cardSchemeId");
			String currencyCodeId = data.getString("currencyCodeId");
			Long poolAccountId = data.getLong("poolAccountId");
			Long corporateCustomerId = data.has("corporateCustomerId") ? data.getLong("corporateCustomerId") : null;
			Long corporateCustomerAccountId = data.has("corporateCustomerAccountId") ? data.getLong("corporateCustomerAccountId") : null;
			
			
			Customer customer = null;
			Account account = null;
			
			String hql = "";
			if(customerVerificationNo!=null)
			{
				hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNo + "'";
				customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(accountIdentifier!=null)
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "'";
				account = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(account==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
				jsonObject.put("message", "You need a valid wallet created first before you can create a virtual card");
				return jsonObject;
			}
			
			hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"'";
			Bank bank_ = (Bank)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where tp.id = " + acquirerId;
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			CardScheme cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
			Issuer issuer = cardScheme.getIssuer();
			
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			if(corporateCustomerId!=null &corporateCustomerAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = " + corporateCustomerId;
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				hql = "Select tp from Account tp where tp.id = " + corporateCustomerAccountId;
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(customer!=null && customer.getStatus().equals(CustomerStatus.ACTIVE))
			{
				
				
				JSONObject resp = TutukaHelper.orderNewTutukaCompanionPhysicalCard
					(
						customer, 
						account,
						acquirer, 
						issuer, 
						cardScheme, 
						branch_code, 
						currencyCodeId, 
						corporateCustomer, 
						corporateCustomerAccount, 
						this.swpService,
						account.getIsLive()
					);
				
				System.out.println("resp... = " + resp.toString());
				
				if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_ORDER_SUCCESSFUL))
				{
					ECard card = (ECard)resp.get("card");
					String smsMessage = "Hey, we've assigned a new card for you. Your card ends with ***" + card.getPan().substring(card.getPan().length() - 4) + 
						". You can load your card and start using your card. \nRemember to download our mobile application!";
					//new Thread(new SmsSender(this.swpService, smsMessage, card.getAccount().getCustomer().getContactMobile())).start();
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "New virtual card created successfully");
					return jsonObject;
					
				}
				else
				{
					jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
					jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "New virtual card could not be created");
					return jsonObject;
				}
			}
			else
			{
				jsonObject.put("status", ERROR.CUSTOMER_NOT_ACTIVE);
				jsonObject.put("message", "You can not assign the card to the customer. The customer is not an active customer");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return jsonObject;
	}


	@POST
	@Path("/getTutukaCardStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getTutukaCardStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token)
	{
		System.out.println(">>>>>>>>");
		JSONObject jsonObject = new JSONObject();
		try
		{
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			String trackingNumber = card.getTrackingNumber();
			JSONObject resp = TutukaHelper.getTutukaCardStatus(card, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==ERROR.GENERAL_OK)
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("statusList", resp.getJSONObject("statusList"));
				
				
				JSONObject statuses = resp.getJSONObject("statusList");
				System.out.println(TutukaCardStatus.ACTIVATED.name() + " = " + statuses.getBoolean(TutukaCardStatus.ACTIVATED.name()));
				System.out.println(TutukaCardStatus.CANCELLED.name() + " = " + statuses.getBoolean(TutukaCardStatus.CANCELLED.name()));
				System.out.println(TutukaCardStatus.EMPTY.name() + " = " + statuses.getBoolean(TutukaCardStatus.EMPTY.name()));
				System.out.println(TutukaCardStatus.EXPIRED.name() + " = " + statuses.getBoolean(TutukaCardStatus.EXPIRED.name()));
				System.out.println(TutukaCardStatus.LOADED.name() + " = " + statuses.getBoolean(TutukaCardStatus.LOADED.name()));
				System.out.println(TutukaCardStatus.LOST.name() + " = " + statuses.getBoolean(TutukaCardStatus.LOST.name()));
				System.out.println(TutukaCardStatus.PIN_BLOCKED.name() + " = " + statuses.getBoolean(TutukaCardStatus.PIN_BLOCKED.name()));
				System.out.println(TutukaCardStatus.REDEEMED.name() + " = " + statuses.getBoolean(TutukaCardStatus.REDEEMED.name()));
				System.out.println(TutukaCardStatus.RETIRED.name() + " = " + statuses.getBoolean(TutukaCardStatus.RETIRED.name()));
				System.out.println(TutukaCardStatus.STOLEN.name() + " = " + statuses.getBoolean(TutukaCardStatus.STOLEN.name()));
				System.out.println(TutukaCardStatus.STOPPED.name() + " = " + statuses.getBoolean(TutukaCardStatus.STOPPED.name()));
				System.out.println(TutukaCardStatus.VALID.name() + " = " + statuses.getBoolean(TutukaCardStatus.VALID.name()));
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card status could not be retrieved");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}


	
	@POST
	@Path("/activateTutukaCompanionPhysicalCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject activateTutukaCompanionPhysicalCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			//if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
			//{
				String trackingNumber = card.getTrackingNumber();
				JSONObject resp = TutukaHelper.getTutukaCardStatus(card, swpService);
				
				System.out.println("resp... = " + resp.toString());
				
				if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
				{
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("statusList", resp.getJSONObject("statusList"));
					JSONObject statusList = resp.getJSONObject("statusList");
					//if(statusList.getBoolean(TutukaCardStatus.ACTIVATED.name())==false)
					//{
						resp = TutukaHelper.activateTutukaCard(card, swpService);
						System.out.println("resp... = " + resp.toString());
						return jsonObject;
					//}
					
					
				}
				else
				{
					jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
					jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card status could not be retrieved");
					return jsonObject;
				}
			//}
			//jsonObject.put("status", ERROR.INVALID_CARD_TYPE);
			//jsonObject.put("message", "The card can not be activated due to the type of the card");
			//return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}


	@POST
	@Path("/linkCustomerToTutukaCompanionPhysicalCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject linkCustomerToTutukaCompanionPhysicalCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardBinId = data.getLong("cardBinId");
			Long accountId = data.getLong("accountId");
			Long poolAccountId = data.getLong("poolAccountId");
			String currencyCode = data.getString("currencyCodeId");
			Long cardSchemeId = data.getLong("cardSchemeId");
			
			
			String hql = "Select tp from ECardBin tp where tp.id = " + cardBinId;
			ECardBin cardBin = (ECardBin)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Account tp where tp.id = " + accountId;
			Account customerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			Customer customer = customerAccount.getCustomer();
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			CardScheme cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
			
			JSONObject resp = TutukaHelper.linkTutukaCardToCustomer(cardScheme, currencyCode, branch_code, bankCode, customer, customerAccount, cardBin, swpService, customerAccount.getIsLive());
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
			{
				jsonObject.put("card", (ECard)resp.get("card"));
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("statusList", resp.getJSONObject("statusList"));
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card status could not be retrieved");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	
	
	@POST
	@Path("/changeTutukaCompanionPhysicalCardPin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeTutukaCompanionPhysicalCardPin(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("epin") String epin, @FormParam("npin") String newPin, @FormParam("appDeviceId") String appDeviceId, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Pin change was not successful");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String cardId = data.getString("cardId");
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			hql = "Select tp from Customer tp where tp.user.id = " + tokenUser.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			if(customer==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "We could not find a profile mapped to this card. If this issue persists please contact your card issuer");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			Long customerId = customer.getId();
			

			
			System.out.println("cardId..." + cardId);
			
			String[] cardInfoArray = cardId.split("~");
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL";
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			if(card==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			System.out.println(card.getPin());
			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card pin provided. This card will be blocked after multiple failed attempts to change its pin.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			

			if(card.getPin().equals(newPin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid new card pin provided. Your new pin is the same as the old pin. Provide a different new pin.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			

			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card pin provided. This card will be blocked after multiple failed attempts to change its pin.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStatus()!=null && !card.getStatus().equals(CardStatus.ACTIVE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card. Card needs to be active before its pin can be changed");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStopFlag()!=null && card.getStopFlag().equals(Boolean.TRUE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card ending with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " is currently blocked. You can only change pins for active cards");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			//hql = "Select tp from Customer tp where tp.id = " + customerId;
			//Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			JSONObject resp = TutukaHelper.changeTutukaCardPin(customer, card, newPin, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "The pin for your card ending with ****" + card.getPan().substring(card.getPan().length()-4) + " has been changed successfully.");
				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card pin could not be changed successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	
	
	@POST
	@Path("/transferTutukaCompanionPhysicalCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject transferTutukaCompanionPhysicalCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			Long newCardId = data.getLong("newCardBinId");
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from ECardBin tp where tp.id = " + newCardId;
			ECardBin newCard = (ECardBin)swpService.getUniqueRecordByHQL(hql);
			Customer customer = card.getAccount().getCustomer();
			JSONObject resp = TutukaHelper.transferTutukaCard(customer, card, newCard, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_TRANSFERRED_SUCCESSFULLY))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Your new card ends with ****" + card.getPan().substring(card.getPan().length()-4) + ". You can no longer use the old card. Please proceed to start using the new card");
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card could not be transfered successfully");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return jsonObject;
	}


	
	@POST
	@Path("/stopTutukaCompanionCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopTutukaCompanionCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("epin") String epin, @FormParam("appDeviceId") String appDeviceId, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "The card you selected could not be blocked");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			//String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String cardId = data.getString("cardId");
			String stopCardReasonId = data.getString("stopCardReasonId");
			String notes = data.getString("notes");

			System.out.println("appDeviceId ==" + appDeviceId);
			hql = "Select tp from AppDevice tp where tp.appId = '"+ appDeviceId +"'";
			AppDevice appDevice = (AppDevice)swpService.getUniqueRecordByHQL(hql);
			if(appDevice==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid request received. Please consider refreshing your profile to secure your profile");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			//String decryptedPin = (String) UtilityHelper.decryptData(epin, appDevice.getDeviceKey());
			//System.out.println("decryptedPin ==" + decryptedPin);
			
			System.out.println("cardId..." + cardId);
			
			String[] cardInfoArray = cardId.split("~");
			
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL";
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			if(card==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card matching the pin provided could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			System.out.println(card.getPin());
			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card matching the pin provided could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStatus()!=null && !card.getStatus().equals(CardStatus.ACTIVE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card. Card needs to be active before it can be blocked");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStopFlag()!=null && card.getStopFlag().equals(Boolean.TRUE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card ending with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " and matching the pin provided has already been stopped");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			StopCardReason stopCardReason = StopCardReason.valueOf(stopCardReasonId);
			Customer customer = card.getAccount().getCustomer();
			JSONObject resp = TutukaHelper.stopTutukaCard(customer, card, stopCardReason, notes, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_STOPPED_SUCCESSFULLY))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Your new card ending with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " has been blocked. You can no longer use this card for your transactions until you remove the block on the card");

				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card could not be blocked successfully");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		

		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	

	@POST
	@Path("/unstopTutukaCompanionCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response unstopTutukaCompanionCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token, @FormParam("epin") String epin, @FormParam("appDeviceId") String appDeviceId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			String bankKey = null;
			String acquirerCode = null;
			
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Action was not performed");
				
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
					//jsonObject.put("token", verifyJ.getString("token"));
				}

				System.out.println("verifyJ ==" + verifyJ.toString());
				acquirerCode = verifyJ.getString("acquirerCode");
				bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
				String subject = verifyJ.getString("subject");
				User user = new Gson().fromJson(subject, User.class);
			
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			System.out.println("appDeviceId ==" + appDeviceId);
			hql = "Select tp from AppDevice tp where tp.appId = '"+ appDeviceId +"'";
			AppDevice appDevice = (AppDevice)swpService.getUniqueRecordByHQL(hql);
			if(appDevice==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid request received. Please consider refreshing your profile to secure your profile");
				return Response.status(200).entity(jsonObject.toString()).build();
			}

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String cardId = data.getString("cardId");
			String notes = data.getString("notes");
			
			System.out.println("cardId..." + cardId);
			
			String[] cardInfoArray = cardId.split("~");
			
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL";
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			if(card==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card matching the pin provided could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			System.out.println(card.getPin());
			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card matching the pin provided could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			if(!(card.getStopFlag()!=null && card.getStopFlag().equals(Boolean.TRUE)))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card ending with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " and matching the pin provided is already active");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = card.getAccount().getCustomer();
			JSONObject resp = TutukaHelper.unstopTutukaCard(customer, card, notes, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_UNSTOPPED_SUCCESSFULLY))
			{
				hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
						+ " AND (tp.deleted_at IS NULL)";
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				jsonObject.put("ecards", ecards==null ? null : ecards);
				jsonObject.put("status", ERROR.GENERAL_OK);
				
				jsonObject.put("message", "Your new card ends with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " has been unblocked. You can now begin to use this card for your transactions");
				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card could not be unblocked successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
		}
		catch(JwtException e)
		{
			System.out.println("JWT EXception");
			try {
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your Session has expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
	}


	@POST
	@Path("/retireTutukaCompanionCard")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject retireTutukaCompanionCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			Long customerId = data.getLong("customerId");
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Customer tp where tp.id = " + customerId;
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			JSONObject resp = TutukaHelper.retireTutukaCard(customer, card, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_RETIRED_SUCCESSFULLY))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Your card ending with ****" + card.getPan().substring(card.getPan().length()-4) + " has been retired. You can no longer use this card");
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card could not be retired successfully");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}


	@POST
	@Path("/updateTutukaCompanionVirtualCardCVV")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateTutukaCompanionVirtualCardCVV(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, 
			@FormParam("epin") String epin, @FormParam("appDeviceId") String appDeviceId, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "CVV Update was not successful. Please try again later");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			String hql = "Select tp from Customer tp where tp.user.id = " + tokenUser.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			if(customer==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "We could not find a profile mapped to this card. If this issue persists please contact your card issuer");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			Long customerId = customer.getId();

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			String cardId = data.getString("cardId");
			
			
			
			String[] cardInfoArray = cardId.split("~");
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL";
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			if(card==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card could not be found.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			System.out.println(card.getPin());
			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card pin provided. This card will be blocked after multiple failed attempts to change its CVV.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}

			if(!card.getPin().equals(epin))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card pin provided. This card will be blocked after multiple failed attempts to change its CVV.");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStatus()!=null && !card.getStatus().equals(CardStatus.ACTIVE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid card. Card needs to be active before its CVV can be changed");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(card.getStopFlag()!=null && card.getStopFlag().equals(Boolean.TRUE))
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "The card ending with ****" + card.getTrackingNumber().substring(card.getTrackingNumber().length()-4) + " is currently blocked. You can only change CVV for active cards");

				return Response.status(200).entity(jsonObject.toString()).build();
			}
			JSONObject resp = TutukaHelper.updateTutukaCardCVV(customer, card, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.CARD_CVV_UPDATED_SUCCESSFULLY))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "The CVV for your card ending with ****" + card.getPan().substring(card.getPan().length()-4) + " has been updated. Your new CVV is" + resp.getString("cvv"));
				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "The Card CVV could not be updated successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	


	@POST
	@Path("/getActivLinkedTutukaCompanionCards")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getActivLinkedTutukaCompanionCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			String cardTypeStr = data.getString("cardType");
			CardType cardType = null;
			if(cardTypeStr!=null)
			{
				cardType = CardType.valueOf(cardTypeStr);
			}
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			JSONObject resp = TutukaHelper.getCustomerActiveTutukaCards(card.getSerialNo(), cardType, swpService);
			
			System.out.println("resp... = " + resp.toString());
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	
	/*public JSONObject createNewMPQRTutukaCard(String encryptedData, String token)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			Customer customer = null;
			Bank issuer = null;
			Acquirer acquirer = null;
			CardScheme cardScheme = null;
			String currencyCode = null;
			PoolAccount poolAccount = null;
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			Device mpqrDevice = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
				//return Response.status(200).entity(jsonObject).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long customerId = data.getLong("customerId");
			Long acquirerId = data.getLong("acquirerId");
			Long cardSchemeId = data.getLong("cardSchemeId");
			String currencyCodeId = data.getString("currencyCodeId");
			Long poolAccountId = data.getLong("poolAccountId");
			Long mpqrDeviceId = data.getLong("mpqrDeviceId");
			Long corporateCustomerId = data.has("corporateCustomerId") ? data.getLong("corporateCustomerId") : null;
			Long corporateCustomerAccountId = data.has("corporateCustomerAccountId") ? data.getLong("corporateCustomerAccountId") : null;
			String customerTypeStr = data.getString("customerType");
			String meansOfIdentificationTypeStr = data.getString("meansOfIdentificationType");
			String meansOfIdentificationNumber = data.getString("meansOfIdentificationNumber");
			
			
			String hql = "Select tp from Customer tp where tp.id = " + customerId;
			customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Bank tp where tp.bankCode = '"+issuerBankCode+"'";
			issuer = (Bank)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Acquirer tp where tp.id = " + acquirerId;
			acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from PoolAccount tp where tp.id = " + poolAccountId;
			poolAccount = (PoolAccount)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Device tp where tp.id = " + mpqrDeviceId;
			mpqrDevice = (Device)swpService.getUniqueRecordByHQL(hql);
			CustomerType customerType = CustomerType.valueOf(customerTypeStr);
			MeansOfIdentificationType meansOfIdentificationType = MeansOfIdentificationType.valueOf(meansOfIdentificationTypeStr);
			if(corporateCustomerId!=null && corporateCustomerAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = " + corporateCustomerId;
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				hql = "Select tp from Account tp where tp.id = " + corporateCustomerAccountId;
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			if(TutukaHelper.modeTest==true && customer.getMeansOfIdentificationType()==null && customer.getMeansOfIdentificationNumber()==null)
			{
				customer.setMeansOfIdentificationType(MeansOfIdentificationType.NRC);
				customer.setMeansOfIdentificationNumber(RandomStringUtils.randomNumeric(15));
				swpService.updateRecord(customer);
			}
			
			JSONObject resp = TutukaHelper.createNewMPQRTutukaCard(customerType, meansOfIdentificationType, 
					meansOfIdentificationNumber, mpqrDevice, issuer, acquirer, cardScheme, branch_code, currencyCodeId, 
					poolAccount, corporateCustomer, corporateCustomerAccount, swpService);
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status").equals(ERROR.CARD_CREATED_SUCCESSFULLY))
			{
				ECard card = (ECard)resp.get("card");
				String smsMessage = "Hey, we've created a new virtual card for you. Your card ends with ***" + card.getPan().substring(card.getPan().length() - 4) + 
					". You can load your card and start using your card. \nRemember to download our mobile application!";
				new Thread(new SmsSender(this.swpService, smsMessage, card.getAccount().getCustomer().getContactMobile())).start();
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("card", card);
				jsonObject.put("message", "New virtual card created successfully");
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "New virtual card could not be created");
				return jsonObject;
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		return jsonObject;
	}*/
	
	
	@POST
	@Path("/createQRData")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createQRData(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerId") String encryptedData, @FormParam("token") String token)
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			System.out.println("verifyJ ==" + verifyJ.toString());
			//String acquirerCode = verifyJ.getString("acquirerCode");
			//System.out.println("acquirerCode ==" + acquirerCode);
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			
			
			Customer customer = null;
			Bank bank = null;
			Device device = null;
			Acquirer acquirer = null;
			CardScheme cardScheme = null;
			String currencyCode = null;
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			Long deviceId = data.getLong("deviceId");
			Long corporateCustomerId = data.has("corporateCustomerId") ? data.getLong("corporateCustomerId") : null;
			Long corporateCustomerAccountId = data.has("corporateCustomerAccountId") ? data.getLong("corporateCustomerAccountId") : null;
			//Long customerId = data.getLong("customerId");
			Long acquirerId = data.getLong("acquirerId");
			Long cardSchemeId = data.getLong("cardSchemeId");
			currencyCode = data.getString("currencyCodeId");
			//Long poolAccountId = data.getLong("poolAccountId");
			//String customerTypeStr = data.getString("customerType");
			//String meansOfIdentificationTypeStr = data.getString("meansOfIdentificationType");
			//String meansOfIdentificationNumber = data.getString("meansOfIdentificationNumber");
			String walletNumber = data.getString("walletNumber");
			String mpqrDataType = data.getString("mpqrDataType");
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerId+"'";
			acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from CardScheme tp where tp.id = "+cardSchemeId;
			cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Device tp where tp.id = " + deviceId;
			device = (Device)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Issuer tp where tp.id = " + cardScheme.getIssuer().getId();
			Issuer issuer =(Issuer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+walletNumber+"' AND tp.deleted_at IS NULL";
			Account walletAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			if(walletAccount==null)
			{
				JSONObject resp = new JSONObject();
	        	resp.put("message", "Invalid wallet number provided. Please provide a valid wallet");
	        	resp.put("status", ERROR.MPQR_ACCOUNT_NOT_CREATED_SUCCESSFULLY);
	        	return resp;
			}
			
			MPQRDataType mdt = MPQRDataType.valueOf(mpqrDataType);
			JSONObject resp = TutukaHelper.createQRData(walletAccount.getBranchCode(), device, swpService, tokenUser, walletAccount, mdt);
			
			
			System.out.println("---------------------------------Start MPQR DEbug");
			System.out.println(resp);
			System.out.println("new card resp status = " + resp.getInt("status"));
			if(resp!=null && resp.getInt("status")==(ERROR.MPQR_ACCOUNT_CREATED_SUCCESSFULLY))
			{
				//MPQRData mpqrData = (MPQRData)resp.get("mpqrData");
				//String qrCodeId = resp.getString("qrCodeId");
				
				
				/*MPQRData mpqrData = (MPQRData)this.swpService.getRecordById(MPQRData.class, 2558L);*/
				//JSONObject newCardResp = TutukaHelper.createNewMPQRTutukaCard(mpqrData, qrCodeId, customerType, meansOfIdentificationType, meansOfIdentificationNumber, device, acquirer, issuer, cardScheme, branch_code, currencyCode, corporateCustomer, corporateCustomerAccount, swpService);
				//System.out.println("newCardResp... = " + newCardResp.toString());
			}
			
			
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	@POST
	@Path("/deactivateQRData")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject deactivateQRData(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			
			
			MPQRData mpqrData = null;
			Long mpqrDataId = data.getLong("mpqrDataId");
			
			String hql = "Select tp from MPQRData tp where tp.id = "+mpqrDataId;
			mpqrData = (MPQRData)swpService.getUniqueRecordByHQL(hql);
			
			
			JSONObject resp = TutukaHelper.deactivateQRData(mpqrData, swpService);
			
			System.out.println("resp... = " + resp.toString());
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	//REMOTE API
	public JSONObject deductTutukaCompanionCards(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);

			JSONObject resp = TutukaHelper.DeductWallet(xml, swpService, app, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	
	
	@GET
	@Path("/getTutukaCompanionCardBalance")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getTutukaCompanionCardBalance(
			@QueryParam("xml") String xml, 
			@QueryParam("logId") String logId)
	{
		JSONObject jsonObject = new JSONObject();
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);

			JSONObject resp = TutukaHelper.getTutukaCardBalance(xml, swpService, app, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	


	public JSONObject deductReversalTutukaCompanionCards(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();

			JSONObject resp = TutukaHelper.DeductReversal(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}


	
	public JSONObject deductAdjustmentTutukaCompanionCard(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);

			JSONObject resp = TutukaHelper.DeductAdjustment(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	


	public JSONObject loadAdjustmentTutukaCompanionCard(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.LoadAdjustment(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = ");
			System.out.println(resp!=null ? resp.toString() : "null");
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	
	
	public JSONObject loadAdjustmentReversalTutukaCompanionCard(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.LoadReversal(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	
	
	public JSONObject stopRemoteTutukaCompanionCard(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.StopRemoteTutukaCard(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	
	
	public JSONObject handleAdministrativeMessage3DSecureOTP(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.AdministrativeMessage3DSecureOTP(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}


	public JSONObject handleLoadAuth(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.LoadAuth(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	

	
	public JSONObject handleLoadAuthReversal(String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.LoadAuthReversal(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}


	@POST
	@Path("/updateTutukaCompanionCardBearer")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateTutukaCompanionCardBearer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
				return jsonObject;
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);

			String decryptedData = (String) UtilityHelper.decryptData(encryptedData, bankKey);
			JSONObject data = new JSONObject(decryptedData);
			Long cardId = data.getLong("cardId");
			String firstName = data.getString("firstName");
			
			
			String hql = "Select tp from ECard tp where tp.id = " + cardId;
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			Customer customer = card.getAccount().getCustomer();
			System.out.println(customer.getId());
			System.out.println(card.getAccount().getCustomer().getId());
			
			if(!customer.getId().equals(card.getAccount().getCustomer().getId()))
			{
				jsonObject.put("status", ERROR.CARD_BEARER_MISMATCH);
				jsonObject.put("message", "The card can not be reassigned to another customer. To update the bearer details ensure you provide the details of the current bearer of this card");
				return jsonObject;
			}
			
			customer.setFirstName(firstName);
			swpService.updateRecord(customer);
			JSONObject resp = TutukaHelper.updateTutukaCardBearer(customer, card, swpService);
			
			System.out.println("resp... = " + resp.toString());
			
			if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
			{
				jsonObject.put("status", ERROR.GENERAL_OK);
				return jsonObject;
				
			}
			else
			{
				jsonObject.put("status", resp!=null && resp.has("status") ? resp.getInt("status") : ERROR.GENERAL_FAIL);
				jsonObject.put("message", resp!=null && resp.has("message") ? resp.getString("message") : "There are no cards linked to the customer");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	
	
	@POST
	@Path("/handleLoadMPQRWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject handleLoadMPQRWallet(
			@FormParam("xml") String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject resp = TutukaHelper.loadQRWallet(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}
	

	@POST
	@Path("/handleMPQRWalletLoadReversal")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject handleMPQRWalletLoadReversal(
			@FormParam("xml") String xml, String logId)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Action was not performed");
			
			this.swpService = this.serviceLocator.getSwpService();
			
			JSONObject resp = TutukaHelper.loadReversalQRWallet(xml, swpService, logId);
			
			System.out.println(logId + "- " + "resp... = " + (resp==null ? "null" : resp.toString()));
			
			return resp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + "- ", e);
		}
		
		return jsonObject;
	}



}
