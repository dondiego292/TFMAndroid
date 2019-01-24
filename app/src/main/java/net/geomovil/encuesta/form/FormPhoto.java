package net.geomovil.encuesta.form;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.geomovil.gestor.R;

import org.json.JSONObject;

import java.io.File;

public class FormPhoto extends FormWidget {
	private static final String TAG = "FormPhoto"; 
	protected Button btn;
	protected String path;
	
	public FormPhoto(Context _context, JSONObject data, LayoutInflater inflater, ViewGroup parent, String _path) throws Exception {
		super(_context, data);
		_viewgroup = (ViewGroup) inflater.inflate(R.layout.form_photo, parent,false);
		this.path = ((photoData)_context).getPhotoName(_path);
		Log.e(TAG,_path);
		btn = (Button) _viewgroup.findViewById(R.id.btn);
		btn.setText(this.text);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((takePhoto)FormPhoto.this._context).onTakePhoto(((photoData)FormPhoto.this._context).getPath(path));
			}
		});
		if(required == false){
			TextView txt_required = (TextView) _viewgroup.findViewById(R.id.txt_required);
			txt_required.setVisibility(View.GONE);
		}
		checkTaken();
		parent.addView(_viewgroup);
	}
	
	public void checkTaken(){
		String fullpath = ((photoData)_context).getPath(this.path);
		File file = new File(fullpath);
		if(file.exists()){
			btn.setBackgroundColor(_context.getResources().getColor(R.color.photo_tomada));
			btn.setTextColor(_context.getResources().getColor(R.color.photo_tomada_text));
		}
	}
	
	public interface takePhoto{
		void onTakePhoto(String _path);
	}

	@Override
	public void setValue(String data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getValue() {
		String fullpath = ((photoData)_context).getPath(this.path);
		File file = new File(fullpath);
		if(file.exists()){
			return path;
		}
		return null;
	}

	@Override
	public String getFinalValue() {
		return getValue();
	}
	
	public interface photoData{
		public String getPath(String label);
		public String getPhotoName(String label);
	}

	@Override
	public JSONObject getError() {
		String path = ((photoData)_context).getPath(this.path);
		if(visible && required && ( new File(path)).exists()==false){
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
	public boolean haveValue(String value) {
		return false;
	}
	
}
