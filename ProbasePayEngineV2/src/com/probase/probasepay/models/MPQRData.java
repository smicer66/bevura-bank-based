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
import com.probase.probasepay.enumerations.MPQRDataType;
import com.probase.probasepay.enumerations.MerchantStatus;
import com.probase.probasepay.enumerations.NetBankingPaymentType;
  
@Entity
@Table(name="mpqr_data")  
public class MPQRData implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String qrCardNumber; 
	@Column(nullable = false)
	String qrDataString; 
	@Column(nullable = false)
	String qrDataImage;
	Long deviceId;
	@Column(nullable = true)
	Long cardId;
	@Column(nullable = false)
	MPQRDataStatus status;
	@OneToOne  
    @JoinColumn
	User setupByUser;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	@Column(nullable = false)
	String qrCodeId;
	@OneToOne  
    @JoinColumn
	Account walletAccount;
	@Column(nullable = true)
	String qrSaveDataImagePath;
	@Column(nullable = true)
	MPQRDataType mpqrDataType;
	Integer isLive;
	
	
	public MPQRData()
	{
		super();
	}
	
	public MPQRData(String qrCodeId, String qrCardNumber, String qrDataString, String qrDataImage, Long deviceId,
			Long cardId, MPQRDataStatus status, User setupByUser, Account walletAccount, String qrSaveDataImagePath, MPQRDataType mpqrDataType, Integer isLive) {
		super();
		this.qrCodeId = qrCodeId;
		this.qrCardNumber = qrCardNumber;
		this.qrDataString = qrDataString;
		this.qrDataImage = qrDataImage;
		this.deviceId = deviceId;
		this.cardId = cardId;
		this.status = status;
		this.setupByUser = setupByUser;
		this.walletAccount = walletAccount;
		this.qrSaveDataImagePath = qrSaveDataImagePath;
		this.mpqrDataType = mpqrDataType;
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
	public MPQRDataStatus getStatus() {
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


	public String getQrDataString() {
		return qrDataString;
	}


	public void setQrDataString(String qrDataString) {
		this.qrDataString = qrDataString;
	}


	public String getQrDataImage() {
		return qrDataImage;
	}


	public void setQrDataImage(String qrDataImage) {
		this.qrDataImage = qrDataImage;
	}


	public Long getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}


	public Long getCardId() {
		return cardId;
	}


	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}


	public User getSetupByUser() {
		return setupByUser;
	}


	public void setSetupByUser(User setupByUser) {
		this.setupByUser = setupByUser;
	}


	public void setStatus(MPQRDataStatus status) {
		this.status = status;
	}


	public String getQrCardNumber() {
		return qrCardNumber;
	}


	public void setQrCardNumber(String qrCardNumber) {
		this.qrCardNumber = qrCardNumber;
	}


	public String getQrCodeId() {
		return qrCodeId;
	}


	public void setQrCodeId(String qrCodeId) {
		this.qrCodeId = qrCodeId;
	}

	public Account getWalletAccount() {
		return walletAccount;
	}

	public void setWalletAccount(Account walletAccount) {
		this.walletAccount = walletAccount;
	}

	public String getQrSaveDataImagePath() {
		return qrSaveDataImagePath;
	}

	public void setQrSaveDataImagePath(String qrSaveDataImagePath) {
		this.qrSaveDataImagePath = qrSaveDataImagePath;
	}

	public MPQRDataType getMpqrDataType() {
		return mpqrDataType;
	}

	public void setMpqrDataType(MPQRDataType mpqrDataType) {
		this.mpqrDataType = mpqrDataType;
	}

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}


	
	
    
}

