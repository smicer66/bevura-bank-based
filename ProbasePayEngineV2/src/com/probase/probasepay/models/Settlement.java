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
@Table(name="settlements")  
public class Settlement implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String crAccountNumber; 
	@Column(nullable = false)
	String crBankName;
	@Column(nullable = false)
	String crAccountName;
	@Column(nullable = true)
	Long crAccountId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	@Column(nullable = false)
	Date startDate;
	@Column(nullable = false)
	Date endDate;
	@Column(nullable = false)
	Double amount;
	@Column(nullable = false)
	Long deviceId;
	@Column(nullable = false)
	Long merchantId;
	@Column(nullable = true)
	String drAccountNumber; 
	@Column(nullable = true)
	Long drAccountId;
	@Column(nullable = true)
	String currency; 
	
	public Settlement() {
		super();
	}

	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
		this.updated_at = new Date();
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

	public String getCrAccountNumber() {
		return crAccountNumber;
	}

	public void setCrAccountNumber(String crAccountNumber) {
		this.crAccountNumber = crAccountNumber;
	}

	public String getCrBankName() {
		return crBankName;
	}

	public void setCrBankName(String crBankName) {
		this.crBankName = crBankName;
	}

	public String getCrAccountName() {
		return crAccountName;
	}

	public void setCrAccountName(String crAccountName) {
		this.crAccountName = crAccountName;
	}

	public Long getCrAccountId() {
		return crAccountId;
	}

	public void setCrAccountId(Long crAccountId) {
		this.crAccountId = crAccountId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getDrAccountNumber() {
		return drAccountNumber;
	}

	public void setDrAccountNumber(String drAccountNumber) {
		this.drAccountNumber = drAccountNumber;
	}

	public Long getDrAccountId() {
		return drAccountId;
	}

	public void setDrAccountId(Long drAccountId) {
		this.drAccountId = drAccountId;
	}

	public Settlement(String crAccountNumber, String crBankName, String crAccountName, Long crAccountId, Date startDate,
			Date endDate, Double amount, Long deviceId, Long merchantId, String drAccountNumber, Long drAccountId, String currency) {
		super();
		this.crAccountNumber = crAccountNumber;
		this.crBankName = crBankName;
		this.crAccountName = crAccountName;
		this.crAccountId = crAccountId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.amount = amount;
		this.deviceId = deviceId;
		this.merchantId = merchantId;
		this.drAccountNumber = drAccountNumber;
		this.drAccountId = drAccountId;
		this.currency = currency;
		this.created_at = new Date();
		this.updated_at = new Date();
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}


	

}
