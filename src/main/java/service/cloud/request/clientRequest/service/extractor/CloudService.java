package service.cloud.request.clientRequest.service.extractor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dao.TransaccionRepository;
import service.cloud.request.clientRequest.dao.UsuariocamposRepository;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionGuiasDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionLineasDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionLineasImpuestoDTO;
import service.cloud.request.clientRequest.entity.*;
import service.cloud.request.clientRequest.exception.VenturaExcepcion;
import service.cloud.request.clientRequest.service.emision.interfac.InterfacePrincipal;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.Util;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.math.BigDecimal.valueOf;

@Service
public class CloudService implements CloudInterface {

    private final Logger logger = Logger.getLogger(String.valueOf(CloudService.class));

    @Autowired
    TransaccionRepository transaccionRepository;

    @Autowired
    UsuariocamposRepository usuariocamposRepository;

    @Autowired
    InterfacePrincipal interfacePrincipal;

    //@Value("${logging.database.name}")
    //private String databaseName;

    private BigDecimal descuentoTotal = new BigDecimal("0.0");


    @Override
    public ResponseEntity<TransacctionDTO[]> proccessDocument(String stringRequestOnpremise) {
        TransacctionDTO[] transacctionDTO = null;
        try {
            Gson gson = new Gson();
            transacctionDTO = gson.fromJson(stringRequestOnpremise, TransacctionDTO[].class);
            for (int i = 0; i < transacctionDTO.length; i++) {
                Transaccion transaccion = llenar(transacctionDTO[i]);
                interfacePrincipal.EnviarTransacciones(transaccion, stringRequestOnpremise);
            }
        } catch (Exception e) {
            logger.info("SE GENERO UN ERROR : " + e.getMessage());
        }
        return ResponseEntity.ok(transacctionDTO);
    }

    public Transaccion llenar(TransacctionDTO transacctionDTO) throws JsonProcessingException {



        logger.info("Tipo de transaccion: " + Util.getTipoTransac(transacctionDTO.getFE_TipoTrans()));


        Optional<Transaccion> optionalTransaccion = transaccionRepository.findById(transacctionDTO.getFE_Id());
        optionalTransaccion.ifPresent(transaccionRepository::delete);

        Transaccion transaccion = insertarDatosTransaccion(transacctionDTO);
        transaccion.setDbName(transacctionDTO.getDbName());
        transaccion.setFE_Id(transacctionDTO.getFE_Id());
        transacctionDTO.setFE_Id(transaccion.getFE_Id());


        transaccion.setTransaccionImpuestosList(extraerTransaccionImpuestos(transacctionDTO));
        transaccion.setTransaccionLineasList(extraerTransaccionLineas(transacctionDTO, transaccion));
        transaccion.setTransaccionCuotas(extraerTranssacionCuotas(transacctionDTO));
        Set<TransaccionLineas> transaccionLineas = new HashSet<>(transaccion.getTransaccionLineasList());
        insertarImpuestoBolsa(transaccionLineas, transaccion);
        List<Usuariocampos> usucamposList = usuariocamposRepository.findAll();
        List<TransaccionUsucampos> transaccionCamposUsuarios = getTransaccionCamposUsuarios(transaccion.getFE_Id(), transacctionDTO, usucamposList);
        transaccion.setTransaccionUsucamposList(transaccionCamposUsuarios);
        transaccion.setTransaccionContractdocrefList(extraerTransaccionContractDocRefs(transacctionDTO));
        transaccion.setTransaccionTotalesList(extraerTransaccionTotales(transacctionDTO));
        transaccion.setTransaccionGuiaRemision(extraerTransaccionGuias(transacctionDTO));
        transaccion.setTransaccionPropiedadesList(extraerTransaccionPropiedades(transacctionDTO));
        transaccion.setTransaccionDocrefersList(ExtraerTransaccionDocReferes(transacctionDTO));
        transaccion.setTransaccionComprobantePagoList(ExtraerTransaccionComprobantes(transacctionDTO));
        transaccion.setTransaccionAnticipoList(extraerTransaccionAnticipos(transacctionDTO));
        transaccion.setDOC_DescuentoTotal(transaccion.getDOC_Descuento());
        //transaccion.setKey_sociedad(transaccion.getSN_DocIdentidad_Nro());

        return transaccion;
    }

    public List<TransaccionUsucampos> getTransaccionCamposUsuarios(String FE_ID, TransacctionDTO transacctionDTO, List<Usuariocampos> usucamposList) throws JsonProcessingException {
        final List<TransaccionUsucampos> camposUsuario = new LinkedList<>();


        JSONObject jsonTransacction = Util.convertToJSONObject(transacctionDTO);
        JSONObject jsonPlano = Util.flattenJSON(jsonTransacction);

        String[] keys = JSONObject.getNames(jsonPlano);
        for (String key : keys) {
            if (key.startsWith("U_")) {
                final String valor = key.substring(2);
                Optional<Usuariocampos> optional = usucamposList.parallelStream().filter(transaccionUsucampo -> valor.equalsIgnoreCase(transaccionUsucampo.getNombre())).findAny();
                optional.ifPresent(transaccionUsucampo -> {
                    TransaccionUsucampos cu = new TransaccionUsucampos(new TransaccionUsucamposPK(FE_ID, transaccionUsucampo.getId()));
                    Object value = jsonPlano.get(key);
                    cu.setValor(value == null ? "" : value.toString());
                    camposUsuario.add(cu);
                });
            }
        }
        return camposUsuario;
    }

    private void insertarImpuestoBolsa(Set<TransaccionLineas> transaccionLineas, Transaccion transaccion) {
        char impuestoBolsa = 'I', itemBolsa = 'A';
        Optional<TransaccionLineas> lineasOptional = transaccionLineas.stream().filter(linea -> linea.getItemBolsa().equals(itemBolsa)).findAny();

        lineasOptional.ifPresent(lineaBolsa -> {
            Optional<TransaccionLineas> impuestoBolsaOptional = transaccionLineas.stream().filter(linea -> linea.getItemBolsa().equals(impuestoBolsa)).findAny();
            TransaccionImpuestosPK transaccionImpuestosPK = new TransaccionImpuestosPK();
            String feId = lineaBolsa.getTransaccionLineasPK().getFEId();
            transaccionImpuestosPK.setFEId(feId);
            int nroOrden = lineaBolsa.getTransaccionLineasPK().getNroOrden();
            int nroOrdenImpuesto = nroOrden;
            while (existeNumeroOrden(new HashSet<>(transaccion.getTransaccionImpuestosList()), nroOrdenImpuesto)) {
                nroOrdenImpuesto++;
            }
            transaccionImpuestosPK.setLineId(nroOrdenImpuesto);
            TransaccionImpuestos transaccionImpuesto = new TransaccionImpuestos();
            transaccionImpuesto.setTransaccionImpuestosPK(transaccionImpuestosPK);
            transaccionImpuesto.setAbreviatura(Constants.TAX_TOTAL_OTH_CODE);
            transaccionImpuesto.setMoneda(transaccion.getDOC_MON_Codigo());
            BigDecimal precioRefMonto = impuestoBolsaOptional.map(TransaccionLineas::getPrecioRefMonto).orElseGet(lineaBolsa::getPrecioRefMonto);
            BigDecimal totalBruto = impuestoBolsaOptional.map(TransaccionLineas::getTotalBruto).orElseGet(lineaBolsa::getTotalBruto);
            transaccionImpuesto.setMonto(precioRefMonto);
            transaccionImpuesto.setValorVenta(totalBruto);
            transaccionImpuesto.setPorcentaje(BigDecimal.valueOf(100));
            transaccionImpuesto.setTipoTributo(Constants.TAX_TOTAL_BPT_ID);
            transaccionImpuesto.setCodigo("C");
            transaccionImpuesto.setNombre(Constants.TAX_TOTAL_BPT_NAME);
            transaccion.getTransaccionImpuestosList().add(transaccionImpuesto);

            Optional<TransaccionImpuestos> impuestosOptional = transaccion.getTransaccionImpuestosList().stream().filter(impuestoTotal -> impuestoTotal.getNombre().isEmpty()).findAny();
            impuestosOptional.ifPresent(transaccion.getTransaccionImpuestosList()::remove);

            Optional<Integer> optionalNumeroOrden = lineaBolsa.getTransaccionLineaImpuestosList().stream().map(TransaccionLineaImpuestos::getTransaccionLineaImpuestosPK).map(TransaccionLineaImpuestosPK::getLineId).max(Integer::compareTo);
            int lineNumber = optionalNumeroOrden.orElseGet(lineaBolsa.getTransaccionLineaImpuestosList()::size) + 1;
            final TransaccionLineaImpuestos transaccionLineaImpuesto = new TransaccionLineaImpuestos();
            transaccionLineaImpuesto.setTransaccionLineaImpuestosPK(new TransaccionLineaImpuestosPK(feId, nroOrden, lineNumber));
            transaccionLineaImpuesto.setAbreviatura(Constants.TAX_TOTAL_OTH_CODE);
            transaccionLineaImpuesto.setMoneda(transaccion.getDOC_MON_Codigo());
            transaccionLineaImpuesto.setMonto(precioRefMonto);
            transaccionLineaImpuesto.setValorVenta(totalBruto);
            transaccionLineaImpuesto.setPorcentaje(BigDecimal.valueOf(100));
            transaccionLineaImpuesto.setTipoTributo(Constants.TAX_TOTAL_BPT_ID);
            transaccionLineaImpuesto.setCodigo("C");
            transaccionLineaImpuesto.setNombre(Constants.TAX_TOTAL_BPT_NAME);
            transaccionLineaImpuesto.setTransaccionLineas(lineaBolsa);
            lineaBolsa.getTransaccionLineaImpuestosList().add(transaccionLineaImpuesto);

            impuestoBolsaOptional.ifPresent(transaccion.getTransaccionLineasList()::remove);

            Predicate<TransaccionImpuestos> predicate = impuesto -> impuesto.getPorcentaje().compareTo(valueOf(100)) == 0 && impuesto.getValorVenta().compareTo(totalBruto) == 0 && !Constants.TAX_TOTAL_BPT_NAME.equalsIgnoreCase(impuesto.getNombre());
            ArrayList<TransaccionImpuestos> transaccionImpuestos = new ArrayList<>(transaccion.getTransaccionImpuestosList());
            Optional<TransaccionImpuestos> optional = transaccionImpuestos.stream().filter(predicate).findAny();
            optional.ifPresent(transaccion.getTransaccionImpuestosList()::remove);
        });
    }

    private boolean existeNumeroOrden(Set<TransaccionImpuestos> transaccionImpuestos, final int numeroOrden) {
        return transaccionImpuestos.stream().map(TransaccionImpuestos::getTransaccionImpuestosPK).anyMatch(impuesto -> impuesto.getLineId() == numeroOrden);
    }

    public Transaccion insertarDatosTransaccion(TransacctionDTO transacctionDTO) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(transacctionDTO);
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        Transaccion transaccion = gson.fromJson(jsonObject, Transaccion.class);
        return transaccion;
    }

    public List<TransaccionAnticipo> extraerTransaccionAnticipos(TransacctionDTO transacctionDTO) {
        Set<TransaccionAnticipo> transaccionAnticipos = new HashSet<>();
        String feID = transacctionDTO.getFE_Id();
        try {
            transacctionDTO.getTransactionActicipoDTOList().stream().map(transactionActicipoDTO -> {
                TransaccionAnticipo anticipo = new TransaccionAnticipo(new TransaccionAnticipoPK(feID, transactionActicipoDTO.getNroAnticipo()));
                anticipo.setAnticipoMonto(transactionActicipoDTO.getAnticipo_Monto());
                anticipo.setAntiDOCTipo(transactionActicipoDTO.getAntiDOC_Tipo());
                anticipo.setAntiDOCSerieCorrelativo(transactionActicipoDTO.getAntiDOC_Serie_Correlativo());
                anticipo.setDOCNumero(transactionActicipoDTO.getDOC_Numero());
                anticipo.setDOCTipo(transactionActicipoDTO.getDOC_Tipo());
                anticipo.setDOCMoneda(transactionActicipoDTO.getDOC_Moneda());
                transaccionAnticipos.add(anticipo);
                return anticipo;
            }).collect(Collectors.toSet());
        } catch (Exception ex) {
            logger.info("Error en extraerTransaccionAnticipos: " + ex.getMessage());
        }
        //return transaccionAnticipos;
        return new ArrayList<>(transaccionAnticipos);
    }

    public List<TransaccionComprobantePago> ExtraerTransaccionComprobantes(TransacctionDTO transacctionDTO) {
        Set<TransaccionComprobantePago> transaccionComprobantePagos = new HashSet<>();
        String feID = transacctionDTO.getFE_Id();

        try {
            transacctionDTO.getTransactionComprobantesDTOList().stream().map(transactionComprobantesDTO -> {
                TransaccionComprobantePago comprobantePago = new TransaccionComprobantePago(new TransaccionComprobantePagoPK(feID, transactionComprobantesDTO.getNroOrden()));
                try {
                    comprobantePago.setCPFecha(Util.returnDate(transactionComprobantesDTO.getCP_Fecha()));
                } catch (Exception e) {
                    comprobantePago.setCPFecha(new Date());
                }
                try {
                    comprobantePago.setDOCFechaEmision(Util.returnDate(transactionComprobantesDTO.getDOC_FechaEmision()));
                } catch (Exception e) {
                    comprobantePago.setDOCFechaEmision(new Date());
                }
                try {
                    comprobantePago.setPagoFecha(Util.returnDate(transactionComprobantesDTO.getPagoFecha()));
                } catch (Exception e) {
                    comprobantePago.setPagoFecha(new Date());
                }
                try {
                    comprobantePago.setTCFecha(Util.returnDate(transactionComprobantesDTO.getTC_Fecha()));
                } catch (Exception e) {
                    comprobantePago.setTCFecha(new Date());
                }
                comprobantePago.setCPImporte(transactionComprobantesDTO.getCP_Importe());
                comprobantePago.setCPImporteTotal(transactionComprobantesDTO.getCP_ImporteTotal());
                comprobantePago.setCPMoneda(transactionComprobantesDTO.getCP_Moneda());
                comprobantePago.setCPMonedaMontoNeto(transactionComprobantesDTO.getCP_MonedaMontoNeto());

                comprobantePago.setDOCImporte(transactionComprobantesDTO.getDOC_Importe());
                comprobantePago.setDOCMoneda(transactionComprobantesDTO.getDOC_Moneda());
                comprobantePago.setDOCNumero(transactionComprobantesDTO.getDOC_Numero());
                comprobantePago.setDOCTipo(transactionComprobantesDTO.getDOC_Tipo());

                comprobantePago.setPagoImporteSR(transactionComprobantesDTO.getPagoImporteSR());
                comprobantePago.setPagoMoneda(transactionComprobantesDTO.getPagoMoneda());
                comprobantePago.setPagoNumero(transactionComprobantesDTO.getPagoNumero());
                comprobantePago.setTCFactor(transactionComprobantesDTO.getTC_Factor());

                comprobantePago.setTCMonedaObj(transactionComprobantesDTO.getTC_MonedaObj());
                comprobantePago.setTCMonedaRef(transactionComprobantesDTO.getTC_MonedaRef());
                List<Usuariocampos> usuariocampos = usuariocamposRepository.findAll();
                comprobantePago.setTransaccionComprobantepagoUsuarioList(getTransaccionComprobanteCampoUsuario(feID, comprobantePago.getTransaccionComprobantePagoPK().getNroOrden(), transacctionDTO, usuariocampos));
                transaccionComprobantePagos.add(comprobantePago);
                return comprobantePago;
            }).collect(Collectors.toSet());

        } catch (Exception ex) {
            logger.info("Error en ExtraerTransaccionComprobantes");
        }
        //return transaccionComprobantePagos;
        return new ArrayList<>(transaccionComprobantePagos);
    }

    private List<TransaccionComprobantepagoUsuario> getTransaccionComprobanteCampoUsuario(String FE_ID, int nroOrden, TransacctionDTO transacctionDTO, List<Usuariocampos> usuariocampos) {
        List<TransaccionComprobantepagoUsuario> lstComprobantePagoUsuario = new ArrayList<>();

        Gson gson = new Gson();
        String json = gson.toJson(transacctionDTO);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JSONObject jsonValues = new JSONObject(jsonObject.toString());
        JSONObject jsonPlano = Util.flattenJSON(jsonValues);


        String[] keys = JSONObject.getNames(jsonPlano);
        for (String key : keys) {
            Object value = jsonPlano.get(key);
            if (key.startsWith("U_")) {
                String campoElemento = key.substring(2);
                Optional<Usuariocampos> optional = usuariocampos.parallelStream().filter(usuariocampo -> campoElemento.equalsIgnoreCase(usuariocampo.getNombre())).findAny();
                if (optional.isPresent()) {
                    try {
                        Usuariocampos campoUsuario = optional.get();
                        TransaccionComprobantepagoUsuario cu = new TransaccionComprobantepagoUsuario(new TransaccionComprobantepagoUsuarioPK(campoUsuario.getId(), FE_ID, nroOrden));
                        cu.setValor(value.toString());
                        lstComprobantePagoUsuario.add(cu);
                    } catch (Exception e) {
                        System.out.println(" Error " + e.getMessage());
                    }
                }
            }
        }
        return lstComprobantePagoUsuario;
    }

    public List<TransaccionDocrefers> ExtraerTransaccionDocReferes(TransacctionDTO transacctionDTO) {
        Set<TransaccionDocrefers> transaccionDocrefers = new HashSet<>();
        String feID = transacctionDTO.getFE_Id();

        try {
            transacctionDTO.getTransactionDocReferDTOList().stream().map(transactionDocReferDTO -> {
                TransaccionDocrefers docrefers = new TransaccionDocrefers(new TransaccionDocrefersPK(feID, transactionDocReferDTO.getLineId()));
                docrefers.setId(transactionDocReferDTO.getId().toString());
                docrefers.setTipo(transactionDocReferDTO.getTipo());
                transaccionDocrefers.add(docrefers);
                return docrefers;
            }).collect(Collectors.toSet());
        } catch (Exception ex) {
            logger.info("Error en ExtraerTransaccionDocReferes: " + ex.getMessage());
        }
        //return transaccionDocrefers;
        return new ArrayList<>(transaccionDocrefers);
    }

    public List<TransaccionPropiedades> extraerTransaccionPropiedades(TransacctionDTO transacctionDTO) {
        Set<TransaccionPropiedades> transaccionPropiedades = new HashSet<>();
        String feID = transacctionDTO.getFE_Id();
        try {
            transacctionDTO.getTransactionPropertiesDTOList().stream().map(transactionPropertiesDTO -> {
                TransaccionPropiedades propiedad = new TransaccionPropiedades(new TransaccionPropiedadesPK(feID, transactionPropertiesDTO.getId()));
                propiedad.setValor(transactionPropertiesDTO.getValor());
                propiedad.setDescription(transactionPropertiesDTO.getDescription());
                transaccionPropiedades.add(propiedad);
                return propiedad;
            }).collect(Collectors.toSet());
        } catch (Exception ex) {
            logger.info("Error en extraerTransaccionPropiedades: " + ex.getMessage());
        }
        //return transaccionPropiedades;
        return new ArrayList<>(transaccionPropiedades);
    }

    public TransaccionGuiaRemision extraerTransaccionGuias(TransacctionDTO transacctionDTO) {
        String feID = transacctionDTO.getFE_Id();
        TransaccionGuiaRemision transaccionGuiaRemision = new TransaccionGuiaRemision(feID);

        try {
            TransactionGuiasDTO transactionGuiasDTO = transacctionDTO.getTransactionGuias();
            if (Util.esValido(transactionGuiasDTO.getCodigoMotivo())) {
                transaccionGuiaRemision.setCodigoMotivo(transactionGuiasDTO.getCodigoMotivo());
                transaccionGuiaRemision.setCodigoPuerto(transactionGuiasDTO.getCodigoPuerto());
                transaccionGuiaRemision.setDescripcionMotivo(transactionGuiasDTO.getDescripcionMotivo());
                transaccionGuiaRemision.setDocumentoConductor(transactionGuiasDTO.getDocumentoConductor());
                transaccionGuiaRemision.setLicenciaConducir(transactionGuiasDTO.getLicenciaConductor());
                transaccionGuiaRemision.setFechaInicioTraslado(Util.returnDate(transactionGuiasDTO.getFechaInicioTraslado()));
                transaccionGuiaRemision.setIndicadorTransbordoProgramado(transactionGuiasDTO.getIndicadorTransbordoProgramado());
                transaccionGuiaRemision.setModalidadTraslado(transactionGuiasDTO.getModalidadTraslado());
                transaccionGuiaRemision.setNombreRazonTransportista(transactionGuiasDTO.getNombreRazonTransportista());
                transaccionGuiaRemision.setNumeroBultos(transactionGuiasDTO.getNumeroBultos());
                transaccionGuiaRemision.setNumeroContenedor(transactionGuiasDTO.getNumeroContenedor());
                transaccionGuiaRemision.setPeso(transactionGuiasDTO.getPeso());
                transaccionGuiaRemision.setPlacaVehiculo(transactionGuiasDTO.getPlacaVehiculo());
                transaccionGuiaRemision.setRUCTransporista(transactionGuiasDTO.getRUCTransporista());
                transaccionGuiaRemision.setTipoDOCTransportista(transactionGuiasDTO.getTipoDOCTransportista());
                transaccionGuiaRemision.setTipoDocConductor(transactionGuiasDTO.getTipoDocConductor());
                transaccionGuiaRemision.setUnidadMedida(transactionGuiasDTO.getUnidadMedida());
                transaccionGuiaRemision.setDireccionPartida(transactionGuiasDTO.getDireccionLlegada());
                transaccionGuiaRemision.setUbigeoPartida(transactionGuiasDTO.getUbigeoPartida());

                //GUIAS REMISION REST

                transaccionGuiaRemision.setTipoDocRelacionadoTrans(transactionGuiasDTO.getTipoDocRelacionadoTrans());
                transaccionGuiaRemision.setTipoDocRelacionadoTransDesc(transactionGuiasDTO.getTipoDocRelacionadoTransDesc());
                transaccionGuiaRemision.setDocumentoRelacionadoTrans(transactionGuiasDTO.getDocumentoRelacionadoTrans());
                transaccionGuiaRemision.setIndicadorTransbordo(transactionGuiasDTO.getIndicadorTransbordo());
                transaccionGuiaRemision.setIndicadorTraslado(transactionGuiasDTO.getIndicadorTraslado());
                transaccionGuiaRemision.setIndicadorRetorno(transactionGuiasDTO.getIndicadorRetorno());
                transaccionGuiaRemision.setIndicadorRetornoVehiculo(transactionGuiasDTO.getIndicadorRetornoVehiculo());
                transaccionGuiaRemision.setIndicadorTrasladoTotal(transactionGuiasDTO.getIndicadorTrasladoTotal());
                transaccionGuiaRemision.setIndicadorRegistro(transactionGuiasDTO.getIndicadorRegistro());
                transaccionGuiaRemision.setNroRegistroMTC(transactionGuiasDTO.getNroRegistroMTC());
                transaccionGuiaRemision.setNombreApellidosConductor(transactionGuiasDTO.getNombreApellidosConductor());
                transaccionGuiaRemision.setUbigeoLlegada(transactionGuiasDTO.getUbigeoLlegada());
                transaccionGuiaRemision.setDireccionLlegada(transactionGuiasDTO.getDireccionLlegada());
                transaccionGuiaRemision.setTarjetaCirculacion(transactionGuiasDTO.getTarjetaCirculacion());
                transaccionGuiaRemision.setNumeroPrecinto(transactionGuiasDTO.getNumeroPrecinto());
                transaccionGuiaRemision.setNumeroContenedor2(transactionGuiasDTO.getNumeroContenedor2());
                transaccionGuiaRemision.setNumeroPrecinto2(transactionGuiasDTO.getNumeroPrecinto2());
                transaccionGuiaRemision.setDescripcionPuerto(transactionGuiasDTO.getDescripcionPuerto());
                transaccionGuiaRemision.setCodigoAereopuerto(transactionGuiasDTO.getCodigoAereopuerto());
                transaccionGuiaRemision.setDescripcionAereopuerto(transactionGuiasDTO.getDescripcionAereopuerto());
                transaccionGuiaRemision.setTicketRest(transactionGuiasDTO.getTicketRest());
            }
        } catch (Exception e) {
            logger.info("Error en extraerTransaccionGuias");
        }

        return transaccionGuiaRemision;
    }

    public List<TransaccionTotales> extraerTransaccionTotales(TransacctionDTO transacctionDTO) {
        Set<TransaccionTotales> transaccionTotalesSet = new HashSet<>();
        try {
            String feId = transacctionDTO.getFE_Id();
            transacctionDTO.getTransactionTotalesDTOList().stream().map(totales -> {
                TransaccionTotales transaccionTotales = new TransaccionTotales(new TransaccionTotalesPK(feId, totales.getId()));
                transaccionTotales.setMonto(totales.getMonto());
                transaccionTotales.setPrcnt(totales.getPrcnt());
                transaccionTotalesSet.add(transaccionTotales);
                return transaccionTotales;
            }).collect(Collectors.toSet());
        } catch (Exception ex) {
            logger.info("Error en extraerTransaccionTotales : " + ex.getMessage());
        }
        //return transaccionTotalesSet;
        return new ArrayList<>(transaccionTotalesSet);
    }

    public List<TransaccionContractdocref> extraerTransaccionContractDocRefs(TransacctionDTO transacctionDTO) {
        Set<TransaccionContractdocref> docrefs = new HashSet<>();
        String feId = transacctionDTO.getFE_Id();
        try {
            transacctionDTO.getTransactionContractDocRefListDTOS().stream().map(contractMap -> {
                List<Usuariocampos> usuariocampos = usuariocamposRepository.findAll();
                List<Map<String, String>> listaDocRef = transacctionDTO.getTransactionContractDocRefListDTOS();
                Optional.ofNullable(listaDocRef).ifPresent(listMap -> docrefs.addAll(getTransaccionContractdocref(listMap.get(0), usuariocamposRepository, usuariocampos, feId)));
                return contractMap;
            }).collect(Collectors.toSet());
        } catch (Exception e) {
            logger.info("Error en extraerTransaccionContractDocRefs : " + e.getMessage());
        }
        return new ArrayList<>(docrefs);
    }

    public List<TransaccionContractdocref> getTransaccionContractdocref(Map<String, String> mapContract, UsuariocamposRepository repository, List<Usuariocampos> usucamposList, String feId) {
        Set<TransaccionContractdocref> transaccionContractdocrefs = new HashSet<>();

        mapContract.entrySet().stream().forEach(entry -> {
            if (entry.getValue() != null) {
                Optional<Usuariocampos> optional = usucamposList.parallelStream().filter(transaccionUsucampo -> entry.getKey().equalsIgnoreCase(transaccionUsucampo.getNombre())).findAny();
                Integer id;
                if (optional.isPresent()) {
                    Usuariocampos usuariocampos = optional.get();
                    id = usuariocampos.getId();
                    TransaccionContractdocref docref = new TransaccionContractdocref(new TransaccionContractdocrefPK(feId, id));
                    docref.setUsuariocampos(usuariocampos);
                    docref.setValor(entry.getValue().toString());
                    transaccionContractdocrefs.add(docref);
                } else {
                    Usuariocampos usuariocampo = new Usuariocampos();
                    usuariocampo.setNombre(entry.getKey());
                    repository.saveAndFlush(usuariocampo);
                    id = usuariocampo.getId();
                    usucamposList.add(usuariocampo);
                    TransaccionContractdocref docref = new TransaccionContractdocref(new TransaccionContractdocrefPK(feId, id));
                    docref.setUsuariocampos(usuariocampo);
                    docref.setValor(entry.getValue().toString());
                    transaccionContractdocrefs.add(docref);
                }
            }
        });
        return new ArrayList<>(transaccionContractdocrefs);
    }

    public List<TransaccionCuotas> extraerTranssacionCuotas(TransacctionDTO transacctionDTO) {
        Set<TransaccionCuotas> lineasCuotas = new HashSet<>();
        String feID = transacctionDTO.getFE_Id();
        try {

            transacctionDTO.getTransactionCuotasDTOList().stream().map(transactionCuotasDTO -> {
                TransaccionCuotas cuotas = new TransaccionCuotas(new TransaccionCuotasPK(feID, transactionCuotasDTO.getNroOrden()));
                cuotas.setCuota(transactionCuotasDTO.getCuota());
                cuotas.setFechaCuota(Util.returnDate(transactionCuotasDTO.getFechaCuota()));
                cuotas.setMontoCuota(transactionCuotasDTO.getMontoCuota());
                cuotas.setFechaEmision(Util.returnDate(transactionCuotasDTO.getFechaEmision()));
                cuotas.setFormaPago(transactionCuotasDTO.getFormaPago());
                lineasCuotas.add(cuotas);
                return cuotas;
            }).collect(Collectors.toSet());

        } catch (Exception e) {
            logger.info("Error en extraerTranssacionCuotas");
        }
        return new ArrayList<>(lineasCuotas);
    }

    public List<TransaccionLineas> extraerTransaccionLineas(TransacctionDTO transacctionDTO, Transaccion transaccion) {
        Set<TransaccionLineas> transaccionLineas2 = new HashSet<>();

        crearCamposUsuario(transacctionDTO.getTransactionLineasDTOList().get(0).getTransaccionLineasCamposUsuario());

        String feID = transacctionDTO.getFE_Id();
        try {
            transacctionDTO.getTransactionLineasDTOList().stream().map(transactionLineasDTO -> {
                TransaccionLineas linea = new TransaccionLineas(new TransaccionLineasPK(feID, transactionLineasDTO.getNroOrden()));
                linea.setCantidad(transactionLineasDTO.getCantidad());
                linea.setDSCTOMonto(transactionLineasDTO.getDSCTO_Monto());
                linea.setDSCTOPorcentaje(transactionLineasDTO.getDSCTO_Porcentaje());
                linea.setDescripcion(transactionLineasDTO.getDescripcion());
                linea.setPrecioDscto(transactionLineasDTO.getPrecioDscto());
                linea.setPrecioIGV(transactionLineasDTO.getPrecioIGV());
                linea.setTotalLineaSinIGV(transactionLineasDTO.getTotalLineaSinIGV());
                linea.setTotalLineaConIGV(transactionLineasDTO.getTotalLineaConIGV());
                linea.setPrecioRefCodigo(transactionLineasDTO.getPrecioRef_Codigo());
                linea.setPrecioRefMonto(transactionLineasDTO.getPrecioRef_Monto());
                linea.setUnidad(transactionLineasDTO.getUnidad());
                linea.setUnidadSunat(transactionLineasDTO.getUnidadSunat());
                linea.setCodArticulo(transactionLineasDTO.getCodArticulo());
                linea.setCodSunat(transactionLineasDTO.getCodSunat());
                linea.setTotalBruto(transactionLineasDTO.getTotalBruto());
                linea.setLineaImpuesto(transactionLineasDTO.getLineaImpuesto());
                linea.setCodProdGS1(transactionLineasDTO.getCodProdGS1());
                linea.setCodUbigeoOrigen(transactionLineasDTO.getCodUbigeoOrigen());
                linea.setDirecOrigen(transactionLineasDTO.getDirecOrigen());
                linea.setCodUbigeoDestino(transactionLineasDTO.getCodUbigeoDestino());
                linea.setDirecDestino(transactionLineasDTO.getDirecDestino());
                linea.setDetalleViaje(transactionLineasDTO.getDetalleViaje());
                linea.setValorTransporte(transactionLineasDTO.getValorTransporte());
                linea.setValorCargaEfectiva(transactionLineasDTO.getValorCargaEfectiva());
                linea.setValorCargaUtil(transactionLineasDTO.getValorCargaUtil());
                linea.setConfVehicular(transactionLineasDTO.getConfVehicular());
                linea.setCUtilVehiculo(transactionLineasDTO.getCUtilVehiculo());
                linea.setCEfectivaVehiculo(transactionLineasDTO.getCEfectivaVehiculo());
                linea.setValorRefTM(transactionLineasDTO.getValorRefTM());
                linea.setValorPreRef(transactionLineasDTO.getValorPreRef());
                linea.setFactorRetorno(transactionLineasDTO.getFactorRetorno());

                linea.setNombreEmbarcacion(transactionLineasDTO.getNombreEmbarcacion());
                linea.setTipoEspeciaVendida(transactionLineasDTO.getTipoEspecieVendida());
                linea.setLugarDescarga(transactionLineasDTO.getLugarDescarga());
                linea.setFechaDescarga(transactionLineasDTO.getFechaDescarga());
                linea.setCantidadEspecieVendida(transactionLineasDTO.getCantidadEspecieVendida());

                //GUIAS REST API SUNAT
                linea.setSubPartida(transactionLineasDTO.getSubPartida());
                linea.setIndicadorBien(transactionLineasDTO.getIndicadorBien());
                linea.setNumeracion(transactionLineasDTO.getNumeracion());
                linea.setNumeroSerie(transactionLineasDTO.getNumeroSerie());
                try {
                    Optional<Character> itmBolsaOptional = Optional.ofNullable(transactionLineasDTO.getItmBolsa()).map(s -> s.isEmpty() ? null : s.charAt(0));
                    linea.setItemBolsa(itmBolsaOptional.orElse('N'));
                } catch (Exception e) {
                    logger.info("No se encuentra el Item de Bolsa.");
                    linea.setItemBolsa('N');
                }
                Integer resultado = (transactionLineasDTO.getNroOrden() - 1);
                linea.setTransaccionLineaImpuestosList(extraerTransaccionLineasImpuestos(transactionLineasDTO.getTransactionLineasImpuestoListDTO(), linea.getTransaccionLineasPK().getNroOrden(), feID));
                List<Usuariocampos> usuariocampos = usuariocamposRepository.findAll();

                Map<String, String> listaCamposUsuarioLinea = transactionLineasDTO.getTransaccionLineasCamposUsuario();
                List<TransaccionLineasUsucampos> camposUsuario = getTransaccionLineaCamposUsuarios(feID, linea.getTransaccionLineasPK().getNroOrden(), listaCamposUsuarioLinea, usuariocampos);
                linea.setTransaccionLineasUsucamposList(camposUsuario);
                linea.setTransaccionLineasBillrefList(extraerTransaccionLineasBillRefs(transactionLineasDTO, linea.getTransaccionLineasPK().getNroOrden(), feID));
                descuentoTotal = descuentoTotal.add((transactionLineasDTO.getDSCTO_Monto()));
                linea.setTransaccion(transaccion);
                transaccionLineas2.add(linea);

                return linea;
            }).collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>(transaccionLineas2);
    }

    private List<TransaccionLineasBillref> extraerTransaccionLineasBillRefs(TransactionLineasDTO transacctionDTO, Integer nroOrden, String feID) {
        Set<TransaccionLineasBillref> transactionLineasBillRefDTOS = new HashSet<>();
        try {
            transacctionDTO.getTransaccionLineasBillrefListDTO().stream().map(transactionLineasBillRefDTO -> {
                TransaccionLineasBillref billref = new TransaccionLineasBillref(new TransaccionLineasBillrefPK(feID, nroOrden, transactionLineasBillRefDTO.getLineId()));
                billref.setAdtDocRefId(transactionLineasBillRefDTO.getAdtDocRef_Id());
                billref.setAdtDocRefSchemaId(transactionLineasBillRefDTO.getAdtDocRef_SchemaId());
                billref.setInvDocRefDocTypeCode(transactionLineasBillRefDTO.getInvDocRef_DocTypeCode());
                billref.setInvDocRefId(transactionLineasBillRefDTO.getInvDocRef_Id());
                transactionLineasBillRefDTOS.add(billref);
                return billref;
            }).collect(Collectors.toSet());
        } catch (Exception e) {
            logger.info("error en extraerTransaccionLineasBillRefs: " + e.getMessage());
        }
        return new ArrayList<>(transactionLineasBillRefDTOS);

    }


    /**
     * metodo que recibe un json plano de todos los campos para recorrer y filtrar por los campos de usuario
     */
    public List<TransaccionLineasUsucampos> getTransaccionLineaCamposUsuarios(String FE_ID, int nroorden, Map<String, String> mapCamposUsuario, List<Usuariocampos> usuariocampos) {
        final Set<TransaccionLineasUsucampos> camposUsuario = new HashSet<>();

        mapCamposUsuario.entrySet().stream().forEach(objectMap -> {
            if (objectMap.getKey().startsWith("U_")) {
                String finalKey = objectMap.getKey().substring(2);
                Optional<Usuariocampos> optional = usuariocampos.parallelStream().filter(usuariocampo -> finalKey.equalsIgnoreCase(usuariocampo.getNombre())).findAny();
                optional.ifPresent(usuariocampo -> {
                    try {
                        TransaccionLineasUsucampos cu = new TransaccionLineasUsucampos(new TransaccionLineasUsucamposPK(FE_ID, nroorden, usuariocampo.getId()));
                        cu.setValor(objectMap.getValue().toString());
                        camposUsuario.add(cu);
                        cu.setUsuariocampos(usuariocampo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {

            }
        });
        return new ArrayList<>(camposUsuario);
    }

    private List<TransaccionLineaImpuestos> extraerTransaccionLineasImpuestos(List<TransactionLineasImpuestoDTO> transactionLineasImpuestoDTOS, Integer nroOrden, String feID) {
        Set<TransaccionLineaImpuestos> transaccionLineaImpuestosSet = new HashSet<>();

        if (!Objects.isNull(transactionLineasImpuestoDTOS)) {
            transactionLineasImpuestoDTOS.stream().map(lineaImpuestosDTO -> {
                TransaccionLineaImpuestos impuesto = new TransaccionLineaImpuestos(new TransaccionLineaImpuestosPK(feID, nroOrden, lineaImpuestosDTO.getLineId()));
                impuesto.setMoneda(lineaImpuestosDTO.getMoneda());
                impuesto.setMonto(lineaImpuestosDTO.getMonto());
                impuesto.setPorcentaje(lineaImpuestosDTO.getPorcentaje());
                impuesto.setTipoTributo(Optional.ofNullable(lineaImpuestosDTO.getTipoTributo()).orElse(""));
                impuesto.setTipoAfectacion(Optional.ofNullable(lineaImpuestosDTO.getTipoAfectacion()).orElse(""));
                impuesto.setTierRange(lineaImpuestosDTO.getTierRange());
                impuesto.setAbreviatura(Optional.ofNullable(lineaImpuestosDTO.getAbreviatura()).orElse(""));
                impuesto.setCodigo(Optional.ofNullable(lineaImpuestosDTO.getCodigo()).orElse(""));
                impuesto.setValorVenta(lineaImpuestosDTO.getValorVenta());
                impuesto.setNombre(Optional.ofNullable(lineaImpuestosDTO.getNombre()).orElse(""));
                transaccionLineaImpuestosSet.add(impuesto);
                return impuesto;
            }).collect(Collectors.toSet());
        }
        return new ArrayList<>(transaccionLineaImpuestosSet);
    }

    public List<TransaccionImpuestos> extraerTransaccionImpuestos(TransacctionDTO transacctionDTO) {
        Set<TransaccionImpuestos> impuestos = new HashSet<>();
        try {
            impuestos = transacctionDTO.getTransactionImpuestosDTOList().stream().map(transactionImpuestosDTO -> {
                TransaccionImpuestos transaccionImpuestos = new TransaccionImpuestos(new TransaccionImpuestosPK(transacctionDTO.getFE_Id(), transactionImpuestosDTO.getLineId()));
                transaccionImpuestos.setMoneda(transactionImpuestosDTO.getMoneda());
                transaccionImpuestos.setMonto(transactionImpuestosDTO.getMonto());
                transaccionImpuestos.setPorcentaje(transactionImpuestosDTO.getPorcentaje());
                transaccionImpuestos.setTipoTributo(Optional.ofNullable(transactionImpuestosDTO.getTipoTributo()).orElse(""));
                transaccionImpuestos.setTipoAfectacion(transactionImpuestosDTO.getTipoAfectacion());
                transaccionImpuestos.setAbreviatura(Optional.ofNullable(transactionImpuestosDTO.getAbreviatura()).orElse(""));
                transaccionImpuestos.setCodigo(Optional.ofNullable(transactionImpuestosDTO.getCodigo()).orElse(""));
                transaccionImpuestos.setValorVenta(transactionImpuestosDTO.getValorVenta());
                transaccionImpuestos.setNombre(Optional.ofNullable(transactionImpuestosDTO.getNombre()).orElse(""));
                transaccionImpuestos.setTierRange(transactionImpuestosDTO.getTierRange());
                return transaccionImpuestos;
            }).collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>(impuestos);
    }


    private void crearCamposUsuario(Map<String, String> mapObject) {


        mapObject.entrySet().stream().forEach(entry -> {
            if (entry.getKey().startsWith("U_")) {
                Optional<Usuariocampos> optional = usuariocamposRepository.findByNombre(entry.getKey().substring(2));
                if (!optional.isPresent()) {
                    Usuariocampos uc = new Usuariocampos();
                    uc.setNombre(entry.getKey().substring(2));
                    try {
                        usuariocamposRepository.saveAndFlush(uc);
                    } catch (Exception ex) {
                        throw new VenturaExcepcion("No fue posible crear el campo: " + entry.getKey() + ". " + ex.getMessage());
                    }
                }
            }
        });
    }


    private void processJsonObject(JsonObject jsonObjects) {
        JSONObject jsonObject = new JSONObject(jsonObjects.toString());
        JSONObject jsonPlano = Util.flattenJSON(jsonObject);
        String[] keys = JSONObject.getNames(jsonPlano);

        try {
            //Si existen campos que inician con U_
            for (String key : keys) {
                if (key.startsWith("U_")) {
                    key = key.substring(2);
                    Optional<Usuariocampos> optional = usuariocamposRepository.findByNombre(key);
                    if (!optional.isPresent()) {
                        Usuariocampos uc = new Usuariocampos();
                        uc.setNombre(key);
                        try {
                            usuariocamposRepository.saveAndFlush(uc);
                        } catch (Exception ex) {
                            throw new VenturaExcepcion("No fue posible crear el campo: " + key + ". " + ex.getMessage());
                        }
                    }
                }
            }
            //Propiedades de configuracion
            for (TransaccionUsucampos tuc : Transaccion.propiedades) {
                String ncampo = tuc.getUsuariocampos().getNombre();
                Optional<Usuariocampos> optional = usuariocamposRepository.findByNombre(ncampo);
                if (optional.isPresent()) {
                    try {
                        usuariocamposRepository.saveAndFlush(tuc.getUsuariocampos());
                    } catch (Exception ex) {
                        throw new VenturaExcepcion("No fue posible crear el campo: " + ncampo + ". " + ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new VenturaExcepcion(ex.getMessage());
        }
    }

}
