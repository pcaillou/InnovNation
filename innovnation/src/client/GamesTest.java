package client;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import data.IPlayer;
import functions.Game;
import functions.GameDescription;
import functions.IGame;

public class GamesTest {


	private final static Logger logger = Logger.getLogger("test");



	public static void main(String[] args) {
		boolean tricky = false;
		try {
			logger.info("game creation...");
			IGame game = new Game(new GameDescription("toto", "tutu", "theme"));
			logger.info("done");
			
			if(tricky){
				logger.info("data manipulation...");
				simulateData(game);
				logger.info("done");
			} else {
				logger.info("local game creation...");
				IGame localGame = LocalCopyOfGame.getOrCreateLocalCopy(game);
				logger.info("done");
	
				logger.info("data manipulation...");
				simulateData(localGame);
				logger.info("done");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("failed");
		} finally {
			Runtime.getRuntime().exit(0);
		}
	}

	public static int makePlayer(IGame game, String name, String iconName) throws RemoteException{
		int id = game.addPlayer(name);
		IPlayer player = game.getPlayer(id);
		try{
			player.setPicturePath("."+File.separator+"avatars"+File.separator+iconName);
		} catch (NullPointerException e){
			logger.error("error while setting player icon for "+name+":"+e);
		}
		return id;
	}

	public static int makeIdea(IGame game, int authorId, String name, String ideaDesc, Collection<Integer> itemList, Collection<Integer> parentIds) throws RemoteException{
		int id = game.addIdea(authorId, name, ideaDesc, itemList, parentIds);
		return id;
	}
	
	public static void simulateData(IGame game) {
		int p1, p2;
		try {
			p1 = makePlayer(game, "Pierre", "pierre_gmail.png");
			logger.info("new player: "+game.getPlayer(p1));

			p2 = makePlayer(game, "Sam", "sam_facebook.jpg");
			logger.info("new player: "+game.getPlayer(p2));
			
			LinkedList<Integer> allItems = new LinkedList<Integer>();
			LinkedList<Integer> someItems = new LinkedList<Integer>();

			for (int i=0; i<5; i++) {
				int idea1 = game.addItem(p1, "item "+i, "description "+i);
				allItems.add(idea1);
				if (i%2 == 0) 
					someItems.add(idea1);
			}

			int idea1id = makeIdea(game, p1, "idea1", "testdesc1", someItems, Collections.<Integer>emptyList());
			logger.info("new idea: "+game.getIdea(idea1id));
			
			LinkedList<Integer> parentIds = new LinkedList<Integer>();
			parentIds.add(idea1id);
			
			int idea2id = makeIdea(game, p2, "idea2", "testdesc2", allItems, parentIds);
			logger.info("new idea: "+game.getIdea(idea2id));
			
			int idea3id = makeIdea(game, p1, "idea3", "testdesc3", allItems, parentIds);
			logger.info("new idea: "+game.getIdea(idea3id));
			
		} catch (RemoteException e) {
			logger.error(e);
		}

	}
}
