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

import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.DeviceStatus;
import com.probase.probasepay.enumerations.DeviceType;
import com.probase.probasepay.enumerations.MPQRDataStatus;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.NetBankingPaymentType;
import com.probase.probasepay.enumerations.ServiceType;
  
@Entity
@Table(name="funds_transfer")  
public class FundsTransfer implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String sourceIdentityNo; 
	@Column(nullable = false)
	String sourceType; 
	@Column(nullable = false)
	String sourceCustomerName;
	@Column(nullable = false)
	Long sourceCustomerId;
	@Column(nullable = false)
	String sourceBankName;
	@Column(nullable = false)
	Long sourceBankId;
	@Column(nullable = false)
	String receipientIdentityNo;
	@Column(nullable = true)
	String receipientType;
	@Column(nullable = false)
	String receipientCustomerName;
	@Column(nullable = false)
	Long receipientCustomerId;
	@Column(nullable = false)
	String receipientBankName;
	@Column(nullable = false)
	Long receipientBankId;
	String narration;
	@Column(nullable = false)
	Double amount;
	@Column(nullable = false)
	Double transactionFee;
	@Column(nullable = false)
	Double fixedFee;
	Double sourcePriorBalance;
	Double sourceNewBalance;
	@Column(nullable = false)
	ServiceType serviceType;
	@Column(nullable = false)
	Channel channel;
	@Column(nullable = false)
	String transactionRef;
	@Column(nullable = false)
	String debitOrderRef;
	@Column(nullable = false)
	String creditOrderRef;
	@Column(nullable = false)
	Long transactionId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	String receipientCreditBankReferenceNo;
	String receipientResponseData;
	String sourceResponseData;
	String sourceDebitBankReferenceNo;
	String chargeBankPaymentReference;
	Integer isLive;
	
	public FundsTransfer()
	{
		super();
	}
	
	


	public FundsTransfer(String sourceIdentityNo, String sourceType, String sourceCustomerName, Long sourceCustomerId,
			String sourceBankName, Long sourceBankId, String receipientIdentityNo, String receipientType,
			String receipientCustomerName, Long receipientCustomerId, String receipientBankName,
			Long receipientBankId, String narration, Double amount, Double transactionFee, Double fixedFee,
			Double sourcePriorBalance, Double sourceNewBalance, ServiceType serviceType, Channel channel,
			String transactionRef, String debitOrderRef, String creditOrderRef, Long transactionId, 
			String receipientCreditBankReferenceNo, String receipientResponseData,
			String sourceResponseData, String sourceDebitBankReferenceNo, String chargeBankPaymentReference, Integer isLive) {
		super();
		this.sourceIdentityNo = sourceIdentityNo;
		this.sourceType = sourceType;
		this.sourceCustomerName = sourceCustomerName;
		this.sourceCustomerId = sourceCustomerId;
		this.sourceBankName = sourceBankName;
		this.sourceBankId = sourceBankId;
		this.receipientIdentityNo = receipientIdentityNo;
		this.receipientType = receipientType;
		this.receipientCustomerName = receipientCustomerName;
		this.receipientCustomerId = receipientCustomerId;
		this.receipientBankName = receipientBankName;
		this.receipientBankId = receipientBankId;
		this.narration = narration;
		this.amount = amount;
		this.transactionFee = transactionFee;
		this.fixedFee = fixedFee;
		this.sourcePriorBalance = sourcePriorBalance;
		this.sourceNewBalance = sourceNewBalance;
		this.serviceType = serviceType;
		this.channel = channel;
		this.transactionRef = transactionRef;
		this.debitOrderRef = debitOrderRef;
		this.creditOrderRef = creditOrderRef;
		this.transactionId = transactionId;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.receipientCreditBankReferenceNo = receipientCreditBankReferenceNo;
		this.receipientResponseData = receipientResponseData;
		this.sourceResponseData = sourceResponseData;
		this.sourceDebitBankReferenceNo = sourceDebitBankReferenceNo;
		this.chargeBankPaymentReference = chargeBankPaymentReference;
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

	
	
	@Enumerated(EnumType.STRING)
	public ServiceType getServiceType() {
	    return this.serviceType;
	}

	
	@Enumerated(EnumType.STRING)
	public Channel getChannel() {
	    return this.channel;
	}
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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




	public String getSourceIdentityNo() {
		return sourceIdentityNo;
	}




	public void setSourceIdentityNo(String sourceIdentityNo) {
		this.sourceIdentityNo = sourceIdentityNo;
	}




	public String getSourceType() {
		return sourceType;
	}




	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}




	public String getSourceCustomerName() {
		return sourceCustomerName;
	}




	public void setSourceCustomerName(String sourceCustomerName) {
		this.sourceCustomerName = sourceCustomerName;
	}








	public String getSourceBankName() {
		return sourceBankName;
	}




	public void setSourceBankName(String sourceBankName) {
		this.sourceBankName = sourceBankName;
	}






	public String getReceipientIdentityNo() {
		return receipientIdentityNo;
	}




	public void setReceipientIdentityNo(String receipientIdentityNo) {
		this.receipientIdentityNo = receipientIdentityNo;
	}




	public String getReceipientType() {
		return receipientType;
	}




	public void setReceipientType(String receipientType) {
		this.receipientType = receipientType;
	}




	public String getReceipientCustomerName() {
		return receipientCustomerName;
	}




	public void setReceipientCustomerName(String receipientCustomerName) {
		this.receipientCustomerName = receipientCustomerName;
	}




	public String getReceipientBankName() {
		return receipientBankName;
	}




	public void setReceipientBankName(String receipientBankName) {
		this.receipientBankName = receipientBankName;
	}





	public String getNarration() {
		return narration;
	}




	public void setNarration(String narration) {
		this.narration = narration;
	}




	public Double getAmount() {
		return amount;
	}




	public void setAmount(Double amount) {
		this.amount = amount;
	}




	public Double getTransactionFee() {
		return transactionFee;
	}




	public void setTransactionFee(Double transactionFee) {
		this.transactionFee = transactionFee;
	}




	public Double getFixedFee() {
		return fixedFee;
	}




	public void setFixedFee(Double fixedFee) {
		this.fixedFee = fixedFee;
	}




	public Double getSourcePriorBalance() {
		return sourcePriorBalance;
	}




	public void setSourcePriorBalance(Double sourcePriorBalance) {
		this.sourcePriorBalance = sourcePriorBalance;
	}




	public Double getSourceNewBalance() {
		return sourceNewBalance;
	}




	public void setSourceNewBalance(Double sourceNewBalance) {
		this.sourceNewBalance = sourceNewBalance;
	}




	public String getTransactionRef() {
		return transactionRef;
	}




	public void setTransactionRef(String transactionRef) {
		this.transactionRef = transactionRef;
	}



	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
	}




	public void setChannel(Channel channel) {
		this.channel = channel;
	}




	public String getDebitOrderRef() {
		return debitOrderRef;
	}




	public void setDebitOrderRef(String debitOrderRef) {
		this.debitOrderRef = debitOrderRef;
	}




	public String getCreditOrderRef() {
		return creditOrderRef;
	}




	public void setCreditOrderRef(String creditOrderRef) {
		this.creditOrderRef = creditOrderRef;
	}




	public Long getSourceCustomerId() {
		return sourceCustomerId;
	}




	public void setSourceCustomerId(Long sourceCustomerId) {
		this.sourceCustomerId = sourceCustomerId;
	}




	public Long getSourceBankId() {
		return sourceBankId;
	}




	public void setSourceBankId(Long sourceBankId) {
		this.sourceBankId = sourceBankId;
	}




	public Long getReceipientCustomerId() {
		return receipientCustomerId;
	}




	public void setReceipientCustomerId(Long receipientCustomerId) {
		this.receipientCustomerId = receipientCustomerId;
	}




	public Long getReceipientBankId() {
		return receipientBankId;
	}




	public void setReceipientBankId(Long receipientBankId) {
		this.receipientBankId = receipientBankId;
	}




	public Long getTransactionId() {
		return transactionId;
	}




	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}




	public String getReceipientCreditBankReferenceNo() {
		return receipientCreditBankReferenceNo;
	}




	public void setReceipientCreditBankReferenceNo(String receipientCreditBankReferenceNo) {
		this.receipientCreditBankReferenceNo = receipientCreditBankReferenceNo;
	}




	public String getReceipientResponseData() {
		return receipientResponseData;
	}




	public void setReceipientResponseData(String receipientResponseData) {
		this.receipientResponseData = receipientResponseData;
	}




	public String getSourceResponseData() {
		return sourceResponseData;
	}




	public void setSourceResponseData(String sourceResponseData) {
		this.sourceResponseData = sourceResponseData;
	}




	public String getSourceDebitBankReferenceNo() {
		return sourceDebitBankReferenceNo;
	}




	public void setSourceDebitBankReferenceNo(String sourceDebitBankReferenceNo) {
		this.sourceDebitBankReferenceNo = sourceDebitBankReferenceNo;
	}




	public String getChargeBankPaymentReference() {
		return chargeBankPaymentReference;
	}




	public void setChargeBankPaymentReference(String chargeBankPaymentReference) {
		this.chargeBankPaymentReference = chargeBankPaymentReference;
	}




	public Integer getIsLive() {
		return isLive;
	}




	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}
	
}