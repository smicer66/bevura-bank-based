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
@Table(name="groups")  
public class Group implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@OneToOne  
    @JoinColumn
    User createdByUser;
	@Column(nullable = false)
	String name;
	String shortName;
	String story;
	String iconUrl;
	@Column(nullable = false, unique = true)
	String groupCode;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = false)
	Boolean isActive;
	@Column(nullable = false)
	Boolean isOpen;
	@Column(nullable = false)
	Integer maximumMembers;
	@Column(nullable = true)
	String backgroundColor;
	@Column(nullable = true)
	String fontColor;
	@Column(nullable = true)
	Boolean isContributionPackageSet;
	@Column(nullable = true)
	Boolean isLoanPackageSet;
	String currentContributionAmount;
	String currentContributionInterval;
	String currentMaximumLoan;
	String currentLoanInterestRate;
	String currentLoanInterestRateType;
	
	
	
	
	public Group() {
	}

	public Group(String backgroundColor, String fontColor, User createdByUser, String name, String shortName, String story, String iconUrl, String groupCode, Boolean isActive, Boolean isOpen, Integer maximumMembers) {
		super();
		this.createdByUser = createdByUser;
		this.name = name;
		this.story = story;
		this.iconUrl = iconUrl;
		this.groupCode = groupCode;
		this.isActive = isActive;
		this.isOpen = isOpen;
		this.shortName = shortName;
		this.maximumMembers = maximumMembers;
		this.backgroundColor = backgroundColor;
		this.fontColor = fontColor;
		
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

	public User getCreatedByUser() {
		return createdByUser;
	}

	public void setCreatedByUser(User createdByUser) {
		this.createdByUser = createdByUser;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStory() {
		return story;
	}

	public void setStory(String story) {
		this.story = story;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Long getId() {
		return id;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsOpen() {
		return isOpen;
	}

	public void setIsOpen(Boolean isOpen) {
		this.isOpen = isOpen;
	}

	public Integer getMaximumMembers() {
		return maximumMembers;
	}

	public void setMaximumMembers(Integer maximumMembers) {
		this.maximumMembers = maximumMembers;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getFontColor() {
		return fontColor;
	}

	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}

	public Boolean getIsContributionPackageSet() {
		return isContributionPackageSet;
	}

	public void setIsContributionPackageSet(Boolean isContributionPackageSet) {
		this.isContributionPackageSet = isContributionPackageSet;
	}

	public Boolean getIsLoanPackageSet() {
		return isLoanPackageSet;
	}

	public void setIsLoanPackageSet(Boolean isLoanPackageSet) {
		this.isLoanPackageSet = isLoanPackageSet;
	}

	public String getCurrentContributionAmount() {
		return currentContributionAmount;
	}

	public void setCurrentContributionAmount(String currentContributionAmount) {
		this.currentContributionAmount = currentContributionAmount;
	}

	public String getCurrentContributionInterval() {
		return currentContributionInterval;
	}

	public void setCurrentContributionInterval(String currentContributionInterval) {
		this.currentContributionInterval = currentContributionInterval;
	}

	public String getCurrentMaximumLoan() {
		return currentMaximumLoan;
	}

	public void setCurrentMaximumLoan(String currentMaximumLoan) {
		this.currentMaximumLoan = currentMaximumLoan;
	}

	public String getCurrentLoanInterestRate() {
		return currentLoanInterestRate;
	}

	public void setCurrentLoanInterestRate(String currentLoanInterestRate) {
		this.currentLoanInterestRate = currentLoanInterestRate;
	}

	public String getCurrentLoanInterestRateType() {
		return currentLoanInterestRateType;
	}

	public void setCurrentLoanInterestRateType(String currentLoanInterestRateType) {
		this.currentLoanInterestRateType = currentLoanInterestRateType;
	}

	
}
