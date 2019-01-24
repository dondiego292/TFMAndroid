package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "survey_catalog_padre")
public class SurveyCatalogPadre implements Serializable {
    private static final long serialVersionUID = 8883297353289030977L;

    public static final String WEBID = "webid";
    public static final String NOMBRE = "nombre";
    public static final String CODIGO = "codigo";
    public static final String VALOR = "valor";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = WEBID)
    private String webId;

    @DatabaseField(columnName = NOMBRE)
    private String nombre;

    @DatabaseField(columnName = CODIGO)
    private String codigo;

    @DatabaseField(columnName = VALOR)
    private String valor;

    @DatabaseField(canBeNull = false, foreign = true)
    private Survey encuesta;

    public SurveyCatalogPadre() {
    }

    /**
     * Constructor de la clase que recibe la encuesta a la que pertenece este
     * objeto del catalogo y el objeto json que representa al catalogo y se
     * obtiene del servidor en el proceso de instalacion de la encuesta
     *
     * @param survey  encuesta
     * @param catalog objeto json que representa el catalodo
     * @throws Exception
     */
    public SurveyCatalogPadre(Survey survey, JSONObject catalog) throws Exception {
        encuesta = survey;
        webId = catalog.getString("_id");
        nombre = catalog.getString("nombre");
        codigo = catalog.getString("codigo");
        valor = catalog.getString("valor");
    }

    @Override
    public String toString() {
        return "SurveyCatalog{" +
                "id=" + id +
                ", webId='" + webId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + codigo + '\'' +
                ", valor='" + valor + '\'' +
                ", encuesta=" + encuesta +
                '}';
    }

    public String getCodigo() {
        return codigo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }


    public void setId(int id) {
        this.id = id;
    }

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Survey getEncuesta() {
        return encuesta;
    }

    public void setEncuesta(Survey encuesta) {
        this.encuesta = encuesta;
    }
}
