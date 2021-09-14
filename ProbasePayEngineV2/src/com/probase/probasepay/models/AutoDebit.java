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
@Table(name="autodebits")  
public class AutoDebit implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Long acquirerId;
	@Column(nullable = false)
	String userMobile;
	@Column(nullable = false)
	String userFullName;
	@Column(nullable = false)
	Double amountDue;
	@Column(nullable = false)
	Date dateDue;
	@Column(nullable = false)
	Long accountId;
	@Column(nullable = true)
	Long cardId;
	@Column(nullable = false)
	Boolean isProcessed;
	@Column(nullable = false)
	Boolean isLocked;
	String requestData;
	String responseData;
	Long transactionId;
	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	
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

	public Long getAcquirerId() {
		return acquirerId;
	}

	public void setAcquirerId(Long acquirerId) {
		this.acquirerId = acquirerId;
	}

	public String getUserMobile() {
		return userMobile;
	}

	public void setUserMobile(String userMobile) {
		this.userMobile = userMobile;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public Double getAmountDue() {
		return amountDue;
	}

	public void setAmountDue(Double amountDue) {
		this.amountDue = amountDue;
	}

	public Date getDateDue() {
		return dateDue;
	}

	public void setDateDue(Date dateDue) {
		this.dateDue = dateDue;
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

	public Boolean getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(Boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	public String getRequestData() {
		return requestData;
	}

	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}

	public String getResponseData() {
		return responseData;
	}

	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public AutoDebit(Long acquirerId, String userMobile, String userFullName, Double amountDue, Date dateDue,
			Long accountId, Long cardId, Boolean isProcessed, Boolean isLocked, String requestData, String responseData,
			Long transactionId) {
		super();
		this.acquirerId = acquirerId;
		this.userMobile = userMobile;
		this.userFullName = userFullName;
		this.amountDue = amountDue;
		this.dateDue = dateDue;
		this.accountId = accountId;
		this.cardId = cardId;
		this.isProcessed = isProcessed;
		this.isLocked = isLocked;
		this.requestData = requestData;
		this.responseData = responseData;
		this.transactionId = transactionId;
		this.created_at = new Date();
		this.updated_at = new Date();
	}
}
