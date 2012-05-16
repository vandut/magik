package net.vandut.magik;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.vandut.magik.db.CardDao;
import net.vandut.magik.db.EditionDao;
import net.vandut.magik.db.PropertyDao;
import net.vandut.magik.types.Card;
import net.vandut.magik.types.Edition;
import net.vandut.magik.types.Property;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DB {

	private ConnectionSource connection;
	
	private PropertyDao propertyDao;
	private EditionDao editionDao;
	private CardDao cardDao;
	
	public void openMemDb() throws SQLException {
		String databaseUrl = "jdbc:h2:mem:account";
		open(databaseUrl);
	}
	
	public void openDefaultFileDb() throws IOException, SQLException {
		String appBaseDir = Utils.getAppBaseDirectory().getAbsolutePath();
		openFileDb(appBaseDir + File.separator + "db");
	}
	
	public void openFileDb(String path) throws SQLException {
		String databaseUrl = "jdbc:h2:file:" + path;
		open(databaseUrl);
	}

	private void open(String databaseUrl) throws SQLException {
		connection = new JdbcConnectionSource(databaseUrl);
		propertyDao = DaoManager.createDao(connection, Property.class);
		editionDao = DaoManager.createDao(connection, Edition.class);
		cardDao = DaoManager.createDao(connection, Card.class);
	}

	public void close() throws SQLException {
		connection.close();
	}

	public boolean tablesExist() throws SQLException {
		return propertyDao.isTableExists();
	}

	public void createTables() throws SQLException {
		TableUtils.createTable(connection, Edition.class);
		TableUtils.createTable(connection, Card.class);
		TableUtils.createTable(connection, Property.class);
	}

	public ConnectionSource getConnection() {
		return connection;
	}

	public PropertyDao getPropertyDao() {
		return propertyDao;
	}

	public EditionDao getEditionDao() {
		return editionDao;
	}

	public CardDao getCardDao() {
		return cardDao;
	}

	public static void main(String[] args) throws SQLException, IOException {
		DB db = new DB();
		db.openDefaultFileDb();
		if (!db.tablesExist()) {
			db.createTables();
		}
		db.close();
	}

}
