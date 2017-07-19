package com.benoitquenaudon.tvfoot.red;

import android.app.Application;
import android.content.Context;
import com.benoitquenaudon.tvfoot.red.app.injection.component.AppComponent;
import com.benoitquenaudon.tvfoot.red.app.injection.component.DaggerAppComponent;
import com.benoitquenaudon.tvfoot.red.app.injection.module.AppModule;
import com.squareup.leakcanary.LeakCanary;
import timber.log.Timber;

public class RedApp extends Application {
  AppComponent appComponent;

  public static RedApp get(Context context) {
    return (RedApp) context.getApplicationContext();
  }

  @Override public void onCreate() {
    super.onCreate();

    setupLeakCanary();
    setupTimber();
  }

  private void setupTimber() {
    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
  }

  private void setupLeakCanary() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This reduce is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this reduce.
      return;
    }
    LeakCanary.install(this);
  }

  public AppComponent getComponent() {
    if (appComponent == null) {
      appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }
    return appComponent;
  }
}
