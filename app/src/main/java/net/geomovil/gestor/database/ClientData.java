package net.geomovil.gestor.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import net.geomovil.gestor.util.TextHelper;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@DatabaseTable(tableName = "client_data")
public class ClientData implements Serializable {
    private final Logger log = Logger.getLogger(ClientData.class.getSimpleName());
    private static final long serialVersionUID = 3669199015034951207L;

    public static final String WEBID = "WEBID";
    public static final String NOMBRE = "nombre";
    public static final String APELLIDO = "apellido";
    public static final String IDENTIFICACION = "identificacion";
    public static final String TELEFONO = "TELEFONO";
    public static final String DIRECCION = "direccion";
    public static final String ESTADOCIVIL = "estadoCivil";
    public static final String GENERO = "genero";
    public static final String LUGARDENACIMIENTO = "lugarDeNacimiento";
    public static final String CIUDAD = "ciudad";
    public static final String FECHAINSPECCION = "fechaInspeccion";
    public static final String FECHACREACION = "fechaCreacion";
    public static final String FECHAVISITADO = "fechaVisitado";
    public static final String FECHANACIMIENTO = "fechaNacimiento";
    public static final String LATITUD = "latitud";
    public static final String LONGITUD = "longitud";
    public static final String FLATITUD = "fLatitud";
    public static final String FLONGITUD = "fLongitud";
    public static final String GESTION = "gestion";
    public static final String LATITUDCAPTURADA = "latitudCapturada";
    public static final String LONGITUDCAPTURADA = "longitudCapturada";
    public static final String MOVILSTATUS = "movilStatus";
    public static final String FECHA = "fecha";


    @DatabaseField(generatedId = true, columnName = "id")
    public int id;

    @DatabaseField(columnName = WEBID)
    private String webID;

    @DatabaseField(columnName = NOMBRE)
    private String nombre;

    @DatabaseField(columnName = APELLIDO)
    private String apellido;

    @DatabaseField(columnName = IDENTIFICACION)
    private String identificacion;

    @DatabaseField(columnName = TELEFONO)
    private String telefono;

    @DatabaseField(columnName = DIRECCION)
    private String direccion;

    @DatabaseField(columnName = ESTADOCIVIL)
    private String estadoCivil;

    @DatabaseField(columnName = GENERO)
    private String genero;

    @DatabaseField(columnName = LUGARDENACIMIENTO)
    private String lugarDeNacimiento;

    @DatabaseField(columnName = CIUDAD)
    private String ciudad;

    @DatabaseField(columnName = FECHAINSPECCION)
    private Date fechaInspeccion;

    @DatabaseField(columnName = FECHACREACION)
    private Date fechaCreacion;

    @DatabaseField(columnName = FECHAVISITADO)
    private Date fechaVisitado;

    @DatabaseField(columnName = FECHANACIMIENTO)
    private Date fechaNacimiento;

    @DatabaseField(columnName = FECHA)
    private Date fecha;

    @DatabaseField(columnName = LATITUD)
    private String latitud;

    @DatabaseField(columnName = LONGITUD)
    private String longitud;

    @DatabaseField(columnName = FLATITUD)
    private double flatitud;

    @DatabaseField(columnName = FLONGITUD)
    private double flongitud;

    @DatabaseField(columnName = GESTION)
    private String gestion;

    @DatabaseField(columnName = LATITUDCAPTURADA)
    private String latitudCapturada;

    @DatabaseField(columnName = LONGITUDCAPTURADA)
    private String longitudCapturada;

    @DatabaseField(columnName = MOVILSTATUS)
    private int movilStatus;

    public ClientData() {
    }

    public ClientData(JSONObject client) throws Exception {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2017-06-20T15:50:37
        DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");//2017-06-20T15:50:37
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        Date dateFechaCreacion = null;
        String fechaCreacion1 = "";
        if (client.has("fechaCreacion") && !TextHelper.isEmptyData(client.getString("fechaCreacion").substring(0, 24))) {
            dateFechaCreacion = sdf.parse(client.getString("fechaCreacion").substring(0, 24));
            fechaCreacion1 = formatter.format(dateFechaCreacion);
        }
        Date dateFechaVisitado = null;
        String fechaVisitado1 = "";
        if (client.has("fechaVisitado") && !TextHelper.isEmptyData(client.getString("fechaVisitado").substring(0, 24))) {
            dateFechaVisitado = sdf.parse(client.getString("fechaVisitado").substring(0, 24));
            fechaVisitado1 = formatter.format(dateFechaVisitado);
        }

        Date dateFecha = null;
        String fecha1 = "";
        if (client.has("fecha") && !TextHelper.isEmptyData(client.getString("fecha").substring(0, 24))) {
            dateFecha = sdf.parse(client.getString("fecha").substring(0, 24));
            fecha1 = formatter.format(dateFecha);
        }


        Date dateFechaNacimiento = null;
        String fechaNacimiento1 = "";
        if (client.has("fechaNacimiento") && !TextHelper.isEmptyData(client.getString("fechaNacimiento").substring(0, 24))) {
            dateFechaNacimiento = sdf.parse(client.getString("fechaNacimiento").substring(0, 24));
            fechaNacimiento1 = formatter.format(dateFechaNacimiento);
        }
        Date dateFechaInspeccion = null;
        String fechaInspeccion1 = "";
        if (client.has("fechaInspeccion") && !TextHelper.isEmptyData(client.getString("fechaInspeccion").substring(0, 24))) {
            dateFechaInspeccion = sdf.parse(client.getString("fechaInspeccion").substring(0, 24));
            fechaInspeccion1 = formatter2.format(dateFechaInspeccion);
        }
        webID = client.getString("_id");
        nombre = !TextHelper.isEmptyData("nombre") ? client.getString("nombre") : "";
        apellido = !TextHelper.isEmptyData("apellido") ? client.getString("apellido") : "";
        identificacion = !TextHelper.isEmptyData("identificacion") ? client.getString("identificacion") : "";
        telefono = !TextHelper.isEmptyData("telefono") ? client.getString("telefono") : "";
        direccion = !TextHelper.isEmptyData("direccion") ? client.getString("direccion") : "";
        estadoCivil = !TextHelper.isEmptyData("estadoCivil") ? client.getString("estadoCivil") : "";
        genero = !TextHelper.isEmptyData("genero") ? client.getString("genero") : "";
        lugarDeNacimiento = !TextHelper.isEmptyData("lugarNacimiento") ? client.getString("lugarNacimiento") : "";
        ciudad = !TextHelper.isEmptyData("ciudad") ? client.getString("ciudad") : "";
        latitud = !TextHelper.isEmptyData("latitud") ? client.getString("latitud") : "";
        longitud = !TextHelper.isEmptyData("longitud") ? client.getString("longitud") : "";
        flatitud = !TextHelper.isEmptyData("latitud") ? client.getDouble("latitud") : 0;
        flongitud = !TextHelper.isEmptyData("longitud") ? client.getDouble("longitud") : 0;
        gestion = !TextHelper.isEmptyData("gestion") ? client.getString("gestion") : "";
        fechaCreacion = !TextHelper.isEmptyData(fechaCreacion1) ? formatter.parse(fechaCreacion1) : null;
        fechaVisitado = !TextHelper.isEmptyData(fechaVisitado1) ? formatter.parse(fechaVisitado1) : null;
        fechaNacimiento = !TextHelper.isEmptyData(fechaNacimiento1) ? formatter.parse(fechaNacimiento1) : null;
        fechaInspeccion = !TextHelper.isEmptyData(fechaInspeccion1) ? formatter2.parse(fechaInspeccion1) : null;
        fecha = !TextHelper.isEmptyData(fecha1) ? formatter2.parse(fecha1) : null;

        movilStatus = 0;
    }

    public void UpdateData(JSONObject client) throws Exception {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2017-06-20T15:50:37
        DateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");//2017-06-20T15:50:37
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        Date dateFechaCreacion = null;
        String fechaCreacion1 = "";
        if (client.has("fechaCreacion") && !TextHelper.isEmptyData(client.getString("fechaCreacion").substring(0, 24))) {
            dateFechaCreacion = sdf.parse(client.getString("fechaCreacion").substring(0, 24));
            fechaCreacion1 = formatter.format(dateFechaCreacion);
        }
        Date dateFechaVisitado = null;
        String fechaVisitado1 = "";
        if (client.has("fechaVisitado") && !TextHelper.isEmptyData(client.getString("fechaVisitado").substring(0, 24))) {
            dateFechaVisitado = sdf.parse(client.getString("fechaVisitado").substring(0, 24));
            fechaVisitado1 = formatter.format(dateFechaVisitado);
        }
        Date dateFechaNacimiento = null;
        String fechaNacimiento1 = "";
        if (client.has("fechaNacimiento") && !TextHelper.isEmptyData(client.getString("fechaNacimiento").substring(0, 24))) {
            dateFechaNacimiento = sdf.parse(client.getString("fechaNacimiento").substring(0, 24));
            fechaNacimiento1 = formatter.format(dateFechaNacimiento);
        }
        Date dateFechaInspeccion = null;
        String fechaInspeccion1 = "";
        if (client.has("fechaInspeccion") && !TextHelper.isEmptyData(client.getString("fechaInspeccion").substring(0, 24))) {
            dateFechaInspeccion = sdf.parse(client.getString("fechaInspeccion").substring(0, 24));
            fechaInspeccion1 = formatter.format(dateFechaInspeccion);
        }
        nombre = !TextHelper.isEmptyData("nombre") ? client.getString("nombre") : "";
        apellido = !TextHelper.isEmptyData("apellido") ? client.getString("apellido") : "";
        identificacion = !TextHelper.isEmptyData("identificacion") ? client.getString("identificacion") : "";
        telefono = !TextHelper.isEmptyData("telefono") ? client.getString("telefono") : "";
        direccion = !TextHelper.isEmptyData("direccion") ? client.getString("direccion") : "";
        estadoCivil = !TextHelper.isEmptyData("estadoCivil") ? client.getString("estadoCivil") : "";
        genero = !TextHelper.isEmptyData("genero") ? client.getString("genero") : "";
        lugarDeNacimiento = !TextHelper.isEmptyData("lugarNacimiento") ? client.getString("lugarNacimiento") : "";
        ciudad = !TextHelper.isEmptyData("ciudad") ? client.getString("ciudad") : "";
        latitud = !TextHelper.isEmptyData("latitud") ? client.getString("latitud") : "";
        longitud = !TextHelper.isEmptyData("longitud") ? client.getString("longitud") : "";
        flatitud = !TextHelper.isEmptyData("latitud") ? client.getDouble("latitud") : 0;
        flongitud = !TextHelper.isEmptyData("longitud") ? client.getDouble("longitud") : 0;
        gestion = !TextHelper.isEmptyData("gestion") ? client.getString("gestion") : "";
        fechaCreacion = !TextHelper.isEmptyData(fechaCreacion1) ? formatter.parse(fechaCreacion1) : null;
        fechaVisitado = !TextHelper.isEmptyData(fechaVisitado1) ? formatter.parse(fechaVisitado1) : null;
        fechaNacimiento = !TextHelper.isEmptyData(fechaNacimiento1) ? formatter.parse(fechaNacimiento1) : null;
        fechaInspeccion = !TextHelper.isEmptyData(fechaInspeccion1) ? formatter2.parse(fechaInspeccion1) : null;
    }

    public ClientData(String webID, String identificacion, String nombre, String direccion, String latitud, String longitud, String gestion) {
        try {
            this.webID = webID;
            this.identificacion = identificacion;
            this.direccion = direccion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.nombre = nombre;
            this.gestion = gestion;
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            this.fechaInspeccion = formatter.parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        } catch (Exception e) {
            log.error("", e);
        }

    }

    @Override
    public String toString() {
        return "ClientData{" +
                "id=" + id +
                ", webID='" + webID + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", identificacion='" + identificacion + '\'' +
                ", telefono='" + telefono + '\'' +
                ", direccion='" + direccion + '\'' +
                ", estadoCivil='" + estadoCivil + '\'' +
                ", genero='" + genero + '\'' +
                ", lugarDeNacimiento='" + lugarDeNacimiento + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", fechaInspeccion=" + fechaInspeccion +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaVisitado=" + fechaVisitado +
                ", fechaNacimiento=" + fechaNacimiento +
                ", latitud='" + latitud + '\'' +
                ", longitud='" + longitud + '\'' +
                ", flatitud=" + flatitud +
                ", flongitud=" + flongitud +
                ", gestion='" + gestion + '\'' +
                ", latitudCapturada='" + latitudCapturada + '\'' +
                ", longitudCapturada='" + longitudCapturada + '\'' +
                ", movilStatus=" + movilStatus +
                '}';
    }


    /**
     * 0 - creado
     * 1 - guardado
     * 2 - enviado
     *
     * @return
     */
    public int getMovilStatus() {
        return movilStatus;
    }

    public void setMovilStatus(int movilStatus) {
        this.movilStatus = movilStatus;
    }

    public boolean isError() {
        return movilStatus == 100;
    }

    public boolean isSaved() {
        return movilStatus == 1;
    }

    public boolean isSend() {
        return movilStatus == 2;
    }

    public boolean isInactive() {
        return movilStatus == 3;
    }

    public GeoPoint getLocation() {
        double lat = Double.parseDouble(latitud);
        double lng = Double.parseDouble(longitud);
        return new GeoPoint(lat, lng);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWebID() {
        return webID;
    }

    public void setWebID(String webID) {
        this.webID = webID;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getLugarDeNacimiento() {
        return lugarDeNacimiento;
    }

    public void setLugarDeNacimiento(String lugarDeNacimiento) {
        this.lugarDeNacimiento = lugarDeNacimiento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Date getFechaInspeccion() {
        return fechaInspeccion;
    }

    public void setFechaInspeccion(Date fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaVisitado() {
        return fechaVisitado;
    }

    public void setFechaVisitado(Date fechaVisitado) {
        this.fechaVisitado = fechaVisitado;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public double getFlatitud() {
        return flatitud;
    }

    public void setFlatitud(double flatitud) {
        this.flatitud = flatitud;
    }

    public double getFlongitud() {
        return flongitud;
    }

    public void setFlongitud(double flongitud) {
        this.flongitud = flongitud;
    }

    public String getGestion() {
        return gestion;
    }

    public void setGestion(String gestion) {
        this.gestion = gestion;
    }

    public String getLatitudCapturada() {
        return latitudCapturada;
    }

    public void setLatitudCapturada(String latitudCapturada) {
        this.latitudCapturada = latitudCapturada;
    }

    public String getLongitudCapturada() {
        return longitudCapturada;
    }

    public void setLongitudCapturada(String longitudCapturada) {
        this.longitudCapturada = longitudCapturada;
    }
}
