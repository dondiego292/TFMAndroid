package net.geomovil.gestor.interfaces;

import org.osmdroid.util.GeoPoint;

public interface LocationRecorderInterface {
    boolean hasLocation();
    GeoPoint getLocation();
}
