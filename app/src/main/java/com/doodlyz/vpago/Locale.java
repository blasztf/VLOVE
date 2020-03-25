package com.doodlyz.vpago;

//import android.support.annotation.NonNull;

import android.support.annotation.Nullable;

public class Locale {
    public static Locale KOREAN = new Locale("ko", "Korean");
    public static Locale ENGLISH = new Locale("en", "English");
    public static Locale JAPANESE = new Locale("ja", "Japanese");
    public static Locale CHINESE = new Locale("zh-CN", "Chinese");
    public static Locale CHINESE_TRADITIONAL = new Locale("zh-TW", "Chinese Tra.");
    public static Locale SPANISH = new Locale("es", "Spanish");
    public static Locale FRENCH = new Locale("fr", "French");
    public static Locale VIETNAME = new Locale("vi", "Vietnamese");
    public static Locale THAILAND = new Locale("th", "Thailand");
    public static Locale INDONESIA = new Locale("id", "Indonesia");
    public static Locale AUTO = new Locale(null, null, true);

    private String mLocale, mCountry;
    private boolean mEnableAutoDetection;

    private Locale(String locale, String country, boolean useAutoDetection) throws LocaleException {
        mEnableAutoDetection = useAutoDetection;
        locale = getVLiveCompatibility(locale);
        if (isLocaleOk(locale)) {
            mLocale = locale;
            mCountry = country;

            if (mCountry == null) {
                mCountry = getCountry(locale);
            }
        }
        else mLocale = null; //throw new LocaleException("But '" + locale + "' was given.");
    }

    public Locale(String locale, String country) throws LocaleException {
        this(locale, country, false);
    }

    public Locale(String locale) throws LocaleException {
        this(locale, null);
    }

    private static class LocaleException extends RuntimeException {
        private String msg;

        LocaleException(String message) {
//            super(message);
            msg = message;
        }

        @Override
        public String getMessage() {
            return "Only support : 'ko', 'en', 'ja', 'zh-CN', 'zh-TW', 'es', 'fr', 'vi', 'th', 'id'." + msg;
        }
    }

    void merge(Locale locale) {

    }

    private String getCountry(String locale) {
        if ("ko".equals(locale)) {
            return "Korea";
        }
        else if ("en".equals(locale)) {
            return "English";
        }
        else if ("ja".equals(locale)) {
            return "Japanese";
        }
        else if ("zh-CN".equals(locale)) {
            return "Chinese";
        }
        else if ("zh-TW".equals(locale)) {
            return "Chinese Tra.";
        }
        else if ("es".equals(locale)) {
            return "Spanish";
        }
        else if ("fr".equals(locale)) {
            return "France";
        }
        else if ("vi".equals(locale)) {
            return "Vietname";
        }
        else if ("th".equals(locale)) {
            return "Thailand";
        }
        else if ("id".equals(locale)){
            return "Indonesia";
        }
        else {
            return null;
        }
    }

    private boolean isLocaleOk(String locale) {
        if ("ko".equals(locale)) {
            return true;
        }
        else if ("en".equals(locale)) {
            return true;
        }
        else if ("ja".equals(locale)) {
            return true;
        }
        else if ("zh-CN".equals(locale)) {
            return true;
        }
        else if ("zh-TW".equals(locale)) {
            return true;
        }
        else if ("es".equals(locale)) {
            return true;
        }
        else if ("fr".equals(locale)) {
            return true;
        }
        else if ("vi".equals(locale)) {
            return true;
        }
        else if ("th".equals(locale)) {
            return true;
        }
        else if ("id".equals(locale)){
            return true;
        }
        else {
            return "".equals(locale) || locale == null;
        }
    }

    private String getVLiveCompatibility(String locale) {
        if ("so".equals(locale) || "sl".equals(locale)) {
            return "es";
        }
        else if ("zh-hans".equals(locale)) {
            return "zh-CW";
        }
        return locale;
    }

    public boolean useAutoDetection() {
        return mEnableAutoDetection;
    }

    public boolean isSupported() {
        return mLocale != null;
    }

    public boolean equals(@Nullable Locale obj) {
        if (obj != null) {
            if (obj.mLocale != null) {
                return obj.mLocale.equals(mLocale);
            }
        }
        return mLocale == null;
    }

    @Override
    public String toString() {
        return mLocale;
    }

    public String toCountryString() {
        return mCountry;
    }
}
