/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.custom.JAXBDeserializerHelper;

/**
 *
 * @author Andres Bel Alonso
 */
public class RequestCache {

    private static final Logger LOGGER = LogManager.getLogger(RequestCache.class);

    public static final File DEFAULT_CACHE_FOLDER;

    static {
        String home = System.getProperty("user.home", "");
        File ai4czcFile = new File(home, ".ai4czc");
        File historicalData = new File(ai4czcFile, "entsoe_historical");
        File cache = new File(historicalData, "request_cache");
        DEFAULT_CACHE_FOLDER = cache;
    }

    private final File cacheFolder;

    public RequestCache(File cacheFolder) {
        this.cacheFolder = cacheFolder;
        if (cacheFolder.exists() && cacheFolder.isFile()) {
            throw new IllegalArgumentException("Provided cache directory is a file");
        }
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
    }

    /**
     * Uses $HOME/.ai4czc/entsoe_historical/cache as cache folder
     */
    public RequestCache() {
        this(DEFAULT_CACHE_FOLDER);
    }

    /**
     *
     * @param xmlRequest
     * @param completeURL the complete URL
     */
    public void writeRequest(String xmlRequest, String completeURL) {
        String cleanUrl = transformURLToValideFileName(completeURL, true);
        File targetFile = new File(cacheFolder, cleanUrl);
        try {
            FileUtils.writeStringToFile(targetFile, xmlRequest, Charset.defaultCharset());
        } catch (IOException ex) {
            LOGGER.warn("Unable to write xml request " + completeURL + " to " + targetFile.getAbsolutePath());
        }
    }

    /**
     * Caches a request that is a file
     *
     * @param tmpFile
     * @param completeURL
     */
    public void writeFileRequest(File tmpFile, String completeURL) {
        String cleanUrl = transformURLToValideFileName(completeURL, false);
        File targetFile = new File(cacheFolder, cleanUrl);
        try {
            Files.copy(tmpFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.warn("Unable to write zip file, request " + completeURL + " to " + targetFile.getAbsolutePath());
        }
    }

    /**
     *
     * @param <T>
     * @param completeURL
     * @param jaxbContext
     * @return The stored value or or null if it does not exist
     */
    public <T> T getCachedRequest(String completeURL, Class<T> projectClass) {
        String cleanUrl = transformURLToValideFileName(completeURL, true);
        File targetFile = new File(cacheFolder, cleanUrl);
        if (targetFile.exists()) {
            try {
                return (T) JAXBDeserializerHelper.doJAXBUnmarshall(new FileInputStream(targetFile), projectClass);
            } catch (JAXBException | FileNotFoundException ex) {
                LOGGER.warn("Problem deserializing cache file " + targetFile.getAbsolutePath() + " . Cache ignored");
                return null;
            }
        }
        // file not found, return null by contract
        return null;
    }

    /**
     *
     * @param completeURL
     * @return the stored value or empty optional if not present
     */
    public Optional<File> getCacheFileRequest(String completeURL) {
        String cleanUrl = transformURLToValideFileName(completeURL, false);
        File targetFile = new File(cacheFolder, cleanUrl);
        if(!targetFile.exists()) {
            return Optional.empty();
        }
        return Optional.of(targetFile);
    }

    private String transformURLToValideFileName(String completeURL, boolean xml) {
        return completeURL.replace("/", "_") + (xml ? ".xml" : ".zip");
    }

}
