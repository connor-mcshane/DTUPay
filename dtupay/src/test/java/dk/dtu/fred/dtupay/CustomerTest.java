package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import org.junit.Test;

import dk.dtu.fred.dtupay.models.Customer;

public class CustomerTest {

	@Test (expected = NullPointerException.class)
	public void testConstructorThrowsNullPointerException() {
		new Customer(null, null, null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testConstructorEmptyCprString() {
		new Customer("", "name", "id");
	}

	@Test (expected = IllegalArgumentException.class)
	public void testConstructorEmptyNameString() {
		new Customer("111111-3333", "", "id");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testConstructorEmptyBankIdString() {
		new Customer("111111-3333", "name", "");
	}
	
	@Test
	public void testConstructorNotNull() {
		Customer customer = new Customer("111111-3333", "name", "id");
		assertNotNull(customer);
	}

	@Test
	public void testGetCprSameValue() {
		Customer customer = new Customer("111111-3333", "name", "id");
		String actual = customer.getCpr();
		String expected = "111111-3333";
		assertEquals(expected, actual);
	}
	
	@Test (expected = NullPointerException.class)
	public void testSetCprThrowsNullPointerException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setCpr(null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetCprThrowsIllegalArgumentException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setCpr("");
	}

	@Test
	public void testGetNameSameValue() {
		Customer customer = new Customer("111111-3333", "name","id");
		String actual = customer.getName();
		String expected = "name";
		assertEquals(expected, actual);
	}
	
	@Test (expected = NullPointerException.class)
	public void testSetNameThrowsNullPointerException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setName(null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetNameThrowsIllegalArgumentException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setName("");
	}
	
	@Test
	public void testGetBankIdSameValue() {
		Customer customer = new Customer("111111-3333", "name","id");
		String actual = customer.getBankId();
		String expected = "id";
		assertEquals(expected, actual);
	}
	
	@Test (expected = NullPointerException.class)
	public void testSetBankIdThrowsNullPointerException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setBankId(null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetBankIdThrowsIllegalArgumentException() {
		Customer customer = new Customer("111111-3333", "name","id");
		customer.setBankId("");
	}

}
