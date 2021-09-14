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

import com.probase.probasepay.enumerations.GroupLoanStatus;
import com.probase.probasepay.enumerations.TenorType;

import javax.persistence.Enumerated;


  
@Entity
@Table(name="group_loans")  
public class GroupLoan implements Serializable {  
	
	private static final long serialVersionUID = -4978223012667661805L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@OneToOne  
    @JoinColumn
	ContributionPackage contributionPackage;
	@OneToOne  
    @JoinColumn
	GroupMember groupMember;
	@Column(nullable = false)
	Double principal;
	@Column(nullable = false)
	Double interestRate;
	@Column(nullable = false)
	Integer tenor;
	@Column(nullable = false)
	TenorType tenorType;
	@Column(nullable = false)
	Date dateSubmitted;
	@Column(nullable = true)
	Date dateApproved;
	@Column(nullable = false)
	Date repaymentStartDate;
	@Column(nullable = false)
	GroupLoanStatus status;
	@GeneratedValue(strategy=GenerationType.AUTO)
	String loanAccountNo;
	@Column(nullable = false)
	Double incurredInterest;
	@Column(nullable = false)
	Double incurredPenalties;
	@Column(nullable = false)
	Double outstandingBalance;
	@Column(nullable = false)
	Double totalPrincipalRepaid;
	@Column(nullable = false)
	Double totalInterestRepaid;
	@Column(nullable = false)
	Double totalPenaltiesRepaid;
	@OneToOne  
    @JoinColumn
	GroupLoanTerms groupLoanTerm;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	Date lastRepaymentDate;
	Long groupId;
	@Column(nullable = false)
	Double expectedInterest;
	String loanDetails;
	
	
	
	
	public GroupLoan() {
	}


	public GroupLoan(ContributionPackage contributionPackage,
			GroupMember groupMember, Double principal,
			Double interestRate, Integer tenor, TenorType tenorType,
			Date dateSubmitted, Date dateApproved, Date repaymentStartDate,
			GroupLoanStatus status, String loanAccountNo,
			Double incurredInterest, Double incurredPenalties,
			Double outstandingBalance, Double totalPrincipalRepaid,
			Double totalInterestRepaid, Double totalPenaltiesRepaid,
			GroupLoanTerms groupLoanTerm, Long groupId, Double expectedInterest, 
			String loanDetails) {
		super();
		this.contributionPackage = contributionPackage;
		this.groupMember = groupMember;
		this.principal = principal;
		this.interestRate = interestRate;
		this.tenor = tenor;
		this.tenorType = tenorType;
		this.dateSubmitted = dateSubmitted;
		this.dateApproved = dateApproved;
		this.repaymentStartDate = repaymentStartDate;
		this.status = status;
		this.loanAccountNo = loanAccountNo;
		this.incurredInterest = incurredInterest;
		this.incurredPenalties = incurredPenalties;
		this.outstandingBalance = outstandingBalance;
		this.totalPrincipalRepaid = totalPrincipalRepaid;
		this.totalInterestRepaid = totalInterestRepaid;
		this.totalPenaltiesRepaid = totalPenaltiesRepaid;
		this.groupLoanTerm = groupLoanTerm;
		this.groupId = groupId;
		this.expectedInterest = expectedInterest;
		this.createdAt = new Date();
		this.updatedAt = new Date();
		this.loanDetails = loanDetails;
		
	}

	
	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
		this.createdAt = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = new Date();
	}

	
	
	@Enumerated(EnumType.STRING)
	public GroupLoanStatus getGroupLoanStatus() {
	    return this.status;
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

	public ContributionPackage getContributionPackage() {
		return contributionPackage;
	}

	public void setContributionPackage(ContributionPackage contributionPackage) {
		this.contributionPackage = contributionPackage;
	}

	public GroupMember getGroupMember() {
		return groupMember;
	}

	public void setGroupMember(GroupMember groupMember) {
		this.groupMember = groupMember;
	}

	public Double getPrincipal() {
		return principal;
	}

	public void setPrincipal(Double principal) {
		this.principal = principal;
	}

	public Double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(Double interestRate) {
		this.interestRate = interestRate;
	}

	public Integer getTenor() {
		return tenor;
	}

	public void setTenor(Integer tenor) {
		this.tenor = tenor;
	}

	@Enumerated(EnumType.STRING)
	public TenorType getTenorType() {
		return tenorType;
	}

	public void setTenorType(TenorType tenorType) {
		this.tenorType = tenorType;
	}

	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(Date dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public Date getDateApproved() {
		return dateApproved;
	}

	public void setDateApproved(Date dateApproved) {
		this.dateApproved = dateApproved;
	}

	public Date getRepaymentStartDate() {
		return repaymentStartDate;
	}

	public void setRepaymentStartDate(Date repaymentStartDate) {
		this.repaymentStartDate = repaymentStartDate;
	}

	public GroupLoanStatus getStatus() {
		return status;
	}

	public void setStatus(GroupLoanStatus status) {
		this.status = status;
	}

	public String getLoanAccountNo() {
		return loanAccountNo;
	}

	public void setLoanAccountNo(String loanAccountNo) {
		this.loanAccountNo = loanAccountNo;
	}

	public Double getIncurredInterest() {
		return incurredInterest;
	}

	public void setIncurredInterest(Double incurredInterest) {
		this.incurredInterest = incurredInterest;
	}

	public Double getIncurredPenalties() {
		return incurredPenalties;
	}

	public void setIncurredPenalties(Double incurredPenalties) {
		this.incurredPenalties = incurredPenalties;
	}

	public Double getOutstandingBalance() {
		return outstandingBalance;
	}

	public void setOutstandingBalance(Double outstandingBalance) {
		this.outstandingBalance = outstandingBalance;
	}

	public Double getTotalPrincipalRepaid() {
		return totalPrincipalRepaid;
	}

	public void setTotalPrincipalRepaid(Double totalPrincipalRepaid) {
		this.totalPrincipalRepaid = totalPrincipalRepaid;
	}

	public Double getTotalInterestRepaid() {
		return totalInterestRepaid;
	}

	public void setTotalInterestRepaid(Double totalInterestRepaid) {
		this.totalInterestRepaid = totalInterestRepaid;
	}

	public Double getTotalPenaltiesRepaid() {
		return totalPenaltiesRepaid;
	}

	public void setTotalPenaltiesRepaid(Double totalPenaltiesRepaid) {
		this.totalPenaltiesRepaid = totalPenaltiesRepaid;
	}

	public GroupLoanTerms getGroupLoanTerm() {
		return groupLoanTerm;
	}

	public void setGroupLoanTerm(GroupLoanTerms groupLoanTerm) {
		this.groupLoanTerm = groupLoanTerm;
	}

	public Long getId() {
		return id;
	}


	public Date getLastRepaymentDate() {
		return lastRepaymentDate;
	}


	public void setLastRepaymentDate(Date lastRepaymentDate) {
		this.lastRepaymentDate = lastRepaymentDate;
	}


	public Long getGroupId() {
		return groupId;
	}


	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}


	public Double getExpectedInterest() {
		return expectedInterest;
	}


	public void setExpectedInterest(Double expectedInterest) {
		this.expectedInterest = expectedInterest;
	}


	public String getLoanDetails() {
		return loanDetails;
	}


	public void setLoanDetails(String loanDetails) {
		this.loanDetails = loanDetails;
	}
	
    
}

