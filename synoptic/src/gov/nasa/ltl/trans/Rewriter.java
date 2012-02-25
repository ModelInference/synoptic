
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;


/**
 * DOCUMENT ME!
 */
public class Rewriter {
  public static Formula applyRule (Formula expr, Formula rule,
                                   Formula rewritten) {
    return expr.rewrite(rule, rewritten);
  }

  public static void main (String[] args) {
    int osize = 0;
    int rsize = 0;

    try {
      if (args.length != 0) {
        for (int i = 0; i < args.length; i++) {
          Formula f = Formula.parse(args[i]);

          osize += f.size();
          System.out.println(f = rewrite(f));
          rsize += f.size();

          System.err.println(((rsize * 100) / osize) + "% (" + osize +
                             " => " + rsize + ")");
        }
      } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
          try {
            String line = in.readLine();

            if (line == null) {
              break;
            }

            if (line.equals("")) {
              continue;
            }

            Formula f = Formula.parse(line);

            osize += f.size();
            System.out.println(f = rewrite(f));
            rsize += f.size();

            System.err.println(((rsize * 100) / osize) + "% (" + osize +
                               " => " + rsize + ")");
          } catch (IOException e) {
            System.out.println("error");

            break;
          }
        }
      }
    } catch (ParseErrorException e) {
      System.err.println("parse error: " + e.getMessage());
    }
  }

  public static Formula[] readRules () {
    Formula[] rules = new Formula[0];

    try {
      // Modified by ckong - Sept 7, 2001

      /*
         FileReader fr = null;

               for(int i = 0, l = ClassPath.length(); i < l; i++)
           try {
             fr = new FileReader(ClassPath.get(i) + File.separator + "gov.nasa.ltl.trans.rules".replace('.', File.separatorChar));
           } catch(FileNotFoundException e) {
           }

               if(fr == null) {
           try {
             fr = new FileReader("rules");
           } catch(FileNotFoundException e) {
           }
               }

               if(fr == null) return null;

               BufferedReader in = new BufferedReader(fr);
       */
      BufferedReader in = new BufferedReader(
                                new StringReader(RulesClass.getRules()));

      while (true) {
        String line = in.readLine();

        if (line == null) {
          break;
        }

        if (line.equals("")) {
          continue;
        }

        Formula   rule = Formula.parse(line);

        Formula[] n = new Formula[rules.length + 1];
        System.arraycopy(rules, 0, n, 0, rules.length);
        n[rules.length] = rule;
        rules = n;
      }
    } catch (IOException e) {
    	// ignore
    } catch (ParseErrorException e) {
      System.err.println("parse error: " + e.getMessage());
      System.exit(1);
    }

    return rules;
  }

  public static String rewrite (String expr) throws ParseErrorException {
    try {
      //   	System.out.println("String is: " + expr);
      Formula formula = Formula.parse(expr);

      //    	System.out.println("And after parsing " + formula.toString());
      return rewrite(formula).toString();
    } catch (ParseErrorException e) {
      throw new ParseErrorException(e.getMessage());
    }
  }

  public static Formula rewrite (Formula expr) throws ParseErrorException {
    //  	System.out.println("testing if gets in here");
    Formula[] rules = readRules();

    if (rules == null) {
      return expr;
    }

    try {
      boolean negated = false;
      boolean changed;

      do {
        Formula old;
        changed = false;

        do {
          old = expr;

          for (int i = 0; i < rules.length; i += 2) {
            expr = applyRule(expr, rules[i], rules[i + 1]);
          }

          if (old != expr) {
            changed = true;
          }
        } while (old != expr);

        negated = !negated;
        expr = Formula.parse("!" + expr.toString());
      } while (changed || negated);

      return expr;
    } catch (ParseErrorException e) {
      throw new ParseErrorException(e.getMessage());
    }
  }
}