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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;

public class Tamination {

  static private Document mdoc = null;

  static public Document getXMLAsset(Context ctx, String name)
  {
    Document doc = null;
    //  Read the xml file
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = ctx.getAssets().open(name);
      doc = db.parse(is);
      is.close();
    } catch (Exception e) {
      //  	TODO report error
    }
    return doc;
  }

  static public void loadMoves(Context ctx)
  {
    if (mdoc == null)
      mdoc = getXMLAsset(ctx,"src/moves.xml");
  }

  static public Element getFormation(Context ctx, String fname)
  {
    Document fdoc = getXMLAsset(ctx,"src/formations.xml");
    NodeList flist = fdoc.getElementsByTagName("formation");
    for (int i=0; i<flist.getLength(); i++) {
      Element f = (Element)flist.item(i);
      if (f.getAttribute("name").equals(fname))
        return f;
    }
    //  TODO handle this error
    return null;
  }

  static List<Movement> translate(Element elem)
  {
    List<Movement> retval = null;
    String tag = elem.getTagName();
    if (tag.equals("path"))
      retval = translatePath(elem);
    else if (tag.equals("move"))
      retval = translateMove(elem);
    else if (tag.equals("movement"))
      retval = translateMovement(elem);
    return retval;
  }

  //  Takes a path, which is an XML element with children that
  //  are moves or movements.
  //  Returns an array of movements
  static List<Movement> translatePath(Element pathelem)
  {
    List<Movement> movements = new ArrayList<Movement>();
    NodeList movelist = pathelem.getElementsByTagName("*");
    for (int i=0; i<movelist.getLength(); i++)
      movements.addAll(translate((Element)movelist.item(i)));
    return movements;
  }

  //  Takes a move, which is an XML element that references another XML
  //  path with its "select" attribute
  //  Returns an array of movements
  static List<Movement> translateMove(Element moveelem)
  {
    //  First retrieve the requested path
    String movename = moveelem.getAttribute("select");
    NodeList plist = mdoc.getElementsByTagName("path");
    Element pathelem = null;
    for (int p=0; p<plist.getLength(); p++) {
      pathelem = (Element)plist.item(p);
      if (pathelem.getAttribute("name").equals(movename))
        break;
    }
    // TODO handle error if path not found
    List<Movement> retval = translatePath(pathelem);
    //  Now apply any modifications
    double beats = 0;
    if (moveelem.hasAttribute("beats"))
      beats = Double.valueOf(moveelem.getAttribute("beats"));
    double scaleX = 0;
    if (moveelem.hasAttribute("scaleX"))
      scaleX = Double.valueOf(moveelem.getAttribute("scaleX"));
    double scaleY = 0;
    if (moveelem.hasAttribute("scaleY"))
      scaleY = Double.valueOf(moveelem.getAttribute("scaleY"));
    double offsetX = 0;
    if (moveelem.hasAttribute("offsetX"))
      offsetX = Double.valueOf(moveelem.getAttribute("offsetX"));
    double offsetY = 0;
    if (moveelem.hasAttribute("offsetY"))
      offsetY = Double.valueOf(moveelem.getAttribute("offsetY"));
    String reflect = moveelem.getAttribute("reflect");
    String hands = moveelem.getAttribute("hands");
    double oldbeats = 0;  //  If beats is given, we need to know how to scale
    for  (int i=0; i<retval.size(); i++)  // each movement
      oldbeats += retval.get(i).beats;
    for  (int i=0; i<retval.size(); i++) {
      Movement move = retval.get(i);
      if (beats != 0)
        move.beats *= beats / oldbeats;
      if (scaleX != 0) {
        move.cx1 *= scaleX;
        move.cx2 *= scaleX;
        move.x2 *= scaleX;
        move.cx3 *= scaleX;
        move.cx4 *= scaleX;
        move.x4 *= scaleX;
      }
      if (scaleY != 0) {
        move.cy1 *= scaleY;
        move.cy2 *= scaleY;
        move.y2 *= scaleY;
        move.cy4 *= scaleY;
        move.y4 *= scaleY;
      }
      if (reflect.equals("-1")) {
        move.cy1 = -move.cy1;
        move.cy2 = -move.cy2;
        move.y2 = -move.y2;
        move.cy4 = -move.cy4;
        move.y4 = -move.y4;
      }
      if (offsetX != 0) {
        move.cx2 += offsetX;
        move.x2 += offsetX;
      }
      if (offsetY != 0) {
        move.cy2 += offsetY;
        move.y2 += offsetY;
      }
      if (!hands.equals(""))
        move.hands = Movement.getHands(hands);
      else if (reflect.equals("-1")) {
        if (move.hands == Movement.RIGHTHAND)
          move.hands = Movement.LEFTHAND;
        else if (move.hands == Movement.LEFTHAND)
          move.hands = Movement.RIGHTHAND;
        else if (move.hands == Movement.GRIPRIGHT)
          move.hands = Movement.GRIPLEFT;
        else if (move.hands == Movement.GRIPLEFT)
          move.hands = Movement.GRIPRIGHT;
      }
      move.recalculate();
    }
    return retval;
  }

  //  Accepts a movement element from a XML file, either an animation definition
  //  or moves.xml
  //  Returns an array of a single movement
  static List<Movement> translateMovement(Element move)
  {
    List<Movement> movements = new ArrayList<Movement>();
    movements.add(new Movement(move));
    return movements;
  }

  /**
   *   Returns an array of numbers to use numering the dancers
   */
  private static String[] phantomNums = { "a", "b", "c", "d" };
  static String[] getNumbers(Element tam)
  {
    NodeList paths = tam.getElementsByTagName("path");
    int np = Math.min(paths.getLength(), 4);
    String[] retval = new String[paths.getLength()*2];
    for (int i=0; i<paths.getLength(); i++) {
      Element p = (Element)paths.item(i);
      String n = p.getAttribute("numbers");
      if (n.length() >= 3) {  // numbers supplied in animation XML
        retval[i*2] = n.substring(0,1);
        retval[i*2+1] = n.substring(2,3);
      }
      else if (i > 3) {  // phantoms
        retval[i*2] = phantomNums[i*2-8];
        retval[i*2+1] = phantomNums[i*2-7];
      }
      else { // default numbers
        retval[i*2] = String.valueOf(i+1);
        retval[i*2+1] = String.valueOf(i+1+np);
      }
    }
    return retval;
  }

}
