package com.probase.probasepay.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.probase.probasepay.services.TutukaServicesV2;
import com.probase.probasepay.util.ERROR;

/**
 * Servlet implementation class TutukaServlet
 */
public class TutukaServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(TutukaServlet.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TutukaServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("Tester8");
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String method = request.getMethod();  
		String xml = null;
		PrintWriter out = response.getWriter();
		String logId = RandomStringUtils.randomNumeric(20) + " TUTUKA";
		try
		{
			byte[] xmlData = new byte[request.getContentLength()];
			InputStream sis = request.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(sis);
			bis.read(xmlData, 0, xmlData.length);
			if (request.getCharacterEncoding() != null) {
				xml = new String(xmlData, request.getCharacterEncoding());
			}
			else {
				xml = new String(xmlData);
			}
			
			System.out.println(logId + ": " + xml);
			
			BufferedReader reader = new BufferedReader(new StringReader(xml));
		    StringBuffer result = new StringBuffer();
		    try {
		        String line;
		        while ( (line = reader.readLine() ) != null)
		            result.append(line.trim());
		        
		        xml = result.toString();
		    } catch (IOException e) {
		        throw new RuntimeException(e);
		    }
			
		    System.out.println(logId + ": " + xml);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        InputSource is = new InputSource(new StringReader(xml));
	        Document doc = builder.parse(is);
	        NodeList nodes = doc.getElementsByTagName("methodName");
	        String methodName = null;
	        JSONObject resp = null;
            String nodeValue;
            String xmlResponse = "";
            
	        if(nodes.getLength()>0)
	        {
		        Element element = (Element) nodes.item(0);
		        System.out.println("Name: " + element.getTextContent() + " && " + element.getNodeName());
		        methodName = element.getTextContent();
		        xmlResponse = formatXMLResponse(methodName, xml, logId);
		        
		        if(xmlResponse!=null && xmlResponse.length()>0)
		        {
		        	System.out.println(logId + ": XML" + "==> " + xml);
		        	System.out.println(logId + ": GENERAL_OK" + "==> " + xmlResponse);
		        	response.setContentType("text/xml");
        			out.write(xmlResponse);
		        }
	        }
	        else
	        {
	        	System.out.println(logId + ": INVALID_XML_MESSAGE" + "==> " + xml);
	        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(logId + ": Exception" + "==> ", e);
		}
	}

	public String formatXMLResponse(String methodName, String xml, String logId) throws JSONException
	{
		String xmlResponse = "";
		switch(methodName)
        {
        	case "Deduct":
        		xmlResponse = handleDeduct(xml, logId);
        		break;
        	case "DeductReversal":
        		xmlResponse = handleDeductReversal(xml, logId);
        		break;
        	case "DeductAdjustment":
        		xmlResponse = handleDeductAdjustment(xml, logId);
        		break;
        	case "LoadAdjustment":
        		xmlResponse = handleLoadAdjustment(xml, logId);
        		break;
        	case "LoadReversal":
        		xmlResponse = handleLoadReversal(xml, logId);
        		break;
        	case "Stop":
        		xmlResponse = handleStop(xml, logId);
        		break;
        	case "AdministrativeMessage":
        		xmlResponse = handleAdministrativeMessage(xml, logId);
        		break;
        	case "LoadAuth":
        		xmlResponse = handleLoadAuth(xml, logId);
        		break;
        	case "LoadAuthReversal":
        		xmlResponse = handleLoadAuthReversal(xml, logId);
        		break;
        	case "Balance":
        		xmlResponse = handleBalance(xml, logId);
        		break;
			default:
				break;
        	
        }
		return xmlResponse;
	}
	
	
	public String handleDeduct(String xml, String logId) throws JSONException
	{
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.deductTutukaCompanionCards(xml, logId); 
		//resp.put("status", "-7");
		if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_SUCCESSFUL))
		{
			
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
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_TRANSACTION_AMOUNT_EXCEEDS_LIMIT))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>-18</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_FAIL_INCORRECT_EXP_DATE))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>1001</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_FAIL_INSUFFICIENT_FUNDS))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>-17</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_FAIL_INCORRECT_CVV_MASTERCARD))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>1022</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_FAIL_INCORRECT_CVV_VISA))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>1000</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else
		{
			//RESPOND WITH TIMEOUT CODE -9
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>-7</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
			System.out.println(logId + ": ERROR" + " ==> " + xml);
			System.out.println(logId + ": INVALID_XML_MESSAGE" + " ==> " + xml);
		}
		return xmlResponse;
	}
	
	
	
	public String handleBalance(String xml, String logId) throws JSONException
	{
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.getTutukaCompanionCardBalance(xml, logId); 
		if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
		{
			
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
			                     xmlResponse = xmlResponse + "<int>"+ resp.getDouble("balance")+"</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.GENERAL_FAIL))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>-26</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		return xmlResponse;
	}

	public String handleDeductReversal(String xml, String logId) throws JSONException
	{
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.deductReversalTutukaCompanionCards(xml, logId); 
		/*if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_CARD_REVERSAL_SUCCESS))
		{
			
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
		}*/
		
		
		//DUMMY
		xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
		return xmlResponse;
	}

	public String handleDeductAdjustment(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.deductAdjustmentTutukaCompanionCard(xml, logId); 
		if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_ADJUSTMENT_SUCCESS))
		{
			
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
		}
		else if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.DEBIT_ADJUSTMENT_SUCCESS_OVERDEBIT))
		{
			
			xmlResponse = xmlResponse + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			xmlResponse = xmlResponse + "<methodResponse>";
			   xmlResponse = xmlResponse + "<params>";
			      xmlResponse = xmlResponse + "<param>";
			         xmlResponse = xmlResponse + "<value>";
			            xmlResponse = xmlResponse + "<struct>";
			               xmlResponse = xmlResponse + "<member>";
			                  xmlResponse = xmlResponse + "<name>resultCode</name>";
			                  xmlResponse = xmlResponse + "<value>";
			                     xmlResponse = xmlResponse + "<int>-9</int>";
			                  xmlResponse = xmlResponse + "</value>";
			               xmlResponse = xmlResponse + "</member>";
			            xmlResponse = xmlResponse + "</struct>";
			         xmlResponse = xmlResponse + "</value>";
			      xmlResponse = xmlResponse + "</param>";
			   xmlResponse = xmlResponse + "</params>";
			xmlResponse = xmlResponse + "</methodResponse>";
		}
		else
		{
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
		}
		return xmlResponse;
	}
	
	public String handleLoadAdjustment(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.loadAdjustmentTutukaCompanionCard(xml, logId); 
		/*if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.LOAD_ADJUSTMENT_SUCCESS))
		{
			
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
		}*/
		
		//DUMMY
		xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
		return xmlResponse;
	}
	

	public String handleLoadReversal(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.loadAdjustmentReversalTutukaCompanionCard(xml, logId); 
		/*if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.LOAD_REVERSAL_SUCCESS))
		{
			
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
		}*/
		
		
		//DUMMY
		xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
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
		return xmlResponse;
	}
	
	

	public String handleStop(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.stopRemoteTutukaCompanionCard(xml, logId); 
		if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.CARD_STOPPED_SUCCESSFULLY))
		{
			
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
		}
		return xmlResponse;
	}
	
	
	
	public String handleAdministrativeMessage(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		System.out.println(">>>>>>>>>>" + 1);
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.handleAdministrativeMessage3DSecureOTP(xml, logId); 
		if(resp!=null && resp.has("status") && resp.getInt("status")==(ERROR.GENERAL_OK))
		{
			
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
		}
		return xmlResponse;
	}
	
	
	

	public String handleLoadAuth(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.handleLoadAuth(xml, logId);
		if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.LOAD_AUTH_SUCCESS))
		{
			
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
		}
		return xmlResponse;
	}
	


	public String handleLoadAuthReversal(String xml, String logId) throws JSONException {
		// TODO Auto-generated method stub
		TutukaServicesV2 tutukaService = new TutukaServicesV2();
		String xmlResponse = "";
		JSONObject resp = tutukaService.handleLoadAuthReversal(xml, logId);
		if(resp!=null && resp.has("status") && resp.getString("status").equals(ERROR.LOAD_AUTH_REVERSAL_SUCCESS))
		{
			
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
		}
		return xmlResponse;
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Tester1");
		response.setContentType("text/html;charset=UTF-8");
      // Allocate a output writer to write the response message into the network socket
      PrintWriter out = response.getWriter();
 
      // Write the response message, in an HTML page
      try {
         out.println("<!DOCTYPE html>");
         out.println("<html><head>");
         out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
         out.println("<title>Hello, World</title></head>");
         out.println("<body>");
         out.println("<h1>Hello, world!</h1>");  // says Hello
         // Echo client's request information
         out.println("<p>Request URI: " + request.getRequestURI() + "</p>");
         out.println("<p>Protocol: " + request.getProtocol() + "</p>");
         out.println("<p>PathInfo: " + request.getPathInfo() + "</p>");
         out.println("<p>Remote Address: " + request.getRemoteAddr() + "</p>");
         // Generate a random number upon each request
         out.println("<p>A Random Number: <strong>" + Math.random() + "</strong></p>");
         out.println("</body>");
         out.println("</html>");
         out.flush();
         out.close();
      } finally {
         out.close();  // Always close the output writer
      }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
