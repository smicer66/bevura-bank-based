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

import com.probase.probasepay.enumerations.BankStaffStatus;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.Gender;
  
@Entity
@Table(name="bankstaffs")  
public class BankStaff implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String firstName;
	@Column(nullable = false)
	String lastName;
	String otherName;
	@Column(nullable = false)
	String identityNumber;
	Date dateOfBirth;
	Gender gender;
	@Column(nullable = false)
	String addressLine1;
	String addressLine2;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	District locationDistrict;
	@Column(nullable = false)
	Long provinceId;
	@OneToOne  
    @JoinColumn
	User user;
	@OneToOne  
    @JoinColumn
	Bank bank;
	@Column(nullable = false)
	String branchCode;
	
	BankStaffStatus status;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	
	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}

	
	
	@Enumerated(EnumType.STRING)
	public Gender getGender() {
	    return gender;
	}
	
	@Enumerated(EnumType.STRING)
	public BankStaffStatus getStatus() {
	    return status;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getOtherName() {
		return otherName;
	}

	public void setOtherName(String otherName) {
		this.otherName = otherName;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public District getLocationDistrict() {
		return locationDistrict;
	}

	public void setLocationDistrict(District locationDistrict) {
		this.locationDistrict = locationDistrict;
	}


	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public Date getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Date updated_at) {
		this.updated_at = updated_at;
	}

	public Date getDeleted_at() {
		return deleted_at;
	}

	public void setDeleted_at(Date deleted_at) {
		this.deleted_at = deleted_at;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public void setStatus(BankStaffStatus status) {
		this.status = status;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
		this.bank = bank;
	}

	public String getIdentityNumber() {
		return identityNumber;
	}

	public void setIdentityNumber(String identityNumber) {
		this.identityNumber = identityNumber;
	}

	public String getBranchCode() {
		return branchCode;
	}

	public void setBranchCode(String branchCode) {
		this.branchCode = branchCode;
	}

	public Long getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(Long provinceId) {
		this.provinceId = provinceId;
	}
}
