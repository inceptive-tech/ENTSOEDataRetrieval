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
public enum ContractMarketAgreement {
    DAILY("A01", "Daily"),
    WEEKLY("A02", "Weekly"),
    MONTHLY("A03", "Monthly"),
    YEARLY("A04", "Yearly"),
    TOTAL("A05", "Total"),
    LONG_TERM("A06", "Long term"),
    INTRADAY("A07", "Intraday"),
    HOURLY("A13", "Hourly (Type_MarketAgreement.Type only)");

    private final String code;
    private final String meaning;

    private ContractMarketAgreement(String code, String meaning) {
        this.code = code;
        this.meaning = meaning;
    }

    public String getCode() {
        return code;
    }

    public String getMeaning() {
        return meaning;
    }
}