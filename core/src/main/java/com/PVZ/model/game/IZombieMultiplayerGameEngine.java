package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.reaction.ReactionCatalog;
import com.PVZ.model.game.reaction.ReactionEvent;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.IZombieLevelDefinition;
import com.PVZ.model.minigame.izombie.IZombieLevelLoader;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.PVZ.view.renderer.EntityRenderer;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

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

    /** واکنش‌های در انتظار نمایش به‌صورت حباب (نگاه کنید به GameScreen که این را poll می‌کند). */
    private final Queue<ReactionEvent> pendingReactionEvents = new ArrayDeque<>();
    /** استیکرهای متحرک فعال روی زمین بازی (انیمیشن PAM واقعی). */
    private final List<StickerEffect> activeStickers = new java.util.ArrayList<>();
    private float reactionCooldown = 0f;
    private static final float REACTION_COOLDOWN_SECONDS = 0.75f;
    private static final float STICKER_LIFETIME_SECONDS = 1.4f;

    private static final class StickerEffect {
        final String path;
        final String clip;
        final float x;
        final float y;
        float elapsed = 0f;

        StickerEffect(String path, String clip, float x, float y) {
            this.path = path;
            this.clip = clip;
            this.x = x;
            this.y = y;
        }
    }

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
            java.util.List<com.PVZ.model.minigame.izombie.ZombieOption> customRoster = new java.util.ArrayList<>();
            for (String alias : selectedZombies) {
                com.PVZ.model.enums.ZombieType zt = null;
                for (com.PVZ.model.enums.ZombieType t : com.PVZ.model.enums.ZombieType.values()) {
                    if (t.alias.equalsIgnoreCase(alias)) {
                        zt = t;
                        break;
                    }
                }
                int cost = calculateZombieCost(zt);
                String name = formatZombieDisplayName(zt);
                customRoster.add(new com.PVZ.model.minigame.izombie.ZombieOption(alias, cost, name));
            }
            def.setZombieRoster(customRoster);
        }
        IZombieGame izGame = new IZombieGame(def);
        setGame(izGame);
    }

    private static int calculateZombieCost(com.PVZ.model.enums.ZombieType type) {
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

    private static String formatZombieDisplayName(com.PVZ.model.enums.ZombieType type) {
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
        // In multiplayer, clear local random seeds so both sides start with an identical empty/synced lawn
        plants.clear();
        if (map != null && getGame() != null) {
            for (int r = 0; r < getGame().getRows(); r++) {
                for (int c = 0; c < getGame().getRedLineCol(); c++) {
                    map.setPlant(r, c, null);
                }
            }
        }
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
            float slotSize = 110f;
            float gap = 12f;
            int count = plantOptions.size();
            float barWidth = count * slotSize + (count - 1) * gap;
            float barX = (2560f - barWidth) / 2f;
            float topY = 1440f - 40f - slotSize;
            plantBar.layout(plantOptions, barX, topY, false);
        }
        if (getGame() != null && getGame().getSun() < 300) {
            getGame().addSun(300 - getGame().getSun());
            if (gameStatus != null) {
                gameStatus.setSunflower(getGame().getSun());
            }
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
            String reactionId = msg.getString("reaction");
            Gdx.app.postRunnable(() -> handleReaction(reactionId, false));
        });
    }

    private void handleRemotePlant(String typeName, int row, int col) {
        if (map == null || row < 0 || col < 0) return;
        try {
            PlantType type = PlantType.valueOf(typeName);
            Plant p = PlantFactory.createPlant(type, 1);
            if (p != null) {
                map.setPlant(row, col, p);
                plants.add(p);
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

    /**
     * ارسال یک واکنش به حریف (شناسه از {@link ReactionCatalog}). یک کول‌داون کوتاه
     * دارد تا کسی نتواند با اسپم کلیک، شبکه یا صفحه‌ی حریف را پر کند.
     */
    public void sendReaction(String reactionId) {
        if (ReactionCatalog.findById(reactionId) == null) return;
        if (reactionCooldown > 0f) return;
        reactionCooldown = REACTION_COOLDOWN_SECONDS;

        NetworkMessage msg = NetworkMessage.push(MessageType.SEND_REACTION)
            .with("reaction", reactionId);
        NetworkSession.client().sendFireAndForget(msg);
        handleReaction(reactionId, true);
    }

    private void handleReaction(String reactionId, boolean mine) {
        ReactionCatalog.Reaction reaction = ReactionCatalog.findById(reactionId);
        if (reaction == null) return;

        activeReaction = (mine ? "You: " : opponentName + ": ") + reaction.label();
        reactionDisplayTimer = 3.0f;
        pendingReactionEvents.offer(new ReactionEvent(reactionId, mine));

        if (reaction.kind() == ReactionCatalog.Kind.STICKER && reaction.pamPath() != null && map != null) {
            float centerX = map.getStartX() + (map.getCols() > 0 ? map.getCols() : 9) * map.getTileWidth() / 2f;
            float centerY = map.getStartY() - (map.getRows() > 0 ? map.getRows() : 5) * map.getTileHeight() / 2f;
            activeStickers.add(new StickerEffect(reaction.pamPath(), reaction.pamClip(), centerX, centerY));
        }
    }

    /**
     * یک واکنشِ در صف را برای نمایش به‌صورت حباب (ReactionBubble در GameScreen)
     * برمی‌دارد؛ اگر چیزی در صف نباشد null برمی‌گرداند.
     */
    public ReactionEvent pollReactionEvent() {
        return pendingReactionEvents.poll();
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

        int cost = (plant.getInstance() != null && plant.getInstance().getDefinition() != null)
            ? plant.getInstance().getDefinition().getCost()
            : 100;
        if (getGame().getSun() < cost) {
            return false;
        }
        getGame().addSun(-cost);
        if (gameStatus != null) {
            gameStatus.setSunflower(getGame().getSun());
        }

        map.setPlant(row, col, plant);
        plants.add(plant);
        NetworkMessage msg = NetworkMessage.push(MessageType.GAME_PLANT_INPUT)
            .with("plantType", type.name())
            .with("row", row)
            .with("col", col);
        NetworkSession.client().sendFireAndForget(msg);
        return true;
    }

    public String getMyRole() { return myRole; }
    public SeedPacketBar getPlantBar() { return plantBar; }
    public PlantType getSelectedPlantType() { return selectedPlantType; }
    public void setSelectedPlantType(PlantType selectedPlantType) { this.selectedPlantType = selectedPlantType; }

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
        if (reactionCooldown > 0f) {
            reactionCooldown -= delta;
        }
        if (!activeStickers.isEmpty()) {
            Iterator<StickerEffect> it = activeStickers.iterator();
            while (it.hasNext()) {
                StickerEffect fx = it.next();
                fx.elapsed += delta;
                if (fx.elapsed >= STICKER_LIFETIME_SECONDS) {
                    it.remove();
                }
            }
        }
    }

    @Override
    protected void drawHud(SpriteBatch batch) {
        if (getGame() == null || map == null) return;
        ensureTexturesLoaded();
        batch.begin();
        if (myRole.equals("ZOMBIE")) {
            IZombieInputProcessor izInput = (inputProcessor instanceof IZombieInputProcessor inp) ? inp : null;
            String selectedAlias = izInput != null ? izInput.getSelectedAlias() : null;
            zombiePacketBar.draw(batch, font, tinyFont, getGame(), selectedAlias);

            if (izInput != null && hudPixel != null) {
                int row = izInput.getSelectedRow();
                int minZombieCol = getGame().getRedLineCol() + 1;
                int maxZombieCol = getGame().getCols() - 1;
                int col = Math.max(minZombieCol, Math.min(maxZombieCol, izInput.getSelectedCol()));
                float th = map.getTileHeight();
                float tw = map.getTileWidth();
                float rowY = map.getStartY() - (row + 1) * th;
                float deployX = map.getStartX() + col * tw;

                // Soft brightness boost on the selected tile
                float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.004) * 0.06 + 0.22);
                batch.setColor(1f, 1f, 1f, pulse);
                batch.draw(hudPixel, deployX + 2f, rowY + 2f, tw - 4f, th - 4f);

                // Elegant subtle border outline (2px)
                batch.setColor(1f, 0.92f, 0.5f, 0.75f);
                batch.draw(hudPixel, deployX + 2f, rowY + 2f, tw - 4f, 2f);
                batch.draw(hudPixel, deployX + 2f, rowY + th - 4f, tw - 4f, 2f);
                batch.draw(hudPixel, deployX + 2f, rowY + 2f, 2f, th - 4f);
                batch.draw(hudPixel, deployX + tw - 4f, rowY + 2f, 2f, th - 4f);
                batch.setColor(Color.WHITE);
            }
        } else if (myRole.equals("PLANT")) {
            plantBar.drawIconsAndLabels(batch, FontManager.getInstance().getEnglishMenuFont(), null, selectedPlantType);
        }
        batch.end();
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

        drawActiveStickers(batch);
    }

    /** استیکرهای متحرک فعال را (انیمیشن PAM واقعی) روی وسط زمین بازی پخش می‌کند. */
    private void drawActiveStickers(SpriteBatch batch) {
        if (activeStickers.isEmpty()) return;
        batch.begin();
        for (StickerEffect fx : activeStickers) {
            EntityRenderer.getInstance().renderPam(batch, fx.path, fx.clip, fx.elapsed, fx.x - 128f, fx.y - 128f);
        }
        batch.end();
    }
}
