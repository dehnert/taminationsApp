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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PracticeActivity extends RotationActivity
                              implements AnimationListener
{
  protected AnimationView av;
  private Document calldoc;
  private String link;
  private View buttonDefinition;
  private View fragmentDefinition;
  private SharedPreferences prefs;

  @Override
  protected boolean isLandscapeActivity()
  {
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_practice);
    prefs = getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    buttonDefinition = findViewById(R.id.button_practice_definition);
    fragmentDefinition = findViewById(R.id.fragment_definition);
    setTitle("Practice");
    hideExtraStuff();
    calldoc = Tamination.getXMLAsset(this,"src/calls.xml");
    av = (AnimationView)findViewById(R.id.animationview);
    av.setAnimationListener(this);
    nextAnimation();
  }

  protected void hideExtraStuff()
  {
    findViewById(R.id.practice_complete_panel).setVisibility(View.GONE);
    buttonDefinition.setVisibility(View.GONE);
    findViewById(R.id.fragment_definition).setVisibility(View.GONE);
  }

  protected void nextAnimation()
  {
    String gender = prefs.getString("gender", "boy");
    String selector = prefs.getString("selector","level='Basic and Mainstream' and @sublevel!='Styling'");
    NodeList calls = Tamination.evalXPath("/calls/call[@"+selector+"]",calldoc);
    for (boolean found=false; !found;) {
      int callnum = (int)(Math.random()*calls.getLength());
      Element e = (Element)calls.item(callnum);
      link = e.getAttribute("link");
      String xmlname = link + ".xml";
      Document tamdoc = Tamination.getXMLAsset(this, xmlname);
      NodeList tamlist = tamdoc.getElementsByTagName("tam");
      if (tamlist.getLength() > 0) {
        int animnum = (int)(Math.random()*tamlist.getLength());
        Element tam = (Element)tamdoc.getElementsByTagName("tam").item(animnum);
        //  For now, skip any "difficult" animations
        if (tam.getAttribute("difficulty").equals("3"))
          continue;
        //  Skip any call with parens in the title - it could be a cross-reference
        //  to a concept call from a higher level
        if (tam.getAttribute("title").contains("("))
          continue;
        setTitle(tam.getAttribute("title"));
        av.setSquare();
        av.setAnimation(tam, gender.equals("Boy") ? Dancer.BOY : Dancer.GIRL);
        found = true;
        //  Save link for definition
        getIntent().putExtra("link",link);
      }
    }
  }

  //  This is a hook for TutorialActivity, which postpones the start
  //  until the user dismisses the instructions
  protected void animationReady()
  {
    av.doStart();
  }

  @Override
  public void onAnimationChanged(int action, double x, double y, double z)
  {
    if (action == AnimationListener.ANIMATION_READY) {
      runOnUiThread(new Runnable() {
        public void run() {
          hideExtraStuff();
        }});
      //  Force some settings required for practice
      av.setSpeed(prefs.getString("practicespeed","Slow"));
      av.setLoop(false);
      av.setGridVisibility(true);
      av.setPathVisibility(false);
      av.setPhantomVisibility(false);
      av.setNumbers(Dancer.NUMBERS_OFF);
      animationReady();
    }
    if (action == AnimationListener.ANIMATION_PROGRESS) {
      final int iscore = (int)Math.ceil(av.getScore());
      runOnUiThread(new Runnable() {
        public void run() {
          TextView scoreview = (TextView)findViewById(R.id.text_score);
          scoreview.setText("Score: "+iscore);
        }});
    }
    if (action == AnimationListener.ANIMATION_DONE) {
      runOnUiThread(new Runnable() {
        public void run() {
          findViewById(R.id.practice_complete_panel).setVisibility(View.VISIBLE);
          findViewById(R.id.button_practice_continue).setVisibility(View.VISIBLE);
          double score = Math.ceil(av.getScore());
          double perfect = av.getBeats()*10;
          String result = (int)score+" / "+(int)perfect;
          ((TextView)findViewById(R.id.finalscore)).setText(result);
          TextView congrats = (TextView)findViewById(R.id.contgrats);
          if (score / perfect >= 0.9) {
            congrats.setText("Excellent!");
            success();
          }
          else if (score / perfect >= 0.7) {
            congrats.setText("Very Good!");
            success();
          }
          else {
            congrats.setText("Poor.");
            failure();
          }
        }
      });
    }
  }

  //  These are hooks so the tutorial can get the result
  //  Since the tutorial should not show the Definitions button
  //  we will turn it on in these routines
  protected void success()
  {
    buttonDefinition.setVisibility(View.VISIBLE);
  }
  protected void failure()
  {
    buttonDefinition.setVisibility(View.VISIBLE);
  }

  public void clickRepeat(View v)
  {
    hideExtraStuff();
    av.doStart();
  }


  public void clickContinue(View v)
  {
    hideExtraStuff();
    nextAnimation();
    av.doStart();
  }

  public void clickReturn(View v)
  {
    hideExtraStuff();
    finish();
  }

  public void clickDefinition(View v)
  {
    if (fragmentDefinition.getVisibility() == View.VISIBLE) {
      fragmentDefinition.setVisibility(View.GONE);
      findViewById(R.id.practice_complete_panel).setVisibility(View.VISIBLE);
    } else {
      DefinitionFragment df = new DefinitionFragment();
      df.setArguments(getIntent().getExtras());  // pass the link
      replaceFragment(df, R.id.fragment_definition);
      hideExtraStuff();
      fragmentDefinition.setVisibility(View.VISIBLE);
      buttonDefinition.setVisibility(View.VISIBLE);
      buttonDefinition.setSelected(true);
    }
  }

  /**
   * Invoked when the Activity loses user focus.
   */
  @Override
  protected void onPause() {
    super.onPause();
    av.doPause(); // pause animation when Activity pauses
  }


  @Override
  public void onBackPressed()
  {
    if (fragmentDefinition.getVisibility() == View.VISIBLE)
      clickDefinition(null);
    else
      super.onBackPressed();
  }

}
