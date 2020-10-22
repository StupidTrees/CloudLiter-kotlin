package com.stupidtree.hichat.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.stupidtree.hichat.data.model.ChatMessage;
import com.stupidtree.hichat.data.model.Conversation;
import com.stupidtree.hichat.data.source.ChatMessageDao;
import com.stupidtree.hichat.data.source.ConversationDao;

@Database(entities = {ChatMessage.class, Conversation.class}, version = 1)
@androidx.room.TypeConverters({TypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatMessageDao chatMessageDao();
    public abstract ConversationDao conversationDao();
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
