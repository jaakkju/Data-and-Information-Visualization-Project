package big.marketing.view;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewMouseEvent.Button;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.project.api.Workspace;
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

	@Override
	public void mouseClicked(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// Triggered when clicked an mouse was not moved
		logger.info("CLICK!");
	}

	@Override
	public void mouseDragged(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		logger.info("DRAG!");

	}

	@Override
	public void mousePressed(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// This is triggered
		logger.info("PRESS!");

		// only react on left mouse button clicks, other events are passed to other listeners
		if (event.button == Button.LEFT) {
			event.setConsumed(true);
		}

	}

	@Override
	public void mouseReleased(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		logger.info("RELEASE!");

	}

}
