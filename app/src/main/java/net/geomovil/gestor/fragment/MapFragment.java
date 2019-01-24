package net.geomovil.gestor.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.geomovil.gestor.BaseActivity;
import net.geomovil.gestor.MainActivity;
import net.geomovil.gestor.R;
import net.geomovil.gestor.calendar.CalendarDayHelper;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.helper.DistanceHelper;
import net.geomovil.gestor.helper.RutaPackageHelper;
import net.geomovil.gestor.interfaces.FragmentChangeInterface;
import net.geomovil.gestor.interfaces.LocationRecorderInterface;
import net.geomovil.gestor.interfaces.ProcessClientListener;
import net.geomovil.gestor.util.DirectionsJSONParser;
import net.geomovil.gestor.util.LinkMarkerLongClickListener;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, LocationRecorderInterface {

    private final Logger log = Logger.getLogger(MapFragment.class.getSimpleName());
    private GoogleMap map;
    private ArrayList markerPoints;
    private Context parent;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeoPoint user_location;
    private SharedPreferences sharedPref;

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentChangeInterface) {
            ((FragmentChangeInterface) context).changeFragment(this);
            ((MainActivity) context).changeKingOfMenu(MainActivity.MENU_MAP);
        }
        parent = context;
    }

    @Override
    public void onDetach() {
        parent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            } else if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            } else {
                log.error("No hay permiso para acceder al servicio de GPS");
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }
        getActivity().unregisterReceiver(myReceiver);
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(myReceiver, new IntentFilter(RutaPackageHelper.ROUTE_UPDATED));
    }


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                ArrayList<String> list = (ArrayList<String>) intent.getSerializableExtra("json");
                drawRoute(list);
                showClientPorVisitar();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        markerPoints = new ArrayList();
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(getLastPointLocation(), 13));
        map.getUiSettings().setZoomControlsEnabled(true);
        showClientPorVisitar();
        //showClientVisitados();
        startGPSActivity();
        //map.setOnMapClickListener(this);

        map.setOnMarkerDragListener(new LinkMarkerLongClickListener(lstMarkers) {
            @Override
            public void onLongClickListener(Marker marker) {
                if (Integer.parseInt(marker.getTag().toString()) > 0) {
                    ClientData c = getClientById(Integer.parseInt(marker.getTag().toString()));
                    if (c != null)
                        processClient(c);
                }

            }
        });
    }

    /**
     * funcion que se ejecuta al dar clic largo sobre el mapa
     *
     * @param client cliente seleccionado
     */
    private void processClient(ClientData client) {
        try {
            if (!client.isSaved() && parent != null && parent instanceof ProcessClientListener)
                ((ProcessClientListener) parent).processClient(client.getId());
            else if (client.isSaved() && parent instanceof BaseActivity) {
                ((ProcessClientListener) parent).processClient(client.getId());
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    /**
     * Retorna la ultima ubicacion conocida del gestor
     *
     * @return
     */
    public LatLng getLastPointLocation() {
        LatLng startPoint = new LatLng(-0.176238, -78.477931);
        try {
            SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
            double lat = Double.parseDouble(sharedPref.getString("user_latitud", "0"));
            double lon = Double.parseDouble(sharedPref.getString("user_longitude", "0"));
            if (lat != 0 && lon != 0)
                startPoint = new LatLng(lat, lon);
        } catch (Exception e) {
            log.error("", e);
        }
        return startPoint;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public boolean hasLocation() {
        return user_location != null;
    }

    @Override
    public GeoPoint getLocation() {
        return user_location;
    }


    protected ClientData getClientById(int id) {
        ClientData c = null;
        try {
            c = getDBHelper().getClientDao().queryForId(id);
        } catch (Exception e) {
            log.error("", e);
        }
        return c != null ? c : null;
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<JSONObject, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(JSONObject... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = new LinkedList<>();
                int i = 0;
                int iter = sharedPref.getInt("numero_iter", 10);
                for (JSONObject json : jsonData) {
                    while (i <= iter) {
                        if (json.has("data" + i)) {
                            JSONObject jsonObject = new JSONObject(json.get("data" + i).toString());
                            routes.addAll(parser.parse(jsonObject));
                        }
                        i++;
                    }
                }
                //routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            try {

                ArrayList points = null;
                PolylineOptions lineOptions = null;
                MarkerOptions markerOptions = new MarkerOptions();
                String distance = "";
                String duration = "";

                if (result.size() < 1) {
                    Toast.makeText(getActivity(), "No Points", Toast.LENGTH_SHORT).show();
                    return;
                }

                lineOptions = new PolylineOptions();
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList();
                    List<HashMap<String, String>> path = result.get(i);
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        if (j == 0) { // Get distance from the list
                            distance = (String) point.get("distance");
                            continue;
                        } else if (j == 1) { // Get duration from the list
                            duration = (String) point.get("duration");
                            continue;
                        }
                        if (point.containsKey("lat") || point.containsKey("lng")) {
                            double lat = Double.parseDouble(point.get("lat"));
                            double lng = Double.parseDouble(point.get("lng"));
                            LatLng position = new LatLng(lat, lng);
                            points.add(position);
                        }
                    }
                    lineOptions.addAll(points);
                    lineOptions.width(10);
                    lineOptions.color(Color.RED);
                }
                map.addPolyline(lineOptions);

            } catch (Exception e) {
                log.error("", e);
            }
        }

    }

    private DatabaseHelper db;

    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return db;
    }

    private ItemizedOverlayWithFocus<OverlayItem> no_visited_items;

    /**
     * Muestra en el mapa los clientes pendientes por visitar
     */
    private List<Marker> lstMarkers;

    private void showClientPorVisitar() {
        try {
            QueryBuilder<ClientData, Integer> queryBuilder = getDBHelper().getClientDao().queryBuilder();
            queryBuilder.where()
                    .eq(ClientData.MOVILSTATUS, 0)
                    .and()
                    .ne(ClientData.LATITUD, "0.0")
                    .and()
                    .eq(ClientData.FECHAINSPECCION, getSelectedDate());
            List<ClientData> clients = queryBuilder.query();
            lstMarkers = new ArrayList<>();
            for (final ClientData c : clients) {
                int icon = R.drawable.user_marker_asig;
                Marker m = map.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(c.getLatitud()), Double.parseDouble(c.getLongitud())))
                        .title(c.getNombre())
                        .snippet(c.getDireccion())
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker_auto))
                        .icon(BitmapDescriptorFactory.fromResource(icon))
                        .anchor(0.5f, 0.5f));
                m.setDraggable(true);
                m.setTag(c.getId());
                lstMarkers.add(m);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private Date getSelectedDate(){
        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
        String fecha = sharedPref.getString("fecha_inspeccion",null);
        if(fecha == null){
            fecha = CalendarDayHelper.get(CalendarDay.today());
        }
        return CalendarDayHelper.getDate(fecha);
    }





    public void startGPSActivity() {
        if (locationManager == null) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    //log.info("location change: " + location.getLatitude() + " - " + location.getLongitude());
                    GeoPoint geoPoint = new GeoPoint(location);
                    updateLocation(geoPoint);
                }

                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, locationListener);
                } else if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, locationListener);
                } else {
                    log.error("No hay permiso para acceder al servicio de GPS");
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 10, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, locationListener);
            }
        }
    }

    /**
     * Actualiza la localizacion obtenida desde el GPS
     *
     * @param p localizacion del GPS
     */
    public void updateLocation(GeoPoint p) {
        if (p != null) {
            if (user_location == null) {
                user_location = p;
                showLocationInMap();
            } else {
                double distance = (DistanceHelper.distance(p, user_location) * 1000);
                if (distance > 20) {
                    user_location = p;
                    showLocationInMap();
                }
            }
        }
    }

    private Marker marker;

    private void showLocationInMap() {

        if (marker != null)
            marker.remove();
        LatLng mCustomerLatLng = new LatLng(user_location.getLatitude(), user_location.getLongitude());
        MarkerOptions options = new MarkerOptions();
        options.position(mCustomerLatLng);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location));
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(user_location.getLatitude(), user_location.getLongitude()));
        //CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);
        map.moveCamera(center);
        //map.animateCamera(zoom);
        marker = map.addMarker(options);

        /*marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(user_location.getLatitude(), user_location.getLongitude()))
                .title(getResources().getString(R.string.your_position))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location))
                .anchor(0.5f, 0.5f));
        marker.setTag(0);*/

        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_latitud", user_location.getLatitude() + "");
        editor.putString("user_longitude", user_location.getLongitude() + "");
        editor.commit();
    }

    /*public void drawRoute(String url) {
        //DownloadTask downloadTask = new DownloadTask();
        //downloadTask.execute(url);
        ParserTask parserTask = new ParserTask();
        parserTask.execute(url);
    }*/

    public void drawRoute(ArrayList<String> url) {
        ParserTask parserTask = new ParserTask();
        JSONObject data = new JSONObject();
        for (int i = 0; i < url.size(); i++) {
            try {
                data.put("data" + String.valueOf(i), url.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        parserTask.execute(data);
    }

}
