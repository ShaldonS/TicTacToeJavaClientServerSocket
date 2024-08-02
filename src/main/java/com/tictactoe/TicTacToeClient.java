package com.tictactoe;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient {
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Scanner scanner;

    public TicTacToeClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            scanner = new Scanner(System.in);

            playGame();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playGame() throws IOException {
        byte[] response = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(response)) != -1) {
            String message = new String(response, 0, bytesRead);

            if (message.contains("Your move")) {
                System.out.print("Enter row and column like '01': ");
                String move = scanner.nextLine().replace(" ", "");
                output.write(move.getBytes());
            } else {
                System.out.println(message);
            }

            if (message.contains("wins!") || message.contains("It's a tie!")) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 8082;
        new TicTacToeClient(serverAddress, port);
    }
}
