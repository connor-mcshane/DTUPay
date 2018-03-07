package dk.dtu.fred.systemtest.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import dk.dtu.fred.systemtest.customer.CustomerSimulator;
import dk.dtu.fred.systemtest.CreateBankClientRequest;
import dk.dtu.fred.systemtest.DTUPayResponse;

public class CreateAccountTest {

	private static final Logger logger = LoggerFactory.getLogger(CreateAccountTest.class);	
	
	public static final String dtupayUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
//	public static final String dtupayUrl = "http://localhost:8080/dtupay/rest";

	private String cpr;
	private String name;
	private CustomerSimulator simulator;
	private DTUPayResponse result;
	private String bankId; 

	@Given("^I want to create a DTUPay user considering my cpr \"([^\"]*)\"$")
	public void i_want_to_create_a_DTUPay_user_considering_my_cpr(String arg1) throws Exception {
		logger.debug("cpr: {}", arg1);
		this.cpr = arg1;
	}

	@Given("^my name \"([^\"]*)\"$")
	public void my_name(String arg1) throws Exception {
		logger.debug("name: {}", arg1);
		this.name = arg1;
		if (this.cpr.equals("987654-3210")) {
			Gson jsonLib = new Gson();
			CreateBankClientRequest requestModel = new CreateBankClientRequest();
			requestModel.setCpr(cpr);
			requestModel.setFirst(name);
			requestModel.setLast(name);
			requestModel.setBalance(10000.9);
			
			logger.debug("Sending create bank account request for model {}", requestModel);
			HttpResponse<String> resultCreateBankClient = Unirest
					.post(dtupayUrl + "/create_bank_client")
					.header("Content-Type", "application/json")
					.header("Accept", "text/plain")
					.body(jsonLib.toJson(requestModel))
					.asString();
			bankId = resultCreateBankClient.getBody();
			logger.debug("bankId: {}", bankId);
		}
	}

	@When("^DTUPay tries to create me a new account$")
	public void dtupay_tries_to_create_me_a_new_account() throws Exception {
		simulator = new CustomerSimulator();
		
		logger.debug("Sending request");
		result = simulator.createDtuPayAccount(cpr, name);
	}
	
	@Then("^DTU Pay returns status code (\\d+)$")
	public void dtu_Pay_returns_status_code(int arg1) throws Exception {		
		logger.debug("Received response code {} and expected {}", result.getResponseCode(), arg1);
		assertEquals(arg1, result.getResponseCode());
		if (arg1 == 200) {
			simulator.deleteAccount(cpr);
			logger.info("Sending delete bank account request for bankID {}", bankId);
			HttpResponse<String> resultDeleteBankId = Unirest
					.delete(dtupayUrl + "/delete_bank_client")
					.queryString("bank_id", bankId)
					.asString();
			logger.info("Result from delete bank account request for bankID {}", resultDeleteBankId.getBody());
		}
	}	

	@Then("^I receive the message \"([^\"]*)\"$")
	public void i_receive_the_message(String arg1) throws Exception {
		logger.debug("Received response rext: \"{}\" :: Expected \"{}\"", result.getResponseText(), arg1);
		assertTrue(result.getResponseText().contains(arg1));
	}
}
