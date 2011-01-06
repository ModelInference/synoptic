
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

/**
 * DOCUMENT ME!
 */
class Pool {
  private static int     last_assigned = 0;
  private static boolean stopped = false;

  public static int assign () {
    if (!stopped) {
      //			System.out.println("Value of last_assigned " +last_assigned);
      return (last_assigned++);
    } else {
      //			System.out.println("Value of last_assigned " + last_assigned);
      return last_assigned;
    }
  }

  public static void reset_static () {
    last_assigned = 0;
    stopped = false;
  }

  public static void stop () {
    stopped = true;
    last_assigned--;
  }
}