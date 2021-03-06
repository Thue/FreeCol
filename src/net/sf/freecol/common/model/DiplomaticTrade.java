/**
 *  Copyright (C) 2002-2013   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.model.Player.Stance;

import org.w3c.dom.Element;


/**
 * The class <code>DiplomaticTrade</code> represents an offer one player can
 * make another.
 */
public class DiplomaticTrade extends FreeColObject {

    /** A type for the trade status. */
    public static enum TradeStatus {
        PROPOSE_TRADE,
        ACCEPT_TRADE,
        REJECT_TRADE
    }


    /** The game in play. */
    private final Game game;

    /** The player who proposed agreement. */
    private Player sender;

    /** The player who is to accept this agreement. */
    private Player recipient;

    /** The status of this agreement. */
    private TradeStatus status;

    /** The individual items the trade consists of. */
    private final List<TradeItem> items = new ArrayList<TradeItem>();

    /** Counter for the number of iterations on this attempt to agree. */
    private int version;


    /**
     * Creates a new <code>DiplomaticTrade</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param sender The sending <code>Player</code>.
     * @param recipient The recipient <code>Player</code>.
     * @param items A list of items to trade.
     */
    public DiplomaticTrade(Game game, Player sender, Player recipient,
                           List<TradeItem> items, int version) {
        setId("");
        this.game = game;
        this.sender = sender;
        this.recipient = recipient;
        this.status = TradeStatus.PROPOSE_TRADE;
        this.items.clear();
        if (items != null) this.items.addAll(items);
        this.version = version;
    }

    /**
     * Creates a new <code>DiplomaticTrade</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param element an <code>Element</code> value
     */
    public DiplomaticTrade(Game game, Element element) {
        this.game = game;

        readFromXMLElement(element);
    }


    /**
     * Get the trade status.
     *
     * @return The status of this agreement.
     */
    public TradeStatus getStatus() {
        return status;
    }

    /**
     * Set the trade status.
     *
     * @param status The new <code>TradeStatus</code> for this agreement.
     */
    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    /**
     * Get the sending player.
     *
     * @return The sending <code>Player</code>.
     */
    public final Player getSender() {
        return sender;
    }

    /**
     * Set the sending player.
     *
     * @param newSender The new sending <code>Player</code>.
     */
    public final void setSender(final Player newSender) {
        this.sender = newSender;
    }

    /**
     * Get the recipient player.
     *
     * @return The recipient <code>Player</code>.
     */
    public final Player getRecipient() {
        return recipient;
    }

    /**
     * Set the recieving player.
     *
     * @param newRecipient The new recipient <code>Player</code>.
     */
    public final void setRecipient(final Player newRecipient) {
        this.recipient = newRecipient;
    }

    /**
     * Get the other player in a trade.
     *
     * @param player The known <code>Player</code>.
     * @return The other player, not the supplied known one.
     */
    public Player getOtherPlayer(Player player) {
        return (sender == player) ? recipient : sender;
    }

    /**
     * Add to the DiplomaticTrade.
     *
     * @param newItem The <code>TradeItem</code> to add.
     */
    public void add(TradeItem newItem) {
        if (newItem.isUnique()) {
            removeType(newItem);
        }
        items.add(newItem);
    }

    /**
     * Remove a from the DiplomaticTrade.
     *
     * @param newItem The <code>TradeItem</code> to remove.
     */
    public void remove(TradeItem newItem) {
        items.remove(newItem);
    }

    /**
     * Remove from the DiplomaticTrade.
     *
     * @param index The index of the <code>TradeItem</code> to remove
     */
    public void remove(int index) {
        items.remove(index);
    }

    /**
     * Removes all trade items of the same class as the given argument.
     *
     * @param someItem a <code>TradeItem</code> value
     */
    public void removeType(TradeItem someItem) {
        Iterator<TradeItem> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            if (itemIterator.next().getClass() == someItem.getClass()) {
                itemIterator.remove();
            }
        }
    }

    /**
     * Get a list of all items to trade.
     *
     * @return A list of all the TradeItems.
     */
    public final List<TradeItem> getTradeItems() {
        return items;
    }

    /**
     * Get an iterator for all the TradeItems.
     *
     * @return An iterator for all TradeItems.
     */
    public Iterator<TradeItem> iterator() {
        return items.iterator();
    }

    /**
     * Get the items offered by a particular player.
     *
     * @param player The <code>Player</code> to check.
     * @return A list of <code>TradeItem</code>s offered by the player.
     */
    public List<TradeItem> getItemsGivenBy(Player player) {
        List<TradeItem> goodsList = new ArrayList<TradeItem>();
        for (TradeItem ti : items) {
            if (player == ti.getSource()) goodsList.add(ti);
        }
        return goodsList;
    }

    /**
     * Get the stance being offered.
     *
     * @return The <code>Stance</code> offered in this trade, or null if none.
     */
    public Stance getStance() {
        for (TradeItem ti : items) {
            if (ti instanceof StanceTradeItem) {
                return ((StanceTradeItem)ti).getStance();
            }
        }
        return null;
    }

    /**
     * Get a list of colonies offered in this trade.
     *
     * @return A list of <code>Colony</code>s offered in this trade.
     */
    public List<Colony> getColoniesGivenBy(Player player) {
        List<Colony> colonyList = new ArrayList<Colony>();
        for (TradeItem ti : items) {
            if (ti instanceof ColonyTradeItem && player == ti.getSource()) {
                colonyList.add(((ColonyTradeItem)ti).getColony());
            }
        }
        return colonyList;
    }

    /**
     * Get the gold offered in this trade by a given player.
     *
     * @param player The <code>Player</code> to check.
     * @return The gold offered in this trade.
     */
    public int getGoldGivenBy(Player player) {
        for (TradeItem ti : items) {
            if (ti instanceof GoldTradeItem && player == ti.getSource()) {
                return ((GoldTradeItem)ti).getGold();
            }
        }
        return -1;
    }

    /**
     * Get the goods being offered.
     *
     * @return A list of <code>Goods</code> offered in this trade.
     */
    public List<Goods> getGoodsGivenBy(Player player) {
        List<Goods> goodsList = new ArrayList<Goods>();
        for (TradeItem ti : items) {
            if (ti instanceof GoodsTradeItem && player == ti.getSource()) {
                goodsList.add(((GoodsTradeItem)ti).getGoods());
            }
        }
        return goodsList;
    }

    /**
     * Get a list of units offered in this trade.
     *
     * @return A list of <code>Unit</code>s offered in this trade.
     */
    public List<Unit> getUnitsGivenBy(Player player) {
        List<Unit> unitList = new ArrayList<Unit>();
        for (TradeItem ti : items) {
            if (ti instanceof UnitTradeItem && player == ti.getSource()) {
                unitList.add(((UnitTradeItem)ti).getUnit());
            }
        }
        return unitList;
    }

    /**
     * Gets the version of this agreement.
     *
     * @return The version number.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Increment the version of this agreement.
     */
    public void incrementVersion() {
        this.version++;
    }


    // Serialization

    private static final String RECIPIENT_TAG = "recipient";
    private static final String SENDER_TAG = "sender";
    private static final String STATUS_TAG = "status";
    private static final String VERSION_TAG = "version";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        xw.writeAttribute(SENDER_TAG, sender);

        xw.writeAttribute(RECIPIENT_TAG, recipient);

        xw.writeAttribute(STATUS_TAG, status);

        xw.writeAttribute(VERSION_TAG, version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        for (TradeItem item : items) item.toXML(xw);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        sender = xr.getAttribute(game, SENDER_TAG,
                                 Player.class, (Player)null);

        recipient = xr.getAttribute(game, RECIPIENT_TAG,
                                    Player.class, (Player)null);

        status = xr.getAttribute(STATUS_TAG, TradeStatus.class,
                                 TradeStatus.REJECT_TRADE);

        version = xr.getAttribute(VERSION_TAG, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChildren(FreeColXMLReader xr) throws XMLStreamException {
        // Clear containers.
        items.clear();

        super.readChildren(xr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final String tag = xr.getLocalName();

        if (ColonyTradeItem.getXMLElementTagName().equals(tag)) {
            add(new ColonyTradeItem(game, xr));

        } else if (GoldTradeItem.getXMLElementTagName().equals(tag)) {
            add(new GoldTradeItem(game, xr));

        } else if (GoodsTradeItem.getXMLElementTagName().equals(tag)) {
            add(new GoodsTradeItem(game, xr));

        } else if (StanceTradeItem.getXMLElementTagName().equals(tag)) {
            add(new StanceTradeItem(game, xr));

        } else if (UnitTradeItem.getXMLElementTagName().equals(tag)) {
            add(new UnitTradeItem(game, xr));

        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return getXMLElementTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "diplomaticTrade".
     */
    public static String getXMLElementTagName() {
        return "diplomaticTrade";
    }
}
