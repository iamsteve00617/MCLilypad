package net.minecraft.src;

import java.util.Random;

public class BlockGreenscreen extends Block {
	public BlockGreenscreen(int id, int tex) {
		super(id, tex, Material.rock);
	}

	public int idDropped(int i1, Random random2) {
		return Block.greenscreen.blockID;
	}
	
	public boolean isOpaqueCube() {
		return true;
	}
}
