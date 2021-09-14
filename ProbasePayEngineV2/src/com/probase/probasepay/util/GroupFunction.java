package com.probase.probasepay.util;



import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.probase.probasepay.enumerations.AccountStatus;
import com.probase.probasepay.enumerations.BillType;
import com.probase.probasepay.enumerations.CardStatus;
import com.probase.probasepay.enumerations.Channel;
import com.probase.probasepay.enumerations.GroupLoanStatus;
import com.probase.probasepay.enumerations.NotificationType;
import com.probase.probasepay.enumerations.PenaltyApplicableType;
import com.probase.probasepay.enumerations.PeriodType;
import com.probase.probasepay.enumerations.ProbasePayCurrency;
import com.probase.probasepay.enumerations.RepaymentStrategy;
import com.probase.probasepay.enumerations.ServiceType;
import com.probase.probasepay.enumerations.TenorType;
import com.probase.probasepay.enumerations.TransactionCode;
import com.probase.probasepay.enumerations.TransactionStatus;
import com.probase.probasepay.enumerations.UserStatus;
import com.probase.probasepay.enumerations.VillageBankingSetting;
import com.probase.probasepay.models.Account;
import com.probase.probasepay.models.Acquirer;
import com.probase.probasepay.models.ContributionPackage;
import com.probase.probasepay.models.ContributionPackageDebit;
import com.probase.probasepay.models.Customer;
import com.probase.probasepay.models.Device;
import com.probase.probasepay.models.ECard;
import com.probase.probasepay.models.Group;
import com.probase.probasepay.models.GroupAccount;
import com.probase.probasepay.models.GroupContribution;
import com.probase.probasepay.models.GroupJoinRequest;
import com.probase.probasepay.models.GroupLoan;
import com.probase.probasepay.models.GroupLoanRepayment;
import com.probase.probasepay.models.GroupLoanRepaymentsExpected;
import com.probase.probasepay.models.GroupLoanTerms;
import com.probase.probasepay.models.GroupMember;
import com.probase.probasepay.models.GroupMessage;
import com.probase.probasepay.models.GroupPaymentsExpected;
import com.probase.probasepay.models.Merchant;
import com.probase.probasepay.models.Notification;
import com.probase.probasepay.models.Transaction;
import com.probase.probasepay.models.User;
import com.probase.probasepay.models.VBCustomerAccount;
import com.probase.probasepay.services.AccountServicesV2;
import com.probase.probasepay.services.PaymentServicesV2;

public final class GroupFunction {
	private static GroupFunction groupFunction = null;

    
    private static Logger log = Logger.getLogger(GroupFunction.class);
	private ServiceLocator serviceLocator = null;
	public SwpService swpService = null;
	public PrbCustomService swpCustomService = PrbCustomService.getInstance();
	Application application = null;

	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private GroupFunction() {
    	serviceLocator = ServiceLocator.getInstance();
    }

    public static GroupFunction getInstance() {
        if ( groupFunction == null ) {
        	groupFunction = new GroupFunction();
        }

        return groupFunction;
    }
    
    
    public Response generateLoanRepaymentDetails(String token, Double principal, Integer term, Long loanTermId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && principal!=null && term!=null && loanTermId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			JSONObject verifyJ = UtilityHelper.verifyToken(token, app);
			if(verifyJ.length()==0 || (verifyJ.length()>0 && verifyJ.has("active") && verifyJ.getInt("active")==0))
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Token expired");
				return Response.status(200).entity(jsonObject.toString()).build();
				//return Response.status(200).entity(jsonObject).build();
			}
			else
			{
				//jsonObject.put("token", verifyJ.getString("token"));
			}
			System.out.println("verifyJ ==" + verifyJ.toString());
			String subject = verifyJ.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from GroupLoanTerms tp where tp.deletedAt IS NULL " +
					"AND tp.id = " + loanTermId;
    		GroupLoanTerms groupLoanTerm = (GroupLoanTerms)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupLoanTerm==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				jsonObject.put("message", "Hey, you are applying for a loan for a contribution package that has not setup loaning on its group. Please only apply for loans on contribution packages that have loans running");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		
    		Integer minLoanTenor = groupLoanTerm.getMinimumTerm();
    		Integer maxLoanTenor = groupLoanTerm.getMaximumTerm();
    		Double minLoanPrincipal = groupLoanTerm.getMinimumPrincipalLoanable();
    		Double maxLoanPrincipal = groupLoanTerm.getMaximumPrincipalLoanable();
    		
    		if((term<minLoanTenor) || (term>maxLoanTenor))
    		{
    			jsonObject.put("message", "Your preferred loan repayment period is not valid. Your loan repayment period must be at least " + 
    				minLoanTenor + "" + groupLoanTerm.getRepaymentTenorType().name().toLowerCase() + "(s) and not more than " + maxLoanTenor + "" + groupLoanTerm.getRepaymentTenorType().name().toLowerCase() + "(s)");
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if((principal<=minLoanPrincipal) || (principal>=maxLoanPrincipal))
    		{
    			jsonObject.put("message", "Your preferred loan amount must be at least K" + 
    				minLoanPrincipal + "" + " and not more than K" + maxLoanPrincipal + "");
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
			
			
			hql = "Select tp from ContributionPackage tp where tp.id = " + groupLoanTerm.getContributionPackage().getId() + " AND tp.deletedAt IS NULL";
			ContributionPackage contributionPackage = (ContributionPackage)this.swpService.getUniqueRecordByHQL(hql);
			
			if(contributionPackage==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "Your selected contribution package could not be found. Please select a valid contribution package");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = "+ customer.getId()  +" AND tp.group.id = " + contributionPackage.getGroup().getId() + 
					" AND tp.isActive = 1 AND tp.deletedAt IS NULL";
    		GroupMember groupMember= (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupMember==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you are not a member of the group to apply for loans");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from GroupLoan tp where tp.status IN (" + GroupLoanStatus.ACTIVE.ordinal() + ", " + 
    				GroupLoanStatus.DEFAULTED.ordinal() + ", " + GroupLoanStatus.LATE_30.ordinal() + ", " + 
    				GroupLoanStatus.LATE_60.ordinal() + ", " + GroupLoanStatus.LATE_90.ordinal() + ") AND tp.deletedAt IS NULL " +
    				"AND tp.contributionPackage.id = " + contributionPackage.getId();
    		Collection<GroupLoan> groupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
    		if(groupLoans.size()>0)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_HAS_LOANS);
				jsonObject.put("message", "Hey, you already have loans that are yet to be closed. Ensure you close out on those loans by paying off any outstandings in " +
						"order to qualify for a new load");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Double totalContributions = 0.00;
    		Double totalContributionDebits = 0.00;
    		hql = "Select tp from GroupContribution tp where tp.customer.id = "+ customer.getId()  +" AND tp.groupPaymentExpected.countributionPackage.id = " + contributionPackage.getId() + 
					" AND tp.deletedAt IS NULL";
    		log.info("hql..." + hql);
    		System.out.println("hql.." + hql);
    		
    		Collection<GroupContribution> allGroupMemberContributions= (Collection<GroupContribution>)this.swpService.getAllRecordsByHQL(hql);
    		if(allGroupMemberContributions!=null && allGroupMemberContributions.size()>0)
    		{
    			Iterator<GroupContribution> it = allGroupMemberContributions.iterator();
    			while(it.hasNext())
    			{
    				GroupContribution gc = it.next();
    				totalContributions = totalContributions + gc.getAmount();
    			}
    		}
    		
    		
    		hql = "Select tp from ContributionPackageDebit tp where tp.groupMember.addedCustomer.id = "+ customer.getId()  + 
    				" AND tp.contributionPackage.id = " + contributionPackage.getId() + 
					" AND tp.deletedAt IS NULL";
    		Collection<ContributionPackageDebit> allGroupMemberContributionDebits= (Collection<ContributionPackageDebit>)this.swpService.getAllRecordsByHQL(hql);
    		if(allGroupMemberContributionDebits!=null && allGroupMemberContributionDebits.size()>0)
    		{
    			Iterator<ContributionPackageDebit> it = allGroupMemberContributionDebits.iterator();
    			while(it.hasNext())
    			{
    				ContributionPackageDebit cpd = it.next();
    				totalContributionDebits = totalContributions + cpd.getAmount();
    			}
    		}
    		
    		Double currentBalance = totalContributions - totalContributionDebits;
    		
    		if(currentBalance < groupLoanTerm.getMinimumTotalContribution())
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_HAS_LOANS);
    			jsonObject.put("totalContribution", totalContributions);
				jsonObject.put("message", "Hey, you can not book for this loan as you do not qualify. To qualify, your contributions must exceed K" + groupLoanTerm.getMinimumTotalContribution());
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		String availableTenorType = "";
    		TenorType repaymentTenorType = groupLoanTerm.getRepaymentTenorType();
    		if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.MONTH))
    		{
    			availableTenorType = (TenorType.MONTH.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.WEEK))
    		{
    			availableTenorType = (TenorType.WEEK.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.YEAR))
    		{
    			availableTenorType = (TenorType.YEAR.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.DAY))
    		{
    			availableTenorType = (TenorType.DAY.name());
    		}
    		
    		
    		JSONArray schedule = null;
    		Double totalPrincipal = 0.00;
    		Double totalInterest = 0.00;
    		Double outstandingBalance = 0.00;
    		if(availableTenorType.equals(TenorType.MONTH.name()))
    		{
    			Double monthlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForMonthly(principal, monthlyInterestRate, term, new Date());
    		}
    		else if(availableTenorType.equals(TenorType.WEEK.name()))
    		{
    			Double weeklyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForWeekly(principal, weeklyInterestRate, term, new Date());
    		}
    		else if(availableTenorType.equals(TenorType.DAY.name()))
    		{
    			Double dailyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForDaily(principal, dailyInterestRate, term, new Date());
    		}
    		else if(availableTenorType.equals(TenorType.YEAR.name()))
    		{
    			Double yearlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForYearly(principal, yearlyInterestRate, term, new Date());
    		}
    		
    		if(schedule==null)
    		{
    			
    			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
    			jsonObject.put("message", "We could not generate a repayment schedule for your loan. Please try again");
    			
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}

    		
    		for(int i=0; i<schedule.length(); i++)
			{
				JSONObject scheduleEntry = schedule.getJSONObject(i);
				double interestPaid = scheduleEntry.getDouble("interestPaid");
				double principalPaid = scheduleEntry.getDouble("principalPaid");
				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
				totalInterest = totalInterest + interestPaid;
				totalPrincipal = totalPrincipal + principalPaid;
				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
			}
			//totalInterest = BigDecimal.valueOf(totalInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
			//totalPrincipal = BigDecimal.valueOf(totalPrincipal).setScale(2, RoundingMode.HALF_UP).doubleValue();
			//totalPrincipal = BigDecimal.valueOf(totalPrincipal).setScale(2, RoundingMode.HALF_UP).doubleValue();
    		

    		jsonObject.put("totalPrincipal", totalPrincipal);
    		jsonObject.put("totalInterest", totalInterest);
    		jsonObject.put("outstandingBalance", outstandingBalance);
    		jsonObject.put("schedule", schedule.toString());
    		jsonObject.put("currency", contributionPackage.getCurrency().name());
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, review the interest & repayment schedule for your preferred loan. If you are okay with the terms, repayment schedule and interest you can press the APPLY button");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }


    public Response getLoanDetails(String token, Long loanId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && loanId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from GroupLoan tp where tp.deletedAt IS NULL " +
					"AND tp.id = " + loanId;
    		GroupLoan groupLoan = (GroupLoan)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupLoan==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_LOAN_NOT_FOUND);
				jsonObject.put("message", "Hey, we could not find this loan on our system. Please try again");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		JSONArray schedule = new JSONArray();
    		hql = "Select tp from GroupLoanRepaymentsExpected tp where tp.groupLoan.id = " + groupLoan.getId() + " AND " +
    				"tp.groupLoan.groupMember.addedCustomer.id = " + customer.getId() + " AND " +
    				"tp.deletedAt IS NULL order by tp.installmentNumber";
    		Collection<GroupLoanRepaymentsExpected> groupLoanRepaymentsExpected = (Collection<GroupLoanRepaymentsExpected>) swpService.getAllRecordsByHQL(hql);
    		if(groupLoanRepaymentsExpected!=null && groupLoanRepaymentsExpected.size()>0)
    		{
    			int i1 = 0;
    			int indx = 0;
    			String[] colors = new String[6];
    			colors[0] = "#FF5733";
    			colors[1] = "#FCFF33";
    			colors[2] = "#33FF64";
    			colors[3] = "#3361FF";
    			colors[4] = "#FF33E6";
    			colors[5] = "#FF3333";
    			int i5 = 0;
    			int interval = 0;
				int canPay = 0;
				int noMore = 0;
    			Iterator<GroupLoanRepaymentsExpected> iterExpected = groupLoanRepaymentsExpected.iterator();
    			while(iterExpected.hasNext())
    			{
    				
    				indx = new Random().nextInt(6-0) + 0;
    				String iconBgColor = colors[indx];
    				String bgColor = i1%2==0 ? "#E2F5D6" : "";
    				GroupLoanRepaymentsExpected glre = iterExpected.next();
    				Double totalPrincipalIncurred = glre.getTotalPrincipalIncurred();
    				totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPrincipalIncurred);
    				log.info(totalPrincipalIncurred);
    				Double totalPenaltiesIncurred = glre.getTotalPenaltiesIncurred();
    				totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPenaltiesIncurred);
    				log.info(totalPenaltiesIncurred);
    				Double totalInterestIncurred = glre.getTotalInterestIncurred();
    				totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalInterestIncurred);
    				log.info(totalInterestIncurred);
    				Double totalPrincipalRepaid = glre.getTotalPrincipalRepaid();
    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPrincipalRepaid);
    				log.info(totalPrincipalRepaid);
    				Double totalPenaltiesRepaid = glre.getTotalPenaltiesRepaid();
    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPenaltiesRepaid);
    				log.info(totalPenaltiesRepaid);
    				Double totalInterestRepaid = glre.getTotalInterestRepaid();
    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalInterestRepaid);
    				log.info(totalInterestRepaid);
    				
    				Double amountIncurred = totalPrincipalIncurred + totalPenaltiesIncurred + totalInterestIncurred;
    				System.out.println(amountIncurred);
    				log.info(amountIncurred);
    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
    				System.out.println(amountRepaid);
    				log.info(amountRepaid);
    				Double amountToPay = amountIncurred - 
    						amountRepaid;
    				System.out.println("Amount to pay - " + amountToPay);
    				log.info("Amount to pay - " + amountToPay);
    				
    				
    				
					
					
    				
    				Boolean isLate = true;
    				if(glre.getExpectedRepaymentDate().after(new Date()))
    				{
    					isLate = false;
    				}
    				
    				Date currentDate = new Date();
    				Date repaymentDate = glre.getExpectedRepaymentDate();
    				TenorType tenorType = glre.getGroupLoan().getTenorType();
    				Calendar cal1 = Calendar.getInstance();
    				cal1.setTime(repaymentDate);
    				if(tenorType.equals(TenorType.DAY))
    				{
    					interval = interval + 1;
						cal1.add(Calendar.DATE, interval);
    				}
					if(tenorType.equals(TenorType.MONTH))
					{
						interval = interval + 1;
						cal1.add(Calendar.MONTH, interval);
					}
					if(tenorType.equals(TenorType.WEEK))
					{
						interval = interval + 1;
						cal1.add(Calendar.DATE, (interval*7));
					}
					if(tenorType.equals(TenorType.YEAR))
					{
						interval = interval + 1;
						cal1.add(Calendar.YEAR, interval);
					}
					
					if(canPay == 1)
					{
						canPay = 0;
						noMore = 1;
					}
					else
					{
	    				if(cal1.getTime().after(currentDate) && amountToPay>0 && canPay==0 && noMore==0)
	    				{
	    					canPay = 1;
	    				}
					}
    				
    				JSONObject jsObj = new JSONObject();
    				jsObj.put("iconBgColor", iconBgColor);
    				jsObj.put("iconTxt", glre.getInstallmentNumber());
    				jsObj.put("bgColor", bgColor);
    				jsObj.put("amount", amountToPay);
    				jsObj.put("expectedPaymentDate", new SimpleDateFormat("yyyy MMM dd").format(glre.getExpectedRepaymentDate()));
    				jsObj.put("principal", glre.getTotalPrincipalIncurred());
    				jsObj.put("interest", glre.getTotalInterestIncurred());
    				jsObj.put("penalty", glre.getTotalPenaltiesIncurred());
    				jsObj.put("loanId", glre.getGroupLoan().getId());
    				jsObj.put("repaymentExpectedId", glre.getId());
    				jsObj.put("isLate", isLate);
    				jsObj.put("isPaid", amountToPay==0 ? 1 : 0);
    				jsObj.put("canPay", canPay);
    				jsObj.put("installmentNumber", glre.getInstallmentNumber());
    				schedule.put(jsObj);
    				i1++;
    			}
    		}


    		jsonObject.put("loanNo", groupLoan.getLoanAccountNo());
    		jsonObject.put("groupName", groupLoan.getGroupLoanTerm().getContributionPackage().getGroup().getName());
    		jsonObject.put("principal", groupLoan.getPrincipal());
    		jsonObject.put("interest", groupLoan.getInterestRate() + "% per " + groupLoan.getGroupLoanTerm().getInterestType().name().toLowerCase());
    		jsonObject.put("period", groupLoan.getTenor() + " " + groupLoan.getGroupLoanTerm().getRepaymentTenorType().name().toLowerCase() + "(s)");
    		jsonObject.put("penalty", groupLoan.getGroupLoanTerm().getPenalty() + " (" + groupLoan.getGroupLoanTerm().getPenaltyApplicableType().name().toLowerCase().replace('_', ' ') + ")");
    		jsonObject.put("schedule", schedule.toString());
			jsonObject.put("status", ERROR.GENERAL_OK);
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    public Response getLoanRepayments(String token, Long loanId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && loanId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from GroupLoan tp where tp.deletedAt IS NULL " +
					"AND tp.id = " + loanId;
    		GroupLoan groupLoan = (GroupLoan)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupLoan==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_LOAN_NOT_FOUND);
				jsonObject.put("message", "Hey, we could not find this loan on our system. Please try again");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		JSONArray schedule = new JSONArray();
    		hql = "Select tp from GroupLoanRepayment tp where tp.groupLoanRepaymentExpected.groupLoan.id = " + groupLoan.getId() + " AND " +
    				"tp.groupLoanRepaymentExpected.groupLoan.groupMember.addedCustomer.id = " + customer.getId() + " AND " +
    				"tp.deletedAt IS NULL";
    		Collection<GroupLoanRepayment> groupLoanRepayments = (Collection<GroupLoanRepayment>) swpService.getAllRecordsByHQL(hql);
    		if(groupLoanRepayments!=null && groupLoanRepayments.size()>0)
    		{
    			int i1 = 0;
    			int indx = 0;
    			String[] colors = new String[6];
    			colors[0] = "#FF5733";
    			colors[1] = "#FCFF33";
    			colors[2] = "#33FF64";
    			colors[3] = "#3361FF";
    			colors[4] = "#FF33E6";
    			colors[5] = "#FF3333";
    			int i5 = 0;
    			int interval = 0;
				int canPay = 0;
				int noMore = 0;
    			Iterator<GroupLoanRepayment> iterExpected = groupLoanRepayments.iterator();
    			while(iterExpected.hasNext())
    			{
    				
    				indx = new Random().nextInt(6-0) + 0;
    				String iconBgColor = colors[indx];
    				String bgColor = i1%2==0 ? "#E2F5D6" : "";
    				GroupLoanRepayment glr = iterExpected.next();
    				Double totalPrincipalRepaid = glr.getPrincipalAmountRepaid();
    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPrincipalRepaid);
    				log.info(totalPrincipalRepaid);
    				Double totalPenaltiesRepaid = glr.getPenaltyAmountRepaid();
    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPenaltiesRepaid);
    				log.info(totalPenaltiesRepaid);
    				Double totalInterestRepaid = glr.getInterestAmountRepaid();
    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalInterestRepaid);
    				log.info(totalInterestRepaid);
    				
    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
    				System.out.println(amountRepaid);
    				log.info(amountRepaid);
    				
    				JSONObject jsObj = new JSONObject();
    				jsObj.put("iconBgColor", iconBgColor);
    				jsObj.put("iconTxt", glr.getGroupLoanRepaymentExpected().getInstallmentNumber());
    				jsObj.put("bgColor", bgColor);
    				jsObj.put("amount", amountRepaid);
    				jsObj.put("datePaid", new SimpleDateFormat("yyyy MMM dd").format(glr.getCreatedAt()));
    				jsObj.put("principal", glr.getPrincipalAmountRepaid());
    				jsObj.put("interest", glr.getInterestAmountRepaid());
    				jsObj.put("penalty", glr.getPenaltyAmountRepaid());
    				jsObj.put("loanId", glr.getGroupLoanRepaymentExpected().getGroupLoan().getId());
    				jsObj.put("loanAccountNumber", glr.getGroupLoanRepaymentExpected().getGroupLoan().getLoanAccountNo());
    				jsObj.put("installmentNumber", glr.getGroupLoanRepaymentExpected().getInstallmentNumber());
    				schedule.put(jsObj);
    				i1++;
    			}
    		}


    		jsonObject.put("repaymentLoanNo", groupLoan.getLoanAccountNo());
    		jsonObject.put("repaymentGroupName", groupLoan.getGroupLoanTerm().getContributionPackage().getGroup().getName());
    		jsonObject.put("repaymentLoanPrincipal", groupLoan.getPrincipal());
    		jsonObject.put("repaymentLoanInterest", groupLoan.getInterestRate() + "% per " + groupLoan.getGroupLoanTerm().getInterestType().name().toLowerCase());
    		jsonObject.put("repaymentLoanPeriod", groupLoan.getTenor() + " " + groupLoan.getGroupLoanTerm().getRepaymentTenorType().name().toLowerCase() + "(s)");
    		jsonObject.put("repaymentLoanPenalty", groupLoan.getGroupLoanTerm().getPenalty() + " (" + groupLoan.getGroupLoanTerm().getPenaltyApplicableType().name().toLowerCase().replace('_', ' ') + ")");
    		jsonObject.put("repayments", schedule.toString());
			jsonObject.put("status", ERROR.GENERAL_OK);
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }


    
    
    public Response createNewGroup(String backgroundColor, String fontColor, User user, String name, String shortName, String story, String iconurl, Integer isOpen, Integer maximumMembers, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(name!=null && isOpen!=null && maximumMembers!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			
			
			
			String hql = "Select tp from Group tp where lower(tp.name) = '"+name.toLowerCase()+"' AND tp.deletedAt IS NULL";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		if(group!=null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NAME_EXISTS);
				jsonObject.put("message", "Hey, there seems to already have a group with a name similar to the one you want to create. Provide a different name for the new group to avoid confusions");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from Customer tp where tp.user.id = " + user.getId() + " AND tp.deleted_at IS NULL";
    		Customer customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
    		
    		String groupCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    		group = new Group(backgroundColor, fontColor, user, name, shortName, story, iconurl, groupCode, Boolean.TRUE, isOpen==1 ? true : false, maximumMembers);
    		swpService.createNewRecord(group);
    		
    		String groupAccountNumber = RandomStringUtils.randomNumeric(12);
    		GroupAccount groupAccount = new GroupAccount(group, groupAccountNumber, 0.00);
    		swpService.createNewRecord(groupAccount);
    		
    		
    		GroupMember groupMember = new GroupMember(group, user, Boolean.TRUE, Boolean.TRUE, customer, 0.00, 0.00);
    		swpService.createNewRecord(groupMember);
    		
    		String details = "Welcome to " + group.getName();
    		GroupMessage groupMessage = new GroupMessage(details, customer, null, group);
    		swpService.createNewRecord(groupMessage);
    		
    		hql = "Select tp from GroupMember tp where tp.group.id = "+group.getId()+" AND tp.deletedAt IS NULL";
    		Collection<GroupMember> groupMembers = (Collection<GroupMember>)this.swpService.getAllRecordsByHQL(hql);
    		int remainingMemberCount = groupMembers==null ? 0 : groupMembers.size();
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("groupId", group.getId());
			jsonObject.put("groupName", group.getName());
			jsonObject.put("remainingMemberCount", remainingMemberCount);
			jsonObject.put("message", "Hey, your new group has been created successfully. You can now add members to your group");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }

    public Response createNewGroupMessage(String groupMessageStr, User postByUser, Long groupId, Long receiverUserId) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(groupMessageStr!=null && postByUser!=null && groupId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			
			
			
			String hql = "Select tp from Group tp where (tp.id) = "+groupId+" AND tp.deletedAt IS NULL";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NAME_EXISTS);
				jsonObject.put("message", "Hey, no group found matching the details provided. Please try again");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from Customer tp where tp.user.id = " + postByUser.getId() + " AND tp.deleted_at IS NULL";
    		Customer customerSender = (Customer)this.swpService.getUniqueRecordByHQL(hql);
    		
    		Customer customerReceiver = null;
    		if(receiverUserId!=null)
    		{

        		hql = "Select tp from Customer tp where tp.user.id = " + receiverUserId + " AND tp.deleted_at IS NULL";
        		customerReceiver = (Customer)this.swpService.getUniqueRecordByHQL(hql);
    		}
    		
    		String groupCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    		GroupMessage groupMessage = new GroupMessage(groupMessageStr, customerSender, customerReceiver, group);
    		swpService.createNewRecord(groupMessage);
    		
    		
    		
    		hql = "Select tp from GroupMember tp where tp.group.id = "+group.getId()+" AND tp.deletedAt IS NULL";
    		Collection<GroupMember> groupMembers = (Collection<GroupMember>)this.swpService.getAllRecordsByHQL(hql);
    		Iterator<GroupMember> gmiter = groupMembers.iterator();
    		String msgBody = groupMessageStr;
    		while(gmiter.hasNext())
    		{
    			GroupMember gm = gmiter.next();
    			if(gm.getAddedCustomer().getUser().getId()!=postByUser.getId())
    			{
    				Notification not = new Notification(gm.getAddedCustomer().getUser().getId(), false, "New Village Banking Group Message", msgBody, null, NotificationType.GROUP_NEW_MESSAGE.name());
    				this.swpService.createNewRecord(not);
    			}
    		}
    			
    		
    		JSONArray allGroupMessages = new JSONArray();
    		hql = "Select tp.* from group_messages tp where tp.deletedAt IS NULL AND tp.group_id = " + groupId + " AND tp.receiver_id IS NULL ORDER BY tp.createdAt ASC";
			List<Map<String, Object>> groupMessages = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
    		jsonObject.put("allGroupMessages", groupMessages);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Your message has been posted successfully.");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }

    
    
    public Response removeGroup(String token, String groupCode, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupCode!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		
			
			Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from Group tp where lower(tp.groupCode) = '"+groupCode.toLowerCase()+"' AND tp.deletedAt IS NULL AND tp.createdByUser.id = " + actorUser.getId();
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
				jsonObject.put("message", "Hey, we could not find the group you intend to remove. Please ensure you select an active group to remove the group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from GroupMember tp where tp.group.id = " + group.getId() + " AND tp.deletedAt IS NULL";
    		Collection<GroupMember> groupMembers = (Collection<GroupMember>)this.swpService.getAllRecordsByHQL(hql);
    		Iterator<GroupMember> it = groupMembers.iterator();
    		while(it.hasNext())
    		{
    			GroupMember groupMember = it.next();
    			groupMember.setDeletedAt(new Date());
    			swpService.updateRecord(groupMember);
    		}
    		group.setDeletedAt(new Date());
    		swpService.updateRecord(group);
    		
    		hql = "Select tp from GroupAccount tp where tp.group.id = " + group.getId() + " AND tp.deletedAt IS NULL";
    		GroupAccount groupAccount = (GroupAccount)this.swpService.getUniqueRecordByHQL(hql);
    		groupAccount.setDeletedAt(new Date());
    		swpService.updateRecord(groupAccount);
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your selected group has been removed successfully.  You wont have access to this group anymore");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    public Response createNewGroupMember(String token, String groupId, Customer customer, User user, Boolean isActive, Integer isAdmin, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupId!=null && customer!=null && isActive!=null && isAdmin!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			

    		String hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
				jsonObject.put("message", "We could not find any group matching your selected group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if(!(group.getIsOpen()!=null && group.getIsOpen().equals(Boolean.TRUE)))
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
				jsonObject.put("message", "You can not join this group at the moment. The group is not open to everyone.");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
			hql = "Select tp from GroupMember tp where lower(tp.group.id) = "+groupId+" AND tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customer.getId();
    		GroupMember groupMember = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupMember!=null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_EXISTS);
				jsonObject.put("message", "You seem to already have this member added to your group. You can only add members who have not been added previously");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from Customer tp where tp.id = " + customer.getId() + " AND tp.deleted IS NULL";
    		Customer customerToAdd = customer;
    		
    		String groupCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    		groupMember = new GroupMember(group, user, isActive, isAdmin!=null && isAdmin==1 ? true : false, customerToAdd, 0.00, 0.00);
    		swpService.createNewRecord(groupMember);
    		
    		
    		
    		hql = "Select tp from ContributionPackage tp where tp.group.id = " + group.getId() + " AND tp.deletedAt IS NULL";
    		ContributionPackage contributionPackage = (ContributionPackage)swpService.getUniqueRecordByHQL(hql);
			Double amount = contributionPackage.getContributionAmount();
			Integer period = contributionPackage.getContributionPeriod();
			PeriodType periodType = contributionPackage.getContributionPeriodType();
			ProbasePayCurrency ProbasePayCurrency = contributionPackage.getCurrency();
			Integer numberOfPayments = contributionPackage.getNumberOfPayments();
			String packageName = contributionPackage.getPackageName();
			Double penaltyApplicable = contributionPackage.getPenaltyApplicable();
			PenaltyApplicableType penaltyApplicableType = contributionPackage.getPenaltyApplicableType();
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			Integer interval = period;
			
			for(int i=1; i<(numberOfPayments+1); i++)
			{
				cal.setTime(today);
				System.out.println("interval.." + interval);
				System.out.println("current date.." + cal.getTime());
				if(periodType.equals(PeriodType.DAY.name()))
					cal.add(Calendar.DATE, interval);
				if(periodType.equals(PeriodType.MONTH.name()))
					cal.add(Calendar.MONTH, interval);
				if(periodType.equals(PeriodType.WEEK.name()))
					cal.add(Calendar.DATE, (interval*7));
				if(periodType.equals(PeriodType.YEAR.name()))
					cal.add(Calendar.YEAR, interval);

				Date dateExpected = cal.getTime();
				System.out.println("dateExpected.." + dateExpected);
				GroupPaymentsExpected groupPaymentExpected = new GroupPaymentsExpected(contributionPackage, groupMember, 
						amount, dateExpected, 0.00, amount, 0.00, 0.00, Boolean.FALSE, (i), contributionPackage.getGroup().getId());
				swpService.createNewRecord(groupPaymentExpected);
				interval = interval + period;
			}
			
			
			hql = "Select tp.*, gm.currentBalance, gm.currentContributions, gm.isActive as isMemberActive, gm.isAdmin from group_members gm, groups tp where gm.group_id = tp.id AND gm.deletedAt IS NULL AND gm.isActive = 1 AND gm.addedCustomer_id = " + customer.getId()
				+ " AND tp.deletedAt IS NULL AND tp.isActive = 1";
			hql = "Select tp.*, COUNT(d.id) AS currentMemberCount, d.currentBalance, d.currentContributions, d.isActive as isMemberActive, d.isAdmin from groups tp LEFT JOIN group_members AS d ON d.group_id = tp.id where tp.deletedAt IS NULL "
					+ "AND tp.isActive = 1 AND d.addedCustomer_id = " + customer.getId() + " GROUP BY d.group_id";
			List<Map<String, Object>> myGroups = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
    			
			jsonObject.put("groupId", group.getId());
			jsonObject.put("myGroups", myGroups);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, you have been added to the village banking group");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    public Response createNewGroupMembers(String token, String groupId, String customers, Boolean isActive, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		System.out.println("customers..." + customers);
    		log.info("customers..." + customers);
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupId!=null && customers!=null && isActive!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			JSONArray allCustomers = new JSONArray(customers);
			
			String hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
				jsonObject.put("message", "Hey, we could not find any group matching your selected group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = " + customer.getId() + " AND tp.isActive = 1 AND tp.isAdmin = 1 AND tp.group.id = " + groupId;
			GroupMember gpAdmin = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
			if(gpAdmin==null)
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_PRIVILEDGES);
				jsonObject.put("message", "Only group administrators can add members to the group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			for(int i5=0; i5<allCustomers.length(); i5++)
			{
				JSONObject customerDetails = allCustomers.getJSONObject(i5);
				Long customerId = customerDetails.getLong("customerId");
				Integer isAdminVal = customerDetails.getInt("isAdmin");
				Boolean isAdmin = isAdminVal!=null && isAdminVal.equals(1) ? true : false;

				hql = "Select tp from GroupMember tp where lower(tp.group.id) = "+groupId+" AND tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customerId;
	    		GroupMember groupMember = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
	    		if(groupMember!=null)
	    		{
	    			
	    		}
	    		else
	    		{
		    		hql = "Select tp from Customer tp where tp.id = " + customerId + " AND tp.deletedAt IS NULL";
		    		Customer customerToAdd = (Customer)this.swpService.getUniqueRecordByHQL(hql);
		    		
		    		groupMember = new GroupMember(group, actorUser, isActive, isAdmin, customerToAdd, 0.00, 0.00);
		    		swpService.createNewRecord(groupMember);
	    		}
			}
			
			
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your new group member(s) has been added to the group.");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    public Response verifyUserByMobileNumber(String mobileNumber, Long groupId) throws LoginException {

    	JSONObject jsonObject = new JSONObject();
		try{
			jsonObject.put("mobileNumber", mobileNumber);
			if(groupId==null || mobileNumber==null)
	    	{
				jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Provide your mobile number");
				
	            
	            return Response.status(200).entity(jsonObject.toString()).build();
	    	}

			this.swpService = this.serviceLocator.getSwpService();
			application = Application.getInstance(swpService);

			
			
			String hql = "Select tp from Customer tp where tp.user.mobileNumber = '"+ mobileNumber +"' " +
					"AND tp.deletedAt IS NULL";
			log.info("hql: " + hql);
			log.info("UserStatus.ACTIVE.ordinal() == " + UserStatus.ACTIVE.name());
			log.info("UserStatus.INACTIVE.ordinal() == " + UserStatus.INACTIVE.name());
			Customer customer = (Customer)this.swpService.getUniqueRecordByHQL(hql);
			
			if(customer!=null)
			{
				hql = "Select tp from GroupMember tp where tp.group.id = "+ groupId +" " +
						"AND tp.addedCustomer.id = " + customer.getId() + " " + 
						"AND tp.deletedAt IS NULL";
				GroupMember groupMember = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
				if(groupMember==null)
				{
					//Does Not Exist
					jsonObject.put("status", ERROR.GENERAL_OK);
					jsonObject.put("message", "Registered member matching mobile number found.");
					jsonObject.put("memberId", customer.getId());
					jsonObject.put("firstName", customer.getFirstName());
					jsonObject.put("lastName", customer.getLastName());
					jsonObject.put("mobileNumber", customer.getContactMobile());
					
					
		            return Response.status(200).entity(jsonObject.toString()).build();
				}
				else
				{
					jsonObject.put("status", ERROR.GROUP_MEMBER_EXISTS);
					jsonObject.put("message", "Hey, this group member already belongs to the group");
					
		            return Response.status(200).entity(jsonObject.toString()).build();
				}
			}
			else
			{
				jsonObject.put("status", ERROR.USER_NOT_FOUND);
				jsonObject.put("message", "Hey, the mobile number "+mobileNumber+" does not belong to anyone on the platform. Only registered members can be added to your group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			log.warn(e); log.error("exception...", e); log.error("exception...", e);
            
			return Response.status(200).entity(jsonObject.toString()).build();
		}
    }
    
    
    public Response joinGroup(String token, String groupId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			

    		String hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
				jsonObject.put("message", "Hey, we could not find any group matching your selected group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if(group.getIsOpen())
    		{
    			//Valid
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.GROUP_NOT_OPEN_TO_EVERYONE);
				jsonObject.put("message", "Hey, you can only be added to this group by an administrator of the group. Request the groups administrator(s) to add you");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
			hql = "Select tp from GroupMember tp where lower(tp.group.id) = "+groupId+" AND tp.deletedAt IS NULL AND tp.customer.id = " + customer.getId();
    		GroupMember groupMember = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupMember!=null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_EXISTS);
				jsonObject.put("message", "Hey, you seem to already be a member of this group. You can only join groups you are not a member of");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Customer customerToAdd = customer;
    		
    		String groupCode = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    		groupMember = new GroupMember(group, actorUser, Boolean.TRUE, Boolean.FALSE, customerToAdd, 0.00, 0.00);
    		swpService.createNewRecord(groupMember);
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, you have been added to the group.");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }

    public Response removeGroupMember(String token, Long groupId, Long removeCustomerId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupId!=null && removeCustomerId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		
			
			Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			String hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL AND tp.isActive = 1";
    		Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		if(group==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, we could not find the group you intend to remove a member from. Please ensure you select an active group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		hql = "Select tp from GroupMember tp where tp.customer.id = " + customer.getId() + " AND tp.isActive = 1 AND tp.isAdmin = 1";
			GroupMember gpAdmin = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
			if(gpAdmin==null)
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_PRIVILEDGES);
				jsonObject.put("message", "Only group administrators can remove others from the group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where lower(tp.group.id) = '"+ groupId +"' AND tp.deletedAt IS NULL AND tp.addedCustomer.id = " + removeCustomerId;
    		GroupMember groupMember = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupMember==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, we could not find the group member you intend to remove. Please ensure you select an active group member to remove from the group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		groupMember.setDeletedAt(new Date());
    		swpService.updateRecord(groupMember);
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your selected group member has been removed from the group successfully. The member wont have access to this group anymore");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    public Response createNewContributionPackage(String token, String packageName,
			Double contributionAmount, Integer contributionPeriod,
			String contributionPeriodType, Double minimumBalanceRequired, String story, Integer numberOfPayments,
			Double penaltyApplicable, String penaltyApplicableType, Long groupId, 
			Double minLoanAmount, Double maxLoanAmount, Integer minimumTerm, Integer maximumTerm, String repaymentPeriodType, 
			Double interestRate, String interestType, Double loanPenalty, String loanPenaltyType, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && packageName!=null && contributionAmount!=null && contributionPeriod!=null && contributionPeriodType!=null 
    				&& penaltyApplicable!=null && penaltyApplicableType!=null && groupId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from GroupMember tp where tp.addedCustomer.id = "+ customer.getId()  +" AND tp.group.id = " + groupId + 
					" AND tp.isActive = 1 AND tp.isAdmin = 1 AND tp.deletedAt IS NULL";
    		GroupMember groupAdmin = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupAdmin==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you need to be an administrator of the group to create new contribution packages");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Group group = groupAdmin.getGroup();
    		ProbasePayCurrency currency = null;
    		PenaltyApplicableType pat = null;
    		PeriodType pt = null;
    		PeriodType interestPer = null;
    		TenorType repaymentTenorType = null;
    		PenaltyApplicableType loanPenaltyType_ = null;
    		currency = ProbasePayCurrency.ZMW;
    		
    		try
    		{
    			pat = PenaltyApplicableType.valueOf(penaltyApplicableType);
    			loanPenaltyType_ = PenaltyApplicableType.valueOf(loanPenaltyType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			
    		}
    		
    		try
    		{
    			pt = PeriodType.valueOf(contributionPeriodType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Hey, you need to specify if you want contributions to be every week, month or in days");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
	
			try
    		{
    			interestPer = PeriodType.valueOf(interestType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Hey, you need to specify the loan interest type. Specify if its in weeks, months or in days");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		try
    		{
    			repaymentTenorType = TenorType.valueOf(repaymentPeriodType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Hey, you need to specify the loan repayment period. Specify if its in weeks, months or in days");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		
    		
    		
    		ContributionPackage contributionPackage = new ContributionPackage(actorUser, packageName, contributionAmount, contributionPeriod, 
    				pt, penaltyApplicable, pat, group, currency, story, numberOfPayments);
    		contributionPackage = (ContributionPackage)swpService.createNewRecord(contributionPackage);
    		
    		if(contributionPackage!=null && minimumBalanceRequired!=null)
			{
    			GroupLoanTerms groupLoanTerm = new GroupLoanTerms(minimumBalanceRequired, contributionPackage, maxLoanAmount, minLoanAmount, minimumTerm, 
    					maximumTerm, interestPer, interestRate, repaymentTenorType, RepaymentStrategy.PRINCIPAL_INTEREST_PENALTY, loanPenalty, loanPenaltyType_);
    			groupLoanTerm = (GroupLoanTerms)swpService.createNewRecord(groupLoanTerm);
    		}
    			
    		jsonObject.put("groupId", contributionPackage.getGroup().getId());
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your new group contibution package has been created successfully. All members will be notified of their " +
					"need to contribute at the specified periodical interval");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		log.error(e);
    		e.printStackTrace();
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    public Response startContributionPackage(String token, Long packageId, Integer startOrEnd, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && packageId!=null && startOrEnd!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			String hql = "Select tp from ContributionPackage tp where tp.id = " + packageId + " AND tp.deletedAt IS NULL";
			ContributionPackage contributionPackage = (ContributionPackage)this.swpService.getUniqueRecordByHQL(hql);
			if(contributionPackage==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "Your selected contribution package could not be found. Please select a valid contribution package");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = "+ customer.getId()  +" AND tp.group.id = " + contributionPackage.getGroup().getId() + 
					" AND tp.isActive = 1 AND tp.isAdmin = 1 AND tp.deletedAt IS NULL";
			log.info(hql);
			System.out.println("hql...." + hql);
    		GroupMember groupAdmin = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupAdmin==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you need to be an administrator of the group to start this contribution packages");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if(startOrEnd==1)
    			contributionPackage.setStartDate(new Date());
    		else if(startOrEnd==0)
    			contributionPackage.setEndDate(new Date());
    		swpService.updateRecord(contributionPackage);
    		
    		hql = "Select tp from GroupMember tp where tp.group.id = " + contributionPackage.getGroup().getId() + " AND tp.deletedAt IS NULL";
			Collection<GroupMember> groupMembers = (Collection<GroupMember>)swpService.getAllRecordsByHQL(hql);
			Double amount = contributionPackage.getContributionAmount();
			Integer period = contributionPackage.getContributionPeriod();
			PeriodType periodType = contributionPackage.getContributionPeriodType();
			ProbasePayCurrency ProbasePayCurrency = contributionPackage.getCurrency();
			Group group = contributionPackage.getGroup();
			Integer numberOfPayments = contributionPackage.getNumberOfPayments();
			String packageName = contributionPackage.getPackageName();
			Double penaltyApplicable = contributionPackage.getPenaltyApplicable();
			PenaltyApplicableType penaltyApplicableType = contributionPackage.getPenaltyApplicableType();
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			Integer interval = period;
			
			Iterator<GroupMember> iterGroupMember = groupMembers.iterator();
			while(iterGroupMember.hasNext())
			{
				GroupMember groupMember = iterGroupMember.next();
				for(int i=0; i<numberOfPayments; i++)
				{
					cal.setTime(today);
					System.out.println("interval.." + interval);
					System.out.println("current date.." + cal.getTime());
					if(periodType.equals(PeriodType.DAY))
						cal.add(Calendar.DATE, interval);
					if(periodType.equals(PeriodType.MONTH))
						cal.add(Calendar.MONTH, interval);
					if(periodType.equals(PeriodType.WEEK))
						cal.add(Calendar.DATE, (interval*7));
					if(periodType.equals(PeriodType.YEAR))
						cal.add(Calendar.YEAR, interval);

					Date dateExpected = cal.getTime();
					System.out.println("dateExpected.." + dateExpected);
					GroupPaymentsExpected groupPaymentExpected = new GroupPaymentsExpected(contributionPackage, groupMember, 
							amount, dateExpected, 0.00, amount, 0.00, 0.00, Boolean.FALSE, (i), contributionPackage.getGroup().getId());
					swpService.createNewRecord(groupPaymentExpected);
					interval = interval + period;
				}
			}
    			
			JSONObject resp = getListOfGroups(customer);
			resp.put("status", ERROR.GENERAL_OK);
			if(startOrEnd==1)
				resp.put("message", "Hey, your new group contibution package has been started. All members will be notified of their " +
						"need to contribute at the specified periodical interval");
			else
				resp.put("message", "Hey, your new group contibution package has ended. All members will be notified of their " +
						"contributions and the closure of this contribution package");
			resp.put("isActive", contributionPackage.getStartDate()!=null && contributionPackage.getEndDate()==null ? true : false);
			
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    /*public Response payContributionOld(String token, Long expectedGroupPaymentId, Double amount, String requestId, String ipAddress, 
    		Account account, List<ECard> cardList, Device device) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{

    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && expectedGroupPaymentId!=null && amount!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			String subject = verify.getString("subject");
			String acquirerCode = verify.getString("acquirerCode");
			String bankKey = UtilityHelper.getBankKey(acquirerCode, swpService);
			User user = new Gson().fromJson(subject, User.class);
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from Acquirer tp where lower(tp.acquirerCode) = '" + acquirerCode + "'";
			System.out.println("hql ==" + hql);
			Acquirer acquirer = (Acquirer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from CustomerAccount tp where tp.customer.id = " + customer.getId() + " AND tp.deletedAt IS NULL";
			VBCustomerAccount customerAccount = (VBCustomerAccount)swpService.getUniqueRecordByHQL(hql);
			Double minAcceptableBalance = account.getAccountScheme().getMinimumBalance();
			//String minAcceptableBalance = app.getAllSettings().getString(Setting.MINIMUM_WALLET_BALANCE.name());
			//Double minAcceptableBalance  = Double.valueOf(minAcceptableBalance);
			
			if(customerAccount!=null && amount>(customerAccount.getAvailableBalance() - minAcceptableBalance))
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS_IN_WALLET);
				jsonObject.put("message", "Insufficient funds in your wallet to make this payment. You can pay around " + customerAccount.getCurrency().name() + "" + (customerAccount.getAvailableBalance() - minAcceptableBalance));
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from GroupPaymentsExpected tp where tp.id = " + expectedGroupPaymentId + " AND tp.deletedAt IS NULL " +
					"AND tp.groupMember.addedCustomer.id = " + customer.getId();
			GroupPaymentsExpected groupPaymentsExpected = (GroupPaymentsExpected)this.swpService.getUniqueRecordByHQL(hql);
			if(groupPaymentsExpected==null)
			{
				jsonObject.put("status", ERROR.EXPECTED_GROUP_PAYMENT_NOT_FOUND);
				jsonObject.put("message", "Your selected group contribution payment could not be found. Please select a valid contribution payment to pay");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			if(groupPaymentsExpected.getIsPaid()!=null && groupPaymentsExpected.getIsPaid().equals(Boolean.TRUE) && groupPaymentsExpected.getOutstandingBalance()==0.00)
			{
				jsonObject.put("status", ERROR.GROUP_PAYMENT_COMPLETED);
				jsonObject.put("message", "Your selected group contribution payment for the date has been paid off. Please select any other group contribution payment to pay");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from GroupAccount tp where tp.group.id = " + groupPaymentsExpected.getGroupMember().getGroup().getId() + " AND tp.deletedAt IS NULL";
			GroupAccount groupAccount = (GroupAccount)swpService.getUniqueRecordByHQL(hql);
			
			Double totalAmountPaidNow = 0.00;
			if(amount>groupPaymentsExpected.getOutstandingBalance())
			{
				hql = "Select tp from GroupPaymentsExpected tp where tp.isPaid = 0 AND tp.deletedAt IS NULL and tp.groupMember.id = " + groupPaymentsExpected.getGroupMember().getId() + 
						" ORDER by tp.dateExpected ASC";
				Collection<GroupPaymentsExpected> allGroupPaymentsExpected = (Collection<GroupPaymentsExpected>)swpService.getAllRecordsByHQL(hql);
				Iterator<GroupPaymentsExpected> it = allGroupPaymentsExpected.iterator();
				Double amountToPay = amount;
				Double tempAmount = amount;
				while(it.hasNext())
				{
					Double amountPaidForContribution = 0.00;
					GroupPaymentsExpected gpe= it.next();
					if(amountToPay>0)
					{
						Double outstandingBalance = gpe.getOutstandingBalance();
						Double contributionAmountOutstanding = gpe.getAmount() - gpe.getAmountPaid(); 
						
						if((amountToPay - contributionAmountOutstanding)>=0 && amountToPay>0)
						{
							//Pay Primary Amount
							gpe.setAmountPaid(gpe.getAmountPaid() + contributionAmountOutstanding);
							gpe.setOutstandingBalance(gpe.getOutstandingBalance() - contributionAmountOutstanding);
							totalAmountPaidNow = totalAmountPaidNow + contributionAmountOutstanding;
							amountPaidForContribution = amountPaidForContribution + contributionAmountOutstanding;
							amountToPay = amountToPay - contributionAmountOutstanding;
							GroupContribution gc = new GroupContribution(amountPaidForContribution, customerAccount, gpe, Boolean.FALSE, groupPaymentsExpected.getGroupMember().getGroup().getId());
							swpService.createNewRecord(gc);
							
							
							if(cardList!=null && cardList.size()>0)
							{
								for(int i1=0; i1<cardList.size(); i1++)
								{
									ECard card = cardList.get(i1);
									Double minimumCardBalance = card.getCardScheme().getMinimumBalance();
									Double cardBalance = card.getCardBalance();
									Double fixedCharge = 0.00;
									Double transactionCharge = 0.00;
									Double transactionPercentage = 0.00;
									Double schemeTransactionCharge = 0.00;
									Double schemeTransactionPercentage = 0.00;
									Double totalCharge = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
									
									if(tempAmount>=(cardBalance-minimumCardBalance))
									{
										if(tempAmount>0)
										{
											tempAmount = tempAmount - (cardBalance-minimumCardBalance);
											card = card.withdraw(swpService, (cardBalance-minimumCardBalance), totalCharge);
										}
									}
									else
									{
										card = card.withdraw(swpService, cardBalance-minimumCardBalance, totalCharge);
									}
									String transactionRef =  RandomStringUtils.randomAlphanumeric(16);
									String orderRef = customerAccount.getCustomer().getVerificationNumber() + "" + RandomStringUtils.randomAlphanumeric(8);
									Channel channel = Channel.MOBILE;
									Date transactionDate = new Date();
									ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
									User transactingUser = user;
									String messageRequest = null;
									String messageResponse = null;
									String narration = "GC|CA:" + customerAccount.getAccountNumber() + "|CD:" + gpe.getCountributionPackage().getGroup().getGroupCode() + "|AMT:" + amountPaidForContribution;
									
									
									Boolean creditAccountTrue = null;
									Boolean creditCardTrue = null;
									String rpin = null;
									String payerName = customer.getFirstName() + " " + customer.getLastName();
									String payerEmail = customer.getContactEmail();
									String payerMobile = customer.getContactMobile();
									TransactionStatus status = TransactionStatus.SUCCESS;
									ProbasePayCurrency probasePayCurrency = gpe.getCountributionPackage().getCurrency();
									String transactionCode = TransactionCode.transactionSuccess;
									Boolean creditPoolAccountTrue = null;
									String responseCode = null;
									String oTP = null;
									String oTPRef = null;
									Long merchantId = device.getMerchant().getId();
									String merchantName = device.getMerchant().getMerchantName();
									String merchantCode = device.getMerchant().getMerchantCode();
									String merchantBank = device.getMerchant().getMerchantBank().getBankName();
									String merchantAccount = null; 
									Long transactingBankId = device.getMerchant().getMerchantBank().getId();
									Long receipientTransactingBankId = null;
									Integer accessCode = null;
									Long sourceEntityId = card.getId();
									Long receipientEntityId = null;
									Channel receipientChannel = channel;
									String transactionDetail = "Pay Merchant - " + device.getMerchant().getMerchantName();
									Double closingBalance = 0.00;
									Double totalCreditSum = 0.00;
									Double totalDebitSum = 0.00;
									Long paidInByBankUserAccountId = null;
									String customData = null;
									String responseData = null;
									Long adjustedTransactionId = null;
									Long acquirerId = acquirer.getId();
									Boolean debitAccountTrue = null;
									Boolean debitCardTrue = true;
									Long creditAccountId = null;
									Long creditCardId = card.getId();
									Long debitAccountId = null;
									Long debitCardId = card.getId();
									String details = transactionDetail;
									
									
									Transaction transaction = new Transaction(transactionRef, null,
											customer.getId(), creditAccountTrue, creditCardTrue,
											orderRef, rpin, channel,
											transactionDate, ServiceType.GROUP_CONTRIBUTION, payerName,
											payerEmail, payerMobile, status,
											probasePayCurrency, transactionCode,
											null, card, device,
											creditPoolAccountTrue, messageRequest,
											 messageResponse, fixedCharge,
											 transactionCharge, transactionPercentage,
											 schemeTransactionCharge, schemeTransactionPercentage,
											 amount,  responseCode, oTP, oTPRef,
											 merchantId,  merchantName, merchantCode,
											 merchantBank, merchantAccount, 
											 transactingBankId, receipientTransactingBankId,
											 accessCode,  sourceEntityId, receipientEntityId,
											 receipientChannel,  transactionDetail,
											 closingBalance,  totalCreditSum,  totalDebitSum,
											 paidInByBankUserAccountId,  customData,
											 responseData,  adjustedTransactionId,  acquirerId,  debitAccountTrue,  debitCardTrue, 
											 creditAccountId,
											 creditCardId,
											 debitAccountId,
											 debitCardId,  details);
									swpService.createNewRecord(transaction);
								}
							}
							else
							{
								String transactionRef =  RandomStringUtils.randomAlphanumeric(16);
								String orderRef = customerAccount.getCustomer().getVerificationNumber() + "" + RandomStringUtils.randomAlphanumeric(8);
								Channel channel = Channel.MOBILE;
								Date transactionDate = new Date();
								ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
								User transactingUser = user;
								String messageRequest = null;
								String messageResponse = null;
								String narration = "GC|CA:" + customerAccount.getAccountNumber() + "|CD:" + gpe.getCountributionPackage().getGroup().getGroupCode() + "|AMT:" + amountPaidForContribution;
								
								
								Boolean creditAccountTrue = null;
								Boolean creditCardTrue = null;
								String rpin = null;
								String payerName = customer.getFirstName() + " " + customer.getLastName();
								String payerEmail = customer.getContactEmail();
								String payerMobile = customer.getContactMobile();
								TransactionStatus status = TransactionStatus.SUCCESS;
								ProbasePayCurrency probasePayCurrency = gpe.getCountributionPackage().getCurrency();
								String transactionCode = TransactionCode.transactionSuccess;
								Boolean creditPoolAccountTrue = null;
								Double fixedCharge = 0.00;
								Double transactionCharge = 0.00;
								Double transactionPercentage = 0.00;
								Double schemeTransactionCharge = 0.00;
								Double schemeTransactionPercentage = 0.00;
								String responseCode = null;
								String oTP = null;
								String oTPRef = null;
								Long merchantId = device.getMerchant().getId();
								String merchantName = device.getMerchant().getMerchantName();
								String merchantCode = device.getMerchant().getMerchantCode();
								String merchantBank = device.getMerchant().getMerchantBank().getBankName();
								String merchantAccount = null; 
								Long transactingBankId = device.getMerchant().getMerchantBank().getId();
								Long receipientTransactingBankId = null;
								Integer accessCode = null;
								Long sourceEntityId = account.getId();
								Long receipientEntityId = null;
								Channel receipientChannel = channel;
								String transactionDetail = "Pay Merchant - " + device.getMerchant().getMerchantName();
								Double closingBalance = 0.00;
								Double totalCreditSum = 0.00;
								Double totalDebitSum = 0.00;
								Long paidInByBankUserAccountId = null;
								String customData = null;
								String responseData = null;
								Long adjustedTransactionId = null;
								Long acquirerId = acquirer.getId();
								Boolean debitAccountTrue = true;
								Boolean debitCardTrue = null;
								Long creditAccountId = null;
								Long creditCardId = null;
								Long debitAccountId = account.getId();
								Long debitCardId = null;
								String details = transactionDetail;
								
								
								Transaction transaction = new Transaction(transactionRef, null,
										customer.getId(), creditAccountTrue, creditCardTrue,
										orderRef, rpin, channel,
										transactionDate, ServiceType.GROUP_CONTRIBUTION, payerName,
										payerEmail, payerMobile, status,
										probasePayCurrency, transactionCode,
										account, null, device,
										creditPoolAccountTrue, messageRequest,
										 messageResponse, fixedCharge,
										 transactionCharge, transactionPercentage,
										 schemeTransactionCharge, schemeTransactionPercentage,
										 amount,  responseCode, oTP, oTPRef,
										 merchantId,  merchantName, merchantCode,
										 merchantBank, merchantAccount, 
										 transactingBankId, receipientTransactingBankId,
										 accessCode,  sourceEntityId, receipientEntityId,
										 receipientChannel,  transactionDetail,
										 closingBalance,  totalCreditSum,  totalDebitSum,
										 paidInByBankUserAccountId,  customData,
										 responseData,  adjustedTransactionId,  acquirerId,  debitAccountTrue,  debitCardTrue, 
										 creditAccountId,
										 creditCardId,
										 debitAccountId,
										 debitCardId,  details);
								swpService.createNewRecord(transaction);
							}
						}
						else
						{
							if((amountToPay)>0)
							{
								if(cardList!=null && cardList.size()>0)
								{
									for(int i1=0; i1<cardList.size(); i1++)
									{
										ECard card = cardList.get(i1);
										Double minimumCardBalance = card.getCardScheme().getMinimumBalance();
										Double cardBalance = card.getCardBalance();
										Double fixedCharge = 0.00;
										Double transactionCharge = 0.00;
										Double transactionPercentage = 0.00;
										Double schemeTransactionCharge = 0.00;
										Double schemeTransactionPercentage = 0.00;
										Double totalCharge = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
										
										if(tempAmount>=(cardBalance-minimumCardBalance))
										{
											if(tempAmount>0)
											{
												tempAmount = tempAmount - (cardBalance-minimumCardBalance);
												card = card.withdraw(swpService, (cardBalance-minimumCardBalance), totalCharge);
											}
										}
										else
										{
											card = card.withdraw(swpService, cardBalance-minimumCardBalance, totalCharge);
										}
								
										
										//Pay Primary Amount
										gpe.setAmountPaid(gpe.getAmountPaid() + amountToPay);
										gpe.setOutstandingBalance(gpe.getOutstandingBalance() - amountToPay);
										totalAmountPaidNow = totalAmountPaidNow + amountToPay;
										amountPaidForContribution = amountPaidForContribution + amountToPay;
										amountToPay = amountToPay - amountToPay;
										GroupContribution gc = new GroupContribution(amountPaidForContribution, customerAccount, gpe, Boolean.FALSE, gpe.getGroupId());
										swpService.createNewRecord(gc);
										
										String transactionRef =  RandomStringUtils.randomAlphanumeric(16);
										String orderRef = customerAccount.getCustomer().getVerificationNumber() + "" + RandomStringUtils.randomAlphanumeric(8);
										Channel channel = Channel.MOBILE;
										Date transactionDate = new Date();
										ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
										User transactingUser = user;
										String messageRequest = null;
										String messageResponse = null;
										String narration = "GC|CA:" + customerAccount.getAccountNumber() + "|CD:" + gpe.getCountributionPackage().getGroup().getGroupCode() + "|AMT:" + amountPaidForContribution;
										
										
										Boolean creditAccountTrue = null;
										Boolean creditCardTrue = null;
										String rpin = null;
										String payerName = customer.getFirstName() + " " + customer.getLastName();
										String payerEmail = customer.getContactEmail();
										String payerMobile = customer.getContactMobile();
										TransactionStatus status = TransactionStatus.SUCCESS;
										ProbasePayCurrency probasePayCurrency = gpe.getCountributionPackage().getCurrency();
										String transactionCode = TransactionCode.transactionSuccess;
										Boolean creditPoolAccountTrue = null;
										String responseCode = null;
										String oTP = null;
										String oTPRef = null;
										Long merchantId = device.getMerchant().getId();
										String merchantName = device.getMerchant().getMerchantName();
										String merchantCode = device.getMerchant().getMerchantCode();
										String merchantBank = device.getMerchant().getMerchantBank().getBankName();
										String merchantAccount = null; 
										Long transactingBankId = device.getMerchant().getMerchantBank().getId();
										Long receipientTransactingBankId = null;
										Integer accessCode = null;
										Long sourceEntityId = card.getId();
										Long receipientEntityId = null;
										Channel receipientChannel = channel;
										String transactionDetail = "Pay Merchant - " + device.getMerchant().getMerchantName();
										Double closingBalance = 0.00;
										Double totalCreditSum = 0.00;
										Double totalDebitSum = 0.00;
										Long paidInByBankUserAccountId = null;
										String customData = null;
										String responseData = null;
										Long adjustedTransactionId = null;
										Long acquirerId = acquirer.getId();
										Boolean debitAccountTrue = false;
										Boolean debitCardTrue = true;
										Long creditAccountId = null;
										Long creditCardId = null;
										Long debitAccountId = null;
										Long debitCardId = card.getId();
										String details = transactionDetail;
		
										
										
										Transaction transaction = new Transaction(transactionRef, null,
												customer.getId(), creditAccountTrue, creditCardTrue,
												orderRef, rpin, channel,
												transactionDate, ServiceType.GROUP_CONTRIBUTION, payerName,
												payerEmail, payerMobile, status,
												probasePayCurrency, transactionCode,
												null, card, device,
												creditPoolAccountTrue, messageRequest,
												 messageResponse, fixedCharge,
												 transactionCharge, transactionPercentage,
												 schemeTransactionCharge, schemeTransactionPercentage,
												 amount,  responseCode, oTP, oTPRef,
												 merchantId,  merchantName, merchantCode,
												 merchantBank, merchantAccount, 
												 transactingBankId, receipientTransactingBankId,
												 accessCode,  sourceEntityId, receipientEntityId,
												 receipientChannel,  transactionDetail,
												 closingBalance,  totalCreditSum,  totalDebitSum,
												 paidInByBankUserAccountId,  customData,
												 responseData,  adjustedTransactionId,  acquirerId,  debitAccountTrue,  debitCardTrue, 
												 creditAccountId,
												 creditCardId,
												 debitAccountId,
												 debitCardId,  details);
										swpService.createNewRecord(transaction);
									}
								}
								else
								{
									Double minimumCardBalance = account.getAccountScheme().getMinimumBalance();
									Double acctBalance = 0.00;
									Double fixedCharge = 0.00;
									Double transactionCharge = 0.00;
									Double transactionPercentage = 0.00;
									Double schemeTransactionCharge = 0.00;
									Double schemeTransactionPercentage = 0.00;
									Double totalCharge = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
									
									if(tempAmount>=(acctBalance-minimumCardBalance))
									{
										if(tempAmount>0)
										{
											tempAmount = tempAmount - (acctBalance-minimumCardBalance);
											//card = card.withdraw(swpService, (acctBalance-minimumCardBalance), totalCharge);
										}
									}
									else
									{
										jsonObject = new JSONObject();
							        	jsonObject.put( "status", ERROR.INSUFFICIENT_FUNDS );
							        	jsonObject.put( "message", "Insufficient funds to pay this contribution" );
							            

							            return Response.status(200).entity(jsonObject.toString()).build();
									}
							
									
									//Pay Primary Amount
									gpe.setAmountPaid(gpe.getAmountPaid() + amountToPay);
									gpe.setOutstandingBalance(gpe.getOutstandingBalance() - amountToPay);
									totalAmountPaidNow = totalAmountPaidNow + amountToPay;
									amountPaidForContribution = amountPaidForContribution + amountToPay;
									amountToPay = amountToPay - amountToPay;
									GroupContribution gc = new GroupContribution(amountPaidForContribution, customerAccount, gpe, Boolean.FALSE, gpe.getGroupId());
									swpService.createNewRecord(gc);
									
									String transactionRef =  RandomStringUtils.randomAlphanumeric(16);
									String orderRef = customerAccount.getCustomer().getVerificationNumber() + "" + RandomStringUtils.randomAlphanumeric(8);
									Channel channel = Channel.MOBILE;
									Date transactionDate = new Date();
									ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
									User transactingUser = user;
									String messageRequest = null;
									String messageResponse = null;
									String narration = "GC|CA:" + customerAccount.getAccountNumber() + "|CD:" + gpe.getCountributionPackage().getGroup().getGroupCode() + "|AMT:" + amountPaidForContribution;
									
									
									Boolean creditAccountTrue = null;
									Boolean creditCardTrue = null;
									String rpin = null;
									String payerName = customer.getFirstName() + " " + customer.getLastName();
									String payerEmail = customer.getContactEmail();
									String payerMobile = customer.getContactMobile();
									TransactionStatus status = TransactionStatus.SUCCESS;
									ProbasePayCurrency probasePayCurrency = gpe.getCountributionPackage().getCurrency();
									String transactionCode = TransactionCode.transactionSuccess;
									Boolean creditPoolAccountTrue = null;
									String responseCode = null;
									String oTP = null;
									String oTPRef = null;
									Long merchantId = device.getMerchant().getId();
									String merchantName = device.getMerchant().getMerchantName();
									String merchantCode = device.getMerchant().getMerchantCode();
									String merchantBank = device.getMerchant().getMerchantBank().getBankName();
									String merchantAccount = null; 
									Long transactingBankId = device.getMerchant().getMerchantBank().getId();
									Long receipientTransactingBankId = null;
									Integer accessCode = null;
									Long sourceEntityId = account.getId();
									Long receipientEntityId = null;
									Channel receipientChannel = channel;
									String transactionDetail = "Pay Merchant - " + device.getMerchant().getMerchantName();
									Double closingBalance = 0.00;
									Double totalCreditSum = 0.00;
									Double totalDebitSum = 0.00;
									Long paidInByBankUserAccountId = null;
									String customData = null;
									String responseData = null;
									Long adjustedTransactionId = null;
									Long acquirerId = acquirer.getId();
									Boolean debitAccountTrue = true;
									Boolean debitCardTrue = false;
									Long creditAccountId = null;
									Long creditCardId = null;
									Long debitAccountId = account.getId();
									Long debitCardId = null;
									String details = transactionDetail;
	
									
									
									Transaction transaction = new Transaction(transactionRef, null,
											customer.getId(), creditAccountTrue, creditCardTrue,
											orderRef, rpin, channel,
											transactionDate, ServiceType.GROUP_CONTRIBUTION, payerName,
											payerEmail, payerMobile, status,
											probasePayCurrency, transactionCode,
											account, null, device,
											creditPoolAccountTrue, messageRequest,
											 messageResponse, fixedCharge,
											 transactionCharge, transactionPercentage,
											 schemeTransactionCharge, schemeTransactionPercentage,
											 amount,  responseCode, oTP, oTPRef,
											 merchantId,  merchantName, merchantCode,
											 merchantBank, merchantAccount, 
											 transactingBankId, receipientTransactingBankId,
											 accessCode,  sourceEntityId, receipientEntityId,
											 receipientChannel,  transactionDetail,
											 closingBalance,  totalCreditSum,  totalDebitSum,
											 paidInByBankUserAccountId,  customData,
											 responseData,  adjustedTransactionId,  acquirerId,  debitAccountTrue,  debitCardTrue, 
											 creditAccountId,
											 creditCardId,
											 debitAccountId,
											 debitCardId,  details);
									swpService.createNewRecord(transaction);
								}
								
							}
						}
						
						
						if(gpe.getOutstandingBalance()==0.00)
						{
							gpe.setIsPaid(Boolean.TRUE);
						}
						gpe.setUpdatedAt(new Date());
						swpService.updateRecord(gpe);
						
						
						
						groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() + amountPaidForContribution);
						swpService.updateRecord(groupAccount);
						
					}
				}
				
			}
			else
			{
				Double amountToPay = amount;
				Double amountPaidForContribution = 0.00;
				if(amountToPay>0)
				{
					Double outstandingBalance = groupPaymentsExpected.getOutstandingBalance();
					Double contributionAmountOutstanding = groupPaymentsExpected.getAmount() - groupPaymentsExpected.getAmountPaid(); 
					
					if((amountToPay - contributionAmountOutstanding)>=0)
					{
						//Pay Primary Amount
						groupPaymentsExpected.setAmountPaid(groupPaymentsExpected.getAmountPaid() + contributionAmountOutstanding);
						groupPaymentsExpected.setOutstandingBalance(groupPaymentsExpected.getOutstandingBalance() - contributionAmountOutstanding);
						totalAmountPaidNow = totalAmountPaidNow + contributionAmountOutstanding;
						amountPaidForContribution = amountPaidForContribution + contributionAmountOutstanding;
						amountToPay = amountToPay - contributionAmountOutstanding;
						GroupContribution gc = new GroupContribution(amountPaidForContribution, customerAccount, groupPaymentsExpected, Boolean.TRUE, 
								groupPaymentsExpected.getGroupId());
						swpService.createNewRecord(gc);
					}
					else
					{
						if(amountToPay>0)
						{
							//Pay Primary Amount
							groupPaymentsExpected.setAmountPaid(groupPaymentsExpected.getAmountPaid() + amountToPay);
							groupPaymentsExpected.setOutstandingBalance(groupPaymentsExpected.getOutstandingBalance() - amountToPay);
							totalAmountPaidNow = totalAmountPaidNow + amountToPay;
							amountPaidForContribution = amountPaidForContribution + amountToPay;
							amountToPay = amountToPay - amountToPay;
							GroupContribution gc = new GroupContribution(amountToPay, customerAccount, groupPaymentsExpected, Boolean.TRUE, groupPaymentsExpected.getGroupId());
							swpService.createNewRecord(gc);
						}
					}
					//Pay penalty
					Double penaltyBalance = groupPaymentsExpected.getTotalPenalties() - groupPaymentsExpected.getPenaltiesPaid();
					if(amountToPay>=penaltyBalance && penaltyBalance>0.00)
					{
						groupPaymentsExpected.setPenaltiesPaid(groupPaymentsExpected.getPenaltiesPaid() + penaltyBalance);
						groupPaymentsExpected.setOutstandingBalance(groupPaymentsExpected.getOutstandingBalance() - penaltyBalance);
						totalAmountPaidNow = totalAmountPaidNow + penaltyBalance;
						amountPaidForContribution = amountPaidForContribution + penaltyBalance;
						amountToPay = amountToPay - penaltyBalance;
						GroupContribution gc = new GroupContribution(penaltyBalance, customerAccount, groupPaymentsExpected, Boolean.FALSE, groupPaymentsExpected.getGroupId());
						swpService.createNewRecord(gc);
					}
					else
					{
						if(amountToPay>0.00)
						{
							groupPaymentsExpected.setPenaltiesPaid(groupPaymentsExpected.getPenaltiesPaid() + amountToPay);
							groupPaymentsExpected.setOutstandingBalance(groupPaymentsExpected.getOutstandingBalance() - amountToPay);
							totalAmountPaidNow = totalAmountPaidNow + amountToPay;
							amountPaidForContribution = amountPaidForContribution + amountToPay;
							amountToPay = amountToPay - amountToPay;
							GroupContribution gc = new GroupContribution(amountToPay, customerAccount, groupPaymentsExpected, Boolean.FALSE, groupAccount.getGroup().getId());
							swpService.createNewRecord(gc);
						}
					}
					groupPaymentsExpected.setUpdatedAt(new Date());
					
					if(groupPaymentsExpected.getOutstandingBalance()==0.00)
					{
						groupPaymentsExpected.setIsPaid(Boolean.TRUE);
					}
					groupPaymentsExpected.setUpdatedAt(new Date());
					swpService.updateRecord(groupPaymentsExpected);
					
					
					System.out.println("totalAmountPaidNow .." + totalAmountPaidNow);
					groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() + amountPaidForContribution);
					swpService.updateRecord(groupAccount);
					
				}
			}
			
			

			System.out.println("1 totalAmountPaidNow .." + totalAmountPaidNow);
			GroupMember groupMember = groupPaymentsExpected.getGroupMember();
			groupMember.setCurrentContributions(groupMember.getCurrentContributions() + totalAmountPaidNow);
			swpService.updateRecord(groupMember);
			
			customerAccount.setCurrentBalance(customerAccount.getAvailableBalance() - totalAmountPaidNow);
			swpService.updateRecord(customerAccount);
			
			
    		
			JSONObject resp = getGroupDataAction(token, groupMember.getGroup().getId(), requestId, ipAddress);
			
			if(totalAmountPaidNow>0)
			{
				resp.put("status", ERROR.GENERAL_OK);
				resp.put("message", "Hey, Thanks for paying your contribution. Keep paying your contribution towards making your group stronger and richer");
			}
			else
			{
				resp.put("status", ERROR.GENERAL_FAIL);
				resp.put("message", "Your contribution could not be paid. Please try again");
			}
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    */
    
    
    public Response applyForGroupLoan(String token, Long loanTermId, Double principal, Integer term, 
    		String loanDetailsStr, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{

    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && loanTermId!=null && principal!=null && term!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			JSONObject loanDetails_ = new JSONObject();
			loanDetails_.put("From", customer.getFirstName() + " " + customer.getLastName());
			loanDetails_.put("Date", new SimpleDateFormat("MMM d, yyyy HH:mm a").format(new Date()));
			
			JSONArray loanDetailsArray = new JSONArray();
			loanDetailsArray.put(loanDetails_);
			String loanDetails = loanDetailsArray.toString();
			
			
			
			hql = "Select tp from GroupLoanTerms tp where tp.id = " + loanTermId + " AND tp.deletedAt IS NULL";
			GroupLoanTerms groupLoanTerm = (GroupLoanTerms)this.swpService.getUniqueRecordByHQL(hql);
			
			if(groupLoanTerm==null)
			{
				jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				jsonObject.put("message", "Your selected group loan could not be found. Please select a valid contribution package");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = " + customer.getId() + " AND tp.group.id = " + groupLoanTerm.getContributionPackage().getGroup().getId() + 
					" AND tp.deletedAt IS NULL";
			GroupMember groupMember= (GroupMember)swpService.getUniqueRecordByHQL(hql);

    		if(groupMember==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you are not a member of the group to apply for loans");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
			hql = "Select tp from GroupAccount tp where tp.group.id = " + groupLoanTerm.getContributionPackage().getGroup().getId() + " AND tp.deletedAt IS NULL";
			GroupAccount groupAccount = (GroupAccount)swpService.getUniqueRecordByHQL(hql);
			if(groupAccount.getCurrentBalance()<principal)
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
				jsonObject.put("message", "There are not sufficient funds currently in your groups wallet. You can try booking a loan later after the group has received payments");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
    		
    		
			Integer minLoanTenor = groupLoanTerm.getMinimumTerm();
    		Integer maxLoanTenor = groupLoanTerm.getMaximumTerm();
    		Double minLoanPrincipal = groupLoanTerm.getMinimumPrincipalLoanable();
    		Double maxLoanPrincipal = groupLoanTerm.getMaximumPrincipalLoanable();
    		
    		if((term<minLoanTenor) || (term>maxLoanTenor))
    		{
    			jsonObject.put("message", "Your preferred loan repayment period is not valid. Your loan repayment period must be at least " + 
    				minLoanTenor + "" + groupLoanTerm.getRepaymentTenorType().name().toLowerCase() + "(s) and not more than " + maxLoanTenor + "" + groupLoanTerm.getRepaymentTenorType().name().toLowerCase() + "(s)");
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if((principal<minLoanPrincipal) || (principal>maxLoanPrincipal))
    		{
    			jsonObject.put("message", "Your preferred loan amount must be at least K" + 
    				minLoanPrincipal + "" + " and not more than K" + maxLoanPrincipal + "");
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
    		
    		hql = "Select tp from GroupLoan tp where tp.status IN (" + GroupLoanStatus.ACTIVE.ordinal() + ", " + 
    				GroupLoanStatus.DEFAULTED.ordinal() + ", " + GroupLoanStatus.LATE_30.ordinal() + ", " + 
    				GroupLoanStatus.LATE_60.ordinal() + ", " + GroupLoanStatus.LATE_90.ordinal() + ") AND tp.deletedAt IS NULL " +
    				"AND tp.contributionPackage.id = " + groupLoanTerm.getContributionPackage().getId();
    		Collection<GroupLoan> groupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
    		if(groupLoans.size()>0)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_HAS_LOANS);
				jsonObject.put("message", "Hey, you already have loans that are yet to be closed. Ensure you close out on those loans by paying off any outstandings in " +
						"order to qualify for a new loan");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Double minimumBalance = 0.00;
    		Double totalContributions = 0.00;
    		Double totalContributionDebits = 0.00;
    		hql = "Select tp from GroupContribution tp where tp.groupPaymentExpected.groupMember.addedCustomer.id = " + customer.getId() + " " +
    				"AND tp.groupPaymentExpected.groupMember.group.id = " + groupLoanTerm.getContributionPackage().getGroup().getId() +
    				"AND tp.deletedAt IS NULL";
    		Collection<GroupContribution> allGroupMemberContributions= (Collection<GroupContribution>)this.swpService.getAllRecordsByHQL(hql);
    		if(allGroupMemberContributions!=null && allGroupMemberContributions.size()>0)
    		{
    			Iterator<GroupContribution> it = allGroupMemberContributions.iterator();
    			while(it.hasNext())
    			{
    				GroupContribution gc = it.next();
    				totalContributions = totalContributions + gc.getAmount();
    			}
    		}
    		
    		
    		hql = "Select tp from ContributionPackageDebit tp where tp.groupMember.addedCustomer.id = "+ customer.getId()  + 
    				" AND tp.contributionPackage.id = " + groupLoanTerm.getContributionPackage().getId() + 
					" AND tp.deletedAt IS NULL";
    		Collection<ContributionPackageDebit> allGroupMemberContributionDebits= (Collection<ContributionPackageDebit>)this.swpService.getAllRecordsByHQL(hql);
    		if(allGroupMemberContributionDebits!=null && allGroupMemberContributionDebits.size()>0)
    		{
    			Iterator<ContributionPackageDebit> it = allGroupMemberContributionDebits.iterator();
    			while(it.hasNext())
    			{
    				ContributionPackageDebit cpd = it.next();
    				totalContributionDebits = totalContributions + cpd.getAmount();
    			}
    		}
    		
    		Double currentBalance = totalContributions - totalContributionDebits;
    		
    		if(currentBalance < groupLoanTerm.getMinimumTotalContribution())
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_HAS_LOANS);
    			jsonObject.put("totalContribution", totalContributions);
				jsonObject.put("message", "Hey, you can not book for this loan as you do not qualify. To qualify, your contributions must exceed K" + groupLoanTerm.getMinimumTotalContribution());
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}

    		
    		hql = "Select tp from GroupLoan tp where tp.contributionPackage.id = " + groupLoanTerm.getContributionPackage().getId();
    		Collection<GroupLoan> allGroupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
    		
    		
    		String availableTenorType = "";
    		TenorType repaymentTenorType = groupLoanTerm.getRepaymentTenorType();
    		if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.MONTH))
    		{
    			availableTenorType = (TenorType.MONTH.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.WEEK))
    		{
    			availableTenorType = (TenorType.WEEK.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.YEAR))
    		{
    			availableTenorType = (TenorType.YEAR.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.DAY))
    		{
    			availableTenorType = (TenorType.DAY.name());
    		}
    		
    		Integer groupSize = allGroupLoans.size() + 1;
    		String groupSizeStr = groupSize + "";
    		String loanAccountNo = StringUtils.leftPad(""+(groupSize), (8-groupSizeStr.length()), '0');
    		
    		JSONObject sched = null;
    		JSONArray schedule = null;
    		GroupLoan groupLoan = null;
    		if(availableTenorType.equals(TenorType.MONTH.name()))
    		{
    			Double monthlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForMonthly(principal, monthlyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			Date repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}/**/
    			groupLoan = new GroupLoan(groupLoanTerm.getContributionPackage(), groupMember, principal, monthlyInterestRate, term, TenorType.MONTH, new Date(), null, 
    					repaymentStartDate, GroupLoanStatus.APPLYING, loanAccountNo, 0.00, 0.00, outstandingBalance, 0.00, 0.00, 0.00, groupLoanTerm, 
    					groupMember.getGroup().getId(), totalIncurredInterest, loanDetails);
    			swpService.createNewRecord(groupLoan);
    			/*groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);*/
    		}
    		else if(availableTenorType.equals(TenorType.WEEK.name()))
    		{
    			Double weeklyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForWeekly(principal, weeklyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			Date repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
    			groupLoan = new GroupLoan(groupLoanTerm.getContributionPackage(), groupMember, principal, weeklyInterestRate, term, TenorType.WEEK, new Date(), new Date(), 
    					repaymentStartDate, GroupLoanStatus.APPLYING, loanAccountNo, 0.00, 0.00, outstandingBalance, 0.00, 0.00, 0.00, groupLoanTerm, 
    					groupMember.getGroup().getId(), totalIncurredInterest, loanDetails);
    			swpService.createNewRecord(groupLoan);
    			/*groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);*/
    		}
    		else if(availableTenorType.equals(TenorType.DAY.name()))
    		{
    			Double dailyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForDaily(principal, dailyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			Date repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
    			groupLoan = new GroupLoan(groupLoanTerm.getContributionPackage(), groupMember, principal, dailyInterestRate, term, TenorType.DAY, new Date(), new Date(), 
    					repaymentStartDate, GroupLoanStatus.APPLYING, loanAccountNo, 0.00, 0.00, outstandingBalance, 0.00, 0.00, 0.00, groupLoanTerm, 
    					groupMember.getGroup().getId(), totalIncurredInterest, loanDetails);
    			swpService.createNewRecord(groupLoan);
    			/*groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);*/
    		}
    		else if(availableTenorType.equals(TenorType.YEAR.name()))
    		{
    			Double yearlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForYearly(principal, yearlyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			Date repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
				totalIncurredInterest = BigDecimal.valueOf(totalIncurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
				totalIncurredPrincipal = BigDecimal.valueOf(totalIncurredPrincipal).setScale(2, RoundingMode.HALF_UP).doubleValue();
    			groupLoan = new GroupLoan(groupLoanTerm.getContributionPackage(), groupMember, principal, yearlyInterestRate, term, TenorType.YEAR, new Date(), new Date(), 
    					repaymentStartDate, GroupLoanStatus.APPLYING, loanAccountNo, 0.00, 0.00, outstandingBalance, 0.00, 0.00, 0.00, groupLoanTerm, 
    					groupMember.getGroup().getId(), totalIncurredInterest, loanDetails);
    			swpService.createNewRecord(groupLoan);
    			/*groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);*/
    		}
    		


    		
    		jsonObject.put("schedule", schedule.toString());
    		jsonObject.put("groupLoan", new Gson().toJson(groupLoan));
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, Congratulations! We have sent to your village banking group officials your loan request for the sum of " + groupLoanTerm.getContributionPackage().getCurrency().name() + "" + principal + ". " +
					"They will review to approve or disapprove your loan. Please remember to repay your loan on time to help the group grow");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    
    
    
    public Response repayGroupLoan(String token, Long groupLoanExpectedRepaymentId, Double amount, String pin, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{

    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupLoanExpectedRepaymentId!=null && amount!=null && pin!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from CustomerAccount tp where tp.customer.id = " + customer.getId() + " AND tp.deletedAt IS NULL";
			VBCustomerAccount customerAccount = (VBCustomerAccount)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from GroupLoanRepaymentsExpected tp where tp.id = " + groupLoanExpectedRepaymentId + " AND tp.deletedAt IS NULL";
			GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = (GroupLoanRepaymentsExpected)this.swpService.getUniqueRecordByHQL(hql);
			
			if(groupLoanRepaymentsExpected==null)
			{
				jsonObject.put("status", ERROR.GROUP_LOAN_REPAYMENT_SCHEDULE_NOT_FOUND);
				jsonObject.put("message", "Hey, your selected loan repayment schedule was not found");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}

			hql = "Select tp from GroupLoanTerms tp where tp.deletedAt IS NULL AND tp.contributionPackage.id  = " + groupLoanRepaymentsExpected.getGroupLoan().getContributionPackage().getId();
    		GroupLoanTerms groupLoanTerm = (GroupLoanTerms)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = "+ customer.getId()  +" AND tp.group.id = " + 
					groupLoanRepaymentsExpected.getGroupLoan().getContributionPackage().getGroup().getId() + 
					" AND tp.isActive = 1 AND tp.deletedAt IS NULL";
    		GroupMember groupMember= (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupMember==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you are not a member of the group to apply for loans");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		String minimumAccountBalanceStr = app.getAllSettings().getString(VillageBankingSetting.MINIMUM_WALLET_BALANCE.name());
    		Double minAcctBal = Double.valueOf(minimumAccountBalanceStr);
    		
    		Double amountOnHandToPay = amount;
    		Double totalAmountFinallyRepaid = 0.00;
    		Double interestAmountRepaid = 0.00;
    		Double principalAmountRepaid = 0.00;
    		Double penaltyAmountRepaid = 0.00;
    		if((customerAccount.getAvailableBalance() - minAcctBal)<amountOnHandToPay)
    		{
    			jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS_IN_WALLET);
				jsonObject.put("message", "Hey, you do not enough funds in your wallet to make this payment. Your available balance is K" + customerAccount.getAvailableBalance());
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		else
    		{
    			RepaymentStrategy repaymentStrategy = groupLoanTerm.getRepaymentStrategy();
    			if(repaymentStrategy==null)
    			{
    				repaymentStrategy = RepaymentStrategy.INTEREST_PRINCIPAL_PENALTY;
    			}
    			Double totalRepaid = groupLoanRepaymentsExpected.getTotalAmountRepaid();
    			Double balanceOutstanding = (groupLoanRepaymentsExpected.getTotalInterestIncurred() + groupLoanRepaymentsExpected.getTotalPrincipalIncurred() + 
    					groupLoanRepaymentsExpected.getTotalPenaltiesIncurred()) - totalRepaid;
    			GroupLoan groupLoan = groupLoanRepaymentsExpected.getGroupLoan();
    			
    			
    			
    			if(balanceOutstanding>0)
    			{

					//Interest FIRST before PRINCIPAL repayments then PENALTY
					Double interestBalance= (groupLoanRepaymentsExpected.getTotalInterestIncurred() - groupLoanRepaymentsExpected.getTotalInterestRepaid());
					if(interestBalance > 0)
					{
						if(amountOnHandToPay>=interestBalance)
						{
							//Amount On Hand is equals interest balance or greater than interest balance
							groupLoanRepaymentsExpected.setTotalInterestRepaid(groupLoanRepaymentsExpected.getTotalInterestRepaid() + interestBalance);
							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + interestBalance);
							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
							swpService.updateRecord(groupLoanRepaymentsExpected);
							totalAmountFinallyRepaid = totalAmountFinallyRepaid + interestBalance;
							interestAmountRepaid = interestBalance;
							groupLoan.setTotalInterestRepaid(groupLoan.getTotalInterestRepaid() + interestAmountRepaid);
							swpService.updateRecord(groupLoan);
							amountOnHandToPay = amountOnHandToPay - interestBalance;
						}
						else
						{
							//Amount on hand is less than interest balance
							groupLoanRepaymentsExpected.setTotalInterestRepaid(groupLoanRepaymentsExpected.getTotalInterestRepaid() + amountOnHandToPay);
							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + amountOnHandToPay);
							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
							swpService.updateRecord(groupLoanRepaymentsExpected);
							totalAmountFinallyRepaid = totalAmountFinallyRepaid + amountOnHandToPay;
							interestAmountRepaid = amountOnHandToPay;
							groupLoan.setTotalInterestRepaid(groupLoan.getTotalInterestRepaid() + interestAmountRepaid);
							swpService.updateRecord(groupLoan);
							amountOnHandToPay = 0.00;
						}
						
						/*Update Outstanding Amount*/
						Double totalPrincipalIncurred = groupLoanRepaymentsExpected.getTotalPrincipalIncurred();
	    				totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalPrincipalIncurred);
	    				log.info(totalPrincipalIncurred);
	    				Double totalPenaltiesIncurred = groupLoanRepaymentsExpected.getTotalPenaltiesIncurred();
	    				totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalPenaltiesIncurred);
	    				log.info(totalPenaltiesIncurred);
	    				Double totalInterestIncurred = groupLoanRepaymentsExpected.getTotalInterestIncurred();
	    				totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalInterestIncurred);
	    				log.info(totalInterestIncurred);
	    				Double totalPrincipalRepaid = groupLoanRepaymentsExpected.getTotalPrincipalRepaid();
	    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalPrincipalRepaid);
	    				log.info(totalPrincipalRepaid);
	    				Double totalPenaltiesRepaid = groupLoanRepaymentsExpected.getTotalPenaltiesRepaid();
	    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalPenaltiesRepaid);
	    				log.info(totalPenaltiesRepaid);
	    				Double totalInterestRepaid = groupLoanRepaymentsExpected.getTotalInterestRepaid();
	    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
	    				System.out.println(totalInterestRepaid);
	    				log.info(totalInterestRepaid);
	    				
	    				Double amountIncurred = totalPrincipalIncurred + totalPenaltiesIncurred + totalInterestIncurred;
	    				System.out.println(amountIncurred);
	    				log.info(amountIncurred);
	    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
	    				System.out.println(amountRepaid);
	    				log.info(amountRepaid);
	    				Double totalBalance = amountIncurred - amountRepaid;
	    				if(totalBalance==0)
	    				{
	    					groupLoan.setStatus(GroupLoanStatus.CLOSED);
	    				}
	    				groupLoan.setOutstandingBalance(totalBalance);
	    				swpService.updateRecord(groupLoan);
					}
					
					
					//REPAY PRINCIPAL
					if(amountOnHandToPay>0)
					{
    					Double principalBalance= (groupLoanRepaymentsExpected.getTotalPrincipalIncurred() - groupLoanRepaymentsExpected.getTotalPrincipalRepaid());
    					if(principalBalance > 0)
    					{
    						if(amountOnHandToPay>=principalBalance)
    						{
    							//Amount On Hand is equals interest balance or greater than interest balance
    							groupLoanRepaymentsExpected.setTotalPrincipalRepaid(groupLoanRepaymentsExpected.getTotalPrincipalRepaid() + principalBalance);
    							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + principalBalance);
    							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
    							swpService.updateRecord(groupLoanRepaymentsExpected);
    							totalAmountFinallyRepaid = totalAmountFinallyRepaid + principalBalance;
    							principalAmountRepaid = principalBalance;
    							groupLoan.setTotalPrincipalRepaid((groupLoan.getTotalPrincipalRepaid() + principalAmountRepaid));
    							swpService.updateRecord(groupLoan);
    							groupMember.setCurrentContributions(groupMember.getCurrentContributions() + principalAmountRepaid);
    							swpService.updateRecord(groupMember);
    							amountOnHandToPay = amountOnHandToPay - principalBalance;
    						}
    						else
    						{
    							//Amount on hand is less than interest balance
    							groupLoanRepaymentsExpected.setTotalPrincipalRepaid(groupLoanRepaymentsExpected.getTotalPrincipalRepaid() + amountOnHandToPay);
    							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + amountOnHandToPay);
    							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
    							swpService.updateRecord(groupLoanRepaymentsExpected);
    							totalAmountFinallyRepaid = totalAmountFinallyRepaid + amountOnHandToPay;
    							principalAmountRepaid = amountOnHandToPay;
    							groupLoan.setTotalPrincipalRepaid((groupLoan.getTotalPrincipalRepaid() + principalAmountRepaid));
    							swpService.updateRecord(groupLoan);
    							groupMember.setCurrentContributions(groupMember.getCurrentContributions() + principalAmountRepaid);
    							swpService.updateRecord(groupMember);
    							amountOnHandToPay = 0.00;
    						}
    						
    						/*Update Outstanding Amount*/
    						Double totalPrincipalIncurred = groupLoanRepaymentsExpected.getTotalPrincipalIncurred();
    	    				totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPrincipalIncurred);
    	    				log.info(totalPrincipalIncurred);
    	    				Double totalPenaltiesIncurred = groupLoanRepaymentsExpected.getTotalPenaltiesIncurred();
    	    				totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPenaltiesIncurred);
    	    				log.info(totalPenaltiesIncurred);
    	    				Double totalInterestIncurred = groupLoanRepaymentsExpected.getTotalInterestIncurred();
    	    				totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalInterestIncurred);
    	    				log.info(totalInterestIncurred);
    	    				Double totalPrincipalRepaid = groupLoanRepaymentsExpected.getTotalPrincipalRepaid();
    	    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPrincipalRepaid);
    	    				log.info(totalPrincipalRepaid);
    	    				Double totalPenaltiesRepaid = groupLoanRepaymentsExpected.getTotalPenaltiesRepaid();
    	    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPenaltiesRepaid);
    	    				log.info(totalPenaltiesRepaid);
    	    				Double totalInterestRepaid = groupLoanRepaymentsExpected.getTotalInterestRepaid();
    	    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalInterestRepaid);
    	    				log.info(totalInterestRepaid);
    	    				
    	    				Double amountIncurred = totalPrincipalIncurred + totalPenaltiesIncurred + totalInterestIncurred;
    	    				System.out.println(amountIncurred);
    	    				log.info(amountIncurred);
    	    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
    	    				System.out.println(amountRepaid);
    	    				log.info(amountRepaid);
    	    				Double totalBalance = amountIncurred - amountRepaid;
    	    				groupLoan.setOutstandingBalance(totalBalance);
    	    				if(totalBalance==0)
    	    				{
    	    					groupLoan.setStatus(GroupLoanStatus.CLOSED);
    	    				}
    	    				swpService.updateRecord(groupLoan);
    					}
					}
					
					
					//REPAY PENALTIES
					if(amountOnHandToPay>0)
					{
    					Double penaltiesBalance= (groupLoanRepaymentsExpected.getTotalPenaltiesIncurred() - groupLoanRepaymentsExpected.getTotalPenaltiesRepaid());
    					if(penaltiesBalance > 0)
    					{
    						if(amountOnHandToPay>=penaltiesBalance)
    						{
    							//Amount On Hand is equals interest balance or greater than interest balance
    							groupLoanRepaymentsExpected.setTotalPenaltiesRepaid(groupLoanRepaymentsExpected.getTotalPenaltiesRepaid() + penaltiesBalance);
    							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + penaltiesBalance);
    							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
    							swpService.updateRecord(groupLoanRepaymentsExpected);
    							totalAmountFinallyRepaid = totalAmountFinallyRepaid + penaltiesBalance;
    							penaltyAmountRepaid = penaltiesBalance;
    							groupLoan.setTotalPenaltiesRepaid(groupLoan.getTotalPenaltiesRepaid() + penaltyAmountRepaid);
    							swpService.updateRecord(groupLoan);
    							amountOnHandToPay = amountOnHandToPay - penaltiesBalance;
    						}
    						else
    						{
    							//Amount on hand is less than interest balance
    							groupLoanRepaymentsExpected.setTotalPenaltiesRepaid(groupLoanRepaymentsExpected.getTotalPenaltiesRepaid() + amountOnHandToPay);
    							groupLoanRepaymentsExpected.setTotalAmountRepaid(groupLoanRepaymentsExpected.getTotalAmountRepaid() + amountOnHandToPay);
    							groupLoanRepaymentsExpected.setUpdatedAt(new Date());
    							swpService.updateRecord(groupLoanRepaymentsExpected);
    							totalAmountFinallyRepaid = totalAmountFinallyRepaid + amountOnHandToPay;
    							penaltyAmountRepaid = amountOnHandToPay;
    							groupLoan.setTotalPrincipalRepaid((groupLoan.getTotalPrincipalRepaid() + penaltyAmountRepaid));
    							swpService.updateRecord(groupLoan);
    							amountOnHandToPay = 0.00;
    							
    						}
    						
    						/*Update Outstanding Amount*/
    						Double totalPrincipalIncurred = groupLoanRepaymentsExpected.getTotalPrincipalIncurred();
    	    				totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPrincipalIncurred);
    	    				log.info(totalPrincipalIncurred);
    	    				Double totalPenaltiesIncurred = groupLoanRepaymentsExpected.getTotalPenaltiesIncurred();
    	    				totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPenaltiesIncurred);
    	    				log.info(totalPenaltiesIncurred);
    	    				Double totalInterestIncurred = groupLoanRepaymentsExpected.getTotalInterestIncurred();
    	    				totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalInterestIncurred);
    	    				log.info(totalInterestIncurred);
    	    				Double totalPrincipalRepaid = groupLoanRepaymentsExpected.getTotalPrincipalRepaid();
    	    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPrincipalRepaid);
    	    				log.info(totalPrincipalRepaid);
    	    				Double totalPenaltiesRepaid = groupLoanRepaymentsExpected.getTotalPenaltiesRepaid();
    	    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalPenaltiesRepaid);
    	    				log.info(totalPenaltiesRepaid);
    	    				Double totalInterestRepaid = groupLoanRepaymentsExpected.getTotalInterestRepaid();
    	    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    	    				System.out.println(totalInterestRepaid);
    	    				log.info(totalInterestRepaid);
    	    				
    	    				Double amountIncurred = totalPrincipalIncurred + totalPenaltiesIncurred + totalInterestIncurred;
    	    				System.out.println(amountIncurred);
    	    				log.info(amountIncurred);
    	    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
    	    				System.out.println(amountRepaid);
    	    				log.info(amountRepaid);
    	    				Double totalBalance = amountIncurred - amountRepaid;
    	    				groupLoan.setOutstandingBalance(totalBalance);
    	    				if(totalBalance==0)
    	    				{
    	    					groupLoan.setStatus(GroupLoanStatus.CLOSED);
    	    				}
    	    				swpService.updateRecord(groupLoan);
    					}
					}
					
					if(totalAmountFinallyRepaid>0)
					{
						Double newBalance = groupLoanRepaymentsExpected.getTotalAmountRepaid() - 
								(groupLoanRepaymentsExpected.getTotalPenaltiesIncurred() + groupLoanRepaymentsExpected.getTotalPrincipalIncurred() + 
										groupLoanRepaymentsExpected.getTotalInterestIncurred()); 
						if(newBalance==0)
						{
							groupLoanRepaymentsExpected.setIsCompleted(Boolean.TRUE);
						}
						customerAccount.setCurrentBalance(customerAccount.getCurrentBalance() - totalAmountFinallyRepaid);
						customerAccount.setAvailableBalance(customerAccount.getCurrentBalance() - totalAmountFinallyRepaid);
						swpService.updateRecord(customerAccount);
						
						GroupLoanRepayment groupLoanRepayment = new GroupLoanRepayment(groupLoanRepaymentsExpected, totalAmountFinallyRepaid, 
								principalAmountRepaid, interestAmountRepaid, penaltyAmountRepaid, groupLoan.getGroupId());
						swpService.createNewRecord(groupLoanRepayment);
						
						groupLoan.setLastRepaymentDate(new Date());
						swpService.updateRecord(groupLoan);
						
						String transactionRef =  RandomStringUtils.randomAlphanumeric(16);
						String orderRef = customerAccount.getCustomer().getVerificationNumber() + "" + RandomStringUtils.randomAlphanumeric(8);
						Channel channel = Channel.MOBILE;
						String currency = customerAccount.getCurrency().name();
						Date transactionDate = new Date();
						ServiceType serviceType = ServiceType.GROUP_LOAN_REPAYMENT;
						User transactingUser = actorUser;
						String messageRequest = null;
						String messageResponse = null;
						String narration = "GP|CA:" + customerAccount.getAccountNumber() + "|LA:" + groupLoan.getLoanAccountNo() + "|AMT:" + totalAmountFinallyRepaid;
						Double fixedCharge = 0.00;
						Double transactionFee = 0.00;
	    				/*Transaction transaction = new Transaction(transactionRef, orderRef, channel, currency, transactionDate, serviceType, 
								transactingUser, customerAccount, TransactionStatus.SUCCESS, messageRequest, messageResponse, fixedCharge, 
								transactionFee, totalAmountFinallyRepaid, narration);
						swpService.createNewRecord(transaction);*/
						
						hql = "Select tp from GroupAccount tp where tp.group.id = " + groupLoan.getGroupLoanTerm().getContributionPackage().getGroup().getId() + " AND " +
								"tp.deletedAt IS NULL";
						GroupAccount groupAccount = (GroupAccount)swpService.getUniqueRecordByHQL(hql);
						if(groupAccount!=null)
						{
							groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() + totalAmountFinallyRepaid);
							swpService.updateRecord(groupAccount);
						}
					}
    			}
    		}
    		
    		
    		
    		if(totalAmountFinallyRepaid==0)
    		{
    			jsonObject.put("status", ERROR.FUNDS_CANT_SERVICE_LOAN);
    			jsonObject.put("message", "Hey, the amount you intend to pay is not enough to repay the loan. Ensure the amount you are paying is enough to service your loan");
    			
                
                return Response.status(200).entity(jsonObject.toString()).build();
    		}

    		
    		
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, Thanks! Total amount repaid for the selected repayment schedule is " + groupLoanRepaymentsExpected.getGroupLoan().getContributionPackage().getCurrency().name() + "" + totalAmountFinallyRepaid + ". " +
					"The balance has been repaid back into your wallet");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    public Response applyPenaltyToLoan(String token, Long groupLoanId, String requestId, String ipAddress) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{

    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupLoanId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			User actorUser = (User)verify.get("actorUser");
			
			
			String hql = "Select tp from GroupLoan tp where tp.status NOT IN (" + 
					GroupLoanStatus.CANCELED.ordinal() + ", " + GroupLoanStatus.CLOSED.ordinal() + ", " + 
					GroupLoanStatus.APPLYING.ordinal() + ", " + GroupLoanStatus.DEFAULTED.ordinal() +
				") AND tp.deletedAt IS NULL";
			Collection<GroupLoan> groupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
			Iterator groupLoanIter = groupLoans.iterator();
			while(groupLoanIter.hasNext())
			{
				GroupLoan groupLoan = (GroupLoan)groupLoanIter.next();
				hql = "Select tp from GroupLoanRepaymentsExpected tp where tp.groupLoan.id = " + groupLoan.getId() + " AND tp.deletedAt IS NULL " +
						"AND tp.isCompleted = 0";
				Collection<GroupLoanRepaymentsExpected> groupLoanRepaymentsExpected = (Collection<GroupLoanRepaymentsExpected>)this.swpService.getUniqueRecordByHQL(hql);
				Iterator<GroupLoanRepaymentsExpected> it = groupLoanRepaymentsExpected.iterator();
				GroupLoanRepaymentsExpected glre = it.next();
				
				ContributionPackage cp = groupLoan.getContributionPackage();
				Double outstandingBalanceWithoutPenalty = glre.getTotalAmountRepaid() - glre.getTotalInterestIncurred() + glre.getTotalPrincipalIncurred();
				Double penaltyApplicable = groupLoan.getContributionPackage().getPenaltyApplicable();
				PenaltyApplicableType penaltyType = groupLoan.getContributionPackage().getPenaltyApplicableType();
				Date lastRepayed = groupLoan.getLastRepaymentDate();
				Date expectedRepaymentDueDate = glre.getExpectedRepaymentDate();
				Date today = new Date();
				if(today.after(expectedRepaymentDueDate) && penaltyApplicable!=null && penaltyType!=null)
				{
					long difference = expectedRepaymentDueDate.getTime() - lastRepayed.getTime();
					long differenceInDay = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
					
					Integer late30Days =  Integer.valueOf(app.getAllSettings().getString(VillageBankingSetting.LATE_30_DAYS.name()));
					Integer late60Days = Integer.valueOf(app.getAllSettings().getString(VillageBankingSetting.LATE_60_DAYS.name()));
					Integer late90Days = Integer.valueOf(app.getAllSettings().getString(VillageBankingSetting.LATE_90_DAYS.name()));
					Integer late120Days = Integer.valueOf(app.getAllSettings().getString(VillageBankingSetting.LATE_120_DAYS.name()));
					Integer late180Days = Integer.valueOf(app.getAllSettings().getString(VillageBankingSetting.LATE_180_DAYS.name()));
					
					if(differenceInDay>late30Days)
					{
						groupLoan.setStatus(GroupLoanStatus.LATE_30);
						swpService.updateRecord(groupLoan);
						if(penaltyType.equals(PenaltyApplicableType.FLAT_FEE))
						{
							Double penalty = differenceInDay * penaltyApplicable;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
						else if(penaltyType.equals(PenaltyApplicableType.PERCENTAGE_OF_AMOUNT))
						{
							Double penalty = differenceInDay * penaltyApplicable/100;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
					}
					if(differenceInDay>late60Days)
					{
						groupLoan.setStatus(GroupLoanStatus.LATE_60);
						swpService.updateRecord(groupLoan);
						if(penaltyType.equals(PenaltyApplicableType.FLAT_FEE))
						{
							Double penalty = differenceInDay * penaltyApplicable;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
						else if(penaltyType.equals(PenaltyApplicableType.PERCENTAGE_OF_AMOUNT))
						{
							Double penalty = differenceInDay * penaltyApplicable/100;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
					}
					if(differenceInDay>late90Days)
					{
						groupLoan.setStatus(GroupLoanStatus.LATE_90);
						swpService.updateRecord(groupLoan);
						if(penaltyType.equals(PenaltyApplicableType.FLAT_FEE))
						{
							Double penalty = differenceInDay * penaltyApplicable;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
						else if(penaltyType.equals(PenaltyApplicableType.PERCENTAGE_OF_AMOUNT))
						{
							Double penalty = differenceInDay * penaltyApplicable/100;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
					}
					if(differenceInDay>late120Days) 
					{
						groupLoan.setStatus(GroupLoanStatus.DEFAULTED);
						swpService.updateRecord(groupLoan);
						if(penaltyType.equals(PenaltyApplicableType.FLAT_FEE))
						{
							Double penalty = differenceInDay * penaltyApplicable;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
						else if(penaltyType.equals(PenaltyApplicableType.PERCENTAGE_OF_AMOUNT))
						{
							Double penalty = differenceInDay * penaltyApplicable/100;
							glre.setTotalPenaltiesIncurred(penalty);
							swpService.updateRecord(glre);
						}
					}
					if(differenceInDay>late180Days) 
					{
						groupLoan.setStatus(GroupLoanStatus.CANCELED);
						swpService.updateRecord(groupLoan);
					}
				}
			}
			
    		
    		
			jsonObject.put("status", ERROR.GENERAL_OK);
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }
    
    
    
    
    

	private Date addDate(Integer contributionPeriod,
			PeriodType contributionPeriodType) {
		// TODO Auto-generated method stub
		Date currentDate = new Date();
		if(contributionPeriodType.equals(PeriodType.DAY))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_YEAR, contributionPeriod);
			return calendar.getTime();
		}
		else if(contributionPeriodType.equals(PeriodType.MONTH))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.MONTH, contributionPeriod);
			return calendar.getTime();
		}
		else if(contributionPeriodType.equals(PeriodType.WEEK))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.DAY_OF_YEAR, contributionPeriod*7);
			return calendar.getTime();
		}
		else if(contributionPeriodType.equals(PeriodType.YEAR))
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(currentDate);
			calendar.add(Calendar.YEAR, contributionPeriod);
			return calendar.getTime();
		}
		return null;
	}

	public JSONObject listGroups(String token, String requestId, String ipAddress) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return jsonObject;
			}
			
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			jsonObject = getListOfGroups(customer);
			log.info("jsonObject...");
			if(jsonObject!=null)
				log.info(jsonObject.toString());
			
            return jsonObject;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return jsonObject;
    	}
	}
	

	public JSONObject topTenRandomGroups(String token, String requestId, String ipAddress) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return jsonObject;
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			jsonObject = getListOfGroups(customer);
			
            return jsonObject;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return jsonObject;
    	}
	}

	public JSONObject myGroups(String token, String requestId, String ipAddress) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return jsonObject;
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			jsonObject = getListOfGroups(customer);
			
            return jsonObject;
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return jsonObject;
    	}
	}
	
	
	private JSONObject getListOfGroups(Customer customer) throws Exception
	{
		JSONObject jsonObject = new JSONObject();
		String hql = "";
		String groupIds = "";
		GroupMember customerGroupMember = null;
		JSONArray jsArrMyAdmins = new JSONArray();
		JSONObject groupMemberCountJS = new JSONObject();
		JSONObject customerGroupMemberJS = new JSONObject();
		System.out.println("getListOfGroups");
		log.info("getListOfGroups");
		hql = "Select tp from GroupMember tp where tp.deletedAt IS NULL AND tp.group.id IN (Select ta.group.id from GroupMember ta WHERE ta.addedCustomer.id = "+ customer.getId() +")";
		Collection<GroupMember> groupMembers = (Collection<GroupMember>)swpService.getAllRecordsByHQL(hql);
		Iterator<GroupMember> it = groupMembers.iterator();
		while(it.hasNext())
		{
			GroupMember gm = it.next();
			groupIds = groupIds + (gm.getGroup().getId()) + ", ";
			int cnt = 0;
			if(groupMemberCountJS.has(gm.getGroup().getGroupCode()))
			{
				cnt = groupMemberCountJS.getInt(gm.getGroup().getGroupCode());
			}
			groupMemberCountJS.put(gm.getGroup().getGroupCode(), cnt + 1);
			if(gm.getAddedCustomer().getId().equals(customer.getId()) && gm.getIsActive()!=null && gm.getIsActive().equals(Boolean.TRUE))
			{
				jsArrMyAdmins.put(gm.getGroup().getId());
			}
			if(gm.getAddedCustomer().getId().equals(customer.getId()))
			{
				customerGroupMemberJS.put(gm.getGroup().getGroupCode(), gm.getId());
			}
		}
		
		log.info("groupIds...." + groupIds);
		
		JSONObject groupContributionsJS = new JSONObject();
		JSONObject myGroupContributionsJS = new JSONObject();
		JSONObject myContributionPackageJS = new JSONObject();
		hql = "Select tp from GroupContribution tp where tp.deletedAt IS NULL";
		Collection<GroupContribution> groupContributions = (Collection<GroupContribution>)swpService.getAllRecordsByHQL(hql);
		Iterator<GroupContribution> it0 = groupContributions.iterator();
		while(it0.hasNext())
		{
			GroupContribution gc = it0.next();
			double totalAmount = 0.00;
			if(groupContributionsJS.has(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode()))
			{
				totalAmount = groupContributionsJS.getDouble(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode());
			}
			groupContributionsJS.put(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode(), (totalAmount + gc.getAmount()));
			
			if(gc.getGroupPaymentExpected().getGroupMember().getAddedCustomer().getId().equals(customer.getId()))
			{
				double myTotalAmount = 0.00;
				if(myGroupContributionsJS.has(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode()))
				{
					myTotalAmount = myGroupContributionsJS.getInt(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode());
				}
				myGroupContributionsJS.put(gc.getGroupPaymentExpected().getGroupMember().getGroup().getGroupCode(), (myTotalAmount + gc.getAmount()));
			}
		}
		
		
		hql = "Select tp from ContributionPackage tp where tp.deletedAt IS NULL";
		Collection<ContributionPackage> contributionPackages = (Collection<ContributionPackage>)swpService.getAllRecordsByHQL(hql);
		Iterator<ContributionPackage> it7 = contributionPackages.iterator();
		while(it7.hasNext())
		{
			ContributionPackage cp = it7.next();
			myContributionPackageJS.put(cp.getGroup().getGroupCode(), cp);
		}
			
		hql = "Select tp from Group tp where tp.deletedAt IS NULL";
		if((groupIds.length()>2))
			hql = hql + " AND tp.id IN ("+ (groupIds.length()==0 ? "" : (groupIds.subSequence(0, groupIds.length()-2))) +")";
		
		Collection<Group> groups = (Collection<Group>)this.swpService.getAllRecordsByHQL(hql);
		
		if(groups==null)
		{
			jsonObject.put("status", ERROR.GROUP_NOT_FOUND);
			jsonObject.put("message", "Hey, you do not have any groups at the moment. You can create or join a group");
			return jsonObject;
		}
		
		JSONArray groupList = new JSONArray();
		Iterator<Group> it1 = groups.iterator();
		int i1 = 0;
		String[] colors = new String[6];
		colors[0] = "#FF5733";
		colors[1] = "#FCFF33";
		colors[2] = "#33FF64";
		colors[3] = "#3361FF";
		colors[4] = "#FF33E6";
		colors[5] = "#FF3333";
		
		while(it1.hasNext())
		{
			int indx = new Random().nextInt(6-0) + 0;
			Group gp = it1.next();
			ContributionPackage cp = myContributionPackageJS.has(gp.getGroupCode()) ? (ContributionPackage)myContributionPackageJS.get(gp.getGroupCode()) : null;
			Integer memberCount = groupMemberCountJS.has(gp.getGroupCode()) ? groupMemberCountJS.getInt(gp.getGroupCode()) : 0;
			Double totalContributions = groupContributionsJS.has(gp.getGroupCode()) ? groupContributionsJS.getDouble(gp.getGroupCode()) : 0.00;
			Double myTotalContributions = myGroupContributionsJS.has(gp.getGroupCode()) ? myGroupContributionsJS.getDouble(gp.getGroupCode()) : 0.00;
			JSONObject groupEntry = new JSONObject();
			groupEntry.put("groupName", gp.getName());
			groupEntry.put("groupId", gp.getId());
			groupEntry.put("contributionPackageStatus", cp!=null && cp.getStartDate()!=null && cp.getEndDate()==null ? true : false);
			if(cp!=null)
				groupEntry.put("contributionPackageId", cp.getId());
			
			groupEntry.put("isOpen", gp.getIsOpen()!=null && gp.getIsOpen().equals(Boolean.TRUE) ? "Open To All" : "Only Invited");
			groupEntry.put("status", gp.getIsActive()==null ? false : gp.getIsActive());
			groupEntry.put("totalMembers", memberCount);
			groupEntry.put("totalContributions", totalContributions);
			groupEntry.put("myTotalContributions", myTotalContributions);
			groupEntry.put("bgColor", gp.getBackgroundColor());
			groupEntry.put("fgColor", gp.getFontColor());
			groupEntry.put("isContributionPackageSet", gp.getIsContributionPackageSet()!=null && gp.getIsContributionPackageSet().equals(Boolean.TRUE) ? true :false);
			groupEntry.put("isLoanPackageSet", gp.getIsContributionPackageSet()!=null && gp.getIsContributionPackageSet().equals(Boolean.TRUE) ? true :false);
			groupEntry.put("logoUrl", "assets/imgs/");
			groupEntry.put("iconTxt", gp.getName().substring(0,  1).toUpperCase());
			groupEntry.put("iconBgColor", colors[indx]);
			groupEntry.put("customerGroupMemberId", customerGroupMemberJS.has(gp.getGroupCode()) ? customerGroupMemberJS.getLong(gp.getGroupCode()) : null);
			groupEntry.put("currentContributionAmount", gp.getCurrentContributionAmount());
			groupEntry.put("currentContributionInterval", gp.getCurrentContributionInterval());
			groupEntry.put("currentMaximumLoan", gp.getCurrentLoanInterestRate());
			groupEntry.put("currentLoanInterestRate", gp.getCurrentLoanInterestRateType());
			groupEntry.put("currentLoanInterestRateType", gp.getCurrentMaximumLoan());
			groupList.put(groupEntry);
			i1++;
		}
		
		//SELECT tp.*, COUNT(d.id) AS purchased FROM groups AS tp INNER JOIN group_members AS d ON d.group_id = tp.id
		
		System.out.println("groupIds...." + groupIds);
		hql = "Select tp.*, COUNT(d.id) AS currentMemberCount from groups tp LEFT JOIN group_members AS d ON d.group_id = tp.id where tp.deletedAt IS NULL AND tp.isActive = 1";
		if(groupMembers.size()>0)
		{
			hql = hql + " AND tp.id NOT IN ("+ (groupIds.length()==0 ? "" : (groupIds.subSequence(0, groupIds.length()-2))) +")";
		}
		hql = hql + " GROUP BY d.group_id ORDER BY RAND() LIMIT 0, 10";
		
		List<Map<String, Object>> topTenGroups = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
		
		
		
		System.out.println("groupIds...." + groupIds);
		hql = "Select tp.*, gm.currentBalance, gm.currentContributions, gm.isActive as isMemberActive, gm.isAdmin from group_members gm, groups tp where gm.group_id = tp.id AND gm.deletedAt IS NULL AND gm.isActive = 1 AND gm.addedCustomer_id = " + customer.getId()
				+ " AND tp.deletedAt IS NULL AND tp.isActive = 1";
		hql = "Select tp.*, COUNT(d.id) AS currentMemberCount, d.currentBalance, d.currentContributions, d.isActive as isMemberActive, d.isAdmin from groups tp LEFT JOIN group_members AS d ON d.group_id = tp.id where tp.deletedAt IS NULL "
				+ "AND tp.isActive = 1 AND d.addedCustomer_id = " + customer.getId() + " GROUP BY d.group_id";
		List<Map<String, Object>> myGroups = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
		
		

		hql = "Select tp.* from group_join_request tp where tp.deletedAt IS NULL AND tp.requestByUserId = " + customer.getUser().getId();
		List<Map<String, Object>> usersGroupRequests = (List<Map<String, Object>>)this.swpService.getQueryBySQLResultsWithKeys(hql);
		
		
		
		
		
		jsonObject.put("groupsAdminList", jsArrMyAdmins.toString());	
		jsonObject.put("status", ERROR.GENERAL_OK);
		jsonObject.put("message", "Group listings found");
		jsonObject.put("groupList", groupList.toString());
		jsonObject.put("topTenGroups", topTenGroups);
		jsonObject.put("usersGroupRequests", usersGroupRequests);
		jsonObject.put("usersGroups", myGroups);
		//jsonObject.put("memberCountList", memberCountList);
		
		return jsonObject;
	}
	
	
	
	
	
	public Response getGroupData(String token, Long groupId, String requestId, String ipAddress) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		jsonObject = getGroupDataAction(token, groupId, requestId, ipAddress);
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
	}
	
	
	
	
	public Response getLoanTermById(String token, Long loanTermId, String requestId, String ipAddress) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{

    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && loanTermId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			
			
			String hql = "Select tp from GroupLoanTerms tp where tp.id = " + loanTermId + " AND tp.deletedAt IS NULL";
			GroupLoanTerms glt = (GroupLoanTerms)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = " + customer.getId() + " AND tp.deletedAt IS NULL AND tp.isActive = 1 " +
					"AND tp.group.id = " + glt.getContributionPackage().getGroup().getId();
			GroupMember gm = (GroupMember)swpService.getUniqueRecordByHQL(hql);
			
			jsonObject.put("interestRate", glt.getInterestRate());
			jsonObject.put("maxPrincipal", glt.getMaximumPrincipalLoanable());
			jsonObject.put("minPrincipal", glt.getMinimumPrincipalLoanable());
			jsonObject.put("minTerm", glt.getMinimumTerm());
			jsonObject.put("maxTerm", glt.getMaximumTerm());
			jsonObject.put("minContribution", glt.getMinimumTotalContribution());
			jsonObject.put("penalty", glt.getPenalty());
			jsonObject.put("loanPenaltyType", glt.getPenaltyApplicableType().name());
			jsonObject.put("interestType", glt.getInterestType().name());
			jsonObject.put("repaymentStrategy", glt.getRepaymentStrategy().name());
			jsonObject.put("repaymentTenorType", glt.getRepaymentTenorType().name());
			jsonObject.put("contributionPackageName", glt.getContributionPackage().getPackageName());
			jsonObject.put("currentBalance", gm.getCurrentBalance());
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
	}
	
	
	private JSONObject getGroupDataAction(String token, Long groupId, String requestId, String ipAddress){
		JSONObject jsonObject = new JSONObject();
		try
		{
			this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
	            return jsonObject;
			}
			
			Customer customer = (Customer)verify.get("customer");
			User actorUser = (User)verify.get("actorUser");
			
			String[] colors = new String[6];
			colors[0] = "#FF5733";
    		colors[1] = "#FCFF33";
    		colors[2] = "#33FF64";
    		colors[3] = "#3361FF";
    		colors[4] = "#FF33E6";
    		colors[5] = "#FF3333";

			String hql = "";
			JSONObject groupContributionsJS = new JSONObject();
			hql = "Select tp from GroupContribution tp where tp.deletedAt IS NULL AND tp.groupPaymentExpected.countributionPackage.group.id = " + groupId;
			Collection<GroupContribution> groupContributions = (Collection<GroupContribution>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupContribution> it0 = groupContributions.iterator();
			while(it0.hasNext())
			{
				GroupContribution gc = it0.next();
				double totalAmount = 0.00;
				if(groupContributionsJS.has(gc.getGroupPaymentExpected().getGroupMember().getId() + ""))
				{
					totalAmount = groupContributionsJS.getDouble(gc.getGroupPaymentExpected().getGroupMember().getId() + "");
				}
				groupContributionsJS.put(gc.getGroupPaymentExpected().getGroupMember().getId() + "", (totalAmount + gc.getAmount()));
				
			}
			
			
			String groupIds = "";
			JSONArray allGroupMembers = new JSONArray();
			hql = "Select tp from GroupMember tp where tp.deletedAt IS NULL AND tp.group.id = " + groupId;
			Collection<GroupMember> groupMembers = (Collection<GroupMember>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupMember> it = groupMembers.iterator();
			int i = 0;
			JSONArray groupAdmins = new JSONArray();
			while(it.hasNext())
			{
				int indx = new Random().nextInt(6-0) + 0;
				GroupMember gm = it.next();
				Double totalContributions = groupContributionsJS.has(gm.getId()+"") ? groupContributionsJS.getDouble(gm.getId()+"") : 0.00;
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				JSONObject groupMember = new JSONObject();
				groupMember.put("bgColor", bgColor);
				groupMember.put("iconBgColor", colors[indx]);
				groupMember.put("iconTxt", gm.getAddedCustomer().getFirstName().substring(0, 1));
				groupMember.put("fullName", gm.getAddedCustomer().getFirstName() + " " + gm.getAddedCustomer().getLastName());
				groupMember.put("totalContributions", totalContributions);
				groupMember.put("groupMemberId", gm.getId());
				groupMember.put("isAdmin", gm.getIsAdmin());
				allGroupMembers.put(groupMember);
				groupAdmins.put(gm.getId());
				i++;
			}
			
			
			JSONObject groupLoansTaken = new JSONObject();
			hql = "Select tp from GroupLoan tp where tp.deletedAt IS NULL AND tp.contributionPackage.group.id = " + groupId;
			Collection<GroupLoan> groupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoan> it2 = groupLoans.iterator();
			while(it2.hasNext())
			{
				GroupLoan gl = it2.next();
				double totalAmount = 0.00;
				if(groupLoansTaken.has(gl.getGroupLoanTerm().getId() + ""))
				{
					totalAmount = groupLoansTaken.getDouble(gl.getGroupLoanTerm().getId() + "");
				}
				groupLoansTaken.put(gl.getGroupLoanTerm().getId() + "", (totalAmount + gl.getPrincipal()));
				
			}
			
			
			JSONArray allGroupLoans = new JSONArray();
			hql = "Select tp from GroupLoanTerms tp where tp.deletedAt IS NULL AND tp.contributionPackage.group.id = " + groupId;
			Collection<GroupLoanTerms> groupLoanTerms = (Collection<GroupLoanTerms>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoanTerms> it1 = groupLoanTerms.iterator();
			i = 0;
			while(it1.hasNext())
			{
				int indx = new Random().nextInt(6-0) + 0;
				GroupLoanTerms glt = it1.next();
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				Double totalContributions = groupLoansTaken.has(glt.getId()+"") ? groupLoansTaken.getDouble(glt.getId()+"") : 0.00;
				JSONObject groupLoanTerm = new JSONObject();
				groupLoanTerm.put("bgColor", bgColor);
				groupLoanTerm.put("iconBgColor", colors[indx]);
				groupLoanTerm.put("iconTxt", glt.getContributionPackage().getPackageName().substring(0, 1));
				groupLoanTerm.put("name", glt.getContributionPackage().getPackageName());
				groupLoanTerm.put("totalLoansTaken", totalContributions);
				groupLoanTerm.put("interestRate", glt.getInterestRate());
				groupLoanTerm.put("interestType", glt.getInterestType().name());
				groupLoanTerm.put("interestType", glt.getInterestType().name());
				groupLoanTerm.put("penalty", glt.getPenalty());
				groupLoanTerm.put("repaymentTenorType", glt.getRepaymentTenorType().name());
				groupLoanTerm.put("maximumLoan", glt.getMaximumPrincipalLoanable());
				groupLoanTerm.put("minimumLoan", glt.getMinimumPrincipalLoanable());
				groupLoanTerm.put("minimumContribution", glt.getMinimumTotalContribution());
				groupLoanTerm.put("maximumLoanPeriod", glt.getMaximumTerm() + glt.getRepaymentTenorType().name());
				groupLoanTerm.put("maximumLoanTenor", glt.getMaximumTerm());
				groupLoanTerm.put("loanTermId", glt.getId());
				groupLoanTerm.put("loanPenaltyType", glt.getPenaltyApplicableType().name());
				allGroupLoans.put(groupLoanTerm);
				i++;
			}
			
			
			
			
			i = 0;
			JSONArray myGroupLoans_ = new JSONArray();
			JSONObject myGroupLoansTaken = new JSONObject();
			hql = "Select tp from GroupLoan tp where tp.deletedAt IS NULL AND tp.contributionPackage.group.id = " + groupId + " AND tp.groupMember.addedCustomer.id = " + customer.getId();
			Collection<GroupLoan> myGroupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoan> it7 = myGroupLoans.iterator();
			while(it7.hasNext())
			{
				GroupLoan gl = it7.next();
				double totalAmount = 0.00;
				int indx = new Random().nextInt(6-0) + 0;
				GroupLoanTerms glt = gl.getGroupLoanTerm();
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				Double totalContributions = groupLoansTaken.has(glt.getId()+"") ? groupLoansTaken.getDouble(glt.getId()+"") : 0.00;
				JSONObject groupLoan = new JSONObject();
				groupLoan.put("bgColor", bgColor);
				groupLoan.put("iconBgColor", colors[indx]);
				groupLoan.put("iconTxt", glt.getContributionPackage().getPackageName().substring(0, 1).toUpperCase());
				groupLoan.put("name", glt.getContributionPackage().getPackageName());
				groupLoan.put("totalLoansTaken", gl.getPrincipal());
				groupLoan.put("totalInterest", gl.getIncurredInterest());
				groupLoan.put("totalPenalties", gl.getIncurredPenalties());
				groupLoan.put("interestRate", glt.getInterestRate());
				groupLoan.put("interestType", glt.getInterestType().name());
				groupLoan.put("penalty", glt.getPenalty());
				groupLoan.put("repaymentTenorType", glt.getRepaymentTenorType().name());
				groupLoan.put("maximumLoan", glt.getMaximumPrincipalLoanable());
				groupLoan.put("maximumLoanPeriod", glt.getMaximumTerm() + glt.getRepaymentTenorType().name());
				groupLoan.put("maximumLoanTenor", glt.getMaximumTerm());
				groupLoan.put("loanTermId", glt.getId());
				groupLoan.put("loanId", gl.getId());
				groupLoan.put("loanPenaltyType", glt.getPenaltyApplicableType().name());
				myGroupLoans_.put(groupLoan);
				i++;
			}/**/
			
				

			JSONArray contributionsMade = new JSONArray();
			while(it0.hasNext())
			{
				int indx = new Random().nextInt(6-0) + 0;
				GroupContribution gc = it0.next();
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				JSONObject jb = new JSONObject();
				jb.put("bgColor", bgColor);
				jb.put("iconBgColor", colors[indx]);
				jb.put("iconTxt", gc.getGroupPaymentExpected().getGroupMember().getAddedCustomer().getFirstName().toUpperCase().substring(0, 1));
				jb.put("amount", gc.getAmount());
				jb.put("datePaid", new SimpleDateFormat("yyyy MMM dd").format(gc.getCreatedAt()));
				if(gc.getIsPenalty()!=null && gc.getIsPenalty().equals(Boolean.TRUE))
					jb.put("paymentType", "Contribution Penalty");
				else
					jb.put("paymentType", "Contribution");
				jb.put("contributionPackage", gc.getGroupPaymentExpected().getCountributionPackage().getPackageName());
				contributionsMade.put(jb);
				i++;
			}
			
			hql = "Select tp from GroupLoanRepayment tp where tp.deletedAt IS NULL AND tp.groupLoanRepaymentExpected.groupLoan.contributionPackage.group.id = " + groupId;
			Collection<GroupLoanRepayment> groupLoanRepayments = (Collection<GroupLoanRepayment>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoanRepayment> it4 = groupLoanRepayments.iterator();
			i = 0;
			while(it4.hasNext())
			{
				int indx = new Random().nextInt(6-0) + 0;
				GroupLoanRepayment glr = it4.next();
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				Double totalContributions = groupLoansTaken.has(glr.getId()+"") ? groupLoansTaken.getDouble(glr.getId()+"") : 0.00;
				JSONObject groupLoanTerm = new JSONObject();
				JSONObject jb = new JSONObject();
				jb.put("bgColor", bgColor);
				jb.put("iconBgColor", colors[indx]);
				jb.put("iconTxt", glr.getGroupLoanRepaymentExpected().getGroupLoan().getGroupMember().getAddedCustomer().getFirstName().toUpperCase().substring(0, 1));
				jb.put("amount", glr.getAmountRepaid());
				jb.put("datePaid", new SimpleDateFormat("yyyy MMM dd").format(glr.getCreatedAt()));
				if(glr.getPenaltyAmountRepaid()>0)
					jb.put("paymentType", "Loan Penalty Repayment");
				else
					jb.put("paymentType", "Loan Repayment");
				
				jb.put("contributionPackage", glr.getGroupLoanRepaymentExpected().getGroupLoan().getContributionPackage().getPackageName());
				contributionsMade.put(jb);
				i++;
			}
			
			
			JSONArray allGroupLoanExpectedRepayments = new JSONArray();
			hql = "Select tp from GroupPaymentsExpected tp where tp.deletedAt IS NULL AND tp.countributionPackage.group.id = " + groupId + " " +
					"AND tp.isPaid = 0 " +
					"AND tp.groupMember.addedCustomer.id = " + customer.getId();
			Collection<GroupPaymentsExpected> groupPaymentsExpected = (Collection<GroupPaymentsExpected>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupPaymentsExpected> it5 = groupPaymentsExpected.iterator();
			i = 0;
			while(it5.hasNext())
			{
				GroupPaymentsExpected gpe = it5.next();
				if(gpe.getIsPaid()!=null && gpe.getIsPaid().equals(Boolean.FALSE))
				{
					int indx = new Random().nextInt(6-0) + 0;
					String bgColor = i%2 == 0 ? "#E2F5D6" : "";
					JSONObject groupLoanTerm = new JSONObject();
					JSONObject jb = new JSONObject();
					jb.put("bgColor", bgColor);
					jb.put("iconBgColor", colors[2]);
					jb.put("iconTxt", "C");
					jb.put("amount", gpe.getAmount() + gpe.getTotalPenalties() - gpe.getAmountPaid() - gpe.getPenaltiesPaid());
					jb.put("name", gpe.getCountributionPackage().getPackageName());
					jb.put("paymentType", "Contribution");
					jb.put("id", gpe.getId());
					jb.put("groupMemberId", gpe.getGroupMember().getId());
					jb.put("groupMember", gpe.getGroupMember().getAddedCustomer().getFirstName() + " " + gpe. getGroupMember().getAddedCustomer().getLastName());
					jb.put("expectedPaymentDate", new SimpleDateFormat("yyyy MMM dd").format(gpe.getDateExpected()));
					allGroupLoanExpectedRepayments.put(jb);
					i++;
				}
			}
			
			hql = "Select tp from GroupLoanRepaymentsExpected tp where tp.deletedAt IS NULL AND " +
					"tp.isCompleted = 1 AND " +
					"tp.groupLoan.contributionPackage.group.id = " + groupId + " AND " +
					"tp.groupLoan.groupMember.addedCustomer.id = " + customer.getId();
			Collection<GroupLoanRepaymentsExpected> groupLoanRepaymentsExpected = (Collection<GroupLoanRepaymentsExpected>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoanRepaymentsExpected> it6 = groupLoanRepaymentsExpected.iterator();
			i = 0;
			while(it6.hasNext())
			{
				GroupLoanRepaymentsExpected glre = it6.next();
				if(glre.getIsCompleted()!=null && glre.getIsCompleted().equals(Boolean.FALSE))
				{
					int indx = new Random().nextInt(6-0) + 0;
					String bgColor = i%2 == 0 ? "#E2F5D6" : "";
					JSONObject groupLoanTerm = new JSONObject();
					JSONObject jb = new JSONObject();
					
					Double totalPrincipalIncurred = glre.getTotalPrincipalIncurred();
    				totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPrincipalIncurred);
    				log.info(totalPrincipalIncurred);
    				Double totalPenaltiesIncurred = glre.getTotalPenaltiesIncurred();
    				totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPenaltiesIncurred);
    				log.info(totalPenaltiesIncurred);
    				Double totalInterestIncurred = glre.getTotalInterestIncurred();
    				totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalInterestIncurred);
    				log.info(totalInterestIncurred);
    				Double totalPrincipalRepaid = glre.getTotalPrincipalRepaid();
    				totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPrincipalRepaid);
    				log.info(totalPrincipalRepaid);
    				Double totalPenaltiesRepaid = glre.getTotalPenaltiesRepaid();
    				totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalPenaltiesRepaid);
    				log.info(totalPenaltiesRepaid);
    				Double totalInterestRepaid = glre.getTotalInterestRepaid();
    				totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				System.out.println(totalInterestRepaid);
    				log.info(totalInterestRepaid);
    				
    				Double amountIncurred = totalPrincipalIncurred + totalPenaltiesIncurred + totalInterestIncurred;
    				System.out.println(amountIncurred);
    				log.info(amountIncurred);
    				Double amountRepaid = totalPrincipalRepaid + totalPenaltiesRepaid + totalInterestRepaid;
    				System.out.println(amountRepaid);
    				log.info(amountRepaid);
    				Double amountToPay = amountIncurred - 
    						amountRepaid;
    				
    				
					jb.put("bgColor", bgColor);
					jb.put("iconBgColor", colors[3]);
					jb.put("iconTxt", "L");
					jb.put("amount", amountToPay);
					jb.put("name", glre.getGroupLoan().getContributionPackage().getPackageName());
					jb.put("paymentType", "Loan Repayment");
					jb.put("id", glre.getId());
					jb.put("groupMemberId", glre.getGroupLoan().getGroupMember().getId());
					jb.put("groupMember", glre.getGroupLoan().getGroupMember().getAddedCustomer().getFirstName() + " " + glre.getGroupLoan(). getGroupMember().getAddedCustomer().getLastName());
					jb.put("expectedPaymentDate", new SimpleDateFormat("yyyy MMM dd").format(glre.getExpectedRepaymentDate()));
					allGroupLoanExpectedRepayments.put(jb);
					i++;
				}
			}
			
			JSONArray allGroupMessages = new JSONArray();
			hql = "Select tp from GroupMessage tp where tp.deletedAt IS NULL AND tp.group.id = " + groupId + " AND tp.receiver IS NULL";
			Collection<GroupMessage> groupMessages = (Collection<GroupMessage>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupMessage> it8 = groupMessages.iterator();
			i = 0;
			while(it8.hasNext())
			{
				GroupMessage gm = it8.next();
				
				int indx = new Random().nextInt(6-0) + 0;
				String bgColor = i%2 == 0 ? "#E2F5D6" : "";
				JSONObject groupLoanTerm = new JSONObject();
				JSONObject jb = new JSONObject();
				jb.put("bgColor", bgColor);
				jb.put("iconBgColor", colors[indx]);
				jb.put("iconTxt", gm.getSender().getFirstName().substring(0, 1).toUpperCase());
				jb.put("details", gm.getDetails());
				jb.put("name", gm.getSender().getFirstName() + " " + gm.getSender().getLastName());
				jb.put("dateSent", new SimpleDateFormat("yyyy MMM dd").format(gm.getCreatedAt()));
				allGroupMessages.put(jb);
				i++;
				
			}
			
			
    			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Group listings found");
			jsonObject.put("expectedPayments", allGroupLoanExpectedRepayments.toString());
			jsonObject.put("groupLoans", allGroupLoans.toString());
			jsonObject.put("yourLoans", myGroupLoans_.toString());
			jsonObject.put("groupMembers", allGroupMembers.toString());
			jsonObject.put("activities", allGroupMessages.toString());
			jsonObject.put("payments", contributionsMade.toString());
			jsonObject.put("groupAdmins", groupAdmins.toString());
            return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			return jsonObject;
		}
	}
	
	
	
	
	public Response makeGoupMemberAdmin(String token, Long groupMemberId, String requestId, String ipAddress, Integer isMakeAdmin) 
    {
    	JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupMemberId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			System.out.println("verifyJ ==" + verify.toString());
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			hql = "Select tp from GroupMember tp where tp.id = " + groupMemberId + " AND tp.deletedAt IS NULL";
			GroupMember gm = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
			if(gm==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "Your selected group member could not be found. Please select a valid group member from this group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = "+ customer.getId()  +" AND tp.group.id = " + gm.getGroup().getId() + 
					" AND tp.isActive = 1 AND tp.isAdmin = 1 AND tp.deletedAt IS NULL";
    		GroupMember groupAdmin = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
    		if(groupAdmin==null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "Hey, you need to be an administrator of the group to manage administrators of this group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		if(isMakeAdmin==1)
    			gm.setIsAdmin(Boolean.TRUE);
    		else
    			gm.setIsAdmin(Boolean.FALSE);
    		
    		
    		gm.setUpdatedAt(new Date());
    		swpService.updateRecord(gm);
    		
    		String details = null;
    		if(isMakeAdmin==1)
    			details = gm.getAddedCustomer().getFirstName() + " was made a key official!";
    		else
    			details = gm.getAddedCustomer().getFirstName() + " is no longer a key official!";
    		
    		
    		GroupMessage gpm = new GroupMessage(details, customer, null, groupAdmin.getGroup());
    		swpService.createNewRecord(gpm);
    			
    		jsonObject.put("status", ERROR.GENERAL_OK);
    		jsonObject.put("groupMessage", gpm.getDetails());
    		if(isMakeAdmin==1)
    			jsonObject.put("message", "Selected member has been made an Administrator.");
    		else
    			jsonObject.put("message", "Selected member is no longer an Administrator.");
			
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
    }

	public Response createNewGroupRequest(Long groupId, User user, String requestId, String ipAddress, String token) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && groupId!=null && groupId!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			
			
			
			hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL";
			Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
			if(group==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "Your selected group could not be found. Please select a valid group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from ContributionPackage tp where tp.group.id = " + groupId + " AND tp.deletedAt IS NULL "
					+ "AND tp.endDate IS NULL";
			ContributionPackage contributionPackage = (ContributionPackage)this.swpService.getUniqueRecordByHQL(hql);
			if(contributionPackage==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "The village banking group is not yet active. Please wait for the group to be activated first before you can request to join the group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupMember tp where tp.group.id = " + groupId + " AND tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customer.getId() + "";
			GroupMember gm = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
			if(gm!=null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Check to confirm if you already are a member of this group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupJoinRequest tp where tp.requestByUserId = "+ user.getId()  +" AND tp.groupId = " + groupId + 
					" AND (tp.isApproved IS NULL OR tp.isApproved = 1) AND tp.deletedAt IS NULL";
			GroupJoinRequest groupJoinRequest = (GroupJoinRequest)this.swpService.getUniqueRecordByHQL(hql);
			if(groupJoinRequest!=null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "You already are a member of this group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
			groupJoinRequest = new GroupJoinRequest(user.getId(), groupId, customer.getFirstName() + " " + customer.getLastName(), 
					customer.getContactMobile(), null);
    		swpService.createNewRecord(groupJoinRequest);
    		
    			
			//JSONObject resp = getListOfGroups(customer);
    		JSONObject resp = new JSONObject();
			resp.put("status", ERROR.GENERAL_OK);
			resp.put("message", "Your request to join the group has been sent to the groups officials");
			
			
            return Response.status(200).entity(resp.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
	}
	
	

	public Response approveOrDisapproveJoinRequest(Long joinRequestId, User user, String requestId, String ipAddress,
			String token, Integer isApproved) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && joinRequestId!=null && isApproved!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			
			
			String hql = "Select tp from GroupJoinRequest tp where tp.id = " + joinRequestId + " AND tp.deletedAt IS NULL";
			GroupJoinRequest groupJoinRequest = (GroupJoinRequest)this.swpService.getUniqueRecordByHQL(hql);
			if(groupJoinRequest==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "Your selected request to join the group could not be found. Please select a valid request");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from ContributionPackage tp where tp.group.id = " + groupJoinRequest.getGroupId() + " AND tp.deletedAt IS NULL "
					+ "AND tp.endDate IS NULL";
			ContributionPackage contributionPackage = (ContributionPackage)this.swpService.getUniqueRecordByHQL(hql);
			if(contributionPackage==null)
			{
				jsonObject.put("status", ERROR.CONTRIBUTION_PACKAGE_NOT_FOUND);
				jsonObject.put("message", "The village banking group contribution settings has not been provided. Please provide the groups contribution settings before approving or disapproving this request");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			

			
			hql = "Select tp from Customer tp where tp.user.id = " + groupJoinRequest.getRequestByUserId();
			Customer customer1 = (Customer)swpService.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from GroupMember tp where tp.group.id = " + groupJoinRequest.getGroupId() + " AND tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customer1.getId() + "";
			GroupMember gm = (GroupMember)this.swpService.getUniqueRecordByHQL(hql);
			if(gm!=null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Check to confirm if you already are a member of this group");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			hql = "Select tp from GroupJoinRequest tp where tp.requestByUserId = "+ user.getId()  +" AND tp.groupId = " + groupJoinRequest.getGroupId() + 
					" AND (tp.isApproved IS NOT NULL) AND tp.deletedAt IS NULL";
			GroupJoinRequest groupJoinRequestCheck = (GroupJoinRequest)this.swpService.getUniqueRecordByHQL(hql);
			if(groupJoinRequestCheck!=null)
    		{
    			jsonObject.put("status", ERROR.GROUP_MEMBER_NOT_EXISTS);
				jsonObject.put("message", "This request has already been " + (groupJoinRequestCheck.getIsApproved()!=null && groupJoinRequestCheck.getIsApproved().equals(Boolean.TRUE) ? "approved" : "disapproved" ));
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
			hql = "Select tp from Group tp where tp.id = "+ groupJoinRequest.getGroupId() + " AND tp.deletedAt IS NULL";
			Group group = (Group)this.swpService.getUniqueRecordByHQL(hql);
    		
			groupJoinRequest.setIsApproved(isApproved==1 ? true : false);
    		swpService.updateRecord(groupJoinRequest);
    		
    		
    		
    		
    		gm = new GroupMember();
    		gm.setAddedByUser(user);
    		gm.setAddedCustomer(customer1);
    		gm.setCreatedAt(new Date());
    		gm.setUpdatedAt(new Date());
    		gm.setCurrentBalance(0.00);
    		gm.setCurrentContributions(0.00);
    		gm.setGroup(group);
    		gm.setIsActive(true);
    		gm.setIsAdmin(false);
    		this.swpService.createNewRecord(gm);
    		
			Double amount = contributionPackage.getContributionAmount();
			Integer period = contributionPackage.getContributionPeriod();
			PeriodType periodType = contributionPackage.getContributionPeriodType();
			ProbasePayCurrency ProbasePayCurrency = contributionPackage.getCurrency();
			Integer numberOfPayments = contributionPackage.getNumberOfPayments();
			String packageName = contributionPackage.getPackageName();
			Double penaltyApplicable = contributionPackage.getPenaltyApplicable();
			PenaltyApplicableType penaltyApplicableType = contributionPackage.getPenaltyApplicableType();
			Date today = new Date();
			Calendar cal = Calendar.getInstance();
			Integer interval = period;
			
			for(int i=1; i<(numberOfPayments+1); i++)
			{
				cal.setTime(today);
				System.out.println("interval.." + interval);
				System.out.println("current date.." + cal.getTime());
				if(periodType.equals(PeriodType.DAY.name()))
					cal.add(Calendar.DATE, 1);
				if(periodType.equals(PeriodType.MONTH.name()))
					cal.add(Calendar.MONTH, 1);
				if(periodType.equals(PeriodType.WEEK.name()))
					cal.add(Calendar.DATE, (1*7));
				if(periodType.equals(PeriodType.YEAR.name()))
					cal.add(Calendar.YEAR, 1);

				Date dateExpected = cal.getTime();
				System.out.println("dateExpected.." + dateExpected);
				GroupPaymentsExpected groupPaymentExpected = new GroupPaymentsExpected(contributionPackage, gm, 
						amount, dateExpected, 0.00, amount, 0.00, 0.00, Boolean.FALSE, (i), group.getId());
				swpService.createNewRecord(groupPaymentExpected);
				interval = interval + period;
			}
			
    			
			JSONObject resp = new JSONObject();
			resp.put("status", ERROR.GENERAL_OK);
			if(isApproved==1)
				resp.put("message", "Hey, the request has been approved. The group member has been notified of their " +
						"need to contribute at the specified periodical interval");
			else
				resp.put("message", "Hey, the request has been disapproved. The group member has been notified of the disapproval");
			
			resp.put("isActive", isApproved!=null && isApproved==1 ? true : false);
			
			
			
    		
    		String smsMessage = "Your request to join Bevura Village Banking group - " + group.getName() + " - ";
    		if(isApproved==1)
    		{
    			smsMessage = smsMessage + " has been approved by the groups officials. Please log in to your Bevura mobile app to access the group";
    		}
    		else
    		{
    			smsMessage = smsMessage + " has been disapproved by the groups officials. You can request to join other village banking groups";
    		}
			//SMSMesage smsMsg1 = new SMSMesage(contactMobile, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
			//swpService.createNewRecord(smsMsg1);


			SmsSender smsSender = new SmsSender(swpService, smsMessage, groupJoinRequest.getRequestByUserMobileNumber());
			new Thread(smsSender).start();
    			
			resp.put("status", ERROR.GENERAL_OK);
			resp.put("message", "The request to join your Bevura Village Banking group has been " + (isApproved!=null && isApproved==1 ? "approved" : "disapproved"));
			
			
            return Response.status(200).entity(resp.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
	}

	public JSONObject getGroupSummary(String token, String requestId, String ipAddress, Long groupId, Customer customer, SwpService swpService) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("getListOfGroups");
			log.info("getListOfGroups");
			hql = "Select tp from GroupMember tp where tp.deletedAt IS NULL AND tp.group.id = " + groupId + " AND tp.isActive = 1";
			Collection<GroupMember> groupMembers = (Collection<GroupMember>)swpService.getAllRecordsByHQL(hql);
			int groupMemberCount = groupMembers.size();
			
			Iterator<GroupMember> itMem = groupMembers.iterator();
			JSONArray jArray = new JSONArray();
			while(itMem.hasNext())
			{
				GroupMember gm = itMem.next();
				if(gm.getIsAdmin().equals(Boolean.TRUE))
					jArray.put(gm.getAddedCustomer().getUser().getId());
			}
			
			hql = "Select tp from GroupPaymentsExpected tp where tp.deletedAt IS NULL AND tp.groupId = " + groupId;
			Collection<GroupPaymentsExpected> groupPaymentsExpected = (Collection<GroupPaymentsExpected>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupPaymentsExpected> it0 = groupPaymentsExpected.iterator();
			double totalAmountPaid = 0.00;
			double totalAmountExpected = 0.00;
			while(it0.hasNext())
			{
				GroupPaymentsExpected gc = it0.next();
				totalAmountPaid = totalAmountPaid + gc.getAmountPaid() + gc.getPenaltiesPaid();
				totalAmountExpected = totalAmountExpected + gc.getAmount();
			}
			
			
			
			hql = "Select tp from GroupLoan tp where tp.deletedAt IS NULL AND tp.groupId = " + groupId;
			Collection<GroupLoan> groupLoans = (Collection<GroupLoan>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoan> it1 = groupLoans.iterator();
			double totalOutstandingLoanAmount = 0.00;
			double totalLoanPayments = 0.00;
			double totalLoans = 0.00;
			while(it1.hasNext())
			{
				GroupLoan gc = it1.next();
				totalOutstandingLoanAmount = totalOutstandingLoanAmount + gc.getOutstandingBalance();
				totalLoanPayments = totalLoanPayments + gc.getTotalPrincipalRepaid() +  gc.getTotalInterestRepaid() + gc.getTotalPenaltiesRepaid();
				totalLoans = totalLoans + gc.getPrincipal();
			}
			
			
			hql = "Select tp from GroupLoanRepayment tp where tp.deletedAt IS NULL AND tp.groupId = " + groupId;
			Collection<GroupLoanRepayment> groupLoanRepayments = (Collection<GroupLoanRepayment>)swpService.getAllRecordsByHQL(hql);
			Iterator<GroupLoanRepayment> it2 = groupLoanRepayments.iterator();
			double totalInterest = 0.00;
			while(it2.hasNext())
			{
				GroupLoanRepayment gc = it2.next();
				totalInterest = totalInterest + gc.getInterestAmountRepaid();
			}
			
			

			
			
			hql = "Select tp.* from group_messages tp where tp.deletedAt IS NULL AND tp.group_id = " + groupId + " AND tp.receiver_id IS NULL ORDER BY tp.createdAt ASC";
			List<Map<String, Object>> groupMessages = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			
			
			jsonObject.put("groupMessages", groupMessages);	
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Group summary received");
			jsonObject.put("totalOutstandingLoanAmount", totalOutstandingLoanAmount);
			jsonObject.put("totalLoanPayments", totalLoanPayments);
			jsonObject.put("totalAmountPaid", totalAmountPaid);
			jsonObject.put("totalAmountExpected", totalAmountExpected);
			jsonObject.put("totalInterest", totalInterest);
			jsonObject.put("totalLoans", totalLoans);
			jsonObject.put("groupAdmin", jArray);
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return jsonObject;
		}
	}

	public Response villageBankingUpdateContributionSettings(String token, Customer customer, Double contributionAmount, Integer howManyTimes,
			String periodType, String requestId, Long groupId, String ipAddress, SwpService swpService) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			hql = "Select tp from GroupMember tp where tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customer.getId() + 
					" AND tp.group.id = " + groupId + 
					" AND tp.isAdmin = 1 AND tp.deletedAt IS NULL";
			GroupMember groupMember = (GroupMember)swpService.getUniqueRecordByHQL(hql);
			if(groupMember==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "You do not have the appropriate rights to update this groups contribution settings");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from ContributionPackage tp where tp.group.id = " + groupId;
			Collection<ContributionPackage> contributionPackages = (Collection<ContributionPackage>)swpService.getAllRecordsByHQL(hql);
			

			hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.deleted_at IS NULL";
			Account account = (Account)swpService.getUniqueRecordByHQL(hql);
			

			PeriodType pt = null;
			try
    		{
    			pt = PeriodType.valueOf(periodType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Hey, you need to specify if you want contributions to be every week, month or in days");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
			String packageName = groupMember.getGroup().getName() + " - Contribution Package " + (contributionPackages==null ? 1 : (contributionPackages.size() + 1));
			ContributionPackage contributionPackage = new ContributionPackage(customer.getUser(), packageName, contributionAmount, howManyTimes, 
    				pt, null, null, groupMember.getGroup(), account.getProbasePayCurrency(), null, howManyTimes);
    		contributionPackage = (ContributionPackage)swpService.createNewRecord(contributionPackage);
    		
    		
    		DecimalFormat df = new DecimalFormat("0.00");
    		
    		hql = "Select tp from Group tp where tp.id = " + groupId;
    		Group group = (Group)swpService.getUniqueRecordByHQL(hql);
    		group.setIsContributionPackageSet(true);
    		group.setCurrentContributionAmount(account.getProbasePayCurrency().name() + "" + df.format(contributionAmount));
    		String intervl = "";
    		switch(pt)
    		{
    			case DAY:
    				intervl = "per day";
    				break;
    			case MONTH:
    				intervl = "per month";
    				break;
    			case WEEK:
    				intervl = "per week";
    				break;
    			case YEAR:
    				intervl = "per year";
    				break;
				default:
    				intervl = "per year";
    				break;
					
    				
    		}
    		group.setCurrentContributionInterval(intervl);
    		swpService.updateRecord(group);
    		
    		
    		
    		
			Double amount = contributionPackage.getContributionAmount();
			Integer period = contributionPackage.getContributionPeriod();
			ProbasePayCurrency ProbasePayCurrency = contributionPackage.getCurrency();
			Integer numberOfPayments = contributionPackage.getNumberOfPayments();
			Double penaltyApplicable = contributionPackage.getPenaltyApplicable();
			PenaltyApplicableType penaltyApplicableType = contributionPackage.getPenaltyApplicableType();
			Date today = new Date();
			Date dateExpected = today;
			Calendar cal = Calendar.getInstance();
			Integer interval = period;
			
			/*for(int i=1; i<(numberOfPayments+1); i++)
			{
				cal.setTime(dateExpected);
				System.out.println("interval.." + interval);
				System.out.println("current date.." + cal.getTime());
				if(periodType.equals(PeriodType.DAY))
					cal.add(Calendar.DATE, i);
				if(periodType.equals(PeriodType.MONTH))
					cal.add(Calendar.MONTH, i);
				if(periodType.equals(PeriodType.WEEK))
					cal.add(Calendar.DATE, (i*7));
				if(periodType.equals(PeriodType.YEAR))
					cal.add(Calendar.YEAR, i);
				
				

				dateExpected = cal.getTime();
				System.out.println("dateExpected.." + dateExpected);
				GroupPaymentsExpected groupPaymentExpected = new GroupPaymentsExpected(contributionPackage, groupMember, 
						amount, dateExpected, 0.00, amount, 0.00, 0.00, Boolean.FALSE, (i+1), contributionPackage.getGroup().getId());
				swpService.createNewRecord(groupPaymentExpected);
				//interval = interval + period;
			}*/
			DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
			for(int i=1; i<(numberOfPayments+1); i++)
			{
				System.out.println("current date1111.." + cal.getTime());
				cal.setTime(dateExpected);
				System.out.println("interval.." + interval);
				System.out.println("current date.." + cal.getTime());
				if(pt.equals(PeriodType.DAY.name()))
					cal.add(Calendar.DATE, 1);
				if(pt.equals(PeriodType.MONTH.name()))
					cal.add(Calendar.MONTH, 1);
				if(pt.equals(PeriodType.WEEK.name()))
					cal.add(Calendar.DATE, (1*7));
				if(pt.equals(PeriodType.YEAR.name()))
					cal.add(Calendar.YEAR, 1);
				
				

				dateExpected = cal.getTime();
				System.out.println("dateExpected.." + df1.format(dateExpected));
				System.out.println("dateExpected.." + dateExpected);
				GroupPaymentsExpected groupPaymentExpected = new GroupPaymentsExpected(contributionPackage, groupMember, 
						amount, dateExpected, 0.00, amount, 0.00, 0.00, Boolean.FALSE, (i), contributionPackage.getGroup().getId());
				swpService.createNewRecord(groupPaymentExpected);
				
			}
    		
    		
    		JSONObject allGroups = groupFunction.listGroups( token, requestId, ipAddress);
    			
    		jsonObject.put("group", allGroups);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your new group contibution package has been created successfully. All members will be notified of their " +
					"need to contribute at the specified periodical interval");
			
            return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	
	
	

	public Response villageBankingUpdateLoanSettings(String token, Customer customer, Double loanRate, String loanRateType,
			Double minLoanAmount, Double maxLoanAmount, Integer maxLoanPeriod, String periodType, 
			String requestId, Long groupId, String ipAddress, SwpService swpService) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			hql = "Select tp from GroupMember tp where tp.deletedAt IS NULL AND tp.addedCustomer.id = " + customer.getId() + 
					" AND tp.group.id = " + groupId + 
					" AND tp.isAdmin = 1 AND tp.deletedAt IS NULL";
			GroupMember groupMember = (GroupMember)swpService.getUniqueRecordByHQL(hql);
			if(groupMember==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "You do not have the appropriate rights to update this groups contribution settings");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from ContributionPackage tp where tp.group.id = " + groupId;
			Collection<ContributionPackage> contributionPackages = (Collection<ContributionPackage>)swpService.getAllRecordsByHQL(hql);
			ContributionPackage contributionPackage = null;
			if(contributionPackages!=null && contributionPackages.size()>0)
			{
				contributionPackage = contributionPackages.iterator().next();
			}
			

			hql = "Select tp from Account tp where tp.customer.id = " + customer.getId() + " AND tp.deleted_at IS NULL";
			Account account = (Account)swpService.getUniqueRecordByHQL(hql);
			

			TenorType repaymentTenorType = null;
			PeriodType interestType = null;
			try
    		{
				interestType = PeriodType.valueOf(loanRateType);
    			repaymentTenorType = TenorType.valueOf(periodType);
    		}
    		catch(IllegalArgumentException | NullPointerException e)
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Hey, you need to specify valid rate type and period type");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
			
			String packageName = groupMember.getGroup().getName() + " - Contribution Package " + (contributionPackages==null ? 1 : (contributionPackages.size() + 1));
			GroupLoanTerms groupLoanTerms = new GroupLoanTerms(0.00, contributionPackage, maxLoanAmount, minLoanAmount, 0, maxLoanPeriod, interestType, loanRate, repaymentTenorType, 
					RepaymentStrategy.PRINCIPAL_INTEREST_PENALTY, 0.00, null
				);
    		swpService.createNewRecord(groupLoanTerms);
    		
    		hql = "Select tp from Group tp where tp.id = " + groupId;
    		Group group = (Group)swpService.getUniqueRecordByHQL(hql);
    		group.setIsLoanPackageSet(true);
    		group.setCurrentLoanInterestRate(loanRate + "");
    		
    		String intervl = "";
    		switch(interestType)
    		{
    			case DAY:
    				intervl = "/day";
    				break;
    			case MONTH:
    				intervl = "/month";
    				break;
    			case WEEK:
    				intervl = "/week";
    				break;
    			case YEAR:
    				intervl = "/annum";
    				break;
				default:
    				intervl = "/annum";
    				break;
					
    				
    		}
    		group.setCurrentLoanInterestRateType(intervl);
    		group.setCurrentMaximumLoan(maxLoanAmount + "");
    		swpService.updateRecord(group);
    		
    		JSONObject allGroups = groupFunction.listGroups( token, requestId, ipAddress);
    			
    		jsonObject.put("group", allGroups);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Hey, your new group loan settings has been updated successfully");
			
            return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}
	
	
	

	public JSONObject getVillageBankingJoinRequestList(String token, String requestId, String ipAddress, Long groupId,
			Customer customer, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("GroupJoinRequest");
			
			
			hql = "Select u.passportImage, tp.* from group_join_request tp, users u where tp.requestByUserId = u.id AND tp.deletedAt IS NULL AND tp.groupId = " + groupId + " AND tp.isApproved IS NULL";
			List<Map<String, Object>> groupJoinRequests = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			
			
			jsonObject.put("groupJoinRequests", groupJoinRequests);	
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Requests to join group received");
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return jsonObject;
		}
	}

	public Response getGroupContributions(String token, Long groupId, String type, Long groupMemberId, String requestId,
			String ipAddress, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("GroupJoinRequest");
			

			hql = "Select ac.probasePayCurrency, c.user_id, u.firstName, u.mobileNo, u.lastName, tp.* from group_payments_expected tp, group_members gm, customers c, users u, accounts ac where "
					+ "tp.groupMember_id = gm.id AND gm.addedCustomer_id = c.id AND c.user_id = u.id AND ac.customer_id = c.id AND tp.deletedAt IS NULL AND "
					+ "tp.groupId = " + groupId;
			List<Map<String, Object>> group_payments_expected = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("groupContributions", group_payments_expected);	
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Group contributions found");
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}

	public Response getVillageBankingLoanSummary(String token, Customer customer, String requestId, Long groupId,
			String ipAddress, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("GroupJoinRequest");
			
			hql = "Select tp.* from group_loan_terms tp, contribution_packages cp, groups gp where tp.contributionPackage_id = cp.id AND "
					+ "cp.group_id = gp.id AND tp.deletedAt IS NULL AND gp.id = " + groupId;
			List<Map<String, Object>> groupLoanTerms = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
    		if(groupLoanTerms!=null && groupLoanTerms.size()>0)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				jsonObject.put("message", "Your group does not loan money to its group members. If you want this feature, kindly send a message to your groups officials");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Map<String, Object> groupLoanTerm = groupLoanTerms.get(0);
    		
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = " + customer.getId() + " AND tp.group.id = "+ groupId +" AND tp.deletedAt IS NULL";
			GroupMember groupMember = (GroupMember)swpService2.getUniqueRecordByHQL(hql);
			

			hql = "Select sum(tp.outstandingBalance) as totalOutstandingBalance from group_loans tp where tp.groupId = " + groupId + " AND tp.deletedAt IS NULL AND "
					+ "tp.groupMember_id = " + groupMember.getId() + " AND tp.status IN (" + GroupLoanStatus.ACTIVE.ordinal() + ", " + GroupLoanStatus.APPLYING.ordinal() + ", " + 
					GroupLoanStatus.DEFAULTED.ordinal() + ", " + GroupLoanStatus.LATE_30.ordinal() + ", " + GroupLoanStatus.LATE_60.ordinal() + ", " + GroupLoanStatus.LATE_90.ordinal() + ") "
							+ "GROUP BY tp.status ORDER BY tp.createdAt ASC";
			List<Map<String, Object>> currentlyRunningLoans = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			
			
			hql = "Select tp.* from group_loans tp where tp.groupId = " + groupId + " AND tp.deletedAt IS NULL AND "
					+ "tp.groupMember_id = " + groupMember.getId() + " AND tp.status IN (" + GroupLoanStatus.ACTIVE.ordinal() + ", " + GroupLoanStatus.APPLYING.ordinal() + ", " + 
					GroupLoanStatus.DEFAULTED.ordinal() + ", " + GroupLoanStatus.LATE_30.ordinal() + ", " + GroupLoanStatus.LATE_60.ordinal() + ", " + GroupLoanStatus.LATE_90.ordinal() + ") "
							+ " ORDER BY tp.createdAt ASC";
			List<Map<String, Object>> lastLoans = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			
			jsonObject.put("currentlyRunningLoans", currentlyRunningLoans);	
			jsonObject.put("lastLoans", lastLoans);	
			jsonObject.put("groupLoanTerm", groupLoanTerm);
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Currently running loans Found");
			
			return Response.status(200).entity(jsonObject.toString()).build();
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return Response.status(200).entity(jsonObject.toString()).build();
		}
	}

	public JSONObject getVillageBankingLoans(String token, String groupId, Customer customer, String requestId, String ipAddress, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("GroupJoinRequest");
			
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.id = " + customer.getId() + " AND "
					+ "tp.group.id = "+ groupId +" AND tp.isActive = 1 AND tp.deletedAt IS NULL";
			GroupMember gm = (GroupMember)swpService2.getUniqueRecordByHQL(hql);
			
			if(gm==null)
			{
				jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				jsonObject.put("message", "You are not a member of this group so you can not view loans in this group");
				
	            return jsonObject;
			}
			
			hql = "Select tp.*, u.id as userId, u.firstName, u.lastName, u.mobileNo from group_loans tp, group_members gm, customers c, users u where tp.groupMember_id = gm.id AND "
					+ "gm.addedCustomer_id = c.id AND c.user_id = u.id AND tp.deletedAt IS NULL AND gm.group_id = " + groupId;
			List<Map<String, Object>> groupLoans = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
    		if(groupLoans!=null && groupLoans.size()>0)
    		{
    			
    		}
    		else
    		{
    			//jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				//jsonObject.put("message", "Your group does not loan money to its group members. If you want this feature, kindly send a message to your groups officials");
				
	            //return jsonObject;
    		}
    		
    		hql = "Select tp from ContributionPackage tp where tp.group.id = " + groupId + " AND tp.deletedAt  IS NULL";
    		ContributionPackage cp = (ContributionPackage)swpService2.getUniqueRecordByHQL(hql);
			
			jsonObject.put("groupLoans", groupLoans);	
			jsonObject.put("currency", cp.getCurrency().name());
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Currently running loans Found");
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return jsonObject;
		}
	}

	public JSONObject readGroupMessage(Long notificationId, User user, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			String hql = "";
			String groupIds = "";
			GroupMember customerGroupMember = null;
			JSONArray jsArrMyAdmins = new JSONArray();
			JSONObject groupMemberCountJS = new JSONObject();
			JSONObject customerGroupMemberJS = new JSONObject();
			System.out.println("GroupJoinRequest");
			
			
			hql = "Select tp from Notification tp where tp.id = " + notificationId + " AND "
					+ "tp.userId = "+ user.getId() +" AND tp.deletedAt IS NULL";
			Notification notification = (Notification)swpService2.getUniqueRecordByHQL(hql);
			
			if(notification==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Notification could not be found.");
				
	            return jsonObject;
			}
			
			notification.setIsRead(true);
			swpService2.updateRecord(notification);
			
			hql = "Select tp.* from notifications tp where tp.userId = " + user.getId() + " AND tp.isRead = 0";
			List<Map<String, Object>> notifications = (List<Map<String, Object>>)swpService.getQueryBySQLResultsWithKeys(hql);
			jsonObject.put("notifications", notifications==null ? null : notifications);
			
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Notification read");
			jsonObject.put("notifications", ERROR.GENERAL_OK);
			
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return jsonObject;
		}
	}

	public JSONObject payGroupContributions(String token, String groupId, Customer customer, Double amount1,
			String sourceType, String debitSource, String requestId, String ipAddress, Merchant merchant, Device device, 
			SwpService swpService2, javax.ws.rs.core.HttpHeaders httpHeaders, HttpServletRequest requestContext, Application app, String bankKey, 
			Channel channel, Acquirer acquirer, String orderRef) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		try
		{
			DecimalFormat df = new DecimalFormat("0.00");
			
			String hql = "Select tp from Group tp where tp.id = " + groupId + " AND tp.deletedAt IS NULL";
			Group group = (Group)swpService2.getUniqueRecordByHQL(hql);
			
			Account wallet = null;
			ECard card = null;
			ProbasePayCurrency currency = null;
			String accountNo = null;
			if(sourceType!=null && sourceType.equals("CARD"))
			{
				hql = "Select tp from ECard tp where tp.trackingNumber = '"+debitSource+"' AND tp.status = "+ CardStatus.ACTIVE.ordinal() +" AND tp.deleted_at IS NULL "
						+ "AND tp.isLive = " + device.getSwitchToLive();
				card = (ECard)swpService2.getUniqueRecordByHQL(hql);
			}
			else if(sourceType!=null && sourceType.equals("WALLET"))
			{
				hql = "Select tp from Account tp where tp.accountIdentifier = '"+debitSource+"' AND tp.status = "+ AccountStatus.ACTIVE.ordinal() +" AND tp.deleted_at IS NULL"
						+ "AND tp.isLive = " + device.getSwitchToLive();
				wallet = (Account)swpService2.getUniqueRecordByHQL(hql);
			}
			
			if(card==null && wallet==null)
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "You must specify the card or wallet you are paying from.");
				
	            return jsonObject;
			}
			
			if(sourceType!=null && sourceType.equals("CARD"))
			{
				currency = card.getAccount().getProbasePayCurrency();
				accountNo = card.getAccount().getAccountIdentifier();
			}
			else if(sourceType!=null && sourceType.equals("WALLET"))
			{
				currency = wallet.getProbasePayCurrency();
				accountNo = wallet.getAccountIdentifier();
			}
			
			
			hql = "Select tp from GroupPaymentsExpected tp where tp.groupMember.group.id = '"+group.getId()+"' "
					+ "AND tp.groupMember.addedCustomer.id = "+ customer.getId() +" AND "
					+ "tp.isPaid = 0 AND tp.deletedAt IS NULL";
			Collection<GroupPaymentsExpected> groupPaymentsExpected = (Collection<GroupPaymentsExpected>)swpService2.getAllRecordsByHQL(hql);
			Double totalOutstanding = 0.00;
			Double balance = null;
			Double walletMinimumBalance = 0.00;
			if(groupPaymentsExpected!=null && groupPaymentsExpected.size()>0)
			{
				Iterator<GroupPaymentsExpected> gpeIter = groupPaymentsExpected.iterator();
				while(gpeIter.hasNext())
				{
					GroupPaymentsExpected gpe = gpeIter.next();
					Double totalPenalties = gpe.getTotalPenalties()==null ? 0 : gpe.getTotalPenalties();
					Double penaltiesPaid = gpe.getPenaltiesPaid()==null ? 0 : gpe.getPenaltiesPaid();
					totalOutstanding = totalOutstanding + (gpe.getOutstandingBalance() + (totalPenalties - penaltiesPaid));
				}
				
				
				if(totalOutstanding<=0.00)
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "You do not have any outstanding group contributions to make at the moment");
					
		            return jsonObject;
				}
				
				if(sourceType.equals("WALLET"))
				{
					AccountServicesV2 as_ = new AccountServicesV2();
					Response balanceDetResp = as_.getAccountBalance(
							httpHeaders,
							requestContext,
							debitSource, 
							token, 
							merchant.getMerchantCode(), 
							device.getDeviceCode());
					String balanceDetStr = (String)balanceDetResp.getEntity();
					System.out.println("balanceDet1.....");
					System.out.println(balanceDetStr);
					if(balanceDetStr!=null)
					{
						JSONObject balanceDet = new JSONObject(balanceDetStr);
						System.out.println("balanceDet.....");
						System.out.println(balanceDet.toString());
						 
						if(balanceDet.has("status") && balanceDet.getInt("status")==ERROR.CUSTOMER_CREATE_SUCCESS)
						{
							Double currentbalance = balanceDet.getDouble("currentBalance");
							Double availablebalance = balanceDet.getDouble("availableBalance");
							System.out.println("totalCurrentBalance....." + currentbalance);
							System.out.println("availablebalance....." + availablebalance);
							
							balance = availablebalance;
							walletMinimumBalance = wallet.getAccountScheme().getMinimumBalance();
						}
					}
				}
				else if(sourceType.equals("CARD"))
				{
					balance = card.getCardBalance();
					walletMinimumBalance = card.getCardScheme().getMinimumBalance();
				}
				
				
				if(balance==null || (balance!=null && balance==0))
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "Insufficient balance. Please add funds to your wallet to make this payment. Minimum amount you are required to have to make this payment is " + currency.name() + df.format(amount1+ walletMinimumBalance));

					
		            return jsonObject;
				}
				
				
				if(balance<(amount1 + walletMinimumBalance))
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "Insufficient balance. Please add funds to your wallet to make this payment. Minimum amount you are required to have to make this payment is " + currency.name() + df.format(amount1+ walletMinimumBalance));

					
		            return jsonObject;
				}
				
				Double totalToPay = 0.00;
				List<GroupPaymentsExpected> gpeToPay = new ArrayList();
				List<JSONObject> gpeToPayPartialOutstanding = new ArrayList();
				List<JSONObject> gpeToPayPartialPenalties = new ArrayList();
				Double amount2 = amount1;
				gpeIter = groupPaymentsExpected.iterator();

				log.info(amount1);
				while(gpeIter.hasNext())
				{
					GroupPaymentsExpected gpe = gpeIter.next();
					Double totalPenalties = gpe.getTotalPenalties()==null ? 0 : gpe.getTotalPenalties();
					Double penaltiesPaid = gpe.getPenaltiesPaid()==null ? 0 : gpe.getPenaltiesPaid();
					Double outstanding = (gpe.getOutstandingBalance() + (totalPenalties - penaltiesPaid));
					
					
					if(amount2>0)
					{
						log.info(outstanding);
						log.info(totalToPay);
						log.info((totalToPay + outstanding));
						log.info(balance);
						log.info((balance - walletMinimumBalance));
						log.info(amount2);
						if((totalToPay + outstanding)<=(balance - (walletMinimumBalance + totalToPay)) && ((totalToPay + outstanding)<=amount2))
						{
							log.info("Put in gpeToPay");
							totalToPay = totalToPay + outstanding;
							gpeToPay.add(gpe);
							amount2 = amount2 - outstanding;
						}
						else
						{
							
							if(gpe.getOutstandingBalance()<=amount2)
							{
								JSONObject jb = new JSONObject();
								jb.put("contributionAmount", gpe.getOutstandingBalance());
								jb.put("groupPaymentsExpected", gpe);
								gpeToPayPartialOutstanding.add(jb);
								amount2 = amount2 - gpe.getOutstandingBalance();
							}
							else if(gpe.getOutstandingBalance()>amount2)
							{
								JSONObject jb = new JSONObject();
								jb.put("contributionAmount", amount2);
								jb.put("groupPaymentsExpected", gpe);
								gpeToPayPartialOutstanding.add(jb);
								amount2 = 0.00;
							}
							else
							{
								if((totalPenalties - penaltiesPaid)<=amount2)
								{
									JSONObject jb = new JSONObject();
									jb.put("penaltyAmount", (totalPenalties - penaltiesPaid));
									jb.put("groupPaymentsExpected", gpe);
									gpeToPayPartialPenalties.add(jb);
									amount2 = amount2 - (totalPenalties - penaltiesPaid);
								}
								else if((totalPenalties - penaltiesPaid)>amount2)
								{
									JSONObject jb = new JSONObject();
									jb.put("penaltyAmount", amount2);
									jb.put("groupPaymentsExpected", gpe);
									gpeToPayPartialPenalties.add(jb);
									amount2 = 0.00;
								}
							}
						}
					}
				}
				
				

				totalToPay = 0.00;
				if((gpeToPay.size() + gpeToPayPartialOutstanding.size() + gpeToPayPartialPenalties.size())>0)
				{
					log.info("gpeToPay..." + gpeToPay.size());
					Iterator<GroupPaymentsExpected> gpeToPayIter = gpeToPay.iterator();
					while(gpeToPayIter.hasNext())
					{
						GroupPaymentsExpected gpe = gpeToPayIter.next();
						totalToPay = totalToPay + gpe.getOutstandingBalance() + (gpe.getTotalPenalties() - gpe.getPenaltiesPaid());
					}
					
					

					Iterator<JSONObject> jbIter = gpeToPayPartialOutstanding.iterator();
					while(jbIter.hasNext())
					{
						JSONObject gpe = jbIter.next();
						Double outstandingAmountToPay = gpe.getDouble("contributionAmount");
						totalToPay = totalToPay + outstandingAmountToPay;
					}
					
					

					jbIter = gpeToPayPartialPenalties.iterator();
					while(jbIter.hasNext())
					{
						JSONObject gpe = jbIter.next();
						Double penaltyAmountToPay = gpe.getDouble("penaltyAmount");
						totalToPay = totalToPay + penaltyAmountToPay;
					}

					log.info("totalToPay..." + totalToPay);
					
					String zauthKey = null;
					if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(true))
						zauthKey = device.getZicbAuthKey();
					else
						zauthKey = device.getZicbDemoAuthKey();
					
						
					//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
					
					if(totalToPay>app.getAllSettings().getDouble("minimumtransactionamountweb") && totalToPay<app.getAllSettings().getDouble("maximumtransactionamountweb"))
					{
						String accountData = UtilityHelper.encryptData(accountNo, bankKey);
						System.out.println("bankKey..." + bankKey);
						System.out.println("accountNo..." + accountNo);
						System.out.println("accountData..." + accountData);
						String narration = "GROUPCONTRIBUTION~" + amount1 + "~" + totalToPay + "~" + group.getName() + "~" + customer.getUser().getMobileNo();
						PaymentServicesV2 paymentServices = new PaymentServicesV2();
						
						String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
						String bankPaymentReference = null;
						Long customerId = customer.getId();
						Boolean creditAccountTrue = null;
						Boolean creditCardTrue = null;
						String rpin = null;
						Channel ch = channel;
						Date transactionDate = new Date();
						ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
						String payerName = customer.getFirstName() + " " + customer.getLastName();
						String payerEmail = customer.getContactEmail();
						String payerMobile = customer.getContactMobile();
						TransactionStatus status = TransactionStatus.PENDING;
						ProbasePayCurrency probasePayCurrency = currency;
						String transactionCode = null;
						Boolean creditPoolAccountTrue = false;
						String messageRequest = null;
						String messageResponse = null;
						Double fixedCharge = 0.00;
						Double transactionCharge = 0.00;
						Double transactionPercentage = 0.00;
						Double schemeTransactionCharge = 0.00;
						Double schemeTransactionPercentage = 0.00;
						String responseCode = null;
						String oTP = null;
						String oTPRef = null;
						String merchantBank = null;
						String merchantAccount = null; 
						Long transactingBankId = acquirer.getBank().getId();
						Long receipientTransactingBankId = null;
						Integer accessCode = null;
						Long sourceEntityId = null;
						Long receipientEntityId = null;
						Channel receipientChannel = ch;
						String transactionDetail = narration;
						Double closingBalance = null;
						Double totalCreditSum = null;
						Double totalDebitSum = null;
						Long paidInByBankUserAccountId = null;
						String customData = null;
						String responseData = null;
						Long adjustedTransactionId = null;
						Long acquirerId = acquirer.getId();
						String merchantName = merchant.getMerchantName();
						String merchantCode = merchant.getMerchantCode();
						String deviceCode = device.getDeviceCode();
						Long merchantId_ = merchant.getId();
						Boolean debitAccountTrue = null;
						Boolean debitCardTrue = null;
						Long creditAccountId = null;
						Long creditCardId = null;
						Long debitAccountId = null;
						Long debitCardId = null;
						String otp= null;
						Account acct = null;
						if(sourceType.equals("WALLET"))
						{
							
							creditAccountTrue = false;
							creditCardTrue = false;
							serviceType = ServiceType.DEBIT_WALLET;
							sourceEntityId = wallet.getId();
							
							debitAccountTrue = true;
							debitCardTrue = false;
							creditAccountId = null;
							creditCardId = null;
							debitAccountId = wallet.getId();
							debitCardId = null;
							acct = wallet;
						}
						else if(sourceType.equals("CARD"))
						{
							creditAccountTrue = false;
							creditCardTrue = false;
							serviceType = ServiceType.DEBIT_CARD;
							sourceEntityId = card.getId();
							
							debitAccountTrue = false;
							debitCardTrue = true;
							creditAccountId = null;
							creditCardId = null;
							debitAccountId = null;
							debitCardId = card.getId();
							acct = card.getAccount();
						}
						
						
						Double totalCharges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
						Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
								customerId, creditAccountTrue, creditCardTrue,
								orderRef, rpin, ch,
								transactionDate, serviceType, payerName,
								payerEmail, payerMobile, status,
								probasePayCurrency, transactionCode,
								acct, card, device,
								creditPoolAccountTrue, messageRequest,
								messageResponse, fixedCharge,
								transactionCharge, transactionPercentage,
								schemeTransactionCharge, schemeTransactionPercentage,
								totalToPay, responseCode, oTP, oTPRef,
								merchantId_, merchantName, merchantCode,
								merchantBank, merchantAccount, 
								transactingBankId, receipientTransactingBankId,
								accessCode, sourceEntityId, receipientEntityId,
								receipientChannel, transactionDetail,
								closingBalance, totalCreditSum, totalDebitSum,
								paidInByBankUserAccountId, customData,
								responseData, adjustedTransactionId, acquirerId, 
								debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
								"Village Banking Contribution", device.getSwitchToLive());
						transaction = (Transaction)swpService2.createNewRecord(transaction);
						//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
						String hash = merchantCode + "" + deviceCode + "" + serviceType.name() + "" + orderRef + "" + df.format(totalToPay) + "" + merchant.getApiKey();
						hash = UtilityHelper.get_SHA_512_SecurePassword(hash);
						log.info("hash....111");
						log.info(hash);
						Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, totalToPay, hash, "", transaction, 
								transaction.getOrderRef(), serviceType.name(), token);
						String drResStr = (String)drRes.getEntity();
						System.out.println(drResStr);
						if(drResStr!=null)
						{
							JSONObject drResJS = new JSONObject(drResStr);
							if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
							{
								
								
								if(sourceType.equals("CARD"))
									card = card.withdraw(swpService2, totalToPay, totalCharges);
								
								
								JSONObject jsbreakdown = new JSONObject();
								jsbreakdown.put("Sub-total", totalToPay);
								jsbreakdown.put("Charges", totalCharges);
								
								transaction.setStatus(TransactionStatus.SUCCESS);
								JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.VILLAGE_BANKING_GROUP_CONTRIBUTION.name(), group.getName(), "", accountNo, transaction.getOrderRef().toUpperCase(), 
										transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
								transaction.setSummary(txnDetails.toString());
								swpService2.updateRecord(transaction);
								
								
								
								hql = "Select tp from GroupMember tp where tp.group.id = "+group.getId()+" AND tp.addedCustomer.id != " + customer.getId() + " AND tp.deletedAt IS NULL "
										+ "AND tp.isActive = 1";
								Collection<GroupMember> allGroupMembers = (Collection<GroupMember>)swpService2.getAllRecordsByHQL(hql);
								Iterator<GroupMember> gmIter = allGroupMembers.iterator();
					    		
								String msgBody = "Group contribution of " + currency + df.format(totalToPay) + " paid by " + customer.getFirstName();
					    		GroupMessage groupMessage = new GroupMessage(msgBody, customer, null, group);
					    		swpService2.createNewRecord(groupMessage);
					    		
								while(gmIter.hasNext())
								{
									GroupMember gm = gmIter.next();
									Notification not = new Notification(gm.getAddedCustomer().getUser().getId(), false, "Village Banking Contribution", msgBody, null, NotificationType.GROUP_CONTRIBUTION.name());
									swpService2.createNewRecord(not);
								}
								
								gpeIter = gpeToPay.iterator();
								while(gpeIter.hasNext())
								{
									GroupPaymentsExpected gpe = gpeIter.next();
									Double amountPaid = (gpe.getAmountPaid()!=null ? gpe.getAmountPaid() : 0.00) + gpe.getOutstandingBalance();
									Double penaltiesPaid = (gpe.getPenaltiesPaid()!=null ? gpe.getPenaltiesPaid() : 0.00) + (gpe.getTotalPenalties() - gpe.getPenaltiesPaid());
									gpe.setAmountPaid(amountPaid);
									gpe.setPenaltiesPaid(penaltiesPaid);
									gpe.setOutstandingBalance(0.00);
									gpe.setIsPaid(true);
									swpService2.updateRecord(gpe);
									
									if(amountPaid>0)
									{
										GroupContribution gc = new GroupContribution(amountPaid, customer, gpe, false, group.getId());
										swpService2.createNewRecord(gc);
									}
									if(penaltiesPaid>0)
									{
										GroupContribution gc = new GroupContribution(penaltiesPaid, customer, gpe, true, group.getId());
										swpService2.createNewRecord(gc);
									}
								}
								
								
								
								

								jbIter = gpeToPayPartialPenalties.iterator();
								while(jbIter.hasNext())
								{
									JSONObject gpe = jbIter.next();
									Double penaltyAmountToPay = gpe.getDouble("penaltyAmount");
									totalToPay = totalToPay + penaltyAmountToPay;
								}
								
								
								jbIter = gpeToPayPartialOutstanding.iterator();
								while(jbIter.hasNext())
								{
									JSONObject jb = jbIter.next();
									Double amountPaid = jb.getDouble("contributionAmount");
									GroupPaymentsExpected gpe = (GroupPaymentsExpected)jb.get("groupPaymentsExpected");
									gpe.setAmountPaid(gpe.getAmountPaid() + amountPaid);
									gpe.setOutstandingBalance(gpe.getAmount() - gpe.getAmountPaid());
									gpe.setIsPaid((gpe.getAmount() - gpe.getAmountPaid())>=0 ? false : false);
									swpService2.updateRecord(gpe);
									
									if(amountPaid>0)
									{
										GroupContribution gc = new GroupContribution(amountPaid, customer, gpe, false, group.getId());
										swpService2.createNewRecord(gc);
									}
								}
								
								
								
								

								
								jbIter = gpeToPayPartialPenalties.iterator();
								while(jbIter.hasNext())
								{
									JSONObject jb = jbIter.next();
									Double penaltyPaid = jb.getDouble("penaltyAmount");
									GroupPaymentsExpected gpe = (GroupPaymentsExpected)jb.get("groupPaymentsExpected");
									gpe.setPenaltiesPaid(gpe.getPenaltiesPaid() + penaltyPaid);
									gpe.setOutstandingBalance(gpe.getTotalPenalties() - gpe.getPenaltiesPaid());
									swpService2.updateRecord(gpe);
									
									if(penaltyPaid>0)
									{
										GroupContribution gc = new GroupContribution(penaltyPaid, customer, gpe, true, group.getId());
										swpService2.createNewRecord(gc);
									}
								}

								String receipentMobileNumber = customer.getContactMobile();
								String smsMessage = "Hi "+customer.getFirstName()+".\nYour payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was successful.\nGroup paid for: " + group.getName() + 
										"\nReceipt Num: " + transaction.getTransactionRef().toUpperCase();
								//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
								//swpService.createNewRecord(smsMsg);

								SmsSender smsSender = new SmsSender(swpService2, smsMessage, receipentMobileNumber);
								new Thread(smsSender).start();
								
								
								jsonObject.put("status", ERROR.GENERAL_OK);
								jsonObject.put("message", "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was successful");
								 return jsonObject;
							}
							else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
							{
								transaction.setStatus(TransactionStatus.FAIL);
								swpService2.updateRecord(transaction);
								String message = drResJS.getString("message");
								jsonObject.put("status", ERROR.GENERAL_FAIL);
								jsonObject.put("message", message==null ? "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful" : "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful. " + message);
								 return jsonObject;
							}
						}
						else
						{
							transaction.setStatus(TransactionStatus.FAIL);
							swpService2.updateRecord(transaction);
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful");
							 return jsonObject;
						}
					}
					else
					{
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", "Invalid amount. To pay, your must pay more than " + currency.name() + df.format(app.getAllSettings().getDouble("maximumtransactionamountweb")));

						
			            return jsonObject;
					}
				}
				else
				{
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "Your payment was not successful. The amount you provided does not cover any of the outstanding payments");
					 return jsonObject;
				}
				
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "You do not have any outstanding group contribution payments.");
				
	            return jsonObject;
			}
			
			
			jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Your payment was not successful. You either do not have any outstanding contributions to make or the amount you provided is does not cover any of the outstanding payments");
			return jsonObject;
		}
		catch(Exception e)
		{
			e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
			return jsonObject;
		}
	}

	
	
	public Response approveOrDisapproveGroupLoan(String token, Long loanId, Integer isApprove, String requestId,
			String ipAddress, SwpService swpService) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		DecimalFormat df = new DecimalFormat("0.00");
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null && loanId!=null && isApprove!=null && (isApprove==1 || isApprove==0))
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			
			String hql = "Select tp from GroupLoan tp where tp.id = " + loanId + " AND tp.deletedAt IS NULL AND tp.status = " + GroupLoanStatus.APPLYING.ordinal();
			GroupLoan groupLoan = (GroupLoan)this.swpService.getUniqueRecordByHQL(hql);
			
			if(groupLoan==null)
			{
				jsonObject.put("status", ERROR.GROUP_LOAN_NOT_SETUP);
				jsonObject.put("message", "Group loan can not be found. Confirm if its been approved or disapproved");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			hql = "Select tp from GroupMember tp where tp.addedCustomer.user.id = " + user.getId() + " AND tp.group.id = "+ groupLoan.getGroupId() +" AND tp.isAdmin = 1 AND tp.isActive = 1";
			GroupMember groupMemberAdmin = (GroupMember)swpService.getUniqueRecordByHQL(hql);
			if(groupMemberAdmin==null)
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_PRIVILEDGES);
				jsonObject.put("message", "You do not have the privileges to approve or disapprove loans on this group");
				
	            return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			Customer customer = groupLoan.getGroupMember().getAddedCustomer();
			
			if(isApprove==0)
			{
				groupLoan.setStatus(GroupLoanStatus.REJECTED);
				this.swpService.updateRecord(groupLoan);
				
				jsonObject.put("status", ERROR.GENERAL_OK);
				jsonObject.put("message", "Loan request has been rejected successfully. Thank you for your prompt action on the loan request");
				
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
			
			
			Double principal = groupLoan.getPrincipal();
			hql = "Select tp from GroupAccount tp where tp.group.id = " + groupLoan.getGroupId() + " AND tp.deletedAt IS NULL";
			GroupAccount groupAccount = (GroupAccount)swpService.getUniqueRecordByHQL(hql);
			if(groupAccount.getCurrentBalance()<principal)
			{
				jsonObject.put("status", ERROR.INSUFFICIENT_FUNDS);
				jsonObject.put("message", "There are not sufficient funds currently in your groups wallet. You can try booking a loan later after the group has received payments");
				
				return Response.status(200).entity(jsonObject.toString()).build();
			}
    		
			Group group = groupAccount.getGroup();
    		
			
    		
    		GroupLoanTerms groupLoanTerm = groupLoan.getGroupLoanTerm();
    		
    		
    		String availableTenorType = "";
    		TenorType repaymentTenorType = groupLoanTerm.getRepaymentTenorType();
    		if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.MONTH))
    		{
    			availableTenorType = (TenorType.MONTH.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.WEEK))
    		{
    			availableTenorType = (TenorType.WEEK.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.YEAR))
    		{
    			availableTenorType = (TenorType.YEAR.name());
    		}
    		else if(repaymentTenorType!=null && repaymentTenorType.equals(TenorType.DAY))
    		{
    			availableTenorType = (TenorType.DAY.name());
    		}
    		
    		
    		JSONObject sched = null;
    		JSONArray schedule = null;
    		Integer term = groupLoan.getTenor();
    		GroupMember groupMember = groupLoan.getGroupMember();
    		Date repaymentStartDate = null;
    		if(availableTenorType.equals(TenorType.MONTH.name()))
    		{
    			Double monthlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForMonthly(principal, monthlyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}/**/
    			
    			groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);
    		}
    		else if(availableTenorType.equals(TenorType.WEEK.name()))
    		{
    			Double weeklyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForWeekly(principal, weeklyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
    			groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);
    		}
    		else if(availableTenorType.equals(TenorType.DAY.name()))
    		{
    			Double dailyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForDaily(principal, dailyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
    			groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);
    		}
    		else if(availableTenorType.equals(TenorType.YEAR.name()))
    		{
    			Double yearlyInterestRate = groupLoanTerm.getInterestRate();
    			schedule = UtilityHelper.amortizationScheduleForYearly(principal, yearlyInterestRate, term, new Date());

        		if(schedule==null)
        		{
        			
        			jsonObject.put("status", ERROR.INVALID_REPAYMENT_PERIOD);
        			
                    
                    return Response.status(200).entity(jsonObject.toString()).build();
        		}
    			Double incurredInterest = 0.00;
    			Double principalIncurred = 0.00;
    			Double outstandingBalance = 0.00;
    			Double totalIncurredInterest = 0.00;
    			Double totalIncurredPrincipal = 0.00;
    			repaymentStartDate = null;
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				double interestPaid = scheduleEntry.getDouble("interestPaid");
    				double principalPaid = scheduleEntry.getDouble("principalPaid");
    				interestPaid = BigDecimal.valueOf(interestPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalPaid = BigDecimal.valueOf(principalPaid).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				totalIncurredInterest = totalIncurredInterest + interestPaid;
    				totalIncurredPrincipal = totalIncurredPrincipal + principalPaid;
    				outstandingBalance = outstandingBalance + interestPaid + principalPaid;
    			}
				totalIncurredInterest = BigDecimal.valueOf(totalIncurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
				totalIncurredPrincipal = BigDecimal.valueOf(totalIncurredPrincipal).setScale(2, RoundingMode.HALF_UP).doubleValue();
    			groupAccount.setCurrentBalance(groupAccount.getCurrentBalance() - principal);
    			swpService.updateRecord(groupAccount);
    			for(int i=0; i<schedule.length(); i++)
    			{
    				JSONObject scheduleEntry = schedule.getJSONObject(i);
    				Date dueDate = (Date)scheduleEntry.get("dateDueReal");
    				if(repaymentStartDate==null)
    				{
    					repaymentStartDate = (Date)scheduleEntry.get("dateDueReal");
    				}
    				incurredInterest = scheduleEntry.getDouble("interestPaid");
    				principalIncurred = scheduleEntry.getDouble("principalPaid");
    				incurredInterest = BigDecimal.valueOf(incurredInterest).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				principalIncurred = BigDecimal.valueOf(principalIncurred).setScale(2, RoundingMode.HALF_UP).doubleValue();
    				outstandingBalance = outstandingBalance + incurredInterest + principalIncurred;
    				GroupLoanRepaymentsExpected groupLoanRepaymentsExpected = 
    						new GroupLoanRepaymentsExpected(groupLoan, dueDate, Boolean.FALSE, principalIncurred, incurredInterest, 0.00, 0.00, 0.00, 0.00, 0.00, (i+1));
    				swpService.createNewRecord(groupLoanRepaymentsExpected);
    			}
    			groupLoan.setStatus(GroupLoanStatus.ACTIVE);
    			swpService.updateRecord(groupLoan);
    		}
    		

    		
    		String details = "Loan of "+groupLoan.getContributionPackage().getCurrency().name()+"" +df.format(principal)+ " approved for " + 
    				groupLoan.getGroupMember().getAddedCustomer().getFirstName() + 
    				". Last repayment date is " + schedule.getJSONObject(schedule.length() - 1).getString("dateDue") + ". New portfolio balance: " + 
    				groupLoan.getContributionPackage().getCurrency().name() + "" + df.format(groupAccount.getCurrentBalance());
    		GroupMessage groupMessage = new GroupMessage(details, customer, null, group);
    		swpService.createNewRecord(groupMessage);
    		
    		String smsMessage = "Bevura Village Banking Loan No - #" + groupLoan.getLoanAccountNo() + "\nLoan Amount: " + groupLoan.getContributionPackage().getCurrency().name()
    				+ df.format(principal) + "\nStatus: Approved\nFirst Repayment Date: " + schedule.getJSONObject(0).getString("dateDue") + 
    				"\n\nPlease check your Bevura app to view your repayment schedule";
    	
			SmsSender smsSender = new SmsSender(swpService, smsMessage, groupLoan.getGroupMember().getAddedCustomer().getContactMobile());
			new Thread(smsSender).start();

			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "You have approved the loan successfully. The loan amount has been paid into the group members wallet");
			
            return Response.status(200).entity(jsonObject.toString()).build();
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return Response.status(200).entity(jsonObject.toString()).build();
    	}
	}

	public JSONObject getLoanRepaymentSchedule(String token, Long loanId, String requestId, String ipAddress, SwpService swpService2) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return jsonObject;
			}
			
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService2.getUniqueRecordByHQL(hql);
			
			hql = "Select tp.* from  group_loan_repayments_expected tp where tp.deletedAt IS NULL AND tp.groupLoan_id = " + loanId + " ORDER BY tp.createdAt ASC";
			List<Map<String, Object>> group_loan_repayments_expected = (List<Map<String, Object>>)swpService2.getQueryBySQLResultsWithKeys(hql);
			
			GroupLoan gl = (GroupLoan)swpService2.getRecordById(GroupLoan.class, loanId);
			
    		Double totalPrincipal = 0.00;
    		Double totalInterest = 0.00;
    		Double outstandingBalance = 0.00;
    		Double totalPaid_ = 0.00;

    		for(int i1=0; i1<group_loan_repayments_expected.size(); i1++)
			{
				Map<String, Object> group_loan_repayments_expected_entry = group_loan_repayments_expected.get(i1);
    			Double totalInterestIncurred = (Double)group_loan_repayments_expected_entry.get("totalInterestIncurred");
    			totalInterestIncurred = totalInterestIncurred==null ? 0.00 : totalInterestIncurred;
    			Double totalPrincipalIncurred = (Double)group_loan_repayments_expected_entry.get("totalPrincipalIncurred");
    			totalPrincipalIncurred = totalPrincipalIncurred==null ? 0.00 : totalPrincipalIncurred;
    			Double totalPenaltiesIncurred = (Double)group_loan_repayments_expected_entry.get("totalPenaltiesIncurred");
    			totalPenaltiesIncurred = totalPenaltiesIncurred==null ? 0.00 : totalPenaltiesIncurred;
    			Double totalInterestRepaid = (Double)group_loan_repayments_expected_entry.get("totalInterestRepaid");
    			totalInterestRepaid = totalInterestRepaid==null ? 0.00 : totalInterestRepaid;
    			Double totalPrincipalRepaid = (Double)group_loan_repayments_expected_entry.get("totalPrincipalRepaid");
    			totalPrincipalRepaid = totalPrincipalRepaid==null ? 0.00 : totalPrincipalRepaid;
    			Double 	totalPenaltiesRepaid = (Double)group_loan_repayments_expected_entry.get("totalPenaltiesRepaid");
    			totalPenaltiesRepaid = 	totalPenaltiesRepaid==null ? 0.00 : 	totalPenaltiesRepaid;
    			totalInterestIncurred = BigDecimal.valueOf(totalInterestIncurred).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    			totalPrincipalIncurred = BigDecimal.valueOf(totalPrincipalIncurred).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    			totalPenaltiesIncurred = BigDecimal.valueOf(totalPenaltiesIncurred).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    			totalPrincipalRepaid = BigDecimal.valueOf(totalPrincipalRepaid).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    			totalInterestRepaid = BigDecimal.valueOf(totalInterestRepaid).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    			totalPenaltiesRepaid = BigDecimal.valueOf(totalPenaltiesRepaid).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
				totalInterest = totalInterest + totalInterestIncurred;
				totalPrincipal = totalPrincipal + totalPrincipalIncurred;
				totalPaid_ = totalPaid_ + totalPrincipalRepaid + totalInterestRepaid + totalPenaltiesRepaid;
    		}
			
    		
			jsonObject.put("status", ERROR.GENERAL_OK);
			jsonObject.put("message", "Loan repayment schedule found");
			jsonObject.put("schedule", group_loan_repayments_expected);
			jsonObject.put("totalPrincipal", totalPrincipal);
    		jsonObject.put("totalInterest", totalInterest);
    		jsonObject.put("outstandingBalance", gl.getOutstandingBalance());
    		jsonObject.put("totalPaid", totalPaid_);
    		jsonObject.put("currency", gl.getContributionPackage().getCurrency().name());
			log.info(jsonObject.toString());
			return jsonObject;
			
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "Loan repayment was not successful. We experienced a system error processing your payment. Please try again");
			
            
            return jsonObject;
    	}
	}

	public JSONObject repayVillageBankingLoan(String token, Long loanId, Double amount, String requestId,
			String ipAddress, SwpService swpService2, Device device, Merchant merchant, String sourceType, Account wallet, 
			ECard card, String bankKey, Channel channel, Acquirer acquirer, String orderRef, 
			javax.ws.rs.core.HttpHeaders httpHeaders, HttpServletRequest requestContext) {
		// TODO Auto-generated method stub
		DecimalFormat df = new DecimalFormat("0.00");
		JSONObject jsonObject = new JSONObject();
    	try
    	{
    		this.swpService = serviceLocator.getSwpService();
    		if(token!=null)
    		{
    			
    		}
    		else
    		{
    			jsonObject.put("status", ERROR.INCOMPLETE_PARAMETERS);
				jsonObject.put("message", "Incomplete Parameters in request");
				
	            return jsonObject;
    		}
    		
    		Application app = Application.getInstance(swpService);
			JSONObject verify = UtilityHelper.verifyToken(token, app);
			if(verify == null)
			{
				jsonObject.put("status", ERROR.FORCE_LOGOUT_USER);
				jsonObject.put("message", "Your session has expired. Please log in again");
				
				return jsonObject;
			}
			
			String subject = verify.getString("subject");
			User user = new Gson().fromJson(subject, User.class);
			
			String hql = "Select tp from Customer tp where tp.user.id = " + user.getId();
			Customer customer = (Customer)swpService2.getUniqueRecordByHQL(hql);
			
			hql = "Select tp from GroupLoan tp where tp.id = " + loanId;
			GroupLoan groupLoan = (GroupLoan)swpService2.getUniqueRecordByHQL(hql);
			Group group = groupLoan.getGroupMember().getGroup();
			ProbasePayCurrency currency = groupLoan.getContributionPackage().getCurrency();
			
			hql = "Select tp from GroupLoanRepaymentsExpected tp where tp.groupLoan.id = " + loanId + " AND tp.deletedAt IS NULL";
			Collection<GroupLoanRepaymentsExpected> groupLoanRepaymentsExpected = (Collection<GroupLoanRepaymentsExpected>)swpService2.getAllRecordsByHQL(hql);
			
			String accountNo = null;
			if(card==null)
				accountNo = wallet.getAccountIdentifier();
			else
				accountNo = card.getAccount().getAccountIdentifier();
			
			Iterator<GroupLoanRepaymentsExpected> groupLoanRepaymentsExpectedIter = groupLoanRepaymentsExpected.iterator();
			Double amount2 = amount;
			Double totalToPay = 0.00;
			while(groupLoanRepaymentsExpectedIter.hasNext())
			{
				GroupLoanRepaymentsExpected glre = groupLoanRepaymentsExpectedIter.next();
				Double principalOutstanding = (glre.getTotalPrincipalIncurred()==null ? 0 : glre.getTotalPrincipalIncurred()) - (glre.getTotalPrincipalRepaid()==null ? 0 : glre.getTotalPrincipalRepaid());
				Double interestOutstanding = (glre.getTotalInterestIncurred()==null ? 0 : glre.getTotalInterestIncurred()) - (glre.getTotalInterestRepaid()==null ? 0 : glre.getTotalInterestRepaid());
				Double penaltiesOutstanding = (glre.getTotalPenaltiesIncurred()==null ? 0 : glre.getTotalPenaltiesIncurred()) - (glre.getTotalPenaltiesRepaid()==null ? 0 : glre.getTotalPenaltiesRepaid());

				log.info("principalOutstanding = " + principalOutstanding);
				log.info("interestOutstanding = " + interestOutstanding);
				log.info("penaltiesOutstanding = " + penaltiesOutstanding);
				if(principalOutstanding>0)
				{
					if(principalOutstanding>amount2 && amount2>0)
					{
						log.info("option 1");
						totalToPay = totalToPay + amount2;
						log.info("totalToPay = " + totalToPay );
						amount2 = 0.00;
						log.info("amount2 = " + amount2);
					}
					else if((amount2 - principalOutstanding)>=0)
					{
						log.info("option 2");
						totalToPay = totalToPay + principalOutstanding;
						log.info("totalToPay = " + totalToPay );
						amount2 = amount2 - principalOutstanding;
						log.info("amount2 = " + amount2);
					}
				}
				if(interestOutstanding>0)
				{
					if(interestOutstanding>amount2 && amount2>0)
					{
						log.info("option 3");
						totalToPay = totalToPay + amount2;
						log.info("totalToPay = " + totalToPay );
						amount2 = 0.00;
						log.info("amount2 = " + amount2);
					}
					else if((amount2 - interestOutstanding)>=0)
					{
						log.info("option 4");
						totalToPay = totalToPay + interestOutstanding;
						log.info("totalToPay = " + totalToPay );
						amount2 = amount2 - interestOutstanding;
						log.info("amount2 = " + amount2);
					}
				}
				if(penaltiesOutstanding>0)
				{
					if(principalOutstanding>amount2 && amount2>0)
					{
						log.info("option 5");
						totalToPay = totalToPay + amount2;
						log.info("totalToPay = " + totalToPay );
						amount2 = 0.00;
						log.info("amount2 = " + amount2);
					}
					else if((amount2 - penaltiesOutstanding)>=0)
					{
						log.info("option 6");
						totalToPay = totalToPay + penaltiesOutstanding;
						log.info("totalToPay = " + totalToPay);
						amount2 = amount2 - penaltiesOutstanding;
						log.info("amount2 = " + amount2);
					}
				}
				
			}
			
			
			log.info("totalToPay....");
			log.info(totalToPay);

			log.info("totalToPay..." + totalToPay);
			
			String zauthKey = null;
			if(device.getSwitchToLive()!=null && device.getSwitchToLive().equals(true))
				zauthKey = device.getZicbAuthKey();
			else
				zauthKey = device.getZicbDemoAuthKey();
			
				
			//String[] verifyResp = UtilityHelper.verifyZICBWalletOTP(new JSONObject(), mobileNo, userEmail, otpRef, otp, zauthKey, acquirer.getFundsTransferEndPoint());
			
			if(totalToPay>app.getAllSettings().getDouble("minimumtransactionamountweb") && totalToPay<app.getAllSettings().getDouble("maximumtransactionamountweb"))
			{
				String accountData = UtilityHelper.encryptData(accountNo, bankKey);
				System.out.println("bankKey..." + bankKey);
				System.out.println("accountNo..." + accountNo);
				System.out.println("accountData..." + accountData);
				String narration = "GROUPLOANREPAYMENT~" + amount + "~" + totalToPay + "~" + group.getName() + "~" + customer.getUser().getMobileNo();
				PaymentServicesV2 paymentServices = new PaymentServicesV2();
				
				String transactionRef = RandomStringUtils.randomAlphanumeric(16).toUpperCase();
				String bankPaymentReference = null;
				Long customerId = customer.getId();
				Boolean creditAccountTrue = null;
				Boolean creditCardTrue = null;
				String rpin = null;
				Channel ch = channel;
				Date transactionDate = new Date();
				ServiceType serviceType = ServiceType.GROUP_CONTRIBUTION;
				String payerName = customer.getFirstName() + " " + customer.getLastName();
				String payerEmail = customer.getContactEmail();
				String payerMobile = customer.getContactMobile();
				TransactionStatus status = TransactionStatus.PENDING;
				ProbasePayCurrency probasePayCurrency = groupLoan.getContributionPackage().getCurrency();
				String transactionCode = null;
				Boolean creditPoolAccountTrue = false;
				String messageRequest = null;
				String messageResponse = null;
				Double fixedCharge = 0.00;
				Double transactionCharge = 0.00;
				Double transactionPercentage = 0.00;
				Double schemeTransactionCharge = 0.00;
				Double schemeTransactionPercentage = 0.00;
				String responseCode = null;
				String oTP = null;
				String oTPRef = null;
				String merchantBank = null;
				String merchantAccount = null; 
				Long transactingBankId = acquirer.getBank().getId();
				Long receipientTransactingBankId = null;
				Integer accessCode = null;
				Long sourceEntityId = null;
				Long receipientEntityId = null;
				Channel receipientChannel = ch;
				String transactionDetail = narration;
				Double closingBalance = null;
				Double totalCreditSum = null;
				Double totalDebitSum = null;
				Long paidInByBankUserAccountId = null;
				String customData = null;
				String responseData = null;
				Long adjustedTransactionId = null;
				Long acquirerId = acquirer.getId();
				String merchantName = merchant.getMerchantName();
				String merchantCode = merchant.getMerchantCode();
				String deviceCode = device.getDeviceCode();
				Long merchantId_ = merchant.getId();
				Boolean debitAccountTrue = null;
				Boolean debitCardTrue = null;
				Long creditAccountId = null;
				Long creditCardId = null;
				Long debitAccountId = null;
				Long debitCardId = null;
				String otp= null;
				Account acct = null;
				if(sourceType.equals("WALLET"))
				{
					
					creditAccountTrue = false;
					creditCardTrue = false;
					serviceType = ServiceType.DEBIT_WALLET;
					sourceEntityId = wallet.getId();
					
					debitAccountTrue = true;
					debitCardTrue = false;
					creditAccountId = null;
					creditCardId = null;
					debitAccountId = wallet.getId();
					debitCardId = null;
					acct = wallet;
				}
				else if(sourceType.equals("CARD"))
				{
					creditAccountTrue = false;
					creditCardTrue = false;
					serviceType = ServiceType.DEBIT_CARD;
					sourceEntityId = card.getId();
					
					debitAccountTrue = false;
					debitCardTrue = true;
					creditAccountId = null;
					creditCardId = null;
					debitAccountId = null;
					debitCardId = card.getId();
					acct = card.getAccount();
				}
				
				
				Double totalCharges = fixedCharge + transactionCharge + transactionPercentage + schemeTransactionCharge + schemeTransactionPercentage;
				Transaction transaction = new Transaction(transactionRef, bankPaymentReference,
						customerId, creditAccountTrue, creditCardTrue,
						orderRef, rpin, ch,
						transactionDate, serviceType, payerName,
						payerEmail, payerMobile, status,
						probasePayCurrency, transactionCode,
						acct, card, device,
						creditPoolAccountTrue, messageRequest,
						messageResponse, fixedCharge,
						transactionCharge, transactionPercentage,
						schemeTransactionCharge, schemeTransactionPercentage,
						totalToPay, responseCode, oTP, oTPRef,
						merchantId_, merchantName, merchantCode,
						merchantBank, merchantAccount, 
						transactingBankId, receipientTransactingBankId,
						accessCode, sourceEntityId, receipientEntityId,
						receipientChannel, transactionDetail,
						closingBalance, totalCreditSum, totalDebitSum,
						paidInByBankUserAccountId, customData,
						responseData, adjustedTransactionId, acquirerId, 
						debitAccountTrue, debitCardTrue, creditAccountId, creditCardId, debitAccountId, debitCardId, 
						"Village Banking Loan Repayment", device.getSwitchToLive());
				transaction = (Transaction)swpService2.createNewRecord(transaction);
				//paymentServices.directDebitZICBPayment(httpHeaders, requestContext, false, otp, transaction, null, null, orderId, account, accountNo, narration);
				String hash = merchantCode + "" + deviceCode + "" + serviceType.name() + "" + orderRef + "" + df.format(totalToPay) + "" + merchant.getApiKey();;
				log.info(hash);
				hash = UtilityHelper.get_SHA_512_SecurePassword(hash);
				log.info("hash....111");
				log.info(hash);
				Response drRes = paymentServices.debitWallet(httpHeaders, requestContext, false, otp, merchantCode, deviceCode, accountData, narration, totalToPay, hash, "", transaction, 
						transaction.getOrderRef(), serviceType.name(), token);
				String drResStr = (String)drRes.getEntity();
				System.out.println(drResStr);
				if(drResStr!=null)
				{
					JSONObject drResJS = new JSONObject(drResStr);
					if(drResJS.has("status") && drResJS.getInt("status")==(ERROR.PAYMENT_TRANSACTION_SUCCESS))
					{
						
						
						if(sourceType.equals("CARD"))
							card = card.withdraw(swpService2, totalToPay, totalCharges);
						
						
						JSONObject jsbreakdown = new JSONObject();
						jsbreakdown.put("Sub-total", totalToPay);
						jsbreakdown.put("Charges", totalCharges);
						
						transaction.setStatus(TransactionStatus.SUCCESS);
						JSONObject txnDetails = UtilityHelper.createTxnDetails(BillType.VILLAGE_BANKING_LOAN_REPAYMENT.name(), group.getName(), "", accountNo, transaction.getOrderRef().toUpperCase(), 
								transaction.getCreated_at(), transaction.getProbasePayCurrency().name(), transaction.getAmount(), transaction.getStatus().name(), jsbreakdown);
						transaction.setSummary(txnDetails.toString());
						swpService2.updateRecord(transaction);
						
						
						
						hql = "Select tp from GroupMember tp where tp.group.id = "+group.getId()+" AND tp.addedCustomer.id != " + customer.getId() + " AND tp.deletedAt IS NULL "
								+ "AND tp.isActive = 1";
						Collection<GroupMember> allGroupMembers = (Collection<GroupMember>)swpService2.getAllRecordsByHQL(hql);
						Iterator<GroupMember> gmIter = allGroupMembers.iterator();
			    		
						String msgBody = "Group loan repayment of " + groupLoan.getContributionPackage().getCurrency().name() + df.format(totalToPay) + " repaid by " + customer.getFirstName();
			    		GroupMessage groupMessage = new GroupMessage(msgBody, customer, null, group);
			    		swpService2.createNewRecord(groupMessage);
			    		
						while(gmIter.hasNext())
						{
							GroupMember gm = gmIter.next();
							Notification not = new Notification(gm.getAddedCustomer().getUser().getId(), false, "Village Banking Loan Repayment", msgBody, null, NotificationType.GROUP_LOAN_REPAYMENT.name());
							swpService2.createNewRecord(not);
						}
						
						
						groupLoanRepaymentsExpectedIter = groupLoanRepaymentsExpected.iterator();
						amount2 = amount;
						Double totalRepaid = 0.00;
						Double totalInterestRepaid = 0.00;
						Double totalPrincipalRepaid = 0.00;
						Double totalPenaltyRepaid = 0.00;
						log.info("<<<--------------->>>");
						log.info(amount2);
						totalToPay = 0.00;
						while(groupLoanRepaymentsExpectedIter.hasNext())
						{
							GroupLoanRepaymentsExpected glre = groupLoanRepaymentsExpectedIter.next();
							Double principalOutstanding = (glre.getTotalPrincipalIncurred()==null ? 0 : glre.getTotalPrincipalIncurred()) - (glre.getTotalPrincipalRepaid()==null ? 0 : glre.getTotalPrincipalRepaid());
							Double interestOutstanding = (glre.getTotalInterestIncurred()==null ? 0 : glre.getTotalInterestIncurred()) - (glre.getTotalInterestRepaid()==null ? 0 : glre.getTotalInterestRepaid());
							Double penaltiesOutstanding = (glre.getTotalPenaltiesIncurred()==null ? 0 : glre.getTotalPenaltiesIncurred()) - (glre.getTotalPenaltiesRepaid()==null ? 0 : glre.getTotalPenaltiesRepaid());
							

							log.info("principalOutstanding = " + principalOutstanding);
							log.info("interestOutstanding = " + interestOutstanding);
							log.info("penaltiesOutstanding = " + penaltiesOutstanding);
							Double principalToPay = 0.00;
							Double interestToPay = 0.00;
							Double penaltyToPay = 0.00;
							
							if(principalOutstanding>0)
							{
								if(principalOutstanding>amount2 && amount2>0)
								{
									log.info("option 1");
									totalToPay = totalToPay + amount2;
									principalToPay = amount2;
									amount2 = 0.00;
								}
								else if((amount2 - principalOutstanding)>=0)
								{
									log.info("option 2");
									totalToPay = totalToPay + principalOutstanding;
									principalToPay = principalOutstanding;
									amount2 = amount2 - principalOutstanding;
								}
							}
							if(interestOutstanding>0)
							{
								if(interestOutstanding>amount2 && amount2>0)
								{
									log.info("option 3");
									totalToPay = totalToPay + amount2;
									interestToPay = amount2;
									amount2 = 0.00;
								}
								else if((amount2 - interestOutstanding)>=0)
								{
									log.info("option 4");
									totalToPay = totalToPay + interestOutstanding;
									interestToPay = interestOutstanding;
									amount2 = amount2 - interestOutstanding;
								}
							}
							if(penaltiesOutstanding>0)
							{
								if(principalOutstanding>amount2 && amount2>0)
								{
									log.info("option 5");
									totalToPay = totalToPay + amount2;
									penaltyToPay = amount2;
									amount2 = 0.00;
								}
								else if((amount2 - penaltiesOutstanding)>=0)
								{
									log.info("option 6");
									totalToPay = totalToPay + penaltiesOutstanding;
									penaltyToPay = penaltiesOutstanding;
									amount2 = amount2 - penaltiesOutstanding;
								}
							}
							
							if((principalToPay + interestToPay + penaltyToPay)>0)
							{
								glre.setTotalAmountRepaid(glre.getTotalAmountRepaid() + (principalToPay + interestToPay + penaltyToPay));
								glre.setTotalPrincipalRepaid(glre.getTotalInterestRepaid() + principalToPay);
								glre.setTotalPenaltiesRepaid(glre.getTotalPenaltiesRepaid() + penaltyToPay);
								glre.setTotalInterestRepaid(glre.getTotalInterestRepaid() + interestToPay);
								
								Double outstandingBalanceAfterPayment = (glre.getTotalPrincipalIncurred() - glre.getTotalPrincipalRepaid()) + 
										(glre.getTotalInterestIncurred() - glre.getTotalInterestRepaid()) + 
										(glre.getTotalPenaltiesIncurred() - glre.getTotalPenaltiesRepaid());
								if(outstandingBalanceAfterPayment>0)
								{
									glre.setIsCompleted(true);
								}
								swpService2.updateRecord(glre);
								
								GroupLoanRepayment glr = new GroupLoanRepayment(glre, (principalToPay + interestToPay + penaltyToPay), 
										principalToPay, interestToPay, penaltyToPay, group.getId());
								swpService2.createNewRecord(glr);
								totalRepaid = totalRepaid +  (principalToPay + interestToPay + penaltyToPay);

								totalInterestRepaid = totalInterestRepaid + interestToPay;
								totalPrincipalRepaid = totalPrincipalRepaid + principalToPay;
								totalPenaltyRepaid = totalPenaltyRepaid + penaltyToPay; 
							}
							
							
						}
						
						
						if((totalInterestRepaid+totalPrincipalRepaid+totalPenaltyRepaid)>0)
						{
							groupLoan.setLastRepaymentDate(new Date());
							groupLoan.setTotalInterestRepaid(groupLoan.getTotalInterestRepaid() + totalInterestRepaid);
							groupLoan.setTotalPrincipalRepaid(groupLoan.getTotalPrincipalRepaid() + totalPrincipalRepaid);
							groupLoan.setTotalPenaltiesRepaid(groupLoan.getTotalPenaltiesRepaid() + totalPenaltyRepaid);
							groupLoan.setOutstandingBalance(groupLoan.getOutstandingBalance() - (totalPenaltyRepaid + totalInterestRepaid + totalPrincipalRepaid));
							
							if(groupLoan.getOutstandingBalance()==0)
							{
								groupLoan.setStatus(GroupLoanStatus.CLOSED);
							}
							swpService2.updateRecord(groupLoan);
							
							hql = "Select tp from GroupAccount tp where tp.group.id = " + group.getId() + " AND tp.deletedAt IS NULL";
							GroupAccount ga = (GroupAccount)swpService2.getUniqueRecordByHQL(hql);
							ga.setCurrentBalance(ga.getCurrentBalance() + totalPenaltyRepaid);
							swpService2.updateRecord(ga);
							
							
							
							String receipentMobileNumber = customer.getContactMobile();
							String smsMessage = "Hi "+customer.getFirstName()+".\nYour payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was successful.\nGroup paid for: " + group.getName() + 
									"\nReceipt Num: " + transaction.getTransactionRef().toUpperCase();
							//SMSMesage smsMsg = new SMSMesage(receipentMobileNumber, smsMessage, null, SMSMessageStatus.PENDING.name(), null);
							//swpService.createNewRecord(smsMsg);
	
							SmsSender smsSender = new SmsSender(swpService2, smsMessage, receipentMobileNumber);
							new Thread(smsSender).start();
							
							jsonObject.put("status", ERROR.GENERAL_OK);
							jsonObject.put("message", "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was successful");
							 return jsonObject;
						}
						else
						{
							jsonObject.put("status", ERROR.GENERAL_FAIL);
							jsonObject.put("message", "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful");
							 return jsonObject;
						}
						
						
						
					}
					else if(drResJS.has("status") && drResJS.getInt("status")!=(ERROR.PAYMENT_TRANSACTION_SUCCESS))
					{
						transaction.setStatus(TransactionStatus.FAIL);
						swpService2.updateRecord(transaction);
						String message = drResJS.getString("message");
						jsonObject.put("status", ERROR.GENERAL_FAIL);
						jsonObject.put("message", message==null ? "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful" : "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful. " + message);
						 return jsonObject;
					}
				}
				else
				{
					transaction.setStatus(TransactionStatus.FAIL);
					swpService2.updateRecord(transaction);
					jsonObject.put("status", ERROR.GENERAL_FAIL);
					jsonObject.put("message", "Your payment of "+ (currency.name() + df.format(totalToPay)) +" for your Bevura Village Banking group contribution was not successful");
					 return jsonObject;
				}
			}
			else
			{
				jsonObject.put("status", ERROR.GENERAL_FAIL);
				jsonObject.put("message", "Invalid amount. To pay, your must pay more than " + currency.name() + df.format(app.getAllSettings().getDouble("maximumtransactionamountweb")));

				
	            return jsonObject;
			}
			
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		log.error(e);
    		jsonObject.put("status", ERROR.GENERAL_FAIL);
			jsonObject.put("message", "General system error");
			
            
            return jsonObject;
    	}
    	

		jsonObject.put("status", ERROR.GENERAL_FAIL);
		jsonObject.put("message", "Loan repayment was not successful. Please try again");
		return jsonObject;
	}
	
	
	
	
}