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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class LevelActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_level);
  }

  /**
   *  This handles clicks on the buttons for different levels.
   *  Display the screen listing calls for that level.
   */

  private void processClick(String level, String selector)
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("level", level)
                .putString("selector", selector).apply();
    startActivity(new Intent(this,CallActivity.class));
  }

  public void onBasicAndMainstreamClick(View v)
  {
    processClick("Basic and Mainstream","level='Basic and Mainstream'");
  }
  public void onBasic1Click(View v)
  {
    processClick("Basic 1","sublevel='Basic 1'");
  }
  public void onBasic2Click(View v)
  {
    processClick("Basic 2","sublevel='Basic 2'");
  }
  public void onMainstreamClick(View v)
  {
    processClick("Mainstream","sublevel='Mainstream'");
  }
  public void onPlusClick(View v)
  {
    processClick("Plus","level='Plus'");
  }
  public void onAdvancedClick(View v)
  {
    processClick("Advanced","level='Advanced'");
  }
  public void onA1Click(View v)
  {
    processClick("A-1","sublevel='A-1'");
  }
  public void onA2Click(View v)
  {
    processClick("A-2","sublevel='A-2'");
  }
  public void onChallengeClick(View v)
  {
    processClick("Challenge","level='Challenge'");
  }
  public void onC1Click(View v)
  {
    processClick("C-1","sublevel='C-1'");
  }
  public void onC2Click(View v)
  {
    processClick("C-2","sublevel='C-2'");
  }
  public void onC3AClick(View v)
  {
    processClick("C-3A","sublevel='C-3A'");
  }
  public void onIndexClick(View v)
  {
    processClick("Index of All Calls","level");
  }


}
