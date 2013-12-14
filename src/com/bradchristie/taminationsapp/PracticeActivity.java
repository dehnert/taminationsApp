package com.bradchristie.taminationsapp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class PracticeActivity extends RotationActivity
                              implements AnimationListener
{
  protected AnimationView av;
  private Document calldoc;

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
    setTitle("Practice");
    calldoc = Tamination.getXMLAsset(this,"src/calls.xml");
    av = (AnimationView)findViewById(R.id.animationview);
    av.setAnimationListener(this);
    nextAnimation();
  }

  protected void nextAnimation()
  {
    //  Choose a random call from the selected level
    SharedPreferences prefs = getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    String gender = prefs.getString("gender", "boy");
    String selector = prefs.getString("selector","level='Basic and Mainstream' and @sublevel!='Styling'");
    NodeList calls = Tamination.evalXPath("/calls/call[@"+selector+"]",calldoc);
    for (boolean found=false; !found;) {
      int callnum = (int)(Math.random()*calls.getLength());
      Element e = (Element)calls.item(callnum);
      String xmlname = e.getAttribute("link").replace(".html", ".xml");
      Document tamdoc = Tamination.getXMLAsset(this, xmlname);
      NodeList tamlist = tamdoc.getElementsByTagName("tam");
      if (tamlist.getLength() > 0) {
        int animnum = (int)(Math.random()*tamlist.getLength());
        Element tam = (Element)tamdoc.getElementsByTagName("tam").item(animnum);
        //  For now, skip any "difficult" animations
        if (tam.getAttribute("difficulty").equals("3"))
          continue;
        setTitle(tam.getAttribute("title"));
        av.setAnimation(tam,gender.equals("boy")?Dancer.BOY:Dancer.GIRL);
        found = true;
      }
    }
  }

  @Override
  public void onAnimationChanged(int action, double x, double y, double z)
  {
    if (action == AnimationListener.ANIMATION_READY) {
      Log.i("PracticeActivity", "starting");
      av.getThread().doStart();
    }
    if (action == AnimationListener.ANIMATION_PROGRESS) {
      final int iscore = (int)Math.ceil(av.getThread().getScore());
      runOnUiThread(new Runnable() {
        public void run() {
          TextView scoreview = (TextView)findViewById(R.id.text_score);
          scoreview.setText("Score: "+iscore);
        }});
    }
    if (action == AnimationListener.ANIMATION_DONE) {
      runOnUiThread(new Runnable() {
        public void run() {
          findViewById(R.id.text_instructions).setVisibility(View.GONE);
          findViewById(R.id.practice_complete_panel).setVisibility(View.VISIBLE);
          findViewById(R.id.button_practice_continue).setVisibility(View.VISIBLE);
          double score = Math.ceil(av.getThread().getScore());
          double perfect = av.getThread().getBeats()*10;
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
  protected void success()
  {
  }
  protected void failure()
  {
  }

  public void clickRepeat(View v)
  {
    findViewById(R.id.practice_complete_panel).setVisibility(View.GONE);
    av.getThread().doStart();
  }


  public void clickContinue(View v)
  {
    findViewById(R.id.practice_complete_panel).setVisibility(View.GONE);
    nextAnimation();
    av.getThread().doStart();
  }

  public void clickReturn(View v)
  {
    findViewById(R.id.practice_complete_panel).setVisibility(View.GONE);
    finish();
  }
}
