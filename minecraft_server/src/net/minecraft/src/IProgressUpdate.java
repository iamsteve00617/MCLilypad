package net.minecraft.src;

public interface IProgressUpdate {
	void displayProgressMessage(String var1);

	void resetProgressAndWorkingMessage(String var1);

	void setLoadingProgress(int var1);
}
