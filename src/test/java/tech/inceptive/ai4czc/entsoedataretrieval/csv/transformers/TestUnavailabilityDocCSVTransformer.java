/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.UnavailabilityMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.custom.JAXBDeserializerHelper;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestUnavailabilityDocCSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(TestUnavailabilityDocCSVTransformer.class);

    @Test
    public void testWriteNextEntry() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ",", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2019, 7, 10, 12, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(true, out);
        verify(mockBW, times(1)).write(",1,1");
    }

    @Test
    public void testWriteNextEntryOutOfBondsBefore() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ",", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2018, 7, 10, 12, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(false, out);
        verify(mockBW, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryOutOfBondsAfter() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ",", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2020, 7, 10, 12, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(false, out);
        verify(mockBW, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryStartPoint() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ",", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2019, 7, 7, 22, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(true, out);
        verify(mockBW, times(1)).write(",1,1");
    }

    @Test
    public void testWriteNextEntryEndPoint() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ",", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2019, 7, 19, 22, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(false, out);
        verify(mockBW, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNewEntryDifferentSepator() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2019, 7, 10, 12, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(true, out);
        verify(mockBW, times(1)).write(";1;1");
    }

    @Test
    public void testWriteNewEntryNonScheduled() throws IOException {
        // this document contains an outage non scheduled
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMeUnscheduled.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2020, 4, 10, 4, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(true, out);
        verify(mockBW, times(1)).write(";1;0");
    }

    @Test
    public void testWriteNewEntryGenerationOutage() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("GenerationOutage.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");
        BufferedWriter mockBW = mock(BufferedWriter.class);
        LocalDateTime ldt = LocalDateTime.of(2023, 3, 10, 12, 0);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockBW);

        //then
        assertEquals(true, out);
        verify(mockBW, times(1)).write(";1;1;114.0");
    }

    @Test
    public void testComputeColumnDefTransmission() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutageGridRsMe.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");

        //when
        List<ColumnDefinition> colDef = transformer.computeColumnDef();

        //then
        assertEquals(2, colDef.size());
        assertEquals(UnavailabilityDocCSVTransformer.TRANSMISSION_UNAVAILABILITY_BASE_NAME + "10T-ME-RS-00001G",
                colDef.get(0).colName());
        assertEquals(UnavailabilityDocCSVTransformer.UNAVAILABILITY_SCHEDULED_BN + "10T-ME-RS-00001G",
                colDef.get(1).colName());
    }

    @Test
    public void testComputeColumnDefGeneration() throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("GenerationOutage.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");
        
        //when
        List<ColumnDefinition> colDef = transformer.computeColumnDef();
        
        //then
        assertEquals(3, colDef.size());
        assertEquals(UnavailabilityDocCSVTransformer.GENERATION_UNAVAILABILITY_BASE_NAME + "21W00000000PIVA9", 
                colDef.get(0).colName());
        assertEquals(UnavailabilityDocCSVTransformer.UNAVAILABILITY_SCHEDULED_BN + "21W00000000PIVA9",
                colDef.get(1).colName());
        assertEquals("ME_" + UnavailabilityDocCSVTransformer.NOMINAL_OUTAGE_POWER + "21W00000000PIVA9", 
                colDef.get(2).colName());
    }
    
    @Test
    public void testComputeColumnDefTransmissionMissingAsset()  throws IOException {
        //given
        UnavailabilityMarketDocument doc = readUnavailabilityDoc("OutagaeGridUnknow.xml");
        UnavailabilityDocCSVTransformer transformer = new UnavailabilityDocCSVTransformer(doc, ";", "\"");

        //when
        List<ColumnDefinition> colDef = transformer.computeColumnDef();
        
        //then
        assertEquals(2, colDef.size());
        assertEquals(UnavailabilityDocCSVTransformer.TRANSMISSION_UNAVAILABILITY_BASE_NAME + "Unknown_asset_IT-CS_ME",
                colDef.get(0).colName());
        assertEquals(UnavailabilityDocCSVTransformer.UNAVAILABILITY_SCHEDULED_BN + "Unknown_asset_IT-CS_ME",
                colDef.get(1).colName());
    }

    private UnavailabilityMarketDocument readUnavailabilityDoc(String xmlFilename) throws IOException {
        try {
            return JAXBDeserializerHelper.doJAXBUnmarshall(
                    TestUnavailabilityDocCSVTransformer.class.getResource(xmlFilename).openStream(),
                    UnavailabilityMarketDocument.class, true);
        } catch (JAXBException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }
}
