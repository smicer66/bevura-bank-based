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
import com.probase.probasepay.enumerations.StopCardReason;
  
@Entity
@Table(name="ecard_bin")  
public class ECardBin implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String pan;
	@OneToOne  
    @JoinColumn
	Acquirer acquirer;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Issuer issuer;
	//@Column(nullable = false)
	@Column(nullable = false)
	CardStatus status;
	@Column(nullable = false)
	CardType cardType;
	String serialNo;

	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	String trackingNumber;
	String cardBatchCode;
	@OneToOne  
    @JoinColumn
	ECard cardIssued;
	
	
	
	public ECardBin()
	{
		
	}

	

	public ECardBin(String cardBatchCode, String pan, Acquirer acquirer, Issuer issuer,
			CardStatus status, CardType cardType, String serialNo,
			String trackingNumber, ECard cardIssued) {
		super();
		this.cardBatchCode = cardBatchCode;
		this.pan = pan;
		this.issuer = issuer;
		this.acquirer = acquirer;
		this.status = status;
		this.cardType = cardType;
		this.serialNo = serialNo;
		this.trackingNumber = trackingNumber;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.cardIssued = cardIssued;
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
	public CardStatus getStatus() {
	    return status;
	}
	
	@Enumerated(EnumType.STRING)
	public CardType getCardType() {
	    return cardType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}


	public Acquirer getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(Acquirer acquirer) {
		this.acquirer = acquirer;
	}

	public Issuer getIssuer() {
		return issuer;
	}

	public void setIssuer(Issuer issuer) {
		this.issuer = issuer;
	}


	public void setStatus(CardStatus status) {
		this.status = status;
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

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}


	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}
	
	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}



	public ECard getCardIssued() {
		return cardIssued;
	}



	public void setCardIssued(ECard cardIssued) {
		this.cardIssued = cardIssued;
	}

}

