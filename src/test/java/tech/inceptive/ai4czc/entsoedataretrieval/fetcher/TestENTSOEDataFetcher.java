/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.PublicationMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.UnavailabilityMarketDocument;

/**
 *
 * @author Andres Bel Alonso
 */
@RunWith(MockitoJUnitRunner.class)
public class TestENTSOEDataFetcher {

    private static final Logger LOGGER = LogManager.getLogger(TestENTSOEDataFetcher.class);

    private String authToken = "test_auth_token";

    @Mock
    private HttpBridge mockBridge;

    @Mock
    private DisponibilityChecker checker;

    @InjectMocks
    private ENTSOEDataFetcher dataFetcher = new ENTSOEDataFetcher(authToken, false);

    @Before
    public void setUp() {
        // disponibility checker always available
        when(checker.checkAvailability(any(), any(), any())).thenAnswer(a -> {
            LocalDateTime entryDate = a.getArgument(1);
            return entryDate;
        });
    }

    // do not think a test is needed
    @Test
    public void testFetchActualLoad() {
        // Verify that the HttpBridge is correctly call during fech load operation
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> parameters = Map.of("securityToken", authToken, "documentType", "A65",
                "processType", "A16",
                "outBiddingZone_Domain",
                "10YCS-CG-TSO---S",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        GLMarketDocument mockDoc = mock(GLMarketDocument.class);
        when(mockBridge.doGetOperation(eq(parameters), eq(ENTSOEDataFetcher.BASE_URL), any())).
                thenReturn(mockDoc);
        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchActualLoad(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        //verify correct calls
        verify(mockBridge, times(1)).doGetOperation(eq(parameters),
                eq(ENTSOEDataFetcher.BASE_URL), any());
        //verify correct outputs
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

//    @Test
    public void testFetchActualLoadBigGap() {

    }

//    @Test
    public void testFetchActualLoadSmallGap() {

    }

//    @Test
    public void testFetchActualLoadHandleDataRetrieval() {

    }

    @Test
    public void testFetchActualLoadDataNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.A"), eq(periodStart),
                eq(Area.MONTENEGRO))).thenAnswer(a -> {
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
            return start;
        });
        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchActualLoad(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(checker, times(1)).checkAvailability(eq("6.1.A"), eq(periodStart),
                eq(Area.MONTENEGRO));
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchActualLoadDataPartiallyAvailable() {
        // the data is available only from 1st hune 2023, ask only this data
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.A"), eq(periodStart),
                eq(Area.MONTENEGRO))).thenAnswer(a -> {
            LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
            return start;
        });

        //when
        dataFetcher.fetchActualLoad(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> extectedParams = Map.of("securityToken", authToken, "documentType", "A65",
                "processType", "A16",
                "outBiddingZone_Domain",
                "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(extectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());
    }

    @Test
    public void testFetchDayAheadLoadForecast() {
        //verify the input parameters
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A65",
                "processType", "A01",
                "outBiddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        GLMarketDocument mockDoc = mock(GLMarketDocument.class);
        when(mockBridge.doGetOperation(eq(params), eq(ENTSOEDataFetcher.BASE_URL), any())).
                thenReturn(mockDoc);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchDayAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        //verify correct calls
        verify(mockBridge, times(1)).doGetOperation(eq(params),
                eq(ENTSOEDataFetcher.BASE_URL), any());
        //verify correct outputs
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

    @Test
    public void testFetchDayAheadLoadForecastOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 2, 1, 0, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchDayAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchDayAheadLoadForecastMinimunOneDayInterval() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2022, 1, 1, 23, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchDayAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchDayAheadLoadForecastDataNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.B"), eq(periodStart), eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
                    return start;
                });
        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchDayAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(checker, times(1)).checkAvailability(eq("6.1.B"), eq(periodStart),
                eq(Area.MONTENEGRO));
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchDayAheadLoadForecastDataPartiallyAvailable() {
        // the data is available only from 1st hune 2023, ask only this data
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.B"), eq(periodStart),
                eq(Area.MONTENEGRO))).thenAnswer(a -> {
            LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
            return start;
        });

        //when
        dataFetcher.fetchDayAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> extectedParams = Map.of("securityToken", authToken,
                "documentType", "A65",
                "processType", "A01",
                "outBiddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(extectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());
    }

    @Test
    public void testfetchWeekAheadLoadForecast() {
        //verify the input parameters
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A65",
                "processType", "A31",
                "outBiddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        GLMarketDocument mockDoc = mock(GLMarketDocument.class);
        when(mockBridge.doGetOperation(eq(params), eq(ENTSOEDataFetcher.BASE_URL), any())).
                thenReturn(mockDoc);
        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchWeekAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        //verify correct calls
        verify(mockBridge, times(1)).doGetOperation(eq(params),
                eq(ENTSOEDataFetcher.BASE_URL), any());
        //verify correct outputs
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

    @Test
    public void testFechWeekAheadLoadForecastOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 2, 1, 0, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchWeekAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFechWeekAheadLoadForecastMinimunOneWeek() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2022, 1, 3, 0, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchWeekAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFechWeekAheadLoadForecastDataNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.C"), eq(periodStart), eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
                    return start;
                });
        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchWeekAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(checker, times(1)).checkAvailability(eq("6.1.C"), eq(periodStart),
                eq(Area.MONTENEGRO));
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFechWeekAheadLoadForecastDataPartiallyAvailable() {
        // the data is available only from 1st hune 2023, ask only this data
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("6.1.C"), eq(periodStart),
                eq(Area.MONTENEGRO))).thenAnswer(a -> {
            LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
            return start;
        });

        //when
        dataFetcher.fetchWeekAheadLoadForecast(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> extectedParams = Map.of("securityToken", authToken,
                "documentType", "A65",
                "processType", "A31",
                "outBiddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(extectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());
    }

    @Test
    public void testFetchAggregatedGenerationType() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A75",
                "processType", "A16",
                "in_Domain", "10YCS-SERBIATSOV",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        GLMarketDocument mockDoc = mock(GLMarketDocument.class);
        when(mockBridge.doGetOperation(eq(params), eq(ENTSOEDataFetcher.BASE_URL), any())).
                thenReturn(mockDoc);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchAggregatedGenerationType(Area.SERBIA, periodStart, periodEnd);

        //then
        verify(mockBridge, times(1)).doGetOperation(eq(params),
                eq(ENTSOEDataFetcher.BASE_URL), any());
        //verify correct outputs
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

    @Test
    public void testFetchAggregatedGenerationTypeOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 2, 1, 0, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchAggregatedGenerationType(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchAggregatedGenerationTypeMinimal24hLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2022, 1, 1, 12, 0);

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchAggregatedGenerationType(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchAggregatedGenerationTypeDataNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("16.1.B&C"), eq(periodStart), eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
                    return start;
                });

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchAggregatedGenerationType(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchAggregatedGenerationTypeDataPartiallyAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("16.1.B&C"), eq(periodStart), eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        Optional<GLMarketDocument> doc = dataFetcher.fetchAggregatedGenerationType(Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> expectedParams = Map.of("securityToken", authToken,
                "documentType", "A75",
                "processType", "A16",
                "in_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(expectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());

    }

    @Test
    public void testFetchPhysicalFlows() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A11",
                "in_Domain", "10YCS-CG-TSO---S",
                "out_Domain", "10YBA-JPCC-----D",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        PublicationMarketDocument mockDoc = mock(PublicationMarketDocument.class);
        when(mockBridge.doGetOperation(eq(params), eq(ENTSOEDataFetcher.BASE_URL), any())).
                thenReturn(mockDoc);
        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO,
                Area.BOSNIA, periodStart, periodEnd);

        //then
        verify(mockBridge, times(1)).doGetOperation(eq(params),
                eq(ENTSOEDataFetcher.BASE_URL), any());
        //verify correct outputs
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

    @Test
    public void testFetchPhysicalFlowsOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 2, 1, 0, 0);

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO,
                Area.BOSNIA, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());

    }

    @Test
    public void testFetchPhysicalFlowsOneHourMinimun() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 14, 15, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2022, 1, 14, 15, 45);

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO,
                Area.BOSNIA, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchPhysicalFlowsNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("12.1.G"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
                    return start;
                });

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO,
                Area.BOSNIA, periodStart, periodEnd);
        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchPhysicalFlowsPartiallyAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("12.1.G"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO, Area.BOSNIA, periodStart, periodEnd);

        //then
        Map<String, String> expectedParams = Map.of("securityToken", authToken,
                "documentType", "A11",
                "in_Domain", "10YCS-CG-TSO---S",
                "out_Domain", "10YBA-JPCC-----D",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(expectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());
    }

    @Test
    public void testFetchPhysicalFlowsOneSidePartiallyAvailable() {
        // The physical flows are only available on kosovo from 1-6-2022 and it is the out domain. The out domain should
        // also be consider
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("12.1.G"), eq(periodStart), eq(Area.KOSOVO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });
        when(checker.checkAvailability(eq("12.1.G"), eq(periodStart), eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2017, 1, 1, 0, 0);
                    return start;
                });
        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchPhysicalFlows(Area.MONTENEGRO, Area.KOSOVO,
                periodStart, periodEnd);

        //then
        Map<String, String> expectedParams = Map.of("securityToken", authToken,
                "documentType", "A11",
                "in_Domain", "10YCS-CG-TSO---S",
                "out_Domain", "10Y1001C--00100H",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(expectedParams),
                eq(ENTSOEDataFetcher.BASE_URL), any());
    }

    @Test
    public void testFetchTransmissionGridOutages() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A78",
                "in_Domain", "10YCS-CG-TSO---S",
                "out_Domain", "10YCS-SERBIATSOV",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        List<UnavailabilityMarketDocument> mockDocs = Mockito.mock(List.class);
        when(mockBridge.doZipGetOperation(eq(params), any(),
                eq(UnavailabilityMarketDocument.class))).thenReturn(mockDocs);
        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchTransmissionGridOutages(Area.MONTENEGRO,
                Area.SERBIA, periodStart, periodEnd);

        //then
        assertEquals(true, Objects.equals(doc, mockDocs));
        verify(mockBridge, times(1)).doZipGetOperation(eq(params), any(), any());
    }

    @Test
    public void testFetchTransmissionGridOutagesOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 4, 1, 0, 0);

        //when
        List<UnavailabilityMarketDocument> res = dataFetcher.fetchTransmissionGridOutages(Area.BOSNIA, Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        assertEquals(true, res.isEmpty());
        verify(mockBridge, times(0)).doZipGetOperation(any(), any(), any());
    }

    @Test
    public void testFetchTransmissionGridOutagesHandleError() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(mockBridge.doZipGetOperation(any(), any(), any())).thenThrow(DataRetrievalRuntimeException.class);

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchTransmissionGridOutages(Area.MONTENEGRO,
                Area.SERBIA, periodStart, periodEnd);

        //then
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchTransmissionGridOutagesNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("10.1.A&B"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 6, 1, 0, 0);
                    return start;
                });

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchTransmissionGridOutages(Area.MONTENEGRO,
                Area.SERBIA, periodStart, periodEnd);

        //then
        assertEquals(true, doc.isEmpty());
        verify(mockBridge, times(0)).doZipGetOperation(any(), any(), any());
    }

    @Test
    public void testFetchTransmissionGridOutagesPartiallyAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("10.1.A&B"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchTransmissionGridOutages(Area.MONTENEGRO,
                Area.SERBIA, periodStart, periodEnd);

        //then
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A78",
                "in_Domain", "10YCS-CG-TSO---S",
                "out_Domain", "10YCS-SERBIATSOV",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doZipGetOperation(eq(params), any(), any());
    }

    @Test
    public void testFetchGenerationUnitOutages() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A80",
                "biddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        List<UnavailabilityMarketDocument> mockDocs = Mockito.mock(List.class);
        when(mockBridge.doZipGetOperation(eq(params), any(),
                eq(UnavailabilityMarketDocument.class))).thenReturn(mockDocs);

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchGenerationUnitOutages(Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        //verify correct outputs
        assertEquals(true, mockDocs.equals(doc));
        verify(mockBridge, times(1)).doZipGetOperation(eq(params), any(), any());
    }

    @Test
    public void testFetchGenerationUnitOutagesOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2024, 1, 1, 0, 0);

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchGenerationUnitOutages(Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        assertEquals(true, doc.isEmpty());
        verify(mockBridge, times(0)).doZipGetOperation(any(), any(), any());
    }

    @Test
    public void testFetchGenerationUnitOutagesHandleError() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(mockBridge.doZipGetOperation(any(), any(), any())).thenThrow(DataRetrievalRuntimeException.class);

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchGenerationUnitOutages(Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        assertEquals(true, doc.isEmpty());

    }

    @Test
    public void testFetchGenerationUnitOutagesNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("15.1.A&B"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 6, 1, 0, 0);
                    return start;
                });

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchGenerationUnitOutages(Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        assertEquals(true, doc.isEmpty());
        verify(mockBridge, times(0)).doZipGetOperation(any(), any(), any());
    }

    @Test
    public void testFetchGenerationUnitOutagesPartiallyAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("15.1.A&B"), eq(periodStart), any()))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        List<UnavailabilityMarketDocument> doc = dataFetcher.fetchGenerationUnitOutages(Area.MONTENEGRO,
                periodStart, periodEnd);

        //then
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A80",
                "biddingZone_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doZipGetOperation(eq(params), any(), any());

    }

    @Test
    public void testFetchWeekAheadCapacityForecast() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A61",
                "contract_MarketAgreement.Type", "A02",
                "in_Domain", "10YCS-SERBIATSOV",
                "out_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202201010000",
                "periodEnd", "202301010000");

        PublicationMarketDocument mockDoc = mock(PublicationMarketDocument.class);
        when(mockBridge.doGetOperation(eq(params), any(), eq(PublicationMarketDocument.class))).
                thenReturn(mockDoc);

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(1)).doGetOperation(eq(params), any(),
                eq(PublicationMarketDocument.class));
        assertEquals(true, doc.isPresent());
        assertEquals(mockDoc, doc.get());
    }

    @Test
    public void testFetchWeekAheadCapacityForecastOneYearLimit() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 2, 1, 0, 0);

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testFetchWeekAheadCapacityForecastOneWeekMinimun() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2022, 1, 3, 0, 0);

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());
    }

    @Test
    public void testfetchWeekAheadCapacityForecastNonAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("11.1.A.C"), eq(periodStart),
                eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2024, 6, 1, 0, 0);
                    return start;
                });

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        verify(mockBridge, times(0)).doGetOperation(any(), any(), any());
        assertEquals(true, doc.isEmpty());

    }

    @Test
    public void testFetchWeekAheadCapacityForecastPariallyAvailable() {
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(eq("11.1.A.C"), eq(periodStart),
                eq(Area.MONTENEGRO)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A61",
                "contract_MarketAgreement.Type", "A02",
                "in_Domain", "10YCS-SERBIATSOV",
                "out_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(params), any(),
                eq(PublicationMarketDocument.class));
    }

    @Test
    public void testFetchWeekAheadCapacityForecastPariallyAvailableOnOtherSide() {
        //all the SERBIA exchanges are partially available. Take into account
        //given
        LocalDateTime periodStart = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime periodEnd = LocalDateTime.of(2023, 1, 1, 0, 0);
        when(checker.checkAvailability(any(), any(), any())).thenAnswer(a -> {
            LocalDateTime dt = a.getArgument(1);
            return dt;
        });
        when(checker.checkAvailability(eq("11.1.A.C"), eq(periodStart),
                eq(Area.SERBIA)))
                .thenAnswer(a -> {
                    LocalDateTime start = LocalDateTime.of(2022, 6, 1, 0, 0);
                    return start;
                });

        //when
        Optional<PublicationMarketDocument> doc = dataFetcher.fetchWeekAheadCapacityForecast(Area.SERBIA,
                Area.MONTENEGRO, periodStart, periodEnd);

        //then
        Map<String, String> params = Map.of("securityToken", authToken,
                "documentType", "A61",
                "contract_MarketAgreement.Type", "A02",
                "in_Domain", "10YCS-SERBIATSOV",
                "out_Domain", "10YCS-CG-TSO---S",
                "periodStart", "202206010000",
                "periodEnd", "202301010000");
        verify(mockBridge, times(1)).doGetOperation(eq(params), any(),
                eq(PublicationMarketDocument.class));
    }

}
