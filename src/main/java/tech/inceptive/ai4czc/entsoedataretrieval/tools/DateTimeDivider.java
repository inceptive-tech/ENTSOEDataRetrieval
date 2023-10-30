/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package tech.inceptive.ai4czc.entsoedataretrieval.tools;
 
 
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
 
/**
 * A class to provide a toolbox to split big time intervals in small ones. 
 * @author Andres Bel Alonso
 */
public class DateTimeDivider {
    
    private static final Logger LOGGER = LogManager.getLogger(DateTimeDivider.class);

    private DateTimeDivider() {
        // API class
    }
  
    public static List<LocalDateTime> splitInMilestones(LocalDateTime startDate, LocalDateTime endDate, Duration maxDur, 
            Duration minDur) {
        if(maxDur.minus(minDur).isNegative()) {
            throw new DataRetrievalRuntimeException("Max duration should be bigger than min duration ");
        }
        LocalDateTime nextStep = startDate.plus(maxDur);
        List<LocalDateTime> res = new ArrayList<>();
        res.add(startDate);
        while(nextStep.isBefore(endDate)) {
            res.add(nextStep);
            nextStep= nextStep.plus(maxDur);
        }
        LocalDateTime last = nextStep.minus(maxDur);
        if(Duration.between(last, endDate).isZero()) {
            return res;
        }
        if(Duration.between(nextStep, endDate).isZero()) {
            res.add(nextStep);
            return res;
        }
        if(maxDur.minus(minDur).isZero()) {
            throw new DataRetrievalRuntimeException("Max duration should be bigger than min duration ");
        }
        if(Duration.between(last, endDate).minus(minDur).isNegative()) {
            res.remove(res.size()-1);
            res.add(endDate.minus(minDur));
        }
        res.add(endDate);
        return res;
    }
}