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
@Table(name="group_contributions")  
public class GroupContribution implements Serializable{  
	private static final long serialVersionUID = 5457558421999610410L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Double amount; 
	@Column(nullable = false)
	Boolean isPenalty; 
	@OneToOne  
    @JoinColumn
	Customer customer;
	@OneToOne  
    @JoinColumn
	GroupPaymentsExpected groupPaymentExpected; 
	Long groupId;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	
	
	
	public GroupContribution() {
	}

	public GroupContribution(Double amount, Customer customer,
			GroupPaymentsExpected groupPaymentExpected, Boolean isPenalty, Long groupId) {
		super();
		this.amount = amount;
		this.customer = customer;
		this.groupPaymentExpected = groupPaymentExpected;
		this.createdAt = new Date();
		this.updatedAt = new Date();
		this.isPenalty = isPenalty;
		this.groupId = groupId;
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

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomerAccount(Customer customer) {
		this.customer = customer;
	}

	public GroupPaymentsExpected getGroupPaymentExpected() {
		return groupPaymentExpected;
	}

	public void setGroupPaymentExpected(GroupPaymentsExpected groupPaymentExpected) {
		this.groupPaymentExpected = groupPaymentExpected;
	}

	public Long getId() {
		return id;
	}

	public Boolean getIsPenalty() {
		return isPenalty;
	}

	public void setIsPenalty(Boolean isPenalty) {
		this.isPenalty = isPenalty;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

    
}

