package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;


public class BotLogic extends TelegramLongPollingBot {
    Map<String, ArrayList<Integer>> mediaGroups = new HashMap<>();
    Map<String, ArrayList<String>> photoGroups = new HashMap<>();
    Deque<String> mediaGroupIdsQueue = new LinkedList<>();
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

        }else if(message.isGroupMessage() || message.isSuperGroupMessage()){
            //new behaviour to develop
            groupMessageBehaviour(message);
        }

    }
    private void userMessageBehaviour(Message message) {
        String mediaGroupId = message.getMediaGroupId();
        if (message.hasPhoto()) {
            if(mediaGroupId == null){
                // If the message received is a "photo" type, it'll echo it but blurred
                //If only 1 photo will be sent
                this.sendPhotoWithSpoiler(message.getChatId(),message.getPhoto());
            }else{
                // handle media group
                this.mediaGroupIdsQueue.add(mediaGroupId);
                this.mediaGroups.putIfAbsent(mediaGroupId, new ArrayList<>());
                this.photoGroups.putIfAbsent(mediaGroupId, new ArrayList<>());

                //Check to save the server from explode
                if(mediaGroupIdsQueue.size() > 3){
                    String toRemoveCachedMediaGroupId = this.mediaGroupIdsQueue.pop();
                    this.mediaGroups.remove(toRemoveCachedMediaGroupId);
                    this.photoGroups.remove(toRemoveCachedMediaGroupId);
                }

                //Ottenere i message id da cancellare
                ArrayList<Integer> messageIds = this.mediaGroups.get(mediaGroupId);
                //Ottenere i file id delle foto
                ArrayList<String> photoFileIds = this.photoGroups.get(mediaGroupId);

                if(messageIds.isEmpty()){
                    //If is the first photo it will be sent
                    List<PhotoSize> photoSizeList = message.getPhoto();
                    Message sentMessage = this.sendPhotoWithSpoiler(message.getChatId(),photoSizeList);
                    //And it will be saved
                    if(sentMessage == null)return;
                    //save
                    messageIds.add(sentMessage.getMessageId());
                    photoFileIds.add(getPhotoMaxResolutionFileId(photoSizeList));

                }else{
                    //If not the first photo then the first will be deleted, and it will be all sent it again

                    SendMediaGroup toSendmediaGroup = getSendMediaGroup(message, messageIds, photoFileIds);

                    //mandare il mediagroup
                    try {
                        List<Message> sentMessages = execute(toSendmediaGroup);
                        //save
                        photoFileIds.add(this.getPhotoMaxResolutionFileId(message.getPhoto()));
                        for( Message sentMessage: sentMessages){
                            messageIds.add(sentMessage.getMessageId());
                        }
                    }catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                }

            }

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
        //if u reply the photo with bot username it on group chat

        System.out.println(this.getBotUsername());
        if(message.hasText() && message.getText().contains(this.getBotUsername()) && message.isReply()){
            Message replyToMessage = message.getReplyToMessage();
            if (replyToMessage.hasPhoto())
                this.sendPhotoWithSpoiler(message.getChatId(),replyToMessage.getPhoto());
        }

    }

    private Message sendPhotoWithSpoiler(Long chatId, List<PhotoSize> photos) {
        // Takes only the largest photo for best quality (usually the last)
        String photoFileId = getPhotoMaxResolutionFileId(photos);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);                            //  Syncs it with the Chat's ID
        sendPhoto.setPhoto(new InputFile(photoFileId));         //  Creates the new "photo" file to be sent
        sendPhoto.setHasSpoiler(true);                          //  Sets the .setHasSpoiler(Boolean) tag to true
        try {
            return execute(sendPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPhotoMaxResolutionFileId(List<PhotoSize> photos){
        return photos.get(photos.size() - 1).getFileId();
    }

    private SendMediaGroup getSendMediaGroup(Message message, ArrayList<Integer> messageIds, ArrayList<String> photoFileIds) {
        //Lista di media
        List<InputMedia> medias = new ArrayList<>();

        Iterator<Integer> iterator = messageIds.iterator();
        while (iterator.hasNext()){
            Integer messageId = iterator.next();
            //Cancellare il messaggio della foto
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(message.getChatId());
            deleteMessage.setMessageId(messageId);
            try {
                execute(deleteMessage);
                iterator.remove();
            }catch (TelegramApiException e){
                e.printStackTrace();
            }
        }


        for (String photoFileId : photoFileIds){
            //Aggiungere al mediagroup il file id delle foto
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
            inputMediaPhoto.setMedia(photoFileId);
            inputMediaPhoto.setHasSpoiler(true);
            medias.add(inputMediaPhoto);
        }

        //Aggiungere l' ultima photo
        InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
        inputMediaPhoto.setMedia(this.getPhotoMaxResolutionFileId(message.getPhoto()));
        inputMediaPhoto.setHasSpoiler(true);
        medias.add(inputMediaPhoto);


        //Creare il mediagroup
        SendMediaGroup mediaGroup = new SendMediaGroup();
        mediaGroup.setChatId(message.getChatId());
        mediaGroup.setMedias(medias);
        //mediaGroup.setProtectContent(true);
        return mediaGroup;
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
