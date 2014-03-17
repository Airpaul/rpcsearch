package com.baidu.fengchao.stargate.rpcsearch.sal;

import com.baidu.fengchao.stargate.rpcsearch.biz.bo.RpcDataStructure;
import com.baidu.fengchao.stargate.rpcsearch.sal.bo.ZkClusterInfo;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.api.CuratorWatcher;
import com.netflix.curator.framework.api.GetChildrenBuilder;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import com.netflix.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author: liangyafei
 * date: 14-3-7
 */
@Service
public class StargateZkClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(StargateZkClient.class);
    private static Map<String, CuratorFramework> ZK_CLIENT = new ConcurrentHashMap<String, CuratorFramework>();
    private static Map<String, ZkClusterInfo> CLUSTERS = new ConcurrentHashMap<String, ZkClusterInfo>();

    //init cache
    @PostConstruct
    public void init() {
        try {
            //加载配置文件
            StopWatch watch = new StopWatch();
            watch.start("load properties");
            loadProperties();
            watch.stop();
            LOGGER.info(watch.getLastTaskName() + " costs " + watch.getLastTaskTimeMillis() + "ms");

            //初始化所有ZK连接
            watch.start("init zk clients");
            initZkClients();
            watch.stop();
            LOGGER.info(watch.getLastTaskName() + " costs " + watch.getLastTaskTimeMillis() + "ms");

            //初始化本地cache
            watch.start("init search cache");
            for (ZkClusterInfo info : CLUSTERS.values()) {
                findRegisteredServiceRecursion(info.getName(), info.getRootPath());
            }
            watch.stop();
            LOGGER.info(watch.getLastTaskName() + " costs " + watch.getLastTaskTimeMillis() + "ms");
        } catch (Exception e) {
            LOGGER.error("init zookeeper client failed", e);
            System.exit(0);
        }
    }

    private void loadProperties() {
        InputStream is = null;
        try {
            LOGGER.info("loading file zkcluster.properties.");
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("zkcluster.properties");
            Properties properties = new Properties();
            properties.load(new InputStreamReader(is, "gbk"));
            if (properties.size() % 3 != 0) {  //配置文件的规则是：每个zk集群都有三个属性
                LOGGER.error("zkcluster.properties configs error, please obey the correct rule!");
            }
            Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
            for (Object key : properties.keySet()) {
                String str = key.toString();
                String numStr = str.substring(str.indexOf(".") + 1, str.lastIndexOf("."));
                Integer num = Integer.valueOf(numStr);
                if (!countMap.containsKey(num)) {
                    countMap.put(num, 0);
                }
                countMap.put(num, countMap.get(num) + 1);
            }
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                if (value == null || value != 3) {
                    LOGGER.error("zkcluster" + key + " has not 3 properties, please obey the correct rule!");
                    throw new RuntimeException("zkcluster" + key + " has not 3 properties, please obey the correct rule!");
                }
                ZkClusterInfo info = generateClusterInfo(String.valueOf(key), properties);
                CLUSTERS.put(info.getName(), info);
            }

        } catch (Exception e) {
            LOGGER.error("init zookeeper client, load zkcluster.properties failed", e);
            System.exit(0);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.warn("init zookeeper client, close file descriptor failed", e);
                }
            }
        }
    }

    private ZkClusterInfo generateClusterInfo(String num, Properties properties) {
        ZkClusterInfo info = new ZkClusterInfo();
        info.setNo(num);
        info.setAddr((String) properties.get("zkcluster." + num + ".addr"));
        info.setName((String) properties.get("zkcluster." + num + ".name"));
        info.setRootPath((String) properties.get("zkcluster." + num + ".rootpath"));
        return info;
    }

    private void initZkClients() {
        CuratorFramework client = null;
        for (final ZkClusterInfo server : CLUSTERS.values()) {
            client = CuratorFrameworkFactory.builder()
                    .connectString(server.getAddr())
                    .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                    .build();
            client.start();
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    if (newState.equals(ConnectionState.LOST)) {
                        LOGGER.error("damn it!!!!!!!!!!!!!!!!!!! connection lost!!!!!!!!!!!!!!!!!");
                    }
                }
            });
            ZK_CLIENT.put(server.getName(), client);
        }
        if (ZK_CLIENT.isEmpty()) {
            LOGGER.error("init zookeeper client, connect to zookeeper failed, no zk cluster attached");
        }
    }

    private static void findRegisteredServiceRecursion(String namespace, String path) throws Exception {
        CuratorFramework client = ZK_CLIENT.get(namespace);
        GetChildrenBuilder children = client.getChildren();
        List<String> nodes = children.forPath(path);
        boolean hasRegistered = false;
        if (nodes != null && !nodes.isEmpty()) {
            Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+$");
            for (String child : nodes) {
                Matcher m = p.matcher(child);
                if (m.matches()) {
                    LOGGER.info("#########add registered service # " + path + ":" + child);
                    RpcDataStructure data = new RpcDataStructure();
                    data.setInterfaceName(path.substring(path.indexOf(":") + 1, path.lastIndexOf(":")));
                    data.setPath(path.substring(0, path.lastIndexOf("/")));
                    data.setGroup(path.substring(path.lastIndexOf("/") + 1, path.indexOf(":")));
                    data.setVersion(path.substring(path.lastIndexOf(":") + 1));
                    data.setIp(child.substring(0, child.indexOf(":")));
                    data.setPort(child.substring(child.indexOf(":") + 1));
                    data.setNamespace(namespace);
                    data.setZkServer(CLUSTERS.get(namespace).getAddr());
                    data.setFullPath(path);
                    RpcLocalCache.put(data);
                    //为叶子节点注册watcher
                    if (!hasRegistered) {
                        registerWatcher4Node(namespace, path);
                        hasRegistered = true;
                    }
                    continue;
                }
                findRegisteredServiceRecursion(namespace, path + "/" + child);
            }
        }
        //为普通节点注册watcher
        if (!hasRegistered) {
            registerWatcher4Node(namespace, path);
        }
    }

    private static void registerWatcher4Node(final String namespace, final String path) throws Exception {
        CuratorWatcher watcher = new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) throws Exception {
                if (watchedEvent.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
                    refreshCache(namespace, path);
                }
            }
        };
        CuratorFramework client = ZK_CLIENT.get(namespace);
        List<String> stat = client.getChildren().usingWatcher(watcher).forPath(path);
        LOGGER.info(namespace + ":" + path + " add node watcher stat:" + stat);
    }

    //清理掉cache对应的变化的path的信息，重新把该path载入cache
    private static void refreshCache(String namespace, String path) throws Exception {
        List<RpcDataStructure> list = RpcLocalCache.getDataByFullPath(namespace, path);
        for (RpcDataStructure data : list) {
            if (data.getFullPath().equals(path)) {
                list.remove(data);
                LOGGER.info("####### delete service " + data.getInterfaceName() + data.getPath());
            }
        }
        findRegisteredServiceRecursion(namespace, path);
    }

    public static void main(String args[]) throws Exception {
        //StargateZkClient zk = new StargateZkClient();
        //zk.init();
        //CuratorFramework curatorFramework = ZK_CLIENT.get("QA环境");
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString("10.40.70.183:4888")
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                .connectionTimeoutMs(5000).build();
        curatorFramework.start();

        final CuratorWatcher watcher = new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) throws Exception {
                if (watchedEvent.getType().equals(Watcher.Event.EventType.NodeChildrenChanged)) {
                    System.out.println("path " + watchedEvent.getPath() + " child changed");
                }
            }
        };
        curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                System.out.println("connectionState:" + connectionState);
            }
        });
        curatorFramework.getChildren().usingWatcher(watcher).
                forPath("/registry/test/liangyafei");
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/registry/test/liangyafei/test", "test".getBytes("gbk"));

        Thread.sleep(60000);
    }
}
