package hu.unimiskolc.iit.mobile.framework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import hu.unimiskolc.iit.mobile.framework.db.converter.DateTypeConverter
import hu.unimiskolc.iit.mobile.framework.db.converter.PropellantTypeConverter
import hu.unimiskolc.iit.mobile.framework.db.dao.CarDao
import hu.unimiskolc.iit.mobile.framework.db.dao.UserDao
import hu.unimiskolc.iit.mobile.framework.db.entity.CarEntity
import hu.unimiskolc.iit.mobile.framework.db.entity.UserEntity

@Database(
    entities = [CarEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateTypeConverter::class,
    PropellantTypeConverter::class
)
abstract class CarNotifyDatabase : RoomDatabase() {
    companion object {
        private const val DATABASE_NAME = "car_notify.db"
        private var instance: CarNotifyDatabase? = null

        private fun create(context: Context) : CarNotifyDatabase = Room.databaseBuilder(context, CarNotifyDatabase::class.java, DATABASE_NAME)
            .addCallback(DB_CALLBACK)
            .build()

        fun getInstance(context: Context) : CarNotifyDatabase = (instance ?: create(context)).also { instance = it }

        private val DB_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }

    abstract fun carDao() : CarDao
    abstract fun userDao() : UserDao
}