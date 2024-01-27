package service.cloud.request.clientRequest.handler.object.item;

import java.math.BigDecimal;

public class RetentionItemObject {

    private String fechaEmision;

    private String fechaPago;

    private String tipoDocumento;

    private String nroDoc;

    private String montoTotal;

    private String monedaMontoTotal;

    private String monedaRetencion;

    private BigDecimal baseRetencion;

    private BigDecimal porcentaje;

    private String valorRetencion;

    private String montoSoles;

    private String tipoCambio;

    private String numeroPago;

    private BigDecimal importeNeto;

    public String getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(String fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getMonedaMontoTotal() {
        return monedaMontoTotal;
    }

    public void setMonedaMontoTotal(String monedaMontoTotal) {
        this.monedaMontoTotal = monedaMontoTotal;
    }

    public String getMonedaRetencion() {
        return monedaRetencion;
    }

    public void setMonedaRetencion(String monedaRetencion) {
        this.monedaRetencion = monedaRetencion;
    }

    public String getMontoSoles() {
        return montoSoles;
    }

    public void setMontoSoles(String montoSoles) {
        this.montoSoles = montoSoles;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNroDoc() {
        return nroDoc;
    }

    public void setNroDoc(String nroDoc) {
        this.nroDoc = nroDoc;
    }

    public String getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(String montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getBaseRetencion() {
        return baseRetencion;
    }

    public void setBaseRetencion(BigDecimal baseRetencion) {
        this.baseRetencion = baseRetencion;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getValorRetencion() {
        return valorRetencion;
    }

    public void setValorRetencion(String valorRetencion) {
        this.valorRetencion = valorRetencion;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getNumeroPago() {
        return numeroPago;
    }

    public void setNumeroPago(String numeroPago) {
        this.numeroPago = numeroPago;
    }

    public BigDecimal getImporteNeto() {
        return importeNeto;
    }

    public void setImporteNeto(BigDecimal importeNeto) {
        this.importeNeto = importeNeto;
    }

}
