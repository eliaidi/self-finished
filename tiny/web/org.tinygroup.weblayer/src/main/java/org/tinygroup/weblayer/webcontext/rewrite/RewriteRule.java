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
package org.tinygroup.weblayer.webcontext.rewrite;

import static org.tinygroup.commons.tools.ArrayUtil.isEmptyArray;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.tinygroup.commons.tools.MatchResultSubstitution;
import org.tinygroup.commons.tools.StringEscapeUtil;
import org.tinygroup.commons.tools.StringUtil;
import org.tinygroup.commons.tools.ToStringBuilder;
import org.tinygroup.commons.tools.ToStringBuilder.MapBuilder;
import org.tinygroup.logger.LogLevel;
import org.tinygroup.logger.Logger;
import org.tinygroup.logger.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 代表一个rewrite规则。
 * <p>
 * 每个rewrite规则被匹配后，就会再试着匹配所有的conditions（如果有的话）。假如conditions也被满足，
 * 那么substitution就会被执行。
 * </p>
 *
 * @author renhui
 */
public class RewriteRule implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(RewriteRule.class);
    private String              patternString;
    private Pattern             pattern;
    private boolean             negative;
    private RewriteCondition[]  conditions;
    private RewriteSubstitution substitution;
    private Object[]            handlers;

    public String getPattern() {
        return patternString;
    }

    public void setPattern(String patternString) throws PatternSyntaxException {
        this.patternString = StringUtil.trimToNull(patternString);
    }

    public void setConditions(RewriteCondition[] conditions) {
        this.conditions = conditions;
    }

    public void setSubstitution(RewriteSubstitution substitution) {
        this.substitution = substitution;
    }

    public void setHandlers(Object[] handlers) {
        this.handlers = handlers;
    }

    public Object[] handlers() {
        return handlers;
    }

    public void afterPropertiesSet() throws Exception {
        // pattern
        if (patternString == null || "!".equals(patternString)) {
            throw new PatternSyntaxException("empty pattern", patternString, -1);
        }

        String realPattern;

        if (patternString.startsWith("!")) {
            negative = true;
            realPattern = patternString.substring(1);
        } else {
            realPattern = patternString;
        }

        pattern = Pattern.compile(realPattern);

        // conditions
        if (conditions == null) {
            conditions = new RewriteCondition[0];
        }
        for(RewriteCondition condition:conditions){
    		condition.afterPropertiesSet();
    	}

        // substitution
        if (substitution == null) {
            substitution = new RewriteSubstitution();
        }
        substitution.afterPropertiesSet();

        // handlers
        if (handlers == null) {
            handlers = new RewriteSubstitutionHandler[0];
        }
    }

    /**
     * 试图匹配rule。
     * <p>
     * 如果匹配，则返回匹配结果。否则返回<code>null</code>表示不匹配。
     * </p>
     */
    public MatchResult match(String path) {
        Matcher matcher = pattern.matcher(path);
        boolean matched = matcher.find();

        if (!negative && matched) {
            	logger.logMessage(LogLevel.DEBUG, "Testing \"{}\" with rule pattern: \"{}\", MATCHED", StringEscapeUtil.escapeJava(path),
                          StringEscapeUtil.escapeJava(patternString));
            return matcher.toMatchResult();
        }

        if (negative && !matched) {
            	logger.logMessage(LogLevel.DEBUG,"Testing \"{}\" with rule pattern: \"{}\", MATCHED", StringEscapeUtil.escapeJava(path),
                          StringEscapeUtil.escapeJava(patternString));
            return MatchResultSubstitution.EMPTY_MATCH_RESULT;
        }

        	logger.logMessage(LogLevel.TRACE,"Testing \"{}\" with rule pattern: \"{}\", MISMATCHED", StringEscapeUtil.escapeJava(path),
                      StringEscapeUtil.escapeJava(patternString));

        return null;
    }

    public MatchResult matchConditions(MatchResult ruleMatchResult, HttpServletRequest request) {
        MatchResult conditionMatchResult = MatchResultSubstitution.EMPTY_MATCH_RESULT;

        if (!isEmptyArray(conditions)) {
            int i = 0;
            for (RewriteCondition condition : conditions) {
                MatchResult result = condition.match(ruleMatchResult, conditionMatchResult, request);

                // 判断ornext标记，但对最后一个condition忽略ornext标记，否则会得出现异常误结果。
                boolean ornext = i < conditions.length - 1 && condition.getConditionFlags().hasOR();

                if (result == null) {
                    if (!ornext) {
                        conditionMatchResult = null;
                        break;
                    }

                    // 保持conditionMatchResult结果为最近的一次匹配
                } else {
                    conditionMatchResult = result;

                    if (ornext) {
                        break;
                    }
                }

                i++;
            }
        }

        return conditionMatchResult;
    }

    public RewriteSubstitution getSubstitution() {
        return substitution;
    }

    
    public String toString() {
        MapBuilder mb = new MapBuilder();

        mb.append("pattern", patternString);

        if (!isEmptyArray(conditions)) {
            mb.append("conditions", conditions);
        }

        mb.append("substitution", substitution);

        return new ToStringBuilder().append("RewriteRule").append(mb).toString();
    }
}
