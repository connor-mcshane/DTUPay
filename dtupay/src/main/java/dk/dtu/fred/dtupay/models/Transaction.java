package dk.dtu.fred.dtupay.models;

import java.util.Date;


/**
 * Class for transactions between customer and merchant.
 */

public class Transaction {
	private String ID;
	private Date date;
	private Double amount;
	private String customerID;
	private String merchantID;

	/**
	 * Constructor of a transaction object.
	 * @param customerID
	 * 			The costumer of DTUPay
	 * @param amount
	 * 			The amount wanting to transfer
	 */
	public Transaction(String customerID, Double amount) {
		if(customerID == null || amount == null) {
			throw new NullPointerException("CustomerID and amount should not be null");
		} else if(customerID.isEmpty()) {
			throw new IllegalArgumentException("CustomerID should not be empty");
		} else if(amount <= 0) {
			throw new IllegalArgumentException("Transaction amount should be greater than 0");
		} else {
			this.ID = new String();
			this.date = new Date();
			this.amount = amount;
			this.customerID = customerID;
		}
	}

	public String getID() {
		return this.ID;
	}
	
	public void setID(String id) {
		if (id == null || id.isEmpty()) {
			throw new NullPointerException("Transaction id should not be null or empty");
		} else {
			this.ID = id;
		}
	}
	
	public Date getDate() {
		return this.date;
	}

	public void setDate() {
		this.date = new Date();
	}

	public Double getAmount() {
		return this.amount;
	}

	public void setAmount(Double amount) {
		if(amount == null) {
			throw new NullPointerException("Transaction amount should not be null");
		} else if(amount <= 0) {
			throw new IllegalArgumentException("Transaction amount should be greater than 0");
		} else {
			this.amount = amount;
		}
	}
	
	public String getCostumerID() {
		return this.customerID;
	}

	public String getMerchantID() {
		if(this.merchantID == null) {
			throw new NullPointerException("MerchantID has not yet been initialized");
		} else {
			return this.merchantID;
		}
	}

	public void setMerchantID(String merchantID) {
		if(merchantID == null) {
			throw new NullPointerException("CustomerID and amount should not be null");
		} else if(merchantID.isEmpty()) {
			throw new IllegalArgumentException("CustomerID should not be empty");
		} else {
			this.merchantID = merchantID;
		}
	}
}
