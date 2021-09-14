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

import com.probase.probasepay.enumerations.PenaltyApplicableType;
import com.probase.probasepay.enumerations.PeriodType;
import com.probase.probasepay.enumerations.RepaymentStrategy;
import com.probase.probasepay.enumerations.TenorType;

import javax.persistence.Enumerated;

  
@Entity
@Table(name="group_loan_terms")  
public class GroupLoanTerms implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Double minimumTotalContribution; 
	@OneToOne  
    @JoinColumn
	ContributionPackage contributionPackage;
	@Column(nullable = false)
	Double maximumPrincipalLoanable;
	@Column(nullable = false)
	Double minimumPrincipalLoanable;
	@Column(nullable = false)
	Integer minimumTerm;
	@Column(nullable = false)
	Integer maximumTerm;
	@Column(nullable = false)
	PeriodType interestType;
	@Column(nullable = false)
	Double interestRate;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = false)
	TenorType repaymentTenorType;			//LOAN PERIOD
	@Column(nullable = false)
	RepaymentStrategy repaymentStrategy;
	@Column(nullable = false)
	Double penalty;
	PenaltyApplicableType penaltyApplicableType;
	
	
	
	
	public GroupLoanTerms(Double minimumTotalContribution,
			ContributionPackage contributionPackage,
			Double maximumPrincipalLoanable, Double minimumPrincipalLoanable,
			Integer minimumTerm, Integer maximumTerm, PeriodType interestType,
			Double interestRate, TenorType repaymentTenorType,
			RepaymentStrategy repaymentStrategy, Double penalty, PenaltyApplicableType penaltyApplicableType) {
		super();
		this.minimumTotalContribution = minimumTotalContribution;
		this.contributionPackage = contributionPackage;
		this.maximumPrincipalLoanable = maximumPrincipalLoanable;
		this.minimumPrincipalLoanable = minimumPrincipalLoanable;
		this.minimumTerm = minimumTerm;
		this.maximumTerm = maximumTerm;
		this.interestType = interestType;
		this.interestRate = interestRate;
		this.repaymentTenorType = repaymentTenorType;
		this.repaymentStrategy = repaymentStrategy;
		this.penalty = penalty;
		this.penaltyApplicableType = penaltyApplicableType;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}


	public GroupLoanTerms() {
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

	public Double getMinimumTotalContribution() {
		return minimumTotalContribution;
	}

	public void setMinimumTotalContribution(Double minimumTotalContribution) {
		this.minimumTotalContribution = minimumTotalContribution;
	}

	public ContributionPackage getContributionPackage() {
		return contributionPackage;
	}

	public void setContributionPackage(ContributionPackage contributionPackage) {
		this.contributionPackage = contributionPackage;
	}

	public Double getMaximumPrincipalLoanable() {
		return maximumPrincipalLoanable;
	}

	public void setMaximumPrincipalLoanable(Double maximumPrincipalLoanable) {
		this.maximumPrincipalLoanable = maximumPrincipalLoanable;
	}

	public Integer getMinimumTerm() {
		return minimumTerm;
	}

	public void setMinimumTerm(Integer minimumTerm) {
		this.minimumTerm = minimumTerm;
	}

	public Integer getMaximumTerm() {
		return maximumTerm;
	}

	public void setMaximumTerm(Integer maximumTerm) {
		this.maximumTerm = maximumTerm;
	}

	public TenorType getRepaymentTenorType() {
		return repaymentTenorType;
	}

	public void setRepaymentTenorType(TenorType repaymentTenorType) {
		this.repaymentTenorType = repaymentTenorType;
	}

	public Long getId() {
		return id;
	}

	@Enumerated(EnumType.STRING)
	public RepaymentStrategy getRepaymentStrategy() {
		return repaymentStrategy;
	}

	public void setRepaymentStrategy(RepaymentStrategy repaymentStrategy) {
		this.repaymentStrategy = repaymentStrategy;
	}

	public Double getMinimumPrincipalLoanable() {
		return minimumPrincipalLoanable;
	}

	public void setMinimumPrincipalLoanable(Double minimumPrincipalLoanable) {
		this.minimumPrincipalLoanable = minimumPrincipalLoanable;
	}


	public PeriodType getInterestType() {
		return interestType;
	}


	public void setInterestType(PeriodType interestType) {
		this.interestType = interestType;
	}


	public Double getInterestRate() {
		return interestRate;
	}


	public void setInterestRate(Double interestRate) {
		this.interestRate = interestRate;
	}


	public Double getPenalty() {
		return penalty;
	}


	public void setPenalty(Double penalty) {
		this.penalty = penalty;
	}

	@Enumerated(EnumType.STRING)
	public PenaltyApplicableType getPenaltyApplicableType() {
		return penaltyApplicableType;
	}


	public void setPenaltyApplicableType(PenaltyApplicableType penaltyApplicableType) {
		this.penaltyApplicableType = penaltyApplicableType;
	}


	
}
