package service.cloud.request.clientRequest.proxy.object;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for statusResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="statusResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="statusCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="statusMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statusResponse", propOrder = {
        "content",
        "statusCode",
        "statusMessage"
})
public class StatusResponse {

    protected byte[] content;
    protected String statusCode;
    protected String statusMessage;

    public StatusResponse() {

    }

    public StatusResponse(byte[] content, String statusCode, String statusMessage) {
        this.content = content;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public StatusResponse(String statusCode, String statusMessage, byte[] content) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.content = content;
    } //StatusResponse

    public StatusResponse(String statusCode, byte[] content) {
        this.statusCode = statusCode;
        this.content = content;
    }

    /**
     * Gets the value of the content property.
     *
     * @return possible object is
     * byte[]
     */
    public byte[] getContent() {

        return content;
    } //getContent

    /**
     * Sets the value of the content property.
     *
     * @param value allowed object is
     *              byte[]
     */
    public void setContent(byte[] value) {
        this.content = value;
    } //setContent

    /**
     * Gets the value of the statusCode property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusCode() {
        return statusCode;
    } //getStatusCode

    /**
     * Sets the value of the statusCode property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusCode(String value) {
        this.statusCode = value;
    } //setStatusCode

    /**
     * Gets the value of the statusMessage property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getStatusMessage() {
        return statusMessage;
    } //getStatusMessage

    /**
     * Sets the value of the statusMessage property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setStatusMessage(String value) {
        this.statusMessage = value;
    } //setStatusMessage

} //StatusResponse
