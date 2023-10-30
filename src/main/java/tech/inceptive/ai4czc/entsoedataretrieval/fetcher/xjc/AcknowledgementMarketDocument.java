/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package tech.inceptive.ai4czc.entsoedataretrieval.fetcher.xjc;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Andres Bel Alonso
 */
@XmlRootElement(name = "Acknowledgement_MarketDocument")
@XmlAccessorType(XmlAccessType.FIELD)
public class AcknowledgementMarketDocument {

    @XmlElement(name = "mRID", required = true)
    private String mRID;

    @XmlElement(name = "createdDateTime")
    protected XMLGregorianCalendar createdDateTime;

    @XmlElement(name = "sender_MarketParticipant.mRID")
    private PartyIDString senderMarketParticipant;

    @XmlElement(name = "receiver_MarketParticipant.mRID")
    private PartyIDString receiverMarketParticipant;

    @XmlElement(name = "received_MarketDocument.createdDateTime")
    private XMLGregorianCalendar receivedMarketDocumentCreatedDateTime;

    @XmlElement(name = "Reason")
    private Reason reason;

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public XMLGregorianCalendar getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(XMLGregorianCalendar createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public PartyIDString getSenderMarketParticipant() {
        return senderMarketParticipant;
    }

    public void setSenderMarketParticipant(PartyIDString senderMarketParticipant) {
        this.senderMarketParticipant = senderMarketParticipant;
    }

    public PartyIDString getReceiverMarketParticipant() {
        return receiverMarketParticipant;
    }

    public void setReceiverMarketParticipant(PartyIDString receiverMarketParticipant) {
        this.receiverMarketParticipant = receiverMarketParticipant;
    }

    public XMLGregorianCalendar getReceivedMarketDocumentCreatedDateTime() {
        return receivedMarketDocumentCreatedDateTime;
    }

    public void setReceivedMarketDocumentCreatedDateTime(XMLGregorianCalendar receivedMarketDocumentCreatedDateTime) {
        this.receivedMarketDocumentCreatedDateTime = receivedMarketDocumentCreatedDateTime;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    
    
}