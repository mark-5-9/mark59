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

package com.mark59.metrics.data.servercommandlinks.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.mark59.metrics.data.beans.ServerCommandLink;
import com.mark59.metrics.utils.ServerMetricsWebUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
public class ServerCommandLinksDAOexcelWorkbookImpl implements ServerCommandLinksDAO 
{
	
	
	Sheet servercommandlinksSheet;

	public ServerCommandLinksDAOexcelWorkbookImpl(Sheet servercommandlinksSheet) {
		this.servercommandlinksSheet = servercommandlinksSheet;
	}
	
	
	@Override
	public List<ServerCommandLink> findServerCommandLinksForServerProfile(String serverProfileName) {

		List<ServerCommandLink> serverCommandLinkList = new ArrayList<>();

		Iterator<Row> iterator = servercommandlinksSheet.iterator();
		iterator.next(); // a header row is assumed and bypassed

		while (iterator.hasNext()) {
			Row serverCommandLinkRow = iterator.next();
			// System.out.println("ServerCommandLinks key =" + ServerMetricsWebUtils.cellValue(serverCommandLinkRow.getCell(0)));

			if (serverProfileName != null && serverProfileName.equalsIgnoreCase(ServerMetricsWebUtils.cellValue(serverCommandLinkRow.getCell(0)))) {				
				ServerCommandLink serverCommandLink = new ServerCommandLink();
				serverCommandLink.setServerProfileName(ServerMetricsWebUtils.cellValue(serverCommandLinkRow.getCell(0)));
				serverCommandLink.setCommandName      (ServerMetricsWebUtils.cellValue(serverCommandLinkRow.getCell(1)));
				serverCommandLinkList.add(serverCommandLink);
			}
		}
		return serverCommandLinkList;
	}	
	
	
	@Override
	public ServerCommandLink findServerCommandLink(String serverProfile, String commandName) {
		return null;
	}

	@Override
	public List<ServerCommandLink> findServerCommandLinks() {
		return null;
	}

	@Override
	public List<ServerCommandLink> findServerCommandLinks(String selectionCol, String selectionValue) {
		return null;
	}

	@Override
	public void insertServerCommandLink(ServerCommandLink serverCommandLink) {
	}

	@Override
	public void updateServerCommandLinksForServerProfileName(String serverProfile, List<String> commandNames) {
	}

	@Override
	public void deleteServerCommandLink(ServerCommandLink serverCommandLink) {
	}

	@Override
	public void deleteServerCommandLinksForServerProfile(String serverProfile) {
	}

	@Override
	public void deleteServerCommandLinksForCommandName(String commandName) {
	}
	

}
