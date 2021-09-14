package com.probase.probasepay.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
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

import org.json.JSONException;
import org.json.JSONObject;

import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.CardType;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.StopCardReason;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.util.SwpService;
import com.probase.probasepay.util.UtilityHelper;
  
@Entity
@Table(name="ecards")  
public class ECard implements Serializable {  
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	Long id;
	@Column(nullable = false)
	String nameOnCard;
	@Column(nullable = false)
	String pan;
	@Column(nullable = true)
	String pin;
	@Column(nullable = true)
	Date expiryDate;
	//@Column(nullable = false)
	Long customerId;
	@Column(nullable = true)
	String cvv;
	String otp;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Acquirer acquirer;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Issuer issuer;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	CardScheme cardScheme;
	//@Column(nullable = false)
	@OneToOne  
    @JoinColumn
	Account account;
	@Column(nullable = false)
	Long accountId;
	@Column(nullable = false)
	CardStatus status;
	@Column(nullable = false)
	CardType cardType;
	@Column(columnDefinition = "BIT", length = 1)
	Boolean stopFlag;
	@Column(nullable = true)
	StopCardReason stopReason;
	String stopReasonDescription;
	//@GeneratedValue(strategy=GenerationType.AUTO)
	String serialNo;

	@Column(nullable = false)
	Date created_at;
	@Column(nullable = false)
	Date updated_at;
	Date deleted_at;
	
	@Column(columnDefinition = "BIT", length = 1)
	Boolean corporateOwned;
	@OneToOne  
    @JoinColumn
	Customer corporateCustomer;
	Long corporateCustomerId;
	@OneToOne  
    @JoinColumn
	Account corporateCustomerAccount;
	Long corporateCustomerAccountId;
	String trackingNumber;
	@Column(columnDefinition = "BIT", length = 1)
	private Boolean changedCardPin;
	@Column(nullable = true)
	private Double cardBalance;
	@Column(nullable = true)
	private Double cardCharges;
	private Boolean overDebit;
	Integer isLive;
	
	
	public ECard()
	{
		
	}

	public ECard(String nameOnCard, String pan, String pin, Date expiryDate,
			Long customerId, String cvv, String otp, Acquirer acquirer,
			Issuer issuer, CardScheme cardScheme, Account account,
			Long accountId, CardStatus status, CardType cardType,
			Boolean changedCardPin, String serialNo, 
			Boolean corporateOwned, Customer corporateCustomer,
			Long corporateCustomerId, Account corporateCustomerAccount,
			Long corporateCustomerAccountId, String trackingNumber, Integer isLive) {
		super();
		this.nameOnCard = nameOnCard;
		this.pan = pan;
		this.pin = pin;
		this.expiryDate = expiryDate;
		this.customerId = customerId;
		this.cvv = cvv;
		this.otp = otp;
		this.issuer = issuer;
		this.acquirer = acquirer;
		this.cardScheme = cardScheme;
		this.account = account;
		this.accountId = accountId;
		this.status = status;
		this.cardType = cardType;
		this.changedCardPin = changedCardPin;
		this.serialNo = serialNo;
		this.created_at = new Date();
		this.updated_at = new Date();
		this.corporateOwned = corporateOwned;
		this.corporateCustomer = corporateCustomer;
		this.corporateCustomerId = corporateCustomerId;
		this.corporateCustomerAccount = corporateCustomerAccount;
		this.corporateCustomerAccountId = corporateCustomerAccountId;
		this.trackingNumber = trackingNumber;
		this.cardBalance = 0.00;
		this.overDebit = null;
		this.cardCharges = 0.00;
		this.isLive = isLive;
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
	public CardStatus getStatus() {
	    return status;
	}
	
	@Enumerated(EnumType.STRING)
	public CardType getCardType() {
	    return cardType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNameOnCard() {
		return nameOnCard;
	}

	public void setNameOnCard(String nameOnCard) {
		this.nameOnCard = nameOnCard;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public void setChangedCardPin(Boolean changedCardPin) {
		this.changedCardPin = changedCardPin;
	}

	public Boolean getChangedCardPin() {
		return changedCardPin;
	}


	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public Acquirer getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(Acquirer acquirer) {
		this.acquirer = acquirer;
	}

	public Issuer getIssuer() {
		return issuer;
	}

	public void setIssuer(Issuer issuer) {
		this.issuer = issuer;
	}

	public CardScheme getCardScheme() {
		return cardScheme;
	}

	public void setCardScheme(CardScheme cardScheme) {
		this.cardScheme = cardScheme;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}


	public void setStatus(CardStatus status) {
		this.status = status;
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

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Boolean getCorporateOwned() {
		return corporateOwned;
	}

	public void setCorporateOwned(Boolean corporateOwned) {
		this.corporateOwned = corporateOwned;
	}

	public Customer getCorporateCustomer() {
		return corporateCustomer;
	}

	public void setCorporateCustomer(Customer corporateCustomer) {
		this.corporateCustomer = corporateCustomer;
	}

	public Account getCorporateCustomerAccount() {
		return corporateCustomerAccount;
	}

	public void setCorporateCustomerAccount(Account corporateCustomerAccount) {
		this.corporateCustomerAccount = corporateCustomerAccount;
	}

	public Long getCorporateCustomerId() {
		return corporateCustomerId;
	}

	public void setCorporateCustomerId(Long corporateCustomerId) {
		this.corporateCustomerId = corporateCustomerId;
	}

	public Long getCorporateCustomerAccountId() {
		return corporateCustomerAccountId;
	}

	public void setCorporateCustomerAccountId(Long corporateCustomerAccountId) {
		this.corporateCustomerAccountId = corporateCustomerAccountId;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public Boolean getStopFlag() {
		return stopFlag;
	}

	public void setStopFlag(Boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	public StopCardReason getStopReason() {
		return stopReason;
	}

	public void setStopReason(StopCardReason stopReason) {
		this.stopReason = stopReason;
	}

	public String getStopReasonDescription() {
		return stopReasonDescription;
	}

	public void setStopReasonDescription(String stopReasonDescription) {
		this.stopReasonDescription = stopReasonDescription;
	}

	public Double getCardBalance() {
		return cardBalance;
	}

	public void setCardBalance(Double cardBalance) {
		this.cardBalance = cardBalance;
	}

	public Boolean getOverDebit() {
		return overDebit;
	}

	public void setOverDebit(Boolean overDebit) {
		this.overDebit = overDebit;
	}

	public JSONObject getCardDetails() {
		// TODO Auto-generated method stub
		JSONObject oneAccount = new JSONObject();
		try {
			oneAccount.put("id", this.getId());
			oneAccount.put("cardPan", UtilityHelper.formatPan(this.getPan()));
			oneAccount.put("serialNo", UtilityHelper.getValue(this.getSerialNo()));
			oneAccount.put("trackingNumber", UtilityHelper.getValue(this.getTrackingNumber()));
			oneAccount.put("accountIdentifier", (this.getAccount().getAccountIdentifier()));
			oneAccount.put("cardType", UtilityHelper.getValue(this.getCardType().name()));
			oneAccount.put("cardScheme", UtilityHelper.getValue(this.getCardScheme().getSchemeName()));
			oneAccount.put("status", UtilityHelper.getValue(this.getStatus().name()));
			return oneAccount;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	public synchronized ECard withdraw(SwpService swpService, Double amt, Double newCharges) throws Exception{
		synchronized (this.id) {
			this.cardBalance= this.cardBalance-amt;
			this.cardCharges = this.cardCharges + newCharges;
			swpService.updateRecord(this);
			return this;
		}
	}
	
	
	public synchronized ECard deposit(SwpService swpService, 
			Double amount, Double newCharges) throws Exception
	{
		synchronized (this.id) {
			this.cardBalance= this.cardBalance+amount;
			this.cardCharges = this.cardCharges + newCharges;
			
			swpService.updateRecord(this);
			
			return this;
		}
	}
	
	
	public synchronized ECard overDebit(SwpService swpService, Boolean isOverDebit) throws Exception{
		synchronized (this.id) {
			this.overDebit= isOverDebit;
			swpService.updateRecord(this);
			return this;
		}
	}

	public Double getCardCharges() {
		return cardCharges;
	}

	public void setCardCharges(Double cardCharges) {
		this.cardCharges = cardCharges;
	}

	
	public JSONObject getSummary() throws JSONException
	{
		JSONObject js1 = new JSONObject();
		js1.put("nameOnCard", this.nameOnCard);
		js1.put("cardScheme", this.cardScheme);
		js1.put("status", this.status);
		js1.put("cardType", this.cardType.name());
		js1.put("serialNo", this.serialNo);
		js1.put("cardBalance", this.cardBalance);
		js1.put("overDebit", this.overDebit);
		js1.put("cardCharges", this.cardCharges);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
		js1.put("expiryDate", sdf.format(this.expiryDate));
		js1.put("pin", this.pin);
		js1.put("cvv", this.cvv);
		
		return js1;
	}
	
	
	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

	public JSONObject getSummaryFrom(JSONObject js) throws JSONException
	{
		JSONObject js1 = new JSONObject();
		js1.put("nameOnCard", js.getString("nameOnCard"));
		js1.put("cardScheme", (CardScheme)js.get("cardScheme"));
		js1.put("status", js.getString("status"));
		js1.put("cardType", js.getString("cardType"));
		js1.put("serialNo", js.getString("serialNo"));
		js1.put("cardBalance", js.getString("cardBalance"));
		js1.put("overDebit", js.getString("overDebit"));
		js1.put("cardCharges", js.getString("cardCharges"));
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
		js1.put("expiryDate", sdf.format(js.getString("expiryDate")));
		js1.put("pin", js.getString("pin"));
		js1.put("cvv", js.getString("cvv"));
		
		return js1;
	}
    
}

