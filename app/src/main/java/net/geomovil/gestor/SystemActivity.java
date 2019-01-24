package net.geomovil.gestor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import net.geomovil.gestor.database.QuestionRule;
import net.geomovil.gestor.database.QuestionType;
import net.geomovil.gestor.database.Survey;
import net.geomovil.gestor.database.SurveyCatalog;
import net.geomovil.gestor.database.SurveyCatalogHijo;
import net.geomovil.gestor.database.SurveyCatalogPadre;
import net.geomovil.gestor.database.SurveyQuestion;
import net.geomovil.gestor.database.User;
import net.geomovil.gestor.dialog.SystemSurveyDialog;
import net.geomovil.gestor.util.NetApi;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SystemActivity extends BaseActivity implements View.OnClickListener,
        SystemSurveyDialog.SystemSurveyDialogListener {

    private final Logger log = Logger.getLogger(SystemActivity.class.getSimpleName());
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system);
        log.info("Iniciada la pantalla de sistema");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle(getString(R.string.ecuestas_name));
        FloatingActionButton btn_update = findViewById(R.id.btn_update_system);
        btn_update.setOnClickListener(this);

        try {
            mRecyclerView = findViewById(R.id.my_recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new SurveyListAdapter(getDBHelper().getSurveyDao().queryForAll());
            mRecyclerView.setAdapter(mAdapter);

        } catch (Exception e) {
            log.error("", e);
            showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * funcion que se ejecuta al dar clic sobre la encuesta
     *
     * @param survey encuesta seleccionada
     */
    private void processSurvey(Survey survey) {
        DialogFragment dialog = SystemSurveyDialog.newInstance(survey.getId(), survey.getNombre(), survey.getInstalada());
        dialog.show(getSupportFragmentManager(), "survey_dialog");
    }

    /**
     * Este metodo se ejecuta cuando el usuario selecciona del dialogo la
     * opcion aceptar
     *
     * @param dialog    dialogo del sistema
     * @param survey_id id de la encuesta
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int survey_id) {
        try {
            Survey s = getDBHelper().getSurveyDao().queryForId(survey_id);
            if (s.EstadoInstalada()) {
                unInstallSurvey(s);
            } else if (s.EstadoPorInstalar()) {
                installSurvey(s);
            } else if (s.EstadoObsoleta()) {
                removeSurvey(s);
            }
        } catch (Exception e) {
            log.error("", e);
            showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
        }
    }

    /**
     * Elimina la encuesta del sistema
     *
     * @param s encuesta a eliminar
     */
    private void removeSurvey(Survey s) {
        try {
            getDBHelper().getSurveyQuestionDao().delete(s.getPreguntas());
            getDBHelper().getSurveyCatalogDao().delete(s.getCatalogos());
            getDBHelper().getSurveyCatalogPadreDao().delete(s.getCatalogosPadres());
            getDBHelper().getSurveyCatalogHijoDao().delete(s.getCatalogosHijos());
            getDBHelper().getSurveyCatalogDao().delete(s.getCatalogos());
            getDBHelper().getSurveyDao().delete(s);
            ((SurveyListAdapter) mAdapter).refreshData(getDBHelper().getSurveyDao().queryForAll());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Desinstala la encuesta del sistema
     *
     * @param s encuesta a desinstalar
     */
    private void unInstallSurvey(Survey s) {
        try {
            /*DeleteBuilder<Etiqueta, Integer> deleteBuilder = getDBHelper().getEtiquetaDao().deleteBuilder();
            deleteBuilder.where().eq(Etiqueta.ENCUESTA, s.getEtiqueta());
            deleteBuilder.delete();*/

            s.setInstalada(0);
            getDBHelper().getSurveyQuestionDao().delete(s.getPreguntas());
            getDBHelper().getSurveyCatalogDao().delete(s.getCatalogos());
            getDBHelper().getSurveyCatalogPadreDao().delete(s.getCatalogosPadres());
            getDBHelper().getSurveyCatalogHijoDao().delete(s.getCatalogosHijos());
            getDBHelper().getSurveyDao().update(s);
            ((SurveyListAdapter) mAdapter).refreshData(getDBHelper().getSurveyDao().queryForAll());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Instala la encuesta
     *
     * @param s encuesta a instalar
     */
    private void installSurvey(Survey s) {
        try {
            User user = checkUser();
            if (user != null) {
                String url = NetApi.URL + "movil/encuesta/data/" + s.getWebId() + "?token=" + user.getToken();
                //log.info("url: " + url);
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //log.info("response: " + response.toString());
                                try {
                                    if (response.has("ok") && !response.getBoolean("ok")) {
                                        log.error("Server request error " + response.toString());
                                        showMessage(SweetAlertDialog.ERROR_TYPE,
                                                getString(R.string.auth_error),
                                                response.getString("mensaje"));
                                    } else {
                                        //log.info("respuesta " + response.toString());
                                        installSurvey(response);
                                    }
                                } catch (Exception e) {
                                    log.error(e);
                                    showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
                                } finally {
                                    hideProgressDialog();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                log.error(volleyError.toString());
                                hideProgressDialog();
                                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", volleyError.getMessage());
                            }
                        }
                );
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                        NetApi.TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                showProgressDialog(getString(R.string.installing_survey));
                NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Instala en el sistema la encuesta que llega por parametro
     * Esta encuesta viene del servidor y no contiene solo los datos
     * generales, sino que tambien contiene los maestros que usa asi
     * como el listado de preguntas
     *
     * @param apiEncuesta ApiEncuesta - Estructura devuelta por el servidor mientras se instala
     *                    la encuesta
     */
    private void installSurvey(JSONObject apiEncuesta) {
        try {
            JSONObject encuesta = apiEncuesta.getJSONObject("encuesta");
            List<Survey> svs = getDBHelper().getSurveyDao().queryForEq(Survey.WEBID, encuesta.getString("_id"));
            if (svs.size() > 0) {
                Survey survey = svs.get(0);
                JSONArray preguntas = apiEncuesta.getJSONArray("preguntas");
                for (int i = 0; i < preguntas.length(); i++) {
                    getDBHelper().getSurveyQuestionDao().create(new SurveyQuestion(survey, preguntas.getJSONObject(i)));
                }
                deletePadre();
                JSONArray catalogo = apiEncuesta.getJSONArray("padres");
                for (int i = 0; i < catalogo.length(); i++) {
                    getDBHelper().getSurveyCatalogPadreDao().create(new SurveyCatalogPadre(survey, catalogo.getJSONObject(i)));
                }
                deleteHijo();
                JSONArray catalogoHijo = apiEncuesta.getJSONArray("hijos");
                for (int i = 0; i < catalogoHijo.length(); i++) {
                    getDBHelper().getSurveyCatalogHijoDao().create(new SurveyCatalogHijo(survey, catalogoHijo.getJSONObject(i)));
                }
                if (apiEncuesta.has("reglas")) {
                    JSONArray rules = apiEncuesta.getJSONArray("reglas");
                    for (int i = 0; i < rules.length(); i++) {
                        getDBHelper().getQuestionRuleDao().create(new QuestionRule(rules.getJSONObject(i), getDBHelper().getSurveyQuestionDao()));
                    }
                }

                getDBHelper().getSurveyCatalogDao().delete(survey.getCatalogos());
                List<SurveyCatalogPadre> test = getDBHelper().getSurveyCatalogPadreDao().queryForAll();
                for (SurveyCatalogPadre t : test) {
                    List<SurveyCatalogHijo> hijos = getHijos(t.getWebId());
                    for (SurveyCatalogHijo h : hijos) {
                        //int webId, String nombre, String codigo, String valor, String nombrePadre, String codigoPadre, Survey encuesta
                        getDBHelper().getSurveyCatalogDao().create(new SurveyCatalog(t.getWebId(), t.getNombre(), h.getCodigo(), h.getValor(), "", "", survey));
                        //"ID":0,"Nombre":"OPCIÓN DE CONTACTO","Codigo":"1","Valor":"CONTACTADO","NombrePadre":"","CodigoPadre":"","Eliminado":false}
                    }


                }


                /*if (apiEncuesta.has("Etiquetas")) {
                    JSONArray surveys = apiEncuesta.getJSONArray("Etiquetas");
                    List<Integer> ids = new ArrayList<>();
                    for (int i = 0; i < surveys.length(); i++) {
                        JSONObject s = surveys.getJSONObject(i);
                        createEtiquetas(s);
                        ids.add(s.getInt("Id"));
                    }
                    if (ids.size() > 0)
                        deleteEtiqueta(ids, survey.getEtiqueta());
                }*/

                survey.setInstalada(1);
                getDBHelper().getSurveyDao().update(survey);
                ((SurveyListAdapter) mAdapter).refreshData(getDBHelper().getSurveyDao().queryForAll());
                List<SurveyCatalog> catalogs = getDBHelper().getSurveyCatalogDao().queryForAll();

                deletePadre();
                deleteHijo();
            } else {
                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", "No se encontro la encuesta a instalar");
            }
        } catch (Exception e) {
            log.error("", e);
            showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
        }
    }

    private List<SurveyCatalogHijo> getHijos(String catalogoPadre) {
        List<SurveyCatalogHijo> hijos = new LinkedList<>();
        try {
            Dao<SurveyCatalogHijo, Integer> hijoDao = getDBHelper().getSurveyCatalogHijoDao();
            QueryBuilder<SurveyCatalogHijo, Integer> queryBuilder = hijoDao.queryBuilder();
            Where<SurveyCatalogHijo, Integer> where = queryBuilder.where();
            where.eq(SurveyCatalogHijo.CATALOGOPADRE, catalogoPadre);
            queryBuilder.orderBy(SurveyCatalogHijo.NOMBRE, true);
            //log.info("query: " + queryBuilder.prepareStatementString());
            hijos = queryBuilder.query();
        } catch (Exception e) {
            log.error("", e);
        }
        return hijos;
    }

    private void deleteHijo() {
        try {
            Dao<SurveyCatalogHijo, Integer> questionTypeDao = getDBHelper().getSurveyCatalogHijoDao();
            QueryBuilder<SurveyCatalogHijo, Integer> queryBuilder = questionTypeDao.queryBuilder();
            queryBuilder.where().gt("id", 0);
            for (SurveyCatalogHijo q : queryBuilder.query())
                questionTypeDao.delete(q);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void deletePadre() {
        try {
            Dao<SurveyCatalogPadre, Integer> questionTypeDao = getDBHelper().getSurveyCatalogPadreDao();
            QueryBuilder<SurveyCatalogPadre, Integer> queryBuilder = questionTypeDao.queryBuilder();
            queryBuilder.where().gt("id", 0);
            for (SurveyCatalogPadre q : queryBuilder.query())
                questionTypeDao.delete(q);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void deleteQuestionType() {
        try {
            Dao<QuestionType, Integer> questionTypeDao = getDBHelper().getQuestionTypeDao();
            QueryBuilder<QuestionType, Integer> queryBuilder = questionTypeDao.queryBuilder();
            queryBuilder.where().gt("id", 0);
            for (QuestionType q : queryBuilder.query())
                questionTypeDao.delete(q);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void onClick(View btn) {
        try {
            User user = checkUser();
            if (user != null) {
                String url = NetApi.URL + "movil/encuesta/" + user.getGestorID() + "?token=" + user.getToken();
                //log.info("url: " + url);
                //String url = NetApi.getURL(this)+"ApiEncuestas/GetEncuestas?token="+ user.getToken();
                //String url = NetApi.getURL(this)+"api/ApiEncuesta/movil/token/"+ user.getToken();
                JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject systemData) {
                                try {
                                    //log.info("response: " + systemData.toString());
                                    JSONArray surveys = systemData.getJSONArray("encuestas");
                                    List<String> uuids = new ArrayList<>();
                                    for (int i = 0; i < surveys.length(); i++) {
                                        JSONObject s = surveys.getJSONObject(i);
                                        createSurvey(s);
                                        uuids.add("'" + s.getString("uuid") + "'");
                                    }
                                    String sql = "UPDATE survey SET " + Survey.INSTALADA + " = 2 ";
                                    if (uuids.size() > 0)
                                        sql += "WHERE " + Survey.UUID + " NOT IN (" + TextUtils.join(",", uuids) + ");";
                                    else
                                        sql += "WHERE id > 0;";
                                    getDBHelper().getSurveyDao().updateRaw(sql);
                                    ((SurveyListAdapter) mAdapter).refreshData(getDBHelper().getSurveyDao().queryForAll());

                                    deleteQuestionType();
                                    JSONArray tipoPreguntas = systemData.getJSONArray("tipoPreguntas");
                                    for (int i = 0; i < tipoPreguntas.length(); i++) {
                                        getDBHelper().getQuestionTypeDao().create(new QuestionType(tipoPreguntas.getJSONObject(i)));
                                    }

                                    /*JSONArray motivos = systemData.getJSONArray("CatalogoMotivos");
                                    DeleteBuilder<MotivoFueraServicio, Integer> deleteBuilder = getDBHelper().getMotivoDao().deleteBuilder();
                                    deleteBuilder.where().gt("id", 0);
                                    deleteBuilder.delete();
                                    for (int i = 0; i < motivos.length(); i++) {
                                        JSONObject motivo = motivos.getJSONObject(i);
                                        getDBHelper().getMotivoDao().create(new MotivoFueraServicio(motivo.getString("NombreCorto")));
                                    }*/
                                } catch (Exception e) {
                                    log.error(e);
                                    showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", e.getMessage());
                                } finally {
                                    hideProgressDialog();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                log.error(volleyError.toString());
                                hideProgressDialog();
                                showMessage(SweetAlertDialog.ERROR_TYPE, "ERROR", volleyError.getMessage());
                            }
                        }
                );
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                        NetApi.TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                showProgressDialog(getString(R.string.loading_system_data));
                NetApi.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /***
     * Ingresa la encuesta en la base de datos del dispositivo en caso de
     * que esta no exista ya en el sistema. Si la encuesta existe actualiza
     * los datos de la misma mantieniendo el estado que tiene
     * @param survey Objeto JSONObject que representa la encuesta
     * @throws Exception
     */
    private void createSurvey(JSONObject survey) throws Exception {
        Dao<Survey, Integer> SurveyDAO = getDBHelper().getSurveyDao();
        String UUID = survey.getString("uuid");
        List<Survey> svs = SurveyDAO.queryForEq(Survey.UUID, UUID);
        if (svs.size() == 0) {
            SurveyDAO.create(new Survey(survey));
        } else {
            SurveyDAO.update(svs.get(0).update(survey));
        }
    }

    /**
     * Adaptador para mostrar la lista de las encuestas
     */
    public class SurveyListAdapter extends RecyclerView.Adapter<SurveyListAdapter.ViewHolder> {
        private List<Survey> svs;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView txt_name;
            public TextView txt_description;
            public TextView txt_type;
            public TextView txt_survey_free;
            public ImageView img_estado;
            public int position;

            public ViewHolder(View v) {
                super(v);
                txt_name = (TextView) v.findViewById(R.id.txt_survey_name);
                txt_description = (TextView) v.findViewById(R.id.txt_survey_description);
                txt_type = (TextView) v.findViewById(R.id.txt_survey_type);
                txt_survey_free = (TextView) v.findViewById(R.id.txt_survey_free);
                img_estado = (ImageView) v.findViewById(R.id.img_survey_status);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                processSurvey(svs.get(position));
            }
        }

        public SurveyListAdapter(List<Survey> svs) {
            this.svs = svs;
        }


        public void refreshData(List<Survey> svs) {
            this.svs = svs;
            notifyDataSetChanged();
        }

        @Override
        public SurveyListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.survey_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Survey s = svs.get(position);
            holder.txt_name.setText(s.getNombre());
            holder.txt_type.setText(s.getTipo());
            holder.txt_description.setText(s.getDescripcion());
            holder.img_estado.setImageResource(s.getImgEstado());
            if (s.isLibre()) {
                holder.txt_survey_free.setText(R.string.survey_free);
                holder.txt_survey_free.setVisibility(View.VISIBLE);
            } else {
                holder.txt_survey_free.setVisibility(View.GONE);
            }

            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return svs.size();
        }
    }
}
