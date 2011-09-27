package synoptic.gui;

import java.awt.event.InputEvent;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

import synoptic.model.Partition;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * Extension of EditingModalGraphMouse which maps transforming mode to right
 * mouse clicks, picking mode to left mouse clicks, and removes editing mode
 * functionality
 * 
 * @author jenny
 */
public class ModalMousePlugin extends
        EditingModalGraphMouse<INode<Partition>, ITransition<Partition>> {

    /**
     * Constructs a new ModalMousePlugin with the given RenderContext
     * 
     * @param rc
     *            the RenderContext for this MousePlugin
     */
    public ModalMousePlugin(
            RenderContext<INode<Partition>, ITransition<Partition>> rc) {
        super(rc, null, null);
    }

    /*
     * Provides the following unique functionality to this extension of the
     * EditingModalGraphMouse: -Transforming mode corresponds to right mouse
     * clicks -Picking mode corresponds to left mouse clicks -Editing mode is
     * not supported
     */
    @Override
    protected void loadPlugins() {
        add(new PickingGraphMousePlugin<INode<Partition>, ITransition<Partition>>());
        add(new AnimatedPickingGraphMousePlugin<INode<Partition>, ITransition<Partition>>());
        add(new TranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));
        add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0));
        add(new RotatingGraphMousePlugin(InputEvent.BUTTON3_MASK
                | InputEvent.SHIFT_MASK));
        add(new ShearingGraphMousePlugin(InputEvent.BUTTON3_MASK
                | InputEvent.CTRL_MASK));
    }
}
