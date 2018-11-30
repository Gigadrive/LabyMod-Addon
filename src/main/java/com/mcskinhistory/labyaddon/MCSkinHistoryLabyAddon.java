package com.mcskinhistory.labyaddon;

import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MCSkinHistoryLabyAddon extends LabyModAddon {
    ArrayList<String> savedUUIDs;
    ArrayList<String> queue;

    Thread readThread;
    Thread saveThread;

    /**
     * Called when the addon gets enabled
     */
    @Override
    public void onEnable() {
        try {
            savedUUIDs = new ArrayList<String>();
            queue = new ArrayList<String>();

            System.out.println("Loading Skin History addon");

            // READ THREAD
            readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            Minecraft minecraft = Minecraft.getMinecraft();
                            if (minecraft != null) {
                                NetHandlerPlayClient netHandler = minecraft.getNetHandler();

                                if (netHandler != null) {
                                    if (netHandler.getPlayerInfoMap() != null) {
                                        ArrayList<NetworkPlayerInfo> players = new ArrayList<NetworkPlayerInfo>(netHandler.getPlayerInfoMap());

                                        Iterator<NetworkPlayerInfo> iterator = players.iterator();
                                        while (iterator.hasNext()) {
                                            NetworkPlayerInfo p = iterator.next();

                                            String id = p.getGameProfile().getId().toString().replace("-", "");

                                            if (!savedUUIDs.contains(id) && !queue.contains(id)) {
                                                queue.add(id);
                                            }
                                        }
                                    }
                                }
                            }

                            Thread.sleep(3 * 1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "SH User Read Thread");

            this.readThread.start();

            // SAVE THREAD
            this.saveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            if (queue.size() > 0) {
                                String uuid = queue.get(0);
                                System.out.println("SAVING: " + uuid);

                                HttpURLConnection con = (HttpURLConnection) new URL("https://mcskinhistory.com/api/playerData/" + uuid).openConnection();
                                con.setRequestMethod("GET");
                                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

                                con.connect();

                                BufferedReader in = new BufferedReader(
                                        new InputStreamReader(con.getInputStream()));
                                String inputLine;
                                StringBuilder content = new StringBuilder();
                                while ((inputLine = in.readLine()) != null) {
                                    content.append(inputLine);
                                }
                                in.close();

                                System.out.println(content.toString());

                                con.disconnect();

                                savedUUIDs.add(uuid);
                                queue.remove(uuid);
                            }
                        }
                    } catch (IOException e) {
                        // ignored
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "SH User Save Thread");

            this.saveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the addon gets disabled
     */
    @Override
    public void onDisable() {
        this.readThread.interrupt();
        this.readThread = null;

        this.saveThread.interrupt();
        this.saveThread = null;
    }

    /**
     * Called when this addon's config was loaded and is ready to use
     */
    @Override
    public void loadConfig() {

    }

    /**
     * Called when the addon's ingame settings should be filled
     *
     * @param subSettings a list containing the addon's settings' elements
     */
    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {

    }
}
