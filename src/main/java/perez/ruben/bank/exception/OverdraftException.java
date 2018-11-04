package perez.ruben.bank.exception;

public class OverdraftException extends Exception {

	private static final long serialVersionUID = 9131709275716242238L;

	private double amount;
	
	public OverdraftException(double amount) {
		this.amount = amount;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "OverdraftException [amount=" + amount + "]";
	}
}
