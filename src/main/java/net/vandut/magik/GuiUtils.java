package net.vandut.magik;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class GuiUtils {

	public static void addSeparator(JPanel panel, String labelText) {
		JLabel label = new JLabel(labelText);
		label.setForeground(Color.BLUE);
		JSeparator separator = new JSeparator();
		panel.add(label, "split 2, span");
		panel.add(separator, "growx, wrap");
	}

	public static JButton addRow(JPanel panel, String labelText, Component comp, String btnText) {
		panel.add(new JLabel(labelText));
		panel.add(comp, "growx");
		JButton btn = new JButton(btnText);
		panel.add(btn, "growx, wrap");
		return btn;
	}

	public static void addRow(JPanel panel, String labelText, Component comp) {
		panel.add(new JLabel(labelText));
		panel.add(comp, "growx, span");
	}

}
