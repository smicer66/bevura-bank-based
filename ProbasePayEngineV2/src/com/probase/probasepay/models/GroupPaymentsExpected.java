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
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Enumerated;


  
@Entity
@Table(name="group_payments_expected")  
public class GroupPaymentsExpected implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@OneToOne  
    @JoinColumn
    ContributionPackage countributionPackage;
	@OneToOne  
    @JoinColumn
    GroupMember groupMember;
	@Column(nullable = false)
	Double amount;
	@Column(nullable = false)
	Date dateExpected;
	@Column(nullable = false)
	Double amountPaid;
	@Column(nullable = false)
	Double outstandingBalance;
	@Column(nullable = false)
	Double totalPenalties;
	@Column(nullable = false)
	Double penaltiesPaid;
	@Column(nullable = false)
	Boolean isPaid;
	@Column(nullable = false)
	Integer expectedPaymentNumber;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	Long groupId;
	
	
	
	
	public GroupPaymentsExpected() {
	}

	public GroupPaymentsExpected(ContributionPackage countributionPackage,
			GroupMember groupMember, Double amount, Date dateExpected,
			Double amountPaid, Double outstandingBalance,
			Double totalPenalties, Double penaltiesPaid, Boolean isPaid, Integer expectedPaymentNumber, Long groupId) {
		super();
		this.countributionPackage = countributionPackage;
		this.groupMember = groupMember;
		this.amount = amount;
		this.dateExpected = dateExpected;
		this.amountPaid = amountPaid;
		this.outstandingBalance = outstandingBalance;
		this.totalPenalties = totalPenalties;
		this.penaltiesPaid = penaltiesPaid;
		this.isPaid = isPaid;
		this.expectedPaymentNumber = expectedPaymentNumber;
		this.createdAt = new Date();
		this.updatedAt = new Date();
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

	public ContributionPackage getCountributionPackage() {
		return countributionPackage;
	}

	public void setCountributionPackage(ContributionPackage countributionPackage) {
		this.countributionPackage = countributionPackage;
	}

	public GroupMember getGroupMember() {
		return groupMember;
	}

	public void setGroupMember(GroupMember groupMember) {
		this.groupMember = groupMember;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Date getDateExpected() {
		return dateExpected;
	}

	public void setDateExpected(Date dateExpected) {
		this.dateExpected = dateExpected;
	}

	public Double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(Double amountPaid) {
		this.amountPaid = amountPaid;
	}

	public Double getOutstandingBalance() {
		return outstandingBalance;
	}

	public void setOutstandingBalance(Double outstandingBalance) {
		this.outstandingBalance = outstandingBalance;
	}

	public Double getTotalPenalties() {
		return totalPenalties;
	}

	public void setTotalPenalties(Double totalPenalties) {
		this.totalPenalties = totalPenalties;
	}

	public Double getPenaltiesPaid() {
		return penaltiesPaid;
	}

	public void setPenaltiesPaid(Double penaltiesPaid) {
		this.penaltiesPaid = penaltiesPaid;
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

	public Long getId() {
		return id;
	}

	public Integer getExpectedPaymentNumber() {
		return expectedPaymentNumber;
	}

	public void setExpectedPaymentNumber(Integer expectedPaymentNumber) {
		this.expectedPaymentNumber = expectedPaymentNumber;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	
}
