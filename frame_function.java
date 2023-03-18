package my;

import java.io.File;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class frame_function {

    public static long byte_to_long(byte[] b){
        long i = 0L;
        int count = 0;
        for (int j=0;j<8;j++){
            if(b[j] != 0)
                break;
            else
                count ++;
        }
        for (int j=count;j<b.length;j++){
            i = (i << 8) | (b[j] << 56) >>> 56;//他会先将byte强转为int，然后前面就全部补1了。。这个有符号数左移补0，右移补最高位
        }
        return i;
    }
    public static byte[] long_to_byte(long i){
        byte[] result = new byte[8];
        for (int j=0;j<8;j++){
            result[7 - j] = (byte)(i >> j*8);
//            System.out.printf("得到%d",result_temp[(7-j)]);
        }
        return result;
    }

    public static int byte_to_int(byte[] b){
        int i = 0;
        int count = 0;
        for (int j=0;j<4;j++){
            if(b[j] != 0)
                break;
            else
                count ++;
        }
        for (int j=count;j<b.length;j++){
            i = (i << 8) | (b[j] << 24) >>> 24;//他会先将byte强转为int，然后前面就全部补1了。。这个有符号数左移补0，右移补最高位
        }
        return i;
    }

    public static byte[] int_to_byte(int i){
        byte[] result = new byte[4];
        for (int j=0;j<4;j++){
            result[3 - j] = (byte)(i >> j*8);
        }
        return result;
    }

    public static byte[] MD5_hash(byte[] frame_content) {
        byte[] result = null;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5") ;
            md.update(frame_content);
            result = md.digest();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] create_frame(byte frame_type,byte serial_number,byte[] frame_content) {  //生成一个帧
        byte[] frame = new byte[2 + 1 + 1 + 16 + frame_content.length];//长度，类型，帧序号，hash，帧数据
        int frame_length = frame_content.length;//最长为为65535
        byte[] frame_hash = MD5_hash(frame_content);
        byte[] frame_length_data_temp = int_to_byte(frame_length);
        byte[] frame_length_data = new byte[2];
        System.arraycopy(frame_length_data_temp,2,frame_length_data,0,2);//得到的是4个字节，这里最多两个，所以复制一下
        for (byte i=0;i<2;i++){   //填充帧大小
            frame [i] = frame_length_data[i];//int有4位
        }
        frame[2] = frame_type;  //帧类型
        frame[3] = serial_number;//帧序号
        byte[] hash = MD5_hash(frame_content);  //填充hash值
        for (byte i=0;i<16;i++){
            frame[i+4] = hash[i];
        }
        System.arraycopy(frame_content,0,frame,20,frame_length);//填充帧内容
        return frame;
    }

    public static int get_frame_contant_length(byte[] frame){
        byte[] length = new byte[4];
        System.arraycopy(frame,0,length,2,2);
        int frame_contant_lenth = byte_to_int(length);
        return frame_contant_lenth;
    }

    public static byte get_frame_type(byte[] frame){
        byte frame_type = frame[2];
        return frame_type;
    }

    public static byte get_frame_serial_num(byte[] frame){
        byte frame_seiral_num = frame[3];
        return frame_seiral_num;
    }

    public static byte[] get_frame_hash(byte[] frame){
        byte[] hash = new byte[16];
        System.arraycopy(frame,4,hash,0,16);
        return hash;
    }

    public static byte[] get_frame_contant(byte[] frame){
        int contant_length = get_frame_contant_length(frame);
        byte[] frame_contant = new byte[contant_length];
        System.arraycopy(frame,20,frame_contant,0,contant_length);
        return frame_contant;
    }
}
