package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>(); // Key is the Name of Client, Value - connection

    public static void main(String[] args) throws IOException {
        try {
            System.out.println("Введите номер порта сервера (например: 2205)");
            serverSocket = new ServerSocket(ConsoleHelper.readInt());
            System.out.println("Server is loaded" );
            while (true){
                clientSocket = serverSocket.accept();  //слушаем сокет
                Server.Handler handler = new Server.Handler(clientSocket);
                handler.start();
            }
        }catch (Exception e){
            serverSocket.close();
            System.out.println("Ошибка соединения");
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> pair: connectionMap.entrySet()
             ) {
            try {
                pair.getValue().send(message);
            }catch (IOException e){
                System.out.println("Сообщение не отправлено!");
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler (Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            Connection connection;
            String userName;

            System.out.println("Установлено соединение с " + socket.getRemoteSocketAddress());   //1
            try {
                connection = new Connection(socket);   //2
                userName = serverHandshake(connection);   //3
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));   //4
                notifyUsers(connection, userName);   //5
                serverMainLoop(connection, userName);   //6
                Iterator<Map.Entry<String, Connection>> iterator = connectionMap.entrySet().iterator();
                while (iterator.hasNext()){
                    if(iterator.next().getKey().equals(userName)){
                        iterator.remove();    //7
                        sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));    //7
                    }
                }
            }catch (IOException e){
                System.out.println("Ошибка соединения!");
            }catch (ClassNotFoundException e){
                System.out.println("Пользователь не существует!");
            }

        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            Message messageReceive;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                messageReceive = connection.receive();
                if(messageReceive.getType() == MessageType.USER_NAME && messageReceive.getData() != null
                        && !messageReceive.getData().equals("") && !messageReceive.getData().equals(null)
                        && !messageReceive.getData().isEmpty() && !connectionMap.containsKey(messageReceive.getData())) {
                    name = messageReceive.getData();
                    connectionMap.put(name, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    break;
                }
            }
            return name;
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (Map.Entry<String, Connection> pair: connectionMap.entrySet()
                 ) {
                if(!pair.getKey().equals(userName)){
                    connection.send(new Message(MessageType.USER_ADDED, pair.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true){
                Message messageFromClient =  connection.receive();
                if(messageFromClient.getType() == MessageType.TEXT){
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + messageFromClient.getData()));
                }else {
                    ConsoleHelper.writeMessage("Error! Ошибка. Неверный тип сообщения!");
                }
            }
        }

    }
}
