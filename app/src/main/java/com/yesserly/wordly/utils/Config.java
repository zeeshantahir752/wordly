package com.yesserly.wordly.utils;
import com.yesserly.wordly.models.pojo.Language;

public class Config {

    public static int[] TIMES = {1, 3, 6, 12, 24};
    public static Language[] LANGUAGES = {
            new Language("English",
                    "qwertyuiopasdfghjklzxcvbnm",
                    false,
                    new String[]{"4_letters.csv", "5_letters.csv", "6_letters.csv"},
                    new int[]{4,5,6}
            ),
            new Language("Arabic",
                    "يوهنملكقفغعظطضصشسزرذدخحجثتبا",
                    true,
                    new String[]{"ar_4_letters.csv", "ar_5_letters.csv", "ar_6_letters.csv"},
                    new int[]{4,5,6}
            )
    };
    public static String FIRST_DATE = "14-02-2022";

    //For Notifications
    public static final String CHANNEL_ID = "Wordly";
    public static final String DATABASE_NAME = "Wordly";
}
