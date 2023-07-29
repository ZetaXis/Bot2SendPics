package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.io.File;
import java.util.*;

public class ParserFiles {
    File dbDir = new File("db");
    public List<Long> channelsID = new ArrayList<>();
    public List<String> channelsName = new ArrayList<>();
    public List<List<String>> pictureData;

    public void InitializeChannels() {
        if (dbDir.exists()){
            for (String existID:dbDir.list()) {
                channelsID.add(Long.valueOf(existID));
            }
        }
    }

    public void RecordChannel(long chID) {

    }

    public void CreateFolders(String chID, String chName) {
        if (!dbDir.exists()) dbDir.mkdir();
        File chDir = new File(dbDir + "/" + chID); //+"("+chName+")" было бы удобнее, но с другой стороны тяжело тогда передавать это значение.
        if (!chDir.exists()) chDir.mkdir();
    }

    public File[] SenderFileList(Long channelID) {
        File senderDir = new File(dbDir + "/" + channelID);
        return senderDir.listFiles();
    }
}
