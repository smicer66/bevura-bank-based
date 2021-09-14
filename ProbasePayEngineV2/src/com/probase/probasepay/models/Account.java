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
@Table(name="accounts")  
public class Account implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Customer customer;
	@Column(nullable = false)
	AccountStatus status;
	String otp;
	String otpRef;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	String branchCode;
	@OneToOne  
    @JoinColumn
	Acquirer acquirer;
	@Column(nullable = false)
	String currencyCode;
	@Column(nullable = false)
	AccountType accountType;
	@GeneratedValue(strategy=GenerationType.AUTO)
	String accountIdentifier;
	@Column(nullable = false)
	int accountCount;
	@OneToOne  
    @JoinColumn
	Customer corporateCustomer;
	Long corporateCustomerId;
	@OneToOne  
    @JoinColumn
	Account corporateCustomerAccount;
	Long corporateCustomerAccountId;
	@Column(nullable = false)
	ProbasePayCurrency probasePayCurrency;
	Double accountBalance;
	Double floatingBalance;
	@OneToOne  
    @JoinColumn
	CardScheme accountScheme;
	Long restrictedToUseOnDeviceId;
	Integer isLive;
	
	public Account()
	{
		
	}

	public Account(Customer customer, AccountStatus status, String otp,
			String branchCode, Acquirer acquirer, String currencyCode,
			AccountType accountType, String accountIdentifier,
			Integer accountCount, Customer corporateCustomer, Long corporateCustomerId,
			Account corporateCustomerAccount, Long corporateCustomerAccountId,
			ProbasePayCurrency probasePayCurrency, Double floatingBalance, Double accountBalance, CardScheme accountScheme, Integer isLive) {
		super();
		this.customer = customer;
		this.status = status;
		this.otp = otp;
		this.branchCode = branchCode;
		this.acquirer = acquirer;
		this.currencyCode = currencyCode;
		this.accountType = accountType;
		this.accountIdentifier = accountIdentifier;
		this.accountCount = accountCount;
		this.corporateCustomer = corporateCustomer;
		this.corporateCustomerId = corporateCustomerId;
		this.corporateCustomerAccount = corporateCustomerAccount;
		this.corporateCustomerAccountId = corporateCustomerAccountId;
		this.probasePayCurrency = probasePayCurrency;
		this.floatingBalance = floatingBalance;
		this.accountBalance = accountBalance;
		this.accountScheme = accountScheme;
		this.isLive = isLive;
		this.created_at = new Date();
		this.updated_at = new Date();
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

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
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

	public String getAccountIdentifier() {
		return accountIdentifier;
	}

	public void setAccountIdentifier(String accountIdentifier) {
		this.accountIdentifier = accountIdentifier;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public Integer getAccountCount() {
		return accountCount;
	}

	public void setAccountCount(Integer accountCount) {
		this.accountCount = accountCount;
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

	public Customer getCorporateCustomer() {
		return corporateCustomer;
	}

	public void setCorporateCustomer(Customer corporateCustomer) {
		this.corporateCustomer = corporateCustomer;
	}

	public Account getCorporateCustomerAccount() {
		return corporateCustomerAccount;
	}

	public void setCorporateCustomerAccount(Account corporateCustomerAccount) {
		this.corporateCustomerAccount = corporateCustomerAccount;
	}

	public void setAccountCount(int accountCount) {
		this.accountCount = accountCount;
	}

	public JSONObject getAccountDetails() {
		// TODO Auto-generated method stub
		JSONObject oneAccount = new JSONObject();
		try {
			oneAccount.put("id", this.getId());
			oneAccount.put("accountIdentifier", UtilityHelper.getValue(this.getAccountIdentifier()));
			oneAccount.put("accountType", UtilityHelper.getValue(this.getAccountType().name()));
			oneAccount.put("acquirerName", UtilityHelper.getValue(this.getAcquirer().getAcquirerName()));
			oneAccount.put("customerverficationnumber", (this.getCustomer().getVerificationNumber()));
			oneAccount.put("customerFullName", (this.getCustomer().getLastName() + " " + this.getCustomer().getFirstName() + (this.getCustomer().getOtherName()==null ? "" : " " + this.getCustomer().getOtherName())));
			oneAccount.put("status", UtilityHelper.getValue(this.getStatus().name()));
			return oneAccount;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	public Acquirer getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(Acquirer acquirer) {
		this.acquirer = acquirer;
	}

	public Double getFloatingBalance() {
		return floatingBalance;
	}

	public void setFloatingBalance(Double floatingBalance) {
		this.floatingBalance = floatingBalance;
	}

	public CardScheme getAccountScheme() {
		return accountScheme;
	}

	public void setAccountScheme(CardScheme accountScheme) {
		this.accountScheme = accountScheme;
	}

	
	public JSONObject getSummary() throws JSONException
	{
		JSONObject js1 = new JSONObject();
		js1.put("customerName", this.customer.getFirstName() + " " + this.customer.getLastName());
		js1.put("accountScheme", this.accountScheme);
		js1.put("status", this.status);
		js1.put("accountType", this.accountType.name());
		js1.put("accountIdentifier", this.accountIdentifier);
		
		
		return js1;
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

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}
}
