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

public class Bezier {

  private double x1;
  private double y1;
  private double ctrlx1;
  private double ctrly1;
  private double ctrlx2;
  private double ctrly2;
  private double x2;
  private double y2;
  private double ax;
  private double bx;
  private double cx;
  private double ay;
  private double by;
  private double cy;

  Bezier(double x1, double y1, double ctrlx1, double ctrly1,
         double ctrlx2, double ctrly2, double x2, double y2)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.ctrlx1 = ctrlx1;
    this.ctrly1 = ctrly1;
    this.ctrlx2 = ctrlx2;
    this.ctrly2 = ctrly2;
    this.x2 = x2;
    this.y2 = y2;
    this.calculatecoefficients();
  }

  private void calculatecoefficients()
  {
    cx = 3.0*(ctrlx1-x1);
    bx = 3.0*(ctrlx2-ctrlx1) - cx;
    ax = x2 - x1 - cx - bx;

    cy = 3.0*(ctrly1-y1);
    by = 3.0*(ctrly2-ctrly1) - cy;
    ay = y2 - y1 - cy - by;
  }

  //  Return the movement along the curve given "t" between 0 and 1
  public Matrix translate(double t)
  {
    double x = x1 + t*(cx + t*(bx + t*ax));
    double y = y1 + t*(cy + t*(by + t*ay));
    Matrix retval = new Matrix();
    retval.postTranslate(x,y);
    return retval;
  }

  //  Return the angle of the derivative given "t" between 0 and 1
  public Matrix rotate(double t)
  {

    double x = cx + t*(2.0*bx + t*3.0*ax);
    double y = cy + t*(2.0*by + t*3.0*ay);
    double theta = Math.atan2(y,x);
    Matrix retval = new Matrix();
    retval.postRotate(theta);
    return retval;
  };

}
