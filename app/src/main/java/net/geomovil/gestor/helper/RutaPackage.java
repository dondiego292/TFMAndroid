package net.geomovil.gestor.helper;

import org.apache.commons.lang3.StringUtils;
import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;
import java.util.List;

public class RutaPackage {
    protected List<GeoPoint> points;
    protected List<Integer> clients;

    protected GeoPoint start;

    public RutaPackage(List<GeoPoint> points, GeoPoint start) {
        this.points = points;
        this.start = start;
    }

    public RutaPackage(GeoPoint start) {
        this.start = start;
        this.points = new LinkedList<>();
        this.clients = new LinkedList<>();
    }

    /**
     * Adiciona un nuevo cliente al paquete
     *
     * @param p   punto de geolocalizacion
     * @param cid cliente id
     */
    public void add(GeoPoint p, int cid) {
        clients.add(cid);
        points.add(p);
    }

    /**
     * Retorna el ID del cliente que se encuentra en la posicion {pos} del paquete
     *
     * @param pos posicion del cliente
     * @return
     */
    public int getClient(int pos) {
        return clients.get(pos);
    }

    public int clientSize() {
        return clients.size();
    }

    public int size() {
        return points.size();
    }

    public GeoPoint getLast() {
        return points.get(points.size() - 1);
    }

    public String getURL() {
        GeoPoint end = getLast();
        String url = "&origin=" + start.getLatitude() + "," + start.getLongitude() + "&destination=" + end.getLatitude() + "," + end.getLongitude();
        List<String> pts = new LinkedList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            GeoPoint p = points.get(i);
            pts.add(p.getLatitude() + "," + p.getLongitude());
        }
        url += "&waypoints=optimize:true|" + StringUtils.join(pts, "|");
        return url;
    }
}
