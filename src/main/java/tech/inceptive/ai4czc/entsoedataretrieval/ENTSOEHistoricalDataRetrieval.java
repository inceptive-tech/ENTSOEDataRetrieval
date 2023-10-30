/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package tech.inceptive.ai4czc.entsoedataretrieval;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.CSVGenerator;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.CSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.GLDocumentCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.PublicationDocCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.UnavailabilityDocCSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.ENTSOEDataFetcher;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ColumnType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.PublicationMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.UnavailabilityMarketDocument;
import tech.inceptive.ai4czc.entsoedataretrieval.tools.DateTimeDivider;

/**
 * The class to fetch an dataset from entsoe.
 *
 * @author andres
 */
public class ENTSOEHistoricalDataRetrieval {

    private final Set<ColumnType> columnType;
    private ENTSOEDataFetcher fetcher; // not final for testing purpose
    private CSVGenerator csvGen; // not final for testing purpose
    private final String csvEscapeChar;
    private final String csvSeparator;
    // TODO : make the time granularity configurable?

    public ENTSOEHistoricalDataRetrieval(String authToken,
            Set<ColumnType> columnType, String csvEscapeChar, String csvSeparator, boolean useRequestCache) {
        this.columnType = columnType;
        this.fetcher = new ENTSOEDataFetcher(authToken, useRequestCache);
        this.csvEscapeChar = csvEscapeChar;
        this.csvSeparator = csvSeparator;
        this.csvGen = new CSVGenerator(csvSeparator, csvEscapeChar);
    }

    /**
     * Retrieves to a temporal file the dataset, on CSV format.
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public File fetchDataset(LocalDateTime startDate, LocalDateTime endDate, Duration timeStep, Set<Area> targetAreas,
            Area mainFlowsArea, Duration maxTimeDuration) {
        List<CSVTransformer> transformers = new ArrayList<>(columnType.size());
        List<LocalDateTime> splits;
        if (Duration.ofDays(365).minus(maxTimeDuration).isNegative()) {
            throw new DataRetrievalError("Maximal duration can not be bigger than 365 days");
        }
        if (columnType.contains(ColumnType.ACTUAL_LOAD)) {
            // split request depending on stard and end date
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(60, ChronoUnit.MINUTES));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<GLMarketDocument> opt = fetcher.fetchActualLoad(targetArea, splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new GLDocumentCSVTransformer(csvSeparator, csvEscapeChar, opt.get()));
                    }
                }
            }
        }
        if (columnType.contains(ColumnType.DAY_AHEAD_LOAD)) {
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(24, ChronoUnit.HOURS));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<GLMarketDocument> opt = fetcher.fetchDayAheadLoadForecast(targetArea, splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new GLDocumentCSVTransformer(csvSeparator, csvEscapeChar, opt.get()));
                    }
                }
            }
        }
        if (columnType.contains(ColumnType.WEAK_AHEAD_LOAD)) {
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(7, ChronoUnit.DAYS));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<GLMarketDocument> opt = fetcher.fetchWeekAheadLoadForecast(targetArea, splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new GLDocumentCSVTransformer(csvSeparator, csvEscapeChar, opt.get()));
                    }
                }
            }
        }
        if (columnType.contains(ColumnType.AGGREGATED_GENERATION_TYPE)) {
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(60, ChronoUnit.MINUTES));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<GLMarketDocument> opt = fetcher.fetchAggregatedGenerationType(targetArea, splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new GLDocumentCSVTransformer(csvSeparator, csvEscapeChar, opt.get()));
                    }
                }
            }
        }
        if (columnType.contains(ColumnType.PHYSICAL_FLOW)) {
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(60, ChronoUnit.MINUTES));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<PublicationMarketDocument> opt = fetcher.fetchPhysicalFlows(targetArea, mainFlowsArea,
                            splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new PublicationDocCSVTransformer(opt.get(), csvSeparator, csvEscapeChar));
                    }
                    opt = fetcher.fetchPhysicalFlows(mainFlowsArea, targetArea,
                            splits.get(i), splits.get(i + 1));
                    if (opt.isPresent()) {
                        transformers.add(new PublicationDocCSVTransformer(opt.get(), csvSeparator, csvEscapeChar));
                    }
                }
            }
        }
        if (columnType.contains(ColumnType.TRANSMISSION_OUTAGE)) {
            if (mainFlowsArea == null) {
                throw new DataRetrievalError("Please fix the main flows are when retriving data from transmission "
                        + "outages (article 10.1.A&B)");
            }
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(60, ChronoUnit.MINUTES));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    // TODO : check if we need to do both sides
                    List<UnavailabilityMarketDocument> outages = fetcher.fetchTransmissionGridOutages(mainFlowsArea, targetArea,
                            splits.get(i), splits.get(i + 1));
                    transformers.addAll(outages.stream().
                            map(doc -> (CSVTransformer) new UnavailabilityDocCSVTransformer(doc, csvSeparator, csvEscapeChar)).
                            toList());
                }
            }
        }
        if (columnType.contains(ColumnType.GENERATION_OUTAGE)) {
            // work week by week as some countries have a lot
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(1, ChronoUnit.DAYS));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    List<UnavailabilityMarketDocument> outages = fetcher.fetchGenerationUnitOutages(targetArea,
                            splits.get(i), splits.get(i + 1));
                    transformers.addAll(outages.stream().
                            map(doc -> (CSVTransformer) new UnavailabilityDocCSVTransformer(doc, csvSeparator, csvEscapeChar)).
                            toList());
                }
            }
        }
        if (columnType.contains(ColumnType.WEEK_FORECAST_CAPACITY)) {
            if (mainFlowsArea == null) {
                throw new DataRetrievalError("Please fix the main flows are when retriving transmission forecast capacity "
                        + "data (article 11.1.A)");
            }
            splits = DateTimeDivider.splitInMilestones(startDate, endDate, maxTimeDuration,
                    Duration.of(7, ChronoUnit.DAYS));
            for (int i = 0; i < splits.size() - 1; i++) {
                for (Area targetArea : targetAreas) {
                    Optional<PublicationMarketDocument> optDoc = fetcher.fetchWeekAheadCapacityForecast(mainFlowsArea, targetArea,
                            splits.get(i), splits.get(i + 1));
                    if (optDoc.isPresent()) {
                        transformers.add(new PublicationDocCSVTransformer(optDoc.get(), csvSeparator, csvEscapeChar));
                    }
                    optDoc = fetcher.fetchWeekAheadCapacityForecast(targetArea, mainFlowsArea,
                            splits.get(i), splits.get(i + 1));
                    if (optDoc.isPresent()) {
                        transformers.add(new PublicationDocCSVTransformer(optDoc.get(), csvSeparator, csvEscapeChar));
                    }
                }
            }
        }
        try {
            File tmpFile = File.createTempFile("tmp_", ".csv");
            csvGen.writeCSVFile(transformers, tmpFile, startDate, endDate, timeStep);
            return tmpFile;
        } catch (IOException ex) {
            throw new DataRetrievalError(ex);
        }
    }

}
