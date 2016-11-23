package br.jus.trt4.justica_em_numeros_2016.auxiliar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Classe que contém métodos auxiliares utilizados nesse projeto.
 * @author fgiotto
 *
 */
public class Auxiliar {

	private static final File arquivoConfiguracoes = new File("config.properties");

	private static final Logger LOGGER = LogManager.getLogger(Auxiliar.class);
	private static Properties configs = null;
	private static String diaMesAnoArquivosXML;
	private static final SimpleDateFormat dfDataNascimento = new SimpleDateFormat("yyyyMMdd");
	private static File pastaSaida = null;

	
	/**
	 * Cria uma conexão com o banco de dados do PJe, conforme a instância selecionada (1 ou 2),
	 * lendo os dados dos parâmetros "url_jdbc_1g" ou "url_jdbc_2g"
	 * @throws SQLException
	 */
	public static Connection getConexaoPJe(int grau) throws SQLException {
		LOGGER.info("Abrindo conexão com o PJe " + grau + "G");
		return getConexaoDasConfiguracoes("url_jdbc_" + grau + "g");
	}
	
	
	/**
	 * Cria uma conexão com o banco de dados de staging do e-Gestão, conforme a instância selecionada (1 ou 2),
	 * lendo os dados dos parâmetros "url_jdbc_egestao_1g" ou "url_jdbc_egestao_2g"
	 * @throws SQLException
	 */
	public static Connection getConexaoStagingEGestao(int grau) throws SQLException {
		LOGGER.info("Abrindo conexão com o Staging do e-Gestão " + grau + "G");
		return getConexaoDasConfiguracoes("url_jdbc_egestao_" + grau + "g");
	}
	
	
	/**
	 * Cria uma nova conexão com o banco de dados do PJe, recebendo por parâmetro o nome da chave do 
	 * arquivo "config.properties" que contém a String JDBC que deverá ser utilizada para conexão.
	 * 
	 * @param parametro
	 * @return
	 * @throws SQLException
	 */
	private static Connection getConexaoDasConfiguracoes(String parametro) throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return DriverManager.getConnection(getParametroConfiguracao(parametro, true));
	}

	/**
	 * Carrega um parâmetro booleano do arquivo "config.properties". Se o parâmetro não existir ou não for "SIM" ou "NAO", lança uma exceção.
	 * 
	 * @param parametro
	 * @return
	 */
	public static boolean getParametroBooleanConfiguracao(String parametro) {
		String valor = getParametroConfiguracao(parametro, false);
		if ("SIM".equals(valor)) {
			return true;
		}
		if ("NAO".equals(valor)) {
			return false;
		}
		throw new RuntimeException("Defina o parâmetro '" + parametro + "' no arquivo '" + arquivoConfiguracoes + "' com os valores SIM ou NAO");
	}
	
	
	/**
	 * Carrega um parâmetro booleano do arquivo "config.properties". Se o parâmetro não existir ou não for "SIM" ou "NAO", retorna um valor padrão
	 * 
	 * @param parametro
	 * @return
	 */
	public static boolean getParametroBooleanConfiguracao(String parametro, boolean valorPadrao) {
		String valor = getParametroConfiguracao(parametro, false);
		if ("SIM".equals(valor)) {
			return true;
		}
		if ("NAO".equals(valor)) {
			return false;
		}
		return valorPadrao;
	}
	
	
	/**
	 * Carrega um parâmetro numérico do arquivo "config.properties". Se o parâmetro não existir ou não for numérico, lança uma exceção.
	 * 
	 * @param parametro
	 * @return
	 */
	public static int getParametroInteiroConfiguracao(String parametro) {
		try {
			return Integer.parseInt(getParametroConfiguracao(parametro, true));
		} catch (NumberFormatException ex) {
			throw new RuntimeException("O parâmetro '" + parametro + "', no arquivo '" + arquivoConfiguracoes + "', deve ser numérico.");
		}
	}
	
	
	/**
	 * Carrega um parâmetro numérico do arquivo "config.properties". Se o parâmetro não existir ou não for numérico, retorna o valor padrão
	 * 
	 * @param parametro
	 * @return
	 */
	public static int getParametroInteiroConfiguracao(String parametro, int valorPadrao) {
		try {
			return Integer.parseInt(getParametroConfiguracao(parametro, true));
		} catch (NumberFormatException ex) {
			return valorPadrao;
		}
	}
	
	
	/**
	 * Retorna o conteúdo de um determinado parâmetro definido no arquivo "config.properties".
	 * 
	 * Se o parâmetro for obrigatório e não existir, será lançada uma exceção, solicitando que o usuário preencha o referido parâmetro.
	 * 
	 * @param parametro
	 * @param obrigatorio: indica se o parâmetro é obrigatório
	 * @return se o parâmetro existir, retorna seu valor. Se o parâmetro não existir e for obrigatório, será lançada uma exceção.
	 * Se o parâmetro não existir mas não for obrigatório, retornará "null".
	 */
	public static String getParametroConfiguracao(String parametro, boolean obrigatorio) {
		if (obrigatorio && !getConfigs().containsKey(parametro)) {
			throw new RuntimeException("Defina o parâmetro '" + parametro + "' no arquivo '" + arquivoConfiguracoes + "'");
		}
		return getConfigs().getProperty(parametro);
	}

	
	/**
	 * Retorna o conteúdo de um determinado parâmetro definido no arquivo "config.properties".
	 * 
	 * Se o parâmetro não existir, será retornado o valor padrão.
	 */
	public static String getParametroConfiguracao(String parametro, String valorPadrao) {
		if (getConfigs().containsKey(parametro)) {
			return getConfigs().getProperty(parametro);
		} else {
			return valorPadrao;
		}
	}

	
	/**
	 * Carrega e mantém em cache as configurações definidas no arquivo "config.properties"
	 */
	private static Properties getConfigs() {
		if (configs == null) {

			try {
				configs = carregarPropertiesDoArquivo(arquivoConfiguracoes);
			} catch (IOException ex) {
				throw new RuntimeException("Erro ao ler arquivo de configurações (" + arquivoConfiguracoes + "): " + ex.getLocalizedMessage(), ex);
			}
		}
		return configs;
	}


	/**
	 * Carrega os dados de um arquivo ".properties"
	 *  
	 * @throws IOException
	 */
	public static Properties carregarPropertiesDoArquivo(File arquivo) throws IOException {
		if (!arquivo.exists()) {
			throw new RuntimeException("O arquivo '" + arquivo + "' não existe!");
		}
		Properties p = new Properties();

		FileInputStream fis = new FileInputStream(arquivo);
		try {
			p.load(fis);
		} finally {
			fis.close();
		}
		return p;
	}

	
	/**
	 * Consome um stream, mostrando seu resultado no console
	 * 
	 * @param inputStream: stream que será lido
	 * @param prefix: prefixo para escrever no console antes de cada linha lida do stream
	 * @throws IOException
	 */
	public static void consumirStream(InputStream inputStream, String prefix) throws IOException {
		try (Scanner scanner = new Scanner(inputStream, "LATIN1")) {
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				LOGGER.debug(prefix + line);
			}
		}
	}
	
	
	/**
	 * Aguarda o término da execução de um processo e confere se ele retornou código "0",
	 * que é o código padrão para um processo que retornou sem erros.
	 * 
	 * @param process
	 * @throws InterruptedException
	 */
	public static void conferirRetornoProcesso(Process process) throws InterruptedException {
		process.waitFor();
		
		// Confere se o processo retornou "0", que significa "OK".
		if (process.exitValue() != 0) {
			throw new RuntimeException("Processo retornou " + process.exitValue());
		}
	}
	
	
	/**
	 * Consome um determinado stream, gravando linha por linha nos logs do LOG4J.
	 * 
	 * Esse método é um pouco mais complexo do que um simples "while(hasNextLine) { log(nextLine) }", 
	 * pois é utilizado para ler a STDOUT e a STDERR da JAR "replicacao-client", do CNJ.
	 * 
	 * Essa JAR, em diversas vezes, utiliza "print" em vez de "println", ou seja, não envia o caractere de
	 * fim de linha, o que faz com que o "hasNextLine" fique aguardando indefinidamente.
	 * 
	 * Por isso, o método "consumirStreamAssincrono" lê todos os bytes disponíveis, mesmo que não exista
	 * uma quebra de linha.
	 * 
	 * @param inputStream
	 * @param prefix
	 * @param logLevel
	 * @throws IOException
	 */
	public static void consumirStreamAssincrono(final InputStream inputStream, final String prefix, final Level logLevel) throws IOException {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while(true) {
						leBytesDisponiveis(inputStream, logLevel);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							leBytesDisponiveis(inputStream, logLevel);
						}
					}
					
				} catch (IOException e) {
					LOGGER.log(logLevel, "Leitura do stream finalizada: " + inputStream);
				}
			}

			private void leBytesDisponiveis(final InputStream inputStream, final Level logLevel) throws IOException {
				int qtdBytes = inputStream.available();
				if (qtdBytes > 0) {
					byte[] bytes = new byte[qtdBytes];
					inputStream.read(bytes);
					String str = new String(bytes);
					for (String linha: str.split("\n")) {
						LOGGER.log(logLevel, linha);
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	
	/**
	 * Lê todo o conteúdo de um arquivo UTF-8 e retorna em uma String
	 * 
	 * @param arquivo
	 * @throws IOException
	 */
	public static String lerConteudoDeArquivo(String arquivo) throws IOException {
		return FileUtils.readFileToString(new File(arquivo), "UTF-8");
	}
	
	
	/**
	 * Lê um campo "int" de um ResultSet, lançando uma exceção se ele for nulo
	 */
	public static int getCampoIntNotNull(ResultSet rs, String fieldName) throws SQLException {
		int result = rs.getInt(fieldName);
		if (rs.wasNull()) {
			throw new RuntimeException("Campo '" + fieldName + "' do ResultSet é nulo!");
		}
		
		return result;
	}
	
	
	/**
	 * Lê um campo "string" de um ResultSet, lançando uma exceção se ele for nulo
	 */
	public static String getCampoStringNotNull(ResultSet rs, String fieldName) throws SQLException {
		String result = rs.getString(fieldName);
		if (result == null) {
			throw new RuntimeException("Campo '" + fieldName + "' do ResultSet é nulo!");
		}
		
		return result;
	}
	

	/**
	 * Lê um campo "double" de um ResultSet, retornando NULL se estiver em branco no banco de dados.
	 */
	public static Double getCampoDoubleOrNull(ResultSet rs, String fieldName) throws SQLException {
		double result = rs.getDouble(fieldName);
		if (rs.wasNull()) {
			return null;
		} else {
			return result;
		}
	}
	
	/**
	 * Retorna o arquivo onde deve ser gravada e lida a lista de processos de uma determinada
	 * instância do PJe.
	 */
	public static File getArquivoListaProcessos(int grau) {
		return new File(prepararPastaDeSaida(), grau + "g/lista_processos.txt");
	}
	
	
	/**
	 * Retorna a pasta raiz onde serão gravados e lidos os arquivos XML de uma determinada
	 * instância do PJe.
	 */
	public static File getPastaXMLsIndividuais(int grau) {
		return new File(prepararPastaDeSaida(), grau + "g/xmls_individuais");
	}


	/**
	 * Retorna a pasta raiz onde serão gravados e lidos os arquivos XML unificados para serem
	 * enviados ao CNJ
	 */
	public static File getPastaXMLsUnificados() {
		File pasta = new File(prepararPastaDeSaida(), "xmls_unificados");
		pasta.mkdirs();
		return pasta;
	}

	
	/**
	 * Retorna o prefixo que os arquivos XML a serem enviados ao CNJ devem possuir, conforme 
	 * formato definido no site do CNJ:
	 * <SIGLA_TRIBUNAL>_<GRAU_JURISDICAO>_<DIAMESANO>
	 */
	public static String getPrefixoArquivoXML(int grau) {
		if (diaMesAnoArquivosXML == null) {
			diaMesAnoArquivosXML = getParametroConfiguracao("dia_padrao_para_arquivos_xml", new SimpleDateFormat("ddMMyyyy").format(new Date()));
		}
		return Auxiliar.getParametroConfiguracao("sigla_tribunal", true) + "_G" + grau + "_" + diaMesAnoArquivosXML;
	}
	
	
	/**
	 * Formata uma data conforme estabelecido no arquivo de intercomunicação: AAAAMMDD
	 */
	public static String formataDataAAAAMMDD(Date data) {
		return dfDataNascimento.format(data);
	}
	
	
	/**
	 * Retorna a pasta padrão onde os arquivos TXT e XML serão gerados pela ferramenta.
	 * 
	 * Direciona, também, os arquivos de log para esta pasta
	 * Fonte: http://stackoverflow.com/questions/25114526/log4j2-how-to-write-logs-to-separate-files-for-each-user
	 */
	public static File prepararPastaDeSaida() {
		
		if (pastaSaida == null) {
			String nomePastaSaida = Auxiliar.getParametroConfiguracao("pasta_saida_padrao", "output");
			pastaSaida = new File(nomePastaSaida);
			
			ThreadContext.put("logFolder", nomePastaSaida);
		}
		
		return pastaSaida;
	}
}
