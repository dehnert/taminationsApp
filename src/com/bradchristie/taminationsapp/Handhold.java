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

import android.graphics.Matrix;

public class Handhold implements Comparable<Handhold> {

  public Dancer dancer1;
  public Dancer dancer2;
  public int hold1;
  public int hold2;
  public float angle1;
  public float angle2;
  public float distance;
  public float score;

  public Handhold(Dancer d1, Dancer d2, int h1, int h2,
                  float a1, float a2, float d, float s)
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

  public static Handhold getHandhold(Dancer d1, Dancer d2)
  {
    if (d1.hidden || d2.hidden)
      return null;
    //  Turn off grips if not specified in current movement
    if ((d1.hands & Movement.GRIPRIGHT) != Movement.GRIPRIGHT)
      d1.rightgrip = null;
    if ((d1.hands & Movement.GRIPLEFT) != Movement.GRIPLEFT)
      d1.leftgrip = null;
    if ((d2.hands & Movement.GRIPRIGHT) != Movement.GRIPRIGHT)
      d2.rightgrip = null;
    if ((d2.hands & Movement.GRIPLEFT) != Movement.GRIPLEFT)
      d2.leftgrip = null;

    //  Check distance
    float[] t1 = new float[9];
    d1.tx.getValues(t1);
    float x1 = t1[Matrix.MTRANS_X];
    float y1 = t1[Matrix.MTRANS_Y];
    float[] t2 = new float[9];
    d2.tx.getValues(t2);
    float x2 = t2[Matrix.MTRANS_X];
    float y2 = t2[Matrix.MTRANS_Y];
    float dx = x2-x1;
    float dy = y2-y1;
    float dfactor1 = 0.1f;  // for distance up to 2.0
    float dfactor2 = 2.0f;  // for distance past 2.0
    float cutover = 2.0f;
    // TODO if (d1.tamsvg.hexagon)
    //  cutover = 2.5;
    //if (d1.tamsvg.bigon)
    //  cutover = 3.7;
    float d = MathF.sqrt(dx*dx+dy*dy);
    float dfactor0 =  /* this.hexagon ? 1.15 :  */ 1.0f;
    float d0 = d*dfactor0;
    float score1 = d0 > cutover ? (d0-cutover)*dfactor2+2*dfactor1 : d0*dfactor1;
    float score2 = score1;
    //  Angle between dancers
    float a0 = (float)Math.atan2(dy,dx);
    //  Angle each dancer is facing
    float a1 = (float)Math.atan2(t1[Matrix.MSKEW_Y],t1[Matrix.MSCALE_Y]);
    float a2 = (float)Math.atan2(t2[Matrix.MSKEW_Y],t2[Matrix.MSCALE_Y]);
    //  For each dancer, try left and right hands
    int h1 = 0;
    int h2 = 0;
    float ah1 = 0f;
    float ah2 = 0f;
    float afactor1 = 0.2f;
    float afactor2 = 1.0f;
    // TODO if (d1.tamsvg.bigon)
    //  afactor2 = 0.6;
    //  Dancer 1
    float a = MathF.abs(MathF.IEEEremainder(MathF.abs(a1-a0+MathF.PI*3f/2f),MathF.PI*2f));
    float ascore = a > MathF.PI/6f ? (a-MathF.PI/6f)*afactor2+MathF.PI/6f*afactor1
                                   : a*afactor1;
    if (score1+ascore < 1.0f && (d1.hands & Movement.RIGHTHAND) != 0 &&
        d1.rightgrip==null || d1.rightgrip==d2) {
      score1 = d1.rightgrip==d2 ? 0.0f : score1 + ascore;
      h1 = Movement.RIGHTHAND;
      ah1 = a1-a0+MathF.PI*3f/2f;
    } else {
      a = MathF.abs(MathF.IEEEremainder(MathF.abs(a1-a0+MathF.PI/2f),MathF.PI*2f));
      ascore = a > MathF.PI/6f ? (a-MathF.PI/6f)*afactor2+MathF.PI/6f*afactor1
                               : a*afactor1;
      if (score1+ascore < 1.0f && (d1.hands & Movement.LEFTHAND) != 0 &&
          d1.leftgrip==null || d1.leftgrip==d2) {
        score1 = d1.leftgrip==d2 ? 0.0f : score1 + ascore;
        h1 = Movement.LEFTHAND;
        ah1 = a1-a0+MathF.PI/2f;
      } else
        score1 = 10f;
    }
    //  Dancer 2
    a = MathF.abs(MathF.IEEEremainder(MathF.abs(a2-a0+MathF.PI/2f),MathF.PI*2f));
    ascore = a > MathF.PI/6f ? (a-MathF.PI/6f)*afactor2+MathF.PI/6f*afactor1
                             : a*afactor1;
    if (score2+ascore < 1.0f && (d2.hands & Movement.RIGHTHAND) != 0 &&
        d2.rightgrip==null || d2.rightgrip==d1) {
      score2 = d2.rightgrip==d1 ? 0.0f : score2 + ascore;
      h2 = Movement.RIGHTHAND;
      ah2 = a2-a0+MathF.PI/2f;
    } else {
      a = MathF.abs(MathF.IEEEremainder(MathF.abs(a2-a0+MathF.PI*3f/2f),MathF.PI*2f));
      ascore = a > MathF.PI/6f ? (a-MathF.PI/6f)*afactor2+MathF.PI/6f*afactor1
                               : a*afactor1;
      if (score2+ascore < 1.0f && (d2.hands & Movement.LEFTHAND) != 0 &&
          d2.leftgrip==null || d2.leftgrip==d1) {
        score2 = d2.leftgrip==d1 ? 0.0f : score2 + ascore;
        h2 = Movement.LEFTHAND;
        ah2 = a2-a0+MathF.PI*3f/2f;
      } else
        score2 = 10f;
    }

    if (d1.rightgrip == d2 && d2.rightgrip == d1)
      return new Handhold(d1,d2,Movement.RIGHTHAND,Movement.RIGHTHAND,ah1,ah2,d,0f);
    if (d1.rightgrip == d2 && d2.leftgrip == d1)
      return new Handhold(d1,d2,Movement.RIGHTHAND,Movement.LEFTHAND,ah1,ah2,d,0f);
    if (d1.leftgrip == d2 && d2.rightgrip == d1)
      return new Handhold(d1,d2,Movement.LEFTHAND,Movement.RIGHTHAND,ah1,ah2,d,0f);
    if (d1.leftgrip == d2 && d2.leftgrip == d1)
      return new Handhold(d1,d2,Movement.LEFTHAND,Movement.LEFTHAND,ah1,ah2,d,0f);

    if (score1 > 1.0f || score2 > 1.0f || score1+score2 > 1.2f)
      return null;
    return new Handhold(d1,d2,h1,h2,ah1,ah2,d,score1+score2);
  }

  @Override
  public int compareTo(Handhold another)
  {
    return (int)(score*1000 - another.score*1000);
  }

}
