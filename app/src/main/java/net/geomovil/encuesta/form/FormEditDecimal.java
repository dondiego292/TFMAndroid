package net.geomovil.encuesta.form;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import net.geomovil.gestor.R;
import net.geomovil.gestor.util.TextHelper;

import org.json.JSONObject;

public class FormEditDecimal  extends FormWidget{
	protected static final String TAG="FormEditDecimal";
	protected EditText edt;

	public FormEditDecimal(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent) throws Exception {
		super(_context, data);
		_viewgroup = (ViewGroup) inflater.inflate(R.layout.form_edit_float, parent,false);
		this.edt = (EditText) _viewgroup.findViewById(R.id.edt);
		TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_text);
//		txv.setText((data.has("position")?data.getString("position"):"") + ") " +this.text);
		txv.setText(this.text);
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
		if(visible) {
			if (required && TextUtils.isEmpty(edt.getText().toString())) {
				try {
					JSONObject error = new JSONObject();
					error.put("name", text);
					error.put("message", "Respuesta requerida");
					return error;
				} catch (Exception e) {
					Log.e(TAG, "" + e.getMessage());
				}
			}
			if(!TextHelper.isEmptyData(expresion)){
				String value = getValue();
				if(value.matches(expresion) == false){
					try {
						JSONObject error = new JSONObject();
						error.put("name", text);
						error.put("message", "Formato incorrecto");
						return error;
					} catch (Exception e) {
						Log.e(TAG, "" + e.getMessage());
					}
				}
			}
		}
		return null;
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
		
		return edt.getText().toString().equalsIgnoreCase(value);
	}
}
