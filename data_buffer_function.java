package my;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import my.ui.receive_ui.change_iscancel;
import my.ui.receive_ui.test_validation;

/*理了好久，现在决定这个区就是帧缓冲区，发送turn数量帧，它负责逐一发送，（必须逐一我发现，不然帧的差错控制就没有意义了，因为会粘包）
* 接受时逐一帧读取，然后组合乘一个字节数组返回，交给下一出处理。数据并行发送接收
* 这就要求，这里要进行差错控制，发送了那边收到立马回复消息，再发下一帧。校验在另一个线程好了，最终整个turn发完了，将校验结果返回，哪个问题再补发
* */
public class data_buffer_function {  //(那这个发送区就主要负责多线程就好了，给它帧，它多线程发送。)前面这个想法打扰了
    OutputStream out;
    InputStream in;
//    static int sleep_time = 150;  //信道有往返延迟，发送一帧后需要对信道监听返回帧，按网速64M，传64k要8ms，所以就等10ms吧，但是这里是内网，具体情况就测试看看
//                                //经过测试，发送2byte后到收到回应，这中间最短是150ms。。。。为什么这么久
    //实际发现并不是上面的问题
    private static byte[] prepare_frame = other_frameFunction.prepared_frame();
    private static byte[] back_frame = other_frameFunction.back_frame();
    private static byte[] cancel_frame = other_frameFunction.cancel_frame();
    private static byte[] turn_over_frame  = other_frameFunction.turn_over_frame();
    private static byte[] need_resend_frame = other_frameFunction.need_resend_frame();
    ArrayList<byte[]> receive_buffer = new ArrayList<>();//专门给接收用的
    boolean iscancel = false;//取消发送标志，默认为flase
    byte current_serial_num;//暂停时最后一个发送的frame的序号，主要是用来给下次重新发送时找到重传的位置
    int current_Thread_frame_num = 0;//初始为0，当前线程发送的帧数
    boolean send_finish = false;//是否将这一轮turn发送完成
    boolean  send_success = false;//当前帧是否发送成功
    int total_frame_num,pre_frame_num;
    JProgressBar thread_process;
    change_iscancel cs;
    test_validation tv;
    public data_buffer_function(OutputStream out, InputStream in) throws IOException {
        this.out = out;
        this.in = in;
    }

    public void send_frame(byte[] frame) throws IOException{
        out.write(frame);
        out.flush();
    }

    public  void send(ArrayList<byte[]> frame_group) throws IOException, InterruptedException {//不关心发送了什么，只关心发送成功否
        current_serial_num = 0;//每波发送前讲已发送帧数置为零
        send_frame_data(frame_group);//turn里第一波逐帧发送

        int result = 0;
        while(true){
            try{
                result = in.read();
//                System.out.println("校验返回的结果"+result);
            }catch (IOException e){
                client.appendJTextArea("\n原本是超时重传IO异常");
            }
            break;
        }
        if(result != turn_over_frame[0]){//需要补发   用不等于是因为既然都校验出问题了，可能socket就有问题，发过来的就不一定等于要接受的那个
            try{
                int num = in.read();//需要补发时第一个byte为补发帧数量，接下来就读取这么多好了
                byte[] serial_num = new byte[num];
                client.appendJTextArea("\n需要补发的帧的数量:"+num);
                in.read(serial_num);//再读取需要补发的帧
                ArrayList<byte[]> resend_frame = new ArrayList<>();
                String temp = "\n";
                for(int m=0;m<serial_num.length;m++){
                    temp += serial_num[m]+" ";
                    resend_frame.add(frame_group.get(serial_num[m]));
                }
                client.appendJTextArea("\n补发队列加入的帧序号为："+temp);
                send_frame_data(resend_frame);
                client.appendJTextArea("\n发给接收方了");
            }catch (IOException e){
                e.printStackTrace();
            }
            try{
                int next = in.read();//再次监听
                if(next != turn_over_frame[0] ){//信道有问题
                    client.appendJTextArea("\n接收方得到的帧依然有问题，那就是socket有问题了");
                    current_serial_num = 0;//相较于接收方，只需要将这个标志置为零就行了，表示一帧都没发送出去
                }
                else if(result == turn_over_frame[0])
                    client.appendJTextArea("\n没有问题了！继续发送");
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if(result == turn_over_frame[0]){//这一伦turn结束了
//            System.out.println("turn 结束一轮");
            return;
        }
    }

    public void send_frame_data(ArrayList<byte[]> frame_group) throws InterruptedException, IOException { //和下面的原因一样，验证出问题时需要补发，和发送差不多流程，所以独立出来
        int frame_num = frame_group.size();//总的帧数，包括了首帧
        int send_point = 0;//当前要发送的帧
        int frame_type = 0;
        send_finish = false;//是否将这一轮turn发送完成
        do{
            send_success = false;//当前帧是否发送成功
            do{
                try{//肯定要阻塞，不然返回就失效了
                    send_frame(frame_group.get(send_point));//发送一帧
//                    Thread.sleep(15);//因为数据错乱不得已只能这样试试
//                        System.out.printf("send发送帧%d \n",send_point);
                    frame_type = in.read();//收到反馈，目前看来应该有两种，一个是收到确认，还有一个时暂停发送
//                    System.out.println("  回复为："+frame_type);
                }catch (IOException e){
                    Thread.sleep(4000);
                    client.appendJTextArea("\n没收到回复");
//                    break;//这是我最终改动的了，把这个超时是重传直接去去掉，因为在这没意义还会导致我的多线程出现问题(后来发现是发送时的多线程读取有问题，哈哈，这个有没有问题不知道，现在管他呢）
//                    continue;      //接受不到返回确认帧则继续发送这一帧。别忘了设置阻塞延迟
                }
                if((frame_type == back_frame[0]) | (frame_type == cancel_frame[0])) {//无论收到发送终止还是确认,都是发送一帧成功（其实我最终已经没有发送的暂停信号了，哈哈为了偷个懒时间不够了）
                    current_serial_num ++;//当前发送帧数加1
                    send_success = true;
                    if(send_point > 0){//只要point大于0就加1，因为0是头帧。
                        current_Thread_frame_num ++;//已发送帧加一，上面那个置零这个不置零
                    }
                    if(frame_type == cancel_frame[0]){
                        client.appendJTextArea("\n接受方发来暂停请求");
                        iscancel = true;//将取消发送置为true，这样下帧就不再发送了
                    }
                }
            }while(!send_success);
            if(iscancel == true){//断掉发送
                break;
            }
            send_point ++;   //往下一帧
            if(send_point == frame_num){//这一轮发送完成
                send_finish = true;
            }
        }while(!send_finish);
    }

    public boolean isreceive_prepared(){//此时
          return receive_buffer.isEmpty();
    }

    public void receive() throws IOException, InterruptedException {
        frame_hash_validation validation = new frame_hash_validation();//计算hash，应该是贯穿这个流的一生的//看起来不行，只能每次批量计算时创建
        if(isreceive_prepared()){//缓冲区为空，再进行新一轮的turn的接收
            byte frame_type;
            byte frame_num = 0;//因为下面抛出了为初始化的错误。。。。
            current_serial_num = 0;//每次准备接收时置为0
            byte[] first_frame = new byte[2];
            try{
                in.read(first_frame);//尝试读取首帧
//                System.out.println("读取首帧为："+" "+first_frame[0]+" "+first_frame[1]);
            }catch (IOException e){
                e.printStackTrace();
            }
            frame_num = first_frame[1];
            send_frame(back_frame);//确认帧，让发送发继续发送下一帧
            //                System.out.println("1返回："+back_frame[0]);
            receive_frame_data(false,frame_num,validation);//逐帧进行读取，因为下面补发时有差不多的操作，所以我将她拆开了

            ArrayList resend_frame= new ArrayList();    //判断有没有帧是出错的
            for(int i=0;i<current_serial_num;i++){//应该改成当前发送帧
                if(validation.all_result[i] == 0){//出错的帧，请求补发
                    resend_frame.add((byte)(i+1));//这里加1，因为校验时的帧往前推1了
                }
            }
            if(resend_frame.size() > 0){//表示又出错的帧，需要补发
                server.appendJTextArea("\n校验后发现了错误，错误帧为");
                String temp = "\n";
                for(int m=0;m<resend_frame.size();m++){
                    temp += resend_frame.get(m)+" ";
                }
                server.appendJTextArea(temp);
                byte[] serial_num = new byte[resend_frame.size()];
                send_frame(need_resend_frame);//告诉发送方要补发送
                for(int i=0;i<resend_frame.size();i++){
                    serial_num[i] = (byte) resend_frame.get(i);
                }
                byte isresend_success = resend(serial_num);//进行请求补发

                if(isresend_success == 0){//表示补发时任然失败，可以认为连接有问题，请求暂停
                    server.appendJTextArea("\n对补发的帧进行校验后依然发现有问题");
                    send_frame(need_resend_frame);
                    current_serial_num = 0;//出问题后直接置零,既然连接有问题就直接抛弃这一turn
                    receive_buffer.clear();//同样接收到的帧全清除
                }
                else
                    send_frame(turn_over_frame);
            }
            send_frame(turn_over_frame);//一轮发送完成
//            System.out.println("返回："+turn_over_frame[0]);
        }
    }

    public void set_receive_buffer(){  //让receive_frame_diapatch拿数据后进行清空，进行下一轮
        receive_buffer.clear();
    }

    public void receive_frame_data(boolean resend,int frame_num,frame_hash_validation validation) throws InterruptedException, IOException {//从原本的接收那里拆出来的，因为我发现下面补发好像也是这一套
        boolean receive_finish = false;
        int resend_num = 0;
        do{
            boolean receive_success = false;//
            byte[] frame = new byte[65535 + 20];//帧最长这么多
            byte[] pre_hash ;
            byte serial_num;
            int frame_contant_length;
            do{//这个do里只是确保正确的接收了一帧，返回信号在这个循环外
                try{
                    int read_length = in.read(frame);//尝试读取下一帧

                    int serial_num_temp = frame_function.get_frame_serial_num(frame); //这里读取可能会出错
                    frame_contant_length = frame_function.get_frame_contant_length(frame);
                    while(read_length < frame_contant_length + 20){//表示当前读取出错，socket中数据没有读取完。因为实际发现有可能会少读60字节，目前看起来是随机发生的
//                        System.out.println("没有读全只读到了"+read_length);
                        byte[] frame_temp = new byte[65535 + 20];//帧最长这么多
                        int read_length_temp = in.read(frame_temp);
//                        System.out.println("又读取到了"+read_length_temp);//没读完60字节，将剩下的60字节读取
                        System.arraycopy(frame_temp,0,frame,read_length,read_length_temp);//将之前没读到的填入
                        read_length += read_length_temp;
                    }
//                    System.out.printf("receive收到帧内序号%d\n",serial_num_temp);
                    if(resend == false){//应该区分一下，进行resend时就不要进行这些操作了
                        current_serial_num ++;//已经收到的帧数加1
                        current_Thread_frame_num ++;//这个刚好是切切实实收到的数据包，这样刚好不用每次都判断序号是不是大于零了(在外层统计的话有读取的头帧，所以要大于0）
                        thread_process.setValue(100*(current_Thread_frame_num+pre_frame_num)/total_frame_num);
                    }
                    else{
                        resend_num ++;
                    }
                }catch (IOException e){
                    server.appendJTextArea("\nreceive_buffer触发读写异常");
//                    continue;//若没有接收到下一帧，则再给send发确认，等
                }
                receive_success = true;
            }while(!receive_success);

            pre_hash = frame_function.get_frame_hash(frame);//原本的hash值
            serial_num = frame_function.get_frame_serial_num(frame);//下面补发时的顺序不一定，所以要取出序号
            byte[] frame_contant = frame_function.get_frame_contant(frame);//读取帧内容
            validation.start_caculate(serial_num,pre_hash,frame_contant);//加入线程池计算hash
            if(resend == false){
                try{//这个偷点懒。。
                    if(receive_buffer.size() < serial_num){
                        receive_buffer.add(frame_contant);//添加到列表；
                    }
                    else if (receive_buffer.size() >= serial_num){//这种只会出来在下面补发的情况
                        receive_buffer.set(serial_num-1,frame);//此时应该替换
                    }
                }catch (Exception e){
                    server.appendJTextArea("\n插入帧出现问题，可能是越界或者序号不对");
                }
                if (cs.iscancel == true){//检查一下（/*透，以前这个只有返回却认帧，放在了确保接收一帧正确里面，
                    // 现在加了暂停，最终发现得把hash计算添加，还有这帧加到列表才应该算接收帧结束。。不然暂停后那一帧就不进行hash和添加列表了*/）
                    send_frame(cancel_frame);//告诉发送方停止，，这个是在每次收到一个数据包后发的
                    server.appendJTextArea("\n接受方发送了暂停请求");
                    iscancel = true;
                    validation.exit_pool();//退出前还是得确保线程池执行完成的（用的话上面还得返回暂停到哪，不然127校验结果全是0，直接去掉，没时间改了）
                    break;
                }
                else{
                    send_frame(back_frame);//给发送方回复收货确认，让他发下一帧
                }
                if(receive_buffer.size() == frame_num){
                    receive_finish = true;
                }
            }
            else{
                send_frame(back_frame);//给发送方回复收货确认，让他发下一帧
                if(resend_num == frame_num){
                    receive_finish = true;
                }
            }

        }while(!receive_finish);
        validation.exit_pool();//确保线程池里的任务全部执行完成
        if(tv.istest == true){//测试校验，将这一轮接受到的全部改为校验错误，而不是修改接受到的结果        在校验结束后修改！！！！！！不然没用
            for(int mm = 0;mm<1;mm++){
                validation.all_result[mm] = 0;//改5帧
            }
            tv.istest = false;//将这个复原，不然下次校验就要出错啦
        }
    }

    public byte resend(byte[] serial_num) throws IOException, InterruptedException {
        byte[] serial_num_and_num = new byte[serial_num.length + 1];
        serial_num_and_num[0] = (byte)serial_num.length;//加个头通知对方后面有多少
        System.arraycopy(serial_num,0,serial_num_and_num,1,serial_num.length);
        frame_hash_validation validation = new frame_hash_validation();
        byte isresend_ok = 1;
        send_frame(serial_num_and_num);//通知发送方出问题的帧；

        receive_frame_data(true,serial_num.length,validation);//进行那一套组合拳

        for(int i=0;i<serial_num.length;i++){
            if(validation.all_result[serial_num[i]] == 0){//一般只处理其中部分帧，所以只校验那部分帧
                isresend_ok = 0;
                break;
            }
        }
//        System.out.println("补发的校验结果？？"+isresend_ok);
        isresend_ok = 1;
        return isresend_ok;
    }

    class frame_hash_validation{
        byte[] all_result = new byte[127];//最长127帧
        private ExecutorService pool = Executors.newCachedThreadPool();//线程池  线程池只负责分配线程，后续得调用isterminated才能知道有没有全部执行完

        public void start_caculate(byte serial_num,byte[] pre_hash,byte[] frame_contant){
            pool.execute(new frame_hash_validation_each(serial_num,pre_hash,frame_contant));//提交给线程池
        }
        public void exit_pool() throws InterruptedException {
            pool.shutdown();
            while(true){
                if(pool.isTerminated()){
//                    System.out.println("线程全部完成");
                    break;
                }
                  Thread.sleep(10);
            }
        }
        class frame_hash_validation_each extends Thread{
            byte[] frame_contant;
            byte[] pre_hash;
            byte serial_num;
            boolean result;
            public frame_hash_validation_each(byte serial_num,byte[] pre_hash,byte[] frame_contant){
                this.frame_contant = frame_contant;
                this.pre_hash = pre_hash;
                this.serial_num = serial_num;
            }
            @Override
            public void run() {
                byte[] current_hash = frame_function.MD5_hash(frame_contant);
                result = Arrays.equals(pre_hash,current_hash);
                all_result[serial_num - 1] = (byte) (result?1:0); //往前推一个
            }
        }
    }
}
