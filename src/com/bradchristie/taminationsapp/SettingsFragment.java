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

//Use the backward-compatibility library to support Android 2
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

public class SettingsFragment extends RotationFragment
{
  private AnimationSettingsListener listener;

  public void setListener(AnimationSettingsListener newlistener)
  {
    listener = newlistener;
  }
  private void broadcast(int setting)
  {
    if (listener != null)
      listener.settingsChanged(setting);
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    //  Inflate the layout for this fragment
    final View fragment = inflater.inflate(R.layout.fragment_settings, container, false);

    //  Apply current settings
    final SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Context.MODE_PRIVATE);

    String speed = prefs.getString("speed", "Normal");
    RadioButton speedSlowRadioButton =
        (RadioButton)fragment.findViewById(R.id.speedSlowRadioButton);
    speedSlowRadioButton.setChecked(speed.equals("Slow"));
    RadioButton speedNormalRadioButton =
        (RadioButton)fragment.findViewById(R.id.speedNormalRadioButton);
    speedNormalRadioButton.setChecked(speed.equals("Normal"));
    RadioButton speedFastRadioButton =
        (RadioButton)fragment.findViewById(R.id.speedFastRadioButton);
    speedFastRadioButton.setChecked(speed.equals("Fast"));
    if (speed.equals("Slow"))
      ((TextView)fragment.findViewById(R.id.speedText))
          .setText("Dancers move at a Slow pace");
    else if (speed.equals("Fast"))
      ((TextView)fragment.findViewById(R.id.speedText))
          .setText("Dancers move at a Fast pace");
    else
      ((TextView)fragment.findViewById(R.id.speedText))
          .setText("Dancers move at a Normal pace");

    Boolean loop = prefs.getBoolean("loop", false);
    CheckBox loopCheckBox =
        (CheckBox)fragment.findViewById(R.id.loopCheckBox);
    loopCheckBox.setChecked(loop);

    Boolean grid = prefs.getBoolean("grid", false);
    CheckBox gridCheckBox =
        (CheckBox)fragment.findViewById(R.id.gridCheckBox);
    gridCheckBox.setChecked(grid);

    Boolean paths = prefs.getBoolean("paths", false);
    CheckBox pathsCheckBox =
        (CheckBox)fragment.findViewById(R.id.pathsCheckBox);
    pathsCheckBox.setChecked(paths);

    String numbers = prefs.getString("numbers2", "Off");
    RadioButton numbersOffRadioButton =
        (RadioButton)fragment.findViewById(R.id.numbersOffRadioButton);
    numbersOffRadioButton.setChecked(
        !numbers.contains("1-8") && !numbers.contains("1-4"));
    RadioButton numbersDancersRadioButton =
        (RadioButton)fragment.findViewById(R.id.numbersDancersRadioButton);
    numbersDancersRadioButton.setChecked(numbers.contains("1-8"));
    RadioButton numbersCouplesRadioButton =
        (RadioButton)fragment.findViewById(R.id.numbersCouplesRadioButton);
    numbersCouplesRadioButton.setChecked(numbers.contains("1-4"));
    if (numbers.contains("1-4"))
      ((TextView)fragment.findViewById(R.id.numbersText))
          .setText("Number couples 1-4");
    else if (numbers.contains("1-8"))
      ((TextView)fragment.findViewById(R.id.numbersText))
          .setText("Number dancers 1-8");
    else
      ((TextView)fragment.findViewById(R.id.numbersText))
          .setText("Dancers not numbered");

    Boolean phantoms = prefs.getBoolean("phantoms", false);
    CheckBox phantomsCheckBox =
        (CheckBox)fragment.findViewById(R.id.phantomsCheckBox);
    phantomsCheckBox.setChecked(phantoms);

    String geometry = prefs.getString("geometry", "None");
    RadioButton geometryNoneRadioButton =
        (RadioButton)fragment.findViewById(R.id.geometryNoneRadioButton);
    geometryNoneRadioButton.setChecked(geometry.equals("None"));
    RadioButton geometryHexagonRadioButton =
        (RadioButton)fragment.findViewById(R.id.geometryHexagonRadioButton);
    geometryHexagonRadioButton.setChecked(geometry.equals("Hexagon"));
    RadioButton geometryBigonRadioButton =
        (RadioButton)fragment.findViewById(R.id.geometryBigonRadioButton);
    geometryBigonRadioButton.setChecked(geometry.equals("Bi-gon"));

    //  Add listeners
    speedSlowRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("speed","Slow").commit();
              broadcast(AnimationSettingsListener.SPEED_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.speedText))
                  .setText("Dancers move at a Slow pace");
            }
          }
        });
    speedNormalRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("speed","Normal").commit();
              broadcast(AnimationSettingsListener.SPEED_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.speedText))
                  .setText("Dancers move at a Normal pace");
            }
          }
        });
    speedFastRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("speed","Fast").commit();
              broadcast(AnimationSettingsListener.SPEED_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.speedText))
                  .setText("Dancers move at a Fast pace");
            }
          }
        });

    loopCheckBox.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            prefs.edit().putBoolean("loop", isChecked).commit();
            broadcast(AnimationSettingsListener.LOOP_SETTING_CHANGED);
          }
        });

    gridCheckBox.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            prefs.edit().putBoolean("grid", isChecked).commit();
            broadcast(AnimationSettingsListener.GRID_SETTING_CHANGED);
          }
        });

    pathsCheckBox.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            prefs.edit().putBoolean("paths", isChecked).commit();
            broadcast(AnimationSettingsListener.PATHS_SETTING_CHANGED);
          }
        });

    numbersOffRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("numbers2","off").commit();
              broadcast(AnimationSettingsListener.NUMBERS_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.numbersText))
                  .setText("Dancers not numbered");
            }
          }
        });
    numbersDancersRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("numbers2","1-8").commit();
              broadcast(AnimationSettingsListener.NUMBERS_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.numbersText))
                  .setText("Number dancers 1-8");
            }
          }
        });
    numbersCouplesRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("numbers2","1-4").commit();
              broadcast(AnimationSettingsListener.NUMBERS_SETTING_CHANGED);
              ((TextView)fragment.findViewById(R.id.numbersText))
                  .setText("Number couples 1-4");
            }
          }
        });

    phantomsCheckBox.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            prefs.edit().putBoolean("phantoms", isChecked).commit();
            broadcast(AnimationSettingsListener.PHANTOMS_SETTING_CHANGED);
          }
        });

    geometryNoneRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("geometry","None").commit();
              broadcast(AnimationSettingsListener.GEOMETRY_SETTING_CHANGED);
            }
          }
        });
    geometryHexagonRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("geometry","Hexagon").commit();
              broadcast(AnimationSettingsListener.GEOMETRY_SETTING_CHANGED);
            }
          }
        });
    geometryBigonRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView,
              boolean isChecked) {
            if (isChecked) {
              prefs.edit().putString("geometry","Bi-gon").commit();
              broadcast(AnimationSettingsListener.GEOMETRY_SETTING_CHANGED);
            }
          }
        });

    return fragment;
  }

}
