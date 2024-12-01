package com.testbot;

import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    int wordCount = 0;                         // счетчик слов
    int charCount = 0;                  // счетик букв
    //long mainChatId = -1001222805332L;   //id ручкососы
    long mainChatId = -4632666382L;
    String gameWord;
    String playerName;
    String[] finalWords = {};
    boolean isTimerActive = false;
    boolean isWordsCounted = false;
    boolean isTimerSuccess = false;
    boolean isGameWordSet = false;
    boolean isRoundSuccess = false;
    Timer timer = new Timer("Игра");
    List<String> finalWordList;
    ArrayList<String> playerNames = new ArrayList<>();


    public Bot(String botToken) {
        super(botToken);
    }

    public String getBotUsername() {
        return "HandleSuckerJr";
    }

    public void onUpdateReceived(@NotNull Update update) {   // Общение с юзером

        if (update.getMessage().getText().equals("/ready")
                && !playerNames.contains(update.getMessage().getFrom().getUserName())) {  // статус Ready
            playerName = update.getMessage().getFrom().getUserName();
            playerNames.add(playerName);    // массив готовых игроков
            try {
                sendMessage(mainChatId, playerName + " готов");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.getMessage().getText().startsWith("/word ")
                && playerNames.contains(update.getMessage().getFrom().getUserName())) {
            try {
                gameWord = update.getMessage().getText().substring(6);
                isGameWordSet = true;
                sendMessage(mainChatId, "Слово назначено: "
                        + gameWord);
                sendMessage(mainChatId, "Игра готова. Господа, к барьеру!");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (playerNames.contains(update.getMessage().getFrom().getUserName())
                && isGameWordSet) {

            String command = update.getMessage().getText();
            switch (command) {

                case "/start" -> {

                    try {
                        timing(); // запуск таймера
                        isTimerActive = true;
                        sendMessage(mainChatId, "Раунд начат,, время пошло!");
                        sendMessage(mainChatId, "*************      "
                                + gameWord + "      *************");

                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "/cancel" -> {
                    if (!isTimerActive) {
                        try {
                            gameRestart();
                            sendMessage(mainChatId, "Прерываю игру...отменяю раунд");
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (isTimerActive) {
                        try {
                            timer.cancel();
                            gameRestart();
                            sendMessage(mainChatId, "Отменяю раунд");
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }
        }

        if (update.getMessage().getText().startsWith("/")
                && playerNames.contains(update.getMessage().getFrom().getUserName())
                && isTimerSuccess) {

            if (playerNames.contains(update.getMessage().getFrom().getUserName())
                    && update.getMessage().getText().startsWith("/")
                    && isTimerSuccess) {
                if (isWordsCounted) {
                    try {
                        subtraction(update);        //удаление ошибочных слов
                        sendMessage(mainChatId, "***** UPDATE *****" + "\n" + playerName + " :"
                                + "\nСлов: " + finalWordList.size()
                                + "\nБукв: " + charCount);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException();
                    }
                }
                if (!isWordsCounted) {
                    try {
                        count(update);                     // первичный подсчёт слов и букв
                        isWordsCounted = true;
                        sendMessage(mainChatId, playerName +
                                " :" + "\nСлов: " + wordCount
                                + "\nБукв: " + charCount);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }
    }

    public void count(@NotNull Update update) {

        String[] lines = update.getMessage().getText().split("\n"); // массив слов
        lines[0] = lines[0].replace("/", ""); // удаление / косой
        int i;
        for (i = 0; i < lines.length; i++) {
            lines[i] = lines[i].strip();
        }
        Set<String> wordSet = new HashSet<>(Arrays.asList(lines)); // удаление дубликатов через Set
        String[] finalWords = wordSet.toArray(new String[wordSet.size()]); // возврат в новый массив

        for (String s : finalWords) {
            charCount += s.length(); // подсчёт букв
        }
        wordCount = finalWords.length;
    }

    public void subtraction(@NotNull Update update) {       // SUBSTRACTION

        String[] wordsToSubtract = update.getMessage().getText().split("\n"); // массив слов на удаление
        wordsToSubtract[0] = wordsToSubtract[0].replace("/", ""); // удаление / косой
        int charsToSubtract = 0;
        finalWordList = Arrays.asList((finalWords));

        for (int i = 0; i < wordsToSubtract.length; i++) {
            wordsToSubtract[i] = wordsToSubtract[i].strip();
        }

        for (int i = 0; i < finalWordList.size(); i++) {
            finalWordList.remove(wordsToSubtract[i]);
        }

        for (String s : wordsToSubtract) {
            charsToSubtract += s.length(); // подсчёт букв для удаления
        }
        wordCount -= wordsToSubtract.length;
        charCount -= charsToSubtract;
    }

    public void sendMessage(long id, String msgText) throws TelegramApiException { // метод отправки сообщения юзеру

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msgText);
        sendMessage.setChatId(id);
        execute(sendMessage);
    }

    public void gameRestart() {                 // Сервис перезапуска игры

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
                    isWordsCounted = false;
                    sendMessage(mainChatId, "Время истекло! Жду слова,."); // сообщение о конце времени\
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        int delay = 7000;          // таймер 15 сек
        timer.schedule(gameStop, delay);
    }
}

