package com.shubham.backspace.models;

import android.webkit.JavascriptInterface;

import com.shubham.backspace.callerActivity;

public class InterfaceJava {

    // it will notify when our peer gets connected
    callerActivity callActivity;

    public InterfaceJava(callerActivity callActivity) {
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callerActivity.onPeerConnected();
    }

}
