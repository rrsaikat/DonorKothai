package com.rrsaikat.donorkothai.injection;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.rrsaikat.donorkothai.storage.SharedPreferenceManager;



final public class Injection {
  private Injection() {
  }

  public static FirebaseAuth provideFireBaseAuth() {
    return FirebaseAuth.getInstance();
  }

  public static FirebaseDatabase provideFireBaseDatabase() {
    return FirebaseDatabase.getInstance();
  }

  public static SharedPreferenceManager getSharedPreference() {
    return SharedPreferenceManager.getInstance();
  }


}
