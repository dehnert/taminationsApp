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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class CalllistActivity extends PortraitActivity
                              implements  CallClickListener
{

  private CalllistFragment cf;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calllist);
    cf = new CalllistFragment();
    cf.setArguments(getIntent().getExtras());
  }

  protected void onResume()
  {
    super.onResume();
    replaceFragment(cf, R.id.fragment_calllist);
    //  Make sure intents are only processed once
    getIntent().setAction(Intent.ACTION_MAIN);
  }

  public void onCallClick(Intent intent)
  {
    //  Start the next activity
    startActivity(intent.setClass(this, AnimListActivity.class));
  }

  public void onLogoClicked(View view) {
    super.onLogoClicked(view);
  }
}
