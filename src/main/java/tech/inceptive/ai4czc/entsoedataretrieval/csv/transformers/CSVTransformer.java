/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.ColumnDefinition;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.Point;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.TimeSeries;

/**
 * CSVTransformer. There may be many csv transformer for one column, and some periods can overlap. But these
 * overlappings are equal.
 *
 * @author Andres Bel Alonso
 */
public abstract class CSVTransformer {

    private static final Logger LOGGER = LogManager.getLogger(CSVTransformer.class);

    /**
     * A time serie with a selected point. Selected period : the period index selected on ts Selected point : the
     * selected index on ts.getPeriod.getPoint
     */
    public record MarkedTS(TimeSeries ts, int selectedPeriod, int selectedPoint) {

        /**
         * The value of the selected point of this time serie
         *
         * @return
         */
        public int getSelectedPointValue() {
            return ts.getPeriod().get(selectedPeriod).getPoint().get(selectedPoint).getQuantity().intValue();
        }
    }
    ;

    private final String csvSeparator;
    private final String csvEscapeChar;
    private List<ColumnDefinition> columnDefinition = null;

    public CSVTransformer(String csvSeparator, String csvEscapeChar) {
        this.csvSeparator = csvSeparator;
        this.csvEscapeChar = csvEscapeChar;
    }

    public List<ColumnDefinition> getColumnDefinition() {
        if (columnDefinition == null) {
            columnDefinition = computeColumnDef();
        }
        return columnDefinition;
    }

    /**
     * Writes the next line of the given time stamp.Starts with the separator.
     *
     * @param os
     * @return true if the entry was writed, false otherwise
     */
    public abstract boolean writeNextEntry(LocalDateTime timeStamp, BufferedWriter os);

    protected abstract List<ColumnDefinition> computeColumnDef();

    /**
     *
     * @param startingPoint
     * @param timeStamp
     * @return
     */
    protected static List<GLDocumentCSVTransformer.MarkedTS> getPeriodForTimeStamp(List<TimeSeries> timeSeries,
            LocalDateTime timeStamp) {
        // search for the correct ts
        // compute the corresponding positions
        // we assume that is there is many point of interest, they will be in different ts
        List<Integer> tsPositions = computeTSPosition(timeSeries,timeStamp);
        if (tsPositions.isEmpty()) {
            return new ArrayList<>();
        }
        // start of the time series
        List<GLDocumentCSVTransformer.MarkedTS> res = new ArrayList<>();
        for (int tsPosition : tsPositions) {
            for (int periodPosition = 0; periodPosition < timeSeries.size(); periodPosition++) {
                LocalDateTime startDT = timeSeries.get(tsPosition).getPeriod().get(0).getTimeInterval().getLDTStart();
                int resolutionInt = getResolutionInMinutes(timeSeries.get(tsPosition).getPeriod().get(0).getResolution());
                long gapMinutes = ChronoUnit.MINUTES.between(startDT, timeStamp);
                long selectPosition = (gapMinutes / resolutionInt);
                if (selectPosition > Integer.MAX_VALUE) {
                    throw new DataRetrievalError("The position to select is to big. Please use smaller queries");
                }
                List<Point> points = timeSeries.get(tsPosition).getPeriod().get(0).getPoint();
                if (selectPosition >= points.size()) {
                    // we handle the exceptional case of some week ahead time series that have a lenght of 1 week and one hour and are in this 
                    // exceptional case. We handle it
                    if (selectPosition == points.size()
                            && ("A60".equals(timeSeries.get(tsPosition).getBusinessType()) || "A61".equals(timeSeries.get(tsPosition).getBusinessType()))) {
                        res.add(new GLDocumentCSVTransformer.MarkedTS(timeSeries.get(tsPosition), periodPosition, (int) selectPosition - 1));
                        break;
                    }
                    String msg = "Error computing the selected position. Position computed : " + selectPosition
                            + " in TS of size " + points.size() + " .Requested time stamp is : "
                            + timeStamp.format(DateTimeFormatter.ISO_DATE_TIME) + " on document starting in "
                            + timeSeries.get(tsPosition).getPeriod().get(0).getTimeInterval().getLDTStart().format(DateTimeFormatter.ISO_DATE_TIME)
                            + " and ending at " + timeSeries.get(tsPosition).getPeriod().get(0).getTimeInterval().getLDTEnd().format(DateTimeFormatter.ISO_DATE_TIME);
                    LOGGER.warn(msg);
                    LOGGER.warn("Gap in minutes : {}, resolution in minutes : {}, ratio in double {}", gapMinutes, resolutionInt, gapMinutes / (double) resolutionInt);
                    // invalid time series, do nothing
                } else {
                    res.add(new GLDocumentCSVTransformer.MarkedTS(timeSeries.get(tsPosition), periodPosition, (int) selectPosition));
                    // supose there is only one pertienent period
                    break;
                }
            }
        }
        return res;
    }
    
    private static int getResolutionInMinutes(Duration duration) {
        if (duration.isSet(DatatypeConstants.MINUTES)) {
            return duration.getMinutes();
        } else if (duration.isSet(DatatypeConstants.DAYS)) {
            return duration.getDays() * 24 * 60;
        } else {
            throw new UnsupportedOperationException("Non suported duration type");
        }
    }
    
    private static List<Integer> computeTSPosition(List<TimeSeries> timeSeries, LocalDateTime timeStamp) {
        List<Integer> res = new ArrayList<>();
        for (int curPos = 0; curPos < timeSeries.size(); curPos++) {
            // TODO : handle periods
            LocalDateTime curStart = timeSeries.get(curPos).getPeriod().get(0).getTimeInterval().getLDTStart();
            LocalDateTime curEnd = timeSeries.get(curPos).getPeriod().get(0).getTimeInterval().getLDTEnd();
            if ((timeStamp.isAfter(curStart) || timeStamp.isEqual(curStart)) && timeStamp.isBefore(curEnd)) {
                res.add(curPos);
            }
        }
        return res;
    }

    public String getCsvSeparator() {
        return csvSeparator;
    }

    public String getCsvEscapeChar() {
        return csvEscapeChar;
    }
    
    /**
     * Generates missing values for each element of column definition.
     * @return The missing values for this 
     */
    public List<String> getMissingValues() {
        return IntStream.range(0, getColumnDefinition().size()).
                mapToObj(i -> "").
                toList();
    }
    
}
