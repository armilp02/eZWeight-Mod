package com.armilp.ezweight.events;

import com.armilp.ezweight.EZWeight;
import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.registry.ModAttributes;
import com.armilp.ezweight.registry.ModEffects;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import org.joml.Vector3d;

@EventBusSubscriber(modid = EZWeight.MODID)
public class SableWeightHandler {

    private static final boolean SABLE_PRESENT =
            ModList.get().isLoaded("sable");

    private static final boolean CREATE_PRESENT =
            ModList.get().isLoaded("create");

    private static final boolean AERONAUTICS_PRESENT =
            ModList.get().isLoaded("aeronautics_bundled");

    private static final double LIGHT_ENCUMBERED_FORCE = WeightConfig.COMMON.LIGHT_ENCUMBERED_FORCE.get();
    private static final double ENCUMBERED_FORCE = WeightConfig.COMMON.ENCUMBERED_FORCE.get();
    private static final double HEAVILY_ENCUMBERED_FORCE = WeightConfig.COMMON.HEAVILY_ENCUMBERED_FORCE.get();
    private static final double OVERBURDENED_FORCE = WeightConfig.COMMON.OVERBURDENED_FORCE.get();
    private static final double CRUSHED_FORCE = WeightConfig.COMMON.CRUSHED_FORCE.get();

    private static final double MAX_UPWARD_VELOCITY = 1.0;
    private static final double MAX_DOWNWARD_VELOCITY = -2.0;

    static {
        if (SABLE_PRESENT && CREATE_PRESENT && AERONAUTICS_PRESENT) {
            EZWeight.LOGGER.info(
                    "Sable/Create/Aeronautics detected. Ship weight integration enabled."
            );
        } else {
            EZWeight.LOGGER.info(
                    "Ship weight integration disabled. Sable={}, Create={}, Aeronautics={}",
                    SABLE_PRESENT,
                    CREATE_PRESENT,
                    AERONAUTICS_PRESENT
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!SABLE_PRESENT || !CREATE_PRESENT || !AERONAUTICS_PRESENT) {
            return;
        }

        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        MobEffectInstance crushed =
                player.getEffect(ModEffects.CRUSHED);

        MobEffectInstance overburdened =
                player.getEffect(ModEffects.OVERBURDENED);

        MobEffectInstance heavilyEncumbered =
                player.getEffect(ModEffects.HEAVILY_ENCUMBERED);

        MobEffectInstance encumbered =
                player.getEffect(ModEffects.ENCUMBERED);

        MobEffectInstance lightEncumbered =
                player.getEffect(ModEffects.LIGHT_ENCUMBERED);

        if (crushed == null &&
                overburdened == null &&
                heavilyEncumbered == null &&
                encumbered == null &&
                lightEncumbered == null) {
            return;
        }

        SubLevel trackingSubLevel =
                Sable.HELPER.getTrackingOrVehicleSubLevel(player);

        if (!(trackingSubLevel instanceof ServerSubLevel serverSubLevel)) {
            return;
        }

        RigidBodyHandle handle =
                RigidBodyHandle.of(serverSubLevel);

        if (handle == null || !handle.isValid()) {
            return;
        }

        double playerWeight =
                player.getAttributeValue(ModAttributes.WEIGHT);

        double baseForce;
        int amplifier;

        if (crushed != null) {
            baseForce = CRUSHED_FORCE;
            amplifier = crushed.getAmplifier();
        } else if (overburdened != null) {
            baseForce = OVERBURDENED_FORCE;
            amplifier = overburdened.getAmplifier();
        } else if (heavilyEncumbered != null) {
            baseForce = HEAVILY_ENCUMBERED_FORCE;
            amplifier = heavilyEncumbered.getAmplifier();
        } else if (encumbered != null) {
            baseForce = ENCUMBERED_FORCE;
            amplifier = encumbered.getAmplifier();
        } else {
            baseForce = LIGHT_ENCUMBERED_FORCE;
            amplifier = lightEncumbered.getAmplifier();
        }

        double ampMultiplier =
                1.0 + (amplifier * 0.5);

        double force =
                playerWeight * baseForce * ampMultiplier;

        double impulse =
                force / 20.0;

        Vector3d worldImpulse =
                new Vector3d(0.0, -impulse, 0.0);

        Vector3d localImpulse =
                new Vector3d(worldImpulse);

        serverSubLevel.logicalPose()
                .transformNormalInverse(localImpulse);

        handle.applyLinearAndAngularImpulse(
                localImpulse,
                new Vector3d(0.0, 0.0, 0.0),
                true
        );

        Vector3d currentVelocity =
                new Vector3d();

        handle.getLinearVelocity(currentVelocity);

        if (currentVelocity.y > MAX_UPWARD_VELOCITY) {

            double correction =
                    MAX_UPWARD_VELOCITY - currentVelocity.y;

            handle.addLinearAndAngularVelocity(
                    new Vector3d(0.0, correction, 0.0),
                    new Vector3d(0.0, 0.0, 0.0)
            );

        } else if (currentVelocity.y < MAX_DOWNWARD_VELOCITY) {

            double correction =
                    MAX_DOWNWARD_VELOCITY - currentVelocity.y;

            handle.addLinearAndAngularVelocity(
                    new Vector3d(0.0, correction, 0.0),
                    new Vector3d(0.0, 0.0, 0.0)
            );
        }
    }
}