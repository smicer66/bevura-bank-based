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
@Table(name="group_loan_repayments_expected")  
public class GroupLoanRepaymentsExpected implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@OneToOne  
    @JoinColumn
	GroupLoan groupLoan;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = false)
	Date expectedRepaymentDate;
	Boolean isCompleted;
	@Column(nullable = false)
	Double totalAmountRepaid;
	@Column(nullable = false)
	Double totalPenaltiesRepaid;
	@Column(nullable = false)
	Double totalInterestRepaid;
	@Column(nullable = false)
	Double totalPrincipalRepaid;
	@Column(nullable = false)
	Double totalPenaltiesIncurred;
	@Column(nullable = false)
	Double totalInterestIncurred;
	@Column(nullable = false)
	Double totalPrincipalIncurred;
	Integer installmentNumber;
	
	
	
	
	public GroupLoanRepaymentsExpected() {
	}

	public GroupLoanRepaymentsExpected(GroupLoan groupLoan,
			Date expectedRepaymentDate, Boolean isCompleted,
			Double totalPrincipalIncurred, Double totalInterestIncurred, 
			Double totalPenaltiesIncurred, Double totalAmountRepaid, 
			Double totalPenaltiesRepaid, Double totalInterestRepaid, 
			Double totalPrincipalRepaid, Integer installmentNumber) {
		super();
		this.groupLoan = groupLoan;
		this.expectedRepaymentDate = expectedRepaymentDate;
		this.isCompleted = isCompleted;
		this.totalAmountRepaid = totalAmountRepaid;
		this.totalPenaltiesIncurred = totalPenaltiesIncurred;
		this.totalPenaltiesRepaid = totalPenaltiesRepaid;
		this.totalPrincipalIncurred = totalPrincipalIncurred;
		this.totalInterestIncurred = totalInterestIncurred;
		this.totalInterestRepaid = totalInterestRepaid;
		this.totalPrincipalRepaid = totalPrincipalRepaid;
		this.installmentNumber = installmentNumber;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public GroupLoan getGroupLoan() {
		return groupLoan;
	}

	public void setGroupLoan(GroupLoan groupLoan) {
		this.groupLoan = groupLoan;
	}

	public Date getExpectedRepaymentDate() {
		return expectedRepaymentDate;
	}

	public void setExpectedRepaymentDate(Date expectedRepaymentDate) {
		this.expectedRepaymentDate = expectedRepaymentDate;
	}

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public Double getTotalAmountRepaid() {
		return totalAmountRepaid;
	}

	public void setTotalAmountRepaid(Double totalAmountRepaid) {
		this.totalAmountRepaid = totalAmountRepaid;
	}

	public Double getTotalPenaltiesIncurred() {
		return totalPenaltiesIncurred;
	}

	public void setTotalPenaltiesIncurred(Double totalPenaltiesIncurred) {
		this.totalPenaltiesIncurred = totalPenaltiesIncurred;
	}

	public Double getTotalPenaltiesRepaid() {
		return totalPenaltiesRepaid;
	}

	public void setTotalPenaltiesRepaid(Double totalPenaltiesRepaid) {
		this.totalPenaltiesRepaid = totalPenaltiesRepaid;
	}

	public Long getId() {
		return id;
	}

	public Double getTotalPrincipalIncurred() {
		return totalPrincipalIncurred;
	}

	public void setTotalPrincipalIncurred(Double totalPrincipalIncurred) {
		this.totalPrincipalIncurred = totalPrincipalIncurred;
	}

	public Double getTotalInterestIncurred() {
		return totalInterestIncurred;
	}

	public void setTotalInterestIncurred(Double totalInterestIncurred) {
		this.totalInterestIncurred = totalInterestIncurred;
	}

	public Double getTotalInterestRepaid() {
		return totalInterestRepaid;
	}

	public void setTotalInterestRepaid(Double totalInterestRepaid) {
		this.totalInterestRepaid = totalInterestRepaid;
	}

	public Double getTotalPrincipalRepaid() {
		return totalPrincipalRepaid;
	}

	public void setTotalPrincipalRepaid(Double totalPrincipalRepaid) {
		this.totalPrincipalRepaid = totalPrincipalRepaid;
	}

	public Integer getInstallmentNumber() {
		return installmentNumber;
	}

	public void setInstallmentNumber(Integer installmentNumber) {
		this.installmentNumber = installmentNumber;
	}



	
}
