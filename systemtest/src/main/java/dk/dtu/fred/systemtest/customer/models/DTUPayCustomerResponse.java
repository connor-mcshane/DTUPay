package dk.dtu.fred.systemtest.customer.models;

/**
 * Class used to hold the body and the response code of a response from DTUPay to the customer.
 */

import dk.dtu.fred.systemtest.DTUPayResponse;

public class DTUPayCustomerResponse extends DTUPayResponse {

	public DTUPayCustomerResponse(int responseCode, String responseText) {
		super(responseCode, responseText);
	}

}
