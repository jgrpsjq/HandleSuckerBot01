package com.testbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.util.Date;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    //int readyPlayersCount = 0;                 // кол-во готовых игроков
    int todayRoundsCount = 0;                  // кол-во раундов за день
    int wordCount = 0;                         // счетчик слов
    int todayWordsCount = 0;                    //
    //int todaySeriesCount = 0;                    // кол-во серий за сегодня
    int charCount = 0;                  // счетик букв
    int getReadyPlayerId;
    int readyPlayersCount = 0;
    //long mainChatId = -1001222805332L;   //id ручкососы
    long mainChatId = -4632666382L;
    String gameWord;
    String playerName;
    long playerId;
    boolean isTimerActive = false;
    boolean isTimerSuccess = false;
    boolean isGameWordSet = false;
    boolean isRoundSuccess = false;
    Timer timer = new Timer("Игра");
    ArrayList<String> playerNames = new ArrayList<>();
    Date date = new Date();
    //HashMap<String, Integer> playerRoundStats = new HashMap<>();

    public Bot(String botToken) {
        super(botToken);
    }

    public String getBotUsername() {
        return "HandleSuckerJr";
    }

    public void onUpdateReceived(Update update) {   // Общение с юзером

        if (update.getMessage().getText().equals("/ready")
                && !playerNames.contains(update.getMessage().getFrom().getUserName())) {  // статус Ready
            playerName = update.getMessage().getFrom().getUserName();
            playerNames.add(playerName);    // массив имен готовых игроков
            try {
                sendMessage(mainChatId, playerName + " готов");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.getMessage().getText().startsWith("/word")
                && playerNames.contains(update.getMessage().getFrom().getUserName())) {         // слово для игры

            try {
                gameWord = update.getMessage().getText();
                isGameWordSet = true;
                sendMessage(mainChatId, "Слово назначено: " + gameWord.substring(6));
                sendMessage(mainChatId, "Игра готова. Господа, к барьеру!");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.getMessage().getText().equals("/start")
                && playerNames.contains(update.getMessage().getFrom().getUserName())
                && isGameWordSet) {         // СТАРТ ИГРЫ *********

            try {
                timing(); // запуск таймера
                isTimerActive = true;
                sendMessage(mainChatId, "Раунд начат,, время пошло!");
                sendMessage(mainChatId, "*************      "
                        + gameWord.substring(6) + "      *************");

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.hasMessage() && update.getMessage().getText().equals("/cancel")
                && playerNames.contains(update.getMessage().getFrom().getUserName())
                && isTimerActive) {

            try {
                sendMessage(mainChatId, "Прерываю игру...отменяю раунд");
                gameRestart();
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if (update.getMessage().getText().startsWith("/")           //подсчет слов и букв
                && playerNames.contains(update.getMessage().getFrom().getUserName())
                && isTimerSuccess) {

            try {
                String[] lines = update.getMessage().getText().split("\n"); // массив слов
                lines[0] = lines[0].replace("/", ""); // удаление / косой
                int i;
                for (i = 0; i < lines.length; i++) {
                    lines[i] = lines[i].strip();
                }
                Set<String> wordSet = new HashSet<>(Arrays.asList(lines)); // удаление дубликатов через Set
                String[] finalWords = wordSet.toArray(new String[0]); // возврат в новый массив

                for (String s : finalWords) {
                    charCount += s.length(); // подсчёт букв
                }
                wordCount = finalWords.length;
                todayWordsCount += wordCount;
                sendMessage(mainChatId, playerName + " :" + "\nСлов: " + wordCount
                        + "\nБукв: " + charCount);

            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            if (update.getMessage().getText().equals("/approve")
                    && playerNames.contains(update.getMessage().getFrom().getUserName())) {
                try {
                    sendMessage(mainChatId, "Раунд завершён!");

                    isRoundSuccess = true;               // раунд успешен
                    todayRoundsCount++;                 // + 1 к сегодняшним раундам
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void sendMessage(long id, String msgText) throws TelegramApiException { // метод отправки сообщения юзеру

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msgText);
        sendMessage.setChatId(id);
        execute(sendMessage);
    }

    public void gameRestart() {                 // Сервис перезапуска игры

        timer.cancel();
        isTimerActive = false;
        isGameWordSet = false;
        isRoundSuccess = false;
        isTimerSuccess = false;
        playerNames.clear();

    }

    public void timing() throws TelegramApiException { // ТАЙМЕР и первичная обработка раунда

        TimerTask gameStop = new TimerTask() {

            public void run() { // задание для конца таймера

                try {
                    isTimerSuccess = true;
                    isTimerActive = false;                  // таймер неактивен
                    sendMessage(mainChatId, "Время истекло! Ожидаю слов..."); // сообщение о конце времени\
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        int delay = 7000;          // таймер 15 сек
        timer.schedule(gameStop, delay);
    }

}





