package net.vandut.magik;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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
		
        cardListingArea.setText("2 Forest @set=m14 @random\n");
    }

    public List<Card> findCardByName(String name) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Card.CN_NAME, name);
        List<Card> cards = db.getCardDao().queryForFieldValuesArgs(map);
        if (cards.isEmpty()) {
            return Collections.emptyList();
        }
        return cards;
    }

    private List<Card> filterSameEditionCards(List<Card> cards) {
        final Card firstCard = cards.get(0);
        return Lists.newArrayList(Iterables.filter(cards, new Predicate<Card>() {
            @Override
            public boolean apply(@Nullable Card card) {
                return card.getEdition().getId().equals(firstCard.getEdition().getId());
            }
        }));
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
                    cardListProgress.setValue((int) (++index / (float) editionList.size() * 100));
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
                if (pdfFile != null) {
                    GeneratePDF generatePDF = new GeneratePDF(pdfFile);

                    int index = 0;
                    for (Entry<Card, Integer> cardCount : downloadableCards.entrySet()) {
                        generatePDF.addImage(Downloader.downloadCoverIfNotExists(cardCount.getKey()).getAbsolutePath(), cardCount.getValue());
                        pdfGenProgress.setValue((int) (++index / (float) downloadableCards.size() * 100));
                    }
                    generatePDF.close();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                cardListBtn.setEnabled(true);
                pdfGenBtn.setEnabled(true);
            }
        }

        public final static String CARD_NAME_REGEXP = "^\\s*(\\d+)\\s+([^@]+).*";
        public final static String CARD_MODF_REGEXP = "@([^\\s=]+)=?([^\\s]*)";

        public final Pattern CARD_NAME_PATTERN = Pattern.compile(CARD_NAME_REGEXP);
        public final Pattern CARD_MODF_PATTERN = Pattern.compile(CARD_MODF_REGEXP);

        public Map<Card, Integer> convertTextToCards(String text) throws Exception {
            Map<Card, Integer> cardMap = new HashMap<Card, Integer>();
            String rows[] = text.split("[\\r\\n]+");
            for (String row : rows) {
                cardMap.putAll(generateCardsFromLine(row));
            }
            return cardMap;
        }

        public Map<Card, Integer> generateCardsFromLine(String text) throws Exception {
            Matcher cardNameMatcher = CARD_NAME_PATTERN.matcher(text);
            if(!cardNameMatcher.matches()) {
                throw new Exception("Could not find Card in: " + text + "\nUse format <number> <name> [@random] [@set=m14,...]");
            }
            int cardCount = Integer.valueOf(cardNameMatcher.group(1).trim());
            String cardName = cardNameMatcher.group(2).trim();

            Map<String, String> modifiers = new HashMap<String, String>();
            Matcher cardNameModifiers = CARD_MODF_PATTERN.matcher(text);
            String actionName, actionArgs;
            while(cardNameModifiers.find()) {
                actionName = cardNameModifiers.group(1);
                actionArgs = cardNameModifiers.group(2);
                modifiers.put(actionName.trim(), actionArgs == null ? null : actionArgs.trim());
            }

            return generateListOfCards(cardName, cardCount, modifiers);
        }

        public Map<Card, Integer> generateListOfCards(String cardName, int cardCount, Map<String, String> modifiers) throws Exception {
            List<Card> cardList = findCardByName(cardName.toUpperCase());
            if (cardList.isEmpty()) {
                throw new Exception("Unknown card: " + cardName);
            }

            if(modifiers.containsKey("set")) {
                String sets = modifiers.get("set");
                if(sets != null) {
                    cardList = filterCardsBySet(cardList, sets.split(","));
                }
            }

            if(!modifiers.containsKey("random")) {
                Map<Card, Integer> cardMap = new HashMap<Card, Integer>();
                cardMap.put(cardList.get(0), cardCount);
                return cardMap;
            }

            return useRandomCards(cardList, cardCount);
        }

        public List<Card> filterCardsBySet(List<Card> cardList, String[] sets) {
            List<String> setList = Arrays.asList(sets);
            List<Card> filteredCards = new ArrayList<Card>();
            for(Card card : cardList) {
                if(setList.contains(card.getEdition().getId())) {
                    filteredCards.add(card);
                }
            }
            return filteredCards;
        }

        public Map<Card, Integer> useRandomCards(List<Card> cardList, int cardCount) {
            Map<Card, Integer> cardMap = new HashMap<Card, Integer>();
            for(int idx = 0, size = cardList.size(); cardCount > 0; cardCount--, idx++) {
                Card card = cardList.get(idx % size);
                if(!cardMap.containsKey(card)) {
                    cardMap.put(card, 1);
                } else {
                    cardMap.put(card, cardMap.get(card) + 1);
                }
            }
            /*Random randomGenerator = new Random();
            for(; cardCount > 0; cardCount--) {
                Card card = cardList.get(randomGenerator.nextInt(cardList.size()));
                if(!cardMap.containsKey(card)) {
                    cardMap.put(card, 1);
                } else {
                    cardMap.put(card, cardMap.get(card) + 1);
                }
            }*/
            return cardMap;
        }

        public Map<Card, Integer> convertTextToCards_old(String text) throws Exception {
            Map<Card, Integer> cards = new HashMap<Card, Integer>();
            String rows[] = text.split("[\\r\\n]+");
            for (String row : rows) {

                int wcPos = row.indexOf(' ');
                int wcPos2 = row.indexOf('\t');
                if (wcPos2 > -1 && wcPos2 < wcPos) {
                    wcPos = wcPos2;
                }
                if (wcPos < 0) {
                    continue;
                }

                int count;
                try {
                    count = Integer.parseInt(row.substring(0, wcPos));
                } catch (NumberFormatException e) {
                    continue;
                }

                String name = row.substring(wcPos).trim();
                String actionName = null;
                List<String> actionArgs = new ArrayList<String>();

                int atSignPos = name.indexOf('@');
                if (atSignPos > 0) {
                    actionName = name.substring(atSignPos).trim();
                    name = name.substring(0, atSignPos).trim();

                    int blankPos = actionName.indexOf(' ');
                    if (blankPos > 0) {
                        actionArgs = Arrays.asList(actionName.substring(blankPos).trim().split("\\s+"));
                        actionName = actionName.substring(0, blankPos).trim();
                    }

                    System.out.println(String.format("Action, name: %s, args: %d %s", actionName, actionArgs.size(), actionArgs));
                }
                name = name.toUpperCase();

                List<Card> cardList = findCardByName(name);
                if (cardList.isEmpty()) {
                    throw new Exception("Unknown card: " + name);
                }
                cardList = filterCards(cardList, count, actionName, actionArgs);
                if (cardList.isEmpty()) {
                    throw new Exception("Filter not matched for card: " + name);
                }

                int cardsInList = cardList.size();
                if (cardsInList == 1) {
                    cards.put(cardList.get(0), count);
                } else {
                    int remaining = count;
                    for (int i = 1; i < cardsInList; i++) {
                        cards.put(cardList.get(i), count / cardsInList);
                        remaining -= count / cardsInList;
                    }
                    cards.put(cardList.get(0), remaining);
                }
            }
            return cards;
        }

        private List<Card> filterCards(List<Card> cardList, int count, String action, List<String> args) {
            if (action == null) {
                return Arrays.asList(cardList.get(0));
            }
            List<Card> result = null;
            if ("@random".equalsIgnoreCase(action)) {
                result = cardList;
            } else if ("@set".equals(action)) {
                final Set<String> sets = new HashSet<String>(args);
                result = Lists.newArrayList(Iterables.filter(cardList, new Predicate<Card>() {
                    @Override
                    public boolean apply(@Nullable Card card) {
                        return sets.contains(card.getEdition().getId());
                    }
                }));
            }
            if (result == null) {
                throw new UnsupportedOperationException(String.format("Action %s is not supported", action));
            }
            if (count > result.size()) {
                return result;
            }
            Collections.shuffle(result);
            return result.subList(0, count);
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
