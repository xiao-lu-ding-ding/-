package my;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import my.other_frameFunction.current_thread_already_done;

public class send_frame_dispatch extends Thread{  //这个主要还是负责调度， 终于差不多理清关系了，发送调度，shit你竟然还要负责多线程
    long start,file_length;
    int total_frame_num;
    Socket soc;
    static int MAX_frame_data_length = 65535;
    String file_name;
    RandomAccessFile randfile;
    FileChannel channel;
    FileLock filelock;
    file_frameFunction file_frame_group;
    int every_turn_frame_num = 127;//这里就每次发127帧
    int MAX_turn_byte = every_turn_frame_num * MAX_frame_data_length;//每turn最大传输的byte数
    data_buffer_function data_send_buffer;
    boolean iscancle = false;
    current_thread_already_done current_Thread_send_frame_num = new current_thread_already_done();//记录当前线程发送的帧数，创建时就为0
    long current_Thread_send_byte;
    ArrayList every_thread_already_send;
    int thread_num;
    public send_frame_dispatch(Socket soc, String file_name,long start, long file_length,int thread_num,ArrayList every_thread_already_send) throws IOException {
        this.start = start;
        this.soc = soc;
        this.randfile = new RandomAccessFile(file_name,"r");
        randfile.seek(start);//设置该线程读取开始的位置
        this.channel = randfile.getChannel();
        filelock = channel.lock(start,file_length,true);
        this.every_thread_already_send = every_thread_already_send;
        this.thread_num = thread_num;
        this.file_length = file_length;
        this.total_frame_num = (int)(file_length / MAX_frame_data_length);
        if((file_length % MAX_frame_data_length) != 0)
            total_frame_num ++;
        this.file_frame_group = new file_frameFunction(randfile);
        this.data_send_buffer = new data_buffer_function(soc.getOutputStream(),soc.getInputStream());
    }

    @Override
    public void run() {  //turn就是每次交给缓冲区的帧数
        int turn_times = total_frame_num / every_turn_frame_num;
        if(total_frame_num % every_turn_frame_num != 0 )
            turn_times ++;
        for (int i=0;i<turn_times;i++){
            if(data_send_buffer.iscancel == false){
                long start_part = i * every_turn_frame_num * MAX_frame_data_length;
                int part_length = (i == turn_times - 1) ? (int) (file_length - start_part) : MAX_turn_byte;//到了最后一轮就不是最大值了
                int current_turn_frame_num = (part_length < MAX_turn_byte) ?    //这三行是计算最后一批的帧数，太长了分成三行
                        ((part_length % MAX_frame_data_length == 0) ? (part_length / MAX_frame_data_length) :
                                (part_length / MAX_frame_data_length) + 1) : every_turn_frame_num;
                ArrayList frame_group = null;
                try {
                    frame_group = file_frame_group.send_frame_group(part_length);//流固定了，每次读取指定数量就可以了。都是从头开始
                    byte[] first_frame = other_frameFunction.int_frame((byte) current_turn_frame_num);
                    frame_group.add(0,first_frame); //在首部添加首帧
                    data_send_buffer.send(frame_group) ;//开启发送，如果被监听停止了，那么它也结束
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //发送完关闭流
        try {
            if (data_send_buffer.current_Thread_frame_num == total_frame_num){
                current_Thread_send_byte = file_length;
                every_thread_already_send.set(thread_num,current_Thread_send_byte);
            }
            else{//记录终止后当前线程已经发送的数据
                current_Thread_send_byte = data_send_buffer.current_Thread_frame_num * MAX_frame_data_length;
                every_thread_already_send.set(thread_num,current_Thread_send_byte);//给上层返回每个线程发了多少字节
            }
            filelock.release();
            channel.close();
            randfile.close();
            //socket的关闭交给上一层，因为可能进行的时重发，这一层不好确定有没有发完。
        } catch (IOException e) {
            client.appendJTextArea("\n发送结束，关闭socket");
//            e.printStackTrace();
        }
        client.appendJTextArea("\n发送线程"+(thread_num+1)+"结束");
    }
}
