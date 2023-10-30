/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.tools;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestDateTimeDivider {

    private static final Logger LOGGER = LogManager.getLogger(TestDateTimeDivider.class);

    @Test
    public void testSplitInMilestones() {
        //given
        LocalDateTime startDate = LocalDateTime.of(2019, Month.MARCH, 2, 14, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, Month.JUNE, 30, 17, 42);
        Duration maxDur = Duration.ofDays(365);
        Duration minDur = Duration.ofMinutes(60);

        //when
        List<LocalDateTime> res = DateTimeDivider.splitInMilestones(startDate, endDate, maxDur, minDur);

        //then
        assertEquals(6, res.size());
        assertEquals(LocalDateTime.of(2019, Month.MARCH, 2, 14, 0), res.get(0));
        assertEquals(LocalDateTime.of(2020, Month.MARCH, 1, 14, 0), res.get(1));
        assertEquals(LocalDateTime.of(2021, Month.MARCH, 1, 14, 0), res.get(2));
        assertEquals(LocalDateTime.of(2022, Month.MARCH, 1, 14, 0), res.get(3));
        assertEquals(LocalDateTime.of(2023, Month.MARCH, 1, 14, 0), res.get(4));
        assertEquals(LocalDateTime.of(2023, Month.JUNE, 30, 17, 42), res.get(5));

    }

    @Test
    public void testSplitInMilestonesNoSplit() {
        //given
        LocalDateTime startDate = LocalDateTime.of(2023, Month.MARCH, 14, 21, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, Month.JUNE, 28, 17, 42);
        Duration maxDur = Duration.ofDays(365);
        Duration minDur = Duration.ofMinutes(60);

        //when
        List<LocalDateTime> res = DateTimeDivider.splitInMilestones(startDate, endDate, maxDur, minDur);

        //then
        assertEquals(2, res.size());
        assertEquals(startDate, res.get(0));
        assertEquals(endDate, res.get(1));
    }

    @Test
    public void testSplitInMilestonesToSmallLastSplit() {
        //given
        LocalDateTime startDate = LocalDateTime.of(2022, Month.MARCH, 2, 14, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, Month.MARCH, 2, 14, 42);
        Duration maxDur = Duration.ofDays(365);
        Duration minDur = Duration.ofMinutes(60);
        
        //when
        List<LocalDateTime> res = DateTimeDivider.splitInMilestones(startDate, endDate, maxDur, minDur);
        
        //then
        assertEquals(3, res.size());
        assertEquals(startDate, res.get(0));
        assertEquals(LocalDateTime.of(2023, Month.MARCH, 2, 13, 42), res.get(1));
        assertEquals(endDate,res.get(2));
    }
}
