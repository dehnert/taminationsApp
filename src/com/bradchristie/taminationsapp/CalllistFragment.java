/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2014 Brad Christie

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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class CalllistFragment extends RotationFragment implements OnItemClickListener
{
  private CallListAdapter cla;
  private View fragment;
  private String levelname;
  private EditText editText;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    fragment = inflater.inflate(R.layout.fragment_calllist, container, false);
    editText = (EditText) fragment.findViewById(R.id.searchtext);
    editText.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          hideKeyboard();
          handled = true;
        }
        return handled;
      }
    });
    editText.setOnTouchListener(new OnTouchListener()
    {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (editText.getCompoundDrawables()[2] == null)
          return false;
        if (event.getAction() != MotionEvent.ACTION_UP)
          return false;
        if (event.getX() > editText.getWidth() - editText.getPaddingRight() - 40) {
          editText.setText("");
        }
        return false;
      }
    });
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
      @Override
      public void afterTextChanged(Editable s)
      {
        resetView(s.toString());
      }
    });
    //  Hide the keyboard until the user asks for it
    getActivity().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    resetView("");
    return fragment;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    //  If the level has changed, re-generate the call list
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    if (!prefs.getString("level","Basic and Mainstream").equals(levelname)) {
      resetView("");
      editText.setText("");
    }
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
        Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(fragment.getWindowToken(), 0);
  }

  private void resetView(String query)
  {
    //  Set the title to the current dance level
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Activity.MODE_PRIVATE);
    levelname = prefs.getString("level","Basic and Mainstream");
    String selector = prefs.getString("selector","level='Basic and Mainstream' and @sublevel!='Styling'");
    setTitle("Taminations - "+levelname);
    //  Fetch all the calls for this level and store in the array of objects
    Document doc = Tamination.getXMLAsset(getActivity(),"src/calls.xml");
    NodeList list1 = Tamination.evalXPath("/calls/call[@"+selector+"]",doc);
    cla = new CallListAdapter(getActivity(),R.layout.calllist_item);
    for (int j=0; j<list1.getLength(); j++) {
      Element e1 = (Element)list1.item(j);
      if (!e1.getAttribute("text").toLowerCase(Locale.US).contains(query.toLowerCase()))
        continue;
      CallListItem item = new CallListItem(e1.getAttribute("text"),
                                           e1.getAttribute("level"),
                                           e1.getAttribute("sublevel"),
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
  public void onItemClick(AdapterView<?> parent, View v, int position, long id)
  {
    hideKeyboard();
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
    public String sublevel;
    public CallListItem(String _call, String _level, String _sublevel, String _link)
    {
      call = _call;
      level = _level;
      sublevel = _sublevel;
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
      super(context,R.layout.calllist_item,R.id.button_calllist);
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
      View cellview = mInflater.inflate(R.layout.calllist_item,null);
      TextView myview = (TextView)cellview.findViewById(R.id.button_calllist);
      CallListItem item = getItem(position);
      myview.setText(item.toString());
      TextView mylevel = (TextView)cellview.findViewById(R.id.level_calllist);
      mylevel.setText(item.sublevel);
      if (item.level.equals("Basic and Mainstream"))
        cellview.setBackgroundColor(0xffe0e0ff);
      else if (item.level.equals("Plus"))
        cellview.setBackgroundColor(0xffe0ffe0);
      else if (item.level.equals("Advanced"))
        cellview.setBackgroundColor(0xfffff0c0);
      else if (item.level.equals("Challenge"))
        cellview.setBackgroundColor(0xffffe0e0);
      return cellview;
    }

  }

}
