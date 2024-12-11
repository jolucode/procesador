package service.cloud.request.clientRequest.handler.document;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.utils.exception.SignerDocumentException;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Este clase HANDLER contiene metodos para realizar el firmado electronico
 * de los documentos UBL validos por Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class SignerHandler {

    private final Logger logger = Logger.getLogger(SignerHandler.class);

    /* Identicador del objeto */
    private String identifier;

    /* Configuracion del certificado */
    private byte[] certificate;

    private String password;

    private String keystoreType;

    private String keystoreProvider;

    private String signerName;

    /* Informacion confidencial del certificado */
    private X509Certificate x509Certificate;

    private PrivateKey privateKey;

    private PublicKey publicKey;


    /**
     * Constructor privado basico para evitar la creacion de
     * instancias usando el constructor.
     */
    private SignerHandler() {
    }

    /**
     * Constructor privado para evitar la creacion de instancias
     * usando el constructor.
     *
     * @param identifier Identificador del objeto SignerHandler.
     */
    private SignerHandler(String identifier) {
        this.identifier = identifier;
    } //SignerHandler

    /**
     * Este metodo crea una nueva instancia de la clase SignerHandler.
     *
     * @return Retorna una nueva instancia de la clase SignerHandler.
     */
    public static synchronized SignerHandler newInstance() {
        return new SignerHandler();
    } //newInstance

    /**
     * Este metodo crea una nueva instancia de la clase SignerHandler.
     *
     * @param identifier Identificador del objeto SignerHandler.
     * @return Retorna una nueva instancia de la clase SignerHandler.
     */
    public static synchronized SignerHandler newInstance(String identifier) {
        return new SignerHandler(identifier);
    } //newInstance


    /**
     * Este metodo guarda la configuracion del objeto SignerHandler. Guarda el
     * certificado digital, la contrasenia del certificado digital y el nombre
     * del firmante en memoria.
     * <p>
     * El nombre del firmante tiene que ser el mismo que esta en el documento UBL
     * en el TAG SignatureType.
     *
     * @param certificate    El certificado digital en bytes.
     * @param certificatePwd La contrasenia del certificado digital.
     * @param signerName     El nombre del firmante del certificado digital.
     */
    public void setConfiguration(byte[] certificate, String certificatePwd, String keystoreType, String keystoreProvider, String signerName)
            throws SignerDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+setConfiguration()" + (null != this.identifier ? " [" + this.identifier + "]" : ""));
        }
        this.certificate = certificate;
        this.password = certificatePwd;
        this.keystoreType = keystoreType;
        this.keystoreProvider = keystoreProvider;
        this.signerName = signerName;

        /* Cargando el certificado digital */
        loadDigitalCertificate();
        if (logger.isDebugEnabled()) {
            logger.debug("-setConfiguration()" + (null != this.identifier ? " [" + this.identifier + "]" : ""));
        }
    } //setConfiguration


    /**
     * Este metodo firma el documento UBL especificado en la ruta 'documentPath' y
     * retorna un objeto File con la nueva ruta del documento UBL firmado.
     *
     * @param documentPath La ruta del documento UBL sin firmar.
     * @param docUUID      UUID del documento correspondiente al registro del objeto Homologationdocument.
     * @return Retorna un objeto File con la ruta del documento UBL firmado.
     * @throws SignerDocumentException
     */
    @SuppressWarnings("unused")
    public File signDocument(String documentPath, String docUUID) throws SignerDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+signDocument()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "]");
        }
        File outputFile = null;

        try {
            outputFile = new File(documentPath);
            if (!outputFile.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_254.getMessage());
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(outputFile);

            /* Creates a DOMSignContext object */
            DOMSignContext signContext = new DOMSignContext(privateKey, getElementPositionToSign(document, docUUID));
            signContext.putNamespacePrefix(XMLSignature.XMLNS, ISignerConfig.SIGN_CONTEXT_NAMESPACE_PREFIX);

            /* Assembles the XML signature */
            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance(ISignerConfig.SIGNATURE_FACTORY_MECHANISM);

            Reference reference = signatureFactory.newReference("", signatureFactory.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA1,
                    null), Collections.singletonList(signatureFactory.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                    (TransformParameterSpec) null)), null, null);

            SignedInfo signedInfo = signatureFactory.newSignedInfo(signatureFactory.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null), signatureFactory.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(reference));

            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
            KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

            List<Object> x509DataContent = new java.util.ArrayList<Object>();

            /* DESCOMENTAR ESTO CUANDO TENGA QUE APARECER LA INFORMACION DEL SUBJECT
            x509DataContent.add(x509Certificate.getSubjectX500Principal().getName()); */
            x509DataContent.add(x509Certificate);
            X509Data x509Data = keyInfoFactory.newX509Data(x509DataContent);

            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
            XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, keyInfo, null, signerName, null);

            /* Generates the XML signature */
            xmlSignature.sign(signContext);

            /* Records the XML signature */
            OutputStream os = new FileOutputStream(outputFile);

            /* Creates the TransformerFactory */
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, ISignerConfig.XML_TRANSFORM_KEYS_ENCODING);
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, ISignerConfig.XML_TRANSFORM_KEYS_METHOD);
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, ISignerConfig.XML_TRANSFORM_KEYS_INDENT);
            transformer.transform(new javax.xml.transform.dom.DOMSource(document), new javax.xml.transform.stream.StreamResult(os));

            logger.info("signDocument()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "] Documento UBL firmado correctamente.");

        } catch (Exception e) {
            logger.error("-signDocument()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "] ERROR: " + e.getMessage());
            logger.error("-signDocument()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new SignerDocumentException(IVenturaError.ERROR_255);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-signDocument()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "]");
        }
        return outputFile;
    } //signDocument

    public byte[] signDocumentv2(byte[] xmlDocument, String docUUID) throws SignerDocumentException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(xmlDocument));

            /* Creates a DOMSignContext object */
            DOMSignContext signContext = new DOMSignContext(privateKey, getElementPositionToSign(document, docUUID));
            signContext.putNamespacePrefix(XMLSignature.XMLNS, ISignerConfig.SIGN_CONTEXT_NAMESPACE_PREFIX);

            /* Assembles the XML signature */
            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance(ISignerConfig.SIGNATURE_FACTORY_MECHANISM);

            Reference reference = signatureFactory.newReference("", signatureFactory.newDigestMethod(javax.xml.crypto.dsig.DigestMethod.SHA1,
                null), Collections.singletonList(signatureFactory.newTransform(javax.xml.crypto.dsig.Transform.ENVELOPED,
                (TransformParameterSpec) null)), null, null);

            SignedInfo signedInfo = signatureFactory.newSignedInfo(signatureFactory.newCanonicalizationMethod(javax.xml.crypto.dsig.CanonicalizationMethod.INCLUSIVE,
                    (C14NMethodParameterSpec) null), signatureFactory.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(reference));

            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
            KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

            List<Object> x509DataContent = new java.util.ArrayList<Object>();
            x509DataContent.add(x509Certificate);
            X509Data x509Data = keyInfoFactory.newX509Data(x509DataContent);

            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
            XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, keyInfo, null, signerName, null);

            /* Generates the XML signature */
            xmlSignature.sign(signContext);

            /* Records the XML signature */
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            /* Creates the TransformerFactory */
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, ISignerConfig.XML_TRANSFORM_KEYS_ENCODING);
            transformer.setOutputProperty(OutputKeys.METHOD, ISignerConfig.XML_TRANSFORM_KEYS_METHOD);
            transformer.setOutputProperty(OutputKeys.INDENT, ISignerConfig.XML_TRANSFORM_KEYS_INDENT);
            transformer.transform(new DOMSource(document), new StreamResult(os));

            return os.toByteArray();
        } catch (Exception e) {
            throw new SignerDocumentException(IVenturaError.ERROR_255);
        }
    }

    /**
     * Este metodo carga el certificado digital, verificando que la contrasenia
     * coincida con el certificado.
     *
     * @throws SignerDocumentException
     */
    private void loadDigitalCertificate() throws SignerDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+loadDigitalCertificate()" + (null != this.identifier ? " [" + this.identifier + "]" : ""));
        }
        try {
            /* Cargando el certificado */
            KeyStore keyStore = KeyStore.getInstance(keystoreType, keystoreProvider);
            keyStore.load(new ByteArrayInputStream(this.certificate), this.password.toCharArray());

            /* Obtiene el ALIAS del certificado */
            String alias = getAliasFromCertificate(keyStore);

            PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) keyStore.getEntry(alias,
                    new KeyStore.PasswordProtection(password.toCharArray()));

            /*
             * Extraer el certificado digital en formato X509, la llave privada
             * y la llave publica.
             */
            x509Certificate = (X509Certificate) privateKeyEntry.getCertificate();
            privateKey = privateKeyEntry.getPrivateKey();
            publicKey = x509Certificate.getPublicKey();
        } catch (Exception e) {
            logger.error("getAliasFromCertificate()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " ERROR: " + IVenturaError.ERROR_253.getMessage());
            throw new SignerDocumentException(IVenturaError.ERROR_253);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-loadDigitalCertificate()" + (null != this.identifier ? " [" + this.identifier + "]" : ""));
        }
    } //loadDigitalCertificate

    /**
     * Este metodo retorna el ALIAS del certificado digital, extrayendolo del KEYSTORE.
     *
     * @param keyStore El objeto KeyStore.
     * @return Retorna el ALIAS del certificado digital.
     * @throws SignerDocumentException
     */
    private String getAliasFromCertificate(KeyStore keyStore) throws SignerDocumentException {
        String alias = null;
        try {
            Enumeration<String> aliasEnumeration = keyStore.aliases();
            while (aliasEnumeration.hasMoreElements()) {
                alias = aliasEnumeration.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("getAliasFromCertificate()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " ERROR: " + IVenturaError.ERROR_252.getMessage());
            throw new SignerDocumentException(IVenturaError.ERROR_252);
        }
        return alias;
    } //getAliasFromCertificate

    /**
     * Este metodo obtiene la posicion de un nodo para ser firmado.
     *
     * @param document El objeto Document que contien el archivo XML.
     * @return Retorna el nodo para firmar.
     * @throws Exception
     */
    private Node getElementPositionToSign(Document document, String docUUID) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getElementPositionToSign()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "]");
        }
        Node node = null;
        NodeList nodeList = document.getElementsByTagName(ISignerConfig.UBL_TAG_UBLEXTENSION);
        if (null != nodeList && 0 < nodeList.getLength()) {
            /*
             * Gets the last UBLExtension
             * The tag to sign is found in the last UBLExtension.
             */
            NodeList childNodes = nodeList.item(nodeList.getLength() - 1).getChildNodes();

            for (int i = 0; i < childNodes.getLength(); ++i) {
                /* Find the node with EXTENSIONCONTENT tag */
                if (childNodes.item(i).getNodeName().equalsIgnoreCase(ISignerConfig.UBL_TAG_EXTENSIONCONTENT)) {
                    node = childNodes.item(i);
                    break;
                }
            }
        } else {
            logger.error("+-getElementPositionToSign()" + (null != this.identifier ? " [" + this.identifier + "]" : "") + " [" + docUUID + "] ERROR: " + IVenturaError.ERROR_256.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_256);
        }
        return node;
    } //getElementPositionToSign

} //SignerHandler
