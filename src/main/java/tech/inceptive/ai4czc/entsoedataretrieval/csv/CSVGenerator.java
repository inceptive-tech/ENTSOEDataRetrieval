/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.csv.transformers.CSVTransformer;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;

/**
 *
 * @author Andres Bel Alonso
 */
public class CSVGenerator {

    private static final Logger LOGGER = LogManager.getLogger(CSVGenerator.class);
    
    private static record ColumnBlock(List<ColumnDefinition> colDefs, List<String> missingValues){

        public ColumnBlock(List<ColumnDefinition> colDefs, List<String> missingValues) {
            if(colDefs.size() != missingValues.size()) {
                throw new DataRetrievalError("Malformed column signature. Column definition size of " + colDefs.size() + 
                        " while " + missingValues.size());
            }
            this.colDefs = colDefs;
            this.missingValues = missingValues;
        }

        
    }

    private final String csvSepator;
    private final String csvEscapeChar;

    public CSVGenerator(String csvSepator, String csvEscapeChar) {
        this.csvSepator = csvSepator;
        this.csvEscapeChar = csvEscapeChar;
    }

    public void writeCSVFile(List<CSVTransformer> transformers, File targetFile, LocalDateTime startDate,
            LocalDateTime endDate, Duration timeStep) {

        List<ColumnBlock> colDefs = transformers.stream().
                map(t -> new ColumnBlock(t.getColumnDefinition(), t.getMissingValues())).
                distinct().// it can be many documents representing the same thing but at different 
                toList();
        // column definitions as provided by transformers.getColumnDefinition
        // In the values, transformers are grouped by same column. In fact, they represent transformers of the same column, but at different time stamps
        Map<ColumnBlock, List<CSVTransformer>> transformersIndex = buildTransformersIndex(transformers);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(targetFile);
            bw = new BufferedWriter(fw);
            //write header
            writeHeader(colDefs, bw);
            LocalDateTime curDT = startDate;
            int idCount = 0;
            while (curDT.isBefore(endDate)) {
                bw.newLine();
                // id
                bw.write(String.valueOf(idCount));
                bw.write(csvSepator);
                // Time stamp
                bw.write(curDT.format(DateTimeFormatter.ISO_DATE_TIME));
                bw.flush();
                for (ColumnBlock colBlock : colDefs) {
                    boolean writedColumn = false;
                    if(transformersIndex.containsKey(colBlock)) {
                        List<CSVTransformer> associatedTransformer = transformersIndex.get(colBlock);
                        for(CSVTransformer curTransformer : associatedTransformer) {
                            boolean writed = curTransformer.writeNextEntry(curDT, bw);
                            if(writed) {
                                // when one transformer
                                writedColumn = true;
                                break;
                            }
                        }
                        if(!writedColumn) {
                            // None of the transformers could write for the given entry, filling it
                            for(int i=0; i<colBlock.colDefs.size(); i++) {
                                bw.write(csvSepator + colBlock.missingValues.get(i));
                            }
                        }
                    } else {
                        throw new DataRetrievalError("Uncoherent columns to transformers index. This should not happen");
                    }
                }
                bw.flush();
                idCount++;
                curDT = curDT.plus(timeStep);
            }
        } catch (IOException ex) {
            throw new DataRetrievalError(ex);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                LOGGER.catching(ex);
            }
        }        
    }

    private void writeHeader(List<ColumnBlock> colDefs, BufferedWriter bof) throws IOException {
        bof.write("id,time_stamp");
        colDefs.stream().
                map(ColumnBlock::colDefs).
                flatMap(List::stream).
                forEach((Consumer<ColumnDefinition>) c -> {
            try {
                bof.write(csvSepator);
                String colName = c.colName();
                if (colName.contains(this.csvEscapeChar)) {
                    throw new UnsupportedOperationException("Non suported, escapechar on column name " + colName);
                }
                if (colName.contains(this.csvSepator)) {
                    colName = csvEscapeChar + colName + csvEscapeChar;
                }
                bof.write(colName);
            } catch (IOException ex) {
                throw new DataRetrievalError(ex);
            }
        });
        bof.flush();
    }

    private Map<ColumnBlock, List<CSVTransformer>> buildTransformersIndex(List<CSVTransformer> transformers) {
        Map<ColumnBlock, List<CSVTransformer>> res = new HashMap<>();
        for(CSVTransformer curTransformer : transformers) {
            List<ColumnDefinition> colDefs = curTransformer.getColumnDefinition();
            List<String> missingValues = curTransformer.getMissingValues();
            ColumnBlock colBlock = new ColumnBlock(colDefs, missingValues);
            res.computeIfAbsent(colBlock, e -> new ArrayList<>());
            res.get(colBlock).add(curTransformer);
        }
        return res;
    }
}
