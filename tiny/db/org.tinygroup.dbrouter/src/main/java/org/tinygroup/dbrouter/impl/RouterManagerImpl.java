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
package org.tinygroup.dbrouter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tinygroup.cache.Cache;
import org.tinygroup.commons.tools.CollectionUtil;
import org.tinygroup.dbrouter.PartitionRule;
import org.tinygroup.dbrouter.RouterKeyGenerator;
import org.tinygroup.dbrouter.RouterManager;
import org.tinygroup.dbrouter.ShardRule;
import org.tinygroup.dbrouter.StatementProcessor;
import org.tinygroup.dbrouter.balance.ShardBalance;
import org.tinygroup.dbrouter.balance.ShardBalanceDefault;
import org.tinygroup.dbrouter.config.KeyTable;
import org.tinygroup.dbrouter.config.KeyTables;
import org.tinygroup.dbrouter.config.Partition;
import org.tinygroup.dbrouter.config.Router;
import org.tinygroup.dbrouter.config.Routers;
import org.tinygroup.dbrouter.config.Shard;
import org.tinygroup.dbrouter.exception.DbrouterRuntimeException;
import org.tinygroup.dbrouter.util.DbRouterUtil;
import org.tinygroup.jsqlparser.JSQLParserException;
import org.tinygroup.jsqlparser.parser.CCJSqlParserManager;
import org.tinygroup.jsqlparser.statement.Statement;
import org.tinygroup.logger.LogLevel;
import org.tinygroup.logger.Logger;
import org.tinygroup.logger.LoggerFactory;
import org.tinygroup.xstream.XStreamFactory;
import org.tinygroup.xstream.config.XStreamAnnotationClass;
import org.tinygroup.xstream.config.XStreamConfiguration;

import com.thoughtworks.xstream.XStream;

/**
 * 分区管理器实现类
 */
public class RouterManagerImpl implements RouterManager {

	private static final String DBCLUSTER_XSTREAM_XML = "/dbrouter.xstream.xml";
	private static Logger logger = LoggerFactory
			.getLogger(RouterManagerImpl.class);
	/**
	 * 所有可用的集群MAP，String:集群ID，Router：集群配置
	 */
	private Map<String, Router> routerMap = new ConcurrentHashMap<String, Router>();
	/**
	 * 主键生成器，String:类名,RouterKeyGenerator主键生成器
	 */
	private Map<String, RouterKeyGenerator> routerKeyGeneratorMap = new ConcurrentHashMap<String, RouterKeyGenerator>();
	/**
	 * SqlParser管理器
	 */
	private CCJSqlParserManager parserManager = new CCJSqlParserManager();
	/**
	 * 已经解析出来的语句缓冲
	 */
	private Cache cache;

	/**
	 * 分片负载平衡器
	 */
	private ShardBalance balance = new ShardBalanceDefault();
	/**
	 * 语句处理器
	 */
	private List<StatementProcessor> statementProcessorList = new ArrayList<StatementProcessor>();

	private XStream routerXStream;

	private static final String CLUSTER_CONFIG = "dbrouter-config.xml";

	private static final String KEY_TABLE_XSTREAM_XML = "/keygenerator.sqlconfig.xml";

	private KeyTables keyTables;

	public RouterManagerImpl() {
		XStream loadXStream = XStreamFactory.getXStream();
		XStreamConfiguration xstreamConfiguration = (XStreamConfiguration) loadXStream
				.fromXML(RouterManagerImpl.class
						.getResourceAsStream(DBCLUSTER_XSTREAM_XML));
		routerXStream = XStreamFactory.getXStream(xstreamConfiguration
				.getPackageName());
		try {
			initKeyTables();
			loadAnnotationClass(routerXStream, xstreamConfiguration);
			ClassLoader loader = getClass().getClassLoader() == null ? ClassLoader
					.getSystemClassLoader() : getClass().getClassLoader();
			Enumeration<URL> urls = loader.getResources(CLUSTER_CONFIG);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				logger.logMessage(LogLevel.INFO, "找到集群配置文件：{0}", url.toString());
				Routers routers = (Routers) routerXStream.fromXML(url);
				addRouters(routers);
			}
		} catch (ClassNotFoundException e) {
			logger.errorMessage("dbrouter.xstream.xml文件不存在", e);
			throw new DbrouterRuntimeException(e);
		} catch (IOException e) {
			logger.errorMessage("查找集群配置:dbrouter-config.xml出现异常", e);
			throw new DbrouterRuntimeException(e);
		}
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public boolean isShardSql(Partition partition, String sql,
			Object... preparedParams) {
		if (partition.getMode() == Partition.MODE_PRIMARY_SLAVE) {
			// 如果是主从模式，则返回false
			return false;
		}
		if (partition.getShards() != null) {
			for (Shard shard : partition.getShards()) {
				for (ShardRule shardRule : shard.getShardRules()) {
					if (shardRule
							.isMatch(partition, shard, sql, preparedParams)) {
						logger.logMessage(LogLevel.DEBUG,
								"sql:{0},找到处理的shard:{1},shard-rule:{2}", sql,
								shard.getId(), shardRule.toString());
						return true;
					}
				}
			}
		}
		return false;
	}

	public void addStatementProcessor(StatementProcessor statementProcessor) {
		statementProcessorList.add(statementProcessor);
	}

	public List<StatementProcessor> getStatementProcessorList() {
		return this.statementProcessorList;
	}

	public void setStatementProcessorList(
			List<StatementProcessor> statementProcessorList) {
		this.statementProcessorList = statementProcessorList;
	}

	public synchronized <T> T getPrimaryKey(Router router, String tableName) {
		RouterKeyGenerator generator = router.getKeyGenerator();
		if (generator != null) {
			generator.setRouter(router);
			return (T) generator.getKey(tableName);
		}
		logger.log(LogLevel.ERROR, "router:{0},不存在key获取器:{0}", router.getId());
		throw new DbrouterRuntimeException("不存在key获取器");
	}

	public void addRouter(Router router) {
		initKeyGenerator(router);
		routerMap.put(router.getId(), router);
	}

	private void initKeyTables() {
		try {
			XStream stream = XStreamFactory.getXStream("");
			stream.processAnnotations(new Class[] { KeyTables.class,
					KeyTable.class });
			keyTables = (KeyTables) stream.fromXML(this.getClass()
					.getResourceAsStream(KEY_TABLE_XSTREAM_XML));
			keyTables.init();
		} catch (Exception e) {
			logger.errorMessage("加载主键表配置:keygenerator.sqlconfig.xml出现异常", e);
		}

	}

	/**
	 * 动态创建主键存储表
	 * 
	 * @param keyGenerator
	 */
	private void initKeyGenerator(Router router) {
		RouterKeyGenerator<?> keyGenerator = router.getKeyGenerator();
		// 判断用户设置是否能动态创建物理表
		if (keyGenerator == null || !keyGenerator.isAutoCreate()) {
			return;
		}
		logger.logMessage(LogLevel.DEBUG, "router:{0},执行动态创建主键存储表开始",
				router.getId());
		keyGenerator.setRouter(router);
		keyGenerator.createKeyTable(keyTables);
		logger.logMessage(LogLevel.DEBUG, "router:{0},执行动态创建主键存储表结束",
				router.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.tinygroup.dbproxy.config.RouterManager#getRouter(java.lang.String)
	 */
	public Router getRouter(String routerId) {
		return routerMap.get(routerId);
	}

	public boolean isMatch(Partition partition, String sql) {
		List<PartitionRule> rules = partition.getPartitionRules();
		if (rules == null) {
			return true;
		}
		for (PartitionRule rule : rules) {
			if (rule.isMatch(sql)) {
				return true;
			}
		}
		return false;
	}

	public boolean isMatch(Partition partition, Shard shard, String sql,
			Object... preparedParams) {
		List<ShardRule> rules = shard.getShardRules();
		if (rules == null || rules.size() == 0) {
			return true;
		}
		for (ShardRule rule : rules) {
			if (rule.isMatch(partition, shard, sql, preparedParams)) {
				return true;
			}
		}
		return false;
	}

	public String getSql(Partition partition, Shard shard, String sql,
			Object... preparedParams) {
		List<ShardRule> rules = shard.getShardRules();
		for (ShardRule rule : rules) {
			if (rule.isMatch(partition, shard, sql, preparedParams)) {
				// 如果有匹配的，则返回匹配的规则处理过的SQL
				return rule.getReplacedSql(partition, shard, sql);
			}
		}
		if (!CollectionUtil.isEmpty(shard.getTableMappings())) {
			return DbRouterUtil.transformSqlWithTableName(sql,
					shard.getTableMappingMap());
		}
		return sql;
	}

	public Collection<Partition> getPartitions(String routerId, String sql) {
		Router router = getRouter(routerId);
		return getPartitions(router, sql);
	}

	public Partition getPartition(String routerId, String sql) {
		return getPartition(getRouter(routerId), sql);
	}

	public Partition getPartition(Router router, String sql) {
		if (router != null) {
			for (Partition partition : router.getPartitions()) {
				if (isMatch(partition, sql)) {
					return partition;
				}
			}
		}
		throw new DbrouterRuntimeException("不能找到SQL:" + sql + "匹配的分区！");
	}

	public List<Partition> getPartitions(Router router, String sql) {
		List<Partition> partitions = new ArrayList<Partition>();
		if (router != null) {
			for (Partition partition : router.getPartitions()) {
				if (isMatch(partition, sql)) {
					partitions.add(partition);
				}
			}
		}
		return partitions;
	}

	public List<Shard> getShards(Partition partition, String sql,
			Object... preparedParams) {
		List<Shard> shards = new ArrayList<Shard>();
		if (partition != null && !CollectionUtil.isEmpty(partition.getShards())) {
			for (Shard shard : partition.getShards()) {
				if (isMatch(partition, shard, sql, preparedParams)) {
					shards.add(shard);
				}
			}

		}
		return shards;
	}

	public Statement getSqlStatement(String sql) {
		Statement statement = null;
		try {
			statement = (Statement) cache.get(sql);
		} catch (Exception e) {
		}
		if (statement != null) {
			return statement;
		}
		// synchronized(parserManager){
		try {
			statement = parserManager.parse(new StringReader(sql));
			cache.put(sql, statement);
		} catch (JSQLParserException e) {
			throw new DbrouterRuntimeException(e);
		}
		// }
		return statement;
	}

	public ShardBalance getShardBalance() {
		return balance;
	}

	public void setShardBalance(ShardBalance balance) {
		this.balance = balance;
	}

	public void addRouters(String routerFilePath) {
		addRouters(RouterManagerImpl.class.getResourceAsStream(routerFilePath));
	}

	public void addRouters(InputStream inputStream) {
		Routers routers = (Routers) routerXStream.fromXML(inputStream);
		addRouters(routers);
	}

	public void addRouters(Routers routers) {
		for (Router router : routers.getRouterList()) {
			addRouter(router);
		}
	}

	private void loadAnnotationClass(XStream xStream,
			XStreamConfiguration xstreamConfiguration)
			throws ClassNotFoundException {
		if (xstreamConfiguration.getxStreamAnnotationClasses() != null) {
			for (XStreamAnnotationClass annotationClass : xstreamConfiguration
					.getxStreamAnnotationClasses()) {
				xStream.processAnnotations(Class.forName(annotationClass
						.getClassName()));
			}
		}
	}

	public Map<String, Router> getRouterMap() {
		return routerMap;
	}

}
