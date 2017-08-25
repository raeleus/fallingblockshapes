package com.ray3k.fallingblockshapes.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ray3k.fallingblockshapes.Core;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                config.width = 800;
                config.height = 800;
		new LwjglApplication(new Core(), config);
	}
}
