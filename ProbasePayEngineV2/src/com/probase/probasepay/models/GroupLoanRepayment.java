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
import javax.persistence.UniqueConstraint;

  
@Entity
@Table(name="group_loan_repayments")  
public class GroupLoanRepayment implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@OneToOne  
    @JoinColumn
	GroupLoanRepaymentsExpected groupLoanRepaymentExpected;
	@Column(nullable = false)
	Double amountRepaid;
	@Column(nullable = false)
	Double principalAmountRepaid;
	@Column(nullable = false)
	Double interestAmountRepaid;
	@Column(nullable = false)
	Double penaltyAmountRepaid;
	Long groupId;
	
	
	
	public GroupLoanRepayment() {
	}

	public GroupLoanRepayment(
			GroupLoanRepaymentsExpected groupLoanRepaymentExpected,
			Double amountRepaid, Double principalAmountRepaid, Double interestAmountRepaid, 
			Double penaltyAmountRepaid, Long groupId) {
		super();
		this.groupLoanRepaymentExpected = groupLoanRepaymentExpected;
		this.amountRepaid = amountRepaid;
		this.principalAmountRepaid = principalAmountRepaid;
		this.interestAmountRepaid = interestAmountRepaid;
		this.penaltyAmountRepaid = penaltyAmountRepaid;
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

	public GroupLoanRepaymentsExpected getGroupLoanRepaymentExpected() {
		return groupLoanRepaymentExpected;
	}

	public void setGroupLoanRepaymentExpected(
			GroupLoanRepaymentsExpected groupLoanRepaymentExpected) {
		this.groupLoanRepaymentExpected = groupLoanRepaymentExpected;
	}

	public Double getAmountRepaid() {
		return amountRepaid;
	}

	public void setAmountRepaid(Double amountRepaid) {
		this.amountRepaid = amountRepaid;
	}

	public Long getId() {
		return id;
	}

	public Double getPrincipalAmountRepaid() {
		return principalAmountRepaid;
	}

	public void setPrincipalAmountRepaid(Double principalAmountRepaid) {
		this.principalAmountRepaid = principalAmountRepaid;
	}

	public Double getInterestAmountRepaid() {
		return interestAmountRepaid;
	}

	public void setInterestAmountRepaid(Double interestAmountRepaid) {
		this.interestAmountRepaid = interestAmountRepaid;
	}

	public Double getPenaltyAmountRepaid() {
		return penaltyAmountRepaid;
	}

	public void setPenaltyAmountRepaid(Double penaltyAmountRepaid) {
		this.penaltyAmountRepaid = penaltyAmountRepaid;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}


	
}
