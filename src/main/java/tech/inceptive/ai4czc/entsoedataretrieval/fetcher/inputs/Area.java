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
 *
 * @author Andres Bel Alonso
 */
public enum Area {
    MONTENEGRO("10YCS-CG-TSO---S", "BZN|ME, CTA|ME, Montenegro (ME), MBA|ME, SCA|ME, LFA|ME",
            "Montenegro", "ME"),
    ITALY_CENTRE_SOUTH("10Y1001A1001A71M", "BZN|IT-Centre-South, SCA|IT-Centre-South, MBA|IT-Z-Centre-South",
            "Italy Centre South", "IT-CS"),
    BOSNIA("10YBA-JPCC-----D", "LFA|BA, BZN|BA, CTA|BA, Bosnia and Herz. (BA), SCA|BA, MBA|BA",
            "Bosnia", "BA"),
    SERBIA("10YCS-SERBIATSOV", "LFA|RS, SCA|RS, MBA|RS, Serbia (RS), CTA|RS, BZN|RS",
            "Serbia", "RS"),
    KOSOVO("10Y1001C--00100H", "BZN|XK, CTA|XK, Kosovo (XK), MBA|XK, LFB|XK, LFA|XK",
            "Kosovo", "XK"),
    ALBANIA("10YAL-KESH-----5", "LFB|AL, LFA|AL, BZN|AL, CTA|AL, Albania (AL), SCA|AL, MBA|AL", 
            "Albania", "AL");

    private static final Logger LOGGER = LogManager.getLogger(Area.class);

    private static final Map<String, Area> CLIID_TO_AREA = new HashMap<>();
    private static final Map<String, Area> ID_TO_AREA = new HashMap<>();

    static {
        for (Area area : Area.values()) {
            CLIID_TO_AREA.put(area.getOptionCLIID(), area);
            ID_TO_AREA.put(area.getId(), area);
        }
    }

    private final String id;
    private final String longDescription;
    private final String prettyName;
    private final String optionCLIID;

    private Area(String id, String longDescription, String prettyName, String optionCLIID) {
        this.id = id;
        this.longDescription = longDescription;
        this.prettyName = prettyName;
        this.optionCLIID = optionCLIID;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * The long description as presented on ENTSOE platform
     *
     * @return
     */
    public String getLongDescription() {
        return longDescription;
    }

    /**
     * Pretty name of the selected area
     *
     * @return
     */
    public String getPrettyName() {
        return prettyName;
    }

    /**
     * The ID on the CLI interface
     *
     * @return
     */
    public String getOptionCLIID() {
        return optionCLIID;
    }

    public static Area fromCLIID(String cliId) {
        if (cliId == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        Area area = CLIID_TO_AREA.get(cliId);
        if (area == null) {
            throw new IllegalArgumentException("No enum instance found with id: " + cliId);
        }
        return area;
    }

    public static Area fromID(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        Area area = ID_TO_AREA.get(id);
        if (area == null) {
            throw new IllegalArgumentException("Non enum instance found with id:" + id);
        }
        return area;
    }

}
