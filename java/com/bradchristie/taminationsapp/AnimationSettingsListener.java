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

public interface AnimationSettingsListener
{
  int SPEED_SETTING_CHANGED = 1;
  int LOOP_SETTING_CHANGED = 2;
  int GRID_SETTING_CHANGED = 3;
  int PATHS_SETTING_CHANGED = 4;
  int NUMBERS_SETTING_CHANGED = 5;
  int PHANTOMS_SETTING_CHANGED = 6;
  int GEOMETRY_SETTING_CHANGED = 7;

  void settingsChanged(int setting);
}
