package big.marketing.view;

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.MouseResponsiveRenderer;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;

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
public class MouseRenderer extends NodeRenderer implements Renderer, MouseResponsiveRenderer {

	@Override
	public boolean needsPreviewMouseListener(PreviewMouseListener previewMouseListener) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "MouseRenderer";
	}

	@Override
	public PreviewProperty[] getProperties() {
		return super.getProperties();
	}

	@Override
	public boolean isRendererForitem(Item item, PreviewProperties properties) {
		return super.isRendererForitem(item, properties);
	}

	@Override
	public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
		return super.needsItemBuilder(itemBuilder, properties);
	}

	@Override
	public void preProcess(PreviewModel previewModel) {
		super.preProcess(previewModel);
	}

	@Override
	public void render(Item item, RenderTarget target, PreviewProperties properties) {
		super.render(item, target, properties);

	}

}
