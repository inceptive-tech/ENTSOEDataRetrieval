/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.CSVGenerator;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.GLDocumentCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.ENTSOEDataFetcher;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ColumnType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;

/**
 *
 * @author Andres Bel Alonso
 */
@RunWith(MockitoJUnitRunner.class)
public class TestENTSOEHistoricalDataRetrieval {

    private static final Logger LOGGER = LogManager.getLogger(TestENTSOEHistoricalDataRetrieval.class);

    @Mock
    private ENTSOEDataFetcher fetcher;

    @Mock
    private CSVGenerator csvGen;

    @InjectMocks
    private ENTSOEHistoricalDataRetrieval retriever = new ENTSOEHistoricalDataRetrieval("token",
            Set.of(ColumnType.ACTUAL_LOAD), "\'", ",", false);

    @Test
    public void testFetchDatasetActualLoadLessOneYear() {
        // the aim of this test is verify the fetcher and csvGen entries
        // so we mock the fetcher output and check both
        //given
        LocalDateTime startDate = LocalDateTime.of(2022, Month.FEBRUARY, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, Month.JANUARY, 7, 12, 0);
        GLMarketDocument mockedDoc = mock(GLMarketDocument.class);
        when(fetcher.fetchActualLoad(any(), eq(startDate), eq(endDate))).
                thenReturn(Optional.of(mockedDoc));

        //when
        File file = retriever.fetchDataset(startDate, endDate, Duration.ofMinutes(60), Set.of(Area.MONTENEGRO),
                Area.MONTENEGRO, Duration.ofDays(365));

        //then
        verify(fetcher, times(1)).fetchActualLoad(eq(Area.MONTENEGRO),
                eq(startDate), eq(endDate));
        verify(csvGen, times(1)).writeCSVFile(eq(Arrays.asList(
                new GLDocumentCSVTransformer(",", "\"", mockedDoc))), any(),
                eq(startDate), eq(endDate), eq(Duration.ofHours(1)));
    }

    @Test
    public void testFetchDatasetActualLoadMoreThanOneYear() {
        // the aim of this test is verify the fetcher and csvGen entries
        // so we mock the fetcher output and check both
        //given
        LocalDateTime startDate = LocalDateTime.of(2021, Month.FEBRUARY, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, Month.JANUARY, 7, 12, 0);
        GLMarketDocument mockedDoc1 = mock(GLMarketDocument.class);
        GLMarketDocument mockedDoc2 = mock(GLMarketDocument.class);
        LocalDateTime split1 = LocalDateTime.of(2022, Month.FEBRUARY, 1, 0, 0);
        when(fetcher.fetchActualLoad(any(), eq(startDate), eq(split1))).
                thenReturn(Optional.of(mockedDoc1));
        when(fetcher.fetchActualLoad(any(), eq(split1), eq(endDate))).
                thenReturn(Optional.of(mockedDoc2));
        //when
        File file = retriever.fetchDataset(startDate, endDate, Duration.ofMinutes(60), Set.of(Area.MONTENEGRO), 
                Area.MONTENEGRO, Duration.ofDays(365));

        //then
        // the calls of the fetcher should be splited
        verify(fetcher, times(1)).fetchActualLoad(eq(Area.MONTENEGRO),
                eq(startDate), eq(split1));
        verify(fetcher, times(1)).fetchActualLoad(eq(Area.MONTENEGRO),
                eq(split1), eq(endDate));
        verify(csvGen, times(1)).writeCSVFile(eq(Arrays.asList(
                new GLDocumentCSVTransformer(",", "\"", mockedDoc1),
                new GLDocumentCSVTransformer(",", "\"", mockedDoc2))), any(),
                eq(startDate), eq(endDate), eq(Duration.ofHours(1)));
    }

}
