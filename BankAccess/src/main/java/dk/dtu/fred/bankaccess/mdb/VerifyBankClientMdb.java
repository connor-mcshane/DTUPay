package dk.dtu.fred.bankaccess.mdb;


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

import dtu.ws.fastmoney.*;

/**
 * SOAP communication class for verifying if a person is a Bank Client
 */

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/VerifyBankClientQueue") })
public class VerifyBankClientMdb implements MessageListener {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */

	private static final Logger logger = LoggerFactory.getLogger(VerifyBankClientMdb.class);
	Account bankAccount = new Account();

	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	ConnectionFactory connectionFactory;
	QueueConnection connection;
	

	/**
	 * Listener for requests to query the bank. Upon triggering, a the JMS message should 
	 * contain a string that contains a CPR-number.
	 * 
	 * 
	 * Message  	TextMessage
	 *					JMS message should contain a string that contains a DTUPay ID.
	 * Message 	ReplyTo 
	 * 				Address of temporary queue to send answer to
	 * 			
	 * return none
	 **/   

	public void onMessage(Message message) {
		logger.info("VerifyBankClient request received");
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			try {
				/**
				 * Connect to a temporary queue to send message to received request. 
				 *  JMS message should be a string that contains  "True" + BankID. 
				 *  or a string containing "FALSE" 
				*/
				connection = (QueueConnection) connectionFactory.createConnection();
				try {
					QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
					try {

						Destination dest = message.getJMSReplyTo();

						if (dest != null) {
							MessageProducer producer = session.createProducer(message.getJMSReplyTo());

							BankServiceProxy proxy = new BankServiceProxy();

							String cpr = textMessage.getText();
							logger.debug("DTUPay UserID recieved", cpr);


							try {
								logger.info("Checking with bank to verify client status");
								Account bankAccount = proxy.getAccountByCprNumber(cpr);
								message = session.createTextMessage("TRUE " + bankAccount.getId());
								logger.debug("Client Found", bankAccount.getId());
								
							} catch (BankServiceException Except) {
								logger.debug("Client Not Found", bankAccount.getId());
								message = session.createTextMessage("FALSE");
							}
							
							logger.info("Sending reply to request");
							producer.send(message);
							producer.close();
							
						} else {
							logger.error("Destination was null");
						}
					} finally {
						session.close();
					}
				} finally {
					connection.close();
				}
			} catch (Exception ex) {
				throw new Error(ex);
			}
		}
	}

}