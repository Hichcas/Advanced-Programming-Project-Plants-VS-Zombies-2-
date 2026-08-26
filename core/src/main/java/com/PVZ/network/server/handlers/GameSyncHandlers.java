package com.PVZ.network.server.handlers;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.network.server.ClientSession;
import com.PVZ.network.server.MatchmakingManager;
import com.PVZ.network.server.OnlineMatchSession;
import com.PVZ.network.server.RequestDispatcher;

public final class GameSyncHandlers {

    private GameSyncHandlers() {
    }

    public static void registerAll(RequestDispatcher dispatcher) {
        dispatcher.register(MessageType.GAME_PLANT_INPUT, GameSyncHandlers::handlePlantInput);
        dispatcher.register(MessageType.GAME_DEPLOY_INPUT, GameSyncHandlers::handleDeployInput);
        dispatcher.register(MessageType.GAME_OVER, GameSyncHandlers::handleGameOver);
        dispatcher.register(MessageType.SEND_REACTION, GameSyncHandlers::handleSendReaction);
        dispatcher.register(MessageType.SELECTION_READY, GameSyncHandlers::handleSelectionReady);
        dispatcher.register(MessageType.SURRENDER, GameSyncHandlers::handleSurrender);
        dispatcher.register(MessageType.DRAW_OFFER, GameSyncHandlers::handleDrawOffer);
        dispatcher.register(MessageType.DRAW_RESPONSE, GameSyncHandlers::handleDrawResponse);
    }

    private static NetworkMessage handleDrawOffer(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            NetworkMessage forward = NetworkMessage.push(MessageType.DRAW_OFFER);
            opponent.send(forward);
        }
        return null;
    }

    private static NetworkMessage handleDrawResponse(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            NetworkMessage forward = NetworkMessage.push(MessageType.DRAW_RESPONSE)
                .with("accept", request.getBoolean("accept", false));
            opponent.send(forward);
        }
        return null;
    }

    private static NetworkMessage handlePlantInput(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            NetworkMessage forward = NetworkMessage.push(MessageType.GAME_PLANT_INPUT)
                .with("plantType", request.getString("plantType"))
                .with("row", request.getInt("row", 0))
                .with("col", request.getInt("col", 0));
            opponent.send(forward);
        }
        return null;
    }

    private static NetworkMessage handleDeployInput(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            NetworkMessage forward = NetworkMessage.push(MessageType.GAME_DEPLOY_INPUT)
                .with("alias", request.getString("alias"))
                .with("row", request.getInt("row", 0))
                .with("col", request.getInt("col", 0));
            opponent.send(forward);
        }
        return null;
    }

    private static NetworkMessage handleGameOver(ClientSession session, NetworkMessage request) {
        session.setInGame(false);
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            opponent.setInGame(false);
            NetworkMessage forward = NetworkMessage.push(MessageType.GAME_OVER)
                .with("winner", request.getString("winner", ""))
                .with("reason", request.getString("reason", ""));
            opponent.send(forward);
        }

        // پاک‌سازی session مربوطه
        String roomId = session.getCurrentRoomId();
        if (roomId != null) {
            OnlineMatchSession matchSession = MatchmakingManager.getInstance().getSession(roomId);
            if (matchSession != null) {
                matchSession.markGameFinished();
            }
        }
        return null;
    }

    private static NetworkMessage handleSendReaction(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            NetworkMessage forward = NetworkMessage.push(MessageType.REACTION_RECEIVED)
                .with("reaction", request.getString("reaction", "THUMBS_UP"))
                .with("from", session.getUsername());
            opponent.send(forward);
        }
        return null;
    }

    private static NetworkMessage handleSelectionReady(ClientSession session, NetworkMessage request) {
        String roomId = session.getCurrentRoomId();
        if (roomId != null) {
            OnlineMatchSession matchSession = MatchmakingManager.getInstance().getSession(roomId);
            if (matchSession != null) {
                matchSession.markReady(session.getUsername());
            }
        }
        return null;
    }

    private static NetworkMessage handleSurrender(ClientSession session, NetworkMessage request) {
        ClientSession opponent = session.getOpponentSession();
        if (opponent != null && opponent.isInGame()) {
            opponent.setInGame(false);
            NetworkMessage gameOver = NetworkMessage.push(MessageType.GAME_OVER)
                .with("winner", opponent.getCurrentRole())
                .with("reason", "SURRENDER");
            opponent.send(gameOver);
        }

        session.setInGame(false);
        // پاک‌سازی session
        String roomId = session.getCurrentRoomId();
        if (roomId != null) {
            OnlineMatchSession matchSession = MatchmakingManager.getInstance().getSession(roomId);
            if (matchSession != null) {
                matchSession.markGameFinished();
            }
        }
        return null;
    }
}
