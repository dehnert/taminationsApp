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

import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class CalllistFragment extends RotationFragment implements OnItemClickListener
{
  private CallListAdapter cla;
  private View fragment;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    fragment = inflater.inflate(R.layout.fragment_calllist, container, false);
    resetView();
    return fragment;
  }

  public void resetView()
  {
    //  Set the title to the current dance level
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    String levelname = prefs.getString("level","Basic and Mainstream");
    String selector = prefs.getString("selector","level='Basic and Mainstream' and @sublevel!='Styling'");
    String query = null;
    if (levelname.equals("Search Calls")) {
      query = selector;
      selector = LevelData.find("All Calls").selector;
      setTitle("Taminations - "+levelname+": "+query);
    } else
      setTitle("Taminations - "+levelname);
    //  Fetch all the calls for this level and store in the array of objects
    Document doc = Tamination.getXMLAsset(getActivity(),"src/calls.xml");
    NodeList list1 = Tamination.evalXPath("/calls/call[@"+selector+"]",doc);
    cla = new CallListAdapter(getActivity(),R.layout.calllist_item);
    for (int j=0; j<list1.getLength(); j++) {
      Element e1 = (Element)list1.item(j);
      if (query != null && !e1.getAttribute("text").toLowerCase(Locale.US).contains(query))
        continue;
      CallListItem item = new CallListItem(e1.getAttribute("text"),
                                           e1.getAttribute("level"),
                                           e1.getAttribute("link"));
      cla.add(item);
    }
    //  Build the list from the array
    ListView lv = (ListView)fragment.findViewById(R.id.listview_calls);
    lv.setAdapter(cla);
    lv.setFastScrollEnabled(cla.getCount() > 20);
    lv.setOnItemClickListener(this);
  }

  //  Process a click on one of the calls
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    //  Save the call info
    CallListItem item = cla.getItem(position);
    CallClickListener act = (CallClickListener)getActivity();
    act.onCallClick(item.call, item.link);
  }

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
    public String level;
    public CallListItem(String _call, String _level, String _link)
    {
      call = _call;
      level = _level;
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
    private LayoutInflater mInflater;
    CallListAdapter(Context context, int textViewResourceId)
    {
      super(context,textViewResourceId);
      mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
      TextView myview = (TextView)mInflater.inflate(R.layout.calllist_item,parent,false);
      CallListItem item = getItem(position);
      myview.setText(item.toString());
      if (item.level.equals("Basic and Mainstream"))
        myview.setBackgroundColor(0xffe0e0ff);
      else if (item.level.equals("Plus"))
        myview.setBackgroundColor(0xffe0ffe0);
      else if (item.level.equals("Advanced"))
        myview.setBackgroundColor(0xfffff0c0);
      else if (item.level.equals("Challenge"))
        myview.setBackgroundColor(0xffffe0e0);
      return myview;
    }

  }

}
