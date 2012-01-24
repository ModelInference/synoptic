#!/usr/sbin/dtrace -s

/*
  Purpose:
  ========
  Runs dtrace and captures all method calls and returns within a
  command-line specified Java package. Outputs these to stdout in the
  format:
  """
  c synoptic/main/Main.main.([Ljava/lang/String;)V
  r synoptic/main/Main.main.([Ljava/lang/String;)V
  """

  Usage:
  ======
  ./synflow.d <package_path>

  
  Examples:
  ========
  $ ./synflow.d 'synoptic/'
  $ ./synflow.d 'synoptic/main'


  Relevant articles and source code:
  ==================================
  http://docs.oracle.com/javase/6/docs/technotes/guides/vm/dtrace.html
  http://docs.oracle.com/cd/E19253-01/819-5488/gcglu/index.html
  http://blogs.oracle.com/damien/entry/dtrace_java_methods
  http://web.mit.edu/course/13/13.715/jdk1.6.0_18/sample/dtrace/hotspot/method_invocation_tree.d
*/

#pragma D option quiet
#pragma D option destructive
#pragma D option defaultargs
#pragma D option bufsize=16m
#pragma D option aggrate=100ms

/* Allow strings upto 4k long. */
#pragma D option strsize=4096

self string class_name;
self string method_name;
self string signature;
self int indent;

inline string package_prefix_filter = $1;
inline int package_prefix_len = strlen(package_prefix_filter);

/* Retrieve the method's package name so that we can filter on it
   below. */
hotspot*:::method-entry
{
  self->str_ptr = (char*) copyin(arg1, arg2+1);
  self->str_ptr[arg2] = '\0';
  self->class_name = (string) self->str_ptr;
  self->package_name = dirname(self->class_name);
}

/* Retrieve the method's package name so that we can filter on it
   below. */
hotspot*:::method-return
{
  self->str_ptr = (char*) copyin(arg1, arg2+1);
  self->str_ptr[arg2] = '\0';
  self->class_name = (string) self->str_ptr;
  self->package_name = dirname(self->class_name);
}

/****************************************************************/

/* Print out method ENTRY information for package methods only. */
hotspot*:::method-entry
/substr(self->package_name,0,package_prefix_len) == package_prefix_filter/
{
  self->str_ptr = (char*) copyin(arg1, arg2+1);
  self->str_ptr[arg2] = '\0';
  self->class_name = (string) self->str_ptr;

  self->str_ptr = (char*) copyin(arg3, arg4+1);
  self->str_ptr[arg4] = '\0';
  self->method_name = (string) self->str_ptr;

  /* 
     For some unknown reason, this next code snippet does not work for
     extracting the method's signature. Instead, we use copyinstr().
  */
  /*
    self->str_ptr = (char*) copyin(arg5, arg6+1);
    self->str_ptr[arg6] = '\0';
  */

  self->signature = copyinstr(arg5); 

  /*
    self->indent++;
    printf("%*s %s %s.%s.%s\n",
	 self->indent, "", "->", self->class_name,
	 self->method_name, self->signature);
  */
  printf("c %s.%s.%s\n",
	 self->class_name, self->method_name, self->signature);
}



/* Print out method RETURN information for package methods only. */
hotspot*:::method-return
/substr(self->package_name,0,package_prefix_len) == package_prefix_filter/
{
  self->str_ptr = (char*) copyin(arg1, arg2+1);
  self->str_ptr[arg2] = '\0';
  self->class_name = (string) self->str_ptr;

  self->str_ptr = (char*) copyin(arg3, arg4+1);
  self->str_ptr[arg4] = '\0';
  self->method_name = (string) self->str_ptr;

  self->signature = copyinstr(arg5); 

  /*
  printf("%*s %s %s.%s.%s\n",
	 self->indent, "", "->", self->class_name,
	 self->method_name, self->signature);
  self->indent--;
  */

  printf("r %s.%s.%s\n",
	 self->class_name, self->method_name, self->signature);
}
