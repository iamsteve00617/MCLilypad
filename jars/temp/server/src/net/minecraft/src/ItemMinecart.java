package net.minecraft.src;

public class ItemMinecart extends Item {
	public int minecartType;

	public ItemMinecart(int itemID, int minecartType) {
		super(itemID);
		this.maxStackSize = 1;
		this.minecartType = minecartType;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
		int i8 = world.getBlockId(x, y, z);
		if(i8 == Block.minecartTrack.blockID) {
			world.spawnEntityInWorld(new EntityMinecart(world, (double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), this.minecartType));
			--stack.stackSize;
			return true;
		} else {
			return false;
		}
	}
}
