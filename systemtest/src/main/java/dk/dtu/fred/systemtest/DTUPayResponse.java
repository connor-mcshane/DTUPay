package dk.dtu.fred.systemtest;

/**
 * Class used to hold the body and the response code of a response from DTUPay.
 */
public abstract class DTUPayResponse {
	private final int responseCode;
	private final String responseText;
	
	public DTUPayResponse(int responseCode, String responseText) {
		this.responseCode = responseCode;
		this.responseText = responseText;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseText() {
		return responseText;
	}
}
