/*
 * Created by JFormDesigner on Sun May 17 15:21:38 CST 2020
 */

package my.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import javax.swing.*;
import my.*;

/**
 * @author Brainrain
 */
public class send_ui {
    client my_client;
    String file;
    int back_thread_num;
    ArrayList<JProgressBar> processbar_list = new ArrayList<>();
    public send_ui() throws IOException, InterruptedException {
        initComponents();
        my_send_ui.setVisible(true);
        my_send_ui.setLocation(100,200);
        my_client = new client(notify);
    }

    private void searchActionPerformed(ActionEvent e) throws IOException {
        JFileChooser fc = new JFileChooser("E:/小鹿叮叮纸尿裤/学习相关/网络工程师综合训练/file/send");
        int val = fc.showOpenDialog(null);
        if (val == fc.APPROVE_OPTION){
            text_file_name.setText(fc.getSelectedFile().toString());
        }
        else{
            text_file_name.setText("未选择文件");
        }
        file = text_file_name.getText();
        if(file.equals("请选择文件")|file == null){
            new warning("你还没有选择文件呢！");
        }else{
            my_client.send_file_info(file);
            long file_length_modify = my_client.file_length/1024;
            String file_length_str = String.valueOf(file_length_modify)+"KB";
            if(file_length_modify > 10000){
                file_length_modify /= 1024;
                file_length_str =String.valueOf(file_length_modify)+"MB";
            }
            notify.append("\n文件名为："+my_client.file_name+"\n文件大小为："+file_length_str);
            file_size2.setText(file_length_str);
            file_name2.setText(my_client.file_name);
        }
    }

    class request_thread extends Thread{
        @Override
        public void run() {
            try {
                my_client.file_info_send();
            } catch (IOException e) {
                e.printStackTrace();
            }
            back_thread_num = my_client.get_thread_num();
            if(back_thread_num == 0){
                notify.append("\n对方拒绝你的文件哦！");
            }
            else{
                thread_num2.setText(String.valueOf(back_thread_num));
                notify.append("\n对方同意发送啦！");
                try {
                    my_client.create_soc(back_thread_num);//创建线程连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void request_sendActionPerformed(ActionEvent e) throws IOException {
        request_thread t  = new request_thread();
        t.start();
    }

    private void my_send_uiWindowClosing(WindowEvent e) throws IOException {
        for(Socket i : my_client.soc_list){//将socket都关闭
            i.close();
        }
    }

    class sendActionPerformed_thread extends Thread{
        @Override
        public void run() {
            try {
                my_client.start_send(my_client.soc_list,file,back_thread_num);//开启发送
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendActionPerformed(ActionEvent e) throws IOException {
        sendActionPerformed_thread t = new sendActionPerformed_thread();
        t.start();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("my.ui.test");
        my_send_ui = new JFrame();
        choose_file = new JLabel();
        search = new JButton();
        scrollPane1 = new JScrollPane();
        text_file_name = new JTextArea();
        request_send = new JButton();
        send = new JButton();
        thread_num = new JLabel();
        status = new JPanel();
        file_size = new JLabel();
        already_send = new JLabel();
        file_name2 = new JLabel();
        file_size2 = new JLabel();
        thread_num2 = new JLabel();
        scrollPane2 = new JScrollPane();
        notify = new JTextArea();

        //======== my_send_ui ========
        {
            my_send_ui.setTitle(bundle.getString("my_send_ui.title"));
            my_send_ui.setVisible(true);
            my_send_ui.setName("send_ui2");
            my_send_ui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            my_send_ui.setResizable(false);
            my_send_ui.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        my_send_uiWindowClosing(e);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            Container my_send_uiContentPane = my_send_ui.getContentPane();
            my_send_uiContentPane.setLayout(null);

            //---- choose_file ----
            choose_file.setText(bundle.getString("choose_file.text"));
            my_send_uiContentPane.add(choose_file);
            choose_file.setBounds(5, 5, 60, 40);

            //---- search ----
            search.setText(bundle.getString("search.text"));
            search.addActionListener(e -> {
                try {
                    searchActionPerformed(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            my_send_uiContentPane.add(search);
            search.setBounds(200, 50, 90, 35);

            //======== scrollPane1 ========
            {

                //---- text_file_name ----
                text_file_name.setLineWrap(true);
                text_file_name.setEditable(false);
                text_file_name.setWrapStyleWord(true);
                text_file_name.setText("\u8bf7\u9009\u62e9\u6587\u4ef6");
                scrollPane1.setViewportView(text_file_name);
            }
            my_send_uiContentPane.add(scrollPane1);
            scrollPane1.setBounds(65, 5, 310, 40);

            //---- request_send ----
            request_send.setText(bundle.getString("request_send.text"));
            request_send.addActionListener(e -> {
                try {
                    request_sendActionPerformed(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            my_send_uiContentPane.add(request_send);
            request_send.setBounds(200, 85, 90, 35);

            //---- send ----
            send.setText(bundle.getString("send.text"));
            send.addActionListener(e -> {
                try {
                    sendActionPerformed(e);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            my_send_uiContentPane.add(send);
            send.setBounds(290, 85, 90, 35);

            //---- thread_num ----
            thread_num.setText(bundle.getString("thread_num.text"));
            my_send_uiContentPane.add(thread_num);
            thread_num.setBounds(290, 50, 50, 35);

            //======== status ========
            {
                status.setLayout(null);

                //---- file_size ----
                file_size.setText(bundle.getString("file_size.text"));
                status.add(file_size);
                file_size.setBounds(0, 5, file_size.getPreferredSize().width, 20);

                //---- already_send ----
                already_send.setText(bundle.getString("already_send.text"));
                status.add(already_send);
                already_send.setBounds(0, 45, already_send.getPreferredSize().width, 20);
                status.add(file_name2);
                file_name2.setBounds(60, 5, 135, 20);
                status.add(file_size2);
                file_size2.setBounds(60, 45, 135, 20);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < status.getComponentCount(); i++) {
                        Rectangle bounds = status.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = status.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    status.setMinimumSize(preferredSize);
                    status.setPreferredSize(preferredSize);
                }
            }
            my_send_uiContentPane.add(status);
            status.setBounds(5, 50, 195, 70);

            //---- thread_num2 ----
            thread_num2.setText(bundle.getString("thread_num2.text_2"));
            my_send_uiContentPane.add(thread_num2);
            thread_num2.setBounds(340, 50, 40, 35);

            //======== scrollPane2 ========
            {

                //---- notify ----
                notify.setWrapStyleWord(true);
                notify.setLineWrap(true);
                notify.setEditable(false);
                scrollPane2.setViewportView(notify);
            }
            my_send_uiContentPane.add(scrollPane2);
            scrollPane2.setBounds(380, 5, 150, 115);

            my_send_uiContentPane.setPreferredSize(new Dimension(545, 170));
            my_send_ui.setSize(545, 170);
            my_send_ui.setLocationRelativeTo(my_send_ui.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    public JFrame my_send_ui;
    private JLabel choose_file;
    private JButton search;
    private JScrollPane scrollPane1;
    private JTextArea text_file_name;
    private JButton request_send;
    private JButton send;
    private JLabel thread_num;
    private JPanel status;
    private JLabel file_size;
    private JLabel already_send;
    private JLabel file_name2;
    private JLabel file_size2;
    private JLabel thread_num2;
    private JScrollPane scrollPane2;
    private JTextArea notify;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    public static void main(String[] args) throws InterruptedException, IOException {
        new send_ui();
    }
}
