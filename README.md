# Sunhill Practical Development Task: Bank Accounts

## Compile this project

In the root folder of the project, run:

    mvn clean install

After the unit tests complete and the build finishes successfully, a file with name `bank-0.0.1-SNAPSHOT.jar` will be created in the `target` folder.

## Run this project

For convenience, the generated `.jar` file can be run directly as:

    java -jar bank-0.0.1-SNAPSHOT.jar

## Description of the classes, design decisions and assumptions

A set of classes representing bank accounts with certain operational characteristics have been developed as requested. The main characteristics are:

* They must work concurrently:

    > Imagine you are working for a bank where _a lot of sensitive cash operations happen every second_.

* The two requested account types have been implemented as two distinct classes - `SavingsAccount` and `CurrentAccount` - which inherit from a general supertype `Account`.

  * Note: It is unclear whether or not instances of the "unspecific" `Account` type are allowed to exists. This implementation assumes they can exist, but we could easily prevent that by changing the constructor access type to `protected` or marking the class as `abstract`.

* The superclass `Account` implements methods for getting and setting the owner's name and for getting the balance and making a deposit. The amounts to deposit may be positive (representing a deposit) or negative (representing a withdrawal). Thus only one method is needed to implement the two basic operations of depositing and withdrawing money. The `deposit` method is thread-safe so that it can be executed concurrently. An `Account` class can not overdraft.

* The class `SavingsAccount` has an associated interest rate and defines two new methods for getting the current interest amount and paying it in a thread-safe manner (using the `deposit` method defined by the superclass).

  * The class `SavingsAccount` imposes no restrictions on the interest rate value, even though one would intuitively expect it to be a positive number lower than 1. However, it was left this way because the exercise did not mention that specifically. Although a class with a negative interest rate may be counter-intuitive, it works exactly the same as one with an interest rate within the usual limits.

* The class `CheckingAccount` has an associated overdraft value, so that the balance in the account can be negative up until that value. By convention, the overdraft value must be negative. This way, `getOverdraft` always returns a value consistent with the overdraft parameter accepted by the constructor and `setOverdraft`.

* Deposits in instances of `CheckingAccount` are synchronized with the changes to the overdraft limit. If this was not the case, the following situation could happen:
  1. A withdrawal of 1000 is performed in an account with 500 balance and a -500 overdraft. This operation should succeed.
  2. Almost at the same time, but _*after*_ the withdrawal, the overdraft limit is changed to -400. This operation should fail, because the new balance is -500 after the withdrawal, and the overdraft limit may not be higher.
  3. Because the operations run concurrently, the change in the overdraft limit may be completed before the withdrawal does. The operation to change the overdraft limit succeeds when it should not.
  4. Now the withdrawal operation goes on, but it fails because it violates the overdraft constraint.
  
* Instances of `CheckingAccount` may transfer money to another `CheckingAccount` instance. Calls to `transfer` are synchronized and are guaranteed to happen in order.
  * Note: This does not mean that transfers from account A to account B may not be interspersed with transfers from B to A or separate deposits to A or B while the transfer is not completed. Doing so requires a more complex locking mechanism which seemed out of scope for this exercise.

#### A note about the executable interactive menu

The task description reads exactly:

> The solution has to be easily executable

So, for instance, a very simple executable to showcase the `SavingsAccount` could be:

```java
import perez.ruben.bank.accounts.SavingsAccount;
import perez.ruben.bank.exception.OverdraftException;

public class SampleRun {

  public static void main(String[] argv) throws OverdraftException {
    System.out.println("Create savings account");
    SavingsAccount acc = new SavingsAccount("John Doe", 0.0125);
    System.out.println("Created: " + acc);
    
    System.out.println("Add 5000 to the balance");
    acc.deposit(5000);
    System.out.println("Done: " + acc);

    System.out.println("The generated interest is: " + acc.getInterest());
		
    System.out.println("Pay interest");
    acc.payInterest();
    System.out.println("Done: " + acc);
  }
}
```

But then, I consider that this code is very similar to a series of unit tests put together without the assert statements, and therefore it does not add much value. The unit tests are better structured than a casual program like the one above, so in my opinion they showcase the implemented functionality much better.

For this reason, the delivered executable (implemented in `InteractiveMenu.java`) does not intend to showcase the proposed classes' simplicity (it should be clear by reading the source code), but to provide a tool to _"play" with the solution_ without having to write boilerplate code.
