package net.geomovil.gestor.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.geomovil.gestor.MainActivity;
import net.geomovil.gestor.R;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.database.LocationData;
import net.geomovil.gestor.database.LogEvent;
import net.geomovil.gestor.database.User;
import net.geomovil.gestor.helper.DistanceHelper;
import net.geomovil.gestor.util.DeviceTool;
import net.geomovil.gestor.util.FolderInspector;
import net.geomovil.gestor.util.NetApi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class TrackingService extends Service {
    private final Logger log = Logger.getLogger(TrackingService.class.getSimpleName());

    private static final int NOTIFICATION_ID = 155;
    public LocationManager locationManager;
    private Handler loadEventServiceHandler;
    private Handler logEventServiceHandler;

    private DatabaseHelper db;
    private static int TIME_TO_SEND = 2 * 60 * 1000;
    private static int TIME_TO_SEND_LOG = 3 * 60 * 1000;
    //private static int GPS_REQUEST_TIME = 5 * 1000;
    private static int GPS_REQUEST_TIME = 1 * 60 * 1000;
    //private static int OFFLINE_LIMIT_TIME = 10 * 60 * 1000;

    //private boolean active = false;


    @Override
    public void onCreate() {
        //Toast.makeText(this, "Servicio creado", Toast.LENGTH_SHORT).show();
        log.info("Servicio creado");
        configureLog();

        checkServiceToUpdate();
        checkServiceLog();

        checkGPSEnabled();

        /*Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);*/
        //active = false;
    }

    private void checkServiceLog() {
        logEventServiceHandler = new Handler();
        logEventServiceHandler.post(logEventServiceRunnable);
    }

    private Runnable logEventServiceRunnable = new Runnable() {
        @Override
        public void run() {
            List<LogEvent> logs = getDBHelper().getLogsToSend(100);
            if (logs.size() > 0 && (logsToSend == null || logsToSend.isEmpty())) {
                logsToSend = new Stack<>();
                for (int i = logs.size() - 1; i >= 0; i--)
                    logsToSend.add(logs.get(i));
                TrackingService.this.sendLastLogsEvent();
            }
            //log.info("enviando LOGS");
            logEventServiceHandler.postDelayed(logEventServiceRunnable, TIME_TO_SEND_LOG);
        }
    };

    private Stack<LogEvent> logsToSend;

    private void sendLastLogsEvent() {
        if (logsToSend.empty() == false) {
            sendLogsData(logsToSend.pop());
        }
    }

    private void sendLogsData(final LogEvent data) {
        User user = getDBHelper().getUser();
        if (user != null) {
            //log.info("enviando log con id: " + data.getId());
            HashMap<String, String> params = new HashMap<String, String>();
            //params.put("Key", user.getToken());
            params.put("IMEI", DeviceTool.getDeviceUniqueID(this));
            params.put("Tipo", "" + data.getTipo());
            params.put("Propietario", "" + data.getPropietario());
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("Fecha", formatter.format(data.getFecha()));
            params.put("UUID", data.getUuuid());
            params.put("Mensaje", data.getMensaje());
            params.put("Bateria", batteryLevel() + "");
            params.put("Gestor", user.getGestorID());
            String url = NetApi.URL + "movil/event/log?token=" + user.getToken();
            log.info("url: " + url);
            log.info("params: " + params.toString());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            log.info(String.format("Respuesta del servidor enviado log %s", response.toString()));
                            if (response.has("error")) {
                                log.error("Server request error logs " + response.toString());
                                logsToSend.clear();
                            } else {
                                closeLogEvent(data);
                                sendLastLogsEvent();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error("Server request error logs", error);
                    logsToSend.clear();
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    NetApi.TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    private int batteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        return (int) (batteryPct * 100);
    }

    private void closeLogEvent(LogEvent data) {
        try {
            data.setEnviado(true);
            getDBHelper().getLogEventDao().update(data);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void checkServiceToUpdate() {
        loadEventServiceHandler = new Handler();
        loadEventServiceHandler.post(loadEventServiceRunnable);
    }

    private Runnable loadEventServiceRunnable = new Runnable() {
        @Override
        public void run() {

            List<LocationData> locations = getDBHelper().getLocationToSend(100);
            if (locations.size() > 0 && (locationsToSend == null || locationsToSend.isEmpty())) {
                locationsToSend = new Stack<>();
                for (int i = locations.size() - 1; i >= 0; i--)
                    locationsToSend.add(locations.get(i));
                TrackingService.this.sendLastLocationData();
            }
            if (isMyServiceRunning(TrackingService.class)) {
                //log.info("El servicio de Tracking esta corriendo");
            } else {
                //GPSService.this.startTrackingService();
                //log.info("El servicio de Tracking esta DETENIDO");
            }
            //log.info("enviando datos");
            loadEventServiceHandler.postDelayed(loadEventServiceRunnable, TIME_TO_SEND);
        }
    };

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private Stack<LocationData> locationsToSend;

    private void sendLastLocationData() {
        if (locationsToSend.empty() == false) {
            sendLocationData(locationsToSend.pop());
        }
    }

    private void sendLocationData(final LocationData data) {
        User user = getDBHelper().getUser();
        if (user != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            //params.put("Key", user.getToken());
            params.put("Device", DeviceTool.getDeviceUniqueID(this));
            params.put("Latitud", "" + data.getLatitude());
            params.put("Longitud", "" + data.getLongitude());
            params.put("UUID", data.getUuid());
            params.put("Altitud", "" + data.getAltitude());
            params.put("Accuracy", "" + data.getAccuracy());
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            params.put("StartDate", "" + formatter.format(data.getStartDate()));
            params.put("StartTime", "" + data.getStartTime());
            params.put("EndTime", "" + (data.getStatus() == 0 ? System.currentTimeMillis() : data.getEndTime()));
            params.put("Bateria", batteryLevel() + "");
            params.put("Gestor", user.getGestorID());
            String url = NetApi.URL + "movil/event/ruta?token=" + user.getToken();
            log.info("url: " + url);
            log.info("params: " + params.toString());
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            log.info(String.format("Respuesta del servidor enviada la ruta %s", response.toString()));
                            if (response.has("error")) {
                                log.error("Server request error ruta" + response.toString());
                                locationsToSend.clear();
                            } else {
                                closeLocation(data);
                                sendLastLocationData();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log.error("Server request error ruta", error);
                    locationsToSend.clear();
                }
            });

            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    NetApi.TIMEOUT,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
        }
    }

    private void closeLocation(LocationData data) {
        try {
            if (data.getStatus() == 1) {
                data.setStatus(2);
                getDBHelper().getLocationDao().update(data);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_REQUEST_TIME, 10, listener);
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_REQUEST_TIME, 0, listener);
                //return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_REQUEST_TIME, 10, listener);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_REQUEST_TIME, 0, listener);
        } catch (Exception e) {
            log.error("", e);
        }
        //requestGPSLocation();
        return START_STICKY;
    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            try {
                //log.info("onLocationChanged: " + location.toString());
                //log.info("getAccuracy: " + location.getAccuracy());
                if (location == null) {
                    closeLastLocation();
                    //log.info("la localizacion es null");
                } else if (location.getAccuracy() < 25) {
                    // log.info(String.format("Recibida: Latitud: %f  Longitud: %f Accuracy: %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
                    long time = System.currentTimeMillis();
                    LocationData lastlocation = getDBHelper().getLastLocation();
                    if (lastlocation == null)
                        getDBHelper().addNewLocation(location);
                    else {
                        //log.info("getSpeed: " + location.getSpeed());
                        //diferencia de tiempo entre a ultima ubicacion y la actual
                        long diff_time = time - lastlocation.getEndTime();
                        // diferencias de tiempos entre una localizacion y la siguiente
                        int times = (int) (diff_time / GPS_REQUEST_TIME);

                        // resto de la division en caso de que no sean i
                        // guales
                        int rest = (int) (diff_time % GPS_REQUEST_TIME);

                        GeoPoint last = new GeoPoint(lastlocation.getLatitude(), lastlocation.getLongitude());
                        GeoPoint act = new GeoPoint(location.getLatitude(), location.getLongitude());
                        double distance = (DistanceHelper.distance(last, act) * 1000);
                        //log.info("distance: " + distance);

                        //if (diff_time > OFFLINE_LIMIT_TIME || rest > 500) {
                        if ((/*diff_time > OFFLINE_LIMIT_TIME ||*/ rest > 500) && distance > 30) {
                            //log.info("entro if");
                            closeLastLocation();
                            getDBHelper().addNewLocation(location);
                            //log.info(String.format("Guardada: Latitud: %f  Longitud: %f Accuracy: %f", location.getLatitude(), location.getLongitude(), location.getAccuracy()));
                        } else {
                            //log.info("entro else");
                            lastlocation.setEndTime(System.currentTimeMillis());
                            getDBHelper().getLocationDao().update(lastlocation);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            closeLastLocation();
            //log.info(String.format("%s fue deshabilitado", provider));
            try {
                //getDBHelper().getLogEventDao().create(new LogEvent(String.format("%s FUE DESABILITADO", provider), 1, 2));
                if (provider.equalsIgnoreCase("GPS"))
                    getDBHelper().getLogEventDao().create(new LogEvent("GPS FUE DESABILITADO", 1, 2));
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            //log.info(String.format("%s es habilitado", provider));
            try {
                //getDBHelper().getLogEventDao().create(new LogEvent(String.format("%s FUE HABILITADO", provider), 1));
                if (provider.equalsIgnoreCase("GPS"))
                    getDBHelper().getLogEventDao().create(new LogEvent("GPS FUE HABILITADO", 1));
            } catch (Exception e) {
                log.error("", e);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    //log.info(String.format("%s %s", provider, "fuera de servicio"));
                    try {
                        getDBHelper().getLogEventDao().create(new LogEvent("GPS FUERA DE SERVICIO", 0, 2));
                        //getDBHelper().getLogEventDao().create(new LogEvent(String.format("%s %s", provider, "FUERA DE SERVICIO"), 0, 2));
                    } catch (Exception e) {
                        log.error("", e);
                    }

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //log.info(String.format("%s %s", provider, "temporalmente fuera de servicio"));
                    try {
                        getDBHelper().getLogEventDao().create(new LogEvent("GPS TEMPORALMENTE FUERA DE SERVICIO", 0, 2));
                        //getDBHelper().getLogEventDao().create(new LogEvent(String.format("%s %s", provider, "TEMPORALMENTE FUERA DE SERVICIO"), 0, 2));
                    } catch (Exception e) {
                        log.error("", e);
                    }
                    break;
                case LocationProvider.AVAILABLE:
                    //log.info(String.format("%s %s", provider, "disponible"));
                    break;
            }

        }
    };

    @Override
    public void onDestroy() {
        //log.info("El servicio fue detenido");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(listener);
            }
        } else {
            locationManager.removeUpdates(listener);
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intencion) {
        return null;
    }

    /**
     * Configuracion inicial del log del sistema
     */
    public void configureLog() {
        final LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(FolderInspector.getProjectFolder(this) + File.separator + "mobile.log");
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.configure();
    }

    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return db;
    }

    public void closeLastLocation() {
        try {
            getDBHelper().closeLastLocation();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private boolean checkGPSEnabled() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}

