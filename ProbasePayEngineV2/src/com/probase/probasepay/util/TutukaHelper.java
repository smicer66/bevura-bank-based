package com.probase.probasepay.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.BillType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.KLVData;
import com.probase.probasepay.enumerations.MPQRDataStatus;
import com.probase.probasepay.enumerations.MPQRDataType;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RefundStatus;
import com.probase.probasepay.enumerations.RefundType;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.StopCardReason;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.TutukaCardStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BevuraToken;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.DeviceBankAccount;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardBin;
import com.probase.probasepay.models.ECardRequest;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.MPQRData;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantBankAccount;
import com.probase.probasepay.models.Refund;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.services.AuthenticationServicesV2;
import com.probase.probasepay.services.BankServices;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

//import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.stax2.ri.typed.StringBase64Decoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class TutukaHelper {
	private static Logger log = Logger.getLogger(TutukaHelper.class);


	private final static String USER_AGENT = "Mozilla/5.0";

	/*test*/
	private static final String TUTUKA_TEST_ENDPOINT = 					"https://companion.uat.tutuka.cloud/v2_0/XmlRpc.cfm";
	private static final String TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL = 		"https://apidev.tutuka.com/mpqr/v1_0/merchant/xmlrpc.cfm";
	private static final String TUTUKA_TEST_ENDPOINT_QR_CREATE_CARD = 	"http://apidev.tutuka.com/companion/v2_0/xmlrpc.cfm";
	public static final String TERMINAL_ID_VIRTUAL = "0071472542";
	public static final String TERMINAL_PASSWORD_VIRTUAL = "D78D9AFBE8";
	public static final String TERMINAL_ID_PHYSICAL = "0072201362";
	public static final String TERMINAL_PASSWORD_PHYSICAL = "D78ACD5F04";


	public static final boolean modeTest = true;
	private static final String RECEIVING_INSTITUTION_TERMINAL_ID = "0038777206";
	private static final String RECEIVING_INSTITUTION_TERMINAL_PASSWORD = "E5CE431BC3";
	private static final String ORIGINATING_INSTITUTION_TERMINAL_ID = "0089655047";
	private static final String ORIGINATING_INSTITUTION_TERMINAL_PASSWORD = "E5D21054BA";


	
	
	
	public static String generateChecksum(String data, String password)
	{
		System.out.println("data.." + data);
		
		String signature = null;
		try {
			byte[] hmacSha256 = TutukaHelper.calcHmacSha256(password.getBytes("UTF-8"), data.getBytes("UTF-8"));
			//signature = new String(org.apache.commons.codec.binary.Base64.encodeBase64(hmacSha256), "UTF-8");
			signature = String.format("%032x", new BigInteger(1, hmacSha256));
			while(signature.length()<64)
			{
				signature = "0" + signature;
			}
			System.out.println("Base64: " + signature);
		} catch (UnsupportedEncodingException e) {
		  e.printStackTrace();
		} 
		return signature;
	}
	
	
	public static String generateChecksumSha1(String data, String password)
	{
		System.out.println("data.." + data);
		
		String signature = null;
		try {
			byte[] hmacSha1 = TutukaHelper.calcHmacSha1(password.getBytes("UTF-8"), data.getBytes("UTF-8"));
			//signature = new String(org.apache.commons.codec.binary.Base64.encodeBase64(hmacSha256), "UTF-8");
			signature = String.format("%032x", new BigInteger(1, hmacSha1));
			while(signature.length()<40)
			{
				signature = "0" + signature;
			}
			System.out.println("Base64: " + signature);
		} catch (UnsupportedEncodingException e) {
		  e.printStackTrace();
		} 
		return signature;
	}
	
	
	private static byte[] calcHmacSha1(byte[] secretKey, byte[] message) {
		byte[] hmacSha256 = null;
		try 
		{
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA1");
			mac.init(secretKeySpec);
			hmacSha256 = mac.doFinal(message);
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate hmac-sha1", e);
		}
		    return hmacSha256;
	}
	
	
	private static byte[] calcHmacSha256(byte[] secretKey, byte[] message) {
		byte[] hmacSha256 = null;
		try 
		{
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
			mac.init(secretKeySpec);
			hmacSha256 = mac.doFinal(message);
		} catch (Exception e) {
			throw new RuntimeException("Failed to calculate hmac-sha256", e);
		}
		    return hmacSha256;
	}
	
	
	
	
	/*LOCAL TUTUKA APIS*/
	//VIRTUAL CARD 
	public static JSONObject createNewTutukaCompanionVirtualCard(
			Customer customer, Account account, Acquirer acquirer, Issuer issuer, CardScheme cardScheme, String branchCode, 
			String currencyCode, Customer corporateCustomer, Account corporateCustomerAccount, SwpService swpService, Integer isTokenize, String tokenizeMerchantCode, 
			String tokenizeDeviceCode, Integer isLive) {
		// TODO Auto-generated method stub
		try {
			Application app = Application.getInstance(swpService);
			
			String hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
			System.out.println("hql ==" + hql);
			Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			
			hql = "Select tp from ECard tp where tp.account.customer.id = " + customer.getId();
			System.out.println("hql ==" + hql);
			Collection<ECard> cardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
			
			String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
					RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
			
			Date date_now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYdm");
			String d_now_s = sdf.format(date_now);
			System.out.println(">>>> = " + d_now_s);
			long identl = Long.valueOf(ident);
			long d_now_l = Long.valueOf(d_now_s);
			String sum = ident + "" + d_now_l;
			String identifier1 = acquirer.getId() + "" + sum;
			String accountNumber = identifier1;
			String cardSerialNo = accountNumber + "" + cardList.size();
			
			String firstName = customer.getFirstName();
			String lastName = customer.getLastName();
			String contactMobile = customer.getContactMobile();
			MeansOfIdentificationType meansOfIdentificationType = customer.getMeansOfIdentificationType();
			String meansOfIdentificationNumber = customer.getMeansOfIdentificationNumber();
			
			String terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
			String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			
			System.out.println("TERMINAL_ID_VIRTUAL..." + TutukaHelper.TERMINAL_ID_VIRTUAL);
			System.out.println("TERMINAL_PASSWORD_VIRTUAL..." + TutukaHelper.TERMINAL_PASSWORD_VIRTUAL);
			System.out.println("TUTUKA_TEST_ENDPOINT..." + TutukaHelper.TUTUKA_TEST_ENDPOINT);
			
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
	
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
			String checksum = "CreateLinkedCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(cardSerialNo)+""+formatDataForTutuka(firstName)+""+
					formatDataForTutuka(lastName)+""+formatDataForTutuka(meansOfIdentificationNumber)+""+
					formatDataForTutuka(contactMobile)+""+(expiryDateFormatted)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			String xml = "<?xml version=\"1.0\"?>" + 
			"<methodCall>" + 
			"<methodName>CreateLinkedCard</methodName>" + 
			"<params>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(formatDataForTutuka(terminalId))+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(cardSerialNo)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(firstName)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(lastName)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(meansOfIdentificationNumber)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(contactMobile)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<dateTime.iso8601>"+(expiryDateFormatted)+"</dateTime.iso8601>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(checksum)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"</params>" + 
			"</methodCall>"; 
	
			
			System.out.println(xml);
		
		    /**/Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "cvv2":
			            		resp_cvv = (nodeValue);
			            		break;
			            	case "cardNumber":
			            		resp_pan = (nodeValue);
			            		break;
			            	case "expiryDate":
			            		DateFormat df1 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
			            		resp_expiryDate = df1.parse(nodeValue);
			            		break;
			            	case "trackingNumber":
			            		resp_trackingNumber = (nodeValue);
			            		break;
			            	case "validDate":
			            		resp_validDate = (nodeValue);
			            		break;
			            	case "resultCode":
			            		resp_resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resp_resultText = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resp_resultCode.equals("1") || resp_resultCode.equals("0"))
			        {
			        	String pin = Integer.valueOf(RandomStringUtils.randomNumeric(4)) + "";
			        	Long customerId = customer.getId();
			        	
			        	
			        	hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + "";
			        	Collection<Account> allCustomerAccounts= (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			        	int accountCount = allCustomerAccounts!=null ? allCustomerAccounts.size() : 0;
			        	
			        
			        	if(account==null)
			        	{
			        		JSONObject allSettings = app.getAllSettings();
							String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
							CardScheme accountScheme = null;
							if(defaultAccountSchemeIdObj!=null)
							{
								Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
								accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
							}
			        		account = new Account(customer, AccountStatus.ACTIVE, null, branchCode, acquirer, currencyCode, AccountType.VIRTUAL, accountNumber, accountCount, 
			        			corporateCustomer, 
			        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
	        					corporateCustomerAccount, 
	        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
	        					ProbasePayCurrency.valueOf(currencyCode), 
	        					null, null, accountScheme, isLive);
			        		account = (Account)swpService.createNewRecord(account);
			        	}
			        	
			        	
						
			        	ECard card = new ECard(firstName + " " + lastName, resp_pan, pin, resp_expiryDate, customerId, resp_cvv, null, acquirer, 
		        			issuer, cardScheme, account, account.getId(), resp_resultCode.equals("1") ? CardStatus.ACTIVE : CardStatus.INACTIVE, CardType.TUTUKA_VIRTUAL_CARD, 
			        		false, cardSerialNo, corporateCustomer==null ? false : true, 
	        				corporateCustomer, 
		        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
        					corporateCustomerAccount, 
        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
			        		resp_trackingNumber, isLive
				        );
			        	
			        	
			        	
			        	
			        	card = (ECard)swpService.createNewRecord(card);
			        	
			        	
				        JSONObject resp = new JSONObject();
			        	
				        
				        if(isTokenize!=null && isTokenize==1)
				        {
				        	String cardBevuraToken = UtilityHelper.tokenizeCard(card);
							if(cardBevuraToken!=null)
							{
								hql = "Select tp from Merchant tp where tp.merchantCode = '"+tokenizeMerchantCode+"'";
								Merchant tokenizeMerchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
	
								hql = "Select tp from Device tp where tp.deviceCode = '"+tokenizeDeviceCode+"'";
								Device tokenizeDevice = (Device)swpService.getUniqueRecordByHQL(hql);
								
								BevuraToken bevuraToken = new BevuraToken(cardBevuraToken, null, card.getId(), tokenizeMerchant==null ? null : tokenizeMerchant.getId(), 
										tokenizeDevice==null ? null : tokenizeDevice.getId(), "CARD", card.getCustomerId(), 1, null, null, isLive!=null && isLive==1 ? 1 : 0);
								bevuraToken = (BevuraToken)swpService.createNewRecord(bevuraToken);
								resp.put("cardBevuraToken", bevuraToken.getToken());
							}
				        }

			        	System.out.println("card Id..." + card.getId());
			        	System.out.println("card tracking number...." + card.getTrackingNumber());
			        	System.out.println("card pan...." + card.getPan() );
			        	System.out.println("card cvv..." + card.getCvv());
			        	
			        	
				        resp.put("card", card);
				        resp.put("message", "Card creation was successful");
				        resp.put("status", ERROR.CARD_CREATED_SUCCESSFULLY);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "Card creation was not successful");
			        	resp.put("status", ERROR.CARD_NOT_CREATED_SUCCESSFULLY);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "Card creation was not successful");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	//PHYSICAL CARD
	public static JSONObject orderNewTutukaCompanionPhysicalCard(
			Customer customer, Account account, Acquirer acquirer, Issuer issuer, CardScheme cardScheme, String branchCode, 
			String currencyCode, Customer corporateCustomer, Account corporateCustomerAccount, SwpService swpService, Integer isLive) {
		// TODO Auto-generated method stub

		Application app = Application.getInstance(swpService);
		String verificationNumber = customer.getVerificationNumber(); 
		String initials = customer.getFirstName();
		String lastName = customer.getLastName();
		String title1 = customer.getGender()!=null && customer.getGender().equals(Gender.MALE) ? "Mr" : (customer.getGender().equals(Gender.FEMALE) ? "Ms" : "Mr");
		String address1 = customer.getAddressLine1();
		String address2 = customer.getAddressLine2();
		String address3 = customer.getLocationDistrict().getName();
		String address4 = customer.getLocationDistrict().getProvinceName();
		String address5 = customer.getLocationDistrict().getCountryName();
		String additionalData = customer.getVerificationNumber() + "/" + RandomStringUtils.randomAlphanumeric(12).toUpperCase();
		String contactMobile = customer.getContactMobile();
		MeansOfIdentificationType meansOfIdentificationType = customer.getMeansOfIdentificationType();
		String meansOfIdentificationNumber = customer.getMeansOfIdentificationNumber();
		
		String terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL; 
		String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		//transactionDateFormatted = "20200811T22:27:04";
		//transactionId = "3osntzfclo8coftzs6l6b0mw";
		//additionalData = "4022042605064KK1O08GBD";
		String checksum = "OrderCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(title1)+""+formatDataForTutuka(initials)+""+formatDataForTutukaStripSpecialCharacter(lastName.toUpperCase())+""+formatDataForTutuka(address1)+""+
				formatDataForTutuka(address2)+""+formatDataForTutuka(address3)+""+formatDataForTutuka(address4)+""+formatDataForTutuka(address5)+""+formatDataForTutuka(additionalData)+""+formatDataForTutukaStripSpecialCharacter(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>OrderCard</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(title1)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(initials)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(lastName.toUpperCase())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(address1)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(address2)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(address3)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(address4)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(address5)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(additionalData)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		/*<?xml version="1.0"?><methodCall><methodName>OrderCard</methodName><params><param><value><string>0072201362</string></value></param>
		 * <param><value><string>Mr</string></value></param><param><value><string>James</string></value></param><param><value><string>Paul</string></value></param>
		 * <param><value><string>89AdeniyiRemiClose</string></value></param><param><value><string>LusakaHighRoad</string></value></param>
		 * <param><param><value><string>LUSAKADISTRICT</string></value></param>
		 * <param><param><value><string>LUSAKA</string></value></param><param><param><value><string>Zambia</string></value></param>
		 * <param><value><string>4022042605XXLXFZZHZHAC</string></value></param><param><value><string>wtyq8u4mfag5uwnwot0bo90y</string></value>
		 * </param><param><value><dateTime.iso8601>20200803T015338</dateTime.iso8601></value></param>
		 * <param><value><string>3126bf53da4084083e900fa3a241363bbf6b08e5a0ec25c9fed007f2b6f33b4b</string></value></param></params></methodCall>
		 */
		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			//String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member><name>cvv2</name><value><string>307</string></value></member><member><name>resultCode</name><value><int>1</int></value></member><member><name>cardNumber</name><value><string>5368982634142982</string></value></member><member><name>resultText</name><value><string>Approved</string></value></member><member><name>validDate</name><value><string>07/21</string></value></member><member><name>expiryDate</name><value><dateTime.iso8601>20210729T00:00:00</dateTime.iso8601></value></member><member><name>trackingNumber</name><value><string>807077100000003</string></value></member></struct></value></param></params></methodResponse>";
		    if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
	            String resp_resultCode = null;
	            String resp_resultText = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
			            		resp_resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resp_resultText = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	if(account==null)
			        	{
			        		
			        		Long customerId = customer.getId();
			        		String hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
			    			System.out.println("hql ==" + hql);
			    			Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			    			
			    			
			    			String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
			    					RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
			    			
			    			Date date_now = new Date();
			    			SimpleDateFormat sdf = new SimpleDateFormat("YYdm");
			    			String d_now_s = sdf.format(date_now);
			    			System.out.println(">>>> = " + d_now_s);
			    			long identl = Long.valueOf(ident);
			    			long d_now_l = Long.valueOf(d_now_s);
			    			String sum = ident + "" + d_now_l;
			    			String identifier1 = acquirer.getId() + "" + sum;
			    			String accountNumber = identifier1;
				        	
				        	
				        	hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + "";
				        	Collection<Account> allCustomerAccounts= (Collection<Account>)swpService.getAllRecordsByHQL(hql);
				        	int accountCount = allCustomerAccounts!=null ? allCustomerAccounts.size() : 0;
				        	
				        	JSONObject allSettings = app.getAllSettings();
							String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
							CardScheme accountScheme = null;
							if(defaultAccountSchemeIdObj!=null)
							{
								Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
								accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
							}
							
							
			        		account = new Account(customer, AccountStatus.ACTIVE, null, branchCode, acquirer, currencyCode, AccountType.VIRTUAL, accountNumber, accountCount, 
			        			corporateCustomer, 
			        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
	        					corporateCustomerAccount, 
	        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
	        					ProbasePayCurrency.valueOf(currencyCode), 
	        					null, 
	        					null, accountScheme, isLive);
			        		account = (Account)swpService.createNewRecord(account);
			        	}
			        	
			        	
			        	
			        	ECardRequest cardRequest = new ECardRequest(initials + " " + lastName, title1, address1, address2, address3, address4, address5, additionalData, transactionId, account);
			        	cardRequest = (ECardRequest)swpService.createNewRecord(cardRequest);
			        	
				        JSONObject resp = new JSONObject();
			        	//resp.put("cardRequest", cardRequest);
			        	resp.put("message", "Card Order was successful");
			        	resp.put("status", ERROR.CARD_ORDER_SUCCESSFUL);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "Card order request was not successful");
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "Card order request was not successful");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	//PHYSICAL CARD
	public static JSONObject activateTutukaCard(
			ECard card, SwpService swpService) {
		// TODO Auto-generated method stub

		try
		{
			boolean proceed = true;
			JSONObject respStatus = TutukaHelper.getTutukaCardStatus(card, swpService);
	    	if(respStatus!=null && respStatus.length()>0 && respStatus.has("status") && respStatus.getInt("status")==1)
	    	{
	    		if(respStatus.getBoolean(TutukaCardStatus.VALID.name())==true)
	    		{
	    			proceed = true;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.PIN_BLOCKED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.ACTIVATED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.RETIRED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.LOADED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.REDEEMED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.CANCELLED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.STOPPED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.LOST.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.STOLEN.name())==true)
	    		{
	    			proceed = false;
	    		}
	    		if(respStatus.getBoolean(TutukaCardStatus.EXPIRED.name())==true)
	    		{
	    			proceed = false;
	    		}
	    	}
	    	
	    	if(proceed==false)
	    	{
	    		JSONObject resp = new JSONObject();
	        	resp.put("message", "The card can not be activated due to its current status");
	        	resp.put("status", ERROR.CARD_ACTIVATION_FAILED_DUE_TO_STATUS);
	        	return resp;
	    	}
			String terminalId = null;
			String terminalPassword = null;
			if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
			{
				terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
				terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			}
			else
			{
				terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
				terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			}
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
			String cardTrackingNumber = card.getTrackingNumber();
	
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
			String checksum = "ActivateCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(cardTrackingNumber)+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			String xml = "<?xml version=\"1.0\"?>" + 
			"<methodCall>" + 
			"<methodName>ActivateCard</methodName>" + 
			"<params>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(cardTrackingNumber)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(checksum)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"</params>" + 
			"</methodCall>"; 
	
			
			System.out.println(xml);
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			//String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member><name>cvv2</name><value><string>307</string></value></member><member><name>resultCode</name><value><int>1</int></value></member><member><name>cardNumber</name><value><string>5368982634142982</string></value></member><member><name>resultText</name><value><string>Approved</string></value></member><member><name>validDate</name><value><string>07/21</string></value></member><member><name>expiryDate</name><value><dateTime.iso8601>20210729T00:00:00</dateTime.iso8601></value></member><member><name>trackingNumber</name><value><string>807077100000003</string></value></member></struct></value></param></params></methodResponse>";
		    if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
	            String resp_resultCode = null;
	            String resp_resultText = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
			            		resp_resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resp_resultText = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	//String hql = "Select tp from ECard tp where tp.trackingNumber = '"+ cardTrackingNumber +"' AND tp.status = " + CardStatus.INACTIVE.ordinal() + " AND tp.deleted_at IS NULL";
			        	//ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			        	card.setStatus(CardStatus.ACTIVE);
			        	swpService.updateRecord(card);
			        	
				        JSONObject resp = new JSONObject();
			        	resp.put("card", card);
			        	resp.put("message", "The selected Card has been activated successfully");
			        	resp.put("status", ERROR.CARD_ACTIVATION_SUCCESS);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "The selected Card was not activated successfully");
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "The selected Card was not activated successfully");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", xml);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	public static JSONObject getTutukaCardStatus(ECard card,
			SwpService swpService) {
		// TODO Auto-generated method stub
		try {
			JSONObject resp = new JSONObject();
			String terminalId = null;
			String terminalPassword = null;
			String cardReference = null;
			if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
			{
				terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
				terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
				cardReference = card.getTrackingNumber();
			}
			else if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
			{
				terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL; 
				terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
				cardReference = card.getPan();
			}
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
			//String trackingNumber = card.getTrackingNumber(); 
			//String reference = card.getAccount().getCustomer().getVerificationNumber();
			String reference = card.getSerialNo();
	
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
			String checksum = "Status"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(cardReference)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			String xml = "<?xml version=\"1.0\"?>" + 
			"<methodCall>" + 
			"<methodName>Status</methodName>" + 
			"<params>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(reference)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(cardReference)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
			"</value>" + 
			"</param>" + 
			"<param>" + 
			"<value>" + 
			"<string>"+formatDataForTutuka(checksum)+"</string>" + 
			"</value>" + 
			"</param>" + 
			"</params>" + 
			"</methodCall>"; 
	
			
			System.out.println(xml);
		
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
	            String resp_resultCode = null;
	            String resp_resultText = null;
	            JSONObject statusList = new JSONObject();
		        if(nodes.getLength()>0)
		        {
		        	
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
			            		resp_resultText = nodeValue;
			            		break;
			            	case "valid":
			            		statusList.put(TutukaCardStatus.VALID.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "pinBlocked":
			            		statusList.put(TutukaCardStatus.PIN_BLOCKED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "activated":
			            		statusList.put(TutukaCardStatus.ACTIVATED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "retired":
			            		statusList.put(TutukaCardStatus.RETIRED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "loaded":
			            		statusList.put(TutukaCardStatus.LOADED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "redeemed":
			            		statusList.put(TutukaCardStatus.REDEEMED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "empty":
			            		statusList.put(TutukaCardStatus.EMPTY.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "cancelled":
			            		statusList.put(TutukaCardStatus.CANCELLED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "stopped":
			            		statusList.put(TutukaCardStatus.STOPPED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "lost":
			            		statusList.put(TutukaCardStatus.LOST.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "stolen":
			            		statusList.put(TutukaCardStatus.STOLEN.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	case "expired":
			            		statusList.put(TutukaCardStatus.EXPIRED.name(), nodeValue.equals("1") ? true : false);
			            		break;
			            	
			            }
			        }
			        
			        System.out.println(resp_resultCode);
			        if(resp_resultCode.equals("1"))
			        {
			        	System.out.println(">>>>>>");
			        	resp.put("statusList", statusList);
			        	resp.put("status", ERROR.GENERAL_OK);
			        	return resp;
			        }
			        else
			        {
			        	System.out.println("<<<<<<");
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println("......");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "Status of the card could not be obtained. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	
	//PHYSICAL CARD
	public static JSONObject getTutukaCardBalance(
			String xml, SwpService swpService, Application application, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Balance")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        	
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String narrative = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
					            narrative = nodeValue;
			            		break;
			            	case 3:
			            		transactionData = nodeValue;
			            		break;
			            	case 4:
			            		transactionId = nodeValue;
			            		break;
			            	case 5:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 6:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        if(reference!=null)
			        {
			        	 
			    		String terminalPassword = "";
			    		if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			    			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			    		else if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			    			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			    		
			        	String checksumCheck = "Balance"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
			        			formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionData) +""+
			        			formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			        	System.out.println(logId + "- checksumCheck " + checksumCheck);
			    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
			    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
			    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
			    		
			    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
			    		{
			    			System.out.println(-1);
			    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
				        	resp.put("message", "Checksum mismatch");
				        	return resp;
			    		}
			    		System.out.println(0);
			    		
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	System.out.println(dataJson.toString());
			        	if(dataJson!=null)
			        	{
			        		System.out.println(dataJson);
			        		//String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
				        	String hql = "Select tp.*, cs.minimumBalance, ac.status as account_status, ac.probasePayCurrency, c.status as customer_status from ecards tp, accounts ac, "
				        			+ "customers c, cardschemes cs where "
				        			+ "tp.account_id = ac.id AND ac.customer_id = c.id AND "
				        			+ "tp.deleted_at IS NULL AND tp.cardScheme_id = cs.id AND (tp.serialNo = '"+reference+"') ";
				        			//"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
				        	System.out.println(hql);
				        	List<Map<String, Object>> card_ = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				        	Map<String, Object> card = null;
				        	if(card_!=null && card_.size()>0)
				        	{
				        		card = card_.get(0);
				        	}
				        	
				        	if(card!=null)
				        	{
				        		if(CardStatus.values()[(Integer)card.get("status")].equals(CardStatus.ACTIVE))
				        		{
				        			//if(AccountStatus.values()[(Integer)card.get("account_status")].equals(AccountStatus.ACTIVE))
				        			//{
				        				//if(CustomerStatus.values()[(Integer)card.get("customer_status")].equals(CustomerStatus.ACTIVE))
				        				//{
							        		resp.put("status", ERROR.GENERAL_OK);
							        		Double minimumCardBalance = (Double)card.get("minimumBalance");
							        		if(((Double)card.get("cardBalance") - minimumCardBalance)<0)
							        			resp.put("balance", 0*100);
							        		else
							        			resp.put("balance", ((Double)card.get("cardBalance") - minimumCardBalance)*100);
							        		
							        		resp.put("currentBalance", ((Double)card.get("cardBalance"))*100);
							        		resp.put("cardCurrency", (String)ProbasePayCurrency.values()[(Integer)card.get("probasePayCurrency")].name());
							        		
				        				//}
				        				//else
				        				//{
				        					//resp.put("status", ERROR.GENERAL_FAIL);
				        					//resp.put("message", "Current status of the card owner is not active for transactions");
				        				//}
				        			//}
				        			//else
				        			//{
				        			//	resp.put("status", ERROR.GENERAL_FAIL);
			        				//	resp.put("message", "Current wallet status of the card owner is not active for transactions");
				        			//}
				        		}
				        		else
				        		{
				        			resp.put("status", ERROR.GENERAL_FAIL);
		        					resp.put("message", "Current status of the card is not active for transactions");
				        		}
				        	}
				        	else
				        	{
				        		resp.put("status", ERROR.GENERAL_FAIL);
				        		resp.put("message", "Invalid transaction data provided");
				        	}
				        	return resp;

			        	}
			        	System.out.println(5);
			        	resp.put("status", ERROR.TRANSACTION_DATA_UNPARSEABLE);
			        	resp.put("message", "Invalid transaction data provided");
			        	return resp;
			        }
			        else
			        {
			        	System.out.println(6);
			        	resp.put("status", ERROR.CARD_NOT_FOUND);
			        	resp.put("message", "The debit could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println(7);
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The debit could not be implemented. Please try again");
		        	resp.put("xmlMessage", xml);
		        	return resp;
		        }
		    }
		    System.out.println(8);
		    return null;
		    
		} catch (Exception e) {
			System.out.println(9);
			log.error("error", e);
		     e.printStackTrace();
		     return null;
		}

	}
	
	//PHYSICAL CARD
	
	
	public static JSONObject linkTutukaCardToCustomer(CardScheme cardScheme, String currencyCode, String branch_code, String bankCode, 
			Customer customer, Account customerAccount, ECardBin cardBin, SwpService swpService, Integer isLive) throws Exception {
		// TODO Auto-generated method stub

		Application app = Application.getInstance(swpService);
		String firstName = customer.getFirstName();
		String lastName = customer.getLastName();
		String contactMobile = customer.getContactMobile();
		MeansOfIdentificationType meansOfIdentificationType = customer.getMeansOfIdentificationType();
		String meansOfIdentificationNumber = customer.getMeansOfIdentificationNumber();
		
		
		String hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
		System.out.println("hql ==" + hql);
		Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
		
		hql = "Select tp from ECard tp where tp.account.customer.id = " + customer.getId();
		System.out.println("hql ==" + hql);
		Collection<ECard> cardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
		
		String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
				RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
		
		Date date_now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("YYdm");
		String d_now_s = sdf.format(date_now);
		System.out.println(">>>> = " + d_now_s);
		long identl = Long.valueOf(ident);
		long d_now_l = Long.valueOf(d_now_s);
		String sum = ident + "" + d_now_l;
		String identifier1 = cardBin.getAcquirer().getId() + "" + sum;
		String accountNumber = identifier1;
		String cardSerialNo = accountNumber + "" + cardList.size();
		String reference = cardSerialNo;
		
		String terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL; 
		String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		//transactionDateFormatted = "20200812T02:05:18";
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "LinkCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
				formatDataForTutuka(cardBin.getPan())+""+formatDataForTutuka(firstName)+""+
						formatDataForTutuka(lastName)+""+formatDataForTutuka(meansOfIdentificationNumber)+""+
				formatDataForTutuka(contactMobile)+""+formatDataForTutukaStripSpecialCharacter(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>LinkCard</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(cardBin.getPan())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(firstName)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(lastName)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(meansOfIdentificationNumber)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(contactMobile)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutukaStripSpecialCharacter(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			//String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member><name>cvv2</name><value><string>307</string></value></member><member><name>resultCode</name><value><int>1</int></value></member><member><name>cardNumber</name><value><string>5368982634142982</string></value></member><member><name>resultText</name><value><string>Approved</string></value></member><member><name>validDate</name><value><string>07/21</string></value></member><member><name>expiryDate</name><value><dateTime.iso8601>20210729T00:00:00</dateTime.iso8601></value></member><member><name>trackingNumber</name><value><string>807077100000003</string></value></member></struct></value></param></params></methodResponse>";
		    if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
	            String resp_resultCode = null;
	            String resp_resultText = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
			            		resp_resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resp_resultText = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	Long customerId = customer.getId();
			        	Customer corporateCustomer  = null;
			        	Account corporateCustomerAccount = null;
			        	
			        	hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + "";
			        	Collection<Account> allCustomerAccounts= (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			        	int accountCount = allCustomerAccounts!=null ? allCustomerAccounts.size() : 0;
			        	
			        	
			        	JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						
						
			        	Account account = new Account(customer, AccountStatus.ACTIVE, null, branch_code, cardBin.getAcquirer(), currencyCode, 
			        			AccountType.VIRTUAL, accountNumber, accountCount, 
			        			corporateCustomer, 
			        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
	        					corporateCustomerAccount, 
	        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
	        					ProbasePayCurrency.valueOf(currencyCode), 
	        					null, 
	        					null, accountScheme, isLive);
			        	
			        	
			        	
			        	account.setAccountBalance(0.00);
			        	account = (Account)swpService.createNewRecord(account);
			        	
			        	
						String pin = null;
						Date expiryDate = null; 
						String cvv = null;
						Acquirer issuer = cardBin.getAcquirer();
						Issuer acquirer = cardBin.getIssuer();
						String otp = null;
						
			        	ECard card = new ECard(firstName + " " + lastName, cardBin.getPan(), pin, expiryDate, customerId, cvv, otp, issuer, 
		        			acquirer, cardScheme, account, account.getId(), 
		        			resp_resultCode.equals("1") ? CardStatus.ACTIVE : CardStatus.INACTIVE, CardType.TUTUKA_PHYSICAL_CARD, 
			        		false, cardSerialNo, corporateCustomer==null ? false : true, 
	        				corporateCustomer, 
		        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
        					corporateCustomerAccount, 
        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
			        		cardBin.getTrackingNumber(), 
			        		isLive
				        );
			        	
			        	card.setCardBalance(0.00);
			        	card = (ECard)swpService.createNewRecord(card);
			        	
			        	
			        	

			        	System.out.println("card Id..." + card.getId());
			        	System.out.println("card tracking number...." + card.getTrackingNumber());
			        	System.out.println("card pan...." + card.getPan() );
			        	card.setUpdated_at(new Date());
			        	swpService.updateRecord(card);
			        	
			        	
			        	cardBin.setStatus(CardStatus.ISSUED);
			        	swpService.updateRecord(cardBin);

				        JSONObject resp = new JSONObject();
			        	resp.put("card", card);
			        	resp.put("message", "Card has been linked to the specified customer successfully");
			        	resp.put("status", ERROR.CARD_LINKED_SUCCESSFULLY);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else if(resp_resultCode.equals("-779"))
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "The selected card has already been linked previously to another customer");
			        	resp.put("status", ERROR.CARD_ALREADY_LINKED);
			        	return resp;
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "Card was not linked to the specified customer");
			        	resp.put("status", ERROR.CARD_COULD_NOT_BE_LINKED);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "Invalid xml response message");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	
	public static JSONObject getCustomerActiveTutukaCards(String walletReference, CardType cardType,
			SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		String terminalId = null; 
		String terminalPassword = null;
		if(cardType.equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		}
		else if(cardType.equals(CardType.TUTUKA_PHYSICAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL; 
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
		}
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = walletReference;

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "GetActiveLinkedCards"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>GetActiveLinkedCards</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
			            	case "cards":
			            		NodeList members = node1.getElementsByTagName("member");
			            		for(int j=0; j<members.getLength(); j++)
			            		{
			            			JSONObject jsonObjectEntry = new JSONObject();
			            			Element memberItem = (Element)members.item(j);
			            			NodeList memberNameNodeList = memberItem.getElementsByTagName("name");
			            			Element memberNameElement = (Element)memberNameNodeList.item(0);
			            			String memberName = memberNameElement.getTextContent();

			            			memberNameNodeList = memberItem.getElementsByTagName("value");
			            			Element node2 = (Element)memberNameNodeList.item(0);
			            			Node node3 = node2.getFirstChild();
			            			String memberValue = node3.getTextContent();
			            			
			            			switch(memberName)
			            			{
			            				case "cvv2":
			            					jsonObjectEntry.put("cvv2", memberValue);
			            					break;
			            				case "cardNumber":
			            					jsonObjectEntry.put("cardNumber", memberValue);
			            					break;
			            				case "validDate":
			            					jsonObjectEntry.put("validDate", memberValue);
			            					break;
			            				case "expiryDate":
			            					jsonObjectEntry.put("expiryDate", memberValue);
			            					break;
			            				case "trackingNumber":
			            					jsonObjectEntry.put("trackingNumber", memberValue);
			            					break;
			            				default:
			            					break;
			            			}
			            			cardArray.put(jsonObjectEntry);
			            		}
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        System.out.println(resp_resultCode);
			        if(resp_resultCode.equals("1"))
			        {
			        	System.out.println(">>>");
			        	resp.put("status", ERROR.ACTIVE_LINKED_CARDS_AVAILABLE);
			        	resp.put("cardEntries", cardArray);
			        	return resp;
			        }
			        else
			        {
			        	System.out.println("<<<");
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.ACTIVE_LINKED_CARDS_UNAVAILABLE);
			        	resp.put("resp_message", resp_resultText);
			        	resp.put("message", "The cards linked to the customer could not be found. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println("<<<>>>");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The cards linked to the customer could not be found. Invalid Message. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	//PHYSICAL
	
	public static JSONObject changeTutukaCardPin(
			Customer customer, ECard card, String newPin, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		
		String terminalId = null; 
		 
		String terminalPassword = null;
		if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		}
		else
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
		}
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = customer.getVerificationNumber();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "ChangePin"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
				formatDataForTutuka(card.getTrackingNumber())+""+
				formatDataForTutuka(newPin)+""+formatDataForTutukaStripSpecialCharacter(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>ChangePin</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(newPin)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutukaStripSpecialCharacter(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
			        	{
				        	card.setPin(newPin);
				        	swpService.updateRecord(card);
			        	}
			        	resp.put("status", ERROR.GENERAL_OK);
			        	resp.put("cardEntries", cardArray);
			        	return resp;
			        }
			        else if(resp_resultCode.equals("-38"))
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.CARD_LOST);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
			        else if(resp_resultCode.equals("-39"))
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.CARD_STOLEN);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
			        else if(resp_resultCode.equals("-10"))
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.PIN_TRIES_EXCEEDED);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The cards linked to the customer could not be found. Please try again");
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	

	//PHYSICAL
	public static JSONObject resetTutukaCardPin(
			Customer customer, ECard card, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		String terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
		String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = customer.getVerificationNumber();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "ResetPin"+terminalId+""+reference+""+card.getTrackingNumber()+""+transactionId+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>ResetPin</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+formatDataForTutuka(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            line = (Element) title.item(0);
			            NodeList valueNode = line.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	card.setChangedCardPin(Boolean.FALSE);
			        	swpService.updateRecord(card);
			        	resp.put("status", 1);
			        	resp.put("cardEntries", cardArray);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", 0);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", 0);
		        	resp.put("message", "The cards linked to the customer could not be found. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	
	//PHYSICAL
	public static JSONObject updateTutukaCardBearer(
			Customer customer, ECard card, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		
		String terminalId = null;
		String terminalPassword = null;
		if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		}
		else if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
		}
		
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = card.getSerialNo();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "UpdateBearer"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(card.getPan())
				+""+formatDataForTutuka(customer.getFirstName())+""+formatDataForTutuka(customer.getLastName())+""+formatDataForTutuka(customer.getMeansOfIdentificationNumber())+""+formatDataForTutuka(customer.getContactMobile())
				+""+formatDataForTutukaStripSpecialCharacter(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>UpdateBearer</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getPan())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(customer.getFirstName())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(customer.getLastName())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(customer.getMeansOfIdentificationNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(customer.getContactMobile())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutukaStripSpecialCharacter(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	resp.put("status", 1);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", 0);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", 0);
		        	resp.put("message", "The cards linked to the customer could not be found. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	

	//PHYSICAL
	public static JSONObject transferTutukaCard(
			Customer customer, ECard card, ECardBin newCard, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		String terminalId = null;
		String terminalPassword = null;
		if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		}
		else if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
		}
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = card.getSerialNo();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "TransferLink"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)
				+""+formatDataForTutuka(card.getTrackingNumber())+""+formatDataForTutuka(newCard.getTrackingNumber())
				+""+formatDataForTutukaStripSpecialCharacter(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>TransferLink</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(newCard.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutukaStripSpecialCharacter(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	
			        	
			        	
			        	Customer corporateCustomer = card.getCorporateCustomer();
			        	Account corporateCustomerAccount = card.getCorporateCustomerAccount();
			        	ProbasePayCurrency currencyCode = card.getAccount().getProbasePayCurrency();
			        	String firstName = card.getAccount().getCustomer().getFirstName();
			        	String lastName = card.getAccount().getCustomer().getLastName();
			        	String pin = null;
			        	Long customerId = card.getCustomerId();
			        	Acquirer acquirer = card.getAcquirer();
			        	Issuer issuer = card.getIssuer();
			        	String branchCode = card.getAccount().getBranchCode();
			        	CardScheme cardScheme = card.getCardScheme();
			        	
			        	String hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
						System.out.println("hql ==" + hql);
						Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
						
						hql = "Select tp from ECard tp where tp.account.customer.id = " + customer.getId();
						System.out.println("hql ==" + hql);
						Collection<ECard> cardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
						
						String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
								RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
						
						Date date_now = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("YYdm");
						String d_now_s = sdf.format(date_now);
						System.out.println(">>>> = " + d_now_s);
						long identl = Long.valueOf(ident);
						long d_now_l = Long.valueOf(d_now_s);
						String sum = ident + "" + d_now_l;
						String identifier1 = acquirer.getId() + "" + sum;
						String accountNumber = identifier1;
						
						
						
			        	
						
						
			        	ECard newECard = new ECard(firstName + " " + lastName, newCard.getPan(), pin, resp_expiryDate, customerId, resp_cvv, null, acquirer, 
		        			issuer, cardScheme, card.getAccount(), card.getAccount().getId(), resp_resultCode.equals("1") ? CardStatus.ACTIVE : CardStatus.INACTIVE, card.getCardType(), 
			        		false, card.getSerialNo(), corporateCustomer==null ? false : true, 
	        				corporateCustomer, 
		        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
        					corporateCustomerAccount, 
        					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
			        		newCard.getTrackingNumber(), 
			        		card.getIsLive()
				        );
			        	
			        	swpService.createNewRecord(newECard);
			        	card.setStatus(CardStatus.DELETED);
			        	swpService.updateRecord(card);
			        	newCard.setStatus(CardStatus.ISSUED);
			        	swpService.updateRecord(newCard);
			        	
			        	resp.put("status", ERROR.CARD_TRANSFERRED_SUCCESSFULLY);
			        	resp.put("card", card);
			        	return resp;
			        }
			        else if(resp_resultCode.equals("-779"))
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.CARD_ALREADY_LINKED);
			        	resp.put("message", "New Card To Be Linked Could Not Be Linked. Card Has Already Been Linked Previously");
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "Invalid response message could not be parsed");
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	

	
	public static JSONObject stopTutukaCard(
			Customer customer, ECard card, StopCardReason stopCardReason, String notes, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		String terminalId = null;
		String terminalPassword = null;
		String cardReference = null;
		if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			cardReference = card.getTrackingNumber();
		}
		else if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			cardReference = card.getPan();
		}
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		//String reference = customer.getVerificationNumber();
		String reference = card.getSerialNo();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "StopCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(cardReference)+""+formatDataForTutuka(stopCardReason.getValue()+"")+""+formatDataForTutuka(notes)
				+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>StopCard</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(cardReference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(stopCardReason.getValue()+"")+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(notes)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	card.setStopReason(stopCardReason);
			        	card.setStopReasonDescription(notes);
			        	card.setStopFlag(true);
			        	card.setStatus(CardStatus.STOPPED);
			        	swpService.updateRecord(card);
			        	
			        	resp.put("status", ERROR.CARD_STOPPED_SUCCESSFULLY);
			        	return resp;
			        }
			        else if(resp_resultCode.equals("-5"))
			        {
			        	resp.put("status", ERROR.CARD_ALREADY_STOPPED);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.GENERAL_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card could not be stopped. Please try again");
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	

	public static JSONObject unstopTutukaCard(
			Customer customer, ECard card, String notes, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		String terminalId = null;
		String terminalPassword = null;
		String cardReference = null;
		if(card.getCardType().equals(CardType.TUTUKA_VIRTUAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			cardReference = card.getTrackingNumber();
		}
		else if(card.getCardType().equals(CardType.TUTUKA_PHYSICAL_CARD))
		{
			terminalId = TutukaHelper.TERMINAL_ID_PHYSICAL;
			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			cardReference = card.getPan();
		}
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = customer.getVerificationNumber();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "UnstopCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(cardReference)
				+""+formatDataForTutuka(notes)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>UnstopCard</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(cardReference)+"</string>" + 
		"</value>" + 
		"</param>" +  
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(notes)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	card.setStopFlag(null);
			        	card.setStopReason(null);
			        	card.setStopReasonDescription(notes);
			        	card.setStatus(CardStatus.ACTIVE);
			        	swpService.updateRecord(card);
			        	resp.put("status", ERROR.CARD_UNSTOPPED_SUCCESSFULLY);
			        	resp.put("card", card.getSummary());
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.CARD_UNSTOP_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card could not be unstopped. Invalid xml message received Please try again");
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	

	//VIRTUAL
	
	
	
	public static JSONObject updateTutukaCardCVV(
			Customer customer, ECard card, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		String terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
		String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = customer.getVerificationNumber();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "UpdateCVV"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(card.getTrackingNumber())
				+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>UpdateCVV</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
			            	case "cvv2":
					            nodeValue = node1.getTextContent();
			            		resp_cvv = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	card.setCvv(resp_cvv);
			        	swpService.updateRecord(card);
			        	
			        	resp.put("status", ERROR.CARD_CVV_UPDATED_SUCCESSFULLY);
			        	resp.put("card", card);
			        	resp.put("cvv", resp_cvv);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.CARD_CVV_UPDATE_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card cvv could not be updated. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	
	
	
	

	public static JSONObject retireTutukaCard(
			Customer customer, ECard card, SwpService swpService) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		String terminalId = TutukaHelper.TERMINAL_ID_VIRTUAL; 
		String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();
		String reference = customer.getVerificationNumber();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
		String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
		String checksum = "RetireCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(card.getTrackingNumber())
				+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);
		String xml = "<?xml version=\"1.0\"?>" + 
		"<methodCall>" + 
		"<methodName>RetireCard</methodName>" + 
		"<params>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(terminalId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(reference)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(card.getTrackingNumber())+"</string>" + 
		"</value>" + 
		"</param>" +
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(transactionId)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<dateTime.iso8601>"+(transactionDateFormatted)+"</dateTime.iso8601>" + 
		"</value>" + 
		"</param>" + 
		"<param>" + 
		"<value>" + 
		"<string>"+formatDataForTutuka(checksum)+"</string>" + 
		"</value>" + 
		"</param>" + 
		"</params>" + 
		"</methodCall>"; 

		
		System.out.println(xml);
		try {
		    Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getEntity(String.class);
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resp_pan = null;
	            String resp_cvv = null;
	            Date resp_expiryDate = null;
	            String resp_trackingNumber = null;
	            String resp_validDate = null;
	            String resp_resultCode = null;
	            String resp_resultText = null;

	            String nodeValue;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
					            nodeValue = node1.getTextContent();
			            		resp_resultCode = nodeValue;
			            		break;
			            	case "resultText":
					            nodeValue = node1.getTextContent();
			            		resp_resultText = nodeValue;
			            		break;
            				default:
            					break;
			            	
			            }
			            
			            
			        }
			        
			        if(resp_resultCode.equals("1"))
			        {
			        	card.setStatus(CardStatus.RETIRED);
			        	swpService.updateRecord(card);
			        	
			        	resp.put("status", ERROR.CARD_RETIRED_SUCCESSFULLY);
			        	resp.put("cardEntries", cardArray);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("cardEntries", new JSONArray());
			        	resp.put("status", ERROR.CARD_RETIRE_FAIL);
			        	resp.put("message", resp_resultText);
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card could not be retired. Invalid messaging. Please try again");
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	public static JSONObject createNewMPQRTutukaCard(MPQRData mpqrData, String trackingNumber, CustomerType customerType, MeansOfIdentificationType meansOfIdentificationType, 
			String meansOfIdentificationNumber, Device mpqrDevice, Acquirer issuer, Issuer acquirer, CardScheme cardScheme, String branchCode, 
			String currencyCode, Customer corporateCustomer, Account corporateCustomerAccount, SwpService swpService, Integer isLive) {
		// TODO Auto-generated method stub
		try {
			Application app = Application.getInstance(swpService);
			if(mpqrDevice==null)
			{
				JSONObject resp = new JSONObject();
	        	resp.put("message", "Specify the device you are setting up a QR code for");
	        	resp.put("status", ERROR.DEVICE_EXIST_FAIL);
	        	return resp;
			}
			if(mpqrDevice.getSettlementCardId()!=null)
			{
				JSONObject resp = new JSONObject();
	        	resp.put("message", "Device already has an MPQR attached to it");
	        	resp.put("status", ERROR.DEVICE_MPQR_EXIST);
	        	return resp;
			}
			String hql = "Select tp from Account tp where tp.customer.device.id = " + mpqrDevice.getId();
			System.out.println("hql ==" + hql);
			Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			Collection<ECard> cardList = null;
			Customer customer = null;
			if(customerAccountList!=null && customerAccountList.size()>0)
			{
				customer = customerAccountList.iterator().next().getCustomer();
				hql = "Select tp from ECard tp where tp.account.customer.id = " + customer.getId();
				System.out.println("hql ==" + hql);
				cardList = (Collection<ECard>)swpService.getAllRecordsByHQL(hql);
			}
			
			String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
					RandomStringUtils.randomNumeric(customerAccountList.size()>9 ? 4 : 5) + "" + customerAccountList.size();
			
			Date date_now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("YYdm");
			String d_now_s = sdf.format(date_now);
			System.out.println(">>>> = " + d_now_s);
			long identl = Long.valueOf(ident);
			long d_now_l = Long.valueOf(d_now_s);
			String sum = ident + "" + d_now_l;
			String identifier1 = acquirer.getId() + "" + sum;
			String accountNumber = identifier1;
			String cardSerialNo = accountNumber + "" + (cardList==null ? 0 : cardList.size());
			
			if(customer==null)
			{
				String verificationNumber = RandomStringUtils.randomAlphanumeric(24).toUpperCase();
				String firstName = mpqrDevice.getMerchant().getUser().getFirstName();
				String lastName = mpqrDevice.getMerchant().getUser().getLastName();
				String otherName = mpqrDevice.getMerchant().getUser().getOtherName();
				Date dateOfBirth = null;
				Gender gender = null;
				String addressLine1 = mpqrDevice.getMerchant().getAddressLine1();
				String addressLine2 = mpqrDevice.getMerchant().getAddressLine2();
				District locationDistrict = null;
				User user = mpqrDevice.getMerchant().getUser();
				String contactMobile = mpqrDevice.getMerchant().getContactMobile();
				String altContactMobile = mpqrDevice.getMerchant().getContactMobile();
				String contactEmail = mpqrDevice.getMerchant().getContactEmail();
				String altContactEmail = mpqrDevice.getMerchant().getContactEmail();
				String customerImage = null;
				String mobileMoneyPassword = null;
				
				
				
				customer = new Customer(verificationNumber, firstName, lastName, otherName, dateOfBirth, gender, addressLine1, addressLine2, 
						locationDistrict, contactMobile, altContactMobile, contactEmail, altContactEmail, CustomerStatus.ACTIVE, customerImage, customerType, 
						meansOfIdentificationType, meansOfIdentificationNumber, mpqrDevice, user);
				customer = (Customer)swpService.createNewRecord(customer);
			}
			
			String firstName = customer.getFirstName();
			String lastName = customer.getLastName();
			String contactMobile = customer.getContactMobile();
			meansOfIdentificationType = customer.getMeansOfIdentificationType();
			meansOfIdentificationNumber = customer.getMeansOfIdentificationNumber();
			
			String terminalId = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID; 
			String terminalPassword = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD;
			
			System.out.println("RECEIVING_INSTITUTION_TERMINAL_ID..." + TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID);
			System.out.println("RECEIVING_INSTITUTION_TERMINAL_PASSWORD..." + TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD);
			System.out.println("TUTUKA_TEST_ENDPOINT_QR_CREATE_CARD..." + TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_CREATE_CARD);
			
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
	
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
			String checksum = "CreateLinkedCard"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(cardSerialNo)+""+formatDataForTutuka(firstName)+""+
					formatDataForTutuka(lastName)+""+formatDataForTutuka(meansOfIdentificationNumber)+""+
					formatDataForTutuka(contactMobile)+""+(expiryDateFormatted)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			
        	String pin = Integer.valueOf(RandomStringUtils.randomNumeric(4)) + "";
        	Long customerId = customer.getId();
        	
        	
        	hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + "";
        	Collection<Account> allCustomerAccounts= (Collection<Account>)swpService.getAllRecordsByHQL(hql);
        	int accountCount = allCustomerAccounts!=null ? allCustomerAccounts.size() : 0;
        	
        
        	JSONObject allSettings = app.getAllSettings();
			String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
			CardScheme accountScheme = null;
			if(defaultAccountSchemeIdObj!=null)
			{
				Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
				accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
			}
        	Account account = new Account(customer, AccountStatus.ACTIVE, null, branchCode, issuer, currencyCode, AccountType.VIRTUAL, accountNumber, accountCount, 
        			corporateCustomer, 
        			corporateCustomer!=null ? corporateCustomer.getId() : null, 
					corporateCustomerAccount, 
					corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
					ProbasePayCurrency.valueOf(currencyCode), 
					null, null, accountScheme, isLive);
        	account = (Account)swpService.createNewRecord(account);
        	
        	
        	
        	
        	
        	
        	ECard card = new ECard(firstName + " " + lastName, mpqrData.getQrCardNumber(), pin, null, customerId, null, null, issuer, 
    			acquirer, cardScheme, account, account.getId(), CardStatus.ACTIVE, CardType.TUTUKA_DEVICE_SETTLEMENT_CARD, 
        		false, cardSerialNo, corporateCustomer==null ? false : true, 
				corporateCustomer, 
    			corporateCustomer!=null ? corporateCustomer.getId() : null, 
				corporateCustomerAccount, 
				corporateCustomerAccount!=null ? corporateCustomerAccount.getId() :null, 
        		trackingNumber, isLive
	        );
        	card = (ECard)swpService.createNewRecord(card);
        	
        	mpqrDevice.setSettlementCardId(card.getId());
        	swpService.updateRecord(mpqrDevice);
        	
        	
        	mpqrData.setCardId(card.getId());
        	swpService.updateRecord(mpqrData);

        	System.out.println("card Id..." + card.getId());
        	System.out.println("card tracking number...." + card.getTrackingNumber());
        	System.out.println("card pan...." + card.getPan() );
        	System.out.println("card cvv..." + card.getCvv());
        	
        	
	        JSONObject resp = new JSONObject();
	        resp.put("card", card);
	        resp.put("message", "Card creation was successful");
	        resp.put("status", ERROR.CARD_CREATED_SUCCESSFULLY);
        	return resp;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	public static JSONObject createQRData(String branchCode, Device device, SwpService swpService, User setUpByUser, Account walletAccount, MPQRDataType mpqrDataType) {
		// TODO Auto-generated method stub
		try {
			
			if(!device.getSwitchToLive().equals(walletAccount.getIsLive()))
			{
				JSONObject resp = new JSONObject();
	        	resp.put("message", "MPQR Account Setup Was Not Successful. You are trying to create a MasterCard QR Pass using an account that is on " + (walletAccount.getIsLive().equals(Boolean.TRUE) ? " live mode" : " test mode") + ". This wont work.");
	        	resp.put("status", ERROR.MPQR_ACCOUNT_NOT_CREATED_SUCCESSFULLY);
	        	return resp;
			}
			String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
					RandomStringUtils.randomNumeric(4) + "" + 0;
			
			
			String terminalId = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID; 
			String terminalPassword = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD;
			
			System.out.println("TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL ..." + TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL);
			System.out.println("TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID ..." + TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID);
			System.out.println("TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD ..." + TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD);
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
			int endLength = device.getMerchant().getMerchantName().length() > (39-device.getDeviceCode().length()) ? (39-device.getDeviceCode().length()) : device.getMerchant().getMerchantName().length();
			String merchantName = device.getMerchant().getMerchantName().substring(0, endLength);
			String city = device.getMerchant().getCity()!=null ? device.getMerchant().getCity().substring(0, device.getMerchant().getCity().length()>25 ? 25 : device.getMerchant().getCity().length()) : "Lusaka";
			String isocode = device.getMerchant().getCountryOfOperation().getIsoCode()==null ? "ZMB" : device.getMerchant().getCountryOfOperation().getIsoCode();
			String deviceCode = device.getDeviceCode();
			String merchantCategoryCode = "5112";
			String optionalData = "";
	
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss'+0000'");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			//transactionDateFormatted = "20200819T11:24:17+0000";
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toUpperCase();
			//String qrCodeId = card.getSerialNo();
			String qrCodeId =  RandomStringUtils.randomAlphanumeric(48).toUpperCase();
			qrCodeId = "WIXA8O1TBPDIPJXWU8YCZNTV1KANKO1CWPYWXRWUF668MPYC";
			System.out.println("qrCodeId ... " + qrCodeId); 
			String checksum = "CreateQRData"+formatDataForTutuka(terminalId)+""+merchantCategoryCode+""+formatDataForTutuka(merchantName)+""+formatDataForTutuka(city)+""+
					formatDataForTutuka(isocode)+""+formatDataForTutuka(qrCodeId)+""+formatDataForTutuka(transactionId)+""+
					(transactionDateFormatted)+""+formatDataForTutuka(optionalData);
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksumSha1(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			String xml = "<methodCall>" +
					"<methodName>CreateQRData</methodName>" +
						"<params>" +
							"<param><value><string>"+formatDataForTutuka(terminalId)+"</string></value></param>" +
							"<param><value><string>5112</string></value></param>" +
							"<param><value><string>"+formatDataForTutuka(merchantName) +"</string></value></param>" +
							"<param><value><string>"+ formatDataForTutuka(city) +"</string></value></param>" +
							"<param><value><string>"+ formatDataForTutuka(isocode) +"</string></value></param>" +
							"<param><value><string>"+ formatDataForTutuka(qrCodeId) +"</string></value></param>" +
							"<param><value><string>"+ formatDataForTutuka(transactionId) +"</string></value></param>" +
							"<param><value><dateTime.iso8601>"+transactionDateFormatted+"</dateTime.iso8601></value></param>" +
							"<param><value><struct/></value></param>" +
							"<param><value><string>"+checksum+"</string></value></param>" +
						"</params>" +
					"</methodCall>"; 
							
			System.out.println(xml);
		
		   /*Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }*/

		    // display response
		    /*String output = response.getEntity(String.class); */
			/*Unirest.setTimeouts(0, 0);
			HttpResponse<String> response = Unirest.post(TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL)
			  .header("Content-Type", "application/xml")
			  .body(xml)
			  .asString();

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }

		    // display response
		    String output = response.getBody();*/
		    String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><methodResponse><params><param><value><struct><member><name>resultCode</name><value><double>1</double></value></member><member><name>cardNumber</name><value><double>5368981443205816</double></value></member><member><name>resultText</name><value><string>Approved</string></value></member><member><name>qrCodeImage</name><value><string>/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAJYAlgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKA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/wDba9A+GXwy/wCFc/2p/wATf+0Pt/lf8u3lbNm//bbOd/t0oA9AooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigD/9k=</string></value></member><member><name>qrCodeString</name><value><string>00020101021104155368981443205815204511253039675802ZM5907Probase6006Lusaka630437D4</string></value></member></struct></value></param></params></methodResponse>";
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");/**/
			

			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resultCode = null;
	            String resultText = null;
	            String qrCodeString = null;
	            String qrCodeImage = null;
	            String qrCardNumber = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "qrCodeString":
			            		qrCodeString = nodeValue;
			            		break;
			            	case "qrCodeImage":
			            		qrCodeImage = (nodeValue);
			            		break;
			            	case "resultCode":
			            		resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resultText = (nodeValue);
			            		break;
			            	case "cardNumber":
			            		qrCardNumber = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resultCode.equals("1"))
			        {
			        	String pin = null;
			        	Long deviceId = device.getId();
			        	String trackingNumber = null;
			        	
			        	
						
			        	String imageString = qrCodeImage;

			        	// create a buffered image
			        	byte[] data = DatatypeConverter.parseBase64Binary(imageString);
			        	String fileName = RandomStringUtils.randomAlphanumeric(15).toLowerCase() + ".jpg";
			            String path = "/var/www/html/domains/probasepay.com/public_html2.0/qr_data_images/" + fileName;
			            File file = new File(path);
			            
			            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
			                outputStream.write(data);
			                Set<PosixFilePermission> crunchifyPermissions = new HashSet<PosixFilePermission>();
			        		crunchifyPermissions.add(PosixFilePermission.OWNER_READ);
			        		crunchifyPermissions.add(PosixFilePermission.OWNER_WRITE);
			        		crunchifyPermissions.add(PosixFilePermission.OWNER_EXECUTE);
			        		crunchifyPermissions.add(PosixFilePermission.GROUP_READ);
			        		crunchifyPermissions.add(PosixFilePermission.GROUP_EXECUTE);
			        		crunchifyPermissions.add(PosixFilePermission.OTHERS_READ);
			        		crunchifyPermissions.add(PosixFilePermission.OTHERS_EXECUTE);
			        		
			        		Files.setPosixFilePermissions(Paths.get(path), crunchifyPermissions);
			        		
			            } catch (IOException e) {
			                e.printStackTrace();
			                fileName = null;
			            }
			        	
			        	MPQRData mpqrData = new MPQRData(qrCodeId, qrCardNumber, qrCodeString, qrCodeImage, device.getId(), null, MPQRDataStatus.ACTIVE, setUpByUser, 
			        			walletAccount, fileName, mpqrDataType, device.getSwitchToLive());
			        	swpService.createNewRecord(mpqrData);
			        	
			        	String hql = "Select tp from MerchantBankAccount tp where tp.merchant.id = " + device.getMerchant().getId() + " AND tp.status = 1";
			        	MerchantBankAccount  merchantBankAccount = (MerchantBankAccount)swpService.getUniqueRecordByHQL(hql);
			        	
			        	
			        	DeviceBankAccount deviceBankAccount = new DeviceBankAccount(device.getMerchant().getBankAccount(), merchantBankAccount.getBankBranchCode(), merchantBankAccount.getMerchantBank().getId(),
			        			merchantBankAccount.getMerchantBank().getBankName(), device.getId(), merchantBankAccount.getBankAccountName(), null,
								true, setUpByUser.getId(), walletAccount.getId(), walletAccount.getAccountIdentifier(), device.getSwitchToLive());
			        	swpService.createNewRecord(deviceBankAccount);
			        	
				        JSONObject resp = new JSONObject();
				        resp.put("mpqrDataFileName", fileName);
				        resp.put("qrCodeId", qrCodeId);
				        resp.put("message", "MPQR Account Setup Was Successful");
				        resp.put("status", ERROR.MPQR_ACCOUNT_CREATED_SUCCESSFULLY);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "MPQR Account Setup Was Not Successful");
			        	resp.put("status", ERROR.MPQR_ACCOUNT_NOT_CREATED_SUCCESSFULLY);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "MPQR Account Setup Was Not Successful");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	

	public static JSONObject deactivateQRData(MPQRData mpqrData, SwpService swpService) {
		// TODO Auto-generated method stub
		try {
			String hql = "Select tp from ECard tp where tp.id = " + mpqrData.getCardId();
			ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			
			String ident = (AccountType.VIRTUAL.ordinal() + 1) + "" + 
					RandomStringUtils.randomNumeric(4) + "" + 0;
			
			
			String terminalId = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID; 
			String terminalPassword = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD;
			Date currentDate = new Date();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, 1);
			Date expiryDate = calendar.getTime();
			
			DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss'+0000'");
			String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
			String transactionDateFormatted = simpleDateFormatISO8601.format(new Date());
			String transactionId = RandomStringUtils.randomAlphanumeric(24).toLowerCase();
			String mpqrDataCardNumber = mpqrData.getQrCardNumber();
			//String cardPan = card.getPan
			mpqrDataCardNumber = "5368981544068782";
			
			String checksum = "DeactivateQR"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(mpqrDataCardNumber)+""+formatDataForTutuka(transactionId)+""+
					(transactionDateFormatted);
			System.out.println("checksum raw --- " + checksum);
			checksum = TutukaHelper.generateChecksumSha1(checksum, terminalPassword);
			System.out.println("checksum -- " + checksum);
			String xml = "<methodCall>" +
					"<methodName>DeactivateQR</methodName>" +
						"<params>" +
							"<param><value><string>"+formatDataForTutuka(terminalId)+"</string></value></param>" +
							"<param><value><string>"+formatDataForTutuka(mpqrDataCardNumber) +"</string></value></param>" +
							"<param><value><string>"+ formatDataForTutuka(transactionId) +"</string></value></param>" +
							"<param><value><dateTime.iso8601>"+transactionDateFormatted+"</dateTime.iso8601></value></param>" +
							"<param><value><string>"+checksum+"</string></value></param>" +
						"</params>" +
					"</methodCall>"; 
							
			System.out.println(xml);
		
			/*Client client = Client.create();

		    WebResource webResource = client.resource(TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL);

		    // POST method
		    ClientResponse response = webResource.accept("application/xml").type("application/xml").post(ClientResponse.class, xml);
			
			Unirest.setTimeouts(0, 0);
			HttpResponse<String> response = Unirest.post(TutukaHelper.TUTUKA_TEST_ENDPOINT_QR_RI_LOCAL)
			  .header("Content-Type", "application/xml")
			  .body(xml)
			  .asString();

		    // check response status code
		    if (response.getStatus() != 200) {
		        throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		    }*/

		    // display response
		    String output = "";
		    //output = response.getBody();
		    System.out.println("Output from Server .... ");
		    System.out.println(output + "\n");
			
			if(output!=null)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(output));
		        Document doc = builder.parse(is);
		        NodeList nodes = doc.getElementsByTagName("member");
		       
		        String resultCode = null;
	            String resultText = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("name");
			            Element line = (Element) name.item(0);
			            System.out.println("Name: " + line.getTextContent() + " && " + line.getNodeName());
	
			            NodeList title = element.getElementsByTagName("value");
			            Element line1 = (Element) title.item(0);
			            NodeList valueNode = line1.getChildNodes();
			            Element node1 = (Element)valueNode.item(0);
			            System.out.println("Name: " + node1.getTextContent() + " && " + node1.getNodeName());
			            String nodeTitle = line.getTextContent();
			            String nodeValue = node1.getTextContent();
			            
			            
			            switch(nodeTitle)
			            {
			            	case "resultCode":
			            		resultCode = (nodeValue);
			            		break;
			            	case "resultText":
			            		resultText = (nodeValue);
			            		break;
			            	
			            }
			        }
			        
			        if(resultCode.equals("1"))
			        {
			        	
			        	card.setStatus(CardStatus.DISABLED);
			        	swpService.updateRecord(card);
			        	
			        	mpqrData.setStatus(MPQRDataStatus.DISABLED);
			        	swpService.updateRecord(mpqrData);

			        	System.out.println("card Id..." + card.getId());
			        	System.out.println("card pan...." + card.getPan());
			        	
			        	
				        JSONObject resp = new JSONObject();
				        resp.put("card", card);
				        resp.put("message", "MPQR Account Disabled Successfully");
				        resp.put("status", ERROR.MPQR_ACCOUNT_DISABLED_SUCCESSFULLY);
			        	return resp;
				        //return builder.parse(is);
			        }
			        else
			        {
			        	JSONObject resp = new JSONObject();
			        	resp.put("message", "MPQR Account Setup Was Not Successful");
			        	resp.put("status", ERROR.MPQR_ACCOUNT_NOT_DISABLED_SUCCESSFULLY);
			        	return resp;
			        }
		        }
		        else
		        {
		        	JSONObject resp = new JSONObject();
		        	resp.put("message", "MPQR Account Disabling Was Not Successful");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("xmlMessage", output);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	
	
	
	
	/*REMOTE TUTUKA APIS*/
	public static JSONObject DeductWallet(
			String xml, SwpService swpService, Application application, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println("checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println("checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Deduct")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        	
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Integer requestAmount = null;
	            String narrative = null;
	            String transactionType = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = Integer.valueOf(nodeValue);
			            		break;
			            	case 3:
					            narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionType = nodeValue;
			            		break;
			            	case 5:
			            		transactionData = nodeValue;
			            		break;
			            	case 6:
			            		transactionId = nodeValue;
			            		break;
			            	case 7:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 8:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        if(reference!=null)
			        {
			        	 
			    		String terminalPassword = "";
			    		if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			    			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			    		else if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			    			terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			    		
			        	String checksumCheck = "Deduct"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
			        			formatDataForTutuka(requestAmount+"") + "" + formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionType) + "" + 
			        			formatDataForTutuka(transactionData) +""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
			        	System.out.println(logId + "- checksumCheck " + checksumCheck);
			    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
			    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
			    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
			    		
			    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
			    		{
			    			System.out.println(-1);
			    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
				        	resp.put("message", "Checksum mismatch");
				        	return resp;
			    		}
			    		System.out.println(0);
			    		
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	System.out.println(dataJson.toString());
			        	if(dataJson!=null)
			        	{
			        		System.out.println(dataJson);
			        		String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
				        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
				        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
				        	System.out.println(hql);
				        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
				        	Double minimumCardBalance = card.getCardScheme().getMinimumBalance();
				        	
				        	
				        	
				        	if((requestAmount/100)>application.getAllSettings().getDouble("maximumtransactionamountweb"))
				        	{
				        		System.out.println(2);
				        		resp.put("status", ERROR.DEBIT_TRANSACTION_AMOUNT_EXCEEDS_LIMIT);
					        	resp.put("message", "Transaction amount exceeds limit");
					        	return resp;
				        	}
				        	if(card.getCardBalance()!=null && 
				        			card.getCardBalance()>=
				        			((requestAmount/100)
				        					+minimumCardBalance))
				        	{
				        		System.out.println(3);
				        		String xmlResponse = null; 
				        		
				        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
				        		String bankPaymentReference = null;
				        		Long customerId = card.getCustomerId();
				        		boolean creditAccountTrue = false;
				        		boolean creditCardTrue = false;
				        		Boolean creditWalletTrue = null;
				        		Boolean creditMobileAccountTrue = null;
				        		String orderRef = transactionId;
				        		Channel channel = getChannelByTutukaTransactionType(transactionType);
				        		String rpin = null;
				        		ServiceType serviceType = getServiceTypeByTutukaTransactionType(transactionType);
				        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
				        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
				        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
				        		TransactionStatus status = TransactionStatus.SUCCESS;
				        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
				        		String transactionCode = transactionType;
				        		Account account = card.getAccount();
				        		Device device = null;
				        		Boolean creditPoolAccountTrue = false;
				        		String messageRequest = xml;
				        		String messageResponse = xmlResponse;
				        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
				        		Double transactionCharge = 0.00;
				        		Double transactionPercentage = 0.00;
				        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
				        		Double schemeTransactionPercentage = 0.00;
				        		Double amount = Double.valueOf(requestAmount)/100;
				        		String responseCode = TransactionCode.transactionSuccess;
				        		String otp = null;
				        		String otpref = null;
				        		Long merchantId = null;
				        		String merchantName = null;
				        		String merchantCode = null;
				        		String merchantBank = null;
				        		String merchantAccount = null;
				        		Long transactingBankId = card.getAcquirer().getBank().getId();
				        		Long receipientTransactingBankId = null;
				        		Integer accessCode = null;
				        		Long sourceEntityId = card.getId();
				        		Long receipientEntityId = null;
				        		Channel receipientChannel = channel;
				        		String transactionDetail = "Debit Card: Debit "+amount+" from card #"+card.getTrackingNumber();
				        		Double closingBalance = card.getCardBalance() - amount;
				        		Double totalCreditSum = null;
				        		Double totalDebitSum = null;
				        		Long paidInByBankUserAccountId = null;
				        		String customData = null;
				        		String responseData = null;
				        		Long adjustedTransactionId = null;
				        		Long acquirerId = card.getAccount().getAcquirer().getId();
				        		
				        		
				        		
				        		
				        		
				        		
				        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + " AND tp.deleted_at IS NULL "
				        				+ "AND tp.isLive = " + (card.getIsLive()!=null && card.getIsLive()==1 ? 1 : 0);
								List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
								Boolean debitAccountTrue = false;
								Boolean debitCardTrue = true;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								Account recipientAccount = null;
								if(accounts!=null && accounts.size()>0)
								{
									Map<String, Object> devicebankaccount = accounts.get(0);
									creditAccountId = (Long)devicebankaccount.get("id");
									debitCardId = card.getId();
									debitAccountId = account.getId();
									recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
								}
								//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
				        		
								
				        		Transaction transaction = new Transaction(
				        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
				        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
				        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
				        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
				        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
				        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, adjustedTransactionId,
				        				acquirerId, debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Debit Wallet", 
				        				card.getIsLive()
		        				);
				        		transaction = (Transaction)swpService.createNewRecord(transaction);
				        		Double cardBalance = card.getCardBalance();
				        		card.setCardBalance(cardBalance - amount);
				        		swpService.updateRecord(card);
				        		
				        		Account cardAccount = card.getAccount();
				        		cardAccount.setAccountBalance((cardAccount.getAccountBalance()==null ? 0 : cardAccount.getAccountBalance()) - amount);
				        		swpService.updateRecord(cardAccount);
	
		        				System.out.println(logId + "- " + xmlResponse);
		        				resp.put("status", ERROR.DEBIT_SUCCESSFUL);
	        					resp.put("transaction", transaction);
					        	return resp;
				        	}
				        	System.out.println(4);
				        	resp.put("status", ERROR.DEBIT_FAIL_INSUFFICIENT_FUNDS);
				        	resp.put("message", "Insufficient funds");
				        	return resp;
			        	}
			        	System.out.println(5);
			        	resp.put("status", ERROR.TRANSACTION_DATA_UNPARSEABLE);
			        	resp.put("message", "Invalid transaction data provided");
			        	return resp;
			        }
			        else
			        {
			        	System.out.println(6);
			        	resp.put("status", ERROR.CARD_NOT_FOUND);
			        	resp.put("message", "The debit could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println(7);
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The debit could not be implemented. Please try again");
		        	resp.put("xmlMessage", xml);
		        	return resp;
		        }
		    }
		    System.out.println(8);
		    return null;
		    
		} catch (Exception e) {
			System.out.println(9);
			log.error("error", e);
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	public static JSONObject DeductAdjustment(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("DeductAdjustment")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Integer requestAmount = null;
	            String narrative = null;
	            String transactionData = null;
	            String referenceId = null;
	            String referenceDate = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = Integer.valueOf(nodeValue);
			            		break;
			            	case 3:
					            narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		referenceId = nodeValue;
			            		break;
			            	case 6:
			            		referenceDate = nodeValue;
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "DeductAdjustment"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
			        		formatDataForTutuka(requestAmount+"") + "" + formatDataForTutuka(narrative) + "" + 
			        		formatDataForTutuka(transactionData) + "" + formatDataForTutuka(referenceId) + "" + (referenceDate)+""+
			        		formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	Double originalAmount = dataJson.getDouble(KLVData.ORIGINAL_TRANSACTION_AMOUNT.name());
			        	
			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	hql = "Select tp from Transaction tp where tp.orderRef = '" + referenceId + "' AND tp.status = " + TransactionStatus.SUCCESS.ordinal() + 
			        		" AND tp.amount = '"+ originalAmount +"' AND tp.card.id = " + card.getId() + " AND tp.isLive = " + card.getIsLive();
			        	Collection<Transaction> oldTransactions = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql, 0, 1);
			        	
			        	Transaction oldTransaction = null;
			        	if(oldTransactions==null && oldTransactions.size()>0)
			        	{
			        		oldTransaction = oldTransactions.iterator().next();
			        	}
			        	else
			        	{
			        		
			        	}
			        	
			        	
			        	if(card.getCardBalance()!=null)
			        	{
			        		String xmlResponse = null; 
			        		
			        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
			        		String bankPaymentReference = null;
			        		Long customerId = card.getCustomerId();
			        		boolean creditAccountTrue = false;
			        		boolean creditCardTrue = false;
			        		Boolean creditWalletTrue = null;
			        		Boolean creditMobileAccountTrue = null;
			        		String orderRef = transactionId;
			        		Channel channel = oldTransaction==null ? Channel.NOT_SPECIFIED : oldTransaction.getChannel();
			        		String rpin = null;
			        		ServiceType serviceType = ServiceType.ADJUSTMENT;
			        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
			        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
			        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
			        		TransactionStatus status = TransactionStatus.SUCCESS;
			        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
			        		String transactionCode = transactionData;
			        		Account account = card.getAccount();
			        		Device device = null;
			        		Boolean creditPoolAccountTrue = false;
			        		String messageRequest = xml;
			        		String messageResponse = xmlResponse;
			        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
			        		Double transactionCharge = 0.00;
			        		Double transactionPercentage = 0.00;
			        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
			        		Double schemeTransactionPercentage = 0.00;
			        		Double amount = Double.valueOf(requestAmount)/100;
			        		String responseCode = TransactionCode.transactionSuccess;
			        		String otp = null;
			        		String otpref = null;
			        		Long merchantId = null;
			        		String merchantName = null;
			        		String merchantCode = null;
			        		String merchantBank = null;
			        		String merchantAccount = null;
			        		Long transactingBankId = card.getAcquirer().getBank().getId();
			        		Long receipientTransactingBankId = null;
			        		Integer accessCode = null;
			        		Long sourceEntityId = card.getId();
			        		Long receipientEntityId = null;
			        		Channel receipientChannel = channel;
			        		String transactionDetail = "Debit Card Adjustment: Debit "+amount+" from card #"+card.getTrackingNumber();
			        		Double closingBalance = card.getCardBalance() - amount;
			        		Double totalCreditSum = null;
			        		Double totalDebitSum = null;
			        		Long paidInByBankUserAccountId = null;
			        		String customData = null;
			        		String responseData = null;
			        		Long acquirerId = card.getAccount().getAcquirer().getId();
			        		
			        		
			        		
			        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
			        				" AND tp.deleted_at IS NULL AND tp.isLive = " + card.getIsLive();
							List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
							Boolean debitAccountTrue = false;
							Boolean debitCardTrue = true;
							Long creditAccountId = null;
							Long creditCardId = null;
							Long debitAccountId = null;
							Long debitCardId = null;
							Account recipientAccount = null;
							if(accounts!=null && accounts.size()>0)
							{
								Map<String, Object> devicebankaccount = accounts.get(0);
								debitCardId = card.getId();
								debitAccountId = account.getId();
								creditAccountId = (Long)devicebankaccount.get("id");
								recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
							}
							//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
							
			        		Transaction transaction = new Transaction(
			        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
			        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
			        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
			        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
			        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
			        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, 
			        				responseData, oldTransaction==null ? null : oldTransaction.getId(), acquirerId, debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
	        						"Debit Card Adjustment", card.getIsLive()
			        			);
			        		transaction = (Transaction)swpService.createNewRecord(transaction);

	        				System.out.println(logId + "- " + xmlResponse);
	        				resp.put("status", ERROR.DEBIT_ADJUSTMENT_SUCCESS);
	        				Double minimumCardBalance = card.getCardScheme().getMinimumBalance();
	        				if(card.getCardBalance()<=(amount+minimumCardBalance))
	        				{
	        					resp.put("status", ERROR.DEBIT_ADJUSTMENT_SUCCESS_OVERDEBIT);
	        				}
	        				else
	        				{
	        					Double cardBalance = card.getCardBalance();
				        		card.setCardBalance(Math.abs(cardBalance - amount));
				        		swpService.updateRecord(card);
				        		
				        		Account cardAccount = card.getAccount();
				        		cardAccount.setAccountBalance((cardAccount.getAccountBalance()==null ? 0 : cardAccount.getAccountBalance()) - amount);
				        		swpService.updateRecord(account);
	        				}
        					resp.put("transaction", transaction);
				        	return resp;
			        	}
			        	resp.put("status", ERROR.DEBIT_FAIL_CARD_NOT_CREDITED_PREVIOUSLY);
			        	resp.put("message", "Debit Adjustment was not successful. Card has never been credit before");
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.CARD_NOT_FOUND);
			        	resp.put("message", "The debit could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The debit could not be implemented. Please try again");
		        	resp.put("xmlMessage", xml);
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	


	public static JSONObject DeductReversal(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	System.out.println("xml --- " + xml);
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        System.out.println("methodName... " + methodName);
		        
		        if(!(methodName!=null && methodName.equals("DeductReversal")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Integer requestAmount = null;
	            String narrative = null;
	            String transactionData = null;
	            String referenceId = null;
	            String referenceDate = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = Integer.valueOf(nodeValue);
			            		break;
			            	case 3:
					            narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		referenceId = nodeValue;
			            		break;
			            	case 6:
			            		referenceDate = nodeValue;
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "DeductReversal"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+
			        		formatDataForTutuka(requestAmount+"") + "" + formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionData) + "" + 
			        		formatDataForTutuka(referenceId) + "" + referenceDate +""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	//Double originalAmount = dataJson.getDouble(KLVData.ORIGINAL_TRANSACTION_AMOUNT.name());
			        	
			        	//String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND " +
			        			//"(tp.trackingNumber = '"+cardTrackingNumber+"' OR tp.serialNo = '"+reference+"') " +
			        			"(tp.serialNo = '"+reference+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	System.out.println(hql);
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	hql = "Select tp from Transaction tp where tp.orderRef = '" + referenceId + "' AND tp.status = " + TransactionStatus.SUCCESS.ordinal() + 
			        		//" AND tp.amount = '"+ originalAmount +"'";
			        			" AND tp.card.id = " + card.getId() + " AND tp.isLive = " + card.getIsLive() + 
			        			" ORDER BY tp.id DESC";
			        	System.out.println(hql);
			        	Collection<Transaction> oldTransactions = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql, 0, 1);
			        	Transaction oldTransaction = null;
			        	if(oldTransactions!=null && oldTransactions.size()>0)
			        	{
			        		System.out.println("<<<");
			        		oldTransaction = oldTransactions.iterator().next();
			        	}
			        	else
			        	{
			        		System.out.println(">>>>");
			        		resp.put("status", ERROR.TRANSACTION_NOT_FOUND+"");
				        	resp.put("message", "Previous transaction could not be found");
				        	return resp;
			        	}
			        	
			        	
			        	/*if(oldTransaction!=null && !oldTransaction.getCard().getId().equals(card.getId()))
			        	{
			        		resp.put("status", 0);
				        	resp.put("message", "Previous transaction could not be found");
				        	return resp;
			        	}*/
			        	
			        	
		        		String xmlResponse = null; 
		        		
		        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
		        		String bankPaymentReference = null;
		        		Long customerId = card.getCustomerId();
		        		boolean creditAccountTrue = false;
		        		boolean creditCardTrue = false;
		        		Boolean creditWalletTrue = null;
		        		Boolean creditMobileAccountTrue = null;
		        		String orderRef = transactionId;
		        		Channel channel = oldTransaction==null ? Channel.NOT_SPECIFIED : oldTransaction.getChannel();
		        		String rpin = null;
		        		ServiceType serviceType = ServiceType.REVERSE_DEBIT_ON_CARD;
		        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
		        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
		        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
		        		TransactionStatus status = TransactionStatus.SUCCESS;
		        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
		        		String transactionCode = transactionData;
		        		Account account = card.getAccount();
		        		Device device = null;
		        		Boolean creditPoolAccountTrue = false;
		        		String messageRequest = xml;
		        		String messageResponse = xmlResponse;
		        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
		        		Double transactionCharge = 0.00;
		        		Double transactionPercentage = 0.00;
		        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
		        		Double schemeTransactionPercentage = 0.00;
		        		Double amount = Double.valueOf(requestAmount)/100;
		        		String responseCode = TransactionCode.transactionSuccess;
		        		String otp = null;
		        		String otpref = null;
		        		Long merchantId = null;
		        		String merchantName = null;
		        		String merchantCode = null;
		        		String merchantBank = null;
		        		String merchantAccount = null;
		        		Long transactingBankId = card.getAcquirer().getBank().getId();
		        		Long receipientTransactingBankId = null;
		        		Integer accessCode = null;
		        		Long sourceEntityId = card.getId();
		        		Long receipientEntityId = null;
		        		Channel receipientChannel = channel;
		        		String transactionDetail = "Debit Card Reversal: Credit "+amount+" to card #"+card.getTrackingNumber();
		        		Double closingBalance = card.getCardBalance() - amount;
		        		Double totalCreditSum = null;
		        		Double totalDebitSum = null;
		        		Long paidInByBankUserAccountId = null;
		        		String customData = null;
		        		String responseData = null;
		        		Long acquirerId = card.getAccount().getAcquirer().getId();
		        		
		        		
		        		
		        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
		        				" AND tp.deleted_at IS NULL AND tp.isLve = " + card.getIsLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
						Boolean debitAccountTrue = false;
						Boolean debitCardTrue = false;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						if(accounts!=null && accounts.size()>0)
						{
							Map<String, Object> devicebankaccount = accounts.get(0);
							creditCardId = card.getId();
							creditAccountId = account.getId();
							debitAccountId = (Long)devicebankaccount.get("id");
							recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
						}
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						
		        		Transaction transaction = new Transaction(
		        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
		        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card,  
		        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
		        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
		        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
		        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, 
		        				responseData, oldTransaction==null ? null : oldTransaction.getId(), acquirerId, 
        						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Debit Card Reversal", card.getIsLive()
		        			);
		        		transaction = (Transaction)swpService.createNewRecord(transaction);
		        		Double cardBalance = card.getCardBalance();
		        		card.setCardBalance((cardBalance==null ? 0 : cardBalance) + amount);
		        		swpService.updateRecord(card);
		        		
		        		Account cardAccount = card.getAccount();
		        		cardAccount.setAccountBalance((cardAccount.getAccountBalance()==null ? 0 : cardAccount.getAccountBalance()) + amount);
		        		swpService.updateRecord(cardAccount);

        				System.out.println(logId + "- " + xmlResponse);
        				resp.put("status", ERROR.DEBIT_CARD_REVERSAL_SUCCESS);
    					resp.put("transaction", transaction);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.CARD_NOT_FOUND);
			        	resp.put("message", "The debit reversal could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The debit reversal could not be implemented. Invalid XML Message Received. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	
	
	public static JSONObject LoadAdjustment(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("LoadAdjustment")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Integer requestAmount = null;
	            String narrative = null;
	            String transactionData = null;
	            String referenceId = null;
	            String referenceDate = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = Integer.valueOf(nodeValue);
			            		break;
			            	case 3:
					            narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		referenceId = nodeValue;
			            		break;
			            	case 6:
			            		referenceDate = nodeValue;
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "LoadAdjustment"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmount+"") + "" + 
			        		formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionData) + "" + 
			        		formatDataForTutuka(referenceId) + "" + referenceDate +""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	Double originalAmount = dataJson.getDouble(KLVData.ORIGINAL_TRANSACTION_AMOUNT.name());
			        	

			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	
			        	
		        		String xmlResponse = null; 
		        		
		        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
		        		String bankPaymentReference = null;
		        		Long customerId = card.getCustomerId();
		        		boolean creditAccountTrue = false;
		        		boolean creditCardTrue = false;
		        		Boolean creditWalletTrue = null;
		        		Boolean creditMobileAccountTrue = null;
		        		String orderRef = transactionId;
		        		Channel channel = Channel.NOT_SPECIFIED;
		        		String rpin = null;
		        		ServiceType serviceType = ServiceType.ADJUSTMENT;
		        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
		        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
		        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
		        		TransactionStatus status = TransactionStatus.SUCCESS;
		        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
		        		String transactionCode = transactionData;
		        		Account account = card.getAccount();
		        		Device device = null;
		        		Boolean creditPoolAccountTrue = false;
		        		String messageRequest = xml;
		        		String messageResponse = xmlResponse;
		        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
		        		Double transactionCharge = 0.00;
		        		Double transactionPercentage = 0.00;
		        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
		        		Double schemeTransactionPercentage = 0.00;
		        		Double amount = Double.valueOf(requestAmount)/100;
		        		String responseCode = TransactionCode.transactionSuccess;
		        		String otp = null;
		        		String otpref = null;
		        		Long merchantId = null;
		        		String merchantName = null;
		        		String merchantCode = null;
		        		String merchantBank = null;
		        		String merchantAccount = null;
		        		Long transactingBankId = card.getAcquirer().getBank().getId();
		        		Long receipientTransactingBankId = null;
		        		Integer accessCode = null;
		        		Long sourceEntityId = card.getId();
		        		Long receipientEntityId = null;
		        		Channel receipientChannel = channel;
		        		String transactionDetail = "Credit Card: Credit "+amount+" to card #"+card.getTrackingNumber();
		        		Double closingBalance = card.getCardBalance() - amount;
		        		Double totalCreditSum = null;
		        		Double totalDebitSum = null;
		        		Long paidInByBankUserAccountId = null;
		        		String customData = null;
		        		String responseData = null;
		        		Long acquirerId = card.getAccount().getAcquirer().getId();
		        		
		        		
		        		
		        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + " AND tp.deleted_at IS NULL "
		        				+ "AND tp.isLive = " + card.getIsLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
						Boolean debitAccountTrue = false;
						Boolean debitCardTrue = false;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						if(accounts!=null && accounts.size()>0)
						{
							Map<String, Object> devicebankaccount = accounts.get(0);
							creditCardId = card.getId();
							creditAccountId = account.getId();
							debitAccountId = (Long)devicebankaccount.get("id");
							recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
						}
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
		        		Transaction transaction = new Transaction(
		        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
		        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
		        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
		        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
		        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
		        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, null, acquirerId, 
		        				debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Credit Money to Card", card.getIsLive()
		        			);
		        		transaction = (Transaction)swpService.createNewRecord(transaction);
		        		Double cardBalance = card.getCardBalance();
		        		card.setCardBalance((cardBalance==null ? 0 : cardBalance) + amount);
		        		swpService.updateRecord(card);
		        		
		        		Account cardAccount = card.getAccount();
		        		cardAccount.setAccountBalance((cardAccount.getAccountBalance()==null ? 0 : cardAccount.getAccountBalance()) + amount);
		        		swpService.updateRecord(cardAccount);

        				System.out.println(logId + "- " + xmlResponse);
        				resp.put("status", ERROR.LOAD_ADJUSTMENT_SUCCESS);
    					resp.put("transaction", transaction);
			        	return resp;
			        }
			        else
			        {
			        	System.out.println(logId + "- Invalid XML");
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The load adjustment could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println(logId + "- Invalid XML1");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The load adjustment could not be implemented. Invalid request received. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
			log.error("error", e);
		     e.printStackTrace();
		     return null;
		}

	}

	
	
	
	public static JSONObject LoadReversal(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("LoadReversal")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Integer requestAmount = null;
	            String narrative = null;
	            String transactionData = null;
	            String referenceId = null;
	            String referenceDate = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = Integer.valueOf(nodeValue);
			            		break;
			            	case 3:
					            narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		referenceId = nodeValue;
			            		break;
			            	case 6:
			            		referenceDate = nodeValue;
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "LoadReversal"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmount+"") + "" + 
			        		formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionData) + "" + 
			        		formatDataForTutuka(referenceId) + "" + referenceDate +""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	//Double originalAmount = dataJson.getDouble(KLVData.ORIGINAL_TRANSACTION_AMOUNT.name());
			        	

			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	
			        	hql = "Select tp from Transaction tp where tp.orderRef = '" + referenceId + "' AND tp.status = " + TransactionStatus.SUCCESS.ordinal() + 
			        		//" AND tp.amount = '"+ originalAmount +"'" +
			        		" AND tp.card.id = " + card.getId();
			        	Collection<Transaction> oldTransactions = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql, 0, 1);
			        	if(oldTransactions==null && oldTransactions.size()>0)
			        	{
			        	}
			        	else
			        	{
			        		resp.put("status", ERROR.TRANSACTION_NOT_FOUND);
				        	resp.put("message", "Previous transaction could not be found");
				        	return resp;
			        	}
			        	
			        	Transaction oldTransaction = oldTransactions.iterator().next();
		        		String xmlResponse = null; 
		        		
		        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
		        		String bankPaymentReference = null;
		        		Long customerId = card.getCustomerId();
		        		boolean creditAccountTrue = false;
		        		boolean creditCardTrue = false;
		        		Boolean creditWalletTrue = null;
		        		Boolean creditMobileAccountTrue = null;
		        		String orderRef = transactionId;
		        		Channel channel = Channel.NOT_SPECIFIED;
		        		String rpin = null;
		        		ServiceType serviceType = ServiceType.REVERSE_CREDIT_ON_CARD;
		        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
		        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
		        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
		        		TransactionStatus status = TransactionStatus.SUCCESS;
		        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
		        		String transactionCode = null;
		        		Account account = card.getAccount();
		        		Device device = null;
		        		Boolean creditPoolAccountTrue = false;
		        		String messageRequest = xml;
		        		String messageResponse = xmlResponse;
		        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
		        		Double transactionCharge = 0.00;
		        		Double transactionPercentage = 0.00;
		        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
		        		Double schemeTransactionPercentage = 0.00;
		        		Double amount = Double.valueOf(requestAmount)/100;
		        		String responseCode = TransactionCode.transactionSuccess;
		        		String otp = null;
		        		String otpref = null;
		        		Long merchantId = null;
		        		String merchantName = null;
		        		String merchantCode = null;
		        		String merchantBank = null;
		        		String merchantAccount = null;
		        		Long transactingBankId = card.getAcquirer().getBank().getId();
		        		Long receipientTransactingBankId = null;
		        		Integer accessCode = null;
		        		Long sourceEntityId = card.getId();
		        		Long receipientEntityId = null;
		        		Channel receipientChannel = channel;
		        		String transactionDetail = "Reverse Credit Card: Debit "+amount+" from card #"+card.getTrackingNumber();
		        		Double closingBalance = card.getCardBalance() - amount;
		        		Double totalCreditSum = null;
		        		Double totalDebitSum = null;
		        		Long paidInByBankUserAccountId = null;
		        		String customData = null;
		        		String responseData = null;
		        		Long acquirerId = card.getAccount().getAcquirer().getId();
		        		
		        		
		        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
		        				" AND tp.deleted_at IS NULL AND tp.isLive = " + card.getIsLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
		        		Boolean debitAccountTrue = true;
						Boolean debitCardTrue = true;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						if(accounts!=null && accounts.size()>0)
						{
							Map<String, Object> devicebankaccount = accounts.get(0);
							debitCardId = card.getId();
							debitAccountId = account.getId();
							creditAccountId = (Long)devicebankaccount.get("id");
							recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
						}
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						
		        		Transaction transaction = new Transaction(
		        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
		        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
		        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
		        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
		        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
		        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, oldTransaction.getId(), acquirerId, 
		        				debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Reverse Card Funding", card.getIsLive()
		        			);
		        		transaction = (Transaction)swpService.createNewRecord(transaction);
		        		Double cardBalance = card.getCardBalance();
		        		card.setCardBalance((cardBalance==null ? 0 : cardBalance) - amount);
		        		swpService.updateRecord(card);
		        		
		        		Account cardAccount = card.getAccount();
		        		cardAccount.setAccountBalance((cardAccount.getAccountBalance()==null ? 0 : cardAccount.getAccountBalance()) - amount);
		        		swpService.updateRecord(cardAccount);

        				System.out.println(logId + "- " + xmlResponse);
        				resp.put("status", ERROR.LOAD_REVERSAL_SUCCESS);
    					resp.put("transaction", transaction);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The credit reversal could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The credit reversal could not be implemented. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	

	public static JSONObject AdministrativeMessage3DSecureOTP(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        System.out.println("methodName ... " + methodName);
		        if(!(methodName!=null && methodName.equals("AdministrativeMessage")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String messageName = null;
	            String messageData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		messageName = nodeValue;
			            		break;
			            	case 3:
					            messageData = nodeValue;
			            		break;
			            	case 4:
			            		transactionId = nodeValue;
			            		break;
			            	case 5:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 6:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "AdministrativeMessage"+formatDataForTutuka(terminalId)+""+
			        		formatDataForTutuka(reference)+""+formatDataForTutuka(messageName)+""+
			        		formatDataForTutuka(messageData)+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(messageData);
			        	String otp = dataJson.getString(KLVData.SECURE3D_OTP.name());
			        	

			        	//String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.serialNo = '"+reference+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	System.out.println(hql);
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				System.out.println(logId + "- " + xmlResponse);
        				
        				if(otp!=null && otp.length()>0)
        				{
	        				String smsMessage = "Provide the OTP - " + otp + " to complete your transaction #" + transactionId.toUpperCase();
	        				String receipient = card.getAccount().getCustomer().getContactMobile();
	        				UtilityHelper.sendSMSMessage(receipient, smsMessage);
        				}
        				resp.put("status", ERROR.GENERAL_OK);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "Invalid reference id received. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The Administrative message could not be received. Invalid message sent. sPlease try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	public static JSONObject Stop(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Stop")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String voucherNumber = null;
	            String stopReason = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		voucherNumber = nodeValue;
			            		break;
			            	case 3:
			            		stopReason = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		transactionId = nodeValue;
			            		break;
			            	case 6:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 7:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "Stop"+terminalId+""+reference+""+voucherNumber+""+stopReason+""+transactionData+""+transactionId+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", 0);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				System.out.println(logId + "- " + xmlResponse);
        				
        				
        				String smsMessage = "Your wallet - " + card.getTrackingNumber() + " has been stopped.";
        				String receipient = card.getAccount().getCustomer().getContactMobile();
        				UtilityHelper.sendSMSMessage(receipient, smsMessage);

        				resp.put("status", 1);
			        	return resp;
			        }
			        else
			        {
			        	resp.put("status", 0);
			        	resp.put("message", "The card could not be stopped successfully. No reference provided. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", 0);
		        	resp.put("message", "The card could not be stopped. Invalid message sent. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	/*
	public static JSONObject ValidatePIN(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("ValidatePIN")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String pinBlock = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		pinBlock = nodeValue;
			            		break;
			            	case 3:
			            		transactionId = nodeValue;
			            		break;
			            	case 4:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 5:
			            		checksum = nodeValue;
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "ValidatePIN"+terminalId+""+reference+""+pinBlock+""+transactionId+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		    		System.out.println(logId + "- " + "checksum -- " + checksum);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	

			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				System.out.println(logId + "- " + xmlResponse);
        				
        				
        				if(card.getPin()!=null && card.getPin().equals(pinBlock))
        				{
        					resp.put("status", 1);
    			        	return resp;
        				}
        				resp.put("status", 0);
        				resp.put("message", "Pin mismatch");
			        	return resp;
        				
			        }
			        else
			        {
			        	resp.put("status", 0);
			        	resp.put("message", "The pin could not be validated. No reference provided. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", 0);
		        	resp.put("message", "The pin could not be validated. Invalid message sent. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}*/
	
	
	
	
	
	public static JSONObject Balance(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Balance")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String narrative = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		narrative = nodeValue;
			            		break;
			            	case 3:
			            		narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionId = nodeValue;
			            		break;
			            	case 5:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 6:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "Balance"+terminalId+""+reference+""+narrative+""+transactionData+""+transactionId+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>balanceAmount</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>"+(Math.round(card.getCardBalance()*100))+"</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				System.out.println(logId + "- " + xmlResponse);
        				
        				
    					resp.put("status", 1);
			        	return resp;
        				
			        }
			        else
			        {
			        	resp.put("status", 0);
			        	resp.put("message", "The balance could not be obtained. No reference provided. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", 0);
		        	resp.put("message", "The balance could not be obtained. Invalid message sent. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	

	
	
	
	
	public static JSONObject LoadAuth(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("LoadAuth")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String requestAmount = null;
	            String narrative = null;
	            Integer transactionType = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = (nodeValue);
			            		break;
			            	case 3:
			            		narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionType = Integer.valueOf(nodeValue);
			            		break;
			            	case 5:
			            		transactionData = (nodeValue);
			            		break;
			            	case 6:
			            		transactionId = nodeValue;
			            		break;
			            	case 7:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 8:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        System.out.println("requestAmount");
			        System.out.println(requestAmount);
			        String checksumCheck = "LoadAuth"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmount+"")+""+
			        		formatDataForTutuka(narrative)+""+formatDataForTutuka(transactionType+"")+""+formatDataForTutuka(transactionData)+""+
			        		formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	System.out.println(hql);
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				//System.out.println(logId + "- " + xmlResponse);
        				
        				if(transactionType==20)
        				{
        					//Handle Refunds
        					Double requestAmountFormatted = Double.valueOf(requestAmount);
        					//Refund refund = new Refund(transactionData, xml, xmlResponse, card, null, transactionId, requestAmountFormatted/100, RefundType.REFUND);
        					//refund = (Refund)swpService.createNewRecord(refund);
        					
        					
        				}
        				else if(transactionType==28)
        				{
        					//Handle MoneySend/Payment
        					String transactionRef = RandomStringUtils.randomAlphanumeric(16);
			        		String bankPaymentReference = null;
			        		Long customerId = card.getCustomerId();
			        		boolean creditAccountTrue = true;
			        		boolean creditCardTrue = true;
			        		Boolean creditWalletTrue = null;
			        		Boolean creditMobileAccountTrue = null;
			        		String orderRef = transactionId;
			        		Channel channel = null;
			        		String rpin = null;
			        		ServiceType serviceType = ServiceType.CREDIT_CARD;
			        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
			        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
			        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
			        		TransactionStatus status = TransactionStatus.SUCCESS;
			        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
			        		String transactionCode = transactionData;
			        		Account account = card.getAccount();
			        		Device device = null;
			        		Boolean creditPoolAccountTrue = false;
			        		String messageRequest = xml;
			        		String messageResponse = xmlResponse;
			        		Double fixedCharge = 0.00;
			        		Double transactionCharge = 0.00;
			        		Double transactionPercentage = 0.00;
			        		Double schemeTransactionCharge = 0.00;
			        		Double schemeTransactionPercentage = 0.00;
			        		Double amount = Double.valueOf(requestAmount)/100;
			        		String responseCode = TransactionCode.transactionSuccess;
			        		String otp = null;
			        		String otpref = null;
			        		Long merchantId = null;
			        		String merchantName = null;
			        		String merchantCode = null;
			        		String merchantBank = null;
			        		String merchantAccount = null;
			        		Long transactingBankId = card.getAcquirer().getBank().getId();
			        		Long receipientTransactingBankId = null;
			        		Integer accessCode = null;
			        		Long sourceEntityId = card.getId();
			        		Long receipientEntityId = null;
			        		Channel receipientChannel = channel;
			        		String transactionDetail = "Credit Card (Refund): Credit "+amount+" to card #"+card.getTrackingNumber();
			        		Double closingBalance = card.getCardBalance() + amount;
			        		Double totalCreditSum = null;
			        		Double totalDebitSum = null;
			        		Long paidInByBankUserAccountId = null;
			        		String customData = null;
			        		String responseData = null;
			        		Long adjustedTransactionId = null;
			        		Long acquirerId = card.getAccount().getAcquirer().getId();
			        		
			        		
			        		
			        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
			        				" AND tp.deleted_at IS NULL" + " AND tp.isLive = " + card.getIsLive();
							List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			        		Boolean debitAccountTrue = false;
							Boolean debitCardTrue = false;
							Long creditAccountId = null;
							Long creditCardId = null;
							Long debitAccountId = null;
							Long debitCardId = null;
							Account recipientAccount = null;
							if(accounts!=null && accounts.size()>0)
							{
								Map<String, Object> devicebankaccount = accounts.get(0);
								creditCardId = card.getId();
								creditAccountId = account.getId();
								debitAccountId = (Long)devicebankaccount.get("id");
								recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
							}
							//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
        					Transaction transaction = new Transaction(transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
			        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
			        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
			        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
			        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
			        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, adjustedTransactionId, acquirerId,
			        				debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Credit Card (Refund)", card.getIsLive()
			        				);
        					transaction = (Transaction)swpService.createNewRecord(transaction);
        					
        					//Refund refund = new Refund(transactionData, xml, xmlResponse, card, transaction, transactionId, amount, RefundType.REVERSAL);
        					//refund = (Refund)swpService.createNewRecord(refund);
        					
        					//Notify the customer that the merchant is processing the refund
        					//card.setCardBalance(card.getCardBalance() + amount);
        					//swpService.updateRecord(card);
        				}
        					
    					resp.put("status", ERROR.LOAD_AUTH_SUCCESS);
			        	return resp;
        				
			        }
			        else
			        {
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The load auth on the card could not be refunded/paid. No reference provided. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The load auth on the card could not be refunded/paid. Invalid message sent. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	
	
	public static JSONObject LoadAuthReversal(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);
		/*String checksum = "RetireCard"+terminalId+""+reference+""+card.getTrackingNumber()
				+""+transactionId+""+transactionDateFormatted;
		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		checksum = TutukaHelper.generateChecksum(checksum, terminalPassword);
		System.out.println(logId + "- " + "checksum -- " + checksum);*/
		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("LoadAuthReversal")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            Double requestAmount = null;
	            String requestAmountStr = null;
	            String narrative = null;
	            String transactionData = null;
	            String referenceID = null;
	            Date referenceDate = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            String referenceDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmountStr = (nodeValue);
			            		requestAmount = Double.valueOf(nodeValue);
			            		break;
			            	case 3:
			            		narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		referenceID = nodeValue;
			            		break;
			            	case 6:
			            		referenceDateFormatted = nodeValue;
			            		referenceDate = simpleDateFormatISO8601.parse(referenceDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String referenceDateFm = sdf.format(referenceDate);
			            		referenceDate = sdf.parse(referenceDateFm);
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        String checksumCheck = "LoadAuthReversal"+formatDataForTutuka(terminalId)+""+
			        		formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmountStr)+""+formatDataForTutuka(narrative)+""+
			        		formatDataForTutuka(transactionData)+""+formatDataForTutuka(referenceID)+""+
			        		referenceDateFormatted+""+formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksum);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword).toLowerCase();
		    		System.out.println(logId + "- " + "checksum -- " + checksumCheck);
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	//String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.serialNo = '"+reference+"') " +
			        			"AND tp.status = " + CardStatus.ACTIVE.ordinal() + "";
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	String xmlResponse = "";
			        	xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			        	xmlResponse = xmlResponse + "<methodResponse>";
			        	xmlResponse = xmlResponse + "<params>";
			        	xmlResponse = xmlResponse + "<param>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<struct>";
			        	xmlResponse = xmlResponse + "<member>";
			        	xmlResponse = xmlResponse + "<name>resultCode</name>";
			        	xmlResponse = xmlResponse + "<value>";
			        	xmlResponse = xmlResponse + "<int>1</int>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</member>";
			        	xmlResponse = xmlResponse + "</struct>";
			        	xmlResponse = xmlResponse + "</value>";
			        	xmlResponse = xmlResponse + "</param>";
			        	xmlResponse = xmlResponse + "</params>";
			        	xmlResponse = xmlResponse + "</methodResponse>";
			        	
        				System.out.println(logId + "- " + xmlResponse);
        				
        				//hql = "Select tp from Refund tp where tp.transactionId = '"+referenceID+"' AND tp.deleted_at IS NULL";
        				//Refund oldRefund = (Refund)swpService.getUniqueRecordByHQL(hql);
        				
        				
        				/*if(oldRefund!=null && oldRefund.getStatus().equals(RefundStatus.REFUNDED))
        				{
        					//Handle Refund Reversal
        					if(oldRefund.getCreditCardTransaction()==null)
        					{
	        					//Refund refund = new Refund(transactionData, xml, xmlResponse, card, null, transactionId, requestAmount/100, RefundType.REVERSAL);
	        					//refund = (Refund)swpService.createNewRecord(refund);
        					}
        					else
        					{*/
        						String transactionRef = RandomStringUtils.randomAlphanumeric(16);
    			        		String bankPaymentReference = null;
    			        		Long customerId = card.getCustomerId();
    			        		boolean creditAccountTrue = false;
    			        		boolean creditCardTrue = false;
    			        		Boolean creditWalletTrue = null;
    			        		Boolean creditMobileAccountTrue = null;
    			        		String orderRef = transactionId;
    			        		Channel channel = Channel.NOT_SPECIFIED;
    			        		String rpin = null;
    			        		ServiceType serviceType = ServiceType.REVERSE_REFUND;
    			        		String payerName = card.getAccount().getCustomer().getFirstName() + " " + card.getAccount().getCustomer().getLastName();
    			        		String payerEmail = card.getAccount().getCustomer().getContactEmail();
    			        		String payerMobile = card.getAccount().getCustomer(). getContactMobile();
    			        		TransactionStatus status = TransactionStatus.SUCCESS;
    			        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
    			        		String transactionCode = transactionData;
    			        		Account account = card.getAccount();
    			        		Device device = null;
    			        		Boolean creditPoolAccountTrue = false;
    			        		String messageRequest = xml;
    			        		String messageResponse = xmlResponse;
    			        		Double fixedCharge = 0.00;
    			        		Double transactionCharge = 0.00;
    			        		Double transactionPercentage = 0.00;
    			        		Double schemeTransactionCharge = 0.00;
    			        		Double schemeTransactionPercentage = 0.00;
    			        		Double amount = Double.valueOf(requestAmount)/100;
    			        		String responseCode = TransactionCode.transactionSuccess;
    			        		String otp = null;
    			        		String otpref = null;
    			        		Long merchantId = null;
    			        		String merchantName = null;
    			        		String merchantCode = null;
    			        		String merchantBank = null;
    			        		String merchantAccount = null;
    			        		Long transactingBankId = card.getAcquirer().getBank().getId();
    			        		Long receipientTransactingBankId = null;
    			        		Integer accessCode = null;
    			        		Long sourceEntityId = card.getId();
    			        		Long receipientEntityId = null;
    			        		Channel receipientChannel = channel;
    			        		String transactionDetail = "Debit Card (Refund Reversal): Debit "+amount +" from card #"+card.getTrackingNumber();
    			        		Double closingBalance = card.getCardBalance() + amount;
    			        		Double totalCreditSum = null;
    			        		Double totalDebitSum = null;
    			        		Long paidInByBankUserAccountId = null;
    			        		String customData = null;
    			        		String responseData = null;
    			        		Long adjustedTransactionId = null;
				        		Long acquirerId = card.getAccount().getAcquirer().getId();
				        		
				        		
				        		
				        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
				        				" AND tp.deleted_at IS NULL AND tp.isLive = " + card.getIsLive();
								List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
				        		Boolean debitAccountTrue = true;
								Boolean debitCardTrue = true;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								Account recipientAccount = null;
								if(accounts!=null && accounts.size()>0)
								{
									Map<String, Object> devicebankaccount = accounts.get(0);
									debitCardId = card.getId();
									debitAccountId = account.getId();
									creditAccountId = (Long)devicebankaccount.get("id");
									recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
								}
								//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
								
            					Transaction transaction = new Transaction(transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
    			        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
    			        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
    			        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
    			        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
    			        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, adjustedTransactionId, acquirerId, 
    			        				debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Debit Card (Refund Reversal)", card.getIsLive()
    			        				);
            					transaction = (Transaction)swpService.createNewRecord(transaction);
            					
            					//Refund refund = new Refund(transactionData, xml, xmlResponse, card, transaction, transactionId, requestAmount/100, RefundType.REVERSAL);
            					//refund = (Refund)swpService.createNewRecord(refund);
            					
            					//Send notification that the mechant has ca 
            					//card.setCardBalance(card.getCardBalance() - amount);
            					//swpService.updateRecord(card);
        					/*}
        					
        					
        				}*/
        					
    					resp.put("status", ERROR.LOAD_AUTH_REVERSAL_SUCCESS);
			        	return resp;
        				
			        }
			        else
			        {
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The load auth refund on the card could not be reversed. No reference provided. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The load auth refund on the card could not be reversed. Invalid message sent. Please try again");
		        	return resp;
		        }
		    }
		    return null;
		    
		} catch (Exception e) {
		     e.printStackTrace();
		     return null;
		}

	}
	
	
	
	
	public static JSONObject StopRemoteTutukaCard(
			String xml, SwpService swpService, String logId) {
		// TODO Auto-generated method stub

		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);

		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Stop")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String voucherNumber = null;
	            Integer stopReason = null;
	            String stopReasonStr = null;
	            String transactionData = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		voucherNumber = (nodeValue);
			            		break;
			            	case 3:
					            stopReason = Integer.valueOf(nodeValue) - 1;
					            stopReasonStr = (nodeValue);
			            		break;
			            	case 4:
			            		transactionData = nodeValue;
			            		break;
			            	case 5:
			            		transactionId = nodeValue;
			            		break;
			            	case 6:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 7:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.TERMINAL_ID_PHYSICAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_PHYSICAL;
			        else if(terminalId.equals(TutukaHelper.TERMINAL_ID_VIRTUAL))
			        	terminalPassword = TutukaHelper.TERMINAL_PASSWORD_VIRTUAL;
			        
			        String checksumCheck = "Stop"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(voucherNumber) + "" + 
			        		formatDataForTutuka(stopReasonStr) + "" + formatDataForTutuka(transactionData) + "" + 
			        		formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksumCheck);
		    		checksumCheck = TutukaHelper.generateChecksum(checksumCheck, terminalPassword);
		    		System.out.println(logId + "- " + "checksum -- " + checksum.toLowerCase());
		    		System.out.println(logId + "- " + "checksumCheck -- " + checksumCheck.toLowerCase());
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			System.out.println("1");
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	System.out.println("2");
			        	JSONObject dataJson = formatTransactionData(transactionData);
			        	

			        	String cardTrackingNumber = dataJson.getString(KLVData.TRACKING_NUMBER.name());
			        	String hql = "Select tp from ECard tp where tp.deleted_at IS NULL AND (tp.trackingNumber = '"+cardTrackingNumber+"')" +
			        			"";
			        	System.out.println(hql);
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);

			        	System.out.println("stopReason ... " + stopReason);
			        	card.setStopReason(StopCardReason.values()[stopReason]);
			        	card.setStopReasonDescription("Stopped By Tutuka Request - " + transactionData);
			        	card.setStopFlag(true);
			        	swpService.updateRecord(card);
			        	
			        	resp.put("status", ERROR.CARD_STOPPED_SUCCESSFULLY);
			        	
			        	return resp;
			        	
			        }
			        else
			        {
			        	System.out.println("Invalid xML Message1");
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The card stop could not be implemented. Card reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println("Invalid Message 2");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card stop could not be implemented. Please try again");
		        	return resp;
		        }
		    }
		    System.out.println("Invalid xml 3");
		    return null;
		    
		} catch (Exception e) {
			System.out.println("Invalid exception");
		     e.printStackTrace();
		     return null;
		}

	}
	

	
	
	public static JSONObject loadQRWallet(
			String xml, SwpService swpService, String logId) 
	{
		// TODO Auto-generated method stub
		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);

		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("Load")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String requestAmount = null;
	            String narrative = null;
	            String transactionType = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = (nodeValue);
			            		break;
			            	case 3:
			            		narrative = (nodeValue);
			            		break;
			            	case 4:
			            		transactionType = nodeValue;
			            		break;
			            	case 5:
			            		transactionId = nodeValue;
			            		break;
			            	case 6:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 7:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID))
			        	terminalPassword = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD;
			        else if(terminalId.equals(TutukaHelper.ORIGINATING_INSTITUTION_TERMINAL_ID))
			        	terminalPassword = TutukaHelper.ORIGINATING_INSTITUTION_TERMINAL_PASSWORD;
			        
			        String checksumCheck = "Load"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmount) + "" + 
			        		formatDataForTutuka(narrative) + "" + formatDataForTutuka(transactionType) + "" + 
			        		formatDataForTutuka(transactionId)+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksumCheck);
		    		checksumCheck = TutukaHelper.generateChecksumSha1(checksumCheck, terminalPassword);
		    		System.out.println(logId + "- " + "checksum -- " + checksum.toLowerCase());
		    		System.out.println(logId + "- " + "checksumCheck -- " + checksumCheck.toLowerCase());
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			System.out.println("1");
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	System.out.println("2");
			        	String hql = "Select tp from MPQRData tp where tp.deleted_at IS NULL AND (tp.qrCodeId = '"+reference+"')" +
			        			"";
			        	System.out.println(hql);
			        	MPQRData mpqrData = (MPQRData)swpService.getUniqueRecordByHQL(hql);
			        	hql = "Select tp from ECard tp where tp.id = " + mpqrData.getCardId();
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			        	hql = "Select tp from Device tp where tp.id = " + mpqrData.getDeviceId();
			        	Device device = (Device)swpService.getUniqueRecordByHQL(hql);

			        	
			        	String transactionRef = RandomStringUtils.randomAlphanumeric(16);
		        		String bankPaymentReference = null;
		        		Long customerId = card.getCustomerId();
		        		boolean creditAccountTrue = false;
		        		boolean creditCardTrue = false;
		        		Boolean creditWalletTrue = null;
		        		Boolean creditMobileAccountTrue = null;
		        		String orderRef = transactionId;
		        		Channel channel = Channel.NOT_SPECIFIED;
		        		String rpin = null;
		        		ServiceType serviceType = ServiceType.MPQR_WALLET_LOAD;
		        		String payerName = device.getMerchant().getMerchantName();
		        		String payerEmail = device.getMerchant().getContactEmail();
		        		String payerMobile = device.getMerchant().getContactMobile();
		        		TransactionStatus status = TransactionStatus.SUCCESS;
		        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
		        		String transactionCode = RandomStringUtils.randomAlphanumeric(12);
		        		Account account = card.getAccount();
		        		Boolean creditPoolAccountTrue = false;
		        		String messageRequest = xml;
		        		String messageResponse = null;
		        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
		        		Double transactionCharge = 0.00;
		        		Double transactionPercentage = 0.00;
		        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
		        		Double schemeTransactionPercentage = 0.00;
		        		Double amount = Double.valueOf(requestAmount)/100;
		        		String responseCode = TransactionCode.transactionSuccess;
		        		String otp = null;
		        		String otpref = null;
		        		Long merchantId = null;
		        		String merchantName = null;
		        		String merchantCode = null;
		        		String merchantBank = null;
		        		String merchantAccount = null;
		        		Long transactingBankId = card.getAcquirer().getBank().getId();
		        		Long receipientTransactingBankId = null;
		        		Integer accessCode = null;
		        		Long sourceEntityId = card.getId();
		        		Long receipientEntityId = null;
		        		Channel receipientChannel = channel;
		        		String transactionDetail = "MPQR Wallet Load: Credit "+amount+" to card #"+card.getPan();
		        		Double closingBalance = card.getCardBalance() - amount;
		        		Double totalCreditSum = null;
		        		Double totalDebitSum = null;
		        		Long paidInByBankUserAccountId = null;
		        		String customData = null;
		        		String responseData = null;
		        		Long acquirerId = card.getAccount().getAcquirer().getId();
		        		
		        		
		        		
		        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
		        				" AND tp.deleted_at IS NULL AND tp.isLive = " + card.getIsLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
		        		Boolean debitAccountTrue = true;
						Boolean debitCardTrue = true;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						if(accounts!=null && accounts.size()>0)
						{
							Map<String, Object> devicebankaccount = accounts.get(0);
							creditCardId = card.getId();
							creditAccountId = account.getId();
							debitAccountId = (Long)devicebankaccount.get("id");
							recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
						}
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
		        		Transaction transaction = new Transaction(
		        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
		        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
		        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
		        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
		        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
		        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, null, acquirerId, 
		        				debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "MPQR Wallet Credit", card.getIsLive()
		        			);
		        		transaction = (Transaction)swpService.createNewRecord(transaction);
		        		

						


						JSONObject jsbreakdown = new JSONObject();
						jsbreakdown.put("Sub-total", amount);
						jsbreakdown.put("Charges", (fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage));
						
						JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.CREDIT_MERCHANT_MPQR.name(), device.getMerchant().getMerchantName(), device.getDeviceCode(), card.getTrackingNumber(), transaction.getOrderRef().toUpperCase(), 
								transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
						transaction.setSummary(txnDetails.toString());
						swpService.updateRecord(transaction);
		        		
		        		
			        	card.setCardBalance((card.getCardBalance()==null ? 0 : card.getCardBalance()) + Double.valueOf(requestAmount)/100);
			        	swpService.updateRecord(card);

			        	account.setAccountBalance((account.getAccountBalance()==null ? 0 : account.getAccountBalance()) + Double.valueOf(requestAmount)/100);
			        	swpService.updateRecord(account);
			        	
			        	resp.put("status", ERROR.ACCOUNT_CREDITED_SUCCESSFULLY);
			        	
			        	return resp;
			        	
			        }
			        else
			        {
			        	System.out.println("Invalid xML Message1");
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The MPQR  Account could not be credited. Reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println("Invalid Message 2");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The card stop could not be implemented. Please try again");
		        	return resp;
		        }
		    }
		    System.out.println("Invalid xml 3");
		    return null;
		    
		} catch (Exception e) {
			System.out.println("Invalid exception");
		     e.printStackTrace();
		     return null;
		}
	}
	
	
	
	public static JSONObject loadReversalQRWallet(
			String xml, SwpService swpService, String logId) 
	{
		// TODO Auto-generated method stub
		JSONObject resp = new JSONObject();
		JSONArray cardArray = new JSONArray();
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.YEAR, 1);
		Date expiryDate = calendar.getTime();

		DateFormat simpleDateFormatISO8601 = new SimpleDateFormat("yyyyMMdd'T'HH':'mm':'ss");
		String expiryDateFormatted = simpleDateFormatISO8601.format(expiryDate);

		
		try {
		    if(xml!=null && xml.length()>0)
		    {
		    	
		    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		        DocumentBuilder builder = factory.newDocumentBuilder();
		        InputSource is = new InputSource(new StringReader(xml));
		        Document doc = builder.parse(is);
		        NodeList nodeMethods = doc.getElementsByTagName("methodName");
		        Element methodItem = (Element) nodeMethods.item(0);
		        String methodName = methodItem.getTextContent();
		        
		        if(!(methodName!=null && methodName.equals("LoadReversal")))
		        {
		        	resp.put("status", ERROR.INVALID_OPERATION);
		        	resp.put("message", "Invalid operation identifier provided");
		        	return resp;
		        }
		        NodeList nodes = doc.getElementsByTagName("param");
	            String nodeValue;
	            
	            String terminalId = null;
	            String reference = null;
	            String requestAmount = null;
	            String oldReference = null;
	            String narrative = null;
	            String transactionId = null;
	            Date transactionDate = null;
	            String checksum = null;
	            String transactionDateFormatted = null;
	            String transactionData = null;
	            String oldtransactionDateFormatted = null;
	            Date oldtransactionDate = null;
	            
		        if(nodes.getLength()>0)
		        {
			        for (int i = 0; i < nodes.getLength(); i++) {
			        	Element element = (Element) nodes.item(i);
			            NodeList name = element.getElementsByTagName("value");
			            Element line = (Element) name.item(0);
			            System.out.println(logId + "- " + "Name: " + line.getTextContent() + " && " + line.getNodeName());
			            nodeValue = line.getTextContent();
			            
			            
			            
			            switch(i)
			            {
			            	case 0:
					            terminalId = nodeValue;
			            		break;
			            	case 1:
			            		reference = nodeValue;
			            		break;
			            	case 2:
			            		requestAmount = (nodeValue);
			            		break;
			            	case 3:
			            		narrative = nodeValue;
			            		break;
			            	case 4:
			            		transactionData = (nodeValue);
			            		break;
			            	case 5:
			            		oldReference = (nodeValue);
			            		break;
			            	case 6:
			            		oldtransactionDateFormatted = nodeValue;
			            		oldtransactionDate = simpleDateFormatISO8601.parse(oldtransactionDateFormatted);
			            		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String oldtransactionDateFm = sdf.format(oldtransactionDate);
			            		oldtransactionDate = sdf.parse(oldtransactionDateFm);
			            		break;
			            	case 7:
			            		transactionId = nodeValue;
			            		break;
			            	case 8:
			            		transactionDateFormatted = nodeValue;
			            		transactionDate = simpleDateFormatISO8601.parse(transactionDateFormatted);
			            		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			            		String transactionDateFm = sdf.format(transactionDate);
			            		transactionDate = sdf.parse(transactionDateFm);
			            		break;
			            	case 9:
			            		checksum = nodeValue.toLowerCase();
			            		break;
			            	
			            }
			            
			            
			        }
			        
			        String terminalPassword = "";
			        if(terminalId.equals(TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_ID))
			        	terminalPassword = TutukaHelper.RECEIVING_INSTITUTION_TERMINAL_PASSWORD;
			        else if(terminalId.equals(TutukaHelper.ORIGINATING_INSTITUTION_TERMINAL_ID))
			        	terminalPassword = TutukaHelper.ORIGINATING_INSTITUTION_TERMINAL_PASSWORD;
			        
			        String checksumCheck = "LoadReversal"+formatDataForTutuka(terminalId)+""+formatDataForTutuka(reference)+""+formatDataForTutuka(requestAmount) + "" + 
			        		formatDataForTutuka(narrative) + "" + transactionData + "" + formatDataForTutuka(oldReference)+""+oldtransactionDateFormatted + "" +  
			        		formatDataForTutuka(transactionId.trim())+""+transactionDateFormatted;
		    		System.out.println(logId + "- " + "checksum raw --- " + checksumCheck);
		    		checksumCheck = TutukaHelper.generateChecksumSha1(checksumCheck, terminalPassword);
		    		System.out.println(logId + "- " + "checksum -- " + checksum.toLowerCase());
		    		System.out.println(logId + "- " + "checksumCheck -- " + checksumCheck.toLowerCase());
		    		
		    		if(!checksumCheck.toLowerCase().equals(checksum.toLowerCase()))
		    		{
		    			System.out.println("1");
		    			resp.put("status", ERROR.CHECKSUM_MISMATCH);
			        	resp.put("message", "Checksum mismatch");
			        	return resp;
		    		}
			        
			        if(reference!=null)
			        {
			        	System.out.println("2");
			        	String hql = "Select tp from MPQRData tp where tp.deleted_at IS NULL AND (tp.qrCodeId = '"+reference+"')" +
			        			"";
			        	System.out.println(hql);
			        	MPQRData mpqrData = (MPQRData)swpService.getUniqueRecordByHQL(hql);
			        	hql = "Select tp from ECard tp where tp.id = " + mpqrData.getCardId();
			        	ECard card = (ECard)swpService.getUniqueRecordByHQL(hql);
			        	hql = "Select tp from Device tp where tp.id = " + mpqrData.getDeviceId();
			        	Device device = (Device)swpService.getUniqueRecordByHQL(hql);
			        	
			        	
			        	
			        	hql = "Select tp from Transaction tp where tp.orderRef = '" + oldReference + "' AND tp.status = " + TransactionStatus.SUCCESS.ordinal() + 
			        		//" AND tp.amount = '"+ originalAmount +"'" +
			        		" AND tp.card.id = " + card.getId() + " AND tp.isLive = " + card.getIsLive();
			        	Collection<Transaction> oldTransactions = (Collection<Transaction>)swpService.getAllRecordsByHQL(hql, 0, 1);
			        	Transaction oldTransaction = null;
			        	if(oldTransactions==null && oldTransactions.size()>0)
			        	{
			        		oldTransaction = oldTransactions.iterator().next();
			        	}
			        	else
			        	{
			        		/*resp.put("status", ERROR.TRANSACTION_NOT_FOUND);
				        	resp.put("message", "Previous transaction could not be found");
				        	return resp;*/
			        	}
			        	
			        	
		        		String xmlResponse = null; 
		        		
		        		String transactionRef = RandomStringUtils.randomAlphanumeric(16);
		        		String bankPaymentReference = null;
		        		Long customerId = card.getCustomerId();
		        		boolean creditAccountTrue = false;
		        		boolean creditCardTrue = false;
		        		Boolean creditWalletTrue = null;
		        		Boolean creditMobileAccountTrue = null;
		        		String orderRef = transactionId;
		        		Channel channel = Channel.NOT_SPECIFIED;
		        		String rpin = null;
		        		ServiceType serviceType = ServiceType.REVERSE_MPQR_WALLET_LOAD;
		        		String payerName = device.getMerchant().getMerchantName();
		        		String payerEmail = device.getMerchant().getContactEmail();
		        		String payerMobile = device.getMerchant().getContactMobile();
		        		TransactionStatus status = TransactionStatus.SUCCESS;
		        		ProbasePayCurrency probasePayCurrency = card.getAccount().getProbasePayCurrency();
		        		String transactionCode = null;
		        		Account account = card.getAccount();
		        		Boolean creditPoolAccountTrue = false;
		        		String messageRequest = xml;
		        		String messageResponse = xmlResponse;
		        		Double fixedCharge = card.getCardScheme().getOverrideFixedFee();
		        		Double transactionCharge = 0.00;
		        		Double transactionPercentage = 0.00;
		        		Double schemeTransactionCharge = card.getCardScheme().getOverrideTransactionFee();
		        		Double schemeTransactionPercentage = 0.00;
		        		Double amount = Double.valueOf(requestAmount)/100;
		        		String responseCode = TransactionCode.transactionSuccess;
		        		String otp = null;
		        		String otpref = null;
		        		Long merchantId = null;
		        		String merchantName = null;
		        		String merchantCode = null;
		        		String merchantBank = null;
		        		String merchantAccount = null;
		        		Long transactingBankId = card.getAcquirer().getBank().getId();
		        		Long receipientTransactingBankId = null;
		        		Integer accessCode = null;
		        		Long sourceEntityId = card.getId();
		        		Long receipientEntityId = null;
		        		Channel receipientChannel = channel;
		        		String transactionDetail = "Reverse MPQR Wallet Load: Debit "+amount+" from card #"+card.getPan();
		        		Double closingBalance = card.getCardBalance() - amount;
		        		Double totalCreditSum = null;
		        		Double totalDebitSum = null;
		        		Long paidInByBankUserAccountId = null;
		        		String customData = null;
		        		String responseData = null;
		        		Long acquirerId = card.getAccount().getAcquirer().getId();
		        		
		        		
		        		hql = "Select tp.* from accounts tp WHERE tp.accountType = " + AccountType.PROBASE_TUTUKA_MASTERCARD_TRANSIT_ACCOUNT.ordinal() + 
		        				" AND tp.deleted_at IS NULL AND tp.isLive = " + card.getIsLive();
						List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
		        		Boolean debitAccountTrue = true;
						Boolean debitCardTrue = true;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						if(accounts!=null && accounts.size()>0)
						{
							Map<String, Object> devicebankaccount = accounts.get(0);
							debitCardId = card.getId();
							debitAccountId = account.getId();
							creditAccountId = (Long)devicebankaccount.get("id");
							recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
						}
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
		        		
		        		Transaction transaction = new Transaction(
		        				transactionRef, bankPaymentReference, customerId, creditAccountTrue, creditCardTrue, orderRef, rpin, 
		        				channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, probasePayCurrency, transactionCode, account, card, 
		        				device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, transactionCharge, transactionPercentage, 
		        				schemeTransactionCharge, schemeTransactionPercentage, amount, responseCode, otp, otpref, merchantId, merchantName, merchantCode, merchantBank, merchantAccount, 
		        				transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, receipientEntityId, receipientChannel, transactionDetail, 
		        				closingBalance, totalCreditSum, totalDebitSum, paidInByBankUserAccountId, customData, responseData, 
		        				oldTransaction==null ? null : oldTransaction.getId(), acquirerId, 
        						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Reverse MPQR Wallet Load", card.getIsLive()
		        			);
		        		transaction = (Transaction)swpService.createNewRecord(transaction);

			        	card.setCardBalance((card.getCardBalance()==null ? 0 : card.getCardBalance()) - Double.valueOf(requestAmount)/100);
			        	swpService.updateRecord(card);
			        	
			        	account.setAccountBalance((account.getAccountBalance()==null ? 0 : account.getAccountBalance()) - Double.valueOf(requestAmount)/100);
			        	swpService.updateRecord(account);
			        	
			        	resp.put("status", ERROR.ACCOUNT_CREDIT_REVERSED_SUCCESSFULLY);
			        	
			        	return resp;
			        	
			        }
			        else
			        {
			        	System.out.println("Invalid xML Message1");
			        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
			        	resp.put("message", "The MPQR  Account Load could not be Reversed. Reference could not be matched to a card. Please try again");
			        	return resp;
			        }
		        }
		        else
		        {
		        	System.out.println("Invalid Message 2");
		        	resp.put("status", ERROR.INVALID_XML_MESSAGE);
		        	resp.put("message", "The MPQR  Account Load could not be implemented. Please try again");
		        	return resp;
		        }
		    }
		    System.out.println("Invalid xml 3");
		    return null;
		    
		} catch (Exception e) {
			System.out.println("Invalid exception");
		     e.printStackTrace();
		     return null;
		}
	}
	
	
	
	public static JSONObject formatTransactionData(String transactionDataToBeParsed) {
		// TODO Auto-generated method stub
		JSONObject txnDataJSON = new JSONObject();
		try
		{
	    	while(transactionDataToBeParsed.length()>0)
	    	{
	    		System.out.println("transactionDataToBeParsed.."+ transactionDataToBeParsed);
	    		String key = transactionDataToBeParsed.substring(0, 3);
	    		System.out.println("key .." + key);
	    		transactionDataToBeParsed = transactionDataToBeParsed.substring(3);
	    		System.out.println("1 transactionDataToBeParsed.."+ transactionDataToBeParsed);
	    		String lengthStr = transactionDataToBeParsed.substring(0, 2);
	    		System.out.println("lengthStr.." + lengthStr);
	    		transactionDataToBeParsed = transactionDataToBeParsed.substring(2);
	    		System.out.println("2 transactionDataToBeParsed.."+ transactionDataToBeParsed);
	    		lengthStr = lengthStr.charAt(0)=='0' ? (lengthStr.charAt(1)+"") : lengthStr;
	    		System.out.println("lengthStr..." + lengthStr);
	    		int length = Integer.valueOf(lengthStr);
	    		System.out.println("length..." + length);
	    		String value = length==0 ? null : transactionDataToBeParsed.substring(0, length);
	    		System.out.println("value..." + value);
	    		transactionDataToBeParsed = transactionDataToBeParsed.substring(length);
	    		System.out.println("3 transactionDataToBeParsed.."+ transactionDataToBeParsed);
	    		
	    		KLVData klvData = null;
	    		
	    		if(value!=null)
	    		{
	        		switch(key)
	        		{
	        			case "002":
	        				txnDataJSON.put(KLVData.TRACKING_NUMBER.name(), value);
	        				break;
	        			case "004":
	        				txnDataJSON.put(KLVData.ORIGINAL_TRANSACTION_AMOUNT.name(), Double.valueOf(value));
	        				break;
	        			case "026":
	        				txnDataJSON.put(KLVData.MERCHANT_CATEGORY_CODE.name(), value);
	        				break;
	        			case "037":
	        				txnDataJSON.put(KLVData.RETRIEVAL_REFERENCE_NUMBER.name(), value);
	        				break;
	        			case "041":
	        				txnDataJSON.put(KLVData.TERMINAL_ID.name(), value);
	        				break;
	        			case "042":
	        				txnDataJSON.put(KLVData.MERCHANT_IDENTIFIER.name(), value);
	        				break;
	        			case "048":
	        				txnDataJSON.put(KLVData.FRAUD_SCORING_DATA.name(), value);
	        				break;
	        			case "049":
	        				txnDataJSON.put(KLVData.ORIGINAL_CURRENCY_CODE.name(), value);
	        				break;
	        			case "050":
	        				txnDataJSON.put(KLVData.FROM_ACCOUNT.name(), value);
	        				break;
	        			case "052":
	        				txnDataJSON.put(KLVData.PIN_BLOCK.name(), value);
	        				break;
	        			case "085":
	        				txnDataJSON.put(KLVData.MARKUP_AMOUNT.name(), Double.valueOf(value));
	        				break;
	        			case "250":
	        				txnDataJSON.put(KLVData.CAPTURE_MODE.name(), value);
	        				break;
	        			case "251":
	        				txnDataJSON.put(KLVData.NETWORK.name(), value);
	        				break;
	        			case "252":
	        				txnDataJSON.put(KLVData.FEE_TYPE.name(), value);
	        				break;
	        			case "253":
	        				txnDataJSON.put(KLVData.LAST_FOUR_DIGITS_PAN.name(), value);
	        				break;
	        			case "254":
	        				txnDataJSON.put(KLVData.MDES_DIGITIZED_PAN.name(), value);
	        				break;
	        			case "255":
	        				txnDataJSON.put(KLVData.MDES_DIGITIZED_WALLET_ID.name(), value);
	        				break;
	        			case "256":
	        				txnDataJSON.put(KLVData.ADJUSTMENT_REASON.name(), value);
	        				break;
	        			case "257":
	        				txnDataJSON.put(KLVData.REFERENCE_ID.name(), value);
	        				break;
	        			case "258":
	        				txnDataJSON.put(KLVData.MARKUP_TYPE.name(), value);
	        				break;
	        			case "900":
	        				txnDataJSON.put(KLVData.SECURE3D_OTP.name(), value);
	        				break;
	        			case "901":
	        				txnDataJSON.put(KLVData.DIGITIZATION_ACTIVATION.name(), value);
	        				break;
	        			case "999":
	        				txnDataJSON.put(KLVData.GENERIC_KEY.name(), value);
	        				break;
	        			default:
	        				break;
	        		}
	    		}
	    	}
	    	return txnDataJSON;
		}
    	catch(JSONException e)
    	{
    		return null;
    	}
	}


	private static Channel getChannelByTutukaTransactionType(String transactionType) {
		// TODO Auto-generated method stub
		Channel channel;
		switch(transactionType)
		{
			case "00":
				channel = Channel.POS;
				break;
			case "01":
				channel = Channel.ATM;
				break;
			case "09":
				channel = Channel.POS;
				break;
			default:
				channel = Channel.NOT_SPECIFIED;
				break;
			
		}
		
		return channel;
	}
	
	
	
	private static ServiceType getServiceTypeByTutukaTransactionType(String transactionType)
	{
		ServiceType serviceType;
		switch(transactionType)
		{
			case "00":
				serviceType = ServiceType.DEBIT_CARD;
				break;
			case "01":
				serviceType = ServiceType.DEBIT_CARD;
				break;
			case "02":
				serviceType = ServiceType.ADJUSTMENT;
				break;
			case "09":
				serviceType = ServiceType.CASHBACK;
				break;
			case "21":
				serviceType = ServiceType.DEPOSIT_OTC;
				break;
			default:
				serviceType = null;
				break;
			
		}
		
		return serviceType;
	}
	
	
	
	public static String formatDataForTutuka(String data)
	{
		//return data.replaceAll("[^a-zA-Z0-9\\s]", "");
		return data;
	}
	
	private static String formatDataForTutukaStripSpecialCharacter(String data)
	{
		return data.replaceAll("[^a-zA-Z0-9\\s]", "");
	}
	
	
	
}
