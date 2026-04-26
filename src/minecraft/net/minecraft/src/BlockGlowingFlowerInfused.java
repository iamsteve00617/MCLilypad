package net.minecraft.src;

public class BlockGlowingFlowerInfused extends BlockGlowing {
	public int power = 0;

	protected BlockGlowingFlowerInfused(int i1, int i2, int i3, int i4) {
		super(i1, i2, i3);
		this.power = i4;
	}

	public void onEntityCollidedWithBlock(World world1, int i2, int i3, int i4, Entity entity5) {
		if(entity5 instanceof EntityMob) {
			((EntityMob)entity5).attackEntityFrom((Entity)null, 2 * this.power);
			((EntityMob)entity5).fire = 300;
		}

	}
}
