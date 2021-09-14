package com.probase.probasepay.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.itextpdf.html2pdf.HtmlConverter;
import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.models.SMSMesage;

public class SmsSender implements Runnable {
	private static Logger log = Logger.getLogger(SmsSender.class);
	SwpService swpService;
	String message;
	String htmlMessage;
	String subject;
	String receipientMobileNumber;
	String receipientEmailAddress;
	String[] attachments;
	private static final String HTTP_API_KEY = "key-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
	private static final String HTTP_URL = "https://api.mailgun.net/v3/mg.example.com/messages";
	//final static String baseUrl = "http://smsapi.probasesms.com/apis/text/index.php";
	//final static String baseUrl = "https://probasesms.com/api/json/multi/res/bulk/sms";
	///https://www.probasesms.com/text/multi/res/trns/sms?username=rtsa&password=password@1&mobiles=260967307151&message=Logging+Into+ProbasePay.com%3F+%0AYour+One+Time+Password+is+6958%0A%0AThank+You.&sender=RTSA&type=TEXT
	final static String baseUrl = "https://probasesms.com/text/multi/res/trns/sms";
	
	public SmsSender(SwpService swpService, String message, String receipientMobileNumber)
	{
		this.swpService = swpService;
		this.message = message;
		this.receipientMobileNumber = receipientMobileNumber;
		this.receipientMobileNumber = "260967307151";
		this.subject = null;
		this.attachments = null;
	}
	
	
	
	public SmsSender(SwpService swpService, String htmlMessage, String receipientEmailAddress, String subject, String html)
	{
		this.swpService = swpService;
		this.htmlMessage = htmlMessage;
		this.receipientEmailAddress = receipientEmailAddress;
		this.subject= subject;
		this.attachments = attachments;
	}
	
	public static String getSmsResponseStatus(String resp)
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance()
	                .newDocumentBuilder()
	                .parse(new InputSource(new StringReader(resp)));
			NodeList errNodes = doc.getElementsByTagName("response");
			if (errNodes.getLength() > 0) {
			    Element err = (Element)errNodes.item(0);
			    return (err.getElementsByTagName("messagestatus")
			                          .item(0)
			                          .getTextContent());
			}
			return null;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public void run() {
		try
		{
			System.out.println("this.subject === " + this.subject);
			if(this.subject==null)
			{
				/**/receipientMobileNumber = receipientMobileNumber.startsWith("260") ? receipientMobileNumber : (receipientMobileNumber.startsWith("0") ? ("260"+receipientMobileNumber.substring(1)) : null);
				
				if(receipientMobileNumber!=null)
				{
					//receipientMobileNumber = "260967307151";
					String url = baseUrl + "?username=smspbs@123$$&password=pbs@sms123$$&mobiles="+receipientMobileNumber+"&message="+URLEncoder.encode(message,"UTF-8")+"&sender=Bevura&type=TEXT";
					//String url = baseUrl + "?SMS_MAXIMUM_ATTEMPTS=2&SMS_AUTH_USERNAME=pbs@sms123$$&SMS_AUTH_PASSWORD=smspbs@123$$&mobiles="+receipientMobileNumber+"&message="+URLEncoder.encode(message,"UTF-8")+"&SMS_SENDER_ID=probase&type=TEXT";
					System.out.println("url...." + url);
			
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
					// optional default is GET
					//con.setRequestMethod("GET");
					con.setRequestMethod("POST");
			
					//add request header
					con.setRequestProperty("User-Agent", "Mozilla/5.0");
			
					int responseCode = con.getResponseCode();
					System.out.println("\nSending 'GET' request to URL : " + url);
					System.out.println("Response Code : " + responseCode);
			
					if(responseCode==200)
					{
						BufferedReader in = new BufferedReader(
						        new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer response = new StringBuffer();
				
						while ((inputLine = in.readLine()) != null) {
							response.append(inputLine);
						}
						in.close();
						con.disconnect();
				
						//print result
						System.out.println(response.toString());
						String status = getSmsResponseStatus(response.toString());
						SMSMesage sms = new SMSMesage();
						sms.setReceipentMobileNumber(receipientMobileNumber);
						sms.setResponseCode(responseCode);
						sms.setMessage(message);
						sms.setCreatedAt(new Date());
						sms.setUpdatedAt(new Date());
						sms.setDataResponse(response.toString());
						sms.setStatus(status==null ? SMSMessageStatus.FAILED.name() : SMSMessageStatus.SENT.name());
						swpService.createNewRecord(sms);
					}
				}
				/*String parameters = "SMS_MAXIMUM_ATTEMPTS=2&SMS_AUTH_USERNAME=pbs@sms123$$&SMS_AUTH_PASSWORD=smspbs@123$$&mobiles="+receipientMobileNumber+"&message="+URLEncoder.encode(message,"UTF-8")+"&SMS_SENDER_ID=probase&type=TEXT";
				JSONObject jsObj = new JSONObject();
				jsObj.put("Content-Type", "application/json");
				String test = UtilityHelper.sendPost(baseUrl, parameters, jsObj);*/
			}
			else
			{
				String mailgunurl = "https://api.mailgun.net/v3/mails.shikola.com/messages";
				String username = "api";
				String password = "key-d2ae63cf4e7f4e08b04b4ac4e37e10df";
				String api = "Basic";
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(
				  auth.getBytes(StandardCharsets.ISO_8859_1));
				String authHeader = "Basic " + new String(encodedAuth);
				JSONObject header = new JSONObject();
				header.put("Authorization", authHeader);
				JSONObject parameterJS = new JSONObject();
				parameterJS.put("from", "Welcome<mailer@shikola.com>");
				parameterJS.put("to", ""+ this.receipientEmailAddress +"");
				parameterJS.put("cc", ""+ this.receipientEmailAddress +"");
				parameterJS.put("bcc", "smicer66@gmail.com");
				parameterJS.put("subject", ""+ subject +"");
				parameterJS.put("html", this.htmlMessage);
				//parameterJS.put("attachment", "'Welcome<mailer@shikola.com>'");
				
				

				String msg = "";
				Iterator<String> iter = parameterJS.keys();
				while(iter.hasNext())
				{
					String key = iter.next();
					msg = msg + (key + "=" + parameterJS.getString(key)) + "&";
				}
				String test = UtilityHelper.sendPost(mailgunurl, msg, header);
				System.out.println(test);
		        log.info(test);
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(">>", e);
		}
	}
	
}

