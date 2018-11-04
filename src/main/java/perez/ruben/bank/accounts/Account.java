package perez.ruben.bank.accounts;

import java.util.Objects;

import perez.ruben.bank.exception.OverdraftException;

public class Account {

	protected double balance;
	protected String owner;
	
	public Account(String owner) {
		setOwner(owner);
		balance = 0;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		Objects.requireNonNull(owner, "The owner may not be null");
		this.owner = owner;
	}

	public double getBalance() {
		return balance;
	}

	// Synchronized so that changes to the balance are thread-safe
	public synchronized void deposit(double amount) throws OverdraftException {

		double newBalance = balance + amount;
		
		// Assuming "normal" accounts must not overdraft
		if (newBalance >= 0)
			balance = newBalance;
		else
			throw new OverdraftException(amount);
	}

	@Override
	public String toString() {
		return "Account [owner=" + owner + ", balance=" + balance + "]";
	}
}
