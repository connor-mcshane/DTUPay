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

import dtu.ws.fastmoney.*;

/**
 * 
 * MDB that creates a bank client account.
 *
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/CreateBankClientQueue") })
public class CreateBankClientMdb implements MessageListener {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateBankClientMdb.class);

	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	ConnectionFactory connectionFactory;
	QueueConnection connection;

	/**
	 * Creates a bank client account.
	 */
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			logger.info("CreateBankClient request received");
			TextMessage textMessage = (TextMessage) message;
			try {

				connection = (QueueConnection) connectionFactory.createConnection();
				QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination dest = message.getJMSReplyTo();

				if (dest != null) {
					MessageProducer producer = session.createProducer(message.getJMSReplyTo());

					BankServiceProxy proxy = new BankServiceProxy();

					String[] bankCallParams = textMessage.getText().split("\\s");

					String cpr = bankCallParams[0];
					String first = bankCallParams[1];
					String last = bankCallParams[2];
					String balance = bankCallParams[3];
					logger.debug("Customer info received", cpr);

					dtu.ws.fastmoney.User user = new User(cpr, first, last);

					String bankAccountNumber;
					try {
						logger.info("Calling bank to create client");
						bankAccountNumber = proxy.createAccountWithBalance(user, new BigDecimal(balance));
						logger.debug("Client Found", bankAccountNumber);
					}
					catch (dtu.ws.fastmoney.BankServiceException e) {
						bankAccountNumber = proxy.getAccountByCprNumber(cpr).getId();
					}
					
					logger.info("Sending reply to request");
					message = session.createTextMessage(bankAccountNumber);
					
					connection.start();
					producer.send(message);
					
					producer.close();
					session.close();
					connection.close();
				} else
					logger.debug("Destination was null");
			} catch (Exception ex) {
				throw new Error(ex);
			}
		}
	}

}