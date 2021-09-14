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
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TransactionStatus;
  
@Entity
@Table(name="transactionsswept")  
public class TransactionSwept implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String transactionRef; 
	Long customerId;
	String orderRef;
	String rpin;
	Date transactionDate;
	Date sweepingDate;
	Long deviceId;
	Long deviceCode;
	Long merchantId;
	Double amount;
	@Column(nullable = false)
	Long transactionId;
	@Column(nullable = false)
	Long merchantBankId;
	@Column(nullable = false)
	String merchantName;
	@Column(nullable = false)
	String merchantCode;
	@Column(nullable = false)
	String merchantBankName;
	@Column(nullable = false)
	String merchantBankCode;
	@Column(nullable = false)
	String merchantAccountNumber;
	@Column(nullable = false)
	TransactionStatus status;
	@Column(nullable = false)
	Long authorizedByUserId;
	@Column(nullable = false)
	Long initiatedByUserId;

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

	public String getRpin() {
		return rpin;
	}

	public void setRpin(String rpin) {
		this.rpin = rpin;
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

	public String getTransactionRef() {
		return transactionRef;
	}

	public void setTransactionRef(String transactionRef) {
		this.transactionRef = transactionRef;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public Date getSweepingDate() {
		return sweepingDate;
	}

	public void setSweepingDate(Date sweepingDate) {
		this.sweepingDate = sweepingDate;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(Long deviceCode) {
		this.deviceCode = deviceCode;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Long getMerchantBankId() {
		return merchantBankId;
	}

	public void setMerchantBankId(Long merchantBankId) {
		this.merchantBankId = merchantBankId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getMerchantBankName() {
		return merchantBankName;
	}

	public void setMerchantBankName(String merchantBankName) {
		this.merchantBankName = merchantBankName;
	}

	public String getMerchantBankCode() {
		return merchantBankCode;
	}

	public void setMerchantBankCode(String merchantBankCode) {
		this.merchantBankCode = merchantBankCode;
	}

	public String getMerchantAccountNumber() {
		return merchantAccountNumber;
	}

	public void setMerchantAccountNumber(String merchantAccountNumber) {
		this.merchantAccountNumber = merchantAccountNumber;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}

	

}
