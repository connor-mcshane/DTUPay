package dk.dtu.fred.systemtest.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import dk.dtu.fred.systemtest.customer.models.CreateAccountRequest;
import dk.dtu.fred.systemtest.customer.models.DTUPayCustomerResponse;
import dk.dtu.fred.systemtest.DTUPayResponse;

/**
 * A simulator used in the system test to simulate the behaviour of a customer.
 */
public class CustomerSimulator {
	/**
	 * The destination URL which the simulator is connecting to.
	 */
	public static final String dtupayUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
	// public static final String dtupayUrl = "http://localhost:8080/dtupay/rest";

	/**
	 * A logger which logs the activity
	 */
	private static final Logger logger = LoggerFactory.getLogger(CustomerSimulator.class);

	/**
	 * JSON library
	 */
	private Gson jsonLib;

	public CustomerSimulator() {
		jsonLib = new Gson();
	}

	/**
	 * Creates a DTUPay account.
	 * 
	 * @param cpr
	 *            The CPR number which the DTUPay account should contain.
	 * @param name
	 *            The name which the DTUPay account should contain.
	 */
	public DTUPayCustomerResponse createDtuPayAccount(String cpr, String name) throws UnirestException {
		CreateAccountRequest requestModel = new CreateAccountRequest();
		requestModel.setCpr(cpr);
		requestModel.setName(name);

		logger.info("Sending create account request for model {}", requestModel);
		HttpResponse<String> result = Unirest.post(dtupayUrl + "/account").header("Content-Type", "application/json")
				.header("Accept", "text/plain").body(jsonLib.toJson(requestModel)).asString();
		return new DTUPayCustomerResponse(result.getStatus(), result.getBody());
	}

	/**
	 * Returns a user barcode.
	 * 
	 * @param cpr
	 *            The CPR number of the user which barcode is wanted to retreive.
	 * 
	 * @return A {@link DTUPayResponse} which holds the text and the status.
	 */
	public DTUPayCustomerResponse getBarcode(String cpr) throws UnirestException {
		logger.info("Sending barcode request for cpr {}", cpr);
		HttpResponse<String> result = Unirest.get(dtupayUrl + "/barcode").queryString("cpr", cpr).asString();
		return new DTUPayCustomerResponse(result.getStatus(), result.getBody());
	}

	/**
	 * Customer A method to simulate a user getting the last recorded transaction.
	 * 
	 * @param cpr
	 *            The CPR number of the user.
	 * 
	 * @return A {@link DTUPayResponse} which holds the text and the status.
	 */
	public DTUPayResponse getLastTransactionAmount(String cpr) throws UnirestException {
		logger.info("Sending last transaction amount request for cpr {}", cpr);
		HttpResponse<String> result = Unirest.get(dtupayUrl + "/last_transaction_amount").queryString("cpr", cpr)
				.asString();
		return new DTUPayCustomerResponse(result.getStatus(), result.getBody());
	}

	/**
	 * Method to simulate a user requesting to delete its DTUPay account.
	 * @param cpr
	 * 			The CPR number of the user.
	 * @return A {@link DTUPayResponse} which holds the text and the status.
	 * @throws UnirestException
	 */
	public DTUPayCustomerResponse deleteAccount(String cpr) throws UnirestException {
		logger.info("Sending delete account request for cpr {}", cpr);
		HttpResponse<String> result = Unirest.delete(dtupayUrl + "/delete_account").queryString("cpr", cpr).asString();
		return new DTUPayCustomerResponse(result.getStatus(), result.getBody());
	}
}
