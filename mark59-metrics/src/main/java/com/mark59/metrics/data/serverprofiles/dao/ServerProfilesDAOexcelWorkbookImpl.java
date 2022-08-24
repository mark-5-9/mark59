/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.metrics.data.serverprofiles.dao;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.utils.ServerMetricsWebUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
public class ServerProfilesDAOexcelWorkbookImpl implements ServerProfilesDAO {
	
	Sheet serverprofilesSheet;

	public ServerProfilesDAOexcelWorkbookImpl(Sheet serverprofilesSheet) {
		this.serverprofilesSheet = serverprofilesSheet;
	}

	
	@Override
	public ServerProfile  findServerProfile(String serverProfileName){
		ServerProfile serverProfile = null; 
        Iterator<Row> iterator = serverprofilesSheet.iterator();
        iterator.next();   										 //a header row is assumed and bypassed
        boolean notFound = true;

        while (iterator.hasNext() && notFound ) {
            Row serverProfileRow = iterator.next();
            // System.out.println("findServerProfile key=" + ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(0)));
            
			if (serverProfileName != null && serverProfileName.equalsIgnoreCase(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(0)))){	
            	notFound=false;
            	serverProfile = new ServerProfile();
            	serverProfile.setServerProfileName	(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(0)));
            	serverProfile.setExecutor			(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(1)));
            	serverProfile.setServer				(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(2)));
            	serverProfile.setAlternativeServerId(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(3)));
            	serverProfile.setUsername			(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(4)));
            	serverProfile.setPassword			(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(5)));
            	serverProfile.setPasswordCipher		(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(6)));
            	serverProfile.setConnectionPort		(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(7)));
            	serverProfile.setConnectionTimeout	(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(8)));
            	serverProfile.setComment			(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(9)));
            	serverProfile.setParameters(deserializeJsonToMap(ServerMetricsWebUtils.cellValue(serverProfileRow.getCell(10))));
            }
        }   
		return  serverProfile;
	}

	
	@Override
	public List<ServerProfile> findServerProfiles() {
		return null;
	}

	@Override
	public List<ServerProfile> findServerProfiles(String selectionCol, String selectionValue) {
		return null;
	}

	@Override
	public void insertServerProfile(ServerProfile serverProfile) {
	}

	@Override
	public void updateServerProfile(ServerProfile serverProfile) {
	}

	@Override
	public void deleteServerProfile(String serverProfileName) {
	}

}
