package perez.ruben.bank.exception;

public class OverdraftException extends Exception {

	private static final long serialVersionUID = 9131709275716242238L;

	private double amount;
	private double balance;
	
	public OverdraftException(double amount, double balance) {
		this.amount = amount;
		this.balance = balance;
	}

	public double getAmount() {
		return amount;
	}

	public double getBalance() {
		return balance;
	}
	
	@Override
	public String toString() {
		return "OverdraftException [amount=" + amount + ", balance=" + balance + "]";
	}
}
