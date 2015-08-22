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
import java.util.List;

public class Path
{
  public List<Movement> movelist;
  public List<Matrix> transformlist;

  public Path() {
    clear();
  }

  public Path(Path p) {
    clear();
    add(p);
  }

  public Path(List<Movement> moves)
  {
    clear();
    for (Movement m : moves)
      movelist.add(new Movement(m));
    recalculate();
  }
  //  other constructors to be added as needed

  public void clear()
  {
    movelist = new ArrayList<>();
    transformlist = new ArrayList<>();
  }

  public Path add(Path p)
  {
    movelist.addAll(p.movelist);
    recalculate();
    return this;
  }

  public Path add(Movement m)
  {
    movelist.add(m);
    recalculate();
    return this;
  }

  public Movement pop()
  {
    Movement m = movelist.remove(movelist.size()-1);
    recalculate();
    return m;
  }

  public void reflect()
  {
    for (Movement m : movelist)
      m.reflect();
    recalculate();
  }

  double beats()
  {
    double b = 0.0;
    for (Movement m : movelist)
      b += m.beats;
    return b;
  }

  void changebeats(double newbeats)
  {
    double factor = newbeats / beats();
    for (Movement m : movelist)
      m.beats *= factor;
  }

  void changehands(int hands)
  {
    for (Movement m : movelist)
      m.hands = hands;
  }

  void scale(double x, double y)
  {
    for (Movement m : movelist)
      m.scale(x,y);
  }

  void skew(double x, double y)
  {
    if (movelist.size() > 0)
      movelist.get(movelist.size()-1).skew(x,y);
  }

  private void recalculate()
  {
    transformlist = new ArrayList<>();
    Matrix tx = new Matrix();
    for (Movement m : movelist) {
      tx.preConcat(m.translate());
      tx.preConcat(m.rotate());
      transformlist.add(new Matrix(tx));
    }
  }

}
