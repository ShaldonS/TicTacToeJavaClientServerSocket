package com.tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {

    static int cnt_threads = 0;
    static int size = 100;
    static String[] table = new String[size];
    static MyJButton[] buttons = new MyJButton[size];

    static boolean first_player = true;
    static int steps_of_player = 0;
    static boolean first_step = true;
    static boolean start = false;
    static boolean first_connected = false;
    static boolean second_connected = false;

    static boolean game_ended = false;
    static final JFrame frame = new JFrame("Server");

    static ClientConnection[] client = new ClientConnection[2];

    public static void createGUI(Container container) {
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel panel = new JPanel();
        //panel.setLayout(null);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(10, 10, 1, 1));
        //panel.setLayout(new GridLayout());

        //MyJButton btn;
        for (int i = 0; i < size; i++) {
            table[i] = "";
            MyJButton btn = new MyJButton("");
            btn.setNum(i);
            buttons[i] = btn;
            /*btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(btn.getText() == "") {
                        if(first_player) {
                            btn.setText("X");
                            steps_of_player++;
                            if(first_step) {
                                first_step = false;
                                steps_of_player = 2;
                            }
                            if(steps_of_player > 1) {
                                first_player = false;
                                steps_of_player = 0;
                            }
                            int num = btn.getNum();
                            table[num] = btn.getText();
                            if(checkEndOfGame(num))
                                panel.add(new JTextArea("WINNER"));
                        }
                        else {
                            btn.setText("0");
                            steps_of_player++;
                            if(steps_of_player > 1) {
                                first_player = true;
                                steps_of_player = 0;
                            }
                            int num = btn.getNum();
                            table[num] = btn.getText();
                            if(checkEndOfGame(num))
                                panel.add(new JTextArea("WINNER"));
                        }
                    }
                }
            });*/
            panel.add(btn);
        }
        container.add(panel);
        container.setPreferredSize((new Dimension(500, 500)));
    }

    static public boolean checkEndOfGame(int last_upd_cell) {
        for(int i = 0; i < size; ++i) {
            if(table[i] != "" && table[i] == table[i+1]) {
                return true;
            }
        }
        return false;
    }

    public static class MyJButton extends JButton {
        int num = 0;

        public void setNum(int num_to_set) {
            num = num_to_set;
        }

        public int getNum() {
            return num;
        }

        public MyJButton(String text) {
            super(text);
        }
    }

    public static void main(String[] args) throws IOException {
        //final JFrame frame = new JFrame("Server");
        Thread t = new Thread(() -> {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    createGUI(frame.getContentPane());
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            });
        }
        );
        t.start();

        int serverPort = 8070; // the server port
        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            int thread_num = 0;
            while (true) {
                Socket clientSocket = listenSocket.accept(); // listen for new connection
                client[thread_num] = new ClientConnection(clientSocket); // launch new thread
                thread_num++;
                System.out.println("Client connected");
            }
        }
    }

    static class ClientConnection extends Thread {
        DataInputStream in;
        DataOutputStream out;
        Socket clientSocket;

        public ClientConnection(Socket aClientSocket) {
            try {
                clientSocket = aClientSocket;
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                this.start();
            } catch (IOException e) {
                System.out.println("tcp.ClientConnection:" + e.getMessage());
            }
        }

        public void run() { // an echo server
            try {
                if(!first_connected) {
                    out.writeInt(0);
                    first_connected = true;
                }
                else if(!second_connected){
                    sleep(1000);
                    out.writeInt(1);
                    second_connected = true;
                }

                while(!game_ended) {
                    String player_and_cell = in.readUTF();
                    char player = player_and_cell.charAt(0);
                    String cell_str = player_and_cell.substring(1);
                    int cell = Integer.parseInt(cell_str);
                    String val = "";

                    if (player == '0') {
                        val = "X";
                    } else {
                        val = "O";
                    }
                    buttons[cell].setText(val);

                    String send = val + cell_str;
                    if(val == "X")
                        client[1].out.writeUTF(send);
                    else client[0].out.writeUTF(send);
                }

            } catch (EOFException e) {
                System.out.println("EOF:" + e.getMessage());
                try {
                    //clientSocket.shutdownOutput();
                    client[0].clientSocket.close();
                    client[1].clientSocket.close();
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Socket was closed");
                }
                System.out.println("Socket was not closed");
            } catch (IOException e) {
                System.out.println("readline:" + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }










    static class ServerThread extends Thread {
        Socket s = null;

        ObjectOutputStream to_client = null;
        ObjectInputStream from_client = null;
        Thread thread;
        boolean started = false;
        int thread_num = 0;

        int num = 0;
        int cnt = 0;

        public ServerThread() {
        }

        public ServerThread(Socket s, int thread_num) {
            this.s = s;
            this.thread_num = thread_num;
        }

        public void run() {
            try (
                    ObjectOutputStream to_client = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream from_client = new ObjectInputStream(s.getInputStream());
            ) {
                /*while(!start) {
                    if(!first_connected) {
                        to_client.writeObject(0);
                        first_connected = true;
                    }
                    if(!second_connected) {
                        String str = "";

                        to_client.writeObject(1);

                        //to_client.writeObject(str);

                        second_connected = true;
                        start = true;
                    }
                }*/
                while (true) {
                    num = (int) from_client.readObject();

                    if(buttons[num].getText() == "") {
                        System.out.println("ACCEPTED: " + num);
                        if(first_player) {
                            buttons[num].setText("X");
                            cnt++;
                            if(cnt > 1) {
                                first_player = false;
                                cnt = 0;
                            }
                            if(first_step) {
                                first_player = false;
                                first_step = false;
                                cnt = 0;
                            }
                            to_client.writeObject(num);
                            String val = "X";
                            to_client.writeObject(val);
                            System.out.println("Posted: " + num);

                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            to_client.writeObject(num);
                            to_client.writeObject(val);
                            System.out.println("Posted: " + num);
                        }
                        else {
                            buttons[num].setText("O");
                            cnt++;
                            if(cnt > 1) {
                                first_player = true;
                                cnt = 0;
                            }
                            to_client.writeObject(num);
                            String val = "O";
                            to_client.writeObject(val);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
