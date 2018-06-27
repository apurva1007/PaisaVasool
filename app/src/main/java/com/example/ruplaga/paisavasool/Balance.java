package com.example.ruplaga.paisavasool;

/**
 * Created by ruplaga on 6/20/2018.
 */

public class Balance {

    float bankAccount = 0;
    float paytm = 0;
    float cash = 0;

    public Balance() {
    }

    public Balance(float bankAccount, float paytm, float cash) {
        this.bankAccount = bankAccount;
        this.paytm = paytm;
        this.cash = cash;
    }

    public float getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(float bankAccount) {
        this.bankAccount = bankAccount;
    }

    public float getPaytm() {
        return paytm;
    }

    public void setPaytm(float paytm) {
        this.paytm = paytm;
    }

    public float getCash() {
        return cash;
    }

    public void setCash(float cash) {
        this.cash = cash;
    }
}


