package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Iris;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BlockUtils;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
/* We use this to process the bounding boxes collided around the player for our checks to use as utils */
public class CollisionAssessment {
    private PlayerData data;
    private boolean onGround, fullyInAir, inLiquid, blocksOnTop, pistonsNear, onHalfBlock, onClimbable, onIce, collidesHorizontally, inWeb, onSlime;
    private Set<Material> materialsCollided;
    private BoundingBox playerBox;

    public CollisionAssessment(BoundingBox playerBox, PlayerData data) {
        onGround = inLiquid = blocksOnTop = pistonsNear = onHalfBlock = onClimbable = onIce = collidesHorizontally = inWeb = onSlime = false;
        fullyInAir = true;
        this.data = data;
        this.playerBox = playerBox;
        materialsCollided = new HashSet<>();
    }

    public void assessBox(BoundingBox bb, World world, boolean isEntity) {
        Location location = bb.getMinimum().toLocation(world);
        Block block = BlockUtils.getBlock(location);

        if (BlockUtils.isSolid(block) || isEntity) {
            if ((bb.getMaximum().getY()) <= playerBox.getMinimum().getY() + 0.3 && bb.intersectsWithBox(playerBox.subtract(0, 0.0001f, 0, 0, 1.4f, 0))) {
                onGround = true;
            }

            if ((bb.getMinimum().getY() + 0.3) >= playerBox.getMaximum().getY() && bb.intersectsWithBox(playerBox.add(0, 1.5f, 0, 0, 0.35f, 0))) {
                blocksOnTop = true;
            }

            if(getData().isDebuggingBox() && bb.collides(playerBox) && Iris.getInstance().getCurrentTicks() % 2 == 0) {
                //getData().getPlayer().sendMessage(block.getType().toString() + ": " + block.getType().getId() + " id");
                Atlas.getInstance().getSchedular().submit(() -> MiscUtils.createParticlesForBoundingBox(getData().getPlayer(), bb, WrappedEnumParticle.FLAME, 0.25f));
            }

            if (BlockUtils.isPiston(block)) {
                pistonsNear = true;
            }

            if (BlockUtils.isSlab(block) || BlockUtils.isStair(block) || block.getType().getId() == 92 || block.getType().getId() == 397) {
                onHalfBlock = true;
            }

            if (BlockUtils.isIce(block) && playerBox.subtract(0, 0.5f, 0, 0, 0, 0).intersectsWithBox(bb)) {
                onIce = true;
            }

            if (bb.collidesHorizontally(playerBox.grow((ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0), 0, (ProtocolVersion.getGameVersion().isOrAbove(ProtocolVersion.V1_13) ? 0.05f : 0)))) {
                collidesHorizontally = true;

            }

            if(bb.collidesVertically(playerBox.subtract(0, 0.1f,0,0,0,0)) && block.getType().toString().contains("SLIME")) {
                onSlime = true;
            }

            Location loc = new Location(data.getPlayer().getWorld(), data.getMovementProcessor().getTo().getX(), data.getBoundingBox().minY, data.getMovementProcessor().getTo().getZ());

            Block footBlock = BlockUtils.getBlock(loc);

            if (BlockUtils.isClimbableBlock(footBlock) && (getData().getMovementProcessor().getDeltaY() < 0 || collidesHorizontally)) {
                onClimbable = true;
            }
        } else {
            if (BlockUtils.isLiquid(block) && playerBox.collidesVertically(bb)) {
                inLiquid = true;
            }
            if (block.getType().toString().contains("WEB") && playerBox.collidesVertically(bb)) {
                inWeb = true;
            }
        }

        addMaterial(location.getBlock());
    }

    private String getId() {
        return "%%__USER__%%" + "%%__USER__%%";
    }

    private void addMaterial(Block block) {
        materialsCollided.add(block.getType());
    }
}
