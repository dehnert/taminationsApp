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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.bradchristie.taminationsapp.LevelActivity.LevelData;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefinitionActivity extends PortraitActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_definition);
    SharedPreferences prefs = getSharedPreferences("Taminations", MODE_PRIVATE);
    String link = prefs.getString("link",getString(android.R.string.untitled));
    String xmlname = link + ".xml";
    String level = xmlname.split("/")[0];
    Button levelButton = (Button)findViewById(R.id.button_level);
    levelButton.setText(LevelData.find(level).name);
    Document tamdoc = Tamination.getXMLAsset(this, xmlname);
    Element tamination = (Element)tamdoc.getElementsByTagName("tamination").item(0);
    String titlestr = tamination.getAttribute("title");
    setTitle(titlestr);
    WebView defview = (WebView)findViewById(R.id.definitionView);
    //  Turn on pinch-to-zoom, which is off(!) by default
    defview.getSettings().setBuiltInZoomControls(true);
    Tamination.loadDefinition(link,defview,this);
  }


  public void onLogoClicked(View view) {
    super.onLogoClicked(view);
  }

  public void onSpeakerClicked(View view) {
    super.onSpeakerClicked(view);
  }

  public void onLevelClicked(View view) {
    super.onLevelClicked(view);
  }
}
