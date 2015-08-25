/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2015 Brad Christie

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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

public abstract class RotationActivity extends FragmentActivity
                              implements Animation.AnimationListener
{

  public interface FragmentFactory
  {
    RotationFragment getFragment();
  }
  private FragmentFactory ff;

  private View fragmentview;
  private int fid;

  public String intentString(String key)
  {
    return getIntent().getStringExtra(key);
  }
  public int intentInt(String key)
  {
    return getIntent().getIntExtra(key, 0);
  }

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

  protected String shareURL()
  {
    return null;
  }

  public void onShareClicked(View v)
  {
    String url = shareURL();
    if (url != null) {
      Intent sendIntent = new Intent();
      sendIntent.setAction(Intent.ACTION_SEND);
      sendIntent.putExtra(Intent.EXTRA_TEXT, url);
      sendIntent.setType("text/plain");
      startActivity(Intent.createChooser(sendIntent, "Share via"));
    }
  }

  public void onLogoClicked(View v)
  {
    //  An intent with nothing specified to view will show the levels
    //  Copy the current intent so landscape will show the current level
    //  Set action to MAIN to pop to the top
    startActivity(new Intent(getIntent())
        .setAction(Intent.ACTION_MAIN).setClass(this,LevelActivity.class));
  }

  public void onLevelClicked(View v)
  {
    //  Build an intent to view the specific level
    //  Set action to VIEW to show the list of calls in portrait mode
    LevelData d = LevelData.find(((TextView)v).getText().toString());
    Uri u = Uri.parse("intent://view/"+d.dir);
    startActivity(new Intent(Intent.ACTION_VIEW,u,this,LevelActivity.class));
  }

  public void onSpeakerClicked(View v)
  {
    //SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    //String level = prefs.getString("level", "");
    //level = LevelData.find(level).dir;
    //Tamination.playCallName(this, level, title);
  }

  public boolean isPortrait()
  {
    return getResources().getConfiguration().orientation
                          == Configuration.ORIENTATION_PORTRAIT ||
           getResources().getBoolean(R.bool.portrait_only);
  }

  public void setTitle(String title)
  {
    //  Remove extra stuff like (DBD) or (A-1) from title
    title = title.replaceAll("\\(.*?\\)","");
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
    //  SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    //  String level = prefs.getString("level", "");
    //  level = LevelData.find(level).dir;
    //  boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(title));
      findViewById(R.id.speaker).setVisibility( /* hasAudio ? View.VISIBLE : */ View.GONE);
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
