package net.geomovil.encuesta.form;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.geomovil.gestor.R;
import net.geomovil.gestor.database.DatabaseHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FormAutoComplete extends FormWidget implements AdapterView.OnItemSelectedListener,AdapterView.OnItemClickListener {
    private final Logger log = Logger.getLogger(FormList.class.getSimpleName());
    protected int encuesta_id;
    protected String maestro_col;
    protected AutoCompleteTextView auto;
    protected ArrayAdapter<String> adapter;
    protected List<String> values;
    protected List<String> codes;
    protected int selected = 0;
    protected String value;
    private DatabaseHelper db;

    public FormAutoComplete(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
        super(_context, data);
        encuesta_id = data.getInt("encuesta");
        maestro_col = data.getString("maestro_col");
        _viewgroup = (ViewGroup) inflater.inflate(R.layout.form_autocomplete, parent,false);
        TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_text);
        txv.setText(this.text);
        if(required == false){
            TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
            txt_required.setVisibility(View.GONE);
        }
        parent.addView(_viewgroup);
        auto = (AutoCompleteTextView) _viewgroup.findViewById(R.id.txt_auto);

        adapter =  new ArrayAdapter<String>( _context, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        //values = getValues(null);
        adapter.add("");
        for (String value : values) {
            adapter.add(value);
        }
        /*spn.setAdapter(adapter);
        spn.setSelection( 0 );
        spn.setOnItemSelectedListener(this);*/
        auto.setThreshold(1);
        auto.setAdapter(adapter);
        auto.setOnItemSelectedListener(this);

    }

    @Override
    public void setValue(String data) {

    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public JSONObject getError() {
        return null;
    }

    /**
     * Retorna el listado de posibles valores de la lista
     * @return
     */
    /*private List<String> getValues(String parentValue) {
        try {
            if(isDependent() && TextUtils.isEmpty(parentValue)) {
                codes = new LinkedList<>();
                return new LinkedList<>();
            }
            if(!isLocationCatalog()){
                boolean cobranza = isCobranzaSurvey();
                if(cobranza && getLabel().equalsIgnoreCase("NUMEROCOMPROBANTE")){
                    return getRecibosValues();
                }else if(cobranza && getLabel().equalsIgnoreCase("IdDireccion")){
                    return getDireccionValues();
                }
                else
                    return getValuesFromCatalogo(parentValue);
            }else {
                return getValuesFromLocationCatalog(parentValue);
            }
        }
        catch(Exception e){
            log.error("",e);
            return new LinkedList<>();
        }
    }*/




    @Override
    public boolean haveValue(String value) {
        return selected > 0 && codes.get(selected-1).equalsIgnoreCase(value);
    }

    @Override
    protected void onChangeValueEvent() {
        super.onChangeValueEvent();
        notifyDependents();
        if(_context instanceof FormAutoCompleteChangeListener)
            ((FormAutoCompleteChangeListener) _context).formAutoCompleteChange(this);
    }

    public void notifyDependents(){
        String value = getValue();
        for (FormDependentListener d : dependents) {
            d.parentValueChange(value);
        }
    }

    @Override
    public void parentValueChange(String value) {
        super.parentValueChange(value);
        //values = getValues(value);
        resetData();
        notifyDependents();
    }

    @Override
    public String getFinalValue() {
        return null;
    }

    protected void resetData(){
        adapter.clear();
        adapter.add("");
        for (String value : values) {
            adapter.add(value);
        }
        setValue(value);
    }

    protected DatabaseHelper getDBHelper(){
        if(db == null){
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public interface FormAutoCompleteChangeListener{
        void formAutoCompleteChange(FormAutoComplete component);
    }
}
