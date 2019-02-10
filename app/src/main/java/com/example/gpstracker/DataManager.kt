package com.example.gpstracker

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.gpstracker.GeoNoticeDao.Companion.migrations


class DataManager(ctx: Context) {

    private val db: GeoNoticeyDatabase =
        Room.databaseBuilder(ctx, GeoNoticeyDatabase::class.java, GeoNoticeDao.DATABASE)
            .addMigrations(*migrations)
            .allowMainThreadQueries()
            .build()

    var places: ArrayList<Place>? = null
        get() {
            if(field == null)
                field = ArrayList(db.dao().getAll())
            return field
        }
        private set(value) { field = value }


    var notices: ArrayList<Notice>? = null
        get() {
            if(field == null)
                field = ArrayList(db.dao().getAllNotice())
            return field
        }
        private set(value) { field = value }




    fun addPlace(place: Place) {
        db.dao().insertPlace(place)
        places?.add(place)
    }

    fun addNotice(notice: Notice) {
        db.dao().insertNotice(notice)
        notices?.add(notice)
    }

    fun deleteAll() {
        deleteNotice()
        deletePlace()
    }
    fun deleteNotice(noticeId: Int? = null) {
        if(noticeId == null) {
            db.dao().deleteNotices()
            places?.clear()
        }
    }
    fun deletePlace(placeId: Int? = null) {
        if(placeId == null) {
            db.dao().deletePlaces()
            places?.clear()
        }
    }
}


@Dao
interface GeoNoticeDao {

    companion object {
        const val DATABASE = "places"
        const val TABLE_PLACES = "places"
        const val TABLE_NOTIFICATION = "notifications"

        val migrations = arrayOf<Migration>(
            object: Migration(1, 2) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    database.execSQL("CREATE TABLE '$TABLE_NOTIFICATION' ('id' INTEGER PRIMARY KEY NOT NULL, 'name' TEXT NOT NULL)")
                }
            }
        )
    }

    @Query("SELECT * FROM $TABLE_PLACES")
    fun getAll(): List<Place>

    @Query("SELECT * FROM $TABLE_NOTIFICATION")
    fun getAllNotice(): List<Notice>

    @Insert
    fun insertPlace(vararg places: Place)
    @Insert
    fun insertNotice(vararg notices: Notice)

    @Query("DELETE FROM $TABLE_NOTIFICATION")
    fun deleteNotices()
    
    @Query("DELETE FROM $TABLE_PLACES")
    fun deletePlaces()
}


@Database(entities = [Place::class, Notice::class], version = 2)
abstract class GeoNoticeyDatabase: RoomDatabase() {
    abstract fun dao(): GeoNoticeDao
}


@Entity(tableName = GeoNoticeDao.TABLE_PLACES)
data class Place(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "x") val x: Double,
    @ColumnInfo(name = "y") val y: Double,
    @PrimaryKey(autoGenerate = true) val id: Int = 0)



@Entity(tableName = GeoNoticeDao.TABLE_NOTIFICATION)
data class Notice(
    @ColumnInfo(name = "name") val name: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0)