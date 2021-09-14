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
import com.probase.probasepay.enumerations.BankStaffStatus;
import com.probase.probasepay.enumerations.BillType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.SourceType;
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
import com.probase.probasepay.models.BankBranch;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.FundsTransfer;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.models.UtilityPurchased;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/WalletServicesV2")
public class WalletServicesV2 {
	private static Logger log = Logger.getLogger(WalletServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	

	
	@POST
	@Path("/fundWalletFromWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundWalletFromWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("channel") String channel,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("otp") String otp,
			@FormParam("hash") String hash,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("walletReceipient") String walletReceipient, 
			@FormParam("narration") String narration_)
	{
		JSONObject resp = new JSONObject();
		try
		{

			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String accountIdentifier_ = (String)UtilityHelper.decryptData(accountIdentifier, bankKey);
			String walletReceipient_ = (String)UtilityHelper.decryptData(walletReceipient, bankKey);
			resp.put("accountIdentifier", accountIdentifier_);
			
			ServiceType serviceType = ServiceType.FT_WALLET_TO_WALLET;
			String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
			String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" +accountIdentifier_  + "~" + walletReceipient_ + "~" + transactionRef;
			Response res = transferWalletToWallet(httpHeaders,
					requestContext,
					token, 
					amount, 
					accountIdentifier, 
					merchantId,
					deviceCode,
					channel,
					orderRef,
					otpRef,
					otp,
					hash,
					serviceTypeId,
					walletReceipient, 
					narration_, 
					narration,
					serviceType, 
					transactionRef, 
					"Transfer Money to Wallet",
					null);
			return res;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return Response.status(200).entity(resp.toString()).build();
		}
	}
	
	
	
	
	private Response transferWalletToWallet(HttpHeaders httpHeaders,
			HttpServletRequest requestContext,
			String token, 
			Double amount, 
			String accountIdentifier, 
			String merchantId,
			String deviceCode,
			String channel,
			String orderRef,
			String otpRef,
			String otp,
			String hash,
			String serviceTypeId,
			String walletReceipient, 
			String narration_,
			String narration,
			ServiceType serviceType, 
			String transactionRef,
			String detail, 
			Transaction transaction)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String accountIdentifier_ = (String)UtilityHelper.decryptData(accountIdentifier, bankKey);
			String walletReceipient_ = (String)UtilityHelper.decryptData(walletReceipient, bankKey);
			resp.put("accountIdentifier", accountIdentifier_);

			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+accountIdentifier_+"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal()  +
					 " AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account sourceAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+walletReceipient_+"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal()  +
					" AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account receipientAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			
			if(sourceAccount!=null && receipientAccount!=null)
			{
				log.info("card founr...true");
				if(receipientAccount.getCustomer().getId()==user.getId())
				{
					log.info("receipient wallet not found");
					
					resp.put("status", ERROR.WRONG_API_USED);
					resp.put("message", "You can not transfer funds to your wallet from another wallet using this channel.");
					
					return Response.status(200).entity(resp.toString()).build();
				}
				
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				String rpin = null;
				Date transactionDate = new Date();
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = receipientAccount.getId();
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				
				String transactionDetail = narration_;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				
				AccountServicesV2 asv = new AccountServicesV2();
				Response balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
				JSONObject balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
				Double walletBalance= 0.00;
				if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
				{
					String accountList = balanceDet.getString("accountList");
					log.info("accountList ---" + accountList);
					JSONArray accounts = new JSONArray(accountList);
					for(int i=0; i<accounts.length(); i++)
					{
						JSONObject acct_ = accounts.getJSONObject(i);
						log.info("acct_ ==" + acct_.toString());
						//JSONObject acct_ = accounts;
						Double currentbalance = acct_.getDouble("currentbalance");
						log.info("currentbalance --  " + currentbalance);
						Double availablebalance = acct_.getDouble("availablebalance");
						log.info("availablebalance --  " + availablebalance);
						walletBalance = walletBalance + currentbalance;
					}
					
				}
				
				Double walletMinimumBalance = sourceAccount.getAccountScheme().getMinimumBalance();
				String mobileNo = sourceAccount.getCustomer().getContactMobile();
				String userEmail = sourceAccount.getCustomer().getContactEmail();
				String zauthKey = "";
				
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					//parametersRequest.put("serviceKey", acquirer.getServiceKey());
					//header.put("authKey", acquirer.getAuthKey());
					zauthKey = device.getZicbAuthKey();
				}
				else
				{
					zauthKey = device.getZicbDemoAuthKey();
				}
				
				
				
				if(walletBalance<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your wallet to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				hql = "Select tp from ECard tp where tp.account.id = " + sourceAccount.getId() + " AND tp.deleted_at IS NULL" +
						" AND tp.isLive = " + device.getSwitchToLive();
				Collection<ECard> accountCards = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
				Iterator<ECard> iterCard = accountCards.iterator();
				Double amountOnCards = 0.00;
				while(iterCard.hasNext())
				{
					ECard cd = iterCard.next();
					amountOnCards = amountOnCards + cd.getCardBalance();
				}
				
				
				log.info("otpRef..." + otpRef);
				log.info("otp..." + otp);
				
				//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
				//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
				if(1==1)
				{
					log.info(amount);
					log.info(walletMinimumBalance);
					log.info(walletBalance);
					if((amount+walletMinimumBalance)<=(walletBalance - amountOnCards))
					{
						Boolean creditAccountTrue = true;
						Boolean creditCardTrue = false;
						Boolean debitAccountTrue = true;
						Boolean debitCardTrue = false;
						Long creditAccountId = (Long)receipientAccount.getId();
						Long creditCardId = null;
						Long debitAccountId = sourceAccount.getId();
						Long debitCardId = null;
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						if(transaction==null)
						{
							transaction = new Transaction(transactionRef, bankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, chanel,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, transactionStatus,
								probasePayCurrency, transactionCode,
								sourceAccount, null, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								amount, responseCode, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
								null, null, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
								detail, device.getSwitchToLive());
							transaction = (Transaction)this.swpService.createNewRecord(transaction);
						}
						
						
						if(transaction==null)
						{
							JSONObject transferInfo = new JSONObject();
							resp.put("status", ERROR.INVALID_TRANSACTION);
							resp.put("message", "Funds transferred was not successful. Please try again");
							resp.put("orderRef", orderRef);
							resp.put("receipientInfo", transferInfo.toString());
							return Response.status(200).entity(resp.toString()).build();
						}
						
						String sourceIdentityNo = sourceAccount.getAccountIdentifier();
						String sourceType = SourceType.ACCOUNT.name();
						String sourceCustomerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
						Long sourceCustomerId = sourceAccount.getCustomer().getId();
						String sourceBankName = sourceAccount.getAcquirer().getBank().getBankName();
						Long sourceBankId = sourceAccount.getAcquirer().getBank().getId();
						String receipientIdentityNo = receipientAccount.getAccountIdentifier();
						String receipientType = SourceType.ACCOUNT.name();
						String receipientCustomerName = receipientAccount.getCustomer().getFirstName() + " " + receipientAccount.getCustomer().getLastName();
						Long receipientCustomerId = receipientAccount.getCustomer().getId();
						String receipientBankName = receipientAccount.getAcquirer().getBank().getBankName();
						Long receipientBankId = receipientAccount.getAcquirer().getBank().getId();
						//String narration = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
						Double sourcePriorBalance = walletBalance;
						Double sourceNewBalance = null;
						ServiceType st = serviceType;
						String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
						String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
						Long transactionId = transaction.getId();
						
						
						
						
						
						//UtilityHelper.fundCard(this.swpService, httpHeaders, requestContext, amount, card, account, token, merchantId, deviceCode, user, channel, orderRef, transaction);
						
						/*resp.put("status", ERROR.GENERAL_OK);
						resp.put("message", "Card Funded Successfully");
						resp.put("newBalance", card.getCardBalance());
						resp.put("orderRef", orderRef);
						resp.put("amountTransferred", amount);
						resp.put("receipientInfo", card.getSerialNo());*/
						
						
						
						log.info("INTERNAL FUNDS TRANSFER");
						log.info("===================");
						JSONObject ftRes = UtilityHelper.internalFundsTransfer(app, swpService, httpHeaders, requestContext, amount, sourceAccount, 
								receipientAccount, token, merchantCode, deviceCode, user, channel, orderRef, narration, acquirer, device);
						log.info("FTResponse");
						if(ftRes!=null)
						{
							log.info(ftRes.toString());
							JSONObject allResponse = new JSONObject();
							if(ftRes.has("status") && ftRes.getInt("status")==(ERROR.WALLET_CREDIT_SUCCESS))
							{

								
								


								
								JSONObject jsbreakdown = new JSONObject();
								jsbreakdown.put("Sub-total", amount);
								jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
								
								
								JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), receipientAccount.getCustomer().getFirstName() + " " + receipientAccount.getCustomer().getFirstName(), 
										sourceAccount.getAccountIdentifier(), 
										sourceIdentityNo, transaction.getOrderRef().toUpperCase(), 
										transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
								transaction.setSummary(txnDetails.toString());
								swpService.updateRecord(transaction);
								
								
								
								String bankReferenceNo = ftRes.getString("bankReference");
								String chargeBankPaymentReference = ftRes.has("chargeBankPaymentReference") ? ftRes.getString("chargeBankPaymentReference") : null;
								FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
										sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
										receipientCustomerName, receipientCustomerId, receipientBankName,
										receipientBankId, narration, amount, transactionCharge, fixedCharge,
										sourcePriorBalance, sourceNewBalance, st, channel_,
										transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), 
										bankReferenceNo, chargeBankPaymentReference, device.getSwitchToLive());
								ft = (FundsTransfer)swpService.createNewRecord(ft);
								
								transaction.setTransactionCode(TransactionCode.transactionSuccess);
								transaction.setResponseCode("00");
								transaction.setMessageResponse(allResponse.toString());
								transaction.setStatus(TransactionStatus.SUCCESS);
								swpService.updateRecord(transaction);
								allResponse.put("fundsTransferResponse", ftRes.toString());
								allResponse.put("fundsTransferCreditResponse", ftRes.toString());
								
								balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
								balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
								walletBalance= 0.00;
								Double walletAvailablebalance = 0.00;
								if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
								{
									String accountList = balanceDet.getString("accountList");
									log.info("accountList ---" + accountList);
									JSONArray accounts = new JSONArray(accountList);
									
									
									
									
									Double totalAmount = 0.00;
									for(int i=0; i<accounts.length(); i++)
									{
										
										JSONObject acct_ = accounts.getJSONObject(i);
										
										hql = "Select sum(tp.cardBalance + tp.cardCharges) as totalAmountDeposited, tp.cardBalance as cardBalance, tp.trackingNumber, tp.serialNo, tp.pan, tp.nameOnCard, tp.cvv, date_format(tp.expiryDate, '%m/%y') as expiryDate from ecards tp where "
												+ "tp.account_id = "+sourceAccount.getId()+" Group By tp.id";
										log.info(hql);
										List<Map<String, Object>> cardsListing = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
										if(cardsListing!=null && cardsListing.size()>0)
										{
											for(int i1=0; i1<cardsListing.size(); i1++)
											{
												Map<String, Object> totalAmountMap = cardsListing.get(i1);
												Double amt = (Double)totalAmountMap.get("cardBalance");
												totalAmount = totalAmount + amt;
												log.info(totalAmount);
											}
										}
										
										log.info("acct_ ==" + acct_.toString());
										//JSONObject acct_ = accounts;
										Double currentbalance = acct_.getDouble("currentbalance");
										log.info("currentbalance --  " + currentbalance);
										Double availablebalance = acct_.getDouble("availablebalance");
										log.info("availablebalance --  " + availablebalance);
										walletBalance = walletBalance + currentbalance;
										walletAvailablebalance = walletAvailablebalance + availablebalance - totalAmount;
									}
									
								}
								
									
									
								JSONObject transferInfo = new JSONObject();
									transferInfo.put("recepientCardIdentifier", receipientAccount.getAccountIdentifier());
									transferInfo.put("recepientCustomerName", receipientAccount.getCustomer().getFirstName() + " " + receipientAccount.getCustomer().getLastName());
									transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
									transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
									transferInfo.put("newSourceAccountAvailableBalance", walletAvailablebalance);
									transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
									
									transferInfo.put("amountDebitedFromSource", amount);
									transferInfo.put("sourceWalletCharges", charges);
									transferInfo.put("amountCreditToReceipient", amount);
									transferInfo.put("receipientWalletCharges", 0.00);
									
									transferInfo.put("transactionId", transaction.getId());
								resp.put("status", ERROR.GENERAL_OK);
								resp.put("message", "Funds transferred successfully.");
								resp.put("orderRef", orderRef);
								resp.put("amountTransferred", amount);
								resp.put("receipientInfo", transferInfo.toString());
								
							}
						}
					}
					else
					{
						resp.put("status", ERROR.INSUFFICIENT_FUNDS);
						resp.put("message", "Insufficient funds available in your wallet. Your available balance must exceed the amount you want to transfer");
						resp.put("orderRef", orderRef);
					}
				}
				else
				{
					resp.put("status", ERROR.OTP_CHECK_FAIL);
					resp.put("message", "OTP entered is not valid. Please enter the valid OTP");
					resp.put("orderRef", orderRef);
				}

				
				return Response.status(200).entity(resp.toString()).build();
			}
			
			resp.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
			resp.put("message", "Source Account or destination debit/credit card could not be found. Please try again");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}
	
	
	
	private Response transferWalletToBank(HttpHeaders httpHeaders,
			HttpServletRequest requestContext,
			String token, 
			Double amount, 
			String accountIdentifier, 
			String merchantId,
			String deviceCode,
			String channel,
			String orderRef,
			String otpRef,
			String otp,
			String hash,
			String serviceTypeId,
			String destinationIdentifier, 
			String narration_,
			String narration,
			ServiceType serviceType, 
			String transactionRef,
			String sortCode,
			String bicCode)
	{
		log.info(1);
		JSONObject resp = new JSONObject();
		try
		{
			log.info(2);
			this.swpService = this.serviceLocator.getSwpService();
			log.info(3);
			Application app = Application.getInstance(swpService);
			log.info(4);


			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}
			log.info(5);
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info(6);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			log.info(7);
			String subject = verifyJ.getString("subject");
			log.info(8);
			User user = new Gson().fromJson(subject, User.class);
			log.info(9);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			log.info(10);
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String accountIdentifier_ = accountIdentifier;//(String)UtilityHelper.decryptData(accountIdentifier, bankKey);
			resp.put("accountIdentifier", accountIdentifier_);
			String destinationIdentifier_ = (String)UtilityHelper.decryptData(destinationIdentifier, bankKey);
			resp.put("destinationIdentifier_", destinationIdentifier_);

			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+accountIdentifier_+"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal()  +
					 " AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account sourceAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			
			
			
			if(sourceAccount!=null)
			{
				log.info("card founr...true");
				
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				String rpin = null;
				Date transactionDate = new Date();
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				
				String transactionDetail = narration_;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				
				AccountServicesV2 asv = new AccountServicesV2();
				Response balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
				JSONObject balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
				Double walletBalance= 0.00;
				if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
				{
					String accountList = balanceDet.getString("accountList");
					log.info("accountList ---" + accountList);
					JSONArray accounts = new JSONArray(accountList);
					for(int i=0; i<accounts.length(); i++)
					{
						JSONObject acct_ = accounts.getJSONObject(i);
						log.info("acct_ ==" + acct_.toString());
						//JSONObject acct_ = accounts;
						Double currentbalance = acct_.getDouble("currentbalance");
						log.info("currentbalance --  " + currentbalance);
						Double availablebalance = acct_.getDouble("availablebalance");
						log.info("availablebalance --  " + availablebalance);
						walletBalance = walletBalance + currentbalance;
					}
					
				}
				
				Double walletMinimumBalance = sourceAccount.getAccountScheme().getMinimumBalance();
				String mobileNo = sourceAccount.getCustomer().getContactMobile();
				String userEmail = sourceAccount.getCustomer().getContactEmail();
				String zauthKey = "";
				
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					//parametersRequest.put("serviceKey", acquirer.getServiceKey());
					//header.put("authKey", acquirer.getAuthKey());
					zauthKey = device.getZicbAuthKey();
				}
				else
				{
					zauthKey = device.getZicbDemoAuthKey();
				}
				
				
				
				if(walletBalance<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your wallet to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				hql = "Select tp from ECard tp where tp.account.id = " + sourceAccount.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				Collection<ECard> accountCards = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
				Iterator<ECard> iterCard = accountCards.iterator();
				Double amountOnCards = 0.00;
				while(iterCard.hasNext())
				{
					ECard cd = iterCard.next();
					amountOnCards = amountOnCards + cd.getCardBalance();
				}
				
				
				log.info("otpRef..." + otpRef);
				log.info("otp..." + otp);
				
				

				
				hql = "Select tp from BankBranch tp where tp.sortCode = '"+sortCode+"' AND tp.deleted_at IS NULL";
				BankBranch bankBranch = (BankBranch)this.swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Bank tp where tp.bicCode = '"+bicCode+"' AND tp.deleted_at IS NULL";
				Bank destinationBank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
				
				//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
				//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
				if(1==1)
				{
					log.info(amount);
					log.info(walletMinimumBalance);
					log.info(walletBalance);
					if((amount+walletMinimumBalance)<=(walletBalance - amountOnCards))
					{
						Boolean creditAccountTrue = true;
						Boolean creditCardTrue = false;
						Boolean debitAccountTrue = true;
						Boolean debitCardTrue = false;

						String creditExternalAccountId = destinationIdentifier;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = sourceAccount.getId();
						Long debitCardId = null;
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						
						Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, chanel,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, transactionStatus,
								probasePayCurrency, transactionCode,
								sourceAccount, null, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								amount, responseCode, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
								null, null, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Bank Account", device.getSwitchToLive());
						transaction = (Transaction)this.swpService.createNewRecord(transaction);
						
						
						String sourceIdentityNo = sourceAccount.getAccountIdentifier();
						String sourceType = SourceType.ACCOUNT.name();
						String sourceCustomerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
						Long sourceCustomerId = sourceAccount.getCustomer().getId();
						String sourceBankName = sourceAccount.getAcquirer().getBank().getBankName();
						Long sourceBankId = sourceAccount.getAcquirer().getBank().getId();
						String receipientIdentityNo = destinationIdentifier;
						String receipientType = SourceType.ACCOUNT.name();
						String receipientCustomerName = "";
						Long receipientCustomerId = null;
						String receipientBankName = destinationBank.getBankName();
						Long receipientBankId = destinationBank.getId();
						
						//String narration = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
						Double sourcePriorBalance = walletBalance;
						Double sourceNewBalance = null;
						ServiceType st = serviceType;
						String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
						String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
						Long transactionId = transaction.getId();
						
						
						
						
						
						//UtilityHelper.fundCard(this.swpService, httpHeaders, requestContext, amount, card, account, token, merchantId, deviceCode, user, channel, orderRef, transaction);
						
						/*resp.put("status", ERROR.GENERAL_OK);
						resp.put("message", "Card Funded Successfully");
						resp.put("newBalance", card.getCardBalance());
						resp.put("orderRef", orderRef);
						resp.put("amountTransferred", amount);
						resp.put("receipientInfo", card.getSerialNo());*/
						
						
						log.info("EXTERNAL FUNDS TRANSFER");
						log.info("===================");
						String ipAddress = requestContext.getRemoteAddr();
						//ipAddress = requestContext.getHeader("X-FORWARDED-FOR");  
						log.info(ipAddress);
						//if (ipAddress == null) {  
						      
						    //log.info(ipAddress);
						//}
						
						JSONObject ftRes = UtilityHelper.externalFundsTransfer(app, swpService, httpHeaders,
								requestContext, amount, sourceAccount, destinationIdentifier_, token, merchantCode, deviceCode, 
								user, channel, orderRef, narration, acquirer, device, destinationBank.getBankName(), bicCode, 
								bankBranch.getBranchDetails(), ipAddress);
						log.info("FTResponse");
						if(ftRes!=null)
						{
							log.info(ftRes.toString());
							JSONObject allResponse = new JSONObject();
							if(ftRes.has("status") && ftRes.getInt("status")==(ERROR.WALLET_CREDIT_SUCCESS))
							{

								String bankReferenceNo = ftRes.getString("bankReference");
								String chargeBankPaymentReference = ftRes.has("chargeBankPaymentReference") ? ftRes.getString("chargeBankPaymentReference") : null;
								FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
										sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
										receipientCustomerName, receipientCustomerId, receipientBankName,
										receipientBankId, narration, amount, transactionCharge, fixedCharge,
										sourcePriorBalance, sourceNewBalance, st, channel_,
										transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), 
										bankReferenceNo, chargeBankPaymentReference, device.getSwitchToLive());
								ft = (FundsTransfer)swpService.createNewRecord(ft);
								
								transaction.setTransactionCode(TransactionCode.transactionSuccess);
								transaction.setResponseCode("00");
								transaction.setMessageResponse(ftRes.toString());
								transaction.setStatus(TransactionStatus.SUCCESS);
								
								



								JSONObject jsbreakdown = new JSONObject();
								jsbreakdown.put("Sub-total", amount);
								jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
								
								JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), destinationBank.getBankName(), destinationIdentifier, 
										sourceAccount.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
										transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
								transaction.setSummary(txnDetails.toString());
								swpService.updateRecord(transaction);
								
								
								
								allResponse.put("fundsTransferResponse", ftRes.toString());
								allResponse.put("fundsTransferCreditResponse", ftRes.toString());
								
								balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
								balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
								walletBalance= 0.00;
								Double walletAvailablebalance = 0.00;
								if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
								{
									String accountList = balanceDet.getString("accountList");
									log.info("accountList ---" + accountList);
									JSONArray accounts = new JSONArray(accountList);
									
									
									
									
									Double totalAmount = 0.00;
									for(int i=0; i<accounts.length(); i++)
									{
										
										JSONObject acct_ = accounts.getJSONObject(i);
										
										hql = "Select sum(tp.cardBalance + tp.cardCharges) as totalAmountDeposited, tp.cardBalance as cardBalance, tp.trackingNumber, tp.serialNo, tp.pan, tp.nameOnCard, tp.cvv, date_format(tp.expiryDate, '%m/%y') as expiryDate from ecards tp where "
												+ "tp.account_id = "+sourceAccount.getId()+" Group By tp.id AND tp.isLive = " + device.getSwitchToLive();
										log.info(hql);
										List<Map<String, Object>> cardsListing = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
										if(cardsListing!=null && cardsListing.size()>0)
										{
											for(int i1=0; i1<cardsListing.size(); i1++)
											{
												Map<String, Object> totalAmountMap = cardsListing.get(i1);
												Double amt = (Double)totalAmountMap.get("cardBalance");
												totalAmount = totalAmount + amt;
												log.info(totalAmount);
											}
										}
										
										log.info("acct_ ==" + acct_.toString());
										//JSONObject acct_ = accounts;
										Double currentbalance = acct_.getDouble("currentbalance");
										log.info("currentbalance --  " + currentbalance);
										Double availablebalance = acct_.getDouble("availablebalance");
										log.info("availablebalance --  " + availablebalance);
										walletBalance = walletBalance + currentbalance;
										walletAvailablebalance = walletAvailablebalance + availablebalance - totalAmount;
									}
									
								}
								
									
									
								JSONObject transferInfo = new JSONObject();
								transferInfo.put("recepientAccountIdentifier", destinationIdentifier_);
								transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
								transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
								transferInfo.put("newSourceAccountAvailableBalance", walletAvailablebalance);
								transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
								
								transferInfo.put("amountDebitedFromSource", amount);
								transferInfo.put("sourceWalletCharges", charges);
								transferInfo.put("amountCreditToReceipient", amount);
								transferInfo.put("receipientWalletCharges", 0.00);
								
								transferInfo.put("transactionId", transaction.getId());
								
								resp.put("status", ERROR.GENERAL_OK);
								resp.put("message", "Funds transferred successfully.");
								resp.put("orderRef", orderRef);
								resp.put("amountTransferred", amount);
								resp.put("receipientInfo", transferInfo.toString());
								
							}
							else
							{
								transaction.setTransactionCode(TransactionCode.transactionFail);
								transaction.setResponseCode("02");
								transaction.setMessageResponse(ftRes.toString());
								transaction.setStatus(TransactionStatus.FAIL);
								
								JSONObject transferInfo = new JSONObject();
								transferInfo.put("recepientAccountIdentifier", destinationIdentifier_);
								transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
								transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
								transferInfo.put("newSourceAccountAvailableBalance", walletBalance);
								transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
								
								transferInfo.put("amountDebitedFromSource", amount);
								transferInfo.put("sourceWalletCharges", charges);
								transferInfo.put("amountCreditToReceipient", amount);
								transferInfo.put("receipientWalletCharges", 0.00);
								
								transferInfo.put("transactionId", transaction.getId());
								
								resp.put("status", ERROR.GENERAL_FAIL);
								resp.put("message", "Funds transfer failed.");
								resp.put("orderRef", orderRef);
								resp.put("amountTransferred", amount);
								resp.put("receipientInfo", transferInfo.toString());
							}
						}
						else
						{
							transaction.setTransactionCode(TransactionCode.transactionFail);
							transaction.setResponseCode("02");
							transaction.setMessageResponse(ftRes.toString());
							transaction.setStatus(TransactionStatus.FAIL);

							
							JSONObject transferInfo = new JSONObject();
							transferInfo.put("recepientAccountIdentifier", destinationIdentifier_);
							transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
							transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
							transferInfo.put("newSourceAccountAvailableBalance", walletBalance);
							transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
							
							transferInfo.put("amountDebitedFromSource", amount);
							transferInfo.put("sourceWalletCharges", charges);
							transferInfo.put("amountCreditToReceipient", amount);
							transferInfo.put("receipientWalletCharges", 0.00);
							
							transferInfo.put("transactionId", transaction.getId());
							
							resp.put("status", ERROR.GENERAL_FAIL);
							resp.put("message", "Funds transfer failed.");
							resp.put("orderRef", orderRef);
							resp.put("receipientInfo", transferInfo.toString());
						}
					}
					else
					{
						JSONObject transferInfo = new JSONObject();
						transferInfo.put("recepientAccountIdentifier", destinationIdentifier_);
						transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
						transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
						transferInfo.put("newSourceAccountAvailableBalance", walletBalance);
						transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
						
						transferInfo.put("amountDebitedFromSource", amount);
						transferInfo.put("sourceWalletCharges", charges);
						transferInfo.put("amountCreditToReceipient", amount);
						transferInfo.put("receipientWalletCharges", 0.00);
						
						resp.put("status", ERROR.INSUFFICIENT_FUNDS);
						resp.put("message", "Insufficient funds available in your wallet. Your available balance must exceed the amount you want to transfer");
						resp.put("orderRef", orderRef);
						resp.put("receipientInfo", transferInfo.toString());
					}
				}
				else
				{
					resp.put("status", ERROR.OTP_CHECK_FAIL);
					resp.put("message", "OTP entered is not valid. Please enter the valid OTP");
					resp.put("orderRef", orderRef);
				}

				
				return Response.status(200).entity(resp.toString()).build();
			}
			
			resp.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
			resp.put("message", "Source Account or destination debit/credit card could not be found. Please try again");
			
		}
		catch(Exception e)
		{
			log.error(e);
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}
	
	
	
	
	private Response transferCardToBank(HttpHeaders httpHeaders,
			HttpServletRequest requestContext,
			String token, 
			Double amount, 
			String cardIdentifier, 
			String merchantId,
			String deviceCode,
			String channel,
			String orderRef,
			String otpRef,
			String otp,
			String hash,
			String serviceTypeId,
			String destinationIdentifier, 
			String narration_,
			String narration,
			ServiceType serviceType, 
			String transactionRef,
			String sortCode,
			String bicCode)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String cardInfo = (String)UtilityHelper.decryptData(cardIdentifier, bankKey);
			resp.put("cardInfo", cardInfo);
			String destinationIdentifier_ = (String)UtilityHelper.decryptData(destinationIdentifier, bankKey);
			resp.put("destinationIdentifier_", destinationIdentifier_);
			log.info("cardInfo....." + cardInfo);
			String[] cardInfoArray = cardInfo.split("~");
			
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard sourceCard = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			
			
			if(sourceCard!=null)
			{
				log.info("card founr...true");
				Account sourceAccount_ = sourceCard.getAccount();
				String bankPaymentReference = null;
				Long customerId = sourceAccount_.getCustomer().getId();
				String rpin = null;
				Date transactionDate = new Date();
				String payerName = sourceAccount_.getCustomer().getFirstName() + " " + sourceAccount_.getCustomer().getLastName();
				String payerEmail = sourceAccount_.getCustomer().getContactEmail();
				String payerMobile = sourceAccount_.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount_.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceCard.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				
				String transactionDetail = narration_;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				Double cardMinimumBalance = sourceCard.getCardScheme().getMinimumBalance();
				
				if((sourceCard.getCardBalance() - cardMinimumBalance)<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				AccountServicesV2 asv = new AccountServicesV2();
				
				
				Double walletMinimumBalance = sourceCard.getCardScheme().getMinimumBalance();
				String mobileNo = sourceAccount_.getCustomer().getContactMobile();
				String userEmail = sourceAccount_.getCustomer().getContactEmail();
				String zauthKey = "";
				
				if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					//parametersRequest.put("serviceKey", acquirer.getServiceKey());
					//header.put("authKey", acquirer.getAuthKey());
					zauthKey = device.getZicbAuthKey();
				}
				else
				{
					zauthKey = device.getZicbDemoAuthKey();
				}
				
				
				
				log.info("otpRef..." + otpRef);
				log.info("otp..." + otp);
				
				

				
				hql = "Select tp from BankBranch tp where tp.sortCode = '"+sortCode+"'";
				BankBranch bankBranch = (BankBranch)this.swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Bank tp where tp.id = '"+bankBranch.getBankId()+"'";
				Bank destinationBank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
				
				//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
				//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
				if(1==1)
				{
					log.info(amount);
					log.info(walletMinimumBalance);
					log.info(sourceCard.getCardBalance());
					Boolean creditAccountTrue = false;
					Boolean creditCardTrue = false;
					Boolean debitAccountTrue = false;
					Boolean debitCardTrue = true;

					String creditExternalAccountId = destinationIdentifier;
					Long creditAccountId = null;
					Long creditCardId = null;
					Long debitAccountId = sourceAccount_.getId();
					Long debitCardId = null;
					//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
					
					
					Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
							customerId, creditAccountTrue, creditCardTrue,
							orderRef, rpin, chanel,
							transactionDate, serviceType, payerName,
							payerEmail, payerMobile, transactionStatus,
							probasePayCurrency, transactionCode,
							sourceAccount_, null, device,
							creditPoolAccountTrue, messageRequest,
							messageResponse, fixedCharge,
							transactionCharge, transactionPercentage,
							schemeTransactionCharge, schemeTransactionPercentage,
							amount, responseCode, null, null,
							merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
							null, null, 
							transactingBankId, receipientTransactingBankId,
							accessCode, sourceEntityId, receipientEntityId,
							receipientChannel, transactionDetail,
							closingBalance, totalCreditSum, totalDebitSum,
							paidInByBankUserAccountId, customData,
							responseData, adjustedTransactionId, acquirerId, 
							debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Bank Account", device.getSwitchToLive());
					transaction = (Transaction)this.swpService.createNewRecord(transaction);
					
					
					String sourceIdentityNo = sourceCard.getTrackingNumber();
					String sourceType = SourceType.CARD.name();
					String sourceCustomerName = sourceAccount_.getCustomer().getFirstName() + " " + sourceAccount_.getCustomer().getLastName();
					Long sourceCustomerId = sourceAccount_.getCustomer().getId();
					String sourceBankName = sourceAccount_.getAcquirer().getBank().getBankName();
					Long sourceBankId = sourceAccount_.getAcquirer().getBank().getId();
					String receipientIdentityNo = destinationIdentifier;
					String receipientType = SourceType.ACCOUNT.name();
					String receipientCustomerName = "";
					Long receipientCustomerId = null;
					String receipientBankName = destinationBank.getBankName();
					Long receipientBankId = destinationBank.getId();
					
					//String narration = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
					Double sourcePriorBalance = sourceCard.getCardBalance();
					Double sourceNewBalance = null;
					ServiceType st = serviceType;
					String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
					String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
					Long transactionId = transaction.getId();
					
					
					
					
					
					//UtilityHelper.fundCard(this.swpService, httpHeaders, requestContext, amount, card, account, token, merchantId, deviceCode, user, channel, orderRef, transaction);
					
					/*resp.put("status", ERROR.GENERAL_OK);
					resp.put("message", "Card Funded Successfully");
					resp.put("newBalance", card.getCardBalance());
					resp.put("orderRef", orderRef);
					resp.put("amountTransferred", amount);
					resp.put("receipientInfo", card.getSerialNo());*/
					
					
					log.info("EXTERNAL FUNDS TRANSFER FROM CARD");
					log.info("===================");
					
					String ipAddress = requestContext.getHeader("X-FORWARDED-FOR");  
					if (ipAddress == null) {  
					    ipAddress = requestContext.getRemoteAddr();  
					}
					
					JSONObject ftRes = UtilityHelper.externalFundsTransfer(app, swpService, httpHeaders,
							requestContext, amount, sourceAccount_, destinationIdentifier_, token, merchantCode, deviceCode, 
							user, channel, orderRef, narration, acquirer, device, destinationBank.getBankName(), bicCode, 
							bankBranch.getBranchDetails(), ipAddress);
					log.info("FTResponse");
					if(ftRes!=null)
					{
						log.info(ftRes.toString());
						JSONObject allResponse = new JSONObject();
						if(ftRes.has("status") && ftRes.getInt("status")==(ERROR.WALLET_CREDIT_SUCCESS))
						{
							
							sourceCard = sourceCard.withdraw(this.swpService, amount, charges);
							sourceNewBalance = sourceCard.getCardBalance();
							
							
							String bankReferenceNo = ftRes.getString("bankReference");
							String chargeBankPaymentReference = ftRes.has("chargeBankPaymentReference") ? ftRes.getString("chargeBankPaymentReference") : null;
							FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
									sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
									receipientCustomerName, receipientCustomerId, receipientBankName,
									receipientBankId, narration, amount, transactionCharge, fixedCharge,
									sourcePriorBalance, sourceNewBalance, st, channel_,
									transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), bankReferenceNo, chargeBankPaymentReference, device.getSwitchToLive());
							ft = (FundsTransfer)swpService.createNewRecord(ft);
							
							transaction.setTransactionCode(TransactionCode.transactionSuccess);
							transaction.setResponseCode("00");
							transaction.setMessageResponse(allResponse.toString());
							transaction.setStatus(TransactionStatus.SUCCESS);
							
							
							



							JSONObject jsbreakdown = new JSONObject();
							jsbreakdown.put("Sub-total", amount);
							jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
							
							JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), destinationBank.getBankName(), destinationIdentifier, 
									sourceCard.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
									transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
							transaction.setSummary(txnDetails.toString());
							swpService.updateRecord(transaction);
							
							
							swpService.updateRecord(transaction);
							allResponse.put("fundsTransferResponse", ftRes.toString());
							allResponse.put("fundsTransferCreditResponse", ftRes.toString());
							
							
							JSONObject transferInfo = new JSONObject();
							transferInfo.put("recepientAccountIdentifier", destinationIdentifier_);
							transferInfo.put("sourceCardIdentifier", sourceCard.getTrackingNumber());
							transferInfo.put("sourceCustomerName", sourceAccount_.getCustomer().getFirstName() + " " + sourceAccount_.getCustomer().getLastName());
							transferInfo.put("newSourceCardAvailableBalance", sourceCard.getCardBalance());
							transferInfo.put("newSourceCardCurrentBalance", sourceCard.getCardBalance());
							
							transferInfo.put("amountDebitedFromSource", amount);
							transferInfo.put("sourceCCharges", charges);
							transferInfo.put("amountCreditToReceipient", amount);
							transferInfo.put("receipientWalletCharges", 0.00);
							
							transferInfo.put("transactionId", transaction.getId());
							
							resp.put("status", ERROR.GENERAL_OK);
							resp.put("message", "Funds transferred successfully.");
							resp.put("orderRef", orderRef);
							resp.put("amountTransferred", amount);
							resp.put("receipientInfo", transferInfo.toString());
							
						}
					}
				}
				else
				{
					resp.put("status", ERROR.OTP_CHECK_FAIL);
					resp.put("message", "OTP entered is not valid. Please enter the valid OTP");
					resp.put("orderRef", orderRef);
				}

				
				return Response.status(200).entity(resp.toString()).build();
			}
			
			resp.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
			resp.put("message", "Source Account or destination debit/credit card could not be found. Please try again");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}
	
	
	
	
	@POST
	@Path("/fundCardFromWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundCardFromWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			

			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("channel") String channel,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("otp") String otp,
			@FormParam("hash") String hash,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("cardTrackingId") String cardTrackingId,
			@FormParam("cardSerialNo") String cardSerialNo)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("accountIdentifier", accountIdentifier);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			

			log.info("bankKey....." + bankKey);
			
			String accountIdentifier_ = (String)UtilityHelper.decryptData(accountIdentifier, bankKey);
			log.info("accountIdentifier....." + accountIdentifier);
			cardTrackingId = (String)UtilityHelper.decryptData(cardTrackingId, bankKey);
			log.info("cardTrackingId....." + cardTrackingId);
			cardSerialNo = (String)UtilityHelper.decryptData(cardSerialNo, bankKey);
			log.info("cardSerialNo....." + cardSerialNo);
			
			
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}


			resp.put("accountIdentifier", accountIdentifier_);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues debiting your wallet please try again");
			
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}


			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+accountIdentifier_+"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + AccountStatus.ACTIVE.ordinal() + " AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account sourceAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+ cardTrackingId +"' AND tp.serialNo = '"+ cardSerialNo +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)" + " AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardDestination = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			if(sourceAccount!=null && cardDestination!=null)
			{
				log.info("card founr...true");
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				String rpin = null;
				Date transactionDate = new Date();
				ServiceType serviceType = ServiceType.FT_WALLET_TO_CARD;
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				String oTP = null;
				String oTPRef = null;
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" + sourceAccount.getAccountIdentifier() + "~" + cardDestination.getTrackingNumber() + "~" + transactionRef;
				String transactionDetail = narration;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				
				log.info("SourceAccount id ..." + sourceAccount.getId());
				log.info("SourceAccountScheme id ..." + sourceAccount.getAccountScheme().getId());
				log.info("SourceAccountScheme getMinimumBalance ..." + sourceAccount.getAccountScheme().getMinimumBalance());
				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				Double walletMinimumBalance = sourceAccount.getAccountScheme().getMinimumBalance();
				
				AccountServicesV2 asv = new AccountServicesV2();
				Response balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
				JSONObject balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
				Double walletBalance= 0.00;
				if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
				{
					String accountList = balanceDet.getString("accountList");
					log.info("accountList ---" + accountList);
					JSONArray accounts = new JSONArray(accountList);
					for(int i=0; i<accounts.length(); i++)
					{
						JSONObject acct_ = accounts.getJSONObject(i);
						log.info("acct_ ==" + acct_.toString());
						//JSONObject acct_ = accounts;
						Double currentbalance = acct_.getDouble("currentbalance");
						log.info("currentbalance --  " + currentbalance);
						Double availablebalance = acct_.getDouble("availablebalance");
						log.info("availablebalance --  " + availablebalance);
						walletBalance = walletBalance + currentbalance;
					}
					
				}
				

				log.info("walletBalance --  " + walletBalance);

				log.info("walletMinimumBalance --  " + walletMinimumBalance);

				log.info("amount --  " + amount);

				log.info("charges --  " + charges);
				
				if((walletBalance - walletMinimumBalance)<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				
				
				Boolean debitAccountTrue = true;
				Boolean debitCardTrue = false;
				Boolean creditAccountTrue = false;
				Boolean creditCardTrue = true;
				
				
				Long creditAccountId = null;
				Long creditCardId = null;
				Long debitAccountId = null;
				Long debitCardId = null;
				creditCardId = cardDestination.getId();
				debitAccountId = sourceAccount.getId();
				//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
				
				if(cardDestination.getAccount().getId()==debitAccountId)
				{
					debitAccountTrue = null;
					debitCardTrue = null;
					creditAccountTrue = null;
					creditCardTrue = null;
				}
				
				Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
						customerId, creditAccountTrue, creditCardTrue,
						orderRef, rpin, chanel,
						transactionDate, serviceType, payerName,
						payerEmail, payerMobile, transactionStatus,
						probasePayCurrency, transactionCode,
						sourceAccount, cardDestination, device,
						creditPoolAccountTrue, messageRequest,
						messageResponse, fixedCharge,
						transactionCharge, transactionPercentage,
						schemeTransactionCharge, schemeTransactionPercentage,
						amount, responseCode, oTP, oTPRef,
						merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
						null, null, 
						transactingBankId, receipientTransactingBankId,
						accessCode, sourceEntityId, receipientEntityId,
						receipientChannel, transactionDetail,
						closingBalance, totalCreditSum, totalDebitSum,
						paidInByBankUserAccountId, customData,
						responseData, adjustedTransactionId, acquirerId, 
						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Card", device.getSwitchToLive());
				transaction = (Transaction)this.swpService.createNewRecord(transaction);
				
				

				
				log.info("cardDestination.getAccount().getId()...." + cardDestination.getAccount().getId());
				log.info("debitAccountId....." + debitAccountId);
				
				
				if(cardDestination.getAccount().getId().equals(debitAccountId))
				{
					cardDestination = cardDestination.deposit(this.swpService, amount, charges);
					
					

					String sourceIdentityNo = sourceAccount.getAccountIdentifier();
					String sourceType = SourceType.ACCOUNT.name();
					String sourceCustomerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
					Long sourceCustomerId = sourceAccount.getCustomer().getId();
					String sourceBankName = sourceAccount.getAcquirer().getBank().getBankName();
					Long sourceBankId = sourceAccount.getAcquirer().getBank().getId();
					String receipientIdentityNo = cardDestination.getTrackingNumber();
					String receipientType = SourceType.CARD.name();
					String receipientCustomerName = cardDestination.getNameOnCard();
					Long receipientCustomerId = cardDestination.getAccount().getCustomer().getId();
					String receipientBankName = cardDestination.getAcquirer().getBank().getBankName();
					Long receipientBankId = cardDestination.getAcquirer().getBank().getId();
					String narration_ = "DR~WALLET~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
					Double sourcePriorBalance = walletBalance;
					Double sourceNewBalance = walletBalance;
					ServiceType st = ServiceType.FT_WALLET_TO_CARD;
					String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
					String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
					Long transactionId = transaction.getId();
					String chargeBankRefNo = null;
					
					
					FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
							sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
							receipientCustomerName, receipientCustomerId, receipientBankName,
							receipientBankId, narration, amount, transactionCharge, fixedCharge,
							sourcePriorBalance, sourceNewBalance, st, channel_,
							transactionRef, debitOrderRef, creditOrderRef, transactionId, null, null, null, null, chargeBankRefNo, device.getSwitchToLive());
					ft = (FundsTransfer)swpService.createNewRecord(ft);
					
					ft.setSourceDebitBankReferenceNo(null);
					ft.setSourceResponseData(null);
					this.swpService.updateRecord(ft);
					
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setResponseCode("00");
					transaction.setStatus(TransactionStatus.SUCCESS);
					transaction.setMessageResponse(null);

					JSONObject jsbreakdown = new JSONObject();
					jsbreakdown.put("Sub-total", amount);
					jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
					
					JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), cardDestination.getNameOnCard(), cardDestination.getTrackingNumber(), 
							sourceAccount.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
							transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
					transaction.setSummary(txnDetails.toString());
					swpService.updateRecord(transaction);
					
					
					
					hql = "Select tp from ECard tp where tp.account.id = " + sourceAccount.getId() + " AND tp.deleted_at IS  NULL AND tp.isLive = " + device.getSwitchToLive();
					Collection<ECard> accountCards = (Collection<ECard>)this.swpService.getAllRecordsByHQL(hql);
					Iterator<ECard> iterCard = accountCards.iterator();
					Double totalAmountOnCards = 0.00;
					while(iterCard.hasNext())
					{
						ECard cd = iterCard.next();
						totalAmountOnCards = totalAmountOnCards + cd.getCardBalance();
					}
					
					JSONObject transferInfo = new JSONObject();
					transferInfo.put("recepientCardIdentifier", cardDestination.getTrackingNumber());
					transferInfo.put("recepientCustomerName", cardDestination.getAccount().getCustomer().getFirstName() + " " + cardDestination.getAccount().getCustomer().getLastName());
					transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
					transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
					transferInfo.put("newSourceAccountAvailableBalance", walletBalance - totalAmountOnCards);
					transferInfo.put("newSourceAccountCurrentBalance", walletBalance);
					
					transferInfo.put("amountDebitedFromSource", amount);
					transferInfo.put("sourceWalletCharges", charges);
					transferInfo.put("amountCreditToReceipient", amount);
					transferInfo.put("receipientWalletCharges", 0.00);
					
					resp.put("status", ERROR.GENERAL_OK);
					resp.put("message", "Funds transferred successfully.");
					resp.put("orderRef", orderRef);
					resp.put("amountTransferred", amount);
					resp.put("receipientInfo", transferInfo.toString());
					
					
				}
				else
				{
					Account receipientAccount = cardDestination.getAccount();
					WalletServicesV2 ws = new WalletServicesV2();
					
					String sourceIdentityNo = sourceAccount.getAccountIdentifier();
					String receipientIdentityNo = cardDestination.getTrackingNumber();
					String sourceCustomerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
					String receipientCustomerName = cardDestination.getNameOnCard();
					
					String narration_ = "DR~WALLET~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
					
					Response rs = transferWalletToWallet(httpHeaders,
							requestContext,
							token, 
							amount, 
							accountIdentifier, 
							merchantId,
							deviceCode,
							channel,
							orderRef,
							otpRef,
							otp,
							hash,
							serviceTypeId,
							receipientAccount.getAccountIdentifier(), 
							narration_,
							narration,
							serviceType, 
							transactionRef, 
							"Transfer Money to Card",
							transaction);
					
					
					
					String walletDebitString = (String)rs.getEntity();
					
					if(walletDebitString!=null)
					{
						JSONObject walletDebit = new JSONObject(walletDebitString);
						if(walletDebit!=null)
						{
							
							
						
							int status = walletDebit.getInt("status");
							String receipientInfo = walletDebit.getString("receipientInfo");
							JSONObject transferInfo = new JSONObject(receipientInfo);
							resp.put("status", ERROR.GENERAL_OK);
							resp.put("message", "Funds transferred successfully.");
							resp.put("orderRef", orderRef);
							resp.put("amountTransferred", amount);
							resp.put("receipientInfo", transferInfo.toString());
							
							
							if(status==(ERROR.GENERAL_OK))
							{
								

								
								String sourceType = SourceType.ACCOUNT.name();
								Long sourceCustomerId = sourceAccount.getCustomer().getId();
								String sourceBankName = sourceAccount.getAcquirer().getBank().getBankName();
								Long sourceBankId = sourceAccount.getAcquirer().getBank().getId();
								String receipientType = SourceType.CARD.name();
								Long receipientCustomerId = cardDestination.getAccount().getCustomer().getId();
								String receipientBankName = cardDestination.getAcquirer().getBank().getBankName();
								Long receipientBankId = cardDestination.getAcquirer().getBank().getId();
								Double sourcePriorBalance = walletBalance;
								Double sourceNewBalance = walletBalance;
								ServiceType st = ServiceType.FT_WALLET_TO_CARD;
								String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
								String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
								Long transactionId = transaction.getId();
								String chargeBankRefNo = null;
								
								
								FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
										sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
										receipientCustomerName, receipientCustomerId, receipientBankName,
										receipientBankId, narration, amount, transactionCharge, fixedCharge,
										sourcePriorBalance, sourceNewBalance, st, channel_,
										transactionRef, debitOrderRef, creditOrderRef, transactionId, null, null, null, null, chargeBankRefNo, device.getSwitchToLive());
								ft = (FundsTransfer)swpService.createNewRecord(ft);
								
								
								
								cardDestination = cardDestination.deposit(this.swpService, amount, 0.00);
								
								

								
								JSONObject jsbreakdown = new JSONObject();
								jsbreakdown.put("Sub-total", amount);
								jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
								
								
								JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), cardDestination.getNameOnCard(), cardDestination.getTrackingNumber(), 
										accountIdentifier_, transaction.getOrderRef().toUpperCase(), 
										transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
								transaction.setSummary(txnDetails.toString());
								swpService.updateRecord(transaction);
								
								
								balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
								balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
								walletBalance= 0.00;
								Double currentbalance = 0.00;
								Double availablebalance = 0.00;
								if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
								{
									String accountList = balanceDet.getString("accountList");
									log.info("accountList ---" + accountList);
									JSONArray accounts = new JSONArray(accountList);
									for(int i=0; i<accounts.length(); i++)
									{
										JSONObject acct_ = accounts.getJSONObject(i);
										log.info("acct_ ==" + acct_.toString());
										//JSONObject acct_ = accounts;
										currentbalance = acct_.getDouble("currentbalance");
										log.info("currentbalance --  " + currentbalance);
										availablebalance = acct_.getDouble("availablebalance");
										log.info("availablebalance --  " + availablebalance);
										walletBalance = walletBalance + currentbalance;
									}
									
								}
								
								
								transferInfo = new JSONObject();
									transferInfo.put("recepientCardIdentifier", cardDestination.getTrackingNumber());
									transferInfo.put("recepientCustomerName", cardDestination.getAccount().getCustomer().getFirstName() + " " + cardDestination.getAccount().getCustomer().getLastName());
									transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
									transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
									transferInfo.put("newSourceAccountAvailableBalance", availablebalance);
									transferInfo.put("newSourceAccountCurrentBalance", currentbalance);
									
									transferInfo.put("amountDebitedFromSource", transferInfo.getDouble("amountDebitedFromSource"));
									transferInfo.put("sourceWalletCharges", transferInfo.getDouble("sourceWalletCharges"));
									transferInfo.put("amountCreditToReceipient", 0.00);
									transferInfo.put("receipientWalletCharges", 0.00);
								resp.put("status", ERROR.GENERAL_OK);
								resp.put("message", "Funds transferred successfully.");
								resp.put("orderRef", orderRef);
								resp.put("amountTransferred", amount);
								resp.put("receipientInfo", transferInfo.toString());
								return Response.status(200).entity(resp.toString()).build();
							}
						}
					}
					
					transaction.setStatus(TransactionStatus.FAIL);
					transaction.setTransactionCode(TransactionCode.transactionFail);
					transaction.setResponseCode("02");
					swpService.updateRecord(transaction);
					
					resp.put("status", ERROR.DEBIT_SUCCESSFUL_CREDIT_FAILED);
					resp.put("message", "Funds transfer failed. A Reverse on debit is being processed where necessary");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", JSONObject.NULL);
					
					/*
					PaymentServicesV2 paymentServices = new PaymentServicesV2();
					String otp = null;
					String accountData = UtilityHelper.encryptData(sourceAccount.getAccountIdentifier(), bankKey);
					Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, amount, hash, "", transaction, ft.getDebitOrderRef(), serviceTypeId, token);
					String drResStr = (String)drRes.getEntity();
					log.info("DebitResponse");
					log.info(drResStr);
					if(drResStr!=null)
					{
						JSONObject allResponse = new JSONObject();
						allResponse.put("fundsTransferDebitResponse", drResStr);
						JSONObject debitResponse = new JSONObject(drResStr);
						if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
						{
	
							String bankReferenceNo = debitResponse.getString("bankReferenceNo");
							ft.setSourceDebitBankReferenceNo(bankReferenceNo);
							ft.setSourceResponseData(drResStr);
							this.swpService.updateRecord(ft);
							
							transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
							transaction.setResponseCode("03");
							transaction.setMessageResponse(allResponse.toString());
							swpService.updateRecord(transaction);
							
							Account receipientAccount = cardDestination.getAccount();
							JSONObject creditResponse = UtilityHelper.creditBankWallet(app, swpService, httpHeaders,
									requestContext, amount, receipientAccount, token, merchantId, deviceCode, 
									user, channel, ft.getCreditOrderRef(), narration, acquirer, transaction);
							
							log.info("creditResponse....");
							log.info(creditResponse);
							if(creditResponse!=null && creditResponse.has("status") && creditResponse.getInt("status")==ERROR.WALLET_CREDIT_SUCCESS)
							{
								allResponse.put("fundsTransferCreditResponse", creditResponse);
								
								String bankReference = creditResponse.getString("bankReference");
								ft.setReceipientCreditBankReferenceNo(bankReference);
								ft.setReceipientResponseData(creditResponse.toString());
								this.swpService.updateRecord(ft);
								
								transaction.setTransactionCode(TransactionCode.transactionSuccess);
								transaction.setResponseCode("00");
								transaction.setMessageResponse(allResponse.toString());
								transaction.setStatus(TransactionStatus.SUCCESS);
								swpService.updateRecord(transaction);
								
	
								
								cardDestination = cardDestination.deposit(this.swpService, amount, charges);
								
								balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
								balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
								walletBalance= 0.00;
								Double currentbalance = 0.00;
								Double availablebalance = 0.00;
								if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
								{
									String accountList = balanceDet.getString("accountList");
									log.info("accountList ---" + accountList);
									JSONArray accounts = new JSONArray(accountList);
									for(int i=0; i<accounts.length(); i++)
									{
										JSONObject acct_ = accounts.getJSONObject(i);
										log.info("acct_ ==" + acct_.toString());
										//JSONObject acct_ = accounts;
										currentbalance = acct_.getDouble("currentbalance");
										log.info("currentbalance --  " + currentbalance);
										availablebalance = acct_.getDouble("availablebalance");
										log.info("availablebalance --  " + availablebalance);
										walletBalance = walletBalance + currentbalance;
									}
									
								}
								
								
								JSONObject transferInfo = new JSONObject();
									transferInfo.put("recepientCardIdentifier", cardDestination.getTrackingNumber());
									transferInfo.put("recepientCustomerName", cardDestination.getAccount().getCustomer().getFirstName() + " " + cardDestination.getAccount().getCustomer().getLastName());
									transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
									transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
									transferInfo.put("newSourceAccountAvailableBalance", availablebalance);
									transferInfo.put("newSourceAccountCurrentBalance", currentbalance);
									
									transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
									transferInfo.put("sourceWalletCharges", debitResponse.getDouble("charges"));
									transferInfo.put("amountCreditToReceipient", creditResponse.getDouble("amount"));
									transferInfo.put("receipientWalletCharges", creditResponse.getDouble("charges"));
								resp.put("status", ERROR.GENERAL_OK);
								resp.put("message", "Funds transferred successfully.");
								resp.put("orderRef", orderRef);
								resp.put("amountTransferred", amount);
								resp.put("receipientInfo", transferInfo.toString());
							}
							else
							{
							
								
								
								
								JSONObject reverseResponse = UtilityHelper.creditBankWallet(app, this.swpService, httpHeaders, requestContext, amount, sourceAccount, token, 
										merchantId, deviceCode, user, channel, ft.getCreditOrderRef(), narration, acquirer, transaction);
								
								if(reverseResponse!=null && reverseResponse.has("status") && reverseResponse.getInt("status")==ERROR.WALLET_CREDIT_SUCCESS)
								{
									
									allResponse.put("fundsTransferReverseResponse", reverseResponse.toString());
									
									transaction.setTransactionCode(TransactionCode.transactionReverseSuccess);
									transaction.setResponseCode("04");
									transaction.setMessageResponse(allResponse.toString());
									
									swpService.updateRecord(transaction);
									
									
									balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
									balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
									walletBalance= 0.00;
									Double currentbalance = 0.00;
									Double availablebalance = 0.00;
									if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
									{
										String accountList = balanceDet.getString("accountList");
										log.info("accountList ---" + accountList);
										JSONArray accounts = new JSONArray(accountList);
										for(int i=0; i<accounts.length(); i++)
										{
											JSONObject acct_ = accounts.getJSONObject(i);
											log.info("acct_ ==" + acct_.toString());
											//JSONObject acct_ = accounts;
											currentbalance = acct_.getDouble("currentbalance");
											log.info("currentbalance --  " + currentbalance);
											availablebalance = acct_.getDouble("availablebalance");
											log.info("availablebalance --  " + availablebalance);
											walletBalance = walletBalance + currentbalance;
										}
										
									}
									
									JSONObject transferInfo = new JSONObject();
										transferInfo.put("recepientCardIdentifier", cardDestination.getTrackingNumber());
										transferInfo.put("recepientCustomerName", cardDestination.getAccount().getCustomer().getFirstName() + " " + cardDestination.getAccount().getCustomer().getLastName());
										transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
										transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
										transferInfo.put("newSourceAccountAvailableBalance", availablebalance);
										transferInfo.put("newSourceAccountCurrentBalance", currentbalance);
										
										transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
										transferInfo.put("sourceWalletCharges", debitResponse.getDouble("charges"));
										transferInfo.put("amountCreditToReceipient", creditResponse.getDouble("amount"));
										transferInfo.put("receipientWalletCharges", creditResponse.getDouble("charges"));
									resp.put("status", ERROR.TRANSACTION_FAIL);
									resp.put("message", "Funds transfer was not successful. Your funds debited have been reversed.");
									resp.put("orderRef", orderRef);
									resp.put("amountTransferred", amount);
									resp.put("receipientInfo", transferInfo.toString());
								}
								else
								{
									transaction.setStatus(TransactionStatus.FAIL);
									transaction.setTransactionCode(TransactionCode.transactionFail);
									transaction.setResponseCode("02");
									swpService.updateRecord(transaction);
									
									resp.put("status", ERROR.DEBIT_SUCCESSFUL_CREDIT_FAILED);
									resp.put("message", "Funds transfer failed. A Reverse on debit is being processed.");
									resp.put("orderRef", orderRef);
									resp.put("receipientInfo", JSONObject.NULL);
								}
							}
						}
					}*/
				}
				return Response.status(200).entity(resp.toString()).build();
			}
			
			if(sourceAccount==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid wallet provided to be debited. Please select a valid wallet to transfer funds from");
			}

			if(cardDestination==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid receipient card provided to be funded. Please select a valid card to fund");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}
	
	

	
	@POST
	@Path("/fundBeneficiaryCardFromWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundBeneficiaryCardFromWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			

			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("channel") String channel,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("otp") String otp,
			@FormParam("hash") String hash,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("walletReceipient") String walletReceipient,
			@FormParam("cardTrackingId") String cardTrackingId)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("accountIdentifier", accountIdentifier);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String accountIdentifier_ = (String)UtilityHelper.decryptData(accountIdentifier, bankKey);
			String cardTrackingId_ = (String)UtilityHelper.decryptData(cardTrackingId, bankKey);
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardTrackingId_+"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Account tp where tp.accountIdentifier = '"+accountIdentifier_+"' AND tp.deleted_at IS NULL AND tp.status = " + AccountStatus.ACTIVE.ordinal()  +
					 " AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account account = (Account)swpService.getUniqueRecordByHQL(hql);
			
			if(card!=null)
			{
				log.info("card founr...true");
				if(card.getAccount().getAccountIdentifier().equals(accountIdentifier_))
				{
					log.info("receipient wallet not found");
					
					resp.put("status", ERROR.WRONG_API_USED);
					resp.put("message", "You can not transfer funds from your wallet to your card using this channel. Use the appropriate channel to transfer funds from your wallet to your card");
					
					
					String sn = card.getSerialNo();
					sn = UtilityHelper.encryptData(sn, bankKey);
					
					
					Response fundWalletResponse = fundCardFromWallet(
							httpHeaders,
							requestContext,
							token, 
							amount, 
							accountIdentifier, 
							merchantId, 
							deviceCode, 
							channel, 
							orderRef, 
							otpRef,
							otp,
							hash, 
							serviceTypeId, 
							cardTrackingId,  
							sn);
					
					return fundWalletResponse;
				}
				
				Account sourceAccount = account;
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				Boolean creditAccountTrue = false;
				Boolean creditCardTrue = true;
				String rpin = null;
				Date transactionDate = new Date();
				ServiceType serviceType = ServiceType.FT_WALLET_TO_CARD;
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				String oTP = null;
				String oTPRef = null;
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" +accountIdentifier_  + "~" + card.getTrackingNumber() + "~" + transactionRef;
				String transactionDetail = narration;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;
				String details = "Transfer Money to Card";

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				
				AccountServicesV2 asv = new AccountServicesV2();
				Response balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
				JSONObject balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
				Double walletBalance= 0.00;
				if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
				{
					String accountList = balanceDet.getString("accountList");
					log.info("accountList ---" + accountList);
					JSONArray accounts = new JSONArray(accountList);
					for(int i=0; i<accounts.length(); i++)
					{
						JSONObject acct_ = accounts.getJSONObject(i);
						log.info("acct_ ==" + acct_.toString());
						//JSONObject acct_ = accounts;
						Double currentbalance = acct_.getDouble("currentbalance");
						log.info("currentbalance --  " + currentbalance);
						Double availablebalance = acct_.getDouble("availablebalance");
						log.info("availablebalance --  " + availablebalance);
						walletBalance = walletBalance + currentbalance;
					}
					
				}
				
				
				if(walletBalance<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				
				Boolean debitAccountTrue = true;
				Boolean debitCardTrue = false;
				Long creditAccountId = card.getAccount().getId();
				Long debitCardId = null;
				Long creditCardId = card.getId();
				Long debitAccountId = sourceAccount.getId();
				//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
				
				Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
						customerId, creditAccountTrue, creditCardTrue,
						orderRef, rpin, chanel,
						transactionDate, serviceType, payerName,
						payerEmail, payerMobile, transactionStatus,
						probasePayCurrency, transactionCode,
						sourceAccount, card, device,
						creditPoolAccountTrue, messageRequest,
						messageResponse, fixedCharge,
						transactionCharge, transactionPercentage,
						schemeTransactionCharge, schemeTransactionPercentage,
						amount, responseCode, oTP, oTPRef,
						merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
						null, null, 
						transactingBankId, receipientTransactingBankId,
						accessCode, sourceEntityId, receipientEntityId,
						receipientChannel, transactionDetail,
						closingBalance, totalCreditSum, totalDebitSum,
						paidInByBankUserAccountId, customData,
						responseData, adjustedTransactionId, acquirerId, 
						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, details, accessCode);
				transaction = (Transaction)this.swpService.createNewRecord(transaction);/**/
				
				
				String sourceIdentityNo = account.getAccountIdentifier();
				String sourceType = SourceType.ACCOUNT.name();
				String sourceCustomerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
				Long sourceCustomerId = account.getCustomer().getId();
				String sourceBankName = account.getAcquirer().getBank().getBankName();
				Long sourceBankId = account.getAcquirer().getBank().getId();
				String receipientIdentityNo = card.getTrackingNumber();
				String receipientType = SourceType.CARD.name();
				String receipientCustomerName = card.getNameOnCard();
				Long receipientCustomerId = card.getAccount().getCustomer().getId();
				String receipientBankName = card.getAcquirer().getBank().getBankName();
				Long receipientBankId = card.getAcquirer().getBank().getId();
				String narration_ = "DR~WALLET~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
				Double sourcePriorBalance = card.getCardBalance();
				Double sourceNewBalance = null;
				ServiceType st = ServiceType.FT_WALLET_TO_CARD;
				String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				String chargeBankRefNo = null;
				
				
				
				
				
				
				Account receipientAccount = card.getAccount();
				
				/*Response rs = fundWalletFromWallet(
						httpHeaders,
						requestContext,
						token, 
						amount, 
						accountIdentifier, 
						merchantId,
						deviceCode,
						channel,
						orderRef,
						otpRef,
						otp,
						hash,
						serviceTypeId,
						receipientAccount.getAccountIdentifier(), 
						narration_);*/
				
				
				
				String receipientAccountEnc = (String)UtilityHelper.encryptData(receipientAccount.getAccountIdentifier(), bankKey);
				
				
				Response rs = transferWalletToWallet(httpHeaders,
						requestContext,
						token, 
						amount, 
						accountIdentifier, 
						merchantId,
						deviceCode,
						channel,
						orderRef,
						otpRef,
						otp,
						hash,
						serviceTypeId,
						receipientAccountEnc, 
						narration_,
						narration,
						serviceType, 
						transactionRef,
						"Transfer Money to Card", 
						transaction);
				
				String walletDebitString = (String)rs.getEntity();
				
				if(walletDebitString!=null)
				{
					JSONObject walletDebit = new JSONObject(walletDebitString);
					if(walletDebit!=null)
					{
						
						log.info("walletDebit....");
						log.info(walletDebit.toString());
					
						int status = walletDebit.getInt("status");
						String receipientInfo = walletDebit.getString("receipientInfo");
						JSONObject transferInfo_ = new JSONObject(receipientInfo);
						resp.put("status", ERROR.GENERAL_OK);
						resp.put("message", "Funds transferred successfully.");
						resp.put("orderRef", orderRef);
						resp.put("amountTransferred", amount);
						resp.put("receipientInfo", transferInfo_.toString());
						
						
						
						if(status==(ERROR.GENERAL_OK))
						{


							Long transactionId = transferInfo_.getLong("transactionId");
							FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
									sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
									receipientCustomerName, receipientCustomerId, receipientBankName,
									receipientBankId, narration, amount, transactionCharge, fixedCharge,
									sourcePriorBalance, sourceNewBalance, st, channel_,
									transactionRef, debitOrderRef, creditOrderRef, transactionId, null, null, null, null, chargeBankRefNo, device.getSwitchToLive());
							ft = (FundsTransfer)swpService.createNewRecord(ft);
							
							
							card = card.deposit(this.swpService, amount, 0.00);
							
							

							JSONObject jsbreakdown = new JSONObject();
							jsbreakdown.put("Sub-total", amount);
							jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
							
							
							hql = "Select tp from Transaction tp where tp.id = " + transactionId;
							transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
							JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), card.getNameOnCard(), card.getTrackingNumber(), 
									sourceIdentityNo, transaction.getOrderRef().toUpperCase(), 
									transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
							transaction.setSummary(txnDetails.toString());
							swpService.updateRecord(transaction);
							
							
							
							balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  accountIdentifier_,  merchantId, deviceCode);
							balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
							walletBalance= 0.00;
							Double currentbalance = 0.00;
							Double availablebalance = 0.00;
							if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
							{
								String accountList = balanceDet.getString("accountList");
								log.info("accountList ---" + accountList);
								JSONArray accounts = new JSONArray(accountList);
								for(int i=0; i<accounts.length(); i++)
								{
									JSONObject acct_ = accounts.getJSONObject(i);
									log.info("acct_ ==" + acct_.toString());
									//JSONObject acct_ = accounts;
									currentbalance = acct_.getDouble("currentbalance");
									log.info("currentbalance --  " + currentbalance);
									availablebalance = acct_.getDouble("availablebalance");
									log.info("availablebalance --  " + availablebalance);
									walletBalance = walletBalance + currentbalance;
								}
								
							}
							
							
							JSONObject transferInfo = new JSONObject();
								transferInfo.put("recepientCardIdentifier", card.getTrackingNumber());
								transferInfo.put("recepientCustomerName", card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName());
								transferInfo.put("sourceAccountIdentifier", sourceAccount.getAccountIdentifier());
								transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
								transferInfo.put("newSourceAccountAvailableBalance", availablebalance);
								transferInfo.put("newSourceAccountCurrentBalance", currentbalance);
								
								transferInfo.put("amountDebitedFromSource", transferInfo_.getDouble("amountDebitedFromSource"));
								transferInfo.put("sourceWalletCharges", transferInfo_.getDouble("sourceWalletCharges"));
								transferInfo.put("amountCreditToReceipient", 0.00);
								transferInfo.put("receipientWalletCharges", 0.00);
							resp.put("status", ERROR.GENERAL_OK);
							resp.put("message", "Funds transferred successfully.");
							resp.put("orderRef", orderRef);
							resp.put("amountTransferred", amount);
							resp.put("receipientInfo", transferInfo.toString());
							return Response.status(200).entity(resp.toString()).build();
						}
					}
				}
				
				/*transaction.setStatus(TransactionStatus.FAIL);
				transaction.setTransactionCode(TransactionCode.transactionFail);
				transaction.setResponseCode("02");
				swpService.updateRecord(transaction);*/
				
				resp.put("status", ERROR.DEBIT_SUCCESSFUL_CREDIT_FAILED);
				resp.put("message", "Funds transfer failed. A Reverse on debit is being processed where necessary");
				resp.put("orderRef", orderRef);
				resp.put("receipientInfo", JSONObject.NULL);
				/*
				PaymentServicesV2 paymentServices = new PaymentServicesV2();
				//String otp = null;
				String accountData = UtilityHelper.encryptData(card.getAccount().getAccountIdentifier(), bankKey);
				
				log.info("DEBIT SOURCE WALLET");
				log.info("===================");
				Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, amount, hash, "", transaction, ft.getDebitOrderRef(), serviceTypeId, token);
				String drResStr = (String)drRes.getEntity();
				log.info("DebitResponse");
				log.info(drResStr);
				if(drResStr!=null)
				{
					JSONObject allResponse = new JSONObject();
					allResponse.put("fundsTransferDebitResponse", drResStr);
					JSONObject debitResponse = new JSONObject(drResStr);
					if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
					{

						String bankReferenceNo = debitResponse.getString("bankReferenceNo");
						ft.setSourceDebitBankReferenceNo(bankReferenceNo);
						ft.setSourceResponseData(drResStr);
						this.swpService.updateRecord(ft);
						
						transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
						transaction.setResponseCode("03");
						transaction.setMessageResponse(allResponse.toString());
						swpService.updateRecord(transaction);
						
						
						

						log.info("CREDIT RECIPIENT WALLET");
						log.info("=======================");
						JSONObject creditResponse = UtilityHelper.creditBankWallet(app, swpService, httpHeaders,
								requestContext, amount, account, token, merchantId, deviceCode, 
								user, channel, ft.getCreditOrderRef(), narration, acquirer, transaction);
						
						log.info("creditResponse....");
						log.info(creditResponse);
						if(creditResponse!=null && creditResponse.has("status") && creditResponse.getInt("status")==ERROR.WALLET_CREDIT_SUCCESS)
						{
							allResponse.put("fundsTransferCreditResponse", creditResponse);
							
							String bankReference = creditResponse.getString("bankReference");
							ft.setReceipientCreditBankReferenceNo(bankReference);
							ft.setReceipientResponseData(creditResponse.toString());
							this.swpService.updateRecord(ft);
							
							transaction.setTransactionCode(TransactionCode.transactionSuccess);
							transaction.setResponseCode("00");
							transaction.setMessageResponse(allResponse.toString());
							transaction.setStatus(TransactionStatus.SUCCESS);
							swpService.updateRecord(transaction);
							

							
							card = card.deposit(this.swpService, amount, charges);
							
							
							
							JSONObject transferInfo = new JSONObject();
								transferInfo.put("recepientCardIdentifier", card.getTrackingNumber());
								transferInfo.put("recepientCustomerName", card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName());
								transferInfo.put("sourceAccountIdentifier", account.getAccountIdentifier());
								transferInfo.put("sourceCustomerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
								transferInfo.put("newSourceAccountAvailableBalance", debitResponse.getDouble("availablebalance"));
								transferInfo.put("newSourceAccountCurrentBalance", debitResponse.getDouble("currentbalance"));
								
								transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
								transferInfo.put("sourceWalletCharges", debitResponse.getDouble("charges"));
								transferInfo.put("amountCreditToReceipient", creditResponse.getDouble("amount"));
								transferInfo.put("receipientWalletCharges", creditResponse.getDouble("charges"));
							resp.put("status", ERROR.GENERAL_OK);
							resp.put("message", "Funds transferred successfully.");
							resp.put("orderRef", orderRef);
							resp.put("amountTransferred", amount);
							resp.put("receipientInfo", transferInfo.toString());
						}
						else
						{
						
							
							

							
						}
					}
				}*/
				return Response.status(200).entity(resp.toString()).build();
			}
			
			resp.put("status", ERROR.CARD_NOT_PROFILED);
			resp.put("message", "Invalid card provided to be funded. Please select a valid card to fund");
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}
	
	
	
	
	@POST
	@Path("/fundWalletFromCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundWalletFromCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("cardData") String cardData, 
			@FormParam("channel") String channel, 
			@FormParam("walletReceipient") String walletReceipient,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("orderRef") String orderRef,
			@FormParam("hash") String hash)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("cardData", cardData);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			String cardInfo = (String)UtilityHelper.decryptData(cardData, bankKey);
			log.info("cardInfo....." + cardInfo);
			String[] cardInfoArray = cardInfo.split("~");
			String destWalletReceipient = (String)UtilityHelper.decryptData(walletReceipient, bankKey);
			log.info("destWalletReceipient....." + destWalletReceipient);
			
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}


			resp.put("destWalletReceipient", destWalletReceipient);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues debiting your wallet please try again");
			
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}


			hql = "Select tp from Account tp where tp.accountIdentifier = '"+destWalletReceipient+"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + AccountStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			Account walletDestination = (Account)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)" + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardSource = (ECard)swpService.getUniqueRecordByHQL(hql);
			

			log.info("card founr...true");
			Account sourceAccount = cardSource.getAccount();
			String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
			String bankPaymentReference = null;
			Long customerId = sourceAccount.getCustomer().getId();
			Boolean creditAccountTrue = false;
			Boolean creditCardTrue = false;
			String rpin = null;
			Date transactionDate = new Date();
			ServiceType serviceType = ServiceType.FT_CARD_TO_WALLET;
			String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
			String payerEmail = sourceAccount.getCustomer().getContactEmail();
			String payerMobile = sourceAccount.getCustomer().getContactMobile();
			TransactionStatus transactionStatus = TransactionStatus.PENDING;
			ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
			String transactionCode = TransactionCode.transactionPending;
			Boolean creditPoolAccountTrue = false;
			String messageRequest = null;
			String messageResponse =  null;
			String responseCode = "01";
			String oTP = null;
			String oTPRef = null;
			Double schemeTransactionPercentage = 0.00;
			String merchantName = merchant.getMerchantName();
			String merchantBank = null;
			String merchantAccount = null;
			Long transactingBankId = acquirer.getBank().getId();
			Long receipientTransactingBankId = acquirer.getBank().getId();
			Integer accessCode = null;
			Long sourceEntityId = sourceAccount.getId();
			Long receipientEntityId = null;
			Channel chanel = Channel.valueOf(channel);
			Channel receipientChannel = Channel.valueOf(channel);
			String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" + cardSource.getTrackingNumber() + "~" + walletDestination.getAccountIdentifier() + "~" + transactionRef;
			String transactionDetail = narration;
			Double closingBalance = null;
			Double totalCreditSum = null;
			Double totalDebitSum = null;
			Long paidInByBankUserAccountId = null;
			String customData = null;
			String responseData = null;
			Long adjustedTransactionId = null;
			Long acquirerId = acquirer.getId();
			Double transactionCharge = 0.00;
			Double transactionPercentage = 0.00;
			Double schemeTransactionCharge = 0.00;
			Double fixedCharge = 0.00;

			Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
			Double cardMinimumBalance = cardSource.getCardScheme().getMinimumBalance();
			
			if((cardSource.getCardBalance() - cardMinimumBalance)<(amount+charges))
			{
				JSONObject transferInfo = new JSONObject();
				resp.put("status", ERROR.INSUFFICIENT_FUNDS);
				resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
				resp.put("orderRef", orderRef);
				resp.put("receipientInfo", transferInfo.toString());
				return Response.status(200).entity(resp.toString()).build();
			}
			
			
			
			
			Boolean debitAccountTrue = false;
			Boolean debitCardTrue = true;
			Long creditAccountId = null;
			Long creditCardId = null;
			Long debitAccountId = null;
			Long debitCardId = null;
			creditAccountId = walletDestination.getId();
			debitCardId = cardSource.getId();
			//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
			
			Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
					customerId, creditAccountTrue, creditCardTrue,
					orderRef, rpin, chanel,
					transactionDate, serviceType, payerName,
					payerEmail, payerMobile, transactionStatus,
					probasePayCurrency, transactionCode,
					sourceAccount, cardSource, device,
					creditPoolAccountTrue, messageRequest,
					messageResponse, fixedCharge,
					transactionCharge, transactionPercentage,
					schemeTransactionCharge, schemeTransactionPercentage,
					amount, responseCode, oTP, oTPRef,
					merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
					null, null, 
					transactingBankId, receipientTransactingBankId,
					accessCode, sourceEntityId, receipientEntityId,
					receipientChannel, transactionDetail,
					closingBalance, totalCreditSum, totalDebitSum,
					paidInByBankUserAccountId, customData,
					responseData, adjustedTransactionId, acquirerId, 
					debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Wallet", device.getSwitchToLive());
			transaction = (Transaction)this.swpService.createNewRecord(transaction);
			
			
			String sourceIdentityNo = cardSource.getTrackingNumber();
			String sourceType = SourceType.CARD.name();
			String sourceCustomerName = cardSource.getNameOnCard();
			Long sourceCustomerId = cardSource.getAccount().getCustomer().getId();
			String sourceBankName = cardSource.getAcquirer().getBank().getBankName();
			Long sourceBankId = cardSource.getAcquirer().getBank().getId();
			String receipientIdentityNo = walletDestination.getAccountIdentifier();
			String receipientType = SourceType.CARD.name();
			String receipientCustomerName = walletDestination.getCustomer().getFirstName() + " " + walletDestination.getCustomer().getLastName();
			Long receipientCustomerId = walletDestination.getCustomer().getId();
			String receipientBankName = walletDestination.getAcquirer().getBank().getBankName();
			Long receipientBankId = walletDestination.getAcquirer().getBank().getId();
			String narration_ = "DR~CARD~" + sourceIdentityNo + "~WALLET~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
			Double sourcePriorBalance = cardSource.getCardBalance();
			Double sourceNewBalance = null;
			ServiceType st = ServiceType.FT_CARD_TO_WALLET;
			String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
			String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
			Long transactionId = transaction.getId();
			String chargeBankRefNo = null;
			
			
			
			
			
			
			if(cardSource!=null && walletDestination!=null)
			{
				if(cardSource.getAccount().getId().equals(walletDestination.getId()))
				{
					
					cardSource = cardSource.withdraw(this.swpService, amount, charges);
					
					
					FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
							sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
							receipientCustomerName, receipientCustomerId, receipientBankName,
							receipientBankId, narration, amount, transactionCharge, fixedCharge,
							sourcePriorBalance, sourceNewBalance, st, channel_,
							transactionRef, debitOrderRef, creditOrderRef, transactionId, null, null, null, null, chargeBankRefNo, device.getSwitchToLive());
					ft = (FundsTransfer)swpService.createNewRecord(ft);
					
					
					ft.setSourceDebitBankReferenceNo(null);
					ft.setSourceResponseData(null);
					this.swpService.updateRecord(ft);
					
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setResponseCode("00");
					transaction.setMessageResponse(null);
					
					


					
					JSONObject jsbreakdown = new JSONObject();
					jsbreakdown.put("Sub-total", amount);
					jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
					
					
					JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), walletDestination.getCustomer().getFirstName() + " " + walletDestination.getCustomer().getFirstName(), 
							cardSource.getTrackingNumber(), 
							sourceIdentityNo, transaction.getOrderRef().toUpperCase(), 
							transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
					transaction.setSummary(txnDetails.toString());
					swpService.updateRecord(transaction);
					
					
					hql = "Select tp from ECard tp where tp.account.id = " + sourceAccount.getId();
					Collection<ECard> accountCards = (Collection<ECard>)this.swpService.getAllRecordsByHQL(hql);
					Iterator<ECard> iterCard = accountCards.iterator();
					Double totalAmountOnCards = 0.00;
					while(iterCard.hasNext())
					{
						ECard cd = iterCard.next();
						totalAmountOnCards = totalAmountOnCards + cd.getCardBalance();
					}
					
					JSONObject transferInfo = new JSONObject();
					transferInfo.put("sourceCardIdentifier", cardSource.getTrackingNumber());
					transferInfo.put("sourceCustomerName", cardSource.getAccount().getCustomer().getFirstName() + " " + cardSource.getAccount().getCustomer().getLastName());
					transferInfo.put("recipientAccountIdentifier", sourceAccount.getAccountIdentifier());
					transferInfo.put("recipientCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
					transferInfo.put("newSourceCardBalance", cardSource.getCardBalance());
					
					transferInfo.put("amountDebitedFromSource", amount);
					transferInfo.put("sourceCardCharges", charges);
					transferInfo.put("amountCreditToReceipient", amount);
					transferInfo.put("receipientWalletCharges", 0.00);
					
					resp.put("status", ERROR.GENERAL_OK);
					resp.put("message", "Funds transferred successfully.");
					resp.put("orderRef", orderRef);
					resp.put("amountTransferred", amount);
					resp.put("receipientInfo", transferInfo.toString());	
					return Response.status(200).entity(resp.toString()).build();
				}
				else
				{
					PaymentServicesV2 paymentServices = new PaymentServicesV2();
					String otp = null;
					String accountData = UtilityHelper.encryptData(cardSource.getAccount().getAccountIdentifier(), bankKey);
					
					cardSource = cardSource.withdraw(this.swpService, amount, charges);
					
					
					
					log.info("INTERNAL FUNDS TRANSFER");
					log.info("===================");
					JSONObject ftRes = UtilityHelper.internalFundsTransfer(app, swpService, httpHeaders, requestContext, amount, sourceAccount, 
							walletDestination, token, merchantCode, deviceCode, user, channel, orderRef, narration, acquirer, device);
					log.info("FTResponse");
					if(ftRes!=null)
					{
						log.info(ftRes.toString());
						JSONObject allResponse = new JSONObject();
						if(ftRes.has("status") && ftRes.getInt("status")==(ERROR.WALLET_CREDIT_SUCCESS))
						{



							
							JSONObject jsbreakdown = new JSONObject();
							jsbreakdown.put("Sub-total", amount);
							jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
							
							
							JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), walletDestination.getCustomer().getFirstName() + " " + walletDestination.getCustomer().getFirstName(), 
									walletDestination.getAccountIdentifier(), 
									sourceIdentityNo, transaction.getOrderRef().toUpperCase(), 
									transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
							transaction.setSummary(txnDetails.toString());
							swpService.updateRecord(transaction);

							String bankReferenceNo = ftRes.getString("bankReference");
							String chargeBankPaymentReference = ftRes.has("chargeBankPaymentReference") ? ftRes.getString("chargeBankPaymentReference") : null;
							FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
									sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
									receipientCustomerName, receipientCustomerId, receipientBankName,
									receipientBankId, narration, amount, transactionCharge, fixedCharge,
									sourcePriorBalance, sourceNewBalance, st, channel_,
									transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), 
									bankReferenceNo, chargeBankPaymentReference, device.getSwitchToLive());
							ft = (FundsTransfer)swpService.createNewRecord(ft);
							
							transaction.setTransactionCode(TransactionCode.transactionSuccess);
							transaction.setResponseCode("00");
							transaction.setMessageResponse(allResponse.toString());
							transaction.setStatus(TransactionStatus.SUCCESS);
							swpService.updateRecord(transaction);
							allResponse.put("fundsTransferResponse", ftRes.toString());
							allResponse.put("fundsTransferCreditResponse", ftRes.toString());
							
							AccountServicesV2 asv = new AccountServicesV2();
							Response balanceDetResp = asv.getWalletDetails( httpHeaders, requestContext, acquirerCode,  sourceAccount.getAccountIdentifier(),  merchantId, deviceCode);
							JSONObject balanceDet = new JSONObject(balanceDetResp.getEntity().toString());
							Double walletBalance= 0.00;
							Double walletAvailablebalance = 0.00;
							if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
							{
								String accountList = balanceDet.getString("accountList");
								log.info("accountList ---" + accountList);
								JSONArray accounts = new JSONArray(accountList);
								
								
								
								
								Double totalAmount = 0.00;
								for(int i=0; i<accounts.length(); i++)
								{
									
									JSONObject acct_ = accounts.getJSONObject(i);
									
									hql = "Select sum(tp.cardBalance + tp.cardCharges) as totalAmountDeposited, tp.cardBalance as cardBalance, tp.trackingNumber, tp.serialNo, tp.pan, tp.nameOnCard, tp.cvv, date_format(tp.expiryDate, '%m/%y') as expiryDate from ecards tp where "
											+ "tp.account_id = "+sourceAccount.getId()+" Group By tp.id AND tp.isLive = " + device.getSwitchToLive();
									log.info(hql);
									List<Map<String, Object>> cardsListing = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
									if(cardsListing!=null && cardsListing.size()>0)
									{
										for(int i1=0; i1<cardsListing.size(); i1++)
										{
											Map<String, Object> totalAmountMap = cardsListing.get(i1);
											Double amt = (Double)totalAmountMap.get("cardBalance");
											totalAmount = totalAmount + amt;
											log.info(totalAmount);
										}
									}
									
									log.info("acct_ ==" + acct_.toString());
									//JSONObject acct_ = accounts;
									Double currentbalance = acct_.getDouble("currentbalance");
									log.info("currentbalance --  " + currentbalance);
									Double availablebalance = acct_.getDouble("availablebalance");
									log.info("availablebalance --  " + availablebalance);
									walletBalance = walletBalance + currentbalance;
									walletAvailablebalance = walletAvailablebalance + availablebalance - totalAmount;
								}
								
							}
							
								
								
							JSONObject transferInfo = new JSONObject();
								transferInfo.put("recepientAccountIdentifier", walletDestination.getAccountIdentifier());
								transferInfo.put("recepientCustomerName", walletDestination.getCustomer().getFirstName() + " " + walletDestination.getCustomer().getLastName());
								transferInfo.put("sourceCardIdentifier", cardSource.getTrackingNumber());
								transferInfo.put("sourceCustomerName", sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName());
								transferInfo.put("newSourceCardAvailableBalance", cardSource.getCardBalance());
								transferInfo.put("newSourceAccountCurrentBalance", cardSource.getCardBalance());
								
								transferInfo.put("amountDebitedFromSource", amount);
								transferInfo.put("sourceWalletCharges", charges);
								transferInfo.put("amountCreditToReceipient", amount);
								transferInfo.put("receipientWalletCharges", 0.00);
								
								transferInfo.put("transactionId", transaction.getId());
							resp.put("status", ERROR.GENERAL_OK);
							resp.put("message", "Funds transferred successfully.");
							resp.put("orderRef", orderRef);
							resp.put("amountTransferred", amount);
							resp.put("receipientInfo", transferInfo.toString());
							
						}
					}
				}
			}
			
			if(cardSource==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid card provided to be debited. Please select a valid card to debit");
			}

			if(walletDestination==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid wallet provided to be funded. Please provide a valid wallet to fund");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}



	//Same Card Owner
	@POST
	@Path("/fundCardFromCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundCardFromCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("cardData") String cardData, 
			@FormParam("channel") String channel, 
			@FormParam("cardReceipient") String cardReceipient,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("orderRef") String orderRef,
			@FormParam("hash") String hash)
	{
		
		
		
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("cardData", cardData);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			String cardInfo = (String)UtilityHelper.decryptData(cardData, bankKey);
			log.info("cardInfo....." + cardInfo);
			String[] cardInfoArray = cardInfo.split("~");
			String destCardIdentifierStr = (String)UtilityHelper.decryptData(cardReceipient, bankKey);
			log.info("destCardIdentifierStr....." + destCardIdentifierStr);
			String[] destCardIdentifier = destCardIdentifierStr.split("~");
			
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}


			resp.put("destCardIdentifier", destCardIdentifier);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues debiting your wallet please try again");
			
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}


			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+destCardIdentifier[0]+"' AND tp.serialNo = '"+ destCardIdentifier[1] +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardDestination = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardSource = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			if(cardSource!=null && cardDestination!=null)
			{
				if(!cardSource.getAccount().getCustomer().getUser().getId().equals(cardDestination.getAccount().getCustomer().getUser().getId()))
				{
					
					Response resp1 = this.fundBeneficiaryCardFromCard(httpHeaders, requestContext, token, amount, cardData, channel, destCardIdentifier[0], merchantId, deviceCode, serviceTypeId, orderRef, hash);
					return resp1;					
				}
				log.info("card founr...true");
				Account sourceAccount = cardSource.getAccount();
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				Boolean creditAccountTrue = false;
				Boolean creditCardTrue = false;
				String rpin = null;
				Date transactionDate = new Date();
				ServiceType serviceType = ServiceType.FT_CARD_TO_CARD;
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				String oTP = null;
				String oTPRef = null;
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" + cardSource.getTrackingNumber() + "~" + cardDestination.getTrackingNumber() + "~" + transactionRef;
				String transactionDetail = narration;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				Double cardMinimumBalance = cardSource.getCardScheme().getMinimumBalance();
				
				if((cardSource.getCardBalance() - cardMinimumBalance)<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}
				
				Boolean debitAccountTrue = false;
				Boolean debitCardTrue = true;
				Long creditAccountId = null;
				Long creditCardId = null;
				Long debitAccountId = null;
				Long debitCardId = null;
				creditCardId = (Long)cardDestination.getId();
				debitCardId = cardSource.getId();
				//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
				
				Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
						customerId, creditAccountTrue, creditCardTrue,
						orderRef, rpin, chanel,
						transactionDate, serviceType, payerName,
						payerEmail, payerMobile, transactionStatus,
						probasePayCurrency, transactionCode,
						sourceAccount, cardSource, device,
						creditPoolAccountTrue, messageRequest,
						messageResponse, fixedCharge,
						transactionCharge, transactionPercentage,
						schemeTransactionCharge, schemeTransactionPercentage,
						amount, responseCode, oTP, oTPRef,
						merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
						null, null, 
						transactingBankId, receipientTransactingBankId,
						accessCode, sourceEntityId, receipientEntityId,
						receipientChannel, transactionDetail,
						closingBalance, totalCreditSum, totalDebitSum,
						paidInByBankUserAccountId, customData,
						responseData, adjustedTransactionId, acquirerId, 
						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Card", device.getSwitchToLive());
				transaction = (Transaction)this.swpService.createNewRecord(transaction);
				
				
				String sourceIdentityNo = cardSource.getTrackingNumber();
				String sourceType = SourceType.CARD.name();
				String sourceCustomerName = cardSource.getNameOnCard();
				Long sourceCustomerId = cardSource.getAccount().getCustomer().getId();
				String sourceBankName = cardSource.getAcquirer().getBank().getBankName();
				Long sourceBankId = cardSource.getAcquirer().getBank().getId();
				String receipientIdentityNo = cardDestination.getTrackingNumber();
				String receipientType = SourceType.CARD.name();
				String receipientCustomerName = cardDestination.getNameOnCard();
				Long receipientCustomerId = cardDestination.getAccount().getCustomer().getId();
				String receipientBankName = cardDestination.getAcquirer().getBank().getBankName();
				Long receipientBankId = cardDestination.getAcquirer().getBank().getId();
				String narration_ = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
				Double sourcePriorBalance = cardSource.getCardBalance();
				Double sourceNewBalance = null;
				ServiceType st = ServiceType.FT_CARD_TO_CARD;
				String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				Long transactionId = transaction.getId();
				String chargeBankRefNo = null;
				
				
				FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
						sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
						receipientCustomerName, receipientCustomerId, receipientBankName,
						receipientBankId, narration, amount, transactionCharge, fixedCharge,
						sourcePriorBalance, sourceNewBalance, st, channel_,
						transactionRef, debitOrderRef, creditOrderRef, transactionId, null, null, null, null, chargeBankRefNo, device.getSwitchToLive());
				ft = (FundsTransfer)swpService.createNewRecord(ft);

				cardSource = cardSource.withdraw(this.swpService, amount, charges);
				cardDestination = cardDestination.deposit(this.swpService, amount, charges);
				
				
				JSONObject jsbreakdown = new JSONObject();
				jsbreakdown.put("Sub-total", amount);
				jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
				
				
				JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), cardDestination.getNameOnCard(), cardDestination.getTrackingNumber(), 
						cardSource.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
						transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
				transaction.setSummary(txnDetails.toString());
				swpService.updateRecord(transaction);
							
				JSONObject transferInfo = new JSONObject();
					transferInfo.put("recepientCardIdentifier", cardDestination.getTrackingNumber());
					transferInfo.put("recepientCustomerName", cardDestination.getNameOnCard());
					transferInfo.put("sourceCardIdentifier", cardSource.getTrackingNumber());
					transferInfo.put("sourceCustomerName", cardSource.getNameOnCard());
					transferInfo.put("newSourceCardBalance", cardSource.getCardBalance());
					
					transferInfo.put("amountDebitedFromSource", amount);
					transferInfo.put("sourceWalletCharges", charges);
					transferInfo.put("amountCreditToReceipient", amount);
					transferInfo.put("receipientWalletCharges", 0.00);
				resp.put("status", ERROR.GENERAL_OK);
				resp.put("message", "Funds transferred successfully.");
				resp.put("orderRef", orderRef);
				resp.put("amountTransferred", amount);
				resp.put("receipientInfo", transferInfo.toString());

				return Response.status(200).entity(resp.toString()).build();
			}
			
			if(cardSource==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid card provided to be debited. Please select a valid card to debit");
			}

			if(cardDestination==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid card provided to be funded. Please select a valid card to fund");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}


	
	
	
	//Transfer to external card
	@POST
	@Path("/fundBeneficiaryCardFromCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundBeneficiaryCardFromCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("cardData") String cardData, 
			@FormParam("channel") String channel, 
			@FormParam("cardReceipient") String cardReceipient,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("orderRef") String orderRef,
			@FormParam("hash") String hash)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("cardData", cardData);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);

			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			String cardInfo = (String)UtilityHelper.decryptData(cardData, bankKey);
			log.info("cardInfo....." + cardInfo);
			String[] cardInfoArray = cardInfo.split("~");
			String destCardIdentifier = (String)UtilityHelper.decryptData(cardReceipient, bankKey);
			log.info("destCardIdentifier....." + destCardIdentifier);
			
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}


			resp.put("destCardIdentifier", destCardIdentifier);
			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues debiting your wallet please try again");
			
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}


			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+destCardIdentifier+"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardDestination = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from ECard tp where tp.trackingNumber = '"+cardInfoArray[0]+"' AND tp.serialNo = '"+ cardInfoArray[1] +"' AND tp.deleted_at IS NULL "
					+ "AND tp.status = " + CardStatus.ACTIVE.ordinal()  +
					 " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0) AND tp.isLive = " + device.getSwitchToLive();
			log.info("hql..." + hql);
			ECard cardSource = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			if(cardSource!=null && cardDestination!=null)
			{
				if(cardSource.getAccount().getCustomer().getUser().getId()==cardDestination.getAccount().getCustomer().getUser().getId())
				{
					Response resp1 = this.fundCardFromCard(httpHeaders, requestContext, token, amount, cardData, channel, cardReceipient, merchantId, deviceCode, serviceTypeId, orderRef, hash);
					return resp1;				
				}
				log.info("card founr...true");
				Account sourceAccount = cardSource.getAccount();
				Account walletDestination = cardDestination.getAccount();
				
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				String bankPaymentReference = null;
				Long customerId = sourceAccount.getCustomer().getId();
				Boolean creditAccountTrue = false;
				Boolean creditCardTrue = false;
				String rpin = null;
				Date transactionDate = new Date();
				ServiceType serviceType = ServiceType.FT_CARD_TO_CARD;
				String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
				String payerEmail = sourceAccount.getCustomer().getContactEmail();
				String payerMobile = sourceAccount.getCustomer().getContactMobile();
				TransactionStatus transactionStatus = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
				String transactionCode = TransactionCode.transactionPending;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse =  null;
				String responseCode = "01";
				String oTP = null;
				String oTPRef = null;
				Double schemeTransactionPercentage = 0.00;
				String merchantName = merchant.getMerchantName();
				String merchantBank = null;
				String merchantAccount = null;
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = acquirer.getBank().getId();
				Integer accessCode = null;
				Long sourceEntityId = sourceAccount.getId();
				Long receipientEntityId = null;
				Channel chanel = Channel.valueOf(channel);
				Channel receipientChannel = Channel.valueOf(channel);
				String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" + cardSource.getTrackingNumber() + "~" + cardDestination.getTrackingNumber() + "~" + transactionRef;
				String transactionDetail = narration;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double fixedCharge = 0.00;

				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				Double cardMinimumBalance = cardSource.getCardScheme().getMinimumBalance();
				
				if((cardSource.getCardBalance() - cardMinimumBalance)<(amount+charges))
				{
					JSONObject transferInfo = new JSONObject();
					resp.put("status", ERROR.INSUFFICIENT_FUNDS);
					resp.put("message", "Funds transferred was not successful. You do not have enough funds in your card to cover this transfer.");
					resp.put("orderRef", orderRef);
					resp.put("receipientInfo", transferInfo.toString());
					return Response.status(200).entity(resp.toString()).build();
				}


				Boolean debitAccountTrue = false;
				Boolean debitCardTrue = true;
				Long creditAccountId = null;
				Long creditCardId = null;
				Long debitAccountId = null;
				Long debitCardId = null;

				creditCardId = (Long)cardDestination.getId();
				debitCardId = cardSource.getId();
				//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
				Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
						customerId, creditAccountTrue, creditCardTrue,
						orderRef, rpin, chanel,
						transactionDate, serviceType, payerName,
						payerEmail, payerMobile, transactionStatus,
						probasePayCurrency, transactionCode,
						sourceAccount, cardSource, device,
						creditPoolAccountTrue, messageRequest,
						messageResponse, fixedCharge,
						transactionCharge, transactionPercentage,
						schemeTransactionCharge, schemeTransactionPercentage,
						amount, responseCode, oTP, oTPRef,
						merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
						null, null, 
						transactingBankId, receipientTransactingBankId,
						accessCode, sourceEntityId, receipientEntityId,
						receipientChannel, transactionDetail,
						closingBalance, totalCreditSum, totalDebitSum,
						paidInByBankUserAccountId, customData,
						responseData, adjustedTransactionId, acquirerId, 
						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Transfer Money to Card", device.getSwitchToLive());
				transaction = (Transaction)this.swpService.createNewRecord(transaction);
				
				
				String sourceIdentityNo = cardSource.getTrackingNumber();
				String sourceType = SourceType.CARD.name();
				String sourceCustomerName = cardSource.getNameOnCard();
				Long sourceCustomerId = cardSource.getAccount().getCustomer().getId();
				String sourceBankName = cardSource.getAcquirer().getBank().getBankName();
				Long sourceBankId = cardSource.getAcquirer().getBank().getId();
				String receipientIdentityNo = cardDestination.getTrackingNumber();
				String receipientType = SourceType.CARD.name();
				String receipientCustomerName = cardDestination.getNameOnCard();
				Long receipientCustomerId = cardDestination.getAccount().getCustomer().getId();
				String receipientBankName = cardDestination.getAcquirer().getBank().getBankName();
				Long receipientBankId = cardDestination.getAcquirer().getBank().getId();
				String narration_ = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
				Double sourcePriorBalance = cardSource.getCardBalance();
				Double sourceNewBalance = null;
				ServiceType st = ServiceType.FT_CARD_TO_CARD;
				String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
				Long transactionId = transaction.getId();
				String chargeBankRefNo = null;
				
				
				
				PaymentServicesV2 paymentServices = new PaymentServicesV2();
				String otp = null;
				String accountData = UtilityHelper.encryptData(cardSource.getAccount().getAccountIdentifier(), bankKey);
				
				cardSource = cardSource.withdraw(this.swpService, amount, charges);
				
				
				
				log.info("INTERNAL FUNDS TRANSFER");
				log.info("===================");
				JSONObject ftRes = UtilityHelper.internalFundsTransfer(app, swpService, httpHeaders, requestContext, amount, sourceAccount, 
						walletDestination, token, merchantCode, deviceCode, user, channel, orderRef, narration, acquirer, device);
				log.info("FTResponse");
				if(ftRes!=null)
				{
					log.info(ftRes.toString());
					JSONObject allResponse = new JSONObject();
					if(ftRes.has("status") && ftRes.getInt("status")==(ERROR.WALLET_CREDIT_SUCCESS))
					{

						String bankReferenceNo = ftRes.getString("bankReference");
						String chargeBankPaymentReference = ftRes.has("chargeBankPaymentReference") ? ftRes.getString("chargeBankPaymentReference") : null;
						FundsTransfer ft = new FundsTransfer(sourceIdentityNo, sourceType, sourceCustomerName, sourceCustomerId,
								sourceBankName, sourceBankId, receipientIdentityNo, receipientType,
								receipientCustomerName, receipientCustomerId, receipientBankName,
								receipientBankId, narration, amount, transactionCharge, fixedCharge,
								sourcePriorBalance, sourceNewBalance, st, channel_,
								transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), bankReferenceNo, 
								chargeBankPaymentReference, device.getSwitchToLive());
						ft = (FundsTransfer)swpService.createNewRecord(ft);
						cardDestination = cardDestination.deposit(swpService, amount, charges);
						
						transaction.setTransactionCode(TransactionCode.transactionSuccess);
						transaction.setResponseCode("00");
						transaction.setMessageResponse(allResponse.toString());
						transaction.setStatus(TransactionStatus.SUCCESS);

						JSONObject jsbreakdown = new JSONObject();
						jsbreakdown.put("Sub-total", amount);
						jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
						
						JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.FUNDS_TRANSFER.name(), cardDestination.getNameOnCard(), cardDestination.getTrackingNumber(), 
								cardSource.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
								transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
						transaction.setSummary(txnDetails.toString());
						swpService.updateRecord(transaction);

						allResponse.put("fundsTransferResponse", ftRes.toString());
						allResponse.put("fundsTransferCreditResponse", ftRes.toString());
						
						AccountServicesV2 asv = new AccountServicesV2();
						Double walletBalance= 0.00;
						Double walletAvailablebalance = 0.00;
						
						
							
							
						JSONObject transferInfo = new JSONObject();
							transferInfo.put("recepientCardIdentifier", "**** **** **** **** " + cardDestination.getTrackingNumber().substring(cardDestination.getTrackingNumber().length() - 4));
							transferInfo.put("recepientCustomerName", cardDestination.getNameOnCard());
							transferInfo.put("sourceCardIdentifier", cardSource.getTrackingNumber());
							transferInfo.put("sourceCustomerName", cardSource.getNameOnCard());
							transferInfo.put("newSourceCardAvailableBalance", cardSource.getCardBalance());
							transferInfo.put("newSourceAccountCurrentBalance", cardSource.getCardBalance());
							
							transferInfo.put("amountDebitedFromSource", amount);
							transferInfo.put("sourceWalletCharges", charges);
							transferInfo.put("amountCreditToReceipient", amount);
							transferInfo.put("receipientCardCharges", 0.00);
							
							transferInfo.put("transactionId", transaction.getId());
						resp.put("status", ERROR.GENERAL_OK);
						resp.put("message", "Funds transferred successfully.");
						resp.put("orderRef", orderRef);
						resp.put("amountTransferred", amount);
						resp.put("receipientInfo", transferInfo.toString());
						
					}
				}
			}
			
			if(cardSource==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid card provided to be debited. Please select a valid card to debit");
			}

			if(cardDestination==null)
			{
				resp.put("status", ERROR.CARD_NOT_PROFILED);
				resp.put("message", "Invalid card provided to be funded. Please select a valid card to fund");
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return Response.status(200).entity(resp.toString()).build();
	}

	
	
	

	@POST
	@Path("/fundBankAccountFromWalletAndCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fundBankAccountFromWalletAndCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("sourceType") String sourceType,
			@FormParam("token") String token, 
			@FormParam("amount") Double amount, 
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("channel") String channel,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("otp") String otp,
			@FormParam("hash") String hash, 
			@FormParam("sortCode") String sortCode,
			@FormParam("bicCode") String bicCode,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("sourceIdentifier") String sourceIdentifier, 
			@FormParam("destinationIdentifier") String destinationIdentifier, 
			@FormParam("narration") String narration_)
	{
		JSONObject resp = new JSONObject();
		try
		{

			
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);


			resp.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			resp.put("message", "We experienced issues funding your card please try again");
			
				
	    	JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				resp.put("status", ERROR.FORCE_LOGOUT_USER);
				resp.put("message", "Token expired");
				resp.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(resp.toString()).build();
				
			}
			else
			{
				resp.put("token", verifyJ.getString("token"));
			}

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			String merchantCode = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
				merchantCode = merchant.getMerchantCode();
			}
			
			Channel channel_ = null;
			try
			{
				channel_ = Channel.valueOf(channel);
			}
			catch(Exception e)
			{
				
			}

			String sourceIdentifier_ = (String)UtilityHelper.decryptData(sourceIdentifier, bankKey);
			resp.put("accountIdentifier", sourceIdentifier);
			
			ServiceType serviceType = ServiceType.FT_WALLET_TO_WALLET;
			String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
			String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" +sourceType  + "~" + sourceIdentifier_ + "~" + transactionRef;
			
			log.info(sourceType);
			if(sourceType.equals("WALLET"))
			{
				Response res = transferWalletToBank(httpHeaders,
					requestContext,
					token, 
					amount, 
					sourceIdentifier_, 
					merchantId,
					deviceCode,
					channel,
					orderRef,
					otpRef,
					otp,
					hash,
					serviceTypeId,
					destinationIdentifier, 
					narration_, 
					narration,
					serviceType, 
					transactionRef, 
					sortCode,
					bicCode);
				return res;
			}
			else if(sourceType.equals("CARD"))
			{
				Response res = transferCardToBank(httpHeaders,
					requestContext,
					token, 
					amount, 
					sourceIdentifier_, 
					merchantId,
					deviceCode,
					channel,
					orderRef,
					otpRef,
					otp,
					hash,
					serviceTypeId,
					destinationIdentifier, 
					narration_, 
					narration,
					serviceType, 
					transactionRef, 
					sortCode,
					bicCode);
				return res;
			}
			resp.put("status", ERROR.INCOMPLETE_PARAMETERS);
			resp.put("message", "Invalid source specified. Please specify if you are debiting a wallet or a card");
			return Response.status(200).entity(resp.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return Response.status(200).entity(resp.toString()).build();
		}
	}
	
	
	
	/*@GET
	@Path("/listEWalletAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listEWalletAccounts(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") Integer status, @QueryParam("token") String token)
	{
		//Status values
		//ACTIVE, - 0 
		//INACTIVE, - 1
		//ADMIN_DISABLED, - 2 
		//LOCKED - 3
		JSONObject jsonObject = new JSONObject();
		try{
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
			log.info("verifyJ ==" + verifyJ.toString());
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			log.info("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			log.info("staff_bank_code ==" + staff_bank_code);
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
			Wallet wallet = null;
			
			String hql = "Select tp from Wallet tp";
			if(status!=null)
			{
				hql = hql + " WHERE tp.status" + WalletStatus.values()[status];
			}
			

			Collection<Wallet> walletAcctList = (Collection<Wallet>)this.swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("walletAcctList", new Gson().toJson(new ArrayList<Wallet>(walletAcctList)));
			jsonObject.put("message", "Wallet Accounts pulled succcessfully");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return jsonObject;
	}
	


	@GET
	@Path("/getEWalletAccountBalance")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getEWalletAccountBalance(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, @QueryParam("merchantCode") String merchantCode, @QueryParam("deviceCode") String deviceCode)
	{
		JSONObject jsonObject = new JSONObject();
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			String staff_bank_code = "PROBASEWALLET";
			String bankKey = UtilityHelper.getBankKey(staff_bank_code, swpService);
			
				
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
			
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			String walletCode = verifyJ.getString("branchCode");
			String hql = "Select tp from WalletAccount tp where tp.wallet.userId = " + user.getId() + " AND tp.wallet.status = " + 
					WalletStatus.ACTIVE.ordinal();
		    Collection<WalletAccount> acctList = (Collection<WalletAccount>)this.swpService.getAllRecordsByHQL(hql);
			    
				Double totalBalance = 0.0;
				if(acctList.size()>0)
				{
					Iterator<WalletAccount> iter = acctList.iterator();
					JSONObject acctBalances = new JSONObject();
					while(iter.hasNext())
					{
						WalletAccount walletaccount = (WalletAccount)iter.next();
						Account acct = walletaccount.getAccount();
						Wallet wallet = walletaccount.getWallet();
						
						if(wallet.getWalletType().equals(WalletType.ZICB_WALLET))
						{
							
							JSONObject balanceDet = this.getWalletDetails(httpHeaders, requestContext, acct.getBank().getBankCode(), acct.getAccountIdentifier(), merchantCode, deviceCode);
							
							if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.GENERAL_OK)
							{
								String accountList = balanceDet.getString("accountList");
								log.info("accountList ---" + accountList);
								JSONArray accounts = new JSONArray(accountList);
								for(int i=0; i<accounts.length(); i++)
								{
									JSONObject acct_ = accounts.getJSONObject(i);
									log.info("acct_ ==" + acct_.toString());
									//JSONObject acct_ = accounts;
									Double currentbalance = acct_.getDouble("currentbalance");
									log.info("currentbalance --  " + currentbalance);
									Double availablebalance = acct_.getDouble("availablebalance");
									log.info("availablebalance --  " + availablebalance);
									totalBalance = totalBalance + currentbalance;
									log.info("totalBalance --  " + totalBalance);
									acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), currentbalance);
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
							acctBalances.put(acct.getId() + "---" + acct.getAccountIdentifier(), lastTransaction.getClosingBalance());
						}
								
					}
					jsonObject.put("status", ERROR.EWALLET_BALANCE_PULL_SUCCESS);
					jsonObject.put("message", "Balance Retrieved");
					jsonObject.put("balanceList", new Gson().toJson(acctBalances));
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
			e.printStackTrace();
		}
		return jsonObject;
	}
	


	@GET
	@Path("/getAllEWalletAccountBalanceByCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getAllEWalletAccountBalanceByCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantCode") String merchantCode, @QueryParam("deviceCode") String deviceCode, 
			@QueryParam("serviceTypeId") String serviceTypeId, @QueryParam("customerNumber") String customerNumber, @QueryParam("hash") String hash)
	{
		JSONObject jsonObject = new JSONObject();
		log.info("merchantCode ... " + merchantCode);
		log.info("deviceCode ... " + deviceCode);
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
				return jsonObject;
			}
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "No Device match Found");
				return jsonObject;
			}
			
			String api_key = merchant.getApiKey();
			if(UtilityHelper.validateTransactionHash(
					hash, 
					merchantCode,
					deviceCode,
					serviceTypeId,
					customerNumber,
					0.00,
					device.getSuccessUrl(),
					api_key)==true)
			{
			
				hql = "Select tp from WalletAccount tp where tp.wallet.customer.verificationNumber = '" + customerNumber + "' AND tp.wallet.status = " + 
					WalletStatus.ACTIVE.ordinal();
				log.info(hql);
				Collection<WalletAccount> acctList = (Collection<WalletAccount>)this.swpService.getAllRecordsByHQL(hql);
			    log.info(acctList.size());
				Double totalBalance = 0.0;
				if(acctList.size()>0)
				{
					Iterator<WalletAccount> iter = acctList.iterator();
					JSONArray acctBalances = new JSONArray();
					while(iter.hasNext())
					{
						WalletAccount walletaccount = (WalletAccount)iter.next();
						Account acct = walletaccount.getAccount();
						Wallet wallet = walletaccount.getWallet();
						log.info(wallet.getWalletType().name());
						if(wallet.getWalletType().equals(WalletType.ZICB_WALLET))
						{
							
							JSONObject balanceDet = this.getWalletDetails(httpHeaders, requestContext, acct.getBank().getBankCode(), acct.getAccountIdentifier(), merchantCode, deviceCode);
							log.info("balanceStr == " + balanceDet.toString());
							
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
									js_.put("walletNumber", wallet.getWalletCode());
									js_.put("accountId", acct.getId());
									js_.put("currentBalance", currentbalance);
									js_.put("availableBalance", availablebalance);
									js_.put("walletType", wallet.getWalletType().name());
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
							js_.put("walletType", wallet.getWalletType().name());
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
		return jsonObject;
	}
	
	


	@GET
	@Path("/listEWalletAccountsByCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listEWalletAccountsByCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("verificationNumber") String verificationNumber)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			Wallet wallet = null;
			
			String hql = "Select tp from Wallet tp where tp.customer.verificationNumber = '"+verificationNumber+"' AND tp.deleted_at IS NULL";
			Collection<Wallet> walletAcctList = (Collection<Wallet>)this.swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("walletAcctList", new Gson().toJson(new ArrayList<Wallet>(walletAcctList)));
			jsonObject.put("message", "Wallet Accounts pulled succcessfully");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	


	@GET
	@Path("/getUserAccountBalances")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getUserAccountBalances(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("merchantCode") String merchantCode, 
			@QueryParam("deviceCode") String deviceCode)
	{
		JSONObject jsonObject = new JSONObject();
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
			
			
			String hql = "Select tp from Transaction tp ";
			if(subject!=null)
			{
				User user = (User)(new Gson().fromJson(subject, User.class));
				hql = "Select tp from Wallet tp where tp.user.id = " + user.getId() +
						"AND tp.user.status = " + UserStatus.ACTIVE.ordinal() + " AND tp.status = " + WalletStatus.ACTIVE.ordinal();
				Wallet wallet = (Wallet)this.swpService.getUniqueRecordByHQL(hql);
				
				
				
				hql = "Select tp from WalletAccount tp where tp.wallet.id = " + wallet.getId() + " AND tp.deleted_at IS NULL";
				Collection<WalletAccount> walletAccounts = (Collection<WalletAccount>)this.swpService.getAllRecordsByHQL(hql);
				
				Iterator<WalletAccount> itr = walletAccounts.iterator();
				Double balance = 0.0;
				JSONObject getBalance = null;
				JSONObject balances = new JSONObject();
				String key = "";
				Double value = 0.00;
				while(itr.hasNext())
				{
					WalletAccount wAcct = (WalletAccount)itr.next();
					getBalance = UtilityServicesV2.getCurrentBalance(httpHeaders, requestContext, wAcct.getAccount(), null, swpService, Channel.WALLET); 
					if(getBalance.has("balance"))
						balance = balance + (getBalance.getDouble("balance"));  
					
					key = wAcct.getAccount().getId() + "|||" + wAcct.getAccount().getAccountIdentifier() + "|||" + wAcct.getAccount().getBank().getBankName();
					value = getBalance.getDouble("balance");
					balances.put(key, value);
				}
				
				hql = "Select tp from Merchant tp where tp.merchantCode = '" + merchantCode + "' AND tp.status = " + MerchantStatus.ACTIVE.ordinal();
				Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
				Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
				
				jsonObject.put("status", ERROR.TRANSACTION_SUCCESS);
				jsonObject.put("message", "Account balances successfully pulled");
				jsonObject.put("balance", balance);
				jsonObject.put("merchant", new Gson().toJson(merchant));
				jsonObject.put("device", new Gson().toJson(device));
				jsonObject.put("balanceList", balances.toString());
			}
			else
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	


	@GET
	@Path("/getWalletDetails")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getWalletDetails(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("bankCode") String bankCode, 
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
			String hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"' AND tp.deleted_at IS NULL";
			log.info(hql);
			Bank bank = (Bank)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
		    		"AND tp.deviceCode = '" + deviceCode + "'";
			log.info(hql);
		    Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
		    if(device==null)
		    {
		    	log.info("device not found");
		    	jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "We could not find a device mapped to the device code and merchant id");
				return jsonObject;
		    }
		    
		    
			if(bank!=null && (bank.getBankCode().equals("3001") || bank.getBankCode().equals("3002")))
			{
				log.info("bank found");
				parametersRequest.put("accountNos", bankAccountNo);
				parametersRequest.put("serviceKey", device.getZicbServiceKey());
				parameters.put("request", parametersRequest);
				parameters.put("service", "ZB0629");
				JSONObject header = new JSONObject();
				header.put("authKey", device.getZicbAuthKey());
				header.put("Content-Type", "application/json; utf-8");
				header.put("Accept", "application/json");
				
				log.info(header.toString());
				log.info(parameters.toString());
				
				String walletDetails = null;
				if(bank.getIsLiveYes()!=null && bank.getIsLiveYes().equals(Boolean.TRUE))
				{
					log.info(">>>>");
					walletDetails = UtilityHelper.sendPost(bank.getLiveEndpoint(), parameters.toString(), header);
				}
				else
				{
					log.info("<<<<");
					walletDetails = UtilityHelper.sendPost(bank.getDemoEndpoint(), parameters.toString(), header);
				}
				
				JSONObject walletDetailsJS = new JSONObject(walletDetails);
				Integer status = walletDetailsJS.getInt("status");
				//Integer status = 200;
				if(status==200)
				{
					
					JSONObject response = walletDetailsJS.getJSONObject("response");
					JSONArray accountList = response.getJSONArray("accountList");
					
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Wallet found");
					jsonObject.put("accountList", accountList.toString());
					
					log.info("resp....1.." + jsonObject.toString());
					return jsonObject;
				}
				
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "We could not obtain details about this wallet");
				return jsonObject;
			}
			
			jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
			jsonObject.put("message", "We could not obtain details about this wallet");
			return jsonObject;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (new JSONObject());
		
	}
	
	


	@POST
	@Path("/createBankWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createBankWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,@FormParam("token") String token, @FormParam("merchantCode") String merchantCode, @FormParam("deviceCode") String deviceCode, @FormParam("firstName") String firstName, 
			@FormParam("lastName") String lastName, @FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, @FormParam("addressLine3") String addressLine3, @FormParam("addressLine4") String addressLine4, 
			@FormParam("addressLine5") String addressLine5, @FormParam("uniqueType") String uniqueType, @FormParam("uniqueValue") String uniqueValue, @FormParam("dateOfBirth") String dateOfBirth, @FormParam("email") String email, 
			@FormParam("sex") String sex, @FormParam("mobileNumber") String mobileNumber, @FormParam("accType") String accType, @FormParam("currency") String currency, @FormParam("idFront") String idFront, @FormParam("idBack") String idBack, 
			@FormParam("custImg") String custImg, @FormParam("custSig") String custSig, @FormParam("bankCode") String bankCode, @FormParam("eWalletAccountCreateTrue") Boolean eWalletAccountCreateTrue, @FormParam("mobileMoneyCreateTrue") Boolean mobileMoneyCreateTrue,
			@FormParam("parentCustomerId") Long parentCustomerId, @FormParam("parentAccountId") Long parentAccountId)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			
			String hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"' AND tp.deleted_at IS NULL";
			Bank bank = (Bank)swpService.getUniqueRecordByHQL(hql);
			
			resp = createZicbWallet(httpHeaders, requestContext, token, merchantCode, deviceCode, firstName, 
					lastName, addressLine1, addressLine2, addressLine3, addressLine4, 
					addressLine5, uniqueType, uniqueValue, dateOfBirth, email, 
					sex, mobileNumber, accType, currency, idFront, idBack, 
					custImg, custSig, bank, eWalletAccountCreateTrue, mobileMoneyCreateTrue,
					parentCustomerId, parentAccountId);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resp;
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
			@FormParam("custImg") String custImg, @FormParam("customerId") String custSig, @FormParam("bankCode") String bankCode, @FormParam("serviceTypeId") String serviceTypeId, @FormParam("orderId") String orderId, @FormParam("responseUrl") String responseUrl, @FormParam("isWalletOrAccount") Integer isWalletOrAccount)
	{
		JSONObject resp = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			
			String hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"' AND tp.deleted_at IS NULL";
			Bank bank = (Bank)swpService.getUniqueRecordByHQL(hql);
			
			resp = mapExistingZicbWallet(httpHeaders, requestContext, token, merchantCode, deviceCode, accountNumber, firstName, 
					lastName, addressLine1, addressLine2, addressLine3, addressLine4, 
					addressLine5, uniqueType, uniqueValue, dateOfBirth, email, 
					sex, mobileNumber, accType, currency, idFront, idBack, 
					custImg, custSig, bank, serviceTypeId, orderId, responseUrl, isWalletOrAccount);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return resp;
	}
	


	public JSONObject createZicbWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,String token, String merchantCode, String deviceCode, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Bank bank, Long parentCustomerId, Long parentAccountId)
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
			log.info("verifyJ ==" + verifyJ.toString());
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			log.info("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			log.info("staff_bank_code ==" + staff_bank_code);
			
			String hql = "Select tp from Wallet tp where tp.customer.user.username = '"+mobileNumber+"' AND tp.deleted_at IS NULL AND tp.walletType = " + WalletType.ZICB_WALLET.ordinal();
			Wallet wallet = (Wallet)swpService.getUniqueRecordByHQL(hql);
			
			if(wallet!=null)
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
				log.info(hql);
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Account tp where tp.id = "+parentAccountId+" AND tp.deleted_at IS NULL";
				log.info(hql);
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
			log.info(hql);
			Merchant merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
			
			if(merchant==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
				jsonObject.put("message", "Merchant Code Invalid");
				return jsonObject;
			}
			

			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
			log.info(hql);
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
			log.info(parameters.toString());
			log.info(header.toString());
			log.info("Test");
			log.info(device.getZicbAuthKey());
			String newWalletResponse = null;
			if(device.getIsTestYes()!=null && device.getIsTestYes().equals(Boolean.TRUE))
			{
				log.info(UtilityHelper.ZICB_CREATE_WALLET_URL_TEST); 
				//newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_CREATE_WALLET_URL_TEST, parameters.toString(), header);
				
				newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				log.info(UtilityHelper.ZICB_CREATE_WALLET_URL); 
				newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_CREATE_WALLET_URL, parameters.toString(), header);
			}
			log.info(newWalletResponse);
			
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
				log.info("Test");
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
			log.info(response.toString());
			if(response.get("cust")==null)
				log.info("nullable cust");
			else
				log.info("non nullable cust");
			String operationStatus = responseHeader.getInt("status");
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
					log.info("accNo..." + accNo_);
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
					User user = null;
					if(eWalletAccountCreateTrue!=null && eWalletAccountCreateTrue.equals(Boolean.TRUE))
					{
						hql = "Select tp from User tp where lower(tp.username) = '" + mobileNumber + "'";
						user = (User)this.swpService.getUniqueRecordByHQL(hql);
						if(user==null)
						{
							user = new User();
							user.setWebActivationCode(webActivationCode);
							user.setMobileActivationCode(mobileActivationCode);
							user.setFailedLoginCount(0);
							user.setUsername(mobileNumber);
							user.setUserEmail(email);
							user.setLockOut(Boolean.FALSE);
							user.setCreated_at(new Date());
							user.setStatus(UserStatus.ACTIVE);
							user.setRoleType(RoleType.CUSTOMER);
							user.setFirstName(firstName);
							user.setLastName(lastName);
							user.setMobileNo(mobileNumber);
							user.setPassportImage(custImg);
							
							
							String pswd = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
							log.info("pswd ===>" + pswd);
							String encryptedpswd = UtilityHelper.encryptData(pswd, bankKey);
							user.setPassword(encryptedpswd);
							jsonObject.put("useracctid", pswd);
							user = (User)this.swpService.createNewRecord(user);

							jsonObject.put("userId", user.getId());
							jsonObject.put("userpwd", pswd);
						}
						
						
						customer.setUser(user);
						this.swpService.updateRecord(customer);
					}
					
					hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
					Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
					hql = "Select tp from PoolAccount tp where tp.status = '" + AccountStatus.ACTIVE + "' ORDER BY rand()";
					Collection<PoolAccount> poolAccountList = (Collection<PoolAccount>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
					Iterator<PoolAccount> it = poolAccountList.iterator();
					PoolAccount poolAccount = (PoolAccount)it.next();
					Account account = new Account(customer, AccountStatus.ACTIVE, null, branch_code, bank, currency, AccountType.VIRTUAL, accNo_, 
							customerAccountList==null ? 0 : customerAccountList.size(), poolAccount, eWalletAccountCreateTrue, mobileMoneyCreateTrue, 
							user.getId(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, ProbasePayCurrency.valueOf(currency));
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
					transaction.setPoolAccount(poolAccount);
					transaction.setCreditPoolAccountTrue(true);
					transaction.setTransactingBankId(account.getBank().getId());
					transaction.setReceipientChannel(Channel.OTC);
					transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
					transaction.setReceipientEntityId(account.getId());
					transaction.setDevice(device);
					transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
					transaction.setMerchantId(merchant.getId());
					transaction.setCustomerUserId(user.getId());
					transaction = (Transaction)this.swpService.createNewRecord(transaction);
					jsonObject.put("transactionId", transaction.getId());
					
					
					
		
		
					if(eWalletAccountCreateTrue!=null && eWalletAccountCreateTrue.equals(Boolean.TRUE))
					{						
						wallet = new Wallet();
						wallet.setCreated_at(new Date());
						wallet.setCustomer(customer);
						wallet.setStatus(WalletStatus.ACTIVE);
						wallet.setWalletUniqueId(accNo_);
						wallet.setWalletType(WalletType.ZICB_WALLET);
						String walletCode = RandomStringUtils.randomNumeric(4);
						wallet.setWalletCode(walletCode);
						wallet.setUser(user);
						wallet.setUserId(user==null ? null : user.getId());
						jsonObject.put("walletcodedetail", walletCode);
						this.swpService.createNewRecord(wallet);
						account.setEwalletaccount(true);
						this.swpService.updateRecord(account);
						
						WalletAccount walletAccount = new WalletAccount();
						walletAccount.setWallet(wallet);
						walletAccount.setAccount(account);
						walletAccount.setCreated_at(new Date());
						walletAccount.setCustomerId(customer.getId());
						walletAccount.setUserId(user==null ? null : user.getId());
						this.swpService.createNewRecord(walletAccount);
						
						jsonObject.put("walletNumber", wallet.getWalletCode());
					}
					
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
			String custImg, String custSig, Bank bank, String serviceTypeId, String orderId, String responseUrl, Integer isWalletOrAccount)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			
			String hql = "Select tp from Wallet tp where tp.customer.user.username = '"+mobileNumber+"' AND tp.deleted_at IS NULL AND tp.walletType = " + WalletType.ZICB_WALLET.ordinal();
			Wallet wallet = (Wallet)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
			log.info(hql);
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
				log.info("To HAsh = " + merchantCode+"-"+deviceCode+"-"+serviceTypeId+"-"+customerNumber+"-"+amt+"-"+responseUrl+"-"+api_key);
				String hash = "";
				try {
					hash = UtilityHelper.get_SHA_512_SecurePassword(toHash);
					log.info("1.hash = " + hash);
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					log.info(e.getMessage());
				}
				
				 
				JSONObject balanceStr = this.getAllEWalletAccountBalanceByCustomer(httpHeaders, requestContext, merchantCode, deviceCode, serviceTypeId, customerNumber, hash);
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
			
			log.info("Test");
			log.info(device.getZicbAuthKey());
			log.info(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL);
			String newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL, parameters.toString(), header);
			log.info(newWalletResponse);
			
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
							String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, "260" + mobileNo, "", device);
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
				log.info("Test");
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



	@POST
	@Path("/verifyZICBOTPAndCreateWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject verifyZICBOTPAndCreateWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,@FormParam("token") String token, @FormParam("merchantCode") String merchantCode, @FormParam("deviceCode") String deviceCode, @FormParam("firstName") String firstName, 
			@FormParam("lastName") String lastName, @FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, @FormParam("addressLine3") String addressLine3, @FormParam("addressLine4") String addressLine4, 
			@FormParam("addressLine5") String addressLine5, @FormParam("uniqueType") String uniqueType, @FormParam("uniqueValue") String uniqueValue, @FormParam("dateOfBirth") String dateOfBirth, @FormParam("email") String email, 
			@FormParam("sex") String sex, @FormParam("mobileNumber") String mobileNumber, @FormParam("accType") String accType, @FormParam("idFront") String idFront, @FormParam("idBack") String idBack, 
			@FormParam("custImg") String custImg, @FormParam("custSig") String custSig, @FormParam("serviceTypeId") String serviceTypeId, @FormParam("orderId") String orderId, @FormParam("responseUrl") String responseUrl, 
			@FormParam("isWalletOrAccount") Integer isWalletOrAccount, 
			@FormParam("otpref") String otpref, @FormParam("otp") String otp, @FormParam("bankCode") String bankCode) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"' AND tp.deleted_at IS NULL";
			log.info(hql);
			this.swpService = this.serviceLocator.getSwpService();
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "No Device match Found");
				return jsonObject;
			}
			String[] verify = UtilityHelper.verifyZICBWalletOTP(jsonObject, mobileNumber, email, otpref, otp, device.getZicbAuthKey());
			
			
			String[] resp = new String[2];
			if(verify==null)
			{
				jsonObject.put("status", ERROR.OTP_CHECK_FAIL);
				jsonObject.put("message", "Your OTP could not be verified on this transaction.");
				return jsonObject;
			}
			else
			{
				if(resp[0]=="1")
				{
					hql = "Select tp from Bank tp where tp.bankCode = '"+bankCode+"' AND tp.deleted_at IS NULL";
					log.info(hql);
					Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
					
					
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
					
					log.info("Test");
					log.info(device.getZicbAuthKey());
					log.info(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL);
					String newWalletResponse = UtilityHelper.sendPost(UtilityHelper.ZICB_QUERY_BY_MOBILE_NUMBER_URL, parameters.toString(), header);
					log.info(newWalletResponse);
					
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
								String uniqueIdName = custAccDetails_.getString("uniqueIdName");
								String ccy = custAccDetails_.getString("ccy");
								
								String mobileNumberSub = mobileNumber.substring(mobileNumber.length() - 9);
								
								if(mobileNumberSub.equals(mobileNo))
								{
									JSONObject createResponseJSON = this.createZicbWallet(httpHeaders, requestContext, token, merchantCode, deviceCode, accDesc_.length>0 ? accDesc_[0] : firstName, 
											accDesc_.length>1 ? accDesc_[(accDesc_.length-1)] : lastName, addressLine1, addressLine2, addressLine3, addressLine4, 
											addressLine5, uniqueIdName, uniqueIdVal, dateOfBirth, email, 
											sex, "260"+mobileNo, accType, ccy, idFront, idBack, 
											custImg, custSig, bank, false, false, null, null);
									
									 
									String customerNumber = createResponseJSON.getString("customerNumber");
									
									Double amt = 0.00;
									String api_key = device.getMerchant().getApiKey();
									String toHash = merchantCode+deviceCode+serviceTypeId+customerNumber+amt+responseUrl+api_key;
									log.info("To HAsh = " + merchantCode+"-"+deviceCode+"-"+serviceTypeId+"-"+customerNumber+"-"+amt+"-"+responseUrl+"-"+api_key);
									String hash = "";
									try {
										hash = UtilityHelper.get_SHA_512_SecurePassword(toHash);
										log.info("1.hash = " + hash);
										
									} catch (UnsupportedEncodingException e) {
										// TODO Auto-generated catch block
										log.info(e.getMessage());
									}
									
									JSONObject balanceStr = this.getAllEWalletAccountBalanceByCustomer(httpHeaders, requestContext, merchantCode, deviceCode, serviceTypeId, customerNumber, hash);
									jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
									jsonObject.put("message", "You already have a wallet in ZICB");
									jsonObject.put("accountBalances", balanceStr.toString());
									return createResponseJSON;
								}
								else
								{
									jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
									jsonObject.put("message", "Your new customer account could not be created.");
									return jsonObject;
								}
								
								
								
							}
							else
							{
								jsonObject.put("status", ERROR.INVALID_WALLET_PROVIDED);
								jsonObject.put("message", "We could not find any bank account linked to the details you shared");
								return jsonObject;
							}
						}
						else
						{
							jsonObject.put("status", ERROR.INVALID_WALLET_PROVIDED);
							jsonObject.put("message", "We could not find any bank account linked to the details you shared");
							return jsonObject;
						}
					}
					catch(Exception e)
					{
						log.info("Test");
						e.printStackTrace();
						jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
						jsonObject.put("message", "Your new customer account could not be created.");
						return jsonObject;
					}
					
				}
				else
				{
					return new JSONObject(resp[1]);
					//return jsonObject;
				}
			}
		}
		catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
			return null;
		}
	}*/
	
	
	
	
}
