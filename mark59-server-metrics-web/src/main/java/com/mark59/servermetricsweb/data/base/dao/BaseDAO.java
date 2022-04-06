package com.mark59.servermetricsweb.data.base.dao;

import java.util.List;

public interface BaseDAO {
	
	List<String> findColumnNamesForTable(String tableName);
	
}
