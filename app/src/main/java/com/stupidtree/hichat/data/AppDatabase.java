package com.stupidtree.hichat.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.source.ChatMessageDao;

@Database(entities = {ChatMessage.class}, version = 1)
@TypeConverters({DateTimestampConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context){
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "cloudliter").build();
                }
            }
        }
        return INSTANCE;
    }

}
