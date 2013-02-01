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
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class AnimationView extends SurfaceView implements SurfaceHolder.Callback
{

  class AnimationThread extends Thread {

    /** Indicate whether the surface has been created & is ready to draw */
    private boolean isAlive = true;
    /** Indicate if the animation is running */
    private boolean isRunning = false;
    /** Indicate if the grid should be drawn */
    private boolean showGrid = false;
    /** Indicate if dancer paths should be drawn */
    private boolean showPaths = false;
    /** Indicate if the animation should repeat when the end is reached */
    private boolean loop = false;
    /** Indicate if phantoms should be drawn */
    private boolean showPhantoms = false;
    /** Array of dancers in the current animation */
    private Dancer[] dancers;
    /** Context from application */
    private Context ctx;
    /** Total length of the animation */
    private float beats = 0f;
    private float speed = 500f;
    private AnimationListener listener = null;
    private boolean dirty = false;
    /** Parts string from formation, for passing to tic labeler  */
    private String parts;

    /** Handle to the surface manager object we interact with */
    private SurfaceHolder mSurfaceHolder;

    /** Used to figure out elapsed time between frames */
    private long mLastTime;
    /** Current location in the animation */
    private float beat = -2f;

    public AnimationThread(SurfaceHolder surfaceHolder, Context context,
        Handler handler) {
      mSurfaceHolder = surfaceHolder;
      ctx = context;
    }

    /**
     *   Starts the animation
     */
    public synchronized void doStart()
    {
      mLastTime = System.currentTimeMillis();
      if (beat > beats)
        beat = -2f;
      isAlive = true;
      isRunning = true;
      dirtify();
    }

    /**
     * Pauses the dancers update & animation.
     */
    public synchronized void doPause()
    {
      isRunning = false;
    }

    /**
     *  Rewinds to the start of the animation, even if it is running
     */
    public synchronized void doRewind()
    {
      beat = -2f;
      dirtify();
    }

    /**
     *   Moves the animation back a little
     */
    public synchronized void doBackup()
    {
      beat = Math.max(beat-0.1f, -2f);
      dirtify();
    }

    /**
     *   Moves the animation forward a little
     */
    public synchronized void doForward()
    {
      beat = Math.min(beat+0.1f, beats);
      dirtify();
    }

    /**
     *   Moves to the end of the animation, minus leadout
     */
    public synchronized void doEnd()
    {
      beat = beats-2f;
      dirtify();
    }

    /**  Tells caller if the animation is running
     *
     */
    public synchronized boolean running()
    {
      return isRunning;
    }

    /**
     * Shut down the animation and stop the thread.
     */
    public synchronized void doQuit()
    {
      isRunning = false;
      isAlive = false;
      dirtify();
    }

    /**
     *   Set the visibility of the grid
     */
    public synchronized void setGridVisibility(boolean show)
    {
      showGrid = show;
      dirtify();
    }

    /**
     *  Turn on drawing of dancer paths
     */
    public synchronized void setPathVisibility(boolean show)
    {
      showPaths = show;
      dirtify();
    }

    /**
     *   Set animation looping
     */
    public synchronized void setLoop(boolean loopit)
    {
      loop = loopit;
    }

    /**
     *   Set display of dancer numbers
     */
    public synchronized void setNumbers(boolean numberem)
    {
      for (Dancer d : dancers)
        d.showNumber = numberem;
    }

    /**
     *   Set speed of animation
     */
    public synchronized void setSpeed(String myspeed)
    {
      if (myspeed.equals("Slow"))
        speed = 1500f;
      else if (myspeed.equals("Fast"))
        speed = 200;
      else
        speed = 500;  // default normal speed
    }

    /**
     *   Return animation beats, including 2 beat intro
     */
    public synchronized float getBeats()
    {
      return beats+2f;
    }

    /**
     *  Return animation parts, defined in formation xml
     */
    public synchronized String getParts()
    {
      return parts;
    }

    /**
     *   Set location of animation
     */
    public synchronized void setLocation(float loc)
    {
      beat = loc - 2f;
      dirtify();
    }

    /**
     *   Called when a change is made that affects the display.
     *   Tell the display to redraw even if the animation is not running.
     */
    private void dirtify()
    {
      dirty = true;
      notify();
    }

    @Override
    public void run() {
      while (isAlive) {
        synchronized (this) {
          if (dirty || isRunning) {
            updateDancers();
            doDraw();
          }
          if (listener != null)
            listener.onAnimationChanged(AnimationListener.ANIMATION_PROGRESS,
                                        (beat+2f)/(beats+2f));
          if (!isRunning)
            //  animation is not running, so don't chew up the CPU
            try {
              wait();
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
        c = mSurfaceHolder.lockCanvas();
        if (c == null)
          return;  // sanity check
        //  Draw background
        ColorDrawable cd = new ColorDrawable(0xffffffc0);
        Rect candim = c.getClipBounds();
        cd.setBounds(candim);
        cd.draw(c);
        //  Use dancer's coordinate system
        float range = Math.min(candim.width(),candim.height());
        c.translate(candim.width()/2,candim.height()/2);
        float s = range/13.0f;
        c.scale(s,-s);
        c.rotate(90f);
        //  Draw grid if on
        if (showGrid) {
          Paint pline = new Paint();
          pline.setARGB(255,0,0,0);
          pline.setStrokeWidth(0f);
          for (float x=-7.5f; x<=7.5f; x+=1)
            c.drawLine(x,-7.5f,x,7.5f,pline);
          for (float y=-7.5f; y<=7.5f; y+=1)
            c.drawLine(-7.5f,y,7.5f,y,pline);
        }
        //  Draw paths if requested
        if (showPaths) {
          for (Dancer d: dancers) {
            if (!d.hidden) {
              Paint ppath = new Paint();
              //  The path color is a partly transparent version of the draw color
              ppath.setColor(d.drawColor & 0x50ffffff);
              ppath.setStyle(Style.STROKE);
              ppath.setStrokeWidth(0.1f);
              c.drawPath(d.pathpath,ppath);
            }
          }
        }
        //  Draw handholds
        Paint hline = new Paint();
        hline.setColor(Color.ORANGE);
        hline.setStrokeWidth(0.05f);
        hline.setStyle(Style.FILL);
        for (Dancer d: dancers) {
          Pair<Float,Float> loc = d.location();
          if (d.rightHandVisibility && d.rightdancer.number.compareTo(d.number) < 0) {
            Pair<Float,Float> loc2 = d.rightdancer.location();
            c.drawLine(loc.first, loc.second, loc2.first, loc2.second, hline);
            c.drawCircle((loc.first+loc2.first)/2f,
                         (loc.second+loc2.second)/2f,.125f,hline);
          }
          if (d.leftHandVisibility && d.leftdancer.number.compareTo(d.number) < 0) {
            Pair<Float,Float> loc2 = d.leftdancer.location();
            c.drawLine(loc.first, loc.second, loc2.first, loc2.second, hline);
            c.drawCircle((loc.first+loc2.first)/2f,
                         (loc.second+loc2.second)/2f,.125f,hline);
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
          mSurfaceHolder.unlockCanvasAndPost(c);
      }
    }

    /**
     * Updates dancers positions based on the passage of realtime.
     * Called at the start of draw().
     */
    private void updateDancers() {
      //  Update the animation time
      long now = System.currentTimeMillis();
      long diff = now - mLastTime;
      if (isRunning)
        beat += (float)diff/speed;
      mLastTime = now;
      //  Move dancers
      for (int i=0; i<dancers.length; i++)
        dancers[i].animate(beat);

      //  TODO add rest of code from tamsvg.paint
      //  specifically hexagon, bigon, barstool

      //  Compute handholds
      //  TODO Handhold.dfactor0 = this.hexagon ? 1.15 : 1.0;
      ArrayList<Handhold> hhlist = new ArrayList<Handhold>();
      for (Dancer d0 : dancers) {
        d0.rightdancer = d0.leftdancer = null;
        d0.rightHandNewVisibility = false;
        d0.leftHandNewVisibility = false;
      }
      for (int i1=0; i1<dancers.length-1; i1++) {
        Dancer d1 = dancers[i1];
        if (d1.isPhantom() && !this.showPhantoms)
          continue;
        for (int i2=i1+1; i2<this.dancers.length; i2++) {
          Dancer d2 = this.dancers[i2];
          if (d2.isPhantom() && !this.showPhantoms)
            continue;
          Handhold hh = Handhold.getHandhold(d1,d2);
          if (hh != null)
            hhlist.add(hh);
        }
      }

      Handhold[] hharr = new Handhold[hhlist.size()];
      hhlist.toArray(hharr);
      Arrays.sort(hharr);

      //  Apply the handholds in order from best to worst
      //  so that if a dancer has a choice it gets the best handhold
      for (Handhold hh : hharr) {
        /*if (this.bigon) {
          if (Math.abs(hh.d1.centerAngle()-3*Math.PI/2) < 3 &&
              hh.d1.hands == Movement.RIGHTHAND)
            continue;
        }*/
        //  Check that the hands aren't already used
        //  TODO var incenter = this.hexagon && hh.inCenter();
        boolean incenter = false;
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

      //  At end of animation?
      if (beat >= beats) {
        if (loop && isRunning)
          beat = -2f;
        else {
          doPause();
          if (listener != null)
            listener.onAnimationChanged(AnimationListener.ANIMATION_DONE,1f);
        }
      }
    }

    public synchronized void setAnimation(Element tam)
    {
      Element formation = null;
      Tamination.loadMoves(ctx);
      NodeList tlist = tam.getElementsByTagName("formation");
      if (tlist.getLength() > 0)
        formation = (Element)tlist.item(0);
      else
        formation = Tamination.getFormation(ctx,tam.getAttribute("formation"));
      parts = tam.getAttribute("parts");
      NodeList flist = formation.getElementsByTagName("dancer");
      dancers = new Dancer[flist.getLength()*2];
      int[] dancerColor = { Color.RED, Color.YELLOW, Color.LTGRAY };
      String[] numbers = Tamination.getNumbers(tam);
      for (int i=0; i<flist.getLength(); i++) {
        Element fd = (Element)flist.item(i);
        float x = Float.valueOf(fd.getAttribute("x"));
        float y = Float.valueOf(fd.getAttribute("y"));
        float angle = Float.valueOf(fd.getAttribute("angle"));
        String gender = fd.getAttribute("gender");
        int g = 0;
        if (gender.equals("boy"))
          g = Dancer.BOY;
        else if (gender.equals("girl"))
          g = Dancer.GIRL;
        else if (gender.equals("phantom"))
          g = Dancer.PHANTOM;
        Element pathelem = (Element)tam.getElementsByTagName("path").item(i);
        List<Movement> movelist = Tamination.translatePath(pathelem);
        dancers[i*2] = new Dancer(numbers[i*2],g,dancerColor[i/2],x,y,angle,movelist);
        dancers[i*2+1] =
              new Dancer(numbers[i*2+1],g,Color.rotate(dancerColor[i/2]),-x,-y,angle+180f,movelist);
        if (g == Dancer.PHANTOM && !showPhantoms) {
          dancers[i*2].hidden = true;
          dancers[i*2+1].hidden = true;
        }
        beats = Math.max(beats,dancers[i*2].beats()+2f);
      }
      dirtify();
    }

    public synchronized void setListener(AnimationListener l)
    {
      listener = l;
    }

  }  // end of AnimationThread class

  /** The thread that actually draws the animation */
  private AnimationThread thread;
  private Element tam;
  private AnimationListener listener = null;

  public AnimationView(Context context, AttributeSet attrs)
  {
    super(context,attrs);
    // register our interest in hearing about changes to our surface
    SurfaceHolder holder = getHolder();
    holder.addCallback(this);
  }

  /**
   * Fetches the animation thread corresponding to this AnimationView.
   *
   * @return the animation thread
   */
  public AnimationThread getThread() {
    return thread;
  }

  public void setAnimation(Element tam)
  {
    this.tam = tam;
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
  public void onWindowFocusChanged(boolean hasWindowFocus) {
      if (!hasWindowFocus)
        thread.doPause();
  }

  /**
   * Callback invoked when the Surface has been created and is ready to be
   * used.
   */
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    // start the thread here so that we don't busy-wait in run()
    // waiting for the surface to be created
    // create thread if needed
    thread = new AnimationThread(holder, getContext(), new Handler() {
      @Override
      public void handleMessage(Message m) {
      }
    });
    thread.setAnimation(tam);
    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(getContext());
    thread.setGridVisibility(prefs.getBoolean("grid", false));
    thread.setPathVisibility(prefs.getBoolean("paths", false));
    thread.setLoop(prefs.getBoolean("loop",false));
    thread.setNumbers(prefs.getBoolean("numbers",false));
    thread.setSpeed(prefs.getString("speed", "Normal"));
    thread.setListener(listener);
    if (listener != null)
      listener.onAnimationChanged(AnimationListener.ANIMATION_READY,0f);
    thread.start();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
      int height) {
  }

  /**
   * Callback invoked when the Surface has been destroyed and must no longer
   * be touched. WARNING: after this method returns, the Surface/Canvas must
   * never be touched again!
   */
  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    // we have to tell thread to shut down & wait for it to finish, or else
    // it might touch the Surface after we return and explode
    boolean retry = true;
    thread.doQuit();
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException e) {
        // retry again
      }
    }

  }


}
