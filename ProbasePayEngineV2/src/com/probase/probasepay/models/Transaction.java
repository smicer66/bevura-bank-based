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
@Table(name="transactions")  
public class Transaction implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String transactionRef; 
	String bankPaymentReference;
	//@Column(nullable = false)
	Long customerId;
	@Column(nullable = true)
	Boolean creditAccountTrue;
	@Column(nullable = true)
	Boolean creditCardTrue;
	@Column(nullable = true)
	Boolean debitAccountTrue;
	@Column(nullable = true)
	Boolean debitCardTrue;
	String orderRef;
	String rpin;
	@Column(nullable = false)
	Channel channel;
	@Column(nullable = false)
	Date transactionDate;
	@Column(nullable = false)
	ServiceType serviceType;
	@Column(nullable = true)
	String payerName;
	@Column(nullable = true)
	String payerEmail;
	@Column(nullable = true)
	String payerMobile;
	@Column(nullable = false)
	TransactionStatus status;
	@Column(nullable = false)
	ProbasePayCurrency probasePayCurrency;
	@Column(nullable = true)
	String transactionCode;
	@OneToOne  
    @JoinColumn
	Acquirer acquirer;
	@OneToOne  
    @JoinColumn
	Account account;
	@OneToOne  
    @JoinColumn
	ECard card;
	@OneToOne  
    @JoinColumn
	Device device;
	Boolean creditPoolAccountTrue;
	String messageRequest;
	String messageResponse;
	Double fixedCharge;
	Double transactionCharge;
	Double transactionPercentage;
	Double schemeTransactionCharge;
	Double schemeTransactionPercentage;
	Integer isLive;
	
	
	public Acquirer getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(Acquirer acquirer) {
		this.acquirer = acquirer;
	}


	Double amount;
	String responseCode;
	String OTP;
	String OTPRef;
	Long merchantId;
	String merchantName;
	String merchantCode;
	String merchantBank;
	String merchantAccount;
	Long transactingBankId;	//Src Bank Id
	Long transactingIssuerId;	//Src Bank Id
	Long receipientTransactingBankId;	//Rec Bank Id
	Integer accessCode;
	Long sourceEntityId;
	Long receipientEntityId;
	Channel receipientChannel;
	String transactionDetail;
	Double closingBalance;
	Double totalCreditSum;
	Double totalDebitSum;
	Long paidInByBankUserAccountId;
	String customData;
	String responseData;
	Long adjustedTransactionId;
	Long acquirerId;
	Long creditAccountId;
	Long creditCardId;
	Long debitAccountId;
	Long debitCardId;
	String debitBankPaymentReference;
	String creditBankPaymentReference;
	String details;
	@Column(nullable = true)
	String summary; 
	
	
	public Transaction()
	{
		
	}
	
	public Transaction(String transactionRef, String bankPaymentReference,
			Long customerId, Boolean creditAccountTrue, Boolean creditCardTrue,
			String orderRef, String rpin, Channel channel,
			Date transactionDate, ServiceType serviceType, String payerName,
			String payerEmail, String payerMobile, TransactionStatus status,
			ProbasePayCurrency probasePayCurrency, String transactionCode,
			Account account, ECard card, Device device,
			Boolean creditPoolAccountTrue, String messageRequest,
			String messageResponse, Double fixedCharge,
			Double transactionCharge, Double transactionPercentage,
			Double schemeTransactionCharge, Double schemeTransactionPercentage,
			Double amount, String responseCode, String oTP, String oTPRef,
			Long merchantId, String merchantName, String merchantCode,
			String merchantBank, String merchantAccount, 
			Long transactingBankId, Long receipientTransactingBankId,
			Integer accessCode, Long sourceEntityId, Long receipientEntityId,
			Channel receipientChannel, String transactionDetail,
			Double closingBalance, Double totalCreditSum, Double totalDebitSum,
			Long paidInByBankUserAccountId, String customData,
			String responseData, Long adjustedTransactionId, Long acquirerId, Boolean debitAccountTrue, Boolean debitCardTrue, 
			Long creditAccountId,
			Long creditCardId,
			Long debitAccountId,
			Long debitCardId, String details, Integer isLive) {
		super();
		this.transactionRef = transactionRef;
		this.bankPaymentReference = bankPaymentReference;
		this.customerId = customerId;
		this.creditAccountTrue = creditAccountTrue;
		this.creditCardTrue = creditCardTrue;
		this.debitAccountTrue = debitAccountTrue;
		this.debitCardTrue = debitCardTrue;
		this.orderRef = orderRef;
		this.rpin = rpin;
		this.channel = channel;
		this.transactionDate = transactionDate;
		this.serviceType = serviceType;
		this.payerName = payerName;
		this.payerEmail = payerEmail;
		this.payerMobile = payerMobile;
		this.status = status;
		this.probasePayCurrency = probasePayCurrency;
		this.transactionCode = transactionCode;
		this.account = account;
		this.card = card;
		this.device = device;
		this.creditPoolAccountTrue = creditPoolAccountTrue;
		this.messageRequest = messageRequest;
		this.messageResponse = messageResponse;
		this.fixedCharge = fixedCharge;
		this.transactionCharge = transactionCharge;
		this.transactionPercentage = transactionPercentage;
		this.schemeTransactionCharge = schemeTransactionCharge;
		this.schemeTransactionPercentage = schemeTransactionPercentage;
		this.amount = amount;
		this.responseCode = responseCode;
		this.OTP = oTP;
		this.OTPRef = oTPRef;
		this.merchantId = merchantId;
		this.merchantName = merchantName;
		this.merchantCode = merchantCode;
		this.merchantBank = merchantBank;
		this.merchantAccount = merchantAccount;
		this.transactingBankId = transactingBankId;
		this.receipientTransactingBankId = receipientTransactingBankId;
		this.accessCode = accessCode;
		this.sourceEntityId = sourceEntityId;
		this.receipientEntityId = receipientEntityId;
		this.receipientChannel = receipientChannel;
		this.transactionDetail = transactionDetail;
		this.closingBalance = closingBalance;
		this.totalCreditSum = totalCreditSum;
		this.totalDebitSum = totalDebitSum;
		this.paidInByBankUserAccountId = paidInByBankUserAccountId;
		this.customData = customData;
		this.responseData = responseData;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.adjustedTransactionId = adjustedTransactionId;
		this.acquirerId = acquirerId;
		this.creditAccountId = creditAccountId;
		this.creditCardId = creditCardId;
		this.debitAccountId = debitAccountId;
		this.debitCardId = debitCardId;
		this.debitBankPaymentReference = null;
		this.creditBankPaymentReference = null;
		this.details = details;
		this.isLive = isLive;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
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

	
	
	@Enumerated(EnumType.STRING)
	public TransactionStatus getStatus() {
	    return status;
	}
	
	@Enumerated(EnumType.STRING)
	public Channel getChannel() {
	    return channel;
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

	public String getPayerName() {
		return payerName;
	}

	public void setPayerName(String payerName) {
		this.payerName = payerName;
	}

	public String getPayerEmail() {
		return payerEmail;
	}

	public void setPayerEmail(String payerEmail) {
		this.payerEmail = payerEmail;
	}

	public String getPayerMobile() {
		return payerMobile;
	}

	public void setPayerMobile(String payerMobile) {
		this.payerMobile = payerMobile;
	}

	public String getOTP() {
		return OTP;
	}

	public void setOTP(String oTP) {
		OTP = oTP;
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

	public String getMessageRequest() {
		return messageRequest;
	}

	public void setMessageRequest(String messageRequest) {
		this.messageRequest = messageRequest;
	}

	public String getMessageResponse() {
		return messageResponse;
	}

	public void setMessageResponse(String messageResponse) {
		this.messageResponse = messageResponse;
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

	public Double getSchemeTransactionCharge() {
		return schemeTransactionCharge;
	}

	public void setSchemeTransactionCharge(Double schemeTransactionCharge) {
		this.schemeTransactionCharge = schemeTransactionCharge;
	}

	public Double getSchemeTransactionPercentage() {
		return schemeTransactionPercentage;
	}

	public void setSchemeTransactionPercentage(Double schemeTransactionPercentage) {
		this.schemeTransactionPercentage = schemeTransactionPercentage;
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

	public void setChannel(Channel channel) {
		this.channel = channel;
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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public boolean isCreditAccountTrue() {
		return creditAccountTrue;
	}

	public void setCreditAccountTrue(boolean creditAccountTrue) {
		this.creditAccountTrue = creditAccountTrue;
	}

	public ECard getCard() {
		return card;
	}

	public void setCard(ECard card) {
		this.card = card;
	}

	public boolean isCreditCardTrue() {
		return creditCardTrue;
	}

	public void setCreditCardTrue(boolean creditCardTrue) {
		this.creditCardTrue = creditCardTrue;
	}



	public boolean isCreditPoolAccountTrue() {
		return creditPoolAccountTrue;
	}

	public void setCreditPoolAccountTrue(boolean creditPoolAccountTrue) {
		this.creditPoolAccountTrue = creditPoolAccountTrue;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Long getTransactingBankId() {
		return transactingBankId;
	}

	public void setTransactingBankId(Long transactingBankId) {
		this.transactingBankId = transactingBankId;
	}

	public Boolean getCreditAccountTrue() {
		return creditAccountTrue;
	}

	public void setCreditAccountTrue(Boolean creditAccountTrue) {
		this.creditAccountTrue = creditAccountTrue;
	}

	public Boolean getCreditCardTrue() {
		return creditCardTrue;
	}

	public void setCreditCardTrue(Boolean creditCardTrue) {
		this.creditCardTrue = creditCardTrue;
	}

	public Boolean getCreditPoolAccountTrue() {
		return creditPoolAccountTrue;
	}

	public void setCreditPoolAccountTrue(Boolean creditPoolAccountTrue) {
		this.creditPoolAccountTrue = creditPoolAccountTrue;
	}

	public Integer getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(Integer accessCode) {
		this.accessCode = accessCode;
	}

	public Long getSourceEntityId() {
		return sourceEntityId;
	}

	public void setSourceEntityId(Long sourceEntityId) {
		this.sourceEntityId = sourceEntityId;
	}

	public Long getReceipientEntityId() {
		return receipientEntityId;
	}

	public void setReceipientEntityId(Long receipientEntityId) {
		this.receipientEntityId = receipientEntityId;
	}


	public Channel getReceipientChannel() {
		return receipientChannel;
	}

	public void setReceipientChannel(Channel receipientChannel) {
		this.receipientChannel = receipientChannel;
	}

	public String getTransactionDetail() {
		return transactionDetail;
	}

	public void setTransactionDetail(String transactionDetail) {
		this.transactionDetail = transactionDetail;
	}

	public Double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}

	public Double getTotalCreditSum() {
		return totalCreditSum;
	}

	public void setTotalCreditSum(Double totalCreditSum) {
		this.totalCreditSum = totalCreditSum;
	}

	public Double getTotalDebitSum() {
		return totalDebitSum;
	}

	public void setTotalDebitSum(Double totalDebitSum) {
		this.totalDebitSum = totalDebitSum;
	}
	
	public Long getPaidInByBankUserAccountId() {
		return paidInByBankUserAccountId;
	}

	public void setPaidInByBankUserAccountId(Long paidInByBankUserAccountId) {
		this.paidInByBankUserAccountId = paidInByBankUserAccountId;
	}

	public Long getReceipientTransactingBankId() {
		return receipientTransactingBankId;
	}

	public void setReceipientTransactingBankId(Long receipientTransactingBankId) {
		this.receipientTransactingBankId = receipientTransactingBankId;
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

	public String getMerchantBank() {
		return merchantBank;
	}

	public void setMerchantBank(String merchantBank) {
		this.merchantBank = merchantBank;
	}

	public String getMerchantAccount() {
		return merchantAccount;
	}

	public void setMerchantAccount(String merchantAccount) {
		this.merchantAccount = merchantAccount;
	}

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
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

	public String getOTPRef() {
		return OTPRef;
	}

	public void setOTPRef(String oTPRef) {
		OTPRef = oTPRef;
	}

	public Long getAdjustedTransactionId() {
		return adjustedTransactionId;
	}

	public void setAdjustedTransactionId(Long adjustedTransactionId) {
		this.adjustedTransactionId = adjustedTransactionId;
	}

	public Long getTransactingIssuerId() {
		return transactingIssuerId;
	}

	public void setTransactingIssuerId(Long transactingIssuerId) {
		this.transactingIssuerId = transactingIssuerId;
	}

	public Long getAcquirerId() {
		return acquirerId;
	}

	public void setAcquirerId(Long acquirerId) {
		this.acquirerId = acquirerId;
	}

	public Boolean getDebitAccountTrue() {
		return debitAccountTrue;
	}

	public void setDebitAccountTrue(Boolean debitAccountTrue) {
		this.debitAccountTrue = debitAccountTrue;
	}

	public Boolean getDebitCardTrue() {
		return debitCardTrue;
	}

	public void setDebitCardTrue(Boolean debitCardTrue) {
		this.debitCardTrue = debitCardTrue;
	}

	public Long getCreditAccountId() {
		return creditAccountId;
	}

	public void setCreditAccountId(Long creditAccountId) {
		this.creditAccountId = creditAccountId;
	}

	public Long getCreditCardId() {
		return creditCardId;
	}

	public void setCreditCardId(Long creditCardId) {
		this.creditCardId = creditCardId;
	}

	public Long getDebitAccountId() {
		return debitAccountId;
	}

	public void setDebitAccountId(Long debitAccountId) {
		this.debitAccountId = debitAccountId;
	}

	public Long getDebitCardId() {
		return debitCardId;
	}

	public void setDebitCardId(Long debitCardId) {
		this.debitCardId = debitCardId;
	}

	public String getDebitBankPaymentReference() {
		return debitBankPaymentReference;
	}

	public void setDebitBankPaymentReference(String debitBankPaymentReference) {
		this.debitBankPaymentReference = debitBankPaymentReference;
	}

	public String getCreditBankPaymentReference() {
		return creditBankPaymentReference;
	}

	public void setCreditBankPaymentReference(String creditBankPaymentReference) {
		this.creditBankPaymentReference = creditBankPaymentReference;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

}
