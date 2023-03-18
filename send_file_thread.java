package my;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class send_file_thread {//最高层的文件发送
    String file_name;
    int Thread_num;
    boolean iscancle = false;
    ArrayList soc_list;//从这个socket发送
    ArrayList every_thread_already_send = new ArrayList();
    ExecutorService fixedpool;
    File f;
    long[] profile_info;//传进来的要么是全0，要么是没发完的，不会是发送完成的
    public send_file_thread(ArrayList soc_list, String file_name, int Thread_num,long[] profile_info){//终于可以多线程了
//        System.out.println("首socket为"+(Socket)soc_list.get(0)+" 文件名："+file_name+" 线程数："+Thread_num);
        this.file_name = file_name;
        this.Thread_num = Thread_num;
        this.soc_list = soc_list;
        this.profile_info = profile_info;
        for(int i=0;i<Thread_num;i++){
            every_thread_already_send.add(0l);
        }
    }

    public void send_file() throws IOException, InterruptedException {//对文件进行拆分交给dispatch
        f = new File(file_name);
        long file_length = f.length();
        long every_Thread_length = file_length / Thread_num;
        long last_turn_length = file_length % Thread_num + (file_length / Thread_num);//多下来的byte补给最后一个线程
        fixedpool = Executors.newFixedThreadPool(Thread_num);//创建线程池，这个线程池容纳的数量为thread_num
        for (int i=0;i<Thread_num;i++){
            long Thread_start = i * every_Thread_length;
            long Thread_file_length = every_Thread_length;
            if ((i == Thread_num - 1)&last_turn_length != 0){
                Thread_file_length = last_turn_length;
            }
            Socket temp = (Socket)soc_list.get(i);
//            System.out.printf("send_thread 发送线程为：%s，\n该线程传输文件开始和长度 %d  %d\n",i,
//                    Thread_start,Thread_file_length);
            long current_Thread_start = Thread_start + profile_info[i+1];//因为会有重发的缘故，这里要重新更新
            long current_Thread_file_length = Thread_file_length - profile_info[i+1];
            Thread a = new send_frame_dispatch((Socket)soc_list.get(i),file_name,current_Thread_start,current_Thread_file_length,i,every_thread_already_send);//进度条和该线程起点,把线程号和已发送传进去
            fixedpool.execute(a);//加入线程池
//            a.start();  //线程开启
        }
        fixedpool.shutdown();//结束线程
        while(true){//检查县城有没有执行完
            if(fixedpool.isTerminated())
                break;
            else
                Thread.sleep(1000);
        }
        long total_byte = 0l;
        for(int i=0;i<every_thread_already_send.size();i++){
            long num = (long)every_thread_already_send.get(i);
            profile_info[i+1] += num;//之前我这里竟然设置的是赋值，大错特错呀，运行完线程就结束了，；不会保存之前发了多少帧的，只有这里会保存
            total_byte += profile_info[i+1];
        }
        if(total_byte == f.length()){//发送完
            profile_info[0] = 1l;//发送完后将其置为1,然后socket才能够关闭，其他时候只是关闭流
            for(Object i :soc_list){
                Socket get_i = (Socket)i;
                get_i.close();
            }
        }
        client.appendJTextArea("\nsend stop");
    }
}
