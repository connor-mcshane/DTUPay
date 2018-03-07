package dk.dtu.fred.systemtest;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import dk.dtu.fred.systemtest.DTUPayResponse;
import dk.dtu.fred.systemtest.customer.CustomerSimulator;
import dk.dtu.fred.systemtest.customer.models.CreateAccountRequest;
import dk.dtu.fred.systemtest.CreateBankClientRequest;
import dk.dtu.fred.systemtest.merchant.MerchantSimulator;

public class PayMerchantTest {

	private static final String appUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
//	private static final String appUrl = "http://localhost:8080/dtupay/rest";
	
	private static final Gson jsonLib = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(PayMerchantTest.class);
	
	private CustomerSimulator phone;
	private String customerCpr = "041093-5472";
	private String customerBankId;
	private double customerFunds = 10000.0;
	private String merchantCpr = "041093-7749";
	private String merchantBankId;
	private MerchantSimulator merchant;
	private double amount;
	private String barcode;
	private DTUPayResponse transactionResult;
	
	@Before("@pay")
	public void beforeScenario() throws Exception {
		logger.debug("Setting up");
		String firstName = "Tim";
		String lastName = "Smith";
		
		CreateBankClientRequest createCustomerBankRequest = new CreateBankClientRequest(customerCpr, firstName, lastName, customerFunds);
		HttpResponse<String> createBankResponse = Unirest
				.post(appUrl + "/create_bank_client")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createCustomerBankRequest))
				.asString();
		assertEquals(201, createBankResponse.getStatus());		
		customerBankId = createBankResponse.getBody();
		logger.debug("Customer bank user created with bank ID {}", customerBankId);
		
		CreateAccountRequest createCustomerDtupayRequest = new CreateAccountRequest();
		createCustomerDtupayRequest.setCpr(customerCpr);
		createCustomerDtupayRequest.setName(firstName + " " + lastName);
		HttpResponse<String> createDtuPayResponse = Unirest
				.post(appUrl + "/account")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createCustomerDtupayRequest))
				.asString();
		assertEquals(200, createDtuPayResponse.getStatus());
		logger.debug("Customer DTUPay account created for cpr {}", createDtuPayResponse.getBody());
		
		String merchantFirstName = "Hans";
		String merchantLastName = "Hansi";
		CreateBankClientRequest createMerchantBankRequest = new CreateBankClientRequest(merchantCpr, merchantFirstName, merchantLastName, 10000.0);
		HttpResponse<String> createMerchantBankResponse = Unirest
				.post(appUrl + "/create_bank_client")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createMerchantBankRequest))
				.asString();
		assertEquals(201, createMerchantBankResponse.getStatus());
		merchantBankId = createMerchantBankResponse.getBody();
		logger.debug("Merchant bank user created with bank ID {}", merchantBankId);	
		
		CreateAccountRequest createMerchantDtupayRequest = new CreateAccountRequest();
		createMerchantDtupayRequest.setCpr(merchantCpr);
		createMerchantDtupayRequest.setName(merchantFirstName + " " + merchantLastName);
		HttpResponse<String> createMerchantDtuPayResponse = Unirest
				.post(appUrl + "/account")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createMerchantDtupayRequest))
				.asString();
		assertEquals(200, createMerchantDtuPayResponse.getStatus());
		logger.debug("Merchat DTUPay account created for cpr {}", createMerchantDtuPayResponse.getBody());
		
		logger.debug("Setup finished");
	}
	
	@After("@pay")
	public void afterScenario() throws Exception {
		logger.debug("Cleaning up");
		
		if (customerBankId != null && !customerBankId.isEmpty() ) {
			HttpResponse<String> removeBankResponse = Unirest
					.delete(appUrl + "/delete_bank_client")
					.header("Accept", "text/plain")
					.queryString("bank_id", customerBankId)
					.asString();
			assertEquals(200, removeBankResponse.getStatus());		
			logger.debug("Customer bank user removed");	
		}
		
		if (customerCpr != null && !customerCpr.isEmpty() ) {
			HttpResponse<String> removeDtuPayResponse = Unirest
					.delete(appUrl + "/delete_account")
					.queryString("cpr", customerCpr)
					.asString();
			assertEquals(200, removeDtuPayResponse.getStatus());
			logger.debug("Customer DTUPay account removed");	
		}		
		
		if (merchantBankId != null && !merchantBankId.isEmpty() ) {
			HttpResponse<String> removeBankResponse = Unirest
					.delete(appUrl + "/delete_bank_client")
					.header("Accept", "text/plain")
					.queryString("bank_id", merchantBankId)
					.asString();
			assertEquals(200, removeBankResponse.getStatus());		
			logger.debug("Merchant bank user removed");	
		}
		
		if (merchantCpr != null && !merchantCpr.isEmpty() ) {
			HttpResponse<String> removeDtuPayResponse = Unirest
					.delete(appUrl + "/delete_account")
					.queryString("cpr", merchantCpr)
					.asString();
			assertEquals(200, removeDtuPayResponse.getStatus());
			logger.debug("Merchant DTUPay account removed");	
		}
		
		logger.debug("Cleaning finished");
	}
	
	@Given("^I am a DTUPay customer with a valid barcode$")
	public void i_am_a_DTUPay_customer_with_a_valid_barcode() throws Exception {
		phone = new CustomerSimulator();		
		DTUPayResponse result = phone.getBarcode(customerCpr);
		assertEquals(200, result.getResponseCode());
				
		barcode = result.getResponseText();
		logger.debug("Received barcode id {}", barcode);
	}

	@Given("^a merchant with a bank account$")
	public void a_merchant_with_a_bank_account() throws Exception {				
		merchant = new MerchantSimulator(merchantCpr);
	}

	@When("^I show the merchant my barcode to pay (\\d+)$")
	public void i_show_the_merchant_my_barcode_to_pay(double arg1) throws Exception {	
		amount = arg1;
	}

	@When("^the merchant requests a transaction with comment \"([^\"]*)\"$")
	public void the_merchant_requests_a_transaction_with_comment(String arg1) throws Exception {
		logger.debug("Initiating transaction with comment \"{}\"", arg1);
		transactionResult = merchant.intiateTransaction(barcode, amount, arg1);
	}

	@Then("^the merchant receives a confirmation$")
	public void the_merchant_receives_a_confirmation() throws Exception {
		logger.debug("Transaction result: {}", transactionResult.getResponseText());
	    assertEquals(200, transactionResult.getResponseCode());	    
	}
	
	@Given("^I am a DTUPay customer with an invalid barcode$")
	public void i_am_a_DTUPay_customer_with_an_invalid_barcode() throws Exception {
		phone = new CustomerSimulator();		
		barcode = UUID.randomUUID().toString();
		logger.debug("Invalid UUID is {}", barcode);
	}

	@Then("^the merchant receives an error (\\d+)$")
	public void the_merchant_receives_an_error(int arg1) throws Exception {	    
		assertEquals(arg1, transactionResult.getResponseCode());
		logger.debug("Correctly received error [{}] {}", transactionResult.getResponseCode(), transactionResult.getResponseText());
	}	
	
	@When("^I show the merchant my barcode to pay more than my funds$")
	public void i_show_the_merchant_my_barcode_to_pay_more_than_my_funds() throws Exception {
	    amount = customerFunds * 2;
	}


}
