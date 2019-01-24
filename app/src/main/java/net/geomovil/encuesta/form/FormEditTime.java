package net.geomovil.encuesta.form;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.geomovil.gestor.R;
import net.geomovil.gestor.SurveyActivity;

import org.json.JSONObject;

public class FormEditTime extends FormWidget implements  View.OnClickListener{
    private static final String TAG = "FormEditTime";
    protected Button btn;
    protected String date;

    public FormEditTime(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
        super(_context, data);
        _viewgroup = (ViewGroup) inflater.inflate(R.layout.form_edit_date, parent,false);
        date = "";
        btn = (Button) _viewgroup.findViewById(R.id.btn);
        btn.setText(this.text);
        btn.setOnClickListener(this);
        if(required == false){
            TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
            txt_required.setVisibility(View.GONE);
        }
        ImageButton clear = (ImageButton)_viewgroup.findViewById(R.id.clear_btn);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormEditTime.this.setValue("");
            }
        });
        parent.addView(_viewgroup);
    }

    @Override
    public void onClick(View v) {
        if(_context instanceof SurveyActivity){
            SurveyActivity act = (SurveyActivity) _context;
            TimePickerFragment picker = new TimePickerFragment();
            picker.setForm(this);
            picker.show(act.getSupportFragmentManager(), this.label);
        }
    }
    @Override
    public void setValue(String data) {
        date = data;
        if(TextUtils.isEmpty(date)){
            btn.setText(text);
        } else {
            btn.setText(text + ": " + date);
        }
    }

    @Override
    public String getValue() {
        return date;
    }

    @Override
    public String getFinalValue() {
        return visible ? getValue(): "";
    }

    @Override
    public JSONObject getError() {
        if(visible && required && TextUtils.isEmpty(date)){
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

        return date.equalsIgnoreCase(value);
    }
}
