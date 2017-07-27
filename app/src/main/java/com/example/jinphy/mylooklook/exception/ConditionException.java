package com.example.jinphy.mylooklook.exception;

/**
 * Created by jinphy on 2017/7/27.
 */

public class ConditionException extends Exception {

    public ConditionException() {
    }

    public ConditionException(String message) {
        super(message);
    }

    public ConditionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConditionException(Throwable cause) {
        super(cause);
    }

    public ConditionException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
