package dk.dtu.fred.dtupay.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.dtupay.models.Customer;
import dk.dtu.fred.dtupay.models.InternalDatabase;

/**
 * MDB that deletes a DTUPay user.
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/DeleteAccountQueue") })
public class DeleteAccountMdb implements MessageListener {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(DeleteAccountMdb.class);
	
	/**
	 * The internal database
	 */
	private final InternalDatabase database = InternalDatabase.getInstance();

	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/** 
	 * Deletes a DTUPay user. Checks that the customer is in the system before deleting.
	 */
	public void onMessage(Message message) {
		logger.debug("Message received: {}", message);

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();

			try {
				QueueSession session = (QueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				Destination serviceDestination = message.getJMSReplyTo();

				if (serviceDestination != null) {

					try {
						MessageProducer producer = session.createProducer(serviceDestination);
						TextMessage textMessage = (TextMessage) message;
						String cpr = textMessage.getText();
						String reply;
						Customer customer = database.getCustomer(cpr);
						if (customer==null) {
							reply = "This is not a DTUPay user";
						} else {
							boolean isCustomerDeleted = database.deleteCustomer(cpr);
							if (isCustomerDeleted) {
								reply = "The customer is deleted";
							} else {
								reply = "The customer could not be deleted";
							}
						}
						TextMessage replyMessage = session.createTextMessage(reply);
						connection.start();
						producer.send(replyMessage);
						producer.close();
						logger.info("Finished deleting DTUPay user");
					} finally {
						session.close();
					}
				} else {
					logger.warn("Temporary queue to reply to is null");
				}
			} finally {
				connection.close();
			}
		} catch (JMSException e) {
			logger.warn("Failed to clean up resources", e);
		}

	}


}
