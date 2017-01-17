package no.javazone.archframework.model.dto;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class JZPreciseDate {

    public int day;
    public int year;
    public int hour;
    public int month;
    public int minute;
    public int second;
    private GregorianCalendar calendar = new GregorianCalendar();

    public JZPreciseDate(final String dateString)  {

        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date;
        try {
            date = fmt.parse(dateString);
            day = date.getDate();
            year = 1900+date.getYear();
            hour = date.getHours()+2; // TODO timezone hack...
            month  = date.getMonth();
            minute = date.getMinutes();
            second = date.getSeconds();


        }
        catch (ParseException e) {
            Log.e(this.getClass().getName(),e.getMessage(),e);
        }


    }

    public long millis(){

        calendar.set(year,month,day,hour,minute,second);

        return calendar.getTimeInMillis();
    }

}
