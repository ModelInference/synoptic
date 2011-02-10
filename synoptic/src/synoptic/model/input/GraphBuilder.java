package synoptic.model.input;

import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.PartitionGraph;

/**
 * Used to build a graph composed of nodes that are LogEvent. Essentially
 * converts a set of traces into graph form.
 */
public class GraphBuilder implements IBuilder<LogEvent> {
    /**
     * The graph this GraphBuilder will gradually build up.
     */
    private final Graph<LogEvent> graph;

    /**
     * The last event that was added to the graph.
     */
    private LogEvent prevEvent;

    /**
     * The default relation to use when composing the graph.
     */
    private static final String defaultRelation = "t".intern();

    /**
     * Generate a new, empty graph.
     */
    public GraphBuilder() {
        prevEvent = null;
        graph = new Graph<LogEvent>();
    }

    /**
     * Add a node
     */
    @Override
    public LogEvent append(Action act) {
        LogEvent nextMessage = new LogEvent(act);
        if (prevEvent != null) {
            prevEvent.addTransition(nextMessage, defaultRelation);
        } else {
            // This is the first node in the graph. Tag it as initial.
            graph.tagInitial(nextMessage, defaultRelation);
        }
        graph.add(nextMessage);
        prevEvent = nextMessage;
        return prevEvent;
    }

    /**
     * Creates and returns a partition graph.
     * 
     * @param merge
     * @return
     */
    public PartitionGraph getPartitionGraph(boolean merge) {
        return new PartitionGraph(graph, merge);
    }

    /**
     * Return the graph as it was built.
     * 
     * @return the graph as it was built.
     */
    public Graph<LogEvent> getGraph() {
        return graph;
    }

    /**
     * Create a new partition by "spitting" off whatever we've build up so far.
     * NOTE: this is the ONLY way that nodes are marked as terminals, therefore
     * the last partition must also be split if its last event is intended to be
     * a terminal event.
     */
    @Override
    public void split() {
        // We split, or create a new partition by losing information about
        // the last node, which we can now tag as terminal.
        assert (prevEvent != null);
        // Mark last node in the partition as terminal w.r.t all relations
        // it is a part of.
        for (String relation : prevEvent.getRelations()) {
            graph.tagTerminal(prevEvent, relation);
        }
        prevEvent = null;
    }

    public static PartitionGraph buildGraph(String[] trace) {
        GraphBuilder gb = new GraphBuilder();

        for (String t : trace) {
            gb.append(new Action(t));
        }
        return gb.getPartitionGraph(false);
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
        return gb.getPartitionGraph(false);
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

    @Override
    public LogEvent insert(Action act) {
        LogEvent nextMessage = new LogEvent(act);
        graph.add(nextMessage);
        return nextMessage;
    }

    @Override
    public void tagInitial(LogEvent initialNode, String relation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect(LogEvent first, LogEvent second, String relation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tagTerminal(LogEvent terminalNode) {
        // TODO Auto-generated method stub

    }

    // This code is used to interface with ProtoBuf formatted messages
    // TODO: refactor into a separate library
    //
    // Public Static Partitiongraph Buildgraph(Traceset Traceset, Boolean Merge)
    // {
    // Graphbuilder Gb = New Graphbuilder();
    // For (Fulltrace T : Traceset.Getfulltracelist()) {
    // For (Wrappedmessage M : T.Getwrappedmessagelist()) {
    // Action A = New Action("Error");
    // If (M.Gettype().Equals("Url Get")
    // || M.Gettype().Equals("Url Post")) {
    // Url P;
    // Try {
    // P = Url.Parsefrom(M.Getthemessage());
    // A = New Action(P.Getpath());
    // } Catch (Invalidprotocolbufferexception E) {
    // // Todo Auto-generated Catch Block
    // E.Printstacktrace();
    // }
    // } Else {
    // A = New Action(M.Gettype());
    // }
    // // Todo Replace Action With A Wrappedmessagetype
    // Gb.Append(A);
    // }
    // Gb.Split();
    // }
    // Return Gb.Getgraph(Merge);
    // }
    //
    // Public Arraylist<Pingpongmessage> Extractcommunicationwith(Trace T, Int
    // Addr) {
    // Arraylist<Pingpongmessage> Result = New Arraylist<Pingpongmessage>();
    // For (Pingpongmessage P : T.Getpingpongmessagelist()) {
    // If (P.Getsrc() == Addr || P.Getdst() == Addr) {
    // Result.Add(P);
    // }
    // }
    // Return Result;
    // }
    //
    // Public Void Buildgraph(Trace T, Int Addr) {
    // Arraylist<Messageevent> Previous = New Arraylist<Messageevent>();
    // List<Pingpongmessage> List = Extractcommunicationwith(T, Addr);
    // Hashmap<Link, Arraylist<Messageevent>> Previousr = New Hashmap<Link,
    // Arraylist<Messageevent>>();
    // For (Int I = 0; I < List.Size();) {
    // Long Time = List.Get(I).Gettimestamp();
    // Arraylist<Messageevent> Current = New Arraylist<Messageevent>();
    // // Graph.Addstate(Currentstate);
    // For (Int J = I; J < List.Size()
    // && Time == List.Get(J).Gettimestamp(); ++J, ++I) {
    // Messageevent M = New Messageevent(New Action(List.Get(J)
    // .Gettype()), 1);
    // Graph.Add(M);
    // Pingpongmessage Org = List.Get(J);
    // Link L = New Link(Org.Getsrc(), Org.Getdst());
    // Arraylist<Messageevent> Initials = Previousr.Get(L
    // .Getresponselink());
    // If (Initials != Null && Initials.Size() > 0) {
    // // For (Message Im : Initials) {
    // // Im.Addtransition(M, New Action("R"));
    // // }
    // Initials.Clear();
    // } /*
    // * Else Graph.Addinitial(Previousstate);
    // */
    // Initials = Previousr.Get(L);
    // If (Initials == Null) {
    // Previousr.Put(L, New Arraylist<Messageevent>());
    // }
    // Previousr.Get(L).Add(M);
    // Current.Add(M);
    // }
    // For (Messageevent Prev : Previous) {
    // For (Messageevent Cur : Current) {
    // // This Blows Performace
    // Prev.Addtransition(Cur, Defaultrelation);
    // }
    // }
    // Previous = Current;
    // }
    // }
}
