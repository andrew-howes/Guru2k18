import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class Guru {

	static int[] values;
	static String[] entrants;
	static int[] scores;
	static ArrayList<String[]> allPicks;
	static String[] results;
	static String[][] possibleResults;
	static File neighbors; 
	static int nextMatch;
	
	
	//main execution thread - initializes list of brackets, starts output. 
	//input argument (optional): how many matches to check (int), defaults to 1
	public static void main(String[] args) {
		populateValues();
		nextMatch = 0;
		allPicks = new ArrayList<String[]>();
		try {
			//changed default bracket file to allbrackets.txt
	        File inFile = new File("allbrackets.txt");
	        
	        neighbors = new File("neighbors.txt");
	        
	        BufferedReader in = new BufferedReader(new FileReader(inFile));
	        String line;
	        ArrayList<String> players = new ArrayList<String>();
	        int count = 0;
	        while ((line = in.readLine()) != null) {
	            String[] picks = line.split(", ", -1);
	            //master results bracket
	            if(picks[0].equals("ACTUAL"))
	            {
	            	processResults(picks);
	            }//possible results bracket - only really matters for round 1.
	            else if(picks[0].equals("POSSIBLE"))
	            {
	            	processPossibleResults(picks);
	            }else{
	            	players.add(picks[0]);
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
		scores = calculateScores(results);
		System.out.println("Current Match: " + nextMatch + " Remaining Brackets: " + entrants.length);
		outputClosestBrackets();
		
		//How many matches to check - default is 1
		if(args.length <= 0)
			checkNext(1,"");
		else
			checkNext(Integer.parseInt(args[0]),"");
		
		//calculateScenarios("");
	}
	
	//simulates the next 'i' matches to find eliminations
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
				neighbors = new File(filename+poss+".txt");
				outputClosestBrackets();
				nextMatch--;
			}else{
				nextMatch++;
				checkNext(i-1, filename+poss+"+");
				nextMatch--;
			}
		}
		possibleResults[nextMatch] = possibles;
		
	}
	
	//calculates the winners for the remaining matches.
	//when simulating multiple matches at once, *scene* will contain a plus-delimited list of simulated winners to this point.
	public static void calculateScenarios(String scene)
	{
		String[] possibles = getPossibles(nextMatch);
		for(String poss : possibles)
		{
			possibleResults[nextMatch] = new String[1];
			possibleResults[nextMatch][0] = poss;
			results[nextMatch] = poss;
			scores = calculateScores(results);
			//if the current match is the final, print the winner(s), else continue to iterate.
			if(nextMatch == 34)
			{
				String newScene = scene+poss;
				outputWinner(newScene);
			}else{
				nextMatch++;
				calculateScenarios(scene+poss+"+");
				nextMatch--;
			}
		}
		possibleResults[nextMatch] = new String[possibles.length];
		possibleResults[nextMatch] = possibles;
	}
	
	//outputs the winner(s) for a given scenario.
	public static void outputWinner(String scene)
	{
		int maxscore = scores[0];
		for(int i = 1; i < scores.length; i++)
		{
			if(scores[i] > maxscore)
				maxscore = scores[i];
		}
		System.out.print("Winner(s) for " + scene +": ");
		for(int j = 0; j < scores.length; j++)
		{
			if(scores[j]==maxscore)
				System.out.print(entrants[j]+" ");
		}
		System.out.println();
	}
	
	//gets the list of possible winners for a given match
	//assumes that the previous matches have been played or simulated at this point. 
	public static String[] getPossibles(int match)
	{
		String[] result;
		int start;
		
		//if this is in the playins/first round, take the possible results directly from the possible results bracket.
		if(!possibleResults[match][0].equals(""))
			return possibleResults[match];
		ArrayList<String> temp = new ArrayList<String>();
		//for matches 21-28 (zero-indexed)
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
	
	//create the list of point values for a given match number.
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
	
	//output the closest brackets for each entrant, and prints the eliminations given a specific result.
	public static void outputClosestBrackets()
	{
		try {
			FileWriter writer = new FileWriter(neighbors);
			
			String winner = neighbors.getName();
			
			winner = winner.substring(0,winner.indexOf("."));
			if(! winner.equals("neighbors"))
				System.out.println("Elims for a "+winner+" win:");
			
			writer.write("<span class=\"nocode\">\n");
			writer.write("updated through "+results[nextMatch-1]+"'s win\n");
			int[][] comparisons;
			int minscore;
			String out;
			ArrayList<Integer> minIDs = new ArrayList<Integer>();
			int[] diffmatches;
			boolean hasPrinted = false;
			for(int player = 0; player < entrants.length; player++)
			{
				comparisons = new int[entrants.length][3];
				for(int second = 0; second < entrants.length; second++)
				{
					comparisons[second] = getDifferenceScore(player, second);
				}
				minscore = 384;
				minIDs.clear();
				for(int i = 0; i < entrants.length; i++)
				{
					if(i != player)
					{
						//if(comparisons[i][1] < minscore)
						//if((scores[i]-scores[player]) + comparisons[i][2] < minscore)
						if((comparisons[i][2]-(scores[i]-scores[player])) < 5 ||
								(scores[player]-scores[i]) + comparisons[i][2] < minscore)
						{
							if(minscore > 5)
								minIDs.clear();
							//minscore = comparisons[i][1];
							if(comparisons[i][2]-(scores[i]-scores[player]) < minscore)
								minscore = (comparisons[i][2]-(scores[i]-scores[player]));
							minIDs.add(i);
						//}else if(comparisons[i][1] == minscore)
						}else if((scores[player]-scores[i]) + comparisons[i][2] == minscore)
						{
							minIDs.add(i);
						}
					}
				}
				out = "";
				writer.write(entrants[player]+"'s closest brackets: - current score: " 
								+ scores[player] + " count: " + minIDs.size() + "\n");
				hasPrinted = false;
				for(Integer i : minIDs)
				{
					if((comparisons[i][2]-(scores[i]-scores[player]))<0 || minscore>=0)
					{
						out += "  " + entrants[i] + " -";
						out += " total difference: " + comparisons[i][1];
						out += " current deficit: "+ (scores[i]-scores[player]); 
						out += " possible gain: " + comparisons[i][2] +"\n";
						out += "    magic number: " + (comparisons[i][2]-(scores[i]-scores[player])) + "\n";
						out += "\tdifferences: ";
						diffmatches = getDifferentMatches(player,i);
						out += Arrays.toString(diffmatches)+"\n";
						if((scores[i]-scores[player]) > comparisons[i][2])
						{
							out += "Should be dead\n";
							if(!hasPrinted){
								System.out.print(entrants[player] + " by " + entrants[i]);
								hasPrinted = true;
							}else
								System.out.print(", " + entrants[i]);
						}
					}
				}
				if(hasPrinted) System.out.println();
				writer.write(out);
			}
			System.out.println();
			writer.write("</span>\n");
			writer.close();
		} catch (IOException e) {
			System.out.println("problem with output");
			System.exit(1);
		}
		//System.out.println("Done getting differences");
	}
	
	//returns the list of match numbers that have different picks in the given brackets. 
	public static int[] getDifferentMatches(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		
		ArrayList<Integer> differences = new ArrayList<Integer>();
		
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				differences.add(i+1);
			}
		}
		int[] result = new int[differences.size()];
		for(int i = 0; i < result.length; i++)
		{
			result[i] = differences.get(i).intValue();
		}
		return result;
	}
	
	//gets the possible point difference between two brackets, along with the absolute number of differences and the points to make up.
	public static int[] getDifferenceScore(int first, int second)
	{
		String[] firstPicks = allPicks.get(first);
		String[] lastPicks = allPicks.get(second);
		int[] result = new int[3];
		//number of differences, point value, possible points to make up
		result[0] = result[1] = result[2] = 0;
		for(int i = 0; i < firstPicks.length; i++)
		{
			if(!firstPicks[i].equals(lastPicks[i]))
			{
				result[1] += values[i];
				result[0]++;
				if(i >= nextMatch && isValid(firstPicks[i],i))
				{
					result[2]+=values[i];
				}
			}
		}
		
		return result;
	}
	
	//return if a pick is valid for a given match - i.e can the player still earn points if they picked it. 
	//recurses for later matches.
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
			return isValid(pick, 32)||isValid(pick,33);
		}
	}
	

	//read in the possible results from the .csv file and place them in the results global variable.
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
	
	//read in the actual results from the .csv file and place them in the results global variable.
	//sets the *nextMatch* global variable to the first blank result.
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
	
	//read in all picks for a guru contestant from a .csv
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
	
	//calculates the scores for all entrants based on a given set of results. 
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
