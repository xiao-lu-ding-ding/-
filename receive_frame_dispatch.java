package my;

import javax.swing.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import my.ui.receive_ui.change_iscancel;
import my.ui.receive_ui.test_validation;

public class receive_frame_dispatch extends Thread{//需要处理多线程文件的接收，一个socket同一时间只能传输或者接收一个，所以需要多个socket
    Socket soc;
    long start,length;
    int every_turn_num = 127;
    static int MAX_frame_data_length = 65535;
    int total_frame_num;
    data_buffer_function data_receive_buffer;
    RandomAccessFile randfile;
    FileChannel channel;
    FileLock filelock;
    MappedByteBuffer mbb;
//    volatile Boolean iscancle;  //监听暂停发送的信号  前面那个修饰词表示更改了之后立刻刷新
    change_iscancel cs;
    test_validation tv;
    long current_Thread_receive_byte;
    JProgressBar thread_process;
    int current_thread_serial;
    ArrayList every_thread_already_receive;
    int pre_send_frame_num;
    public receive_frame_dispatch(Socket soc,String file_name,long start,long length,JProgressBar thread_process,int current_thread_serial,ArrayList every_thread_already_receive,int pre_send_frame_num,change_iscancel cs,test_validation tv) throws IOException{   //多线程接收的soc
        this.soc = soc;
        this.randfile = new RandomAccessFile(file_name,"rw");
        this.channel = randfile.getChannel();
        filelock = channel.lock(start, length, true);//感觉应该在这里加锁
        this.start = start;
        this.length = length;
        this.thread_process = thread_process;
        this.current_thread_serial = current_thread_serial;
        this.every_thread_already_receive = every_thread_already_receive;
        this.pre_send_frame_num = pre_send_frame_num;
//        System.out.println("之前的send"+pre_send_frame_num);
        this.cs = cs;
//        cs = false; //每次创建时线程时，也就是开始发送时将其置为零
        this.tv = tv;
        int temp = (int)(length / MAX_frame_data_length);
        this.total_frame_num = (length % MAX_frame_data_length == 0) ? temp : (temp+1);
        data_receive_buffer = new data_buffer_function(soc.getOutputStream(),soc.getInputStream());
        data_receive_buffer.pre_frame_num = pre_send_frame_num;
        data_receive_buffer.total_frame_num = total_frame_num;
        data_receive_buffer.thread_process = thread_process;
        data_receive_buffer.cs = cs;
        data_receive_buffer.tv = tv;
    }

    @Override
    public void run() {
        long write_start = start;
        int turn_times = total_frame_num / every_turn_num;
        turn_times = (total_frame_num % every_turn_num == 0) ? turn_times : (turn_times + 1);
        for(int i=0;i<turn_times;i++){
            if(data_receive_buffer.iscancel == false){//只有这个为false才继续发送
                try {
                    data_receive_buffer.receive();
                    int last_size = 0;
                    for(int j=0;j<data_receive_buffer.receive_buffer.size();j++){//本来是想在data——buffer里完成的，但是里面还要经过校验，不能一步确定最终大小，所以还是这里搞
                        last_size += data_receive_buffer.receive_buffer.get(j).length;//统计总共有多少，一轮最多是8M
                    }
                    byte[] write_buffer = new byte[last_size];
                    int point = 0;
                    for(int j=0;j<data_receive_buffer.receive_buffer.size();j++){
                        byte[] frame_contant = data_receive_buffer.receive_buffer.get(j);
                        int frame_contant_length = frame_contant.length;
                        System.arraycopy(frame_contant,0,write_buffer,point,frame_contant_length);//将读取的127帧填入数组，一次性写入（因为我多先线程老出问题，这样先试一下）
                        point += frame_contant_length;
                    }
                    mbb = channel.map(FileChannel.MapMode.READ_WRITE,write_start, last_size);
                    mbb.put(write_buffer);
                    mbb.force();
                    write_start += last_size;//写文件指针偏移
                    data_receive_buffer.set_receive_buffer();//讲接收列表清空，主备下一轮
                } catch (IOException | InterruptedException e) {
                    server.appendJTextArea("\n中断连接，线程发送结束");
                    e.printStackTrace();
                }
            }
        }
        try{
            if(data_receive_buffer.current_Thread_frame_num == total_frame_num){
                current_Thread_receive_byte = length;
                every_thread_already_receive.set(current_thread_serial,current_Thread_receive_byte);
            }
            else{
                current_Thread_receive_byte = data_receive_buffer.current_Thread_frame_num * MAX_frame_data_length;
                every_thread_already_receive.set(current_thread_serial,current_Thread_receive_byte);
            }
            //mbbb不释放程序内好像没法删除他掌握的文件，因为他的句柄还在，不过我也不用在程序里删除。而且我找了版太难，找不到什么好方法，sun.misc.Cleaner这个类又不知道是什么问题，八嘎
            filelock.release();
            channel.close();
            randfile.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        server.appendJTextArea("\n接收线程"+(current_thread_serial+1)+"结束");
    }
}
