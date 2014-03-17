package com.baidu.fengchao.stargate.rpcsearch.biz;

import com.baidu.fengchao.stargate.rpcsearch.biz.bo.RpcDataStructure;

import java.util.List;

/**
 * author: liangyafei
 * date: 14-3-7
 */
public interface RpcSearchService {

    /**
     * 根据查询条件查询RPC信息
     * @param namespace
     * @param query
     * @return
     */
    List<RpcDataStructure> searchRPCInfo(String namespace, String query);
}
