package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;
@DatabaseTable(tableName = "survey_question")
public class SurveyQuestion implements Serializable {
    private static final long serialVersionUID = 4840545095893958860L;
    public static final String UUID = "uuid";
    public static final String TIPO_NUMERO = "numero";
    public static final String TIPO_DECIMAL = "decimal";
    public static final String TIPO_FOTO = "foto";
    public static final String TIPO_FECHA = "fecha";
    public static final String TIPO_HORA = "hora";
    public static final String TIPO_TEXTO = "texto";
    public static final String TIPO_TEXTO_LARGO = "textolargo";
    public static final String TIPO_LISTA_SIMPLE = "sseleccion";
    public static final String TIPO_LISTA_MULTIPLE = "mseleccion";
    //public static final String TIPO_AUTO_COMPLETE = "autocomplete";


    public static final String TEXTO = "texto";
    public static final String ETIQUETA = "etiqueta";
    public static final String TIPO = "tipo";
    public static final String OBLIGATORIO = "obligatorio";
    public static final String CATALOGO = "catalogo";
    public static final String EXPRESION = "expresion";
    public static final String WEBID = "webid";
    public static final String POCISION = "posicion";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = UUID)
    private String uuid;

    @DatabaseField(columnName = WEBID)
    private String webId;

    @DatabaseField(columnName = TEXTO)
    private String texto;

    @DatabaseField(columnName = ETIQUETA)
    private String etiqueta;

    @DatabaseField(columnName = TIPO)
    private String tipo;

    @DatabaseField(columnName = OBLIGATORIO)
    private boolean obligatorio;

    @DatabaseField(columnName = CATALOGO)
    private String catalogo;

    @DatabaseField(columnName = EXPRESION)
    private String expresion;

    @DatabaseField(columnName = POCISION)
    private int posicion;

    @DatabaseField(canBeNull = false, foreign = true)
    private Survey encuesta;

    public SurveyQuestion() {
    }

    /**
     * Contructor de la clase
     * @param survey encuesta a la que pertenece la pregunta
     * @param question objeto json que representa la pregunta
     * @throws Exception
     */
    public SurveyQuestion(Survey survey,JSONObject question) throws Exception{
        encuesta = survey;
        webId = question.getString("_id");
        texto = question.getString("texto");
        etiqueta = question.getString("etiqueta");
        tipo = question.getString("tipo");
        obligatorio = question.getBoolean("obligatorio");
        posicion = question.getInt("posicion");
        catalogo = question.getString("catalogo");
        expresion = question.getString("expresion");
        uuid = question.getString("uuid");
    }


    public int getId() {
        return id;
    }

    public Survey getEncuesta() {
        return encuesta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getTexto() {
        return texto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setEncuesta(Survey encuesta) {
        this.encuesta = encuesta;
    }

    public int getPosicion() {
        return posicion;
    }

    public String getCatalogo() {
        return catalogo;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }

    public String getExpresion() {
        return expresion;
    }
}
