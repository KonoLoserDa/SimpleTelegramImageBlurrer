package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class BotLogic extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage();            // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("This bot handles only images.");   //  Custom text upon receiving a text message
            try {
                execute(message);                               // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            // If the message received is a "photo" type, it'll echo it but blurred
            List<PhotoSize> photos = update.getMessage().getPhoto();
            // Takes only the largest photo for best quality (usually the last)
            String fileId = photos.get(photos.size() - 1).getFileId();
            sendPhotoWithSpoiler(update.getMessage().getChatId(), fileId);
        }
    }
    private void sendPhotoWithSpoiler(Long chatId, String photoFileId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);                            //  Syncs it with the Chat's ID
        sendPhoto.setPhoto(new InputFile(photoFileId));         //  Creates the new "photo" file to be sent
        sendPhoto.setHasSpoiler(true);                          //  Sets the .setHasSpoiler(Boolean) tag to true

        try {
            execute(sendPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public String getBotUsername() {
        return "Your_Bot_Username_Here";
    }
    @Override
    public String getBotToken() {
        return "Your_Bot_Token_Here";
    }
    //  I'm not sure if the last two here are necessary
    @Override
    public void onRegister() {
        super.onRegister();
    }
    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }
}
