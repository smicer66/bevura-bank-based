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
@Table(name="airtime_to_money")  
public class Airtime2Money implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Customer customer;
	String status;
	Long recipientAccountId;
	Long recipientCardId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	String recipientMobileNumber;
	@Column(nullable = false)
	Double amount;
	String convertType;
	
	public Airtime2Money()
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


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Long getRecipientAccountId() {
		return recipientAccountId;
	}


	public void setRecipientAccountId(Long recipientAccountId) {
		this.recipientAccountId = recipientAccountId;
	}


	public Long getRecipientCardId() {
		return recipientCardId;
	}


	public void setRecipientCardId(Long recipientCardId) {
		this.recipientCardId = recipientCardId;
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


	public String getRecipientMobileNumber() {
		return recipientMobileNumber;
	}


	public void setRecipientMobileNumber(String recipientMobileNumber) {
		this.recipientMobileNumber = recipientMobileNumber;
	}


	public Double getAmount() {
		return amount;
	}


	public void setAmount(Double amount) {
		this.amount = amount;
	}


	public String getConvertType() {
		return convertType;
	}


	public void setConvertType(String convertType) {
		this.convertType = convertType;
	}

	
}
