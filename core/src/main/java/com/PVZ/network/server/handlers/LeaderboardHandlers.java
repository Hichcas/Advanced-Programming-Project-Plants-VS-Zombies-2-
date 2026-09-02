package com.PVZ.network.server.handlers;

import com.PVZ.model.leaderboard.Leaderboard;
import com.PVZ.model.leaderboard.LeaderboardEntry;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.network.server.ClientSession;
import com.PVZ.network.server.RequestDispatcher;

import java.util.List;

public final class LeaderboardHandlers {

    private LeaderboardHandlers() {
    }

    public static void registerAll(RequestDispatcher dispatcher) {
        dispatcher.register(MessageType.FETCH_LEADERBOARD, LeaderboardHandlers::handleFetchLeaderboard);
    }

    private static NetworkMessage handleFetchLeaderboard(ClientSession session, NetworkMessage request) {
        try {
            String sortName = request.getString("sort");
            com.PVZ.model.leaderboard.LeaderboardSortField sort = null;
            if (sortName != null) {
                try {
                    sort = com.PVZ.model.leaderboard.LeaderboardSortField.valueOf(sortName);
                } catch (IllegalArgumentException ignored) {
                }
            }
            boolean ascending = request.getBoolean("ascending", true);
            UserRegistry.saveAllDirtyUsers();
            List<LeaderboardEntry> entries = Leaderboard.getEntries(sort, ascending);
            return NetworkMessage.reply(request.getRequestId(), MessageType.FETCH_LEADERBOARD_RESULT)
                    .with("success", true)
                    .with("entries", entries);
        } catch (Exception e) {
            return NetworkMessage.reply(request.getRequestId(), MessageType.FETCH_LEADERBOARD_RESULT)
                    .with("success", false)
                    .with("message", "Failed to fetch leaderboard: " + e.getMessage());
        }
    }
}
