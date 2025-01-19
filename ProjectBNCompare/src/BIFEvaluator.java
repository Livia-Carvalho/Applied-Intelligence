import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.BIFReader;
import weka.core.Instances;

public class BIFEvaluator extends BIFReader
{

	public BIFEvaluator()
	{
		
	}
	
	public void buildClassifier(Instances instances) throws Exception 
	{
	}
	
	static public void main(String args[])
	{
		new BIFEvaluator(args);				
	}

	public BIFEvaluator(String args[])
	{
		if(args.length != 2 && args.length != 3)
		{
			System.out.println("Erro de sintaxe!");
			System.out.println("Sintaxe correta: Evaluator.jar " +/*
					 		   "'nome do arquivo da base de testes'.arff " +*/
					           "'nome do arquivo da rede induzida'.xml"+
					           "'nome do arquivo da rede original'.xml");			
		}
		else
		{
			
			try 
			{				
				System.out.println();
				System.out.println("-- Avaliando a classificacao e distancia da rede --");
								
				// comparacao entre a rede induzida e a original.
				
				if (args.length == 2) Comparison(args[0], args[1]);
				else ComparisonMatrix(args[0], args[1], args[2]);
				
			}
			catch (Throwable t) 
			{
				t.printStackTrace();
			}
		}
	}
	/**
	 * Responsavel pela comparacao entre a rede induzida e a rede original.				 
	 * @param inducedNetName
	 * @param originalNetName
	 */
	public void Comparison(String inducedNetName, String originalNetName)
	{
		try
		{			
			System.out.println("Comparando a rede " + inducedNetName + " e a original");
			
			BIFEvaluator inducedNet = new BIFEvaluator();
			inducedNet.processFile(inducedNetName);
			
			BIFEvaluator originalNet = new BIFEvaluator();
			originalNet.processFile(originalNetName);
			
			int corretos = originalNet.correctArcs(inducedNet);
			System.out.println("N. arcos corretos:" + corretos);
			int extras = originalNet.extraArcs(inducedNet);
			System.out.println("N. arcos extras:" + extras);
			int ausentes = originalNet.missingArcs(inducedNet);
			System.out.println("N. arcos ausentes:" + ausentes);
			int reversos = originalNet.reversedArcs(inducedNet);
			System.out.println("N. arcos reversos:" + reversos);
			
			float acuracia = (float)corretos / (corretos + extras + ausentes + reversos);
			System.out.println("");
			System.out.println("Acurácia: " + acuracia);
			
			writeResultsToCSV(inducedNetName, corretos, extras, ausentes, reversos, acuracia);
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
		}
	}
	
	public void ComparisonMatrix(String inducedNetName, String originalNetName, String inducedMatrixName)
	{
		try
		{			
			System.out.println("Comparando a topologia da rede " + inducedNetName + " e a da original");
			
			int[][] matrix = loadMatrixFromFile(inducedMatrixName);
			
			BIFEvaluator emptyInducedNet = new BIFEvaluator();
			emptyInducedNet.processFile(inducedNetName);
			
			BIFEvaluator originalNet = new BIFEvaluator();
			originalNet.processFile(originalNetName);
			
			int corretos = originalNet.correctArcs(matrix, emptyInducedNet);
			System.out.println("N. arcos corretos:" + corretos);
			int extras = originalNet.extraArcs(matrix, emptyInducedNet);
			System.out.println("N. arcos extras:" + extras);
			int ausentes = originalNet.missingArcs(matrix, emptyInducedNet);
			System.out.println("N. arcos ausentes:" + ausentes);
			int reversos = originalNet.reversedArcs(matrix, emptyInducedNet);
			System.out.println("N. arcos reversos:" + reversos);
			
			float acuracia = (float)corretos / (corretos + extras + ausentes + reversos);
			System.out.println("");
			System.out.println("Acurácia: " + acuracia);
			
			writeResultsToCSV(inducedNetName, corretos, extras, ausentes, reversos, acuracia);
		}
		catch (Throwable t) 
		{
			t.printStackTrace();
		}
	}
	
	public int[][] loadMatrixFromFile(String inducedNetName) {
	    int[][] matrix = null;
	    try (BufferedReader reader = new BufferedReader(new FileReader(inducedNetName))) {
	        String line;
	        int rowNum = 0;
	        while ((line = reader.readLine()) != null) {
	            String[] values = line.split(",");
	            if (matrix == null) {
	                // Inicializa a matriz na primeira linha lida
	                matrix = new int[values.length][values.length];
	            }
	            for (int colNum = 0; colNum < values.length; colNum++) {
	                // Condição: Se o valor for zero, atribua zero; caso contrário, atribua 1
	                matrix[rowNum][colNum] = (Integer.parseInt(values[colNum]) == 0) ? 0 : 1;
	            }
	            rowNum++;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return matrix;
	}
	

    public void writeResultsToCSV(String datasetName, int corretos, int extras, int ausentes, int reversos, float acuracia) {
        try (FileWriter writer = new FileWriter(datasetName.substring(0, datasetName.length() - 4) + "_results.csv", true)) {
        	
        	File file = new File(datasetName.substring(0, datasetName.length() - 4) + "_results.csv");
        	if (file.length() == 0) {
                // Escreve a linha de cabeçalho se o arquivo estiver vazio
                writer.append("Arcos Corretos, Arcos Extras, Arcos Ausentes, Arcos Reversos, Acurácia\n");
            }
        	
            writer.append(String.valueOf(corretos)).append(",");
            writer.append(String.valueOf(extras)).append(",");
            writer.append(String.valueOf(ausentes)).append(",");
            writer.append(String.valueOf(reversos)).append(",");
            writer.append(String.valueOf(acuracia)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

			
	/**
	 * @param arffName - O caminho completo do arquivo Arff
	 * @return - Um objeto Instances (do Weka) que ï¿½ uma coleï¿½ï¿½o de vï¿½rios objetos 
	 *           Instance (cada Instance representa uma linha do arquivo ARFF)
	 * @throws Exception - se algo der errado, lanï¿½a uma Exceï¿½ao genï¿½rica
	 */
	/*
	private static Instances getInstances(String arffName) throws Exception
	{
		// FileReader ï¿½ uma classe do Java para leitura de arquivos, no construtor 
		// dela passamos o caminho do arquivo que queremos ler
		FileReader arffReader = new FileReader(arffName);
		
		//Instances ï¿½ uma classe do Weka, e no construtor dela podemos passar um 
		//objeto FileReader (que construï¿½mos na linha 
		//acima). Daï¿½ o Weka recupera os dados do arquivo pelo FileReader e monta 
		//uma coleï¿½ï¿½o de Instance num objeto Instances
		Instances instances = new Instances(arffReader);
		arffReader.close(); //fechamos o leitor de arquivo
			
		return instances; //retorna o objeto de Instances
	}
	
	/**
	 * A Funcao saveResults salva os resultados obtidos pelo modelo em arquivo e os
	 * mostra na tela.
	 * @param instances - as amostras da base de dados
	 * @param net - o modelo criado
	 */
	/*
	private void savePercents(Instances instances, BayesNet net)
	{
		try
		{
			FileWriter writer = new FileWriter(new File("porcentagens.xls"));
			PrintWriter percents = new PrintWriter(writer,true);
			double probabilities[];
			
			//System.out.println("Probabilidades \t Acertou (1) ou Errou (0)");
			percents.println("Probabilidades \t Acertou (1) ou Errou (0)");
			for(int i=0; i<instances.numInstances(); i++)
			{				
				probabilities = net.distributionForInstance(instances.instance(i));
				Double prob_classe_zero = probabilities[0];				
				Double prob_classe_um = probabilities[1];
				double probability = net.classifyInstance(instances.instance(i));
				if(prob_classe_zero>prob_classe_um)
				{
					//System.out.print(prob_classe_zero);
					percents.print(prob_classe_zero);
				}
				else
				{
					//System.out.print(prob_classe_um);
					percents.print(prob_classe_um);
				}
				//System.out.print("\t" + probability + "\n");	
				percents.print("\t" + probability + "\n");
			}
			percents.close();
		}
		catch(Exception ex)
		{
			//mostra o erro e a pilha de execuï¿½ao atï¿½ onde o erro ocorreu
			ex.printStackTrace(); 
		}
	}
	
	private void saveClassification(double classification)
	{
		try
		{
			FileWriter writer = new FileWriter(new File("classificacao.xls"), true);
			PrintWriter writerClassification = new PrintWriter(writer,true);
			
			writerClassification.println(classification);
			writerClassification.close();
			System.out.println(classification);
		}
		catch(Exception ex)
		{
			//mostra o erro e a pilha de execuï¿½ao atï¿½ onde o erro ocorreu
			ex.printStackTrace(); 
		}
	}
	*/
}