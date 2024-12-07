package service.cloud.request.clientrequest.utils;

public interface IWSSecurityConfig {

    String HEADER_PARENT_NAME = "Security";
    String HEADER_PARENT_BASE_PFX = "wsse";
    String HEADER_PARENT_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    String SUB_UNTOKEN_NAME = "UsernameToken";
    String SUB_UNTOKEN_PFX = "xmlns:wsu";
    String SUB_UNTOKEN_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    String PARAM_USERNAME = "Username";
    String PARAM_PASSWORD = "Password";

    String COMMON_ATTRIBUTE_TYPE = "Type";
    String COMMON_ATRIBUTE_PWD_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";

} //IWSSecurityConfig
