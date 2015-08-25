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

import scala.collection.JavaConversions.asScalaBuffer

object Path {

  def apply(moves:java.util.List[Movement]) = {
    val mm = asScalaBuffer(moves)
    var mlist = List[Movement]()
    for (m <- mm)
      mlist = mlist :+ m
    new Path(mlist)
  }

}

class Path
{
  //  Eventually we might make Path a fully functional class,
  //  returning a new Path on any requested change
  //  Then these would be vals
  var movelist:List[Movement] = Nil
  var transformlist:List[Matrix] = Nil

  def recalculate() = {
    var tx = new Matrix()
    transformlist = movelist.map(m => {
      tx = tx.preConcat(m.translate()).preConcat(m.rotate())
      new Matrix(tx)
    })
  }

  def this(move:List[Movement]) = {
    this()
    movelist = move.map(m => m)
    recalculate()
  }

  def copy = new Path(movelist)

  def clear() = {
    movelist = Nil
    transformlist = Nil
  }

  def add(p:Path):Path = {
    movelist = movelist ++ p.movelist
    recalculate
    this
  }

  def pop:Movement = {
    val m:Movement = movelist.last
    movelist = movelist.init
    recalculate
    m
  }

  def reflect() = {
    movelist = movelist.map(m => m.reflect)
    recalculate
  }

  def beats:Double =
    movelist.foldLeft[Double](0)((b,m) => b + m.beats)

  def changebeats(newbeats:Double) = {
    val factor = newbeats / beats
    movelist = movelist.map(m => m.time(m.beats*factor))
    //  no need to recalculate, transformlist doesn't depend on beats
  }

  def changehands(hands:Int) =
    movelist = movelist.map(m => m.handy(hands))

  def scale(x:Double, y:Double) = {
    movelist = movelist.map(m => m.scale(x,y))
    recalculate
  }

  def skew(x:Double, y:Double) = {
    if (movelist.size > 0)
      //  Apply the skew to just the last movement
      movelist = movelist.init :+ movelist.last.skew(x, y)
      recalculate
  }

  /**
   * Return a transform for a specific point of time
   */
  def animate(b:Double):Matrix = {
    var bv = b
    var tx:Matrix = new Matrix()
    // Apply all completed movements
    var m:Movement = null
    for (i <- 0 until movelist.size if m == null) {
      m = movelist(i)
      if (bv >= m.beats) {
        tx = transformlist(i)
        bv = bv - m.beats
        m = null
      }
    }
    // Apply movement in progress
    if (m != null)
      tx = tx.preConcat(m.translate(bv)).preConcat(m.rotate(bv))
    tx
  }

  /**
   * Return the current hand at a specific point in time
   */
  def hands(b:Double):Int = {
    var bv = b
    var m:Movement = null
    var h:Int = Movement.BOTHHANDS
    for (i <- 0 until movelist.size if m == null) {
      m = movelist(i)
      h = m.hands
      if (bv >= m.beats) {
        bv = bv - m.beats
        m = null
      }
    }
    if (m == null)
      //  End of path
      h = Movement.BOTHHANDS
    h
  }

}
