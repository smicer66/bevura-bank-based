package com.probase.probasepay.models;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

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

import org.json.JSONException;
import org.json.JSONObject;

import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.CustomerStatus;
import com.probase.probasepay.enumerations.CustomerType;
import com.probase.probasepay.enumerations.Gender;
import com.probase.probasepay.enumerations.MeansOfIdentificationType;
import com.probase.probasepay.util.UtilityHelper;
  
@Entity
@Table(name="customers")  
public class Customer implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String verificationNumber;
	@Column(nullable = false)
	String firstName;
	@Column(nullable = false)
	String lastName;
	String otherName;
	Date dateOfBirth;
	Gender gender;
	String addressLine1;
	String addressLine2;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	District locationDistrict;
	@Column(nullable = false)
	String contactMobile;
	String altContactMobile;
	String contactEmail;
	String altContactEmail;
	@Column(nullable = false)
	CustomerStatus status;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	String customerImage;
	CustomerType customerType;
	String mobileMoneyPassword;
	MeansOfIdentificationType meansOfIdentificationType;
	String meansOfIdentificationNumber;
	@OneToOne  
    @JoinColumn
	Device device;
	@OneToOne  
    @JoinColumn
	User user;
	
	
	public Customer(String verificationNumber, String firstName,
			String lastName, String otherName, Date dateOfBirth, Gender gender,
			String addressLine1, String addressLine2,
			District locationDistrict, String contactMobile,
			String altContactMobile, String contactEmail,
			String altContactEmail, CustomerStatus status,
			String customerImage, CustomerType customerType,
			MeansOfIdentificationType meansOfIdentificationType,
			String meansOfIdentificationNumber, Device device, User user) {
		super();
		this.verificationNumber = verificationNumber;
		this.firstName = firstName;
		this.lastName = lastName;
		this.otherName = otherName;
		this.dateOfBirth = dateOfBirth;
		this.gender = gender;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.locationDistrict = locationDistrict;
		this.contactMobile = contactMobile;
		this.altContactMobile = altContactMobile;
		this.contactEmail = contactEmail;
		this.altContactEmail = altContactEmail;
		this.status = status;
		this.customerImage = customerImage;
		this.customerType = customerType;
		this.meansOfIdentificationType = meansOfIdentificationType;
		this.meansOfIdentificationNumber = meansOfIdentificationNumber;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.device = device;
		this.user = user;
	}

	public Customer() {
		super();
	}

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
	public CustomerStatus getStatus() {
	    return status;
	}
	
	@Enumerated(EnumType.STRING)
	public CustomerType getCustomerType() {
		return customerType;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVerificationNumber() {
		return verificationNumber;
	}

	public void setVerificationNumber(String verificationNumber) {
		this.verificationNumber = verificationNumber;
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

	public void setMobileMoneyPassword(String mobileMoneyPassword) {
		this.mobileMoneyPassword = mobileMoneyPassword;
	}

	public String getMobileMoneyPassword() {
		return mobileMoneyPassword;
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

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getAltContactMobile() {
		return altContactMobile;
	}

	public void setAltContactMobile(String altContactMobile) {
		this.altContactMobile = altContactMobile;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getAltContactEmail() {
		return altContactEmail;
	}

	public void setAltContactEmail(String altContactEmail) {
		this.altContactEmail = altContactEmail;
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

	public void setStatus(CustomerStatus status) {
		this.status = status;
	}

	public String getCustomerImage() {
		return customerImage;
	}

	public void setCustomerImage(String customerImage) {
		this.customerImage = customerImage;
	}

	public void setCustomerType(CustomerType customerType) {
		this.customerType = customerType;
	}

	public MeansOfIdentificationType getMeansOfIdentificationType() {
		return meansOfIdentificationType;
	}

	public void setMeansOfIdentificationType(
			MeansOfIdentificationType meansOfIdentificationType) {
		this.meansOfIdentificationType = meansOfIdentificationType;
	}

	public String getMeansOfIdentificationNumber() {
		return meansOfIdentificationNumber;
	}

	public void setMeansOfIdentificationNumber(String meansOfIdentificationNumber) {
		this.meansOfIdentificationNumber = meansOfIdentificationNumber;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}
	
	
	public JSONObject getCustomerDetails()
	{
		JSONObject oneCustomer = new JSONObject();
		try {
			oneCustomer.put("id", this.getId());
			oneCustomer.put("firstName", UtilityHelper.getValue(this.getFirstName()));
			oneCustomer.put("lastName", UtilityHelper.getValue(this.getLastName()));
			oneCustomer.put("otherName", UtilityHelper.getValue(this.getOtherName()));
			oneCustomer.put("gender", UtilityHelper.getValue(this.getGender()==null ? null : this.getGender().name()));
			oneCustomer.put("addressLine1", UtilityHelper.getValue(this.getAddressLine1()));
			oneCustomer.put("addressLine2", UtilityHelper.getValue(this.getAddressLine2()));
			oneCustomer.put("locationDistrict", UtilityHelper.getValue(this.getLocationDistrict()==null ? "" : this.getLocationDistrict().getName()));
			oneCustomer.put("locationDistrictId", this.getLocationDistrict()==null ? null : this.getLocationDistrict().getId());
			oneCustomer.put("locationProvince", this.getLocationDistrict()==null ? "" : this.getLocationDistrict().getProvinceName());
			oneCustomer.put("locationProvinceId", this.getLocationDistrict()==null ? null : this.getLocationDistrict().getProvinceId());
			oneCustomer.put("dateOfBirth", UtilityHelper.getDateOfBirth(this.getDateOfBirth()));
			oneCustomer.put("contactMobile", UtilityHelper.getValue(this.getContactMobile()));
			oneCustomer.put("altContactMobile", UtilityHelper.getValue(this.getAltContactMobile()));
			oneCustomer.put("contactEmail", UtilityHelper.getValue(this.getContactEmail()));
			oneCustomer.put("altContactEmail", UtilityHelper.getValue(this.getAltContactEmail()));
			oneCustomer.put("verificationNumber", UtilityHelper.getValue(this.getVerificationNumber()));
			oneCustomer.put("status", UtilityHelper.getValue(this.getStatus().name()));
			oneCustomer.put("customerType", UtilityHelper.getValue(this.getCustomerType()==null ? null : this.getCustomerType().name()));
			oneCustomer.put("customerImage", UtilityHelper.getValue(this.getCustomerImage()));
			
			return oneCustomer;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
	
}
