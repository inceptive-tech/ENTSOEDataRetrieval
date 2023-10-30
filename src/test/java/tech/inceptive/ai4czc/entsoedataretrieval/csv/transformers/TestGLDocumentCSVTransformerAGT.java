/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
import static org.mockito.Mockito.*;

/**
 * Parametric test for GLDocumentCSVTransformer on Generation per production type
 *
 * @author Andres Bel Alonso
 */
@RunWith(Parameterized.class)
public class TestGLDocumentCSVTransformerAGT {

    private static final Logger LOGGER = LogManager.getLogger(TestGLDocumentCSVTransformerAGT.class);

    public static record TestParameters(String xmlFilename, String xmlFilenameMissing, int nbCols, String writedContent,
            String writedContentMissing) {

    }

    ;
    
    @Parameterized.Parameters(name = "factory={0}")
    public static Iterable<TestParameters> data() {
        List<TestParameters> res = new ArrayList<>();
        res.add(new TestParameters("prodagg/MontenegroGenerationPerProdType.xml",
                "prodagg/MontenegroGenerationPerProdType_missing.xml",
                4,
                ",221,35,359,112",
        ",220,,410,57"));
        res.add(new TestParameters("prodagg/KosovoGenerationPerProdType.xml", 
                "prodagg/KosovoGenerationPerProdType_missing.xml", 4, ",728,6,6,126", 
                ",737,10,,110"));
        res.add(new TestParameters("prodagg/BosniaGenerationPerProdType.xml", 
                "prodagg/BosniaGenerationPerProdType_missing.xml", 7, ",993,116,603,0,,,39", ",830,,729,0,,,18"));
        // TODO : add italy
        return res;
    }

    private TestParameters curParams;

    public TestGLDocumentCSVTransformerAGT(TestParameters curParams) {
        this.curParams = curParams;
    }

    @Test
    public void testComputeColumnDefAG() {
        // Verify that the column definition has the correct size
        //given
        GLMarketDocument doc = getGLMarketDocument(curParams.xmlFilename);
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);

        //when
        Set<ColumnDefinition> colDefs = new HashSet<>(transformer.getColumnDefinition());

        //then
        assertEquals(curParams.nbCols, colDefs.size());
    }

    @Test
    public void testWriteNextEntryAG() throws IOException {
        // verify that write next entry when all the possible entries are present is correct
        //given
        GLMarketDocument doc = getGLMarketDocument(curParams.xmlFilename);
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ts = LocalDateTime.of(2023, 1, 17, 6, 0);
        BufferedWriter bf = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ts, bf);

        //then
        assertEquals(true, out);
        verify(bf, times(1)).write(eq(curParams.writedContent));

    }

    @Test
    public void testWriteNextEntryMissingValue() throws IOException {
        // verify that write next entry when there are missing values is correct
        // given
        GLMarketDocument doc = getGLMarketDocument(curParams.xmlFilenameMissing);
        GLDocumentCSVTransformer transformer = new GLDocumentCSVTransformer(",", "\"", doc);
        LocalDateTime ts = LocalDateTime.of(2023, 1, 17, 14, 0);
        BufferedWriter bf = mock(BufferedWriter.class);

        //when
        boolean out = transformer.writeNextEntry(ts, bf);

        //then
        assertEquals(true, out);
        verify(bf, times(1)).write(eq(curParams.writedContentMissing));

    }

    private GLMarketDocument getGLMarketDocument(String resourceName) {
        try {
            JAXBContext context = JAXBContext.newInstance(GLMarketDocument.class);
            return (GLMarketDocument) context.createUnmarshaller().unmarshal(TestGLDocumentCSVTransformerAGT.class.getResource(resourceName).openStream());
        } catch (JAXBException | IOException ex) {
            throw new DataRetrievalError(ex);
        }
    }
}
