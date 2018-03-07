package dk.dtu.fred.bankaccess.model;

import java.math.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dtu.ws.fastmoney.*;

/**
 * A separation of the business logic from the MDB. The business logic which transfers an amount
 * from one user to another can be tested by the use of Junit.
 */
public class BankTransaction {
	/**
	 * The receiver ID
	 */
	private String receiveTransactionID;
	
	/**
	 * The sender ID
	 */
	private String senderTransactionID;
	
	/**
	 * The amount of money transfered
	 */
	private BigDecimal amount;
	
	/**
	 * A comment which is added to the transaction
	 */
	private String comment;
	
	/**
	 * The bank proxy which provides the SOAP calls needed to communicate with the fast money bank
	 */
	private BankServiceProxy proxy;
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(BankTransaction.class);
	
	/**
	 * @param receiver
	 * 			The receiver of the money.
	 * @param sender
	 * 			The sender of the money.
	 * @param amount
	 * 			The amount of money transfered.
	 * @param comment
	 * 			Comment added to the transaction.
	 */
	public BankTransaction(String receiver, String sender, BigDecimal amount, String comment) {


		this.receiveTransactionID = receiver;
		this.senderTransactionID = sender;
		this.amount = amount;
		this.comment = comment;
		this.proxy = new BankServiceProxy();
	}
	
	/**
	 * The initial transaction takes the initial parameters utilizes them.
	 * 
	 * @return A text message which indicates the result of the transaction.
	 */
	public String initiateTransaction() {
		try {
			Account sender = this.proxy.getAccount(this.senderTransactionID);
			
			if (this.amount.compareTo(sender.getBalance()) != 1) {
				try {

					proxy.transferMoneyFromTo(this.senderTransactionID,
											this.receiveTransactionID,
											this.amount,
											this.comment);
					logger.info("Transaction complete");
					return "Transaction complete";

				} catch (BankServiceException Except) {
					logger.info("Transaction failed");
					return "Transaction failed";
				}
			} else {
				logger.info("insufficient funds");
				return "Insufficient funds";
			}
		} catch (Exception e) {
			logger.info("Transaction failed");
			return "Transaction failed";
		}
	}

}
