package com.probase.probasepay.util;

import java.util.Collection;

public class ERROR {

	/*JWT*/
	public static final int FORCE_LOGOUT_USER = -1;
	
	/*Customers*/
	public static final int CUSTOMER_CREATE_SUCCESS = 100;
	public static final int CUSTOMER_CREATION_DOB_FAILED = 102;
	public static final int CUSTOMER_CREATION_FAILED = 101;
	public static final int CUSTOMER_LIST_FETCH_SUCCESS = 110;
	public static final int CUSTOMER_LIST_FETCH_FAIL = 111;
	public static final int CUSTOMER_CREATE_SUCCESS_NO_USER_ACCOUNT = 112;
	public static final int CUSTOMER_ACCOUNT_ADDITION_SUCCESSFUL = 113;
	public static final int CUSTOMER_ACCOUNT_UPDATE_SUCCESSFUL = 114;
	public static final int BATCH_CUSTOMER_CREATE_SUCCESS = 115;
	public static final int BATCH_CUSTOMER_CREATE_FAIL = 116;
	/*Merchants*/
	public static final int MERCHANT_SETUP_SUCCESS = 200;
	public static final int MERCHANT_SETUP_FAIL = 201;
	public static final int MERCHANT_LIST_FETCH_SUCCESS = 210;
	public static final int MERCHANT_LIST_FETCH_FAIL = 211;
	public static final int MERCHANT_UPDATE_STATUS_SUCCESS = 220;
	public static final int MERCHANT_UPDATE_STATUS_FAIL_NO_MERCHANT = 221;
	public static final int MERCHANT_UPDATE_STATUS_FAIL = 222;
	public static final int MERCHANT_SETUP_SUCCESS_NO_USER_ACCOUNT = 223;
	public static final int MERCHANT_SETUP_COMPANY_NAME_EXIST = 224;
	/*EWaller*/
	public static final int EWALLET_ACTIVATION_SUCCESS = 300;
	public static final int EWALLET_ACTIVATION_FAIL = 301;
	public static final int EWALLET_ACTIVATION_INVALID_CODE = 302;
	/*MMoney*/
	public static final int MMONEY_PROFILE_SUCCESS = 400;
	public static final int MMONEY_PROFILE_FAIL_INVALID_MOBILE = 401;
	public static final int MMONEY_PROFILE_FAIL = 402;
	public static final int MMONEY_ADD_SUCCESS = 410;
	public static final int MMONEY_ADD_FAIL = 411;
	public static final int MMONEY_ADD_FAIL_PAN_MISMATCH = 412;
	public static final int MMONEY_ADD_FAIL_MMONEY_EXIST = 413;
	public static final int MMONEY_STATUS_UPDATED = 414;
	public static final int MMONEY_STATUS_FAIL = 415;
	public static final int MMONEY_ACCOUNT_ACTIVATED = 416;
	public static final int MMONEY_ACCOUNT_ACTIVATE_FAIL = 417;
	/*User Accounts*/
	public static final int USER_ACCOUNT_ADD_SUCCESSFUL = 500;
	public static final int USER_ACCOUNT_ADD_FAIL = 501;
	public static final int USER_STATUS_UPDATE_SUCCESS = 502;
	public static final int USER_STATUS_UPDATE_FAIL = 503;
	public static final int USER_STATUS_UPDATE_FAIL_MOBILE_NUMBER_TAKEN = 504;
	/*User Account Authenticate*/
	public static final int AUTHENTICATE_FAIL = 601;
	public static final int AUTHENTICATE_OK = 600;
	public static final int NEW_PASSWORD_SET = 602;
	public static final int NEW_PASSWORD_SET_FAILED = 603;
	public static final int AUTHENTICATE_OK_PIN_NEEDED = 604;
	/*Devices*/
	public static final int DEVICE_ADD_SUCCESS = 700;
	public static final int DEVICE_ADD_FAIL = 701;
	public static final int DEVICE_LIST_FETCH_SUCCESS = 710;
	public static final int DEVICE_LIST_FETCH_FAIL = 711;
	/*Bank Staff*/
	public static final int BANK_STAFF_CREATE_SUCCESS = 800;
	public static final int BANK_STAFF_CREATE_FAIL = 801;
	/*Transaction*/
	public static final int OTP_GENERATE_SUCCESS = 900;
	public static final int BALANCE_INADEQUATE = 901;
	public static final int INVALID_TXN_AMOUNT = 902;
	public static final int HASH_FAIL = 903;
	public static final int EXPIRED_CARD = 904;
	public static final int INVALID_EXPIRY_DATE = 905;
	public static final int MERCHANT_EXIST_FAIL = 906;
	public static final int MERCHANT_PLUS_DEVICE_STATUS_FAIL = 907;
	public static final int CVV_FAIL = 908;
	public static final int DATA_INCONSISTENCY = 909;
	public static final int PAYMENT_TRANSACTION_SUCCESS = 0;
	public static final int TRANSACTION_NOT_FOUND = 911;
	public static final int MERCHANT_PLUS_VENDOR_SERVICE_STATUS_FAIL = 912;
	public static final int MERCHANT_VENDOR_SERVICE_INVALID = 913;
	public static final int TRANSACTION_STATUS_UPDATED = 914;
	public static final int CARD_NOT_VALID = 915;
	/*Payment Interface Data*/
	public static final int PAYMENT_INTERFACE_PULL_SUCCESS = 1000;
	public static final int MERCHANT_NOT_EXIST = 1001;
	public static final int PAYMENT_INITIATION_SUCCESS = 1002;
	/*General Transaction*/
	public static final int TRANSACTION_SUCCESS = 0;
	public static final int INSUFFICIENT_FUNDS = 1;
	public static final int OTP_CHECK_FAIL = 2;
	public static final int TRANSACTION_FAIL = 3;
	/*EWallet Transactions*/
	public static final int EWALLET_TRANSFER_INVALID_REC_ACCOUNT = 2001;
	public static final int EWALLET_TRANSFER_DESTINATION_STATUS_INACTIVE = 2002;
	public static final int EWALLET_BALANCE_PULL_SUCCESS = 2003;
	public static final int EWALLET_ACCOUNTS_NO_EXIST = 2004;
	/*Card Scheme*/
	public static final int CARD_SCHEME_FETCH_SUCCESS = 2100;
	public static final int CARD_SCHEME_FETCH_FAIL = 2101;
	public static final int CARD_SCHEME_CREATED_SUCCESS = 2102;
	public static final int CARD_SCHEME_UPDATED_SUCCESS = 2103;
	/*OTP*/
	public static final int OTP_TIMEOUT = 3100;
	/*CARD*/
	public static final int CARD_BALANCE_SUCCESS = 6000;
	public static final int CARD_NOT_PROFILED = 6001;

	public static final int GENERAL_SYSTEM_FAIL = 99;

	

	public static int API_KEY_FAIL = 4000;
	public static int RESPONSE_URL_NOT_PROVIDED = 4001;
	public static int HASH_NOT_PROVIDED= 4002;
	public static int ORDER_ID_NOT_PROVIDED= 4003;
	public static int TRANSACTION_AMOUNT_INVALID= 4004;
	public static int SERVICE_TYPE_NOT_PROVIDED= 4005;
	public static int MERCHANT_DEVICE_FAIL= 4006;
	public static int HASH_FAIL_VALIDATION= 4007;
	public static int INVALID_TRANSACTION = 4008;
	public static int INVALID_DEVICE_DETAIL = 4009;
	
	
	public static int GENERAL_OK = 5000;
	public static int GENERAL_FAIL = 5001;

	public static int INVALID_PARAMETERS = 50100;

	public static int SETTLEMENT_ACCOUNT_NOT_FOUND = 50101;

	public static int CUSTOMER_ID_NO_ALREADY_EXISTS = 50102;

	public static int INVALID_QR_CODE = 50103;

	public static int TWOFA_ENABLED = 50104;

	public static int TWOFA_DISABLED = 50105;

	public static Collection<?> GROUP_LOAN_REPAYMENT_SCHEDULE_NOT_FOUND;

	public static Collection<?> FUNDS_CANT_SERVICE_LOAN;




	




	

	
	public static final int PAYMENT_TRANSACTION_PAYER_CANCELED = 5002;
	public static final int PAYMENT_TRANSACTION_PAYER_FAILED = 5003;
	public static final int WALLET_ALREADY_EXISTS = 5004;
	public static final int INCOMPLETE_TOTAL_AMOUNT_DEBIT = 5005;
	public static final int DEVICE_EXIST_FAIL = 5006;

	public static final int WALLET_CREATION_FAIL = 5007;

	public static final int INVALID_WALLET_PROVIDED = 5008;
	public static final int MERCHANT_BANK_ACCOUNT_NO_EXIST = 5009;

	public static final int INVALID_CURRENCY_PROVIDED = 5010;

	public static final int COUNTRY_OF_OPERATION_NOT_PROVIDED = 5011;

	public static final int OTP_GENERATE_FAIL = 5012;

	public static final int CUSTOMER_NOT_FOUND = 7800;
	public static final int CUSTOMER_ACCOUNT_NOT_FOUND = 7801;
	public static final int CUSTOMER_NOT_ACTIVE = 7802;
	
	
	
	/*Tutuka*/
	public static final int INVALID_XML_MESSAGE = 1000001;
	public static final int CARD_STOPPED_SUCCESSFULLY = 1000002;
	public static final int CARD_ALREADY_STOPPED = 1000003;
	public static final int CARD_CREATED_SUCCESSFULLY = 1000004;
	public static final int CARD_NOT_CREATED_SUCCESSFULLY = 1000005;

	public static final int CARD_COULD_NOT_BE_LINKED = 1000006;

	public static final int CARD_LINKED_SUCCESSFULLY = 1000007;

	public static final int CARD_ALREADY_LINKED = 1000008;

	public static final int CARD_LOST = 1000009;

	public static final int CARD_STOLEN = 1000010;

	public static final int PIN_TRIES_EXCEEDED = 1000011;

	public static final int CARD_ORDER_SUCCESSFUL = 1000012;

	public static final int CARD_ACTIVATION_FAILED_DUE_TO_STATUS = 1000013;

	public static final int CARD_ACTIVATION_SUCCESS = 1000014;

	public static final int CARD_TRANSFERRED_SUCCESSFULLY = 1000015;

	public static final int DEBIT_SUCCESSFUL = 1000016;

	public static final int DEBIT_FAILED = 10000161;

	public static final int DEBIT_FAIL_INSUFFICIENT_FUNDS = 1000017;

	public static final int TRANSACTION_DATA_UNPARSEABLE = 1000018;

	public static final int CARD_NOT_FOUND = 1000019;

	public static final int DEBIT_TRANSACTION_AMOUNT_EXCEEDS_LIMIT = 1000020;

	public static final int DEBIT_CARD_REVERSAL_SUCCESS = 1000021;

	public static final int INVALID_OPERATION = 1000022;

	public static final int DEBIT_ADJUSTMENT_SUCCESS = 1000023;

	public static final int DEBIT_ADJUSTMENT_SUCCESS_OVERDEBIT = 1000024;

	public static final int DEBIT_FAIL_CARD_NOT_CREDITED_PREVIOUSLY = 1000025;

	public static final int INVALID_CARD_TYPE = 1000026;

	public static final int CARD_RETIRED_SUCCESSFULLY = 1000027;

	public static final int CARD_RETIRE_FAIL = 1000028;

	public static final int CARD_UNSTOPPED_SUCCESSFULLY = 1000028;
	
	public static final int CARD_UNSTOP_FAIL = 1000029;

	public static final int CARD_CVV_UPDATED_SUCCESSFULLY = 1000030;

	public static final int CARD_CVV_UPDATE_FAIL = 1000031;

	public static final int ACTIVE_LINKED_CARDS_UNAVAILABLE = 1000032;

	public static final int ACTIVE_LINKED_CARDS_AVAILABLE = 1000033;

	public static final int CARD_BEARER_MISMATCH = 1000034;

	public static final int DEBIT_FAIL_INCORRECT_CVV_MASTERCARD = 1000035;

	public static final int DEBIT_FAIL_INCORRECT_CVV_VISA = 1000036;

	public static final int DEBIT_FAIL_INCORRECT_EXP_DATE = 1000037;

	public static final int CHECKSUM_MISMATCH = 1000038;

	public static final int LOAD_ADJUSTMENT_SUCCESS = 1000039;

	public static final int CARD_MISMATCH = 1000040;

	public static final int LOAD_REVERSAL_SUCCESS = 1000041;

	public static final int LOAD_AUTH_SUCCESS = 1000042;

	public static final int LOAD_AUTH_REVERSAL_SUCCESS = 1000043;

	public static final int INCOMPLETE_PARAMETERS = 1000044;

	public static final int MPQR_ACCOUNT_CREATED_SUCCESSFULLY = 1000045;

	public static final int MPQR_ACCOUNT_NOT_CREATED_SUCCESSFULLY = 1000046;

	public static final int MPQR_ACCOUNT_DISABLED_SUCCESSFULLY = 1000047;

	public static final int MPQR_ACCOUNT_NOT_DISABLED_SUCCESSFULLY = 1000048;

	public static final int ACCOUNT_CREDITED_SUCCESSFULLY = 1000049;

	public static final int ACCOUNT_CREDIT_REVERSED_SUCCESSFULLY = 1000050;

	public static final int DEVICE_MPQR_EXIST = 1000051;

	public static final int MERCHANT_SCHEME_NOT_FOUND = 6000051;

	public static final int INVALID_BANK_PROVIDED = 6000052;

	public static final int ACQUIRER_NOT_FOUND = 6000053;

	public static final int POOL_ACCOUNT_NOT_FOUND = 6000055;

	public static final int CARD_BIN_NOT_FOUND = 6000056;

	public static final int BANK_CREATE_FAIL = 6000057;

	public static final int BANK_CREATE_SUCCESS = 6000058;

	public static final int ACQURIER_CREATE_SUCCESS = 6000059;
	
	public static final int ACQUIRER_CREATE_FAIL = 6000060;

	public static final int BILL_ID_VALIDATED_SUCCESS = 6000061;

	public static final int BILL_ID_NOT_VALIDATED_SUCCESS = 6000062;

	public static final int CARDS_NOT_PROVIDED = 6000063;

	public static final int WALLET_NOT_PROFILED = 6000064;

	public static final int WALLET_DEBIT_FAILED = 6000065;

	public static final int WALLET_CREDIT_FAILED = 6000066;

	public static final int WALLET_CREDIT_SUCCESS = 6000067;

	public static final int DEBIT_SUCCESSFUL_CREDIT_FAILED = 6000068;

	public static final int WRONG_API_USED = 6000069;

	public static final int CUSTOMER_WALLET_EXISTS = 6000070;


	

	public static final int GROUP_LOAN_NOT_SETUP = 809200001;

	public static final int CONTRIBUTION_PACKAGE_NOT_FOUND = 809200002;

	public static final int GROUP_MEMBER_NOT_EXISTS = 809200003;

	public static final int GROUP_MEMBER_HAS_LOANS = 809200004;

	public static final int INVALID_REPAYMENT_PERIOD = 809200005;

	public static final int GROUP_LOAN_NOT_FOUND = 809200006;

	public static final int GROUP_NAME_EXISTS = 809200007;

	public static final int GROUP_NOT_FOUND = 809200008;

	public static final int INSUFFICIENT_PRIVILEDGES = 809200009;

	public static final int GROUP_MEMBER_EXISTS = 809200010;

	public static final int USER_NOT_FOUND = 809200011;

	public static final int GROUP_NOT_OPEN_TO_EVERYONE = 809200012;

	public static final int INSUFFICIENT_FUNDS_IN_WALLET = 809200013;

	public static final int EXPECTED_GROUP_PAYMENT_NOT_FOUND = 809200014;

	public static final int GROUP_PAYMENT_COMPLETED = 809200015;

	

	
	

}
