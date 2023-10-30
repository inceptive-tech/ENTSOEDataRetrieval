/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
 
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs;
 
 
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
/**
 * The column types shorted by relevant article on the EU legislation.
 * @author Andres Bel Alonso
 */
public enum ColumnType {
    ACTUAL_LOAD("6.1.A"),
    DAY_AHEAD_LOAD("6.1.B"),
    WEAK_AHEAD_LOAD("6.1.C"),
    TRANSMISSION_OUTAGE("10.1.A&B"),
    WEEK_FORECAST_CAPACITY("11.1.A.C"),
    PHYSICAL_FLOW("12.1.G"),
    GENERATION_OUTAGE("15.1.A&B"),
    AGGREGATED_GENERATION_TYPE("16.1.B&C");
    
    private static final Logger LOGGER = LogManager.getLogger(ColumnType.class);
    
    private static final Map<String, ColumnType> ARTICLE_TO_COLUMN_TYPE;
    
    static {
        ARTICLE_TO_COLUMN_TYPE = new HashMap<>();
        for(ColumnType curCol : ColumnType.values()) {
            ARTICLE_TO_COLUMN_TYPE.put(curCol.getRelevantArticle(), curCol);
        }
    }
    
    private final String relevantArticle;

    private ColumnType(String relevantArticle) {
        this.relevantArticle = relevantArticle;
    }

    public String getRelevantArticle() {
        return relevantArticle;
    }
    
    public static ColumnType ColumnTypeFromArticle(String article) {
        if (article == null) {
            throw new IllegalArgumentException("Article cannot be null");
        }
        ColumnType colType = ARTICLE_TO_COLUMN_TYPE.get(article);
        if (colType == null) {
            throw new IllegalArgumentException("Can not find the columnType for input article " + article);
        }
        return colType;
    }
}