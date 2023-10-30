/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs;


import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Andres Bel Alonso
 */
public enum ProcessType {
    DAY_AHEAD("A01", "Day ahead"),
    INTRA_DAY_INCREMENTAL("A02", "Intra day incremental"),
    REALISED("A16", "Realised"),
    INTRA_DAY_TOTAL("A18", "Intraday total"),
    WEEK_AHEAD("A31", "Week ahead"),
    MONTH_AHEAD("A32", "Month ahead"),
    YEAR_AHEAD("A33", "Year ahead"),
    SYNCHRONISATION_PROCESS("A39", "Synchronisation process"),
    INTRADAY_PROCESS("A40", "Intraday process"),
    REPLACEMENT_RESERVE("A46", "Replacement reserve"),
    MANUAL_FREQUENCY_RESTORATION_RESERVE("A47", "Manual frequency restoration reserve"),
    AUTOMATIC_FREQUENCY_RESTORATION_RESERVE("A51", "Automatic frequency restoration reserve"),
    FREQUENCY_CONTAINMENT_RESERVE("A52", "Frequency containment reserve"),
    FREQUENCY_RESTORATION_RESERVE("A56", "Frequency restoration reserve"),
    SCHEDULED_ACTIVATION_MFRR("A60", "Scheduled activation mFRR"),
    DIRECT_ACTIVATION_MFRR("A61", "Direct activation mFRR"),
    CENTRAL_SELECTION_AFRR("A67", "Central Selection aFRR"),
    LOCAL_SELECTION_AFRR("A68", "Local Selection aFRR");

    
    private static final Logger LOGGER = LogManager.getLogger(ProcessType.class);
    
    private static final Map<String, ProcessType> ID_TO_PROCESS_TYPE = new HashMap<>();
    
    static {
        for(ProcessType process : ProcessType.values()) {
            ID_TO_PROCESS_TYPE.put(process.getId(), process);
        }
    }
 
    // id and descriptiond from https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html
    // Annex 7 Process Type
    private final String id;
    private final String description;

    private ProcessType(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    
    public static ProcessType fromId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        ProcessType myEnum = ID_TO_PROCESS_TYPE.get(id);
        if (myEnum == null) {
            throw new IllegalArgumentException("No enum instance found with id: " + id);
        }
        return myEnum;
    }
}
