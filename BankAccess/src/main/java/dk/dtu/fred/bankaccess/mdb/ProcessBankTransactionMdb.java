package dk.dtu.fred.bankaccess.mdb;

import java.math.BigDecimal;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.bankaccess.model.BankTransaction;

/**
 * GetBankBalance is a listener of the GetBankBalance JMS queue. 
 * 		When it is triggered, it reads a string containing a CPR-number.
 *  		The reply message contains a string which can contain either 
 *  				the bank balance
 *  					or
 *  				the message "No Such Bank Client"
 *  					or
 *     			the message "Negative Balance"
 *  
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/ProcessBankTransactionQueue") })
public class ProcessBankTransactionMdb implements MessageListener {
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ProcessBankTransactionMdb.class);
	
	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	ConnectionFactory connectionFactory;
	
	/**
	 * The connection which is being made by connecting to the queue with the connectionfactory
	 */
	QueueConnection connection;
	
	/**
	 * The message gets invoked whenever the MDB receives a message. The message text is formatted and parsed on to
	 * {@link BankTransaction} which handles the business logic.
	 */
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			try {
				connection = (QueueConnection) connectionFactory.createConnection();
				try {
					QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
					try {

						Destination dest = message.getJMSReplyTo();

						if (dest != null) {
							MessageProducer producer = session.createProducer(message.getJMSReplyTo());
							
							String[] elements = textMessage.getText().split(" ");
							String comments = "";
							for (int i=3;i<elements.length ; i++) {
								comments = comments + " " + elements[i];
							}							

							BankTransaction bankTransaction = new BankTransaction(elements[0],elements[1], new BigDecimal(Double.parseDouble((elements[2]))), comments);
							
							message = session.createTextMessage(bankTransaction.initiateTransaction());
							
							producer.send(message);
							producer.close();
						} else {
							logger.error("Destination was null");
							System.out.println("Destination was null");
						}
					} finally {
						session.close();
					}
				} finally {
					connection.close();
				}
			} catch (Exception ex) {
				logger.warn("Failed to connect");
				throw new Error(ex);
			}
		}
	}

}