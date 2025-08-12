package com.mark59.datahunter.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.mark59.datahunter.controller.HomeController;

// ref: https://spring.io/guides/gs/testing-web/
// @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT) // will use port 8081 
// note @LocalServerPort has been deprecated/removed

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2mem")
public class DataHunterWebApplicationTests    {
	
	@Value("${local.server.port}")
	private int port;
	
	@Autowired
	private HomeController homeController;
	
	@Autowired
	private TestRestTemplate restTemplate;	
	
	
	@Test
	public void checkDataHunterApplicationBasicFunctionalHttpActions() {
		// testing basic POSTs (ie, and if using SUBMIT btn) and drill downs.
		// try {Thread.sleep(6000000);} catch (Exception e) {}		
		String dhUrl = "http://localhost:" + port + "/mark59-datahunter/";
		System.out.println(">> Started DataHunter using h2mem profile on port " + port + ", URL : " + dhUrl);
		
		assertNotNull(homeController);
		String getBody = restTemplate.getForObject(dhUrl,	String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Datahunter Home \n"+getBody);
		assertTrue(getBody.contains("DataHunter Home"));
		
		// add row
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("application", "unittest");		
		formData.add("identifier", "a1");
		formData.add("lifecycle", "junit");
		formData.add("useability", "UNUSED");
		formData.add("otherdata", "");
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(formData, headers);
		ResponseEntity<String> response = restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>add a1 \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS"));
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));

		//add another row 
		formData.remove("identifier");formData.add("identifier", "a2");
		response = restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>add a2 \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS"));
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));		
		
		//check the 2 rows exist using Items Breakdown
		formData.remove("identifier");
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown \n"+response);
		assertTrue(response.getBody().contains("id=unittest_junit_UNUSED_count>2<"));
				
		//drill down as if using the 'magnifying glass' (GET) 
		response = restTemplate.getForEntity(dhUrl+"select_multiple_policies_action?application=unittest&lifecycle=junit&useability=UNUSED", String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>drilldown select_multiple_policies_action \n"+response);
		assertTrue(response.getBody().contains("update_policy_data?application=unittest&identifier=a1&lifecycle=junit"));		
		assertTrue(response.getBody().contains("update_policy_data?application=unittest&identifier=a2&lifecycle=junit"));	
		assertTrue(response.getBody().contains("id=rowsAffected>2<"));			

		//go to delete an item is if clicking the 'X' image from  select_multiple_policies_action (GET)
		response = restTemplate.getForEntity(dhUrl+"delete_policy?application=unittest&identifier=a1&lifecycle=junit", String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>goto Delete Item from select_multiple_policies_action \n"+response);
		assertTrue(response.getBody().contains("<h1>Delete an Item</h1>"));		
		assertTrue(response.getBody().contains("action=\"delete_policy_action\""));		
		assertTrue(response.getBody().contains("value=\"a1\""));	
	
		//delete the a1 row (as if click submit from the 'Delete an Item' page)
		formData.remove("identifier");formData.add("identifier", "a1");
		response = restTemplate.postForEntity( dhUrl+"delete_policy_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> delete policy a1 \n"+response);
		assertTrue(response.getBody().contains("id=application>unittest<"));
		assertTrue(response.getBody().contains("id=identifier>a1<"));
		assertTrue(response.getBody().contains("id=lifecycle>junit<"));
		assertTrue(response.getBody().contains("id=sqlResult>PASS"));
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));
		
		//add back 2 items and update Use State of all to REUSABLE 		
		formData.remove("identifier");formData.add("identifier", "a3");
		restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );	
		formData.remove("identifier");formData.add("identifier", "a4");
		restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );			
		formData.remove("identifier");formData.add("identifier", "");		
		formData.add("toUseability", "REUSABLE");
		response = restTemplate.postForEntity( dhUrl+"update_policies_use_state_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>update_policies_use_state_action \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS"));
		assertTrue(response.getBody().contains("id=rowsAffected>3<"));
		assertTrue(response.getBody().contains("update_policies_use_state?application=unittest&identifier=&lifecycle=junit&useability=UNUSED&toUseability=REUSABLE"));
		// check the expected rows in detail... 
		response = restTemplate.getForEntity(dhUrl+"select_multiple_policies_action?application=unittest&lifecycle=junit&useability=", String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>drilldown select_multiple_policies_action \n"+response);
		assertTrue(response.getBody().contains("update_policy_data?application=unittest&identifier=a2&lifecycle=junit"));		
		assertTrue(response.getBody().contains("update_policy_data?application=unittest&identifier=a3&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=unittest&identifier=a4&lifecycle=junit"));	
		assertTrue(response.getBody().contains("id=rowsAffected>3<"));	
		assertEquals(3, StringUtils.countMatches(response.getBody(), "<td>REUSABLE</td>"));

		
		// testing of INDEXED row types
		

        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("uploadFileTest", ".txt");
            Files.writeString(tempFilePath, "b1\nb2\nb3\nb4\nb5\nb6\nb7\nb8\nb9\nb10");
        } catch (IOException e) {e.printStackTrace(); throw new RuntimeException("Creation of the TempFile failed: " + e.getMessage()); }

        HttpHeaders uploadIdsHeaders = new HttpHeaders();
        uploadIdsHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> uploadIdsBody = new LinkedMultiValueMap<>();
        uploadIdsBody.add("file", new FileSystemResource(tempFilePath.toFile())); // "file" is the parameter name
        uploadIdsBody.add("application", "testix");
        uploadIdsBody.add("lifecycle",   "junit");
        uploadIdsBody.add("useability",  "REUSABLE");
        uploadIdsBody.add("typeOfUpload","BULK_LOAD_AS_INDEXED_REUSABLE");
        HttpEntity<MultiValueMap<String, Object>> idsUploadRequestEntity = new HttpEntity<>(uploadIdsBody, uploadIdsHeaders);
        
        // upload a file of ids
        try {
            response = restTemplate.postForEntity(dhUrl+"upload_ids_action", idsUploadRequestEntity, String.class);
//    		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>upload_ids_action \n"+response);
    		assertTrue(response.getBody().contains("<td id=rowsAffected>11 (11 inserts, 0 updates,0 deleted)</td>"));
        } catch (Exception e) {e.printStackTrace();throw new RuntimeException("Error during file upload: " + e.getMessage());} 
        
        // clean up temp file once data loaded
        try {Files.deleteIfExists(tempFilePath);} catch (IOException e){e.printStackTrace();throw new RuntimeException("TempFile deleter error: "+e.getMessage());}
  
        // should have 11 rows (10 + ix row)
        formData = new LinkedMultiValueMap<String, String>();
		formData.add("application", "testix");
		request = new HttpEntity<MultiValueMap<String, String>>(formData, headers);
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (of loaded indexed data \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("id=counter>11</td>"));
		assertTrue(response.getBody().contains("policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_count>11</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_isindexed>Y</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>0 (0%), ix=10</td>"));
        
		// delete b1,b4,b10  go to items breakdown (and check the holes left)
		formData.add("lifecycle", "junit");
		formData.remove("identifier");formData.add("identifier", "0000000001");
		response = restTemplate.postForEntity( dhUrl+"delete_policy_action", request, String.class );	
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));	
		formData.remove("identifier");formData.add("identifier", "0000000004");
		response = restTemplate.postForEntity( dhUrl+"delete_policy_action", request, String.class );	
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));
		formData.remove("identifier");formData.add("identifier", "0000000010");
		response = restTemplate.postForEntity( dhUrl+"delete_policy_action", request, String.class );	
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));
		
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (of loaded indexed data after 3 rows removed \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("id=counter>8</td>"));
		assertTrue(response.getBody().contains("policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_count>8</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_isindexed>Y</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>3 (30%), ix=10</td>"));		
        
		// reindex (like clicking the reindex icon on the rhs of the data on the breakdown page)
		response = restTemplate.getForEntity(dhUrl+"policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE", String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> reindex repsonse \n"+response);
		assertTrue(response.getBody().contains("<td id=reindexResultSuccess>true</td>"));		
		assertTrue(response.getBody().contains("<td id=reindexResultRowsMoved>2</td>"));	
		assertTrue(response.getBody().contains("<td id=reindexResulIxCount>7</td>"));	
		
		// check the breakdown is reporting ok
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (of re-indexed data \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("id=counter>8</td>"));
		assertTrue(response.getBody().contains("policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_count>8</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_isindexed>Y</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>0 (0%), ix=7</td>"));		
		
		//add an item (it should always be added at the end for INDEXED data)E 		
		formData.remove("identifier");formData.add("identifier", "anyoldidasitsnotused");
		formData.add("useability", "REUSABLE");
		formData.add("otherdata", "added-8th-row");		
		response = restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>added extra INDEXED row  \n"+response);		
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));

		// check the breakdown is reporting ok
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (of re-indexed data \n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("id=counter>9</td>"));
		assertTrue(response.getBody().contains("policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_count>9</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_isindexed>Y</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>0 (0%), ix=8</td>"));
		
		// sanity check row was added as 0000000008 etc 
		response = restTemplate.getForEntity(dhUrl+"select_multiple_policies_action?application=testix&lifecycle=junit&useability=REUSABLE", String.class);
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>drilldown select_multiple_policies_action after 8th INDEXED row added \n"+response);
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000000_IX&lifecycle=junit"));		
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000001&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000002&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000003&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000004&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000005&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000006&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000007&lifecycle=junit"));	
		assertTrue(response.getBody().contains("update_policy_data?application=testix&identifier=0000000008&lifecycle=junit"));	
		assertTrue(response.getBody().contains("id=rowsAffected>9<"));	
		assertEquals(9, StringUtils.countMatches(response.getBody(), "<td>REUSABLE</td>"));

		String respNoWithSpace = response.getBody().replaceAll("\\s+", "");
		assertTrue(respNoWithSpace.contains("<td>testix</td><td>0000000000_IX</td><td>junit</td><td>REUSABLE</td><td>8</td>"));	
		assertTrue(respNoWithSpace.contains("<td>testix</td><td>0000000008</td><td>junit</td><td>REUSABLE</td><td>added-8th-row</td>"));
		
		// change the 'oldest' INDEXED (= first for IX types) row to USED and check a hole is created
		formData.remove("identifier");formData.add("identifier", "0000000001");
		formData.remove("lifecycle");formData.add("lifecycle", "junit");
		formData.remove("useability");formData.add("useability", "USED");
		formData.remove("otherdata");formData.add("otherdata", "b8-to-used");		
		response = restTemplate.postForEntity( dhUrl+"update_policy_action", request, String.class );
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> change the 'oldest' INDEXED row from REUSABLE to USED  \n"+response);		
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));
		assertTrue(response.getBody().contains("?application=testix&identifier=0000000001&lifecycle=junit&useability=USED&otherdata=b8-to-used"));
		
		formData.remove("identifier");formData.remove("lifecycle");formData.remove("useability");formData.remove("otherdata");
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (after change of the 'oldest' INDEXED (row 1 for IX types)\n"+response);
		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>1 (12%), ix=8</td>"));
		assertTrue(response.getBody().contains(" <td id=testix_junit_USED_count>1</td>"));

		// check the now 'oldest' INDEXED row is returned for the LOOKUP
		formData.remove("identifier");
		formData.remove("lifecycle");formData.add("lifecycle", "junit");
		formData.remove("useability");formData.add("useability", "REUSABLE");
		formData.remove("selectOrder");formData.add("selectOrder", "SELECT_OLDEST_ENTRY");		
		response = restTemplate.postForEntity( dhUrl+"next_policy_action?pUseOrLookup=lookup", request, String.class );
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> confirm row 2 is now the 'oldest' INDEXED row  \n"+response);		
		assertTrue(response.getBody().contains("<td id=sqlResult>PASS</td>"));		
		assertTrue(response.getBody().contains("<td id=identifier>0000000002</td>"));		
		
		// add a new INDEXED item and check it is  added at the end (not in the existing hole)
		formData.remove("identifier");formData.add("identifier", "anyjunkyid");
		formData.remove("lifecycle");formData.add("lifecycle", "junit");		
		formData.remove("useability");formData.add("useability", "REUSABLE");
		formData.remove("otherdata");formData.add("otherdata", "id should be changed to numeric id");
		formData.remove("selectOrder");formData.add("selectOrder", "SELECT_OLDEST_ENTRY");		
		response = restTemplate.postForEntity( dhUrl+"add_policy_action", request, String.class );
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>added extra INDEXED row (should appear at end) \n"+response);		
		assertTrue(response.getBody().contains("id=rowsAffected>1<"));
		assertTrue(response.getBody().contains("<td id=identifier>0000000009</td>"));
		assertTrue(response.getBody().contains("<td id=lifecycle>junit</td>"));
		assertTrue(response.getBody().contains("<td id=useability>REUSABLE</td>"));
		assertTrue(response.getBody().contains("<td id=otherdata>id should be changed to numeric id</td>"));
		
		// finally, just confirm breakdown is as expected 
		formData.remove("applicationStartsWithOrEquals");formData.add("applicationStartsWithOrEquals", "STARTS_WITH");
		formData.remove("application");formData.add("application", "");				
		formData.remove("identifier");
		formData.remove("lifecycle");formData.add("lifecycle", "");
		formData.remove("useability");formData.add("useability", "");
		formData.remove("otherdata");
		formData.remove("selectOrder");	
		response = restTemplate.postForEntity( dhUrl+"policies_breakdown_action", request, String.class );	
		
//		System.out.println("\n\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>breakdown (final comfirmation data as expected \n"+response);
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_count>9</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_isindexed>Y</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_REUSABLE_holestats>1 (11%), ix=9</td>"));		
		assertTrue(response.getBody().contains("policies_breakdown_reindex?application=testix&lifecycle=junit&useability=REUSABLE"));
		
		assertTrue(response.getBody().contains("<td id=testix_junit_USED_count>1</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_USED_isindexed>N</td>"));
		assertTrue(response.getBody().contains("<td id=testix_junit_USED_holestats></td>"));
		
		assertTrue(response.getBody().contains("<td id=unittest_junit_REUSABLE_count>3</td>"));
		assertTrue(response.getBody().contains("<td id=unittest_junit_REUSABLE_isindexed>N</td>"));
		assertTrue(response.getBody().contains("<td id=unittest_junit_REUSABLE_holestats></td>"));		

		assertTrue(response.getBody().contains("id=sqlResult>PASS<"));
		assertTrue(response.getBody().contains("id=rowsAffected>3</td>"));
		
		
//		try {Thread.sleep(6000000);} catch (Exception e) {}		
		
	}
}