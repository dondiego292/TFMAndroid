package net.geomovil.gestor.database;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.geomovil.gestor.R;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "survey")
public class Survey implements Serializable {
    private static final long serialVersionUID = 7693859244489768870L;

    public static final String NOMBRE = "nombre";
    public static final String ETIQUETA = "etiqueta";
    public static final String UUID = "uuid";
    public static final String TIPO = "tipo";
    public static final String ESTADO = "estado";
    public static final String LIBRE = "libre";
    public static final String INSTALADA = "instalada";
    public static final String DESCRIPCION = "descripcion";
    public static final String WEBID = "webid";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = NOMBRE)
    private String nombre;

    @DatabaseField(columnName = ETIQUETA)
    private String etiqueta;

    @DatabaseField(columnName = UUID)
    private String uuid;

    @DatabaseField(columnName = TIPO)
    private String tipo;

    @DatabaseField(columnName = DESCRIPCION)
    private String descripcion;

    @DatabaseField(columnName = ESTADO)
    private int estado;

    @DatabaseField(columnName = LIBRE)
    private boolean libre;

    @DatabaseField(columnName = INSTALADA)
    private int instalada;

    @DatabaseField(columnName = WEBID)
    private String webId;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SurveyQuestion> preguntas;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SurveyCatalog> catalogos;
    @ForeignCollectionField(eager = false)
    private ForeignCollection<SurveyCatalogPadre> catalogosPadres;
    @ForeignCollectionField(eager = false)
    private ForeignCollection<SurveyCatalogHijo> catalogosHijos;

    public Survey() {
    }

    public Survey(JSONObject survey) throws Exception {
        webId = survey.getString("_id");
        tipo = survey.getString("tipo");
        uuid = survey.getString("uuid");
        nombre = survey.getString("nombre");
        etiqueta = survey.getString("etiqueta");
        estado = survey.getInt("estado");
        libre = survey.getInt("libre") != 0 ? true : false;
        descripcion = survey.getString("descripcion");
        instalada = 0;
    }

    public Survey update(JSONObject survey) throws Exception {
        tipo = survey.getString("tipo");
        uuid = survey.getString("uuid");
        nombre = survey.getString("nombre");
        etiqueta = survey.getString("etiqueta");
        estado = survey.getInt("estado");
        libre = survey.getInt("libre") != 0 ? true : false;
        descripcion = survey.getString("descripcion");
        return this;
    }

    @Override
    public String toString() {
        return "Survey{" +
                "etiqueta='" + etiqueta + '\'' +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", preguntas=" + preguntas +
                '}';
    }


    public String getEtiqueta() {
        return etiqueta;
    }

    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public String getWebId() {
        return webId;
    }

    public String getUuid() {
        return uuid;
    }

    public ForeignCollection<SurveyQuestion> getPreguntas() {
        return preguntas;
    }

    public void setPreguntas(ForeignCollection<SurveyQuestion> preguntas) {
        this.preguntas = preguntas;
    }

    public ForeignCollection<SurveyCatalog> getCatalogos() {
        return catalogos;
    }

    public ForeignCollection<SurveyCatalogHijo> getCatalogosHijos() {
        return catalogosHijos;
    }

    public ForeignCollection<SurveyCatalogPadre> getCatalogosPadres() {
        return catalogosPadres;
    }

    public void setCatalogosPadres(ForeignCollection<SurveyCatalogPadre> catalogosPadres) {
        this.catalogosPadres = catalogosPadres;
    }

    public String getTipo() {
        return tipo;
    }

    /**
     * Dependiendo del estado en el que se encuentra la encuesta,
     * retorna la imagen que se debe utilizar en la lista de
     * encuesta de la pantalla Sistema
     *
     * @return
     */
    public int getImgEstado() {
        switch (instalada) {
            case 0:
                return R.drawable.ic_por_instalar;
            case 1:
                return R.drawable.ic_instalada;
        }
        return R.drawable.ic_eliminada;
    }

    public void setInstalada(int instalada) {
        this.instalada = instalada;
    }

    public int getInstalada() {
        return instalada;
    }

    public boolean EstadoInstalada() {
        return instalada == 1;
    }

    public boolean EstadoPorInstalar() {
        return instalada == 0;
    }

    public boolean EstadoObsoleta() {
        return instalada == 2;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isLibre() {
        return libre;
    }
}
