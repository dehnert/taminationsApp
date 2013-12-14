package com.bradchristie.taminationsapp;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

public class StartPracticeActivity extends RotationActivity
{

  //  Extend SettingsFragment so we can hide the settings that
  //  don't apply to Practice
  private static class PracticeSettingsFragment extends SettingsFragment
  {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
      final View fragment = super.onCreateView(inflater, container, savedInstanceState);
      fragment.findViewById(R.id.loopPanel).setVisibility(View.GONE);
      fragment.findViewById(R.id.gridPanel).setVisibility(View.GONE);
      fragment.findViewById(R.id.pathsPanel).setVisibility(View.GONE);
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

    //  Set up the gender radio buttons
    final SharedPreferences prefs = getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    String gender = prefs.getString("gender", "boy");
    RadioButton genderBoyRadioButton =
        (RadioButton)findViewById(R.id.genderBoy);
    genderBoyRadioButton.setChecked(gender.equals("boy"));
    RadioButton genderGirlRadioButton =
        (RadioButton)findViewById(R.id.genderGirl);
    genderGirlRadioButton.setChecked(gender.equals("girl"));
    genderBoyRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("gender","boy").commit();
            }
          }
        });
    genderGirlRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("gender","girl").commit();
            }
          }
        });

    //  Set up the input method radio buttons
    /*
    String inputmethod = prefs.getString("inputmethod", "touch");
    RadioButton inputTouchRadioButton =
        (RadioButton)findViewById(R.id.inputTouch);
    inputTouchRadioButton.setChecked(inputmethod.equals("touch"));
    RadioButton inputWalkRadioButton =
        (RadioButton)findViewById(R.id.inputWalk);
    inputWalkRadioButton.setChecked(inputmethod.equals("walk"));
    inputTouchRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("inputmethod","touch").commit();
            }
          }
        });
    inputWalkRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("inputmethod","walk").commit();
            }
          }
        });
        */
    if (prefs.getBoolean("tutorialcompleted", false))
      showLevelsFragment(null);
    else
      showSettingsFragment(null);
  }

  public void startTutorial(View v)
  {
    startActivity(new Intent(this,TutorialActivity.class));
  }

  public void showLevelsFragment(View v)
  {
    if (findViewById(R.id.fragment_startpractice) != null)
      replaceFragment(new LevelFragment(LevelFragment.SHOWLEVELSONLY),
                      R.id.fragment_startpractice);
    else
      startActivity(new Intent(this,LevelActivity.class));
  }

  public void showSettingsFragment(View v)
  {
    if (findViewById(R.id.fragment_startpractice) != null)
      replaceFragment(new PracticeSettingsFragment(),R.id.fragment_startpractice);
    else
      startActivity(new Intent(this,SettingsActivity.class));
  }

  //  This is called when the user clicks on one of the levels
  //  Go start practice at that level
  public void processClick(View v)
  {
    LevelData d = LevelData.find((String) ((TextView)v).getText());
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("level",d.name).putString("selector",d.selector).commit();
    startActivity(new Intent(this,PracticeActivity.class));
  }

}
