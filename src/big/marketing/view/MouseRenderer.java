package big.marketing.view;

import java.awt.Color;

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.MouseResponsiveRenderer;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.lookup.ServiceProvider;

import processing.core.PApplet;
import processing.core.PGraphics;
import big.marketing.data.Node;

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
	public void render(Item item, RenderTarget target, PreviewProperties properties) {

		org.gephi.graph.api.Node n = (org.gephi.graph.api.Node) item.getSource();
		Object typeValue = n.getAttributes().getValue("hostType");
		if (typeValue == null) {
			// external node
			super.render(item, target, properties);
		} else {
			// internal node
			renderShapeProcessing(item, (ProcessingTarget) target, properties, (Short) typeValue);
		}
	}

	public void renderShapeProcessing(Item item, ProcessingTarget target, PreviewProperties properties, short type) {

		Float x = item.getData(NodeItem.X);
		Float y = item.getData(NodeItem.Y);
		Float size = item.getData(NodeItem.SIZE);

		Color color = item.getData(NodeItem.COLOR);
		Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
		float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
		int alpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
		if (alpha > 255) {
			alpha = 255;
		}

		PGraphics graphics = target.getGraphics();

		if (borderSize > 0) {
			graphics.stroke(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha);
			graphics.strokeWeight(borderSize);
		} else {
			graphics.noStroke();
		}
		graphics.fill(color.getRed(), color.getGreen(), color.getBlue(), alpha);

		switch (type) {
		case Node.TYPE_SERVER:
			graphics.triangle(x - size / 2, y + size / 2, x + size / 2, y + size / 2, x, y - size / 2);
			break;
		case Node.TYPE_WORKSTATION:
			graphics.rect(x, y, size, size);
			break;
		case Node.TYPE_ADMINISTRATOR:
			x -= size / 2;
			y -= size / 2;
			graphics.beginShape();
			{
				graphics.vertex(x, y + size / 2);
				graphics.vertex(x + size / 3, y + size);
				graphics.vertex(x + size / 3 * 2, y + size);
				graphics.vertex(x + size, y + size / 2);
				graphics.vertex(x + size / 3 * 2, y);
				graphics.vertex(x + size / 3, y);
			}
			graphics.endShape();
			break;
		default:
		}
	}

	@Override
	public void renderProcessing(Item item, ProcessingTarget target, PreviewProperties properties) {

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
