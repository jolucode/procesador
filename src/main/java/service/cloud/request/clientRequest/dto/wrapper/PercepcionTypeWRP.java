package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_2.PerceptionType;

public class PercepcionTypeWRP {

    public static PercepcionTypeWRP instance = null;

    protected PercepcionTypeWRP() {

    }

    public static PercepcionTypeWRP getInstance() {
        if (instance == null) {
            instance = new PercepcionTypeWRP();
        }
        return instance;
    }

    private PerceptionType perceptionType;

    public PerceptionType getPerceptionType() {
        return perceptionType;
    }

    public void setPerceptionType(PerceptionType perceptionType) {
        this.perceptionType = perceptionType;
    }

    public static void setInstance(PercepcionTypeWRP instance) {
        PercepcionTypeWRP.instance = instance;
    }

}
