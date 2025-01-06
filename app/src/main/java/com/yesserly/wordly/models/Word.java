package com.yesserly.wordly.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Objects;

@Entity
public class Word {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String word;
    private Long timestamp;
    private boolean custom;
    private boolean won;
    private boolean rewinded;
    private ArrayList<String> trys;
    private String definition;

    public Word(String word, Long timestamp, boolean custom, boolean won, boolean rewinded, String definition) {
        this.word = word;
        this.timestamp = timestamp;
        this.custom = custom;
        this.won = won;
        this.rewinded = rewinded;
        this.definition = definition;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public ArrayList<String> getTrys() {
        return trys;
    }

    public void setTrys(ArrayList<String> trys) {
        this.trys = trys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(id, word.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isRewinded() {
        return rewinded;
    }

    public void setRewinded(boolean rewinded) {
        this.rewinded = rewinded;
    }
}
