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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;

import org.w3c.dom.Element;


/**
 * A FreeCol high score record.
 */
public class HighScore extends FreeColObject {

    private static final Logger logger = Logger.getLogger(HighScore.class.getName());

    /**
     * On retirement, an object will be named in honour of the
     * player.  The nature of the object depends on the player's score.
     */
    public static enum ScoreLevel {
        CONTINENT(40000),
        COUNTRY(35000),
        STATE(30000),
        CITY(25000),
        MOUNTAIN_RANGE(20000),
        RIVER(15000),
        INSTITUTE(12000),
        UNIVERSITY(10000),
        STREET(8000),
        SCHOOL(7000),
        BIRD_OF_PREY(6000),
        TREE(5000),
        FLOWER(4000),
        RODENT(3200),
        FOUL_SMELLING_PLANT(2400),
        POISONOUS_PLANT(1600),
        SLIME_MOLD_BEETLE(800),
        BLOOD_SUCKING_INSECT(400),
        INFECTIOUS_DISEASE(200),
        PARASITIC_WORM(0);

        private int minimumScore;


        ScoreLevel(int minimumScore) {
            this.minimumScore = minimumScore;
        }

        public int getMinimumScore() {
            return minimumScore;
        }
    }

    /** The format to use for dates.  Almost ISO8601. */
    private static final SimpleDateFormat dateFormat
        = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    /** The turn in which independence was granted. */
    private int independenceTurn = -1;

    /** The name of the human player. */
    private String playerName;

    /** The nation that retired. */
    private String nationId;

    /** The nation type that retired. */
    private String nationTypeId;

    /** The high score. */
    private int score;

    /** The ScoreLevel/title for this score. */
    private ScoreLevel level;

    /** The name given to the new independent nation. */
    private String nationName;

    /** The difficulty level of this game. */
    private String difficulty;

    /** The final number of units. */
    private int units;

    /** The final number of colonies. */
    private int colonies;

    /** The name for the New World. */
    private String newLandName;

    /** The date for this score. */
    private Date date;
    
    /** The turn when the player retired. */
    private int retirementTurn;


    /**
     * Create a new high score record.
     *
     * @param player The <code>Player</code> the score is for.
     * @param theDate The <code>Data</code> the score is created.
     */
    public HighScore(Player player, Date theDate) {
        Game game = player.getGame();
        date = theDate;
        retirementTurn = game.getTurn().getNumber();
        score = player.getScore();
        for (ScoreLevel someLevel : ScoreLevel.values()) {
            if (score >= someLevel.getMinimumScore()) {
                level = someLevel;
                break;
            }
        }
        playerName = player.getName();
        nationId = player.getNationId();
        nationTypeId = player.getNationType().getId();
        colonies = player.getColonies().size();
        units = player.getUnits().size();
        if (player.getPlayerType() == Player.PlayerType.INDEPENDENT) {
            independenceTurn = game.getTurn().getNumber();
            nationName = player.getIndependentNationName();
        } else {
            independenceTurn = -1;
        }
        // TODO: how difficult is a custom difficulty?
        difficulty = game.getSpecification().getDifficultyLevel().getId();
        newLandName = player.getNewLandName();
    }

    /**
     * Create a new <code>HighScore</code> by reading a stream.
     *
     * @param xr The <code>FreeColXMLReader</code> to read.
     * @exception XMLStreamException if there is a problem reading the stream.
     */
    public HighScore(FreeColXMLReader xr) throws XMLStreamException {
        readFromXML(xr);
    }

    /**
     * Create a new <code>HighScore</code> by reading an element.
     *
     * @param element The <code>Element</code> to read.
     */
    public HighScore(Element element) {
        readFromXMLElement(element);
    }


    /**
     * Get the turn independence occurs.
     *
     * @return The independence turn.
     */
    public final int getIndependenceTurn() {
        return independenceTurn;
    }

    /**
     * Get the turn the player retired.
     *
     * @return The retirement turn.
     */
    public final int getRetirementTurn() {
        return retirementTurn;
    }

    /**
     * Get the player name.
     *
     * @return The player name.
     */
    public final String getPlayerName() {
        return playerName;
    }

    /**
     * Get the nation identifier.
     *
     * @return The nation identifier.
     */
    public final String getNationId() {
        return nationId;
    }

    /**
     * Get the nation type identifier.
     *
     * @return The nation type identifier.
     */
    public final String getNationTypeId() {
        return nationTypeId;
    }

    /**
     * Get the final score.
     *
     * @return The score.
     */
    public final int getScore() {
        return score;
    }

    /**
     * Get the <code>ScoreLevel</code> corresponding to the score.
     *
     * @return The score level.
     */
    public final ScoreLevel getLevel() {
        return level;
    }

    /**
     * Get the original nation localized name key.
     *
     * @return The old name key.
     */
    public final String getOldNationNameKey() {
        return nationId + ".name";
    }

    /**
     * Get the independent nation name.
     *
     * @return The independent nation name.
     */
    public final String getNationName() {
        return nationName;
    }

    /**
     * Get the name given to the New World.
     *
     * @return The new land name.
     */
    public final String getNewLandName() {
        return newLandName;
    }

    /**
     * Get the game difficulty key.
     *
     * @return The game difficulty key.
     */
    public final String getDifficulty() {
        return difficulty;
    }

    /**
     * Get number of units.
     *
     * @return The number of units.
     */
    public final int getUnits() {
        return units;
    }

    /**
     * Get the number of colonies.
     *
     * @return The number of colonies.
     */
    public final int getColonies() {
        return colonies;
    }

    /**
     * Get the <code>Date</code> the score was achieved.
     *
     * @return The score <code>Date</code>.
     */
    public final Date getDate() {
        return date;
    }


    // Serialization.
    // High scores are only FreeColObjects so that they can be c-s serialized,
    // they do not have ids.


    private static final String COLONIES_TAG = "colonies";
    private static final String DATE_TAG = "date";
    private static final String DIFFICULTY_TAG = "difficulty";
    private static final String INDEPENDENCE_TURN_TAG = "independenceTurn";
    private static final String LEVEL_TAG = "level";
    private static final String NATION_ID_TAG = "nationId";
    private static final String NATION_NAME_TAG = "nationName";
    private static final String NATION_TYPE_ID_TAG = "nationTypeId";
    private static final String NEW_LAND_NAME_TAG = "newLandName";
    private static final String PLAYER_NAME_TAG = "playerName";
    private static final String RETIREMENT_TURN_TAG = "retirementTurn";
    private static final String SCORE_TAG = "score";
    private static final String UNITS_TAG = "units";
    // @compat 0.10.7
    private static final String OLD_NATION_ID_TAG = "nationID";
    private static final String OLD_NATION_TYPE_ID_TAG = "nationTypeID";
    // end @compat


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        // HighScores do not have ids, no super.writeAttributes().

        xw.writeAttribute(DATE_TAG, dateFormat.format(date));

        xw.writeAttribute(RETIREMENT_TURN_TAG, retirementTurn);

        xw.writeAttribute(INDEPENDENCE_TURN_TAG, independenceTurn);

        xw.writeAttribute(PLAYER_NAME_TAG, playerName);

        xw.writeAttribute(NATION_ID_TAG, nationId);

        xw.writeAttribute(NATION_TYPE_ID_TAG, nationTypeId);

        xw.writeAttribute(SCORE_TAG, score);

        xw.writeAttribute(LEVEL_TAG, level.toString());

        if (nationName != null) {
            xw.writeAttribute(NATION_NAME_TAG, nationName);
        }

        if (newLandName != null) {
            xw.writeAttribute(NEW_LAND_NAME_TAG, newLandName);
        }

        xw.writeAttribute(DIFFICULTY_TAG, difficulty);

        xw.writeAttribute(UNITS_TAG, units);

        xw.writeAttribute(COLONIES_TAG, colonies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        // HighScores do not have ids, no super.readAttributes().

        String str = xr.getAttribute(DATE_TAG, "2008-01-01 00:00:00+0000");
        try {
            date = dateFormat.parse(str);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Bad date: " + str, e);
            date = new Date();
        }

        retirementTurn = xr.getAttribute(RETIREMENT_TURN_TAG, 0);

        independenceTurn = xr.getAttribute(INDEPENDENCE_TURN_TAG, 0);

        playerName = xr.getAttribute(PLAYER_NAME_TAG, "anonymous");

        nationId = xr.getAttribute(NATION_ID_TAG,
            // @compat 0.10.7
            xr.getAttribute(OLD_NATION_ID_TAG,
            // end @compat
                "model.nation.dutch"));

        nationTypeId = xr.getAttribute(NATION_TYPE_ID_TAG,
            // @compat 0.10.7
            xr.getAttribute(OLD_NATION_TYPE_ID_TAG,
            // end @compat
                "model.nationType.trade"));

        score = xr.getAttribute(SCORE_TAG, 0);

        level = xr.getAttribute(LEVEL_TAG, ScoreLevel.class,
                                ScoreLevel.PARASITIC_WORM);

        nationName = xr.getAttribute(NATION_NAME_TAG, "Freedonia");

        newLandName = xr.getAttribute(NEW_LAND_NAME_TAG, "New World");
        
        difficulty = xr.getAttribute(DIFFICULTY_TAG,
                                     "model.difficulty.medium");

        units = xr.getAttribute(UNITS_TAG, 0);

        colonies = xr.getAttribute(COLONIES_TAG, 0);
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return getXMLElementTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "highScore".
     */
    public static String getXMLElementTagName() {
        return "highScore";
    }
}
