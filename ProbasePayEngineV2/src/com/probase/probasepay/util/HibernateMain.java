package com.probase.probasepay.util;


import java.security.Key;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
/*import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;*/

import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.User;
import com.probase.probasepay.services.AuthenticationServicesV2;
import com.probase.probasepay.services.MerchantServices;
import com.probase.probasepay.services.UserServicesV2;

public class HibernateMain {

	/**/
	/*private static Logger log = Logger.getLogger(MerchantServices.class);
	private static ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public static SwpService swpService = null;
	public static PrbCustomService swpCustomService = PrbCustomService.getInstance();*/
	
	 public static void main(String[] args) {
		 
		 //byte[] d = Base64.decodeBase64("V01YR0dIb3d6RmRxMGZwVGc5M3BZbUE1V2p1aXE5N2w=".getBytes());
		 //System.out.println(new String(d));
		 
		 //AuthenticationServices as = new AuthenticationServices();
		 /*String str = as.createNewUserAccount("smicer66-pkcs7@gmail.com", 
				 "eyJpdiI6IjA2Z3JXbldKZDRsaWoxaDBGc29pcEE9PSIsInZhbHVlIjoi" +
				 "TW5mZFhac3VPVFNcL0htTlRuMEVKV2Fub2VHWnZjMmR4VitpTTBBZHdP" +
				 "SXZcL0Y4SUFLd3hZWUhCRnJQUkFndnZmd3poVG1sdzVGekE4YURsZVwv" +
				 "ZFhvUjMwaTR0WHZUeUhWXC9NQk5PdUd3clwvOD0iLCJtYWMiOiI3OWE5" +
				 "N2FhZDdkMzMxYjRkMDFlNjJjYjgwYTE3ZWMxOWU1MTk3OGRhOTVlMTEy" +
				 "N2E4YjYxY2E4MmJiZTdiNjFlIn0=", "BANK_STAFF", "035");*/
		 /*String str = as.authenticateUsers("smicer66@gmail.com", 
				 "eyJpdiI6IjRUN3hcL0taNm5mYjJ6a2hVaXFkRVdnPT0iLCJ2YWx1ZSI6" +
				 "IkRyQzlBc084ZTYrSUVaZCtsNjhHM1E9PSIsIm1hYyI6Ijg1ZTk3MTI5" +
				 "NWJmNGY4NjNlMWJhZjJmYWMyMzYyMzc2ODBkNDc2YWIxNzU4NDkwODVm" +
				 "MzkxOGI5OTUxNjk4NTgifQ==", "035");*/
		 /*UserServices us = new UserServices();
		 String str = us.createNewAdminUser(null, "12 Ade", "", null, null, "test@gmail.com", "0803094422", "08/08/07", "John", "MALE", "Fenim", "Bas", 
				 "POTZR_STAFF", 1L, "eyJpdiI6IjF4aUFHVGN4TmJaTTdoaXkyZUh1VHc9PSIsInZhbHVlIjoiVGo2OERoRFBvVG4xaHJZOWpOOW1EQT09Iiwi" +
				 		"bWFjIjoiNTExN2M0M2UxMmQxODMwZjZiYWEzNDQzZTBkZjM4YjM3ODk0ZTYyNGU1ZDUyZjc3MzBjZjI2OTc3ZTI0Y2Q4ZSJ9", null);
		 System.out.println("str =" + str);*/
		  /*Configuration configuration=new Configuration();
		  configuration.configure("hibernate.cfg.xml");
		  ServiceRegistry sr= new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
		  SessionFactory sf=configuration.buildSessionFactory(sr);
		  
		  /*User user1=new User();
		  user1.setUserName("Arpit");
		  user1.setUserMessage("Hello world from arpit");
		  
		  User user2=new User();
		  user2.setUserName("Ankita");
		  user2.setUserMessage("Hello world from ankita");
		  Session ss=sf.openSession();
		  ss.beginTransaction();
		 //saving objects to session
		  ss.save(user1);
		  ss.save(user2);
		  ss.getTransaction().commit();
		  ss.close();*/
		 /*String status = "ACTIVE";
		 JSONObject jsonObject = new JSONObject();
		 try {
				swpService = serviceLocator.getSwpService();
				Collection<Merchant> merchantList = null;
				String hql = "Select tp from Merchant tp where tp.status = "+(MerchantStatus.ACTIVE.ordinal() + 1)+"";
				
				if(status==null)
					merchantList = (Collection<Merchant>)swpService.getAllRecords(Merchant.class);
				else
					merchantList = (Collection<Merchant>)swpService.getAllRecordsByHQL(hql);
					
				jsonObject.put("status", 1);
				jsonObject.put("message", "Merchant list fetched");
				jsonObject.put("merchantlist", merchantList.toArray());
				System.out.println(jsonObject.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				jsonObject.put("status", 0);
				jsonObject.put("message", "Merchant creation Failed");
				log.debug(e.getMessage());
				System.out.println(jsonObject.toString());
			}*/
		 
		 /*MerchantServices merchantServices = new MerchantServices();
		 String str = merchantServices.createNewMerchant("0001", 
				 "Testing", 
				 null, 
				 null, 
				 null, 
				 "0001", 
				 "asdasd", 
				 "2asdas", 
				 "asdasd", 
				 "James & Harding", 
				 "192102", 
				 "sldasld@sasdlasd.com", 
				 "0802303", 
				 1L, 
				 "James & Harding", 
				 1L);*/
		 //try{
			 /*AuthenticationServices authService = new AuthenticationServices();
			 String token = authService.authenticateUsers("James", "Password");
			 System.out.println(token);*/
			 
			 
			 /*String encryptedStr = "eyJpdiI6Iis0NVVIREE3cDJGTTZLTDVKTVVBbWc9PSIsInZhbHVlIjoiQ2" +
			 		"k5YXk2SmptWFhcL1hEN1wvRHhFdjZnPT0iLCJtYWMiOiJhZjk5MzQ1ZDI3YjVhMGVjODBkY2F" +
			 		"lMzgwMzQwYjdlODU4ZjUxNjVmMGYxMTBmY2Y3YTI3NWNiNGMzYWQ2MTAwIn0=";
			 
			 byte[] decode = Base64.decodeBase64(encryptedStr.getBytes());
			 String decodeStr = new String(decode);
			 System.out.println("Decoded = " + decodeStr);
			 JSONObject jsonObject = new JSONObject(decodeStr);
			 String iv1 = jsonObject.getString("iv");
			 String value = jsonObject.getString("value");
			 String mac = jsonObject.getString("mac");
			 byte[] keyValue = "WMXGGHowzFdq0fpTg93pYmA5Wjuiq97l".getBytes();
			 
			 	Key key = new SecretKeySpec(keyValue, "AES");
			    byte[] iv = Base64.decodeBase64(iv1.getBytes("UTF-8"));
			    byte[] decodedValue = Base64.decodeBase64(value.getBytes("UTF-8"));


			    System.out.println(key.getAlgorithm());
			    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); // or PKCS5Padding
			    c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			    byte[] decValue = c.doFinal(decodedValue);

			    int firstQuoteIndex = 0;
			    while(decValue[firstQuoteIndex] != (byte)'"') firstQuoteIndex++;
			    String vl = new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length-2));
			    System.out.println("vl = " + vl);
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 */
			 
			 
		 
		 
		 
		 //System.out.println("Str = " + str);
		 //System.out.println(UtilityHelper.generatePan("260", "01", "01", "7992739871"));
	  
	 }

}


