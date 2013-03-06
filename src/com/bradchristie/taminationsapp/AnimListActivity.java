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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AnimListActivity extends Activity
       implements OnItemClickListener {

  private class CallListAdapter extends ArrayAdapter<String>
  {
    private LayoutInflater mInflater;
    private ArrayList<Integer> resources = new ArrayList<Integer>();
    private ArrayList<Integer> difficulty = new ArrayList<Integer>();
    CallListAdapter(Context context, int textViewResourceId)
    {
      super(context,textViewResourceId);
      mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public View getView (int position, View convertView, ViewGroup parent)
    {
      TextView myview = (TextView)mInflater.inflate(resources.get(position),parent,false);
      myview.setText((CharSequence)getItem(position));
      int d = difficulty.get(position);
      if (d==1)
        myview.setBackgroundColor(0xffe0e0ff);
      else if (d==2)
        myview.setBackgroundColor(0xffffffe0);
      else if (d==3)
        myview.setBackgroundColor(0xffffe0e0);
      return myview;
    }

    public void add(String s, int r, int d)
    {
      resources.add(r);
      difficulty.add(d);
      add(s);
    }

  }  // end of CallListAdapter class

  private String xmlname;
  private int[] posanim;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animlist);
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    setTitle(prefs.getString("call",getString(android.R.string.untitled)));
    xmlname = prefs.getString("link",getString(android.R.string.untitled)).replace("html", "xml");
    //  For now, ignore links to specific animations
    //  Read the xml file and build the list of animations
    Document doc = Tamination.getXMLAsset(this,xmlname);
    NodeList tams = doc.getElementsByTagName("tam");
    String prevtitle = "";
    String prevgroup = "";
    CallListAdapter s = new CallListAdapter(this,R.layout.calllist_item);
    posanim = new int[tams.getLength()*2];
    for (int j=0; j<posanim.length; j++)
      posanim[j] = -1;
    int diffsum = 0;
    for (int i=0; i<tams.getLength(); i++) {
      Element tam = (Element)tams.item(i);
      if (tam.getAttribute("display").equals("none"))
        continue;  // animations for sequencer only
      String title = tam.getAttribute("title");
      String from = tam.getAttribute("from");
      String group = tam.getAttribute("group");
      int d = 0;
      String diffstr = tam.getAttribute("difficulty");
      if (diffstr.length() > 0)
        d = Integer.valueOf(diffstr);
      diffsum += d;
      if (group.length() > 0) {
        //  Add header for new group as needed
        if (!group.equals(prevgroup)) {
          if (group.matches("\\s+")) {
            //  Blank group, for calls with no common starting phrase
            //  Add a green separator unless it's the first group
            if (s.getCount() > 0)
              s.add(group,R.layout.calllist_separator,0);
          }
          else
            //  Named group e.g. "As Couples.."
            //  Add a header with the group name, which starts
            //  each call in the group
            s.add(group,R.layout.calllist_header,0);
        }
        from = title.replace(group," ").trim();
      }
      else if (!title.equals(prevtitle))
        //  Not a group but a different call
        //  Put out a header with this call
        s.add(title+" from",R.layout.calllist_header,0);
      prevtitle = title;
      prevgroup = group;
      posanim[s.getCount()] = i;
      // Put out a selectable item
      if (group.matches("\\s+"))
        s.add(from,R.layout.calllist_item,d);
      else
        s.add(from,R.layout.calllist_indenteditem,d);
    }
    if (tams.getLength() == 0) {
      //  Special handling if there are no animations for this call
      String title = "Sorry, there are no animations for " +
          prefs.getString("call",getString(android.R.string.untitled));
      s.add(title,R.layout.calllist_header,0);
      title = "You can view the definition by tapping here.";
      s.add(title,R.layout.calllist_item,0);
    }
    //  Build the list of animations
    ListView lv = (ListView)findViewById(R.id.listview_anim);
    lv.setAdapter(s);
    lv.setOnItemClickListener(this);
    //  Show the difficulty legend only if difficulties are set
    View dv = findViewById(R.id.layout_difficulty);
    dv.setVisibility(diffsum > 0 ? View.VISIBLE : View.GONE);
  }

  //  Definition
  public void onButtonDefinitionClicked(View v) {
    startActivity(new Intent(this,DefinitionActivity.class));
  }
  //  Settings
  public void onButtonSettingsClicked(View v) {
    startActivity(new Intent(this,SettingsActivity.class));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_animlist, menu);
    return true;
  }

  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    if (posanim.length == 0) {
      if (position >= 1)
        startActivity(new Intent(this,DefinitionActivity.class));
    }
    else if (posanim[position] >= 0) {
      SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
      prefs.edit().putInt("anim",posanim[position]).commit();
      prefs.edit().putString("xmlname",xmlname).commit();
      startActivity(new Intent(this,AnimationActivity.class));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle item selection
      switch (item.getItemId()) {
          case R.id.menu_settings:
              startActivity(new Intent(this,SettingsActivity.class));
              return true;
          case R.id.menu_definition:
              startActivity(new Intent(this,DefinitionActivity.class));
              return true;
          case R.id.menu_about:
            startActivity(new Intent(this,AboutActivity.class));
            return true;
          //case R.id.help:
          //    showHelp();
          //    return true;
          default:
              return super.onOptionsItemSelected(item);
      }
  }

}
