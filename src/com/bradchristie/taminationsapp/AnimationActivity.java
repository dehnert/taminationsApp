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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.bradchristie.taminationsapp.AnimationView.AnimationThread;

public class AnimationActivity extends Activity
             implements AnimationListener,
                        SharedPreferences.OnSharedPreferenceChangeListener,
                        SeekBar.OnSeekBarChangeListener
{

  private class AnimationUpdater implements Runnable
  {
    private int action;
    private float loc;
    public AnimationUpdater(int action, float loc)
    {
      this.action = action;
      this.loc = loc;
    }
    public void run() {
      switch (action) {
      case ANIMATION_READY :
        //  Tell the tic view where to put the tic marks
        SliderTicView ticView = (SliderTicView)findViewById(R.id.slidertics);
        mAnimationThread = mAnimationView.getThread();
        ticView.setTics(mAnimationThread.getBeats(),mAnimationThread.getParts());
        break;
      case ANIMATION_PROGRESS :
        //  Position slider to current location
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
        float m = (float)sb.getMax();
        sb.setProgress((int)(loc*m));
        break;
      case ANIMATION_DONE :
        Button playbutton = (Button)findViewById(R.id.button_play);
        playbutton.setText(R.string.button_animation_play);
        break;
      }
    }
  };

  /** A handle to the thread that's actually running the animation. */
  private AnimationThread mAnimationThread;

  /** A handle to the View in which the animation is running. */
  private AnimationView mAnimationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animation);
    // get handles to the AnimationView from XML, and its AnimationThread
    mAnimationView = (AnimationView)findViewById(R.id.animation);
    mAnimationView.setAnimationListener(this);
    SeekBar sb = (SeekBar)findViewById(R.id.seekBar1);
    sb.setOnSeekBarChangeListener(this);
    //  Load the animation xml file
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String xmlname = prefs.getString("xmlname",getString(android.R.string.untitled));
    int anim = prefs.getInt("anim",0);
    //  Read the xml file and select the requested animation
    Document tamdoc = Tamination.getXMLAsset(this, xmlname);
    Element tam = (Element)tamdoc.getElementsByTagName("tam").item(anim);
    setTitle(tam.getAttribute("title"));
    //  Pass the animation definition to the code that generates the animation
    mAnimationView.setAnimation(tam);
  }

  //  Handlers for button clicks
  //  Rewind
  public void onButtonRewindClicked(View v) {
    mAnimationView.getThread().doRewind();
  }
  //  Backup
  public void onButtonBackupClicked(View v) {
    mAnimationView.getThread().doBackup();
  }
  //  Forward
  public void onButtonForwardClicked(View v) {
    mAnimationView.getThread().doForward();
  }
  //  End
  public void onButtonEndClicked(View v) {
    mAnimationView.getThread().doEnd();
  }
  //  Play
  public void onButtonPlayClicked(View v) {
    Button playbutton = (Button)v;
    mAnimationThread = mAnimationView.getThread();
    if (mAnimationThread.running()) {
      mAnimationThread.doPause();
      playbutton.setText(R.string.button_animation_play);
    }
    else {
      mAnimationThread.doStart();
      playbutton.setText(R.string.button_animation_stop);
    }
  }

  //  Definition
  public void onButtonDefinitionClicked(View v) {
    startActivity(new Intent(this,DefinitionActivity.class));
  }
  //  Settings
  public void onButtonSettingsClicked(View v) {
    startActivity(new Intent(this,SettingsActivity.class));
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  protected void onPause() {
    super.onPause();
    mAnimationView.getThread().doPause(); // pause animation when Activity pauses
  }
  public void onWindowFocusChanged (boolean hasFocus)
  {
    super.onWindowFocusChanged(hasFocus);
    //  Set the play button back to 'Play'
    Button playbutton = (Button)findViewById(R.id.button_play);
    playbutton.setText(R.string.button_animation_play);
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_animation, menu);
    return true;
  }

  @Override
  public void onAnimationChanged(int action, float loc)
  {
    AnimationUpdater a = new AnimationUpdater(action, loc);
    runOnUiThread(a);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
          case R.id.menu_settings:
              startActivity(new Intent(this,SettingsActivity.class));
              return true;
          case R.id.menu_definition:
            startActivity(new Intent(this,DefinitionActivity.class));
            return true;
          case R.id.menu_about:
            startActivity(new Intent(this,AboutActivity.class));
            return true;
          //case R.id.help:
          //    showHelp();
          //    return true;
          default:
              return super.onOptionsItemSelected(item);
      }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
  {
    mAnimationThread.setGridVisibility(prefs.getBoolean("grid",false));
    mAnimationThread.setPathVisibility(prefs.getBoolean("paths",false));
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    if (fromUser) {
      mAnimationThread = mAnimationView.getThread();
      float loc = (float)progress * mAnimationThread.getBeats()
                                  / (float)seekBar.getMax();
      mAnimationThread.setLocation(loc);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar)
  {
    //  nothing to do here
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar)
  {
    //  nothing to do here
  }

}
