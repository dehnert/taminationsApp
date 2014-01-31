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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 *     The About fragment loads content from an html file and displays it.
 *
 */
public class AboutFragment extends RotationFragment
{
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View fragment = inflater.inflate(R.layout.fragment_about, container, false);
    WebView defview = (WebView)fragment.findViewById(R.id.aboutWebView);
    defview.loadUrl("file:///android_asset/info/about.html");
    setTitle("Taminations");
    return fragment;
  }
}
