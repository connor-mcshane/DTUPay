package dk.dtu.fred.systemtest.merchant.models;

/**
 * A request used when wanting to make a transaction
 */

public class TransactionRequest {
	private String uuid;
	private String merchantCpr;
	private Double amount;
	private String comment;
	
	public TransactionRequest() {
		
	}
	
	public String getMerchantCpr() {
		return merchantCpr;
	}

	public void setMerchantCpr(String merchantCpr) {
		this.merchantCpr = merchantCpr;
	}

	public String getBarcodeUuid() {
		return uuid;
	}

	public void setBarcodeUuid(String barcodeUuid) {
		this.uuid = barcodeUuid;
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
