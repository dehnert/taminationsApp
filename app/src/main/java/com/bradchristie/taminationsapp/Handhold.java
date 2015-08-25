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

import android.graphics.Matrix;
import android.support.annotation.NonNull;

public class Handhold implements Comparable<Handhold> {

  public final Dancer dancer1;
  public final Dancer dancer2;
  public final int hold1;
  public final int hold2;
  public final double angle1;
  public final double angle2;
  public final double distance;
  public final double score;

  public Handhold(Dancer d1, Dancer d2, int h1, int h2,
                  double a1, double a2, double d, double s)
  {
    dancer1 = d1;
    dancer2 = d2;
    hold1 = h1;
    hold2 = h2;
    angle1 = a1;
    angle2 = a2;
    distance = d;
    score = s;
  }

  public static Handhold getHandhold(Dancer d1, Dancer d2, int geometry)
  {
    if (d1.hidden || d2.hidden)
      return null;
    //  Turn off grips if not specified in current movement
    if ((d1.hands & Movement.GRIPRIGHT()) != Movement.GRIPRIGHT())
      d1.rightgrip = null;
    if ((d1.hands & Movement.GRIPLEFT()) != Movement.GRIPLEFT())
      d1.leftgrip = null;
    if ((d2.hands & Movement.GRIPRIGHT()) != Movement.GRIPRIGHT())
      d2.rightgrip = null;
    if ((d2.hands & Movement.GRIPLEFT()) != Movement.GRIPLEFT())
      d2.leftgrip = null;

    //  Check distance
    float[] t1 = new float[9];
    d1.tx.getValues(t1);
    double x1 = t1[Matrix.MTRANS_X];
    double y1 = t1[Matrix.MTRANS_Y];
    float[] t2 = new float[9];
    d2.tx.getValues(t2);
    double x2 = t2[Matrix.MTRANS_X];
    double y2 = t2[Matrix.MTRANS_Y];
    double dx = x2-x1;
    double dy = y2-y1;
    double dfactor1 = 0.1;  // for distance up to 2.0
    double dfactor2 = 2.0;  // for distance past 2.0
    double cutover = 2.0;
    if (geometry == Geometry.HEXAGON)
      cutover = 2.5;
    else if (geometry == Geometry.BIGON)
      cutover = 3.7;
    double d = Math.sqrt(dx*dx+dy*dy);
    double dfactor0 = geometry == Geometry.HEXAGON ? 1.15 : 1.0;
    double d0 = d*dfactor0;
    double score1 = d0 > cutover ? (d0-cutover)*dfactor2+2*dfactor1 : d0*dfactor1;
    double score2 = score1;
    //  Angle between dancers
    double a0 = Math.atan2(dy,dx);
    //  Angle each dancer is facing
    double a1 = Math.atan2(t1[Matrix.MSKEW_Y],t1[Matrix.MSCALE_Y]);
    double a2 = Math.atan2(t2[Matrix.MSKEW_Y],t2[Matrix.MSCALE_Y]);
    //  For each dancer, try left and right hands
    int h1 = 0;
    int h2 = 0;
    double ah1 = 0.0;
    double ah2 = 0.0;
    double afactor1 = 0.2;
    double afactor2 = 1.0;
    if (geometry == Geometry.BIGON)
      afactor2 = 0.6f;
    //  Dancer 1
    double a = Math.abs(Math.IEEEremainder(Math.abs(a1-a0+Math.PI*3.0/2.0),Math.PI*2.0));
    double ascore = a > Math.PI/6.0 ? (a-Math.PI/6.0)*afactor2+Math.PI/6.0*afactor1
                                   : a*afactor1;
    if (score1+ascore < 1.0 && (d1.hands & Movement.RIGHTHAND()) != 0 &&
        d1.rightgrip==null || d1.rightgrip==d2) {
      score1 = d1.rightgrip==d2 ? 0.0 : score1 + ascore;
      h1 = Movement.RIGHTHAND();
      ah1 = a1-a0+Math.PI*3.0/2.0;
    } else {
      a = Math.abs(Math.IEEEremainder(Math.abs(a1-a0+Math.PI/2.0),Math.PI*2.0));
      ascore = a > Math.PI/6.0 ? (a-Math.PI/6.0)*afactor2+Math.PI/6.0*afactor1
                               : a*afactor1;
      if (score1+ascore < 1.0 && (d1.hands & Movement.LEFTHAND()) != 0 &&
          d1.leftgrip==null || d1.leftgrip==d2) {
        score1 = d1.leftgrip==d2 ? 0.0 : score1 + ascore;
        h1 = Movement.LEFTHAND();
        ah1 = a1-a0+Math.PI/2.0;
      } else
        score1 = 10.0;
    }
    //  Dancer 2
    a = Math.abs(Math.IEEEremainder(Math.abs(a2-a0+Math.PI/2.0),Math.PI*2.0));
    ascore = a > Math.PI/6.0 ? (a-Math.PI/6.0)*afactor2+Math.PI/6.0*afactor1
                             : a*afactor1;
    if (score2+ascore < 1.0 && (d2.hands & Movement.RIGHTHAND()) != 0 &&
        d2.rightgrip==null || d2.rightgrip==d1) {
      score2 = d2.rightgrip==d1 ? 0.0 : score2 + ascore;
      h2 = Movement.RIGHTHAND();
      ah2 = a2-a0+Math.PI/2.0;
    } else {
      a = Math.abs(Math.IEEEremainder(Math.abs(a2-a0+Math.PI*3.0/2.0),Math.PI*2.0));
      ascore = a > Math.PI/6.0 ? (a-Math.PI/6.0)*afactor2+Math.PI/6.0*afactor1
                               : a*afactor1;
      if (score2+ascore < 1.0 && (d2.hands & Movement.LEFTHAND()) != 0 &&
          d2.leftgrip==null || d2.leftgrip==d1) {
        score2 = d2.leftgrip==d1 ? 0.0 : score2 + ascore;
        h2 = Movement.LEFTHAND();
        ah2 = a2-a0+Math.PI*3.0/2.0;
      } else
        score2 = 10f;
    }

    if (d1.rightgrip == d2 && d2.rightgrip == d1)
      return new Handhold(d1,d2,Movement.RIGHTHAND(),Movement.RIGHTHAND(),ah1,ah2,d,0.0);
    if (d1.rightgrip == d2 && d2.leftgrip == d1)
      return new Handhold(d1,d2,Movement.RIGHTHAND(),Movement.LEFTHAND(),ah1,ah2,d,0.0);
    if (d1.leftgrip == d2 && d2.rightgrip == d1)
      return new Handhold(d1,d2,Movement.LEFTHAND(),Movement.RIGHTHAND(),ah1,ah2,d,0.0);
    if (d1.leftgrip == d2 && d2.leftgrip == d1)
      return new Handhold(d1,d2,Movement.LEFTHAND(),Movement.LEFTHAND(),ah1,ah2,d,0.0);

    if (score1 > 1.0 || score2 > 1.0 || score1+score2 > 1.2)
      return null;
    return new Handhold(d1,d2,h1,h2,ah1,ah2,d,score1+score2);
  }

  @Override
  public int compareTo(@NonNull Handhold another)
  {
    return (int)(score*1000 - another.score*1000);
  }

  public boolean inCenter()
  {
    return dancer1.inCenter() && dancer2.inCenter();
  }

}
