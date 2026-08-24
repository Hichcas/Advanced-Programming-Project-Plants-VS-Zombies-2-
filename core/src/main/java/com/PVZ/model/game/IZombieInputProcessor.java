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

    @Override
    public boolean keyDown(int keycode) {
        if (engine == null || engine.getGame() == null) return false;

        // Keys 1 to 8: Select card by index (top row and numpad)
        int index = -1;
        if (keycode >= com.badlogic.gdx.Input.Keys.NUM_1 && keycode <= com.badlogic.gdx.Input.Keys.NUM_8) {
            index = keycode - com.badlogic.gdx.Input.Keys.NUM_1;
        } else if (keycode >= com.badlogic.gdx.Input.Keys.NUMPAD_1 && keycode <= com.badlogic.gdx.Input.Keys.NUMPAD_8) {
            index = keycode - com.badlogic.gdx.Input.Keys.NUMPAD_1;
        }

        if (index >= 0) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine && "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                java.util.List<SeedPacket> packets = multiEngine.getPlantBar().getPackets();
                if (index < packets.size()) {
                    multiEngine.setSelectedPlantType(packets.get(index).getPlantType());
                    System.out.println("[IZombie] Selected plant: " + packets.get(index).getPlantType());
                    return true;
                }
            } else {
                java.util.List<ZombiePacket> packets = engine.getZombiePacketBar().getPackets();
                if (index < packets.size()) {
                    selectZombie(packets.get(index).getOption().getAlias());
                    System.out.println("[IZombie] Selected zombie: " + packets.get(index).getOption().getDisplayName());
                    return true;
                }
            }
        }

        // Keys W / UP: Move target row up
        if (keycode == com.badlogic.gdx.Input.Keys.W || keycode == com.badlogic.gdx.Input.Keys.UP) {
            selectedRow = Math.max(0, selectedRow - 1);
            System.out.println("[IZombie] Target row: " + selectedRow);
            return true;
        }

        // Keys S / DOWN: Move target row down
        if (keycode == com.badlogic.gdx.Input.Keys.S || keycode == com.badlogic.gdx.Input.Keys.DOWN) {
            selectedRow = Math.min(engine.getGame().getRows() - 1, selectedRow + 1);
            System.out.println("[IZombie] Target row: " + selectedRow);
            return true;
        }

        // Keys A / LEFT: Move target col left
        if (keycode == com.badlogic.gdx.Input.Keys.A || keycode == com.badlogic.gdx.Input.Keys.LEFT) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine && "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                selectedCol = Math.max(0, selectedCol - 1);
            } else {
                int minZombieCol = engine.getGame().getRedLineCol() + 1;
                selectedCol = Math.max(minZombieCol, selectedCol - 1);
            }
            System.out.println("[IZombie] Target col: " + selectedCol);
            return true;
        }

        // Keys D / RIGHT: Move target col right
        if (keycode == com.badlogic.gdx.Input.Keys.D || keycode == com.badlogic.gdx.Input.Keys.RIGHT) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine && "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                selectedCol = Math.min(engine.getGame().getRedLineCol() - 1, selectedCol + 1);
            } else {
                int maxZombieCol = engine.getGame().getCols() - 1;
                int minZombieCol = engine.getGame().getRedLineCol() + 1;
                selectedCol = Math.min(maxZombieCol, Math.max(minZombieCol, selectedCol + 1));
            }
            System.out.println("[IZombie] Target col: " + selectedCol);
            return true;
        }

        // Space / Enter: Deploy / Plant
        if (keycode == com.badlogic.gdx.Input.Keys.SPACE || keycode == com.badlogic.gdx.Input.Keys.ENTER) {
            if (engine instanceof IZombieMultiplayerGameEngine multiEngine && "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
                if (multiEngine.getSelectedPlantType() != null) {
                    boolean success = multiEngine.plantByPlayer(multiEngine.getSelectedPlantType(), selectedRow, selectedCol);
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

        // 1. Plant Player Interaction:
        if (engine instanceof IZombieMultiplayerGameEngine multiEngine && "PLANT".equalsIgnoreCase(multiEngine.getMyRole())) {
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

            if (engine.getMap() != null && multiEngine.getSelectedPlantType() != null) {
                Map map = engine.getMap();
                int row = map.worldToRow(world.y);
                int col = map.worldToCol(world.x);
                if (map.isWithinBounds(row, col)) {
                    boolean success = multiEngine.plantByPlayer(multiEngine.getSelectedPlantType(), row, col);
                    if (success) {
                        multiEngine.setSelectedPlantType(null);
                    }
                    return true;
                }
            }
            return false;
        }

        // 2. Zombie Player Interaction:
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
