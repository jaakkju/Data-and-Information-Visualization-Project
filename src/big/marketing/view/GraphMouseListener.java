package big.marketing.view;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewMouseEvent.Button;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import big.marketing.controller.GephiController;

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
	private GephiController gc;

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

			gc.selectNodesFromCoords(startX, startY, endX, endY);

		} else {
			logger.info("Clicked on node");
			gc.showNodeInfo(startX, startY);

		}

	}

	private boolean isReactiveFor(PreviewMouseEvent event) {
		return event.button == Button.LEFT;
	}

	public void setGephiController(GephiController gc) {
		this.gc = gc;
	}

	public void mouseMoved(float x, float y) {

		gc.showNodeInfo(x, y);

	}

}
