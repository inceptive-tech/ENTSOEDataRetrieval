/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.inceptive.ai4czc.entsoedataretrieval.exceptions.DataRetrievalError;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.Area;
import tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs.ColumnType;

/**
 *
 * @author andres
 */
public class MainCLI {

    private static final Logger LOGGER = LogManager.getLogger(MainCLI.class);

    private static final String CSV_ESCAPE_CHAR_NAME = "csv-escape";
    private static final String CSV_SEPARATOR_NAME = "csv-separator";
    private static final String TIME_STEP_NAME = "time-step";
    private static final String AREA_NAME = "area";
    private static final String AREA_FLOW_NAME = "area-flow";
    private static final String CACHE_NAME = "cache";
    private static final String MAX_REQUEST_DURATION_NAME = "max-request-duration";

    public static void main(String[] args) {
        // CLI definition
        Options options = new Options();
        options.addRequiredOption("t", "token", true, "Requiered. ENTSOE transparency token to "
                + "use in the API.");
        options.addRequiredOption("s", "stard-date", true, "Requiered. Start date is enclusive. Start date on format day-Month-yearTHour:Minute:second");
        options.addRequiredOption("e", "end-date", true, "Requiered. End date is exclusive. End date on format day-Month-yearTHour:Minute:second");
        options.addOption("o", "output-file", true, "Output file to print the output CSV. Otherwise will be printed on stdout");
        options.addOption("w", "overwrite", false, "Sets if the output file should be overwrite even if it already exists");
        options.addOption(Option.builder().
                longOpt("all").
                desc("Add this to fetch all possible columns handle by "
                        + "this module. This option will make ignore the columns option. Not having this option or c will produce an empty file").build());
        // TODO : document what columns you can fetch
        options.addOption("c", "columns", true, "A coma separated list of column, of the relevant articles to retrieve. \n"
                + "Please note that all option is prioritary.\n"
                + "Suported articles : 6.1.A : Actual load.\n"
                + "6.1.B : Day Ahead load forecast.\n"
                + "6.1.C : Week Ahead load forecast.\n"
                + "10.1.A&B : Transmission outage.\n"
                + "11.1.A.C : Transfert capacity forecast (week).\n"
                + "12.1.G : Physical flows.\n"
                + "15.1.A&B : Generation outage.\n"
                + "16.1.B&C : Aggregated generation per generation type.");
        options.addOption(Option.builder().
                longOpt(CSV_ESCAPE_CHAR_NAME).
                hasArg().
                desc("The csv escape char of the output file. \" by default").build());
        options.addOption(Option.builder().
                longOpt(CSV_SEPARATOR_NAME).
                hasArg().
                desc("The csv separator of the output file. , by default").build());
        options.addOption(Option.builder().
                longOpt(TIME_STEP_NAME).
                hasArg().
                desc("The amount of minutes between each time steep. "
                        + "Depending on the column, the value can change. Usually 60 is ok and it is th default value").build());
        String countriesDsc = Arrays.asList(Area.values()).stream().
                map(a -> a.getPrettyName() + "-" + a.getId()).
                collect(Collectors.joining(" ", "(", ")"));
        options.addOption(Option.builder(). // TODO change this to many aread to extract the data
                option("a").
                longOpt(AREA_NAME).
                hasArg().
                // new areas are added automatically to the description
                desc("A coma saparate list of ids to extract data. "
                        + " Posible areas (pretty_name-id) : "
                        + countriesDsc).build());
        options.addOption(Option.builder().
                longOpt(AREA_FLOW_NAME).
                hasArg().
                desc("The main area to compute the flows. All flows will be computed between this area and the "
                        + "list in the area option.").build());
        options.addOption(Option.builder().
                longOpt(CACHE_NAME).
                desc("Activate the local files cache to prevent doing queries to ENTSOE platform").build());
        options.addOption(Option.builder().
                longOpt(MAX_REQUEST_DURATION_NAME).
                hasArg().
                desc("The maximal time interval duration for a query. Set this when retrieving outages. "
                        + "The formats accepted are based on the ISO-8601 duration format PnDTnHnMn.nS with days "
                        + "considered to be exactly 24 hours.\n"
                        + "Examples :     "
                        + "\"PT20.345S\" -- parses as \"20.345 seconds\"\n"
                        + "    \"PT15M\"     -- parses as \"15 minutes\" (where a minute is 60 seconds)\n"
                        + "    \"PT10H\"     -- parses as \"10 hours\" (where an hour is 3600 seconds)\n"
                        + "    \"P2D\"       -- parses as \"2 days\" (where a day is 24 hours or 86400 seconds)\n"
                        + "    \"P2DT3H4M\"  -- parses as \"2 days, 3 hours and 4 minutes\"").build());
        options.addOption(new Option("h", "help", false, "Display help and does nothing else."));

        // manual parsing of help
        // If the user uses help, he will not add the requiered parameters and the app will crash
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar data-retriever.jar -t [your_token] -s [start_date] -e [end_date]", options);
                return;
            }
        }

        // CLI entry processing
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println("Problem parsing the parameters. " + ex.getMessage());
            return;
        }

        // launching
        String token = cmd.getOptionValue("t");
        boolean overwrite = cmd.hasOption("w");
        LocalDateTime startDate = LocalDateTime.parse(cmd.getOptionValue("s"), DateTimeFormatter.ofPattern("y-M-d\'T\'H:m:s"));
        LocalDateTime endDate = LocalDateTime.parse(cmd.getOptionValue("e"), DateTimeFormatter.ofPattern("y-M-d\'T\'H:m:s"));
        String csvEscapeChar = cmd.getOptionValue(CSV_ESCAPE_CHAR_NAME, "\"");
        String csvSeparator = cmd.getOptionValue(CSV_SEPARATOR_NAME, ",");
        String timeStepValue = cmd.getOptionValue(TIME_STEP_NAME, "60");
        Duration duration = Duration.ofMinutes(Long.parseLong(timeStepValue));
        boolean useRequestCache = cmd.hasOption(CACHE_NAME);
        Set<ColumnType> columnTypes = new HashSet<>();
        Set<Area> targetAreas = new HashSet<>();
        Area mainArea = null;

        //columns
        if (cmd.hasOption("all")) {
            columnTypes.addAll(Arrays.asList(ColumnType.values()));
        } else if (cmd.hasOption("c")) {
            String[] splitedVal = cmd.getOptionValue("c").split(",");
            columnTypes.addAll(Arrays.stream(splitedVal).map(s -> ColumnType.ColumnTypeFromArticle(s)).toList());
        }

        //areas
        if (cmd.hasOption("all")) {
            targetAreas.addAll(Arrays.asList(Area.values()));
        } else if (cmd.hasOption("a")) {
            String[] splitVal = cmd.getOptionValue("a").split(",");
            targetAreas.addAll(Arrays.stream(splitVal).map(s -> Area.fromCLIID(s)).toList());
        }
        if (cmd.hasOption(AREA_FLOW_NAME)) {
            mainArea = Area.fromCLIID(cmd.getOptionValue(AREA_FLOW_NAME));
        }
        Duration maxRequestDuration;
        //other
        if (cmd.hasOption(MAX_REQUEST_DURATION_NAME)) {
            maxRequestDuration = Duration.parse(cmd.getOptionValue(MAX_REQUEST_DURATION_NAME));
        } else {
            maxRequestDuration = Duration.ofDays(365);
        }
        LOGGER.info("Retrieving dataset");

        ENTSOEHistoricalDataRetrieval dataRetrieval = new ENTSOEHistoricalDataRetrieval(token,
                columnTypes, csvEscapeChar, csvSeparator, useRequestCache);
        File tmpFile = dataRetrieval.fetchDataset(startDate, endDate, duration, targetAreas, mainArea,
                maxRequestDuration);
        LOGGER.info("Dataset correctly retrieve");
        if (cmd.hasOption("o")) {
            String filePath = cmd.getOptionValue("o");
            File outFile = new File(filePath);
            if (outFile.exists() && (!overwrite)) {
                System.err.println("Unable to write to file " + outFile.getAbsolutePath() + ". File already exists and"
                        + "overwrite option was not added. Try to add -w option to solve this");
                return;
            }
            try {
                Files.move(tmpFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                System.err.println("Unable to move temporal file " + tmpFile.getAbsolutePath() + " to destination path"
                        + filePath);
            }

        } else {
            throw new UnsupportedOperationException("Not suported already to print in std ouput the file");
        }

    }

}
