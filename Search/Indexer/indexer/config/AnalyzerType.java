//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.03.17 at 01:35:22 PM CET 
//


package indexer.config;

import javax.xml.bind.annotation.XmlEnum;
import indexer.config.AnalyzerType;


/**
 * <p>Java class for analyzerType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="analyzerType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STOP"/>
 *     &lt;enumeration value="SIMPLE"/>
 *     &lt;enumeration value="STANDARD"/>
 *     &lt;enumeration value="WHITESPACE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum AnalyzerType {

    SIMPLE,
    STANDARD,
    STOP,
    WHITESPACE;

    public String value() {
        return name();
    }

    public AnalyzerType fromValue(String v) {
        return valueOf(v);
    }

}
