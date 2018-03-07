package dk.dtu.fred.dtupay.rest;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("delete_bank_client")
public class DeleteBankClientService {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(DeleteBankClientService.class);

	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * Destination for the JMS Queue that serves to create delete bank client messages.
	 */
	@Resource(lookup = "java:jboss/exported/DeleteBankClientQueue")
	private Destination destination;

	/**
	 * HTTP DELETE entry point for deleting a bank client account. The entry point
	 * takes a bank id as parameter in the request and produces plain-text as its response.
	 * <p>
	 * 
	 * @param bankId
	 * 			Bank account id of the bank client to delete
	 * @return A plain-text string with the state of the deletion (completed or failed)
	 */
	@DELETE()
	@Produces({ "text/plain" })
	public Response deleteBankClient(@QueryParam("bank_id") String bankId) {
		logger.info("DELETE request received");
		logger.debug("Request bank account id: {}", bankId);

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					MessageProducer producer = session.createProducer(destination);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer replyConsumer = session.createConsumer(replyDestination);

					try {
						TextMessage message = session.createTextMessage(bankId);
						message.setJMSReplyTo(replyDestination);

						connection.start();

						logger.debug("Sending message to bank access MDB");
						producer.send(message);

						long timeOutMiliseconds = 10000L;
						TextMessage replyMessage = (TextMessage) replyConsumer.receive(timeOutMiliseconds);
						
						if (replyMessage == null) {
							logger.warn("DELETE error. Timed out or closed trying to reach bank access MDB");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}

						String reply = replyMessage.getText();

						if (reply == null || reply.isEmpty()) {
							logger.warn("DELETE error. Bank access MDB returned empty or no reply");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
						}

						return Response.ok(reply).build();

					} finally {
						replyConsumer.close();
						producer.close();
					}
				} finally {
					session.close();
				}
			} finally {
				connection.close();
			}
		} catch (JMSException e) {
			logger.warn("POST error. Exception while processing the request", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
		}
	}

}
