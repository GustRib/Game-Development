package com.donos.zebra.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemDefinition;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.items.SellPrices;
import com.donos.zebra.items.ShopCatalog;
import com.donos.zebra.items.Wallet;

/**
 * Mentor shop — Comprar (potions) / Vender (ores + crafted gear), one unit at a time.
 */
public class ShopUI extends GameWindow {

    private enum Mode { BUY, SELL }

    private final Skin skin;
    private final AssetManager assetManager;
    private final Label statusLabel;
    private final CurrencyReadout currencyReadout;
    private final Table offerTable;
    private final ScrollPane scrollPane;
    private final TextButton buyTab;
    private final TextButton sellTab;
    private Player boundPlayer;
    private Mode mode = Mode.BUY;
    private Runnable onOpened;
    private Runnable onInventoryChanged;
    private java.util.function.Consumer<String> toast;

    public ShopUI(Skin skin, AssetManager assetManager) {
        super("Loja", skin);
        this.skin = skin;
        this.assetManager = assetManager;
        ensureStyles(skin);

        setResizable(false);

        offerTable = new Table();
        offerTable.top().left().pad(4);

        currencyReadout = new CurrencyReadout(skin, assetManager);

        statusLabel = new Label("Compre pocoes com Prata.", skin);
        statusLabel.setWrap(true);

        scrollPane = new ScrollPane(offerTable, skin);
        scrollPane.setFadeScrollBars(false);

        buyTab = new TextButton("Comprar", skin, "shop-tab");
        sellTab = new TextButton("Vender", skin, "shop-tab");
        buyTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mode = Mode.BUY;
                statusLabel.setText("Compre pocoes com Prata.");
                statusLabel.setColor(Color.WHITE);
                rebuild();
            }
        });
        sellTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mode = Mode.SELL;
                statusLabel.setText("Venda minerios e equipamentos forjados (1x).");
                statusLabel.setColor(Color.WHITE);
                rebuild();
            }
        });

        Table tabs = new Table();
        tabs.add(buyTab).height(28).growX().padRight(4);
        tabs.add(sellTab).height(28).growX();

        Table body = new Table();
        body.add(currencyReadout).growX().pad(4).row();
        body.add(tabs).growX().pad(4).row();
        body.add(scrollPane).grow().row();
        body.add(statusLabel).growX().pad(6).row();
        Label help = new Label("[E/ESPACO] Fechar", skin);
        help.setColor(Color.LIGHT_GRAY);
        body.add(help).padBottom(4).left();

        add(body).grow();
        float maxH = Math.min(400f, Gdx.graphics.getHeight() * 0.75f);
        float maxW = Math.min(360f, Gdx.graphics.getWidth() * 0.55f);
        setSize(maxW, maxH);
    }

    public void setOnOpened(Runnable onOpened) {
        this.onOpened = onOpened;
    }

    public void setOnInventoryChanged(Runnable onInventoryChanged) {
        this.onInventoryChanged = onInventoryChanged;
    }

    public void setToast(java.util.function.Consumer<String> toast) {
        this.toast = toast;
    }

    public void open(Player player) {
        this.boundPlayer = player;
        this.mode = Mode.BUY;
        statusLabel.setText("Compre pocoes com Prata.");
        statusLabel.setColor(Color.WHITE);
        float maxH = Math.min(400f, Gdx.graphics.getHeight() * 0.75f);
        float maxW = Math.min(360f, Gdx.graphics.getWidth() * 0.55f);
        setSize(maxW, maxH);
        setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);
        rebuild();
        openPanel();
        if (onOpened != null) {
            onOpened.run();
        }
    }

    @Override
    public void closePanel() {
        boundPlayer = null;
        super.closePanel();
    }

    public void rebuild() {
        offerTable.clearChildren();
        if (boundPlayer == null) {
            return;
        }
        currencyReadout.refresh(boundPlayer.getWallet());
        float contentWidth = Math.max(180f, getWidth() - getPadLeft() - getPadRight() - 28f);

        if (mode == Mode.BUY) {
            Wallet wallet = boundPlayer.getWallet();
            for (ShopCatalog.Offer offer : ShopCatalog.potionOffers()) {
                offerTable.add(buildBuyRow(offer, wallet, contentWidth)).width(contentWidth).growX().padBottom(6).row();
            }
        } else {
            Inventory inventory = boundPlayer.getInventory();
            java.util.LinkedHashMap<ItemDefinition, Integer> sellable = new java.util.LinkedHashMap<>();
            for (ItemStack stack : inventory.getSlots()) {
                if (stack == null || !SellPrices.isSellable(stack.getDefinition())) {
                    continue;
                }
                sellable.merge(stack.getDefinition(), stack.getQuantity(), Integer::sum);
            }
            if (sellable.isEmpty()) {
                Label empty = new Label("Nada vendavel no inventario.", skin);
                empty.setColor(Color.LIGHT_GRAY);
                offerTable.add(empty).left().pad(8).row();
            } else {
                for (java.util.Map.Entry<ItemDefinition, Integer> entry : sellable.entrySet()) {
                    offerTable.add(buildSellRow(entry.getKey(), entry.getValue(), contentWidth))
                        .width(contentWidth).growX().padBottom(6).row();
                }
            }
        }
        offerTable.pack();
        scrollPane.layout();
    }

    private Table buildBuyRow(ShopCatalog.Offer offer, Wallet wallet, float width) {
        Table row = new Table();
        row.setBackground(solid(new Color(0.18f, 0.18f, 0.2f, 0.95f)));
        row.pad(6);
        row.left();

        boolean canAfford = wallet.canAfford(offer.priceSilver);

        Table header = new Table();
        Image icon = createIcon(offer.item.getIconPath(), canAfford);
        if (icon != null) {
            header.add(icon).size(24, 24).padRight(6);
        }
        Label name = new Label(offer.item.getName(), skin);
        name.setColor(canAfford ? Color.WHITE : Color.GRAY);
        name.setWrap(true);
        header.add(name).growX().left();
        row.add(header).width(width - 12f).growX().row();

        Label meta = new Label(
            "+" + offer.item.getHealAmount() + " HP  |  "
                + offer.priceSilver + " Prata"
                + (canAfford ? "" : "  (falta dinheiro)"),
            skin);
        meta.setWrap(true);
        meta.setColor(canAfford ? new Color(0.7f, 0.85f, 0.7f, 1f) : Color.DARK_GRAY);
        row.add(meta).width(width - 12f).growX().padTop(2).left().row();

        String btnText = canAfford ? "COMPRAR" : "FALTA DINHEIRO";
        TextButton buy = new TextButton(btnText, skin, "shop-buy");
        buy.setDisabled(!canAfford);
        if (!canAfford) {
            buy.setColor(Color.DARK_GRAY);
        }
        buy.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tryBuy(offer);
            }
        });
        row.add(buy).padTop(4).height(28).growX();
        return row;
    }

    private Table buildSellRow(ItemDefinition item, int ownedQty, float width) {
        Table row = new Table();
        row.setBackground(solid(new Color(0.18f, 0.18f, 0.2f, 0.95f)));
        row.pad(6);
        row.left();

        int price = SellPrices.unitSellPrice(item);

        Table header = new Table();
        Image icon = createIcon(item.getIconPath(), true);
        if (icon != null) {
            header.add(icon).size(24, 24).padRight(6);
        }
        Label name = new Label(item.getName() + "  x" + ownedQty, skin);
        name.setColor(Color.WHITE);
        name.setWrap(true);
        header.add(name).growX().left();
        row.add(header).width(width - 12f).growX().row();

        Label meta = new Label("Vende por " + price + " Prata (1x)", skin);
        meta.setColor(new Color(0.7f, 0.85f, 0.7f, 1f));
        row.add(meta).width(width - 12f).growX().padTop(2).left().row();

        TextButton sell = new TextButton("VENDER 1", skin, "shop-buy");
        sell.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                trySell(item);
            }
        });
        row.add(sell).padTop(4).height(28).growX();
        return row;
    }

    private void tryBuy(ShopCatalog.Offer offer) {
        if (boundPlayer == null) {
            return;
        }
        if (!boundPlayer.getWallet().canAfford(offer.priceSilver)) {
            statusLabel.setText("Falta dinheiro.");
            statusLabel.setColor(Color.ORANGE);
            if (toast != null) {
                toast.accept("Falta dinheiro");
            }
            rebuild();
            return;
        }
        boolean ok = ShopCatalog.tryBuy(offer, boundPlayer.getWallet(), boundPlayer.getInventory());
        if (ok) {
            statusLabel.setText("Comprado: " + offer.item.getName());
            statusLabel.setColor(Color.GREEN);
            if (toast != null) {
                toast.accept("Comprado: " + offer.item.getName());
            }
            if (onInventoryChanged != null) {
                onInventoryChanged.run();
            }
        } else {
            statusLabel.setText("Inventario cheio.");
            statusLabel.setColor(Color.RED);
        }
        rebuild();
    }

    private void trySell(ItemDefinition item) {
        if (boundPlayer == null) {
            return;
        }
        int price = SellPrices.unitSellPrice(item);
        boolean ok = SellPrices.trySellOne(item, boundPlayer.getInventory(), boundPlayer.getWallet());
        if (ok) {
            statusLabel.setText("Vendido: " + item.getName() + " (+" + price + " Prata)");
            statusLabel.setColor(Color.GREEN);
            if (toast != null) {
                toast.accept("+" + price + " Prata");
            }
            if (onInventoryChanged != null) {
                onInventoryChanged.run();
            }
        } else {
            statusLabel.setText("Nao foi possivel vender.");
            statusLabel.setColor(Color.ORANGE);
        }
        rebuild();
    }

    private Image createIcon(String path, boolean enabled) {
        if (path == null || !assetManager.isLoaded(path)) {
            return null;
        }
        Image image = new Image(assetManager.get(path, Texture.class));
        if (!enabled) {
            image.setColor(0.45f, 0.45f, 0.45f, 1f);
        }
        return image;
    }

    private static void ensureStyles(Skin skin) {
        if (!skin.has("shop-buy", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.up = solid(new Color(0.25f, 0.4f, 0.25f, 1f));
            style.down = solid(new Color(0.15f, 0.28f, 0.15f, 1f));
            style.over = solid(new Color(0.32f, 0.5f, 0.32f, 1f));
            style.disabledFontColor = Color.DARK_GRAY;
            skin.add("shop-buy", style);
        }
        if (!skin.has("shop-tab", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle tab = new TextButton.TextButtonStyle();
            tab.font = skin.getFont("default");
            tab.fontColor = Color.WHITE;
            tab.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            tab.down = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            tab.checked = solid(new Color(0.4f, 0.35f, 0.2f, 1f));
            tab.over = solid(new Color(0.3f, 0.3f, 0.36f, 1f));
            skin.add("shop-tab", tab);
        }
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
