package net.vandut.magik;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import net.vandut.magik.db.EditionDao;
import net.vandut.magik.types.Card;
import net.vandut.magik.types.Edition;

public class AppGui extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String FRAME_TITLE = "Magik (C) Konrad Bielak 2012";

	private DB db;

	private JProgressBar cardListProgress;
	private JProgressBar pdfGenProgress;
	private JButton cardListBtn;
	private JButton pdfGenBtn;
	private JTextArea cardListingArea;

	public AppGui() throws SQLException, IOException {
		super(FRAME_TITLE);
		openDB();
		setNativeLookAndFeel();
		createAndShowGUI();
	}

	private void openDB() throws SQLException, IOException {
		db = new DB();
		db.openDefaultFileDb();
		if (!db.tablesExist()) {
			db.createTables();
			pupulateTables(db);
		}
	}

	private void pupulateTables(DB db2) throws SQLException {
		// Property properties[] = { new Property("editions", EDITIONS) };
		// PropertyDao propertyDao = db.getPropertyDao();
		// for (Property p : properties) {
		// propertyDao.create(p);
		// }
	}

	private void setNativeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}
	}

	private void createAndShowGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel(new MigLayout("fill", "[]10[200]10[]"));
		getContentPane().add(panel);

		GuiUtils.addSeparator(panel, "Step 1: Download card list");
		cardListProgress = new JProgressBar();
		cardListBtn = GuiUtils.addRow(panel, "Status:", cardListProgress,
				"Download");

		GuiUtils.addSeparator(panel, "Step 2: Select cards");
		cardListingArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(cardListingArea);
		panel.add(scrollPane, "span, grow, wrap, h 100!");
		cardListingArea.setFont(new Font("Sans Serif", 0, 14));
		cardListingArea.setText("1 Forest");

		GuiUtils.addSeparator(panel, "Step 3: Generate PDF");
		pdfGenProgress = new JProgressBar();
		pdfGenBtn = GuiUtils.addRow(panel, "Status:", pdfGenProgress,
				"Generate");

		cardListBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new CardListThread().start();
			}
		});

		pdfGenBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new PDFThread().start();
			}
		});

		pack();
		setResizable(false);
		setVisible(true);
		setLocationRelativeTo(null);
	}
	
	public Card findCardByName(String name) throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Card.CN_NAME, name);
		List<Card> cards = db.getCardDao().queryForFieldValuesArgs(map);
		if(cards.isEmpty()) {
			return null;
		}
		return cards.get(0);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					new AppGui();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private class CardListThread extends Thread {

		@Override
		public void run() {
			cardListBtn.setEnabled(false);
			pdfGenBtn.setEnabled(false);
			
			cardListProgress.setValue(0);

			try {
				List<Edition> editionList = Downloader.downloadEditionList();
				EditionDao editionDao = db.getEditionDao();
				int index = 0;
				for (Edition edition : editionList) {
					if (!editionDao.idExists(edition.getId())) {
						edition.setEmptyCardList(editionDao);
						Downloader.downloadEdition(edition);
						editionDao.create(edition);
					}
					cardListProgress.setValue((int)(++index / (float)editionList.size()*100));
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} finally {
				cardListBtn.setEnabled(true);
				pdfGenBtn.setEnabled(true);
			}
		}
	}
	
	private class PDFThread extends Thread {

		@Override
		public void run() {
			cardListBtn.setEnabled(false);
			pdfGenBtn.setEnabled(false);

			pdfGenProgress.setValue(0);
			
			try {
				Map<Card, Integer> downloadableCards = convertTextToCards(cardListingArea.getText());
				
				File pdfFile = selectPDFFile();
				if(pdfFile != null) {
					GeneratePDF generatePDF = new GeneratePDF(pdfFile);
	
					int index = 0;
					for(Entry<Card, Integer> cardCount : downloadableCards.entrySet()) {
						generatePDF.addImage(Downloader.downloadCoverIfNotExists(cardCount.getKey()).getAbsolutePath(), cardCount.getValue());
						pdfGenProgress.setValue((int)(++index / (float)downloadableCards.size()*100));
					}
					generatePDF.close();
				}
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			} finally {
				cardListBtn.setEnabled(true);
				pdfGenBtn.setEnabled(true);
			}
		}
		
		public Map<Card, Integer> convertTextToCards(String text) throws Exception {
			Map<Card, Integer> cards = new HashMap<Card, Integer>();
			String rows[] = text.split("[\\r\\n]+");
			for(String row : rows) {
				
				int wcPos = row.indexOf(' ');
				int wcPos2 = row.indexOf('\t');
				if(wcPos2 > -1 && wcPos2 < wcPos) {
					wcPos = wcPos2;
				}
				if(wcPos < 0) {
					continue;
				}
				
				int count;
				try {
					count = Integer.parseInt(row.substring(0, wcPos));
				} catch (NumberFormatException e) {
					continue;
				}
				String name = row.substring(wcPos).trim().toUpperCase();
				
				Card card = findCardByName(name);
				if(card == null) {
					throw new Exception("Unknown card: "+name);
				}
				cards.put(card, count);
			}
			return cards;
		}
		
		public File selectPDFFile() {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {
					return "PDF files (*.pdf)";
				}

				@Override
				public boolean accept(File f) {
					return true;
				}
			});
			if (fc.showOpenDialog(AppGui.this) == JFileChooser.APPROVE_OPTION) {
				if (fc.getSelectedFile().getAbsolutePath().endsWith(".pdf")) {
					return fc.getSelectedFile();
				}
				return new File(fc.getSelectedFile().getAbsolutePath() + ".pdf");
			}
			return null;
		}

	}

}
