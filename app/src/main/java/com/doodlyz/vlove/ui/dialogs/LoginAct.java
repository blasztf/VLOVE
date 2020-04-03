package com.doodlyz.vlove.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.doodlyz.vlove.VloveSettings;
import com.doodlyz.vlove.R;
import com.doodlyz.vlove.VloveRequest;
import com.doodlyz.vlove.logger.CrashCocoExceptionHandler;

import java.util.Objects;

public final class LoginAct extends BaseDialogAct {

    private static final String URL_VLIVE = "https://www.vlive.tv/";
    private static final String URL_VLIVE_HOME = "https://www.vlive.tv/home";
    private static final String URL_VLIVE_LOGGED_IN = "#_=_";

//    private static final String EXTRA_LISTENER = "com.doodlyz.vlove.Login$Activity$EXTRA_LISTENER:LLoginListener";

    private RelativeLayout dialogScreen;
    private WebView webLogin;

    public LoginAct() {
        super();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_dialog_login;
    }

    @Override
    protected void onPrepareContentViewElement() {
        dialogScreen    = findViewById(R.id.dialogScreen);
        webLogin        = findViewById(R.id.webLogin);
    }

    @Override
    protected void onReady(Intent intent) {
        if (isAlreadyLogin()) {
            popupSuccess();
            finish();
        }
        else {
            clearCookie();
            setupWebLoginSettings();
            setupCookieEnvironment();
            setupWebLogin();
            startLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopSyncCookie();
    }

    @Override
    protected void onPause() {
        super.onPause();
        syncCookie();
    }

    private void popupSuccess() {
        Popup.with(this, Popup.ID_INFO)
                .make("Logged In")
                .show();
    }

    private void popupFailed() {
        Popup.with(this, Popup.ID_INFO)
                .make("Failed to log in")
                .show();
    }

    private boolean isAlreadyLogin() {
        Login.LoginInfo info = VloveSettings.getInstance(this).getLoginInfo();
        boolean alreadyLogin = info != null && !info.shouldLogin();
        if (alreadyLogin) {
            VloveRequest.with(this).setCookie(URL_VLIVE, info.getCookie());
        }
        return alreadyLogin;
    }

    private void userLoggedIn() {
        VloveRequest.ApiRequest request;
        VloveRequest.with(this).setCookie(URL_VLIVE, getCookieLogin());
        request = new VloveRequest.ApiRequest("https://www.vlive.tv/auth/loginInfo",
                response -> {
                    Login.LoginInfo loginInfo = new Login.LoginInfo(response, getCookieLogin());
                    if (loginInfo.isLogin()) {
                        VloveSettings.getInstance(LoginAct.this).setLoginInfo(loginInfo);
                        VloveRequest.with(this).setCookie(URL_VLIVE, loginInfo.getCookie());
                        popupSuccess();
                    }
                    else {
                        popupFailed();
                    }
                    finish();
                },
                error -> {
                    popupFailed();
                    finish();
                })
                .setReferer("https://www.vlive.tv/home")
                .setHost(VloveSettings.VLIVE_HOST)
                .addHeader("X-Requested-With", "XMLHttpRequest");
        VloveRequest.with(this).addToQueue(request);
    }

    private boolean isUserLoggedIn(String url) {
        return url.contains(URL_VLIVE_LOGGED_IN);
    }

    private void startLogin() {
        webLogin.loadUrl(URL_VLIVE);
        webLogin.zoomIn();
    }

    private void setupWebLogin() {
        webLogin.clearCache(true);
        webLogin.clearFormData();
        webLogin.clearHistory();
        webLogin.setWebChromeClient(new WebChromeClient());
        webLogin.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog = ProgressDialog.show(LoginAct.this, "Loading", "...");

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }

                if (isUserLoggedIn(url)) {
                    view.stopLoading();
                    userLoggedIn();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (url.contains(URL_VLIVE_HOME)) {
                    view.evaluateJavascript("(function () {" +
                            "var btnLogin = document.getElementsByClassName('btn_login')[0];" +
                            "btnLogin.click();" +
                            "})();", null);
                }
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebLoginSettings() {
        WebSettings settings = webLogin.getSettings();
        settings.setUserAgentString(VloveSettings.VLOVE_USER_AGENT);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setAppCachePath(Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    private void setupCookieEnvironment() {
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        else {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webLogin, true);
        }
    }

    private void syncCookie() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        }
        else {
            CookieManager.getInstance().flush();
        }
    }

    private void stopSyncCookie() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    private void clearCookie() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().removeSessionCookies(null);
        }
        else {
            CookieManager.getInstance().removeAllCookie();
            CookieManager.getInstance().removeSessionCookie();
            CookieManager.getInstance().removeExpiredCookie();
        }
    }

    private String getCookieLogin() {
        return CookieManager.getInstance().getCookie(URL_VLIVE);
    }
}
