package com.probase.probasepay.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
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
import javax.ws.rs.POST;
import javax.ws.rs.GET;
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
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
//import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.AccountMapRequest;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BevuraToken;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardBin;
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


@Path("/CustomerServicesV2")
public class CustomerServicesV2 {

	private static Logger log = Logger.getLogger(CustomerServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	
	/**Service Method - createNewCustomerAccount
	 * Method for bank to profile Eagle Card customers
	 * at a bank/financial institution. Profiling of 
	 * those want to make use of Eagle Card Service
	 * 
	 * Create Customer
	 * Create Customer Virtual Account
	 * Create Card Mapped to the Account and Customer
	 * Create MobileMoney Account for Card Created (Optional)
	 * Create User profile for eWallet option (Web login)
	 * 
	 * 
	 * 
	 * @param status - MerchantStatus
	 * @param parentCustomerId - For Customers under a corporate account. This is the id of the corporate customer
	 * @param parentAccountId - For Customers under a corporate account. This is the id of the account of the corporate customer
	 * @return Stringified JSONObject of the list of merchants
	 */
	
	
	
	
	
	@POST
	@Path("/createNewCustomerAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewCustomerAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
			@FormParam("district") Integer locationDistrict_id, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("token") String token, 
			@FormParam("customerType") String customerType, 
			@FormParam("parentCustomerId") Long parentCustomerId, 
			@FormParam("parentAccountId") Long parentAccountId, 
			@FormParam("meansOfIdentificationType") String meansOfIdentificationType, 
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber, 
			@FormParam("merchantCode") String merchantCode,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("acquirerId") String acquirerId,
			@FormParam("userId") Long userId,
			@FormParam("isTokenize") Integer isTokenize,
			@FormParam("isSettlementAccount") Integer isSettlementAccount
			)
	{
		 //acquirerId= "PROBASE";
		String logId = RandomStringUtils.randomAlphanumeric(10);
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			log.info("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			Acquirer acquirer = null;
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+ acquirerCode+"'";
				acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			}
			else {
				String hql = "Select tp from Acquirer tp where tp.id = "+ acquirerId;
				acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			}
			
			District locationDistrict = null;
			String hql = "Select tp from District tp where tp.id = " + locationDistrict_id;
			log.info("hql ==" + hql);
			locationDistrict = (District)this.swpService.getUniqueRecordByHQL(hql);
			Date dob = null;
			if(dateOfBirth!=null && dateOfBirth.length()>0)
			{
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				dob = df.parse(dateOfBirth);
			}
	
			String addressLine3 = locationDistrict.getName();
			String addressLine4 = locationDistrict.getProvinceName();
			String addressLine5 = locationDistrict.getCountryName();
			String uniqueType = meansOfIdentificationType;
			String uniqueValue = meansOfIdentificationNumber;
			String sex = gender.toUpperCase().substring(0, 1);
			String accType = "WA";
			String idFront = null;
			String idBack = null;
			String custImg = null;
			String custSig = null;
			Gender gender_ = Gender.valueOf(gender);
			MeansOfIdentificationType moit = MeansOfIdentificationType.valueOf(meansOfIdentificationType);
			CustomerType ct = CustomerType.valueOf(customerType);
			
			Customer customer = null;
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
			jsonObject.put("message", "New Customer Wallet Creation Failed.");
			
			
			Merchant merchant = null;
			Device device = null;
			if(merchantCode!=null && deviceCode!=null)
			{
				hql = "Select tp from Merchant tp where tp.merchantCode = '" + merchantCode + "'";
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			}


			if(device==null || merchant==null)
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide all necessary information to create a wallet and card");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			int isLive = device.getSwitchToLive();
				

			hql = "Select tp from Account tp where tp.customer.contactMobile = '" + contactMobile + "' AND tp.isLive = " + isLive;
			Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			if(customerAccountList!=null && customerAccountList.size()>0)
			{
				
			}
			else
			{
				String verificationNumber = RandomStringUtils.randomNumeric(10);
				CustomerStatus status = CustomerStatus.ACTIVE;
				
				User user = (User)swpService.getRecordById(User.class, userId);
				customer = new Customer(verificationNumber, firstName, lastName, otherName, dob, gender_, addressLine1, addressLine2, 
						locationDistrict, contactMobile, altContactMobile, contactEmail, altContactEmail, status, customerImage, ct, 
						moit, meansOfIdentificationNumber, device, user);
			}
			
			Account account = new Account();
			if(acquirer.getHoldFundsYes()!=null && acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				log.info("Bank Doesnt Hold Funds");
				String identifier1 = "0" + (AccountType.valueOf(accountType).ordinal() + 1) + "" + 
						RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
				account.setAccountIdentifier(identifier1);
				account.setAccountCount(customerAccountList.size());
				account.setCreated_at(new Date());
				account.setCustomer(customer);
				account.setStatus(AccountStatus.ACTIVE);
				account.setAcquirer(acquirer);
				account.setBranchCode(branch_code);
				account.setCurrencyCode(currencyCode);
				account.setAccountType(AccountType.valueOf(accountType));
				account.setIsLive(device.getSwitchToLive());
				
				
				
				account = (Account)this.swpService.createNewRecord(account);
				jsonObject.put("accountNo", identifier1);
				jsonObject.put("customerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
				
				
				/*Create transaction for crediting of customers account*/
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
				transaction.setCreditPoolAccountTrue(true);
				transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
				transaction.setAcquirer(acquirer);
				transaction.setReceipientChannel(Channel.OTC);
				transaction.setTransactionDetail("Wallet Opening: Deposit " + currencyCode + "" + openingAccountAmount + " into Account #" + account.getAccountIdentifier());
				transaction.setDetails("Wallet Opening");
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
				transaction.setIsLive(device.getSwitchToLive());
				
				this.swpService.createNewRecord(transaction);
				jsonObject.put("amountDeposited", openingAccountAmount);
				
				String accountNo = null;
				if(merchantCode!=null && deviceCode!=null)
					accountNo = UtilityHelper.generateAccountNo(AccountType.DEVICE_SETTLEMENT, account);
				else
					accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
				
				account.setAccountIdentifier(accountNo);
				this.swpService.updateRecord(account);
				
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
				jsonObject.put("message", "New Customer Wallet Created Successfully");
				jsonObject.put("accountNumber", accountNo);
				jsonObject.put("customerVerificationNumber", customer.getVerificationNumber());
				
				
				log.info("Create New customer wallet = " + jsonObject.toString());
				
				
				
				String receipentMobileNumber = account.getCustomer().getContactMobile();
				String fname = customer.getFirstName() + " " + customer.getLastName();
				String smsMessage = "";
				if(device.getSwitchToLive().equals(1))
					smsMessage = "Hello "+fname+"\nA new Eagle Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
				else
					smsMessage = "Hello "+fname+"\nA new Test Eagle Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
				
				//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
				//swpService.createNewRecord(smsMsg);


				SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
				new Thread(smsSender).start();
			}
			else
			{
				log.info(contactMobile);
				switch(acquirer.getBank().getBankCode())
				{
					case Application.ZICB_BANK_CODE:
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
							customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
							customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
							sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
							"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MEANS_OF_ID_NUMBER");
						log.info("jsonObject ..." + jsonObject.toString());
						
						
						boolean walletExistsInBank = false;
						JSONArray customerWalletList = null;
						if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
						{
							
							customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
							if(customerWalletList!=null && customerWalletList.length()>0)
							{
								//walletExistsInBank = true;
								jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
									customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
									customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
									sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
									"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
								log.info("jsonObject ..." + jsonObject.toString());
								if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
								{
									
									customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
									if(customerWalletList!=null && customerWalletList.length()>0)
									{
										walletExistsInBank = true;
									}
								}
							}
						}
						else
						{
							jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
								customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
								customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
								sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
								"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
							log.info("jsonObject ..." + jsonObject.toString());
							if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
							{
								
								customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
								if(customerWalletList!=null && customerWalletList.length()>0)
								{
									walletExistsInBank = true;
								}
							}
						}
						
						
						if(walletExistsInBank==true)
						{
							JSONObject customerWallet = customerWalletList.getJSONObject(0);
							
							JSONObject allSettings = app.getAllSettings();
							String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
							CardScheme accountScheme = null;
							if(defaultAccountSchemeIdObj!=null)
							{
								Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
								accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
							}
							
							ProbasePayCurrency currency = ProbasePayCurrency.valueOf(currencyCode);
							/*String accountNo = customerWallet.getString("accountNo");
							String mobileNumberFromBank = customerWallet.getString("mobileNumber");
							String meansOfIdentificationNumberrFromBank = customerWallet.getString("meansOfIdentificationNumber");*/
							String accountNo = customerWallet.getString("accountNo");
							String mobileNumberFromBank = customerWallet.getString("mobileNo");
							//String meansOfIdentificationNumberrFromBank = customerWallet.getString("nationalId");
							String meansOfIdentificationNumberrFromBank = customerWallet.getString("uniqueIdVal");
							String branchCode = null;
							String contactMobileTemp = contactMobile.substring(3);
							log.info("contactMobileTemp..." + contactMobileTemp);
							log.info("contactMobileTemp..." + contactMobileTemp);
							log.info("mobileNumberFromBank..." + mobileNumberFromBank);
							log.info("meansOfIdentificationNumberrFromBank..." + meansOfIdentificationNumberrFromBank);
							log.info("mobileNumberFromBank..." + mobileNumberFromBank);
							log.info("meansOfIdentificationNumberrFromBank..." + meansOfIdentificationNumberrFromBank);
							
							if(!contactMobileTemp.equals(mobileNumberFromBank) || !meansOfIdentificationNumber.equals(meansOfIdentificationNumberrFromBank))
							{
								jsonObject.put("status", ERROR.INVALID_PARAMETERS);
								jsonObject.put("message", "We have created a profile for you. We however can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank");
								
								
								return Response.status(200).entity(jsonObject.toString()).build();
							}
							
							String otp = RandomStringUtils.randomNumeric(4);
							String otpRef = RandomStringUtils.randomNumeric(6);
							
							//User user = customer.getUser();
							//user.setOtp(otp);
							//swpService.updateRecord(user);

							AccountMapRequest accountMapRequest = new AccountMapRequest(customer.getId(), com.probase.probasepay.enumerations.AccountStatus.INACTIVE , otp, otpRef, 
									branchCode, acquirer.getId(), currencyCode, AccountType.valueOf(accountType), null, null,
									currency, 0.00, 0.00, accountScheme.getId(), null, accountNo, 0, isTokenize, device.getSwitchToLive());
							
							accountMapRequest = (AccountMapRequest)swpService.createNewRecord(accountMapRequest);
							
							String receipentMobileNumber = mobileNumberFromBank;
							String smsMessage = "Hello,\n We received a request to link your ZICB wallet/account - "+ accountNo+" to your Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
							if(device.getSwitchToLive().equals(1))
								smsMessage = "Hello,\n We received a request to link your ZICB wallet/account - "+ accountNo+" to your Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
							else
								smsMessage = "Hello,\n We received a request to link your ZICB wallet/account - "+ accountNo+" to your Test Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
								
							SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
							new Thread(smsSender).start();
							jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
							jsonObject.put("message", "Enter the OTP sent to your mobile number to complete this process");
							jsonObject.put("customerVerificationNo", customer.getVerificationNumber());
							
							
							/*String accountNo = customerWallet.getString("accountNo");


							account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currencyCode, AccountType.DEVICE_SETTLEMENT, accountNo, 
									customerAccountList==null ? 0 : customerAccountList.size(), null, null, null, null, currency, 
									null, null, accountScheme);
							account = (Account)swpService.createNewRecord(account);
							
							
							
							
							Transaction transaction = new Transaction();
							transaction.setChannel(Channel.WEB);
							transaction.setCreated_at(new Date());
							transaction.setAmount(0.00);
							transaction.setCustomerId(customer.getId());
							transaction.setFixedCharge(null);
							transaction.setMessageRequest(null);
							transaction.setServiceType(ServiceType.DEPOSIT_OTC);
							transaction.setStatus(TransactionStatus.SUCCESS);
							transaction.setPayerEmail(customer.getContactEmail());
							transaction.setPayerMobile(customer.getContactMobile());
							transaction.setPayerName(lastName + ", " + firstName);
							transaction.setResponseCode(TransactionCode.transactionSuccess);
							transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
							transaction.setTransactionDate(new Date());
							transaction.setTransactionCode(TransactionCode.transactionSuccess);
							transaction.setAccount(account);
							transaction.setCreditAccountTrue(true);
							transaction.setCreditPoolAccountTrue(true);
							transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
							transaction.setReceipientChannel(Channel.WEB);
							transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
							transaction.setReceipientEntityId(account.getId());
							transaction.setDevice(device);
							transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
							if(merchant!=null)
								transaction.setMerchantId(merchant.getId());
							if(device!=null)
								transaction.setDevice(device);
								
							
							transaction = (Transaction)swpService.createNewRecord(transaction);
							jsonObject.put("transactionId", transaction.getId());
							
							hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
							List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
							jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
							jsonObject.put("walletExists", allaccounts!=null && allaccounts.size()>0 ? true : false);
							jsonObject.put("accountNo", accountNo);
							

							hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
									+ " AND (tp.deleted_at IS NULL)";
							List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
							jsonObject.put("ecards", ecards==null ? null : ecards);
							jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
						
							jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
							jsonObject.put("customerNumber", customer.getVerificationNumber());
							jsonObject.put("customerId", customer.getId());
							jsonObject.put("accountId", account.getId());
							jsonObject.put("accountIdentifier", accountNo);
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "New Customer Wallet Created Successfully");
							
							
							
							String receipentMobileNumber = account.getCustomer().getContactMobile();
							String fname = customer.getFirstName() + " " + customer.getLastName();
							String smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
							//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
							//swpService.createNewRecord(smsMsg);

							SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
							new Thread(smsSender).start();*/
							
							
							return Response.status(200).entity(jsonObject.toString()).build();
						}
						else
						{
							jsonObject = UtilityHelper.createZICBWallet(swpService, logId, token, merchantCode, deviceCode, firstName, lastName, addressLine1, addressLine2, 
								addressLine3, addressLine4, addressLine5, uniqueType, uniqueValue, dateOfBirth, contactEmail, sex, contactMobile, accType, currencyCode, idFront, idBack, custImg, 
								custSig, acquirer, parentCustomerId, parentAccountId, customer.getVerificationNumber(), locationDistrict, null, isTokenize);
							log.info("Create New customer = " + jsonObject.toString());
						}
						break;
					default:
						break;
				}
			}
			/*log.info("zicbWalletResp..." + zicbWalletRespJSON.toString());
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
						String accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
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
						log.info("ecard Id ==" + ecard.getId());
						JSONObject pinJSON = new JSONObject();
						pinJSON.put("pin", pin);
						pinJSON.put("cvv", cvv);
						pinJSON.put("pan", ecard.getPan().substring(0,  4) + "****" + ecard.getPan().substring(ecard.getPan().length() - 4));
						SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
						pinJSON.put("expire", sdf_.format(expDate));
						
						jsonObject.put("ecarddetail", pinJSON.toString());
						log.info("jsonObject ==" + jsonObject.toString());
						
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


							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
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
							
							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
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
			}*/
			
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("message", "New customer creation Failed. Value for Date of birth provided invalid. Date should be in format YYYY-MM-DD");
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_DOB_FAILED);
				log.warn(e);
				log.info("Create New customer Failed = " + jsonObject.toString());
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.debug(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.debug(e);
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	@POST
	@Path("/createNewMerchantCustomerAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewMerchantCustomerAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
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
			@FormParam("district") Integer locationDistrict_id, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("customerType") String customerType, 
			@FormParam("parentCustomerId") Long parentCustomerId, 
			@FormParam("parentAccountId") Long parentAccountId, 
			@FormParam("meansOfIdentificationType") String meansOfIdentificationType, 
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber, 
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("acquirerId") String acquirerId,
			@FormParam("userId") Long userId,
			@FormParam("branchCode") String branchCode
			)
	{
		 //acquirerId= "PROBASE";
		String logId = RandomStringUtils.randomAlphanumeric(10);
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
		try {
			int mpin = Integer.valueOf(RandomStringUtils.randomNumeric(4));
			int pin = Integer.valueOf(RandomStringUtils.randomNumeric(4));
			this.swpService = this.serviceLocator.getSwpService();
			
			Application app = Application.getInstance(swpService);
			
			String hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "' AND tp.deleted_at IS NULL";
			Device device = (Device)swpService.getUniqueRecordByHQL(hql);
			
			if(!device.getMerchant().getMerchantCode().equals(merchantId))
			{
				
			}
			Merchant merchant = device.getMerchant();
			hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+ acquirerId+"'";
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			District locationDistrict = null;
			hql = "Select tp from District tp where tp.id = " + locationDistrict_id;
			log.info("hql ==" + hql);
			locationDistrict = (District)this.swpService.getUniqueRecordByHQL(hql);
			Date dob = null;
			if(dateOfBirth!=null && dateOfBirth.length()>0)
			{
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				dob = df.parse(dateOfBirth);
			}
	
			String addressLine3 = locationDistrict.getName();
			String addressLine4 = locationDistrict.getProvinceName();
			String addressLine5 = locationDistrict.getCountryName();
			String uniqueType = meansOfIdentificationType;
			String uniqueValue = meansOfIdentificationNumber;
			String sex = gender.toUpperCase().substring(0, 1);
			String accType = "WA";
			String idFront = null;
			String idBack = null;
			String custImg = null;
			String custSig = null;
			Gender gender_ = Gender.valueOf(gender);
			MeansOfIdentificationType moit = MeansOfIdentificationType.valueOf(meansOfIdentificationType);
			CustomerType ct = CustomerType.valueOf(customerType);
			
			Customer customer = null;
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
			jsonObject.put("message", "New Customer Wallet Creation Failed.");
			
			
			
			
			hql = "Select tp from User tp where tp.username = '"+ contactMobile +"'";
			User userCheck = (User)this.swpService.getUniqueRecordByHQL(hql);
			if(userCheck!=null)
			{
				jsonObject.put("status", ERROR.USER_STATUS_UPDATE_FAIL_MOBILE_NUMBER_TAKEN);
				jsonObject.put("message", "Customer already has a wallet.");
				jsonObject.put("customerVerificationNumber", customer.getVerificationNumber());
			}

			
			String otp = RandomStringUtils.randomNumeric(4);
			String webCode = RandomStringUtils.randomNumeric(32);
			String mobCode = RandomStringUtils.randomNumeric(6);
			User user = new User();
			user.setUsername(contactMobile);
			user.setFailedLoginCount(0);
			user.setLockOut(Boolean.FALSE);
			//user.setMobileActivationCode(mobCode);
			user.setStatus(UserStatus.INACTIVE);
			//user.setWebActivationCode(webCode);
			user.setRoleType(RoleType.CUSTOMER);
			user.setCreated_at(new Date());

			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setOtherName(otherName);
			user.setMobileNo(contactMobile);
			user.setOtp(otp);
			user.setAcquirerId(acquirer.getId());
			user = (User)this.swpService.createNewRecord(user);
			
			
			
			customer = new Customer();
			customer.setContactMobile(contactMobile);
			customer.setCreated_at(new Date());
			customer.setUpdated_at(new Date());
			customer.setCustomerType(CustomerType.INDIVIDUAL);
			customer.setFirstName(firstName);
			customer.setLastName(lastName);
			customer.setOtherName(otherName);
			customer.setVerificationNumber(RandomStringUtils.randomAlphanumeric(16).toUpperCase());
			customer.setUser(user);
			customer.setStatus(CustomerStatus.INACTIVE);
			customer = (Customer)this.swpService.createNewRecord(customer);
			



			hql = "Select tp from Account tp where tp.customer.contactMobile = '" + contactMobile + "'";
			Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			if(customerAccountList!=null && customerAccountList.size()>0)
			{
				Account customerAccount = customerAccountList.iterator().next();
				jsonObject.put("message", "Customer already has a wallet");
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("customerNumber", customerAccount.getCustomer().getVerificationNumber());
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				String verificationNumber = RandomStringUtils.randomNumeric(10);
				CustomerStatus status = CustomerStatus.ACTIVE;
				customer = new Customer(verificationNumber, firstName, lastName, otherName, dob, gender_, addressLine1, addressLine2, 
						locationDistrict, contactMobile, altContactMobile, contactEmail, altContactEmail, status, customerImage, ct, 
						moit, meansOfIdentificationNumber, device, user);
			}
			
			Account account = new Account();
			if(acquirer.getHoldFundsYes()!=null && acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				log.info("Bank Doesnt Hold Funds");
				String identifier1 = "0" + (AccountType.valueOf(accountType).ordinal() + 1) + "" + 
						RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
				account.setAccountIdentifier(identifier1);
				account.setAccountCount(customerAccountList.size());
				account.setCreated_at(new Date());
				account.setCustomer(customer);
				account.setStatus(AccountStatus.ACTIVE);
				account.setAcquirer(acquirer);
				account.setBranchCode(branchCode);
				account.setCurrencyCode(currencyCode);
				account.setAccountType(AccountType.valueOf(accountType));
				account.setRestrictedToUseOnDeviceId(device.getId());
				
				
				
				account = (Account)this.swpService.createNewRecord(account);
				jsonObject.put("accountNo", identifier1);
				jsonObject.put("customerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
				
				
				/*Create transaction for crediting of customers account*/
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
				transaction.setCreditPoolAccountTrue(true);
				transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
				transaction.setAcquirer(acquirer);
				transaction.setReceipientChannel(Channel.OTC);
				transaction.setTransactionDetail("Wallet Opening: Deposit " + currencyCode + "" +  openingAccountAmount + " into Account #" + account.getAccountIdentifier());
				transaction.setDetails("Wallet Opening");
				transaction.setReceipientEntityId(account.getId());

				hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
						"tp.account.id = " + account.getId() + " ORDER BY tp.updated_at DESC";
				Collection<Transaction> lastTransactions = (Collection<Transaction>)this.swpService.getAllRecordsByHQL(hql, 0, 1);
				Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
				transaction.setClosingBalance((lastTransaction!=null ? lastTransaction.getTotalCreditSum() : 0.0) + openingAccountAmount);
				transaction.setTotalCreditSum((lastTransaction!=null ? lastTransaction.getTotalCreditSum() : 0.0) + openingAccountAmount);
				transaction.setTotalCreditSum((lastTransaction!=null ? lastTransaction.getTotalDebitSum() : 0.0) + openingAccountAmount);
				transaction.setUpdated_at(new Date());
				transaction.setPaidInByBankUserAccountId(user.getId());
				
				this.swpService.createNewRecord(transaction);
				jsonObject.put("amountDeposited", openingAccountAmount);
				
				String accountNo = null;
				accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
				
				account.setAccountIdentifier(accountNo);
				this.swpService.updateRecord(account);
				
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
				jsonObject.put("message", "New Customer Wallet Created Successfully");
				jsonObject.put("accountNumber", accountNo);
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				
				
				log.info("Create New customer wallet = " + jsonObject.toString());
			}
			else
			{
				log.info(contactMobile);
				switch(acquirer.getBank().getBankCode())
				{
					case Application.ZICB_BANK_CODE:
						jsonObject = UtilityHelper.createDeviceRestrictedZICBWallet(swpService, logId, user, merchant, device, firstName, lastName, addressLine1, addressLine2, 
								addressLine3, addressLine4, addressLine5, uniqueType, uniqueValue, dateOfBirth, contactEmail, sex, contactMobile, accType, currencyCode, idFront, idBack, custImg, 
								custSig, acquirer, parentCustomerId, parentAccountId, customer.getVerificationNumber(), locationDistrict);
						log.info("Create New customer = " + jsonObject.toString());
						break;
					default:
						break;
				}
			}
			/*log.info("zicbWalletResp..." + zicbWalletRespJSON.toString());
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
						String accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
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
						log.info("ecard Id ==" + ecard.getId());
						JSONObject pinJSON = new JSONObject();
						pinJSON.put("pin", pin);
						pinJSON.put("cvv", cvv);
						pinJSON.put("pan", ecard.getPan().substring(0,  4) + "****" + ecard.getPan().substring(ecard.getPan().length() - 4));
						SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
						pinJSON.put("expire", sdf_.format(expDate));
						
						jsonObject.put("ecarddetail", pinJSON.toString());
						log.info("jsonObject ==" + jsonObject.toString());
						
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


							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
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
							
							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
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
			}*/
			
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("message", "New customer creation Failed. Value for Date of birth provided invalid. Date should be in format YYYY-MM-DD");
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_DOB_FAILED);
				log.warn(e);
				log.info("Create New customer Failed = " + jsonObject.toString());
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.debug(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.debug(e);
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	@POST
	@Path("/createTutukaVirtualCard")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createTutukaVirtualCard(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("encryptedData") String encryptedData, @FormParam("token") String token)
	{
		TutukaServicesV2  ts = new TutukaServicesV2();
		JSONObject jsonObject = ts.createLinkedVirtualCard(httpHeaders, requestContext, encryptedData, token);
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	@POST
	@Path("/updateCustomerProfile")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateCustomerProfile(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("addressLine1") String addressLine1, 
			@FormParam("addressLine2") String addressLine2, 
			@FormParam("altContactEmail") String altContactEmail, 
			@FormParam("altContactMobile") String altContactMobile, 
			@FormParam("contactEmail") String contactEmail, 
			@FormParam("dateOfBirth") String dateOfBirth, 
			@FormParam("customerImage") String customerImage,
			@FormParam("firstName") String firstName, 
			@FormParam("gender") String gender, 
			@FormParam("lastName") String lastName, 
			@FormParam("otherName") String otherName, 
			@FormParam("district") Integer locationDistrict_id,
			@FormParam("token") String token, 
			@FormParam("meansOfIdentificationType") String meansOfIdentificationType, 
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber, 
			@FormParam("customerVerificationNumber") String customerVerificationNumber
			)
	{
		 //acquirerId= "PROBASE";
		String logId = RandomStringUtils.randomAlphanumeric(10);
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			log.info("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			
			District locationDistrict = null;
			String hql = "Select tp from District tp where tp.id = " + locationDistrict_id;
			log.info("hql ==" + hql);
			locationDistrict = (District)this.swpService.getUniqueRecordByHQL(hql);
			Date dob = null;
			if(dateOfBirth!=null && dateOfBirth.length()>0)
			{
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				dob = df.parse(dateOfBirth);
			}
	
			Gender gender_ = Gender.valueOf(gender);
			MeansOfIdentificationType moit = MeansOfIdentificationType.valueOf(meansOfIdentificationType);
			
			Customer customer = null;
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
			jsonObject.put("message", "Customer Profile Update Failed.");
			
			

			hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNumber + "'";
			log.info("Uhql..." + hql);
			customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			if(customer==null)
			{
				jsonObject.put("message", "Customer Profile Not Found.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				log.info("Update Customer...");
				log.info("addressLine1..." + addressLine1);
				log.info("customer id..." + customer.getId());
				
				customer.setAddressLine1(addressLine1);
				customer.setAddressLine2(addressLine2);
				customer.setAltContactEmail(altContactEmail);
				customer.setAltContactMobile(altContactMobile);
				customer.setContactEmail(contactEmail);
				customer.setCustomerImage(customerImage);
				customer.setDateOfBirth(dob);
				customer.setFirstName(firstName);
				customer.setLastName(lastName);
				customer.setOtherName(otherName);
				customer.setGender(gender_);
				customer.setLocationDistrict(locationDistrict);
				customer.setMeansOfIdentificationNumber(meansOfIdentificationNumber);
				customer.setMeansOfIdentificationType(moit);
				this.swpService.updateRecord(customer);
				
				jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_UPDATE_SUCCESSFUL);
				jsonObject.put("message", "Customer Profile Updated Successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			/*log.info("zicbWalletResp..." + zicbWalletRespJSON.toString());
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
						String accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
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
						log.info("ecard Id ==" + ecard.getId());
						JSONObject pinJSON = new JSONObject();
						pinJSON.put("pin", pin);
						pinJSON.put("cvv", cvv);
						pinJSON.put("pan", ecard.getPan().substring(0,  4) + "****" + ecard.getPan().substring(ecard.getPan().length() - 4));
						SimpleDateFormat sdf_ = new SimpleDateFormat("MM/YY");
						pinJSON.put("expire", sdf_.format(expDate));
						
						jsonObject.put("ecarddetail", pinJSON.toString());
						log.info("jsonObject ==" + jsonObject.toString());
						
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


							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
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
							
							jsonObject.put("ecarddetail", pinJSON.toString());
							log.info("jsonObject ==" + jsonObject.toString());
							jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
							jsonObject.put("message", "New Customer Account Added Successfully");
						}
			return Response.status(200).entity(jsonObject.toString()).build();
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
			}*/
			
			
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("message", "New customer creation Failed. Value for Date of birth provided invalid. Date should be in format YYYY-MM-DD");
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_DOB_FAILED);
				log.warn(e);
				log.info("Create New customer Failed = " + jsonObject.toString());
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.debug(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.debug(e);
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	/**Service Method - listCustomers
	 * 
	 * @param status - CustomerStatus
	 * @return Stringified JSONObject of the list of customers
	 */
	
	@GET
	@Path("/listCustomers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCustomers(
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
			log.info(hql);
			
			
			
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			log.info("verifyJ ==" + verifyJ.toString());
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
				//Applicable to bankCodes that are PROBASEPAY
				if(status!=null)
				{
					hql = hql + " where tp.status = "+(CustomerStatus.valueOf(status).ordinal());
				}
				
				//Applicable to bankCodes that are not PROBASEPAY
				/*if(verifyJ.has("issuerBankCode") && !verifyJ.getString("issuerBankCode").equalsIgnoreCase("PROBASE"))
				{
					hql = "Select DISTINCT tp.customer from Account tp where " +
							"tp.bank.bankCode = '" + verifyJ.getString("issuerBankCode") + "'"; 
					if(status!=null)
					{
						hql = hql + " AND tp.customer.status = "+(CustomerStatus.valueOf(status).ordinal());
					}
					
				}*/
				log.info(hql);
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
			log.info(customerArray.toString());	
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer list fetched successfully");
			jsonObject.put("customerlist", customerArray);
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}


	@GET
	@Path("/getCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") Long customerId, 
			@QueryParam("token") String token)
	{
		List<Map<String, Object>> customers = null;
		Map<String, Object> customer = null;
		
		JSONObject jsonObject = new JSONObject();
		try {
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
			log.info("verifyJ ==" + verifyJ.toString());
			
			
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			
			Long customerIdI = customerId;
			
			String hql = "Select tp.*, p.provinceName, d.name from customers tp, districts d, provinces p where "
					+ "tp.locationDistrict_id = d.id AND d.provinceId = p.id AND tp.id = " + customerIdI;
			log.info(hql);
			customers = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			customer = customers.get(0);
			
			JSONObject oneCustomer = new JSONObject();
			log.info("oneCustomer ..." + customer.get("id"));
			oneCustomer.put("id", (BigInteger)customer.get("id"));
			oneCustomer.put("firstName", UtilityHelper.getValue((String)customer.get("firstName")));
			oneCustomer.put("lastName", UtilityHelper.getValue((String)customer.get("lastName")));
			oneCustomer.put("otherName", UtilityHelper.getValue((String)customer.get("otherName")));
			oneCustomer.put("gender", ((Integer)customer.get("gender")==null ? null : Gender.values()[(Integer)customer.get("gender")]));
			oneCustomer.put("addressLine1", UtilityHelper.getValue((String)customer.get("addressLine1")));
			oneCustomer.put("addressLine2", UtilityHelper.getValue((String)customer.get("addressLine2")));
			oneCustomer.put("locationDistrictId", customer.get("locationDistrictId")==null ? null : (BigInteger)customer.get("locationDistrictId"));
			oneCustomer.put("locationDistrict", customer.get("name")==null ? "" : (String)customer.get("name"));
			oneCustomer.put("locationProvince", customer.get("provinceName")==null ? "" : (String)customer.get("provinceName"));
			oneCustomer.put("locationProvinceId", customer.get("locationDistrict")==null ? null : (BigInteger)customer.get("provinceId"));
			oneCustomer.put("dateOfBirth", UtilityHelper.getDateOfBirth((Date)customer.get("dateOfBirth")));
			oneCustomer.put("contactMobile", UtilityHelper.getValue((String)customer.get("contactMobile")));
			oneCustomer.put("altContactMobile", UtilityHelper.getValue((String)customer.get("altContactMobile")));
			oneCustomer.put("contactEmail", UtilityHelper.getValue((String)customer.get("contactEmail")));
			oneCustomer.put("altContactEmail", UtilityHelper.getValue((String)customer.get("altContactEmail")));
			oneCustomer.put("verificationNumber", UtilityHelper.getValue((String)customer.get("verificationNumber")));
			oneCustomer.put("status", (CustomerStatus.values()[(Integer)customer.get("status")].name()));
			oneCustomer.put("customerType", (customer.get("customerType")==null ? null : CustomerType.values()[(Integer)customer.get("customerType")].name()));
			oneCustomer.put("customerImage", UtilityHelper.getValue((String)customer.get("customerImage")));
			
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Customer record found");
			jsonObject.put("customer", oneCustomer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
		
		
	}

	
	
	
	
	@POST
	@Path("/addAccountToCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAccountToCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerVerificationNo") String customerVerificationNo, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("merchantCode") String merchantCode,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("acquirerId") Long acquirerId,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("isSettlementAccount") Integer isSettlementAccount,
			@FormParam("isTokenize") Integer isTokenize,
			@FormParam("token") String token)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		String logId = RandomStringUtils.randomAlphanumeric(10);
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
		try {
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
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			log.info("verifyJ ==" + verifyJ.toString());
			String branch_code = null;
			String acquirerCode = null;
			
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				branch_code = verifyJ.getString("branchCode");
				log.info("branch_code ==" + branch_code);
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("acquirerCode ==" + acquirerCode);
			}
			else if(tokenUser.getRoleType().equals(RoleType.CUSTOMER))
			{
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("bankCode ==" + acquirerCode);
			}
			
			String hql = "Select tp from Acquirer tp where tp.id = " + acquirerId;
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNo + "'";
			customer  = (Customer)this.swpService.getUniqueRecordByHQL(hql);

			Merchant merchant = null;
			Device device = null;
			if(merchantCode!=null && deviceCode!=null)
			{
				hql = "Select tp from Merchant tp where tp.merchantCode = '" + merchantCode + "'";
				merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
				device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			}
			
			if(device==null || merchant==null)
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide all necessary information to create a wallet and card");
				return Response.status(200).entity(jsonObject.toString()).build();
			}

			hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			Collection<Account> customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
			
			
			
			Account account = new Account();
			if(acquirer.getHoldFundsYes()!=null && acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				log.info("Bank Doesnt Hold Funds");
				String identifier1 = "0" + (AccountType.valueOf(accountType).ordinal() + 1) + "" + 
						RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
				account.setAccountIdentifier(identifier1);
				account.setAccountCount(customerAccountList.size());
				account.setCreated_at(new Date());
				account.setCustomer(customer);
				account.setStatus(AccountStatus.ACTIVE);
				account.setAcquirer(acquirer);
				account.setBranchCode(branch_code);
				account.setCurrencyCode(currencyCode);
				account.setAccountType(AccountType.valueOf(accountType));
				
				
				
				account = (Account)this.swpService.createNewRecord(account);
				
				
				jsonObject.put("accountId", account.getId());
				jsonObject.put("accountIdentifier", identifier1);
				jsonObject.put("accountNo", identifier1);
				jsonObject.put("accountId", account.getId());
				
				
				/*Create transaction for crediting of customers account*/
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
				transaction.setCreditPoolAccountTrue(true);
				transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
				transaction.setAcquirer(acquirer);
				transaction.setReceipientChannel(Channel.OTC);
				transaction.setTransactionDetail("Wallet Opening: Deposit " + currencyCode + "" + openingAccountAmount + " into Account #" + account.getAccountIdentifier());
				transaction.setDetails("Wallet Opening");
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
				transaction.setIsLive(device.getSwitchToLive());
				
				this.swpService.createNewRecord(transaction);
				jsonObject.put("transactionId", transaction.getId());
				jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				jsonObject.put("customerId", customer.getId());
				jsonObject.put("amountDeposited", openingAccountAmount);
				
				String accountNo = null;
				if(merchantCode!=null && deviceCode!=null)
					accountNo = UtilityHelper.generateAccountNo(AccountType.DEVICE_SETTLEMENT, account);
				else
					accountNo = UtilityHelper.generateAccountNo(AccountType.VIRTUAL, account);
				
				account.setAccountIdentifier(accountNo);
				this.swpService.updateRecord(account);
				
				jsonObject.put("accountIdentifier", accountNo);
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
				jsonObject.put("message", "New Customer Wallet Created Successfully");
				
				
				log.info("Create New customer wallet = " + jsonObject.toString());
				
				
				String receipentMobileNumber = account.getCustomer().getContactMobile();
				String fname = customer.getFirstName() + " " + customer.getLastName();
				String smsMessage = "Hello "+fname+"\nA new Eagle Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
				//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
				//swpService.createNewRecord(smsMsg);

				SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
				new Thread(smsSender).start();
			}
			else
			{
				log.info("Bank Holds Funds");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				log.info("getFirstName ..." + customer.getFirstName());
				log.info("getLastName ..." + customer.getLastName());
				log.info("getAddressLine1..." + customer.getAddressLine1());
				log.info("getAddressLine2..." + customer.getAddressLine2());
				log.info("getLocationDistrict..." + customer.getLocationDistrict());
				log.info("getMeansOfIdentificationType..." + customer.getMeansOfIdentificationType().name());
				log.info("getMeansOfIdentificationNumber..." + customer.getMeansOfIdentificationNumber());
				log.info("getContactEmail..." + customer.getContactEmail());
				log.info("getDateOfBirth..." + customer.getDateOfBirth());
				log.info("getGender..." + customer.getGender().name());
				log.info("getContactMobile..." + customer.getContactMobile());
						
						
				switch (acquirer.getBank().getBankCode())
				{
					case Application.ZICB_BANK_CODE:
						if(customer.getFirstName()!=null && customer.getFirstName().length()>0 && 
								customer.getLastName()!=null && customer.getLastName().length()>0 && 
								customer.getAddressLine1()!=null && customer.getAddressLine1().length()>0 && 
								customer.getAddressLine2()!=null && customer.getAddressLine2().length()>0 && 
								customer.getLocationDistrict()!=null && customer.getMeansOfIdentificationType()!=null && 
								customer.getMeansOfIdentificationNumber()!=null && customer.getDateOfBirth()!=null && 
								customer.getContactEmail()!=null && customer.getGender()!=null && customer.getContactMobile()!=null)
						{
							log.info("Customer Profile Data provided");
							/*jsonObject = UtilityHelper.verifyZICBCustomerByMobileNumber(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
								customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
								customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
								sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
								"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MEANS_OF_ID_NUMBER");
							log.info("jsonObject ..." + jsonObject.toString());
							
							
							if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
							{
								return Response.status(200).entity(jsonObject.toString()).build();
							}
							else
							{
								jsonObject = UtilityHelper.verifyZICBCustomerByMobileNumber(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
									customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
									customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
									sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
									"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
								log.info("jsonObject ..." + jsonObject.toString());
								if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
								{
									return Response.status(200).entity(jsonObject.toString()).build();
								}
							}
							
							jsonObject = UtilityHelper.createZICBWallet(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
								customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
								customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
								sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
								"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), null);
							
							
							*/
							
							
							
							
							
							
							
							
							
							
							jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
									customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
									customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
									sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
									"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MEANS_OF_ID_NUMBER");
								log.info("jsonObject ..." + jsonObject.toString());
								
								
								boolean walletExistsInBank = false;
								JSONArray customerWalletList = null;
								if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
								{
									
									customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
									if(customerWalletList!=null && customerWalletList.length()>0)
									{
										walletExistsInBank = true;
									}
								}
								else
								{
									jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
										customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
										customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
										sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
										"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
									log.info("jsonObject ..." + jsonObject.toString());
									if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
									{
										
										customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
										if(customerWalletList!=null && customerWalletList.length()>0)
										{
											walletExistsInBank = true;
										}
									}
								}
								
								
								if(walletExistsInBank==true)
								{
									JSONObject customerWallet = customerWalletList.getJSONObject(0);
									
									JSONObject allSettings = app.getAllSettings();
									String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
									CardScheme accountScheme = null;
									if(defaultAccountSchemeIdObj!=null)
									{
										Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
										accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
									}
									
									ProbasePayCurrency currency = ProbasePayCurrency.valueOf(currencyCode);
									/*String accountNo = customerWallet.getString("accountNo");
									String mobileNumberFromBank = customerWallet.getString("mobileNumber");
									String meansOfIdentificationNumberrFromBank = customerWallet.getString("meansOfIdentificationNumber");*/
									String accountNo = customerWallet.getString("accountNo");
									String mobileNumberFromBank = customerWallet.getString("mobileNo");
									//String meansOfIdentificationNumberrFromBank = customerWallet.getString("nationalId");
									String meansOfIdentificationNumberrFromBank = customerWallet.getString("uniqueIdVal");
									String branchCode = null;
									
									
									if(!customer.getContactMobile().equals(mobileNumberFromBank) || !customer.getMeansOfIdentificationNumber().equals(meansOfIdentificationNumberrFromBank))
									{
										jsonObject.put("status", ERROR.INVALID_PARAMETERS);
										jsonObject.put("message", "We can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank");
										
										
										return Response.status(200).entity(jsonObject.toString()).build();
									}
									
									String otp = RandomStringUtils.randomNumeric(4);
									String otpRef = RandomStringUtils.randomNumeric(6);
									
									User user = customer.getUser();
									user.setOtp(otp);
									swpService.updateRecord(user);

									AccountMapRequest accountMapRequest = new AccountMapRequest(customer.getId(), com.probase.probasepay.enumerations.AccountStatus.INACTIVE , otp, otpRef, 
											branchCode, acquirer.getId(), currencyCode, AccountType.valueOf(accountType), null, null,
											currency, 0.00, 0.00, accountScheme.getId(), null, accountNo, 0, isTokenize, device.getSwitchToLive());
									
									accountMapRequest = (AccountMapRequest)swpService.createNewRecord(accountMapRequest);
									
									String receipentMobileNumber = mobileNumberFromBank;
									String smsMessage = "";
									if(device.getSwitchToLive().equals(1))
										smsMessage = "Hello,\n We received a request to link your ZICB wallet/account - "+ accountNo+" to your Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
									else
										smsMessage = "Hello,\n We received a request to link your ZICB wallet/account - "+ accountNo+" to your Test Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
									
										
									SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
									new Thread(smsSender).start();

									jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
									jsonObject.put("message", "Enter the OTP sent to your mobile number to complete this process");


									/*account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currencyCode, AccountType.DEVICE_SETTLEMENT, accountNo, 
											customerAccountList==null ? 0 : customerAccountList.size(), null, null, null, null, currency, 
											null, null, accountScheme);
									account = (Account)swpService.createNewRecord(account);
									
									
									
									
									Transaction transaction = new Transaction();
									transaction.setChannel(Channel.WEB);
									transaction.setCreated_at(new Date());
									transaction.setAmount(0.00);
									transaction.setCustomerId(customer.getId());
									transaction.setFixedCharge(null);
									transaction.setMessageRequest(null);
									transaction.setServiceType(ServiceType.DEPOSIT_OTC);
									transaction.setStatus(TransactionStatus.SUCCESS);
									transaction.setPayerEmail(customer.getContactEmail());
									transaction.setPayerMobile(customer.getContactMobile());
									transaction.setPayerName(customer.getLastName() + ", " +  customer.getFirstName());
									transaction.setResponseCode(TransactionCode.transactionSuccess);
									transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
									transaction.setTransactionDate(new Date());
									transaction.setTransactionCode(TransactionCode.transactionSuccess);
									transaction.setAccount(account);
									transaction.setCreditAccountTrue(true);
									transaction.setCreditPoolAccountTrue(true);
									transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
									transaction.setReceipientChannel(Channel.WEB);
									transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
									transaction.setReceipientEntityId(account.getId());
									transaction.setDevice(device);
									transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
									if(merchant!=null)
										transaction.setMerchantId(merchant.getId());
									if(device!=null)
										transaction.setDevice(device);
										
									
									transaction = (Transaction)swpService.createNewRecord(transaction);
									jsonObject.put("transactionId", transaction.getId());
									
									hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
									List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
									jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
									jsonObject.put("walletExists", allaccounts!=null && allaccounts.size()>0 ? true : false);
									jsonObject.put("accountNo", accountNo);
									

									hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
											+ " AND (tp.deleted_at IS NULL)";
									List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
									jsonObject.put("ecards", ecards==null ? null : ecards);
									jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
								
									jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
									jsonObject.put("customerNumber", customer.getVerificationNumber());
									jsonObject.put("customerId", customer.getId());
									jsonObject.put("accountId", account.getId());
									jsonObject.put("accountIdentifier", accountNo);
									jsonObject.put("status", ERROR.GENERAL_OK);
									jsonObject.put("message", "New Customer Wallet Created Successfully");
									
									
									
									String receipentMobileNumber = account.getCustomer().getContactMobile();
									String fname = customer.getFirstName() + " " + customer.getLastName();
									String smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
									//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
									//swpService.createNewRecord(smsMsg);

									SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
									new Thread(smsSender).start();*/
									
									return Response.status(200).entity(jsonObject.toString()).build();
								}
								else
								{
									jsonObject = UtilityHelper.createZICBWallet(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
										customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
										customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
										sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
										"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), null, isTokenize);
										
									log.info("Create New customer = " + jsonObject.toString());
								}
						}
						else
						{
							log.info("Incomplete customer profile data");
							jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
							if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
								jsonObject.put("message", "New Customer Wallet Creation Failed. Please ensure customers profile is fully updated");
							else
								jsonObject.put("message", "New Customer Wallet Creation Failed. Please ensure your profile is fully updated");
						}
						break;
					default:
						break;
				}
				
			}
			

			log.info(jsonObject.toString());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				e.printStackTrace();
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	@POST
	@Path("/confirmOTPAndCreateWallet")
	@Produces(MediaType.APPLICATION_JSON)
	public Response confirmOTPAndCreateWallet(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerVerificationNo") String customerVerificationNo, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("otp") String otp)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		String logId = RandomStringUtils.randomAlphanumeric(10);
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
		try {
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			JSONObject allSettings = app.getAllSettings();
			
			String sql = "Select tp from Customer tp where tp.verificationNumber = '"+ customerVerificationNo +"'";
			customer = (Customer)this.swpService.getUniqueRecordByHQL(sql);
			
			sql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(sql);
			int isLive = device.getSwitchToLive();
			
			sql = "Select tp from AccountMapRequest tp where tp.customerId = " + customer.getId() + " " + 
				" AND tp.status = " + AccountStatus.INACTIVE.ordinal() + " AND tp.isLive = "+isLive+"  ORDER BY tp.id DESC";
			log.info(sql);
			log.info(sql);
			Collection<AccountMapRequest> accountMapRequests = (Collection<AccountMapRequest>)this.swpService.getAllRecordsByHQL(sql);
			
			AccountMapRequest accountMapRequest = null;
			if(accountMapRequests==null)
			{

				
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid request. We could not find any request linked to that OTP");
				
				log.info(jsonObject.toString());
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				accountMapRequest = accountMapRequests.iterator().next();
				if(!accountMapRequest.getOtp().equals(otp))
				{
					
					String notp = RandomStringUtils.randomNumeric(4);
					String notpRef = RandomStringUtils.randomNumeric(6);
					//accountMapRequest.setOtp(notp);
					accountMapRequest.setFailCount(accountMapRequest.getFailCount() + 1);
					if(accountMapRequest.getFailCount()>2)
					{
						accountMapRequest.setStatus(AccountStatus.DISABLED);
						//String receipentMobileNumber = customer.getContactMobile();
						//String accountNo = accountMapRequest.getAccountNo();
						//String smsMessage = "Hello,\nYour previous OTP has expired. We have created a new OTP to validate your Bevura wallet - "+ accountNo +" . OTP:  "+otp+"\nIf you did not originate this request please call us on 260977000000";
						//SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
						//new Thread(smsSender).start();
						
						
						jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
						jsonObject.put("message", "This request has been canceled. You can no longer use the OTP anymore.");
					}
					else
					{
						
					}
					swpService.updateRecord(accountMapRequest);
					
					//User user = customer.getUser();
					//user.setOtp(notp);
					//swpService.updateRecord(user);
					
	
					jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
					jsonObject.put("message", "Enter the OTP sent to your mobile number to complete this process");
					
					log.info(jsonObject.toString());
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					accountMapRequest.setOtp(null);
					accountMapRequest.setStatus(AccountStatus.ACTIVE);
					swpService.updateRecord(accountMapRequest);
					
					
					if(customer.getStatus().equals(CustomerStatus.INACTIVE))
					{
						customer.setStatus(CustomerStatus.ACTIVE);
						swpService.updateRecord(customer);
					}

					User user = customer.getUser();
					if(user.getStatus().equals(UserStatus.INACTIVE))
					{
						user.setStatus(UserStatus.ACTIVE);
						swpService.updateRecord(customer);
					}
					
					//User user = customer.getUser();
					//if(user.getOtp()!=null && user.getOtp().equals(otp))
					//{
					//	user.setOtp(null);
					//	swpService.updateRecord(user);
					//}
				}
			}
			
			sql = "Select tp from Acquirer tp where tp.id = " + accountMapRequest.getAcquirerId();
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(sql);
			
			sql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.isLive = " + isLive;
			Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(sql);
			
			sql = "Select tp from CardScheme tp where tp.id = " + accountMapRequest.getAccountSchemeId();
			CardScheme accountScheme = (CardScheme)this.swpService.getUniqueRecordByHQL(sql);

			
			
			Account account = new Account(customer, AccountStatus.ACTIVE, null, accountMapRequest.getBranchCode(), acquirer, accountMapRequest.getCurrencyCode(), 
					accountMapRequest.getAccountType(), accountMapRequest.getAccountNo(), customerAccountList!=null ? customerAccountList.size() : 0, null, null, null, null, accountMapRequest.getProbasePayCurrency(), 
					accountMapRequest.getFloatingBalance(), accountMapRequest.getAccountBalance(), accountScheme, isLive);
			account = (Account)swpService.createNewRecord(account);
			
					
					
			/*Transaction transaction = new Transaction();
			transaction.setChannel(Channel.WEB);
			transaction.setCreated_at(new Date());
			transaction.setAmount(0.00);
			transaction.setCustomerId(customer.getId());
			transaction.setFixedCharge(null);
			transaction.setMessageRequest(null);
			transaction.setServiceType(ServiceType.DEPOSIT_OTC);
			transaction.setStatus(TransactionStatus.SUCCESS);
			transaction.setPayerEmail(customer.getContactEmail());
			transaction.setPayerMobile(customer.getContactMobile());
			transaction.setPayerName(customer.getLastName() + ", " +  customer.getFirstName());
			transaction.setResponseCode(TransactionCode.transactionSuccess);
			transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
			transaction.setTransactionDate(new Date());
			transaction.setTransactionCode(TransactionCode.transactionSuccess);
			transaction.setAccount(account);
			transaction.setCreditAccountTrue(true);
			transaction.setCreditPoolAccountTrue(true);
			transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
			transaction.setReceipientChannel(Channel.WEB);
			transaction.setTransactionDetail("Wallet Opening: Deposit ZMW" + 0.00 + " into Account #" + account.getAccountIdentifier());
			transaction.setDetails("Wallet Opening");
			transaction.setReceipientEntityId(account.getId());
			transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
			if(device!=null)
				transaction.setMerchantId(device.getMerchant().getId());
			if(device!=null)
				transaction.setDevice(device);
				
			
			transaction = (Transaction)swpService.createNewRecord(transaction);
			jsonObject.put("transactionId", transaction.getId());*/
			

			String accountNo = accountMapRequest.getAccountNo();
			String receipentMobileNumber = customer.getContactMobile();
			String fname = account.getCustomer().getFirstName();
			
			sql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
			List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(sql);
			jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
			jsonObject.put("walletExists", allaccounts!=null && allaccounts.size()>0 ? true : false);
			jsonObject.put("accountNo", accountNo);
			
			sql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
					+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
			List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(sql);
			jsonObject.put("ecards", ecards==null ? null : ecards);
			jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
		
			jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
			jsonObject.put("customerNumber", customer.getVerificationNumber());
			jsonObject.put("customerId", customer.getId());
			jsonObject.put("accountId", account.getId());
			jsonObject.put("accountIdentifier", accountNo);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("responseUrl", device.getSuccessUrl());
			jsonObject.put("message", "New Customer Wallet Created Successfully");
			/**/
			

			String smsMessage = null;
			if(isLive==1)
				smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
			else
				smsMessage = "Hello "+fname+"\nA new Test Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
			
			//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
			//swpService.createNewRecord(smsMsg);

			SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
			new Thread(smsSender).start();
			
			
			log.info(jsonObject.toString());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				e.printStackTrace();
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/addAccountAndDefaultCardToCustomer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAccountAndDefaultCardToCustomer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("customerVerificationNo") String customerVerificationNo, 
			@FormParam("currencyCode") String currencyCode, 
			@FormParam("accountType") String accountType,
			@FormParam("merchantCode") String merchantCode,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("openingAccountAmount") Double openingAccountAmount, 
			@FormParam("addressLine1") String addressLine1,
			@FormParam("addressLine2") String addressLine2,
			@FormParam("districtId") Long districtId,
			@FormParam("meansOfIdentificationType") String meansOfIdentificationType,
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber,
			@FormParam("contactEmail") String contactEmail,
			@FormParam("gender") String gender,
			@FormParam("dateOfBirth") String dateOfBirth,
			@FormParam("isSettlementAccount") Integer isSettlementAccount,
			@FormParam("isTokenize") Integer isTokenize,
			@FormParam("token") String token)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		Merchant tokenizeMerchant = null;
		Device tokenizeDevice = null;
		String logId = RandomStringUtils.randomAlphanumeric(10);
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();

		if(isTokenize!=null)
			log.info("isTokenize..." + isTokenize);
		
		
		log.info("Create New Customer");
		try {
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
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			log.info("verifyJ ==" + verifyJ.toString());
			String branch_code = null;
			String acquirerCode = null;
			JSONObject allSettings = app.getAllSettings();
			
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				branch_code = verifyJ.getString("branchCode");
				log.info("branch_code ==" + branch_code);
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("acquirerCode ==" + acquirerCode);
			}
			else if(tokenUser.getRoleType().equals(RoleType.CUSTOMER))
			{
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("bankCode ==" + acquirerCode);
				branch_code = allSettings.getString("defaultnonfundsholdingbranchcode");
			}
			
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '" + acquirerCode + "'";
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			

			hql = "Select tp from CardScheme tp where isDefault = 1 and tp.deleted_at IS NULL";
			CardScheme cardScheme = (CardScheme)this.swpService.getUniqueRecordByHQL(hql);
			if(cardScheme==null)
			{
				jsonObject.put("status", ERROR.CARD_SCHEME_FETCH_FAIL);
				jsonObject.put("message", "Invalid type of card provided");
			}
			Issuer issuer = cardScheme.getIssuer();
			
			Collection<Account> customerAccountList = new ArrayList<Account>();
			hql = "Select tp from Customer tp where tp.verificationNumber = '" + customerVerificationNo + "'";
			log.info("hql ==" + hql);
			customer  = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			
			if(customer!=null)
			{
				log.info("customer ==" + customer.getId());
				hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
				customerAccountList = (Collection<Account>)this.swpService.getAllRecordsByHQL(hql);
				
			}
			else
			{
				log.info("customer111 ==");
				jsonObject.put("status", ERROR.CUSTOMER_NOT_FOUND);
				jsonObject.put("message", "Invalid customer details provided. Customer verification number could not be linked to any existing customer");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			Device device = null;
			Merchant merchant = null;
			int isLive = 0;
			if(merchantCode!=null && deviceCode!=null)
			{
				hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
				
	
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				device = (Device)swpService.getUniqueRecordByHQL(hql);

				isLive = device.getSwitchToLive();
				
				tokenizeMerchant = merchant;
				tokenizeDevice = device;
			}
			if(device==null || merchant==null)
			{

				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide all necessary information to create a wallet and card");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Account account = new Account();
			if(acquirer.getHoldFundsYes()!=null && acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				log.info("Bank Doesnt Hold Funds");
				String identifier1 = "0" + branch_code + "" + (AccountType.valueOf(accountType).ordinal() + 1) + "" + 
						RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 9 : 10) + "" + customerAccountList.size();
				
				
				String currency = allSettings.getString("currencycode");
				
				
				
				Double openingBalance = Double.valueOf(allSettings.getString("defaultnonholdingfundsopeningbalance"));
				
				String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
				CardScheme accountScheme = null;
				if(defaultAccountSchemeIdObj!=null)
				{
					Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
					accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
				}
				
				account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.valueOf(accountType), identifier1, 
						customerAccountList==null ? 0 : customerAccountList.size(), null, null, null, null, ProbasePayCurrency.valueOf(currency), 
								openingBalance, openingBalance, accountScheme, isLive);
				account = (Account)this.swpService.createNewRecord(account);
				
				
				jsonObject.put("accountId", account.getId());
				jsonObject.put("accountIdentifier", identifier1);
				jsonObject.put("accountNo", identifier1);
				jsonObject.put("accountId", account.getId());
				
				
				
				
				Date dob = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateOfBirth);
				District district = (District)swpService.getRecordById(District.class, districtId);
				customer.setAddressLine1(addressLine1);
				customer.setAddressLine2(addressLine2);
				customer.setLocationDistrict((District)this.swpService.getRecordById(District.class, districtId));
				customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(meansOfIdentificationType));
				customer.setMeansOfIdentificationNumber(meansOfIdentificationNumber);
				customer.setContactEmail(contactEmail);
				customer.setDateOfBirth(dob);
				customer.setLocationDistrict(district);
				customer.setGender(Gender.valueOf(gender));
				this.swpService.updateRecord(customer);
				log.info("getFirstName ..." + customer.getFirstName());
				log.info("getLastName ..." + customer.getLastName());
				log.info("getAddressLine1..." + customer.getAddressLine1());
				log.info("getAddressLine2..." + customer.getAddressLine2());
				log.info("getLocationDistrict..." + customer.getLocationDistrict());
				log.info("getMeansOfIdentificationType..." + customer.getMeansOfIdentificationType().name());
				log.info("getMeansOfIdentificationNumber..." + customer.getMeansOfIdentificationNumber());
				log.info("getContactEmail..." + customer.getContactEmail());
				log.info("getDateOfBirth..." + customer.getDateOfBirth());
				log.info("getGender..." + customer.getGender().name());
				log.info("getContactMobile..." + customer.getContactMobile());
				
				
				Long accountId = jsonObject.getLong("accountId");
				Account acct = (Account)this.swpService.getRecordById(Account.class, accountId);
				JSONObject cardJS = new JSONObject();
				cardJS.put("customerVerificationNo", customer.getVerificationNumber());
				cardJS.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
				cardJS.put("acquirerId", acct.getAcquirer().getId());
				cardJS.put("cardSchemeId", cardScheme.getId());
				cardJS.put("currencyCodeId", acct.getCurrencyCode());
				if(isTokenize!=null && isTokenize==1)
				{
					cardJS.put("isTokenize", isTokenize);
					cardJS.put("tokenizeMerchantCode", tokenizeMerchant.getMerchantCode());
					cardJS.put("tokenizeDeviceCode", tokenizeDevice.getDeviceCode());
					
				}
				//cardJS.put("corporateCustomerId", JSONObject.NULL);
				//cardJS.put("corporateCustomerAccountId", JSONObject.NULL);

				jsonObject.put("message", "New customer wallet creation was successful");
				jsonObject.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
				jsonObject.put("customerVerificationNo", customer.getVerificationNumber());
				jsonObject.put("customername", customer.getFirstName() + " " + customer.getLastName());
				jsonObject.put("mobileContact", customer.getContactMobile());
				jsonObject.put("status", ERROR.GENERAL_OK);
				
				hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
				List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				jsonObject.put("accounts", accounts==null ? null : accounts);
				jsonObject.put("walletExists", accounts!=null && accounts.size()>0 ? true : false);
				

				String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
				String encryptedData =  UtilityHelper.encryptData(cardJS.toString(), bankKey);
				Response newCard = this.createTutukaVirtualCard(
						httpHeaders,
						requestContext,
						encryptedData, 
						token);
				String cardRespStr = (String)(newCard.getEntity());
				String pn = null;
				String ein = null;
				String ex = null;
				String cv = null;
				if(cardRespStr!=null)
				{
					JSONObject cardResp = new JSONObject(cardRespStr);
					Integer cardRespStatus = cardResp.has("status") ? cardResp.getInt("status") : null;
					if(cardRespStatus!=null && cardRespStatus==(ERROR.GENERAL_OK))
					{
						Long cardCreatedId = cardResp.has("cardId") ? cardResp.getLong("cardId") : null;
						String cardCreatedSerialNo = cardResp.has("serialNo") ? cardResp.getString("serialNo") : null;
						String cardCreatedType = cardResp.has("cardType") ? cardResp.getString("cardType") : null;
						String cardCreatedNameOnCard = cardResp.has("nameOnCard") ? cardResp.getString("nameOnCard") : null;
						if(cardCreatedId!=null)
						{
							jsonObject.put("cardSerialNo", cardCreatedSerialNo);
							jsonObject.put("cardType", cardCreatedType);
							jsonObject.put("nameOnCard", cardCreatedNameOnCard);
							if(cardCreatedType.equals(CardType.TUTUKA_VIRTUAL_CARD.name()))
							{
								pn = (cardResp.has("pan") ? cardResp.getString("pan") : null);
								ein = (cardResp.has("pin") ? cardResp.getString("pin") : null);
								ex = (cardResp.has("expiryDate") ? cardResp.getString("expiryDate") : null);
								cv = (cardResp.has("cvv") ? cardResp.getString("cvv") : null);
								
								if(pn!=null)
									jsonObject.put("cardPan", (pn.substring(pn.length()-4)));
								
								if(ein!=null)
									jsonObject.put("ecardpin", UtilityHelper.encryptData(ein, bankKey));
								
								if(ex!=null)
								{
									SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd"); 
									Date expd = sdf1.parse(ex);
									sdf1 = new SimpleDateFormat("MM/yy");
									String expd1 = sdf1.format(expd);
									jsonObject.put("ecardexpire", UtilityHelper.encryptData(expd1, bankKey));
								}
								
								if(cv!=null)
									jsonObject.put("ecardcvv", UtilityHelper.encryptData(cv, bankKey));
							}
							
							//hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId();
							//List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
							//jsonObject.put("ecards", ecards==null ? null : ecards);
						}
					}
				}
				
				
				/*Create transaction for crediting of customers account*/
				
				
				
				Transaction transaction = new Transaction();
				transaction.setChannel(Channel.MOBILE);
				transaction.setCreated_at(new Date());
				transaction.setAmount(openingBalance);
				transaction.setCustomerId(customer.getId());
				transaction.setFixedCharge(null);
				transaction.setMessageRequest(null);
				transaction.setServiceType(ServiceType.DEPOSIT_OTC);
				transaction.setStatus(TransactionStatus.SUCCESS);
				transaction.setPayerEmail(customer.getContactEmail());
				transaction.setPayerMobile(customer.getContactMobile());
				transaction.setPayerName(customer.getFirstName() + " " + customer.getLastName());
				transaction.setResponseCode(TransactionCode.transactionSuccess);
				transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
				transaction.setTransactionDate(new Date());
				transaction.setTransactionCode(TransactionCode.transactionSuccess);
				transaction.setAccount(account);
				transaction.setCreditAccountTrue(true);
				transaction.setCreditPoolAccountTrue(true);
				transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
				transaction.setAcquirer(acquirer);
				transaction.setReceipientChannel(Channel.MOBILE);
				transaction.setTransactionDetail("Wallet Opening: Deposit " + currencyCode + "" + openingBalance + " into Account #" + account.getAccountIdentifier());
				transaction.setDetails("Wallet Opening");
				transaction.setReceipientEntityId(account.getId());
				transaction.setDevice(device);
				transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
				transaction.setPaidInByBankUserAccountId(tokenUser.getId());
				transaction.setUpdated_at(new Date());
				transaction.setIsLive(device.getSwitchToLive());
				if(merchant!=null)
					transaction.setMerchantId(merchant.getId());
				
				transaction = (Transaction)swpService.createNewRecord(transaction);
				jsonObject.put("transactionId", transaction.getId());
				
				
				jsonObject.put("transactionId", transaction.getId());
				jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				jsonObject.put("customerId", customer.getId());
				jsonObject.put("amountDeposited", openingAccountAmount);
				
				
				
				hql = "Select tp.nameOnCard, tp.cardScheme_id, tp.status, tp.cardType, tp.serialNo, tp.cardBalance, tp.overDebit, tp.cardCharges, tp.expiryDate, tp.pin, tp.cvv"
						+ " from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
						+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				jsonObject.put("ecards", ecards==null ? null : ecards);
				jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
			
				jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				jsonObject.put("customerId", customer.getId());
				jsonObject.put("accountId", account.getId());
				jsonObject.put("accountIdentifier", account.getAccountIdentifier());
				jsonObject.put("message", "New Customer Wallet Created Successfully");
				jsonObject.put("transactionId", transaction.getId());
				
				
				log.info("Create New customer wallet = " + jsonObject.toString());
				
				
				

				
				

				
				String receipentMobileNumber = account.getCustomer().getContactMobile();
				String fname = account.getCustomer().getFirstName();
				String smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
				//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
				//swpService.createNewRecord(smsMsg);

				SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
				new Thread(smsSender).start();
				
				if(pn!=null)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
					smsMessage = "Your new Bevura Mastercard ending with **** "+pn.substring(pn.length()-4)+ ".\nExp Date: " + sdf.format(ex) + "\nPin: " + pn + "\nCVV: " + cv + ".\nPlease change your pin before you start using this card";
					//SMSMesage smsMsg1 = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
					//swpService.createNewRecord(smsMsg1);


					SmsSender smsSender1 = new SmsSender(swpService, smsMessage, receipentMobileNumber);
					new Thread(smsSender1).start();
				}
			}
			else
			{
				
				//Check If account exist
				
				log.info("Bank Holds Funds");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				District district = (District)swpService.getRecordById(District.class, districtId);

				Date dob = (new SimpleDateFormat("yyyy-MM-dd")).parse(dateOfBirth);
				customer.setAddressLine1(addressLine1);
				customer.setAddressLine2(addressLine2);
				customer.setLocationDistrict((District)this.swpService.getRecordById(District.class, districtId));
				customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(meansOfIdentificationType));
				customer.setMeansOfIdentificationNumber(meansOfIdentificationNumber);
				customer.setContactEmail(contactEmail);
				customer.setDateOfBirth(dob);
				customer.setLocationDistrict(district);
				customer.setGender(Gender.valueOf(gender));
				this.swpService.updateRecord(customer);
				log.info("getFirstName ..." + customer.getFirstName());
				log.info("getLastName ..." + customer.getLastName());
				log.info("getAddressLine1..." + customer.getAddressLine1());
				log.info("getAddressLine2..." + customer.getAddressLine2());
				log.info("getLocationDistrict..." + customer.getLocationDistrict());
				log.info("getMeansOfIdentificationType..." + customer.getMeansOfIdentificationType().name());
				log.info("getMeansOfIdentificationNumber..." + customer.getMeansOfIdentificationNumber());
				log.info("getContactEmail..." + customer.getContactEmail());
				log.info("getDateOfBirth..." + customer.getDateOfBirth());
				log.info("getGender..." + customer.getGender().name());
				log.info("getContactMobile..." + customer.getContactMobile());
				
				
				

				String accountNo = null;
				String mobileNumberFromBank = null;
				String branchCode = null;
				String meansOfIdentificationNumberrFromBank = null;
				String uniqueIdNameFromBank = null;
				
				
				
				jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
					customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
					customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
					sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
					"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MEANS_OF_ID_NUMBER");
				log.info("jsonObject ..." + jsonObject.toString());
				

				boolean bounceDueToMismatch = false;
				boolean walletExistsInBank = false;
				JSONArray customerWalletList = null;
				JSONObject customerWallet = null;
				if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
				{
					
					customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
					if(customerWalletList!=null && customerWalletList.length()>0)
					{
						for(int j1=0; j1<customerWalletList.length(); j1++)
						{
							if(customerWalletList.getJSONObject(j1).getString("mobileNo").equals(customer.getContactMobile().substring(3)))
							{
								walletExistsInBank = true;
								customerWallet = customerWalletList.getJSONObject(j1);
							}
							else
							{
								bounceDueToMismatch = true;
							}
						}
						
						if(walletExistsInBank==true && bounceDueToMismatch==false)
						{
							String custNo = customerWallet.getString("custNo");
							
							
							
							
							jsonObject = UtilityHelper.addWalletToCustomer(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
									customer.getLastName(), acquirer, customer, custNo, null, null, null, currencyCode, isTokenize);
							log.info("jsonObject ..." + jsonObject.toString());
							if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
							{
								
								String accountIdentifier = jsonObject.getString("accountIdentifier");
								Long accountId = jsonObject.getLong("accountId");
								String accountToken = jsonObject.has("accountToken") ? jsonObject.getString("accountToken") : null;
								
								
								if(accountIdentifier!=null)
								{
									hql = "Select tp from Account tp where tp.id = " + accountId;
									account = (Account)this.swpService.getUniqueRecordByHQL(hql);
									String receipentMobileNumber = account.getCustomer().getContactMobile();
									String fname = customer.getFirstName() + " " + customer.getLastName();
									String smsMessage = "";
									if(device.getSwitchToLive().equals(1))
										smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
									else
										smsMessage = "Hello "+fname+"\nA new Test Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
									//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
									//swpService.createNewRecord(smsMsg);

									SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
									new Thread(smsSender).start();
								}
								
								
								
								
								if(jsonObject!=null && jsonObject.has("status") && (jsonObject.getInt("status"))==(ERROR.CUSTOMER_CREATE_SUCCESS))
								{
									customer.setAddressLine1(addressLine1);
									customer.setAddressLine2(addressLine2);
									customer.setLocationDistrict((District)this.swpService.getRecordById(District.class, districtId));
									customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(meansOfIdentificationType));
									customer.setMeansOfIdentificationNumber(meansOfIdentificationNumber);
									customer.setContactEmail(contactEmail);
									customer.setDateOfBirth(dob);
									customer.setLocationDistrict(district);
									customer.setGender(Gender.valueOf(gender));
									this.swpService.updateRecord(customer);
									log.info("getFirstName ..." + customer.getFirstName());
									log.info("getLastName ..." + customer.getLastName());
									log.info("getAddressLine1..." + customer.getAddressLine1());
									log.info("getAddressLine2..." + customer.getAddressLine2());
									log.info("getLocationDistrict..." + customer.getLocationDistrict());
									log.info("getMeansOfIdentificationType..." + customer.getMeansOfIdentificationType().name());
									log.info("getMeansOfIdentificationNumber..." + customer.getMeansOfIdentificationNumber());
									log.info("getContactEmail..." + customer.getContactEmail());
									log.info("getDateOfBirth..." + customer.getDateOfBirth());
									log.info("getGender..." + customer.getGender().name());
									log.info("getContactMobile..." + customer.getContactMobile());
									
									
									//Long accountId = jsonObject.getLong("accountId");
									Account acct = (Account)this.swpService.getRecordById(Account.class, accountId);
									JSONObject cardJS = new JSONObject();
									cardJS.put("customerVerificationNo", customer.getVerificationNumber());
									cardJS.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
									cardJS.put("acquirerId", acct.getAcquirer().getId());
									cardJS.put("cardSchemeId", cardScheme.getId());
									cardJS.put("currencyCodeId", acct.getCurrencyCode());
									
									if(isTokenize!=null && isTokenize==1)
									{
										cardJS.put("isTokenize", isTokenize);

										cardJS.put("tokenizeMerchantCode", tokenizeMerchant.getMerchantCode());
										cardJS.put("tokenizeDeviceCode", tokenizeDevice.getDeviceCode());
										
									}

									//cardJS.put("corporateCustomerId", JSONObject.NULL);
									//cardJS.put("corporateCustomerAccountId", JSONObject.NULL);

									jsonObject.put("message", "New customer wallet creation was successful");
									
									if(isTokenize!=null && isTokenize==1)
									{
										jsonObject.put("accountToken", jsonObject.getString("accountToken"));
									}
									jsonObject.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
									jsonObject.put("customerVerificationNo", customer.getVerificationNumber());
									jsonObject.put("customername", customer.getFirstName() + " " + customer.getLastName());
									jsonObject.put("mobileContact", customer.getContactMobile());
									jsonObject.put("status", ERROR.GENERAL_OK);
									
									hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
									List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
									jsonObject.put("accounts", accounts==null ? null : accounts);
									jsonObject.put("walletExists", accounts!=null && accounts.size()>0 ? true : false);
									

									String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
									String encryptedData =  UtilityHelper.encryptData(cardJS.toString(), bankKey);
									Response newCard = this.createTutukaVirtualCard(
											httpHeaders,
											requestContext,
											encryptedData, 
											token);
									String cardRespStr = (String)(newCard.getEntity());
									if(cardRespStr!=null)
									{
										JSONObject cardResp = new JSONObject(cardRespStr);
										Integer cardRespStatus = cardResp.has("status") ? cardResp.getInt("status") : null;
										if(cardRespStatus!=null && cardRespStatus==(ERROR.GENERAL_OK))
										{
											Long cardCreatedId = cardResp.has("cardId") ? cardResp.getLong("cardId") : null;
											String cardCreatedSerialNo = cardResp.has("serialNo") ? cardResp.getString("serialNo") : null;
											String cardCreatedType = cardResp.has("cardType") ? cardResp.getString("cardType") : null;
											String cardCreatedNameOnCard = cardResp.has("nameOnCard") ? cardResp.getString("nameOnCard") : null;
											if(cardCreatedId!=null)
											{
												jsonObject.put("cardSerialNo", cardCreatedSerialNo);
												jsonObject.put("cardType", cardCreatedType);
												jsonObject.put("nameOnCard", cardCreatedNameOnCard);
												if(cardCreatedType.equals(CardType.TUTUKA_VIRTUAL_CARD.name()))
												{
													String pn = (cardResp.has("pan") ? cardResp.getString("pan") : null);
													String ein = (cardResp.has("pin") ? cardResp.getString("pin") : null);
													String ex = (cardResp.has("expiryDate") ? cardResp.getString("expiryDate") : null);
													String cv = (cardResp.has("cvv") ? cardResp.getString("cvv") : null);
													
													if(pn!=null)
														jsonObject.put("cardPan", (pn.substring(pn.length()-4)));
													
													if(ein!=null)
														jsonObject.put("ecardpin", UtilityHelper.encryptData(ein, bankKey));
													
													if(ex!=null)
													{
														SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd"); 
														Date expd = sdf1.parse(ex);
														sdf1 = new SimpleDateFormat("MM/yy");
														String expd1 = sdf1.format(expd);
														jsonObject.put("ecardexpire", UtilityHelper.encryptData(expd1, bankKey));
													}
													
													
													
													if(pn!=null)
													{
														String receipentMobileNumber = account.getCustomer().getContactMobile();
														sdf = new SimpleDateFormat("yy-MM");
														String smsMessage = "Hello "+(customer.getFirstName() + " " + customer.getLastName())+"\nYour new Bevura Master Card ending with **** "+pn.substring(pn.length()-4)+ ".\nExp Date: " + sdf.format(ex) + "\nPin: " + pn + "\nCVV: " + cv + ".\nPlease change your pin before you start using this card";
														//SMSMesage smsMsg1 = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
														//swpService.createNewRecord(smsMsg1);

														SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
														new Thread(smsSender).start();
													}
												}
												
												hql = "Select tp.nameOnCard, tp.cardScheme_id, tp.status, tp.cardType, tp.serialNo, tp.cardBalance, tp.overDebit, tp.cardCharges, tp.expiryDate, tp.pin, tp.cvv"
														+ " from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
														+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
												List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
												
												hql = "Select bt.type, bt.token, bt.merchantId, bt.deviceId, tp.nameOnCard, tp.serialNo, tp.expiryDate, bt.cardId"
														+ " from ecards tp, bevura_tokens bt  where bt.cardId = tp.id AND (bt.cardId = " + cardCreatedId + ")"
														+ " AND (tp.deleted_at IS NULL) AND bt.isLive = " + isLive + " AND tp.isLive = " + isLive;
												List<Map<String, Object>> cardBevuraTokens = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
												
												hql = "Select bt.type, bt.merchantId, bt.deviceId, bt.token, bt.accountId"
														+ " from bevura_tokens bt  where (bt.accountId = " + accountId + ")"
														+ " AND (bt.deleted_at IS NULL) AND bt.isLive = " + isLive;
												List<Map<String, Object>> acctBevuraTokens = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
												
												
												
												Iterator<Map<String, Object>> bvIt = cardBevuraTokens.iterator();
												
												JSONArray tokenArray = new JSONArray();
												while(bvIt.hasNext())
												{
													Map<String, Object> bv = bvIt.next();
													if(device!=null)
													{
														BigInteger mchtId = (BigInteger)bv.get("merchantId");
														BigInteger dvId = (BigInteger)bv.get("deviceId");
														BigInteger bvActId = (BigInteger)bv.get("accountId");
														BigInteger bvCdId = (BigInteger)bv.get("cardId");
														String bvtype = (String)bv.get("type");
														String bvtoken = (String)bv.get("token");
														if(mchtId.longValue()==device.getMerchant().getId() && dvId.longValue()==device.getId())
														{
															JSONObject tokens = new JSONObject();
															if(bvtype.equals("CARD"))
															{
																if(bvCdId!=null)
																{
																	ECard ecard = (ECard)this.swpService.getRecordById(ECard.class, bvCdId.longValue());
																	tokens.put("tokenTitle", ecard.getSerialNo().substring(0, 4) + " **** **** **** " + (ecard.getSerialNo().substring(ecard.getSerialNo().length()-4)));
																	tokens.put("code", bvtoken);
																	tokens.put("type", "CARD");
																}
															}
															tokenArray.put(tokens);
														}
													}
													else
													{
														BigInteger bvActId = (BigInteger)bv.get("accountId");
														BigInteger bvCdId = (BigInteger)bv.get("cardId");
														String bvtype = (String)bv.get("type");
														String bvtoken = (String)bv.get("token");
														
														JSONObject tokens = new JSONObject();
														if(bvtype.equals("CARD"))
														{
															if(bvCdId!=null)
															{
																ECard ecard = (ECard)this.swpService.getRecordById(ECard.class, bvCdId.longValue());
																tokens.put("tokenTitle", ecard.getSerialNo().substring(0, 4) + " **** **** **** " + (ecard.getSerialNo().substring(ecard.getSerialNo().length()-4)));
																tokens.put("code", bvtoken);
																tokens.put("type", "CARD");
															}
														}
														tokenArray.put(tokens);
														
													}
												}
												
												bvIt = acctBevuraTokens.iterator();
												while(bvIt.hasNext())
												{
													Map<String, Object> bv = bvIt.next();
													if(device!=null)
													{
														BigInteger mchtId = (BigInteger)bv.get("merchantId");
														BigInteger dvId = (BigInteger)bv.get("deviceId");
														BigInteger bvActId = (BigInteger)bv.get("accountId");
														BigInteger bvCdId = (BigInteger)bv.get("cardId");
														String bvtype = (String)bv.get("type");
														String bvtoken = (String)bv.get("token");
														if(mchtId.longValue()==device.getMerchant().getId() && dvId.longValue()==device.getId())
														{
															JSONObject tokens = new JSONObject();
															if(bvtype.equals("ACCOUNT"))
															{
																if(bvActId!=null)
																{
																	Account account_ = (Account)this.swpService.getRecordById(Account.class, bvActId.longValue());
																	tokens.put("tokenTitle", " **** **** **** " + account_.getAccountIdentifier().substring(account_.getAccountIdentifier().length()-4));
																	tokens.put("code", bvtoken);
																	tokens.put("type", "ACCOUNT");
																}
															}
															tokenArray.put(tokens);
														}
													}
													else
													{
														BigInteger bvActId = (BigInteger)bv.get("accountId");
														BigInteger bvCdId = (BigInteger)bv.get("cardId");
														String bvtype = (String)bv.get("type");
														String bvtoken = (String)bv.get("token");
														
														JSONObject tokens = new JSONObject();
														if(bvtype.equals("ACCOUNT"))
														{
															if(bvActId!=null)
															{
																Account account_ = (Account)this.swpService.getRecordById(Account.class, bvActId.longValue());
																tokens.put("tokenTitle", " **** **** **** " + account_.getAccountIdentifier().substring(account_.getAccountIdentifier().length()-4));
																tokens.put("code", bvtoken);
																tokens.put("type", "ACCOUNT");
															}
														}
														tokenArray.put(tokens);
														
													}
												}
												
												jsonObject.put("ecards", ecards==null ? null : ecards);
												jsonObject.put("tokenListing", tokenArray);
												jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
												return Response.status(200).entity(jsonObject.toString()).build();
											}
										}
									}
								}
								else
								{
									jsonObject.put("message", "New customer wallet creation was not successful");
									jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
									return Response.status(200).entity(jsonObject.toString()).build();
								}
							}
							else
							{
								//jsonObject.put("message", "We have created a profile for you. We however can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what exists at the bank");
								//jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
								//return Response.status(200).entity(jsonObject.toString()).build();
							}
						}
						else
						{
							
						}
						
//						if(bounceDueToMismatch==false && customerWallet!=null)
//						{
//							jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
//								customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
//								customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
//								sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
//								"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
//							log.info("jsonObject ..." + jsonObject.toString());
//							if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
//							{
//								
//								customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
//								if(customerWalletList!=null && customerWalletList.length()>0)
//								{
//									walletExistsInBank = true;
//								}
//							}
//							else
//							{
//								//jsonObject.put("message", "We have created a profile for you. We however can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what exists at the bank");
//								//jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
//								//return Response.status(200).entity(jsonObject.toString()).build();
//							}
//						}
					}
				}
				else
				{
					jsonObject = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
						customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
						customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
						sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
						"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
					log.info("jsonObject ..." + jsonObject.toString());
					if(jsonObject!=null && jsonObject.has("status") && jsonObject.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
					{
						
						customerWalletList = jsonObject.has("customerWalletList") ? jsonObject.getJSONArray("customerWalletList") : null;
						if(customerWalletList!=null && customerWalletList.length()>0)
						{
							for(int j1=0; j1<customerWalletList.length(); j1++)
							{
								if(customerWalletList.getJSONObject(j1).getString("nationalId").equals(customer.getMeansOfIdentificationNumber()))
								{
									walletExistsInBank = true;
									customerWallet = customerWalletList.getJSONObject(j1);
								}
								else
								{
									bounceDueToMismatch = true;
								}
							}
						}
					}
				}
				
				

				String contactMobile = customer.getContactMobile();
				String contactMobileTemp = contactMobile.substring(3);
				if(walletExistsInBank==true)
				{
					
					
					
					String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
					CardScheme accountScheme = null;
					if(defaultAccountSchemeIdObj!=null)
					{
						Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
						accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
					}
					
					ProbasePayCurrency currency = ProbasePayCurrency.valueOf(currencyCode);
					/*String accountNo = customerWallet.getString("accountNo");
					String mobileNumberFromBank = customerWallet.getString("mobileNumber");
					String meansOfIdentificationNumberrFromBank = customerWallet.getString("meansOfIdentificationNumber");*/
					accountNo = customerWallet.getString("accountNo");
					mobileNumberFromBank = customerWallet.getString("mobileNo");
					meansOfIdentificationNumberrFromBank = customerWallet.getString("nationalId");
					//meansOfIdentificationNumberrFromBank = customerWallet.getString("uniqueIdVal");
					branchCode = null;
					log.info("contactMobileTemp..." + contactMobileTemp);
					log.info("contactMobileTemp..." + contactMobileTemp);
					log.info("mobileNumberFromBank..." + mobileNumberFromBank);
					log.info("meansOfIdentificationNumberrFromBank..." + meansOfIdentificationNumberrFromBank);
					log.info("mobileNumberFromBank..." + mobileNumberFromBank);
					log.info("meansOfIdentificationNumberrFromBank..." + meansOfIdentificationNumberrFromBank);
					
					if(!contactMobileTemp.equals(mobileNumberFromBank) || !meansOfIdentificationNumber.equals(meansOfIdentificationNumberrFromBank))
					{
						jsonObject.put("status", ERROR.INVALID_PARAMETERS);
						jsonObject.put("message", "We have created a profile for you. We however can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank");
						
						
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					
					String otp = RandomStringUtils.randomNumeric(4);
					String otpRef = RandomStringUtils.randomNumeric(6);
					
					//User user = customer.getUser();
					//user.setOtp(otp);
					//swpService.updateRecord(user);

					AccountMapRequest accountMapRequest = new AccountMapRequest(customer.getId(), com.probase.probasepay.enumerations.AccountStatus.INACTIVE , otp, otpRef, 
							branchCode, acquirer.getId(), currencyCode, AccountType.valueOf(accountType), null, null,
							currency, 0.00, 0.00, accountScheme.getId(), null, accountNo, 0, isTokenize, device.getSwitchToLive());
					
					accountMapRequest = (AccountMapRequest)swpService.createNewRecord(accountMapRequest);
					
					String receipentMobileNumber = mobileNumberFromBank;
					String smsMessage = "Hello,\n We received a request to link your ZICB wallet - "+ accountNo+" to your Bevura profile. Please enter the OTP - "+otp+" to confirm this action originated from you.\nIf you did not originate this request please call us on 260977000000";
					SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
					new Thread(smsSender).start();
					jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
					jsonObject.put("message", "Enter the OTP sent to your mobile number to complete this process");
					jsonObject.put("customerVerificationNo", customer.getVerificationNumber());
					
					
					
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					if(bounceDueToMismatch==true)
					{
						jsonObject.put("status", ERROR.INVALID_PARAMETERS);
						jsonObject.put("message", "We have created a profile for you. We however can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank");
						
						
						return Response.status(200).entity(jsonObject.toString()).build();
					}
				}
				
				/*if(walletExistsInBank==true)
				{
					
					
					
				}
				JSONObject jsonObjectForIdCheck = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
					customer.getLastName(), 
					customer.getAddressLine1(), 
					customer.getAddressLine2(), 
					customer.getLocationDistrict().getName(), 
					customer.getLocationDistrict().getProvinceName(), 
					customer.getLocationDistrict().getCountryName(), 
					customer.getMeansOfIdentificationType().name(), 
					customer.getMeansOfIdentificationNumber(), 
					sdf.format(customer.getDateOfBirth()), 
					customer.getContactEmail(), 
					customer.getGender().name().toUpperCase().substring(0,1), 
					customer.getContactMobile(), 
					"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MEANS_OF_ID_NUMBER");
				log.info("jsonObject ..." + jsonObject.toString());
				
				JSONObject jsonObjectForMobileCheck = UtilityHelper.verifyZICBExistCustomerByMobileNumberOrIdNo(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
						customer.getLastName(), customer.getAddressLine1(), customer.getAddressLine2(), customer.getLocationDistrict().getName(), customer.getLocationDistrict().getProvinceName(), 
						customer.getLocationDistrict().getCountryName(), customer.getMeansOfIdentificationType().name(), customer.getMeansOfIdentificationNumber(), 
						sdf.format(customer.getDateOfBirth()), customer.getContactEmail(), customer.getGender().name().toUpperCase().substring(0,1), customer.getContactMobile(), 
						"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), customer.getLocationDistrict(), isSettlementAccount, "MOBILE_NUMBER");
					log.info("jsonObject ..." + jsonObject.toString());
					
				boolean walletExistsInBank = false;
				JSONArray customerWalletList = null;
				if(jsonObjectForMobileCheck!=null && jsonObjectForMobileCheck.has("status") && jsonObjectForMobileCheck.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
				{
					
					customerWalletList = jsonObjectForMobileCheck.has("customerWalletList") ? jsonObjectForMobileCheck.getJSONArray("customerWalletList") : null;
					if(customerWalletList!=null && customerWalletList.length()>0)
					{
						walletExistsInBank = true;
						

						JSONObject customerWallet = customerWalletList.getJSONObject(0);
						accountNo = customerWallet.getString("accountNo");
						mobileNumberFromBank = customerWallet.getString("mobileNo");
						branchCode = customerWallet.getString("branch");
						uniqueIdNameFromBank = customerWallet.getString("uniqueIdName");
						meansOfIdentificationNumberrFromBank = customerWallet.getString("uniqueIdVal");
						
						if(!customer.getContactMobile().equals(mobileNumberFromBank) || !customer.getMeansOfIdentificationNumber().equals(meansOfIdentificationNumberrFromBank))
						{
							jsonObject.put("status", ERROR.INVALID_PARAMETERS);
							jsonObject.put("message", "We can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank. Call us on 7000 to assist you in fixing this issue");
							
							
							return Response.status(200).entity(jsonObject.toString()).build();
						}
					}
				}
				if(jsonObjectForIdCheck!=null && jsonObjectForIdCheck.has("status") && jsonObjectForIdCheck.getInt("status")==ERROR.CUSTOMER_WALLET_EXISTS)
				{
					
					customerWalletList = jsonObjectForIdCheck.has("customerWalletList") ? jsonObjectForIdCheck.getJSONArray("customerWalletList") : null;
					if(customerWalletList!=null && customerWalletList.length()>0)
					{
						walletExistsInBank = true;
						
						JSONObject customerWallet = customerWalletList.getJSONObject(0);
						//accountNo = customerWallet.getString("accountNo");
						mobileNumberFromBank = customerWallet.getString("mobileNo");
						//branchCode = customerWallet.getString("branch");
						uniqueIdNameFromBank = customerWallet.getString("uniqueIdName");
						meansOfIdentificationNumberrFromBank = customerWallet.getString("uniqueIdVal");
						
						if(!customer.getContactMobile().equals(mobileNumberFromBank) || !customer.getMeansOfIdentificationNumber().equals(meansOfIdentificationNumberrFromBank))
						{
							jsonObject.put("status", ERROR.INVALID_PARAMETERS);
							jsonObject.put("message", "We can not create a wallet for you. We found a mismatch in the mobile number and identification number provided with what you provided the bank. Call us on 7000 to assist you in fixing this issue");
							
							
							return Response.status(200).entity(jsonObject.toString()).build();
						}
					}
				}
				
				
				if(walletExistsInBank==true)
				{
					JSONObject customerWallet = customerWalletList.getJSONObject(0);
					
					String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
					CardScheme accountScheme = null;
					if(defaultAccountSchemeIdObj!=null)
					{
						Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
						accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
					}
					
					ProbasePayCurrency currency = ProbasePayCurrency.valueOf(currencyCode);
					
					
					
					
					User user = customer.getUser();

					
					
					
					String receipentMobileNumber = mobileNumberFromBank;
					String smsMessage = "";
					
					if(user.getOtp()!=null)
					{
						String otp = user.getOtp();
						String otpRef = RandomStringUtils.randomNumeric(6);
						AccountMapRequest accountMapRequest = new AccountMapRequest(customer.getId(), com.probase.probasepay.enumerations.AccountStatus.INACTIVE , otp, otpRef, 
								branchCode, acquirer.getId(), currencyCode, AccountType.valueOf(accountType), null, null,
								currency, 0.00, 0.00, accountScheme.getId(), null, accountNo, 0);

						accountMapRequest = (AccountMapRequest)swpService.createNewRecord(accountMapRequest);
						smsMessage = "Hello,\nWe received a request to link your ZICB wallet/account - "+ accountNo+" to your Bevura profile. Please enter the OTP we sent to your number to confirm this request originated from you.\nIf you did not originate this request please call us on 260977000000";
						SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
						new Thread(smsSender).start();
					}
					else
					{

						String otp = RandomStringUtils.randomNumeric(4);
						String otpRef = RandomStringUtils.randomNumeric(6);
						
						user.setOtp(otp);
						swpService.updateRecord(user);
						AccountMapRequest accountMapRequest = new AccountMapRequest(customer.getId(), com.probase.probasepay.enumerations.AccountStatus.INACTIVE , otp, otpRef, 
								branchCode, acquirer.getId(), currencyCode, AccountType.valueOf(accountType), null, null,
								currency, 0.00, 0.00, accountScheme.getId(), null, accountNo, 0);

						accountMapRequest = (AccountMapRequest)swpService.createNewRecord(accountMapRequest);
						smsMessage = "Hello,\nWe received a request to link your ZICB wallet/account - "+ accountNo+" to your Bevura profile. Please enter the OTP - "+otp+" to confirm this request originated from you.\nIf you did not originate this request please call us on 260977000000";
						SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
						new Thread(smsSender).start();
					}
					jsonObject.put("status", ERROR.OTP_GENERATE_SUCCESS);
					jsonObject.put("message", "Enter the OTP sent to your mobile number to complete this process");
					
					
					
					
					
					return Response.status(200).entity(jsonObject.toString()).build();
				}*/
				
						
						
				switch (acquirer.getBank().getBankCode())
				{
					case Application.ZICB_BANK_CODE:
						if(customer.getFirstName()!=null && customer.getFirstName().length()>0 && 
							customer.getLastName()!=null && customer.getLastName().length()>0 && 
								addressLine1!=null && addressLine1.length()>0 && 
									addressLine2!=null && addressLine2.length()>0 && 
										district!=null && meansOfIdentificationType!=null && 
											meansOfIdentificationNumber!=null && dateOfBirth!=null && 
												contactEmail!=null && gender!=null && customer.getContactMobile()!=null)
						{
							log.info("Customer Profile Data provided");
							jsonObject = UtilityHelper.createZICBWallet(this.swpService, logId, token, merchantCode, deviceCode, customer.getFirstName(), 
									customer.getLastName(), addressLine1, addressLine2, district.getName(), district.getProvinceName(), 
									district.getCountryName(), meansOfIdentificationType, meansOfIdentificationNumber, 
									dateOfBirth, contactEmail, gender.toUpperCase().substring(0,1), customer.getContactMobile(), 
									"WA", currencyCode, null, null, null, null, acquirer, null, null, customer.getVerificationNumber(), district, isSettlementAccount, isTokenize);
							
							
							
							String accountIdentifier = jsonObject.getString("accountIdentifier");
							Long accountId = jsonObject.getLong("accountId");
							String accountToken = jsonObject.has("accountToken") ? jsonObject.getString("accountToken") : null;
							
							
							if(accountIdentifier!=null)
							{
								hql = "Select tp from Account tp where tp.id = " + accountId;
								account = (Account)this.swpService.getUniqueRecordByHQL(hql);
								String receipentMobileNumber = account.getCustomer().getContactMobile();
								String fname = customer.getFirstName() + " " + customer.getLastName();
								String smsMessage = "";
								if(device.getSwitchToLive().equals(1))
									smsMessage = "Hello "+fname+"\nA new Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
								else
									smsMessage = "Hello "+fname+"\nA new Test Bevura Wallet - "+ (account.getAccountIdentifier()) +" has been created for you. Please proceed to fund your wallet and start using your wallet";
								//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
								//swpService.createNewRecord(smsMsg);

								SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
								new Thread(smsSender).start();
							}
							
							
							
							
							if(jsonObject!=null && jsonObject.has("status") && (jsonObject.getInt("status"))==(ERROR.CUSTOMER_CREATE_SUCCESS))
							{
								customer.setAddressLine1(addressLine1);
								customer.setAddressLine2(addressLine2);
								customer.setLocationDistrict((District)this.swpService.getRecordById(District.class, districtId));
								customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(meansOfIdentificationType));
								customer.setMeansOfIdentificationNumber(meansOfIdentificationNumber);
								customer.setContactEmail(contactEmail);
								customer.setDateOfBirth(dob);
								customer.setLocationDistrict(district);
								customer.setGender(Gender.valueOf(gender));
								this.swpService.updateRecord(customer);
								log.info("getFirstName ..." + customer.getFirstName());
								log.info("getLastName ..." + customer.getLastName());
								log.info("getAddressLine1..." + customer.getAddressLine1());
								log.info("getAddressLine2..." + customer.getAddressLine2());
								log.info("getLocationDistrict..." + customer.getLocationDistrict());
								log.info("getMeansOfIdentificationType..." + customer.getMeansOfIdentificationType().name());
								log.info("getMeansOfIdentificationNumber..." + customer.getMeansOfIdentificationNumber());
								log.info("getContactEmail..." + customer.getContactEmail());
								log.info("getDateOfBirth..." + customer.getDateOfBirth());
								log.info("getGender..." + customer.getGender().name());
								log.info("getContactMobile..." + customer.getContactMobile());
								
								
								//Long accountId = jsonObject.getLong("accountId");
								Account acct = (Account)this.swpService.getRecordById(Account.class, accountId);
								JSONObject cardJS = new JSONObject();
								cardJS.put("customerVerificationNo", customer.getVerificationNumber());
								cardJS.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
								cardJS.put("acquirerId", acct.getAcquirer().getId());
								cardJS.put("cardSchemeId", cardScheme.getId());
								cardJS.put("currencyCodeId", acct.getCurrencyCode());
								
								if(isTokenize!=null && isTokenize==1)
								{
									cardJS.put("isTokenize", isTokenize);

									cardJS.put("tokenizeMerchantCode", tokenizeMerchant.getMerchantCode());
									cardJS.put("tokenizeDeviceCode", tokenizeDevice.getDeviceCode());
									
								}

								//cardJS.put("corporateCustomerId", JSONObject.NULL);
								//cardJS.put("corporateCustomerAccountId", JSONObject.NULL);

								jsonObject.put("message", "New customer wallet creation was successful");
								
								if(isTokenize!=null && isTokenize==1)
								{
									jsonObject.put("accountToken", jsonObject.getString("accountToken"));
								}
								jsonObject.put("accountIdentifier", jsonObject.getString("accountIdentifier"));
								jsonObject.put("customerVerificationNo", customer.getVerificationNumber());
								jsonObject.put("customername", customer.getFirstName() + " " + customer.getLastName());
								jsonObject.put("mobileContact", customer.getContactMobile());
								jsonObject.put("status", ERROR.GENERAL_OK);
								
								hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
								List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
								jsonObject.put("accounts", accounts==null ? null : accounts);
								jsonObject.put("walletExists", accounts!=null && accounts.size()>0 ? true : false);
								

								String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
								String encryptedData =  UtilityHelper.encryptData(cardJS.toString(), bankKey);
								Response newCard = this.createTutukaVirtualCard(
										httpHeaders,
										requestContext,
										encryptedData, 
										token);
								String cardRespStr = (String)(newCard.getEntity());
								if(cardRespStr!=null)
								{
									JSONObject cardResp = new JSONObject(cardRespStr);
									Integer cardRespStatus = cardResp.has("status") ? cardResp.getInt("status") : null;
									if(cardRespStatus!=null && cardRespStatus==(ERROR.GENERAL_OK))
									{
										Long cardCreatedId = cardResp.has("cardId") ? cardResp.getLong("cardId") : null;
										String cardCreatedSerialNo = cardResp.has("serialNo") ? cardResp.getString("serialNo") : null;
										String cardCreatedType = cardResp.has("cardType") ? cardResp.getString("cardType") : null;
										String cardCreatedNameOnCard = cardResp.has("nameOnCard") ? cardResp.getString("nameOnCard") : null;
										if(cardCreatedId!=null)
										{
											jsonObject.put("cardSerialNo", cardCreatedSerialNo);
											jsonObject.put("cardType", cardCreatedType);
											jsonObject.put("nameOnCard", cardCreatedNameOnCard);
											if(cardCreatedType.equals(CardType.TUTUKA_VIRTUAL_CARD.name()))
											{
												String pn = (cardResp.has("pan") ? cardResp.getString("pan") : null);
												String ein = (cardResp.has("pin") ? cardResp.getString("pin") : null);
												String ex = (cardResp.has("expiryDate") ? cardResp.getString("expiryDate") : null);
												String cv = (cardResp.has("cvv") ? cardResp.getString("cvv") : null);
												
												if(pn!=null)
													jsonObject.put("cardPan", (pn.substring(pn.length()-4)));
												
												if(ein!=null)
													jsonObject.put("ecardpin", UtilityHelper.encryptData(ein, bankKey));
												
												if(ex!=null)
												{
													SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd"); 
													Date expd = sdf1.parse(ex);
													sdf1 = new SimpleDateFormat("MM/yy");
													String expd1 = sdf1.format(expd);
													jsonObject.put("ecardexpire", UtilityHelper.encryptData(expd1, bankKey));
												}
												
												
												
												if(pn!=null)
												{
													String receipentMobileNumber = account.getCustomer().getContactMobile();
													sdf = new SimpleDateFormat("yy-MM");
													String smsMessage = "Hello "+(customer.getFirstName() + " " + customer.getLastName())+"\nYour new Bevura Master Card ending with **** "+pn.substring(pn.length()-4)+ ".\nExp Date: " + sdf.format(ex) + "\nPin: " + pn + "\nCVV: " + cv + ".\nPlease change your pin before you start using this card";
													//SMSMesage smsMsg1 = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
													//swpService.createNewRecord(smsMsg1);

													SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
													new Thread(smsSender).start();
												}
											}
											
											hql = "Select tp.nameOnCard, tp.cardScheme_id, tp.status, tp.cardType, tp.serialNo, tp.cardBalance, tp.overDebit, tp.cardCharges, tp.expiryDate, tp.pin, tp.cvv"
													+ " from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
													+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
											List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
											
											hql = "Select bt.type, bt.token, bt.merchantId, bt.deviceId, tp.nameOnCard, tp.serialNo, tp.expiryDate, bt.cardId"
													+ " from ecards tp, bevura_tokens bt  where bt.cardId = tp.id AND (bt.cardId = " + cardCreatedId + ")"
													+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
											List<Map<String, Object>> cardBevuraTokens = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
											
											hql = "Select bt.type, bt.merchantId, bt.deviceId, bt.token, bt.accountId"
													+ " from bevura_tokens bt  where (bt.accountId = " + accountId + ")"
													+ " AND (bt.deleted_at IS NULL) AND tp.isLive = " + isLive;
											List<Map<String, Object>> acctBevuraTokens = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
											
											
											
											Iterator<Map<String, Object>> bvIt = cardBevuraTokens.iterator();
											
											JSONArray tokenArray = new JSONArray();
											while(bvIt.hasNext())
											{
												Map<String, Object> bv = bvIt.next();
												if(device!=null)
												{
													BigInteger mchtId = (BigInteger)bv.get("merchantId");
													BigInteger dvId = (BigInteger)bv.get("deviceId");
													BigInteger bvActId = (BigInteger)bv.get("accountId");
													BigInteger bvCdId = (BigInteger)bv.get("cardId");
													String bvtype = (String)bv.get("type");
													String bvtoken = (String)bv.get("token");
													if(mchtId.longValue()==device.getMerchant().getId() && dvId.longValue()==device.getId())
													{
														JSONObject tokens = new JSONObject();
														if(bvtype.equals("CARD"))
														{
															if(bvCdId!=null)
															{
																ECard ecard = (ECard)this.swpService.getRecordById(ECard.class, bvCdId.longValue());
																tokens.put("tokenTitle", ecard.getSerialNo().substring(0, 4) + " **** **** **** " + (ecard.getSerialNo().substring(ecard.getSerialNo().length()-4)));
																tokens.put("code", bvtoken);
																tokens.put("type", "CARD");
															}
														}
														tokenArray.put(tokens);
													}
												}
												else
												{
													BigInteger bvActId = (BigInteger)bv.get("accountId");
													BigInteger bvCdId = (BigInteger)bv.get("cardId");
													String bvtype = (String)bv.get("type");
													String bvtoken = (String)bv.get("token");
													
													JSONObject tokens = new JSONObject();
													if(bvtype.equals("CARD"))
													{
														if(bvCdId!=null)
														{
															ECard ecard = (ECard)this.swpService.getRecordById(ECard.class, bvCdId.longValue());
															tokens.put("tokenTitle", ecard.getSerialNo().substring(0, 4) + " **** **** **** " + (ecard.getSerialNo().substring(ecard.getSerialNo().length()-4)));
															tokens.put("code", bvtoken);
															tokens.put("type", "CARD");
														}
													}
													tokenArray.put(tokens);
													
												}
											}
											
											bvIt = acctBevuraTokens.iterator();
											while(bvIt.hasNext())
											{
												Map<String, Object> bv = bvIt.next();
												if(device!=null)
												{
													BigInteger mchtId = (BigInteger)bv.get("merchantId");
													BigInteger dvId = (BigInteger)bv.get("deviceId");
													BigInteger bvActId = (BigInteger)bv.get("accountId");
													BigInteger bvCdId = (BigInteger)bv.get("cardId");
													String bvtype = (String)bv.get("type");
													String bvtoken = (String)bv.get("token");
													if(mchtId.longValue()==device.getMerchant().getId() && dvId.longValue()==device.getId())
													{
														JSONObject tokens = new JSONObject();
														if(bvtype.equals("ACCOUNT"))
														{
															if(bvActId!=null)
															{
																Account account_ = (Account)this.swpService.getRecordById(Account.class, bvActId.longValue());
																tokens.put("tokenTitle", " **** **** **** " + account_.getAccountIdentifier().substring(account_.getAccountIdentifier().length()-4));
																tokens.put("code", bvtoken);
																tokens.put("type", "ACCOUNT");
															}
														}
														tokenArray.put(tokens);
													}
												}
												else
												{
													BigInteger bvActId = (BigInteger)bv.get("accountId");
													BigInteger bvCdId = (BigInteger)bv.get("cardId");
													String bvtype = (String)bv.get("type");
													String bvtoken = (String)bv.get("token");
													
													JSONObject tokens = new JSONObject();
													if(bvtype.equals("ACCOUNT"))
													{
														if(bvActId!=null)
														{
															Account account_ = (Account)this.swpService.getRecordById(Account.class, bvActId.longValue());
															tokens.put("tokenTitle", " **** **** **** " + account_.getAccountIdentifier().substring(account_.getAccountIdentifier().length()-4));
															tokens.put("code", bvtoken);
															tokens.put("type", "ACCOUNT");
														}
													}
													tokenArray.put(tokens);
													
												}
											}
											
											jsonObject.put("ecards", ecards==null ? null : ecards);
											jsonObject.put("tokenListing", tokenArray);
											jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
										}
									}
								}
							}
							else
							{
								jsonObject.put("message", "New customer wallet creation was not successful");
								jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
							}
						}
						else
						{
							log.info("Incomplete customer profile data");
							jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
							if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
								jsonObject.put("message", "New Customer Wallet Creation Failed. Please ensure customers profile is fully updated");
							else
								jsonObject.put("message", "New Customer Wallet Creation Failed. Please ensure your profile is fully updated");
						}
						break;
					default:
						break;
				}
				
			}
			

			log.info(jsonObject.toString());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				e.printStackTrace();
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	@POST
	@Path("/getCustomerDataByAccountNumberAndId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerDataByAccountNumberAndId(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("mobileNumber") String mobileNo, 
			@FormParam("meansOfIdentificationNumber") String meansOfIdentificationNumber,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("token") String token)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		String logId = RandomStringUtils.randomAlphanumeric(10);
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
		try {
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
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			log.info("verifyJ ==" + verifyJ.toString());
			String branch_code = null;
			String acquirerCode = null;
			JSONObject allSettings = app.getAllSettings();
			
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				branch_code = verifyJ.getString("branchCode");
				log.info("branch_code ==" + branch_code);
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("acquirerCode ==" + acquirerCode);
			}
			else if(tokenUser.getRoleType().equals(RoleType.CUSTOMER))
			{
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("bankCode ==" + acquirerCode);
				branch_code = allSettings.getString("defaultnonfundsholdingbranchcode");
			}
			
			String hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			int isLive = 0;
			if(device!=null && device.getSwitchToLive().equals(1))
			{
				isLive = 1;
			}
			
			
			hql = "Select tp from Account tp where tp.customer.contactMobile = '" + mobileNo + "' AND "
					+ "tp.customer.meansOfIdentificationNumber = '"+meansOfIdentificationNumber+"' AND tp.isLive = " + isLive;
			Account acct = (Account)this.swpService.getUniqueRecordByHQL(hql);

			if(acct==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
				jsonObject.put("message", "Account matching details provided not found");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			customer = acct.getCustomer();
			jsonObject.put("message", "Customer details found");
			jsonObject.put("customerNumber", customer.getVerificationNumber());
			jsonObject.put("accountNo", acct.getAccountIdentifier());
			
			hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId()+" AND tp.isLive = " + isLive;
			List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
			jsonObject.put("walletExists", allaccounts!=null && allaccounts.size()>0 ? true : false);
			

			hql = "Select tp.nameOnCard, tp.cardScheme_id, tp.status, tp.cardType, tp.serialNo, tp.cardBalance, tp.overDebit, tp.cardCharges, tp.expiryDate, tp.pin, tp.cvv"
					+ " from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
					+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
			List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
			jsonObject.put("ecards", ecards==null ? null : ecards);
			jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
			jsonObject.put("customerNumber", customer.getVerificationNumber());
			jsonObject.put("customerId", customer.getId());
			jsonObject.put("accountId", acct.getId());
			jsonObject.put("accountIdentifier", acct.getAccountIdentifier());
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Customer account details found");					
								
			

			log.info(jsonObject.toString());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "General System error");
				e.printStackTrace();
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	@POST
	@Path("/getCustomerDataByMobileNumberAndVerificationNumber")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerDataByMobileNumberAndVerificationNumber(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("mobileNumber") String mobileNo, 
			@FormParam("customerVerificationNumber") String customerVerificationNumber,
			@FormParam("token") String token)
	{
		//Integer bankId = 1;
		//String branchCode = "053";
		//Integer acquirerId = 1; 
		//Integer issuerId = 1;
		String logId = RandomStringUtils.randomAlphanumeric(10);
		Customer customer = null;
		
		JSONObject jsonObject = new JSONObject();
		log.info("Create New Customer");
		try {
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
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			log.info("verifyJ ==" + verifyJ.toString());
			String branch_code = null;
			String acquirerCode = null;
			JSONObject allSettings = app.getAllSettings();
			
			if(tokenUser.getRoleType().equals(RoleType.BANK_STAFF))
			{
				branch_code = verifyJ.getString("branchCode");
				log.info("branch_code ==" + branch_code);
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("acquirerCode ==" + acquirerCode);
			}
			else if(tokenUser.getRoleType().equals(RoleType.CUSTOMER))
			{
				acquirerCode = verifyJ.getString("acquirerCode");
				log.info("bankCode ==" + acquirerCode);
				branch_code = allSettings.getString("defaultnonfundsholdingbranchcode");
			}
			
			String hql = "Select tp from Account tp where tp.customer.contactMobile = '" + mobileNo + "' AND tp.customer.verificationNumber = '"+customerVerificationNumber+"'";
			Account acct = (Account)this.swpService.getUniqueRecordByHQL(hql);

			if(acct==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_ACCOUNT_NOT_FOUND);
				jsonObject.put("message", "Account matching details provided not found");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			customer = acct.getCustomer();
			jsonObject.put("message", "Customer details found");
			jsonObject.put("customerNumber", customer.getVerificationNumber());
			jsonObject.put("accountNo", acct.getAccountIdentifier());
			
			hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
			List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
			jsonObject.put("walletExists", allaccounts!=null && allaccounts.size()>0 ? true : false);
			

			hql = "Select tp.nameOnCard, tp.cardScheme_id, tp.status, tp.cardType, tp.serialNo, tp.cardBalance, tp.overDebit, tp.cardCharges, tp.expiryDate, tp.pin, tp.cvv"
					+ " from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
					+ " AND (tp.deleted_at IS NULL)";
			List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
			jsonObject.put("ecards", ecards==null ? null : ecards);
			jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
			jsonObject.put("customerNumber", customer.getVerificationNumber());
			jsonObject.put("customerId", customer.getId());
			jsonObject.put("accountId", acct.getId());
			jsonObject.put("accountIdentifier", acct.getAccountIdentifier());
			jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
			jsonObject.put("message", "Customer account details found");					
								
			

			log.info(jsonObject.toString());
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "General System error");
				e.printStackTrace();
				log.info("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	

	
	@GET
	@Path("/listCards")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCards(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") Long customerId, 
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			log.info("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			log.info("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			
			List<Map<String, Object>> customerAccountList = null;
			String hql = "Select tp.*, iss.issuerName, iss.issuerCode, b.bankName, c.verificationNumber, c.firstName, c.lastName, c.otherName, concat(c.firstName, ' ', c.lastName, ' ', c.otherName) as customerName"
					+ " from accounts tp, issuers iss, banks b, customers c where "
					+ "tp.issuer_id = iss.id AND iss.bank_id = b.id AND tp.customer_id = c.id ";
			
			
			int and_ = 0;
			Map<String, Object> customer = null;
			if(status!=null)
			{
				hql = hql + " AND tp.status = '"+(AccountStatus.valueOf(status).ordinal() + 1)+"'";
				and_ = 1;
			}
			if(customerId!=null)
			{
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " AND";
				}
				hql = hql + " tp.customer_id = " + customerId;
				
				String hqlDevice = "Select tp.* from customers tp where tp.id = " + customerId;
				List<Map<String, Object>> customerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hqlDevice);
				customer = customerList.get(0);
				jsonObject.put("customer", (customer));
			}

			log.info(hql);
			customerAccountList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Accounts list fetched successfully");
			jsonObject.put("customeracctlist", customerAccountList);
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@GET
	@Path("/listCustomerAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCustomerAccounts(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("customerId") Long customerId, 
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			log.info("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			log.info("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			
			List<Map<String, Object>> customerAccountList = null;
			String hql = "Select tp.*, iss.issuerName, iss.issuerCode, b.bankName, c.verificationNumber, c.firstName, c.lastName, c.otherName, concat(c.firstName, ' ', c.lastName, ' ', c.otherName) as customerName"
					+ " from accounts tp, issuers iss, banks b, customers c where "
					+ "tp.issuer_id = iss.id AND iss.bank_id = b.id AND tp.customer_id = c.id ";
			
			
			int and_ = 0;
			Map<String, Object> customer = null;
			if(status!=null)
			{
				hql = hql + " AND tp.status = '"+(AccountStatus.valueOf(status).ordinal() + 1)+"'";
				and_ = 1;
			}
			if(customerId!=null)
			{
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " AND";
				}
				hql = hql + " tp.customer_id = " + customerId;
				
				String hqlDevice = "Select tp.* from customers tp where tp.id = " + customerId;
				List<Map<String, Object>> customerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hqlDevice);
				customer = customerList.get(0);
				jsonObject.put("customer", (customer));
			}

			log.info(hql);
			customerAccountList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Accounts list fetched successfully");
			jsonObject.put("customeracctlist", customerAccountList);
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@GET
	@Path("/listCustomerAccountsByUserId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listCustomerAccountsByUserId(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("userId") Long userId, 
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			log.info("verifyJ ==" + verifyJ.toString());
			String bankCode = verifyJ.getString("issuerBankCode");
			log.info("issuerBankCode ==" + bankCode);
			String branch_code = verifyJ.getString("branchCode");
			log.info("branch_code ==" + branch_code);
			String bankKey = UtilityHelper.getBankKey(bankCode, swpService);
			
			List<Map<String, Object>> customerAccountList = null;
			String hql = "Select tp.*, iss.issuerName, iss.issuerCode, b.bankName, c.verificationNumber, c.firstName, c.lastName, c.otherName, concat(c.firstName, ' ', c.lastName, ' ', c.otherName) as customerName"
					+ " from accounts tp, issuers iss, banks b, customers c where "
					+ "tp.issuer_id = iss.id AND iss.bank_id = b.id AND tp.customer_id = c.id ";
			
			
			int and_ = 0;
			Map<String, Object> customer = null;
			if(status!=null)
			{
				hql = hql + " AND tp.status = '"+(AccountStatus.valueOf(status).ordinal() + 1)+"'";
				and_ = 1;
			}
			if(userId!=null)
			{
				String hqlDevice = "Select tp.* from customers tp where tp.user_id = " + userId;
				List<Map<String, Object>> customerList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hqlDevice);
				customer = customerList.get(0);
				jsonObject.put("customer", (customer));
				
				if(and_==1)
				{
					hql = hql + " AND";
				}else
				{
					hql = hql + " AND";
				}
				hql = hql + " tp.customer_id = " + (BigInteger)customer.get("id");
				
			}

			log.info(hql);
			customerAccountList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.CUSTOMER_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Customer Accounts list fetched successfully");
			jsonObject.put("customeracctlist", customerAccountList);
			return Response.status(200).entity(jsonObject.toString()).build();
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
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/getCustomerDashboardStatistics")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerDashboardStatistics(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("deviceCode") String deviceCode)
	{
		List<Map<String, Object>> customers = null;
		Map<String, Object> customer = null;
		List<Map<String, Object>> accounts = null;
		List<Map<String, Object>> cards = null;
		List<Map<String, Object>> transactions = null;
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Customer statistics not pulled");
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
			log.info("verifyJ ==" + verifyJ.toString());
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			String subject = verifyJ.getString("subject");
			log.info("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			
			
			Device device = null;
			
			if(merchantId!=null && deviceCode!=null)
			{
				String hql = "Select tp from Device tp where tp.merchant.merchantCode = '" + merchantId + "' " +
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
			
			
			String hql = "Select tp.* from customers tp where tp.user_id = " + tokenUser.getId();
			customers = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			customer = customers.get(0);
			
			hql = "Select tp.* from accounts tp where "
					+ "tp.customer_id = " + (BigInteger)customer.get("id") + 
					" AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			log.info(hql);
			accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			hql = "Select tp.*, cs.currency from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND "
					+ "tp.customerId = " + (BigInteger)customer.get("id") + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
					+ " AND (tp.deleted_at IS NULL)" + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			log.info(hql);
			cards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			Iterator<Map<String, Object>> cit = cards.iterator();
			String strList = "";
			while(cit.hasNext())
			{
				Map<String, Object> mps = cit.next();
				BigInteger id_ = (BigInteger)mps.get("id");
				strList = strList + (id_.longValue()) + ",";
			}
			
			//hql = "Select tp.* from transactions tp where "
			
			if(strList.length()>0)
			{
				strList = strList.substring(0, strList.length()-1);
				//		+ "tp.customerId = " + (BigInteger)customer.get("id") + " AND tp.serviceType IN (" + ServiceType.CREDIT_CARD.ordinal() + ") AND tp.card_id IS NOT NULL ORDER BY ID DESC LIMIT 0, 10 " ;
				hql = "Select tp.* from transactions tp where tp.card_id IN (" + (strList) +  ") AND tp.deleted_at IS NULL "
						+ "AND tp.isLive = " + device.getSwitchToLive() + " order by tp.created_at DESC"; 
				log.info(">>>>" + hql);
				transactions = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			}
			else
			{
				transactions = new ArrayList<Map<String, Object>>();
			}
			
			jsonObject.put("accounts", accounts);
			jsonObject.put("cards", cards);
			jsonObject.put("transactions", transactions);
			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Customer statistics pulled");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			e.printStackTrace();
		}
		return Response.status(200).entity(jsonObject.toString()).build();
		
		
	}

	
}
