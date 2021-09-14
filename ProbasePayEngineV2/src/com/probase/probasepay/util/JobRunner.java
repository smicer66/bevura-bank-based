package com.probase.probasepay.util;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.BillType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.SourceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.FundsTransfer;
import com.probase.probasepay.models.GroupMember;
import com.probase.probasepay.models.GroupPaymentsExpected;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;

public class JobRunner {

	private static Logger log = Logger.getLogger(Test.class);
	private static ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public static SwpService swpService = null;
	public static String ENDPOINT = "localhost:8080";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Application app = Application.getInstance(swpService);
			swpService = serviceLocator.getSwpService();
			handleVillageBankingDebits(swpService, app);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.info(e);
		}

	}

	private static void handleVillageBankingDebits(SwpService swpService2, Application app) {
		// TODO Auto-generated method stub
		try {
			/*Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String latestDate = sdf.format(today);
			String hql = "Select tp.*, c.id as customerId from group_payments_expected tp, group_members gm, customers c, accounts ac where tp.groupMember_id = gm.id "
					+ "AND gm.addedCustomer_id = c.id AND ac.customer_id = c.id AND "
					+ "tp.isPaid = 0 AND tp.deletedAt IS NULL AND tp.dateExpected < '"+ latestDate +"'";
			System.out.println(hql);
			List<Map<String, Object>> group_payments_expecteds = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			group_payments_expecteds.forEach(gpe -> {
				
				try {
					Long accountId = ((Long)gpe.get("account_id")).longValue();
					String sql = "Select sum(tp.cardBalance) as cardBalanceTotal from ecards tp where tp.account_id = " + accountId + " AND tp.deletedAt IS NULL";
					System.out.println(sql);
					List<Map<String, Object>> cardBalances;
					cardBalances = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(sql);
					if(cardBalances!=null && cardBalances.size()>0)
					{
						Map<String, Object> cardBalanceTotal = cardBalances.get(0);
						Double cardBalance = ((Double)cardBalanceTotal.get("cardBalanceTotal")).doubleValue();
						System.out.println(cardBalance);
						
						Double accountBalance = 10000.00;
						Double charges = 0.00;
						if(cardBalance==null)
						{
							cardBalance = 0.00;
						}
						
						Double amount = ((Double)gpe.get("outstandingBalance")).doubleValue();
						if(amount<(accountBalance - cardBalance - charges))
						{
							Boolean creditAccountTrue = true;
							Boolean creditCardTrue = false;
							Boolean debitAccountTrue = true;
							Boolean debitCardTrue = false;
							Boolean creditPoolAccountTrue = false;
							Long creditCardId = null;
							Long debitCardId = null;
							//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
							
							String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
							String bankPaymentReference = null;
							Long customerId = ((Long)gpe.get("customerId")).longValue();
							String orderRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
							String rpin = null;
							Channel chanel = Channel.SYSTEM;
							Date transactionDate = new Date();
							ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
							String payerName = ((String)gpe.get("firstName")) + " " + ((Long)gpe.get("lastName"));
							String payerEmail = ((String)gpe.get("contactEmail"));
							String payerMobile = ((String)gpe.get("contactMobile"));
							TransactionStatus transactionStatus = TransactionStatus.PENDING;
							ProbasePayCurrency probasePayCurrency = ProbasePayCurrency.values()[((Integer)gpe.get("probasePayCurrency")).intValue()];
							String transactionCode = TransactionCode.transactionPending;
							Long sourceAccount = ((Long)gpe.get("accountId")).longValue();
							Long debitAccountId = sourceAccount;
							Long device_id =  ((Long)gpe.get("deviceId")).longValue();
							Long receipientEntityId = ((Long)gpe.get("villageBankingHoldingAccountId")).longValue();
							Long creditAccountId = receipientEntityId;
							String messageRequest = null;
							String messageResponse =  null;
							String responseCode = "01";
							Double schemeTransactionPercentage = 0.00;
							String merchantName = ((String)gpe.get("merchantName"));
							String merchantCode = ((String)gpe.get("merchantCode"));
							String deviceCode = ((String)gpe.get("deviceCode"));
							String merchantBank = null;
							String merchantAccount = null;
							Long transactingBankId = ((Long)gpe.get("bankId")).longValue();
							Long receipientTransactingBankId = ((Long)gpe.get("villageBankingHoldingAccountBankId")).longValue();
							Integer accessCode = null;
							Long sourceEntityId = ((Long)gpe.get("accountId")).longValue();
							Channel receipientChannel = Channel.SYSTEM;
							String narration_ = "GROUP CONTRIBUTION~"+payerName+"~" + amount;
							
							String transactionDetail = narration_;
							Double closingBalance = null;
							Double totalCreditSum = null;
							Double totalDebitSum = null;
							Long paidInByBankUserAccountId = null;
							String customData = null;
							String responseData = null;
							Long adjustedTransactionId = null;
							Long acquirerId = ((Long)gpe.get("acquirerId")).longValue();
							Long merchantId = ((Long)gpe.get("merchantId")).longValue();
							Double transactionCharge = 0.00;
							Double transactionPercentage = 0.00;
							Double schemeTransactionCharge = 0.00;
							Double fixedCharge = 0.00;

							String sourceIdentityNo = ((String)gpe.get("accountIdentifier"));
							String sourceType = SourceType.ACCOUNT.name();
							String sourceCustomerName = payerName;
							Long sourceCustomerId = customerId;
							String sourceBankName = ((String)gpe.get("bankName"));
							Long sourceBankId = transactingBankId;
							String receipientIdentityNo = ((String)gpe.get("receipientIdentityNo"));
							String receipientType = SourceType.ACCOUNT.name();
							String receipientCustomerName = "Village Banking Holding Account";
							Long receipientCustomerId = null;
							String receipientBankName = ((String)gpe.get("bankName"));
							Long receipientBankId = transactingBankId;
							//String narration = "DR~CARD~" + sourceIdentityNo + "~CARD~" + receipientIdentityNo + "~" + sourceCustomerName + "~" + receipientCustomerName + "~" + transactionRef;
							Double sourcePriorBalance = accountBalance;
							Double sourceNewBalance = null;
							ServiceType st = serviceType;
							String debitOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
							String creditOrderRef = RandomStringUtils.randomAlphabetic(16).toUpperCase();
							String OTP = null;
							String OTPRef = null;
							Date todayDate = new Date();
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String created_at = sdf.format(todayDate);
							String updated_at = sdf.format(todayDate);
							String deleted_at = null;
							String creditBankPaymentReference = null;
							String debitBankPaymentReference = null;
							
							Long transactingIssuerId = ((Long)gpe.get("transactingIssuerId")).longValue();
							Long card_id = null;
							Long account_id = sourceAccount;
							String details = null;
							String summary = null;
							Integer status = TransactionStatus.PENDING.ordinal();
							
							
							sql = "INSERT INTO "
									+ "transactions(OTP, OTPRef, accessCode, adjustedTransactionId, amount, bankPaymentReference, channel, "
									+ "closingBalance, created_at, creditAccountTrue, creditCardTrue, creditPoolAccountTrue, "
									+ "customData, customerId, deleted_at, fixedCharge, merchantAccount, merchantBank, merchantCode, "
									+ "merchantId, merchantName, messageRequest, messageResponse, orderRef, paidInByBankUserAccountId, "
									+ "payerEmail, payerMobile, payerName, probasePayCurrency, receipientChannel, receipientEntityId, "
									+ "receipientTransactingBankId, responseCode, responseData, rpin, schemeTransactionCharge, "
									+ "schemeTransactionPercentage, serviceType, sourceEntityId, status, totalCreditSum, totalDebitSum, "
									+ "transactingBankId, transactingIssuerId, transactionCharge, transactionCode, transactionDate, "
									+ "transactionDetail, transactionPercentage, transactionRef, updated_at, account_idIndex, acquirer_idIndex, "
									+ "card_idIndex, device_idIndex, acquirerId, creditAccountId, creditBankPaymentReference, "
									+ "creditCardId, debitAccountId, debitAccountTrue, debitBankPaymentReference, debitCardId, "
									+ "debitCardTrue, details, summary) VALUES "
									+ "VALUES('" + OTP + "', '" + OTPRef + "', '" + accessCode + "', '" + adjustedTransactionId + "', '" + amount + "', '" + bankPaymentReference + "', '" + chanel.ordinal() + "', '"
									+ closingBalance + "', '" + created_at + "', '" + creditAccountTrue + "', '" + creditCardTrue + "', '" + creditPoolAccountTrue + "', '"
									+ customData + "', '" + customerId + "', '" + deleted_at + "', '" + fixedCharge + "', '" + merchantAccount + "', '" + merchantBank + "', '" + merchantCode + "', '"
									+ merchantId + "', '" + merchantName + "', '" + messageRequest + "', '" + messageResponse + "', '" + orderRef + "', '" + paidInByBankUserAccountId + "', '"
									+ payerEmail + "', '" + payerMobile + "', '" + payerName + "', '" + probasePayCurrency + "', '" + receipientChannel + "', '" + receipientEntityId + "', '"
									+ receipientTransactingBankId + "', '" + responseCode + "', '" + responseData + "', '" + rpin + "', '" + schemeTransactionCharge + "', '"
									+ schemeTransactionPercentage + "', '" + serviceType + "', '" + sourceEntityId + "', '" + status + "', '" + totalCreditSum + "', '" + totalDebitSum + "', '"
									+ transactingBankId + "', '" + transactingIssuerId + "', '" + transactionCharge + "', '" + transactionCode + "', '" + transactionDate + "', '"
									+ transactionDetail + "', '" + transactionPercentage + "', '" + transactionRef + "', '" + updated_at + "', '" + account_id + "', '" + acquirerId + "', '"
									+ card_id + "', '" + device_id + "', '" + acquirerId + "', '" + creditAccountId + "', '" + creditBankPaymentReference + "', '"
									+ creditCardId + "', '" + debitAccountId + "', '" + debitAccountTrue + "', '" + debitBankPaymentReference + "', '" + debitCardId + "', '"
									+ debitCardTrue + "', '" + details + "', '" + summary + "')";
							swpService2.insertIntoDb(sql);							
							
							
							Long transactionId = transaction.getId();
							
							
							hql = "Select tp from Account tp where tp.accountIdentifier = '"+receipientIdentityNo+"'";
							Account receipientAccount = (Account)swpService2.getUniqueRecordByHQL(hql);
							hql = "Select tp from Account tp where tp.id = "+sourceAccount;
							Account sourceAccount_ = (Account)swpService2.getUniqueRecordByHQL(hql);
							
							User user = receipientAccount.getCustomer().getUser();
							Acquirer acquirer = receipientAccount.getAcquirer();
							Device device = (Device)swpService2.getUniqueRecordByHQL("Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'");
							Gson gson = new Gson();
							String obj = gson.toJson(user);
							System.out.println("tkobj ==" + obj);
							//Key jwtKey = Application.getKey();
							//String token = Jwts.builder().setSubject(obj).signWith(SignatureAlgorithm.HS512, jwtKey).compact();
							String tkId = RandomStringUtils.randomAlphanumeric(10);
							System.out.println("tkId ==" + tkId);
							String token = "";
							token = app.createJWT(tkId, acquirer.getAcquirerCode(), 
									null, obj, (3*60*1000));
							
							
							
							//UtilityHelper.fundCard(this.swpService, httpHeaders, requestContext, amount, card, account, token, merchantId, deviceCode, user, channel, orderRef, transaction);
							
							
							
							
							log.info("INTERNAL FUNDS TRANSFER");
							log.info("===================");
							JSONObject ftRes = UtilityHelper.internalFundsTransfer(app, swpService, null, null, amount, sourceAccount_, 
									receipientAccount, token, merchantCode, deviceCode, user, chanel.name(), orderRef, narration_, acquirer, device);
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
											sourceAccount_.getAccountIdentifier(), 
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
											transactionRef, debitOrderRef, creditOrderRef, transactionId, bankReferenceNo, ftRes.toString(), ftRes.toString(), bankReferenceNo, chargeBankPaymentReference);
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
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
