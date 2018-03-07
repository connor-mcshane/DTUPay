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
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dtu.fred.dtupay.models.CreateAccountRequest;
import dk.dtu.fred.dtupay.models.CreateBankClientRequest;

/**
 * REST service class for creating a bank client account.
 */
@Path("create_bank_client")
public class CreateBankClientService {
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateBankClientService.class);

	/**
	 * Connection factory for the JMS queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * Destination for the JMS Queue that serves to create the create bank client messages.
	 */
	@Resource(lookup = "java:jboss/exported/CreateBankClientQueue")
	private Destination destination;
	
	/**
	 * HTTP POST entry point for creating a bank client account. The entry point
	 * consumes JSON for its request body and produces plain-text as its response.
	 * <p>
	 * On 200 OK the response contains the bank account id of the newly created
	 * or already existing bank client. On all 4xx and 500 responses the body <i>may</i> contain an
	 * error message, or else it is empty.
	 * 
	 * @param model
	 * 			request model parsed from the request body, should contain all
	 *            fields defined in {@link CreateBankClientRequest}}.
	 * @return A plain-text string with the bank id of the customer.
	 * 			 If the resource could not be created, the response
	 *         <i>may</i> contain an error message or be empty.
	 */
	@POST()
	@Consumes({ "application/json" })
	@Produces({ "text/plain" })
	public Response createBankClient(CreateBankClientRequest model) {
		
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
					MessageProducer producer = session.createProducer(destination);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer replyConsumer = session.createConsumer(replyDestination);

					try {
						String text = model.getCpr() + " " + model.getFirst() + " " + model.getLast() + " " + model.getBalance();
						TextMessage message = session.createTextMessage(text);
						message.setJMSReplyTo(replyDestination);
						
						connection.start();
						logger.debug("Sending message to bank access MDB");
						producer.send(message);
						
						TextMessage replyMessage = (TextMessage) replyConsumer.receive(10000L);
					
						if (replyMessage == null) {
							logger.warn("POST error. Timed out or closed trying to reach bank access MDB");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}
						
						String reply = replyMessage.getText();
						
						if (reply == null || reply.isEmpty()) {
							logger.warn("POST error. Bank access MDB returned empty or no reply");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
						}
						
						logger.info("POST sucessful");
						logger.debug("Returning created bank account number: {}", reply);
						return Response.status(Response.Status.CREATED).entity(reply).build();	

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
	
	private boolean isCreateModelValid(CreateBankClientRequest requestModel) {
		if (requestModel == null) {
			return false;
		}
		if (requestModel.getCpr() == null || requestModel.getCpr().isEmpty() || requestModel.getCpr().contains(" ")) {
			return false;
		}
		if (requestModel.getLast() == null || requestModel.getLast().isEmpty() || requestModel.getLast().contains(" ")) {
			return false;
		}
		if (requestModel.getFirst() == null || requestModel.getFirst().isEmpty() || requestModel.getFirst().contains(" ")) {
			return false;
		}
		if (requestModel.getBalance() < 0 ) {
			return false;
		}
		return true;
	}

}
