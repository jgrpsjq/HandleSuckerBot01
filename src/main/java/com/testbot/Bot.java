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
    String[] wordsToSubtract = {};
    boolean isTimerActive = false;
    boolean isApproved = false;
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

        if (update.getMessage().getText().equals("/ready") && !isTimerSuccess
                && !playerNames.contains(update.getMessage().getFrom().getUserName())) {  // статус Ready
            //playerName = update.getMessage().getFrom().getUserName();
            playerNames.add(update.getMessage().getFrom().getUserName());    // массив готовых игроков
            try {
                sendMessage(mainChatId, update.getMessage().getFrom().getUserName() + " готов");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.getMessage().getText().startsWith("/word ")
                && playerNames.contains(update.getMessage().getFrom().getUserName())) {
            try {
                System.out.println(playerNames.size());
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

        if (update.getMessage().getText().startsWith("/")               // вызов подсчёта
                && playerNames.contains(update.getMessage().getFrom().getUserName())
                && isTimerSuccess) {
            count(update);                     // подсчёт слов и букв
            isWordsCounted = true;
        }

        if (update.getMessage().getText().equals("/approve")) {
            try {
                gameRestart();
                sendMessage(mainChatId, "Раунд завершён!");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void count(@NotNull Update update) {

        if (!isWordsCounted) {

            String[] lines = update.getMessage().getText().split("\n"); // массив слов
            lines[0] = lines[0].replace("/", ""); // удаление /
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].strip();
            }
            Set<String> wordSet = new HashSet<>(Arrays.asList(lines)); // удаление дубликатов через Set
            String[] finalWords = wordSet.toArray(new String[wordSet.size()]);

            for (String s : finalWords) {
                charCount += s.length(); // подсчёт букв
            }
            wordCount = finalWords.length;
            try {
                sendMessage(mainChatId, update.getMessage().getFrom().getUserName() +
                        " :" + "\nСлов: " + wordCount
                        + "\nБукв: " + charCount);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (isWordsCounted) {

            String[] wordsToSubtract = update.getMessage().getText().split("\n"); // массив слов на удаление
            wordsToSubtract[0] = wordsToSubtract[0].replace("/", ""); // удаление / косой
            int charsToSubtract = 0;
            finalWordList = Arrays.asList((finalWords));

            for (int i = 0; i < wordsToSubtract.length; i++) {     // удаление пробелов
                wordsToSubtract[i] = wordsToSubtract[i].strip();
            }

            for (String s : wordsToSubtract) {
                charsToSubtract += s.length(); // подсчёт букв
            }

            for (int i = 0; i < finalWordList.size(); i++) {      // удаление заявленных слов
                for (String s : wordsToSubtract) {
                    if (finalWordList.get(i).equals(s)) {
                        finalWordList.remove(i);
                    }
                }
            }
            try {
                sendMessage(mainChatId, "***** UPDATE *****" + "\n" + update.getMessage().getFrom().getUserName() + " :"
                        + "\nСлов: " + (wordCount - wordsToSubtract.length)
                        + "\nБукв: " + (charCount - charsToSubtract));
            } catch (TelegramApiException e) {
                throw new RuntimeException();
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

        isTimerActive = false;
        isGameWordSet = false;
        isRoundSuccess = false;
        isTimerSuccess = false;
        isApproved = false;
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

//        for (int i = 0; i < finalWords.length; i++) {
//            for (int j = 0; j < wordsToSubtract.length; j++) {
//                if (finalWords[i].equals(wordsToSubtract[j])) {
//                    finalWords[i] = "";;
//                }
//            }
//        }
//        int finalI = i;
//        finalWordList.removeIf(element -> element.equals(wordsToSubtract[finalI]));