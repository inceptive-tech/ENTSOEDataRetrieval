/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;
 
 
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
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
 
/**
 *
 * @author Andres Bel Alonso
 */
public class TestCSVTransformer {
    
    private static final Logger LOGGER = LogManager.getLogger(TestCSVTransformer.class);
  
        // the mehtod getPeriodForTimeStamp does not check the correctness of the date. It is assumed to be correct
    @Test
    public void testGetPeriodForTimeStamp() {
        //given
        GLMarketDocument doc = getDocument("LoadExample2.xml", GLMarketDocument.class);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 3, 7, 0);

        //when
        List<GLDocumentCSVTransformer.MarkedTS> res = CSVTransformer.getPeriodForTimeStamp(doc.getTimeSeries(), ldt);

        //then
        assertEquals(1, res.size());
        assertEquals(7, res.get(0).selectedPoint());
        assertEquals(0, res.get(0).selectedPeriod());
        assertEquals(9304, res.get(0).getSelectedPointValue());
    }

    @Test
    public void testGetPeriodForTimeStartingPoint() {
        //given
        GLMarketDocument doc = getDocument("LoadExample2.xml", GLMarketDocument.class);
        LocalDateTime ldt = LocalDateTime.of(2023, Month.MARCH, 3, 0, 0);

        //when
        List<GLDocumentCSVTransformer.MarkedTS> res = CSVTransformer.getPeriodForTimeStamp(doc.getTimeSeries(), ldt);

        //then
        assertEquals(1, res.size());
        assertEquals(0, res.get(0).selectedPoint());
        assertEquals(0, res.get(0).selectedPeriod());
        assertEquals(7146, res.get(0).getSelectedPointValue());
    }
    
    private <T> T getDocument(String resourceName, Class<T> clazz) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            return (T) context.createUnmarshaller().unmarshal(TestCSVTransformer.class.getResource(resourceName).openStream());
        } catch (JAXBException | IOException ex) {
            throw new DataRetrievalError(ex);
        }
    }

}