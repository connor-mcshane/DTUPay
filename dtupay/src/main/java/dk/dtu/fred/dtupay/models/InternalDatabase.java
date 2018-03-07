package dk.dtu.fred.dtupay.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The internal database which stores the clients of DTUPay. The database consist of hashmaps and is initialized as
 * a singleton class to ensure only one reference of the database exists. 
 *
 */
public class InternalDatabase {
	
	/**
	 * {@link org.apache.logging.log4j.core.Logger} for the class.
	 */
	private static final Logger logger = LoggerFactory.getLogger(InternalDatabase.class);
	
	/**
	 * The instance is used to utilize the singleton pattern
	 */
	private static InternalDatabase instance;
	
	/**
	 * Several hasmaps to contain the customer, barcode and transaction data.
	 */
	private HashMap<String, Customer> customers;
	private HashMap<String, String> barcodes; 
	private HashMap<String, List<Transaction>> transactions;

	/**
	 * A private constructor to ensure the class cannot be instantiated this way.
	 */
	private InternalDatabase() {				
		customers = new HashMap<String, Customer>();
		barcodes = new HashMap<String, String>();
		transactions = new HashMap<String,List<Transaction>>();
		//Initial Test Users
		Customer testCustomer = new Customer("111111-0000", "Test customer", "044a672-3766-44a5-8601-9740bfbfb867");
		customers.put(testCustomer.getCpr(), testCustomer);
		Customer testMerchant = new Customer("222222-0000", "Test merchant", "60bbeae7-a6e2-4e11-8104-601ec41a75a3");
		customers.put(testMerchant.getCpr(), testMerchant);
		
		//For Testing Merchant Transaction
		Customer transTestCustomer = new Customer("010101-0101", "Max Powers", "9b589bd4-6fbe-4ea2-be33-02ed5db207ba");
		customers.put(transTestCustomer.getCpr(), transTestCustomer);
		Customer transTestMerchant = new Customer("101010-1010", "Gator Gat", "44700f22-1f63-438e-b58d-0679a025f88a");
		customers.put(transTestMerchant.getCpr(), transTestMerchant);

		// Test Barcodes WITH NO bank accounts
		barcodes.put("123456789", "111111-0000");		
		barcodes.put("111151119", "111111-0000");
        //Test Barcodes WITH bank accounts
        barcodes.put("987654321", "010101-0101");
		barcodes.put("121416181", "010101-0101");
		
	}
	
	/**
	 * The method will return the instance of the internal database if it has not been instantiated before.
	 * 
	 * @return the database instance
	 */
	public static InternalDatabase getInstance() {
		if(instance == null) {
			instance = new InternalDatabase();
		}
		return instance;
	}
	
	/**
	 * Get the last transaction of a user with a given CPR number
	 * 
	 * @param cpr
	 * 			The CPR number
	 * @return The amount of the last transaction
	 */
	public Double getLastTransactionAmount(String cpr) {
		if (transactions.get(cpr) == null || transactions.get(cpr).size() < 1) {
			return null;
		}
		return transactions.get(cpr).get(transactions.get(cpr).size() - 1).getAmount();
	}
	
	/**
	 * Adds a transaction to the internal database
	 * 
	 * @param cpr
	 * 			The CPR number
	 * @param transaction
	 * 			The transaction
	 */
	public void addTransactions(String cpr, Transaction transaction) {
		List<Transaction> listTrans = this.transactions.get(cpr);
		if (listTrans == null) {
			listTrans = new ArrayList<Transaction>();
		}
		listTrans.add(transaction);
		transactions.put(cpr,listTrans);
	}
	
	
	/**
	 * Add a costumer to the internal database
	 * 
	 * @param customer
	 * 			The costumer
	 * @return Return a boolean the indicate whether the costumer already was in the database.
	 */
	public boolean addCustomer(Customer customer) {
		Customer oldValue = customers.put(customer.getCpr(), customer);
		if (oldValue == null) {			
			logger.debug("Added customer {} to key {}", customer, customer.getCpr());
			return true;			
		} else {
			logger.debug("Replaced customer {} with new customer {}", oldValue, customer);
			return false;	
		}		
	}
	
	/**
	 * 
	 * @param cpr
	 * 			The CPR number
	 * @return The costumer CPR number
	 */
	public Customer getCustomer(String cpr) {
		return customers.get(cpr);
	}
	
	
	/**
	 * Removes the customer from the internal database
	 * 
	 * @param cpr
	 * 			The CPR number
	 * @return Returns a boolean the reply whether the user existed in the database.
	 */
	public boolean deleteCustomer(String cpr) {
		Customer oldValue = customers.remove(cpr);		
		if (oldValue != null) {			
			logger.debug("Removed customer {}", oldValue);
			List<Transaction> listTrans = transactions.remove(cpr);
			if (listTrans == null) {
				logger.debug("No transaction to remove when deleting customer with cpr {}", cpr);
			} else {
				logger.debug("Removed list of transactions for customer with cpr {}", cpr);
			}
			return true;
		} else {
			logger.debug("No customer to remove with key {}", cpr);
			return false;	
		}	
	}
	
	/**
	 * 
	 * @param cpr
	 * 			The CPR number
	 * @param uuid
	 * 			The UUID number of the customer
	 * @return Returns a boolean to state whether the barcode is added or the previous barcode was replaced.
	 */
	public boolean addBarcode (String cpr, String uuid) {
		String oldValue = barcodes.put(uuid, cpr);
		if (oldValue == null) {
			logger.debug("Added barcode {} to key", uuid, cpr);
			return true;			
		} else {
			logger.debug("Replaced barcode {} with new barcode {}", oldValue, uuid);
			return false;	
		}	
	}
	
	/**
	 * Returning the barcode given a CPR number
	 * 
	 * @param uuid
	 * 			The UUID which can be used to create the barcode
	 * @return The UUID
	 */
	public String getCprOfBarcode (String uuid) {
		return barcodes.get(uuid);
	}
	
	/**
	 * Checks if the barcode is already used
	 * 
	 * @param uuid
	 * 			The UUID
	 * @return returns a boolean to indicate whether the barcode exists or not.
	 */
	public boolean barcodeIsUsed (String uuid) {
		return barcodes.containsKey(uuid);
	}
	
	/**
	 * Delete a barcode from the internal database
	 * 
	 * @param uuid
	 * 			The UUID
	 * @return Returns a boolean value the indicate whether the barcode was removed.
	 */
	public boolean deleteBarcode (String uuid) {
		String oldValue = barcodes.remove(uuid);
		if (oldValue != null) {
			logger.debug("Removed barcode {}", uuid);
			return true;
		} else {
			logger.debug("No barcode to remove with key {}", uuid);
			return false;
		}
	}
}
