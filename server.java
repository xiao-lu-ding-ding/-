package my;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import my.ui.receive_ui.change_iscancel;
import my.ui.receive_ui.test_validation;

public class server {
    static byte[] fisrt_frame = new byte[1024];
    public static long file_length;
    public static String pre_file_name;
    static int file_name_length;
    static String file;
    public static ArrayList<Socket> soc_list = new java.util.ArrayList<>();
    public static ServerSocket first_server;
    public static Socket first_client;
    static int first_cilent_port = 20100;
    static int first_server_port  =20000;
    OutputStream out;
    InputStream in;
    static JTextArea notify;
    long[] profile_info = null;
    change_iscancel cs;
    test_validation tv;
    public server(JTextArea notify,change_iscancel cs,test_validation tv) throws IOException, InterruptedException {
        this.notify = notify;
        this.cs = cs;
        this.tv = tv;
        first_server = new ServerSocket();
        set(first_server);
        first_server.bind(new InetSocketAddress(Inet4Address.getLocalHost(),first_server_port));
        notify.append("server ready");
        init();
    }
    public void write(byte[] a) throws IOException {
        out.write(a);
        out.flush();
    }

    public void init() throws IOException, InterruptedException {
        boolean over = false;
        while(!over){
            first_client = first_server.accept();//等待客户连接
            notify.append("\n有客户端连接啦"+first_client.getLocalAddress());
            out = first_client.getOutputStream();
            in = first_client.getInputStream();
            file_info_receive();
            over = true;
        }
    }

    public static void set(ServerSocket soc) throws SocketException {
        soc.setReuseAddress(true);//地址复用
        soc.setReceiveBufferSize(1024 * 1024);//发送和接受缓冲，发送接收时大于这个会被拆分
    }
    public static void appendJTextArea(String info) {
        notify.append(info);
        notify.paintImmediately(notify.getBounds());
        notify.setCaretPosition(notify.getDocument().getLength());
    }
    public void file_info_receive() throws IOException {
        boolean ok = false;//首先读取发送方的文件发送请求
        do{
            try{
                int i = in.read();//读1byte
            }catch (IOException e){
                continue;
            }
            byte[] file_name_length_byte = new byte[4];
            in.read(file_name_length_byte);
            file_name_length = other_frameFunction.byte_to_int(file_name_length_byte);
            byte[] file_length_byte = new byte[8];
            in.read(file_length_byte);
            file_length = other_frameFunction.byte_to_long(file_length_byte);
            byte[] file_name_byte = new byte[file_name_length];
            in.read(file_name_byte);
            pre_file_name = new String(file_name_byte);
            appendJTextArea("\n文件名为："+pre_file_name);
//            notify.append("\n文件名为："+pre_file_name);
            long length_modify = file_length/(1024);
            String length_str = String.valueOf(length_modify)+"KB";
            if(length_modify >= 10000){
                length_modify /= 1024;
                length_str = String.valueOf(length_modify)+"MB";
            }
            appendJTextArea("\n文件长度为："+length_str);
//            notify.append("\n文件长度为："+length_str);
            ok = true;
        }while(!ok);
    }

    public void back_thread_num(int thread_num) throws IOException {//dialog进行调用
        //在接收到文件信息后要告诉发送方接受到了并且准备好接收了,用的是线程数
        byte[] t_n = {(byte) thread_num};
        write(t_n);
    }

    public void create_server_socket(int thread_num) throws IOException {//创建文件发送的socket
        soc_list.add(first_client);
        appendJTextArea("\n线程"+(1)+"准备就绪"+"\n端口为"+first_client.getLocalPort());
//        notify.append("\n线程"+(1)+"准备就绪"+"\n端口为"+first_client.getLocalPort());
        for(int i=0;i<thread_num-1;i++){
            Socket c = first_server.accept();//从这个里面获取client对象          这是阻塞的。nice
            soc_list.add(c);
            System.out.println("\n接收到端口"+c.getInetAddress());
            appendJTextArea("\n线程"+(i+1)+"准备就绪"+"\n端口为"+c.getLocalPort());
//            notify.append("\n线程"+(i+1)+"准备就绪"+"\n端口为"+c.getLocalPort());//获得连接的客户端

        }
        if(soc_list.size()<thread_num){
            appendJTextArea("\n线程数不够");
//            notify.append("\n线程数不够");
        }
    }
    public void create_profile(int Thread_num2){//创建数据表， （文件名，是否完成，各线程进度）(最终精简为（是否完成，各线程进度))因为就在创建一个时进行所以压根就不用管名字哈哈
        profile_info = new long[1+Thread_num2];
        for (int i=0;i<Thread_num2;i++){
            profile_info[i+1] = 0l;
        }
        profile_info[0] = 0;
    }

    public void recurent_receive(ArrayList soc_list_temp,String file_name_temp,int thread_num_temp,long file_length_temp,ArrayList processbar_list) throws IOException, InterruptedException {
        boolean able_receive = true;
        if(profile_info == null){//没有数据表就创建，有就直接传递
            create_profile(thread_num_temp);
        }
        else{
            if(profile_info[0] == 1l){//发送已经完成就不能再发送
                able_receive = false;
            }
        }
        if(able_receive = false){
            appendJTextArea("\n已经接收完成啦！");
        }
        else{
            appendJTextArea("\n开始接收");
            receive_file_thread file_receive = new receive_file_thread(soc_list_temp,file_name_temp,thread_num_temp,file_length_temp,processbar_list,profile_info,cs,tv);
            file_receive.receive_file();
        }
    }

    public volatile boolean issend = false;
    public void receive_file(ArrayList soc_list_temp,String file_name_temp,int thread_num_temp,long file_length_temp,ArrayList processbar_list)throws IOException, InterruptedException {
        recurent_receive(soc_list_temp,file_name_temp,thread_num_temp,file_length_temp,processbar_list);//先进性有ilu能接受，然后进入while循环（来应对暂停重传）
        while(profile_info[0] != 1l){//还没接收完  这个循环将进行到文件传输结束为止
            cs.iscancel = false;//这个要重置呀！！！
            if(issend == true){//等待上层传来的信号
                issend = false;//重置给下一次用
                appendJTextArea("\n通知发送方继续发送");
                String s = "";
                for(int m=0;m<profile_info.length-1;m++){
                    s+=profile_info[m+1]+",";
                }
                appendJTextArea("下次开始的位置:"+s);
                byte[] resend = {(byte)88};
                do{
                    try{
                        write(resend);//通知对方继续淦
                    }catch (IOException e){
                        System.out.println("socket错误？？？？");
                        Thread.sleep(1000);
                        continue;
                    }
                    break;
                }while(true);
                recurent_receive(soc_list_temp,file_name_temp,thread_num_temp,file_length_temp,processbar_list);//继续接收
            }
            else{//如果没有则等待一下在进入循环
                Thread.sleep(1000);
            }
        }
    }
}
