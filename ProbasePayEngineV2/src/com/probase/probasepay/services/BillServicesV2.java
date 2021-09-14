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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
import com.probase.probasepay.models.MerchantPayment;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.RequestTransactionReversal;
import com.probase.probasepay.models.SMSMesage;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.models.UtilityPurchased;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/BillServicesV2")
public class BillServicesV2 {
	private static Logger log = Logger.getLogger(BillServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	

	@GET
	@Path("/listBanks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listBanks(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("staff_bank_code ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			Integer bankId = null;
			Bank bank = null;
			String hql = "Select tp.id, tp.bankName, tp.bankCode, tp.onlineBankingURL from banks tp";
			List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Banks fetch succcessful");
			jsonObject.put("bankList", (bankList));
			System.out.println("Add New Card = " + jsonObject.toString());
		}
		catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	
	@POST
	@Path("/validateAirtime")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateAirtime(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("telcoProvider") String telcoProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("debitSourceType") String debitSourceType, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("token") String token)
	{

		System.out.println("deviceCode==" + deviceCode);
		System.out.println("merchantId==" + merchantId);
		System.out.println(telcoProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(telcoProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			String receipientFirstChar = receipient.substring(0, 1);
			System.out.println(receipientFirstChar);
			if(receipientFirstChar.equals("+"))
			{
				receipient = receipient.substring(1);
			}
			
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode +"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(debitSourceType.equals("CARD"))
			{
				
				hql = "Select tp.*, cs.minimumBalance from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
						+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					
					if((amount+cardMinimumBalance)<=balance)
					{
						String authKey = Application.BILLS_AUTH_KEY;
						String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
						String airtimeResponseString = UtilityHelper.validateAirtime(telcoProvider, receipient, amount, authKey, externalRef);
						if(airtimeResponseString!=null)
						{
							JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
							JSONObject data = airtimeResponseJS.getJSONObject("data");
							JSONObject validation = data.getJSONObject("Validation");
							JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
							if(validation!=null && errors==null)
							{
								Double amountPurchased = validation.getDouble("amount");
								JSONObject validationResponse = validation.getJSONObject("response");
								String validationServiceType = validation.getString("service_type");
								String validationToken = validation.getString("token");
								String operation = validationResponse.getString("operation");
								Integer operationStatus = validationResponse.getInt("status");
								
								if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
								{
									hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
									List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
									Map<String, Object> customerMobile = customerMobiles.get(0);
									Object obj = customerMobile.get("contactMobile");
									Object obj1 = customerMobile.get("contactEmail");
									String mobileNo = (String)obj;
									String userEmail = (String)obj1;
									
									System.out.println("mobileNo..." + mobileNo);
									System.out.println("userEmail..." + userEmail);
									System.out.println("hql..." + ((BigInteger)ecard.get("customerId")));
									
									JSONObject js_ = new JSONObject();
									
									
									String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
									if(otpGenerated!=null && otpGenerated[0].equals("1"))
									{
										jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
										jsonObject.put("otpRef", otpGenerated[3]);
										jsonObject.put("validationToken", validationToken);
										jsonObject.put("message", "Mobile number validated successful. Please enter the One-Time Password sent to your mobile number");
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
									}
									
									//PaymentServicesV2 paymentServices = new PaymentServicesV2();
									//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
									
								}
								else
								{
									JSONObject provider = validationResponse.getJSONObject("provider");
									String errorMessage = provider.getString("error");
									jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
									jsonObject.put("message", "Mobile number could not be validated. " + errorMessage);
								}
							}
							else
							{
								JSONObject error = errors.getJSONObject(0);
								String errorMessage = error.getString("message");
								jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
								jsonObject.put("message", errorMessage);
							}
						}
						else
						{
							jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
							jsonObject.put("message", "Mobile number could not be validated. Please provide a valid mobile number");
						}
						
						
					}
					else
					{
						jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
						jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase airtime. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			else if(debitSourceType.equals("WALLET"))
			{
				
				hql = "Select cs.minimumBalance, tp.* from accounts tp, cardschemes cs where tp.accountScheme_id = cs.id AND (tp.accountIdentifier = '" + accountIdentifier + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(accounts!=null && accounts.size()>0)
				{
					
					Map<String, Object> account = accounts.get(0);
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr==null)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
								Double balance = availablebalance;
								Double walletMinimumBalance = (Double)account.get("minimumBalance");
								
								if((amount+walletMinimumBalance)<=balance)
								{
									String authKey = Application.BILLS_AUTH_KEY;
									String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
									String airtimeResponseString = UtilityHelper.validateAirtime(telcoProvider, receipient, amount, authKey, externalRef);
									if(airtimeResponseString!=null)
									{
										JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
										JSONObject data = airtimeResponseJS.getJSONObject("data");
										JSONObject validation = data.getJSONObject("Validation");
										JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
										if(validation!=null && errors==null)
										{
											Double amountPurchased = validation.getDouble("amount");
											JSONObject validationResponse = validation.getJSONObject("response");
											String validationServiceType = validation.getString("service_type");
											String validationToken = validation.getString("token");
											String operation = validationResponse.getString("operation");
											Integer operationStatus = validationResponse.getInt("status");
											
											if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
											{
												hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)account.get("customer_id"));
												List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
												Map<String, Object> customerMobile = customerMobiles.get(0);
												Object obj = customerMobile.get("contactMobile");
												Object obj1 = customerMobile.get("contactEmail");
												String mobileNo = (String)obj;
												String userEmail = (String)obj1;
												
												System.out.println("mobileNo..." + mobileNo);
												System.out.println("userEmail..." + userEmail);
												System.out.println("hql..." + ((BigInteger)account.get("customer_id")));
												
												JSONObject js_ = new JSONObject();
												String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
												if(otpGenerated!=null && otpGenerated[0].equals("1"))
												{
													jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
													jsonObject.put("otpRef", otpGenerated[3]);
													jsonObject.put("validationToken", validationToken);
													jsonObject.put("message", "Mobile number validated successful. Please enter the One-Time Password sent to your mobile number");
												}
												else
												{
													jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
													jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
												}
												
												//PaymentServicesV2 paymentServices = new PaymentServicesV2();
												//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
												
											}
											else
											{
												JSONObject provider = validationResponse.getJSONObject("provider");
												String errorMessage = provider.getString("error");
												jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
												jsonObject.put("message", "Mobile number could not be validated. " + errorMessage);
											}
										}
										else
										{
											JSONObject error = errors.getJSONObject(0);
											String errorMessage = error.getString("message");
											jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
											jsonObject.put("message", errorMessage);
										}
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "Airtime can not be purchased at the moment. Please try again later.");
									}
									
									
								}
								else
								{
									jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
									jsonObject.put("message", "Insufficient funds. You do not have enough funds in your wallet to purchase airtime because you have reached your spending limit. The least amount allowed on your card is " + walletMinimumBalance);
								}
							
						}
					}
					
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.BANK_CREATE_FAIL);
				jsonObject.put("message", "Validation failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/validateElectricity")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateElectricity(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("vendorProvider") String vendorProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("debitSourceType") String debitSourceType, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("token") String token)
	{
		System.out.println(vendorProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(vendorProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			

			hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode +"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			if(debitSourceType.equals("CARD"))
			{
			
				hql = "Select tp.*, cs.minimumBalance from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
						+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				
				
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					
					if((amount+cardMinimumBalance)<=balance)
					{
						String authKey = Application.BILLS_AUTH_KEY;
						String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
						String airtimeResponseString = UtilityHelper.validateElecticity(vendorProvider, receipient, amount, authKey, externalRef);
						if(airtimeResponseString!=null)
						{
							JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
							JSONObject data = airtimeResponseJS.getJSONObject("data");
							JSONObject validation = data.getJSONObject("Validation");
							JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
							if(validation!=null && errors==null)
							{
								Double amountPurchased = validation.getDouble("amount");
								JSONObject validationResponse = validation.getJSONObject("response");
								String validationServiceType = validation.getString("service_type");
								String validationToken = validation.getString("token");
								String operation = validationResponse.getString("operation");
								Integer operationStatus = validationResponse.getInt("status");
								
								if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
								{
									JSONObject provider = validationResponse.getJSONObject("provider");
									hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
									List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
									Map<String, Object> customerMobile = customerMobiles.get(0);
									Object obj = customerMobile.get("contactMobile");
									Object obj1 = customerMobile.get("contactEmail");
									String mobileNo = (String)obj;
									String userEmail = (String)obj1;
									
									System.out.println("mobileNo..." + mobileNo);
									System.out.println("userEmail..." + userEmail);
									System.out.println("hql..." + ((BigInteger)ecard.get("customerId")));
									
									JSONObject js_ = new JSONObject();
									String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
									if(otpGenerated!=null && otpGenerated[0].equals("1"))
									{
										jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
										jsonObject.put("otpRef", otpGenerated[3]);
										jsonObject.put("validationToken", validationToken);
										jsonObject.put("customerInfo", provider);
										jsonObject.put("message", "Electricity meter number validated successful. Please enter the One-Time Password sent to your mobile number");
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
									}
									
									//PaymentServicesV2 paymentServices = new PaymentServicesV2();
									//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
									
								}
								else
								{
									JSONObject provider = validationResponse.getJSONObject("provider");
									String errorMessage = provider.getString("error");
									jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
									jsonObject.put("message", "Meter number could not be validated. " + errorMessage);
								}
							}
							else
							{
								JSONObject error = errors.getJSONObject(0);
								String errorMessage = error.getString("message");
								jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
								jsonObject.put("message", errorMessage);
							}
						}
						else
						{
							jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
							jsonObject.put("message", "Meter number could not be validated. Please provide a valid meter number");
						}
						
						
					}
					else
					{
						jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
						jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase electricity units. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			else if(debitSourceType.equals("WALLET"))
			{
				
				hql = "Select cs.minimumBalance, tp.* from accounts tp, cardschemes cs where tp.accountScheme_id = cs.id AND (tp.accountIdentifier = '" + accountIdentifier + "') "
						+ "AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(accounts!=null && accounts.size()>0)
				{
					
					Map<String, Object> account = accounts.get(0);
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr==null)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
								Double balance = availablebalance;
								Double walletMinimumBalance = (Double)account.get("minimumBalance");
								
								if((amount+walletMinimumBalance)<=balance)
								{
									String authKey = Application.BILLS_AUTH_KEY;
									String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
									String airtimeResponseString = UtilityHelper.validateElecticity(vendorProvider, receipient, amount, authKey, externalRef);
									if(airtimeResponseString!=null)
									{
										JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
										JSONObject data = airtimeResponseJS.getJSONObject("data");
										JSONObject validation = data.getJSONObject("Validation");
										JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
										if(validation!=null && errors==null)
										{
											Double amountPurchased = validation.getDouble("amount");
											JSONObject validationResponse = validation.getJSONObject("response");
											String validationServiceType = validation.getString("service_type");
											String validationToken = validation.getString("token");
											String operation = validationResponse.getString("operation");
											Integer operationStatus = validationResponse.getInt("status");
											
											if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
											{
												JSONObject provider = validationResponse.getJSONObject("provider");
												hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)account.get("customer_id"));
												List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
												Map<String, Object> customerMobile = customerMobiles.get(0);
												Object obj = customerMobile.get("contactMobile");
												Object obj1 = customerMobile.get("contactEmail");
												String mobileNo = (String)obj;
												String userEmail = (String)obj1;
												
												System.out.println("mobileNo..." + mobileNo);
												System.out.println("userEmail..." + userEmail);
												System.out.println("hql..." + ((BigInteger)account.get("customer_id")));
												
												JSONObject js_ = new JSONObject();
												String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
												if(otpGenerated!=null && otpGenerated[0].equals("1"))
												{
													jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
													jsonObject.put("otpRef", otpGenerated[3]);
													jsonObject.put("validationToken", validationToken);
													jsonObject.put("customerInfo", provider);
													jsonObject.put("message", "Electricity meter number validated successful. Please enter the One-Time Password sent to your mobile number");
												}
												else
												{
													jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
													jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
												}
												
												//PaymentServicesV2 paymentServices = new PaymentServicesV2();
												//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
												
											}
											else
											{
												JSONObject provider = validationResponse.getJSONObject("provider");
												String errorMessage = provider.getString("error");
												jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
												jsonObject.put("message", "Meter number could not be validated. " + errorMessage);
											}
										}
										else
										{
											JSONObject error = errors.getJSONObject(0);
											String errorMessage = error.getString("message");
											jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
											jsonObject.put("message", errorMessage);
										}
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "Meter number could not be validated. Please provide a valid meter number");
									}
									
									
								}
								else
								{
									jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
									jsonObject.put("message", "Insufficient funds. You do not have enough funds in your wallet to purchase electricity units because you have reached your spending limit. The least amount allowed on your card is " + walletMinimumBalance);
								}
							
						}
					}
					
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General System error. Please try again later");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	
	@POST
	@Path("/validateMerchant")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateMerchant(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("recipientDeviceCode") String recipientDeviceCode, 
			@FormParam("paymentReferenceNumber") String paymentReferenceNumber, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("debitSourceType") String debitSourceType, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("token") String token)
	{
		System.out.println(recipientDeviceCode);
		System.out.println(paymentReferenceNumber);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		System.out.println(accountIdentifier);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(recipientDeviceCode!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			

			hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode +"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			
			if(paymentReferenceNumber!=null)
			{
				
				JSONObject merchantValidate = UtilityHelper.validateMerchant(recipientDeviceCode, paymentReferenceNumber, swpService);
				
				if(merchantValidate!=null)
				{
					Merchant merchant = (Merchant)merchantValidate.get("merchant");
					MerchantPayment merchantPayment = null;
					if(merchantValidate.has("merchantPayment") && merchantValidate.get("merchantPayment")!=null)
					{
						amount = merchantPayment.getAmount();
						merchantPayment = (MerchantPayment)merchantValidate.get("merchantPayment");
						jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
						
						JSONObject customerInfo = new JSONObject();
							customerInfo.put("merchantCode", merchant.getMerchantCode());
							customerInfo.put("merchantPaymentRef", merchantPayment.getTransactionRef());
							customerInfo.put("merchantName", merchant.getMerchantName());
							customerInfo.put("merchantAddress", merchant.getAddressLine1());
							customerInfo.put("merchantCity", merchant.getCity());
							customerInfo.put("amountToPay", amount);
							customerInfo.put("message", "Merchant payment details found");
						jsonObject.put("customerInfo", customerInfo);
						
						if(debitSourceType.equals("WALLET"))
						{
							hql = "Select cs.minimumBalance, tp.*, c.contactMobile, c.contactEmail from accounts tp, cardschemes cs, customers c where tp.accountScheme_id = cs.id AND "
									+ "tp.customer_id = c.id AND (tp.accountIdentifier = '" + accountIdentifier + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
							List<Map<String, Object>> accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
							if(accounts!=null && accounts.size()>0)
							{
								
								Map<String, Object> account = accounts.get(0);
								
								AccountServicesV2 as_ = new AccountServicesV2();
								Response balanceDetResp = as_.getAccountBalance(
										httpHeaders,
										requestContext,
										accountIdentifier, 
										token, 
										merchantId, 
										deviceCode);
								String balanceDetStr = (String)balanceDetResp.getEntity();
								System.out.println("balanceDet1.....");
								System.out.println(balanceDetStr);
								if(balanceDetStr==null)
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
										Double balance = availablebalance;
										Double walletMinimumBalance = (Double)account.get("minimumBalance");
										String mobileNo = (String)account.get("contactMobile");
										String userEmail = (String)account.get("contactEmail");
										
										if((merchantPayment.getAmount()+walletMinimumBalance)<=balance)
										{
											JSONObject js_ = new JSONObject();
											String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, swpService);
											if(otpGenerated!=null && otpGenerated[0].equals("1"))
											{
												hql = "Select tp from Transaction tp where tp.id = " + merchantPayment.getTransactionId() + " AND tp.deleted_at IS NULL "
														+ "AND tp.isLive = " + device.getSwitchToLive();
												Transaction transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
												transaction.setOTPRef(otpGenerated[3]);
												swpService.updateRecord(transaction);
												
												jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
												jsonObject.put("otpRef", otpGenerated[3]);
											}
											else
											{
												jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
												jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
											}
										}
										else
										{
											jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
											jsonObject.put("message", "You do not have enough funds in your wallet to make this payment. Please fund your wallet first");
										}
									}
									else
									{

										jsonObject.put("status", ERROR.GENERAL_FAIL);
										jsonObject.put("message", "We could not get the balance on your wallet at the moment.");
									}
								}
							}
							else
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "You do not seem to have any wallet at the moment");
							}
						}
						else if(debitSourceType.equals("CARD"))
						{
							hql = "Select tp.*, cs.minimumBalance, c.contactMobile, c.contactEmail from ecards tp, cardschemes cs, customers c where tp.cardScheme_id = cs.id AND "
									+ "tp.customerId = c.id AND (tp.serialNo = '" + cardSerialNo + "'" 
									+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at is NULL AND tp.isLive = " + device.getSwitchToLive();
							List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
							
							
							if(ecards!=null && ecards.size()>0)
							{
								Map<String, Object> ecard = ecards.get(0);
								Double balance = (Double)ecard.get("cardBalance");
								Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
								String mobileNo = (String)ecard.get("contactMobile");
								String userEmail = (String)ecard.get("contactEmail");
								
								if((amount+cardMinimumBalance)<=balance)
								{
									String authKey = Application.BILLS_AUTH_KEY;
									String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
									
									JSONObject js_ = new JSONObject();
									String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, swpService);
									if(otpGenerated!=null && otpGenerated[0].equals("1"))
									{
										hql = "Select tp from Transaction tp where tp.id = " + merchantPayment.getTransactionId() + " AND tp.deleted_at IS NULL "
												+ "AND tp.isLive = " + device.getSwitchToLive();
										Transaction transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
										transaction.setOTPRef(otpGenerated[3]);
										swpService.updateRecord(transaction);
										
										jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
										jsonObject.put("otpRef", otpGenerated[3]);
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
									}
								}
								else
								{
									jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
									jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase electricity units. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
								}
							}
							else
							{
								jsonObject.put("status", ERROR.CARD_NOT_VALID);
								jsonObject.put("message", "Invalid card. Card not found");
							}
						}
						
					}
					else
					{
						jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
						jsonObject.put("message", "No payment found matching the payment reference number provided");
					}
					
							
				}
				else
				{
					jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
					jsonObject.put("message", "No payment found matching the payment reference number provided");
				}
			}
			else
			{
				JSONObject merchantValidate = UtilityHelper.validateMerchant(recipientDeviceCode, null, swpService);
				
				if(merchantValidate!=null)
				{
					Merchant merchant = (Merchant)merchantValidate.get("merchant");
					jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
					JSONObject customerInfo = new JSONObject();
						customerInfo.put("merchantCode", merchant.getMerchantCode());
						customerInfo.put("merchantName", merchant.getMerchantName());
						customerInfo.put("merchantAddress", merchant.getAddressLine1());
						customerInfo.put("merchantCity", merchant.getCity());
						customerInfo.put("amountToPay", amount);
						customerInfo.put("message", "Merchant payment details found");
					jsonObject.put("customerInfo", customerInfo);
					
					if(debitSourceType.equals("WALLET"))
					{
						hql = "Select cs.minimumBalance, tp.*, c.contactMobile from accounts tp, cardschemes cs, customers c where tp.accountScheme_id = cs.id AND "
								+ "tp.customer_id = c.id AND (tp.accountIdentifier = '" + accountIdentifier + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
						if(accounts!=null && accounts.size()>0)
						{
							
							Map<String, Object> account = accounts.get(0);

							System.out.println("balanceDet0....");
							System.out.println(merchantId);
							System.out.println(deviceCode);
							AccountServicesV2 as_ = new AccountServicesV2();
							Response balanceDetResp = as_.getAccountBalance(
									httpHeaders,
									requestContext,
									accountIdentifier, 
									token, 
									merchantId, 
									deviceCode);
							String balanceDetStr = (String)balanceDetResp.getEntity();
							System.out.println("balanceDet1.....");
							System.out.println(balanceDetStr);
							if(balanceDetStr==null)
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
									Double balance = availablebalance;
									Double walletMinimumBalance = (Double)account.get("minimumBalance");
									String mobileNo = (String)account.get("contactMobile");
									String userEmail = (String)account.get("contactEmail");
									
									if((amount+walletMinimumBalance)<=balance)
									{
										JSONObject js_ = new JSONObject();
										String[] otpGenerated = null;
										if(acquirer.getHoldFundsYes().equals(Boolean.TRUE))
										{
											otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, swpService);
										}
										else
										{
											hql = "Select tp from Account tp where tp.accountIdentifier = '"+ accountIdentifier +"' AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
											Account acct = (Account)swpService.getUniqueRecordByHQL(hql);
											otpGenerated = UtilityHelper.generateWalletOTP(swpService, js_, acct, null, acct.getCustomer().getContactMobile(), 
													acct.getCustomer().getContactEmail(), acquirer, device);
										}
										if(otpGenerated!=null && otpGenerated[0].equals("1"))
										{
											jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
											jsonObject.put("otpRef", otpGenerated[3]);
										}
										else
										{
											jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
											jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
										}
									}
									else
									{
										jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
										jsonObject.put("message", "You do not have enough funds in your wallet to make this payment. Please fund your wallet first");
									}
								}
								else
								{

									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "We could not get the balance on your wallet at the moment.");
								}
							}
						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "You do not seem to have any wallet at the moment");
						}
					}
					else if(debitSourceType.equals("CARD"))
					{
						hql = "Select tp.*, cs.minimumBalance, c.contactMobile, c.contactEmail from ecards tp, cardschemes cs, customers c where tp.cardScheme_id = cs.id AND "
								+ "tp.customerId = c.id AND (tp.serialNo = '" + cardSerialNo + "'" 
								+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
						List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
						
						
						if(ecards!=null && ecards.size()>0)
						{
							Map<String, Object> ecard = ecards.get(0);
							Double balance = (Double)ecard.get("cardBalance");
							Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
							String mobileNo = (String)ecard.get("contactMobile");
							String userEmail = (String)ecard.get("contactEmail");
							
							if((amount+cardMinimumBalance)<=balance)
							{
								String authKey = Application.BILLS_AUTH_KEY;
								String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
								
								JSONObject js_ = new JSONObject();
								String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
								if(otpGenerated!=null && otpGenerated[0].equals("1"))
								{
									jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
									jsonObject.put("otpRef", otpGenerated[3]);
								}
								else
								{
									jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
									jsonObject.put("message", "We could not send you an OTP to confirm your purchase. Please try again");
								}
							}
							else
							{
								jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
								jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase electricity units. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
							}
						}
						else
						{
							jsonObject.put("status", ERROR.CARD_NOT_VALID);
							jsonObject.put("message", "Invalid card. Card not found");
						}
					}
							
				}
				else
				{
					jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
					jsonObject.put("message", "No payment found matching the payment reference number provided");
				}
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			if(debitSourceType.equals("CARD"))
			{
			
				hql = "Select tp.*, cs.minimumBalance from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
						+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				
				
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					
					if((amount+cardMinimumBalance)<=balance)
					{
						String authKey = Application.BILLS_AUTH_KEY;
						String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
						
						
						
						
						
						
						
					}
					else
					{
						jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
						jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase electricity units. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			
			
			
			
			
			
			
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General System error. Please try again later");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	

	@POST
	@Path("/payMerchant")
	@Produces(MediaType.APPLICATION_JSON)
	public Response payMerchant(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("debitSourceType") String debitSourceType, 
			@FormParam("recipientDeviceCode") String recipientDeviceCode, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("amount") Double amount, 
			@FormParam("paymentReferenceNumber") String paymentReferenceNumber, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("channel") String channel,
			@FormParam("otpRef") String otpRef, 
			@FormParam("otp") String otp,
			@FormParam("narration") String narration,
			@FormParam("longNarration") String longNarration,
			@FormParam("hash") String hash,
			@FormParam("orderRef") String orderRef,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("token") String token)
	{
		System.out.println(narration==null ? "narration is null" : narration);
		System.out.println(longNarration==null ? "longNarration is null" : longNarration);
		System.out.println(recipientDeviceCode);
		System.out.println(paymentReferenceNumber);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		System.out.println(accountIdentifier);
		System.out.println(debitSourceType);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				
				return Response.status(200).entity(jsonObject.toString()).build();
				
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}

			
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
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
			
			

			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			if(recipientDeviceCode!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			
			if(paymentReferenceNumber!=null)
			{
				
				JSONObject merchantValidate = UtilityHelper.validateMerchant(recipientDeviceCode, paymentReferenceNumber, swpService);
				
				if(merchantValidate!=null)
				{
					Merchant merchant = (Merchant)merchantValidate.get("merchant");
					Device device = (Device)merchantValidate.get("device");
					
					

					
					
					
					
					MerchantPayment merchantPayment = null;
					if(merchantValidate.has("merchantPayment") && merchantValidate.get("merchantPayment")!=null)
					{
						amount = null;
						merchantPayment = (MerchantPayment)merchantValidate.get("merchantPayment");
						amount = merchantPayment.getAmount();
						jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
						jsonObject.put("merchantCode", merchant.getMerchantCode());
						jsonObject.put("merchantPaymentRef", merchantPayment.getTransactionRef());
						jsonObject.put("merchantName", merchant.getMerchantName());
						jsonObject.put("merchantAddress", merchant.getAddressLine1());
						jsonObject.put("merchantCity", merchant.getCity());
						jsonObject.put("amountToPay", amount);
						jsonObject.put("message", "Merchant payment details found");
						
						
						if(!orderRef.equals(merchantPayment.getOrderRef()))
						{

							jsonObject.put("status", ERROR.ORDER_ID_NOT_PROVIDED);
							jsonObject.put("message", "Order reference mismatch");
							return Response.status(200).entity(jsonObject.toString()).build();
						}
						
						if(debitSourceType.equals("WALLET"))
						{
							hql = "Select tp from Account tp where (tp.accountIdentifier = '" + accountIdentifier + "') AND tp.deleted_at IS NULL AND "
									+ " tp.isLive = " + deviceTrafficSource.getSwitchToLive();
							Account account = (Account)swpService.getUniqueRecordByHQL(hql);
							if(account!=null)
							{
								
								hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + 
										" AND tp.deleted_at IS NULL AND " + " tp.isLive = " + deviceTrafficSource.getSwitchToLive();
								List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
								Boolean debitAccountTrue = true;
								Boolean debitCardTrue = false;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								Account recipientAccount = null;
								if(devicebankaccounts!=null && devicebankaccounts.size()>0)
								{
									Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
									creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
									debitAccountId = account.getId();
									recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
								}
								
								if(recipientAccount==null)
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Merchant payment was not successful. Merchants account not activated");
									return Response.status(200).entity(jsonObject.toString()).build();
								}
								
								AccountServicesV2 as_ = new AccountServicesV2();
								Response balanceDetResp = as_.getAccountBalance(
										httpHeaders,
										requestContext,
										accountIdentifier, 
										token, 
										merchantId, 
										deviceCode);
								String balanceDetStr = (String)balanceDetResp.getEntity();
								System.out.println("balanceDet1.....");
								System.out.println(balanceDetStr);
								if(balanceDetStr==null)
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
										Double balance = availablebalance;
										Double walletMinimumBalance = account.getAccountScheme().getMinimumBalance();
										String mobileNo = account.getCustomer().getContactMobile();
										String userEmail = account.getCustomer().getContactEmail();
										
										if((merchantPayment.getAmount()+walletMinimumBalance)<=balance)
										{
											if(acquirer.getHoldFundsYes().equals(Boolean.FALSE))
											{
												String zauthKey = null;
												if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
													zauthKey = acquirer.getAuthKey();
												else
													zauthKey = acquirer.getDemoAuthKey();
													
												
												
												/*String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
												if(!(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1")))
												{
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
													return Response.status(200).entity(jsonObject.toString()).build();
												}*/
												
												if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
												{
													PaymentServicesV2 paymentServices = new PaymentServicesV2();
													String accountData = UtilityHelper.encryptData(account.getAccountIdentifier(), bankKey);
													
													System.out.println("DEBIT SOURCE WALLET");
													System.out.println("===================");
													hql = "Select tp from Transaction tp where tp.id = " + merchantPayment.getTransactionId() + " AND tp.deleted_at IS NULL "
															+ "AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
													Transaction transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
													
													Double charges = 0.00;
													account.setAccountBalance(account.getAccountBalance() - amount - charges);
													swpService.updateRecord(account);
													String bankReferenceNo = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
													
													JSONObject transferInfo = new JSONObject();
													transferInfo.put("sourceIdentifier", account.getAccountIdentifier());
													transferInfo.put("sourceCustomerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
													transferInfo.put("newSourceAvailableBalance", account.getAccountBalance());
													transferInfo.put("newSourceCurrentBalance", account.getAccountBalance());
													transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
													transferInfo.put("sourceCharges", charges);
													transferInfo.put("transactionRefNo", transaction.getTransactionRef());
													
															
															
															
													jsonObject.put("status", ERROR.GENERAL_OK);
													jsonObject.put("message", "Payment was successful");
													jsonObject.put("orderRef", orderRef);
													jsonObject.put("amountTransferred", amount);
													jsonObject.put("receipientInfo", transferInfo.toString());
													

													System.out.println("CREDIT RECIPIENT WALLET1");
													System.out.println("=======================");
													
													
													transaction.setDebitAccountTrue(debitAccountTrue);
													transaction.setDebitCardTrue(debitCardTrue);
													transaction.setCreditAccountId(creditAccountId);
													transaction.setCreditCardId(creditCardId);
													transaction.setDebitAccountId(debitAccountId);
													transaction.setDebitCardId(debitCardId);
													transaction.setTransactionCode(TransactionCode.transactionSuccess);
													transaction.setResponseCode("00");
													transaction.setStatus(TransactionStatus.SUCCESS);
													transaction.setDetails("Pay Merchant - " + merchantPayment.getMerchant_name());
													transaction.setFixedCharge(charges);
													transaction.setIsLive(deviceTrafficSource.getSwitchToLive());
													swpService.updateRecord(transaction);
													
													merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
													swpService.updateRecord(merchantPayment);
													
													


													JSONObject jsbreakdown = new JSONObject();
													jsbreakdown.put("Sub-total", amount);
													jsbreakdown.put("Charges", (charges));
													
													transaction.setStatus(TransactionStatus.SUCCESS);
													JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), account.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
															transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
													
													
													if(narration!=null)
														txnDetails.put("narration", narration);
													if(longNarration!=null)
														txnDetails.put("extraInformation", longNarration);
													
													
													System.out.println("narration str...");
													System.out.println(narration.toString());
													transaction.setSummary(txnDetails.toString());
													swpService.updateRecord(transaction);
													
													
													
													
													String receipentMobileNumber = account.getCustomer().getContactMobile();
													String smsMessage = "Hello\nYour payment of "+ (account.getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
													//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
													//swpService.createNewRecord(smsMsg);


													SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
													new Thread(smsSender).start();
													
													return Response.status(200).entity(jsonObject.toString()).build();
												}
												else
												{
													jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
													jsonObject.put("message", "You cannot make this payment due to insufficient funds in your wallet. The minimum amount allowed is " + account.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
												}
											}
											else
											{
												String zauthKey = null;
												if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
													zauthKey = acquirer.getAuthKey();
												else
													zauthKey = acquirer.getDemoAuthKey();
													
												
												
												/*String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
												if(!(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1")))
												{
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
													return Response.status(200).entity(jsonObject.toString()).build();
												}*/

												Double charges = 0.00;
												if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
												{
													PaymentServicesV2 paymentServices = new PaymentServicesV2();
													String accountData = UtilityHelper.encryptData(account.getAccountIdentifier(), bankKey);
													
													System.out.println("DEBIT SOURCE WALLET");
													System.out.println("===================");
													//Transaction transaction = (Transaction)swpService.getRecordById(Transaction.class, merchantPayment.getTransactionId());
													hql = "Select tp from Transaction tp where tp.id = " + merchantPayment.getTransactionId() + " AND tp.deleted_at IS NULL "
															+ "AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
													Transaction transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
													Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, hash, "", transaction, orderRef, serviceTypeId, token);
													String drResStr = (String)drRes.getEntity();
													System.out.println("DebitResponse");
													System.out.println(drResStr);
													if(drResStr!=null)
													{
														JSONObject allResponse = new JSONObject();
														allResponse.put("fundsTransferDebitResponse", drResStr);
														JSONObject debitResponse = new JSONObject(drResStr);
														if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
														{
	
															String bankReferenceNo = debitResponse.getString("bankReferenceNo");
															transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
															transaction.setResponseCode("03");
															transaction.setMessageResponse(allResponse.toString());
															transaction.setDebitBankPaymentReference(bankReferenceNo);
															transaction.setFixedCharge(charges);
															swpService.updateRecord(transaction);
															
															
															
															JSONObject transferInfo = new JSONObject();
															transferInfo.put("sourceIdentifier", account.getAccountIdentifier());
															transferInfo.put("sourceCustomerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
															transferInfo.put("newSourceAvailableBalance", debitResponse.getDouble("availablebalance"));
															transferInfo.put("newSourceCurrentBalance", debitResponse.getDouble("currentbalance"));
															transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
															transferInfo.put("sourceCharges", debitResponse.getDouble("charges"));
															transferInfo.put("transactionRefNo", transaction.getTransactionRef());
															
															
															
															
															jsonObject.put("status", ERROR.GENERAL_OK);
															jsonObject.put("message", "Merchant payment was successful");
															jsonObject.put("orderRef", orderRef);
															jsonObject.put("amountTransferred", amount);
															jsonObject.put("receipientInfo", transferInfo.toString());
															
	
															System.out.println("CREDIT RECIPIENT WALLET2");
															System.out.println("=======================");
															
															
															transaction.setDebitAccountTrue(debitAccountTrue);
															transaction.setDebitCardTrue(debitCardTrue);
															transaction.setCreditAccountId(creditAccountId);
															transaction.setCreditCardId(creditCardId);
															transaction.setDebitAccountId(debitAccountId);
															transaction.setDebitCardId(debitCardId);
															transaction.setTransactionCode(TransactionCode.transactionSuccess);
															transaction.setResponseCode("00");
															transaction.setMessageResponse(allResponse.toString());
															transaction.setStatus(TransactionStatus.SUCCESS);

															transaction.setDetails("Pay Merchant - " + merchantPayment.getMerchant_name());
															transaction.setIsLive(deviceTrafficSource.getSwitchToLive());
															swpService.updateRecord(transaction);
															
															merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
															
															swpService.updateRecord(merchantPayment);
															


															JSONObject jsbreakdown = new JSONObject();
															jsbreakdown.put("Sub-total", amount);
															jsbreakdown.put("Charges", (charges));
															
															JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), account.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
																	transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
															if(narration!=null)
																txnDetails.put("narration", narration);

															if(longNarration!=null)
																txnDetails.put("extraInformation", longNarration);
															
															System.out.println("narration str...");
															System.out.println(narration.toString());
															transaction.setSummary(txnDetails.toString());
															swpService.updateRecord(transaction);
															
															
															
															
															String receipentMobileNumber = account.getCustomer().getContactMobile();
															String smsMessage = "Hello\nYour payment of "+ (account.getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
															//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
															//swpService.createNewRecord(smsMsg);

															SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
															new Thread(smsSender).start();
	
														}
													}
													return Response.status(200).entity(jsonObject.toString()).build();
												}
												else
												{
													jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
													jsonObject.put("message", "You cannot make this payment due to insufficient funds in your wallet. The minimum amount allowed is " + account.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
												}
											}
										}
										else
										{
											jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
											jsonObject.put("message", "You do not have enough funds in your wallet to make this payment. Please fund your wallet first");
										}
									}
									else
									{

										jsonObject.put("status", ERROR.GENERAL_FAIL);
										jsonObject.put("message", "We could not get the balance on your wallet at the moment.");
									}
								}
							}
							else
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "You do not seem to have any wallet at the moment");
							}
						}
						else if(debitSourceType.equals("CARD"))
						{
							accountIdentifier = null;
							hql = "Select tp.*, cs.minimumBalance, c.contactMobile, c.contactEmail, c.firstName, c.lastName, acc.accountIdentifier from ecards tp, cardschemes cs, customers c, accounts acc where tp.cardScheme_id = cs.id AND "
									+ "tp.customerId = c.id AND tp.account_id = acc.id AND (tp.serialNo = '" + cardSerialNo + "'" 
									+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
							List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
							
							
							if(ecards!=null && ecards.size()>0)
							{
								Map<String, Object> ecard = ecards.get(0);
								Double balance = (Double)ecard.get("cardBalance");
								Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
								String mobileNo = (String)ecard.get("contactMobile");
								String userEmail = (String)ecard.get("contactEmail");
								String accountIdentifier_ = (String)ecard.get("accountIdentifier");
								Long ecardId = ((BigInteger)ecard.get("id")).longValue();
								ECard card = (ECard)swpService.getRecordById(ECard.class, ecardId);
								
								if((amount+cardMinimumBalance)<=balance)
								{
									String zauthKey = null;
									if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
										zauthKey = acquirer.getAuthKey();
									else
										zauthKey = acquirer.getDemoAuthKey();
										
									
									
									/*String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
									if(!(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1")))
									{
										jsonObject.put("status", ERROR.GENERAL_FAIL);
										jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
										return Response.status(200).entity(jsonObject.toString()).build();
									}*/
									Double charges = 0.00;
									
									if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
									{
										PaymentServicesV2 paymentServices = new PaymentServicesV2();
										String accountData = UtilityHelper.encryptData(accountIdentifier_, bankKey);
										
										System.out.println("DEBIT SOURCE WALLET");
										System.out.println("===================");
										//Transaction transaction = (Transaction)swpService.getRecordById(Transaction.class, merchantPayment.getTransactionId());
										hql = "Select tp from Transaction tp where tp.id = " + merchantPayment.getTransactionId() + " AND tp.deleted_at IS NULL "
												+ "AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
										Transaction transaction = (Transaction)swpService.getUniqueRecordByHQL(hql);
										Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, hash, "", transaction, orderRef, serviceTypeId, token);
										String drResStr = (String)drRes.getEntity();
										System.out.println("DebitResponse");
										System.out.println(drResStr);
										if(drResStr!=null)
										{
											JSONObject allResponse = new JSONObject();
											allResponse.put("fundsTransferDebitResponse", drResStr);
											JSONObject debitResponse = new JSONObject(drResStr);
											if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
											{

												
												String bankReferenceNo = debitResponse.getString("bankReferenceNo");
												transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
												transaction.setResponseCode("03");
												transaction.setMessageResponse(allResponse.toString());
												transaction.setDebitBankPaymentReference(bankReferenceNo);
												transaction.setFixedCharge(charges);
												swpService.updateRecord(transaction);
												
												
												card = card.withdraw(swpService, amount, 0.00);


												System.out.println("CREDIT RECIPIENT WALLET3");
												System.out.println("=======================");
												
												hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
												List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
												Boolean debitAccountTrue = true;
												Boolean debitCardTrue = false;
												Long creditAccountId = null;
												Long creditCardId = null;
												Long debitAccountId = null;
												Long debitCardId = null;
												Account recipientAccount = null;
												if(devicebankaccounts!=null && devicebankaccounts.size()>0)
												{
													Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
													creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
													debitCardId = ((BigInteger)ecard.get("id")).longValue();
													recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
												}
												
												if(recipientAccount==null)
												{
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", "Payment was not successful. Merchants account not activated");
													return Response.status(200).entity(jsonObject.toString()).build();
												}
												transaction.setDebitAccountTrue(debitAccountTrue);
												transaction.setDebitCardTrue(debitCardTrue);
												transaction.setCreditAccountId(creditAccountId);
												transaction.setCreditCardId(creditCardId);
												transaction.setDebitAccountId(debitAccountId);
												transaction.setDebitCardId(debitCardId);
												transaction.setTransactionCode(TransactionCode.transactionSuccess);
												transaction.setResponseCode("00");
												transaction.setMessageResponse(allResponse.toString());
												transaction.setStatus(TransactionStatus.SUCCESS);

												transaction.setDetails("Pay Merchant - " + merchantPayment.getMerchant_name());
												transaction.setIsLive(deviceTrafficSource.getSwitchToLive());
												swpService.updateRecord(transaction);
												
												merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
												swpService.updateRecord(merchantPayment);
												
												

												
												JSONObject transferInfo = new JSONObject();
												transferInfo.put("sourceIdentifier", card.getPan().substring(0, 4) + "****" + card.getPan().substring(card.getPan().length()-4));
												transferInfo.put("sourceCustomerName", card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName());
												transferInfo.put("newSourceAccountAvailableBalance", debitResponse.getDouble("availablebalance"));
												transferInfo.put("newSourceAccountCurrentBalance", debitResponse.getDouble("currentbalance"));
												transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
												transferInfo.put("sourceWalletCharges", debitResponse.getDouble("charges"));
												transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
												transferInfo.put("transactionRefNo", transaction.getTransactionRef());
												
												
												
												
												jsonObject.put("status", ERROR.GENERAL_OK);
												jsonObject.put("message", "Payment was successful");
												jsonObject.put("orderRef", orderRef);
												jsonObject.put("amountTransferred", amount);
												jsonObject.put("receipientInfo", transferInfo.toString());
												
												

												


												JSONObject jsbreakdown = new JSONObject();
												jsbreakdown.put("Sub-total", amount);
												jsbreakdown.put("Charges", (charges));
												
												JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), card.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
														transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);


												if(narration!=null)
													txnDetails.put("narration", narration);


												if(longNarration!=null)
													txnDetails.put("extraInformation", longNarration);
												
												System.out.println("narration str...");
												System.out.println(narration.toString());
												transaction.setSummary(txnDetails.toString());
												swpService.updateRecord(transaction);
												
												
												
												String receipentMobileNumber = card.getAccount().getCustomer().getContactMobile();
												String smsMessage = "Hello\nYour payment of "+ (card.getAccount().getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
												//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
												//swpService.createNewRecord(smsMsg);
												


												SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
												new Thread(smsSender).start();
											}
										}
										return Response.status(200).entity(jsonObject.toString()).build();
									}
									else
									{
										jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
										jsonObject.put("message", "You cannot make this payment due to insufficient funds in your card. The minimum amount allowed is " + card.getAccount().getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
									}
								}
								else
								{
									jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
									jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to make this payment. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
								}
							}
							else
							{
								jsonObject.put("status", ERROR.CARD_NOT_VALID);
								jsonObject.put("message", "Invalid card. Card not found");
							}
						}
						
					}
					else
					{
						jsonObject.put("status", ERROR.TRANSACTION_NOT_FOUND);
						jsonObject.put("message", "No payment found matching the payment reference number provided");
					}
					
							
				}
				else
				{
					jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
					jsonObject.put("message", "No payment found matching the payment reference number provided");
				}
			}
			else
			{
				JSONObject merchantValidate = UtilityHelper.validateMerchant(recipientDeviceCode, paymentReferenceNumber, swpService);
				if(merchantValidate!=null)
				{
					Merchant merchant = (Merchant)merchantValidate.get("merchant");
					Device device = (Device)merchantValidate.get("device");
					
					
					String transactionRef = RandomStringUtils.randomAlphabetic(12).toUpperCase();
					jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
					jsonObject.put("merchantCode", merchant.getMerchantCode());
					jsonObject.put("merchantPaymentRef", transactionRef);
					jsonObject.put("merchantName", merchant.getMerchantName());
					jsonObject.put("merchantAddress", merchant.getAddressLine1());
					jsonObject.put("merchantCity", merchant.getCity());
					jsonObject.put("amountToPay", amount);
					jsonObject.put("message", "Merchant payment details found");
					
					if(debitSourceType.equals("WALLET"))
					{
						hql = "Select tp from Account tp where (tp.accountIdentifier = '" + accountIdentifier + "')";
						Account account = (Account)swpService.getUniqueRecordByHQL(hql);
						if(account!=null)
						{
							
							hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
							List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
							Boolean debitAccountTrue = true;
							Boolean debitCardTrue = false;
							Long creditAccountId = null;
							Long creditCardId = null;
							Long debitAccountId = null;
							Long debitCardId = null;
							Account recipientAccount = null;
							if(devicebankaccounts!=null && devicebankaccounts.size()>0)
							{
								Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
								creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
								debitAccountId = account.getId();
								recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
							}
							
							if(recipientAccount==null)
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "Payment was not successful. Merchant account not activated");
								return Response.status(200).entity(jsonObject.toString()).build();
							}
							
							AccountServicesV2 as_ = new AccountServicesV2();
							Response balanceDetResp = as_.getAccountBalance(
									httpHeaders,
									requestContext,
									accountIdentifier, 
									token, 
									merchantId, 
									deviceCode);
							String balanceDetStr = (String)balanceDetResp.getEntity();
							System.out.println("balanceDet1.....");
							System.out.println(balanceDetStr);
							if(balanceDetStr==null)
							{
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", "We could not get your wallet balance at this moment. Please try again later");
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
									Double balance = availablebalance;
									Double walletMinimumBalance = account.getAccountScheme().getMinimumBalance();
									String mobileNo = account.getCustomer().getContactMobile();
									String userEmail = account.getCustomer().getContactEmail();
									
									if((amount+walletMinimumBalance)<=balance)
									{
										String zauthKey = null;
										if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
											zauthKey = acquirer.getAuthKey();
										else
											zauthKey = acquirer.getDemoAuthKey();
											
										
										
										/*String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
										if(!(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1")))
										{
											jsonObject.put("status", ERROR.GENERAL_FAIL);
											jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
											return Response.status(200).entity(jsonObject.toString()).build();
										}*/
										
										if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
										{
											PaymentServicesV2 paymentServices = new PaymentServicesV2();
											String accountData = UtilityHelper.encryptData(account.getAccountIdentifier(), bankKey);
											
											System.out.println("DEBIT SOURCE WALLETA");
											System.out.println("===================");
											
											String bankPaymentReference = null;
											Long customerId = account.getCustomer().getId();
											Boolean creditAccountTrue = true;
											Boolean creditCardTrue = false;
											String rpin = null;
											Channel ch = Channel.valueOf(channel);
											Date transactionDate = new Date();
											ServiceType serviceType = ServiceType.PAY_MERCHANT;
											String payerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
											String payerEmail = account.getCustomer().getContactEmail();
											String payerMobile = account.getCustomer().getContactMobile();
											TransactionStatus status = TransactionStatus.PENDING;
											ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
											String transactionCode = null;
											Boolean creditPoolAccountTrue = false;
											String messageRequest = null;
											String messageResponse = null;
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											Double schemeTransactionCharge = 0.00;
											Double schemeTransactionPercentage = 0.00;
											String responseCode = null;
											String oTP = null;
											String oTPRef = otpRef;
											String merchantBank = null;
											String merchantAccount = null; 
											Long transactingBankId = acquirer.getBank().getId();
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = account.getId();
											Long receipientEntityId = null;
											Channel receipientChannel = ch;
											String transactionDetail = narration;
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = null;
											String customData = null;
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = null;
											String merchantName = merchant.getMerchantName();
											String merchantCode = merchant.getMerchantCode();
											Long merchantId_ = merchant.getId();
											
											
											
											
											
											
											Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
													customerId, creditAccountTrue, creditCardTrue,
													orderRef, rpin, ch,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probasePayCurrency, transactionCode,
													account, null, device,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													amount, responseCode, oTP, oTPRef,
													merchantId_, merchantName, merchantCode,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													receipientChannel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customData,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
													"Pay Merchant - " + merchant.getMerchantName(), 
													deviceTrafficSource.getSwitchToLive());
											transaction = (Transaction)swpService.createNewRecord(transaction);
											
											
											
											if(acquirer.getHoldFundsYes().equals(Boolean.FALSE))
											{
												System.out.println("getHoldFundsYes....false");
												Double charges = 0.00;
												account.setAccountBalance(account.getAccountBalance() - amount - charges);
												swpService.updateRecord(account);
												String bankReferenceNo = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
												
												JSONObject transferInfo = new JSONObject();
												transferInfo.put("sourceIdentifier", account.getAccountIdentifier());
												transferInfo.put("sourceCustomerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
												transferInfo.put("newSourceAvailableBalance", account.getAccountBalance());
												transferInfo.put("newSourceCurrentBalance", account.getAccountBalance());
												transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
												transferInfo.put("sourceCharges", charges);
												transferInfo.put("transactionRefNo", transaction.getTransactionRef());

												jsonObject.put("status", ERROR.GENERAL_OK);
												jsonObject.put("message", "Payment was successful");
												jsonObject.put("orderRef", orderRef);
												jsonObject.put("amountTransferred", amount);
												jsonObject.put("receipientInfo", transferInfo.toString());	
												
												transaction.setDebitAccountTrue(debitAccountTrue);
												transaction.setDebitCardTrue(debitCardTrue);
												transaction.setCreditAccountId(creditAccountId);
												transaction.setCreditCardId(creditCardId);
												transaction.setDebitAccountId(debitAccountId);
												transaction.setDebitCardId(debitCardId);
												transaction.setTransactionCode(TransactionCode.transactionSuccess);
												transaction.setResponseCode("00");
												transaction.setStatus(TransactionStatus.SUCCESS);
												swpService.updateRecord(transaction);
												
												
												

												


												JSONObject jsbreakdown = new JSONObject();
												jsbreakdown.put("Sub-total", amount);
												jsbreakdown.put("Charges", (charges));
												
												JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), account.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
														transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
												
												if(narration!=null)
													txnDetails.put("narration", narration);


												if(longNarration!=null)
													txnDetails.put("extraInformation", longNarration);
												
												System.out.println("narration str...");
												
												transaction.setSummary(txnDetails.toString());
												swpService.updateRecord(transaction);
												
												
												String receipentMobileNumber = account.getCustomer().getContactMobile();
												String smsMessage = "Hello\nYour payment of "+ (account.getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
												//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
												//swpService.createNewRecord(smsMsg);


												SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
												new Thread(smsSender).start();
												
												return Response.status(200).entity(jsonObject.toString()).build();
											}
											else
											{
												double charges = 0.00;
												Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, hash, "", transaction, orderRef, serviceTypeId, token);
												String drResStr = (String)drRes.getEntity();
												System.out.println("DebitResponse");
												System.out.println(drResStr);
												if(drResStr!=null)
												{
													JSONObject allResponse = new JSONObject();
													allResponse.put("fundsTransferDebitResponse", drResStr);
													JSONObject debitResponse = new JSONObject(drResStr);
													if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
													{
	
														String bankReferenceNo = debitResponse.getString("bankReferenceNo");
														transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
														transaction.setResponseCode("03");
														transaction.setMessageResponse(allResponse.toString());
														transaction.setDebitBankPaymentReference(bankReferenceNo);
														swpService.updateRecord(transaction);
														
	
														
														MerchantPayment merchantPayment = new MerchantPayment(merchant.getMerchantName(), orderRef, transactionRef, 
																bankReferenceNo, responseData, TransactionStatus.PENDING.name(), amount, transaction.getId(), 
																account.getProbasePayCurrency().name(), null, 
																account.getId(), customerId, null, merchant.getId(), device.getId(), deviceTrafficSource.getSwitchToLive());
														merchantPayment = (MerchantPayment)swpService.createNewRecord(merchantPayment);
														
														JSONObject transferInfo = new JSONObject();
														transferInfo.put("sourceIdentifier", account.getAccountIdentifier());
														transferInfo.put("sourceCustomerName", account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName());
														transferInfo.put("newSourceAvailableBalance", debitResponse.getDouble("availablebalance"));
														transferInfo.put("newSourceCurrentBalance", debitResponse.getDouble("currentbalance"));
														transferInfo.put("sourceCharges", debitResponse.getDouble("charges"));
														transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
														transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
														
														jsonObject.put("status", ERROR.GENERAL_OK);
														jsonObject.put("message", "Payment was successful");
														jsonObject.put("orderRef", orderRef);
														jsonObject.put("amountTransferred", amount);
														jsonObject.put("receipientInfo", transferInfo.toString());		
														
														
														
														
														
	
														System.out.println("CREDIT RECIPIENT WALLET6");
														System.out.println("=======================");
														
														
														transaction.setDebitAccountTrue(debitAccountTrue);
														transaction.setDebitCardTrue(debitCardTrue);
														transaction.setCreditAccountId(creditAccountId);
														transaction.setCreditCardId(creditCardId);
														transaction.setDebitAccountId(debitAccountId);
														transaction.setDebitCardId(debitCardId);
														transaction.setTransactionCode(TransactionCode.transactionSuccess);
														transaction.setResponseCode("00");
														transaction.setMessageResponse(allResponse.toString());
														transaction.setStatus(TransactionStatus.SUCCESS);
														transaction.setIsLive(deviceTrafficSource.getSwitchToLive());
														swpService.updateRecord(transaction);
														
														merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
														swpService.updateRecord(merchantPayment);
														
														
														

														


														JSONObject jsbreakdown = new JSONObject();
														jsbreakdown.put("Sub-total", amount);
														jsbreakdown.put("Charges", (charges));
														
														JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), account.getAccountIdentifier(), transaction.getOrderRef().toUpperCase(), 
																transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
														
														
														if(narration!=null)
															txnDetails.put("narration", narration);


														if(longNarration!=null)
															txnDetails.put("extraInformation", longNarration);
														
														System.out.println("narration str...");
														
														transaction.setSummary(txnDetails.toString());
														swpService.updateRecord(transaction);
														
														String receipentMobileNumber = account.getCustomer().getContactMobile();
														String smsMessage = "Hello\nYour payment of "+ (account.getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
														//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
														//swpService.createNewRecord(smsMsg);


														SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
														new Thread(smsSender).start();
														
														/*JSONObject creditResponse = UtilityHelper.creditBankWallet(app, swpService, httpHeaders,
																requestContext, amount, recipientAccount, token, merchantId, deviceCode, 
																user, channel, orderRef, narration, acquirer, transaction);
														
														System.out.println("creditResponse....");
														System.out.println(creditResponse);
														if(creditResponse!=null && creditResponse.has("status") && creditResponse.getInt("status")==ERROR.WALLET_CREDIT_SUCCESS)
														{
															allResponse.put("fundsTransferCreditResponse", creditResponse);
															
															String bankReference = creditResponse.getString("bankReference");
	
															transaction.setTransactionCode(TransactionCode.transactionSuccess);
															transaction.setResponseCode("00");
															transaction.setMessageResponse(allResponse.toString());
															transaction.setStatus(TransactionStatus.SUCCESS);
															transaction.setCreditBankPaymentReference(bankReference);
															swpService.updateRecord(transaction);
															
															merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
															swpService.updateRecord(merchantPayment);
															
															
															
															
															transferInfo.put("recepientIdentifier", recipientAccount.getAccountIdentifier());
															transferInfo.put("recepientCustomerName", recipientAccount.getCustomer().getFirstName() + " " + recipientAccount.getCustomer().getLastName());
															transferInfo.put("amountCreditToReceipient", creditResponse.getDouble("amount"));
															transferInfo.put("receipientCharges", creditResponse.getDouble("charges"));
															transferInfo.put("receipientBankReferenceNo", transaction.getCreditBankPaymentReference());
															transferInfo.put("transactionRefNo", transaction.getTransactionRef());
															
															
															
															
														}
														else
														{
														
															String description = narration;
															String requestId = bankReferenceNo;
															RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																null
															);
															requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
															
															
															
															transaction.setStatus(TransactionStatus.FAIL);
															transaction.setTransactionCode(TransactionCode.transactionFail);
															transaction.setResponseCode("02");
															swpService.updateRecord(transaction);
															
															jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL_CREDIT_FAILED);
															jsonObject.put("message", "Payment failed. A Reverse on debit is being processed.");
															jsonObject.put("orderRef", orderRef);
															jsonObject.put("reversalRefNo", requestTransactionReversal.getReverseTransactionRef());
															jsonObject.put("amountTransferred", amount);
															jsonObject.put("receipientInfo", JSONObject.NULL);
															
															
														}*/
													}
												}
												return Response.status(200).entity(jsonObject.toString()).build();
											}
										}
										else
										{
											jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
											jsonObject.put("message", "You cannot make this payment due to insufficient funds in your wallet. The minimum amount allowed is " + account.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
										}
									}
									else
									{
										jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
										jsonObject.put("message", "You do not have enough funds in your wallet to make this payment. Please fund your wallet first");
									}
								}
								else
								{

									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "We could not get the balance on your wallet at the moment.");
								}
							}
						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "You do not seem to have any wallet at the moment");
						}
					}
					else if(debitSourceType.equals("CARD"))
					{
						accountIdentifier = null;
						hql = "Select tp.*, cs.minimumBalance, c.contactMobile, c.contactEmail, c.firstName, c.lastName, acc.accountIdentifier from ecards tp, cardschemes cs, customers c, accounts acc where tp.cardScheme_id = cs.id AND "
								+ "tp.customerId = c.id AND tp.account_id = acc.id AND (tp.serialNo = '" + cardSerialNo + "'" 
								+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
						List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
						
						
						if(ecards!=null && ecards.size()>0)
						{
							Map<String, Object> ecard = ecards.get(0);
							Double balance = (Double)ecard.get("cardBalance");
							Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
							String mobileNo = (String)ecard.get("contactMobile");
							String userEmail = (String)ecard.get("contactEmail");
							String accountIdentifier_ = (String)ecard.get("accountIdentifier");
							Long ecardId = ((BigInteger)ecard.get("id")).longValue();
							ECard card = (ECard)swpService.getRecordById(ECard.class, ecardId);
							
							if((amount+cardMinimumBalance)<=balance)
							{
								String zauthKey = null;
								if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
									zauthKey = acquirer.getAuthKey();
								else
									zauthKey = acquirer.getDemoAuthKey();
									
								
								hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + 
										" AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
								List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
								Boolean debitAccountTrue = true;
								Boolean debitCardTrue = false;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								Account recipientAccount = null;
								if(devicebankaccounts!=null && devicebankaccounts.size()>0)
								{
									Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
									creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
									debitCardId = card.getId();
									recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
								}
								
								if(recipientAccount==null)
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Merchant payment was not successful. Merchants account not activated");
									return Response.status(200).entity(jsonObject.toString()).build();
								}
								
								/*String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
								if(!(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1")))
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
									return Response.status(200).entity(jsonObject.toString()).build();
								}*/
								
								if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
								{
									PaymentServicesV2 paymentServices = new PaymentServicesV2();
									String accountData = UtilityHelper.encryptData(accountIdentifier_, bankKey);
									
									
									String bankPaymentReference = null;
									Long customerId = card.getAccount().getCustomer().getId();
									Boolean creditAccountTrue = false;
									Boolean creditCardTrue = true;
									String rpin = null;
									Channel ch = Channel.valueOf(channel);
									Date transactionDate = new Date();
									ServiceType serviceType = ServiceType.PAY_MERCHANT;
									String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
									String payerEmail = card.getAccount().getCustomer().getContactEmail();
									String payerMobile = card.getAccount().getCustomer().getContactMobile();
									TransactionStatus status = TransactionStatus.PENDING;
									ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
									String transactionCode = null;
									Boolean creditPoolAccountTrue = false;
									String messageRequest = null;
									String messageResponse = null;
									Double fixedCharge = 0.00;
									Double transactionCharge = 0.00;
									Double transactionPercentage = 0.00;
									Double schemeTransactionCharge = 0.00;
									Double schemeTransactionPercentage = 0.00;
									String responseCode = null;
									String oTP = null;
									String oTPRef = otpRef;
									String merchantBank = null;
									String merchantAccount = null; 
									Long transactingBankId = acquirer.getBank().getId();
									Long receipientTransactingBankId = null;
									Integer accessCode = null;
									Long sourceEntityId = card.getAccount().getId();
									Long receipientEntityId = null;
									Channel receipientChannel = ch;
									String transactionDetail = narration;
									Double closingBalance = null;
									Double totalCreditSum = null;
									Double totalDebitSum = null;
									Long paidInByBankUserAccountId = null;
									String customData = null;
									String responseData = null;
									Long adjustedTransactionId = null;
									Long acquirerId = null;
									String merchantName = merchant.getMerchantName();
									String merchantCode = merchant.getMerchantCode();
									Long merchantId_ = merchant.getId();
									
									
									Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
											customerId, creditAccountTrue, creditCardTrue,
											orderRef, rpin, ch,
											transactionDate, serviceType, payerName,
											payerEmail, payerMobile, status,
											probasePayCurrency, transactionCode,
											card.getAccount(), card, device,
											creditPoolAccountTrue, messageRequest,
											messageResponse, fixedCharge,
											transactionCharge, transactionPercentage,
											schemeTransactionCharge, schemeTransactionPercentage,
											amount, responseCode, oTP, oTPRef,
											merchantId_, merchantName, merchantCode,
											merchantBank, merchantAccount, 
											transactingBankId, receipientTransactingBankId,
											accessCode, sourceEntityId, receipientEntityId,
											receipientChannel, transactionDetail,
											closingBalance, totalCreditSum, totalDebitSum,
											paidInByBankUserAccountId, customData,
											responseData, adjustedTransactionId, acquirerId, 
											debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
											merchant.getMerchantName(), deviceTrafficSource.getSwitchToLive());
									transaction = (Transaction)swpService.createNewRecord(transaction);
									
									System.out.println("DEBIT SOURCE WALLET");
									System.out.println("===================");
									Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, hash, "", transaction, orderRef, serviceTypeId, token);
									String drResStr = (String)drRes.getEntity();
									System.out.println("DebitResponse");
									System.out.println(drResStr);
									if(drResStr!=null)
									{
										JSONObject allResponse = new JSONObject();
										allResponse.put("fundsTransferDebitResponse", drResStr);
										JSONObject debitResponse = new JSONObject(drResStr);
										if(debitResponse.has("status") && debitResponse.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
										{
											
											
											String bankReferenceNo = debitResponse.getString("bankReferenceNo");
											transaction.setTransactionCode(TransactionCode.transactionAdviceReverse);
											transaction.setResponseCode("03");
											transaction.setMessageResponse(allResponse.toString());
											transaction.setDebitBankPaymentReference(bankReferenceNo);
											swpService.updateRecord(transaction);
											
											MerchantPayment merchantPayment = new MerchantPayment(merchant.getMerchantName(), orderRef, transactionRef, 
													bankReferenceNo, responseData, TransactionStatus.PENDING.name(), amount, transaction.getId(), 
													card.getAccount().getProbasePayCurrency().name(), null, 
													card.getAccount().getId(), customerId, null, merchant.getId(), device.getId(), deviceTrafficSource.getSwitchToLive());
											merchantPayment = (MerchantPayment)swpService.createNewRecord(merchantPayment);
											
											
											
											card = card.withdraw(swpService, amount, 0.00);
											
											
											JSONObject transferInfo = new JSONObject();
											transferInfo.put("sourceIdentifier", card.getPan().substring(0, 4) + "****" + card.getPan().substring(card.getPan().length()-4));
											transferInfo.put("sourceCustomerName", card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName());
											transferInfo.put("newSourceAccountAvailableBalance", debitResponse.getDouble("availablebalance"));
											transferInfo.put("newSourceAccountCurrentBalance", debitResponse.getDouble("currentbalance"));
											transferInfo.put("amountDebitedFromSource", debitResponse.getDouble("amount"));
											transferInfo.put("sourceWalletCharges", debitResponse.getDouble("charges"));
											transferInfo.put("sourceBankReferenceNo", transaction.getDebitBankPaymentReference());
											transferInfo.put("transactionRefNo", transaction.getTransactionRef());
											
											
											
											
											jsonObject.put("status", ERROR.GENERAL_OK);
											jsonObject.put("message", "Payment was successful");
											jsonObject.put("orderRef", orderRef);
											jsonObject.put("amountTransferred", amount);
											jsonObject.put("receipientInfo", transferInfo.toString());


											System.out.println("CREDIT RECIPIENT WALLET8");
											System.out.println("=======================");
											
											

											


											JSONObject jsbreakdown = new JSONObject();
											jsbreakdown.put("Sub-total", amount);
											jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
											
											JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.PAY_MERCHANT.name(), merchant.getMerchantName(), device.getDeviceCode(), card.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
													transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
											
											if(narration!=null)
												txnDetails.put("narration", narration);


											if(longNarration!=null)
												txnDetails.put("extraInformation", longNarration);
											
											System.out.println("narration str...");
											
											transaction.setSummary(txnDetails.toString());
											swpService.updateRecord(transaction);
											
											String receipentMobileNumber = card.getAccount().getCustomer().getContactMobile();
											String smsMessage = "Hello\nYour payment of "+ (card.getAccount().getProbasePayCurrency().name() + amount) +" to the Merchant - " + merchant.getMerchantName() + "  was successful.";
											//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
											//swpService.createNewRecord(smsMsg);

											SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
											new Thread(smsSender).start();
											
											
											
											
											/*JSONObject creditResponse = UtilityHelper.creditBankWallet(app, swpService, httpHeaders,
													requestContext, amount, recipientAccount, token, merchantId, deviceCode, 
													user, channel, orderRef, narration, acquirer, transaction);
											
											System.out.println("creditResponse....");
											System.out.println(creditResponse);
											if(creditResponse!=null && creditResponse.has("status") && creditResponse.getInt("status")==ERROR.WALLET_CREDIT_SUCCESS)
											{
												allResponse.put("fundsTransferCreditResponse", creditResponse);
												
												String bankReference = creditResponse.getString("bankReference");

												transaction.setTransactionCode(TransactionCode.transactionSuccess);
												transaction.setResponseCode("00");
												transaction.setMessageResponse(allResponse.toString());
												transaction.setStatus(TransactionStatus.SUCCESS);
												transaction.setCreditBankPaymentReference(bankReference);
												swpService.updateRecord(transaction);
												
												merchantPayment.setStatus(TransactionStatus.SUCCESS.name());
												swpService.updateRecord(merchantPayment);

												card = card.deposit(swpService, amount, 0.00);
												
												
												
												transferInfo.put("recepientIdentifier", recipientAccount.getAccountIdentifier());
												transferInfo.put("recepientCustomerName", recipientAccount.getCustomer().getFirstName() + " " + recipientAccount.getCustomer().getLastName());
												
												transferInfo.put("amountCreditToReceipient", creditResponse.getDouble("amount"));
												transferInfo.put("receipientWalletCharges", creditResponse.getDouble("charges"));
												transferInfo.put("receipientBankReferenceNo", transaction.getCreditBankPaymentReference());
												
											}
											else
											{
											
												String description = narration;
												String requestId = bankReferenceNo;
												RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
													RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
													description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
													transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
													card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName(), 
													null
												);
												requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
												
												
												transaction.setStatus(TransactionStatus.FAIL);
												transaction.setTransactionCode(TransactionCode.transactionFail);
												transaction.setResponseCode("02");
												swpService.updateRecord(transaction);
												
													
													
													
												
												jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL_CREDIT_FAILED);
												jsonObject.put("message", "Payment failed. A Reverse on debit is being processed.");
												jsonObject.put("orderRef", orderRef);
												jsonObject.put("reversalRefNo", requestTransactionReversal.getReverseTransactionRef());
												jsonObject.put("amountTransferred", amount);
												jsonObject.put("receipientInfo", JSONObject.NULL);
												
												
											}*/
										}
									}
									return Response.status(200).entity(jsonObject.toString()).build();
								}
								else
								{
									jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
									jsonObject.put("message", "You cannot pay this merchant the amount due to insufficient funds in your card. The minimum amount allowed is " + card.getAccount().getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
								}
							}
							else
							{
								jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
								jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to make this payment. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
							}
						}
						else
						{
							jsonObject.put("status", ERROR.CARD_NOT_VALID);
							jsonObject.put("message", "Invalid card. Card not found");
						}
					}
					
							
				}
				else
				{
					jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
					jsonObject.put("message", "No payment found matching the payment reference number provided");
				}
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General System error. Please try again later");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	
	
	
	
	@POST
	@Path("/purchaseAirtime")
	@Produces(MediaType.APPLICATION_JSON)
	public Response purchaseAirtime(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("telcoProvider") String telcoProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("accountIdentifier") String accountIdentifier,
			@FormParam("otp") String otp,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("channel") String channel,
			@FormParam("hash") String hash,
			@FormParam("validationToken") String validationToken,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("debitSourceType") String debitSourceType,
			@FormParam("token") String token)
	{

		System.out.println("========================================");
		System.out.println("Purchase Airtime");
		System.out.println("========================================");
		System.out.println(merchantId);
		System.out.println(deviceCode);
		System.out.println(telcoProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(accountIdentifier);
		System.out.println(token);
		System.out.println(otpRef);
		System.out.println(otp);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(telcoProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
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
			
			
			if(debitSourceType.equals("WALLET"))
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "' AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
				Account account_ = (Account)this.swpService.getUniqueRecordByHQL(hql);
				
				
				if(account_!=null)
				{
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr!=null)
					{
						JSONObject balanceDet = new JSONObject(balanceDetStr);
						System.out.println("balanceDet.....");
						System.out.println(balanceDet.toString());
						 
						if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
						{
							Double currentbalance = balanceDet.getDouble("currentBalance");
							Double availablebalance = balanceDet.getDouble("availableBalance");
							System.out.println("totalCurrentBalance....." + currentbalance);
							System.out.println("availablebalance....." + availablebalance);
							//totalCurrentBalance = totalCurrentBalance + currentbalance;
							//totalAvailableBalance = totalAvailableBalance + availablebalance;
						//}
						//jsonObject.put("currentbalance", totalCurrentBalance);
						//jsonObject.put("availablebalance", totalAvailableBalance);
							Double balance = availablebalance;
							Double walletMinimumBalance = account_.getAccountScheme().getMinimumBalance();
							
							if((amount+walletMinimumBalance)<=balance)
							{
								
								
								Object obj = account_.getCustomer().getContactMobile();
								Object obj1 = account_.getCustomer().getContactEmail();
								String mobileNo = (String)obj;
								String userEmail = (String)obj1;
								String zauthKey = null;
								if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
									zauthKey = acquirer.getAuthKey();
								else
									zauthKey = acquirer.getDemoAuthKey();
									
								//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
								
								if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
								{
									//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
									if(1==1)
									{
										System.out.println(amount);
										System.out.println(walletMinimumBalance);
										System.out.println(balance);
										if((amount+walletMinimumBalance)<=balance)
										{
											String authKey = Application.BILLS_AUTH_KEY;
											String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
											String airtimeResponseString = UtilityHelper.purchaseAirtime(authKey, validationToken);
											System.out.println(airtimeResponseString);
											if(airtimeResponseString!=null)
											{
												JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
												JSONObject data = airtimeResponseJS.getJSONObject("data");
												
												
												if(data.has("Purchase"))
												{
													System.out.println("Purchase exist");
													if(data.isNull("Purchase"))
													{
														System.out.println("Purchase is null");
													}
												}
												JSONObject purchase = data.has("Purchase") && data.get("Purchase")!=null ?  data.getJSONObject("Purchase") : null;
												JSONArray errors = airtimeResponseJS.has("errors") && airtimeResponseJS.get("errors")!=null ? airtimeResponseJS.getJSONArray("errors") : null;
												if(purchase!=null && errors==null)
												{
				
													JSONObject purchaseResponse = purchase.getJSONObject("response");
													String purchaseStatus = purchase.getString("status");
													String etransactionRef = purchase.getString("transaction_ref");
													Integer purchaseResponseStatus = purchaseResponse.getInt("status");
													String purchaseResponseOperation = purchaseResponse.getString("operation");
													JSONObject provider = purchaseResponse.getJSONObject("provider");
													System.out.println(provider.toString());
													String msisdn = provider.getString("msisdn");
													String amountPurchased = provider.getString("amount");
													
													
													
													
													if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
													{
														String accountNo = account_.getAccountIdentifier();
														
														
														//hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+ 
														//		"' AND tp.merchant.merchantCode = '"+merchantId+"' AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
														//Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
														Device device = deviceTrafficSource;
														Merchant merchant = merchantTrafficSource;
														//if(device!=null)
														//{
														//	merchant = device.getMerchant();
														//}
														
														String accountData = UtilityHelper.encryptData(accountNo, bankKey);
														System.out.println("bankKey..." + bankKey);
														System.out.println("accountNo..." + accountNo);
														System.out.println("accountData..." + accountData);
														String narration = "AIRTIMEPURCHASE~" + amount + "~" + amountPurchased + "~" + telcoProvider + "~" + receipient;
														PaymentServicesV2 paymentServices = new PaymentServicesV2();
														
														String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
														String bankPaymentReference = null;
														Long customerId = account_.getCustomer().getId();
														Boolean creditAccountTrue = true;
														Boolean creditCardTrue = false;
														String rpin = null;
														Channel ch = Channel.valueOf(channel);
														Date transactionDate = new Date();
														ServiceType serviceType = ServiceType.DEBIT_WALLET;
														String payerName = account_.getCustomer().getFirstName() + " " + account_.getCustomer().getLastName();
														String payerEmail = account_.getCustomer().getContactEmail();
														String payerMobile = account_.getCustomer().getContactMobile();
														TransactionStatus status = TransactionStatus.PENDING;
														ProbasePayCurrency probasePayCurrency = account_.getProbasePayCurrency();
														String transactionCode = null;
														Boolean creditPoolAccountTrue = false;
														String messageRequest = null;
														String messageResponse = null;
														Double fixedCharge = 0.00;
														Double transactionCharge = 0.00;
														Double transactionPercentage = 0.00;
														Double schemeTransactionCharge = 0.00;
														Double schemeTransactionPercentage = 0.00;
														String responseCode = null;
														String oTP = null;
														String oTPRef = null;
														String merchantBank = null;
														String merchantAccount = null; 
														Long transactingBankId = acquirer.getBank().getId();
														Long receipientTransactingBankId = null;
														Integer accessCode = null;
														Long sourceEntityId = account_.getId();
														Long receipientEntityId = null;
														Channel receipientChannel = ch;
														String transactionDetail = narration;
														Double closingBalance = null;
														Double totalCreditSum = null;
														Double totalDebitSum = null;
														Long paidInByBankUserAccountId = null;
														String customData = null;
														String responseData = null;
														Long adjustedTransactionId = null;
														Long acquirerId = null;
														String merchantName = merchant.getMerchantName();
														String merchantCode = merchant.getMerchantCode();
														Long merchantId_ = merchant.getId();
														
														
														
														hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id "
																+ "AND tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL AND " + " tp.isLive = " + deviceTrafficSource.getSwitchToLive();
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
															debitAccountId = account_.getId();
														}
														
														Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
																customerId, creditAccountTrue, creditCardTrue,
																orderRef, rpin, ch,
																transactionDate, serviceType, payerName,
																payerEmail, payerMobile, status,
																probasePayCurrency, transactionCode,
																account_, null, device,
																creditPoolAccountTrue, messageRequest,
																messageResponse, fixedCharge,
																transactionCharge, transactionPercentage,
																schemeTransactionCharge, schemeTransactionPercentage,
																amount, responseCode, oTP, oTPRef,
																merchantId_, merchantName, merchantCode,
																merchantBank, merchantAccount, 
																transactingBankId, receipientTransactingBankId,
																accessCode, sourceEntityId, receipientEntityId,
																receipientChannel, transactionDetail,
																closingBalance, totalCreditSum, totalDebitSum,
																paidInByBankUserAccountId, customData,
																responseData, adjustedTransactionId, acquirerId, 
																debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
																"Purchase Airtime - " + msisdn, deviceTrafficSource.getSwitchToLive());
														transaction = (Transaction)swpService.createNewRecord(transaction);
														//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
														Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, amount, hash, "", transaction, 
																transaction.getOrderRef(), serviceTypeId, token);
														String drResStr = (String)drRes.getEntity();
														System.out.println(drResStr);
														if(drResStr!=null)
														{
															JSONObject drResJS = new JSONObject(drResStr);
															if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
															{
																
																
																
																JSONObject jsbreakdown = new JSONObject();
																jsbreakdown.put("Sub-total", amount);
																jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
																
																transaction.setStatus(TransactionStatus.SUCCESS);
																JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.DIRECT_TOP_UP.name(), telcoProvider, receipient, accountNo, transaction.getOrderRef().toUpperCase(), 
																		transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
																transaction.setSummary(txnDetails.toString());
																swpService.updateRecord(transaction);
																
																UtilityPurchased up = new UtilityPurchased(
																		"AIRTIME", "Direct-TopUp", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
																		transaction.getAmount(), transaction.getId(), null, account_.getId(), account_.getCustomer().getId(), 
																		probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
																swpService.createNewRecord(up);
																
																

																String receipentMobileNumber = account_.getCustomer().getContactMobile();
																String smsMessage = "Hello\nAirtime purchase of "+ (account_.getProbasePayCurrency().name() + amount) +" was successful.";
																//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
																//swpService.createNewRecord(smsMsg);

																SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
																new Thread(smsSender).start();
																
																
																jsonObject.put("status", ERROR.GENERAL_OK);
																jsonObject.put("message", "Airtime purchase was successful. The mobile number " + receipient + " has been credited with " + account_.getProbasePayCurrency().name() + amount + " worth of airtime");
															}
															else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
															{
																String message = drResJS.getString("message");
																jsonObject.put("status", ERROR.GENERAL_FAIL);
																jsonObject.put("message", message==null ? "Airtime purchase was not successful" : "Airtime purchase was not successful." + message);
															}
														}
														else
														{
															jsonObject.put("status", ERROR.GENERAL_FAIL);
															jsonObject.put("message", "Airtime purchase was not successful");
														}
														
													}
													else
													{
														
														String errorMessage = provider.getString("error");
														jsonObject.put("status", ERROR.GENERAL_FAIL);
														jsonObject.put("message", "Airtime purchase was not successful. " + errorMessage);
													}
												}
												else
												{
													JSONObject error = errors.getJSONObject(0);
													String errorMessage = error.getString("message");
													jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
													jsonObject.put("message", errorMessage);
												}
											}
											else
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "Your Airtime purchase was not successful. Please try again");
											}
											
											
											
										}
									}
									else
									{
										jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
										jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
									}
								}
								else
								{
									jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
									jsonObject.put("message", "You cannot purchase airtime for that amount. The minimum amount allowed is " + account_.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
								}
							}
							else
							{

								jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
								jsonObject.put("message", "You do not have sufficient funds to pay for this purchase");
							}

						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			else
			{
			
				hql = "Select tp.*, cs.minimumBalance, cs.currency from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
						+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				
				
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					Integer currency = (Integer)ecard.get("currency");
					
					hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
					List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
					Map<String, Object> customerMobile = customerMobiles.get(0);
					Object obj = customerMobile.get("contactMobile");
					Object obj1 = customerMobile.get("contactEmail");
					String mobileNo = (String)obj;
					String userEmail = (String)obj1;
					String zauthKey = null;
					if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
						zauthKey = acquirer.getAuthKey();
					else
						zauthKey = acquirer.getDemoAuthKey();
						
					//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
					
					if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
					{
						//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
						if(1==1)
						{
							System.out.println(amount);
							System.out.println(cardMinimumBalance);
							System.out.println(balance);
							if((amount+cardMinimumBalance)<=balance)
							{
								String authKey = Application.BILLS_AUTH_KEY;
								String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
								String airtimeResponseString = UtilityHelper.purchaseAirtime(authKey, validationToken);
								System.out.println(airtimeResponseString);
								if(airtimeResponseString!=null)
								{
									JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
									JSONObject data = airtimeResponseJS.getJSONObject("data");
									
									
									if(data.has("Purchase"))
									{
										System.out.println("Purchase exist");
										if(data.isNull("Purchase"))
										{
											System.out.println("Purchase is null");
										}
									}
									JSONObject purchase = data.has("Purchase") && data.get("Purchase")!=null ?  data.getJSONObject("Purchase") : null;
									JSONArray errors = airtimeResponseJS.has("errors") && airtimeResponseJS.get("errors")!=null ? airtimeResponseJS.getJSONArray("errors") : null;
									if(purchase!=null && errors==null)
									{
	
										JSONObject purchaseResponse = purchase.getJSONObject("response");
										String purchaseStatus = purchase.getString("status");
										String etransactionRef = purchase.getString("transaction_ref");
										Integer purchaseResponseStatus = purchaseResponse.getInt("status");
										String purchaseResponseOperation = purchaseResponse.getString("operation");
										JSONObject provider = purchaseResponse.getJSONObject("provider");
										System.out.println(provider.toString());
										String msisdn = provider.getString("msisdn");
										String amountPurchased = provider.getString("amount");
										
										
										
										
										if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
										{
											hql = "Select tp from Account tp where tp.id = " + ((BigInteger)ecard.get("accountId")) + " AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
											Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
											String accountNo = account.getAccountIdentifier();
											hql = "Select tp from ECard tp where (tp.serialNo = '" + cardSerialNo + "'" 
													+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
											ECard card = (ECard)this.swpService.getUniqueRecordByHQL(hql);
											
											//hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantId+"'";
											//Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
											//Merchant merchant = null;
											//if(device!=null)
											//{
											//	merchant = device.getMerchant();
											//}
											
											String accountData = UtilityHelper.encryptData(accountNo, bankKey);
											System.out.println("bankKey..." + bankKey);
											System.out.println("accountNo..." + accountNo);
											System.out.println("accountData..." + accountData);
											String narration = "AIRTIMEPURCHASE~" + amount + "~" + amountPurchased + "~" + telcoProvider + "~" + receipient;
											PaymentServicesV2 paymentServices = new PaymentServicesV2();
											
											String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
											String bankPaymentReference = null;
											Long customerId = ((BigInteger)customerMobile.get("id")).longValue();
											Boolean creditAccountTrue = false;
											Boolean creditCardTrue = false;
											String rpin = null;
											Channel ch = Channel.valueOf(channel);
											Date transactionDate = new Date();
											ServiceType serviceType = ServiceType.DEBIT_WALLET;
											String payerName = (String)customerMobile.get("firstName") + " " + (String)customerMobile.get("lastName");
											String payerEmail = (String)customerMobile.get("contactEmail");
											String payerMobile = (String)customerMobile.get("contactMobile");
											TransactionStatus status = TransactionStatus.PENDING;
											ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
											String transactionCode = null;
											Boolean creditPoolAccountTrue = false;
											String messageRequest = null;
											String messageResponse = null;
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											Double schemeTransactionCharge = 0.00;
											Double schemeTransactionPercentage = 0.00;
											String responseCode = null;
											String oTP = null;
											String oTPRef = null;
											String merchantBank = null;
											String merchantAccount = null; 
											Long transactingBankId = acquirer.getBank().getId();
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = card.getId();
											Long receipientEntityId = null;
											Channel receipientChannel = ch;
											String transactionDetail = narration;
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = null;
											String customData = null;
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = null;
											String merchantName = merchantTrafficSource.getMerchantName();
											String merchantCode = merchantTrafficSource.getMerchantCode();
											Long merchantId_ = merchantTrafficSource.getId();

											hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
													+ "tp.deviceId = " + deviceTrafficSource.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
											List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
											Boolean debitAccountTrue = false;
											Boolean debitCardTrue = true;
											Long creditAccountId = null;
											Long creditCardId = null;
											Long debitAccountId = null;
											Long debitCardId = null;
											if(devicebankaccounts!=null && devicebankaccounts.size()>0)
											{
												Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
												creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
												debitCardId = card.getId();
											}
											
											Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
													customerId, creditAccountTrue, creditCardTrue,
													orderRef, rpin, ch,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probasePayCurrency, transactionCode,
													account, card, deviceTrafficSource,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													amount, responseCode, oTP, oTPRef,
													merchantId_, merchantName, merchantCode,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													receipientChannel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customData,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Purchase Airtime - " + msisdn, 
													deviceTrafficSource.getSwitchToLive());
											transaction = (Transaction)swpService.createNewRecord(transaction);
											//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
											Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, amount, hash, "", transaction, 
													transaction.getOrderRef(), serviceTypeId, token);
											String drResStr = (String)drRes.getEntity();
											System.out.println(drResStr);
											if(drResStr!=null)
											{
												JSONObject drResJS = new JSONObject(drResStr);
												if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													card = card.withdraw(swpService, amount, 0.00);
													
													JSONObject jsbreakdown = new JSONObject();
													jsbreakdown.put("Sub-total", amount);
													jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
													
													transaction.setStatus(TransactionStatus.SUCCESS);
													JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.DIRECT_TOP_UP.name(), telcoProvider, receipient, card.getTrackingNumber() + " - MASTERCARD", transaction.getOrderRef().toUpperCase(), 
															transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
													transaction.setSummary(txnDetails.toString());
													swpService.updateRecord(transaction);
													
													
													UtilityPurchased up = new UtilityPurchased(
															"AIRTIME", "Direct-TopUp", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
															transaction.getAmount(), transaction.getId(), card.getId(), card.getAccount().getId(), card.getAccount().getCustomer().getId(), 
															probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
													swpService.createNewRecord(up);
													
													
													
													String receipentMobileNumber = card.getAccount().getCustomer().getContactMobile();
													String smsMessage = "Hello\nAirtime purchase of "+ (card.getAccount().getProbasePayCurrency().name() + amount) +" was successful.";
													//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
													//swpService.createNewRecord(smsMsg);
													


													SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
													new Thread(smsSender).start();
													
													
													jsonObject.put("status", ERROR.GENERAL_OK);
													jsonObject.put("message", "Airtime purchase was successful. The mobile number " + receipient + " has been credited with " + ProbasePayCurrency.values()[currency].name() + amount + " worth of airtime");
												}
												else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String message = drResJS.getString("message");
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", message==null ? "Airtime purchase was not successful" : "Airtime purchase was not successful." + message);
												}
											}
											else
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "Airtime purchase was not successful");
											}
											
										}
										else
										{
											
											String errorMessage = provider.getString("error");
											jsonObject.put("status", ERROR.GENERAL_FAIL);
											jsonObject.put("message", "Airtime purchase was not successful. " + errorMessage);
										}
									}
									else
									{
										JSONObject error = errors.getJSONObject(0);
										String errorMessage = error.getString("message");
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", errorMessage);
									}
								}
								else
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Your Airtime purchase was not successful. Please try again");
								}
								
								
								
							}
						}
						else
						{
							jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
							jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
						jsonObject.put("message", "You cannot purchase airtime for that amount. The minimum amount allowed is " + ProbasePayCurrency.values()[currency].name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "System error. Please try again");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/purchaseElectricity")
	@Produces(MediaType.APPLICATION_JSON)
	public Response purchaseElectricity(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("vendorProvider") String vendorProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("accountIdentifier") String accountIdentifier,
			@FormParam("otp") String otp,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("channel") String channel,
			@FormParam("hash") String hash,
			@FormParam("validationToken") String validationToken,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("debitSourceType") String debitSourceType,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("token") String token)
	{
		System.out.println(vendorProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		System.out.println(otpRef);
		System.out.println(otp);
		System.out.println(merchantId);
		System.out.println(deviceCode);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);

			if(vendorProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
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
			
			hql = "Select tp.*, cs.minimumBalance, cs.currency from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
					+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
			List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			
			
			
			
			
			
			if(UtilityHelper.validateTransactionHash(
					hash, 
					merchantId==null ? "" : merchantId,
					deviceCode==null ? "" : deviceCode,
					serviceTypeId,
					orderRef,
					amount,
					"",
					acquirer.getAccessExodus())==true)
			{
				
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid request. Data mismatch. Please try again");
			}
			
			if(debitSourceType.equals("WALLET"))
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "'";
				Account account_ = (Account)this.swpService.getUniqueRecordByHQL(hql);
				
				
				if(account_!=null)
				{
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr!=null)
					{
						JSONObject balanceDet = new JSONObject(balanceDetStr);
						System.out.println("balanceDet.....");
						System.out.println(balanceDet.toString());
						 
						if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
						{
							Double currentbalance = balanceDet.getDouble("currentBalance");
							Double availablebalance = balanceDet.getDouble("availableBalance");
							System.out.println("totalCurrentBalance....." + currentbalance);
							System.out.println("availablebalance....." + availablebalance);
							//totalCurrentBalance = totalCurrentBalance + currentbalance;
							//totalAvailableBalance = totalAvailableBalance + availablebalance;
						//}
						//jsonObject.put("currentbalance", totalCurrentBalance);
						//jsonObject.put("availablebalance", totalAvailableBalance);
							Double balance = availablebalance;
							Double walletMinimumBalance = account_.getAccountScheme().getMinimumBalance();
							
							if((amount+walletMinimumBalance)<=balance)
							{
								
								
								Object obj = account_.getCustomer().getContactMobile();
								Object obj1 = account_.getCustomer().getContactEmail();
								String mobileNo = (String)obj;
								String userEmail = (String)obj1;
								String zauthKey = null;
								if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
									zauthKey = acquirer.getAuthKey();
								else
									zauthKey = acquirer.getDemoAuthKey();
									
								//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
								
								if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
								{
									//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
									if(1==1)
									{
										System.out.println(amount);
										System.out.println(walletMinimumBalance);
										System.out.println(balance);
										if((amount+walletMinimumBalance)<=balance)
										{

											PaymentServicesV2 paymentServices = new PaymentServicesV2();
											Account account = account_;
											String accountNo = account.getAccountIdentifier();
											String accountData = UtilityHelper.encryptData(accountNo, bankKey);
											System.out.println("bankKey..." + bankKey);
											System.out.println("accountNo..." + accountNo);
											System.out.println("accountData..." + accountData);
											String narration = "ELECTRICITYPURCHASE~" + amount + "~" + amount + "~" + vendorProvider + "~" + 
													receipient;
											
											String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
											String bankPaymentReference = null;
											Long customerId = account.getCustomer().getId();
											Boolean creditAccountTrue = false;
											Boolean creditCardTrue = false;
											String rpin = null;
											Channel ch = Channel.valueOf(channel);
											Date transactionDate = new Date();
											ServiceType serviceType = ServiceType.DEBIT_WALLET;
											String payerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
											String payerEmail = account.getCustomer().getContactEmail();
											String payerMobile = account.getCustomer().getContactMobile();
											TransactionStatus status = TransactionStatus.PENDING;
											ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
											String transactionCode = null;
											Boolean creditPoolAccountTrue = false;
											String messageRequest = null;
											String messageResponse = null;
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											Double schemeTransactionCharge = 0.00;
											Double schemeTransactionPercentage = 0.00;
											String responseCode = null;
											String oTP = null;
											String oTPRef = null;
											Long merchantId_ = deviceTrafficSource.getMerchant().getId();
											String merchantName = null;
											String merchantCode = null;
											String merchantBank = null;
											String merchantAccount = null; 
											Long transactingBankId = acquirer.getBank().getId();
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = account.getId();
											Long receipientEntityId = null;
											Channel receipientChannel = ch;
											String transactionDetail = narration;
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = null;
											String customData = null;
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = null;
											
											hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
													+ "tp.deviceId = " + deviceTrafficSource.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
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
													orderRef, rpin, ch,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probasePayCurrency, transactionCode,
													account, null, deviceTrafficSource,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													amount, responseCode, oTP, oTPRef,
													merchantId_, merchantName, merchantCode,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													receipientChannel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customData,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Purchase " + vendorProvider + " Units", 
													deviceTrafficSource.getSwitchToLive());
											transaction = (Transaction)swpService.createNewRecord(transaction);
											
											Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, 
													amount, hash, "", transaction, transaction.getOrderRef(), serviceTypeId, token);
											String drResStr = (String)drRes.getEntity();
											System.out.println(drResStr);
											if(drResStr!=null)
											{
												JSONObject drResJS = new JSONObject(drResStr);
												String bankReferenceNo = drResJS.getString("bankReference");
												if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String authKey = Application.BILLS_AUTH_KEY;
													try
													{
														String airtimeResponseString = UtilityHelper.purchaseElectricity(authKey, validationToken);
														System.out.println(airtimeResponseString);
														if(airtimeResponseString!=null)
														{
															JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
															JSONObject data = airtimeResponseJS.getJSONObject("data");
															JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
															if(errors==null)
															{
			
																JSONObject purchase = data.getJSONObject("Purchase");
																JSONObject purchaseResponse = purchase.getJSONObject("response");
																String purchaseStatus = purchase.getString("status");
																String etransactionRef = purchase.getString("transaction_ref");
																Integer purchaseResponseStatus = purchaseResponse.getInt("status");
																String purchaseResponseOperation = purchaseResponse.getString("operation");
																JSONObject provider = purchaseResponse.getJSONObject("provider");
																System.out.println(provider.toString());
																JSONObject electricity_data = provider.getJSONObject("electricity_data");
																JSONObject totals = electricity_data.getJSONObject("totals");
																JSONArray fbe_tokens = electricity_data.getJSONArray("fbe_tokens");
																JSONObject fbe_token = fbe_tokens.getJSONObject(0);
																String code = fbe_token.getString("code");
																String units = totals.getString("units");
																String amountPurchased = totals.getString("total");
																
																
																if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
																{
																	

																	String[] code_ = code.split("(?<=\\G.{5})");
																	String codeStr = String.join("-", code_);
	
																	
	
																	JSONObject jsbreakdown = new JSONObject();
																	jsbreakdown.put("Sub-total", amount);
																	jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
																	
																	transaction.setStatus(TransactionStatus.SUCCESS);
																	JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.ELECTRICITY_PURCHASE.name(), vendorProvider, receipient, accountNo, transaction.getOrderRef().toUpperCase(), 
																			transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
																	txnDetails.put("unitsPurchased", units);
																	txnDetails.put("electricityPin", codeStr);
																	transaction.setSummary(txnDetails.toString());
																	
																	swpService.updateRecord(transaction);
																	
																	
																	
																	//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
																	UtilityPurchased up = new UtilityPurchased(
																			"ZESCO", "Electricity Units", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
																			transaction.getAmount(), transaction.getId(), null, account_.getId(), account_.getCustomer().getId(), 
																			probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
																	swpService.createNewRecord(up);
																	
																	jsonObject.put("electricity_data", provider);
																	jsonObject.put("status", ERROR.GENERAL_OK);
																	jsonObject.put("message", "Electricity units purchase was successful. Amount purchased for meter number - " + receipient + " is " + account_.getProbasePayCurrency().name() + amount + " worth of units - " + units);
																	
																	
	
																	
																	String receipentMobileNumber = account.getCustomer().getContactMobile();
																	String smsMessage = "Hello\nYour purchase of " + units + "KWH " + vendorProvider + " units on Meter No. " + receipient + " was successful.\nToken ID: " + codeStr + "\nPay Ref: " + transaction.getOrderRef();
																	//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
																	//swpService.createNewRecord(smsMsg);
	
	
																	SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
																	new Thread(smsSender).start();
																}
																else
																{
																	String description = narration;
																	String requestId = bankReferenceNo;
																	RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																			RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																			description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																			transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																			account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																			null, deviceTrafficSource.getSwitchToLive()
																		);
																	requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
																	
																	String errorMessage = provider.getString("error");
																	jsonObject.put("status", ERROR.GENERAL_FAIL);
																	jsonObject.put("message", "Electricity units purchase was not successful. " + errorMessage);
																}
															}
															else
															{
																String description = narration;
																String requestId = bankReferenceNo;
																RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																		RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																		description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																		transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																		account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																		null, deviceTrafficSource.getSwitchToLive()
																	);
																requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
																
																JSONObject error = errors.getJSONObject(0);
																String errorMessage = error.getString("message");
																jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
																jsonObject.put("message", errorMessage);
															}
														}
														else
														{
															String description = narration;
															String requestId = bankReferenceNo;
															RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																	RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																	description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																	transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																	account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																	null, deviceTrafficSource.getSwitchToLive()
																);
															requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
															jsonObject.put("status", ERROR.GENERAL_FAIL);
															jsonObject.put("message", "Your Electricity units purchase was not successful. Please try again");
														}
													}
													catch(Exception e)
													{
														String description = narration;
														String requestId = bankReferenceNo;
														RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																null, deviceTrafficSource.getSwitchToLive()
															);
														requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
														jsonObject.put("status", ERROR.GENERAL_FAIL);
														jsonObject.put("message", "Your Electricity units purchase was not successful. Please try again");
													}
													
												}
												else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String message = drResJS.getString("message");
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", message==null ? "Electricity units purchase was not successful" : "Electricity units purchase was not successful." + message);
												}
											}
											else
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "Electricity units purchase was not successful");
											}
										}
									}
									else
									{
										jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
										jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
									}
								}
								else
								{
									jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
									jsonObject.put("message", "You cannot purchase airtime for that amount. The minimum amount allowed is " + account_.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
								}
							}
							else
							{

								jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
								jsonObject.put("message", "You do not have sufficient funds to pay for this purchase");
							}

						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			else
			{
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					Integer currency = (Integer)ecard.get("currency");
					
					hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
					List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
					Map<String, Object> customerMobile = customerMobiles.get(0);
					Object obj = customerMobile.get("contactMobile");
					Object obj1 = customerMobile.get("contactEmail");
					String mobileNo = (String)obj;
					String userEmail = (String)obj1;
					String zauthKey = null;
					if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
						zauthKey = acquirer.getAuthKey();
					else
						zauthKey = acquirer.getDemoAuthKey();
						
					
					
					if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
					{
						//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
						//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
						if(1==1)
						{
							System.out.println(amount);
							System.out.println(cardMinimumBalance);
							System.out.println(balance);
							if((amount+cardMinimumBalance)<=balance)
							{
								String authKey = Application.BILLS_AUTH_KEY;
								String narration = "ELECTRICITYPURCHASE~" + amount + "~" + vendorProvider + "~" + 
										receipient;
								
								hql = "Select tp from Account tp where tp.id = " + ((BigInteger)ecard.get("accountId")) + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
								Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
								String accountNo = account.getAccountIdentifier();
								hql = "Select tp from ECard tp where (tp.serialNo = '" + cardSerialNo + "'" 
										+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
								ECard card = (ECard)this.swpService.getUniqueRecordByHQL(hql);
								
								
								String accountData = UtilityHelper.encryptData(accountNo, bankKey);
								System.out.println("bankKey..." + bankKey);
								System.out.println("accountNo..." + accountNo);
								System.out.println("accountData..." + accountData);
								
								PaymentServicesV2 paymentServices = new PaymentServicesV2();
								
								String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
								String bankPaymentReference = null;
								Long customerId = ((BigInteger)customerMobile.get("id")).longValue();
								Boolean creditAccountTrue = false;
								Boolean creditCardTrue = false;
								String rpin = null;
								Channel ch = Channel.valueOf(channel);
								Date transactionDate = new Date();
								ServiceType serviceType = ServiceType.DEBIT_WALLET;
								String payerName = (String)customerMobile.get("firstName") + " " + (String)customerMobile.get("lastName");
								String payerEmail = (String)customerMobile.get("contactEmail");
								String payerMobile = (String)customerMobile.get("contactMobile");
								TransactionStatus status = TransactionStatus.PENDING;
								ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
								String transactionCode = null;
								Boolean creditPoolAccountTrue = false;
								String messageRequest = null;
								String messageResponse = null;
								Double fixedCharge = 0.00;
								Double transactionCharge = 0.00;
								Double transactionPercentage = 0.00;
								Double schemeTransactionCharge = 0.00;
								Double schemeTransactionPercentage = 0.00;
								String responseCode = null;
								String oTP = null;
								String oTPRef = null;
								Long merchantId_ = null;
								String merchantName = null;
								String merchantCode = null;
								String merchantBank = null;
								String merchantAccount = null; 
								Long transactingBankId = acquirer.getBank().getId();
								Long receipientTransactingBankId = null;
								Integer accessCode = null;
								Long sourceEntityId = account.getId();
								Long receipientEntityId = null;
								Channel receipientChannel = ch;
								String transactionDetail = narration;
								Double closingBalance = null;
								Double totalCreditSum = null;
								Double totalDebitSum = null;
								Long paidInByBankUserAccountId = null;
								String customData = null;
								String responseData = null;
								Long adjustedTransactionId = null;
								Long acquirerId = null;
								
								hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + deviceTrafficSource.getId() + 
										" AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
								List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
								Boolean debitAccountTrue = false;
								Boolean debitCardTrue = true;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								if(devicebankaccounts!=null && devicebankaccounts.size()>0)
								{
									Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
									creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
									debitCardId = card.getId();
								}
								//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
								
								Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
										customerId, creditAccountTrue, creditCardTrue,
										orderRef, rpin, ch,
										transactionDate, serviceType, payerName,
										payerEmail, payerMobile, status,
										probasePayCurrency, transactionCode,
										account, card, deviceTrafficSource,
										creditPoolAccountTrue, messageRequest,
										messageResponse, fixedCharge,
										transactionCharge, transactionPercentage,
										schemeTransactionCharge, schemeTransactionPercentage,
										amount, responseCode, oTP, oTPRef,
										merchantId_, merchantName, merchantCode,
										merchantBank, merchantAccount, 
										transactingBankId, receipientTransactingBankId,
										accessCode, sourceEntityId, receipientEntityId,
										receipientChannel, transactionDetail,
										closingBalance, totalCreditSum, totalDebitSum,
										paidInByBankUserAccountId, customData,
										responseData, adjustedTransactionId, acquirerId, 
										debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Purchase " + vendorProvider + " Units", 
										deviceTrafficSource.getSwitchToLive());
								transaction = (Transaction)swpService.createNewRecord(transaction);
								//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
								Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, 
										amount, hash, "", transaction, transaction.getOrderRef(), serviceTypeId, token);
								String drResStr = (String)drRes.getEntity();
								System.out.println(drResStr);
								if(drResStr!=null)
								{
									JSONObject drResJS = new JSONObject(drResStr);
									String bankReferenceNo = drResJS.getString("bankReference");
									if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
									{
										try
										{
											card = card.withdraw(swpService, amount, 0.00);
											
											String airtimeResponseString = UtilityHelper.purchaseElectricity(authKey, validationToken);
											System.out.println(airtimeResponseString);
											if(airtimeResponseString!=null)
											{
												JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
												JSONObject data = airtimeResponseJS.getJSONObject("data");
												JSONObject purchase = data.getJSONObject("Purchase");
												JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
												if(purchase!=null && errors==null)
												{
				
													JSONObject purchaseResponse = purchase.getJSONObject("response");
													String purchaseStatus = purchase.getString("status");
													String etransactionRef = purchase.getString("transaction_ref");
													Integer purchaseResponseStatus = purchaseResponse.getInt("status");
													String purchaseResponseOperation = purchaseResponse.getString("operation");
													JSONObject provider = purchaseResponse.getJSONObject("provider");
													System.out.println(provider.toString());
													JSONObject electricity_data = provider.getJSONObject("electricity_data");
													JSONObject totals = electricity_data.getJSONObject("totals");
													String units = totals.getString("units");
													String amountPurchased = totals.getString("total");
													JSONArray fbe_tokens = electricity_data.getJSONArray("fbe_tokens");
													JSONObject fbe_token = fbe_tokens.getJSONObject(0);
													String code = fbe_token.getString("code");
													
													
													
													
													if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
													{
														narration = "ELECTRICITYPURCHASE~" + amount + "~" + amountPurchased + "~" + vendorProvider + "~" + 
																receipient + "~" + etransactionRef + "~" + units;
														

														String[] code_ = code.split("(?<=\\G.{5})");
														String codeStr = String.join("-", code_);
														
														JSONObject jsbreakdown = new JSONObject();
														jsbreakdown.put("Sub-total", amount);
														jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
														
														transaction.setTransactionDetail(narration);
														transaction.setStatus(TransactionStatus.SUCCESS);
														JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.ELECTRICITY_PURCHASE.name(), vendorProvider, receipient, card.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
																transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
														txnDetails.put("unitsPurchased", units);
														txnDetails.put("electricityPin", codeStr);
														transaction.setSummary(txnDetails.toString());
														swpService.updateRecord(transaction);
														
														
														UtilityPurchased up = new UtilityPurchased(
																"ZESCO", "Electricity Units", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
																transaction.getAmount(), transaction.getId(), card.getId(), card.getAccount().getId(), card.getAccount().getCustomer().getId(), 
																probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
														swpService.createNewRecord(up);
														
														jsonObject.put("electricity_data", provider);
														jsonObject.put("status", ERROR.GENERAL_OK);
														jsonObject.put("message", "Electricity units purchase was successful. Amount purchased for meter number - " + receipient + " is " + ProbasePayCurrency.values()[currency].name() + amount + " worth of units - " + units);
														
														
														
														

														
														String receipentMobileNumber = card.getAccount().getCustomer().getContactMobile();
														String smsMessage = "Hello\nYour purchase of " + units + "KWH " + vendorProvider + " units on Meter No. " + receipient + " was successful.\nToken ID: " + codeStr + "\nPay Ref: " + transaction.getOrderRef();
														//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
														//swpService.createNewRecord(smsMsg);


														SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
														new Thread(smsSender).start();
													}
													else
													{
														
														String errorMessage = provider.getString("error");
														jsonObject.put("status", ERROR.GENERAL_FAIL);
														jsonObject.put("message", "Electricity units purchase was not successful. " + errorMessage);
													}
												}
												else
												{
													JSONObject error = errors.getJSONObject(0);
													String errorMessage = error.getString("message");
													jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
													jsonObject.put("message", errorMessage);
												}
											}
											else
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "Your Electricity units purchase was not successful. Please try again");
											}

										}
										catch(Exception e)
										{
											String description = narration;
											String requestId = bankReferenceNo;
											RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
													RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
													description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
													transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
													account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
													null, deviceTrafficSource.getSwitchToLive()
												);
											requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
											jsonObject.put("status", ERROR.GENERAL_FAIL);
											jsonObject.put("message", "Your Electricity units purchase was not successful. Please try again");
										}
										
										
									}
									else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
									{
										String message = drResJS.getString("message");
										jsonObject.put("status", ERROR.GENERAL_FAIL);
										jsonObject.put("message", message==null ? "Electricity units purchase was not successful" : "Electricity units purchase was not successful." + message);
									}
								}
								else
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Electricity units purchase was not successful");
								}
									
									
								
								
							}
						}
						else
						{
							jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
							jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
						jsonObject.put("message", "You cannot purchase Electricity units for that amount. The minimum amount allowed is " + ProbasePayCurrency.values()[currency].name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General system failure");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/validateDSTV")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateDSTV(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("vendorProvider") String vendorProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("debitSourceType") String debitSourceType, 
			@FormParam("accountIdentifier") String accountIdentifier, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("token") String token)
	{
		System.out.println(vendorProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			

			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(vendorProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "1Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "2Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from Device tp where tp.deviceCode = '"+ deviceCode +"' AND tp.merchant.merchantCode = '"+merchantId+"'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp.*, cs.minimumBalance from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
					+ " OR tp.trackingNumber = '" + cardTrackingNo + "')";
			List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			System.out.println("ecards..." + ecards.size());
			if(debitSourceType.equals("WALLET"))
			{
				
				hql = "Select cs.minimumBalance, tp.* from accounts tp, cardschemes cs where tp.accountScheme_id = cs.id AND (tp.accountIdentifier = '" + accountIdentifier + "')";
				List<Map<String, Object>> accounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
				if(accounts!=null && accounts.size()>0)
				{
					
					Map<String, Object> account = accounts.get(0);
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr==null)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
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
								Double balance = availablebalance;
								Double walletMinimumBalance = (Double)account.get("minimumBalance");
								
								if((amount+walletMinimumBalance)<=balance)
								{
									String authKey = Application.BILLS_AUTH_KEY;
									String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
									String airtimeResponseString = UtilityHelper.validateDSTV(vendorProvider, receipient, amount, authKey, externalRef);
									if(airtimeResponseString!=null)
									{
										JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
										JSONObject data = airtimeResponseJS.getJSONObject("data");
										JSONObject validation = data.getJSONObject("Validation");
										JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
										if(validation!=null && errors==null)
										{
											Double amountPurchased = validation.getDouble("amount");
											JSONObject validationResponse = validation.getJSONObject("response");
											String validationServiceType = validation.getString("service_type");
											String validationToken = validation.getString("token");
											String operation = validationResponse.getString("operation");
											Integer operationStatus = validationResponse.getInt("status");
											
											if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
											{
												JSONObject provider = validationResponse.getJSONObject("provider");
												hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)account.get("customer_id"));
												List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
												Map<String, Object> customerMobile = customerMobiles.get(0);
												Object obj = customerMobile.get("contactMobile");
												Object obj1 = customerMobile.get("contactEmail");
												String mobileNo = (String)obj;
												String userEmail = (String)obj1;
												
												System.out.println("mobileNo..." + mobileNo);
												System.out.println("userEmail..." + userEmail);
												System.out.println("hql..." + ((BigInteger)account.get("customer_id")));
												
												JSONObject js_ = new JSONObject();
												String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
												if(otpGenerated!=null && otpGenerated[0].equals("1"))
												{
													jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
													jsonObject.put("otpRef", otpGenerated[3]);
													jsonObject.put("validationToken", validationToken);
													jsonObject.put("customerInfo", provider);
													jsonObject.put("message", "DSTV smart card number validated successful. Please enter the One-Time Password sent to your mobile number");
												}
												else
												{
													jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
													jsonObject.put("message", "We could not send you an OTP to confirm your subscription purchase. Please try again");
												}
												
												//PaymentServicesV2 paymentServices = new PaymentServicesV2();
												//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
												
											}
											else
											{
												JSONObject provider = validationResponse.getJSONObject("provider");
												String errorMessage = provider.getString("error");
												jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
												jsonObject.put("message", "Mobile number could not be validated. " + errorMessage);
											}
										}
										else
										{
											JSONObject error = errors.getJSONObject(0);
											String errorMessage = error.getString("message");
											jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
											jsonObject.put("message", errorMessage);
										}
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "DSTV smart card number could not be validated. Please provide a valid smart card number");
									}
									
									
								}
								else
								{
									jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
									jsonObject.put("message", "Insufficient funds. You do not have enough funds in your wallet to purchase electricity units because you have reached your spending limit. The least amount allowed on your card is " + walletMinimumBalance);
								}
							
						}
					}
					
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			else if(debitSourceType.equals("CARD"))
			{
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					
					System.out.println("balance..." + balance);
					System.out.println("amount..." + amount);
					System.out.println("cardMinimumBalance..." + cardMinimumBalance);
					
					if((amount+cardMinimumBalance)<=balance)
					{
						String authKey = Application.BILLS_AUTH_KEY;
						String externalRef = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
						String airtimeResponseString = UtilityHelper.validateDSTV(vendorProvider, receipient, amount, authKey, externalRef);
						if(airtimeResponseString!=null)
						{
							JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
							JSONObject data = airtimeResponseJS.getJSONObject("data");
							JSONObject validation = data.getJSONObject("Validation");
							JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
							if(validation!=null && errors==null)
							{
								Double amountPurchased = validation.getDouble("amount");
								JSONObject validationResponse = validation.getJSONObject("response");
								String validationServiceType = validation.getString("service_type");
								String validationToken = validation.getString("token");
								String operation = validationResponse.getString("operation");
								Integer operationStatus = validationResponse.getInt("status");
								
								if(operationStatus.equals(0) && operation.equalsIgnoreCase("success"))
								{
									JSONObject provider = validationResponse.getJSONObject("provider");
									hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
									List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
									Map<String, Object> customerMobile = customerMobiles.get(0);
									Object obj = customerMobile.get("contactMobile");
									Object obj1 = customerMobile.get("contactEmail");
									String mobileNo = (String)obj;
									String userEmail = (String)obj1;
									
									System.out.println("mobileNo..." + mobileNo);
									System.out.println("userEmail..." + userEmail);
									System.out.println("hql..." + ((BigInteger)ecard.get("customerId")));
									
									JSONObject js_ = new JSONObject();
									String[] otpGenerated = UtilityHelper.generateZICBWalletOTP(js_, mobileNo, userEmail, acquirer, device, this.swpService);
									if(otpGenerated!=null && otpGenerated[0].equals("1"))
									{
										jsonObject.put("status", ERROR.BILL_ID_VALIDATED_SUCCESS);
										jsonObject.put("otpRef", otpGenerated[3]);
										jsonObject.put("validationToken", validationToken);
										jsonObject.put("customerInfo", provider);
										jsonObject.put("message", "DSTV smart card number validated successful. Please enter the One-Time Password sent to your mobile number");
									}
									else
									{
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", "We could not send you an OTP to confirm your subscription purchase. Please try again");
									}
									
									//PaymentServicesV2 paymentServices = new PaymentServicesV2();
									//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, validateOTP, otp, transaction, merchantCode, deviceCode, orderId, account, accountNo, narration);
									
								}
								else
								{
									JSONObject provider = validationResponse.getJSONObject("provider");
									String errorMessage = provider.getString("error");
									jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
									jsonObject.put("message", "Mobile number could not be validated. " + errorMessage);
								}
							}
							else
							{
								JSONObject error = errors.getJSONObject(0);
								String errorMessage = error.getString("message");
								jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
								jsonObject.put("message", errorMessage);
							}
						}
						else
						{
							jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
							jsonObject.put("message", "DSTV smart card number could not be validated. Please provide a valid smart card number");
						}
						
						
					}
					else
					{
						jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
						jsonObject.put("message", "Insufficient funds. You do not have enough funds in your card to purchase a DSTV Subscription. You have reached your spending limit. The least amount allowed on your card is " + cardMinimumBalance);
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Invalid card. Card not found");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General System error. Please try again later");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@POST
	@Path("/purchaseDSTV")
	@Produces(MediaType.APPLICATION_JSON)
	public Response purchaseDSTV(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("vendorProvider") String vendorProvider, 
			@FormParam("receipient") String receipient, 
			@FormParam("amount") Double amount, 
			@FormParam("cardSerialNo") String cardSerialNo, 
			@FormParam("cardTrackingNo") String cardTrackingNo, 
			@FormParam("accountIdentifier") String accountIdentifier,
			@FormParam("otp") String otp,
			@FormParam("orderRef") String orderRef,
			@FormParam("otpRef") String otpRef,
			@FormParam("channel") String channel,
			@FormParam("hash") String hash,
			@FormParam("validationToken") String validationToken,
			@FormParam("serviceTypeId") String serviceTypeId,
			@FormParam("debitSourceType") String debitSourceType,
			@FormParam("merchantId") String merchantId,
			@FormParam("deviceCode") String deviceCode,
			@FormParam("token") String token)
	{
		System.out.println(merchantId);
		System.out.println(deviceCode);
		System.out.println(vendorProvider);
		System.out.println(receipient);
		System.out.println(amount);
		System.out.println(cardSerialNo);
		System.out.println(cardTrackingNo);
		System.out.println(token);
		System.out.println(otpRef);
		System.out.println(otp);
		Bank bank = new Bank();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			if(vendorProvider!=null && receipient!=null && amount!=null)
			{
				if(debitSourceType.equals("WALLET") && accountIdentifier!=null)
				{
					
				}
				else if(debitSourceType.equals("CARD") && cardSerialNo!=null && cardTrackingNo!=null)
				{
					
				}
				else
				{
					jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
					jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete parameters provided. Provide all the required parameters");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(amount<0 || amount==0)
			{
				jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
				jsonObject.put("message", "Invalid amount provided.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
		
			String hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"'";// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
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
			
			if(UtilityHelper.validateTransactionHash(
					hash, 
					merchantId==null ? "" : merchantId,
					deviceCode==null ? "" : deviceCode,
					serviceTypeId,
					orderRef,
					amount,
					"",
					acquirer.getAccessExodus())==true)
			{
				
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid request. Data mismatch. Please try again");
			}
			
			if(debitSourceType.equals("WALLET"))
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '" + accountIdentifier + "'";
				Account account_ = (Account)this.swpService.getUniqueRecordByHQL(hql);
				
				
				if(account_!=null)
				{
					
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							accountIdentifier, 
							token, 
							merchantId, 
							deviceCode);
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr!=null)
					{
						JSONObject balanceDet = new JSONObject(balanceDetStr);
						System.out.println("balanceDet.....");
						System.out.println(balanceDet.toString());
						 
						if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
						{
							Double currentbalance = balanceDet.getDouble("currentBalance");
							Double availablebalance = balanceDet.getDouble("availableBalance");
							System.out.println("totalCurrentBalance....." + currentbalance);
							System.out.println("availablebalance....." + availablebalance);
							//totalCurrentBalance = totalCurrentBalance + currentbalance;
							//totalAvailableBalance = totalAvailableBalance + availablebalance;
						//}
						//jsonObject.put("currentbalance", totalCurrentBalance);
						//jsonObject.put("availablebalance", totalAvailableBalance);
							Double balance = availablebalance;
							Double walletMinimumBalance = account_.getAccountScheme().getMinimumBalance();
							
							if((amount+walletMinimumBalance)<=balance)
							{
								
								
								Object obj = account_.getCustomer().getContactMobile();
								Object obj1 = account_.getCustomer().getContactEmail();
								String mobileNo = (String)obj;
								String userEmail = (String)obj1;
								String zauthKey = null;
								if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
									zauthKey = acquirer.getAuthKey();
								else
									zauthKey = acquirer.getDemoAuthKey();
									
								//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
								
								if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
								{
									//if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
									if(1==1)
									{
										System.out.println(amount);
										System.out.println(walletMinimumBalance);
										System.out.println(balance);
										if((amount+walletMinimumBalance)<=balance)
										{
											hql = "Select tp from Account tp where tp.id = " + account_.getId();
											Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
											String accountNo = account.getAccountIdentifier();
											
											
											String accountData = UtilityHelper.encryptData(accountNo, bankKey);
											System.out.println("bankKey..." + bankKey);
											System.out.println("accountNo..." + accountNo);
											System.out.println("accountData..." + accountData);
											String narration = "PAYDSTV~" + amount + "~" + vendorProvider + "~" + 
													receipient + "~";
											PaymentServicesV2 paymentServices = new PaymentServicesV2();
											
											String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
											String bankPaymentReference = null;
											Long customerId = account_.getId();
											Boolean creditAccountTrue = false;
											Boolean creditCardTrue = false;
											String rpin = null;
											Channel ch = Channel.valueOf(channel);
											Date transactionDate = new Date();
											ServiceType serviceType = ServiceType.DEBIT_WALLET;
											String payerName = account_.getCustomer().getFirstName() + " " + account_.getCustomer().getLastName();
											String payerEmail = account_.getCustomer().getContactEmail();
											String payerMobile = account_.getCustomer().getContactMobile();
											TransactionStatus status = TransactionStatus.PENDING;
											ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
											String transactionCode = null;
											Boolean creditPoolAccountTrue = false;
											String messageRequest = null;
											String messageResponse = null;
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											Double schemeTransactionCharge = 0.00;
											Double schemeTransactionPercentage = 0.00;
											String responseCode = null;
											String oTP = null;
											String oTPRef = null;
											Long merchantId_ = merchantTrafficSource!=null ? merchantTrafficSource.getId() : null;
											String merchantName = null;
											String merchantCode = null;
											String merchantBank = null;
											String merchantAccount = null; 
											Long transactingBankId = acquirer.getBank().getId();
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = account.getId();
											Long receipientEntityId = null;
											Channel receipientChannel = ch;
											String transactionDetail = narration;
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = null;
											String customData = null;
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = null;
											
											hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
													+ "tp.deviceId = " + deviceTrafficSource.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
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
													orderRef, rpin, ch,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probasePayCurrency, transactionCode,
													account, null, deviceTrafficSource,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													amount, responseCode, oTP, oTPRef,
													merchantId_, merchantName, merchantCode,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													receipientChannel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customData,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
													vendorProvider.toUpperCase() + " Subscription - " + receipient, deviceTrafficSource.getSwitchToLive());
											transaction = (Transaction)swpService.createNewRecord(transaction);
											//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
											Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, 
													hash, "", transaction, transaction.getOrderRef(), serviceTypeId, token);
											String drResStr = (String)drRes.getEntity();
											System.out.println(drResStr);
											if(drResStr!=null)
											{
												JSONObject drResJS = new JSONObject(drResStr);
												if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String bankReferenceNo = drResJS.getString("bankReferenceNo");
													String authKey = Application.BILLS_AUTH_KEY;
													String airtimeResponseString = UtilityHelper.purchaseDSTV(authKey, validationToken);
													System.out.println(airtimeResponseString);
													if(airtimeResponseString!=null)
													{
														JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
														JSONObject data = airtimeResponseJS.getJSONObject("data");
														JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
														if(errors==null)
														{
															JSONObject purchase = data.getJSONObject("Purchase");
						
															JSONObject purchaseResponse = purchase.getJSONObject("response");
															String purchaseStatus = purchase.getString("status");
															Integer purchaseResponseStatus = purchaseResponse.getInt("status");
															String purchaseResponseOperation = purchaseResponse.getString("operation");
															JSONObject provider = purchaseResponse.getJSONObject("provider");
															System.out.println(provider.toString());
															String etransactionRef = purchase.getString("transaction_ref");
															Double amountPurchased = purchase.getDouble("face_value");
															
															
															
															
															if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
															{

																
																JSONObject jsbreakdown = new JSONObject();
																jsbreakdown.put("Sub-total", amount);
																jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
																
																transaction.setStatus(TransactionStatus.SUCCESS);
																JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.CABLE_TV_SUBSCRIPTION.name(), vendorProvider, receipient, accountNo, transaction.getOrderRef().toUpperCase(), 
																		transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
																transaction.setSummary(txnDetails.toString());
																swpService.updateRecord(transaction);
																
																
																
																UtilityPurchased up = new UtilityPurchased(
																		"DSTV", "Subscription", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
																		transaction.getAmount(), transaction.getId(), null, account.getId(), account.getCustomer().getId(), 
																		probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
																swpService.createNewRecord(up);
																
																jsonObject.put("electricity_data", provider);
																jsonObject.put("status", ERROR.GENERAL_OK);
																jsonObject.put("message", "DSTV subscription payment was successful. Amount paid - " + account_.getProbasePayCurrency().name() + amount);
																
																
																
																String receipentMobileNumber = account.getCustomer().getContactMobile();
																String smsMessage = "Hello\nYour payment of "+ (account.getProbasePayCurrency().name() + amount) +" for DSTV subscription was successful.\nDecoder number - " + receipient;
																//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
																//swpService.createNewRecord(smsMsg);


																SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
																new Thread(smsSender).start();
															}
															else
															{
																String description = narration;
																String requestId = bankReferenceNo;
																RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																		RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																		description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																		transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																		account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																		null, deviceTrafficSource.getSwitchToLive()
																	);
																requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
																
																String errorMessage = provider.getString("error");
																jsonObject.put("status", ERROR.GENERAL_FAIL);
																jsonObject.put("message", "DSTV subscription payment was not successful. " + errorMessage);
															}
														}
														else
														{
															String description = narration;
															String requestId = bankReferenceNo;
															RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																	RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																	description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																	transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																	account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																	null, deviceTrafficSource.getSwitchToLive()
																);
															requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
															
															JSONObject error = errors.getJSONObject(0);
															String errorMessage = error.getString("message");
															jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
															jsonObject.put("message", errorMessage);
														}
													}
													else
													{
														String description = narration;
														String requestId = bankReferenceNo;
														RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
																RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, requestId, transaction.getAmount(), 
																description, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
																transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
																account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(), 
																null, deviceTrafficSource.getSwitchToLive()
															);
														requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
														
														jsonObject.put("status", ERROR.GENERAL_FAIL);
														jsonObject.put("message", "Your DSTV subscription payment was not successful. Please try again");
													}
													
												}
												else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String message = drResJS.getString("message");
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", message==null ? "DSTV subscription payment was not successful" : "DSTV subscription payment was not successful." + message);
												}
											}
											else
											{
												
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "DSTV subscription payment was not successful");
											}
											
											
											
										}
									}
									else
									{
										jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
										jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
									}
								}
								else
								{
									jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
									jsonObject.put("message", "You cannot purchase airtime for that amount. The minimum amount allowed is " + account_.getProbasePayCurrency().name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
								}
							}
							else
							{

								jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
								jsonObject.put("message", "You do not have sufficient funds to pay for this purchase");
							}

						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not get your wallets balance at this moment. Please try again later");
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			else
			{
				hql = "Select tp.*, cs.minimumBalance, cs.currency from ecards tp, cardschemes cs where tp.cardScheme_id = cs.id AND (tp.serialNo = '" + cardSerialNo + "'" 
						+ " OR tp.trackingNumber = '" + cardTrackingNo + "') AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
				List<Map<String, Object>> ecards = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			
				if(ecards!=null && ecards.size()>0)
				{
					Map<String, Object> ecard = ecards.get(0);
					Double balance = (Double)ecard.get("cardBalance");
					Double cardMinimumBalance = (Double)ecard.get("minimumBalance");
					Integer currency = (Integer)ecard.get("currency");
					
					hql = "Select tp.* from customers tp where tp.id = " + ((BigInteger)ecard.get("customerId"));
					List<Map<String, Object>> customerMobiles = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
					Map<String, Object> customerMobile = customerMobiles.get(0);
					Object obj = customerMobile.get("contactMobile");
					Object obj1 = customerMobile.get("contactEmail");
					String mobileNo = (String)obj;
					String userEmail = (String)obj1;
					String zauthKey = null;
					if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
						zauthKey = acquirer.getAuthKey();
					else
						zauthKey = acquirer.getDemoAuthKey();
						
					
					
					if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
					{
						//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
//						if(verifyResp!=null && verifyResp.length>0 && verifyResp[0].equals("1"))
						if(1==1)
						{
							System.out.println(amount);
							System.out.println(cardMinimumBalance);
							System.out.println(balance);
							if((amount+cardMinimumBalance)<=balance)
							{
								String authKey = Application.BILLS_AUTH_KEY;
								String airtimeResponseString = UtilityHelper.purchaseDSTV(authKey, validationToken);
								System.out.println(airtimeResponseString);
								if(airtimeResponseString!=null)
								{
									JSONObject airtimeResponseJS = new JSONObject(airtimeResponseString);
									JSONObject data = airtimeResponseJS.getJSONObject("data");
									JSONObject purchase = data.getJSONObject("Purchase");
									JSONArray errors = airtimeResponseJS.has("errors") ? airtimeResponseJS.getJSONArray("errors") : null;
									if(purchase!=null && errors==null)
									{
	
										JSONObject purchaseResponse = purchase.getJSONObject("response");
										String purchaseStatus = purchase.getString("status");
										Integer purchaseResponseStatus = purchaseResponse.getInt("status");
										String purchaseResponseOperation = purchaseResponse.getString("operation");
										JSONObject provider = purchaseResponse.getJSONObject("provider");
										System.out.println(provider.toString());
										String etransactionRef = purchase.getString("transaction_ref");
										Double amountPurchased = purchase.getDouble("face_value");
										
										
										
										
										if(purchaseResponseStatus.equals(0) && purchaseResponseOperation.equalsIgnoreCase("success"))
										{
											hql = "Select tp from Account tp where tp.id = " + ((BigInteger)ecard.get("accountId")) + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
											Account account = (Account)this.swpService.getUniqueRecordByHQL(hql);
											String accountNo = account.getAccountIdentifier();
											hql = "Select tp from ECard tp where (tp.serialNo = '" + cardSerialNo + "'" 
													+ " OR tp.trackingNumber = '" + cardTrackingNo + "')";
											ECard card = (ECard)this.swpService.getUniqueRecordByHQL(hql);
											
											
											String accountData = UtilityHelper.encryptData(accountNo, bankKey);
											System.out.println("bankKey..." + bankKey);
											System.out.println("accountNo..." + accountNo);
											System.out.println("accountData..." + accountData);
											String narration = "ELECTRICITYPURCHASE~" + amount + "~" + amountPurchased + "~" + vendorProvider + "~" + 
													receipient + "~" + etransactionRef;
											PaymentServicesV2 paymentServices = new PaymentServicesV2();
											
											String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
											String bankPaymentReference = null;
											Long customerId = ((BigInteger)customerMobile.get("id")).longValue();
											Boolean creditAccountTrue = false;
											Boolean creditCardTrue = false;
											String rpin = null;
											Channel ch = Channel.valueOf(channel);
											Date transactionDate = new Date();
											ServiceType serviceType = ServiceType.DEBIT_WALLET;
											String payerName = (String)customerMobile.get("firstName") + " " + (String)customerMobile.get("lastName");
											String payerEmail = (String)customerMobile.get("contactEmail");
											String payerMobile = (String)customerMobile.get("contactMobile");
											TransactionStatus status = TransactionStatus.PENDING;
											ProbasePayCurrency probasePayCurrency = account.getProbasePayCurrency();
											String transactionCode = null;
											Boolean creditPoolAccountTrue = false;
											String messageRequest = null;
											String messageResponse = null;
											Double fixedCharge = 0.00;
											Double transactionCharge = 0.00;
											Double transactionPercentage = 0.00;
											Double schemeTransactionCharge = 0.00;
											Double schemeTransactionPercentage = 0.00;
											String responseCode = null;
											String oTP = null;
											String oTPRef = null;
											Long merchantId_ = merchantTrafficSource==null ? null : merchantTrafficSource.getId();
											String merchantName = null;
											String merchantCode = null;
											String merchantBank = null;
											String merchantAccount = null; 
											Long transactingBankId = acquirer.getBank().getId();
											Long receipientTransactingBankId = null;
											Integer accessCode = null;
											Long sourceEntityId = account.getId();
											Long receipientEntityId = null;
											Channel receipientChannel = ch;
											String transactionDetail = narration;
											Double closingBalance = null;
											Double totalCreditSum = null;
											Double totalDebitSum = null;
											Long paidInByBankUserAccountId = null;
											String customData = null;
											String responseData = null;
											Long adjustedTransactionId = null;
											Long acquirerId = null;
											
											hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND "
													+ "tp.deviceId = " + deviceTrafficSource.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + deviceTrafficSource.getSwitchToLive();
											List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
											Boolean debitAccountTrue = false;
											Boolean debitCardTrue = true;
											Long creditAccountId = null;
											Long creditCardId = null;
											Long debitAccountId = null;
											Long debitCardId = null;
											if(devicebankaccounts!=null && devicebankaccounts.size()>0)
											{
												Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
												creditAccountId = ((BigInteger)devicebankaccount.get("transientAccountId")).longValue();
												debitCardId = card.getId();
											}
											//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
											
											Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
													customerId, creditAccountTrue, creditCardTrue,
													orderRef, rpin, ch,
													transactionDate, serviceType, payerName,
													payerEmail, payerMobile, status,
													probasePayCurrency, transactionCode,
													account, card, deviceTrafficSource,
													creditPoolAccountTrue, messageRequest,
													messageResponse, fixedCharge,
													transactionCharge, transactionPercentage,
													schemeTransactionCharge, schemeTransactionPercentage,
													amount, responseCode, oTP, oTPRef,
													merchantId_, merchantName, merchantCode,
													merchantBank, merchantAccount, 
													transactingBankId, receipientTransactingBankId,
													accessCode, sourceEntityId, receipientEntityId,
													receipientChannel, transactionDetail,
													closingBalance, totalCreditSum, totalDebitSum,
													paidInByBankUserAccountId, customData,
													responseData, adjustedTransactionId, acquirerId, 
													debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, 
													debitCardId, vendorProvider.toUpperCase() + " Subscription - " + receipient, deviceTrafficSource.getSwitchToLive());
											transaction = (Transaction)swpService.createNewRecord(transaction);
											//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
											Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantId, deviceCode, accountData, narration, amount, 
													hash, "", transaction, transaction.getOrderRef(), serviceTypeId, token);
											String drResStr = (String)drRes.getEntity();
											System.out.println(drResStr);
											if(drResStr!=null)
											{
												JSONObject drResJS = new JSONObject(drResStr);
												if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													card = card.withdraw(swpService, amount, 0.00);
													

													JSONObject jsbreakdown = new JSONObject();
													jsbreakdown.put("Sub-total", amount);
													jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
													
													transaction.setStatus(TransactionStatus.SUCCESS);
													JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.CABLE_TV_SUBSCRIPTION.name(), vendorProvider, receipient, card.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
															transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
													transaction.setSummary(txnDetails.toString());
													swpService.updateRecord(transaction);
													
													UtilityPurchased up = new UtilityPurchased(
															"DSTV", "Subscription", transaction.getOrderRef(), etransactionRef, airtimeResponseString, "SUCCESS", 
															transaction.getAmount(), transaction.getId(), card.getId(), card.getAccount().getId(), card.getAccount().getCustomer().getId(), 
															probasePayCurrency.name(), deviceTrafficSource.getSwitchToLive());
													swpService.createNewRecord(up);
													
													jsonObject.put("electricity_data", provider);
													jsonObject.put("status", ERROR.GENERAL_OK);
													jsonObject.put("message", "DSTV subscription payment was successful. Amount paid - " + ProbasePayCurrency.values()[currency].name() + amount);
												}
												else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
												{
													String message = drResJS.getString("message");
													jsonObject.put("status", ERROR.GENERAL_FAIL);
													jsonObject.put("message", message==null ? "DSTV subscription payment was not successful" : "DSTV subscription payment was not successful." + message);
												}
											}
											else
											{
												jsonObject.put("status", ERROR.GENERAL_FAIL);
												jsonObject.put("message", "DSTV subscription payment was not successful");
											}
											
										}
										else
										{
											
											String errorMessage = provider.getString("error");
											jsonObject.put("status", ERROR.GENERAL_FAIL);
											jsonObject.put("message", "DSTV subscription payment was not successful. " + errorMessage);
										}
									}
									else
									{
										JSONObject error = errors.getJSONObject(0);
										String errorMessage = error.getString("message");
										jsonObject.put("status", ERROR.BILL_ID_NOT_VALIDATED_SUCCESS);
										jsonObject.put("message", errorMessage);
									}
								}
								else
								{
									jsonObject.put("status", ERROR.GENERAL_FAIL);
									jsonObject.put("message", "Your DSTV subscription payment was not successful. Please try again");
								}
								
								
								
							}
						}
						else
						{
							jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
							jsonObject.put("message", "Your One-Time Password could not be validated. Please try again");
						}
					}
					else
					{
						jsonObject.put("status", ERROR.INVALID_TXN_AMOUNT);
						jsonObject.put("message", "You cannot purchase DSTV subscription for that amount. The minimum amount allowed is " + ProbasePayCurrency.values()[currency].name() + app.getAllSettings().getDouble("minimumtransactionamountweb"));
					}
				}
				else
				{
					jsonObject.put("status", ERROR.CARD_NOT_VALID);
					jsonObject.put("message", "Card to be debited could not be found.");
				}
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "General system failure");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	
	

}
