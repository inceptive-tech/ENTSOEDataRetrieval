/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalRuntimeException;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.DocumentType;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.AssetRegisteredResource;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.Reason;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.ResourceIDString;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.TimeSeries;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.UnavailabilityMarketDocument;

/**
 *
 * @author Andres Bel Alonso
 */
public class UnavailabilityDocCSVTransformer extends CSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(UnavailabilityDocCSVTransformer.class);

    private static record ReadedInfo(boolean isInTS, String nominalPower) {

    }
    ;

    static final String TRANSMISSION_UNAVAILABILITY_BASE_NAME = "outage_grid_";
    static final String UNAVAILABILITY_SCHEDULED_BN = "scheduled_outage_";
    static final String GENERATION_UNAVAILABILITY_BASE_NAME = "outage_generation_";
    static final String NOMINAL_OUTAGE_POWER = "outage_gen_nominal_";

    private final UnavailabilityMarketDocument doc;
    private final DocumentType docType;

    public UnavailabilityDocCSVTransformer(UnavailabilityMarketDocument doc, String csvSeparator, String csvEscapeChar) {
        super(csvSeparator, csvEscapeChar);
        this.doc = doc;
        docType = DocumentType.fromId(doc.getType());
    }

    @Override
    public boolean writeNextEntry(LocalDateTime timeStamp, BufferedWriter os) {
        ReadedInfo readedInfo = checkIsInOutage(timeStamp);
        if (readedInfo.isInTS) {
            boolean isScheduled = checkIsScheduled();
            String content;
            if (isScheduled) {
                content = getCsvSeparator() + "1" + getCsvSeparator() + "1";
            } else {
                content = getCsvSeparator() + "1" + getCsvSeparator() + "0";
            }
            switch (docType) {
                case GENERATION_UNAVAILABILITY:
                    String gen = readedInfo.nominalPower;
                    if (gen == null) {
                        throw new DataRetrievalRuntimeException("Generation unavailability without a nominal power");
                    }
                    content += getCsvSeparator() + gen;
                    break;
                case TRANSMISSION_UNAVAILABILITY:
                    // nothing to do
                    break;
                default:
                    throw new UnsupportedOperationException("Not suported outage column name for type " + docType.getId()
                            + " ; " + docType.getDescription());
            }
            try {
                os.write(content);
            } catch (IOException ex) {
                throw new DataRetrievalRuntimeException("Error writing timeStamp " + timeStamp.format(DateTimeFormatter.ISO_DATE), ex);
            }
            return true;
        } else {
            return false;
        }
    }

    private ReadedInfo checkIsInOutage(LocalDateTime timeStamp) {
        for (TimeSeries curTs : doc.getTimeSeries()) {
            LocalDateTime startDT = LocalDateTime.of(curTs.getStartDateAndOrTimeDate().getYear(),
                    curTs.getStartDateAndOrTimeDate().getMonth(), curTs.getStartDateAndOrTimeDate().getDay(),
                    curTs.getStartDateAndOrTimeTime().getHour(), curTs.getStartDateAndOrTimeTime().getMinute());
            LocalDateTime endDT = LocalDateTime.of(curTs.getEndDateAndOrTimeDate().getYear(),
                    curTs.getEndDateAndOrTimeDate().getMonth(), curTs.getEndDateAndOrTimeDate().getDay(),
                    curTs.getEndDateAndOrTimeTime().getHour(), curTs.getEndDateAndOrTimeTime().getMinute());
            if ((startDT.isBefore(timeStamp) && endDT.isAfter(timeStamp)) || startDT.equals(timeStamp)) {
                String nominalPower = null;
                if (curTs.getProductionRegisteredResourcePSRTypePowerSystemResourcesNominalP() != null) {
                    nominalPower = Float.toString(curTs.getProductionRegisteredResourcePSRTypePowerSystemResourcesNominalP().getValue());
                }
                return new ReadedInfo(true, nominalPower);
            }
        }
        return new ReadedInfo(false, "");
    }

    private boolean checkIsScheduled() {
        List<Reason> reasonList = doc.getReason();
        if (reasonList.size() != 1) {
            throw new UnsupportedOperationException("Expected one reason but there are " + reasonList.size() + " reasons");
        }
        Reason reason = reasonList.get(0);
        switch (reason.getCode()) {
            case "B19":
                return true;
            case "B20": // shutdown
            case "B18": // failure on generation unit
            case "A95": // complentary info?
                return false;
            default:
                printCurDoc();
                throw new UnsupportedOperationException("Unknow reason code " + reason.getCode());

        }
    }

    private void printCurDoc() {
        try {
            // just print th pb doucment
            JAXBContext jaxbContext = JAXBContext.newInstance(UnavailabilityMarketDocument.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(doc, System.out);
        } catch (JAXBException ex) {
            LOGGER.catching(ex);
        }
    }

    @Override
    protected List<ColumnDefinition> computeColumnDef() {
        DocumentType docType = DocumentType.fromId(doc.getType());
        switch (docType) {
            case TRANSMISSION_UNAVAILABILITY:
                return doc.getTimeSeries().stream().
                        flatMap(ts -> {
                            if (ts.getAssetRegisteredResource().isEmpty()) {
                                AssetRegisteredResource res = new AssetRegisteredResource();
                                Area inDomain = Area.fromID(ts.getInDomainMRID().getValue());
                                Area outDomain = Area.fromID(ts.getOutDomainMRID().getValue());
                                ResourceIDString mrdid = new ResourceIDString();
                                mrdid.setValue("Unknown_asset_" + outDomain.getOptionCLIID() + "_" + inDomain.getOptionCLIID());
                                res.setMRID(mrdid);
                                return Stream.of(res);
                            }
                            return ts.getAssetRegisteredResource().stream();
                        }).
                        map(AssetRegisteredResource::getMRID).
                        map(ResourceIDString::getValue).
                        map(s -> Arrays.asList(new ColumnDefinition(TRANSMISSION_UNAVAILABILITY_BASE_NAME + s),
                        new ColumnDefinition(UNAVAILABILITY_SCHEDULED_BN + s))).
                        flatMap(List::stream).
                        toList();
            case GENERATION_UNAVAILABILITY:
                Area area = Area.fromID(doc.getTimeSeries().get(0).getBiddingZoneDomainMRID().getValue());
                return doc.getTimeSeries().stream().
                        map(ts -> ts.getProductionRegisteredResourceMRID()).
                        map(ResourceIDString::getValue).
                        map(s -> Arrays.asList(new ColumnDefinition(GENERATION_UNAVAILABILITY_BASE_NAME + s),
                        new ColumnDefinition(UNAVAILABILITY_SCHEDULED_BN + s),
                        new ColumnDefinition(area.getOptionCLIID() + "_" + NOMINAL_OUTAGE_POWER + s))).
                        flatMap(List::stream).
                        toList();
            default:
                throw new UnsupportedOperationException("Not suported outage column name for type " + docType.getId()
                        + " ; " + docType.getDescription());
        }

    }

    @Override
    public List<String> getMissingValues() {
        return IntStream.range(0, getColumnDefinition().size()).
                mapToObj(i -> "0").
                toList();
    }

}
