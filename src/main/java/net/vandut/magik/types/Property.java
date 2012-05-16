package net.vandut.magik.types;

import net.vandut.magik.db.PropertyDaoImpl;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "properties", daoClass = PropertyDaoImpl.class)
public class Property extends BaseDaoEnabled<Property, String> {

	@DatabaseField(id = true)
	private String key;
	@DatabaseField(canBeNull = false)
	private String value;

	public Property() {
		// ORMLite needs a no-arg constructor
	}

	public Property(String key) {
		this.key = key;
	}

	public Property(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
