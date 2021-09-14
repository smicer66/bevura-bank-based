package com.probase.probasepay.services;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.AppDevice;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;

@Path("/UserServicesV2")
public class UserServicesV2 {
	private static Logger log = Logger.getLogger(UserServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	public Application application = null;
	
	/**Service Method - Customer signs up mobile money 
	 * on mobile application
	 * 
	 * @return Stringified JSONObject of the list of customers
	 */
	
	@POST
	@Path("/updatePrivileges")
	@Consumes({"text/plain"})
	@Produces("application/json")
	public Response updatePrivileges(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("userId") Integer userId, @FormParam("status") String status, @FormParam("privileges") String privileges)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from User where tp.id = " + userId;
			User user = (User)this.swpService.getUniqueRecordByHQL(hql);
			user.setPrivileges(privileges);
			this.swpService.updateRecord(user);
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Pool account fetch succcessful");
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	
	
	
	@POST
	@Path("/createBankStaff")
	@Consumes({"text/plain"})
	@Produces("application/json")
	public Response createBankStaff(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("bankStaffId") String bankStaffId, @FormParam("addressLine1") String addressLine1, @FormParam("addressLine2") String addressLine2, 
			@FormParam("altContactEmail") String altContactEmail, @FormParam("altContactMobile") String altContactMobile, @FormParam("contactEmail") String contactEmail, 
			@FormParam("contactMobile") String contactMobile, @FormParam("dateOfBirth") String dateOfBirth, 
			@FormParam("firstName") String firstName, @FormParam("gender") String gender, @FormParam("lastName") String lastName, @FormParam("otherName") String otherName, 
			@FormParam("role_type") String role_type, @FormParam("locationDistrict_id") Integer locationDistrict_id, @FormParam("encPassword") String encPassword, @FormParam("token") String token, 
			@FormParam("bankId") Integer bankId, @FormParam("branchCode") String branchCode, @FormParam("identityNumber") String identityNumber, @FormParam("profilePix") String profilePix)
	{
		JSONObject jsonObject = new JSONObject();
		BankStaff bankStaff = new BankStaff();
		User userCheck = null;
		User user = null;
		
		
		System.out.println("Create New Bank Staff");
		try {
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from Bank tp where tp.id = " + bankId;
			Bank bank = (Bank)this.swpService.getUniqueRecordByHQL(hql);
			application = Application.getInstance(swpService);
			JSONObject bankKeys = application.getAccessKeys();
			if(bankKeys.length()==0 || bank==null)
			{
				return null;
			}
			String bankCode = bank.getBankCode();
			
			System.out.println("Bank Code = " + bankCode);
			String bankKey = bankKeys.getString(bankCode);
			System.out.println("Bank Key = " + bankKey);
			//bankKey = new String(Base64.decodeBase64(bankKey.getBytes()));
			String password = (UtilityHelper.decryptData(encPassword, bankKey)).toString();
			System.out.println("dateOfBirth = " + dateOfBirth);

			District locationDistrict = null;
			hql = "Select tp from District tp where tp.id = " + locationDistrict_id;
			locationDistrict = (District)this.swpService.getUniqueRecordByHQL(hql);
			Date dob = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			dob = sdf.parse(dateOfBirth);
			String webActivationCode = RandomStringUtils.randomAlphanumeric(32);
			String mobileActivationCode = RandomStringUtils.randomAlphanumeric(6);
			
			if(bankStaffId!=null)
			{
				Integer bankStaffIdI = (Integer) UtilityHelper.decryptData(bankStaffId, bankKey);
				hql = "Select tp from BankStaff tp where tp.id = " + bankStaffIdI;
				bankStaff = (BankStaff)this.swpService.getUniqueRecordByHQL(hql);
			}
					
			if(bankStaffId==null)
			{
				hql = "Select tp from User tp where lower(tp.username) = '" + contactEmail.toLowerCase() + "'";
				userCheck = (User)this.swpService.getUniqueRecordByHQL(hql);
			}
			
			if(userCheck==null)
			{
				Application app = Application.getInstance(swpService);
				JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
				//System.out.println("verifyJ ==" + verifyJ.toString());
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
					jsonObject.put("token", verifyJ.getString("token"));
				}
				
				if(bankStaffId==null)
				{
					user = new User();
					//user.setWebActivationCode(webActivationCode);
					//user.setMobileActivationCode(mobileActivationCode);
					user.setFailedLoginCount(0);
					user.setUsername(contactEmail);
					user.setLockOut(Boolean.FALSE);
					user.setCreated_at(new Date());
					user.setStatus(UserStatus.INACTIVE);
					user.setMobileNo(contactMobile);
					user.setFirstName(firstName);
					user.setLastName(lastName);
					user.setOtherName(otherName);
					System.out.println("roleType = " + role_type + " && Value = " + RoleType.valueOf(role_type).ordinal());
					user.setRoleType(RoleType.valueOf(role_type));
					user.setPassword(password);
					if(profilePix!=null)
						user.setProfilePix(profilePix);
					
					user = (User)this.swpService.createNewRecord(user);
				}
				
				
				bankStaff.setAddressLine1(addressLine1);
				bankStaff.setAddressLine2(addressLine2);
				bankStaff.setCreated_at(new Date());
				bankStaff.setDateOfBirth(dob);
				bankStaff.setFirstName(firstName);
				bankStaff.setLastName(lastName);
				bankStaff.setGender(Gender.valueOf(gender));
				bankStaff.setLocationDistrict(locationDistrict);
				bankStaff.setOtherName(otherName);
				bankStaff.setIdentityNumber(identityNumber);
				bankStaff.setBranchCode(branchCode);
				bankStaff.setBank(bank);
				bankStaff.setStatus(BankStaffStatus.INACTIVE);
				bankStaff.setProvinceId(locationDistrict.getProvinceId());
				if(bankStaffId==null)
				{
					bankStaff.setUser(user);
				}
				bankStaff = (BankStaff)this.swpService.createNewRecord(bankStaff);
				
				
				jsonObject.put("status", ERROR.BANK_STAFF_CREATE_SUCCESS);
				jsonObject.put("message", "New Bank Staff Account Created Successfully");
				
				
				System.out.println("Create New Bank Staff = " + jsonObject.toString());
			}else
			{
				jsonObject.put("status", ERROR.BANK_STAFF_CREATE_FAIL);
				jsonObject.put("message", "New Bank Staff Creation Failed. User with email address already exists");
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("message", "New customer creation Failed. Value for Date of birth provided invalid. Date should be in format YYYY-MM-DD");
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_DOB_FAILED);
				log.warn(e);
				System.out.println("Create New customer Failed = " + jsonObject.toString());
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			log.warn(e);
			try {
				jsonObject.put("status", ERROR.CUSTOMER_CREATION_FAILED);
				jsonObject.put("message", "New customer creation Failed");
				log.warn(e);
				System.out.println("Create New Customer Failed = " + jsonObject.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		
	}
	

	
	
	@POST
	@Path("/createNewAppDevice")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewAppDevice(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("device_app_id") String device_app_id, @FormParam("deviceVersion") String deviceVersion, @FormParam("deviceName") String deviceName, 
			@FormParam("deviceKey") String deviceKey, @FormParam("deviceId") String deviceId)
	{
		System.out.println(">>>>>>>>> 0");
		JSONObject jsonObject= new JSONObject();
		jsonObject.put("status", ERROR.GENERAL_FAIL);
		jsonObject.put("message", "General system error");
		try{
			System.out.println(">>>>>>>>> 1");
			System.out.println(">>>>>>>>> 2");
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject bankKeys = application.getAccessKeys();
			System.out.println(">>>>>>>>> 4");
			if(bankKeys.length()==0)
			{
				return null;
			}
			System.out.println(">>>>>>>>> 5");
			System.out.println(device_app_id);
			System.out.println(deviceVersion);
			System.out.println(deviceName);
			System.out.println(deviceKey);
			System.out.println(deviceId);
			log.info(device_app_id);
			log.info(deviceVersion);
			log.info(deviceName);
			log.info(deviceKey);
			log.info(deviceId);
			
			
			if(deviceKey!=null && device_app_id!=null)
			{
				String hql = "Select tp from AppDevice tp where tp.deviceKey = '"+ deviceKey+"' AND tp.appId = '"+ device_app_id +"'";
				Collection<AppDevice> appDevices = (Collection<AppDevice>)swpService.getAllRecordsByHQL(hql);
				if(appDevices.size()==0)
				{
					AppDevice appDevice = new AppDevice(deviceId, deviceKey, deviceName, deviceVersion, device_app_id);
					appDevice = (AppDevice)swpService.createNewRecord(appDevice);
					
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "New Device Added Successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("failKey", 0);
					jsonObject.put("message", "Try again. Invalid key provided");
				}
			}
			else
			{

				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("failKey", 1);
				jsonObject.put("message", "Try again. Invalid key provided");
			}

			
				
			
		}catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "General system error");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		System.out.println("211");
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	
	
	@POST
	@Path("/createNewUserAccount")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createNewUserAccount(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("username") String username, @FormParam("encPassword") String encPassword, @FormParam("roleCode") String roleCode, 
			@FormParam("acquirerCode") String acquirerCode, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName, 
			@FormParam("otherName") String otherName,
			@FormParam("device_app_id") String device_app_id, @FormParam("deviceVersion") String deviceVersion, @FormParam("deviceName") String deviceName, 
			@FormParam("deviceKey") String deviceKey, @FormParam("deviceId") String deviceId)
	{
		System.out.println("0");
		JSONObject jsonObject= new JSONObject();
		try{
			System.out.println("1");
			System.out.println("2");
			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);
			JSONObject bankKeys = application.getAccessKeys();
			System.out.println("4");
			if(bankKeys.length()==0)
			{
				return null;
			}
			System.out.println("1");
			String bankKey = bankKeys.getString(acquirerCode);
			System.out.println("bankKeys ==" + bankKeys.toString());
			System.out.println("bankKey ==" + bankKey);
			//bankKey = new String(Base64.decodeBase64(bankKey.getBytes()));
			String password = UtilityHelper.decryptData(encPassword, bankKey).toString();
			System.out.println("password ==" + password);
			
			if(password!=null)
			{
				String hql = "Select tp from User tp where tp.username = '"+ username +"'";
				Collection<User> userCheck = (Collection<User>)this.swpService.getAllRecordsByHQL(hql);
				if(userCheck!=null && userCheck.size()>0)
				{
					jsonObject.put("status", ERROR.USER_STATUS_UPDATE_FAIL_MOBILE_NUMBER_TAKEN);
					jsonObject.put("message", "Invalid mobile number provided. The mobile number you provided has already signed up for our service");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				
				hql = "Select tp from Acquirer tp where tp.acquirerCode = '"+ acquirerCode +"'";
				Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL(hql);
				
				String otp = RandomStringUtils.randomNumeric(4);
				String webCode = RandomStringUtils.randomNumeric(32);
				String mobCode = RandomStringUtils.randomNumeric(4);
				otp = "1111";
				User user = new User();
				user.setUsername(username);
				user.setPassword(password);
				user.setFailedLoginCount(0);
				user.setLockOut(Boolean.FALSE);
				//user.setMobileActivationCode(mobCode);
				user.setStatus(UserStatus.INACTIVE);
				//user.setWebActivationCode(webCode);
				user.setRoleType(RoleType.valueOf(roleCode));
				user.setCreated_at(new Date());

				user.setFirstName(firstName);
				user.setLastName(lastName);
				user.setOtherName(otherName);
				user.setMobileNo(username);
				user.setOtp(otp);
				user.setPin(otp);
				user.setAcquirerId(acquirer.getId());
				user = (User)this.swpService.createNewRecord(user);
				
				
				if(roleCode.equals(RoleType.CUSTOMER.name()))
				{
					Customer customer = new Customer();
					customer.setContactMobile(username);
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
					jsonObject.put("verificationNumber", customer.getVerificationNumber());
				}
				
				if(deviceKey!=null && device_app_id!=null)
				{
					hql = "Select tp from AppDevice tp where tp.deviceKey = '"+ deviceKey+"' AND tp.appId = '"+ device_app_id +"'";
					Collection<AppDevice> appDevices = (Collection<AppDevice>)swpService.getAllRecordsByHQL(hql);
					if(appDevices.size()==0)
					{
						AppDevice appDevice = new AppDevice(deviceId, deviceKey, deviceName, deviceVersion, device_app_id);
						appDevice = (AppDevice)swpService.createNewRecord(appDevice);
					}
				}
				

				jsonObject.put("status", ERROR.USER_ACCOUNT_ADD_SUCCESSFUL);
				jsonObject.put("message", "New User Account Added Successfully");
				jsonObject.put("otp", otp);
			}else
			{
				
				jsonObject.put("status", ERROR.USER_ACCOUNT_ADD_FAIL);
				jsonObject.put("message", "New User Account Addition Failed");
			}
				
			
		}catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			try {
				jsonObject.put("status", ERROR.USER_ACCOUNT_ADD_FAIL);
				jsonObject.put("message", "New User Account Addition Failed");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		System.out.println("211");
		return Response.status(200).entity(jsonObject.toString()).build();
	}

	
	
	@GET
	@Path("/listAllUsers")
	@Consumes({"text/plain"})
	@Produces("application/json")
	public Response listAllUsers(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("status") String status, 
			@QueryParam("urole") String urole, 
			@QueryParam("token") String token) throws JSONException
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
			String issuerBankCode = verifyJ.getString("issuerBankCode");
			System.out.println("issuerBankCode ==" + issuerBankCode);
			String branch_code = verifyJ.getString("branchCode");
			System.out.println("branch_code ==" + branch_code);
			String staff_bank_code = verifyJ.getString("staff_bank_code");
			System.out.println("staff_bank_code ==" + staff_bank_code);

			String hql = "Select tp.id, tp.firstName, tp.lastName, tp.otherName, tp.failedLoginCount, tp.lockOut, tp.privileges, tp.profilePix, "
					+ "tp.roleType, tp.status, tp.updated_at, tp.created_at, tp.username, tp.mobileNo, tp.userEmail from users tp";
			boolean whereCheck = false;
			if(status!=null)
			{
				whereCheck = true;
				hql = hql + " where tp.status = " + UserStatus.valueOf(status).ordinal();
			}
			
			if(whereCheck==false)
				hql = hql + " where ";
			else
				hql = hql + " AND ";
			
			if(urole!=null)
				hql = hql + " tp.roleType = " + RoleType.valueOf(urole).ordinal();
			
			
			System.out.println(hql);
			List<Map<String, Object>> userList = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "User List fetch succcessful");
			jsonObject.put("userList", (userList));
			
		}catch(Exception e)
		{
			log.warn(e);
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "User List fetch failed");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
		
	}
	
	
	@POST
	@Path("/listAllUsersNoToken")
	@Consumes({"text/plain"})
	@Produces("application/json")
	public Response listAllUsersNoToken(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext
			)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
		

			String hql = "Select tp from User tp";
			
			Collection<User> userList = (Collection<User>)this.swpService.getAllRecordsByHQL(hql);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "User List fetch succcessful");
			jsonObject.put("userList", userList);
			
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
		
	}
	
	
	@POST
	@Path("/updateUserStatus")
	@Consumes({"text/plain"})
	@Produces("application/json")
	public Response updateUserStatus(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, @FormParam("status") Integer status, @FormParam("userIdS") String userIdS)
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
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			else
			{
				jsonObject.put("token", verifyJ.getString("token"));
			}
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
			String subject = verifyJ.getString("subject");
			Integer userId = (Integer)UtilityHelper.decryptData(userIdS, bankKey);
			System.out.println("userId = " + userId);
			
			String hql = "Select tp from User tp where tp.id = " + userId;
			System.out.println(hql);
			User usr = (User)this.swpService.getUniqueRecordByHQL(hql);
			if(usr!=null)
			{
				System.out.println("2-->" + hql);
				usr.setStatus(status==1 ? UserStatus.ACTIVE : UserStatus.ADMIN_DISABLED);
				usr.setLockOut(status==1 ? false : usr.getLockOut());
				usr.setFailedLoginCount(status==1 ? 0 : usr.getFailedLoginCount());
				this.swpService.updateRecord(user);
				
				jsonObject.put("status", ERROR.USER_STATUS_UPDATE_SUCCESS);
				jsonObject.put("user", new Gson().toJson(usr));
				jsonObject.put("message", "User status updated successfully");
			}
			else
			{
				jsonObject.put("status", ERROR.USER_STATUS_UPDATE_FAIL);
				jsonObject.put("message", "User status updated failed");
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	

	

}
