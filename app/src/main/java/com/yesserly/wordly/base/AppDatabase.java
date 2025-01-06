package com.yesserly.wordly.base;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.yesserly.wordly.models.Word;
import com.yesserly.wordly.models.daos.WordDao;
import com.yesserly.wordly.utils.RoomConverters;

@Database(entities = {Word.class}, version = 2)
@TypeConverters({RoomConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract WordDao wordDao();
}
