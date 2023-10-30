/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.custom;

import java.io.InputStream;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;

/**
 *
 * @author Andres Bel Alonso
 */
public class JAXBDeserializerHelper {

    private static final Logger LOGGER = LogManager.getLogger(JAXBDeserializerHelper.class);

    private JAXBDeserializerHelper() {
        // API Class
    }

    public static <T> T doJAXBUnmarshall(InputStream source, Class<T> clazz) throws JAXBException {
        return doJAXBUnmarshall(source, clazz, false);
    }
    

    public static <T> T doJAXBUnmarshall(InputStream source, Class<T> clazz, boolean verbose) throws JAXBException{
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);

            // Apply the filter
            XMLFilter filter = new NamespaceFilter("urn:iec62325.351:tc57wg16:451-6:generationloaddocument:3:0", true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xr = saxParser.getXMLReader();
            filter.setParent(xr);

            // Do the unmarshalling
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (verbose) {
                unmarshaller.setEventHandler(new ValidationEventHandler() {
                    public boolean handleEvent(ValidationEvent event) {
                        System.out.println("Event: " + event.getMessage());
                        System.out.println("Severity: " + event.getSeverity());
                        return true; // Continue processing.
                    }
                });
            }
            Source src = new SAXSource(filter, new InputSource(source));
            return (T) unmarshaller.unmarshal(src);
        } catch (ParserConfigurationException | SAXException ex) {
            throw new JAXBException(ex);
        }
    }

}
