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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.lang.reflect.Method;

@SuppressLint("SetJavaScriptEnabled")
public class DefinitionFragment extends RotationFragment
{

  private WebView defview;
  private RadioButton abbrevRadioButton;
  private RadioButton fullRadioButton;
  private View fragment;
  private SharedPreferences prefs;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    fragment = inflater.inflate(R.layout.fragment_definition, container, false);
    prefs = getActivity().getSharedPreferences("Taminations",Context.MODE_PRIVATE);
    defview = (WebView)fragment.findViewById(R.id.definitionView);
    //  Turn on pinch-to-zoom, which is off(!) by default
    defview.getSettings().setBuiltInZoomControls(true);
    defview.getSettings().setJavaScriptEnabled(true);

    abbrevRadioButton = (RadioButton)fragment.findViewById(R.id.definitionAbbrevRadioButton);
    abbrevRadioButton.setOnCheckedChangeListener(
        new CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              evaluateJavascript("setAbbrev(true)");
              prefs.edit().putBoolean("isabbrev",true).commit();
            }
          }
        });

    fullRadioButton = (RadioButton)fragment.findViewById(R.id.definitionFullRadioButton);
    fullRadioButton.setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                  evaluateJavascript("setAbbrev(false)");
                  prefs.edit().putBoolean("isabbrev", false).commit();
                }
              }
            });
    setDefinition(intentString("link"));
    return fragment;
  }

  public void setDefinition(String link)
  {
    //  Check for non-English definition
    Tamination.loadDefinition(link,defview,getActivity());
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
    evaluateJavascript(jsfunction);

    //  Show abbrev/full buttons only for Basic and Mainstream
    View dgv =  fragment.findViewById(R.id.definitionGroup);
    if (link.matches("(b1|b2|ms).*")) {
      dgv.setVisibility(View.VISIBLE);
      boolean isAbbrev = prefs.getBoolean("isabbrev",true);
      abbrevRadioButton.setChecked(isAbbrev);
      fullRadioButton.setChecked(!isAbbrev);
      //  Function to show either full or abbrev
      //  We need to wait until the page loading is finished
      //  before injecting this
      final String jsfunction2 =
      "    function setAbbrev(isAbbrev) {" +
      "      var nodes = document.getElementsByTagName('*');" +
      "      for (var i=0; i<nodes.length; i++) {" +
      "        var elem = nodes.item(i);" +
      "        if (elem.className.indexOf('abbrev') >= 0)" +
      "          elem.style.display = isAbbrev ? '' : 'none';" +
      "        if (elem.className.indexOf('full') >= 0)" +
      "          elem.style.display = isAbbrev ? 'none' : '';" +
      "      }" +
      "    } " +
         (isAbbrev ? "setAbbrev(true)" : "setAbbrev(false)");
      //  Once the web page is loaded, inject the function to switch
      //  abbrev/full and set the current state
      defview.setWebViewClient(new WebViewClient() {
        public void onPageFinished(WebView v, String u) {
          evaluateJavascript(jsfunction2);
        }
      });


    }
    else  //  Not a BMS def, so don't show radio buttons
      dgv.setVisibility(View.GONE);
  }

  //  This is called as the animation progresses through
  //  different parts of the call
  public void setPart(int part)
  {
    String name = intentString("title");
    if (name != null) {
      name = name.replaceAll("\\s+", "");
      evaluateJavascript("currentcall='" + name + "'");
      evaluateJavascript("setPart(" + part + ")");
    }
  }

  private void evaluateJavascript(String script)
  {
    try {
      //  WebView.evaluateJavascript method available starting with KitKat
      Method m = defview.getClass().getMethod("evaluateJavascript", String.class, ValueCallback.class);
      m.invoke(defview,script,null);
    } catch (Exception e) {
      //  fall back to loadUrl which usually works
      defview.loadUrl("javascript:"+script);
    }

  }

}
