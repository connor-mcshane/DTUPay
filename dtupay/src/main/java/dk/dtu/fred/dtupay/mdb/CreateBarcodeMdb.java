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

import dk.dtu.fred.dtupay.models.CreateBarcodeHandler;

/**
 * The MBD which creates a barcode
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/CreateBarcodeQueue") })
public class CreateBarcodeMdb implements MessageListener {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateBarcodeMdb.class);

	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * The barcode is created by the use of a parsed CPR number. Checks are added to
	 * ensure the uniqueness of the barcode.
	 */
	public void onMessage(Message message) {
		logger.info("message received: {}", message);

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination replyDest = message.getJMSReplyTo();
				/**
				 * We check that the temporary queue is not null.
				 */
				if ((replyDest) != null) {
					try {
						TextMessage textMessage = (TextMessage) message;
						String cpr = textMessage.getText();
						logger.info("Read request to create barcode for cpr: {}", cpr);
						MessageProducer sender = session.createProducer(replyDest);
						TextMessage messageToSend = session
								.createTextMessage(CreateBarcodeHandler.CreateBarcodeFromCpr(cpr));

						connection.start();
						logger.info("Sending reply to barcode service");
						sender.send(messageToSend);
						logger.info("Reply sent: {}", messageToSend.getText());

						sender.close();
						logger.info("Finished processing request");
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
