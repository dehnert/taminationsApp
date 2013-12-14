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

public class Quaternion
{
  public double x,y,z,w;

  private void init(double a, double b, double c, double d)
  {
    x = a;
    y = b;
    z = c;
    w = d;
  }

  public Quaternion(double a, double b, double c)
  {
    double dd = 1.0 - a*a - b*b - c*c;
    assert(dd > 0.0);
    init(a,b,c,dd > 0 ? Math.sqrt(dd) : 0);
  }

  public Quaternion(double a, double b, double c, double d)
  {
    init(a,b,c,d);
  }

  public Quaternion inverse()
  {
    return new Quaternion(-x,-y,-z,w);
  }

  public Quaternion postMultiply(Quaternion m)
  {
    return new Quaternion(w*m.x+x*m.w+y*m.z-z*m.y,
                          w*m.y-x*m.z+y*m.w+z*m.x,
                          w*m.z+x*m.y-y*m.x+z*m.w,
                          w*m.w-x*m.x-y*m.y-z*m.z);
  }

  public Quaternion postMultiply(Vector3D m)
  {
    return new Quaternion(w*m.x      +y*m.z-z*m.y,
                          w*m.y-x*m.z      +z*m.x,
                          w*m.z+x*m.y-y*m.x      ,
                               -x*m.x-y*m.y-z*m.z);
  }


  public String toString()
  {
    return String.format("%4.2f %4.2f %4.2f %4.2f",x,y,z,w);
  }

}
