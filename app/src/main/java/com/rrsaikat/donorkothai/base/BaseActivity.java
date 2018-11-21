package com.rrsaikat.donorkothai.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


abstract public class BaseActivity extends AppCompatActivity {


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  protected void showSnackBar(String message) {
    showSnackBar(message, findViewById(android.R.id.content));
  }

  protected void showSnackBar(String message, View view) {
    Snackbar snackBar =
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
    View sbView = snackBar.getView();
    TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
    textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    snackBar.show();
  }

  protected void setTitle(String title) {
    if (title == null) return;
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }
  }
}
