package com.PVZ.network.server.handlers;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.network.server.ClientSession;
import com.PVZ.network.server.MatchmakingManager;
import com.PVZ.network.server.RequestDispatcher;

public final class MatchmakingHandlers {

    private MatchmakingHandlers() {
    }

    public static void registerAll(RequestDispatcher dispatcher) {
        dispatcher.register(MessageType.CHALLENGE_USER, MatchmakingHandlers::handleChallengeUser);
        dispatcher.register(MessageType.JOIN_RANDOM_QUEUE, MatchmakingHandlers::handleJoinRandomQueue);
        dispatcher.register(MessageType.LEAVE_RANDOM_QUEUE, MatchmakingHandlers::handleLeaveRandomQueue);
        dispatcher.register(MessageType.CHALLENGE_RESPONSE, MatchmakingHandlers::handleChallengeResponse);
    }

    private static NetworkMessage handleChallengeUser(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return error(request, "Not logged in.");
        }

        String targetUsername = request.getString("username");
        String result = MatchmakingManager.getInstance().challenge(session, targetUsername);
        if (result != null) {
            return error(request, result);
        }

        return NetworkMessage.reply(request.getRequestId(), MessageType.CHALLENGE_USER)
            .with("success", true)
            .with("message", "Invitation sent to " + targetUsername + ".");
    }

    private static NetworkMessage handleJoinRandomQueue(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return error(request, "Not logged in.");
        }

        MatchmakingManager.getInstance().joinRandomQueue(session);
        return NetworkMessage.reply(request.getRequestId(), MessageType.JOIN_RANDOM_QUEUE)
            .with("success", true)
            .with("message", "Joined random queue.");
    }

    private static NetworkMessage handleLeaveRandomQueue(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return error(request, "Not logged in.");
        }

        MatchmakingManager.getInstance().leaveRandomQueue(session);
        return NetworkMessage.reply(request.getRequestId(), MessageType.LEAVE_RANDOM_QUEUE)
            .with("success", true)
            .with("message", "Left random queue.");
    }

    private static NetworkMessage handleChallengeResponse(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return error(request, "Not logged in.");
        }

        boolean accept = request.getBoolean("accept", false);
        String result = MatchmakingManager.getInstance().respondToInvitation(session, accept);
        if (result != null) {
            return error(request, result);
        }

        return NetworkMessage.reply(request.getRequestId(), MessageType.CHALLENGE_RESPONSE)
            .with("success", accept)
            .with("message", accept ? "Invitation accepted." : "Invitation declined.");
    }

    private static NetworkMessage error(NetworkMessage request, String message) {
        return NetworkMessage.reply(request.getRequestId(), MessageType.SERVER_ERROR)
            .with("success", false)
            .with("message", message);
    }
}
