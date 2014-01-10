package big.marketing.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

import big.marketing.controller.DataController;

public class CustomJSlider extends JSlider {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7716315239388611562L;

	public CustomJSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		setUI(new MySliderUI(this));
	}

	private class MySliderUI extends BasicSliderUI {
		private final int range;

		public MySliderUI(JSlider aSlider) {
			super(aSlider);
			range = ControlsJPanel.QW_MAX - ControlsJPanel.QW_MIN;

		}

		@Override
		public void paintThumb(Graphics g) {
			int QW_LENGTH = (int) (getWidth() * DataController.QUERYWINDOW_SIZE / range);
			g.setColor(new Color(0, 0, 255, 120));
			g.fillRect(thumbRect.width / 2 + thumbRect.x - QW_LENGTH / 2, thumbRect.y, QW_LENGTH, thumbRect.height);
			super.paintThumb(g);
		}
	}
}
