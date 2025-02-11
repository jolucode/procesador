package service.cloud.request.clientRequest.service.core;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ProviderProperties;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.entity.TransaccionTotales;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.handler.PDFGenerateHandler;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentFormatImpl implements DocumentFormatInterface {


  Logger logger = LoggerFactory.getLogger(DocumentFormatImpl.class);

  private final String docUUID = Constants.DOC_UUID;
  ;

  @Autowired
  ProviderProperties providerProperties;

  public String rutaRecursoPdf(String ruc, String typeDocument) {
    String rutaPDF = providerProperties.getRutaBaseDoc() + ruc + File.separator + "formatos" + File.separator + typeDocument;
    return rutaPDF;
  }

  @Override
  public byte[] createPDFDocument(UBLDocumentWRP wrp, Transaccion transaction, ConfigData configuracion) {
    if (logger.isDebugEnabled()) {
      logger.debug("+createPDFDocument() [" + this.docUUID + "]");
    }
    byte[] pdfBytes = null;
    List<TransaccionTotales> transaccionTotales = new ArrayList<>(transaction.getTransaccionTotalesList());
    try {
      // Crear una nueva instancia de PDFGenerateHandler para cada llamada
      PDFGenerateHandler pdfHandler = PDFGenerateHandler.newInstance(this.docUUID);

      String logoSociedad = providerProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + "COMPANY_LOGO.jpg";
      String rutaPaymentSelected = rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "InvoiceDocumentPaymentDetail.jasper");

      String personalizacion = transaction.getTransaccionContractdocrefList().stream()
              .filter(x -> x.getUsuariocampos().getNombre().equals("pdf_per"))
              .map(x -> x.getValor())
              .findFirst()
              .orElse("");


      String documentName;
      switch (transaction.getDOC_Codigo()) {
        case IUBLConfig.DOC_INVOICE_CODE:
          if (!personalizacion.isEmpty()) {
            documentName = "invoiceDocument_" + personalizacion + ".jrxml";
          }else {
            documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "invoiceDocument_Ing.jrxml" : "invoiceDocument.jrxml";
          }

          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generateInvoicePDF(wrp, configuracion);
          break;

        case IUBLConfig.DOC_BOLETA_CODE:
          documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "boletaDocument_Ing.jrxml" : "boletaDocument.jrxml";
          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generateBoletaPDF(wrp, configuracion);
          break;

        case IUBLConfig.DOC_CREDIT_NOTE_CODE:
          documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "creditNoteDocument_Ing.jrxml" : "creditNoteDocument.jrxml";
          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generateCreditNotePDF(wrp, transaccionTotales, configuracion);
          break;

        case IUBLConfig.DOC_DEBIT_NOTE_CODE:
          documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "debitNoteDocument_Ing.jrxml" : "debitNoteDocument.jrxml";
          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generateDebitNotePDF(wrp, transaccionTotales, configuracion);
          break;

        case IUBLConfig.DOC_PERCEPTION_CODE:
          pdfHandler.setConfiguration(providerProperties.getRutaBaseDoc() + File.separator + "perceptionDocument.jrxml", rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generatePerceptionPDF(wrp, configuracion); // Comentado porque no estaba implementado
          break;

        case IUBLConfig.DOC_RETENTION_CODE:
          documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "retentionDocument_Ing.jrxml" : "retentionDocument.jrxml";
          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, "EmisorElectronico");
          pdfBytes = pdfHandler.generateRetentionPDF(wrp, configuracion);
          break;

        case IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE:
          documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "remissionguideDocument_Ing.jrxml" : "remissionguideDocument.jrxml";
          pdfHandler.setConfiguration(rutaRecursoPdf(transaction.getDocIdentidad_Nro(), documentName), rutaRecursoPdf(transaction.getDocIdentidad_Nro(), "legendReport.jasper"), rutaPaymentSelected, logoSociedad, providerProperties.getClientProperties(transaction.getDocIdentidad_Nro()).getEmisorElecRs());
          pdfBytes = pdfHandler.generateDespatchAdvicePDF(wrp, configuracion);
          break;

        default:
          logger.error("createPDFDocument() [" + this.docUUID + "] " + IVenturaError.ERROR_460.getMessage());
          throw new ConfigurationException(IVenturaError.ERROR_460.getMessage());
      }
    } catch (PDFReportException e) {
      logger.error("createPDFDocument() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
      logger.error("createPDFDocument() [" + this.docUUID + "] PDFReportException -->" + ExceptionUtils.getStackTrace(e));
    } catch (ConfigurationException e) {
      logger.error("createPDFDocument() [" + this.docUUID + "] ConfigurationException - ERROR: " + e.getMessage());
      logger.error("createPDFDocument() [" + this.docUUID + "] ConfigurationException -->" + ExceptionUtils.getStackTrace(e));
    } catch (Exception e) {
      logger.error("createPDFDocument() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") ERROR: " + e.getMessage());
      logger.error("createPDFDocument() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
    }
    if (logger.isDebugEnabled()) {
      logger.debug("-createPDFDocument() [" + this.docUUID + "]");
    }
    return pdfBytes;
  }

  //createPDFDocument}

  @Override
  public Optional<byte[]> unzipResponse(byte[] cdr) throws IOException {
    ByteArrayInputStream bais = new ByteArrayInputStream(cdr);
    ZipInputStream zis = new ZipInputStream(bais);
    ZipEntry entry = zis.getNextEntry();
    byte[] xml = null;
    if (entry != null) { // valida dos veces lo mismo
      while (entry != null) {
        if (!entry.isDirectory()) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          byte[] bytesIn = new byte['?'];
          int read;
          while ((read = zis.read(bytesIn)) != -1) {
            baos.write(bytesIn, 0, read);
          }
          baos.close();
          xml = baos.toByteArray();
        }
        zis.closeEntry();
        entry = zis.getNextEntry();
      }
      zis.close();
      return Optional.ofNullable(xml);
    } else {
      zis.close();
      return Optional.empty();
    }
  }
}
