package com.probase.probasepay.services;

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

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.GroupFunction;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;

@Path("/GroupServices")
public class GroupServices {

	private static Logger log = Logger.getLogger(BankServices.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/*
	 * @params	status	
	 * 	CardStatus.ACTIVE	0
	 *	CardStatus.DELETED	1
	 *	CardStatus.DISABLED	2
	 *	CardStatus.INACTIVE	3
	 */
	
	private static final long serialVersionUID = -6663599014192066936L;

	
	
	
	
	   
    @POST
    @Path( "createNewGroup" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewGroup(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "name" ) String name, 
            @FormParam( "shortName" ) String shortName, 
            @FormParam( "story" ) String story, 
            @FormParam( "iconurl" ) String iconurl, 
            @FormParam( "isOpen" ) Integer isOpen,
            @FormParam( "token" ) String token,
            @FormParam( "backgroundColor" ) String backgroundColor,
            @FormParam( "fontColor" ) String fontColor,
            @FormParam( "maximumMembers" ) Integer maximumMembers
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.createNewGroup(backgroundColor, fontColor, user, name, shortName, story, iconurl, isOpen, maximumMembers,
            		requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "createNewGroupMessage" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewGroupMessage(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "groupMessage" ) String groupMessage, 
            @FormParam( "groupId" ) Long groupId, 
            @FormParam( "groupId" ) Long receiverUserId, 
            @FormParam( "token" ) String token
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.createNewGroupMessage(groupMessage, user, groupId, receiverUserId);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    
    
    
    @POST
    @Path( "readGroupMessage" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response readGroupMessage(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "notificationId" ) Long notificationId, 
            @FormParam( "token" ) String token
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            jsonObject = groupFunction.readGroupMessage(notificationId, user, this.swpService);

            return Response.status(200).entity(jsonObject.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    

	   
    @POST
    @Path( "createNewGroupRequest" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewGroupRequest(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.createNewGroupRequest(groupId, user, requestId, ipAddress, token);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "approveOrDisapproveJoinRequest" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response approveOrDisapproveJoinRequest(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "joinRequestId" ) Long joinRequestId,
            @FormParam( "isApproved" ) Integer isApproved
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.approveOrDisapproveJoinRequest(joinRequestId, user, requestId, ipAddress, token, isApproved);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "villageBankingSummary" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response villageBankingSummary(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
        	JSONObject allGroups = groupFunction.listGroups( token, requestId, ipAddress);
        	return Response.status(200).entity(allGroups.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    @POST
    @Path( "villageBankingJoinRequestList" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response villageBankingJoinRequestList(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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

			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
        	JSONObject allGroups = groupFunction.getVillageBankingJoinRequestList( token, requestId, ipAddress, groupId, customer, this.swpService);
        	return Response.status(200).entity(allGroups.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    @POST
    @Path( "villageBankingGroupSummary" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response villageBankingGroupSummary(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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

			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
        	JSONObject allGroups = groupFunction.getGroupSummary( token, requestId, ipAddress, groupId, customer, this.swpService);
        	return Response.status(200).entity(allGroups.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "removeGroup" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response removeGroup(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupCode" ) String groupCode) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.removeGroup( token, groupCode, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

        	return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    @POST
    @Path( "createNewGroupMember" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewGroupMember(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "groupId" ) String groupId, 
            @FormParam( "token" ) String token,
            @FormParam( "isAdmin" ) Integer isAdmin
            ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	

			

			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
            Response authResponse = groupFunction.createNewGroupMember( token, groupId, customer, user, true, isAdmin, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    @POST
    @Path( "createNewGroupMembers" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewGroupMembers(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "groupId" ) String groupId, 
            @FormParam( "customers" ) String customers, 
            @FormParam( "token" ) String token,
            @FormParam( "isActive" ) Boolean isActive
            ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.createNewGroupMembers( token, groupId, customers, isActive, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    @POST
    @Path( "verifyUserByMobileNumber" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response verifyUserByMobileNumber(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "mobileNumber" ) String mobileNumber, 
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.verifyUserByMobileNumber( mobileNumber, groupId);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    @POST
    @Path( "joinGroup" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response joinGroup(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) String groupId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.joinGroup( token, groupId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "listGroups" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response listGroups(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            JSONObject authResponse = groupFunction.listGroups( token, requestId, ipAddress);

            return Response.status(200).entity(authResponse.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "getLoanRepaymentSchedule" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getLoanRepaymentSchedule(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanId" ) Long loanId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            JSONObject authResponse = groupFunction.getLoanRepaymentSchedule( token, loanId, requestId, ipAddress, this.swpService);

            return Response.status(200).entity(authResponse.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    @POST
    @Path( "repayVillageBankingLoan" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response repayVillageBankingLoan(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanId" ) Long loanId,
            @FormParam( "sourceType" ) String sourceType,
            @FormParam( "debitSource" ) String debitSource,
            @FormParam( "merchantId" ) String merchantId,
            @FormParam( "deviceCode" ) String deviceCode,
            @FormParam( "channel" ) String channel,
            @FormParam( "orderRef" ) String orderRef,
            @FormParam( "amount" ) Double amount) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			System.out.println("verifyJ ==" + verifyJ.toString());
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			
			Channel channel_ = Channel.valueOf(channel);
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL("Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL");
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL("Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"' AND tp.deleted_at IS NULL");
			Device device = (Device)this.swpService.getUniqueRecordByHQL("Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL");
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
        	Account wallet = null;
        	ECard card = null;
        	
        	if(sourceType.equals("WALLET"))
        	{
        		hql = "Select tp from Account tp where tp.accountIdentifier = '"+ debitSource +"' AND tp.deleted_at IS NULL";
        		wallet = (Account)this.swpService.getUniqueRecordByHQL(hql);
        	}
        	if(sourceType.equals("CARD"))
        	{
        		hql = "Select tp from ECard tp where tp.trackingNumber = '"+ debitSource +"' AND tp.deleted_at IS NULL";
        		card = (ECard)this.swpService.getUniqueRecordByHQL(hql);
        	}
        	
            JSONObject authResponse = groupFunction.repayVillageBankingLoan( token, loanId, amount, requestId, ipAddress, this.swpService, 
            		device, merchant, sourceType, wallet, card, bankKey, channel_, acquirer, orderRef, 
            		httpHeaders, requestContext);

            return Response.status(200).entity(authResponse.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    @POST
    @Path( "listGroupMembers" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response listGroupMembers(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            
        	
        	String hql = "Select g.createdByUser_id, c.user_id, u.firstName, u.lastName, u.mobileNo, tp.* from group_members tp, customers c, users u, groups g where tp.addedCustomer_id = c.id AND "
        			+ "c.user_id = u.id AND tp.group_id = g.id AND tp.deletedAt IS NULL AND tp.group_id = " + groupId + " ORDER by tp.isAdmin DESC, u.firstName ASC";
        	List<Map<String, Object>> groupMembers = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
        	

        	jsonObject.put("groupMembers", groupMembers);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Group members found");
            return Response.status(200).entity(jsonObject.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    @POST
    @Path( "getGroupData" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getGroupData(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.getGroupData( token, groupId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    @POST
    @Path( "removeGroupMember" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response removeGroupMember(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId, 
            @FormParam( "removeCustomerId" ) Long removeCustomerId) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.removeGroupMember( token, groupId, removeCustomerId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "createNewContributionPackage" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createNewContributionPackage(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "packageName" ) String packageName,
            @FormParam( "contributionAmount" ) Double contributionAmount, 
            @FormParam( "contributionPeriod" ) Integer contributionPeriod,
            @FormParam( "contributionPeriodType" ) String contributionPeriodType, 
            @FormParam( "minimumBalanceRequired" ) Double minimumBalanceRequired, 
            @FormParam( "story" ) String story, 
            @FormParam( "numberOfPayments" ) Integer numberOfPayments,
            @FormParam( "penaltyApplicable" ) Double penaltyApplicable, 
            @FormParam( "penaltyApplicableType" ) String penaltyApplicableType, 
            @FormParam( "groupId" ) Long groupId,
            @FormParam( "minLoanAmount" ) Double minLoanAmount, 
            @FormParam( "maxLoanAmount" ) Double maxLoanAmount,
            @FormParam( "minTerm" ) Integer minTerm, 
            @FormParam( "maxTerm" ) Integer maxTerm, 
            @FormParam( "repaymentPeriodType" ) String repaymentPeriodType, 
            @FormParam( "interestRate" ) Double interestRate, 
            @FormParam( "interestType" ) String interestType, 
            @FormParam( "penalty" ) Double penalty,
            @FormParam( "loanPenaltyType" ) String loanPenaltyType
            
			
    ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
			
            Response authResponse = groupFunction.createNewContributionPackage( token, packageName,
        			contributionAmount, contributionPeriod,
        			contributionPeriodType, minimumBalanceRequired, story, numberOfPayments,
        			penaltyApplicable, penaltyApplicableType, groupId, minLoanAmount, maxLoanAmount, 
        			minTerm, maxTerm, repaymentPeriodType, interestRate, interestType, penalty, loanPenaltyType,
        			requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    

    
    @POST
    @Path( "getLoanTermById" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getLoanTermById(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanTermId" ) Long loanTermId
    ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
			
            Response authResponse = groupFunction.getLoanTermById(token, loanTermId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    @POST
    @Path( "generateLoanRepaymentDetails" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response generateLoanRepaymentDetails(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "principal" ) Double principal,
            @FormParam( "term" ) Integer term, 
            @FormParam( "loanTermId" ) Long loanTermId
    ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
			
            Response authResponse = groupFunction.generateLoanRepaymentDetails(token, principal, term, 
            		loanTermId, requestId, ipAddress) ;

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
		
    
    
    @POST
    @Path( "startContributionPackage" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response startContributionPackage(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
    		@FormParam( "packageId" ) Long packageId, 
    		@FormParam( "startOrEnd" ) Integer startOrEnd) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.startContributionPackage(token, packageId, startOrEnd, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "makeGroupMemberAdmin" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response makeGroupMemberAdmin(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
    		@FormParam( "groupMemberId" ) Long groupMemberId, 
    		@FormParam( "isMakeAdmin" ) Integer isMakeAdmin
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            Response authResponse = groupFunction.makeGoupMemberAdmin(token, groupMemberId, requestId, ipAddress, isMakeAdmin);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    /*@POST
    @Path( "payContribution" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response payContribution(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "expectedGroupPaymentId" ) Long expectedGroupPaymentId, 
            @FormParam( "deviceCode" ) String deviceCode, 
            @FormParam( "amount" ) Double amount) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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

			

			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"'";
			Device device = (Device)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
			Account account = (Account)swpService.getUniqueRecordByHQL(hql);
			hql = "Select tp from ECard tp where tp.accountId = " + account.getId() + " AND tp.status = " + CardStatus.ACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
			Collection<ECard> ecards = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
			Double tempAmount = amount;
			Double accountBalance = 0.00;
			List<ECard> cardDebitList = new ArrayList<ECard>();
			if(ecards.size()>0)
			{
				Iterator<ECard> iterCard = ecards.iterator();
				while(iterCard.hasNext())
				{
					ECard ecard = iterCard.next();
					Double minimumCardBalance = ecard.getCardScheme().getMinimumBalance();
					Double cardBalance = ecard.getCardBalance();
					
					if(tempAmount>=(cardBalance-minimumCardBalance))
					{
						if(tempAmount>0)
						{
							cardDebitList.add(ecard);
							tempAmount = tempAmount - (cardBalance-minimumCardBalance);
						}
					}
					else
					{
						cardDebitList.add(ecard);
						tempAmount = 0.00;
					}
				}
			}
			else
			{
				Double minimumAccountBalance = account.getAccountScheme().getMinimumBalance();
				if(tempAmount>=(accountBalance-minimumAccountBalance))
				{
					tempAmount = tempAmount - (accountBalance-minimumAccountBalance);
				}
			}
			
			if(tempAmount>0)
			{
				jsonObject = new JSONObject();
	        	jsonObject.put( "status", ERROR.INSUFFICIENT_FUNDS );
	        	jsonObject.put( "message", "Insufficient funds to pay this contribution" );
	            

	            return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.payContribution( token, expectedGroupPaymentId, amount, requestId, ipAddress, account, cardDebitList, device );

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }*/
    
    
    
    
    
    @POST
    @Path( "listGroupContributions" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response listGroupContributions(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId,
            @FormParam( "type" ) String type,
            @FormParam( "groupMemberId" ) Long groupMemberId
            ) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.getGroupContributions(token, groupId, type, groupMemberId, requestId, ipAddress, this.swpService);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    @POST
    @Path( "getLoanDetails" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getLoanDetails(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanId" ) Long loanId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.getLoanDetails(token, loanId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    @POST
    @Path( "getLoanRepayments" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getLoanRepayments(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanId" ) Long loanId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.getLoanRepayments(token, loanId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    @POST
    @Path( "applyForGroupLoan" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response applyForGroupLoan(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanTermId" ) Long loanTermId, 
            @FormParam( "principal" ) Double principal, 
            @FormParam( "term" ) Integer tenor, 
            @FormParam( "loanDetails" ) String loanDetails
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.applyForGroupLoan(token, loanTermId, principal, tenor, 
            		loanDetails, requestId, ipAddress );

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    

    
    @POST
    @Path( "approveOrDisapproveLoanRequest" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response approveOrDisapproveLoanRequest(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanId" ) Long loanId, 
            @FormParam( "isApprove" ) int isApprove
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.approveOrDisapproveGroupLoan(token, loanId, isApprove, requestId, ipAddress, this.swpService);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    
    @POST
    @Path( "repayGroupLoan" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response repayGroupLoan(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupLoanExpectedRepaymentId" ) Long groupLoanExpectedRepaymentId, 
            @FormParam( "amount" ) Double amount, 
            @FormParam( "pin" ) String pin
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.repayGroupLoan(token, groupLoanExpectedRepaymentId, amount, pin, requestId, ipAddress );

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    @POST
    @Path( "applyPenaltyToLoan" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response applyPenaltyToLoan(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupLoanId" ) Long groupLoanId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
            Response authResponse = groupFunction.applyPenaltyToLoan(token, groupLoanId, requestId, ipAddress);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    

    
    
    @POST
    @Path( "villageBankingUpdateContributionSettings" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response villageBankingUpdateContributionSettings(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "amount" ) Double amount,
            @FormParam( "howManyTimes" ) Integer howManyTimes,
            @FormParam( "groupId" ) Long groupId,
            @FormParam( "periodType" ) String periodType
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");

			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
        	
            Response authResponse = groupFunction.villageBankingUpdateContributionSettings( token, customer, amount,
            		howManyTimes, periodType, requestId, groupId, ipAddress, swpService);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    

    
    
    @POST
    @Path( "villageBankingUpdateLoanSettings" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response villageBankingUpdateLoanSettings(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "loanRate" ) Double loanRate,
            @FormParam( "loanRateType" ) String loanRateType,
            @FormParam( "groupId" ) Long groupId,
            @FormParam( "minLoanAmount" ) Double minLoanAmount,
            @FormParam( "maxLoanAmount" ) Double maxLoanAmount,
            @FormParam( "maxLoanPeriod" ) Integer maxLoanPeriod,
            @FormParam( "periodType" ) String periodType
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");

			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
        	
            Response authResponse = groupFunction.villageBankingUpdateLoanSettings(token, customer, loanRate, loanRateType, minLoanAmount, maxLoanAmount, 
            		maxLoanPeriod, periodType, requestId, groupId, ipAddress, swpService);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    

    @POST
    @Path( "getVillageBankingLoanSummary" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getVillageBankingLoanSummary(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "token" ) String token,
            @FormParam( "groupId" ) Long groupId
    		) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");

			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
        	
            Response authResponse = groupFunction.getVillageBankingLoanSummary(token, customer, requestId, groupId, ipAddress, swpService);

            return authResponse;

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    @POST
    @Path( "getVillageBankingLoans" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getVillageBankingLoans(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "groupId" ) String groupId,
            @FormParam( "token" ) String token) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
            JSONObject authResponse = groupFunction.getVillageBankingLoans( token, groupId, customer, requestId, ipAddress, this.swpService);

            return Response.status(200).entity(authResponse.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    @POST
    @Path( "payGroupContributions" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response payGroupContributions(
    		@Context HttpHeaders httpHeaders,
    		@Context HttpServletRequest requestContext,
            @FormParam( "amount" ) Double amount,
            @FormParam( "sourceType" ) String sourceType,
            @FormParam( "debitSource" ) String debitSource,
            @FormParam( "groupId" ) String groupId,
            @FormParam( "merchantId" ) String merchantId,
            @FormParam( "deviceCode" ) String deviceCode,
            @FormParam( "channel" ) String channel,
            @FormParam( "orderRef" ) String orderRef,
            @FormParam( "token" ) String token) throws JSONException {

    	String ipAddress = requestContext.getRemoteAddr();
        GroupFunction groupFunction = GroupFunction.getInstance();
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
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			String acquirerCode = verifyJ.getString("acquirerCode");
			System.out.println("acquirerCode ==" + acquirerCode);
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			//String branch_code = verifyJ.getString("branchCode");
			//System.out.println("branch_code ==" + branch_code);
			
			Channel channel_ = Channel.valueOf(channel);
			Acquirer acquirer = (Acquirer)this.swpService.getUniqueRecordByHQL("Select tp from Acquirer tp where tp.acquirerCode = '"+acquirerCode+"' AND tp.deleted_at IS NULL");
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL("Select tp from Merchant tp where tp.merchantCode = '"+merchantId+"' AND tp.deleted_at IS NULL");
			Device device = (Device)this.swpService.getUniqueRecordByHQL("Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.deleted_at IS NULL");
        	String requestId = token.substring(token.length()-10) + "-" + (new SimpleDateFormat("YYYYMMddHHmmss").format(new Date())) + ": ";
        	log.info(requestId + "Proceed 1");
        	
        	
        	
            JSONObject authResponse = groupFunction.payGroupContributions( token, groupId, customer, amount, sourceType, debitSource, requestId, ipAddress, 
            		merchant, device, this.swpService, httpHeaders, requestContext, app, bankKey, 
            		channel_, acquirer, orderRef);

            return Response.status(200).entity(authResponse.toString()).build();

        } catch ( final Exception ex ) {
        	ex.printStackTrace();
        	jsonObject = new JSONObject();
        	jsonObject.put( "message", "Experienced a system server error " );
            

            return Response.status(200).entity(jsonObject.toString()).build();
        }
    }
    
    
    
    
    
    
}
