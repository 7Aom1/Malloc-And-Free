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
            // ѭ��һֱ�ȴ��ͻ��˵���Ϣ
            while(stop){
                //��ȡ��ǰ����Ϣ����
                int flag = dis.readInt();
                //�յ���Ϣ��Ŀǰ����������
                if(flag == 1){
                    String backMessage = dis.readUTF();
                    System.out.println(backMessage+"���Կ��ٿռ�");
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
