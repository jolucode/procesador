package service.cloud.request.clientRequest.utils;

public interface IWSSecurityConfig {

    public static final String HEADER_PARENT_NAME = "Security";
    public static final String HEADER_PARENT_BASE_PFX = "wsse";
    public static final String HEADER_PARENT_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    public static final String SUB_UNTOKEN_NAME = "UsernameToken";
    public static final String SUB_UNTOKEN_PFX = "xmlns:wsu";
    public static final String SUB_UNTOKEN_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String PARAM_USERNAME = "Username";
    public static final String PARAM_PASSWORD = "Password";

    public static final String COMMON_ATTRIBUTE_TYPE = "Type";
    public static final String COMMON_ATRIBUTE_PWD_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";

} //IWSSecurityConfig
