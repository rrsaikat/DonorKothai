package com.rrsaikat.donorkothai.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rrsaikat.donorkothai.App;

public class NetworkUtil {

  public static boolean isOnline() {

    ConnectivityManager connectivityManager = (ConnectivityManager) App.getInstance()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager != null) {
      NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
      return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    return false;
  }
}
