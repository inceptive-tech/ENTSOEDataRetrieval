/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.TestGLDocumentCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;

/**
 *
 * @author Andres Bel Alonso
 */
public class TestRequestCache {

    private static final Logger LOGGER = LogManager.getLogger(TestRequestCache.class);

    @Test
    public void testWriteRequest() throws IOException {
        // test a file is writed in target directory and name is correct
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        String xmlContent = "some_empty_xml_not_format";
        String url = "url/to/something";

        //when
        cache.writeRequest(xmlContent, url);

        //then
        File targetFile = new File(cacheLocation, url.replace("/", "_") + ".xml");
        assertEquals(true, targetFile.exists());
        assertEquals(xmlContent, FileUtils.readFileToString(targetFile, Charset.defaultCharset()));
    }

    @Test
    public void testGetCachedRequest() throws IOException, JAXBException {
        // test if we can find and instanciate a real doc
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        //create the cached file.
        File cachedDic = createCacheContext(cacheLocation);

        //when
        GLMarketDocument doc = cache.getCachedRequest(cachedDic.getName().replace(".xml", ""), GLMarketDocument.class);

        //then
        assertEquals(true, doc != null);
        // a random point to ensure there is something in the doc
        assertEquals(8456, doc.getTimeSeries().get(0).getPeriod().get(0).getPoint().get(1).getQuantity().intValue());
    }

    @Test
    public void testGetCachedRequestCacheMiss() throws IOException, JAXBException {
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        //create the cached file.
        File cachedDic = createCacheContext(cacheLocation);

        //when
        GLMarketDocument doc = cache.getCachedRequest("file2", GLMarketDocument.class);

        //then
        assertEquals(true, doc == null);
    }

    @Test
    public void testWriteFileRequest() throws IOException {
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        String url = "url/to/something";
        String data = "here some data";
        File theFile = Files.createTempFile("temp1", "").toFile();
        FileUtils.writeStringToFile(theFile, data, Charset.defaultCharset());

        //when
        cache.writeFileRequest(theFile, url);

        //then
        File targetFile = new File(cacheLocation, url.replace("/", "_") + ".zip");
        assertEquals(true, targetFile.exists());
        assertEquals(data, FileUtils.readFileToString(targetFile, Charset.defaultCharset()));

    }

    @Test
    public void testGetCacheFileRequest() throws IOException {
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        String data = "some_empty_xml_not_format";
        String url = "url/to/something";
        File theFile = new File(cacheLocation, url.replace("/", "_") + ".zip");
        FileUtils.writeStringToFile(theFile, data, Charset.defaultCharset());

        //when
        Optional<File> res = cache.getCacheFileRequest(url);
        
        //then
        assertEquals(true, res.isPresent());
        assertEquals(theFile, res.get());
        assertEquals(data, FileUtils.readFileToString(res.get(), Charset.defaultCharset()));
    }

    @Test
    public void testGetCacheFileRequestMissingValue() throws IOException {
        //given
        File cacheLocation = Files.createTempDirectory("cache_").toFile();
        RequestCache cache = new RequestCache(cacheLocation);
        String url = "url/to/something";
        
        //when
        Optional<File> res = cache.getCacheFileRequest(url);
        
        //then
        assertEquals(true, res.isEmpty());
    }

    private File createCacheContext(File cacheLocationDir) throws IOException {
        File destFile = new File(cacheLocationDir, "file1.xml");
        FileUtils.copyInputStreamToFile(TestRequestCache.class.getResource("ActualTotalLocalExample.xml").openStream(), destFile);
        return destFile;
    }

}
