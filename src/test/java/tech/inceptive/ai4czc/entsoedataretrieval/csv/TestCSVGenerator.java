/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.*;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.CSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.GLDocumentCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.UnavailabilityDocCSVTransformer;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestCSVGenerator {

    private static final Logger LOGGER = LogManager.getLogger(TestCSVGenerator.class);

    @Test
    public void testWriteCSVFile() throws IOException {
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer1 = generateTransformer(Arrays.asList("field1", "field2"),
                Map.of("2023-03-01T00:00:00", ",A,3378", "2023-03-01T01:00:00", ",C,4598"));
        CSVTransformer transformer2 = generateTransformer(Arrays.asList("fieldK"),
                Map.of("2023-03-01T00:00:00", ",chaisse", "2023-03-01T01:00:00", ",hello"));
        List<CSVTransformer> transformers = Arrays.asList(transformer1, transformer2);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2023, Month.MARCH, 1, 0, 0),
                LocalDateTime.of(2023, Month.MARCH, 1, 2, 0), Duration.ofMinutes(60));

        //then
        String expected = "id,time_stamp,field1,field2,fieldK" + System.lineSeparator()
                + "0,2023-03-01T00:00:00,A,3378,chaisse" + System.lineSeparator()
                + "1,2023-03-01T01:00:00,C,4598,hello";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVFileCorrectlyEscapeHeader() throws IOException {
        // given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        List<CSVTransformer> transformers = Arrays.asList(generateTransformer(Arrays.asList("field1",
                "a very, very, bad field"),
                Map.of("2023-03-01T00:00:00", ",kk,987", "2023-03-01T01:00:00", ",ff,123")));
        // when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2023, Month.MARCH, 1, 0, 0),
                LocalDateTime.of(2023, Month.MARCH, 1, 2, 0), Duration.ofMinutes(60));

        // then
        String expected = "id,time_stamp,field1,\"a very, very, bad field\"" + System.lineSeparator()
                + "0,2023-03-01T00:00:00,kk,987" + System.lineSeparator()
                + "1,2023-03-01T01:00:00,ff,123";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVMultiDocumentSameVar() throws IOException {
        // we verify that if we have a split of many documents of the same variable (one for 2021, one for 2022) 
        // everything goes fine when writing the csv
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer1 = generateTransformer(Arrays.asList("field1", "field2"),
                Map.of("2021-01-01T00:00:00", ",A,3378"));
        CSVTransformer transformer2 = generateTransformer(Arrays.asList("field1", "field2"),
                Map.of("2022-01-01T00:00:00", ",B,4563"));
        List<CSVTransformer> transformers = Arrays.asList(transformer1, transformer2);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 1, 0, 0), Duration.ofDays(365));

        //then
        String expected = "id,time_stamp,field1,field2" + System.lineSeparator()
                + "0,2021-01-01T00:00:00,A,3378" + System.lineSeparator()
                + "1,2022-01-01T00:00:00,B,4563";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVMultiDocumentCrossedVar() throws IOException {
        // Two variables in two docs, crossed
        // Verify that the values are writed in order
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer1 = generateTransformer(Arrays.asList("field1", "field2"),
                Map.of("2021-01-01T00:00:00", ",A,3378"), CSVTransformer.class);
        CSVTransformer transformer2 = generateTransformer(Arrays.asList("field4", "field5", "field6"),
                Map.of("2021-01-01T00:00:00", ",koala,123,147"), GLDocumentCSVTransformer.class);
        CSVTransformer transformer3 = generateTransformer(Arrays.asList("field4", "field5", "field6"),
                Map.of("2022-01-01T00:00:00", ",castor,85,198"), GLDocumentCSVTransformer.class);
        CSVTransformer transformer4 = generateTransformer(Arrays.asList("field1", "field2"),
                Map.of("2022-01-01T00:00:00", ",B,4563"), CSVTransformer.class);
        List<CSVTransformer> transformers = Arrays.asList(transformer1, transformer2, transformer3, transformer4);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2023, 1, 1, 0, 0), Duration.ofDays(365));

        //then
        String expected = "id,time_stamp,field1,field2,field4,field5,field6" + System.lineSeparator()
                + "0,2021-01-01T00:00:00,A,3378,koala,123,147" + System.lineSeparator()
                + "1,2022-01-01T00:00:00,B,4563,castor,85,198";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVMultiDocumentCrossedOverlapDT() throws IOException {
        // One variable, two docs, the docs have an entry for the same time stamp (same value, both are correct)
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer1 = generateTransformer(Arrays.asList("field1"),
                Map.of("2021-01-01T00:00:00", ",A",
                        "2022-01-01T00:00:00", ",B",
                        "2023-01-01T00:00:00", ",C"), CSVTransformer.class);
        CSVTransformer transformer2 = generateTransformer(Arrays.asList("field1"),
                Map.of("2023-01-01T00:00:00", ",C",
                        "2024-01-01T00:00:00", ",E"), CSVTransformer.class);
        List<CSVTransformer> transformers = Arrays.asList(transformer1, transformer2);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 1, 0), Duration.ofDays(365));

        //then
        String expected = "id,time_stamp,field1" + System.lineSeparator()
                + "0,2021-01-01T00:00:00,A" + System.lineSeparator()
                + "1,2022-01-01T00:00:00,B" + System.lineSeparator()
                + "2,2023-01-01T00:00:00,C" + System.lineSeparator()
                + "3,2024-01-01T00:00:00,E";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVFileMissingValue() throws IOException {
        // One column contains missing values
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer1 = generateTransformer(Arrays.asList("field1"),
                Map.of("2021-01-01T00:00:00", ",A",
                        "2022-01-01T00:00:00", ",B",
                        "2023-01-01T00:00:00", ",C"), CSVTransformer.class);
        CSVTransformer transformer2 = generateTransformer(Arrays.asList("field2"),
                Map.of("2022-01-01T00:00:00", ",895",
                        "2023-01-01T00:00:00", ",741"), CSVTransformer.class);
        List<CSVTransformer> transformers = Arrays.asList(transformer1, transformer2);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 1, 0), Duration.ofDays(365));

        //then
        String expected = "id,time_stamp,field1,field2" + System.lineSeparator()
                + "0,2021-01-01T00:00:00,A," + System.lineSeparator()
                + "1,2022-01-01T00:00:00,B,895" + System.lineSeparator()
                + "2,2023-01-01T00:00:00,C,741" + System.lineSeparator()
                + "3,2024-01-01T00:00:00,,";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
//        System.out.println(expected);
//        System.out.println(found);
        assertEquals(expected, found);
    }

    @Test
    public void testWriteCSVFileMissingValueDifferent() throws IOException {
        // Some columns can have specific missing values other than "". Unavailability csv transformer has a default 
        // missing value of "0"
        //given
        CSVGenerator csvGen = new CSVGenerator(",", "\"");
        File targetFile = Files.createTempFile("test_", ".csv").toFile();
        CSVTransformer transformer = generateTransformer(Arrays.asList("col1", "col2"), Map.of(), UnavailabilityDocCSVTransformer.class);
        List<CSVTransformer> transformers = Arrays.asList(transformer);

        //when
        csvGen.writeCSVFile(transformers, targetFile, LocalDateTime.of(2021, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 1, 0), Duration.ofDays(365));

        //then
        String expected = "id,time_stamp,col1,col2" + System.lineSeparator()
                + "0,2021-01-01T00:00:00,0,0" + System.lineSeparator()
                + "1,2022-01-01T00:00:00,0,0" + System.lineSeparator()
                + "2,2023-01-01T00:00:00,0,0" + System.lineSeparator()
                + "3,2024-01-01T00:00:00,0,0";
        String found = FileUtils.readFileToString(targetFile, Charset.defaultCharset());
        assertEquals(expected, found);
    }

    /**
     *
     * @param header
     * @param values : key date time in ISO_DATETIME format and values the content to write (exact)
     */
    private CSVTransformer generateTransformer(List<String> header, Map<String, String> values) {
        return generateTransformer(header, values, CSVTransformer.class);
    }

    private CSVTransformer generateTransformer(List<String> header, Map<String, String> values, Class<? extends CSVTransformer> baseClass) {
        CSVTransformer res = mock(baseClass);
        when(res.getColumnDefinition()).thenReturn(header.stream().map(s -> new ColumnDefinition(s)).toList());
        when(res.getMissingValues()).thenCallRealMethod();
        values.entrySet().stream().forEach(e -> {
            doAnswer(a -> {
                BufferedWriter os = a.getArgument(1);
                os.write(e.getValue());
                return true;
            }).when(res).writeNextEntry(
                    eq(LocalDateTime.parse(e.getKey(), DateTimeFormatter.ISO_DATE_TIME)),
                    any(BufferedWriter.class));
        });
        return res;
    }
}
