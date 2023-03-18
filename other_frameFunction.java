package my;

public class other_frameFunction extends frame_function_source {  //用来发送字符串

    public static byte[] string_frame(long file_length,String file_name){  //这里注意，这里主要是文件传输，有字符串数据一般也应该较短，长的话地特殊处理，先不写
        byte[] frame_file_name = file_name.getBytes();
        byte[] frame_file_length = frame_function.long_to_byte(file_length);
        int frame_length = frame_file_length.length + frame_file_name.length + 5;//我人都傻了，这个直接放到下面得到的大小就是比实际少2？？？
        byte[] String_frame = new byte[frame_length];
        String_frame[0] = 1;//作为接收时的首个byte
        byte[] frame_name_length_byte = int_to_byte(frame_file_name.length);
        System.arraycopy(frame_name_length_byte,0,String_frame,1,4);
        System.arraycopy(frame_file_length,0,String_frame,5,8);
        System.arraycopy(frame_file_name,0,String_frame,13,frame_file_name.length);
        return String_frame;
    }

    public static byte[] int_frame(byte  frame_num){//长度为2byte
        //暂时应该是用作每次dispatch发送地数量，因为可能不满127，或者发其它的什么的，所以开头就要告接收方有多少
        byte[] frame = new byte[2];
        byte frame_type = 2;//这个除了上面的作用应该还可以用作一些命令
        frame[0] = frame_type;
        frame[1] = frame_num;
        return frame;//在diapatch中插入turn列表
    }

    public static byte[] back_frame(){  //接收到帧确认
        byte[] frame = {3};
        return frame;
    }

    public static byte[] cancel_frame() {//取消发送
        byte[] frame = {4};
        return frame;
    }

    public static byte[] prepared_frame(){ //接受区准备好接收
        byte[] frame = {5};
        return frame;
    }

    public static byte[] not_prepared_frame(){//接受区没有准备好接收
        byte[] frame = {6};
        return  frame;
    }

    public static byte[] turn_over_frame(){//表示一轮结束，由接收方发给发送方
        byte[] frame = {7};
        return  frame;
    }

    public static byte[] need_resend_frame(){
        byte[] frame = {8};
        return  frame;
    }

    static class current_thread_already_done{//这个没办法了，我用了Integer还是无法反馈回值，也没找到为什么，只能在这创建个类了
        int send;
        int receive;
        public current_thread_already_done(){
            send = 0;
            receive = 0;
        }
    }
}

