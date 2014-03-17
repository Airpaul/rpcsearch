package com.baidu.fengchao.stargate.rpcsearch.biz.bo;

/**
 * author: liangyafei
 * date: 14-3-7
 */
public class RpcDataStructure {
    private String interfaceName;
    private String path;
    private String version;
    private String ip;
    private String port;
    private String namespace;
    private String zkServer;
    private String group;
    private String fullPath;

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getZkServer() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "RpcDataStructure{" +
                "interfaceName='" + interfaceName + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RpcDataStructure that = (RpcDataStructure) o;

        if (!interfaceName.equals(that.interfaceName)) return false;
        if (!ip.equals(that.ip)) return false;
        if (!path.equals(that.path)) return false;
        if (!port.equals(that.port)) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = interfaceName.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + ip.hashCode();
        result = 31 * result + port.hashCode();
        return result;
    }
}
