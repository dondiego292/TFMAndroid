package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "survey_catalog_hijo")
public class SurveyCatalogHijo implements Serializable {
    private static final long serialVersionUID = -1505655331545858249L;

    public static final String WEBID = "webid";
    public static final String NOMBRE = "nombre";
    public static final String CODIGO = "codigo";
    public static final String VALOR = "valor";
    public static final String CATALOGOPADRE = "catalogoPadre";

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

    @DatabaseField(columnName = CATALOGOPADRE)
    private String catalogoPadre;

    @DatabaseField(canBeNull = false, foreign = true)
    private Survey encuesta;

    public SurveyCatalogHijo() {
    }

    public SurveyCatalogHijo(Survey survey,JSONObject catalog)throws Exception{
        encuesta = survey;
        webId = catalog.getString("_id");
        nombre = catalog.getString("nombre");
        codigo = catalog.getString("codigo");
        valor = catalog.getString("valor");
        catalogoPadre = catalog.getString("catalogoPadre");
    }

    @Override
    public String toString() {
        return "SurveyCatalogHijo{" +
                "id=" + id +
                ", webId='" + webId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + codigo + '\'' +
                ", valor='" + valor + '\'' +
                ", catalogoPadre='" + catalogoPadre + '\'' +
                ", encuesta=" + encuesta +
                '}';
    }

    public int getId() {
        return id;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getCatalogoPadre() {
        return catalogoPadre;
    }

    public void setCatalogoPadre(String catalogoPadre) {
        this.catalogoPadre = catalogoPadre;
    }

    public Survey getEncuesta() {
        return encuesta;
    }

    public void setEncuesta(Survey encuesta) {
        this.encuesta = encuesta;
    }
}
