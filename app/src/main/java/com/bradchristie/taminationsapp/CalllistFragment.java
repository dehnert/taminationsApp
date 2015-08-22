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

import java.util.Locale;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
  private boolean startedWithQuery;

  private class CallListData {
    public final String title;
    public final String link;
    public final String level;
    public final String sublevel;
    public CallListData(String title, String link, String level, String sublevel)
    {
      this.title = title;
      this.link = link;
      this.level = level;
      this.sublevel = sublevel;
    }
  }
  private CallListData calllistdata[];

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    fragment = inflater.inflate(R.layout.fragment_calllist, container, false);
    editText = (EditText)fragment.findViewById(R.id.searchtext);
    //  Hide the keyboard until the user asks for it
    getActivity().getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    //  Read all the data now so searching is more interactive
    String selector = intentString("selector");
    levelname = intentString("level");
    Document doc = Tamination.getXMLAsset(getActivity(),"src/calls.xml");
    NodeList list1 = Tamination.evalXPath("/calls/call[@"+selector+"]",doc);
    String query = "";
    if (levelname.equals("Index of All Calls")) {
      doc = Tamination.getXMLAsset(getActivity(),"src/callindex.xml");
      list1 = Tamination.evalXPath("/calls/call",doc);
      query = intentString("query");
      if (query == null)
        query = "";
      editText.setText(query);
    }
    calllistdata = new CallListData[list1.getLength()];
    for (int j=0; j<list1.getLength(); j++) {
      Element e1 = (Element)list1.item(j);
      calllistdata[j] = new CallListData(
          e1.getAttribute("title"),
          e1.getAttribute("link"),
          e1.getAttribute("level"),
          e1.getAttribute("sublevel"));
    }
    startedWithQuery = query.length() > 0;
    resetView(query);
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

    return fragment;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    //  If the level has changed, re-generate the call list
    if (!intentString("level").equals(levelname)) {
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
    setTitle("Taminations - "+levelname);
    cla = new CallListAdapter(getActivity());
    //  Build a regex from the user query
    //  Use upper case and dup numbers while building regex so expressions don't get compounded
    query = query.toLowerCase();
    //  Remove all non-alphanums that might interfere with the regex
    query = query.replaceAll("[^a-zA-Z0-9/-]+"," ");
    //  Through => Thru
    query = query.replaceAll("\\bthrough\\b","THRU");
    //  Process fractions 1/2 3/4 1/4 2/3
    query = query.replaceAll("\\b1/2|(one.)?half\\b","(HALF|11/22)");
    query = query.replaceAll("\\b(three\\s+quarters?|3/4)\\b","33/44");
    query = query.replaceAll("\\b((one\\s+)?quarter|1/4)\\b","(QUARTER|11/44)");
    query = query.replaceAll("\\btwo.thirds?\\b","22/33");

    //  Process any other numbers
    query = query.replaceAll("\\b(1|one)\\b","(11|ONE)");
    query = query.replaceAll("\\b(2|two)\\b","(22|TWO)");
    query = query.replaceAll("\\b(3|three)\\b","(33|THREE)");
    query = query.replaceAll("\\b(4|four)\\b","(44|FOUR)");
    query = query.replaceAll("\\b(5|five)\\b","(55|FIVE)");
    query = query.replaceAll("\\b(6|six)\\b","(66|SIX)");
    query = query.replaceAll("\\b(7|seven)\\b","(77|SEVEN)");
    query = query.replaceAll("\\b(8|eight)\\b","(88|EIGHT)");
    query = query.replaceAll("\\b(9|nine)\\b","(99|NINE)");

    //  Finally repair the upper case and dup numbers
    query = query.toLowerCase();
    query = query.replaceAll("([0-9])\\1", "$1");
    //  The regex has to match the entire call string, so add "anything"
    //  prefix and postfix expressions
    Pattern p = Pattern.compile(".*" + query + ".*");
    int exactmatches = 0;
    CallListItem exactcla = null;
    String action = intentString("action");
    String weblink = null;
    if (action.contains("SEARCH") || action.contains("VIEW"))
      weblink = intentString("link");
    //  Prepare for matching a link from a web site
    if (weblink != null)
      weblink = LevelActivity.LevelData.find(intentString("level")).dir
                    + "/" + weblink.replace(".html","");
    for (CallListData c : calllistdata) {
      if (p.matcher(c.title.toLowerCase(Locale.US)).matches()) {
        CallListItem item = new CallListItem(c.title,
            c.level,
            c.sublevel,
            c.link);
        cla.add(item);
        //  If the query was passed in from Google, remember if it matches exactly.
        //  If we get just one exact match, go there immediately.
        if (startedWithQuery && c.title.toLowerCase().matches(query)) {
          exactcla = item;
          exactmatches += 1;
        }
        //  If a website link was passed in, see if it matches
        if (weblink != null && weblink.equals(c.link)) {
          exactcla = item;
          exactmatches += 1;
        }
      }
    }
    if (exactmatches == 1) {
      CallClickListener act = (CallClickListener)getActivity();
      Intent intent = new Intent();
      intent.putExtras(getArguments());
      intent.putExtra("call",exactcla.call);
      intent.putExtra("link",exactcla.link);
      act.onCallClick(intent);
    }
    else if (weblink != null) {
      AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
      ab.setTitle("Error loading Taminations link");
      ab.setMessage("Incorrect call: " + intentString("link"));
      ab.setNegativeButton("Cancel", null);
      ab.create().show();
    }
    //  Build the list from the array
    ListView lv = (ListView)fragment.findViewById(R.id.listview_calls);
    lv.setAdapter(cla);
    lv.setFastScrollEnabled(cla.getCount() > 20);
    lv.setOnItemClickListener(this);
    //  Make sure web link is only processed once
    Bundle b = getArguments();
    if (b != null) {
      b.putString("action", Intent.ACTION_MAIN);
      b.remove("webquery");
      //  A bit of a hack, reaching up into the activity
      getActivity().getIntent().removeExtra("webquery");
    }
  }

  //  Process a click on one of the calls
  public void onItemClick(AdapterView<?> parent, View v, int position, long id)
  {
    hideKeyboard();
    //  Save the call info
    CallListItem item = cla.getItem(position);
    CallClickListener act = (CallClickListener)getActivity();
    Intent intent = new Intent();
    intent.putExtra("call",item.call);
    intent.putExtra("link",item.link);
    act.onCallClick(intent);
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
    CallListAdapter(Context context)
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
      switch (item.level) {
        case "Basic and Mainstream":
          cellview.setBackgroundColor(0xffe0e0ff);
          break;
        case "Plus":
          cellview.setBackgroundColor(0xffe0ffe0);
          break;
        case "Advanced":
          cellview.setBackgroundColor(0xfffff0c0);
          break;
        case "Challenge":
          cellview.setBackgroundColor(0xffffe0e0);
          break;
      }
      return cellview;
    }

  }

}
