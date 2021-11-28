package com.operationSystem.four;

import java.io.DataInputStream;
import java.net.Socket;

/**
 * @author xwj
 * @date 2021/11/27 16:07
 */
public class ClientRecive2 extends Thread{

    private volatile static boolean stop = true;

    private Socket socket;

    public ClientRecive2(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            // 循环一直等待客户端的消息
            while(stop){
                //读取当前的消息类型
                int flag = dis.readInt();
                //收到消息，目前正在运行中
                if(flag == 1){
                    String backMessage = dis.readUTF();
                    System.out.println(backMessage+"：已经拿到内存空间");
                    for (int i = 0; i < 5; i++) {
                       sleep(10000);
                        System.out.println("正在运行中，距离运行结束还有："+i);
                    }
                    ClientWrite2.setFlag(2);
                    terminate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void terminate(){
        stop =false;
    }
}
