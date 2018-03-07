package dk.dtu.fred.systemtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import dk.dtu.fred.systemtest.customer.CustomerSimulator;
import dk.dtu.fred.systemtest.merchant.MerchantSimulator;
import dk.dtu.fred.systemtest.customer.models.CreateAccountRequest;
import dk.dtu.fred.systemtest.CreateBankClientRequest;
import dk.dtu.fred.systemtest.DTUPayResponse;

public class RequestLastTransactionAmountTest {

	private static final Logger logger = LoggerFactory.getLogger(RequestLastTransactionAmountTest.class);	
	private static final Gson jsonLib = new Gson();	

	private CustomerSimulator customerSimulator;
	private MerchantSimulator merchantSimulator;

	private String barcode;
	private Double transactionAmount = 20.0;
	private String firstName = "ClientFirstName";
	private String lastName = "ClientLastName";
	private String customerCpr;
	private Double customerFunds = 20000.9;
	private String customerBankId;
	private String merchantCpr = "459273-0072";
	private String merchantBankId;
	
	private static final String appUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
	private DTUPayResponse result;
	private DTUPayResponse customerResult;
	private DTUPayResponse merchantResult;

	@Before("@amount")
	public void beforeScenario() throws Exception {
		customerSimulator = new CustomerSimulator();
		merchantSimulator = new MerchantSimulator(merchantCpr);
		
		logger.debug("Setting up starting for RequestLastTransactionAmountTest");
		
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
		
		logger.debug("Setup finished for RequestLastTransactionAmountTest");
	}
	
	@After("@amount")
	public void afterScenario() throws Exception {
		logger.debug("Cleaning up started for RequestLastTransactionAmountTest");
		
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
		
		logger.debug("Cleaning finished for RequestLastTransactionAmountTest");
	}
	
	@Given("^I want to get the amount of my last transaction considering my \"([^\"]*)\"$")
	public void i_want_to_get_the_amount_of_my_last_transaction_considering_my(String arg1) throws Exception {
		logger.debug("cpr: {}", arg1);
		this.customerCpr = arg1;
		
		CreateBankClientRequest createCustomerBankRequest = new CreateBankClientRequest(this.customerCpr, firstName, lastName, customerFunds);
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
		createCustomerDtupayRequest.setCpr(this.customerCpr);
		createCustomerDtupayRequest.setName(firstName + " " + lastName);
		HttpResponse<String> createDtuPayResponse = Unirest
				.post(appUrl + "/account")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createCustomerDtupayRequest))
				.asString();
		assertEquals(200, createDtuPayResponse.getStatus());
		logger.debug("Customer DTUPay account created for cpr {}", createDtuPayResponse.getBody());
		
		if(this.customerCpr.equals("926489-4368")) {
			result = customerSimulator.getBarcode(this.customerCpr);
			this.barcode = result.getResponseText();
			merchantSimulator.intiateTransaction(barcode, this.transactionAmount, "This transaction has been initiated for RequestLastTransactionAmountTest");
		}
	}

	@When("^initializing the request to DTUPay$")
	public void initializing_the_request_to_DTUPay() throws Exception {
		logger.debug("Sending request");
		customerResult = customerSimulator.getLastTransactionAmount(this.customerCpr);
		merchantResult = merchantSimulator.getLastTransactionAmount(this.merchantCpr);
	}

	@Then("^DTUPay returns the response \"([^\"]*)\"$")
	public void dtupay_returns_the_value(String arg1) throws Exception {
		logger.debug("Received customer response amount: \"{}\" :: Expected \"{}\"", this.customerResult.getResponseText(), arg1);
		logger.debug("Received merchant response amount: \"{}\" :: Expected \"{}\"", this.merchantResult.getResponseText(), arg1);
		assertTrue(this.customerResult.getResponseText().contains(arg1));
		assertTrue(this.merchantResult.getResponseText().contains(arg1));
	}
}
