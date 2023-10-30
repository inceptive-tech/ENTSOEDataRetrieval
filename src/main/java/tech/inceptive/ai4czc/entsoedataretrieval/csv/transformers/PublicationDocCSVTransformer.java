/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.DocumentType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.PublicationMarketDocument;

/**
 *
 * @author Andres Bel Alonso
 */
public class PublicationDocCSVTransformer extends CSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(PublicationDocCSVTransformer.class);

    private final PublicationMarketDocument doc;

    public PublicationDocCSVTransformer(PublicationMarketDocument doc, String csvSeparator, String csvEscapeChar) {
        super(csvSeparator, csvEscapeChar);
        this.doc = doc;
    }

    @Override
    public boolean writeNextEntry(LocalDateTime timeStamp, BufferedWriter os) {
        LocalDateTime docStart = doc.getPeriodTimeInterval().getLDTStart();
        LocalDateTime docEnd = doc.getPeriodTimeInterval().getLDTEnd();
        if (timeStamp.isBefore(docStart) || timeStamp.isAfter(docEnd) || docEnd.equals(timeStamp)) {
            // out of time bounds of doc, do nothing
            return false;
        }
        List<MarkedTS> timeSeries = getPeriodForTimeStamp(doc.getTimeSeries(), timeStamp);
        if (timeSeries.size() > 1) {
            Area inDomain = Area.fromID(doc.getTimeSeries().get(0).getInDomainMRID().getValue());
            Area outDomain = Area.fromID(doc.getTimeSeries().get(0).getOutDomainMRID().getValue());
            throw new DataRetrievalError("Expected one point at time stamp "
                    + timeStamp.format(DateTimeFormatter.ISO_DATE_TIME) + " but got " + timeSeries.size() + " on "
                    + "phys flows from " + inDomain.getPrettyName() + " to " + outDomain.getPrettyName());
        } else if (timeSeries.size() == 0) {
            // seems to be a missing value
            return false;
        }
        String content = getCsvSeparator() + timeSeries.get(0).getSelectedPointValue();
        try {
            os.write(content);
        } catch (IOException ex) {
            throw new DataRetrievalRuntimeException("Error writing timeStamp " + timeStamp.format(DateTimeFormatter.ISO_DATE), ex);
        }
        return true;
    }

    @Override
    protected List<ColumnDefinition> computeColumnDef() {
        Area inDomain = Area.fromID(doc.getTimeSeries().get(0).getInDomainMRID().getValue());
        Area outDomain = Area.fromID(doc.getTimeSeries().get(0).getOutDomainMRID().getValue());
        DocumentType docType = DocumentType.fromId(doc.getType());
        switch (docType) {
            case ESTIMATED_NET_TRANSFER_CAPACITY:
                return Arrays.asList(new ColumnDefinition("transfer_capacity_" + outDomain.getOptionCLIID() + 
                        "_to_" + inDomain.getOptionCLIID()));
            case AGGREGATED_ENERGY_DATA_REPORT:
                return Arrays.asList(new ColumnDefinition("physical_flows_" + inDomain.getOptionCLIID() + "_to_"
                        + outDomain.getOptionCLIID()));
            default:
                throw new UnsupportedOperationException("Unsupported documentType " + docType.getDescription());
        }

    }

}
