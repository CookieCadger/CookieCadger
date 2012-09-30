package com.mattslifebytes.cookiecadger;

/**
 * A class to handle exceptions in CookieCadger.
 */
@SuppressWarnings("serial")
public class CookieCadgerException extends Exception {

    /**
     * Constructs a new CookieCadgerException with null as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to Throwable.initCause(java.lang.Throwable).
     */
    public CookieCadgerException() {
    	super();
    }

    /**
     * Constructs a new CookieCadgerException with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to Throwable.initCause(java.lang.Throwable).
     * 
     * @param message
     *            the detail message. The detail message is saved for later
     *            retrieval by the Throwable.getMessage() method.
     */
    public CookieCadgerException(String message) {
    	super(message);
    }

    /**
     * Constructs a new CookieCadgerException with the specified detail message
     * and cause. Note that the detail message associated with cause is not
     * automatically incorporated in this exception's detail message
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            Throwable.getMessage() method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            Throwable.getCause() method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     */
    public CookieCadgerException(String message, Throwable cause) {
    	super(message, cause);
    }

    /**
     * Constructs a new CookieCadgerException with the specified cause and a
     * detail message of (cause==null ? null : cause.toString()) (which
     * typically contains the class and detail message of cause).
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            Throwable.getCause() method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     */
    public CookieCadgerException(Throwable cause) {
    	super(cause);
    }
}
