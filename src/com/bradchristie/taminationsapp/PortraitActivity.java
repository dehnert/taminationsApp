package com.bradchristie.taminationsapp;

import android.os.Bundle;

public class PortraitActivity extends RotationActivity
{
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    //  Don't show a portrait-only activity if this
    //  is a large device in landscape orientation
    if (!isPortrait() && !getResources().getBoolean(R.bool.portrait_only))
      finish();
  }

}
