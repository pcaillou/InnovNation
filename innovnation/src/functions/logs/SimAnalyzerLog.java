package functions.logs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.exceptions.MatrixException;

import client.gui.GuiCreateGame;
import functions.Game;

public class SimAnalyzerLog {

	public static final Integer SYM_LOGP_STEP = 60;
	
	/**
	 * Genere des logs pour SimAnalyzer depuis des logs deja crees
	 */
	public static void generateSimAnalyzerLog()
	{
		/* on recharge le fichier pour le transformer en log pour SimAnalyzer */
        File fileSource = new File(GuiCreateGame.GAME_NAME + ".csv");
        
        Matrix matrix;
        
        try {
        	matrix=MatrixFactory.importFromFile(FileFormat.CSV, fileSource,";");
		} catch (MatrixException e) {
			System.out.println("Erreur sur la matrice : \n");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Erreur sur le chargement du fichier : \n");
			e.printStackTrace();
			return;
		}
        
        System.out.println("Chargement termine (" + matrix.getSize()[0] + " lignes * " + matrix.getSize()[1] + " colonnes )\n");

		for(long column = 0; column < matrix.getColumnCount(); column++ ){
			matrix.setColumnLabel(column, matrix.getAsString(0,column));
		}
		
		/* on recupere les logP par groupe de pas de temps pour regenerer les logs */
		long cursor = 1;
		int tempsMin = 0;
		int tempsPas = SYM_LOGP_STEP;
		int tempsMax = tempsMin + tempsPas;
		int numPas = 0;
		long columnTime = matrix.getColumnForLabel(Game.LOG_TIME_NAME);
		long columnType = matrix.getColumnForLabel(Game.LOG_TYPE_NAME);
		long columnId = matrix.getColumnForLabel("playerId");
		long columnAbstract = matrix.getColumnForLabel("abstract");
		Collection<Long> buffer = new ArrayList<Long>();
		
		/* on cree la matrice resultat */
		Matrix result = MatrixFactory.zeros(ValueType.STRING, 1, matrix.getColumnCount());
		for (long column = matrix.getColumnCount()-1 ; column >= 2 ; column--)
		{
			result.setAsString(matrix.getAsString(0,column), 0,column);
		}
		result.setAsString("min", 0,0);
		result.setAsString("id", 0,1);
		
		/* on recupere la liste des colomnes graphes (cas special) */
		String[] graphNames = GraphLogPack.titles().substring(0, GraphLogPack.titles().length()-1).split(";");
		ArrayList<Long> graphColumns = new ArrayList<Long>();
		for (int i = graphNames.length-1 ; i >= 0 ; i--)
		{
			graphColumns.add(matrix.getColumnForLabel(graphNames[i])); 
		}
		
		/* on lit la matrice de haut en bas, et on stocke chaque pas de temps dans une seule ligne */
		while(cursor <= matrix.getRowCount() || buffer.size() > 0)
		{
			/* si on depasse le pas, ou qu'on est au dernier buffer, on ajoute le buffer au resultat */
			while((buffer.size() > 0 && cursor >= matrix.getRowCount()) || tempsMax <= matrix.getAsInt(cursor,columnTime))
			{
				/* si on n'a rien trouve, on passe directement au pas suivant */
				if (buffer.size() > 0)
				{
					/* on recuepre la liste des joueurs */
					ArrayList<Integer> idList = new ArrayList<Integer>();
					
					for (Long p : buffer)
					{
						if (!idList.contains(matrix.getAsInt(p,columnId)))
						{
							idList.add(matrix.getAsInt(p,columnId));
						}
					}
					
					Matrix tmp = MatrixFactory.zeros(ValueType.STRING, idList.size(), matrix.getColumnCount());
			
					for (long b : buffer)
					{
						long row = idList.indexOf(matrix.getAsInt(b,columnId));
						
						/* on remplace un a un les valeurs, avec comme cas special les valeurs de graphe */
						for (long column = matrix.getColumnCount()-1 ; column >= 0 ; column--)
						{
							if (graphColumns.contains(column))
							{
								ArrayList<String> graphBuffer = new ArrayList<String>();
								String columnContent =  tmp.getAsString(row,column);
								if (columnContent != null)
								{
									graphBuffer.addAll(DynamicGraph.stringToTab(columnContent));
								}
								columnContent =  matrix.getAsString(b,column);
								if (columnContent != null)
								{
									graphBuffer.addAll(DynamicGraph.stringToTab(columnContent));
								}
								tmp.setAsString(DynamicGraph.tabToString(graphBuffer), row, column);
							}
							else
							{
								tmp.setAsString(matrix.getAsString(b,column), row,column);
							}
						}
					}
					
					/* on change la colonne time pour que celle ci se synchronise sur le pas */
					for (long row = tmp.getRowCount()-1 ; row >= 0 ; row--)
					{
						tmp.setAsInt(numPas, row,columnTime);
					}
					
					/* on modifie les 2 premieres colonnes pour rajouter min et id
					 *  et la colonne abstract car elle provoque des erreurs dans symAnalizer */
					for (long row = tmp.getRowCount()-1 ; row >= 0 ; row--)
					{
						tmp.setAsString(String.valueOf(numPas), row,0);
						tmp.setAsString(tmp.getAsString(row,columnId), row,1);
						tmp.setAsString("0", row,columnAbstract);
					}
					
					/* on ajoute la matrice tmp au resultat */
					Matrix n2 = result.appendVertically(tmp);	
					result=n2.subMatrix(Ret.NEW, 0, 0, n2.getRowCount()-1, n2.getColumnCount()-1);
					for(long column =0; column<matrix.getColumnCount(); column++ ){
						long nc=result.getColumnCount()-matrix.getColumnCount()+column;
						result.setColumnLabel(nc, matrix.getColumnLabel(column));
					}
					
				}
				
				
				/* on initialise pour la partie suivante */
				buffer.clear();
				numPas++;
				tempsMax += tempsPas;
			}
			
			
			if (cursor < matrix.getRowCount())
			{
				/* on ajoute la ligne logp a la liste du pas */
				if (matrix.getAsString(cursor,columnType).equals("logp"))
				{
					buffer.add(cursor);
				}
			}
			
			cursor++;
		}
		
		/* on sauvegarde la matrice dans un fichier */
		String line,filename = GuiCreateGame.GAME_NAME + "_symLog.csv";
		try
		{
			FileWriter fw = new FileWriter(filename, false);
			BufferedWriter output = new BufferedWriter(fw);
			
			/* on parcourt chaque ligne qu'on ajoute au fichier */
			for(long row = 0; row < result.getRowCount(); row++ ){
				line = "";
				for(long col = 0; col < result.getColumnCount(); col++ ){
					if (!line.equals(""))
					{
						line += ";";
					}
					line += result.getAsString(row,col);
				}
				
				output.write(line + "\n");
			}
			
			/* on ecrit le tout dans le fichier */
			output.flush();
			output.close();
			System.out.println("Matrice SimAnalyzer sauvegardee dans le fichier : \"" + filename + "\"");
		}
		catch(IOException e){
			System.out.print("Erreur lors de la sauvegarde du fichier: ");
			e.printStackTrace();
		}
		
	}
	
}
