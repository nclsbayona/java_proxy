package proxy;

import java.util.Objects;

public class VirtualHost {
    private String virtual_host;
    private String real_host;
    private String root_directory;


    public VirtualHost() {
    }


    public VirtualHost(String virtual_host, String real_host, String root_directory) {
        this.virtual_host = virtual_host;
        this.real_host = real_host;
        this.root_directory = root_directory;
    }

    public String getVirtual_host() {
        return this.virtual_host;
    }

    public void setVirtual_host(String virtual_host) {
        this.virtual_host = virtual_host;
    }

    public String getReal_host() {
        return this.real_host;
    }

    public void setReal_host(String real_host) {
        this.real_host = real_host;
    }

    public String getRoot_directory() {
        return this.root_directory;
    }

    public void setRoot_directory(String root_directory) {
        this.root_directory = root_directory;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof VirtualHost)) {
            return false;
        }
        VirtualHost virtualHost = (VirtualHost) o;
        return Objects.equals(this.getVirtual_host(), virtualHost.virtual_host) && Objects.equals(this.getReal_host(), virtualHost.real_host) && Objects.equals(this.getRoot_directory(), virtualHost.root_directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(virtual_host, real_host, root_directory);
    }

    @Override
    public String toString() {
        return "{" +
            " virtual_host='" + getVirtual_host() + "'" +
            ", real_host='" + getReal_host() + "'" +
            ", root_directory='" + getRoot_directory() + "'" +
            "}";
    }

}
