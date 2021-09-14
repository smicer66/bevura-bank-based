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
import com.probase.probasepay.enumerations.ProbasePayCurrency;

import javax.persistence.Enumerated;

  
@Entity
@Table(name="contribution_packages")  
public class ContributionPackage implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@OneToOne  
    @JoinColumn
    User createdByUser;
	@OneToOne  
    @JoinColumn
    Group group;
	@Column(nullable = false)
    String packageName; 
	@Column(nullable = false)
	Double contributionAmount; 
	@Column(nullable = false)
	Integer contributionPeriod; 
	@Column(nullable = false)
	Integer numberOfPayments; 
	@Column(nullable = false)
	PeriodType contributionPeriodType; 
	@Column(nullable = false)
	ProbasePayCurrency currency;
	Double penaltyApplicable;
	PenaltyApplicableType penaltyApplicableType;
	String story;
	Date startDate;
	Date endDate;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	
	
	public ContributionPackage() {
	}





	public ContributionPackage(User createdByUser, String packageName,
			Double contributionAmount, Integer contributionPeriod,
			PeriodType contributionPeriodType,
			Double penaltyApplicable, PenaltyApplicableType penaltyApplicableType, Group group, ProbasePayCurrency currencyType, 
			String story, Integer numberOfPayments) {
		super();
		this.createdByUser = createdByUser;
		this.packageName = packageName;
		this.contributionAmount = contributionAmount;
		this.contributionPeriod = contributionPeriod;
		this.contributionPeriodType = contributionPeriodType;
		this.penaltyApplicable = penaltyApplicable;
		this.penaltyApplicableType = penaltyApplicableType;
		this.group = group;
		this.currency = currencyType;
		this.story = story;
		this.numberOfPayments = numberOfPayments;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}



	
	
	@Enumerated(EnumType.STRING)
	public ProbasePayCurrency getCurrency() {
	    return this.currency;
	}
	
	public void setCurrency(ProbasePayCurrency currency) {
		this.currency= currency ;
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

	public User getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(User createdByUser) {
		this.createdByUser = createdByUser;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Double getContributionAmount() {
		return contributionAmount;
	}

	public void setContributionAmount(Double contributionAmount) {
		this.contributionAmount = contributionAmount;
	}

	public Integer getContributionPeriod() {
		return contributionPeriod;
	}

	public void setContributionPeriod(Integer contributionPeriod) {
		this.contributionPeriod = contributionPeriod;
	}

	@Enumerated(EnumType.STRING)
	public PeriodType getContributionPeriodType() {
		return contributionPeriodType;
	}

	public void setContributionPeriodType(PeriodType contributionPeriodType) {
		this.contributionPeriodType = contributionPeriodType;
	}

	public Double getPenaltyApplicable() {
		return penaltyApplicable;
	}

	public void setPenaltyApplicable(Double penaltyApplicable) {
		this.penaltyApplicable = penaltyApplicable;
	}

	@Enumerated(EnumType.STRING)
	public PenaltyApplicableType getPenaltyApplicableType() {
		return penaltyApplicableType;
	}

	public void setPenaltyApplicableType(PenaltyApplicableType penaltyApplicableType) {
		this.penaltyApplicableType = penaltyApplicableType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}





	public String getStory() {
		return story;
	}





	public void setStory(String story) {
		this.story = story;
	}






	public Integer getNumberOfPayments() {
		return numberOfPayments;
	}





	public void setNumberOfPayments(Integer numberOfPayments) {
		this.numberOfPayments = numberOfPayments;
	}


}
