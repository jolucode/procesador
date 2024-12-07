package service.cloud.request.clientrequest.dto.finalClass;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
public class Response {
    private byte[] response;
    private String errorCode;
    private String errorMessage;
}
