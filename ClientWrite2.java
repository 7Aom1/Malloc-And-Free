package com.operationSystem.four;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * @author xwj
 * @date 2021/11/27 15:40
 */
public class ClientWrite2 {

    private volatile static boolean stop = true;

    public volatile static int flag = 0;

    public static Socket socket;

    public static int getFlag() {
        return flag;
    }

    public static void setFlag(int flag) {
        ClientWrite2.flag = flag;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("==�ͻ��˵�����==");
        // ��1������һ��Socket��ͨ�Źܵ������������˵Ķ˿����ӡ�
        socket = new Socket("127.0.0.1",9999);
        //Ϊ�ͻ��˵�socket����һ���߳� ר�Ÿ�������Ϣ
        new ClientRecive2(socket).start();
        int flag = 0;
        while(true){
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            if(getFlag()==0){
                //TODO �������bug��֮ǰ����1����������ס����2��ͨ����
                if (flag==0) {
                    /**
                     * send data of {@1} ,this is indicated current Malloc
                     */
                    dos.writeInt(1);
                    dos.writeInt(5);
                    dos.flush();
                    flag++;
                }
            }else {
                dos.writeInt(2);
                dos.flush();
                terminate();
            }
        }
    }
    public static void terminate(){
        stop =false;
    }
}