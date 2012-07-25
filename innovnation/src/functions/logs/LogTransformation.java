package functions.logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.exceptions.MatrixException;

public class LogTransformation {
	
	public static final String FILE = "C:\\Users\\Philippe\\Desktop\\Tournoi_401_vs_402_V1.csv";
	public static final String FILE_LOGI = "C:\\Users\\Philippe\\Desktop\\LogI.csv";
	public static final String FILE_LOGP = "C:\\Users\\Philippe\\Desktop\\LogP.csv";
	public static final String SEPARATOR = ";";
	
	public static final int GRAPH_PERSUATION = 	1 << 0;
	public static final int GRAPH_TOKENS = 		1 << 1;
	public static final int GRAPH_COMMENT = 	1 << 2;
	public static final int GRAPH_IDEAS = 		1 << 3;
	
	public static final int[] GRAPH_LIST = {GRAPH_PERSUATION,GRAPH_TOKENS,GRAPH_COMMENT,GRAPH_IDEAS};
	public static final String[] GRAPH_NAMES = {"persuasionGraph","tokenGraph","commentGraph","ideaGraph"};
	
	public static final int TIME_PER_STEP = 60;
	
	private Matrix matrix = null;
	
	GraphInnovNation graph;
	
	/**
	 * Cree un GraphComputation depuis une matrice contenue dans un fichier
	 * Genere une matrice contenant le contenu du fichier
	 * Et un graphe representant la partie entiere, completant les donnees manquante aleatoirement (/!\)
	 * @param filename : le fichier de la matrice source
	 */
	public LogTransformation(String filename)
	{
		/* on charge le fichier */
		
        File fileSource = new File(filename);
        
        try {
        	matrix=MatrixFactory.importFromFile(FileFormat.CSV, fileSource,SEPARATOR);
		} catch (MatrixException e) {
			System.out.println("Erreur sur la matrice : ");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Erreur sur le chargement du fichier : ");
			e.printStackTrace();
		}
        
		for(long column = 0; column < matrix.getColumnCount(); column++ ){
			matrix.setColumnLabel(column, matrix.getAsString(0,column));
		}	
		
		generateInnovGraph();
	}
	
	/**
	 * Affiche la matrice principale sur l'ecran
	 */
	public void showMatrix()
	{
		matrix.showGUI();
	}
	
	/**
	 * Affiche le graphe a l'ecran
	 */
	public void showGraph()
	{
		
		try {
			graph.displayGraph();
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Genere le graphe innovNation
	 */
	public void generateInnovGraph()
	{
		/* on recupere ce qu'on peut pour recreer le graphe */
		try {
			graph = new GraphInnovNation("root");
		} catch (GraphInnovNationException e) {
			e.printStackTrace();
		}
				
		boolean firstLogI = false;
		String rootId = "";
        long innovColumn = matrix.getColumnForLabel("InnovNationGraph");
        long typeColumn = matrix.getColumnForLabel("type");
        long timeColumn = matrix.getColumnForLabel("time");
        long playerColumn = matrix.getColumnForLabel("playerId");
        long ideaColumn = matrix.getColumnForLabel("ideaId");
        long ideaNbParentsColumn = matrix.getColumnForLabel("ideaParents");
        long ideaOwnerColumn = matrix.getColumnForLabel("ideaOwnerId");
        long voteTokensColumn = matrix.getColumnForLabel("voteTokens");
        long voteValenceColumn = matrix.getColumnForLabel("voteValence");
        java.util.Random rand = new java.util.Random();
        
        rand.setSeed(0);
        
        String type = "";
        String prec = "";
    
        int nbError = 0;
        
		for(long row = 1; row < matrix.getRowCount(); row++ )
		{
			try {	
				// s'il n'y a pas de colonnes innovNations, on recréé le graphe
				if (innovColumn == -1)
				{
					type = matrix.getAsString(row,typeColumn);
					if (type == null)
					{
						continue;
					}
					// TODO pes de moyen pour trouver les idees sources d'une idee
					if (type.equals("idea") || type.equals("logi"))
					{ 
						String ideaId = matrix.getAsString(row,ideaColumn);
						String ownerId = matrix.getAsString(row,ideaOwnerColumn);
						int nbParents = matrix.getAsInt(row,ideaNbParentsColumn)-1;					
						long timeAdd = matrix.getAsInt(row,timeColumn);
						
						if (ideaId != null && !firstLogI)
						{
							rootId = ideaId;
							graph.addRoot(ideaId);
							firstLogI = true;
						}
						else
						{
							
							if (ownerId != null && !graph.playerExists(ownerId) )
							{
								graph.addPlayer(ownerId, 0l);
							}
							if (ideaId != null && !graph.ideaExists(ideaId))
							{
								/* les idees source etant inconnue, on doit en choisir une au hazard */
								ArrayList<String> sourcesPossible = new ArrayList<String>();
								ArrayList<String> sources = new ArrayList<String>();
								for (Integer i : graph.getIdeasIndexs())
								{
									sourcesPossible.add((String)graph.getNode(i).getAttribute("ID"));
								}
								if (firstLogI)
								{
									sourcesPossible.add(rootId);
								}
								for (int i = 0 ; i < nbParents && sourcesPossible.size() > 0 ; i++)
								{
									sources.add(sourcesPossible.get(Math.abs(rand.nextInt())%sourcesPossible.size()));
								}
								
								
								//System.out.println("ajout de l'idee " + ideaId);
								graph.addIdea(ideaId, sources, ownerId, timeAdd);
							}
						}
											
					}
					else if (type.equals("vote"))
					{
						String playerId = matrix.getAsString(row,playerColumn);
						int tokens = matrix.getAsInt(row,voteTokensColumn);
						Integer idCible = matrix.getAsInt(row,ideaColumn);
						int valence = matrix.getAsInt(row,voteValenceColumn);
						long timeAdd = matrix.getAsInt(row,timeColumn);
						if (!graph.playerExists(playerId))
						{
							graph.addPlayer(playerId, 0l);
						}	
						
						if (!graph.ideaExists(idCible.toString()))
						{
							if (!firstLogI)
							{
								graph.addRoot(idCible.toString());
							}
							
						}
								
						
						
						graph.addVote(String.valueOf(graph.getVotesIndexs().size()+1), playerId, idCible.toString(), tokens, valence, timeAdd);
						
						
					}
					else if (type.equals("logp"))
					{
						
						String playerId = matrix.getAsString(row,playerColumn);
						if (!graph.playerExists(playerId) && playerId != null)
						{
							graph.addPlayer(playerId, 0l);
						}	
					}
					else if (type.equals("comment"))
					{
						String playerId = matrix.getAsString(row,playerColumn);
						int tokens = matrix.getAsInt(row,voteTokensColumn);
						int valence = matrix.getAsInt(row,voteValenceColumn);
						long timeAdd = matrix.getAsInt(row,timeColumn);
						if (!graph.playerExists(playerId))
						{
							graph.addPlayer(playerId, 0l);
						}	
						
						/* l'idee source etant inconnue, on doit en choisir une au hazard */
						ArrayList<String> sourcesPossible = new ArrayList<String>();
						Node owner = graph.getNode(GraphInnovNation.PREFIX_PLAYER + playerId);
						for (Integer i : graph.getIdeasIndexs())
						{
							/* on n'ajoute pas les idees s'il existe deja un vote au temps donne */
							boolean ajoutSource = true;
							Node n = graph.getNode(i);
							
							if (owner.hasEdgeBetween(n))
							{
								Iterable<Edge> edges = owner.getEachLeavingEdge();
								
								for (Edge e : edges)
								{
									if (e.getOpposite(owner).equals(n))
									{
										ArrayList<long[]> hist = e.getAttribute(GraphInnovNation.VOTE_HIST);
										if (hist != null)
										{
											for (int j = hist.size()-1 ; j >= 0 ; j--)
											{
												if (hist.get(j)[0] == timeAdd)
												{
													ajoutSource = false;
													break;
												}
											}
											if (!ajoutSource)
											{
												break;
											}
										}
									}
								}
							}
							
							if (ajoutSource)
							{
								sourcesPossible.add((String)n.getAttribute("ID"));
							}
						}
						if (firstLogI)
						{
							sourcesPossible.add(rootId);
						}
						
						graph.addVote(String.valueOf(graph.getVotesIndexs().size()+1), playerId, sourcesPossible.get(Math.abs(rand.nextInt())%sourcesPossible.size()), tokens, valence, timeAdd);
					}
				}
				else // si la colonne innovNation existe, on recharge tout
				{
			        String g = matrix.getAsString(row,innovColumn);
			        if (g != null && !g.equals("" + GraphInnovNation.LEFT_BRACE + GraphInnovNation.RIGHT_BRACE) &&  !prec.equals(g))
			        {
						graph.loadFromString(g);
						prec = g;
			        }
				}
			}
			catch (MatrixException e) {
				System.err.println("Error line " + row + " : MatrixException : " + e.getLocalizedMessage());
				nbError++;
			} 
			catch (GraphInnovNationException e) {
				System.err.println("Error line " + row + " : GraphInnovNationException : " + e.getLocalizedMessage());
				nbError++;
			} 
			catch (NullPointerException e) {
				System.err.println("Error line " + row + " : NullPointerException : " + e.getLocalizedMessage());
				nbError++;
			}
		}
		if (nbError > 0)
		{
			System.err.println("ATTENTION : graphe genere avec " + nbError + " erreurs");
		}
	}
	
	/**
	 * Supprime les colonnes a null
	 */
	public void removeCorruptedColumns()
	{
		ArrayList<Integer> columnsCorrupted = new ArrayList<Integer>();
        
		for (int i = 0 ; i < matrix.getColumnCount() ; i++)
		{
			if (matrix.getAsString(0,i) == null || matrix.getAsString(0,i).equals("") /*|| matrix.getColumnLabel(i).equals("abstract")*/)
			{
				columnsCorrupted.add(i);
			}
		}
		matrix = matrix.deleteColumns(Ret.NEW, columnsCorrupted);
		
	}
	
	public void saveMatrix(String filename)
	{
		saveMatrix(filename, matrix);
	}
	
	/**
	 * Genere un fichier contenant les logs au format SimAnalyzer avec les donnees graphes
	 * @param filename : fichier ou sauvegarder la matrice generee
	 * @param columnLog : nom de la colonne de logs a generer
	 * @param graphOption : liste des graphes a inclure
	 */
	public void generateLogs(String filename, String columnLog, int graphOption)
	{
		
		/* on recupere les logP par groupe de pas de temps pour regenerer les logs */
		long cursor = 1;
		int tempsMin = 0;
		int tempsPas = TIME_PER_STEP;
		int tempsMax = tempsMin + tempsPas;
		int numPas = 0;
		long columnTime = matrix.getColumnForLabel("time");
		long columnType = matrix.getColumnForLabel("type");
		long columnId = 0;
		Collection<Long> buffer = new ArrayList<Long>();
		int nbCorrupted = 0;
		
		if (columnLog.equals("logp"))
		{
			columnId = matrix.getColumnForLabel("playerId");
		}
		else if (columnLog.equals("logi"))
		{
			columnId = matrix.getColumnForLabel("ideaId");
		}
		else
		{
			columnId = matrix.getColumnForLabel("playerId");
		}
		
		
		/* on cree la matrice resultat en ajoutant les 2 colonnes de graphe*/
		Matrix result = MatrixFactory.zeros(ValueType.STRING, 1, matrix.getColumnCount()); 
		for (long column = result.getColumnCount()-1 ; column >= 2 ; column--)
		{
			result.setAsString(matrix.getAsString(0,column), 0,column);
		}
		result.setAsString("id", 0,1);
		result.setAsString("min", 0,0);
				
		/* on genere une matrice ne contenant que les lignes demandees */
		while(cursor <= matrix.getRowCount() || buffer.size() > 0)
		{
			/* si on depasse le pas, ou qu'on est au dernier buffer, on ajoute le buffer au resultat */
			while((buffer.size() > 0 && cursor >= matrix.getRowCount()) || tempsMax <= matrix.getAsInt(cursor,columnTime))
			{
				
				/* si on n'a rien trouve, on passe directement au pas suivant */
				if (buffer.size() > 0)
				{
					/* on recupere la liste des joueurs */
					ArrayList<Integer> idList = new ArrayList<Integer>();
					
					for (Long p : buffer)
					{
						if (!idList.contains(matrix.getAsInt(p,columnId)))
						{
							idList.add(matrix.getAsInt(p,columnId));
						}
					}
					
					Matrix tmp = MatrixFactory.zeros(ValueType.STRING, idList.size(), result.getColumnCount());
			
					for (long b : buffer)
					{
						long row = idList.indexOf(matrix.getAsInt(b,columnId));
						
						/* on remplace un a un les valeurs */
						for (long column = tmp.getColumnCount()-1 ; column >= 0 ; column--)
						{
							tmp.setAsString(matrix.getAsString(b,column), row,column);
						}
					}
					
					/* on change la colonne time pour que celle ci se synchronise sur le pas */
					for (long row = tmp.getRowCount()-1 ; row >= 0 ; row--)
					{
						tmp.setAsInt(numPas, row,columnTime);
					}
					
					/* on modifie les 2 premieres colonnes  pour correspondre avec leurs titre */
					for (long row = tmp.getRowCount()-1 ; row >= 0 ; row--)
					{
						tmp.setAsString(String.valueOf(numPas), row,0);
						tmp.setAsString(tmp.getAsString(row,columnId), row,1);
					}
					
					/* on ajoute la matrice tmp au resultat */
					Matrix n2 = result.appendVertically(tmp);	
					result=n2.subMatrix(Ret.NEW, 0, 0, n2.getRowCount()-1, n2.getColumnCount()-1);
					for(long column =0; column<tmp.getColumnCount(); column++ ){
						long nc=result.getColumnCount()-tmp.getColumnCount()+column;
						result.setColumnLabel(nc, tmp.getColumnLabel(column));
					}
				}
				
				
				/* on initialise pour la partie suivante */
				buffer.clear();
				numPas++;
				tempsMax += tempsPas;
			}

			if (cursor < matrix.getRowCount())
			{
				boolean corruptedLine,fullZeros;
				/* on ajoute la ligne demandee a la liste du pas */
				String type = matrix.getAsString(cursor,columnType);
				if (type != null && type.equals(columnLog))
				{
					/* les 74 premieres lignes ne doivent pas etre à null, sinon le resultat est dit corrompu */
					corruptedLine = false;
					fullZeros = true;
					for (long i = 1 ; i < 74 ; i++)
					{
						//System.out.println("getAsObject(" + cursor + "," + i + ")");
						if (matrix.getAsObject(cursor,i) == null)
						{
							corruptedLine = true;
							break;
						}
						else
						{
							if (fullZeros && matrix.getAsInt(cursor,i) != 0)
							{
								fullZeros = false;
							}
						}
					}
					if (!corruptedLine && !fullZeros)
					{
						buffer.add(cursor);
					}
					else
					{
						nbCorrupted++;
					}
					
				}
			}
			
			cursor++;
		}
		
		if (nbCorrupted > 0)
		{
			System.err.println("Warning : " + nbCorrupted + " lignes corrompue");
		}
		
		/* on cree la matrice de graphe */
		
		ArrayList<HashMap<String,Collection<String>>> logs = new ArrayList<HashMap<String,Collection<String>>>();
		ArrayList<DynamicGraph> graphs = new ArrayList<DynamicGraph>();
		ArrayList<String> columnNames = new ArrayList<String>();
		GraphInnovNation g = graph.clone();
		
		g.divideTime(TIME_PER_STEP);
		
		for (int i = 0 ; i < GRAPH_LIST.length ; i++)
		{
			int op = graphOption & GRAPH_LIST[i];
			if (op != 0)
			{
				columnNames.add(GRAPH_NAMES[i]);
				logs.add(new HashMap<String,Collection<String>>());
				switch(GRAPH_LIST[i])
				{
					case GRAPH_PERSUATION :
						graphs.add(g.getPersuationGraph());
						break;
					case GRAPH_IDEAS :
						graphs.add(g.getIdeaGraph());
						break;
					case GRAPH_TOKENS :
						graphs.add(g.getTokenGraph());
						break;
					case GRAPH_COMMENT :
						graphs.add(g.getCommentGraph());
						break;
				}
			}
		}
		
		Matrix graphMatrix = MatrixFactory.zeros(ValueType.STRING, result.getRowCount(), graphs.size());

		for (int i = 0 ; i < columnNames.size(); i++)
		{
			graphMatrix.setAsString(columnNames.get(i),0,i);
			graphMatrix.setColumnLabel(i, columnNames.get(i));
		}
		
		String id;
		Integer time;
		Node p;
		
		ArrayList<String> edges, diff;
		
		//boolean innovNationLogsGenerated = false;
		
		for (long row = 1 ; row < result.getRowCount() ; row++)
		{
			/* on recupere le temps et l'id du joueur */
			id = result.getAsString(row,columnId);
			time = result.getAsInt(row,columnTime);
			
			//System.out.println("ligne sur l'id " + id + " de la colonne " + columnId);

			
			/* on genere par defaut les entrees dans les logs */
			for (HashMap<String,Collection<String>> log : logs)
			{
				if (!log.containsKey(id))
				{
					log.put(id, new ArrayList<String>());
				}
			}
			
			/* on recupere la different arcs + declaration node pour le joueur p sur le graphe */

			for (int i = 0 ; i < graphs.size(); i++)
			{			
				edges = new ArrayList<String>();
				diff = new ArrayList<String>();
				p = graphs.get(i).getNode(id);			
				
				edges.add(graphs.get(i).nodeToString(p));

				for (Edge e : p.getEachLeavingEdge())
				{
					edges.add(graphs.get(i).edgeToString(e));
				}
				
				
				diff.addAll(edges);
				diff.removeAll(logs.get(i).get(id));
				
				
				logs.get(i).put(id,edges);
				graphMatrix.setAsString(DynamicGraph.tabToString(diff), row,i);
			}
		}
		
		/* on ajoute la matrice tmp au resultat */
		Matrix n2 = result.appendHorizontally(graphMatrix);	
		result=n2.subMatrix(Ret.NEW, 0, 0, n2.getRowCount()-1, n2.getColumnCount()-1);
		for(long column =0; column<graphMatrix.getColumnCount(); column++ ){
			long nc=result.getColumnCount()-graphMatrix.getColumnCount()+column;
			result.setColumnLabel(nc, graphMatrix.getColumnLabel(column));
		}
		
		saveMatrix(filename, result);
	}
	
	/**
	 * 
	 * @param filename : fichier ou sauvegarder la matrice
	 * @param m : matrice a sauvegarder
	 */
	private static void saveMatrix(String filename, Matrix m)
	{
		String line;
		try
		{
			FileWriter fw = new FileWriter(filename, false);
			BufferedWriter output = new BufferedWriter(fw);
			
			/* on parcourt chaque ligne qu'on ajoute au fichier */
			for(long row = 0; row < m.getRowCount(); row++ ){
				line = "";
				for(long col = 0; col < m.getColumnCount(); col++ ){
					if (!line.equals(""))
					{
						line += SEPARATOR;
					}
					line += m.getAsString(row,col);
				}
				
				output.write(line + "\n");
			}
			
			/* on ecrit le tout dans le fichier */
			output.flush();
			output.close();
			System.out.println("Matrice sauvegardee dans le fichier : \"" + filename + "\"");
		}
		catch(IOException e){
			System.out.print("Erreur lors de la sauvegarde du fichier: ");
			e.printStackTrace();
		}
	}

}
