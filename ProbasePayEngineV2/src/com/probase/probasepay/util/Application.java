package com.probase.probasepay.util;

import java.util.Date;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.models.Issuer;
import com.probase.probasepay.models.Bank;
import com.probase.probasepay.models.CardScheme;
import com.probase.probasepay.models.Country;
import com.probase.probasepay.models.District;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.MerchantScheme;
import com.probase.probasepay.models.Province;
import com.probase.probasepay.models.Setting;
import com.probase.probasepay.services.MerchantServices;

public class Application {

	
	

	private static Application instance;
	//private static Key key;
	private static JSONObject accessKeys;
	
	private Logger log = Logger.getLogger(Application.class);
	private ServiceLocator serviceLocator = ServiceLocator.getInstance();
	public SwpService swpService = null;
	private Collection<Bank> allBanks = null;
	private Collection<MerchantScheme> allMerchantSchemes = null;
	private Collection<CardScheme> allCardSchemes = null;
	private Collection<Province> allProvinces = null;
	private Collection<District> allDistricts = null;
	private Collection<Country> allCountries = null;
	private Collection<Acquirer> allAcquirers = null;
	private Collection<Issuer> allIssuers = null;

	private JSONObject allDeviceTypes = null;
	private JSONObject allSettings = null;

	public String cyberSourceUrl = "https://secureacceptance.cybersource.com/pay";
	public String demoCyberSourceUrl = "https://testsecureacceptance.cybersource.com/pay";
	/*private final String probaseBranchCode = "999";
	private final String countryCode = "026";
	private final String acquirerId = "PROBASE";
	private Double minimumBalance = 10.00;
	private Double minimumTransactionAmountWeb = 1.00;
	private Double maximumTransactionAmountWeb = 100001.00;*/
	//public String cyberSourceAccessKeyOld = "cyberSourceAccessKeyOld";
	//public String cyberSourceAccessKey = "cyberSourceAccessKey";
	//public String cyberSourceProfileIdTestOld = "cyberSourceProfileIdTestOld";
	//public String cyberSourceProfileId = "cyberSourceProfileId";
	/*public String cyberSourceSecretKeyOld = 
			"cyberSourceSecretKeyOld";*/
	/*public String cyberSourceSecretKey = "cyberSourceSecretKey";
	

	public String DemocyberSourceProfileId = "DemocyberSourceProfileId";
	public String DemocyberSourceAccessKey = "DemocyberSourceAccessKey";
	public String DemocyberSourceSecretKey = "DemocyberSourceSecretKey";
	
	public String cyberSourceLocale = "en-us";
	public String ubaMerchantId = "ubaMerchantId";
	public String ubaServiceKey = "ubaServiceKey";
	public String zambiaCurrencyCode = "894";
	public String ubaDemoURL = "https://ucollect.ubagroup.com/cipg-payportal/regptran";
	public String ubaLiveURL = "https://ucollect.ubagroup.com/cipg-payportal/regptran";*/
	public static final Integer BASE_LIST_COUNT = 100;
	public static final String PROBASEPAY_SERVICE_TYPE_ID_REVERSAL 				= "1981598619900";
	public static final String PROBASEPAY_SERVICE_TYPE_ID_GENERIC_ECOMMERCE 	= "1981511018900";
	public static final String PROBASEPAY_SERVICE_TYPE_SCHOOL_FEE_PAYMENT		= "1981598182741";
	public static final String ZICB_BANK_CODE = "3001";
	public static final String UAT_ZICB_BANK_CODE = "3001";
	public static final String BILLS_AUTH_KEY = "956a16e50a70fb56ca21074";
	
    private Application(SwpService swpService)
    {
        // shouldn't be instantiated
    	//setKey(MacProvider.generateKey());
    	//key.

    	try {
    		this.swpService = this.serviceLocator.getSwpService();
			String hql = "Select tp.* from settings tp where tp.status = " + 1;
			List<Map<String, Object>> settingList = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			Iterator<Map<String, Object>> it1 = settingList.iterator();
			JSONObject settingJSON = new JSONObject();
			while(it1.hasNext())
			{
				Map<String, Object> it2 = it1.next();
				settingJSON.put((String)it2.get("name"), (String)it2.get("value"));
			}
			/*JSONArray settingJSON = null;
			if(settingList.size()>0)
			{
				String str_ = new Gson().toJson(new ArrayList<Setting>(settingList));
				System.out.println("str_ ==> " + str_);
				settingJSON = new JSONArray(str_);
			}*/
			
			this.setAllSettings(settingJSON);
			
			/*Iterator<Setting> it = settingList.iterator();
			while(it.hasNext())
			{
				Setting set = (Setting)it.next();
				String val = set.getValue();
				if(set.getName().toLowerCase().equals("cyberSourceAccessKey".toLowerCase()))
				{
					val = "CyberSourceAccessKey";
					this.setCyberSourceAccessKey(val);
				}
				else if(set.getName().toLowerCase().equals("cyberSourceProfileId".toLowerCase()))
				{
					val = "CyberSourceProfileId";
					this.setCyberSourceProfileId(val);
				}
				else if(set.getName().toLowerCase().equals("cyberSourceSecretKey".toLowerCase()))
				{
					val = "Cybersourcesecretkey";
					this.setCybersourcesecretkey(val);
				}
				else if(set.getName().toLowerCase().equals("cyberSourceLocale".toLowerCase()))
				{
					this.setCybersourcelocale(val);
				}
				else if(set.getName().toLowerCase().equals("minimumBalance".toLowerCase()))
				{
					try{
						this.setMinimumBalance(Double.valueOf(val));}
					catch(NumberFormatException e){	}
				}
				else if(set.getName().toLowerCase().equals("minimumTransactionAmountWeb".toLowerCase()))
				{
					try{
						this.setMinimumTransactionAmountWeb(Double.valueOf(val));}
					catch(NumberFormatException e){	}
				}
				else if(set.getName().toLowerCase().equals("maximumTransactionAmountWeb".toLowerCase()))
				{
					try{
						this.setMaximumTransactionAmountWeb(Double.valueOf(val));}
					catch(NumberFormatException e){	}
				}
				else if(set.getName().toLowerCase().equals("paymentVendorCode".toLowerCase()))
				{
					this.setPaymentVendorCode(val);
				}
				else if(set.getName().toLowerCase().equals("methodOfPayment".toLowerCase()))
				{
					this.setMethodOfPayment(val);
				}
				else if(set.getName().toLowerCase().equals("currencyCode".toLowerCase()))
				{
					this.setCurrencyCode(val);
				}
				else if(set.getName().toLowerCase().equals("vendorCode".toLowerCase()))
				{
					this.setVendorCode(val);
				}
				else if(set.getName().toLowerCase().equals("language".toLowerCase()))
				{
					this.setLanguage(val);
				}
				else if(set.getName().toLowerCase().equals("dataSource".toLowerCase()))
				{
					this.setDataSource(val);
				}
				else if(set.getName().toLowerCase().equals("interfaceType".toLowerCase()))
				{
					this.setInterfaceType(val);
				}
				else if(set.getName().toLowerCase().equals("businessUnit".toLowerCase()))
				{
					this.setBusinessUnit(val);
				}
			}*/
			

			hql = "Select tp from Province tp order by tp.provinceName ASC";
			Collection<Province> provinceList = (Collection<Province>)swpService.getAllRecordsByHQL(hql);
			hql = "Select tp from CardScheme tp order by tp.schemeName ASC";
			Collection<CardScheme> cardSchemeList = (Collection<CardScheme>)swpService.getAllRecordsByHQL(hql);
			hql = "Select tp from District tp order by tp.name ASC";
			Collection<District> districtList = (Collection<District>)swpService.getAllRecordsByHQL(hql);
			hql = "Select tp from Country tp order by tp.name ASC";
			Collection<Country> countryList = (Collection<Country>)swpService.getAllRecordsByHQL(hql);
			hql = "Select tp from Acquirer tp order by tp.acquirerName ASC";
			Collection<Acquirer> acquirerList = (Collection<Acquirer>)swpService.getAllRecordsByHQL(hql);
			hql = "Select tp from Issuer tp order by tp.issuerName ASC";
			Collection<Issuer> issuerList = (Collection<Issuer>)swpService.getAllRecordsByHQL(hql);
			
			JSONObject jsonObject = new JSONObject();
			for(Iterator<Acquirer> itAcquirer = acquirerList.iterator(); itAcquirer.hasNext();)
			{
				Acquirer acquirer = itAcquirer.next();
				jsonObject.put(acquirer.getAcquirerCode(), acquirer.getAccessExodus()==null ? "" : acquirer.getAccessExodus());
			}
			jsonObject.put("PROBASE", "WMXGGHowzFdq0fpTg93pYmA5Wjuiq97l");
			jsonObject.put("PROBASEWALLET", "WMXGGHowzFdq0fpTg93pYmA5Wjuiq97l");
			//jsonObject.put("PROBASE", "y4GKrKbeMLrfTuy2jY3o8idIXATV97oz");
			//jsonObject.put("PROBASEWALLET", "y4GKrKbeMLrfTuy2jY3o8idIXATV97oz");
			System.out.println("AccessBank Keys = " + jsonObject.toString());
			
			this.setAllBanks((Collection<Bank>)swpService.getAllRecords(Bank.class));
			this.setAllMerchantSchemes((Collection<MerchantScheme>)swpService.getAllRecords(MerchantScheme.class));
			
			
			JSONObject dType = new JSONObject();
			for(DeviceType d : DeviceType.values())
			{
				dType.put(Integer.toString(d.ordinal()), d.name());
			}
			
			this.setAllDeviceTypes(dType);
			this.setAllCardSchemes(cardSchemeList);
			this.setAllProvinces(provinceList);
			this.setAllDistricts(districtList);
			this.setAllCountries(countryList);
			this.setAllAcquirers(acquirerList);
			this.setAllIssuers(issuerList);
			this.setAccessKeys(jsonObject);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			System.out.println(e.getMessage());
		}
    }

    /**
     * Gets the shared instance of this Class
     *
     * @return the shared service locator instance.
     */
    public static final Application getInstance(SwpService swpService)
    {
    	//System.out.println("Get Instance 1");
        if (instance == null)
        {
            instance = new Application(swpService);
        }
        return instance;
    }
    
    public static final Application getInstance(SwpService swpService, Boolean force)
    {
    	System.out.println("Get Instance 2");
        if (instance == null || force.equals(Boolean.TRUE))
        {
        	System.out.println("Force Reload of Application");
            instance = new Application(swpService);
        }
        return instance;
    }


    /*public String getCyberSourceAccessKey() {
		return cyberSourceAccessKey;
	}

	public String getCyberSourceProfileId() {
		return cyberSourceProfileId;
	}

	public String getCybersourcesecretkey() {
		return cyberSourceSecretKey;
	}

	public String getCybersourcelocale() {
		return cyberSourceLocale;
	}


    public void setCyberSourceAccessKey(String cyberSourceAccessKey) {
		this.cyberSourceAccessKey = cyberSourceAccessKey;
	}

	public void setCyberSourceProfileId(String cyberSourceProfileId) {
		this.cyberSourceProfileId = cyberSourceProfileId;
	}

	public void setCybersourcesecretkey(String cyberSourceSecretKey) {
		this.cyberSourceSecretKey = cyberSourceSecretKey;
	}

	public void setCybersourcelocale(String cyberSourceLocale) {
		this.cyberSourceLocale  = cyberSourceLocale;
	}*/
	
	
	public JSONObject getAccessKeys() {
		return accessKeys;
	}

	public void setAccessKeys(JSONObject accessKeys) {
		Application.accessKeys = accessKeys;
	}
	
	public static String getTokenKey()
	{
		//return "Yn7sWDar7yPZZh7xvHFpnRWRNcj1l8Rf";
		return "WMXGGHowzFdq0fpTg93pYmA5Wjuiq97l";
	}
	
	public String createJWT(String id, String acquirerCode, String branchCode, String subject, long ttlMillis)
	{
		try
		{
			SignatureAlgorithm signAlg = SignatureAlgorithm.HS256;
			
			long nowMillis = System.currentTimeMillis();
			Date date = new Date(nowMillis);
			//byte[] keyBytes = DatatypeConverter.parseBase64Binary(getTokenKey());
			byte[] keyBytes = DatatypeConverter.parseBase64Binary(getAccessKeys().getString(acquirerCode));
			Key signingKey = new SecretKeySpec(keyBytes, signAlg.getJcaName());
			
			JwtBuilder jwtBuilder = Jwts.builder().setId(id)
					.setIssuedAt(date)
					.setSubject(subject)
					.setIssuer(acquirerCode)
					.setAudience(branchCode)
					.signWith(signAlg, signingKey);
			
			
			
			if (ttlMillis >= 0) {
			    long expMillis = nowMillis + ttlMillis;
			    Date exp = new Date(expMillis);
			    jwtBuilder.setExpiration(exp);
			}
			
			return jwtBuilder.compact();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e);
			return null;
		}
		
	}
	
	public String createGuestJWT(String id, String subject, long ttlMillis)
	{
		SignatureAlgorithm signAlg = SignatureAlgorithm.HS256;
		
		long nowMillis = System.currentTimeMillis();
		Date date = new Date(nowMillis);
		byte[] keyBytes = DatatypeConverter.parseBase64Binary(getTokenKey());
		Key signingKey = new SecretKeySpec(keyBytes, signAlg.getJcaName());
		
		JwtBuilder jwtBuilder = Jwts.builder().setId(id)
				.setIssuedAt(date)
				.setSubject(subject)
				.signWith(signAlg, signingKey);
		
		
		
		if (ttlMillis >= 0) {
		    long expMillis = nowMillis + ttlMillis;
		    Date exp = new Date(expMillis);
		    jwtBuilder.setExpiration(exp);
		}
		
		return jwtBuilder.compact();
		
	}

	public Collection<Bank> getAllBanks() {
		return allBanks;
	}

	public void setAllBanks(Collection<Bank> allBanks) {
		this.allBanks = allBanks;
	}

	public Collection<MerchantScheme> getAllMerchantSchemes() {
		return allMerchantSchemes;
	}

	public void setAllMerchantSchemes(Collection<MerchantScheme> allMerchantSchemes) {
		this.allMerchantSchemes = allMerchantSchemes;
	}

	public JSONObject getAllDeviceTypes() {
		return allDeviceTypes;
	}

	public void setAllDeviceTypes(JSONObject deviceTypes) {
		this.allDeviceTypes = deviceTypes;
	}

	public Collection<CardScheme> getAllCardSchemes() {
		return allCardSchemes;
	}
	
	public Collection<Acquirer> getAllAcquirers()
	{
		return allAcquirers;
	}
	
	public Collection<Issuer> getAllIssuers()
	{
		return allIssuers;
	}

	public void setAllCardSchemes(Collection<CardScheme> allCardSchemes) {
		this.allCardSchemes = allCardSchemes;
	}
	
	public void setAllAcquirers(Collection<Acquirer> allAcquirers) {
		this.allAcquirers = allAcquirers;
	}

	public Collection<Province> getAllProvinces() {
		return allProvinces;
	}

	public void setAllProvinces(Collection<Province> allProvinces) {
		this.allProvinces = allProvinces;
	}

	public Collection<District> getAllDistricts() {
		return allDistricts;
	}

	public void setAllDistricts(Collection<District> allDistricts) {
		this.allDistricts = allDistricts;
	}

	/*public String getProbaseBranchCode() {
		return probaseBranchCode;
	}


	public String getCountryCode() {
		return countryCode;
	}

	public String getAcquirerId() {
		return acquirerId;
	}

	public Double getMinimumBalance() {
		return minimumBalance;
	}

	public Double getMinimumTransactionAmountWeb() {
		return minimumTransactionAmountWeb;
	}

	public Double getMaximumTransactionAmountWeb() {
		return maximumTransactionAmountWeb;
	}*/

	public JSONObject getAllSettings() {
		return allSettings;
	}

	public void setAllSettings(JSONObject allSettings) {
		this.allSettings = allSettings;
	}


	public Collection<Country> getAllCountries() {
		return allCountries;
	}

	public void setAllCountries(Collection<Country> allCountries) {
		this.allCountries = allCountries;
	}

	/*public String getBusinessUnit() {
		return businessUnit;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getInterfaceType() {
		return interfaceType;
	}

	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getVendorCode() {
		return vendorCode;
	}

	public void setVendorCode(String vendorCode) {
		this.vendorCode = vendorCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getMethodOfPayment() {
		return methodOfPayment;
	}

	public void setMethodOfPayment(String methodOfPayment) {
		this.methodOfPayment = methodOfPayment;
	}

	public String getPaymentVendorCode() {
		return paymentVendorCode;
	}

	public void setPaymentVendorCode(String paymentVendorCode) {
		this.paymentVendorCode = paymentVendorCode;
	}

	public void setMinimumBalance(Double minimumBalance) {
		this.minimumBalance = minimumBalance;
	}

	public void setMinimumTransactionAmountWeb(Double minimumTransactionAmountWeb) {
		this.minimumTransactionAmountWeb = minimumTransactionAmountWeb;
	}

	public void setMaximumTransactionAmountWeb(Double maximumTransactionAmountWeb) {
		this.maximumTransactionAmountWeb = maximumTransactionAmountWeb;
	}*/

	public void setAllIssuers(Collection<Issuer> allIssuers) {
		this.allIssuers = allIssuers;
	}
}
