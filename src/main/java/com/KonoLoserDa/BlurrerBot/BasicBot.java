package com.KonoLoserDa.BlurrerBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BasicBot extends TelegramLongPollingBot {

    public BasicBot(String TOKEN){
        super(TOKEN);
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage())
            onMessageReceived(update.getMessage());
        if (update.hasInlineQuery())
            onInlineQueryReceived(update.getInlineQuery());
    }

    protected void onInlineQueryReceived(InlineQuery inlineQuery) {
        //Not implemented
    }

    protected void onMessageReceived(Message message){
        //Not implemented
    }

    @Override
    public String getBotUsername() {
        try {
            return getMe().getUserName();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }



}
