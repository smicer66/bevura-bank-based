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
  
@Entity
@Table(name="acquirer")  
public class Acquirer implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String acquirerName;
	@Column(nullable = false)
	String acquirerCode;
	@Column(nullable = false)
	Boolean holdFundsYes;
	String balanceInquiryEndPoint;
	String fundsTransferEndPoint;
	String accountCreationEndPoint;
	String serviceKey;
	String ftServiceKey;
	String authKey;
	String ftAuthKey;
	String balanceInquiryDemoEndPoint;
	String fundsTransferDemoEndPoint;
	String accountCreationDemoEndPoint;
	String demoServiceKey;
	String ftDemoServiceKey;
	String demoAuthKey;
	String ftDemoAuthKey;
	String demoCreditFTAuthKey;
	String creditFTAuthKey;
	Boolean isLive;
	@Column(nullable = false)
	String allowedCurrency;
	String accessExodus;
	@Column(nullable = true)
	Long defaultMerchantSchemeId;
	
	

	@OneToOne  
    @JoinColumn
	Bank bank;

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

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}

	public String getAcquirerCode() {
		return acquirerCode;
	}

	public void setAcquirerCode(String acquirerCode) {
		this.acquirerCode = acquirerCode;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	public Boolean getHoldFundsYes() {
		return holdFundsYes;
	}

	public void setHoldFundsYes(Boolean holdFundsYes) {
		this.holdFundsYes = holdFundsYes;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getBalanceInquiryEndPoint() {
		return balanceInquiryEndPoint;
	}

	public void setBalanceInquiryEndPoint(String balanceInquiryEndPoint) {
		this.balanceInquiryEndPoint = balanceInquiryEndPoint;
	}

	public String getFundsTransferEndPoint() {
		return fundsTransferEndPoint;
	}

	public void setFundsTransferEndPoint(String fundsTransferEndPoint) {
		this.fundsTransferEndPoint = fundsTransferEndPoint;
	}

	public String getAccountCreationEndPoint() {
		return accountCreationEndPoint;
	}

	public void setAccountCreationEndPoint(String accountCreationEndPoint) {
		this.accountCreationEndPoint = accountCreationEndPoint;
	}

	public String getBalanceInquiryDemoEndPoint() {
		return balanceInquiryDemoEndPoint;
	}

	public void setBalanceInquiryDemoEndPoint(String balanceInquiryDemoEndPoint) {
		this.balanceInquiryDemoEndPoint = balanceInquiryDemoEndPoint;
	}

	public String getFundsTransferDemoEndPoint() {
		return fundsTransferDemoEndPoint;
	}

	public void setFundsTransferDemoEndPoint(String fundsTransferDemoEndPoint) {
		this.fundsTransferDemoEndPoint = fundsTransferDemoEndPoint;
	}

	public String getAccountCreationDemoEndPoint() {
		return accountCreationDemoEndPoint;
	}

	public void setAccountCreationDemoEndPoint(String accountCreationDemoEndPoint) {
		this.accountCreationDemoEndPoint = accountCreationDemoEndPoint;
	}

	public String getDemoServiceKey() {
		return demoServiceKey;
	}

	public void setDemoServiceKey(String demoServiceKey) {
		this.demoServiceKey = demoServiceKey;
	}

	public String getDemoAuthKey() {
		return demoAuthKey;
	}

	public void setDemoAuthKey(String demoAuthKey) {
		this.demoAuthKey = demoAuthKey;
	}

	public Boolean getIsLive() {
		return isLive;
	}

	public void setIsLive(Boolean isLive) {
		this.isLive = isLive;
	}

	public String getAllowedCurrency() {
		return allowedCurrency;
	}

	public void setAllowedCurrency(String allowedCurrency) {
		this.allowedCurrency = allowedCurrency;
	}
	public String getAccessExodus() {
		return accessExodus;
	}

	public void setAccessExodus(String accessExodus) {
		this.accessExodus = accessExodus;
	}

	public String getFtServiceKey() {
		return ftServiceKey;
	}

	public void setFtServiceKey(String ftServiceKey) {
		this.ftServiceKey = ftServiceKey;
	}

	public String getFtAuthKey() {
		return ftAuthKey;
	}

	public void setFtAuthKey(String ftAuthKey) {
		this.ftAuthKey = ftAuthKey;
	}

	public String getFtDemoServiceKey() {
		return ftDemoServiceKey;
	}

	public void setFtDemoServiceKey(String ftDemoServiceKey) {
		this.ftDemoServiceKey = ftDemoServiceKey;
	}

	public String getFtDemoAuthKey() {
		return ftDemoAuthKey;
	}

	public void setFtDemoAuthKey(String ftDemoAuthKey) {
		this.ftDemoAuthKey = ftDemoAuthKey;
	}

	public Long getDefaultMerchantSchemeId() {
		return defaultMerchantSchemeId;
	}

	public void setDefaultMerchantSchemeId(Long defaultMerchantSchemeId) {
		this.defaultMerchantSchemeId = defaultMerchantSchemeId;
	}

	public String getDemoCreditFTAuthKey() {
		return demoCreditFTAuthKey;
	}

	public void setDemoCreditFTAuthKey(String demoCreditFTAuthKey) {
		this.demoCreditFTAuthKey = demoCreditFTAuthKey;
	}

	public String getCreditFTAuthKey() {
		return creditFTAuthKey;
	}

	public void setCreditFTAuthKey(String creditFTAuthKey) {
		this.creditFTAuthKey = creditFTAuthKey;
	}

}
