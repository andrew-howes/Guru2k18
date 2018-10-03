import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Arrays;


public class GuruElimChecker {

	static int[] values; 
	static String[] entrants;
	static int[] scores;
	static int[] scenarioScores;
	static ArrayList<String[]> allPicks;
	static String[] scenarioResults;
	static String[] results;
	static String[][] possibleResults;
	static File outFile;
	static String[] closeEntries;
	static int nextMatch;
	static int checkIndex;
	static int[] wrongMatches;
	static String winningScenario;
	static FileWriter writer;
	
	
	public GuruElimChecker(int[] INvalues,String[] INentrants, ArrayList<String[]> INallPicks, String[] INresults, String[][] INpossibleResults,
			int INnextMatch)
	{
		values = INvalues; 
		entrants = INentrants;
		allPicks = INallPicks;
		

		results = INresults;
		possibleResults = INpossibleResults;
		//static File neighbors;
		nextMatch = INnextMatch;
	}
	
	
	public static void main(String[] args) {
		populateValues();
		nextMatch = 0;
		allPicks = new ArrayList<String[]>();
		try {
	        File inFile = new File("allbrackets.txt");
	        String player = "";
//	        if(args.length < 1)
	        	checkIndex = 0;
//	        else
//	        	player = args[0];
//	        
	        
	        //neighbors = new File("neighbors.txt");
	        
	        
	        BufferedReader in = new BufferedReader(new FileReader(inFile));
	        String line;
	        ArrayList<String> players = new ArrayList<String>();
	        int count = 0;
	        while ((line = in.readLine()) != null) {
	            String[] picks = line.split(", ", -1);
	            if(picks[0].equals("ACTUAL"))
	            {
	            	processResults(picks);
	            }else if(picks[0].equals("POSSIBLE"))
	            {
	            	processPossibleResults(picks);
	            }else{
	            	players.add(picks[0]);
	            	if(picks[0].equals(player))
	            	{
	            		checkIndex = count;
	            	}
	            	processPlayer(picks);
	            	count++;
	            }
	        }
	        entrants = new String[count];
	        players.toArray(entrants);
	        in.close();
	    } catch (IOException e) {
	        System.out.println("File Read Error: " + e.getMessage());
	    }
		//scores = calculateScores(results);
		//System.out.println("Current Match: " + nextMatch);
		
//		if(checkIndex == 0)
//		{
//			checkAllPlayers();
//		}
//		else
//		{
//			checkPlayer();
//		}
		
		//outputClosestBrackets();
			if(args.length <= 0)
				checkNext(1,"Spotcheck_");
			else
				checkNext(Integer.parseInt(args[0]),"Spotcheck_");
		/*
		calculateScenarios("");*/
	}
	
	public static void checkNext(int i, String filename)
	{
		String[] possibles = getPossibles(nextMatch);
		for(String poss : possibles)
		{
			possibleResults[nextMatch] = new String[1];
			possibleResults[nextMatch][0] = poss;
			results[nextMatch] = poss;
			scores = calculateScores(results);
			if(i <= 1)
			{
				nextMatch++;
				outFile = new File(filename+poss+".txt");
				//outputClosestBrackets();
				checkAllPlayers();
				nextMatch--;
			}else{
				nextMatch++;
				checkNext(i-1, filename+poss+"+");
				nextMatch--;
			}
		}
		possibleResults[nextMatch] = possibles;
		
	}
	
	
	public static void checkAllPlayers()
	{
		try {
			writer = new FileWriter(outFile);
			for(int i = 0; i < entrants.length; i++)
			{
				checkIndex = i;
				checkPlayer();
			}
			writer.close();
		}catch(IOException e) {
			System.out.println("problem with output");
			//return false;
			//System.exit(1);
		}
	}
	
	
	public static void checkPlayer()
	{
		try {
			
			scenarioResults = new String[35];
			ArrayList<Integer> differences = new ArrayList<Integer>();
			//set scenarioResults to current result or player's bracket when not impossible
			for(int i=0; i < 35; i++)
			{
				if(i < nextMatch){
					scenarioResults[i] = results[i];
				}else{
					if(isValid(allPicks.get(checkIndex)[i],i)){
						scenarioResults[i] = allPicks.get(checkIndex)[i];
					}else{
						scenarioResults[i] = "";
						differences.add(i);
					}
				}
			}
			if(differences.size() == 0)
			{
				if(outputScenarioWinner("any combination+"))
				{
					writer.write("\t"+entrants[checkIndex]+" is ALIVE");
				}else{
					writer.write("\t"+entrants[checkIndex]+" is DEAD");
				}
			}else{
				//find later round matches to iterate through, where the player is wrong
				wrongMatches = new int[differences.size()];


				for(int i = 0; i < wrongMatches.length; i++)
				{
					wrongMatches[i] = differences.get(i).intValue();
				}

				//recurse through results, checking from left-most first. When you reach the end of the list of matches, check scores
				boolean isAlive = checkPlayerHelper(0,"");

				//if player is the winner, end execution, else print scenario and winners
				if(isAlive)
				{
					writer.write("\t"+entrants[checkIndex]+" is ALIVE");
				}else{
					writer.write("\t"+entrants[checkIndex]+" is DEAD");
				}
			}
			writer.write("\n");
			
		}	
		catch (IOException e) {
			System.out.println("problem with output");
			System.exit(1);
		}
	}
	
	public static boolean checkPlayerHelper(int i, String scenario)
	{
		boolean result = false;
		if(i >= wrongMatches.length)
		{
			return outputScenarioWinner(scenario);
		}
		String[] possibles = getPlayerPossibles(wrongMatches[i]);
		
		for(String poss : possibles)
		{
			scenarioResults[wrongMatches[i]] = poss;
			result = checkPlayerHelper(i+1, scenario+poss+"+");
			if(result)
				break;
		}
			//possibleResults[nextMatch] = possibles;
		//if player is the winner, end execution, else print scenario and winners
		return result;
	}
	
	public static boolean outputScenarioWinner(String scene)
	{
		try{
			boolean result = false;
			scores = calculateScores(scenarioResults);
			int maxscore = scores[0];
			for(int i = 1; i < scores.length; i++)
			{
				if(scores[i] > maxscore)
					maxscore = scores[i];
			}
			scene = scene.substring(0,scene.length()-1);
			writer.write("Winner(s) for " + scene +": ");
			for(int j = 0; j < scores.length; j++)
			{
				if(scores[j]==maxscore){
					if(j == checkIndex){
						result = true;
						winningScenario = scene;
					}
					writer.write(entrants[j]+" ");
				}
			}
			writer.write("\n");
			return result;
		}catch(IOException e) {
			System.out.println("problem with output");
			return false;
			//System.exit(1);
		}
	}
	

	
	
//	public static void checkNext(int i, String filename)
//	{
//		String[] possibles = getPossibles(nextMatch);
//		for(String poss : possibles)
//		{
//			possibleResults[nextMatch] = new String[1];
//			possibleResults[nextMatch][0] = poss;
//			results[nextMatch] = poss;
//			scores = calculateScores(results);
//			if(i <= 1)
//			{
//				neighbors = new File(filename+poss+".txt");
//				//outputClosestBrackets();
//			}else{
//				nextMatch++;
//				checkNext(i-1, filename+poss+"+");
//				nextMatch--;
//			}
//		}
//		possibleResults[nextMatch] = possibles;
//		
//	}
	
	public static String[] getPlayerPossibles(int match)
	{
		String[] result;
		int start;
		ArrayList<String> temp = new ArrayList<String>();
		if(match < 28)
		{
			start = (match-20)*2+4;
		}else if(match < 32)
		{
			start = (match-28)*2+20;
		}else if(match < 34)
		{
			start = (match-32)*2+28;
		}else
		{
			start = 32;
		}
		for(int i = start; i < start+2; i++)
		{
			temp.add(scenarioResults[i]);
		}
		result = temp.toArray(new String[temp.size()]);
		
		return result;
	}
	
	
	public static String[] getPossibles(int match)
	{
		String[] result;
		int start;
		if(!possibleResults[match][0].equals(""))
			return possibleResults[match];
		ArrayList<String> temp = new ArrayList<String>();
		if(match < 28)
		{
			start = (match-20)*2+4;
		}else if(match < 32)
		{
			start = (match-28)*2+20;
		}else if(match < 34)
		{
			start = (match-32)*2+28;
		}else
		{
			start = 32;
		}
		for(int i = start; i < start+2; i++)
		{
			if(i < nextMatch)
			{
				temp.add(results[i]);
			}else{
				for(int j = 0; j < possibleResults[i].length; j++)
				{
					temp.add(possibleResults[i][j]);
				}
			}
		}
		result = temp.toArray(new String[temp.size()]);
		
		return result;
	}
	
	public static void populateValues()
	{
		values = new int[35];
		for(int i = 0; i < 35; i++)
		{
			if(i < 20)
				values[i] = 1;
			else if (i < 28)
				values[i] = 2;
			else if (i < 32)
				values[i] = 4;
			else if (i < 34)
				values[i] = 8;
			else 
				values[i] = 16;
		}
	}
	

	

	

	public static boolean isValid(String pick, int matchNum)
	{
		if(matchNum < 20)
		{
			if(matchNum < nextMatch)
			{
				return results[matchNum].equals(pick);
			}
			
			for(int i = 0; i < possibleResults[matchNum].length; i++)
			{
				if(possibleResults[matchNum][i].equals(pick))
					return true;
			}
			return false;
		}else if(matchNum < 28)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-20)*2+4) ||
						isValid(pick, (matchNum-20)*2+5);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 32)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-28)*2+20) ||
						isValid(pick, (matchNum-28)*2+21);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 34)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isValid(pick, (matchNum-32)*2+28) ||
						isValid(pick, (matchNum-32)*2+29);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else
		{
			return isValid(pick, 33)||isValid(pick,32);
		}
	}
	
	//checks if a pick is valid for a given match, given the possible results or the given results.
	public static boolean isPlayerPickValid(String pick, int matchNum)
	{
		if(matchNum < 20)
		{
			if(matchNum < nextMatch)
			{
				return results[matchNum].equals(pick);
			}else{
				return scenarioResults[matchNum].equals(pick);
			}

		}else if(matchNum < 28)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-20)*2+4) ||
						isPlayerPickValid(pick, (matchNum-20)*2+5);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 32)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-28)*2+20) ||
						isPlayerPickValid(pick, (matchNum-28)*2+21);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else if(matchNum < 34)
		{
			if(possibleResults[matchNum][0].equals(""))
				return isPlayerPickValid(pick, (matchNum-32)*2+28) ||
						isPlayerPickValid(pick, (matchNum-32)*2+29);
			else
				return possibleResults[matchNum][0].equals(pick);
		}else
		{
			return isPlayerPickValid(pick, 32)||isPlayerPickValid(pick,33);
		}
	}

	
	public static void processPossibleResults(String[] possible)
	{
		possibleResults = new String[35][0];
		String[] parts;
		for(int i = 0; i < 35; i++)
		{
			parts = possible[i+1].split("; ");
			possibleResults[i] = parts;
		}
	}
	
	public static void processResults(String[] picks)
	{
		results = new String[35];
		for(int i = 1; i < picks.length; i++)
		{
			results[i-1] = picks[i];
			if(picks[i].equals("") && nextMatch == 0)
				nextMatch = i-1;
		}
	}
	
	public static void processPlayer(String[] picks)
	{
		String[] playerPicks = new String[picks.length-1];
		for(int i = 1; i < picks.length-1; i++)
		{
			playerPicks[i-1] = picks[i];
		}
		playerPicks[playerPicks.length-1] = 
				picks[picks.length-1].substring(0,picks[picks.length-1].indexOf(';'));
		allPicks.add(playerPicks);
	}
	
	public static int[] calculateScores(String[] results)
	{
		int[] scores = new int[entrants.length];
		//results = checkResults(preResults);
		for(int i = 0; i < results.length; i++)
		{
			if(!results[i].equals(""))
			{
				//for each player
				for(int j = 0; j < entrants.length; j++)
				{
					//if the player's pick for the match is equal to the result
					if(allPicks.get(j)[i].equals(results[i]))
					{
						//increase their points by the value of the match
						scores[j] += values[i];
					}
				}
			}else{
				break;
			}
		}
		return scores;
	}
}
