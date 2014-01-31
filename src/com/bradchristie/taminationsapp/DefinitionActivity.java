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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

public class DefinitionActivity extends PortraitActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_definition);
    SharedPreferences prefs = getSharedPreferences("Taminations",MODE_PRIVATE);
    String name = prefs.getString("link",getString(android.R.string.untitled));
    String xmlname = prefs.getString("link", getString(android.R.string.untitled))
        .replace("html", "xml");
    String level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(LevelData.find(level).name);
    String titlestr = prefs.getString("call",getString(android.R.string.untitled));
    setTitle(titlestr);
    WebView defview = (WebView)findViewById(R.id.definitionView);
    //  Turn on pinch-to-zoom, which is off(!) by default
    defview.getSettings().setBuiltInZoomControls(true);
    defview.loadUrl("file:///android_asset/" + name);
  }


}
