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

import com.probase.probasepay.enumerations.MerchantStatus;
  
@Entity
@Table(name="devicebankaccounts")  
public class DeviceBankAccount implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String settlementBankAccountNumber; 
	@Column(nullable = false)
	String settlementBankBranchCode;
	@Column(nullable = false)
	Long settlementBankId;
	@Column(nullable = false)
	String settlementBankName;
	@Column(nullable = false)
	Long deviceId;
	@Column(nullable = false)
	String settlementBankAccountName;
	@Column(nullable = true)
	Long settlementAccountId;
	@Column(nullable = false)
	Boolean status;
	@Column(nullable = false)
	Long addedByUserId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	@Column(nullable = true)
	Long transientAccountId;
	@Column(nullable = false)
	String transientAccountNumber;
	Integer isLive;
	
	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
		this.updated_at = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}

	public String getSettlementBankAccountNumber() {
		return settlementBankAccountNumber;
	}

	public void setSettlementBankAccountNumber(String settlementBankAccountNumber) {
		this.settlementBankAccountNumber = settlementBankAccountNumber;
	}

	public String getSettlementBankBranchCode() {
		return settlementBankBranchCode;
	}

	public void setSettlementBankBranchCode(String settlementBankBranchCode) {
		this.settlementBankBranchCode = settlementBankBranchCode;
	}

	public Long getSettlementBankId() {
		return settlementBankId;
	}

	public void setSettlementBankId(Long settlementBankId) {
		this.settlementBankId = settlementBankId;
	}

	public String getSettlementBankName() {
		return settlementBankName;
	}

	public void setSettlementBankName(String settlementBankName) {
		this.settlementBankName = settlementBankName;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getSettlementBankAccountName() {
		return settlementBankAccountName;
	}

	public void setSettlementBankAccountName(String settlementBankAccountName) {
		this.settlementBankAccountName = settlementBankAccountName;
	}

	public Long getSettlementAccountId() {
		return settlementAccountId;
	}

	public void setSettlementAccountId(Long settlementAccountId) {
		this.settlementAccountId = settlementAccountId;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Long getAddedByUserId() {
		return addedByUserId;
	}

	public void setAddedByUserId(Long addedByUserId) {
		this.addedByUserId = addedByUserId;
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

	public Long getTransientAccountId() {
		return transientAccountId;
	}

	public void setTransientAccountId(Long transientAccountId) {
		this.transientAccountId = transientAccountId;
	}

	public String getTransientAccountNumber() {
		return transientAccountNumber;
	}

	public void setTransientAccountNumber(String transientAccountNumber) {
		this.transientAccountNumber = transientAccountNumber;
	}

	public Long getId() {
		return id;
	}

	public DeviceBankAccount() {

	}

	public DeviceBankAccount(String settlementBankAccountNumber, String settlementBankBranchCode, Long settlementBankId,
			String settlementBankName, Long deviceId, String settlementBankAccountName, Long settlementAccountId,
			Boolean status, Long addedByUserId, Long transientAccountId, String transientAccountNumber, Integer isLive) {
		super();
		this.settlementBankAccountNumber = settlementBankAccountNumber;
		this.settlementBankBranchCode = settlementBankBranchCode;
		this.settlementBankId = settlementBankId;
		this.settlementBankName = settlementBankName;
		this.deviceId = deviceId;
		this.settlementBankAccountName = settlementBankAccountName;
		this.settlementAccountId = settlementAccountId;
		this.status = status;
		this.addedByUserId = addedByUserId;
		this.transientAccountId = transientAccountId;
		this.transientAccountNumber = transientAccountNumber;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.isLive = isLive;
		
	}

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}



	
	
    
}

