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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

public class CalllistActivity extends PortraitActivity
                              implements OnItemClickListener, CallClickListener
{

  private CallListAdapter cla;
  /**
   *   This is an array of hashes, one array entry for each call,
   *   in alphabetical order as given by the list in menus.xml
   *   Each hash has two entries
   *       "call" - the displayed name of the call
   *       "link" - where to fetch the html and xml files for the call  */
  class CallListItem
  {
    public String call;
    public String link;
    public CallListItem(String _call, String _link)
    {
      call = _call;
      link = _link;
    }
    public String toString()
    {
      return call;
    }
  }

  private class CallListAdapter extends ArrayAdapter<CallListItem>
                                implements SectionIndexer
  {
    private String[] index =
      { "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
        "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    CallListAdapter(Context context, int textViewResourceId)
    {
      super(context,textViewResourceId);
    }

    @Override
    public Object[] getSections()
    {
      return index;
    }

    @Override
    public int getPositionForSection(int section)
    {
      if (section == 0)
        return 0;
      for (int i=0; i<getCount(); i++)
        if (getItem(i).call.compareTo(index[section]) > 0)
          return i;
      return 0;
    }

    @Override
    public int getSectionForPosition(int position)
    {
      if (position == 0)
        return 0;
      String letter = getItem(position).call.substring(0,1);
      for (int i=0; i<index.length; i++)
        if (index[i].equals(letter))
          return i;
      return 0;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calllist);
    return;
  }

  protected void onResume()
  {
    super.onResume();
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String upto = prefs.getString("navigateupto", "");
    CalllistFragment cf = new CalllistFragment();
    replaceFragment(cf,R.id.fragment_calllist);
    if (upto.equals("CalllistActivity")) {
      prefs.edit().remove("navigateupto").commit();
    }
    else if (upto.length() > 0)
      finish();
  }

  //  Process a click on one of the calls
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //  Save the call info
    CallListItem item = cla.getItem(position);
    onCallClick(item.call,item.link);
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

}
