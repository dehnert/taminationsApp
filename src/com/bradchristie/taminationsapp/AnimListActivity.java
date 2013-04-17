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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class AnimListActivity extends FragmentActivity
       implements OnItemClickListener {

  private class AnimListItem
  {
    public String name;
    public String group;
    public int resource;
    public int difficulty;
    public AnimListItem(String n, String g, int r, int d)
    {
      name = n;
      group = g;
      resource = r;
      difficulty = d;
    }
    public String toString()
    {
      return name;
    }
  }

  private class AnimListAdapter extends ArrayAdapter<AnimListItem>
  {
    private LayoutInflater mInflater;
    AnimListAdapter(Context context, int textViewResourceId)
    {
      super(context,textViewResourceId);
      mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
      AnimListItem item = getItem(position);
      TextView myview = (TextView)mInflater.inflate(item.resource,parent,false);
      myview.setText(item.toString());
      int d = item.difficulty;
      if (d==1)
        myview.setBackgroundColor(0xffc0ffc0);
      else if (d==2)
        myview.setBackgroundColor(0xffffffc0);
      else if (d==3)
        myview.setBackgroundColor(0xffffc0c0);
      else if (d==0)
        myview.setBackgroundColor(0xffffffff);
      return myview;
    }

  }  // end of AnimListAdapter class

  private String xmlname;
  private int[] posanim;
  private TextView titleView;
  private AnimListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animlist);
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    //  Set the title
    boolean multifragment = findViewById(R.id.animation) != null;
    titleView = (TextView)findViewById(R.id.animlist_title);
    String titlestr = prefs.getString("call",getString(android.R.string.untitled));
    if (!multifragment) {
      if (titlestr.length() > 40)
        titleView.setTextSize(18.0f);
      else if (titlestr.length() > 16)
        titleView.setTextSize(24.0f);
      else
        titleView.setTextSize(36.0f);
    }
    titleView.setText(titlestr);

    //  Fetch the list of animations and build the table
    xmlname = prefs.getString("link",getString(android.R.string.untitled)).replace("html", "xml");
    //  Read the xml file and build the list of animations
    Document doc = Tamination.getXMLAsset(this,xmlname);
    NodeList tams = doc.getElementsByTagName("tam");
    String prevtitle = "";
    String prevgroup = "";
    adapter = new AnimListAdapter(this,R.layout.animlist_item);
    posanim = new int[tams.getLength()*2];
    for (int j=0; j<posanim.length; j++)
      posanim[j] = -1;
    int diffsum = 0;
    int firstanim = -1;
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
            if (adapter.getCount() > 0)
              adapter.add(new AnimListItem(group,"",R.layout.animlist_separator,-1));
          }
          else
            //  Named group e.g. "As Couples.."
            //  Add a header with the group name, which starts
            //  each call in the group
            adapter.add(new AnimListItem(group,"",R.layout.animlist_header,-1));
        }
        from = title.replace(group," ").trim();
      }
      else if (!title.equals(prevtitle))
        //  Not a group but a different call
        //  Put out a header with this call
        adapter.add(new AnimListItem(title+" from","",R.layout.animlist_header,-1));
      prevtitle = title;
      prevgroup = group;
      posanim[adapter.getCount()] = i;
      if (firstanim < 0)
        firstanim = adapter.getCount();
      // Put out a selectable item
      if (group.matches("\\s+"))
        adapter.add(new AnimListItem(from,"",R.layout.animlist_item,d));
      else if (group.length() > 0)
        adapter.add(new AnimListItem(from,group,R.layout.animlist_indenteditem,d));
      else
        adapter.add(new AnimListItem(from,title+" from",R.layout.animlist_indenteditem,d));
    }
    if (tams.getLength() == 0) {
      //  Special handling if there are no animations for this call
      String title = "Sorry, there are no animations for " +
          prefs.getString("call",getString(android.R.string.untitled));
      adapter.add(new AnimListItem(title,"",R.layout.animlist_header,-1));
    } else {
      AnimListItem item = adapter.getItem(firstanim);
      titleView.setText(item.group+" "+item.name);
    }
    //  Build the list of animations
    ListView lv = (ListView)findViewById(R.id.listview_anim);
    lv.setAdapter(adapter);
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

  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    if (posanim.length == 0) {
      //  Click on definition item of calls with no animations
      if (position >= 1)
        startActivity(new Intent(this,DefinitionActivity.class));
    }
    else if (posanim[position] >= 0) {
      //  Save info and start animation activity
      SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
      prefs.edit().putInt("anim",posanim[position])
                  .putString("xmlname",xmlname).commit();
      if (findViewById(R.id.animation) != null) {
        //  Multi-fragment display - switch animation
        AnimListItem item = adapter.getItem(position);
        titleView.setText(item.group+" "+item.name);
        AnimationFragment af = (AnimationFragment)getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_animation);
        af.resetAnimation();
      }
      else
        //  Single fragment - start animation activity
      startActivity(new Intent(this,AnimationActivity.class));
    }
  }

}
