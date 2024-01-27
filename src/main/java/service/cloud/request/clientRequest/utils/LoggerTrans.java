/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Percy
 */
public class LoggerTrans {

    private static final Map<String, Logger> logs = new HashMap<>();

    private static final DateFormat dh = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final DateFormat d = new SimpleDateFormat("yyyyMMdd");

    public static final DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");

    private static FileHandler fh = null;

    /**
     * Crea y almacena un Logger con con el nombre del parámetro "nombre".
     * Además creará un archivo log con el mismo nombre.
     *
     * @param nombre el nombre del logger a crear y almacenar.
     * @return uns instacia del logger creado con el nombre del parámetro
     * "nombre"
     */
    private static Logger StoreLogger(String nombre) {
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String sRutaLogReal = System.getProperty("user.dir");
        String[] sRutaLogGeneral = sRutaLogReal.split("[\\\\/]", -1);
        sRutaLogReal = "";
        for (int i = 0; i < sRutaLogGeneral.length - 1; i++) {
            sRutaLogReal = sRutaLogReal + sRutaLogGeneral[i] + File.separator;
        }
        String ruta = sRutaLogReal + "\\logs" + File.separator + date;
        try {
            File f = new File(ruta);
            f.mkdirs();
        } catch (Exception ex) {
            ruta = sRutaLogReal + "\\logs" + File.separator + date;
        }
        String nombreArchivo = ruta + "\\" + nombre + ".log";
        Logger dlog = Logger.getLogger(nombre);
        dlog.setLevel(Level.FINER);
        logs.put(nombre, dlog);
        try {
            //System.out.println("nombreArchivo: "+nombreArchivo);
            fh = new FileHandler(nombreArchivo, true);
            fh.setLevel(Level.ALL);
            dlog.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            ex.printStackTrace();
        }
        return dlog;
    }

    /**
     * Crea un Logger con la fecha y hora del momento de su creación. Este
     * durará durante la ejecución actual de la aplicación
     *
     * @return un Logger con la fecha y hora del momento de su creación.
     */
    public static Logger createInstanceLogger() {
        Date ahora = new Date(System.currentTimeMillis());
        String nomLogger = dh.format(ahora);
        return StoreLogger(nomLogger);
    }

    /**
     * @param sufijo el nombre del log.
     * @return Crea un Logger con la fecha del momento de su creación. El nombre
     * del archivo se construirá con la fecha de su creación mas el parámetro
     * sufijo (fecha+sufijo) Si ya existe un archivo con el mismo nombre, se
     * reutilizará
     */
    public static Logger createCDLogger(String sufijo) {
        Date ahora = new Date(System.currentTimeMillis());
        String fecha = d.format(ahora);
        String nomLogger = fecha + sufijo;
        return StoreLogger(nomLogger);
    }

    public static Logger createCDMainLogger() {
        return createCDLogger("Request_Main");
    }

    public static Logger getLogger(String nombre) {
        Logger l = logs.get(nombre);
        if (l == null) {
            l = StoreLogger(nombre);
        }
        return l;
    }

    public static Logger getInstanceLogger() {
        Date ahora = new Date(System.currentTimeMillis());
        String nomLogger = dh.format(ahora);
        return getLogger(nomLogger);
    }

    public static Logger getCDLogger(String sufijo) {
        Date ahora = new Date(System.currentTimeMillis());
        String fecha = d.format(ahora);
        Logger l = logs.get(fecha + sufijo);
        if (l == null) {
            l = createCDLogger(sufijo);
        }
        return l;
    }

    /**
     * @return el logger diario con el nombre del hilo actual
     */
    public static Logger getCDThreadLogger() {
        String sufijo = Thread.currentThread().getName();
        Logger l = logs.get(sufijo);
        if (l == null) {
            l = getCDLogger(sufijo);
        }
        return l;
    }

    public static Logger getCDMainLogger() {
        String sufijo = "Request_Main";
        Logger l = logs.get(sufijo);
        if (l == null) {
            l = getCDLogger(sufijo);
        }
        return l;
    }

    public static int cerrarLogger(String sufijo) {
        Date ahora = new Date(System.currentTimeMillis());
        String fecha = d.format(ahora);
        String nombre = fecha + sufijo;
        Logger dlog = Logger.getLogger(nombre);
        if (dlog != null) {
            for (Handler fileHandler : dlog.getHandlers()) {
                fileHandler.close();
            }
        }
        return 1;
    }
}
