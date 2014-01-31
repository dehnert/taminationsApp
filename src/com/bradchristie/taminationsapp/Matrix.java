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

public class Matrix extends android.graphics.Matrix
{
  public Matrix()
  {
    super();
  }

  public Matrix(Matrix from)
  {
    super(from);
  }

  public boolean preRotate(double radians)
  {
    return preRotate((float)(Math.toDegrees(radians)));
  }

  public boolean postRotate(double radians)
  {
    return postRotate((float)(Math.toDegrees(radians)));
  }

  public boolean preTranslate(double x, double y)
  {
    return preTranslate((float)x,(float)y);
  }

  public boolean postTranslate(double x, double y)
  {
    return postTranslate((float)x,(float)y);
  }

  public boolean postScale(double d, double e)
  {
    return postScale((float)d,(float)e);
  }



}
