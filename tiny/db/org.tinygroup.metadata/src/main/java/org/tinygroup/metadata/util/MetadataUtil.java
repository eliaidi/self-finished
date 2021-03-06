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
package org.tinygroup.metadata.util;

import java.util.List;

import org.tinygroup.beancontainer.BeanContainerFactory;
import org.tinygroup.commons.tools.CollectionUtil;
import org.tinygroup.commons.tools.StringUtil;
import org.tinygroup.metadata.bizdatatype.BusinessTypeProcessor;
import org.tinygroup.metadata.config.PlaceholderValue;
import org.tinygroup.metadata.config.bizdatatype.BusinessType;
import org.tinygroup.metadata.config.stddatatype.DialectType;
import org.tinygroup.metadata.config.stddatatype.StandardType;
import org.tinygroup.metadata.config.stdfield.StandardField;
import org.tinygroup.metadata.stddatatype.StandardTypeProcessor;
import org.tinygroup.metadata.stdfield.StandardFieldProcessor;

public final class MetadataUtil {
	public static final String METADATA_XSTREAM = "metadata";
	public static final String STDFIELDPROCESSOR_BEAN = "standardFieldProcessor";
	public static final String STANDARDTYPEPROCESSOR_BEAN = "standardTypeProcessor";
	public static final String BUSINESSTYPEPROCESSOR_BEAN = "businessTypeProcessor";
	public static final String CONSTANTPROCESSOR_BEAN = "constantProcessor";
	public static final String ERRORMESSAGEPROCESSOR_BEAN = "errorMessageProcessor";

	private MetadataUtil() {
	}

	public static String passNull(String string) {
		if (string == null) {
			return "";
		}
		return string;
	}

	public static String formatType(String type,
			List<PlaceholderValue> placeholderValueList) {
		String result = type;
		if (placeholderValueList != null) {
			for (PlaceholderValue placeholderValue : placeholderValueList) {
				if(!StringUtil.isEmpty(placeholderValue.getName()) && !StringUtil.isEmpty(placeholderValue.getValue())){
					result = result.replaceAll(
							"[$][{]" + placeholderValue.getName() + "[}]",
							placeholderValue.getValue());
				}
				
			}
		}
		return result;
	}

	public static StandardField getStandardField(String fieldId,
			ClassLoader loader) {
		StandardFieldProcessor standardFieldProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.STDFIELDPROCESSOR_BEAN);
		return standardFieldProcessor.getStandardField(fieldId);
	}

	public static String getStandardFieldType(String stdFieldId, String type,
			ClassLoader loader) {
		StandardFieldProcessor standardFieldProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.STDFIELDPROCESSOR_BEAN);
		String datatype = standardFieldProcessor.getType(stdFieldId, type);
		return datatype;
	}

	public static DialectType getDialectType(String stdFieldId, String type,
			ClassLoader loader) {
		StandardType standardType = getStandardType(stdFieldId, loader);
		List<DialectType> dialectTypes = standardType.getDialectTypeList();
		if (!CollectionUtil.isEmpty(dialectTypes)) {
			for (DialectType dialectType : dialectTypes) {
				if (dialectType.getLanguage().equals(type)) {
					return dialectType;
				}
			}
		}
		return null;
	}

	public static String getPlaceholderValue(String stdFieldId,
			String holderName,ClassLoader loader) {
		return getPlaceholderValue(stdFieldId, holderName, null,loader);
	}

	public static String getPlaceholderValue(String stdFieldId,
			String holderName, String defaultValue, ClassLoader loader) {
		BusinessType businessType = getBusinessType(stdFieldId, loader);
		List<PlaceholderValue> values = businessType.getPlaceholderValueList();
		if (!CollectionUtil.isEmpty(values)) {
			String[] names = holderName.split(",");
			for (String name : names) {
				for (PlaceholderValue value : values) {
					if (value.getName().equals(name)) {
						return value.getValue();
					}
				}
			}
		}
		return defaultValue;
	}

	public static StandardType getStandardType(String fieldId,
			ClassLoader loader) {
		StandardField field = getStandardField(fieldId, loader);
		BusinessTypeProcessor businessTypeProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.BUSINESSTYPEPROCESSOR_BEAN);
		BusinessType businessType = businessTypeProcessor
				.getBusinessTypes(field.getTypeId());
		StandardTypeProcessor standardTypeProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.STANDARDTYPEPROCESSOR_BEAN);
		return standardTypeProcessor.getStandardType(businessType.getTypeId());
	}

	public static BusinessType getBusinessType(String fieldId,
			ClassLoader loader) {
		StandardField field = getStandardField(fieldId, loader);
		BusinessTypeProcessor businessTypeProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.BUSINESSTYPEPROCESSOR_BEAN);
		Thread.currentThread().getContextClassLoader();
		BusinessType businessType = businessTypeProcessor
				.getBusinessTypes(field.getTypeId());
		return businessType;
	}

	public static StandardType getStandardType(StandardField field,
			ClassLoader loader) {
		BusinessTypeProcessor businessTypeProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.BUSINESSTYPEPROCESSOR_BEAN);
		BusinessType businessType = businessTypeProcessor
				.getBusinessTypes(field.getTypeId());
		StandardTypeProcessor standardTypeProcessor = BeanContainerFactory
				.getBeanContainer(loader).getBean(
						MetadataUtil.STANDARDTYPEPROCESSOR_BEAN);
		return standardTypeProcessor.getStandardType(businessType.getTypeId());
	}
}
