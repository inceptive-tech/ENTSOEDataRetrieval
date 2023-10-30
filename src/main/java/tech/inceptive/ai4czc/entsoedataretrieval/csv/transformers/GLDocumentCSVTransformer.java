/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.DocumentType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ProcessType;
import static tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ProcessType.REALISED;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.PsrType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.PsrTypesByArea;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.GLMarketDocument;

/**
 * Class to generate entries relative to GLDocumentCSVTransformer
 *
 * @author Andres Bel Alonso
 */
public class GLDocumentCSVTransformer extends CSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(GLDocumentCSVTransformer.class);

    private final GLMarketDocument doc;

    public GLDocumentCSVTransformer(String csvSeparator, String csvEscapeChar, GLMarketDocument doc) {
        super(csvSeparator, csvEscapeChar);
        this.doc = doc;
    }

    @Override
    public boolean writeNextEntry(LocalDateTime timeStamp, BufferedWriter os) {
        // start inclusive, end exclusive
        if (doc.getTimePeriodTimeInterval().getLDTStart().isEqual(timeStamp)
                || (doc.getTimePeriodTimeInterval().getLDTStart().isBefore(timeStamp)
                && doc.getTimePeriodTimeInterval().getLDTEnd().isAfter(timeStamp))) {
            // we asume that the whole information was requested for a single continuos interval
            List<MarkedTS> timeSeries = getPeriodForTimeStamp(doc.getTimeSeries(),timeStamp);
            if (timeSeries.isEmpty()) {
                // probably a missing value, do not write nothing by convention
                return false;
            }
            DocumentType docType = DocumentType.fromId(doc.getType());
            ProcessType processType = ProcessType.fromId(doc.getProcessProcessType());
            String content;
            switch (docType) {
                case SYSTEM_TOTAL_LOAD:
                    switch (processType) {
                        case REALISED:
                        case DAY_AHEAD:
                            if (timeSeries.size() != 1) {
                                throw new DataRetrievalError("Expected one point at time stamp "
                                        + timeStamp.format(DateTimeFormatter.ISO_DATE_TIME) + " but got " + timeSeries.size());
//                                int[] vals = pt.stream().mapToInt(cp -> cp.getQuantity().intValue()).toArray();
                                // TODO : end the case where we check if values are the same or different
//                                LOGGER.warn("Warning! Expected one valid point for processType {} but got {}. "
//                                        + "Values are different so stoping", processType.getDescription(), pt.size());
                            }
                            content = this.getCsvSeparator() + Integer.toString(timeSeries.get(0).getSelectedPointValue());// only one point expected
                            break;
                        case WEEK_AHEAD:
                            if (timeSeries.size() != 2) {
                                throw new DataRetrievalError("Expected two point at time stamp "
                                        + timeStamp.format(DateTimeFormatter.ISO_DATE_TIME) + " but got " + timeSeries.size());
                            }
                            int val1 = timeSeries.get(0).getSelectedPointValue();
                            int val2 = timeSeries.get(1).getSelectedPointValue();
                            content = getCsvSeparator() + Integer.toString(Math.min(val1, val2)) + getCsvSeparator() + Integer.toString(Math.max(val1, val2));
                            break;
                        default:
                            throw new UnsupportedOperationException("Non suported processType " + processType.name());
                    }
                    break;
                case ACTUAL_GENERATION_PER_TYPE:
                    Area curArea = Area.fromID(doc.getTimeSeries().get(0).getInBiddingZoneDomainMRID().getValue());
                    if (timeSeries.size() > getColumnDefinition().size()) {
                        throw new DataRetrievalError("Expected at maximun " + getColumnDefinition().size() + " elements for "
                                + " but got " + timeSeries.size() + " for " + curArea.getPrettyName() + " are on generation per type");
                    }
                    // index the found time series
                    Map<PsrType, String> indexedTs = new HashMap<>();
                    timeSeries.stream().forEach(ts -> indexedTs.put(PsrType.fromID(ts.ts().getMktPSRType().getPsrType()),
                            Integer.toString(ts.getSelectedPointValue())));
                    List<PsrType> prodTypes = PsrTypesByArea.INSTANCE.getProductionTypesForArea(curArea);
                    StringBuilder sb = new StringBuilder();
                    for (PsrType curProdType : prodTypes) {
                        sb.append(getCsvSeparator());
                        sb.append(indexedTs.getOrDefault(curProdType, ""));
                    }
                    content = sb.toString();
                    break;
                default:
                    throw new UnsupportedOperationException("Non suported document type " + docType.name());
            }
            try {
                os.write(content);
                return true;
            } catch (IOException ex) {
                throw new DataRetrievalRuntimeException("Error writing timeStamp " + timeStamp.format(DateTimeFormatter.ISO_DATE), ex);
            }
        }
        // nothing to write, out of bounds
        return false;
    }

    @Override
    protected List<ColumnDefinition> computeColumnDef() {
        List<ColumnDefinition> res = new ArrayList<>();
        DocumentType docType = DocumentType.fromId(doc.getType());
        ProcessType processType = ProcessType.fromId(doc.getProcessProcessType());
        Area curArea;
        switch (docType) {
            case SYSTEM_TOTAL_LOAD:
                curArea = Area.fromID(doc.getTimeSeries().get(0).getOutBiddingZoneDomainMRID().getValue());
                switch (processType) {
                    // TODO : add the geographic zone to the col name
                    case REALISED:
                        res.add(new ColumnDefinition("load_realised_actual_" + curArea.getOptionCLIID()));
                        break;
                    case DAY_AHEAD:
                        res.add(new ColumnDefinition("load_day_ahead_forecast_" + curArea.getOptionCLIID()));
                        break;
                    case WEEK_AHEAD:
                        res.add(new ColumnDefinition("load_week_ahead_forecast_min_" + curArea.getOptionCLIID()));
                        res.add(new ColumnDefinition("load_week_ahead_forecast_max_" + curArea.getOptionCLIID()));
                        break;
                    default:
                        throw new UnsupportedOperationException("Non suported processType " + processType.name());
                }
                break;
            case ACTUAL_GENERATION_PER_TYPE:
                curArea = Area.fromID(doc.getTimeSeries().get(0).getInBiddingZoneDomainMRID().getValue());
                // list all the generation mediums available
                // Two options : 
                // 1 column per document, so we need to keep a track of the productions types per area
                // ALL columns in document, but there is a problem if a doc has not all the production types of a country...
                // ALL columns in document, keep track of existing columns so we can handle when there are missing values
                //
                //The last seems better
                String baseName = "_generation_";
                List<PsrType> prodTypes = PsrTypesByArea.INSTANCE.getProductionTypesForArea(curArea);
                for (PsrType curType : prodTypes) {
                    res.add(new ColumnDefinition(curArea.getOptionCLIID() + baseName + curType.getCsvName()));
                }
                break;
            default:
                throw new UnsupportedOperationException("Non suported docType " + docType.name());
        }
        return Collections.unmodifiableList(res);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.doc);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GLDocumentCSVTransformer other = (GLDocumentCSVTransformer) obj;
        return Objects.equals(this.doc, other.doc);
    }

}
