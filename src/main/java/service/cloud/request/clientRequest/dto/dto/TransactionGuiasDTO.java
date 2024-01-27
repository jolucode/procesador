package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionGuiasDTO {
    private String CodigoMotivo;
    private String CodigoPuerto;
    private String DescripcionMotivo;
    private String DocumentoConductor;
    private String LicenciaConductor;
    private String FechaInicioTraslado;
    private String IndicadorTransbordoProgramado;
    private String ModalidadTraslado;
    private String NombreRazonTransportista;
    private BigDecimal NumeroBultos;
    private String NumeroContenedor;
    private BigDecimal Peso;
    private String PlacaVehiculo;
    private String RUCTransporista;
    private String TipoDOCTransportista;
    private String TipoDocConductor;
    private String UnidadMedida;
    private String DireccionPartida;
    private String UbigeoPartida;

    private String TipoDocRelacionadoTrans;
    private String TipoDocRelacionadoTransDesc;
    private String DocumentoRelacionadoTrans;
    private String IndicadorTransbordo;
    private String IndicadorTraslado;
    private String IndicadorRetorno;
    private String IndicadorRetornoVehiculo;
    private String IndicadorTrasladoTotal;
    private String IndicadorRegistro;
    private String NroRegistroMTC;

    private String NombreApellidosConductor;
    private String UbigeoLlegada;
    private String DireccionLlegada;
    private String TarjetaCirculacion;
    private String numeroPrecinto;
    private String numeroContenedor2;
    private String numeroPrecinto2;
    private String DescripcionPuerto;
    private String CodigoAereopuerto;
    private String DescripcionAereopuerto;

    private String TicketRest;
}
