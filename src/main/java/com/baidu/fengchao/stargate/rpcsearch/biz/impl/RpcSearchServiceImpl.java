package com.baidu.fengchao.stargate.rpcsearch.biz.impl;

import com.baidu.fengchao.stargate.rpcsearch.biz.RpcSearchService;
import com.baidu.fengchao.stargate.rpcsearch.biz.bo.RpcDataStructure;
import com.baidu.fengchao.stargate.rpcsearch.sal.RpcLocalCache;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * author: liangyafei
 * date: 14-3-7
 */
@Service
public class RpcSearchServiceImpl implements RpcSearchService{

    @Override
    public List<RpcDataStructure> searchRPCInfo(String namespace, String query) {
        return RpcLocalCache.get(namespace, query);
    }
}
