package big.marketing.view;

import org.gephi.preview.api.PreviewMouseEvent;
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

	@Override
	public void mouseClicked(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(PreviewMouseEvent event, PreviewProperties properties, Workspace workspace) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
