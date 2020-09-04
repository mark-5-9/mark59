package com.mark59.servermetricsweb.data.base.dao;

import java.util.List;

public interface BaseDAO {
	
	public List<String> findColumnNamesForTable(String tableName);
	
}
