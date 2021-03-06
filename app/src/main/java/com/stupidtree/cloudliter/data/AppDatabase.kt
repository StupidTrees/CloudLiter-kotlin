package com.stupidtree.cloudliter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.stupidtree.cloudliter.data.model.*
import com.stupidtree.cloudliter.data.source.dao.*

@Database(entities = [ChatMessage::class, Conversation::class, UserRelation::class, UserProfile::class, ImageEntity::class,FaceResult::class], version = 1)
@androidx.room.TypeConverters(TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun userRelationDao():UserRelationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun faceResultDao(): FaceResultDao
    abstract fun imageDao(): ImageDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        @JvmStatic
        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                AppDatabase::class.java, "cloudliter").build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}