package net.geomovil.gestor.util;

import net.geomovil.encuesta.form.FormWidget;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.Survey;

import org.apache.log4j.Logger;

public class ClientDataInitializer {
    private static final Logger log = Logger.getLogger(ClientDataInitializer.class.getSimpleName());
    public static void initData(FormWidget fw, ClientData client, Survey survey) {
        try {
//            if (survey.getEtiqueta().equalsIgnoreCase("COBRANZAGESTOR")) {

            if(!survey.isLibre()){




            }


//            }
            if (survey.getEtiqueta().equalsIgnoreCase("VERIFICACION")){

            }

            if (survey.getEtiqueta().equalsIgnoreCase("VERIFICACIONLABORAL")){

            }


            if (survey.getEtiqueta().equalsIgnoreCase("actualizacion")){

            }



        }catch (Exception e){
            log.error("",e);
        }
    }
}
