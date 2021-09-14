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
@Table(name="group_messages")  
public class GroupMessage implements Serializable{  
	private static final long serialVersionUID = -6700990760991590860L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
    String details;
	@OneToOne  
    @JoinColumn
    Customer sender;
	@OneToOne  
    @JoinColumn
    Customer receiver;
	@OneToOne  
    @JoinColumn
    Group group;
	@Column(nullable = false)
	Date createdAt;
	@Column(nullable = false)
	Date updatedAt;
	Date deletedAt;
	
	
	
	
	public GroupMessage(String details, Customer sender, Customer receiver, Group group) {
		super();
		this.details = details;
		this.sender = sender;
		this.receiver = receiver;
		this.group = group;
		this.createdAt = new Date();
		this.updatedAt = new Date();
	}


	public GroupMessage() {
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


	public String getDetails() {
		return details;
	}


	public void setDetails(String details) {
		this.details = details;
	}


	public Customer getSender() {
		return sender;
	}


	public void setSender(Customer sender) {
		this.sender = sender;
	}


	public Customer getReceiver() {
		return receiver;
	}


	public void setReceiver(Customer receiver) {
		this.receiver = receiver;
	}


	public Group getGroup() {
		return group;
	}


	public void setGroup(Group group) {
		this.group = group;
	}



	
}
