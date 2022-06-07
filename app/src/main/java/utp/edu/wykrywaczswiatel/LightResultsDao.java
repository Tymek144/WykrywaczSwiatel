package utp.edu.wykrywaczswiatel;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Dao
@TypeConverters({DateConverter.class, UriConverters.class})
public interface LightResultsDao {
    @Query("SELECT * FROM LightResults")
    List<LightResults> getAll();

    @Query("SELECT * FROM LightResults WHERE uid IN (:lightIds)")
    List<LightResults> loadAllByIds(int[] lightIds);

    @Query("SELECT * FROM LightResults WHERE uid IN (:lightId)")
    LightResults loadById(int lightId);

    @Query("SELECT * FROM LightResults WHERE date LIKE :data " +
            "LIMIT 1")
    LightResults findByDateTime(Date data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(LightResults... lights);

    @Delete
    void delete(LightResults lightResults);
}
