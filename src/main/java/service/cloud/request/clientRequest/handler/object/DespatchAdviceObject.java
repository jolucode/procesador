/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.handler.object;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author VS-LT-06
 */
public class DespatchAdviceObject {

    public String getDireccionDestino() {
        return DireccionDestino;
    }

    public void setDireccionDestino(String DireccionDestino) {
        this.DireccionDestino = DireccionDestino;
    }

    public String getDireccionPartida() {
        return DireccionPartida;
    }

    public void setDireccionPartida(String DireccionPartida) {
        this.DireccionPartida = DireccionPartida;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getObervaciones() {
        return obervaciones;
    }

    public void setObervaciones(String obervaciones) {
        this.obervaciones = obervaciones;
    }

    public String getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(String fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getNumeroDocEmisor() {
        return NumeroDocEmisor;
    }

    public void setNumeroDocEmisor(String NumeroDocEmisor) {
        this.NumeroDocEmisor = NumeroDocEmisor;
    }

    public String getNumeroDocConsumidor() {
        return numeroDocConsumidor;
    }

    public void setNumeroDocConsumidor(String numeroDocConsumidor) {
        this.numeroDocConsumidor = numeroDocConsumidor;
    }

    public String getNombreEmisor() {
        return nombreEmisor;
    }

    public void setNombreEmisor(String nombreEmisor) {
        this.nombreEmisor = nombreEmisor;
    }

    public String getNombreConsumidor() {
        return nombreConsumidor;
    }

    public void setNombreConsumidor(String nombreConsumidor) {
        this.nombreConsumidor = nombreConsumidor;
    }

    public String getFechaTraslado() {
        return fechaTraslado;
    }

    public void setFechaTraslado(String fechaTraslado) {
        this.fechaTraslado = fechaTraslado;
    }

    public String getCodigoMotivoTraslado() {
        return codigoMotivoTraslado;
    }

    public void setCodigoMotivoTraslado(String codigoMotivoTraslado) {
        this.codigoMotivoTraslado = codigoMotivoTraslado;
    }

    public String getDescripcionMotivoTraslado() {
        return descripcionMotivoTraslado;
    }

    public void setDescripcionMotivoTraslado(String descripcionMotivoTraslado) {
        this.descripcionMotivoTraslado = descripcionMotivoTraslado;
    }

    public String getModalidadTraslado() {
        return modalidadTraslado;
    }

    public void setModalidadTraslado(String modalidadTraslado) {
        this.modalidadTraslado = modalidadTraslado;
    }

    public BigDecimal getNumeroBultos() {
        return numeroBultos;
    }

    public void setNumeroBultos(BigDecimal numeroBultos) {
        this.numeroBultos = numeroBultos;
    }

    public BigDecimal getPesoBruto() {
        return pesoBruto;
    }

    public void setPesoBruto(BigDecimal pesoBruto) {
        this.pesoBruto = pesoBruto;
    }

    public String getUMPesoBruto() {
        return UMPesoBruto;
    }

    public void setUMPesoBruto(String UMPesoBruto) {
        this.UMPesoBruto = UMPesoBruto;
    }

    public String getRUCTransportista() {
        return RUCTransportista;
    }

    public void setRUCTransportista(String RUCTransportista) {
        this.RUCTransportista = RUCTransportista;
    }

    public String getNombreTransportista() {
        return NombreTransportista;
    }

    public void setNombreTransportista(String NombreTransportista) {
        this.NombreTransportista = NombreTransportista;
    }

    public String getTipoDocumentoTransportista() {
        return TipoDocumentoTransportista;
    }

    public void setTipoDocumentoTransportista(String TipoDocumentoTransportista) {
        this.TipoDocumentoTransportista = TipoDocumentoTransportista;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public String getTipoDocumentoConductor() {
        return TipoDocumentoConductor;
    }

    public void setTipoDocumentoConductor(String TipoDocumentoConductor) {
        this.TipoDocumentoConductor = TipoDocumentoConductor;
    }

    public String getDocumentoConductor() {
        return DocumentoConductor;
    }

    public void setDocumentoConductor(String DocumentoConductor) {
        this.DocumentoConductor = DocumentoConductor;
    }

    public String getNumeroContenedor() {
        return numeroContenedor;
    }

    public void setNumeroContenedor(String numeroContenedor) {
        this.numeroContenedor = numeroContenedor;
    }

    public String getCodigoEmbarque() {
        return codigoEmbarque;
    }

    public void setCodigoEmbarque(String codigoEmbarque) {
        this.codigoEmbarque = codigoEmbarque;
    }

    public List<WrapperItemObject> getItemListDynamic() {
        return itemListDynamic;
    }

    public void setItemListDynamic(List<WrapperItemObject> itemListDynamic) {
        this.itemListDynamic = itemListDynamic;
    }

    public String getResolutionCodeValue() {
        return resolutionCodeValue;
    }

    public void setResolutionCodeValue(String resolutionCodeValue) {
        this.resolutionCodeValue = resolutionCodeValue;
    }

    public String getValidezPDF() {
        return validezPDF;
    }

    public void setValidezPDF(String validezPDF) {
        this.validezPDF = validezPDF;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTelefono1() {
        return telefono1;
    }

    public void setTelefono1(String telefono1) {
        this.telefono1 = telefono1;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPaginaWeb() {
        return paginaWeb;
    }

    public void setPaginaWeb(String paginaWeb) {
        this.paginaWeb = paginaWeb;
    }

    public String getSenderLogo() {
        return senderLogo;
    }

    public void setSenderLogo(String senderLogo) {
        this.senderLogo = senderLogo;
    }

    public String getLicenciaConducir() {
        return licenciaConducir;
    }

    public void setLicenciaConducir(String licenciaConducir) {
        this.licenciaConducir = licenciaConducir;
    }

    public Map<String, String> getDespatchAdvicePersonalizacion() {
        return despatchAdvicePersonalizacion;
    }

    public void setDespatchAdvicePersonalizacion(Map<String, String> despatchAdvicePersonalizacion) {
        this.despatchAdvicePersonalizacion = despatchAdvicePersonalizacion;
    }

    public InputStream getCodeQR() {
        return codeQR;
    }

    public void setCodeQR(InputStream codeQR) {
        this.codeQR = codeQR;
    }

    private String DireccionDestino;

    private String DireccionPartida;

    private String numeroGuia;

    private String obervaciones;

    private String fechaEmision;

    private String NumeroDocEmisor;

    private String numeroDocConsumidor;

    private String nombreEmisor;

    private String nombreConsumidor;

    private String fechaTraslado;

    private String codigoMotivoTraslado;

    private String descripcionMotivoTraslado;

    private String modalidadTraslado;

    private BigDecimal numeroBultos;

    private BigDecimal pesoBruto;

    private String UMPesoBruto;

    private String RUCTransportista;

    private String NombreTransportista;

    private String TipoDocumentoTransportista;

    private String placaVehiculo;

    private String TipoDocumentoConductor;

    private String DocumentoConductor;

    private String numeroContenedor;

    private String codigoEmbarque;

    private List<WrapperItemObject> itemListDynamic;

    private String resolutionCodeValue;

    private String telefono;

    private String telefono1;

    private String email;

    private String paginaWeb;

    private String validezPDF;

    private String senderLogo;

    private String licenciaConducir;

    private InputStream codeQR;

    /* Lista de Personalizacion */
    Map<String, String> despatchAdvicePersonalizacion;

}
