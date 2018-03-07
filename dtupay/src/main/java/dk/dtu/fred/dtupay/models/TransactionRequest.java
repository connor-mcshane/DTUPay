package dk.dtu.fred.dtupay.models;

import java.io.Serializable;

/**
 * A request used when wanting to perform a transaction
 *
 */
public class TransactionRequest implements Serializable {
	private static final long serialVersionUID = -195461453891852464L;
	
	private String uuid;
	private String merchantCpr;
	private Double amount;
	private String comment;
	
	public TransactionRequest() {
		
	}
	
	/**
	 * 
	 * @param uuid
	 * 			The UUID
	 * @param merchantCpr
	 * 			The merchant CPR
	 * @param amount
	 * 			The amount which
	 * @param comment
	 * 			A comment added to the transaction
	 */
	public TransactionRequest(String uuid, String merchantCpr, Double amount, String comment) {
		super();
		this.uuid = uuid;
		this.merchantCpr = merchantCpr;
		this.amount = amount;
		this.comment = comment;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getMerchantCpr() {
		return merchantCpr;
	}

	public void setMerchantCpr(String merchantCpr) {
		this.merchantCpr = merchantCpr;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}	
}


