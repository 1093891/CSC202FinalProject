
package com.onlinemarketplace.exception;

//Custom exception thrown when a harvest date is invalid (e.g., in the future).
public class HarvestDateException extends Exception {

     //Constructs a new HarvestDateException with the specified detail message.
    public HarvestDateException(String message) {
        super(message);
    }
}
