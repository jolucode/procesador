package service.cloud.request.clientRequest.handler.object.item;

import java.math.BigDecimal;

/**
 * Esta clase contiene la informacion de ITEM de una BOLETA.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class BoletaItemObject {

    private BigDecimal quantityItem;

    private String unitMeasureItem;

    private String descriptionItem;

    private BigDecimal unitValueItem;

    private BigDecimal unitPriceItem;

    private BigDecimal unitPriceIGV;

    private String discountItem;

    private String amountItem;


    /**
     * Constructor basico de la clase BoletaItemObject.
     */
    public BoletaItemObject() {
    }


    public BigDecimal getQuantityItem() {
        return quantityItem;
    } //getQuantityItem

    public void setQuantityItem(BigDecimal quantityItem) {
        this.quantityItem = quantityItem;
    } //setQuantityItem

    public String getUnitMeasureItem() {
        return unitMeasureItem;
    } //getUnitMeasureItem

    public void setUnitMeasureItem(String unitMeasureItem) {
        this.unitMeasureItem = unitMeasureItem;
    } //setUnitMeasureItem

    public String getDescriptionItem() {
        return descriptionItem;
    } //getDescriptionItem

    public void setDescriptionItem(String descriptionItem) {
        this.descriptionItem = descriptionItem;
    } //setDescriptionItem

    public BigDecimal getUnitValueItem() {
        return unitValueItem;
    } //getUnitValueItem

    public void setUnitValueItem(BigDecimal unitValueItem) {
        this.unitValueItem = unitValueItem;
    } //setUnitValueItem

    public BigDecimal getUnitPriceItem() {
        return unitPriceItem;
    } //getUnitPriceItem

    public void setUnitPriceItem(BigDecimal unitPriceItem) {
        this.unitPriceItem = unitPriceItem;
    } //setUnitPriceItem

    public String getDiscountItem() {
        return discountItem;
    } //getDiscountItem

    public void setDiscountItem(String discountItem) {
        this.discountItem = discountItem;
    } //setDiscountItem

    public String getAmountItem() {
        return amountItem;
    } //getAmountItem

    public void setAmountItem(String amountItem) {
        this.amountItem = amountItem;
    } //setAmountItem


    public BigDecimal getUnitPriceIGV() {
        return unitPriceIGV;
    }


    public void setUnitPriceIGV(BigDecimal unitPriceIGV) {
        this.unitPriceIGV = unitPriceIGV;
    }

} //BoletaItemObject
