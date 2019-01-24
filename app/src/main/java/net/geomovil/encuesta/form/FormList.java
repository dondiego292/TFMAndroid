package net.geomovil.encuesta.form;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.gestor.R;
import net.geomovil.gestor.SurveyActivity;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.database.SurveyCatalog;
import net.geomovil.gestor.database.SurveyQuestion;
import net.geomovil.gestor.util.TextHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FormList extends FormWidget implements OnItemSelectedListener {
    private final Logger log = Logger.getLogger(FormList.class.getSimpleName());
    protected int encuesta_id;
    protected String maestro_col;
    protected Spinner spn;
    protected ArrayAdapter<String> adapter;
    protected List<String> values;
    protected List<String> codes;
    protected int selected = 0;
    protected String value;
    private DatabaseHelper db;

    public FormList(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
        super(_context, data);
        encuesta_id = data.getInt("encuesta");
        maestro_col = data.getString("maestro_col");
        _viewgroup = (ViewGroup) inflater.inflate(R.layout.form_list, parent, false);
        TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_text);
//		txv.setText((data.has("position")?data.getString("position"):"") + ") " +this.text);
        txv.setText(this.text);
        if (required == false) {
            TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
            txt_required.setVisibility(View.GONE);
        }
        parent.addView(_viewgroup);
        spn = (Spinner) _viewgroup.findViewById(R.id.spn_list);

        adapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        values = getValues(null);
        adapter.add("");
        for (String value : values) {
            adapter.add(value);
        }
        spn.setAdapter(adapter);
        spn.setSelection(0);
        spn.setOnItemSelectedListener(this);

    }

    /**
     * Retorna el listado de posibles valores de la lista
     *
     * @return
     */
    private List<String> getValues(String parentValue) {
        try {
            if (isDependent() && TextUtils.isEmpty(parentValue)) {
                codes = new LinkedList<>();
                return new LinkedList<>();
            }
            if (!isLocationCatalog()) {
                boolean cobranza = isCobranzaSurvey();
                return getValuesFromCatalogo(parentValue);
            } else
                return getValuesFromCatalogo(parentValue);
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }

    }


    /**
     * Chequea si la lista es una lista de seleccion del catalogo de ubicacion
     *
     * @return
     */
    private boolean isLocationCatalog() {
        //String[] locations = new String[]{"PROVINCIA", "CANTON", "PARROQUIA", "BARRIO"};
        String[] locations = new String[]{"XXXXXXXXX"};
        return ArrayUtils.contains(locations, label.toUpperCase());
    }

    /**
     * Obtiene los valores del catalogo desde la base de datos del
     * catalogo.
     *
     * @param parentValue
     * @return
     */
    private List<String> getValuesFromCatalogo(String parentValue) {
        List<SurveyCatalog> catalogs = getCatalogs(parentValue);
        List<String> values = new LinkedList<>();
        codes = new LinkedList<>();
        for (SurveyCatalog c : catalogs) {
            values.add(c.getValor());
            codes.add(c.getCodigo());
        }
        return values;
    }

    /**
     * Busca si la lista es dependiente
     *
     * @return verdadero si la lista es dependiente
     */
    private boolean isDependent() {
        try {
            List<SurveyCatalog> catalogs = getCatalogs(null);
            for (SurveyCatalog c : catalogs) {
                if (TextHelper.isEmptyData(c.getNombrePadre()) == false)
                    return true;
            }
            return false;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * Retorna el id de la pregunta de la cual depende la lista
     *
     * @return id de la pregunta
     */
    public int getParentDependent() {
        try {
            List<SurveyCatalog> catalogs = getCatalogs(null);
            for (SurveyCatalog c : catalogs) {
                if (TextHelper.isEmptyData(c.getNombrePadre()) == false) {
                    QueryBuilder<SurveyQuestion, Integer> queryBulder = getDBHelper().getSurveyQuestionDao().queryBuilder();
                    queryBulder.where()
                            .eq("encuesta_id", encuesta_id)
                            .and().eq(SurveyQuestion.CATALOGO, c.getNombrePadre());
                    List<SurveyQuestion> questions = queryBulder.query();
                    if (questions.size() > 0) {
                        return questions.get(0).getId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return 0;
    }

    private List<SurveyCatalog> getCatalogs(String parentValue) {
        try {
            Dao<SurveyCatalog, Integer> catalogDao = getDBHelper().getSurveyCatalogDao();
            QueryBuilder<SurveyCatalog, Integer> queryBuilder = catalogDao.queryBuilder();
            if (TextUtils.isEmpty(parentValue)) {
                queryBuilder.where()
                        .eq("encuesta_id", encuesta_id)
                        .and()
                        .eq(SurveyCatalog.NOMBRE, maestro_col);
            } else {
                queryBuilder.where()
                        .eq("encuesta_id", encuesta_id)
                        .and().eq(SurveyCatalog.NOMBRE, maestro_col)
                        .and().eq(SurveyCatalog.CODIGOPADRE, parentValue);
            }
            //log.info("sql: " + queryBuilder.prepareStatementString());
            return queryBuilder.orderBy(SurveyCatalog.VALOR, true).query();
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        selected = position;
        onChangeValueEvent();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setValue(String data) {
        if (TextUtils.isEmpty(data)) {
            selected = 0;
            spn.setSelection(0);
        } else {
            value = data;
            int index = codes.indexOf(value);
            selected = (index >= 0) ? index + 1 : 0;
            spn.setSelection(selected);
        }
    }

    @Override
    public String getValue() {
        if (selected == 0)
            return "";
        //return "";//+NomenclatureTable.getIdByValue(_context, ""+this.id, values.get(selected-1));
        return codes.get(selected - 1);
    }

    @Override
    public String getFinalValue() {
        return visible ? getValue() : "";
    }

    @Override
    public JSONObject getError() {
        if (visible) {
            if (required && selected == 0) {
                try {
                    JSONObject error = new JSONObject();
                    error.put("name", text);
                    error.put("message", "Respuesta requerida");
                    return error;
                } catch (Exception e) {
                }
            } else if (isNotValidValue()) {
                try {
                    JSONObject error = new JSONObject();
                    error.put("name", text);
                    error.put("message", "Respuesta Incorrecta");
                    return error;
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        return null;
    }

    private boolean isCobranzaSurvey() {
        try {
            String survey = ((SurveyActivity) _context).getSurvey().getEtiqueta();
            //return survey.equalsIgnoreCase("COBRANZAGESTOR");
            return survey.equalsIgnoreCase("MORA");
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    private boolean isGestionCampoSurvey() {
        try {
            String survey = ((SurveyActivity) _context).getSurvey().getEtiqueta();
            //return survey.equalsIgnoreCase("COBRANZAGESTOR");
            return survey.equalsIgnoreCase("gestion_campo");
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }

    private boolean isNotValidValue() {
        try {
            String survey = ((SurveyActivity) _context).getSurvey().getEtiqueta();
            //if(survey.equalsIgnoreCase("COBRANZAGESTOR")){
            if (survey.equalsIgnoreCase("MORA")) {
                if (label.equalsIgnoreCase("RESPUESTA") && new LinkedList<String>(
                        Arrays.asList("2690", "2691", "2710", "4100")).contains(getValue())) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return false;
    }


    @Override
    public boolean haveValue(String value) {
        return selected > 0 && codes.get(selected - 1).equalsIgnoreCase(value);
    }

    @Override
    protected void onChangeValueEvent() {
        super.onChangeValueEvent();
        notifyDependents();
        if (_context instanceof FormListChangeListener)
            ((FormListChangeListener) _context).formListChange(this);
    }

    public void notifyDependents() {
        String value = getValue();
        for (FormDependentListener d : dependents) {
            d.parentValueChange(value);
        }
    }

    @Override
    public void parentValueChange(String value) {
        super.parentValueChange(value);
//		values = NomenclatureTable.getValues(_context,String.valueOf(id) , value);
        values = getValues(value);
        resetData();
        notifyDependents();
    }

    protected void resetData() {
        adapter.clear();
        adapter.add("");
        for (String value : values) {
            adapter.add(value);
        }
        setValue(value);
    }

    protected DatabaseHelper getDBHelper() {
        if (db == null) {
            db = OpenHelperManager.getHelper(_context, DatabaseHelper.class);
        }
        return db;
    }

    @Override
    protected void finalize() throws Throwable {
        if (db != null) {
            OpenHelperManager.releaseHelper();
            db = null;
        }
        super.finalize();
    }

    public interface FormListChangeListener {
        void formListChange(FormList component);
    }
}
