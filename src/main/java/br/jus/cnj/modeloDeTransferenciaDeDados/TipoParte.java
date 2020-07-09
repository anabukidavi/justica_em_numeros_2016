//
// Este arquivo foi gerado pela Arquitetura JavaTM para Implementação de Referência (JAXB) de Bind XML, v2.2.8-b130911.1802 
// Consulte <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas as modificações neste arquivo serão perdidas após a recompilação do esquema de origem. 
// Gerado em: 2020.07.09 às 02:03:35 PM BRT 
//


package br.jus.cnj.modeloDeTransferenciaDeDados;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 				Tipo de elemento que permite a identificação de uma
 * 				parte que compõe o processo. Cada parte deve ter apenas
 * 				uma pessoa
 * 				física ou jurídica.
 * 			
 * 
 * <p>Classe Java de tipoParte complex type.
 * 
 * <p>O seguinte fragmento do esquema especifica o conteúdo esperado contido dentro desta classe.
 * 
 * <pre>
 * &lt;complexType name="tipoParte">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="pessoa" type="{http://www.cnj.jus.br/modelo-de-transferencia-de-dados-1.0}tipoPessoa"/>
 *           &lt;element name="interessePublico" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element name="advogado" type="{http://www.cnj.jus.br/modelo-de-transferencia-de-dados-1.0}tipoRepresentanteProcessual" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="pessoaProcessualRelacionada" type="{http://www.cnj.jus.br/modelo-de-transferencia-de-dados-1.0}tipoParte" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="assistenciaJudiciaria" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="intimacaoPendente">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int">
 *             &lt;minInclusive value="0"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="relacionamentoProcessual" type="{http://www.cnj.jus.br/modelo-de-transferencia-de-dados-1.0}modalidadeRelacionamentoProcessual" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tipoParte", propOrder = {
    "pessoa",
    "interessePublico",
    "advogado",
    "pessoaProcessualRelacionada"
})
public class TipoParte {

    protected TipoPessoa pessoa;
    protected String interessePublico;
    protected List<TipoRepresentanteProcessual> advogado;
    protected List<TipoParte> pessoaProcessualRelacionada;
    @XmlAttribute(name = "assistenciaJudiciaria")
    protected Boolean assistenciaJudiciaria;
    @XmlAttribute(name = "intimacaoPendente")
    protected Integer intimacaoPendente;
    @XmlAttribute(name = "relacionamentoProcessual")
    protected ModalidadeRelacionamentoProcessual relacionamentoProcessual;

    /**
     * Obtém o valor da propriedade pessoa.
     * 
     * @return
     *     possible object is
     *     {@link TipoPessoa }
     *     
     */
    public TipoPessoa getPessoa() {
        return pessoa;
    }

    /**
     * Define o valor da propriedade pessoa.
     * 
     * @param value
     *     allowed object is
     *     {@link TipoPessoa }
     *     
     */
    public void setPessoa(TipoPessoa value) {
        this.pessoa = value;
    }

    /**
     * Obtém o valor da propriedade interessePublico.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInteressePublico() {
        return interessePublico;
    }

    /**
     * Define o valor da propriedade interessePublico.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInteressePublico(String value) {
        this.interessePublico = value;
    }

    /**
     * Gets the value of the advogado property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the advogado property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAdvogado().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TipoRepresentanteProcessual }
     * 
     * 
     */
    public List<TipoRepresentanteProcessual> getAdvogado() {
        if (advogado == null) {
            advogado = new ArrayList<TipoRepresentanteProcessual>();
        }
        return this.advogado;
    }

    /**
     * Gets the value of the pessoaProcessualRelacionada property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pessoaProcessualRelacionada property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPessoaProcessualRelacionada().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TipoParte }
     * 
     * 
     */
    public List<TipoParte> getPessoaProcessualRelacionada() {
        if (pessoaProcessualRelacionada == null) {
            pessoaProcessualRelacionada = new ArrayList<TipoParte>();
        }
        return this.pessoaProcessualRelacionada;
    }

    /**
     * Obtém o valor da propriedade assistenciaJudiciaria.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAssistenciaJudiciaria() {
        return assistenciaJudiciaria;
    }

    /**
     * Define o valor da propriedade assistenciaJudiciaria.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAssistenciaJudiciaria(Boolean value) {
        this.assistenciaJudiciaria = value;
    }

    /**
     * Obtém o valor da propriedade intimacaoPendente.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIntimacaoPendente() {
        return intimacaoPendente;
    }

    /**
     * Define o valor da propriedade intimacaoPendente.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIntimacaoPendente(Integer value) {
        this.intimacaoPendente = value;
    }

    /**
     * Obtém o valor da propriedade relacionamentoProcessual.
     * 
     * @return
     *     possible object is
     *     {@link ModalidadeRelacionamentoProcessual }
     *     
     */
    public ModalidadeRelacionamentoProcessual getRelacionamentoProcessual() {
        return relacionamentoProcessual;
    }

    /**
     * Define o valor da propriedade relacionamentoProcessual.
     * 
     * @param value
     *     allowed object is
     *     {@link ModalidadeRelacionamentoProcessual }
     *     
     */
    public void setRelacionamentoProcessual(ModalidadeRelacionamentoProcessual value) {
        this.relacionamentoProcessual = value;
    }

}
