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
@Table(name="contribution_package_debits")  
public class ContributionPackageDebit implements Serializable{  
	private static final long serialVersionUID = 5457558421999610410L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	Double amount; 
	@OneToOne  
    @JoinColumn
	VBCustomerAccount destinationCustomerAccount; 
	@OneToOne  
    @JoinColumn
	GroupMember groupMember;
	@OneToOne  
    @JoinColumn
	ContributionPackage contributionPackage; 
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	
	
	
	public ContributionPackageDebit(Double amount,
			VBCustomerAccount destinationCustomerAccount,
			GroupMember groupMember, ContributionPackage contributionPackage) {
		super();
		this.amount = amount;
		this.destinationCustomerAccount = destinationCustomerAccount;
		this.groupMember = groupMember;
		this.contributionPackage = contributionPackage;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}


	public ContributionPackageDebit() {
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


	public Double getAmount() {
		return amount;
	}


	public void setAmount(Double amount) {
		this.amount = amount;
	}


	public VBCustomerAccount getDestinationCustomerAccount() {
		return destinationCustomerAccount;
	}


	public void setDestinationCustomerAccount(
			VBCustomerAccount destinationCustomerAccount) {
		this.destinationCustomerAccount = destinationCustomerAccount;
	}


	public GroupMember getGroupMember() {
		return groupMember;
	}


	public void setGroupMember(GroupMember groupMember) {
		this.groupMember = groupMember;
	}


	public ContributionPackage getContributionPackage() {
		return contributionPackage;
	}


	public void setContributionPackage(ContributionPackage contributionPackage) {
		this.contributionPackage = contributionPackage;
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


	public Long getId() {
		return id;
	}
	


    
}

