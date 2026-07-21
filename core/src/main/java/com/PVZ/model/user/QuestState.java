package com.PVZ.model.user;

import com.PVZ.model.quest.QuestManager;
import java.util.List;
import com.PVZ.model.quest.Quest;

public class QuestState {
    private QuestManager questManager;

    public QuestState() {
        this.questManager = new QuestManager();
    }

    public QuestManager getQuestManager() { return questManager; }
    public void setQuestManager(QuestManager questManager) { this.questManager = questManager; }
}
