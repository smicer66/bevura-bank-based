package com.probase.probasepay.util;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dstvcc.etz.services.DSTVService;
import com.dstvcc.etz.services.SubmitPaymentResponseForPaymentBySmartCard;
import com.dstvcc.models.GetAvailableProducts;
import com.dstvcc.models.GetAvailableProductsResponse;
import com.dstvcc.models.GetCustomerDetailsByCustomerNumberResponse;
import com.dstvcc.models.GetCustomerDetailsByDeviceNumber;
import com.dstvcc.models.GetPrimaryProducts;
import com.dstvcc.models.GetPrimaryProductsResponse;
import com.dstvcc.models.GetProducts;
import com.dstvcc.models.GetProductsResponse;
import com.dstvcc.models.SubmitPayment;
import com.dstvcc.models.SubmitPaymentBySmartCard;
import com.dstvcc.models.SubmitPaymentResponse;
import com.google.gson.Gson;

public class DSTVProcessor {

    
	public String handleSubmitPayment(SubmitPayment submitPayment) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
    	SubmitPaymentResponse submitPaymentResponse = 
    			dstvService.submitPayment(submitPayment);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(submitPaymentResponse.getSubmitPaymentResult().getSubmitPaymentEntry().getStatus().equals(Boolean.FALSE))
		{
			jmap.put("error", "01");
            jmap.put("fault", submitPaymentResponse.getSubmitPaymentResult().getSubmitPaymentEntry().getErrorMessage());
            jmap.put("auditRefNo", submitPaymentResponse.getSubmitPaymentResult().getSubmitPaymentEntry().getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(submitPaymentResponse));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
	}

	public String handleSubmitPaymentBySmartCard(
			SubmitPaymentBySmartCard submitPaymentBySmartCard) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
    	SubmitPaymentResponseForPaymentBySmartCard submitPaymentResponseForPaymentBySmartCard = 
    			dstvService.submitPaymentBySmartCard(submitPaymentBySmartCard);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(submitPaymentResponseForPaymentBySmartCard.getPaymentTransactionResponse().getStatus().equals(Boolean.FALSE))
		{
			jmap.put("error", "01");
            jmap.put("fault", submitPaymentResponseForPaymentBySmartCard.getPaymentTransactionResponse().getErrorMessage());
            jmap.put("auditRefNo", submitPaymentResponseForPaymentBySmartCard.getPaymentTransactionResponse().getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(submitPaymentResponseForPaymentBySmartCard));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
	}

	public String handleGetAvailableProducts(
			GetAvailableProducts getAvailableProducts) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
    	GetAvailableProductsResponse getAvailableProductsResponse = 
    			dstvService.getAvailableProducts(getAvailableProducts);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(getAvailableProductsResponse.getStatus().equals(Boolean.FALSE))
		{
			jmap.put("error", "01");
            jmap.put("fault", getAvailableProductsResponse.getErrorMessage());
            jmap.put("auditRefNo", getAvailableProductsResponse.getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(getAvailableProductsResponse));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
	}

	public String handleGetPrimaryProducts(
			GetPrimaryProducts getPrimaryProducts) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
    	GetPrimaryProductsResponse getPrimaryProductsResponse = 
    			dstvService.getPrimaryProducts(getPrimaryProducts);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(getPrimaryProductsResponse.getStatus().equals(Boolean.FALSE))
		{
			jmap.put("error", "01");
            jmap.put("fault", getPrimaryProductsResponse.getErrorMessage());
            jmap.put("auditRefNo", getPrimaryProductsResponse.getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(getPrimaryProductsResponse));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
	}

	public String handleGetCustomerDetailsByDeviceNumber(
			GetCustomerDetailsByDeviceNumber getCustomerDetailsByDeviceNumber) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
    	GetCustomerDetailsByCustomerNumberResponse getCustomerDetailsByCustomerNumberResponse = 
    			dstvService.getCustomerDetailsByDeviceNumber(getCustomerDetailsByDeviceNumber);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(getCustomerDetailsByCustomerNumberResponse.getStatus().equals(Boolean.FALSE))
		{
			jmap.put("error", "01");
            jmap.put("fault", getCustomerDetailsByCustomerNumberResponse.getErrorMessage());
            jmap.put("auditRefNo", getCustomerDetailsByCustomerNumberResponse.getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(getCustomerDetailsByCustomerNumberResponse));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
	}

	@SuppressWarnings("unchecked")
	public String handleGetProducts(GetProducts getProducts) throws JSONException {
		// TODO Auto-generated method stub
    	DSTVService dstvService = new DSTVService();
		GetProductsResponse getProductsResponse = dstvService.getProducts(getProducts);
		JSONObject jmap = new JSONObject();
		String ret = "";
		
		if(getProductsResponse.getStatus()==false)
		{
			jmap.put("error", "01");
            jmap.put("fault", getProductsResponse.getErrorMessage());
            jmap.put("auditRefNo", getProductsResponse.getAuditReferenceNumber());

            ret = new Gson().toJson(jmap);
            System.out.println(":: [GET PRODUCTS]: " + ret);
            return ret;
		}
		jmap.put("error", "00");
        jmap.put("fault", new Gson().toJson(getProductsResponse));
        
        ret = new Gson().toJson(jmap);
        System.out.println(":: [QUERY RETURNED]: " + ret);
        return ret;
		
	}
}
