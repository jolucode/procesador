package service.cloud.request.clientRequest.handler.object.legend;

/**
 * Esta clase contiene la informacion de la leyenda de un comprobante
 * de pago.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class LegendObject {

    private String legendValue;


    /**
     * Constructor basico de la clase LegendObject.
     */
    public LegendObject() {
    }


    public String getLegendValue() {
        return legendValue;
    } //getLegendValue

    public void setLegendValue(String legendValue) {
        this.legendValue = legendValue;
    } //setLegendValue

} //LegendObject
