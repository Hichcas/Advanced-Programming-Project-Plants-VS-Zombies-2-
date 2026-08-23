package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.IZombieLevelDefinition;
import com.PVZ.model.minigame.izombie.IZombieLevelLoader;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class IZombieMultiplayerGameEngine extends IZombieGameEngine {

    private final String myRole; // "PLANT" or "ZOMBIE"
    private final String opponentName;
    private final String roomId;
    private final int levelId;

    private float matchTimeRemaining = 120.0f; // 2 minutes
    private boolean matchFinished = false;
    private String matchResultText = "";
    private boolean wonMatch = false;

    private final SeedPacketBar plantBar = new SeedPacketBar();
    private PlantType selectedPlantType = null;
    private String activeReaction = null;
    private float reactionDisplayTimer = 0f;

    private final List<PlantType> selectedPlants;
    private final List<String> selectedZombies;

    public IZombieMultiplayerGameEngine(String myRole, String opponentName, String roomId, int levelId) {
        this(myRole, opponentName, roomId, levelId, null, null);
    }

    public IZombieMultiplayerGameEngine(String myRole, String opponentName, String roomId, int levelId,
                                        List<PlantType> selectedPlants, List<String> selectedZombies) {
        super();
        this.myRole = (myRole != null && myRole.equalsIgnoreCase("PLANT")) ? "PLANT" : "ZOMBIE";
        this.opponentName = opponentName != null ? opponentName : "Opponent";
        this.roomId = roomId;
        this.levelId = levelId >= 1 && levelId <= 3 ? levelId : 1;
        this.selectedPlants = selectedPlants;
        this.selectedZombies = selectedZombies;

        setupLevel();
        registerNetworkListeners();
    }

    private void setupLevel() {
        IZombieLevelDefinition def = new IZombieLevelLoader().loadLevel(levelId);
        if (selectedZombies != null && !selectedZombies.isEmpty()) {
            List<ZombieOption> customRoster = new ArrayList<>();
            for (String alias : selectedZombies) {
                ZombieType zt = null;
                for (ZombieType t : ZombieType.values()) {
                    if (t.alias.equalsIgnoreCase(alias)) {
                        zt = t;
                        break;
                    }
                }
                int cost = calculateZombieCost(zt);
                String name = formatZombieDisplayName(zt);
                customRoster.add(new ZombieOption(alias, cost, name));
            }
            def.setZombieRoster(customRoster);
        }
        IZombieGame izGame = new IZombieGame(def);
        setGame(izGame);
    }

    private static int calculateZombieCost(ZombieType type) {
        if (type == null) return 50;
        String name = type.name().toUpperCase();
        if (name.contains("GARGANTUAR") || name.contains("ZOMBOSS")) return 300;
        if (name.contains("BRICK") || name.contains("KNIGHT") || name.contains("ARMOR2") || name.contains("CENTURION")) return 150;
        if (name.contains("BUCKET") || name.contains("BARREL") || name.contains("JALAPENO") || name.contains("SQUASH")) return 125;
        if (name.contains("CONE") || name.contains("ARMOR1") || name.contains("HELMET") || name.contains("FLAG")) return 75;
        if (name.contains("IMP")) return 25;
        if (name.contains("ZOMBOTANY")) return 100;
        return 50;
    }

    private static String formatZombieDisplayName(ZombieType type) {
        if (type == null) return "Zombie";
        String name = type.name().replace("ZOMBOTANY_", "Zombotany ").replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String part : name.split(" ")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override
    public void initializeBoard() {
        super.initializeBoard();
        if (myRole.equals("PLANT")) {
            List<PlantType> plantOptions = (selectedPlants != null && !selectedPlants.isEmpty())
                ? selectedPlants
                : List.of(
                    PlantType.PEASHOOTER,
                    PlantType.SUNFLOWER,
                    PlantType.WALL_NUT,
                    PlantType.SNOW_PEA,
                    PlantType.REPEATER,
                    PlantType.POTATO_MINE
                );
            plantBar.layout(plantOptions, 400f, 1440f - 160f);
        }
    }

    private void registerNetworkListeners() {
        NetworkSession.client().on(MessageType.GAME_PLANT_INPUT, msg -> {
            String typeName = msg.getString("plantType");
            int row = msg.getInt("row", -1);
            int col = msg.getInt("col", -1);
            Gdx.app.postRunnable(() -> handleRemotePlant(typeName, row, col));
        });

        NetworkSession.client().on(MessageType.GAME_DEPLOY_INPUT, msg -> {
            String alias = msg.getString("alias");
            int row = msg.getInt("row", -1);
            int col = msg.getInt("col", -1);
            Gdx.app.postRunnable(() -> handleRemoteDeploy(alias, row, col));
        });

        NetworkSession.client().on(MessageType.GAME_OVER, msg -> {
            String winner = msg.getString("winner");
            Gdx.app.postRunnable(() -> handleRemoteGameOver(winner));
        });

        NetworkSession.client().on(MessageType.REACTION_RECEIVED, msg -> {
            String reaction = msg.getString("reaction");
            Gdx.app.postRunnable(() -> showReaction(reaction));
        });
    }

    private void handleRemotePlant(String typeName, int row, int col) {
        if (map == null || row < 0 || col < 0) return;
        try {
            PlantType type = PlantType.valueOf(typeName);
            Plant p = PlantFactory.createPlant(type, 1);
            if (p != null) {
                map.setPlant(row, col, p);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleRemoteDeploy(String alias, int row, int col) {
        if (alias == null || row < 0 || col < 0) return;
        spawnZombie(alias, row, col);
    }

    private void handleRemoteGameOver(String winner) {
        if (matchFinished) return;
        matchFinished = true;
        wonMatch = myRole.equalsIgnoreCase(winner);
        matchResultText = wonMatch ? "VICTORY! YOU WIN!" : "DEFEAT! YOU LOSE!";
    }

    public void sendReaction(String reaction) {
        NetworkMessage msg = NetworkMessage.push(MessageType.SEND_REACTION)
            .with("reaction", reaction);
        NetworkSession.client().sendFireAndForget(msg);
        showReaction("You: " + reaction);
    }

    private void showReaction(String text) {
        activeReaction = text;
        reactionDisplayTimer = 3.0f;
    }

    @Override
    public String deployZombie(String alias, int row, int col) {
        if (!myRole.equals("ZOMBIE")) {
            return "You are playing as PLANT; you cannot deploy zombies.";
        }
        String res = super.deployZombie(alias, row, col);
        if (res.startsWith("Deployed")) {
            NetworkMessage msg = NetworkMessage.push(MessageType.GAME_DEPLOY_INPUT)
                .with("alias", alias)
                .with("row", row)
                .with("col", col);
            NetworkSession.client().sendFireAndForget(msg);
        }
        return res;
    }

    public boolean plantByPlayer(PlantType type, int row, int col) {
        if (!myRole.equals("PLANT") || map == null || getGame() == null) return false;
        if (col >= getGame().getRedLineCol()) return false;
        if (map.getPlantAt(row, col) != null) return false;

        Plant plant = PlantFactory.createPlant(type, 1);
        if (plant == null) return false;

        map.setPlant(row, col, plant);
        NetworkMessage msg = NetworkMessage.push(MessageType.GAME_PLANT_INPUT)
            .with("plantType", type.name())
            .with("row", row)
            .with("col", col);
        NetworkSession.client().sendFireAndForget(msg);
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!matchFinished) {
            matchTimeRemaining -= delta;
            if (matchTimeRemaining <= 0f) {
                matchTimeRemaining = 0f;
                matchFinished = true;
                wonMatch = myRole.equals("PLANT");
                matchResultText = wonMatch ? "TIME UP - PLANT WINS!" : "TIME UP - ZOMBIE LOSES!";
                NetworkMessage msg = NetworkMessage.push(MessageType.GAME_OVER)
                    .with("winner", "PLANT")
                    .with("reason", "TIME_UP");
                NetworkSession.client().sendFireAndForget(msg);
            } else if (getGame() != null && getGame().getBrainsRemaining() <= 0) {
                matchFinished = true;
                wonMatch = myRole.equals("ZOMBIE");
                matchResultText = wonMatch ? "ALL BRAINS EATEN - ZOMBIE WINS!" : "BRAINS LOST - PLANT LOSES!";
                NetworkMessage msg = NetworkMessage.push(MessageType.GAME_OVER)
                    .with("winner", "ZOMBIE")
                    .with("reason", "BRAINS_EATEN");
                NetworkSession.client().sendFireAndForget(msg);
            }
        }

        if (reactionDisplayTimer > 0f) {
            reactionDisplayTimer -= delta;
            if (reactionDisplayTimer <= 0f) {
                activeReaction = null;
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
        renderMultiplayerHud(batch);
    }

    private void renderMultiplayerHud(SpriteBatch batch) {
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        batch.begin();

        int mins = (int) (matchTimeRemaining / 60);
        int secs = (int) (matchTimeRemaining % 60);
        String timeStr = String.format("%02d:%02d", mins, secs);

        String info = String.format("ROLE: %s | VS: %s | TIME: %s | LEVEL: %d",
            myRole, opponentName, timeStr, levelId);
        font.setColor(Color.YELLOW);
        font.draw(batch, info, 100f, 1400f);

        if (activeReaction != null) {
            font.setColor(Color.CYAN);
            font.draw(batch, "Reaction: " + activeReaction, 100f, 1340f);
        }

        if (matchFinished) {
            font.setColor(wonMatch ? Color.GREEN : Color.RED);
            font.draw(batch, matchResultText, 900f, 750f);
        }

        font.setColor(Color.WHITE);
        batch.end();
    }
}
