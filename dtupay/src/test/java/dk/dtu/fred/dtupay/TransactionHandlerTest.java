package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import dk.dtu.fred.dtupay.models.CreateBarcodeHandler;
import dk.dtu.fred.dtupay.models.Customer;
import dk.dtu.fred.dtupay.models.InternalDatabase;
import dk.dtu.fred.dtupay.models.TransactionHandler;
import dk.dtu.fred.dtupay.models.TransactionRequest;

public class TransactionHandlerTest {
	
	private InternalDatabase database = InternalDatabase.getInstance();

	@Test
	public void testValidTransaction() {
		//InternalDatabase database = InternalDatabase.getInstance();
		
		String customerCpr = "111111-0000";
		String merchantCpr = "000000-1111";
		String uuid = "123412345";
		double amount = 100;
		
		Customer customer = new Customer(customerCpr,"customer","bankid");
		Customer merchant = new Customer(merchantCpr,"merchant","bankid");
		database.addCustomer(customer);
		database.addCustomer(merchant);
		database.addBarcode(customerCpr, uuid);
		
		TransactionRequest transRequest = new TransactionRequest(uuid,merchantCpr,amount,"comment");
		TransactionHandler transHandle = new TransactionHandler(transRequest);
		
		String verifyParticipants = transHandle.verifyTransactionParticipants();
		String transJMSMessage = transHandle.generateTransactionJMSRequest();
		database.deleteCustomer(customerCpr);
		database.deleteCustomer(merchantCpr);

		assertEquals("Transaction participants are valid",verifyParticipants);
		assertEquals(transJMSMessage,customer.getBankId()+" "+ merchant.getBankId()+ " " + amount + " " + "comment");
	}
	
	@Test
	public void testInvalidMerchantTransaction() {
		//InternalDatabase database = InternalDatabase.getInstance();

		String customerCpr = "111111-0000";
		String merchantCpr = "000000-1111";
		String uuid = "123412345";
		double amount = 100;
		
		Customer customer = new Customer(customerCpr,"customer","bankid");
		Customer merchant = new Customer(merchantCpr,"merchant","bankid");
		database.addCustomer(customer);
		//database.addCustomer(merchant); Merchant not in database
		database.addBarcode(customerCpr, uuid);
		
		TransactionRequest transRequest = new TransactionRequest(uuid,merchantCpr,amount,"comment");
		TransactionHandler transHandle = new TransactionHandler(transRequest);
		
		String verifyParticipants = transHandle.verifyTransactionParticipants();
		database.deleteCustomer(customerCpr);
		database.deleteCustomer(merchantCpr);
		
		assertEquals("This merchant is not a DTUPay user",verifyParticipants);
	}
	
	@Test
	public void testInvalidCustomerTransaction() {
		//InternalDatabase database = InternalDatabase.getInstance();

		String customerCpr = "111111-0000";
		String merchantCpr = "000000-1111";
		String uuid = "123412345";
		double amount = 100;
		
		Customer customer = new Customer(customerCpr,"customer","bankid");
		Customer merchant = new Customer(merchantCpr,"merchant","bankid");
		
		//database.addCustomer(customer); not registered
		database.addCustomer(merchant);
		database.addBarcode(customerCpr, uuid);
		
		TransactionRequest transRequest = new TransactionRequest(uuid,merchantCpr,amount,"comment");
		TransactionHandler transHandle = new TransactionHandler(transRequest);
		
		String verifyParticipants = transHandle.verifyTransactionParticipants();
		database.deleteCustomer(customerCpr);
		database.deleteCustomer(merchantCpr);
		
		assertEquals("This barcode does not belong to any of DTUPay users",verifyParticipants);
	}
	
	@Test
	public void testInvalidBarcodeTransaction() {
		//InternalDatabase database = InternalDatabase.getInstance();

		String customerCpr = "111111-0000";
		String merchantCpr = "000000-1111";
		String uuid = "123412345";
		double amount = 100;
		
		Customer customer = new Customer(customerCpr,"customer","bankid");
		Customer merchant = new Customer(merchantCpr,"merchant","bankid");
		database.addCustomer(merchant); //Merchant not in database
		//database.addBarcode(customerCpr, uuid);
		
		TransactionRequest transRequest = new TransactionRequest(uuid,merchantCpr,amount,"comment");
		TransactionHandler transHandle = new TransactionHandler(transRequest);
		
		String verifyParticipants = transHandle.verifyTransactionParticipants();
		database.deleteCustomer(customerCpr);
		database.deleteCustomer(merchantCpr);
		
		assertEquals("This barcode does not exist",verifyParticipants);
	}
	
}
