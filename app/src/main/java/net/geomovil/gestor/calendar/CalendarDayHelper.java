package net.geomovil.gestor.calendar;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CalendarDayHelper {
    private static final Logger log = Logger.getLogger(CalendarDayHelper.class.getSimpleName());
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String VIEW_DATE_FORMAT = "dd/MM/yyyy";
    private static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
    private static final DateFormat vdf = new SimpleDateFormat(VIEW_DATE_FORMAT);

    /**
     * Retorna la fecha en formato YYYY-MM-DD
     *
     * @param date
     * @return
     */
    public static String get(CalendarDay date) {
        return df.format(date.getDate());
    }

    /**
     * Retorna este dia para el siguiente mes
     *
     * @param date
     * @return
     */
    public static String getNextMoth(CalendarDay date) {
        int month = date.getMonth() == 11 ? 0 : date.getMonth() + 1;
        int year = date.getMonth() == 11 ? date.getYear() + 1 : date.getYear();
        return df.format(CalendarDay.from(year, month, date.getDay()).getDate());
    }

    public static CalendarDay getCalendarDay(String date) {
        try {
            return CalendarDay.from(df.parse(date));
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static Date getDate(String date) {
        try {
            return df.parse(date);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static String revertFormat(String date, boolean to_v) {
        try {
            if (to_v) {
                return vdf.format(df.parse(date));
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return date;
    }
}
