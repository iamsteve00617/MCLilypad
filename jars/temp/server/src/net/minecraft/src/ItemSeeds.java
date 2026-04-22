package net.minecraft.src;

public class ItemSeeds extends Item {
	private int blockType;

	public ItemSeeds(int itemID, int blockType) {
		super(itemID);
		this.blockType = blockType;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
		if(i7 != 1) {
			return false;
		} else {
			int i8 = world.getBlockId(x, y, z);
			if(i8 == Block.tilledField.blockID) {
				world.setBlockWithNotify(x, y + 1, z, this.blockType);
				--stack.stackSize;
				return true;
			} else {
				return false;
			}
		}
	}
}
