package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable(tableName = "log_event")
public class LogEvent implements Serializable{
    private static final long serialVersionUID = 9211046598689463029L;
    public static final String FECHA = "fecha";
    public static final String MENSAJE = "mensaje";
    public static final String ENVIADO = "enviado";
    public static final String UUID = "uuid";
    public static final String TIPO = "tipo";
    public static final String PROPIETARIO = "proppietario";

    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = FECHA)
    public Date fecha;

    @DatabaseField(columnName = MENSAJE)
    public String mensaje;

    @DatabaseField(columnName = ENVIADO)
    public boolean enviado;
    /**
     * Tipo de mensaje
     * 1 - informativo
     * 2 - error
     */
    @DatabaseField(columnName = TIPO)
    public int tipo;

    @DatabaseField(columnName = PROPIETARIO)
    public int propietario;

    @DatabaseField(columnName = UUID)
    public String uuuid;

    public LogEvent() {
    }

    public LogEvent(String mensaje) {
        this.mensaje = mensaje;
        this.fecha = new Date(System.currentTimeMillis());
        this.enviado = false;
        this.tipo = 1;
        this.propietario = 0;
        this.uuuid = java.util.UUID.randomUUID().toString();
    }
    /**
     *
     * @param mensaje mensaje a guardar en el log
     * @param propietario 0- sistema 1- usuario
     */
    public LogEvent(String mensaje, int propietario) {
        this.mensaje = mensaje;
        this.fecha = new Date(System.currentTimeMillis());
        this.enviado = false;
        this.tipo = 1;
        this.propietario = propietario;
        this.uuuid = java.util.UUID.randomUUID().toString();
    }

    /**
     * Crea una nueva instancia del log
     * @param mensaje mensaje a registrar
     * @param propietario 0- sistema 1- usuario
     * @param tipo
     */
    public LogEvent(String mensaje, int propietario, int tipo) {
        this.mensaje = mensaje;
        this.fecha = new Date(System.currentTimeMillis());
        this.enviado = false;
        this.tipo = tipo;
        this.uuuid = java.util.UUID.randomUUID().toString();
        this.propietario = propietario;
    }

    public int getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public int getTipo() {
        return tipo;
    }

    public int getPropietario() {
        return propietario;
    }

    public String getUuuid() {
        return uuuid;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }
}
