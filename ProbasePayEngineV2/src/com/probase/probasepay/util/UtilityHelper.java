package com.probase.probasepay.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import javax.servlet.http.HttpServletRequest;
import javax.swing.event.ListSelectionEvent;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import com.dstvcc.models.AccountStatus;
import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.BillType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.SMSMessageStatus;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.ECardDeposit;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.BevuraToken;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.MerchantBankAccount;
import com.probase.probasepay.models.MerchantPayment;
import com.probase.probasepay.models.RequestTransactionReversal;
import com.probase.probasepay.models.SMSMesage;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.services.AccountServicesV2;
import com.probase.probasepay.services.AuthenticationServicesV2;
import com.probase.probasepay.services.BankServices;

//import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.buf.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;


public class UtilityHelper {
	private static Logger log = Logger.getLogger(UtilityHelper.class);


	private final static String USER_AGENT = "Mozilla/5.0";

	/*test*/
	public static final String ZICB_OTP_GENERATE_WALLET_URL_TEST = "http://41.175.14.69:7664/api/json/commercials/probase/zicb/fundsTransfer";
	//public static final String ZICB_CREATE_WALLET_URL_TEST = "http://41.175.14.69:7664/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_CREATE_WALLET_URL_TEST = "http://41.175.13.198:7664/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_DEBIT_WALLET_URL_TEST = "http://41.175.14.69:7664/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_GET_BANK_ACCT_DETAILS_TEST = "http://41.175.14.69:7664/api/json/commercials/probase/zicb/fundsTransfer";
	
	/*Live*/
	public static final String ZICB_OTP_GENERATE_WALLET_URL = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_CREATE_WALLET_URL = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_DEBIT_WALLET_URL = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_GET_BANK_ACCT_DETAILS = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_QUERY_BY_ACCT_NO_URL = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	public static final String ZICB_QUERY_BY_MOBILE_NUMBER_URL = "http://41.175.13.198:8990/api/json/commercials/probase/zicb/fundsTransfer";
	
	//public static final String ZICB_AUTH_KEY = "Mgl0DlqjD8nuzXLOjUfWiKctuS9JHUg1b7dDR3pfFRxUupaRJzWwRXLJV1Rv3Q7BiKcLvbU0RrzITQoWUpWYA09Vk9s9ZafDvyecefhuNGZsm3VgbQGbVyUxX0r1Pcydqu6VOVZuofrOzStdQPvRXzhVnKfyPivcUwhI6A5mQdI5FEnvvYm7M8D25sRcuQK3vwKSk0vAeP1VfJxaCZydgW2dHvwk9kwYkTmVeKaypGNTMDF89NVlxi5rLETzu3wpAU1Dp25boxZp0fkt2cqs7AgsFZOZlbvMfSxTVjXmd7gz92fYnUVwIMJ7h0mXEnab93QudyohFwsmnpVKRkBGSRBPeDRBRETvwAFTs8jJYhfmqNB65lXTABq2L5rDaDJjs3qaUiN8hrWWCLpUaca0ytss94e1bIXBJAN3R9gz7eJ3VO6IUSGkuO1qbQMDi1NrNwfTdy0Xdawg8eU0qjhLqr5awCPIQXGhOwIVdIlUCfndpHZGwkLwdz2DO41URBMBcRJ9Q5C7jIfws69H1RjOUrwFYy9ZOCp0uVOIdlwmQ3gujcBi4NbX78tWhvBHd0DX18ieK8v3UWiapRIRwVG7zmCHbD66z7LdXu8MqfzDX1seupwZ45QIbY4DSZIlvAqipg9KPMZxLDfd9Y7IdEavlPaZP6aIgGJlM5X9kXaZRjFEvtL5TuSrNoUMQTcMIZqS7gTaqiDnhZzkcuMf1JGNIPKbOQuTOTFpnDyj11xB9r6QG0pLglQl8gejHi9wwyypQ4bmmI0irIvt5xEwTU3pguFF7cIBojyEVD7ipy3SDvo47eeWJoehCfleWAI30UzgVQNKSNFtyauh0ccADDnWZS6QauwVEOj4kaGRplJgfdtT4Vwo7waWmcJxrsL4pWzPfMMEIqvP71KWXVGLA3r4NkygxT9hysuD15ZtU22V5jr9gokAq10NnlZ3t9YWEMEXFU23FCd9F7cswrsaAoFvlLsbQV7eNwfKr0AogreHgbhnPbTkhBR5zAxGGPxY49dfNCpk4tZa2EBjeTemGInMKxPiJPtIKxH3uswOnPXS6k2mtMZu5qS4t9v0itKaw0LBtBrx7K9yT79tLxICBB9o4mTmtg3jE";
	public static final String ZICB_CREATE_WALLET_SERVICE_CODE = "ZB0631";
	public static final String ZICB_ADD_WALLET_SERVICE_CODE = "BNK9907";
	
	public static final String ZICB_OTP_WALLET_SERVICE_CODE = "ZB0637";
	//public static final String ZICB_SERVICE_KEY = "aedd3681c6ec02d5f5c33f2effa3b683";
	public static final String ZICB_DEBIT_WALLET_SERVICE_CODE = "ZB0641";
	public static final String ZICB_OTP_WALLET_SERVICE_VERIFY_CODE = "ZB0638";
	public static final String ZICB_QUERY_BY_MOBILE_NUMBER = "ZB0640";
	public static final String ZICB_CREDIT_WALLET_SERVICE_CODE = "ZB0628";
	public static final String ZICB_FT_WALLET_SERVICE_CODE = "ZB0609";
	public static final String ZICB_CHECK_CUSTOMER_EXIST_BY_IDNO_SERVICE_CODE = "BNK9909";
	public static final String ZICB_CHECK_CUSTOMER_EXIST_BY_MOBILE_SERVICE_CODE = "BNK9910";

	private static final String ZICB_FT_INTERNAL_FUNDS_TRANSFER_CODE = "BNK9930";
	public static final String ZICB_BANK_BRANCH_LIST = "BNK9901";
	public static final String ZICB_FT_EXTERNAL_FUNDS_TRANSFER_CODE = "BNK9900";
	
	
	
	
	
	public static void checkConstraintsForNewMerchant(String merchantCode, String addressLine1, 
			String bankAccount, String certificateOfIncorporation, 
			String companyData, String companyLogo, String companyName, 
			String companyRegNo, String contactEmail, String contactMobile, 
			String merchantBank, String merchantName, String merchantScheme)
	{
		try{
			JSONObject returnErrors = new JSONObject();
			
			if(merchantCode!=null && merchantCode.length()==0){returnErrors.put("merchantCode", "Merchant Code must be provided");}
			if(addressLine1!=null && addressLine1.length()==0){returnErrors.put("addressLine1", "First line of address must be provided");}
			if(bankAccount!=null && bankAccount.length()==0){returnErrors.put("bankAccount", "Valid bank account for the merchant must be provided");}
			if(certificateOfIncorporation!=null && certificateOfIncorporation.length()==0){returnErrors.put("certificateOfIncorporation", "Certificate of Incorporation must be provided");
			}
			if(merchantCode!=null && merchantCode.length()==0){
				returnErrors.put("merchantCode", "Merchant Code must be provided");
			}
			if(addressLine1!=null && addressLine1.length()==0){
				returnErrors.put("addressLine1", "First line of address must be provided");
			}
			if(bankAccount!=null && bankAccount.length()==0){
				returnErrors.put("bankAccount", "Valid bank account for the merchant must be provided");
			}
			if(certificateOfIncorporation!=null && certificateOfIncorporation.length()==0){
				returnErrors.put("certificateOfIncorporation", "Certificate of Incorporation must be provided");
			}
		}catch(JSONException e)
		{
			log.info(e.getMessage());
		}
	}

	public static String getLoggedInBankBranchAndBankCode(SwpService swpService, String token, 
			String currencyCode, 
			String productScheme, String uniqueCode, Long customerId) {
		// TODO Auto-generated method stub
		String hql = "Select tp from Account tp where tp.customer.id = " + customerId;
		Collection<Account> customerAccounts;
		try {
			customerAccounts = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
			return "035" + "001" + currencyCode + productScheme + uniqueCode + customerAccounts.size();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
			return null;
		}
		
	}

	public static String generateAccountNo(AccountType accountType,
			Account account) {
		// TODO Auto-generated method stub
		log.info("Account No => " + accountType.ordinal() + account.getAccountIdentifier() + account.getAccountCount());
		return accountType.ordinal() + account.getAccountIdentifier() + account.getAccountCount();
	}
	
	public static int addTwoDigitNum(int x)
	{
		String xString = Integer.toString(x);
		char[] xStringArray = xString.toCharArray();
		int sum = xString.length()>2 ? (Integer.parseInt(""+xStringArray[0]) + Integer.parseInt(""+xStringArray[1])) : x;
		return 2;
	}

	public static String generatePan(String countryCode, String bankCode, String branchCode,  String accountNo) {
		// TODO Auto-generated method stub
		//						01 234567 8
		//pan = 0|260|10|01|  - 01|001000|0|X
		char[] acc = accountNo.toCharArray();
		int x8th = Integer.parseInt("" + acc[8]) * 2;
		int x7th = Integer.parseInt("" + acc[7]);
		int x6th = Integer.parseInt("" + acc[6]) * 2;
		int x5th = Integer.parseInt("" + acc[7]);
		int x4th = Integer.parseInt("" + acc[4]) * 2;
		int x3th = Integer.parseInt("" + acc[3]);
		int x2th = Integer.parseInt("" + acc[2]) * 2;
		int x1th = Integer.parseInt("" + acc[1]);
		int x0th = Integer.parseInt("" + acc[0]) * 2;
		int sumXth = (x8th<10 ? x8th : addTwoDigitNum(x8th)) + x7th +
				(x8th<10 ? x8th : addTwoDigitNum(x6th)) +  x5th +
				(x8th<10 ? x8th : addTwoDigitNum(x4th)) +  x3th +
				(x8th<10 ? x8th : addTwoDigitNum(x2th)) +  x1th +
				(x8th<10 ? x8th : addTwoDigitNum(x0th));
		int mod = sumXth%10;
		return "0" + countryCode + bankCode + branchCode + "" + accountNo + "" + (10 - (mod==0 ? 10 : mod));
	}
	
	public static String decryptDataNew(byte[] keyValue, String ivValue, String encryptedData, String macValue) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, DecoderException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		Key key = new SecretKeySpec(keyValue, "AES");
        byte[] iv = org.apache.commons.codec.binary.Base64.decodeBase64(ivValue.getBytes("UTF-8"));
        byte[] decodedValue = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedData.getBytes("UTF-8"));

        SecretKeySpec macKey = new SecretKeySpec(keyValue, "HmacSHA256");
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        hmacSha256.init(macKey);
        hmacSha256.update(ivValue.getBytes("UTF-8"));
        byte[] calcMac = hmacSha256.doFinal(encryptedData.getBytes("UTF-8"));
        byte[] mac = Hex.decodeHex(macValue.toCharArray());
        if (!Arrays.equals(calcMac, mac))
            return "MAC mismatch";

        Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding"); // or PKCS5Padding
        c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decValue = c.doFinal(decodedValue);

        int firstQuoteIndex = 0;
        while (decValue[firstQuoteIndex] != (byte) '"') firstQuoteIndex++;
        return new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length - 2));
	}
	
	public static Object decryptData(String encryptedStr, String bankKey)
	{
		try{
			log.info("encryptedStr = " + encryptedStr);
			byte[] decode = org.apache.commons.codec.binary.Base64.decodeBase64(encryptedStr.getBytes("UTF-8"));
			String decodeStr = new String(decode, "UTF-8");
			log.info("Decoded = " + decodeStr);
			JSONObject jsonObject = new JSONObject(decodeStr);
			String iv1 = jsonObject.getString("iv");
			String value = jsonObject.getString("value");
			String mac = jsonObject.getString("mac");
			byte[] keyValue = bankKey.getBytes("UTF-8");
			/*String dec = decryptDataNew(keyValue, iv1, encryptedStr, mac);
			log.info("dec = " + dec);
			return dec;
		}catch(Exception e)
		 {
			 log.info(e.getMessage());
			 return null;
		 }*/
			log.info("value = " + value);

			log.info("bankKey = " + bankKey);
			Key key = new SecretKeySpec(keyValue, "AES");
			byte[] iv = org.apache.commons.codec.binary.Base64.decodeBase64(iv1.getBytes("UTF-8"));
			byte[] decodedValue = org.apache.commons.codec.binary.Base64.decodeBase64(value.getBytes("UTF-8"));
			
			
			log.info(key.getAlgorithm());
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding"); // or PKCS5Padding
			log.info(1);
			c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
			log.info(2);
			byte[] decValue = c.doFinal(decodedValue);
			log.info(3);
			log.info(new String(decValue));
			
			int firstQuoteIndex = 0;
			byte b;
			log.info(decValue[0]);
			if(decValue[0] == 105 || decValue[0] == 100 ){
				b = ((byte)';');
			}
			else{
				b = ((byte)'"');
			}
			
			log.info("length>--" + decValue.length);
			log.info("" + decValue[0]);
			
			for(int j1=0; j1<decValue.length; j1++)
				log.info("j1[" + j1 + "] == " + decValue[j1]);
			
			log.info("" + new String(decValue, "UTF-8"));
			while(firstQuoteIndex<decValue.length && decValue[firstQuoteIndex] != b){
				log.info("firstQuoteIndex..." + firstQuoteIndex);
				firstQuoteIndex++;
			}
			String vl = "";
			
			
			if(decValue[0] == 105 || decValue[0] == 100){
				vl = new String(Arrays.copyOfRange(decValue, 2, decValue.length - 1), "UTF-8");
				log.info("vl..." + vl);
				if(decValue[0] == 105)
				{
					return Integer.valueOf(vl);
				}else if(decValue[0] == 100)
				{
					return Long.valueOf(vl);
				}
			}else{
				//vl = new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length-2), "UTF-8");
				log.info("vl1..." + vl);
				vl = new String(Arrays.copyOfRange(decValue, firstQuoteIndex + 1, decValue.length-2), "UTF-8");
				log.info("vl1..." + vl);
			}
			return (vl);
		 }catch(Exception e)
		 {
			 log.info(e.getMessage());
			 log.info(e.getLocalizedMessage());
			 e.printStackTrace();
			 return null;
		 }
	}
	
	public AlgorithmParameterSpec getIV(Cipher cipher) {
		AlgorithmParameterSpec ivspec;
		byte[] iv = new byte[cipher.getBlockSize()];
		new SecureRandom().nextBytes(iv);
		ivspec = new IvParameterSpec(iv);
		return ivspec;
	}
	
	
	
	public static String bcryptData(String data)
	{
		return BCrypt.hashpw(data, BCrypt.gensalt());
	}
	
	
	public static String encryptData(Object toencrypt, String bankKey) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, JSONException
	{
		String strtoencrypt = "s:" + toencrypt.toString().length() + ":\"" + toencrypt.toString() + "\";";
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    SecretKeySpec key = new SecretKeySpec(bankKey.getBytes("UTF-8"), "AES");
	    byte[] iv = RandomStringUtils.randomAlphanumeric(16).getBytes("UTF-8");
	    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
	    byte[] df = cipher.doFinal(strtoencrypt.getBytes("UTF-8"));
	    byte[] val = org.apache.commons.codec.binary.Base64.encodeBase64(df);
	    byte[] iv1 = org.apache.commons.codec.binary.Base64.encodeBase64(iv);
	    byte[] dest = new byte[iv1.length + val.length];
	    System.arraycopy(iv, 0, dest, 0, iv.length);
	    System.arraycopy(val, 0, dest, iv1.length, val.length);
	    //cipher.
	    key = new SecretKeySpec(bankKey.getBytes("UTF-8"), "HmacSHA256");
	    Mac mac = Mac.getInstance("HmacSHA256");
	    mac.init(key);
	    mac.update(iv1);
	    mac.update(val);
	    
	    
	    //Arrays.
	    byte[] mc = (mac.doFinal());
	    JSONObject  js = new JSONObject();
	    js.put("iv", new String(iv1, "UTF-8"));
	    js.put("value", new String(val, "UTF-8"));
	    js.put("mac", new String(mc, "UTF-8"));
	    String encStr = new String(org.apache.commons.codec.binary.Base64.encodeBase64(js.toString().getBytes()), "UTF-8");
	    return (encStr);
	}

	public static String getBankKey(String bankCode, SwpService swpService) throws JSONException {
		// TODO Auto-generated method stub
		Application application = Application.getInstance(swpService);
		JSONObject bankKeys = application.getAccessKeys();
		log.info("bankKeys ==> " + bankKeys.toString());
		
		
		if(bankCode==null)
			bankCode = "PROBASE";
		
		return bankKeys.getString(bankCode);
	}

	public static JSONObject verifyToken(String jwtoken, Application app) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try{
			log.info("-------------------------");
			log.info("jwtone =" + jwtoken);
			Claims claims = Jwts.parser()         
					   .setSigningKey(DatatypeConverter.parseBase64Binary(app.getTokenKey()))
					   .parseClaimsJws(jwtoken).getBody();
			Date expDate = claims.getExpiration();
			String subject = claims.getSubject();
			String acquirerCode = claims.getIssuer();
			String branchCode = claims.getAudience();
			Date now = new Date(System.currentTimeMillis());
			
			log.info("exp Date ==" + expDate);
			log.info("Now Date ==" + now);
			int active = 1;
			long ttl = 0;
			if(now.after(expDate))
				active = 0;
			else
				ttl = claims.getExpiration().getTime() - now.getTime();
			
			String tkId = RandomStringUtils.randomAlphanumeric(10);
			
			log.info("active ==" + active);
			log.info("new tkId ==" + tkId);
			log.info("remove tkId ==" + claims.getId());
			log.info("-------------------------");
			
			jsonObject.put("active", active);
			jsonObject.put("branchCode", branchCode);
			jsonObject.put("acquirerCode", acquirerCode);
			jsonObject.put("subject", subject);
			log.info("acquirerCode ==" + acquirerCode);
			if(active==1)
				jsonObject.put("token", app.createJWT(tkId, acquirerCode, branchCode, subject, (ttl)));
			
			log.info("verifyJSON Response = " + jsonObject.toString());
			
			return jsonObject;
			
		}catch(ExpiredJwtException e)
		{
			log.info(e.getMessage());
			log.error(">>>", e);
			return new JSONObject();
		}catch(Exception e)
		{
			log.info(e.getMessage());
			log.error(">>>", e);
			return new JSONObject();
		}
	}
	
	
	
	public static JSONObject verifyTokenGetUser(String jwtoken, Application app, SwpService swpService) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try{
			log.info("-------------------------");
			log.info("jwtone =" + jwtoken);
			Claims claims = Jwts.parser()         
					   .setSigningKey(DatatypeConverter.parseBase64Binary(app.getTokenKey()))
					   .parseClaimsJws(jwtoken).getBody();
			Date expDate = claims.getExpiration();
			String subject = claims.getSubject();
			Gson gsonConverter = new Gson();
			User user = gsonConverter.fromJson(subject, User.class);
			String hql = "Select tp from User tp where tp.id = " + user.getId();
			user = (User)swpService.getUniqueRecordByHQL(hql);
			subject = gsonConverter.toJson(user);
			String issuerBankCode = claims.getIssuer();
			String branchCode = claims.getAudience();
			Date now = new Date(System.currentTimeMillis());
			
			log.info("exp Date ==" + expDate);
			log.info("Now Date ==" + now);
			int active = 1;
			long ttl = 0;
			if(now.after(expDate))
				active = 0;
			else
				ttl = claims.getExpiration().getTime() - now.getTime();
			
			String tkId = RandomStringUtils.randomAlphanumeric(10);
			
			log.info("active ==" + active);
			log.info("new tkId ==" + tkId);
			log.info("remove tkId ==" + claims.getId());
			log.info("-------------------------");
			
			jsonObject.put("active", active);
			jsonObject.put("issuerBankCode", issuerBankCode);
			jsonObject.put("branchCode", branchCode);
			jsonObject.put("staff_bank_code", issuerBankCode);
			jsonObject.put("subject", subject);
			log.info("staff_bank_code ==" + issuerBankCode);
			if(active==1)
				jsonObject.put("token", app.createJWT(tkId, issuerBankCode, branchCode, subject, (ttl)));
			
			return jsonObject;
			
		}catch(ExpiredJwtException e)
		{
			log.info(e.getMessage());
			return new JSONObject();
		}catch(Exception e)
		{
			log.info(e.getMessage());
			return new JSONObject();
		}
	}
	
	
	public static JSONObject verifyGuestToken(String jwtoken, Application app) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try{
			log.info("-------------------------");
			log.info("jwtone =" + jwtoken);
			Claims claims = Jwts.parser()         
					   .setSigningKey(DatatypeConverter.parseBase64Binary(app.getTokenKey()))
					   .parseClaimsJws(jwtoken).getBody();
			Date expDate = claims.getExpiration();
			String subject = claims.getSubject();
			Date now = new Date(System.currentTimeMillis());
			
			log.info("exp Date ==" + expDate);
			log.info("Now Date ==" + now);
			int active = 1;
			long ttl = 0;
			if(now.after(expDate))
				active = 0;
			else
				ttl = claims.getExpiration().getTime() - now.getTime();
			
			String tkId = RandomStringUtils.randomAlphanumeric(10);
			
			log.info("active ==" + active);
			log.info("new tkId ==" + tkId);
			log.info("remove tkId ==" + claims.getId());
			log.info("-------------------------");
			
			jsonObject.put("active", active);
			if(active==1)
				jsonObject.put("token", app.createGuestJWT(tkId, subject, (ttl)));
			
			return jsonObject;
			
		}catch(ExpiredJwtException e)
		{
			log.info(e.getMessage());
			return new JSONObject();
		}catch(Exception e)
		{
			log.info(e.getMessage());
			return new JSONObject();
		}
	}

	public static boolean validateTransactionHash(String hash, String merchantId, String deviceCode,
			String serviceTypeId, String orderId, Double amount, String responseUrl, String api_key) {
		// TODO Auto-generated method stub
		DecimalFormat df = new DecimalFormat("0.00");
		String amt = df.format(amount);
		amount = Double.valueOf(amt);
		//FROM COOP	-	M9UDPDBYNW43664232541981598182741SHMWHPWXI4.00http://smartcoops.com/confirm-payment13FQMuMzxZCf57kG9h1lOA1RKuQUhPew
		//FROM PROB - 	M9UDPDBYNW43664232541981598182741SHMWHPWXI4.00http://smartcoops.com/confirm-payment13FQMuMzxZCf57kG9h1lOA1RKuQUhPew
		String toHash = merchantId+deviceCode+serviceTypeId+orderId+amt+responseUrl+api_key;
		log.info("To HAsh = " + merchantId+"-"+deviceCode+"-"+serviceTypeId+"-"+orderId+"-"+amt+"-"+responseUrl+"-"+api_key);
		try {
			String hashed = UtilityHelper.get_SHA_512_SecurePassword(toHash);
			log.info("1.hash = " + hash);
			log.info("2.hash = " + hashed);
			if(hashed.equals(hash))
			{
				return true;
			}
			else
			{
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
			return false;
		}
	}

	public static boolean validateTransactionHash2(String hash, String merchantId, String deviceCode,
			String serviceTypeId, String orderId, String responseUrl, String api_key) {
		// TODO Auto-generated method stub
		//FROM COOP	-	M9UDPDBYNW43664232541981598182741SHMWHPWXI4.00http://smartcoops.com/confirm-payment13FQMuMzxZCf57kG9h1lOA1RKuQUhPew
		//FROM PROB - 	M9UDPDBYNW43664232541981598182741SHMWHPWXI4.00http://smartcoops.com/confirm-payment13FQMuMzxZCf57kG9h1lOA1RKuQUhPew
		String toHash = merchantId+deviceCode+serviceTypeId+orderId+responseUrl+api_key;
		log.info("To HAsh = " + merchantId+"-"+deviceCode+"-"+serviceTypeId+"-"+orderId+"-"+responseUrl+"-"+api_key);
		try {
			String hashed = UtilityHelper.get_SHA_512_SecurePassword(toHash);
			log.info("1.hash = " + hash);
			log.info("2.hash = " + hashed);
			if(hashed.equals(hash))
			{
				return true;
			}
			else
			{
				return false;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.info(e.getMessage());
			return false;
		}
	}
	
	public static String get_SHA_512_SecurePassword(String passwordToHash) throws UnsupportedEncodingException{
		String generatedPassword = null;
		    try {
		         MessageDigest md = MessageDigest.getInstance("SHA-512");
		         byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
		         StringBuilder sb = new StringBuilder();
		         for(int i=0; i< bytes.length ;i++){
		            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		         }
		         generatedPassword = sb.toString();
		        } 
		       catch (NoSuchAlgorithmException e){
		        log.info(e.getMessage());
		       }
		    return generatedPassword;
		}

	public static JSONObject validateForCyberSource(String billingFirstName,
			String billingLastName, String billingPhone, String billingEmail,
			String billingStreetAddress, String billingCity,
			String billingDistrict, String merchantId, String deviceCode,
			String serviceTypeId, String orderId, String hash,
			String payerName, Double amount, String responseurl,
			String api_key, SwpService swpService, Application app) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			if(merchantId!=null && deviceCode!=null)
				if(serviceTypeId!=null)
					if(orderId!= null)
						if(hash!=null)
							if(validateTransactionHash(hash, merchantId, deviceCode, serviceTypeId, orderId, amount, responseurl, api_key))
								if(amount!=null && amount>0 && amount > app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
									if(responseurl!=null)
										if(api_key!=null && api_key.length()>0)
											return null;
										else
											jsonObject.put(ERROR.API_KEY_FAIL + "", "Merchant API key failed");//apiKey
									else
										jsonObject.put(ERROR.RESPONSE_URL_NOT_PROVIDED + "", "Response URL not provided");//responseUrl
								else
									jsonObject.put(ERROR.TRANSACTION_AMOUNT_INVALID + "", "Transaction Amount is Invalid.");//amount < 0  & minimumtxn
							else
								jsonObject.put(ERROR.HASH_FAIL_VALIDATION + "", "HASH Validation Failed");//hash validation failed
						else
							jsonObject.put(ERROR.HASH_NOT_PROVIDED + "", "HASH Not Provided");//hash not provided
					else
						jsonObject.put(ERROR.ORDER_ID_NOT_PROVIDED + "", "Order ID Not Provided");//orderId
				else
					jsonObject.put(ERROR.SERVICE_TYPE_NOT_PROVIDED + "", "Service Type Not Provided");//serviceType Not provided
			else
				jsonObject.put(ERROR.MERCHANT_DEVICE_FAIL + "", "Merchant and Device Codes provided are invalid");//merchantId, deviceCode not provided
			
		}catch(Exception e)
		{
			log.info(e.getMessage());
		}
		return jsonObject;
	}
	
	
	
	public static JSONObject validateForCyberSourceV2(String merchantId, String deviceCode,
			String serviceTypeId, String orderId, String hash,
			Double amount, String responseurl,
			String api_key, SwpService swpService, Application app) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			if(merchantId!=null && deviceCode!=null)
				if(serviceTypeId!=null)
					if(orderId!= null)
						if(hash!=null)
							if(validateTransactionHash(hash, merchantId, deviceCode, serviceTypeId, orderId, amount, responseurl, api_key))
								if(amount!=null && amount>0 && amount > app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
									if(responseurl!=null)
										if(api_key!=null && api_key.length()>0)
											return null;
										else
											jsonObject.put(ERROR.API_KEY_FAIL + "", "Merchant API key failed");//apiKey
									else
										jsonObject.put(ERROR.RESPONSE_URL_NOT_PROVIDED + "", "Response URL not provided");//responseUrl
								else
									jsonObject.put(ERROR.TRANSACTION_AMOUNT_INVALID + "", "Transaction Amount is Invalid.");//amount < 0  & minimumtxn
							else
								jsonObject.put(ERROR.HASH_FAIL_VALIDATION + "", "HASH Validation Failed");//hash validation failed
						else
							jsonObject.put(ERROR.HASH_NOT_PROVIDED + "", "HASH Not Provided");//hash not provided
					else
						jsonObject.put(ERROR.ORDER_ID_NOT_PROVIDED + "", "Order ID Not Provided");//orderId
				else
					jsonObject.put(ERROR.SERVICE_TYPE_NOT_PROVIDED + "", "Service Type Not Provided");//serviceType Not provided
			else
				jsonObject.put(ERROR.MERCHANT_DEVICE_FAIL + "", "Merchant and Device Codes provided are invalid");//merchantId, deviceCode not provided
			
		}catch(Exception e)
		{
			log.info(e.getMessage());
		}
		return jsonObject;
	}
	
	
	
	
	public static String hash_hmac(String dataToEncrypt, String secretKey)
	{
		String signature = null;
		try
		{
	        byte[] decodedKey = Hex.decodeHex(secretKey.toCharArray());
	        SecretKeySpec keySpec = new SecretKeySpec(decodedKey, "HmacSHA256");
	        Mac mac = Mac.getInstance("HmacSHA256");
	        mac.init(keySpec);
	        byte[] dataBytes = dataToEncrypt.getBytes("UTF-8");
	        byte[] signatureBytes = mac.doFinal(dataBytes);
	        signature = new String(org.apache.commons.codec.binary.Base64.encodeBase64(signatureBytes), "UTF-8");
	        log.info("key = " + secretKey);
	        log.info("data = " + dataToEncrypt);
	        log.info("signature = " + signature);
		}catch(Exception e)
		{
			log.info(e.getMessage());
		}
		return signature;
	}
	
	
	
	public static String sendGet(String baseUrl, String parameters, JSONObject jsObj) throws Exception {

		String url = baseUrl + (parameters!=null ? ("?" + parameters) : "");
		log.info("url ==" + url);
		//url="";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		if(jsObj!=null && jsObj.length()>0)
		{
			Iterator<String> iter = jsObj.keys();
			while(iter.hasNext())
			{
				String key = iter.next();
				con.setRequestProperty(key, jsObj.getString(key));
			}
		}

		int responseCode = con.getResponseCode();
		log.info("\nSending 'GET' request to URL : " + url);
		log.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		log.info(response.toString());
		return response.toString();

	}
	
	public static String sendPost(String baseUrl, String parameters, JSONObject jsObj) {
		
		try
		{

			String url = "https://selfsolve.apple.com/wcResults.do";
			url = baseUrl;
			log.info("112");
			log.info("url = " + url);
			log.info("parameters = " + parameters);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
	
			log.info("113");
			//add reuqest header
			con.setRequestMethod("POST");
			log.info("114");
			con.setRequestProperty("User-Agent", USER_AGENT);
			log.info("115");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			log.info("116");
			if(jsObj!=null && jsObj.length()>0)
			{
				Iterator<String> iter = jsObj.keys();
				while(iter.hasNext())
				{
					String key = iter.next();
					con.setRequestProperty(key, jsObj.getString(key));
					log.info("key == " + key + " && val = " + jsObj.getString(key));
				}
			}
			log.info("117");
	
			//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
			String urlParameters = parameters;
			log.info("118");
	
			// Send post request
			con.setDoOutput(true);
			log.info("119");
			
			log.info("\nSending 'POST' request to URL : " + url);
			log.info("Post parameters : " + urlParameters);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			log.info("121");
			if(urlParameters!=null)
				wr.writeBytes(urlParameters);
			
			log.info("112");
			
			wr.flush();
			wr.close();
			log.info("113");
	
			int responseCode = con.getResponseCode();
			log.info("Response Code : " + responseCode);
	
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
				
	
				//print result
				log.info(response.toString());
				return response.toString();
			}
			else
			{
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getErrorStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
	
				//print result
				log.info(response.toString());
				return null;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			e.printStackTrace();
			return "";
		}

	}
	
	
	
	public static String sendHttpsPost(String baseUrl, String parameters, JSONObject jsObj) {
		
		try
		{

			String url = "https://selfsolve.apple.com/wcResults.do";
			url = baseUrl;
			log.info("112");
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setHostnameVerifier(UtilityHelper.getHostnameVerifier());
			
	
			log.info("113");
			//add reuqest header
			con.setRequestMethod("POST");
			log.info("114");
			con.setRequestProperty("User-Agent", USER_AGENT);
			log.info("115");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			log.info("116");
			if(jsObj!=null && jsObj.length()>0)
			{
				Iterator<String> iter = jsObj.keys();
				while(iter.hasNext())
				{
					String key = iter.next();
					con.setRequestProperty(key, jsObj.getString(key));
					log.info("key == " + key + " && val = " + jsObj.getString(key));
				}
			}
			log.info("117");
	
			//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
			String urlParameters = parameters;
			log.info("118");
	
			// Send post request
			con.setDoOutput(true);
			log.info("119");
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			log.info("121");
			if(urlParameters!=null)
				wr.writeBytes(urlParameters);
			
			log.info("112");
			
			wr.flush();
			wr.close();
			log.info("113");
	
			int responseCode = con.getResponseCode();
			
			log.info("\nSending 'POST' request to URL : " + url);
			log.info("Post parameters : " + urlParameters);
			log.info("Response Code : " + responseCode);
	
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
				
	
				//print result
				log.info(response.toString());
				return response.toString();
			}
			else
			{
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getErrorStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
	
				//print result
				log.info(response.toString());
				return null;
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			return "";
		}

	}
	
	public static String[] generateZICBWalletOTP(JSONObject jsonObject, String mobileno, String email, Acquirer acquirer, Device device, SwpService swpService) throws Exception {
		// TODO Auto-generated method stub
		try
		{
			//email  = "smicer66@gmail.com";
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_OTP_WALLET_SERVICE_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("emailid", email);
			parametersRequest.put("mobileno", mobileno);
			//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
			//	parametersRequest.put("serviceKey", acquirer.getServiceKey());
			//else if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.FALSE))
			//	c
			
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
				parametersRequest.put("serviceKey",  device.getZicbServiceKey());
			else
				parametersRequest.put("serviceKey", device.getZicbDemoServiceKey());
			
			parameters.put("request", parametersRequest);
			
			log.info("parameters...." + parameters.toString());
			JSONObject header = new JSONObject();
			
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				header.put("authKey", device.getZicbAuthKey());
			else//else if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.FALSE))
				header.put("authKey", device.getZicbDemoAuthKey());
			
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			
			log.info(header.toString());
			
			String newWalletResponse = null;
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
			{
				log.info(acquirer.getFundsTransferEndPoint());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
				log.info(newWalletResponse);
			}
			else//else if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.FALSE))
			{
				log.info(acquirer.getFundsTransferDemoEndPoint());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
				log.info(newWalletResponse);
			}
			
			
			
			String[] resp = new String[4];
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "OTP could not be generated for your transaction.");
				resp[0] = "0";
				resp[1] = jsonObject.toString();
				resp[2] = null;
				resp[3] = null;
				return resp;
			}
			JSONObject walletResponse = new JSONObject(newWalletResponse);
			if(walletResponse.has("response"))
			{
				JSONObject response = walletResponse.getJSONObject("response");
				Integer status = walletResponse.getInt("status");
				if(status!=200)
				{
					jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
					jsonObject.put("message", "An OTP could not be sent for your transaction. Please try again.");
					resp[0] = "0";
					resp[1] = jsonObject.toString();
					resp[2] = null;
					resp[3] = null;
					return resp;
				}
			}
			
			
			if(walletResponse.has("tekHeader"))
			{
				JSONObject tekHeader = walletResponse.getJSONObject("tekHeader");
				String headerStatus = tekHeader.getString("status");
				
				if(headerStatus!=null && headerStatus.toLowerCase().equals("success"))
				{
					log.info("Inside success");
					String otp = walletResponse.getString("otp");
					String otpref = tekHeader.getString("hostrefno");
					resp[0] = "1";
					resp[1] = null;
					resp[2] = otp;
					resp[3] = otpref;
					
					

					SimpleDateFormat sdf = new SimpleDateFormat("yy-MM");
					String receipentMobileNumber = mobileno;
					String smsMessage = "Hello\nYour One-Time Password is " + otp + ". Please enter this OTP to complete your transaction or process.";
					SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
					swpService.createNewRecord(smsMsg);
					
					
					SmsSender smsSender = new SmsSender(swpService, smsMessage, receipentMobileNumber);
					new Thread(smsSender).start();
					
					return resp;
				}
				else
				{
					log.info("Inside not success");
					jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
					jsonObject.put("message", "An OTP could not be sent for your transaction. Please try again.");
					resp[0] = "0";
					resp[1] = jsonObject.toString();
					resp[2] = null;
					resp[3] = null;
					return resp;
				}
			}
			
			log.info("walletResponse has no response");
			jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
			jsonObject.put("message", "An OTP could not be sent for your transaction. Please try again.");
			resp[0] = "0";
			resp[1] = jsonObject.toString();
			resp[2] = null;
			resp[3] = null;
			return resp;
			
		}
		catch (JSONException e) {
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}
	
	private static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {

			@Override
			public boolean verify(String hosdtname, SSLSession session) {
				// TODO Auto-generated method stub
				HostnameVerifier hv =
                        HttpsURLConnection.getDefaultHostnameVerifier();
                return true;
			}
        };
        return hostnameVerifier;
    }
	
	
	public static String[] verifyZICBWalletOTP(JSONObject jsonObject, String mobileno, String email, String otpref, String otp, String zicbAuthKey, String endpoint) {
		// TODO Auto-generated method stub
		try
		{
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_OTP_WALLET_SERVICE_VERIFY_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("emailid", email);
			parametersRequest.put("mobileno", mobileno);
			parametersRequest.put("otpref", otpref);
			parametersRequest.put("otp", otp);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("authKey", zicbAuthKey);
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			log.info(header.toString());
			
			log.info(endpoint);
			String newWalletResponse = UtilityHelper.sendPost(endpoint, parameters.toString(), header);
			log.info(newWalletResponse);
			
			String[] resp = new String[2];
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your OTP could not be verified on this transaction.");
				resp[0] = "0";
				resp[1] = jsonObject.toString();
				return resp;
			}
			JSONObject walletResponse = new JSONObject(newWalletResponse);
			if(walletResponse.has("response"))
			{
				JSONObject response = walletResponse.getJSONObject("response");
				Integer status = walletResponse.getInt("status");
				if(status!=200)
				{
					jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
					jsonObject.put("message", "An OTP could not be validated for your transaction. Please try again.");
					resp[0] = "0";
					resp[1] = jsonObject.toString();
					return resp;
				}
			}
			
			if(walletResponse.has("tekHeader"))
			{
				JSONObject tekHeader = walletResponse.getJSONObject("tekHeader");
				String headerStatus = tekHeader.getString("status");
				
				if(headerStatus!=null && headerStatus.toLowerCase().equals("success"))
				{
					log.info("Inside success");
					resp[0] = "1";
					resp[1] = null;
					return resp;
				}
				else
				{
					log.info("Inside not success");
					jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
					jsonObject.put("message", "An OTP could not be validated for your transaction. Please try again.");
					resp[0] = "0";
					resp[1] = jsonObject.toString();
					return resp;
				}
			}
			log.info("walletResponse has no response");
			jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
			jsonObject.put("message", "An OTP could not be sent for your transaction. Please try again.");
			resp[0] = "0";
			resp[1] = jsonObject.toString();
			return resp;
		}
		catch (JSONException e) {
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	
	
	public static JSONObject internalFundsTransfer(Application app, SwpService swpService, HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Double amount, Account sourceAccount, Account destinationAccount1, String token, String merchantCode, String deviceCode, 
			User user, String channel, String orderRef, String remarks, Acquirer acquirer, Device device ) {
		// TODO Auto-generated method stub
		try
		{
			if(acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				Double fixedCharge = sourceAccount.getAccountScheme().getOverrideFixedFee();
				Double transactionCharge = sourceAccount.getAccountScheme().getOverrideTransactionFee();
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				
				
				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge;
				JSONObject jsonObject = new JSONObject();
				Customer customer = sourceAccount.getCustomer();
				jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				jsonObject.put("customerId", customer.getId());
				jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
				jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
				jsonObject.put("charges", charges);
				jsonObject.put("amount", amount);
				jsonObject.put("bankReference", RandomStringUtils.randomAlphanumeric(16).toUpperCase());
				jsonObject.put("message", "Funds transfer was successful");
				
				sourceAccount.setAccountBalance(sourceAccount.getAccountBalance() - amount - charges);
				swpService.updateRecord(sourceAccount);
				
				destinationAccount1.setAccountBalance(destinationAccount1.getAccountBalance() + amount);
				swpService.updateRecord(destinationAccount1);
				
				
				
				
				//FOR CHARGES
				if(charges>0)
				{
					String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
					String bankPaymentReference = null;
					Long customerId = sourceAccount.getCustomer().getId();
					String rpin = null;
					Date transactionDate = new Date();
					ServiceType serviceType = ServiceType.SERVICE_CHARGE;
					String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
					String payerEmail = sourceAccount.getCustomer().getContactEmail();
					String payerMobile = sourceAccount.getCustomer().getContactMobile();
					TransactionStatus transactionStatus = TransactionStatus.SUCCESS;
					ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
					String transactionCode = TransactionCode.transactionSuccess;
					Boolean creditPoolAccountTrue = false;
					String messageRequest = null;
					String messageResponse =  null;
					String responseCode = "00";
					Double schemeTransactionPercentage = 0.00;
					String merchantName = device.getMerchant().getMerchantName();
					String merchantBank = null;
					String merchantAccount = null;
					Long transactingBankId = acquirer.getBank().getId();
					Long receipientTransactingBankId = acquirer.getBank().getId();
					Integer accessCode = null;
					Long sourceEntityId = sourceAccount.getId();
					Long receipientEntityId = destinationAccount1.getId();
					Channel chanel = Channel.valueOf(channel);
					Channel receipientChannel = Channel.valueOf(channel);
					String narration = "FUNDSTRANSFERCHG~" + amount + "~" + amount + "~" +sourceAccount.getAccountIdentifier()  + "~" + destinationAccount1.getAccountIdentifier() + "~" + transactionRef;
					String transactionDetail = "FUNDS TXFR CHG - " + charges;
					Double closingBalance = null;
					Double totalCreditSum = null;
					Double totalDebitSum = null;
					Long paidInByBankUserAccountId = null;
					String customData = null;
					String responseData = null;
					Long adjustedTransactionId = null;
					Long acquirerId = acquirer.getId();
					Boolean creditAccountTrue=  true;
					Boolean creditCardTrue = false;
					Boolean debitAccountTrue = true;
					Boolean debitCardTrue = false;
					Long creditAccountId = destinationAccount1.getId();
					Long creditCardId = null;
					Long debitAccountId = sourceAccount.getId();
					Long debitCardId = null;
					
					Merchant merchant = device.getMerchant();
					Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
							customerId, creditAccountTrue, creditCardTrue,
							orderRef, rpin, chanel,
							transactionDate, serviceType, payerName,
							payerEmail, payerMobile, transactionStatus,
							probasePayCurrency, transactionCode,
							sourceAccount, null, device,
							creditPoolAccountTrue, messageRequest,
							messageResponse, fixedCharge,
							transactionCharge, transactionPercentage,
							schemeTransactionCharge, schemeTransactionPercentage,
							amount, responseCode, null, null,
							merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
							null, null, 
							transactingBankId, receipientTransactingBankId,
							accessCode, sourceEntityId, receipientEntityId,
							receipientChannel, transactionDetail,
							closingBalance, totalCreditSum, totalDebitSum,
							paidInByBankUserAccountId, customData,
							responseData, adjustedTransactionId, acquirerId, 
							debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Money Transfer Charge", device.getSwitchToLive());
					transaction = (Transaction)swpService.createNewRecord(transaction);
					
					

					JSONObject allSettings = app.getAllSettings();
					String chargeAccountNumber = allSettings.getString("chargeaccountnumber");
					String hql = "Select tp from Account tp where tp.accountIdentifier = '"+ chargeAccountNumber +"' AND tp.isLive = " + device.getSwitchToLive();
					Account chargeAccount = (Account)swpService.getUniqueRecordByHQL(hql);
					
					sourceAccount.setAccountBalance(sourceAccount.getAccountBalance() - charges);
					swpService.updateRecord(sourceAccount);
					
					chargeAccount.setAccountBalance(chargeAccount.getAccountBalance() + charges);
					swpService.updateRecord(chargeAccount);
					
					
					jsonObject.put("chargeTransactionRef", transaction.getTransactionRef());
					jsonObject.put("chargeOrderRef", transaction.getOrderRef());
				}
				
				
				return jsonObject;
			}
			else if(acquirer.getHoldFundsYes().equals(Boolean.TRUE))
			{
				AccountServicesV2 asv = new AccountServicesV2();
				JSONObject jsonObject = new JSONObject();
				Long defaultaccountschemeId = Long.parseLong(app.getAllSettings().getString("defaultaccountscheme"));
				//Long defaultaccountschemeId = (Long)(defaultaccountscheme);
				CardScheme accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultaccountschemeId);
				
				Double fixedCharge = accountScheme.getOverrideFixedFee();
				Double transactionCharge = accountScheme.getOverrideTransactionFee();
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				
				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge;
				Double chargeableAmount = amount;
						
				jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
				jsonObject.put("message", "Funds transfer was not successful");		
						
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				Date pDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject parameters = new JSONObject();
				parameters.put("service", UtilityHelper.ZICB_FT_INTERNAL_FUNDS_TRANSFER_CODE);
				JSONObject parametersRequest = new JSONObject();
				parametersRequest.put("amount", amount);
				parametersRequest.put("destAcc", destinationAccount1.getAccountIdentifier());
				parametersRequest.put("destBranch", destinationAccount1.getBranchCode());
				parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
				parametersRequest.put("payDate", sdf.format(pDate));
				parametersRequest.put("referenceNo", orderRef);
				parametersRequest.put("remarks", remarks);
				parametersRequest.put("srcAcc", sourceAccount.getAccountIdentifier());
				parametersRequest.put("srcBranch", sourceAccount.getBranchCode());
				parametersRequest.put("srcCurrency", sourceAccount.getProbasePayCurrency().name());
				parametersRequest.put("transferRef", orderRef);
				parametersRequest.put("transferTyp", "INTERNAL");
				
				
				parameters.put("request", parametersRequest);
				JSONObject header = new JSONObject();
				header.put("Content-Type", "application/json; utf-8");
				header.put("Accept", "application/json");
				log.info(parameters.toString());
				log.info(header.toString());
				log.info("Test");
				String newWalletResponse = null;
				if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					header.put("authKey", device.getZicbAuthKey());
					log.info(device.getZicbAuthKey()); 
					log.info("Live Auth Key..." + device.getZicbAuthKey());
					newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
					
					//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
				}
				else
				{
					header.put("authKey", device.getZicbDemoAuthKey());
					log.info(device.getZicbDemoAuthKey());
					log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
					newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
				}
				log.info(newWalletResponse);
				
				if(newWalletResponse==null)
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				
				JSONObject walletResponse = null;
				try
				{
					walletResponse = new JSONObject(newWalletResponse);
				}
				catch(Exception e)
				{
					log.info("Test");
					e.printStackTrace();
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				JSONObject response = walletResponse.getJSONObject("response");
				Integer status = walletResponse.getInt("status");
				if(status!=200)
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				JSONObject responseHeader = response.getJSONObject("tekHeader");
				log.info(response.toString());
				
				String operationStatus = responseHeader.getString("status");
				if(operationStatus.equals("FAIL"))
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
					Iterator<String> keys = msgList.keys();
					String errorMessage = null;
					while(keys.hasNext())
					{
						String ky = keys.next();
						errorMessage = msgList.getString(ky);
					}
					jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
				}
				else if(operationStatus.equals("SUCCESS"))
				{
					String debitBankPaymentReference = responseHeader.getString("tekesbrefno");
					
					Customer customer = sourceAccount.getCustomer();
					jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
					jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
					jsonObject.put("charges", charges);
					jsonObject.put("amount", amount);
					jsonObject.put("bankReference", debitBankPaymentReference);
					jsonObject.put("message", "Funds transfer was successful");
					
					
					//FOR CHARGES
					if(charges>0)
					{
						JSONObject allSettings = app.getAllSettings();
						String chargeAccountNumber = allSettings.getString("chargeaccountnumber");
						String hql = "Select tp from Account tp where tp.accountIdentifier = '"+ chargeAccountNumber +"' AND tp.isLive = " + device.getSwitchToLive();
						Account chargeAccount = (Account)swpService.getUniqueRecordByHQL(hql);
						
						
						String transactionRef_ = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
						String chargeBankPaymentReference = null;
						Long customerId = sourceAccount.getCustomer().getId();
						String rpin = null;
						Date transactionDate = new Date();
						ServiceType serviceType = ServiceType.SERVICE_CHARGE;
						String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
						String payerEmail = sourceAccount.getCustomer().getContactEmail();
						String payerMobile = sourceAccount.getCustomer().getContactMobile();
						TransactionStatus transactionStatus = TransactionStatus.PENDING;
						ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
						String transactionCode = TransactionCode.transactionPending;
						Boolean creditPoolAccountTrue = false;
						String messageRequest = null;
						String messageResponse =  null;
						String responseCode = "01";
						Double schemeTransactionPercentage = 0.00;
						String merchantName = device.getMerchant().getMerchantName();
						String merchantBank = null;
						String merchantAccount = null;
						Long transactingBankId = acquirer.getBank().getId();
						Long receipientTransactingBankId = acquirer.getBank().getId();
						Integer accessCode = null;
						Long sourceEntityId = sourceAccount.getId();
						Long receipientEntityId = chargeAccount.getId();
						Channel chanel = Channel.valueOf(channel);
						Channel receipientChannel = Channel.valueOf(channel);
						String narration = "FUNDSTRANSFERCHG~" + charges + "~" + charges + "~" +sourceAccount.getAccountIdentifier()  + "~" + chargeAccount.getAccountIdentifier() + "~" + transactionRef;
						String transactionDetail = "FUNDS TXFR CHG - " + charges;
						Double closingBalance = null;
						Double totalCreditSum = null;
						Double totalDebitSum = null;
						Long paidInByBankUserAccountId = null;
						String customData = null;
						String responseData = null;
						Long adjustedTransactionId = null;
						Long acquirerId = acquirer.getId();
						Boolean creditAccountTrue=  true;
						Boolean creditCardTrue = false;
						Boolean debitAccountTrue = true;
						Boolean debitCardTrue = false;
						Long creditAccountId = chargeAccount.getId();
						Long creditCardId = null;
						Long debitAccountId = sourceAccount.getId();
						Long debitCardId = null;
						
						Merchant merchant = device.getMerchant();
						Transaction transaction = new Transaction(transactionRef_, chargeBankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, chanel,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, transactionStatus,
								probasePayCurrency, transactionCode,
								sourceAccount, null, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								charges, responseCode, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
								null, null, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Money Transfer Charge", device.getSwitchToLive());
						transaction = (Transaction)swpService.createNewRecord(transaction);
						
						
						
						
						
						//DEBIT SOURCE CREDIT CHARGES ACCOUNT
						pDate = new Date();
						sdf = new SimpleDateFormat("yyyy-MM-dd");
						parameters = new JSONObject();
						parameters.put("service", UtilityHelper.ZICB_FT_INTERNAL_FUNDS_TRANSFER_CODE);
						parametersRequest = new JSONObject();
						parametersRequest.put("amount", charges);
						parametersRequest.put("destAcc", chargeAccount.getAccountIdentifier());
						parametersRequest.put("destBranch", chargeAccount.getBranchCode());
						parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
						parametersRequest.put("payDate", sdf.format(pDate));
						parametersRequest.put("referenceNo", orderRef);
						parametersRequest.put("remarks", remarks);
						parametersRequest.put("srcAcc", sourceAccount.getAccountIdentifier());
						parametersRequest.put("srcBranch", sourceAccount.getBranchCode());
						parametersRequest.put("srcCurrency", sourceAccount.getProbasePayCurrency().name());
						parametersRequest.put("transferRef", orderRef);
						parametersRequest.put("transferTyp", "INTERNAL");
						
						
						parameters.put("request", parametersRequest);
						header = new JSONObject();
						header.put("Content-Type", "application/json; utf-8");
						header.put("Accept", "application/json");
						log.info(parameters.toString());
						log.info(header.toString());
						log.info("Test");
						newWalletResponse = null;
						if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
						{
							header.put("authKey", device.getZicbAuthKey());
							log.info(device.getZicbAuthKey()); 
							log.info("Live Auth Key..." + device.getZicbAuthKey());
							newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
							
							//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
						}
						else
						{
							header.put("authKey", device.getZicbDemoAuthKey());
							log.info(device.getZicbDemoAuthKey());
							log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
							newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
						}
						log.info(newWalletResponse);
						
						if(newWalletResponse==null)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
								
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						
						walletResponse = null;
						try
						{
							walletResponse = new JSONObject(newWalletResponse);
						}
						catch(Exception e)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
							
							
							log.info("Test");
							e.printStackTrace();
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						response = walletResponse.getJSONObject("response");
						status = walletResponse.getInt("status");
						if(status!=200)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
								
								
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						responseHeader = response.getJSONObject("tekHeader");
						log.info(response.toString());
						
						operationStatus = responseHeader.getString("status");
						if(operationStatus.equals("FAIL"))
						{
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
							Iterator<String> keys = msgList.keys();
							String errorMessage = null;
							while(keys.hasNext())
							{
								String ky = keys.next();
								errorMessage = msgList.getString(ky);
							}
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							
							
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
						}
						else if(operationStatus.equals("SUCCESS"))
						{

							chargeBankPaymentReference = responseHeader.getString("tekesbrefno");
							jsonObject.put("chargeTransactionRef", transaction.getTransactionRef());
							jsonObject.put("chargeOrderRef", transaction.getOrderRef());
							jsonObject.put("chargeBankPaymentReference", chargeBankPaymentReference);
						}
					}
					
					
					
					
					
					
				
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
				}
				
				return jsonObject;
			}
			else
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
				jsonObject.put("message", "Funds transfer was not successful");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	

	
	public static JSONObject externalFundsTransfer(Application app, SwpService swpService, HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Double amount, Account sourceAccount, String destinationAccountNumber, String token, String merchantCode, String deviceCode, 
			User user, String channel, String orderRef, String remarks, Acquirer acquirer, Device device, String destinationBankName, String destinationBranchSortCode, 
			String destinationBranchName, String ipAddress) {
		// TODO Auto-generated method stub
		try
		{
			if(acquirer.getHoldFundsYes().equals(Boolean.FALSE))
			{
				Double fixedCharge = sourceAccount.getAccountScheme().getOverrideFixedFee();
				Double transactionCharge = sourceAccount.getAccountScheme().getOverrideTransactionFee();
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				
				
				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge;
				JSONObject jsonObject = new JSONObject();
				Customer customer = sourceAccount.getCustomer();
				jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
				jsonObject.put("customerNumber", customer.getVerificationNumber());
				jsonObject.put("customerId", customer.getId());
				jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
				jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
				jsonObject.put("charges", charges);
				jsonObject.put("amount", amount);
				jsonObject.put("bankReference", RandomStringUtils.randomAlphanumeric(16).toUpperCase());
				jsonObject.put("message", "Funds transfer was successful");
				
				
				
				
				//FOR CHARGES
				if(charges>0)
				{
					String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
					String bankPaymentReference = null;
					Long customerId = sourceAccount.getCustomer().getId();
					String rpin = null;
					Date transactionDate = new Date();
					ServiceType serviceType = ServiceType.SERVICE_CHARGE;
					String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
					String payerEmail = sourceAccount.getCustomer().getContactEmail();
					String payerMobile = sourceAccount.getCustomer().getContactMobile();
					TransactionStatus transactionStatus = TransactionStatus.SUCCESS;
					ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
					String transactionCode = TransactionCode.transactionSuccess;
					Boolean creditPoolAccountTrue = false;
					String messageRequest = null;
					String messageResponse =  null;
					String responseCode = "00";
					Double schemeTransactionPercentage = 0.00;
					String merchantName = device.getMerchant().getMerchantName();
					String merchantBank = null;
					String merchantAccount = null;
					Long transactingBankId = acquirer.getBank().getId();
					Long receipientTransactingBankId = acquirer.getBank().getId();
					Integer accessCode = null;
					Long sourceEntityId = sourceAccount.getId();
					Long receipientEntityId = null;
					Channel chanel = Channel.valueOf(channel);
					Channel receipientChannel = Channel.valueOf(channel);
					String narration = "FUNDSTRANSFER~" + amount + "~" + amount + "~" +sourceAccount.getAccountIdentifier()  + "~" + destinationAccountNumber + "~" + transactionRef;
					String transactionDetail = "FUNDS TXFR CHG - " + charges;
					Double closingBalance = null;
					Double totalCreditSum = null;
					Double totalDebitSum = null;
					Long paidInByBankUserAccountId = null;
					String customData = null;
					String responseData = null;
					Long adjustedTransactionId = null;
					Long acquirerId = acquirer.getId();
					Boolean creditAccountTrue=  true;
					Boolean creditCardTrue = false;
					Boolean debitAccountTrue = true;
					Boolean debitCardTrue = false;
					Long creditAccountId = null;
					Long creditCardId = null;
					Long debitAccountId = sourceAccount.getId();
					Long debitCardId = null;
					
					Merchant merchant = device.getMerchant();
					Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
							customerId, creditAccountTrue, creditCardTrue,
							orderRef, rpin, chanel,
							transactionDate, serviceType, payerName,
							payerEmail, payerMobile, transactionStatus,
							probasePayCurrency, transactionCode,
							sourceAccount, null, device,
							creditPoolAccountTrue, messageRequest,
							messageResponse, fixedCharge,
							transactionCharge, transactionPercentage,
							schemeTransactionCharge, schemeTransactionPercentage,
							amount, responseCode, null, null,
							merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
							null, null, 
							transactingBankId, receipientTransactingBankId,
							accessCode, sourceEntityId, receipientEntityId,
							receipientChannel, transactionDetail,
							closingBalance, totalCreditSum, totalDebitSum,
							paidInByBankUserAccountId, customData,
							responseData, adjustedTransactionId, acquirerId, 
							debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Money Transfer Charge", 
							device.getSwitchToLive());
					transaction = (Transaction)swpService.createNewRecord(transaction);
					
					

					JSONObject allSettings = app.getAllSettings();
					String chargeAccountNumber = allSettings.getString("chargeaccountnumber");
					String hql = "Select tp from Account tp where tp.accountIdentifier = '"+ chargeAccountNumber +"' AND tp.isLive = " + device.getSwitchToLive();
					Account chargeAccount = (Account)swpService.getUniqueRecordByHQL(hql);
					
					sourceAccount.setAccountBalance(sourceAccount.getAccountBalance() - charges);
					swpService.updateRecord(sourceAccount);
					
					chargeAccount.setAccountBalance(chargeAccount.getAccountBalance() + charges);
					swpService.updateRecord(chargeAccount);
					
					
					jsonObject.put("chargeTransactionRef", transaction.getTransactionRef());
					jsonObject.put("chargeOrderRef", transaction.getOrderRef());
				}
				
				
				return jsonObject;
			}
			else if(acquirer.getHoldFundsYes().equals(Boolean.TRUE))
			{
				AccountServicesV2 asv = new AccountServicesV2();
				JSONObject jsonObject = new JSONObject();
				Long defaultaccountschemeId = Long.parseLong(app.getAllSettings().getString("defaultaccountscheme"));
				//Long defaultaccountschemeId = (Long)(defaultaccountscheme);
				CardScheme accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultaccountschemeId);
				
				Double fixedCharge = accountScheme.getOverrideFixedFee();
				Double transactionCharge = accountScheme.getOverrideTransactionFee();
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				
				Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge;
				Double chargeableAmount = amount;
						
				jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
				jsonObject.put("message", "Funds transfer was not successful");		
				
				
				
				
				
				
						
				Customer customer =  sourceAccount.getCustomer();
				String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
				Date pDate = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				JSONObject parameters = new JSONObject();
				parameters.put("service", UtilityHelper.ZICB_FT_EXTERNAL_FUNDS_TRANSFER_CODE);
				JSONObject parametersRequest = new JSONObject();
				parametersRequest.put("userName", "DEMO");
				parametersRequest.put("customerId", customer.getContactMobile());
				parametersRequest.put("ipAddress", ipAddress);
				parametersRequest.put("srcAcc", sourceAccount.getAccountIdentifier());
				parametersRequest.put("destAcc", destinationAccountNumber);
				parametersRequest.put("amount", amount);
				parametersRequest.put("destCurrency", sourceAccount.getProbasePayCurrency().name());
				parametersRequest.put("srcCurrency", sourceAccount.getProbasePayCurrency().name());
				parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
				parametersRequest.put("transferTyp", "RTGS");
				parametersRequest.put("destBranch", destinationBranchName);
				parametersRequest.put("srcBranch", sourceAccount.getBranchCode());
				parametersRequest.put("bankName", destinationBankName);
				parametersRequest.put("sortCode", destinationBranchSortCode);
				parametersRequest.put("remarks", "RTGS Transfer");
				parametersRequest.put("beneName", "RTGS");
				parametersRequest.put("payDate", sdf.format(new Date()));
				parametersRequest.put("senderName", customer.getFirstName() + " " + customer.getLastName());
				parametersRequest.put("senderEmail", customer.getContactEmail()!=null ? customer.getContactEmail() : null);
				parametersRequest.put("sendermobileno", customer.getContactMobile()!=null ? customer.getContactMobile().substring(customer.getContactMobile().length() - 3) : null);
				parametersRequest.put("beneEmail", "");
				parametersRequest.put("beneMobileNo", "");
				parametersRequest.put("senderAddress1", customer.getAddressLine1());
				parametersRequest.put("senderAddress2", customer.getAddressLine2());
				parametersRequest.put("senderAddress3", customer.getLocationDistrict().getName());
				
				
				parameters.put("request", parametersRequest);
				JSONObject header = new JSONObject();
				header.put("Content-Type", "application/json; utf-8");
				header.put("Accept", "application/json");
				log.info(parameters.toString());
				log.info(header.toString());
				log.info("Test");
				String newWalletResponse = null;
				if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
				{
					header.put("authKey", device.getZicbAuthKey());
					log.info(device.getZicbAuthKey()); 
					log.info("Live Auth Key..." + device.getZicbAuthKey());
					newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
					
					//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
				}
				else
				{
					header.put("authKey", device.getZicbDemoAuthKey());
					log.info(device.getZicbDemoAuthKey());
					log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
					newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
				}
				log.info(newWalletResponse);
				
				if(newWalletResponse==null)
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				
				JSONObject walletResponse = null;
				try
				{
					walletResponse = new JSONObject(newWalletResponse);
				}
				catch(Exception e)
				{
					log.info("Test");
					e.printStackTrace();
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				JSONObject response = walletResponse.getJSONObject("response");
				Integer status = walletResponse.getInt("status");
				if(status!=200)
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
					return jsonObject;
				}
				JSONObject responseHeader = response.getJSONObject("tekHeader");
				log.info(response.toString());
				
				String operationStatus = responseHeader.getString("status");
				if(operationStatus.equals("FAIL"))
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
					Iterator<String> keys = msgList.keys();
					String errorMessage = null;
					while(keys.hasNext())
					{
						String ky = keys.next();
						errorMessage = msgList.getString(ky);
					}
					jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
				}
				else if(operationStatus.equals("SUCCESS"))
				{
					String debitBankPaymentReference = responseHeader.getString("tekesbrefno");
					
					customer = sourceAccount.getCustomer();
					jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
					jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
					jsonObject.put("charges", charges);
					jsonObject.put("amount", amount);
					jsonObject.put("bankReference", debitBankPaymentReference);
					jsonObject.put("message", "Funds transfer was successful");
					
					
					//FOR CHARGES
					if(charges>0)
					{
						JSONObject allSettings = app.getAllSettings();
						String chargeAccountNumber = allSettings.getString("chargeaccountnumber");
						String hql = "Select tp from Account tp where tp.accountIdentifier = '"+ chargeAccountNumber +"' AND tp.isLive = " + device.getSwitchToLive();
						Account chargeAccount = (Account)swpService.getUniqueRecordByHQL(hql);
						
						
						String transactionRef_ = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
						String chargeBankPaymentReference = null;
						Long customerId = sourceAccount.getCustomer().getId();
						String rpin = null;
						Date transactionDate = new Date();
						ServiceType serviceType = ServiceType.SERVICE_CHARGE;
						String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
						String payerEmail = sourceAccount.getCustomer().getContactEmail();
						String payerMobile = sourceAccount.getCustomer().getContactMobile();
						TransactionStatus transactionStatus = TransactionStatus.PENDING;
						ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
						String transactionCode = TransactionCode.transactionPending;
						Boolean creditPoolAccountTrue = false;
						String messageRequest = null;
						String messageResponse =  null;
						String responseCode = "01";
						Double schemeTransactionPercentage = 0.00;
						String merchantName = device.getMerchant().getMerchantName();
						String merchantBank = null;
						String merchantAccount = null;
						Long transactingBankId = acquirer.getBank().getId();
						Long receipientTransactingBankId = acquirer.getBank().getId();
						Integer accessCode = null;
						Long sourceEntityId = sourceAccount.getId();
						Long receipientEntityId = chargeAccount.getId();
						Channel chanel = Channel.valueOf(channel);
						Channel receipientChannel = Channel.valueOf(channel);
						String narration = "FUNDSTRANSFER~" + charges + "~" + charges + "~" +sourceAccount.getAccountIdentifier()  + "~" + chargeAccount.getAccountIdentifier() + "~" + transactionRef;
						String transactionDetail = "FUNDS TXFR CHG - " + charges;
						Double closingBalance = null;
						Double totalCreditSum = null;
						Double totalDebitSum = null;
						Long paidInByBankUserAccountId = null;
						String customData = null;
						String responseData = null;
						Long adjustedTransactionId = null;
						Long acquirerId = acquirer.getId();
						Boolean creditAccountTrue=  true;
						Boolean creditCardTrue = false;
						Boolean debitAccountTrue = true;
						Boolean debitCardTrue = false;
						Long creditAccountId = chargeAccount.getId();
						Long creditCardId = null;
						Long debitAccountId = sourceAccount.getId();
						Long debitCardId = null;
						
						Merchant merchant = device.getMerchant();
						Transaction transaction = new Transaction(transactionRef_, chargeBankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, chanel,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, transactionStatus,
								probasePayCurrency, transactionCode,
								sourceAccount, null, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								charges, responseCode, null, null,
								merchant.getId(), merchant.getMerchantName(), merchant.getMerchantCode(),
								null, null, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Money Transfer Charge", device.getSwitchToLive());
						transaction = (Transaction)swpService.createNewRecord(transaction);
						
						
						
						
						
						//DEBIT SOURCE CREDIT CHARGES ACCOUNT
						pDate = new Date();
						sdf = new SimpleDateFormat("yyyy-MM-dd");
						parameters = new JSONObject();
						parameters.put("service", UtilityHelper.ZICB_FT_INTERNAL_FUNDS_TRANSFER_CODE);
						parametersRequest = new JSONObject();
						parametersRequest.put("amount", charges);
						parametersRequest.put("destAcc", chargeAccount.getAccountIdentifier());
						parametersRequest.put("destBranch", chargeAccount.getBranchCode());
						parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
						parametersRequest.put("payDate", sdf.format(pDate));
						parametersRequest.put("referenceNo", orderRef);
						parametersRequest.put("remarks", remarks);
						parametersRequest.put("srcAcc", sourceAccount.getAccountIdentifier());
						parametersRequest.put("srcBranch", sourceAccount.getBranchCode());
						parametersRequest.put("srcCurrency", sourceAccount.getProbasePayCurrency().name());
						parametersRequest.put("transferRef", orderRef);
						parametersRequest.put("transferTyp", "INTERNAL");
						
						
						parameters.put("request", parametersRequest);
						header = new JSONObject();
						header.put("Content-Type", "application/json; utf-8");
						header.put("Accept", "application/json");
						log.info(parameters.toString());
						log.info(header.toString());
						log.info("Test");
						newWalletResponse = null;
						if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
						{
							header.put("authKey", device.getZicbAuthKey());
							log.info(device.getZicbAuthKey()); 
							log.info("Live Auth Key..." + device.getZicbAuthKey());
							newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
							
							//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
						}
						else
						{
							header.put("authKey", device.getZicbDemoAuthKey());
							log.info(device.getZicbDemoAuthKey());
							log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
							newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
						}
						log.info(newWalletResponse);
						
						if(newWalletResponse==null)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
								
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						
						walletResponse = null;
						try
						{
							walletResponse = new JSONObject(newWalletResponse);
						}
						catch(Exception e)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
							
							
							log.info("Test");
							e.printStackTrace();
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						response = walletResponse.getJSONObject("response");
						status = walletResponse.getInt("status");
						if(status!=200)
						{
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
								
								
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							return jsonObject;
						}
						responseHeader = response.getJSONObject("tekHeader");
						log.info(response.toString());
						
						operationStatus = responseHeader.getString("status");
						if(operationStatus.equals("FAIL"))
						{
							jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
							JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
							Iterator<String> keys = msgList.keys();
							String errorMessage = null;
							while(keys.hasNext())
							{
								String ky = keys.next();
								errorMessage = msgList.getString(ky);
							}
							jsonObject.put("message", "Funds transfer was not successful. A reversal on the debit is being processed");
							
							
							RequestTransactionReversal requestTransactionReversal = new RequestTransactionReversal(
								RandomStringUtils.randomAlphanumeric(16).toUpperCase(), orderRef, debitBankPaymentReference, transaction.getAmount(), 
								"REVERSE TXN " + debitBankPaymentReference, transaction.getMerchantId(), transaction.getMerchantName(), transaction.getMerchantCode(), 
								transaction.getDevice().getDeviceCode(), TransactionStatus.PENDING, transaction, 
								sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName(), 
								null, device.getSwitchToLive()
							);
							requestTransactionReversal = (RequestTransactionReversal)swpService.createNewRecord(requestTransactionReversal);
						}
						else if(operationStatus.equals("SUCCESS"))
						{

							chargeBankPaymentReference = responseHeader.getString("tekesbrefno");
							jsonObject.put("chargeTransactionRef", transaction.getTransactionRef());
							jsonObject.put("chargeOrderRef", transaction.getOrderRef());
							jsonObject.put("chargeBankPaymentReference", chargeBankPaymentReference);
						}
					}
					
					
					
					
					
					
				
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
					jsonObject.put("message", "Funds transfer was not successful");
				}
				
				return jsonObject;
			}
			else
			{
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
				jsonObject.put("message", "Funds transfer was not successful");
				return jsonObject;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	public static void sendSMSMessage(String receipient, String smsMessage) {
		// TODO Auto-generated method stub
		
	}

	public static String getValue(String val) {
		// TODO Auto-generated method stub
		String val1 = "";
		try
		{
			val1 = (val==null ? "" : val);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("error", e);
			val1 = "";
		}
		return val1;
	}

	public static String formatPan(String pan) {
		// TODO Auto-generated method stub
		if(pan==null)
			return "";
		int len = pan.length();
		String pan1 = pan.substring(0, 4);
		String pan2 = pan.substring(len-4);
		String pad = "";
		len = len - 8;
		while(len>0)
		{
			pad = pad + "*";
			len = len - 1;
		}
		
		return (pan1 + pad + pan2);
	}

	public static String getDateOfBirth(Date date) {
		// TODO Auto-generated method stub
		String val1 = "";
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
		try
		{
			if(date!=null)
				val1 = df.format(date);
			else
				val1 = "";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error("error", e);
			val1 = "";
		}
		return val1;
	}
	
	
	
	public static JSONObject createZICBWallet(SwpService swpService, 
			String logId, String token, String merchantCode, String deviceCode, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Acquirer acquirer, Long parentCustomerId, Long parentAccountId, 
			String customerVerificationNumber, District district, Integer isSettlementAccount, Integer isTokenize)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
			jsonObject.put("message", "System error. We experienced some issues. Please give it a try again");
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			log.info("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			//String branch_code = verifyJ.getString("branchCode");
			//log.info("branch_code ==" + branch_code);
			
			Device device = null;
			Merchant merchant = null;
			int isLive = 0;
			if(merchantCode!=null && deviceCode!=null)
			{
				String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
				
				if(merchant==null)
				{
					jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Merchant Code Invalid");
					return jsonObject;
				}
				
	
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				device = (Device)swpService.getUniqueRecordByHQL(hql);
				
				if(device==null)
				{
					jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "Device code Invalid");
					return jsonObject;
				}
				
				if(device!=null && device.getSwitchToLive().equals(1))
				{
					isLive = 1;
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide valid information");
				return jsonObject;
			}
			
			
			String hql = "Select tp.* from accounts tp, customers c where tp.customer_id = c.id AND c.contactMobile = '"+mobileNumber+"' AND "
					+ "tp.acquirer_id = " + acquirer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
			if(isSettlementAccount!=null && isSettlementAccount==1)
				hql = hql + " AND tp.accountType = " + AccountType.DEVICE_SETTLEMENT.ordinal();
			else
				hql = hql + " AND tp.accountType = " + AccountType.VIRTUAL.ordinal();
			
			
			List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			if(accounts!=null && accounts.size()>0)
			{
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a wallet in " + acquirer.getBank().getBankName());
				return jsonObject;
			}
			
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			if(parentCustomerId!=null && parentAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = "+parentCustomerId+" AND tp.deleted_at IS NULL";
				log.info(hql);
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Account tp where tp.id = "+parentAccountId+" AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
				log.info(hql);
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			
			
			Date dob = null;
			
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_CREATE_WALLET_SERVICE_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("firstName", firstName);
			parametersRequest.put("lastName", lastName);
			parametersRequest.put("add1", addressLine1);
			parametersRequest.put("add2", addressLine2);
			parametersRequest.put("add3", addressLine3);
			parametersRequest.put("add4", addressLine4);
			parametersRequest.put("add5", addressLine5);
			parametersRequest.put("uniqueType", uniqueType);
			parametersRequest.put("uniqueValue", uniqueValue);
			if(dateOfBirth!=null)
			{
				dob = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
				parametersRequest.put("dateOfBirth", dateOfBirth);
			}
			
			parametersRequest.put("email", email);
			parametersRequest.put("sex", sex);
			parametersRequest.put("mobileNumber", mobileNumber);
			parametersRequest.put("accType", accType);
			parametersRequest.put("currency", currency);
			parametersRequest.put("idFront", idFront);
			parametersRequest.put("idBack", idBack);
			parametersRequest.put("custImg", custImg);
			parametersRequest.put("custSig", custSig);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			log.info(parameters.toString());
			log.info(header.toString());
			log.info("Test");
			log.info(acquirer.getAuthKey());
			String newWalletResponse = null;
			//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
			{
				//header.put("authKey", acquirer.getAuthKey());
				header.put("authKey", device.getZicbAuthKey());
				log.info(acquirer.getAccountCreationEndPoint()); 
				log.info("Live Auth Key..." + device.getZicbAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationEndPoint(), parameters.toString(), header);
				
				//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				//header.put("authKey", acquirer.getDemoAuthKey());
				header.put("authKey", device.getZicbDemoAuthKey());
				log.info(acquirer.getAccountCreationDemoEndPoint());
				log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationDemoEndPoint(), parameters.toString(), header);
			}
			log.info(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
			}
			catch(Exception e)
			{
				log.info("Test");
				e.printStackTrace();
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject response = walletResponse.getJSONObject("response");
			Integer status = walletResponse.getInt("status");
			if(status!=200)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject responseHeader = response.getJSONObject("tekHeader");
			log.info(response.toString());
			if(response.get("cust")==null)
				log.info("nullable cust");
			else
				log.info("non nullable cust");
			String operationStatus = responseHeader.getString("status");
			if(operationStatus.equals("FAIL"))
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
				Iterator<String> keys = msgList.keys();
				String errorMessage = null;
				while(keys.hasNext())
				{
					String ky = keys.next();
					errorMessage = msgList.getString(ky);
				}
				jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
			}
			else if(operationStatus.equals("SUCCESS"))
			{
				JSONObject customer_ = response.get("cust")!=null ? response.getJSONObject("cust") : null;
				if(customer_!=null)
				{
					JSONObject accNos = customer_.getJSONObject("accNos");
					Iterator<String> accNo = accNos.keys();
					String accNo_ = accNo.next();
					log.info("accNo..." + accNo_);
					JSONObject acctDetails = accNos.getJSONObject(accNo_);
		
					
					
					/*Customer customer = new Customer();
					customer.setAddressLine1(addressLine1);
					customer.setAddressLine2(addressLine2);
					customer.setContactEmail(email);
					customer.setAltContactMobile(null);
					customer.setContactMobile(mobileNumber);
					customer.setDateOfBirth(dob);
					customer.setFirstName(firstName);
					customer.setGender(sex==null ? null : Gender.valueOf(sex=="M" ? "MALE" : "FEMALE"));
					customer.setLastName(lastName);
					customer.setVerificationNumber(acctDetails.getInt("idCustomer") + "");
					customer.setStatus(CustomerStatus.ACTIVE);
					customer.setCreated_at(new Date());
					customer.setCustomerType(CustomerType.INDIVIDUAL);
					customer.setCustomerImage(custImg);
					customer.setLocationDistrict(district);
					customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(uniqueType));
					customer.setMeansOfIdentificationNumber(uniqueValue);
					customer = (Customer)swpService.createNewRecord(customer);*/
					hql = "Select tp from Customer tp where tp.verificationNumber = '"+customerVerificationNumber+"' AND tp.deleted_at IS NULL";
					log.info(hql);
					Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
					
					jsonObject.put("customerNumber", customer.getVerificationNumber());

					
					hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.isLive = " + device.getSwitchToLive();
					Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
					Account account = null;
					String branch_code = acctDetails.getString("branchCode");
					if(merchantCode!=null && deviceCode!=null)
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.DEVICE_SETTLEMENT, accNo_, 
								customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, ProbasePayCurrency.valueOf(currency), 
								null, null, accountScheme, isLive);
					}
					else
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.VIRTUAL, accNo_, 
							customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, 
							ProbasePayCurrency.valueOf(currency), null, null, accountScheme, isLive);
					}
					account = (Account)swpService.createNewRecord(account);
					String accountToken = null;
					log.info("Entering create Bevura Token");
					if(isTokenize!=null && isTokenize==1)
					{
						accountToken = UtilityHelper.tokenizeAccount(account);
						log.info("Account Token...." + accountToken);
						if(accountToken!=null)
						{
							BevuraToken bevuraToken = new BevuraToken(accountToken, account.getId(), null, merchant!=null ? merchant.getId() : null, 
									device!=null ? device.getId() : null, "ACCOUNT", account.getCustomer().getId(), 1, null, null, device.getSwitchToLive());
							bevuraToken = (BevuraToken)swpService.createNewRecord(bevuraToken);
							jsonObject.put("accountToken", bevuraToken.getToken());
						}
					}
					jsonObject.put("accountNo", accNo_);
					
					
					Transaction transaction = new Transaction();
					transaction.setChannel(Channel.WEB);
					transaction.setCreated_at(new Date());
					transaction.setAmount(0.00);
					transaction.setCustomerId(customer.getId());
					transaction.setFixedCharge(null);
					transaction.setMessageRequest(null);
					transaction.setServiceType(ServiceType.DEPOSIT_OTC);
					transaction.setStatus(TransactionStatus.SUCCESS);
					transaction.setPayerEmail(email);
					transaction.setPayerMobile(mobileNumber);
					transaction.setPayerName(lastName + ", " + firstName);
					transaction.setResponseCode(TransactionCode.transactionSuccess);
					transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
					transaction.setTransactionDate(new Date());
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setAccount(account);
					transaction.setCreditAccountTrue(true);
					transaction.setCreditPoolAccountTrue(true);
					transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
					transaction.setReceipientChannel(Channel.WEB);
					transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
					transaction.setReceipientEntityId(account.getId());
					transaction.setDevice(device);
					transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
					if(merchant!=null)
						transaction.setMerchantId(merchant.getId());
					
					transaction.setIsLive(device.getSwitchToLive());
					transaction = (Transaction)swpService.createNewRecord(transaction);
					jsonObject.put("transactionId", transaction.getId());
					
					hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
					List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
					jsonObject.put("walletExists", allaccounts!=null && accounts.size()>0 ? true : false);
					

					hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
							+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + device.getSwitchToLive();
					List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("ecards", ecards==null ? null : ecards);
					jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
				
					jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountId", account.getId());
					jsonObject.put("accountIdentifier", accNo_);
					if(isTokenize!=null && isTokenize==1)
					{
						jsonObject.put("accountToken", accountToken);
					}
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Wallet Created Successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
					jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	
	private static int[] parse(String cardId) {
        char[] cardIdArr = cardId.toCharArray();
        int[] result = new int[cardIdArr.length];

        for (int i = 0; i < cardIdArr.length; i++) {
            result[i] = Character.getNumericValue(cardIdArr[i]);
        }

        return result;
    }
	
	private static boolean validFirst(String number) {
        return number.matches("^[3-6].*$");
    }
	
	public static String tokenizeAccount(Account account) {
		// TODO Auto-generated method stub
		String accountId = account.getAccountIdentifier();
		String token = "A" + RandomStringUtils.randomAlphanumeric(128); 
		token = token + account.getId();
		token = token.toUpperCase();
        return token;
	}
	
	public static String tokenizeCard(ECard card) {
		// TODO Auto-generated method stub
		String token = "C" + RandomStringUtils.randomAlphanumeric(128); 
		token = token + card.getId();
		token = token.toUpperCase();
        return token;
	}

	public static JSONObject createDeviceRestrictedZICBWallet(SwpService swpService, 
			String logId, User userOwner, Merchant merchant, Device device, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Acquirer acquirer, Long parentCustomerId, Long parentAccountId, 
			String customerVerificationNumber, District district)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
			jsonObject.put("message", "System error. We experienced some issues. Please give it a try again");
			Application app = Application.getInstance(swpService);
			
			//String branch_code = verifyJ.getString("branchCode");
			//log.info("branch_code ==" + branch_code);
			
			String hql = "Select c.* from accounts tp, customers c where tp.customer_id = c.id AND c.contactMobile = '"+mobileNumber+"' AND tp.acquirer_id = " + acquirer.getId() + " AND tp.deleted_at IS NULL";
			hql = hql + " AND tp.accountType = " + AccountType.VIRTUAL.ordinal();
			
			
			List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			if(accounts!=null && accounts.size()>0)
			{
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a wallet in " + acquirer.getBank().getBankName());
				jsonObject.put("customerNumber", (String)(accounts.iterator().next().get("verificationNumber")));
				return jsonObject;
			}
			
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			if(parentCustomerId!=null && parentAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = "+parentCustomerId+" AND tp.deleted_at IS NULL";
				log.info(hql);
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Account tp where tp.id = "+parentAccountId+" AND tp.deleted_at IS NULL";
				log.info(hql);
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			Date dob = null;
			
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_CREATE_WALLET_SERVICE_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("firstName", firstName);
			parametersRequest.put("lastName", lastName);
			parametersRequest.put("add1", addressLine1);
			parametersRequest.put("add2", addressLine2);
			parametersRequest.put("add3", addressLine3);
			parametersRequest.put("add4", addressLine4);
			parametersRequest.put("add5", addressLine5);
			parametersRequest.put("uniqueType", uniqueType);
			parametersRequest.put("uniqueValue", uniqueValue);
			if(dateOfBirth!=null)
			{
				dob = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
				parametersRequest.put("dateOfBirth", dateOfBirth);
			}
			
			parametersRequest.put("email", email);
			parametersRequest.put("sex", sex);
			parametersRequest.put("mobileNumber", mobileNumber);
			parametersRequest.put("accType", accType);
			parametersRequest.put("currency", currency);
			parametersRequest.put("idFront", idFront);
			parametersRequest.put("idBack", idBack);
			parametersRequest.put("custImg", custImg);
			parametersRequest.put("custSig", custSig);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			log.info(parameters.toString());
			log.info(header.toString());
			log.info("Test");
			log.info(acquirer.getAuthKey());
			String newWalletResponse = null;
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
			{
				header.put("authKey", acquirer.getAuthKey());
				log.info(acquirer.getAccountCreationEndPoint()); 
				log.info("Live Auth Key..." + acquirer.getDemoAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationEndPoint(), parameters.toString(), header);
				
				//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				header.put("authKey", acquirer.getDemoAuthKey());
				log.info(acquirer.getAccountCreationDemoEndPoint());
				log.info("Demo Auth Key..." + acquirer.getDemoAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationDemoEndPoint(), parameters.toString(), header);
			}
			log.info(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
			}
			catch(Exception e)
			{
				log.info("Test");
				e.printStackTrace();
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject response = walletResponse.getJSONObject("response");
			Integer status = walletResponse.getInt("status");
			if(status!=200)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject responseHeader = response.getJSONObject("tekHeader");
			log.info(response.toString());
			if(response.get("cust")==null)
				log.info("nullable cust");
			else
				log.info("non nullable cust");
			String operationStatus = responseHeader.getString("status");
			if(operationStatus.equals("FAIL"))
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
				Iterator<String> keys = msgList.keys();
				String errorMessage = null;
				while(keys.hasNext())
				{
					String ky = keys.next();
					errorMessage = msgList.getString(ky);
				}
				jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
			}
			else if(operationStatus.equals("SUCCESS"))
			{
				JSONObject customer_ = response.get("cust")!=null ? response.getJSONObject("cust") : null;
				if(customer_!=null)
				{
					JSONObject accNos = customer_.getJSONObject("accNos");
					Iterator<String> accNo = accNos.keys();
					String accNo_ = accNo.next();
					log.info("accNo..." + accNo_);
					JSONObject acctDetails = accNos.getJSONObject(accNo_);
		
					
					
					/*Customer customer = new Customer();
					customer.setAddressLine1(addressLine1);
					customer.setAddressLine2(addressLine2);
					customer.setContactEmail(email);
					customer.setAltContactMobile(null);
					customer.setContactMobile(mobileNumber);
					customer.setDateOfBirth(dob);
					customer.setFirstName(firstName);
					customer.setGender(sex==null ? null : Gender.valueOf(sex=="M" ? "MALE" : "FEMALE"));
					customer.setLastName(lastName);
					customer.setVerificationNumber(acctDetails.getInt("idCustomer") + "");
					customer.setStatus(CustomerStatus.ACTIVE);
					customer.setCreated_at(new Date());
					customer.setCustomerType(CustomerType.INDIVIDUAL);
					customer.setCustomerImage(custImg);
					customer.setLocationDistrict(district);
					customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(uniqueType));
					customer.setMeansOfIdentificationNumber(uniqueValue);
					customer = (Customer)swpService.createNewRecord(customer);*/
					hql = "Select tp from Customer tp where tp.verificationNumber = '"+customerVerificationNumber+"' AND tp.deleted_at IS NULL";
					log.info(hql);
					Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
					
					jsonObject.put("customerNumber", customer.getVerificationNumber());

					
					hql = "Select tp from Account tp where tp.customer.id = " + customer.getId();
					Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
					Account account = null;
					String branch_code = null;
					
					int isLive = 0;
					if(device!=null && device.getSwitchToLive().equals(1))
					{
						isLive = 1;
					}
					
					if(merchant!=null && device!=null)
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.DEVICE_SETTLEMENT, accNo_, 
								customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, ProbasePayCurrency.valueOf(currency), 
								null, null, accountScheme, isLive);
					}
					else
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.VIRTUAL, accNo_, 
							customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, 
							ProbasePayCurrency.valueOf(currency), null, null, accountScheme, isLive);
					}
					account = (Account)swpService.createNewRecord(account);
					jsonObject.put("accountNo", accNo_);
					
					
					Transaction transaction = new Transaction();
					transaction.setChannel(Channel.WEB);
					transaction.setCreated_at(new Date());
					transaction.setAmount(0.00);
					transaction.setCustomerId(customer.getId());
					transaction.setFixedCharge(null);
					transaction.setMessageRequest(null);
					transaction.setServiceType(ServiceType.DEPOSIT_OTC);
					transaction.setStatus(TransactionStatus.SUCCESS);
					transaction.setPayerEmail(email);
					transaction.setPayerMobile(mobileNumber);
					transaction.setPayerName(lastName + ", " + firstName);
					transaction.setResponseCode(TransactionCode.transactionSuccess);
					transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
					transaction.setTransactionDate(new Date());
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setAccount(account);
					transaction.setCreditAccountTrue(true);
					transaction.setCreditPoolAccountTrue(true);
					transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
					transaction.setReceipientChannel(Channel.WEB);
					transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
					transaction.setReceipientEntityId(account.getId());
					transaction.setDevice(device);
					transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
					if(merchant!=null)
						transaction.setMerchantId(merchant.getId());
					
					transaction = (Transaction)swpService.createNewRecord(transaction);
					jsonObject.put("transactionId", transaction.getId());
					
					
				
					jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountId", account.getId());
					jsonObject.put("accountIdentifier", accNo_);
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Wallet Created Successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
					jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}

	
	public static JSONObject getCustomerDetailsFromMap(JSONObject customer)
	{
		JSONObject oneCustomer = new JSONObject();
		try {
			oneCustomer.put("id", (BigInteger)customer.get("id"));
			oneCustomer.put("firstName", UtilityHelper.getValue((String)customer.get("firstName")));
			oneCustomer.put("lastName", UtilityHelper.getValue((String)customer.get("lastName")));
			oneCustomer.put("otherName", UtilityHelper.getValue((String)customer.get("otherName")));
			oneCustomer.put("gender", UtilityHelper.getValue(customer.get("gender")==null ? null : Gender.values()[(Integer)customer.get("gender")].name()));
			oneCustomer.put("addressLine1", UtilityHelper.getValue((String)customer.get("addressLine1")));
			oneCustomer.put("addressLine2", UtilityHelper.getValue((String)customer.get("addressLine2")));
			oneCustomer.put("locationDistrict", UtilityHelper.getValue(customer.get("locationDistrict")==null ? "" : (String)customer.get("locationDistrict")));
			oneCustomer.put("locationDistrictId", customer.get("locationDistrictId")==null ? null : (BigInteger)customer.get("locationDistrictId"));
			oneCustomer.put("locationProvince", customer.get("locationProvince")==null ? "" : (String)customer.get("locationProvince"));
			oneCustomer.put("locationProvinceId", customer.get("locationProvinceId")==null ? null : (BigInteger)customer.get("locationProvinceId"));
			oneCustomer.put("dateOfBirth", UtilityHelper.getDateOfBirth((Date)customer.get("dateOfBirth")));
			oneCustomer.put("contactMobile", UtilityHelper.getValue((String)customer.get("contactMobile")));
			oneCustomer.put("altContactMobile", UtilityHelper.getValue((String)customer.get("altContactMobile")));
			oneCustomer.put("contactEmail", UtilityHelper.getValue((String)customer.get("contactEmail")));
			oneCustomer.put("altContactEmail", UtilityHelper.getValue((String)customer.get("altContactEmail")));
			oneCustomer.put("verificationNumber", UtilityHelper.getValue((String)customer.get("verificationNumber")));
			oneCustomer.put("status", UtilityHelper.getValue(CustomerStatus.values()[(Integer)customer.get("status")].name()));
			oneCustomer.put("customerType", UtilityHelper.getValue(customer.get("customerType")==null ? null :  CustomerType.values()[(Integer)customer.get("customerType")].name()));
			oneCustomer.put("customerImage", UtilityHelper.getValue((String)customer.get("customerImage")));
			
			return oneCustomer;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static JSONObject getAccountDetailsFromMap(JSONObject account) {
		// TODO Auto-generated method stub
		JSONObject oneAccount = new JSONObject();
		try {
			oneAccount.put("id", (BigInteger)account.get("id"));
			oneAccount.put("accountIdentifier", UtilityHelper.getValue((String)account.get("accountIdentifier")));
			oneAccount.put("accountType", UtilityHelper.getValue(AccountType.values()[(Integer)account.get("accountType")].name()));
			oneAccount.put("status", UtilityHelper.getValue(AccountStatus.values()[(Integer)account.get("status")].name()));
			return oneAccount;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public static JSONObject getCardDetails(JSONObject card) {
		// TODO Auto-generated method stub
		JSONObject oneAccount = new JSONObject();
		try {
			oneAccount.put("id", (BigInteger)card.get("id"));
			oneAccount.put("cardPan", UtilityHelper.formatPan((String)card.get("pan")));
			oneAccount.put("serialNo", UtilityHelper.getValue((String)card.get("SerialNo(")));
			oneAccount.put("trackingNumber", UtilityHelper.getValue((String)card.get("TrackingNumber")));
			oneAccount.put("accountIdentifier", ((String)card.get("accountIdentifier")));
			oneAccount.put("cardType", UtilityHelper.getValue(CardType.values()[(Integer)card.get("cardType")].name()));
			oneAccount.put("status", UtilityHelper.getValue(CardStatus.values()[(Integer)card.get("status")].name()));
			return oneAccount;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public static void fundCard(SwpService swpService, HttpHeaders httpHeaders, HttpServletRequest requestContext, Double amount, 
		ECard card, Account account, String token, String merchantCode, String deviceCode, User user, String channel, String orderRef, Transaction transaction) throws Exception {
		// TODO Auto-generated method stub
		AccountServicesV2 asv = new AccountServicesV2();
		Response accountBalanceResp = asv.getAccountBalance(httpHeaders, requestContext, account.getAccountIdentifier(), token, null, null);
		if(accountBalanceResp!=null)
		{
			JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
			if(accountBalance!=null)
			{
				Double availableBalance = accountBalance.getDouble("availableBalance");
				if(availableBalance>=amount)
				{
					CardBalanceManage cardBalanceManageThread = new CardBalanceManage(swpService, httpHeaders, requestContext, amount, card, account, token, 
							merchantCode, deviceCode, channel, user, 1, orderRef);
					cardBalanceManageThread.start();
					
				}
			}
		}
		
	}
	
	
	
	public static JSONObject transferFunds(SwpService swpService, HttpHeaders httpHeaders, HttpServletRequest requestContext, Double amount, ECard card, Account srcAccount, 
			String destAccountIdentifier, String token, User user, String channel)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
			jsonObject.put("message", "System error. We experienced some issues. Please give it a try again");
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			log.info("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			//String branch_code = verifyJ.getString("branchCode");
			//log.info("branch_code ==" + branch_code);
			
			Map<String, Object> destinationAccount = null;
			String hql = "Select tp.* from accounts tp  where tp.accountIdentifier = '" + destAccountIdentifier + "' AND tp.deleted_at IS NULL";
			List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			if(accounts!=null && accounts.size()>0)
			{
				destinationAccount = accounts.get(0);
			}
			
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	static class CardBalanceManage extends Thread {
		
		SwpService swpService;
		HttpHeaders httpHeaders;
		HttpServletRequest requestContext;
		Double amount;
		ECard card;
		Account account;
		String token;
		String merchantCode; 
		String deviceCode;
		Integer operation;
		String channel;
		User user;
		String orderRef;
		
		CardBalanceManage(SwpService swpService, HttpHeaders httpHeaders, HttpServletRequest requestContext, Double amount, ECard card, Account account, String token, 
				String merchantCode, String deviceCode, String channel, User user, Integer operation, String orderRef)
		{
			this.swpService = swpService;
			this.httpHeaders = httpHeaders;
			this.requestContext = requestContext;
			this.amount = amount;
			this.card = card;
			this.account = account;
			this.token = token;
			this.merchantCode = merchantCode;
			this.deviceCode = deviceCode;
			this.channel = channel;
			this.user = user;
			this.operation = operation;
			this.orderRef = orderRef; 
		}
		
		
		public void run() {
			try
			{
				if(this.operation!=null && this.operation==0)//DEBIT CARD
				{
					AccountServicesV2 asv = new AccountServicesV2();
					Response accountBalanceResp = asv.getAccountBalance(this.httpHeaders, 
							this.requestContext, 
							this.account.getAccountIdentifier(), 
							this.token, 
							this.merchantCode, 
							this.deviceCode);
					if(accountBalanceResp!=null)
					{
						JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
						if(accountBalance!=null)
						{
							Double availableBalance = accountBalance.getDouble("availableBalance");
							if(availableBalance>=amount)
							{
								CardScheme cardScheme = this.card.getCardScheme();
								Double transactionCharge = cardScheme.getOverrideFixedFee();
								Double transactionPercentage = cardScheme.getOverrideTransactionFee();
								Double fixedCharge = cardScheme.getOverrideFixedFee();
								Double percentageCharge =  cardScheme.getOverrideTransactionFee();
								Double schemeTransactionCharge =  fixedCharge==null ? 0 : (fixedCharge*this.amount/100);
								schemeTransactionCharge = new BigDecimal(schemeTransactionCharge.toString()).setScale(2,RoundingMode.HALF_UP).doubleValue();
								Double schemeTransactionPercentage = cardScheme.getOverrideTransactionFee()==null ? 0 : cardScheme.getOverrideTransactionFee();
								Double newCharges = schemeTransactionPercentage + schemeTransactionCharge;
								Double newBalance = this.amount - (newCharges);
								this.card = this.card.withdraw(this.swpService, this.amount, newCharges);
							}
						}
					}
				}
				else if(this.operation!=null && this.operation==1)//CREDIT CARD
				{
					AccountServicesV2 asv = new AccountServicesV2();
					Response accountBalanceResp = asv.getAccountBalance(this.httpHeaders, 
							this.requestContext, 
							this.account.getAccountIdentifier(), 
							this.token, 
							this.merchantCode, 
							this.deviceCode);
					if(accountBalanceResp!=null)
					{
						JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
						if(accountBalance!=null)
						{
							Double availableBalance = accountBalance.getDouble("availableBalance");
							if(availableBalance>=amount)
							{
								Double previousBalance = this.card.getCardBalance();
								String[] tr = RandomStringUtils.randomAlphanumeric(16).toUpperCase().split("", 4);
								StringBuilder sb = new StringBuilder();
								String transactionRef =  StringUtils.join(Arrays.asList(tr), '-');
								String bankPaymentReference = null;
								Long customerId =  this.card.getCustomerId();
								Boolean creditAccountTrue =  false;
								Boolean creditCardTrue = true;
								String rpin =  null;
								Channel channel = Channel.valueOf(this.channel);
								Date transactionDate =  new Date();
								ServiceType serviceType =  ServiceType.CREDIT_CARD;
								String payerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
								String payerEmail =  account.getCustomer().getContactEmail();
								String payerMobile =  account.getCustomer().getContactMobile();
								TransactionStatus status = TransactionStatus.SUCCESS;
								CardScheme cardScheme = this.card.getCardScheme();
								ProbasePayCurrency probasePayCurrency =  cardScheme.getCurrency();
								String transactionCode = "00";
								Account account =  null;
								Boolean creditPoolAccountTrue =  false;
								String messageRequest = null;
								String messageResponse =  null;
								Double transactionCharge = cardScheme.getOverrideFixedFee();
								Double transactionPercentage = cardScheme.getOverrideTransactionFee();
								Double fixedCharge = cardScheme.getOverrideFixedFee();
								Double percentageCharge =  cardScheme.getOverrideTransactionFee();
								Double schemeTransactionCharge =  fixedCharge==null ? 0 : (fixedCharge*this.amount/100);
								schemeTransactionCharge = new BigDecimal(schemeTransactionCharge.toString()).setScale(2,RoundingMode.HALF_UP).doubleValue();
								Double schemeTransactionPercentage = cardScheme.getOverrideTransactionFee()==null ? 0 : cardScheme.getOverrideTransactionFee();
								transactionCharge = 0.00; //For Credit
								transactionPercentage =0.00; //For Credit
								fixedCharge = 0.00; //For Credit
								percentageCharge = 0.00; //For Credit
								schemeTransactionCharge = 0.00; //For Credit
								schemeTransactionPercentage = 0.00; //For Credit
								Double amountToCredit = amount - schemeTransactionCharge - schemeTransactionPercentage;
								String responseCode =  "00";
								String otp =  null;
								String otpRef = null;
								Long merchantId =  null;
								String merchantName =  null;
								String merchantCode = null;
								String merchantBank =  null;
								String merchantAccount =  null;
								Long transactingBankId =  null;
								Long acquirerId =  this.account.getAcquirer().getBank().getId();
								Long receipientTransactingBankId = null;
								Integer accessCode =  null;
								Long sourceEntityId = this.account.getId(); 
								Long receipientEntityId = this.card.getId();
								Channel receipientChannel =  channel;
								String transactionDetail = "TXFR AMT:" + probasePayCurrency.name() + "" + amount + " SRC: ACCT-" + this.account.getAccountIdentifier() + 
									" RECV: CARD-" + this.card.getTrackingNumber() + " DT:" + (new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
								Double closingBalance =  this.card.getCardBalance();
								Long paidInByBankUserAccountId =  this.user.getId();
								String customData = "";
								String responseData =  null;
								Long adjustedTransactionId = null;
								Device device = null;
								
								Double newCharges = 0.00;
								Double newBalance = this.amount - (newCharges);
								log.info("new Balance..." + newBalance);
								this.card = this.card.deposit(this.swpService, newBalance, newCharges);
								closingBalance = this.card.getCardBalance();
								
								
								
								String hql = "Select tp.* from devicebankaccounts tp, accounts acc where tp.transientAccountId = acc.id AND tp.deviceId = " + device.getId() + " AND tp.deleted_at IS NULL";
								List<Map<String, Object>> devicebankaccounts = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
								Boolean debitAccountTrue = true;
								Boolean debitCardTrue = false;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = null;
								Long debitCardId = null;
								Account recipientAccount = null;
								if(devicebankaccounts!=null && devicebankaccounts.size()>0)
								{
									Map<String, Object> devicebankaccount = devicebankaccounts.get(0);
									debitAccountId = (Long)devicebankaccount.get("transientAccountId");
									creditCardId = this.card.getId();
									creditAccountId = this.card.getAccount().getId();
									recipientAccount = (Account) swpService.getRecordById(Account.class, creditAccountId);
								}
								//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
								
								Transaction transaction = new Transaction(transactionRef, bankPaymentReference, customerId, creditAccountTrue, 
									creditCardTrue, orderRef, rpin, channel, transactionDate, serviceType, payerName, payerEmail, payerMobile, status, 
									probasePayCurrency, transactionCode, this.account, this.card, device, creditPoolAccountTrue, messageRequest, messageResponse, fixedCharge, 
									transactionCharge, transactionPercentage, schemeTransactionCharge, schemeTransactionPercentage, this.amount, responseCode, otp, otpRef, merchantId, 
									merchantName, this.merchantCode, merchantBank, merchantAccount, transactingBankId, receipientTransactingBankId, accessCode, sourceEntityId, 
									receipientEntityId, receipientChannel, transactionDetail, closingBalance, null, null, paidInByBankUserAccountId, 
									customData, responseData, adjustedTransactionId, acquirerId, 
									debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
									"Credit Card - **** " + this.card.getTrackingNumber().substring(this.card.getTrackingNumber().length() - 4), this.card.getIsLive());
								transaction = (Transaction)this.swpService.createNewRecord(transaction);
								
			
								ECardDeposit ecardDeposit = new ECardDeposit(this.card.getId(), this.amount, previousBalance, this.card.getCardBalance(), 
									this.account.getAccountIdentifier(), (schemeTransactionPercentage + schemeTransactionCharge), orderRef, transactionRef, 
									transaction.getId(), cardScheme.getId(), this.account.getId(), null, null, null, this.card.getId(),
									this.account.getCustomer().getId(), this.user.getId());
								ecardDeposit = (ECardDeposit)this.swpService.createNewRecord(ecardDeposit);
							}
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	    }
	}


	public static String validateAirtime(String telcoProvider, String receipient, Double amount, String authKey, String external_ref) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		String reqBody = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		log.info(reqBody);
		okhttp3.RequestBody body = RequestBody.create(mediaType, reqBody);
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		log.info(responseString);*/
		
		String baseUrl = "https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services";
		String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\""+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}
	

	public static JSONObject validateMerchant(String recipientDeviceCode, String paymentReferenceNo, SwpService swpService) throws Exception {
		// TODO Auto-generated method stub
		String hql = "Select tp from Device tp where tp.deviceCode = '"+recipientDeviceCode+"' AND tp.status = " + DeviceStatus.ACTIVE.ordinal();
		Device device = (Device)swpService.getUniqueRecordByHQL(hql);
		if(device==null)
		{
			return null;
		}
		JSONObject js = new JSONObject();
		if(paymentReferenceNo!=null)
		{
			hql = "Select tp from MerchantPayment tp where lower(tp.transactionRef) = '"+ paymentReferenceNo.toLowerCase() +"' AND tp.status = 'PENDING' AND "
					+ "tp.merchantId = " + device.getId();
			MerchantPayment merchantPayment = (MerchantPayment)swpService.getUniqueRecordByHQL(hql);
			js.put("merchant", device.getMerchant());
			js.put("device", device);
			js.put("merchantPayment", merchantPayment);
		}
		else
		{
			js.put("merchant", device.getMerchant());
			js.put("device", device);
		}
		
		return js;
	}
	
	
	public static String validateElecticity(String vendorProvider, String receipient, Double amount, String authKey, String external_ref) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		String reqBody = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		log.info(reqBody);
		okhttp3.RequestBody body = RequestBody.create(mediaType, reqBody);
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		log.info(responseString);*/
		
		String baseUrl = "https://smarthub-test.ops.probasegroup.com/graphql/commercials/bill-payment/services";
		//String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+vendorProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"ELECTRICITY\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		String parameters = "{\"query\":\"{\\n    Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+vendorProvider+"\\\"\\n        recipient: \\\""+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"ELECTRICITY\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}
	

	public static String validateDSTV(String vendorProvider, String receipient, Double amount, String authKey, String external_ref) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		String reqBody = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		log.info(reqBody);
		okhttp3.RequestBody body = RequestBody.create(mediaType, reqBody);
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		log.info(responseString);*/
		
		String baseUrl = "https://smarthub-test.ops.probasegroup.com/graphql/commercials/bill-payment/services";
		//String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+vendorProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"ELECTRICITY\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		String parameters = "{\"query\":\"{\\n    Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+vendorProvider+"\\\"\\n        recipient: \\\""+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"DSTV\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}


	public static String purchaseAirtime(String authKey, String validationToken) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		okhttp3.RequestBody body = RequestBody.create(mediaType, "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}");
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		return responseString;*/
		
		

		String baseUrl = "https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services";
		//String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		//String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n            running_balance\\n    }\\n}\",\"variables\":{}}";
		String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}


	public static String purchaseElectricity(String authKey, String validationToken) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		okhttp3.RequestBody body = RequestBody.create(mediaType, "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}");
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		return responseString;*/
		
		

		String baseUrl = "https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services";
		//String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		//String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n            running_balance\\n    }\\n}\",\"variables\":{}}";
		String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}


	public static String purchaseDSTV(String authKey, String validationToken) throws Exception {
		// TODO Auto-generated method stub
		/*OkHttpClient client = new OkHttpClient().newBuilder()
		  .build();
		okhttp3.MediaType mediaType = MediaType.parse("application/json");
		okhttp3.RequestBody body = RequestBody.create(mediaType, "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}");
		okhttp3.Request request = new Request.Builder()
		  .url("https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services")
		  .method("POST", body)
		  .addHeader("Content-Type", "application/json")
		  .build();
		okhttp3.Response response = client.newCall(request).execute();
		okhttp3.ResponseBody responseBody = response.body();
		String responseString = responseBody.string();
		return responseString;*/
		
		

		String baseUrl = "https://smarthub-test.ops.probasegroup.com//graphql/commercials/bill-payment/services";
		//String parameters = "{\"query\":\"{\\n        Validation(auth_key: \\\""+authKey+"\\\", \\n        voucher_provider: \\\""+telcoProvider+"\\\"\\n        recipient: \\\"+"+receipient+"\\\",\\n        amount: "+amount+",\\n        external_ref: \\\""+external_ref+"\\\",\\n        service_type: \\\"AIRTIME\\\")\\n        {\\n        token\\n        amount\\n        voucher_provider\\n        service_type\\n        response\\n    }\\n}\",\"variables\":{}}";
		//String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n            running_balance\\n    }\\n}\",\"variables\":{}}";
		String parameters = "{\"query\":\"{\\n    Purchase(auth_key: \\\""+authKey+"\\\",\\n        token: \\\""+validationToken+"\\\") \\n        {\\n            external_ref\\n            face_value\\n            purchase_time\\n            transaction_ref\\n            recipient_account\\n            response\\n            face_value\\n            commissionValue\\n            status\\n    }\\n}\",\"variables\":{}}";
		JSONObject jsObj = new JSONObject();
		jsObj.put("Content-Type", "application/json");
		String responseString = UtilityHelper.sendPost(baseUrl, parameters, jsObj);
		
		return responseString;
	}

	public static JSONObject debitBankWallet(Application app, SwpService swpService, HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Double amount, Account sourceAccount, String token, String merchantCode, String deviceCode, 
			User user, String channel, String orderRef, String remarks, Acquirer acquirer) throws Exception {
		// TODO Auto-generated method stub
		
		String hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantCode+"'";
		Device device = (Device)swpService.getUniqueRecordByHQL(hql);
		Merchant merchant = null;
		if(device!=null)
		{
			merchant = device.getMerchant();
		}
		
		
		AccountServicesV2 asv = new AccountServicesV2();
		JSONObject jsonObject = new JSONObject();
		String defaultaccountscheme = app.getAllSettings().getString("defaultaccountscheme");
		Long defaultaccountschemeId = Long.parseLong(defaultaccountscheme);
		CardScheme accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultaccountschemeId);
		
		Double fixedCharge = accountScheme.getOverrideFixedFee();
		Double transactionCharge = accountScheme.getOverrideTransactionFee();
		Double transactionPercentage = 0.00;
		Double schemeTransactionCharge = 0.00;
		
		Double charges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge;
		Double chargeableAmount = amount;
				
				
				
		Response accountBalanceResp = asv.getAccountBalance(httpHeaders, requestContext, sourceAccount.getAccountIdentifier(), token, null, null);
		if(accountBalanceResp!=null)
		{
			JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
			if(accountBalance!=null)
			{
				Double availableBalance = accountBalance.getDouble("availableBalance");
				if(availableBalance>=chargeableAmount)
				{
					String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
					Date pDate = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
					JSONObject parameters = new JSONObject();
					parameters.put("service", UtilityHelper.ZICB_DEBIT_WALLET_SERVICE_CODE);
					JSONObject parametersRequest = new JSONObject();
					parametersRequest.put("srcAcc", sourceAccount.getAccountIdentifier());
					parametersRequest.put("srcBranch", sourceAccount.getBranchCode());
					parametersRequest.put("amount", amount);
					parametersRequest.put("payDate", sdf.format(pDate));
					parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
					parametersRequest.put("remarks", remarks);
					parametersRequest.put("referenceNo", orderRef + "-" + transactionRef);
					parameters.put("request", parametersRequest);
					JSONObject header = new JSONObject();
					header.put("Content-Type", "application/json; utf-8");
					header.put("Accept", "application/json");
					log.info(parameters.toString());
					log.info(header.toString());
					log.info("Test");
					String newWalletResponse = null;
					if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
					{
						header.put("authKey", acquirer.getAuthKey());
						log.info(acquirer.getAccountCreationEndPoint()); 
						log.info("Live Auth Key..." + acquirer.getDemoAuthKey());
						newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
						
						//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
					}
					else
					{
						header.put("authKey", acquirer.getDemoAuthKey());
						log.info(acquirer.getAccountCreationDemoEndPoint());
						log.info("Demo Auth Key..." + acquirer.getDemoAuthKey());
						newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
					}
					log.info(newWalletResponse);
					
					if(newWalletResponse==null)
					{
						jsonObject.put("status", ERROR.WALLET_DEBIT_FAILED);
						jsonObject.put("message", "Source wallet debit was not successful");
						return jsonObject;
					}
					
					JSONObject walletResponse = null;
					try
					{
						walletResponse = new JSONObject(newWalletResponse);
					}
					catch(Exception e)
					{
						log.info("Test");
						e.printStackTrace();
						jsonObject.put("status", ERROR.WALLET_DEBIT_FAILED);
						jsonObject.put("message", "Source wallet debit was not successful");
						return jsonObject;
					}
					JSONObject response = walletResponse.getJSONObject("response");
					Integer status = walletResponse.getInt("status");
					if(status!=200)
					{
						jsonObject.put("status", ERROR.WALLET_DEBIT_FAILED);
						jsonObject.put("message", "Source wallet debit was not successful");
						return jsonObject;
					}
					JSONObject responseHeader = response.getJSONObject("tekHeader");
					log.info(response.toString());
					if(response.get("cust")==null)
						log.info("nullable cust");
					else
						log.info("non nullable cust");
					String operationStatus = responseHeader.getString("status");
					if(operationStatus.equals("FAIL"))
					{
						jsonObject.put("status", ERROR.WALLET_DEBIT_FAILED);
						JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
						Iterator<String> keys = msgList.keys();
						String errorMessage = null;
						while(keys.hasNext())
						{
							String ky = keys.next();
							errorMessage = msgList.getString(ky);
						}
						jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
					}
					else if(operationStatus.equals("SUCCESS"))
					{
						accountBalanceResp = asv.getAccountBalance(httpHeaders, requestContext, sourceAccount.getAccountIdentifier(), token, null, null);
						if(accountBalanceResp!=null)
						{
							accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
							if(accountBalance!=null)
							{
								availableBalance = accountBalance.getDouble("availableBalance");
							}
						}
						String bankPaymentReference = responseHeader.getString("tekesbrefno");
						Long customerId = sourceAccount.getCustomer().getId();
						Boolean creditAccountTrue = false;
						Boolean creditCardTrue = false;
						String rpin = null;
						Date transactionDate = new Date();
						ServiceType serviceType = ServiceType.DEBIT_WALLET;
						String payerName = sourceAccount.getCustomer().getFirstName() + " " + sourceAccount.getCustomer().getLastName();
						String payerEmail = sourceAccount.getCustomer().getContactEmail();
						String payerMobile = sourceAccount.getCustomer().getContactMobile();
						TransactionStatus transactionStatus = TransactionStatus.SUCCESS;
						ProbasePayCurrency probasePayCurrency = sourceAccount.getProbasePayCurrency();
						String transactionCode = TransactionCode.transactionSuccess;
						ECard card = null;
						Boolean creditPoolAccountTrue = false;
						String messageRequest = null;
						String messageResponse =  newWalletResponse;
						String responseCode = "00";
						String oTP = null;
						String oTPRef = null;
						Double schemeTransactionPercentage = null;
						Long merchantId = null;
						String merchantName = null;
						String merchantBank = null;
						String merchantAccount = null;
						Long transactingBankId = acquirer.getBank().getId();
						Long receipientTransactingBankId = acquirer.getBank().getId();
						Integer accessCode = null;
						Long sourceEntityId = sourceAccount.getId();
						Long receipientEntityId = null;
						Channel chanel = Channel.valueOf(channel);
						Channel receipientChannel = Channel.valueOf(channel);
						String transactionDetail = remarks;
						Double closingBalance = availableBalance;
						Double totalCreditSum = null;
						Double totalDebitSum = null;
						Long paidInByBankUserAccountId = null;
						String customData = null;
						String responseData = null;
						Long adjustedTransactionId = null;
						Long acquirerId = acquirer.getId();
						
						
						
						Boolean debitAccountTrue = true;
						Boolean debitCardTrue = false;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						Account recipientAccount = null;
						debitAccountId = sourceAccount.getId();
						//debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId
						
						Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, chanel,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, transactionStatus,
								probasePayCurrency, transactionCode,
								sourceAccount, card, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								amount, responseCode, oTP, oTPRef,
								merchantId, merchantName, merchantCode,
								merchantBank, merchantAccount, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, "Debit Wallet", sourceAccount.getIsLive());
								
						
						transaction = (Transaction)swpService.createNewRecord(transaction);
						jsonObject.put("transactionId", transaction.getId());
						
						
						Customer customer = sourceAccount.getCustomer();
						jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
						jsonObject.put("customerNumber", customer.getVerificationNumber());
						jsonObject.put("customerId", customer.getId());
						jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
						jsonObject.put("availableBalance", availableBalance);
						jsonObject.put("charges", charges);
						jsonObject.put("amount", amount);
						jsonObject.put("status", ERROR.DEBIT_SUCCESSFUL);
						jsonObject.put("bankReference", bankPaymentReference);
						jsonObject.put("message", "Source wallet debited successfully");
					
					}
					else
					{
						jsonObject.put("status", ERROR.WALLET_DEBIT_FAILED);
						jsonObject.put("message", "Source wallet debited was not successful");
					}
					
				}
			}
		}
		
		return jsonObject;
	}
	
	
	
	
	
	public static JSONObject creditBankWallet(Application app, SwpService swpService, HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Double amount, Account destinationAccount, String token, String merchantCode, String deviceCode, 
			User user, String channel, String orderRef, String remarks, Acquirer acquirer, Transaction transaction) {
		// TODO Auto-generated method stub
		try
		{
			String hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantCode+"'";
			Device device = (Device)swpService.getUniqueRecordByHQL(hql);
			Merchant merchant = null;
			if(device!=null)
			{
				merchant = device.getMerchant();
			}
			
			
			AccountServicesV2 asv = new AccountServicesV2();
			JSONObject jsonObject = new JSONObject();
			Long defaultaccountschemeId = Long.parseLong(app.getAllSettings().getString("defaultaccountscheme"));
			//Long defaultaccountschemeId = (Long)(defaultaccountscheme);
			CardScheme accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultaccountschemeId);
			
			Double fixedCharge = accountScheme.getOverrideFixedFee();
			Double transactionCharge = accountScheme.getOverrideTransactionFee();
			Double transactionPercentage = 0.00;
			Double schemeTransactionCharge = 0.00;
			
			Double charges = 0.00;
			Double chargeableAmount = amount;
					
			jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
			jsonObject.put("message", "Source wallet credit was not successful");		
					
			Response accountBalanceResp = asv.getAccountBalance(httpHeaders, requestContext, destinationAccount.getAccountIdentifier(), token, merchantCode, deviceCode);
			if(accountBalanceResp!=null)
			{
				JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
				if(accountBalance!=null)
				{
					String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
					Date pDate = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					JSONObject parameters = new JSONObject();
					parameters.put("service", UtilityHelper.ZICB_CREDIT_WALLET_SERVICE_CODE);
					JSONObject parametersRequest = new JSONObject();
					parametersRequest.put("destAcc", destinationAccount.getAccountIdentifier());
					parametersRequest.put("destBranch", destinationAccount.getBranchCode());
					parametersRequest.put("amount", amount);
					parametersRequest.put("payDate", sdf.format(pDate));
					parametersRequest.put("payCurrency", destinationAccount.getProbasePayCurrency().name());
					parametersRequest.put("remarks", remarks);
					parametersRequest.put("referenceNo", orderRef);
					parametersRequest.put("referenceNo", orderRef);
					parameters.put("request", parametersRequest);
					JSONObject header = new JSONObject();
					header.put("Content-Type", "application/json; utf-8");
					header.put("Accept", "application/json");
					log.info(parameters.toString());
					log.info(header.toString());
					log.info("Test");
					String newWalletResponse = null;
					if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(Boolean.TRUE))
					{
						header.put("authKey", acquirer.getCreditFTAuthKey());
						log.info(acquirer.getCreditFTAuthKey()); 
						log.info("Live Auth Key..." + acquirer.getCreditFTAuthKey());
						newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
						
						//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
					}
					else
					{
						header.put("authKey", acquirer.getDemoCreditFTAuthKey());
						log.info(acquirer.getDemoCreditFTAuthKey());
						log.info("Demo Auth Key..." + acquirer.getDemoCreditFTAuthKey());
						newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
					}
					log.info(newWalletResponse);
					
					if(newWalletResponse==null)
					{
						jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
						jsonObject.put("message", "Source wallet credit was not successful");
						return jsonObject;
					}
					
					JSONObject walletResponse = null;
					try
					{
						walletResponse = new JSONObject(newWalletResponse);
					}
					catch(Exception e)
					{
						log.info("Test");
						e.printStackTrace();
						jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
						jsonObject.put("message", "Source wallet credit was not successful");
						return jsonObject;
					}
					JSONObject response = walletResponse.getJSONObject("response");
					Integer status = walletResponse.getInt("status");
					if(status!=200)
					{
						jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
						jsonObject.put("message", "Source wallet credit was not successful");
						return jsonObject;
					}
					JSONObject responseHeader = response.getJSONObject("tekHeader");
					log.info(response.toString());
					
					String operationStatus = responseHeader.getString("status");
					if(operationStatus.equals("FAIL"))
					{
						jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
						JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
						Iterator<String> keys = msgList.keys();
						String errorMessage = null;
						while(keys.hasNext())
						{
							String ky = keys.next();
							errorMessage = msgList.getString(ky);
						}
						jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
					}
					else if(operationStatus.equals("SUCCESS"))
					{
						
						String bankPaymentReference = responseHeader.getString("tekesbrefno");
						
						jsonObject.put("transactionId", transaction.getId());
						Customer customer = destinationAccount.getCustomer();
						jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
						jsonObject.put("customerNumber", customer.getVerificationNumber());
						jsonObject.put("customerId", customer.getId());
						jsonObject.put("accountIdentifier", destinationAccount.getAccountIdentifier());
						jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
						jsonObject.put("charges", charges);
						jsonObject.put("amount", amount);
						jsonObject.put("bankReference", bankPaymentReference);
						jsonObject.put("message", "Wallet credit was successful");
					
					}
					else
					{
						jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
						jsonObject.put("message", "Wallet credit was not successful");
					}
				}
			}
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	
	
	public static JSONObject fundsTransfer(Application app, SwpService swpService, HttpHeaders httpHeaders,
			HttpServletRequest requestContext, Double amount, Account sourceAccount, Account destinationAccount1, String token, String merchantCode, String deviceCode, 
			User user, String channel, String orderRef, String remarks, Acquirer acquirer, Transaction transaction) {
		// TODO Auto-generated method stub
		try
		{
			AccountServicesV2 asv = new AccountServicesV2();
			JSONObject jsonObject = new JSONObject();
			Long defaultaccountschemeId = Long.parseLong(app.getAllSettings().getString("defaultaccountscheme"));
			//Long defaultaccountschemeId = (Long)(defaultaccountscheme);
			CardScheme accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultaccountschemeId);
			
			Double fixedCharge = accountScheme.getOverrideFixedFee();
			Double transactionCharge = accountScheme.getOverrideTransactionFee();
			Double transactionPercentage = 0.00;
			Double schemeTransactionCharge = 0.00;
			
			Double charges = 0.00;
			Double chargeableAmount = amount;
					
			jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
			jsonObject.put("message", "Source wallet credit was not successful");		
					
			Response accountBalanceResp = asv.getAccountBalance(httpHeaders, requestContext, sourceAccount.getAccountIdentifier(), token, merchantCode, deviceCode);
			if(accountBalanceResp!=null)
			{
				JSONObject accountBalance = new JSONObject((String)accountBalanceResp.getEntity());
				if(accountBalance!=null)
				{
					Double currentBalance = accountBalance.getDouble("currentBalance");
					if(amount>app.getAllSettings().getDouble("minimumtransactionamountweb") && amount<app.getAllSettings().getDouble("maximumtransactionamountweb"))
					{
						//if((amount - app.getMinimumBalance())>balance)
						//if(balance  > amount)
						if(currentBalance  > amount)
						{
							String transactionRef = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
							Date pDate = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							JSONObject parameters = new JSONObject();
							parameters.put("service", UtilityHelper.ZICB_FT_WALLET_SERVICE_CODE);
							JSONObject parametersRequest = new JSONObject();
							parametersRequest.put("destAcc", sourceAccount.getAccountIdentifier());
							parametersRequest.put("destBranch", sourceAccount.getBranchCode());
							parametersRequest.put("amount", amount);
							parametersRequest.put("payDate", sdf.format(pDate));
							parametersRequest.put("payCurrency", sourceAccount.getProbasePayCurrency().name());
							parametersRequest.put("remarks", remarks);
							parametersRequest.put("referenceNo", orderRef);
							
							
							
							parametersRequest.put("addBene", false);
							parametersRequest.put("amount", amount);
							parametersRequest.put("bankCode", orderRef);
							parametersRequest.put("bankName", orderRef);
							parametersRequest.put("benName", orderRef);
							parametersRequest.put("beneEmail", orderRef);
							parametersRequest.put("benePhoneno", orderRef);
							parametersRequest.put("beneTransfer", orderRef);
							parametersRequest.put("branchCode", orderRef);
							parametersRequest.put("channelType", orderRef);
							parametersRequest.put("customerId", orderRef);
							parametersRequest.put("destinationAccount", destinationAccount1.getAccountIdentifier());
							parametersRequest.put("destinationBranch", destinationAccount1.getBranchCode());
							parametersRequest.put("destinationCurrency", destinationAccount1.getProbasePayCurrency().name());
							parametersRequest.put("sourceAccount", sourceAccount.getAccountIdentifier());
							parametersRequest.put("sourceBranch", sourceAccount.getBranchCode());
							parametersRequest.put("srcCurrency", sourceAccount.getProbasePayCurrency().name());
							parametersRequest.put("ipAddress", orderRef);
							parametersRequest.put("language", orderRef);
							parametersRequest.put("nationalClearingCode", orderRef);
							parametersRequest.put("requestId", orderRef);
							parametersRequest.put("swiftCode", orderRef);
							parametersRequest.put("transferTyp", orderRef);
							parametersRequest.put("userName", orderRef);
							
							
							parameters.put("request", parametersRequest);
							JSONObject header = new JSONObject();
							header.put("Content-Type", "application/json; utf-8");
							header.put("Accept", "application/json");
							log.info(parameters.toString());
							log.info(header.toString());
							log.info("Test");
							String newWalletResponse = null;
							
							String hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.merchantCode = '"+merchantCode+"'";
							Device device = (Device)swpService.getUniqueRecordByHQL(hql);
							Merchant merchant = null;
							if(device!=null)
							{
								merchant = device.getMerchant();
							}
							
							
							
							if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(Boolean.TRUE))
							{
								header.put("authKey", acquirer.getCreditFTAuthKey());
								log.info(acquirer.getCreditFTAuthKey()); 
								log.info("Live Auth Key..." + acquirer.getCreditFTAuthKey());
								newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
								
								//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
							}
							else
							{
								header.put("authKey", acquirer.getDemoCreditFTAuthKey());
								log.info(acquirer.getDemoCreditFTAuthKey());
								log.info("Demo Auth Key..." + acquirer.getDemoCreditFTAuthKey());
								newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
							}
							log.info(newWalletResponse);
							
							if(newWalletResponse==null)
							{
								jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
								jsonObject.put("message", "Source wallet credit was not successful");
								return jsonObject;
							}
							
							JSONObject walletResponse = null;
							try
							{
								walletResponse = new JSONObject(newWalletResponse);
							}
							catch(Exception e)
							{
								log.info("Test");
								e.printStackTrace();
								jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
								jsonObject.put("message", "Source wallet credit was not successful");
								return jsonObject;
							}
							JSONObject response = walletResponse.getJSONObject("response");
							Integer status = walletResponse.getInt("status");
							if(status!=200)
							{
								jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
								jsonObject.put("message", "Source wallet credit was not successful");
								return jsonObject;
							}
							JSONObject responseHeader = response.getJSONObject("tekHeader");
							log.info(response.toString());
							
							String operationStatus = responseHeader.getString("status");
							if(operationStatus.equals("FAIL"))
							{
								jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
								JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
								Iterator<String> keys = msgList.keys();
								String errorMessage = null;
								while(keys.hasNext())
								{
									String ky = keys.next();
									errorMessage = msgList.getString(ky);
								}
								jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
							}
							else if(operationStatus.equals("SUCCESS"))
							{
								
								String bankPaymentReference = responseHeader.getString("tekesbrefno");
								
								jsonObject.put("transactionId", transaction.getId());
								Customer customer = sourceAccount.getCustomer();
								jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
								jsonObject.put("customerNumber", customer.getVerificationNumber());
								jsonObject.put("customerId", customer.getId());
								jsonObject.put("accountIdentifier", sourceAccount.getAccountIdentifier());
								jsonObject.put("status", ERROR.WALLET_CREDIT_SUCCESS);
								jsonObject.put("charges", charges);
								jsonObject.put("amount", amount);
								jsonObject.put("bankReference", bankPaymentReference);
								jsonObject.put("message", "Wallet credit was successful");
							
							}
							else
							{
								jsonObject.put("status", ERROR.WALLET_CREDIT_FAILED);
								jsonObject.put("message", "Wallet credit was not successful");
							}
						}
					}
				}
			}
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	public static JSONObject verifyZICBExistCustomerByMobileNumberOrIdNo(SwpService swpService, 
			String logId, String token, String merchantCode, String deviceCode, String firstName, 
			String lastName, String addressLine1, String addressLine2, String addressLine3, String addressLine4, 
			String addressLine5, String uniqueType, String uniqueValue, String dateOfBirth, String email, 
			String sex, String mobileNumber, String accType, String currency, String idFront, String idBack, 
			String custImg, String custSig, Acquirer acquirer, Long parentCustomerId, Long parentAccountId, 
			String customerVerificationNumber, District district, Integer isSettlementAccount, String verifyType) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
			jsonObject.put("message", "System error. We experienced some issues. Please give it a try again");
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			log.info("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			//String branch_code = verifyJ.getString("branchCode");
			//log.info("branch_code ==" + branch_code);
			Device device = null;
			Merchant merchant = null;
			if(merchantCode!=null && deviceCode!=null)
			{
				String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
				
	
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				device = (Device)swpService.getUniqueRecordByHQL(hql);
				
				
			}
			
			String hql = "Select tp.* from accounts tp, customers c where tp.customer_id = c.id AND c.contactMobile = '"+mobileNumber+"' AND "
					+ "tp.acquirer_id = " + acquirer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + device.getSwitchToLive();
			if(isSettlementAccount!=null && isSettlementAccount==1)
				hql = hql + " AND tp.accountType = " + AccountType.DEVICE_SETTLEMENT.ordinal();
			else
				hql = hql + " AND tp.accountType = " + AccountType.VIRTUAL.ordinal();
			
			
			List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			if(accounts!=null && accounts.size()>0)
			{
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a Bevura Wallet.");
				return jsonObject;
			}
			
			
			
			if(device==null)
			{
				jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
				jsonObject.put("message", "Device code Invalid");
				return jsonObject;
			}
			if(merchant==null)
			{
				jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
				jsonObject.put("message", "Merchant Code Invalid");
				return jsonObject;
			}
			
			Date dob = null;
			
			JSONObject parameters = new JSONObject();
			if(verifyType!=null && verifyType.equalsIgnoreCase("MOBILE_NUMBER"))
				parameters.put("service", UtilityHelper.ZICB_CHECK_CUSTOMER_EXIST_BY_MOBILE_SERVICE_CODE);
			if(verifyType!=null && verifyType.equalsIgnoreCase("MEANS_OF_ID_NUMBER"))
				parameters.put("service", UtilityHelper.ZICB_CHECK_CUSTOMER_EXIST_BY_IDNO_SERVICE_CODE);
			
			JSONObject parametersRequest = new JSONObject();
			if(verifyType!=null && verifyType.equalsIgnoreCase("MOBILE_NUMBER"))
				parametersRequest.put("mobileNo", mobileNumber);
			if(verifyType!=null && verifyType.equalsIgnoreCase("MEANS_OF_ID_NUMBER"))
			{
				parametersRequest.put("nrc", uniqueValue);
			}
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			log.info(parameters.toString());
			log.info(header.toString());
			log.info("Test");
			String newWalletResponse = null;
			//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
			{
				header.put("authKey", device.getZicbAuthKey());

				//header.put("authKey", acquirer.getAuthKey());
				log.info(device.getZicbAuthKey());
				log.info(acquirer.getAccountCreationEndPoint()); 
				log.info("Live Auth Key..." + device.getZicbAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationEndPoint(), parameters.toString(), header);
				
				//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				header.put("authKey", device.getZicbDemoAuthKey());
				//header.put("authKey", acquirer.getDemoAuthKey());
				log.info(device.getZicbDemoAuthKey());
				log.info(acquirer.getAccountCreationDemoEndPoint());
				log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationDemoEndPoint(), parameters.toString(), header);
			}
			log.info(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "We could not verify if this customer exist on our platform");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
			}
			catch(Exception e)
			{
				log.info("Test");
				e.printStackTrace();

				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "We could not verify if this customer exist on our platform");
				return jsonObject;
			}
			JSONObject response = walletResponse.getJSONObject("response");
			Integer status = walletResponse.getInt("status");
			if(status!=200)
			{

				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "We could not verify if this customer exist on our platform");
				return jsonObject;
			}
			JSONObject responseHeader = response.getJSONObject("tekHeader");
			log.info(response.toString());
			
			if(verifyType!=null && verifyType.equalsIgnoreCase("MOBILE_NUMBER"))
			{
				if(response.get("custAccDetails")==null)
					log.info("nullable cust");
				else
					log.info("non nullable cust");
			}
			String operationStatus = responseHeader.getString("status");
			if(operationStatus.equals("FAIL"))
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
				Iterator<String> keys = msgList.keys();
				String errorMessage = null;
				while(keys.hasNext())
				{
					String ky = keys.next();
					errorMessage = msgList.getString(ky);
				}
				jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
			}
			else if(operationStatus.equals("SUCCESS"))
			{
				if(verifyType!=null && verifyType.equalsIgnoreCase("MOBILE_NUMBER"))
				{
					JSONArray customer__ = response.get("custAccDetails")!=null ? response.getJSONArray("custAccDetails") : null;
					
					if(customer__==null)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not verify if this customer exist on our platform");
					}
					else
					{	
						if(customer__.length()==0)
						{
							jsonObject.put("status", ERROR.CUSTOMER_NOT_FOUND);
							jsonObject.put("message", "Customer not found.");
						}
						else
						{
							jsonObject.put("status", ERROR.CUSTOMER_WALLET_EXISTS);
							jsonObject.put("message", "Customer found.");
							jsonObject.put("customerWalletList", customer__);
							JSONArray formattedWalletList = new JSONArray();
							JSONArray customer__1 = new JSONArray();
							for(int k1=0; k1<customer__.length(); k1++)
							{
								JSONObject jo = customer__.getJSONObject(k1);

								String branch = jo.getString("branch");
								String accTypeOfBank = jo.getString("accType");
								if(accTypeOfBank.equals("SAVINGS") && branch.equals("101"))
								{
									JSONObject jo_ = new JSONObject();
									jo_.put("custNo", jo.get("custNo"));
									jo_.put("nationalId", jo.get("uniqueIdVal"));
									jo_.put("mobileNo", jo.get("mobileNo"));
									jo_.put("accountNo", jo.get("accountNo"));
									formattedWalletList.put(jo_);
									customer__1.put(jo);
								}
							}
							
							if(formattedWalletList.length()==0)
							{
								jsonObject.put("status", ERROR.CUSTOMER_NOT_FOUND);
								jsonObject.put("message", "Customer not found.");
								jsonObject.put("customerWalletList", customer__1);
							}
							else
							{
								jsonObject.put("status", ERROR.CUSTOMER_WALLET_EXISTS);
								jsonObject.put("message", "Customer found.");
								jsonObject.put("customerWalletList", customer__1);
							}
							//jsonObject.put("message", "You already have a wallet previously created for you.");
						}

					
						
					}
				}
				if(verifyType!=null && verifyType.equalsIgnoreCase("MEANS_OF_ID_NUMBER"))
				{
					
					JSONArray customer__ = response.get("customerDetails")!=null ? response.getJSONArray("customerDetails") : null;
					if(customer__==null)
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "We could not verify if this customer exist on our platform");
					}
					else
					{	
						if(customer__.length()==0)
						{
							jsonObject.put("status", ERROR.CUSTOMER_NOT_FOUND);
							jsonObject.put("message", "Customer not found.");
						}
						else
						{
							
							JSONArray formattedWalletList = new JSONArray();
							JSONArray customer__1 = new JSONArray();
							for(int k1=0; k1<customer__.length(); k1++)
							{
								JSONObject jo = customer__.getJSONObject(k1);
								String customerType = jo.getString("customerType");
								if(customerType.equals("WALLET"))
								{
									JSONObject jo_ = new JSONObject();
									jo_.put("custNo", jo.get("custNo"));
									jo_.put("nationalId", jo.get("uniqueIdVal"));
									jo_.put("mobileNo", jo.get("mobileNo"));
									formattedWalletList.put(jo_);
									customer__1.put(jo);
								}
							}
							
							if(formattedWalletList.length()==0)
							{
								jsonObject.put("status", ERROR.CUSTOMER_NOT_FOUND);
								jsonObject.put("message", "Customer found.");
								jsonObject.put("customerWalletList", customer__1);
							}
							else
							{
								jsonObject.put("status", ERROR.CUSTOMER_WALLET_EXISTS);
								jsonObject.put("message", "Customer found.");
								jsonObject.put("customerWalletList", customer__1);
							}
							//jsonObject.put("message", "You already have a wallet previously created for you.");
						}

					
						
					}
						
				}
					
					
					
					
					
				
			}
			else
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}
	
	

	public static String[] generateWalletOTP(SwpService swpService, JSONObject js_, Account account, ECard card, String mobileno,
			String email, Acquirer acquirer, Device device) {
		// TODO Auto-generated method stub
		try
		{
			account.setOtp(RandomStringUtils.randomNumeric(6));
			account.setOtp("111111");
			account.setOtpRef(RandomStringUtils.randomAlphanumeric(8).toUpperCase());
			swpService.updateRecord(account);
			
			
			String[] resp = new String[4];
			
			log.info("Inside success");
			String otp = account.getOtp();
			String otpref = account.getOtpRef();
			resp[0] = "1";
			resp[1] = null;
			resp[2] = otp;
			resp[3] = otpref;
			return resp;
		}
		catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	public static String[] getCurrentBankBranches(String token, Application app, Acquirer acquirer, Device device, JSONObject jsonObject) {
		// TODO Auto-generated method stub
		try
		{
			//email  = "smicer66@gmail.com";
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_BANK_BRANCH_LIST);
			JSONObject parametersRequest = new JSONObject();
			
			parameters.put("request", parametersRequest);
			
			log.info("parameters...." + parameters.toString());
			JSONObject header = new JSONObject();
			
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(Boolean.TRUE))
				header.put("authKey", device.getZicbAuthKey());
			else 
				header.put("authKey", device.getZicbDemoAuthKey());
			
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			
			log.info(header.toString());
			
			String newWalletResponse = null;
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(Boolean.TRUE))
			{
				log.info(acquirer.getFundsTransferEndPoint());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferEndPoint(), parameters.toString(), header);
				log.info(newWalletResponse);
			}
			else
			{
				log.info(acquirer.getFundsTransferDemoEndPoint());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getFundsTransferDemoEndPoint(), parameters.toString(), header);
				log.info(newWalletResponse);
			}
			
			
			
			String[] resp = new String[4];
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
				jsonObject.put("message", "No bank branches found");
				resp[0] = "0";
				resp[1] = jsonObject.toString();
				resp[2] = null;
				resp[3] = null;
				return resp;
			}
			JSONObject walletResponse = new JSONObject(newWalletResponse);
			if(walletResponse.has("response"))
			{
				JSONObject response = walletResponse.getJSONObject("response");
				Integer status = walletResponse.getInt("status");
				if(status!=200)
				{
					jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
					jsonObject.put("message", "No bank branches found");
					resp[0] = "0";
					resp[1] = jsonObject.toString();
					resp[2] = null;
					resp[3] = null;
					return resp;
				}
				
				if(response.has("tekHeader"))
				{
					JSONObject tekHeader = response.getJSONObject("tekHeader");
					String headerStatus = tekHeader.getString("status");
					
					if(headerStatus!=null && headerStatus.toLowerCase().equals("success"))
					{
						log.info("Inside success");
						//log.info(response.toString());
						JSONArray bankList = response.getJSONArray("bankList");
						log.info(bankList.toString());
						resp[0] = "1";
						resp[1] = null;
						resp[2] = bankList.toString();
						resp[3] = null;
						
						
						return resp;
					}
					else
					{
						log.info("Inside not success");
						jsonObject.put("status", ERROR.GENERAL_SYSTEM_FAIL);
						jsonObject.put("message", "No bank branches found");
						resp[0] = "0";
						resp[1] = jsonObject.toString();
						resp[2] = null;
						resp[3] = null;
						return resp;
					}
				}
			}
			
			
			
			
			log.info("walletResponse has no response");
			jsonObject.put("status", ERROR.OTP_GENERATE_FAIL);
			jsonObject.put("message", "An OTP could not be sent for your transaction. Please try again.");
			resp[0] = "0";
			resp[1] = jsonObject.toString();
			resp[2] = null;
			resp[3] = null;
			return resp;
			
		}
		catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	public static JSONArray amortizationScheduleForMonthly(Double principal, Double monthlyInterestRate, Integer numMonths, Date startDate) throws JSONException
    {
		Double interestPaid, principalPaid, newBalance;
		Double monthlyPayment;
		Integer month;
		JSONArray jsonArray = new JSONArray();
		monthlyPayment      = monthlyPayment(principal, monthlyInterestRate, numMonths);

		String[] colors = new String[6];
		colors[0] = "#FF5733";
		colors[1] = "#FCFF33";
		colors[2] = "#33FF64";
		colors[3] = "#3361FF";
		colors[4] = "#FF33E6";
		colors[5] = "#FF3333";
		int i1 = 0;
		
		for (month = 1; month <= numMonths; month++) {
			// Compute amount paid and new balance for each payment period
			int indx = new Random().nextInt(6-0) + 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.MONTH, (month));
			Date dateDue = calendar.getTime();
			interestPaid  = principal      * (monthlyInterestRate / 100);
			principalPaid = monthlyPayment - interestPaid;
			newBalance    = principal      - principalPaid;
			principal = newBalance;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("month", month);
			jsonObject.put("dateDue", dateDue);
			jsonObject.put("dateDueReal", dateDue);
			jsonObject.put("interestPaid", interestPaid);
			jsonObject.put("principalPaid", principalPaid);
			jsonObject.put("iconBgColor", colors[indx]);
			jsonObject.put("bgColor", i1%2==0 ? "#E2F5D6" : "");
			jsonArray.put(jsonObject);
			i1++;

		}
		return jsonArray;
	}
	
	
	
	
	public static JSONArray amortizationScheduleForWeekly(Double principal, Double weeklyInterestRate, Integer numWeeks, Date startDate) throws JSONException
    {
		Double interestPaid, principalPaid, newBalance;
		JSONArray jsonArray = new JSONArray();
		Double weeklyPayment      = weeklyPayment(principal, weeklyInterestRate, numWeeks);

		String[] colors = new String[6];
		colors[0] = "#FF5733";
		colors[1] = "#FCFF33";
		colors[2] = "#33FF64";
		colors[3] = "#3361FF";
		colors[4] = "#FF33E6";
		colors[5] = "#FF3333";
		int i1 = 0;
		
		for (int week = 1; week <= numWeeks; week++) {
			// Compute amount paid and new balance for each payment period
			int indx = new Random().nextInt(6-0) + 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.DAY_OF_YEAR, (week*7));
			Date dateDue = calendar.getTime();
			interestPaid  = principal      * (weeklyInterestRate / 100);
			principalPaid = weeklyPayment - interestPaid;
			newBalance    = principal      - principalPaid;
			principal = newBalance;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("week", week);
			jsonObject.put("dateDue", dateDue);
			jsonObject.put("dateDueReal", dateDue);
			jsonObject.put("interestPaid", interestPaid);
			jsonObject.put("principalPaid", principalPaid);
			jsonObject.put("iconBgColor", colors[indx]);
			jsonObject.put("bgColor", i1%2==0 ? "#E2F5D6" : "");
			jsonArray.put(jsonObject);
			i1++;
			
		}
		return jsonArray;
	}
	
	public static JSONArray amortizationScheduleForDaily(Double principal, Double dailyInterestRate, Integer numDays, Date startDate) throws JSONException
    {
		Double interestPaid, principalPaid, newBalance;
		JSONArray jsonArray = new JSONArray();
		Double weeklyPayment      = dailyPayment(principal, dailyInterestRate, numDays);

		String[] colors = new String[6];
		colors[0] = "#FF5733";
		colors[1] = "#FCFF33";
		colors[2] = "#33FF64";
		colors[3] = "#3361FF";
		colors[4] = "#FF33E6";
		colors[5] = "#FF3333";
		int i1 = 0;
		for (int day = 1; day <= numDays; day++) {
			// Compute amount paid and new balance for each payment period
			int indx = new Random().nextInt(6-0) + 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.DAY_OF_YEAR, (day));
			Date dateDue = calendar.getTime();
			interestPaid  = principal      * (dailyInterestRate / 100);
			principalPaid = weeklyPayment - interestPaid;
			newBalance    = principal      - principalPaid;
			principal = newBalance;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("day", day);
			jsonObject.put("dateDue", new SimpleDateFormat("yyyy MMM dd").format(dateDue));
			jsonObject.put("dateDueReal", dateDue);
			jsonObject.put("interestPaid", interestPaid);
			jsonObject.put("principalPaid", principalPaid);
			jsonObject.put("iconBgColor", colors[indx]);
			jsonObject.put("bgColor", i1%2==0 ? "#E2F5D6" : "");
			jsonArray.put(jsonObject);
			i1++;
			
		}
		return jsonArray;
	}
	
	public static JSONArray amortizationScheduleForYearly(Double principal, Double yearlyInterestRate, Integer numDays, Date startDate) throws JSONException
    {
		Double interestPaid, principalPaid, newBalance;
		JSONArray jsonArray = new JSONArray();
		Double yearlyPayment      = yearlyPayment(principal, yearlyInterestRate, numDays);

		String[] colors = new String[6];
		colors[0] = "#FF5733";
		colors[1] = "#FCFF33";
		colors[2] = "#33FF64";
		colors[3] = "#3361FF";
		colors[4] = "#FF33E6";
		colors[5] = "#FF3333";
		int i1 = 0;
		
		for (int yr = 1; yr <= numDays; yr++) {
			// Compute amount paid and new balance for each payment period
			int indx = new Random().nextInt(6-0) + 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(startDate);
			calendar.add(Calendar.YEAR, (yr));
			Date dateDue = calendar.getTime();
			interestPaid  = principal      * (yearlyInterestRate / 100);
			principalPaid = yearlyPayment - interestPaid;
			newBalance    = principal      - principalPaid;
			principal = newBalance;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("year", yr);
			jsonObject.put("dateDue", dateDue);
			jsonObject.put("dateDueReal", dateDue);
			jsonObject.put("interestPaid", interestPaid);
			jsonObject.put("principalPaid", principalPaid);
			jsonObject.put("iconBgColor", colors[indx]);
			jsonObject.put("bgColor", i1%2==0 ? "#E2F5D6" : "");
			jsonArray.put(jsonObject);
			i1++;
			
		}
		return jsonArray;
	}
	

	static double yearlyPayment(Double loanAmount, Double yearlyInterestRate, Integer numberOfYears) {
        yearlyInterestRate /= 100;  // e.g. 5% => 0.05
        return loanAmount * yearlyInterestRate /
                ( 1 - 1 / Math.pow(1 + yearlyInterestRate, numberOfYears) );
    }

	static double dailyPayment(Double loanAmount, Double dailyInterestRate, Integer numberOfDays) {
        dailyInterestRate /= 100;  // e.g. 5% => 0.05
        return loanAmount * dailyInterestRate /
                ( 1 - 1 / Math.pow(1 + dailyInterestRate, numberOfDays) );
    }
	
	static double weeklyPayment(Double loanAmount, Double weeklyInterestRate, Integer numberOfWeeks) {
        weeklyInterestRate /= 100;  // e.g. 5% => 0.05
        return loanAmount * weeklyInterestRate /
                ( 1 - 1 / Math.pow(1 + weeklyInterestRate, numberOfWeeks) );
    }
	
	static double monthlyPayment(double loanAmount, double monthlyInterestRate, int numberOfYears) {
        monthlyInterestRate /= 100;  // e.g. 5% => 0.05
        return loanAmount * monthlyInterestRate /
                ( 1 - 1 / Math.pow(1 + monthlyInterestRate, numberOfYears * 12) );
    }
	
	
	public static JSONObject createTxnDetails(String billType, String vendor, String receipient, String accountNo,
			String orderRef, Date created_at, String currency, Double amount, String status, JSONObject breakdown) {
		// TODO Auto-generated method stub
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM d, yyyy - h:m a");
		
		JSONObject js = new JSONObject();
		js.put("billType", billType);
		js.put("vendor", vendor);
		js.put("receipient", receipient);
		js.put("accountNo", accountNo);
		js.put("orderRef", orderRef);
		js.put("created_at", sdf1.format(created_at));
		js.put("currency", currency);
		js.put("amount", amount);
		js.put("status", status);
		js.put("breakdown", breakdown);
		
		return js;
		
	}

	public static JSONObject addWalletToCustomer(SwpService swpService, String logId, String token, String merchantCode,
			String deviceCode, String firstName, String lastName, Acquirer acquirer, Customer customer, String custNo,
			Integer isSettlementAccount, Long parentCustomerId, Long parentAccountId, String currency, Integer isTokenize) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
			jsonObject.put("message", "System error. We experienced some issues. Please give it a try again");
			Application app = Application.getInstance(swpService);
			String bankKey = UtilityHelper.getBankKey("PROBASEWALLET", swpService);
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
			log.info("verifyJ ==" + verifyJ.toString());
			String acquirerCode = verifyJ.getString("acquirerCode");
			log.info("acquirerCode ==" + acquirerCode);
			//String branch_code = verifyJ.getString("branchCode");
			//log.info("branch_code ==" + branch_code);
			
			Device device = null;
			Merchant merchant = null;
			int isLive = 0;
			if(merchantCode!=null && deviceCode!=null)
			{
				String hql = "Select tp from Merchant tp where tp.merchantCode = '"+merchantCode+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				merchant = (Merchant)swpService.getUniqueRecordByHQL(hql);
				
				if(merchant==null)
				{
					jsonObject.put("status", ERROR.MERCHANT_EXIST_FAIL);
					jsonObject.put("message", "Merchant Code Invalid");
					return jsonObject;
				}
				
	
				hql = "Select tp from Device tp where tp.deviceCode = '"+deviceCode+"' AND tp.merchant.id = '"+merchant.getId()+"' AND tp.deleted_at IS NULL";
				log.info(hql);
				device = (Device)swpService.getUniqueRecordByHQL(hql);
				
				if(device==null)
				{
					jsonObject.put("status", ERROR.DEVICE_EXIST_FAIL);
					jsonObject.put("message", "Device code Invalid");
					return jsonObject;
				}
				
				if(device!=null && device.getSwitchToLive().equals(1))
				{
					isLive = 1;
				}
			}
			else
			{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete request. Please provide valid information");
				return jsonObject;
			}
			
			String mobileNumber = customer.getContactMobile();
			String hql = "Select tp.* from accounts tp, customers c where tp.customer_id = c.id AND c.contactMobile = '"+mobileNumber+"' AND "
					+ "tp.acquirer_id = " + acquirer.getId() + " AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
			if(isSettlementAccount!=null && isSettlementAccount==1)
				hql = hql + " AND tp.accountType = " + AccountType.DEVICE_SETTLEMENT.ordinal();
			else
				hql = hql + " AND tp.accountType = " + AccountType.VIRTUAL.ordinal();
			
			
			List<Map<String, Object>> accounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			if(accounts!=null && accounts.size()>0)
			{
				jsonObject.put("status", ERROR.WALLET_ALREADY_EXISTS);
				jsonObject.put("message", "You already have a wallet in " + acquirer.getBank().getBankName());
				return jsonObject;
			}
			
			Customer corporateCustomer = null;
			Account corporateCustomerAccount = null;
			
			if(parentCustomerId!=null && parentAccountId!=null)
			{
				hql = "Select tp from Customer tp where tp.id = "+parentCustomerId+" AND tp.deleted_at IS NULL";
				log.info(hql);
				corporateCustomer = (Customer)swpService.getUniqueRecordByHQL(hql);
				
				hql = "Select tp from Account tp where tp.id = "+parentAccountId+" AND tp.deleted_at IS NULL AND tp.isLive = " + isLive;
				log.info(hql);
				corporateCustomerAccount = (Account)swpService.getUniqueRecordByHQL(hql);
			}
			
			
			
			Date dob = null;
			
			JSONObject parameters = new JSONObject();
			parameters.put("service", UtilityHelper.ZICB_ADD_WALLET_SERVICE_CODE);
			JSONObject parametersRequest = new JSONObject();
			parametersRequest.put("customerId", custNo);
			parametersRequest.put("accType", "Wallet Account");
			parametersRequest.put("brnCode", device.getSwitchToLive()==1 ? acquirer.getBank().getLiveBankCode() : acquirer.getBank().getBankCode());
			parametersRequest.put("accountName", firstName + " " + lastName);
			parameters.put("request", parametersRequest);
			JSONObject header = new JSONObject();
			header.put("Content-Type", "application/json; utf-8");
			header.put("Accept", "application/json");
			log.info(parameters.toString());
			log.info(header.toString());
			log.info("Test");
			log.info(acquirer.getAuthKey());
			String newWalletResponse = null;
			//if(acquirer.getIsLive()!=null && acquirer.getIsLive().equals(Boolean.TRUE))
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(1))
			{
				//header.put("authKey", acquirer.getAuthKey());
				header.put("authKey", device.getZicbAuthKey());
				log.info(acquirer.getAccountCreationEndPoint()); 
				log.info("Live Auth Key..." + device.getZicbAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationEndPoint(), parameters.toString(), header);
				
				//newWalletResponse = "{\"errorList\":{},\"operation_status\":\"SUCCESS\",\"preauthUUID\":\"d013bf59-ade1-4073-ac69-49c8e44da236\",\"request\":{\"code\":\"ZB0631\",\"accType\":\"WA\",\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"currency\":\"ZMW\",\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\"},\"request-reference\":\"2020198-ZICB-1597845917\",\"response\":{\"cust\":{\"accNos\":{\"1019000001549\":{\"accDesc\":\"Wallet Account\",\"accNo\":1019000001549,\"accStatus\":\"A\",\"accType\":\"WA\",\"avlBal\":0,\"branchCode\":\"101\",\"createdAt\":1597845917540,\"curBal\":0,\"currency\":\"ZMW\",\"idCustomer\":9000508,\"updatedAt\":1597845917540}},\"add1\":\"Plot 38 Luswata Close\",\"add2\":\"Roma\",\"add3\":\"LUSAKA DISTRICT\",\"add4\":\"LUSAKA\",\"add5\":\"Zambia\",\"createdAt\":1597845917538,\"custImg\":null,\"custSig\":null,\"dateOfBirth\":null,\"email\":\"smicer66@gmail.com\",\"firstName\":\"Kachi\",\"idBack\":null,\"idCustomer\":9000508,\"idFront\":null,\"lastName\":\"Akujua\",\"mobileNumber\":\"260967307151\",\"sex\":\"M\",\"status\":\"A\",\"uniqueType\":\"NRC\",\"uniqueValue\":\"1892000-0019-19\",\"updatedAt\":1597845917538},\"tekHeader\":{\"errList\":{},\"hostrefno\":null,\"msgList\":{\"WA-CUS1\":\"Customer creation successful \"},\"status\":\"SUCCESS\",\"tekesbrefno\":\"902ae12e-3cf8-7e99-0304-6d497a37d2e8\",\"username\":\"TEKESBRETAIL\",\"warnList\":{}}},\"status\":200,\"timestamp\":1597845917623}";
			}
			else
			{
				//header.put("authKey", acquirer.getDemoAuthKey());
				header.put("authKey", device.getZicbDemoAuthKey());
				log.info(acquirer.getAccountCreationDemoEndPoint());
				log.info("Demo Auth Key..." + device.getZicbDemoAuthKey());
				newWalletResponse = UtilityHelper.sendPost(acquirer.getAccountCreationDemoEndPoint(), parameters.toString(), header);
			}
			log.info(newWalletResponse);
			
			if(newWalletResponse==null)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			
			JSONObject walletResponse = null;
			try
			{
				walletResponse = new JSONObject(newWalletResponse);
			}
			catch(Exception e)
			{
				log.info("Test");
				e.printStackTrace();
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject response = walletResponse.getJSONObject("response");
			Integer status = walletResponse.getInt("status");
			if(status!=200)
			{
				jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT);
				jsonObject.put("message", "Your new customer wallet could not be created.");
				return jsonObject;
			}
			JSONObject responseHeader = response.getJSONObject("tekHeader");
			log.info(response.toString());
			if(response.get("acc")==null)
				log.info("nullable acc");
			else
				log.info("non nullable acc");
			String operationStatus = responseHeader.getString("status");
			if(operationStatus.equals("FAIL"))
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				JSONObject msgList = responseHeader.getJSONObject("msgList");//1019000001577
				Iterator<String> keys = msgList.keys();
				String errorMessage = null;
				while(keys.hasNext())
				{
					String ky = keys.next();
					errorMessage = msgList.getString(ky);
				}
				jsonObject.put("message", "" + (errorMessage!=null ? ("" + errorMessage) : ""));
			}
			else if(operationStatus.equals("SUCCESS"))
			{
				JSONObject accNo = response.get("acc")!=null ? response.getJSONObject("acc") : null;
				if(customer!=null)
				{
					
					Long accNo_1 = accNo.getLong("accNo");
					String accNo_ = Long.toString(accNo_1.longValue());
					log.info("accNo..." + accNo_);
		
					
					
					/*Customer customer = new Customer();
					customer.setAddressLine1(addressLine1);
					customer.setAddressLine2(addressLine2);
					customer.setContactEmail(email);
					customer.setAltContactMobile(null);
					customer.setContactMobile(mobileNumber);
					customer.setDateOfBirth(dob);
					customer.setFirstName(firstName);
					customer.setGender(sex==null ? null : Gender.valueOf(sex=="M" ? "MALE" : "FEMALE"));
					customer.setLastName(lastName);
					customer.setVerificationNumber(acctDetails.getInt("idCustomer") + "");
					customer.setStatus(CustomerStatus.ACTIVE);
					customer.setCreated_at(new Date());
					customer.setCustomerType(CustomerType.INDIVIDUAL);
					customer.setCustomerImage(custImg);
					customer.setLocationDistrict(district);
					customer.setMeansOfIdentificationType(MeansOfIdentificationType.valueOf(uniqueType));
					customer.setMeansOfIdentificationNumber(uniqueValue);
					customer = (Customer)swpService.createNewRecord(customer);*/
					
					jsonObject.put("customerNumber", customer.getVerificationNumber());

					
					hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.isLive = " + device.getSwitchToLive();
					Collection<Account> customerAccountList = (Collection<Account>)swpService.getAllRecordsByHQL(hql);
					Account account = null;
					String branch_code = device.getSwitchToLive()==1 ? acquirer.getBank().getLiveBankCode() : acquirer.getBank().getBankCode();
					if(isSettlementAccount!=null && isSettlementAccount==1)
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.DEVICE_SETTLEMENT, accNo_, 
								customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, ProbasePayCurrency.valueOf(currency), 
								null, null, accountScheme, isLive);
					}
					else
					{
						JSONObject allSettings = app.getAllSettings();
						String defaultAccountSchemeIdObj = allSettings.getString("defaultaccountscheme");
						CardScheme accountScheme = null;
						if(defaultAccountSchemeIdObj!=null)
						{
							Long defaultAccountSchemeId = Long.parseLong(defaultAccountSchemeIdObj);
							accountScheme = (CardScheme)swpService.getRecordById(CardScheme.class, defaultAccountSchemeId);
						}
						account = new Account(customer, com.probase.probasepay.enumerations.AccountStatus.ACTIVE , null, branch_code, acquirer, currency, AccountType.VIRTUAL, accNo_, 
							customerAccountList==null ? 0 : customerAccountList.size(), corporateCustomer, parentCustomerId, corporateCustomerAccount, parentAccountId, 
							ProbasePayCurrency.valueOf(currency), null, null, accountScheme, isLive);
					}
					account = (Account)swpService.createNewRecord(account);
					String accountToken = null;
					log.info("Entering create Bevura Token");
					if(isTokenize!=null && isTokenize==1)
					{
						accountToken = UtilityHelper.tokenizeAccount(account);
						log.info("Account Token...." + accountToken);
						if(accountToken!=null)
						{
							BevuraToken bevuraToken = new BevuraToken(accountToken, account.getId(), null, merchant!=null ? merchant.getId() : null, 
									device!=null ? device.getId() : null, "ACCOUNT", account.getCustomer().getId(), 1, null, null, device.getSwitchToLive());
							bevuraToken = (BevuraToken)swpService.createNewRecord(bevuraToken);
							jsonObject.put("accountToken", bevuraToken.getToken());
						}
					}
					jsonObject.put("accountNo", accNo_);
					
					
					Transaction transaction = new Transaction();
					transaction.setChannel(Channel.WEB);
					transaction.setCreated_at(new Date());
					transaction.setAmount(0.00);
					transaction.setCustomerId(customer.getId());
					transaction.setFixedCharge(null);
					transaction.setMessageRequest(null);
					transaction.setServiceType(ServiceType.DEPOSIT_OTC);
					transaction.setStatus(TransactionStatus.SUCCESS);
					transaction.setPayerEmail(customer.getContactEmail());
					transaction.setPayerMobile(mobileNumber);
					transaction.setPayerName(lastName + ", " + firstName);
					transaction.setResponseCode(TransactionCode.transactionSuccess);
					transaction.setTransactionRef(RandomStringUtils.randomNumeric(10));
					transaction.setTransactionDate(new Date());
					transaction.setTransactionCode(TransactionCode.transactionSuccess);
					transaction.setAccount(account);
					transaction.setCreditAccountTrue(true);
					transaction.setCreditPoolAccountTrue(true);
					transaction.setTransactingBankId(account.getAcquirer().getBank().getId());
					transaction.setReceipientChannel(Channel.WEB);
					transaction.setTransactionDetail("Account Opening: Deposit " + 0.00 + " into Account #" + account.getAccountIdentifier());
					transaction.setReceipientEntityId(account.getId());
					transaction.setDevice(device);
					transaction.setProbasePayCurrency(ProbasePayCurrency.ZMW);
					if(merchant!=null)
						transaction.setMerchantId(merchant.getId());
					
					transaction.setIsLive(device.getSwitchToLive());
					transaction = (Transaction)swpService.createNewRecord(transaction);
					jsonObject.put("transactionId", transaction.getId());
					
					hql = "Select tp.* from accounts tp where tp.customer_id = " + customer.getId();
					List<Map<String, Object>> allaccounts = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("accounts", allaccounts==null ? null : allaccounts);
					jsonObject.put("walletExists", allaccounts!=null && accounts.size()>0 ? true : false);
					

					hql = "Select tp.* from ecards tp where tp.customerId = " + customer.getId() + " AND (tp.stopFlag IS NULL OR tp.stopFlag = 0)"
							+ " AND (tp.deleted_at IS NULL) AND tp.isLive = " + device.getSwitchToLive();
					List<Map<String, Object>> ecards = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
					jsonObject.put("ecards", ecards==null ? null : ecards);
					jsonObject.put("ecardExists", ecards!=null && ecards.size()>0 ? true : false);
				
					jsonObject.put("customerName", customer.getLastName() + " " + customer.getFirstName() + (customer.getOtherName()==null ? "" : (" " + customer.getOtherName())));
					jsonObject.put("customerNumber", customer.getVerificationNumber());
					jsonObject.put("customerId", customer.getId());
					jsonObject.put("accountId", account.getId());
					jsonObject.put("accountIdentifier", accNo_);
					if(isTokenize!=null && isTokenize==1)
					{
						jsonObject.put("accountToken", accountToken);
					}
					jsonObject.put("status", ERROR.CUSTOMER_CREATE_SUCCESS);
					jsonObject.put("message", "New Customer Wallet Created Successfully");
				}
				else
				{
					jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
					jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
				}
			}
			else
			{
				jsonObject.put("status", ERROR.WALLET_CREATION_FAIL);
				jsonObject.put("message", "New Customer Wallet Creation was not Successfully");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			e.printStackTrace();
		}
		
		return jsonObject;
	}


}
