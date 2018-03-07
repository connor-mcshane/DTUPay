package dk.dtu.fred.dtupay.mdb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.dtupay.models.CreateAccountRequest;
import dk.dtu.fred.dtupay.models.Customer;
import dk.dtu.fred.dtupay.models.InternalDatabase;

/**
 * A MDB which creates a user of DTUPay.
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/CreateAccountQueue") })
public class CreateAccountMdb implements MessageListener {
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateAccountMdb.class);
	
	/**
	 * The internal database
	 */
	private final InternalDatabase database = InternalDatabase.getInstance();
	
	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;
	
	/**
	 * Destination for the JMS Queue that serve create account messages.
	 */
	@Resource(lookup = "java:jboss/exported/VerifyBankClientQueue")
	private Destination verifyBCMdbDestination;
	
	/**
	 * Creates an user account on DTUPay. Several test is added to ensure that the user is not already in the system
	 * and that he has a bank account.
	 */
	public void onMessage(Message message) {
		logger.info("Message received: {}", message);

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();

			try {
				QueueSession session = (QueueSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				Destination serviceDestination = message.getJMSReplyTo();

				if (serviceDestination != null) {

					try {
						ObjectMessage objectMessage = (ObjectMessage) message;
						CreateAccountRequest requestCustomer = objectMessage.getBody(CreateAccountRequest.class);
						String customerBankId = " ";
						logger.info("Read request to create customer: {}", requestCustomer);

						Customer customer = new Customer(requestCustomer.getCpr(), requestCustomer.getName(),
								customerBankId);

						MessageProducer verifyBCProducer = session.createProducer(verifyBCMdbDestination);
						Destination replyVerifyBCDestination = session.createTemporaryQueue();
						MessageConsumer replyConsumer = session.createConsumer(replyVerifyBCDestination);

						TextMessage verifyBCMessage = session.createTextMessage(customer.getCpr());
						verifyBCMessage.setJMSReplyTo(replyVerifyBCDestination);

						connection.start();

						logger.info("Seding request to verify customer bank account");
						verifyBCProducer.send(verifyBCMessage);
						logger.info("Request sent");

						logger.info("Awaiting response...");
						TextMessage replyFromVerifyBC = (TextMessage) replyConsumer.receive(10000L);
						logger.info("Response received: {}", replyFromVerifyBC);

						TextMessage reply = session.createTextMessage();
						if (replyFromVerifyBC == null) {
							logger.warn("Response is null. Did it maybe time out?");
							reply.setText("Unexpected response");
						} else if (replyFromVerifyBC.getText().contains("TRUE")) {
							if (database.getCustomer(requestCustomer.getCpr()) != null) {
								logger.warn("Customer already exists in DTUPay");
								reply.setText("You already have a DTUPay account");
							} else {
								customerBankId = replyFromVerifyBC.getText().split(" ")[1];
								customer.setBankId(customerBankId);
								database.addCustomer(customer);
								logger.info("Created new DTUPay account: {}", customer);
								reply.setText(customer.getCpr());
							}
						} else if (replyFromVerifyBC.getText().equals("FALSE")) {
							logger.warn("Customer does not have a bank account.");
							reply.setText("Please create a bank account before requesting a DTUPay account");
						} else {
							logger.warn("Unexpected response");
							reply.setText("Unexpected response");
						}

						logger.info("Sending reply to service");
						MessageProducer producer = session.createProducer(serviceDestination);
						producer.send(reply);
						logger.info("Reply sent: {}", reply.getText());

						verifyBCProducer.close();
						producer.close();
						replyConsumer.close();
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
