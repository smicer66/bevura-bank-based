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

import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.StopCardReason;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;
  
@Entity
@Table(name="ecard_deposits")  
public class ECardDeposit implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Long ecard_id;
	@Column(nullable = false)
	Double amount;
	@Column(nullable = true)
	Double previousBalance;
	@Column(nullable = true)
	Double newBalance;
	@Column(nullable = false)
	String sourceReference;
	@Column(nullable = true)
	Double chargeAmount;
	@Column(nullable = false)
	String transactionOrderNo;
	String transactionReference;
	@Column(nullable = false)
	Long transaction_id;
	@Column(nullable = false)
	Long card_scheme_id;
	Long source_account_id;
	Long source_card_id;
	Long receipient_customer_id;
	Long receipient_account_id;
	Long receipient_card_id;
	Long source_customer_id;
	Long source_user_id;

	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	
	
	public ECardDeposit()
	{
		
	}

	

	public ECardDeposit(Long ecard_id, Double amount, Double previousBalance, Double newBalance, String sourceReference,
			Double chargeAmount, String transactionOrderNo, String transactionReference, Long transaction_id,
			Long card_scheme_id, Long source_account_id, Long source_card_id, Long receipient_customer_id, Long receipient_account_id,
			Long receipient_card_id, Long source_customer_id, Long source_user_id) {
		super();
		this.ecard_id = ecard_id;
		this.amount = amount;
		this.previousBalance = previousBalance;
		this.newBalance = newBalance;
		this.sourceReference = sourceReference;
		this.chargeAmount = chargeAmount;
		this.transactionOrderNo = transactionOrderNo;
		this.transactionReference = transactionReference;
		this.transaction_id = transaction_id;
		this.card_scheme_id = card_scheme_id;
		this.source_account_id = source_account_id;
		this.source_card_id = source_card_id;
		this.receipient_customer_id = receipient_customer_id;
		this.receipient_account_id = receipient_account_id;
		this.receipient_card_id = receipient_card_id;
		this.source_customer_id = source_customer_id;
		this.source_user_id = source_user_id;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.deleted_at = new Date();
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



	public Long getEcard_id() {
		return ecard_id;
	}



	public void setEcard_id(Long ecard_id) {
		this.ecard_id = ecard_id;
	}



	public Double getAmount() {
		return amount;
	}



	public void setAmount(Double amount) {
		this.amount = amount;
	}



	public Double getPreviousBalance() {
		return previousBalance;
	}



	public void setPreviousBalance(Double previousBalance) {
		this.previousBalance = previousBalance;
	}



	public Double getNewBalance() {
		return newBalance;
	}



	public void setNewBalance(Double newBalance) {
		this.newBalance = newBalance;
	}



	public String getSourceReference() {
		return sourceReference;
	}



	public void setSourceReference(String sourceReference) {
		this.sourceReference = sourceReference;
	}



	public Double getChargeAmount() {
		return chargeAmount;
	}



	public void setChargeAmount(Double chargeAmount) {
		this.chargeAmount = chargeAmount;
	}



	public String getTransactionOrderNo() {
		return transactionOrderNo;
	}



	public void setTransactionOrderNo(String transactionOrderNo) {
		this.transactionOrderNo = transactionOrderNo;
	}



	public String getTransactionReference() {
		return transactionReference;
	}



	public void setTransactionReference(String transactionReference) {
		this.transactionReference = transactionReference;
	}



	public Long getTransaction_id() {
		return transaction_id;
	}



	public void setTransaction_id(Long transaction_id) {
		this.transaction_id = transaction_id;
	}



	public Long getCard_scheme_id() {
		return card_scheme_id;
	}



	public void setCard_scheme_id(Long card_scheme_id) {
		this.card_scheme_id = card_scheme_id;
	}



	public Long getSource_account_id() {
		return source_account_id;
	}



	public void setSource_account_id(Long source_account_id) {
		this.source_account_id = source_account_id;
	}



	public Long getSource_card_id() {
		return source_card_id;
	}



	public void setSource_card_id(Long source_card_id) {
		this.source_card_id = source_card_id;
	}



	public Long getReceipient_customer_id() {
		return receipient_customer_id;
	}



	public void setReceipient_customer_id(Long receipient_customer_id) {
		this.receipient_customer_id = receipient_customer_id;
	}



	public Long getSource_customer_id() {
		return source_customer_id;
	}



	public void setSource_customer_id(Long source_customer_id) {
		this.source_customer_id = source_customer_id;
	}



	public Long getSource_user_id() {
		return source_user_id;
	}



	public void setSource_user_id(Long source_user_id) {
		this.source_user_id = source_user_id;
	}



	public Long getReceipient_account_id() {
		return receipient_account_id;
	}



	public void setReceipient_account_id(Long receipient_account_id) {
		this.receipient_account_id = receipient_account_id;
	}



	public Long getReceipient_card_id() {
		return receipient_card_id;
	}



	public void setReceipient_card_id(Long receipient_card_id) {
		this.receipient_card_id = receipient_card_id;
	}


    
}

