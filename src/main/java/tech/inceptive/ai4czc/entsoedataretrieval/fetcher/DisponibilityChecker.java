/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;

/**
 * The class to verify if a data column is available at ENTSOE platform at a given date. Some data are not available
 * until a specific date. This class provides a method to check it.
 *
 * @author Andres Bel Alonso
 */
public class DisponibilityChecker {

    private static final Logger LOGGER = LogManager.getLogger(DisponibilityChecker.class);

    private static record Entry(String article, Area area) {

    }

    ;
    
    private static record SourceEntry(String article, Area area, LocalDateTime startDt) {

    }
    ;
    
    /**
     * Key : area + data type
     * Value : start of availability date
     */
    public Map<Entry, LocalDateTime> availabilityIndex;

    public DisponibilityChecker() {
        InputStream is = DisponibilityChecker.class.getResourceAsStream("availability.json");
        availabilityIndex = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            List<SourceEntry> sourceEntries = objectMapper.readValue(is,
                    TypeFactory.defaultInstance().constructCollectionLikeType(List.class, SourceEntry.class));
            for (SourceEntry curEntry : sourceEntries) {
                availabilityIndex.put(new Entry(curEntry.article, curEntry.area), curEntry.startDt);
            }
        } catch (IOException ex) {
            throw new DataRetrievalError(ex);
        }
    }

    /**
     *
     * @param article
     * @param startDate
     * @return The start date if data is already available or the date when when the data was available
     */
    public LocalDateTime checkAvailability(String article, LocalDateTime startDate, Area area) {
        Entry curEntry = new Entry(article, area);
        if (availabilityIndex.containsKey(curEntry)) {
            LocalDateTime dataStartDate = availabilityIndex.get(curEntry);
            if (dataStartDate.isAfter(startDate)) {
                return dataStartDate;
            }
        }
        // data seems always available
        return startDate;
    }

}
