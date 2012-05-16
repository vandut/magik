package net.vandut.magik.db;

import java.sql.SQLException;

import net.vandut.magik.types.Edition;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class EditionDaoImpl extends BaseDaoImpl<Edition, String> implements EditionDao {
	
    public EditionDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Edition.class);
	}

}
