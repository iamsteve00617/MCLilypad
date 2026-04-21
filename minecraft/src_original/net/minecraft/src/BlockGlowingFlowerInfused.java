package net.minecraft.src;

public class BlockGlowingFlowerInfused extends BlockGlowing {
	public int power = 0;

	protected BlockGlowingFlowerInfused(int var1, int var2, int var3, int var4) {
		super(var1, var2, var3);
		this.power = var4;
	}

	public void onEntityCollidedWithBlock(World var1, int var2, int var3, int var4, Entity var5) {
		if(var5 instanceof EntityMob) {
			((EntityMob)var5).attackEntityFrom((Entity)null, 2 * this.power);
			((EntityMob)var5).fire = 300;
		}

	}
}
