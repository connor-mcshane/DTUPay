package dk.dtu.fred.systemtest.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import dk.dtu.fred.systemtest.customer.models.DTUPayCustomerResponse;
import dk.dtu.fred.systemtest.CreateBankClientRequest;


public class RequestBarcodeTest {
	
	private static final String appUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
//	private static final String appUrl = "http://localhost:8080/dtupay/rest";
	private static final Gson jsonLib = new Gson();
	private static final Logger logger = LoggerFactory.getLogger(RequestBarcodeTest.class);
	

	private String customerBankId;
	private CustomerSimulator simulator;
	private DTUPayCustomerResponse result;
	private String cpr;
	
	
	@Given("^A customer with cpr \"([^\"]*)\"$")
	public void a_customer_with_cpr(String arg1) throws Exception {
		logger.debug("cpr: {}", arg1);
	    cpr = arg1;
	}

	@When("^he requests a barcode from DTU Pay$")
	public void he_requests_a_barcode_from_DTU_Pay() throws Exception {		
		simulator = new CustomerSimulator();
		
		logger.debug("Sending request for cpr {}", cpr);
		result = simulator.getBarcode(cpr);
	}
	
	@Then("^DTU Pay returns an error code (\\d+)$")
	public void dtu_Pay_returns_an_error_code(int arg1) throws Exception {
		logger.debug("Received code {} and expected code {}", result.getResponseCode(), arg1);
		assertEquals(arg1, result.getResponseCode());
	}	

	@Given("^he is registered in DTU Pay$")
	public void he_is_registered_in_DTU_Pay() throws Exception {
		CreateBankClientRequest createCustomerBankRequest = new CreateBankClientRequest(this.cpr, "firstName", "lastName", 10000.9);
		HttpResponse<String> createBankResponse = Unirest
				.post(appUrl + "/create_bank_client")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(createCustomerBankRequest))
				.asString();
		assertEquals(201, createBankResponse.getStatus());		
		customerBankId = createBankResponse.getBody();
		logger.debug("Customer bank user created with bank ID {}", customerBankId);
		simulator = new CustomerSimulator();
		simulator.createDtuPayAccount(cpr, "name");
	}
	
	@Then("^DTU Pay returns a barcode$")
	public void dtu_Pay_returns_a_barcode() throws Exception {
		logger.debug("Received code {} and expected code {}", result.getResponseCode(), 200);
		assertEquals(200, result.getResponseCode());	  
		
		logger.debug("Received result text (expecting UUID format) \"{}\"", result.getResponseText());
	    assertNotNull(UUID.fromString(result.getResponseText()));
	    
	    if (customerBankId != null && !customerBankId.isEmpty() ) {
			HttpResponse<String> removeBankResponse = Unirest
					.delete(appUrl + "/delete_bank_client")
					.header("Accept", "text/plain")
					.queryString("bank_id", customerBankId)
					.asString();
			assertEquals(200, removeBankResponse.getStatus());		
			logger.debug("Customer bank user removed");
		}
	    
		simulator = new CustomerSimulator();
		simulator.deleteAccount(cpr);
	    
	}
}
