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

import dk.dtu.fred.dtupay.models.InternalDatabase;

/**
 * A MDB which return the value of the last transaction
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/LastTransactionAmountQueue") })
public class LastTransactionAmountMdb implements MessageListener{
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(LastTransactionAmountMdb.class);
	
	/**
	 * An instance of the internal database
	 */
	InternalDatabase DATABASE = InternalDatabase.getInstance(); 
	
	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;
	
	/**
	 * When a message is received the method looks into the internal database and retrieves the last transaction
	 */
	@Override
	public void onMessage(Message message) {
		logger.info("onMessage invoked");
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination replyDestination = message.getJMSReplyTo();
				
				if(replyDestination == null) {
					logger.error("Temporary queue to reply is null");
				} else {
					TextMessage textMessage = (TextMessage) message;
					String cpr = textMessage.getText();
					logger.info("Message received from service Cpr {}", cpr);
					
					MessageProducer producer = session.createProducer(replyDestination);
					TextMessage replyMessage = session.createTextMessage();
					
					Double amount = DATABASE.getLastTransactionAmount(cpr);
					if(amount == null) {
						replyMessage.setText("You don't have any transaction");
					} else {
						replyMessage.setText(String.valueOf(amount));
					}
					
					producer.send(replyMessage);
					producer.close();
				}
				
			} catch (JMSException e) {
				logger.error("Creating queue session failed");
			} finally {
				connection.close();
			}
		} catch (JMSException e) {
			logger.error("Connection failed");
		}
	}
}
