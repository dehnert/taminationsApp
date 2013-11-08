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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;


public class AnimListActivity extends RotationActivity
    implements  OnItemClickListener, AnimationSettingsListener {

  private class AnimListItem {
    public String title;
    public String name;
    public String group;
    public int resource;
    public int difficulty;

    public AnimListItem(String t, String n, String g, int r, int d) {
      title = t;
      name = n;
      group = g;
      resource = r;
      difficulty = d;
    }

    public String toString() {
      return name;
    }
  }

  private class AnimListAdapter extends ArrayAdapter<AnimListItem> {
    private LayoutInflater mInflater;

    AnimListAdapter(Context context, int textViewResourceId) {
      super(context, textViewResourceId);
      mInflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      AnimListItem item = getItem(position);
      TextView myview = (TextView) mInflater.inflate(item.resource, parent,
          false);
      myview.setText(item.toString());
      int d = item.difficulty;
      if (d == 0)
        myview.setBackgroundResource(R.drawable.animlist_background_default);
      else if (d == 1)
        myview.setBackgroundResource(R.drawable.animlist_background_common);
      else if (d == 2)
        myview.setBackgroundResource(R.drawable.animlist_background_harder);
      else if (d == 3)
        myview.setBackgroundResource(R.drawable.animlist_background_difficult);
      if (position == 1)
        myview.setSelected(true);
      //  Hack for showing 1st item as selected
      if (selectedView == null && d >= 0 && !isPortrait()) {
        firstViewBackground = myview.getBackground();
        if (d == 0)
          myview.setBackgroundResource(R.drawable.background_default_selected);
        else if (d == 1)
          myview.setBackgroundResource(R.drawable.background_common_selected);
        else if (d == 2)
          myview.setBackgroundResource(R.drawable.background_harder_selected);
        else if (d == 3)
          myview.setBackgroundResource(R.drawable.background_difficult_selected);
        selectedView = myview;
      }
      return myview;
    }

  } // end of AnimListAdapter class

  private String xmlname;
  private int[] posanim;
  private TextView titleView;
  private AnimListAdapter adapter;
  private boolean multifragment;
  private AnimationFragment animfrag;
  private DefinitionFragment deffrag;
  public TextView selectedView;
  public Drawable firstViewBackground;
  private String level;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animlist);
    // Read the xml file with the list of animations
    SharedPreferences prefs = getSharedPreferences("Taminations", MODE_PRIVATE);
    xmlname = prefs.getString("link", getString(android.R.string.untitled))
        .replace("html", "xml");
    Document doc = Tamination.getXMLAsset(this, xmlname);
    NodeList tams = doc.getElementsByTagName("tam");
    multifragment = findViewById(R.id.fragment_animation) != null;
    titleView = (TextView)findViewById(R.id.title2);
    String titlestr = prefs.getString("call",
        getString(android.R.string.untitled));
    if (multifragment) {
      if (tams.getLength() > 0) {
        animfrag = new AnimationFragment();
        replaceFragment(animfrag, R.id.fragment_animation);
      }
      deffrag = new DefinitionFragment();
      replaceFragment(deffrag, R.id.fragment_definition);
    }
    setTitle(titlestr);;
    level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(LevelData.find(level).name);
    selectedView = null;
    firstViewBackground = null;
    boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(titlestr));
    View speaker = findViewById(R.id.speaker);
    if (speaker != null)  // null if not landscape
      speaker.setVisibility(hasAudio ? View.VISIBLE : View.INVISIBLE);

    // Fetch the list of animations and build the table
    String prevtitle = "";
    String prevgroup = "";
    adapter = new AnimListAdapter(this, R.layout.animlist_item);
    posanim = new int[tams.getLength() * 2];
    for (int j = 0; j < posanim.length; j++)
      posanim[j] = -1;
    int diffsum = 0;
    int firstanim = -1;
    for (int i = 0; i < tams.getLength(); i++) {
      Element tam = (Element) tams.item(i);
      if (tam.getAttribute("display").equals("none"))
        continue; // animations for sequencer only
      String title = tam.getAttribute("title");
      String from = tam.getAttribute("from");
      String group = tam.getAttribute("group");
      int d = 0;
      String diffstr = tam.getAttribute("difficulty");
      if (diffstr.length() > 0)
        d = Integer.valueOf(diffstr);
      diffsum += d;
      if (group.length() > 0) {
        // Add header for new group as needed
        if (!group.equals(prevgroup)) {
          if (group.matches("\\s+")) {
            // Blank group, for calls with no common starting phrase
            // Add a green separator unless it's the first group
            if (adapter.getCount() > 0)
              adapter.add(new AnimListItem("",group, "",
                  R.layout.animlist_separator, -1));
          } else
            // Named group e.g. "As Couples.."
            // Add a header with the group name, which starts
            // each call in the group
            adapter.add(new AnimListItem("",group, "", R.layout.animlist_header,
                -1));
        }
        from = title.replace(group, " ").trim();
      } else if (!title.equals(prevtitle))
        // Not a group but a different call
        // Put out a header with this call
        adapter.add(new AnimListItem("",title + " from", "",
            R.layout.animlist_header, -1));
      prevtitle = title;
      prevgroup = group;
      posanim[adapter.getCount()] = i;
      if (firstanim < 0)
        firstanim = adapter.getCount();
      // Put out a selectable item
      if (group.matches("\\s+"))
        adapter.add(new AnimListItem(title,from, "", R.layout.animlist_item, d));
      else if (group.length() > 0)
        adapter.add(new AnimListItem(title,from, group,
            R.layout.animlist_indenteditem, d));
      else
        adapter.add(new AnimListItem(title,from, title + " from",
            R.layout.animlist_indenteditem, d));
    }
    if (tams.getLength() == 0) {
      // Special handling if there are no animations for this call
      String title = "Sorry, there are no animations for "
          + prefs.getString("call", getString(android.R.string.untitled));
      adapter.add(new AnimListItem("",title, "", R.layout.animlist_header, -1));
    } else if (multifragment) {
      AnimListItem item = adapter.getItem(firstanim);
      titleView.setText(item.title);
      prefs.edit().putString("title",item.title).commit();
    }
    // Build the list of animations
    ListView lv = (ListView)findViewById(R.id.listview_anim);
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(this);
    // Show the difficulty legend only if difficulties are set
    View dv = findViewById(R.id.layout_difficulty);
    dv.setVisibility(diffsum > 0 ? View.VISIBLE : View.GONE);
  }

  // Definition
  public void onButtonDefinitionClicked(View v) {
    if (multifragment) {
      deffrag = new DefinitionFragment();
      replaceFragment(deffrag,R.id.fragment_definition);
    }
    else
      startActivity(new Intent(this,DefinitionActivity.class));
  }
  public void setPart(int part)
  {
    if (multifragment && deffrag != null)
      deffrag.setPart(part);
  }

  // Settings
  public void onButtonSettingsClicked(View v) {
    if (multifragment) {
      deffrag = null;
      SettingsFragment settingsfragment = new SettingsFragment();
      replaceFragment(settingsfragment,R.id.fragment_definition);
      settingsfragment.setListener(this);
    }
    else
      startActivity(new Intent(this, SettingsActivity.class));
  }

  protected void onResume()
  {
    super.onResume();
    /*
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String upto = prefs.getString("navigateupto", "");
    if (upto.equals("AnimListActivity"))
      prefs.edit().remove("navigateupto").commit();
    else if (upto.length() > 0)
      finish();
      */
    if (isPortrait()) {
      if (selectedView != null)
        selectedView.setSelected(false);
      selectedView = null;
    }
  }


  public void settingsChanged(int setting)
  {
    if (multifragment)
      animfrag.readSettings(setting);
  }

  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //  Handle hack for 1st item selection
    if (firstViewBackground != null) {
      selectedView.setBackgroundDrawable(firstViewBackground);
      firstViewBackground = null;
    }
    v.setSelected(true);
    selectedView = (TextView)v;
    if (posanim.length == 0) {
      // Click on definition item of calls with no animations
      if (position >= 1)
        startActivity(new Intent(this, DefinitionActivity.class));
    } else if (posanim[position] >= 0) {
      // Save info and start animation activity
      AnimListItem item = adapter.getItem(position);
      SharedPreferences prefs = getSharedPreferences("Taminations",
          MODE_PRIVATE);
      prefs.edit().putInt("anim", posanim[position])
           .putString("title",item.title)
           .putString("name",item.group+" "+item.name)
           .putString("xmlname", xmlname).commit();
      if (findViewById(R.id.animation) != null) {
        // Multi-fragment display - switch animation
        titleView.setText(item.title);
        replaceFragment2(new RotationActivity.FragmentFactory() {
          @Override
          public RotationFragment getFragment() {
            animfrag = new AnimationFragment();
            return animfrag;
          }
        }, R.id.fragment_animation);
        boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(item.title));
        findViewById(R.id.speaker).setVisibility(hasAudio ? View.VISIBLE : View.INVISIBLE);

      } else
        // Single fragment - start animation activity
        startActivity(new Intent(this, AnimationActivity.class));
    }
  }

}
