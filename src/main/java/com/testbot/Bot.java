package com.testbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import static java.util.concurrent.TimeUnit.*;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

//import javax.management.RuntimeErrorException;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {

    long mainChatId = -1001222805332L;

    public Bot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "iggiTestBot";
    }

    @Override

    public void onUpdateReceived(Update update) { // Общение с юзером

        if (update.getMessage().getText().equals("!старт")) { // старт игры
            try {

                sendMessage(mainChatId, "Раунд начат,, время пошло!");

                Timing(update); // запуск таймера

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.getMessage().getText().equals("!тазз")) { // условие если таз

            try {

                sendMessage(mainChatId, "@ego_re " + "тазищев как ты братик,,");
                RandomTimeMsg();

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            if (update.getMessage().getFrom().getUserName() == "@no1noU") {
                try {
                    sendMessage(mainChatId, "иди нах,, попастик, ");

                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);

                }
            }
        }
        if (update.getMessage().getText().equals("!Игорян")) { // Игорян

            try {

                if (update.getMessage().getFrom().getUserName().equals("ego_re")) { // если Игорян пишет егорчик
                    try {

                        sendMessage(mainChatId, "чё за паль??? UBZ DENIED");

                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else
                    sendMessage(mainChatId, "всё понял братик,, молчу");

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.getMessage().getText().startsWith("/")) {

            try {
                String[] lines = update.getMessage().getText().split("\n"); // массив слов
                lines[0] = lines[0].replace("/", ""); // удаление / косой
                int charCount = 0;
                int i;
                for (i = 0; i < lines.length; i++) {
                    lines[i] = lines[i].strip();

                }

                Set<String> wordSet = new HashSet<>(Arrays.asList(lines)); // удаление дубликатов через Set
                String[] finalWords = wordSet.toArray(new String[wordSet.size()]); // возврат в новый массив

                for (String s : finalWords) {
                    charCount += s.length(); // подсчёт букв

                }

                sendMessage(mainChatId, update.getMessage().getFrom().getUserName() + "\nСлов: " + (finalWords.length)
                        + "\nБукв: " + charCount);

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(long id, String msgText) throws TelegramApiException { // метод отправки сообщения юзеру

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msgText);
        sendMessage.setChatId(id);
        execute(sendMessage);
    }

    public void Timing(Update update) throws TelegramApiException { // ТАЙМЕР

        Timer timer = new Timer("Игра");
        TimerTask gameStop = new TimerTask() {
            public void run() { // задание для конца таймера
                try {
                    sendMessage(mainChatId, "Время истекло!"); // сообщение о конце времени
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        int delay = 300000; // таймер 5 минут
        timer.schedule(gameStop, delay);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void RandomTimeMsg() {

        Random rnd = new Random(); // рандомайзер
        String[] tauntDict = { "гоблин,,,как ты Тазманец,", "сТАЗис братик, как оно.", "ТАЗОМБЕК,",
                "гоооошенька,,сладкий мой Тазюк,, как ты дино," }; // словарь доёбок
        final Runnable taunter = new Runnable() {
            @Override
            public void run() {

                try {

                    int randomNum = rnd.nextInt(tauntDict.length); // диапазон рандомайзера
                    sendMessage(mainChatId, "@ego_re " + tauntDict[randomNum]);
                } catch (TelegramApiException e) { // рандомная доёбка раз в 10 секунд из словаря
                    throw new RuntimeException();
                }
            }
        };
        final ScheduledFuture<?> tauntHandle = scheduler.scheduleAtFixedRate(taunter, 10, 10, SECONDS);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                tauntHandle.cancel(true);
            }
        }, 60 * 60, SECONDS);
    }

}
