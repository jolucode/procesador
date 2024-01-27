/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;

/**
 * @author VS-LT-06
 */
public class DespatchAdvicetypeWRP {

    private static DespatchAdvicetypeWRP instance = null;

    protected DespatchAdvicetypeWRP() {

    }

    public static DespatchAdvicetypeWRP getInstance() {

        if (instance == null) {
            instance = new DespatchAdvicetypeWRP();

        }
        return instance;

    }

    private DespatchAdviceType despatchAdviceType;

    public DespatchAdviceType getDespatchAdviceType() {
        return despatchAdviceType;
    }

    public void setDespatchAdviceType(DespatchAdviceType despatchAdviceType) {
        this.despatchAdviceType = despatchAdviceType;
    }


}
