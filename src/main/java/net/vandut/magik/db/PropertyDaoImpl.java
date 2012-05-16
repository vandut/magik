package net.vandut.magik.db;

import java.sql.SQLException;

import net.vandut.magik.types.Property;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class PropertyDaoImpl extends BaseDaoImpl<Property, String> implements PropertyDao {
	
    public PropertyDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Property.class);
	}

}
