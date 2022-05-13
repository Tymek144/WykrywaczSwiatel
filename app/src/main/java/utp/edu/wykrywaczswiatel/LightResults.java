package utp.edu.wykrywaczswiatel;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Time;
import java.util.Date;

@Entity
@TypeConverters(DateConverter.class)
public class LightResults {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "date")
    public Date date;

   // @ColumnInfo(name = "image")
   // public Bitmap image;
}
