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
package org.tinygroup.dict;

import org.tinygroup.context.Context;

/**
 * 字典项过滤器，有时需要根据不同的人员情况对字典项进行一定过滤
 * 
 * @author luoguo
 * 
 */
public interface DictFilter {
	/**
	 * 对字典项进行过滤
	 * 
	 * @param context
	 * @param dict
	 * @return 过滤后的结果
	 */
	Dict filte(Context context, Dict dict);
}
