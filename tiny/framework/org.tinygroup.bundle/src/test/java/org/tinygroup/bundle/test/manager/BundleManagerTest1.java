/**
 *  Copyright (c) 1997-2013, www.tinygroup.org (luo_guo@icloud.com).
 *
 *  Licensed under the GPL, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.gnu.org/licenses/gpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tinygroup.bundle.test.manager;

import junit.framework.TestCase;

import org.tinygroup.beancontainer.BeanContainer;
import org.tinygroup.beancontainer.BeanContainerFactory;
import org.tinygroup.bundle.BundleManager;
import org.tinygroup.bundle.test.util.TestUtil;

public class BundleManagerTest1 extends TestCase {

	public void testStop() {
		TestUtil.init();
		BeanContainer<?> container = BeanContainerFactory.getBeanContainer(this.getClass().getClassLoader());
		BundleManager manager = (BundleManager) container.getBean(BundleManager.BEAN_NAME);
		
		manager.start();
		try {
			manager.getTinyClassLoader("test2").loadClass(
					"org.tinygroup.MyTestImpl2");

		} catch (ClassNotFoundException e) {
			assertFalse(true);
		}
		// manager.stop();
		// if(manager.getTinyClassLoader("test2")==null){
		// assertTrue(true);
		// }else{
		// assertTrue(false);
		// }

	}

}
