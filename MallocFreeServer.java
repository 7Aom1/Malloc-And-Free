package com.operationSystem.four;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xwj
 * @date 2021/11/24 8:08
 */
public class MallocFreeServer {

    /**
     *  Init Memory
     *  Creates a {@code byte[]} with the default {@initial} capacity and
     *  whose elements are all the following:
     *     0	1	1	0	0	1	1	1	0
     *     1	0	1	0	1	0	1	0	0
     *     2	0	0	0	0	0	0	0	0
     *     3	1	0	0	0	0	0	0	1
     *     4	0	0	0	0	0	0	0	0
     *     5	0	0	0	0	0	0	0	0
     *     6	0	0	0	0	0	0	0	0
     *     7	0	0	0	0	0	0	0	0
     */
    volatile static char[] initMemory = new char[]
                            {0B111_0011,
                            0B1010_10,
                            0B0,
                            0B1000_0001,
                            0B0,
                            0B0,
                            0B0,
                            0B0};

    /**
     * create a short number , the value of the memory max size is 64
     */
    public final static short MAX_MEMORY_SIZE =  1<<6;

    /**
     * MallocMap
     * Create a hash table,and key is socket ,value is available space
     * why high expected concurrency for updates?
     * because all thread maybe change the order of table
     */
    volatile static Map<String, Queue<Integer>> map = new HashMap<>();

    /**
     *  Priority queue
     *  Creates a {@code PriorityQueue} with the default initial capacity and
     *  whose elements are ordered according to the specified comparator.
     */
    static PriorityQueue<MallocProperty> priorityQueue =
            new PriorityQueue<>(Comparator.comparingInt(MallocProperty::getCounts));

    /**
     * Creates a new {@code ReentrantReadWriteLock} with
     * the given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    /**
     * Malloc Memory
     * Erases the original bit number
     */
    public static void malloc(int needs) {
        System.out.println("分配之前打印内存块--------------------------");
        display();
        char[] copyMemory = new char[8];
        System.arraycopy(initMemory,0,copyMemory,0,initMemory.length);

        for (int i = 0; i < initMemory.length; i++) {
            char a = 0B11111111;
            while (initMemory[i]!=a){
                if(needs>0){
                    //将0变成1
                    initMemory[i] = (char) ((initMemory[i]+1 ) | initMemory[i]);
                    needs--;
                }else break;
            }
        }

        Queue<Integer> list = new LinkedList<>();
        for (int i = 0; i < initMemory.length; i++) {
            for (short j = 0; j < (1<<3); j++) {
                if (((copyMemory[i] & (1 << j))!= (1 << j))&&((initMemory[i] & (1 << j)) == (1 << j))) {
                    list.add(i*(1<<3)+j);
                    map.put("1",list);
                }
            }
        }
        System.out.println("分配之后打印内存块-----------------------------------");
        display();
    }

    /**
     * Free Memory
     */
    public static void free(Socket socket){
        System.out.println("开始回收---------------------------");
        Queue<Integer> queue = map.get("1");
        while (!queue.isEmpty()){
            Integer poll = queue.poll();
            int i = poll/ 8;
            int j =poll%8;
            initMemory[i]= (char) (initMemory[i]^(1<<j));
        }
        System.out.println("回收之后打印内存块--------------------------");
        display();
    }

    /**
     * check memory was available
     */
    public static boolean checkMemory(int needs) {
        int memoryCounts = getMemoryCounts();
        return memoryCounts >= needs;
    }

    /**
     * get current memory Size
     */
    public static int getMemoryCounts() {
        lock.readLock().lock();
        AtomicInteger available = new AtomicInteger();
        for (char b : initMemory) {
            for (short i = 0; i < (1<<3); i++) {
                if ((b & (1 << i)) == (1 << i)) {
                    available.getAndIncrement();
                }
            }
        }
        lock.readLock().unlock();
        return MAX_MEMORY_SIZE-available.get();

    }

    /**
     * display memory
     */
    public static void display(){
        System.out.println("当前剩余大小："+getMemoryCounts());
        for (char c : initMemory) {
            String s = Integer.toBinaryString(c);
            int bit = 8 - s.length();
            if (s.length() < 8) {
                for (int j = 0; j < bit; j++) {
                    s = "0" + s;
                }
            }
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("服务器端开始启动");
            ServerSocket ss = new ServerSocket(9999);
            HandlerSocketThreadPool handlerSocketThreadPool =
                    new HandlerSocketThreadPool(3, 100);
            while (true) {
                Socket socket = ss.accept();
                handlerSocketThreadPool.execute(new ReaderClientRunnable(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ReaderClientRunnable implements Runnable {


    private Socket socket;

    public ReaderClientRunnable(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            while (true){
             InputStream is = socket.getInputStream();
             DataInputStream dis = new DataInputStream(is);

             // read the  data into the buffer 1:malloc 2:free
             int flag = dis.readInt();
             int needs = dis.readInt();
             MallocProperty mallocProperty = new MallocProperty(socket, needs);
             //读取当前的消息类型 ：malloc/free
             if (flag == 1) {
                 System.out.println("服务端接收到请求");
                 System.out.println("请求类型:"+"开辟空间"+"\n需要的内存块:"+needs);
                 //检测当前内存块是否足够，不够就挂起来
                 boolean b = MallocFreeServer.checkMemory(needs);
                 if (b) {
                     MallocFreeServer.malloc(needs);
                     //send back to client value {@1}
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     dos.writeInt(1);
                     dos.writeUTF(socket.getRemoteSocketAddress().toString());
                     dos.flush();
                 } else {
                     //blocking state
                     System.out.println(socket.getRemoteSocketAddress()+"需要"+needs+"块，"+"当前仅剩："+MallocFreeServer.getMemoryCounts()+"暂时不够，已经挂起！");
                     MallocFreeServer.priorityQueue.add(mallocProperty);
                 }
             }
             if (flag==2){
                 System.out.println("服务端接收到请求，需要回收内存块");
                 //current socket client free memory
                 MallocFreeServer.free(socket);
                 //again get memory
                 if (!MallocFreeServer.priorityQueue.isEmpty()) {
                     MallocProperty poll = MallocFreeServer.priorityQueue.poll();
                     Socket socket = poll.getSocket();
                     boolean b = MallocFreeServer.checkMemory(poll.getCounts());
                     if (b) {
                         MallocFreeServer.malloc(needs);
                         //send back to client
                         DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                         dos.writeInt(1); // 回调类型
                         dos.writeUTF("--->" + this.socket.getRemoteSocketAddress());
                         dos.flush();
                     }
                 }
                break;
             }
         }
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
}