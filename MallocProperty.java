package com.operationSystem.four;

import java.net.Socket;

/**
 * @author xwj
 * @date 2021/11/26 16:22
 */
public class MallocProperty{

    private Socket socket;

    private int counts;


    public int getCounts() {
        return counts;
    }

    public void setCounts(int counts) {
        this.counts = counts;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public MallocProperty(Socket socket, int counts) {
        this.socket = socket;
        this.counts = counts;
    }
}
