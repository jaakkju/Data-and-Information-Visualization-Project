package big.marketing.view.gephi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;

import org.apache.log4j.Logger;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewMouseEvent;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.openide.util.Lookup;

import processing.core.PApplet;
import processing.core.PVector;

public class CustomApplet extends PApplet implements MouseWheelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -511805493507982019L;

	static Logger logger = Logger.getLogger(CustomApplet.class);

	//Const
	private static final int WHEEL_TIMER = 500;
	//States
	private final PVector ref = new PVector();
	private final PVector trans = new PVector();
	private final PVector lastMove = new PVector();
	private float scaling;
	private Color background = Color.WHITE;
	private Timer wheelTimer;
	//Data
	private final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
	private PreviewModel model;
	private RenderTarget target;

	/**
	 * Refreshes the preview using the current graph from the preview controller.
	 */
	public void refresh(PreviewModel model, RenderTarget target) {
		this.model = model;
		this.target = target;
		// updates fonts
		//fontMap.clear(); Don't clear to prevent PFont memory leak from Processing library.
		if (model != null) {
			background = model.getProperties().getColorValue(PreviewProperty.BACKGROUND_COLOR);
		}

		// redraws the applet
		initAppletLayout();
		redraw();
	}

	public boolean isRedrawn() {
		return redraw;
	}

	@Override
	public void setup() {
		size(1000, 1000, JAVA2D);
		rectMode(CENTER);
		if (background != null) {
			background(background.getRGB(), background.getAlpha());
		}
		smooth();
		noLoop(); // the preview is drawn once and then redrawn when necessary
		addMouseWheelListener(this);

		toolPanel = new Panel();
		toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
		add(toolPanel);
		toolPanel.setVisible(false);

		//		legendPanel = createLegend();
		//		add(legendPanel);
		//		legendPanel.setBounds(1000, 1000, 100, 100);
		//		legendPanel.doLayout();
		//		legendPanel.setVisible(true);

	}

	Panel legendPanel;

	private Panel createLegend() {
		Panel p = new Panel();
		p.add(new Label("Test"));
		p.getGraphics();
		p.doLayout();
		return p;
	}

	public void showTooltip(String t) {
		toolPanel.removeAll();
		for (String line : t.split("\\n")) {
			if (line.length() > 0)
				toolPanel.add(new Label(line));
		}
		toolPanel.doLayout();
		Dimension d = toolPanel.getPreferredSize();
		toolPanel.setBounds(mouseX, mouseY, d.width + 10, d.height + 10);
		toolPanel.doLayout();
		toolPanel.setVisible(true);
	}

	public void hideTooltip() {
		toolPanel.setVisible(false);
	}

	Panel toolPanel;

	public void triangle(float x, float y, float size) {
		triangle(x + size / 2, y + size * MouseRenderer.SQRT_OF_12, x - size / 2, y + size * MouseRenderer.SQRT_OF_12, x, y - 2 * size
		      * MouseRenderer.SQRT_OF_12);
	}

	public void sixCorners(float x, float y, float size) {
		x -= size / 2;
		y -= size / 2;
		beginShape();
		{
			vertex(x, y + size / 2);
			vertex(x + size / 3, y + size);
			vertex(x + size / 3 * 2, y + size);
			vertex(x + size, y + size / 2);
			vertex(x + size / 3 * 2, y);
			vertex(x + size / 3, y);
		}
		endShape();
	}

	public void drawLegend() {

		//BACKGROUND
		fill(255, 255, 255);
		rectMode(CORNERS);
		float x = width * 0.8f;
		rect(x, 0, width, height);
		rectMode(CENTER);

		//
		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		int LINE_HEIGHT = 15;
		int MARGIN = 5;
		float y = LINE_HEIGHT / 2;
		text("Shapes", x, y);

		String[] desc = { "Admin", "Workstation", "Server", "External" };
		float symbolx = x + 10;
		y -= MARGIN;
		for (int i = 0; i < 4; i++) {
			y += LINE_HEIGHT + MARGIN;
			fill(0, 0, 0);
			text(desc[i], x + LINE_HEIGHT + MARGIN, y);
			fill(128, 128, 128);
			switch (i) {
			case 0:
				sixCorners(symbolx, y, LINE_HEIGHT);
				break;
			case 1:
				rect(symbolx, y, LINE_HEIGHT, LINE_HEIGHT);
				break;
			case 2:
				triangle(symbolx, y, LINE_HEIGHT);
				break;
			case 3:
				ellipse(symbolx, y, LINE_HEIGHT, LINE_HEIGHT);
				break;
			}
		}

		y = drawColorLegend("Nodes (amount of connections):", null, x, y, LINE_HEIGHT, MARGIN, Color.red, Color.yellow, Color.green);

		drawColorLegend("Edges (total Bytes):", null, x, y, LINE_HEIGHT, MARGIN, Color.red, Color.yellow, Color.green);

	}

	private float drawColorLegend(String line1, String line2, float x, float y, int LINE_HEIGHT, int MARGIN, Color... c) {
		fill(0, 0, 0);
		y += LINE_HEIGHT + MARGIN;
		text(line1, x + 5, y);
		if (line2 != null) {
			y += LINE_HEIGHT + MARGIN;
			text(line2, x + 5, y);
		}

		y += 10;
		int GRADIENT_WIDTH = (int) (width - x - 2 * MARGIN);

		drawMultiGradient(x + MARGIN, y, GRADIENT_WIDTH, LINE_HEIGHT, c);
		y += LINE_HEIGHT + MARGIN;
		text("low", x + 5, y);
		text("high", x + 5 + GRADIENT_WIDTH - 20, y);
		return y;
	}

	private void drawMultiGradient(float x1, float y1, float width, float height, Color... colors) {
		int div = colors.length - 1;
		float startx = x1, starty = y1;
		for (int i = 0; i < div; i++) {
			setGradient((int) startx, (int) starty, width / div, height, colors[i], colors[i + 1]);
			startx += width / div;
		}
	}

	void setGradient(int x, int y, float w, float h, Color c1, Color c2) {

		noFill();
		int color1 = color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());
		int color2 = color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha());

		for (int i = x; i <= x + w; i++) {
			float inter = map(i, x, x + w, 0, 1);
			int c = lerpColor(color1, color2, inter);
			stroke(c);
			line(i, y, i, y + h);
		}
	}

	@Override
	public void draw() {
		// blank the applet
		if (background != null) {
			background(background.getRGB(), background.getAlpha());
		}
		drawLegend();

		// user zoom
		PVector center = new PVector(width / 2f, height / 2f);
		PVector scaledCenter = PVector.mult(center, scaling);
		PVector scaledTrans = PVector.sub(center, scaledCenter);
		translate(scaledTrans.x, scaledTrans.y);
		scale(scaling);
		//scale(1f, -1f);

		// user move
		translate(trans.x, trans.y);
		//Draw target
		previewController.render(target);
	}

	@Override
	protected void resizeRenderer(int i, int i1) {
		if (i > 0 && i1 > 0) {
			super.resizeRenderer(i, i1);
		}
	}

	public Point screenPositionToModelPosition(float x, float y) {
		PVector vec = screenPositionToModelPosition(new PVector(x, y));
		return new Point((int) vec.x, (int) vec.y);
	}

	private PVector screenPositionToModelPosition(PVector screenPos) {
		PVector center = new PVector(width / 2f, height / 2f);
		PVector scaledCenter = PVector.mult(center, scaling);
		PVector scaledTrans = PVector.sub(center, scaledCenter);

		PVector modelPos = new PVector(screenPos.x, screenPos.y);
		modelPos.sub(scaledTrans);
		modelPos.div(scaling);
		modelPos.sub(trans);
		return modelPos;
	}

	private PVector getMouseModelPosition() {
		return screenPositionToModelPosition(new PVector(mouseX, mouseY));
	}

	private PreviewMouseEvent buildPreviewMouseEvent(PreviewMouseEvent.Type type) {
		PVector pos = getMouseModelPosition();
		PreviewMouseEvent.Button button;

		switch (mouseButton) {
		case CENTER:
			button = PreviewMouseEvent.Button.MIDDLE;
			break;
		case RIGHT:
			button = PreviewMouseEvent.Button.RIGHT;
			break;
		case LEFT:
		default:
			button = PreviewMouseEvent.Button.LEFT;
		}

		return new PreviewMouseEvent((int) pos.x, (int) pos.y, type, button, keyEvent);
	}

	@Override
	public void mouseClicked() {
		if (previewController.sendMouseEvent(buildPreviewMouseEvent(PreviewMouseEvent.Type.CLICKED))) {
			previewController.refreshPreview();
			redraw();
		}
	}

	@Override
	public void mousePressed() {
		previewController.sendMouseEvent(buildPreviewMouseEvent(PreviewMouseEvent.Type.PRESSED));

		previewController.refreshPreview();
		handleMousePress();
		redraw();
	}

	@Override
	public void mouseDragged() {
		if (!previewController.sendMouseEvent(buildPreviewMouseEvent(PreviewMouseEvent.Type.DRAGGED))) {
			handleMouseDrag();
		}
		redraw();
	}

	@Override
	public void mouseReleased() {
		if (!previewController.sendMouseEvent(buildPreviewMouseEvent(PreviewMouseEvent.Type.RELEASED))) {
			handleMouseRelease();
		}

		previewController.refreshPreview();
		redraw();
	}

	private void handleMousePress() {
		ref.set(mouseX, mouseY, 0);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		super.mouseMoved(arg0);

		PVector modelPosition = getMouseModelPosition();
		GraphMouseListener gml = Lookup.getDefault().lookup(GraphMouseListener.class);
		gml.mouseMoved(modelPosition.x, modelPosition.y);
	}

	@Override
	public void mouseMoved() {
	}

	private void handleMouseDrag() {
		setMoving(true);
		trans.set(mouseX, mouseY, 0);
		trans.sub(ref);
		trans.div(scaling); // ensure const. moving speed whatever the zoom is
		trans.add(lastMove);
	}

	private void handleMouseRelease() {
		lastMove.set(trans);
		setMoving(false);
		redraw();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getUnitsToScroll() == 0) {
			return;
		}
		float way = -e.getUnitsToScroll() / Math.abs(e.getUnitsToScroll());
		scaling = scaling * (way > 0 ? 2f : 0.5f);
		setMoving(true);
		if (wheelTimer != null) {
			wheelTimer.cancel();
			wheelTimer = null;
		}
		wheelTimer = new Timer();
		wheelTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				setMoving(false);
				redraw();
				wheelTimer = null;
			}
		}, WHEEL_TIMER);

		redraw();
	}

	@Override
	public void keyPressed() {
		switch (key) {
		case '+':
			scaling = scaling * 2f;
			break;
		case '-':
			scaling = scaling / 2f;
			break;
		case '0':
			scaling = 1;
			break;
		}

		redraw();
	}

	public void zoomPlus() {
		scaling = scaling * 2f;
		redraw();
	}

	public void zoomMinus() {
		scaling = scaling / 2f;
		redraw();
	}

	public void resetZoom() {
		scaling = 0;
		initAppletLayout();
		redraw();
	}

	public void setMoving(boolean moving) {
		if (model != null) {
			model.getProperties().putValue(PreviewProperty.MOVING, moving);
		}
	}

	/**
	 * Initializes the preview applet layout according to the graph's dimension.
	 */
	private void initAppletLayout() {
		//graphSheet.setMargin(MARGIN);
		if (model != null && model.getDimensions() != null && model.getTopLeftPosition() != null) {

			// initializes zoom
			Dimension dimensions = model.getDimensions();
			Point topLeftPostition = model.getTopLeftPosition();
			PVector box = new PVector((float) dimensions.getWidth(), (float) dimensions.getHeight());
			float ratioWidth = width / box.x;
			float ratioHeight = height / box.y;
			if (scaling == 0) {
				scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

				// initializes move
				PVector semiBox = PVector.div(box, 2);
				PVector topLeftVector = new PVector((float) topLeftPostition.x, (float) topLeftPostition.y);
				PVector center = new PVector(width / 2f, height / 2f);
				PVector scaledCenter = PVector.add(topLeftVector, semiBox);
				trans.set(center);
				trans.sub(scaledCenter);
				lastMove.set(trans);

			}
		}
	}
}
