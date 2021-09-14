package com.probase.probasepay.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;  
import javax.persistence.Entity;  
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
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
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.NetBankingPaymentType;
  
@Entity
@Table(name="devices")  
public class Device implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String deviceCode; 
	@Column(nullable = false)
	String deviceSerialNo;
	
	@OneToOne(fetch = FetchType.LAZY)  
    @JoinColumn
	Merchant merchant;
	@Column(nullable = false)
	DeviceType deviceType;
	@Column(nullable = false)
	DeviceStatus status;
	@Column(nullable = true)
	String successUrl;
	@Column(nullable = true)
	String failureUrl;
	@Column(nullable = true)
	String domainUrl;
	@Column(nullable = true)
	String emailNotify;
	@Column(nullable = true)
	String mobileNotify;
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
	User setupByUser;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	NetBankingPaymentType otcPaymentType;
	@Column(columnDefinition = "BIT", length = 1)
	Integer switchToLive;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean mastercardAccept;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean visaAccept;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean mobileMoneyAccept;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean walletAccept;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean bankOnlineAccept;
	String zicbAuthKey;
	String zicbServiceKey;
	String zicbDemoAuthKey;
	String zicbDemoServiceKey;
	@Column(nullable = true)
	Long settlementCardId;
	@Column(nullable = true)
	Boolean probaseOwned;
	@Column(nullable = true)
	Boolean isTestYes;
	

	@Column(nullable = true)
	String cybersourceLiveAccessKey;
	@Column(nullable = true)
	String cybersourceLiveProfileId;
	@Column(nullable = true)
	String cybersourceLiveSecretKey;
	@Column(nullable = true)
	String cybersourceDemoAccessKey;
	@Column(nullable = true)
	String cybersourceDemoProfileId;
	@Column(nullable = true)
	String cybersourceDemoSecretKey;
	@Column(nullable = true)
	String ubaServiceKey;
	@Column(nullable = true)
	String ubaMerchantId;
	@Column(nullable = true)
	String mpqrDeviceCode;
	@Column(nullable = true)
	String mpqrDeviceSerialNo;
	
	
	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
	}

	public Integer getSwitchToLive() {
		return switchToLive;
	}

	public void setSwitchToLive(Integer switchToLive) {
		this.switchToLive = switchToLive;
	}

	public Boolean getMobileMoneyAccept() {
		return mobileMoneyAccept;
	}

	public void setMobileMoneyAccept(Boolean mobileMoneyAccept) {
		this.mobileMoneyAccept = mobileMoneyAccept;
	}

	public Boolean getWalletAccept() {
		return walletAccept;
	}

	public void setWalletAccept(Boolean walletAccept) {
		this.walletAccept = walletAccept;
	}

	public Boolean getBankOnlineAccept() {
		return bankOnlineAccept;
	}

	public void setBankOnlineAccept(Boolean bankOnlineAccept) {
		this.bankOnlineAccept = bankOnlineAccept;
	}
	
	public NetBankingPaymentType getOtcPaymentType() {
		return otcPaymentType;
	}

	public void setOtcPaymentType(NetBankingPaymentType otcPaymentType) {
		this.otcPaymentType = otcPaymentType;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}

	
	
	@Enumerated(EnumType.STRING)
	public DeviceStatus getStatus() {
	    return status;
	}
	
	@Enumerated(EnumType.STRING)
	public DeviceType getDeviceType() {
	    return deviceType;
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

	public String getDeviceCode() {
		return deviceCode;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getDeviceSerialNo() {
		return deviceSerialNo;
	}

	public void setDeviceSerialNo(String deviceSerialNo) {
		this.deviceSerialNo = deviceSerialNo;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public void setMerchant(Merchant merchant) {
		this.merchant = merchant;
	}

	public String getSuccessUrl() {
		return successUrl;
	}

	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}

	public String getFailureUrl() {
		return failureUrl;
	}

	public void setFailureUrl(String failureUrl) {
		this.failureUrl = failureUrl;
	}

	public String getDomainUrl() {
		return domainUrl;
	}

	public void setDomainUrl(String domainUrl) {
		this.domainUrl = domainUrl;
	}

	public String getEmailNotify() {
		return emailNotify;
	}

	public void setEmailNotify(String emailNotify) {
		this.emailNotify = emailNotify;
	}

	public String getMobileNotify() {
		return mobileNotify;
	}

	public void setMobileNotify(String mobileNotify) {
		this.mobileNotify = mobileNotify;
	}

	public User getSetupByUser() {
		return setupByUser;
	}

	public void setSetupByUser(User setupByUser) {
		this.setupByUser = setupByUser;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public String getZicbAuthKey() {
		return zicbAuthKey;
	}

	public void setZicbAuthKey(String zicbAuthKey) {
		this.zicbAuthKey = zicbAuthKey;
	}

	public String getZicbServiceKey() {
		return zicbServiceKey;
	}

	public void setZicbServiceKey(String zicbServiceKey) {
		this.zicbServiceKey = zicbServiceKey;
	}

	public Long getSettlementAccountId() {
		return settlementCardId;
	}

	public void setSettlementCardId(Long settlementCardId) {
		this.settlementCardId = settlementCardId;
	}

	
	public String getCybersourceLiveAccessKey() {
		return cybersourceLiveAccessKey;
	}

	public void setCybersourceLiveAccessKey(String cybersourceLiveAccessKey) {
		this.cybersourceLiveAccessKey = cybersourceLiveAccessKey;
	}

	public String getCybersourceLiveProfileId() {
		return cybersourceLiveProfileId;
	}

	public void setCybersourceLiveProfileId(String cybersourceLiveProfileId) {
		this.cybersourceLiveProfileId = cybersourceLiveProfileId;
	}

	public String getCybersourceLiveSecretKey() {
		return cybersourceLiveSecretKey;
	}

	public void setCybersourceLiveSecretKey(String cybersourceLiveSecretKey) {
		this.cybersourceLiveSecretKey = cybersourceLiveSecretKey;
	}

	public String getCybersourceDemoAccessKey() {
		return cybersourceDemoAccessKey;
	}

	public void setCybersourceDemoAccessKey(String cybersourceDemoAccessKey) {
		this.cybersourceDemoAccessKey = cybersourceDemoAccessKey;
	}

	public String getCybersourceDemoProfileId() {
		return cybersourceDemoProfileId;
	}

	public void setCybersourceDemoProfileId(String cybersourceDemoProfileId) {
		this.cybersourceDemoProfileId = cybersourceDemoProfileId;
	}

	public String getCybersourceDemoSecretKey() {
		return cybersourceDemoSecretKey;
	}

	public void setCybersourceDemoSecretKey(String cybersourceDemoSecretKey) {
		this.cybersourceDemoSecretKey = cybersourceDemoSecretKey;
	}

	public String getUbaServiceKey() {
		return ubaServiceKey;
	}

	public void setUbaServiceKey(String ubaServiceKey) {
		this.ubaServiceKey = ubaServiceKey;
	}

	public String getUbaMerchantId() {
		return ubaMerchantId;
	}

	public void setUbaMerchantId(String ubaMerchantId) {
		this.ubaMerchantId = ubaMerchantId;
	}

	public String getMpqrDeviceCode() {
		return mpqrDeviceCode;
	}

	public void setMpqrDeviceCode(String mpqrDeviceCode) {
		this.mpqrDeviceCode = mpqrDeviceCode;
	}

	public String getMpqrDeviceSerialNo() {
		return mpqrDeviceSerialNo;
	}

	public void setMpqrDeviceSerialNo(String mpqrDeviceSerialNo) {
		this.mpqrDeviceSerialNo = mpqrDeviceSerialNo;
	}

	public Boolean getProbaseOwned() {
		return probaseOwned;
	}

	public void setProbaseOwned(Boolean probaseOwned) {
		this.probaseOwned = probaseOwned;
	}

	public Long getSettlementCardId() {
		return settlementCardId;
	}

	public Boolean getIsTestYes() {
		return isTestYes;
	}

	public void setIsTestYes(Boolean isTestYes) {
		this.isTestYes = isTestYes;
	}

	public Boolean getMastercardAccept() {
		return mastercardAccept;
	}

	public void setMastercardAccept(Boolean mastercardAccept) {
		this.mastercardAccept = mastercardAccept;
	}

	public Boolean getVisaAccept() {
		return visaAccept;
	}

	public void setVisaAccept(Boolean visaAccept) {
		this.visaAccept = visaAccept;
	}

	public String getZicbDemoAuthKey() {
		return zicbDemoAuthKey;
	}

	public void setZicbDemoAuthKey(String zicbDemoAuthKey) {
		this.zicbDemoAuthKey = zicbDemoAuthKey;
	}

	public String getZicbDemoServiceKey() {
		return zicbDemoServiceKey;
	}

	public void setZicbDemoServiceKey(String zicbDemoServiceKey) {
		this.zicbDemoServiceKey = zicbDemoServiceKey;
	}
    
}

