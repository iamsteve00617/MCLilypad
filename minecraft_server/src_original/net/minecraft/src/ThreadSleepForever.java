package net.minecraft.src;

import net.minecraft.server.MinecraftServer;

public class ThreadSleepForever extends Thread {
	final MinecraftServer mcServer;

	public ThreadSleepForever(MinecraftServer var1) {
		this.mcServer = var1;
		this.setDaemon(true);
		this.start();
	}

	public void run() {
		while(true) {
			try {
				Thread.sleep(2147483647L);
			} catch (InterruptedException var2) {
			}
		}
	}
}
