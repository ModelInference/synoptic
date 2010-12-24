package model.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import com.google.protobuf.InvalidProtocolBufferException;

//import net.unto.twitter.UtilProtos.Url;

import model.Action;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
//import trace.MessageTrace.FullTrace;
//import trace.MessageTrace.TraceSet;
//import trace.MessageTrace.WrappedMessage;
//import trace.ProtoTrace.PingPongMessage;
//import trace.ProtoTrace.Trace;
import util.IterableAdapter;
import util.IterableIterator;

public class GraphBuilder implements IBuilder<MessageEvent> {
	private Graph<MessageEvent> graph;
	private MessageEvent curMessage;
	private static final String defaultRelation = "t".intern();

	public GraphBuilder() {
		curMessage = null;
		graph = new Graph<MessageEvent>();
		// graph.addInitialState(curState);
	}

	@Override
	public MessageEvent append(Action act) {
		MessageEvent nextMessage = new MessageEvent(act, 1);
		if (curMessage != null) {
			curMessage.addTransition(nextMessage, defaultRelation);
		} else
			graph.addInitial(nextMessage, defaultRelation);
		// graph.addState(nextState);
		graph.add(nextMessage);
		curMessage = nextMessage;
		return curMessage;
	}

	@Override
	public MessageEvent insertAfter(MessageEvent curMessage, Action act) {
		MessageEvent nextMessage = new MessageEvent(act, 1);
		if (curMessage != null) {
			curMessage.addTransition(nextMessage, defaultRelation);
		} else
			graph.addInitial(nextMessage, defaultRelation);
		// graph.addState(nextState);
		graph.add(nextMessage);
		curMessage = nextMessage;
		return nextMessage;
	}

	public PartitionGraph getGraph(boolean merge) {
		return new PartitionGraph(graph, merge);
	}

	/**
	 * Return the graph as it was built.
	 * 
	 * @return the graph as it was built.
	 */
	public Graph<MessageEvent> getRawGraph() {
		return graph;
	}

	@Override
	public void split() {
		curMessage = null;
		// graph.addInitial(curState);
	}

	public static PartitionGraph buildGraph(String[] trace) {
		GraphBuilder gb = new GraphBuilder();

		for (String t : trace) {
			gb.append(new Action(t));
		}
		return gb.getGraph(false);
	}

	public static PartitionGraph buildGraph(String[][] traces) {
		GraphBuilder gb = new GraphBuilder();
		for (int i = 0; i < traces.length; ++i) {
			for (String t : traces[i]) {
				gb.append(new Action(t));
			}
			if (i != traces.length - 1) {
				gb.split();
			}
		}
		return gb.getGraph(false);
	}

	public void buildGraphLocal(String[][] traces) {
		for (int i = 0; i < traces.length; ++i) {
			for (String t : traces[i]) {
				append(new Action(t));
			}
			if (i != traces.length - 1) {
				split();
			}
		}
	}

	

// This code is used to interface with ProtoBuf formatted messages
// TODO: refactor into a separate library
//
//	Public Static Partitiongraph Buildgraph(Traceset Traceset, Boolean Merge) {
//		Graphbuilder Gb = New Graphbuilder();
//		For (Fulltrace T : Traceset.Getfulltracelist()) {
//			For (Wrappedmessage M : T.Getwrappedmessagelist()) {
//				Action A = New Action("Error");
//				If (M.Gettype().Equals("Url Get")
//						|| M.Gettype().Equals("Url Post")) {
//					Url P;
//					Try {
//						P = Url.Parsefrom(M.Getthemessage());
//						A = New Action(P.Getpath());
//					} Catch (Invalidprotocolbufferexception E) {
//						// Todo Auto-generated Catch Block
//						E.Printstacktrace();
//					}
//				} Else {
//					A = New Action(M.Gettype());
//				}
//				// Todo Replace Action With A Wrappedmessagetype
//				Gb.Append(A);
//			}
//			Gb.Split();
//		}
//		Return Gb.Getgraph(Merge);
//	}
//
//	Public Arraylist<Pingpongmessage> Extractcommunicationwith(Trace T, Int Addr) {
//		Arraylist<Pingpongmessage> Result = New Arraylist<Pingpongmessage>();
//		For (Pingpongmessage P : T.Getpingpongmessagelist()) {
//			If (P.Getsrc() == Addr || P.Getdst() == Addr) {
//				Result.Add(P);
//			}
//		}
//		Return Result;
//	}
//
//	Public Void Buildgraph(Trace T, Int Addr) {
//		Arraylist<Messageevent> Previous = New Arraylist<Messageevent>();
//		List<Pingpongmessage> List = Extractcommunicationwith(T, Addr);
//		Hashmap<Link, Arraylist<Messageevent>> Previousr = New Hashmap<Link, Arraylist<Messageevent>>();
//		For (Int I = 0; I < List.Size();) {
//			Long Time = List.Get(I).Gettimestamp();
//			Arraylist<Messageevent> Current = New Arraylist<Messageevent>();
//			// Graph.Addstate(Currentstate);
//			For (Int J = I; J < List.Size()
//					&& Time == List.Get(J).Gettimestamp(); ++J, ++I) {
//				Messageevent M = New Messageevent(New Action(List.Get(J)
//						.Gettype()), 1);
//				Graph.Add(M);
//				Pingpongmessage Org = List.Get(J);
//				Link L = New Link(Org.Getsrc(), Org.Getdst());
//				Arraylist<Messageevent> Initials = Previousr.Get(L
//						.Getresponselink());
//				If (Initials != Null && Initials.Size() > 0) {
//					// For (Message Im : Initials) {
//					// Im.Addtransition(M, New Action("R"));
//					// }
//					Initials.Clear();
//				} /*
//				 * Else Graph.Addinitial(Previousstate);
//				 */
//				Initials = Previousr.Get(L);
//				If (Initials == Null) {
//					Previousr.Put(L, New Arraylist<Messageevent>());
//				}
//				Previousr.Get(L).Add(M);
//				Current.Add(M);
//			}
//			For (Messageevent Prev : Previous) {
//				For (Messageevent Cur : Current) {
//					// This Blows Performace
//					Prev.Addtransition(Cur, Defaultrelation);
//				}
//			}
//			Previous = Current;
//		}
//	}

	@Override
	public MessageEvent insert(Action act) {
		MessageEvent nextMessage = new MessageEvent(act, 1);
		graph.add(nextMessage);
		return nextMessage;
	}

	@Override
	public void addInitial(MessageEvent curMessage, String relation) {
		graph.addInitial(curMessage, relation);

	}

	@Override
	public void connect(MessageEvent first, MessageEvent second, String relation) {
		first.addTransition(second, relation);
	}

	@Override
	public void setTerminal(MessageEvent terminalNode) {
		// TODO Auto-generated method stub

	}

}
