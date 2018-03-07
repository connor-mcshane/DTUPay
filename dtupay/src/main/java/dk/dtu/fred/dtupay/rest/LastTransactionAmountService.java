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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST service class for customer account resources.
 */
@Path(LastTransactionAmountService.resourcePath)
public class LastTransactionAmountService {
	
	/**
	 * ULR path for the resources served by this REST service.
	 */
	public static final String resourcePath = "last_transaction_amount";
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(LastTransactionAmountService.class);
	
	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;
	
	/**
	 * Destination for the JMS Queue that serve create account messages.
	 */
	@Resource(lookup = "java:jboss/exported/LastTransactionAmountQueue")
	private Destination destination;
	
	/**
	 * Parses on the request to see the users last transaction to the {@link LastTransactionAmountService} class.
	 * 
	 * @throws JMSException
	 * 
	 * @param cpr
	 * 			CPR number of the user.
	 * 
	 * @return A response containing a status of the request.
	 */
	@GET()
	@Produces({ "plain/text" })
	public Response getLastTransactionAmount(@QueryParam("cpr") String cpr) throws JMSException {
		logger.info("GET request received");
		logger.debug("Cpr: {}", cpr);
		
		if (!isCprValid(cpr)) {
			logger.warn("GET bad request parameter cpr: {}", cpr);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					MessageProducer producer = session.createProducer(destination);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					
					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer consumer = session.createConsumer(replyDestination);
					
					try {
						TextMessage message = session.createTextMessage();
						message.setText(cpr);
						message.setJMSReplyTo(replyDestination);
						
						connection.start();

						logger.debug("Sending message to bussiness layer");
						producer.send(message);
						
						TextMessage replyMessage = (TextMessage) consumer.receive(10000L);
						if(replyMessage == null) {
							logger.warn("POST error. Timed out or closed trying to reach bussiness layer");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}
						String replyText = replyMessage.getText();
						if(replyText == null || replyText.isEmpty()) {
							logger.warn("POST error. Business layer returned empty or no reply");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Error when getting the amount").build();
						}

						logger.info("GET sucessful");
						logger.debug("Returning amount of the last transaction: {}", replyText);
						return Response.ok(replyText).build();	
						
					} catch (JMSException e) {
						logger.warn("POST error. Exception while processing the request", e);
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
					} finally {
						producer.close();
						consumer.close();
					}
				} catch (JMSException e) {
					logger.warn("POST error. Exception while processing the request", e);
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
				} finally {
					session.close();
				}
			} catch (JMSException e) {
				logger.warn("POST error. Exception while processing the request", e);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
			} finally {
				connection.close();
			}
		} catch (JMSException e) {
			logger.warn("POST error. Exception while processing the request", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
		}
	}
	
	/**
	 * Validates the crp format
	 */
	private boolean isCprValid(String cpr) {
		return cpr.matches("\\d{6}-\\d{4}") || cpr.matches("\\d{10}$");
	}
	
}
