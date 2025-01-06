package com.yesserly.wordly.models.pojo;

public class Letter {

    private final int letters_number;
    private final String file_name;

    public Letter(int letters_number, String file_name) {
        this.letters_number = letters_number;
        this.file_name = file_name;
    }

    public int getLetters_number() {
        return letters_number;
    }

    public String getFile_name() {
        return file_name;
    }

}
