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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class LevelActivity extends RotationActivity
                           implements CallClickListener
{

  View selectedView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_level);
    setTitle("Taminations");
    if (findViewById(R.id.fragment_calllist) != null) {
      //  Multi-fragment display - switch calllist fragment
      RotationFragment af = new AboutFragment();
      SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
      if (!prefs.getString("level", "").equals(""))
        af = new CalllistFragment();
      replaceFragment(af,R.id.fragment_calllist);
    }
  }

  protected void onResume()
  {
    super.onResume();
    if (isPortrait()) {
      setTitle("Taminations");
      if (selectedView != null) {
        selectedView.setSelected(false);
        selectedView = null;
      }
    }
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().remove("navigateupto").commit();
  }

  /**
   *  This handles clicks on the buttons for different levels.
   *  Display the screen listing calls for that level.
   */
  private void highlightClick(View v)
  {
    if (selectedView != null)
      selectedView.setSelected(false);
    v.setSelected(true);
    selectedView = v;
  }
  private void processClick(View v, String level, String selector)
  {
    highlightClick(v);
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("level", level)
                .putString("selector", selector).commit();
    if (isPortrait()) {
      //  Single-fragment display - start calllist activity
      startActivity(new Intent(this,CalllistActivity.class));
    } else {
      //  Multi-fragment display - switch calllist fragment
      CalllistFragment cf = new CalllistFragment();
      replaceFragment(cf,R.id.fragment_calllist);
    }
  }

  //  Process a click on one of the calls
  public void onCallClick(String call, String link)
  {
    //  Save the call info
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("call",call)
                .putString("link",link)
                .putInt("anim", 0).commit();
    //  Start the next activity
    startActivity(new Intent(this,AnimListActivity.class));
  }

  public void onBasicAndMainstreamClick(View v)
  {
    processClick(v,"Basic and Mainstream","level='Basic and Mainstream' and @sublevel!='Styling'");
  }
  public void onBasic1Click(View v)
  {
    processClick(v,"Basic 1","sublevel='Basic 1'");
  }
  public void onBasic2Click(View v)
  {
    processClick(v,"Basic 2","sublevel='Basic 2'");
  }
  public void onMainstreamClick(View v)
  {
    processClick(v,"Mainstream","sublevel='Mainstream'");
  }
  public void onPlusClick(View v)
  {
    processClick(v,"Plus","level='Plus'");
  }
  public void onAdvancedClick(View v)
  {
    processClick(v,"Advanced","level='Advanced'");
  }
  public void onA1Click(View v)
  {
    processClick(v,"A-1","sublevel='A-1'");
  }
  public void onA2Click(View v)
  {
    processClick(v,"A-2","sublevel='A-2'");
  }
  public void onChallengeClick(View v)
  {
    processClick(v,"Challenge","level='Challenge'");
  }
  public void onC1Click(View v)
  {
    processClick(v,"C-1","sublevel='C-1'");
  }
  public void onC2Click(View v)
  {
    processClick(v,"C-2","sublevel='C-2'");
  }
  public void onC3AClick(View v)
  {
    processClick(v,"C-3A","sublevel='C-3A'");
  }
  public void onIndexClick(View v)
  {
    processClick(v,"Index of All Calls","level!='Info' and @sublevel!='Styling'");
  }
  public void onAboutClick(View v)
  {
    highlightClick(v);
    if (findViewById(R.id.fragment_calllist) != null) {
      //  Multi-fragment display - switch calllist fragment
      AboutFragment af = new AboutFragment();
      replaceFragment(af,R.id.fragment_calllist);
    } else
      //  Single-fragment display - start calllist activity
      startActivity(new Intent(this,AboutActivity.class));

  }

}
