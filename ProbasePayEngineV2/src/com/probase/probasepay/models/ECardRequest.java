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

import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
  
@Entity
@Table(name="ecard_requests")  
public class ECardRequest implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String nameOnCard;
	@Column(nullable = false)
	String title;
	@Column(nullable = false)
	String addressLine1;
	@Column(nullable = false)
	String addressLine2;
	@Column(nullable = false)
	String addressLine3;
	@Column(nullable = false)
	String addressLine4;
	@Column(nullable = false)
	String addressLine5;
	@Column(nullable = false)
	String additionalData;
	@Column(nullable = false)
	String requestId;
	@OneToOne  
    @JoinColumn
	Account account;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	
	
	public ECardRequest()
	{
		
	}


	public ECardRequest(String nameOnCard, String title, String addressLine1,
			String addressLine2, String addressLine3, String addressLine4,
			String addressLine5, String additionalData, String requestId,
			Account account) {
		super();
		this.nameOnCard = nameOnCard;
		this.title = title;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.addressLine3 = addressLine3;
		this.addressLine4 = addressLine4;
		this.addressLine5 = addressLine5;
		this.additionalData = additionalData;
		this.requestId = requestId;
		this.account = account;
		this.created_at = new Date();
		this.updated_at = new Date();
	}


	public String getNameOnCard() {
		return nameOnCard;
	}


	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getAddressLine1() {
		return addressLine1;
	}


	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}


	public String getAddressLine2() {
		return addressLine2;
	}


	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}


	public String getAddressLine3() {
		return addressLine3;
	}


	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}


	public String getAddressLine4() {
		return addressLine4;
	}


	public void setAddressLine4(String addressLine4) {
		this.addressLine4 = addressLine4;
	}


	public String getAddressLine5() {
		return addressLine5;
	}


	public void setAddressLine5(String addressLine5) {
		this.addressLine5 = addressLine5;
	}


	public String getAdditionalData() {
		return additionalData;
	}


	public void setAdditionalData(String additionalData) {
		this.additionalData = additionalData;
	}


	public String getRequestId() {
		return requestId;
	}


	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}


	public Account getAccount() {
		return account;
	}


	public void setAccount(Account account) {
		this.account = account;
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

	
    
}

