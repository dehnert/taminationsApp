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

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
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
      new LevelData("Index of All Calls","","level!='Info' and @sublevel!='Styling'",R.id.button_index)
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
  }  //  end of LevelData class

  private View selectedView = null;

  /**
   *    Called once after Activity is started
   */
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_level);
    setTitle("Taminations");
    if (findViewById(R.id.fragment_calllist) != null) {
      //  Multi-fragment display - set calllist fragment to show About
      RotationFragment af = new AboutFragment();
      replaceFragment(af,R.id.fragment_calllist);
    }
  }

  @Override
  protected void onNewIntent(Intent intent)
  {
    setIntent(intent);
    //  Then onResume is called, and getIntent will retrieve the new intent
  }

  /**
   *   Called whenever this activity is re-displayed
   */
  @Override
  protected void onResume() {
    super.onResume();
    String action = getIntent().getAction();

    //  Process request from Google to search Taminations
    if (action != null) {
      if (action.contains("SEARCH")) {
        Intent intent = new Intent(getIntent());
        intent.putExtra("action",action);
        intent.putExtra("query", getIntent().getStringExtra(SearchManager.QUERY));
        gotoLevel(LevelData.find("Index of All Calls"),intent);
      }

      //  Process link to app
      if (action.contains("VIEW")) {
        Uri u = getIntent().getData();
        Intent intent = new Intent(getIntent());
        intent.putExtra("action", action);
        if (u != null) {
          if (u.getQuery() != null)
            intent.putExtra("webquery", u.getQuery());
          if (u.getPath() != null) {
            String[] parts = u.getPath().split("/");
            //  Look for the level in the path
            LevelData d = null;
            for (int i=0; i<parts.length; i++) {
              if (parts[i].matches("(b1|b2|ms|plus|a1|a2|c1|c2|c3a|c3b)")) {
                d = LevelData.find(parts[i]);
                if (i < parts.length-1)
                  intent.putExtra("link", parts[i+1]);
                gotoLevel(d,intent);
              }
            }
            if (d == null) {
              //  bad level in link
              AlertDialog.Builder ab = new AlertDialog.Builder(this);
              ab.setTitle("Error loading Taminations link");
              ab.setMessage("Unable to parse level: "+u);
              ab.setNegativeButton("Cancel",null);
              ab.create().show();
            }
          }
        }
      }
    }

    //  End of processing intent
    //  So reset it to MAIN so we don't repeat when the user navigates back up
    getIntent().setAction(Intent.ACTION_MAIN).replaceExtras((Bundle)null);

    //  For portrait view, make sure none of the levels are highlighted
    String level = intentString("level");
    if (isPortrait()) {
      setTitle("Taminations");
      if (selectedView != null) {
        selectedView.setSelected(false);
        selectedView = null;
      }
    }
    //  Landscape multi-panel view
    else if (level != null) {
      CalllistFragment cf = new CalllistFragment();
      cf.setArguments(getIntent().getExtras());
      replaceFragment(cf, R.id.fragment_calllist);
      LevelData data = LevelData.find(level);
      highlightClick(findViewById(data.id));
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
  public void processClick(View v) {
    highlightClick(v);
    LevelData d = LevelData.find(((TextView) v).getText().toString());
    gotoLevel(d, new Intent().putExtra("action",Intent.ACTION_MAIN));
  }

  private void gotoLevel(LevelData d, Intent intent)
  {
    intent.putExtra("level", d.name);
    intent.putExtra("selector", d.selector);
    if (isPortrait()) {
      //  Single-fragment display - start calllist activity
      startActivity(intent.setClass(this, CalllistActivity.class));
    } else {
      //  Multi-fragment display - switch calllist fragment
      CalllistFragment cf = new CalllistFragment();
      cf.setArguments(intent.getExtras());
      replaceFragment(cf,R.id.fragment_calllist);
    }
  }

  //  Process a click on one of the calls
  public void onCallClick(Intent intent)
  {
    //  Save the call info
    //  Start the next activity
    startActivity(intent.setClass(this, AnimListActivity.class));
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
      startActivity(new Intent(this,SequenceActivity.class));
  }

}
