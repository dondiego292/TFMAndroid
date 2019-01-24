package net.geomovil.gestor.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

@DatabaseTable(tableName = "question_rule")
public class QuestionRule implements Serializable {
    private static final long serialVersionUID = 1443270658228623761L;

    private final Logger log = Logger.getLogger(QuestionRule.class.getSimpleName());
    public static final String PREGUNTA = "pregunta";
    public static final String DEPENDE = "depende";
    public static final String VALOR = "valor";
    public static final String ACCION = "accion";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;
    /**
     * Datos de la encuesta
     */
    @DatabaseField(columnName = PREGUNTA)
    private int pregunta;

    @DatabaseField(columnName = DEPENDE)
    private int depende;

    @DatabaseField(columnName = VALOR)
    private String valor;

    @DatabaseField(columnName = ACCION)
    private String accion;

    public QuestionRule() {
    }

    public QuestionRule(String accion, int depende, int pregunta, String valor) {
        this.accion = accion;
        this.depende = depende;
        this.pregunta = pregunta;
        this.valor = valor;
    }

    public QuestionRule(JSONObject data, Dao<SurveyQuestion, Integer> questionDao){
        try {
            valor = data.getString("valor");
            accion = data.getString("accion").toLowerCase();
            List<SurveyQuestion> qs = questionDao
                    .queryForEq(SurveyQuestion.UUID,data.getString("codigoPrincipal"));
            if(qs.size() > 0){
                pregunta = qs.get(0).getId();
            }
            qs = questionDao
                    .queryForEq(SurveyQuestion.UUID, data.getString("codigoDependiente"));
            if(qs.size() > 0){
                depende = qs.get(0).getId();
            }

        }
        catch(Exception e){
            log.error("",e);
        }
    }

    public int getPregunta() {
        return pregunta;
    }

    public String getValor() {
        return valor;
    }

    public String getAccion() {
        return accion;
    }
}
