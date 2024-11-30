package com.testbot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot("7729860277:AAE7tffepM-92pUePxc6BP-FJ5jpr_SQ9to");
        botsApi.registerBot(bot);
    }

}

//                for (int k = 0; k < 5; k++) {
//                    try {
//                        Thread.sleep(1000);
//                        sendMessage(mainChatId, "До начала " + k + "секунд...");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }