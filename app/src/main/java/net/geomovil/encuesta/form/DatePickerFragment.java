package net.geomovil.encuesta.form;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
				implements DatePickerDialog.OnDateSetListener{
	FormEditDate _form;	
		

	public void setForm(FormEditDate _form) {
		this._form = _form;
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
		final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
		String date = _form.getValue();
		//String date = "2014-06-15";
		String pattern = "\\d\\d\\d\\d-\\d\\d-\\d\\d";
		if(date.matches(pattern)){
			year = Integer.parseInt(date.substring(0,4));
			month = Integer.parseInt(date.substring(5,7))-1;
			day = Integer.parseInt(date.substring(8,10));
		} 
		
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
	
	@Override
	public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
		String year = String.valueOf(arg1);
		String month = ((arg2+1) < 10)? "0"+String.valueOf((arg2+1)):String.valueOf((arg2+1));
		String day = (arg3 < 10)? "0"+String.valueOf(arg3):String.valueOf(arg3);
		_form.setValue(year+"-"+month+"-"+day);
	}

}
