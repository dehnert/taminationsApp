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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

public class StartPracticeActivity extends RotationActivity
{

  private SharedPreferences prefs;

  public static class PracticeLevelFragment extends RotationFragment
  {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
      return inflater.inflate(R.layout.fragment_practice_level, container, false);
    }

  }

  @Override
  protected boolean isLandscapeActivity()
  {
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_startpractice);
    setTitle("Practice");

    prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    String gender = prefs.getString("gender", "Boy");
    setRadioButton(R.id.genderBoy,gender.equals("Boy"));
    setRadioButton(R.id.genderGirl,gender.equals("Girl"));
    String primary = prefs.getString("primarycontrol","Right");
    setRadioButton(R.id.primaryRight,primary.equals("Right"));
    setRadioButton(R.id.primaryLeft,primary.equals("Left"));
    String speed = prefs.getString("practicespeed", "Slow");
    setRadioButton(R.id.speedSlow,speed.equals("Slow"));
    setRadioButton(R.id.speedModerate,speed.equals("Moderate"));
    setRadioButton(R.id.speedNormal,speed.equals("Normal"));
    replaceFragment(new PracticeLevelFragment(),R.id.fragment_startpractice);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    boolean tutdone = prefs.getBoolean("tutorialcomplete", false);
    findViewById(R.id.button_basic_1).setEnabled(tutdone);
    findViewById(R.id.button_basic_2).setEnabled(tutdone);
    findViewById(R.id.button_mainstream).setEnabled(tutdone);
    findViewById(R.id.button_plus).setEnabled(tutdone);
    findViewById(R.id.button_a_1).setEnabled(tutdone);
    findViewById(R.id.button_a_2).setEnabled(tutdone);
    findViewById(R.id.button_c1).setEnabled(tutdone);
    findViewById(R.id.button_c2).setEnabled(tutdone);
    findViewById(R.id.button_c3a).setEnabled(tutdone);
    findViewById(R.id.button_c3b).setEnabled(tutdone);
  }

  private void setRadioButton(int id, boolean isChecked)
  {
    RadioButton rb = (RadioButton)findViewById(id);
    rb.setChecked(isChecked);
  }

  public void clickGender(View v)
  {
    String gender = ((RadioButton)v).getText().toString();
    prefs.edit().putString("gender",gender).commit();
  }

  public void clickPrimaryControl(View v)
  {
    String primary = ((RadioButton)v).getText().toString().split(" ")[0];
    prefs.edit().putString("primarycontrol",primary).commit();
  }

  public void clickSpeed(View v)
  {
    String speed = ((RadioButton)v).getText().toString();
    prefs.edit().putString("practicespeed",speed).commit();
  }

  /**
   *   This function is bound to the Tutorial button.
   *   It starts the Tutorial activity.
   * @param v  ignored
   */
  public void startTutorial(View v)
  {
    startActivity(new Intent(this,TutorialActivity.class));
  }

  /**
   *  This is called when the user clicks on one of the levels
   *  Go start practice at that level
   *
   * @param v  View the user clicked on, used to get level
   */
  public void processClick(View v)
  {
    LevelData d = LevelData.find((String) ((TextView)v).getText());
    prefs.edit().putString("level",d.name).putString("selector",d.selector).commit();
    startActivity(new Intent(this,PracticeActivity.class));
  }

}
