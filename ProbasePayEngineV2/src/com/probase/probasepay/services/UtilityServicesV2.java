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
import javax.ws.rs.core.Response;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

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
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.MobileAccountStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.VendorStatus;
import com.probase.probasepay.enumerations.WalletStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BankStaff;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Country;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.Setting;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.util.Application;
import com.probase.probasepay.util.ERROR;
import com.probase.probasepay.util.PrbCustomService;
import com.probase.probasepay.util.ServiceLocator;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;


@Path("/UtilityServicesV2")
public class UtilityServicesV2 {
	private static Logger log = Logger.getLogger(UtilityServicesV2.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	
	
	/**Service Method - Customer signs up mobile money 
	 * on mobile application
	 * 
	 * @return Stringified JSONObject of the list of customers
	 */

	@GET
	@Path("/listAllProvinces")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAllProvinces(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext)
	{
		JSONObject jsonObject = new JSONObject();
		Collection<Province> provinceList = null;
		int i = 0;
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from Province tp";
			provinceList = (Collection<Province>)this.swpService.getAllRecordsByHQL(hql);
			
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "All Provinces pulled succcessfully");
			jsonObject.put("provinceList", provinceList.toArray());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	

	@GET
	@Path("/listAllMerchants")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAllMerchants(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext)
	{
		JSONObject jsonObject = new JSONObject();
		Collection<Merchant> merchantList = null;
		int i = 0;
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from Merchant tp";
			merchantList = (Collection<Merchant>)this.swpService.getAllRecordsByHQL(hql);
			
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "All Merchants pulled succcessfully");
			jsonObject.put("merchantList", new Gson().toJson(new ArrayList<Merchant>(merchantList)));
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	

	@GET
	@Path("/listDistrictsByProvince")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDistrictsByProvince(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("districtId") Integer districtId)
	{
		JSONObject jsonObject = new JSONObject();
		Collection<District> districtList = null;
		int i = 0;
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from District tp WHERE tp.provinceId = " + districtId;
			districtList = (Collection<District>)this.swpService.getAllRecordsByHQL(hql);
			
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Districts pulled succcessfully");
			jsonObject.put("districtList", districtList.toArray());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
					
	}
	
	

	@GET
	@Path("/listProvinceByCountry")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listProvinceByCountry(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("countryId") Integer countryId)
	{
		JSONObject jsonObject = new JSONObject();
		Collection<Province> provinceList = null;
		int i = 0;
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from Province tp WHERE tp.countryId = " + countryId;
			provinceList = (Collection<Province>)this.swpService.getAllRecordsByHQL(hql);
			
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Provinces pulled succcessfully");
			jsonObject.put("provinceList", provinceList.toArray());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	

	@GET
	@Path("/listDistrictsByCountry")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listDistrictsByCountry(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("countryId") Integer countryId)
	{
		JSONObject jsonObject = new JSONObject();
		Collection<District> districtList = null;
		int i = 0;
		
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp from District tp WHERE tp.countryId = " + countryId + " ORDER BY tp.name ASC";
			districtList = (Collection<District>)this.swpService.getAllRecordsByHQL(hql);
			
			
			jsonObject.put("status", ERROR.MMONEY_ADD_SUCCESS);
			jsonObject.put("message", "Districts pulled succcessfully");
			jsonObject.put("districtList", districtList.toArray());
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	@GET
	@Path("/pullPaymentDefaultData")
	@Produces(MediaType.APPLICATION_JSON)
	public Response pullPaymentDefaultData(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("merchantCode") String merchantCode, 
			@QueryParam("hash") String hash, 
			@QueryParam("deviceCode") String deviceCode, 
			@QueryParam("serviceTypeId") String serviceTypeId, 
			@QueryParam("orderId") String orderId, 
			@QueryParam("amount") Double amount)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			
			String hql = "Select tp from Merchant tp where tp.merchantCode = '" + merchantCode + "'";
			Merchant merchant = (Merchant)this.swpService.getUniqueRecordByHQL(hql);

			hql = "Select tp from Device tp where tp.deviceCode = '" + deviceCode + "'";
			Device device = (Device)this.swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Bank tp where tp.countryOfOperation.id = " + merchant.getCountryOfOperation().getId();
			Collection<Bank> allBanks = (Collection<Bank>)this.swpService.getAllRecordsByHQL(hql);
			
			Collection<Province> allProvinces = app.getAllProvinces();
			if(merchant!=null)
			{
				Country countryOfOperation = merchant.getCountryOfOperation();
				System.out.println("hash = " + hash);
				if(UtilityHelper.validateTransactionHash(
						hash, 
						merchantCode,
						deviceCode,
						serviceTypeId,
						orderId,
						amount,
						device.getSuccessUrl(),
						merchant.getApiKey())==false)
				{
					
					jsonObject.put("status", ERROR.HASH_FAIL);
					jsonObject.put("message", "Hash failed. Hash must match to proceed with transaction");
					return Response.status(200).entity(jsonObject.toString()).build();
				}
				Double fixedCharge = merchant.getMerchantScheme().getFixedCharge();
				Double txnPercent = merchant.getMerchantScheme().getTransactionPercentage();
				Double flatFixedCharge = null;
				//app.getAllSettings()!=null && app.getAllSettings().has("Fixed Charge") ? app.getAllSettings().getDouble("Fixed Charge") : null;;
				Double flatTxnPercent = null;
				//app.getAllSettings()!=null && app.getAllSettings().has("Transaction Percentage") ? app.getAllSettings().getDouble("Transaction Percentage") : null;
				
				//Merchant Scheme fixed charge overrides general fixed charge. Same with txnPercent
				Double fxCharge = fixedCharge==null ? (flatFixedCharge==null ? 0.0 : flatFixedCharge) : fixedCharge;
				Double txnPer = txnPercent==null ? (flatTxnPercent==null ? 0.0 : flatTxnPercent) : txnPercent;
				
				
				JSONObject paymentOptions = new JSONObject();
				paymentOptions.put("bevuraWalletAccept", device.getWalletAccept()!=null && device.getWalletAccept().equals(Boolean.TRUE) ? 1 : 0);
				paymentOptions.put("mastercardAccept", device.getMastercardAccept()!=null && device.getMastercardAccept().equals(Boolean.TRUE) ? 1 : 0);
				paymentOptions.put("visaAccept", device.getVisaAccept()!=null && device.getVisaAccept().equals(Boolean.TRUE) ? 1 : 0);
				paymentOptions.put("mobileMoneyAccept", device.getMobileMoneyAccept()!=null && device.getMobileMoneyAccept().equals(Boolean.TRUE) ? 1 : 0);
				paymentOptions.put("onlineBankingAccept", device.getBankOnlineAccept()!=null && device.getBankOnlineAccept().equals(Boolean.TRUE) ? 1 : 0);
				
				jsonObject.put("all_banks", new ArrayList<Bank>(allBanks));
				jsonObject.put("all_provinces", new ArrayList<Province>(allProvinces));
				jsonObject.put("all_countries", new ArrayList<Country>(app.getAllCountries()));
				jsonObject.put("all_districts", new ArrayList<District>(app.getAllDistricts()));
				jsonObject.put("payment_options", paymentOptions);
				jsonObject.put("fixedChargePerTransaction", fxCharge);
				jsonObject.put("percentagePerTransaction", txnPer);
				jsonObject.put("status", ERROR.PAYMENT_INTERFACE_PULL_SUCCESS);
				jsonObject.put("message", "Data pulled successfully");
			}else
			{
				jsonObject.put("status", ERROR.MERCHANT_NOT_EXIST);
				jsonObject.put("message", "Invalid Merchant Code");
			}
		}catch(Exception e)
		{
			log.warn(e);
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	public static JSONObject getCurrentBalance(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			Account account, 
			String isNotTransactionRef, 
			SwpService swpService, 
			Channel channel)
	{
		Double balance = 0.0;
		JSONObject jsonObject = new JSONObject();
		try
		{
			/*Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			
			String hql = "Select tp from AccountStatement tp where " +
			"tp.account.id = " + account.getId() + " AND tp.statementMonth = " + (cal.get(Calendar.MONTH)) + 
			" AND tp.statementYear = " + cal.get(Calendar.YEAR);
			AccountStatement accountStatement = (AccountStatement)swpService.getUniqueRecordByHQL(hql);
			
			if(accountStatement!=null)
			{
				balance = accountStatement.getClosingBalance();
			}
			
			hql = "Select sum(tp.amount) from Transaction tp where " +
					"tp.account.id = " + account.getId() + " AND tp.creditAccountTrue = (1)";
			if(isNotTransactionRef!=null)
				hql = hql + " AND tp.transactionRef != '" + isNotTransactionRef + "'";
	
			Double creditSum = (Double)swpService.getUniqueRecordByHQL(hql);
			System.out.println("Credit Sum = " + creditSum);
			creditSum = (creditSum!=null ? creditSum :0) + (balance!=null ? balance : 0);
			
			hql = "Select sum(tp.amount) from Transaction tp where " +
					"tp.account.id = " + account.getId() + " AND tp.creditAccountTrue = (0)";
			if(isNotTransactionRef!=null)
				hql = hql + " AND tp.transactionRef != '" + isNotTransactionRef + "'";
	
			Double debitSum = (Double)swpService.getUniqueRecordByHQL(hql);
			System.out.println("Debit Sum = " + debitSum);
			
			balance = creditSum - (debitSum!=null ? debitSum : 0);*/
			

			String hql = "Select tp from Transaction tp where tp.status = " + TransactionStatus.SUCCESS.ordinal() + " AND " +
					"tp.account.id = " + account.getId() + " ORDER BY tp.updated_at DESC";
			Collection<Transaction> lastTransactions = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql, 0, 1);
			Transaction lastTransaction = lastTransactions.size()>0 ? lastTransactions.iterator().next(): null;
			balance = lastTransaction.getClosingBalance();
			Double creditSum = lastTransaction.getTotalCreditSum();
			Double debitSum = lastTransaction.getTotalDebitSum();
			
			//TransactionCount Via A Channel
			hql = "Select count(tp.id) from Transaction tp where " +
					"tp.account.id = " + account.getId() + " AND tp.status = " + TransactionStatus.SUCCESS.ordinal();
			if(channel!=null)
				hql = hql + " AND tp.channel = " + channel.ordinal();
			Long count = (Long)swpService.getUniqueRecordByHQL(hql);
			
			
			jsonObject.put("creditSum", creditSum==null ? 0 : creditSum);
			jsonObject.put("debitSum", debitSum==null ? 0 : debitSum);
			jsonObject.put("balance", balance);
			jsonObject.put("transactionCount", count);
		}catch(Exception e)
		{
			log.warn(e);
		}
			
		return jsonObject;
		
	}
	
	
	
	
	public void testSOAPRequest()
	{
		
		try
		{
        	SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            System.out.println("--->findByTPIN == testing disable");
            //Util.disableSslVerification();
            String url = "http://41.57.218.10:8088/services/GanaQueryWebsercice";
          //url = "http://testwspaymentservice2-pallpod.rhcloud.com/TestWSPaymentService2/WSZmPayment";
            //url = "http://testwspaymentservice2-pallpod.rhcloud.com/TestWSPaymentService2/WSZmPayment";
            /*url = Determiner.BANK_STANBIC==1 ? "http://196.8.211.42:8084/asycudaws/WSZmPayment" : 
            	(Determiner.BANK_BANCABC==1 ? "http://10.16.76.30:8084/asycudaws/WSZmPayment" : "");
            url = Determiner.BANK_BANCABC_LIVE==1 ? "https://10.16.208.111:8443/asycudaws/WSZmPayment" : "";*/
            //url = "http://localhost:8185/WS/WSZmPayment";
            SOAPMessage soapResponse = null;
            
	        MessageFactory messageFactory = MessageFactory.newInstance();
	        SOAPMessage soapMessage = messageFactory.createMessage();
	        SOAPPart soapPart = soapMessage.getSOAPPart();
	
	        SOAPEnvelope envelope = soapPart.getEnvelope();
	        MimeHeaders mh = soapMessage.getMimeHeaders();
	        
	        mh.addHeader("Authorization", "Basic U1RBTkJJQzE6VGVzdDEyMw==");
	        envelope.addNamespaceDeclaration("xsd", "http://com.ztesoft.zsmart/service");
	        //3envelope.addNamespaceDeclaration("asy", "http://asycuda.org/");
	
	        SOAPBody soapBody = envelope.getBody();
	        
	        SOAPElement soapBodyElem = soapBody.addChildElement("QryUserIPP4GhanaPaymentPlatformReq", "xsd");
	        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("MSISDN");
	        soapBodyElem2.addTextNode("233000099");
	        
	        	
	        soapMessage.saveChanges();
	        /******Print the request message******/
	        System.out.print("Request SOAP Message = ");
	        soapMessage.writeTo(System.out);
	        System.out.println();
	        soapResponse = soapConnection.call(soapMessage, url);
	
	        SOAPBody soapBodyResponse = soapResponse.getSOAPBody();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageFactory messageFactory;
			SOAPMessage soapMessage = null;
			try {
				messageFactory = MessageFactory.newInstance();
				
				try {
					soapMessage = messageFactory.createMessage();
				} catch (SOAPException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (SOAPException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
		} 
        
        
	}
	

	@POST
	@Path("/updateApplicationSetting")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateApplicationSetting(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@FormParam("token") String token, 
			@FormParam("appSettings") String appSettings) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			Application app = Application.getInstance(swpService);
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			System.out.println("verifyJ = " + jsonObject.toString());
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
			
			System.out.println(appSettings);
			JSONObject appJson = new JSONObject(appSettings);
			Iterator it = appJson.keys();
			Application application = Application.getInstance(swpService);
			while(it.hasNext())
			{
				String key = (String)it.next();
				String hql = "Select tp from Setting tp where (tp.name) = '" + key.toLowerCase() + "'";
				System.out.println(hql);
				System.out.println("key ==> " + key);
				System.out.println("key ==> " + appJson.get(key));
				
				if(appJson.has(key))
				{
					String val = !appJson.isNull(key) ? appJson.getString(key) : null;
					if(val!=null && val.length()>0)
					{
						System.out.println("val ==> " + val);
						Setting setting = (Setting)this.swpService.getUniqueRecordByHQL(hql);
						if(setting!=null){
							System.out.println("setting ==> " + setting.getName() + " && " + setting.getValue());
							setting.setValue(val);
							setting.setUpdated_at(new Date());
							this.swpService.updateRecord(setting);
						}
						else{
							setting = new Setting();
							setting.setName(key.toLowerCase());
							setting.setCreated_at(new Date());
							setting.setStatus(true);
							setting.setValue(val);
							setting.setUpdated_at(new Date());
							this.swpService.createNewRecord(setting);
						}
		
						
							
						
						/*if(key.toLowerCase().equals("cyberSourceAccessKey".toLowerCase()))
						{
							application.setCyberSourceAccessKey(val);
						}
						else if(key.toLowerCase().equals("cyberSourceProfileId".toLowerCase()))
						{
							application.setCyberSourceProfileId(val);
						}
						else if(key.toLowerCase().equals("cyberSourceSecretKey".toLowerCase()))
						{
							application.setCybersourcesecretkey(val);
						}
						else if(key.toLowerCase().equals("cyberSourceLocale".toLowerCase()))
						{
							application.setCybersourcelocale(val);
						}
						else if(key.toLowerCase().equals("minimumBalance".toLowerCase()))
						{
							try{
								application.setMinimumBalance(Double.valueOf(val));}
							catch(NumberFormatException e){	}
						}
						else if(key.toLowerCase().equals("minimumTransactionAmountWeb".toLowerCase()))
						{
							try{
								application.setMinimumTransactionAmountWeb(Double.valueOf(val));}
							catch(NumberFormatException e){	}
						}
						else if(key.toLowerCase().equals("maximumTransactionAmountWeb".toLowerCase()))
						{
							try{
								application.setMaximumTransactionAmountWeb(Double.valueOf(val));}
							catch(NumberFormatException e){	}
						}
						else if(key.toLowerCase().equals("paymentVendorCode".toLowerCase()))
						{
							application.setPaymentVendorCode(val);
						}
						else if(key.toLowerCase().equals("methodOfPayment".toLowerCase()))
						{
							application.setMethodOfPayment(val);
						}
						else if(key.toLowerCase().equals("currencyCode".toLowerCase()))
						{
							application.setCurrencyCode(val);
						}
						else if(key.toLowerCase().equals("vendorCode".toLowerCase()))
						{
							application.setVendorCode(val);
						}
						else if(key.toLowerCase().equals("language".toLowerCase()))
						{
							application.setLanguage(val);
						}
						else if(key.toLowerCase().equals("dataSource".toLowerCase()))
						{
							application.setDataSource(val);
						}
						else if(key.toLowerCase().equals("interfaceType".toLowerCase()))
						{
							application.setInterfaceType(val);
						}
						else if(key.toLowerCase().equals("businessUnit".toLowerCase()))
						{
							application.setBusinessUnit(val);
						}*/
					}
					else
					{
						Setting setting = (Setting)this.swpService.getUniqueRecordByHQL(hql);
						if(setting!=null){
							System.out.println("setting ==> " + setting.getName() + " && " + setting.getValue());
							setting.setValue(null);
							setting.setUpdated_at(new Date());
							this.swpService.updateRecord(setting);
						}
						else{
							setting = new Setting();
							setting.setName(key.toLowerCase());
							setting.setCreated_at(new Date());
							setting.setStatus(true);
							setting.setValue(null);
							setting.setUpdated_at(new Date());
							this.swpService.createNewRecord(setting);
						}
					}
				}
			}
			
			application = Application.getInstance(swpService, Boolean.TRUE);
			
			String hql = "Select tp.* from settings tp where tp.status = " + 1;
			List<Map<String, Object>> settingList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			Iterator<Map<String, Object>> it1 = settingList.iterator();
			JSONObject settingJSON = new JSONObject();
			while(it1.hasNext())
			{
				Map<String, Object> it2 = it1.next();
				settingJSON.put((String)it2.get("name"), (String)it2.get("value"));
			}
			application.setAllSettings(settingJSON);
			
			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Application Settings Updated Successfully.");
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Application Settings Update Failed.");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	

	@GET
	@Path("/getApplicationSetting")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getApplicationSetting(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext,
			@QueryParam("token") String token) throws JSONException
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
			
			/*String hql = "Select tp from Setting tp where tp.deleted_at IS NULL";
			Collection<Setting> appSettings = (Collection<Setting>)this.swpService.getAllRecordsByHQL(hql);
			Iterator<Setting> it = appSettings.iterator();*/
			Application application = Application.getInstance(swpService);
			
			
			/*if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("cyberSourceAccessKey", application.getCyberSourceAccessKey());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("cyberSourceProfileId", application.getCyberSourceProfileId());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("cyberSourceSecretKey", application.getCybersourcesecretkey());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("cyberSourceLocale", application.getCybersourcelocale());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("minimumBalance", application.getMinimumBalance());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("minimumTransactionAmountWeb", application.getMinimumTransactionAmountWeb());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("maximumTransactionAmountWeb", application.getMaximumTransactionAmountWeb());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("paymentVendorCode", application.getPaymentVendorCode());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("methodOfPayment", application.getMethodOfPayment());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("currencyCode", application.getCurrencyCode());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("vendorCode", application.getVendorCode());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("language", application.getLanguage());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("dataSource", application.getDataSource());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("interfaceType", application.getInterfaceType());
			if(application.getCyberSourceAccessKey()!=null)
				jsonObject.put("businessUnit", application.getBusinessUnit());*/
			
			
			String hql = "Select tp.* from settings tp where tp.status = " + 1;
			List<Map<String, Object>> settingList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			Iterator<Map<String, Object>> it1 = settingList.iterator();
			JSONObject settingJSON = new JSONObject();
			while(it1.hasNext())
			{
				Map<String, Object> it2 = it1.next();
				settingJSON.put((String)it2.get("name"), (String)it2.get("value"));
			}
			
			jsonObject.put("settingJSON", settingJSON);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Application Settings Updated Successfully.");
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Application Settings Update Failed.");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
	
	
	
	
	
	@POST
	@Path("/getSecurityQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSecurityQuestions(
			@Context HttpHeaders httpHeaders,
			@Context HttpServletRequest requestContext) throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		User user = null;
		try
		{
			this.swpService = this.serviceLocator.getSwpService();
			
			String hql = "Select tp.* from security_questions tp where tp.status = " + 1;
			List<Map<String, Object>> securityQuestionList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("securityQuestionList", securityQuestionList);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Application Security Questions Listed Successfully.");
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Application Security Questions Listed Update Failed.");
		}
		return Response.status(200).entity(jsonObject.toString()).build();
	}
	
}
