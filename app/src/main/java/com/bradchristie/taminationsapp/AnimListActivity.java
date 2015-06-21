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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
    public final String title;
    public final String name;
    public final String group;
    public final int resource;
    public final int difficulty;
    public boolean wasSelected;

    public AnimListItem(String t, String n, String g, int r, int d) {
      title = t;
      name = n;
      group = g;
      resource = r;
      difficulty = d;
      wasSelected = false;
    }

    public String toString() {
      return name;
    }
  }

  private class AnimListAdapter extends ArrayAdapter<AnimListItem> {
    private final LayoutInflater mInflater;

    AnimListAdapter(Context context, int textViewResourceId) {
      super(context, textViewResourceId);
      mInflater = (LayoutInflater) context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //  Build one list item view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      AnimListItem item = getItem(position);
      TextView myview = (TextView) mInflater.inflate(item.resource, parent,
          false);
      //  Insert the text.  The view is set up to wrap as needed
      myview.setText(item.toString());
      //  Fade the text if the item was previously selected
      if (item.wasSelected && position != selectedPosition)
        myview.setTextColor(0xff808080);
      //  Set the background according to the difficulty
      int d = item.difficulty;
      if (d == 0)
        myview.setBackgroundResource(R.drawable.animlist_background_default);
      else if (d == 1)
        myview.setBackgroundResource(R.drawable.animlist_background_common);
      else if (d == 2)
        myview.setBackgroundResource(R.drawable.animlist_background_harder);
      else if (d == 3)
        myview.setBackgroundResource(R.drawable.animlist_background_difficult);
      if (position == selectedPosition) {
        myview.setSelected(true);
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
  private AnimListAdapter adapter;
  private boolean multifragment;
  private AnimationFragment animfrag;
  private DefinitionFragment deffrag;
  public TextView selectedView;
  public int selectedPosition;
  public Drawable firstViewBackground;
  private String level;
  private Intent intent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animlist);
    // Read the xml file with the list of animations
    xmlname = intentString("link") + ".xml";
    Document doc = Tamination.getXMLAsset(this, xmlname);
    NodeList tams = Tamination.tamList(doc);
    multifragment = findViewById(R.id.fragment_animation) != null;
    Element tamination = (Element)doc.getElementsByTagName("tamination").item(0);
    String titlestr = tamination.getAttribute("title");
    setTitle(titlestr);
    //  If we came from the master index, the user clicked call could be
    //  something different than the main title of this call
    String clickedtitlestr = intentString("call");
    String webquery = intentString("webquery");
    boolean foundwebquery = false;
    //  Set up the level button at the top right of the title
    level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(LevelData.find(level).name);
    //
    selectedView = null;
    selectedPosition = -1;
    firstViewBackground = null;
    //boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(titlestr));
    View speaker = findViewById(R.id.speaker);
    if (speaker != null)  // null if not landscape
      speaker.setVisibility( /* hasAudio ? View.VISIBLE : */ View.INVISIBLE);

    // Fetch the list of animations and build the table
    String prevtitle = "";
    String prevgroup = "";
    adapter = new AnimListAdapter(this, R.layout.animlist_item);
    //  posanim maps from the display list item to the animation item
    posanim = new int[tams.getLength() * 2];
    for (int j = 0; j < posanim.length; j++)
      posanim[j] = -1;
    int diffsum = 0;
    int firstanim = -1;
    int selectanim = -1;
    for (int i = 0; i < tams.getLength(); i++) {
      Element tam = (Element) tams.item(i);
      if (tam.getAttribute("display").equals("none"))
        continue; // animations for sequencer only
      String title = tam.getAttribute("title");
      String from = tam.getAttribute("from");
      String group = tam.getAttribute("group");
      int d = 0;
      String diffstr = Tamination.tamXref(this,tam).getAttribute("difficulty");
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
      //  Build list item for this animation
      prevtitle = title;
      prevgroup = group;
      posanim[adapter.getCount()] = i;
      //  Remember where the first real animation is in the list
      if (firstanim < 0)
        firstanim = adapter.getCount();
      //  Also remember where the first animation that matches the user
      //  selection from the master index
      if (selectanim < 0 && title.equals(clickedtitlestr))
        selectanim = adapter.getCount();
      //  Check for a match to a web link
      if (webquery != null) {
        String webtarget = group.length() > 0 ? title : title + "from" + from;
        if (webquery.toLowerCase().equals(webtarget.toLowerCase().replaceAll("\\s",""))) {
          selectanim = adapter.getCount();
          foundwebquery = true;
        }
      }
      // Put out a selectable item
      if (group.matches("\\s+"))
        adapter.add(new AnimListItem(title,from, "", R.layout.animlist_item, d));
      else if (group.length() > 0)
        adapter.add(new AnimListItem(title,from, group,
            R.layout.animlist_indenteditem, d));
      else
        adapter.add(new AnimListItem(title,from, title + " from",
            R.layout.animlist_indenteditem, d));
      //
    }

    //  List of all animations completed
    //  User selection from master index supercedes default 1st animation
    intent = new Intent(getIntent());
    if (selectanim >= 0)
      firstanim = selectanim;
    if (webquery != null && !foundwebquery) {
      AlertDialog.Builder ab = new AlertDialog.Builder(this);
      ab.setTitle("Error loading Taminations link");
      ab.setMessage("Incorrect animation: "+webquery);
      ab.setNegativeButton("Cancel",null);
      ab.create().show();
    }
    if (tams.getLength() == 0) {
      // Special handling if there are no animations for this call
      String title = "Sorry, there are no animations for " + titlestr;
      adapter.add(new AnimListItem("",title, "", R.layout.animlist_header, -1));
    } else if (multifragment) {
      //  Showing an animation in another fragment
      //  Highlight the selected animation
      AnimListItem item = adapter.getItem(firstanim);
      setTitle(item.title);
      selectedPosition = firstanim;
      intent.putExtra("anim", posanim[firstanim]);
      intent.putExtra("title", item.title);
    }
    //  Hook up the list of animations
    ListView lv = (ListView)findViewById(R.id.listview_anim);
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(this);
    // Show the difficulty legend only if difficulties are set
    View dv = findViewById(R.id.layout_difficulty);
    dv.setVisibility(diffsum > 0 ? View.VISIBLE : View.GONE);
    //  For landscape tablet, set up the other panels
    if (multifragment) {
      if (tams.getLength() > 0) {
        animfrag = new AnimationFragment();
        animfrag.setArguments(intent.getExtras());
        replaceFragment(animfrag, R.id.fragment_animation);
      }
      deffrag = new DefinitionFragment();
      deffrag.setArguments(intent.getExtras());
      replaceFragment(deffrag, R.id.fragment_definition);
    }
    //  If in portrait and we have a specific requested animation,
    //  go there immediately
    else if (webquery != null && foundwebquery) {
      AnimListItem item = adapter.getItem(selectanim);
      intent.putExtra("anim", posanim[selectanim]);
      intent.putExtra("title", item.title);
      startActivity(intent.setClass(this, AnimationActivity.class));
    }
  }

  //  Link to use with share button
  @Override
  protected String shareURL()
  {
    String url = "http://www.tamtwirlers.org/tamination/" + intentString("link") + ".html";
    if (multifragment) {
      url = animfrag.intentString("url");
    }
    return url;
  }

  // Definition
  public void onButtonDefinitionClicked(View v) {
    if (multifragment) {
      deffrag = new DefinitionFragment();
      deffrag.setArguments(intent.getExtras());
      replaceFragment(deffrag, R.id.fragment_definition);
    }
    else
      startActivity(getIntent().setClass(this, DefinitionActivity.class));
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
    if (selectedView != null) {
      selectedView.setTextColor(0xc0808080);
      if (isPortrait())
        selectedView.setSelected(false);
      selectedView = null;
      selectedPosition = -1;
    }
  }

  public void settingsChanged(int setting)
  {
    if (multifragment)
      animfrag.readSettings(setting);
  }

  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //  Handle hack for 1st item selection
    if (firstViewBackground != null && selectedView != null) {
      //noinspection deprecation
      selectedView.setBackgroundDrawable(firstViewBackground);
      firstViewBackground = null;
    }
    v.setSelected(true);
    if (selectedView != null)
      selectedView.setTextColor(0xff808080);
    selectedView = (TextView)v;
    selectedPosition = position;
    if (posanim.length == 0) {
      // Click on definition item of calls with no animations
      if (position >= 1)
        startActivity(new Intent(this, DefinitionActivity.class));
    } else if (posanim[position] >= 0) {
      // Save info and start animation activity
      AnimListItem item = adapter.getItem(position);
      item.wasSelected = true;
      intent.putExtra("anim", posanim[position]);
      intent.putExtra("title",item.title);
      intent.putExtra("name",item.group+" "+item.name);
      intent.putExtra("xmlname", xmlname);
      if (findViewById(R.id.animation) != null) {
        // Multi-fragment display - switch animation
        setTitle(item.title);
        replaceFragment2(new RotationActivity.FragmentFactory() {
          @Override
          public RotationFragment getFragment() {
            animfrag = new AnimationFragment();
            animfrag.setArguments(intent.getExtras());
            return animfrag;
          }
        }, R.id.fragment_animation);
        //boolean hasAudio = Tamination.assetExists(this, level, Tamination.audioAssetName(item.title));
        //findViewById(R.id.speaker).setVisibility( /* hasAudio ? View.VISIBLE : */ View.INVISIBLE);

      } else
        // Single fragment - start animation activity
        startActivity(intent.setClass(this, AnimationActivity.class));
    }
  }

}
