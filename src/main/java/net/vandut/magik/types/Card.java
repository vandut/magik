package net.vandut.magik.types;

import net.vandut.magik.db.CardDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cards", daoClass = CardDaoImpl.class)
public class Card extends BaseDaoEnabled<Card, Integer> {
	
	public static final String CN_NAME = "name";

	@DatabaseField(generatedId = true)
	private Integer index;
	@DatabaseField(canBeNull = false)
	private String no;
	@DatabaseField(canBeNull = false, columnName = CN_NAME)
	private String name;
	@DatabaseField(canBeNull = false)
	private String type;
	@DatabaseField
	private String mana;
	@DatabaseField(canBeNull = false)
	private String rarity;
	@DatabaseField(canBeNull = false)
	private String artist;
	@DatabaseField(canBeNull = false, foreign = true)
	private Edition edition;

	public Card() {
		// ORMLite needs a no-arg constructor
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMana() {
		return mana;
	}

	public void setMana(String mana) {
		this.mana = mana;
	}

	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public Edition getEdition() {
		return edition;
	}

	public void setEdition(Edition edition) {
		this.edition = edition;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(this.getClass().getName());
		result.append("{no=");
		result.append(no);
		result.append("; name=");
		result.append(name);
		result.append("; edition=");
		result.append(edition.getName());
		result.append("; type=");
		result.append(type);
		result.append("; mana=");
		result.append(mana);
		result.append("; rarity=");
		result.append(rarity);
		result.append("; artist=");
		result.append(artist);
		result.append("}");
		return result.toString();
	}

}
