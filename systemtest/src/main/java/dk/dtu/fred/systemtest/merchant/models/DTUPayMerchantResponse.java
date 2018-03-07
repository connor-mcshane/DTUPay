package dk.dtu.fred.systemtest.merchant.models;

/**
 * Class used to hold the body and the response code of a response from DTUPay to the merchant.
 */
import dk.dtu.fred.systemtest.DTUPayResponse;

public class DTUPayMerchantResponse extends DTUPayResponse {

	public DTUPayMerchantResponse(int responseCode, String responseText) {
		super(responseCode, responseText);
	}
}
