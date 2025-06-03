
package com.onlinemarketplace.exception;

//Custom exception thrown when attempting to order a product that is out of its seasonal availability.
public class OutOfSeasonException extends Exception {
    
    //Constructs a new OutOfSeasonException with the specified detail message.
    public OutOfSeasonException(String message) {
        super(message);
    }
}
