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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;


public class CalllistActivity extends PortraitActivity
                              implements  CallClickListener
{

  private CalllistFragment cf;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Intent intent = getIntent();
    String action = intent.getAction();
    SharedPreferences prefs = getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    if (action != null && action.endsWith("SEARCH_ACTION")) {
      String query = intent.getStringExtra(SearchManager.QUERY);
      prefs.edit().putString("level","Index of All Calls")
                  .putString("selector","level!='Info' and @sublevel!='Styling'" )
                  .putString("query",query)
                  .commit();
    }
    else  //  clear out any previous query
      prefs.edit().putString("query","").commit();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calllist);
    cf = new CalllistFragment();
  }

  protected void onResume()
  {
    super.onResume();
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String upto = prefs.getString("navigateupto", "");
    replaceFragment(cf,R.id.fragment_calllist);
    if (upto.equals("CalllistActivity")) {
      prefs.edit().remove("navigateupto").commit();
    }
    else if (upto.length() > 0)
      finish();
  }

  public void onCallClick(String call, String link)
  {
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("call",call)
         .putString("link",link)
         .putInt("anim",0).commit();
    //  Start the next activity
    startActivity(new Intent(this,AnimListActivity.class));
  }

  public void onLogoClicked(View view) {
    super.onLogoClicked(view);
  }
}
