package dk.dtu.fred.dtupay;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

import dk.dtu.fred.dtupay.models.CreateBarcodeHandler;
import dk.dtu.fred.dtupay.models.Customer;
import dk.dtu.fred.dtupay.models.InternalDatabase;

public class CreateBarcodeHandlerTest {
	
	private InternalDatabase database = InternalDatabase.getInstance();

	@Test
	public void testGetUuidForRegisteredUserReturnsUuid() {
		String cpr = "111111-0000";
		Customer customer = new Customer(cpr,"name","bankid");
		database.addCustomer(customer);
		try {
			UUID.fromString(CreateBarcodeHandler.CreateBarcodeFromCpr(cpr));
		} catch (IllegalArgumentException e) {
			fail();
		}
	}
	
	@Test
	public void testGetUuidForUnregisteredUserReturnsErrorMessage() {
		String cpr = "125489-0030";
		database.deleteCustomer(cpr);
		String expected = "Sorry";
		assertTrue(CreateBarcodeHandler.CreateBarcodeFromCpr(cpr).contains(expected));
	}

}
