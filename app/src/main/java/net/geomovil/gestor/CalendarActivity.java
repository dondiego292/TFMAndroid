package net.geomovil.gestor;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import net.geomovil.gestor.calendar.CalendarDayHelper;
import net.geomovil.gestor.calendar.EventDecorator;
import net.geomovil.gestor.database.ClientData;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends BaseActivity implements OnMonthChangedListener, OnDateSelectedListener {
    private final Logger log = Logger.getLogger(CalendarActivity.class.getSimpleName());

    private MaterialCalendarView widget;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle(getString(R.string.Calendar));
        widget = (MaterialCalendarView) findViewById(R.id.calendarView);
        widget.setOnMonthChangedListener(this);
        widget.setOnDateChangedListener(this);
        initCalendarDate();
        new UserCalendar().execute(widget.getCurrentDate());
    }

    private void initCalendarDate() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
        String fecha = sharedPref.getString("fecha_inspeccion",null);
        if(fecha == null){
            widget.setDateSelected(CalendarDay.today(),false);
        }else{
            widget.setDateSelected(CalendarDayHelper.getCalendarDay(fecha),false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        widget.removeDecorators();
        new UserCalendar().execute(date);
    }

    @Override
    public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("app",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("fecha_inspeccion",CalendarDayHelper.get(date));
        editor.commit();
        setResult(RESULT_OK);
        finish();
    }

    private class UserCalendar extends AsyncTask<CalendarDay, Void, List<CalendarDay>> {
        private CalendarDay date;
        @Override
        protected List<CalendarDay> doInBackground(@NonNull CalendarDay... days) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            date = days[0];
            String sql = "SELECT "+ ClientData.FECHAINSPECCION+",count(id) as CANT from client_data " +
                    "WHERE ("+ ClientData.MOVILSTATUS+" = 0 OR "+ClientData.MOVILSTATUS+" =1) AND " +
                    ClientData.FECHAINSPECCION + " >= '"+CalendarDayHelper.get(date)+"' AND " +
                    ClientData.FECHAINSPECCION+" < '"+CalendarDayHelper.getNextMoth(date)+"' " ;/*+
                    " GROUP BY FECHA";*/
            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor c  = db.rawQuery(sql, null);
            ArrayList<CalendarDay> dates = new ArrayList<>();
            while(c.moveToNext()){
                dates.add(CalendarDayHelper.getCalendarDay(c.getString(0)));
            }
            c.close();
            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);
            if (isFinishing()) {
                return;
            }
            // marcando los dias del calendario en el que faltan visitas por realizar
            widget.addDecorator(new EventDecorator(Color.parseColor("#FC3A3A"), calendarDays));

            String sql = "SELECT "+ ClientData.FECHAINSPECCION+",count(id) as CANT from client_data " +
                    "WHERE ("+ ClientData.MOVILSTATUS+" = 2 OR " + ClientData.MOVILSTATUS+" = 3) AND " +
                    ClientData.FECHAINSPECCION + " >= '"+CalendarDayHelper.get(date)+"' AND " +
                    ClientData.FECHAINSPECCION+" < '"+CalendarDayHelper.getNextMoth(date)+"' "; /*+
                   " GROUP BY FECHA";*/
            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor c  = db.rawQuery(sql, null);
            ArrayList<CalendarDay> dates = new ArrayList<>();
            while(c.moveToNext()){
                CalendarDay d = CalendarDayHelper.getCalendarDay(c.getString(0));
                if(!calendarDays.contains(d))
                    dates.add(CalendarDayHelper.getCalendarDay(c.getString(0)));
            }
            c.close();
            // marcando los dias del calendario en que todas las visitas fueron hechas
            widget.addDecorator(new EventDecorator(Color.parseColor("#66bb6a"), dates));
        }
    }
}
