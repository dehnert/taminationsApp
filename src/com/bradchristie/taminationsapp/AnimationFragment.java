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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bradchristie.taminationsapp.AnimationView.AnimationThread;

public class AnimationFragment extends Fragment
                               implements AnimationListener,
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
        SliderTicView ticView = (SliderTicView)getActivity().findViewById(R.id.slidertics);
        mAnimationThread = mAnimationView.getThread();
        ticView.setTics(mAnimationThread.getBeats(),mAnimationThread.getParts());
        break;
      case ANIMATION_PROGRESS :
        //  Position slider to current location
        SeekBar sb = (SeekBar)getActivity().findViewById(R.id.seekBar1);
        float m = (float)sb.getMax();
        sb.setProgress((int)(loc*m));
        //  Fade out any Taminator text
        int a = (int)Math.floor(Math.max(-beat/2.01,0.0)*256.0);
        TextView tamsaysview = (TextView)getActivity().findViewById(R.id.text_tamsays);
        //  setAlpha would be easier but not available for API 10
        tamsaysview.setTextColor(a<<24);
        tamsaysview.setBackgroundColor(((a*3/4)<<24) | 0x00ffffff);
        break;
      case ANIMATION_DONE :
        ImageButton playbutton = (ImageButton)getActivity().findViewById(R.id.button_play);
        playbutton.setSelected(false);
        break;
      }
    }
  };

  /** A handle to the thread that's actually running the animation. */
  private AnimationThread mAnimationThread;

  /** A handle to the View in which the animation is running. */
  private AnimationView mAnimationView;

  private View fragment;

  public void resetAnimation()
  {
    //  Load the animation xml file
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    String xmlname = prefs.getString("link",getString(android.R.string.untitled)).replace("html", "xml");
    int anim = prefs.getInt("anim",0);
    //  Read the xml file and select the requested animation
    Document tamdoc = Tamination.getXMLAsset(getActivity(), xmlname);
    Element tam = (Element)tamdoc.getElementsByTagName("tam").item(anim);
    //  Set title and scale it to fit header space
    TextView titleView = (TextView)fragment.findViewById(R.id.animation_title);
    if (titleView != null) {
      String titlestr = tam.getAttribute("title");
      if (titlestr.length() > 40)
        titleView.setTextSize(18.0f);
      else if (titlestr.length() > 16)
        titleView.setTextSize(24.0f);
      else
        titleView.setTextSize(36.0f);
      titleView.setText(titlestr);
    }
    //  Display any Taminator quote
    NodeList tamsayslist = tam.getElementsByTagName("taminator");
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
      ticView.setTics(mAnimationThread.getBeats(),mAnimationThread.getParts());

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
    resetAnimation();
    //  Add listeners for buttons
    fragment.findViewById(R.id.button_rewind).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.getThread().doPrevPart();
      }
    });
    ImageButton prevButton = (ImageButton)fragment.findViewById(R.id.button_prev);
    prevButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.getThread().doBackup();
      }
    });
    ImageButton forwardButton = (ImageButton)fragment.findViewById(R.id.button_next);
    forwardButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.getThread().doForward();
      }
    });
    fragment.findViewById(R.id.button_end).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationView.getThread().doNextPart();
      }
    });
    fragment.findViewById(R.id.button_play).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mAnimationThread = mAnimationView.getThread();
        if (mAnimationThread.running()) {
          mAnimationThread.doPause();
          v.setSelected(false);
        }
        else {
          mAnimationThread.doStart();
          v.setSelected(true);
        }
      }
    });
    //  Add hook for long-press on forward button
    forwardButton.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.getThread().doEnd();
          return true;
        }
      }
    );
    //  Add hook for long-press on prev button
    prevButton.setOnLongClickListener(new View.OnLongClickListener() {
        public boolean onLongClick(View v) {
          mAnimationView.getThread().doRewind();
          return true;
        }
      }
    );
    return fragment;
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  public void onPause() {
    super.onPause();
    AnimationThread th = mAnimationView.getThread();
    if (th != null)  // sanity check
      th.doPause(); // pause animation when Activity pauses
  }

  @Override
  public void onAnimationChanged(int action, double loc, double beat)
  {
    AnimationUpdater a = new AnimationUpdater(action, loc, beat);
    getActivity().runOnUiThread(a);
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
