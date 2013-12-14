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

//  Use the backward-compatibility library to support Android 2
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LevelFragment extends RotationFragment
{
  static public final boolean SHOWLEVELSONLY = true;
  private boolean noLevels = false;

  public LevelFragment()
  {
    super();
    noLevels = false;
  }

  public LevelFragment(boolean showLevelsOnly)
  {
    super();
    noLevels = true;
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    // Inflate the layout for this fragment
    View fragment = inflater.inflate(R.layout.fragment_level, container, false);
    if (noLevels) {
      fragment.findViewById(R.id.button_search).setVisibility(View.GONE);
      fragment.findViewById(R.id.button_about).setVisibility(View.GONE);
      fragment.findViewById(R.id.button_index).setVisibility(View.GONE);
    }
    return fragment;
  }

}
