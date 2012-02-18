
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
package gov.nasa.ltl.trans;

/**
 * DOCUMENT ME!
 * @author ckong - Sept 7, 2001
 */
public class RulesClass {
  public static String getRules () {
    return "p/\\p\n" + "p\n\n" + "p/\\true\n" + "p\n\n" + "p/\\false\n" + 
           "false\n\n" + "p/\\!p\n" + "false\n\n" + "p\\/p\n" + "p\n\n" + 
           "p\\/true\n" + "true\n\n" + "p\\/false\n" + "p\n\n" + "p\\/!p\n" + 
           "true\n\n" + "( X p ) U ( X q )\n" + "X ( p U q )\n\n" + 
           "( p V q ) /\\ ( p V r )\n" + "p V ( q /\\ r )\n\n" + 
           "( p V r ) \\/ ( q V r )\n" + "( p \\/ q ) V r\n\n" + 
           "( X p ) /\\ ( X q )\n" + "X ( p /\\ q )\n\n" + "X true\n" + 
           "true\n\n" + "p U false\n" + "false\n\n" + 
           "[] <> p \\/ [] <> q\n" + "[] <> ( p \\/ q )\n\n" + "<> X p\n" + 
           "X <> p\n\n" + "[] [] <> p\n" + "[] <> p\n\n" + "<> [] <> p\n" + 
           "[] <> p\n\n" + "X [] <> p\n" + "[] <> p\n\n" + 
           "<> ( p /\\ [] <> q )\n" + "( <> p ) /\\ ( [] <> q )\n\n" + 
           "[] ( p \\/ [] <> q )\n" + "( [] p ) \\/ ( [] <> q )\n\n" + 
           "X ( p /\\ [] <> q )\n" + "( X p ) /\\ ( [] <> q )\n\n" + 
           "X ( p \\/ [] <> q )\n" + "( X p ) \\/ ( [] <> q )";
  }

  public static void main (String[] args) {
    System.out.println(getRules());
  }
}