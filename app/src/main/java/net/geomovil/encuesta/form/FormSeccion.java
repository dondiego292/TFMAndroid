package net.geomovil.encuesta.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;


import net.geomovil.gestor.R;

import org.json.JSONObject;

import java.util.Locale;

public class FormSeccion extends FormWidget {

	public FormSeccion(Context _context, JSONObject data,LayoutInflater inflater, ViewGroup parent) throws Exception {
		super(_context, data);
		_viewgroup = (ViewGroup) inflater.inflate(R.layout.form_seccion, parent);
		TextView txv = (TextView) _viewgroup.findViewById(R.id.txt_title);
		txv.setText(this.text.toUpperCase(Locale.getDefault()));
	}

	@Override
	public void setValue(String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFinalValue() {
		return getValue();
	}
	
	@Override
	public JSONObject getError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean haveValue(String value) {
		// TODO Auto-generated method stub
		return false;
	}

}
