package service.cloud.request.clientRequest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "config")
public class ListOfMapProperties {
    private List<Map<String, Object>> miscellaneous;


    public void imprimirdatos() {
        //System.out.println(miscellaneous.get(0));
    }

    // Constructor, Getter, and Setter methods
    // toString()
}