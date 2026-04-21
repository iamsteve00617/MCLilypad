package net.minecraft.src;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.lwjgl.input.Keyboard;

public class World implements IBlockAccess {
	private List lightingToUpdate;
	public List loadedEntityList;
	private List unloadedEntityList;
	private TreeSet scheduledTickTreeSet;
	private Set scheduledTickSet;
	public List loadedTileEntityList;
	public long worldTime;
	public boolean snowCovered;
	private long skyColor;
	private long fogColor;
	private long cloudColor;
	public int skylightSubtracted;
	protected int updateLCG;
	protected int DIST_HASH_MAGIC;
	public boolean editingBlocks;
	public static float[] lightBrightnessTable = new float[16];
	private final long lockTimestamp;
	protected int autosavePeriod;
	public List playerEntities;
	public int difficultySetting;
	public Object fontRenderer;
	public Random rand;
	public int spawnX;
	public int spawnY;
	public int spawnZ;
	public boolean isNewWorld;
	protected List worldAccesses;
	private IChunkProvider chunkProvider;
	public File saveDirectory;
	public long randomSeed;
	private NBTTagCompound nbtCompoundPlayer;
	public long sizeOnDisk;
	public final String levelName;
	public boolean worldChunkLoadOverride;
	private ArrayList collidingBoundingBoxes;
	private Set positionsToUpdate;
	private int soundCounter;
	private List entitiesWithinAABBExcludingEntity;
	public boolean multiplayerWorld;
	public long milestone;
	public boolean exclFrailMode;
	public boolean checkedInputManager;
	public boolean hasInputManager;
	public boolean bossfightInProgress;
	public MobGiant bossRef;
	public String bossname;

	public boolean CanUseCheats() {
		if(!this.checkedInputManager) {
			try {
				Class.forName("InputHandler");
				this.hasInputManager = true;
			} catch (ClassNotFoundException var2) {
				this.hasInputManager = false;
			}

			this.checkedInputManager = true;
		}

		return this.hasInputManager ? InputHandler.cheatsEnabled : false;
	}

	public void CueSpawnBossFrom(int var1, int var2) {
		if(!this.multiplayerWorld && !this.bossfightInProgress) {
			int var3 = var1 + 64 * ((new Random()).nextInt(3) - 1);
			int var4 = var2 + 64 * ((new Random()).nextInt(3) - 1);
			if(var3 == var1 && var4 == var2) {
				var4 += 64;
			}

			String var5 = "Giant of " + GuiIngame.Namegen2(this.randomSeed, var3 / 32, var4 / 32);
			this.bossname = var5;
			this.bossRef = InputHandler.SpawnGiant((double)var3, 100.0D, (double)var4);
			System.out.println("spawned at " + var3 + ", " + var4 + ", health: " + this.bossRef.health);
			this.bossfightInProgress = true;
		}

	}

	public static NBTTagCompound getLevelData(File var0, String var1) {
		File var2 = new File(new File(var0, "saves"), var1);
		if(!var2.exists()) {
			return null;
		} else {
			File var3 = new File(var2, "level.dat");
			if(var3.exists()) {
				try {
					return ClassX.a((InputStream)(new FileInputStream(var3))).getCompoundTag("Data");
				} catch (Exception var5) {
					var5.printStackTrace();
				}
			}

			return null;
		}
	}

	public static void deleteWorld(File var0, String var1) {
		File var2 = new File(new File(var0, "saves"), var1);
		if(var2.exists()) {
			deleteWorldFiles(var2.listFiles());
			var2.delete();
		}
	}

	private static void deleteWorldFiles(File[] var0) {
		for(int var1 = 0; var1 < var0.length; ++var1) {
			if(var0[var1].isDirectory()) {
				deleteWorldFiles(var0[var1].listFiles());
			}

			var0[var1].delete();
		}

	}

	public World(File var1, String var2) {
		this(var1, var2, (new Random()).nextLong());
	}

	public World(String var1) {
		this.milestone = 0L;
		this.exclFrailMode = true;
		this.checkedInputManager = false;
		this.hasInputManager = false;
		this.bossfightInProgress = false;
		this.bossRef = null;
		this.bossname = "";
		this.lightingToUpdate = new ArrayList();
		this.loadedEntityList = new ArrayList();
		this.unloadedEntityList = new ArrayList();
		this.scheduledTickTreeSet = new TreeSet();
		this.scheduledTickSet = new HashSet();
		this.loadedTileEntityList = new ArrayList();
		this.worldTime = 0L;
		this.snowCovered = false;
		this.skyColor = 8961023L;
		this.fogColor = 12638463L;
		this.cloudColor = 16777215L;
		this.skylightSubtracted = 0;
		this.updateLCG = (new Random()).nextInt();
		this.DIST_HASH_MAGIC = 1013904223;
		this.editingBlocks = false;
		this.lockTimestamp = System.currentTimeMillis();
		this.autosavePeriod = 40;
		this.playerEntities = new ArrayList();
		this.rand = new Random();
		this.isNewWorld = false;
		this.worldAccesses = new ArrayList();
		this.randomSeed = 0L;
		this.sizeOnDisk = 0L;
		this.collidingBoundingBoxes = new ArrayList();
		this.positionsToUpdate = new HashSet();
		this.soundCounter = this.rand.nextInt(12000);
		this.entitiesWithinAABBExcludingEntity = new ArrayList();
		this.multiplayerWorld = false;
		this.levelName = var1;
		this.chunkProvider = this.getChunkProvider(this.saveDirectory);
		this.calculateInitialSkylight();
	}

	public World(File var1, String var2, long var3) {
		this.milestone = 0L;
		this.exclFrailMode = true;
		this.checkedInputManager = false;
		this.hasInputManager = false;
		this.bossfightInProgress = false;
		this.bossRef = null;
		this.bossname = "";
		this.lightingToUpdate = new ArrayList();
		this.loadedEntityList = new ArrayList();
		this.unloadedEntityList = new ArrayList();
		this.scheduledTickTreeSet = new TreeSet();
		this.scheduledTickSet = new HashSet();
		this.loadedTileEntityList = new ArrayList();
		this.worldTime = 0L;
		this.snowCovered = false;
		this.skyColor = 8961023L;
		this.fogColor = 12638463L;
		this.cloudColor = 16777215L;
		this.skylightSubtracted = 0;
		this.updateLCG = (new Random()).nextInt();
		this.DIST_HASH_MAGIC = 1013904223;
		this.editingBlocks = false;
		this.lockTimestamp = System.currentTimeMillis();
		this.autosavePeriod = 40;
		this.playerEntities = new ArrayList();
		this.rand = new Random();
		this.isNewWorld = false;
		this.worldAccesses = new ArrayList();
		this.randomSeed = 0L;
		this.sizeOnDisk = 0L;
		this.collidingBoundingBoxes = new ArrayList();
		this.positionsToUpdate = new HashSet();
		this.soundCounter = this.rand.nextInt(12000);
		this.entitiesWithinAABBExcludingEntity = new ArrayList();
		this.multiplayerWorld = false;
		this.levelName = var2;
		var1.mkdirs();
		(this.saveDirectory = new File(var1, var2)).mkdirs();

		try {
			DataOutputStream var5 = new DataOutputStream(new FileOutputStream(new File(this.saveDirectory, "session.lock")));

			try {
				var5.writeLong(this.lockTimestamp);
			} finally {
				var5.close();
			}
		} catch (IOException var12) {
			throw new RuntimeException("Failed to check session lock, aborting");
		}

		File var13 = new File(this.saveDirectory, "level.dat");
		this.isNewWorld = !var13.exists();
		if(var13.exists()) {
			try {
				NBTTagCompound var6 = ClassX.a((InputStream)(new FileInputStream(var13))).getCompoundTag("Data");
				this.randomSeed = var6.getLong("RandomSeed");
				this.spawnX = var6.getInteger("SpawnX");
				this.spawnY = var6.getInteger("SpawnY");
				this.spawnZ = var6.getInteger("SpawnZ");
				this.worldTime = var6.getLong("Time");
				this.sizeOnDisk = var6.getLong("SizeOnDisk");
				this.snowCovered = var6.getBoolean("SnowCovered");
				if(var6.hasKey("Player")) {
					this.nbtCompoundPlayer = var6.getCompoundTag("Player");
				}

				this.milestone = var6.getLong("Milestones");
				this.exclFrailMode = var6.getBoolean("ExclusivelyFrail");
				System.out.println("Current milestone: " + this.milestone);
			} catch (Exception var10) {
				var10.printStackTrace();
			}
		} else {
			this.snowCovered = this.rand.nextInt(4) == 0;
		}

		boolean var14 = false;
		if(this.randomSeed == 0L) {
			this.randomSeed = var3;
			var14 = true;
		}

		this.chunkProvider = this.getChunkProvider(this.saveDirectory);
		if(var14) {
			this.worldChunkLoadOverride = true;
			this.spawnX = 0;
			this.spawnY = 64;

			for(this.spawnZ = 0; !this.findSpawn(this.spawnX, this.spawnZ); this.spawnZ += this.rand.nextInt(64) - this.rand.nextInt(64)) {
				this.spawnX += this.rand.nextInt(64) - this.rand.nextInt(64);
			}

			this.worldChunkLoadOverride = false;
		}

		this.calculateInitialSkylight();
	}

	protected IChunkProvider getChunkProvider(File var1) {
		return new ChunkProviderLoadOrGenerate(this, new ChunkLoader(var1, true), new ChunkProviderGenerate(this, this.randomSeed));
	}

	public void setSpawnLocation() {
		if(this.spawnY <= 0) {
			this.spawnY = 64;
		}

		while(this.getFirstUncoveredBlock(this.spawnX, this.spawnZ) == 0) {
			this.spawnX += this.rand.nextInt(8) - this.rand.nextInt(8);
			this.spawnZ += this.rand.nextInt(8) - this.rand.nextInt(8);
		}

	}

	private boolean findSpawn(int var1, int var2) {
		return this.getFirstUncoveredBlock(var1, var2) == Block.sand.blockID;
	}

	private int getFirstUncoveredBlock(int var1, int var2) {
		int var3;
		for(var3 = 63; this.getBlockId(var1, var3 + 1, var2) != 0; ++var3) {
		}

		return this.getBlockId(var1, var3, var2);
	}

	public void spawnPlayerWithLoadedChunks(EntityPlayer var1) {
		try {
			if(this.nbtCompoundPlayer != null) {
				var1.readFromNBT(this.nbtCompoundPlayer);
				this.nbtCompoundPlayer = null;
			}

			this.spawnEntityInWorld(var1);
		} catch (Exception var3) {
			var3.printStackTrace();
		}

	}

	public void saveWorld(boolean var1, IProgressUpdate var2) {
		if(this.chunkProvider.canSave()) {
			if(var2 != null) {
				var2.displayProgressMessage("Saving level");
			}

			this.saveLevel();
			if(var2 != null) {
				var2.displayLoadingString("Saving chunks");
			}

			this.chunkProvider.saveChunks(var1, var2);
		}
	}

	private void saveLevel() {
		this.checkSessionLock();
		NBTTagCompound var1 = new NBTTagCompound();
		var1.setLong("RandomSeed", this.randomSeed);
		var1.setInteger("SpawnX", this.spawnX);
		var1.setInteger("SpawnY", this.spawnY);
		var1.setInteger("SpawnZ", this.spawnZ);
		var1.setLong("Time", this.worldTime);
		var1.setLong("SizeOnDisk", this.sizeOnDisk);
		var1.setBoolean("SnowCovered", this.snowCovered);
		var1.setLong("LastPlayed", System.currentTimeMillis());
		var1.setLong("Milestones", this.milestone);
		var1.setBoolean("ExclusivelyFrail", this.exclFrailMode);
		Entity var2 = null;
		if(this.playerEntities.size() > 0) {
			var2 = (Entity)this.playerEntities.get(0);
		}

		NBTTagCompound var3;
		if(var2 != null) {
			var3 = new NBTTagCompound();
			var2.writeToNBT(var3);
			var1.setCompoundTag("Player", var3);
		}

		var3 = new NBTTagCompound();
		var3.setTag("Data", var1);

		try {
			File var4 = new File(this.saveDirectory, "level.dat_new");
			File var5 = new File(this.saveDirectory, "level.dat_old");
			File var6 = new File(this.saveDirectory, "level.dat");
			ClassX.a(var3, (OutputStream)(new FileOutputStream(var4)));
			if(var5.exists()) {
				var5.delete();
			}

			var6.renameTo(var5);
			if(var6.exists()) {
				var6.delete();
			}

			var4.renameTo(var6);
			if(var4.exists()) {
				var4.delete();
			}
		} catch (Exception var7) {
			var7.printStackTrace();
		}

	}

	public boolean saveWorld(int var1) {
		if(!this.chunkProvider.canSave()) {
			return true;
		} else {
			if(var1 == 0) {
				this.saveLevel();
			}

			return this.chunkProvider.saveChunks(false, (IProgressUpdate)null);
		}
	}

	public int getBlockId(int var1, int var2, int var3) {
		return var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000 ? (var2 < 0 ? 0 : (var2 >= 128 ? 0 : this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4).getBlockID(var1 & 15, var2, var3 & 15))) : 0;
	}

	public boolean blockExists(int var1, int var2, int var3) {
		return var2 >= 0 && var2 < 128 && this.chunkExists(var1 >> 4, var3 >> 4);
	}

	public boolean checkChunksExist(int var1, int var2, int var3, int var4, int var5, int var6) {
		if(var5 >= 0 && var2 < 128) {
			var1 >>= 4;
			var2 >>= 4;
			var3 >>= 4;
			var4 >>= 4;
			var5 >>= 4;
			var6 >>= 4;

			for(int var7 = var1; var7 <= var4; ++var7) {
				for(int var8 = var3; var8 <= var6; ++var8) {
					if(!this.chunkExists(var7, var8)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean chunkExists(int var1, int var2) {
		return this.chunkProvider.chunkExists(var1, var2);
	}

	public Chunk getChunkFromBlockCoords(int var1, int var2) {
		return this.getChunkFromChunkCoords(var1 >> 4, var2 >> 4);
	}

	public Chunk getChunkFromChunkCoords(int var1, int var2) {
		return this.chunkProvider.provideChunk(var1, var2);
	}

	public boolean setBlockAndMetadata(int var1, int var2, int var3, int var4, int var5) {
		return var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000 && var2 >= 0 && var2 < 128 && this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4).setBlockIDWithMetadata(var1 & 15, var2, var3 & 15, var4, var5);
	}

	public boolean setBlock(int var1, int var2, int var3, int var4) {
		return var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000 && var2 >= 0 && var2 < 128 && this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4).setBlockID(var1 & 15, var2, var3 & 15, var4);
	}

	public Material getBlockMaterial(int var1, int var2, int var3) {
		int var4 = this.getBlockId(var1, var2, var3);
		return var4 == 0 ? Material.air : Block.blocksList[var4].material;
	}

	public int getBlockMetadata(int var1, int var2, int var3) {
		if(var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
			if(var2 < 0) {
				return 0;
			} else if(var2 >= 128) {
				return 0;
			} else {
				Chunk var4 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
				var1 &= 15;
				var3 &= 15;
				return var4.getBlockMetadata(var1, var2, var3);
			}
		} else {
			return 0;
		}
	}

	public void setBlockMetadataWithNotify(int var1, int var2, int var3, int var4) {
		this.setBlockMetadata(var1, var2, var3, var4);
	}

	public boolean setBlockMetadata(int var1, int var2, int var3, int var4) {
		if(var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
			if(var2 < 0) {
				return false;
			} else if(var2 >= 128) {
				return false;
			} else {
				Chunk var5 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
				var1 &= 15;
				var3 &= 15;
				var5.setBlockMetadata(var1, var2, var3, var4);
				return true;
			}
		} else {
			return false;
		}
	}

	public boolean setBlockWithNotify(int var1, int var2, int var3, int var4) {
		if(this.setBlock(var1, var2, var3, var4)) {
			this.notifyBlockChange(var1, var2, var3, var4);
			return true;
		} else {
			return false;
		}
	}

	public boolean setBlockAndMetadataWithNotify(int var1, int var2, int var3, int var4, int var5) {
		return this.bStage2(var1, var2, var3, var4, var5);
	}

	public boolean bStage2(int var1, int var2, int var3, int var4, int var5) {
		if(this.setBlockAndMetadata(var1, var2, var3, var4, var5)) {
			this.notifyBlockChange(var1, var2, var3, var4);
			return true;
		} else {
			return false;
		}
	}

	public void markBlockNeedsUpdate(int var1, int var2, int var3) {
		for(int var4 = 0; var4 < this.worldAccesses.size(); ++var4) {
			((IWorldAccess)this.worldAccesses.get(var4)).markBlockAndNeighborsNeedsUpdate(var1, var2, var3);
		}

	}

	protected void notifyBlockChange(int var1, int var2, int var3, int var4) {
		this.markBlockNeedsUpdate(var1, var2, var3);
		this.notifyBlocksOfNeighborChange(var1, var2, var3, var4);
	}

	public void markBlocksDirtyVertical(int var1, int var2, int var3, int var4) {
		if(var3 > var4) {
			int var5 = var4;
			var4 = var3;
			var3 = var5;
		}

		this.markBlocksDirty(var1, var3, var2, var1, var4, var2);
	}

	public void markBlocksDirty(int var1, int var2, int var3, int var4, int var5, int var6) {
		for(int var7 = 0; var7 < this.worldAccesses.size(); ++var7) {
			((IWorldAccess)this.worldAccesses.get(var7)).markBlockRangeNeedsUpdate(var1, var2, var3, var4, var5, var6);
		}

	}

	public void notifyBlocksOfNeighborChange(int var1, int var2, int var3, int var4) {
		this.notifyBlockOfNeighborChange(var1 - 1, var2, var3, var4);
		this.notifyBlockOfNeighborChange(var1 + 1, var2, var3, var4);
		this.notifyBlockOfNeighborChange(var1, var2 - 1, var3, var4);
		this.notifyBlockOfNeighborChange(var1, var2 + 1, var3, var4);
		this.notifyBlockOfNeighborChange(var1, var2, var3 - 1, var4);
		this.notifyBlockOfNeighborChange(var1, var2, var3 + 1, var4);
	}

	private void notifyBlockOfNeighborChange(int var1, int var2, int var3, int var4) {
		if(!this.editingBlocks && !this.multiplayerWorld) {
			Block var5 = Block.blocksList[this.getBlockId(var1, var2, var3)];
			if(var5 != null) {
				var5.onNeighborBlockChange(this, var1, var2, var3, var4);
			}

		}
	}

	public boolean canBlockSeeTheSky(int var1, int var2, int var3) {
		return this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4).canBlockSeeTheSky(var1 & 15, var2, var3 & 15);
	}

	public int getBlockLightValue(int var1, int var2, int var3) {
		return this.getBlockLightValue_do(var1, var2, var3, true);
	}

	public int getBlockLightValue_do(int var1, int var2, int var3, boolean var4) {
		if(var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
			int var5;
			if(var4) {
				var5 = this.getBlockId(var1, var2, var3);
				if(var5 == Block.stairSingle.blockID || var5 == Block.tilledField.blockID) {
					int var6 = this.getBlockLightValue_do(var1, var2 + 1, var3, false);
					int var7 = this.getBlockLightValue_do(var1 + 1, var2, var3, false);
					int var8 = this.getBlockLightValue_do(var1 - 1, var2, var3, false);
					int var9 = this.getBlockLightValue_do(var1, var2, var3 + 1, false);
					int var10 = this.getBlockLightValue_do(var1, var2, var3 - 1, false);
					if(var7 > var6) {
						var6 = var7;
					}

					if(var8 > var6) {
						var6 = var8;
					}

					if(var9 > var6) {
						var6 = var9;
					}

					if(var10 > var6) {
						var6 = var10;
					}

					return var6;
				}
			}

			if(var2 < 0) {
				return 0;
			} else if(var2 >= 128) {
				var5 = 15 - this.skylightSubtracted;
				if(var5 < 0) {
					var5 = 0;
				}

				return var5;
			} else {
				Chunk var11 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
				var1 &= 15;
				var3 &= 15;
				return var11.getBlockLightValue(var1, var2, var3, this.skylightSubtracted);
			}
		} else {
			return 15;
		}
	}

	public boolean canExistingBlockSeeTheSky(int var1, int var2, int var3) {
		if(var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
			if(var2 < 0) {
				return false;
			} else if(var2 >= 128) {
				return true;
			} else if(!this.chunkExists(var1 >> 4, var3 >> 4)) {
				return false;
			} else {
				Chunk var4 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
				var1 &= 15;
				var3 &= 15;
				return var4.canBlockSeeTheSky(var1, var2, var3);
			}
		} else {
			return false;
		}
	}

	public int getHeightValue(int var1, int var2) {
		return var1 >= -32000000 && var2 >= -32000000 && var1 < 32000000 && var2 <= 32000000 ? (!this.chunkExists(var1 >> 4, var2 >> 4) ? 0 : this.getChunkFromChunkCoords(var1 >> 4, var2 >> 4).getHeightValue(var1 & 15, var2 & 15)) : 0;
	}

	public void neighborLightPropagationChanged(EnumSkyBlock var1, int var2, int var3, int var4, int var5) {
		this.aStage3(var1, var2, var3, var4, var5);
	}

	public void aStage3(EnumSkyBlock var1, int var2, int var3, int var4, int var5) {
		if(this.blockExists(var2, var3, var4)) {
			if(var1 == EnumSkyBlock.Sky) {
				if(this.canExistingBlockSeeTheSky(var2, var3, var4)) {
					var5 = 15;
				}
			} else if(var1 == EnumSkyBlock.Block) {
				int var6 = this.getBlockId(var2, var3, var4);
				if(Block.lightValue[var6] > var5) {
					var5 = Block.lightValue[var6];
				}
			}

			if(this.getSavedLightValue(var1, var2, var3, var4) != var5) {
				this.scheduleLightingUpdate(var1, var2, var3, var4, var2, var3, var4);
			}

		}
	}

	public int getSavedLightValue(EnumSkyBlock var1, int var2, int var3, int var4) {
		if(var3 >= 0 && var3 < 128 && var2 >= -32000000 && var4 >= -32000000 && var2 < 32000000 && var4 <= 32000000) {
			int var5 = var2 >> 4;
			int var6 = var4 >> 4;
			return !this.chunkExists(var5, var6) ? 0 : this.getChunkFromChunkCoords(var5, var6).getSavedLightValue(var1, var2 & 15, var3, var4 & 15);
		} else {
			return var1.defaultLightValue;
		}
	}

	public void setLightValue(EnumSkyBlock var1, int var2, int var3, int var4, int var5) {
		if(var2 >= -32000000 && var4 >= -32000000 && var2 < 32000000 && var4 <= 32000000) {
			if(var3 >= 0) {
				if(var3 < 128) {
					if(this.chunkExists(var2 >> 4, var4 >> 4)) {
						this.getChunkFromChunkCoords(var2 >> 4, var4 >> 4).setLightValue(var1, var2 & 15, var3, var4 & 15, var5);

						for(int var6 = 0; var6 < this.worldAccesses.size(); ++var6) {
							((IWorldAccess)this.worldAccesses.get(var6)).markBlockAndNeighborsNeedsUpdate(var2, var3, var4);
						}

					}
				}
			}
		}
	}

	public float getBrightness(int var1, int var2, int var3) {
		return lightBrightnessTable[this.getBlockLightValue(var1, var2, var3)];
	}

	public boolean isDaytime() {
		return this.skylightSubtracted < 4;
	}

	public MovingObjectPosition rayTraceBlocks(Vec3D var1, Vec3D var2) {
		return this.rayTraceBlocks_do(var1, var2, false);
	}

	public MovingObjectPosition rayTraceBlocks_do(Vec3D var1, Vec3D var2, boolean var3) {
		if(!Double.isNaN(var1.xCoord) && !Double.isNaN(var1.yCoord) && !Double.isNaN(var1.zCoord)) {
			if(!Double.isNaN(var2.xCoord) && !Double.isNaN(var2.yCoord) && !Double.isNaN(var2.zCoord)) {
				int var4 = MathHelper.floor_double(var2.xCoord);
				int var5 = MathHelper.floor_double(var2.yCoord);
				int var6 = MathHelper.floor_double(var2.zCoord);
				int var7 = MathHelper.floor_double(var1.xCoord);
				int var8 = MathHelper.floor_double(var1.yCoord);
				int var9 = MathHelper.floor_double(var1.zCoord);
				int var10 = 20;

				while(var10-- >= 0) {
					if(Double.isNaN(var1.xCoord) || Double.isNaN(var1.yCoord) || Double.isNaN(var1.zCoord)) {
						return null;
					}

					if(var7 == var4 && var8 == var5 && var9 == var6) {
						return null;
					}

					double var11 = 999.0D;
					double var13 = 999.0D;
					double var15 = 999.0D;
					if(var4 > var7) {
						var11 = (double)var7 + 1.0D;
					}

					if(var4 < var7) {
						var11 = (double)var7 + 0.0D;
					}

					if(var5 > var8) {
						var13 = (double)var8 + 1.0D;
					}

					if(var5 < var8) {
						var13 = (double)var8 + 0.0D;
					}

					if(var6 > var9) {
						var15 = (double)var9 + 1.0D;
					}

					if(var6 < var9) {
						var15 = (double)var9 + 0.0D;
					}

					double var17 = 999.0D;
					double var19 = 999.0D;
					double var21 = 999.0D;
					double var23 = var2.xCoord - var1.xCoord;
					double var25 = var2.yCoord - var1.yCoord;
					double var27 = var2.zCoord - var1.zCoord;
					if(var11 != 999.0D) {
						var17 = (var11 - var1.xCoord) / var23;
					}

					if(var13 != 999.0D) {
						var19 = (var13 - var1.yCoord) / var25;
					}

					if(var15 != 999.0D) {
						var21 = (var15 - var1.zCoord) / var27;
					}

					byte var29;
					if(var17 < var19 && var17 < var21) {
						if(var4 > var7) {
							var29 = 4;
						} else {
							var29 = 5;
						}

						var1.xCoord = var11;
						var1.yCoord += var25 * var17;
						var1.zCoord += var27 * var17;
					} else if(var19 < var21) {
						if(var5 > var8) {
							var29 = 0;
						} else {
							var29 = 1;
						}

						var1.xCoord += var23 * var19;
						var1.yCoord = var13;
						var1.zCoord += var27 * var19;
					} else {
						if(var6 > var9) {
							var29 = 2;
						} else {
							var29 = 3;
						}

						var1.xCoord += var23 * var21;
						var1.yCoord += var25 * var21;
						var1.zCoord = var15;
					}

					Vec3D var30 = Vec3D.createVector(var1.xCoord, var1.yCoord, var1.zCoord);
					double var32 = (double)MathHelper.floor_double(var1.xCoord);
					var30.xCoord = var32;
					var7 = (int)var32;
					if(var29 == 5) {
						--var7;
						++var30.xCoord;
					}

					double var35 = (double)MathHelper.floor_double(var1.yCoord);
					var30.yCoord = var35;
					var8 = (int)var35;
					if(var29 == 1) {
						--var8;
						++var30.yCoord;
					}

					double var38 = (double)MathHelper.floor_double(var1.zCoord);
					var30.zCoord = var38;
					var9 = (int)var38;
					if(var29 == 3) {
						--var9;
						++var30.zCoord;
					}

					int var40 = this.getBlockId(var7, var8, var9);
					int var41 = this.getBlockMetadata(var7, var8, var9);
					Block var42 = Block.blocksList[var40];
					if(var40 > 0 && var42.canCollideCheck(var41, var3)) {
						MovingObjectPosition var43 = var42.collisionRayTrace(this, var7, var8, var9, var1, var2);
						if(var43 != null) {
							return var43;
						}
					}
				}

				return null;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void playSoundAtEntity(Entity var1, String var2, float var3, float var4) {
		for(int var5 = 0; var5 < this.worldAccesses.size(); ++var5) {
			((IWorldAccess)this.worldAccesses.get(var5)).playSound(var2, var1.posX, var1.posY - (double)var1.yOffset, var1.posZ, var3, var4);
		}

	}

	public void playSoundEffect(double var1, double var3, double var5, String var7, float var8, float var9) {
		for(int var10 = 0; var10 < this.worldAccesses.size(); ++var10) {
			((IWorldAccess)this.worldAccesses.get(var10)).playSound(var7, var1, var3, var5, var8, var9);
		}

	}

	public void playRecord(String var1, int var2, int var3, int var4) {
		for(int var5 = 0; var5 < this.worldAccesses.size(); ++var5) {
			((IWorldAccess)this.worldAccesses.get(var5)).playRecord(var1, var2, var3, var4);
		}

	}

	public void spawnParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
		for(int var14 = 0; var14 < this.worldAccesses.size(); ++var14) {
			((IWorldAccess)this.worldAccesses.get(var14)).spawnParticle(var1, var2, var4, var6, var8, var10, var12);
		}

	}

	public boolean spawnEntityInWorld(Entity var1) {
		int var2 = MathHelper.floor_double(var1.posX / 16.0D);
		int var3 = MathHelper.floor_double(var1.posZ / 16.0D);
		boolean var4 = false;
		if(var1 instanceof EntityPlayer) {
			var4 = true;
		}

		if(!var4 && !this.chunkExists(var2, var3)) {
			return false;
		} else {
			if(var1 instanceof EntityPlayer) {
				this.playerEntities.add(var1);
				System.out.println("Player count: " + this.playerEntities.size());
			}

			this.getChunkFromChunkCoords(var2, var3).addEntity(var1);
			this.loadedEntityList.add(var1);
			this.obtainEntitySkin(var1);
			return true;
		}
	}

	protected void obtainEntitySkin(Entity var1) {
		for(int var2 = 0; var2 < this.worldAccesses.size(); ++var2) {
			((IWorldAccess)this.worldAccesses.get(var2)).obtainEntitySkin(var1);
		}

	}

	protected void releaseEntitySkin(Entity var1) {
		for(int var2 = 0; var2 < this.worldAccesses.size(); ++var2) {
			((IWorldAccess)this.worldAccesses.get(var2)).releaseEntitySkin(var1);
		}

	}

	public void setEntityDead(Entity var1) {
		var1.setEntityDead();
		if(var1 instanceof EntityPlayer) {
			this.playerEntities.remove(var1);
			System.out.println("Player count: " + this.playerEntities.size());
		}

	}

	public void addWorldAccess(IWorldAccess var1) {
		this.worldAccesses.add(var1);
	}

	public void removeWorldAccess(IWorldAccess var1) {
		this.worldAccesses.remove(var1);
	}

	public List getCollidingBoundingBoxes(Entity var1, AxisAlignedBB var2) {
		this.collidingBoundingBoxes.clear();
		int var3 = MathHelper.floor_double(var2.minX);
		int var4 = MathHelper.floor_double(var2.maxX + 1.0D);
		int var5 = MathHelper.floor_double(var2.minY);
		int var6 = MathHelper.floor_double(var2.maxY + 1.0D);
		int var7 = MathHelper.floor_double(var2.minZ);
		int var8 = MathHelper.floor_double(var2.maxZ + 1.0D);

		for(int var9 = var3; var9 < var4; ++var9) {
			for(int var10 = var7; var10 < var8; ++var10) {
				if(this.blockExists(var9, 64, var10)) {
					for(int var11 = var5 - 1; var11 < var6; ++var11) {
						Block var12 = Block.blocksList[this.getBlockId(var9, var11, var10)];
						if(var12 != null) {
							var12.getCollidingBoundingBoxes(this, var9, var11, var10, var2, this.collidingBoundingBoxes);
						}
					}
				}
			}
		}

		List var15 = this.getEntitiesWithinAABBExcludingEntity(var1, var2.expand(0.25D, 0.25D, 0.25D));

		for(int var16 = 0; var16 < var15.size(); ++var16) {
			AxisAlignedBB var13 = ((Entity)var15.get(var16)).getBoundingBox();
			if(var13 != null && var13.intersectsWith(var2)) {
				this.collidingBoundingBoxes.add(var13);
			}

			AxisAlignedBB var14 = var1.getCollisionBox((Entity)var15.get(var16));
			if(var14 != null && var14.intersectsWith(var2)) {
				this.collidingBoundingBoxes.add(var14);
			}
		}

		return this.collidingBoundingBoxes;
	}

	public int calculateSkylightSubtracted(float var1) {
		float var2 = 1.0F - (MathHelper.cos(this.getCelestialAngle(var1) * (float)Math.PI * 2.0F) * 2.0F + 0.5F);
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		return (int)(var2 * 11.0F);
	}

	public Vec3D getSkyColor(float var1) {
		float var2 = MathHelper.cos(this.getCelestialAngle(var1) * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		return Vec3D.createVector((double)((float)(this.skyColor >> 16 & 255L) / 255.0F * var2), (double)((float)(this.skyColor >> 8 & 255L) / 255.0F * var2), (double)((float)(this.skyColor & 255L) / 255.0F * var2));
	}

	public float getCelestialAngle(float var1) {
		float var2 = ((float)((int)(this.worldTime % 24000L)) + var1) / 24000.0F - 0.25F;
		if(var2 < 0.0F) {
			++var2;
		}

		if(var2 > 1.0F) {
			--var2;
		}

		return var2 + (1.0F - (float)((Math.cos((double)var2 * Math.PI) + 1.0D) / 2.0D) - var2) / 3.0F;
	}

	public Vec3D getCloudColor(float var1) {
		float var2 = MathHelper.cos(this.getCelestialAngle(var1) * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		return Vec3D.createVector((double)((float)(this.cloudColor >> 16 & 255L) / 255.0F * (var2 * 0.9F + 0.1F)), (double)((float)(this.cloudColor >> 8 & 255L) / 255.0F * (var2 * 0.9F + 0.1F)), (double)((float)(this.cloudColor & 255L) / 255.0F * (var2 * 0.85F + 0.15F)));
	}

	public Vec3D getFogColor(float var1) {
		float var2 = MathHelper.cos(this.getCelestialAngle(var1) * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		return Vec3D.createVector((double)((float)(this.fogColor >> 16 & 255L) / 255.0F * (var2 * 0.94F + 0.06F)), (double)((float)(this.fogColor >> 8 & 255L) / 255.0F * (var2 * 0.94F + 0.06F)), (double)((float)(this.fogColor & 255L) / 255.0F * (var2 * 0.91F + 0.09F)));
	}

	public int getTopSolidOrLiquidBlock(int var1, int var2) {
		Chunk var3 = this.getChunkFromBlockCoords(var1, var2);
		int var4 = 127;
		var1 &= 15;

		for(var2 &= 15; var4 > 0; --var4) {
			int var5 = var3.getBlockID(var1, var4, var2);
			if(var5 != 0 && (Block.blocksList[var5].material.getIsSolid() || Block.blocksList[var5].material.getIsLiquid())) {
				return var4 + 1;
			}
		}

		return -1;
	}

	public int getPrecipitationHeight(int var1, int var2) {
		return this.eStage2(var1, var2);
	}

	public int eStage2(int var1, int var2) {
		return this.getChunkFromBlockCoords(var1, var2).getHeightValue(var1 & 15, var2 & 15);
	}

	public float getStarBrightness(float var1) {
		float var2 = 1.0F - (MathHelper.cos(this.getCelestialAngle(var1) * (float)Math.PI * 2.0F) * 2.0F + 12.0F / 16.0F);
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		return var2 * var2 * 0.5F;
	}

	public void scheduleBlockUpdate(int var1, int var2, int var3, int var4) {
		NextTickListEntry var5 = new NextTickListEntry(var1, var2, var3, var4);
		if(this.checkChunksExist(var1 - 8, var2 - 8, var3 - 8, var1 + 8, var2 + 8, var3 + 8)) {
			if(var4 > 0) {
				var5.setScheduledTime((long)Block.blocksList[var4].tickRate() + this.worldTime);
			}

			if(!this.scheduledTickSet.contains(var5)) {
				this.scheduledTickSet.add(var5);
				this.scheduledTickTreeSet.add(var5);
			}
		}

	}

	public void updateEntities() {
		this.loadedEntityList.removeAll(this.unloadedEntityList);

		int var1;
		Entity var2;
		int var3;
		int var4;
		for(var1 = 0; var1 < this.unloadedEntityList.size(); ++var1) {
			var2 = (Entity)this.unloadedEntityList.get(var1);
			var3 = var2.chunkCoordX;
			var4 = var2.chunkCoordZ;
			if(var2.addedToChunk && this.chunkExists(var3, var4)) {
				this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
			}
		}

		for(var1 = 0; var1 < this.unloadedEntityList.size(); ++var1) {
			this.releaseEntitySkin((Entity)this.unloadedEntityList.get(var1));
		}

		this.unloadedEntityList.clear();

		for(var1 = 0; var1 < this.loadedEntityList.size(); ++var1) {
			var2 = (Entity)this.loadedEntityList.get(var1);
			if(var2.ridingEntity != null) {
				if(!var2.ridingEntity.isDead && var2.ridingEntity.riddenByEntity == var2) {
					continue;
				}

				var2.ridingEntity.riddenByEntity = null;
				var2.ridingEntity = null;
			}

			if(!var2.isDead) {
				this.updateEntity(var2);
			}

			if(var2.isDead) {
				var3 = var2.chunkCoordX;
				var4 = var2.chunkCoordZ;
				if(var2.addedToChunk && this.chunkExists(var3, var4)) {
					this.getChunkFromChunkCoords(var3, var4).removeEntity(var2);
				}

				this.loadedEntityList.remove(var1--);
				this.releaseEntitySkin(var2);
			}
		}

		for(var1 = 0; var1 < this.loadedTileEntityList.size(); ++var1) {
			((TileEntity)this.loadedTileEntityList.get(var1)).updateEntity();
		}

	}

	protected void updateEntity(Entity var1) {
		int var2 = MathHelper.floor_double(var1.posX);
		int var3 = MathHelper.floor_double(var1.posZ);
		if(this.checkChunksExist(var2 - 16, 0, var3 - 16, var2 + 16, 128, var3 + 16)) {
			var1.lastTickPosX = var1.posX;
			var1.lastTickPosY = var1.posY;
			var1.lastTickPosZ = var1.posZ;
			var1.prevRotationYaw = var1.rotationYaw;
			var1.prevRotationPitch = var1.rotationPitch;
			if(var1.ridingEntity != null) {
				var1.updateRidden();
			} else {
				var1.onUpdate();
			}

			int var5 = MathHelper.floor_double(var1.posX / 16.0D);
			int var6 = MathHelper.floor_double(var1.posY / 16.0D);
			int var7 = MathHelper.floor_double(var1.posZ / 16.0D);
			if(!var1.addedToChunk || var1.chunkCoordX != var5 || var1.chunkCoordY != var6 || var1.chunkCoordZ != var7) {
				if(var1.addedToChunk && this.chunkExists(var1.chunkCoordX, var1.chunkCoordZ)) {
					this.getChunkFromChunkCoords(var1.chunkCoordX, var1.chunkCoordZ).removeEntityAtIndex(var1, var1.chunkCoordY);
				}

				if(this.chunkExists(var5, var7)) {
					this.getChunkFromChunkCoords(var5, var7).addEntity(var1);
				} else {
					var1.addedToChunk = false;
					System.out.println("Removing entity because it\'s not in a chunk!!");
					var1.setEntityDead();
				}
			}

			if(var1.riddenByEntity != null) {
				if(!var1.riddenByEntity.isDead && var1.riddenByEntity.ridingEntity == var1) {
					this.updateEntity(var1.riddenByEntity);
				} else {
					var1.riddenByEntity.ridingEntity = null;
					var1.riddenByEntity = null;
				}
			}

			if(Double.isNaN(var1.posX) || Double.isInfinite(var1.posX)) {
				var1.posX = var1.lastTickPosX;
			}

			if(Double.isNaN(var1.posY) || Double.isInfinite(var1.posY)) {
				var1.posY = var1.lastTickPosY;
			}

			if(Double.isNaN(var1.posZ) || Double.isInfinite(var1.posZ)) {
				var1.posZ = var1.lastTickPosZ;
			}

			if(Double.isNaN((double)var1.rotationPitch) || Double.isInfinite((double)var1.rotationPitch)) {
				var1.rotationPitch = var1.prevRotationPitch;
			}

			if(Double.isNaN((double)var1.rotationYaw) || Double.isInfinite((double)var1.rotationYaw)) {
				var1.rotationYaw = var1.prevRotationYaw;
			}

		}
	}

	public boolean checkIfAABBIsClear(AxisAlignedBB var1) {
		List var2 = this.getEntitiesWithinAABBExcludingEntity((Entity)null, var1);

		for(int var3 = 0; var3 < var2.size(); ++var3) {
			Entity var4 = (Entity)var2.get(var3);
			if(!var4.isDead && var4.preventEntitySpawning) {
				return false;
			}
		}

		return true;
	}

	public boolean getIsAnyLiquid(AxisAlignedBB var1) {
		int var2 = MathHelper.floor_double(var1.minX);
		int var3 = MathHelper.floor_double(var1.maxX + 1.0D);
		int var4 = MathHelper.floor_double(var1.minY);
		int var5 = MathHelper.floor_double(var1.maxY + 1.0D);
		int var6 = MathHelper.floor_double(var1.minZ);
		int var7 = MathHelper.floor_double(var1.maxZ + 1.0D);
		if(var1.minX < 0.0D) {
			--var2;
		}

		if(var1.minY < 0.0D) {
			--var4;
		}

		if(var1.minZ < 0.0D) {
			--var6;
		}

		for(int var8 = var2; var8 < var3; ++var8) {
			for(int var9 = var4; var9 < var5; ++var9) {
				for(int var10 = var6; var10 < var7; ++var10) {
					Block var11 = Block.blocksList[this.getBlockId(var8, var9, var10)];
					if(var11 != null && var11.material.getIsLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean isBoundingBoxBurning(AxisAlignedBB var1) {
		int var2 = MathHelper.floor_double(var1.minX);
		int var3 = MathHelper.floor_double(var1.maxX + 1.0D);
		int var4 = MathHelper.floor_double(var1.minY);
		int var5 = MathHelper.floor_double(var1.maxY + 1.0D);
		int var6 = MathHelper.floor_double(var1.minZ);
		int var7 = MathHelper.floor_double(var1.maxZ + 1.0D);

		for(int var8 = var2; var8 < var3; ++var8) {
			for(int var9 = var4; var9 < var5; ++var9) {
				for(int var10 = var6; var10 < var7; ++var10) {
					int var11 = this.getBlockId(var8, var9, var10);
					if(var11 == Block.fire.blockID || var11 == Block.lavaMoving.blockID || var11 == Block.lavaStill.blockID) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean handleMaterialAcceleration(AxisAlignedBB var1, Material var2, Entity var3) {
		int var4 = MathHelper.floor_double(var1.minX);
		int var5 = MathHelper.floor_double(var1.maxX + 1.0D);
		int var6 = MathHelper.floor_double(var1.minY);
		int var7 = MathHelper.floor_double(var1.maxY + 1.0D);
		int var8 = MathHelper.floor_double(var1.minZ);
		int var9 = MathHelper.floor_double(var1.maxZ + 1.0D);
		boolean var10 = false;
		Vec3D var11 = Vec3D.createVector(0.0D, 0.0D, 0.0D);

		for(int var12 = var4; var12 < var5; ++var12) {
			for(int var13 = var6; var13 < var7; ++var13) {
				for(int var14 = var8; var14 < var9; ++var14) {
					Block var15 = Block.blocksList[this.getBlockId(var12, var13, var14)];
					if(var15 != null && var15.material == var2 && (double)var7 >= (double)((float)(var13 + 1) - BlockFluid.getFluidHeightPercent(this.getBlockMetadata(var12, var13, var14)))) {
						var10 = true;
						var15.velocityToAddToEntity(this, var12, var13, var14, var3, var11);
					}
				}
			}
		}

		if(var11.lengthVector() > 0.0D) {
			Vec3D var16 = var11.normalize();
			var3.motionX += var16.xCoord * 0.004D;
			var3.motionY += var16.yCoord * 0.004D;
			var3.motionZ += var16.zCoord * 0.004D;
		}

		return var10;
	}

	public boolean isMaterialInBB(AxisAlignedBB var1, Material var2) {
		int var3 = MathHelper.floor_double(var1.minX);
		int var4 = MathHelper.floor_double(var1.maxX + 1.0D);
		int var5 = MathHelper.floor_double(var1.minY);
		int var6 = MathHelper.floor_double(var1.maxY + 1.0D);
		int var7 = MathHelper.floor_double(var1.minZ);
		int var8 = MathHelper.floor_double(var1.maxZ + 1.0D);

		for(int var9 = var3; var9 < var4; ++var9) {
			for(int var10 = var5; var10 < var6; ++var10) {
				for(int var11 = var7; var11 < var8; ++var11) {
					Block var12 = Block.blocksList[this.getBlockId(var9, var10, var11)];
					if(var12 != null && var12.material == var2) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean isAABBInMaterial(AxisAlignedBB var1, Material var2) {
		int var3 = MathHelper.floor_double(var1.minX);
		int var4 = MathHelper.floor_double(var1.maxX + 1.0D);
		int var5 = MathHelper.floor_double(var1.minY);
		int var6 = MathHelper.floor_double(var1.maxY + 1.0D);
		int var7 = MathHelper.floor_double(var1.minZ);
		int var8 = MathHelper.floor_double(var1.maxZ + 1.0D);

		for(int var9 = var3; var9 < var4; ++var9) {
			for(int var10 = var5; var10 < var6; ++var10) {
				for(int var11 = var7; var11 < var8; ++var11) {
					Block var12 = Block.blocksList[this.getBlockId(var9, var10, var11)];
					if(var12 != null && var12.material == var2) {
						int var13 = this.getBlockMetadata(var9, var10, var11);
						double var14 = (double)(var10 + 1);
						if(var13 < 8) {
							var14 = (double)(var10 + 1) - (double)var13 / 8.0D;
						}

						if(var14 >= var1.minY) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public void createExplosion(Entity var1, double var2, double var4, double var6, float var8) {
		(new Explosion()).doExplosion(this, var1, var2, var4, var6, var8);
	}

	public float getBlockDensity(Vec3D var1, AxisAlignedBB var2) {
		double var3 = 1.0D / ((var2.maxX - var2.minX) * 2.0D + 1.0D);
		double var5 = 1.0D / ((var2.maxY - var2.minY) * 2.0D + 1.0D);
		double var7 = 1.0D / ((var2.maxZ - var2.minZ) * 2.0D + 1.0D);
		int var9 = 0;
		int var10 = 0;

		for(float var11 = 0.0F; var11 <= 1.0F; var11 += (float)var3) {
			for(float var12 = 0.0F; var12 <= 1.0F; var12 += (float)var5) {
				for(float var13 = 0.0F; var13 <= 1.0F; var13 += (float)var7) {
					if(this.rayTraceBlocks(Vec3D.createVector(var2.minX + (var2.maxX - var2.minX) * (double)var11, var2.minY + (var2.maxY - var2.minY) * (double)var12, var2.minZ + (var2.maxZ - var2.minZ) * (double)var13), var1) == null) {
						++var9;
					}

					++var10;
				}
			}
		}

		return (float)var9 / (float)var10;
	}

	public void extinguishFire(int var1, int var2, int var3, int var4) {
		if(var4 == 0) {
			--var2;
		}

		if(var4 == 1) {
			++var2;
		}

		if(var4 == 2) {
			--var3;
		}

		if(var4 == 3) {
			++var3;
		}

		if(var4 == 4) {
			--var1;
		}

		if(var4 == 5) {
			++var1;
		}

		if(this.getBlockId(var1, var2, var3) == Block.fire.blockID) {
			this.playSoundEffect((double)((float)var1 + 0.5F), (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), "random.fizz", 0.5F, 2.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.8F);
			this.setBlockWithNotify(var1, var2, var3, 0);
		}

	}

	public Entity createDebugPlayer(Class var1) {
		return null;
	}

	public String getDebugLoadedEntities() {
		return "All: " + this.loadedEntityList.size();
	}

	public TileEntity getBlockTileEntity(int var1, int var2, int var3) {
		Chunk var4 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
		return var4 != null ? var4.getChunkBlockTileEntity(var1 & 15, var2, var3 & 15) : null;
	}

	public void setBlockTileEntity(int var1, int var2, int var3, TileEntity var4) {
		Chunk var5 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
		if(var5 != null) {
			var5.setChunkBlockTileEntity(var1 & 15, var2, var3 & 15, var4);
		}

	}

	public void removeBlockTileEntity(int var1, int var2, int var3) {
		Chunk var4 = this.getChunkFromChunkCoords(var1 >> 4, var3 >> 4);
		if(var4 != null) {
			var4.removeChunkBlockTileEntity(var1 & 15, var2, var3 & 15);
		}

	}

	public boolean isBlockNormalCube(int var1, int var2, int var3) {
		Block var4 = Block.blocksList[this.getBlockId(var1, var2, var3)];
		return var4 != null && var4.isOpaqueCube();
	}

	public void saveWorldIndirectly(IProgressUpdate var1) {
		this.saveWorld(true, var1);
	}

	public boolean updatingLighting() {
		return this.e2stage2();
	}

	public boolean e2stage2() {
		int var1 = 1000;

		while(this.lightingToUpdate.size() > 0) {
			--var1;
			if(var1 <= 0) {
				return true;
			}

			((MetadataChunkBlock)this.lightingToUpdate.remove(this.lightingToUpdate.size() - 1)).updateLight(this);
		}

		return false;
	}

	public void scheduleLightingUpdate(EnumSkyBlock var1, int var2, int var3, int var4, int var5, int var6, int var7) {
		this.scheduleLightingUpdate_do(var1, var2, var3, var4, var5, var6, var7, true);
	}

	public void scheduleLightingUpdate_do(EnumSkyBlock var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
		if(this.blockExists((var5 + var2) / 2, 64, (var7 + var4) / 2)) {
			int var9 = this.lightingToUpdate.size();
			if(var8) {
				int var10 = 4;
				if(var10 > var9) {
					var10 = var9;
				}

				for(int var11 = 0; var11 < var10; ++var11) {
					MetadataChunkBlock var12 = (MetadataChunkBlock)this.lightingToUpdate.get(this.lightingToUpdate.size() - var11 - 1);
					if(var12.skyBlock == var1 && var12.getLightUpdated(var2, var3, var4, var5, var6, var7)) {
						return;
					}
				}
			}

			this.lightingToUpdate.add(new MetadataChunkBlock(var1, var2, var3, var4, var5, var6, var7));
			if(this.lightingToUpdate.size() > 100000) {
				while(this.lightingToUpdate.size() > '\uc350') {
					this.updatingLighting();
				}
			}

		}
	}

	public void calculateInitialSkylight() {
		int var1 = this.calculateSkylightSubtracted(1.0F);
		if(var1 != this.skylightSubtracted) {
			this.skylightSubtracted = var1;
		}

	}

	public void tick() {
		this.chunkProvider.unload100OldestChunks();
		int var1 = this.calculateSkylightSubtracted(1.0F);
		if(var1 != this.skylightSubtracted) {
			this.skylightSubtracted = var1;

			for(int var2 = 0; var2 < this.worldAccesses.size(); ++var2) {
				((IWorldAccess)this.worldAccesses.get(var2)).updateAllRenderers();
			}
		}

		if(this.bossfightInProgress && this.bossRef.isDead) {
			this.bossfightInProgress = false;
			this.bossRef = null;
		}

		if(this.exclFrailMode && InputHandler.mc.options.difficulty != 4) {
			this.exclFrailMode = false;
			System.out.println("World has been changed from Frail mode");
		}

		if(this.CanUseCheats() && Keyboard.isKeyDown(Keyboard.KEY_MULTIPLY)) {
			this.worldTime += 12L;
		} else if(!this.CanUseCheats() || !Keyboard.isKeyDown(Keyboard.KEY_DECIMAL)) {
			++this.worldTime;
			if(this.worldTime % 23000L == 0L) {
				++this.milestone;
				System.out.println("Milestone " + this.milestone + (this.exclFrailMode ? "*" : "") + " reached.");
				if(this.exclFrailMode && this.milestone == 10L) {
					GuiIngame.uqKey = ScreenKeyInput.playerIndex() + ": " + ScreenKeyInput.calcString((long)ScreenKeyInput.playerIndex() << 56, ~((int)(this.milestone - 2L - (long)ScreenKeyInput.playerIndex())));
					System.out.println(GuiIngame.uqKey);
				}
			}
		}

		if(this.worldTime % (long)this.autosavePeriod == 0L) {
			this.saveWorld(false, (IProgressUpdate)null);
		}

		this.tickUpdates(false);
		this.updateBlocksAndPlayCaveSounds();
	}

	protected void updateBlocksAndPlayCaveSounds() {
		this.positionsToUpdate.clear();

		int var4;
		int var7;
		for(int var1 = 0; var1 < this.playerEntities.size(); ++var1) {
			EntityPlayer var2 = (EntityPlayer)this.playerEntities.get(var1);
			int var3 = MathHelper.floor_double(var2.posX / 16.0D);
			var4 = MathHelper.floor_double(var2.posZ / 16.0D);
			byte var5 = 9;

			for(int var6 = -var5; var6 <= var5; ++var6) {
				for(var7 = -var5; var7 <= var5; ++var7) {
					this.positionsToUpdate.add(new ChunkCoordIntPair(var6 + var3, var7 + var4));
				}
			}
		}

		if(this.soundCounter > 0) {
			--this.soundCounter;
		}

		Iterator var15 = this.positionsToUpdate.iterator();

		while(var15.hasNext()) {
			Object var16 = var15.next();
			ChunkCoordIntPair var17 = (ChunkCoordIntPair)var16;
			var4 = var17.chunkXPos * 16;
			int var18 = var17.chunkZPos * 16;
			Chunk var19 = this.getChunkFromChunkCoords(var17.chunkXPos, var17.chunkZPos);
			int var8;
			int var9;
			int var10;
			int var11;
			if(this.soundCounter == 0) {
				this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
				var7 = this.updateLCG >> 2;
				var8 = var7 & 15;
				var9 = var7 >> 8 & 15;
				var10 = var7 >> 16 & 127;
				var11 = var19.getBlockID(var8, var10, var9);
				int var12 = var8 + var4;
				int var13 = var9 + var18;
				if(var11 == 0 && this.getBlockLightValue(var12, var10, var13) <= this.rand.nextInt(8) && this.getSavedLightValue(EnumSkyBlock.Sky, var12, var10, var13) <= 0) {
					EntityPlayer var14 = this.getClosestPlayer((double)var12 + 0.5D, (double)var10 + 0.5D, (double)var13 + 0.5D, 8.0D);
					if(var14 != null && var14.getDistanceSq((double)var12 + 0.5D, (double)var10 + 0.5D, (double)var13 + 0.5D) > 4.0D) {
						this.playSoundEffect((double)var12 + 0.5D, (double)var10 + 0.5D, (double)var13 + 0.5D, "ambient.cave.cave", 0.7F, 0.8F + this.rand.nextFloat() * 0.2F);
						this.soundCounter = this.rand.nextInt(12000) + 6000;
					}
				}
			}

			if(this.snowCovered && this.rand.nextInt(4) == 0) {
				this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
				var7 = this.updateLCG >> 2;
				var8 = var7 & 15;
				var9 = var7 >> 8 & 15;
				var10 = this.getTopSolidOrLiquidBlock(var8 + var4, var9 + var18);
				if(var10 >= 0 && var10 < 128 && var19.getSavedLightValue(EnumSkyBlock.Block, var8, var10, var9) < 10) {
					var11 = var19.getBlockID(var8, var10 - 1, var9);
					if(var19.getBlockID(var8, var10, var9) == 0 && Block.snow.canPlaceBlockAt(this, var8 + var4, var10, var9 + var18)) {
						this.setBlockWithNotify(var8 + var4, var10, var9 + var18, Block.snow.blockID);
					}

					if(var11 == Block.waterStill.blockID && var19.getBlockMetadata(var8, var10 - 1, var9) == 0) {
						this.setBlockWithNotify(var8 + var4, var10 - 1, var9 + var18, Block.ice.blockID);
					}
				}
			}

			for(var7 = 0; var7 < 80; ++var7) {
				this.updateLCG = this.updateLCG * 3 + this.DIST_HASH_MAGIC;
				var8 = this.updateLCG >> 2;
				var9 = var8 & 15;
				var10 = var8 >> 8 & 15;
				var11 = var8 >> 16 & 127;
				byte var20 = var19.blocks[var9 << 11 | var10 << 7 | var11];
				if(Block.tickOnLoad[var20]) {
					Block.blocksList[var20].updateTick(this, var9 + var4, var11, var10 + var18, this.rand);
				}
			}
		}

	}

	public boolean tickUpdates(boolean var1) {
		int var2 = this.scheduledTickTreeSet.size();
		if(var2 != this.scheduledTickSet.size()) {
			throw new IllegalStateException("TickNextTick list out of synch");
		} else {
			if(var2 > 1000) {
				var2 = 1000;
			}

			for(int var3 = 0; var3 < var2; ++var3) {
				NextTickListEntry var4 = (NextTickListEntry)this.scheduledTickTreeSet.first();
				if(!var1 && var4.scheduledTime > this.worldTime) {
					break;
				}

				this.scheduledTickTreeSet.remove(var4);
				this.scheduledTickSet.remove(var4);
				if(this.checkChunksExist(var4.xCoord - 8, var4.yCoord - 8, var4.zCoord - 8, var4.xCoord + 8, var4.yCoord + 8, var4.zCoord + 8)) {
					int var6 = this.getBlockId(var4.xCoord, var4.yCoord, var4.zCoord);
					if(var6 == var4.blockID && var6 > 0) {
						Block.blocksList[var6].updateTick(this, var4.xCoord, var4.yCoord, var4.zCoord, this.rand);
					}
				}
			}

			return this.scheduledTickTreeSet.size() != 0;
		}
	}

	public void randomDisplayUpdates(int var1, int var2, int var3) {
		Random var5 = new Random();

		for(int var6 = 0; var6 < 1000; ++var6) {
			int var7 = var1 + this.rand.nextInt(16) - this.rand.nextInt(16);
			int var8 = var2 + this.rand.nextInt(16) - this.rand.nextInt(16);
			int var9 = var3 + this.rand.nextInt(16) - this.rand.nextInt(16);
			int var10 = this.getBlockId(var7, var8, var9);
			if(var10 > 0) {
				Block.blocksList[var10].randomDisplayTick(this, var7, var8, var9, var5);
			}
		}

	}

	public List getEntitiesWithinAABBExcludingEntity(Entity var1, AxisAlignedBB var2) {
		this.entitiesWithinAABBExcludingEntity.clear();
		int var3 = MathHelper.floor_double((var2.minX - 2.0D) / 16.0D);
		int var4 = MathHelper.floor_double((var2.maxX + 2.0D) / 16.0D);
		int var5 = MathHelper.floor_double((var2.minZ - 2.0D) / 16.0D);
		int var6 = MathHelper.floor_double((var2.maxZ + 2.0D) / 16.0D);

		for(int var7 = var3; var7 <= var4; ++var7) {
			for(int var8 = var5; var8 <= var6; ++var8) {
				if(this.chunkExists(var7, var8)) {
					this.getChunkFromChunkCoords(var7, var8).getEntitiesWithinAABBForEntity(var1, var2, this.entitiesWithinAABBExcludingEntity);
				}
			}
		}

		return this.entitiesWithinAABBExcludingEntity;
	}

	public List getEntitiesWithinAABB(Class var1, AxisAlignedBB var2) {
		int var3 = MathHelper.floor_double((var2.minX - 2.0D) / 16.0D);
		int var4 = MathHelper.floor_double((var2.maxX + 2.0D) / 16.0D);
		int var5 = MathHelper.floor_double((var2.minZ - 2.0D) / 16.0D);
		int var6 = MathHelper.floor_double((var2.maxZ + 2.0D) / 16.0D);
		ArrayList var7 = new ArrayList();

		for(int var8 = var3; var8 <= var4; ++var8) {
			for(int var9 = var5; var9 <= var6; ++var9) {
				if(this.chunkExists(var8, var9)) {
					this.getChunkFromChunkCoords(var8, var9).getEntitiesOfTypeWithinAAAB(var1, var2, var7);
				}
			}
		}

		return var7;
	}

	public List getLoadedEntityList() {
		return this.loadedEntityList;
	}

	public void updateTileEntityChunkAndDoNothing(int var1, int var2, int var3, TileEntity var4) {
		if(this.blockExists(var1, var2, var3)) {
			this.getChunkFromBlockCoords(var1, var3).setChunkModified();
		}

		for(int var5 = 0; var5 < this.worldAccesses.size(); ++var5) {
			((IWorldAccess)this.worldAccesses.get(var5)).doNothingWithTileEntity(var1, var2, var3, var4);
		}

	}

	public int countEntities(Class var1) {
		int var2 = 0;

		for(int var3 = 0; var3 < this.loadedEntityList.size(); ++var3) {
			if(var1.isAssignableFrom(((Entity)this.loadedEntityList.get(var3)).getClass())) {
				++var2;
			}
		}

		return var2;
	}

	public void addLoadedEntities(List var1) {
		this.loadedEntityList.addAll(var1);

		for(int var2 = 0; var2 < var1.size(); ++var2) {
			this.obtainEntitySkin((Entity)var1.get(var2));
		}

	}

	public void unloadEntities(List var1) {
		this.unloadedEntityList.addAll(var1);
	}

	public void dropOldChunks() {
		while(this.chunkProvider.unload100OldestChunks()) {
		}

	}

	public boolean canBlockBePlacedAt(int var1, int var2, int var3, int var4, boolean var5) {
		Block var6 = Block.blocksList[this.getBlockId(var2, var3, var4)];
		Block var7 = Block.blocksList[var1];
		AxisAlignedBB var8 = var7.getCollisionBoundingBoxFromPool(this, var2, var3, var4);
		if(var5) {
			var8 = null;
		}

		return (var8 == null || this.checkIfAABBIsClear(var8)) && (var6 == Block.waterMoving || var6 == Block.waterStill || var6 == Block.lavaMoving || var6 == Block.lavaStill || var6 == Block.fire || var6 == Block.snow || var1 > 0 && var6 == null && var7.canPlaceBlockAt(this, var2, var3, var4));
	}

	public PathEntity getPathToEntity(Entity var1, Entity var2, float var3) {
		int var4 = MathHelper.floor_double(var1.posX);
		int var5 = MathHelper.floor_double(var1.posY);
		int var6 = MathHelper.floor_double(var1.posZ);
		int var7 = (int)(var3 + 16.0F);
		return (new Pathfinder(new ChunkCache(this, var4 - var7, var5 - var7, var6 - var7, var4 + var7, var5 + var7, var6 + var7))).createEntityPathTo(var1, var2, var3);
	}

	public PathEntity getEntityPathToXYZ(Entity var1, int var2, int var3, int var4, float var5) {
		int var6 = MathHelper.floor_double(var1.posX);
		int var7 = MathHelper.floor_double(var1.posY);
		int var8 = MathHelper.floor_double(var1.posZ);
		int var9 = (int)(var5 + 8.0F);
		return (new Pathfinder(new ChunkCache(this, var6 - var9, var7 - var9, var8 - var9, var6 + var9, var7 + var9, var8 + var9))).createEntityPathTo(var1, var2, var3, var4, var5);
	}

	public boolean isBlockProvidingPowerTo(int var1, int var2, int var3, int var4) {
		int var5 = this.getBlockId(var1, var2, var3);
		return var5 != 0 && Block.blocksList[var5].isIndirectlyPoweringTo(this, var1, var2, var3, var4);
	}

	public boolean isBlockGettingPowered(int var1, int var2, int var3) {
		return this.isBlockProvidingPowerTo(var1, var2 - 1, var3, 0) || this.isBlockProvidingPowerTo(var1, var2 + 1, var3, 1) || this.isBlockProvidingPowerTo(var1, var2, var3 - 1, 2) || this.isBlockProvidingPowerTo(var1, var2, var3 + 1, 3) || this.isBlockProvidingPowerTo(var1 - 1, var2, var3, 4) || this.isBlockProvidingPowerTo(var1 + 1, var2, var3, 5);
	}

	public boolean isBlockIndirectlyProvidingPowerTo(int var1, int var2, int var3, int var4) {
		if(this.isBlockNormalCube(var1, var2, var3)) {
			return this.isBlockGettingPowered(var1, var2, var3);
		} else {
			int var5 = this.getBlockId(var1, var2, var3);
			return var5 != 0 && Block.blocksList[var5].isPoweringTo(this, var1, var2, var3, var4);
		}
	}

	public boolean isBlockIndirectlyGettingPowered(int var1, int var2, int var3) {
		return this.isBlockIndirectlyProvidingPowerTo(var1, var2 - 1, var3, 0) || this.isBlockIndirectlyProvidingPowerTo(var1, var2 + 1, var3, 1) || this.isBlockIndirectlyProvidingPowerTo(var1, var2, var3 - 1, 2) || this.isBlockIndirectlyProvidingPowerTo(var1, var2, var3 + 1, 3) || this.isBlockIndirectlyProvidingPowerTo(var1 - 1, var2, var3, 4) || this.isBlockIndirectlyProvidingPowerTo(var1 + 1, var2, var3, 5);
	}

	public EntityPlayer getClosestPlayerToEntity(Entity var1, double var2) {
		return this.getClosestPlayer(var1.posX, var1.posY, var1.posZ, var2);
	}

	public EntityPlayer getClosestPlayer(double var1, double var3, double var5, double var7) {
		double var9 = -1.0D;
		EntityPlayer var11 = null;

		for(int var12 = 0; var12 < this.playerEntities.size(); ++var12) {
			EntityPlayer var13 = (EntityPlayer)this.playerEntities.get(var12);
			double var14 = var13.getDistanceSq(var1, var3, var5);
			if((var7 < 0.0D || var14 < var7 * var7) && (var9 == -1.0D || var14 < var9)) {
				var9 = var14;
				var11 = var13;
			}
		}

		return var11;
	}

	public void setChunkData(int var1, int var2, int var3, int var4, int var5, int var6, byte[] var7) {
		int var8 = var1 >> 4;
		int var9 = var3 >> 4;
		int var10 = var1 + var4 - 1 >> 4;
		int var11 = var3 + var6 - 1 >> 4;
		int var12 = 0;
		int var13 = var2;
		int var14 = var2 + var5;
		if(var2 < 0) {
			var13 = 0;
		}

		if(var14 > 128) {
			var14 = 128;
		}

		for(int var15 = var8; var15 <= var10; ++var15) {
			int var16 = var1 - var15 * 16;
			int var17 = var1 + var4 - var15 * 16;
			if(var16 < 0) {
				var16 = 0;
			}

			if(var17 > 16) {
				var17 = 16;
			}

			for(int var18 = var9; var18 <= var11; ++var18) {
				int var19 = var3 - var18 * 16;
				int var20 = var3 + var6 - var18 * 16;
				if(var19 < 0) {
					var19 = 0;
				}

				if(var20 > 16) {
					var20 = 16;
				}

				var12 = this.getChunkFromChunkCoords(var15, var18).setChunkData(var7, var16, var13, var19, var17, var14, var20, var12);
				this.markBlocksDirty(var15 * 16 + var16, var13, var18 * 16 + var19, var15 * 16 + var17, var14, var18 * 16 + var20);
			}
		}

	}

	public void sendQuittingDisconnectingPacket() {
	}

	public void checkSessionLock() {
		try {
			DataInputStream var1 = new DataInputStream(new FileInputStream(new File(this.saveDirectory, "session.lock")));

			try {
				if(var1.readLong() != this.lockTimestamp) {
					throw new MinecraftException("The save is being accessed from another location, aborting");
				}
			} finally {
				var1.close();
			}

		} catch (IOException var6) {
			throw new MinecraftException("Failed to check session lock, aborting");
		}
	}

	public void setWorldTime(long var1) {
		this.worldTime = var1;
	}

	public void joinEntityInSurroundings(Entity var1) {
		int var2 = MathHelper.floor_double(var1.posX / 16.0D);
		int var3 = MathHelper.floor_double(var1.posZ / 16.0D);
		byte var4 = 2;

		for(int var5 = var2 - var4; var5 <= var2 + var4; ++var5) {
			for(int var6 = var3 - var4; var6 <= var3 + var4; ++var6) {
				this.getChunkFromChunkCoords(var5, var6);
			}
		}

		if(!this.loadedEntityList.contains(var1)) {
			System.out.println("REINSERTING PLAYER!");
			this.loadedEntityList.add(var1);
		}

	}

	static {
		for(int var1 = 0; var1 <= 15; ++var1) {
			float var2 = 1.0F - (float)var1 / 15.0F;
			lightBrightnessTable[var1] = (1.0F - var2) / (var2 * 3.0F + 1.0F) * 0.95F + 0.05F;
		}

	}
}
