package my;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class client {
    public static ArrayList<Socket> soc_list = new ArrayList<>();//socket列表
    static int first_cilent_port = 20100;
    static int first_server_port  =20000;
    OutputStream out;
    InputStream in;
    public static String file_name;
    public static long file_length;
    public static Socket first_client;
    static JTextArea notify;
//    String profile = "E:\\小鹿叮叮纸尿裤\\学习相关\\网络工程师综合训练\\file\\profile.txt";//文件发送的配置表，发送过的文件都记录在这里
    long[] profile_info = null;
    public client(JTextArea notify) throws IOException, InterruptedException {//初始化
        this.notify = notify;
        first_client = new Socket();
        set(first_client);   //绑定之前设定，不然设定就无效了
        first_client.bind(new InetSocketAddress(Inet4Address.getLocalHost(),first_cilent_port));
        first_client.connect(new InetSocketAddress(Inet4Address.getLocalHost(),first_server_port));
        out = first_client.getOutputStream();
        in = first_client.getInputStream();
        notify.append("client ready");

//        soc_list.add(first_client);
//        create_soc();
//        send_file_thread file_send = new send_file_thread(soc_list,file_name,1);
//        file_send.send_file();//发送文件
    }

    public void write(byte[] a) throws IOException {
        out.write(a);
        out.flush();
    }

    public static void appendJTextArea(String info) {
        notify.append(info);
        notify.paintImmediately(notify.getBounds());
        notify.setCaretPosition(notify.getDocument().getLength());
    }
    public static void set(Socket soc) throws SocketException {
        soc.setSoTimeout(20000);
        soc.setReuseAddress(true);//地址复用
        soc.setTcpNoDelay(true);//这个算法防止碎片化发送
        soc.setKeepAlive(true);//类似心跳包
        soc.setReceiveBufferSize(1024 *1024);
        soc.setSendBufferSize(1024 *1024);
    }

    public static void send_file_info(String file){
        File f = new File(file);
        file_length = f.length();
        String[] temp = file.split("\\\\");
        file_name = temp[temp.length-1];
    }

    public static void file_info_send() throws IOException {//传递长度名称
        OutputStream out = first_client.getOutputStream();
        InputStream in = first_client.getInputStream();
        byte[] first = other_frameFunction.string_frame(file_length,file_name);
        out.write(first);
        out.flush();
        appendJTextArea("\nclient发出请求");
//        notify.append("\nclient发出请求");
    }

    public int get_thread_num(){
        int i;
        do{
            try{
                i = in.read();
            } catch (IOException e) {
                continue;
            }
            break;
        }while(true);
        return i;
    }

    public static void create_soc(int num) throws IOException {//在这创建soc列表，多线程传输时要多个soc
        soc_list.add(first_client);
        appendJTextArea("\n发送线程"+1+"准备就绪"+"\n端口为："+20100);
//        notify.append("\n发送线程"+1+"准备就绪"+"\n端口为："+20100);
        for(int i=0;i<num-1;i++){
            Socket client_temp = new Socket();
            client_temp.bind(new InetSocketAddress(Inet4Address.getLocalHost(),first_cilent_port + i+1));
            client_temp.connect(new InetSocketAddress(Inet4Address.getLocalHost(),first_server_port));//全连接这一个就行
            soc_list.add(client_temp);
            appendJTextArea("\n发送线程"+(i+2)+"准备就绪\n端口为："+(first_cilent_port+i+1));
//            notify.append("\n发送线程"+(i+2)+"准备就绪\n端口为："+(first_cilent_port+i+2));
        }
    }

    public void create_profile(int Thread_num2){//创建数据表， （文件名，是否完成，各线程进度）(最终精简为（是否完成，各线程进度))因为就在创建一个时进行所以压根就不用管名字哈哈
        profile_info = new long[3+Thread_num2];
        for (int i=0;i<Thread_num2;i++){
            profile_info[i+3] = 0l;
        }
        profile_info[0] = 0;

    }

    public void recurent_send(ArrayList soc_list_temp,String file_name_temp,int thread_num_temp) throws IOException, InterruptedException {
        boolean able_send = true;
        if(profile_info == null){//没有数据表就创建，有就直接传递
            create_profile(thread_num_temp);
        }
        else{
            if(profile_info[0] == 1l){//发送已经完成就不能再发送
                able_send = false;
            }
        }
        if(able_send = false){
            appendJTextArea("\n你已经发送完成啦！");
        }
        else{
            appendJTextArea("\n开始发送");
//        notify.append("\n开始发送");
            send_file_thread file_send = new send_file_thread(soc_list_temp,file_name_temp,thread_num_temp,profile_info);
            file_send.send_file();
        }
    }

    public void start_send(ArrayList soc_list_temp,String file_name_temp,int thread_num_temp) throws IOException, InterruptedException {
        recurent_send(soc_list_temp,file_name_temp,thread_num_temp);//先发送一波
        int answer;
        while(profile_info[0] != 1l){
            do{
                try{
                    answer = in.read();//等待接收方的通知
                }catch (IOException e){
                    Thread.sleep(100);
                    continue;
                }
                break;
            }while(true);
            if(answer == 88){
                appendJTextArea("\n接收方要求继续，准备继续发送");
                String s = "";
                for(int m=0;m<profile_info.length-1;m++){
                    s+=profile_info[m+1]+",";
                }
                appendJTextArea("下次开始的位置:"+s);
                Thread.sleep(100);//怕可能接收方还没准备好，所以略微等待一下
                recurent_send(soc_list_temp,file_name_temp,thread_num_temp);//继续发送
            }
            else{
                Thread.sleep(1000);//小憩一会
            }
        }
    }
}
