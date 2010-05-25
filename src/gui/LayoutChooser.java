package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

class LayoutChooser implements ActionListener {
	static final Class[] constructorArgsWanted = { Graph.class };

	private final JComboBox jcb;
	private final Graph g;
	private final VisualizationViewer vv;

	private LayoutChooser(JComboBox jcb, Graph g, VisualizationViewer gd) {
		super();
		this.jcb = jcb;
		this.g = g;
		this.vv = gd;
	}

	public void actionPerformed(ActionEvent arg0) {
		Object[] constructorArgs = { g };

		Class layoutC = (Class) jcb.getSelectedItem();
		Class lay = layoutC;
		try {
			Constructor constructor = lay.getConstructor(constructorArgsWanted);
			Object o = constructor.newInstance(constructorArgs);
			Layout l = (Layout) o;
			vv.setGraphLayout(l);
		} catch (Exception e) {
			throw new RuntimeException("Could not load layout " + lay);
		}
	}

	public static void addLayoutCombo(JPanel panel, Graph g, VisualizationViewer vv) {
		final JComboBox jcb = new JComboBox(getCombos());
		jcb.setSelectedItem(FRLayout.class);
		jcb.addActionListener(new LayoutChooser(jcb, g, vv));
		panel.add(jcb, BorderLayout.NORTH);
	}

	private static Vector<Class> getCombos() {
		Vector<Class> layouts = new Vector<Class>();
		layouts.add(KKLayout.class);
		layouts.add(FRLayout.class);
		layouts.add(CircleLayout.class);
		layouts.add(SpringLayout.class);
		layouts.add(ISOMLayout.class);
		return layouts;
	}
}
