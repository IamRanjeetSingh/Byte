package com.example.bytes.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Profile.class, Message.class}, version = 1, exportSchema = false)
@TypeConverters({Profile.Converter.class, Message.Converter.class})
public abstract class CacheDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "CacheDatabase";

    private static CacheDatabase instance;

    public abstract ProfileDao getProfileDao();
    public abstract MessageDao getMessageDao();

    public static CacheDatabase getInstance(@NonNull Application application){
        if(instance == null){
            instance = Room.databaseBuilder(application, CacheDatabase.class, DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .build();
        }
        return instance;
    }
}
