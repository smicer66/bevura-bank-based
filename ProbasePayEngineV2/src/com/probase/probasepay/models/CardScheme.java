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

import com.probase.probasepay.enumerations.ProbasePayCurrency;

import javax.persistence.Enumerated;
  
@Entity
@Table(name="cardschemes")  
public class CardScheme  implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	@Column(nullable = false)
	String schemeName;
	@Column(nullable = false)
	Double overrideTransactionFee;
	@Column(nullable = false)
	Double overrideFixedFee;
	@Column(nullable = false)
	String schemeCode;
	@Column(nullable = false)
	Double minimumBalance;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	ProbasePayCurrency currency;
	@OneToOne  
    @JoinColumn
	Issuer issuer;
	@Column(nullable = false)
	Boolean isDefault;
	
	
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

	public String getSchemeName() {
		return schemeName;
	}

	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}

	public Double getOverrideTransactionFee() {
		return overrideTransactionFee;
	}

	public void setOverrideTransactionFee(Double overrideTransactionFee) {
		this.overrideTransactionFee = overrideTransactionFee;
	}

	public Double getOverrideFixedFee() {
		return overrideFixedFee;
	}

	public void setOverrideFixedFee(Double overrideFixedFee) {
		this.overrideFixedFee = overrideFixedFee;
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

	public String getSchemeCode() {
		return schemeCode;
	}

	public void setSchemeCode(String schemeCode) {
		this.schemeCode = schemeCode;
	}

	public Double getMinimumBalance() {
		return minimumBalance;
	}

	public void setMinimumBalance(Double minimumBalance) {
		this.minimumBalance = minimumBalance;
	}

	public ProbasePayCurrency getCurrency() {
		return currency;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Enumerated(EnumType.STRING)
	public void setCurrency(ProbasePayCurrency currency) {
		this.currency = currency;
	}

	public Issuer getIssuer() {
		return issuer;
	}

	public void setIssuer(Issuer issuer) {
		this.issuer = issuer;
	}
}
