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

import java.util.Calendar;

public class FormEditDate extends FormWidget implements View.OnClickListener {
    private static final String TAG = "FormEditText";
    protected Button btn;
    protected String date;

    public FormEditDate(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
        super(_context, data);
        _viewgroup = (ViewGroup) inflater.inflate(R.layout.form_edit_date, parent, false);
        date = "";
        btn = (Button) _viewgroup.findViewById(R.id.btn);
        btn.setText(this.text);
        btn.setOnClickListener(this);
        if (required == false) {
            TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
            txt_required.setVisibility(View.GONE);
        }
        ImageButton clear = (ImageButton) _viewgroup.findViewById(R.id.clear_btn);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FormEditDate.this.setValue("");
            }
        });
        parent.addView(_viewgroup);
    }

    @Override
    public void setValue(String data) {
        date = data;
        if (TextUtils.isEmpty(date)) {
            btn.setText(text);
        } else {
            btn.setText(text + ": " + date);
        }
        onChangeValueEvent();
    }

    @Override
    public String getValue() {
        return date;
    }

    @Override
    public String getFinalValue() {
        return visible ? getValue() : "";
    }

    @Override
    public JSONObject getError() {
        if (visible) {
            if (required && TextUtils.isEmpty(date)) {
                try {
                    JSONObject error = new JSONObject();
                    error.put("name", text);
                    error.put("message", "Respuesta requerida");
                    return error;
                } catch (Exception e) {
                    Log.e(TAG, "" + e.getMessage());
                }
            }
            if (expresion.equals("+=")) {
                try {
                    String pattern = "\\d\\d\\d\\d-\\d\\d-\\d\\d";
                    if (date.matches(pattern)) {

                        Calendar cdateAfter = Calendar.getInstance();
                        cdateAfter.add(Calendar.DATE, 365);
                        Calendar today = Calendar.getInstance();
                        Calendar cdate = Calendar.getInstance();
                        cdate.set(Integer.parseInt(date.substring(0, 4)),
                                Integer.parseInt(date.substring(5, 7)) - 1,
                                Integer.parseInt(date.substring(8, 10)));
                        if (cdate.before(today)) {
                            JSONObject error = new JSONObject();
                            error.put("name", text);
                            error.put("message", "No puede ser menor a la fecha actual");
                            return error;
                        } else if (cdate.after(cdateAfter)) {
                            JSONObject error = new JSONObject();
                            error.put("name", text);
                            error.put("message", "No puede ser mayor a 1 año de la fecha actual");
                            return error;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "" + e.getMessage());
                }
            }
        }
        return null;
    }


    @Override
    public boolean haveValue(String value) {

        return date.equalsIgnoreCase(value);
    }

    @Override
    public void onClick(View v) {

        if (_context instanceof SurveyActivity) {
            SurveyActivity act = (SurveyActivity) _context;
            DatePickerFragment picker = new DatePickerFragment();
            picker.setForm(this);
            picker.show(act.getSupportFragmentManager(), this.label);
        }
    }

    @Override
    protected void onChangeValueEvent() {
        super.onChangeValueEvent();
        if (_context instanceof FormEditDateChangeListener)
            ((FormEditDateChangeListener) _context).formEditDateChange(this);
    }

    public interface FormEditDateChangeListener {
        void formEditDateChange(FormEditDate component);
    }

}
