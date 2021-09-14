package com.probase.probasepay.models;

import java.io.Serializable;

import javax.persistence.Column;  
import javax.persistence.Entity;  
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;  
import javax.persistence.GenerationType;
import javax.persistence.Id;  
import javax.persistence.Table;
import javax.persistence.Enumerated;
  
@Entity
@Table(name="merchantschemes")
public class MerchantScheme implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;  
	@Column(nullable = false)
	String schemename;
	@Column(nullable = false)
	Double transactionPercentage;
	@Column(nullable = false)
	Double fixedCharge;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSchemename() {
		return schemename;
	}
	public void setSchemename(String schemename) {
		this.schemename = schemename;
	}
	public Double getTransactionPercentage() {
		return transactionPercentage;
	}
	public void setTransactionPercentage(Double transactionPercentage) {
		this.transactionPercentage = transactionPercentage;
	}
	public Double getFixedCharge() {
		return fixedCharge;
	}
	public void setFixedCharge(Double fixedCharge) {
		this.fixedCharge = fixedCharge;
	}
	
	
}
