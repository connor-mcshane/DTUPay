package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.dtu.fred.dtupay.models.Customer;
import dk.dtu.fred.dtupay.models.InternalDatabase;
import dk.dtu.fred.dtupay.models.Transaction;

public class InternalDatabaseTest {

	private static final String cpr = "021136-4491";
	private static final String name = "Storm L. Andresen";
	private static final String bankId = "bank-id";
	private static final UUID uuid = UUID.randomUUID();
	private InternalDatabase database;

	@Before
	public void setUp() throws Exception {
		database = InternalDatabase.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		database.deleteCustomer(cpr);
	}

	@Test
	public void getInstanceReturnsNotNull() {
		assertNotNull(InternalDatabase.getInstance());
	}	
	
	@Test
	public void addCustomerReturnsTrue() {
		Customer customer = new Customer(cpr, name, bankId);
		assertTrue(database.addCustomer(customer));
	}
	
	@Test
	public void getCustomerReturnsCustomer() {
		Customer customer = new Customer(cpr, name, bankId);
		database.addCustomer(customer);		
		assertNotNull(database.getCustomer(customer.getCpr()));
	}
	
	@Test
	public void deleteCustomerReturnsTrue() {
		Customer customer = new Customer(cpr, name, bankId);
		database.addCustomer(customer);
		assertTrue(database.deleteCustomer(customer.getCpr()));
	}

	@Test
	public void addBarcodeReturnsTrue() {
		assertTrue(database.addBarcode(cpr,uuid.toString()));
	}
	
	@Test
	public void getCprOfBarcodeReturnsBarcode() {
		database.addBarcode(cpr,uuid.toString());		
		assertEquals(cpr,database.getCprOfBarcode(uuid.toString()));
	}
	
	@Test
	public void deleteBarcodeReturnsTrue() {
		database.addBarcode(cpr,uuid.toString());
		assertTrue(database.deleteBarcode(uuid.toString()));
	}
	
	@Test
	public void barcodeIsUsedReturnsTrue() {
		database.addBarcode(cpr,uuid.toString());
		assertTrue(database.barcodeIsUsed(uuid.toString()));
	}
	
	@Test
	public void barcodeIsUsedReturnsFalse() {
		database.deleteBarcode(uuid.toString());
		assertFalse(database.barcodeIsUsed(uuid.toString()));
	}
	
	@Test
	public void getLastTransactionReturnsNull( ) {
		assertEquals(null,database.getLastTransactionAmount("123"));
	}
	
	@Test
	public void getLastTransactionReturnsExpectedValue( ) {
		Transaction transaction = new Transaction("111111-0000", 10.0);
		database.addTransactions("111111-0000", transaction);
		assertEquals(transaction.getAmount(), database.getLastTransactionAmount("111111-0000"));
	}
}
