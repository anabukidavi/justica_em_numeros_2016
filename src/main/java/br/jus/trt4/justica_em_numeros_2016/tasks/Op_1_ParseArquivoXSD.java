package br.jus.trt4.justica_em_numeros_2016.tasks;

import java.util.ArrayList;

import br.jus.trt4.justica_em_numeros_2016.auxiliar.Auxiliar;

/**
 * Processa o arquivo XSD recebido do CNJ (src/main/resources/replicacao-nacional.xsd) e gera as classes
 * Java correspondentes, para permitir a gera��o dos arquivos XML.
 * 
 * Essa classe s� precisar� ser executada novamente se houver alguma altera��o do arquivo XSD.
 * 
 * Fonte: https://sanaulla.info/2010/08/29/using-jaxb-to-generate-xml-from-the-java-xsd-2/
 * 
 * @author fgiotto
 */
public class Op_1_ParseArquivoXSD {
	
	public static void main(String[] args) throws Exception {
		
		ArrayList<String> comandos = new ArrayList<>();
		
		// Utilit�rio do Java para processar schemas XSD e gerar classes Java
		comandos.add("xjc");
		
		// Verifica se ser� necess�rio utilizar proxy
		String proxy = Auxiliar.getParametroConfiguracao("configuracao_proxy", false);
		if (proxy != null) {
			comandos.add("-httpproxy");
			comandos.add(proxy);
		}
		
		// Arquivo XSD recebido do CNJ
		comandos.add("src/main/resources/replicacao-nacional.xsd");
		
		// Pasta de sa�da dos arquivos Java gerados
		comandos.add("-d");
		comandos.add("src/main/java");
		
		// Executa o comando
		Process p = Runtime.getRuntime().exec(comandos.toArray(new String[comandos.size()]));
		Auxiliar.consumirStream(p.getInputStream(), "STDOUT - ");
		Auxiliar.consumirStream(p.getErrorStream(), "STDERR - ");
		Auxiliar.conferirRetornoProcesso(p);
	}
}
