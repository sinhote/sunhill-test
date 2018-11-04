package perez.ruben.bank.accounts;

import java.util.Objects;

import perez.ruben.bank.exception.OverdraftException;

public class CheckingAccount extends Account {

	// We assume it will always be negative, so we can compare the balance with this value directly
	// The account may never have a negative balance lower than this value
	Double overdraft = 0.0;

	public CheckingAccount(String owner) {
		super(owner);
		overdraft = 0.0;
	}

	public CheckingAccount(String owner, double overdraft) {
		super(owner);
		setOverdraft(overdraft);
	}

	public double getOverdraft() {
		return overdraft;
	}

	public void setOverdraft(double overdraft) {
		if (overdraft > 0) 
			throw new IllegalArgumentException("Overdraft must be negative");

		synchronized(this.overdraft) {
			this.overdraft = overdraft;
		}
	}

	public void deposit(double amount) throws OverdraftException {

		// We must synchronize deposits with changes to the overdraft limit
		// because deposits may be rejected when the overdraft changes
		
		// By synchronizing on "overdraft", and not on the object, we allow to interleave calls to #deposit and calls to #transfer
		// Otherwise, we could have deadlocks when two accounts transfer to each other concurrently
		synchronized(this.overdraft) {
			double newBalance = balance + amount;

			// We may never overdraft beyond the minimum balance
			if (newBalance >= overdraft)
				balance = newBalance;
			else
				throw new OverdraftException(amount);
		}
	}

	public synchronized void transfer(double amount, CheckingAccount otherAccount) throws OverdraftException {
		Objects.requireNonNull(otherAccount);

		// Make sure we always withdraw first (in case there is an overdraft)
		if (amount >= 0) {
			deposit(-amount);
			otherAccount.deposit(amount);
		} else {
			otherAccount.deposit(amount);
			deposit(-amount);
		}				
	}

	@Override
	public String toString() {
		return "CheckingAccount [owner=" + owner + ", balance=" + balance + ", overdraft=" + overdraft + "]";
	}

}
