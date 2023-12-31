//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.06.09 at 02:13:47 PM UTC 
//


package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc.statusrequest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StandardCategoryTypeList.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="StandardCategoryTypeList"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="A01"/&gt;
 *     &lt;enumeration value="A02"/&gt;
 *     &lt;enumeration value="A03"/&gt;
 *     &lt;enumeration value="A04"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "StandardCategoryTypeList", namespace = "urn:entsoe.eu:wgedi:codelists")
@XmlEnum
public enum StandardCategoryTypeList {


    /**
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;CodeDescription xmlns:ecl="urn:entsoe.eu:wgedi:codelists" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;Title&gt;Base&lt;/Title&gt;&lt;Definition&gt;The auction is for a base period.&lt;/Definition&gt;&lt;/CodeDescription&gt;
     * </pre>
     * 
     * 
     */
    @XmlEnumValue("A01")
    A_01("A01"),

    /**
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;CodeDescription xmlns:ecl="urn:entsoe.eu:wgedi:codelists" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;Title&gt;Peak&lt;/Title&gt;&lt;Definition&gt;The auction is for a peak period.&lt;/Definition&gt;&lt;/CodeDescription&gt;
     * </pre>
     * 
     * 
     */
    @XmlEnumValue("A02")
    A_02("A02"),

    /**
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;CodeDescription xmlns:ecl="urn:entsoe.eu:wgedi:codelists" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;Title&gt;Off peak&lt;/Title&gt;&lt;Definition&gt;The auction is for an off peak period.&lt;/Definition&gt;&lt;/CodeDescription&gt;
     * </pre>
     * 
     * 
     */
    @XmlEnumValue("A03")
    A_03("A03"),

    /**
     * 
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;CodeDescription xmlns:ecl="urn:entsoe.eu:wgedi:codelists" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;Title&gt;Hourly&lt;/Title&gt;&lt;Definition&gt;The auction is for an hourly period.&lt;/Definition&gt;&lt;/CodeDescription&gt;
     * </pre>
     * 
     * 
     */
    @XmlEnumValue("A04")
    A_04("A04");
    private final String value;

    StandardCategoryTypeList(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StandardCategoryTypeList fromValue(String v) {
        for (StandardCategoryTypeList c: StandardCategoryTypeList.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
