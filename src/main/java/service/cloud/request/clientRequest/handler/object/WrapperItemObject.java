package service.cloud.request.clientRequest.handler.object;

import java.util.List;
import java.util.Map;


public class WrapperItemObject {

    private List<String> lstDinamicaItem;

    private Map<String, String> lstItemHashMap;

    public Map<String, String> getLstItemHashMap() {
        return lstItemHashMap;
    }

    public void setLstItemHashMap(Map<String, String> lstItemHashMap) { this.lstItemHashMap = lstItemHashMap; }

    public List<String> getLstDinamicaItem() {
        return lstDinamicaItem;
    }

    public void setLstDinamicaItem(List<String> lstDinamicaItem) {
        this.lstDinamicaItem = lstDinamicaItem;
    }





}
