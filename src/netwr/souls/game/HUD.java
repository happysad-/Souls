package netwr.souls.game;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import netwr.souls.Window;
import netwr.souls.logic.GameItem;
import netwr.souls.rendering.text.FontTexture;
import netwr.souls.rendering.text.IHUD;
import netwr.souls.rendering.text.TextItem;

public class HUD implements IHUD {

    private static final Font FONT = new Font("Arial", Font.PLAIN, 20);

    private static final String CHARSET = "ISO-8859-1";

    private final List<GameItem> gameItems;

    private final TextItem statusTextItem;

    public HUD(String statusText) throws Exception {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(0.5f, 0.5f, 0.5f, 10f));

        // Create list that holds the items that compose the HUD
        gameItems = new ArrayList<GameItem>();
        gameItems.add(statusTextItem);
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }
    
    @Override
    public List<GameItem> getGameItems() {
        return gameItems;
    }
   
    public void updateSize(Window window) {
        this.statusTextItem.setPosition(10f, window.getHeight() - 50f, 0);
    }
}
