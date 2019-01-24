package net.geomovil.encuesta.form;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import net.geomovil.gestor.R;
import net.geomovil.gestor.SurveyActivity;
import net.geomovil.gestor.database.ClientData;
import net.geomovil.gestor.database.DatabaseHelper;
import net.geomovil.gestor.database.SurveyCatalog;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FormCheckBoxs extends FormWidget {
    private final Logger log = Logger.getLogger(FormCheckBoxs.class.getSimpleName());
    public static final String TAG = "FormCheckBoxs";
    protected int encuesta_id;
    protected String maestro_col;
    protected List<String> values;
    protected List<String> codes;
    protected List<String> selecteds;
    protected List<CheckBox> checks;
    private DatabaseHelper db;

    public FormCheckBoxs(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
        super(_context, data);
        _viewgroup = (ViewGroup) inflater.inflate(R.layout.form_checkboxs, parent, false);
        TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_text);
        if (isCuoteList()) {
            ClientData client = ((SurveyActivity) _context).getClient();
            //boolean credit = client.getTipoOperacion().equalsIgnoreCase("CTIPPROCRE");
            boolean credit = false;
            this.text += credit ? " (CREDITO)" : " (TCF)";
        }
//		txv.setText((data.has("position")?data.getString("position"):"") + ") " +this.text);
        txv.setText(this.text);
        encuesta_id = data.getInt("encuesta");
        maestro_col = data.getString("maestro_col");
        ViewGroup checks_container = (ViewGroup) _viewgroup.findViewById(R.id.checks_container);

        values = getValues();
        checks = new ArrayList<CheckBox>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            CheckBox ch = new CheckBox(_context);
            ch.setText(value);
            ch.setTag(codes.get(i));
            checks_container.addView(ch);
            ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!selecteds.contains((String) buttonView.getTag()))
                            selecteds.add((String) buttonView.getTag());
                    } else {
                        selecteds.remove((String) buttonView.getTag());
                    }
                    onChangeValueEvent();
                }
            });
            checks.add(ch);
        }
        if (required == false) {
            TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
            txt_required.setVisibility(View.GONE);
        }
        parent.addView(_viewgroup);

        selecteds = new ArrayList<String>();
    }

    protected void activeChecks() {
        for (CheckBox ch : checks) {
            ch.setChecked(selecteds.contains(ch.getTag() + ""));
        }
    }

    @Override
    public void setValue(String data) {
        try {
            selecteds = new ArrayList<String>();
            JSONArray vs = new JSONArray(data);
            for (int i = 0; i < vs.length(); i++) {
                String value = vs.getString(i);
                if (!TextUtils.isEmpty(value) && !selecteds.contains(value))
                    selecteds.add(value);
            }
            activeChecks();
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public String getValue() {
        JSONArray vs = new JSONArray();
        for (int i = 0; i < selecteds.size(); i++) {
            vs.put(selecteds.get(i));
        }
        return vs.toString();
    }

    @Override
    public String getFinalValue() {
        return visible ? StringUtils.join(selecteds, ",") : "";
    }

    @Override
    public JSONObject getError() {
        if (visible && required && selecteds.size() == 0) {
            try {
                JSONObject error = new JSONObject();
                error.put("name", text);
                error.put("message", "Respuesta requerida");
                return error;
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean haveValue(String value) {
        return selecteds.contains(value);
    }

    /**
     * Retorna el listado de posibles valores de la lista
     *
     * @return
     */
    private List<String> getValues() {
        try {
            if (isCuoteList()) {
                //return getCuoteListOptions();
                return new LinkedList<>();
            } else {
                return getValuesFromDb();
            }
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }
    }

    /*private List<String> getCuoteListOptions() {
        try {
            ClientData client = ((SurveyActivity) _context).getClient();
            List<ClientCuota> cuota = getDBHelper().getClientCuotaDao().queryForEq(ClientCuota.ID_NEGOCIO,
                    client.getId_negocio());
            boolean credit = client.getTipoOperacion().equalsIgnoreCase("CTIPPROCRE");
            codes = new LinkedList<>();
            values = new LinkedList<>();
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            for (ClientCuota c : cuota) {
                if (credit)
                    values.add(c.getCuota() + "  -  $" + c.getTotal_cuota());
                else
                    values.add(formatter.format(c.getVencimiento_inicial()) + "  -  $" + c.getTotal_cuota());
                codes.add(c.getDetalleOperacion() + "");
            }
            return values;
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }
    }*/

    /**
     * Retorna verdadero en caso de ser el campo de checks solicitados para el
     * numero de cuotas
     *
     * @return
     */
    private boolean isCuoteList() {
        //return label.equalsIgnoreCase("NUMEROCUOTAS");
        return false;
    }

    private List<String> getValuesFromDb() {
        try {
            List<SurveyCatalog> catalogs = getCatalogs();
            List<String> values = new LinkedList<>();
            codes = new LinkedList<>();
            for (SurveyCatalog c : catalogs) {
                values.add(c.getValor());
                codes.add(c.getCodigo());
            }
            return values;
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }
    }


    private List<SurveyCatalog> getCatalogs() {
        try {
            Dao<SurveyCatalog, Integer> catalogDao = getDBHelper().getSurveyCatalogDao();
            QueryBuilder<SurveyCatalog, Integer> queryBuilder = catalogDao.queryBuilder();
            queryBuilder.where()
                    .eq("encuesta_id", encuesta_id)
                    .and()
                    .eq(SurveyCatalog.NOMBRE, maestro_col);
            return queryBuilder.orderBy(SurveyCatalog.VALOR, true).query();
        } catch (Exception e) {
            log.error("", e);
            return new LinkedList<>();
        }
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
}
