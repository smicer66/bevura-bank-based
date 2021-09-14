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
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Enumerated;

import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
  
@Entity
@Table(name="wallet_transactions")  
public class WalletTransaction implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String transactionRef; 
	String bankPaymentReference;
	//@Column(nullable = false)
	Long customerId;
	String crdrType;
	String orderRef;
	@Column(nullable = false)
	Date transactionDate;
	@Column(nullable = false)
	ServiceType serviceType;
	@Column(nullable = false)
	String accountName;
	@Column(nullable = false)
	String accountMobile;
	@Column(nullable = false)
	TransactionStatus status;
	@Column(nullable = false)
	ProbasePayCurrency probasePayCurrency;
	@Column(nullable = true)
	String transactionCode;
	Long accountId;
	@Column(nullable = true)
	Long deviceId;
	@Column(nullable = true)
	Long merchantId;
	Long transactionId;
	Double fixedCharge;
	Double transactionCharge;
	Double transactionPercentage;
	Double amount;
	String responseCode;
	@Column(nullable = true)
	Long adjustedTransactionId;
	Long acquirerId;
	Integer isLive;
	
	
	public WalletTransaction()
	{
		
	}
	


	public WalletTransaction(String transactionRef, String bankPaymentReference, Long customerId, String crdrType,
			String orderRef, Date transactionDate, ServiceType serviceType, String accountName, String accountMobile,
			TransactionStatus status, ProbasePayCurrency probasePayCurrency, String transactionCode, Long accountId,
			Long deviceId, Long merchantId, Long transactionId, Double fixedCharge, Double transactionCharge,
			Double transactionPercentage, Double amount, String responseCode, Long adjustedTransactionId,
			Long acquirerId, Integer isLive) {
		super();
		this.transactionRef = transactionRef;
		this.bankPaymentReference = bankPaymentReference;
		this.customerId = customerId;
		this.crdrType = crdrType;
		this.orderRef = orderRef;
		this.transactionDate = transactionDate;
		this.serviceType = serviceType;
		this.accountName = accountName;
		this.accountMobile = accountMobile;
		this.status = status;
		this.probasePayCurrency = probasePayCurrency;
		this.transactionCode = transactionCode;
		this.accountId = accountId;
		this.deviceId = deviceId;
		this.merchantId = merchantId;
		this.transactionId = transactionId;
		this.fixedCharge = fixedCharge;
		this.transactionCharge = transactionCharge;
		this.transactionPercentage = transactionPercentage;
		this.amount = amount;
		this.responseCode = responseCode;
		this.adjustedTransactionId = adjustedTransactionId;
		this.acquirerId = acquirerId;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.isLive = isLive;
	}



	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	
	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}

	
	
	public String getCrdrType() {
		return crdrType;
	}

	public void setCrdrType(String crdrType) {
		this.crdrType = crdrType;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountMobile() {
		return accountMobile;
	}

	public void setAccountMobile(String accountMobile) {
		this.accountMobile = accountMobile;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	@Enumerated(EnumType.STRING)
	public TransactionStatus getStatus() {
	    return status;
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}


	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	@Enumerated(EnumType.STRING)
	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public String getTransactionRef() {
		return transactionRef;
	}

	public void setTransactionRef(String transactionRef) {
		this.transactionRef = transactionRef;
	}

	public String getTransactionCode() {
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode) {
		this.transactionCode = transactionCode;
	}

	public Double getFixedCharge() {
		return fixedCharge;
	}

	public void setFixedCharge(Double fixedCharge) {
		this.fixedCharge = fixedCharge;
	}

	public Double getTransactionCharge() {
		return transactionCharge;
	}

	public void setTransactionCharge(Double transactionCharge) {
		this.transactionCharge = transactionCharge;
	}

	public Double getTransactionPercentage() {
		return transactionPercentage;
	}

	public void setTransactionPercentage(Double transactionPercentage) {
		this.transactionPercentage = transactionPercentage;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
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

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public ProbasePayCurrency getProbasePayCurrency() {
		return probasePayCurrency;
	}

	public void setProbasePayCurrency(ProbasePayCurrency probasePayCurrency) {
		this.probasePayCurrency = probasePayCurrency;
	}

	public String getBankPaymentReference() {
		return bankPaymentReference;
	}

	public void setBankPaymentReference(String bankPaymentReference) {
		this.bankPaymentReference = bankPaymentReference;
	}

	public Long getAdjustedTransactionId() {
		return adjustedTransactionId;
	}

	public void setAdjustedTransactionId(Long adjustedTransactionId) {
		this.adjustedTransactionId = adjustedTransactionId;
	}

	public Long getAcquirerId() {
		return acquirerId;
	}

	public void setAcquirerId(Long acquirerId) {
		this.acquirerId = acquirerId;
	}



	public Integer getIsLive() {
		return isLive;
	}



	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

}
