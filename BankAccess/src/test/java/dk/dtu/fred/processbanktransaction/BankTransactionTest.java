package dk.dtu.fred.processbanktransaction;

import org.junit.*;
import static org.junit.Assert.*;
import java.math.*;
import java.rmi.RemoteException;

import org.junit.Test;

import dk.dtu.fred.bankaccess.model.BankTransaction;
import dtu.ws.fastmoney.*;

public class BankTransactionTest {
	private BankServiceProxy proxy;
	private Account senderAccount;
	private Account receiverAccount;
	
	@Before
	public void setup() throws Exception{
		this.proxy = new BankServiceProxy();
				
		String user1Cpr = "2504519730";
		User user1 = new User(user1Cpr, "Jim", "Carrey");	
		this.proxy.createAccountWithBalance(user1, new BigDecimal(5000));
		this.senderAccount =  proxy.getAccountByCprNumber(user1Cpr);
		
		String user2Cpr = "1811629329";
		User user2 = new User(user2Cpr, "Big", "Jimbo");	
		this.proxy.createAccountWithBalance(user2, new BigDecimal(5000));
		this.receiverAccount = proxy.getAccountByCprNumber(user2Cpr);
	}
	
	@After
	public void tearDown() throws Exception{
		this.proxy.retireAccount(this.senderAccount.getId());
		this.proxy.retireAccount(this.receiverAccount.getId());
	}
	
	/**
	 * Test simple valid transaction
	 * @throws Exception
	 */
	@Test
	public void validTransaction() throws Exception{
		
		String amount = "500";

		// To from amount comment
		BankTransaction transaction = new BankTransaction(this.receiverAccount.getId(),this.senderAccount.getId(), new BigDecimal(Double.parseDouble(amount)), "comment");
		
		assertEquals("Transaction complete",transaction.initiateTransaction());
		
		BigDecimal userBalance = this.proxy.getAccountByCprNumber(this.senderAccount.getUser().getCprNumber()).getBalance();

		//Check the sender balance post transaction

		assertEquals(userBalance, new BigDecimal(4500));

		userBalance = this.proxy.getAccountByCprNumber(this.receiverAccount.getUser().getCprNumber()).getBalance();

		//Check the receiver balance post transaction
		assertEquals(userBalance, new BigDecimal(5500));
	}
	
	/**
	 * Test simple invalid transaction by way of invalid transaction amount
	 */
	@Test
	public void invalidTransaction() {
		
		String amount = "10000";
		String comment = "comment";

		BankTransaction transaction = new BankTransaction(this.receiverAccount.getId(),this.senderAccount.getId(), new BigDecimal(Double.parseDouble(amount)), "comment");
		
		assertEquals("Insufficient funds",transaction.initiateTransaction());
	}

}
