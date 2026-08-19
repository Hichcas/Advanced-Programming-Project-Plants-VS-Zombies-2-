package com.PVZ.model;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.game.GameStatus;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.util.GameInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ZombieFreezeLogicTest {

    private RegularGameEngine engine;

    @BeforeAll
    public static void initAll() throws Exception {
        GameInitialization.initialize();
    }

    @BeforeEach
    public void setup() {
        GameStatus status = new GameStatus();
        engine = new RegularGameEngine(status, new ArrayList<>());
        Map map = new Map(0f, 500f, 900f, 500f, 5, 9);
        engine.setMap(map);
    }

    @Test
    public void testZombieFreezeSolidAndThaw() {
        Zombie zombie = ZombieType.TUTORIAL_DEFAULT.create();
        zombie.initPosition(500, 200, 1);
        assertFalse(zombie.isFrozen(), "Zombie should initially not be frozen");

        // Freeze zombie for 3 seconds
        zombie.freeze(3.0f);
        assertTrue(zombie.isFrozen(), "Zombie must be frozen after freeze(3.0f)");
        assertEquals(0, zombie.getCurrentSpeed(), "Movement speed must be stopped when frozen");

        // Update 1.5s -> still frozen
        zombie.updateEffects(1.5f);
        assertTrue(zombie.isFrozen());

        // Update 2.0s -> total 3.5s > 3.0s -> should thaw
        zombie.updateEffects(2.0f);
        assertFalse(zombie.isFrozen(), "Zombie should thaw after freeze duration expires");
        assertTrue(zombie.getCurrentSpeed() > 0, "Zombie should resume movement");
    }

    @Test
    public void testFireDamageInstantlyThawsFrozenZombie() {
        Zombie zombie = ZombieType.TUTORIAL_DEFAULT.create();
        zombie.initPosition(500, 200, 1);
        zombie.freeze(5.0f);
        assertTrue(zombie.isFrozen());

        // Hit by fire damage -> instant thaw
        zombie.takeDamage(20, DamageType.FIRE);
        assertFalse(zombie.isFrozen(), "Fire damage must instantly thaw a frozen zombie");
    }

    @Test
    public void testFrozenZombieTakesDamageFromProjectiles() {
        Zombie zombie = ZombieType.TUTORIAL_DEFAULT.create();
        zombie.initPosition(500, 200, 1);
        zombie.freeze(5.0f);
        assertTrue(zombie.isFrozen());

        double initialHp = zombie.getHitpoints();
        zombie.takeDamage(20, DamageType.NORMAL);
        assertEquals(initialHp - 20, zombie.getHitpoints(), "Frozen zombie must take normal damage");

        zombie.takeDamage(30, DamageType.ICE);
        assertEquals(initialHp - 50, zombie.getHitpoints(), "Frozen zombie must take ice damage as well");
    }

    @Test
    public void testZombieAshPowderDisintegration() {
        Zombie zombie = ZombieType.TUTORIAL_DEFAULT.create();
        zombie.initPosition(500, 200, 1);
        assertEquals(com.PVZ.model.enums.DeathType.NORMAL, zombie.getDeathType());

        // Set ash death
        zombie.setDeathType(com.PVZ.model.enums.DeathType.ASH);
        assertEquals(com.PVZ.model.enums.DeathType.ASH, zombie.getDeathType());
        assertTrue(zombie.getAshPamPath(false).contains("ZOMBIE_ASH.PAM"));

        // Imp ash path check
        Zombie imp = ZombieType.IMP_TUTORIAL.create();
        assertTrue(imp.getAshPamPath(false).contains("ZOMBIE_IMP_ASH.PAM"));

        // Gargantuar ash path check
        Zombie garg = ZombieType.GARGANTUAR_BASIC.create();
        assertTrue(garg.getAshPamPath(false).contains("ZOMBIE_GARGANTUAR_ASH.PAM"));
    }

    @Test
    public void testFireDamageKillingBlowSetsAshDeath() {
        Zombie zombie = ZombieType.TUTORIAL_DEFAULT.create();
        zombie.initPosition(500, 200, 1);
        assertEquals(com.PVZ.model.enums.DeathType.NORMAL, zombie.getDeathType());

        // Fire damage that exceeds HP -> deathType becomes ASH
        zombie.takeDamage((int) zombie.getHitpoints() + 50, DamageType.FIRE);
        assertEquals(com.PVZ.model.enums.DeathType.ASH, zombie.getDeathType(), "Lethal fire damage must turn zombie to ASH");
    }

    @Test
    public void testFootballZombieKickAnimation() {
        Zombie allstar = ZombieType.ALLSTAR.create();
        assertTrue(allstar instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieAllStar);
        com.PVZ.model.entity.zombies.types.special_movement.ZombieAllStar football = (com.PVZ.model.entity.zombies.types.special_movement.ZombieAllStar) allstar;
        assertFalse(football.isTackled());
        assertFalse(football.isKicking());

        // Initial charge speed is fast
        assertTrue(football.getCurrentSpeed() > 0.4);
    }

    @Test
    public void testSnorkelZombieSubmergedMechanics() {
        Zombie snorkel = ZombieType.BEACH_SNORKEL.create();
        assertTrue(snorkel instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel);
        com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel s = (com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel) snorkel;

        assertTrue(s.isSubmerged(), "Snorkel Zombie should start submerged in water");
        assertTrue(s.isProjectileImmune(), "Submerged Snorkel Zombie must be immune to straight projectiles");

        // When surfacing to eat
        s.surface();
        assertFalse(s.isSubmerged(), "Snorkel Zombie is no longer submerged when on surface");
        assertFalse(s.isProjectileImmune(), "Surfaced Snorkel Zombie can take projectile hits");

        // Diving back down
        s.dive();
        assertTrue(s.isSubmerged());
        assertTrue(s.isProjectileImmune());
    }

    @Test
    public void testAllElevenZombiesExistAndHaveValidPamPaths() {
        String[] aliases = {
            "ZombieDarkJuggler",
            "ZombieWizard",
            "ZombieDarkKing",
            "ZombieDarkImpDragon",
            "ZombieModernAllStar",
            "ZombieArcade",
            "ZombieLostCityJane",
            "ZombieCrystalSkull",
            "ZombieProspector",
            "ZombiePiano",
            "ZombieNewspaper"
        };

        for (String alias : aliases) {
            ZombieType zt = ZombieType.fromAlias(alias);
            assertNotNull(zt, "ZombieType for " + alias + " must not be null");
            Zombie z = zt.create();
            assertNotNull(z, "Created zombie for " + alias + " must not be null");

            String pamPath = com.PVZ.model.entity.zombies.base.ZombieTexturePaths.getPamPath(alias);
            assertNotNull(pamPath, "PAM path for " + alias + " must not be null");
            assertNotEquals("768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM", pamPath,
                "PAM path for " + alias + " must not fall back to default Egypt Basic!");
        }
    }

    @Test
    public void testDarkJugglerSpinAndReflectionMechanic() {
        Zombie juggler = ZombieType.DARK_JUGGLER.create();
        assertTrue(juggler instanceof com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler);
        com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler jj =
            (com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler) juggler;

        assertFalse(jj.isSpinning());

        // Reflecting multiple projectiles (unlimited)
        for (int i = 0; i < 20; i++) {
            assertTrue(jj.reflectProjectile(), "Jester must reflect projectile #" + i);
        }
        assertTrue(jj.isSpinning(), "Jester must be in spinning state");

        // Simulation update while spinning: stays spinning and pauses movement
        jj.update(0.5f, null);
        assertTrue(jj.isSpinning());

        // After spin time expires without new hits: stops spinning and resumes walking
        jj.update(1.5f, null);
        assertFalse(jj.isSpinning());
    }
}
