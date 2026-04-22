package net.minecraft.src;

public class ItemPainting extends Item {
	public ItemPainting(int i1) {
		super(i1);
		this.maxDamage = 64;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer entityPlayer, World world, int x, int y, int z, int i7) {
		if(i7 == 0) {
			return false;
		} else if(i7 == 1) {
			return false;
		} else {
			byte b8 = 0;
			if(i7 == 4) {
				b8 = 1;
			}

			if(i7 == 3) {
				b8 = 2;
			}

			if(i7 == 5) {
				b8 = 3;
			}

			EntityPainting entityPainting9 = new EntityPainting(world, x, y, z, b8);
			if(entityPainting9.onValidSurface()) {
				world.spawnEntityInWorld(entityPainting9);
				--stack.stackSize;
			}

			return true;
		}
	}
}
