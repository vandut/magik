package net.vandut.magik;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.vandut.magik.types.Card;
import net.vandut.magik.types.Edition;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
	
	public static Document getDocument(String url) throws IOException {
		return Jsoup.connect(url).get();
	}
	
	public static List<Edition> downloadEditionList() throws IOException {
		List<Edition> editionList = new ArrayList<Edition>();
		Document doc = getDocument("http://magiccards.info/sitemap.html");
		Elements editions = new Elements();
		String headers[] = {"Expansions", "Core Sets"};
		for(String h : headers) {
			editions.addAll(doc.select("h2:contains(English) + table h3:contains("+h+") + ul li li"));
		}
		for(Element row : editions) {
			Edition e = new Edition();
			e.setId(row.getElementsByTag("small").text());
			e.setName(row.getElementsByTag("a").text());
            System.out.println(String.format("Name: %s, id: %s", e.getName(), e.getId()));
			editionList.add(e);
		}
		return editionList;
	}
	
	public static Edition downloadEdition(Edition edition) throws IOException {
		Document doc = getDocument("http://magiccards.info/"+edition.getId()+"/en.html");
		edition.setName(doc.select("body > h1").text().replaceAll(" "+edition.getId()+"/en", ""));
		Elements cards = doc.select("body > table").eq(2).select("tbody tr:gt(0)");
		for(Element c : cards) {
			Elements rows = c.getElementsByTag("td");
			Card card = new Card();
			card.setNo(rows.eq(0).text());
			card.setName(rows.eq(1).text().toUpperCase());
			card.setType(rows.eq(2).text());
			card.setMana(rows.eq(3).text());
			card.setRarity(rows.eq(4).text());
			card.setArtist(rows.eq(5).text());
			card.setEdition(edition);
			edition.getCards().add(card);
			System.out.println(card);
		}
		return edition;
	}
	
	public static File cardCoverFile(Card card) throws IOException {
		final char FS = File.separatorChar;
		File dirFile = new File(Utils.getAppBaseDirectory(), "covers" + FS
				+ card.getEdition().getId() + FS);
		return new File(dirFile, card.getNo() + ".jpg");
	}
	
	public static File downloadCoverIfNotExists(Card card) throws IOException {
		File coverFile = cardCoverFile(card);
		if(coverFile.exists()) {
			return coverFile;
		}
		return downloadCover(card);
	}

	public static File downloadCover(Card card) throws IOException {
		File coverFile = cardCoverFile(card);
		Utils.createDirIfNotExists(coverFile.getParentFile());
		URL url = new URL(String.format(
				"http://magiccards.info/scans/%s/%s/%s.jpg", "en",
				card.getEdition().getId(), card.getNo()));
		FileUtils.copyURLToFile(url, coverFile);
		return coverFile;
	}

}
