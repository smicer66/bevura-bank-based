package com.probase.probasepay.models;

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

import org.json.JSONObject;

import com.probase.probasepay.enumerations.OTPType;
import com.probase.probasepay.enumerations.RoleType;
import com.probase.probasepay.enumerations.UserStatus;
  
@Entity
@Table(name="users")  
public class User {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id; 

	Integer failedLoginCount;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean lockOut;
	String password;
	@Column(nullable = false)
	String username;
	@Column(nullable = false)
	UserStatus status;
	String salt;
	RoleType roleType;
	String privileges;
	@Column(nullable = false)
	Date created_at;
	Date updated_at;
	Date deleted_at;
	@Column(nullable = false)
	String firstName;
	@Column(nullable = false)
	String lastName;
	String otherName;
	String mobileNo;
	String userEmail;
	String passportImage;
	OTPType otpType;
	
	String profilePix;
	
	String otp;
	
	String pin;
	Boolean activateMobilePin;
	Boolean activateOTPLogin;
	Boolean activateOTPDebit;
	
	Long acquirer_id;
	Integer securityQuestionId;
	String securityQuestionAnswer;
	
	@PrePersist
	protected void onCreate() {
		this.created_at = new Date();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updated_at = new Date();
	}

	
	
	@Enumerated(EnumType.STRING)
	public UserStatus getStatus() {
	    return status;
	}
	

	@Enumerated(EnumType.STRING)
	public RoleType getRoleType() {
	    return roleType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFailedLoginCount() {
		return failedLoginCount;
	}

	public void setFailedLoginCount(Integer failedLoginCount) {
		this.failedLoginCount = failedLoginCount;
	}

	public Boolean getLockOut() {
		return lockOut;
	}

	public void setLockOut(Boolean lockOut) {
		this.lockOut = lockOut;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
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

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public String getPrivileges() {
		return privileges;
	}

	public void setPrivileges(String privileges) {
		this.privileges = privileges;
	}

	public String getProfilePix() {
		return profilePix;
	}

	public void setProfilePix(String profilePix) {
		this.profilePix = profilePix;
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

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getPassportImage() {
		return passportImage;
	}

	public void setPassportImage(String passportImage) {
		this.passportImage = passportImage;
	}

	public Long getAcquirerId() {
		return this.acquirer_id;
	}

	public void setAcquirerId(Long acquirer_id) {
		this.acquirer_id = acquirer_id;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public Boolean getActivateMobilePin() {
		return activateMobilePin;
	}

	public void setActivateMobilePin(Boolean activateMobilePin) {
		this.activateMobilePin = activateMobilePin;
	}
	
	
	
	public String getSummary()
	{
		JSONObject js = new JSONObject();
		js.put("id", this.id); 
		js.put("failedLoginCount", this.failedLoginCount);
		js.put("lockOut", this.lockOut);
		//js.put("password", this.password);
		js.put("username", this.username);
		js.put("status", this.status);
		//js.put("salt", this.salt);
		js.put("roleType", this.roleType);
		js.put("privileges", this.privileges);
		js.put("created_at", this.created_at);
		js.put("updated_at", this.updated_at);
		js.put("deleted_at", this.deleted_at);
		js.put("firstName", this.firstName);
		js.put("lastName", this.lastName);
		js.put("otherName", this.otherName);
		js.put("mobileNo", this.mobileNo);
		js.put("userEmail", this.userEmail);
		js.put("passportImage", this.passportImage);
		js.put("profilePix", this.profilePix);
		//js.put("otp", this.otp);
		//js.put("pin", this.pin);
		js.put("isPinSet", this.pin==null ? false : true);
		js.put("activateMobilePin", this.activateMobilePin);
		
		return js.toString();
	}

	public Boolean getActivateOTPLogin() {
		return activateOTPLogin;
	}

	public void setActivateOTPLogin(Boolean activateOTPLogin) {
		this.activateOTPLogin = activateOTPLogin;
	}

	public Boolean getActivateOTPDebit() {
		return activateOTPDebit;
	}

	public void setActivateOTPDebit(Boolean activateOTPDebit) {
		this.activateOTPDebit = activateOTPDebit;
	}

	public OTPType getOtpType() {
		return otpType;
	}

	public void setOtpType(OTPType otpType) {
		this.otpType = otpType;
	}

	public Integer getSecurityQuestionId() {
		return securityQuestionId;
	}

	public void setSecurityQuestionId(Integer securityQuestionId) {
		this.securityQuestionId = securityQuestionId;
	}

	public String getSecurityQuestionAnswer() {
		return securityQuestionAnswer;
	}

	public void setSecurityQuestionAnswer(String securityQuestionAnswer) {
		this.securityQuestionAnswer = securityQuestionAnswer;
	}
    
}