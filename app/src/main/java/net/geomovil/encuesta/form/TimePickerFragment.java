package net.geomovil.encuesta.form;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener{
    protected FormEditTime _form;

    public void setForm(FormEditTime _form) {
        this._form = _form;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        String time = _form.getValue();
        String pattern = "\\d\\d:\\d\\d";
        if(time.matches(pattern)){
            hour = Integer.parseInt(time.substring(0,2));
            minute = Integer.parseInt(time.substring(3,5));
        }
        return new TimePickerDialog(getActivity(), this, hour, minute,true);
    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String min = minute < 10 ? "0"+minute : ""+minute;
        _form.setValue(hour+":"+min);
    }

}
