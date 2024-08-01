package service.cloud.request.clientRequest.handler.object.item;

import java.math.BigDecimal;

public class PerceptionItemObject {

    private String tipoDocumento;

    private String fechaEmision;

    private String numSerieDoc;

    private BigDecimal precioVenta;

    private BigDecimal importePercepcion;

    private BigDecimal montoTotalCobrado;

    private BigDecimal porcentajePercepcion;

    private String monedaPercepcion;

    private String monedaMontoTotal;

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getNumSerieDoc() {
        return numSerieDoc;
    }

    public void setNumSerieDoc(String numSerieDoc) {
        this.numSerieDoc = numSerieDoc;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getImportePercepcion() {
        return importePercepcion;
    }

    public void setImportePercepcion(BigDecimal importePercepcion) {
        this.importePercepcion = importePercepcion;
    }

    public BigDecimal getMontoTotalCobrado() {
        return montoTotalCobrado;
    }

    public void setMontoTotalCobrado(BigDecimal montoTotalCobrado) {
        this.montoTotalCobrado = montoTotalCobrado;
    }

    public BigDecimal getPorcentajePercepcion() {
        return porcentajePercepcion;
    }

    public void setPorcentajePercepcion(BigDecimal porcentajePercepcion) {
        this.porcentajePercepcion = porcentajePercepcion;
    }

    public String getMonedaPercepcion() {
        return monedaPercepcion;
    }

    public void setMonedaPercepcion(String monedaPercepcion) {
        this.monedaPercepcion = monedaPercepcion;
    }

    public String getMonedaMontoTotal() {
        return monedaMontoTotal;
    }

    public void setMonedaMontoTotal(String monedaMontoTotal) {
        this.monedaMontoTotal = monedaMontoTotal;
    }

}
