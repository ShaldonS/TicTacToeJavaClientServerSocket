package com.tictactoe;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToeServer {
    private ServerSocket serverSocket;
    private Socket playerX;
    private Socket playerO;
    private Socket currentPlayer;
    private char sign;
    private char[] board;
    private final int BOARD_SIZE = 9;
    private final int BOARD_SIDE_SIZE = (int) Math.sqrt(BOARD_SIZE);

    public TicTacToeServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running and waiting for players...");

            playerX = serverSocket.accept();
            System.out.println("Player X connected.");

            playerO = serverSocket.accept();
            System.out.println("Player O connected.");

            currentPlayer = playerX;
            sign = 'X';
            board = new char[BOARD_SIZE];
            for (int i = 0; i < BOARD_SIZE; i++) {
                board[i] = '-';
            }

            sendBoardToClients();
            playerX.getOutputStream().write("Your move".getBytes());

            playGame();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playGame() throws IOException, InterruptedException {
        boolean gameActive = true;
        while (gameActive) {
            int move = getPlayerMove();
            if(!setSignToBoard(move)) {
                continue;
            }
            sendBoardToClients();

            if (checkWin(sign)) {
                String message = "Player " + sign + " wins!";
                System.out.println(message);
                sendMessageToClients(message);
                gameActive = false;
            } else if (isBoardFull()) {
                String message = "It's a tie!";
                System.out.println(message);
                sendMessageToClients(message);
                gameActive = false;
            } else {
                if (sign == 'X') {
                    sign = 'O';
                    currentPlayer = playerO;
                }
                else {
                    sign = 'X';
                    currentPlayer = playerX;
                }
                sendMessageToClient(currentPlayer, "Your move");
            }
        }

        playerX.close();
        playerO.close();
    }

    private boolean setSignToBoard(int move) throws IOException, InterruptedException{
        Boolean res;
        if (move < 0 || move > BOARD_SIZE - 1) {
            sendMessageToClient(currentPlayer, "This cell doesn't exist. Min value is '00'. Max value is '" + (BOARD_SIDE_SIZE) + (BOARD_SIDE_SIZE) + "'");
            Thread.sleep(300);
            sendMessageToClient(currentPlayer, "Your move");
            res = false;
        }
        else if(board[move] == '-')
        {
            board[move] = sign;
            res = true;
        }
        else {
            sendMessageToClient(currentPlayer, "Cell already seted with " + board[move]);
            Thread.sleep(300);
            sendMessageToClient(currentPlayer, "Your move");
            res = false;
        }
        return res;
    }

    private void sendMessageToClient(Socket player, String message) throws IOException {
        player.getOutputStream().write(message.getBytes());
    }

    private void sendBoardToClients() throws IOException {
        playerX.getOutputStream().write(("Board:\n"+boardToString()).getBytes());
        playerO.getOutputStream().write(("Board:\n"+boardToString()).getBytes());
    }

    private void sendMessageToClients(String message) throws IOException {
        playerX.getOutputStream().write(message.getBytes());
        playerO.getOutputStream().write(message.getBytes());
    }

    private String boardToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ");
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            sb.append(i).append(" ");
        }
        sb.append('\n');
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            sb.append(i).append(" ");
            for (int j = 0; j < BOARD_SIDE_SIZE; j++) {
                sb.append(board[i * BOARD_SIDE_SIZE + j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private int getPlayerMove() throws IOException {
        byte[] response = new byte[2];
        currentPlayer.getInputStream().read(response);        

        int row = response[0] - '0';
        int col = response[1] - '0';

        return row * BOARD_SIDE_SIZE + col;
    }

    private boolean checkWin(char player) {
        // rows
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            boolean rowWin = true;
            for (int j = 0; j < BOARD_SIDE_SIZE; j++) {
                if (board[i * BOARD_SIDE_SIZE + j] != player) {
                    rowWin = false;
                    break;
                }
            }
            if (rowWin) return true;
        }
    
        // cols
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            boolean colWin = true;
            for (int j = 0; j < BOARD_SIDE_SIZE; j++) {
                if (board[j * BOARD_SIDE_SIZE + i] != player) {
                    colWin = false;
                    break;
                }
            }
            if (colWin) return true;
        }
    
        // left to right diag
        boolean leftDiagWin = true;
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            if (board[i * BOARD_SIDE_SIZE + i] != player) {
                leftDiagWin = false;
                break;
            }
        }
        if (leftDiagWin) return true;
    
        // right to left diag
        boolean rightDiagWin = true;
        for (int i = 0; i < BOARD_SIDE_SIZE; i++) {
            if (board[i * BOARD_SIDE_SIZE + (BOARD_SIDE_SIZE - 1 - i)] != player) {
                rightDiagWin = false;
                break;
            }
        }
        if (rightDiagWin) return true;
    
        return false;
    }
    

    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i] == '-') return false;
        }
        return true;
    }

    public static void main(String[] args) {
        int port = 8082;
        new TicTacToeServer(port);
    }
}
