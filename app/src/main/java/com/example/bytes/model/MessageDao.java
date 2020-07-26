package com.example.bytes.model;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addMessage(Message message);

    @Query("UPDATE Message SET seen = 1 WHERE chatId = :uid")
    void readMessages(String uid);

    @Query("SELECT * FROM Message WHERE chatId = :chatId ORDER BY time")
    Cursor getMessages(String chatId);

    @Query("SELECT * FROM (SELECT M.*,P.* FROM Message AS M INNER JOIN Profile AS P ON M.chatId = P.uid ORDER BY M.time) GROUP BY chatId ORDER BY time DESC")
    Cursor getChats();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMessage(Message message);
}
