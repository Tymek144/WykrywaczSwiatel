package utp.edu.wykrywaczswiatel;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RoomConnect extends Application {

    private Context con;
    private LightResultsDao lightResultsDao;

    public RoomConnect(Context context)
    {
        con =context;
        AppDatabase db = Room.databaseBuilder(con,
                AppDatabase.class, "light-results").allowMainThreadQueries().build();
        lightResultsDao = db.lightResultsDao();
    }

    public void SaveResult(Bitmap result, LightResults.Light type)
    {
        List<LightResults> lights = lightResultsDao.getAll();
        int max= lights.size();

        OutputStream out = null;

        String file = String.valueOf(max+1) + ".jpg";

        File storge = Environment.getExternalStorageDirectory();

        File f = new File(storge.getAbsolutePath(), file);

        try {
            f.createNewFile();
            f.setWritable(true);
            // Compress the bitmap and save in jpg format
            FileOutputStream stream = new FileOutputStream(f);
            result.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        LightResults lightResults = new LightResults();
        lightResults.path = Uri.parse(f.getAbsolutePath());
        lightResults.light = type;
        lightResults.date = Calendar.getInstance().getTime();
        lightResultsDao.insertAll(lightResults);
    }

    public LightData getDataFromDate(Date date)
    {
        LightData lightData = new LightData();

        LightResults lightResults = lightResultsDao.findByDateTime(date);

        Bitmap bitmap = BitmapFactory.decodeFile(lightResults.path.getPath());
        lightData.bitmap = bitmap;
        lightData.light = lightResults.light;
        lightData.data = lightResults.date;
        lightData.id = lightResults.uid;
        return lightData;
    }

    public LightData getDataFromId(int id)
    {
        LightData lightData = new LightData();

        LightResults lightResults = lightResultsDao.loadById(id);
        Bitmap bitmap = BitmapFactory.decodeFile(lightResults.path.getPath());
        lightData.bitmap = bitmap;
        lightData.light = lightResults.light;
        lightData.data = lightResults.date;
        lightData.id = lightResults.uid;
        return lightData;
    }

    public ArrayList<LightData> getAllData()
    {
        ArrayList<LightData> ld = new ArrayList<LightData>();
        List<LightResults> lr = lightResultsDao.getAll();

        for (LightResults lightResults : lr)
        {
            LightData lightData = new LightData();

            Bitmap bitmap = BitmapFactory.decodeFile(lightResults.path.getPath());
            lightData.bitmap = bitmap;
            lightData.light = lightResults.light;
            lightData.data = lightResults.date;
            lightData.id = lightResults.uid;
            ld.add(lightData);
        }
        return ld;
    }

    public void DeleteFromId(int id)
    {
        LightResults lightResults = lightResultsDao.loadById(id);
        File file = new File(lightResults.path.getPath());
        file.delete();
        lightResultsDao.delete(lightResults);
    }

    public void DeleteAll()
    {
        List<LightResults> lr = lightResultsDao.getAll();
        for (LightResults lightResults : lr) {
            File file = new File(lightResults.path.getPath());
            file.delete();
            lightResultsDao.delete(lightResults);
        }
    }
}
