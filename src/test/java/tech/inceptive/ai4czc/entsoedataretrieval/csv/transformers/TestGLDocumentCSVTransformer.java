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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.Point;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestGLDocumentCSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(TestGLDocumentCSVTransformer.class);

    @Test
    public void testWriteNextEntry() throws IOException {
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 4, 10, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        // we have writen to the output stream one time ",8242"
        assertEquals(true, out);
        verify(mockOS, times(1)).write(",8242");
    }

    @Test
    public void testWriteNextEntryOutOfBondsBefore() throws IOException {
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 1, 10, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(false, out);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryOutOfBondsAfter() throws IOException {
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 6, 2, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(false, out);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntryStartPoint() throws IOException {
        // Here our document starts at 2023-03-03T00:00Z and it receives a request for a document at this date
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 3, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(true, out);
        verify(mockOS, times(1)).write(",7146");
    }

    @Test
    public void testWriteNextEntryEndPoint() throws IOException {
        // Here our document ends at 2023-03-06T00:00Z and we ask this date. No writing should be done
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 6, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(false, out);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void testWriteNextEntrySplitedDoc() throws IOException {
        //given
        // take from a splited in many time series doc and ask for an entry on the second ts
        GLMarketDocument doc = getGLMarketDocument("ActualLoad2020.xml");
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2020, Month.SEPTEMBER, 12, 0, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(true, out);
        verify(mockOS, times(1)).write(",282");
    }

    @Test
    public void writeNextEntryMissingValue() throws IOException {
        // ask to write a 2020 value that is missing
        //given
        GLMarketDocument doc = getGLMarketDocument("ActualLoad2020.xml");
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2020, Month.SEPTEMBER, 11, 20, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        assertEquals(false, out);
        verify(mockOS, times(0)).write(any(String.class));
    }

    @Test
    public void writeNextEntryLessWeekAhead() throws IOException {
        // Week ahead has a day resolution (so two values of a day should be the same) and outputs a min and a max value
        //given
        GLMarketDocument doc = getGLMarketDocument("WeekAheadExample.xml");
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt1 = LocalDateTime.of(2023, Month.JANUARY, 4, 15, 0);
        LocalDateTime ldt2 = LocalDateTime.of(2023, Month.JANUARY, 4, 22, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out1 = transformer.writeNextEntry(ldt1, mockOS);
        boolean out2 = transformer.writeNextEntry(ldt2, mockOS);

        //then
        assertEquals(true, out1);
        verify(mockOS, times(2)).write(",272,438");
    }
    
    @Test
    public void testWriteNewEntryOverlappingEntry() throws IOException {
        // In week ahead predicitons we can have two time series overlapping, because one is to big. 
        // Here we have a time series from 2020-10-18T22:00Z to 2020-10-25T23:00Z (of 1 week and 1h) and the next 
        // from 2020-10-25T23:00Z to 2020-11-01T23:00Z (of 1 week). Check this is correctly handle.
        // given
        GLMarketDocument doc = getGLMarketDocument("WeakAheadOverlapping.xml");
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2020, 10, 25, 22, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);
        
        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);
        
        //then
        assertEquals(true, out);
        verify(mockOS, times(1)).write(",250,396");
        
    }
    
    @Test
    public void testWriteNewEntryDifferentSepator() throws IOException {
        // instead of clasical , we use ; as separator
        //given
        GLMarketDocument doc = getGLMarketLoadExample2();
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(";", "\"", doc);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 4, 10, 0);
        BufferedWriter mockOS = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ldt, mockOS);

        //then
        // we have writen to the output stream one time ",8242"
        assertEquals(true, out);
        verify(mockOS, times(1)).write(";8242");
    }

    private GLMarketDocument getGLMarketLoadExample2() {
        return getGLMarketDocument("LoadExample2.xml");
    }

    private GLMarketDocument getGLMarketDocument(String resourceName) {
        try {
            JAXBContext context = JAXBContext.newInstance(GLMarketDocument.class);
            return (GLMarketDocument) context.createUnmarshaller().unmarshal(TestGLDocumentCSVTransformer.class.getResource(resourceName).openStream());
        } catch (JAXBException | IOException ex) {
            throw new DataRetrievalError(ex);
        }
    }

}
