package net.geomovil.encuesta.form;


import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.geomovil.gestor.R;

import org.json.JSONObject;

public class FormLongText extends FormWidget implements TextWatcher {
	protected static final String TAG="FormLongText";
	protected EditText edt;
	public FormLongText(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
		super(_context, data);
		_viewgroup = (ViewGroup) inflater.inflate(R.layout.form_edit_text, parent,false);
		this.edt = (EditText) _viewgroup.findViewById(R.id.edt);
		
		TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_text);
//		txv.setText((data.has("position")?data.getString("position"):"") + ") " +this.text);
		txv.setText(this.text);
		if(required == false){
			TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
			txt_required.setVisibility(View.GONE);
		}
		if(required == false){
			TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
			txt_required.setVisibility(View.GONE);
		}
		parent.addView(_viewgroup);
	}
	@Override
	public void setValue(String data) {
		edt.setText(data);		
	}
	
	@Override
	public String getValue() {
		return edt.getText().toString();
	}

	@Override
	public String getFinalValue() {
		return visible ? getValue(): "";
	}

	@Override
	public JSONObject getError() {
		if(visible && required && TextUtils.isEmpty(edt.getText().toString())){
			try {
				JSONObject error = new JSONObject();
				error.put("name", text);
				error.put("message", "Respuesta requerida");
				return error;
			} catch (Exception e) {
				Log.e(TAG,""+e.getMessage());
			}
		}
		return null;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		checkColor();
	}
	
	protected void checkColor(){
		if (TextUtils.isEmpty(edt.getText().toString())) {
			edt.setBackgroundColor(_context.getResources().getColor(
					R.color.text_required));
		} else {
			edt.setBackgroundResource(android.R.drawable.editbox_background_normal);
		}
	}
	
	@Override
	public boolean haveValue(String value) {		
		return false;
	}
}
