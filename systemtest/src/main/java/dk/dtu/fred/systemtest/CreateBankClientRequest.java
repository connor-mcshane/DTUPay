package dk.dtu.fred.systemtest;

import java.io.Serializable;

/**
 *  Request object used when creating a bank client.
 */
public class CreateBankClientRequest implements Serializable {
	
		private static final long serialVersionUID = -195461453891852464L;
		/**
		 * CPR number of the customer
		 */
		private String cpr;
		/**
		 * First name of the customer
		 */
		private String first;
		/**
		 * Last name of the customer
		 */
		private String last;
		/**
		 * Initial balance of the customer
		 */
		private double balance;
			
		
		public CreateBankClientRequest() {
			
		}
		
		/**
		 * 
		 * @param cpr
		 * 			CPR number of the customer
		 * @param first
		 * 			First name of the customer
		 * @param last
		 * 			Last name of the customer
		 * @param balance
		 * 			Initial balance of the customer
		 */
		public CreateBankClientRequest(String cpr, String first, String last, Double balance) {
			this.cpr = cpr;
			this.first = first;
			this.last = last;
			this.balance = balance;
		}

		public String getCpr() {
			return cpr;
		}

		public void setCpr(String cpr) {
			this.cpr = cpr;
		}

		public String getFirst() {
			return first;
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public String getLast() {
			return last;
		}

		public void setLast(String last) {
			this.last = last;
		}

		public double getBalance() {
			return balance;
		}

		public void setBalance(double balance) {
			this.balance = balance;
		}

}
