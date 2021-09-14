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
@Table(name="group_loan_penalties")  
public class GroupLoanPenalty implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 
	@Column(nullable = false)
	Boolean isRepaid;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = false)
	String penaltyDescription;
	@Column(nullable = false)
	Double penaltyAmount;
	@Column(nullable = false)
	Double amountPaid;
	@OneToOne  
    @JoinColumn
    GroupLoan groupLoan;
	
	
	
	

	public GroupLoanPenalty() {
	}

	public GroupLoanPenalty(Boolean isRepaid, String penaltyDescription,
			Double penaltyAmount, Double amountPaid, GroupLoan groupLoan) {
		super();
		this.isRepaid = isRepaid;
		this.penaltyDescription = penaltyDescription;
		this.penaltyAmount = penaltyAmount;
		this.amountPaid = amountPaid;
		this.groupLoan = groupLoan;
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

	public Boolean getIsRepaid() {
		return isRepaid;
	}

	public void setIsRepaid(Boolean isRepaid) {
		this.isRepaid = isRepaid;
	}

	public String getPenaltyDescription() {
		return penaltyDescription;
	}

	public void setPenaltyDescription(String penaltyDescription) {
		this.penaltyDescription = penaltyDescription;
	}

	public Double getPenaltyAmount() {
		return penaltyAmount;
	}

	public void setPenaltyAmount(Double penaltyAmount) {
		this.penaltyAmount = penaltyAmount;
	}

	public Double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(Double amountPaid) {
		this.amountPaid = amountPaid;
	}

	public GroupLoan getGroupLoan() {
		return groupLoan;
	}

	public void setGroupLoan(GroupLoan groupLoan) {
		this.groupLoan = groupLoan;
	}

	public Long getId() {
		return id;
	}

	
}
