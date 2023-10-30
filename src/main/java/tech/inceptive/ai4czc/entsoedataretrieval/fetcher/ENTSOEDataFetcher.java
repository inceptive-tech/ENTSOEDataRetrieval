/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ColumnType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ContractMarketAgreement;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.DocumentType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Params;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ProcessType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.PublicationMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.UnavailabilityMarketDocument;

/**
 * The fetcher using https://transparency.entsoe.eu/content/static_content/Static%20content/web%20api/Guide.html
 * allowing to retrieve data from all the suported API. This class only presents the API of what you can get, and return
 * the objects or nothing if there was a fetch problem.
 *
 * @author Andres Bel Alonso
 */
public class ENTSOEDataFetcher {

    private static final Logger LOGGER = LogManager.getLogger(ENTSOEDataFetcher.class);

    public static String BASE_URL = "https://web-api.tp.entsoe.eu/api";

    private String authToken;
    private HttpBridge bridge;
    private DisponibilityChecker checker;

    public ENTSOEDataFetcher(String authToken, boolean useRequestCache) {
        this.authToken = authToken;
        this.bridge = new HttpBridge(useRequestCache); // TODO : fix server URL
        this.checker = new DisponibilityChecker();
    }

    /**
     * One year limitation applies. Fetchs actual load (documentType : A65, ProcessType : A16)
     *
     * @return
     */
    public Optional<GLMarketDocument> fetchActualLoad(Area outBiddingZoneDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("6.1.A", periodStart, outBiddingZoneDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toMinutes() < 60) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 60 minutes but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.SYSTEM_TOTAL_LOAD.getId());
        params.put(Params.PROCESS_TYPE.getValue(), ProcessType.REALISED.getId());
        params.put(Params.OUT_BIDDINDZONE_DOMAIN.getValue(), outBiddingZoneDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        String msg = "Unable to correctly fetch actual load at " + outBiddingZoneDomain.getPrettyName() + " from "
                + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, msg, GLMarketDocument.class);
    }

    public Optional<GLMarketDocument> fetchDayAheadLoadForecast(Area outBiddingZoneDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("6.1.B", periodStart, outBiddingZoneDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toHours() < 24) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 24 hours but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.SYSTEM_TOTAL_LOAD.getId());
        params.put(Params.PROCESS_TYPE.getValue(), ProcessType.DAY_AHEAD.getId());
        params.put(Params.OUT_BIDDINDZONE_DOMAIN.getValue(), outBiddingZoneDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        GLMarketDocument doc = null;
        String msg = "Unable to correctly fetch day ahead load at " + outBiddingZoneDomain.getPrettyName() + " from "
                + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, msg, GLMarketDocument.class);
    }

    public Optional<GLMarketDocument> fetchWeekAheadLoadForecast(Area outBiddingZoneDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("6.1.C", periodStart, outBiddingZoneDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toDays() < 7) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 7 days but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.SYSTEM_TOTAL_LOAD.getId());
        params.put(Params.PROCESS_TYPE.getValue(), ProcessType.WEEK_AHEAD.getId());
        params.put(Params.OUT_BIDDINDZONE_DOMAIN.getValue(), outBiddingZoneDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));

        String msg = "Unable to correctly fetch week ahead load at " + outBiddingZoneDomain.getPrettyName() + " from "
                + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, msg, GLMarketDocument.class);
    }

    public Optional<GLMarketDocument> fetchAggregatedGenerationType(Area inDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("16.1.B&C", periodStart, inDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toDays() < 7) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 7 days but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.ACTUAL_GENERATION_PER_TYPE.getId());
        params.put(Params.PROCESS_TYPE.getValue(), ProcessType.REALISED.getId());
        params.put(Params.IN_DOMAIN.getValue(), inDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));

        String msg = "Unable to correctly fetch aggregated generation per type on " + inDomain.getPrettyName() + " from "
                + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, msg, GLMarketDocument.class);
    }

    public Optional<PublicationMarketDocument> fetchPhysicalFlows(Area inDomain, Area outDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart1 = checker.checkAvailability("12.1.G", periodStart, inDomain);
        LocalDateTime dataStart2 = checker.checkAvailability("12.1.G", periodStart, outDomain);
        LocalDateTime dataStart;
        if (dataStart1.equals(dataStart2)) {
            dataStart = dataStart1;
        } else if (dataStart1.isAfter(dataStart2)) {
            dataStart = dataStart1;
        } else {
            dataStart = dataStart2;
        }
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toHours() < 24) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 24 hours but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.AGGREGATED_ENERGY_DATA_REPORT.getId());
        params.put(Params.IN_DOMAIN.getValue(), inDomain.getId());
        params.put(Params.OUT_DOMAIN.getValue(), outDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        String erroMsg = "Unable to correctly retrieve physical flows from " + outDomain.getPrettyName() + " to "
                + inDomain.getPrettyName() + " from " + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, erroMsg, PublicationMarketDocument.class);
    }

    /**
     * Fetchs the transmission grid outages.
     *
     * @param inDomain
     * @param outDomain
     * @param periodStart
     * @param periodEnd
     * @return One document per outage (can be a lot)
     */
    public List<UnavailabilityMarketDocument> fetchTransmissionGridOutages(Area inDomain, Area outDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("10.1.A&B", periodStart, inDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return new ArrayList<>();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.TRANSMISSION_UNAVAILABILITY.getId());
        params.put(Params.IN_DOMAIN.getValue(), inDomain.getId());
        params.put(Params.OUT_DOMAIN.getValue(), outDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        try {
            return bridge.doZipGetOperation(params, BASE_URL, UnavailabilityMarketDocument.class);
        } catch (DataRetrievalRuntimeException ex) {
            LOGGER.catching(ex);
            LOGGER.warn("Unable to correctly retrieve outages between " + inDomain.getPrettyName() + " and "
                    + outDomain.getPrettyName() + " for time between " + usedStart.format(DateTimeFormatter.ISO_DATE_TIME)
                    + " and " + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME));
            return new ArrayList<>();
        }
    }

    public List<UnavailabilityMarketDocument> fetchGenerationUnitOutages(Area biddingZoneDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime dataStart = checker.checkAvailability("15.1.A&B", periodStart, biddingZoneDomain);
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dataStart)) {
            if (dataStart.isAfter(periodEnd)) {
                return new ArrayList<>();
            }
            usedStart = dataStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.GENERATION_UNAVAILABILITY.getId());
        params.put(Params.BIDDING_ZONE_DOMAIN.getValue(), biddingZoneDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        try {
            return bridge.doZipGetOperation(params, BASE_URL, UnavailabilityMarketDocument.class);
        } catch (DataRetrievalRuntimeException ex) {
            LOGGER.catching(ex);
            LOGGER.warn("Unable to correctly retrieve generation outages at " + biddingZoneDomain.getPrettyName()
                    + " for time between " + usedStart.format(DateTimeFormatter.ISO_DATE_TIME)
                    + " and " + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME));
            return new ArrayList<>();
        }
    }

    public Optional<PublicationMarketDocument> fetchWeekAheadCapacityForecast(Area inDomain, Area outDomain, LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        LocalDateTime date1 = checker.checkAvailability(ColumnType.WEEK_FORECAST_CAPACITY.getRelevantArticle(),
                periodStart, outDomain);
        LocalDateTime date2 = checker.checkAvailability(ColumnType.WEEK_FORECAST_CAPACITY.getRelevantArticle(),
                periodStart, inDomain);
        LocalDateTime dateStart = (date1.isAfter(date2)) ? date1 : date2;
        LocalDateTime usedStart = periodStart;
        if (!periodStart.equals(dateStart)) {
            if (dateStart.isAfter(periodEnd)) {
                return Optional.empty();
            }
            usedStart = dateStart;
        }
        Duration gap = Duration.between(usedStart, periodEnd);
        if (gap.toDays() > 365) {
            LOGGER.warn("Error, uncorrect duration. Expected request of less than 365 days, but was " + gap.toDays() + " days");
            return Optional.empty();
        }
        if (gap.toDays() < 7) {
            LOGGER.warn("Error, uncorrect duration. Expected at least 24 hours but it was " + gap.toMinutes() + " minutes");
            return Optional.empty();
        }
        Map<String, String> params = new HashMap<>();
        params.put(Params.SECURITY_TOKEN.getValue(), authToken);
        params.put(Params.DOCUMENT_TYPE.getValue(), DocumentType.ESTIMATED_NET_TRANSFER_CAPACITY.getId());
        params.put(Params.CONTRACT_MARKET_AGREEMENT_TYPE.getValue(), ContractMarketAgreement.WEEKLY.getCode());
        params.put(Params.IN_DOMAIN.getValue(), inDomain.getId());
        params.put(Params.OUT_DOMAIN.getValue(), outDomain.getId());
        params.put(Params.PERIOD_START.getValue(), usedStart.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        params.put(Params.PERIOD_END.getValue(), periodEnd.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        String erroMsg = "Unable to correctly week ahead transfert capacity forecast from " + outDomain.getPrettyName() + " to "
                + inDomain.getPrettyName() + " from " + usedStart.format(DateTimeFormatter.ISO_DATE_TIME) + " to "
                + periodEnd.format(DateTimeFormatter.ISO_DATE_TIME);
        return performGetOperation(params, erroMsg, PublicationMarketDocument.class);
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    private <T> Optional<T> performGetOperation(Map<String, String> params, String errorMsg, Class<T> reflectedClass) throws DataBindingException {
        T doc = null;
        try {
            doc = bridge.doGetOperation(params, BASE_URL, reflectedClass);
        } catch (DataRetrievalRuntimeException ex) {
            LOGGER.catching(ex);
            LOGGER.warn(errorMsg);
            return Optional.empty();
        }
        return Optional.ofNullable(doc);
    }

}
