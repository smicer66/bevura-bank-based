package com.probase.probasepay.services;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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



import antlr.StringUtils;

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
import com.probase.probasepay.enumerations.ProbasePayCurrency;
//import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.VendorStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.enumerations.WalletType;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.BevuraToken;
import com.probase.probasepay.models.BillingAddress;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.DeviceBankAccount;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantBankAccount;
import com.probase.probasepay.models.MerchantPayment;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.SMSMesage;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.models.UtilityPurchased;
import com.probase.probasepay.models.WalletTransaction;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/PaymentServicesV2")
public class PaymentServicesV2 {
	private static Logger log = Logger.getLogger(PaymentServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/**Service Method - Initiate a Payment 
	 * Generate an OTP for the transaction
	 * Make an entry for the transaction
	 * Transaction to be completed when OTP
	 * is valid
	 * 1. Handled Custom Data
	 * */
	@POST
	@Path("/generateOTPForTransaction")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateOTPForTransaction(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("transactionObject") String transactionObject, 
			@FormParam("channel") String channelType, 
			@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
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
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String transactionInitiator = new String(Base64.decode(transactionObject));
			System.out.println("transactionInitiator=> " + transactionInitiator);
			JSONObject jsTxn = new JSONObject(transactionInitiator);
			String txnDetail = jsTxn.getString("txnDetail");
			System.out.println("acquirerCode=> " + acquirerCode);
			System.out.println("txnDetail=> " + txnDetail);
			System.out.println("bankKey=> " + bankKey);
			
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
				
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
			
			if(txnDetail!=null)
			{
				String cardObjectString = (String)UtilityHelper.decryptData(txnDetail, bankKey);
				System.out.println("cardObjectString=> " + cardObjectString);
				JSONObject ecardJson = new JSONObject(cardObjectString);
				System.out.println("ecardJson=> " + ecardJson.toString());
				String cardSerialNo  = ecardJson.has("cardSerialNo") ? ecardJson.getString("cardSerialNo") : null;
				System.out.println("cardSerialNo = " + cardSerialNo );
				String[] cardSerialNos = cardSerialNo.split("~~~");
				String cardTrackingNo  = ecardJson.has("cardTrackingNo") ? ecardJson.getString("cardTrackingNo") : null;
				System.out.println("cardTrackingNo = " + cardTrackingNo );
				String[] cardTrackingNos = cardTrackingNo.split("~~~");
				String wallettoken  = ecardJson.has("wallettoken") ? ecardJson.getString("wallettoken") : null;
				String cvv1  = ecardJson.has("cvv") ? ecardJson.getString("cvv") : null;
				String[] cvvs = cvv1.split("~~~");
				String expiryDate_  = ecardJson.has("expiryDate") ? ecardJson.getString("expiryDate") : null;
				String[] expiryDates = expiryDate_.split("~~~");
				String payeeFirstName  = ecardJson.getString("payeeFirstName");
				String payeeEmail  = ecardJson.getString("payeeEmail");
				String payeeMobile  = ecardJson.getString("payeeMobile");
				Double amount  = ecardJson.getDouble("amount");
				String responseUrl  = ecardJson.getString("responseUrl");
				String orderId  = ecardJson.getString("orderId");
				String hash  = ecardJson.getString("hash");
				String merchantId  = ecardJson.getString("merchantId");
				String deviceCode  = ecardJson.getString("deviceCode");
				String serviceTypeId  = ecardJson.getString("serviceTypeId");
				String currency = ecardJson.has("currency") ? ecardJson.getString("currency") : null;
				String customdata = ecardJson.has("customdata") ? ecardJson.getString("customdata") : null;
				
				/*Billing*/
				String billingPayeeFirstName  = ecardJson.getString("firstName"); 
				String billingPayeeLastName  = ecardJson.getString("lastName");
				String billPayeeMobile  = ecardJson.getString("countryCode") + "" + ecardJson.getString("billPayeeMobile");
				String billingPayeeEmail  = ecardJson.getString("email"); 
				String billingStreetAddress = ecardJson.getString("streetAddress");
				String billingCity = ecardJson.getString("city");
				String billingDistrict = ecardJson.getString("district");
				/*billing ends*/
				
				ProbasePayCurrency probaseCurrency = null;
				try
				{
					probaseCurrency = ProbasePayCurrency.valueOf(currency);
				}
				catch(IllegalArgumentException e)
				{

					jsonObject.put("status", ERROR.INVALID_CURRENCY_PROVIDED);
					jsonObject.put("message", "Invalid currency provided");
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
			    
			    hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device!=null)
			    {
			    	merchant = device.getMerchant();
			    	String deviceResponseUrl = device.getSuccessUrl();
			    	if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
			    	{
			    		String api_key = merchant.getApiKey();
						if(UtilityHelper.validateTransactionHash(
								hash, 
								merchantId,
								deviceCode,
								serviceTypeId,
								orderId,
								amount,
								deviceResponseUrl,
								api_key)==true)
						{
							
							if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
							{
								Double readjustedAmount = amount;
								Boolean amountCheck = false;
								int i1 = 0;
								JSONObject errorMessages = new JSONObject();
								JSONArray cardsVerified = new JSONArray();
								
								if(cardSerialNos.length==0)
								{
									jsonObject.put("status", ERROR.CARDS_NOT_PROVIDED);
									jsonObject.put("message", "You do not provide any valid cards for payment");
								}
								else if(cardSerialNos.length>1)
								{
									while(amountCheck==false && i1<cardSerialNos.length)
									{
										System.out.println(i1 + " -- " + cardSerialNos.length);
										if(amountCheck==false)
										{
											System.out.println("amount check is false");
										}
										String sn = cardSerialNos[i1];
										String tn = cardTrackingNos[i1];
										String cvv = cvvs[i1];
										String expiryDate = expiryDates[i1];
										hql = "Select tp from ECard tp where tp.serialNo = '" + sn + "' AND tp.trackingNumber = '"+ tn +"' AND tp.deleted_at IS NULL "
												+ "AND tp.isLive = " + device.getSwitchToLive();
										System.out.println(hql);
										ECard ecard = (ECard)this.swpService.getUniqueRecordByHQL(hql);
										
										if(ecard!=null)
										{
											if(ecard.getCvv().equals(cvv))
											{
												expiryDate = expiryDate + "/01 23:59:59";
												System.out.println("expiryDate=> " + expiryDate);
												SimpleDateFormat sdf= new SimpleDateFormat("MM/yy/dd H:m:s");
												Date expDate = sdf.parse(expiryDate);
												
												Date todayDate = new Date();
												
												Calendar cal = Calendar.getInstance();
												cal.setTime(expDate);
												System.out.println("Exp Date=>" + "Month=" + cal.get(Calendar.MONTH) + " && Year = " + cal.get(Calendar.YEAR) + " && Day = " + cal.get(Calendar.DAY_OF_MONTH));
												//GregorianCalendar gc = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, 1);
												//java.util.Date monthEndDate = new java.util.Date(gc.getTime().getTime());
												//cal.setTime(monthEndDate);
												cal.add(Calendar.MONTH, 1);
												cal.add(Calendar.DAY_OF_MONTH, -1);
												Date monthEndDate = cal.getTime();
												String formDate = sdf.format(monthEndDate);
											    System.out.println(">>><<< " + sdf.format(monthEndDate));
											    cal.clear();
											    monthEndDate = sdf.parse(formDate);
											    
												Date cardExp = ecard.getExpiryDate();
												Calendar cal1 =Calendar.getInstance();
												cal1.setTime(cardExp);
												Calendar cal2 =Calendar.getInstance();
												cal2.setTime(monthEndDate);
												
												System.out.println(">>>>" + cal1.get(Calendar.MONTH) + "==" + cal2.get(Calendar.MONTH));
												System.out.println(">>>>" + cal1.get(Calendar.YEAR) + "==" + cal2.get(Calendar.YEAR));
												System.out.println(">>>>" + cardExp + " && " + monthEndDate);
												
												
												if(cal1.get(Calendar.MONTH)==cal2.get(Calendar.MONTH) && cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR))
												{
													if(monthEndDate.after(todayDate))
													{
														Double balance = ecard.getCardBalance();
														
														if(balance - ecard.getCardScheme().getMinimumBalance() >= readjustedAmount)
														{
															amountCheck = true;
															cardsVerified.put(ecard);
															readjustedAmount = 0.00;
														}
														else
														{
															readjustedAmount = readjustedAmount - balance;
														}
													}
													else
													{
														errorMessages.put(sn, "Expired Card Used");
													}
												}
												else
												{
													errorMessages.put(sn, "Invalid Expiry Date Provided");
												}
											}
											else
											{
												errorMessages.put(sn, "Invalid CVV Provided");
											}
							    		}
										else
										{
											errorMessages.put(sn, "Invalid Card Details Provided");
										}
										i1++;
									}
									
									if(readjustedAmount==0.00)
									{
										String oTP = RandomStringUtils.randomNumeric(4);
										oTP = "1111";
										String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
										String bankPaymentReference = null;
										Boolean creditAccountTrue = false;
										Boolean creditCardTrue = false;
										String orderRef = orderId;
										String rpin = null;
										Date transactionDate = new Date();
										ServiceType serviceType = ServiceType.DEBIT_CARD;
										
										Channel channel = null;
										if(channelType==null)
										{
											channel = Channel.WEB;
											if(device.getDeviceType().equals(DeviceType.ATM))
												channel = Channel.ATM;
											if(device.getDeviceType().equals(DeviceType.MPQR))
												channel = Channel.POS;
											if(device.getDeviceType().equals(DeviceType.POS))
												channel = Channel.POS;
											if(device.getDeviceType().equals(DeviceType.WEB))
												channel = Channel.WEB;
										}
										else
										{
											try
											{
												channel = Channel.valueOf(channelType);
											}
											catch(Exception e)
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "Invalid Channel Provided");
												jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
												return Response.status(200).entity(jsonObject.toString()).build();
											}
										}
										TransactionStatus status = TransactionStatus.PENDING;
										String transactionCode = TransactionCode.transactionPending;
										Boolean creditPoolAccountTrue = false;
										String messageRequest = transactionObject;
										String messageResponse = null;
										String contactMobile = null;
										
										
										readjustedAmount = amount;
										for(int i2=0; i2<cardsVerified.length(); i2++)
										{
											ECard ecardVerified = (ECard)cardsVerified.get(i2);
											Long customerId = ecardVerified.getCustomerId();
											String payerName = ecardVerified.getNameOnCard();
											String payerEmail = ecardVerified.getAccount().getCustomer().getContactEmail();
											String payerMobile = ecardVerified.getAccount().getCustomer().getContactMobile();
											Account account = ecardVerified.getAccount();
											Double fixedCharge = ecardVerified.getCardScheme().getOverrideFixedFee();
											Double transactionCharge = ecardVerified.getCardScheme().getOverrideTransactionFee() * amount / 100;
											Double transactionPercentage = ecardVerified.getCardScheme().getOverrideTransactionFee();
											Double schemeTransactionCharge = ecardVerified.getCardScheme().getOverrideTransactionFee() * amount / 100;
											Double schemeTransactionPercentage = ecardVerified.getCardScheme().getOverrideTransactionFee();
											Double debitAmount = null;
											Double balance = ecardVerified.getCardBalance();
											String responseCode = null;
											String oTPRef = null;
											String merchantName = merchant.getMerchantName();
											String merchantBank = null;
											String merchantAccount = null;
											Long transactingBankId = null;
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = ecardVerified.getId();
											Long receipientEntityId = merchant.getId();
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = tokenUser.getId();
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = acquirer.getId();
											Long merchantIdL = merchant.getId();
											if(i2==0)
											{
												contactMobile = payerMobile;
											}
											
											
											
											if(balance - ecardVerified.getCardScheme().getMinimumBalance() >= readjustedAmount)
											{
												debitAmount = readjustedAmount;
												readjustedAmount = readjustedAmount - readjustedAmount;
											}
											else
											{
												debitAmount = balance;
												readjustedAmount = readjustedAmount - balance;
											}
											String transactionDetail = "DR CARD|" + ecardVerified.getSerialNo() + "|" + debitAmount + "|" + orderRef + 
													"|" + amount;
											
											
											
											hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
													+ "tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
											List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
											Boolean debitAccountTrue = true;
											Boolean debitCardTrue = false;
											Long creditAccountId = null;
											Long creditCardId = null;
											Long debitAccountId = null;
											Long debitCardId = null;
											if(devicebankaccounts!=null && devicebankaccounts.size()>0)
											{
												Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
												creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
												debitCardId = ecardVerified.getId();
											}
											//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
											
											
											Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
													customerId, creditAccountTrue, creditCardTrue,
													orderRef, rpin, channel,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probaseCurrency, transactionCode,
													account, ecardVerified, device,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													debitAmount, responseCode, oTP, oTPRef,
													merchantIdL, merchantName, merchantId,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													channel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customdata,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, null, 
													device.getSwitchToLive());
											this.swpService.createNewRecord(transaction);
										}
										
										
										
										
										
										jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
										jsonObject.put("message", "OTP generated succcessfully");
										jsonObject.put("otp", oTP);
										jsonObject.put("otpRec", contactMobile);
										jsonObject.put("orderRef", orderRef);
										jsonObject.put("transactionRef", transactionRef);
										
										if(customdata!=null)
											jsonObject.put("customData", customdata);
										jsonObject.put("errorMessages", errorMessages);
										
										

										String smsMessage = "Your One-Time Password to complete your transaction/payment is " + oTP;
										//SMSMesage smsMsg1 = new SMSMesage(contactMobile, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
										//swpService.createNewRecord(smsMsg1);


										SmsSender smsSender = new SmsSender(swpService, smsMessage, contactMobile);
										new Thread(smsSender).start();
									}
									else
									{
										jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
										jsonObject.put("message", "Payment was not successful.");
										jsonObject.put("errorMessages", errorMessages);
									}
								}
								else
								{
									String sn = cardSerialNos[i1];
									String tn = cardTrackingNos[i1];
									String expiryDate = expiryDates[i1];
									String cvv = cvvs[i1];
									hql = "Select tp from ECard tp where tp.serialNo = '" + sn + "' AND tp.trackingNumber = '"+ tn +"' AND tp.deleted_at IS NULL "
											+ "AND tp.isLive = " + device.getSwitchToLive();
									ECard ecard = (ECard)this.swpService.getUniqueRecordByHQL(hql);
									
									if(ecard!=null)
									{
										if(ecard.getCvv().equals(cvv))
										{
											expiryDate = expiryDate + "/01 23:59:59";
											System.out.println("expiryDate=> " + expiryDate);
											SimpleDateFormat sdf= new SimpleDateFormat("MM/yy/dd H:m:s");
											Date expDate = sdf.parse(expiryDate);
											
											Date todayDate = new Date();
											
											Calendar cal = Calendar.getInstance();
											cal.setTime(expDate);
											System.out.println("Exp Date=>" + "Month=" + cal.get(Calendar.MONTH) + " && Year = " + cal.get(Calendar.YEAR) + " && Day = " + cal.get(Calendar.DAY_OF_MONTH));
											//GregorianCalendar gc = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, 1);
											//java.util.Date monthEndDate = new java.util.Date(gc.getTime().getTime());
											//cal.setTime(monthEndDate);
											cal.add(Calendar.MONTH, 1);
											cal.add(Calendar.DAY_OF_MONTH, -1);
											Date monthEndDate = cal.getTime();
											String formDate = sdf.format(monthEndDate);
										    System.out.println(">>><<< " + sdf.format(monthEndDate));
										    cal.clear();
										    monthEndDate = sdf.parse(formDate);
										    
											Date cardExp = ecard.getExpiryDate();
											Calendar cal1 =Calendar.getInstance();
											cal1.setTime(cardExp);
											Calendar cal2 =Calendar.getInstance();
											cal2.setTime(monthEndDate);
											
											System.out.println(">>>>" + cal1.get(Calendar.MONTH) + "==" + cal2.get(Calendar.MONTH));
											System.out.println(">>>>" + cal1.get(Calendar.YEAR) + "==" + cal2.get(Calendar.YEAR));
											System.out.println(">>>>" + cardExp + " && " + monthEndDate);
											
											
											if(cal1.get(Calendar.MONTH)==cal2.get(Calendar.MONTH) && cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR))
											{
												if(monthEndDate.after(todayDate))
												{
													Double balance = ecard.getCardBalance();
													System.out.println(">>>>balance.." + balance);
													
													if(balance - ecard.getCardScheme().getMinimumBalance() > readjustedAmount)
													{
														System.out.println(">>>>amountCheck..true");
														amountCheck = true;
														cardsVerified.put(ecard);
														readjustedAmount = 0.00;
														
														
														String oTP = RandomStringUtils.randomNumeric(4);
														oTP = "1111";
														String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
														String bankPaymentReference = null;
														Boolean creditAccountTrue = false;
														Boolean creditCardTrue = false;
														String orderRef = orderId;
														String rpin = null;
														Date transactionDate = new Date();
														ServiceType serviceType = ServiceType.DEBIT_CARD;
														
														Channel channel = Channel.WEB;
														if(device.getDeviceType().equals(DeviceType.ATM))
															channel = Channel.ATM;
														if(device.getDeviceType().equals(DeviceType.MPQR))
															channel = Channel.POS;
														if(device.getDeviceType().equals(DeviceType.POS))
															channel = Channel.POS;
														if(device.getDeviceType().equals(DeviceType.WEB))
															channel = Channel.WEB;
														TransactionStatus status = TransactionStatus.PENDING;
														String transactionCode = TransactionCode.transactionPending;
														Boolean creditPoolAccountTrue = false;
														String messageRequest = transactionObject;
														String messageResponse = null;
														String contactMobile = null;
														
														
														readjustedAmount = amount;
														ECard ecardVerified = ecard;
														Long customerId = ecardVerified.getCustomerId();
														String payerName = ecardVerified.getNameOnCard();
														String payerEmail = ecardVerified.getAccount().getCustomer().getContactEmail();
														String payerMobile = ecardVerified.getAccount().getCustomer().getContactMobile();
														Account account = ecardVerified.getAccount();
														Double fixedCharge = ecardVerified.getCardScheme().getOverrideFixedFee();
														Double transactionCharge = ecardVerified.getCardScheme().getOverrideTransactionFee() * amount / 100;
														Double transactionPercentage = ecardVerified.getCardScheme().getOverrideTransactionFee();
														Double schemeTransactionCharge = ecardVerified.getCardScheme().getOverrideTransactionFee() * amount / 100;
														Double schemeTransactionPercentage = ecardVerified.getCardScheme().getOverrideTransactionFee();
														Double debitAmount = null;
														String responseCode = null;
														String oTPRef = null;
														String merchantName = merchant.getMerchantName();
														String merchantBank = null;
														String merchantAccount = null;
														Long transactingBankId = null;
														Long receipientTransactingBankId = null;
														Integer accessCode = null;
														Long sourceEntityId = ecardVerified.getId();
														Long receipientEntityId = merchant.getId();
														String transactionDetail = "DR CARD|" + ecardVerified.getSerialNo() + "|" + debitAmount + "|" + orderRef + 
																"|" + amount;
														Double closingBalance = null;
														Double totalCreditSum = null;
														Double totalDebitSum = null;
														Long paidInByBankUserAccountId = tokenUser.getId();
														String responseData = null;
														Long adjustedTransactionId = null;
														Long acquirerId = acquirer.getId();
														Long merchantIdL = merchant.getId();
														contactMobile = payerMobile;
														
														
														
														if(balance - ecardVerified.getCardScheme().getMinimumBalance() >= readjustedAmount)
														{
															debitAmount = readjustedAmount;
															readjustedAmount = readjustedAmount - readjustedAmount;
														}
														else
														{
															debitAmount = balance;
															readjustedAmount = readjustedAmount - balance;
														}
														
														
														hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
																+ "tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
														List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
														Boolean debitAccountTrue = true;
														Boolean debitCardTrue = false;
														Long creditAccountId = null;
														Long creditCardId = null;
														Long debitAccountId = null;
														Long debitCardId = null;
														if(devicebankaccounts!=null && devicebankaccounts.size()>0)
														{
															Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
															creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
															debitAccountId = account.getId();
														}
														//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
														
														Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
																customerId, creditAccountTrue, creditCardTrue,
																orderRef, rpin, channel,
																transactionDate, serviceType, payerName,
																payerEmail, payerMobile, status,
																probaseCurrency, transactionCode,
																account, ecardVerified, device,
																creditPoolAccountTrue, messageRequest,
																messageResponse, fixedCharge,
																transactionCharge, transactionPercentage,
																schemeTransactionCharge, schemeTransactionPercentage,
																debitAmount, responseCode, oTP, oTPRef,
																merchantIdL, merchantName, merchantId,
																merchantBank, merchantAccount, 
																transactingBankId, receipientTransactingBankId,
																accessCode, sourceEntityId, receipientEntityId,
																channel, transactionDetail,
																closingBalance, totalCreditSum, totalDebitSum,
																paidInByBankUserAccountId, customdata,
																responseData, adjustedTransactionId, acquirerId, 
																debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, null, 
																device.getSwitchToLive());
														this.swpService.createNewRecord(transaction);
														
														
														
														jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
														jsonObject.put("message", "OTP generated succcessfully");
														jsonObject.put("otp", oTP);
														jsonObject.put("otpRec", contactMobile);
														jsonObject.put("orderRef", orderRef);
														jsonObject.put("transactionRef", transactionRef);
														
														if(customdata!=null)
															jsonObject.put("customData", customdata);
														jsonObject.put("errorMessages", errorMessages);
														
														
														String smsMessage = "Your One-Time Password to complete your transaction/payment is " + oTP;
														//SMSMesage smsMsg1 = new SMSMesage(contactMobile, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
														//swpService.createNewRecord(smsMsg1);


														SmsSender smsSender = new SmsSender(swpService, smsMessage, contactMobile);
														new Thread(smsSender).start();
													}
													else
													{
														//Balance is invalid
														jsonObject.put("status", ERROR.BALANCE_INADEQUATE);
														jsonObject.put("message", "Balance Inadequate");
														errorMessages.put(sn, "Balance Inadequate");
													}
												}
												else
												{

													//Invalid Card - Expired Card

													jsonObject.put("status", ERROR.EXPIRED_CARD);
													jsonObject.put("message", "Expired Card Used");
													errorMessages.put(sn, "Expired Card Used");
												}
											}
											else
											{
												//Invalid Card - Invalid Expiry Date
												jsonObject.put("status", ERROR.INVALID_EXPIRY_DATE);
												jsonObject.put("message", "Invalid Expiry Date Provided");
												errorMessages.put(sn, "Invalid Expiry Date Provided");
											}
										}
										else
										{
											jsonObject.put("status", ERROR.CVV_FAIL);
											jsonObject.put("message", "Invalid CVV Provided");
											errorMessages.put(sn, "Invalid CVV Provided");
										}
						    		}
									else
									{
										//Card does not exist
						    			jsonObject.put("status", ERROR.CARD_NOT_VALID);
										jsonObject.put("message", "Invalid Card Detail Provided");
										errorMessages.put(sn, "Invalid Card Details Provided");
									}
								}
								

								jsonObject.put("errorMessages", errorMessages);
								
								
								
							}
							else
							{
								//Amount beyond max and min limits
								jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
								jsonObject.put("message", "Transaction Amount invalid. Transaction amount must be between ZMW" + 
										app.getAllSettings().getDouble("minimumtransactionamountweb") + " AND ZMW" + app.getAllSettings().getDouble("maximumtransactionamountweb"));
								
							}			
										
										
									
								
						}
						else
						{
							//Hash failed
							jsonObject.put("status", ERROR.HASH_FAIL);
							jsonObject.put("message", "Hash Failed");
						}
			    	}else
			    	{
			    		//return merchant status invalid
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant Device. Merchant and or device are currently invalidated");
			    	}
			    }else
			    {
			    	//return merchant not existing
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
			    }
				
				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.error(">>", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	
	@POST
	@Path("/generateOTPForTokenization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateOTPForTokenization(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("transactionObject") String transactionObject, 
			@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
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
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String transactionInitiator = new String(Base64.decode(transactionObject));
			System.out.println("transactionInitiator=> " + transactionInitiator);
			JSONObject jsTxn = new JSONObject(transactionInitiator);
			String txnDetail = jsTxn.getString("txnDetail");
			System.out.println("acquirerCode=> " + acquirerCode);
			System.out.println("txnDetail=> " + txnDetail);
			System.out.println("bankKey=> " + bankKey);
			
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
				
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
			
			if(txnDetail!=null)
			{
				String cardObjectString = (String)UtilityHelper.decryptData(txnDetail, bankKey);
				System.out.println("cardObjectString=> " + cardObjectString);
				JSONObject ecardJson = new JSONObject(cardObjectString);
				System.out.println("ecardJson=> " + ecardJson.toString());
				String responseUrl  = ecardJson.getString("responseUrl");
				String orderId  = ecardJson.getString("orderId");
				String hash  = ecardJson.getString("hash");
				String merchantId  = ecardJson.getString("merchantId");
				String deviceCode  = ecardJson.getString("deviceCode");
				String serviceTypeId  = ecardJson.getString("serviceTypeId");
				JSONArray selected_  = ecardJson.getJSONArray("selected");
				log.info("......................");
				log.info(selected_.toString());
				log.info(selected_.length());
				System.out.println(selected_.length());
				
				
				hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"'";
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);

				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
				
				if(merchant==null || device==null){
					jsonObject.put("status", ERROR.MERCHANT_DEVICE_FAIL);
					jsonObject.put("message", "Merchant/Device mismatch. Provide appropriate device and merchant codes");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
				if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
		    	{
		    		String api_key = acquirer.getAccessExodus();
					if(UtilityHelper.validateTransactionHash2(
							hash, 
							merchantId,
							deviceCode,
							serviceTypeId,
							orderId,
							responseUrl,
							api_key)==true)
					{
						Collection<BevuraToken> bevuraTokensWalletExisting = new ArrayList<BevuraToken>();
						String randomRefNo = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
						String otp = RandomStringUtils.randomNumeric(4);
						otp = "1111";
						
						for(int x=0; x<selected_.length(); x++)
						{
							String selectedCard = selected_.getString(x);
							log.info(selectedCard);
							System.out.println(selectedCard);
							String[] selectedCard_ = selectedCard.split("~~~");

							System.out.println("Selected Card Details");
							System.out.println(selectedCard_[0]);
							System.out.println(selectedCard_[1]);
							String selectedCard1 = (String)UtilityHelper.decryptData(selectedCard_[0], bankKey);
							if(selectedCard_[1].equals("WALLET"))
							{
								
								hql = "Select tp from Account tp where tp.accountIdentifier = '" + selectedCard1 + "' " +
							    		"AND tp.customer.user.id = '" + tokenUser.getId() + "' AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive() ;
							    Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
							    if(account!=null)
							    {
								    hql = "Select tp from BevuraToken tp where tp.accountId = " + account.getId() + " AND tp.merchantId = " + merchant.getId() + 
								    		" AND tp.deviceId = " + device.getId() + 
								    		" AND tp.isLive = " + device.getSwitchToLive();
								    Collection<BevuraToken> bevuraTokensWallet1 = (Collection<BevuraToken>)this.swpService.getAllRecordsByHQL(hql);
								    Iterator<BevuraToken> bevuraTokenIt = bevuraTokensWallet1.iterator();
								    while(bevuraTokenIt.hasNext())
								    {
								    	BevuraToken bv = bevuraTokenIt.next();
								    	this.swpService.deleteRecord(bv);
								    }
							    }
							}
							if(selectedCard_[1].equals("CARD"))
							{
								hql = "Select tp from ECard tp where tp.serialNo = '" + selectedCard1 + "' " +
							    		"AND tp.account.customer.user.id = '" + tokenUser.getId() + "' AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
								ECard eCard = (ECard)this.swpService.getUniqueRecordByHQL(hql);
							    
							    hql = "Select tp from BevuraToken tp where tp.cardId = " + eCard.getId() + " AND tp.merchantId = " + merchant.getId() + 
							    		" AND tp.deviceId = " + device.getId() + 
							    		" AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
							    Collection<BevuraToken> bevuraTokensWallet1 = (Collection<BevuraToken>)this.swpService.getAllRecordsByHQL(hql);
							    Iterator<BevuraToken> bevuraTokenIt = bevuraTokensWallet1.iterator();
							    while(bevuraTokenIt.hasNext())
							    {
							    	BevuraToken bv = bevuraTokenIt.next();
							    	this.swpService.deleteRecord(bv);
							    }
							}
						}
						
						
						for(int x=0; x<selected_.length(); x++)
						{
							String selectedCard = selected_.getString(x);
							log.info(selectedCard);
							System.out.println(selectedCard);
							String[] selectedCard_ = selectedCard.split("~~~");

							System.out.println("Selected Card Details");
							System.out.println(selectedCard_[0]);
							System.out.println(selectedCard_[1]);
							String selectedCard1 = (String)UtilityHelper.decryptData(selectedCard_[0], bankKey);
							if(selectedCard_[1].equals("WALLET"))
							{
								
								hql = "Select tp from Account tp where tp.accountIdentifier = '" + selectedCard1 + "' " +
							    		"AND tp.customer.user.id = '" + tokenUser.getId() + "' AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
							    Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
							    if(account!=null)
							    {
								    
								    String accountToken = UtilityHelper.tokenizeAccount(account);
								    BevuraToken bevuraToken = new BevuraToken(accountToken, account.getId(), null, merchant.getId(), device.getId(), "ACCOUNT", 
								    		account.getCustomer().getId(), 0, randomRefNo, otp, device.getSwitchToLive());
								    this.swpService.createNewRecord(bevuraToken);
							    }
							}
							if(selectedCard_[1].equals("CARD"))
							{
								hql = "Select tp from ECard tp where tp.serialNo = '" + selectedCard1 + "' " +
							    		"AND tp.account.customer.user.id = '" + tokenUser.getId() + "' AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
								ECard eCard = (ECard)this.swpService.getUniqueRecordByHQL(hql);
								if(eCard!=null)
								{
								    String accountToken = UtilityHelper.tokenizeCard(eCard);
								    BevuraToken bevuraToken = new BevuraToken(accountToken, null, eCard.getId(), merchant.getId(), device.getId(), "CARD", 
								    		eCard.getAccount().getCustomer().getId(), 0, randomRefNo, otp, device.getSwitchToLive());
								    this.swpService.createNewRecord(bevuraToken);
								}
							}
						}
						
						
						String oTP = RandomStringUtils.randomNumeric(4);
						oTP = "1111";
						jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
						jsonObject.put("message", "OTP generated succcessfully");
						jsonObject.put("transactionRef", randomRefNo);
						jsonObject.put("otpRec", tokenUser.getMobileNo());
						

						String smsMessage = "Your One-Time Password to complete your transaction/payment is " + oTP;
						//SMSMesage smsMsg1 = new SMSMesage(contactMobile, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
						//swpService.createNewRecord(smsMsg1);


						SmsSender smsSender = new SmsSender(swpService, smsMessage, tokenUser.getMobileNo());
						new Thread(smsSender).start();
						
					}else
			    	{
			    		//return merchant status invalid
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant Device. Merchant and or device are currently invalidated");
			    	}
			    }else
			    {
			    	//return merchant not existing
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
			    }
			}
			else
			{

		    	jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid details provided to be tokenized");
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.error(">>", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	
	@POST
	@Path("/confirmOTPPayment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmOTPPayment(
		@Context HttpHeaders httpHeaders,
		@Context HttpServletRequest requestContext,
		@FormParam("orderId") String orderId, 
		@FormParam("transactionRef") String transactionRef, 
		@FormParam("otp") String otp, 
		@FormParam("serviceTypeId") String serviceTypeId, 
		@FormParam("responseUrl") String responseUrl, 
		@FormParam("amount") Double amount, 
		@FormParam("merchantId") String merchantId, 
		@FormParam("deviceCode") String deviceCode, 
		@FormParam("hash") String hash, 
		@FormParam("paymentDetail") String paymentDetail,
		@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("amount", amount);
			jsonObject.put("txnRef", transactionRef);
			jsonObject.put("orderId", orderId);
			jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			jsonObject.put("message", "General system error. Transaction could not be completed");
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("amountDebited", 0.00);
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			
			
			
			if(transactionRef!=null && orderId!=null && otp!=null)
			{
				String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
				System.out.println("hql device => " + hql);
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	//return merchant not existing
					JSONArray cardInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("orderId", orderId);
					jsonObject.put("cardInfo", cardInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
					jsonObject.put("amountDebited", 0.00);
					return Response.status(200).entity(jsonObject.toString()).build();
					//jsonObject.put("autoReturnToMerchant", 
					//		(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
					//jsonObject.put("returnUrl", responseUrl);
			    }
			    merchant = device.getMerchant();
			    
				hql = "Select tp from Transaction tp WHERE tp.transactionRef = '" + transactionRef + "' AND tp.orderRef = '" + orderId + "' " +
						"AND tp.OTP = '" + otp + "' AND tp.status = " + TransactionStatus.PENDING.ordinal() + " AND tp.isLive = " + device.getSwitchToLive();
				System.out.println("hql to check transaction => " + hql);
				Collection<Transaction> transactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql);
				if(transactions!=null && transactions.size()>0)
				{
					
					if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
			    	{
			    		String api_key = merchant.getApiKey();
						if(UtilityHelper.validateTransactionHash(
								hash, 
								merchantId,
								deviceCode,
								serviceTypeId,
								orderId,
								amount,
								responseUrl,
								api_key)==true)
						{
							
							if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
							{
								int i1 = 0;
								JSONObject errorMessages = new JSONObject();
								JSONArray cardsVerified = new JSONArray();
								
								Iterator<Transaction> iterTransaction = transactions.iterator();
								String narration = null;
								String accountData = null;
								List txnFail = new ArrayList();
								List txnSuccess = new ArrayList();
								String channel = null; 
								String customerMobileNumber = null; 
								String customrEmailAddress = null; 
								while(iterTransaction.hasNext())
								{
									Transaction tx = iterTransaction.next();
									tx.setDetails(paymentDetail);
									
									channel = tx.getChannel().name();
									ECard ecard = (ECard)tx.getCard();
									customerMobileNumber = ecard.getAccount().getCustomer().getContactMobile();
									customrEmailAddress = ecard.getAccount().getCustomer().getContactEmail();
									Account account = ecard.getAccount();
									String accountNo = account.getAccountIdentifier();
									String bankPaymentReference = null;
									accountData = UtilityHelper.encryptData(accountNo, bankKey);
									System.out.println("bankKey..." + bankKey);
									System.out.println("accountNo..." + accountNo);
									System.out.println("accountData..." + accountData);
									
									narration = "PAYMERCHANT~" + tx.getAmount() + "~" + merchant.getMerchantCode() + "~" + merchant.getMerchantName() + "~" + 
											device.getDeviceCode() + "~" + tx.getOrderRef() + "~" + ecard.getNameOnCard();
									
									MerchantPayment mp = new MerchantPayment(merchant.getMerchantName(), tx.getOrderRef(), tx.getTransactionRef(), 
											null, null, TransactionStatus.PENDING.name(), tx.getAmount(), tx.getId(), tx.getProbasePayCurrency().name(), 
											ecard.getId(), ecard.getAccountId(), ecard.getCustomerId(), ecard.getNameOnCard(), merchant.getId(), device.getId(), 
											device.getSwitchToLive());
									swpService.createNewRecord(mp);
									
									String toencrypt = merchantId+deviceCode+serviceTypeId+orderId+tx.getAmount()+responseUrl+merchant.getApiKey();

									System.out.println("toencrypt..." + toencrypt);
									//String hash1 = UtilityHelper.encryptData(toencrypt, bankKey);
									PaymentServicesV2 paymentServices = new PaymentServicesV2();
									Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, 
											narration, tx.getAmount(), hash, responseUrl, tx, tx.getOrderRef(), serviceTypeId, token);
									String drResStr = (String)drRes.getEntity();
									System.out.println(drResStr);
									if(drResStr!=null)
									{
										JSONObject drResJS = new JSONObject(drResStr);
										if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
										{
											Double charges = tx.getFixedCharge() + tx.getTransactionCharge();
											ecard = ecard.withdraw(swpService, amount, charges);
											String bankReferenceNo = drResJS.getString("bankReferenceNo");
											
											mp.setBank_reference(bankReferenceNo);
											mp.setResponseData(drResStr);
											mp.setStatus(TransactionStatus.SUCCESS.name());
											swpService.updateRecord(mp);
											
											tx.setStatus(TransactionStatus.SUCCESS);
											tx.setTransactionCode(TransactionCode.transactionSuccess);
											tx.setResponseCode(TransactionCode.transactionSuccess);
											tx.setClosingBalance(ecard.getCardBalance());
											tx.setMessageResponse(drResStr);
											tx.setResponseData(drResStr);
											tx.setBankPaymentReference(bankReferenceNo);
											tx.setOTP(null);
											swpService.updateRecord(tx);
											
											
											JSONObject jsSuccess = new JSONObject();
											jsSuccess.put("txnId", tx.getId());
											jsSuccess.put("card", ecard);
											jsSuccess.put("txn", tx);
											jsSuccess.put("amount", tx.getAmount());
											jsSuccess.put("responseMessage", drResStr);
											jsSuccess.put("orderRef", tx.getOrderRef());
											jsSuccess.put("bankReferenceNo", bankReferenceNo);
											jsSuccess.put("transactionRef", tx.getTransactionRef());
											txnSuccess.add(jsSuccess);
										}
										else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
										{
											mp.setResponseData(drResStr);
											mp.setStatus(TransactionStatus.FAIL.name());
											swpService.updateRecord(mp);
											
											tx.setStatus(TransactionStatus.FAIL);
											tx.setResponseCode(TransactionCode.transactionFail);
											tx.setMessageResponse(drResStr);
											tx.setResponseData(drResStr);
											swpService.updateRecord(tx);
											
											String message = drResJS.getString("message");
											JSONObject jsFail = new JSONObject();
											jsFail.put("txnId", tx.getId());
											jsFail.put("amount", tx.getAmount());
											jsFail.put("card", ecard);
											jsFail.put("failReason", message);
											jsFail.put("responseMessage", drResStr);
											jsFail.put("orderRef", tx.getOrderRef());
											jsFail.put("transactionRef", tx.getTransactionRef());
											txnFail.add(jsFail);
										}
									}
									else
									{
										JSONObject jsFail = new JSONObject();
										jsFail.put("txnId", tx.getId());
										jsFail.put("amount", tx.getAmount());
										jsFail.put("card", ecard);
										jsFail.put("failReason", ERROR.GENERAL_FAIL);
										jsFail.put("responseMessage", "General System fail");
										jsFail.put("orderRef", tx.getOrderRef());
										jsFail.put("transactionRef", tx.getTransactionRef());
										txnFail.add(jsFail);
									}
								}
								
								if(txnFail!=null && txnFail.size()>0)
								{
									
									if(txnSuccess!=null && txnSuccess.size()>0)
									{
										Iterator txnSuccessIter = txnSuccess.iterator();
										while(txnSuccessIter.hasNext())
										{
											JSONObject jsSuccess = (JSONObject)txnSuccessIter.next();
											Long txnId = jsSuccess.getLong("txnId");
											Transaction reverseTx = (Transaction)jsSuccess.get("txn");
											ECard reverseCard = (ECard)jsSuccess.get("card");
											Double reverseAmount = (Double)jsSuccess.get("amount");
											
											reverseTx.setStatus(TransactionStatus.REQUEST_ROLLBACK);
											reverseTx.setTransactionCode(TransactionCode.transactionAdviceReverse);
											swpService.updateRecord(reverseTx);
											
											Double charges = reverseTx.getFixedCharge() + reverseTx.getTransactionCharge();
											reverseCard = reverseCard.deposit(swpService, amount, charges);
											
											
										}
									}
									
									JSONArray cardInfo = new JSONArray();
									Iterator<JSONObject> iter = txnFail.iterator();
									Double totalAmountDebited = 0.00;
									while(iter.hasNext())
									{
										JSONObject iterJS = iter.next();
										JSONObject newJS = new JSONObject();
										ECard cd = (ECard)iterJS.get("card");
										newJS.put("serialNo", cd.getSerialNo());
										newJS.put("cardHolder", cd.getNameOnCard());
										newJS.put("acquirer", cd.getAcquirer().getAcquirerName());
										newJS.put("amountDebited", (Double)iterJS.get("amount"));
										newJS.put("transactionRef", (String)iterJS.get("transactionRef"));
										newJS.put("orderRef", (String)iterJS.get("orderRef"));
										newJS.put("failReason", (String)iterJS.getString("failReason"));
										newJS.put("responseMessage", (String)iterJS.getString("responseMessage"));
										cardInfo.put(newJS);
									}
									
									
									jsonObject.put("status", ERROR.DEBIT_FAILED);
									jsonObject.put("message", "Payment was not successful. Please try again");

						    		jsonObject.put("txnRef", transactionRef);
									jsonObject.put("orderId", orderId);
									jsonObject.put("cardInfo", cardInfo);
									jsonObject.put("merchantId", merchantId);
									jsonObject.put("merchantName", merchant.getMerchantName());
									jsonObject.put("channel", channel);
									jsonObject.put("customerMobileNumber", customerMobileNumber);
									jsonObject.put("customerEmailAddress", customrEmailAddress);
									jsonObject.put("deviceCode", device.getDeviceCode());
									jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
									jsonObject.put("amountDebited", 0.00);
									jsonObject.put("autoReturnToMerchant", 
											(device.getFailureUrl()!=null ? 1 : 0));
									jsonObject.put("returnUrl", 
											(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
								}
								else
								{
									JSONArray cardInfo = new JSONArray();
									Iterator<JSONObject> iter = txnSuccess.iterator();
									Double totalAmountDebited = 0.00;
									while(iter.hasNext())
									{
										JSONObject iterJS = iter.next();
										JSONObject newJS = new JSONObject();
										ECard cd = (ECard)iterJS.get("card");
										newJS.put("serialNo", cd.getSerialNo());
										newJS.put("cardHolder", cd.getNameOnCard());
										newJS.put("acquirer", cd.getAcquirer().getAcquirerName());
										newJS.put("amountDebited", (Double)iterJS.get("amount"));
										newJS.put("transactionRef", (String)iterJS.get("transactionRef"));
										newJS.put("orderRef", (String)iterJS.get("orderRef"));
										newJS.put("bankReferenceNo", (String)iterJS.getString("bankReferenceNo"));
										newJS.put("chanel", (String)iterJS.getString("bankReferenceNo"));
										cardInfo.put(newJS);
										totalAmountDebited = totalAmountDebited + ((Double)iterJS.get("amount"));
									}
									jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL);
									jsonObject.put("message", "Payment was successful");
									jsonObject.put("txnRef", transactionRef);
									jsonObject.put("orderId", orderId);
									jsonObject.put("cardInfo", cardInfo);
									jsonObject.put("merchantId", merchantId);
									jsonObject.put("merchantName", merchant.getMerchantName());
									jsonObject.put("channel", channel);
									jsonObject.put("customerMobileNumber", customerMobileNumber);
									jsonObject.put("customerEmailAddress", customrEmailAddress);
									jsonObject.put("deviceCode", device.getDeviceCode());
									jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
									jsonObject.put("amountDebited", totalAmountDebited);
									jsonObject.put("autoReturnToMerchant", 
											(device.getSuccessUrl()!=null ? 1 : 0));
									jsonObject.put("returnUrl", 
											(device.getSuccessUrl()!=null ? device.getSuccessUrl() : responseUrl));
								}
							}
							else
							{
								//Amount beyond max and min limits
								JSONArray cardInfo = new JSONArray();
								jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
								jsonObject.put("message", "Transaction Amount invalid. Transaction amount must be between ZMW" + 
										app.getAllSettings().getDouble("minimumtransactionamountweb") + " AND ZMW" + app.getAllSettings().getDouble("maximumtransactionamountweb"));

					    		jsonObject.put("txnRef", transactionRef);
								jsonObject.put("orderId", orderId);
								jsonObject.put("cardInfo", cardInfo);
								jsonObject.put("merchantId", merchantId);
								jsonObject.put("merchantName", merchant.getMerchantName());
								jsonObject.put("deviceCode", device.getDeviceCode());
								jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
								jsonObject.put("message", "Invalid Merchant. Merchant currently invalidated");
								jsonObject.put("amountDebited", 0.00);
								jsonObject.put("autoReturnToMerchant", 
										(device.getFailureUrl()!=null ? 1 : 0));
								jsonObject.put("returnUrl", 
										(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
							}			
										
										
									
								
						}
						else
						{
							//Hash failed
							JSONArray cardInfo = new JSONArray();
							jsonObject.put("status", ERROR.HASH_FAIL);
							jsonObject.put("message", "Hash Failed");
				    		jsonObject.put("txnRef", transactionRef);
							jsonObject.put("orderId", orderId);
							jsonObject.put("cardInfo", cardInfo);
							jsonObject.put("merchantId", merchantId);
							jsonObject.put("deviceCode", device.getDeviceCode());
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
							jsonObject.put("amountDebited", 0.00);
							jsonObject.put("autoReturnToMerchant", 
									(device.getFailureUrl()!=null ? 1 : 0));
							jsonObject.put("returnUrl", 
									(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
						}
					}
			    	else
			    	{
			    		//return merchant status invalid
						JSONArray cardInfo = new JSONArray();
			    		jsonObject.put("txnRef", transactionRef);
						jsonObject.put("orderId", orderId);
						jsonObject.put("cardInfo", cardInfo);
						jsonObject.put("merchantId", merchantId);
						jsonObject.put("deviceCode", device.getDeviceCode());
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant. Merchant currently invalidated");
						jsonObject.put("amountDebited", 0.00);
						jsonObject.put("autoReturnToMerchant", 
								(device.getFailureUrl()!=null ? 1 : 0));
						jsonObject.put("returnUrl", 
								(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
			    	}
					
				}
				else
				{
					JSONArray cardInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("orderId", orderId);
					jsonObject.put("cardInfo", cardInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
					jsonObject.put("message", "Transaction not found. Ensure the OTP provided is valid");
					jsonObject.put("amountDebited", 0.00);
					//jsonObject.put("returnUrl", responseUrl);
				}
			}
			else{
				//invalid data posted
				JSONArray cardInfo = new JSONArray();
				jsonObject.put("cardInfo", cardInfo);
				jsonObject.put("status", ERROR.DATA_INCONSISTENCY);
				jsonObject.put("message", "Inconsistent data received");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				jsonObject.put("amountDebited", 0.00);
				//jsonObject.put("returnUrl", responseUrl);
			}
		    	
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	

	/**Service Method - Initiate a Payment 
	 * Generate an OTP for the transaction
	 * Make an entry for the transaction
	 * Transaction to be completed when OTP
	 * is valid
	 * 1. Handled Custom Data
	 * */
	@POST
	@Path("/generateOTPForWalletTransaction")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateOTPForWalletTransaction(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("transactionObject") String transactionObject, 
			@FormParam("channel") String channelType, 			
			@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
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
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String transactionInitiator = new String(Base64.decode(transactionObject));
			System.out.println("transactionInitiator=> " + transactionInitiator);
			JSONObject jsTxn = new JSONObject(transactionInitiator);
			String txnDetail = jsTxn.getString("txnDetail");
			System.out.println("acquirerCode=> " + acquirerCode);
			System.out.println("txnDetail=> " + txnDetail);
			System.out.println("bankKey=> " + bankKey);
			
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
				
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
			
			if(txnDetail!=null)
			{
				String walletAccountObjectString = (String)UtilityHelper.decryptData(txnDetail, bankKey);
				System.out.println("walletAccountObjectString=> " + walletAccountObjectString);
				JSONObject walletJson = new JSONObject(walletAccountObjectString);
				System.out.println("walletJson=> " + walletJson.toString());
				String walletIdentifier  = walletJson.has("walletIdentifier") ? walletJson.getString("walletIdentifier") : null;
				System.out.println("walletIdentifier = " + walletIdentifier );
				String payeeFirstName  = walletJson.getString("payeeFirstName");
				String payeeEmail  = walletJson.getString("payeeEmail");
				String payeeMobile  = walletJson.getString("payeeMobile");
				Double amount  = walletJson.getDouble("amount");
				String responseUrl  = walletJson.getString("responseUrl");
				String orderId  = walletJson.getString("orderId");
				String hash  = walletJson.getString("hash");
				String merchantId  = walletJson.getString("merchantId");
				String deviceCode  = walletJson.getString("deviceCode");
				String serviceTypeId  = walletJson.getString("serviceTypeId");
				String currency = walletJson.has("currency") ? walletJson.getString("currency") : null;
				String customdata = walletJson.has("customdata") ? walletJson.getString("customdata") : null;
				
				/*Billing*/
				String billingPayeeFirstName  = walletJson.getString("firstName"); 
				String billingPayeeLastName  = walletJson.getString("lastName");
				String billPayeeMobile  = walletJson.getString("countryCode") + "" + walletJson.getString("billPayeeMobile");
				String billingPayeeEmail  = walletJson.getString("email"); 
				String billingStreetAddress = walletJson.getString("streetAddress");
				String billingCity = walletJson.getString("city");
				String billingDistrict = walletJson.getString("district");
				/*billing ends*/
				
				ProbasePayCurrency probaseCurrency = null;
				try
				{
					probaseCurrency = ProbasePayCurrency.valueOf(currency);
				}
				catch(IllegalArgumentException e)
				{

					jsonObject.put("status", ERROR.INVALID_CURRENCY_PROVIDED);
					jsonObject.put("message", "Invalid currency provided");
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
			    
			    hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device!=null)
			    {
			    	merchant = device.getMerchant();
			    	String deviceResponseUrl = device.getSuccessUrl();
			    	if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
			    	{
			    		String api_key = merchant.getApiKey();
						if(UtilityHelper.validateTransactionHash(
								hash, 
								merchantId,
								deviceCode,
								serviceTypeId,
								orderId,
								amount,
								deviceResponseUrl,
								api_key)==true)
						{
							
							if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
							{
								Double readjustedAmount = amount;
								Boolean amountCheck = false;
								int i1 = 0;
								JSONObject errorMessages = new JSONObject();

								if(walletIdentifier==null)
								{
									jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
									jsonObject.put("message", "You do not provide any valid wallet identifier to be debited for this payment");
								}
								else if(walletIdentifier!=null && walletIdentifier.trim().length()>0)
								{
									
									if(amountCheck==false)
									{
										System.out.println("amount check is false");
									}
									
									hql = "Select tp from Account tp where tp.accountIdentifier = '" + walletIdentifier +"' AND tp.deleted_at IS NULL AND "
											+ "tp.isLive = " + device.getSwitchToLive();
									System.out.println(hql);
									Account walletAccount = (Account)this.swpService.getUniqueRecordByHQL(hql);
									
									if(walletAccount!=null)
									{
										
						    		}

									String oTP = RandomStringUtils.randomNumeric(4);
									oTP = "1111";
									String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
									String bankPaymentReference = null;
									Boolean creditAccountTrue = false;
									Boolean creditCardTrue = false;
									String orderRef = orderId;
									String rpin = null;
									Date transactionDate = new Date();
									ServiceType serviceType = ServiceType.DEBIT_WALLET;
									
									
									Channel channel = null;
									if(channelType==null)
									{
										channel = Channel.WEB;
										if(device.getDeviceType().equals(DeviceType.ATM))
											channel = Channel.ATM;
										if(device.getDeviceType().equals(DeviceType.MPQR))
											channel = Channel.POS;
										if(device.getDeviceType().equals(DeviceType.POS))
											channel = Channel.POS;
										if(device.getDeviceType().equals(DeviceType.WEB))
											channel = Channel.WEB;
									}
									else
									{
										try
										{
											channel = Channel.valueOf(channelType);
										}
										catch(Exception e)
										{
											jsonObject.put("status", ERROR.GENERAL_FAIL);
											jsonObject.put("message", "Invalid Channel Provided");
											jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
											return Response.status(200).entity(jsonObject.toString()).build();
										}
									}
									TransactionStatus status = TransactionStatus.PENDING;
									String transactionCode = TransactionCode.transactionPending;
									Boolean creditPoolAccountTrue = false;
									String messageRequest = transactionObject;
									String messageResponse = null;
									String contactMobile = null;
									
									
									readjustedAmount = amount;
									Long customerId = walletAccount.getCustomer().getId();
									String payerName = walletAccount.getCustomer().getFirstName() + " " + walletAccount.getCustomer().getLastName();
									String payerEmail = walletAccount.getCustomer().getContactEmail();
									String payerMobile = walletAccount.getCustomer().getContactMobile();
									Account account = walletAccount;
									Double fixedCharge = walletAccount.getAccountScheme().getOverrideFixedFee();
									Double transactionCharge = walletAccount.getAccountScheme().getOverrideTransactionFee() * amount / 100;
									Double transactionPercentage = walletAccount.getAccountScheme().getOverrideTransactionFee();
									Double schemeTransactionCharge = walletAccount.getAccountScheme().getOverrideTransactionFee() * amount / 100;
									Double schemeTransactionPercentage = walletAccount.getAccountScheme().getOverrideTransactionFee();
									Double debitAmount = null;
									Double balance = .00;
									

									AccountServicesV2 ws = new AccountServicesV2();
									Response walletBalDet = ws.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), 
											account.getAccountIdentifier(), merchantId, deviceCode);
									String walletDetString = (String)walletBalDet.getEntity();
									JSONObject walletDetails = new JSONObject(walletDetString);
									
									System.out.println("walletDetailsStr --- " + walletDetails.toString());
									 
									if(walletDetails.has("status") && walletDetails.getInt("status")==ERROR.GENERAL_OK)
									{
										String accountList = walletDetails.getString("accountList");
										System.out.println("accountList = " + accountList);
										JSONArray accountListArray = new JSONArray(accountList);
										if(accountListArray!=null)
										{
											
											System.out.println("accountListArray == " + accountListArray.toString());

											Double totalSum = 0.00;
											if(accountListArray.length()>0)
											{
												totalSum = totalSum + accountListArray.getJSONObject(0).getDouble("currentbalance");
											}
											
											balance = totalSum;
										}
									}
									
									
									
									hql = "Select tp from ECard tp where tp.account.id = " + account.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
									Collection<ECard> allCustomerCards = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
									Iterator<ECard> iterECard = allCustomerCards.iterator();
									Double cardBalance = 0.00;
									while(iterECard.hasNext())
									{
										ECard e = iterECard.next();
										cardBalance = cardBalance + e.getCardBalance();
									}
									
									Double actualWalletBalance = balance;
									balance = balance - cardBalance;
									
									String responseCode = null;
									String oTPRef = null;
									String merchantName = merchant.getMerchantName();
									String merchantBank = null;
									String merchantAccount = null;
									Long transactingBankId = null;
									Long receipientTransactingBankId = null;
									Integer accessCode = null;
									Long sourceEntityId = walletAccount.getId();
									Long receipientEntityId = merchant.getId();
									Double closingBalance = null;
									Double totalCreditSum = null;
									Double totalDebitSum = null;
									Long paidInByBankUserAccountId = tokenUser.getId();
									String responseData = null;
									Long adjustedTransactionId = null;
									Long acquirerId = acquirer.getId();
									Long merchantIdL = merchant.getId();
									contactMobile = payerMobile;
									
									
									
									if(balance - walletAccount.getAccountScheme().getMinimumBalance() >= readjustedAmount)
									{
										debitAmount = readjustedAmount;
										String transactionDetail = "DR WALLET|" + walletAccount.getAccountIdentifier() + "|" + debitAmount + "|" + orderRef + 
												"|" + amount;
										
										hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + 
												" AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
										List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
										Boolean debitAccountTrue = true;
										Boolean debitCardTrue = false;
										Long creditAccountId = null;
										Long creditCardId = null;
										Long debitAccountId = null;
										Long debitCardId = null;
										if(devicebankaccounts!=null && devicebankaccounts.size()>0)
										{
											Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
											creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
											debitAccountId = account.getId();
										}
										//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
										
										Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
												customerId, creditAccountTrue, creditCardTrue,
												orderRef, rpin, channel,
												transactionDate, serviceType, payerName,
												payerEmail, payerMobile, status,
												probaseCurrency, transactionCode,
												account, null, device,
												creditPoolAccountTrue, messageRequest,
												messageResponse, fixedCharge,
												transactionCharge, transactionPercentage,
												schemeTransactionCharge, schemeTransactionPercentage,
												debitAmount, responseCode, oTP, oTPRef,
												merchantIdL, merchantName, merchantId,
												merchantBank, merchantAccount, 
												transactingBankId, receipientTransactingBankId,
												accessCode, sourceEntityId, receipientEntityId,
												channel, transactionDetail,
												closingBalance, totalCreditSum, totalDebitSum,
												paidInByBankUserAccountId, customdata,
												responseData, adjustedTransactionId, acquirerId, 
												debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, null, 
												device.getSwitchToLive());
										this.swpService.createNewRecord(transaction);
										
										
										
										jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
										jsonObject.put("message", "OTP generated succcessfully");
										jsonObject.put("otp", oTP);
										jsonObject.put("otpRec", contactMobile);
										jsonObject.put("orderRef", orderRef);
										jsonObject.put("transactionRef", transactionRef);
										
										if(customdata!=null)
											jsonObject.put("customData", customdata);
										jsonObject.put("errorMessages", errorMessages);
										
										


										String smsMessage = "Your One-Time Password to complete your transaction/payment is " + oTP;
										//SMSMesage smsMsg1 = new SMSMesage(contactMobile, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
										//swpService.createNewRecord(smsMsg1);


										SmsSender smsSender = new SmsSender(swpService, smsMessage, contactMobile);
										new Thread(smsSender).start();
									}
									else
									{
										debitAmount = null;
										jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
										
										if((actualWalletBalance + cardBalance - walletAccount.getAccountScheme().getMinimumBalance()) > amount)
											jsonObject.put("message", "Insufficient funds in your wallet available to make this payment. Please fund your wallet to make this payment or you can transfer funds from your cards back to your wallet to make this payment" );
										else
											jsonObject.put("message", "Insufficient funds in your wallet. Please fund your wallet to make this payment" );
									}
									
									
								}
								
								
								
								
							}
							else
							{
								//Amount beyond max and min limits
								jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
								jsonObject.put("message", "Transaction Amount invalid. Transaction amount must be between ZMW" + 
										app.getAllSettings().getDouble("minimumtransactionamountweb") + " AND ZMW" + app.getAllSettings().getDouble("maximumtransactionamountweb"));
								
							}			
										
										
									
								
						}
						else
						{
							//Hash failed
							jsonObject.put("status", ERROR.HASH_FAIL);
							jsonObject.put("message", "Hash Failed");
						}
			    	}else
			    	{
			    		//return merchant status invalid
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant Device. Merchant and or device are currently invalidated");
			    	}
			    }else
			    {
			    	//return merchant not existing
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
			    }
				
				
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.error(">>", e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	
	
	@POST
	@Path("/confirmOTPForWalletPayment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmOTPForWalletPayment(
		@Context HttpHeaders httpHeaders,
		@Context HttpServletRequest requestContext,
		@FormParam("orderId") String orderId, 
		@FormParam("transactionRef") String transactionRef, 
		@FormParam("otp") String otp, 
		@FormParam("serviceTypeId") String serviceTypeId, 
		@FormParam("responseUrl") String responseUrl, 
		@FormParam("amount") Double amount, 
		@FormParam("merchantId") String merchantId, 
		@FormParam("deviceCode") String deviceCode, 
		@FormParam("paymentDetail") String paymentDetail,
		@FormParam("hash") String hash, 
		@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("amount", amount);
			jsonObject.put("txnRef", transactionRef);
			jsonObject.put("orderId", orderId);
			jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			jsonObject.put("message", "General system error. Transaction could not be completed");
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("amountDebited", 0.00);
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			
			
			
			if(transactionRef!=null && orderId!=null && otp!=null)
			{
				String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
					
					
			    if(device!=null)
			    {
			    	
			    }
			    else
			    {
			    	//return merchant not existing
					JSONArray accountInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("orderId", orderId);
					jsonObject.put("accountInfo", accountInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
					jsonObject.put("amountDebited", 0.00);
					return Response.status(200).entity(jsonObject.toString()).build();
					//jsonObject.put("autoReturnToMerchant", 
					//		(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
					//jsonObject.put("returnUrl", responseUrl);
			    }
			    
			    
				hql = "Select tp from Transaction tp WHERE tp.transactionRef = '" + transactionRef + "' AND tp.orderRef = '" + orderId + "' " +
						"AND tp.OTP = '" + otp + "' AND tp.status = " + TransactionStatus.PENDING.ordinal() + " AND tp.isLive = " + device.getSwitchToLive();
				System.out.println("hql to check transaction => " + hql);
				Collection<Transaction> transactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql);
				if(transactions!=null && transactions.size()>0)
				{
					merchant = device.getMerchant();

			    	if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
			    	{
			    		String api_key = merchant.getApiKey();
						if(UtilityHelper.validateTransactionHash(
								hash, 
								merchantId,
								deviceCode,
								serviceTypeId,
								orderId,
								amount,
								responseUrl,
								api_key)==true)
						{
							
							if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
							{
								int i1 = 0;
								JSONObject errorMessages = new JSONObject();
								JSONArray cardsVerified = new JSONArray();
								
								Iterator<Transaction> iterTransaction = transactions.iterator();
								String narration = null;
								String accountData = null;
								List txnFail = new ArrayList();
								List txnSuccess = new ArrayList();
								String channel = null; 
								String customerMobileNumber = null; 
								String customrEmailAddress = null; 
								while(iterTransaction.hasNext())
								{
									Transaction tx = iterTransaction.next();
									tx.setDetails(paymentDetail);
									channel = tx.getChannel().name();
									Account account = (Account)tx.getAccount();
									customerMobileNumber = account.getCustomer().getContactMobile();
									customrEmailAddress = account.getCustomer().getContactEmail();
									String accountNo = account.getAccountIdentifier();
									String bankPaymentReference = null;
									accountData = UtilityHelper.encryptData(accountNo, bankKey);
									System.out.println("bankKey..." + bankKey);
									System.out.println("accountNo..." + accountNo);
									System.out.println("accountData..." + accountData);
									
									narration = "PAYMERCHANT~" + tx.getAmount() + "~" + merchant.getMerchantCode() + "~" + merchant.getMerchantName() + "~" + 
											device.getDeviceCode() + "~" + tx.getOrderRef() + "~" + account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
									
									MerchantPayment mp = new MerchantPayment(merchant.getMerchantName(), tx.getOrderRef(), tx.getTransactionRef(), 
											null, null, TransactionStatus.PENDING.name(), tx.getAmount(), tx.getId(), tx.getProbasePayCurrency().name(), 
											null, account.getId(), account.getCustomer().getId(), account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
											merchant.getId(), device.getId(), device.getSwitchToLive());
									swpService.createNewRecord(mp);
									
									String toencrypt = merchantId+deviceCode+serviceTypeId+orderId+tx.getAmount()+responseUrl+merchant.getApiKey();

									System.out.println("toencrypt..." + toencrypt);
									//String hash1 = UtilityHelper.encryptData(toencrypt, bankKey);
									PaymentServicesV2 paymentServices = new PaymentServicesV2();
									
									
									
									
									
									Double balance = 0.00;
									AccountServicesV2 ws = new AccountServicesV2();
									Response walletBalDet = ws.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), 
											account.getAccountIdentifier(), merchantId, deviceCode);
									String walletDetString = (String)walletBalDet.getEntity();
									JSONObject walletDetails = new JSONObject(walletDetString);
									
									System.out.println("walletDetailsStr --- " + walletDetails.toString());
									 
									if(walletDetails.has("status") && walletDetails.getInt("status")==ERROR.GENERAL_OK)
									{
										String accountList = walletDetails.getString("accountList");
										System.out.println("accountList = " + accountList);
										JSONArray accountListArray = new JSONArray(accountList);
										if(accountListArray!=null)
										{
											
											System.out.println("accountListArray == " + accountListArray.toString());

											Double totalSum = 0.00;
											if(accountListArray.length()>0)
											{
												totalSum = totalSum + accountListArray.getJSONObject(0).getDouble("currentbalance");
											}
											
											balance = totalSum;
										}
									}
									hql = "Select tp from ECard tp where tp.account.id = " + account.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
									Collection<ECard> allCustomerCards = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
									Iterator<ECard> iterECard = allCustomerCards.iterator();
									Double cardBalance = 0.00;
									while(iterECard.hasNext())
									{
										ECard e = iterECard.next();
										cardBalance = cardBalance + e.getCardBalance();
									}
									
									Double actualWalletBalance = balance;
									balance = balance - cardBalance;
									
									
									if(!(balance - account.getAccountScheme().getMinimumBalance() >= (tx.getAmount()+tx.getFixedCharge() + tx.getTransactionCharge())))
									{
										JSONObject jsFail = new JSONObject();
										jsFail.put("txnId", tx.getId());
										jsFail.put("amount", tx.getAmount());
										jsFail.put("account", account);
										jsFail.put("failReason", ERROR.INSUFFICIENT_FUNDS);
										jsFail.put("responseMessage", "Insufficient funds to cover this debit on your wallet");
										jsFail.put("orderRef", tx.getOrderRef());
										jsFail.put("transactionRef", tx.getTransactionRef());
										txnFail.add(jsFail);
									}
									else
									{
										Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, 
												narration, tx.getAmount(), hash, responseUrl, tx, tx.getOrderRef(), serviceTypeId, token);
										String drResStr = (String)drRes.getEntity();
										System.out.println(drResStr);
										if(drResStr!=null)
										{
											JSONObject drResJS = new JSONObject(drResStr);
											if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
											{
												Double charges = tx.getFixedCharge() + tx.getTransactionCharge();
												String bankReferenceNo = drResJS.getString("bankReferenceNo");
												
												
												walletBalDet = ws.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), 
														account.getAccountIdentifier(), merchantId, deviceCode);
												walletDetString = (String)walletBalDet.getEntity();
												walletDetails = new JSONObject(walletDetString);
												
												System.out.println("walletDetailsStr --- " + walletDetails.toString());
												 
												Double newBalance = .00;
												if(walletDetails.has("status") && walletDetails.getInt("status")==ERROR.GENERAL_OK)
												{
													String accountList = walletDetails.getString("accountList");
													System.out.println("accountList = " + accountList);
													JSONArray accountListArray = new JSONArray(accountList);
													if(accountListArray!=null)
													{
														
														System.out.println("accountListArray == " + accountListArray.toString());
							
														Double totalSum = 0.00;
														if(accountListArray.length()>0)
														{
															newBalance = newBalance + accountListArray.getJSONObject(0).getDouble("currentbalance");
														}
													}
												}
												
												mp.setBank_reference(bankReferenceNo);
												mp.setResponseData(drResStr);
												mp.setStatus(TransactionStatus.SUCCESS.name());
												mp.setIsLive(device.getSwitchToLive());
												swpService.updateRecord(mp);
												
												tx.setStatus(TransactionStatus.SUCCESS);
												tx.setTransactionCode(TransactionCode.transactionSuccess);
												tx.setResponseCode(TransactionCode.transactionSuccess);
												tx.setClosingBalance(newBalance);
												tx.setMessageResponse(drResStr);
												tx.setResponseData(drResStr);
												tx.setBankPaymentReference(bankReferenceNo);
												tx.setOTP(null);
												swpService.updateRecord(tx);
												
												
												JSONObject jsSuccess = new JSONObject();
												jsSuccess.put("txnId", tx.getId());
												jsSuccess.put("account", account);
												jsSuccess.put("txn", tx);
												jsSuccess.put("amount", tx.getAmount());
												jsSuccess.put("responseMessage", drResStr);
												jsSuccess.put("orderRef", tx.getOrderRef());
												jsSuccess.put("bankReferenceNo", bankReferenceNo);
												jsSuccess.put("transactionRef", tx.getTransactionRef());
												txnSuccess.add(jsSuccess);
											}
											else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
											{
												mp.setResponseData(drResStr);
												mp.setStatus(TransactionStatus.FAIL.name());
												mp.setIsLive(device.getSwitchToLive());
												swpService.updateRecord(mp);
												
												tx.setStatus(TransactionStatus.FAIL);
												tx.setResponseCode(TransactionCode.transactionFail);
												tx.setMessageResponse(drResStr);
												tx.setResponseData(drResStr);
												tx.setIsLive(device.getSwitchToLive());
												swpService.updateRecord(tx);
												
												String message = drResJS.getString("message");
												JSONObject jsFail = new JSONObject();
												jsFail.put("txnId", tx.getId());
												jsFail.put("amount", tx.getAmount());
												jsFail.put("account", account);
												jsFail.put("failReason", message);
												jsFail.put("responseMessage", drResStr);
												jsFail.put("orderRef", tx.getOrderRef());
												jsFail.put("transactionRef", tx.getTransactionRef());
												txnFail.add(jsFail);
											}
										}
										else
										{
											JSONObject jsFail = new JSONObject();
											jsFail.put("txnId", tx.getId());
											jsFail.put("amount", tx.getAmount());
											jsFail.put("account", account);
											jsFail.put("failReason", ERROR.GENERAL_FAIL);
											jsFail.put("responseMessage", "General System fail");
											jsFail.put("orderRef", tx.getOrderRef());
											jsFail.put("transactionRef", tx.getTransactionRef());
											txnFail.add(jsFail);
										}
									}
								}
								
								if(txnFail!=null && txnFail.size()>0)
								{
									
									JSONArray accountInfo = new JSONArray();
									Iterator<JSONObject> iter = txnFail.iterator();
									Double totalAmountDebited = 0.00;
									while(iter.hasNext())
									{
										JSONObject iterJS = iter.next();
										JSONObject newJS = new JSONObject();
										Account cd = (Account)iterJS.get("account");
										newJS.put("accountIdentifier", cd.getAccountIdentifier());
										newJS.put("accountHolder", cd.getCustomer().getFirstName() + " " + cd.getCustomer().getLastName());
										newJS.put("acquirer", cd.getAcquirer().getAcquirerName());
										newJS.put("amountDebited", (Double)iterJS.get("amount"));
										newJS.put("transactionRef", (String)iterJS.get("transactionRef"));
										newJS.put("orderRef", (String)iterJS.get("orderRef"));
										newJS.put("failReason", (String)iterJS.getString("failReason"));
										newJS.put("responseMessage", (String)iterJS.getString("responseMessage"));
										accountInfo.put(newJS);
									}
									
									
									jsonObject.put("status", ERROR.DEBIT_FAILED);
									jsonObject.put("message", "Payment was not successful. Please try again");

						    		jsonObject.put("txnRef", transactionRef);
									jsonObject.put("orderId", orderId);
									jsonObject.put("accountInfo", accountInfo);
									jsonObject.put("merchantId", merchantId);
									jsonObject.put("merchantName", merchant.getMerchantName());
									jsonObject.put("channel", channel);
									jsonObject.put("customerMobileNumber", customerMobileNumber);
									jsonObject.put("customerEmailAddress", customrEmailAddress);
									jsonObject.put("deviceCode", device.getDeviceCode());
									jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
									jsonObject.put("amountDebited", 0.00);
									jsonObject.put("autoReturnToMerchant", 
											(device.getFailureUrl()!=null ? 1 : 0));
									jsonObject.put("returnUrl", 
											(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
								}
								else
								{
									JSONArray accountInfo = new JSONArray();
									Iterator<JSONObject> iter = txnSuccess.iterator();
									Double totalAmountDebited = 0.00;
									while(iter.hasNext())
									{
										JSONObject iterJS = iter.next();
										JSONObject newJS = new JSONObject();
										Account cd = (Account)iterJS.get("account");
										newJS.put("accountIdentifier", cd.getAccountIdentifier());
										newJS.put("accountHolder", cd.getCustomer().getFirstName() + " " + cd.getCustomer().getLastName());
										newJS.put("acquirer", cd.getAcquirer().getAcquirerName());
										newJS.put("amountDebited", (Double)iterJS.get("amount"));
										newJS.put("transactionRef", (String)iterJS.get("transactionRef"));
										newJS.put("orderRef", (String)iterJS.get("orderRef"));
										newJS.put("bankReferenceNo", (String)iterJS.getString("bankReferenceNo"));
										accountInfo.put(newJS);
										totalAmountDebited = totalAmountDebited + ((Double)iterJS.get("amount"));
									}
									jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL);
									jsonObject.put("message", "Payment was successful");
									jsonObject.put("txnRef", transactionRef);
									jsonObject.put("orderId", orderId);
									jsonObject.put("accountInfo", accountInfo);
									jsonObject.put("merchantId", merchantId);
									jsonObject.put("merchantName", merchant.getMerchantName());
									jsonObject.put("channel", channel);
									jsonObject.put("customerMobileNumber", customerMobileNumber);
									jsonObject.put("customerEmailAddress", customrEmailAddress);
									jsonObject.put("deviceCode", device.getDeviceCode());
									jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
									jsonObject.put("amountDebited", totalAmountDebited);
									jsonObject.put("autoReturnToMerchant", 
											(device.getSuccessUrl()!=null ? 1 : 0));
									jsonObject.put("returnUrl", 
											(device.getSuccessUrl()!=null ? device.getSuccessUrl() : responseUrl));
								}
							}
							else
							{
								//Amount beyond max and min limits
								JSONArray accountInfo = new JSONArray();
								jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
								jsonObject.put("message", "Transaction Amount invalid. Transaction amount must be between ZMW" + 
										app.getAllSettings().getDouble("minimumtransactionamountweb") + " AND ZMW" + app.getAllSettings().getDouble("maximumtransactionamountweb"));

					    		jsonObject.put("txnRef", transactionRef);
								jsonObject.put("orderId", orderId);
								jsonObject.put("accountInfo", accountInfo);
								jsonObject.put("merchantId", merchantId);
								jsonObject.put("merchantName", merchant.getMerchantName());
								jsonObject.put("deviceCode", device.getDeviceCode());
								jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
								jsonObject.put("message", "Invalid Merchant. Merchant currently invalidated");
								jsonObject.put("amountDebited", 0.00);
								jsonObject.put("autoReturnToMerchant", 
										(device.getFailureUrl()!=null ? 1 : 0));
								jsonObject.put("returnUrl", 
										(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
							}			
										
										
									
								
						}
						else
						{
							//Hash failed
							JSONArray accountInfo = new JSONArray();
							jsonObject.put("status", ERROR.HASH_FAIL);
							jsonObject.put("message", "Hash Failed");
				    		jsonObject.put("txnRef", transactionRef);
							jsonObject.put("orderId", orderId);
							jsonObject.put("accountInfo", accountInfo);
							jsonObject.put("merchantId", merchantId);
							jsonObject.put("deviceCode", device.getDeviceCode());
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
							jsonObject.put("amountDebited", 0.00);
							jsonObject.put("autoReturnToMerchant", 
									(device.getFailureUrl()!=null ? 1 : 0));
							jsonObject.put("returnUrl", 
									(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
						}
					}
			    	else
			    	{
			    		//return merchant status invalid
						JSONArray accountInfo = new JSONArray();
			    		jsonObject.put("txnRef", transactionRef);
						jsonObject.put("orderId", orderId);
						jsonObject.put("accountInfo", accountInfo);
						jsonObject.put("merchantId", merchantId);
						jsonObject.put("deviceCode", device.getDeviceCode());
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant. Merchant currently invalidated");
						jsonObject.put("amountDebited", 0.00);
						jsonObject.put("autoReturnToMerchant", 
								(device.getFailureUrl()!=null ? 1 : 0));
						jsonObject.put("returnUrl", 
								(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
			    	}
				}
				else
				{
					JSONArray accountInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("orderId", orderId);
					jsonObject.put("accountInfo", accountInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
					jsonObject.put("message", "Transaction not found. Ensure the OTP provided is valid");
					jsonObject.put("amountDebited", 0.00);
					//jsonObject.put("returnUrl", responseUrl);
				}
			}
			else{
				//invalid data posted
				JSONArray accountInfo = new JSONArray();
				jsonObject.put("accountInfo", accountInfo);
				jsonObject.put("status", ERROR.DATA_INCONSISTENCY);
				jsonObject.put("message", "Inconsistent data received");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				jsonObject.put("amountDebited", 0.00);
				//jsonObject.put("returnUrl", responseUrl);
			}
		    	
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	

	
	@POST
	@Path("/confirmOTPForTokenization")
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmOTPForTokenization(
		@Context HttpHeaders httpHeaders,
		@Context HttpServletRequest requestContext,
		@FormParam("transactionRef") String transactionRef, 
		@FormParam("otp") String otp, 
		@FormParam("serviceTypeId") String serviceTypeId, 
		@FormParam("responseUrl") String responseUrl, 
		@FormParam("merchantId") String merchantId, 
		@FormParam("deviceCode") String deviceCode, 
		@FormParam("token") String token)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		Merchant merchant = null;
		Device device = null;
		int i = 0;
		
		try
		{
			jsonObject.put("txnRef", transactionRef);
			jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			jsonObject.put("message", "General system error. Transaction could not be completed");
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("amountDebited", 0.00);
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			
			
			
			if(transactionRef!=null && otp!=null)
			{
				String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
			    		"AND tp.deviceCode = '" + deviceCode + "'";
			    device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			    if(device==null)
			    {
			    	//return merchant not existing
					JSONArray accountInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("accountInfo", accountInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    	jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid Merchant Code Used");
					jsonObject.put("amountDebited", 0.00);
					//jsonObject.put("autoReturnToMerchant", 
					//		(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
					//jsonObject.put("returnUrl", responseUrl);
					return Response.status(200).entity(jsonObject.toString()).build();
			    }
			    
			    
				hql = "Select tp from BevuraToken tp WHERE tp.activateRefNo = '" + transactionRef + "' " +
						"AND tp.otp = '" + otp + "' AND tp.status = 0 AND tp.isLive = " + device.getSwitchToLive();
				System.out.println("hql to check transaction => " + hql);
				Collection<BevuraToken> bevuraTokens = (Collection<BevuraToken>)this.swpService.getAllRecordsByHQL(hql);
				if(bevuraTokens!=null && bevuraTokens.size()>0)
				{
					
					
					
			    	merchant = device.getMerchant();

			    	if(merchant.getStatus().equals(MerchantStatus.ACTIVE) && device.getStatus().equals(DeviceStatus.ACTIVE))
			    	{
			    		
							Iterator<BevuraToken> bvIt = bevuraTokens.iterator();
							
							JSONArray tokenArray = new JSONArray();
							while(bvIt.hasNext())
							{
								BevuraToken bv = bvIt.next();
								if(bv.getMerchantId()==device.getMerchant().getId() && bv.getDeviceId()==device.getId())
								{
									JSONObject tokens = new JSONObject();
									bv.setActivateRefNo(null);
									bv.setOtp(null);
									bv.setStatus(1);
									bv.setUpdated_at(new Date());
									this.swpService.updateRecord(bv);
									
									if(bv.getType().equals("ACCOUNT"))
									{
										hql = "Select tp from Account tp where tp.id = " + bv.getAccountId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
										Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
										//getRecordById(Account.class, bv.getAccountId());
										
										tokens.put("tokenTitle", account.getAccountIdentifier());
										tokens.put("code", bv.getToken());
										tokens.put("type", "ACCOUNT");
									}
									if(bv.getType().equals("CARD"))
									{
										hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND tp.id = "+bv.getCardId()+" AND tp.isLive = " + device.getSwitchToLive();
										ECard ecard = (ECard)this.swpService.getUniqueRecordByHQL(hql);
										
										tokens.put("tokenTitle", ecard.getSerialNo().substring(0, 4) + " **** **** **** " + (ecard.getSerialNo().substring(ecard.getSerialNo().length()-4)));
										tokens.put("code", bv.getToken());
										tokens.put("type", "CARD");
									}
									tokenArray.put(tokens);
								}
							}
							
							
							if(tokenArray.length()>0)
							{
								jsonObject.put("status", ERROR.GENERAL_OK);
								jsonObject.put("message", "Tokenization was successful");
								jsonObject.put("txnRef", transactionRef);
								jsonObject.put("listing", tokenArray);
								jsonObject.put("merchantId", merchantId);
								jsonObject.put("merchantName", merchant.getMerchantName());
								jsonObject.put("deviceCode", device.getDeviceCode());
								jsonObject.put("autoReturnToMerchant", 
										(device.getSuccessUrl()!=null ? 1 : 0));
								jsonObject.put("returnUrl", 
										(device.getSuccessUrl()!=null ? device.getSuccessUrl() : responseUrl));
							}
							else
							{
								JSONArray accountInfo = new JSONArray();
					    		jsonObject.put("txnRef", transactionRef);
								jsonObject.put("accountInfo", accountInfo);
								jsonObject.put("merchantId", merchantId);
								jsonObject.put("deviceCode", device.getDeviceCode());
					    		jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "Invalid OTP provided");
								jsonObject.put("amountDebited", 0.00);
								jsonObject.put("autoReturnToMerchant", 
										(device.getFailureUrl()!=null ? 1 : 0));
								jsonObject.put("returnUrl", 
										(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
							}
							
					}
			    	else
			    	{
			    		//return merchant status invalid
						JSONArray accountInfo = new JSONArray();
			    		jsonObject.put("txnRef", transactionRef);
						jsonObject.put("accountInfo", accountInfo);
						jsonObject.put("merchantId", merchantId);
						jsonObject.put("deviceCode", device.getDeviceCode());
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
			    		jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
						jsonObject.put("message", "Invalid Merchant. Merchant currently invalidated");
						jsonObject.put("amountDebited", 0.00);
						jsonObject.put("autoReturnToMerchant", 
								(device.getFailureUrl()!=null ? 1 : 0));
						jsonObject.put("returnUrl", 
								(device.getFailureUrl()!=null ? device.getFailureUrl() : responseUrl));
			    	}
				    
				}
				else
				{
					JSONArray accountInfo = new JSONArray();
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("accountInfo", accountInfo);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
					jsonObject.put("message", "Tokens not found. Ensure the OTP provided is valid");
					jsonObject.put("amountDebited", 0.00);
					//jsonObject.put("returnUrl", responseUrl);
				}
			}
			else{
				//invalid data posted
				JSONArray accountInfo = new JSONArray();
				jsonObject.put("accountInfo", accountInfo);
				jsonObject.put("status", ERROR.DATA_INCONSISTENCY);
				jsonObject.put("message", "Inconsistent data received");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				jsonObject.put("amountDebited", 0.00);
				//jsonObject.put("returnUrl", responseUrl);
			}
		    	
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	/*@POST
	@Path("/updateTransactionStatus")
	@Produces(MediaType.APPLICATION_JSON)

	public JSONObject updateTransactionStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,@FormParam("transactionId") Integer transactionId, @FormParam("status") String status)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from Transaction tp where tp.id = " + transactionId;
			Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
			transaction.setStatus(TransactionStatus.valueOf(status));
			this.swpService.updateRecord(transaction);
			jsonObject.put("channel", transaction.getChannel().name());
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Transaction Status Update succcessful");
			jsonObject.put("vendorService", new Gson().toJson(transaction));
		}catch(Exception e)
		{
			log.warn(e);
			log.warn(e);
			String txnData = "{\"trasactionId\":"+transactionId+",\"status\":"+status+"}";
			return jsonObject;
		}
		return jsonObject;
	}*/
	
	
	private JSONObject directDebitZICBPayment1(
			HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Boolean validateOTP, 
			String otp, 
			Transaction transaction, 
			String merchantCode, 
			String deviceCode, 
			String orderId, 
			String accountObject, 
			String accountNo, String narration,
			Acquirer acquirer)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(this.swpService);
			
			String hql;
		
			String card = new String(Base64.decode(accountObject));
			JSONObject jsCard = new JSONObject(card);
			String staff_bank_code = "PROBASEWALLET";
			String wallObject = jsCard.getString("accountobj");
			Device device = null;
			Merchant merchant = null;
			

			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
			
			if(wallObject!=null)
			{
				String walletObjectString = (String)UtilityHelper.decryptData(wallObject, bankKey);
				JSONObject walletJson = new JSONObject(walletObjectString);
				Double amount  = transaction.getAmount();
				String responseUrl  = walletJson.has("responseUrl") ? walletJson.getString("responseUrl") : null;
				String hash  = walletJson.getString("hash");
				//String merchantId  = walletJson.getString("merchantId");
				String serviceTypeId  = walletJson.getString("serviceTypeId");
				//String payFrom = walletJson.getString("payFrom");
				//System.out.println("payFrom => " + payFrom);
				//String[] payFromArray = payFrom.split(":::");
				//System.out.println("payFromArray length => " + payFromArray.length);
				String customdata = walletJson.has("customdata") ? walletJson.getString("customdata") : null;
				
				/*int accountIds[] = new int[payFromArray.length];
				String accountIdentifiers[] = new String[payFromArray.length];
				Double balances[] = new Double[payFromArray.length];
				for(int y=0; y<payFromArray.length; y++)
				{
					System.out.println("payFromArray[y] => " + payFromArray[y]);
					String[] payFromArraySplit = payFromArray[y].split("---");
					accountIds[y] = Integer.valueOf(payFromArraySplit[0]);
					accountIdentifiers[y] = payFromArraySplit[1];
					System.out.println("payFromArraySplit[0] => " + payFromArraySplit[0]);
					System.out.println("payFromArraySplit[1] => " + payFromArraySplit[1]);
				}*/
				
				if(merchantCode!=null && deviceCode!=null)
				{
					hql = "Select tp from Merchant tp WHERE tp.merchantCode = '" + merchantCode + "' AND tp.status = " + MerchantStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
					System.out.println("hql == " + hql);
					merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Device tp WHERE tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
					System.out.println("hql == " + hql);
					device = (Device)this.swpService.getUniqueRecordByHQL(hql);
					
					hql = "Select tp from DeviceBankAccount tp where tp.deviceId = " + device.getId() + " AND tp.status = 1 AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
					Collection<DeviceBankAccount> deviceBankAccounts = (Collection<DeviceBankAccount>)this.swpService.getAllRecordsByHQL(hql);
					
					DeviceBankAccount deviceBankAccount = null;
					if(deviceBankAccounts.size()>0)
					{
						Iterator<DeviceBankAccount> it = deviceBankAccounts.iterator();
						deviceBankAccount = it.next();
					}
					
					
					if(deviceBankAccount==null)
					{
						System.out.println("no bank account found for merchant");
						jsonObject.put("status", ERROR.MERCHANT_BANK_ACCOUNT_NO_EXIST);
						jsonObject.put("message", "No settlement bank account has been assigned to the receipient merchant. Request merchant to update their merchant bank account on ProbasePay");
						jsonObject.put("orderId", orderId);
						jsonObject.put("merchantId", merchantCode);
						jsonObject.put("deviceCode", deviceCode);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						jsonObject.put("autoReturnToMerchant", 
								(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
						jsonObject.put("returnUrl", 
								(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
										device.getFailureUrl() : merchant.getManualReturnUrlLink());
						return jsonObject;
					}
				}
	
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountNo + "' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal() + 
						" AND tp.isLive = " + device.getSwitchToLive();
				Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
				
				if(account!=null)
				{
					int check = 0;
					JSONObject jsonObjectChannels = new JSONObject();
					int km = 0;
					String primaryWalletMobile = "";
					double totalAmtToPay = 0.0;
					String successTxns = null;
					
					Boolean otpCheck = true;
					Boolean debitFail = false;
					String accountsDebited = null;
					String debitedAccountNo = null;
					AccountServicesV2 ws = new AccountServicesV2();
					Response walletBalDet = ws.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getBank().getBankCode(), 
							account.getAccountIdentifier(), merchantCode, deviceCode);
					String walletDetString = (String)walletBalDet.getEntity();
					JSONObject walletDetails = new JSONObject(walletDetString);
					
					System.out.println("walletDetailsStr --- " + walletDetails.toString());
					 
					if(walletDetails.has("status") && walletDetails.getInt("status")==ERROR.GENERAL_OK)
					{
						String accountList = walletDetails.getString("accountList");
						System.out.println("accountList = " + accountList);
						JSONArray accountListArray = new JSONArray(accountList);
						if(accountListArray!=null)
						{
							
							System.out.println("accountListArray == " + accountListArray.toString());

							Double totalSum = 0.00;
							if(accountListArray.length()>0)
							{
								totalSum = totalSum + accountListArray.getJSONObject(0).getDouble("currentbalance");
							}
							
							if(totalSum==0.00 || totalSum<amount)
							{
								jsonObject.put("status", ERROR.TRANSACTION_FAIL);
								jsonObject.put("message", "Payment was not succcessful. Insufficient funds available to complete the transaction");
								jsonObject.put("orderRef", orderId);
								jsonObject.put("channel", "PROBASEPAY WALLET");
								return jsonObject;
							}
								

							JSONObject firstAccountFound = accountListArray.getJSONObject(0);
							Double balance = firstAccountFound.getDouble("currentbalance");
							System.out.println("amount === " + amount);
							System.out.println("balance === " + balance);
							if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
							{
								//if((amount - app.getMinimumBalance())>balance)
								//if(balance  > amount)
								if(balance  > amount)
								{
									Account acc = transaction.getAccount();
									Bank bnk = acc.getAcquirer().getBank();
									
									String mobileNo = account.getCustomer().getContactMobile();
									String emailAddress = account.getCustomer().getContactEmail();
									
									
									if(validateOTP!=null && validateOTP.equals(Boolean.TRUE))
									{
										
										String[] verifyOTP = null;
										if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
											verifyOTP = UtilityHelper.verifyZICBWalletOTP(jsonObject, mobileNo, emailAddress, transaction.getOTPRef(), otp, acquirer.getAuthKey(), acquirer.getFundsTransferEndPoint());
										else
											verifyOTP = UtilityHelper.verifyZICBWalletOTP(jsonObject, mobileNo, emailAddress, transaction.getOTPRef(), otp, acquirer.getDemoAuthKey(), acquirer.getFundsTransferDemoEndPoint());
										
										if(verifyOTP!=null && verifyOTP[0]=="1")
										{
											System.out.println("verifyOTP...");
										}
										else
										{
											jsonObject.put("status", ERROR.INCOMPLETE_TOTAL_AMOUNT_DEBIT);
											jsonObject.put("message", "Payment was not successful. OTP verification failed");
											jsonObject.put("accountsDebited", accountsDebited);
											jsonObject.put("orderId", orderId);
											jsonObject.put("totalamountdebited", totalAmtToPay);
											jsonObject.put("amount", totalAmtToPay);
											if(merchantCode!=null && deviceCode!=null)
											{
												jsonObject.put("merchant", new Gson().toJson(merchant));
												jsonObject.put("merchantId", merchantCode);
												jsonObject.put("device", new Gson().toJson(device));
												jsonObject.put("deviceCode", deviceCode);
												jsonObject.put("autoReturnToMerchant", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
												jsonObject.put("returnUrl", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
																device.getFailureUrl() : merchant.getManualReturnUrlLink());
											}
											jsonObject.put("walletMobile", primaryWalletMobile);
											jsonObject.put("channel", "PROBASEPAY WALLET");
											jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
											return jsonObject;
										}
									}
									
									String narration2 = "Debit " + transaction.getAmount() + " From Wallet Account #" + transaction.getAccount()
											.getAccountIdentifier();
									System.out.println("hql == " + narration);
									System.out.println("hql == " + narration2);
									JSONObject parameters = new JSONObject();
									parameters.put("service", UtilityHelper.ZICB_DEBIT_WALLET_SERVICE_CODE);
									JSONObject parametersRequest = new JSONObject();
									parametersRequest.put("srcAcc", acc.getAccountIdentifier());
									parametersRequest.put("srcBranch", acc.getBranchCode());
									parametersRequest.put("amount", transaction.getAmount());
									parametersRequest.put("payDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
									parametersRequest.put("srcCurrency", transaction.getAccount().getCurrencyCode());
									parametersRequest.put("remarks", narration==null ? ("PMT " + transaction.getTransactionRef()) : narration.substring(0,  narration.length()>15 ? 15 : narration.length()));
									parametersRequest.put("referenceNo", transaction.getTransactionRef());
									parameters.put("request", parametersRequest);
									JSONObject header = new JSONObject();
									if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
										header.put("authKey", acquirer.getAuthKey());
									else
										header.put("authKey", acquirer.getDemoAuthKey());
									
									header.put("Content-Type", "application/json; utf-8");
									header.put("Accept", "application/json");
									System.out.println("parameters == " + parameters.toString());
									String newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_DEBIT_WALLET_URL, parameters.toString(), header);
									System.out.println("newWalletResponse -- " + newWalletResponse);
									
									if(newWalletResponse==null)
									{
										System.out.println("new wallet response is null");
										debitFail = true;
									}
									else
									{
										System.out.println("new wallet response is not null");
										
										JSONObject jsonResponse = new JSONObject(newWalletResponse);
										JSONObject response = jsonResponse.getJSONObject("response");
										JSONObject tekHeader = response.getJSONObject("tekHeader");
										String status = tekHeader.getString("status");
										
										if(status.equals("SUCCESS"))
										{
											transaction.setStatus(TransactionStatus.SUCCESS);
											transaction.setTransactionCode(TransactionCode.transactionSuccess);
											transaction.setResponseCode(TransactionCode.transactionSuccess);
											
					
											//transaction.setTransactingBankId(transaction.getAccount().getBank().getId());
											transaction.setReceipientChannel(Channel.WALLET);
											transaction.setTransactionDetail(narration2 + (narration!=null ? narration : ""));
											transaction.setReceipientEntityId(transaction.getAccount().getId());
					
											hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
													"tp.account.id = " + transaction.getAccount().getId() + " ORDER BY tp.updated_at DESC AND tp.isLive = " + device.getSwitchToLive() ;
											System.out.println("hql == " + hql);
											Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
											Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
											transaction.setClosingBalance((lastTransaction!=null && lastTransaction.getClosingBalance()!=null ? lastTransaction.getClosingBalance() : 0.0) - transaction.getAmount());
											transaction.setTotalDebitSum((lastTransaction!=null && lastTransaction.getTotalDebitSum()!=null ? lastTransaction.getTotalDebitSum() : 0.0) + transaction.getAmount());
											transaction.setUpdated_at(new Date());
											transaction.setOTP(null);
											transaction.setIsLive(device.getSwitchToLive());
											transaction.setResponseData(newWalletResponse);
											
											totalAmtToPay = totalAmtToPay + transaction.getAmount();
											
											this.swpService.updateRecord(transaction);
				
											if(km++==0)
												primaryWalletMobile = transaction.getAccount().getCustomer().getContactMobile();
											
											jsonObjectChannels.put(transaction.getTransactionRef(), transaction.getChannel().name());
											check = 1;
											
											successTxns = transaction.getTransactionRef();
											accountsDebited = transaction.getAccount().getAccountIdentifier();
											debitedAccountNo = transaction.getAccount().getAccountIdentifier();
										}
										else
										{
											transaction.setStatus(TransactionStatus.FAIL);
											transaction.setTransactionCode(TransactionCode.transactionFail);
											transaction.setResponseCode(TransactionCode.transactionFail);
											
											//transaction.setTransactingBankId(transaction.getAccount().getBank().getId());
											transaction.setReceipientChannel(Channel.WALLET);
											transaction.setTransactionDetail(narration2 + (narration!=null ? narration : ""));
											transaction.setReceipientEntityId(transaction.getAccount().getId());
					
											hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
													"tp.account.id = " + transaction.getAccount().getId() + " ORDER BY tp.updated_at DESC AND tp.isLive = " + device.getSwitchToLive() ;
											System.out.println("hql == " + hql);
											Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
											Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
											transaction.setClosingBalance((lastTransaction!=null && lastTransaction.getClosingBalance()!=null ? lastTransaction.getClosingBalance() : 0.0) - transaction.getAmount());
											transaction.setTotalDebitSum((lastTransaction!=null && lastTransaction.getTotalDebitSum()!=null ? lastTransaction.getTotalDebitSum() : 0.0) + transaction.getAmount());
											transaction.setUpdated_at(new Date());
											transaction.setOTP(null);
											//transaction.setIsLive(device.getSwitchToLive());
											transaction.setResponseData(newWalletResponse);
											
											totalAmtToPay = totalAmtToPay + transaction.getAmount();
											
											this.swpService.updateRecord(transaction);
				
											if(km++==0)
												primaryWalletMobile = transaction.getAccount().getCustomer().getContactMobile();
											
											jsonObjectChannels.put(transaction.getTransactionRef(), transaction.getChannel().name());
											check = 1;
											
											successTxns = transaction.getTransactionRef();
											accountsDebited = transaction.getAccount().getAccountIdentifier();
											debitedAccountNo = transaction.getAccount().getAccountIdentifier();
										}
									}
								}
							}
						}
					}
					else
					{
						System.out.println("wallet transaction fail");
					}
					
					
					if(debitFail == true)
					{
						jsonObject.put("status", ERROR.INCOMPLETE_TOTAL_AMOUNT_DEBIT);
						jsonObject.put("message", "Payment was not successful");
						jsonObject.put("accountsDebited", accountsDebited);
						jsonObject.put("orderId", orderId);
						jsonObject.put("totalamountdebited", totalAmtToPay);
						jsonObject.put("amount", totalAmtToPay);
						if(merchantCode!=null && deviceCode!=null)
						{
							jsonObject.put("merchant", new Gson().toJson(merchant));
							jsonObject.put("merchantId", merchantCode);
							jsonObject.put("device", new Gson().toJson(device));
							jsonObject.put("deviceCode", deviceCode);
							jsonObject.put("autoReturnToMerchant", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
							jsonObject.put("returnUrl", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
											device.getFailureUrl() : merchant.getManualReturnUrlLink());
						}
						jsonObject.put("debitedAccountNo", debitedAccountNo.toString());
						jsonObject.put("walletMobile", primaryWalletMobile);
						jsonObject.put("channel", "PROBASEPAY WALLET");
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						return jsonObject;
					}
					
					jsonObject.put("accountsDebited", accountsDebited);
					jsonObject.put("debitedAccountNo", debitedAccountNo.toString());
					jsonObject.put("walletMobile", primaryWalletMobile);
					jsonObject.put("status", ERROR.PAYMENT_TRANSACTION_SUCCESS);
					jsonObject.put("message", "Transaction completed succcessfully");
					jsonObject.put("txnRef", successTxns);
					jsonObject.put("orderId", orderId);
					jsonObject.put("channel", transaction.getChannel().name());
					
					if(merchantCode!=null && deviceCode!=null)
					{
						jsonObject.put("merchantId", merchantCode);
						jsonObject.put("deviceCode", device.getDeviceCode());
						jsonObject.put("autoReturnToMerchant", 
								(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
						jsonObject.put("returnUrl", 
								(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
										device.getSuccessUrl() : merchant.getManualReturnUrlLink());
					}
					jsonObject.put("amount", transaction.getAmount());
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getTransactionDate()));
					//jsonObject.put("billingAddress", new Gson().toJson(billingAddress));
					jsonObject.put("transaction", new Gson().toJson(transaction));
					jsonObject.put("customerMobileContact", transaction.getAccount().getCustomer().getContactMobile());
					jsonObject.put("debitedAccountNo", transaction.getAccount().getAccountIdentifier());
					if(customdata!=null)
						jsonObject.put("customdata", customdata);
				}
			}
			else
			{
				jsonObject.put("status", ERROR.TRANSACTION_FAIL);
				jsonObject.put("message", "Payment was not succcessful");
				jsonObject.put("orderRef", orderId);
				jsonObject.put("channel", "PROBASEPAY WALLET");
			}
		}catch(Exception e)
		{
			log.warn(e.getMessage());
			log.error("error", e);
			e.printStackTrace();
			log.debug(e);
			
			accountObject = "{\"merchantCode\": "+ merchantCode + ", \"deviceCode\": "+ deviceCode + ", \"orderId\": "+ orderId + "}";
		}
		return jsonObject;
	}
	
	
	
	
	@POST
	@Path("/debitWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response debitWallet(
			HttpHeaders httpHeaders,
			HttpServletRequest requestContext, 
			Boolean validateOTP, 
			String otp, 
			String merchantCode, 
			String deviceCode, 
			String accountData, 
			String narration, 
			Double amount,
			String hash,
			String responseUrl,
			Transaction transaction,
			String orderRef,
			String serviceTypeId,
			String token )
	{
		JSONObject jsonObject = new JSONObject();
		try{
			
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(merchantCode);
			System.out.println(deviceCode);
			
			
			
			jsonObject.put("status", ERROR.INCOMPLETE_TOTAL_AMOUNT_DEBIT);
			jsonObject.put("message", "Payment was not successful");
			jsonObject.put("orderId", transaction.getOrderRef());
			jsonObject.put("amount", amount);
			jsonObject.put("merchantId", merchantCode);
			jsonObject.put("deviceCode", deviceCode);
			jsonObject.put("serviceTypeId", serviceTypeId);
			jsonObject.put("channel", transaction.getChannel().name());
			
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(this.swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER+"");
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
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
		
			
			
			if(accountData!=null)
			{
				//accountData = new String(Base64.decode(accountData));
				String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
				System.out.println("bankKey" + bankKey);
				System.out.println("accountData..." + accountData);
				String accountNo = (String)UtilityHelper.decryptData(accountData, bankKey);
				System.out.println("accountNo ==" + accountNo);
				Merchant merchant = null;
				Device device = null;
				
				String apiKey = null;
				if(merchantCode!=null && deviceCode!=null)
				{
					hql = "Select tp from Merchant tp WHERE tp.merchantCode = '" + merchantCode + "' AND tp.status = " + MerchantStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
					System.out.println("hql == " + hql);
					merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
					hql = "Select tp from Device tp WHERE tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
					System.out.println("hql == " + hql);
					device = (Device)this.swpService.getUniqueRecordByHQL(hql);
					
					/*hql = "Select tp from MerchantBankAccount tp where tp.merchant.id = " + merchant.getId() + " AND tp.status = 1 AND tp.deleted_at IS NULL";
					Collection<MerchantBankAccount> merchantBankAccounts = (Collection<MerchantBankAccount>)this.swpService.getAllRecordsByHQL(hql);
					
					MerchantBankAccount merchantBankAccount = null;
					if(merchantBankAccounts.size()>0)
					{
						Iterator<MerchantBankAccount> it = merchantBankAccounts.iterator();
						merchantBankAccount = it.next();
						
						if(merchantBankAccount==null)
						{
							System.out.println("no bank account found for merchant");
							jsonObject.put("status", ERROR.MERCHANT_BANK_ACCOUNT_NO_EXIST);
							jsonObject.put("message", "Merchant can not receive payments at this moment. Please try later");
							jsonObject.put("orderId", transaction.getOrderRef());
							jsonObject.put("merchantId", merchantCode);
							jsonObject.put("deviceCode", deviceCode);
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
							jsonObject.put("autoReturnToMerchant", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
							jsonObject.put("returnUrl", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
											device.getFailureUrl() : merchant.getManualReturnUrlLink());
							return Response.status(200).entity(jsonObject.toString()).build();
						}
					}*/
					apiKey = merchant.getApiKey();
					//apiKey = acquirer.getAccessExodus();
				}
				else
				{
					apiKey = acquirer.getAccessExodus();
				}
				
				
				
				
	
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountNo + "' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal() + 
						" AND tp.isLive = " + device.getSwitchToLive();
				Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
				
				if(account!=null)
				{
					if(UtilityHelper.validateTransactionHash(
							hash, 
							merchantCode==null ? "" : merchantCode,
							deviceCode==null ? "" : deviceCode,
							serviceTypeId,
							transaction.getOrderRef(),
							amount,
							responseUrl,
							apiKey)==true)
					{
						int check = 0;
						JSONObject jsonObjectChannels = new JSONObject();
						int km = 0;
						String primaryWalletMobile = "";
						double totalAmtToPay = 0.0;
						String successTxns = null;
						
						Boolean otpCheck = true;
						Boolean debitFail = false;
						String accountsDebited = null;
						String debitedAccountNo = null;
						AccountServicesV2 ws = new AccountServicesV2();
						Response walletBalDet = ws.getWalletDetails(httpHeaders, requestContext, account.getAcquirer().getAcquirerCode(), 
								account.getAccountIdentifier(), merchantCode, deviceCode);
						String walletDetString = (String)walletBalDet.getEntity();
						JSONObject walletDetails = new JSONObject(walletDetString);
						
						System.out.println("walletDetailsStr --- " + walletDetails.toString());
						 
						if(walletDetails.has("status") && walletDetails.getInt("status")==ERROR.GENERAL_OK)
						{
							String accountList = walletDetails.getString("accountList");
							System.out.println("accountList = " + accountList);
							JSONArray accountListArray = new JSONArray(accountList);
							if(accountListArray!=null)
							{
								
								System.out.println("accountListArray == " + accountListArray.toString());
	
								Double totalSum = 0.00;
								if(accountListArray.length()>0)
								{
									totalSum = totalSum + accountListArray.getJSONObject(0).getDouble("currentbalance");
								}
								
								if(totalSum==0.00 || totalSum<amount)
								{
									jsonObject.put("status", ERROR.TRANSACTION_FAIL+"");
									jsonObject.put("message", "Payment was not succcessful. Insufficient funds available to complete the transaction");
									jsonObject.put("orderRef", transaction.getOrderRef());
									jsonObject.put("channel", transaction.getChannel().name());
									return Response.status(200).entity(jsonObject.toString()).build();
								}
									
	
								JSONObject firstAccountFound = accountListArray.getJSONObject(0);
								Double balance = firstAccountFound.getDouble("currentbalance");
								System.out.println("amount === " + amount);
								System.out.println("balance === " + balance);
								
								//if((amount - app.getMinimumBalance())>balance)
								//if(balance  > amount)
								if(balance  > amount)
								{
									Bank bnk = account.getAcquirer().getBank();
									
									String mobileNo = account.getCustomer().getContactMobile();
									String emailAddress = account.getCustomer().getContactEmail();
									
									
									if(validateOTP!=null && validateOTP.equals(Boolean.TRUE))
									{
										
										String[] verifyOTP = null;
										
										if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
										{
											verifyOTP = UtilityHelper.verifyZICBWalletOTP(jsonObject, mobileNo, emailAddress, transaction.getOTPRef(), otp, 
												acquirer.getAuthKey(), acquirer.getFundsTransferEndPoint());
										}
										else
										{
											verifyOTP = UtilityHelper.verifyZICBWalletOTP(jsonObject, mobileNo, emailAddress, transaction.getOTPRef(), otp, 
													acquirer.getDemoAuthKey(), acquirer.getFundsTransferDemoEndPoint());
										}
										
										if(verifyOTP!=null && verifyOTP[0]=="1")
										{
											System.out.println("verifyOTP...");
										}
										else
										{
											jsonObject.put("status", ERROR.INCOMPLETE_TOTAL_AMOUNT_DEBIT);
											jsonObject.put("message", "Payment was not successful. OTP verification failed");
											jsonObject.put("accountsDebited", accountsDebited);
											jsonObject.put("orderId", transaction.getOrderRef());
											jsonObject.put("totalamountdebited", totalAmtToPay);
											jsonObject.put("amount", totalAmtToPay);
											jsonObject.put("walletMobile", primaryWalletMobile);
											jsonObject.put("channel", transaction.getChannel().name());
											jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
											
											if(merchantCode!=null && deviceCode!=null)
											{
												jsonObject.put("merchantId", merchantCode);
												jsonObject.put("deviceCode", deviceCode);
												jsonObject.put("autoReturnToMerchant", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
												jsonObject.put("returnUrl", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
																device.getFailureUrl() : merchant.getManualReturnUrlLink());
											}
											return Response.status(200).entity(jsonObject.toString()).build();
										}
									}
									
									String narration2 = "Debit " + transaction.getAmount() + " From Wallet Account #" + transaction.getAccount()
											.getAccountIdentifier();
									System.out.println("hql == " + narration);
									System.out.println("hql == " + narration2);
									JSONObject parameters = new JSONObject();
									parameters.put("service", UtilityHelper.ZICB_DEBIT_WALLET_SERVICE_CODE);
									JSONObject parametersRequest = new JSONObject();
									parametersRequest.put("srcAcc", account.getAccountIdentifier());
									parametersRequest.put("srcBranch", account.getBranchCode());
									parametersRequest.put("amount", transaction.getAmount().toString());
									parametersRequest.put("payDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
									parametersRequest.put("srcCurrency", transaction.getAccount().getCurrencyCode());
									parametersRequest.put("remarks", narration==null ? ("DR " + transaction.getTransactionRef()) : narration.substring(0,  narration.length()>15 ? 15 : narration.length()));
									//parametersRequest.put("referenceNo", orderRef);//PREVIOUS VERSION
									parametersRequest.put("transferRef", orderRef);
									JSONObject header = new JSONObject();
									if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
									{
										//header.put("authKey", acquirer.getAuthKey());
										header.put("authKey", device.getZicbAuthKey());
										//parametersRequest.put("serviceKey", acquirer.getFtServiceKey());
									}
									else
									{
										//header.put("authKey", acquirer.getDemoAuthKey());
										header.put("authKey", device.getZicbDemoAuthKey());
										//parametersRequest.put("serviceKey", acquirer.getFtDemoServiceKey());
									}
									
									header.put("Content-Type", "application/json; utf-8");
									header.put("Accept", "application/json");
									parameters.put("request", parametersRequest);
									System.out.println("parameters == " + parameters.toString());
									System.out.println("account.getBranchCode()..." + account.getBranchCode());
									System.out.println("header == " + header.toString());
									String url = "";
									if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
										url = acquirer.getFundsTransferEndPoint();
									else
										url = acquirer.getFundsTransferDemoEndPoint();
									
									String newWalletResponse = UtilityHelper.sendPost(url, parameters.toString(), header);
									System.out.println("newWalletResponse -- " + newWalletResponse);
									jsonObject.put("bankResponse", newWalletResponse);
									
									if(newWalletResponse==null)
									{
										System.out.println("new wallet response is null");
										debitFail = true;
									}
									else
									{
										System.out.println("new wallet response is not null");
										
										JSONObject jsonResponse = new JSONObject(newWalletResponse);
										JSONObject response = jsonResponse.getJSONObject("response");
										JSONObject tekHeader = response.getJSONObject("tekHeader");
										String status = tekHeader.getString("status");
										
										if(status.equals("SUCCESS"))
										{
											String tekesbrefno = tekHeader.getString("tekesbrefno");
											jsonObject.put("bankReferenceNo", tekesbrefno);
											/*transaction.setStatus(TransactionStatus.SUCCESS);
											transaction.setTransactionCode(TransactionCode.transactionSuccess);
											transaction.setResponseCode(TransactionCode.transactionSuccess);
											
					
											//transaction.setTransactingBankId(transaction.getAccount().getBank().getId());
											transaction.setReceipientChannel(Channel.WALLET);
											transaction.setTransactionDetail(narration2 + (narration!=null ? narration : ""));
											transaction.setReceipientEntityId(transaction.getAccount().getId());
					
											hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
													"tp.account.id = " + transaction.getAccount().getId() + " ORDER BY tp.updated_at DESC";
											System.out.println("hql == " + hql);
											Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
											Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
											transaction.setClosingBalance((lastTransaction!=null && lastTransaction.getClosingBalance()!=null ? lastTransaction.getClosingBalance() : 0.0) - transaction.getAmount());
											transaction.setTotalDebitSum((lastTransaction!=null && lastTransaction.getTotalDebitSum()!=null ? lastTransaction.getTotalDebitSum() : 0.0) + transaction.getAmount());
											transaction.setUpdated_at(new Date());
											transaction.setOTP(null);
											transaction.setResponseData(newWalletResponse);
											
											this.swpService.updateRecord(transaction);*/
											
											totalAmtToPay = totalAmtToPay + transaction.getAmount();
											
											
											TransactionStatus transactionStatus = TransactionStatus.SUCCESS;
											ProbasePayCurrency probasePayCurrency = transaction.getProbasePayCurrency();
											ServiceType serviceType = ServiceType.DEBIT_WALLET;
											String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
											String bankPaymentReference = "";
											Long customerId = account.getCustomer().getId();
											String crdrType = "DR";
											Date transactionDate = transaction.getTransactionDate();
											String accountName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
											String accountMobile = account.getCustomer().getContactMobile();
											String transactionCode = transaction.getTransactionCode();
											Long accountId = account.getId();
											Long deviceId = device==null ? null : device.getId();
											Long merchantId = merchant==null ? null : merchant.getId();
											Long transactionId = transaction.getId();
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											String responseCode = transaction.getResponseCode();
											Long adjustedTransactionId = null;
											Long acquirerId = account.getAcquirer().getId();
											Double charges = fixedCharge + transactionCharge + transactionPercentage;
											
											
											WalletTransaction walletTransaction = new WalletTransaction( transactionRef,  bankPaymentReference,  customerId,  crdrType,
													 orderRef, transactionDate, serviceType,  accountName,  accountMobile,
													 transactionStatus, probasePayCurrency,  transactionCode,  accountId,
													deviceId,  merchantId,  transactionId,  fixedCharge,  transactionCharge,
													 transactionPercentage,  amount,  responseCode,  adjustedTransactionId,
													 acquirerId, device.getSwitchToLive());
											this.swpService.createNewRecord(walletTransaction);
				
											if(km++==0)
												primaryWalletMobile = transaction.getAccount().getCustomer().getContactMobile();
											
											jsonObjectChannels.put(transaction.getTransactionRef(), transaction.getChannel().name());
											check = 1;
											
											successTxns = transaction.getTransactionRef();
											accountsDebited = transaction.getAccount().getAccountIdentifier();
											debitedAccountNo = transaction.getAccount().getAccountIdentifier();
											
											jsonObject.put("accountsDebited", accountsDebited);
											jsonObject.put("debitedAccountNo", debitedAccountNo.toString());
											jsonObject.put("walletMobile", primaryWalletMobile);
											jsonObject.put("status", ERROR.PAYMENT_TRANSACTION_SUCCESS);
											jsonObject.put("message", "Wallet debit was successful");
											jsonObject.put("txnRef", successTxns);
											jsonObject.put("orderId", transaction.getOrderRef());
											jsonObject.put("channel", transaction.getChannel().name());
											jsonObject.put("amount", transaction.getAmount());
											jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getTransactionDate()));
											//jsonObject.put("billingAddress", new Gson().toJson(billingAddress));
											//jsonObject.put("transaction", new Gson().toJson(transaction));
											
											if(merchantCode!=null && deviceCode!=null)
											{
												jsonObject.put("merchantId", merchantCode);
												jsonObject.put("deviceCode", device.getDeviceCode());
												jsonObject.put("autoReturnToMerchant", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
												jsonObject.put("returnUrl", 
														(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
																device.getSuccessUrl() : merchant.getManualReturnUrlLink());
											}
											jsonObject.put("customerMobileContact", transaction.getAccount().getCustomer().getContactMobile());
											jsonObject.put("debitedAccountNo", transaction.getAccount().getAccountIdentifier());
											jsonObject.put("transactionId", transaction.getId());
											Customer customer = account.getCustomer();
											jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
											jsonObject.put("customerNumber", customer.getVerificationNumber());
											jsonObject.put("customerId", customer.getId());
											jsonObject.put("accountIdentifier", account.getAccountIdentifier());
											jsonObject.put("charges", charges);
											jsonObject.put("bankReference", bankPaymentReference);
											

											AccountServicesV2 asv = new AccountServicesV2();
											Response balanceDetResp = asv.getAccountBalance(httpHeaders, requestContext, account.getAccountIdentifier(), token, merchantCode, deviceCode);
											String balanceDetStr = (String)balanceDetResp.getEntity();
											System.out.println("balanceDet1.....");
											System.out.println(balanceDetStr);
											if(balanceDetStr==null)
											{
												
											}
											else
											{
												JSONObject balanceDet = new JSONObject(balanceDetStr);
												System.out.println("balanceDet.....");
												System.out.println(balanceDet.toString());
												 
												if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
												{
													System.out.println("Success.....");
													//accountList = balanceDet.getString("accountList");
													//JSONArray accounts = new JSONArray(accountList);
													Double totalCurrentBalance = 0.00;
													Double totalAvailableBalance = 0.00;
													//for(int i=0; i<accounts.length(); i++)
													//{
														//JSONObject acct_ = accounts.getJSONObject(i);
														Double currentbalance = balanceDet.getDouble("currentBalance");
														Double availablebalance = balanceDet.getDouble("availableBalance");
														System.out.println("totalCurrentBalance....." + currentbalance);
														System.out.println("availablebalance....." + availablebalance);
														//totalCurrentBalance = totalCurrentBalance + currentbalance;
														//totalAvailableBalance = totalAvailableBalance + availablebalance;
													//}
													//jsonObject.put("currentbalance", totalCurrentBalance);
													//jsonObject.put("availablebalance", totalAvailableBalance);
													jsonObject.put("currentbalance", currentbalance);
													jsonObject.put("availablebalance", availablebalance);
													
												}
											}
											System.out.println("Deb Res....." + jsonObject.toString());
														
											
										}
										else
										{
											/*transaction.setStatus(TransactionStatus.FAIL);
											transaction.setTransactionCode(TransactionCode.transactionFail);
											transaction.setResponseCode(TransactionCode.transactionFail);
											
											//transaction.setTransactingBankId(transaction.getAccount().getBank().getId());
											transaction.setReceipientChannel(Channel.WALLET);
											transaction.setTransactionDetail(narration2 + (narration!=null ? narration : ""));
											transaction.setReceipientEntityId(transaction.getAccount().getId());
					
											hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
													"tp.account.id = " + transaction.getAccount().getId() + " ORDER BY tp.updated_at DESC";
											System.out.println("hql == " + hql);
											Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
											Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
											transaction.setClosingBalance((lastTransaction!=null && lastTransaction.getClosingBalance()!=null ? lastTransaction.getClosingBalance() : 0.0) - transaction.getAmount());
											transaction.setTotalDebitSum((lastTransaction!=null && lastTransaction.getTotalDebitSum()!=null ? lastTransaction.getTotalDebitSum() : 0.0) + transaction.getAmount());
											transaction.setUpdated_at(new Date());
											transaction.setOTP(null);
											transaction.setResponseData(newWalletResponse);
											
											this.swpService.updateRecord(transaction);*/
											
											totalAmtToPay = totalAmtToPay + transaction.getAmount();
				
											if(km++==0)
												primaryWalletMobile = transaction.getAccount().getCustomer().getContactMobile();
											
											jsonObjectChannels.put(transaction.getTransactionRef(), transaction.getChannel().name());
											check = 1;
											
											successTxns = transaction.getTransactionRef();
											accountsDebited = transaction.getAccount().getAccountIdentifier();
											debitedAccountNo = transaction.getAccount().getAccountIdentifier();
											
											
										}
									}
								}
								
							}
						}
						else
						{
							System.out.println("wallet transaction fail");
						}
						
						
						if(debitFail == true)
						{
							jsonObject.put("status", ERROR.INCOMPLETE_TOTAL_AMOUNT_DEBIT+"");
							jsonObject.put("message", "Payment was not successful");
							jsonObject.put("accountsDebited", accountsDebited);
							jsonObject.put("orderId", transaction.getOrderRef());
							jsonObject.put("totalamountdebited", totalAmtToPay);
							jsonObject.put("amount", totalAmtToPay);
							jsonObject.put("merchantId", merchantCode);
							jsonObject.put("deviceCode", deviceCode);
							jsonObject.put("debitedAccountNo", debitedAccountNo.toString());
							jsonObject.put("walletMobile", primaryWalletMobile);
							jsonObject.put("channel", transaction.getChannel().name());
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
							jsonObject.put("autoReturnToMerchant", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
							jsonObject.put("returnUrl", 
									(merchant.isAutoReturnToMerchantSite()!=null && merchant.isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
											device.getFailureUrl() : merchant.getManualReturnUrlLink());
							return Response.status(200).entity(jsonObject.toString()).build();
						}
					}
					else
					{
						//Hash failed
						jsonObject.put("status", ERROR.HASH_FAIL+"");
						jsonObject.put("message", "Transaction failed. Invalid mismatch. Please try again");
						jsonObject.put("orderId", transaction.getOrderRef());
						jsonObject.put("channel", transaction.getChannel().name());
					}
					
					
				}
			}
			else
			{
				jsonObject.put("status", ERROR.TRANSACTION_FAIL+"");
				jsonObject.put("message", "Payment was not succcessful");
				jsonObject.put("orderId", transaction.getOrderRef());
				jsonObject.put("channel", transaction.getChannel().name());
			}
		}catch(Exception e)
		{
			log.warn(e.getMessage());
			log.error("error", e);
			e.printStackTrace();
			log.debug(e);
			
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	
	
	
	
	
	
	
	@GET
	@Path("/getEWalletAccountBalance")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getEWalletAccountBalance(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("merchantCode") String merchantCode, 
			@QueryParam("deviceCode") String deviceCode, 
			@QueryParam("accountNo") String accountNo, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		
		try
		{
			System.out.println(1);
			this.swpService = this.serviceLocator.getSwpService();
			System.out.println(2);
			Application app = Application.getInstance(swpService);
			System.out.println(3);
			
			String staff_bank_code = "PROBASEWALLET";
			System.out.println(4);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			System.out.println(bankKey);
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			String hql = "";
			Merchant merchant = null;
			Device device = null;
			if(merchantCode!=null && deviceCode!=null)
			{
				System.out.println(merchantCode + " " + deviceCode);
				hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				
				if(merchant==null)
				{
					jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "No Merchant match Found");
					return jsonObject;
				}
				
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL";
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
				
				if(device==null)
				{
					jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "No Device match Found");
					return jsonObject;
				}
			}
			if(device==null || merchant==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "No Device match Found");
				return jsonObject;
			}
			
			System.out.println(verifyJ.toString());
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountNo + "' AND tp.status = " + 
					AccountStatus.ACTIVE.ordinal() + " AND tp.isLive = " + device.getSwitchToLive();
		    Collection<Account> acctList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			    
				Double totalBalance = 0.0;
				if(acctList.size()>0)
				{
					System.out.println(acctList.size());
					Iterator<Account> iter = acctList.iterator();
					JSONObject acctBalances = new JSONObject();
					while(iter.hasNext())
					{
						Account acct = (Account)iter.next();
						System.out.println(acct.getAccountType().name());
						
						if(acct!=null && merchantCode!=null && deviceCode!=null)
						{
							System.out.println(231);
							JSONObject balanceDet = this.getEWalletAccountBalance(httpHeaders, requestContext, merchantCode, deviceCode, acct.getAccountIdentifier(), token);
							 
							if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
							{
								String accountList = balanceDet.getString("accountList");
								JSONArray accounts = new JSONArray(accountList);
								for(int i=0; i<accounts.length(); i++)
								{
									JSONObject acct_ = accounts.getJSONObject(i);
									Double currentbalance = acct_.getDouble("currentbalance");
									Double availablebalance = acct_.getDouble("availablebalance");
									totalBalance = totalBalance + currentbalance;
									acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), currentbalance);
								}
								
							}
						}
						else
						{
							System.out.println(441);
							
							hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
									"tp.account.id = " + acct.getId() + " AND tp.isLive = " + device.getSwitchToLive() + " ORDER BY tp.updated_at DESC";
							System.out.println(hql);
							Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
							System.out.println(5);
							Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
							System.out.println(6);
							totalBalance = totalBalance + (lastTransaction==null ? 0 : lastTransaction.getClosingBalance());
							System.out.println(7);
							acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), (lastTransaction==null ? 0 : lastTransaction.getClosingBalance()));
							System.out.println(8);
						}
								
					}
					System.out.println(45551);
					jsonObject.put("status", ERROR.EWALLET_BALANCE_PULL_SUCCESS);
					jsonObject.put("message", "Balance Retrieved");
					jsonObject.put("balanceList", (acctBalances));
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
		    	
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	
	
	
	
	
	
	@POST
	@Path("/initiateCyberSourcePayment")
	@Produces(MediaType.APPLICATION_JSON)
	public Response initiateCyberSourcePayment(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("transactionObject") String transactionObject,
			@FormParam("acquirerCode") String acquirerCode)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		int i = 0;
		
		try
		{
			
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String transactionInitiator = new String(Base64.decode(transactionObject));
			System.out.println("transactionInitiator=> " + transactionInitiator);
			JSONObject jsTxn = new JSONObject(transactionInitiator);
			String txnDetail = jsTxn.getString("txnDetail");
			System.out.println("acquirerCode=> " + acquirerCode);
			System.out.println("txnDetail=> " + txnDetail);
			System.out.println("bankKey=> " + bankKey);
				
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
			
			if(txnDetail!=null)
			{
				String cardObjectString = (String)UtilityHelper.decryptData(txnDetail, bankKey);
				System.out.println("cardObjectString=> " + cardObjectString);
				JSONObject ecardJson = new JSONObject(cardObjectString);
				System.out.println("ecardJson=> " + ecardJson.toString());

				Double amount  = ecardJson.getDouble("amount");
				String responseUrl  = ecardJson.getString("responseUrl");
				String orderId  = ecardJson.getString("orderId");
				String hash  = ecardJson.getString("hash");
				String merchantId  = ecardJson.getString("merchantId");
				String deviceCode  = ecardJson.getString("deviceCode");
				String serviceTypeId  = ecardJson.getString("serviceTypeId");
				String currency = ecardJson.has("currency") ? ecardJson.getString("currency") : null;
				String customdata = ecardJson.has("customdata") ? ecardJson.getString("customdata") : null;
				
				/*Billing*/
				String billingPayeeFirstName  = ecardJson.getString("firstName"); 
				String billingPayeeLastName  = ecardJson.getString("lastName");
				String billPayeeMobile  = ecardJson.getString("countryCode") + "" + ecardJson.getString("billPayeeMobile");
				String billingPayeeEmail  = ecardJson.getString("email"); 
				String billingStreetAddress = ecardJson.getString("streetAddress");
				String billingCity = ecardJson.getString("city");
				String billingDistrict = ecardJson.getString("district");
				/*billing ends*/
				
				ProbasePayCurrency probaseCurrency = null;
				try
				{
					probaseCurrency = ProbasePayCurrency.valueOf(currency);
				}
				catch(IllegalArgumentException e)
				{

					jsonObject.put("status", ERROR.INVALID_CURRENCY_PROVIDED);
					jsonObject.put("message", "Invalid currency provided");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
	
				
				/*public String initiateCyberSourcePayment(String billingFirstName, String billingLastName, String billingPhone, String billingEmail, 
						String billingStreetAddress, String billingCity, String billingDistrict, String merchantId, 
						String deviceCode, String serviceTypeId, String orderId, String hash, String payerName, 
						String payerEmail, String payerPhone, String amount, String paymentItem, String responseurl, 
						String payerId, String nationalId, String scope, String description, String customdata, String currency )
				{*/
			
				hql = "Select tp.* from devices tp, merchants tm where tp.merchant_id = tm.id AND tm.merchantCode = '" + merchantId + "' AND " +
						"tp.deviceCode = '" + deviceCode + "' AND tm.status = " + MerchantStatus.ACTIVE.ordinal() + " AND " +
						"tp.status = " + DeviceStatus.ACTIVE.ordinal();
				List<Map<String, Object>> devices = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(!(devices!=null && devices.size()>0))
				{
					jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid merchant details provided. Profile could not be matched to an existing valid merchant account");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				Map<String, Object> device = devices.get(0);
				
				if(device!=null)
				{
					String api_key = (String)device.get("apiKey");
					 
					JSONObject validate = UtilityHelper.validateForCyberSource(billingPayeeFirstName, billingPayeeLastName, billPayeeMobile, billingPayeeEmail, 
							billingStreetAddress, billingCity, billingDistrict, merchantId, 
							deviceCode, serviceTypeId, orderId, hash, billingPayeeFirstName + " " + billingPayeeLastName, 
							Double.valueOf(amount), responseUrl, 
							api_key, this.swpService, app);
					
					
					Boolean isDeviceLive = false;
					isDeviceLive = (Boolean)device.get("switchToLive");
					JSONObject allSettings = app.getAllSettings();

					String cyberSourceAccessKey = null;
					String cyberSourceProfileId = null;
					String cyberSourceSecretKey = null;
					String cyberSourceUrl = null;
					String locale = allSettings.getString("cybersourcelocale");
					if(isDeviceLive!=null && isDeviceLive.equals(Boolean.TRUE))
					{
						cyberSourceAccessKey = device.get("cybersourceLiveAccessKey")!=null ? (String)device.get("cybersourceLiveAccessKey") : allSettings.getString("cybersourceaccesskey");
						cyberSourceProfileId = device.get("cybersourceLiveProfileId")!=null ? (String)device.get("cybersourceLiveProfileId") : allSettings.getString("cybersourceprofileid");
						cyberSourceSecretKey = device.get("cybersourceLiveSecretKey")!=null ? (String)device.get("cybersourceLiveSecretKey") : allSettings.getString("cybersourcesecretkey");
						cyberSourceUrl = app.cyberSourceUrl;
						jsonObject.put("cybersource_url", cyberSourceUrl);
						jsonObject.put("access_key",cyberSourceAccessKey);
						jsonObject.put("profile_id", cyberSourceProfileId);
						System.out.println("k = " + cyberSourceSecretKey);
						System.out.println("access_key = " + cyberSourceAccessKey);
						System.out.println("profile_id = " + cyberSourceProfileId);
					}
					else
					{
						cyberSourceAccessKey = device.get("cybersourceDemoAccessKey")!=null ? (String)device.get("cybersourceDemoAccessKey") : allSettings.getString("cybersourcedemoaccesskey");
						cyberSourceProfileId = device.get("cybersourceDemoProfileId")!=null ? (String)device.get("cybersourceDemoProfileId") : allSettings.getString("cybersourcedemoprofileid");
						cyberSourceSecretKey = device.get("cybersourceDemoSecretKey")!=null ? (String)device.get("cybersourceDemoSecretKey") : allSettings.getString("cybersourcedemosecretkey");
						cyberSourceUrl = app.demoCyberSourceUrl;
						jsonObject.put("cybersource_url", cyberSourceUrl);
						jsonObject.put("access_key", cyberSourceAccessKey);
						jsonObject.put("profile_id", cyberSourceProfileId);
					}
					if(validate==null)
					{
						if(currency==null)
						{
							currency = ProbasePayCurrency.ZMW.name();
						}
						

						String messageRequest = transactionObject;
						String messageResponse = null;
						String contactMobile = null;
						
						//Double fixedCharge = app.getAllSettings()!=null && app.getAllSettings().has("CyberSource Fixed Charge") ? app.getAllSettings().getDouble("CyberSource Fixed Charge") : null;;
						//Double transactionPercentage = app.getAllSettings()!=null && app.getAllSettings().has("CyberSource Transaction Percentage") ? app.getAllSettings().getDouble("CyberSource Transaction Percentage") : null;
						Double fixedCharge = Double.valueOf(allSettings.getString("cybersourcefixedcharge"));
						Double transactionPercentage = Double.valueOf(allSettings.getString("cybersourcetransactionpercentcharge"));
						Double transactionCharge = transactionPercentage*amount/100;
						
						hql = "Select tp from devices tp where tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
						Device device_ = (Device)this.swpService.getUniqueRecordByHQL(hql);
						Merchant merchant = device_.getMerchant();
						
						String transactionDetail = "PAY MERCHANT|CYBERSOURCE|" + orderId + 
								"|" + amount;
						

						Boolean debitAccountTrue = false;
						Boolean debitCardTrue = false;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						Transaction transaction = new Transaction(null, null,
								null, false, false,
								orderId, null, Channel.WEB, new Date(), ServiceType.DEBIT_CARD, billingPayeeFirstName + " " + billingPayeeLastName,
								billingPayeeEmail, billPayeeMobile, TransactionStatus.PENDING, ProbasePayCurrency.ZMW, TransactionCode.transactionPending,
								null, null, device_, false, messageRequest,
								messageResponse,  fixedCharge, transactionCharge, transactionPercentage,
								fixedCharge,  transactionPercentage, amount, TransactionCode.transactionPending, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(), null, null, 
								null, null, null, null, merchant.getId(),
								Channel.WEB, transactionDetail, null,  null,  null,
								null, null, null, null, acquirer.getId(), 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, null, (Integer)device.get("switchToLive"));
						

						
						jsonObject.put("transaction_uuid", (cyberSourceAccessKey + "-" + transaction.getTransactionRef()));
						
						//String signed_field_names = "access_key,profile_id,transaction_uuid,signed_field_names,unsigned_field_names,signed_date_time,locale,transaction_type,reference_number,amount,currency";
						String signed_field_names = "amount,currency,reference_number,transaction_uuid,transaction_type,locale,bill_to_address_city,bill_to_address_country";
						signed_field_names = signed_field_names + ",bill_to_address_line1,bill_to_address_postal_code,bill_to_address_state,bill_to_email";
						signed_field_names = signed_field_names + ",bill_to_forename,bill_to_surname,bill_to_phone,signed_date_time";
						signed_field_names = signed_field_names + ",signed_field_names,unsigned_field_names,profile_id,access_key";
						String unsigned_field_names = "";
						SimpleDateFormat sdf = new SimpleDateFormat("Y-MM-dd'T'H:m:s'Z'");
						String signed_date_time = sdf.format(transaction.getTransactionDate()); 
						//Demo: String transaction_type = "authorization";
						String transaction_type = "sale";
						String reference_number = merchant.getMerchantCode() + "-" + transaction.getOrderRef();
						
						
						
						
						jsonObject.put("switch_to_live", (isDeviceLive!=null && isDeviceLive.equals(Boolean.TRUE)) ? 1 : 0);
						//jsonObject.put("access_key", "ec5891c2e1513fc5a62054abca70593f");
						//jsonObject.put("profile_id", "E00109B5-665F-4696-A37D-96FAA1C718E5");
						jsonObject.put("signed_field_names", signed_field_names);
						jsonObject.put("unsigned_field_names", unsigned_field_names);
						jsonObject.put("signed_date_time", signed_date_time);
						jsonObject.put("locale", locale);
						jsonObject.put("transaction_type", transaction_type);
						jsonObject.put("reference_number", reference_number);
						jsonObject.put("amount", amount);
						jsonObject.put("currency", currency);
						String toSignData = "";

						//String[] signFields = signed_field_names.split(",");
						//for(int m=0; m<signFields.length; m++)
						//{
							//toSignData = toSignData + signFields[m] + "=" + jsonObject.getString(signFields[m]) + ",";
						//}
						//toSignData = toSignData.substring(0, (toSignData.length()-1));
						//String signature = UtilityHelper.hash_hmac(toSignData, app.cyberSourceSecretKey);
						//System.out.println("Signature = " + signature);
						//jsonObject.put("signature", signature);
						//System.out.println("1Signature = " + signature);
						if(customdata!=null)
							jsonObject.put(("customdata"), customdata);
						
						jsonObject.put("status", ERROR.PAYMENT_INITIATION_SUCCESS);
						System.out.println("1Signature = " + 1);
						jsonObject.put("message", "Transaction initiated succcessfully");
						System.out.println("1Signature = " + 2);
						jsonObject.put("txnRef", transaction.getTransactionRef());
						System.out.println("1Signature = " + 3);
						jsonObject.put("orderId", orderId);
						System.out.println("1Signature = " + 4);
						jsonObject.put("merchantId", merchantId);
						System.out.println("1Signature = " + 5);
						jsonObject.put("deviceCode", deviceCode);
						System.out.println("1Signature = " + 6);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getTransactionDate()));
						System.out.println("1Signature = " + 7);
						System.out.println("1Signature = " + 20);
						System.out.println("1Signature = " + 21);
						jsonObject.put("currency", currency);
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					else
					{
						//validation Failed
						String status = (String)validate.keys().next();
						jsonObject.put("status", status);
						jsonObject.put("message", validate.getString(status));
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						
						jsonObject.put("autoReturnToMerchant", 
								(device.get("isAutoReturnToMerchantSite")!=null && ((Boolean)device.get("isAutoReturnToMerchantSite")).equals(Boolean.TRUE)) ? 1 : 0);
						System.out.println("1Signature = " + 19);
						jsonObject.put("returnUrl", 
								(device.get("isAutoReturnToMerchantSite")!=null && ((Boolean)device.get("isAutoReturnToMerchantSite")) ? 
										device.get("successUrl") : (String)device.get("manualReturnUrlLink")));
						return Response.status(200).entity(jsonObject.toString()).build();
					}
				}
				else
				{
					//device/merchant combination is wrong
					jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
					jsonObject.put("message", "Merchant Code & Device Code Are Invalid");
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
			}
			else
			{

				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete/Invalid parameters provided");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.debug(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	@POST
	@Path("/initiateCyberSourcePaymentV2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response initiateCyberSourcePaymentV2(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("transactionObject") String transactionObject, 
			@FormParam("token") String token,
			@FormParam("acquirerCode") String acquirerCode)
	{
		//Channel: WEB, POS, OTC, ONLINE_BANKING, MOBILE
		JSONObject jsonObject = new JSONObject();
		Collection<Transaction> transactionList = null;
		int i = 0;
		
		try
		{
			
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String transactionInitiator = new String(Base64.decode(transactionObject));
			System.out.println("transactionInitiator=> " + transactionInitiator);
			JSONObject jsTxn = new JSONObject(transactionInitiator);
			String txnDetail = jsTxn.getString("txnDetail");
			System.out.println("acquirerCode=> " + acquirerCode);
			System.out.println("txnDetail=> " + txnDetail);
			System.out.println("bankKey=> " + bankKey);
				
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
			
			if(txnDetail!=null)
			{
				String cardObjectString = (String)UtilityHelper.decryptData(txnDetail, bankKey);
				System.out.println("cardObjectString=> " + cardObjectString);
				JSONObject ecardJson = new JSONObject(cardObjectString);
				System.out.println("ecardJson=> " + ecardJson.toString());
				
				Double amount  = ecardJson.getDouble("amount");
				String responseUrl  = ecardJson.getString("responseUrl");
				String orderId  = ecardJson.getString("orderId");
				String hash  = ecardJson.getString("hash");
				String merchantId  = ecardJson.getString("merchantId");
				String deviceCode  = ecardJson.getString("deviceCode");
				String serviceTypeId  = ecardJson.getString("serviceTypeId");
				String currency = ecardJson.has("currency") ? ecardJson.getString("currency") : null;
				String customdata = ecardJson.has("customdata") ? ecardJson.getString("customdata") : null;
				
				
				
				
				
				ProbasePayCurrency probaseCurrency = null;
				try
				{
					probaseCurrency = ProbasePayCurrency.valueOf(currency);
				}
				catch(IllegalArgumentException e)
				{

					jsonObject.put("status", ERROR.INVALID_CURRENCY_PROVIDED);
					jsonObject.put("message", "Invalid currency provided");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
	
				
				/*public String initiateCyberSourcePayment(String billingFirstName, String billingLastName, String billingPhone, String billingEmail, 
						String billingStreetAddress, String billingCity, String billingDistrict, String merchantId, 
						String deviceCode, String serviceTypeId, String orderId, String hash, String payerName, 
						String payerEmail, String payerPhone, String amount, String paymentItem, String responseurl, 
						String payerId, String nationalId, String scope, String description, String customdata, String currency )
				{*/
			
				hql = "Select tp.*, tm.apiKey from devices tp, merchants tm where tp.merchant_id = tm.id AND tm.merchantCode = '" + merchantId + "' AND " +
						"tp.deviceCode = '" + deviceCode + "' AND tm.status = " + MerchantStatus.ACTIVE.ordinal() + " AND " +
						"tp.status = " + DeviceStatus.ACTIVE.ordinal();
				List<Map<String, Object>> devices = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(!(devices!=null && devices.size()>0))
				{
					jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Invalid merchant details provided. Profile could not be matched to an existing valid merchant account");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				Map<String, Object> device = devices.get(0);
				
				if(device!=null)
				{
					String api_key = (String)device.get("apiKey");
					 
					JSONObject validate = UtilityHelper.validateForCyberSourceV2(merchantId, 
							deviceCode, serviceTypeId, orderId, hash, 
							Double.valueOf(amount), responseUrl, 
							api_key, this.swpService, app);
					
					
					Boolean isDeviceLive = false;
					isDeviceLive = (Boolean)device.get("switchToLive");
					JSONObject allSettings = app.getAllSettings();

					String cyberSourceAccessKey = null;
					String cyberSourceProfileId = null;
					String cyberSourceSecretKey = null;
					String cyberSourceUrl = null;
					String locale = allSettings.getString("cybersourcelocale");
					if(isDeviceLive!=null && isDeviceLive.equals(Boolean.TRUE))
					{
						cyberSourceAccessKey = device.get("cybersourceLiveAccessKey")!=null ? (String)device.get("cybersourceLiveAccessKey") : allSettings.getString("cybersourceaccesskey");
						cyberSourceProfileId = device.get("cybersourceLiveProfileId")!=null ? (String)device.get("cybersourceLiveProfileId") : allSettings.getString("cybersourceprofileid");
						cyberSourceSecretKey = device.get("cybersourceLiveSecretKey")!=null ? (String)device.get("cybersourceLiveSecretKey") : allSettings.getString("cybersourcesecretkey");
						cyberSourceUrl = app.cyberSourceUrl;
						jsonObject.put("cybersource_url", cyberSourceUrl);
						jsonObject.put("access_key",cyberSourceAccessKey);
						jsonObject.put("profile_id", cyberSourceProfileId);
						System.out.println("k = " + cyberSourceSecretKey);
						System.out.println("access_key = " + cyberSourceAccessKey);
						System.out.println("profile_id = " + cyberSourceProfileId);
					}
					else
					{
						cyberSourceAccessKey = device.get("cybersourceDemoAccessKey")!=null ? (String)device.get("cybersourceDemoAccessKey") : allSettings.getString("cybersourcedemoaccesskey");
						cyberSourceProfileId = device.get("cybersourceDemoProfileId")!=null ? (String)device.get("cybersourceDemoProfileId") : allSettings.getString("cybersourcedemoprofileid");
						cyberSourceSecretKey = device.get("cybersourceDemoSecretKey")!=null ? (String)device.get("cybersourceDemoSecretKey") : allSettings.getString("cybersourcedemosecretkey");
						cyberSourceUrl = app.demoCyberSourceUrl;
						jsonObject.put("cybersource_url", cyberSourceUrl);
						jsonObject.put("access_key", cyberSourceAccessKey);
						jsonObject.put("profile_id", cyberSourceProfileId);
					}
					if(validate==null)
					{
						if(currency==null)
						{
							currency = ProbasePayCurrency.ZMW.name();
						}
						

						String messageRequest = transactionObject;
						String messageResponse = null;
						String contactMobile = null;
						
						//Double fixedCharge = app.getAllSettings()!=null && app.getAllSettings().has("CyberSource Fixed Charge") ? app.getAllSettings().getDouble("CyberSource Fixed Charge") : null;;
						//Double transactionPercentage = app.getAllSettings()!=null && app.getAllSettings().has("CyberSource Transaction Percentage") ? app.getAllSettings().getDouble("CyberSource Transaction Percentage") : null;
						Double fixedCharge = Double.valueOf(allSettings.getString("cybersourcefixedcharge"));
						Double transactionPercentage = Double.valueOf(allSettings.getString("cybersourcetransactionpercentcharge"));
						Double transactionCharge = transactionPercentage*amount/100;
						
						hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
						Device device_ = (Device)this.swpService.getUniqueRecordByHQL(hql);
						Merchant merchant = device_.getMerchant();
						
						String transactionDetail = "PAY MERCHANT|CYBERSOURCE|" + orderId + 
								"|" + amount;
						
						
						

						/*String bill_to_address_city  = ecardJson.getString("city");
						String bill_to_address_country  = ecardJson.getString("country");
						String bill_to_address_line1  = ecardJson.getString("streetAddress");
						String bill_to_address_state  = ecardJson.getString("district");
						String bill_to_email  = ecardJson.has("email") ? ecardJson.getString("email") : null;
						String bill_to_forename  = ecardJson.getString("firstName");
						String bill_to_surname  = ecardJson.getString("lastName");
						String bill_to_phone  = ecardJson.getString("countryCode") + ecardJson.getString("billPayeeMobile");*/
						/*jsonObject.put("bill_to_address_city", bill_to_address_city);
						jsonObject.put("bill_to_address_country", bill_to_address_country);
						jsonObject.put("bill_to_address_line1", bill_to_address_line1);
						jsonObject.put("bill_to_address_state", bill_to_address_state);
						jsonObject.put("bill_to_email", bill_to_email);
						jsonObject.put("bill_to_forename", bill_to_forename);
						jsonObject.put("bill_to_surname", bill_to_surname);
						jsonObject.put("bill_to_phone", bill_to_phone);*/
						

						Boolean debitAccountTrue = false;
						Boolean debitCardTrue = false;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
						Transaction transaction = new Transaction(transactionRef, null,
								null, false, false,
								orderId, null, Channel.VISA_MASTERCARD_WEB, new Date(), ServiceType.DEBIT_CARD, null,
								null, null, TransactionStatus.PENDING, ProbasePayCurrency.ZMW, TransactionCode.transactionPending,
								null, null, device_, false, messageRequest,
								messageResponse,  fixedCharge, transactionCharge, transactionPercentage,
								null,  null, amount, TransactionCode.transactionPending, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(), null, null, 
								null, null, null, null, merchant.getId(),
								Channel.VISA_MASTERCARD_WEB, transactionDetail, null,  null,  null,
								null, customdata, null, null, acquirer.getId(), 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, null, (Integer)device.get("switchToLive"));
						transaction = (Transaction)swpService.createNewRecord(transaction);

						
						jsonObject.put("transaction_uuid", transactionRef);
						
						//String signed_field_names = "access_key,profile_id,transaction_uuid,signed_field_names,unsigned_field_names,signed_date_time,locale,transaction_type,reference_number,amount,currency";
						String signed_field_names = "access_key,profile_id,transaction_uuid,signed_field_names,unsigned_field_names,signed_date_time,locale,transaction_type,reference_number,amount,currency";
						String unsigned_field_names = "";
						/*unsigned_field_names = unsigned_field_names + "bill_to_address_city,bill_to_address_country,bill_to_address_line1,bill_to_address_state,bill_to_email";
						unsigned_field_names = unsigned_field_names + ",bill_to_forename,bill_to_surname,bill_to_phone";
						unsigned_field_names = unsigned_field_names + "";*/
						SimpleDateFormat sdf = new SimpleDateFormat("Y-MM-dd'T'H:m:s'Z'");
						String signed_date_time = sdf.format(transaction.getTransactionDate()); 
						String transaction_type = null;
						if(isDeviceLive!=null && isDeviceLive.equals(Boolean.TRUE))
							transaction_type = "authorization";
						else
							transaction_type = "sale";
						
						//String transaction_type = "sale";
						String reference_number = merchant.getMerchantCode() + "-" + transaction.getOrderRef();
						
						
						
						
						jsonObject.put("switch_to_live", (isDeviceLive!=null && isDeviceLive.equals(Boolean.TRUE)) ? 1 : 0);
						//jsonObject.put("access_key", "ec5891c2e1513fc5a62054abca70593f");
						//jsonObject.put("profile_id", "E00109B5-665F-4696-A37D-96FAA1C718E5");
						jsonObject.put("signed_field_names", signed_field_names);
						jsonObject.put("unsigned_field_names", unsigned_field_names);
						jsonObject.put("signed_date_time", signed_date_time);
						jsonObject.put("locale", locale);
						jsonObject.put("transaction_type", transaction_type);
						jsonObject.put("reference_number", reference_number);
						jsonObject.put("amount", amount);
						jsonObject.put("currency", currency);
						String toSignData = "";

						//String[] signFields = signed_field_names.split(",");
						//for(int m=0; m<signFields.length; m++)
						//{
							//toSignData = toSignData + signFields[m] + "=" + jsonObject.getString(signFields[m]) + ",";
						//}
						//toSignData = toSignData.substring(0, (toSignData.length()-1));
						//String signature = UtilityHelper.hash_hmac(toSignData, app.cyberSourceSecretKey);
						//System.out.println("Signature = " + signature);
						//jsonObject.put("signature", signature);
						//System.out.println("1Signature = " + signature);
						if(customdata!=null)
							jsonObject.put(("customdata"), customdata);
						
						jsonObject.put("status", ERROR.PAYMENT_INITIATION_SUCCESS);
						System.out.println("1Signature = " + 1);
						jsonObject.put("message", "Transaction initiated succcessfully");
						System.out.println("1Signature = " + 2);
						jsonObject.put("txnRef", transaction.getTransactionRef());
						System.out.println("1Signature = " + 3);
						jsonObject.put("orderId", orderId);
						System.out.println("1Signature = " + 4);
						jsonObject.put("merchantId", merchantId);
						System.out.println("1Signature = " + 5);
						jsonObject.put("deviceCode", deviceCode);
						System.out.println("1Signature = " + 6);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getTransactionDate()));
						System.out.println("1Signature = " + 7);
						System.out.println("1Signature = " + 20);
						System.out.println("1Signature = " + 21);
						jsonObject.put("currency", currency);
						
						
						
						
						
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					else
					{
						//validation Failed
						String status = (String)validate.keys().next();
						jsonObject.put("status", status);
						jsonObject.put("message", validate.getString(status));
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						
						jsonObject.put("autoReturnToMerchant", 
								(device.get("isAutoReturnToMerchantSite")!=null && ((Boolean)device.get("isAutoReturnToMerchantSite")).equals(Boolean.TRUE)) ? 1 : 0);
						System.out.println("1Signature = " + 19);
						jsonObject.put("returnUrl", 
								(device.get("isAutoReturnToMerchantSite")!=null && ((Boolean)device.get("isAutoReturnToMerchantSite")) ? 
										device.get("successUrl") : (String)device.get("manualReturnUrlLink")));
						return Response.status(200).entity(jsonObject.toString()).build();
					}
				}
				else
				{
					//device/merchant combination is wrong
					jsonObject.put("status", ERROR.MERCHANT_PLUS_DEVICE_STATUS_FAIL);
					jsonObject.put("message", "Merchant Code & Device Code Are Invalid");
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
			}
			else
			{

				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete/Invalid parameters provided");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.debug(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	
	
	
	@POST
	@Path("/finishCyberSourcePaymentV2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response finishCyberSourcePaymentV2(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("responseData") String responseData)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "System Error Encountered.");
			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String staff_bank_code = "PROBASE";
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			System.out.println("bankKey=> " + bankKey);
			
			if(responseData!=null)
			{
				String responseDataEnc = responseData;
				responseData = (String)UtilityHelper.decryptData(responseData, bankKey);
				System.out.println("responseData = " + responseData);
				JSONObject response = new JSONObject(responseData);
				System.out.println("response.getString(decision)===" + response.getString("decision"));
				
				if(response.has("decision") && response.getString("decision").toUpperCase().equals("DECLINE"))
				{
					String transactionRef = response.getString("req_transaction_uuid");
					System.out.println("transactionRef = " + transactionRef);
					
					String[] req_reference_number = response.getString("req_reference_number").split("-");
					System.out.println("req_reference_number = " + response.getString("req_reference_number"));
					String req_amount = response.getString("req_amount");
					System.out.println("req_amount = " + req_amount);
					
					

					String orderId = req_reference_number[1];
					
					String req_bill_to_surname = response.getString("req_bill_to_surname");
					String req_bill_to_forename = response.getString("req_bill_to_forename");
					String req_bill_to_email = response.getString("req_bill_to_email");
					String req_bill_to_phone = response.getString("req_bill_to_phone");
					String req_bill_to_address_city = response.getString("req_bill_to_address_city");
					String req_bill_to_address_country = response.getString("req_bill_to_address_country");
					String req_bill_to_address_state = response.getString("req_bill_to_address_state");
					String req_bill_to_address_line1 = response.getString("req_bill_to_address_line1");
					String reason = response.getString("message");
					String req_card_number = response.getString("req_card_number");
					
						
					String hql = "Select tp from Transaction tp WHERE tp.orderRef = '" + orderId + "' AND tp.transactionRef = '"+transactionRef+"' AND tp.amount = " + Double.valueOf(req_amount) + "" +
							" AND tp.status = " + TransactionStatus.PENDING.ordinal();
					System.out.println("hql = " + hql);
					Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
					if(transaction!=null)
					{
						System.out.println("1 = " + 1);
						
						transaction.setTransactionDetail("DR CARD|CYBERSOURCE|" + (transaction.getMerchantId()!=null ? (transaction.getMerchantName()+"|") : "") + orderId + "|" + transaction.getAmount());
						transaction.setStatus(TransactionStatus.FAIL);
						transaction.setUpdated_at(new Date());
						
						transaction.setReceipientChannel(Channel.VISA_MASTERCARD_WEB);
						if(transaction.getMerchantId()!=null)
							transaction.setTransactionDetail("Debit Failed: " + transaction.getProbasePayCurrency().name() + req_amount + " | Merchant Account #" + transaction.getDevice().getMerchant().getCompanyName());
						else
							transaction.setTransactionDetail("Debit Failed: " + transaction.getProbasePayCurrency().name() + req_amount);
						
						
						transaction.setReceipientEntityId(transaction.getMerchantId());
						transaction.setResponseData(responseDataEnc);
						this.swpService.updateRecord(transaction);


						
						
						
						jsonObject.put("billingPhone", req_bill_to_phone);
						jsonObject.put("billingFirstName", req_bill_to_forename);
						jsonObject.put("billingLastName", req_bill_to_surname);
						jsonObject.put("billingEmail", req_bill_to_email);
						jsonObject.put("billingMobileNumber", req_bill_to_phone);
						jsonObject.put("billingCity", req_bill_to_address_city);
						jsonObject.put("billingAddressLine1", req_bill_to_address_line1);
						jsonObject.put("billingProvinceState", req_bill_to_address_state);
						jsonObject.put("billingCountryCode", req_bill_to_address_country);
						jsonObject.put("channel", transaction.getChannel().name());
						jsonObject.put("message", "Transaction payment failed");
						jsonObject.put("txnRef", transaction.getTransactionRef());
						jsonObject.put("orderId", orderId);
						jsonObject.put("customdata", transaction.getCustomData());
						jsonObject.put("merchantId", req_reference_number[0]);
						jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
						jsonObject.put("amount", transaction.getAmount());
						jsonObject.put("transactionRef", transactionRef);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						jsonObject.put("status", ERROR.DEBIT_FAILED);
						jsonObject.put("message", "Payment was not successful. Please try again");
			    		jsonObject.put("txnRef", transactionRef);
						jsonObject.put("orderId", orderId);
						JSONObject cardInfo = new JSONObject();
							cardInfo.put("serialNo", req_card_number);
							cardInfo.put("cardHolder", req_bill_to_forename + " " + req_bill_to_surname);
							cardInfo.put("acquirer", JSONObject.NULL);
							cardInfo.put("amountDebited", transaction.getAmount());
							cardInfo.put("transactionRef", transactionRef);
							cardInfo.put("orderRef", orderId);
							cardInfo.put("failReason", reason);
							cardInfo.put("responseMessage", reason);
						jsonObject.put("cardInfo", cardInfo);
						jsonObject.put("merchantId", transaction.getMerchantCode());
						jsonObject.put("merchantName", transaction.getMerchantName());
						jsonObject.put("channel", transaction.getChannel().name());
						jsonObject.put("customerMobileNumber", req_bill_to_phone);
						jsonObject.put("customerEmailAddress", req_bill_to_email);
						jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						jsonObject.put("autoReturnToMerchant", 
								(transaction.getDevice().getFailureUrl()!=null ? 1 : 0));
						jsonObject.put("returnUrl", 
								(transaction.getDevice().getFailureUrl()!=null ? transaction.getDevice().getFailureUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink()));
					}
					else
					{
						//transaction does not exist
						System.out.println("Test = Transaction not found");
						jsonObject.put("txnRef", transactionRef);
						jsonObject.put("merchantId", req_reference_number[0]);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						jsonObject.put("billingPhone", req_bill_to_phone);
						jsonObject.put("billingFirstName", req_bill_to_forename);
						jsonObject.put("billingLastName", req_bill_to_surname);
						jsonObject.put("billingEmail", req_bill_to_email);
						jsonObject.put("billingMobileNumber", req_bill_to_phone);
						jsonObject.put("billingCity", req_bill_to_address_city);
						jsonObject.put("billingAddressLine1", req_bill_to_address_line1);
						jsonObject.put("billingProvinceState", req_bill_to_address_state);
						jsonObject.put("billingCountryCode", req_bill_to_address_country);
						jsonObject.put("channel", transaction.getChannel().name());
						jsonObject.put("message", "Transaction payment failed");
						jsonObject.put("orderId", orderId);
						jsonObject.put("customdata", transaction.getCustomData());
						jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
						jsonObject.put("amount", transaction.getAmount());
						jsonObject.put("transactionRef", transactionRef);
						jsonObject.put("status", ERROR.DEBIT_FAILED);
						jsonObject.put("message", "Payment was not successful. Please try again");

			    		jsonObject.put("txnRef", transactionRef);
						jsonObject.put("orderId", orderId);
						JSONObject cardInfo = new JSONObject();
							cardInfo.put("serialNo", req_card_number);
							cardInfo.put("cardHolder", req_bill_to_forename + " " + req_bill_to_surname);
							cardInfo.put("acquirer", JSONObject.NULL);
							cardInfo.put("amountDebited", transaction.getAmount());
							cardInfo.put("transactionRef", transactionRef);
							cardInfo.put("orderRef", orderId);
							cardInfo.put("failReason", reason);
							cardInfo.put("responseMessage", reason);
						jsonObject.put("cardInfo", cardInfo);
						jsonObject.put("merchantId", transaction.getMerchantCode());
						jsonObject.put("merchantName", transaction.getMerchantName());
						jsonObject.put("channel", transaction.getChannel().name());
						jsonObject.put("customerMobileNumber", req_bill_to_phone);
						jsonObject.put("customerEmailAddress", req_bill_to_email);
						jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
						jsonObject.put("autoReturnToMerchant", 
								(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
								&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
						jsonObject.put("returnUrl", 
								(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
								&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
								transaction.getDevice().getFailureUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink());
						
					}
				}
				else if(response.has("decision") && response.getString("decision").equals("CANCEL"))
				{
					String transactionRef = response.getString("req_transaction_uuid");
					System.out.println("transactionRef = " + transactionRef);
					
					String[] req_reference_number = response.getString("req_reference_number").split("-");
					System.out.println("req_reference_number = " + response.getString("req_reference_number"));
					String req_amount = response.getString("req_amount");
					System.out.println("req_amount = " + req_amount);
					
					

					String orderId = req_reference_number[1];
					System.out.println("req_amount = " + req_amount);
					String req_bill_to_surname = response.getString("req_bill_to_surname");
					String req_bill_to_forename = response.getString("req_bill_to_forename");
					String req_bill_to_email = response.getString("req_bill_to_email");
					String req_bill_to_phone = response.getString("req_bill_to_phone");
					String req_bill_to_address_city = response.getString("req_bill_to_address_city");
					String req_bill_to_address_country = response.getString("req_bill_to_address_country");
					String req_bill_to_address_state = response.getString("req_bill_to_address_state");
					String req_bill_to_address_line1 = response.getString("req_bill_to_address_line1");
					String reason = response.getString("message");
					
					
						
					String hql = "Select tp from Transaction tp WHERE tp.orderRef = '" + orderId + "' AND tp.transactionRef = '"+transactionRef+"' AND tp.amount = " + Double.valueOf(req_amount) + "" +
							" AND tp.status = " + TransactionStatus.PENDING.ordinal();
					System.out.println("hql = " + hql);
					Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
					if(transaction!=null)
					{
						System.out.println("1 = " + 1);
						
						transaction.setResponseData(responseData);
						transaction.setStatus(TransactionStatus.CUSTOMER_CANCELED);
						transaction.setUpdated_at(new Date());
						
						transaction.setReceipientChannel(Channel.VISA_MASTERCARD_WEB);
						if(transaction.getMerchantId()!=null)
							transaction.setTransactionDetail("Debit Cancel: " + transaction.getProbasePayCurrency().name() + req_amount + " | Merchant Account #" + transaction.getDevice().getMerchant().getCompanyName());
						else
							transaction.setTransactionDetail("Debit Cancel: " + transaction.getProbasePayCurrency().name() + req_amount);
						
						transaction.setReceipientEntityId(transaction.getMerchantId());
						this.swpService.updateRecord(transaction);

					}
					//transaction does not exist
					jsonObject.put("txnRef", transactionRef);
					jsonObject.put("merchantId", req_reference_number[0]);
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					jsonObject.put("channel", transaction.getChannel().name());
					jsonObject.put("message", "Transaction payment canceled");
					jsonObject.put("orderId", orderId);
					jsonObject.put("customdata", transaction.getCustomData());
					jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
					jsonObject.put("amount", transaction.getAmount());
					jsonObject.put("transactionRef", transactionRef);
					jsonObject.put("status", ERROR.PAYMENT_TRANSACTION_PAYER_CANCELED);
					jsonObject.put("message", "Payment was not successful. Payment canceled by originator");

		    		jsonObject.put("txnRef", transactionRef);
					jsonObject.put("orderId", orderId);
					JSONObject cardInfo = new JSONObject();
					jsonObject.put("cardInfo", cardInfo);
					jsonObject.put("merchantId", transaction.getMerchantCode());
					jsonObject.put("merchantName", transaction.getMerchantName());
					jsonObject.put("channel", transaction.getChannel().name());
					jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
					jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
					jsonObject.put("autoReturnToMerchant", 
							(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
							&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
					jsonObject.put("returnUrl", 
							(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
							&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
							transaction.getDevice().getFailureUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink());
						
					
				}
				else if(response.has("decision") && response.getString("decision").toUpperCase().equals("ACCEPT")) 
				{
					String transactionRef = response.getString("req_transaction_uuid");
					System.out.println("transactionRef = " + transactionRef);					
					String req_card_number = response.getString("req_card_number");
					String[] req_reference_number = response.getString("req_reference_number").split("-");
					System.out.println("req_reference_number = " + response.getString("req_reference_number"));
					String req_amount = response.getString("req_amount");
					System.out.println("req_amount = " + req_amount);
					
					
					System.out.println("req_amount = " + req_amount);
					String req_bill_to_surname = response.getString("req_bill_to_surname");
					String req_bill_to_forename = response.getString("req_bill_to_forename");
					String req_bill_to_email = response.getString("req_bill_to_email");
					String req_bill_to_phone = response.getString("req_bill_to_phone");
					String req_bill_to_address_city = response.getString("req_bill_to_address_city");
					String req_bill_to_address_country = response.getString("req_bill_to_address_country");
					String req_bill_to_address_state = response.getString("req_bill_to_address_state");
					String req_bill_to_address_line1 = response.getString("req_bill_to_address_line1");
					String reason = response.getString("message");
					
					

					String orderId = req_reference_number[1];
					System.out.println("orderId = " + orderId);
					
						
					String hql = "Select tp from Transaction tp WHERE tp.orderRef = '" + orderId + "' AND tp.transactionRef = '"+transactionRef+"' AND tp.amount = " + Double.valueOf(req_amount) + "" +
							" AND tp.status = " + TransactionStatus.PENDING.ordinal();
					System.out.println(hql);
					Transaction transaction = (Transaction)this.swpService.getUniqueRecordByHQL(hql);
					if(transaction!=null)
					{
						if(response.has("reason_code") && 
								(response.getString("reason_code").equals("100") || response.getString("reason_code").equals("110")))
						{
							System.out.println(1);
							transaction.setTransactionDetail(responseData);
							System.out.println(responseData);
							transaction.setStatus(TransactionStatus.SUCCESS);
							System.out.println(2);
							transaction.setUpdated_at(new Date());
							System.out.println(3);
							transaction.setReceipientChannel(Channel.VISA_MASTERCARD_WEB);
							System.out.println(4);
							if(transaction.getMerchantId()!=null)
								transaction.setTransactionDetail("Debit Success: " + transaction.getProbasePayCurrency().name() + req_amount + " | Merchant Account #" + transaction.getDevice().getMerchant().getCompanyName());
							else
								transaction.setTransactionDetail("Debit Success: " + transaction.getProbasePayCurrency().name() + req_amount);
							
							transaction.setReceipientEntityId(transaction.getMerchantId());
							System.out.println(transaction.getMerchantId());
							transaction.setUpdated_at(new Date());

							System.out.println(0);
							this.swpService.updateRecord(transaction);


							
							
							
							jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL);
							jsonObject.put("message", "Payment was successful");
							jsonObject.put("txnRef", transaction.getTransactionRef());
							jsonObject.put("orderId", orderId);
							jsonObject.put("merchantId", transaction.getMerchantCode());
							jsonObject.put("merchantName", transaction.getMerchantName());
							jsonObject.put("channel", transaction.getChannel().name());
							jsonObject.put("customerMobileNumber", req_bill_to_phone);
							jsonObject.put("customerEmailAddress", req_bill_to_email);
							jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
							jsonObject.put("amount", transaction.getAmount());
							jsonObject.put("transactionRef", transactionRef);
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getUpdated_at()));
								JSONObject cardInfo = new JSONObject();
								cardInfo.put("serialNo", req_card_number);
								cardInfo.put("cardHolder", req_bill_to_forename + " " + req_bill_to_surname);
								cardInfo.put("acquirer", JSONObject.NULL);
								cardInfo.put("amountDebited", transaction.getAmount());
								cardInfo.put("transactionRef", transactionRef);
								cardInfo.put("orderRef", orderId);
								cardInfo.put("failReason", reason);
								cardInfo.put("responseMessage", reason);
								cardInfo.put("billingPhone", req_bill_to_phone);
								cardInfo.put("billingFirstName", req_bill_to_forename);
								cardInfo.put("billingLastName", req_bill_to_surname);
								cardInfo.put("billingEmail", req_bill_to_email);
								cardInfo.put("billingMobileNumber", req_bill_to_phone);
								cardInfo.put("billingCity", req_bill_to_address_city);
								cardInfo.put("billingAddressLine1", req_bill_to_address_line1);
								cardInfo.put("billingProvinceState", req_bill_to_address_state);
								cardInfo.put("billingCountryCode", req_bill_to_address_country);
							jsonObject.put("cardInfo", cardInfo);
							jsonObject.put("customdata", transaction.getCustomData());
							jsonObject.put("amountDebited", transaction.getAmount());
							jsonObject.put("autoReturnToMerchant", 
									(transaction.getDevice().getSuccessUrl()!=null ? 1 : 0));
							jsonObject.put("returnUrl", 
									(transaction.getDevice().getSuccessUrl()!=null ? transaction.getDevice().getSuccessUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink()));
							
						}else
						{
							jsonObject.put("status", ERROR.DEBIT_FAILED);
							jsonObject.put("message", "Payment was not successful. Please try again");
							jsonObject.put("txnRef", transaction.getTransactionRef());
							jsonObject.put("orderId", orderId);
							jsonObject.put("merchantId", transaction.getMerchantCode());
							jsonObject.put("merchantName", transaction.getMerchantName());
							jsonObject.put("channel", transaction.getChannel().name());
							jsonObject.put("customerMobileNumber", req_bill_to_phone);
							jsonObject.put("customerEmailAddress", req_bill_to_email);
							jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
							jsonObject.put("amount", transaction.getAmount());
							jsonObject.put("transactionRef", transactionRef);
							jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getUpdated_at()));
							JSONObject cardInfo = new JSONObject();
								cardInfo.put("serialNo", req_card_number);
								cardInfo.put("cardHolder", req_bill_to_forename + " " + req_bill_to_surname);
								cardInfo.put("acquirer", JSONObject.NULL);
								cardInfo.put("amountDebited", transaction.getAmount());
								cardInfo.put("transactionRef", transactionRef);
								cardInfo.put("orderRef", orderId);
								cardInfo.put("failReason", reason);
								cardInfo.put("responseMessage", reason);
								cardInfo.put("billingPhone", req_bill_to_phone);
								cardInfo.put("billingFirstName", req_bill_to_forename);
								cardInfo.put("billingLastName", req_bill_to_surname);
								cardInfo.put("billingEmail", req_bill_to_email);
								cardInfo.put("billingMobileNumber", req_bill_to_phone);
								cardInfo.put("billingCity", req_bill_to_address_city);
								cardInfo.put("billingAddressLine1", req_bill_to_address_line1);
								cardInfo.put("billingProvinceState", req_bill_to_address_state);
								cardInfo.put("billingCountryCode", req_bill_to_address_country);
							jsonObject.put("cardInfo", cardInfo);
							jsonObject.put("customdata", transaction.getCustomData());
							jsonObject.put("autoReturnToMerchant", 
									(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null
									&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
							jsonObject.put("returnUrl", 
									(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
									&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
											transaction.getDevice().getFailureUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink());
						}
					}
					else
					{
						//transaction does not exist						
						jsonObject.put("status", ERROR.DEBIT_FAILED);
						jsonObject.put("message", "Payment was not successful. Please try again");
						jsonObject.put("txnRef", transaction.getTransactionRef());
						jsonObject.put("orderId", orderId);
						jsonObject.put("merchantId", transaction.getMerchantCode());
						jsonObject.put("merchantName", transaction.getMerchantName());
						jsonObject.put("channel", transaction.getChannel().name());
						jsonObject.put("customerMobileNumber", req_bill_to_phone);
						jsonObject.put("customerEmailAddress", req_bill_to_email);
						jsonObject.put("deviceCode", transaction.getDevice().getDeviceCode());
						jsonObject.put("amount", transaction.getAmount());
						jsonObject.put("transactionRef", transactionRef);
						jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(transaction.getUpdated_at()));
						JSONObject cardInfo = new JSONObject();
							cardInfo.put("serialNo", req_card_number);
							cardInfo.put("cardHolder", req_bill_to_forename + " " + req_bill_to_surname);
							cardInfo.put("acquirer", JSONObject.NULL);
							cardInfo.put("amountDebited", transaction.getAmount());
							cardInfo.put("transactionRef", transactionRef);
							cardInfo.put("orderRef", orderId);
							cardInfo.put("failReason", reason);
							cardInfo.put("responseMessage", reason);
							cardInfo.put("billingPhone", req_bill_to_phone);
							cardInfo.put("billingFirstName", req_bill_to_forename);
							cardInfo.put("billingLastName", req_bill_to_surname);
							cardInfo.put("billingEmail", req_bill_to_email);
							cardInfo.put("billingMobileNumber", req_bill_to_phone);
							cardInfo.put("billingCity", req_bill_to_address_city);
							cardInfo.put("billingAddressLine1", req_bill_to_address_line1);
							cardInfo.put("billingProvinceState", req_bill_to_address_state);
							cardInfo.put("billingCountryCode", req_bill_to_address_country);
						jsonObject.put("cardInfo", cardInfo);
						jsonObject.put("customdata", transaction.getCustomData());
						jsonObject.put("autoReturnToMerchant", 
								(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null
								&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 1 : 0);
						jsonObject.put("returnUrl", 
								(transaction.getDevice().getMerchant().isAutoReturnToMerchantSite()!=null 
								&& transaction.getDevice().getMerchant().isAutoReturnToMerchantSite().equals(Boolean.TRUE)) ? 
										transaction.getDevice().getFailureUrl() : transaction.getDevice().getMerchant().getManualReturnUrlLink());
					}
				}//reason code and decision fail/error
							
			}
			else
			{
				//responseData not provided
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				jsonObject.put("status", ERROR.DATA_INCONSISTENCY);
				jsonObject.put("message", "Invalid data provided to service method!");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
				
		}catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			String walletObject = "{\"responseData\": "+  responseData + "}";
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}


	
	
}

