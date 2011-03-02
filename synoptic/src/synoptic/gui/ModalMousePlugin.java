package synoptic.gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import synoptic.model.Partition;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

/**
 * Extension of EditingModalGraphMouse which maps transforming mode to right mouse clicks, picking
 * mode to left mouse clicks, and removes editing mode functionality
 * 
 */
public class ModalMousePlugin extends EditingModalGraphMouse<INode<Partition>, ITransition<Partition>> {

	public ModalMousePlugin(RenderContext <INode<Partition>, ITransition<Partition>> rc) {
		super(rc, null, null);
	}

   protected void loadPlugins() {
	   add(new PickingGraphMousePlugin<INode<Partition>, ITransition<Partition>>());
       add(new AnimatedPickingGraphMousePlugin<INode<Partition>, ITransition<Partition>>());
       add(new TranslatingGraphMousePlugin(InputEvent.BUTTON3_MASK));
       add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0));
       add(new RotatingGraphMousePlugin(MouseEvent.BUTTON3_MASK | MouseEvent.SHIFT_MASK));
       add(new ShearingGraphMousePlugin(MouseEvent.BUTTON3_MASK | MouseEvent.CTRL_MASK));
   }

}
