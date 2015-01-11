/* --------------------------------------------------
   "the" Alternating Bit Protocol

   as presented by Tristan Le Gall and Bertrand Jeannet
   in the proceedings of AMAST06 
   ------------------------------------------------- */


scm ABP :


nb_channels = 3 ;
/* 
channel K -> 0
channel L -> 1
channel C -> 2
*/
/* lossy : 0,1 */

parameters : 
real o ; /* o stands for 0 */
real i ; /* i stands for 1 */
real M ; /* message sent */

automaton sender :

initial : 0

state 0 :
to 1 :  when true , 2 ! M  ;

state 1 :
to 1 :  when true , 0 ! o ;
to 1 :  when true , 1 ? i ;
to 2 :  when true , 1 ? o ;

state 2 :
to 3 :  when true , 2 ! M  ;

state 3 :
to 3 :  when true , 0 ! i ;
to 3 :  when true , 1 ? o ;
to 0 :  when true , 1 ? i ;


automaton receiver :

initial : 0

state 0 :
to 1 :  when true , 1 ! i ;
to 0 :  when true , 0 ? i ;
to 1 :  when true , 0 ? o ;

state 1 :
to 2 :  when true , 2 ? M  ;
to 3 :  when true , 1 ! i ;


state 2 :
to 2 :  when true , 1 ! o ;
to 2 :  when true , 0 ? o ;
to 3 :  when true , 0 ? i ;

state 3 :
to 0 :  when true , 2 ? M  ;


bad_states:
(automaton sender: in 0: true
automaton receiver: in 3: true) 
