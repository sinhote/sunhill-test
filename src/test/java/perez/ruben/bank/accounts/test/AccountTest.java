package perez.ruben.bank.accounts.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import perez.ruben.bank.accounts.Account;
import perez.ruben.bank.exception.OverdraftException;

public class AccountTest {

	private String testOwner = "Test Owner";
	private double delta = 0.0001;

	@Test
	public void testAccountCreation() {

		Account theAccount = new Account(testOwner);

		assertEquals(testOwner, theAccount.getOwner());
		assertEquals(0.0, theAccount.getBalance(), delta);
	}

	@Test
	public void testNullOwnerConstructor() {

		try {
			new Account(null);
			fail("Account did not throw an exception when created with a null owner");
		} catch (NullPointerException npe) {
			// OK!
		}
	}
	
	@Test
	public void testSetOwner() {
		
		String newOwner = "New Owner";
		
		Account theAccount = new Account(testOwner);
		assertEquals(testOwner, theAccount.getOwner());
		
		theAccount.setOwner(newOwner);
		assertEquals(newOwner, theAccount.getOwner());
	}
	
	@Test
	public void testNullOwner() {
		Account theAccount = new Account(testOwner);
		
		try {
			theAccount.setOwner(null);
			fail("Account did not throw an exception when a null owner was set");
		} catch (NullPointerException npe) {
			// OK!
		}
	}

	@Test
	public void testAddBalance() throws OverdraftException {

		Account theAccount = new Account(testOwner);
		double positiveDeposit = 1000;
		double negativeDeposit = -50.5;

		theAccount.deposit(positiveDeposit);
		assertEquals(positiveDeposit, theAccount.getBalance(), delta);

		theAccount.deposit(negativeDeposit);
		assertEquals(positiveDeposit+negativeDeposit, theAccount.getBalance(), delta);
	}

	@Test
	public void testOverdraft() throws OverdraftException {

		Account theAccount = new Account(testOwner);
		double firstDeposit = 100;
		double overdraftDeposit = -150;

		theAccount.deposit(firstDeposit);

		try {
			theAccount.deposit(overdraftDeposit);
			fail("Account did not throw an exception when overdraft");
		} catch (OverdraftException oe) {
			assertEquals(firstDeposit, theAccount.getBalance(), delta);
		}
	}

	private Runnable makeRunnable(Account anAccount, double aDeposit) {
		// We use this function to suppress the exception
		// Because we always use positive deposits for this test, we can never overdraft
		return () -> {
			try {
				anAccount.deposit(aDeposit);
			} catch (OverdraftException e) {
				e.printStackTrace();
			}
		};
	}

	@Test
	public void testConcurrency() throws InterruptedException {
		
		int nTrials = 10000;
		int nThreads = 2;
		
		Random r = new Random();
		Account theAccount = new Account(testOwner);
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		
		// Calculate nTrials random positive numbers to use as deposit amounts
		// Create a runnable for each of them to make the deposits concurrently
		// Store the final expected balance in finalBalance
		double temp, finalBalance = 0;
		for (int i = 0; i < nTrials; i++) {
			// temp is a random number between 0 and 1000
			temp = r.nextDouble()*1000;
			finalBalance += temp;
			executor.submit(makeRunnable(theAccount, temp));
		}

		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.SECONDS);
		if (!executor.isTerminated()) {
			fail("Not all tasks completed");
		}

		assertEquals(finalBalance, theAccount.getBalance(), delta); 
	}

}
