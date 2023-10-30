/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.PublicationMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.custom.JAXBDeserializerHelper;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestPublicationDocCSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(TestPublicationDocCSVTransformer.class);

    @Test
    public void testWriteNextEntry() throws IOException {
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, Month.JANUARY, 1, 14, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, true);
        verify(mockOS, times(1)).write(",792");
    }

    @Test
    public void testWriteNextEntryOutOfBondsBefore() throws IOException {
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2019, Month.JANUARY, 1, 14, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, false);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryOutOfBondsAfter() throws IOException {
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, Month.MARCH, 1, 14, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, false);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryStartPoint() throws IOException {
        //given
        // Doc starts at 1/1/2020 at 00:00 and we ask this time. It should write
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, true);
        verify(mockOS, times(1)).write(",947");
    }

    @Test
    public void testWriteNextEntryEndPoint() throws IOException {
        //given
        // Doc starts at 1/1/2020 at 00:00 and we ask this time. It should write
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, Month.JANUARY, 3, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, false);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNewEntryDifferentSepator() throws IOException {
        // instead of clasical , we use ; as separator
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEBA.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ";", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, Month.JANUARY, 1, 14, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, true);
        verify(mockOS, times(1)).write(";792");
    }

    @Test
    public void testWriteMissingEntry() throws IOException {
        // we ask to the me-it doc, the value for 2019-12-27T00:00:00 which should not crash
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("PhysFlowMEIT.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2019, 12, 27, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(out, false);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteCapcityForecast() throws IOException {
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("CapacityForecastWeekME_RS.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        LocalDateTime ldt = LocalDateTime.of(2020, 2, 1, 7, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(true, out);
        verify(mockOS, times(1)).write(",400");
    }

    @Test
    public void testComputeColumnDefCapacityForecast() throws IOException {
        //given
        PublicationMarketDocument pubDoc = getPublicationDoc("CapacityForecastWeekME_RS.xml");
        PublicationDocCSVTransformer transformer = new PublicationDocCSVTransformer(pubDoc, ",", "\"");
        
        //when
        List<ColumnDefinition> colDef = transformer.computeColumnDef();
        
        //then
        assertEquals(1, colDef.size());
        assertEquals("transfer_capacity_ME_to_RS",colDef.get(0).colName());
    }

    private PublicationMarketDocument getPublicationDoc(String resourceName) throws IOException {
        try {
            return JAXBDeserializerHelper.doJAXBUnmarshall(
                    TestPublicationDocCSVTransformer.class.getResource(resourceName).openStream(),
                    PublicationMarketDocument.class, true);
        } catch (JAXBException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }
}
