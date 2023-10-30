/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.csv;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Andres Bel Alonso
 */
public record ColumnDefinition(String colName) {
    private static final Logger LOGGER = LogManager.getLogger(ColumnDefinition.class);
 
}
