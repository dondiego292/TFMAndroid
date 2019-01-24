package net.geomovil.gestor.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.gestor.R;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.database.Survey;
import net.geomovil.gestor.interfaces.ProcessClientListener;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class FreeSurveyDialog extends DialogFragment {
    private final Logger log = Logger.getLogger(FreeSurveyDialog.class.getSimpleName());

    private DatabaseHelper db;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context parent;

    /**
     * Inicializa el dialogo
     *
     * @return
     */
    public static FreeSurveyDialog newInstace() {
        Bundle args = new Bundle();
        FreeSurveyDialog dialog = new FreeSurveyDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_free_survey, null);
        try {
            mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view_free_survey);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new FreeSurveyListAdapter(getFreeSurvey());
            mRecyclerView.setAdapter(mAdapter);
        } catch (Exception e) {
            log.error("", e);
        }
        builder.setNegativeButton(getResources().getString(R.string.button_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        builder.setView(v);
        return builder.create();
    }

    private List<Survey> getFreeSurvey() {
        List<Survey> surveys = new LinkedList<>();
        try {
            QueryBuilder<Survey, Integer> queryBuilder = getDBHelper().getSurveyDao().queryBuilder();
            queryBuilder.where().eq(Survey.LIBRE, true)
                    .and().eq(Survey.INSTALADA, 1);
            surveys = queryBuilder.query();

        } catch (Exception e) {
            log.error("", e);
        }
        return surveys;
    }

    /*
     * funcion que se ejecuta al dar clic sobre la lista de encuestas libres
     *
     * @param survey encuesta seleccionada
     */
    private void processFreeSurvey(Survey survey) {
        try {
            if (parent != null && parent instanceof ProcessClientListener)
                ((ProcessClientListener) parent).processEspecifyFreeSurvey(survey.getWebId());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /*
     * instanciando la base de datos
     *
     * @return
     */
    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return db;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        parent = activity;
    }

    @Override
    public void onDetach() {
        parent = null;
        super.onDetach();
    }


    public class FreeSurveyListAdapter extends RecyclerView.Adapter<FreeSurveyListAdapter.ViewHolder> {
        private List<Survey> svs;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView txt_survey_name;
            public TextView txt_survey_description;

            public int position;
            public CardView _view;

            public ViewHolder(View v) {
                super(v);
                txt_survey_name = (TextView) v.findViewById(R.id.txt_survey_name);
                txt_survey_description = (TextView) v.findViewById(R.id.txt_survey_description);
                v.findViewById(R.id.layout_container_free_survey).setOnClickListener(this);
                _view = (CardView) v;
            }

            @Override
            public void onClick(View v) {
                processFreeSurvey(svs.get(position));
            }
        }

        public FreeSurveyListAdapter(List<Survey> svs) {
            this.svs = svs;
        }

        public void refreshData(List<Survey> svs) {
            this.svs = svs;
            notifyDataSetChanged();
        }

        @Override
        public FreeSurveyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.free_survey_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Survey s = svs.get(position);
            holder.txt_survey_name.setText(s.getNombre());
            holder.txt_survey_description.setText(s.getDescripcion());
            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return svs.size();
        }
    }
}

