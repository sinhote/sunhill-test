package perez.ruben.bank;

import java.util.Optional;
import java.util.Scanner;

import perez.ruben.bank.accounts.Account;
import perez.ruben.bank.accounts.CheckingAccount;
import perez.ruben.bank.accounts.SavingsAccount;
import perez.ruben.bank.exception.OverdraftException;

public class InteractiveMenu {

	private static Optional<Integer> printMenu(Scanner input, String title, String... options) {
		System.out.println("\n" + title);
		for (int i = 0; i < options.length; i++) {
			System.out.println("\t" + (int)(i+1) + ". " + options[i]);
		}
		System.out.print("Your selection (leave blank to exit): ");

		int retVal = 0;
		while (true) {
			try {
				String retStr = input.nextLine().trim();
				if (retStr.isEmpty()) {
					return Optional.empty();
				} else {
					retVal = Integer.parseUnsignedInt(retStr);
					if ((retVal > 0) && (retVal <= options.length)) {
						return Optional.of(retVal);
					}		
				}
			} catch (NumberFormatException nfe) {
				// supress
			}
			System.out.print("The entered value is incorrect. Please select one of the options above: ");
		}
	}

	private static void depositMenu(Account theAccount, Scanner input) {
		while(true)  {
			System.out.print("\nEnter money amount to deposit (positive) or withdraw (negative). Leave empty to go back: ");
			String readStr = input.nextLine().trim();
			if (readStr.isEmpty()) {
				break;
			} else {
				try {
					theAccount.deposit(Double.parseDouble(readStr));
					System.out.println("Thanks! This is the current account position: " + theAccount);
				} catch (NumberFormatException e) {
					System.out.println("The entered value was not a valid number: " + e);
				} catch (OverdraftException e) {
					System.out.println("The withdrawal could not be completed because of an overdraft: " + e);
					System.out.println("This is the current account position: " + theAccount);
				}
			}	
		}
	}

	private static void interestMenu(SavingsAccount savingsAccount, Scanner input) {
		System.out.print("\nEnter new interest rate [current value: " + savingsAccount.getInterestRate() + "]: ");
		while (true) {
			String readStr = input.nextLine().trim();
			if (readStr.isEmpty())
				break;

			try {
				savingsAccount.setInterestRate(Double.parseDouble(readStr));
				break;
			} catch (NumberFormatException nfe) {
				System.out.print("The value entered was incorrect. Please enter a correct decimal number [currentvalue: " + savingsAccount.getInterestRate() + ": ");
			}
		}
	}

	private static CheckingAccount createCheckingAccountMenu(Scanner input) {
		System.out.print("\nEnter the account owner's name: ");
		String owner = input.nextLine();

		double overdraftLimit;
		System.out.print("Enter the account's overdraft limit: ");
		while (true) {
			try {
				String overdraftStr = input.nextLine();
				overdraftLimit = Double.parseDouble(overdraftStr);
				if (overdraftLimit <= 0)
					break;
			} catch (NumberFormatException e) {
				// suppress
			}
			System.out.print("Please enter a valid, non-positive decimal number: ");
		}

		System.out.println("Creating checking account for " + owner + " with overdraft limit of " + overdraftLimit);
		CheckingAccount checkingAccount = new CheckingAccount(owner, overdraftLimit);
		System.out.println("Account created: " + checkingAccount);
		return checkingAccount;
	}

	private static void overdraftMenu(CheckingAccount theAccount, Scanner input) {
		System.out.print("\nEnter new overdraft limit [current value: " + theAccount.getOverdraft() + "]: ");
		while (true) {
			String readStr = input.nextLine().trim();
			try {
				theAccount.setOverdraft(Double.parseDouble(readStr));
				break;
			} catch (NumberFormatException nfe) {
				System.out.println("The value entered was not valid");
			} catch (OverdraftException e) {
				System.out.println("Could not set a new overdraft limit because the current balance is lower: " + e);
			}
			System.out.print("Please enter a correct decimal number [currentvalue: " + theAccount.getOverdraft() + "]: ");
		}
	}

	private static void transferMenu(CheckingAccount theAccount, Scanner input) {

		String[] menuOptions = {
				"Transfer from the first to the second account",
				"Change balance in the second account",
				"Change overdraft limit in the second account"
		};

		System.out.println("\nDoing transfers requires another checking account");
		CheckingAccount anotherAccount = createCheckingAccountMenu(input);

		while (true) {
			System.out.println("\nPrimary account status: " + theAccount);
			System.out.println("Destination account status: " + anotherAccount);

			Optional<Integer> option = printMenu(input, "Transfer Menu", menuOptions);

			if (option.isPresent()) {
				switch(option.get()) {
				case 1:
					System.out.print("\nEnter money amount to transfer to the second account (positive or negative). Leave empty to go back: ");
					String readStr = input.nextLine().trim();
					if (!readStr.isEmpty()) {
						try {
							theAccount.transfer(Double.parseDouble(readStr), anotherAccount);
						} catch (NumberFormatException e) {
							System.out.println("The entered value was not a valid number: " + e);
							System.out.println();
						} catch (OverdraftException e) {
							System.out.println("The transfer could not be completed because of an overdraft: " + e);
							System.out.println();
						}
					}
					break;
				case 2:
					depositMenu(anotherAccount, input);
					System.out.println();
					break;
				case 3:
					overdraftMenu(anotherAccount, input);
					System.out.println();
					break;
				}
			} else {
				break;
			}
		}
	}

	private static void normalAccountMenu(Scanner input) {
		System.out.print("\nEnter a name for the account owner: ");
		String owner = input.nextLine();
		System.out.println("Creating 'normal' account for " + owner);
		Account normalAccount = new Account(owner);
		System.out.println("Created: " + normalAccount);

		depositMenu(normalAccount, input);
	}

	private static void savingsAccountMenu(Scanner input) {

		String owner;
		double interestRate;
		String[] menuOptions = {
				"Deposit money",
				"Change interest rate",
				"Pay accrued interest"
		};

		System.out.print("\nEnter the account owner's name: ");
		owner = input.nextLine();

		System.out.print("Enter the account's interest rate: ");
		while (true) {
			try {
				String interestStr = input.nextLine();
				interestRate = Double.parseDouble(interestStr);
				break;
			} catch (NumberFormatException e) {
				System.out.print("Please enter a valid decimal number: ");
			}
		}

		System.out.println("Creating savings account for " + owner + " with " + interestRate*100 + "% interest rate");
		SavingsAccount savingsAccount = new SavingsAccount(owner, interestRate);
		System.out.println("Account created: " + savingsAccount);

		while(true) {
			Optional<Integer> option = printMenu(input, "Savings Account Menu", menuOptions);

			if (!option.isPresent())
				break;

			switch(option.get()) {
			case 1:
				depositMenu(savingsAccount, input);
				break;
			case 2:
				interestMenu(savingsAccount, input);
				System.out.println("Thanks! This is the current account position: " + savingsAccount);
				break;
			case 3:
				System.out.println("\nProceeding to pay the current interest payout of: " + savingsAccount.getInterest());
				try {
					savingsAccount.payInterest();
					System.out.println("Thanks! This is the current account position: " + savingsAccount);
				} catch (OverdraftException e) {
					System.out.println("Error: The interest could not be paid because the account would overdraft: " + e);
					System.out.println("This is the current account position: " + savingsAccount);
				}
				break;
			}
		}
	}

	private static void checkingAccountMenu(Scanner input) {

		String[] menuOptions = {
				"Deposit money",
				"Change overdraft value",
				"Transfer to another account"
		};

		CheckingAccount checkingAccount = createCheckingAccountMenu(input);

		while(true) {
			Optional<Integer> option = printMenu(input, "Checking Account Menu", menuOptions);

			if (!option.isPresent())
				break;

			switch(option.get()) {
			case 1:
				depositMenu(checkingAccount, input);
				break;
			case 2:
				overdraftMenu(checkingAccount, input);
				System.out.println("Thanks! This is the current checking account position: " + checkingAccount);
				break;
			case 3:
				transferMenu(checkingAccount, input);
				System.out.println("Thanks! This is the current checking account position: " + checkingAccount);
				break;
			}
		}
	}


	public static void main(String[] args) {

		String[] mainMenuOptions = {
				"Operate with a 'normal' account",
				"Operate with a savings account",
				"Operate with a checking account"
		};

		try (Scanner input = new Scanner(System.in)) {
			System.out.println("Welcome to Sunhill Bank!");
			while (true) {
				Optional<Integer> option = printMenu(input, "Main menu", mainMenuOptions);

				if (option.isPresent()) {
					switch (option.get()) {
					case 1: 
						normalAccountMenu(input);
						break;
					case 2: 
						savingsAccountMenu(input);
						break;
					case 3:
						checkingAccountMenu(input);
						break;
					}
				} else {
					break;
				}
			}

			System.out.println("\n\nThank you for choosing Sunhill Bank! See you next time!");

		}
	}

}
