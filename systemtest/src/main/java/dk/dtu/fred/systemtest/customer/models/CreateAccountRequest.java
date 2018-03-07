package dk.dtu.fred.systemtest.customer.models;

/**
 * A request used when wanting to create an account in DTUPay
 */
public class CreateAccountRequest {
	/**
	 * The CPR number of the user
	 */
	private String cpr;
	/**
	 * The user name
	 */
	private String name;
	
	public String getCpr() {
		return cpr;
	}
	public void setCpr(String cpr) {
		this.cpr = cpr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[cpr:{" + (cpr == null ? "null" : cpr) + "}, name:{" + (name == null ? "null" : name) + "}]";
	}
}
