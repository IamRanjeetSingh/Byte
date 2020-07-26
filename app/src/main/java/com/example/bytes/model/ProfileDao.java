package com.example.bytes.model;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bytes.model.Profile;

@Dao
public interface ProfileDao {
    @Query("SELECT COUNT(*) FROM Profile WHERE uid = :uid")
    Cursor hasProfile(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(Profile profile);

    @Delete
    int deleteProfile(Profile profile);

    @Query("SELECT * FROM Profile WHERE number = :number")
    Profile getProfileFromNumber(String number);

    @Query("SELECT * FROM Profile WHERE uid = :uid")
    Profile getProfileFromUid(String uid);
}
