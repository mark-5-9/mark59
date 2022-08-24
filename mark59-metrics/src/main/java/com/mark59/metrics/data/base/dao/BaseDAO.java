package com.mark59.metrics.data.base.dao;

import java.util.List;

public interface BaseDAO {
	
	List<String> findColumnNamesForTable(String tableName);
	
}
