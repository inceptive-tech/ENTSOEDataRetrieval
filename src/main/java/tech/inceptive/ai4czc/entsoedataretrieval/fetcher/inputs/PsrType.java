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
public enum PsrType {
    MIXED("A03", "Mixed", "mixed"),
    GENERATION("A04", "Generation", "generation"),
    LOAD("A05", "Load", "load"),
    BIOMASS("B01", "Biomass", "biomass"),
    FOSSIL_BROWN_COAL_LIGNITE("B02", "Fossil Brown coal/Lignite", "brown_coal"),
    FOSSIL_COAL_DERIVED_GAS("B03", "Fossil Coal-derived gas", "gas_coal"),
    FOSSIL_GAS("B04", "Fossil Gas", "gas"),
    FOSSIL_HARD_COAL("B05", "Fossil Hard coal", "hard_coal"),
    FOSSIL_OIL("B06", "Fossil Oil", "oil"),
    FOSSIL_OIL_SHALE("B07", "Fossil Oil shale", "oil_shale"),
    FOSSIL_PEAT("B08", "Fossil Peat", "peat"),
    GEOTHERMAL("B09", "Geothermal", "geothermal"),
    HYDRO_PUMPED_STORAGE("B10", "Hydro Pumped Storage", "hydro_pumped"),
    HYDRO_RUN_OF_RIVER_AND_POUNDAGE("B11", "Hydro Run-of-river and poundage", "hydro_run_river"),
    HYDRO_WATER_RESERVOIR("B12", "Hydro Water Reservoir", "hydro_reservoir"),
    MARINE("B13", "Marine", "marine"),
    NUCLEAR("B14", "Nuclear", "nuclear"),
    OTHER_RENEWABLE("B15", "Other renewable", "other_renewable"),
    SOLAR("B16", "Solar", "solar"),
    WASTE("B17", "Waste", "waste"),
    WIND_OFFSHORE("B18", "Wind Offshore", "wind_offshore"),
    WIND_ONSHORE("B19", "Wind Onshore", "wind_onshore"),
    OTHER("B20", "Other", "other"),
    AC_LINK("B21", "AC Link", "ac_link"),
    DC_LINK("B22", "DC Link", "dc_link"),
    SUBSTATION("B23", "Substation", "substation"),
    TRANSFORMER("B24", "Transformer", "transformer");

    private static final Map<String, PsrType> ID_TO_PSR;

    static {
        ID_TO_PSR = new HashMap<>();
        for (PsrType curType : PsrType.values()) {
            ID_TO_PSR.put(curType.id, curType);
        }
    }

    /**
     * ENTSOE transparency ID
     */
    private final String id;
    /**
     * ENTSOE transparency description
     */
    private final String description;
    /**
     * Description for the naming on output files
     */
    private final String csvName;

    private PsrType(String id, String description, String csvName) {
        this.id = id;
        this.description = description;
        this.csvName = csvName;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCsvName() {
        return csvName;
    }

    public static PsrType fromID(String id ){
        if(id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        PsrType psr = ID_TO_PSR.get(id);
        if(psr == null) {
            throw new IllegalArgumentException("Non enum instance found with id :" + id);
        }
        return psr;
    }
}
