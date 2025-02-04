package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class BotLogic extends TelegramLongPollingBot {

    public BotLogic(String botToken){
        super(botToken);
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage())
            onMessageReceived(update.getMessage());



    }


    private void onMessageReceived(Message message){
        //Should be private chat
        if(message.isUserMessage()){
            userMessageBehaviour(message);

        }else if(message.isGroupMessage()){
            //new behaviour to develop
            groupMessageBehaviour(message);
        }

    }
    private void userMessageBehaviour(Message message) {
        if (message.hasPhoto()) {
            // If the message received is a "photo" type, it'll echo it but blurred
            this.sendPhotoWithSpoiler(message.getChatId(),message.getPhoto());
        }else if (message.hasText()) {
            SendMessage sendMessage = new SendMessage();            // Create a SendMessage object with mandatory fields
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("This bot handles only images.");   //  Custom text upon receiving a text message
            try {

                execute(sendMessage);                               // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private void groupMessageBehaviour(Message message) {
        //if u tag it on group chat
        if(message.hasText() && message.getText().contains(this.getBotUsername()) && message.isReply()){
            Message replyToMessage = message.getReplyToMessage();
            if (replyToMessage.hasPhoto())
                this.sendPhotoWithSpoiler(message.getChatId(),message.getPhoto());
        }

    }

    private void sendPhotoWithSpoiler(Long chatId, List<PhotoSize> photos) {
        // Takes only the largest photo for best quality (usually the last)
        String photoFileId = photos.get(photos.size() - 1).getFileId();

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
        try {
            return getMe().getUserName();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

}
