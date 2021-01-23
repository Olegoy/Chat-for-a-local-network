package chat.client;;

import chat.Connection;
import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

 //   @Override
    public void run() {
            SocketThread socketThread = getSocketThread();
            socketThread.setDaemon(true);
            socketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
              //  e.printStackTrace();
                ConsoleHelper.writeMessage("Ошибка программы");
              //  System.exit(0);
                return;
            }
            if(clientConnected == true) System.out.println("Соединение установлено. Для выхода наберите команду 'exit'.");
            else System.out.println("Произошла ошибка во время работы клиента.");
            while (clientConnected == true) {
                String mess = ConsoleHelper.readString();
                if(mess.equals("exit")) {clientConnected = false;}
                if(shouldSendTextFromConsole() == true) {sendTextMessage(mess);}
            }

        }
    }


    protected String getServerAddress() {
        System.out.println("Введите ip адрес сервера");   //1  для локального соединения 127.0.0.1 или localhost
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {   //2
        System.out.println("Введите номер порта сервера");   // такой же как у сервера
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {   //3
        System.out.println("Введите свое Имя");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {   //4
        return true;
    }

    protected SocketThread getSocketThread() {   //5
        return new SocketThread();
    }

    protected  void sendTextMessage(String text) {   //6
        try{
            connection.send(new Message(MessageType.TEXT, text));
        }catch (IOException e) {
            ConsoleHelper.writeMessage("Произошла ошибка при отправке сообщения");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            synchronized (Client.this)
            {
                Client.this.clientConnected=clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true){
                Message mes1 = Client.this.connection.receive();
                if(mes1.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }else if(mes1.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message mesFromServer = Client.this.connection.receive();
                if (mesFromServer.getType() == MessageType.TEXT) {
                    processIncomingMessage(mesFromServer.getData());
                } else if (mesFromServer.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(mesFromServer.getData());
                } else if (mesFromServer.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(mesFromServer.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            super.run();
            Connection connection;
            String adressServer = getServerAddress();
            int port = getServerPort();
            try {
                connection = new Connection(new Socket(adressServer, port));
                Client.this.connection = connection;
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
