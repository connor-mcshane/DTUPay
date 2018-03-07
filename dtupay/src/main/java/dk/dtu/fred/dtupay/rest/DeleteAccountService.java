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

/**
 * REST service class for deleting a DTUPay user.
 */
@Path("delete_account")
public class DeleteAccountService {
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(DeleteAccountService.class);
	
	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * Destination for the JMS Queue that serves to create the delete DTUPay user messages.
	 */
	@Resource(lookup = "java:jboss/exported/DeleteAccountQueue")
	private Destination destination;

	/**
	 * HTTP DELETE entry point for deleting a DTUPay user account. The entry point
	 * takes a cpr as parameter in the request and produces plain-text as its response.
	 * <p>
	 * 
	 * @param cpr
	 * 			CPR number of the DTUPay user to delete
	 * @return A plain-text string with the state of the deletion (completed or failed)
	 */
	@DELETE()
	@Produces({ "plain/text" })
	public Response deleteAccount(@QueryParam("cpr") String cpr) {
		logger.info("GET request received");
		logger.debug("Request cpr: {}", cpr);

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
						TextMessage message = session.createTextMessage(cpr);
						message.setJMSReplyTo(replyDestination);

						connection.start();

						logger.debug("Sending message to bussiness layer");
						producer.send(message);

						TextMessage replyMessage = (TextMessage) replyConsumer.receive(10000L); // 10 sec timeout						
						
						if (replyMessage == null) {
							logger.warn("GET error. Timed out or closed trying to reach bussiness layer");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}

						String replyText = replyMessage.getText();
						
						if (replyText == null || replyText.isEmpty()) {
							logger.warn("GET error. Business layer returned empty or no reply");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
						}
						
						if (replyText.equals("The customer could not be deleted") || replyText.equals("This is not a DTUPay user")) {
							logger.warn("GET error. Business layer returned error: {}", replyText);
							logger.debug("Returning error mesage: {}", replyText);
							return Response.status(Response.Status.BAD_REQUEST).entity(replyText).build();
						} else {
							logger.info("GET sucessful");
							logger.debug("Returning : {}", replyText);
							return Response.ok(replyText).build();
						}
				
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
			logger.warn("GET error. Exception while processing the request", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
		}
		
	}

}
