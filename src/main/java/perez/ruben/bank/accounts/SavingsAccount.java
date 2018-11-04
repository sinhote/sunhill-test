package perez.ruben.bank.accounts;

import perez.ruben.bank.exception.OverdraftException;

public class SavingsAccount extends Account {

	private double interestRate;

	public SavingsAccount(String owner, double interestRate) {
		super(owner);
		this.interestRate = interestRate;
	}

	public SavingsAccount(String owner) {
		this(owner, 0.0);
	}

	// Checking the interest rate is thread-safe
	public double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}

	public double getInterest() {
		return balance * interestRate;
	}

	public void payInterest() throws OverdraftException {
		deposit(getInterest());
	}

	@Override
	public String toString() {
		return "SavingsAccount [owner=" + owner + ", balance=" + balance + ", interestRate=" + interestRate + "]";
	}
}
