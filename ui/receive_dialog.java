/*
 * Created by JFormDesigner on Thu May 21 11:56:10 CST 2020
 */

package my.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import my.ui.warning;
import my.*;

/**
 * @author Brainrain
 */
public class receive_dialog extends JDialog {
    static  String file,file_now;
    static String back_thread_num;
    JTextArea text_file_name;
    JLabel thread_num3;
    server my_server;
    ArrayList<JProgressBar> processbar_list = new ArrayList<>();
    public receive_dialog(JTextArea text_file_name,JLabel thread_num3,server my_server,ArrayList processbar_list) {
        initComponents();
        this.text_file_name = text_file_name;
        this.thread_num3 = thread_num3;
        this.my_server = my_server;
        this.processbar_list = processbar_list;
    }

    private void searchActionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser("E:/小鹿叮叮纸尿裤/学习相关/网络工程师综合训练/file/receive");
        int val = fc.showSaveDialog(null);
        if (val == fc.APPROVE_OPTION){
            choose_file_location.setText(fc.getSelectedFile().toString());
        }
        else{
            choose_file_location.setText("没有文件");
        }
    }
    class sure_thread extends Thread{
        @Override
        public void run() {
            file = choose_file_location.getText();
//        back_thread_num((int)d.choose_thread_num.getSelectedItem());//返回线程数
            back_thread_num = (String) choose_thread_num.getSelectedItem();
            if((file == null)|(file.equals("保存位置"))|file.equals("没有文件")){
                setEnabled(false);
//            System.out.println("弹弹弹");
                new warning("没有选择文件！");
            }
            String pre_file_name = my_server.pre_file_name;
            String[] cut = pre_file_name.split("\\.");//原本可能有后缀也可能没有，没有的话就加个空还是不变
            if(file.split("\\.").length < 2){//没有后缀的话添一个，原本就没有后缀的话添加为空，没影响，即使自己加了也没影响
                file_now =file+ "."+cut[cut.length-1];
            }
            text_file_name.setText(file_now);
            thread_num3.setText(back_thread_num);
            try {
                my_server.back_thread_num(Integer.valueOf(back_thread_num));//给发送端回送消息
            } catch (IOException e) {
                e.printStackTrace();
            }
            dispose();//关闭窗口
            if(Integer.valueOf(back_thread_num)>0){
                try {
                    my_server.create_server_socket(Integer.valueOf(back_thread_num));//大于0则表示接收
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    for (int i=0;i<7-Integer.valueOf(back_thread_num);i++){//设置进度条
                        processbar_list.remove(7-i);
                    }
                    my_server.receive_file(my_server.soc_list,file_now,Integer.valueOf(back_thread_num),my_server.file_length,processbar_list);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void sure(ActionEvent e) throws IOException, InterruptedException {
        sure_thread t = new sure_thread();
        t.start();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("my.ui.test");
        label1 = new JLabel();
        sure = new JButton();
        scrollPane3 = new JScrollPane();
        choose_file_location = new JTextArea();
        thread_num = new JLabel();
        choose_thread_num = new JComboBox<>();
        search = new JButton();

        //======== this ========
        setTitle(bundle.getString("this.title"));
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label1 ----
        label1.setText(bundle.getString("label1.text_2"));
        contentPane.add(label1);
        label1.setBounds(5, 95, 55, 0);

        //---- sure ----
        sure.setText(bundle.getString("sure.text"));
        sure.addActionListener(e -> {
            try {
                sure(e);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        contentPane.add(sure);
        sure.setBounds(95, 70, 120, 25);

        //======== scrollPane3 ========
        {

            //---- choose_file_location ----
            choose_file_location.setText(bundle.getString("choose_file_location.text"));
            choose_file_location.setEditable(false);
            choose_file_location.setLineWrap(true);
            choose_file_location.setWrapStyleWord(true);
            scrollPane3.setViewportView(choose_file_location);
        }
        contentPane.add(scrollPane3);
        scrollPane3.setBounds(85, 5, 200, 60);

        //---- thread_num ----
        thread_num.setText(bundle.getString("thread_num.text_2"));
        contentPane.add(thread_num);
        thread_num.setBounds(5, 5, 40, 30);

        //---- choose_thread_num ----
        choose_thread_num.setMaximumRowCount(4);
        choose_thread_num.setModel(new DefaultComboBoxModel<>(new String[] {
            "1",
            "2",
            "4",
            "8"
        }));
        choose_thread_num.setSelectedIndex(0);
        contentPane.add(choose_thread_num);
        choose_thread_num.setBounds(45, 5, 40, 30);

        //---- search ----
        search.setText(bundle.getString("search.text_2"));
        search.addActionListener(e -> searchActionPerformed(e));
        contentPane.add(search);
        search.setBounds(5, 40, 80, 25);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JButton sure;
    private JScrollPane scrollPane3;
    public JTextArea choose_file_location;
    private JLabel thread_num;
    public JComboBox<String> choose_thread_num;
    private JButton search;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
//    public static void main(String[] args) {
//        new receive_dialog();
//    }
}
