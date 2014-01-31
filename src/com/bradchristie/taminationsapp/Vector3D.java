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

public class Vector3D
{
  public double x;
  public double y;
  public double z;

  public Vector3D(double x_, double y_, double z_)
  {
    x = x_;
    y = y_;
    z = z_;
  }

  public Quaternion postMultiply(Quaternion m)
  {
    return new Quaternion(x*m.w+y*m.z-z*m.y,
                         -x*m.z+y*m.w+z*m.x,
                          x*m.y-y*m.x+z*m.w,
                         -x*m.x-y*m.y-z*m.z);
  }

  public Vector3D rotate(Quaternion qrot)
  {
    Quaternion q = qrot.postMultiply(this).postMultiply(qrot.inverse());
    return new Vector3D(q.x,q.y,q.z);
  }

  public double length()
  {
    return Math.sqrt(x*x+y*y+z*z);
  }

}
