package perez.ruben.bank.accounts.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import perez.ruben.bank.accounts.CheckingAccount;
import perez.ruben.bank.exception.OverdraftException;

public class CheckingAccountTest {

	private String testOwner = "Test Owner";
	private double delta = 0.0001;

	@Test
	public void testAccountCreationWithoutOverdraft() {

		CheckingAccount theAccount = new CheckingAccount(testOwner);

		assertEquals(testOwner, theAccount.getOwner());
		assertEquals(0.0, theAccount.getOverdraft(), delta);
	}

	@Test
	public void testAccountCreationWithNegativeOverdraft() {

		double theOverdraft = -5000;

		CheckingAccount theAccount = new CheckingAccount(testOwner, theOverdraft);

		assertEquals(testOwner, theAccount.getOwner());
		assertEquals(theOverdraft, theAccount.getOverdraft(), delta);
	}

	public void testAccountCreationWithPositiveOverdraft() {

		double theOverdraft = 5000;

		try {
			new CheckingAccount(testOwner, theOverdraft);
			fail("A checking account may not accept a positive overdraft");
		} catch (IllegalArgumentException iae) {
			// OK!
		}
	}	

	@Test
	public void testSetNegativeOverdraft() throws OverdraftException {
		double theOverdraft = -2000;

		CheckingAccount theAccount = new CheckingAccount(testOwner);
		assertEquals(0.0, theAccount.getOverdraft(), delta);

		theAccount.setOverdraft(theOverdraft);
		assertEquals(theOverdraft, theAccount.getOverdraft(), delta);

	}

	@Test
	public void testSetPositiveOverdraft() throws OverdraftException {
		double theOverdraft = 2000;

		CheckingAccount theAccount = new CheckingAccount(testOwner);
		assertEquals(0.0, theAccount.getOverdraft(), delta);

		try {
			theAccount.setOverdraft(theOverdraft);
			fail("The overdraft value must not be positive but setting a value of " + theOverdraft + " did not fail");
		} catch (IllegalArgumentException iae) {
			// OK!
		}	
	}

	@Test
	public void testSetOverdraftGreaterThanCurrentBalance() throws OverdraftException {
		double theOverdraft = -2000;
		double theBalance = -3000;

		CheckingAccount theAccount = new CheckingAccount(testOwner);
		assertEquals(0.0, theAccount.getOverdraft(), delta);
		
		// Set balance and overdraft to the same value
		theAccount.setOverdraft(theBalance);
		theAccount.deposit(theBalance);
		assertEquals(theBalance, theAccount.getBalance(), delta);
		assertEquals(theBalance, theAccount.getOverdraft(), delta);
		
		// Try to change the overdraft to a greater value
		try {
			theAccount.setOverdraft(theOverdraft);
			fail("The overdraft value must not be greater than the balance, but setting a value of " + theOverdraft + " with a balance of " + theBalance + " did not fail");
		} catch (OverdraftException iae) {
			// OK!
		}	
	}
	
	@Test
	public void testAddBalanceWithoutOverdraft() throws OverdraftException {

		CheckingAccount theAccount = new CheckingAccount(testOwner);
		double positiveDeposit = 1000;
		double negativeDeposit = -50.5;

		theAccount.deposit(positiveDeposit);
		assertEquals(positiveDeposit, theAccount.getBalance(), delta);

		theAccount.deposit(negativeDeposit);
		assertEquals(positiveDeposit+negativeDeposit, theAccount.getBalance(), delta);
	}

	@Test
	public void testOverdraftWithZeroOverdraft() throws OverdraftException {

		double firstDeposit = 100;
		double overdraftDeposit = -150;

		CheckingAccount theAccount = new CheckingAccount(testOwner);
		assertEquals(0, theAccount.getBalance(), delta);
		theAccount.deposit(firstDeposit);
		assertEquals(firstDeposit, theAccount.getBalance(), delta);

		try {
			theAccount.deposit(overdraftDeposit);
			fail("Account did not throw an exception when overdraft");
		} catch (OverdraftException oe) {
			assertEquals(firstDeposit, theAccount.getBalance(), delta);
		}
	}

	@Test
	public void testOverdraftWithNonZeroOverdraft() throws OverdraftException {

		double firstDeposit = 100;
		double secondDeposit = -150;
		double overdraftDeposit = -0.01;
		double overdraft = -50;

		CheckingAccount theAccount = new CheckingAccount(testOwner, overdraft);
		assertEquals(0, theAccount.getBalance(), delta);
		assertEquals(overdraft, theAccount.getOverdraft(), delta);

		theAccount.deposit(firstDeposit);
		assertEquals(firstDeposit, theAccount.getBalance(), delta);

		theAccount.deposit(secondDeposit);
		assertEquals(firstDeposit + secondDeposit, theAccount.getBalance(), delta);

		try {
			theAccount.deposit(overdraftDeposit);
			fail("Account did not throw an exception when overdraft");
		} catch (OverdraftException oe) {
			assertEquals(firstDeposit+secondDeposit, theAccount.getBalance(), delta);
		}
	}

	public void testTransfer(double balanceFirst, double overdraftFirst, double balanceSecond, double overdraftSecond, double amount) throws OverdraftException {

		CheckingAccount firstAccount = new CheckingAccount(testOwner, overdraftFirst);
		CheckingAccount secondAccount = new CheckingAccount(testOwner, overdraftSecond);

		firstAccount.deposit(balanceFirst);
		secondAccount.deposit(balanceSecond);

		assertEquals(balanceFirst, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond, secondAccount.getBalance(), delta);

		firstAccount.transfer(amount, secondAccount);

		assertEquals(balanceFirst-amount, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond+amount, secondAccount.getBalance(), delta);
	}


	@Test
	public void testTransferPositive() throws OverdraftException {

		double balanceFirst = 2000;
		double balanceSecond = 500;
		double overdraftFirst = 0.0;
		double overdraftSecond = 0.0;;
		double transferAmount = 1000;

		testTransfer(balanceFirst, overdraftFirst, balanceSecond, overdraftSecond, transferAmount);
	}

	@Test
	public void testTransferNegative() throws OverdraftException {

		double balanceFirst = 2000;
		double balanceSecond = 1500;
		double overdraftFirst = 0.0;
		double overdraftSecond = 0.0;;
		double transferAmount = -1000;

		testTransfer(balanceFirst, overdraftFirst, balanceSecond, overdraftSecond, transferAmount);
	}

	@Test
	public void testTransferWithOverdraftSuccessPositive() throws OverdraftException {

		double balanceFirst = 2000;
		double balanceSecond = 2000;
		double overdraftFirst = -500;
		double overdraftSecond = 0;
		double transferAmount = 2500;

		testTransfer(balanceFirst, overdraftFirst, balanceSecond, overdraftSecond, transferAmount);
	}

	@Test
	public void testTransferWithOverdraftSuccessNegative() throws OverdraftException {

		double balanceFirst = 100;
		double balanceSecond = 2000;
		double overdraftFirst = 0;
		double overdraftSecond = -500;
		double transferAmount = -2500;

		testTransfer(balanceFirst, overdraftFirst, balanceSecond, overdraftSecond, transferAmount);
	}

	@Test
	public void testTransferWithOverdraftFailPositive() throws OverdraftException {

		double balanceFirst = 2000;
		double overdraftFirst = -100;
		double balanceSecond = 500;
		double transferAmount = 2500;

		CheckingAccount firstAccount = new CheckingAccount(testOwner, overdraftFirst);
		CheckingAccount secondAccount = new CheckingAccount("another owner");

		firstAccount.deposit(balanceFirst);
		secondAccount.deposit(balanceSecond);

		assertEquals(balanceFirst, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond, secondAccount.getBalance(), delta);

		try {
			firstAccount.transfer(transferAmount, secondAccount);
			fail("Transfer was successful but should have failed because of an overdraft");
		} catch (OverdraftException oe) {
			// OK!!
		}

		assertEquals(balanceFirst, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond, secondAccount.getBalance(), delta);
	}

	@Test
	public void testTransferWithOverdraftFailNegative() throws OverdraftException {

		double balanceFirst = 2000;
		double overdraftFirst = -100;
		double balanceSecond = 500;
		double transferAmount = 2500;

		CheckingAccount firstAccount = new CheckingAccount(testOwner, overdraftFirst);
		CheckingAccount secondAccount = new CheckingAccount("another owner");

		firstAccount.deposit(balanceFirst);
		secondAccount.deposit(balanceSecond);

		assertEquals(balanceFirst, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond, secondAccount.getBalance(), delta);

		try {
			firstAccount.transfer(transferAmount, secondAccount);
			fail("Transfer was successful but should have failed because of an overdraft");
		} catch (OverdraftException oe) {
			// OK!!
		}

		assertEquals(balanceFirst, firstAccount.getBalance(), delta);
		assertEquals(balanceSecond, secondAccount.getBalance(), delta);
	}

	private Runnable makeTransferRunnable(CheckingAccount anAccount, CheckingAccount anotherAccount, double amount) {
		// We use this function to wrap the overdraft exception in an unchecked exception
		// This way we can submit this runnable to an executor
		return () -> {
			try {
				anAccount.transfer(amount, anotherAccount);
			} catch (OverdraftException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Test
	public void testDepositOverdraftConcurrency() throws OverdraftException, InterruptedException, ExecutionException {

		int nTrials = 20000;
		int nThreads = 2;
		double initialBalance = 1000;

		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		Random r = new Random();

		// Create array with two accounts
		// Make sure they can not overdraft
		CheckingAccount[] accounts = { 
				new CheckingAccount("Account One", Double.NEGATIVE_INFINITY),
				new CheckingAccount("Account Two", Double.NEGATIVE_INFINITY)
		};

		// Set initial balance
		for (CheckingAccount acc : accounts) {
			acc.deposit(initialBalance);
			assertEquals(initialBalance, acc.getBalance(), delta);
		}

		// Concurrent transfers between accounts should be synchronized
		double temp;
		double[] finalBalances = { initialBalance, initialBalance };

		for (int i = 0; i < nTrials; i++) {
			// temp is a random number between -500 and 500
			temp = r.nextDouble()*1000-500;
			// alternate the direction of the transfer
			// even trials transfer from account 0 to account 1
			// odd trials transfer from account 1 to account 0
			// Compute the final balances in finalBalances to check everything works
			finalBalances[i%2] -= temp;
			finalBalances[(i+1)%2] += temp;
			executor.submit(makeTransferRunnable(accounts[i%2], accounts[(i+1)%2], temp));
		}

		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		if (!executor.isTerminated()) {
			fail("Not all tasks completed");
		}
		
		assertEquals(finalBalances[0], accounts[0].getBalance(), delta);
		assertEquals(finalBalances[1], accounts[1].getBalance(), delta);

	}
}
