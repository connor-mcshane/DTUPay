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

import dk.dtu.fred.dtupay.models.TransactionHandler;
import dk.dtu.fred.dtupay.models.TransactionRequest;

/**
 * REST service class for transaction resources.
 */
@Path("transaction")
public class ProcessBankTransactionService {

	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ProcessBankTransactionService.class);

	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;

	/**
	 * Destination for the JMS Queue that serve transaction messages.
	 */
	@Resource(lookup = "java:jboss/exported/ProcessBankTransactionQueue")
	private Destination BankTransactionDest;

	/**
	 * HTTP POST entry point for processing a bank transaction. The entry point
	 * consumes JSON for its request body and produces plain-text as its response.
	 * <p>
	 * On 200 OK the response contains a message describing the state of the
	 * transaction. On all 4xx and 500 responses the request may be wrong, an error
	 * occurred or the body is empty.
	 * 
	 * @param model
	 *            request model parsed from the request body, should contain all
	 *            fields defined in {@link TransactionRequest}}.
	 * @return a plain-text string response with the status of the transaction and
	 *         denoting the local URI for accessing the created resource. If the
	 *         resource could not be created the response <i>may</i> contain an
	 *         error message or be empty.
	 */
	@POST()
	@Consumes({ "application/json" })
	@Produces({ "text/plain" })
	public Response ProcessBankTransaction(TransactionRequest model) {
		logger.info("POST request received in ProcessBankTransactionService");
		logger.debug("Request model: {}", model);

		if (!isCreateModelValid(model)) {
			logger.warn("POST model not valid: {}", model);
			return Response.status(Response.Status.BAD_REQUEST).entity("The request is not correct").build();
		}

		TransactionHandler transactionHandler = new TransactionHandler(model);
		String verifyParticipantsResponse = transactionHandler.verifyTransactionParticipants();
		
		if (verifyParticipantsResponse.equals("This barcode does not exist")) {
			logger.warn("POST error : {}", verifyParticipantsResponse);
			return Response.status(Response.Status.NOT_FOUND).entity(verifyParticipantsResponse).build();
		} else if (!verifyParticipantsResponse.equals("Transaction participants are valid")) {
			logger.warn("POST error : {}", verifyParticipantsResponse);
			return Response.status(Response.Status.BAD_REQUEST).entity(verifyParticipantsResponse).build();
		}

		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = (QueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					MessageProducer producer = session.createProducer(BankTransactionDest);
					producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer replyConsumer = session.createConsumer(replyDestination);

					try {
						TextMessage message = session
								.createTextMessage(transactionHandler.generateTransactionJMSRequest());

						message.setJMSReplyTo(replyDestination);
						connection.start();
						
						logger.info("Sending message to ProcessBankTransaction bean");
						producer.send(message);
						logger.info("Message sent to ProcessBankTransaction bean");
						logger.debug("Message sent to ProcessBankTransaction bean: {}", message.getText());

						TextMessage reply = (TextMessage) replyConsumer.receive(10000L);
						logger.info("Message received from ProcessBankTransaction bean");

						if (reply == null) {
							System.out.println("message from processbanktransaction null in the service");
							logger.warn("POST error. Timed out or closed trying to reach ProcessBankTransaction bean");

							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						}

						String transactionStatus = reply.getText();
						if (transactionStatus == null || transactionStatus.isEmpty()) {
							logger.warn("POST error. ProcessBankTransaction bean returned empty message",
									transactionStatus);
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(transactionStatus)
									.build();
						}
					
						if (transactionStatus.contains("Transaction complete")) {
							logger.info("POST sucessful");
							logger.debug("Returning message: {}", transactionStatus);
							transactionHandler.completeTransaction();
							return Response.ok(transactionStatus).build();
						} else {
							logger.warn("POST error. ProcessBankTransaction bean sent error: {}", transactionStatus);
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(transactionStatus).build();
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

	private boolean isCreateModelValid(TransactionRequest requestModel) {
		if (requestModel == null) {
			return false;
		}
		if (requestModel.getUuid() == null || requestModel.getUuid().isEmpty()
				|| requestModel.getUuid().contains(" ")) {
			return false;
		}
		if (requestModel.getMerchantCpr() == null || requestModel.getMerchantCpr().isEmpty()
				|| requestModel.getMerchantCpr().contains(" ")) {
			return false;
		}
		if (requestModel.getComment() == null || requestModel.getComment().isEmpty()) {
			return false;
		}
		if (requestModel.getAmount() <= 0.0) {
			return false;
		}
		return true;
	}
}
