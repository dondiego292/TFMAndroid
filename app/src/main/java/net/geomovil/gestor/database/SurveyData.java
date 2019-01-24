package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import net.geomovil.gestor.interfaces.ISenderData;
import net.geomovil.gestor.util.TextHelper;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "survey_data")
public class SurveyData implements Serializable, ISenderData {
    private static final long serialVersionUID = -2688451977372695789L;
    private final Logger log = Logger.getLogger(SurveyData.class.getSimpleName());
    public static final String WEBID = "webid";
    public static final String DATOS = "datos";
    public static final String ESTADO = "estado";
    public static final String ERRORES = "errores";
    public static final String DATAID = "dataID";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;
    /**
     * Datos de la encuesta
     */
    @DatabaseField(columnName = DATOS)
    private String datos;
    /**
     * Id del cliente en la web al que corresponden los datos de la
     * encuesta
     */
    @DatabaseField(columnName = WEBID)
    private String webId;
    /**
     * Estado de los datos.
     * 0 - Creado
     * 1 - Enviado Datos
     * 2 - Enviado Fotos
     */
    @DatabaseField(columnName = ESTADO)
    private int estado;
    /**
     * Errores que contiene la encuesta
     */
    @DatabaseField(columnName = ERRORES)
    private String errores;


    @DatabaseField(columnName = DATAID)
    private String dataID;

    public SurveyData() {
        estado = 0;
    }

    public SurveyData(String webId) {
        this.estado = 0;
        this.webId = webId;
        this.dataID = "";
    }

    public SurveyData(String webId, int estado, String datos) {
        this.webId = webId;
        this.estado = estado;
        this.datos = datos;
        this.dataID = "";
    }

    public String getDatos() {
        return datos;
    }

    /**
     * Retorna los datos en formato JSON
     * @return
     */
    public JSONObject getJSONData(){
        try {
            return new JSONObject(getDatos());
        }
        catch(Exception e){
            log.error("",e);
        }

        return new JSONObject();
    }

    public int getId() {
        return id;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getEstado() {
        return estado;
    }

    public String getWebId() {
        return webId;
    }

    @Override
    public String toString() {
        return "SurveyData{" +
                "datos='" + datos + '\'' +
                ", id=" + id +
                ", webId=" + webId +
                ", estado=" + estado +
                ", errores='" + errores + '\'' +
                '}';
    }

    /**
     * Retorna verdadero en caso de que la encuesta contenga fotos
     * @return
     */
    public boolean tieneFotos(){
        try {
            return getJSONData().getBoolean("_tiene_foto");
        }
        catch(Exception e){
            log.error("",e);
        }
        return false;
    }

    /**
     * retorna la etiqueta de la encuesta a la que pertenecen los datos
     * @return
     */
    public String getSurveyName(){
        try {
            return getJSONData().getString("_survey");
        }
        catch(Exception e){
            log.error("",e);
            return  "";
        }
    }

    /**
     * Retorna el prefijo con el que se guardan las fotos de la
     * encuesta a la que pertenecen los datos
     * @return
     */
    public String getPhotoPrefix(){
        if(!TextHelper.isEmptyData(webId)){
            return getSurveyName()+"-"+webId+"-";
        }
        if(webId.equalsIgnoreCase("0")){
            return getSurveyName()+"-";
        }
        return "";
    }

    public String getPhotoPrefix(DatabaseHelper dbHelper) {
        String prefix = getPhotoPrefix();
        return prefix;
    }

    /**
     * Le asigna a los datos el sguiente estado.
     */
    public void setNextStatus() {
        switch (estado){
            case 0:
                estado = tieneFotos()? 1 :2;
                break;
            case 1:
                estado = 2;
                break;
            case 2:
                estado = 3;
        }
    }

    private static final int ERROR_SENDING = 100;

    /**
     * Le asigna a los datos el sguiente estado.
     */
    public void setErrorNextStatus() {
        estado = ERROR_SENDING;
    }

    /**
     * retorna verdadero si los datos ya fueron finalizados
     *
     * @return
     */
    public boolean esErrorFinalizada() {
        return estado == ERROR_SENDING;
    }

    /**
     * retorna verdadero si los datos ya fueron finalizados
     * @return
     */
    public boolean esFinalizada(){
        return  estado == 2;
    }

    @Override
    public String getSenderType() {
        return "SurveyData";
    }

    public String getDataID() {
        return dataID;
    }

    public void setDataID(String dataID) {
        this.dataID = dataID;
    }
}
