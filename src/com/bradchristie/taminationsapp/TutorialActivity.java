/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2014 Brad Christie

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
    public String instructionsLeft;
    public String instructionsRight;
    public TutorialData(String xmlfile, String title,
                        String animforBoy, String animforGirl,
                        String instructionsLeft,
                        String instructionsRight)
    {
      this.xmlfile = xmlfile;
      this.title = title;
      this.animforBoy = animforBoy;
      this.animforGirl = animforGirl;
      this.instructionsLeft = instructionsLeft;
      this.instructionsRight = instructionsRight;
    }
  }
  private static final TutorialData tutdata[] = {
    new TutorialData("ms/walk_and_dodge.xml",
                     "Walk and Dodge",
                     "Right-Hand Box",
                     "Left-Hand Box",
        "Use Left Finger\nto Move Forward",
        "Use Right Finger\nto Move Forward"),
    new TutorialData("b1/circulate.xml",
                     "Box Circulate",
                     "Left-Hand Box",
                     "Right-Hand Box",
        "Follow Path with Left Finger",
        "Follow Path with Right Finger"),
    new TutorialData("ms/walk_and_dodge.xml",
                     "Walk and Dodge",
                     "Left-Hand Box",
                     "Right-Hand Box",
        "Hold Down Right Finger\nSlide with Left Finger",
        "Hold Down Left Finger\nSlide with Right Finger"),
    new TutorialData("b1/turn_back.xml",
                     "U-Turn Back",
                     "Facing Couples",
                     "Sashayed Couples",
        "Rotate Right Finger Right\nto Turn Right",
        "Rotate Left Finger Right\nto Turn Right"),
  };
  private int tutnum = 0;
  private SharedPreferences prefs;

  @Override
  protected void nextAnimation()
  {
    if (tutnum >= tutdata.length)
      tutnum = 0;
    TutorialData td = tutdata[tutnum];
    setTitle(td.title);
    Document tamdoc = Tamination.getXMLAsset(this,td.xmlfile);
    prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    int gender = prefs.getString("gender", "Boy").equals("Boy") ? Dancer.BOY : Dancer.GIRL;
    boolean primaryIsLeft = prefs.getString("primarycontroller", "Right").equals("Left");
    String from = gender == Dancer.BOY ? td.animforBoy : td.animforGirl;
    String selector = "[@title='"+td.title+"' and @from='"+from+"']";
    NodeList tamlist = Tamination.evalXPath("/tamination/tam"+selector, tamdoc);
    Element tam = (Element)tamlist.item(0);
    TextView instr = (TextView)findViewById(R.id.text_instructions);
    instr.setText(primaryIsLeft ? td.instructionsLeft : td.instructionsRight);
    instr.setVisibility(View.VISIBLE);
    av.setAnimation(tam,gender);
  }

  @Override
  public void clickRepeat(View v)
  {
    findViewById(R.id.text_instructions).setVisibility(View.VISIBLE);
    super.clickRepeat(v);
  }

  @Override
  protected void success()
  {
    if (++tutnum >= tutdata.length) {
      TextView congrats = (TextView)findViewById(R.id.contgrats);
      congrats.setText("Tutorial Complete");
      prefs.edit().putBoolean("tutorialcomplete", true).commit();
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
