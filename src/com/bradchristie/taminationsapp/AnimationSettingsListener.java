package com.bradchristie.taminationsapp;

public interface AnimationSettingsListener
{
  public static final int SPEED_SETTING_CHANGED = 1;
  public static final int LOOP_SETTING_CHANGED = 2;
  public static final int GRID_SETTING_CHANGED = 3;
  public static final int PATHS_SETTING_CHANGED = 4;
  public static final int NUMBERS_SETTING_CHANGED = 5;
  public static final int PHANTOMS_SETTING_CHANGED = 6;
  public static final int GEOMETRY_SETTING_CHANGED = 7;

  public void settingsChanged(int setting);
}
