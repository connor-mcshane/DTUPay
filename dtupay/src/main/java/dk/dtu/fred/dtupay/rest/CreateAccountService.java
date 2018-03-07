package dk.dtu.fred.dtupay.rest;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.dtupay.models.CreateAccountRequest;

/**
 * REST service class for customer account resources. 
 */
@Path(CreateAccountService.resourcePath)
public class CreateAccountService {

	/**
	 * ULR path for the resources served by this REST service.
	 */
	public static final String resourcePath = "account";

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateAccountService.class);

	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * Destination for the JMS Queue that serve create account messages.
	 */
	@Resource(lookup = "java:jboss/exported/CreateAccountQueue")
	private Destination createAccountDest;

	/**
	 * HTTP POST entry point for creating a customer account. The entry point
	 * consumes JSON for its request body and produces plain-text as its response.
	 * <p>
	 * On 200 OK the response contains a relative URI to access the newly created
	 * account resource. On all 4xx and 500 responses the body <i>may</i> contain an
	 * error message, or else it is empty.
	 * 
	 * @param model
	 *            request model parsed from the request body, should contain all
	 *            fields defined in {@link CreateAccountRequest}}.
	 * 
	 * @return An plain-text string denoting the local URI for accessing the created
	 *         resource. If the resource could not be created the response
	 *         <i>may</i> contain an error message or be empty.
	 */
	@POST()
	@Consumes({ "application/json" })
	@Produces({ "text/plain" })
	public Response createAccount(CreateAccountRequest model) {
		logger.info("POST request received");
		logger.debug("Request model: {}", model);
		if (!isCreateModelValid(model)) {
			logger.warn("POST bad request model data: {}", model);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					MessageProducer producer = session.createProducer(createAccountDest);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer replyConsumer = session.createConsumer(replyDestination);

					try {
						ObjectMessage message = session.createObjectMessage();
						message.setObject(model);
						message.setJMSReplyTo(replyDestination);

						connection.start();

						logger.debug("Sending message to bussiness layer");
						producer.send(message);

						TextMessage replyMessage = (TextMessage) replyConsumer.receive(10000L); 
						if (replyMessage == null) {
							logger.warn("POST error. Timed out or closed trying to reach bussiness layer");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}

						String reply = replyMessage.getText();
						if (reply == null || reply.isEmpty()) {
							logger.warn("POST error. Business layer returned empty or no reply");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
						}
						
						if (reply.matches("^\\d{6}\\-\\d{4}$") || reply.matches("^\\d{10}$")) {
							logger.info("POST sucessful");
							logger.debug("Returning created cpr: {}", reply);
							return Response.ok(reply).build();	
						} else {
							logger.warn("POST error. Business layer returned error {]", reply);
							logger.debug("Returning message: {}", reply);
							return Response.status(Response.Status.BAD_REQUEST).entity(reply).build();
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
			logger.warn("POST error. Exception while processing the request", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getLocalizedMessage()).build();
		}
	}

	/**
	 * Validate the input of a {@link CreateAccountRequest} object.
	 * 
	 * @param requestModel
	 *            the model to validate.
	 * @return true if the model is valid, and false otherwise.
	 */
	private boolean isCreateModelValid(CreateAccountRequest requestModel) {
		if (requestModel == null) {
			return false;
		}
		if (requestModel.getName() == null || requestModel.getName().isEmpty()) {
			return false;
		}
		if (requestModel.getCpr() == null || requestModel.getCpr().isEmpty()) {
			return false;
		}
		return true;
	}
}