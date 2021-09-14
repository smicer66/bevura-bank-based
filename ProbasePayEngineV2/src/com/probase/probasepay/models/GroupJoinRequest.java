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
@Table(name="group_join_request")  
public class GroupJoinRequest implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
    Long requestByUserId;
	@Column(nullable = false)
	Long groupId;
	@Column(nullable = false)
	String requestByUserFullName;
	@Column(nullable = false)
	String requestByUserMobileNumber;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	@Column(nullable = true)
	Boolean isApproved;
	
	
	
	
	public GroupJoinRequest() {
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

	public Long getId() {
		return id;
	}


	public GroupJoinRequest(Long requestByUserId, Long groupId, String requestByUserFullName,
			String requestByUserMobileNumber, Boolean isApproved) {
		super();
		this.requestByUserId = requestByUserId;
		this.groupId = groupId;
		this.requestByUserFullName = requestByUserFullName;
		this.requestByUserMobileNumber = requestByUserMobileNumber;
		this.isApproved = isApproved;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}


	public Boolean getIsApproved() {
		return isApproved;
	}


	public void setIsApproved(Boolean isApproved) {
		this.isApproved = isApproved;
	}


	public Long getRequestByUserId() {
		return requestByUserId;
	}


	public void setRequestByUserId(Long requestByUserId) {
		this.requestByUserId = requestByUserId;
	}


	public Long getGroupId() {
		return groupId;
	}


	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}


	public String getRequestByUserFullName() {
		return requestByUserFullName;
	}


	public void setRequestByUserFullName(String requestByUserFullName) {
		this.requestByUserFullName = requestByUserFullName;
	}


	public String getRequestByUserMobileNumber() {
		return requestByUserMobileNumber;
	}


	public void setRequestByUserMobileNumber(String requestByUserMobileNumber) {
		this.requestByUserMobileNumber = requestByUserMobileNumber;
	}


	public void setId(Long id) {
		this.id = id;
	}


	
}
