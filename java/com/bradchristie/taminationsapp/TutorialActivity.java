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

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

public class TutorialActivity extends PracticeActivity
{

  public interface StartTutorialInstruction
  {
    public void start();
  }

  private static final String tutdata[] = {
        "Use your %primary% Finger on the %primary% side of the screen."
        + "  Do not put your finger on the dancer."
        + "  Slide your finger forward to move the dancer forward."
        + "  Try to keep pace with the adjacent dancer.",

        "Use your %primary% Finger to follow the turning path."
        + "  Try to keep pace with the adjacent dancer.",

        "Normally your dancer faces the direction you are moving.  "
        + "  But you can use your %secondary% Finger to hold or change the facing direction."
        + "  Press and hold your %secondary% finger on the %secondary% side"
        + " of the screen.  This will keep your dancer facing forward."
        + "  Then use your %primary% finger on the %primary% side"
        + " of the screen to slide your dancer horizontally.",

        "Use your %secondary% finger to turn in place."
        + "  To U-Turn Left, make a 'C' movement with your %secondary% finger."
  };

  private static final String tutcompletemsg =
      "Congratulations!  You have successfully completed the tutorial."
      + "  Now select the level you would like to practice.";

  private int tutnum = 0;
  private SharedPreferences prefs;

  private void showInstructions()
  {
    boolean primaryIsLeft = prefs.getString("primarycontrol", "Right").equals("Left");
    String instructions = tutdata[tutnum];
    instructions = instructions.replace("%primary%",primaryIsLeft?"Left":"Right")
                               .replace("%secondary%",primaryIsLeft?"Right":"Left");
    String title = "Tutorial "+(tutnum+1)+" of "+tutdata.length;
    TutorialInstructionFragment tif =  new TutorialInstructionFragment();
    tif.setMessage(instructions);
    tif.setTitle(title);
    tif.setStarter(new StartTutorialInstruction() {
                     @Override
                     public void start() {
                       av.setInteractiveDancerPathVisibility(true);
                       av.doStart();
                     }
                   });
        tif.show(getSupportFragmentManager(), null);
  }

  @Override
  protected void nextAnimation()
  {
    if (tutnum >= tutdata.length)
      tutnum = 0;
    Document tamdoc = Tamination.getXMLAsset(this,"src/tutorial.xml");
    prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    int gender = prefs.getString("gender", "Boy").equals("Boy") ? Dancer.BOY : Dancer.GIRL;
    int offset = gender == Dancer.BOY ? 0 : 1;
    NodeList tamlist = Tamination.evalXPath("/tamination/tam", tamdoc);
    Element tam = (Element)tamlist.item(tutnum*2 + offset);
    setTitle(tam.getAttribute("title"));
    av.setAnimation(tam,gender);
    av.setInteractiveDancerPathVisibility(true);
    showInstructions();
  }

  //  This overrides the parent method which starts the animation
  //  Do not start the animation until the instructions are dismissed
  @Override
  protected void animationReady()
  {
  }

  @Override
  public void clickRepeat(View v)
  {
    hideExtraStuff();
    showInstructions();
  }

  @Override
  public void clickContinue(View v)
  {
    tutnum++;
    super.clickContinue(v);
  }

  @Override
  protected void success()
  {
    if (tutnum+1 >= tutdata.length) {
      TextView congrats = (TextView)findViewById(R.id.contgrats);
      congrats.setText("Tutorial Complete");
      prefs.edit().putBoolean("tutorialcomplete", true).commit();
      findViewById(R.id.button_practice_continue).setVisibility(View.GONE);
      TutorialInstructionFragment cf = new TutorialInstructionFragment();
      cf.setTitle("Tutorial Complete");
      cf.setMessage(tutcompletemsg);
      cf.show(getSupportFragmentManager(), null);
      cf.setStarter(new StartTutorialInstruction() {
        @Override
        public void start() {
          finish();
        }
      });
      tutnum = 0;
    }
  }

  @Override
  protected void failure()
  {
    findViewById(R.id.button_practice_continue).setVisibility(View.GONE);
  }

}
