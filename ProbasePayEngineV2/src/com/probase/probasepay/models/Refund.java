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

import com.probase.probasepay.enumerations.RefundStatus;
import com.probase.probasepay.enumerations.RefundType;

@Entity
@Table(name="refunds")  
public class Refund implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	@Column(nullable = false)
	String transactionData;
	String messageRequest;
	String messageResponse;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	@OneToOne  
    @JoinColumn
	ECard card;
	@OneToOne  
    @JoinColumn
	Transaction creditCardTransaction;
	@Column(nullable = false)
	String transactionId;
	@Column(nullable = false)
	RefundType refundType;
	@Column(nullable = false)
	RefundStatus status;
	@Column(nullable = false)
	Double amount;
	
	
	public Refund()
	{
		
	}
	
	
	public Refund(String transactionData, String messageRequest,
			String messageResponse, ECard card, Transaction creditCardTransaction, String transactionId, Double amount, RefundType refundType) {
		super();
		this.transactionData = transactionData;
		this.messageRequest = messageRequest;
		this.messageResponse = messageResponse;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.card = card;
		this.creditCardTransaction = creditCardTransaction;
		this.transactionId = transactionId;
		this.refundType = refundType;
		this.amount = amount;
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

	public String getTransactionData() {
		return transactionData;
	}

	public void setTransactionData(String transactionData) {
		this.transactionData = transactionData;
	}

	public String getMessageRequest() {
		return messageRequest;
	}

	public void setMessageRequest(String messageRequest) {
		this.messageRequest = messageRequest;
	}

	public String getMessageResponse() {
		return messageResponse;
	}

	public void setMessageResponse(String messageResponse) {
		this.messageResponse = messageResponse;
	}

	public ECard getCard() {
		return card;
	}

	public void setCard(ECard card) {
		this.card = card;
	}

	public Transaction getCreditCardTransaction() {
		return creditCardTransaction;
	}

	public void setCreditCardTransaction(Transaction creditCardTransaction) {
		this.creditCardTransaction = creditCardTransaction;
	}


	public String getTransactionId() {
		return transactionId;
	}


	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	@Enumerated(EnumType.STRING)
	public RefundType getRefundType() {
		return refundType;
	}


	public void setRefundType(RefundType refundType) {
		this.refundType = refundType;
	}

	@Enumerated(EnumType.STRING)
	public RefundStatus getStatus() {
		return status;
	}


	public void setStatus(RefundStatus status) {
		this.status = status;
	}
	
	
	
}
