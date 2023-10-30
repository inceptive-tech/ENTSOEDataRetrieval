/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.exceptions;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andres
 */
public class DataRetrievalRuntimeException extends RuntimeException{
    private static final Logger LOGGER = LogManager.getLogger(DataRetrievalRuntimeException.class);

    public DataRetrievalRuntimeException(String message) {
        super(message);
    }

    public DataRetrievalRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataRetrievalRuntimeException(Throwable cause) {
        super(cause);
    }

    public DataRetrievalRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
 
    
}
