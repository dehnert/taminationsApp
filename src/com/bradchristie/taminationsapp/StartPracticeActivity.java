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
      View fragment = inflater.inflate(R.layout.fragment_practice_level, container, false);
      return fragment;
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
    String speed = prefs.getString("practicespeed", "Slow");
    setRadioButton(R.id.speedSlow,speed.equals("Slow"));
    setRadioButton(R.id.speedModerate,speed.equals("Moderate"));
    setRadioButton(R.id.speedNormal,speed.equals("Normal"));
    replaceFragment(new PracticeLevelFragment(),R.id.fragment_startpractice);
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
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("level",d.name).putString("selector",d.selector).commit();
    startActivity(new Intent(this,PracticeActivity.class));
  }

}
