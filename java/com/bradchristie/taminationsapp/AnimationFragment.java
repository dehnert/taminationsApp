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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class AnimationFragment extends RotationFragment
                               implements AnimationListener,
                                          SeekBar.OnSeekBarChangeListener
{

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
        Activity act = getActivity();
        SliderTicView ticView = act==null ? null : (SliderTicView)getActivity().findViewById(R.id.slidertics);
        if (ticView != null) {
          ticView.setTics(mAnimationView.getTotalBeats(),mAnimationView.getParts());
        }
        break;
      case ANIMATION_PROGRESS :
        //  Position slider to current location
        //  It's possible for this to be called as the fragment is swapped out,
        //  so be extra special careful
        act = getActivity();
        SeekBar sb = act == null ? null : (SeekBar)act.findViewById(R.id.seekBar1);
        if (sb != null) {
          float m = (float)sb.getMax();
          sb.setProgress((int)(loc*m));
          //  Fade out any Taminator text
          int a = (int)Math.floor(Math.max(-beat/2.01,0.0)*256.0);
          TextView tamsaysview = (TextView)getActivity().findViewById(R.id.text_tamsays);
          //  setAlpha would be easier but not available for API 10
          tamsaysview.setTextColor(a<<24);
          tamsaysview.setBackgroundColor(((a*3/4)<<24) | 0x00ffffff);
        }
        break;
      case ANIMATION_DONE :
        act = getActivity();
        ImageButton playbutton = act==null ? null : (ImageButton)getActivity().findViewById(R.id.button_play);
        if (playbutton != null)
          playbutton.setSelected(false);
        break;
      case ANIMATION_PART :
        //  A bit of a hack to pass the current part to the definition
        act = getActivity();
        if (act != null && act.getClass() == AnimListActivity.class)
          ((AnimListActivity) act).setPart((int) loc);
        break;
      }
    }
  }

  /** A handle to the View in which the animation is running. */
  public AnimationView mAnimationView;

  private View fragment;

  public void readSettings(int setting)
  {
    mAnimationView.readSettings();
    if (setting == AnimationSettingsListener.GEOMETRY_SETTING_CHANGED)
      resetAnimation();
  }

  public void resetAnimation()
  {
    //  Load the animation xml file
    String xmlname = intentString("link");
    int anim = intentInt("anim");
    //  Read the xml file and select the requested animation
    Document tamdoc = Tamination.getXMLAsset(getActivity(), xmlname);
    Element tam = (Element)Tamination.tamList(tamdoc).item(anim);
    if (tam != null) {
      setTitle(tam.getAttribute("title"));
      //  For sharing, store the param for linking
      String title = tam.getAttribute("title");
      String from = tam.getAttribute("from");
      String animname = title + "from" + from;
      String group = tam.getAttribute("group");
      if (group != null && group.length() > 1)
        animname = title;
      getArguments().putString("url", "http://www.tamtwirlers.org/tamination/" + intentString("link") + ".html?"
          +  animname.replaceAll("\\W",""));
    }
    //  Display any Taminator quote
    if (tam != null) {
      NodeList tamsayslist = Tamination.tamXref(getActivity(),tam).getElementsByTagName("taminator");
      if (tamsayslist.getLength() > 0) {
        Element tamsayselem = (Element)tamsayslist.item(0);
        //  Clean up extra white space in the XML
        String tamsays = tamsayselem.getTextContent().trim();
        tamsays = tamsays.replaceAll("\\n\\s+", " ");
        TextView tamsaysview = (TextView)fragment.findViewById(R.id.text_tamsays);
        tamsaysview.setText(tamsays);
      }

      //  Pass the animation definition to the code that generates the animation
      mAnimationView.setAnimation(tam);
      //  Reset the slider display
      SliderTicView ticView = (SliderTicView)getActivity().findViewById(R.id.slidertics);
      if (ticView != null)
        ticView.setTics(mAnimationView.getTotalBeats(),mAnimationView.getParts());
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    fragment = inflater.inflate(R.layout.fragment_animation, container, false);
    // get handles to the AnimationView from XML, and its AnimationThread
    mAnimationView = (AnimationView)fragment.findViewById(R.id.animation);
    mAnimationView.setAnimationListener(this);
    SeekBar sb = (SeekBar)fragment.findViewById(R.id.seekBar1);
    sb.setOnSeekBarChangeListener(this);
    //resetAnimation();
    //  Add listeners for buttons
    View rewindButton = fragment.findViewById(R.id.button_rewind);
    rewindButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.doPrevPart();
      }
    });
    Button prevButton = (Button)fragment.findViewById(R.id.button_prev);
    prevButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.doBackup();
      }
    });
    Button forwardButton = (Button)fragment.findViewById(R.id.button_next);
    forwardButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.doForward();
      }
    });
    View endButton = fragment.findViewById(R.id.button_end);
    endButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.doNextPart();
      }
    });
    fragment.findViewById(R.id.button_play).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (mAnimationView.running()) {
          mAnimationView.doPause();
          v.setSelected(false);
        }
        else {
          mAnimationView.doStart();
          v.setSelected(true);
        }
      }
    });
    //  Add hook for long-press on forward button
    endButton.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.doEnd();
          return true;
        }
      }
    );
    //  Add hook for long-press on prev button
    rewindButton.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.doRewind();
          return true;
        }
      }
    );
    return fragment;
  }

  public void onResume()
  {
    super.onResume();
    resetAnimation();
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  public void onPause() {
    super.onPause();
      mAnimationView.doPause(); // pause animation when Activity pauses
  }

  @Override
  public void onAnimationChanged(int action, double loc, double beat, double z)
  {
    AnimationUpdater a = new AnimationUpdater(action, loc, beat);
    Activity act = getActivity();
    if (act != null)
      act.runOnUiThread(a);
  }


  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    if (fromUser) {
      double b = mAnimationView.getTotalBeats();
      double loc = progress * b / seekBar.getMax();
      mAnimationView.setLocation(loc);
    }
  }

  //  Called when the user starts dragging the slider
  @Override
  public void onStartTrackingTouch(SeekBar seekBar)
  {
    //  To avoid the jitters, stop running the animation
    mAnimationView.doPause();
  }

  //  Called when the user stops dragging the slider
  @Override
  public void onStopTrackingTouch(SeekBar seekBar)
  {
    //  If the animation was running, resume
    View v = fragment.findViewById(R.id.button_play);
    if (v.isSelected())
      mAnimationView.doStart();
  }


}
