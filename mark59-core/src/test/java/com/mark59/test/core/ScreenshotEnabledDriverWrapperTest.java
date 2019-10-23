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

package com.mark59.test.core;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.mark59.core.ScreenshotEnabledDriverWrapper;
/**
* @author Michael Cohen
* Written: Australian Winter 2019 
*/
public class ScreenshotEnabledDriverWrapperTest {

	@Mock
	private MockDriver mockDriver = Mockito.mock(MockDriver.class);
	
	@Test
	public final void bufferScreenshot_bufferAScreenshotWithASpecifiedName_screenshotBufferContainsScreenshot() {
		String imageName = "bufferScreenshot";
		ScreenshotEnabledDriverWrapper<?> w = new MockScreenshotEnabledDriverWrapper(mockDriver);
		w.bufferScreenshot(imageName);
		assert(!w.getBufferedScreenshots().isEmpty());
		assert(w.getBufferedScreenshots().keySet().stream().anyMatch(v->v.contains(imageName)));
	}


}
