package big.marketing.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import big.marketing.controller.DataController;

public class QuerySliderUI extends BasicSliderUI {

	public QuerySliderUI(JSlider aSlider) {
		super(aSlider);
	}

	@Override
	protected Dimension getThumbSize() {
		int length = (int) (slider.getWidth() * DataController.QUERYWINDOW_SIZE / (slider.getMaximum() - slider.getMinimum()));
		return new Dimension(length, contentRect.height);
	}

	@Override
	public void paintThumb(Graphics g) {
		int innerWidth = 1;
		int middle = thumbRect.width / 2 + thumbRect.x;

		// paint outer marker with alpha
		g.setColor(new Color(0, 0, 255, 120));
		g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);

		// paint inner marker
		g.setColor(Color.black);
		g.fillRect(middle - innerWidth / 2, 0, innerWidth, thumbRect.height);
	}

	@Override
	public void paintTrack(Graphics g) {
	}

	@Override
	public void paintFocus(Graphics g) {
	}

}