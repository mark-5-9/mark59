
-- *************************************************************************************
-- **
-- **   from 4.0.0-rc1 to 4.0.0   
-- **   Only change is that the 'pre-defined' variables in in Metrics Graph Mapping   
-- **   change from @application to :application and @runTime to :runTime  
-- **
-- *************************************************************************************


-- connect to the metricsdb database and run:

update GRAPHMAPPING set BAR_RANGE_SQL = REPLACE(BAR_RANGE_SQL,'@',':')
