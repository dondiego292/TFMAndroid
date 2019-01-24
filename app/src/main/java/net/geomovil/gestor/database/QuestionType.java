package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONObject;

import java.io.Serializable;

@DatabaseTable(tableName = "question_type")
public class QuestionType implements Serializable {
    private static final long serialVersionUID = -6791945464913583342L;

    public static final String WEBID = "webid";
    public static final String TIPO = "tipo";
    public static final String NOMBRE = "nombre";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = WEBID)
    private String webId;

    @DatabaseField(columnName = TIPO)
    private String tipo;

    @DatabaseField(columnName = NOMBRE)
    private String nombre;

    public QuestionType() {
    }

    public QuestionType(int id) {
        this.id = id;
    }

    public QuestionType(JSONObject type)throws Exception{
        webId = type.getString("_id");
        tipo = type.getString("tipo");
        nombre = type.getString("nombre");
    }

    @Override
    public String toString() {
        return "QuestionType{" +
                "id=" + id +
                ", webId='" + webId + '\'' +
                ", tipo='" + tipo + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getWebId() {
        return webId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }
}
