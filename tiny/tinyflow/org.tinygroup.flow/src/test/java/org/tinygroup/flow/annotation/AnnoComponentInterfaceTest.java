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
package org.tinygroup.flow.annotation;

import junit.framework.TestCase;

import org.tinygroup.beancontainer.BeanContainerFactory;
import org.tinygroup.context.Context;
import org.tinygroup.context.impl.ContextImpl;
import org.tinygroup.flow.FlowExecutor;
import org.tinygroup.tinytestutil.AbstractTestUtil;

public class AnnoComponentInterfaceTest extends TestCase {

	protected void setUp() throws Exception {
		AbstractTestUtil.init(null, true);
	}

	public void testComponentInterface() {
		FlowExecutor flowExecutor = BeanContainerFactory.getBeanContainer(
				this.getClass().getClassLoader()).getBean(
				FlowExecutor.FLOW_BEAN);
		Context context = new ContextImpl();
		context.put("name", "tiny");
		context.put("resultKey", "result");
		flowExecutor.execute("helloworld", context);
		assertEquals("Hello, tiny", context.get("result"));

	}

}
