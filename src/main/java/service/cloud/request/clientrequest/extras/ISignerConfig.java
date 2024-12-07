package service.cloud.request.clientrequest.extras;

/**
 * Esta interfaz contiene las constantes para el proceso
 * de firmado de los documentos UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public interface ISignerConfig {

    /**
     * Prefijo del firmante
     */
    String SIGNER_PREFIX = "signer";


    /**
     * Prefijos de tag's UBL.
     */
    String UBL_TAG_UBLEXTENSION = "ext:UBLExtension";
    String UBL_TAG_EXTENSIONCONTENT = "ext:ExtensionContent";


    /**
     * Propiedades del XML TRANSFORM
     */
    String XML_TRANSFORM_KEYS_ENCODING = "UTF-8";
    String XML_TRANSFORM_KEYS_METHOD = "xml";
    String XML_TRANSFORM_KEYS_INDENT = "no";


    /**
     * Otros parametros de configuracion.
     */
    String SIGN_CONTEXT_NAMESPACE_PREFIX = "ds";
    String SIGNATURE_FACTORY_MECHANISM = "DOM";

} //ISignerConfig
