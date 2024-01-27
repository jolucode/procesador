package service.cloud.request.clientRequest.proxy.object;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for getStatus complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="getStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rucComprobante" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tipoComprobante" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="serieComprobante" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numeroComprobante" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getStatus2", propOrder = {
        "rucComprobante",
        "tipoComprobante",
        "serieComprobante",
        "numeroComprobante",
        "ticket"
})

public class GetStatus {

    protected String rucComprobante;

    protected String tipoComprobante;

    protected String serieComprobante;

    protected Integer numeroComprobante;

    protected String ticket;

    /**
     * Obtiene el valor de la propiedad ticket.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTicket() {
        return ticket;
    }

    /**
     * Define el valor de la propiedad ticket.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTicket(String value) {
        this.ticket = value;
    }


    /**
     * Gets the value of the rucComprobante property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRucComprobante() {
        return rucComprobante;
    } //getRucComprobante

    /**
     * Sets the value of the rucComprobante property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRucComprobante(String value) {
        this.rucComprobante = value;
    } //setRucComprobante

    /**
     * Gets the value of the tipoComprobante property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTipoComprobante() {
        return tipoComprobante;
    } //getTipoComprobante

    /**
     * Sets the value of the tipoComprobante property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTipoComprobante(String value) {
        this.tipoComprobante = value;
    } //setTipoComprobante

    /**
     * Gets the value of the serieComprobante property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSerieComprobante() {
        return serieComprobante;
    } //getSerieComprobante

    /**
     * Sets the value of the serieComprobante property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSerieComprobante(String value) {
        this.serieComprobante = value;
    } //setSerieComprobante

    /**
     * Gets the value of the numeroComprobante property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getNumeroComprobante() {
        return numeroComprobante;
    } //getNumeroComprobante

    /**
     * Sets the value of the numeroComprobante property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setNumeroComprobante(Integer value) {
        this.numeroComprobante = value;
    } //setNumeroComprobante

} //GetStatus
