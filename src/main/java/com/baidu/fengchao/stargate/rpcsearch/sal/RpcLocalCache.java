package com.baidu.fengchao.stargate.rpcsearch.sal;

import com.baidu.fengchao.stargate.rpcsearch.biz.bo.RpcDataStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * author: liangyafei
 * date: 14-3-7
 */
public class RpcLocalCache {
    //全局cache，key为命名空间，隔离各类环境
    private static final Map<String, Map<String, List<RpcDataStructure>>> CACHE = new ConcurrentHashMap<String, Map<String, List<RpcDataStructure>>>();

    public static void put(RpcDataStructure serviceInfo) {
        String fullPath = serviceInfo.getFullPath();
        String namespace = serviceInfo.getNamespace();
        if (!CACHE.containsKey(namespace)) {
            CACHE.put(namespace, new ConcurrentHashMap<String, List<RpcDataStructure>>());
        }
        Map<String, List<RpcDataStructure>> bucket = CACHE.get(namespace);
        if (!bucket.containsKey(fullPath)) {
            bucket.put(fullPath, new CopyOnWriteArrayList<RpcDataStructure>());
        }
        List<RpcDataStructure> list = bucket.get(fullPath);
        list.add(serviceInfo);
    }

    /**
     * 模糊查询
     * @param namespace
     * @param query
     * @return
     */
    public static List<RpcDataStructure> get(String namespace, String query) {
        List<RpcDataStructure> rtn = new ArrayList<RpcDataStructure>();
        Map<String, List<RpcDataStructure>> bucket = CACHE.get(namespace);
        if (bucket != null) {
            for (String str : bucket.keySet()) {
                if (str.contains(query)) {
                    rtn.addAll(bucket.get(str));
                }
            }
        }
        return rtn;
    }

    /**
     * 根据full path 精确查询
     * @param namespace
     * @param fullPath
     * @return
     */
    public static List<RpcDataStructure> getDataByFullPath(String namespace, String fullPath) {
        Map<String, List<RpcDataStructure>> bucket = CACHE.get(namespace);
        if (bucket != null && bucket.containsKey(fullPath)) {
            return bucket.get(fullPath);
        }
        return Collections.emptyList();
    }

    public static Map<String, List<RpcDataStructure>> getBucket(String namespace) {
           return CACHE.get(namespace);
    }

    public static Set<String> getNameSpace() {
        return CACHE.keySet();
    }
}
