package com.probase.probasepay.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.math.BigInteger;
import java.security.Key;
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
import org.mindrot.jbcrypt.BCrypt;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import antlr.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MPQRDataStatus;
import com.probase.probasepay.enumerations.MPQRDataType;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.RoleType;
//import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.MPQRData;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Country;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.GroupMember;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.SMSMesage;
import com.probase.probasepay.models.Setting;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SmsSender;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/AuthenticationServicesV2")
public class AuthenticationServicesV2 {

	private static Logger log = Logger.getLogger(AuthenticationServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	//public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	Application application = null;
	
	
	/**Service Method - authenticateUser
	 * Method for authenticating all users who require 
	 * a login to access resources
	 * 
	 * @param status - MerchantStatus
	 * @return Stringified JSONObject of the list of merchants
	 */
	
	
	@POST
	@Path("/authenticateUserVerifyOTP")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUserVerifyOTP(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("otp") String otp)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
				
			System.out.println("VerifyJ = " + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				System.out.println("VerifyJ token = " + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}

			String subject = verifyJ.getString("subject");
			User user1 = new Gson().fromJson(subject, User.class);
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			String acquirerKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String otpDecrypted = (String)UtilityHelper.decryptData(otp, acquirerKey);
			
			String hql = "Select tp from User tp where tp.id = " + user1.getId();// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			
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
			
			int isLive = deviceTrafficSource.getSwitchToLive();
			
			System.out.println("hql = " + hql);
			if(user!=null && user.getOtp()!=null && user.getOtp().equals(otpDecrypted))
			{
				Date lastUpdate = user.getUpdated_at();
				System.out.println("lastUpdate==>" + lastUpdate.toString());//7352
				Date now = new Date();
				System.out.println("now==>" + now.toString());
				Calendar cal_5 = Calendar.getInstance();
				cal_5.setTime(lastUpdate);
				System.out.println("cal_5==>" + cal_5.getTime().toString());
				cal_5.add(Calendar.MINUTE, 5);
				Date now_5 = cal_5.getTime();
				System.out.println("now_5==>" + now_5.toString());
				
				if(now.after(now_5))
				{
					//Time has elasped 5 mins timeout for OTP
					jsonObject.put("status", ERROR.OTP_TIMEOUT);
					jsonObject.put("message", "OTP Timeout elasped");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					System.out.println("user.getOtp() = " + user.getOtp());
					
					if(user.getStatus().equals(UserStatus.INACTIVE))
					{
						user.setStatus(UserStatus.ACTIVE);
					}
					user.setOtp(null);
					this.swpService.updateRecord(user);
					
					
					
					
					BankStaff bankStaff = null;
					if(user.getRoleType().equals(RoleType.BANK_STAFF))
					{
						hql = "Select tp from BankStaff tp where tp.user.id = " + user.getId();
						System.out.println("hql = " + hql);
						bankStaff = (BankStaff)this.swpService.getUniqueRecordByHQL(hql);
					}
					Merchant merchant = null;
					if(user.getRoleType().ordinal() == (RoleType.MERCHANT.ordinal()))
					{
						hql = "Select tp from Merchant tp where tp.user.id = " + user.getId();
						
						System.out.println("hql = >" + hql);
						merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
					}
					Customer customer = null;
					if(user.getRoleType().equals(RoleType.CUSTOMER))
					{

						hql = "Select tp from Customer tp where tp.user.id = " + user1.getId();// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
						customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
						if(customer.getStatus().equals(CustomerStatus.INACTIVE))
						{
							customer.setStatus(CustomerStatus.ACTIVE);
							this.swpService.updateRecord(customer);
						}
						hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
						jsonObject.put("accounts", accounts==null ? null : accounts);
						jsonObject.put("walletExists", accounts!=null && accounts.size()>0 ? true : false);
						

						hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
								+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
						List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
						jsonObject.put("ecards", ecards==null ? null : ecards);
						jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
					}
					
					
					if(user.getRoleType().ordinal()== RoleType.BANK_STAFF.ordinal())
					{
						
						String hql_stat = "Select count(ac.id) as accountCount, " +
								"count(ec.id) as cardCount from accounts ac " +
								"LEFT JOIN ecards ec ON ec.accountId = ac.id "
								+ "LEFT JOIN issuers iss ON ec.issuer_id "
								+ "LEFT JOIN banks b ON iss.bank_id = b.id " +
								"WHERE b.id = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive;
						System.out.println("hql_stat==" + hql_stat);
						List<Map<String, Object>> counts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql_stat);
						System.out.println("counts==" + counts.size());
						
						/*hql_stat = "Select  sum(trAcct.amount) as trAcctAmt,  sum(trCard.amount) as trCardAmt,  " +
								"sum(trWall.amount) as trWallAmt,  sum(trMob.amount) as trMobAmt  from accounts " +
								"acc LEFT JOIN transactions trAcct ON (trAcct.creditAccountTrue = 0 AND " +
								"trAcct.account_id = acc.id) LEFT JOIN transactions trCard ON (trCard.creditCardTrue = 0 " +
								"AND trCard.account_id = acc.id)  LEFT JOIN transactions trWall ON (trWall.creditWalletTrue = 0 " +
								"AND trWall.account_id = acc.id)  LEFT JOIN transactions trMob ON (trMob.creditMobileAccountTrue = 0 " +
								"AND trMob.account_id = acc.id) " +
								"WHERE trAcct.transactingBankId = " + bankStaff.getBank().getId();*/
						hql_stat = "Select sum(trAcct.amount) as trAcctAmt, trAcct.serviceType  from transactions trAcct where " +
								"trAcct.transactingBankId = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive + 
								" GROUP by trAcct.serviceType";
						System.out.println("hql_stat==" + hql_stat);
						List<Object[]> sumAmounts = (List<Object[]>)swpService.getQueryBySQLResults(hql_stat);
						System.out.println("sumAmounts==" + sumAmounts.size());
						
						
						/*hql_stat = "Select count(ec.id) as successTxn, " +
								"count(ma.id) as paidOutTxn, count(wa.id) as pendTxn, (count(ec.id) + count(ma.id) + count(wa.id)) as allTxn " +
								"from accounts ac " +
								"LEFT JOIN transactions ec ON (ec.account_id = ac.id AND ec.status = " + TransactionStatus.SUCCESS.ordinal() +
								") LEFT JOIN  transactions ma ON (ma.account_id = ac.id AND ec.status =  " + TransactionStatus.PAIDOUT.ordinal() +
								") LEFT JOIN transactions wa ON (wa.account_id = ac.id AND ec.status =  " + TransactionStatus.PENDING.ordinal() +
								") WHERE ac.bank_id = " + bankStaff.getBank().getId();*/
						hql_stat = "Select count(ac.id), ac.status from accounts ac WHERE ac.bank_id = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive + 
								" GROUP BY ac.status"; 
						System.out.println("hql_stat==" + hql_stat);
						List<Object[]> statusCounts = (List<Object[]>)swpService.getQueryBySQLResults(hql_stat);
						System.out.println("statusCounts==" + statusCounts.size());
						
						
						Collection<Transaction> lastTransactions = 
								(Collection<Transaction>)this.swpService.getAllRecordsByHQL("Select tp from Transaction tp where tp.transactingBankId = " + 
								bankStaff.getBank().getId() + " AND tp.isLive = " + isLive, 0, 10);
						JSONObject jsonObjectStat = new JSONObject();
						
						Map<String, Object> countIt = counts.get(0);
						jsonObjectStat.put("virtualAcctCount", (BigInteger)countIt.get("accountCount"));
						jsonObjectStat.put("cardCount", (BigInteger)countIt.get("cardCount"));
						
						
						/*for(Object[] sumAmount : sumAmounts){
							jsonObjectStat.put("virtualAcctSum", (Double)sumAmount[0]);
							jsonObjectStat.put("mobileAcctSum", (Double)sumAmount[1]);
							jsonObjectStat.put("cardSum", (Double)sumAmount[2]);
							jsonObjectStat.put("walletAcctSum", (Double)sumAmount[3]);
						}
						
						for(Object[] count : statusCounts){
							jsonObjectStat.put("successTxn", (BigInteger)count[0]);
							jsonObjectStat.put("paidOutTxn", (BigInteger)count[1]);
							jsonObjectStat.put("pendTxn", (BigInteger)count[2]);
							jsonObjectStat.put("allTxn", (BigInteger)count[3]);
						}*/
						
						jsonObjectStat.put("lastTransactions", new Gson().toJson(lastTransactions));
						
						jsonObject.put("dashboardStatistics", jsonObjectStat.toString());
						jsonObject.put("transactionByServiceType", sumAmounts);
						jsonObject.put("transactionByStatus", statusCounts);
						
					}
					
					
					Gson gson = new Gson();
					String obj = gson.toJson(user);
					//Key jwtKey = Application.getKey();
					//String token = Jwts.builder().setSubject(obj).signWith(SignatureAlgorithm.HS512, jwtKey).compact();
					String tkId = RandomStringUtils.randomAlphanumeric(10);
					if(bankStaff!=null)
					{
						token = application.createJWT(tkId, acquirerCode, 
							bankStaff.getBranchCode(), obj, (1*60*60*1000));
					}else if(merchant!=null)
					{
						token = application.createJWT(tkId, acquirerCode, 
								merchant.getBankBranchCode(), obj, (1*60*60*1000));
					}
					else if(customer!=null)
					{
						token = application.createJWT(tkId, acquirerCode, 
								null, obj, (1*60*60*1000));
					}
					else
					{
						token = application.createJWT(tkId, "PROBASE", 
								"999", obj, (1*60*60*1000));
					}
					

					JSONObject allSettings = application.getAllSettings();
					String cardSupport = allSettings.has("cardsupport") ? allSettings.getString("cardsupport") : null;
					
					
					jsonObject.put("status", ERROR.AUTHENTICATE_OK);
					jsonObject.put("message", "Authentication Ok");
					jsonObject.put("username", user.getUsername());
					jsonObject.put("id", user.getId());
					jsonObject.put("userEmail", user.getUserEmail());
					jsonObject.put("mobileno", user.getMobileNo());
					jsonObject.put("merchant_key", merchant==null ? null : merchant.getMerchantDecryptKey());
					jsonObject.put("merchant_id", merchant==null ? null : merchant.getId());
					jsonObject.put("customerVerificationNo", customer==null ? null : customer.getVerificationNumber());
					jsonObject.put("customer_id", customer==null ? null : customer.getId());
					jsonObject.put("role_code", user.getRoleType());
					if(bankStaff!=null)
					{
						jsonObject.put("acquirer_code", acquirerCode);
						jsonObject.put("branch_code", bankStaff==null ? null : bankStaff.getBranchCode());
					}
					
					if(merchant!=null)
						jsonObject.put("acquirer_code", acquirerCode);
					
					if(customer!=null)
						jsonObject.put("acquirer_code", acquirerCode);
					
					
					jsonObject.put("token", token);
					jsonObject.put("firstName", user.getFirstName()==null ? null : user.getFirstName());
					jsonObject.put("lastName", user.getLastName()==null ? null : user.getLastName());
					jsonObject.put("otherName", user.getOtherName()==null ? null : user.getOtherName());
					System.out.println("user.getPassportImage()==" + (user.getProfilePix()==null ? "NULL" : user.getPassportImage()));
					jsonObject.put("profile_pix", user.getProfilePix()==null ? null : user.getProfilePix());
					jsonObject.put("all_banks", new Gson().toJson(new ArrayList<Bank>(this.application.getAllBanks())));
					jsonObject.put("all_merchant_schemes", new Gson().toJson(new ArrayList<MerchantScheme>(this.application.getAllMerchantSchemes())));
					jsonObject.put("all_device_types", (this.application.getAllDeviceTypes().toString()));
					jsonObject.put("all_card_schemes", new Gson().toJson(new ArrayList<CardScheme>(this.application.getAllCardSchemes())));
					jsonObject.put("all_acquirers", new Gson().toJson(new ArrayList<Acquirer>(this.application.getAllAcquirers())));
					jsonObject.put("all_issuers", new Gson().toJson(new ArrayList<Issuer>(this.application.getAllIssuers())));
					jsonObject.put("all_provinces", new Gson().toJson(new ArrayList<Province>(this.application.getAllProvinces())));
					jsonObject.put("all_districts", new Gson().toJson(new ArrayList<District>(this.application.getAllDistricts())));
					jsonObject.put("all_countries", new Gson().toJson(new ArrayList<Country>(this.application.getAllCountries())));
					jsonObject.put("cardSupport", cardSupport!=null && cardSupport.equals("1") ? true : false);
					
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				//return null;
				if(user!=null)
				{
					hql = "Select tp from User tp where tp.username = '" + user.getUsername() + "'";
					user = (User)this.swpService.getUniqueRecordByHQL(hql);
					user.setFailedLoginCount((user.getFailedLoginCount()==null ? 0 : user.getFailedLoginCount()) + 1);
					if(user.getFailedLoginCount()==3)
					{
						user.setStatus(UserStatus.LOCKED);
						this.swpService.updateRecord(user);
					}else
					{
						this.swpService.updateRecord(user);
					}
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}else
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.error("Exception", e);
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	

	@POST
	@Path("/authenticateUserVerifyPin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUserVerifyPin(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("otp") String otp)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
				
			System.out.println("VerifyJ = " + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				jsonObject.put("transactionDate", new SimpleDateFormat("MM/yy/dd H:m:s").format(new Date()));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				System.out.println("VerifyJ token = " + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}

			String subject = verifyJ.getString("subject");
			User user1 = new Gson().fromJson(subject, User.class);
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			String acquirerKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String otpDecrypted = (String)UtilityHelper.decryptData(otp, acquirerKey);
			
			String hql = "Select tp from User tp where tp.id = " + user1.getId();// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			
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
			
			int isLive = deviceTrafficSource.getSwitchToLive();
			
			System.out.println("hql = " + hql);
			if(user!=null && user.getPin()!=null && user.getPin().equals(otpDecrypted))
			{
				System.out.println("user.getPin() = " + user.getPin());
				
				if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					user.setStatus(UserStatus.ACTIVE);
				}
				this.swpService.updateRecord(user);
				
				
				
				
				BankStaff bankStaff = null;
				if(user.getRoleType().equals(RoleType.BANK_STAFF))
				{
					hql = "Select tp from BankStaff tp where tp.user.id = " + user.getId();
					System.out.println("hql = " + hql);
					bankStaff = (BankStaff)this.swpService.getUniqueRecordByHQL(hql);
				}
				Merchant merchant = null;
				if(user.getRoleType().ordinal() == (RoleType.MERCHANT.ordinal()))
				{
					hql = "Select tp from Merchant tp where tp.user.id = " + user.getId();
					
					System.out.println("hql = >" + hql);
					merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				}
				Customer customer = null;
				if(user.getRoleType().equals(RoleType.CUSTOMER))
				{

					hql = "Select tp from Customer tp where tp.user.id = " + user1.getId();// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
					customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
					if(customer.getStatus().equals(CustomerStatus.INACTIVE))
					{
						customer.setStatus(CustomerStatus.ACTIVE);
						this.swpService.updateRecord(customer);
					}
					hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
					List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("accounts", accounts==null ? null : accounts);
					jsonObject.put("walletExists", accounts!=null && accounts.size()>0 ? true : false);
					

					hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
							+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + isLive;
					List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("ecards", ecards==null ? null : ecards);
					jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
				}
				
				
				if(user.getRoleType().ordinal()== RoleType.BANK_STAFF.ordinal())
				{
					
					String hql_stat = "Select count(ac.id) as accountCount, " +
							"count(ec.id) as cardCount from accounts ac " +
							"LEFT JOIN ecards ec ON ec.accountId = ac.id "
							+ "LEFT JOIN issuers iss ON ec.issuer_id "
							+ "LEFT JOIN banks b ON iss.bank_id = b.id " +
							"WHERE b.id = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive;
					System.out.println("hql_stat==" + hql_stat);
					List<Map<String, Object>> counts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql_stat);
					System.out.println("counts==" + counts.size());
					
					/*hql_stat = "Select  sum(trAcct.amount) as trAcctAmt,  sum(trCard.amount) as trCardAmt,  " +
							"sum(trWall.amount) as trWallAmt,  sum(trMob.amount) as trMobAmt  from accounts " +
							"acc LEFT JOIN transactions trAcct ON (trAcct.creditAccountTrue = 0 AND " +
							"trAcct.account_id = acc.id) LEFT JOIN transactions trCard ON (trCard.creditCardTrue = 0 " +
							"AND trCard.account_id = acc.id)  LEFT JOIN transactions trWall ON (trWall.creditWalletTrue = 0 " +
							"AND trWall.account_id = acc.id)  LEFT JOIN transactions trMob ON (trMob.creditMobileAccountTrue = 0 " +
							"AND trMob.account_id = acc.id) " +
							"WHERE trAcct.transactingBankId = " + bankStaff.getBank().getId();*/
					hql_stat = "Select sum(trAcct.amount) as trAcctAmt, trAcct.serviceType  from transactions trAcct where " +
							"trAcct.transactingBankId = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive + 
							" GROUP by trAcct.serviceType";
					System.out.println("hql_stat==" + hql_stat);
					List<Object[]> sumAmounts = (List<Object[]>)swpService.getQueryBySQLResults(hql_stat);
					System.out.println("sumAmounts==" + sumAmounts.size());
					
					
					/*hql_stat = "Select count(ec.id) as successTxn, " +
							"count(ma.id) as paidOutTxn, count(wa.id) as pendTxn, (count(ec.id) + count(ma.id) + count(wa.id)) as allTxn " +
							"from accounts ac " +
							"LEFT JOIN transactions ec ON (ec.account_id = ac.id AND ec.status = " + TransactionStatus.SUCCESS.ordinal() +
							") LEFT JOIN  transactions ma ON (ma.account_id = ac.id AND ec.status =  " + TransactionStatus.PAIDOUT.ordinal() +
							") LEFT JOIN transactions wa ON (wa.account_id = ac.id AND ec.status =  " + TransactionStatus.PENDING.ordinal() +
							") WHERE ac.bank_id = " + bankStaff.getBank().getId();*/
					hql_stat = "Select count(ac.id), ac.status from accounts ac WHERE ac.bank_id = " + bankStaff.getBank().getId() + " AND tp.isLive = " + isLive + 
							" GROUP BY ac.status"; 
					System.out.println("hql_stat==" + hql_stat);
					List<Object[]> statusCounts = (List<Object[]>)swpService.getQueryBySQLResults(hql_stat);
					System.out.println("statusCounts==" + statusCounts.size());
					
					
					Collection<Transaction> lastTransactions = 
							(Collection<Transaction>)this.swpService.getAllRecordsByHQL("Select tp from Transaction tp where tp.transactingBankId = " + 
							bankStaff.getBank().getId() + " AND tp.isLive = " + isLive, 0, 10);
					JSONObject jsonObjectStat = new JSONObject();
					
					Map<String, Object> countIt = counts.get(0);
					jsonObjectStat.put("virtualAcctCount", (BigInteger)countIt.get("accountCount"));
					jsonObjectStat.put("cardCount", (BigInteger)countIt.get("cardCount"));
					
					
					/*for(Object[] sumAmount : sumAmounts){
						jsonObjectStat.put("virtualAcctSum", (Double)sumAmount[0]);
						jsonObjectStat.put("mobileAcctSum", (Double)sumAmount[1]);
						jsonObjectStat.put("cardSum", (Double)sumAmount[2]);
						jsonObjectStat.put("walletAcctSum", (Double)sumAmount[3]);
					}
					
					for(Object[] count : statusCounts){
						jsonObjectStat.put("successTxn", (BigInteger)count[0]);
						jsonObjectStat.put("paidOutTxn", (BigInteger)count[1]);
						jsonObjectStat.put("pendTxn", (BigInteger)count[2]);
						jsonObjectStat.put("allTxn", (BigInteger)count[3]);
					}*/
					
					jsonObjectStat.put("lastTransactions", new Gson().toJson(lastTransactions));
					
					jsonObject.put("dashboardStatistics", jsonObjectStat.toString());
					jsonObject.put("transactionByServiceType", sumAmounts);
					jsonObject.put("transactionByStatus", statusCounts);
					
				}
				
				
				Gson gson = new Gson();
				String obj = gson.toJson(user);
				//Key jwtKey = Application.getKey();
				//String token = Jwts.builder().setSubject(obj).signWith(SignatureAlgorithm.HS512, jwtKey).compact();
				String tkId = RandomStringUtils.randomAlphanumeric(10);
				if(bankStaff!=null)
				{
					token = application.createJWT(tkId, acquirerCode, 
						bankStaff.getBranchCode(), obj, (1*60*60*1000));
				}else if(merchant!=null)
				{
					token = application.createJWT(tkId, acquirerCode, 
							merchant.getBankBranchCode(), obj, (1*60*60*1000));
				}
				else if(customer!=null)
				{
					token = application.createJWT(tkId, acquirerCode, 
							null, obj, (1*60*60*1000));
				}
				else
				{
					token = application.createJWT(tkId, "PROBASE", 
							"999", obj, (1*60*60*1000));
				}
				

				JSONObject allSettings = application.getAllSettings();
				String cardSupport = allSettings.has("cardsupport") ? allSettings.getString("cardsupport") : null;
				
				
				String hql_stat = "Select tp from MPQRData tp where tp.status = " + MPQRDataStatus.ACTIVE.ordinal() + " AND tp.walletAccount.customer.user.id = " + user1.getId() + 
						" AND tp.deleted_at IS NULL"; 
				Collection<MPQRData> allMpqrData = (Collection<MPQRData>)swpService.getAllRecordsByHQL(hql_stat);
				Iterator<MPQRData> allMpqrDataIter = allMpqrData.iterator();
				JSONArray mpqrImages = new JSONArray();
				JSONArray mpqrCorporateImages = new JSONArray();
				while(allMpqrDataIter.hasNext())
				{
					MPQRData mp = allMpqrDataIter.next();
					if(mp.getMpqrDataType()!=null)
					{
						if(mp.getMpqrDataType().equals(MPQRDataType.PERSONAL))
							mpqrImages.put(mp.getQrSaveDataImagePath());
						if(mp.getMpqrDataType().equals(MPQRDataType.CORPORATE))
							mpqrCorporateImages.put(mp.getQrSaveDataImagePath());
					}
					
				}

				hql_stat = "Select tp from GroupMember tp where tp.addedCustomer.user.id = " + user.getId() + " AND  tp.isActive = 1"; 
				Collection<GroupMember> groupMembers = (Collection<GroupMember>)swpService.getAllRecordsByHQL(hql_stat);
				
				
				jsonObject.put("status", ERROR.AUTHENTICATE_OK);
				jsonObject.put("message", "Authentication Ok");
				jsonObject.put("username", user.getUsername());
				jsonObject.put("id", user.getId());
				jsonObject.put("userEmail", user.getUserEmail());
				jsonObject.put("mobileno", user.getMobileNo());
				jsonObject.put("merchant_key", merchant==null ? null : merchant.getMerchantDecryptKey());
				jsonObject.put("merchant_id", merchant==null ? null : merchant.getId());
				jsonObject.put("customerVerificationNo", customer==null ? null : customer.getVerificationNumber());
				jsonObject.put("customer_id", customer==null ? null : customer.getId());
				jsonObject.put("role_code", user.getRoleType());
				if(bankStaff!=null)
				{
					jsonObject.put("acquirer_code", acquirerCode);
					jsonObject.put("branch_code", bankStaff==null ? null : bankStaff.getBranchCode());
				}
				
				if(merchant!=null)
					jsonObject.put("acquirer_code", acquirerCode);
				
				if(customer!=null)
					jsonObject.put("acquirer_code", acquirerCode);
				
				
				jsonObject.put("token", token);
				jsonObject.put("isPinSet", user.getPin()==null ? null : true);
				jsonObject.put("firstName", user.getFirstName()==null ? null : user.getFirstName());
				jsonObject.put("lastName", user.getLastName()==null ? null : user.getLastName());
				jsonObject.put("otherName", user.getOtherName()==null ? null : user.getOtherName());
				System.out.println("user.getPassportImage()==" + (user.getProfilePix()==null ? "NULL" : user.getPassportImage()));
				jsonObject.put("profile_pix", user.getProfilePix()==null ? null : user.getProfilePix());
				jsonObject.put("all_banks", new Gson().toJson(new ArrayList<Bank>(this.application.getAllBanks())));
				jsonObject.put("all_merchant_schemes", new Gson().toJson(new ArrayList<MerchantScheme>(this.application.getAllMerchantSchemes())));
				jsonObject.put("all_device_types", (this.application.getAllDeviceTypes().toString()));
				jsonObject.put("all_card_schemes", new Gson().toJson(new ArrayList<CardScheme>(this.application.getAllCardSchemes())));
				jsonObject.put("all_acquirers", new Gson().toJson(new ArrayList<Acquirer>(this.application.getAllAcquirers())));
				jsonObject.put("all_issuers", new Gson().toJson(new ArrayList<Issuer>(this.application.getAllIssuers())));
				jsonObject.put("all_provinces", new Gson().toJson(new ArrayList<Province>(this.application.getAllProvinces())));
				jsonObject.put("all_districts", new Gson().toJson(new ArrayList<District>(this.application.getAllDistricts())));
				jsonObject.put("all_countries", new Gson().toJson(new ArrayList<Country>(this.application.getAllCountries())));
				jsonObject.put("cardSupport", cardSupport!=null && cardSupport.equals("1") ? true : false);
				jsonObject.put("mpqrImage", mpqrImages);
				jsonObject.put("mpqrCorporateImage", mpqrCorporateImages);
				jsonObject.put("villageBankingMembership", groupMembers.size());
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				if(user!=null)
				{
					hql = "Select tp from User tp where tp.username = '" + user.getUsername() + "'";
					user = (User)this.swpService.getUniqueRecordByHQL(hql);
					user.setFailedLoginCount((user.getFailedLoginCount()==null ? 0 : user.getFailedLoginCount()) + 1);
					if(user.getFailedLoginCount()==3)
					{
						user.setStatus(UserStatus.LOCKED);
						this.swpService.updateRecord(user);
					}else
					{
						this.swpService.updateRecord(user);
					}
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}else
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.error("Exception", e);
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	@POST
	@Path("/authenticateUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateUsers(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("username") String username, 
			@FormParam("encPassword") String encPassword, 
			@FormParam("isMobile") Integer isMobile,
			@FormParam("acquirerCode") String acquirerCode,
			@FormParam("deviceCode") String deviceCode, 
			@FormParam("merchantId") String merchantId, 
			@FormParam("autoAuthenticate") Integer autoAuthenticate)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "Authentication Failed");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return null;
			}
			
			JSONObject allSettings = application.getAllSettings();
			String cardSupport = allSettings.has("cardsupport") ? allSettings.getString("cardsupport") : null;
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("1username: " + username);
			System.out.println("1encPassword: " + encPassword);
			System.out.println("1acquirerCode: " + acquirerCode);
			String acquirerKey = acquirerKeys.getString(acquirerCode);
			//bankKey = new String(Base64.decodeBase64(bankKey.getBytes()));
			String password = UtilityHelper.decryptData(encPassword, acquirerKey).toString();
			System.out.println("Password: " + password);
			
			String hql = "Select tp from User tp where tp.username = '" + username + "' AND password = '" + password + "' AND tp.deleted_at IS NULL";
					// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			String otp = "1111";
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					
				}
				else
				{
					if(user.getActivateMobilePin()!=null && user.getActivateMobilePin().equals(Boolean.TRUE))
					{
						if(isMobile!=null && isMobile.equals(1))
						{
							user.setOtp(otp);
							//user.setOtp(RandomStringUtils.randomNumeric(4));
						}
						else
						{
							user.setOtp(otp);
							//user.setOtp(RandomStringUtils.randomNumeric(4));
						}
					}
					else
					{
						if(isMobile!=null && isMobile.equals(1))
						{
							user.setOtp(otp);
							//user.setOtp(RandomStringUtils.randomNumeric(4));
						}
						else
						{
							user.setOtp(otp);
							//user.setOtp(RandomStringUtils.randomNumeric(4));
						}
					}
					
					
					
					
				}
				user.setFailedLoginCount(0);
				user.setUpdated_at(new Date());
				this.swpService.updateRecord(user);
				
				BankStaff bankStaff = null;
				Merchant merchant = null;
				Customer customer = null;
				System.out.println("RoleType = " + user.getRoleType() + " :: Merchant = " + RoleType.MERCHANT);
				System.out.println("RoleType = " + user.getRoleType() + " :: BankStaff = " + RoleType.BANK_STAFF);
				if(user.getRoleType().ordinal() == (RoleType.BANK_STAFF.ordinal()))
				{
					hql = "Select tp from BankStaff tp where tp.user.id = " + user.getId();
					System.out.println("hql = >" + hql);
					bankStaff = (BankStaff)this.swpService.getUniqueRecordByHQL(hql);
				}
				if(user.getRoleType().ordinal() == (RoleType.MERCHANT.ordinal()))
				{
					hql = "Select tp from Merchant tp where tp.user.id = " + user.getId();
					System.out.println("hql = >" + hql);
					merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
				}
				if(user.getRoleType().ordinal() == (RoleType.CUSTOMER.ordinal()))
				{
					hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
					System.out.println("hql = >" + hql);
					customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
				}
				
				
				
				
				Gson gson = new Gson();
				String obj = gson.toJson(user);
				System.out.println("tkobj ==" + obj);
				//Key jwtKey = Application.getKey();
				//String token = Jwts.builder().setSubject(obj).signWith(SignatureAlgorithm.HS512, jwtKey).compact();
				String tkId = RandomStringUtils.randomAlphanumeric(10);
				System.out.println("tkId ==" + tkId);
				String token = "";
				if(bankStaff!=null)
				{
					
					token = application.createJWT(tkId, acquirerCode, 
						null, obj, (1*60*60*1000));
				}else if(merchant!=null)
				{
					token = application.createJWT(tkId, acquirerCode, 
							null, obj, (1*60*60*1000));
				}
				else if(customer!=null)
				{
					if(user.getActivateMobilePin()!=null && user.getActivateMobilePin().equals(Boolean.TRUE))
					{
						
						token = application.createJWT(tkId, acquirerCode, 
								null, obj, (3*60*1000));
					}
					else
					{
						jsonObject.put("id", user.getId());
						token = application.createJWT(tkId, acquirerCode, 
							null, obj, (1*60*60*1000));
					}
					
				}
				else
				{
					token = application.createJWT(tkId, "PROBASE", 
							null, obj, (1*60*60*1000));
				}
				
				
				if(isMobile!=null && isMobile.equals(1) && user.getActivateMobilePin()!=null && user.getActivateMobilePin().equals(Boolean.TRUE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_OK_PIN_NEEDED);
					jsonObject.put("message", "Authentication Ok. Enter Secure Pin");
					jsonObject.put("username", user.getUsername());
					jsonObject.put("role_code", user.getRoleType());
					if(bankStaff!=null)
					{
						jsonObject.put("acquirer_code", acquirerCode);
						jsonObject.put("branch_code", bankStaff==null ? null : bankStaff.getBranchCode());
					}
					
					if(merchant!=null)
						jsonObject.put("acquirer_code", acquirerCode);
					
					if(customer!=null)
						jsonObject.put("acquirer_code", acquirerCode);
					
					
					jsonObject.put("merchant_key", merchant==null ? null : merchant.getMerchantDecryptKey());
					jsonObject.put("merchant_id", merchant==null ? null : merchant.getId());
					jsonObject.put("customer_id", customer==null ? null : customer.getId());
					jsonObject.put("token", token);
					jsonObject.put("firstName", user.getFirstName()==null ? null : user.getFirstName());
					jsonObject.put("lastName", user.getLastName()==null ? null : user.getLastName());
					jsonObject.put("otherName", user.getOtherName()==null ? null : user.getOtherName());
					jsonObject.put("profile_pix", user.getProfilePix()==null ? null : user.getProfilePix());
					jsonObject.put("all_banks", new Gson().toJson(new ArrayList<Bank>(this.application.getAllBanks())));
					jsonObject.put("all_merchant_schemes", new Gson().toJson(new ArrayList<MerchantScheme>(this.application.getAllMerchantSchemes())));
					jsonObject.put("all_device_types", (this.application.getAllDeviceTypes().toString()));
					jsonObject.put("all_card_schemes", new Gson().toJson(new ArrayList<CardScheme>(this.application.getAllCardSchemes())));
					jsonObject.put("all_provinces", new Gson().toJson(new ArrayList<Province>(this.application.getAllProvinces())));
					jsonObject.put("all_districts", new Gson().toJson(new ArrayList<District>(this.application.getAllDistricts())));
					jsonObject.put("all_countries", new Gson().toJson(new ArrayList<Country>(this.application.getAllCountries())));
					jsonObject.put("otp", user.getOtp());
					jsonObject.put("otprecmobile", user.getMobileNo());
					jsonObject.put("activateMobilePin", user.getActivateMobilePin());
					jsonObject.put("cardSupport", cardSupport!=null && cardSupport.equals("1") ? true : false);
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					if(isMobile!=null && isMobile.equals(1) && user.getActivateMobilePin()!=null && user.getActivateMobilePin().equals(Boolean.FALSE))
					{
						user.setOtp("111");
						this.swpService.updateRecord(user);
						return this.authenticateUserVerifyOTP(httpHeaders, requestContext, token, deviceCode, merchantId, UtilityHelper.encryptData(user.getOtp(), acquirerKey));
					}
					else
					{
						if((user.getActivateOTPLogin()!=null && user.getActivateOTPLogin().equals(Boolean.TRUE)) || (user.getOtp()!=null))
						{
	
							jsonObject.put("status", ERROR.AUTHENTICATE_OK_PIN_NEEDED);
							jsonObject.put("message", "Authentication Ok. Enter Secure Pin");
							jsonObject.put("username", user.getUsername());
							jsonObject.put("role_code", user.getRoleType());
							if(bankStaff!=null)
							{
								jsonObject.put("acquirer_code", acquirerCode);
								jsonObject.put("branch_code", bankStaff==null ? null : bankStaff.getBranchCode());
							}
							
							if(merchant!=null)
								jsonObject.put("acquirer_code", acquirerCode);
							
							if(customer!=null)
								jsonObject.put("acquirer_code", acquirerCode);
							
							
							jsonObject.put("merchant_key", merchant==null ? null : merchant.getMerchantDecryptKey());
							jsonObject.put("merchant_id", merchant==null ? null : merchant.getId());
							jsonObject.put("customer_id", customer==null ? null : customer.getId());
							jsonObject.put("token", token);
							jsonObject.put("firstName", user.getFirstName()==null ? null : user.getFirstName());
							jsonObject.put("lastName", user.getLastName()==null ? null : user.getLastName());
							jsonObject.put("otherName", user.getOtherName()==null ? null : user.getOtherName());
							jsonObject.put("profile_pix", user.getProfilePix()==null ? null : user.getProfilePix());
							jsonObject.put("all_banks", new Gson().toJson(new ArrayList<Bank>(this.application.getAllBanks())));
							jsonObject.put("all_merchant_schemes", new Gson().toJson(new ArrayList<MerchantScheme>(this.application.getAllMerchantSchemes())));
							jsonObject.put("all_device_types", (this.application.getAllDeviceTypes().toString()));
							jsonObject.put("all_card_schemes", new Gson().toJson(new ArrayList<CardScheme>(this.application.getAllCardSchemes())));
							jsonObject.put("all_provinces", new Gson().toJson(new ArrayList<Province>(this.application.getAllProvinces())));
							jsonObject.put("all_districts", new Gson().toJson(new ArrayList<District>(this.application.getAllDistricts())));
							jsonObject.put("all_countries", new Gson().toJson(new ArrayList<Country>(this.application.getAllCountries())));
							jsonObject.put("otp", user.getOtp());
							jsonObject.put("otprecmobile", user.getMobileNo());
							jsonObject.put("activateMobilePin", user.getActivateMobilePin());
							jsonObject.put("cardSupport", cardSupport!=null && cardSupport.equals("1") ? true : false);
							
							SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
							
							if(autoAuthenticate!=null && autoAuthenticate==1)
							{
	
								user.setOtp(null);
								this.swpService.updateRecord(user);
							}
							else
							{
								String receipentMobileNumber = user.getMobileNo();
								String smsMessage = "Hello\nYour One-Time Password is " + otp + ". Please enter this OTP to login";
								//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
								//swpService.createNewRecord(smsMsg);
		
								//SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
								//new Thread(smsSender).start();
							}
							return Response.status(200).entity(jsonObject.toString()).build();
							
						}
						else
						{
							user.setOtp("111");
							this.swpService.updateRecord(user);
							return this.authenticateUserVerifyOTP(httpHeaders, requestContext, token, deviceCode, merchantId, UtilityHelper.encryptData(user.getOtp(), acquirerKey));
						}
					}
				}	
				
			}
			else
			{
				//return null;
				hql = "Select tp from User tp where tp.username = '" + username + "'";
				user = (User)this.swpService.getUniqueRecordByHQL(hql);
				if(user!=null)
				{
					user.setFailedLoginCount((user.getFailedLoginCount()==null ? 0 : user.getFailedLoginCount()) + 1);
					if(user.getFailedLoginCount()==3)
					{
						user.setStatus(UserStatus.LOCKED);
						this.swpService.updateRecord(user);
					}else
					{
						this.swpService.updateRecord(user);
					}
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Authentication Failed");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	
	
	@POST
	@Path("/updateLoginOption")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateLoginOption(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token,
			@FormParam("isPinLogin") Integer isPinLogin)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "2-Factor authentication can not be set");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("isPinLogin: " + isPinLogin);
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			String subject = verifyJ.getString("subject");
			User user_ = new Gson().fromJson(subject, User.class);
			
			String hql_ = "Select tp from User tp where tp.id = " + user_.getId();
			User user = (User)this.swpService.getUniqueRecordByHQL(hql_);
			
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is not active. First activate your account before you can update your login settings");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					
				}
				
				
				
				String oldLoginPin = user.getPin();
				if(oldLoginPin!=null)
					System.out.println("oldLoginPin: " + oldLoginPin);
				else
					System.out.println("oldLoginPin: null");
					
				if(oldLoginPin==null && isPinLogin!=null && isPinLogin==1)
				{
					String pin = RandomStringUtils.randomNumeric(4);
					user.setPin(pin);
					user.setActivateMobilePin(true);
					this.swpService.updateRecord(user);
					jsonObject.put("status", ERROR.TWOFA_ENABLED);
					jsonObject.put("message", "2-FA enabled successfully. A new pin has been sent to your mobile number");
					
					String smsMessage = "Hello " + user.getFirstName() + "\n2-FA has been enabled on your Bevura profile\n\nYour new Bevura pin is " + pin;
					SmsSender smsSender = new SmsSender(swpService, smsMessage, user.getMobileNo());
					new Thread(smsSender).start();
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(oldLoginPin!=null && isPinLogin!=null && isPinLogin==1)
				{

					jsonObject.put("status", ERROR.TWOFA_ENABLED);
					jsonObject.put("message", "2-FA is already enabled");
				}
				else
				{
					user.setActivateMobilePin(false);
					user.setPin(null);
					this.swpService.updateRecord(user);
					jsonObject.put("status", ERROR.TWOFA_DISABLED);
					jsonObject.put("message", "2-FA disabled successfully");
				}
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.TWOFA_DISABLED);
				jsonObject.put("message", "2-FA can not be set");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	
	
	
	@POST
	@Path("/postUpdateAuthPin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUpdateAuthPin(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token,
			@FormParam("oldPin") String oldPin,
			@FormParam("newPin") String newPin)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "2-Factor authentication can not be set");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("oldPin: " + oldPin);
			System.out.println("newPin: " + newPin);
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			String subject = verifyJ.getString("subject");
			User user_ = new Gson().fromJson(subject, User.class);
			
			String hql_ = "Select tp from User tp where tp.id = " + user_.getId();
			User user = (User)this.swpService.getUniqueRecordByHQL(hql_);
			
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is not active. First activate your account before you can update your login settings");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					
				}
				
				
					
				if(oldPin!=null && newPin!=null)
				{
					String pin = newPin;
					if(user.getPin()!=null && user.getPin().equals(oldPin))
					{
						user.setPin(pin);
						user.setActivateMobilePin(true);
						this.swpService.updateRecord(user);
						jsonObject.put("status", ERROR.TWOFA_ENABLED);
						jsonObject.put("message", "2-FA pin updated successfully. A new pin has been sent to your mobile number");
						
						String smsMessage = "Hello " + user.getFirstName() + "\nYour new Bevura 2-FA pin  has been updated on your Bevura profile\n\nYour new Bevura pin is " + pin;
						SmsSender smsSender = new SmsSender(swpService, smsMessage, user.getMobileNo());
						new Thread(smsSender).start();
						return Response.status(200).entity(jsonObject.toString()).build();
					}
					else
					{
						jsonObject.put("status", ERROR.TWOFA_DISABLED);
						jsonObject.put("message", "Invalid pin provided. Please provide your correct current 2-FA pin");
					}
					
				}
				else
				{
					user.setActivateMobilePin(false);
					user.setPin(null);
					this.swpService.updateRecord(user);
					jsonObject.put("status", ERROR.TWOFA_DISABLED);
					jsonObject.put("message", "Invalid pin provided. Please provide your correct current 2-FA pin and your new pin");
				}
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.TWOFA_DISABLED);
				jsonObject.put("message", "2-FA pin change was not successful");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	@POST
	@Path("/postUpdateEmailAddress")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUpdateEmailAddress(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token,
			@FormParam("emailAddress") String emailAddress)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "Your email address could not be updated successfully");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("emailAddress: " + emailAddress);
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			String subject = verifyJ.getString("subject");
			User user_ = new Gson().fromJson(subject, User.class);
			
			String hql_ = "Select tp from User tp where tp.id = " + user_.getId();
			User user = (User)this.swpService.getUniqueRecordByHQL(hql_);
			
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is not active. First activate your account before you can update your login settings");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					
				}
				
				
					
				if(emailAddress!=null)
				{
					user.setUserEmail(emailAddress);
					this.swpService.updateRecord(user);
					
					hql_ = "Select tp from Customer tp where tp.user.id = " + user.getId();
					Customer cust = (Customer)this.swpService.getUniqueRecordByHQL(hql_);
					if(cust!=null)
					{
						cust.setContactEmail(emailAddress);
						swpService.updateRecord(cust);
					}
					jsonObject.put("status", ERROR.TWOFA_ENABLED);
					jsonObject.put("message", "Your email address has been updated successfully");
					
					
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					jsonObject.put("status", ERROR.TWOFA_DISABLED);
					jsonObject.put("message", "Your email address could not be updated successfully");
				}
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.TWOFA_DISABLED);
				jsonObject.put("message", "Your email address could not be updated successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	
	
	@POST
	@Path("/postUpdateProfilePicture")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postUpdateProfilePicture(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token,
			@FormParam("fileImageName") String fileImageName)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "Your profile picture could not be updated successfully");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("fileImageName: " + fileImageName);
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			String subject = verifyJ.getString("subject");
			User user_ = new Gson().fromJson(subject, User.class);
			
			String hql_ = "Select tp from User tp where tp.id = " + user_.getId();
			User user = (User)this.swpService.getUniqueRecordByHQL(hql_);
			
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is not active. First activate your account before you can update your login settings");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					
				}
				
				
					
				if(fileImageName!=null)
				{
					user.setProfilePix(fileImageName);
					this.swpService.updateRecord(user);
					
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Your profile picture has been updated successfully");
					
					
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "Your profile picture could not be updated successfully");
				}
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Your profile picture could not be updated successfull");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	@POST
	@Path("/postGetUserSummary")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postGetUserSummary(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "User summary not received");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			String subject = verifyJ.getString("subject");
			User user_ = new Gson().fromJson(subject, User.class);
			
			String hql_ = "Select tp from User tp where tp.id = " + user_.getId();
			User user = (User)this.swpService.getUniqueRecordByHQL(hql_);
			
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is not active. First activate your account before you can update your login settings");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					
				}
				
				
				
				String oldLoginPin = user.getPin();
				if(oldLoginPin!=null)
					System.out.println("oldLoginPin: " + oldLoginPin);
				else
					System.out.println("oldLoginPin: null");
					

				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "User summary received");
				jsonObject.put("userSummary", user.getSummary());
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "User summary not received");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	@POST
	@Path("/validateUsernameForCustomers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateUsername(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("username") String username)
	{
		System.out.println("Hibernate Version" + org.hibernate.Version.getVersionString());
		JSONObject jsonObject = new JSONObject();
		try{

			jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
			jsonObject.put("message", "Authentication Failed");
			
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject acquirerKeys = application.getAccessKeys();
			if(acquirerKeys.length()==0)
			{
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			System.out.println("1username: " + username);
			
			String hql = "Select tp from User tp where tp.username = '" + username + "' AND tp.deleted_at IS NULL";
					// + " AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			
			
			if(user!=null)
			{
				if(user.getStatus().equals(UserStatus.ADMIN_DISABLED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently disabled. Please contact our support team to help you resolve your account");
					return Response.status(200).entity(jsonObject.toString()).build();
					
				}
				else if(user.getStatus().equals(UserStatus.LOCKED))
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "Your account is currently locked. Please contact our support team to help you resolve your account.");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				else if(user.getStatus().equals(UserStatus.INACTIVE))
				{
					
				}
				else
				{
					user.setOtp("1111");
					user.setOtp(RandomStringUtils.randomNumeric(4));
				}
				user.setFailedLoginCount(0);
				user.setUpdated_at(new Date());
				this.swpService.updateRecord(user);
				
				BankStaff bankStaff = null;
				Merchant merchant = null;
				Customer customer = null;
				System.out.println("RoleType = " + user.getRoleType() + " :: Merchant = " + RoleType.MERCHANT);
				System.out.println("RoleType = " + user.getRoleType() + " :: BankStaff = " + RoleType.BANK_STAFF);
				if(user.getRoleType().ordinal() == (RoleType.CUSTOMER.ordinal()))
				{
					hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
					System.out.println("hql = >" + hql);
					customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
				}
				else
				{
					jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
					jsonObject.put("message", "You can not login using this profile. Only Bevura customers can login on this platform. Use the right platform to login");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				
				
				
				
				jsonObject.put("status", ERROR.AUTHENTICATE_OK);
				jsonObject.put("message", "Authentication Ok");
				jsonObject.put("username", user.getUsername());
				jsonObject.put("role_code", user.getRoleType());
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.AUTHENTICATE_FAIL);
				jsonObject.put("message", "Authentication Failed");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}

	
	/**Service Method - authenticateUser
	 * Method for authenticating all users who require 
	 * a login to access resources
	 * 
	 * @param status - MerchantStatus
	 * @return Stringified JSONObject of the list of merchants
	 */
	
	

	
	@GET
	@Path("/forgotPassword")
	@Produces(MediaType.APPLICATION_JSON)
	public Response forgotPassword(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("username") String username,
			@QueryParam("securityQuestionId") Integer securityQuestionId,
			@QueryParam("securityQuestionAnswer") String securityQuestionAnswer)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject bankKeys = application.getAccessKeys();
			if(bankKeys.length()==0)
			{
				jsonObject.put("status", ERROR.NEW_PASSWORD_SET_FAILED);
				jsonObject.put("message", "Password Recovery Failed.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			//Key key = Application.getKey();
			//System.out.println("JWT Key: " + key.toString());
			String bankCode = null;
			
			
			String hql = "Select tp from User tp where tp.username = '" + username + "' " +
					"AND tp.status = '" + UserStatus.ACTIVE.ordinal() + "'";
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			
			String pin = RandomStringUtils.randomNumeric(4);
			
			String tempPassword = RandomStringUtils.randomAlphanumeric(8);
			if(user!=null)
			{
				String securityQuestionAnswerDb = user.getSecurityQuestionAnswer();
				Integer securityQuestionIdDB = user.getSecurityQuestionId();
				
				
				if(!(securityQuestionIdDB==securityQuestionId && securityQuestionAnswer.toLowerCase().equals(securityQuestionAnswerDb.toLowerCase())))
				{
					
					jsonObject.put("status", ERROR.NEW_PASSWORD_SET_FAILED);
					jsonObject.put("message", "Password Recovery Failed. Ensure you provide the correct security question and answer you provided when signing up");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				user.setFailedLoginCount(0);
				user.setPassword(tempPassword);
				user.setPin(pin);
				this.swpService.updateRecord(user);
				
				BankStaff bankStaff = null;
				Merchant merchant = null;
				

				String receipentMobileNumber = user.getMobileNo();
				String smsMessage = "Hello\nYour new temporary password is " + tempPassword + " and your 2FA pin is "+pin+". Please login with your new temporary password. Remember to change this password";
				//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
				//swpService.createNewRecord(smsMsg);

				SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
				new Thread(smsSender).start();
				
				
				jsonObject.put("recmobile", user.getMobileNo());
				jsonObject.put("status", ERROR.NEW_PASSWORD_SET);
				jsonObject.put("message", "Check your mobile phone for a new password");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				jsonObject.put("status", ERROR.NEW_PASSWORD_SET_FAILED);
				jsonObject.put("message", "Password Recovery Failed.");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			log.warn(e);
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	@POST
	@Path("/changePassword")
	@Produces(MediaType.APPLICATION_JSON)
	public Response changePassword(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("currentpassword") String currentpassword, 
			@FormParam("encPassword") String encPassword)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			String bankKey;
			
			bankKey = UtilityHelper.getBankKey(null, swpService);
			
			String cpassword = (String)UtilityHelper.decryptData(currentpassword, bankKey);
			String npassword = (String)UtilityHelper.decryptData(encPassword, bankKey);
			
			System.out.println("VerifyJ = " + verifyJ.toString());
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

			
			
			System.out.println("current password = " + user.getPassword() + " && new password = " + cpassword);
			if(user!=null && user.getPassword().equals(cpassword))
			{
				user.setFailedLoginCount(0);
				user.setLockOut(false);
				user.setOtp(RandomStringUtils.randomNumeric(4));
				user.setPassword(npassword);
				this.swpService.updateRecord(user);
				
				jsonObject.put("recmobile", user.getMobileNo());
				jsonObject.put("status", ERROR.NEW_PASSWORD_SET);
				jsonObject.put("message", "New Password Changed Successfully");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				//return null;
				
				jsonObject.put("status", ERROR.NEW_PASSWORD_SET_FAILED);
				jsonObject.put("message", "Password Change Failed. Incorrect password provided");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	
	
	@POST
	@Path("/getAllNotifications")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllNotifications(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token
			)
	{
		JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("message", "We could not get a list of your notifications. Please try again");
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, application);
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			String bankKey;
			
			bankKey = UtilityHelper.getBankKey(null, swpService);
			
			System.out.println("VerifyJ = " + verifyJ.toString());
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

			String hql = "Select tp.* from notifications tp where tp.userId = " + user.getId() + " AND tp.isRead = 0";
			List<Map<String, Object>> notifications = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("notifications", notifications==null ? null : notifications);
			jsonObject.put("message", "Notifications pulled successfully");
			jsonObject.put("status", ERROR.GENERAL_OK);
			return Response.status(200).entity(jsonObject.toString()).build();
			
			
			
			
		}catch(Exception e)
		{
			log.warn(e);
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}

}
