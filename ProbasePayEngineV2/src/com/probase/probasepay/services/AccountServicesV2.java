package com.probase.probasepay.services;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
//import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.enumerations.WalletType;
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
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.SMSMesage;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/AccountServicesV2")
public class AccountServicesV2 {

	private static Logger log = Logger.getLogger(AccountServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	@GET
	@Path("/listAccountCards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAccountCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("accountIdentifier") String accountIdentifier, 
			@QueryParam("token") String token, 
			@QueryParam("merchantCode") String merchantCode, 
			@QueryParam("deviceCode") String deviceCode)
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
			String bankCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + bankCode);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			//Integer accountId = (Integer) UtilityHelper.decryptData(accountIdS, bankKey);
			
			Collection<ECard> eCardList = null;
			String hql = "Select tp from ECard tp";
			int and_ = 0;
			Account account = null;
			 
			if(accountIdentifier!=null)
			{
				String hqlAcct = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "'";
				account = (Account)this.swpService.getUniqueRecordByHQL(hqlAcct);
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " WHERE";
				}
				hql = hql + " tp.accountId = " + account.getId();
				
			}

			eCardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
			
			Iterator<ECard> customerCardIter = eCardList.iterator();
			JSONArray customerAccountArray = new JSONArray();
			while(customerCardIter.hasNext())
			{
				ECard card= customerCardIter.next();
				JSONObject oneCustomer = new JSONObject();
				oneCustomer.put("id", card.getId());
				oneCustomer.put("full_name", (card.getAccount().getCustomer().getLastName()==null ? "" : card.getAccount().getCustomer().getLastName()) + (card.getAccount().getCustomer().getFirstName()==null ? "" : " " + card.getAccount().getCustomer().getFirstName()));
				oneCustomer.put("accountIdentifier", card.getAccount().getAccountIdentifier()==null ? "" : card.getAccount().getAccountIdentifier());
				oneCustomer.put("serialNo", card.getSerialNo()==null ? "" : card.getSerialNo());
				oneCustomer.put("pan", UtilityHelper.formatPan(card.getPan()));
				oneCustomer.put("schemeName", card.getCardScheme().getSchemeName()==null ? "" : card.getCardScheme().getSchemeName());
				oneCustomer.put("cardType", card.getCardType()==null ? "" : card.getCardType().name());
				oneCustomer.put("status", card.getStatus()==null ? "" : card.getStatus().name());
				customerAccountArray.put(oneCustomer);
			}
			Response acctBalResp = this.getAccountBalance(httpHeaders, requestContext, accountIdentifier, token, merchantCode, deviceCode);
			String acctBalRespStr = (String)acctBalResp.getEntity();
			
			if(acctBalRespStr!=null)
			{
				JSONObject acctBalJS = new JSONObject(acctBalRespStr);
				//JSONObject acctBalJS = new JSONObject(acctBalString);

				jsonObject.put("currentBalance", acctBalJS.has("currentBalance") ? acctBalJS.getDouble("currentBalance") : 0.00);
				jsonObject.put("availableBalance", acctBalJS.has("availableBalance") ? acctBalJS.getDouble("availableBalance") : 0.00);
				jsonObject.put("floatingBalance", acctBalJS.has("floatingBalance") ? acctBalJS.getDouble("floatingBalance") : 0.00);
			}
			
			JSONObject oneAccount = new JSONObject();
			oneAccount.put("id", account.getId());
			oneAccount.put("accountIdentifier", account.getAccountIdentifier());
			oneAccount.put("accountType", account.getAccountType().name());
			oneAccount.put("acquirerName", account.getAcquirer().getAcquirerName());
			oneAccount.put("branchCode", account.getBranchCode());
			oneAccount.put("currencyCode", account.getCurrencyCode());
			oneAccount.put("customerFullName", account.getCustomer().getFirstName() + " " + account.getCustomer().getFirstName() + (account.getCustomer().getOtherName()==null ? account.getCustomer().getOtherName() : ""));
			oneAccount.put("status", account.getStatus()==null ? "" : account.getStatus().name());
				
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Account Cards list fetched successfully");
			jsonObject.put("customercardlist", customerAccountArray);
			jsonObject.put("account", oneAccount);
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Account Cards Fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}

	
	
	@POST
	@Path("/fundAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject fundAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("accountIdS") String accountIdS, 
			@FormParam("bankTransactionId") String bankTransactionId, 
			@FormParam("amountPaid") Double amountPaid, 
			@FormParam("token") String token)
	{
		BankStaff bankStaff = new BankStaff(); 
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
			String bankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			Integer accountId = (Integer) UtilityHelper.decryptData(accountIdS, bankKey);
			
			Account account = null;
			String hql = "Select tp from Account tp where tp.id = " + accountId;
			
			account = (Account)swpService.getUniqueRecordByHQL(hql);
			
			Transaction transaction = new Transaction();
			transaction.setChannel(Channel.OTC);
			transaction.setCreated_at(new Date());
			transaction.setAmount(amountPaid);
			transaction.setCustomerId(account.getCustomer().getId());
			transaction.setAccount(account);
			transaction.setCreditAccountTrue(true);
			transaction.setCreditPoolAccountTrue(true);
			transaction.setFixedCharge(null);
			transaction.setMessageRequest(null);
			transaction.setServiceType(ServiceType.DEPOSIT_OTC);
			transaction.setStatus(TransactionStatus.SUCCESS);
			transaction.setPayerEmail(account.getCustomer().getContactEmail());
			transaction.setPayerMobile(account.getCustomer().getContactMobile());
			transaction.setPayerName(account.getCustomer().getLastName() + ", " + account.getCustomer().getFirstName()  + " " + account.getCustomer().getOtherName());
			transaction.setResponseCode("00");
			transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
			transaction.setTransactionDate(new Date());
			transaction.setTransactionCode("00");
			transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
			transaction.setReceipientChannel(Channel.OTC);
			transaction.setTransactionDetail("Fund Account: Deposit " + amountPaid + " into Account #" + account.getAccountIdentifier());
			transaction.setReceipientEntityId(account.getId());
			
			hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
					"tp.account.id = " + account.getId() + " ORDER BY tp.updated_at DESC";
			Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
			Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
			transaction.setClosingBalance((lastTransaction!=null ? lastTransaction.getClosingBalance() : 0.0) + amountPaid);
			transaction.setUpdated_at(new Date());
			transaction.setTotalCreditSum((lastTransaction!=null && lastTransaction.getTotalCreditSum()!=null ? lastTransaction.getTotalCreditSum() : 0.0) + amountPaid);
			transaction.setDetails("Deposit Into Wallet");
			this.swpService.createNewRecord(transaction);
				
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Account Funded successfully");
			jsonObject.put("transaction", new Gson().toJson(transaction));
			jsonObject.put("amountFunded", transaction.getAmount());
			jsonObject.put("mobileNo", transaction.getAccount().getCustomer().getContactMobile());
			jsonObject.put("newBalance", transaction.getClosingBalance());
			jsonObject.put("accountNo", transaction.getAccount().getAccountIdentifier());
			jsonObject.put("txnDate", transaction.getTransactionDate());
			
			
			

			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
			String receipentMobileNumber = account.getCustomer().getContactMobile();
			String smsMessage = "Hi "+account.getCustomer().getFirstName()+",\nYour wallet "+account.getAccountIdentifier()+ " has been credit with the sum of " + account.getProbasePayCurrency().name() + transaction.getAmount() + 
					". Your new balance is " + account.getProbasePayCurrency().name() + lastTransaction.getClosingBalance();
			//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
			//swpService.createNewRecord(smsMsg);
			SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
			new Thread(smsSender).start();
			
			return jsonObject;
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_FAIL);
				jsonObject.put("message", "Account Funding Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
		
	}

	@POST
	@Path("/addNewCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNewCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("accountId") Long accountId, 
			@FormParam("nameOnCard") String nameOnCard, 
			@FormParam("cardType") String cardType, 
			@FormParam("cardSchemeId") Long cardSchemeId, 
			@FormParam("acquirerId") Long acquirerId, 
			@FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		
		try{
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
			
			String hql = "Select tp from Acquirer tp where lower(tp.id) = " + acquirerId + "";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from CardScheme tp where tp.id = " + cardSchemeId;
			System.out.println("3.hql ==" + hql);
			CardScheme cardScheme = (CardScheme)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Issuer tp where tp.id = " + cardScheme.getIssuer().getId();
			Issuer issuer = (Issuer)swpService.getUniqueRecordByHQL(hql);
			
			String bankKey = UtilityHelper.getBankKey(acquirer.getAcquirerCode(), swpService);
			System.out.println("2>> ==");
			System.out.println("21>> ==" + accountId);
			
			
			if(acquirer==null || cardScheme==null || issuer==null)
			{

				jsonObject.put("status", ERROR.INVALID_PARAMETERS);
				jsonObject.put("message", "Invalid parameters provided");
			}
			
			
			hql = "Select tp from Account tp where tp.id = " + accountId;
			System.out.println("2.hql ==" + hql);
			int and_ = 0;
			Account account = (Account)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp.* from countries where tp.id = " + account.getCustomer().getLocationDistrict().getCountryId();
			List<Map<String, Object>> countries_ = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			Map<String, Object> country = countries_.get(0);
			
			String pan = UtilityHelper.generatePan((String)country.get("mobileCode"), acquirer.getBank().getBankCode(), branch_code,  (account.getAccountIdentifier()));
			System.out.println("Pan ==" + pan);
			int cvi = new Random().nextInt(999);
			String cvv = cvi<10 ? ("00" + cvi) : ((cvi>9 && cvi<100) ? ("0" + cvi) : (cvi + ""));
			System.out.println("CVV ==" + cvv);
			String pin = (new Random(8999).nextInt() + 1000) + "";
			System.out.println("PIN ==" + pin);
			Calendar expiryDate = Calendar.getInstance();
			System.out.println("Expiry Date ==" + expiryDate);
			expiryDate.add(Calendar.YEAR, 1);
			Date expDate = expiryDate.getTime();
			
			ECard ecard = new ECard();
			ecard.setAccount(account);
			ecard.setIssuer(issuer);
			ecard.setCardScheme(cardScheme);
			ecard.setCardType(CardType.EAGLE_CARD);
			ecard.setCreated_at(new Date());
			ecard.setCustomerId(account.getCustomer().getId());
			ecard.setAccountId(account.getId());
			ecard.setCvv(cvv);
			ecard.setExpiryDate(expDate);
			ecard.setAcquirer(acquirer);
			ecard.setNameOnCard(nameOnCard);
			ecard.setPan(pan);
			ecard.setPin(pin);
			ecard.setSerialNo((RandomStringUtils.randomNumeric(16)));
			ecard.setStatus(CardStatus.ACTIVE);
			ecard = (ECard)this.swpService.createNewRecord(ecard);
			jsonObject.put("cvv", cvv);
			jsonObject.put("epin", pin);
			System.out.println("100 ==");
			
			
			System.out.println("201 ==");
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			System.out.println("202 ==");
			jsonObject.put("message", "New Card Added Successfully");
			System.out.println("203 ==");
			jsonObject.put("ecard", new Gson().toJson(ecard));
			System.out.println("204 ==");
			jsonObject.put("cardno", ecard.getPan().substring(0,  4) + "****" + ecard.getPan().substring(ecard.getPan().length()-4));
			System.out.println("205 ==");
			jsonObject.put("mobileNo", ecard.getAccount().getCustomer().getContactMobile());
			System.out.println("206 ==");
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
			String receipentMobileNumber = ecard.getAccount().getCustomer().getContactMobile();
			String smsMessage = "Hi "+ecard.getAccount().getCustomer().getFirstName()+",\nYour new Eagle Card ending with **** "+ecard.getPan().substring(ecard.getPan().length()-4)+ " has been created for you. You can fund your card to start using it.";
			//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
			//swpService.createNewRecord(smsMsg);

			SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
			new Thread(smsSender).start();
			
			smsMessage = "Your new Eagle Card ending with **** "+ecard.getPan().substring(ecard.getPan().length()-4)+ ".\nExp Date: " + sdf.format(expDate) + "\nPin: " + pin + "\nCVV: " + cvv + ".\nPlease change your pin before you start using this card";
			//SMSMesage smsMsg1 = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
			//swpService.createNewRecord(smsMsg1);

			SmsSender smsSender1 = new SmsSender(swpService, smsMessage, receipentMobileNumber);
			new Thread(smsSender1).start();
			
		}catch(Exception e)
		{
			log.warn(e);
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	@GET
	@Path("/lastFiveTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject lastFiveTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("accountId") Long accountId, 
			@QueryParam("token") String token)
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
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			
			
			
			String hql = "Select tp from Transaction tp where tp.account.id = " + 
					accountId + " ORDER BY transactionDate DESC";
			Collection<Transaction> transactionList = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 5);
			
			hql = "Select tp from Account tp where tp.id = " + accountId;
			Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Last Five Transactions Pulled Successfully");
			jsonObject.put("transactionList", transactionList.toArray());
			jsonObject.put("account", new Gson().toJson(account));
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	
	
	
	@GET
	@Path("/getAccountTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccountTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("walletId") Long walletId, 
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
			
			
			String hql = "Select tp.*, acc.accountIdentifier from transactions tp, accounts acc where tp.account_id = acc.id AND tp.card_id IS NOT NULL ";
			if(walletId!=null)
			{
				hql = hql + " AND acc.id = "+(walletId)+"";
				
			}
			System.out.println(hql);
			List<Map<String, Object>> wallettransactionslist = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			
			if(walletId!=null)
			{
				hql = "Select tp.* from accounts tp where tp.id = "+(walletId)+"";
				System.out.println(hql);
				
				List<Map<String, Object>> acct_ = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				Map<String, Object> acct = acct_.get(0);
				
				JSONObject walletJson = new JSONObject();
				walletJson.put("id", (BigInteger)acct.get("id"));
				walletJson.put("accountIdentifier", (String)acct.get("accountIdentifier"));
				jsonObject.put("wallet", walletJson);
			}
			
			
			
			
			
			
			jsonObject.put("status", ERROR.TRANSACTION_SUCCESS);
			jsonObject.put("message", "Wallet transactions fetched");
			jsonObject.put("wallettransactionslist", wallettransactionslist);
			System.out.println("verify3 ==");
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
				jsonObject.put("message", "Wallet transactions fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	

	@POST
	@Path("/updateAccountStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateAccountStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("status") Integer status, 
			@FormParam("accountId") String accountId, 
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			
			
			
			String hql = "Select tp from Customer tp where tp.user.id = " + tokenUser.getId();
			Customer customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountId + "' AND tp.customer.id = " + customer.getId();
			Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);

			if(status==1)
			{
				account.setStatus(AccountStatus.ACTIVE);
			}else if(status==0)
			{
				account.setStatus(AccountStatus.DISABLED);
			}
			swpService.updateRecord(account);
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Account status updated Successfully");
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	

	@GET
	@Path("/getAccountById")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getAccountById(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("accountIdS") String accountIdS, 
			@QueryParam("token") String token)
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
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			Integer accountId = (Integer) UtilityHelper.decryptData(accountIdS, bankKey);
			
			String hql = "Select tp from Account tp where tp.id = " + accountId;
			Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
			
			if(account!=null)
			{
				Customer cust = account.getCustomer();
				jsonObject.put("customer", new Gson().toJson(cust));
				jsonObject.put("account", new Gson().toJson(account));
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
				jsonObject.put("message", "An EWallet account has been setup for this account");
			}
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	
	
	@POST
	@Path("/getAccountBalance")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAccountBalance(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("token") String token, 
			@FormParam("merchantCode") String merchantCode, 
			@FormParam("deviceCode") String deviceCode)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			Merchant merchantTrafficSource = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			Device deviceTrafficSource = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(merchantTrafficSource!=null && deviceTrafficSource!=null && deviceTrafficSource.getMerchant().getId().equals(merchantTrafficSource.getId()))
			{
				
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Incomplete request.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			int isLive = deviceTrafficSource.getSwitchToLive();
			
			
			hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "' AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
			Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
			Double totalBalance = 0.00;
			Double floatingBalance = 0.00;
			if(account!=null)
			{
				Double totalAmount = 0.00;
				JSONObject cardBalances = new JSONObject();
				if(account.getAccountType().equals(AccountType.VIRTUAL) || account.getAccountType().equals(AccountType.DEVICE_SETTLEMENT))
				{
					AccountServicesV2 walletServices = new AccountServicesV2();
					Response walletBalDetResp = walletServices.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), account.getAccountIdentifier(), merchantCode, deviceCode);
					String walletDetString = (String)walletBalDetResp.getEntity();
					JSONObject balanceDet =   new JSONObject(walletDetString);
					
					if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
					{
						String accountList = balanceDet.getString("accountList");
						System.out.println("accountList ---" + accountList);
						JSONArray accounts = new JSONArray(accountList);
						for(int i=0; i<accounts.length(); i++)
						{
							JSONObject acct_ = accounts.getJSONObject(i);
							System.out.println("acct_ ==" + acct_.toString());
							//JSONObject acct_ = accounts;
							Double currentbalance = acct_.getDouble("currentbalance");
							System.out.println("currentbalance --  " + currentbalance);
							Double availablebalance = acct_.getDouble("availablebalance");
							System.out.println("availablebalance --  " + availablebalance);
							totalBalance = totalBalance + currentbalance;
							System.out.println("totalBalance --  " + totalBalance);
							//acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), currentbalance);
						}
						
						
						hql = "Select sum(tp.cardBalance + tp.cardCharges) as totalAmountDeposited, tp.cardBalance as cardBalance, tp.trackingNumber, tp.serialNo, tp.pan, tp.nameOnCard, tp.cvv, date_format(tp.expiryDate, '%m/%y') as expiryDate, tp.status, tp.stopFlag, tp.overDebit, tp.cardType from ecards tp where "
								+ "tp.account_id = "+account.getId()+" AND tp.isLive = "+ deviceTrafficSource.getSwitchToLive() + " Group By tp.id";
						System.out.println(hql);
						List<Map<String, Object>> cardsListing = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
						if(cardsListing!=null && cardsListing.size()>0)
						{
							for(int i1=0; i1<cardsListing.size(); i1++)
							{
								Map<String, Object> totalAmountMap = cardsListing.get(i1);
								Double amt = (Double)totalAmountMap.get("totalAmountDeposited");
								totalAmount = totalAmount + amt;
								System.out.println(totalAmount);
							}
						}
						jsonObject.put("cardsListing", cardsListing);
					}
				}
				else if(account.getAccountType().equals(AccountType.SAVINGS))
				{
					
						Double currentbalance = account.getAccountBalance();
						System.out.println("currentbalance --  " + currentbalance);
						Double availablebalance = account.getAccountBalance();
						System.out.println("availablebalance --  " + availablebalance);
						totalBalance = currentbalance;
						System.out.println("totalBalance --  " + totalBalance);
						//acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), currentbalance);
						
						
						hql = "Select sum(tp.cardBalance + tp.cardCharges) as totalAmountDeposited, tp.cardBalance as cardBalance, tp.trackingNumber, tp.serialNo, tp.pan, tp.nameOnCard, tp.cvv, date_format(tp.expiryDate, '%m/%y') as expiryDate, tp.status, tp.stopFlag, tp.overDebit, tp.cardType from ecards tp where "
								+ "tp.account_id = "+account.getId()+" AND tp.isLive = "+ deviceTrafficSource.getSwitchToLive() + " Group By tp.id";
						System.out.println(hql);
						List<Map<String, Object>> cardsListing = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
						if(cardsListing!=null && cardsListing.size()>0)
						{
							for(int i1=0; i1<cardsListing.size(); i1++)
							{
								Map<String, Object> totalAmountMap = cardsListing.get(i1);
								Double amt = (Double)totalAmountMap.get("cardBalance");
								totalAmount = totalAmount + amt;
								System.out.println(totalAmount);
							}
						}
						jsonObject.put("cardsListing", cardsListing);
					
				}
				
				
				hql = "Select tp.amount, tp.serviceType, tp.created_at, tp.details from transactions tp "
						+ "where tp.customerId = " + account.getCustomer().getId() + " AND tp.isLive = "+ deviceTrafficSource.getSwitchToLive() + " AND tp.status = " + TransactionStatus.SUCCESS.ordinal();
				System.out.println(hql);
				List<Map<String, Object>> recentTransactionsList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				JSONArray recentTransactionsListFormatted = new JSONArray();
				
				
				JSONObject jeys = new JSONObject();
				jeys.put("DEPOSIT_OTC", "Bank Deposit"); 
				jeys.put("PAY_MERCHANT", "Merchant Payment");
				jeys.put("DEBIT_MERCHANT", "Debit Merchant");
				jeys.put("REVERSE_PAYMENT_TO_MERCHANT", "Reverse Payment To Merchant");
				jeys.put("DEBIT_CARD", "Debit Card");
				jeys.put("CASHBACK", "Cashback");
				jeys.put("ADJUSTMENT", "Transaction Adjustment");
				jeys.put("CREDIT_CARD", "Credit Card"); 
				jeys.put("REVERSE_DEBIT_ON_CARD", "Reverse Card Debit");
				jeys.put("REVERSE_CREDIT_ON_CARD", "Reverse Card Credit");
				jeys.put("CASH_PAYMENT", "Cash Payment");
				jeys.put("REVERSE_REFUND", "Reverse Refund");
				jeys.put("MPQR_WALLET_LOAD", "MPQR Wallet Load");
				jeys.put("REVERSE_MPQR_WALLET_LOAD", "Reverse MPQR Wallet Load"); 
				jeys.put("DEBIT_WALLET", "Debit Wallet");
				jeys.put("CREDIT_WALLET", "Credit Wallet");
				jeys.put("REVERSE_DEBIT_ON_WALLET", "Reverse Debit On Wallet");
				jeys.put("FT_WALLET_TO_WALLET", "Transfer To Wallet");
				jeys.put("REVERSE_CREDIT_ON_WALLET", "Reverse Wallet Credit");
				jeys.put("FT_WALLET_TO_CARD", "Transfer To Card");
				jeys.put("FT_CARD_TO_WALLET", "Transfer To Wallet");
				jeys.put("FT_CARD_TO_CARD", "Transfer To Card"); 
				jeys.put("AIRTIME_PURCHASE", "Airtime Purchase");
				
				if(recentTransactionsList!=null && recentTransactionsList.size()>0)
				{
					DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a");
					for(int i1=0; i1<recentTransactionsList.size(); i1++)
					{
						Map<String, Object> recentTransaction = recentTransactionsList.get(i1);
						Double amt = (Double)recentTransaction.get("amount");
						Integer st = (Integer)recentTransaction.get("serviceType");
						Date dt = (Date)recentTransaction.get("created_at");
						String details = (String)recentTransaction.get("details");
						JSONObject js = new JSONObject();
						js.put("amount", amt);
						js.put("serviceType", jeys.getString(ServiceType.values()[st].name()));
						js.put("transactionDate", sdf.format(dt));
						js.put("details", details);
						recentTransactionsListFormatted.put(js);
					}
				}
				
				
				jsonObject.put("customer", account.getCustomer().getCustomerDetails());
				jsonObject.put("account", account.getAccountDetails());
				jsonObject.put("currentBalance", totalBalance==null ? 0.00 : totalBalance);
				jsonObject.put("availableBalance", totalBalance==null ? 0.00 : (totalBalance - totalAmount));
				jsonObject.put("floatingBalance", account.getFloatingBalance()==null ? 0.00 : account.getFloatingBalance());
				jsonObject.put("balanceExcludingCardBalances", account.getFloatingBalance());
				jsonObject.put("accountCurrency", account.getProbasePayCurrency().name());
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
				jsonObject.put("message", "Balance retrieved");
				jsonObject.put("recentTransactionsList", recentTransactionsListFormatted);
				
			}
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	


	
	@POST
	@Path("/cardWalletByTokenization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cardWalletByTokenization(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("bevuraTokenWallet") String bevuraTokenWallet, 
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
			
			String acquirerKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			bevuraTokenWallet = (String)UtilityHelper.decryptData(bevuraTokenWallet, acquirerKey);
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"'";
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from BevuraToken tp where tp.token = " + bevuraTokenWallet + " AND tp.type = 'ACCOUNT' AND tp.merchantId = " + merchant.getId() + 
					"  AND tp.deviceId = " + device.getId() + " AND tp.isLive = " + device.getSwitchToLive();
			BevuraToken bevuraToken = (BevuraToken)this.swpService.getUniqueRecordByHQL(hql);
			
			if(bevuraToken==null)
			{
    			//Card does not exist
    			jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
				jsonObject.put("message", "Invalid Wallet Detail Provided");
			
			}
			else
			{
				hql = "Select tp from Account tp where tp.id = " + bevuraToken.getAccountId() + " AND tp.isLive = " + device.getSwitchToLive();
				Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
				return this.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), account.getAccountIdentifier(), merchantCode, deviceCode);
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
	@Path("/listCorporateSubAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listCorporateSubAccounts(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("token") String token, 
			@QueryParam("corporateAcctId") String corporateAcctId)
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
			
			Collection<Account> corporateCustomerAccountList = null;
			Account account = null;
			if(corporateAcctId!=null)
			{
				Integer corporateAcctIdI = (Integer) UtilityHelper.decryptData(corporateAcctId, bankKey);
				String hql = "Select tp from Account tp WHERE tp.corporateCustomerAccountId = " + corporateAcctIdI;
				corporateCustomerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
				jsonObject.put("corporateCustomerAccountList", new Gson().toJson(corporateCustomerAccountList));
				
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
	
	
	
	
	
	
	
	



	@GET
	@Path("/getWalletDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWalletDetails(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("acquirerCode") String acquirerCode, 
			@QueryParam("bankAccountNo") String bankAccountNo, 
			@QueryParam("merchantId") String merchantId, 
			@QueryParam("deviceCode") String deviceCode)
	{
		JSONObject jsonObject = new JSONObject();
		
		JSONObject parameters = new JSONObject();
		JSONObject parametersRequest = new JSONObject();
		try {
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL";
			System.out.println(hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			Bank bank = acquirer.getBank();
			Device device = null;
			
			if(merchantId!=null && deviceCode!=null)
			{
				hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
				System.out.println(hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	System.out.println("device not found");
			    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "We could not find a device mapped to the device code and merchant id");
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			}
		    
			
			if(acquirer!=null && acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				JSONArray jsonArray = new JSONArray();
				hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal() + 
						" AND tp.isLive = " + device.getSwitchToLive();
				Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
				if(acct==null)
				{
					jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
					jsonObject.put("message", "Customer account not found");
					System.out.println("acctList size  ...0..");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
				
				
				JSONObject job = new JSONObject();
				job.put("currentbalance", acct.getAccountBalance());
				job.put("availablebalance", acct.getAccountBalance());
				job.put("accountId", acct.getId());
				jsonArray.put(job);
				
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Wallet found");
				jsonObject.put("accountList", jsonArray.toString());
				
				System.out.println("resp....1.." + jsonObject.toString());
				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else if(acquirer!=null && acquirer.getHoldFundsYes().equals(Boolean.TRUE))
			{
		    
				if(bank!=null && (bank.getBankCode().equals(Application.ZICB_BANK_CODE) || bank.getBankCode().equals(Application.UAT_ZICB_BANK_CODE)))
				{
					System.out.println("bank found");
					parametersRequest.put("accountNos", bankAccountNo);
					JSONObject header = new JSONObject();
					if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
					{
						//parametersRequest.put("serviceKey", acquirer.getServiceKey());
						//header.put("authKey", acquirer.getAuthKey());
						parametersRequest.put("serviceKey", device.getZicbServiceKey());
						header.put("authKey", device.getZicbAuthKey());
					}
					else
					{
						parametersRequest.put("serviceKey", device.getZicbDemoServiceKey());
						header.put("authKey", device.getZicbDemoAuthKey());
					}
					parameters.put("request", parametersRequest);
					parameters.put("service", "ZB0629");
					header.put("Content-Type", "application/json; utf-8");
					header.put("Accept", "application/json");
					
					System.out.println(header.toString());
					System.out.println(parameters.toString());
					
					String walletDetails = null;
					if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
					{
						System.out.println(">>>>");
						walletDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryEndPoint(), parameters.toString(), header);
					}
					else
					{
						System.out.println("<<<<");
						walletDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryDemoEndPoint(), parameters.toString(), header);
					}
					
					JSONObject walletDetailsJS = new JSONObject(walletDetails);
					Integer status = walletDetailsJS.getInt("status");
					//Integer status = 200;
					if(status==200)
					{
						
						JSONObject response = walletDetailsJS.getJSONObject("response");
						if(response.has("accountList"))
						{
							JSONArray accountList = response.getJSONArray("accountList");
							/*hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
							Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
							JSONArray accountList = new JSONArray();
							JSONObject acc = new JSONObject();
							acc.put("currentbalance", acct.getAccountBalance());
							acc.put("availablebalance", acct.getAccountBalance());
							accountList.put(acc);*/
		
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "Wallet found");
							jsonObject.put("accountList", accountList.toString());
							
							System.out.println("resp....1.." + jsonObject.toString());
							return Response.status(200).entity(jsonObject.toString()).build();
						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "Wallet found");
							JSONArray jsTemp = new JSONArray();
							JSONObject jsTemp2 = new JSONObject();
							jsTemp2.put("currentbalance", 0.00);
							jsTemp2.put("availablebalance", 0.00);
							jsTemp.put(jsTemp2);
							jsonObject.put("accountList", jsTemp.toString());
							
							System.out.println("resp....1.." + jsonObject.toString());
							return Response.status(200).entity(jsonObject.toString()).build();
						}
					}
					
					jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
					jsonObject.put("message", "We could not obtain details about this wallet");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "We could not obtain details about this wallet");
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "We could not obtain details about this wallet");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
		
	}
	
		
	
	





	@POST
	@Path("/validateWalletDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateWalletDetails(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("acquirerCode") String acquirerCode, 
			@FormParam("bankAccountNo") String bankAccountNo, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("deviceCode") String deviceCode)
	{
		JSONObject jsonObject = new JSONObject();
		
		JSONObject parameters = new JSONObject();
		JSONObject parametersRequest = new JSONObject();
		try {
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL";
			System.out.println(hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			Bank bank = acquirer.getBank();
			Device device = null;
			
			if(merchantId!=null && deviceCode!=null)
			{
				hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
				System.out.println(hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	System.out.println("device not found");
			    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "We could not find a device mapped to the device code and merchant id");
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			}
		    
		    if(acquirer.getHoldFundsYes().equals(Boolean.FALSE))
		    {
		    	
	    		hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal();
				Account account = (Account) swpService.getUniqueRecordByHQL(hql);
				
				if(account==null)
				{

					jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
					jsonObject.put("message", "We could not obtain details about this wallet");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				JSONArray jsonArray = new JSONArray();
				hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal();
				Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
				if(acct==null)
				{
					jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
					jsonObject.put("message", "Customer account not found");
					System.out.println("acctList size  ...0..");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
				
				
				JSONObject job = new JSONObject();
				job.put("currentbalance", acct.getAccountBalance());
				job.put("availablebalance", acct.getAccountBalance());
				job.put("accountId", acct.getId());
				job.put("customerName", acct.getCustomer().getFirstName() + " " + acct.getCustomer().getLastName());
				job.put("accountno", acct.getAccountIdentifier());
				jsonArray.put(job);

				JSONObject js_ = new JSONObject();
				String[] otpGenerated = UtilityHelper.generateWalletOTP(swpService, js_, account, null, account.getCustomer().getContactMobile(), account.getCustomer().getContactEmail(), acquirer, device);
				if(otpGenerated!=null && otpGenerated[0].equals("1"))
				{
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("otpRef", otpGenerated[3]);
					jsonObject.put("accountList", jsonArray.toString());
					jsonObject.put("message", "Wallet found");
				}
				else
				{
					jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
					jsonObject.put("message", "We could not send you an OTP to confirm your transaction. Please try again");
				}
				
				/*hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
				Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
				JSONArray accountList = new JSONArray();
				JSONObject acc = new JSONObject();
				acc.put("currentbalance", acct.getAccountBalance());
				acc.put("availablebalance", acct.getAccountBalance());
				accountList.put(acc);*/

				
				System.out.println("resp....1.." + jsonObject.toString());
				return Response.status(200).entity(jsonObject.toString()).build();
			
				
		    }
		    else
		    {
				if(bank!=null && (bank.getBankCode().equals(Application.ZICB_BANK_CODE) || bank.getBankCode().equals(Application.UAT_ZICB_BANK_CODE)))
				{
					System.out.println("bank found");
					parametersRequest.put("accountNos", bankAccountNo);
					JSONObject header = new JSONObject();
					if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
					{
						//parametersRequest.put("serviceKey", acquirer.getServiceKey());
						//header.put("authKey", acquirer.getAuthKey());
						parametersRequest.put("serviceKey", device.getZicbServiceKey());
						header.put("authKey", device.getZicbAuthKey());
					}
					else
					{
						parametersRequest.put("serviceKey", device.getZicbDemoServiceKey());
						header.put("authKey", device.getZicbDemoAuthKey());
					}
					parameters.put("request", parametersRequest);
					parameters.put("service", "ZB0629");
					header.put("Content-Type", "application/json; utf-8");
					header.put("Accept", "application/json");
					
					System.out.println(header.toString());
					System.out.println(parameters.toString());
					
					String walletDetails = null;
					if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
					{
						System.out.println(">>>>");
						walletDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryEndPoint(), parameters.toString(), header);
					}
					else
					{
						System.out.println("<<<<");
						walletDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryDemoEndPoint(), parameters.toString(), header);
					}
					
					JSONObject walletDetailsJS = new JSONObject(walletDetails);
					Integer status = walletDetailsJS.getInt("status");
					//Integer status = 200;
					if(status==200)
					{
						hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
						Account account = (Account) swpService.getUniqueRecordByHQL(hql);
						
						if(account==null)
						{
							
						}
						JSONObject response = walletDetailsJS.getJSONObject("response");
						JSONArray accountList = response.getJSONArray("accountList");
	
						JSONObject js_ = new JSONObject();
						String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, account.getCustomer().getContactMobile(), 
								account.getCustomer().getContactEmail(), acquirer, device, this.swpService);
						if(otpGenerated!=null && otpGenerated[0].equals("1"))
						{
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("otpRef", otpGenerated[3]);
							jsonObject.put("accountList", accountList.toString());
							jsonObject.put("message", "Wallet found");
						}
						else
						{
							jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
							jsonObject.put("message", "We could not send you an OTP to confirm your transaction. Please try again");
						}
						
						/*hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
						Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
						JSONArray accountList = new JSONArray();
						JSONObject acc = new JSONObject();
						acc.put("currentbalance", acct.getAccountBalance());
						acc.put("availablebalance", acct.getAccountBalance());
						accountList.put(acc);*/
	
						
						System.out.println("resp....1.." + jsonObject.toString());
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					
					jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
					jsonObject.put("message", "We could not obtain details about this wallet");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "We could not obtain details about this wallet");
				return Response.status(200).entity(jsonObject.toString()).build();
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
		
	}
	
	
	
	

	@POST
	@Path("/mapExistingBankWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject mapExistingBankWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,@FormParam("token") String token, @FormParam("merchantCode") String merchantCode, @FormParam("deviceCode") String deviceCode, @FormParam("accountNumber") String accountNumber, @FormParam("firstName") String firstName, 
			@FormParam("lastName") String lastName, @FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, @FormParam("addressLine3") String addressLine3, @FormParam("addressLine4") String addressLine4, 
			@FormParam("addressLine5") String addressLine5, @FormParam("uniqueType") String uniqueType, @FormParam("uniqueValue") String uniqueValue, @FormParam("dateOfBirth") String dateOfBirth, @FormParam("email") String email, 
			@FormParam("sex") String sex, @FormParam("mobileNumber") String mobileNumber, @FormParam("accType") String accType, @FormParam("currency") String currency, @FormParam("idFront") String idFront, @FormParam("idBack") String idBack, 
			@FormParam("custImg") String custImg, @FormParam("customerId") String custSig, @FormParam("issuerId") String issuerId, @FormParam("serviceTypeId") String serviceTypeId, 
			@FormParam("orderId") String orderId, @FormParam("responseUrl") String responseUrl, @FormParam("isWalletOrAccount") Integer isWalletOrAccount, @FormParam("accountType") String accountType)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			
			String hql = "Select tp from Issuer tp where tp.id = "+issuerId+" AND tp.deleted_at IS NULL";
			Acquirer issuer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			resp = mapExistingZicbWallet(httpHeaders, requestContext, token, merchantCode, deviceCode, accountNumber, firstName, 
					lastName, addressLine1, addressLine2, addressLine3, addressLine4, 
					addressLine5, uniqueType, uniqueValue, dateOfBirth, email, 
					sex, mobileNumber, accType, currency, idFront, idBack, 
					custImg, custSig, issuer, serviceTypeId, orderId, responseUrl, isWalletOrAccount, accountType);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resp;
	}
	


	public JSONObject createZicbWalletOld(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,String token, String merchantCode, String deviceCode, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Acquirer issuer, Long parentCustomerId, Long parentAccountId, String accountType)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" +acquirerCode);
			
			String hql = "Select tp from Account tp where tp.customer.contactMobile = '"+mobileNumber+"' AND tp.deleted_at IS NULL AND tp.accountType = " + AccountType.valueOf(accountType).ordinal();
			Account  account = (Account)swpService.getUniqueRecordByHQL(hql);
			
			if(account!=null)
			{
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a wallet in ZICB");
				return jsonObject;
			}
			
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			if(parentCustomerId!=null && parentAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = "+parentCustomerId+" AND tp.deleted_at IS NULL";
				System.out.println(hql);
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Account tp where tp.id = "+parentAccountId+" AND tp.deleted_at IS NULL";
				System.out.println(hql);
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
			System.out.println(hql);
			Merchant merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			
			if(merchant==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
				jsonObject.put("message", "Merchant Code Invalid");
				return jsonObject;
			}
			

			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
			System.out.println(hql);
			Device device = (Device)swpService.getUniqueRecordByHQL(hql);
			
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "Device code Invalid");
				return jsonObject;
			}
			
			
			Date dob = null;
			
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_CREATE_WALLET_SERVICE_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("firstName", firstName);
			parametersRequest.put("lastName", lastName);
			parametersRequest.put("add1", addressLine1);
			parametersRequest.put("add2", addressLine2);
			parametersRequest.put("add3", addressLine3);
			parametersRequest.put("add4", addressLine4);
			parametersRequest.put("add5", addressLine5);
			parametersRequest.put("uniqueType", uniqueType);
			parametersRequest.put("uniqueValue", uniqueValue);
			if(dateOfBirth!=null)
			{
				dob = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
				parametersRequest.put("dateOfBirth", dateOfBirth);
			}
			
			parametersRequest.put("email", email);
			parametersRequest.put("sex", sex);
			parametersRequest.put("mobileNumber", mobileNumber);
			parametersRequest.put("accType", accType);
			parametersRequest.put("currency", currency);
			parametersRequest.put("idFront", idFront);
			parametersRequest.put("idBack", idBack);
			parametersRequest.put("custImg", custImg);
			parametersRequest.put("custSig", custSig);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("authKey", device.getZicbAuthKey());
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			System.out.println(parameters.toString());
			System.out.println(header.toString());
			System.out.println("Test");
			System.out.println(device.getZicbAuthKey());
			String newWalletResponse = null;
			if(device.getIsTestYes()!=null && device.getIsTestYes().equals(Boolean.TRUE))
			{
				System.out.println(UtilityHelper.ZICB_CREATE_WALLET_URL_TEST); 
				//newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_CREATE_WALLET_URL_TEST, parameters.toString(), header);
				/*JSONObject sm = new JSONObject();
				JSONObject sm1 = new JSONObject();
				JSONObject tekHeader = new JSONObject();
				tekHeader.put("status", "SUCCESS");
				sm1.put("tekHeader", tekHeader);
				JSONObject cust = new JSONObject();
				JSONObject cust1 = new JSONObject();
				JSONObject cust2 = new JSONObject();
				cust2.put("idCustomer", RandomStringUtils.randomNumeric(8));
				cust2.put("branchCode", branch_code);
				cust1.put(RandomStringUtils.randomAlphanumeric(10).toUpperCase(), cust2);
				cust.put("accNos", cust1);
				
				sm1.put("cust", cust);
				sm.put("status", 200);
				sm.put("response", sm1);
				newWalletResponse = sm.toString();*/
				newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				System.out.println(UtilityHelper.ZICB_CREATE_WALLET_URL); 
				newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_CREATE_WALLET_URL, parameters.toString(), header);
			}
			System.out.println(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer account could not be created.");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
			}
			catch(Exception e)
			{
				System.out.println("Test");
				e.printStackTrace();
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer account could not be created.");
				return jsonObject;
			}
			JSONObject response = walletResponse.getJSONObject("response");
			Integer status = walletResponse.getInt("status");
			if(status!=200)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer account could not be created.");
				return jsonObject;
			}
			JSONObject responseHeader = response.getJSONObject("tekHeader");
			System.out.println(response.toString());
			if(response.get("cust")==null)
				System.out.println("nullable cust");
			else
				System.out.println("non nullable cust");
			String operationStatus = responseHeader.getString("status");
			if(operationStatus.equals("FAIL"))
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Account Creation was not Successfully");
			}
			else if(operationStatus.equals("SUCCESS"))
			{
				JSONObject customer_ = response.get("cust")!=null ? response.getJSONObject("cust") : null;
				if(customer_!=null)
				{
					JSONObject accNos = customer_.getJSONObject("accNos");
					Iterator<String> accNo = accNos.keys();
					String accNo_ = accNo.next();
					System.out.println("accNo..." + accNo_);
					JSONObject acctDetails = accNos.getJSONObject(accNo_);
		
					
					
					Customer customer = new Customer();
					customer.setAddressLine1(addressLine1);
					customer.setAddressLine2(addressLine2);
					customer.setContactEmail(email);
					customer.setAltContactMobile(null);
					customer.setContactMobile(mobileNumber);
					customer.setDateOfBirth(dob);
					customer.setFirstName(firstName);
					customer.setGender(sex==null ? null : Gender.valueOf(sex=="M" ? "MALE" : "FEMALE"));
					customer.setLastName(lastName);
					customer.setVerificationNumber(acctDetails.getInt("idCustomer") + "");
					customer.setStatus(CustomerStatus.ACTIVE);
					customer.setCreated_at(new Date());
					customer.setCustomerType(CustomerType.INDIVIDUAL);
					customer.setCustomerImage(custImg);
					customer = (Customer)this.swpService.createNewRecord(customer);
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					
					
					String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
					String mobileActivationCode = RandomStringUtils.randomAlphanumeric(6);
					hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
					Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
					Bank bank = issuer.getBank();
					
					JSONObject allSettings = app.getAllSettings();
					String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
					CardScheme accountScheme = null;
					if(defaultAccountSchemeIdObj!=null)
					{
						Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
						accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
					}
					
					account = new Account(customer, AccountStatus.ACTIVE, null, branch_code, issuer, currency, AccountType.VIRTUAL, accNo_, 
							customerAccountList==null ? 0 : customerAccountList.size(), 
							corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, ProbasePayCurrency.valueOf(currency), null, null, accountScheme, 0);
					
					
					account = (Account)this.swpService.createNewRecord(account);
					jsonObject.put("accountNo", accNo_);
					
					Transaction transaction = new Transaction();
					transaction.setChannel(Channel.OTC);
					transaction.setCreated_at(new Date());
					transaction.setAmount(0.00);
					transaction.setCustomerId(customer.getId());
					transaction.setFixedCharge(null);
					transaction.setMessageRequest(null);
					transaction.setServiceType(ServiceType.DEPOSIT_OTC);
					transaction.setStatus(TransactionStatus.SUCCESS);
					transaction.setPayerEmail(email);
					transaction.setPayerMobile(mobileNumber);
					transaction.setPayerName(lastName + ", " + firstName);
					transaction.setResponseCode(TransactionCode.transactionSuccess);
					transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
					transaction.setTransactionDate(new Date());
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setAccount(account);
					transaction.setCreditAccountTrue(true);
					transaction.setCreditPoolAccountTrue(true);
					transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
					transaction.setReceipientChannel(Channel.OTC);
					transaction.setTransactionDetail("Wallet Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
					transaction.setReceipientEntityId(account.getId());
					transaction.setDevice(device);
					transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
					transaction.setMerchantId(merchant.getId());
					transaction.setDetails("Wallet Opening");
					transaction = (Transaction)this.swpService.createNewRecord(transaction);
					jsonObject.put("transactionId", transaction.getId());
					
					
					
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountId", account.getId());
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Account Created Successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
					jsonObject.put("message", "New Customer Wallet Account Creation was not Successfully");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Account Creation was not Successfully");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}

	
	private JSONObject mapExistingZicbWallet(@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			String token, String merchantCode, String deviceCode, String accountNumber, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Acquirer issuer, String serviceTypeId, String orderId, String responseUrl, Integer isWalletOrAccount, String accountType)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			Acquirer acquirer = null;
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+ acquirerCode+"'";
			acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			
			hql = "Select tp from Account tp where tp.customer.contactMobile = '"+mobileNumber+"' AND tp.deleted_at IS NULL AND tp.walletType = " + AccountType.valueOf(accountType).ordinal();
			Account wallet = (Account)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
			System.out.println(hql);
			Merchant merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			
			if(merchant==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
				jsonObject.put("message", "Merchant Code Invalid");
				return jsonObject;
			}
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode +"' AND tp.status = " + DeviceStatus.ACTIVE.ordinal() + " AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
					
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "Device code Invalid");
				return jsonObject;
			}
			
			
			if(wallet!=null)
			{
				
				String api_key = device.getMerchant().getApiKey();
				String customerNumber = wallet.getCustomer().getVerificationNumber();
				DecimalFormat df = new DecimalFormat("0.00");
				String amt = df.format(0.00);
				Double amount = Double.valueOf(amt);
				
				
				responseUrl = device.getSuccessUrl();
				String toHash = merchantCode+deviceCode+serviceTypeId+customerNumber+amt+responseUrl+api_key;
				System.out.println("To HAsh = " + merchantCode+"-"+deviceCode+"-"+serviceTypeId+"-"+customerNumber+"-"+amt+"-"+responseUrl+"-"+api_key);
				String hash = "";
				try {
					hash = UtilityHelper.get_SHA_512_SecurePassword(toHash);
					System.out.println("1.hash = " + hash);
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}
				
				Response balResp = this.getAllEWalletAccountBalanceByCustomer(httpHeaders, requestContext, merchantCode, deviceCode, serviceTypeId, customerNumber, hash);
				String balRespString = (String)balResp.getEntity();
				JSONObject balanceStr =  new JSONObject(balRespString);
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a wallet in ZICB");
				jsonObject.put("accountBalances", balanceStr.toString());
				return jsonObject;
			}
			
			
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("mobileNo", mobileNumber);
			parametersRequest.put("accountType", isWalletOrAccount!=null && isWalletOrAccount==1 ? "CB" : (isWalletOrAccount!=null && isWalletOrAccount==0 ? "WB" : ""));
			parametersRequest.put("isfetchAllAccounts", false);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("authKey", device.getZicbAuthKey());
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			
			System.out.println("Test");
			System.out.println(device.getZicbAuthKey());
			System.out.println(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL);
			String newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL, parameters.toString(), header);
			System.out.println(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer account could not be created.");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
				JSONObject response_ = walletResponse.has("response") ? walletResponse.getJSONObject("response") : null;
				if(response_!=null)
				{
					JSONArray custAccDetails = response_.has("custAccDetails") ? response_.getJSONArray("custAccDetails") : null;
					if(custAccDetails!=null && custAccDetails.length()>0)
					{
						JSONObject custAccDetails_ = custAccDetails.getJSONObject(0);
						String accDesc = custAccDetails_.getString("accDesc");
						String custNo = custAccDetails_.getString("custNo");
						String accountNo = custAccDetails_.getString("accountNo");
						String branch = custAccDetails_.getString("branch");
						String[] accDesc_ = accDesc.split(" ");
						String mobileNo = custAccDetails_.getString("mobileNo");
						String uniqueIdVal = custAccDetails_.getString("uniqueIdVal");
						
						String mobileNumberSub = mobileNumber.substring(mobileNumber.length() - 9);
						
						if(mobileNumberSub.equals(mobileNo))
						{
							JSONObject js_ = new JSONObject();
							String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, "260" + mobileNo, "", acquirer, device, this.swpService);
							if(otpGenerated!=null && otpGenerated[0].equals("1"))
							{
								jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
								jsonObject.put("message", "OTP generated succcessfully");
								jsonObject.put("otpRef", otpGenerated[3]);
								
							}
							else
							{
								jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
								jsonObject.put("message", "OTP could not be generated succcessfully");
							}
						}
						
						
						
					}
					else
					{
						jsonObject.put("status", ERROR.INVALID_WALLET_PROVIDED);
						jsonObject.put("message", "We could not find any bank account linked to the details you shared");
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("Test");
				e.printStackTrace();
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer account could not be created.");
				return jsonObject;
			}
			
		}
		catch(Exception e)
		{
			
			log.error(">>>", e);
		}
		
		return jsonObject;
	}



	
	
	
	
	

	@GET
	@Path("/getAllEWalletAccountBalanceByCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllEWalletAccountBalanceByCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantCode") String merchantCode, @QueryParam("deviceCode") String deviceCode, 
			@QueryParam("serviceTypeId") String serviceTypeId, @QueryParam("customerNumber") String customerNumber, @QueryParam("hash") String hash)
	{
		JSONObject jsonObject = new JSONObject();
		System.out.println("merchantCode ... " + merchantCode);
		System.out.println("deviceCode ... " + deviceCode);
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			if(merchant==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
				jsonObject.put("message", "No Merchant match Found");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "No Device match Found");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			String api_key = merchant.getApiKey();
			if(UtilityHelper.validateTransactionHash(
					hash, 
					merchantCode,
					deviceCode,
					serviceTypeId,
					customerNumber,
					0.00, device.getSuccessUrl(),
					api_key)==true)
			{
			
				hql = "Select tp from Account tp where tp.customer.contactMobile = '" + customerNumber + "'";
				//AND tp.status = " + AccountStatus.ACTIVE.ordinal();
				System.out.println(hql);
				Collection<Account> acctList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			    System.out.println(acctList.size());
				Double totalBalance = 0.0;
				if(acctList.size()>0)
				{
					Iterator<Account> iter = acctList.iterator();
					JSONArray acctBalances = new JSONArray();
					while(iter.hasNext())
					{
						Account acct = (Account)iter.next();
						System.out.println(acct.getAccountType().name());
						if(acct.getAcquirer().getBank().getBankCode().equals(Application.ZICB_BANK_CODE) || acct.getAcquirer().getBank().getBankCode().equals(Application.UAT_ZICB_BANK_CODE)) //ZICB
						{
							
							Response walletBalDetResp = this.getWalletDetails(httpHeaders, requestContext, acct.getAcquirer().getAcquirerCode(), acct.getAccountIdentifier(), merchantCode, deviceCode);
							String walletDetString = (String)walletBalDetResp.getEntity();
							JSONObject balanceDet = new JSONObject(walletDetString);
							System.out.println("balanceStr == " + balanceDet.toString());
							
							if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
							{
								String accountList = balanceDet.getString("accountList");
								JSONArray accounts = new JSONArray(accountList);
								for(int i=0; i<accounts.length(); i++)
								{
									JSONObject acct_ = accounts.getJSONObject(i);
									//JSONObject acct_ = accounts;
									Double currentbalance = acct_.getDouble("currentbalance");
									Double availablebalance = acct_.getDouble("availablebalance");
									totalBalance = totalBalance + currentbalance;
									JSONObject js_ = new JSONObject();
									js_.put("accountNumber", acct.getAccountIdentifier());
									js_.put("accountId", acct.getId());
									js_.put("currentBalance", currentbalance);
									js_.put("availableBalance", availablebalance);
									acctBalances.put(js_);
								}
								
							}
						}
						else
						{
							
							hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
									"tp.account.id = " + acct.getId() + " ORDER BY tp.updated_at DESC";
							Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
							Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
							totalBalance = totalBalance + lastTransaction.getClosingBalance();
							JSONObject js_ = new JSONObject();
							js_.put("accountNumber", acct.getAccountIdentifier());
							js_.put("accountId", acct.getId());
							js_.put("currentBalance", lastTransaction.getClosingBalance());
							js_.put("availableBalance", lastTransaction.getClosingBalance());
							acctBalances.put(js_);
						}
								
					}
					jsonObject.put("status", ERROR.EWALLET_BALANCE_PULL_SUCCESS);
					jsonObject.put("message", "Balance Retrieved");
					jsonObject.put("balanceList", (acctBalances.toString()));
					jsonObject.put("balance", totalBalance);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				}
				else
				{
					//List of accounts is 0
					jsonObject.put("status", ERROR.EWALLET_ACCOUNTS_NO_EXIST);
					jsonObject.put("message", "No Wallet Accounts Found");
					jsonObject.put("balance", totalBalance);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	

}
