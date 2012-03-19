
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

// Written by Dimitra Giannakopoulou, 19 Jan 2001
package gov.nasa.ltl.trans;

import gov.nasa.ltl.graph.*;

/**
 * DOCUMENT ME!
 */
public class Translator {
  public static final int LTL2AUT = 0;
  public static final int LTL2BUCHI = 1;
  private static int      algorithm = LTL2BUCHI; // by default 

  public static int get_algorithm () {
    return algorithm;
  }

  public static boolean set_algorithm (int alg) {
    // returns true iff value was legal
    if ((alg == LTL2AUT) || (alg == LTL2BUCHI)) {
      algorithm = alg;

      return true;
    } else {
      return false;
    }
  }

  public static Graph translate (String formula) {
    try {
      Formula ltl = Formula.parse(formula);
      Node    init = Node.createInitial(ltl);
      State[] states = (init.expand(new Automaton())).structForRuntAnalysis();
      return Automaton.SMoutput(states);
    } catch (ParseErrorException e) {
      throw new LTLErrorException("parse error: " + e.getMessage());
    }
  }
}