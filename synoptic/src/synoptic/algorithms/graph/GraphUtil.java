package synoptic.algorithms.graph;

import java.util.HashMap;

import synoptic.model.Action;
import synoptic.model.input.IBuilder;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.model.nets.Event;
import synoptic.model.nets.Net;

/**
 * Utility procedures for the graph.
 * 
 * @author Sigurd Schneider
 */
public class GraphUtil {
    /**
     * Copy a graph to a builder.
     * 
     * @param <T>
     *            the node type of the graph
     * @param <U>
     *            the node type of the builder
     * @param graph
     *            the graph to copy from
     * @param builder
     *            the builder to write to
     */
    public static <T extends INode<T>, U> void copyTo(IGraph<T> graph,
            IBuilder<U> builder) {
        HashMap<T, U> map = new HashMap<T, U>();
        for (T node : graph.getNodes()) {
            map.put(node, builder.insert(new Action(node.getLabel())));
        }
        for (T node : graph.getNodes()) {
            boolean foundTransition = false;
            for (ITransition<T> t : node.getTransitionsIterator()) {
                foundTransition = true;
                builder.connect(map.get(t.getSource()), map.get(t.getTarget()),
                        t.getRelation());
            }
            if (!foundTransition) {
                builder.setTerminal(map.get(node));
            }
        }
    }

    /**
     * Copy the reverse graph to a Builder.
     * 
     * @param <T>
     *            node type of the graph
     * @param <U>
     *            node type of the builder
     * @param graph
     *            the graph to read from
     * @param builder
     *            the builder to write to
     */
    public static <T extends INode<T>, U> void copyReverseTo(IGraph<T> graph,
            IBuilder<U> builder) {
        HashMap<T, U> map = new HashMap<T, U>();
        for (T node : graph.getNodes()) {
            map.put(node, builder.insert(new Action(node.getLabel())));
        }
        for (T node : graph.getNodes()) {
            boolean foundTransition = false;
            for (ITransition<T> t : node.getTransitionsIterator()) {
                foundTransition = true;
                builder.connect(map.get(t.getTarget()), map.get(t.getSource()),
                        t.getRelation());
            }
            if (!foundTransition) {
                builder.setTerminal(map.get(node));
            }
        }
    }

    /**
     * Copies a net to a graph builder.
     * 
     * @param <T>
     *            the node type
     * @param net
     *            the net to copy from
     * @param gBuilder
     *            the builder to write to
     */
    public static <T> void copyNetTo(Net net, IBuilder<T> gBuilder) {
        HashMap<Event, T> map = new HashMap<Event, T>();
        String relation = "";
        for (Event e : net.getEvents()) {
            T t = gBuilder.insert(new Action(e.getName()));
            map.put(e, t);
            if (e.getPostEvents().size() == 0) {
                gBuilder.setTerminal(t);
            }
            if (net.getPreEvents(e).size() == 0) {
                gBuilder.addInitial(t, relation);
            }
        }
        for (Event e : net.getEvents()) {
            for (Event f : e.getPostEvents()) {
                gBuilder.connect(map.get(e), map.get(f), relation);
            }
        }
    }

}
