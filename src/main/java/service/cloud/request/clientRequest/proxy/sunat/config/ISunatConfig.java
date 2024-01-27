package service.cloud.request.clientRequest.proxy.sunat.config;

/**
 * Esta interfaz contiene todas las constantes a utilizar dentro del
 * proyecto.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public interface ISunatConfig {

    /**
     * Nombre de tipo de clientes WS.
     */
    String TEST_CLIENT = "test";
    String HOMOLOGATION_CLIENT = "homologation";
    String PRODUCTION_CLIENT = "production";


    /**
     * Prefijos con los que Sunat devuelve el codigo de una excepcion
     */
    String SOAP_FAULT_CLIENT_EXP = "soap-env-Client";
    String SOAP_FAULT_SERVER_EXP = "soap-env-Server";


    /**
     * Parametros para el WS Security.
     */
    String WS_SECURITY_HEADER_PARENT_NAME = "Security";
    String WS_SECURITY_HEADER_PARENT_BASE_PFX = "wsse";
    String WS_SECURITY_HEADER_PARENT_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    String WS_SECURITY_SUB_UNTOKEN_NAME = "UsernameToken";
    String WS_SECURITY_SUB_UNTOKEN_PFX = "xmlns:wsu";
    String WS_SECURITY_SUB_UNTOKEN_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    String WS_SECURITY_PARAM_USERNAME = "Username";
    String WS_SECURITY_PARAM_PASSWORD = "Password";
    String WS_SECURITY_COMMON_ATTRIBUTE_TYPE = "Type";
    String WS_SECURITY_COMMON_ATRIBUTE_PWD_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";

} //ISunatConfig
