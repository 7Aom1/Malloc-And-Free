package com.operationSystem.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * @author xwj
 * @date 2021/11/27 16:07
 */
public class ClientRecive extends Thread{

    private volatile static boolean stop = true;

    private Socket socket;

    public ClientRecive(Socket socket) {
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
                    System.out.println(backMessage+"可以开辟空间");
                    ClientWrite.setFlag(2);
                    terminate();
//                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
//                    dos.writeInt(2);
//                    terminate();
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
