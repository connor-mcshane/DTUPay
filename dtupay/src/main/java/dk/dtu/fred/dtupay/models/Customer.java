package dk.dtu.fred.dtupay.models;

public class Customer {
	/**
	 * The customer CPR number
	 */
	private String cpr;
	
	/**
	 * The costumer name
	 */
	private String name;
	
	/**
	 * Costumer bankID
	 */
	private String bankId;
	
	/**
	 * Adding a costumer to the DTUPay system.
	 * @param cpr
	 * 			The costumer CPR number
	 * @param name
	 * 			The customer name
	 * @param bankId
	 * 			The costumer bankID
	 */
	public Customer(String cpr, String name, String bankId) {
		if(cpr == null || name == null || bankId == null) {
			throw new NullPointerException("A customer's cpr and name should not be null");
		} else if(cpr.equals("") || name.equals("") || bankId.equals("")) {
			throw new IllegalArgumentException("A customer's cpr and name should contain characters");
		} else {
			this.cpr = cpr;
			this.name = name;
			this.bankId = bankId;
		}
	}
	
	/**
	 * Setting the CPR number
	 * 
	 * @param cpr
	 * 			The costumer CPR number
	 */
	public void setCpr(String cpr) {
		if(cpr == null) {
			throw new NullPointerException("A bank customer's cpr should not be null");
		} else if(cpr.equals("")) {
			throw new IllegalArgumentException("A customer's cpr should contain characters");
		} else {
			this.cpr = cpr;
		}
	}
	
	/**
	 * 
	 * @return the CPR number
	 */
	public String getCpr() {
		return this.cpr;
	}
	
	/**
	 * Set the costumer bank ID
	 * 
	 * @param bankId
	 * 			Costumer bank ID
	 */
	public void setBankId(String bankId) {
		if (bankId == null) {
			throw new NullPointerException("A bank customer's bankId should not be null");
		} else if(bankId.equals("")) {
			throw new IllegalArgumentException("A customer's bankId should contain characters");
		} else {
			this.bankId = bankId;
		}
	}
	
	/**
	 * 
	 * @return
	 * 		The costumer bank ID
	 */
	public String getBankId() {
		return this.bankId;
	}

	public void setName(String name) {
		if(name == null) {
			throw new NullPointerException("A bank customer's name should not be null");
		} else if(name.equals("")) {
			throw new IllegalArgumentException("A customer's name should contain characters");
		} else {
			this.name = name;
		}
	}
	
	/**
	 * 
	 * @return Costumer name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return formatted customer information as a string.
	 */
	@Override
	public String toString() {
		return "[cpr:{" + this.getCpr() + "}, name:{" + this.getName()  + "}, bankId:{" + this.getBankId() + "}]";
	}
	
}
