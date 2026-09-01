package com.PVZ.model.game;

import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

public class IZombieInputProcessor extends InputAdapter {

    private IZombieGameEngine engine;
    private String selectedAlias;
    private int selectedRow = 2; // Default to row 2 (0..4)
    private int selectedCol = 0;

    public void setEngine(IZombieGameEngine engine) {
        this.engine = engine;
        if (engine != null && engine.getGame() != null) {
            if (engine instanceof IZombieMultiplayerGameEngine multi && "PLANT".equalsIgnoreCase(multi.getMyRole())) {
                this.selectedCol = 0;
            } else {
                this.selectedCol = engine.getGame().getRedLineCol() + 1;
            }
        }
    }

    public int getSelectedRow() { return selectedRow; }
    public void setSelectedRow(int selectedRow) { this.selectedRow = selectedRow; }

    public int getSelectedCol() { return selectedCol; }
    public void setSelectedCol(int selectedCol) { this.selectedCol = selectedCol; }

    public void selectZombie(String alias) {
        this.selectedAlias = (selectedAlias != null && selectedAlias.equalsIgnoreCase(alias)) ? null : alias;
    }

    public String getSelectedAlias() { return selectedAlias; }

    public void checkZombieSunCollect() {
        if (engine instanceof IZombieLocalVersusEngine versusEngine) {
            versusEngine.collectZombieSunAtTile(selectedRow, selectedCol);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (engine == null || engine.getGame() == null) return false;

        if (handleNumberKeys(keycode)) return true;
        if (handleMovementKeys(keycode)) return true;
        if (handleActionKeys(keycode)) return true;
        return false;
    }

    private boolean handleNumberKeys(int keycode) {
        int index = -1;
        if (keycode >= com.badlogic.gdx.Input.Keys.NUM_1 &&
            keycode <= com.badlogic.gdx.Input.Keys.NUM_8) {
            index = keycode - com.badlogic.gdx.Input.Keys.NUM_1;
        } else if (keycode >= com.badlogic.gdx.Input.Keys.NUMPAD_1 &&
            keycode <= com.badlogic.gdx.Input.Keys.NUMPAD_8) {
            index = keycode - com.badlogic.gdx.Input.Keys.NUMPAD_1;
        }

        if (index >= 0) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine &&
                "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                java.util.List<SeedPacket> packets = multiEngine.getPlantBar().getPackets();
                if (index < packets.size()) {
                    multiEngine.setSelectedPlantType(packets.get(index).getPlantType());
                    System.out.println("[IZombie] Selected plant: " +
                        packets.get(index).getPlantType());
                    return true;
                }
            } else {
                java.util.List<ZombiePacket> packets = engine.getZombiePacketBar().getPackets();
                if (index < packets.size()) {
                    selectZombie(packets.get(index).getOption().getAlias());
                    System.out.println("[IZombie] Selected zombie: " +
                        packets.get(index).getOption().getDisplayName());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleMovementKeys(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.W ||
            keycode == com.badlogic.gdx.Input.Keys.UP) {
            selectedRow = Math.max(0, selectedRow - 1);
            checkZombieSunCollect();
            return true;
        }

        if (keycode == com.badlogic.gdx.Input.Keys.S ||
            keycode == com.badlogic.gdx.Input.Keys.DOWN) {
            selectedRow = Math.min(engine.getGame().getRows() - 1, selectedRow + 1);
            checkZombieSunCollect();
            return true;
        }

        if (keycode == com.badlogic.gdx.Input.Keys.A ||
            keycode == com.badlogic.gdx.Input.Keys.LEFT) {
            selectedCol = Math.max(0, selectedCol - 1);
            checkZombieSunCollect();
            return true;
        }

        if (keycode == com.badlogic.gdx.Input.Keys.D ||
            keycode == com.badlogic.gdx.Input.Keys.RIGHT) {
            selectedCol = Math.min(engine.getGame().getCols() - 1, selectedCol + 1);
            checkZombieSunCollect();
            return true;
        }
        return false;
    }

    private boolean handleActionKeys(int keycode) {
        if (keycode == com.badlogic.gdx.Input.Keys.SPACE ||
            keycode == com.badlogic.gdx.Input.Keys.ENTER) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine &&
                "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                if (multiEngine.getSelectedPlantType() != null) {
                    boolean success = multiEngine.plantByPlayer(
                        multiEngine.getSelectedPlantType(), selectedRow, selectedCol);
                    if (success) {
                        multiEngine.setSelectedPlantType(null);
                    }
                    return true;
                }
            } else if (selectedAlias != null) {
                int minZombieCol = engine.getGame().getRedLineCol() + 1;
                int maxZombieCol = engine.getGame().getCols() - 1;
                int deployCol = Math.max(minZombieCol, Math.min(maxZombieCol, selectedCol));
                String result = engine.deployZombie(selectedAlias, selectedRow, deployCol);
                System.out.println("[IZombie] " + result);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (engine == null || engine.getGame() == null) return false;

        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

        if (engine instanceof IZombieLocalVersusEngine versusEngine) {
            return handleLocalVersusTouch(versusEngine, world);
        }

        if (engine instanceof IZombieMultiplayerGameEngine multiEngine &&
            "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
            return handleMultiplayerPlantTouch(multiEngine, world);
        }

        return handleZombieTouch(world);
    }

    private boolean handleLocalVersusTouch(IZombieLocalVersusEngine versusEngine, Vector3 world) {
        var packet = versusEngine.getPlantBar().getPacketAt(world.x, world.y);
        if (packet != null) {
            com.PVZ.model.enums.PlantType pt = packet.getPlantType();
            if (versusEngine.getSelectedPlantType() == pt) {
                versusEngine.setSelectedPlantType(null);
            } else {
                versusEngine.setSelectedPlantType(pt);
            }
            return true;
        }

        if (versusEngine.getMap() != null && versusEngine.getSelectedPlantType() != null) {
            Map map = versusEngine.getMap();
            boolean insideLawn = world.x >= map.getStartX()
                && world.x < map.getStartX() + map.getTotalWidth()
                && world.y <= map.getStartY()
                && world.y > map.getStartY() - map.getTotalHeight();
            if (insideLawn) {
                int row = map.worldToRow(world.y);
                int col = map.worldToCol(world.x);
                if (map.isWithinBounds(row, col) && col < versusEngine.getGame().getRedLineCol()) {
                    boolean success = versusEngine.plantByPlayer(
                        versusEngine.getSelectedPlantType(), row, col);
                    if (success) {
                        versusEngine.setSelectedPlantType(null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleMultiplayerPlantTouch(IZombieMultiplayerGameEngine multiEngine,
                                                Vector3 world) {
        var packet = multiEngine.getPlantBar().getPacketAt(world.x, world.y);
        if (packet != null) {
            com.PVZ.model.enums.PlantType pt = packet.getPlantType();
            if (multiEngine.getSelectedPlantType() == pt) {
                multiEngine.setSelectedPlantType(null);
            } else {
                multiEngine.setSelectedPlantType(pt);
            }
            return true;
        }

        if (multiEngine.getMap() != null && multiEngine.getSelectedPlantType() != null) {
            Map map = multiEngine.getMap();
            int row = map.worldToRow(world.y);
            int col = map.worldToCol(world.x);
            if (map.isWithinBounds(row, col)) {
                boolean success = multiEngine.plantByPlayer(
                    multiEngine.getSelectedPlantType(), row, col);
                if (success) {
                    multiEngine.setSelectedPlantType(null);
                }
                return true;
            }
        }
        return false;
    }

    private boolean handleZombieTouch(Vector3 world) {
        var packet = engine.getZombiePacketBar().getPacketAt(world.x, world.y);
        if (packet != null) {
            selectZombie(packet.getOption().getAlias());
            System.out.println("[IZombie] selected " + packet.getOption().getDisplayName());
            return true;
        }

        if (engine.getMap() == null || selectedAlias == null) return false;

        Map map = engine.getMap();
        int row = map.worldToRow(world.y);
        int col = map.worldToCol(world.x);
        if (!map.isWithinBounds(row, col)) return false;

        String result = engine.deployZombie(selectedAlias, row, col);
        System.out.println("[IZombie] " + result);
        return true;
    }

    // Sun is collected by sweeping the mouse over it (matches the classic PvZ feel
    // more closely than click-to-collect), not by clicking - mouseMoved fires
    // continuously while the button is up, and touchDragged covers the touchscreen/
    // button-held-down case, so both routes call the same collection point.
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        tryCollectSunAt(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        tryCollectSunAt(screenX, screenY);
        return false;
    }

    private void tryCollectSunAt(int screenX, int screenY) {
        if (engine == null || engine.getGame() == null) return;
        OrthographicCamera camera = AppStatus.getCamera();
        if (camera == null) return;
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        int collected = engine.collectSunAtWorldPoint(world.x, world.y);
        if (collected > 0) {
            System.out.println("[IZombie] collected " + collected + " sun");
        }
    }
}
