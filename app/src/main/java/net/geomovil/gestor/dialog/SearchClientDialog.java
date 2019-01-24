package net.geomovil.gestor.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import net.geomovil.gestor.R;
import net.geomovil.gestor.interfaces.SearchClientListener;

import org.apache.log4j.Logger;

public class SearchClientDialog extends DialogFragment {
    private final Logger log = Logger.getLogger(SearchClientDialog.class.getSimpleName());
    protected static final String CRITERIA = "CRITERIA";
    protected SearchClientListener listener;
    protected EditText edt_criteria;

    public static SearchClientDialog newInstace(String criteria) {
        Bundle args = new Bundle();
        args.putString(CRITERIA, criteria);
        SearchClientDialog dialog = new SearchClientDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_search_client, null);
        edt_criteria = (EditText) v.findViewById(R.id.edt_search_criteria);
        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                filterData();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("app", Context.MODE_PRIVATE);
        String criteria = sharedPref.getString("filter_criteria", null);
        if (criteria != null) {
            EditText edt_search_criteria = (EditText) v.findViewById(R.id.edt_search_criteria);
            edt_search_criteria.setText(criteria);
            edt_search_criteria.setSelectAllOnFocus(true);
        }
        return builder.create();
    }

    /**
     * Metodo que se ejecuta cuando el usuario ha introducido el criterio de busqueda y
     * presiona el boton BUSCAR
     */
    private void filterData() {
        if (listener != null) {
            listener.filterData(edt_criteria.getText().toString());
        }
    }


    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof SearchClientListener) {
            listener = (SearchClientListener) activity;
        }
    }
}

