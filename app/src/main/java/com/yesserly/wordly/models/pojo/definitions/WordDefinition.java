package com.yesserly.wordly.models.pojo.definitions;

public class WordDefinition {
    private String word;
    private Meaning[] meanings;

    public WordDefinition(String word, Meaning[] meanings) {
        this.word = word;
        this.meanings = meanings;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Meaning[] getMeanings() {
        return meanings;
    }

    public void setMeanings(Meaning[] meaning) {
        this.meanings = meaning;
    }
}
