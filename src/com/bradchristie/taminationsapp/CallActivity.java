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

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CallActivity extends Activity implements OnItemClickListener {

  /**
   *   This is an array of hashes, one array entry for each call,
   *   in alphabetical order as given by the list in menus.xml
   *   Each hash has two entries
   *       "call" - the displayed name of the call
   *       "link" - where to fetch the html and xml files for the call  */
  private ArrayList<HashMap<String,String>> mapping;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_call);
    //  Set the title to the current dance level
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String levelname = prefs.getString("level",getString(android.R.string.untitled));
    TextView titleView = (TextView)findViewById(R.id.call_title);
    titleView.setText(levelname);
    //  Fetch all the calls for this level and store in the array of hashes
    mapping = new ArrayList<HashMap<String,String>>();
    Document doc = Tamination.getXMLAsset(this,"src/menus.xml");
    NodeList list1 = Tamination.evalXPath("/menu/menulist[@title='"+levelname+"']/menuitem",doc);
    for (int j=0; j<list1.getLength(); j++) {
      Element e1 = (Element)list1.item(j);
      HashMap<String,String> map = new HashMap<String,String>();
      map.put("call",e1.getAttribute("text"));
      map.put("link",e1.getAttribute("link"));
      mapping.add(map);
    }
    //  Build the list of calls in the way Android wants it
    String[] from = { "call" };
    int[] to = { R.id.button_calllist };
    SimpleAdapter s = new SimpleAdapter(this,mapping,R.layout.calllist_item,from,to);
    ListView lv = (ListView)findViewById(R.id.listview_calls);
    lv.setAdapter(s);
    lv.setOnItemClickListener(this);
  }

  //  Process a click on one of the calls
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //  Save the call info
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    prefs.edit().putString("call",mapping.get(position).get("call")).commit();
    prefs.edit().putString("link",mapping.get(position).get("link")).commit();
    //  Start the next activity
    startActivity(new Intent(this,AnimListActivity.class));
  }

}
