package com.doodlyz.vpago;

public interface TranslatorListener {
    void onTranslated(Translator.Response translatedText);
    void onError(String errorText);
}
