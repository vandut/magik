package net.vandut.magik.types;

import java.sql.SQLException;

import net.vandut.magik.db.EditionDao;
import net.vandut.magik.db.EditionDaoImpl;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "editions", daoClass = EditionDaoImpl.class)
public class Edition extends BaseDaoEnabled<Edition, String> {
	
	public static final String CN_CARDS = "cards";

	@DatabaseField(id = true)
	private String id;
	@DatabaseField(canBeNull = false)
	private String name;
	@ForeignCollectionField(eager = false, columnName = CN_CARDS)
	private ForeignCollection<Card> cards;

	public Edition() {
		// ORMLite needs a no-arg constructor
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ForeignCollection<Card> getCards() {
		return cards;
	}

	public void setEmptyCardList(EditionDao dao) throws SQLException {
		cards = dao.getEmptyForeignCollection(CN_CARDS);
	}

}
