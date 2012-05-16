package net.vandut.magik.types;

import java.sql.SQLException;

import net.vandut.magik.DB;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PropertyTest {
	
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
		Property p = new Property("test");
		p.setValue("value");
		p.setDao(db.getPropertyDao());
		p.create();
	}

	@Test(expected=SQLException.class)
	public void testAddWithIdNoValue() throws SQLException {
		Property p = new Property("test");
		p.setDao(db.getPropertyDao());
		p.create();
	}

	@Test(expected=SQLException.class)
	public void testAddWithoutId() throws SQLException {
		Property p = new Property();
		p.setValue("value");
		p.setDao(db.getPropertyDao());
		p.create();
	}

}
