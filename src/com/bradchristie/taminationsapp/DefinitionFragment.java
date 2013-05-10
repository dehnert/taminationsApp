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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

@SuppressLint("SetJavaScriptEnabled")
public class DefinitionFragment extends RotationFragment
{

  private WebView defview;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    View fragment = inflater.inflate(R.layout.fragment_definition, container, false);
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    String name = prefs.getString("link",getString(android.R.string.untitled));
    defview = (WebView)fragment.findViewById(R.id.definitionView);
    //  Turn on pinch-to-zoom, which is off(!) by default
    defview.getSettings().setBuiltInZoomControls(true);
    defview.getSettings().setJavaScriptEnabled(true);
    defview.loadUrl("file:///android_asset/" + name);
    String jsfunction =
    "    function setPart(part)    { "+
    "      var nodes = document.getElementsByTagName('span'); "+
    "      for (var i=0; i<nodes.length; i++) { "+
    "        var elem = nodes.item(i); "+
    "        var classstr = ' '+elem.className+' '; "+
    "        classstr = classstr.replace('definition-highlight',''); "+
    "        var teststr = ' '+classstr+' '+elem.id+' '; "+
    "        if (teststr.indexOf(' '+currentcall+part+' ') > 0 || "+
    "            teststr.indexOf('Part'+part+' ') > 0) "+
    "          classstr += 'definition-highlight'; "+
    "        classstr = classstr.replace(/^\\s+|\\s+$/g,''); "+
    "        elem.className = classstr;      }   }  ";
    defview.loadUrl("javascript:"+jsfunction);


    return fragment;
  }


  public void setPart(int part)
  {
    SharedPreferences prefs = getActivity().getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    String name = prefs.getString("title",getString(android.R.string.untitled));
    name = name.replaceAll("\\s+", "");
    defview.loadUrl("javascript:currentcall='"+name+"'");
    defview.loadUrl("javascript:setPart("+part+")");
  }
}
