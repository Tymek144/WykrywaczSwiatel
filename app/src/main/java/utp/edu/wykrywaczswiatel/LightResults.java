package utp.edu.wykrywaczswiatel;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.sql.Time;
import java.util.Date;



@Entity
@TypeConverters({DateConverter.class, UriConverters.class})
public class LightResults {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "light")
    public Light light;

    @ColumnInfo(name = "image")
    public Uri path;

    enum Light {
        LIGHT_NULL(0),
        LIGHT_RED(1),
        LIGHT_GREEN(2),
        LIGHT_YELLOW(3);

        public final int value;

        Light(int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

    @ColumnInfo(name = "lightness")
    public float lightness;
}
