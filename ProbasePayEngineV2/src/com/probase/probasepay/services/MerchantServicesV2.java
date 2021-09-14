package com.probase.probasepay.services;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
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

import javassist.expr.NewArray;

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
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Country;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantBankAccount;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.TutukaHelper;
import com.probase.probasepay.util.UtilityHelper;


@Path("/MerchantServicesV2")
public class MerchantServicesV2 {
	private static Logger log = Logger.getLogger(MerchantServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	@POST
	@Path("/createNewMerchant")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewMerchant(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, 
			@FormParam("altContactEmail") String altContactEmail, @FormParam("altContactMobile") String altContactMobile, @FormParam("certificateOfIncorporation") String certificateOfIncorporation, 
			@FormParam("companyData") String companyData, @FormParam("companyLogo") String companyLogo, @FormParam("companyName") String companyName, 
			@FormParam("companyRegNo") String companyRegNo, @FormParam("contactEmail") String contactEmail, @FormParam("contactMobile") String contactMobile, 
			@FormParam("merchantName") String merchantName, @FormParam("merchantId") String merchantId, 
			@FormParam("token") String token, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName, 
			@FormParam("otherName") String otherName, @FormParam("operationCountry") Long operationCountry)
	{
		//updateStatus = 0 : create
		//updateStatus = 1 : update
		Merchant merchant = null;
		org.hibernate.Transaction txn = null;
		if(merchantId== null)
		{
			merchant = new Merchant();
		}else
		{
			try {
				this.swpService = this.serviceLocator.getSwpService();
				String bankKey = UtilityHelper.getBankKey(null, swpService);
				Integer merchantIdI = (Integer) UtilityHelper.decryptData(merchantId, bankKey);
				String hql_ = "Select tp from Merchant tp where tp.id = " + merchantIdI;
				System.out.println("1. hql_ = " + hql_);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql_);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.warn(e);
				merchant = new Merchant();
			}
		}
		JSONObject jsonObject = new JSONObject();
		System.out.println("Create New merchant");
		String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
		String mobileActivationCode = RandomStringUtils.randomNumeric(6);
		try {
			this.swpService = this.serviceLocator.getSwpService();
			txn = this.swpService.getStartTransaction();
			Application app = Application.getInstance(swpService);
			System.out.println("token111 ==" + token);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			System.out.println("tokenUser.getRoleType()....=" + tokenUser.getRoleType().name());
			
			String hql = "Select tp from Merchant tp where lower(tp.companyName) = '" + companyName + "'";
			System.out.println("hql = " + hql);
			Merchant mer = (Merchant)this.swpService.getUniqueRecordByHQL(hql);
			
			
					
			
			
			
			if(mer==null)
			{
				hql = "Select tp from Country tp where lower(tp.id) = " + operationCountry;
				System.out.println("hql = " + hql);
				Country country = (Country)this.swpService.getUniqueRecordByHQL(hql);
				if(country==null)
				{
					jsonObject.put("status", ERROR.COUNTRY_OF_OPERATION_NOT_PROVIDED);
					jsonObject.put("message", "Country of operation not provided");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				System.out.println("country = " + country.getName());
				
				merchant.setAddressLine1(addressLine1);
				merchant.setAddressLine2(addressLine2);
				merchant.setAltContactEmail(altContactEmail);
				merchant.setAltContactMobile(altContactMobile);
				merchant.setCertificateOfIncorporation(certificateOfIncorporation);
				merchant.setCompanyData(companyData);
				merchant.setCompanyLogo(companyLogo);
				merchant.setCompanyRegNo(companyRegNo);
				merchant.setContactMobile(contactMobile);
				merchant.setContactEmail(contactEmail);
				if(merchantId== null)
				{
					String merchantCode = RandomStringUtils.randomAlphanumeric(10).toUpperCase(); 
					merchant.setMerchantCode(merchantCode);
					merchant.setStatus(MerchantStatus.ACTIVE);
					merchant.setCompanyName(companyName);
					merchant.setMerchantName(merchantName);
					merchant.setApiKey(RandomStringUtils.randomAlphanumeric(32));
					merchant.setMerchantDecryptKey(RandomStringUtils.randomAlphanumeric(32));
				}
				merchant.setCreated_at(new Date());
				merchant.setUpdated_at(new Date());
				merchant.setMobileActivationCode(mobileActivationCode);
				merchant.setWebActivationCode(webActivationCode);
				merchant.setCountryOfOperation(country);
				
				if(merchantId== null)
				{
					/*Create new user account for this merchant*/
					
					if(tokenUser!=null && (tokenUser.getRoleType().equals(RoleType.POTZR_STAFF) || tokenUser.getRoleType().equals(RoleType.BANK_STAFF)))
					{
						this.swpService.createNewRecord(merchant);
						System.out.println("user is null ");
						User user = new User();
						
						user.setStatus(UserStatus.ACTIVE);
						user.setRoleType(RoleType.MERCHANT);
						String pswd = RandomStringUtils.randomAlphanumeric(8);
						
							user.setPassword(pswd);
							//user.setWebActivationCode(webActivationCode);
							//user.setMobileActivationCode(mobileActivationCode);
							user.setFailedLoginCount(0);
							user.setUsername(contactEmail);
							user.setLockOut(Boolean.FALSE);
							user.setCreated_at(new Date());
						
						user.setFirstName(firstName);
						user.setLastName(lastName);
						user.setOtherName(otherName);
						user.setMobileNo(contactMobile);
						user = (User)this.swpService.createNewRecord(user);
						
						merchant.setUser(user);
						this.swpService.updateRecord(merchant);
						
						
						jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
						jsonObject.put("message", "New merchant created");
						jsonObject.put("merchantCode", merchant.getMerchantCode());
						jsonObject.put("merchantId", merchant.getId());
						jsonObject.put("password", pswd);
						jsonObject.put("contactMobile", contactMobile);
						jsonObject.put("message", "New merchant created");
						
						System.out.println("Create New merchant = " + jsonObject.toString());
					}
					else if(tokenUser!=null && (tokenUser.getRoleType().equals(RoleType.CUSTOMER)))
					{
						jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
						jsonObject.put("message", "New merchant created");
						jsonObject.put("merchantCode", merchant.getMerchantCode());
						jsonObject.put("merchantId", merchant.getId());
						jsonObject.put("contactMobile", contactMobile);
						jsonObject.put("message", "New merchant created");
						merchant.setUser(tokenUser);
						this.swpService.updateRecord(merchant);
					}
					else
					{
						txn.rollback();
						jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS_NO_USER_ACCOUNT);
						jsonObject.put("message", "Invalid action. You must be logged in to setup a merchant account.");
					}
				}
				else
				{
					//UPDATE ACTION
					this.swpService.updateRecord(merchant);
					User user = merchant.getUser();
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setOtherName(otherName);
					user.setMobileNo(contactMobile);
					this.swpService.updateRecord(user);
					txn.rollback();
					jsonObject.put("merchantCode", merchant.getMerchantCode());
					jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
					jsonObject.put("message", "Merchant Profile Update Successful");
				}
				
				
				jsonObject.put("token", verifyJ.getString("token"));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("status", ERROR.MERCHANT_SETUP_COMPANY_NAME_EXIST);
				jsonObject.put("message", "Company Name already exist. Provide another company name.");
				jsonObject.put("token", verifyJ.getString("token"));
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
			txn.rollback();
			try {
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				log.warn(e);
				System.out.println("Create New merchant Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			txn.rollback();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				log.warn(e);
				System.out.println("Create New merchant Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	@GET
	@Path("/getMerchantTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMerchantTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantId") Long merchantId, 
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
			
			
			String hql = "Select tp.* from transactions tp, merchants m where tp.merchantId = m.id AND tp.merchantId IS NOT NULL ";
			if(merchantId!=null)
			{
				hql = hql + " AND m.id = "+(merchantId)+"";
				
			}
			System.out.println(hql);
			List<Map<String, Object>> merchanttransactionslist = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			
			if(merchantId!=null)
			{
				hql = "Select tp.* from merchants tp where tp.id = "+(merchantId)+"";
				System.out.println(hql);
				
				List<Map<String, Object>> merchant_ = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				Map<String, Object> merchant = merchant_.get(0);
				
				JSONObject merchantJson = new JSONObject();
				merchantJson.put("id", (BigInteger)merchant.get("id"));
				merchantJson.put("merchantName", (String)merchant.get("merchantName"));
				merchantJson.put("merchantCode", (String)merchant.get("merchantCode"));
				jsonObject.put("merchant", merchantJson);
			}
			
			
			
			
			
			
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant transactions fetched");
			jsonObject.put("merchanttransactionslist", merchanttransactionslist);
			System.out.println("verify3 ==");
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage(), e);
			e.printStackTrace();
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant transactions fetch Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	@POST
	@Path("/updateMerchantScheme")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateMerchantScheme(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("merchantScheme") Long merchantScheme, @FormParam("merchantBank") Long merchantBank, 
			@FormParam("bankAccountName") String bankAccountName, @FormParam("bankAccountNo") String bankAccountNo, @FormParam("bankBranchCode") String bankBranchCode, 
			@FormParam("merchantCode") String merchantCode, @FormParam("token") String token) throws JSONException
	{
		//updateStatus = 0 : create
		//updateStatus = 1 : update
		JSONObject jsonObject = new JSONObject();
		Merchant merchant = null;
		org.hibernate.Transaction txn = null;
		
		try {
			this.swpService = this.serviceLocator.getSwpService();
			String bankKey = UtilityHelper.getBankKey(null, swpService);
			String hql_ = "Select tp from Merchant tp where tp.	merchantCode = '" + merchantCode + "'";
			System.out.println("1. hql_ = " + hql_);
			merchant = (Merchant)swpService.getUniqueRecordByHQL(hql_);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
			jsonObject.put("message", "Merchant account not found. Ensure you select a valid merchant to update");
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		System.out.println("Create New merchant");
		String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
		String mobileActivationCode = RandomStringUtils.randomNumeric(6);
		try {
			this.swpService = this.serviceLocator.getSwpService();
			txn = this.swpService.getStartTransaction();
			Application app = Application.getInstance(swpService);
			System.out.println("token111 ==" + token);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("issuerBankCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			String subject = verifyJ.getString("subject");
			System.out.println("subject ==" + subject);
			User tokenUser = new Gson().fromJson(subject, User.class);
			System.out.println("tokenUser.getRoleType()....=" + tokenUser.getRoleType().name());
			
			String hql = "";
			
			if(tokenUser.getRoleType().equals(RoleType.CUSTOMER))
			{
				hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL";
				Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from MerchantScheme tp where tp.id = " + acquirer.getDefaultMerchantSchemeId();
			}
			else
			{
				hql = "Select tp from MerchantScheme tp where lower(tp.id) = " + merchantScheme;
			}
			System.out.println("hql = " + hql);
			MerchantScheme ms = (MerchantScheme)this.swpService.getUniqueRecordByHQL(hql);
			if(ms==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_SCHEME_NOT_FOUND);
				jsonObject.put("message", "Merchant scheme not found");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			System.out.println("country = " + ms.getSchemename());
			
			hql = "Select tp from Bank tp where lower(tp.id) = " + merchantBank;
			System.out.println("hql = " + hql);
			Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			if(bank==null)
			{
				jsonObject.put("status", ERROR.INVALID_BANK_PROVIDED);
				jsonObject.put("message", "Invalid bank provided. Provide a valid bank");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			System.out.println("bank = " + bank.getBankName());
			
			
			
			merchant.setMerchantBank(bank);
			merchant.setMerchantScheme(ms);
			merchant.setBankBranchCode(bankBranchCode);
			
			MerchantBankAccount mba = null;
			hql = "Select tp from MerchantBankAccount tp where lower(tp.merchant.merchantCode) = '" + merchantCode + "' AND tp.status != 1";
			System.out.println("hql = " + hql);
			Collection<MerchantBankAccount> mbas = (Collection<MerchantBankAccount>)this.swpService.getAllRecordsByHQL(hql);
			Iterator<MerchantBankAccount> itMba = mbas.iterator();
			while(itMba.hasNext())
			{
				MerchantBankAccount mba_ = itMba.next();
				mba_.setStatus(Boolean.FALSE);
				this.swpService.updateRecord(mba_);
			}
			
			mba = new MerchantBankAccount();
			mba.setMerchant(merchant);
			mba.setBankAccountName(bankAccountName);
			mba.setBankAccountNumber(bankAccountNo);
			mba.setBankBranchCode(bankBranchCode);
			mba.setCreated_at(new Date());
			mba.setMerchant(merchant);
			mba.setMerchantBank(bank);
			mba.setStatus(Boolean.TRUE);
			mba.setUpdated_at(new Date());
			this.swpService.createNewRecord(mba);
			
			merchant.setBankAccount(bankAccountNo);
			merchant.setMerchantBank(bank);
			merchant.setAutoReturnToMerchantSite(Boolean.TRUE);
			merchant.setMerchantScheme(ms);
			this.swpService.updateRecord(merchant);
			
			jsonObject.put("status", ERROR.MERCHANT_SETUP_SUCCESS);
			jsonObject.put("message", "Merchant bank account details added/updated");
			
			System.out.println("Create New merchant = " + jsonObject.toString());
			
			
			jsonObject.put("token", verifyJ.getString("token"));
			return Response.status(200).entity(jsonObject.toString()).build();
			
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			log.warn(e);
			e.printStackTrace();
			txn.rollback();
			try {
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				log.warn(e);
				System.out.println("Create New merchant Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			txn.rollback();
			e.printStackTrace();
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.MERCHANT_SETUP_FAIL);
				jsonObject.put("message", "Merchant Profile Update Failed");
				log.warn(e);
				System.out.println("Create New merchant Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	@GET
	@Path("/getMerchantByMerchantName")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMerchantByMerchantName(@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("merchantName") String merchantName, 
			@QueryParam("merchantId") Long merchantId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			List<BigInteger> merchantList = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(merchantId==null)
			{
				String merchantSql = "Select tp.id from merchants tp where tp.merchantName = '"+ merchantName +"'";
				merchantList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			else
			{
				String merchantSql = "Select tp.id from merchants tp where tp.merchantName = '"+ merchantName +"' AND tp.id != " + merchantId;
				merchantList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			
				
			
			if(merchantList.size()>0)
			{
				jsonObject.put("message", "Merchant matching merchant name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			}
			else
			{
				jsonObject.put("message", "No merchant matching merchant name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
			}
			
			
			if(verifyJ.has("active") && verifyJ.getInt("active")==1)
			{
				System.out.println("Token ==" + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
		
	
	/**Service Method - listMerchants
	 * 
	 * @param status - MerchantStatus
	 * @return Stringified JSONObject of the list of merchants
	 */
	
	@GET
	@Path("/listMerchantsV2")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listMerchantsV2(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("status") String status, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			List<Object[]> merchantList = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
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
			
			/*if(status==null)
				merchantList = (Collection<Merchant>)swpService.getAllRecords(Merchant.class);
			else
				merchantList = (Collection<Merchant>)swpService.getAllRecordsByHQL("Select tp from Merchant tp where tp.status = "+(MerchantStatus.valueOf(status).ordinal())+"");*/
			if(status!=null)
			{
				String hql = "Select tp.id, tp.merchantName, tp.contactEmail, tp.merchantCode, tp.bankAccount, tp.status, b.bankName, u.lastName, u.firstName, u.otherName  "
						+ "from merchants tp, banks b, users u where tp.deleted_at IS NULL AND tp.merchantBank_id = b.id AND tp.user_id = u.id AND tp.status = " + MerchantStatus.valueOf(status).ordinal() + 
						"  ORDER BY tp.merchantName";
				
				merchantList = (List<Object[]>)swpService.getQueryBySQLResults(hql);
			}
			else
			{
				String hql = "Select tp.id, tp.merchantName, tp.contactEmail, tp.merchantCode, tp.bankAccount, tp.status, b.bankName, u.lastName, u.firstName, u.otherName  "
						+ "from merchants tp, banks b, users u where tp.deleted_at IS NULL AND tp.merchantBank_id = b.id AND tp.user_id = u.id ORDER BY tp.merchantName";
				merchantList = (List<Object[]>)swpService.getQueryBySQLResults(hql);
			}
			
			/*Iterator<Merchant> iterMerchant = merchantList.iterator();
			JSONArray allMerchants = new JSONArray();
			while(iterMerchant.hasNext())
			{
				Merchant merchant = iterMerchant.next();
				JSONObject oneMerchant = new JSONObject();
				oneMerchant.put("id", merchant.getId());
				oneMerchant.put("merchantName", merchant.getMerchantName());
				oneMerchant.put("contactEmail", merchant.getContactEmail());
				oneMerchant.put("merchantCode", merchant.getMerchantCode());
				oneMerchant.put("bankAccount", merchant.getBankAccount());
				oneMerchant.put("bank", merchant.getMerchantBank()==null ? "" : merchant.getMerchantBank().getBankName());
				oneMerchant.put("primaryContactPerson", merchant.getUser()==null ? "" : (merchant.getUser().getLastName() + " " + merchant.getUser().getFirstName()));
				oneMerchant.put("status", merchant.getStatus());
				allMerchants.put(oneMerchant);
			}*/
				
			jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			jsonObject.put("message", "Merchant list fetched");
			jsonObject.put("merchantlist", merchantList);
			
			
			if(verifyJ.has("active") && verifyJ.getInt("active")==1)
			{
				System.out.println("Token ==" + verifyJ.getString("token"));
				jsonObject.put("token", verifyJ.getString("token"));
			}
			
			return Response.status(200).entity(jsonObject.toString()).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error(">>", e);
			try {
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_FAIL);
				jsonObject.put("message", "Merchant creation Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	

}
