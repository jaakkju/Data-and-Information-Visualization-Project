package big.marketing.view;

import java.awt.Dimension;
import java.awt.Point;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewMouseEvent.Button;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/*
 * How to use ServiceProviders:
 * create a file in META-INF/services/full.qualified.name.of.service.interface
 * add full qualified name of implementation class to created file:
 * example:
 * create file: META-INF/services/org.gephi.preview.spi.PreviewMouseListener
 * add to file: big.marketing.view.GraphMouseListener
 * 
 * 
 * http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html
 */

@ServiceProvider(service = PreviewMouseListener.class)
public class GraphMouseListener implements PreviewMouseListener {

	static Logger logger = Logger.getLogger(GraphMouseListener.class);
	private int startX, startY, endX, endY;

	// Match Event coordinates with nodes...
	@Override
	public void mouseClicked(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;

		// Triggered when clicked an mouse was not moved
		//		logger.info("CLICK!");
	}

	@Override
	public void mouseDragged(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;
		//		logger.info("DRAG!");
		endX = event.x;
		endY = event.y;
		Lookup.getDefault().lookup(MouseRenderer.class).startDragging(startX, startY, endX, endY);
		event.setConsumed(true);

	}

	@Override
	public void mousePressed(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;
		startX = endX = event.x;
		startY = endY = event.y;
		//		logger.info("PRESS!");

		// only react on left mouse button clicks, other events are passed to other listeners
		event.setConsumed(true);

	}

	@Override
	public void mouseReleased(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;
		//		logger.info("RELEASE!");
		if (startX != endX || startY != endY) {
			logger.info("Dragged from (" + startX + "," + startY + ") to (" + endX + "," + endY + ")");
			Lookup.getDefault().lookup(MouseRenderer.class).endDragging();
		}
		PreviewModel pm = Lookup.getDefault().lookup(PreviewController.class).getModel();

		// These are only updated, if new workspace / data is loaded...
		Point topLeft = pm.getTopLeftPosition();
		Dimension d = pm.getDimensions();

		logger.info("Corner: (" + topLeft.x + "," + topLeft.y + ") (" + d.width + "," + d.height + ")");
		//		try {
		//			GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
		//			Node n = gm.getGraph().getNodes().iterator().next();
		//			Attributes a = n.getAttributes();
		//
		//			System.out.println(a);
		//
		//		} catch (Exception e) {
		//			System.out.println(e);
		//		}
	}

	private boolean isReactiveFor(PreviewMouseEvent event) {
		return event.button == Button.LEFT;
	}

}
