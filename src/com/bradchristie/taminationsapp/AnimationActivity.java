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
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bradchristie.taminationsapp.AnimationView.AnimationThread;

public class AnimationActivity extends Activity
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
    //  Remove title bar
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
    //  Set title and scale it to fit header space
    TextView titleView = (TextView)findViewById(R.id.animation_title);
    String titlestr = tam.getAttribute("title");
    if (titlestr.length() > 40)
      titleView.setTextSize(18.0f);
    else if (titlestr.length() > 16)
      titleView.setTextSize(24.0f);
    else
      titleView.setTextSize(36.0f);
    titleView.setText(titlestr);
    //  Display any Taminator quote
    NodeList tamsayslist = tam.getElementsByTagName("taminator");
    if (tamsayslist.getLength() > 0) {
      Element tamsayselem = (Element)tamsayslist.item(0);
      //  Clean up extra white space in the XML
      String tamsays = tamsayselem.getTextContent().trim();
      tamsays = tamsays.replaceAll("\\n\\s+", " ");
      TextView tamsaysview = (TextView)findViewById(R.id.text_tamsays);
      tamsaysview.setText(tamsays);
    }

    //  Pass the animation definition to the code that generates the animation
    mAnimationView.setAnimation(tam);
    //  Add hook for long-press on forward button
    ImageButton bForward = (ImageButton)findViewById(R.id.button_end);
    bForward.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.getThread().doEnd();
          return true;
        }
      }
    );
    //  Add hook for long-press on prev button
    ImageButton bPrev = (ImageButton)findViewById(R.id.button_rewind);
    bPrev.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.getThread().doRewind();
          return true;
        }
      }
    );

  }

  //  Handlers for button clicks
  //  Rewind
  public void onButtonRewindClicked(View v) {
    mAnimationView.getThread().doPrevPart();
  }
  //  Backup
  public void onButtonBackupClicked(View v) {
    mAnimationView.getThread().doBackup();
  }

  //    Forward
  public void onButtonForwardClicked(View v) {
    mAnimationView.getThread().doForward();
  }

  //  End
  public void onButtonEndClicked(View v) {
    mAnimationView.getThread().doNextPart();
  }
  //  Play
  public void onButtonPlayClicked(View v) {
    ImageButton playbutton = (ImageButton)v;
    mAnimationThread = mAnimationView.getThread();
    if (mAnimationThread.running()) {
      mAnimationThread.doPause();
      playbutton.setSelected(false);
    }
    else {
      mAnimationThread.doStart();
      playbutton.setSelected(true);
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
