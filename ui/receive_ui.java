/*
 * Created by JFormDesigner on Tue May 19 18:55:57 CST 2020
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
public class receive_ui {
    server my_server;
    change_iscancel cs = new change_iscancel();
    test_validation tv = new test_validation();
    ArrayList<JProgressBar> processbar_list = new ArrayList<>();
   public receive_ui() throws IOException, InterruptedException {
        initComponents();
        my_receive_ui.setVisible(true);
        my_receive_ui.setLocation(700,200);
        my_server = new server(notify,cs,tv);
       processbar_list.add(thread_process);
       processbar_list.add(thread_process2);
       processbar_list.add(thread_process3);
       processbar_list.add(thread_process4);
       processbar_list.add(thread_process5);
       processbar_list.add(thread_process6);
       processbar_list.add(thread_process7);
       processbar_list.add(thread_process8);
   }

    private void agree_receive(ActionEvent e) throws InterruptedException {
        receive_dialog d = new receive_dialog(text_file_name,thread_num3,my_server,processbar_list);//弹窗
        file_name2.setText(my_server.pre_file_name);
        long file_length_modify = my_server.file_length/1024;
        String file_length_str = String.valueOf(file_length_modify)+"KB";
        if(file_length_modify > 10000){
            file_length_modify /= 1024;
            file_length_str =String.valueOf(file_length_modify)+"MB";
        }
        file_size2.setText(file_length_str);
    }

    private void my_receive_uiWindowClosing(WindowEvent e) throws IOException {
       for (Socket i : my_server.soc_list){//逐一关闭socket
           i.close();
       }
        server.first_server.close(); //窗口关闭时关闭连接
    }

    public static class change_iscancel extends Thread{
        public volatile boolean iscancel = false;
        @Override
        public void run() {
            iscancel = true;
        }
    }

    private void cancelActionPerformed(ActionEvent e) {
        notify.append("\n接收停止");
        cs.start();
    }

    private void continue_receiveActionPerformed(ActionEvent e) {
        my_server.issend = true;//继续传输，奥里给！
    }

    public static class test_validation extends Thread{
        public volatile boolean istest = false;
        @Override
        public void run() {
            istest = true;
        }
    }

    private void validation_testActionPerformed(ActionEvent e) {
        notify.append("\n测试文件校验功能，这里我把校验线程池中某轮前5帧改为校验错误\n");
        tv.start();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("my.ui.test");
        my_receive_ui = new JFrame();
        choose_file = new JLabel();
        scrollPane1 = new JScrollPane();
        text_file_name = new JTextArea();
        cancel = new JButton();
        validation_test = new JButton();
        status = new JPanel();
        file_size = new JLabel();
        file_size2 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        file_name2 = new JLabel();
        label5 = new JLabel();
        scrollPane2 = new JScrollPane();
        notify = new JTextArea();
        thread_num2 = new JLabel();
        thread_num3 = new JLabel();
        button_agree = new JButton();
        button_disagree = new JButton();
        label1 = new JLabel();
        thread_process = new JProgressBar();
        label3 = new JLabel();
        thread_process3 = new JProgressBar();
        thread_process2 = new JProgressBar();
        label2 = new JLabel();
        label4 = new JLabel();
        thread_process4 = new JProgressBar();
        continue_receive = new JButton();
        label8 = new JLabel();
        thread_process5 = new JProgressBar();
        label9 = new JLabel();
        thread_process6 = new JProgressBar();
        label10 = new JLabel();
        thread_process7 = new JProgressBar();
        label11 = new JLabel();
        thread_process8 = new JProgressBar();

        //======== my_receive_ui ========
        {
            my_receive_ui.setResizable(false);
            my_receive_ui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            my_receive_ui.setTitle(bundle.getString("my_receive_ui.title"));
            my_receive_ui.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        my_receive_uiWindowClosing(e);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            Container my_receive_uiContentPane = my_receive_ui.getContentPane();
            my_receive_uiContentPane.setLayout(null);

            //---- choose_file ----
            choose_file.setText(bundle.getString("choose_file.text_2"));
            my_receive_uiContentPane.add(choose_file);
            choose_file.setBounds(5, 15, 60, 35);

            //======== scrollPane1 ========
            {

                //---- text_file_name ----
                text_file_name.setLineWrap(true);
                text_file_name.setEditable(false);
                text_file_name.setWrapStyleWord(true);
                text_file_name.setText("\u4fdd\u5b58\u6587\u4ef6\u540d");
                scrollPane1.setViewportView(text_file_name);
            }
            my_receive_uiContentPane.add(scrollPane1);
            scrollPane1.setBounds(65, 0, 190, 65);

            //---- cancel ----
            cancel.setText(bundle.getString("cancel.text_2"));
            cancel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            cancel.addActionListener(e -> cancelActionPerformed(e));
            my_receive_uiContentPane.add(cancel);
            cancel.setBounds(255, 95, 90, 35);

            //---- validation_test ----
            validation_test.setText(bundle.getString("validation_test.text_2"));
            validation_test.addActionListener(e -> validation_testActionPerformed(e));
            my_receive_uiContentPane.add(validation_test);
            validation_test.setBounds(255, 160, 90, 35);

            //======== status ========
            {
                status.setLayout(null);

                //---- file_size ----
                file_size.setText(bundle.getString("file_size.text_2"));
                status.add(file_size);
                file_size.setBounds(0, 55, file_size.getPreferredSize().width, 20);
                status.add(file_size2);
                file_size2.setBounds(60, 55, 185, 20);

                //---- label6 ----
                label6.setText(bundle.getString("label6.text"));
                status.add(label6);
                label6.setBounds(new Rectangle(new Point(0, 10), label6.getPreferredSize()));
                status.add(label7);
                label7.setBounds(60, 0, 180, label7.getPreferredSize().height);
                status.add(file_name2);
                file_name2.setBounds(60, 10, 185, 20);

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
            my_receive_uiContentPane.add(status);
            status.setBounds(5, 65, 250, 80);

            //---- label5 ----
            label5.setText(bundle.getString("label5.text_2"));
            my_receive_uiContentPane.add(label5);
            label5.setBounds(95, 170, 80, 22);

            //======== scrollPane2 ========
            {

                //---- notify ----
                notify.setLineWrap(true);
                notify.setWrapStyleWord(true);
                notify.setTabSize(12);
                notify.setEditable(false);
                notify.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                scrollPane2.setViewportView(notify);
            }
            my_receive_uiContentPane.add(scrollPane2);
            scrollPane2.setBounds(345, 0, 160, 195);

            //---- thread_num2 ----
            thread_num2.setText(bundle.getString("thread_num2.text"));
            my_receive_uiContentPane.add(thread_num2);
            thread_num2.setBounds(255, 65, 50, 30);

            //---- thread_num3 ----
            thread_num3.setText(bundle.getString("thread_num3.text"));
            my_receive_uiContentPane.add(thread_num3);
            thread_num3.setBounds(305, 65, 40, 30);

            //---- button_agree ----
            button_agree.setText(bundle.getString("button_agree.text_2"));
            button_agree.addActionListener(e -> {
                try {
                    agree_receive(e);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            });
            my_receive_uiContentPane.add(button_agree);
            button_agree.setBounds(255, 0, 90, 35);

            //---- button_disagree ----
            button_disagree.setText(bundle.getString("button_disagree.text_2"));
            my_receive_uiContentPane.add(button_disagree);
            button_disagree.setBounds(255, 35, 90, 30);

            //---- label1 ----
            label1.setText(bundle.getString("label1.text_5"));
            my_receive_uiContentPane.add(label1);
            label1.setBounds(5, 220, 40, 20);
            my_receive_uiContentPane.add(thread_process);
            thread_process.setBounds(45, 220, 200, 20);

            //---- label3 ----
            label3.setText(bundle.getString("label3.text_2"));
            my_receive_uiContentPane.add(label3);
            label3.setBounds(5, 245, 40, 20);
            my_receive_uiContentPane.add(thread_process3);
            thread_process3.setBounds(45, 245, 200, 20);
            my_receive_uiContentPane.add(thread_process2);
            thread_process2.setBounds(305, 220, 200, 20);

            //---- label2 ----
            label2.setText(bundle.getString("label2.text_2"));
            my_receive_uiContentPane.add(label2);
            label2.setBounds(265, 220, 40, 20);

            //---- label4 ----
            label4.setText(bundle.getString("label4.text_2"));
            my_receive_uiContentPane.add(label4);
            label4.setBounds(265, 245, 40, 20);
            my_receive_uiContentPane.add(thread_process4);
            thread_process4.setBounds(305, 245, 200, 20);

            //---- continue_receive ----
            continue_receive.setText(bundle.getString("continue_receive.text"));
            continue_receive.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
            continue_receive.addActionListener(e -> continue_receiveActionPerformed(e));
            my_receive_uiContentPane.add(continue_receive);
            continue_receive.setBounds(255, 130, 90, 35);

            //---- label8 ----
            label8.setText(bundle.getString("label8.text"));
            my_receive_uiContentPane.add(label8);
            label8.setBounds(5, 270, 40, 20);
            my_receive_uiContentPane.add(thread_process5);
            thread_process5.setBounds(45, 270, 200, 20);

            //---- label9 ----
            label9.setText(bundle.getString("label9.text"));
            my_receive_uiContentPane.add(label9);
            label9.setBounds(265, 270, 40, 20);
            my_receive_uiContentPane.add(thread_process6);
            thread_process6.setBounds(305, 270, 200, 20);

            //---- label10 ----
            label10.setText(bundle.getString("label10.text"));
            my_receive_uiContentPane.add(label10);
            label10.setBounds(5, 295, 40, 20);
            my_receive_uiContentPane.add(thread_process7);
            thread_process7.setBounds(45, 295, 200, 20);

            //---- label11 ----
            label11.setText(bundle.getString("label11.text"));
            my_receive_uiContentPane.add(label11);
            label11.setBounds(265, 295, 40, 20);
            my_receive_uiContentPane.add(thread_process8);
            thread_process8.setBounds(305, 295, 200, 20);

            my_receive_uiContentPane.setPreferredSize(new Dimension(520, 360));
            my_receive_ui.setSize(520, 360);
            my_receive_ui.setLocationRelativeTo(null);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JFrame my_receive_ui;
    private JLabel choose_file;
    private JScrollPane scrollPane1;
    private JTextArea text_file_name;
    private JButton cancel;
    private JButton validation_test;
    private JPanel status;
    private JLabel file_size;
    private JLabel file_size2;
    private JLabel label6;
    private JLabel label7;
    private JLabel file_name2;
    private JLabel label5;
    private JScrollPane scrollPane2;
    private JTextArea notify;
    private JLabel thread_num2;
    private JLabel thread_num3;
    private JButton button_agree;
    private JButton button_disagree;
    private JLabel label1;
    private JProgressBar thread_process;
    private JLabel label3;
    private JProgressBar thread_process3;
    private JProgressBar thread_process2;
    private JLabel label2;
    private JLabel label4;
    private JProgressBar thread_process4;
    private JButton continue_receive;
    private JLabel label8;
    private JProgressBar thread_process5;
    private JLabel label9;
    private JProgressBar thread_process6;
    private JLabel label10;
    private JProgressBar thread_process7;
    private JLabel label11;
    private JProgressBar thread_process8;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    public static void main(String[] args) throws IOException, InterruptedException {
        new receive_ui();
    }
}
