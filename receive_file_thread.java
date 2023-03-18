package my;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import my.ui.receive_ui.change_iscancel;
import my.ui.receive_ui.test_validation;

public class receive_file_thread {
    String file_name ;//这个是接收文件的文件名，由myserver传过来
    int Thread_num;
    ArrayList soc_list; //从这个socket接收
    long file_length;//相比send，这个文件还不存在，所以得把长度传过来
    ArrayList<JProgressBar> processbar_list;
    ArrayList every_thread_already_receive = new ArrayList();
    ExecutorService fixedpool;
    long[] profile_info;
    change_iscancel cs;
    test_validation tv;
    public receive_file_thread(ArrayList soc_list, String file_name, int Thread_num,long file_length,ArrayList processbar_list,long[] profile_info,change_iscancel cs,test_validation tv) throws FileNotFoundException {
        this.file_name = file_name;
        this.Thread_num = Thread_num;
        this.soc_list = soc_list;
        this.file_length = file_length;
        this.processbar_list = processbar_list;
        this.profile_info = profile_info;
        this.cs = cs;//来自用户的操作
        this.tv = tv;//测试校验功能
//        server.appendJTextArea("首socket为"+(Socket)soc_list.get(0)+" 文件名："+file_name+" 线程数："+Thread_num+" 文件长度："+file_length);
        for(int i=0;i<Thread_num;i++){
            every_thread_already_receive.add(0l);
        }
    }

    public void receive_file() throws IOException, InterruptedException {
        int[] pre_send_frame_num = new int[Thread_num];//之前发送帧的数量信息，主要是为了设置进度条用的
        for(int j=0;j<Thread_num;j++){
            pre_send_frame_num[j] = (int)profile_info[j+1]/65535;//计算之前发送的帧数
        }
        long every_Thread_length = file_length / Thread_num;
        long last_turn_length = file_length % Thread_num + every_Thread_length;
        fixedpool = Executors.newFixedThreadPool(Thread_num);//创建线程池，这个线程池容纳的数量为thread_num
        for(int i=0;i<Thread_num;i++){
            long Thread_start = i * every_Thread_length;
            if(i == Thread_num -1)
                every_Thread_length = last_turn_length;
            JProgressBar processbar_temp = processbar_list.get(i);
            long current_Thread_start = Thread_start + profile_info[i+1];
            long current_every_Thread_length = every_Thread_length - profile_info[i+1];
            Thread a = new receive_frame_dispatch((Socket)soc_list.get(i),file_name,current_Thread_start,current_every_Thread_length,processbar_temp,i,every_thread_already_receive,pre_send_frame_num[i],cs,tv);
            fixedpool.execute(a);
//            a.start();
        }

        fixedpool.shutdown();//结束所有线程
        while(true){//检查县城有没有执行完
            if(fixedpool.isTerminated())
                break;
            else
                Thread.sleep(1000);
        }
        long total_byte = 0l;
        for(int i=0;i<every_thread_already_receive.size();i++){
            long num = (long)every_thread_already_receive.get(i);
            profile_info[i+1] += num;//之前我这里竟然设置的是赋值，大错特错呀，运行完线程就结束了，；不会保存之前发了多少帧的，只有这里会保存
            total_byte += profile_info[i+1];
        }
        if(total_byte == file_length) {//发送完了
            profile_info[0] = 1l;//发送完后将其置为1,然后socket才能够关闭，其他时候只是关闭流
            for (Object i : soc_list) {
                Socket get_i = (Socket) i;
                get_i.close();
            }
        }
        server.appendJTextArea("\nreceive stop");
    }
}
