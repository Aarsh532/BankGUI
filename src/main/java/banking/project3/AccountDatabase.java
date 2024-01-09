package banking.project3;


/**
 * Account Database handles the account array and all methods for each of the commands
 * @author Aarsh, Hersh
 */


public class AccountDatabase {
    private static final int INCREMENT_AMOUNT = 4;
    private static final int NOT_FOUND = -1;
    private static final int STARTING_NUM_ACCT = 0;
    private static final int RESET_WITHDRAWAL = 0;
    private Account[] accounts; //list of various types of accounts



    private int numAcct; //number of accounts in the array

    /**
     * Method to create Account Database object
     */
    public AccountDatabase(){
        accounts = new Account[4];
        numAcct = STARTING_NUM_ACCT;
    }

    /**
     * Method to find an account in a database
     * @param account as Account
     * @return index as int
     */
    private int find(Account account) {
        int index = NOT_FOUND;
            for (int i = 0; i < numAcct; i++) {
                if (accounts[i].equals(account)) {
                    index = i;
                    break;
                }
            }
            return index;
        }


    /**
     * Method to grow account database
     */
    private void grow(){
        Account[] copy = new Account[numAcct + INCREMENT_AMOUNT];
        for(int i = 0; i < numAcct; i++){
            copy[i] = accounts[i];
        }
        accounts = copy;

    }

    /**
     * Parent method to find method
     * @param account as Account
     * @return true if account is found, false otherwise
     */
    public boolean contains(Account account){
        return find(account) != NOT_FOUND;
    }

    /**
     * Checks if a Checking account exists in the database.
     * @param checking The checking account to check.
     * @return true if the account exists, false otherwise.
     */
    public boolean contains(Checking checking){
        int index = NOT_FOUND;
        for (int i = 0; i < numAcct; i++) {
            if (accounts[i].equalsForTransactions(checking)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to check if account is found in database if not create account
     * @param account as Account
     * @return true if account not found, false otherwise
     */
    public boolean open(Account account){
        if(account.getClass().equals(CollegeChecking.class)){
            if(contains((CollegeChecking) account)){
                return false;
            }
        } else if (account.getClass().equals(Checking.class)) {
            if (contains((Checking) account)) {
                return false;
            }
        } else if(contains(account)){
            return false;
        }
        if (numAcct >= accounts.length) {
            grow();
        }
        accounts[numAcct] = account;
        numAcct++;
        return true;
    }

    /**
     * Method to check if account is found in database to close
     * @param account as Account
     * @return true if account is found to close, false otherwise
     */
    public boolean close(Account account){
        int removeIndex = find(account);
        if (removeIndex != NOT_FOUND) {
            for (int i = removeIndex; i < numAcct - 1; i++) {
                accounts[i] = accounts[i + 1];
            }
            numAcct--;
            accounts[numAcct] = null;
            return true;
        }
        return false;
    }

    /**
     * Method to check if account is found in database to withdraw
     * @param account as Account
     * @return true if account is found, false otherwise
     */
    public boolean withdraw(Account account){
        int index = find(account);
        double withdrawAmt = account.balance;
        if (index == NOT_FOUND) {
            return false;
        }
        Account acct = accounts[index];
        account.balance = accounts[index].balance;
        if (acct.balance < withdrawAmt) {
            return false;
        }
        acct.balance -= withdrawAmt;
        if (acct instanceof MoneyMarket) {
            MoneyMarket mmAccount = (MoneyMarket) acct;
            mmAccount.incrementWithdrawals();
            if (mmAccount.balance < MoneyMarket.MIN_BALANCE_FEE_WAIVED) {
                mmAccount.isLoyal = false;
            }
            accounts[index] = mmAccount;
        }
        return true;
    }

    /**
     * Method to sort account by account type
     */
    public void selectionSortAccountType() {
        int n = numAcct;

        for (int i = 0; i < n-1; i++) {
            int minIdx = i;
            for (int j = i+1; j < n; j++) {
                if (accounts[j].compareTo(accounts[minIdx]) < 0) {
                    minIdx = j;
                }
            }

            Account temp = accounts[minIdx];
            accounts[minIdx] = accounts[i];
            accounts[i] = temp;
        }
    }

    /**
     * Finds the index of an account that matches the given account for transactions.
     * @param account The account to find.
     * @return The index of the matching account, or NOT_FOUND if not found.
     */
    private int findForTransactions(Account account){
        int index = NOT_FOUND;
        for (int i = 0; i < numAcct; i++) {
            if (accounts[i].equals(account)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Method to check if account is found in database, then deposit money into account
     * @param account as Account
     */
    public void deposit(Account account) {
        int index = find(account);
        if (index == NOT_FOUND) {
            return;
        }
        accounts[index].balance += account.balance;
        Account acct = accounts[index];
        if (acct instanceof MoneyMarket) {
            MoneyMarket mmAccount = (MoneyMarket) acct;
            if (mmAccount.balance >= MoneyMarket.MIN_BALANCE_FEE_WAIVED) {
                mmAccount.isLoyal = true;
            }
            accounts[index] = mmAccount;
        }
    }

    /**
     * Checks if account is contained for transactions
     * @param account to be found
     * @return true if contained, false if not
     */
    public boolean containsForTransactions(Account account){
        return findForTransactions(account) != NOT_FOUND;
    }

    /**
     * Parent method to selectionSortAccountType
     * @return String of Accounts by type
     */
    public String printSorted(){
        selectionSortAccountType();
        String text = "";
        for (int i = 0; i < numAcct; i++) {
           text += accounts[i].toString() + "\n";
        }
        return text;
    }

    /**
     * Parent method to selectionSortAccountType, with additional fees and interests
     * @return text of accounts as String
     */
    public String printFeesAndInterests(){
        selectionSortAccountType();
        String text = "";
        for(int i = 0; i< numAcct; i++){
            text += accounts[i].stringWithFees() + "\n";
        }
        return text;
    }

    /**
     * Method to print accounts after updating the balance values with fees and interests
     * @return text of accounts as String
     */
    public String printUpdatedBalances(){
        selectionSortAccountType();
        String text = "";
        selectionSortAccountType();
        for(int i = 0; i < numAcct; i++) {
            accounts[i].balance += accounts[i].monthlyInterest();
            accounts[i].balance -= accounts[i].monthlyFee();
            if (accounts[i] instanceof MoneyMarket) {
                MoneyMarket mmAccount = (MoneyMarket) accounts[i];
                mmAccount.setWithdrawal(RESET_WITHDRAWAL);
                accounts[i] = mmAccount;
            }
            text += accounts[i].toString() + "\n";
        }
        return text;
    }

    /**
     * Method to check if the number of accounts is 0
     * @return true if empty, false otherwise
     */
    public boolean isEmpty(){
        return numAcct == 0;
    }
}
