/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.inputs;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Andres Bel Alonso
 */
public enum Params {
    BIDDING_ZONE_DOMAIN("biddingZone_Domain"),
    DOCUMENT_TYPE("documentType"),
    PERIOD_END("periodEnd"),
    PERIOD_START("periodStart"),
    PROCESS_TYPE("processType"),
    OUT_BIDDINDZONE_DOMAIN("outBiddingZone_Domain"),
    IN_DOMAIN("in_Domain"),
    OUT_DOMAIN("out_Domain"),
    SECURITY_TOKEN("securityToken"),
    OFFSET("offset"),
    CONTRACT_MARKET_AGREEMENT_TYPE("contract_MarketAgreement.Type");
    
    private static final Logger LOGGER = LogManager.getLogger(Params.class);
 
    private final String value;

    private Params(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
