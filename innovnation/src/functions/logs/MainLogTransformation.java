package functions.logs;

import java.io.File;

public class MainLogTransformation {

	
	
	public static void main(String[] args) {
	
		File repertoire = new File(System.getProperty("user.dir" ) );
		
		/* on cree les dossiers qui n'existent pas encore */
		for (String dir : new String[]{"logp","logi"})
		{
			File f = new File(repertoire.getPath() + "\\" + dir);
			if (!f.exists())
			{
				System.out.println("creation du dossier " + f.getPath());
				f.mkdir();
			}
		}
		
		/* on recupere la liste des fichiers � transformer */
		String [] listefichiers = {"a_game.csv","Bot_game.csv"};
		// listefichiers=repertoire.list();
		
		LogTransformation g;
		String filename, name, ext;
		for(int i=0 ; i<listefichiers.length ; i++){
			filename = listefichiers[i];
			try
			{
				int pos = filename.lastIndexOf(".");
				if (pos == -1)
				{
					continue;
				}
				
				
				name = filename.substring(0,pos);
				ext = filename.substring(pos+1, filename.length());
				
				System.out.println(repertoire.getPath() + "\\" + filename);
				g = new LogTransformation(repertoire.getPath() + "\\" + filename);

				//g.graph.displayGraph();
				
				/* supprime les lignes qui semblent corrompue (attention si nouvelle version de logs) */
				g.removeCorruptedColumns();
	
				/* generation des logs */
				g.generateLogs(repertoire.getPath() + "\\" + "logp" + "\\" + name + "_" + "logp" + "." + ext, "logp", LogTransformation.GRAPH_PERSUATION | LogTransformation.GRAPH_TOKENS | LogTransformation.GRAPH_COMMENT| LogTransformation.GRAPH_CONSENSUS);
				g.generateLogs(repertoire.getPath() + "\\" + "logi" + "\\" + name + "_" + "logi" + "." + ext, "logi", LogTransformation.GRAPH_IDEAS);		
			}
			catch(Exception e)
			{
				System.err.println("Erreur lors de la generation de logs pour " + filename);
				e.printStackTrace();
			}
		}
		System.out.println("Fin du programme.");
		
	}

}
