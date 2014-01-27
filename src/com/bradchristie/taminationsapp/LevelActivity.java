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
import android.widget.TextView;

public class LevelActivity extends RotationActivity
                           implements CallClickListener
{

  public static class LevelData
  {
    public String name;
    public String dir;
    public String selector;
    public int id;
    public LevelData(String n, String d, String s, int i)
    {
      name = n;
      dir = d;
      selector = s;
      id = i;
    }
    private static final LevelData data[] = {
      new LevelData("Basic and Mainstream","","level='Basic and Mainstream' and @sublevel!='Styling'",R.id.button_basic_and_mainstream),
      new LevelData("Basic 1","b1","sublevel='Basic 1'",R.id.button_basic_1),
      new LevelData("Basic 2","b2","sublevel='Basic 2'",R.id.button_basic_2),
      new LevelData("Mainstream","ms","sublevel='Mainstream'",R.id.button_mainstream),
      new LevelData("Plus","plus","level='Plus'",R.id.button_plus),
      new LevelData("Advanced","","level='Advanced'",R.id.button_advanced),
      new LevelData("A-1","a1","sublevel='A-1'",R.id.button_a_1),
      new LevelData("A-2","a2","sublevel='A-2'",R.id.button_a_2),
      new LevelData("Challenge","","level='Challenge'",R.id.button_challenge),
      new LevelData("C-1","c1","sublevel='C-1'",R.id.button_c1),
      new LevelData("C-2","c2","sublevel='C-2'",R.id.button_c2),
      new LevelData("C-3A","c3a","sublevel='C-3A'",R.id.button_c3a),
      new LevelData("C-3B","c3b","sublevel='C-3B'",R.id.button_c3b),
      new LevelData("All Calls","","level!='Info' and @sublevel!='Styling'",R.id.button_index),
      new LevelData("Index of All Calls","","level!='Info' and @sublevel!='Styling'",R.id.button_index),
      new LevelData("Search Calls","","",R.id.button_search)
    };
    public static LevelData find(String s)
    {
      for (LevelData d: data) {
        if (d.name.equalsIgnoreCase(s) ||
            d.dir.equalsIgnoreCase(s) ||
            d.selector.equalsIgnoreCase(s))
          return d;
      }
      return null;
    }
  }

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
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().remove("navigateupto").commit();
    if (isPortrait()) {
      setTitle("Taminations");
      if (selectedView != null) {
        selectedView.setSelected(false);
        selectedView = null;
      }
    }
    else {
      CalllistFragment cf = new CalllistFragment();
      replaceFragment(cf,R.id.fragment_calllist);
      String level = prefs.getString("level", "Basic and Mainstream");
      LevelData data = LevelData.find(level);
      if (data != null) {
        int id = data.id;
        highlightClick(findViewById(id));
      }
      else
        //  Search
        highlightClick(null);
    }
  }

  /**
   *  This handles clicks on the buttons for different levels.
   *  Display the screen listing calls for that level.
   */
  private void highlightClick(View v)
  {
    if (selectedView != null)
      selectedView.setSelected(false);
    if (v != null)
      v.setSelected(true);
    selectedView = v;
  }
  public void processClick(View v)
  {
    highlightClick(v);
    LevelData d = LevelData.find((String) ((TextView)v).getText());
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("level",d.name).putString("selector",d.selector).commit();
    if (d.name.equals("Search Calls"))
      onSearchRequested();
    else {
      if (isPortrait()) {
        //  Single-fragment display - start calllist activity
        startActivity(new Intent(this,CalllistActivity.class));
      } else {
        //  Multi-fragment display - switch calllist fragment
        CalllistFragment cf = new CalllistFragment();
        replaceFragment(cf,R.id.fragment_calllist);
      }
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

  public void onPracticeClick(View v)
  {
    startActivity(new Intent(this,StartPracticeActivity.class));
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
