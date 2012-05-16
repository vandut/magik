package net.vandut.magik.db;

import java.sql.SQLException;

import net.vandut.magik.types.Card;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class CardDaoImpl extends BaseDaoImpl<Card, Integer> implements CardDao {
	
    public CardDaoImpl(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, Card.class);
	}

}
