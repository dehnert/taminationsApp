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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.Pair;

public class Dancer {

  static public final int BOY = 1;
  static public final int GIRL = 2;
  static public final int PHANTOM = 3;

  public float startx;
  public float starty;
  public float startangle;
  public int gender;
  public Matrix tx;
  public int hands;
  public String number;
  public boolean showNumber = false;
  public Dancer rightgrip;
  public Dancer leftgrip;
  public boolean hidden = false;
  public List<Movement> movelist;
  public List<Matrix> transformlist;
  public Dancer rightdancer;
  public Dancer leftdancer;
  public boolean rightHandVisibility;
  public boolean leftHandVisibility;
  public boolean rightHandNewVisibility;
  public boolean leftHandNewVisibility;
  public Path pathpath;
  public int fillColor;
  public int drawColor;

  public Dancer(String n, int g, int c, float x, float y, float angle, List<Movement> moves)
  {
    number = n;
    gender = g;
    fillColor = c;
    drawColor = Color.darker(c);
    startx = x;
    starty = y;
    startangle = angle;
    movelist = moves;
    // Calculate list of transforms to speed up animations
    transformlist = new ArrayList<Matrix>();
    Matrix tx = new Matrix();
    for (Movement m :  movelist) {
      Matrix tt = m.translate(999);
      tx.preConcat(tt);
      Matrix tr = m.rotate(999);
      tx.preConcat(tr);
      transformlist.add(new Matrix(tx));
    }
    //  Compute points of path for drawing path
    //  TODO this could use the Path.cubicTo method straight off the Beziers
    pathpath = new Path();
    pathpath.moveTo(startx,starty);
    for (float beat=0.1f; beat<beats(); beat+=0.1f) {
      animate(beat);
      Pair<Float,Float> loc = location();
      pathpath.lineTo(loc.first, loc.second);
    }
  }

  public boolean isPhantom()
  {
    return false;
  }

  public float beats()
  {
    float retval = 0f;
    for (Movement m : movelist)
      retval += m.beats;
    return retval;
  }

  public Pair<Float,Float> location()
  {
    float[] m = new float[9];
    tx.getValues(m);
    return new Pair<Float,Float>(m[Matrix.MTRANS_X],m[Matrix.MTRANS_Y]);
  }

  public void animate(float beat)
  {
    // Be sure to reset grips at start
    if (beat == 0)
      rightgrip = leftgrip = null;
    //  Start to build transform
    Matrix start = new Matrix();
    start.preTranslate(startx, starty);
    start.preRotate(startangle);
    tx = start;
    hands = Movement.BOTHHANDS;
    //  Apply all completed movements
    Movement m = null;
    for (int i=0; i<movelist.size(); i++) {
      m = movelist.get(i);
      if (beat >= m.beats) {
        tx = new Matrix(start);
        tx.preConcat(transformlist.get(i));
        beat -= movelist.get(i).beats;
        m = null;
      } else
        break;
    }
    //  Apply movement in progress
    if (m != null) {
      tx.preConcat(m.translate(beat));
      tx.preConcat(m.rotate(beat));
      if (beat >= 0)
        hands = m.hands;
      if ((m.hands & Movement.GRIPLEFT) == 0)
        leftgrip = null;
      if ((m.hands & Movement.GRIPRIGHT) == 0)
        rightgrip = null;
    } else  // End of path
      hands = Movement.BOTHHANDS;

  }

  public void draw(Canvas c) {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setStyle(Style.FILL);
    p.setColor(drawColor);
    c.drawCircle(0.5f,0f,0.33f,p);
    p.setColor(showNumber ? Color.WHITE : fillColor);
    if (gender == BOY)
      c.drawRect(-0.5f,-0.5f,.5f,.5f,p);
    else if (gender == GIRL)
      c.drawCircle(0f,0f,.5f,p);
    p.setStyle(Style.STROKE);
    p.setStrokeWidth(0.1f);
    p.setColor(drawColor);
    if (gender == BOY)
      c.drawRect(-0.5f,-0.5f,.5f,.5f,p);
    else if (gender == GIRL)
      c.drawCircle(0f,0f,.5f,p);
    if (showNumber) {
      float[] m = new float[9];
      tx.getValues(m);
      float angle = MathF.atan2(m[Matrix.MSKEW_X],m[Matrix.MSCALE_Y]);
      Matrix txtext = new Matrix();
      txtext.postRotate(-angle*180f/MathF.PI + 90f);
      txtext.postScale(1f, -1f);
      c.concat(txtext);
      p.setColor(Color.BLACK);
      p.setStyle(Style.FILL);
      float textSize = 0.7f;
      p.setTextSize(textSize);
      //  Text positioning seems to be confused by the transform
      //  These numbers were found by trial-and-error
      c.drawText(number, -textSize*.3f, textSize*.4f, p);
    }
  }

}
