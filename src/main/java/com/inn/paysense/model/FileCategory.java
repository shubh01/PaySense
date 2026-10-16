package com.inn.paysense.model;

public enum FileCategory {

    CURRENT_ACCOUNT("Current Account"),
    CREDIT_CARD("Credit Card"),
    NRI_ACCOUNT_OPENING("NRI Account Opening"),
    PERSONAL_LOANS("Personal Loan"),
    SAVING_ACCOUNT("Saving Account");

    private final String name;

    FileCategory(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

}
