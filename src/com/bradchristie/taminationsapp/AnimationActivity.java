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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bradchristie.taminationsapp.AnimationView.AnimationThread;

public class AnimationActivity extends PortraitActivity
             implements AnimationListener,
                        SharedPreferences.OnSharedPreferenceChangeListener,
                        SeekBar.OnSeekBarChangeListener
{

  private class AnimationUpdater implements Runnable
  {
    private int action;
    private double loc;
    private double beat;
    public AnimationUpdater(int action, double loc, double beat)
    {
      this.action = action;
      this.loc = loc;
      this.beat = beat;
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
        //  Fade out any Taminator text
        int a = (int)Math.floor(Math.max(-beat/2.01,0.0)*256.0);
        TextView tamsaysview = (TextView)findViewById(R.id.text_tamsays);
        //  setAlpha would be easier but not available for API 10
        tamsaysview.setTextColor(a<<24);
        tamsaysview.setBackgroundColor(((a*3/4)<<24) | 0x00ffffff);
        break;
      case ANIMATION_DONE :
        ImageButton playbutton = (ImageButton)findViewById(R.id.button_play);
        playbutton.setSelected(false);
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
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    setTitle(prefs.getString("name","no title"));
    String xmlname = prefs.getString("link", getString(android.R.string.untitled))
        .replace("html", "xml");
    String level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(Tamination.levelDir2Name(level));
    mAnimationView = (AnimationView)findViewById(R.id.animation);
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
    AnimationThread th = mAnimationView.getThread();
    if (th != null)  // sanity check
      th.doPause(); // pause animation when Activity pauses
  }
  public void onWindowFocusChanged (boolean hasFocus)
  {
    super.onWindowFocusChanged(hasFocus);
    //  Set the play button back to 'Play'
    ImageButton playbutton = (ImageButton)findViewById(R.id.button_play);
    playbutton.setSelected(false);
  }

  @Override
  public void onAnimationChanged(int action, double loc, double beat)
  {
    AnimationUpdater a = new AnimationUpdater(action, loc, beat);
    runOnUiThread(a);
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
      double loc = progress * mAnimationThread.getBeats() / seekBar.getMax();
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
