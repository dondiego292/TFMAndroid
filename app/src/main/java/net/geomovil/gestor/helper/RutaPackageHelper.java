package net.geomovil.gestor.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.j256.ormlite.stmt.QueryBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import net.geomovil.gestor.calendar.CalendarDayHelper;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;

import org.apache.log4j.Logger;
import org.osmdroid.util.GeoPoint;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RutaPackageHelper {

    private final static Logger log = Logger.getLogger(RutaPackageHelper.class.getSimpleName());
    public static final String ROUTE_UPDATED = "net.geomovil.gestor.rutaupdated";

    public static final int NUMBER_OF_WAYPOINTS = 22;
    private static DatabaseHelper db;
    private static Context context;
    private static GeoPoint gestor_location;

    public static List<RutaPackage> getRutaPackages(DatabaseHelper db, Context context, GeoPoint gestor_location){
        List<RutaPackage> packages = new LinkedList<>();
        RutaPackageHelper.db = db;
        RutaPackageHelper.context = context;
        RutaPackageHelper.gestor_location = gestor_location;
        try{
            List<ClientData> clients = getClientsToVisit();
            if(clients.size() > 0){
                double[] distances = new double[clients.size()];
                int[] orders = new int[clients.size()];
                for (int i = 0; i < clients.size(); i++) {
                    distances[i] = DistanceHelper.distance(gestor_location,clients.get(i).getLocation());
                    orders[i] = i;
                }
                // ordeno por distancias lso clientes
                for (int i = 0; i < clients.size()-1; i++) {
                    for (int j = i+1; j < clients.size(); j++) {
                        if(distances[i] > distances[j]){
                            double d = distances[i];
                            distances[i] = distances[j];
                            distances[j] = d;

                            int o = orders[i];
                            orders[i] = orders[j];
                            orders[j] = o;
                        }
                    }
                }
                for (int i = 0; i < orders.length; i=i) {
                    //tomo el punto de inicio del paquete
                    GeoPoint start = i==0 ? RutaPackageHelper.gestor_location : packages.get(packages.size()-1).getLast();
                    // creo el objeto paquete
                    RutaPackage rutaPackage = new RutaPackage(start);
                    // adiciono al paquete cada uno de los paquetes
                    while (rutaPackage.size() <= NUMBER_OF_WAYPOINTS && i < orders.length){
                        rutaPackage.add(clients.get(orders[i]).getLocation(), clients.get(orders[i++]).getId());
                    }
                    packages.add(rutaPackage);
                }

            }
        }catch (Exception e){
            log.error("",e);
        }
        return packages;
    }

    /**
     * Retorna el listado de clientes con coordenadas que deben ser visitados el dia {date}
     * @return
     */
    private static List<ClientData> getClientsToVisit() {
        List<ClientData> clients = new LinkedList<>();
        try{

            QueryBuilder<ClientData, Integer> queryBuilder = db.getClientDao().queryBuilder();
            queryBuilder.where()
                    .eq(ClientData.MOVILSTATUS, 0)
                    .and()
                    .ne(ClientData.LATITUD, "0.0")
                    .and()
                    .eq(ClientData.FECHAINSPECCION, getSelectedDate());
            clients = queryBuilder.query();
        }catch (Exception e){
            log.error("",e);
        }
        return clients;
    }

    private static Date getSelectedDate(){
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
        String fecha = sharedPref.getString("fecha_inspeccion",null);
        if(fecha == null){
            fecha = CalendarDayHelper.get(CalendarDay.today());
        }
        return CalendarDayHelper.getDate(fecha);
    }
}
