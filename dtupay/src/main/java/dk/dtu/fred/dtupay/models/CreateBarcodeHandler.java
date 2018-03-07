package dk.dtu.fred.dtupay.models;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Handler for business logic of creating a barcode
 *
 */
public class CreateBarcodeHandler {
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(CreateBarcodeHandler.class);

	public static String CreateBarcodeFromCpr(String cpr) {
		/**
		 * Create an instance of our internal database.
		 */
		InternalDatabase database = InternalDatabase.getInstance();

		/**
		 * Check if the user with the cpr is in our database.
		 */
		if (database.getCustomer(cpr) != null) {
			UUID uuid = UUID.randomUUID();
			/**
			 * Generate a unique UUID.
			 */
			while (database.barcodeIsUsed(uuid.toString())) {
				uuid = UUID.randomUUID();
			}
			/**
			 * Add the uuid to our hashmap of barcodes.
			 */
			boolean uuidAdded = database.addBarcode(cpr, uuid.toString());
			if (!uuidAdded) {
				logger.debug("the uuid can't be added to the DTUPay database {}", uuid.toString());
				return "Sorry the uuid could not be added to the DTUPay database";
			}
			/**
			 * Return the UUID.
			 */
			logger.debug("added uuid to the database: {}", uuid.toString());
			return uuid.toString();
		} else {
			/**
			 * Return an error message.
			 */
			logger.debug("user not found in DTUPay with cpr: {}", cpr);
			return "Sorry we were unable to find your registered account with the cpr provided "+ cpr;
		}
	}

}
