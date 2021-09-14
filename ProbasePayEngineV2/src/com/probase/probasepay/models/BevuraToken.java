package com.probase.probasepay.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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

import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.StopCardReason;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;
  
@Entity
@Table(name="bevura_tokens")  
public class BevuraToken implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String token;
	@Column(nullable = true)
	Long accountId;
	@Column(nullable = true)
	Long cardId;
	@Column(nullable = true)
	Long merchantId;
	@Column(nullable = true)
	Long deviceId;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	String type;
	String activateRefNo;
	String otp;
	Long customerId;
	@Column(nullable = false)
	Integer status;
	Integer isLive;
	
	
	public BevuraToken()
	{
		
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getCardId() {
		return cardId;
	}

	public void setCardId(Long cardId) {
		this.cardId = cardId;
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

	public Long getId() {
		return id;
	}

	public BevuraToken(String token, Long accountId, Long cardId, Long merchantId, Long deviceId, String type, Long customerId, Integer status, String activateRefNo, String otp, Integer isLive) {
		super();
		this.token = token;
		this.accountId = accountId;
		this.cardId = cardId;
		this.merchantId = merchantId;
		this.deviceId = deviceId; 
		this.created_at = new Date();
		this.updated_at = new Date();
		this.type = type;
		this.customerId = customerId;
		this.status = status;
		this.activateRefNo = activateRefNo;
		this.otp = otp;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getActivateRefNo() {
		return activateRefNo;
	}

	public void setActivateRefNo(String activateRefNo) {
		this.activateRefNo = activateRefNo;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

	
    
}

