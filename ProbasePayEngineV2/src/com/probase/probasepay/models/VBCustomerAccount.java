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
import com.probase.probasepay.enumerations.VBCustomerAccountStatus;

import javax.persistence.Enumerated;

  
@Entity
@Table(name="customer_accounts")  
public class VBCustomerAccount  implements Serializable{  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	@OneToOne  
    @JoinColumn
	Customer customer;
	@Column(nullable = false)
	String accountName;
	@Column(nullable = false)
	String accountNumber;
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = false)
	VBCustomerAccountStatus customerAccountStatus;
	@Column(nullable = false)
	Double currentBalance;
	@Column(nullable = false)
	Double availableBalance;
	@Column(nullable = false)
	ProbasePayCurrency currency;
	
	
	public VBCustomerAccount() {
	}

	public VBCustomerAccount(Customer customer, String accountName,
			String accountNumber, VBCustomerAccountStatus customerAccountStatus, ProbasePayCurrency currency) {
		super();
		this.customer = customer;
		this.accountName = accountName;
		this.accountNumber = accountNumber;
		this.customerAccountStatus = customerAccountStatus;
		this.currentBalance = 0.00;
		this.availableBalance = 0.00;
		this.currency = currency;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}



	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(Date deletedAt) {
		this.deletedAt = deletedAt;
	}


	@Enumerated(EnumType.STRING)
	public VBCustomerAccountStatus getCustomerAccountStatus() {
		return customerAccountStatus;
	}

	public void setCustomerAccountStatus(VBCustomerAccountStatus customerAccountStatus) {
		this.customerAccountStatus =customerAccountStatus;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public Long getId() {
		return id;
	}

	public Double getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(Double currentBalance) {
		this.currentBalance = currentBalance;
	}

	public Double getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(Double availableBalance) {
		this.availableBalance = availableBalance;
	}

	public ProbasePayCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(ProbasePayCurrency currency) {
		this.currency = currency;
	}
}
