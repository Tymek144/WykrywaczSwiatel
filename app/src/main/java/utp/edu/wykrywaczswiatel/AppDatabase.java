package utp.edu.wykrywaczswiatel;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {LightResults.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LightResultsDao lightResultsDao();
}
