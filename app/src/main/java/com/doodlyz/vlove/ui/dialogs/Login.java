package com.doodlyz.vlove.ui.dialogs;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@BaseDialog.DialogId("Login")
public class Login extends BaseDialog {
//    private static final String AUTH_FORMAT = "{ \"message\" : { \"cookie\" : \"%s\", \"token\" : \"%s\", \"snsType\" : \"%s\", \"nickName\" : \"%s\", \"login\" : %b, \"timeStamp\" : %d } }";
    private static final long MAX_LIMIT_LOGIN_TIME = 1000L * 60L * 60L * 24L; // Expired on 1 day.

    public static synchronized Login with(Context context) {
        return new Login(context);
    }

    private Login(Context context) {
        super(context);
    }

    public static final class LoginInfo {
        private boolean login;
        private String nickName;
        private String snsType;
        private String token;
        private String cookie;
        private long timestamp;

        /**
         * Constructor out.
         * @param loginInfoText
         */
        public LoginInfo(String loginInfoText) {
            parse(loginInfoText);
            if (cookie.isEmpty()) {
                throw new IllegalStateException("Bad formatted login info text!");
            }
        }

        /**
         * Constructor in.
         * @param response
         * @param cookie
         */
        LoginInfo(String response, String cookie) {
            parse(response);
            this.cookie = cookie;
        }

        public String getCookie() {
            return cookie;
        }

        public String getToken() {
            return token;
        }

        public String getSnsType() {
            return snsType;
        }

        public String getNickName() {
            return nickName;
        }

        public long getTimeStamp() {
            return timestamp;
        }

        public boolean isLogin() {
            return login;
        }

        public boolean shouldLogin() {
            return !isLogin() || System.currentTimeMillis() - getTimeStamp() > MAX_LIMIT_LOGIN_TIME;
        }

        @Override
        public String toString() {
            String message = new Gson().toJson(this);
            return String.format("{\n \"message\" : %s \n}", message);
//            return String.format(Locale.ENGLISH, AUTH_FORMAT, getCookie().replaceAll("\"", "\\\""), getToken(), getSnsType(), getNickName(), isLogin(), getTimeStamp());
        }

        private void parse(String json) {
            JsonObject root = new JsonParser().parse(json).getAsJsonObject().getAsJsonObject("message");
            token = root.get("token").getAsString();
            snsType = root.get("snsType").getAsString();
            nickName = root.get("nickName").getAsString();
            login = root.get("login").getAsBoolean();

            JsonElement e = root.get("timestamp");
            timestamp = e != null ? e.getAsLong() : System.currentTimeMillis();

            e = root.get("cookie");
            cookie = e != null ? e.getAsString() : "";
        }
    }

}
