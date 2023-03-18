package my;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class file_frameFunction extends frame_function_source {
    RandomAccessFile randfile;
    static int MAX_frame_data_length = 65535;//帧里面数据最大长度为65535
    public file_frameFunction(RandomAccessFile randfile){//将stream直接传进来，不然的话要频繁打开，浪费
        this.randfile = randfile;
    }

    public ArrayList send_frame_group(int part_length) throws IOException {  //每个文件中一块，发送整个就是开头到结尾，以后多线程就是分多个package
        ArrayList<byte[]> frame_group = new ArrayList<>();
        long frame_num = (part_length / MAX_frame_data_length) ;
        if ((part_length % MAX_frame_data_length)!= 0)//意味着最后一帧不满
            frame_num ++;

        int part_length_remain = part_length;
        for (byte i=0;i<frame_num;i++){
            int hasread = 0;
            int frame_contant_length = MAX_frame_data_length;
            if(part_length_remain < MAX_frame_data_length){
                frame_contant_length = part_length_remain;
            }
            byte[] frame_contant = new byte[frame_contant_length];//填充完了再减
            part_length_remain -= MAX_frame_data_length;
            hasread = randfile.read(frame_contant);
            byte[] frame = create_frame((byte) 1,(byte)(i+1),frame_contant);   //不考虑0是因为还有头帧
            frame_group.add(frame);
        }
        return frame_group; //由despatch得到并交由发送
    }
}
