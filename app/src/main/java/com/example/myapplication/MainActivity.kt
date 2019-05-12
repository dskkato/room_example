package com.example.myapplication

import android.app.Application
import android.app.usage.UsageEvents
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.room.*

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

class AppRepository(application: Application) {
    private val mUserDao: UserDao

    init {
        val db = AppDatabase.getDatabase(application)
        mUserDao = db.userDao()
    }

    fun insert(user: User) {
        InsertAsyncTask(mUserDao).execute(user)
        Log.i("insert", "done")
    }
}

class InsertAsyncTask(userDao: UserDao): AsyncTask<User, Void, Void>() {
    private val mAsyncTaskDao: UserDao = userDao
    override fun doInBackground(vararg params: User?): Void? {
        mAsyncTaskDao.insertAll(params[0]!!)
        return null
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = AppRepository(application)

        val button = findViewById<Button>(R.id.Button)

        var ind = 0
        button.setOnClickListener {
            Toast.makeText(this, "$ind",
                Toast.LENGTH_SHORT).show()
            ind += 1

            val user = User(ind, "A", "B")
            db.insert(user)
        }
    }
}
