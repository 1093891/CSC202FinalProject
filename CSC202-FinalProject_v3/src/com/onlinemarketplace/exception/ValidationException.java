
package com.onlinemarketplace.exception;

/**
  A general purpose custom exception for validation errors.
  This can be used when input data does not meet specified criteria (e.g., negative price, empty string).
 */
public class ValidationException extends Exception {
    
    //Constructs a new ValidationException with the specified detail message.
    public ValidationException(String message) {
        super(message);
    }
}
