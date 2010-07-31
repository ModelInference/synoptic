package model.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.protobuf.InvalidProtocolBufferException;

import net.unto.twitter.UtilProtos.Url;

import model.Action;
import model.Graph;
import model.MessageEvent;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.ISuccessorProvider;
import trace.MessageTrace.FullTrace;
import trace.MessageTrace.TraceSet;
import trace.MessageTrace.WrappedMessage;
import trace.ProtoTrace.PingPongMessage;
import trace.ProtoTrace.Trace;
import util.IterableAdapter;
import util.IterableIterator;

public class GraphBuilder implements IBuilder<MessageEvent> {
	private Graph<MessageEvent> graph;
	private MessageEvent curMessage;
	private int stateCtr = 0;
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

	public static PartitionGraph buildGraph(TraceSet traceSet, boolean merge) {
		GraphBuilder gb = new GraphBuilder();
		for (FullTrace t : traceSet.getFullTraceList()) {
			for (WrappedMessage m : t.getWrappedMessageList()) {
				Action a = new Action("error");
				if (m.getType().equals("Url get")
						|| m.getType().equals("Url post")) {
					Url p;
					try {
						p = Url.parseFrom(m.getTheMessage());
						a = new Action(p.getPath());
					} catch (InvalidProtocolBufferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					a = new Action(m.getType());
				}
				// TODO replace action with a WrappedMessageType
				gb.append(a);
			}
			gb.split();
		}
		return gb.getGraph(merge);
	}

	public ArrayList<PingPongMessage> extractCommunicationWith(Trace t, int addr) {
		ArrayList<PingPongMessage> result = new ArrayList<PingPongMessage>();
		for (PingPongMessage p : t.getPingPongMessageList()) {
			if (p.getSrc() == addr || p.getDst() == addr) {
				result.add(p);
			}
		}
		return result;
	}

	public void buildGraph(Trace t, int addr) {
		ArrayList<MessageEvent> previous = new ArrayList<MessageEvent>();
		List<PingPongMessage> list = extractCommunicationWith(t, addr);
		HashMap<Link, ArrayList<MessageEvent>> previousR = new HashMap<Link, ArrayList<MessageEvent>>();
		for (int i = 0; i < list.size();) {
			long time = list.get(i).getTimestamp();
			ArrayList<MessageEvent> current = new ArrayList<MessageEvent>();
			// graph.addState(currentState);
			for (int j = i; j < list.size()
					&& time == list.get(j).getTimestamp(); ++j, ++i) {
				MessageEvent m = new MessageEvent(new Action(list.get(j).getType()), 1);
				graph.add(m);
				PingPongMessage org = list.get(j);
				Link l = new Link(org.getSrc(), org.getDst());
				ArrayList<MessageEvent> initials = previousR
						.get(l.getResponseLink());
				if (initials != null && initials.size() > 0) {
					//for (Message im : initials) {
						// im.addTransition(m, new Action("r"));
					//}
					initials.clear();
				} /*
				 * else graph.addInitial(previousState);
				 */
				initials = previousR.get(l);
				if (initials == null) {
					previousR.put(l, new ArrayList<MessageEvent>());
				}
				previousR.get(l).add(m);
				current.add(m);
			}
			for (MessageEvent prev : previous) {
				for (MessageEvent cur : current) {
					// this blows performace
					prev.addTransition(cur, defaultRelation);
				}
			}
			previous = current;
		}
	}
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
