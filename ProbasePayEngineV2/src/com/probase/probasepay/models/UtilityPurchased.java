package com.probase.probasepay.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;  
import javax.persistence.Entity;  
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;  
import javax.persistence.GenerationType;
import javax.persistence.Id;  
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Enumerated;

import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.MPQRDataStatus;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.NetBankingPaymentType;
  
@Entity
@Table(name="utility_purchased")  
public class UtilityPurchased implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String utilityType; 
	@Column(nullable = false)
	String orderRef; 
	@Column(nullable = false)
	String transactionRef;
	@Column(nullable = false)
	String productType;
	@Column(nullable = false)
	String responseData;
	@Column(nullable = false)
	String status;
	@Column(nullable = false)
	Double amount;
	Long transactionId;
	@Column(nullable = false)
	String probasePayCurrency;
	@Column(nullable = true)
	Long cardId;
	@Column(nullable = true)
	Long accountId;
	@Column(nullable = true)
	Long customerId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	Integer isLive;
	
	public UtilityPurchased()
	{
		super();
	}
	
	

	public UtilityPurchased(String utilityType, String productType, String orderRef, String transactionRef, String responseData,
			String status, Double amount, Long transactionId, Long cardId, Long accountId, Long customerId, String probasePayCurrency, Integer isLive) {
		super();
		this.utilityType = utilityType;
		this.orderRef = orderRef;
		this.transactionRef = transactionRef;
		this.responseData = responseData;
		this.status = status;
		this.amount = amount;
		this.transactionId = transactionId;
		this.cardId = cardId;
		this.accountId = accountId;
		this.customerId = customerId;
		this.productType = productType;
		this.probasePayCurrency = probasePayCurrency;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.isLive = isLive;
	}



	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
	}


	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}



	public String getUtilityType() {
		return utilityType;
	}



	public String getProductType() {
		return productType;
	}



	public void setProductType(String productType) {
		this.productType = productType;
	}



	public void setUtilityType(String utilityType) {
		this.utilityType = utilityType;
	}



	public String getOrderRef() {
		return orderRef;
	}



	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}



	public String getTransactionRef() {
		return transactionRef;
	}



	public void setTransactionRef(String transactionRef) {
		this.transactionRef = transactionRef;
	}



	public String getResponseData() {
		return responseData;
	}



	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public Long getTransactionId() {
		return transactionId;
	}



	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}



	public Long getCardId() {
		return cardId;
	}



	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}



	public Long getAccountId() {
		return accountId;
	}



	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}



	public Long getCustomerId() {
		return customerId;
	}



	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}



	public Date getCreated_at() {
		return created_at;
	}



	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}



	public String getProbasePayCurrency() {
		return probasePayCurrency;
	}



	public void setProbasePayCurrency(String probasePayCurrency) {
		this.probasePayCurrency = probasePayCurrency;
	}



	public Date getUpdated_at() {
		return updated_at;
	}



	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}



	public Date getDeleted_at() {
		return deleted_at;
	}



	public void setDeleted_at(Date deleted_at) {
		this.deleted_at = deleted_at;
	}



	public Long getId() {
		return id;
	}



	public Integer getIsLive() {
		return isLive;
	}



	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

	
	
	
    
}

