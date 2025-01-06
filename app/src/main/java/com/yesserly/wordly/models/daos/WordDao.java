package com.yesserly.wordly.models.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.yesserly.wordly.models.Word;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface WordDao {
    //------------------------------------------ Inserts
    @Insert
    Long insertWord(Word word);

    //------------------------------------------ Queries
    @Query("SELECT * FROM Word WHERE custom = :custom ORDER BY timestamp DESC LIMIT 1")
    Word getLastWord(boolean custom);

    @Query("SELECT * FROM Word WHERE custom = :custom")
    List<Word> getWords(boolean custom);

    //------------------------------------------ Updates
    @Update
    void updateWord(Word word);

    //------------------------------------------ Deletes
    @Query("DELETE FROM Word WHERE custom = 1")
    void clearCustoms();
}
