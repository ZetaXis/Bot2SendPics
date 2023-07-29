package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

//Базовые обращения бота
public class MainBot extends TelegramLongPollingBot {
    //Файл конфигурации
    ResourceBundle rb = ResourceBundle.getBundle("bot");
    int period = Integer.parseInt(rb.getString("Bot.DefaultPeriod"));
    int maxCountThreads = Integer.parseInt(rb.getString("Bot.countThreads"));
    boolean loggingConnections = Boolean.parseBoolean(rb.getString("Bot.LoggingConnections"));
    Thread[] threads = new Thread[maxCountThreads];
    boolean firstRun = true;
    List<Long> usedChID = new ArrayList<>();
    private static final Logger log = Logger.getLogger(MainBot.class.getName());
    public void SendPics(long chatID, InputFile fileDirect) throws TelegramApiException {
        execute(
        SendPhoto.builder()
                .chatId(chatID)
                .photo(fileDirect)
                .build());
    }
    public void SendText(long chatID, String text) throws TelegramApiException {
        execute(
        SendMessage.builder()
                .text(text)
                .chatId(chatID)
                .build());
    }
    int countPcs = 0;
    int countThread = 0;
    ParserFiles pf = new ParserFiles();
    Timer timer = new Timer();

    public void FirstRun(){
        if (firstRun){
            pf.InitializeChannels();
            firstRun = false;
        }
    }

    public void ThreadSendPics(long chID){
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            int checkWorks = pf.SenderFileList(chID).length;
            service.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        int countDbPics = pf.SenderFileList(chID).length;
                        File file = pf.SenderFileList(chID)[0];
                        SendPics(chID, new InputFile(file));
                        file.delete();
                        if (countPcs >= countDbPics-1) {
                            System.out.println("Empty Folder in "+chID);
                            usedChID.remove(chID);
                            service.shutdown();
                        }
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, period, TimeUnit.MINUTES);

        if (checkWorks  == 0){
            usedChID.remove(chID);
            //System.out.println("removed here!");
            return;
        }
        if (countThread == maxCountThreads-1) countThread = 0;
        else countThread++;
    }

    public void ThreadInit(){
        //Поток помирает если длительное время не получает файлы
        //При попытке вызывать создание потоков по таймеру, помирает таймер внутри отправки сообщений 0_0
        //костыль - идентичная периодичность.
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (long chID : pf.channelsID) {
                    if (!usedChID.contains(chID)){
                        threads[countThread] = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ThreadSendPics(chID);
                            }
                        });
                        threads[countThread].start();
                        usedChID.add(chID);
                    }
                }
            }
        }, 0, period*60*1000);
    }


    @Override
    public void onUpdateReceived(Update update) {
        //Загрузка уже имеющихся каналов в случае если была перезагрузка (прописан в main)

        //Поиск по каналам
        if (update.hasChannelPost() && update.getChannelPost().hasText()){
            /* При логировании помимо отображения соединения происходит пополнение списка для дальнейшей рассылки
               Если логирование отключено то блокируется и пополнение, система уходит в закрытый режим рассылок,
               без инициализаций новых источников */
            if (loggingConnections) {
                long chID = update.getChannelPost().getChatId();
                String chName = update.getChannelPost().getChat().getUserName();

                log.info("Channel ID: " + chID
                        + ", Room name: " + chName);

                //System.out.println(pf.channelsID.contains(chID));
                if (!pf.channelsID.contains(chID)) {
                    pf.channelsID.add(chID);
                    pf.RecordChannel(chID);
                    pf.CreateFolders(String.valueOf(chID), chName);
                }
            }

        }

        //Поиск по обращению из @botname ... !Не нужно, но помечу что это InlineQuery

        // Поиск по локальным сообщениям
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (loggingConnections) log.info("Chat ID: "+update.getMessage().getChatId()
                    +", Sender name: "+update.getMessage().getChat().getUserName());

            if (update.getMessage().getChatId().toString().equals(rb.getString("Bot.AdminChatID"))){
                //Управление из Телеграмм-чата TODO
                try {
                    SendText(update.getMessage().getChatId(), update.getMessage().getText());
                }
                catch (TelegramApiException e){
                    e.printStackTrace();
                }
                if (Objects.equals(update.getMessage().getText(), "/addRoom4Pics")){
                    //SendPics(update.getMessage().getChatId(), new InputFile(new File("index.png")));
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return rb.getString("Bot.Username");
    }
    @Override
    public String getBotToken() {
        return rb.getString("Bot.Token");
    }
}
