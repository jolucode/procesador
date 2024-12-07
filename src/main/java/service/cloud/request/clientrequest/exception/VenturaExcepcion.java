/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientrequest.exception;


public class VenturaExcepcion extends RuntimeException {

    public VenturaExcepcion(Throwable thrwbl) {
        super(thrwbl);
    }

    public VenturaExcepcion(String mensaje) {
        super(mensaje);
    }

    public VenturaExcepcion(String mensaje, Exception ex) {
        super(mensaje, ex.getCause());
    }

}
