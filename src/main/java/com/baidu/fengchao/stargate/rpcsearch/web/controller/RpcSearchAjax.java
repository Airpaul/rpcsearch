package com.baidu.fengchao.stargate.rpcsearch.web.controller;

import com.baidu.fengchao.stargate.rpcsearch.biz.RpcSearchService;
import com.baidu.fengchao.stargate.rpcsearch.biz.bo.RpcDataStructure;
import com.baidu.fengchao.stargate.rpcsearch.sal.RpcLocalCache;
import com.baidu.fengchao.stargate.rpcsearch.web.common.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * author: liangyafei
 * date: 14-3-7
 */
@Controller
public class RpcSearchAjax {

    @Autowired
    private RpcSearchService rpcSearchService;

    @RequestMapping("search")
    @ResponseBody
    public CommonResponse<List<RpcDataStructure>> searchRpc(HttpServletRequest request) {
        String query = StringUtils.trimWhitespace(request.getParameter("query"));
        String namespace = StringUtils.trimWhitespace(request.getParameter("namespace"));
        List<RpcDataStructure> services = rpcSearchService.searchRPCInfo(namespace, query);
        CommonResponse<List<RpcDataStructure>> response = new CommonResponse<List<RpcDataStructure>>();
        response.setData(services);
        if (services.isEmpty()) {
            response.setStatus(400);
        }
        return response;
    }

    @RequestMapping("fetchEnv")
    @ResponseBody
    public CommonResponse<List<String>> fetchEnv(HttpServletRequest request) {
        List<String> envs = new ArrayList<String>(RpcLocalCache.getNameSpace());
        CommonResponse<List<String>> response = new CommonResponse<List<String>>();
        response.setData(envs);
        if (envs.isEmpty()) {
            response.setStatus(400);
        }
        return response;
    }
}
