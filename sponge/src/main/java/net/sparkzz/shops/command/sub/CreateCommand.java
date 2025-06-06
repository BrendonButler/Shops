package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Cuboid;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
import java.util.stream.DoubleStream;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Create subcommand used for creating a shop
 *
 * @author Brendon Butler
 */
public class CreateCommand extends SubCommand {

    private static final Parameter.Value<String> name = Parameter.string().key("name").build();
    private static final Parameter.Value<ServerPlayer> target = Parameter.player().key("owner").optional().build();
    private static final Parameter.Value<ServerLocation> startingPoint = Parameter.location().key("start-point").optional().build();
    private static final Parameter.Value<Vector3d> endingPoint = Parameter.vector3d().key("end-point").optional().build();

    public CommandResult execute(@NotNull CommandContext context) throws NumberFormatException {
        resetAttributes();
        // TODO: new permission to limit a player to a number of shops (shops.create.<quantity>)
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        String storeName = context.requireOne(name);

        int shopsOwned = (int) Store.STORES.stream().filter(s -> player.uniqueId().equals(s.getOwner())).count();

        Optional<ServerPlayer> storeOwner = context.one(target);
        Optional<ServerPlayer> owner = Optional.of(player);
        setAttribute("target", owner.get().name());
        Optional<ServerLocation> cuboidStart = context.one(startingPoint);
        Optional<ServerWorld> storeWorld = cuboidStart.map(ServerLocation::world);
        Optional<Vector3d> cuboidEnd = storeWorld.isPresent() ? Optional.of(context.requireOne(endingPoint)) : context.one(endingPoint);

        if (storeOwner.isPresent()) {
            if (!context.subject().hasPermission("shops.create.other-player")) {
                Notifier.process(context.cause(), NO_PERMS_CREATE_OTHER, getAttributes());
                return CommandResult.success();
            }

            shopsOwned = (int) Store.STORES.stream().filter(s -> storeOwner.get().uniqueId().equals(s.getOwner())).count();
            setAttribute("target", storeOwner.get().name());

            owner = storeOwner;
        }

        if (owner.isEmpty()) {
            Notifier.process(context.cause(), PLAYER_NOT_FOUND, getAttributes());
            return CommandResult.success();
        }

        if (shopsOwned >= setAttribute("max-stores", Config.getMaxOwnedStores())) {
            Notifier.process(context.cause(), STORE_CREATE_FAIL_MAX_STORES, getAttributes());
            return CommandResult.success();
        }

        double x1, y1, z1, x2, y2, z2;
        x1 = y1 = z1 = x2 = y2 = z2 = 0D;

        if (cuboidStart.isPresent() && cuboidEnd.isPresent()) {
            Vector3d start = cuboidStart.get().position();
            Vector3d end = cuboidEnd.get();

            x1 = start.x();
            y1 = start.y();
            z1 = start.z();
            x2 = end.x();
            y2 = end.y();
            z2 = end.z();
        }

        Store store;

        if (DoubleStream.of(x1, y1, z1, x2, y2, z2).allMatch(value -> value == 0D))
            store = new Store(storeName, owner.orElseThrow().uniqueId());
        else {
            double minX = setAttribute("min-x", Math.min(x1, x2));
            double maxX = setAttribute("max-x", Math.max(x1, x2));
            double minY = setAttribute("min-y", Math.min(y1, y2));
            double maxY = setAttribute("max-y", Math.max(y1, y2));
            double minZ = setAttribute("min-z", Math.min(z1, z2));
            double maxZ = setAttribute("max-z", Math.max(z1, z2));
            double[] minDims = Config.getMinDimensions();
            double[] maxDims = Config.getMaxDimensions();
            double limitMinX = setAttribute("limit-min-x", minDims[0]);
            double limitMinY = setAttribute("limit-min-y", minDims[1]);
            double limitMinZ = setAttribute("limit-min-z", minDims[2]);
            double limitMaxX = setAttribute("limit-max-x", maxDims[0]);
            double limitMaxY = setAttribute("limit-max-y", maxDims[1]);
            double limitMaxZ = setAttribute("limit-max-z", maxDims[2]);

            if ((maxX - minX) < limitMinX || (maxY - minY) < limitMinY || (maxZ - minZ) < limitMinZ) {
                Notifier.process(context.cause(), STORE_CREATE_FAIL_MIN_DIMS, getAttributes());
                return CommandResult.success();
            }

            if ((limitMaxX > 0 && (maxX - minX) > limitMaxX) || (limitMaxY > 0 && (maxY - minY) > limitMaxY) || (limitMaxZ > 0 && (maxZ - minZ) > limitMaxZ)) {
                Notifier.process(context.cause(), STORE_CREATE_FAIL_MAX_DIMS, getAttributes());
                return CommandResult.success();
            }

            double volume = setAttribute("volume", (maxX - minX) * (maxY - minY) * (maxZ - minZ));
            double minVolume = setAttribute("limit-min-vol", Config.getMinVolume());
            double maxVolume = setAttribute("limit-max-vol", Config.getMaxVolume());

            if (volume < minVolume) {
                Notifier.process(context.cause(), STORE_CREATE_FAIL_MIN_VOL, getAttributes());
                return CommandResult.success();
            }

            if (maxVolume > 0 && volume > maxVolume) {
                Notifier.process(context.cause(), STORE_CREATE_FAIL_MAX_VOL, getAttributes());
                return CommandResult.success();
            }

            Cuboid cuboid = new Cuboid(storeWorld.orElse(player.world()), x1, y1, z1, x2, y2, z2);

            for (Cuboid currentCuboid : Config.getOffLimitsCuboids()) {
                if (cuboid.intersects(currentCuboid) || currentCuboid.intersects(cuboid)) {
                    Notifier.process(context.cause(), STORE_CREATE_FAIL_OFFLIMITS, getAttributes());
                    return CommandResult.success();
                }
            }

            for (Cuboid currentCuboid : Store.STORES.stream().map(Store::getCuboidLocation).toList()) {
                if (currentCuboid != null && (cuboid.intersects(currentCuboid) || currentCuboid.intersects(cuboid))) {
                    Notifier.process(context.cause(), STORE_CREATE_FAIL_OVERLAPS, getAttributes());
                    return CommandResult.success();
                }
            }

            store = new Store(storeName, owner.orElseThrow().uniqueId(), cuboid);
        }

        setAttribute("store", store.getName());

        if (owner.orElseThrow().uniqueId().equals((player).uniqueId()))
            Notifier.process(context.cause(), STORE_CREATE_SUCCESS, getAttributes());
        else Notifier.process(context.cause(), STORE_CREATE_SUCCESS_OTHER_PLAYER, getAttributes());
        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        final Command.Parameterized customizeStoreBuild = Command.builder()
                .executor(new CreateCommand())
                .addParameters(target, startingPoint, endingPoint)
                .build();

        return Command.builder()
                .executor(new CreateCommand())
                .permission("shops.cmd.create")
                .shortDescription(Component.text("Allows a player to create stores"))
                .extendedDescription(Component.text("Create a store for players"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addParameters(name)
                .addChild(customizeStoreBuild, "name")
                .build();
    }
}