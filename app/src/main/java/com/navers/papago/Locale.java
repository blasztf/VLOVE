package com.navers.papago;

import android.support.annotation.NonNull;

public class Locale {
    static Locale KOREAN = new Locale("ko");
    static Locale ENGLISH = new Locale("en");
    static Locale JAPANESE = new Locale("ja");
    static Locale CHINESE = new Locale("zh-CN");
    static Locale CHINESE_TRADITIONAL = new Locale("zh-TW");
    static Locale SPANISH = new Locale("es");
    static Locale FRENCH = new Locale("fr");
    static Locale VIETNAMESE = new Locale("vi");
    static Locale THAI = new Locale("th");
    static Locale BAHASA = new Locale("id");

    private String mLocale;

    private Locale(String locale) {
        mLocale = locale;
    }

    @NonNull
    @Override
    public String toString() {
        return mLocale;
    }
}
