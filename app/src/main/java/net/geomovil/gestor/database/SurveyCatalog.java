package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "survey_catalog")
public class SurveyCatalog implements Serializable {
    private static final long serialVersionUID = -913232092414925908L;
    public static final String WEBID = "webid";
    public static final String NOMBRE = "nombre";
    public static final String CODIGO = "codigo";
    public static final String VALOR = "valor";
    public static final String NOMBREPADRE = "nombre_padre";
    public static final String CODIGOPADRE = "codigo_padre";

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

    @DatabaseField(columnName = NOMBREPADRE)
    private String nombrePadre;

    @DatabaseField(columnName = CODIGOPADRE)
    private String codigoPadre;

    @DatabaseField(canBeNull = false, foreign = true)
    private Survey encuesta;

    public SurveyCatalog() {
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
    public SurveyCatalog(Survey survey, JSONObject catalog) throws Exception {
        encuesta = survey;
//        ID: 1023
        //webId = catalog.getInt("ID");
        webId = catalog.getString("ID");
//        Nombre: "Respuesta"
        nombre = catalog.getString("Nombre");
//        Codigo: "DESBLOQUEOS VISA"
        codigo = catalog.getString("Codigo");
//        Valor: "DESBLOQUEOS VISA"
        valor = catalog.getString("Valor");
//        NombrePadre: "Motivo"
        nombrePadre = catalog.getString("NombrePadre");
//        CodigoPadre: "visa"
        codigoPadre = catalog.getString("CodigoPadre");
    }

    public SurveyCatalog(String webId, String nombre, String codigo, String valor, String nombrePadre, String codigoPadre, Survey encuesta) {
        this.webId = webId;
        this.nombre = nombre;
        this.codigo = codigo;
        this.valor = valor;
        this.nombrePadre = nombrePadre;
        this.codigoPadre = codigoPadre;
        this.encuesta = encuesta;
    }

    @Override
    public String toString() {
        return "SurveyCatalog{" +
                "id=" + id +
                ", webId='" + webId + '\'' +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + codigo + '\'' +
                ", valor='" + valor + '\'' +
                ", nombrePadre='" + nombrePadre + '\'' +
                ", codigoPadre='" + codigoPadre + '\'' +
                ", encuesta=" + encuesta +
                '}';
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoPadre() {
        return codigoPadre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombrePadre() {
        return nombrePadre;
    }

    public String getValor() {
        return valor;
    }

    public Survey getEncuesta() {
        return encuesta;
    }
}
