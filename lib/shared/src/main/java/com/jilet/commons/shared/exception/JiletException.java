package com.jilet.commons.shared.exception;

import lombok.Getter;

@Getter
public class JiletException extends Exception{

    //Used for categorizing exceptions and for client-side handling
    private final int errorCode;

    //Used for setting the HTTP response status code when this exception is thrown in a web context
    private final int httpStatusCode;

    public JiletException(int errorCode, int httpStatusCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public JiletException(int errorCode, int httpStatusCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

     public JiletException(int errorCode, int httpStatusCode, Throwable cause) {
        super(cause);
         this.errorCode = errorCode;
         this.httpStatusCode = httpStatusCode;
    }
}
