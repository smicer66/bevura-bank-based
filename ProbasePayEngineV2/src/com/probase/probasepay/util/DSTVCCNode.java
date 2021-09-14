package com.probase.probasepay.util;


import com.dstvcc.etz.services.DSTVService;
import com.dstvcc.etz.services.SubmitPaymentResponseForPaymentBySmartCard;
import com.dstvcc.models.ArrayOfHardware;
import com.dstvcc.models.ArrayOfService;
import com.dstvcc.models.GetAvailableProducts;
import com.dstvcc.models.GetAvailableProductsResponse;
import com.dstvcc.models.GetCustomerDetailsByCustomerNumberResponse;
import com.dstvcc.models.GetCustomerDetailsByDeviceNumber;
import com.dstvcc.models.GetCustomerDetailsByDeviceNumberResponse;
import com.dstvcc.models.GetPrimaryProducts;
import com.dstvcc.models.GetPrimaryProductsResponse;
import com.dstvcc.models.GetProducts;
import com.dstvcc.models.GetProductsResponse;
import com.dstvcc.models.Hardware;
import com.dstvcc.models.Service;
import com.dstvcc.models.SubmitPayment;
import com.dstvcc.models.SubmitPaymentBySmartCard;
import com.dstvcc.models.SubmitPaymentBySmartCardResponse;
import com.dstvcc.models.SubmitPaymentResponse;
import com.google.gson.Gson;
import com.probase.probasepay.services.PaymentServicesV2;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;




/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class DSTVCCNode {

	private static Logger log = Logger.getLogger(PaymentServicesV2.class);

    public Logger getLog() {
        return log;
    }

    public String process(String json) {
        String ret = "";
        System.out.println(":: [QUERY RECEIVED]: " + json);
        try {
        	JSONObject jmap = new Gson().fromJson(json, JSONObject.class);
        	String key = (String)jmap.get("key");
        	String dataObject = (String)jmap.get("dataObject");
        	String response = "";
        	
        	if(key==null)
        	{
        		JSONObject jap = new JSONObject();
    			jap.put("error", "01");
    			jap.put("fault", "No operation key or dataObject provided in data");
    			
    			ret = new Gson().toJson(jap);
                System.out.println(":: [GET PRODUCTS]: " + ret);
                return ret;
        	}
        	
        	switch(key.toLowerCase())
        	{
        		case "getproducts":
        			GetProducts getProducts = new Gson().fromJson(dataObject, GetProducts.class);
        			response = new DSTVProcessor().handleGetProducts(getProducts);
        			break;
        		case "getcustomerdetailsbydevicenumber":
        			GetCustomerDetailsByDeviceNumber getCustomerDetailsByDeviceNumber = new Gson().fromJson(dataObject, GetCustomerDetailsByDeviceNumber.class);
        			response = new DSTVProcessor().handleGetCustomerDetailsByDeviceNumber(getCustomerDetailsByDeviceNumber);
        			break;
        		case "getprimaryproducts":
        			GetPrimaryProducts getPrimaryProducts = new Gson().fromJson(dataObject, GetPrimaryProducts.class);
        			response = new DSTVProcessor().handleGetPrimaryProducts(getPrimaryProducts);
        			break;
        		case "getavailableproducts":
        			GetAvailableProducts getAvailableProducts = new Gson().fromJson(dataObject, GetAvailableProducts.class);
        			response = new DSTVProcessor().handleGetAvailableProducts(getAvailableProducts);
        			break;
        		case "submitpaymentbysmartcard":
        			SubmitPaymentBySmartCard submitPaymentBySmartCard = new Gson().fromJson(dataObject, SubmitPaymentBySmartCard.class);
        			response = new DSTVProcessor().handleSubmitPaymentBySmartCard(submitPaymentBySmartCard);
        			break;
        		case "submitpayment":
        			SubmitPayment submitPayment = new Gson().fromJson(dataObject, SubmitPayment.class);
        			response = new DSTVProcessor().handleSubmitPayment(submitPayment);
        			break;
        	}
        	

        } catch (Exception e) {
            
            log.error("Process: ", e);
        }
        return ret;
    }

    


}
