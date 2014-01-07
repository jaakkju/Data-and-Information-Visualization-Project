package big.marketing.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.types.DependantOriginalColor;

import processing.core.PApplet;
import big.marketing.controller.DataController;

public class GraphJPanel extends JPanel implements Observer {
	static Logger logger = Logger.getLogger(GraphJPanel.class);

	private static final long serialVersionUID = -7417639995072699909L;
	private final DataController controller;
	private PApplet applet;
	private ProcessingTarget target;

	public void setContent(ProcessingTarget target) {
		this.target = target;
		applet = target.getApplet();
		applet.init();
		removeAll();
		add(applet, BorderLayout.CENTER);
		controller.getGephiController().render(target);
	}

	public GraphJPanel(DataController controller) {
		this.controller = controller;
		setLayout(new BorderLayout());
		this.controller.getGephiController().setGraphPanel(this);
	}

	public void setupModel(PreviewModel previewModel) {
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.FALSE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 50);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
		previewModel.getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, Color.BLACK);
	}

	// TODO: use update(...), not prepareModel and setTarget
	@Override
	public void update(Observable o, Object arg) {

		if (target != null) {
			target.refresh();
			target.resetZoom();
		}
	}

}
