package com.inn.paysense.exception;

/**
 * Paysense custom exception
 */
public class PaySenseException extends Exception{

    private int status;

    public PaySenseException(){
        super();
    }

    public PaySenseException(String message, int status){
        super(message);
        this.status = status;
    }

    public PaySenseException(String message, Throwable cause, int status){
        super(message,cause);
        this.status = status;
    }

    public PaySenseException(Throwable cause, int status){
        super(cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
