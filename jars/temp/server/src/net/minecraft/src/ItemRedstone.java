package net.minecraft.src;

public class ItemRedstone extends Item {
	public ItemRedstone(int i1) {
		super(i1);
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
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

		if(world.getBlockId(x, y, z) != 0) {
			return false;
		} else {
			if(Block.redstoneWire.canPlaceBlockAt(world, x, y, z)) {
				--stack.stackSize;
				world.setBlockWithNotify(x, y, z, Block.redstoneWire.blockID);
			}

			return true;
		}
	}
}
