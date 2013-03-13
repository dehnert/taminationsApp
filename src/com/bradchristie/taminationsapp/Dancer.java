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
import android.graphics.RectF;
import android.util.Pair;

public class Dancer {

  static public final int BOY = 1;
  static public final int GIRL = 2;
  static public final int PHANTOM = 3;
  static public final int NUMBERS_OFF = 0;
  static public final int NUMBERS_DANCERS = 1;
  static public final int NUMBERS_COUPLES = 2;
  static private final RectF rect = new RectF(-0.5f, -0.5f, .5f, .5f);

  public Geometry geom;
  public Matrix starttx;
  public int gender;
  public Matrix tx;
  public int hands;
  public String number;
  public String number_couple;
  public int showNumber = NUMBERS_OFF;
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

  public Dancer(String n, String nc, int g, int c, Matrix mat,
      Geometry geometry, List<Movement> moves) {
    number = n;
    number_couple = nc;
    gender = g;
    fillColor = c;
    drawColor = Color.darker(c);
    starttx = geometry.startMatrix(mat);
    geom = geometry;
    movelist = moves;
    // Calculate list of transforms to speed up animations
    transformlist = new ArrayList<Matrix>();
    Matrix tx = new Matrix();
    for (Movement m : movelist) {
      Matrix tt = m.translate(999);
      tx.preConcat(tt);
      Matrix tr = m.rotate(999);
      tx.preConcat(tr);
      transformlist.add(new Matrix(tx));
    }
    // Compute points of path for drawing path
    pathpath = new Path();
    animate(0.0f);
    Pair<Float, Float> loc = location();
    pathpath.moveTo(loc.first, loc.second);
    for (float beat = 0.1f; beat < beats(); beat += 0.1f) {
      animate(beat);
      loc = location();
      pathpath.lineTo(loc.first, loc.second);
    }
    animate(-2.0f);
  }

  public boolean isPhantom() {
    return gender == PHANTOM;
  }

  public float beats() {
    float retval = 0f;
    for (Movement m : movelist)
      retval += m.beats;
    return retval;
  }

  public Pair<Float, Float> location() {
    float[] m = new float[9];
    tx.getValues(m);
    return new Pair<Float, Float>(m[Matrix.MTRANS_X], m[Matrix.MTRANS_Y]);
  }

  public boolean inCenter()
  {
    Pair<Float,Float> loc = location();
    return MathF.sqrt(loc.first*loc.first + loc.second*loc.second) < 1.1f;
  }

  public void animate(float beat) {
    float beatin = beat;
    // Be sure to reset grips at start
    if (beat == 0)
      rightgrip = leftgrip = null;
    // Start to build transform
    tx = new Matrix(starttx);
    hands = Movement.BOTHHANDS;
    // Apply all completed movements
    Movement m = null;
    for (int i = 0; i < movelist.size(); i++) {
      m = movelist.get(i);
      if (beat >= m.beats) {
        tx = new Matrix(starttx);
        tx.preConcat(transformlist.get(i));
        beat -= movelist.get(i).beats;
        m = null;
      } else
        break;
    }
    // Apply movement in progress
    if (m != null) {
      tx.preConcat(m.translate(beat));
      tx.preConcat(m.rotate(beat));
      if (beat >= 0)
        hands = m.hands;
      if ((m.hands & Movement.GRIPLEFT) == 0)
        leftgrip = null;
      if ((m.hands & Movement.GRIPRIGHT) == 0)
        rightgrip = null;
    } else
      // End of path
      hands = Movement.BOTHHANDS;
    // Modification for any special geometry
    tx.postConcat(geom.pathMatrix(this,beatin));
  }

  public void draw(Canvas c) {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setStyle(Style.FILL);
    p.setColor(drawColor);
    c.drawCircle(0.5f, 0f, 0.33f, p);
    p.setColor(showNumber == NUMBERS_OFF ? fillColor : Color
        .veryBright(fillColor));
    if (gender == BOY)
      c.drawRect(rect, p);
    else if (gender == GIRL)
      c.drawCircle(0f, 0f, .5f, p);
    else if (gender == PHANTOM)
      c.drawRoundRect(rect, 0.3f, 0.3f, p);
    p.setStyle(Style.STROKE);
    p.setStrokeWidth(0.1f);
    p.setColor(drawColor);
    if (gender == BOY)
      c.drawRect(rect, p);
    else if (gender == GIRL)
      c.drawCircle(0f, 0f, .5f, p);
    else if (gender == PHANTOM)
      c.drawRoundRect(rect, 0.3f, 0.3f, p);
    if (showNumber != NUMBERS_OFF) {
      float[] m = new float[9];
      tx.getValues(m);
      float angle = MathF.atan2(m[Matrix.MSKEW_X], m[Matrix.MSCALE_Y]);
      Matrix txtext = new Matrix();
      txtext.postRotate(-angle * 180f / MathF.PI + 90f);
      txtext.postScale(1f, -1f);
      c.concat(txtext);
      p.setColor(Color.BLACK);
      p.setStyle(Style.FILL);
      float textSize = 0.7f;
      p.setTextSize(textSize);
      // Text positioning seems to be confused by the transform
      // These numbers were found by trial-and-error
      c.drawText(showNumber == NUMBERS_DANCERS ? number : number_couple,
          -textSize * .3f, textSize * .4f, p);
    }
  }

}
