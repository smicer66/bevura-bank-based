package com.probase.probasepay.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletContext;
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
import org.apache.log4j.Priority;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.BankStaffStatus;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.VendorStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.DeviceBankAccount;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.RequestTransactionReversal;
import com.probase.probasepay.models.Settlement;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;

@Path("/TransactionServicesV2")
public class TransactionServicesV2 {
	private static Logger log = Logger.getLogger(TransactionServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/**Service Method - Customer signs up mobile money 
	 * on mobile application
	 * 
	 * @return Stringified JSONObject of the list of customers
	 */
	
	
	@GET
	@Path("/listTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerVerificationNumber") String customerVerificationNumber, @QueryParam("merchantId") String merchantId, @QueryParam("channel") String channel, @QueryParam("serviceType") String serviceType, @QueryParam("status") String status, 
			@QueryParam("creditAccountId") Long creditAccountId, @QueryParam("debitAccountId") Long debitAccountId, @QueryParam("creditCardId") Long creditCardId, @QueryParam("debitCardId") Long debitCardId, 
			@QueryParam("count") Integer  count, @QueryParam("token") String token, @QueryParam("bankId") Long bankId, 
			@QueryParam("issuerId") Integer  issuerId, @QueryParam("recBankCode") String recBankCode, @QueryParam("sourceBankCode") String sourceBankCode, @QueryParam("startTransactionDate") String startTransactionDate, 
			@QueryParam("endTransactionDate") String endTransactionDate, 
			@QueryParam("index") Integer  index)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		String req = customerVerificationNumber + "-|||-" + merchantId + "-|||-" + channel + "-|||-" + serviceType + "-|||-" + status + "-|||-" + 
				creditAccountId + "-|||-" + debitAccountId + "-|||-" + creditCardId + "-|||-" + debitCardId + "-|||-" + 
				count + "-|||-" + token + "-|||-" +  
				recBankCode + "-|||-" + startTransactionDate + "-|||-" + endTransactionDate;
		System.out.println("Request ==>" + req);
		
		JSONObject jsonObject = new JSONObject();
		List<Map<String, Object>> transactionList = null;
		Map<String, Object> merchant = null;
		Map<String, Object> creditAccount = null;
		Map<String, Object> debitAccount = null;
		Map<String, Object> creditCard = null;
		Map<String, Object> debitCard = null;
		Map<String, Object> bank = null;
		Map<String, Object> issuer = null;
		int i = 0;
		String period = "";
		if(index==null)
			index=0;
		if(count==null)
			count=Application.BASE_LIST_COUNT;
		
		try
		{
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
			System.out.println("acquirerCode ==" + acquirerCode);
			
			String subject = verifyJ.getString("subject");
			
			if(token!=null)
			{
				User user = null;
				
				if(subject!=null)
				{
					System.out.println("subject=" + subject);
					user = (User)(new Gson().fromJson(subject, User.class));
					
				}
	
				String bankKey = null;
				if(user!=null && user.getRoleType().equals(RoleType.MERCHANT))
				{
					String hql = "Select mt from Merchant mt where mt.user.id = " + user.getId();
					List<Map<String, Object>> merchantList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					
					bankKey = (String)merchantList.get(0).get("merchantDecryptKey");
				}else
				{
					bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
				}
			}
			
				
			
			String hql = "";
			String hql_join_tables = "";
			if(merchantId!=null)
			{
				
				hql = hql + " Where tp.merchantId = " + merchantId;
				i++;
				String sql = "Select tp.* from merchants tp where tp.id = " + merchantId;
				List<Map<String, Object>> merchantList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				merchant = merchantList.get(0);
			}
			if(channel!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.channel = " + Channel.valueOf(channel).ordinal();
			}
			if(serviceType!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.serviceType = " + ServiceType.valueOf(serviceType).ordinal();
			}
			if(status!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.status = " + TransactionStatus.valueOf(status).ordinal();
			}
			if(creditAccountId!=null)
			{
				
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.account_id = " + creditAccountId ;
				String sql = "Select tp.* from accounts tp where tp.id = " + creditAccountId;
				List<Map<String, Object>> accountList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				creditAccount = accountList.get(0);
			}
			if(debitAccountId!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.account_id = " + debitAccountId;
				String sql = "Select tp.* from accounts tp where tp.id = " + debitAccountId;
				List<Map<String, Object>> accountList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				debitAccount = accountList.get(0);
			}
			if(creditCardId!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.card_id = " + creditCardId;
				
				String sql = "Select tp.* from ecards tp where tp.id = " + creditCardId;
				List<Map<String, Object>> cardList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				creditCard = cardList.get(0);
			}
			if(debitCardId!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.card_id = " + debitCardId;
				
				String sql = "Select tp.* from ecards tp where tp.id = " + debitCardId;
				List<Map<String, Object>> cardList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				debitCard = cardList.get(0);
			}
			if(bankId!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.receipientTransactingBankId = " + bankId;
				
				String sql = "Select tp.* from banks tp where tp.id = " + bankId;
				List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				bank = bankList.get(0);
			}
			if(issuerId!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				hql = hql + "tp.issuer_id = " + issuerId;

				String sql = "Select tp.* from issuers tp where tp.id = " + issuerId;
				List<Map<String, Object>> issuerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				issuer = issuerList.get(0);
			}
			if(recBankCode!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;

				String sql = "Select tp.* from banks tp where tp.bankCode = '" + recBankCode+"'";
				List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				Map<String, Object> bank_= bankList.get(0);
				hql = hql + "tp.receipientTransactingBankId = " + (BigInteger)bank_.get("id");
				
			}
			
			
			if(sourceBankCode!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				String sql = "Select tp.* from banks tp where tp.bankCode = '"+sourceBankCode+"'";
				List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				Map<String, Object> bank_= bankList.get(0);
				hql = hql + "tp.transactingBankId = " + (BigInteger)bank_.get("id");
				
			}
			
			
			if(customerVerificationNumber!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				String sql = "Select tp.* from customers tp where tp.verificationNumber = '"+customerVerificationNumber+"'";
				List<Map<String, Object>> customerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				Map<String, Object> customer_= customerList.get(0);
				hql = hql + "tp.customerId = " + (BigInteger)customer_.get("id");
				
			}
			
			if(startTransactionDate!=null && endTransactionDate!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				startTransactionDate = startTransactionDate + " 00:00:00";
				endTransactionDate = endTransactionDate + " 23:59:59";
				hql = hql + "tp.transactionDate >= '" + startTransactionDate + "' AND tp.transactionDate <= '" + endTransactionDate + "'";
				period = "Transactions Between " + startTransactionDate + " AND " + endTransactionDate;
			}
			else
			{
				if(startTransactionDate!=null)
				{
					if(i==0)
						hql = hql + " WHERE ";
					else
						hql = hql + " AND ";
					
					i++;
					String startTransactionDate1 = startTransactionDate + " 00:00:00";
					endTransactionDate = startTransactionDate + " 23:59:59";
					hql = hql + "tp.transactionDate >= '" + startTransactionDate1 + "' AND tp.transactionDate <= '" + endTransactionDate + "'";
					period = "Transactions Occuring On " + startTransactionDate;
				}
			}
			
			String hql_main = "Select tp.* from transactions tp ";
			hql = hql_main + "" + hql_join_tables + "" + hql + " ORDER BY transactionDate DESC";
			if(count==null)
				hql = hql + " LIMIT " + (index*count) + ", " + count;
			System.out.println("HQL --->" + hql);
			transactionList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Transactions pulled succcessfully");
			jsonObject.put("transactionList", transactionList);
			jsonObject.put("merchant", merchant);
			jsonObject.put("creditAccount", creditAccount);
			jsonObject.put("debitAccount", debitAccount);
			jsonObject.put("creditCard", creditCard);
			jsonObject.put("debitCard", debitCard);
			jsonObject.put("bank", bank);
			jsonObject.put("channel", channel);
			jsonObject.put("serviceType", serviceType);
			jsonObject.put("period", period);
			jsonObject.put("txnstatus", status);
		}catch(Exception e)
		{
			log.warn(e);
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	
	@GET
	@Path("/listUtilitiesPaid")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listUtilitiesPaid(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerVerificationNumber") String customerVerificationNumber, @QueryParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		String req = customerVerificationNumber + "-|||-" + token;
		System.out.println("Request ==>" + req);
		
		JSONObject jsonObject = new JSONObject();
		List<Map<String, Object>> transactionList = null;
		Map<String, Object> merchant = null;
		Map<String, Object> creditAccount = null;
		Map<String, Object> debitAccount = null;
		Map<String, Object> creditCard = null;
		Map<String, Object> debitCard = null;
		Map<String, Object> bank = null;
		Map<String, Object> issuer = null;
		int i = 0;
		
		try
		{
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
			System.out.println("acquirerCode ==" + acquirerCode);
			
			String subject = verifyJ.getString("subject");
			User user = null;
			
			if(subject!=null)
			{
				System.out.println("subject=" + subject);
				user = (User)(new Gson().fromJson(subject, User.class));
				
			}

			String bankKey = null;
			if(user!=null && user.getRoleType().equals(RoleType.MERCHANT))
			{
				String hql = "Select mt from Merchant mt where mt.user.id = " + user.getId();
				List<Map<String, Object>> merchantList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				
				bankKey = (String)merchantList.get(0).get("merchantDecryptKey");
			}else
			{
				bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			}
			
				
			
			String hql = "";
			String hql_join_tables = "";
			
			if(customerVerificationNumber!=null)
			{
				if(i==0)
					hql = hql + " WHERE ";
				else
					hql = hql + " AND ";
				
				i++;
				String sql = "Select tp.* from customers tp where tp.verificationNumber = '"+customerVerificationNumber+"'";
				List<Map<String, Object>> customerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(sql);
				Map<String, Object> customer_= customerList.get(0);
				hql = hql + "tp.customerId = " + (BigInteger)customer_.get("id");
				
			}
			
			
			String hql_main = "Select tp.* from utility_purchased tp ";
			hql = hql_main + "" + hql_join_tables + "" + hql + " ORDER BY created_at DESC";
			
			System.out.println("HQL --->" + hql);
			transactionList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Utilities pulled succcessfully");
			jsonObject.put("transactionList", transactionList);
		}catch(Exception e)
		{
			log.warn(e);
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	@POST
	@Path("/reverseTransaction")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject reverseTransaction(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("requestId") String requestId, @FormParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		User user = null;
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
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			String subject = verifyJ.getString("subject");
			requestId = (String)UtilityHelper.decryptData(requestId, bankKey);
			
			System.out.println("requestId = " + requestId);
			//Integer deviceId = Integer.valueOf(txn_device[0]);
			//System.out.println("deviceId = " + deviceId);
			
			String hql = "Select tp from RequestTransactionReversal tp where tp.requestId = '" + requestId + "'";
			//+ " AND tp.device.id = "+ deviceId;
			System.out.println(hql);
			RequestTransactionReversal rtr = (RequestTransactionReversal)this.swpService.getUniqueRecordByHQL(hql);
			if(rtr!=null && rtr.getStatus().equals(TransactionStatus.PENDING))
			{
				hql = "Select tp from Transaction tp where tp.id = " + rtr.getTransaction().getId();
				Transaction tranx = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
				System.out.println("2-->" + hql);
				if(tranx!=null && tranx.getStatus().equals(TransactionStatus.SUCCESS))
				{
					if(tranx.getChannel().equals(Channel.VISA_MASTERCARD_WEB))
					{
						Transaction transaction = new Transaction();
						transaction.setPayerEmail(tranx.getPayerEmail());
						transaction.setPayerMobile(tranx.getPayerMobile());
						transaction.setPayerName(tranx.getPayerName());
						transaction.setMessageRequest(new Gson().toJson(rtr));
						transaction.setStatus(TransactionStatus.SUCCESS);
						transaction.setResponseCode(tranx.getResponseCode());
						transaction.setTransactionDate(new Date());
						transaction.setTransactionCode(tranx.getTransactionCode());
						transaction.setAccount(null);
						transaction.setCard(null);
						transaction.setCreditPoolAccountTrue(false);
						transaction.setFixedCharge(null);
						transaction.setTransactionPercentage(null);
						transaction.setUpdated_at(new Date());
						transaction.setTransactingBankId(tranx.getReceipientTransactingBankId());	//Because we are reversing payment
						transaction.setReceipientTransactingBankId(null);							//Because we are reversing payment
						transaction.setReceipientChannel(Channel.VISA_MASTERCARD_WEB);
						transaction.setTransactionDetail("Credit: MasterCard the amount " + transaction.getAmount());
						transaction.setReceipientEntityId(null);				//Because we are reversing
						transaction.setSourceEntityId(transaction.getMerchantId());
						transaction.setChannel(Channel.VISA_MASTERCARD_WEB);
						transaction.setCreated_at(new Date());
						transaction.setServiceType(ServiceType.REVERSE_PAYMENT_TO_MERCHANT);
						transaction.setTransactionDate(new Date());
						transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
						transaction.setResponseCode(TransactionCode.transactionPending);
						transaction.setOrderRef(tranx.getOrderRef() + "-REV");
						transaction.setAmount(rtr.getAmount());
						transaction.setMerchantId(tranx.getMerchantId());
						transaction.setMerchantAccount(tranx.getMerchantAccount());
						transaction.setMerchantBank(tranx.getMerchantBank());
						transaction.setMerchantCode(tranx.getMerchantCode());
						transaction.setMerchantName(tranx.getMerchantName());
						transaction.setDevice(tranx.getDevice());
						transaction.setDetails("Reverse Transaction");
						transaction = (Transaction)this.swpService.createNewRecord(transaction);
						
						rtr.setStatus(TransactionStatus.SUCCESS);
						rtr.setNewTransaction(transaction);
						this.swpService.updateRecord(rtr);
					}
					else if(tranx.getChannel().equals(Channel.WEB))
					{
						
						Transaction transaction = new Transaction();
						transaction.setChannel(tranx.getChannel());
						transaction.setCreated_at(new Date());
						transaction.setUpdated_at(new Date());
						transaction.setPayerEmail(tranx.getPayerEmail());
						transaction.setPayerMobile(tranx.getPayerMobile());
						transaction.setPayerName(tranx.getPayerName());
						transaction.setCustomerId(tranx.getCustomerId());
						transaction.setCard(tranx.getCard());
						transaction.setCreditAccountTrue(true);
						transaction.setCreditCardTrue(true);
						transaction.setServiceType(ServiceType.REVERSE_PAYMENT_TO_MERCHANT);
						transaction.setStatus(tranx.getStatus());
						transaction.setTransactionCode(tranx.getTransactionCode());
						transaction.setTransactionDate(new Date());
						transaction.setTransactionRef(rtr.getRequestId());
						transaction.setResponseCode(tranx.getResponseCode());
						transaction.setOrderRef(tranx.getOrderRef() + "-REV");
						transaction.setOTP(null);
						transaction.setTransactionDetail("Credit: EagleCard/Account/Wallet/MMobile Account the amount " + transaction.getAmount() + " from Merchant #" + tranx.getMerchantCode());
						
						transaction.setAmount(rtr.getAmount());
						transaction.setMerchantId(tranx.getMerchantId());
						transaction.setMerchantAccount(tranx.getMerchantAccount());
						transaction.setMerchantBank(tranx.getMerchantBank());
						transaction.setMerchantCode(tranx.getMerchantCode());
						transaction.setMerchantName(tranx.getMerchantName());
						transaction.setDevice(tranx.getDevice());
						transaction.setTransactingBankId(tranx.getTransactingBankId());
						transaction.setReceipientTransactingBankId(tranx.getReceipientTransactingBankId());
						transaction.setCreditPoolAccountTrue(false);
						transaction.setDetails("Reverse Transaction");
						transaction = (Transaction)this.swpService.createNewRecord(transaction);
						rtr.setStatus(TransactionStatus.SUCCESS);
						rtr.setNewTransaction(transaction);
						this.swpService.updateRecord(rtr);
					}
					else if(tranx.getChannel().equals(Channel.ONLINE_BANKING))
					{
						String responseData = tranx.getMessageResponse();
						JSONObject response = new JSONObject(responseData);
						String paymentParticulars_ = response.getString("paymentParticulars");
						
						Transaction transaction = new Transaction();
						
						transaction.setChannel(Channel.ONLINE_BANKING);
						transaction.setCreated_at(new Date());
						transaction.setPayerEmail(tranx.getPayerEmail());
						transaction.setPayerMobile(tranx.getPayerMobile());
						transaction.setPayerName(tranx.getPayerName());
						transaction.setCustomerId(null);
						transaction.setServiceType(ServiceType.REVERSE_PAYMENT_TO_MERCHANT);
						transaction.setStatus(TransactionStatus.SUCCESS);
						transaction.setTransactionCode(TransactionCode.transactionSuccess);
						transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
						transaction.setResponseCode(TransactionCode.transactionSuccess);
						transaction.setOrderRef(tranx.getOrderRef() + "-REV");
						transaction.setOTP(null);
						transaction.setAmount(rtr.getAmount());
						transaction.setTransactingBankId(tranx.getReceipientTransactingBankId());
						transaction.setReceipientTransactingBankId(null);
						transaction.setMerchantId(tranx.getMerchantId());
						transaction.setMerchantAccount(tranx.getMerchantAccount());
						transaction.setMerchantBank(tranx.getMerchantBank());
						transaction.setMerchantCode(tranx.getMerchantCode());
						transaction.setMerchantName(tranx.getMerchantName());							
						transaction.setDevice(tranx.getDevice());
						transaction.setMessageRequest(new Gson().toJson(rtr));
						transaction.setFixedCharge(null);
						transaction.setTransactionPercentage(null);
						transaction.setSchemeTransactionCharge(null);
						transaction.setSchemeTransactionPercentage(null);
						transaction.setUpdated_at(new Date());
						transaction.setTransactionDate(new Date());
						transaction.setReceipientChannel(Channel.ONLINE_BANKING);
						transaction.setTransactionDetail("Credit: ONLINE BANKING - Bank Ref #" + paymentParticulars_ + " the amount " + rtr.getAmount() + " from Merchant Code - " + transaction.getDevice().getMerchant().getMerchantCode());
						transaction.setReceipientEntityId(null);
						transaction.setSourceEntityId(transaction.getMerchantId());		
						transaction.setCreditPoolAccountTrue(false);
						transaction.setDetails("Reverse Transaction");
						transaction = (Transaction)this.swpService.createNewRecord(transaction);
						
						rtr.setStatus(TransactionStatus.SUCCESS);
						rtr.setNewTransaction(transaction);
						this.swpService.updateRecord(rtr);
					}
					else if(tranx.getChannel().equals(Channel.WALLET))
					{
						Transaction transaction = new Transaction();
						transaction.setMessageRequest(null);
						transaction.setStatus(TransactionStatus.SUCCESS);
						transaction.setResponseCode(TransactionCode.transactionSuccess);
						transaction.setTransactionDate(new Date());
						transaction.setTransactionCode(TransactionCode.transactionSuccess);
						transaction.setAccount(tranx.getAccount());
						transaction.setCreditAccountTrue(true);
						transaction.setFixedCharge(null);
						transaction.setTransactionPercentage(null);
						transaction.setSchemeTransactionCharge(null);
						transaction.setSchemeTransactionPercentage(null);
						transaction.setUpdated_at(new Date());
						transaction.setChannel(Channel.WALLET);
						transaction.setCreated_at(new Date());
						transaction.setPayerEmail(tranx.getPayerEmail());
						transaction.setPayerMobile(tranx.getPayerMobile());
						transaction.setPayerName(tranx.getPayerName());
						transaction.setCustomerId(tranx.getCustomerId());
						transaction.setServiceType(ServiceType.REVERSE_PAYMENT_TO_MERCHANT);
						transaction.setTransactionDate(new Date());
						transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
						transaction.setOrderRef(tranx.getOrderRef() + "-REV");
						transaction.setAmount(rtr.getAmount());
						transaction.setMerchantId(tranx.getMerchantId());
						transaction.setMerchantAccount(tranx.getMerchantAccount());
						transaction.setMerchantBank(tranx.getMerchantBank());
						transaction.setMerchantCode(tranx.getMerchantCode());
						transaction.setMerchantName(tranx.getMerchantName());
						transaction.setOTP(null);
						transaction.setDevice(tranx.getDevice());
						transaction.setTransactingBankId(tranx.getReceipientTransactingBankId());
						transaction.setReceipientTransactingBankId(tranx.getTransactingBankId());
						transaction.setCustomerId(tranx.getCustomerId());

						transaction.setReceipientChannel(Channel.WALLET);
						transaction.setTransactionDetail("Credit " + rtr.getAmount() + " into Wallet Account #" + tranx.getAccount().getAccountIdentifier() + " from Merchant - " + tranx.getMerchantCode());
						transaction.setReceipientEntityId(tranx.getAccount().getId());
						transaction.setSourceEntityId(tranx.getReceipientEntityId());
						transaction.setUpdated_at(new Date());
						transaction.setDetails("Reverse Transaction");
						
						hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
								"tp.account.id = " + tranx.getAccount().getId() + " ORDER BY tp.updated_at DESC";
						Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
						Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
						transaction.setClosingBalance((lastTransaction!=null ? lastTransaction.getClosingBalance() : 0.0) + tranx.getAmount());
						transaction.setTotalDebitSum((lastTransaction!=null && lastTransaction.getTotalDebitSum()!=null ? lastTransaction.getTotalDebitSum() : 0.0) - tranx.getAmount());
						transaction.setUpdated_at(new Date());
						
						
						transaction = (Transaction)this.swpService.createNewRecord(transaction);
						
						rtr.setStatus(TransactionStatus.SUCCESS);
						rtr.setNewTransaction(transaction);
						this.swpService.updateRecord(rtr);
					}
					
					
					
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("transaction", new Gson().toJson(tranx));
					jsonObject.put("message", "Transaction #" + tranx.getTransactionRef() + " was reversed successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
					jsonObject.put("transaction", new Gson().toJson(tranx));
					jsonObject.put("message", "Transaction #" + rtr.getTransaction().getTransactionRef() + " was not found for reversal");
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		return jsonObject;
	}

	
	@GET
	@Path("/getTransactionStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTransactionStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("merchantCode") String merchantCode, 
			@QueryParam("deviceCode") String deviceCode, 
			@QueryParam("transactionRef") String transactionRef, 
			@QueryParam("orderRef") String orderRef, 
			@QueryParam("cardSerialNo") String cardSerialNo ) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			
			String hql = "Select tp from Transaction tp where tp.deleted_at IS NULL AND tp.orderRef = '"+ orderRef + "'";
			
			if(merchantCode!=null)
				hql = hql + " AND tp.device.merchant.merchantCode = '" + merchantCode + "'";
			if(deviceCode!=null)
				hql = hql + " AND tp.device.deviceCode = '" +deviceCode+ "' ";
			if(transactionRef!=null)
			{
				hql = hql + " AND tp.transactionRef = '"+ transactionRef + "'";
			}
			if(cardSerialNo!=null)
			{
				hql = hql + " AND tp.card.serialNo = '"+ cardSerialNo +"'";
			}
			System.out.println(hql);
			Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
			if(transaction!=null)
			{
				System.out.println(1090);
				System.out.println(1091);
				
				try {
					
					System.out.println(1092);
					String txnMessage = "";
					String status = "";
					JSONObject jsonReturn = new JSONObject();
					if(transaction.getStatus().equals(TransactionStatus.FAIL)){
						status = "3";
						txnMessage = "Transaction Failed to Complete";}
					else if(transaction.getStatus().equals(TransactionStatus.PAIDOUT)){
						status = "4008";
						txnMessage = "Transaction Funds Has Been Paid Out to Merchant";}
					else if(transaction.getStatus().equals(TransactionStatus.PENDING)){
						status = "4009";
						txnMessage = "Transaction is still Pending";}
					else if(transaction.getStatus().equals(TransactionStatus.REVERSED)){
						status = "4010";
						txnMessage = "Transaction has been reversed";}
					else if(transaction.getStatus().equals(TransactionStatus.SUCCESS)){
						status = "00";
						txnMessage = "Transaction was successful";}
					
					Bank transactingBank = null;
					String bankReference = null;
					if(transaction.getTransactingBankId()!=null)
					{
						hql = "Select tp from Bank tp where tp.id = " + transaction.getTransactingBankId();
						transactingBank = (Bank)swpService.getUniqueRecordByHQL(hql);
						bankReference = transaction.getBankPaymentReference();
						if(transactingBank!=null)
						{
							jsonReturn.put("bankCode", transactingBank.getBankCode());
							jsonReturn.put("bankName", transactingBank.getBankName());
						}
					}
					
					jsonReturn.put("bankPaymentReference", bankReference);
					jsonReturn.put("statusmessage", txnMessage); 
					jsonReturn.put("status", ERROR.GENERAL_OK);
					jsonReturn.put("TransactionRef", transaction.getTransactionRef()); 
					jsonReturn.put("transactiontime", transaction.getCreated_at()); 
					jsonReturn.put("orderId", transaction.getOrderRef());
					jsonReturn.put("amount", transaction.getAmount());
					jsonReturn.put("channel", transaction.getChannel().name());
					
					Boolean creditTrue = false;
					creditTrue = transaction.getCreditAccountTrue()!=null || transaction.getCreditCardTrue() ? true : false;
					jsonReturn.put("debitOrCredit", creditTrue);
					jsonReturn.put("fixedCharge", transaction.getFixedCharge()==null ? 0.00 : transaction.getFixedCharge());
					jsonReturn.put("payerEmail", transaction.getPayerEmail()==null ? "" : transaction.getPayerEmail());
					jsonReturn.put("payerMobile", transaction.getPayerMobile()==null ? "" : transaction.getPayerMobile());
					jsonReturn.put("payerName", transaction.getPayerName()==null ? "" : transaction.getPayerName());
					jsonReturn.put("paymentSchemeTransactionCharge", transaction.getSchemeTransactionCharge()==null ? 0.00 : transaction.getSchemeTransactionCharge());
					jsonReturn.put("paymentSchemeTransactionPercentage", transaction.getSchemeTransactionPercentage()==null ? 0.00 : transaction.getSchemeTransactionPercentage());
					jsonReturn.put("serviceType", transaction.getServiceType()==null ? "" : transaction.getServiceType().name());
					jsonReturn.put("transactionCharge", transaction.getTransactionCharge());
					jsonReturn.put("transactionPercentage", transaction.getTransactionPercentage()==null ? 0.00 : transaction.getTransactionPercentage());
					jsonReturn.put("narration", transaction.getTransactionDetail()==null ? "" : transaction.getTransactionDetail());
					jsonReturn.put("messageResponse", transaction.getMessageResponse()==null ? "" : transaction.getMessageResponse());
					return Response.status(200).entity(jsonReturn.toString()).build();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					System.out.println(1094);
					e.printStackTrace();
					System.out.println(e.getMessage());
					JSONObject jsonReturn = new JSONObject();
					jsonReturn.put("statusmessage", "General System Error");
					jsonReturn.put("status", "99");
					jsonReturn.put("TransactionRef", transaction.getTransactionRef()); 
					jsonReturn.put("transactiontime", transaction.getCreated_at()); 
					jsonReturn.put("orderId", transaction.getOrderRef());
					return Response.status(200).entity(jsonReturn.toString()).build();
				}
				
			}
			else
			{
				System.out.println(1096);
				JSONObject jsonReturn = new JSONObject();
				jsonReturn.put("statusmessage", "Transaction Not Found"); 
				jsonReturn.put("status", "906");
				jsonReturn.put("TransactionRef", transactionRef);
				return Response.status(200).entity(jsonReturn.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			System.out.println(1097);
			e.printStackTrace();
			log.warn(e);
			JSONObject jsonReturn = new JSONObject();
			jsonReturn.put("statusmessage", "General System Error"); 
			jsonReturn.put("status", "99");
			return Response.status(200).entity(jsonReturn.toString()).build();
		}
	}
	
	
	@GET
	@Path("/confirmTransactionStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject confirmTransactionStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("carrier") String carrier, @QueryParam("mobilePaymentRefCode") String mobilePaymentRefCode, @QueryParam("transactionRef") String transactionRef) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			//HandleCall to carrier to confirm payment was actually made
			
			//String hql = "Select tp from Transaction tp where tp.device.merchant.merchantCode = '" + merchantCode 
				//	+ "' AND tp.device.deviceCode = '" +deviceCode+ "' AND tp.transactionRef = '"+ transactionRef + "'";
			String hql = "Select tp from Transaction tp where tp.transactionRef = '"+ transactionRef + "'";
			System.out.println(hql);
			Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
			if(transaction!=null)
			{
				System.out.println(1090);
				Merchant merchant = transaction.getDevice().getMerchant();
				if(merchant!=null)
				{
					System.out.println(1091);
					String apiKey = merchant.getApiKey();
					String toHash = transactionRef+apiKey+merchant.getMerchantCode();
					System.out.println("To HAsh = " + transactionRef+"-"+apiKey+"-"+merchant.getMerchantCode());
					try {
						String hashed = UtilityHelper.get_SHA_512_SecurePassword(toHash);
						System.out.println("1.carrier = " + carrier);
						System.out.println("2.hash = " + hashed);
						System.out.println("3.mobilePaymentRefCode = " + mobilePaymentRefCode);
						
						
						System.out.println(1092);
						String txnMessage = "";
						String status = "";
						JSONObject jsonReturn = new JSONObject();
						if(transaction.getStatus().equals(TransactionStatus.FAIL)){
							status = "3";
							txnMessage = "Transaction Failed to Complete";}
						else if(transaction.getStatus().equals(TransactionStatus.PAIDOUT)){
							status = "4008";
							txnMessage = "Transaction Failed to Complete. Funds already settled";}
						else if(transaction.getStatus().equals(TransactionStatus.PENDING)){
							status = "00";
							transaction.setStatus(TransactionStatus.SUCCESS);
							this.swpService.updateRecord(transaction);
							txnMessage = "Transaction was confirmed successfully";}
						else if(transaction.getStatus().equals(TransactionStatus.REVERSED)){
							status = "4010";
							txnMessage = "Transaction has been reversed. Confirmation of this transaction failed";}
						else if(transaction.getStatus().equals(TransactionStatus.SUCCESS)){
							status = "00";
							txnMessage = "Transaction has already been confirmed successfully before";}
						
						jsonReturn.put("statusMessage", txnMessage); 
						jsonReturn.put("status", status);
						jsonReturn.put("carrierPayRef", mobilePaymentRefCode);
						jsonReturn.put("carrier", carrier);
						jsonReturn.put("transactionRef", transaction.getTransactionRef()); 
						jsonReturn.put("transactionTime", transaction.getCreated_at()); 
						jsonReturn.put("orderId", transaction.getOrderRef());
						return jsonReturn;
					
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						System.out.println(1094);
						e.printStackTrace();
						System.out.println(e.getMessage());
						JSONObject jsonReturn = new JSONObject();
						jsonReturn.put("statusMessage", "General System Error"); 
						jsonReturn.put("status", 99);
						jsonReturn.put("carrierPayRef", mobilePaymentRefCode);
						jsonReturn.put("carrier", carrier);
						jsonReturn.put("transactionRef", transaction.getTransactionRef()); 
						jsonReturn.put("transactionTime", transaction.getCreated_at()); 
						jsonReturn.put("orderId", transaction.getOrderRef());
						return jsonReturn;
					}
				}else
				{
					System.out.println(1095);
					JSONObject jsonReturn = new JSONObject();
					jsonReturn.put("statusMessage", "Merchant Not Found For Transaction"); 
					jsonReturn.put("status", 906);
					jsonReturn.put("carrierPayRef", mobilePaymentRefCode);
					jsonReturn.put("carrier", carrier);
					jsonReturn.put("transactionRef", transaction.getTransactionRef()); 
					jsonReturn.put("transactionTime", transaction.getCreated_at()); 
					jsonReturn.put("orderId", transaction.getOrderRef());
					return jsonReturn;
				}
			}
			else
			{
				System.out.println(1096);
				JSONObject jsonReturn = new JSONObject();
				jsonReturn.put("carrierPayRef", mobilePaymentRefCode);
				jsonReturn.put("carrier", carrier);
				jsonReturn.put("statusMessage", "Transaction Not Found"); 
				jsonReturn.put("status", 906);
				jsonReturn.put("transactionRef", transactionRef);
				return jsonReturn;
			}
			
			
		}catch(Exception e)
		{
			System.out.println(1097);
			e.printStackTrace();
			log.warn(e);
			JSONObject jsonReturn = new JSONObject();
			jsonReturn.put("statusmessage", "General System Error"); 
			jsonReturn.put("status", 99);
			jsonReturn.put("carrierPayRef", mobilePaymentRefCode);
			jsonReturn.put("carrier", carrier);
			return jsonReturn;
		}
	}
	
	
	@POST
	@Path("/requestReverseTransaction")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject requestReverseTransaction(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("reversalRequestId") String reversalRequestId, @FormParam("reverseTransactionRef") String reverseTransactionRef, @FormParam("orderId") String orderId, 
			@FormParam("amount") Double amount, @FormParam("description") String description, @FormParam("merchantCode") String merchantCode, 
			@FormParam("deviceCode") String deviceCode, @FormParam("hash") String hash)
	{
		//reverseTransactionRef = transactionRef."-".deviceCode Encrypted
		System.out.println("reversalRequestId = " + reversalRequestId);
		System.out.println("reverseTransactionRef = " + reverseTransactionRef);
		System.out.println("orderId = " + orderId);
		System.out.println("amount = " + amount);
		System.out.println("description = " + description);
		System.out.println("merchantCode = " + merchantCode);
		System.out.println("deviceCode = " + deviceCode);
		System.out.println("hash = " + hash);
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			String transRef = reverseTransactionRef;
			System.out.println("transId = " + transRef);
			System.out.println("deviceCode = " + deviceCode);
			
			String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" +merchantCode+ "' AND tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
			System.out.println(hql);
			Device device = (Device)(this.swpService.getUniqueRecordByHQL(hql));
			
			if(device==null)
			{
				//return invalid device
				jsonObject.put("status", ERROR.INVALID_DEVICE_DETAIL);
				jsonObject.put("message", "Invalid device details provided");
				
				jsonObject.put("reversalRequestId", reversalRequestId);
				jsonObject.put("reverseTransactionRef", reverseTransactionRef);
				jsonObject.put("orderId", orderId);
				jsonObject.put("merchantCode", merchantCode);
				jsonObject.put("message", "Invalid device details provided");
			}
			else
			{
				hql = "Select tp from Merchant tp where tp.merchantCode = '" +merchantCode+ "' AND tp.status = " + MerchantStatus.ACTIVE.ordinal();
				System.out.println(hql);
				Merchant merchant = (Merchant)(this.swpService.getUniqueRecordByHQL(hql));
				
				if(UtilityHelper.validateTransactionHash(
						hash, 
						merchantCode,
						deviceCode,
						Application.PROBASEPAY_SERVICE_TYPE_ID_REVERSAL,
						orderId,
						amount,
						device.getSuccessUrl(),
						merchant.getApiKey())==true)
				{
					hql = "Select tp from Transaction tp where tp.transactionRef = '" + transRef + "' AND tp.orderRef = '" +orderId+ "'";
					//+ " AND tp.device.id = "+ deviceId;
					System.out.println(hql);
					Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
					if(transaction!=null && transaction.getStatus().equals(TransactionStatus.SUCCESS) && transaction.getDevice().getDeviceCode().equals(device.getDeviceCode()) && amount<=transaction.getAmount())
					{
						RequestTransactionReversal rtr = new RequestTransactionReversal();
						rtr.setAmount(amount);
						rtr.setDescription(description);
						rtr.setMerchantCode(merchant.getMerchantCode());
						rtr.setDeviceCode(device.getDeviceCode());
						rtr.setMerchantId(merchant.getId());
						rtr.setMerchantName(merchant.getMerchantName());
						rtr.setOrderId(orderId);
						rtr.setRequestId(reversalRequestId);
						rtr.setReverseTransactionRef(reverseTransactionRef);
						rtr.setStatus(TransactionStatus.PENDING);
						rtr.setCreated_at(new Date());
						rtr.setUpdated_at(new Date());
						rtr.setTransaction(transaction);
						System.out.println("2-->" + hql);
						rtr = (RequestTransactionReversal)this.swpService.createNewRecord(rtr);
						jsonObject.put("reversalRequestId", reversalRequestId);
						jsonObject.put("status", ERROR.GENERAL_OK);
						jsonObject.put("transaction", new Gson().toJson(transaction));
						jsonObject.put("reverseTransaction", new Gson().toJson(rtr));
						jsonObject.put("reverseTransactionRef", reverseTransactionRef);
						jsonObject.put("orderId", orderId);
						jsonObject.put("merchantCode", merchantCode);
						jsonObject.put("message", "Transaction #" + transaction.getTransactionRef() + " has been logged in successfully for reversal");
					}
					else
					{
						//Invalid transaction. Device Code or transaction details mismatch
						jsonObject.put("reversalRequestId", reversalRequestId);
						jsonObject.put("reverseTransactionRef", reverseTransactionRef);
						jsonObject.put("orderId", orderId);
						jsonObject.put("merchantCode", merchantCode);
						jsonObject.put("status", ERROR.INVALID_TRANSACTION);
						jsonObject.put("message", "Invalid transaction. Device Code, Transaction Details Mismatch");
					}
				}
				else
				{
					//return invalid hash
					jsonObject.put("reversalRequestId", reversalRequestId);
					jsonObject.put("reverseTransactionRef", reverseTransactionRef);
					jsonObject.put("orderId", orderId);
					jsonObject.put("merchantCode", merchantCode);
					jsonObject.put("status", ERROR.HASH_FAIL);
					jsonObject.put("message", "Invalid Hash provided");
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		
		return jsonObject;
	}

	
	@GET
	@Path("/listReversalRequests")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listReversalRequests(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		Merchant merchant = null;
		try {
			this.swpService = serviceLocator.getSwpService();
			Collection<RequestTransactionReversal> rtrList = null;
			
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
			
			String hql = "Select tp from RequestTransactionReversal tp";
			rtrList = (Collection<RequestTransactionReversal>)swpService.getAllRecordsByHQL(hql);
			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Transaction Reversal list fetched");
			jsonObject.put("devicelist", new Gson().toJson(new ArrayList<RequestTransactionReversal>(rtrList)));
			return jsonObject;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.DEVICE_LIST_FETCH_FAIL);
				jsonObject.put("message", "Transaction Reversal list fetch Failed");
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
				log.warn(e);
			}
			return jsonObject;
		}
		
	}
	
	@GET
	@Path("/getRequestReverseTransactionStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getRequestReverseTransactionStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("requestUniqueId") String requestUniqueId, @QueryParam("hash") String hash, @QueryParam("merchantCode") String merchantCode, 
			@QueryParam("deviceCode") String deviceCode, @QueryParam("amount") Double amount)
	{
		System.out.println("requestUniqueId = " + requestUniqueId);
		System.out.println("hash = " + hash);
		System.out.println("amount = " + amount);
		System.out.println("merchantCode = " + merchantCode);
		System.out.println("deviceCode = " + deviceCode);
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			
			String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" +merchantCode+ "' AND tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
			System.out.println(hql);
			Device device = (Device)(this.swpService.getUniqueRecordByHQL(hql));
			
			if(device==null)
			{
				//return invalid device
				jsonObject.put("status", ERROR.INVALID_DEVICE_DETAIL);
				jsonObject.put("message", "Invalid device details provided");
				
				jsonObject.put("requestUniqueId", requestUniqueId);
				jsonObject.put("deviceCode", deviceCode);
				jsonObject.put("amount", amount);
				jsonObject.put("merchantCode", merchantCode);
				jsonObject.put("message", "Invalid device details provided");
			}
			else
			{
				hql = "Select tp from Merchant tp where tp.merchantCode = '" +merchantCode+ "' AND tp.status = " + MerchantStatus.ACTIVE.ordinal();
				System.out.println(hql);
				Merchant merchant = (Merchant)(this.swpService.getUniqueRecordByHQL(hql));
				
				if(UtilityHelper.validateTransactionHash(
						hash, 
						merchantCode,
						deviceCode,
						Application.PROBASEPAY_SERVICE_TYPE_ID_REVERSAL,
						requestUniqueId,
						amount,
						device.getSuccessUrl(),
						merchant.getApiKey())==true)
				{
					hql = "Select tp from RequestTransactionReversal tp where tp.requestId = '" + requestUniqueId + "' AND tp.merchantCode = '" +merchantCode+ "' " +
							"AND tp.deviceCode = '" +deviceCode+ "' AND tp.amount = " + amount + " AND tp.status = " + TransactionStatus.SUCCESS.ordinal();
					//+ " AND tp.device.id = "+ deviceId;
					System.out.println(hql);
					RequestTransactionReversal rtr = (RequestTransactionReversal)this.swpService.getUniqueRecordByHQL(hql);
					if(rtr!=null)
					{
						
						jsonObject.put("status", ERROR.GENERAL_OK);
						jsonObject.put("message", "Invalid device details provided");
						jsonObject.put("reversalStatus", rtr.getStatus().name());
						jsonObject.put("requestUniqueId", requestUniqueId);
						jsonObject.put("deviceCode", deviceCode);
						jsonObject.put("amount", amount);
						jsonObject.put("merchantCode", merchantCode);
						jsonObject.put("message", "Invalid device details provided");
					}
					else
					{
						//Invalid transaction. Device Code or transaction details mismatch
						jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
						jsonObject.put("message", "Invalid device details provided");
						
						jsonObject.put("requestUniqueId", requestUniqueId);
						jsonObject.put("deviceCode", deviceCode);
						jsonObject.put("amount", amount);
						jsonObject.put("merchantCode", merchantCode);
						jsonObject.put("message", "Invalid device details provided");
					}
				}
				else
				{
					//return invalid hash
					jsonObject.put("status", ERROR.HASH_FAIL);
					jsonObject.put("message", "Invalid device details provided");
					
					jsonObject.put("requestUniqueId", requestUniqueId);
					jsonObject.put("deviceCode", deviceCode);
					jsonObject.put("amount", amount);
					jsonObject.put("merchantCode", merchantCode);
					jsonObject.put("message", "Invalid device details provided");
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		
		return jsonObject;
	}
	
	
	
	
	
	
	
	@GET
	@Path("/getMerchantStatementFromBank")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMerchantStatement(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("deviceCode") String deviceCode, @QueryParam("startPeriod") String startPeriod, @QueryParam("endPeriod") String endPeriod, 
			@QueryParam("acquirerCode") String acquirerCode)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		String req = deviceCode + "-|||-" + startPeriod + "-|||-" + endPeriod;
		System.out.println("Request ==>" + req);
		
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
			
			if(deviceCode!=null)
			{
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
				System.out.println(hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	System.out.println("device not found");
			    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "We could not find a device mapped to the device code");
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			}
		    
			
			
		    
			if(bank!=null && (bank.getBankCode().equals(Application.ZICB_BANK_CODE) || bank.getBankCode().equals(Application.UAT_ZICB_BANK_CODE)))
			{
				System.out.println("bank found");
				parametersRequest.put("fromDate", startPeriod);
				parametersRequest.put("toDate", endPeriod);
				JSONObject header = new JSONObject();
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					header.put("authKey", device.getZicbAuthKey());
				}
				else
				{
					header.put("authKey", device.getZicbDemoAuthKey());
				}
				parameters.put("request", parametersRequest);
				parameters.put("service", "ZB0632");
				header.put("Content-Type", "application/json; utf-8");
				header.put("Accept", "application/json");
				
				System.out.println(header.toString());
				System.out.println(parameters.toString());
				
				String statementDetails = null;
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					System.out.println(">>>>");
					statementDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryEndPoint(), parameters.toString(), header);
				}
				else
				{
					System.out.println("<<<<");
					statementDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryDemoEndPoint(), parameters.toString(), header);
				}
				
				JSONObject walletDetailsJS = new JSONObject(statementDetails);
				Integer status = walletDetailsJS.getInt("status");
				//Integer status = 200;
				if(status==200)
				{
					
					JSONObject response = walletDetailsJS.getJSONObject("response");
					JSONArray transactions = response.getJSONArray("transactions");
					/*hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
					Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
					JSONArray accountList = new JSONArray();
					JSONObject acc = new JSONObject();
					acc.put("currentbalance", acct.getAccountBalance());
					acc.put("availablebalance", acct.getAccountBalance());
					accountList.put(acc);*/

					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Wallet found");
					jsonObject.put("transactions", transactions.toString());
					
					
					//hql = "Select tp from DeviceBankAccount tp where tp.device.id = " + device.getId() + " AND tp.status = 1";
					//DeviceBankAccount deviceBankAccount = (DeviceBankAccount)swpService.getAllRecordsByHQL(hql);
					
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	
	@GET
	@Path("/getMakeDeviceSettlement")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMakeDeviceSettlement(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("deviceCode") String deviceCode, 
			@QueryParam("acquirerCode") String acquirerCode)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		String req = deviceCode + "-|||-" + acquirerCode;
		System.out.println("Request ==>" + req);
		
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
			
			if(deviceCode!=null)
			{
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
				System.out.println(hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	System.out.println("device not found");
			    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "We could not find a device mapped to the device code");
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			}
		    
			hql = "Select tp from Settlement tp where tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL ORDER BY tp.created_at DESC";
			Collection<Settlement> allSettlements = (Collection<Settlement>)swpService.getAllRecordsByHQL(hql);
			String startPeriod = "";
			String endPeriod = "";
			
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(allSettlements.size()>0)
			{
				Settlement lastSettlement = allSettlements.iterator().next();
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DATE, -1);
				Date dateBeforeToday = cal.getTime();
				endPeriod = sdf.format(dateBeforeToday);
				
				Date lastEndDate = lastSettlement.getEndDate();
				cal = Calendar.getInstance();
				cal.setTime(lastEndDate);
				cal.add(Calendar.DATE, +1);
				Date newStartDate = cal.getTime();
				startPeriod = sdf.format(newStartDate);
			}
			else
			{
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DATE, -1);
				Date dateBeforeToday = cal.getTime();
				startPeriod = sdf.format(device.getCreated_at());
				endPeriod = sdf.format(dateBeforeToday);
			}
			
			
			
			
			
		    
			if(bank!=null && (bank.getBankCode().equals(Application.ZICB_BANK_CODE) || bank.getBankCode().equals(Application.UAT_ZICB_BANK_CODE)))
			{
				System.out.println("bank found");
				parametersRequest.put("fromDate", startPeriod);
				parametersRequest.put("toDate", endPeriod);
				JSONObject header = new JSONObject();
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					header.put("authKey", device.getZicbAuthKey());
				}
				else
				{
					header.put("authKey", device.getZicbDemoAuthKey());
				}
				parameters.put("request", parametersRequest);
				parameters.put("service", "ZB0632");
				header.put("Content-Type", "application/json; utf-8");
				header.put("Accept", "application/json");
				
				System.out.println(header.toString());
				System.out.println(parameters.toString());
				
				String statementDetails = null;
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					System.out.println(">>>>");
					statementDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryEndPoint(), parameters.toString(), header);
				}
				else
				{
					System.out.println("<<<<");
					statementDetails = UtilityHelper.sendPost(acquirer.getBalanceInquiryDemoEndPoint(), parameters.toString(), header);
				}
				
				JSONObject walletDetailsJS = new JSONObject(statementDetails);
				Integer status = walletDetailsJS.getInt("status");
				//Integer status = 200;
				String htmlTable = "";
				if(status==200)
				{
					
					JSONObject response = walletDetailsJS.getJSONObject("response");
					JSONArray transactions = response.getJSONArray("transactions");
					/*hql = "Select tp from Account tp where tp.accountIdentifier = '"+ bankAccountNo +"'";
					Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
					JSONArray accountList = new JSONArray();
					JSONObject acc = new JSONObject();
					acc.put("currentbalance", acct.getAccountBalance());
					acc.put("availablebalance", acct.getAccountBalance());
					accountList.put(acc);*/
					Double totalAmount = 0.00;
					JSONObject jsAmounts = new JSONObject();
					JSONArray jsAllAmounts = new JSONArray();

					if(transactions.length()==0)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "No transactions currently available for settlement");
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					
					JSONObject allTxns = new JSONObject();
					for(int i=0; i<transactions.length(); i++)
					{
						String creditOrDebit = transactions.getJSONObject(i).getString("debitOrCredit");
						if(creditOrDebit.equals("C"))
						{
							Double amount = transactions.getJSONObject(i).getDouble("amount");
							String currency = transactions.getJSONObject(i).getString("txncurrency");
							if(jsAmounts.has(currency))
							{
								Double totalCurrencyAmount = jsAmounts.getDouble(transactions.getJSONObject(i).getString("txncurrency"));
								totalCurrencyAmount = totalCurrencyAmount + amount; 
								jsAmounts.put(transactions.getJSONObject(i).getString("txncurrency"), totalCurrencyAmount);
								JSONArray allTxns_ = allTxns.getJSONArray(currency);
								allTxns_.put(transactions.getJSONObject(i));
								allTxns.put(currency, allTxns_);
							}
							else
							{
								jsAmounts.put(transactions.getJSONObject(i).getString("txncurrency"), amount);
								JSONArray allTxns_ = new JSONArray();
								allTxns_.put(transactions.getJSONObject(i));
								allTxns.put(currency, allTxns_);
							}
							jsAllAmounts.put(transactions);
							
							
							
							
							
						}
					}
					
					
					htmlTable = htmlTable + 
						"<table class='table table-condensed'>"
							+ "<thead>"
								+ "<tr>"
									+ "<th style='background-color: #000; color: #fff; vertical-align:bottom !important;'><strong>Payments for the Period - (" + startPeriod + " to " + endPeriod + ")</strong></th>"
									+ "<th style='text-align:right; background-color: #000; color: #fff; vertical-align:bottom !important;'><strong>Amount</strong></th>"
								+ "</tr>"
							+ "</thead>"
							+ "<tbody>";
							
					
					Iterator<String> jsAmountsIter = allTxns.keys();	//ZMW, NGN, RND
					while(jsAmountsIter.hasNext())
					{
						String currency = jsAmountsIter.next();		//ZMW
						htmlTable = htmlTable 
						+ "<tr>"
							+ "<th style='width:50px; background-color: #555; color: #fff;'>&nbsp;</th>"
							+ "<th style='width:50px; background-color: #555; color: #fff;'><strong>"+ currency+"</strong></th>"
						+ "</tr>";
						
						JSONArray currencyEntries = allTxns.getJSONArray(currency);		//[{"amount": 33, "debitOrCred": "C"}, {"amount": 33, "debitOrCred": "C"}]
						Double total = 0.00;
						for(int i4=0; i4<currencyEntries.length(); i4++)
						{
							JSONObject trxnEntry = currencyEntries.getJSONObject(i4);
							htmlTable = htmlTable + "<tr>"
								+ "<td style='border-top: 1px solid #f4f4f4;'>"+ trxnEntry.getString("remarks") +"</td>"
								+ "<td style='border-top: 1px solid #f4f4f4; text-align:right'>"+ (trxnEntry.getDouble("amount")) +"</td>"
							+ "</tr>";
							total = total + trxnEntry.getDouble("amount");
						}
						htmlTable = htmlTable + "<tr>"
							+ "<td style='background-color: #FF0000; color: #fff' class='text-left' colspan='1'><strong>Total Amount</strong></td>"
							+ "<td style='text-align:right; background-color: #FF0000; color: #fff'>"
								+ "<strong id='amount_to_pay_changer'>" + (total) +  "</strong>"
							+ "</td>"
						+ "</tr>";
					}
					htmlTable = htmlTable + "</table>";
					

					/*SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
					String fileName = "/opt/tomcat-9.0.41.0/settlementpdfs/string-output-"+ (sdf2.format(new Date())) +".pdf";
					OutputStream fileOutputStream = new FileOutputStream(fileName);
					PdfDocument pdfDocument = new PdfDocument(new PdfWriter(fileOutputStream));
				    pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
				    ConverterProperties properties = new ConverterProperties();
				    HtmlConverter.convertToPdf(htmlTable, pdfDocument, properties);
					//HtmlConverter.convertToPdf(htmlTable, fileOutputStream);
					String[] attachmentFiles = new String[1];
					attachmentFiles[0] = fileName;
					//new Thread(new SmsSender(swpService, htmlTable, "smicer66@gmail.com", "test", attachmentFiles)).start();*/
					new Thread(new SmsSender(swpService, htmlTable, "smicer66@gmail.com", "Payments for the Period - (" + startPeriod + " to " + endPeriod + ")", htmlTable)).start();
					
					hql = "Select tp from DeviceBankAccount tp where tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND tp.status = 1";
					Collection<DeviceBankAccount> deviceBankAccounts = (Collection<DeviceBankAccount>)swpService.getAllRecordsByHQL(hql);
					System.out.println("deviceBankAccounts..." + deviceBankAccounts.size());
					
					if(deviceBankAccounts.size()==0)
					{
						jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
						jsonObject.put("message", "No settlement account found. Settlement can only occur when a settlement account has been provided");
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					
					DeviceBankAccount deviceBankAccount = deviceBankAccounts.iterator().next();
					
					Iterator<String> jsAmountKeys = jsAmounts.keys();
					JSONArray settlementList = new JSONArray();
					while(jsAmountKeys.hasNext())
					{
						String amtKey = jsAmountKeys.next();
						Double amt = jsAmounts.getDouble(amtKey);
						Settlement settlement = new Settlement(
							
							deviceBankAccount.getSettlementBankAccountNumber(), 
							deviceBankAccount.getSettlementBankName(),
							deviceBankAccount.getSettlementBankAccountName(),
							deviceBankAccount.getSettlementAccountId(),
							sdf1.parse(startPeriod + " 00:00:00"), 
							sdf1.parse(endPeriod + " 23:59:59"), 
							new BigDecimal(amt.toString()).setScale(2,RoundingMode.HALF_UP).doubleValue(),
							device.getId(),
							device.getMerchant().getId(),
							deviceBankAccount.getTransientAccountNumber(),
							deviceBankAccount.getTransientAccountId(),
							amtKey
						);
						settlement = (Settlement)swpService.createNewRecord(settlement);
						settlementList.put(settlement);
					}
					

					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("settlementList", new Gson().toJson(settlementList));
					jsonObject.put("transactions", jsAllAmounts);
					jsonObject.put("endPeriod", endPeriod);
					jsonObject.put("startPeriod", startPeriod);
					
					
					
					
					
					
					
					//hql = "Select tp from DeviceBankAccount tp where tp.device.id = " + device.getId() + " AND tp.status = 1";
					//DeviceBankAccount deviceBankAccount = (DeviceBankAccount)swpService.getAllRecordsByHQL(hql);
					
					//System.out.println("resp....1.." + jsonObject.toString());
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "We could not obtain details about this wallet");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "We could not obtain details about this wallet");
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	public void sendEmailTest() throws FileNotFoundException
	{
		String htmlTable = "";
		htmlTable = htmlTable + 
		"<table class='table table-condensed'>"
			+ "<thead>"
				+ "<tr>"
					+ "<th style='background-color: #000; color: #fff; vertical-align:bottom !important;'><strong>Payment for the Period -</strong></th>"
					+ "<th style='text-align:right; background-color: #000; color: #fff; vertical-align:bottom !important;'><strong>Amount</strong></th>"
				+ "</tr>"
			+ "</thead>"
			+ "<tbody>";
		htmlTable = htmlTable 
			+ "<tr>"
				+ "<th style='width:50px; background-color: #555; color: #fff;'>&nbsp;</th>"
				+ "<th style='width:50px; background-color: #555; color: #fff;'><strong>ZMW</strong></th>"
			+ "</tr>";
		htmlTable = htmlTable + "</table>";
		/*SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = "/opt/tomcat-9.0.41.0/settlementpdfs/string-output-"+ (sdf2.format(new Date())) +".pdf";
		fileName = "C:\\Users\\user\\Downloads\\zipake_terms_conditions.pdf";
		OutputStream fileOutputStream = new FileOutputStream(fileName);
		PdfDocument pdfDocument = new PdfDocument(new PdfWriter(fileOutputStream));
	    pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
	    ConverterProperties properties = new ConverterProperties();
	    HtmlConverter.convertToPdf(htmlTable, pdfDocument, properties);
		//HtmlConverter.convertToPdf(htmlTable, fileOutputStream);
		String[] attachmentFiles = new String[1];
		attachmentFiles[0] = fileName;*/
		new Thread(new SmsSender(swpService, htmlTable, "smicer66@gmail.com", "test", htmlTable)).start();
	}
	
	
	
	@GET
	@Path("/getDeviceSettlementList")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDeviceSettlementList(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("deviceCode") String deviceCode, 
			@QueryParam("acquirerCode") String acquirerCode)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		String req = deviceCode + "-|||-" + acquirerCode;
		System.out.println("Request ==>" + req);
		
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
			
			if(deviceCode!=null)
			{
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
				System.out.println(hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	System.out.println("device not found");
			    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "We could not find a device mapped to the device code");
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			}
		    
			hql = "Select tp from Settlement tp where tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL ORDER BY tp.created_at DESC";
			Collection<Settlement> allSettlements = (Collection<Settlement>)swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("settlementList", new Gson().toJson(allSettlements));
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	

}
