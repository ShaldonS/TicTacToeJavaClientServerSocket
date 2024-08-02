package com.tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Client {
    static int size = 100;
    static MyJButton[] buttons = new MyJButton[size];
    static String[][] table = new String[10][10];

    static boolean first_player = true;
    static int steps_of_player = 0;
    static DataInputStream in;
    static DataOutputStream out;
    static Socket s;

    static boolean first_step = false;

    public static void createGUI(Container container) {
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(10, 10, 1, 1));

        for (int i = 0; i < size; i++) {
            MyJButton btn = new MyJButton("");
            btn.setNum(i);
            buttons[i] = btn;
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(btn.getText() == "") {
                        if(first_player) {
                            btn.setText("X");
                            steps_of_player++;
                            int num = btn.getNum();
                            int first = num%10;
                            int second = num/10%10;
                            table[first][second] = btn.getText();

                            String player_and_cell = "0" + Integer.toString(num);
                            sendMessage(player_and_cell);
                        }
                        else {
                            btn.setText("O");
                            steps_of_player++;
                            int num = btn.getNum();
                            int first = num%10;
                            int second = num/10%10;
                            table[first][second] = btn.getText();

                            String player_and_cell = "1" + Integer.toString(num);
                            sendMessage(player_and_cell);
                        }
                        if(!first_step) {
                            first_step = true;
                        }
                    }
                }
            });
            panel.add(btn);
        }
        container.add(panel);
        container.setPreferredSize((new Dimension(500, 500)));
    }

    static void sendMessage(String mes) {
        try {
            out.writeUTF(mes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static public boolean checkEndOfGame(int first) {
        int linesCount = 10;
        int countToWin = 6;
        String sign;
        if(first == 0)
            sign = "X";
        else {
            sign = "O";
        }
        for (int i = 0; i <= linesCount - countToWin; i++)
            for (int j = 0; j < linesCount; j++) {
                // строки
                if (checkLine(i, j, 1, 0, sign)) return true;
            }

        for (int i = 0; i < linesCount; i++)
            for (int j = 0; j <= linesCount - countToWin; j++) {
                // столбцы
                if (checkLine(i, j, 0, 1, sign)) return true;
            }

        for (int i = 0; i <= linesCount - countToWin; i++)
            for (int j = 0; j <= linesCount - countToWin; j++) {
                // y=x
                if (checkLine(i, j, 1, 1, sign)) return true;
            }

        for (int i = countToWin - 1; i < linesCount; i++)
            for (int j = 0; j <= linesCount - countToWin; j++) {
                // y=-x
                if (checkLine(i, j, -1, 1, sign)) return true;
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

    private static boolean checkLine(int start_x, int start_y, int dx, int dy, String sign) {
        int count = 0;
        for (int i = 0; i < 6; i++)
            if (table[start_x + i * dx][start_y + i * dy].equals(sign)) {
                count++;
                if (count == 6)
                    return true;
            }
        return false;
    }

    public static void main(String[] args)  {
        Random rand = new Random();
        int n = rand.nextInt(50);
        int first_turn = -1;

        int serverPort = 8070;
        String serverHost = "127.0.0.142";//args[1];

        for(int i = 0; i < 10; ++i) {
            for(int j = 0; j < 10; ++j) {
                table[i][j] = "";
            }
        }
        try {
            s = new Socket(serverHost, serverPort);
            System.out.println("Connected to "+serverHost);
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());

            first_turn = in.readInt();
            System.out.println("Received: " + first_turn);

            if(first_turn == 0) {
                first_player = true;
            }
            else {
                first_player = false;
            }
            final JFrame frame = new JFrame(((first_player) ? "First ":"Second ") + "player");;
            Thread t = new Thread(() -> {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        //JFrame frame = new JFrame(((first_player) ? "First ":"Second ") + "player");
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

            Runnable task = new Runnable() {
                public void run() {
                    while(true) {
                        boolean checkX = checkEndOfGame(0);
                        boolean checkO = checkEndOfGame(1);
                        if(checkX || checkO ) {
                            System.out.println("Game Completed!");
                            String winner = "X";
                            if(checkO) {
                                winner = "O";
                            }
                            JOptionPane.showMessageDialog(frame, "Winner is " + winner);
                            try {
                                s.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                            break;
                        }
                    }
                }
            };
            Thread thread = new Thread(task);
            thread.start();

            while(true) {
                String player_and_cell = null;
                try {
                    player_and_cell = in.readUTF();
                } catch (IOException e) {
                    //e.printStackTrace();
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
                char val = player_and_cell.charAt(0);
                String cell_str = player_and_cell.substring(1);
                int cell = Integer.parseInt(cell_str);

                buttons[cell].setText(Character.toString(val));
                int first = cell%10;
                int second = cell/10%10;
                table[first][second] = Character.toString(val);
            }
        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage()); // host cannot be resolved
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage()); // end of stream reached
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage()); // error in reading the stream
        }
    }
}








/*Runnable task = new Runnable() {
    public void run() {
        System.out.println("Hello, World!");
    }
};
Thread thread = new Thread(task);
thread.start();
*/