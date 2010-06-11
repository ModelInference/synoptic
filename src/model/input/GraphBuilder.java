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
	public static ISuccessorProvider<MessageEvent> makeSuccessorProvider(final Collection<MessageEvent> successors) {
		return new ISuccessorProvider<MessageEvent>() {
			public IterableIterator<MessageEvent> getSuccessorIterator() {
				return new IterableAdapter<MessageEvent>(successors.iterator());
			}
			public void setTarget(SystemState<MessageEvent> s) {}
			public IterableIterator<MessageEvent> getSuccessorIterator(Action act) {
				Set<MessageEvent> filtered = new HashSet<MessageEvent>();
				for (MessageEvent m : successors)
					if (m.getAction().equals(act))
						filtered.add(m);
				return new IterableAdapter<MessageEvent>(filtered.iterator());
			}
		};
	}
	public static ISuccessorProvider<MessageEvent> makeSuccessorProvider() {
		return makeSuccessorProvider(Collections.<MessageEvent>emptyList());
	}

	
	private Graph<MessageEvent> graph;
	private SystemState<MessageEvent> curState;
	private MessageEvent curMessage;
	private int stateCtr = 0;
	private Action relation = new Action("t");

	public GraphBuilder() {
		curState = new SystemState<MessageEvent>("start");
		curMessage = null;
		graph = new Graph<MessageEvent>();
		// graph.addInitialState(curState);
	}
 
	@Override
	public MessageEvent append(Action act) {
		SystemState<MessageEvent> nextState = new SystemState<MessageEvent>(""
				+ (stateCtr++));
		MessageEvent nextMessage = new MessageEvent(act, curState, nextState, 1);
		curState.addSuccessorProvider(makeSuccessorProvider(Collections.singleton(nextMessage)));
		if (curMessage != null) {
			curMessage.addTransition(nextMessage, relation);
		} else
			graph.addInitial(nextMessage, relation);
		// graph.addState(nextState);
		graph.add(nextMessage);
		curState = nextState;
		curMessage = nextMessage;
		return curMessage;
	}
	
	@Override
	public MessageEvent insertAfter(MessageEvent curMessage, Action act) {
		SystemState<MessageEvent> nextState = new SystemState<MessageEvent>(""
				+ (stateCtr++));
		MessageEvent nextMessage = new MessageEvent(act, curState, nextState, 1);
		curState.addSuccessorProvider(makeSuccessorProvider(Collections.singleton(nextMessage)));
		if (curMessage != null) {
			curMessage.addTransition(nextMessage, relation);
		} else
			graph.addInitial(nextMessage, relation);
		// graph.addState(nextState);
		graph.add(nextMessage);
		curState = nextState;
		curMessage = nextMessage;
		return nextMessage;
	}

	public PartitionGraph getGraph(boolean merge) {
		return new PartitionGraph(graph, merge);
	}
	
	public Graph<MessageEvent> getRawGraph() {
		return graph;
	}

	@Override
	public void split() {
		curState = new SystemState<MessageEvent>("split");
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

	public static PartitionGraph buildGraph(TraceSet traceSet) {
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
		return gb.getGraph(false);
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
		SystemState<MessageEvent> previousState = curState;
		List<PingPongMessage> list = extractCommunicationWith(t, addr);
		HashMap<Link, ArrayList<MessageEvent>> previousR = new HashMap<Link, ArrayList<MessageEvent>>();
		for (int i = 0; i < list.size();) {
			long time = list.get(i).getTimestamp();
			ArrayList<MessageEvent> current = new ArrayList<MessageEvent>();
			SystemState<MessageEvent> currentState = new SystemState<MessageEvent>("");
			// graph.addState(currentState);
			for (int j = i; j < list.size()
					&& time == list.get(j).getTimestamp(); ++j, ++i) {
				MessageEvent m = new MessageEvent(new Action(list.get(j).getType()),
						previousState, currentState, 1);
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
				previousState.addSuccessorProvider(makeSuccessorProvider(Collections.singleton(m)));
			}
			for (MessageEvent prev : previous) {
				for (MessageEvent cur : current) {
					// this blows performace
					prev.addTransition(cur, new Action("t"));
				}
			}
			previous = current;
			previousState = currentState;
		}
		curState = new SystemState<MessageEvent>("");
	}
	@Override
	public MessageEvent insert(Action act) {
		MessageEvent nextMessage = new MessageEvent(act, null, null, 1);
		graph.add(nextMessage);
		return nextMessage;
	}
	@Override
	public void addInitial(MessageEvent curMessage, Action relation) {
		graph.addInitial(curMessage, relation);
		
	}
	@Override
	public void connect(MessageEvent first, MessageEvent second, Action relation) {
		first.addTransition(second, relation);
	}
	@Override
	public void setTerminal(MessageEvent terminalNode) {
			// TODO Auto-generated method stub
			
	}


}
