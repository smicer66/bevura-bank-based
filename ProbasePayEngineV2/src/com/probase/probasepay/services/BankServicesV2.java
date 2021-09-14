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
import com.probase.probasepay.models.BankBranch;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Acquirer;
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


@Path("/BankServicesV2")
public class BankServicesV2 {
	private static Logger log = Logger.getLogger(BankServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	

	@GET
	@Path("/listBanks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listBanks(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("isBicCodeCompulsory") Integer isBicCodeCompulsory)
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
			
			
			
			Integer bankId = null;
			Bank bank = null;
			String hql = "Select tp.id, tp.bankName, tp.bankCode, tp.onlineBankingURL from banks tp WHERE tp.deleted_at IS NULL";
			if(isBicCodeCompulsory!=null && isBicCodeCompulsory==1)
			{
				hql = hql + " AND tp.bicCode IS NOT NULL";
			}
			if(isBicCodeCompulsory!=null && isBicCodeCompulsory==0)
			{
				hql = hql + " AND tp.bicCode IS NULL";
			}
			hql = hql + " ORDER BY tp.bankName ASC";
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
	
	
	@GET
	@Path("/listBankBranches")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listBankBranches(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("bankCode") String bankCode)
	{

		JSONObject jsonObject = new JSONObject();
		
		try
		{
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Bank list could not be fetched");
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
			
			
			Bank bank = null;
			String hql = "Select tp.bankId, tp.bicCode, tp.branchDetails, tp.sortCode, bk.id, bk.bankName, bk.bankCode from bank_branches tp, banks bk where tp.bankId = bk.id AND tp.deleted_at IS NULL AND bk.bicCode IS NOT NULL";
			if(bankCode!=null)
			{
				hql = hql + " AND bk.bankCode = '" + bankCode + "'";
			}
			hql = hql + " ORDER BY bk.bankName ASC, tp.branchDetails ASC";
			List<Map<String, Object>> bankBranchList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Bank Branches fetch succcessful");
			jsonObject.put("bankBranchList", (bankBranchList));
			System.out.println("Add Branches = " + jsonObject.toString());
		}
		catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	@GET
	@Path("/updateBankBranches")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateBankBranches(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("token") String token, 
			@QueryParam("deviceCode") String deviceCode)
	{

		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Update was not successful");
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
			String hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
			System.out.println("hql device => " + hql);
		    Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			
			String[] newBankBranchesResp = UtilityHelper.getCurrentBankBranches(token, app, acquirer, device, jsonObject);
			if(newBankBranchesResp!=null && newBankBranchesResp.length>0 && newBankBranchesResp[0].equals("1"))
			{
				String newBankBranchesStr = newBankBranchesResp[2];
				JSONArray newBankBranches = new JSONArray(newBankBranchesStr);
				for(int k= 0; k<newBankBranches.length(); k++)
				{
					String sortCode = newBankBranches.getJSONObject(k).getString("sortCode");
					String bicCode = newBankBranches.getJSONObject(k).getString("bicCode");
					String branchDetails = newBankBranches.getJSONObject(k).getString("branchDesc");
					
					String hql1 = "Select tp from BankBranch tp where lower(tp.sortCode) = '"+sortCode.toLowerCase()+"' AND tp.deleted_at IS NULL";
					BankBranch bb = (BankBranch)this.swpService.getUniqueRecordByHQL(hql1);
					

					hql1 = "Select tp from Bank tp where lower(tp.bicCode) = '"+bicCode.toLowerCase()+"' AND tp.deleted_at IS NULL";
					Bank bk = (Bank)this.swpService.getUniqueRecordByHQL(hql1);
					
					if(bb==null)
					{
						bb = new BankBranch();
						bb.setBankId(bk.getId());
						bb.setBicCode(bicCode);
						bb.setBranchDetails(branchDetails);
						bb.setCreated_at(new Date());
						bb.setUpdated_at(new Date());
						bb.setSortCode(sortCode);
						this.swpService.createNewRecord(bb);
					}
					else
					{
						bb.setBankId(bk.getId());
						bb.setBicCode(bicCode);
						bb.setBranchDetails(branchDetails);
						bb.setCreated_at(new Date());
						bb.setUpdated_at(new Date());
						bb.setSortCode(sortCode);
						this.swpService.updateRecord(bb);
					}
					
				}
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Bank Branches updated succcessfully");
			}
			else
			{

				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Bank Branches not updated succcessfully");
			}
			
			
		}
		catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	
	@GET
	@Path("/listAcquirers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAcquirers(
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
			String hql = "Select tp.* from acquirer tp";
			List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Acquirer fetch succcessful");
			jsonObject.put("acquirerList", (bankList));
			System.out.println("Add New Card = " + jsonObject.toString());
		}
		catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	@GET
	@Path("/listIssuers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listIssuers(
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
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			Integer bankId = null;
			Bank bank = null;
			String hql = "Select tp.id, b.bankName, b.bankCode, tp.issuerCode, tp.issuerName, tp.accountCreationDemoEndPoint, "
					+ "tp.accountCreationEndPoint, tp.balanceInquiryDemoEndPoint, tp.balanceInquiryEndPoint, tp.fundsTransferDemoEndPoint, tp.fundsTransferEndPoint, "
					+ "tp.holdFundsYes, tp.isLive, tp.serviceKey, tp.authKey, tp.demoAuthKey, "
					+ "tp.demoServiceKey from issuers tp, banks b where tp.bank_id = b.id";
			List<Map<String, Object>> bankList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResults(hql);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Issuer fetch succcessful");
			jsonObject.put("issuerList", (bankList));
			System.out.println("Add New Card = " + jsonObject.toString());
		}
		catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	@GET
	@Path("/getBankByBankName")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBankByBankName(@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("bankName") String bankName, 
			@QueryParam("bankId") Long bankId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			List<BigInteger> bankList = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(bankId==null)
			{
				String merchantSql = "Select tp.id from banks tp where lower(tp.bankName) = '"+ bankName.toLowerCase() +"'";
				bankList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			else
			{
				String merchantSql = "Select tp.id from banks tp where lower(tp.bankName) = '"+ bankName.toLowerCase() +"' AND tp.id != " + bankId;
				bankList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			
				
			
			if(bankList.size()>0)
			{
				jsonObject.put("message", "Bank matching bank name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			}
			else
			{
				jsonObject.put("message", "No bank matching bank name found");
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
				jsonObject.put("message", "Bank search Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	@GET
	@Path("/getIssuerByIssuerNameOrIssuerCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBankByBankName(@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@QueryParam("issuerName") String issuerName, 
			@QueryParam("issuerCode") String issuerCode, 
			@QueryParam("issuerId") Long issuerId, 
			@QueryParam("token") String token)
	{
		JSONObject jsonObject = new JSONObject();
		try {
			swpService = serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			List<BigInteger> issuerList = null;
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ ==" + verifyJ.toString());
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(issuerId==null)
			{
				String merchantSql = "Select tp.id from banks tp where (lower(tp.issuerName) = '"+ issuerName.toLowerCase() +"') OR (lower(tp.bankName) = '"+ issuerCode.toLowerCase() +"')";
				issuerList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			else
			{
				String merchantSql = "Select tp.id from banks tp where (lower(tp.bankName) = '"+ issuerName.toLowerCase() +"' OR (lower(tp.bankName) = '"+ issuerCode.toLowerCase() +"')) AND tp.id != " + issuerId;
				issuerList = (List<BigInteger>)swpService.getQueryBySQLResults(merchantSql);
			}
			
				
			
			if(issuerList.size()>0)
			{
				jsonObject.put("message", "Issuer matching issuer name found");
				jsonObject.put("status", ERROR.MERCHANT_LIST_FETCH_SUCCESS);
			}
			else
			{
				jsonObject.put("message", "No issuerList matching issuer name found");
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
				jsonObject.put("message", "Bank search Failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				log.warn(e);
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	@POST
	@Path("/createNewBank")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewBank(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("bankId") Long bankId, 
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
				return Response.status(200).entity(jsonObject.toString()).build();
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
			
			if(bankId!=null)
			{
				String hql = "Select tp from Bank tp where tp.id = " + bankId;
				bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			}
		
		
			System.out.println(bankName);
			System.out.println(bankCode);
			System.out.println(bankId);
			String hql = "Select tp from Bank tp where (lower(tp.bankName) = '" + bankName.toLowerCase() + "'" 
					+ " OR lower(tp.bankCode) = '" + bankCode.toLowerCase() + "')";
			if(bankId!=null)
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
				
				
				jsonObject.put("status", ERROR.BANK_CREATE_SUCCESS);
				jsonObject.put("message", "New Bank creation was successful");
			}
			else
			{
				jsonObject.put("status", ERROR.BANK_CREATE_FAIL);
				jsonObject.put("message", "Bank with bank name or code already exists. New bank could not be created.");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.BANK_CREATE_FAIL);
				jsonObject.put("message", "Bank creation failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	
	
	
	

	
	@POST
	@Path("/createNewAcquirer")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewAcquirer(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext, 
			@FormParam("acquirerId") Long acquirerId, 
			@FormParam("acquirerName") String acquirerName, 
			@FormParam("acquirerCode") String acquirerCode, 
			@FormParam("bankId") Long bankId, 
			@FormParam("accountCreationDemoEndPoint") String accountCreationDemoEndPoint, 
			@FormParam("accountCreationLiveEndPoint") String accountCreationLiveEndPoint, 
			@FormParam("balanceInquiryDemoEndPoint") String balanceInquiryDemoEndPoint, 
			@FormParam("balanceInquiryLiveEndPoint") String balanceInquiryLiveEndPoint, 
			@FormParam("fundsTransferDemoEndPoint") String fundsTransferDemoEndPoint, 
			@FormParam("fundsTransferLiveEndPoint") String fundsTransferLiveEndPoint, 
			@FormParam("demoAuthKey") String demoAuthKey, 
			@FormParam("authKey") String authKey, 
			@FormParam("demoServiceKey") String demoServiceKey, 
			@FormParam("serviceKey") String serviceKey, 
			@FormParam("holdFundsYes") Integer holdFundsYes,
			@FormParam("allowedCurrency") String allowedCurrency,
			@FormParam("token") String token)
	{
		Acquirer acquirer = new Acquirer();
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
			
			
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			
			if(acquirerId!=null)
			{
				String hql = "Select tp from Acquirer tp where tp.id = " + bankId;
				acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			}
		
		
			System.out.println(acquirerName);
			System.out.println(acquirerCode);
			System.out.println(bankId);
			String hql = "Select tp from Acquirer tp where (lower(tp.acquirerName) = '" + acquirerName.toLowerCase() + "'" 
					+ " OR lower(tp.acquirerCode) = '" + acquirerCode.toLowerCase() + "')";
			if(acquirerId!=null)
			{
				hql = hql + " AND tp.id != " + acquirerId;
			}
			Acquirer acquirerExist = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
			
			if(acquirerExist==null)
			{
				
				acquirer.setAccountCreationDemoEndPoint(accountCreationDemoEndPoint);
				acquirer.setAccountCreationEndPoint(accountCreationLiveEndPoint);
				acquirer.setBalanceInquiryDemoEndPoint(balanceInquiryDemoEndPoint);
				acquirer.setBalanceInquiryEndPoint(balanceInquiryLiveEndPoint);
				acquirer.setDemoAuthKey(demoAuthKey);
				acquirer.setAuthKey(authKey);
				acquirer.setDemoServiceKey(demoServiceKey);
				acquirer.setServiceKey(serviceKey);
				acquirer.setFundsTransferDemoEndPoint(fundsTransferDemoEndPoint);
				acquirer.setFundsTransferEndPoint(fundsTransferLiveEndPoint);
				acquirer.setCreated_at(new Date());
				acquirer.setUpdated_at(new Date());
				acquirer.setHoldFundsYes(holdFundsYes==1 ? Boolean.TRUE : Boolean.FALSE);
				acquirer.setAcquirerCode(acquirerCode);
				acquirer.setAcquirerName(acquirerName);
				acquirer.setAllowedCurrency(allowedCurrency);
				acquirer.setBank((Bank)this.swpService.getRecordById(Bank.class, bankId));
				String encryptionKey = null;
				KeyGenerator keyGen = KeyGenerator.getInstance("AES");
				keyGen.init(256); // for example
				SecretKey secretKey = keyGen.generateKey();
				encryptionKey = java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());
				
				if(bankId==null)
				{
					acquirer.setAccessExodus(encryptionKey);
				}
				acquirer = (Acquirer)this.swpService.createNewRecord(acquirer);
				
				jsonObject.put("status", ERROR.ACQURIER_CREATE_SUCCESS);
				jsonObject.put("message", "New Acquirer creation was successful");
			}
			else
			{
				jsonObject.put("status", ERROR.ACQUIRER_CREATE_FAIL);
				jsonObject.put("message", "Acquirer with acquirer name or code already exists. New Acquirer could not be created.");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			log.error("err", e);
			try {
				jsonObject.put("status", ERROR.ACQUIRER_CREATE_FAIL);
				jsonObject.put("message", "Acquirer creation failed");
				log.warn(e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	

}
