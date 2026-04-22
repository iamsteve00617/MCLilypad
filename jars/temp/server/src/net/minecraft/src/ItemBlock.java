package net.minecraft.src;

public class ItemBlock extends Item {
	private int blockID;

	public ItemBlock(int i1) {
		super(i1);
		this.blockID = i1 + 256;
		this.setIconIndex(Block.blockList[i1 + 256].getBlockTextureFromSide(2));
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
		if(world.getBlockId(x, y, z) == Block.snow.blockID) {
			i7 = 0;
		} else {
			if(i7 == 0) {
				--y;
			}

			if(i7 == 1) {
				++y;
			}

			if(i7 == 2) {
				--z;
			}

			if(i7 == 3) {
				++z;
			}

			if(i7 == 4) {
				--x;
			}

			if(i7 == 5) {
				++x;
			}
		}

		if(stack.stackSize == 0) {
			return false;
		} else {
			if(world.canBlockBePlacedAt(this.blockID, x, y, z, false)) {
				Block block8 = Block.blockList[this.blockID];
				if(world.setBlockWithNotify(x, y, z, this.blockID)) {
					Block.blockList[this.blockID].onBlockPlaced(world, x, y, z, i7);
					world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), block8.stepSound.getStepSound(), (block8.stepSound.getVolume() + 1.0F) / 2.0F, block8.stepSound.getPitch() * 0.8F);
					--stack.stackSize;
				}
			}

			return true;
		}
	}
}
