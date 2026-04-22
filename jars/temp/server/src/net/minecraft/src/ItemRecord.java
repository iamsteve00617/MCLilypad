package net.minecraft.src;

public class ItemRecord extends Item {
	private String recordName;

	protected ItemRecord(int itemID, String recordName) {
		super(itemID);
		this.recordName = recordName;
		this.maxStackSize = 1;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
		if(world.getBlockId(x, y, z) == Block.jukebox.blockID && world.getBlockMetadata(x, y, z) == 0) {
			world.setBlockMetadataWithNotify(x, y, z, this.shiftedIndex - Item.record13.shiftedIndex + 1);
			world.playRecord(this.recordName, x, y, z);
			--stack.stackSize;
			return true;
		} else {
			return false;
		}
	}
}
