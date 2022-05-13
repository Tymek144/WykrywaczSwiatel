package utp.edu.wykrywaczswiatel;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long dateTimeLong){
        return dateTimeLong == null ? null: new Date(dateTimeLong);
    }

    @TypeConverter
    public static long fromDate(Date date){
        return date == null ? null :date.getTime();
    }
}
