/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.datastructures;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;

/**
 *
 * @author andres
 */
public class TestGLMarketDocument {
    
    private static final Logger LOGGER = LogManager.getLogger(TestGLMarketDocument.class);
    
    @Test
    public void testXMLParsing() throws IOException, JAXBException {
        //given
        String content = FileUtils.readFileToString(new File(TestGLMarketDocument.class.getResource("ActualTotalLocalExample.xml").getPath()),
                Charset.defaultCharset());
        
        //when
        JAXBContext context = JAXBContext.newInstance(GLMarketDocument.class);
        GLMarketDocument doc = (GLMarketDocument) context.createUnmarshaller().unmarshal(TestGLMarketDocument.class.getResource("ActualTotalLocalExample.xml").openStream());

        //then
        assertEquals("5693afe33ce749e4b0cea17f1f64f211", doc.getMRID());
        assertEquals("1", doc.getRevisionNumber());
        assertEquals("A65", doc.getType());
        assertEquals("A16", doc.getProcessProcessType());
        assertEquals("10X1001A1001A450", doc.getSenderMarketParticipantMRID().getValue());
        assertEquals("A32", doc.getSenderMarketParticipantMarketRoleType());
        assertEquals("10X1001A1001A450", doc.getSenderMarketParticipantMRID().getValue());
        assertEquals("A33", doc.getReceiverMarketParticipantMarketRoleType());
        assertEquals("2016-02-26T07:24:53Z", doc.getCreatedDateTime().toString());
        assertEquals("2015-12-31T23:00Z", doc.getTimePeriodTimeInterval().getStart());
        assertEquals(LocalDateTime.of(2015, Month.DECEMBER, 31, 23, 0), doc.getTimePeriodTimeInterval().getLDTStart());
        assertEquals("2016-12-31T23:00Z",doc.getTimePeriodTimeInterval().getEnd());
        assertEquals(LocalDateTime.of(2016, Month.DECEMBER, 31, 23, 0).format(DateTimeFormatter.ISO_DATE_TIME), doc.getTimePeriodTimeInterval().getLDTEnd().format(DateTimeFormatter.ISO_DATE_TIME));
        assertEquals("1", doc.getTimeSeries().get(0).getMRID());
        assertEquals("A04", doc.getTimeSeries().get(0).getBusinessType());
        assertEquals("A01", doc.getTimeSeries().get(0).getObjectAggregation());
        assertEquals("10YCZ-CEPS-----N", doc.getTimeSeries().get(0).getOutBiddingZoneDomainMRID().getValue());
        assertEquals("MAW", doc.getTimeSeries().get(0).getQuantityMeasureUnitName());
        assertEquals("A01", doc.getTimeSeries().get(0).getCurveType());
        assertEquals("2015-12-31T23:00Z", doc.getTimeSeries().get(0).getPeriod().get(0).getTimeInterval().getStart());
        assertEquals("2016-12-31T23:00Z", doc.getTimeSeries().get(0).getPeriod().get(0).getTimeInterval().getEnd());
        assertEquals(60, doc.getTimeSeries().get(0).getPeriod().get(0).getResolution().getMinutes());
        assertEquals(1, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(0).getPosition());
        assertEquals(6288, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(0).getQuantity().intValue());
        assertEquals(2, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(1).getPosition());
        assertEquals(8456, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(1).getQuantity().intValue());
        assertEquals(3, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(2).getPosition());
        assertEquals(7895, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(2).getQuantity().intValue());
    }
 
}
