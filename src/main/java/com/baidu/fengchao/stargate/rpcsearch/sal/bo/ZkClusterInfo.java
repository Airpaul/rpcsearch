package com.baidu.fengchao.stargate.rpcsearch.sal.bo;

/**
 * author: liangyafei
 * date: 14-3-11
 */
public class ZkClusterInfo {
    private String no;
    private String name;
    private String rootPath;
    private String addr;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
