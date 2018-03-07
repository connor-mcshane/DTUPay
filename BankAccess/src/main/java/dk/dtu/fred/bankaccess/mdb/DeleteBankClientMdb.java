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
 * 
 * MDB that deletes a bank account.
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/DeleteBankClientQueue") })
public class DeleteBankClientMdb implements MessageListener {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(DeleteBankClientMdb.class);
	
	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	ConnectionFactory connectionFactory;
	
	/**
	 * Deletes the bank account of a bank client.
	 */
	public void onMessage(Message message) {
		
		if (message instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) message;
			
			try {
				logger.info("message received");
				QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
				QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination dest = message.getJMSReplyTo();

				if (dest != null) {
					MessageProducer producer = session.createProducer(message.getJMSReplyTo());

					BankServiceProxy proxy = new BankServiceProxy();

					String accountID = textMessage.getText();
					
					String reply;

					try {
						proxy.retireAccount(accountID);
						reply = "Account deleted";
					} catch (dtu.ws.fastmoney.BankServiceException e) {
						logger.warn("No account is found");
						reply = "No account";
					}
					message = session.createTextMessage(reply);
					
					connection.start();
					producer.send(message);
					producer.close();
					session.close();
					connection.close();
				} else
					logger.debug("Destination was null");
			} catch (Exception ex) {
				logger.debug("Connection failed");
				throw new Error(ex);
			}
		}
	}

}