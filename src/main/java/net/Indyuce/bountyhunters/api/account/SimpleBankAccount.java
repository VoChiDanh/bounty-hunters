package net.Indyuce.bountyhunters.api.account;

import net.Indyuce.bountyhunters.BountyHunters;
import org.apache.commons.lang.Validate;

public class SimpleBankAccount implements BankAccount {
    private final String account;

    public SimpleBankAccount(String input) {
        this.account = input;

        Validate.notNull(input, "Could not read bank account name");
    }

    @Override
    public void deposit(double amount) {
        BountyHunters.getInstance().getEconomy().bankDeposit(account, amount);
    }
}