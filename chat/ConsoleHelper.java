package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString() {
        String readMessage = null;
        while (true) {
            try {
                readMessage = bufferedReader.readLine();
           //     bufferedReader.close();  //  при запуске бота выдает ошибку
                return readMessage;
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз." );
            }
        }
    }

    public static int readInt(){
        int number = 0;
        try{
            number = Integer.parseInt(readString());
        }catch (NumberFormatException e) {
            System.out.println("Не похоже на число!");
            number = Integer.parseInt(readString());
        }
        return number;
    }
}
