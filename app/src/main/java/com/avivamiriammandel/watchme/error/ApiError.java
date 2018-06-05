package com.avivamiriammandel.watchme.error;

/**
 * Created by aviva.miriam on 25 מרץ 2018.
 */

public class ApiError {
    private int statusCode;
    private String endpoint;
    private String message =  "Unknown Error.";

    public int getStatusCode() {
        return statusCode;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getMessage() {
        return message;
    }
}
