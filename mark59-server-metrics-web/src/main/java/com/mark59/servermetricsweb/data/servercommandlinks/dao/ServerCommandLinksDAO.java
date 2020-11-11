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

package com.mark59.servermetricsweb.data.servercommandlinks.dao;

import java.util.List;

import com.mark59.servermetricsweb.data.beans.ServerCommandLink;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public interface ServerCommandLinksDAO 
{
	public ServerCommandLink findServerCommandLink(String serverProfile, String commandName);
	
	public List<ServerCommandLink> findServerCommandLinks();
	public List<ServerCommandLink> findServerCommandLinksForServerProfile(String serverProfileName);
	public List<ServerCommandLink> findServerCommandLinks(String selectionCol, String selectionValue);
	
	public void insertServerCommandLink(ServerCommandLink serverCommandLink);

	// all fields are currently key - so any update methods are really add / delete
	public void updateServerCommandLinksForServerProfileName(String serverProfile, List<String> commandNames);

	public void deleteServerCommandLink(ServerCommandLink serverCommandLink);
	public void deleteServerCommandLinksForServerProfile(String serverProfile);
	public void deleteServerCommandLinksForCommandName(String commandName);

}