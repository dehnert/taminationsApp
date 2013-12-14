package com.bradchristie.taminationsapp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

public class TutorialActivity extends PracticeActivity
{

  private static class TutorialData
  {
    public String xmlfile;
    public String title;
    public String animforBoy;
    public String animforGirl;
    public String instructions;
    public TutorialData(String xmlfile, String title,
                        String animforBoy, String animforGirl,
                        String instructions)
    {
      this.xmlfile = xmlfile;
      this.title = title;
      this.animforBoy = animforBoy;
      this.animforGirl = animforGirl;
      this.instructions = instructions;
    }
  }
  private static final TutorialData tutdata[] = {
    new TutorialData("ms/walk_and_dodge.xml",
                     "Walk and Dodge",
                     "Right-Hand Box",
                     "Left-Hand Box",
                     "Use Left Thumb or Finger\nto Move Forward"),
    new TutorialData("ms/walk_and_dodge.xml",
                     "Walk and Dodge",
                     "Left-Hand Box",
                     "Right-Hand Box",
                     "Use Left Thumb or Finger\nto Slide Sideways"),
    new TutorialData("b1/turn_back.xml",
                     "U-Turn Back",
                     "Facing Couples",
                     "Couples Facing Out",
                     "Slide Right Thumb Left\nto Turn Left"),
    new TutorialData("b1/circulate.xml",
                     "Box Circulate",
                     "Right-Hand Box",
                     "Left-Hand Box",
                     "Move Forward with Left Thumb\nwhile Turning with Right Thumb")
  };

  @Override
  protected void nextAnimation()
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    int tutnum = prefs.getInt("tutorial",0);
    if (tutnum >= tutdata.length)
      tutnum = 0;
    TutorialData td = tutdata[tutnum];
    setTitle(td.title);
    Document tamdoc = Tamination.getXMLAsset(this,td.xmlfile);
    int gender = prefs.getString("gender", "boy").equals("boy") ? Dancer.BOY : Dancer.GIRL;
    String from = gender == Dancer.BOY ? td.animforBoy : td.animforGirl;
    String selector = "[@title='"+td.title+"' and @from='"+from+"']";
    NodeList tamlist = Tamination.evalXPath("/tamination/tam"+selector, tamdoc);
    Element tam = (Element)tamlist.item(0);
    TextView instr = (TextView)findViewById(R.id.text_instructions);
    instr.setText(td.instructions);
    instr.setVisibility(View.VISIBLE);
    av.setAnimation(tam,gender);
  }

  @Override
  protected void success()
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    int tutnum = prefs.getInt("tutorial",0)+1;
    if (tutnum >= tutdata.length) {
      TextView congrats = (TextView)findViewById(R.id.contgrats);
      congrats.setText("Tutorial Complete");
      findViewById(R.id.button_practice_continue).setVisibility(View.GONE);
      tutnum = 0;
    }
    prefs.edit().putInt("tutorial", tutnum).commit();
  }

  @Override
  protected void failure()
  {
    findViewById(R.id.button_practice_continue).setVisibility(View.GONE);
  }

}
