/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package tech.inceptive.ai4czc.entsoedataretrieval.exceptions;
 
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
/**
 * In the case of a detected bug or critical issue that should stop directly the run
 * @author Andres Bel Alonso
 */
public class DataRetrievalError extends RuntimeException{
    private static final Logger LOGGER = LogManager.getLogger(DataRetrievalError.class);

    public DataRetrievalError(String message) {
        super(message);
    }

    public DataRetrievalError(String message, Throwable cause) {
        super(message, cause);
    }

    public DataRetrievalError(Throwable cause) {
        super(cause);
    }
    
    
}