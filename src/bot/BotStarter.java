/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

//LUCAfmkfdmgredk
package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable â€œstateâ€�.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{

//int k=0;
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut)
	{
		
        double maxScore = 0;
        double score = 0;
        
        Integer bestRegion = null;

		for(Region region : state.getPickableStartingRegions()) {
            score = region.getSuperRegion().score(state);
            if ((bestRegion == null || score > maxScore)){
            	maxScore = score;
                bestRegion = region.getId();
            }
        }

		Region startingRegion = state.getFullMap().getRegion(bestRegion);
		
		return startingRegion;
		
//		double rand = Math.random();
//		int r = (int) (rand*state.getPickableStartingRegions().size());
//		int regionId = state.getPickableStartingRegions().get(r).getId();
//		Region startingRegion = state.getFullMap().getRegion(regionId);
//		
		//return startingRegion;
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		String opponentName = state.getOpponentPlayerName();
		int armiesLeft = state.getStartingArmies();
		int armies = armiesLeft;
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		
		
		
		while(armiesLeft > 0)
		{
			int numberOfArmies = 0;
			int maxNumberOfArmies = 0;
			double score = 0;
			double bestScore = 0;

			Region regionToPutArmies = null;
			
			//!!!put armies on a region next to your opponent with the minimum number of armies
			for( Region region : visibleRegions) {
				if (region.ownedByPlayer(myName)) {
					score = region.getSuperRegion().score(state);
					for(Region neighbor : region.getNeighbors()) {
						if(neighbor.ownedByPlayer(opponentName) && score > bestScore) {
							bestScore = score;
							regionToPutArmies = region;						} 
					}
				}
			}
			
			if(regionToPutArmies == null) {
				for( Region region : visibleRegions) {
					if (region.ownedByPlayer(myName)){
						numberOfArmies = region.getArmies();
						if(numberOfArmies >= maxNumberOfArmies) {
							regionToPutArmies = region;
							maxNumberOfArmies = numberOfArmies;
						}
					}
				}
			}	
			
			placeArmiesMoves.add(new PlaceArmiesMove(myName, regionToPutArmies, armies));
			armiesLeft -= armies;
			
//			double rand = Math.random();
//			int r = (int) (rand*visibleRegions.size());
//			Region region = visibleRegions.get(r);
//			
//			if(region.ownedByPlayer(myName))
//			{
//				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
//				armiesLeft -= armies;
//			}
		}
		
		return placeArmiesMoves;
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armies = 5;
		//int maxTransfers = 10;
		//int transfers = 0;
		
		
//		for(Region fromRegion : state.getVisibleMap().getRegions())
//		{
//			if(fromRegion.ownedByPlayer(myName))
//			{
//				
//			}
//		}
		
		LinkedList<Region> allRegions = state.getVisibleMap().getRegions();
		
		for(Region fromRegion : allRegions)
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				List<Region> possibleToRegions = new CopyOnWriteArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				
				while(!possibleToRegions.isEmpty())
				{
					
                    Region toRegion = possibleToRegions.get(0);
                    
                    //!!!delete possibilitie of stupid transfers between yours regions
                    if (toRegion.getPlayerName().equals(myName) && !toRegion.nextToOpponent(state)) {
                        possibleToRegions.remove(toRegion);
                        continue;
                    }
                    
                    
                    if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies()>=2 && goodToAttack(fromRegion, toRegion)) //do an attack
					{
						armies = fromRegion.getArmies() - 1;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
                    
                    //!!!this is the part that I was talking about to make the transfer of armies from the region that are not next to the opponent to the regions that are.
					else if(toRegion.getPlayerName().equals(myName) && toRegion.nextToOpponent(state) && !fromRegion.nextToOpponent(state) && fromRegion.getArmies()>=2 ) //do a transfer
					{
						armies = fromRegion.getArmies() - 1;
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else{
						possibleToRegions.remove(toRegion);
					}
				}				
			}
			if(!attackTransferMoves.isEmpty()) break;
		}
		
		return attackTransferMoves;
	}
	
	
	private boolean goodToAttack (Region fromRegion, Region toRegion) {
		
		int possibleDestroyedArmies = fromRegion.getArmies();
		
		if(possibleDestroyedArmies > toRegion.getArmies()) {
			return true;
		}
		else{
			return false;
		}
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
