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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AnimationActivity extends PortraitActivity
             implements AnimationListener,
                        SharedPreferences.OnSharedPreferenceChangeListener,
                        SeekBar.OnSeekBarChangeListener
{

  private AnimationFragment af;

  public void onLogoClicked(View view) {
    super.onLogoClicked(view);
  }

  public void onSpeakerClicked(View view) {
  }

  public void onLevelClicked(View view) {
    super.onLevelClicked(view);
  }

  private class AnimationUpdater implements Runnable
  {
    private final int action;
    private final double loc;
    private final double beat;
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
        AnimationView av = (AnimationView)findViewById(R.id.animation);
        SliderTicView ticView = (SliderTicView)findViewById(R.id.slidertics);
        ticView.setTics(av.getTotalBeats(),av.getParts());
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
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animation);
    setTitle(intentString("title"));
    String xmlname = intentString("link").replace("html", "xml");
    String level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(LevelData.find(level).name);
    //boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(title));
    //findViewById(R.id.speaker).setVisibility( /* hasAudio ? View.VISIBLE : */ View.GONE);
  }


  //  Definition
  public void onButtonDefinitionClicked(View v) {
    startActivity(getIntent().setClass(this, DefinitionActivity.class));
  }
  //  Settings
  public void onButtonSettingsClicked(View v) {
    startActivity(new Intent(this, SettingsActivity.class));
  }

  //  Link to use with share button
  @Override
  protected String shareURL()
  {
    return af.intentString("url");
  }

  @Override
  protected void onResume() {
    super.onResume();
    af = new AnimationFragment();
    af.setArguments(getIntent().getExtras());
    replaceFragment(af, R.id.fragment_animation);
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  protected void onPause() {
    super.onPause();
    AnimationView av = (AnimationView)findViewById(R.id.animation);
    av.doPause(); // pause animation when Activity pauses
  }
  public void onWindowFocusChanged (boolean hasFocus)
  {
    super.onWindowFocusChanged(hasFocus);
    //  Set the play button back to 'Play'
    ImageButton playbutton = (ImageButton)findViewById(R.id.button_play);
    playbutton.setSelected(false);
  }

  @Override
  public void onAnimationChanged(int action, double loc, double beat, double z)
  {
    AnimationUpdater a = new AnimationUpdater(action, loc, beat);
    runOnUiThread(a);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
  {
    AnimationView av = (AnimationView)findViewById(R.id.animation);
    av.setGridVisibility(prefs.getBoolean("grid",false));
    av.setPathVisibility(prefs.getBoolean("paths",false));
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    if (fromUser) {
      AnimationView av = (AnimationView)findViewById(R.id.animation);
      double b = av.getTotalBeats();
      double loc = progress * b / seekBar.getMax();
      av.setLocation(loc);
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
