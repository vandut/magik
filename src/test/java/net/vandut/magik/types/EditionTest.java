package net.vandut.magik.types;

import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.Collection;

import net.vandut.magik.DB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditionTest {
	
	private DB db;

	@Before
	public void setUp() throws Exception {
		db = new DB();
		db.openMemDb();
		if (!db.tablesExist()) {
			db.createTables();
		}
	}

	@After
	public void tearDown() throws Exception {
		db.close();
	}

	@Test
	public void testAddWithId() throws SQLException {
		Edition e = new Edition();
		e.setDao(db.getEditionDao());
		e.setId("id");
		e.setName("name");
		e.create();
	}

	@Test(expected=SQLException.class)
	public void testAddWithIdNoName() throws SQLException {
		Edition e = new Edition();
		e.setDao(db.getEditionDao());
		e.setId("id");
		e.create();
	}

	@Test(expected=SQLException.class)
	public void testAddWithoutId() throws SQLException {
		Edition e = new Edition();
		e.setDao(db.getEditionDao());
		e.setName("name");
		e.create();
	}
	
	@Test
	public void testGetCards() throws SQLException {
		Edition e = new Edition();
		e.setDao(db.getEditionDao());
		e.setId("id");
		e.setName("name");
		e.create();
		
		e.refresh();
		Collection<Card> cards = e.getCards();
		assertNotNull(cards);
	}
	
	@Test
	public void testAddCards() throws SQLException {
		Edition e = new Edition();
		e.setDao(db.getEditionDao());
		e.setId("id");
		e.setName("name");
		e.setEmptyCardList(db.getEditionDao());
		e.create();
	}

}
