package perez.ruben.bank.accounts.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import perez.ruben.bank.accounts.SavingsAccount;
import perez.ruben.bank.exception.OverdraftException;

public class SavingsAccountTest {

	private String testOwner = "Test Owner";
	private double delta = 0.0001;

	@Test
	public void testAccountCreationWithoutInterestRate() {

		SavingsAccount theAccount = new SavingsAccount(testOwner);

		assertEquals(testOwner, theAccount.getOwner());
		assertEquals(0.0, theAccount.getBalance(), delta);
		assertEquals(0.0, theAccount.getInterestRate(), delta);
	}
	
	@Test
	public void testAccountCreationWithInterestRate() {

		double interestRate = 0.05; // 5 %
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, interestRate);

		assertEquals(testOwner, theAccount.getOwner());
		assertEquals(0.0, theAccount.getBalance(), delta);
		assertEquals(interestRate, theAccount.getInterestRate(), delta);
	}

	@Test
	public void testSetInterestRate() {
		
		double firstInterestRate = 0.5; // 50% wow!
		double secondInterestRate = 0.01; // 1% meh!
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, firstInterestRate);
		assertEquals(firstInterestRate, theAccount.getInterestRate(), delta);
		
		theAccount.setInterestRate(secondInterestRate);
		assertEquals(secondInterestRate, theAccount.getInterestRate(), delta);
	}
	
	@Test
	public void testGetInterest() throws OverdraftException {
		
		double balance = 300;
		double interestRate = 0.5;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, interestRate);
		theAccount.deposit(balance);
		
		assertEquals(balance*interestRate, theAccount.getInterest(), delta);
	}
	
	@Test
	public void testGetInterestWithZeroBalance() {
		
		double interestRate = 0.12765;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, interestRate);
		
		// With a balance of 0, we should get 0 interest
		assertEquals(0.0, theAccount.getInterest(), delta);
	}
	
	@Test
	public void testGetInterestWithZeroRate() throws OverdraftException {
		
		double balance = 4333657.4534;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner);
		theAccount.deposit(balance);
		
		// With an interest rate of 0, we should get 0 interest
		assertEquals(0.0, theAccount.getInterest(), delta);
	}
	
	@Test
	public void testPayInterest() throws OverdraftException {
		
		double balance = 12518.432;
		double interestRate = 0.0485;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, interestRate);
		theAccount.deposit(balance);
	
		theAccount.payInterest();
		
		assertEquals(balance*(1+interestRate), theAccount.getBalance(), delta);		
	}

	@Test
	public void testPayInterestWithZeroBalance() throws OverdraftException {
		
		double interestRate = 0.0485;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner, interestRate);
	
		theAccount.payInterest();
		
		assertEquals(0.0, theAccount.getBalance(), delta);		
	}

	@Test
	public void testPayInterestWithZeroRate() throws OverdraftException {
		
		double balance = 975965.9875;
		
		SavingsAccount theAccount = new SavingsAccount(testOwner);
		theAccount.deposit(balance);
	
		theAccount.payInterest();
		
		assertEquals(balance, theAccount.getBalance(), delta);		
	}
}
