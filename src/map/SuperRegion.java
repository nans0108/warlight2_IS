/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package map;
import java.util.LinkedList;
import bot.BotState;

public class SuperRegion {
	
	private int id;
	private int armiesReward;
	private LinkedList<Region> subRegions;
	
	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}
	
	public void addSubRegion(Region subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for(Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}
	
	/**
	 * @return score of superRegion's region
	 */
	public double score(BotState state)
	{
        int numberOfArmies = 0;
        boolean isWasteland = false;
        double score = 0;

        for(Region subRegion : subRegions) {
        	Region region;
        	if (state.getVisibleMap() == null) {
        		 region = null;
        	} else {
                 region =  state.getVisibleMap().getRegion(subRegion.getId());
        	}
        	
            if (region == null) {
                for(Region wasteland : state.getWasteLands()) {
                    if (wasteland.getId() == subRegion.getId()) {
                        isWasteland = true;
                        break;
                    }
                }
                if (isWasteland) {
                	numberOfArmies += 6;
                } else {
                	numberOfArmies += 2;
                }
            } else if (!subRegion.getPlayerName().equals(state.getMyPlayerName())) {
            	numberOfArmies += subRegion.getArmies();
            	
            }
        }
        
        if(numberOfArmies == 0){
        	score = armiesReward;
        } else {
        	score = (double)armiesReward / numberOfArmies;
        }
 
        return score;
    }
	
}
