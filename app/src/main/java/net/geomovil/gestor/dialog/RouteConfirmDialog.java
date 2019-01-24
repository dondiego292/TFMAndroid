package net.geomovil.gestor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.geomovil.gestor.MainActivity;
import net.geomovil.gestor.R;
import net.geomovil.gestor.database.DatabaseHelper;

import org.apache.log4j.Logger;

public class RouteConfirmDialog extends DialogFragment {
    private final Logger log = Logger.getLogger(RouteConfirmDialog.class.getSimpleName());

    private CalculateRutaListener listener;
    protected Spinner spn_medios;
    String[] medios = {"VEHÍCULO", "CAMINANDO"};//,"BICICLETA" };
    String[] medios_value = {"driving", "walking", "bicycling"};
    public static RouteConfirmDialog newInstace() {
        Bundle args = new Bundle();
        RouteConfirmDialog dialog = new RouteConfirmDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_route_confirm, null);
        spn_medios = (Spinner) v.findViewById(R.id.spn_medios);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, medios);
        spn_medios.setAdapter(adapter);


        spn_medios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.rutaMode = medios_value[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(v);
        builder.setPositiveButton("CALCULAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null)
                    listener.calculateRoute();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                listener.calculateRoute();
            }
        });


        return builder.create();
    }

    /*@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CalculateRutaListener) {
            listener = (CalculateRutaListener) activity;
        }
    }*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CalculateRutaListener) {
            listener = (CalculateRutaListener) context;
        }
    }

    public interface CalculateRutaListener {
        void calculateRoute();
    }

    private DatabaseHelper db;

    /**
     * instancia de la base de datos
     *
     * @return
     */
    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return db;
    }
}