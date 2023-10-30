/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.AcknowledgementMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.custom.JAXBDeserializerHelper;

/**
 * This class builds the request with the provided parameters, performs it and returns the expected result, or throws
 * exceptions when things do not behave as expected. Errors should be logged, but simple
 * {@link DataRetrievalRuntimeException} should be throw when it is impossible to retrieve data.
 *
 * @author Andres Bel Alonso
 */
public class HttpBridge {

    private static final Logger LOGGER = LogManager.getLogger(HttpBridge.class);
    // TODO : make request configurable 
    private final static LeakyBucket throttler = new LeakyBucket(1); // 2 request per second, 120 per minute approx is under the 400 of ENTSOE platform
    private static RequestCache REQUEST_CACHE = new RequestCache();

    public static void setRequestCache(RequestCache cache) {
        REQUEST_CACHE = cache;
    }
    
    private final boolean useCache;

    public HttpBridge(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Send request via GET method.
     *
     * @param <T>
     * @param params - map including all URL params
     * @param urlName - base URL
     * @param jaxbContext
     * @throws DataRetrievalRuntimeException When there is an error parsing or making the http request.
     * @return - plain XML content
     */
    public <T> T doGetOperation(Map<String, String> params, String urlName, Class<T> projectClass) throws DataRetrievalRuntimeException {
        // prepare URL with requesting params
        String finalURL = addUrlParams(params, urlName);
        LOGGER.debug("Proceding to request data to " + finalURL);
        try {
            // prepare connection
            URL url = new URL(finalURL);
            if (useCache) {
                T cachedVal = REQUEST_CACHE.getCachedRequest(finalURL, projectClass);
                if (cachedVal != null) {
                    LOGGER.debug("Retrieving data cache for request at {}", finalURL);
                    return cachedVal;
                }
            }
            try {
                //limit calls
                throttler.consume();
            } catch (InterruptedException e) {
                throw new DataRetrievalRuntimeException(e);
            }

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            // success
            if (responseCode == HttpURLConnection.HTTP_OK) {
                LOGGER.debug("Data correctly retrieved from ENTSOE platform");
                Scanner scanner = new Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A");
                String xmlContent = scanner.next();
//                LOGGER.debug(xmlContent);
                try {
                    T res = JAXBDeserializerHelper.doJAXBUnmarshall(new ByteArrayInputStream(xmlContent.getBytes()),
                            projectClass);
                    REQUEST_CACHE.writeRequest(xmlContent, finalURL);
                    return res;
                } catch (JAXBException ex) {
                    LOGGER.error("Problematic XML : ");
                    LOGGER.error(xmlContent);
                    throw new DataRetrievalRuntimeException(ex);
                }
            } else {
                String errorContent = parseToPlainString(con.getErrorStream());
                LOGGER.error(errorContent);
                throw new DataRetrievalRuntimeException("Unable to fetch data. Error code " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Unexpected error. Message %s", e.getMessage()));
        }
        return null;
    }

    /**
     * Sends a request via a get method and retrieves a zip file with many documents. It unzips it and reads the docs
     *
     * @param <T>
     * @param params
     * @param urlName
     * @param projectClass
     * @return
     * @throws DataRetrievalRuntimeException
     */
    public <T> List<T> doZipGetOperation(Map<String, String> params, String urlName, Class<T> projectClass) throws DataRetrievalRuntimeException {
        String finalURL = addUrlParams(params, urlName);
        LOGGER.debug("Proceding to request data to " + finalURL);
        try {
            URL url = new URL(finalURL);
            if (useCache) {
                Optional<File> optFile = REQUEST_CACHE.getCacheFileRequest(finalURL);
                if (optFile.isPresent()) {
                    try {
                        List<File> files = unzipFile(optFile.get().toPath(), Files.createTempDirectory("unzippedContent"));
                        return files.stream().map(f -> {
                            try {
                                return unmarshallFile(f, projectClass);
                            } catch (IOException ex) {
                                throw new DataRetrievalRuntimeException(ex);
                            }
                        }).toList();
                    } catch (DataRetrievalRuntimeException ex) {
                        LOGGER.info("Unable to rebuild using cache file " + optFile.get().getAbsolutePath() + " Proceding "
                                + "to request data");
                    }
                } else {
                    // search for the empty document
                    AcknowledgementMarketDocument doc = REQUEST_CACHE.getCachedRequest(finalURL, AcknowledgementMarketDocument.class);
                    if (doc != null && doc.getReason() != null && Integer.parseInt(doc.getReason().getCode()) == 999) {
                        // The aknowledgement of no data, return an empty list
                        return new ArrayList<>();
                    }
                }
            }

            try {
                //limit calls
                throttler.consume();
            } catch (InterruptedException e) {
                throw new DataRetrievalRuntimeException(e);
            }

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // check we have a zip file 
                String contentType = con.getContentType();
                if (!"application/zip".equals(contentType)) {
                    if ("application/xml".equals(contentType)) {
                        Scanner scanner = new Scanner(con.getInputStream(), "UTF-8").useDelimiter("\\A");
                        String xmlContent = scanner.next();
                        try {
                            AcknowledgementMarketDocument doc = JAXBDeserializerHelper.doJAXBUnmarshall(new ByteArrayInputStream(xmlContent.getBytes()), AcknowledgementMarketDocument.class);
                            if (doc != null && doc.getReason() != null && Integer.parseInt(doc.getReason().getCode()) == 999) {
                                REQUEST_CACHE.writeRequest(xmlContent, finalURL);
                                return new ArrayList<>();
                            }
                        } catch (JAXBException ex) {
                            LOGGER.warn("Unable to deserialize doc as AcknowledgementMarketDocument");
                            LOGGER.warn(xmlContent);
                            throw new DataRetrievalRuntimeException(ex);
                        }
                    }
                    throw new DataRetrievalRuntimeException("The content type of the 200 response is not a zip file: " + contentType);
                }
                LOGGER.debug("Data correctly retrieved from ENTSOE platform");
                // Copy the content on ta temporal file
                Path tempFile = Files.createTempFile("tempZipFile", ".zip");
                Files.copy(con.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
                REQUEST_CACHE.writeFileRequest(tempFile.toFile(), finalURL);
                List<File> files = unzipFile(tempFile, Files.createTempDirectory("unzippedContent"));
                return files.stream().map(f -> {
                    try {
                        return unmarshallFile(f, projectClass);
                    } catch (IOException ex) {
                        throw new DataRetrievalRuntimeException(ex);
                    }
                }).toList();
            } else {
                String errorContent = parseToPlainString(con.getErrorStream());
                LOGGER.error(errorContent);
                throw new DataRetrievalRuntimeException("Unable to fetch data. Error code " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Unexpected error. Message %s", e.getMessage()));
        }
        return null;
    }

    private String addUrlParams(Map<String, String> params, String baseURL) {
        if (!baseURL.endsWith("?")) {
            baseURL += '?';
        }
        StringBuilder result = new StringBuilder(baseURL);
        Iterator<String> keySet = params.keySet().iterator();
        while (keySet.hasNext()) {
            String key = keySet.next();

            result.append(key).append("=").append(params.get(key));
            if (keySet.hasNext()) {
                result.append("&");
            }
        }
        return result.toString();
    }

    private String parseToPlainString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }
        in.close();
        return out.toString();
    }

    private List<File> unzipFile(Path source, Path target) throws IOException {
        List<File> uncompressedFiles = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
            ZipEntry zipEntry = zis.getNextEntry();
            byte[] buffer = new byte[16384];
            while (zipEntry != null) {
                File newFile = newFile(target.toFile(), zipEntry);
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                zipEntry = zis.getNextEntry();
                uncompressedFiles.add(newFile);
            }
            zis.closeEntry();
        }
        return uncompressedFiles;
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private <T> T unmarshallFile(File file, Class<T> projectedClass) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return JAXBDeserializerHelper.doJAXBUnmarshall(is, projectedClass);
        } catch (JAXBException ex) {
            String xmlContent = FileUtils.readFileToString(file, Charset.defaultCharset());
            LOGGER.error("Problematic XML : ");
            LOGGER.error(xmlContent);
            throw new DataRetrievalRuntimeException(ex);
        }
    }
}
