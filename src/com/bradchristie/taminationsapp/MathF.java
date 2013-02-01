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

import android.util.FloatMath;

public class MathF
{
  public static final float PI = 3.141592654f;

  // no instantiation
  private MathF()
  {  }

  public static float abs(float a)
  {
    return Math.abs(a);
  }

  public static float atan2(float y, float x)
  {
    return (float)Math.atan2(y,x);
  }

  public static float sqrt(float a)
  {
    return FloatMath.sqrt(a);
  }

  public static float sin(float a)
  {
    return FloatMath.sin(a);
  }

  public static float cos(float a)
  {
    return FloatMath.cos(a);
  }

  public static float IEEEremainder(float a, float b)
  {
    return (float)Math.IEEEremainder(a,b);
  }
}
