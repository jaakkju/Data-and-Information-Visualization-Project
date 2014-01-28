package big.marketing.view;

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.MouseResponsiveRenderer;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;

import processing.core.PApplet;
import processing.core.PGraphics;

/*
 * How to use ServiceProviders:
 * create a file in META-INF/services/full.qualified.name.of.service.interface
 * add full qualified name of implementation class to created file:
 * example:
 * create file: META-INF/services/org.gephi.preview.spi.Renderer
 * add to file: big.marketing.view.MouseRenderer
 * 
 * 
 * http://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html
 */

@ServiceProvider(service = Renderer.class)
public class MouseRenderer extends NodeRenderer implements MouseResponsiveRenderer {

	boolean isDragging;
	int x, y, w, h;

	public void startDragging(int x, int y, int w, int h) {
		this.isDragging = true;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void endDragging() {
		isDragging = false;
	}

	@Override
	public boolean needsPreviewMouseListener(PreviewMouseListener previewMouseListener) {
		return previewMouseListener instanceof GraphMouseListener;
	}

	@Override
	public void preProcess(PreviewModel previewModel) {
		// TODO Auto-generated method stub
		super.preProcess(previewModel);
	}

	@Override
	public void renderProcessing(Item item, ProcessingTarget target, PreviewProperties properties) {
		// TODO Auto-generated method stub
		// draw rectangle

		super.renderProcessing(item, target, properties);
		if (isDragging) {
			PGraphics g = target.getGraphics();
			PApplet p = target.getApplet();
			int width = w - x;
			int height = h - y;
			//			System.out.println("Width: " + width + " Height: " + height);
			g.fill(128, 128, 128, 20);
			g.rect(x + width / 2, y + height / 2, width, height);
		}
	}

	@Override
	public String getDisplayName() {
		return "MouseRenderer";
	}
}
