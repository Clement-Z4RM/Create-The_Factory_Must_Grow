package com.drmangotea.createindustry.items.weapons.quad_potato_cannon;

import com.drmangotea.createindustry.CreateTFMGClient;
import com.simibubi.create.content.equipment.zapper.ShootGadgetPacket;
import com.simibubi.create.content.equipment.zapper.ShootableGadgetRenderHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class QuadPotatoCannonPacket extends ShootGadgetPacket {

	private float pitch;
	private Vec3 motion;
	private ItemStack item;

	public QuadPotatoCannonPacket(Vec3 location, Vec3 motion, ItemStack item, InteractionHand hand, float pitch, boolean self) {
		super(location, hand, self);
		this.motion = motion;
		this.item = item;
		this.pitch = pitch;
	}

	public QuadPotatoCannonPacket(FriendlyByteBuf buffer) {
		super(buffer);
	}

	@Override
	protected void readAdditional(FriendlyByteBuf buffer) {
		pitch = buffer.readFloat();
		motion = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		item = buffer.readItem();
	}

	@Override
	protected void writeAdditional(FriendlyByteBuf buffer) {
		buffer.writeFloat(pitch);
		buffer.writeFloat((float) motion.x);
		buffer.writeFloat((float) motion.y);
		buffer.writeFloat((float) motion.z);
		buffer.writeItem(item);
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void handleAdditional() {
		CreateTFMGClient.QUAD_POTATO_CANNON_RENDER_HANDLER.beforeShoot(pitch, location, motion, item);
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected ShootableGadgetRenderHandler getHandler() {
		return CreateTFMGClient.QUAD_POTATO_CANNON_RENDER_HANDLER;
	}

}