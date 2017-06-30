package com.benoitquenaudon.tvfoot.red.app.injection;

import android.app.Activity;
import android.support.annotation.NonNull;
import com.benoitquenaudon.tvfoot.red.RedApp;
import com.benoitquenaudon.tvfoot.red.app.injection.component.ActivityComponent;
import com.benoitquenaudon.tvfoot.red.app.injection.component.ScreenComponent;
import com.benoitquenaudon.tvfoot.red.app.injection.module.ActivityModule;

public final class ComponentFactory {
  private ComponentFactory() {
    throw new RuntimeException("can't touch this");
  }

  @NonNull public static ScreenComponent screenComponent(Activity activity) {
    return RedApp.get(activity).getComponent().screenComponent();
  }

  @NonNull public static ActivityComponent activityComponent(ScreenComponent screenComponent,
      Activity activity) {
    return screenComponent.plus(new ActivityModule(activity));
  }
}