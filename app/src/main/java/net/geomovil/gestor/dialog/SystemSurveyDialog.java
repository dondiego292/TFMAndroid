package net.geomovil.gestor.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import net.geomovil.gestor.R;

import org.apache.log4j.Logger;

public class SystemSurveyDialog extends DialogFragment{
    private final Logger log = Logger.getLogger(SystemSurveyDialog.class.getSimpleName());
    private static final String SURVEY_ID = "survey_id";
    private static final String SURVEY_NAME = "survey_name";
    private static final String SURVEY_STATUS = "survey_status";

    private SystemSurveyDialogListener listener;

    /**
     * Inicializa el dialogo
     * @param survey_id id de la encuesta
     * @param name nombre de la encuesta
     * @param status estado de la encuesta
     * @return
     */
    public static SystemSurveyDialog newInstance(int survey_id, String name, int status) {
        SystemSurveyDialog fragment = new SystemSurveyDialog();
        Bundle args = new Bundle();
        args.putInt(SURVEY_ID, survey_id);
        args.putInt(SURVEY_STATUS, status);
        args.putString(SURVEY_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_system_survey, null);
        ((TextView)v.findViewById(R.id.txt_survey_name)).setText(getArguments().getString(SURVEY_NAME));
        String msg = "";
        switch (getArguments().getInt(SURVEY_STATUS)){
            case 0:
                msg = getString(R.string.install_survey_msg);
                break;
            case 1:
                msg = getString(R.string.uninstall_survey_msg);
                break;
            case 2:
                msg = getString(R.string.remove_survey_msg);
                break;
        }
        ((TextView)v.findViewById(R.id.txt_message)).setText(msg);
        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                processSurvey();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (SystemSurveyDialogListener) activity;
        }
        catch(Exception e){
            log.error("",e);
        }
    }

    /**
     * funcion que se ejecuta al dar clic en el boton aceptar del dialogo
     * enviando al listener del dialogo el id de la encuesta y notificandole
     * que el usuario debe procesar esta encuesta
     */
    public  void processSurvey(){
        listener.onDialogPositiveClick(this,getArguments().getInt(SURVEY_ID));
    }

    public interface SystemSurveyDialogListener{
        void onDialogPositiveClick(DialogFragment dialog, int survey_id);
    }
}
