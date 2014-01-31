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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class Tamination {

  static private Document mdoc = null;
  static private Document fdoc = null;
  static private MediaPlayer mediaPlayer = null;


  /**
   *   Convenience method to retrieve a XML document
   * @param ctx
   * @param name
   * @return
   */
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

  /**
   *   Convenience method to evaluate a XPath expression
   * @param expr
   * @param node
   * @return
   */
  static public NodeList evalXPath(String expr, Object node)
  {
    NodeList retval = null;
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      retval = (NodeList) xpath.evaluate(expr, node, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      //  TODO report error
    }
    return retval;
  }

  static public String audioAssetName(String call)
  {
    return call.replace(" ", "_").toLowerCase(Locale.US)+".ogg";
  }

  /**
   *   Test if an asset exists.  Assumes assets are all one directory deep.
   * @param ctx
   * @param dir
   * @param name
   * @return
   */
  static public boolean assetExists(Context ctx, String dir, String name)
  {
    boolean retval = false;
    try {
      AssetManager ass = ctx.getAssets();
      String[] asses = ass.list(dir);
      retval = Arrays.asList(asses).contains(name);
    } catch (IOException e) {
      //  Serious I/O problem
      e.printStackTrace();
    }
    return retval;
  }

  /**
   *    Convenience method to play the audio of a call name
   *    if the audio file is available
   * @param level  Level directory name
   * @param name  Call name, such as "All 8 Circulate"
   */
  static public void playCallName(Context ctx, String level, String call)
  {
    //  See if there's an audio file, with Android's strange way
    //  of finding the presence of an asset
    try {
      AssetManager ass = ctx.getAssets();
      String assname = audioAssetName(call);
      if (assetExists(ctx,level,assname)) {
        //  File exists, open it and send to media player
        AssetFileDescriptor assfile = ass.openFd(level+"/"+assname);
        //  Shut down any previous media player
        if (mediaPlayer == null) {
          mediaPlayer = new MediaPlayer();
          mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mediaPlayer.reset();
        mediaPlayer.setDataSource(assfile.getFileDescriptor(),
                                  assfile.getStartOffset(),assfile.getLength());
        assfile.close();
        mediaPlayer.prepare();
        mediaPlayer.setVolume(1f, 1f);
        mediaPlayer.start();
      }
    } catch (IOException e) {
      // Serious I/O error
      e.printStackTrace();
    }
  }


  static public void loadMoves(Context ctx)
  {
    if (mdoc == null)
      mdoc = getXMLAsset(ctx,"src/moves.xml");
  }

  static public Element getFormation(Context ctx, String fname)
  {
    if (fdoc == null)
      fdoc = getXMLAsset(ctx,"src/formations.xml");
    NodeList flist = evalXPath("/formations/formation[@name='"+fname+"']",fdoc);
    return (Element)flist.item(0);
  }

  static int maxdumps = 20;
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
    for (int i=0; i<movelist.getLength(); i++) {
      Element elem = (Element)movelist.item(i);
      //  A bug in Gingerbread XML parsing getElementsByTagName
      //  returns the parent as well as the children
      //  if it matches the selector.
      //  So we need to check for that here.
      if (!elem.getTagName().equals("path"))
        movements.addAll(translate(elem));
    }
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
    float beats = 0f;
    if (moveelem.hasAttribute("beats"))
      beats = Float.valueOf(moveelem.getAttribute("beats"));
    float scaleX = 0f;
    if (moveelem.hasAttribute("scaleX"))
      scaleX = Float.valueOf(moveelem.getAttribute("scaleX"));
    float scaleY = 0f;
    if (moveelem.hasAttribute("scaleY"))
      scaleY = Float.valueOf(moveelem.getAttribute("scaleY"));
    float offsetX = 0f;
    if (moveelem.hasAttribute("offsetX"))
      offsetX = Float.valueOf(moveelem.getAttribute("offsetX"));
    float offsetY = 0f;
    if (moveelem.hasAttribute("offsetY"))
      offsetY = Float.valueOf(moveelem.getAttribute("offsetY"));
    String reflect = moveelem.getAttribute("reflect");
    String hands = moveelem.getAttribute("hands");
    float oldbeats = 0f;  //  If beats is given, we need to know how to scale
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

  static String[] getCouples(Element tam)
  {
    String[] retval = {"1","3","1","3","2","4","2","4","5","6","5","6"};
    NodeList paths = tam.getElementsByTagName("path");
    for (int i=0; i<paths.getLength(); i++) {
      Element p = (Element)paths.item(i);
      String c = p.getAttribute("couples");
      if (c.length() > 0) {
        retval[i*2] = c.substring(0,1);
        retval[i*2+1] = c.substring(2,3);
      }
    }
    return retval;
  }

  static String levelDir2Namexxx(String dir)
  {
    @SuppressWarnings("serial")
    HashMap<String,String>levelnames =
        new HashMap<String,String>() {
      {
        put("b1","Basic 1");
        put("b2","Basic 2");
        put("ms","Mainstream");
        put("plus","Plus");
        put("a1","A-1");
        put("a2","A-2");
        put("c1","C-1");
        put("c2","C-2");
        put("c3a","C-3A");
      }
    };
    return levelnames.get(dir);
  }

}
