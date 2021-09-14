package com.probase.probasepay.services;

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

import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.BankStaffStatus;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/BankServices")
public class BankServices {
	private static Logger log = Logger.getLogger(BankServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/**Service Method - Customer signs up mobile money 
	 * on mobile application
	 * 
	 * @return Stringified JSONObject of the list of customers
	 */
	@POST
	@Path("/createNewBank")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject createNewBank(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("bankIdS") String bankIdS, 
			@FormParam("bankName") String bankName, 
			@FormParam("bankCode") String bankCode, 
			@FormParam("onlineBankingUrl") String onlineBankingUrl, 
			@FormParam("token") String token)
	{
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
				return jsonObject;
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String bankCode_ = verifyJ.getString("bankCode");
			System.out.println("staff_bank_code ==" + bankCode_);
			String bankKey = UtilityHelper.getBankKey(bankCode_, swpService);
			
			Integer bankId = null;
			if(bankIdS!=null)
			{
				bankId = (Integer) UtilityHelper.decryptData(bankIdS, bankKey);
				String hql = "Select tp from Bank tp where tp.id = " + bankId;
				bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			}
		
		
		
			String hql = "Select tp from Bank tp where (lower(tp.bankName) = '" + bankName.toLowerCase() + "'" 
					+ " OR lower(tp.bankCode) = '" + bankCode.toLowerCase() + "')";
			if(bankIdS!=null)
			{
				hql = hql + " AND tp.id != " + bankId;
			}
			Bank bankExist = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			
			if(bankExist==null)
			{
				bank.setBankCode(bankCode);
				bank.setBankName(bankName);
				bank.setCreated_at(new Date());
				bank.setOnlineBankingURL(onlineBankingUrl);
				bank = (Bank)this.swpService.createNewRecord(bank);
				
				JSONObject jsonAccessKeys = app.getAccessKeys();
				
				app.setAllBanks((Collection<Bank>)swpService.getAllRecords(Bank.class));
				app.setAccessKeys(jsonAccessKeys);
				
				
				jsonObject.put("status", ERROR.MMONEY_PROFILE_SUCCESS);
				jsonObject.put("message", "New Bank creation was successful");
			}
			else
			{
				jsonObject.put("status", ERROR.MMONEY_PROFILE_SUCCESS);
				jsonObject.put("message", "Bank with bank name or code already exists. New bank could not be created.");
			}
			return jsonObject;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.MMONEY_PROFILE_FAIL);
				jsonObject.put("message", "Pool account profiling failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return jsonObject;
		}
		
	}
	
	/**Service Method - addMobileMoneyToCustomer
	 * Method for adding mobile money to existing customer.
	 * Customer provides card details and mobile number to 
	 * verify and setup a mobile account
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
	 * @return Stringified JSONObject of the list of merchants
	 */
	@GET
	@Path("/listBankTransactions")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listBankTransactions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("bankIdS") String bankIdS, 
			@QueryParam("status") String status, 
			@QueryParam("token") String token, 
			@QueryParam("count") Integer count, 
			@QueryParam("pageIndex") Integer pageIndex)
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			Integer bankId = null;
			Bank bank = null;
			if(bankIdS!=null)
			{
				bankId = (Integer) UtilityHelper.decryptData(bankIdS, bankKey);
				String hql = "Select tp from Bank tp where tp.id = " + bankId;
				bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			}
			
			String hql = "Select tp.* from transactions tp WHERE tp.transactingBankId = " + bankId + "";
			
			if(status!=null)
			{
				hql = hql + " AND tp.status = '" + TransactionStatus.valueOf(status) + "'";
			}
			
			if(count!=null && pageIndex!=null)
				hql = hql + " limit " + (pageIndex*count) + ", " + count;
			
			List<Map<String, Object>> transactionList = null;
			transactionList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Transactions fetch succcessful");
			jsonObject.put("transactionlist", transactionList);
			jsonObject.put("bank", new Gson().toJson(bank));
			System.out.println("Add New Card = " + jsonObject.toString());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	@GET
	@Path("/listBanks")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listBanks(
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
			
			Integer bankId = null;
			Bank bank = null;
			String hql = "Select tp from Bank tp";
			Collection<Bank> bankList = (Collection<Bank>)this.swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Banks fetch succcessful");
			jsonObject.put("bankList", new Gson().toJson(new ArrayList<Bank>(bankList)));
			System.out.println("Add New Card = " + jsonObject.toString());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
	}
	
	@GET
	@Path("/getBankStaffList")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getBankStaffList(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("bankIdS") String bankIdS, 
			@QueryParam("status") String status, 
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
			
			Integer bankId = null;
			Bank bank = null;
			if(bankIdS!=null)
			{
				bankId = (Integer) UtilityHelper.decryptData(bankIdS, bankKey);
				String hql = "Select tp from Bank tp where tp.id = " + bankId;
				bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			}
			
			//BankStaffStatus.ACTIVE = 0;
			//BankStaffStatus.DISABLED = 1;
			//BankStaffStatus.INACTIVE = 2;
			
			
			String hql = "Select tp from BankStaff tp where tp.bank.id = " + bankId;
			if(status!=null)
			{
				hql = hql + " AND tp.status = " + BankStaffStatus.valueOf(status);
			}
			System.out.println("hql ==>" + hql);
			Collection<BankStaff> bankStaffList = (Collection<BankStaff>)this.swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Pool account fetch succcessful");
			jsonObject.put("bankStaffList", new Gson().toJson(new ArrayList<BankStaff>(bankStaffList)));
			jsonObject.put("bank", new Gson().toJson(bank));
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
					
	}
	
	
	@GET
	@Path("/changeBankStaffStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject changeBankStaffStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("bankStaffIdS") String bankStaffIdS, 
			@QueryParam("status") Integer status, 
			@QueryParam("token") String token)
	{
		//BankStaffStatus.ACTIVE = 0;
		//BankStaffStatus.DISABLED = 1;
		//BankStaffStatus.INACTIVE = 2;
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			Integer bankStaffId = null;
			if(bankStaffIdS!=null)
			{
				bankStaffId = (Integer) UtilityHelper.decryptData(bankStaffIdS, bankKey);
			}
			String hql = "Select tp from BankStaff where tp.id = " + bankStaffId;
			BankStaff bankStaff = (BankStaff)this.swpService.getUniqueRecordByHQL(hql);
			User user = bankStaff.getUser();
			
			if(status==0)
			{
				bankStaff.setStatus(BankStaffStatus.ACTIVE);
				this.swpService.updateRecord(bankStaff);
				user.setStatus(UserStatus.ACTIVE);
				this.swpService.updateRecord(bankStaff);
			}else if(status==1)
			{
				bankStaff.setStatus(BankStaffStatus.DISABLED);
				this.swpService.updateRecord(bankStaff);
				user.setStatus(UserStatus.ADMIN_DISABLED);
				this.swpService.updateRecord(bankStaff);
			}
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Bank Staff Status Updated Succcessfully");
			jsonObject.put("bankStaff", new Gson().toJson(bankStaff));
		}catch(Exception e)
		{
			log.warn(e);
		}
		return jsonObject;
					
	}
	
	
	
	
	
	

	

}
