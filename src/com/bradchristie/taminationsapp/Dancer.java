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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

public class Dancer implements Comparable<Dancer> {

  static public final int BOY = 1;
  static public final int GIRL = 2;
  static public final int PHANTOM = 3;
  static public final int NUMBERS_OFF = 0;
  static public final int NUMBERS_DANCERS = 1;
  static public final int NUMBERS_COUPLES = 2;
  static private final RectF rect = new RectF(-0.5f, -0.5f, .5f, .5f);

  public Matrix tx;
  public int hands;
  public int showNumber = NUMBERS_OFF;
  public Dancer rightgrip;
  public Dancer leftgrip;
  public boolean hidden = false;
  public boolean showPath = false;
  public Dancer rightdancer;
  public Dancer leftdancer;
  public boolean rightHandVisibility;
  public boolean leftHandVisibility;
  public boolean rightHandNewVisibility;
  public boolean leftHandNewVisibility;

  private int gender;
  public String number;
  private String number_couple;
  private Geometry geom;
  protected Matrix starttx;
  private List<Movement> movelist;
  private List<Matrix> transformlist;
  private Path pathpath;
  protected int fillColor;
  protected int drawColor;

  /**
   *     Constructor for a new dancer
   * @param n    Number to show when Number display is on
   * @param nc   Number to show when Couples Number display is on
   * @param g    Gender - boy, girl, phantom
   * @param c    Base color
   * @param mat  Transform for dancer's start position
   * @param geometry  Square, Bigon, Hexagon
   * @param moves   List of Movements for dancer's path
   */
  public Dancer(String n, String nc, int g, int c, Matrix mat,
      Geometry geometry, List<Movement> moves) {
    //  Save all the parameters
    number = n;
    number_couple = nc;
    gender = g;
    fillColor = c;
    drawColor = Color.darker(c);
    starttx = geometry.startMatrix(mat);
    geom = geometry.clone();
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
    animate(0.0);
    Pair<Float, Float> loc = location();
    pathpath.moveTo(loc.first, loc.second);
    for (double beat = 0.1; beat < beats(); beat += 0.1) {
      animate(beat);
      loc = location();
      pathpath.lineTo(loc.first, loc.second);
    }
    //  Restore dancer to start position
    animate(-2.0);
  }

  public boolean isPhantom() {
    return gender == PHANTOM;
  }

  /**
   * @return  Total number of beats used by dancer's path
   */
  public float beats() {
    float retval = 0f;
    for (Movement m : movelist)
      retval += m.beats;
    return retval;
  }

  /**
   *   Get dancer's location, presumably after animate has been called
   * @return  (x,y) location
   */
  public Pair<Float, Float> location() {
    float[] m = new float[9];
    tx.getValues(m);
    return new Pair<Float, Float>(m[Matrix.MTRANS_X], m[Matrix.MTRANS_Y]);
  }

  /**
   *   Used for hexagon handholds
   * @return  True if dancer is close enough to center to make a center star
   */
  public boolean inCenter()
  {
    Pair<Float,Float> loc = location();
    return Math.sqrt(loc.first*loc.first + loc.second*loc.second) < 1.1;
  }

  /**
   *   Move dancer to location along path
   * @param beat
   */
  public void animate(double beat) {
    double beatin = beat;
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
    tx.postConcat(geom.pathMatrix(this.starttx,this.tx,beatin));
  }

  /**
   *   Draw the entire dancer's path as a translucent colored line
   * @param c  Canvas to draw to
   */
  public void drawPath(Canvas c)
  {
    Paint ppath = new Paint();
    //  The path color is a partly transparent version of the draw color
    ppath.setColor(drawColor & 0x50ffffff);
    ppath.setStyle(Style.STROKE);
    ppath.setStrokeWidth(0.1f);
    c.drawPath(pathpath,ppath);
  }

  /**
   *   Draw the dancer at its current location
   * @param c  Canvas to draw to
   */
  public void draw(Canvas c) {
    //  On Android, anti-alias smoothing is not the default
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    //  Draw the head
    p.setStyle(Style.FILL);
    p.setColor(drawColor);
    c.drawCircle(0.5f, 0f, 0.33f, p);
    //  Draw the body
    p.setColor(showNumber == NUMBERS_OFF ? fillColor : Color
        .veryBright(fillColor));
    if (gender == BOY)
      c.drawRect(rect, p);
    else if (gender == GIRL)
      c.drawCircle(0f, 0f, .5f, p);
    else if (gender == PHANTOM)
      c.drawRoundRect(rect, 0.3f, 0.3f, p);
    //  Draw the body outline
    p.setStyle(Style.STROKE);
    p.setStrokeWidth(0.1f);
    p.setColor(drawColor);
    if (gender == BOY)
      c.drawRect(rect, p);
    else if (gender == GIRL)
      c.drawCircle(0f, 0f, .5f, p);
    else if (gender == PHANTOM)
      c.drawRoundRect(rect, 0.3f, 0.3f, p);
    //  Draw number if on
    if (showNumber != NUMBERS_OFF) {
      //  The dancer is rotated relative to the display, but of course
      //  the dancer number should not be rotated.
      //  So the number needs to be transformed back
      float[] m = new float[9];
      tx.getValues(m);
      double angle = Math.atan2(m[Matrix.MSKEW_X], m[Matrix.MSCALE_Y]);
      Matrix txtext = new Matrix();
      txtext.postRotate(-angle+Math.PI/2);
      txtext.postScale(1, -1);
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

  @Override
  public int compareTo(Dancer d)
  {
    return number.compareTo(d.number);
  }

}
