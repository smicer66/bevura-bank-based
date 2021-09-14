package com.probase.probasepay.models;

import java.io.Serializable;
import java.util.Collection;
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

import org.json.JSONException;
import org.json.JSONObject;

import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.AccountType;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.util.UtilityHelper;
  
@Entity
@Table(name="account_map_requests")  
public class AccountMapRequest implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	@Column(nullable = false)
	Long customerId;
	@Column(nullable = false)
	AccountStatus status;
	String otp;
	String otpRef;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	String branchCode;
	String accountNo;
	Long acquirerId;
	@Column(nullable = false)
	String currencyCode;
	@Column(nullable = false)
	AccountType accountType;
	Long corporateCustomerId;
	Long corporateCustomerAccountId;
	@Column(nullable = false)
	ProbasePayCurrency probasePayCurrency;
	Double accountBalance;
	Double floatingBalance;
	Long accountSchemeId;
	Long restrictedToUseOnDeviceId;
	Integer failCount;
	Integer isTokenize;
	Integer isLive;
	
	public AccountMapRequest(Long customerId, AccountStatus status, String otp, String otpRef, String branchCode, Long acquirerId, String currencyCode,
			AccountType accountType, Long corporateCustomerId, Long corporateCustomerAccountId,
			ProbasePayCurrency probasePayCurrency, Double accountBalance, Double floatingBalance, Long accountSchemeId,
			Long restrictedToUseOnDeviceId, String accountNo, Integer failCount, Integer isTokenize, Integer isLive) {
		super();
		this.customerId = customerId;
		this.status = status;
		this.otp = otp;
		this.otpRef = otpRef;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.deleted_at = null;
		this.branchCode = branchCode;
		this.acquirerId = acquirerId;
		this.currencyCode = currencyCode;
		this.accountType = accountType;
		this.corporateCustomerId = corporateCustomerId;
		this.corporateCustomerAccountId = corporateCustomerAccountId;
		this.probasePayCurrency = probasePayCurrency;
		this.accountBalance = accountBalance;
		this.floatingBalance = floatingBalance;
		this.accountSchemeId = accountSchemeId;
		this.restrictedToUseOnDeviceId = restrictedToUseOnDeviceId;
		this.accountNo = accountNo;
		this.failCount = failCount;
		this.isTokenize = isTokenize;
		this.isLive = isLive;
	}
	public AccountMapRequest()
	{
		
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
	public AccountType getAccountType() {
	    return accountType;
	}
	
	@Enumerated(EnumType.STRING)
	public AccountStatus getStatus() {
	    return status;
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

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}


	public Long getCorporateCustomerId() {
		return corporateCustomerId;
	}

	public void setCorporateCustomerId(Long corporateCustomerId) {
		this.corporateCustomerId = corporateCustomerId;
	}

	public Long getCorporateCustomerAccountId() {
		return corporateCustomerAccountId;
	}

	public void setCorporateCustomerAccountId(Long corporateCustomerAccountId) {
		this.corporateCustomerAccountId = corporateCustomerAccountId;
	}

	public ProbasePayCurrency getProbasePayCurrency() {
		return probasePayCurrency;
	}

	public void setProbasePayCurrency(ProbasePayCurrency probasePayCurrency) {
		this.probasePayCurrency = probasePayCurrency;
	}

	public Double getAccountBalance() {
		return accountBalance;
	}

	public void setAccountBalance(Double accountBalance) {
		this.accountBalance = accountBalance;
	}
	
	public Double getFloatingBalance() {
		return floatingBalance;
	}

	public void setFloatingBalance(Double floatingBalance) {
		this.floatingBalance = floatingBalance;
	}

	
	public Long getRestrictedToUseOnDeviceId() {
		return restrictedToUseOnDeviceId;
	}

	public void setRestrictedToUseOnDeviceId(Long restrictedToUseOnDeviceId) {
		this.restrictedToUseOnDeviceId = restrictedToUseOnDeviceId;
	}

	public String getOtpRef() {
		return otpRef;
	}

	public void setOtpRef(String otpRef) {
		this.otpRef = otpRef;
	}
	public Long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	public Long getAcquirerId() {
		return acquirerId;
	}
	public void setAcquirerId(Long acquirerId) {
		this.acquirerId = acquirerId;
	}
	public Long getAccountSchemeId() {
		return accountSchemeId;
	}
	public void setAccountSchemeId(Long accountSchemeId) {
		this.accountSchemeId = accountSchemeId;
	}
	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public Integer getFailCount() {
		return failCount;
	}
	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}
	public Integer getIsTokenize() {
		return isTokenize;
	}
	public void setIsTokenize(Integer isTokenize) {
		this.isTokenize = isTokenize;
	}
	public Integer getIsLive() {
		return isLive;
	}
	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}
}
