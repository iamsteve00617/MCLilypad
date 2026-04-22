package net.minecraft.src;

public class ItemFlintAndSteel extends Item {
	public ItemFlintAndSteel(int i1) {
		super(i1);
		this.maxStackSize = 1;
		this.maxDamage = 64;
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

		int i8 = world.getBlockId(x, y, z);
		if(i8 == 0) {
			world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "fire.ignite", 1.0F, rand.nextFloat() * 0.4F + 0.8F);
			world.setBlockWithNotify(x, y, z, Block.fire.blockID);
		}

		stack.damageItem(1);
		return true;
	}
}
