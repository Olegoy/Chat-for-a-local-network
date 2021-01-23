package chat.client;

import chat.ConsoleHelper;
import chat.Message;
import chat.MessageType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.spi.CalendarDataProvider;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole(){
        return false;
    }

    @Override
    protected String getUserName(){
        int x = (int) (Math.random() * 100);
        String name = "date_bot_" + x;
        return name;
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if (message==null) return;
            String[] data = message.split(": ");
            if (data.length!=2) return;
            String name = data[0];
            String text = data[1];
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = null;
            switch (text.trim()) {
                case "дата":
                    dateFormat = new SimpleDateFormat("d.MM.YYYY");
                    break;
                case "день":
                    dateFormat = new SimpleDateFormat("d");
                    break;
                case "месяц":
                    dateFormat = new SimpleDateFormat("MMMM");
                    break;
                case "год":
                    dateFormat = new SimpleDateFormat("YYYY");
                    break;
                case "время":
                    dateFormat = new SimpleDateFormat("H:mm:ss");
                    break;
                case "час":
                    dateFormat = new SimpleDateFormat("H");
                    break;
                case "минуты":
                    dateFormat = new SimpleDateFormat("m");
                    break;
                case "секунды":
                    dateFormat = new SimpleDateFormat("s");
                    break;
            }
            if (dateFormat!=null) sendTextMessage(String.format("Информация для %s: %s",
                    name, dateFormat.format(calendar.getTime())));
        }
    

    }
}
