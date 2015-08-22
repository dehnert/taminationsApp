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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AnimationView extends SurfaceView
                           implements SurfaceHolder.Callback,
                                      OnTouchListener,
                                      Runnable
{
  private static final double SLOWSPEED =     1500.0;
  private static final double MODERATESPEED = 1000.0;
  private static final double NORMALSPEED =    500.0;
  private static final double FASTSPEED =      200.0;
  /** Indicate if the animation is running */
  private boolean isRunning = false;
  /** Indicate if the grid should be drawn */
  private boolean showGrid = false;
  /** Indicate if dancer paths should be drawn */
  private boolean showPaths = false;
  /** Indicate if the animation should repeat when the end is reached */
  private boolean loop = false;
  /** Special geometries */
  private int geometry = Geometry.SQUARE;
  /** Indicate if phantoms should be drawn */
  private boolean showPhantoms = false;
  /** Array of dancers in the current animation */
  private Dancer[] dancers;
  /** Total length of the animation */
  private double beats = 0f;
  private double speed = 500f;
  private AnimationListener listener = null;
  private boolean dirty = false;
  /** Parts string from formation, for passing to tic labeler  */
  private String parts;
  private double[] partbeats;
  private int currentpart;
  private InteractiveDancer idancer = null;
  private double iscore;
  private Thread thread;
  /**  The intrinsic lock is used by Android for some View methods.
   *   So as not to interfere with that, we will use a dedicated lock object.   */
  private final Object lock = new Object();

  /** Handle to the surface manager object we interact with */
  private SurfaceHolder surface = null;

  /** Used to figure out elapsed time between frames */
  private long mLastTime;
  /** Current location in the animation */
  private double beat = -2.0;
  /** Previous location  */
  private double prevbeat = -2.0;
  /** Beats to wait before starting animation  */
  private double leadin = 2.0;
  /** Beats to add to end of animation  */
  private double leadout = 2.0;
  private Element tam;
  private int interactiveDancer = -1;

  /**
   *   Starts the animation
   */
  public void doStart()
  {
    synchronized (lock) {
      mLastTime = System.currentTimeMillis();
      if (beat > beats)
        beat = -leadin;
      isRunning = true;
      iscore = 0;
      dirtify();
    }
  }

  /**
   * Pauses the dancers update & animation.
   */
  public void doPause()
  {
    //  This is called when the user hits the Back button, locking
    //  causes a deadlock
    //synchronized (lock) {
      isRunning = false;
    //}
  }

  /**
   *  Rewinds to the start of the animation, even if it is running
   */
  public void doRewind()
  {
    synchronized (lock) {
      beat = -leadin;
      dirtify();
    }
  }

  /**
   *   Moves the animation back a little
   */
  public void doBackup()
  {
    synchronized (lock) {
      beat = Math.max(beat-0.1, -leadin);
      dirtify();
    }
  }

  /**
   *   Moves the animation forward a little
   */
  public void doForward()
  {
    synchronized (lock) {
      beat = Math.min(beat+0.1, beats);
      dirtify();
    }
  }

  /**
   *   Build an array of floats out of the parts of the animation
   */
  private double[] getPartsValues()
  {
    double[] retval = { -2.0f, 0.0f, beats-2.0f, beats };
    if (parts.length() > 0) {
      String[] t = parts.split(";");
      retval = new double[t.length+4];
      retval[0] = -2.0;
      retval[1] = 0.0;
      double b = 0.0;
      for (int i=0; i<t.length; i++) {
        b += Double.valueOf(t[i]);
        retval[i+2] = b;
      }
      retval[t.length+2] = beats - 2.0;
      retval[t.length+3] = beats;
    }
    return retval;
  }

  /**
   *   Moves the animation to the next part
   */
  public void doNextPart()
  {
    synchronized (lock) {
      double[] p = getPartsValues();
      for (double x:p) {
        if (x > beat) {
          beat = x;
          break;
        }
      }
      dirtify();
    }
  }

  /**
   *   Moves the animation to the previous part
   */
  public void doPrevPart()
  {
    synchronized (lock) {
      double[] p = getPartsValues();
      for (int i=p.length-1; i>=0; i--) {
        if (p[i] < beat) {
          beat = p[i];
          break;
        }
      }
      dirtify();
    }
  }

  /**
   *   Moves to the end of the animation, minus leadout
   */
  public void doEnd()
  {
    synchronized (lock) {
      beat = beats;
      dirtify();
    }
  }

  /**  Tells caller if the animation is running
   *
   */
  public boolean running()
  {
    synchronized (lock) {
      return isRunning;
    }
  }

  /**
   *   Set the visibility of the grid
   */
  public void setGridVisibility(boolean show)
  {
    synchronized (lock) {
      showGrid = show;
      dirtify();
    }
  }

  /**
   *   Set the visibility of phantom dancers
   */
  public void setPhantomVisibility(boolean show)
  {
    synchronized (lock) {
      showPhantoms = show;
      if (dancers != null) {
        for (Dancer d : dancers) {
          d.hidden = d.isPhantom() && !show;
        }
      }
    }
  }

  /**
   *  Turn on drawing of dancer paths
   */
  public void setPathVisibility(boolean show)
  {
    synchronized (lock) {
      showPaths = show;
      dirtify();
    }
  }

  public void setInteractiveDancerPathVisibility(boolean show)
  {
    synchronized (lock) {
      if (idancer != null) {
        idancer.showPath = show;
        dirtify();
      }
    }
  }

  /**
   *   Set animation looping
   */
  public void setLoop(boolean loopit)
  {
    synchronized (lock) {
      loop = loopit;
    }
  }

  /**
   *   Set display of dancer numbers
   */
  public void setNumbers(int numberem)
  {
    synchronized (lock) {
      //  For now at least, no numbers for practice
      if (idancer != null)
        numberem = Dancer.NUMBERS_OFF;
      if (dancers != null) {
        for (Dancer d : dancers)
          d.showNumber = numberem;
        dirtify();
      }
    }
  }

  /**
   *   Set speed of animation
   */
  public void setSpeed(String myspeed)
  {
    synchronized (lock) {
      switch (myspeed) {
        case "Slow":
          speed = SLOWSPEED;
          break;
        case "Moderate":
          speed = MODERATESPEED;
          break;
        case "Fast":
          speed = FASTSPEED;
          break;
        default:
          speed = NORMALSPEED;  // default normal speed
          break;
      }
    }
  }

  /**  Set hexagon geometry  */
  public void setHexagon()
  {
    synchronized (lock) {
      geometry = Geometry.HEXAGON;
    }
  }

  /**  Set bigon geometry  */
  public void setBigon()
  {
    synchronized (lock) {
      geometry = Geometry.BIGON;
    }
  }
  public void setSquare()
  {
    synchronized (lock) {
      geometry = Geometry.SQUARE;
    }
  }


  public double getTotalBeats()
  {
    synchronized (lock) {
      return leadin+beats;
    }
  }
  public double getBeats()
  {
    synchronized (lock) {
      return beats-leadout;
    }
  }

  public double getScore()
  {
    synchronized (lock) {
      return iscore;
    }
  }

  /**
   *  Return animation parts, defined in formation xml
   */
  public String getParts()
  {
    synchronized (lock) {
      return parts;
    }
  }

  /**
   *   Set location of animation
   */
  public void setLocation(double loc)
  {
    synchronized (lock) {
      beat = loc - 2.0;
      dirtify();
    }
  }

  /**
   *   Process a touch on the surface
   *   Toggles path display if touched on a dancer
   * @param x  Screen coord
   * @param y  Screen coord
   */
  public void doTouch(double x, double y)
  {
    synchronized (lock) {
      //  Convert x and y to dance floor coords
      if (surface != null) {  // sanity check
        Rect r = surface.getSurfaceFrame();
        double range = Math.min(r.width(), r.height());
        double s = range/13.0;
        double dx = -(y-r.height()/2.0)/s;
        double dy = -(x-r.width()/2.0)/s;
        //  Compare with dancer locations
        Dancer bestd = null;
        double bestdist = 0.5;
        for (Dancer d : dancers) {
          Pair<Float,Float> loc = d.location();
          double distsq = (loc.first-dx)*(loc.first-dx) + (loc.second-dy)*(loc.second-dy);
          if (distsq < bestdist) {
            bestd = d;
            bestdist = distsq;
          }
        }
        if (bestd != null) {
          bestd.showPath = !bestd.showPath;
          dirtify();
        }
      }
    }
  }

  /**
   *   Called when a change is made that affects the display.
   *   Tell the display to redraw even if the animation is not running.
   */
  public void dirtify()
  {
    synchronized (lock) {
      dirty = true;
      lock.notify();
    }
  }

  /**
   *   Method to run in a separate thread.
   *   If an animation is running, it updates the dancer positions
   *   and draws them.
   */
  @Override
  public void run() {
    if (listener != null)
      listener.onAnimationChanged(AnimationListener.ANIMATION_READY,0,0,0);
    while (surface != null) {
      synchronized (lock) {
        if (dirty || isRunning) {
          updateDancers();
          doDraw();
        }
        if (listener != null)
          listener.onAnimationChanged(AnimationListener.ANIMATION_PROGRESS,
              (beat+2.0)/(beats+2.0),beat,0);
        if (!isRunning)
          //  animation is not running, so don't chew up the CPU
          try {
            lock.wait();
          } catch (InterruptedException e) {
            // ignore spurious wakeups, only causes a redraw
          }
      }
    }
  }

  /**
   * Draws the dancers and background
   */
  private void doDraw() {
    Canvas c = null;
    try {
      if (surface != null)
        c = surface.lockCanvas();
      if (c == null)
        return;  // sanity check
      //  Draw background
      ColorDrawable cd = new ColorDrawable(0xfffff0e0);
      Rect candim = c.getClipBounds();
      cd.setBounds(candim);
      cd.draw(c);
      float range = Math.min(candim.width(),candim.height());
      //  Note Loop and dancer speed
      Paint p = new Paint();
      p.setColor(Color.BLACK);
      p.setTextSize(range/15.0f);
      String infostr = "";
      if (speed == SLOWSPEED)
        infostr = "Slow ";
      else if (speed == FASTSPEED)
        infostr = "Fast ";
      if (loop)
        infostr += "Loop";
      c.drawText(infostr, 10.0f, candim.height()-20.0f, p);
      //  For interactive leadin, show countdown
      if (idancer != null && beat < 0.0) {
        String tminus = ""+(int)Math.floor(beat);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(range/2.0f);
        p.setColor(Color.GRAY);
        c.drawText(tminus, range/2.0f, range, p);
      }
      //  Scale coordinate system to dancer's size
      c.translate(candim.width()/2,candim.height()/2);
      float s = range/13.0f;
      //  Flip and rotate
      c.scale(s,-s);
      c.rotate(90f);
      //  Draw grid if on
      if (showGrid)
        Geometry.drawGrid(geometry,c);
      //  Always show bigon center mark
      if (geometry == Geometry.BIGON) {
        Paint pline = new Paint();
        pline.setARGB(255,0,0,0);
        pline.setStyle(Style.STROKE);
        pline.setStrokeWidth(0f);
        c.drawLine(0.0f,-0.5f,0.0f,0.5f,pline);
        c.drawLine(-0.5f,0.0f,0.5f,0.0f,pline);
      }
      if (tam == null)
        return;

      //  Draw paths if requested
      for (Dancer d: dancers) {
        if (!d.hidden && (showPaths || d.showPath))
          d.drawPath(c);
      }

      //  Draw handholds
      Paint hline = new Paint();
      hline.setColor(Color.ORANGE);
      hline.setStrokeWidth(0.05f);
      hline.setStyle(Style.FILL);
      for (Dancer d: dancers) {
        Pair<Float,Float> loc = d.location();
        if (d.rightHandVisibility) {
          if (d.rightdancer == null) {  // hexagon center
            c.drawLine(loc.first,loc.second,0.0f,0.0f,hline);
            c.drawCircle(0.0f,0.0f,0.125f,hline);
          }
          else if (d.rightdancer.compareTo(d) < 0) {
            Pair<Float,Float> loc2 = d.rightdancer.location();
            c.drawLine(loc.first, loc.second, loc2.first, loc2.second, hline);
            c.drawCircle((loc.first+loc2.first)/2f,
                (loc.second+loc2.second)/2f,.125f,hline);
          }
        }
        if (d.leftHandVisibility) {
          if (d.leftdancer == null) {  // hexagon center
            c.drawLine(loc.first,loc.second,0.0f,0.0f,hline);
            c.drawCircle(0.0f,0.0f,0.125f,hline);
          }
          else if (d.leftdancer.compareTo(d) < 0) {
            Pair<Float,Float> loc2 = d.leftdancer.location();
            c.drawLine(loc.first, loc.second, loc2.first, loc2.second, hline);
            c.drawCircle((loc.first+loc2.first)/2f,
                (loc.second+loc2.second)/2f,.125f,hline);
          }
        }
      }
      //  Draw dancers
      for (Dancer d : dancers) {
        if (!d.hidden) {
          c.save();
          c.concat(d.tx);
          d.draw(c);
          c.restore();
        }
      }
      dirty = false;
    } finally {
      // do this in a finally so that if an exception is thrown
      // during the above, we don't leave the Surface in an
      // inconsistent state
      if (c != null)
        surface.unlockCanvasAndPost(c);
    }
  }

  private boolean isInteractiveDancerOnTrack()
  {
    //  Get where the dancer should be
    Matrix computetx = idancer.computeMatrix(beat);
    //  Get computed and actual location vectors
    Vector3D ivu = idancer.tx.location();
    Vector3D ivc = computetx.location();

    //  Check dancer's facing direction
    double au = idancer.tx.angle();
    double ac = computetx.angle();
    if (Math.abs(Vector3D.angleDiff(au,ac)) > Math.PI/4)
      return false;

    //  Check relationship with the other dancers
    for (Dancer d: dancers) {
      if (d != idancer) {
        Vector3D dv = d.tx.location();
        //  Compare angle to computed vs actual
        Vector3D d2ivu = dv.vectorTo(ivu);
        Vector3D d2ivc = dv.vectorTo(ivc);
        double a = d2ivu.angleDiff(d2ivc);
        if (Math.abs(a) > Math.PI/4) // 45 degrees  TODO parameterize?
          return false;
      }
    }
    return true;
  }

  /**
   * Updates dancers positions based on the passage of realtime.
   * Called at the start of draw().
   */
  private void updateDancers() {
    if (tam == null)
      return;
    //  Update the animation time
    long now = System.currentTimeMillis();
    long diff = now - mLastTime;
    if (isRunning)
      beat += (float)diff/speed;
    mLastTime = now;
    //  Move dancers
    //  For big jumps, move incrementally -
    //  this helps hexagon and bigon compute the right location
    double delta = beat - prevbeat;
    int incs = (int)Math.ceil(Math.abs(delta));
    for (int j=1; j<=incs; j++) {
      for (Dancer dancer : dancers)
        dancer.animate(prevbeat + j * delta / incs);
    }
    //  Find the current part, and send a message if it's changed
    int thispart = 0;
    for (int i=0; i<partbeats.length; i++) {
      if (partbeats[i] < beat)
        thispart = i;
    }
    if (beat < 0 || beat > beats)
      thispart = 0;
    if (thispart != currentpart) {
      currentpart = thispart;
      if (listener != null)
        listener.onAnimationChanged(AnimationListener.ANIMATION_PART,currentpart,beats,0);
    }

    //  Compute handholds
    ArrayList<Handhold> hhlist = new ArrayList<>();
    for (Dancer d0 : dancers) {
      d0.rightdancer = d0.leftdancer = null;
      d0.rightHandNewVisibility = false;
      d0.leftHandNewVisibility = false;
    }
    for (int i1=0; i1<dancers.length-1; i1++) {
      Dancer d1 = dancers[i1];
      if (d1.isPhantom() && !showPhantoms)
        continue;
      for (int i2=i1+1; i2<dancers.length; i2++) {
        Dancer d2 = this.dancers[i2];
        if (d2.isPhantom() && !showPhantoms)
          continue;
        Handhold hh = Handhold.getHandhold(d1,d2,geometry);
        if (hh != null)
          hhlist.add(hh);
      }
    }

    //  Sort the array to put best scores first
    Handhold[] hharr = new Handhold[hhlist.size()];
    hhlist.toArray(hharr);
    Arrays.sort(hharr);

    //  Apply the handholds in order from best to worst
    //  so that if a dancer has a choice it gets the best handhold
    for (Handhold hh : hharr) {
      //  Check that the hands aren't already used
      boolean incenter = geometry == Geometry.HEXAGON && hh.inCenter();
      if (incenter ||
          (hh.hold1 == Movement.RIGHTHAND && hh.dancer1.rightdancer == null ||
          hh.hold1 == Movement.LEFTHAND && hh.dancer1.leftdancer == null) &&
          (hh.hold2 == Movement.RIGHTHAND && hh.dancer2.rightdancer == null ||
          hh.hold2 == Movement.LEFTHAND && hh.dancer2.leftdancer == null)) {
        //      	Make the handhold visible
        //  Scale should be 1 if distance is 2
        //  float scale = hh.distance/2f;
        if (hh.hold1 == Movement.RIGHTHAND || hh.hold1 == Movement.GRIPRIGHT) {
          if (!hh.dancer1.rightHandVisibility)
            hh.dancer1.rightHandVisibility = true;
          hh.dancer1.rightHandNewVisibility = true;
        }
        if (hh.hold1 == Movement.LEFTHAND || hh.hold1 == Movement.GRIPLEFT) {
          if (!hh.dancer1.leftHandVisibility)
            hh.dancer1.leftHandVisibility = true;
          hh.dancer1.leftHandNewVisibility = true;
        }
        if (hh.hold2 == Movement.RIGHTHAND || hh.hold2 == Movement.GRIPRIGHT) {
          if (!hh.dancer2.rightHandVisibility)
            hh.dancer2.rightHandVisibility = true;
          hh.dancer2.rightHandNewVisibility = true;
        }
        if (hh.hold2 == Movement.LEFTHAND || hh.hold2 == Movement.GRIPLEFT) {
          if (!hh.dancer2.leftHandVisibility)
            hh.dancer2.leftHandVisibility = true;
          hh.dancer2.leftHandNewVisibility = true;
        }

        if (incenter)
          continue;
        if (hh.hold1 == Movement.RIGHTHAND) {
          hh.dancer1.rightdancer = hh.dancer2;
          if ((hh.dancer1.hands & Movement.GRIPRIGHT) == Movement.GRIPRIGHT)
            hh.dancer1.rightgrip = hh.dancer2;
        } else {
          hh.dancer1.leftdancer = hh.dancer2;
          if ((hh.dancer1.hands & Movement.GRIPLEFT) == Movement.GRIPLEFT)
            hh.dancer1.leftgrip = hh.dancer2;
        }
        if (hh.hold2 == Movement.RIGHTHAND) {
          hh.dancer2.rightdancer = hh.dancer1;
          if ((hh.dancer2.hands & Movement.GRIPRIGHT) == Movement.GRIPRIGHT)
            hh.dancer2.rightgrip = hh.dancer1;
        } else {
          hh.dancer2.leftdancer = hh.dancer1;
          if ((hh.dancer2.hands & Movement.GRIPLEFT) == Movement.GRIPLEFT)
            hh.dancer2.leftgrip = hh.dancer1;
        }
      }
    }
    //  Clear handholds no longer visible
    for (Dancer d : dancers) {
      if (d.rightHandVisibility && !d.rightHandNewVisibility)
        d.rightHandVisibility = false;
      if (d.leftHandVisibility && !d.leftHandNewVisibility)
        d.leftHandVisibility = false;
    }

    //  Update interactive dancer score
    if (idancer != null && beat > 0.0 && beat < beats-leadout) {
      idancer.onTrack = isInteractiveDancerOnTrack();
      if (idancer.onTrack)
        iscore += (beat - Math.max(prevbeat, 0.0)) * 10.0;
    }

    //  At end of animation?
    prevbeat = beat;
    if (beat >= beats) {
      if (loop && isRunning)
        prevbeat = beat = -leadin;
      else if (isRunning) {
        doPause();
        if (listener != null)
          listener.onAnimationChanged(AnimationListener.ANIMATION_DONE,1.0,beats,0);
      }
    }
  }

  /**
   *   This is called to generate or re-generate the dancers and their
   *   animations based on the call, geometry, and other settings.
   * @param xtam     XML element containing the call
   * @param intdan  Dancer controlled by the user, or -1 if not used
   */
  public void setAnimation(Element xtam, int intdan)
  {
    synchronized (lock) {
      if (xtam == null)  // sanity check
        return;
      this.tam = Tamination.tamXref(this.getContext(),xtam);
      interactiveDancer = intdan;
      leadin = intdan < 0 ? 2 : 3;
      leadout = intdan < 0 ? 2 : 1;
      beats = 0;
      SharedPreferences prefs =
          getContext().getSharedPreferences("Taminations", Context.MODE_PRIVATE);
      String numberpref = prefs.getString("numbers2","");
      String primaryControl = prefs.getString("primarycontrol", "Right");
      Element formation;
      Tamination.loadMoves(getContext());
      NodeList tlist = tam.getElementsByTagName("formation");
      if (tlist.getLength() > 0)
        formation = (Element)tlist.item(0);
      else
        formation = Tamination.getFormation(getContext(),tam.getAttribute("formation"));
      parts = tam.getAttribute("parts");
      NodeList flist = formation.getElementsByTagName("dancer");
      dancers = new Dancer[flist.getLength()*geometry];
      //  Except for the phantoms, these are the standard colors
      //  used for teaching callers
      int[] dancerColor = { Color.RED, Color.GREEN,
          Color.BLUE, Color.YELLOW,
          Color.LTGRAY, Color.LTGRAY,
          Color.LTGRAY, Color.LTGRAY };
      //  Get numbers for dancers and couples
      //  This fetches any custom numbers that might be defined in
      //  the animation to match a Callerlab or Ceder Chest illustration
      String[] numbers = Tamination.getNumbers(tam);
      String[] couples = Tamination.getCouples(tam);
      if (geometry == Geometry.HEXAGON) {
        numbers = new String[]{ "A","E","I",
            "B","F","J",
            "C","G","K",
            "D","H","L",
            "u","v","w","x","y","z" };
        couples = new String[]{ "1", "3", "5", "1", "3", "5",
            "2", "4", "6", "2", "4", "6",
            "7", "8", "7", "8", "7", "8" };
        dancerColor = new int[]{ Color.RED, Color.GREEN,
            Color.MAGENTA, Color.BLUE,
            Color.YELLOW, Color.CYAN,
            Color.LTGRAY, Color.LTGRAY,
            Color.LTGRAY, Color.LTGRAY };
      }
      else if (geometry == Geometry.BIGON) {
        numbers = new String[]{ "1", "2", "3", "4", "5", "6", "7", "8" };
        couples = new String[]{ "1", "2", "3", "4", "5", "6", "7", "8" };
      }
      int dnum = 0;
      int icount = -1;
      Matrix im = new Matrix();
      Vector<Geometry> geoms = Geometry.getGeometry(geometry);
      if (intdan > 0) {
        //  Select a random dancer of the correct gender for the interactive dancer
        String selector = intdan == Dancer.BOY
            ? "dancer[@gender='boy']" : "dancer[@gender='girl']";
        NodeList glist = Tamination.evalXPath(selector,formation);
        icount = (int)(Math.random()*glist.getLength());
        //  If the animations starts with "Heads" or "Sides"
        //  then select the first dancer.
        //  Otherwise the formation could rotate 90 degrees
        //  which would be confusing
        String title = tam.getAttribute("title");
        if (title.contains("Heads") || title.contains("Sides"))
          icount = 0;
        //  Find the angle the interactive dancer faces at start
        //  We want to rotate the formation so that direction is up
        double iangle = Double.valueOf(((Element)glist.item(icount)).getAttribute("angle"));
        im.preRotate(-Math.toRadians(iangle));
        icount *= geoms.size();
      }
      //  Create dancers for each one listed in the formation
      for (int i=0; i<flist.getLength(); i++) {
        Element fd = (Element)flist.item(i);
        float x = Float.valueOf(fd.getAttribute("x"));
        float y = Float.valueOf(fd.getAttribute("y"));
        double angle = Double.valueOf(fd.getAttribute("angle"));
        String gender = fd.getAttribute("gender");
        int g = 0;
        switch (gender) {
          case "boy":
            g = Dancer.BOY;
            break;
          case "girl":
            g = Dancer.GIRL;
            break;
          case "phantom":
            g = Dancer.PHANTOM;
            break;
        }
        Element pathelem = (Element)tam.getElementsByTagName("path").item(i);
        List<Movement> movelist = Tamination.translatePath(pathelem);
        //  Each dancer listed in the formation corresponds to
        //  one, two, or three real dancers depending on the geometry
        for (Geometry geom : geoms) {
          Matrix m = new Matrix();
          //  compute the transform for the start position
          m.postRotate(Math.toRadians(angle));
          m.postTranslate(x, y);
          m.postConcat(im);
          //  handle numbers and colors for phantoms
          String nstr = " ";
          String cstr = " ";
          int cnum = Color.LTGRAY;
          if (g != Dancer.PHANTOM) {
            nstr = numbers[dnum];
            cstr = couples[dnum];
            cnum = dancerColor[Integer.valueOf(cstr)-1];
          }
          //  add one dancer
          if (g == intdan && icount-- == 0) {
            //  add the interactive dancer controlled by the user
            idancer = new InteractiveDancer(nstr,cstr,g,cnum,
                m,geom,movelist,primaryControl);
            dancers[dnum] = idancer;
          }
          else
            dancers[dnum] = new Dancer(nstr,cstr,g,cnum,
                m,geom,movelist);
          if (g == Dancer.PHANTOM && !showPhantoms)
            dancers[dnum].hidden = true;
          beats = Math.max(beats,dancers[dnum].beats()+leadout);
          dnum++;
        }
      }
      partbeats = getPartsValues();
      currentpart = 0;
      isRunning = false;
      beat = -leadin;
      if (numberpref.contains("1-8"))
        setNumbers(Dancer.NUMBERS_DANCERS);
      else if (numberpref.contains("1-4"))
        setNumbers(Dancer.NUMBERS_COUPLES);
      else
        setNumbers(Dancer.NUMBERS_OFF);
      dirtify();
    }
  }


  public AnimationView(Context context, AttributeSet attrs)
  {
    super(context,attrs);
    //  This enables the callbacks to surfaceCreated and surfaceDestroyed
    getHolder().addCallback(this);
    setOnTouchListener(this);
  }


  public void setAnimation(Element tam)
  {
    setAnimation(tam,-1);
  }

  //  Set up animation given just a formation without any animation
  //  Used by the sequencer
  public void setFormation(Element f)
  {
    //  Build a complete tam element with empty paths
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();
      Element tam = doc.createElement("tam");
      doc.appendChild(tam);
      tam.appendChild(doc.adoptNode(f.cloneNode(true)));
      int ndancers = f.getElementsByTagName("dancer").getLength();
      for (int i=0; i<ndancers; i++)
        tam.appendChild(doc.createElement("path"));
      //  Now send the complete element to the animation builder
      setAnimation(tam);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }


  public void setAnimationListener(AnimationListener l)
  {
    listener = l;
  }

  /**
   * Standard window-focus override. Notice focus lost so we can pause on
   * focus lost. e.g. user switches to take a call.
   */
  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus)
  {
    if (!hasWindowFocus)
      doPause();
  }

  /**
   * Callback invoked when the Surface has been created and is ready to be
   * used.
   */
  @Override
  public void surfaceCreated(SurfaceHolder surface_) {
    surface = surface_;
    readSettings();
    // start the thread here, it will wait until Play is pressed
      //if (tam != null)
      //  listener.onAnimationChanged(AnimationListener.ANIMATION_READY,0,0,0);
    thread = new Thread(this);
    thread.start();
  }

  public void readSettings()
  {
    SharedPreferences prefs =
        getContext().getSharedPreferences("Taminations", Context.MODE_PRIVATE);
    setGridVisibility(prefs.getBoolean("grid", false));
    setPathVisibility(prefs.getBoolean("paths", false));
    String geom = prefs.getString("geometry","None");
    switch (geom) {
      case "Hexagon":
        setHexagon();
        break;
      case "Bi-gon":
        setBigon();
        break;
      default:
        setSquare();
        break;
    }
    setLoop(prefs.getBoolean("loop",false));
    setSpeed(prefs.getString("speed", "Normal"));
    setPhantomVisibility(prefs.getBoolean("phantoms",false));
    if (tam != null)
      setAnimation(tam,interactiveDancer);
    if (listener != null && tam != null && interactiveDancer < 0)
      listener.onAnimationChanged(AnimationListener.ANIMATION_DONE,0,0,0);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
  }

  public void onDraw(Canvas c)
  {
    dirtify();
  }

  /**
   *  Callback invoked when the Surface has been destroyed and must no longer
   *  be touched. After this method returns, the Surface/Canvas must
   *  never be touched again!
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    // we have to tell thread to shut down & wait for it to finish, or else
    // it might touch the Surface after we return and explode
    boolean retry = true;
    isRunning = false;
    surface = null;
    dirtify();
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
        // retry again
      }
    }
  }

  @Override
  public boolean onTouch(View v, MotionEvent m)
  {
    if (idancer != null)
      idancer.doTouch(v,m);
    else if (m.getAction()==MotionEvent.ACTION_DOWN && m.getPointerCount() > 0)
      doTouch(m.getX(),m.getY());
    return true;
  }



}
