/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestAvailabilityChecker {

    private static final Logger LOGGER = LogManager.getLogger(TestAvailabilityChecker.class);

    @Test
    public void testCheckAvailability() {
        //the entry is ok
        //given
        DisponibilityChecker checker = new DisponibilityChecker();
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 0, 0);

        //when
        LocalDateTime res = checker.checkAvailability("6.1.A", ldt, Area.KOSOVO);

        //then
        assertEquals(true, res.equals(ldt));
    }

    @Test
    public void testCheckAvailabilityEntryNonAvailable() {
        // The entry is not available now
        //given
        DisponibilityChecker checker = new DisponibilityChecker();
        LocalDateTime ldt = LocalDateTime.of(2017, 1, 1, 0, 0);
        
        //when
        LocalDateTime res = checker.checkAvailability("6.1.B", ldt, Area.KOSOVO);
        
        //then
        LocalDateTime expected = LocalDateTime.of(2021,8,1,0,0);
        assertEquals(expected,res);
        
    }
}
