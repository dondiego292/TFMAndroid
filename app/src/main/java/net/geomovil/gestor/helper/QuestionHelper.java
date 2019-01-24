package net.geomovil.gestor.helper;

import net.geomovil.gestor.database.SurveyQuestion;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class QuestionHelper {
    private static final Logger log = Logger.getLogger(QuestionHelper.class.getSimpleName());
    public static JSONObject translate(SurveyQuestion question){
        JSONObject p = new JSONObject();
        try {
            p.put("encuesta", question.getEncuesta().getId());
            p.put("tipo", question.getTipo());
            p.put("tipo_respuesta", "");
            p.put("label", question.getEtiqueta());
            p.put("name", question.getTexto());
            p.put("id", question.getId());
            p.put("position", question.getPosicion());
            p.put("required", question.isObligatorio());
            p.put("maestro_col", question.getCatalogo());
            p.put("expresion", question.getExpresion());
        }
        catch(Exception e){
            log.error("",e);
        }
        return p;
    }
}
