/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2014 Brad Christie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.bradchristie.taminationsapp;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

public class RotationActivity extends FragmentActivity
                              implements Animation.AnimationListener
{

  public interface FragmentFactory
  {
    public RotationFragment getFragment();
  }
  private FragmentFactory ff;

  private String title;
  private View fragmentview;
  private int fid;

  protected boolean isLandscapeActivity()
  {
    return false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (!isLandscapeActivity() && getResources().getBoolean(R.bool.portrait_only))
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    if (title != null)
      setTitle(title);
  }

  protected void onResume()
  {
    super.onResume();
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String upto = prefs.getString("navigateupto", "");
    if (upto.equals(getClass().getSimpleName()) || isTaskRoot())
      prefs.edit().remove("navigateupto").commit();
    else if (upto.length() > 0)
      finish();
  }

  public void onLogoClicked(View v)
  {
    //  not available for Android 2
    //navigateUpTo(new Intent(this, LevelActivity.class));
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("navigateupto", "LevelActivity").commit();
    finish();
  }

  public void onLevelClicked(View v)
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    LevelData d = LevelData.find((String) (((TextView)v).getText()));
    prefs.edit().putString("level",d.name)
                .putString("selector",d.selector).commit();
    prefs.edit().putString("navigateupto", "CalllistActivity").commit();
    finish();
  }

  public void onSpeakerClicked(View v)
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String level = prefs.getString("level", "");
    level = LevelData.find(level).dir;
    Tamination.playCallName(this, level, title);
  }

  public boolean isPortrait()
  {
    return getResources().getConfiguration().orientation
                          == Configuration.ORIENTATION_PORTRAIT ||
           getResources().getBoolean(R.bool.portrait_only);
  }

  public void setTitle(String title)
  {
    TextView titleView = (TextView)findViewById(R.id.title);
    if (titleView != null) {
      if (isPortrait()) {
        if (title.length() > 40)
          titleView.setTextSize(18.0f);
        else if (title.length() > 16)
          titleView.setTextSize(24.0f);
        else
          titleView.setTextSize(36.0f);
      }
      titleView.setText(title);
    }
    //  Show speaker only if audio is availble
    if (findViewById(R.id.speaker) != null) {
      SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
      String level = prefs.getString("level", "");
      level = LevelData.find(level).dir;
      boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(title));
      findViewById(R.id.speaker).setVisibility(hasAudio ? View.VISIBLE : View.GONE);
    }
  }

  public void replaceFragment(RotationFragment replacement, int id)
  {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.setCustomAnimations(R.anim.dropin, R.anim.dropout);
    ft.replace(id,replacement);
    ft.commit();
  }

  public void replaceFragment2(FragmentFactory f, int id)
  {
    ff = f;
    fid = id;
    fragmentview = findViewById(id);
    Animation dropout = AnimationUtils.loadAnimation(this,R.anim.dropout);
    dropout.setAnimationListener(this);
    fragmentview.startAnimation(dropout);
  }

  @Override
  public void onAnimationStart(Animation animation)
  {
  }

  @Override
  public void onAnimationEnd(Animation animation)
  {
    Animation dropin = AnimationUtils.loadAnimation(this,R.anim.dropin);
    fragmentview.startAnimation(dropin);
    RotationFragment rf = ff.getFragment();
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.setTransition(FragmentTransaction.TRANSIT_NONE);
    ft.replace(fid,rf);
    ft.commit();
  }

  @Override
  public void onAnimationRepeat(Animation animation) {  }

}
