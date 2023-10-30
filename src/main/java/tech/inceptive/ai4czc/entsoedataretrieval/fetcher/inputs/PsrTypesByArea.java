/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Andres Bel Alonso
 */
public class PsrTypesByArea {
    
    private static final Logger LOGGER = LogManager.getLogger(PsrTypesByArea.class);
    
    public static final PsrTypesByArea INSTANCE = new PsrTypesByArea();
    
    /**
     * It is important to keep a coherent order in the list of this map, as it will have an impact on the final result
     */
    private Map<Area, List<PsrType>> psrTypesByArea;

    private PsrTypesByArea() {
        psrTypesByArea = new HashMap<>();
        psrTypesByArea.put(Area.MONTENEGRO, 
                Arrays.asList(
                        // B02
                        PsrType.FOSSIL_BROWN_COAL_LIGNITE, 
                        // B11
                        PsrType.HYDRO_RUN_OF_RIVER_AND_POUNDAGE,
                        // B12
                        PsrType.HYDRO_WATER_RESERVOIR,
                        //B19
                        PsrType.WIND_ONSHORE
                        ));
        psrTypesByArea.put(Area.SERBIA,
                Arrays.asList(
                        // B01
                        PsrType.BIOMASS,
                        //B02
                        PsrType.FOSSIL_BROWN_COAL_LIGNITE,
                        //B04
                        PsrType.FOSSIL_GAS,
                        //B10
                        PsrType.HYDRO_PUMPED_STORAGE,
                        //B11
                        PsrType.HYDRO_RUN_OF_RIVER_AND_POUNDAGE,
                        //B12
                        PsrType.HYDRO_WATER_RESERVOIR,
                        //B16 and B17 are never added so I remove it
//                        PsrType.SOLAR,
                        //B17
//                        PsrType.WASTE,
                        //B19
                        PsrType.WIND_ONSHORE,
                        //B20
                        PsrType.OTHER));
        psrTypesByArea.put(Area.ITALY_CENTRE_SOUTH, 
                // TODO : improve it
                Arrays.asList(
                        //B01
                        PsrType.BIOMASS,
                        //B04
                        PsrType.FOSSIL_GAS,
                        //B05
                        PsrType.FOSSIL_HARD_COAL,
                        //B06
                        PsrType.FOSSIL_OIL,
                        //B10
                        PsrType.HYDRO_PUMPED_STORAGE,
                        //B11
                        PsrType.HYDRO_RUN_OF_RIVER_AND_POUNDAGE,
                        //B12
                        PsrType.HYDRO_WATER_RESERVOIR,
                        //B16
                        PsrType.SOLAR,
                        //B17
                        PsrType.WASTE,
                        //B19
                        PsrType.WIND_ONSHORE,
                        //B20
                        PsrType.OTHER));
        psrTypesByArea.put(Area.BOSNIA, 
                Arrays.asList(
                        //B02
                        PsrType.FOSSIL_BROWN_COAL_LIGNITE,
                        //B10
                        PsrType.HYDRO_PUMPED_STORAGE,
                        //B11
                        PsrType.HYDRO_RUN_OF_RIVER_AND_POUNDAGE,
                        //B12
                        PsrType.HYDRO_WATER_RESERVOIR,
                        //B15
                        PsrType.OTHER_RENEWABLE,
                        //B16
                        PsrType.SOLAR,
                        //B19
                        PsrType.WIND_ONSHORE));
        psrTypesByArea.put(Area.KOSOVO, 
                Arrays.asList(
                         //B02
                        PsrType.FOSSIL_BROWN_COAL_LIGNITE,
                        //B11
                        PsrType.HYDRO_RUN_OF_RIVER_AND_POUNDAGE,
                        //B12
                        PsrType.HYDRO_WATER_RESERVOIR,
                        //B19
                        PsrType.WIND_ONSHORE));
        
    }
    
    public List<PsrType> getProductionTypesForArea(Area inArea) {
        if(psrTypesByArea.containsKey(inArea)) {
            return psrTypesByArea.get(inArea);
        }
        throw new UnsupportedOperationException("Non supported area " + inArea.getPrettyName() + " for "
                                + "generation per type");

    }
 
}
