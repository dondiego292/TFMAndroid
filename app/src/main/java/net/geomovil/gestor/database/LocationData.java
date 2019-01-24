package net.geomovil.gestor.database;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable(tableName = "location_data")
public class LocationData implements Serializable {
    private static final long serialVersionUID = -3441664033073600838L;
    public static final String ID = "id";
    public static final String ACCURACY = "accuracy";
    public static final String ALTITUDE = "altitude";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String DATE_TIME = "start_date";
    public static final String STATUS = "status";
    public static final String UUID = "uuid";

    @DatabaseField(generatedId = true, columnName = ID)
    public int id;

    @DatabaseField(columnName = UUID)
    private String uuid;

    @DatabaseField(columnName = ACCURACY)
    private float  accuracy;

    @DatabaseField(columnName = ALTITUDE)
    private double altitude;

    @DatabaseField(columnName = LATITUDE)
    private double latitude;

    @DatabaseField(columnName = LONGITUDE)
    private double longitude;

    @DatabaseField(columnName = START_TIME)
    private long start_time;

    @DatabaseField(columnName = END_TIME)
    private long end_time;

    @DatabaseField(columnName = DATE_TIME)
    private Date start_date;

    /**
     * Estado de la ubicacion
     * 0 - en proceso
     * 1 - cerrada
     * 2 - enviada
     */
    @DatabaseField(columnName = STATUS)
    private int status;

    public LocationData() {
        status = 0;
        uuid = java.util.UUID.randomUUID().toString();
    }

    public LocationData(Location location) {
        altitude = location.getAltitude();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        start_time = System.currentTimeMillis();
        end_time = System.currentTimeMillis();
        start_date = new Date(start_time);
        status = 0;
        uuid = java.util.UUID.randomUUID().toString();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setEndTime(long end_time) {
        this.end_time = end_time;
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getStartTime() {
        return start_time;
    }

    public long getEndTime() {
        return end_time;
    }

    public Date getStartDate() {
        return start_date;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", accuracy=" + accuracy +
                ", altitude=" + altitude +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", start_time=" + start_time +
                ", end_time=" + end_time +
                ", start_date=" + start_date +
                ", status=" + status +
                '}';
    }
}
