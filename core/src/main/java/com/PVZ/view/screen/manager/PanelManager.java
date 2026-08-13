package com.PVZ.view.screen.manager;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.PVZ.view.screen.panels.BasePanel;

import java.util.ArrayList;
import java.util.List;

public class PanelManager {
    private static PanelManager instance;

    private Stage stage;
    private BasePanel currentPanel;

    /**
     * Actors that must stay reachable no matter which full-screen panel is
     * currently showing (e.g. the floating cheat-vault button). Every panel
     * swap re-adds the panel on top of the actor stack, so without this these
     * actors would silently end up covered/unclickable behind the new panel.
     */
    private final List<Actor> persistentOverlays = new ArrayList<>();

    private PanelManager() {
    }

    public static PanelManager getInstance() {
        if (instance == null) {
            instance = new PanelManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.stage = stage;
    }

    /**
     * Registers an actor (already added to the stage) that should keep
     * floating above every panel from now on, in every menu the panel
     * manager shows it in.
     */
    public void addPersistentOverlay(Actor overlay) {
        if (overlay != null && !persistentOverlays.contains(overlay)) {
            persistentOverlays.add(overlay);
        }
    }

    public void performPanelTransition(BasePanel newPanel) {
        if (stage == null) {
            return;
        }

        if (currentPanel != null) {
            currentPanel.remove();
            currentPanel.dispose();
            currentPanel = null;
        }

        currentPanel = newPanel;
        currentPanel.getColor().a = 1f;
        stage.addActor(currentPanel);

        // Bring persistent overlays back above the freshly added panel.
        for (Actor overlay : persistentOverlays) {
            if (overlay.getStage() != null) {
                overlay.toFront();
            }
        }
    }

    public void dispose() {
        if (currentPanel != null) {
            currentPanel.remove();
            currentPanel.dispose();
            currentPanel = null;
        }
        persistentOverlays.clear();
        stage = null;
        instance = null;
    }
}
