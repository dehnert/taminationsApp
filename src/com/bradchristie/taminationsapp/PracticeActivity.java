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
    buttonDefinition = findViewById(R.id.button_practice_definition);
    fragmentDefinition = findViewById(R.id.fragment_definition);
    setTitle("Practice");
    hideExtraStuff();
    calldoc = Tamination.getXMLAsset(this,"src/calls.xml");
    av = (AnimationView)findViewById(R.id.animationview);
    av.setAnimationListener(this);
    nextAnimation();
  }

  private void hideExtraStuff()
  {
    findViewById(R.id.practice_complete_panel).setVisibility(View.GONE);
    buttonDefinition.setVisibility(View.GONE);
    findViewById(R.id.fragment_definition).setVisibility(View.GONE);
  }

  protected void nextAnimation()
  {
    prefs = getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    String gender = prefs.getString("gender", "boy");
    String selector = prefs.getString("selector","level='Basic and Mainstream' and @sublevel!='Styling'");
    NodeList calls = Tamination.evalXPath("/calls/call[@"+selector+"]",calldoc);
    for (boolean found=false; !found;) {
      int callnum = (int)(Math.random()*calls.getLength());
      Element e = (Element)calls.item(callnum);
      link = e.getAttribute("link");
      String xmlname = link.replace(".html", ".xml");
      Document tamdoc = Tamination.getXMLAsset(this, xmlname);
      NodeList tamlist = tamdoc.getElementsByTagName("tam");
      if (tamlist.getLength() > 0) {
        int animnum = (int)(Math.random()*tamlist.getLength());
        Element tam = (Element)tamdoc.getElementsByTagName("tam").item(animnum);
        //  For now, skip any "difficult" animations
        if (tam.getAttribute("difficulty").equals("3"))
          continue;
        setTitle(tam.getAttribute("title"));
        av.setSquare();
        av.setAnimation(tam,gender.equals("boy")?Dancer.BOY:Dancer.GIRL);
        //  Save link for definition
        prefs.edit().putString("link",link).commit();
        found = true;
      }
    }
  }

  @Override
  public void onAnimationChanged(int action, double x, double y, double z)
  {
    if (action == AnimationListener.ANIMATION_READY) {
      //  Force some settings required for practice
      av.setSpeed(prefs.getString("practicespeed","Slow"));
      av.setLoop(false);
      av.setGridVisibility(true);
      av.setPathVisibility(false);
      av.setPhantomVisibility(false);
      av.setNumbers(Dancer.NUMBERS_OFF);
      av.doStart();
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
          findViewById(R.id.text_instructions).setVisibility(View.GONE);
          findViewById(R.id.practice_complete_panel).setVisibility(View.VISIBLE);
          findViewById(R.id.button_practice_continue).setVisibility(View.VISIBLE);
          buttonDefinition.setVisibility(View.VISIBLE);
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
  protected void success()
  {
  }
  protected void failure()
  {
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
      DefinitionFragment df =
          (DefinitionFragment)
          getSupportFragmentManager().findFragmentById(R.id.fragment_definition);
      df.setDefinition(link);
      hideExtraStuff();
      fragmentDefinition.setVisibility(View.VISIBLE);
      buttonDefinition.setVisibility(View.VISIBLE);
      buttonDefinition.setSelected(true);
    }
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
