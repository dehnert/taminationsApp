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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity
                              implements OnSharedPreferenceChangeListener
{

  private ListPreference speedPreference;
  private ListPreference numbersPreference;
  private ListPreference geometryPreference;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
    speedPreference = (ListPreference)getPreferenceScreen().findPreference("speed");
    numbersPreference = (ListPreference)getPreferenceScreen().findPreference("numbers2");
    geometryPreference = (ListPreference)getPreferenceScreen().findPreference("geometry");
  }

  public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
  {
    // Set speed summary to show current value
    if (key.equals("speed")) {
      speedPreference.setSummary("Dancers move at a "+
                                 prefs.getString("speed", "Normal")+" pace");
    }
    else if (key.equals("numbers2"))
      numbersPreference.setSummary(prefs.getString("numbers2","Off"));
    else if (key.equals("geometry")) {
      String value = prefs.getString("geometry", "None");
      if (value.equals("None"))
        geometryPreference.setSummary("Special geometries are Hexagon and Bi-Gon");
      else
        geometryPreference.setSummary(value);
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    // Set the initial summaries
    SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
    onSharedPreferenceChanged(prefs,"speed");
    onSharedPreferenceChanged(prefs,"numbers2");
    onSharedPreferenceChanged(prefs,"geometry");
    // Set up this listener whenever a key changes
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    // Unregister the listener
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }
}
