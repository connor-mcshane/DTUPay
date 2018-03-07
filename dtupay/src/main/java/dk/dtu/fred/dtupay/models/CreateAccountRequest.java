package dk.dtu.fred.dtupay.models;

import java.io.Serializable;

/**
 * A request used when wanting to create an account in DTUPay
 */
public class CreateAccountRequest implements Serializable {
	private static final long serialVersionUID = -195461453891852464L;
	
	private String name;
	private String cpr;
		
	
	public CreateAccountRequest() {
	}
	
	/**
	 * 
	 * @param name
	 * 			The user name
	 * @param cpr
	 * 			The CPR number
	 */
	public CreateAccountRequest(String name, String cpr) {
		super();
		this.name = name;
		this.cpr = cpr;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getCpr() {
		return cpr;
	}
	public void setCpr(String cpr) {
		this.cpr = cpr;
	}
	
	@Override
	public String toString() {
		return "[cpr:{" + this.getCpr() + "}, name:{" + this.getName()  + "}]";
	}
	
	
}
