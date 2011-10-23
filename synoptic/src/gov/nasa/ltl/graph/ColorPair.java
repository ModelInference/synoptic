
//
// Copyright (C) 2005 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.ltl.graph;

import java.util.Iterator;
import java.util.TreeSet;


/**
 * DOCUMENT ME!
 */
public class ColorPair extends Pair<TreeSet<ITypeNeighbor>> implements Comparable<ColorPair> {
  public ColorPair (int colorIn, TreeSet<ITypeNeighbor> iMaxSetIn) {
    super(colorIn, iMaxSetIn);
  }

  public void setColor (int colorIn) {
    super.setValue(colorIn);
  }

  public int getColor () {
    return super.getValue();
  }

  public void setIMaxSet (TreeSet<ITypeNeighbor> iMaxSetIn) {
    super.setElement(iMaxSetIn);
  }

  public TreeSet<ITypeNeighbor> getIMaxSet () {
    return super.getElement();
  }

  public int compareTo (ColorPair other) {
    TreeSet<ITypeNeighbor>   otherSet = other.getIMaxSet();

    if (getIMaxSet().size() < otherSet.size()) {
      return -1;
    }

    if (getIMaxSet().size() > otherSet.size()) {
      return 1;
    }

    // TreeSets are ordered !!
    int index = 0;

    for (Iterator<ITypeNeighbor> i = getIMaxSet().iterator(); i.hasNext();) {
      ITypeNeighbor currNeigh = i.next();
      ITypeNeighbor[]      otherArray = otherSet.toArray(new ITypeNeighbor[otherSet.size()]);
      int           comparison = currNeigh.compareTo(
                                       otherArray[index]);

      if ((comparison < 0) || (comparison > 0)) {
        return comparison;
      }

      index++;
    }

    if (getColor() < other.getColor()) {
      return -1;
    }

    if (getColor() > other.getColor()) {
      return 1;
    }

    return 0;
  }

  @Override
  public boolean equals (Object o) {
    if (!(o instanceof ColorPair))
        return false;
    ColorPair other = (ColorPair) o;
    TreeSet<ITypeNeighbor>   otherSet = other.getIMaxSet();

    if (getIMaxSet().size() < otherSet.size()) {
      return false;
    }

    if (getIMaxSet().size() > otherSet.size()) {
      return false;
    }

    if (getColor() != other.getColor()) {
      return false;
    }

    // TreeSets are ordered
    int index = 0;

    for (Iterator<ITypeNeighbor> i = getIMaxSet().iterator(); i.hasNext();) {
      ITypeNeighbor currNeigh = i.next();
      ITypeNeighbor[] otherArray = otherSet.toArray(new ITypeNeighbor[otherSet.size()]);
      int           comparison = currNeigh.compareTo(
                                       otherArray[index]);

      if ((comparison < 0) || (comparison > 0)) {
        return false;
      }

      index++;
    }

    return true;
  }
}