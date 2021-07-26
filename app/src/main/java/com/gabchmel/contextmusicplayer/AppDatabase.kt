package com.gabchmel.contextmusicplayer

import androidx.room.*

class AppDatabase {

    @Entity
    data class Song(
        @PrimaryKey val uid: Int,
        @ColumnInfo(name = "first_name") val title: String?,
        @ColumnInfo(name = "last_name") val author: String?
    )

    @Dao
    interface UserDao {
        @Query("SELECT * FROM song")
        fun getAll(): List<Song>

        @Query("SELECT * FROM song WHERE uid IN (:userIds)")
        fun loadAllByIds(userIds: IntArray): List<Song>

        @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
                "last_name LIKE :last LIMIT 1")
        fun findByName(first: String, last: String): Song

        @Insert
        fun insertAll(vararg songs: Song)

        @Delete
        fun delete(song: Song)
    }

    @Database(entities = [Song::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): UserDao
    }
}