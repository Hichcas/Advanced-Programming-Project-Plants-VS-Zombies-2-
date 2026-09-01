package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.IZombieLevelDefinition;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local 2-Player Versus Mode (Same Laptop / Same Screen).
 * - Player 1 (Plants): Left vertical bar below the sun bank, uses Mouse.
 * - Player 2 (Zombies): Right vertical bar, uses Keyboard (1-8, WASD, Space).
 * - Red line at column 5.
 * - Both Plants and Zombies are randomly selected from valid rosters.
 */
public class IZombieLocalVersusEngine extends IZombieGameEngine {

    private final SeedPacketBar plantBar = new SeedPacketBar();
    private List<PlantType> randomPlantOptions = new ArrayList<>();
    private PlantType selectedPlantType;
    private int plantSun = 350;
    private float animTime = 0f;

    private boolean matchFinished = false;
    private String matchResultText = "";
    private boolean plantsWon = false;

    private static final float MATCH_DURATION = 180f; // 3 minutes
    private float matchTimeRemaining = MATCH_DURATION;

    /** استیکرهای متحرک فعال روی زمین بازی (انیمیشن PAM واقعی) - مود لوکال دو‌نفره. */
    private final List<LocalStickerEffect> activeStickers = new ArrayList<>();
    private float reactionCooldown = 0f;
    private static final float REACTION_COOLDOWN_SECONDS = 0.75f;

    private static final class LocalStickerEffect {
        final String path;
        final String clip;
        final float x;
        final float y;
        final float lifetime;
        float elapsed = 0f;

        LocalStickerEffect(String path, String clip, float x, float y, float lifetime) {
            this.path = path;
            this.clip = clip;
            this.x = x;
            this.y = y;
            this.lifetime = lifetime;
        }
    }

    /**
     * برای مود دو‌نفره‌ی لوکال (روی یک صفحه): چون هر دو بازیکن همین صفحه را می‌بینند،
     * نیازی به ارسال شبکه‌ای نیست - فقط افکت/حباب روی همان صفحه‌ی مشترک نشان داده
     * می‌شود (دقیقا مثل ری‌اکشن نسخه‌ی آنلاین، منتها بدون شبکه).
     */
    public void sendReaction(String reactionId) {
        com.PVZ.model.game.reaction.ReactionCatalog.Reaction reaction =
                com.PVZ.model.game.reaction.ReactionCatalog.findById(reactionId);
        if (reaction == null || reactionCooldown > 0f) return;
        reactionCooldown = REACTION_COOLDOWN_SECONDS;

        pendingLocalReaction = reaction;

        if (reaction.pamPath() != null && map != null) {
            float centerX = map.getStartX() + (map.getCols() > 0 ? map.getCols() : 9) * map.getTileWidth() / 2f;
            float centerY = map.getStartY() - (map.getRows() > 0 ? map.getRows() : 5) * map.getTileHeight() / 2f;
            activeStickers.add(new LocalStickerEffect(reaction.pamPath(), reaction.pamClip(),
                    centerX, centerY, Math.max(0.6f, reaction.pamLifetimeSeconds())));
        }
    }

    /** آخرین ری‌اکشنی که باید به‌صورت حباب توسط GameScreen نمایش داده شود؛ بعد از خواندن null می‌شود. */
    private com.PVZ.model.game.reaction.ReactionCatalog.Reaction pendingLocalReaction;

    public com.PVZ.model.game.reaction.ReactionCatalog.Reaction pollLocalReaction() {
        com.PVZ.model.game.reaction.ReactionCatalog.Reaction r = pendingLocalReaction;
        pendingLocalReaction = null;
        return r;
    }

    private static com.badlogic.gdx.graphics.g2d.NinePatch zombiePanelBackground;

    private static com.badlogic.gdx.graphics.g2d.NinePatch getZombiePanelBackground() {
        if (zombiePanelBackground == null) {
            int border = 10;
            int size = border * 2 + 4;
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.
                Pixmap(size, size, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.06f, 0.05f, 0.03f, 0.72f);
            pixmap.fill();
            pixmap.setColor(0.85f, 0.65f, 0.25f, 1f);
            pixmap.drawRectangle(0, 0, size, size);
            com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(pixmap);
            pixmap.dispose();
            zombiePanelBackground =
                new com.badlogic.gdx.graphics.g2d.NinePatch(texture, border, border, border, border);
        }
        return zombiePanelBackground;
    }

    public IZombieLocalVersusEngine(int levelId) {
        super();
        setupLevel(levelId);
    }

    private void setupLevel(int levelId) {
        IZombieLevelDefinition def = new IZombieLevelDefinition();
        def.setId(levelId);
        def.setRedLineCol(5); // Red line at column 5
        def.setStartingSun(350);
        def.setPlantDensity(0.0);
        def.setSunProductionBase(100.0);
        def.setSunProductionGrowthPerTick(15.0);
        def.setSunProductionIntervalSeconds(8.0);
        def.setSunProductionCap(300.0);

        // 1. Randomly select 8 valid distinct plants
        List<PlantType> validPlants = new ArrayList<>();
        for (PlantType pt : PlantType.values()) {
            if (pt.getDefinition() != null) {
                validPlants.add(pt);
            }
        }
        Collections.shuffle(validPlants);
        randomPlantOptions = new ArrayList<>(validPlants.subList(0, Math.min(8, validPlants.size())));

        // 2. Randomly select 8 valid distinct zombies
        List<ZombieType> validZombies = new ArrayList<>();
        for (ZombieType zt : ZombieType.values()) {
            if (zt.alias != null && !zt.alias.isEmpty() && !zt.name().startsWith("ZOMBOSS")) {
                validZombies.add(zt);
            }
        }
        Collections.shuffle(validZombies);
        List<ZombieOption> customRoster = new ArrayList<>();
        int zCount = 0;
        for (ZombieType zt : validZombies) {
            if (zCount >= 8) break;
            int cost = calculateZombieCost(zt);
            String name = formatZombieDisplayName(zt);
            customRoster.add(new ZombieOption(zt.alias, cost, name));
            zCount++;
        }
        def.setZombieRoster(customRoster);

        IZombieGame izGame = new IZombieGame(def);
        setGame(izGame);
    }

    private static int calculateZombieCost(ZombieType type) {
        if (type == null) return 50;
        String name = type.name().toUpperCase();
        if (name.contains("GARGANTUAR")) return 300;
        if (name.contains("BRICK") || name.contains("KNIGHT") ||
            name.contains("ARMOR4") || name.contains("CENTURION")) return 150;
        if (name.contains("BUCKET") || name.contains("BARREL") ||
            name.contains("ARMOR2") || name.contains("JALAPENO") || name.contains("SQUASH")) return 125;
        if (name.contains("CONE") || name.contains("ARMOR1") || name.contains("HELMET")
            || name.contains("FLAG")) return 75;
        if (name.contains("IMP")) return 25;
        if (name.contains("ZOMBOTANY")) return 100;
        return 50;
    }

    private static String formatZombieDisplayName(ZombieType type) {
        if (type == null) return "Zombie";
        String name = type.name().replace("ZOMBOTANY_", "Zombotany ")
            .replace("TUTORIAL_", "").replace("MUMMY_", "").replace("ICEAGE_", "").
            replace('_', ' ').toLowerCase();
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
        if (map == null || getGame() == null) return;
        // Do NOT call super.initializeBoard() to avoid spawning stationary AI sun zombies
        plants.clear();
        if (gameStatus != null) {
            gameStatus.setSunflower(plantSun);
        }
        for (int r = 0; r < getGame().getRows(); r++) {
            for (int c = 0; c < getGame().getCols(); c++) {
                map.setPlant(r, c, null);
            }
        }

        // Layout Plant Bar Vertically on the LEFT (brought forward onto pathway)
        float slotSize = 120f;
        float gap = 12f;
        float leftX = 240f;
        float startY = 1180f;
        plantBar.layoutVertical(randomPlantOptions, leftX, startY, slotSize, gap);

        // Layout Zombie Bar Vertically on the RIGHT (brought forward away from pause button)
        if (getGame() != null) {
            float rightX = 2210f;
            zombiePacketBar.layoutVertical(getGame(), rightX, 1180f, 125f, 14f);
        }
    }

    private float skySunTimer = 0f;

    @Override
    public void update(float delta) {
        animTime += delta;
        if (matchFinished) {return;}
        super.update(delta);
        if (gameStatus != null) {gameStatus.setSunflower(plantSun);}
        skySunTimer += delta;
        if (skySunTimer >= 4.5f && map != null) {
            skySunTimer = 0f;
            float dropX = map.getStartX() + (float)(Math.random() * (map.getCols() * map.getTileWidth()));
            float startY = 1400f;
            float groundY = map.getStartY() - (float)(Math.random() * (map.getRows() * map.getTileHeight()));
            sunManager.spawnFalling(dropX, startY, 50, groundY, com.PVZ.model.entity.Sun.SunType.NORMAL, 75.0);}
        if (inputProcessor instanceof IZombieInputProcessor izInp) {
            collectZombieSunAtTile(izInp.getSelectedRow(), izInp.getSelectedCol());}
        if (!matchFinished && getGame() != null) {
            if (getGame().getBrainsRemaining() <= 0) {
                matchFinished = true;
                plantsWon = false;
                matchResultText = "ALL BRAINS EATEN - ZOMBIE PLAYER WINS!";
            } else {
                matchTimeRemaining -= delta;
                if (matchTimeRemaining <= 0f) {
                    matchTimeRemaining = 0f;
                    matchFinished = true;
                    plantsWon = true;
                    matchResultText = "TIME UP - PLANTS SURVIVED! PLAYER 1 WINS!";}}}
        if (reactionCooldown > 0f) {
            reactionCooldown -= delta;}
        if (!activeStickers.isEmpty()) {
            java.util.Iterator<LocalStickerEffect> it = activeStickers.iterator();
            while (it.hasNext()) {
                LocalStickerEffect fx = it.next();
                fx.elapsed += delta;
                if (fx.elapsed >= fx.lifetime) {
                    it.remove();}}}
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    /** True when the Plants (Player 1) side won the match. */
    public boolean isWonMatch() {
        return plantsWon;
    }

    public boolean isDrawResult() {
        return false;
    }

    public String getMatchResultText() {
        return matchResultText;
    }

    public float getMatchTimeRemaining() {
        return matchTimeRemaining;
    }

    public float getMatchDuration() {
        return MATCH_DURATION;
    }

    public int collectZombieSunAtTile(int row, int col) {
        if (map == null || getGame() == null) return 0;
        float tw = map.getTileWidth();
        float th = map.getTileHeight();
        float tileX = map.getStartX() + col * tw;
        float tileY = map.getStartY() - (row + 1) * th;
        com.badlogic.gdx.math.Rectangle tileRect =
            new com.badlogic.gdx.math.Rectangle(tileX - 15f, tileY - 15f, tw + 30f, th + 30f);
        int collected = sunManager.collectAt(tileRect);
        if (collected > 0) {
            getGame().addSun(collected);
        }
        return collected;
    }

    @Override
    public int collectSunAtWorldPoint(float worldX, float worldY) {
        com.badlogic.gdx.math.Rectangle pointer =
            new com.badlogic.gdx.math.Rectangle(worldX - 12f, worldY - 12f, 24f, 24f);
        int collected = sunManager.collectAt(pointer);
        if (collected > 0) {
            plantSun += collected;
            if (gameStatus != null) {
                gameStatus.setSunflower(plantSun);
            }
        }
        return collected;
    }

    public boolean plantByPlayer(PlantType type, int row, int col) {
        if (map == null || getGame() == null || type == null) return false;
        if (col >= getGame().getRedLineCol()) return false;
        if (map.getPlantAt(row, col) != null) return false;

        Plant plant = PlantFactory.createPlant(type, 1);
        if (plant == null) return false;

        int cost = (plant.getStats() != null)
            ? plant.getStats().getCost()
            : (plant.getInstance() != null && plant.getInstance().getDefinition() != null
                ? plant.getInstance().getDefinition().getCost() : 100);

        if (plantSun < cost) {
            return false;
        }
        plantSun -= cost;
        if (gameStatus != null) {
            gameStatus.setSunflower(plantSun);
        }

        map.setPlant(row, col, plant);
        plants.add(plant);
        return true;
    }

    public SeedPacketBar getPlantBar() { return plantBar; }
    public PlantType getSelectedPlantType() { return selectedPlantType; }
    public void setSelectedPlantType(PlantType selectedPlantType) { this.selectedPlantType = selectedPlantType; }
    public int getPlantSun() { return plantSun; }
    public void addPlantSun(int amount) {
        this.plantSun = Math.max(0, this.plantSun + amount);
        if (gameStatus != null) {
            gameStatus.setSunflower(plantSun);
        }
    }

    protected void drawHud(SpriteBatch batch) {
        if (getGame() == null || map == null) return;
        ensureTexturesLoaded();

        batch.begin();

        IZombieInputProcessor izInput = (inputProcessor instanceof IZombieInputProcessor inp) ? inp : null;
        String selectedAlias = izInput != null ? izInput.getSelectedAlias() : null;

        drawPlayerBars(batch, izInput, selectedAlias);
        drawZombieCursor(batch, izInput);
        drawHoverPlantIndicator(batch);
        drawZombieSunBank(batch);
        drawInstructionBanner(batch, font, tinyFont);
        drawMatchResultOverlay(batch);
        drawActiveStickers(batch);

        batch.end();
    }

    private void drawPlayerBars(SpriteBatch batch, IZombieInputProcessor izInput, String selectedAlias) {
        plantBar.drawIconsAndLabels(batch, FontManager.getInstance().getEnglishMenuFont(), null, selectedPlantType);
        zombiePacketBar.draw(batch, font, tinyFont, getGame(), selectedAlias);
    }

    private void drawZombieCursor(SpriteBatch batch, IZombieInputProcessor izInput) {
        if (izInput == null || hudPixel == null) return;

        int row = izInput.getSelectedRow();
        int col = Math.max(0, Math.min(getGame().getCols() - 1, izInput.getSelectedCol()));
        float th = map.getTileHeight();
        float tw = map.getTileWidth();
        float rowY = map.getStartY() - (row + 1) * th;
        float deployX = map.getStartX() + col * tw;

        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.004) * 0.06 + 0.22);
        boolean isSpawnable = col > getGame().getRedLineCol();

        if (isSpawnable) {
            batch.setColor(1f, 0.92f, 0.5f, pulse);
            batch.draw(hudPixel, deployX + 2f, rowY + 2f, tw - 4f, th - 4f);
            batch.setColor(1f, 0.92f, 0.5f, 0.85f);
        } else {
            batch.setColor(0.3f, 0.8f, 1f, pulse * 0.8f);
            batch.draw(hudPixel, deployX + 2f, rowY + 2f, tw - 4f, th - 4f);
            batch.setColor(0.4f, 0.9f, 1f, 0.85f);
        }
        batch.draw(hudPixel, deployX + 2f, rowY + 2f, tw - 4f, 2f);
        batch.draw(hudPixel, deployX + 2f, rowY + th - 4f, tw - 4f, 2f);
        batch.draw(hudPixel, deployX + 2f, rowY + 2f, 2f, th - 4f);
        batch.draw(hudPixel, deployX + tw - 4f, rowY + 2f, 2f, th - 4f);
        batch.setColor(Color.WHITE);
    }

    private void drawHoverPlantIndicator(SpriteBatch batch) {
        if (selectedPlantType == null || hudPixel == null || map == null) return;

        com.badlogic.gdx.graphics.OrthographicCamera cam = com.PVZ.model.status.AppStatus.getCamera();
        if (cam == null) return;

        com.badlogic.gdx.math.Vector3 world =
            cam.unproject(new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);

        if (!map.isWithinBounds(row, col) || col >= getGame().getRedLineCol()) return;

        float th = map.getTileHeight();
        float tw = map.getTileWidth();
        float tileY = map.getStartY() - (row + 1) * th;
        float tileX = map.getStartX() + col * tw;
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.08 + 0.25);

        batch.setColor(0.2f, 1f, 0.3f, pulse);
        batch.draw(hudPixel, tileX + 2f, tileY + 2f, tw - 4f, th - 4f);
        batch.setColor(0.3f, 1f, 0.4f, 0.8f);
        batch.draw(hudPixel, tileX + 2f, tileY + 2f, tw - 4f, 2f);
        batch.draw(hudPixel, tileX + 2f, tileY + th - 4f, tw - 4f, 2f);
        batch.draw(hudPixel, tileX + 2f, tileY + 2f, 2f, th - 4f);
        batch.draw(hudPixel, tileX + tw - 4f, tileY + 2f, 2f, th - 4f);
        batch.setColor(Color.WHITE);
    }

    private void drawZombieSunBank(SpriteBatch batch) {
        float panelHeight = 84f;
        float iconSize = 74f;
        float panelWidth = iconSize + 100f;
        float bankX = 2185f;
        float bankY = 1440f - panelHeight - 30f;

        batch.setColor(1f, 1f, 1f, 1f);
        getZombiePanelBackground().draw(batch, bankX, bankY, panelWidth, panelHeight);

        float iconCx = bankX + iconSize / 2f + 6f;
        float iconCy = bankY + panelHeight / 2f;
        EntityRenderer.getInstance().renderPam(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", animTime, iconCx, iconCy);

        BitmapFont headerFont = FontManager.getInstance().getEnglishMenuFont();
        headerFont.setColor(Color.GOLD);
        String zSunText = String.valueOf(getGame().getSun());
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
            new com.badlogic.gdx.graphics.g2d.GlyphLayout(headerFont, zSunText);
        float textX = bankX + iconSize + 14f;
        float textY = bankY + panelHeight / 2f + layout.height / 2f;
        headerFont.draw(batch, zSunText, textX, textY);
    }

    private void drawInstructionBanner(SpriteBatch batch, BitmapFont smallFont, BitmapFont tinyFont) {
        float bannerW = 860f;
        float bannerH = 44f;
        float bannerX = (2560f - bannerW) / 2f;
        float bannerY = 1330f;

        if (hudPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.70f);
            batch.draw(hudPixel, bannerX, bannerY, bannerW, bannerH);
            batch.setColor(1f, 1f, 1f, 0.35f);
            batch.draw(hudPixel, bannerX, bannerY, bannerW, 1.5f);
            batch.draw(hudPixel, bannerX, bannerY + bannerH - 1.5f, bannerW, 1.5f);
            batch.setColor(Color.WHITE);
        }
        BitmapFont fontToUse = smallFont != null ? smallFont : tinyFont;
        fontToUse.setColor(Color.WHITE);
        String bannerText = "P1 [MOUSE]: Left Bar to Plant  |  P2 [KEYBOARD]: 1-8 / WASD / Space";
        fontToUse.draw(batch, bannerText, bannerX + 30f, bannerY + 30f);
    }

    private void drawMatchResultOverlay(SpriteBatch batch) {
        if (!matchFinished) return;

        if (hudPixel != null) {
            batch.setColor(0f, 0f, 0f, 0.75f);
            batch.draw(hudPixel, 0, 0, 2560f, 1440f);
            batch.setColor(Color.WHITE);
        }
        BitmapFont headerFont = FontManager.getInstance().getEnglishMenuFont();
        headerFont.setColor(Color.YELLOW);
        headerFont.draw(batch, matchResultText, 2560f / 2f - 250f, 1440f / 2f + 20f);
    }

    private void drawActiveStickers(SpriteBatch batch) {
        if (activeStickers.isEmpty()) return;

        for (LocalStickerEffect fx : activeStickers) {
            EntityRenderer.getInstance().renderPam(batch, fx.path, fx.clip, fx.elapsed, fx.x - 128f, fx.y - 128f);
        }
    }

}
