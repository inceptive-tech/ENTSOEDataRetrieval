//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0.1 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.04.21 at 09:42:42 AM UTC 
//


package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LocalUnitSymbol.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LocalUnitSymbol"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="AMP"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "LocalUnitSymbol", namespace = "urn:entsoe.eu:wgedi:codelists")
@XmlEnum
public enum LocalUnitSymbol {

    AMP;

    public String value() {
        return name();
    }

    public static LocalUnitSymbol fromValue(String v) {
        return valueOf(v);
    }

}
