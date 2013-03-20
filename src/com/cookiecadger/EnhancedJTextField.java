package com.cookiecadger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JTextField;

public class EnhancedJTextField extends JTextField {
	private String placeholderText = null;
	private Font placeholderFont;

	@Override
	protected void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);

		if (getText().isEmpty() && getPlaceholder() != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			setForeground(Color.GRAY);

			g2.setFont(this.placeholderFont);

			g2.drawString(getPlaceholder(), 5, 13);
			g2.dispose();
		} else {
			setForeground(Color.BLACK);
		}
	}

	public void setPlaceholder(String placeholderText, Font placeholderFont) {
		this.placeholderText = placeholderText;
		this.placeholderFont = placeholderFont;
	}

	public String getPlaceholder() {
		return this.placeholderText;
	}
}