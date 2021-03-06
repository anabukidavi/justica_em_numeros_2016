package br.jus.trt4.justica_em_numeros_2016.tasks;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.jus.trt4.justica_em_numeros_2016.auxiliar.AcumuladorExceptions;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.Auxiliar;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.ControleAbortarOperacao;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.HttpServerStatus;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.Parametro;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.ProcessoFluxo;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.ProcessoSituacaoEnum;
import br.jus.trt4.justica_em_numeros_2016.auxiliar.ProgressoInterfaceGrafica;

/**
 * Novo protótipo de operação completa que trabalha como uma linha de produção, gerando os arquivos XML, enviando-os ao CNJ
 * e validando o processamentos dos protocolos, de forma paralela.
 *
 * TODO: Conferir erro no "Conferindo protocolos no CNJ"
 * TODO: Tratar travamentos da VPN (tentar forçar fechar conexão ao banco)
 * TODO: Encerrar operação quando todos arquivos forem processados
 * TODO: Tratar também carga de dados de sistemas legados
 *
 * @author felipe.giotto@trt4.jus.br
 */
public class Op_Y_OperacaoFluxoContinuo implements AutoCloseable {

	private static final Logger LOGGER = LogManager.getLogger(Op_Y_OperacaoFluxoContinuo.class);
	
	private Collection<ProcessoFluxo> processosFluxos = new ConcurrentLinkedDeque<>();
	private HttpServerStatus serverStatus;
	private boolean executandoOperacao1BaixandoLista;
	private boolean executandoOperacao2Geracao;
	private boolean executandoOperacao4Envio;
	private boolean executandoOperacao5Conferencia;
	
	private void iniciar() throws IOException, SQLException {
		
		// Servidor web, para controlar o progresso da execução
		iniciarServidorWeb();
		
		// Baixa lista de processos do PJe
		if (Auxiliar.deveProcessarPrimeiroGrau()) {
			op1BaixarListaProcesssoSeNecessario(1);
		}
		if (Auxiliar.deveProcessarSegundoGrau()) {
			op1BaixarListaProcesssoSeNecessario(2);
		}
		
		iniciarAtualizacaoStatusBackground();
		iniciarOperacoesGeracaoEnvioValidacaoEmBackground();
		
		if (!"APENAS_PJE".equals(Auxiliar.getParametroConfiguracao(Parametro.sistema_judicial, false))) {
			AcumuladorExceptions.instance().adicionarException("Essa operação ainda não trabalha corretamente com extração de dados dos sistemas legados. Recomenda-se utilizá-la, por enquanto, somente com o parâmetro 'sistema_judicial=APENAS_PJE'", "Op_Y_OperacaoFluxoContinuo");
		}
		
		// TODO: Implementar condição de saída
	}

	/**
	 * Exibe uma interface de acompanhamento do progresso da operação completa
	 */
	private void iniciarServidorWeb() {
		try {
			serverStatus = new HttpServerStatus(this);
			
			Auxiliar.safeSleep(1000);
			
			serverStatus.abrirNavegador();
		} catch (IOException e) {
			LOGGER.warn(e.getLocalizedMessage(), e);
		}
	}


	@Override
	public void close() throws Exception {
		if (serverStatus != null) {
			serverStatus.close();
			serverStatus = null;
		}
	}

	/**
	 * Baixa, somente uma vez, a lista de processos que devem ser processados no PJe.
	 *
	 * @param baixaDados
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws DadosInvalidosException 
	 */
	private void op1BaixarListaProcesssoSeNecessario(int grau) throws IOException, SQLException {

		try (Op_1_BaixaListaDeNumerosDeProcessos baixaListaProcessos = new Op_1_BaixaListaDeNumerosDeProcessos(grau)) {
			if (!baixaListaProcessos.getArquivoSaida().exists()) {
				try {
					executandoOperacao1BaixandoLista = true;
					baixaListaProcessos.baixarListaProcessos();
				} finally {
					executandoOperacao1BaixandoLista = false;
				}
			}
		}
		
		for (String numeroProcesso : Auxiliar.carregarListaProcessosDoArquivo(Auxiliar.getArquivoListaProcessosPje(grau))) {
			processosFluxos.add(new ProcessoFluxo(grau, numeroProcesso));
		}
	}
	
	/**
	 * Inicia uma thread, em background, que ficará monitorando as alterações nos status dos processos
	 */
	private void iniciarAtualizacaoStatusBackground() {
		new Thread(() -> {
			Auxiliar.prepararThreadLog();
			
			while (isAlgumProcessoComStatusAnterior(ProcessoSituacaoEnum.CONCLUIDO) && !ControleAbortarOperacao.instance().isDeveAbortar()) {
				for (ProcessoFluxo processoFluxo : processosFluxos) {
					processoFluxo.identificarSituacao();
				}
				
				Auxiliar.safeSleep(5_000);
			}
			
		}).start();
	}
	
	/**
	 * Inicia uma thread, em background, que ficará gerando os XMLs enquanto houver processos pendentes
	 */
	private void iniciarOperacoesGeracaoEnvioValidacaoEmBackground() {
		
		// Gera XMLs enquanto houver processos pendentes de geração
		new Thread(() -> {
			Auxiliar.prepararThreadLog();
			
			while (isAlgumProcessoSemXML() && !ControleAbortarOperacao.instance().isDeveAbortar()) {
				
				// TODO: Somente instanciar esses objetos se realmente existirem processos na fase correta (FILA)
				try {
					this.executandoOperacao2Geracao = true;
					Op_2_GeraXMLsIndividuais.executarOperacaoGeracaoXML(false);
				} catch (Exception e) {
					ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(10);
				} finally {
					this.executandoOperacao2Geracao = false;
				}
				
				ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(20);
			}
			
		}).start();
		
		// Envia arquivos enquanto houver processos pendentes de envio
		new Thread(() -> {
			Auxiliar.prepararThreadLog();
			
			// TODO: Somente instanciar esses objetos se realmente existirem processos na fase correta (XML_GERADO)
			while (isAlgumProcessoComStatusAnterior(ProcessoSituacaoEnum.ENVIADO) && !ControleAbortarOperacao.instance().isDeveAbortar()) {
				
				if (isAlgumProcessoComStatus(ProcessoSituacaoEnum.XML_GERADO)) {
					try {
						this.executandoOperacao4Envio = true;
						Op_4_ValidaEnviaArquivosCNJ operacao4Envia = new Op_4_ValidaEnviaArquivosCNJ();
						operacao4Envia.localizarEnviarXMLsAoCNJ();
					} catch (Exception e) {
						LOGGER.error("Erro enviando dados ao CNJ: " + e.getLocalizedMessage(), e);
					} finally {
						this.executandoOperacao4Envio = false;
					}
				}
				
				ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(20);
			}
			
		}).start();

		// Confere protocolos no CNJ enquanto houver protocolos pendentes de conferência
		new Thread(() -> {
			Auxiliar.prepararThreadLog();
			
			// Espera todos os arquivos serem marcados como ENVIADOS
			while (isAlgumProcessoComStatusAnterior(ProcessoSituacaoEnum.ENVIADO) && !ControleAbortarOperacao.instance().isDeveAbortar()) {
				ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(20);
			}
			
			while (isAlgumProcessoComStatusAnterior(ProcessoSituacaoEnum.CONCLUIDO) && !ControleAbortarOperacao.instance().isDeveAbortar()) {
				try {
					this.executandoOperacao5Conferencia = true;
					Op_5_ConfereProtocolosCNJ operacao = new Op_5_ConfereProtocolosCNJ();
					operacao.localizarProtocolosConsultarNoCNJ();
					operacao.gravarTotalProtocolosRecusados();
					
					AcumuladorExceptions.instance().removerException("Conferência de protocolos no CNJ");
				} catch (Exception e) {
					AcumuladorExceptions.instance().adicionarException("Conferência de protocolos no CNJ", e.getLocalizedMessage(), e, true);
					
				} finally {
					this.executandoOperacao5Conferencia = false;
				}
				
				ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(20);
			}
			
		}).start();
		
		// TODO: Retornar esse código quando o serviço estiver funcionando adequadamente (hoje só dá timeout)
		new Thread(() -> {
			Auxiliar.prepararThreadLog();

			// Quando não houver mais nada a fazer, encerra a operação.
			while (!isTodosProcessosComStatus(ProcessoSituacaoEnum.CONCLUIDO)) {
				ControleAbortarOperacao.instance().aguardarTempoEnquantoNaoEncerrado(5);
			}
			ControleAbortarOperacao.instance().setAbortar(true);
			
		}).start();
	}

	private boolean isAlgumProcessoSemXML() {
		return processosFluxos.stream().anyMatch(p -> !p.getArquivoXML().exists());
	}
	
	private boolean isTodosProcessosComStatus(ProcessoSituacaoEnum status) {
		int ordem = status.getOrdem();
		return processosFluxos.stream().allMatch(p -> p.getSituacao().getOrdem() == ordem);
	}
	
	private boolean isAlgumProcessoComStatus(ProcessoSituacaoEnum status) {
		int ordem = status.getOrdem();
		return processosFluxos.stream().anyMatch(p -> p.getSituacao().getOrdem() == ordem);
	}
	
	private boolean isAlgumProcessoComStatusAnterior(ProcessoSituacaoEnum status) {
		int ordem = status.getOrdem();
		return processosFluxos.stream().anyMatch(p -> p.getSituacao().getOrdem() < ordem);
	}
	
	public Collection<ProcessoFluxo> getProcessosFluxos() {
		return processosFluxos;
	}
	
	public boolean isExecutandoOperacao2Geracao() {
		return executandoOperacao2Geracao;
	}
	
	public boolean isExecutandoOperacao4Envio() {
		return executandoOperacao4Envio;
	}
	
	public boolean isExecutandoOperacao1BaixandoLista() {
		return executandoOperacao1BaixandoLista;
	}
	
	public boolean isExecutandoOperacao5Conferencia() {
		return executandoOperacao5Conferencia;
	}
	
	public boolean isExecutandoAlgumaOperacao() {
		return isExecutandoOperacao1BaixandoLista() || isExecutandoOperacao2Geracao() || isExecutandoOperacao4Envio() || isExecutandoOperacao5Conferencia();
	}
	
	public static void main(String[] args) throws Exception {
		
		// Não utilizar a interface AWT nessa operação (que será acompanhada via web)
		ProgressoInterfaceGrafica.setTentarMontarInterface(false);
		Auxiliar.prepararPastaDeSaida();
		
		Op_Y_OperacaoFluxoContinuo operacaoCompleta = new Op_Y_OperacaoFluxoContinuo();
		operacaoCompleta.iniciar();
	}
}