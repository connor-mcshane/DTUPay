package dk.dtu.fred.dtupay.rest;

import java.util.UUID;

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
 * REST service to handle the barcodes.
 * 
 */
@Path("barcode")
public class CreateBarcodeService {
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateBarcodeService.class);
	
	/**
	 * Connection factory for the JSM queues.
	 */
	@Resource(lookup = "java:jboss/exported/FredConnectionFactory")
	private ConnectionFactory connectionFactory;
	
	/**
	 * The connection which is being made by connecting to the queue with the connectionfactory
	 */
	@Resource(lookup = "java:jboss/exported/CreateBarcodeQueue")
	private Destination destination;
	
	/**
	 * Passes on a request for a barcode to the {@link dk.dtu.fred.dtupay.mdb.CreateBarcodeMdb} class.
	 * @throws JMSException
	 * 
	 * @param cpr
	 * 			The CPR number of the user
	 * 
	 * @return return the response
	 */
	@GET()
	@Produces({ "text/plain" })
	public Response getBarcode(@QueryParam("cpr") String cpr) throws JMSException {
		logger.info("GET request received");
		logger.debug("Request cpr: {}", cpr);
		
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection();
			try {
				QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				try {
					MessageProducer sender = session.createProducer(destination);
					sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
					
					Destination replyDestination = session.createTemporaryQueue();
					MessageConsumer receiver = session.createConsumer(replyDestination);
					try {
						TextMessage message = session.createTextMessage(cpr);
						message.setJMSReplyTo(replyDestination);
						
						connection.start();
						
						logger.debug("Sending message to bussiness layer");
						sender.send(message);

						TextMessage uuid = (TextMessage) receiver.receive(10000L);
						
						if (uuid == null) {
							logger.warn("GET error. Timed out or closed trying to reach bussiness layer");
							return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
									.entity("Timed out processing request").build();
						} else {
							
							String uuidReplied = uuid.getText();
							
							if (uuidReplied == null || uuidReplied.isEmpty()) {
								logger.warn("GET error. Business layer returned empty or no reply");
								return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
							} else {
								try {
									UUID.fromString(uuidReplied);
									logger.info("GET sucessful");
									logger.debug("Returning created uuid: {}", uuidReplied);
									return Response.ok(uuidReplied).build();
								} catch (IllegalArgumentException ex) {
									logger.warn("GET error. Business layer returned error {]", uuidReplied);
									logger.debug("Returning message: {}", uuidReplied);
									return Response.status(Response.Status.BAD_REQUEST).entity(uuidReplied).build();
								}					
							}						
						}   
					} finally {
						sender.close();
						receiver.close();
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
