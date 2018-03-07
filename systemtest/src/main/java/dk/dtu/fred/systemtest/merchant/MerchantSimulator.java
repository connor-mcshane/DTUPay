package dk.dtu.fred.systemtest.merchant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import dk.dtu.fred.systemtest.merchant.models.DTUPayMerchantResponse;
import dk.dtu.fred.systemtest.merchant.models.TransactionRequest;
import dk.dtu.fred.systemtest.DTUPayResponse;

/**
 * A simulator used in the system test to simulate the behaviour of a merchant.
 */
public class MerchantSimulator {
	/**
	 * The destination URL which the simulator is connecting to.
	 */
	public static final String dtupayUrl = "http://delhi4.compute.dtu.dk:8080/dtupay/rest";
//	public static final String dtupayUrl = "http://localhost:8080/dtupay/rest";
	
	/**
	 * A logger which logs the activity
	 */
	private static final Logger logger = LoggerFactory.getLogger(MerchantSimulator.class);
	
	/**
	 * JSON library
	 */
	private Gson jsonLib;
	private String cpr;
	
	public MerchantSimulator(String cpr) {
		jsonLib = new Gson();
		this.cpr = cpr;
	}
	
	/**
	 * Initiate a transaction.
	 * 
	 * @param uuid
	 * 		The UUID which identifies the barcode requested by the customer
	 * 
	 * @param amount
	 * 		The transaction's amount 
	 * 
	 * @param comment
	 * 		A comment supplied by the merchant
	 * 
	 * @return A {@link DTUPayResponse} which holds the text and the status.
	 */
	public DTUPayMerchantResponse intiateTransaction(String uuid, Double amount, String comment) throws UnirestException {
		TransactionRequest requestModel = new TransactionRequest();
		
		requestModel.setBarcodeUuid(uuid);
		requestModel.setMerchantCpr(cpr);
		requestModel.setAmount(amount);
		requestModel.setComment(comment);
		
		logger.info("Sending transaction request for model {}", requestModel);
		
		HttpResponse<String> result = Unirest
				.post(dtupayUrl + "/transaction")
				.header("Content-Type", "application/json")
				.header("Accept", "text/plain")
				.body(jsonLib.toJson(requestModel))
				.asString();
		return new DTUPayMerchantResponse(result.getStatus(), result.getBody());
	}
	
	/**
	 * A method to simulate a user getting the last recorded transaction.
	 * 
	 * @param cpr
	 * 			The CPR number of the user.
	 * 
	 * @return A {@link DTUPayResponse} which holds the text and the status. 
	 */
	public DTUPayMerchantResponse getLastTransactionAmount(String cpr) throws UnirestException {
		logger.info("Sending last transaction amount request for cpr {}", cpr);
		HttpResponse<String> result = Unirest
				.get(dtupayUrl + "/last_transaction_amount")
				.queryString("cpr", cpr)
				.asString();
		return new DTUPayMerchantResponse(result.getStatus(), result.getBody());
	}
}
