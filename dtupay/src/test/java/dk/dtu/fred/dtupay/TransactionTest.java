package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.dtu.fred.dtupay.models.Transaction;

public class TransactionTest { 
	
	/**
	 * Test that the transaction generated with {@link dk.dtu.fred.dtupay.models.Transaction#Transaction()} 
	 * is not initialized with null parameters
	 * @throws NullPointerException
	 */
	@Test (expected = NullPointerException.class)
	public void testConstructorThrowsNullPointerException() {
		new Transaction(null, null);
	}
	
	 @Test (expected = NullPointerException.class)
	 public void testSetIdThrowsNullPointerException() {
		 Transaction transaction = new Transaction("id", 10.0);
		 transaction.setID("");
	 }
	 
	 @Test
	 public void testSetIdRighValue() {
		 Transaction transaction = new Transaction("id", 10.0);
		 transaction.setID("id");
		 String expected = "id";
		 assertTrue(transaction.getID().equals(expected));
	 }
	
	/**
	 * Test that the transaction generated with {@link dk.dtu.fred.dtupay.models.Transaction#Transaction()}
	 * is not initialized with a negative customerID
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorEmptyCustomerID() {
		new Transaction("", 10.0);
	}
	
	/**
	 * Test that the transaction generated with {@link dk.dtu.fred.dtupay.models.Transaction#Transaction()}
	 * is not initialized with a negative amount of money
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorNegativeAmount() {
		new Transaction("id", -10.0);
	}
	
	/**
	 * Test that the transaction generated with {@link dk.dtu.fred.dtupay.models.Transaction#Transaction()}
	 * is not initialized with zero amount of money
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorZeroAmount() {
		new Transaction("id", 0.0);
	}
	
	/**
	 * Test that the transaction generated with {@link dk.dtu.fred.dtupay.models.Transaction#Transaction()} is not null.
	 */
	@Test
	public void testConstructorNotNull() {
		Transaction transaction = new Transaction("id", 10.0);
		assertNotNull(transaction);
	}


	/**
	 * Test that the date get from the method {@link dk.dtu.fred.application.core.Transaction#getDate()()}
	 * is not null.
	 */
	@Test
	public void testGetDateNotNull() throws Exception { 
		Transaction transaction = new Transaction("id", 10.0);
		assertNotNull(transaction.getDate());
	}

	/**
	 * Test for methods Get amount
	 * {@link dk.dtu.fred.dtupay.models.Transaction#getAmount()}.
	 */
	@Test
	public void testGetAmountSameValue() {
		Transaction transaction = new Transaction("id", 10.0);
		Double actual = transaction.getAmount();
		Double expected = 10.0;
		assertEquals(expected, actual);
	}

	/**
	 * Test for methods Set amount null parameter
	 * {@link dk.dtu.fred.dtupay.models.Transaction#setAmount(Double)}.
	 * @throws NullPointerException
	 */
	@Test (expected = NullPointerException.class)
	public void testSetAmountThrowsNullPointerException() {
		Transaction transaction = new Transaction("id", 10.0);
		transaction.setAmount(null);
	}

	/**
	 * Test for methods Set amount negative parameter
	 * {@link dk.dtu.fred.dtupay.models.Transaction#setAmount(Double)}
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testSeAmounDNegativeValue() {
		Transaction transaction = new Transaction("id", 10.0);
		transaction.setAmount(-10.0);
	}
	

	/**
	 * Test for methods Set amount to 0.0
	 * {@link dk.dtu.fred.dtupay.models.Transaction#setAmount(Double)}
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testSetAmountZero() {
		Transaction transaction = new Transaction("id", 10.0);
		transaction.setAmount(0.0);
	}

	/**
	 * Test for methods Set MerchantID - null parameter
	 * {@link dk.dtu.fred.dtupay.models.Transaction#setMerchantID(Integer)}
	 */
	@Test (expected = NullPointerException.class)
	public void testSetMerchantIDThrowsNullPointerException() {
		Transaction transaction = new Transaction("id",10.0);
		transaction.setMerchantID(null);
	}

	/**
	 * Test for methods Set MerchantID - negative value
	 * {@link dk.dtu.fred.dtupay.models.Transaction#setMerchantID(Integer)}
	 * @throws IllegalArgumentException
	 */
	@Test (expected = IllegalArgumentException.class)
	public void testSetMerchantIDEmptyValue() {
		Transaction transaction = new Transaction("id", 10.0);
		transaction.setMerchantID("");
	}

	/**
	 * Test for methods Set MerchantID, when it has not been initialized
	 * {@link dk.dtu.fred.dtupay.models.Transaction#getMerchantID()}.
	 * @throws NullPointerException
	 */
	@Test (expected = NullPointerException.class)
	public void testGetMerchantID() {
		Transaction transaction = new Transaction("id",10.0);
		transaction.getMerchantID();
	}

	/**
	 * Test for methods Get merchantID returns expected value
	 * {@link dk.dtu.fred.dtupay.models.Transaction#getMerchantID()}.
	 */
	@Test
	public void testGeMerchantIDSameValue() {
		Transaction transaction = new Transaction("id", 10.0);
		transaction.setMerchantID("id1");
		String actual = transaction.getMerchantID();
		String expected = "id1";
		assertEquals(expected, actual);
	}

	/*public static void createQRcodeImage (BitMatrix qrCode, String imageName) throws IOException {
        Path path = FileSystems.getDefault().getPath(imageName);
        MatrixToImageWriter.writeToPath(qrCode, "PNG", path);
	} */
}
