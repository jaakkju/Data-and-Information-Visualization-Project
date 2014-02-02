package big.marketing.view.gephi;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewMouseEvent.Button;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import big.marketing.controller.gephi.GephiController;

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

	@Override
	public void mouseClicked(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		return;
	}

	@Override
	public void mouseDragged(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;
		if (startX == endX && startY == endY) {
			Lookup.getDefault().lookup(MouseRenderer.class).startDragging(startX, startY);
		}
		endX = event.x;
		endY = event.y;
		Lookup.getDefault().lookup(MouseRenderer.class).dragStep(endX, endY);

		event.setConsumed(true);

	}

	@Override
	public void mousePressed(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;
		startX = endX = event.x;
		startY = endY = event.y;

		event.setConsumed(true);

	}

	@Override
	public void mouseReleased(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		if (!isReactiveFor(event))
			return;

		if (startX != endX || startY != endY) {
			logger.info("Dragged from (" + startX + "," + startY + ") to (" + endX + "," + endY + ")");
			Lookup.getDefault().lookup(MouseRenderer.class).endDragging();

			gc.selectNodesFromCoords(startX, startY, endX, endY);

		} else {
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
