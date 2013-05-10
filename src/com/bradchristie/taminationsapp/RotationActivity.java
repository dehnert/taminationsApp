/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2013 Brad Christie

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getResources().getBoolean(R.bool.portrait_only))
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    if (title != null)
      setTitle(title);
  }

  public boolean isPortrait()
  {
    Configuration config = getResources().getConfiguration();
    return config.orientation == Configuration.ORIENTATION_PORTRAIT;
  }

  public void setTitle(String t)
  {
    title = t;
    TextView titleView = (TextView)findViewById(R.id.title);
    if (titleView != null)
      titleView.setText(title);
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
