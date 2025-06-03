
package com.onlinemarketplace.exception;

//Custom exception thrown when delivery is not available for a specified address or region.
public class DeliveryUnavailableException extends Exception {
    
    //Constructs a new DeliveryUnavailableException with the specified detail message.
    public DeliveryUnavailableException(String message) {
        super(message);
    }
}
