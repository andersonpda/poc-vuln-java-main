package br.com.bradseg.portlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.faces.event.AjaxBehaviorEvent;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;

import br.com.bradseg.bucb.servicos.model.service.ContaCorrenteSOAPImpl;
import br.com.bradseg.bucb.servicos.model.service.WebServiceBusinessException;
import br.com.bradseg.bucb.servicos.model.service.WebServiceIntegrationException;
import br.com.bradseg.bucb.servicos.model.vo.ContaCorrenteVO;
import br.com.bradseg.bucb.servicos.model.vo.CorrentistaVO;
import br.com.bradseg.enums.EstadoCivil;
import br.com.bradseg.enums.ModalidadePrevidencia;
import br.com.bradseg.enums.OrigemRecurso;
import br.com.bradseg.enums.PaisOrigem;
import br.com.bradseg.enums.ParentescoBeneficiario;
import br.com.bradseg.enums.ParentescoPatrocinador;
import br.com.bradseg.enums.PerfilCliente;
import br.com.bradseg.enums.PerguntasFatca;
import br.com.bradseg.enums.Profissao;
import br.com.bradseg.enums.RegimeTributario;
import br.com.bradseg.enums.TipoUsuarioEnum;
import br.com.bradseg.exception.SessionDataServiceException;
import br.com.bradseg.servicos.ConsumoApiRequisicaoXML;
import br.com.bradseg.servicos.ContrataServices;
import br.com.bradseg.servicos.PumaService;
import br.com.bradseg.util.CleanPath;
import br.com.bradseg.util.ConstantesServicos;
import br.com.bradseg.util.ErroTela;
import br.com.bradseg.util.LogService;
import br.com.bradseg.util.LoggerFactory;
import br.com.bradseg.util.RetornoMockApi;
import br.com.bradseg.util.contratacaoRisco.Utils;
import br.com.bradseg.vo.Beneficiario;
import br.com.bradseg.vo.BeneficioIncluirRascunhoEnvioVo;
import br.com.bradseg.vo.BeneficioValidarPropostaInscricaoVo;
import br.com.bradseg.vo.CepVo;
import br.com.bradseg.vo.CorretorSessionHelper;
import br.com.bradseg.vo.DadosTelaEtapa1Vo;
import br.com.bradseg.vo.DetalhaFundoRetornoVo;
import br.com.bradseg.vo.DetalhaParticipanteRetornoVo;
import br.com.bradseg.vo.ExibicaoTelaOfertaVo;
import br.com.bradseg.vo.GeraPropostaEnvioVo;
import br.com.bradseg.vo.IncluirRascunhoEnvioVo;
import br.com.bradseg.vo.IncluirRascunhoRetornoVo;
import br.com.bradseg.vo.LinhasVo;
import br.com.bradseg.vo.ListaBeneficiariosIncluirRascunhoEnvioVo;
import br.com.bradseg.vo.ListaBeneficiariosValidarPropostaInscricaoVo;
import br.com.bradseg.vo.ListaBeneficiosRiscoVo;
import br.com.bradseg.vo.ListaFatcasVo;
import br.com.bradseg.vo.ListaPlanosVo;
import br.com.bradseg.vo.ObterParceiroNegocioAtivoResponseVo;
import br.com.bradseg.vo.OfertaPlanoEnvioVo;
import br.com.bradseg.vo.OfertaPlanoRetornoVo;
import br.com.bradseg.vo.OfertarBeneficiosRiscoEnvioVo;
import br.com.bradseg.vo.OfertarBeneficiosRiscoRetornoVo;
import br.com.bradseg.vo.OpComercialTO;
import br.com.bradseg.vo.ParametrosBeneficioRiscoEnvioVo;
import br.com.bradseg.vo.ParametrosBeneficioRiscoRetornoVo;
import br.com.bradseg.vo.Patrocinador;
import br.com.bradseg.vo.Pep;
import br.com.bradseg.vo.QuadroResumoRisco;
import br.com.bradseg.vo.RetornoPumaVO;
import br.com.bradseg.vo.TelaRiscoEtapa1;
import br.com.bradseg.vo.TelaRiscoEtapa2;
import br.com.bradseg.vo.TelaRiscoEtapa3;
import br.com.bradseg.vo.TelaRiscoEtapa4;
import br.com.bradseg.vo.TelefoneVo;
import br.com.bradseg.vo.ValidaPropostaInscricaoEnvioVo;
import br.com.bradseg.vo.ValidaPropostaInscricaoRetornoVo;
import br.com.bradseg.vo.ValidarBeneficiosRiscoRetornoVo;
import br.com.bradseg.vo.ValidarEnvio;
import br.com.bradseg.vo.ValidarEnvioBeneficiarios;
import br.com.bradseg.vo.ValidarEnvioBeneficios;
import br.com.bradseg.vo.contratacaoRisco.ModalDetalhe;
import br.com.bradseg.vo.contratacaoRisco.ParametroSelecao;
import br.com.bradseg.vo.contratacaoRisco.ParentescoBeneficiarioRisco;
import br.com.bradseg.vo.contratacaoRisco.TelaOfertaRisco;
import br.com.bradseg.vo.contratacaoRisco.TelaOfertaRiscoBeneficios;
import br.com.bradseg.vo.contratacaoRisco.TelaOfertaRiscoPrazo;

@ManagedBean(name="contrataPrev", eager=true)
@SessionScoped
public class ContrataPrevPortlet
{
    private boolean mockApi = false;
    private boolean mockSimularPuma = false;
    private boolean mockTesteSucursal = false;

    private String erro = "";
    private String cpf = "";
    private String dataNascimento;
    private String sexo;
    private String codCorretor = "";
    private String dddCel;
    private String nrCel;
    private Integer idadeAposentadoria = 0;
    private String gerenteComercial;
    private Integer somaPorcentagem = 0;
    private String valorContribuicao;
    private String mensagemValidacaoConta = "";
    private Long cnpjEmpresa = null;
    private String cpfCorretor;
    private String cnpjCorretor;
    private String tpoCorretor;
    private String nomeTpoCorretor;
    private String estruturaComercial;
    private boolean exibeRede;
    private boolean exibeMercado;
    private boolean exibeSucursalDigitada;
    private boolean autoPreenchimentoCorretor;
    private String codCorretorTela;
    private String descricaoSucursal;
    private String codSucursal;
    private String codSucursalDigitado;
	private boolean exibicaoPgVgbl;
    private boolean exibicaoRisco;
    private boolean flagExplicaoRisco;
    private String dtNascimentoBeneficiarioRisco = "";
    private String sexoBeneficiarioRisco;

    private boolean msgGenerica;
    private String msgErro;
    private boolean exibeModalConfirmacaoPlano;
    private boolean exibeModalDetalheOferta;
    private boolean flagExplicacaoPlanos;
    private boolean exibePep;
    private boolean exibeRelacionamentoAgPublico;
    private boolean exibeNacionalidadeAlemBrasileira;
    private boolean exibeResidenciaForaBrasil;
    private boolean exibeVistoOutroPais;
    private boolean exibirDadosContaBancaria;
    private String pep;
    private String relacionamentoAgPublico;
    private String nacionalidadeAlemBrasileira;
    private String residenciaForaBrasil;
    private String vistoOutroPais;
    private boolean editaSexo;
    private boolean editaDtNascimento;
    private boolean editaCpf;
    private boolean editaPep;
    private boolean editaAgentePep;
    private boolean mostrarPlanoJovem = false;
    private LogService logService = new LogService();
    private ErroTela erroTela = new ErroTela();
    private ContrataServices service = new ContrataServices();
    private List<ListaPlanosVo> listaPlanosExibicao = new ArrayList<ListaPlanosVo>();
    private Integer[] listaPerfil = new Integer[4];
    private Integer[] listaPerguntasFatca1 = new Integer[5];
    private Integer[] listaPerguntasFatca2 = new Integer[5];
    private Integer[] listaPerguntasFatca3 = new Integer[5];
    private String rentabilidade3;
    private String rentabilidade6;
    private String rentabilidade12;
    private String paginaRecebidaDetalhes;
    private Integer respondeuPergunta1;
    private Integer respondeuPergunta2;
    private Integer respondeuPergunta3;
    private Integer codPais1;
    private Integer codPais2;
    private Integer codPais3;
    private String docPais1;
    private String docPais2;
    private String docPais3;
    private String downloadPDF = "";
    private String prazoRecebimento;
    private boolean planoJovem = false;
    private boolean exibePerguntaPlanoJovem;
    private String ofertarPlanosJovem;
    private boolean cssBotaoDesabilitado = false;

    private DetalhaParticipanteRetornoVo detalhaParticipante;
    private OfertaPlanoEnvioVo ofertaPlanoEnvio;
    private OfertaPlanoRetornoVo ofertaPlanoRetorno;
    private ListaPlanosVo selecionado;
    private IncluirRascunhoEnvioVo incluirRascunhoEnvio;
    private ValidaPropostaInscricaoEnvioVo validaPropostaInscricaoEnvio;
    private IncluirRascunhoRetornoVo incluirRascunhoRetorno;
    private ValidaPropostaInscricaoRetornoVo validaPropostaInscricaoRetorno;
    private DetalhaFundoRetornoVo detalhaFundoRetorno;
    private DadosTelaEtapa1Vo dadosTelaEtapa1 = new DadosTelaEtapa1Vo();
    private Pep objetoPep;
    private Beneficiario adicaoBeneficiario;
    private Beneficiario beneficiarioRisco = new Beneficiario();
    private List<Beneficiario> listaBeneficiarios = new ArrayList<Beneficiario>();
    private List<Beneficiario> listaBeneficiariosRisco = new ArrayList<Beneficiario>();
    private Patrocinador patrocinador;
    private CepVo cepVO = new CepVo();
    private boolean erroCep = false;
    private String perfilClienteTelaOferta = "";
    private boolean filtroOferta = false;
    private String consisteCep;
    private boolean validaCamposIndex = false;
    private ObterParceiroNegocioAtivoResponseVo obterParceiroNegocioAtivoResponse = new ObterParceiroNegocioAtivoResponseVo();
    ConsumoApiRequisicaoXML consumoApiRequisicaoXML = new ConsumoApiRequisicaoXML();
    private List<OpComercialTO> opComercialTO = obterParceiroNegocioAtivoResponse.getOpComercialTO();

    private boolean bloquearInputEndereco = false;
    private boolean bloquearInputBairro = false;
    private boolean bloquearInputCidade = false;
    private boolean bloquearInputUF = false;
    private boolean bloquearInputEnderecoPatrocinador = false;
    private boolean bloquearInputBairroPatrocinador = false;
    private boolean bloquearInputCidadePatrocinador = false;
    private boolean bloquearInputUFPatrocinador = false;
    private GeraPropostaEnvioVo geraPropostaEnvio;
    private OfertarBeneficiosRiscoEnvioVo ofertarBeneficiosRiscoEnvioVo = new OfertarBeneficiosRiscoEnvioVo();
    private OfertarBeneficiosRiscoRetornoVo ofertarBeneficiosRiscoRetorno = new OfertarBeneficiosRiscoRetornoVo();
    private ParametrosBeneficioRiscoEnvioVo parametrosBeneficioRiscoEnvioVo = new ParametrosBeneficioRiscoEnvioVo();
    private ParametrosBeneficioRiscoRetornoVo parametrosBeneficioRiscoRetornoVo = new ParametrosBeneficioRiscoRetornoVo();
    private ValidarBeneficiosRiscoRetornoVo validarBeneficiosRiscoRetornoVo = new ValidarBeneficiosRiscoRetornoVo();
    @SuppressWarnings("unused")
    private List<ExibicaoTelaOfertaVo> exibicaoTelaOfertaVo = new ArrayList<ExibicaoTelaOfertaVo>();
    private QuadroResumoRisco quadroResumoRisco = new QuadroResumoRisco();
    private ModalDetalhe modalDetalhe = new ModalDetalhe();
    private TelaRiscoEtapa1 telaRiscoEtapa1 = new TelaRiscoEtapa1();
    private TelaRiscoEtapa2 telaRiscoEtapa2 = new TelaRiscoEtapa2();
    private TelaRiscoEtapa3 telaRiscoEtapa3 = new TelaRiscoEtapa3();
    private TelaRiscoEtapa4 telaRiscoEtapa4 = new TelaRiscoEtapa4();
    private boolean bloquearInputEnderecoRisco = false;
    private boolean bloquearInputBairroRisco = false;
    private boolean bloquearInputCidadeRisco = false;
    private boolean bloquearInputUFRisco = false;
    // Risco
    private List<TelaOfertaRisco> listaTelaOfertaRisco = new ArrayList<TelaOfertaRisco>();
    private TelaOfertaRisco telaOfertaRisco = new TelaOfertaRisco();
    private Utils utils = new Utils();
    private Gson gson = new Gson();
    private ParametroSelecao parametroSelecao = new ParametroSelecao();
    private int contribuicaoRisco = 1; // comeca com mensal
    private int prazoRecebimentoRisco = 0;
    private String sliderValue = "";
    private String sliderValueTela = "";
    private int modalidade = 0;
    private boolean temRegistro = false;
    private List<LinhasVo> listaErro = new ArrayList<LinhasVo>();
    private int modalidadeRisco = 0;
    private boolean exibeModalOfertaRisco;
    private boolean confirmacaoHabilitado = false;
    private String urlLaminaRent;
    private Map<String,String> rentabilidadeOferta = new HashMap<String,String>();
    private String identCorretor;
    
    private boolean mensagemEstruturaMercado;
    private boolean mensagemEstruturaRede;
    private boolean mensagemEstruturaMercadoRisco;
    private boolean mensagemEstruturaRedeRisco;
    
    private boolean exibirDadosPix;
    
    private boolean piloto = false;

    public String getLimparCampos()
    {
        if (!this.validaCamposIndex)
        {
            removerAtributosSessao();
        }
        validaCamposIndex = true;
        this.modalidade = 0;

        return "";
    }

    private void removerAtributosSessao()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getSessionMap().remove("#{contrataPrev}");
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.invalidate();
    }

    @PostConstruct
    public void init()
    {
        this.erro = "ContrataPrev|init|Iniciando metodo construtor";
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        this.telaOfertaRisco.setContribuicao(this.contribuicaoRisco);
        listaPerfil[0] = 0;
        listaPerfil[1] = 0;
        listaPerfil[2] = 0;
        listaPerfil[3] = 0;

        try
        {
            if (this.mockApi)
            {
                cnpjEmpresa = 10769203000109L;
            } 
            else
            {
                this.erro = "ContrataPrev|init|Recuperar dados do Usuario Logado";
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
                
                RetornoPumaVO retUserPuma = PumaService.buscarUsuarioLogado();
            
				if (retUserPuma.getCnpjCorret() != null 
				&& !retUserPuma.getCnpjCorret().isEmpty()){
					if (retUserPuma.getCnpjCorret().equals("43338235000109")) {
						setPiloto(true);
					}
				}
				
                this.setCpfCorretor(retUserPuma.getCpfCorret());
                this.setCnpjCorretor(retUserPuma.getCnpjCorret());

                TipoUsuarioEnum tipoUsuario = CorretorSessionHelper.getTipoUsuario();

                this.setTpoCorretor(tipoUsuario.getId());
                this.setNomeTpoCorretor(tipoUsuario.getTipo());

                this.erro = "ContrataPrev|init|ID USUARIO LOGADO" + this.tpoCorretor;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erro = "ContrataPrev|init|DESCRIÇÃO USUARIO LOGADO" + this.nomeTpoCorretor;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erro = "ContrataPrev|init|CPF CORRETOR LOGADO" + this.cpfCorretor;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erro = "ContrataPrev|init|CNPJ CORRETOR LOGADO" + this.cnpjCorretor;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erro = "ContrataPrev|init|Atributo request TIPO_USUARIO_PARAMETER: " + this.nomeTpoCorretor;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            }
            this.erro = "ContrataPrev|init|buscaUsuarioLogado|CNPJ: " + cnpjEmpresa;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
        catch (SessionDataServiceException e)
        {
            this.erro = "ContrataPrev|init|ERRO na sessao do usuario logado: " +  
                CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
    }

    public String ofertaPage()
    {
        return "oferta.xhtml";
    }

    public String doPesquisarParticipante()
    {
    	if(this.getModalidadeRisco() > 0) 
    	{
    		this.setModalidade(this.getModalidadeRisco());
    	}

    	setDetalhaParticipante(new DetalhaParticipanteRetornoVo());
        setOfertaPlanoEnvio(new OfertaPlanoEnvioVo());
        setOfertaPlanoRetorno(new OfertaPlanoRetornoVo());
        setSelecionado(new ListaPlanosVo());
        setIncluirRascunhoEnvio(new IncluirRascunhoEnvioVo());
        setValidaPropostaInscricaoEnvio(new ValidaPropostaInscricaoEnvioVo());
        setIncluirRascunhoRetorno(new IncluirRascunhoRetornoVo());
        setValidaPropostaInscricaoRetorno(new ValidaPropostaInscricaoRetornoVo());
        setSelecionado(new ListaPlanosVo());
        setAdicaoBeneficiario(new Beneficiario());
        setPatrocinador(new Patrocinador());

        setEditaSexo(false);
        setEditaDtNascimento(false);
        setEditaCpf(false);
        validaCamposIndex = true;

        if (null == this.cpf || this.cpf.trim().isEmpty() || retiraMascara(this.cpf).length() < 11
                || !isCpfValido(retiraMascara(this.cpf)))
        {
            this.erroTela.setCpf(true);
        } 
        else
        {
            this.erroTela.setCpf(false);
        }

        if (null == this.dataNascimento || this.dataNascimento.trim().isEmpty()
                || retiraMascara(this.dataNascimento).length() < 8
                || !isDataValida(retiraMascara(this.dataNascimento)))
        {
            this.erroTela.setDtNascimento(true);
        } 
        else
        {
            this.erroTela.setDtNascimento(false);
        }

        if (null == this.sexo || this.sexo.trim().isEmpty())
        {
            this.erroTela.setSexo(true);
        } 
        else
        {
            this.erroTela.setSexo(false);
        }

        if (this.erroTela.isCpf() || this.erroTela.isDtNascimento() || this.erroTela.isSexo())
        {
            return "index.xhtml";
        }

        if (this.mockApi)
        {
            final RetornoMockApi retornoMockApi = new RetornoMockApi();
            final String retorno = retornoMockApi.retornarDetalhaParticipante(); 
            setDetalhaParticipante( new Gson().fromJson( retorno, DetalhaParticipanteRetornoVo.class ) ); 
        } 
        else
        {
            if (calculaIdade(this.dataNascimento) < 24)
            {
                setExibePerguntaPlanoJovem(true);
            } 
            else
            {
                setExibePerguntaPlanoJovem(false);
            }

            this.detalhaParticipante = service.detalharParticipante(retiraMascara(this.cpf),
                    adicionaPontoData(this.dataNascimento), this.sexo);

            //
            // quando API nao retorna: http 500
            //
            if (null == this.detalhaParticipante)
            {
                this.detalhaParticipante = new DetalhaParticipanteRetornoVo();
                this.erroTela.setModal(false);
                setEditaSexo(true);
                setEditaDtNascimento(true);
                setEditaCpf(true);
                this.setValidaCamposIndex(false);
                return "plano.xhtml";
            }

            if (!this.detalhaParticipante.getCodRetorno().equals(0))
            {
                if (this.detalhaParticipante.getMsgRetorno2().trim().equalsIgnoreCase("cliente nao encontrado"))
                {
                    this.erroTela.setModal(false);
                    setEditaSexo(true);
                    setEditaDtNascimento(true);
                    setEditaCpf(true);
                    this.setValidaCamposIndex(false);
                    return "plano.xhtml";
                } 
                else
                {
                    this.erroTela.setModal(true);
                    setMsgErro(converteEncodingTexto(this.detalhaParticipante.getMsgRetorno() + ". "
                            + this.detalhaParticipante.getMsgRetorno2()));
                    return "index.xhtml";
                }
            }
        }

        this.erroTela.setModal(false);
        this.setValidaCamposIndex(false);
        return "plano.xhtml";
    }

    public String doPesquisarOfertas()
    {
        this.erroTela.setFlagExplicacaoRisco(false);
        this.erroTela.setNascimentoBeneficiarioRisco(false);
        this.erroTela.setSexoBeneficiarioRisco(false);
        
    	if(this.getModalidadeRisco() > 0) {
    		this.setModalidade(this.getModalidadeRisco());
    	}
    	
        if (!isFlagExplicaoRisco() && isExibicaoRisco())
        {
            this.erroTela.setFlagExplicacaoRisco(true);
        }

        if ((null == this.dtNascimentoBeneficiarioRisco || this.dtNascimentoBeneficiarioRisco.trim().isEmpty()
                || retiraMascara(this.dtNascimentoBeneficiarioRisco).length() < 8
                || !isDataValida(retiraMascara(this.dtNascimentoBeneficiarioRisco))) && isExibicaoRisco())
        {
            this.erroTela.setNascimentoBeneficiarioRisco(true);
        }

        if ((null == this.sexoBeneficiarioRisco || this.sexoBeneficiarioRisco.trim().isEmpty())
                && isExibicaoRisco())
        {
            this.erroTela.setSexoBeneficiarioRisco(true);
        }

        if (isExibePerguntaPlanoJovem() && isExibicaoPgVgbl())
        {
            if (getOfertarPlanosJovem() == null || getOfertarPlanosJovem().trim().isEmpty())
            {
                this.erroTela.setPerguntaPlanoJovem(true);
            } 
            else
            {
                this.erroTela.setPerguntaPlanoJovem(false);
            }
        } 
        else
        {
            this.erroTela.setPerguntaPlanoJovem(false);
        }

        if (flagExplicacaoPlanos == false && isExibicaoPgVgbl())
        {
            this.erroTela.setFlag(true);
        } 
        else
        {
            this.erroTela.setFlag(false);
        }
        if (this.ofertaPlanoEnvio.getModalidade() == null)
        {
            this.erroTela.setModalidadePlano(true);
        } 
        else
        {
            this.erroTela.setModalidadePlano(false);
        }

        if (this.ofertaPlanoEnvio.getTipoContribuicao() == null && isExibicaoPgVgbl())
        {
            this.erroTela.setContribUnicaMensal(true);
        } 
        else
        {
            this.erroTela.setContribUnicaMensal(false);
        }

        if (this.ofertaPlanoEnvio.getRegimeTributario() == null && isExibicaoPgVgbl())
        {
            this.erroTela.setRegimeTributario(true);
        } 
        else
        {
            this.erroTela.setRegimeTributario(false);
        }

        if ((this.valorContribuicao == null || retiraMascara(this.valorContribuicao).trim().isEmpty()
                || retiraMascara(this.valorContribuicao).equalsIgnoreCase("000")) && isExibicaoPgVgbl())
        {
            this.erroTela.setContribuicaoCliente(true);
        } 
        else
        {
            this.erroTela.setContribuicaoCliente(false);
        }

        if (this.erroTela.isFlag() || this.erroTela.isModalidadePlano() || this.erroTela.isContribUnicaMensal()
                || this.erroTela.isRegimeTributario() || this.erroTela.isContribuicaoCliente()
                || this.erroTela.isPerguntaPlanoJovem() || this.erroTela.isFlagExplicacaoRisco()
                || this.erroTela.isNascimentoBeneficiarioRisco() || this.erroTela.isSexoBeneficiarioRisco())
        {
            return "plano.xhtml";
        }

        if (isExibicaoRisco())
        {
            this.servicosBeneficioRisco();
            this.erroTela.setPrazo(false);

            if ( this.erroTela.isModal() )
            {
                return "plano.xhtml";
            }

            return "contratacaoRisco/ofertaRisco.xhtml";
        }

        setExibeModalConfirmacaoPlano(true);

        return "plano.xhtml";
    }

    public void servicosBeneficioRisco()
    {
        String retorno = "";

        this.erroTela.setOfertaRisco(false);
        this.erroTela.setModal( false );

        this.ofertaPlanoRetorno = new OfertaPlanoRetornoVo();
        this.listaTelaOfertaRisco = new ArrayList<TelaOfertaRisco>();

        this.ofertaPlanoEnvio.setCpf(retiraMascara(this.cpf));
        this.ofertaPlanoEnvio.setPerfil(0);
        this.ofertaPlanoEnvio.setAgencia(0);
        this.ofertaPlanoEnvio.setConta(0);
        this.ofertaPlanoEnvio.setSegmento(0);
        this.ofertaPlanoEnvio.setValorContribuicao(0.0);
        this.ofertaPlanoEnvio.setRegimeTributario(0);
        this.ofertaPlanoEnvio.setTipoContribuicao(this.telaOfertaRisco.getContribuicao());

        final String ofertaPlanoEnvioParam = this.gson.toJson(this.ofertaPlanoEnvio);

        this.erro = "Risco|servicosBeneficioRisco|Envio API OfertaPlano: " + ofertaPlanoEnvioParam;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if (this.mockApi)
        {
            final RetornoMockApi mock = new RetornoMockApi();
            retorno = mock.retornarOfertaPlano();
        } 
        else
        {
            retorno = service.pesquisarOfertasRisco(this.ofertaPlanoEnvio);
        }
        
        this.erro = "Risco|servicosBeneficioRisco|Retorno API OfertaPlano: " + retorno;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if ( null == retorno )
        {
            this.erroTela.setOfertaRisco(true);
            this.erroTela.setModal( true );
            this.setMsgErro("Erro ao processar Oferta do Risco");
            return;
        }

        try
        {
            this.ofertaPlanoRetorno = this.gson.fromJson(retorno, OfertaPlanoRetornoVo.class);
        } 
        catch ( JsonParseException e)
        {
            this.erro = "Risco|servicosBeneficioRisco|Retorno API OfertaPlano: " + retorno;
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erroTela.setOfertaRisco(true);
            this.erroTela.setModal( true );
            this.setMsgErro("Erro ao processar Oferta do Risco");
            return;
        }

        final String ofertaPlanoRetornoRetornoParam = this.gson.toJson(this.ofertaPlanoRetorno);

        this.erro = "Risco|servicosBeneficioRisco|Retorno API OfertaPlano: " + ofertaPlanoRetornoRetornoParam;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        this.setTemRegistro( true );

        if ( 0 == this.ofertaPlanoRetorno.getListaPlanos().size() )
        {
            this.setTemRegistro( false );
            return;
        }

        exibicaoTelaOfertaVo = new ArrayList<ExibicaoTelaOfertaVo>();

        for (ListaPlanosVo itemPlano : this.ofertaPlanoRetorno.getListaPlanos())
        {
            this.parametroSelecao.setRegimeTributario("" + itemPlano.getRegimeTributario());
            this.telaOfertaRisco.setCodIndTributacao("" + itemPlano.getRegimeTributario());
            this.telaOfertaRisco.setCodProdutoComercial(itemPlano.getCodProdutoComercial());
            this.telaOfertaRisco.setNomeProduto(itemPlano.getDsBeneficioTecnico());

            ofertarBeneficiosRiscoEnvioVo = new OfertarBeneficiosRiscoEnvioVo();

            ofertarBeneficiosRiscoEnvioVo.setCodPlano(itemPlano.getCodPlano());
            this.parametroSelecao.setCodPlano(itemPlano.getCodPlano());
            ofertarBeneficiosRiscoEnvioVo.setCodProdutoNegocio(Integer.parseInt(itemPlano.getCodProdutoComercial().trim()));
            ofertarBeneficiosRiscoEnvioVo.setDtNascimentoParticipante(adicionaPontoData(this.dataNascimento));
            ofertarBeneficiosRiscoEnvioVo.setDtNascimentoPatrocinador("0");
            ofertarBeneficiosRiscoEnvioVo.setTipoContribuicao(this.telaOfertaRisco.getContribuicao());

            final String ofertaBeneficioParam = this.gson.toJson(this.ofertarBeneficiosRiscoEnvioVo);

            this.erro = "Risco|servicosBeneficioRisco|Envio API OfertaBeneficio: " + ofertaBeneficioParam;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            if (this.mockApi)
            {
                final RetornoMockApi mock = new RetornoMockApi();
                retorno = mock.ofertarBeneficiosRisco();
            } 
            else
            {
                retorno = service.ofertarBeneficioRisco(ofertarBeneficiosRiscoEnvioVo);
            }

            this.erro = "Risco|servicosBeneficioRisco|Retorno API OfertaBeneficio: " + retorno;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            if ( null == retorno )
            {
                this.erroTela.setOfertaRisco(true);
                this.erroTela.setModal( true );
                this.setMsgErro("Erro ao processar Benefícios da Oferta do Risco");
                return;
            }

            try
            {
                this.ofertarBeneficiosRiscoRetorno = this.gson.fromJson(retorno, OfertarBeneficiosRiscoRetornoVo.class);
            } 
            catch ( JsonParseException e)
            {
                this.erro = "Risco|servicosBeneficioRisco|ERR Retorno API ParametroBeneficio: " + retorno;
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erroTela.setOfertaRisco(true);
                this.erroTela.setModal( true );
                this.setMsgErro("Erro ao processar Benefícios da Oferta do Risco");
                return;
            }

            for (ListaBeneficiosRiscoVo itemBeneficio : ofertarBeneficiosRiscoRetorno.getBeneficiosRisco())
            {
                this.telaOfertaRisco.getBeneficios().setPensao(false);

                if ( itemBeneficio.getExibePrazoRecebimento().equals( "S" ) )
                {
                    if ( this.parametroSelecao.getIdadeBeneficiario() < itemBeneficio.getIdadeMinBeneficiario() || 
                         this.parametroSelecao.getIdadeBeneficiario() > itemBeneficio.getIdadeMaxBeneficiario() )
                    {
                        this.setTemRegistro( false );
                        this.erroTela.setOfertaRisco(true);
                        this.erroTela.setModal( true );
                        this.setMsgErro( "Idade do beneficiario deve ser entre " + 
                            itemBeneficio.getIdadeMinBeneficiario() + " e " + itemBeneficio.getIdadeMaxBeneficiario() + " anos." );
                        return;
                    }

                    if ( this.parametroSelecao.getIdadeParticipante() < itemBeneficio.getIdadeMinParticipante() || 
                         this.parametroSelecao.getIdadeParticipante() > itemBeneficio.getIdadeMaxParticipante() )
                    {
                        this.setTemRegistro( false );
                        this.erroTela.setOfertaRisco(true);
                        this.erroTela.setModal( true );
                        this.setMsgErro( "Idade do participante deve ser entre " + 
                            itemBeneficio.getIdadeMinParticipante() + " e " + itemBeneficio.getIdadeMaxParticipante() + " anos." );
                        return;
                    }

                    this.telaOfertaRisco.getBeneficios().setPensao( true );
                }

                if (itemBeneficio.getExibePrazoRecebimento().equals("S"))
                {
                    this.telaOfertaRisco.getBeneficios().setCodBeneficioPensao(itemBeneficio.getCodBeneficio());
                    this.telaOfertaRisco.setPlanoPgt("" + itemPlano.getCodPlano());
                    this.telaOfertaRisco.getBeneficios().setBeneficioPrevPensao("" + itemBeneficio.getCodBeneficio());
                    this.telaOfertaRisco.getBeneficios().setPossuiBeneficiario(itemBeneficio.getPossuiBeneficiario());

                    if ( this.telaOfertaRisco.isPrimeiraChamada() )
                    {
                        final List< TelaOfertaRiscoPrazo > listaPrazo = new ArrayList< TelaOfertaRiscoPrazo >();
    
                        for (int i = 5; i <= 20; i += 5)
                        {
                            final TelaOfertaRiscoPrazo prazo = new TelaOfertaRiscoPrazo();
                            prazo.setCodigo( i );
                            prazo.setDescricao( i + " anos" );
                            listaPrazo.add( prazo );
                        }
    
                        this.telaOfertaRisco.getBeneficios().setPrazo( listaPrazo );
                    
                        this.prazoRecebimentoRisco = 5;
                    }

                    this.telaOfertaRisco.getBeneficios().setPrazoRecebimento( this.prazoRecebimentoRisco );
                } 
                else
                {
                    this.telaOfertaRisco.getBeneficios().setBeneficioPrevPeculio("" + itemBeneficio.getCodBeneficio());
                    this.telaOfertaRisco.getBeneficios().setCodBeneficioPeculio(itemBeneficio.getCodBeneficio());
                }

                if (this.telaOfertaRisco.getBeneficios().isPensao())
                {
                    this.telaOfertaRisco.getBeneficios().setSusepPensao( itemBeneficio.getCodProcessoSusep());
                    this.telaOfertaRisco.getBeneficios().setExibePrazoRecebimento( itemBeneficio.getExibePrazoRecebimento().equals("S") ? true : false);
                    this.telaOfertaRisco.getBeneficios().setNomeBeneficio(itemBeneficio.getNomeBeneficio());
                } 
                else
                {
                    this.telaOfertaRisco.getBeneficios().setSusepPeculio( itemBeneficio.getCodProcessoSusep());
                    this.telaOfertaRisco.getBeneficios().setNomeBeneficioPeculio(itemBeneficio.getNomeBeneficio());
                }

                parametrosBeneficioRiscoEnvioVo = new ParametrosBeneficioRiscoEnvioVo();

                parametrosBeneficioRiscoEnvioVo.setCodBeneficio(itemBeneficio.getCodBeneficio().toString());
                parametrosBeneficioRiscoEnvioVo.setCodPlano(parametrosBeneficioRiscoEnvioVo.getCodBeneficio());
                parametrosBeneficioRiscoEnvioVo.setCodPlanoJovem("0");
                parametrosBeneficioRiscoEnvioVo.setCpf(retiraMascara(this.cpf));
                parametrosBeneficioRiscoEnvioVo.setCpfPatrocinador("0");
                parametrosBeneficioRiscoEnvioVo.setDtNascimento(adicionaPontoData(this.dataNascimento));
                parametrosBeneficioRiscoEnvioVo.setDtNascimentoPatrocinador("0");
                parametrosBeneficioRiscoEnvioVo.setSexo(this.sexo);
                parametrosBeneficioRiscoEnvioVo.setSexoPatrocinador("0");
                parametrosBeneficioRiscoEnvioVo.setTpContribuicao("" + this.telaOfertaRisco.getContribuicao());

                // Wagner retirar linhas abaixo apos a API estar ok.
                parametrosBeneficioRiscoEnvioVo.setCpfPatrocinador("268891290");
                parametrosBeneficioRiscoEnvioVo.setDtNascimentoPatrocinador("01.08.1973");
                parametrosBeneficioRiscoEnvioVo.setSexoPatrocinador("F");
                //

                final String parametroBeneficioParam = this.gson.toJson(this.parametrosBeneficioRiscoEnvioVo);

                this.erro = "Risco|servicosBeneficioRisco|Envio API ParametroBeneficio: " + parametroBeneficioParam;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                if (this.mockApi)
                {
                    final RetornoMockApi mock = new RetornoMockApi();
                    retorno = mock.parametroBeneficioRisco();
                } 
                else
                {
                    retorno = service.parametroBeneficioRisco(parametrosBeneficioRiscoEnvioVo);
                }

                this.erro = "Risco|servicosBeneficioRisco|Retorno API ParametroBeneficio: " + retorno;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                if ( null == retorno )
                {
                    this.erroTela.setOfertaRisco(true);
                    this.erroTela.setModal( true );
                    this.setMsgErro("Erro ao processar Parâmetro do Benefícios Risco");
                    return;
                }

                try
                {
                    this.parametrosBeneficioRiscoRetornoVo = this.gson.fromJson(retorno, ParametrosBeneficioRiscoRetornoVo.class);
                } 
                catch ( JsonParseException e)
                {
                    this.erro = "Risco|servicosBeneficioRisco|ERR Retorno API ParametroBeneficio: " + retorno;
                    this.logService.error( this.erro );
                    LoggerFactory.getLogger().log(Level.INFO, this.erro );

                    this.erroTela.setOfertaRisco(true);
                    this.erroTela.setModal( true );
                    this.setMsgErro("Erro ao processar Parâmetro do Benefícios Risco");
                    return;
                }

                if (this.telaOfertaRisco.getBeneficios().isPensao())
                {
                    this.telaOfertaRisco.setLimitePensao( false );
                    
                    if ( 0 == this.parametrosBeneficioRiscoRetornoVo.getVlrMinRenda() )
                    {
                        this.telaOfertaRisco.setLimitePensao( true );
                    }
                    
                    this.telaOfertaRisco.getBeneficios().setVlrMinRenda( String.valueOf(this.parametrosBeneficioRiscoRetornoVo.getVlrMinRenda()));

                    if (this.sliderValue.isEmpty())
                    {
                        this.sliderValue = "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMinRenda();
                    }

                    this.telaOfertaRisco.getBeneficios().setVlrMaxRenda( String.valueOf(this.parametrosBeneficioRiscoRetornoVo.getVlrMaxRenda()));
                    this.telaOfertaRisco.getBeneficios().setPrazoRecebimento(this.parametrosBeneficioRiscoRetornoVo.getPrazoMinCobertura());
                    this.telaOfertaRisco.getBeneficios().setClienteIdadeMin( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMinParticipante() );
                    this.telaOfertaRisco.getBeneficios().setClienteIdadeMax( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMaxParticipante() );
                    this.telaOfertaRisco.getBeneficios().setBeneficiarioIdadeMin( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMinBeneficiario() );
                    this.telaOfertaRisco.getBeneficios().setBeneficiarioIdadeMax( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMaxBeneficiario() );
                    this.telaOfertaRisco.getBeneficios().setCoberturaMin( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMinCobertura() );
                    this.telaOfertaRisco.getBeneficios().setCoberturaMax( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMaxCobertura() );
                    this.telaOfertaRisco.getBeneficios().setPremioMinimo( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMinContribuicao() );
                    this.telaOfertaRisco.getBeneficios().setPremioMaximo( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMaxContribuicao() );
                } 
                else
                {
                    this.telaOfertaRisco.setLimitePeculio( false );
                    
                    if ( 0 == this.parametrosBeneficioRiscoRetornoVo.getVlrMinRenda() )
                    {
                        this.telaOfertaRisco.setLimitePeculio( true );
                    }

                    this.telaOfertaRisco.setFatorBenef(itemBeneficio.getMultiplaPensao());
                    this.telaOfertaRisco.getBeneficios().setPeculioIdadeMin( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMinParticipante() );
                    this.telaOfertaRisco.getBeneficios().setPeculioIdadeMax( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMaxParticipante() );
                    this.telaOfertaRisco.getBeneficios().setPeculioBeneficiarioIdadeMin( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMinBeneficiario() );
                    this.telaOfertaRisco.getBeneficios().setPeculioBeneficiarioIdadeMax( "" + this.parametrosBeneficioRiscoRetornoVo.getIdadeMaxBeneficiario() );
                    this.telaOfertaRisco.getBeneficios().setPeculioCoberturaMin( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMinCobertura() );
                    this.telaOfertaRisco.getBeneficios().setPeculioCoberturaMax( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMaxCobertura() );
                    this.telaOfertaRisco.getBeneficios().setPeculioPremioMinimo( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMinContribuicao() );
                    this.telaOfertaRisco.getBeneficios().setPeculioPremioMaximo( "" + this.parametrosBeneficioRiscoRetornoVo.getVlrMaxContribuicao() );
                }
                
                if ( !this.telaOfertaRisco.getBeneficios().isPensao() )
                {
                    try
                    {
                        final double vlrSlider = Double.valueOf( this.sliderValue );
                        double vlrPeculio = vlrSlider * itemBeneficio.getMultiplaPensao();
                        vlrPeculio = Math.round( vlrPeculio * 100  );
                        vlrPeculio /= 100;
                        this.telaOfertaRisco.getBeneficios().setVlrPeculio( "" + vlrPeculio );
                    }
                    catch( NumberFormatException e )
                    {
                        this.erro = "Risco|servicosBeneficioRisco|ERR conversao de valores: " + this.sliderValue
                                + " multiplicador: " + itemBeneficio.getMultiplaPensao();
                        this.logService.error( this.erro );
                        LoggerFactory.getLogger().log(Level.INFO, this.erro );
                        
                        this.erroTela.setOfertaRisco(true);
                        this.erroTela.setModal( true );
                        this.setMsgErro("Erro ao processar cáculo do Pecúlio");
                        return;
                    }
                }

                final ValidarEnvioBeneficiarios beneficiarios = new ValidarEnvioBeneficiarios();

                beneficiarios.setDataNascBeneficiario(adicionaPontoData(this.dtNascimentoBeneficiarioRisco));
                beneficiarios.setSexoBeneficiario(this.sexoBeneficiarioRisco);
                beneficiarios.setGrauParentBeneficiario("0");
                beneficiarios.setPercentualBeneficiario(100.00);

                final ValidarEnvioBeneficios beneficios = new ValidarEnvioBeneficios();

                beneficios.setCodBeneficio(itemBeneficio.getCodBeneficio());
                beneficios.setPrazoRecebimento( 0 );

                if ( this.telaOfertaRisco.getBeneficios().isPensao() )
                {
                    if (this.telaOfertaRisco.getRevalidar() )
                    {
                        beneficios.setPrazoRecebimento(this.prazoRecebimentoRisco);
                    } 
                    else
                    {
                        beneficios.setPrazoRecebimento(parametrosBeneficioRiscoRetornoVo.getPrazoMinCobertura());
                    }
                }

                beneficios.setPrazoCobertura(0);
                beneficios.setValContribuicao("0.00");

                if (this.telaOfertaRisco.getBeneficios().isPensao())
                {
                    beneficios.setValBeneficio(Double.valueOf(this.sliderValue));
                } 
                else
                {
                    beneficios.setValBeneficio(Double.valueOf(this.telaOfertaRisco.getBeneficios().getVlrPeculio()));
                }

                beneficios.setQtdBeneficiario(1);
                beneficios.getBeneficiarios().add(beneficiarios);

                final ValidarEnvio validarEnvio = new ValidarEnvio();

                validarEnvio.setCodProdNegocio(Integer.parseInt(itemPlano.getCodProdutoComercial()));
                validarEnvio.setCodPlano(itemPlano.getCodPlano());
                validarEnvio.setTpContribuicao(this.contribuicaoRisco);
                validarEnvio.setDataAposentadoria("01.08.2065");
                validarEnvio.setDataNascimentoPartic(adicionaPontoData(this.dataNascimento));
                validarEnvio.setCpfPartic(retiraMascara(this.cpf));
                validarEnvio.setSexoPartic(this.sexo);
                validarEnvio.setDataNascPatrocinador(" ");
                validarEnvio.setSexoPatrocinador(" ");
                validarEnvio.setQtdBeneficio(1); // ofertarBeneficiosRiscoRetorno.getBeneficiosRisco().size());
                validarEnvio.getBeneficios().add(beneficios);

                final String validarParam = this.gson.toJson(validarEnvio);

                this.erro = "Risco|servicosBeneficioRisco|Envio API Validar: " + validarParam;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                if (this.mockApi)
                {
                    final RetornoMockApi mock = new RetornoMockApi();
                    retorno = mock.validarBeneficiosRisco();
                } 
                else
                {
                    retorno = service.validaBeneficioRisco(validarEnvio);
                }
                
                this.erro = "Risco|servicosBeneficioRisco|Retorno API ParametroBeneficio: " + retorno;
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                if ( null == retorno )
                {
                    this.erroTela.setOfertaRisco(true);
                    this.erroTela.setModal( true );
                    this.setMsgErro(this.converteEncodingTexto("Erro ao processar Validação do Parâmetro do Benefícios Risco"));
                    return;
                }

                try
                {
                    this.validarBeneficiosRiscoRetornoVo = this.gson.fromJson(retorno, ValidarBeneficiosRiscoRetornoVo.class);
                } 
                catch ( JsonParseException e)
                {
                    this.erro = "Risco|servicosBeneficioRisco|ERR Retorno API Validar: " + retorno;
                    this.logService.error( this.erro );
                    LoggerFactory.getLogger().log(Level.INFO, this.erro );
                    
                    this.erroTela.setOfertaRisco(true);
                    this.erroTela.setModal( true );
                    this.setMsgErro(this.converteEncodingTexto("Erro ao processar Validação do Parâmetro do Benefícios Risco"));
                    return;
                }

                if (this.telaOfertaRisco.getBeneficios().isPensao())
                {
                    this.telaOfertaRisco.getBeneficios().setPremioMensal( "" + this.validarBeneficiosRiscoRetornoVo.getBeneficiosRisco().get(0).getValContribCalculada());
                    this.telaOfertaRisco.getBeneficios().setValContribuicaoPensao( "" + this.validarBeneficiosRiscoRetornoVo.getBeneficiosRisco().get(0).getValContribCalculada());
                } 
                else
                {
                    this.telaOfertaRisco.getBeneficios().setPremioMensalPeculio(String.valueOf( this.validarBeneficiosRiscoRetornoVo.getBeneficiosRisco().get(0).getValContribCalculada()));
                    this.telaOfertaRisco.getBeneficios().setValContribuicaoPeculio("" + this.validarBeneficiosRiscoRetornoVo.getBeneficiosRisco().get(0).getValContribCalculada());
                }
            }
            
            if ( 1 == this.listaTelaOfertaRisco.size() )
            {
                this.listaTelaOfertaRisco.remove( 0 );
            }
            
            this.listaTelaOfertaRisco.add(this.telaOfertaRisco);     
        }

        this.telaOfertaRisco.setPrimeiraChamada( false );
        this.telaOfertaRisco.setRevalidar(false);
        this.listaTelaOfertaRisco.set( 0, telaOfertaRisco );
    }

    public String doAvancarParaOfertas()
    {
        if (this.mockApi)
        {
            final RetornoMockApi mockApi = new RetornoMockApi();
            final String retorno = mockApi.retornarOfertaPlano();
            this.ofertaPlanoRetorno = this.gson.fromJson(retorno, OfertaPlanoRetornoVo.class);
        } 
        else
        {
            this.ofertaPlanoEnvio.setCpf(retiraMascara(this.cpf));
            this.ofertaPlanoEnvio.setPerfil(0);
            this.ofertaPlanoEnvio.setAgencia(0);
            this.ofertaPlanoEnvio.setConta(0);
            this.ofertaPlanoEnvio.setSegmento(0);
            String x = valorContribuicao.replace(".", "");
            x = adicionaDecimal(x).replace(",", ".");
            this.ofertaPlanoEnvio.setValorContribuicao(Double.parseDouble(x));
            this.ofertaPlanoRetorno = service.pesquisarOfertasPrevidencia(this.ofertaPlanoEnvio);

            if ( null == this.ofertaPlanoRetorno )
            {
                this.erroTela.setModal(true);
                setExibeModalConfirmacaoPlano(false);
                setMsgErro( "Erro ao obter Planos." );
                return "plano.xhtml";
            }

            if (!this.ofertaPlanoRetorno.getCodigoRetorno().equals(0) || x.equals("0.00"))
            {
                this.erroTela.setModal(true);
                setExibeModalConfirmacaoPlano(false);
                setMsgErro(converteEncodingTexto("Nenhum plano encontrado com os parâmetros informados."));

                if (this.filtroOferta)
                {
                    listaPlanosExibicao = new ArrayList<ListaPlanosVo>();
                    return "oferta.xhtml";
                } 
                else
                {
                    return "plano.xhtml";
                }
            }
        }

		/*
		 * rentabilidadeOferta = new HashMap<>();
		 * 
		 * for (ListaPlanosVo lista : this.ofertaPlanoRetorno.getListaPlanos()) {
		 * this.detalhaFundoRetorno =
		 * service.detalhaFundo(lista.getCodPlano().toString(),
		 * lista.getCodBeneficio().toString()); if (null != this.detalhaFundoRetorno) {
		 * calculaRentabilidadeFundo();
		 * rentabilidadeOferta.put(lista.getCodPlano().toString()+"/"+lista.
		 * getCodBeneficio().toString(), rentabilidade12); } }
		 */
        setListaPlanosExibicao(this.ofertaPlanoRetorno.getListaPlanos());

        this.erroTela.setModal(false);
        setExibeModalConfirmacaoPlano(false);
        final int idade = calculaIdade(this.dataNascimento);
        
        if ( -1 == idade )
        {
            this.erro = "ContrataPrev|doAvancarParaOfertas|idade incorreta.";
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        }
        
        if ( idade > 23 || getOfertarPlanosJovem().equalsIgnoreCase("N"))
        {
            List<ListaPlanosVo> listaPlanosJovem = new ArrayList<ListaPlanosVo>();
            int size = this.listaPlanosExibicao.size();
            for (int i = 0; i <= size - 1; i++)
            {
                if ("S".trim().equalsIgnoreCase(this.listaPlanosExibicao.get(i).getIndJovem()))
                {
                    listaPlanosJovem.add(this.listaPlanosExibicao.get(i));
                }
            }

            if (!listaPlanosJovem.isEmpty())
            {
                for (ListaPlanosVo x : listaPlanosJovem)
                {
                    this.listaPlanosExibicao.remove(x);
                }
            }
        }

        if (null != getOfertarPlanosJovem() && getOfertarPlanosJovem().equalsIgnoreCase("S"))
        {
            List<ListaPlanosVo> listaPlanosJovem = new ArrayList<ListaPlanosVo>();
            int size = this.listaPlanosExibicao.size();
            for (int i = 0; i <= size - 1; i++)
            {
                if ("N".trim().equalsIgnoreCase(this.listaPlanosExibicao.get(i).getIndJovem()))
                {
                    listaPlanosJovem.add(this.listaPlanosExibicao.get(i));
                }
            }

            if (!listaPlanosJovem.isEmpty())
            {
                for (ListaPlanosVo x : listaPlanosJovem)
                {
                    this.listaPlanosExibicao.remove(x);
                }
            }
        }

        if (this.listaPlanosExibicao.isEmpty())
        {
            this.erroTela.setModal(true);
            setExibeModalConfirmacaoPlano(false);
            setMsgErro(converteEncodingTexto("Nenhum plano encontrado com os parâmetros informados."));

            if (this.filtroOferta)
            {
                listaPlanosExibicao = new ArrayList<ListaPlanosVo>();
                return "oferta.xhtml";
            } 
            else
            {
                return "plano.xhtml";
            }
        }

        if (null != this.detalhaParticipante && this.detalhaParticipante.getCodRetorno().equals(0)  && null != this.detalhaParticipante.getPerfilCliente() && 
        	 !this.filtroOferta)
        {
            setPerfilClienteTelaOferta(" (perfil " + this.detalhaParticipante.getPerfilCliente() + ")" );

            if (this.detalhaParticipante.getPerfilCliente().equalsIgnoreCase("conservador"))
            {
                setListaPerfil(new Integer[4]);
                listaPerfil[0] = 1;
                listaPerfil[1] = 0;
                listaPerfil[2] = 0;
                listaPerfil[3] = 0;
            }
            else if (this.detalhaParticipante.getPerfilCliente().equalsIgnoreCase("moderado"))
            {
                setListaPerfil(new Integer[4]);
                listaPerfil[0] = 2;
                listaPerfil[1] = 0;
                listaPerfil[2] = 0;
                listaPerfil[3] = 0;
            }
            else if (this.detalhaParticipante.getPerfilCliente().equalsIgnoreCase("dinamico"))
            {
                setListaPerfil(new Integer[4]);
                listaPerfil[0] = 3;
                listaPerfil[1] = 0;
                listaPerfil[2] = 0;
                listaPerfil[3] = 0;
            }
            else if (this.detalhaParticipante.getPerfilCliente().equalsIgnoreCase("arrojado"))
            {
                setListaPerfil(new Integer[4]);
                listaPerfil[0] = 4;
                listaPerfil[1] = 0;
                listaPerfil[2] = 0;
                listaPerfil[3] = 0;
            }
            else
            {
                setPerfilClienteTelaOferta( "" );
            }

            filtroPerfilPlanos();
        }

        if (this.filtroOferta)
        {
            filtroPerfilPlanos();
        }

        return "oferta.xhtml";
    }

    // recebe String data no formato dd/mm/yyyy
    public int calculaIdade(String dataNascimento)
    {
        try
        {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date data = formatter.parse(dataNascimento);
            Calendar cData = Calendar.getInstance();
            Calendar cHoje = Calendar.getInstance();
            cData.setTime(data);
            cData.set(Calendar.YEAR, cHoje.get(Calendar.YEAR));
            int idade = cData.after(cHoje) ? -1 : 0;
            cData.setTime(data);
            idade += cHoje.get(Calendar.YEAR) - cData.get(Calendar.YEAR);

            return idade;
        }
        catch( ParseException e )
        {
            return -1;
        }
    }

    public boolean pesquisarContaCorrente(String agencia, String conta, boolean modoRisco)
    {
        if (this.mockApi)
        {
            return true;
        }

        try
        {
            javax.naming.Context ctx;
            ctx = new javax.naming.InitialContext();
            Object URL = ctx.lookup(ConstantesServicos.URL_ACESSO_CONTA_CORRENTE_SOAP);
            // Add porta
            StringBuilder urlPorta = new StringBuilder(
                URL.toString().replaceAll("https", "http") +
                "/BUCB-Servicos-ws/services/ContaCorrenteSOAPImpl/wsdl/ContaCorrenteSOAPImpl.wsdl" );

            final String sanitize = CleanPath.cleanString( urlPorta.toString() );
            URL url = new URL( sanitize );

            this.erro = "ContrataPrev|pesquisarContaCorrente|pesquisarContaCorrente url: " + url.toString();
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            QName qname = new QName("http://service.model.servicos.bucb.bradseg.com.br", "ContaCorrenteSOAPImplService");

            Service service = Service.create(url, qname);

            ContaCorrenteSOAPImpl contaCorrenteInterface = service.getPort(ContaCorrenteSOAPImpl.class);

            ContaCorrenteVO vo = contaCorrenteInterface.consultarCorrentistasPorAgenciaContaCorrente(null, null,
                    Integer.parseInt(agencia), Long.parseLong(conta));

            Long cpfCliente;
            String tipoConta;

            if (modoRisco) 
            {
            	cpfCliente = Long.parseLong(retiraMascara(getTelaRiscoEtapa1().getCpf()));
            	tipoConta = this.telaRiscoEtapa3.getTpContaParticipante();
			} 
            else 
            {

				tipoConta = this.incluirRascunhoEnvio.getTpContaParticipante();
				if (this.selecionado.getIndJovem().equalsIgnoreCase("S"))
	            {
	                cpfCliente = Long.parseLong(retiraMascara(getPatrocinador().getCpf()));
	            } 
	            else
	            {
	                cpfCliente = Long.parseLong(retiraMascara(getDadosTelaEtapa1().getCpf()));
	            }
			}
            

            if ( null != vo.getCodigoTipoConta() && 
                 vo.getCodigoTipoConta().equals(Integer.parseInt(tipoConta) ) )
            {
                for (CorrentistaVO correntista : vo.getCorrentistaArrayVO().getCorrentistaVO())
                {
                    if (correntista.getNumeroCpfCnpj().equals(cpfCliente))
                    {
                        return true;
                    }
                }

                if ( null != this.selecionado.getIndJovem() && this.selecionado.getIndJovem().equalsIgnoreCase("S"))
                {
                    setMensagemValidacaoConta(converteEncodingTexto("Os dados bancários devem ser do patrocinador do plano. Favor tentar novamente"));
                } 
                else
                {
                    setMensagemValidacaoConta(converteEncodingTexto("Os dados bancários devem ser do titular do plano. Favor tentar novamente"));
                }

                return false;

            } 
            else
            {
                setMensagemValidacaoConta( converteEncodingTexto("Os dados bancários informados não conferem. Favor tentar novamente"));
                return false;
            }
        } 
        catch (NumberFormatException e)
        {
            this.mensagemValidacaoConta = e.getMessage();
            this.erro = "ContrataPrev|pesquisarContaCorrente|NumberFormatException: " + 
                CleanPath.cleanString( e.getMessage() );
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return false;
        } 
        catch (WebServiceBusinessException e)
        {
            this.mensagemValidacaoConta = converteEncodingTexto("Os dados bancários informados não conferem. Favor tentar novamente");
            this.erro = "ContrataPrev|pesquisarContaCorrente|WebServiceBusinessException: " + 
                    CleanPath.cleanString( e.getMessage() );
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return false;
        } 
        catch (WebServiceIntegrationException e)
        {
            this.mensagemValidacaoConta = e.getMessage();
            this.erro = "ContrataPrev|pesquisarContaCorrente|WebServiceIntegrationException: " + 
                    CleanPath.cleanString( e.getMessage() );
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return false;
        } 
        catch (NamingException e)
        {
            this.mensagemValidacaoConta = e.getMessage();
            this.erro = "ContrataPrev|pesquisarContaCorrente|NamingException: " + 
                    CleanPath.cleanString( e.getMessage() );
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return false;
        } 
        catch (MalformedURLException e)
        {
            this.mensagemValidacaoConta = e.getMessage();
            this.erro = "ContrataPrev|pesquisarContaCorrente|MalformedURLException: " + 
                    CleanPath.cleanString( e.getMessage() );
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return false;
        }
    }

    public String doFecharModalConfirmacaoPlano()
    {
        setExibeModalConfirmacaoPlano(false);
        return "plano.xhtml";
    }

    public String fecharModalConfirmacaoPlanoRisco()
    {
        this.erroTela.setConfirmacaoRisco( false );
        return "../plano.xhtml";
    }

    public String doSelecionarProposta()
    {
        setDadosTelaEtapa1(new DadosTelaEtapa1Vo());
        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            setPlanoJovem(true);
        } 
        else
        {
            setPlanoJovem(false);
        }

        if (!this.editaCpf)
        {
            if (null != this.detalhaParticipante.getCodPais())
            {
                getDadosTelaEtapa1().setPaisNascimento(this.detalhaParticipante.getCodPais());
            }

            if (null != this.detalhaParticipante.getNmParticipante())
            {
                getDadosTelaEtapa1().setNome(this.detalhaParticipante.getNmParticipante().trim());
            }

            if (null != this.detalhaParticipante.getDtNascimento())
            {
                getDadosTelaEtapa1().setDtNascimento(this.detalhaParticipante.getDtNascimento().trim());
            }

            if (null != this.detalhaParticipante.getSexo())
            {
                getDadosTelaEtapa1().setSexo(this.detalhaParticipante.getSexo().trim());
            }

            if (null != this.detalhaParticipante.getEstadoCivil())
            {
                getDadosTelaEtapa1().setEstadoCivil(EstadoCivil.getCodigoByNome(this.detalhaParticipante.getEstadoCivil().trim()));
            }

            if (null != this.detalhaParticipante.getCpf())
            {
                getDadosTelaEtapa1().setCpf(adicionaZeroEsquerda(this.detalhaParticipante.getCpf(), 11));
            }

            if (null != this.detalhaParticipante.getVlrRendaMensal())
            {
                getDadosTelaEtapa1().setRenda(trocarPonto(this.detalhaParticipante.getVlrRendaMensal()));
            }

            if (null != this.detalhaParticipante.getCodProfissao())
            {
                getDadosTelaEtapa1().setProfissao(this.detalhaParticipante.getCodProfissao());
            }

            if (this.detalhaParticipante.getPrimeiroDdd() != null 
            && !this.detalhaParticipante.getPrimeiroDdd().isEmpty() 
            && !(this.detalhaParticipante.getPrimeiroDdd().length() < 2)
            && !(this.detalhaParticipante.getPrimeiroDdd().substring(
        			this.detalhaParticipante.getPrimeiroDdd().length() - 2, 
        			this.detalhaParticipante.getPrimeiroDdd().length()).equals("00"))){
            	
                getDadosTelaEtapa1().setTelResidencial(this.detalhaParticipante.getPrimeiroDdd().trim().substring(
                        this.detalhaParticipante.getPrimeiroDdd().length() - 2,
                        this.detalhaParticipante.getPrimeiroDdd().length())
                        + this.detalhaParticipante.getPrimeiroNrTelefone().toString());
            }else {
            	 getDadosTelaEtapa1().setTelResidencial("");
            }     
            
            if (this.detalhaParticipante.getSegundoDdd() != null 
            && !this.detalhaParticipante.getSegundoDdd().isEmpty() 
            && !(this.detalhaParticipante.getSegundoDdd().length() < 2)
            && !(this.detalhaParticipante.getSegundoDdd().substring(
        			this.detalhaParticipante.getSegundoDdd().length() - 2, 
        			this.detalhaParticipante.getSegundoDdd().length()).equals("00"))){
                getDadosTelaEtapa1().setTelComercial(this.detalhaParticipante.getSegundoDdd().trim().substring(
                        this.detalhaParticipante.getSegundoDdd().length() - 2,
                        this.detalhaParticipante.getSegundoDdd().length())
                        + this.detalhaParticipante.getSegundoNrTelefone().toString());
            }else {
            	 getDadosTelaEtapa1().setTelComercial("");
            }   
            
            if (null != this.detalhaParticipante.getTerceiroDdd())
            {
                getDadosTelaEtapa1().setTel(this.detalhaParticipante.getTerceiroDdd().trim().substring(
                        this.detalhaParticipante.getTerceiroDdd().length() - 2,
                        this.detalhaParticipante.getTerceiroDdd().length())
                        + this.detalhaParticipante.getTerceiroNrTelefone().toString());
            }
           

            if (null != this.detalhaParticipante.getEmail())
            {
                getDadosTelaEtapa1().setEmail(this.detalhaParticipante.getEmail().trim());
            }

            if (null != this.detalhaParticipante.getCep())
            {
                getDadosTelaEtapa1().setCep(adicionaZeroEsquerda(Long.valueOf(this.detalhaParticipante.getCep()), 5)
                        + adicionaZeroEsquerda(Long.valueOf(this.detalhaParticipante.getComplementoCep()), 3));
            }

            if (null != this.detalhaParticipante.getLogradouro())
            {
                getDadosTelaEtapa1().setLogradouro(this.detalhaParticipante.getLogradouro().trim());
            }

            if (null != this.detalhaParticipante.getNrLogradouro())
            {
                getDadosTelaEtapa1().setNumEndereco(this.detalhaParticipante.getNrLogradouro().trim());
            }

            if (null != this.detalhaParticipante.getComplementoLogradouro()
                    && !this.detalhaParticipante.getComplementoLogradouro().trim().isEmpty())
            {
                getDadosTelaEtapa1().setComplemento(this.detalhaParticipante.getComplementoLogradouro().trim());
            }

            if (null != this.detalhaParticipante.getBairro())
            {
                getDadosTelaEtapa1().setBairro(this.detalhaParticipante.getBairro().trim());
            }

            if (null != this.detalhaParticipante.getCidade())
            {
                getDadosTelaEtapa1().setCidade(this.detalhaParticipante.getCidade().trim());
            }

            if (null != this.detalhaParticipante.getUf())
            {
                getDadosTelaEtapa1().setEstado(this.detalhaParticipante.getUf().trim());
            }
        } 
        else
        {
            getDadosTelaEtapa1().setCpf(this.cpf);
            getDadosTelaEtapa1().setDtNascimento(this.dataNascimento);
            getDadosTelaEtapa1().setSexo(this.sexo);
        }

        setDetalhaFundoRetorno(new DetalhaFundoRetornoVo());
        
        if (mockApi)
        {
            RetornoMockApi retornoMockApi = new RetornoMockApi();
            final String retorno = retornoMockApi.retornarDetalhaFundo();
            setDetalhaFundoRetorno( new Gson().fromJson( retorno, DetalhaFundoRetornoVo.class ) );
        } 
        else
        {
            this.detalhaFundoRetorno = service.detalhaFundo(selecionado.getCodPlano().toString(), selecionado.getCodBeneficio().toString());
            
            if ( null == this.detalhaFundoRetorno )
            {
                return "propostaEtapa1.xhtml";
            }
            calculaRentabilidadeFundo();
        }

        return "propostaEtapa1.xhtml";
    }

    public String voltarParaPlano()
    {
        this.erroTela.setOfertaRisco( false );
        this.erroTela.setModal( false );
        this.telaOfertaRisco.setPrimeiraChamada( true );
        this.telaOfertaRisco.setRevalidar( true );
        return "../plano.xhtml";
    }
    
    public String fecharModalOfertaRisco() 
    {
        setExibeModalOfertaRisco(false);
    	return "ofertaRisco.xhtml";
    }

    public String selecionarRisco(boolean avancarEtapa1)
    {
        this.erroTela.setPrazo(false);
        setExibeModalOfertaRisco(false);

        this.parametroSelecao.setIndice(Integer.valueOf( FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("indice")));
        this.parametroSelecao.setCodProdutoComercial(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("codProdutoComercial"));
        this.parametroSelecao.setFatorBenef(Integer.valueOf( FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("fatorBenef")));
        this.parametroSelecao.setPlanoPgt( FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("planoPgt"));
        this.parametroSelecao.setBeneficioPrevPensao(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("beneficioPrevPensao"));
        this.parametroSelecao.setBeneficioPrevPeculio(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("beneficioPrevPeculio"));
        this.parametroSelecao.setPossuiBeneficiario(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("possuiBeneficiario"));
        this.parametroSelecao.setCodBeneficioPensao(Integer.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("codBeneficioPensao")));
        this.parametroSelecao.setCodBeneficioPeculio(Integer.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("codBeneficioPeculio")));
        this.parametroSelecao.setCodIndTributacao(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("codIndTributacao"));

        final TelaOfertaRisco risco = this.listaTelaOfertaRisco.get(this.parametroSelecao.getIndice());

        if ( risco.getLimitePeculio() || risco.getLimitePensao() )
        {
            return "ofertaRisco.xhtml";
        }
        
        final double valorMaximo = Double.parseDouble( risco.getBeneficios().getVlrMaxRenda() );
        final double valorMinimo = Double.parseDouble( risco.getBeneficios().getVlrMinRenda() );
        final double valorSlider = utils.formatarDouble( this.sliderValueTela );
        
        if ( valorSlider < valorMinimo || valorSlider > valorMaximo )
        {
            this.erroTela.setOfertaRisco( true );
            this.msgErro = this.converteEncodingTexto( "Valor da coberturar deve obter valor mínimo e máximo" );
            return "ofertaRisco.xhtml";
        }
        
        this.parametroSelecao.setValContribuicaoPensao( risco.getBeneficios().getValContribuicaoPensao() );
        this.parametroSelecao.setValContribuicaoPeculio( risco.getBeneficios().getValContribuicaoPeculio() );
        this.quadroResumoRisco.setNomePlano(risco.getNomeProduto());
        this.quadroResumoRisco.setNomeBeneficio(risco.getBeneficios().getNomeBeneficio());
        this.quadroResumoRisco.setValorCobertura( this.sliderValueTela );
        this.quadroResumoRisco.setPremioMensal( risco.getBeneficios().getPremioMensalTela() );
        this.quadroResumoRisco.setPrazoPensao("" + this.prazoRecebimentoRisco);
        this.quadroResumoRisco.setNomeBeneficioPeculio(risco.getBeneficios().getNomeBeneficioPeculio());
        this.quadroResumoRisco.setValorCoberturaPeculio(risco.getBeneficios().getVlrPeculioTela());
        this.quadroResumoRisco.setPremioMensalPeculio( risco.getBeneficios().getPremioMensalPeculioTela() );
        this.quadroResumoRisco.setTotalMensal(risco.getBeneficios().getTotalContribCalculadaTela());
        this.quadroResumoRisco.setSusepPensao(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("susepPensao"));
        this.quadroResumoRisco.setSusepPeculio(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("susepPeculio"));

        final TelaOfertaRiscoBeneficios beneficios = risco.getBeneficios();
        this.modalDetalhe.setNomePlano( risco.getNomeProduto() );
        this.modalDetalhe.setNomeBeneficio(risco.getBeneficios().getNomeBeneficio());
        this.modalDetalhe.setClienteIdadeMin( beneficios.getClienteIdadeMin() );
        this.modalDetalhe.setClienteIdadeMax( beneficios.getClienteIdadeMax() );
        this.modalDetalhe.setBeneficiarioIdadeMin( beneficios.getBeneficiarioIdadeMin() );
        this.modalDetalhe.setBeneficiarioIdadeMax( beneficios.getBeneficiarioIdadeMax() );
        this.modalDetalhe.setCoberturaMin( this.utils.formatarValor( beneficios.getCoberturaMin() ) );
        this.modalDetalhe.setCoberturaMax( this.utils.formatarValor( beneficios.getCoberturaMax() ) );
        this.modalDetalhe.setPremioMinimo( this.utils.formatarValor( beneficios.getPremioMinimo() ) );
        this.modalDetalhe.setPremioMaximo( this.utils.formatarValor( beneficios.getPremioMaximo() ) );
        this.modalDetalhe.setSusep( beneficios.getSusepPensao() );
        this.modalDetalhe.setNomeBeneficioPeculio(risco.getBeneficios().getNomeBeneficioPeculio());
        this.modalDetalhe.setPeculioIdadeMin( beneficios.getPeculioIdadeMin() );
        this.modalDetalhe.setPeculioIdadeMax( beneficios.getPeculioIdadeMax() );
        this.modalDetalhe.setPeculioBeneficiarioIdadeMin( beneficios.getPeculioBeneficiarioIdadeMin() );
        this.modalDetalhe.setPeculioBeneficiarioIdadeMax( beneficios.getPeculioBeneficiarioIdadeMax() );
        this.modalDetalhe.setPeculioCoberturaMin( this.utils.formatarValor( beneficios.getPeculioCoberturaMin() ) );
        this.modalDetalhe.setPeculioCoberturaMax( this.utils.formatarValor( beneficios.getPeculioCoberturaMax() ) );
        this.modalDetalhe.setPeculioPremioMinimo( this.utils.formatarValor( beneficios.getPeculioPremioMinimo() ) );
        this.modalDetalhe.setPeculioPremioMaximo( this.utils.formatarValor( beneficios.getPeculioPremioMaximo() ) );
        this.modalDetalhe.setPeculioSusep( beneficios.getSusepPeculio() );

        this.telaRiscoEtapa1.setNome(this.detalhaParticipante.getNmParticipante());
        this.telaRiscoEtapa1.setDtNascimento(this.dataNascimento);
        this.telaRiscoEtapa1.setCpf(this.cpf);
        this.telaRiscoEtapa1.setSexo(this.sexo);
        this.telaRiscoEtapa1.setPaisNascimento( this.detalhaParticipante.getCodPais() == 0 ? 46 : this.detalhaParticipante.getCodPais());
        this.telaRiscoEtapa1.setRenda(this.detalhaParticipante.getVlrRendaMensal() == 0 ? "0,00" : this.utils.formatarValor("" + this.detalhaParticipante.getVlrRendaMensal()));

        String tmp = "";
           
        if (this.detalhaParticipante.getPrimeiroDdd() != null 
        && !this.detalhaParticipante.getPrimeiroDdd().isEmpty() 
        && !(this.detalhaParticipante.getPrimeiroDdd().length() < 2)
        && !(this.detalhaParticipante.getPrimeiroDdd().substring(
        	 this.detalhaParticipante.getPrimeiroDdd().length() - 2, 
        	 this.detalhaParticipante.getPrimeiroDdd().length()).equals("00"))){
        	
             	this.telaRiscoEtapa1.setTelResidencial(this.detalhaParticipante.getPrimeiroDdd().trim().substring(
                        this.detalhaParticipante.getPrimeiroDdd().length() - 2,
                        this.detalhaParticipante.getPrimeiroDdd().length())
                        + this.detalhaParticipante.getPrimeiroNrTelefone().toString());        		
        } else {
            this.telaRiscoEtapa1.setTelResidencial("");
        }
              
        if (this.detalhaParticipante.getSegundoDdd() != null 
        && !this.detalhaParticipante.getSegundoDdd().isEmpty() 
        && !(this.detalhaParticipante.getSegundoDdd().length() < 2)
        && !(this.detalhaParticipante.getSegundoDdd().substring(
    		 this.detalhaParticipante.getSegundoDdd().length() - 2, 
    		 this.detalhaParticipante.getSegundoDdd().length()).equals("00"))){
        	
            	this.telaRiscoEtapa1.setTelComercial(this.detalhaParticipante.getSegundoDdd().trim().substring(
                    this.detalhaParticipante.getSegundoDdd().length() - 2,
                    this.detalhaParticipante.getSegundoDdd().length())
                    + this.detalhaParticipante.getSegundoNrTelefone().toString());
        }else {
            this.telaRiscoEtapa1.setTelComercial("");
        }

        if ( this.detalhaParticipante.getTerceiroDdd().length() == 4 )
        {
            this.telaRiscoEtapa1.setTel(this.detalhaParticipante.getTerceiroDdd().substring(2, 4)  + this.detalhaParticipante.getTerceiroNrTelefone());
        }
        else
        {
            this.telaRiscoEtapa1.setTel( "00" + this.detalhaParticipante.getTerceiroNrTelefone());
        }
        
        if ( this.telaRiscoEtapa1.getTel().length() != 11 )
        {
            this.telaRiscoEtapa1.setTel( "" );
        }
        
        this.telaRiscoEtapa1.setEmail(this.detalhaParticipante.getEmail());

        if (this.detalhaParticipante.getCep() == 0)
        {
            this.telaRiscoEtapa1.setCep("");
        } 
        else
        {
            tmp = "00000" + this.detalhaParticipante.getCep();
            final String tmpCep = tmp.substring(tmp.length() - 5);
            tmp = "000" + this.detalhaParticipante.getComplementoCep();
            final String tmpCompl = tmp.substring(tmp.length() - 3);

            this.telaRiscoEtapa1.setCep(tmpCep + "-" + tmpCompl);
        }

        this.telaRiscoEtapa1.setNumEndereco(this.detalhaParticipante.getNrLogradouro());
        this.telaRiscoEtapa1.setLogradouro(this.detalhaParticipante.getLogradouro());
        this.telaRiscoEtapa1.setBairro(this.detalhaParticipante.getBairro());
        this.telaRiscoEtapa1.setCidade(this.detalhaParticipante.getCidade());
        this.telaRiscoEtapa1.setEstado(this.detalhaParticipante.getUf());

        this.resetErroRiscoEtapa1();

        if( avancarEtapa1 ) 
        {
        	return "riscoEtapa1.xhtml";
        }
        
        setExibeModalOfertaRisco(true);
        
        return "ofertaRisco.xhtml";
    }

    public String doFecharDetalhes()
    {
        setExibeModalDetalheOferta(false);
        return "oferta.xhtml";
    }

    public String doFecharModalIndex()
    {
        this.erroTela.setModal(false);
        return "index.xhtml";
    }

    public String doFecharModalPlano()
    {
        this.erroTela.setModal(false);
        return "plano.xhtml";
    }

    public void fecharModalPlanoRisco()
    {
        this.erroTela.setOfertaRisco(false);
    }

    public String fecharModalConfirmacaoRisco()
    {
        this.erroTela.setConfirmacaoRisco( false );
        return "confirmacaoRisco.xhtml";
    }

    public String fecharModalOferta()
    {
        this.erroTela.setModal(false);
        return "oferta.xhtml";
    }

    public String doFecharModalConfirmacao()
    {
        this.erroTela.setModal(false);
        return "confirmacao.xhtml";
    }

    public void doAlterarPep()
    {
        if (this.pep.equals("S"))
        {
            setExibePep(true);
        } 
        else
        {
            setExibePep(false);
        }

        if (this.relacionamentoAgPublico.equals("S"))
        {
            setExibeRelacionamentoAgPublico(true);
        } 
        else
        {
            setExibeRelacionamentoAgPublico(false);
        }

        if (this.nacionalidadeAlemBrasileira.equals("S"))
        {
            setExibeNacionalidadeAlemBrasileira(true);
        } 
        else
        {
            setExibeNacionalidadeAlemBrasileira(false);
        }

        if (this.residenciaForaBrasil.equals("S"))
        {
            setExibeResidenciaForaBrasil(true);
        } 
        else
        {
            setExibeResidenciaForaBrasil(false);
        }

        if (this.vistoOutroPais.equals("S"))
        {
            setExibeVistoOutroPais(true);
        } 
        else
        {
            setExibeVistoOutroPais(false);
        }

    }
    
    public void doAlterarEstruturaComercial() {
    	this.erroTela.setErroSucursalSap(false);
    	this.erroTela.setEstruturaComercial(false);
    	
    	//this.autoPreenchimentoCorretor = verdadeiro -> encontrou sucursal
    	if (this.autoPreenchimentoCorretor) {
    		if (this.estruturaComercial.equals("mercado")){
    		    setMensagemEstruturaMercado(true);
    		    setMensagemEstruturaRede(false);
    		}else  {
    		    setMensagemEstruturaRede(true);
    		    setMensagemEstruturaMercado(false);
    		}
    	}else {
		    setMensagemEstruturaRede(false);
		    setMensagemEstruturaMercado(false); 
    	}
    	
    	if (this.estruturaComercial.equals("mercado") && !this.autoPreenchimentoCorretor) {
			this.erroTela.setErroSucursalSap(true);
			setExibeRede(false);
            setExibeMercado(false);
		} else { 
    	
	    	if (this.estruturaComercial.equals("rede")) {
	            setExibeRede(true);
	            setExibeMercado(true);
	        	setExibeSucursalDigitada(false);
	        } else {
	            setExibeRede(false);
	        }
	        if (this.estruturaComercial.equals("mercado")) {
	            setExibeMercado(true);
	            if (this.autoPreenchimentoCorretor == false) {
	            	setExibeSucursalDigitada(true);
	            } else {
	            	setExibeSucursalDigitada(false);
	            }
	        }
		}
    }
    
    public void doAlterarEstruturaComercialRisco() {
    	this.erroTela.setErroSucursalSap(false);
    	this.erroTela.setEstruturaComercial(false);
    	
    	//this.telaRiscoEtapa4.getAutoPreenchimentoCorretor = verdadeiro -> encontrou sucursal
    	if (this.telaRiscoEtapa4.getAutoPreenchimentoCorretor()) {
    		if (this.telaRiscoEtapa4.getEstruturaComercial().equals("mercado")){
    		    setMensagemEstruturaMercadoRisco(true);
    		    setMensagemEstruturaRedeRisco(false);
    		}else  {
    		    setMensagemEstruturaRedeRisco(true);
    		    setMensagemEstruturaMercadoRisco(false);
    		}
    	}else {
		    setMensagemEstruturaRedeRisco(false);
		    setMensagemEstruturaMercadoRisco(false); 
    	}

    	if (this.telaRiscoEtapa4.getEstruturaComercial().equals("mercado") && !this.telaRiscoEtapa4.getAutoPreenchimentoCorretor()) {
			
    		this.erroTela.setErroSucursalSap(true);
			this.telaRiscoEtapa4.setExibeRede(false);
    		this.telaRiscoEtapa4.setExibeMercado(false);
    		
		} else {
			if (this.telaRiscoEtapa4.getEstruturaComercial().equals("rede")) {
	    		this.telaRiscoEtapa4.setExibeRede(true);
	    		this.telaRiscoEtapa4.setExibeMercado(true);
	    		this.telaRiscoEtapa4.setExibeSucursalDigitada(false);
	        } else {
	        	this.telaRiscoEtapa4.setExibeRede(false);
	        }
	    	if (this.telaRiscoEtapa4.getEstruturaComercial().equals("mercado")) {
	    		this.telaRiscoEtapa4.setExibeMercado(true);
	    		if (this.telaRiscoEtapa4.getAutoPreenchimentoCorretor() == false) {
	            	this.telaRiscoEtapa4.setExibeSucursalDigitada(true);
	            } else {
	            	this.telaRiscoEtapa4.setExibeSucursalDigitada(false);
	            }
	    	}
		}
    }

    public void alterarPepRisco()
    {
        this.telaRiscoEtapa2.setExibePep(false);
        this.telaRiscoEtapa2.setExibeRelacionamentoAgPublico(false);
        this.telaRiscoEtapa2.setExibeNacionalidadeAlemBrasileira(false);
        this.telaRiscoEtapa2.setExibeResidenciaForaBrasil(false);
        this.telaRiscoEtapa2.setExibeVistoOutroPais(false);

        if (this.telaRiscoEtapa2.getPep().equals("S"))
        {
            this.telaRiscoEtapa2.setExibePep(true);
        }

        if (this.telaRiscoEtapa2.getRelacionamentoAgPublico().equals("S"))
        {
            this.telaRiscoEtapa2.setExibeRelacionamentoAgPublico(true);
        }

        if (this.telaRiscoEtapa2.getNacionalidadeAlemBrasileira().equals("S"))
        {
            this.telaRiscoEtapa2.setExibeNacionalidadeAlemBrasileira(true);
        }

        if (this.telaRiscoEtapa2.getResidenciaForaBrasil().equals("S"))
        {
            this.telaRiscoEtapa2.setExibeResidenciaForaBrasil(true);
        }

        if (this.telaRiscoEtapa2.getVistoOutroPais().equals("S"))
        {
            this.telaRiscoEtapa2.setExibeVistoOutroPais(true);
        }
    }

    public void doIncluirCodCorretor(AjaxBehaviorEvent event)
    {
        String sucursalSelecionada = getCodCorretorTela();
        
        for (OpComercialTO retornoSIPN : opComercialTO) {
			if (retornoSIPN.getCodSucursal().equals(sucursalSelecionada)){
		        setCodCorretor(retornoSIPN.getCodBVP());
		        setDescricaoSucursal(retornoSIPN.getDescricaoSucursal());
		        setCodSucursal(retornoSIPN.getCodSucursal());
			}
		} 
    }

    public void doIncluirCodCorretorRisco(AjaxBehaviorEvent event)
    {
    	String sucursalSelecionada = telaRiscoEtapa4.getCodCorretorTela();
    	  
        for (OpComercialTO retornoSIPN : opComercialTO) {
			if (retornoSIPN.getCodSucursal().equals(sucursalSelecionada)){
				 telaRiscoEtapa4.setCodCorretor(retornoSIPN.getCodBVP());
				 setDescricaoSucursal(retornoSIPN.getDescricaoSucursal());
				 setCodSucursal(retornoSIPN.getCodSucursal());
			}
		}  
    }

    public void doAlterarFormaPagamento()
    {
        this.incluirRascunhoEnvio.setAgenciaDebito(new String());
        this.incluirRascunhoEnvio.setConta(new String());
        this.incluirRascunhoEnvio.setTpContaParticipante(new String());

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1"))
        {
            setExibirDadosContaBancaria(true);
        } 
        else
        {
            setExibirDadosContaBancaria(false);
        }
        
        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("5"))
        {
            setExibirDadosPix(true);
        } 
        else
        {
            setExibirDadosPix(false);
        }
    }

    public void alterarFormaPagamentoRisco()
    {
        this.getTelaRiscoEtapa3().setExibirDadosContaBancaria(false);
        this.getTelaRiscoEtapa3().setExibirDadosPix(false);

        if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("1"))
        {
            this.getTelaRiscoEtapa3().setExibirDadosContaBancaria(true);
        }	
        
        if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("5"))
        {
            this.getTelaRiscoEtapa3().setExibirDadosPix(true);
        } 
       
    }

    public void doAdicionarBeneficiario()
    {
        this.adicaoBeneficiario.setNaoPermitirInclusao(false);

        boolean isErro = false;

        if (getAdicaoBeneficiario().getNomeCompleto() == null
                || getAdicaoBeneficiario().getNomeCompleto().trim().isEmpty())
        {
            this.erroTela.setNome(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setNome(false);
        }

        if (getAdicaoBeneficiario().getDataNascimento() == null
                || retiraMascara(getAdicaoBeneficiario().getDataNascimento()).trim().isEmpty()
                || retiraMascara(getAdicaoBeneficiario().getDataNascimento()).length() < 8
                || !isDataValida(retiraMascara(getAdicaoBeneficiario().getDataNascimento())))
        {
            this.erroTela.setDtNascimento(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setDtNascimento(false);
        }

        if (getAdicaoBeneficiario().getSexo() == null || getAdicaoBeneficiario().getSexo().trim().isEmpty())
        {
            this.erroTela.setSexo(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setSexo(false);
        }

        if (0 == getAdicaoBeneficiario().getGrauParentesco() )
        {
            this.erroTela.setGrauParentesco(true);
            this.erroTela.setGrauParentescoDesc("Favor informar o grau de parentesco");
            isErro = true;
        } 
        else
        {
            /**
             * verificar se filho tem 12 anos de diferenca do pai.
             */
            this.erroTela.setGrauParentesco(false);

            if (getAdicaoBeneficiario().getGrauParentesco() == ParentescoBeneficiario.FILHO.getValor())
            {
                try
                {
                    int anos = 0;
                    Calendar calendar = new GregorianCalendar();
                    final Date dataPai = new SimpleDateFormat("dd/MM/yyyy").parse(this.dataNascimento);
                    final Date dataFilho = new SimpleDateFormat("dd/MM/yyyy").parse(getAdicaoBeneficiario().getDataNascimento());

                    calendar.setTime(dataFilho);
                    anos = calendar.get(Calendar.YEAR);
                    calendar.setTime(dataPai);
                    anos -= calendar.get(Calendar.YEAR);

                    if (anos >= 0 && anos <= 12 )
                    {
                        this.erroTela.setGrauParentesco(true);
                        isErro = true;
                        this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Filho deve ter 12 anos de diferença de Pai/Mãe."));
                    }
                    
                    if ( anos < 0 )
                    {
                        this.erroTela.setGrauParentesco(true);
                        isErro = true;
                        this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Pai/Mãe deve ter 12 anos de diferença de seu filho."));
                    }
                } 
                catch ( ParseException e)
                {
                    isErro = true;
                    this.erroTela.setGrauParentescoDesc( "Erro ao converter." );
                }
            }

            /**
             * verificar se ja existe esposa ou companheiro(a)
             */
            if (getAdicaoBeneficiario().getGrauParentesco() == ParentescoBeneficiario.CONJUGE.getValor()
                    || getAdicaoBeneficiario().getGrauParentesco() == ParentescoBeneficiario.COMPANHEIRO.getValor())
            {
                if (this.dadosTelaEtapa1.getEstadoCivil() != EstadoCivil.CASADO.getValor())
                {
                    this.erroTela.setGrauParentesco(true);
                    this.erroTela.setGrauParentescoDesc(converteEncodingTexto("A opção de CASADO não foi selecionada."));
                    isErro = true;
                } 
                else
                {
                    for (Beneficiario beneficiario : this.listaBeneficiarios)
                    {
                        if (beneficiario.getGrauParentesco() == ParentescoBeneficiario.CONJUGE.getValor()
                                || beneficiario.getGrauParentesco() == ParentescoBeneficiario.COMPANHEIRO.getValor())
                        {
                            this.erroTela.setGrauParentesco(true);
                            this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Cônjuge/Companheiro(a) já cadastrado."));
                            isErro = true;
                            break;
                        }
                    }
                }
            }
        }

        if ( 0 == getAdicaoBeneficiario().getPorcentagem() )
        {
            this.erroTela.setPorcentagem(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setPorcentagem(false);
        }

        int somaPorcentagem = 0;

        for (Beneficiario beneficiario : this.listaBeneficiarios)
        {
            somaPorcentagem = somaPorcentagem + beneficiario.getPorcentagem();
        }

        somaPorcentagem = somaPorcentagem + getAdicaoBeneficiario().getPorcentagem();

        if (this.listaBeneficiarios.size() == 3 && somaPorcentagem < 100)
        {
            this.erroTela.setPorcentagemMenorQueCem(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setPorcentagemMenorQueCem(false);
        }

        if (this.listaBeneficiarios.size() < 4 && somaPorcentagem > 100)
        {
            this.erroTela.setPorcentagemMaiorQueCem(true);
            isErro = true;
        } 
        else
        {
            this.erroTela.setPorcentagemMaiorQueCem(false);
        }

        if (!isErro)
        {
            this.listaBeneficiarios.add(getAdicaoBeneficiario());
            setSomaPorcentagem(somaPorcentagem);
            this.erroTela.setBeneficiarios(false);
            this.erroTela.setPorcentagemTotalMenorQueCem(false);
            setAdicaoBeneficiario(new Beneficiario());

            if (this.listaBeneficiarios.size() == 4 || somaPorcentagem == 100)
            {
                this.adicaoBeneficiario.setNaoPermitirInclusao(true);
            } 
            else
            {
                this.adicaoBeneficiario.setNaoPermitirInclusao(false);
            }
        }
    }

    public void doExcluirBeneficiario(Beneficiario obj)
    {
        this.listaBeneficiarios.remove(obj);
        int calcPorcentagem = 0;

        for (Beneficiario beneficiario : this.listaBeneficiarios)
        {
            calcPorcentagem = calcPorcentagem + beneficiario.getPorcentagem();
        }

        setSomaPorcentagem(calcPorcentagem);
        this.adicaoBeneficiario.setNaoPermitirInclusao(false);
    }

    public void doAlterarBeneficiario(Beneficiario obj)
    {
        setAdicaoBeneficiario(obj);
        this.doExcluirBeneficiario(obj);
    }

    public void doDistribuirBeneficiarios()
    {
        if (this.listaBeneficiarios.size() == 2 && this.somaPorcentagem.equals(100))
        {
            this.listaBeneficiarios.get(0).setPorcentagem(50);
            this.listaBeneficiarios.get(1).setPorcentagem(50);
        }
        if (this.listaBeneficiarios.size() == 4 && this.somaPorcentagem.equals(100))
        {
            this.listaBeneficiarios.get(0).setPorcentagem(25);
            this.listaBeneficiarios.get(1).setPorcentagem(25);
            this.listaBeneficiarios.get(2).setPorcentagem(25);
            this.listaBeneficiarios.get(3).setPorcentagem(25);
        }
        if (this.listaBeneficiarios.size() == 3 && this.somaPorcentagem.equals(100))
        {
            this.listaBeneficiarios.get(0).setPorcentagem(33);
            this.listaBeneficiarios.get(1).setPorcentagem(33);
            this.listaBeneficiarios.get(2).setPorcentagem(34);
        }
    }

    public String getPerfilPlano(Integer cod)
    {
        return converteEncodingTexto(PerfilCliente.getNomeFormatadoByCodigo(cod));
    }

    public String getNomeEstadoCivil(Integer cod)
    {
        return converteEncodingTexto(EstadoCivil.getNomeFormatadoByCod(cod));
    }

    public String getNomeProfissao(Integer cod)
    {

        return converteEncodingTexto(Profissao.getNomeByCod(cod));
    }

    public String getSimOuNao(String x)
    {
        if (x.trim().equalsIgnoreCase("S"))
        {
            return "Sim";
        } 
        else
        {
            return converteEncodingTexto("Não");
        }
    }

    public String getNomeFormaPagamento(String x)
    {
        if (x.equals("1") || x.equals("2") )
        {
            return converteEncodingTexto("Débito em conta Bradesco");
        } 
        else if(x.equals("3"))
        {
            return "Boleto";
        }    
        else if(x.equals("4"))
        {
            return "PIX";
        }   
        else if(x.equals("5"))
        {
            return "PIX + Boleto";
        }   
        return "";
        
    }

    public String getNomeTipoConta(String x)
    {
        if (x.equals("1"))
        {
            return "Corrente";
        } 
        else
        {
            return converteEncodingTexto("Poupança");
        }
    }

    public String getGeneroCompleto(String abreviado)
    {
        if (abreviado.equals("M"))
        {
            return "Masculino";
        } 
        else
        {
            return "Feminino";
        }
    }

    public String getUnicoOuMensal(Integer cod)
    {
        if (cod.equals(1))
        {
            return "Mensal";
        } 
        else
        {
            return converteEncodingTexto("Única");
        }
    }

    public String getNomeGrauParentescoBeneficiario(Integer cod)
    {
        return converteEncodingTexto(ParentescoBeneficiario.getNomeByCod(cod));
    }

    public String getTributacao(Integer cod)
    {
        if ( 0 == cod )
        {
            return "";
        }
        
        return converteEncodingTexto(RegimeTributario.getNome(cod));
    }

    public String getNomeModalidade(Integer cod)
    {

        return (ModalidadePrevidencia.getNome(cod));
    }

    public String filtroPerfilPlanos()
    {
        setListaPlanosExibicao(this.ofertaPlanoRetorno.getListaPlanos());

        if (listaPerfil.length > 0)
        {
            listaPlanosExibicao = new ArrayList<ListaPlanosVo>();
            this.erroTela.setModal(true);
            for (Integer lista : listaPerfil)
            {
                for (ListaPlanosVo listaPlanos : this.ofertaPlanoRetorno.getListaPlanos())
                {
                    if (lista.equals(listaPlanos.getPerfil()))
                    {
                        listaPlanosExibicao.add(listaPlanos);
                        this.erroTela.setModal(false);
                    }
                }
            }
        }

        return "oferta.xhtml";

    }

    public String doSelecionarPropostaDetalhes(String pagina)
    {
        try
        {
            setPaginaRecebidaDetalhes(new String());
            setPaginaRecebidaDetalhes(pagina);
            setDetalhaFundoRetorno(new DetalhaFundoRetornoVo());
            setExibeModalDetalheOferta(true);

            /**
             * se o contratante for maior que 23 anos, nao e jovem
             */
            this.setMostrarPlanoJovem(false);

            if ("S".equals(this.selecionado.getIndJovem()))
            {
                Calendar calendar = new GregorianCalendar();
                final Date dataPai = new SimpleDateFormat("dd/MM/yyyy").parse(this.dataNascimento);
                @SuppressWarnings("static-access")
                int anos = calendar.getInstance().get(calendar.YEAR);

                calendar.setTime(dataPai);
                anos -= calendar.get(Calendar.YEAR);

                if (anos <= 23)
                {
                    this.setPlanoJovem(true);
                }
            }

            if (mockApi)
            {
                RetornoMockApi retornoMockApi = new RetornoMockApi();
                final String retorno = retornoMockApi.retornarDetalhaFundo();
                setDetalhaFundoRetorno( new Gson().fromJson( retorno, DetalhaFundoRetornoVo.class ) );
            } 
            else
            {
                this.detalhaFundoRetorno = service.detalhaFundo(selecionado.getCodPlano().toString(), selecionado.getCodBeneficio().toString());
                
                if ( null == this.detalhaFundoRetorno )
                {
                    return null;
                }
            }

            calculaRentabilidadeFundo();
        } 
        catch (ParseException e)
        {
            this.erro = "ContrataPrev|doSelecionarPropostaDetalhes|: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
            return null;
        }

        return pagina;
    }

    public String doVoltarParaOferta()
    {
        setErroTela(new ErroTela());
        setSelecionado(new ListaPlanosVo());
        return "oferta.xhtml";
    }

    public String voltarParaOfertaRisco()
    {
    	setExibeModalOfertaRisco(false);
        setErroTela(new ErroTela());
        setSelecionado(new ListaPlanosVo());
        return "ofertaRisco.xhtml";
    }

    public String doVoltarParaEtapa1()
    {
        setErroTela(new ErroTela());

        return "propostaEtapa1.xhtml";
    }

    public String doVoltarParaEtapa2()
    {
        setErroTela(new ErroTela());
        
        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            return "propostaEtapa2.xhtml";
        } 
        else
        {
            return "propostaEtapa1.xhtml";
        }
    }

    public String voltarRiscoEtapa1()
    {
        return "riscoEtapa1.xhtml";
    }

    public String doVoltarParaEtapa3()
    {
        setErroTela(new ErroTela());

        return "propostaEtapa3.xhtml";
    }

    public String voltarRiscoEtapa2()
    {
        return "riscoEtapa2.xhtml";
    }

    public String voltarRiscoEtapa3()
    {
        return "riscoEtapa3.xhtml";
    }

    public String doVoltarParaEtapa4()
    {
        setErroTela(new ErroTela());

        return "propostaEtapa4.xhtml";
    }

    public String voltarRiscoEtapa4()
    {
        this.cssBotaoDesabilitado = false;
        return "riscoEtapa4.xhtml";
    }

    public String voltarRiscoEtapa5()
    {
        return "riscoEtapa5.xhtml";
    }

    public String doVoltarParaEtapa5()
    {
        setErroTela(new ErroTela());
        listaBeneficiarios = new ArrayList<Beneficiario>();
        adicaoBeneficiario = new Beneficiario();

        return "propostaEtapa5.xhtml";
    }

    public String doVoltarParaEtapa6()
    {
        setErroTela(new ErroTela());
        this.cssBotaoDesabilitado = false;
        this.adicaoBeneficiario.setNaoPermitirInclusao(false);

        return "propostaEtapa6.xhtml";
    }

    public String doVoltarParaInicio()
    {
        this.cpf = "";
        this.dataNascimento = "";
        this.sexo = "";
        this.codCorretorTela = "";
        this.dddCel = "";
        this.nrCel = "";
        this.idadeAposentadoria = 0;
        this.gerenteComercial = "";
        this.somaPorcentagem = 0;
        this.valorContribuicao = "";
        this.mensagemValidacaoConta = "";

        setMsgGenerica(false);
        setMsgErro(new String());
        setExibeModalConfirmacaoPlano(false);
        setExibeModalDetalheOferta(false);
        setFlagExplicacaoPlanos(false);
        setExibePep(false);
        setExibeMercado(false);
        setExibeRede(false);
        setExibeSucursalDigitada(false);
        setExibeRelacionamentoAgPublico(false);
        setExibeNacionalidadeAlemBrasileira(false);
        setExibeResidenciaForaBrasil(false);
        setExibeVistoOutroPais(false);
        setExibirDadosContaBancaria(false);
        setPep(new String());
        setRelacionamentoAgPublico(new String());
        setNacionalidadeAlemBrasileira(new String());
        setResidenciaForaBrasil(new String());
        setVistoOutroPais(new String());
        setEditaSexo(false);
        setEditaDtNascimento(false);
        setEditaCpf(false);
        setEditaPep(false);
        setEditaAgentePep(false);
        setPlanoJovem(false);
        setExibePerguntaPlanoJovem(false);
        setOfertarPlanosJovem(new String());

        setErroTela(new ErroTela());
        this.service = new ContrataServices();
        setListaPlanosExibicao(new ArrayList<ListaPlanosVo>());
        setListaPerfil(new Integer[4]);
        setListaPerguntasFatca1(new Integer[5]);
        setListaPerguntasFatca2(new Integer[5]);
        setListaPerguntasFatca3(new Integer[5]);
        setRentabilidade3(new String());
        setRentabilidade6(new String());
        setRentabilidade12(new String());
        setPaginaRecebidaDetalhes(new String());
        setRespondeuPergunta1(0);
        setRespondeuPergunta2(0);
        setRespondeuPergunta3(0);
        setCodPais1(0);
        setCodPais2(0);
        setCodPais3(0);
        setDocPais1(new String());
        setDocPais2(new String());
        ;
        setDocPais3(new String());
        this.downloadPDF = "";
        setPrazoRecebimento(new String());

        setDetalhaParticipante(new DetalhaParticipanteRetornoVo());
        setOfertaPlanoEnvio(new OfertaPlanoEnvioVo());
        setOfertaPlanoRetorno(new OfertaPlanoRetornoVo());
        setSelecionado(new ListaPlanosVo());
        setIncluirRascunhoEnvio(new IncluirRascunhoEnvioVo());
        setValidaPropostaInscricaoEnvio(new ValidaPropostaInscricaoEnvioVo());
        setIncluirRascunhoRetorno(new IncluirRascunhoRetornoVo());
        setValidaPropostaInscricaoRetorno(new ValidaPropostaInscricaoRetornoVo());
        setDetalhaFundoRetorno(new DetalhaFundoRetornoVo());
        setDadosTelaEtapa1(new DadosTelaEtapa1Vo());
        setObjetoPep(new Pep());
        setAdicaoBeneficiario(new Beneficiario());
        setListaBeneficiarios(new ArrayList<Beneficiario>());
        setPatrocinador(new Patrocinador());
        this.cssBotaoDesabilitado = false;
        setGeraPropostaEnvio(new GeraPropostaEnvioVo());
        setCodSucursal(new String());
        setCodSucursalDigitado(new String());
        setDescricaoSucursal(new String());
        setCodCorretorTela(new String());
        setExibicaoPgVgbl(false);
        setExibicaoRisco(false);
        setFlagExplicaoRisco(false);
        this.modalidade = 0;
        this.modalidadeRisco = 0;
        this.dtNascimentoBeneficiarioRisco = new String();
        this.sexoBeneficiarioRisco = new String();
        this.flagExplicacaoPlanos = false;

        return "index.xhtml";
    }

    public String voltarParaInicioRisco()
    {
        setCpf(new String());
        setDataNascimento(new String());
        setSexo(new String());
        setCodCorretor("");
        setDddCel(new String());
        setNrCel(new String());
        setIdadeAposentadoria(0);
        setGerenteComercial(new String());
        setSomaPorcentagem(0);
        setValorContribuicao(new String());
        setMensagemValidacaoConta("");

        setMsgGenerica(false);
        setMsgErro(new String());
        setExibeModalConfirmacaoPlano(false);
        setExibeModalDetalheOferta(false);
        setFlagExplicacaoPlanos(false);
        setExibePep(false);
        setExibeMercado(false);
        setExibeRede(false);
        setExibeRelacionamentoAgPublico(false);
        setExibeNacionalidadeAlemBrasileira(false);
        setExibeResidenciaForaBrasil(false);
        setExibeVistoOutroPais(false);
        setExibirDadosContaBancaria(false);
        setPep(new String());
        setRelacionamentoAgPublico(new String());
        setNacionalidadeAlemBrasileira(new String());
        setResidenciaForaBrasil(new String());
        setVistoOutroPais(new String());
        setEditaSexo(false);
        setEditaDtNascimento(false);
        setEditaCpf(false);
        setEditaPep(false);
        setEditaAgentePep(false);
        setPlanoJovem(false);
        setExibePerguntaPlanoJovem(false);
        setOfertarPlanosJovem(new String());

        setErroTela(new ErroTela());
        this.service = new ContrataServices();
        setListaPlanosExibicao(new ArrayList<ListaPlanosVo>());
        setListaPerfil(new Integer[4]);
        setListaPerguntasFatca1(new Integer[5]);
        setListaPerguntasFatca2(new Integer[5]);
        setListaPerguntasFatca3(new Integer[5]);
        setRentabilidade3(new String());
        setRentabilidade6(new String());
        setRentabilidade12(new String());
        setPaginaRecebidaDetalhes(new String());
        setRespondeuPergunta1(0);
        setRespondeuPergunta2(0);
        setRespondeuPergunta3(0);
        setCodPais1(0);
        setCodPais2(0);
        setCodPais3(0);
        setDocPais1(new String());
        setDocPais2(new String());
        setDocPais3(new String());
        this.downloadPDF = "";
        setPrazoRecebimento(new String());

        setDetalhaParticipante(new DetalhaParticipanteRetornoVo());
        setOfertaPlanoEnvio(new OfertaPlanoEnvioVo());
        setOfertaPlanoRetorno(new OfertaPlanoRetornoVo());
        setSelecionado(new ListaPlanosVo());
        setIncluirRascunhoEnvio(new IncluirRascunhoEnvioVo());
        setValidaPropostaInscricaoEnvio(new ValidaPropostaInscricaoEnvioVo());
        setIncluirRascunhoRetorno(new IncluirRascunhoRetornoVo());
        setValidaPropostaInscricaoRetorno(new ValidaPropostaInscricaoRetornoVo());
        setDetalhaFundoRetorno(new DetalhaFundoRetornoVo());
        setDadosTelaEtapa1(new DadosTelaEtapa1Vo());
        setObjetoPep(new Pep());
        setAdicaoBeneficiario(new Beneficiario());
        setListaBeneficiarios(new ArrayList<Beneficiario>());
        setPatrocinador(new Patrocinador());
        this.cssBotaoDesabilitado = false;
        setGeraPropostaEnvio(new GeraPropostaEnvioVo());
        setCodSucursal(new String());
        setCodSucursalDigitado(new String());
        setDescricaoSucursal(new String());
        setCodCorretorTela(new String());
        setExibicaoPgVgbl(false);
        setExibicaoRisco(false);
        setFlagExplicaoRisco(false);
        this.modalidade = 0;
        this.modalidadeRisco = 0;
        this.dtNascimentoBeneficiarioRisco = new String();
        this.sexoBeneficiarioRisco = new String();
        this.flagExplicacaoPlanos = false;
        this.telaRiscoEtapa1 = new TelaRiscoEtapa1();
        this.telaRiscoEtapa2 = new TelaRiscoEtapa2();
        this.telaRiscoEtapa3 = new TelaRiscoEtapa3();
        this.telaRiscoEtapa4 = new TelaRiscoEtapa4();
        this.beneficiarioRisco = new Beneficiario();
        return "../index.xhtml";
    }

    public String doVoltarParaPlano()
    {
        setFlagExplicacaoPlanos(false);
        setValorContribuicao(new String());
        this.erroTela.setModal(false);

        return "plano.xhtml";
    }

    public String doAvancarEtapa2()
    {
        if (null == getDadosTelaEtapa1().getNome() || getDadosTelaEtapa1().getNome().trim().isEmpty())
        {
            this.erroTela.setNome(true);
        } 
        else
        {
            this.erroTela.setNome(false);
        }

        if (null == getDadosTelaEtapa1().getSexo() || getDadosTelaEtapa1().getSexo().trim().isEmpty())
        {
            this.erroTela.setSexo(true);
        } 
        else
        {
            this.erroTela.setSexo(false);
        }

        if (null == getDadosTelaEtapa1().getEstadoCivil())
        {
            this.erroTela.setEstadoCivil(true);
        } 
        else
        {
            this.erroTela.setEstadoCivil(false);
        }

        if (!isPlanoJovem())
        {
            if (null == getDadosTelaEtapa1().getRenda()
                    || retiraMascara(getDadosTelaEtapa1().getRenda()).trim().isEmpty()
                    || getDadosTelaEtapa1().getRenda().trim().equalsIgnoreCase("0,00"))
            {
                this.erroTela.setRendaMensal(true);
            } 
            else
            {
                this.erroTela.setRendaMensal(false);
            }

            if (null == getDadosTelaEtapa1().getProfissao())
            {
                this.erroTela.setProfissao(true);
            } 
            else
            {
                this.erroTela.setProfissao(false);
            }

            if (null == getDadosTelaEtapa1().getPaisNascimento())
            {
                this.erroTela.setPaisNascimento(true);
            } 
            else
            {
                this.erroTela.setPaisNascimento(false);
            }

        } 
        else
        {
            this.erroTela.setRendaMensal(false);
            this.erroTela.setProfissao(false);
            this.erroTela.setPaisNascimento(false);
        }

        if (null == getDadosTelaEtapa1().getTel() || retiraMascara(getDadosTelaEtapa1().getTel()).trim().isEmpty()
                || retiraMascara(getDadosTelaEtapa1().getTel()).length() < 11
                || !telValido(retiraMascara(getDadosTelaEtapa1().getTel())))
        {
            this.erroTela.setTel(true);
        } 
        else
        {
            this.erroTela.setTel(false);
        }
        
        if (getDadosTelaEtapa1().getTelResidencial() == null || getDadosTelaEtapa1().getTelResidencial().isEmpty()) 
        {
        	getDadosTelaEtapa1().setTelResidencial("");
        	this.erroTela.setTelResidencial(false);
        }
        else  if (!validaTelefoneFixo(getDadosTelaEtapa1().getTelResidencial()))
        {
        	  this.erroTela.setTelResidencial(true);
        }else {
        	this.erroTela.setTelResidencial(false);
        }

        if (getDadosTelaEtapa1().getTelComercial()== null || getDadosTelaEtapa1().getTelComercial().isEmpty()) 
        {
        	getDadosTelaEtapa1().setTelComercial("");
        	this.erroTela.setTelComercial(false);
        }
        else  if (!validaTelefoneFixo(getDadosTelaEtapa1().getTelComercial()))
        {
        	  this.erroTela.setTelComercial(true);
        	  
        }else {
        	this.erroTela.setTelComercial(false);
        }         
       
        if (null == getDadosTelaEtapa1().getCpf() || retiraMascara(getDadosTelaEtapa1().getCpf()).trim().isEmpty()
                || retiraMascara(getDadosTelaEtapa1().getCpf()).length() < 11
                || !isCpfValido(retiraMascara(getDadosTelaEtapa1().getCpf())))
        {
            this.erroTela.setCpf(true);
        } 
        else
        {
            this.erroTela.setCpf(false);
        }

        if (null == getDadosTelaEtapa1().getDtNascimento()
                || retiraMascara(getDadosTelaEtapa1().getDtNascimento()).trim().isEmpty()
                || retiraMascara(getDadosTelaEtapa1().getDtNascimento()).length() < 8
                || !isDataValida(retiraMascara(getDadosTelaEtapa1().getDtNascimento())))
        {
            this.erroTela.setDtNascimento(true);
        } 
        else
        {
            this.erroTela.setDtNascimento(false);
        }

        if (null == getDadosTelaEtapa1().getEmail() || getDadosTelaEtapa1().getEmail().trim().isEmpty()
                || !isEmailValido(getDadosTelaEtapa1().getEmail()))
        {
            this.erroTela.setEmail(true);
        } 
        else
        {
            this.erroTela.setEmail(false);
        }

        if (null == getDadosTelaEtapa1().getCep() || retiraMascara(getDadosTelaEtapa1().getCep()).trim().isEmpty()
                || retiraMascara(getDadosTelaEtapa1().getCep()).length() < 8)
        {
            this.erroTela.setCep(true);
        } 
        else
        {
            this.erroTela.setCep(false);
        }

        if (null == getDadosTelaEtapa1().getLogradouro() || getDadosTelaEtapa1().getLogradouro().isEmpty())
        {
            this.erroTela.setLogradouro(true);
        } 
        else
        {
            this.erroTela.setLogradouro(false);
        }

        if (null == getDadosTelaEtapa1().getNumEndereco() || getDadosTelaEtapa1().getNumEndereco().isEmpty()
                || Integer.parseInt(getDadosTelaEtapa1().getNumEndereco()) == 0)
        {
            this.erroTela.setNumeroEndereco(true);
        } 
        else
        {
            this.erroTela.setNumeroEndereco(false);
        }

        if (null == getDadosTelaEtapa1().getBairro() || getDadosTelaEtapa1().getBairro().isEmpty())
        {
            this.erroTela.setBairro(true);
        } 
        else
        {
            this.erroTela.setBairro(false);
        }

        if (null == getDadosTelaEtapa1().getCidade() || getDadosTelaEtapa1().getCidade().isEmpty())
        {
            this.erroTela.setCidade(true);
        } 
        else
        {
            this.erroTela.setCidade(false);
        }

        if (null == getDadosTelaEtapa1().getEstado() || getDadosTelaEtapa1().getEstado().isEmpty())
        {
            this.erroTela.setEstado(true);
        } 
        else
        {
            this.erroTela.setEstado(false);
        }

        if (this.erroTela.isNome() || this.erroTela.isSexo() || this.erroTela.isEstadoCivil()
                || this.erroTela.isRendaMensal() || this.erroTela.isProfissao() || this.erroTela.isTel() || this.erroTela.isTelResidencial() 
                || this.erroTela.isTelComercial() || this.erroTela.isEmail() || this.erroTela.isCep() || this.erroTela.isLogradouro()
                || this.erroTela.isNumeroEndereco() || this.erroTela.isBairro() || this.erroTela.isCidade()
                || this.erroTela.isEstado() || this.erroTela.isCpf() || this.erroTela.isDtNascimento()
                || this.erroTela.isPaisNascimento())
        {
            return "propostaEtapa1.xhtml";
        }

        setEditaAgentePep(false);
        setEditaPep(false);
        setObjetoPep(new Pep());

        if (!this.editaCpf)
        {
            if ("s".equalsIgnoreCase(this.detalhaParticipante.getPessoaExposta()))
            {
                setPep("S");
                setExibePep(true);
                setEditaPep(true);
            }
            if ("s".equalsIgnoreCase(this.detalhaParticipante.getRelacionamento()))
            {
                setRelacionamentoAgPublico("S");
                setExibeRelacionamentoAgPublico(true);
                setEditaAgentePep(true);
            }
        }

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            return "propostaEtapa2.xhtml";
        } 
        else
        {
            return "propostaEtapa3.xhtml";
        }
    }

    private boolean validaTelefoneFixo(String telFixo) {
    	
    	telFixo = retiraMascara(telFixo);
    	
        if (telFixo.length() != 10 || telFixo.substring(2, 3).equals("0")){
        	return false;
        }
        
        if (!telFixo.matches("[0-9]+")) {
        	return false;
        }
        
        try {
        	int dddNumerico = Integer.valueOf(telFixo.substring(0, 2));
        	if (dddNumerico < 11) {
        		return false;
        	}
        }catch(NumberFormatException e) {
        	return false;
        }		
        		
        return true;
    	
	}

	public String avancarRiscoEtapa2()
    {
        this.resetErroRiscoEtapa1();
        boolean isErro = false;

        if (null == this.telaRiscoEtapa1.getNome() || this.telaRiscoEtapa1.getNome().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setNome(true);
        }

        if (null == this.telaRiscoEtapa1.getSexo() || this.telaRiscoEtapa1.getSexo().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setSexo(true);
        }

        if (null == this.telaRiscoEtapa1.getEstadoCivil())
        {
            isErro = true;
            this.erroTela.setEstadoCivil(true);
        }

        if (!this.isPlanoJovem())
        {
            if (null == this.telaRiscoEtapa1.getRenda()
                    || retiraMascara(this.telaRiscoEtapa1.getRenda()).trim().isEmpty()
                    || this.telaRiscoEtapa1.getRenda().trim().equalsIgnoreCase("0,00"))
            {
                isErro = true;
                this.erroTela.setRendaMensal(true);
            }

            if (null == this.telaRiscoEtapa1.getProfissao())
            {
                isErro = true;
                this.erroTela.setProfissao(true);
            }

            if (null == this.telaRiscoEtapa1.getPaisNascimento())
            {
                isErro = true;
                this.erroTela.setPaisNascimento(true);
            }
        }

        if (null == this.telaRiscoEtapa1.getTel() || retiraMascara(this.telaRiscoEtapa1.getTel()).trim().isEmpty()
                || retiraMascara(this.telaRiscoEtapa1.getTel()).length() < 11
                || !telValido(retiraMascara(this.telaRiscoEtapa1.getTel())))
        {
            isErro = true;
            this.erroTela.setTel(true);
        }
        
        if (this.telaRiscoEtapa1.getTelResidencial() == null || this.telaRiscoEtapa1.getTelResidencial().isEmpty()) 
        {
        	this.telaRiscoEtapa1.setTelResidencial("");
        	this.erroTela.setTelResidencial(false);
        }
        else  if (!validaTelefoneFixo(this.telaRiscoEtapa1.getTelResidencial()))
        {
        	  this.erroTela.setTelResidencial(true);
          	  isErro = true;
        }

        if (this.telaRiscoEtapa1.getTelComercial()== null || this.telaRiscoEtapa1.getTelComercial().isEmpty()) 
        {
        	this.telaRiscoEtapa1.setTelComercial("");
        	this.erroTela.setTelComercial(false);
        }
        else  if (!validaTelefoneFixo(this.telaRiscoEtapa1.getTelComercial()))
        {
        	  this.erroTela.setTelComercial(true);
          	  isErro = true;
        }            
       

        if (null == this.telaRiscoEtapa1.getCpf() || retiraMascara(this.telaRiscoEtapa1.getCpf()).trim().isEmpty()
                || retiraMascara(this.telaRiscoEtapa1.getCpf()).length() < 11
                || !isCpfValido(retiraMascara(this.telaRiscoEtapa1.getCpf())))
        {
            isErro = true;
            this.erroTela.setCpf(true);
        }

        if (null == this.telaRiscoEtapa1.getDtNascimento()
                || retiraMascara(this.telaRiscoEtapa1.getDtNascimento()).trim().isEmpty()
                || retiraMascara(this.telaRiscoEtapa1.getDtNascimento()).length() < 8
                || !isDataValida(retiraMascara(this.telaRiscoEtapa1.getDtNascimento())))
        {
            isErro = true;
            this.erroTela.setDtNascimento(true);
        }

        if (null == this.telaRiscoEtapa1.getEmail() || this.telaRiscoEtapa1.getEmail().trim().isEmpty()
                || !isEmailValido(this.telaRiscoEtapa1.getEmail()))
        {
            isErro = true;
            this.erroTela.setEmail(true);
        }

        if (null == this.telaRiscoEtapa1.getCep() || retiraMascara(this.telaRiscoEtapa1.getCep()).trim().isEmpty()
                || retiraMascara(this.telaRiscoEtapa1.getCep()).length() < 8)
        {
            isErro = true;
            this.erroTela.setCep(true);
        }

        if (null == this.telaRiscoEtapa1.getLogradouro() || this.telaRiscoEtapa1.getLogradouro().isEmpty())
        {
            isErro = true;
            this.erroTela.setLogradouro(true);
        }

        if (null == this.telaRiscoEtapa1.getNumEndereco() || this.telaRiscoEtapa1.getNumEndereco().isEmpty()
                || Integer.parseInt(this.telaRiscoEtapa1.getNumEndereco()) == 0)
        {
            isErro = true;
            this.erroTela.setNumeroEndereco(true);
        }

        if (null == this.telaRiscoEtapa1.getBairro() || this.telaRiscoEtapa1.getBairro().isEmpty())
        {
            isErro = true;
            this.erroTela.setBairro(true);
        }

        if (null == this.telaRiscoEtapa1.getCidade() || this.telaRiscoEtapa1.getCidade().isEmpty())
        {
            isErro = true;
            this.erroTela.setCidade(true);
        }

        if (null == this.telaRiscoEtapa1.getEstado() || this.telaRiscoEtapa1.getEstado().isEmpty())
        {
            isErro = true;
            this.erroTela.setEstado(true);
        }

        if (isErro)
        {
            return "riscoEtapa1.xhtml";
        }

        this.telaRiscoEtapa2.setEditaAgentePep(false);
        this.telaRiscoEtapa2.setEditaPep(false);
        setObjetoPep(new Pep());

        if (!this.editaCpf)
        {
            if ("s".equalsIgnoreCase(this.detalhaParticipante.getPessoaExposta()))
            {
                this.telaRiscoEtapa2.setPep("S");
                this.telaRiscoEtapa2.setExibePep(true);
                this.telaRiscoEtapa2.setEditaPep(true);
            }

            if ("s".equalsIgnoreCase(this.detalhaParticipante.getRelacionamento()))
            {
                this.telaRiscoEtapa2.setRelacionamentoAgPublico("S");
                this.telaRiscoEtapa2.setExibeRelacionamentoAgPublico(true);
                this.telaRiscoEtapa2.setEditaPep(true);
            }
        }

        this.resetErroRiscoEtapa2();
        return "riscoEtapa2.xhtml";
    }

    public String doAvancarParaEtapa3()
    {
        boolean isErro = false;
        this.erroTela.setNomePatrocinador(false);
        this.erroTela.setSexoPatrocinador(false);
        this.erroTela.setEstadoCivilPatrocinador(false);
        this.erroTela.setRendaMensalPatrocinador(false);
        this.erroTela.setProfissaoPatrocinador(false);
        this.erroTela.setTelPatrocinador(false);
        this.erroTela.setTelResidencialPatrocinador(false);
        this.erroTela.setTelComercialPatrocinador(false);
        this.erroTela.setEmailPatrocinador(false);
        this.erroTela.setCepPatrocinador(false);
        this.erroTela.setLogradouroPatrocinador(false);
        this.erroTela.setNumeroEnderecoPatrocinador(false);
        this.erroTela.setBairroPatrocinador(false);
        this.erroTela.setCidadePatrocinador(false);
        this.erroTela.setEstadoPatrocinador(false);
        this.erroTela.setCpfPatrocinador(false);
        this.erroTela.setDtNascimentoPatrocinador(false);
        this.erroTela.setDependenteIr(false);
        this.erroTela.setGrauParentesco(false);
        this.erroTela.setPaisNascimentoPatrocinador(false);
        this.erroTela.setCpfPatrocinadorIgualParticipante(false);
        
        if (null == getPatrocinador().getNome() || getPatrocinador().getNome().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setNomePatrocinador(true);
        } 

        if (null == getPatrocinador().getSexo() || getPatrocinador().getSexo().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setSexoPatrocinador(true);
        } 

        if (null == getPatrocinador().getEstadoCivil())
        {
            isErro = true;
            this.erroTela.setEstadoCivilPatrocinador(true);
        } 

        if (null == getPatrocinador().getRendaMensal() 
                || retiraMascara(getPatrocinador().getRendaMensal()).trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setRendaMensalPatrocinador(true);
        } 

        if (null == getPatrocinador().getProfissao())
        {
            isErro = true;
            this.erroTela.setProfissaoPatrocinador(true);
        } 

        if (null == getPatrocinador().getTel() || retiraMascara(getPatrocinador().getTel()).trim().isEmpty()
                || retiraMascara(getPatrocinador().getTel()).length() < 11
                || !telValido(retiraMascara(getPatrocinador().getTel())))
        {
            isErro = true;
            this.erroTela.setTelPatrocinador(true);
        } 
        
        
        if (getPatrocinador().getTelResidencial() == null || getPatrocinador().getTelResidencial().isEmpty()) 
        {
        	getPatrocinador().setTelResidencial("");
        	this.erroTela.setTelResidencialPatrocinador(false);
        }
        else  if (!validaTelefoneFixo(getPatrocinador().getTelResidencial()))
        {
        	  this.erroTela.setTelResidencialPatrocinador(true);
        }else {
        	this.erroTela.setTelResidencialPatrocinador(false);
        }

        if (getPatrocinador().getTelComercial()== null || getPatrocinador().getTelComercial().isEmpty()) 
        {
        	getPatrocinador().setTelComercial("");
        	this.erroTela.setTelComercialPatrocinador(false);
        }
        else  if (!validaTelefoneFixo(getPatrocinador().getTelComercial()))
        {
        	  this.erroTela.setTelComercialPatrocinador(true);
        	  
        }else {
        	this.erroTela.setTelComercialPatrocinador(false);
        }         
       
        if (null == getPatrocinador().getEmail() || getPatrocinador().getEmail().isEmpty()
                || !isEmailValido(getPatrocinador().getEmail()))
        {
            isErro = true;
            this.erroTela.setEmailPatrocinador(true);
        } 

        if (null == getPatrocinador().getCep() || retiraMascara(getPatrocinador().getCep()).trim().isEmpty()
                || retiraMascara(getPatrocinador().getCep()).length() < 8)
        {
            isErro = true;
            this.erroTela.setCepPatrocinador(true);
        } 

        if (null == getPatrocinador().getLogradouro() || getPatrocinador().getLogradouro().isEmpty())
        {
            isErro = true;
            this.erroTela.setLogradouroPatrocinador(true);
        } 

        if (null == getPatrocinador().getNumeroEnd() || getPatrocinador().getNumeroEnd().isEmpty()
                || Integer.parseInt(getPatrocinador().getNumeroEnd()) == 0)
        {
            isErro = true;
            this.erroTela.setNumeroEnderecoPatrocinador(true);
        } 

        if (null == getPatrocinador().getBairro() || getPatrocinador().getBairro().isEmpty())
        {
            isErro = true;
            this.erroTela.setBairroPatrocinador(true);
        } 

        if (null == getPatrocinador().getCidade() || getPatrocinador().getCidade().isEmpty())
        {
            isErro = true;
            this.erroTela.setCidadePatrocinador(true);
        } 

        if (null == getPatrocinador().getEstado() || getPatrocinador().getEstado().isEmpty())
        {
            isErro = true;
            this.erroTela.setEstadoPatrocinador(true);
        } 

        if (null == getPatrocinador().getCpf() || retiraMascara(getPatrocinador().getCpf()).trim().isEmpty()
                || retiraMascara(getPatrocinador().getCpf()).length() < 11
                || !isCpfValido(retiraMascara(getPatrocinador().getCpf())))
        {
            isErro = true;
            this.erroTela.setCpfPatrocinador(true);
        } 
        
        if (null != getPatrocinador().getCpf() && !retiraMascara(getPatrocinador().getCpf()).trim().isEmpty() && retiraMascara(getPatrocinador().getCpf()).equalsIgnoreCase(retiraMascara(getCpf()))) {
        	isErro = true;
            this.erroTela.setCpfPatrocinadorIgualParticipante(true);
		}

        if (null == getPatrocinador().getDtNascimento()
                || retiraMascara(getPatrocinador().getDtNascimento()).trim().isEmpty()
                || retiraMascara(getPatrocinador().getDtNascimento()).length() < 8
                || !isDataValida(retiraMascara(getPatrocinador().getDtNascimento())))
        {
            isErro = true;
            this.erroTela.setDtNascimentoPatrocinador(true);
        } 

        if (null == getPatrocinador().getDependenteIr())
        {
            isErro = true;
            this.erroTela.setDependenteIr(true);
        } 

        if (null == getPatrocinador().getGrauParentesco())
        {
            isErro = true;
            this.erroTela.setGrauParentesco(true);
        } 

        if (null == getPatrocinador().getPaisNascimento())
        {
            isErro = true;
            this.erroTela.setPaisNascimentoPatrocinador(true);
        } 
        
		if (this.erroTela.isTelComercialPatrocinador() || this.erroTela.isTelResidencialPatrocinador()) 
		{
			isErro = true;
		}
        
        if ( isErro )
        {
            return "propostaEtapa2.xhtml";
        }

        return "propostaEtapa3.xhtml";
    }

    public String avancarRiscoEtapa3()
    {
        this.resetErroRiscoEtapa2();
        boolean isErro = false;

        if (null == this.telaRiscoEtapa2.getPep() || this.telaRiscoEtapa2.getPep().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaPep(true);
        }

        if (null == this.telaRiscoEtapa2.getRelacionamentoAgPublico()
                || this.telaRiscoEtapa2.getRelacionamentoAgPublico().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaRelacionamento(true);
        }

        if (null == this.telaRiscoEtapa2.getNacionalidadeAlemBrasileira()
                || this.telaRiscoEtapa2.getNacionalidadeAlemBrasileira().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca1(true);
        }

        if (null == this.telaRiscoEtapa2.getResidenciaForaBrasil()
                || this.telaRiscoEtapa2.getResidenciaForaBrasil().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca2(true);
        }

        if (null == this.telaRiscoEtapa2.getVistoOutroPais()
                || this.telaRiscoEtapa2.getVistoOutroPais().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca3(true);
        }

        if (this.telaRiscoEtapa2.getPep().equals("S") || this.telaRiscoEtapa2.getRelacionamentoAgPublico().equals("S"))
        {
            if (this.objetoPep.getOrigemRecurso() == null)
            {
                isErro = true;
                this.erroTela.setOrigemRecurso(true);
            } 
            else
            {
                if (this.objetoPep.getOrigemRecurso().equals(99))
                {
                    if (this.objetoPep.getObs() == null || this.objetoPep.getObs().trim().isEmpty())
                    {
                        isErro = true;
                        this.erroTela.setOrigemRecursoObs(true);
                    }
                }
            }
        }

        if (this.telaRiscoEtapa2.getPep().equals("S"))
        {
            if (this.objetoPep.getCargo() == null || this.objetoPep.getCargo().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setCargoPep(true);
            }

            if (this.objetoPep.getEmpresa() == null || this.objetoPep.getEmpresa().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setEmpresaPep(true);
            }

            if (this.objetoPep.getDataInicial() == null
                    || retiraMascara(this.objetoPep.getDataInicial()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getDataInicial()).length() < 8
                    || !isDataValida(retiraMascara(this.objetoPep.getDataInicial())))
            {
                isErro = true;
                this.erroTela.setDataInicialPep(true);
            }

            if (this.objetoPep.getDataFinal() == null || retiraMascara(this.objetoPep.getDataFinal()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getDataFinal()).length() < 8
                    || !isDataValida(retiraMascara(this.objetoPep.getDataFinal())))
            {
                isErro = true;
                this.erroTela.setDataFinalPep(true);
            }
        }

        if (this.telaRiscoEtapa2.getRelacionamentoAgPublico().equals("S"))
        {
            if (this.objetoPep.getAgenteNome() == null || this.objetoPep.getAgenteNome().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenteNome(true);
            }

            if (this.objetoPep.getAgenteCpf() == null || retiraMascara(this.objetoPep.getAgenteCpf()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getAgenteCpf()).trim().length() < 11
                    || !isCpfValido(retiraMascara(this.objetoPep.getAgenteCpf())))
            {
                isErro = true;
                this.erroTela.setAgenteCpf(true);
            }

            if (this.objetoPep.getAgenteCargo() == null || this.objetoPep.getAgenteCargo().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenteCargo(true);
            }

            if (this.objetoPep.getTipoRelacionamento() == null
                    || this.objetoPep.getTipoRelacionamento().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setTipoRelacionamento(true);
            }
        }

        if (this.telaRiscoEtapa2.getNacionalidadeAlemBrasileira().equals("S"))
        {
            if (this.telaRiscoEtapa2.getCodPais1() == null || this.telaRiscoEtapa2.getCodPais1().equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais1(true);
            }
        }

        if (this.telaRiscoEtapa2.getResidenciaForaBrasil().equals("S"))
        {
            if (this.telaRiscoEtapa2.getCodPais2() == null || this.telaRiscoEtapa2.getCodPais2().equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais2(true);
            }
        }

        if (this.telaRiscoEtapa2.getVistoOutroPais().equals("S"))
        {
            if (this.telaRiscoEtapa2.getCodPais3() == null || this.telaRiscoEtapa2.getCodPais3().equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais3(true);
            }
        }

        if (isErro)
        {
            return "riscoEtapa2.xhtml";
        }

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            setIdadeAposentadoria(24);
        }

        this.resetErroRiscoEtapa3();
        this.telaRiscoEtapa3.setIdadeAposentar(this.idadeAposentadoria > 0 ? true : false);

        return "riscoEtapa3.xhtml";
    }

    public String doAvancarParaEtapa4()
    {
        boolean isErro = false;
        this.erroTela.setPerguntaPep(false);
        this.erroTela.setPerguntaRelacionamento(false);
        this.erroTela.setPerguntaFatca1(false);
        this.erroTela.setPerguntaFatca2(false);
        this.erroTela.setPerguntaFatca3(false);
        this.erroTela.setOrigemRecurso(false);
        this.erroTela.setOrigemRecursoObs(false);
        this.erroTela.setCargoPep(false);
        this.erroTela.setEmpresaPep(false);
        this.erroTela.setDataInicialPep(false);
        this.erroTela.setDataFinalPep(false);
        this.erroTela.setAgenteNome(false);
        this.erroTela.setAgenteCpf(false);
        this.erroTela.setAgenteCargo(false);
        this.erroTela.setTipoRelacionamento(false);
        this.erroTela.setCodPais1(false);
        this.erroTela.setCodPais2(false);
        this.erroTela.setCodPais3(false);
        
        if (this.pep == null || this.pep.trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaPep(true);
        } 

        if (this.relacionamentoAgPublico == null || this.relacionamentoAgPublico.trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaRelacionamento(true);
        } 

        if (this.nacionalidadeAlemBrasileira == null || this.nacionalidadeAlemBrasileira.trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca1(true);
        } 

        if (this.residenciaForaBrasil == null || this.residenciaForaBrasil.trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca2(true);
        } 

        if (this.vistoOutroPais == null || this.vistoOutroPais.trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setPerguntaFatca3(true);
        } 

        if (this.pep.equals("S") || this.relacionamentoAgPublico.equals("S"))
        {
            if (this.objetoPep.getOrigemRecurso() == null)
            {
                isErro = true;
                this.erroTela.setOrigemRecurso(true);
            } 

            if (this.objetoPep.getOrigemRecurso().equals(99))
            {
                if (this.objetoPep.getObs() == null || this.objetoPep.getObs().trim().isEmpty())
                {
                    isErro = true;
                    this.erroTela.setOrigemRecursoObs(true);
                } 
            } 
        } 

        if (this.pep.equals("S"))
        {
            if (this.objetoPep.getCargo() == null || this.objetoPep.getCargo().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setCargoPep(true);
            } 

            if (this.objetoPep.getEmpresa() == null || this.objetoPep.getEmpresa().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setEmpresaPep(true);
            } 

            if (this.objetoPep.getDataInicial() == null
                    || retiraMascara(this.objetoPep.getDataInicial()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getDataInicial()).length() < 8
                    || !isDataValida(retiraMascara(this.objetoPep.getDataInicial())))
            {
                isErro = true;
                this.erroTela.setDataInicialPep(true);
            } 

            if (this.objetoPep.getDataFinal() == null || retiraMascara(this.objetoPep.getDataFinal()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getDataFinal()).length() < 8
                    || !isDataValida(retiraMascara(this.objetoPep.getDataFinal())))
            {
                isErro = true;
                this.erroTela.setDataFinalPep(true);
            } 
        } 

        if (this.relacionamentoAgPublico.equals("S"))
        {
            if (this.objetoPep.getAgenteNome() == null || this.objetoPep.getAgenteNome().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenteNome(true);
            } 

            if (this.objetoPep.getAgenteCpf() == null || retiraMascara(this.objetoPep.getAgenteCpf()).trim().isEmpty()
                    || retiraMascara(this.objetoPep.getAgenteCpf()).trim().length() < 11
                    || !isCpfValido(retiraMascara(this.objetoPep.getAgenteCpf())))
            {
                isErro = true;
                this.erroTela.setAgenteCpf(true);
            } 

            if (this.objetoPep.getAgenteCargo() == null || this.objetoPep.getAgenteCargo().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenteCargo(true);
            } 

            if (this.objetoPep.getTipoRelacionamento() == null
                    || this.objetoPep.getTipoRelacionamento().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setTipoRelacionamento(true);
            } 
        } 

        if (this.nacionalidadeAlemBrasileira.equals("S"))
        {
            if (this.codPais1 == null || this.codPais1.equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais1(true);
            } 
        } 

        if (this.residenciaForaBrasil.equals("S"))
        {
            if (this.codPais2 == null || this.codPais2.equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais2(true);
            } 
        } 

        if (this.vistoOutroPais.equals("S"))
        {
            if (this.codPais3 == null || this.codPais3.equals(0))
            {
                isErro = true;
                this.erroTela.setCodPais3(true);
            } 
        } 

        if ( isErro )
        {
            return "propostaEtapa3.xhtml";
        }

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            setIdadeAposentadoria(24);
        }

        return "propostaEtapa4.xhtml";
    }

    public String avancarRiscoEtapa4()
    {
        this.resetErroRiscoEtapa3();
        boolean isErro = false;

        if (this.telaRiscoEtapa3.getExibirDadosContaBancaria()
                && !pesquisarContaCorrente(this.telaRiscoEtapa3.getAgenciaDebito(), this.telaRiscoEtapa3.getConta(), true))
        {
            this.erroTela.setContaCorrente(true);
            return "riscoEtapa3.xhtml";
        }

        if (this.idadeAposentadoria > 0)
        {
            if (this.telaRiscoEtapa3.getIdadeAposentadoria() <= 0)
            {
                isErro = true;
                this.erroTela.setIdadeAposentadoria(true);
            }
        }

        if (this.ofertaPlanoEnvio.getTipoContribuicao().equals(1)){
	        if (this.telaRiscoEtapa3.getDiaVencimento() == null || this.telaRiscoEtapa3.getDiaVencimento().trim().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setDiaPagamento(true);
	        }
        }else {
        	this.telaRiscoEtapa3.setDiaVencimento("0");
        }

        if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("0"))
        {
            isErro = true;
            this.erroTela.setFormaPagamento(true);
        }

        if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("1"))
        {
            if (this.telaRiscoEtapa3.getAgenciaDebito() == null
                    || this.telaRiscoEtapa3.getAgenciaDebito().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenciaPagamento(true);
            }

            if (this.telaRiscoEtapa3.getConta() == null || this.telaRiscoEtapa3.getConta().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setContaPagamento(true);
            }

            if (this.telaRiscoEtapa3.getTpContaParticipante().equals("0"))
            {
                isErro = true;
                this.erroTela.setTipoContaPagamento(true);
            }
        }

        String dataAtual = new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime());
        SimpleDateFormat formato = new SimpleDateFormat("ddMMyyyy");
        Date dataAtualConvertida = new Date();

        try
        {
            dataAtualConvertida = formato.parse(dataAtual);
        } 
        catch ( ParseException e)
        {
            isErro = true;
            this.erroTela.setDataAposentadoriaMenorQueAtual(true);
        }

        String dataConvertida = retiraMascara(this.dataNascimento);
        Integer dataAposentadoria = Integer.parseInt(dataConvertida.substring(4)) + this.idadeAposentadoria;
        String dataAposentadoriaString = dataConvertida.substring(0, 4) + dataAposentadoria.toString();
        Date dataAposentadoriaConvertida = new Date();

        try
        {
            dataAposentadoriaConvertida = formato.parse(dataAposentadoriaString);
        } 
        catch ( ParseException e)
        {
            isErro = true;
            this.erroTela.setDataAposentadoriaMenorQueAtual(true);
        }

        if (this.idadeAposentadoria > 0)
        {
            if (dataAtualConvertida.after(dataAposentadoriaConvertida)
                    || dataAtualConvertida.equals(dataAposentadoriaConvertida))
            {
                isErro = true;
                this.erroTela.setDataAposentadoriaMenorQueAtual(true);
            }
        }

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            if (this.prazoRecebimento == null || this.prazoRecebimento.trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setPrazoRecebimento(true);
            }
        }

        if (isErro)
        {
            return "riscoEtapa3.xhtml";
        }

        this.incluirRascunhoEnvio.setDiaVencimento(this.telaRiscoEtapa3.getDiaVencimento());
      
      
        if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("1")){
            this.incluirRascunhoEnvio.setIdentPagamento("1");
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1")){
                this.incluirRascunhoEnvio.setFormaPagamento("1");
            } 
            else {
                this.incluirRascunhoEnvio.setFormaPagamento("2");
            }         
        }else if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("2")){
            this.incluirRascunhoEnvio.setIdentPagamento("2");     	
            this.incluirRascunhoEnvio.setFormaPagamento("3");
        }else if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("4")){
            this.incluirRascunhoEnvio.setIdentPagamento("1");  
            this.incluirRascunhoEnvio.setFormaPagamento("4");
        }else if (this.telaRiscoEtapa3.getIdentPagamentoTela().equals("5")){
            this.incluirRascunhoEnvio.setIdentPagamento("2");   
            this.incluirRascunhoEnvio.setFormaPagamento("5");
        }
                
        this.incluirRascunhoEnvio.setAgenciaDebito(this.telaRiscoEtapa3.getAgenciaDebito());
        this.incluirRascunhoEnvio.setConta(this.telaRiscoEtapa3.getConta());
        this.incluirRascunhoEnvio.setTpContaParticipante(this.telaRiscoEtapa3.getTpContaParticipante());
        this.incluirRascunhoEnvio.setConcsBenef("01.01.0001");

        if (this.cnpjCorretor == null || "".equals(this.cnpjCorretor) || Long.valueOf(this.cnpjCorretor) == 0) {
			identCorretor = cpfCorretor;
        	recuperarDadosParceiroNegocio();
        } else {
        	identCorretor = cnpjCorretor;
        	recuperarDadosParceiroNegocio();
        	 if(!autoPreenchimentoCorretor) {
 				if (cpfCorretor != null && !cpfCorretor.isEmpty() && !Long.valueOf(cpfCorretor).equals(0L)){
        			identCorretor = cpfCorretor;
        			recuperarDadosParceiroNegocio();
        		}
        	}
        }

        if (null != opComercialTO && !opComercialTO.isEmpty())
        {
            this.telaRiscoEtapa4.setAutoPreenchimentoCorretor(true);
        } 
        else
        {
            this.telaRiscoEtapa4.setAutoPreenchimentoCorretor(false);
        }

        this.resetErroRiscoEtapa4();

        return "riscoEtapa4.xhtml";
    }

    public String doAvancarParaEtapa5()
    {
        boolean isErro = false;
        this.erroTela.setContaCorrente(false);
        this.erroTela.setIdadeAposentadoria(false);
        this.erroTela.setDiaPagamento(false);
        this.erroTela.setFormaPagamento(false);
        this.erroTela.setAgenciaPagamento(false);
        this.erroTela.setContaPagamento(false);
        this.erroTela.setTipoContaPagamento(false);
        this.erroTela.setPrazoRecebimento(false);
        this.erroTela.setDataAposentadoriaMenorQueAtual(false);
        
        if (isExibirDadosContaBancaria() && !pesquisarContaCorrente(this.incluirRascunhoEnvio.getAgenciaDebito(),
                this.incluirRascunhoEnvio.getConta(), false))
        {
            isErro = true;
            this.erroTela.setContaCorrente(true);
        } 

        if (this.idadeAposentadoria == null || this.idadeAposentadoria.equals(0))
        {
            isErro = true;
            this.erroTela.setIdadeAposentadoria(true);
        } 

        if (this.ofertaPlanoEnvio.getTipoContribuicao().equals(1)){
            if (this.incluirRascunhoEnvio.getDiaVencimento() == null
                    || this.incluirRascunhoEnvio.getDiaVencimento().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setDiaPagamento(true);
            } 	
        }else {
        	this.incluirRascunhoEnvio.setDiaVencimento("0");
        }      

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela() == null
                || this.incluirRascunhoEnvio.getIdentPagamentoTela().trim().isEmpty())
        {
            isErro = true;
            this.erroTela.setFormaPagamento(true);
        } 

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1"))
        {

            if (this.incluirRascunhoEnvio.getAgenciaDebito() == null
                    || this.incluirRascunhoEnvio.getAgenciaDebito().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setAgenciaPagamento(true);
            } 

            if (this.incluirRascunhoEnvio.getConta() == null || this.incluirRascunhoEnvio.getConta().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setContaPagamento(true);
            } 

            if (this.incluirRascunhoEnvio.getTpContaParticipante() == null
                    || this.incluirRascunhoEnvio.getTpContaParticipante().trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setTipoContaPagamento(true);
            } 
        } 
        
 
        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1")){
            this.incluirRascunhoEnvio.setIdentPagamento("1");
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1")){
                this.incluirRascunhoEnvio.setFormaPagamento("1");
            } 
            else {
                this.incluirRascunhoEnvio.setFormaPagamento("2");
            } 
            
        }else if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("2")){
            this.incluirRascunhoEnvio.setIdentPagamento("2");     	
            this.incluirRascunhoEnvio.setFormaPagamento("3");   	
        }else if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("4")){
            this.incluirRascunhoEnvio.setIdentPagamento("1");  
            this.incluirRascunhoEnvio.setFormaPagamento("4");  	
        }else if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("5")){
            this.incluirRascunhoEnvio.setIdentPagamento("2");   
            this.incluirRascunhoEnvio.setFormaPagamento("5");     	
        }

        String dataAtual = new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime());
        SimpleDateFormat formato = new SimpleDateFormat("ddMMyyyy");
        Date dataAtualConvertida = new Date();
        
        try
        {
            dataAtualConvertida = formato.parse(dataAtual);
        }
        catch( ParseException e )
        {
            isErro = true;
            this.erro = "ContrataPrev|doAvancarParaEtapa5|ParseException: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        }
        
        String dataConvertida = retiraMascara(this.dataNascimento);
        Integer dataAposentadoria = Integer.parseInt(dataConvertida.substring(4)) + this.idadeAposentadoria;
        String dataAposentadoriaString = dataConvertida.substring(0, 4) + dataAposentadoria.toString();
        Date dataAposentadoriaConvertida = new Date();
        
        try
        {
            dataAposentadoriaConvertida = formato.parse(dataAposentadoriaString);
        }
        catch( ParseException e )
        {
            isErro = true;
            this.erro = "ContrataPrev|doAvancarParaEtapa5|ParseException: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        }
        
        if (dataAtualConvertida.after(dataAposentadoriaConvertida)
                || dataAtualConvertida.equals(dataAposentadoriaConvertida))
        {
            isErro = true;
            this.erroTela.setDataAposentadoriaMenorQueAtual(true);
        } 

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            if (this.prazoRecebimento == null || this.prazoRecebimento.trim().isEmpty())
            {
                isErro = true;
                this.erroTela.setPrazoRecebimento(true);
            } 
        }

        if ( isErro )
        {
            return "propostaEtapa4.xhtml";
        }

        this.incluirRascunhoEnvio.setConcsBenef(incluirPontoData(dataAposentadoriaString));
        
        if (this.cnpjCorretor == null || "".equals(this.cnpjCorretor) || Long.valueOf(this.cnpjCorretor) == 0) {
			identCorretor = cpfCorretor;
        	recuperarDadosParceiroNegocio();
        } else {
        	identCorretor = cnpjCorretor;
        	recuperarDadosParceiroNegocio();
        	if(!autoPreenchimentoCorretor) {
        		if (this.cpfCorretor == null || "".equals(this.cpfCorretor) || Long.valueOf(this.cpfCorretor) == 0) {
        			identCorretor = cpfCorretor;
        			recuperarDadosParceiroNegocio();
        		}
        	}
        }

        return "propostaEtapa5.xhtml";
    }

    public String avancarRiscoEtapa5()
    {
        this.resetErroRiscoEtapa4();
        boolean isErro = false;

        if(this.telaRiscoEtapa4.getEstruturaComercial() == null || this.telaRiscoEtapa4.getEstruturaComercial().trim().isEmpty()) {
        	isErro = true;
        	this.erroTela.setEstruturaComercial(true);
        } else if (this.telaRiscoEtapa4.getEstruturaComercial().equalsIgnoreCase("mercado") && !this.telaRiscoEtapa4.getAutoPreenchimentoCorretor()){
        	isErro = true;
        	this.erroTela.setErroSucursalSap(true);
        } else {
        	if(this.telaRiscoEtapa4.getEstruturaComercial().equalsIgnoreCase("mercado")) {
        		if (this.telaRiscoEtapa4.getAutoPreenchimentoCorretor() == false) {
	    	        if (this.telaRiscoEtapa4.getCodSucursalDigitado() == null || this.telaRiscoEtapa4.getCodSucursalDigitado().trim().isEmpty() 
	    	        || this.telaRiscoEtapa4.getCodSucursalDigitado().equals("0")) {
	    	        	isErro = true;
	    	        	this.erroTela.setSucursalDigitada(true);
	    	        }else {
	    	    		setCodSucursal(this.telaRiscoEtapa4.getCodSucursalDigitado());
	    	        }
	            } 
        		this.telaRiscoEtapa4.setAgencia("0001");
        		this.telaRiscoEtapa4.setGerenteComercial("9806");
        	}
        	
	        if (this.telaRiscoEtapa4.getCodCorretor().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setCodigoCorretor(true);
	        }
	
	        if (this.telaRiscoEtapa4.getAgencia().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setAgenciaComercial(true);
	        }
	
	        if (this.telaRiscoEtapa4.getGerenteComercial().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setGerenteComercial(true);
	        } 
	        else
	        {
	            this.incluirRascunhoEnvio.setVlrResgate(this.telaRiscoEtapa4.getGerenteComercial() + "000000.00");
	            this.erroTela.setMensagemTela04( "" );
	            this.erroTela.setTela04( false );
	
	            this.erro = "ContrataPrev|ERRO TELA04|CODIGO GERENTE:" + this.telaRiscoEtapa4.getGerenteComercial();
	            this.logService.error( this.erro );
	            LoggerFactory.getLogger().log(Level.INFO, this.erro );
	
	//                this.validaPropostaInscricaoEnvio.setCodParticipante( adicionaZeroEsquerda(Long.parseLong(this.telaRiscoEtapa4.getGerenteComercial()), 7));
	            String tmp = "";
	            
	            for( int i = 0; i < ( 7 - this.telaRiscoEtapa4.getGerenteComercial().length() ); ++i )
	            {
	                tmp += "0";
	            }
	
	            tmp += this.telaRiscoEtapa4.getGerenteComercial();
	            this.validaPropostaInscricaoEnvio.setCodParticipante( tmp );
	
	            this.erro = "ContrataPrev|ERRO TELA04|CODIGO GERENTE 7 DIGITOS:" + this.validaPropostaInscricaoEnvio.getCodParticipante();
	            this.logService.error( this.erro );
	            LoggerFactory.getLogger().log(Level.INFO, this.erro );
	        }

	        this.erro = "ContrataPrev|ERRO TELA04|AUTOPREENCHIMENTO:" + this.telaRiscoEtapa4.getAutoPreenchimentoCorretor();
	        this.logService.error( this.erro );
	        LoggerFactory.getLogger().log(Level.INFO, this.erro );
	        
	        this.erro = "ContrataPrev|ERRO TELA04|CORRETOR TELA:" + this.telaRiscoEtapa4.getCodCorretorTela();
	        this.logService.error( this.erro );
	        LoggerFactory.getLogger().log(Level.INFO, this.erro );
	
	        if (this.telaRiscoEtapa4.getAutoPreenchimentoCorretor())
	        {
	            if (this.telaRiscoEtapa4.getCodCorretorTela().isEmpty())
	            {
	                isErro = true;
	                this.erroTela.setSucursal(true);
	            }
	        }
        }
        
        if (isErro)
        {
            return "riscoEtapa4.xhtml";
        }

        this.erro = "ContrataPrev|ERRO TELA04|AGENCIA:" + this.telaRiscoEtapa4.getAgencia();
        this.logService.error( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        this.erro = "ContrataPrev|ERRO TELA04|DT NASCIMENTO:" + this.dtNascimentoBeneficiarioRisco;
        this.logService.error( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        this.erro = "ContrataPrev|ERRO TELA04|SEXO:" + this.sexoBeneficiarioRisco;
        this.logService.error( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        
        this.incluirRascunhoEnvio.setAgencia(this.telaRiscoEtapa4.getAgencia());
        this.incluirRascunhoEnvio.setCodAgencia(this.telaRiscoEtapa4.getAgencia());
        if(this.telaRiscoEtapa4.getEstruturaComercial().equalsIgnoreCase("mercado"))
        {
        	this.incluirRascunhoEnvio.setCodSucur(getCodSucursal());
        } else 
        {
        	this.incluirRascunhoEnvio.setCodSucur("");
        }
        this.resetErroRiscoEtapa5();
        this.beneficiarioRisco.setDataNascimento(this.dtNascimentoBeneficiarioRisco);
        this.beneficiarioRisco.setSexo(this.sexoBeneficiarioRisco.equals("M") ? "Masculino" : "Feminino");

        return "riscoEtapa5.xhtml";
    }

    public String doAvancarParaEtapa6()
    {
        boolean isErro = false;
        this.erroTela.setErroSucursalSap(false);
        this.erroTela.setEstruturaComercial(false);
        this.erroTela.setAgenciaComercial(false);
        this.erroTela.setCodigoCorretor(false);
        this.erroTela.setGerenteComercial(false);
        this.erroTela.setSucursal(false);
        this.erroTela.setSucursalDigitada(false);
           
        
        if(this.estruturaComercial == null || this.estruturaComercial.trim().isEmpty()) {
        	isErro = true;
        	this.erroTela.setEstruturaComercial(true);
        } else if (this.estruturaComercial.equalsIgnoreCase("mercado") && !this.autoPreenchimentoCorretor){
        	isErro = true;
        	this.erroTela.setErroSucursalSap(true);
        } else {
            if (this.estruturaComercial.equalsIgnoreCase("mercado")) {
	            if (this.autoPreenchimentoCorretor == false){
	    	        if (this.codSucursalDigitado == null || this.codSucursalDigitado.trim().isEmpty() 
	    	        || this.codSucursalDigitado.equals("0"))
	    	        {
	    	        	isErro = true;
	    	        	this.erroTela.setSucursalDigitada(true);
	    	        }else {
	    	    		setCodSucursal(this.codSucursalDigitado);
	    	        }
	            } 
	      		this.incluirRascunhoEnvio.setAgencia("0001");
        		this.gerenteComercial = "9806";
        	}
        	
	        if (this.incluirRascunhoEnvio.getAgencia() == null || this.incluirRascunhoEnvio.getAgencia().trim().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setAgenciaComercial(true);
	        } 
	
	        if (codCorretor == null || codCorretor.trim().isEmpty())
	        {
	            isErro = true;
	            this.erroTela.setCodigoCorretor(true);
	        } 
	
	        if (this.gerenteComercial != null && !this.gerenteComercial.trim().isEmpty())
	        {
	            this.incluirRascunhoEnvio.setVlrResgate(this.gerenteComercial + "000000.00");
	            this.validaPropostaInscricaoEnvio.setCodParticipante(adicionaZeroEsquerda(Long.parseLong(this.gerenteComercial), 7));
	        } 
	        else
	        {
	            isErro = true;
	            this.erroTela.setGerenteComercial(true);
	        }
	
	        if (isAutoPreenchimentoCorretor())
	        {
	            if (null == getCodCorretorTela() || getCodCorretorTela().isEmpty())
	            {
	                isErro = true;
	                this.erroTela.setSucursal(true);
	            } 
	        }
        }
        if ( isErro )
        {
            return "propostaEtapa5.xhtml";
        }

        this.incluirRascunhoEnvio.setCodAgencia(this.incluirRascunhoEnvio.getAgencia());

        return "propostaEtapa6.xhtml";
    }

    public String doAvancarParaConfirmacao()
    {
        boolean isErro = false;
        this.erroTela.setBeneficiarios(false);
        this.erroTela.setPorcentagemTotalMenorQueCem(false);
        
        if (this.listaBeneficiarios.isEmpty())
        {
            isErro = true;
            this.erroTela.setBeneficiarios(true);
        }

        if (this.somaPorcentagem < 100)
        {
            isErro = true;
            this.erroTela.setPorcentagemTotalMenorQueCem(true);
        } 

        if ( isErro )
        {
            return "propostaEtapa6.xhtml";
        }

        this.confirmacaoHabilitado = false;
        return "confirmacao.xhtml";
    }

    public String avancarConfirmacaoRisco()
    {
        this.resetErroRiscoEtapa5();
        boolean isErro = false;

        if (this.beneficiarioRisco.getNomeCompleto().isEmpty())
        {
            isErro = true;
            this.erroTela.setNome(true);
        }

        if (this.beneficiarioRisco.getGrauParentesco() == 0)
        {
            isErro = true;
            this.erroTela.setGrauParentesco(true);
        }

        /**
         * verificar se filho tem 12 anos de diferenca do pai.
         */
        if ( this.beneficiarioRisco.getGrauParentesco() == ParentescoBeneficiarioRisco.FILHO.getValor() )
        {
            try
            {
                int anos = 0;
                Calendar calendar = new GregorianCalendar();
                final Date dataPai = new SimpleDateFormat("dd/MM/yyyy").parse( this.dataNascimento );
                final Date dataFilho = new SimpleDateFormat("dd/MM/yyyy").parse( this.beneficiarioRisco.getDataNascimento() );

                calendar.setTime(dataFilho);
                anos = calendar.get(Calendar.YEAR);
                calendar.setTime(dataPai);
                anos -= calendar.get(Calendar.YEAR);

                if (anos >= 0 && anos <= 12 )
                {
                    this.erroTela.setGrauParentesco(true);
                    isErro = true;
                    this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Filho deve ter 12 anos de diferença de Pai/Mãe."));
                }
                
                if ( anos < 0 )
                {
                    this.erroTela.setGrauParentesco(true);
                    isErro = true;
                    this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Pai/Mãe deve ter 12 anos de diferença de seu filho."));
                }
            } 
            catch ( ParseException e)
            {
                isErro = true;
                this.erro = "ContrataPrev|doAdicionarBeneficiario|Erro ao converter da nascimento do PAI e Filho: " +
                    CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            }
        }
        
        /**
         * verificar se pais são mais novos que filho
         */
        if ( this.beneficiarioRisco.getGrauParentesco() == ParentescoBeneficiarioRisco.PAI_MAE.getValor() )
        {
            try
            {
                int anos = 0;
                Calendar calendar = new GregorianCalendar();
                final Date dataPai = new SimpleDateFormat("dd/MM/yyyy").parse( this.beneficiarioRisco.getDataNascimento() );
                final Date dataFilho = new SimpleDateFormat("dd/MM/yyyy").parse( this.dataNascimento );

                calendar.setTime(dataFilho);
                anos = calendar.get(Calendar.YEAR);
                calendar.setTime(dataPai);
                anos -= calendar.get(Calendar.YEAR);

                if (anos >= 0 && anos <= 12 )
                {
                    this.erroTela.setGrauParentesco(true);
                    isErro = true;
                    this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Filho deve ter 12 anos de diferença de Pai/Mãe."));
                }
                
                if ( anos < 0 )
                {
                    this.erroTela.setGrauParentesco(true);
                    isErro = true;
                    this.erroTela.setGrauParentescoDesc( converteEncodingTexto("Pai/Mãe deve ter 12 anos de diferença de seu filho."));
                }
            } 
            catch ( ParseException e)
            {
                this.erro = "ContrataPrev|doAdicionarBeneficiario|Erro ao converter da nascimento do PAI e Filho: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            }
        }

        if (isErro)
        {
            return "riscoEtapa5.xhtml";
        }

        this.listaBeneficiariosRisco.clear();
        this.listaBeneficiariosRisco.add(beneficiarioRisco);
        return "confirmacaoRisco.xhtml";
    }

    public void calculaRentabilidadeFundo()
    {
        Double rentab3 = 1.0;
        Double rentab6 = 1.0;
        Double rentab12 = 1.0;

        for (int i = 0; i < 3 && i < this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().size(); i++)
        {
            if (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidadeSinal().trim().equals("-"))
            {
                rentab3 = rentab3 * (1 + ((this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() * -1) / 100));
            } 
            else
            {
                rentab3 = rentab3 * (1 + (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() / 100));
            }
        }
        rentab3 = (rentab3 - 1) * 100;

        for (int i = 0; i < 6 && i < this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().size(); i++)
        {
            if (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidadeSinal().trim().equals("-"))
            {
                rentab6 = rentab6 * (1 + ((this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() * -1) / 100));
            } 
            else
            {
                rentab6 = rentab6 * (1 + (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() / 100));
            }
        }

        rentab6 = (rentab6 - 1) * 100;

        for (int i = 0; i < 12 && i < this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().size(); i++)
        {
            if (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidadeSinal().trim().equals("-"))
            {
                rentab12 = rentab12 * (1 + ((this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() * -1) / 100));
            } 
            else
            {
                rentab12 = rentab12 * (1 + (this.detalhaFundoRetorno.getIndices().get(0).getRendimentos().get(i).getTxRentabilidade() / 100));
            }
        }

        rentab12 = (rentab12 - 1) * 100;

        setRentabilidade3(String.format("%.2f", rentab3));
        setRentabilidade6(String.format("%.2f", rentab6));
        setRentabilidade12(String.format("%.2f", rentab12));
    }
    
    public String getRentabilidade(Integer codPlano, Integer codBeneficio) {
    	
    	String comparable = codPlano.toString()+"/"+ codBeneficio.toString();
    	for (Map.Entry<String, String> map : rentabilidadeOferta.entrySet()) {
			if (map.getKey().equals(comparable)) {
				return trocarPontoString(map.getValue())+"%";
			}
		}
    	return "N/A";
    }

    public String doFecharModalDetalhesPlano()
    {
        setExibeModalDetalheOferta(false);
        return getPaginaRecebidaDetalhes();
    }

    public String doConfirmar()
    {
        if ( !this.cssBotaoDesabilitado ) 
        {
            return "confirmacao.xhtml";
        }
        setMsgGenerica(false);
        
        if (mockApi)
        {
            RetornoMockApi retornoMockApi = new RetornoMockApi();
            final String retorno = retornoMockApi.incluirRascunho();
            this.incluirRascunhoRetorno = new Gson().fromJson(retorno, IncluirRascunhoRetornoVo.class);
        } 
        else
        {
            popularIncluirRascunho();

            if (null == this.incluirRascunhoRetorno || null == this.incluirRascunhoRetorno.getCodProposta())
            {
                this.incluirRascunhoRetorno=  service.incluirRascunho(this.incluirRascunhoEnvio, getCodCorretor());
                
                if ( null == this.incluirRascunhoRetorno )
                {
                    setMsgErro( "Erro ao incluir rascunho." );
                    this.erroTela.setModal(true);
                    return "confirmacao.xhtml";
                }
            }

            popularValidarInscricao();
            ValidaPropostaInscricaoRetornoVo tmp = null;
            
            tmp = service.validarPropostaInscricao(this.validaPropostaInscricaoEnvio, getCodCorretor() );

            if ( null == tmp )
            {
                this.erro = "ContrataPrev|doConfirmar|ServiceExceptionIncluirRascunho|Erro IncluirRascunho: ";
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

//                    RetornoErroValidacaoVo objErro = new RetornoErroValidacaoVo();
                setMsgErro( "Erro ao validar inscricao da proposta." );
                this.erroTela.setModal(true);
                return "confirmacao.xhtml";
            }

            setValidaPropostaInscricaoRetorno( tmp );

            if (!this.validaPropostaInscricaoRetorno.getLinhas().isEmpty())
            {
                this.erro = "ContrataPrev|doConfirmar|Erro Api Valida Proposta" + this.validaPropostaInscricaoRetorno.getLinhas();
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                setMsgErro(converteEncodingTexto( this.validaPropostaInscricaoRetorno.getLinhas().get(0).getCampoErro()));
                this.erroTela.setModal(true);
                return "confirmacao.xhtml";
            }

            // segunda chamada - incluir rascunho
            this.incluirRascunhoEnvio.setIndicadorOperacao("3");
            this.incluirRascunhoEnvio.setNrProposta(Integer.toString(this.incluirRascunhoRetorno.getCodProposta()));
            this.incluirRascunhoEnvio.setSerie(Integer.toString(this.incluirRascunhoRetorno.getSerieProposta()));
            setIncluirRascunhoRetorno(new IncluirRascunhoRetornoVo());
            this.incluirRascunhoRetorno = service.incluirRascunho(this.incluirRascunhoEnvio, getCodCorretor());
            
            if ( null == this.incluirRascunhoRetorno )
            {
                setMsgErro( "Erro no retorno de incluir rascunho." );
                this.erroTela.setModal(true);
                return "confirmacao.xhtml";
            }

            gerarPdf();

        }

        this.erroTela.setModal(false);
        setGeraPropostaEnvio(new GeraPropostaEnvioVo());
        geraPropostaEnvio.setCanal("27");
        geraPropostaEnvio.setOrigem("1");
        geraPropostaEnvio.setUsuario("0");
        geraPropostaEnvio.setIdtfdFuncionario("N");
        geraPropostaEnvio.setCorretor(getCodCorretor());
        geraPropostaEnvio.setIdtfdRepresentante("N");
        geraPropostaEnvio.setSerie(this.incluirRascunhoRetorno.getSerieProposta().toString());
        geraPropostaEnvio.setProposta(this.incluirRascunhoRetorno.getCodProposta().toString());
        geraPropostaEnvio.setDtVenda(this.incluirRascunhoEnvio.getDtVenda());
        geraPropostaEnvio.setTpProposta("2");

        service.geraProposta(geraPropostaEnvio);

        return "propostaTransmitida.xhtml";
    }

    public void popularIncluirRascunho()
    {
        if (!this.incluirRascunhoEnvio.getCodAge().isEmpty())
        {
            this.incluirRascunhoEnvio.setCodAge(incluirZero(this.incluirRascunhoEnvio.getCodAge(), 7));
        }

        String dataAtual = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
        this.incluirRascunhoEnvio.setCodBloco(0);
        this.incluirRascunhoEnvio.setIndicadorOperacao("1");
        this.incluirRascunhoEnvio.setDtVenda(dataAtual);
        this.incluirRascunhoEnvio.setDtAgendamento(dataAtual);
        this.incluirRascunhoEnvio.setCodFinalidadeProposta("1");
        this.incluirRascunhoEnvio.setIdentSit("1");
        this.incluirRascunhoEnvio.setEmissaoExtrato("3");
        this.incluirRascunhoEnvio.setCodBanco("237");
        this.incluirRascunhoEnvio.setCodConvenio("884");
        this.incluirRascunhoEnvio.setBanco("237");
        this.incluirRascunhoEnvio.setDtIniVigenciaBenef("01.01.0001");
        this.incluirRascunhoEnvio.setDtIniVigenciaPlano("01.01.0001");
        this.incluirRascunhoEnvio.setFatorBenef(0);
        this.incluirRascunhoEnvio.setFormaExtrato("3");
        this.incluirRascunhoEnvio.setFundoPrevidencia("0");
        this.incluirRascunhoEnvio.setIdentExtrtPapel("N");
        this.incluirRascunhoEnvio.setIdentNegoc("N");
        this.incluirRascunhoEnvio.setProduto("N");
        this.incluirRascunhoEnvio.setProdutoFaml("N");
        this.incluirRascunhoEnvio.setProdutoPrev("N");
        this.incluirRascunhoEnvio.setQtdeBeneficio(1);
        this.incluirRascunhoEnvio.setTributo("0");
        this.incluirRascunhoEnvio.setNrProposta("0");
        this.incluirRascunhoEnvio.setSerie("0");
        this.incluirRascunhoEnvio.setIdentAgContribuinte("2");
        this.incluirRascunhoEnvio.setCodIdentificacaoExerc("2");
        this.incluirRascunhoEnvio.setTpConta( "" + this.ofertaPlanoEnvio.getTipoContribuicao() );
        this.incluirRascunhoEnvio.setVlrLiquido((!this.editaCpf) ? this.detalhaParticipante.getVlrPatrimonio().toString() : "0");
        
        if(this.estruturaComercial.equalsIgnoreCase("mercado"))
        {
        	this.incluirRascunhoEnvio.setCodSucur(getCodSucursal());
        }
        else
        {
        	this.incluirRascunhoEnvio.setCodSucur("");
        }

        String cpfNoveDigitos = retiraMascara(getDadosTelaEtapa1().getCpf());

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1")){
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1")){
                this.incluirRascunhoEnvio.setFormaPagamento("1");
            } 
            else {
                this.incluirRascunhoEnvio.setFormaPagamento("2");
            }
        }else
        	
        {
            this.incluirRascunhoEnvio.setTpContaParticipante("3");
            this.incluirRascunhoEnvio.setAgenciaDebito("0");
            this.incluirRascunhoEnvio.setConta("0");
        }

        if (this.pep.equals("S"))
        {
            this.incluirRascunhoEnvio.setCargoEmpreg(this.objetoPep.getCargo());
            this.incluirRascunhoEnvio.setCodControleEmpreg(cpfNoveDigitos.substring(9, 11));
            this.incluirRascunhoEnvio.setCodIdentificacaoExerc("1");
            this.incluirRascunhoEnvio.setCpfCnpjEmpreg(cpfNoveDigitos.substring(0, 9));
            this.incluirRascunhoEnvio.setDtFimExerc(adicionaPontoData(this.objetoPep.getDataFinal()));
            this.incluirRascunhoEnvio.setDtInicioExerc(adicionaPontoData(this.objetoPep.getDataInicial()));
            this.incluirRascunhoEnvio.setOrigemPblic(this.objetoPep.getEmpresa());
            this.incluirRascunhoEnvio.setOrigemRecProdt(this.objetoPep.getOrigemRecurso());
        } 
        else
        {
            this.incluirRascunhoEnvio.setCargoEmpreg(new String());
            this.incluirRascunhoEnvio.setCodControleEmpreg(new String());
            this.incluirRascunhoEnvio.setCpfCnpjEmpreg(new String());
            this.incluirRascunhoEnvio.setDtFimExerc(new String());
            this.incluirRascunhoEnvio.setDtInicioExerc(new String());
            this.incluirRascunhoEnvio.setOrigemPblic(new String());
        }

        if (this.relacionamentoAgPublico.equals("S"))
        {
            this.incluirRascunhoEnvio.setAgControle(this.objetoPep.getAgenteNome());
            this.incluirRascunhoEnvio.setCargo(this.objetoPep.getAgenteCargo());
            String cpfAgente = retiraMascara(this.objetoPep.getAgenteCpf()).trim();
            this.incluirRascunhoEnvio.setCodControleAg(cpfAgente.substring(cpfAgente.length() - 2, cpfAgente.length()));
            this.incluirRascunhoEnvio.setCpfCnpjAg(cpfAgente.substring(0, 9));
            this.incluirRascunhoEnvio.setIdentAgContribuinte("1");
            this.incluirRascunhoEnvio.setOrigemRecProdt(this.objetoPep.getOrigemRecurso());
            this.incluirRascunhoEnvio.setReferenciaRelacionamentoAgente(this.objetoPep.getTipoRelacionamento());
        } 
        else
        {
            this.incluirRascunhoEnvio.setAgControle(new String());
            this.incluirRascunhoEnvio.setCargo(new String());
            this.incluirRascunhoEnvio.setCodControleAg(new String());
            this.incluirRascunhoEnvio.setCpfCnpjAg(new String());
            this.incluirRascunhoEnvio.setReferenciaRelacionamentoAgente(new String());
        }

        if (!"S".equalsIgnoreCase(this.pep) && !"S".equalsIgnoreCase(this.relacionamentoAgPublico))
        {
            this.incluirRascunhoEnvio.setOrigemRecProdt(new Integer(0));
        }

        if (null != this.objetoPep.getOrigemRecurso() && this.objetoPep.getOrigemRecurso().equals(99))
        {
            this.incluirRascunhoEnvio.setObservacaoOrigem(this.objetoPep.getObs());
        }
        
        this.incluirRascunhoEnvio.setCodProduto(this.selecionado.getCodProdutoComercial());
        this.incluirRascunhoEnvio.setPlanoPgt(Integer.toString(this.selecionado.getCodPlano()));
        this.incluirRascunhoEnvio.setMatricula((!this.editaCpf) ? Long.toString(this.detalhaParticipante.getMatricula()) : "0");

        if (this.nacionalidadeAlemBrasileira.equals("S"))
        {
            setRespondeuPergunta1(1);
        } 
        else
        {
            setRespondeuPergunta1(2);
        }

        if (this.residenciaForaBrasil.equals("S"))
        {
            setRespondeuPergunta2(1);
        } 
        else
        {
            setRespondeuPergunta2(2);
        }

        if (this.vistoOutroPais.equals("S"))
        {
            setRespondeuPergunta3(1);
        } 
        else
        {
            setRespondeuPergunta3(2);
        }

        Integer qntRespostaSim = 0;

        if (this.respondeuPergunta1.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta2.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta3.equals(1))
        {
            qntRespostaSim++;
        }
        this.incluirRascunhoEnvio.setQuantidade(qntRespostaSim.toString());

        this.incluirRascunhoEnvio.setCpfCnpj(cpfNoveDigitos.substring(0, 9));
        this.incluirRascunhoEnvio.setControle(cpfNoveDigitos.substring(9, 11));
        this.incluirRascunhoEnvio.setEmailParticipante(getDadosTelaEtapa1().getEmail().trim());
        this.incluirRascunhoEnvio.setSexo(getDadosTelaEtapa1().getSexo());
        this.incluirRascunhoEnvio.setDtNascimento((this.editaCpf) ? adicionaPontoData(getDadosTelaEtapa1().getDtNascimento())
                        : incluirPontoData(getDadosTelaEtapa1().getDtNascimento()));
        this.incluirRascunhoEnvio.setNmParticipante(getDadosTelaEtapa1().getNome().trim());

        this.incluirRascunhoEnvio.setEstadoCivil(getDadosTelaEtapa1().getEstadoCivil().toString());

        if (!"S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.incluirRascunhoEnvio.setPais(getDadosTelaEtapa1().getPaisNascimento().toString());
            this.incluirRascunhoEnvio.setOcupacao(getDadosTelaEtapa1().getProfissao().toString());
            String x = getDadosTelaEtapa1().getRenda().replace(".", "");
            x = x.replace("R$", "");
            x = adicionaDecimal(x).replace(",", ".");
            this.incluirRascunhoEnvio.setVlrRenda(x.trim());
        }

        String cepSemMascara = retiraMascara(getDadosTelaEtapa1().getCep());
        this.incluirRascunhoEnvio.setCep(cepSemMascara.substring(0, 5));
        this.incluirRascunhoEnvio.setComplemento(cepSemMascara.substring(5));
        this.incluirRascunhoEnvio.setLogradouro(getDadosTelaEtapa1().getLogradouro().trim());
        this.incluirRascunhoEnvio.setNrEndereco(getDadosTelaEtapa1().getNumEndereco().trim());
        
        if (null != getDadosTelaEtapa1().getComplemento() && !getDadosTelaEtapa1().getComplemento().trim().isEmpty())
        {
            this.incluirRascunhoEnvio.setComplementoParticipante(getDadosTelaEtapa1().getComplemento());
        }
        
        this.incluirRascunhoEnvio.setBairro(getDadosTelaEtapa1().getBairro());
        this.incluirRascunhoEnvio.setMunicipio(getDadosTelaEtapa1().getCidade());
        this.incluirRascunhoEnvio.setUfParticipante(getDadosTelaEtapa1().getEstado());
        this.incluirRascunhoEnvio.setVlrBoleto(this.ofertaPlanoEnvio.getValorContribuicao().toString());
        this.incluirRascunhoEnvio.setBeneficios(new ArrayList<BeneficioIncluirRascunhoEnvioVo>());

        BeneficioIncluirRascunhoEnvioVo objetoBeneficio = new BeneficioIncluirRascunhoEnvioVo();
        objetoBeneficio.setBeneficioPrev(this.selecionado.getCodBeneficio().toString());
        objetoBeneficio.setItemBloco(1);
        objetoBeneficio.setTpCobertura("0");
        objetoBeneficio.setTpRecebimento(("S".equalsIgnoreCase(this.selecionado.getIndJovem())) ? this.prazoRecebimento : "0");
        objetoBeneficio.setVlrContribuicao(this.ofertaPlanoEnvio.getValorContribuicao().toString());
        objetoBeneficio.setVlrRisco( "0" );
        objetoBeneficio.setQtdeBeneficiario(this.listaBeneficiarios.size());

        this.incluirRascunhoEnvio.getBeneficios().add(objetoBeneficio);
        int listaBeneficiarios = 0;

        this.incluirRascunhoEnvio.getBeneficios().get(0).setBeneficiarios(new ArrayList<ListaBeneficiariosIncluirRascunhoEnvioVo>());

        for (Beneficiario lista : this.listaBeneficiarios)
        {
            ListaBeneficiariosIncluirRascunhoEnvioVo obj = new ListaBeneficiariosIncluirRascunhoEnvioVo();
            obj.setNrBeneficiario(listaBeneficiarios);
            obj.setBeneficiario(lista.getNomeCompleto());
            obj.setDtNascimentoBeneficiario(adicionaPontoData(lista.getDataNascimento()));
            obj.setSexoBeneficiario(lista.getSexo());
            obj.setGrau( "" + lista.getGrauParentesco() );
            obj.setPartBeneficiario("" + lista.getPorcentagem() + "00");
            this.incluirRascunhoEnvio.getBeneficios().get(0).getBeneficiarios().add(obj);
            listaBeneficiarios++;

        }
        this.incluirRascunhoEnvio.setFatcas(new ArrayList<ListaFatcasVo>());

        if (this.respondeuPergunta1.equals(1))
        {
            ListaFatcasVo obj = populaFatca(1, this.codPais1, this.docPais1, listaPerguntasFatca1);

            this.incluirRascunhoEnvio.getFatcas().add(obj);
        }

        if (this.respondeuPergunta2.equals(1))
        {
            ListaFatcasVo obj = populaFatca(2, this.codPais2, this.docPais2, listaPerguntasFatca2);

            this.incluirRascunhoEnvio.getFatcas().add(obj);
        }

        if (this.respondeuPergunta3.equals(1))
        {
            ListaFatcasVo obj = populaFatca(3, this.codPais3, this.docPais3, listaPerguntasFatca3);

            this.incluirRascunhoEnvio.getFatcas().add(obj);
        }

        this.incluirRascunhoEnvio.setTelefones(new ArrayList<TelefoneVo>());

        TelefoneVo tel1 = new TelefoneVo();
        TelefoneVo tel2 = new TelefoneVo();
        TelefoneVo tel3 = new TelefoneVo();
//        tel1.setCodArea(
//                (null != this.detalhaParticipante.getPrimeiroDdd()) ? this.detalhaParticipante.getPrimeiroDdd() : "0");
//        tel1.setNrTelefone((null != this.detalhaParticipante.getPrimeiroNrTelefone())
//                ? Integer.toString(this.detalhaParticipante.getPrimeiroNrTelefone()) : "0");
//        tel1.setTpTelefone("1");
//        tel1.setNrRamal( (null != this.detalhaParticipante.getRamal()) ? Integer.toString(this.detalhaParticipante.getRamal()) : "0");
       
        if (getDadosTelaEtapa1().getTelResidencial().isEmpty()){
	        tel1.setCodArea( "0" );
	        tel1.setNrTelefone( "0" );
	        tel1.setTpTelefone( "1" );
	        tel1.setNrRamal("0" );  

        }else {
            String telsemMascara = retiraMascara(getDadosTelaEtapa1().getTelResidencial());
	        tel1.setCodArea( "00" + telsemMascara.substring(0, 2) );
	        tel1.setNrTelefone( telsemMascara.substring(2) );
	        tel1.setTpTelefone( "1" );
	        tel1.setNrRamal("0" );
        }
        this.incluirRascunhoEnvio.getTelefones().add(tel1);

//        tel2.setCodArea( (null != this.detalhaParticipante.getSegundoDdd()) ? this.detalhaParticipante.getSegundoDdd().trim() : "0");
//        tel2.setNrTelefone((null != this.detalhaParticipante.getSegundoNrTelefone()) ? Integer.toString(this.detalhaParticipante.getSegundoNrTelefone()) : "0");
//        tel2.setTpTelefone("2");
//        tel2.setNrRamal("0");
       
        
        if (getDadosTelaEtapa1().getTelComercial().isEmpty()){
        	tel2.setCodArea( "0" );
        	tel2.setNrTelefone( "0" );
        	tel2.setTpTelefone( "2" );
        	tel2.setNrRamal("0" );
        }else {
            String telsemMascara = retiraMascara(getDadosTelaEtapa1().getTelComercial());
            tel2.setCodArea( "00" + telsemMascara.substring(0, 2) );
            tel2.setNrTelefone( telsemMascara.substring(2) );
            tel2.setTpTelefone( "2" );
            tel2.setNrRamal("0" );
        }
    	this.incluirRascunhoEnvio.getTelefones().add(tel2);
        
        String telsemMascara = retiraMascara(getDadosTelaEtapa1().getTel());
        tel3.setCodArea(telsemMascara.substring(0, 2));
        tel3.setNrTelefone(telsemMascara.substring(2));
        tel3.setNrRamal("0");
        tel3.setTpTelefone("3");
        this.incluirRascunhoEnvio.getTelefones().add(tel3);

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.incluirRascunhoEnvio.setPais(getPatrocinador().getPaisNascimento().toString());
            this.incluirRascunhoEnvio.setNmParticipante(getPatrocinador().getNome());
            this.incluirRascunhoEnvio.setDtNascimento(adicionaPontoData(getPatrocinador().getDtNascimento()));
            this.incluirRascunhoEnvio.setSexo(getPatrocinador().getSexo());
            this.incluirRascunhoEnvio.setEstadoCivil(getPatrocinador().getEstadoCivil().toString());

            String cpfNoveDigitosPatrocinador = retiraMascara(getPatrocinador().getCpf());
            this.incluirRascunhoEnvio.setControle(cpfNoveDigitosPatrocinador.substring(9, 11));
            this.incluirRascunhoEnvio.setCpfCnpj(cpfNoveDigitosPatrocinador.substring(0, 9));

            String valor = getPatrocinador().getRendaMensal().replace(".", "");
            valor = adicionaDecimal(valor).replace(",", ".");
            valor = valor.replace("R$", "");
            this.incluirRascunhoEnvio.setVlrRenda(valor.trim());

            this.incluirRascunhoEnvio.setOcupacao(getPatrocinador().getProfissao().toString());

            String telsemMascaraPatrocinador = retiraMascara(getPatrocinador().getTel());
            String telResidencialsemMascaraPatrocinador = retiraMascara(getPatrocinador().getTelResidencial());
            String telComercialsemMascaraPatrocinador = retiraMascara(getPatrocinador().getTelComercial());

            if (telResidencialsemMascaraPatrocinador.isEmpty()){
                this.incluirRascunhoEnvio.getTelefones().get(0).setCodArea("0");
                this.incluirRascunhoEnvio.getTelefones().get(0).setNrTelefone("0");            	
            }else {
                this.incluirRascunhoEnvio.getTelefones().get(0).setCodArea(telResidencialsemMascaraPatrocinador.substring(0, 2));
                this.incluirRascunhoEnvio.getTelefones().get(0).setNrTelefone(telResidencialsemMascaraPatrocinador.substring(2));       	
            }
            
            if (telComercialsemMascaraPatrocinador.isEmpty()){
                this.incluirRascunhoEnvio.getTelefones().get(1).setCodArea("0");
                this.incluirRascunhoEnvio.getTelefones().get(1).setNrTelefone("0");
            }else {
                this.incluirRascunhoEnvio.getTelefones().get(1).setCodArea(telComercialsemMascaraPatrocinador.substring(0, 2));
                this.incluirRascunhoEnvio.getTelefones().get(1).setNrTelefone(telComercialsemMascaraPatrocinador.substring(2));
            }

            if (telsemMascaraPatrocinador.isEmpty()){
	            this.incluirRascunhoEnvio.getTelefones().get(2).setCodArea("0");
	            this.incluirRascunhoEnvio.getTelefones().get(2).setNrTelefone("0");
            }else {
	            this.incluirRascunhoEnvio.getTelefones().get(2).setCodArea(telsemMascaraPatrocinador.substring(0, 2));
	            this.incluirRascunhoEnvio.getTelefones().get(2).setNrTelefone(telsemMascaraPatrocinador.substring(2));            	
            }
            
            this.incluirRascunhoEnvio.setEmailParticipante(getPatrocinador().getEmail().trim());

            String cepSemMascaraPatrocinador = retiraMascara(getPatrocinador().getCep());
            this.incluirRascunhoEnvio.setCep(cepSemMascaraPatrocinador.substring(0, 5));
            this.incluirRascunhoEnvio.setComplemento(cepSemMascaraPatrocinador.substring(5));

            this.incluirRascunhoEnvio.setLogradouro(getPatrocinador().getLogradouro().trim());
            this.incluirRascunhoEnvio.setNrEndereco(getPatrocinador().getNumeroEnd().trim());

            if (null != getPatrocinador().getComplementoEnd()
                    && !getPatrocinador().getComplementoEnd().trim().isEmpty())
            {
                this.incluirRascunhoEnvio.setComplementoParticipante(getPatrocinador().getComplementoEnd());
            }
            
            this.incluirRascunhoEnvio.setBairro(getPatrocinador().getBairro());
            this.incluirRascunhoEnvio.setMunicipio(getPatrocinador().getCidade());
            this.incluirRascunhoEnvio.setUfParticipante(getPatrocinador().getEstado());

            this.incluirRascunhoEnvio.setCodParticipanteJovem(getPatrocinador().getGrauParentesco().toString());
            this.incluirRascunhoEnvio.setIdentDepdt(getPatrocinador().getDependenteIr().toString());

            this.incluirRascunhoEnvio.setCliente(getDadosTelaEtapa1().getNome());
            this.incluirRascunhoEnvio
                    .setDtNascimentoJ((this.editaCpf) ? adicionaPontoData(getDadosTelaEtapa1().getDtNascimento())
                            : incluirPontoData(getDadosTelaEtapa1().getDtNascimento()));
            this.incluirRascunhoEnvio.setSexoJ(getDadosTelaEtapa1().getSexo());
            this.incluirRascunhoEnvio.setEstadoCivilJ(getDadosTelaEtapa1().getEstadoCivil().toString());

            String cpfNoveDigitosJovem = retiraMascara(getDadosTelaEtapa1().getCpf());
            this.incluirRascunhoEnvio.setControleJ(cpfNoveDigitosJovem.substring(9, 11));
            this.incluirRascunhoEnvio.setCpfCnpjJ(cpfNoveDigitosJovem.substring(0, 9));

            this.incluirRascunhoEnvio.setTelefonesJovem(new ArrayList<TelefoneVo>());

            String telResidencialsemMascaraJovem = retiraMascara(getDadosTelaEtapa1().getTelResidencial());
            String telComercialsemMascaraJovem = retiraMascara(getDadosTelaEtapa1().getTelComercial());
            String telsemMascaraJovem = retiraMascara(getDadosTelaEtapa1().getTel());
            TelefoneVo telResidencial = new TelefoneVo();
            TelefoneVo telComercial = new TelefoneVo();
            TelefoneVo telCelular = new TelefoneVo();
            
            if (telResidencialsemMascaraJovem.isEmpty()){
                telResidencial.setCodArea("0");
                telResidencial.setNrTelefone("0");
                telResidencial.setNrRamal("0");
                telResidencial.setTpTelefone("1");      	
            }else {
                telResidencial.setCodArea(telResidencialsemMascaraJovem.substring(0, 2));
                telResidencial.setNrTelefone(telResidencialsemMascaraJovem.substring(2));
                telResidencial.setNrRamal("0");
                telResidencial.setTpTelefone("1");
            }

            if (telComercialsemMascaraJovem.isEmpty()) {
                telComercial.setCodArea("0");
                telComercial.setNrTelefone("0");
                telComercial.setNrRamal("0");
                telComercial.setTpTelefone("2");
            	
            }else {
                telComercial.setCodArea(telComercialsemMascaraJovem.substring(0, 2));
                telComercial.setNrTelefone(telComercialsemMascaraJovem.substring(2));
                telComercial.setNrRamal("0");
                telComercial.setTpTelefone("2");
            }

            if (telsemMascaraJovem.isEmpty()) {
                telCelular.setCodArea("0");
                telCelular.setNrTelefone("0");
                telCelular.setNrRamal("0");
                telCelular.setTpTelefone("3");       	
            }else {
                telCelular.setCodArea(telsemMascaraJovem.substring(0, 2));
                telCelular.setNrTelefone(telsemMascaraJovem.substring(2));
                telCelular.setNrRamal("0");
                telCelular.setTpTelefone("3");
            }

            this.incluirRascunhoEnvio.getTelefonesJovem().add(telResidencial);
            this.incluirRascunhoEnvio.getTelefonesJovem().add(telComercial);
            this.incluirRascunhoEnvio.getTelefonesJovem().add(telCelular);

            this.incluirRascunhoEnvio.setEmail(getDadosTelaEtapa1().getEmail().trim());

            String cepSemMascaraJovem = retiraMascara(getDadosTelaEtapa1().getCep());
            this.incluirRascunhoEnvio.setCepJ(cepSemMascaraJovem.substring(0, 5));
            this.incluirRascunhoEnvio.setComplementoJ(cepSemMascaraJovem.substring(5));

            this.incluirRascunhoEnvio.setLogradouroJ(getDadosTelaEtapa1().getLogradouro().trim());
            this.incluirRascunhoEnvio.setNrLogradouroJ(getDadosTelaEtapa1().getNumEndereco().trim());

            if (null != getDadosTelaEtapa1().getComplemento()
                    && !getDadosTelaEtapa1().getComplemento().trim().isEmpty())
            {
                this.incluirRascunhoEnvio.setComplementoParticipanteJ(getDadosTelaEtapa1().getComplemento());
            }
            
            this.incluirRascunhoEnvio.setBairroJ(getDadosTelaEtapa1().getBairro());
            this.incluirRascunhoEnvio.setMunicipioJ(getDadosTelaEtapa1().getCidade());
            this.incluirRascunhoEnvio.setUfJ(getDadosTelaEtapa1().getEstado());
        }
    }

    public void popularValidarInscricao()
    {
        this.validaPropostaInscricaoEnvio.setCodAgente(this.incluirRascunhoEnvio.getCodAge());

        if (null != this.incluirRascunhoEnvio.getAgenciaDebito()
                && !this.incluirRascunhoEnvio.getAgenciaDebito().trim().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setAgencia(this.incluirRascunhoEnvio.getAgenciaDebito());
        }
        
        if (this.estruturaComercial.equals("mercado")) 
        {
            this.validaPropostaInscricaoEnvio.setCodSucursal(getCodSucursal());
        }

        this.validaPropostaInscricaoEnvio.setCep(this.incluirRascunhoEnvio.getCep());
        this.validaPropostaInscricaoEnvio.setCodAgenciaVenda(this.incluirRascunhoEnvio.getAgencia());
        this.validaPropostaInscricaoEnvio.setCodBanco("237");
        this.validaPropostaInscricaoEnvio.setCodBancoVenda("237");
        this.validaPropostaInscricaoEnvio.setCodConvenio("884");
        this.validaPropostaInscricaoEnvio.setCodFinalidade("1");
        this.validaPropostaInscricaoEnvio.setCodIdentProduto(this.selecionado.getCodProdutoComercial());
        this.validaPropostaInscricaoEnvio.setCodIdentTipoDoc("0");
        this.validaPropostaInscricaoEnvio.setCodIndCargoPublico(this.incluirRascunhoEnvio.getCodIdentificacaoExerc());
        this.validaPropostaInscricaoEnvio.setCodIndTributacao(this.ofertaPlanoEnvio.getRegimeTributario().toString());
        this.validaPropostaInscricaoEnvio.setCodIndicie("0");
        this.validaPropostaInscricaoEnvio.setCodModalidade(Integer.toString(this.ofertaPlanoEnvio.getModalidade()));
        this.validaPropostaInscricaoEnvio.setCodPais(this.incluirRascunhoEnvio.getPais());
        this.validaPropostaInscricaoEnvio.setCodProdutoNegociado(this.selecionado.getCodProdutoComercial());
        this.validaPropostaInscricaoEnvio.setCodRecurso("0");
        this.validaPropostaInscricaoEnvio.setCodRegistro("0");
        this.validaPropostaInscricaoEnvio.setCodTipoProduto(Integer.toString(this.ofertaPlanoEnvio.getTipoContribuicao()));
        this.validaPropostaInscricaoEnvio.setComplemento(this.incluirRascunhoEnvio.getComplemento());
        this.validaPropostaInscricaoEnvio.setCodRelacionamento("2");

        String cpfNoveDigitos = retiraMascara(getDadosTelaEtapa1().getCpf());

        if (this.pep.equals("S"))
        {
            this.validaPropostaInscricaoEnvio.setCodIndCargoPublico("1");
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(this.objetoPep.getOrigemRecurso().toString());
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjEmpregador(cpfNoveDigitos.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjEmpregador(cpfNoveDigitos.substring(0, 9));
            this.validaPropostaInscricaoEnvio.setDsCargo(this.objetoPep.getCargo());
            this.validaPropostaInscricaoEnvio.setDtFim(adicionaPontoData(this.objetoPep.getDataFinal()));
            this.validaPropostaInscricaoEnvio.setDtInicio(adicionaPontoData(this.objetoPep.getDataInicial()));
            this.validaPropostaInscricaoEnvio.setOrgaoContratante(this.objetoPep.getEmpresa());
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjEmpregador(new String());
            this.validaPropostaInscricaoEnvio.setCpfCnpjEmpregador(new String());
            this.validaPropostaInscricaoEnvio.setDsCargo(new String());
            this.validaPropostaInscricaoEnvio.setDtFim(new String());
            this.validaPropostaInscricaoEnvio.setDtInicio(new String());
            this.validaPropostaInscricaoEnvio.setOrgaoContratante(new String());
        }

        if (this.relacionamentoAgPublico.equals("S"))
        {
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(this.objetoPep.getOrigemRecurso().toString());
            this.validaPropostaInscricaoEnvio.setCodRelacionamento("1");
            String cpfAgente = retiraMascara(this.objetoPep.getAgenteCpf()).trim();
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjAg(cpfAgente.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjAgente(cpfAgente.substring(0, 9));
            this.validaPropostaInscricaoEnvio.setDsCargoAgente(this.objetoPep.getAgenteCargo());
            this.validaPropostaInscricaoEnvio.setDsRelacionamentoAgente(this.objetoPep.getTipoRelacionamento());
            this.validaPropostaInscricaoEnvio.setNmAgente(this.objetoPep.getAgenteNome());
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjAg(new String());
            this.validaPropostaInscricaoEnvio.setCpfCnpjAgente(new String());
            this.validaPropostaInscricaoEnvio.setDsCargoAgente(new String());
            this.validaPropostaInscricaoEnvio.setDsRelacionamentoAgente(new String());
            this.validaPropostaInscricaoEnvio.setNmAgente(new String());
        }

        if (!"S".equalsIgnoreCase(this.relacionamentoAgPublico) && !"S".equalsIgnoreCase(this.pep))
        {
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(new String());
        }

        if (null != this.objetoPep.getOrigemRecurso() && this.objetoPep.getOrigemRecurso().equals(99))
        {
            this.validaPropostaInscricaoEnvio.setObservacaoOrigem(this.objetoPep.getObs());
        }
        
        this.validaPropostaInscricaoEnvio.setControleCpfCnpj(cpfNoveDigitos.substring(9, 11));
        this.validaPropostaInscricaoEnvio.setCpfCnpjParticipante(cpfNoveDigitos.substring(0, 9));
        this.validaPropostaInscricaoEnvio.setDtAgendamento(this.incluirRascunhoEnvio.getDtAgendamento());
        this.validaPropostaInscricaoEnvio.setDtConcessao(this.incluirRascunhoEnvio.getConcsBenef());
        this.validaPropostaInscricaoEnvio.setDtInicioBeneficio("01.01.0001");
        this.validaPropostaInscricaoEnvio.setDtInicioPlano("01.01.0001");
        this.validaPropostaInscricaoEnvio.setDtNascimento((editaCpf) ? adicionaPontoData(getDadosTelaEtapa1().getDtNascimento())
                        : incluirPontoData(getDadosTelaEtapa1().getDtNascimento()));
        this.validaPropostaInscricaoEnvio.setDtVenda(this.incluirRascunhoEnvio.getDtVenda());
        this.validaPropostaInscricaoEnvio.setEmail(this.incluirRascunhoEnvio.getEmailParticipante());
        this.validaPropostaInscricaoEnvio.setEstadoCivil(this.incluirRascunhoEnvio.getEstadoCivil());
        this.validaPropostaInscricaoEnvio.setFormaEnvio("3");
        this.validaPropostaInscricaoEnvio.setIndAgendamento("N");

        if (!this.incluirRascunhoEnvio.getFatcas().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setFatcas(this.incluirRascunhoEnvio.getFatcas());
        }

        this.validaPropostaInscricaoEnvio.setIndNascionalidadeFatca(this.respondeuPergunta1.toString());
        this.validaPropostaInscricaoEnvio.setIndResidenciaFatca(this.respondeuPergunta2.toString());
        this.validaPropostaInscricaoEnvio.setIndVistoFatca(this.respondeuPergunta3.toString());

        Integer qntRespostaSim = 0;

        if (this.respondeuPergunta1.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta2.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta3.equals(1))
        {
            qntRespostaSim++;
        }

        this.validaPropostaInscricaoEnvio.setQtdeFatca(qntRespostaSim.toString());

        this.validaPropostaInscricaoEnvio.setNome(this.incluirRascunhoEnvio.getNmParticipante());

        if (null != this.incluirRascunhoEnvio.getConta() && !this.incluirRascunhoEnvio.getConta().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setNrConta(this.incluirRascunhoEnvio.getConta());
        }

        this.validaPropostaInscricaoEnvio.setNumeroProposta(Integer.toString(this.incluirRascunhoRetorno.getCodProposta()));
        this.validaPropostaInscricaoEnvio.setPlanoPagamento(Integer.toString(this.selecionado.getCodPlano()));
        this.validaPropostaInscricaoEnvio.setSerie(Integer.toString(this.incluirRascunhoRetorno.getSerieProposta()));
        this.validaPropostaInscricaoEnvio.setSexo(this.incluirRascunhoEnvio.getSexo());
        this.validaPropostaInscricaoEnvio.setTaxaCarregamento("0");

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1"))
        {
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1"))
            {
                this.validaPropostaInscricaoEnvio.setTpConta("1");
            } 
            else
            {
                this.validaPropostaInscricaoEnvio.setTpConta("2");
            }
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setTpConta("3");
        }

        this.validaPropostaInscricaoEnvio.setVlrPatrimonio(this.incluirRascunhoEnvio.getVlrLiquido());

        if (!"S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.validaPropostaInscricaoEnvio.setVlrRendaMensal(this.incluirRascunhoEnvio.getVlrRenda());
            this.validaPropostaInscricaoEnvio.setCodOcupacao(this.incluirRascunhoEnvio.getOcupacao());
        }

        this.validaPropostaInscricaoEnvio
                .setVlrTotalContrib(this.incluirRascunhoEnvio.getBeneficios().get(0).getVlrContribuicao());
        this.validaPropostaInscricaoEnvio.setQtdeBeneficio("1");
        this.validaPropostaInscricaoEnvio.setQtdeFatca(this.incluirRascunhoEnvio.getQuantidade());
        this.validaPropostaInscricaoEnvio.setBeneficios(new ArrayList<BeneficioValidarPropostaInscricaoVo>());
        BeneficioValidarPropostaInscricaoVo objetoBeneficio = new BeneficioValidarPropostaInscricaoVo();

        objetoBeneficio.setNrBeneficio("1");
        objetoBeneficio.setCodBeneficio(this.incluirRascunhoEnvio.getBeneficios().get(0).getBeneficioPrev());
        objetoBeneficio.setTpCobertura(("S".equalsIgnoreCase(this.selecionado.getIndJovem())) ? this.prazoRecebimento : "0");
        objetoBeneficio.setTpRecRisco("0");
        objetoBeneficio.setVlrContribRisco(this.incluirRascunhoEnvio.getBeneficios().get(0).getVlrContribuicao());
        objetoBeneficio.setVlrRendaBenRisco("0");

        Integer qtdeBeneficiarios = this.listaBeneficiarios.size();
        objetoBeneficio.setQtdeBeneficiario(qtdeBeneficiarios.toString());

        this.validaPropostaInscricaoEnvio.getBeneficios().add(objetoBeneficio);

        for (ListaBeneficiariosIncluirRascunhoEnvioVo lista : this.incluirRascunhoEnvio.getBeneficios().get(0).getBeneficiarios())
        {
            ListaBeneficiariosValidarPropostaInscricaoVo obj = new ListaBeneficiariosValidarPropostaInscricaoVo();
            obj.setNrBeneficiario(lista.getNrBeneficiario().toString());
            obj.setNmBeneficiario(lista.getBeneficiario());
            obj.setSexoBeneficiario(lista.getSexoBeneficiario());
            obj.setDtNasimentoBeneficiario(lista.getDtNascimentoBeneficiario());
            obj.setGrauParentescoBeneficiario(lista.getGrau());
            obj.setPercentualBeneficiario(lista.getPartBeneficiario());

            this.validaPropostaInscricaoEnvio.getBeneficios().get(0).getBeneficiarios().add(obj);
        }

        this.validaPropostaInscricaoEnvio.setTelefones(new ArrayList<TelefoneVo>());
        this.validaPropostaInscricaoEnvio.setTelefones(this.incluirRascunhoEnvio.getTelefones());

        if ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.validaPropostaInscricaoEnvio.setNome(getPatrocinador().getNome());
            this.validaPropostaInscricaoEnvio.setDtNascimento(adicionaPontoData(getPatrocinador().getDtNascimento()));
            this.validaPropostaInscricaoEnvio.setSexo(getPatrocinador().getSexo());
            this.validaPropostaInscricaoEnvio.setEstadoCivil(getPatrocinador().getEstadoCivil().toString());

            String cpfNoveDigitosPatrocinador = retiraMascara(getPatrocinador().getCpf());
            this.validaPropostaInscricaoEnvio.setControleCpfCnpj(cpfNoveDigitosPatrocinador.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjParticipante(cpfNoveDigitosPatrocinador.substring(0, 9));

            String x = getPatrocinador().getRendaMensal().replace(".", "");
            x = adicionaDecimal(x).replace(",", ".");
            x = x.replace("R$", "");
            this.validaPropostaInscricaoEnvio.setVlrRendaMensal(x.trim());

            this.validaPropostaInscricaoEnvio.setCodOcupacao(getPatrocinador().getProfissao().toString());

            String telsemMascaraPatrocinador = retiraMascara(getPatrocinador().getTel());

            this.validaPropostaInscricaoEnvio.getTelefones().get(2).setCodArea(telsemMascaraPatrocinador.substring(0, 2));
            this.validaPropostaInscricaoEnvio.getTelefones().get(2).setNrTelefone(telsemMascaraPatrocinador.substring(2));
            this.validaPropostaInscricaoEnvio.setEmail(getPatrocinador().getEmail().trim());

            String cepSemMascaraPatrocinador = retiraMascara(getPatrocinador().getCep());
            this.validaPropostaInscricaoEnvio.setCep(cepSemMascaraPatrocinador.substring(0, 5));
            this.validaPropostaInscricaoEnvio.setComplemento(cepSemMascaraPatrocinador.substring(5));

            this.validaPropostaInscricaoEnvio.setCodGrauParentesco(getPatrocinador().getGrauParentesco().toString());
            this.validaPropostaInscricaoEnvio.setCodIdentDependencia(getPatrocinador().getDependenteIr().toString());

            this.validaPropostaInscricaoEnvio.setNomeJovem(getDadosTelaEtapa1().getNome());
            this.validaPropostaInscricaoEnvio.setDtNascimentoJovem(this.incluirRascunhoEnvio.getDtNascimentoJ());
            this.validaPropostaInscricaoEnvio.setSexoJovem(getDadosTelaEtapa1().getSexo());
            this.validaPropostaInscricaoEnvio.setEstadoCivilJovem(getDadosTelaEtapa1().getEstadoCivil().toString());

            String cpfNoveDigitosJovem = retiraMascara(getDadosTelaEtapa1().getCpf());
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjJovemPatrocinado(cpfNoveDigitosJovem.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjJovem(cpfNoveDigitosJovem.substring(0, 9));

            this.validaPropostaInscricaoEnvio.setTelefonesJovem(new ArrayList<TelefoneVo>());

            this.validaPropostaInscricaoEnvio.setTelefonesJovem(this.incluirRascunhoEnvio.getTelefonesJovem());

            this.validaPropostaInscricaoEnvio.setEmailJovem(getDadosTelaEtapa1().getEmail().trim());

            String cepSemMascaraJovem = retiraMascara(getDadosTelaEtapa1().getCep());
            this.validaPropostaInscricaoEnvio.setCepJovem(cepSemMascaraJovem.substring(0, 5));
            this.validaPropostaInscricaoEnvio.setComplementoJovem(cepSemMascaraJovem.substring(5));
        }
    }

    public ListaFatcasVo populaFatca(Integer numeroPergunta, Integer codPais, String doc, Integer[] listaPerguntas)
    {
        ListaFatcasVo obj = new ListaFatcasVo();
        obj.setIdentSituacao(numeroPergunta);
        obj.setCodPaisEstrangeiro(codPais);
        obj.setCodDocFiscal(doc);
        obj.setCodEmissaoFiscal(2);
        obj.setCodIndicadorObr(2);
        obj.setCodIndicadorDis(2);
        obj.setCodIndicadorRenuncia(2);
        obj.setCodDocRenuncia(2);

        for (Integer lista : listaPerguntas)
        {
            if (lista.equals(1))
            {
                obj.setCodEmissaoFiscal(1);
            }
            if (lista.equals(2))
            {
                obj.setCodIndicadorObr(1);
            }
            if (lista.equals(3))
            {
                obj.setCodIndicadorDis(1);
            }
            if (lista.equals(4))
            {
                obj.setCodIndicadorRenuncia(1);
            }
            if (lista.equals(5))
            {
                obj.setCodDocRenuncia(1);
            }

        }
        return obj;
    }

    public void recuperarDadosParceiroNegocio()
    {

        if ( mockSimularPuma )
        {
            // para teste local cpf 34263917839
            this.setTpoCorretor("4");
            identCorretor = "43638022000194";
        }
        
        if (!identCorretor.isEmpty() && this.tpoCorretor.equals("4"))
        {
            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|DADOS DO CORRETOR LOGADO:";
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|CPF/CNPJ CORRETOR " + identCorretor;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|IDENTIFICAÇÃO CORRETOR - TPO :" + this.tpoCorretor;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            String requestSoap;
            requestSoap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://webservice.cadastro.sipn.com.br/\">\n"
                    + "<soapenv:Header/>\n" + "<soapenv:Body>\n" + "<web:recuperarParceiroNegocioAtivoRequest>\n"
                    + "<cpfcnpj>" + identCorretor + "</cpfcnpj>\n" + "</web:recuperarParceiroNegocioAtivoRequest>\n"
                    + "</soapenv:Body>\n" + "</soapenv:Envelope>";
            String urlProvider = ConstantesServicos.URL_DADOS_PARCEIRO_NEGOCIO_SOAP;
            String endPoint = ConstantesServicos.ENDPOINT_DADOS_PARCEIRO_NEGOCIO;

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|Request|Obter Sucursal|atraves do CPF/CNPJ logado:" + identCorretor;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            final String xmlString = consumoApiRequisicaoXML.serviceXml(requestSoap, urlProvider, endPoint);
            
            if ( 0 == xmlString.length() )
            {
                opComercialTO = null;
                setAutoPreenchimentoCorretor(false);
                setDescricaoSucursal(new String());
                setCodCorretorTela(new String());
                setCodCorretor(new String());
                return;
            }
            
            JAXBElement<ObterParceiroNegocioAtivoResponseVo> retornoSoup = consumoApiRequisicaoXML.unmarshal(xmlString);

            opComercialTO = retornoSoup.getValue().getOpComercialTO();

            if (null != opComercialTO && !opComercialTO.isEmpty())
            {
                setAutoPreenchimentoCorretor(true);

                int ind = 0;

                for (OpComercialTO itemLista : opComercialTO)
                {
                    Integer converter = Integer.parseInt(itemLista.getCodSucursal());
                    opComercialTO.get(ind).setCodSucursal(converter.toString());
                    ind++;
                }

                if (opComercialTO.size() == 1)
                {
                    setCodCorretorTela(opComercialTO.get(0).getCodBVP());
                    setCodCorretor(opComercialTO.get(0).getCodBVP());
                    setDescricaoSucursal(opComercialTO.get(0).getDescricaoSucursal());
                }
            } 
            else
            {
                setAutoPreenchimentoCorretor(false);
                setDescricaoSucursal(new String());
                setCodCorretorTela(new String());
                setCodCorretor(new String());
            }

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|Obter Sucursal-atraves do CPF/CNPJ logado";
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
        else
        {
            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|CPF/CNPJ E OU TIPO CORRETOR INVALIDO";
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|CPF/CNPJ CORRETOR " + identCorretor;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|IDENTIFICAÇÃO CORRETOR - TPO :" + this.tpoCorretor;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            setAutoPreenchimentoCorretor(false);
            setCodCorretorTela(new String());
            setDescricaoSucursal(new String());
            setCodCorretor(new String());
        }
    }

    public HttpResponse gerarPdf()
    {
        this.erro = "ContrataPrev|recuperarDadosParceiroNegocio|gerarPdf";
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        final String html = this.montarPdf();
        final String arquivoPDF = "/tmp/pdf-" + this.detalhaParticipante.getCpf() + "-"
                + this.incluirRascunhoRetorno.getCodProposta() + "-" + this.incluirRascunhoRetorno.getSerieProposta();
        File iconeBrradesco = null;
        InputStream iconeBradescoIn = null;
        OutputStream iconeBradescoOut = null;
        OutputStream tmpPdf = null;

        try
        {
            /**
             * gravar imagem do pdf
             */
            iconeBrradesco = new File("/tmp/bradesco-vida.png");
            iconeBradescoIn = this.getClass().getClassLoader().getResourceAsStream("/resources/imagens/bradesco-vida.png");

            try
            {
                iconeBradescoOut = new FileOutputStream(iconeBrradesco);
                IOUtils.copy(iconeBradescoIn, iconeBradescoOut);
            } 
            catch ( FileNotFoundException e)
            {
                this.erro = "ContrataPrev|gerarPdf|Exception|Erro ao gerar PDF: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            } 
            finally
            {
                if ( null != iconeBradescoOut)
                {
                    try { iconeBradescoOut.close(); } catch( IOException e ) { this.erro = "erro"; }
                }
                
                if ( null != iconeBradescoIn )
                {
                    try { iconeBradescoIn.close(); } catch( IOException e ) { this.erro = "erro"; }
                }
            }

            /**
             * gerar o pdf
             */
            Document doc = new Document();
            try
            {
                tmpPdf = new FileOutputStream(arquivoPDF);
                PdfWriter writer = PdfWriter.getInstance((com.lowagie.text.Document) doc, tmpPdf);
                doc.open();
                HTMLWorker htmlparser = new HTMLWorker(doc);
                Reader targetReader = new StringReader(html);
                htmlparser.parse(targetReader);
                doc.close();
                writer.close();
            } 
            catch ( FileNotFoundException e)
            {
                this.erro = "ContrataPrev|gerarPdf|Exception|Erro ao gerar PDF1: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            } 
            finally
            {
                if ( null != tmpPdf )
                {
                    try { tmpPdf.close(); } catch( IOException e ) { this.erro = "erro"; }
                }
            }
            /**
             * salvar o arquivo local
             */
            final File pdf = new File(arquivoPDF);
            final byte[] encoded = FileUtils.readFileToByteArray(pdf);
            this.downloadPDF = "data:application/pdf;base64,";
            this.downloadPDF += Base64.getEncoder().encodeToString(encoded);
        } 
        catch ( IOException e)
        {
            this.erro = "ContrataPrev|gerarPdf|Exception|Erro ao gerar PDF2: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
        catch ( DocumentException e )
        {
            this.erro = "ContrataPrev|gerarPdf|Exception|Erro ao gerar PDF2: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
        finally
        {
            if ( null != iconeBradescoIn )
            {
                try { iconeBradescoIn.close(); } catch( IOException e ) { this.erro = "erro"; }
            }
            if ( null != iconeBradescoOut)
            {
                try { iconeBradescoOut.close(); } catch( IOException e ) { this.erro = "erro"; }
            }
            if ( null != tmpPdf )
            {
                try { tmpPdf.close(); } catch( IOException e ) { this.erro = "erro"; }
            }
        }
        return null;
    }

    public HttpResponse gerarPdfRisco()
    {
        this.erro = "ContrataPrev|gerarPdfRisco|recuperarDadosParceiroNegocio|gerarPdf";
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        final String html = this.montarPdfRisco();
        final String arquivoPDF = "/tmp/pdf-" + this.detalhaParticipante.getCpf() + "-"
                + this.incluirRascunhoRetorno.getCodProposta() + "-" + this.incluirRascunhoRetorno.getSerieProposta();
        File iconeBrradesco = null;
        InputStream iconeBradescoIn = null;
        OutputStream iconeBradescoOut = null;
        OutputStream tmpPdf = null;

        try
        {
            /**
             * gravar imagem do pdf
             */
            iconeBrradesco = new File("/tmp/bradesco-vida.png");
            Object obj = this.getClass();

            if ( null != obj )
            {
                iconeBradescoIn = this.getClass().getClassLoader().getResourceAsStream("/resources/imagens/bradesco-vida.png");
                
                if ( iconeBradescoIn == null )
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
            
            try
            {
                iconeBradescoOut = new FileOutputStream(iconeBrradesco);
                IOUtils.copy(iconeBradescoIn, iconeBradescoOut);
            } 
            catch ( IOException e)
            {
                this.erro = "ContrataPrev|gerarPdfRisco|Exception|Erro ao gerar PDF: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            } 
            finally
            {
                if (iconeBradescoOut != null)
                {
                    try { iconeBradescoOut.close(); } catch( IOException e ) { this.erro = "erro"; }
                }
            }

            /**
             * gerar o pdf
             */
            Document doc = new Document();
            try
            {
                tmpPdf = new FileOutputStream(arquivoPDF);
                PdfWriter writer = PdfWriter.getInstance((com.lowagie.text.Document) doc, tmpPdf);
                doc.open();
                HTMLWorker htmlparser = new HTMLWorker(doc);
                Reader targetReader = new StringReader(html);
                htmlparser.parse(targetReader);
                doc.close();
                writer.close();
            } 
            catch ( FileNotFoundException e)
            {
                this.erro = "ContrataPrev|gerarPdfRisco|Exception|Erro ao gerar PDF1: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            } 
            catch( DocumentException e )
            {
                this.erro = "ContrataPrev|gerarPdfRisco|Exception|Erro ao gerar PDF1: "  +
                        CleanPath.cleanString( e.getMessage() ); 
                     CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );
            } 
            finally
            {
                if (tmpPdf != null)
                {
                    try { tmpPdf.close(); } catch( IOException e ) { this.erro = "close 1"; }
                }
            }
            /**
             * salvar o arquivo local
             */
            final File pdf = new File(arquivoPDF);
            final byte[] encoded = FileUtils.readFileToByteArray(pdf);
            this.downloadPDF = "data:application/pdf;base64,";
            this.downloadPDF += Base64.getEncoder().encodeToString(encoded);
        } 
        catch ( IOException e)
        {
            this.erro = "ContrataPrev|gerarPdfRisco|Exception|Erro ao gerar PDF2: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        } 
        finally
        {
            if (iconeBradescoIn != null)
            {
                try { iconeBradescoIn.close(); } catch( IOException e ) { this.erro = "close 1"; }
            }
            if (iconeBradescoOut != null)
            {
                try { iconeBradescoOut.close(); } catch( IOException e ) { this.erro = "close 2"; }
            }
            if (tmpPdf != null)
            {
                try { tmpPdf.close(); } catch( IOException e ) { this.erro = "close 3"; }
            }
        }
        return null;
    }

    private String montarPdf()
    {
        String nomeParticipante = ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
                ? this.incluirRascunhoEnvio.getCliente()
                : this.incluirRascunhoEnvio.getNmParticipante();

        final String html = ""
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
                + "<html lang=\"pt_BR\" xmlns=\"http://www.w3.org/1999/xhtml\">" + "<body>\r\n"
                + "    <br/><br/><br/><p style=\"line-height: 3.0;\"></p>\r\n"
                + "    <img src=\"/tmp/bradesco-vida.png\" width=\"123\" height=\"92\" />"
                + "    <div class=\"BradescoSans\">\r\n" + "    <br/><br/>\r\n" + "    <p>Ol&aacute;, <strong>"
                + converteEncodingTexto(nomeParticipante) + "</strong>\r\n" + "    <br/>\r\n"
                + "    <p>Voc&ecirc; tem proposta de inscri&ccedil;&atilde;o de previd&ecirc;ncia privada pendente de aprova&ccedil;&atilde;o.</p>\r\n"
                + "    <p>Cliente correntista Bradesco pode realizar o aceite digital na &aacute;rea de Previd&ecirc;ncia do APP ou do Internet Banking.</p><br/>\r\n"
                + "	   <p>___________________________________________________________________________</p><br/>\r\n"
                + "    <p>E para cliente n&atilde;o-correntista o aceite digital pode ser realizado atrav&eacute;s do site <a href=\\\"http://www.bradesco.com.br\\\">www.bradesco.com.br</a> > Produtos e Servi&ccedil;os > Previd&ecirc;ncia Privada > Aceite digital propostas</p>\r\n" + "    <br/>\r\n"
                + "    <p><strong>N&uacute;mero da Proposta: </strong>" + this.incluirRascunhoRetorno.getSerieProposta()
                + "/" + this.incluirRascunhoRetorno.getCodProposta() + "</p>\r\n" + "    <p><strong>Plano: </strong>"
                + converteEncodingTexto(this.selecionado.getNmFundoAbreviado()) + "</p>\r\n"
                + "    <p><strong>Regime Tribut&aacute;rio: </strong>"
                + converteEncodingTexto(getTributacao(this.selecionado.getRegimeTributario())) + "</p>\r\n"
                + "    <p><strong>Tipo de Contribui&ccedil;&atilde;o: </strong>"
                + getUnicoOuMensal(this.ofertaPlanoEnvio.getTipoContribuicao()) + "</p>\r\n"
                + "    <p><strong>Valor da Contribui&ccedil;&atilde;o: </strong> R$ " + this.valorContribuicao
                + "</p>\r\n" + "    <p><strong>Forma de Pagamento: </strong>"
                + getNomeFormaPagamento(this.incluirRascunhoEnvio.getFormaPagamento()) + "</p>\r\n"
                + "    <br/><br/>\r\n"
                + "    <strong>ESTA PROPOSTA TER&Aacute; VALIDADE SOMENTE AP&Oacute;S A CONFIRMA&Ccedil;&Atilde;O DO PAGAMENTO.</strong>\r\n"
                + "    </div>\r\n" + "</body>\r\n" + "</html>\r\n" + "";
        return html;
    }

    private String montarPdfRisco()
    {
        String nomeParticipante = ("S".equalsIgnoreCase(this.selecionado.getIndJovem()))
                ? this.incluirRascunhoEnvio.getCliente()
                : this.incluirRascunhoEnvio.getNmParticipante();

        final String html = ""
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
                + "<html lang=\"pt_BR\" xmlns=\"http://www.w3.org/1999/xhtml\">" + "<body>\r\n"
                + "    <br/><br/><br/><p style=\"line-height: 3.0;\"></p>\r\n"
                + "    <img src=\"/tmp/bradesco-vida.png\" width=\"123\" height=\"92\" />"
                + "    <div class=\"BradescoSans\">\r\n" + "    <br/><br/>\r\n" + "    <p>Ol&aacute;, <strong>"
                + converteEncodingTexto(nomeParticipante) + "</strong>\r\n" + "    <br/>\r\n"
                + "    <p>Voc&ecirc; tem proposta de inscri&ccedil;&atilde;o de previd&ecirc;ncia privada pendente de aprova&ccedil;&atilde;o.</p>\r\n"
                + "    <p>Cliente correntista Bradesco pode realizar o aceite digital na &aacute;rea de Previd&ecirc;ncia do APP ou do Internet Banking.</p><br/>\r\n"
				+ "    <p>___________________________________________________________________________</p><br/>\r\n"
                + "    <p>E para cliente n&atilde;o-correntista o aceite digital pode ser realizado atrav&eacute;s do site <a href=\\\"http://www.bradesco.com.br\\\">www.bradesco.com.br</a> > Produtos e Servi&ccedil;os > Previd&ecirc;ncia Privada > Aceite digital propostas</p>\r\n" + "    <br/>\r\n"
                + "    <p><strong>N&uacute;mero da Proposta: </strong>" + this.incluirRascunhoRetorno.getSerieProposta()
                + "/" + this.incluirRascunhoRetorno.getCodProposta() + "</p>\r\n" + "    <p><strong>Plano: </strong>"
                + converteEncodingTexto( this.quadroResumoRisco.getNomePlano() ) + "</p>\r\n"
//                + "    <p><strong>Regime Tribut&aacute;rio: </strong>"
//                + converteEncodingTexto(getTributacao( Integer.valueOf( this.parametroSelecao.getRegimeTributario() ) ) )+ "</p>\r\n"
                + "    <p><strong>Tipo de Contribui&ccedil;&atilde;o: </strong>"
                + getUnicoOuMensal(this.ofertaPlanoEnvio.getTipoContribuicao()) + "</p>\r\n"
                + "    <p><strong>Valor da Contribui&ccedil;&atilde;o: </strong> R$ " + this.quadroResumoRisco.getTotalMensal()
                + "</p>\r\n" + "    <p><strong>Forma de Pagamento: </strong>"
                + getNomeFormaPagamento(this.incluirRascunhoEnvio.getFormaPagamento()) + "</p>\r\n"
                + "    <br/><br/>\r\n"
                + "    <strong>ESTA PROPOSTA TER&Aacute; VALIDADE SOMENTE AP&Oacute;S A CONFIRMA&Ccedil;&Atilde;O DO PAGAMENTO.</strong>\r\n"
                + "    </div>\r\n" + "</body>\r\n" + "</html>\r\n" + "";
        return html;
    }

    public String incluirZero(String numero, int digitos)
    {
        if (numero.length() == digitos)
        {
            return numero;
        } 
        else
        {
            for (int i = digitos - numero.length(); i > 0; i--)
            {
                numero = "0" + numero;
            }
        }
        return numero;
    }

    public String incluirPontoData(String x)
    {
        String dia = x.substring(0, 2);
        String mes = x.substring(2, 4);
        String ano = x.substring(4, 8);
        x = dia + "." + mes + "." + ano;
        return x;
    }

    public String trocarPonto(Double numero)
    {
        String numeroConvertido = numero.toString();
        numeroConvertido = numeroConvertido.replace(".", ",");
        return adicionaDecimal(numeroConvertido);
    }

    public String trocarPontoString(String numero)
    {
        String numeroConvertido = numero.replace(".", ",");
        return adicionaDecimal(numeroConvertido);
    }

    public String adicionaDecimal(String numero)
    {
        if (numero.contains(","))
        {
            String[] x = numero.split(",");
            if (x.length == 2)
            {
                if (x[1].length() == 2)
                {
                    return numero;
                }
                return numero + "0";
            } 
            else
            {
                return numero + "00";
            }
        } 
        else
        {
            return numero + ",00";
        }
    }

    public String adicionaZeroEsquerda(Long numero, Integer dig)
    {
        String numeroConvertido = numero.toString();
        if (numeroConvertido.length() >= dig)
        {
            return numeroConvertido;
        }

        for (; numeroConvertido.length() < dig;)
        {
            numeroConvertido = "0" + numeroConvertido;
        }

        return numeroConvertido;
    }

    public boolean telValido(String x)
    {
        if (x.substring(0, 2).equalsIgnoreCase("00") || !x.substring(2, 3).equalsIgnoreCase("9")
                || x.substring(3, 4).equalsIgnoreCase("0"))
        {
            return false;
        }
        return true;
    }

    public LinkedHashMap<String, Integer> getModalidades()
    {
        return ModalidadePrevidencia.getAll();
    }

    public LinkedHashMap<String, Integer> getListaEstadoCivil()
    {
        return EstadoCivil.getAll();
    }

    public LinkedHashMap<String, Integer> getPeguntasFatca()
    {
        return PerguntasFatca.getAll();
    }

    public LinkedHashMap<String, Integer> getProfissoes()
    {
        return Profissao.getAll();
    }

    public LinkedHashMap<String, Integer> getPaises()
    {
        return PaisOrigem.getAll();
    }

    public LinkedHashMap<String, Integer> getOrigemRecursos()
    {
        return OrigemRecurso.getAll();
    }

    public LinkedHashMap<String, Integer> getParentescoaBeneficiario()
    {
        if (this.dadosTelaEtapa1.getEstadoCivil() == EstadoCivil.CASADO.getValor())
        {

             return ParentescoBeneficiario.getAll(true);
        }

        return ParentescoBeneficiario.getAll(false);
    }

    public LinkedHashMap<String, Integer> getParentescoBeneficiarioRisco()
    {
        if (this.getTelaRiscoEtapa1().getEstadoCivil() == EstadoCivil.CASADO.getValor())
        {
            return ParentescoBeneficiarioRisco.getAll( true );
        }

        return ParentescoBeneficiarioRisco.getAll( false );
    }

    public LinkedHashMap<String, Integer> getParentescoPatrocinador()
    {
        return ParentescoPatrocinador.getAll();
    }

    public String retiraMascara(String x)
    {
        x = x.replaceAll("[^0-9]", "");
        return x;
    }

    public String adicionaPontoData(String data)
    {
        return data.replace("/", ".");
    }

    public String doVoltarPaginaInicial()
    {
        setMsgGenerica(false);
        return "index.xhtml";
    }

    public static boolean isDataValida(String strDate)
    {
        String dateFormat = "ddMMuuuu";

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)
                .withResolverStyle(ResolverStyle.STRICT);
        try
        {
            LocalDate date;
            
            date = LocalDate.parse(strDate, dateTimeFormatter);
            
            if ( null == date )
            {
                return false;
            }
            
            return true;
        } 
        catch (DateTimeParseException e)
        {
            final String rc = "ContrataPrev|isDataValida|DateTimeParseException: " +
                    CleanPath.cleanString( e.getMessage() );
            LoggerFactory.getLogger().log(Level.INFO, rc );
            return false;
        }
    }

    public boolean isCpfValido(String cpf)
    {
        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (cpf.equals("00000000000") || cpf.equals("11111111111") || cpf.equals("22222222222")
                || cpf.equals("33333333333") || cpf.equals("44444444444") || cpf.equals("55555555555")
                || cpf.equals("66666666666") || cpf.equals("77777777777") || cpf.equals("88888888888")
                || cpf.equals("99999999999") || (cpf.length() != 11))
            return (false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try
        {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++)
            {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (cpf.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else
                dig10 = (char) (r + 48); // converte no respectivo caractere numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++)
            {
                num = (int) (cpf.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else
                dig11 = (char) (r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            if ((dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10)))
                return (true);
            else
                return (false);
        } 
        catch (InputMismatchException erro)
        {
            return (false);
        }
    }

    public boolean isEmailValido(String email)
    {
        final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        final boolean emailErr = Pattern.compile(regexPattern).matcher(email).matches();

        return emailErr;
    }

    public String converteEncodingTexto(String texto)
    {
        String output = "";
        try
        {
            /* De ISO-8859-1 para UTF-8 */
            output = new String(texto.getBytes("ISO-8859-1"), "UTF-8");
        } 
        catch ( UnsupportedEncodingException e)
        {
            this.erro = "ContrataPrev|converteEncodingTexto|Exception|convertendo encoding iso para utf-8: " + 
                    "|ENTRADA: " + texto;
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );
        }
        return output;
    }

    public String buscaCep(AjaxBehaviorEvent event)
    {
        this.erroTela.setCep(false);
        habilitarInput();

        this.setConsisteCep(dadosTelaEtapa1.getCep());

        if (getConsisteCep().replace("-", "").length() == 8)
        {
            dadosTelaEtapa1.setNumEndereco("");
            cepVO = service.buscarCep(dadosTelaEtapa1.getCep());

            if ( null == cepVO || !cepVO.isCepEncontrado())
            {
                this.erroTela.setCep( true );
                this.dadosTelaEtapa1.setLogradouro( "" );
                this.dadosTelaEtapa1.setCidade( "" );
                this.dadosTelaEtapa1.setBairro( "" );
                this.dadosTelaEtapa1.setEstado( "" );
            } 
            else
            {
                this.erroTela.setCep( false );
                this.setBloquearInputEndereco( campoValido( cepVO.getLogdrCompleto() ) );
                dadosTelaEtapa1.setLogradouro( null != cepVO.getLogdrCompleto() && cepVO.getLogdrCompleto().length() > 40 
                        ? cepVO.getLogdrCompleto().substring( 0, 40 ) 
                        : cepVO.getLogdrCompleto() );

                this.setBloquearInputCidade(campoValido(cepVO.getCidade()));
                dadosTelaEtapa1.setCidade(null != cepVO.getCidade() && cepVO.getCidade().length() > 30
                        ? cepVO.getCidade().substring(0, 30)
                        : cepVO.getCidade());

                this.setBloquearInputBairro(campoValido(cepVO.getBairro()));
                dadosTelaEtapa1.setBairro(null != cepVO.getBairro() && cepVO.getBairro().length() > 20
                        ? cepVO.getBairro().substring(0, 20)
                        : cepVO.getBairro());

                this.setBloquearInputUF(campoValido(cepVO.getUf()));
                dadosTelaEtapa1
                        .setEstado(null != cepVO.getUf() && cepVO.getUf().length() > 2 ? cepVO.getUf().substring(0, 2)
                                : cepVO.getUf());
            }
        } 
        else
        {
            this.erroTela.setCep(true);
        }

        return "propostaEtapa1.xhtml";
    }

    public String buscaCepPatrocinador(AjaxBehaviorEvent event)
    {
        this.erroTela.setCep(false);
        habilitarInputPatrocinador();
        patrocinador.setNumeroEnd("");

        this.setConsisteCep(patrocinador.getCep());

        if (getConsisteCep().replace("-", "").length() == 8)
        {
            cepVO = service.buscarCep(patrocinador.getCep());

            if (null == cepVO || !cepVO.isCepEncontrado())
            {
                this.erroTela.setCep(true);
                patrocinador.setLogradouro( "" );
                patrocinador.setCidade( "" );
                patrocinador.setBairro( "" );
                patrocinador.setEstado( "" );
            } 
            else
            {
                this.erroTela.setCep(false);
                this.setBloquearInputEnderecoPatrocinador( campoValido( cepVO.getLogdrCompleto() ) );
                this.setBloquearInputCidadePatrocinador( campoValido( cepVO.getCidade() ) );
                this.setBloquearInputBairroPatrocinador( campoValido( cepVO.getBairro() ) );
                this.setBloquearInputUFPatrocinador( campoValido( cepVO.getBairro() ) );

                patrocinador.setLogradouro( null != cepVO.getLogdrCompleto() && cepVO.getLogdrCompleto().length() > 40 
                        ? cepVO.getLogdrCompleto().substring( 0, 40 )
                        : cepVO.getLogdrCompleto() );
                patrocinador.setCidade( null != cepVO.getCidade() && cepVO.getCidade().length() > 30 
                        ? cepVO.getCidade().substring( 0, 30 )
                        : cepVO.getCidade() );
                patrocinador.setBairro( null != cepVO.getBairro() && cepVO.getBairro().length() > 20 
                        ? cepVO.getBairro().substring( 0, 20 )
                        : cepVO.getBairro() );
                patrocinador.setEstado( null != cepVO.getUf() && cepVO.getUf().length() > 2 
                        ? cepVO.getUf().substring( 0, 2 )
                        : cepVO.getUf() );
            }
        } 
        else
        {
            this.erroTela.setCep(true);
        }

        return "";
    }

    public String getCpf()
    {
        return cpf;
    }

    public void setCpf(String cpf)
    {
        if ( this.cpf.equals( cpf ) )
        {
            return;
        }
        
        this.cpf = cpf;
        this.telaOfertaRisco.setPrimeiraChamada( true );
        this.telaOfertaRisco.setRevalidar( true );
    }

    public String getDataNascimento()
    {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento)
    {
        this.dataNascimento = dataNascimento;
        this.parametroSelecao.setIdadeParticipante( this.utils.obterIdade( this.dataNascimento ) );
    }

    public String getSexo()
    {
        return sexo;
    }

    public void setSexo(String sexo)
    {
        this.sexo = sexo;
    }

    public DetalhaParticipanteRetornoVo getDetalhaParticipante()
    {
        return detalhaParticipante;
    }

    public void setDetalhaParticipante(DetalhaParticipanteRetornoVo detalhaParticipante)
    {
        this.detalhaParticipante = detalhaParticipante;
    }

    public boolean isMsgGenerica()
    {
        return msgGenerica;
    }

    public void setMsgGenerica(boolean msgGenerica)
    {
        this.msgGenerica = msgGenerica;
    }

    public OfertaPlanoEnvioVo getOfertaPlanoEnvio()
    {
        return this.ofertaPlanoEnvio;
    }

    public void setOfertaPlanoEnvio(OfertaPlanoEnvioVo ofertaPlanoEnvio)
    {
        this.ofertaPlanoEnvio = ofertaPlanoEnvio;
    }

    public OfertaPlanoRetornoVo getOfertaPlanoRetorno()
    {
        return ofertaPlanoRetorno;
    }

    public void setOfertaPlanoRetorno(OfertaPlanoRetornoVo ofertaPlanoRetorno)
    {
        this.ofertaPlanoRetorno = ofertaPlanoRetorno;
    }

    public String getMsgErro()
    {
        return msgErro;
    }

    public void setMsgErro(String msgErro)
    {
        this.msgErro = msgErro;
    }

    public ListaPlanosVo getSelecionado()
    {
        return selecionado;
    }

    public void setSelecionado(ListaPlanosVo selecionado)
    {
        this.selecionado = selecionado;
    }

    public IncluirRascunhoEnvioVo getIncluirRascunhoEnvio()
    {
        return incluirRascunhoEnvio;
    }

    public void setIncluirRascunhoEnvio(IncluirRascunhoEnvioVo incluirRascunhoEnvio)
    {
        this.incluirRascunhoEnvio = incluirRascunhoEnvio;
    }

    public ValidaPropostaInscricaoEnvioVo getValidaPropostaInscricaoEnvio()
    {
        return validaPropostaInscricaoEnvio;
    }

    public void setValidaPropostaInscricaoEnvio(ValidaPropostaInscricaoEnvioVo validaPropostaInscricaoEnvio)
    {
        this.validaPropostaInscricaoEnvio = validaPropostaInscricaoEnvio;
    }

    public IncluirRascunhoRetornoVo getIncluirRascunhoRetorno()
    {
        return incluirRascunhoRetorno;
    }

    public void setIncluirRascunhoRetorno(IncluirRascunhoRetornoVo incluirRascunhoRetorno)
    {
        this.incluirRascunhoRetorno = incluirRascunhoRetorno;
    }

    public boolean isExibeModalDetalheOferta()
    {
        return exibeModalDetalheOferta;
    }

    public void setExibeModalDetalheOferta(boolean exibeModalDetalheOferta)
    {
        this.exibeModalDetalheOferta = exibeModalDetalheOferta;
    }

    public ValidaPropostaInscricaoRetornoVo getValidaPropostaInscricaoRetorno()
    {
        return validaPropostaInscricaoRetorno;
    }

    public void setValidaPropostaInscricaoRetorno(ValidaPropostaInscricaoRetornoVo validaPropostaInscricaoRetorno)
    {
        this.validaPropostaInscricaoRetorno = validaPropostaInscricaoRetorno;
    }

    public String getCodCorretor()
    {
        return codCorretor.trim();
    }

    public void setCodCorretor(String codCorretor)
    {
        this.codCorretor = codCorretor;
    }

    public String getDddCel()
    {
        return dddCel;
    }

    public void setDddCel(String dddCel)
    {
        this.dddCel = dddCel;
    }

    public String getNrCel()
    {
        return nrCel;
    }

    public void setNrCel(String nrCel)
    {
        this.nrCel = nrCel;
    }

    public ErroTela getErroTela()
    {
        return erroTela;
    }

    public void setErroTela(ErroTela erroTela)
    {
        this.erroTela = erroTela;
    }

    public boolean isFlagExplicacaoPlanos()
    {
        return flagExplicacaoPlanos;
    }

    public void setFlagExplicacaoPlanos(boolean flagExplicacaoPlanos)
    {
        this.flagExplicacaoPlanos = flagExplicacaoPlanos;
    }

    public List<ListaPlanosVo> getListaPlanosExibicao()
    {
        return listaPlanosExibicao;
    }

    public void setListaPlanosExibicao(List<ListaPlanosVo> listaPlanosExibicao)
    {
        this.listaPlanosExibicao = listaPlanosExibicao;
    }

    public Integer[] getListaPerfil()
    {
        return listaPerfil;
    }

    public void setListaPerfil(Integer[] listaPerfil)
    {
        this.listaPerfil = listaPerfil;
    }

    public DetalhaFundoRetornoVo getDetalhaFundoRetorno()
    {
        return detalhaFundoRetorno;
    }

    public void setDetalhaFundoRetorno(DetalhaFundoRetornoVo detalhaFundoRetorno)
    {
        this.detalhaFundoRetorno = detalhaFundoRetorno;
    }

    public String getRentabilidade3()
    {
        return rentabilidade3;
    }

    public void setRentabilidade3(String rentabilidade3)
    {
        this.rentabilidade3 = rentabilidade3;
    }

    public String getRentabilidade6()
    {
        return rentabilidade6;
    }

    public void setRentabilidade6(String rentabilidade6)
    {
        this.rentabilidade6 = rentabilidade6;
    }

    public String getRentabilidade12()
    {
        return rentabilidade12;
    }

    public void setRentabilidade12(String rentabilidade12)
    {
        this.rentabilidade12 = rentabilidade12;
    }

    public String getPaginaRecebidaDetalhes()
    {
        return paginaRecebidaDetalhes;
    }

    public void setPaginaRecebidaDetalhes(String paginaRecebidaDetalhes)
    {
        this.paginaRecebidaDetalhes = paginaRecebidaDetalhes;
    }

    public DadosTelaEtapa1Vo getDadosTelaEtapa1()
    {
        return dadosTelaEtapa1;
    }

    public void setDadosTelaEtapa1(DadosTelaEtapa1Vo dadosTelaEtapa1)
    {
        this.dadosTelaEtapa1 = dadosTelaEtapa1;
    }

    public boolean isExibePep()
    {
        return exibePep;
    }

    public void setExibePep(boolean exibePep)
    {
        this.exibePep = exibePep;
    }

    public String getPep()
    {
        return pep;
    }

    public void setPep(String pep)
    {
        this.pep = pep;
    }

    public String getRelacionamentoAgPublico()
    {
        return relacionamentoAgPublico;
    }

    public void setRelacionamentoAgPublico(String relacionamentoAgPublico)
    {
        this.relacionamentoAgPublico = relacionamentoAgPublico;
    }

    public boolean isExibeRelacionamentoAgPublico()
    {
        return exibeRelacionamentoAgPublico;
    }

    public void setExibeRelacionamentoAgPublico(boolean exibeRelacionamentoAgPublico)
    {
        this.exibeRelacionamentoAgPublico = exibeRelacionamentoAgPublico;
    }

    public String getNacionalidadeAlemBrasileira()
    {
        return nacionalidadeAlemBrasileira;
    }

    public void setNacionalidadeAlemBrasileira(String nacionalidadeAlemBrasileira)
    {
        this.nacionalidadeAlemBrasileira = nacionalidadeAlemBrasileira;
    }

    public boolean isExibeNacionalidadeAlemBrasileira()
    {
        return exibeNacionalidadeAlemBrasileira;
    }

    public void setExibeNacionalidadeAlemBrasileira(boolean exibeNacionalidadeAlemBrasileira)
    {
        this.exibeNacionalidadeAlemBrasileira = exibeNacionalidadeAlemBrasileira;
    }

    public boolean isExibeResidenciaForaBrasil()
    {
        return exibeResidenciaForaBrasil;
    }

    public void setExibeResidenciaForaBrasil(boolean exibeResidenciaForaBrasil)
    {
        this.exibeResidenciaForaBrasil = exibeResidenciaForaBrasil;
    }

    public String getResidenciaForaBrasil()
    {
        return residenciaForaBrasil;
    }

    public void setResidenciaForaBrasil(String residenciaForaBrasil)
    {
        this.residenciaForaBrasil = residenciaForaBrasil;
    }

    public boolean isExibeVistoOutroPais()
    {
        return exibeVistoOutroPais;
    }

    public void setExibeVistoOutroPais(boolean exibeVistoOutroPais)
    {
        this.exibeVistoOutroPais = exibeVistoOutroPais;
    }

    public String getVistoOutroPais()
    {
        return vistoOutroPais;
    }

    public void setVistoOutroPais(String vistoOutroPais)
    {
        this.vistoOutroPais = vistoOutroPais;
    }

    public Integer getCodPais1()
    {
        return codPais1;
    }

    public void setCodPais1(Integer codPais1)
    {
        this.codPais1 = codPais1;
    }

    public Integer getCodPais2()
    {
        return codPais2;
    }

    public void setCodPais2(Integer codPais2)
    {
        this.codPais2 = codPais2;
    }

    public Integer getCodPais3()
    {
        return codPais3;
    }

    public void setCodPais3(Integer codPais3)
    {
        this.codPais3 = codPais3;
    }

    public Pep getObjetoPep()
    {
        return objetoPep;
    }

    public void setObjetoPep(Pep objetoPep)
    {
        this.objetoPep = objetoPep;
    }

    public Integer getRespondeuPergunta1()
    {
        return respondeuPergunta1;
    }

    public void setRespondeuPergunta1(Integer respondeuPergunta1)
    {
        this.respondeuPergunta1 = respondeuPergunta1;
    }

    public Integer getRespondeuPergunta2()
    {
        return respondeuPergunta2;
    }

    public void setRespondeuPergunta2(Integer respondeuPergunta2)
    {
        this.respondeuPergunta2 = respondeuPergunta2;
    }

    public Integer getRespondeuPergunta3()
    {
        return respondeuPergunta3;
    }

    public void setRespondeuPergunta3(Integer respondeuPergunta3)
    {
        this.respondeuPergunta3 = respondeuPergunta3;
    }

    public String getDocPais1()
    {
        return docPais1;
    }

    public void setDocPais1(String docPais1)
    {
        this.docPais1 = docPais1;
    }

    public String getDocPais2()
    {
        return docPais2;
    }

    public void setDocPais2(String docPais2)
    {
        this.docPais2 = docPais2;
    }

    public String getDocPais3()
    {
        return docPais3;
    }

    public void setDocPais3(String docPais3)
    {
        this.docPais3 = docPais3;
    }

    public Integer[] getListaPerguntasFatca1()
    {
        return listaPerguntasFatca1;
    }

    public void setListaPerguntasFatca1(Integer[] listaPerguntasFatca1)
    {
        this.listaPerguntasFatca1 = listaPerguntasFatca1;
    }

    public Integer[] getListaPerguntasFatca2()
    {
        return listaPerguntasFatca2;
    }

    public void setListaPerguntasFatca2(Integer[] listaPerguntasFatca2)
    {
        this.listaPerguntasFatca2 = listaPerguntasFatca2;
    }

    public Integer[] getListaPerguntasFatca3()
    {
        return listaPerguntasFatca3;
    }

    public void setListaPerguntasFatca3(Integer[] listaPerguntasFatca3)
    {
        this.listaPerguntasFatca3 = listaPerguntasFatca3;
    }

    public boolean isExibirDadosContaBancaria()
    {
        return exibirDadosContaBancaria;
    }

    public void setExibirDadosContaBancaria(boolean exibirDadosContaBancaria)
    {
        this.exibirDadosContaBancaria = exibirDadosContaBancaria;
    }

    public Integer getIdadeAposentadoria()
    {
        return idadeAposentadoria;
    }

    public void setIdadeAposentadoria(Integer idadeAposentadoria)
    {
        this.idadeAposentadoria = idadeAposentadoria;
    }

    public String getGerenteComercial()
    {
        return gerenteComercial;
    }

    public void setGerenteComercial(String gerenteComercial)
    {
        this.gerenteComercial = gerenteComercial;
    }

    public Beneficiario getAdicaoBeneficiario()
    {
        return adicaoBeneficiario;
    }

    public void setAdicaoBeneficiario(Beneficiario adicaoBeneficiario)
    {
        this.adicaoBeneficiario = adicaoBeneficiario;
    }

    public List<Beneficiario> getListaBeneficiarios()
    {
        return listaBeneficiarios;
    }

    public void setListaBeneficiarios(List<Beneficiario> listaBeneficiarios)
    {
        this.listaBeneficiarios = listaBeneficiarios;
    }

    public Integer getSomaPorcentagem()
    {
        return somaPorcentagem;
    }

    public void setSomaPorcentagem(Integer somaPorcentagem)
    {
        this.somaPorcentagem = somaPorcentagem;
    }

    public String getValorContribuicao()
    {
        return valorContribuicao;
    }

    public void setValorContribuicao(String valorContribuicao)
    {
        this.valorContribuicao = valorContribuicao;
    }

    public String getDownloadPDF()
    {
        return downloadPDF;
    }

    public Patrocinador getPatrocinador()
    {
        return patrocinador;
    }

    public void setPatrocinador(Patrocinador patrocinador)
    {
        this.patrocinador = patrocinador;
    }

    public boolean isEditaSexo()
    {
        return editaSexo;
    }

    public void setEditaSexo(boolean editaSexo)
    {
        this.editaSexo = editaSexo;
    }

    public boolean isEditaDtNascimento()
    {
        return editaDtNascimento;
    }

    public void setEditaDtNascimento(boolean editaDtNascimento)
    {
        this.editaDtNascimento = editaDtNascimento;
    }

    public boolean isEditaCpf()
    {
        return editaCpf;
    }

    public void setEditaCpf(boolean editaCpf)
    {
        this.editaCpf = editaCpf;
    }

    public boolean isEditaPep()
    {
        return editaPep;
    }

    public void setEditaPep(boolean editaPep)
    {
        this.editaPep = editaPep;
    }

    public boolean isEditaAgentePep()
    {
        return editaAgentePep;
    }

    public void setEditaAgentePep(boolean editaAgentePep)
    {
        this.editaAgentePep = editaAgentePep;
    }

    public boolean isExibeModalConfirmacaoPlano()
    {
        return exibeModalConfirmacaoPlano;
    }

    public void setExibeModalConfirmacaoPlano(boolean exibeModalConfirmacaoPlano)
    {
        this.exibeModalConfirmacaoPlano = exibeModalConfirmacaoPlano;
    }

    public String getPrazoRecebimento()
    {
        return prazoRecebimento;
    }

    public void setPrazoRecebimento(String prazoRecebimento)
    {
        this.prazoRecebimento = prazoRecebimento;
    }

    public boolean isPlanoJovem()
    {
        return planoJovem;
    }

    public void setPlanoJovem(boolean planoJovem)
    {
        this.planoJovem = planoJovem;
    }

    public boolean getMostrarPlanoJovem()
    {
        return mostrarPlanoJovem;
    }

    public void setMostrarPlanoJovem(boolean mostrarPlanoJovem)
    {
        this.mostrarPlanoJovem = mostrarPlanoJovem;
    }

    public CepVo getCepVO()
    {
        return cepVO;
    }

    public void setCepVO(CepVo cepVO)
    {
        this.cepVO = cepVO;
    }

    public String getMensagemValidacaoConta()
    {
        return mensagemValidacaoConta;
    }

    public void setMensagemValidacaoConta(String mensagemValidacaoConta)
    {
        this.mensagemValidacaoConta = mensagemValidacaoConta;
    }

    public String getOfertarPlanosJovem()
    {
        return ofertarPlanosJovem;
    }

    public void setOfertarPlanosJovem(String ofertarPlanosJovem)
    {
        this.ofertarPlanosJovem = ofertarPlanosJovem;
    }

    public boolean isExibePerguntaPlanoJovem()
    {
        return exibePerguntaPlanoJovem;
    }

    public void setExibePerguntaPlanoJovem(boolean exibePerguntaPlanoJovem)
    {
        this.exibePerguntaPlanoJovem = exibePerguntaPlanoJovem;
    }

    public boolean isErroCep()
    {
        return erroCep;
    }

    public void setErroCep(boolean erroCep)
    {
        this.erroCep = erroCep;
    }

    public String getPerfilClienteTelaOferta()
    {
        return perfilClienteTelaOferta;
    }

    public void setPerfilClienteTelaOferta(String perfilClienteTelaOferta)
    {
        this.perfilClienteTelaOferta = perfilClienteTelaOferta;
    }

    public String filtroOferta()
    {
        this.filtroOferta = true;
        doAvancarParaOfertas();

        this.filtroOferta = false;
        return "oferta.xhtml";

    }

    public boolean isFiltroOferta()
    {
        return filtroOferta;
    }

    public void setFiltroOferta(boolean filtroOferta)
    {
        this.filtroOferta = filtroOferta;
    }

    public String getConsisteCep()
    {
        return consisteCep;
    }

    public void setConsisteCep(String consisteCep)
    {
        this.consisteCep = consisteCep;
    }

    public boolean isValidaCamposIndex()
    {
        return validaCamposIndex;
    }

    public void setValidaCamposIndex(boolean validaCamposIndex)
    {
        this.validaCamposIndex = validaCamposIndex;
    }

    public boolean campoValido(String valorCampo)
    {
        boolean bloquearCampo;

        if (null == valorCampo || "".equals(valorCampo))
        {
            bloquearCampo = false;
        } 
        else
        {
            bloquearCampo = true;
        }

        return bloquearCampo;
    }

    public boolean isBloquearInputEndereco()
    {
        return bloquearInputEndereco;
    }

    public boolean getCssBotaoDesabilitado()
    {
        return this.cssBotaoDesabilitado;
    }

    public void setCssBotaoDesabilitado( boolean cssBotaoDesabilitado)
    {
        this.cssBotaoDesabilitado = cssBotaoDesabilitado;
    }

    public void setBloquearInputEndereco(boolean bloquearInputEndereco)
    {
        this.bloquearInputEndereco = bloquearInputEndereco;
    }

    public boolean isBloquearInputBairro()
    {
        return bloquearInputBairro;
    }

    public void setBloquearInputBairro(boolean bloquearInputBairro)
    {
        this.bloquearInputBairro = bloquearInputBairro;
    }

    public boolean isBloquearInputCidade()
    {
        return bloquearInputCidade;
    }

    public void setBloquearInputCidade(boolean bloquearInputCidade)
    {
        this.bloquearInputCidade = bloquearInputCidade;
    }

    public boolean isBloquearInputUF()
    {
        return bloquearInputUF;
    }

    public void setBloquearInputUF(boolean bloquearInputUF)
    {
        this.bloquearInputUF = bloquearInputUF;
    }

    public void habilitarInput()
    {
        this.setBloquearInputEndereco(false);
        this.setBloquearInputBairro(false);
        this.setBloquearInputCidade(false);
        this.setBloquearInputUF(false);
    }

    public void habilitarInputPatrocinador()
    {
        this.setBloquearInputEnderecoPatrocinador(false);
        this.setBloquearInputBairroPatrocinador(false);
        this.setBloquearInputCidadePatrocinador(false);
        this.setBloquearInputUFPatrocinador(false);
    }

    public boolean isBloquearInputEnderecoPatrocinador()
    {
        return bloquearInputEnderecoPatrocinador;
    }

    public void setBloquearInputEnderecoPatrocinador(boolean bloquearInputEnderecoPatrocinador)
    {
        this.bloquearInputEnderecoPatrocinador = bloquearInputEnderecoPatrocinador;
    }

    public boolean isBloquearInputBairroPatrocinador()
    {
        return bloquearInputBairroPatrocinador;
    }

    public void setBloquearInputBairroPatrocinador(boolean bloquearInputBairroPatrocinador)
    {
        this.bloquearInputBairroPatrocinador = bloquearInputBairroPatrocinador;
    }

    public boolean isBloquearInputCidadePatrocinador()
    {
        return bloquearInputCidadePatrocinador;
    }

    public void setBloquearInputCidadePatrocinador(boolean bloquearInputCidadePatrocinador)
    {
        this.bloquearInputCidadePatrocinador = bloquearInputCidadePatrocinador;
    }

    public boolean isBloquearInputUFPatrocinador()
    {
        return bloquearInputUFPatrocinador;
    }

    public void setBloquearInputUFPatrocinador(boolean bloquearInputUFPatrocinador)
    {
        this.bloquearInputUFPatrocinador = bloquearInputUFPatrocinador;
    }

    public GeraPropostaEnvioVo getGeraPropostaEnvio()
    {
        return geraPropostaEnvio;
    }

    public void setGeraPropostaEnvio(GeraPropostaEnvioVo geraPropostaEnvio)
    {
        this.geraPropostaEnvio = geraPropostaEnvio;
    }

    public Long getCnpjEmpresa()
    {
        return cnpjEmpresa;
    }

    public void setCnpjEmpresa(Long cnpjEmpresa)
    {
        this.cnpjEmpresa = cnpjEmpresa;
    }

    public String getCpfCorretor()
    {
        return cpfCorretor;
    }

    public void setCpfCorretor(String cpfCorretor)
    {
        this.cpfCorretor = cpfCorretor;
    }

    public String getCnpjCorretor()
    {
        return cnpjCorretor;
    }

    public void setCnpjCorretor(String cnpjCorretor)
    {
        this.cnpjCorretor = cnpjCorretor;
    }

    public String getTpoCorretor()
    {
        return tpoCorretor;
    }

    public void setTpoCorretor(String tpoCorretor)
    {
        this.tpoCorretor = tpoCorretor;
    }

    public String getNomeTpoCorretor()
    {
        return nomeTpoCorretor;
    }

    public void setNomeTpoCorretor(String nomeTpoCorretor)
    {
        this.nomeTpoCorretor = nomeTpoCorretor;
    }

    public String getEstruturaComercial() 
	{
		return estruturaComercial;
	}

	public void setEstruturaComercial(String estruturaComercial) 
	{
		this.estruturaComercial = estruturaComercial;
	}

	public boolean isExibeRede() 
	{
		return exibeRede;
	}

	public void setExibeRede(boolean exibeRede) 
	{
		this.exibeRede = exibeRede;
	}

	public boolean isExibeMercado() 
	{
		return exibeMercado;
	}

	public void setExibeMercado(boolean exibeMercado) 
	{
		this.exibeMercado = exibeMercado;
	}

	public boolean isAutoPreenchimentoCorretor()
    {
        return autoPreenchimentoCorretor;
    }

    public void setAutoPreenchimentoCorretor(boolean autoPreenchimentoCorretor)
    {
        this.autoPreenchimentoCorretor = autoPreenchimentoCorretor;
    }

    public List<OpComercialTO> getOpComercialTO()
    {
        return opComercialTO;
    }

    public void setOpComercialTO(List<OpComercialTO> opComercialTO)
    {
        this.opComercialTO = opComercialTO;
    }

    public String getCodCorretorTela()
    {
        return codCorretorTela;
    }

    public void setCodCorretorTela(String codCorretorTela)
    {
        this.codCorretorTela = codCorretorTela;
    }

    public String getDescricaoSucursal()
    {
        return descricaoSucursal;
    }

    public void setDescricaoSucursal(String descricaoSucursal)
    {
        this.descricaoSucursal = descricaoSucursal;
    }

    public boolean isExibicaoPgVgbl()
    {
        return exibicaoPgVgbl;
    }

    public void setExibicaoPgVgbl(boolean exibicaoPgVgbl)
    {
        this.exibicaoPgVgbl = exibicaoPgVgbl;
    }

    public boolean isExibicaoRisco()
    {
        return exibicaoRisco;
    }

    public void setExibicaoRisco(boolean exibicaoRisco)
    {
        this.exibicaoRisco = exibicaoRisco;
    }

    public boolean isFlagExplicaoRisco()
    {
        return flagExplicaoRisco;
    }

    public void setFlagExplicaoRisco(boolean flagExplicaoRisco)
    {
        this.flagExplicaoRisco = flagExplicaoRisco;
    }

    public String getDtNascimentoBeneficiarioRisco()
    {
        return dtNascimentoBeneficiarioRisco;
    }

    public void setDtNascimentoBeneficiarioRisco(String dtNascimentoBeneficiarioRisco)
    {
        this.dtNascimentoBeneficiarioRisco = dtNascimentoBeneficiarioRisco;
        this.parametroSelecao.setIdadeBeneficiario( this.utils.obterIdade( this.dtNascimentoBeneficiarioRisco ) );
    }

    public String getSexoBeneficiarioRisco()
    {
        return sexoBeneficiarioRisco;
    }

    public void setSexoBeneficiarioRisco(String sexoBeneficiarioRisco)
    {
        this.sexoBeneficiarioRisco = sexoBeneficiarioRisco;
    }

    public QuadroResumoRisco getQuadroResumoRisco()
    {
        return quadroResumoRisco;
    }

    public TelaRiscoEtapa2 getTelaRiscoEtapa2()
    {
        return this.telaRiscoEtapa2;
    }

    private void resetErroRiscoEtapa1()
    {
        this.erroTela.setNome(false);
        this.erroTela.setSexo(false);
        this.erroTela.setEstadoCivil(false);
        this.erroTela.setRendaMensal(false);
        this.erroTela.setProfissao(false);
        this.erroTela.setPaisNascimento(false);
        this.erroTela.setRendaMensal(false);
        this.erroTela.setProfissao(false);
        this.erroTela.setPaisNascimento(false);
        this.erroTela.setTel(false);
        this.erroTela.setTelComercial(false);
        this.erroTela.setTelResidencial(false);
        this.erroTela.setCpf(false);
        this.erroTela.setDtNascimento(false);
        this.erroTela.setEmail(false);
        this.erroTela.setCep(false);
        this.erroTela.setLogradouro(false);
        this.erroTela.setNumeroEndereco(false);
        this.erroTela.setBairro(false);
        this.erroTela.setCidade(false);
        this.erroTela.setEstado(false);
    }

    private void resetErroRiscoEtapa2()
    {
        this.erroTela.setPerguntaPep(false);
        this.erroTela.setPerguntaRelacionamento(false);
        this.erroTela.setPerguntaFatca1(false);
        this.erroTela.setPerguntaFatca2(false);
        this.erroTela.setPerguntaFatca3(false);
        this.erroTela.setOrigemRecurso(false);
        this.erroTela.setOrigemRecursoObs(false);
        this.erroTela.setCargoPep(false);
        this.erroTela.setEmpresaPep(false);
        this.erroTela.setDataInicialPep(false);
        this.erroTela.setDataFinalPep(false);
        this.erroTela.setAgenteNome(false);
        this.erroTela.setAgenteCpf(false);
        this.erroTela.setAgenteCargo(false);
        this.erroTela.setTipoRelacionamento(false);
        this.erroTela.setCodPais1(false);
        this.erroTela.setCodPais2(false);
        this.erroTela.setCodPais3(false);
    }

    private void resetErroRiscoEtapa3()
    {
        this.erroTela.setContaCorrente(false);
        this.erroTela.setIdadeAposentadoria(false);
        this.erroTela.setDiaPagamento(false);
        this.erroTela.setFormaPagamento(false);
        this.erroTela.setAgenciaPagamento(false);
        this.erroTela.setContaPagamento(false);
        this.erroTela.setTipoContaPagamento(false);
        this.erroTela.setDataAposentadoriaMenorQueAtual(false);
        this.erroTela.setPrazoRecebimento(false);
    }

    private void resetErroRiscoEtapa4()
    {
    	this.erroTela.setErroSucursalSap(false);
    	this.erroTela.setEstruturaComercial(false);
        this.erroTela.setSucursal(false);
        this.erroTela.setSucursalDigitada(false);
        this.erroTela.setCodigoCorretor(false);
        this.erroTela.setAgenciaComercial(false);
        this.erroTela.setGerenteComercial(false);
    }

    private void resetErroRiscoEtapa5()
    {
        this.erroTela.setNome(false);
        this.erroTela.setGrauParentesco(false);
        this.erroTela.setGrauParentescoDesc( "" );
    }

    public TelaRiscoEtapa1 getTelaRiscoEtapa1()
    {
        return this.telaRiscoEtapa1;
    }

    public TelaRiscoEtapa3 getTelaRiscoEtapa3()
    {
        return this.telaRiscoEtapa3;
    }

    public TelaRiscoEtapa4 getTelaRiscoEtapa4()
    {
        return this.telaRiscoEtapa4;
    }

    public String buscaCepContratacaoRisco(AjaxBehaviorEvent event)
    {
        this.erroTela.setCep(false);
        this.habilitarInputContratacaoRisco();

        this.setConsisteCep(telaRiscoEtapa1.getCep());

        if (getConsisteCep().replace("-", "").length() == 8)
        {
            telaRiscoEtapa1.setNumEndereco("");
            cepVO = service.buscarCep(telaRiscoEtapa1.getCep());

            if ( null == cepVO || !cepVO.isCepEncontrado())
            {
                this.erroTela.setCep( true );
                telaRiscoEtapa1.setLogradouro( "" );
                telaRiscoEtapa1.setCidade( "" );
                telaRiscoEtapa1.setBairro( "" );
                telaRiscoEtapa1.setEstado( "" );
            } 
            else
            {
                this.erroTela.setCep( false );
                this.desbloquearCepRisco();
            }
        } 
        else
        {
            this.erroTela.setCep(true);
        }

        return "";
    }

    private void desbloquearCepRisco()
    {
        this.setBloquearInputEnderecoRisco( true );
        this.setBloquearInputCidadeRisco( true );
        this.setBloquearInputBairroRisco( true );
        this.setBloquearInputUFRisco( true );
        this.telaRiscoEtapa1.setLogradouro( cepVO.getLogdrCompleto() );
        this.telaRiscoEtapa1.setCidade( cepVO.getCidade() );
        this.telaRiscoEtapa1.setBairro( cepVO.getBairro() );
        this.telaRiscoEtapa1.setEstado( cepVO.getUf() );
        
        if ( null == this.telaRiscoEtapa1.getLogradouro() ||
                0 == this.telaRiscoEtapa1.getLogradouro().length() )
        {
            this.setBloquearInputEnderecoRisco( false );
        }
        else
        {
            if ( 40 < this.telaRiscoEtapa1.getLogradouro().length() )
            {
                this.telaRiscoEtapa1.setLogradouro( cepVO.getLogdrCompleto().substring( 0, 40 ) );
            }
        }
        
        if ( null == this.telaRiscoEtapa1.getCidade() || 
                0 == this.telaRiscoEtapa1.getCidade().length() )
        {
            this.setBloquearInputCidadeRisco( false );
            
        }
        else
        {
            if ( 30 < this.telaRiscoEtapa1.getCidade().length() )
            {
                this.telaRiscoEtapa1.setCidade( cepVO.getCidade().substring( 0, 30 ) );
            }
        }
        
        if ( null == this.telaRiscoEtapa1.getBairro() ||
                0 == this.telaRiscoEtapa1.getBairro().length() )
        {
            this.setBloquearInputBairroRisco( false );
        }
        else
        {
            if ( 20 < this.telaRiscoEtapa1.getBairro().length() )
            {
                this.telaRiscoEtapa1.setBairro( cepVO.getBairro().substring( 0, 20 ) );
            }
        }
        
        if ( null == this.telaRiscoEtapa1.getEstado() ||
                0 == this.telaRiscoEtapa1.getEstado().length() )
        {
            this.setBloquearInputUFRisco( false );
        }
        else
        {
            if ( 2 < this.telaRiscoEtapa1.getEstado().length() )
            {
                this.telaRiscoEtapa1.setEstado( cepVO.getUf().substring( 0, 2 ) );
            }
        }
    }

    public void habilitarInputContratacaoRisco()
    {
        this.setBloquearInputEnderecoRisco(false);
        this.setBloquearInputBairroRisco(false);
        this.setBloquearInputCidadeRisco(false);
        this.setBloquearInputUFRisco(false);
    }

    public boolean isBloquearInputEnderecoRisco()
    {
        return this.bloquearInputEnderecoRisco;
    }

    public boolean isBloquearInputBairroRisco()
    {
        return bloquearInputBairroRisco;
    }

    public boolean isBloquearInputCidadeRisco()
    {
        return bloquearInputCidadeRisco;
    }

    public boolean isBloquearInputUFRisco()
    {
        return bloquearInputUFRisco;
    }

    public void setBloquearInputEnderecoRisco(boolean bloquearInputEnderecoRisco)
    {
        this.bloquearInputEnderecoRisco = bloquearInputEnderecoRisco;
    }

    public void setBloquearInputBairroRisco(boolean bloquearInputBairroRisco)
    {
        this.bloquearInputBairroRisco = bloquearInputBairroRisco;
    }

    public void setBloquearInputCidadeRisco(boolean bloquearInputCidadeRisco)
    {
        this.bloquearInputCidadeRisco = bloquearInputCidadeRisco;
    }

    public void setBloquearInputUFRisco(boolean bloquearInputUFRisco)
    {
        this.bloquearInputUFRisco = bloquearInputUFRisco;
    }

    public Beneficiario getBeneficiarioRisco()
    {
        return this.beneficiarioRisco;
    }

    public void setBeneficiarioRisco(Beneficiario beneficiarioRisco)
    {
        this.beneficiarioRisco = beneficiarioRisco;
    }

    public TelaOfertaRisco getTelaOfertaRisco()
    {
        return this.telaOfertaRisco;
    }

    public void setTelaOfertaRisco(TelaOfertaRisco telaOfertaRisco)
    {
        this.telaOfertaRisco = telaOfertaRisco;
    }

    public int getContribuicao()
    {
        return this.contribuicaoRisco;
    }

    public void setContribuicao(int contribuicao)
    {
        if (this.contribuicaoRisco == contribuicao)
        {
            return;
        }

        this.contribuicaoRisco = contribuicao;
        this.quadroResumoRisco.setTipoContribuicao( this.contribuicaoRisco == 1 ? "mensal" : converteEncodingTexto("única") );
        this.telaOfertaRisco.setContribuicao( this.contribuicaoRisco );
        this.telaOfertaRisco.setRevalidar(true);
        this.servicosBeneficioRisco();
    }

    public List<TelaOfertaRisco> getListaTelaOfertaRisco()
    {
        return this.listaTelaOfertaRisco;
    }

    public int getPrazoRecebimentoRisco()
    {
        return this.prazoRecebimentoRisco;
    }

    public void setPrazoRecebimentoRisco(int prazoRecebimentoRisco)
    {
        if ( 0 == prazoRecebimentoRisco )
        {
            return;
        }
        
        if ( this.prazoRecebimentoRisco != prazoRecebimentoRisco )
        {
            this.prazoRecebimentoRisco = prazoRecebimentoRisco;
            this.telaOfertaRisco.getBeneficios().setPrazoRecebimento( this.prazoRecebimentoRisco );
            this.telaOfertaRisco.setRevalidar(true);
            this.servicosBeneficioRisco();
        }
    }

    public List<Beneficiario> getListaBeneficiariosRisco()
    {
        return this.listaBeneficiariosRisco;
    }

    public void setListaBeneficiariosRisco(List<Beneficiario> listaBeneficiariosRisco)
    {
        this.listaBeneficiariosRisco = listaBeneficiariosRisco;
    }

    public String confirmarRisco()
    {
        String retorno = "";
        this.erroTela.setOfertaRisco(false);

        setMsgGenerica(false);

        this.popularIncluirRascunhoRisco();

        String rascunhoEnvio = this.gson.toJson(this.incluirRascunhoEnvio);

        this.erro = "Risco|confirmarRisco|Envio API IncluirRascunho_01: " + rascunhoEnvio;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if (this.mockApi)
        {
            RetornoMockApi mockapi = new RetornoMockApi();
            retorno = mockapi.incluirRascunho();
        } 
        else
        {
            retorno = service.incluirRascunhoRisco(this.incluirRascunhoEnvio, this.telaRiscoEtapa4.getCodCorretor());
        }
        
        this.erro = "Risco|confirmarRisco|Retorno API IncluirRascunho_01: " + retorno;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if ( null == retorno || 
                ( retorno.indexOf( "code" ) > 0 && retorno.indexOf( "message" ) > 0 ) )
        {
            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            linhasVo.setCampoErro( "Erro ao processar incluir rascunho." );
            this.listaErro.add( linhasVo );
            return "confirmacaoRisco.xhtml";
        }

        try
        {
            this.incluirRascunhoRetorno = this.gson.fromJson(retorno, IncluirRascunhoRetornoVo.class);
        } 
        catch ( JsonParseException e)
        {
            this.erro = "Risco|confirmarRisco|ERRO retorno API IncluirRascunho_01: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            linhasVo.setCampoErro( "Erro ao processar retorno de incluir rascunho" );
            this.listaErro.add( linhasVo );
            return "confirmacaoRisco.xhtml";
        }
        
        if ( this.incluirRascunhoRetorno.getCodRetorno() != 0 )
        {
            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            linhasVo.setCampoErro( this.incluirRascunhoRetorno.getMensagem2() );
            this.listaErro.add( linhasVo );
            return "confirmacaoRisco.xhtml";
        }

        this.popularValidarInscricaoRisco();

        final String validarEnvio = this.gson.toJson(this.validaPropostaInscricaoEnvio);

        this.erro = "Risco|confirmarRisco|Envio API ValidarProposta: " + validarEnvio;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if (this.mockApi)
        {
            final RetornoMockApi mockapi = new RetornoMockApi();
            retorno = mockapi.validarBeneficiosRisco();
        } 
        else
        {
            retorno = service.validarPropostaInscricaoRisco(validaPropostaInscricaoEnvio,
                    this.telaRiscoEtapa4.getCodCorretor(), this.telaRiscoEtapa4.getCodAge());
        }

        this.erro = "Risco|confirmarRisco|Retorno API ValidarProposta: " + retorno;
        this.logService.info( this.erro );
        LoggerFactory.getLogger().log(Level.INFO, this.erro );

        if ( null == retorno ||
                ( retorno.indexOf( "code" ) > 0 && retorno.indexOf( "message" ) > 0 ) ) 
        {
            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            linhasVo.setCampoErro( "Erro ao processar validar inscricao." );
            this.listaErro.add( linhasVo );
            return "confirmacaoRisco.xhtml";
        }
        
        try
        {
            this.validaPropostaInscricaoRetorno = this.gson.fromJson(retorno,
                    ValidaPropostaInscricaoRetornoVo.class);
        } 
        catch ( JsonParseException e)
        {
            this.erro = "Risco|confirmarRisco|ERRO retorno API Validar: " +
                    CleanPath.cleanString( e.getMessage() );
            this.logService.error( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            linhasVo.setCampoErro( "Erro ao processar retorno da validar." );
            this.listaErro.add( linhasVo );
            return "confirmacaoRisco.xhtml";
        }

        if ( this.validaPropostaInscricaoRetorno.getCodRetorno() != 0 )
        {
            this.erroTela.setConfirmacaoRisco( true );
            this.listaErro.clear();
            final LinhasVo linhasVo = new LinhasVo();
            
            for( LinhasVo l : this.validaPropostaInscricaoRetorno.getLinhas() )
            {
                linhasVo.setCampoErro( l.getCampoErro() );
                this.listaErro.add( linhasVo );
            }
            return "confirmacaoRisco.xhtml";
        }

        if (!this.mockApi)
        {
            if (null == this.validaPropostaInscricaoRetorno.getSqlCode() || 
                       !this.validaPropostaInscricaoRetorno.getLinhas().isEmpty())
            {
                this.erro = "Risco|confirmar|Erro Api Valida Proposta" + this.validaPropostaInscricaoRetorno.getLinhas();
                this.logService.info( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.listaErro = this.validaPropostaInscricaoRetorno.ObterMensagemErro();
                setMsgErro("Erro ao Validar a Proposta.");
                this.erroTela.setConfirmacaoRisco( true );
                return "confirmacaoRisco.xhtml";
            }

            // segunda chamada - incluir rascunho
            this.incluirRascunhoEnvio.setIndicadorOperacao("3");
            this.incluirRascunhoEnvio.setNrProposta("" + this.incluirRascunhoRetorno.getCodProposta());
            this.incluirRascunhoEnvio.setSerie("" + this.incluirRascunhoRetorno.getSerieProposta());
            setIncluirRascunhoRetorno(new IncluirRascunhoRetornoVo());

            rascunhoEnvio = this.gson.toJson(this.incluirRascunhoEnvio);

            this.erro = "Risco|confirmarRisco|Envio API IncluirRascunho_02: " + rascunhoEnvio;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            retorno = service.incluirRascunhoRisco(this.incluirRascunhoEnvio, this.telaRiscoEtapa4.getCodCorretor());

            this.erro = "Risco|confirmarRisco|Retorno API incluirRascunho_02: " + retorno;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            if ( null == retorno )
            {
                this.erroTela.setConfirmacaoRisco( true );
                this.listaErro.clear();
                LinhasVo linhasVo = new LinhasVo();
                linhasVo.setCampoErro( "Erro ao processar incluir rascunho risco final." );
                this.listaErro.add( linhasVo );
                return "confirmacaoRisco.xhtml";
            }

            try
            {
                this.incluirRascunhoRetorno = this.gson.fromJson(retorno, IncluirRascunhoRetornoVo.class);
            } 
            catch ( JsonParseException e)
            {
                this.erro = "Risco|confirmarRisco|ERRO retorno API IncluirRascunho_02: " +
                        CleanPath.cleanString( e.getMessage() );
                this.logService.error( this.erro );
                LoggerFactory.getLogger().log(Level.INFO, this.erro );

                this.erroTela.setConfirmacaoRisco( true );
                this.listaErro.clear();
                LinhasVo linhasVo = new LinhasVo();
                linhasVo.setCampoErro( "Erro ao processar retorno de incluir rascunho risco." );
                this.listaErro.add( linhasVo );
                return "confirmacaoRisco.xhtml";
            }
            
            if ( this.incluirRascunhoRetorno.getCodRetorno() != 0 )
            {
                this.erroTela.setConfirmacaoRisco( true );
                this.listaErro.clear();
                final LinhasVo linhasVo = new LinhasVo();
                linhasVo.setCampoErro( this.incluirRascunhoRetorno.getMensagem2() );
                this.listaErro.add( linhasVo );
                return "confirmacaoRisco.xhtml";
            }
        }

//        try 
//        {
            gerarPdfRisco();
//        }
//        catch( Exception e )
//        {
//            this.erro = "Risco|confirmarRisco|Erro: ";
//            this.logService.error( this.erro );
//            LoggerFactory.getLogger().log(Level.INFO, this.erro );
//
//            this.erroTela.setConfirmacaoRisco( true );
//            this.listaErro.clear();
//            LinhasVo linhasVo = new LinhasVo();
//            linhasVo.setCampoErro( "Erro ao processar o arquivo PDF" );
//            this.listaErro.add( linhasVo );
//            return "confirmacaoRisco.xhtml";
//        }

        if (this.mockApi)
        {
            this.validaPropostaInscricaoEnvio.setNumeroProposta("50.001");
            this.validaPropostaInscricaoEnvio.setSerie("XXX");
        } 
        else
        {
            setGeraPropostaEnvio(new GeraPropostaEnvioVo());
            geraPropostaEnvio.setCanal("27");
            geraPropostaEnvio.setOrigem("1");
            geraPropostaEnvio.setUsuario("0");
            geraPropostaEnvio.setIdtfdFuncionario("N");
            geraPropostaEnvio.setCorretor(this.telaRiscoEtapa4.getCodCorretor());
            geraPropostaEnvio.setIdtfdRepresentante("N");
            geraPropostaEnvio.setSerie(this.incluirRascunhoRetorno.getSerieProposta().toString());
            geraPropostaEnvio.setProposta(this.incluirRascunhoRetorno.getCodProposta().toString());
            geraPropostaEnvio.setDtVenda(this.incluirRascunhoEnvio.getDtVenda());
            geraPropostaEnvio.setTpProposta("2");

            final String geraEnvio = this.gson.toJson(this.incluirRascunhoEnvio);

            this.erro = "Risco|confirmarRisco|Envio API GeraPropostaEnvio: " + geraEnvio;
            this.logService.info( this.erro );
            LoggerFactory.getLogger().log(Level.INFO, this.erro );

            service.geraProposta(geraPropostaEnvio);
        }

        return "riscoPropostaTransmitida.xhtml";
    }

    public void popularIncluirRascunhoRisco()
    {
        final String dataAtual = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());

        this.incluirRascunhoEnvio.setCodAge(incluirZero(this.telaRiscoEtapa4.getCodAge(), 7));
        this.incluirRascunhoEnvio.setCodBloco(0);
        this.incluirRascunhoEnvio.setIndicadorOperacao("1");
        this.incluirRascunhoEnvio.setDtVenda(dataAtual);
        this.incluirRascunhoEnvio.setDtAgendamento(dataAtual);
        this.incluirRascunhoEnvio.setCodFinalidadeProposta("1");
        this.incluirRascunhoEnvio.setIdentSit("1");
        this.incluirRascunhoEnvio.setEmissaoExtrato("3");
        this.incluirRascunhoEnvio.setCodBanco("237");
        this.incluirRascunhoEnvio.setCodConvenio("884");
        this.incluirRascunhoEnvio.setBanco("237");
        this.incluirRascunhoEnvio.setDtIniVigenciaBenef("01.01.0001");
        this.incluirRascunhoEnvio.setDtIniVigenciaPlano("01.01.0001");
        this.incluirRascunhoEnvio.setFatorBenef(this.parametroSelecao.getFatorBenef());
        this.incluirRascunhoEnvio.setFormaExtrato("3");
        this.incluirRascunhoEnvio.setFundoPrevidencia("0");
        this.incluirRascunhoEnvio.setIdentExtrtPapel("N");
        this.incluirRascunhoEnvio.setIdentNegoc("N");
        this.incluirRascunhoEnvio.setProduto("N");
        this.incluirRascunhoEnvio.setProdutoFaml("N");
        this.incluirRascunhoEnvio.setProdutoPrev("N");
        this.incluirRascunhoEnvio.setQtdeBeneficio(2);
        this.incluirRascunhoEnvio.setTributo("0");
        this.incluirRascunhoEnvio.setNrProposta("0");
        this.incluirRascunhoEnvio.setSerie("0");
        this.incluirRascunhoEnvio.setIdentAgContribuinte("2");
        this.incluirRascunhoEnvio.setCodIdentificacaoExerc("2");
        this.incluirRascunhoEnvio.setTpConta( "" + this.contribuicaoRisco );
        this.incluirRascunhoEnvio.setVlrLiquido((!this.editaCpf) ? this.detalhaParticipante.getVlrPatrimonio().toString() : "0");
        String cpfNoveDigitos = retiraMascara(this.telaRiscoEtapa1.getCpf());

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1"))
        {
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1"))
            {
                this.incluirRascunhoEnvio.setFormaPagamento("1");
            } 
            else
            {
                this.incluirRascunhoEnvio.setFormaPagamento("2");
            }
        } 
        else
        {  	
            
            this.incluirRascunhoEnvio.setTpContaParticipante("3");
            this.incluirRascunhoEnvio.setAgenciaDebito("0");
            this.incluirRascunhoEnvio.setConta("0");
        }

        if (this.telaRiscoEtapa2.getPep().equals("S"))
        {
            this.incluirRascunhoEnvio.setCargoEmpreg(this.objetoPep.getCargo());
            this.incluirRascunhoEnvio.setCodControleEmpreg(cpfNoveDigitos.substring(9, 11));
            this.incluirRascunhoEnvio.setCodIdentificacaoExerc("1");
            this.incluirRascunhoEnvio.setCpfCnpjEmpreg(cpfNoveDigitos.substring(0, 9));
            this.incluirRascunhoEnvio.setDtFimExerc(adicionaPontoData(this.objetoPep.getDataFinal()));
            this.incluirRascunhoEnvio.setDtInicioExerc(adicionaPontoData(this.objetoPep.getDataInicial()));
            this.incluirRascunhoEnvio.setOrigemPblic(this.objetoPep.getEmpresa());
            this.incluirRascunhoEnvio.setOrigemRecProdt(this.objetoPep.getOrigemRecurso());
        } 
        else
        {
            this.incluirRascunhoEnvio.setCargoEmpreg(new String());
            this.incluirRascunhoEnvio.setCodControleEmpreg(new String());
            this.incluirRascunhoEnvio.setCpfCnpjEmpreg(new String());
            this.incluirRascunhoEnvio.setDtFimExerc(new String());
            this.incluirRascunhoEnvio.setDtInicioExerc(new String());
            this.incluirRascunhoEnvio.setOrigemPblic(new String());
        }

        if (this.telaRiscoEtapa2.getRelacionamentoAgPublico().equals("S"))
        {
            this.incluirRascunhoEnvio.setAgControle(this.objetoPep.getAgenteNome());
            this.incluirRascunhoEnvio.setCargo(this.objetoPep.getAgenteCargo());
            String cpfAgente = retiraMascara(this.objetoPep.getAgenteCpf()).trim();
            this.incluirRascunhoEnvio.setCodControleAg(cpfAgente.substring(cpfAgente.length() - 2, cpfAgente.length()));
            this.incluirRascunhoEnvio.setCpfCnpjAg(cpfAgente.substring(0, 9));
            this.incluirRascunhoEnvio.setIdentAgContribuinte("1");
            this.incluirRascunhoEnvio.setOrigemRecProdt(this.objetoPep.getOrigemRecurso());
            this.incluirRascunhoEnvio.setReferenciaRelacionamentoAgente(this.objetoPep.getTipoRelacionamento());
        } 
        else
        {
            this.incluirRascunhoEnvio.setAgControle(new String());
            this.incluirRascunhoEnvio.setCargo(new String());
            this.incluirRascunhoEnvio.setCodControleAg(new String());
            this.incluirRascunhoEnvio.setCpfCnpjAg(new String());
            this.incluirRascunhoEnvio.setReferenciaRelacionamentoAgente(new String());
        }

        if (!"S".equalsIgnoreCase(this.telaRiscoEtapa2.getPep())
                && !"S".equalsIgnoreCase(this.telaRiscoEtapa2.getRelacionamentoAgPublico()))
        {
            this.incluirRascunhoEnvio.setOrigemRecProdt(new Integer(0));
        }

        if (null != this.objetoPep.getOrigemRecurso() && this.objetoPep.getOrigemRecurso().equals(99))
        {
            this.incluirRascunhoEnvio.setObservacaoOrigem(this.objetoPep.getObs());
        }

        this.incluirRascunhoEnvio.setCodProduto(this.parametroSelecao.getCodProdutoComercial());
        this.incluirRascunhoEnvio.setPlanoPgt(this.parametroSelecao.getPlanoPgt());
        this.incluirRascunhoEnvio
                .setMatricula((!this.editaCpf) ? Long.toString(this.detalhaParticipante.getMatricula()) : "0");

        if ("S".equals(this.telaRiscoEtapa2.getNacionalidadeAlemBrasileira()))
        {
            setRespondeuPergunta1(1);
        } 
        else
        {
            setRespondeuPergunta1(2);
        }

        if ("S".equals(this.telaRiscoEtapa2.getResidenciaForaBrasil()))
        {
            setRespondeuPergunta2(1);
        } 
        else
        {
            setRespondeuPergunta2(2);
        }

        if ("S".equals(this.telaRiscoEtapa2.getVistoOutroPais()))
        {
            setRespondeuPergunta3(1);
        } 
        else
        {
            setRespondeuPergunta3(2);
        }

        Integer qntRespostaSim = 0;

        if (this.respondeuPergunta1.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta2.equals(1))
        {
            qntRespostaSim++;
        }
        if (this.respondeuPergunta3.equals(1))
        {
            qntRespostaSim++;
        }

        this.incluirRascunhoEnvio.setQuantidade(qntRespostaSim.toString());
        this.incluirRascunhoEnvio.setCpfCnpj(cpfNoveDigitos.substring(0, 9));
        this.incluirRascunhoEnvio.setControle(cpfNoveDigitos.substring(9, 11));
        this.incluirRascunhoEnvio.setEmailParticipante(this.telaRiscoEtapa1.getEmail().trim());
        this.incluirRascunhoEnvio.setSexo(this.telaRiscoEtapa1.getSexo());
        this.incluirRascunhoEnvio.setDtNascimento(this.telaRiscoEtapa1.getDtNascimento().replace("/", "."));
        this.incluirRascunhoEnvio.setNmParticipante(this.telaRiscoEtapa1.getNome().trim());
        this.incluirRascunhoEnvio.setEstadoCivil(this.telaRiscoEtapa1.getEstadoCivil().toString());

        if (!"S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.incluirRascunhoEnvio.setPais(this.telaRiscoEtapa1.getPaisNascimento().toString());
            this.incluirRascunhoEnvio.setOcupacao(this.telaRiscoEtapa1.getProfissao().toString());
            String x = this.telaRiscoEtapa1.getRenda().replace(".", "");
            x = x.replace("R$", "");
            x = adicionaDecimal(x).replace(",", ".");
            this.incluirRascunhoEnvio.setVlrRenda(x.trim());
        }

        final String cepSemMascara = retiraMascara(this.telaRiscoEtapa1.getCep());

        this.incluirRascunhoEnvio.setCep(cepSemMascara.substring(0, 5));
        this.incluirRascunhoEnvio.setComplemento(cepSemMascara.substring(5));
        this.incluirRascunhoEnvio.setLogradouro(this.telaRiscoEtapa1.getLogradouro().trim());
        this.incluirRascunhoEnvio.setNrEndereco(this.telaRiscoEtapa1.getNumEndereco().trim());

        if (null != this.telaRiscoEtapa1.getComplemento() && !this.telaRiscoEtapa1.getComplemento().trim().isEmpty())
        {
            this.incluirRascunhoEnvio.setComplementoParticipante(this.telaRiscoEtapa1.getComplemento());
        }

        this.incluirRascunhoEnvio.setBairro(this.telaRiscoEtapa1.getBairro());
        this.incluirRascunhoEnvio.setMunicipio(this.telaRiscoEtapa1.getCidade());
        this.incluirRascunhoEnvio.setUfParticipante(this.telaRiscoEtapa1.getEstado());
        this.incluirRascunhoEnvio.setVlrBoleto(this.ofertaPlanoEnvio.getValorContribuicao().toString());

        final List<BeneficioIncluirRascunhoEnvioVo> beneficiosArray = new ArrayList<BeneficioIncluirRascunhoEnvioVo>();

        // Beneficios 01
        BeneficioIncluirRascunhoEnvioVo objetoBeneficio = new BeneficioIncluirRascunhoEnvioVo();

        objetoBeneficio.setBeneficioPrev("" + parametroSelecao.getCodBeneficioPensao());
        objetoBeneficio.setItemBloco(1);
        objetoBeneficio.setTpCobertura("0");
        objetoBeneficio.setTpRecebimento("" + this.prazoRecebimentoRisco);
        objetoBeneficio.setVlrContribuicao(this.parametroSelecao.getValContribuicaoPensao());
        objetoBeneficio.setVlrRisco( this.sliderValue );

        objetoBeneficio.setQtdeBeneficiario(0);
        List<ListaBeneficiariosIncluirRascunhoEnvioVo> listaObj = new ArrayList<ListaBeneficiariosIncluirRascunhoEnvioVo>();
        ListaBeneficiariosIncluirRascunhoEnvioVo obj = new ListaBeneficiariosIncluirRascunhoEnvioVo();

        if ("S".equals(this.parametroSelecao.getPossuiBeneficiario()))
        {
            objetoBeneficio.setQtdeBeneficiario( 1 );

            obj.setNrBeneficiario(0);
            obj.setBeneficiario(this.beneficiarioRisco.getNomeCompleto());
            obj.setDtNascimentoBeneficiario(adicionaPontoData(this.beneficiarioRisco.getDataNascimento()));
            obj.setSexoBeneficiario(this.beneficiarioRisco.getSexo().equals("Feminino") ? "F" : "M");
            obj.setGrau("" + this.beneficiarioRisco.getGrauParentesco());
            obj.setPartBeneficiario("10000");
        }

        listaObj.add(obj);
        objetoBeneficio.setBeneficiarios(listaObj);
        beneficiosArray.add(objetoBeneficio);

        // Beneficios 02
        objetoBeneficio = new BeneficioIncluirRascunhoEnvioVo();

        objetoBeneficio.setBeneficioPrev("" + parametroSelecao.getCodBeneficioPeculio());
        objetoBeneficio.setItemBloco(1);
        objetoBeneficio.setTpCobertura( "0" );
        objetoBeneficio.setTpRecebimento( "0" );
        objetoBeneficio.setVlrContribuicao(this.parametroSelecao.getValContribuicaoPeculio());
        objetoBeneficio.setVlrRisco( this.telaOfertaRisco.getBeneficios().getVlrPeculio() );

        objetoBeneficio.setQtdeBeneficiario(0);

        listaObj = new ArrayList<ListaBeneficiariosIncluirRascunhoEnvioVo>();
        obj = new ListaBeneficiariosIncluirRascunhoEnvioVo();

        if ("S".equals(this.parametroSelecao.getPossuiBeneficiario()))
        {
            objetoBeneficio.setQtdeBeneficiario( 1 );

            obj.setNrBeneficiario(0);
            obj.setBeneficiario(this.beneficiarioRisco.getNomeCompleto());
            obj.setDtNascimentoBeneficiario(adicionaPontoData(this.beneficiarioRisco.getDataNascimento()));
            obj.setSexoBeneficiario(this.beneficiarioRisco.getSexo().equals("Feminino") ? "F" : "M");
            obj.setGrau("" + this.beneficiarioRisco.getGrauParentesco());
            obj.setPartBeneficiario("10000");
        }

        listaObj.add(obj);
        objetoBeneficio.setBeneficiarios(listaObj);
        beneficiosArray.add(objetoBeneficio);

        this.incluirRascunhoEnvio.setBeneficios(beneficiosArray);
        this.incluirRascunhoEnvio.setFatcas(new ArrayList<ListaFatcasVo>());

        if (this.respondeuPergunta1.equals(1))
        {
            ListaFatcasVo fatcas = populaFatca(1, this.codPais1, this.docPais1, listaPerguntasFatca1);
            this.incluirRascunhoEnvio.getFatcas().add(fatcas);
        }

        if (respondeuPergunta2.equals(1))
        {
            ListaFatcasVo populaFatca = populaFatca(2, this.codPais2, this.docPais2, listaPerguntasFatca2);
            this.incluirRascunhoEnvio.getFatcas().add(populaFatca);
        }

        if (respondeuPergunta3.equals(1))
        {
            ListaFatcasVo populaFatca = populaFatca(3, this.codPais3, this.docPais3, listaPerguntasFatca3);
            this.incluirRascunhoEnvio.getFatcas().add(populaFatca);
        }

        this.incluirRascunhoEnvio.setTelefones(new ArrayList<TelefoneVo>());

        TelefoneVo tel1 = new TelefoneVo();
        TelefoneVo tel2 = new TelefoneVo();
        TelefoneVo tel3 = new TelefoneVo();

//        tel1.setCodArea(
//                (null != this.detalhaParticipante.getPrimeiroDdd()) ? this.detalhaParticipante.getPrimeiroDdd() : "0");
//        tel1.setNrTelefone((null != this.detalhaParticipante.getPrimeiroNrTelefone())
//                ? Integer.toString(this.detalhaParticipante.getPrimeiroNrTelefone())
//                : "0");
//        tel1.setTpTelefone("1");
//        tel1.setNrRamal(
//                (null != this.detalhaParticipante.getRamal()) ? Integer.toString(this.detalhaParticipante.getRamal())
//                        : "0");
        
        if (this.telaRiscoEtapa1.getTelResidencial() == null || this.telaRiscoEtapa1.getTelResidencial().isEmpty() 
        || this.telaRiscoEtapa1.getTelResidencial().equals("0")){ 
        	tel1.setCodArea( "0" );
        	tel1.setNrTelefone( "0" );
        	tel1.setTpTelefone( "1" );
        	tel1.setNrRamal("0" );
        }else {
            final String telsemMascara = retiraMascara(this.telaRiscoEtapa1.getTelResidencial());
            tel1.setCodArea( "00" + telsemMascara.substring(0, 2));
            tel1.setNrTelefone(telsemMascara.substring(2));
        	tel1.setTpTelefone( "1" );
        	tel1.setNrRamal("0" );       	
        }

        this.incluirRascunhoEnvio.getTelefones().add(tel1);

//        tel2.setCodArea(
//                (null != this.detalhaParticipante.getSegundoDdd()) ? this.detalhaParticipante.getSegundoDdd().trim()
//                        : "0");
//        tel2.setNrTelefone((null != this.detalhaParticipante.getSegundoNrTelefone())
//                ? Integer.toString(this.detalhaParticipante.getSegundoNrTelefone())
//                : "0");
//        tel2.setTpTelefone("2");
//        tel2.setNrRamal("0");
        
        if (this.telaRiscoEtapa1.getTelComercial() == null || this.telaRiscoEtapa1.getTelComercial().isEmpty() 
        || this.telaRiscoEtapa1.getTelComercial().equals("0")){ 
	        tel2.setCodArea( "0" );
	        tel2.setNrTelefone( "0" );
	        tel2.setTpTelefone( "2" );
	        tel2.setNrRamal( "0" );
        }else {
            final String telsemMascara = retiraMascara(this.telaRiscoEtapa1.getTelComercial());
            tel2.setCodArea( "00" + telsemMascara.substring(0, 2));
            tel2.setNrTelefone(telsemMascara.substring(2));
            tel2.setTpTelefone( "2" );
            tel2.setNrRamal("0" );   	        	   
	    }
        this.incluirRascunhoEnvio.getTelefones().add(tel2);

        final String telsemMascara = retiraMascara(this.telaRiscoEtapa1.getTel());

        tel3.setCodArea(telsemMascara.substring(0, 2));
        tel3.setNrTelefone(telsemMascara.substring(2));
        tel3.setNrRamal("0");
        tel3.setTpTelefone("3");

        this.incluirRascunhoEnvio.getTelefones().add(tel3);
        this.incluirRascunhoEnvio.setCliente(this.telaRiscoEtapa1.getNome());
        this.incluirRascunhoEnvio.setTelefonesJovem(new ArrayList<TelefoneVo>());
    }

    public void popularValidarInscricaoRisco()
    {
        this.validaPropostaInscricaoEnvio.setCodAgente(this.incluirRascunhoEnvio.getCodAge());

        if (null != this.incluirRascunhoEnvio.getAgenciaDebito()
                && !this.incluirRascunhoEnvio.getAgenciaDebito().trim().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setAgencia(this.incluirRascunhoEnvio.getAgenciaDebito());
        }

        this.validaPropostaInscricaoEnvio.setCep(this.incluirRascunhoEnvio.getCep());
        this.validaPropostaInscricaoEnvio.setCodAgenciaVenda(this.incluirRascunhoEnvio.getAgencia());
        this.validaPropostaInscricaoEnvio.setCodBanco("237");
        this.validaPropostaInscricaoEnvio.setCodBancoVenda("237");
        this.validaPropostaInscricaoEnvio.setCodConvenio("884");
        this.validaPropostaInscricaoEnvio.setCodFinalidade("1");
        this.validaPropostaInscricaoEnvio.setCodIdentProduto(this.parametroSelecao.getCodProdutoComercial()); ///////////
        this.validaPropostaInscricaoEnvio.setCodIdentTipoDoc("0");
        this.validaPropostaInscricaoEnvio.setCodIndCargoPublico(this.incluirRascunhoEnvio.getCodIdentificacaoExerc());
        this.validaPropostaInscricaoEnvio.setCodIndTributacao(this.parametroSelecao.getCodIndTributacao());
        this.validaPropostaInscricaoEnvio.setCodIndicie("0");
        this.validaPropostaInscricaoEnvio.setCodModalidade(Integer.toString(this.ofertaPlanoEnvio.getModalidade()));
        this.validaPropostaInscricaoEnvio.setCodPais(this.incluirRascunhoEnvio.getPais());
        this.validaPropostaInscricaoEnvio.setCodProdutoNegociado(this.parametroSelecao.getCodProdutoComercial());//////////
        this.validaPropostaInscricaoEnvio.setCodRecurso("0");
        this.validaPropostaInscricaoEnvio.setCodRegistro("0");
        this.validaPropostaInscricaoEnvio.setCodTipoProduto(Integer.toString(this.ofertaPlanoEnvio.getTipoContribuicao()));
        this.validaPropostaInscricaoEnvio.setComplemento(this.incluirRascunhoEnvio.getComplemento());
        this.validaPropostaInscricaoEnvio.setCodRelacionamento("2");

        String cpfNoveDigitos = retiraMascara(this.telaRiscoEtapa1.getCpf());

        if (this.telaRiscoEtapa2.getPep().equals("S"))
        {
            this.validaPropostaInscricaoEnvio.setCodIndCargoPublico("1");
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(this.objetoPep.getOrigemRecurso().toString());
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjEmpregador(cpfNoveDigitos.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjEmpregador(cpfNoveDigitos.substring(0, 9));
            this.validaPropostaInscricaoEnvio.setDsCargo(this.objetoPep.getCargo());
            this.validaPropostaInscricaoEnvio.setDtFim(adicionaPontoData(this.objetoPep.getDataFinal()));
            this.validaPropostaInscricaoEnvio.setDtInicio(adicionaPontoData(this.objetoPep.getDataInicial()));
            this.validaPropostaInscricaoEnvio.setOrgaoContratante(this.objetoPep.getEmpresa());
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjEmpregador(new String());
            this.validaPropostaInscricaoEnvio.setCpfCnpjEmpregador(new String());
            this.validaPropostaInscricaoEnvio.setDsCargo(new String());
            this.validaPropostaInscricaoEnvio.setDtFim(new String());
            this.validaPropostaInscricaoEnvio.setDtInicio(new String());
            this.validaPropostaInscricaoEnvio.setOrgaoContratante(new String());
        }

        if (this.telaRiscoEtapa2.getRelacionamentoAgPublico().equals("S"))
        {
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(this.objetoPep.getOrigemRecurso().toString());
            this.validaPropostaInscricaoEnvio.setCodRelacionamento("1");

            String cpfAgente = retiraMascara(this.objetoPep.getAgenteCpf()).trim();

            this.validaPropostaInscricaoEnvio.setControleCpfCnpjAg(cpfAgente.substring(9, 11));
            this.validaPropostaInscricaoEnvio.setCpfCnpjAgente(cpfAgente.substring(0, 9));
            this.validaPropostaInscricaoEnvio.setDsCargoAgente(this.objetoPep.getAgenteCargo());
            this.validaPropostaInscricaoEnvio.setDsRelacionamentoAgente(this.objetoPep.getTipoRelacionamento());
            this.validaPropostaInscricaoEnvio.setNmAgente(this.objetoPep.getAgenteNome());
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setControleCpfCnpjAg(new String());
            this.validaPropostaInscricaoEnvio.setCpfCnpjAgente(new String());
            this.validaPropostaInscricaoEnvio.setDsCargoAgente(new String());
            this.validaPropostaInscricaoEnvio.setDsRelacionamentoAgente(new String());
            this.validaPropostaInscricaoEnvio.setNmAgente(new String());
        }

        if (!"S".equalsIgnoreCase(this.telaRiscoEtapa2.getRelacionamentoAgPublico())
                && !"S".equalsIgnoreCase(this.telaRiscoEtapa2.getPep()))
        {
            this.validaPropostaInscricaoEnvio.setCodOrigemRecurso(new String());
        }

        if (null != this.objetoPep.getOrigemRecurso() && this.objetoPep.getOrigemRecurso().equals(99))
        {
            this.validaPropostaInscricaoEnvio.setObservacaoOrigem(this.objetoPep.getObs());
        }

        this.validaPropostaInscricaoEnvio.setControleCpfCnpj(cpfNoveDigitos.substring(9, 11));
        this.validaPropostaInscricaoEnvio.setCpfCnpjParticipante(cpfNoveDigitos.substring(0, 9));
        this.validaPropostaInscricaoEnvio.setDtAgendamento(this.incluirRascunhoEnvio.getDtAgendamento());
        this.validaPropostaInscricaoEnvio.setDtConcessao(this.incluirRascunhoEnvio.getConcsBenef());
        this.validaPropostaInscricaoEnvio.setDtInicioBeneficio("01.01.0001");
        this.validaPropostaInscricaoEnvio.setDtInicioPlano("01.01.0001");
        this.validaPropostaInscricaoEnvio.setDtNascimento(this.incluirRascunhoEnvio.getDtNascimento());
        this.validaPropostaInscricaoEnvio.setDtVenda(this.incluirRascunhoEnvio.getDtVenda());
        this.validaPropostaInscricaoEnvio.setEmail(this.incluirRascunhoEnvio.getEmailParticipante());
        this.validaPropostaInscricaoEnvio.setEstadoCivil(this.incluirRascunhoEnvio.getEstadoCivil());
        this.validaPropostaInscricaoEnvio.setFormaEnvio("3");
        this.validaPropostaInscricaoEnvio.setIndAgendamento("N");

        if (!this.incluirRascunhoEnvio.getFatcas().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setFatcas(this.incluirRascunhoEnvio.getFatcas());
        }

        this.validaPropostaInscricaoEnvio.setIndNascionalidadeFatca(this.respondeuPergunta1.toString());
        this.validaPropostaInscricaoEnvio.setIndResidenciaFatca(this.respondeuPergunta2.toString());
        this.validaPropostaInscricaoEnvio.setIndVistoFatca(this.respondeuPergunta3.toString());

        Integer qntRespostaSim = 0;

        if (this.respondeuPergunta1.equals(1))
        {
            qntRespostaSim++;
        }

        if (this.respondeuPergunta2.equals(1))
        {
            qntRespostaSim++;
        }

        if (this.respondeuPergunta3.equals(1))
        {
            qntRespostaSim++;
        }

        this.validaPropostaInscricaoEnvio.setQtdeFatca(qntRespostaSim.toString());
        this.validaPropostaInscricaoEnvio.setNome(this.incluirRascunhoEnvio.getNmParticipante());

        if (null != this.incluirRascunhoEnvio.getConta() && !this.incluirRascunhoEnvio.getConta().isEmpty())
        {
            this.validaPropostaInscricaoEnvio.setNrConta(this.incluirRascunhoEnvio.getConta());
        }

        this.validaPropostaInscricaoEnvio.setNumeroProposta("" + this.incluirRascunhoRetorno.getCodProposta());
        this.validaPropostaInscricaoEnvio.setPlanoPagamento("" + this.parametroSelecao.getCodPlano());
        this.validaPropostaInscricaoEnvio.setSerie("" + this.incluirRascunhoRetorno.getSerieProposta());
        this.validaPropostaInscricaoEnvio.setSexo(this.incluirRascunhoEnvio.getSexo());
        this.validaPropostaInscricaoEnvio.setTaxaCarregamento("0");

        if (this.incluirRascunhoEnvio.getIdentPagamentoTela().equals("1"))
        {
            if (this.incluirRascunhoEnvio.getTpContaParticipante().equals("1"))
            {
                this.validaPropostaInscricaoEnvio.setTpConta("1");
            } 
            else
            {
                this.validaPropostaInscricaoEnvio.setTpConta("2");
            }
        } 
        else
        {
            this.validaPropostaInscricaoEnvio.setTpConta("3");
        }

        this.validaPropostaInscricaoEnvio.setVlrPatrimonio(this.incluirRascunhoEnvio.getVlrLiquido());

        if (!"S".equalsIgnoreCase(this.selecionado.getIndJovem()))
        {
            this.validaPropostaInscricaoEnvio.setVlrRendaMensal(this.incluirRascunhoEnvio.getVlrRenda());
            this.validaPropostaInscricaoEnvio.setCodOcupacao(this.incluirRascunhoEnvio.getOcupacao());
        }

        this.validaPropostaInscricaoEnvio.setVlrTotalContrib(this.incluirRascunhoEnvio.getBeneficios().get(0).getVlrContribuicao());
        this.validaPropostaInscricaoEnvio.setQtdeBeneficio("2");
        this.validaPropostaInscricaoEnvio.setQtdeFatca(this.incluirRascunhoEnvio.getQuantidade());

        final List<BeneficioValidarPropostaInscricaoVo> listaBeneficios = new ArrayList<BeneficioValidarPropostaInscricaoVo>();

        // Beneficios 01
        BeneficioValidarPropostaInscricaoVo objetoBeneficio = new BeneficioValidarPropostaInscricaoVo();

        objetoBeneficio.setNrBeneficio("1");
        objetoBeneficio.setCodBeneficio(this.incluirRascunhoEnvio.getBeneficios().get(0).getBeneficioPrev());
        objetoBeneficio.setVlrContribRisco(this.incluirRascunhoEnvio.getBeneficios().get(0).getVlrContribuicao());
        objetoBeneficio.setVlrRendaBenRisco(this.incluirRascunhoEnvio.getBeneficios().get(0).getVlrRisco());
        objetoBeneficio.setTpRecRisco(this.incluirRascunhoEnvio.getBeneficios().get(0).getTpRecebimento());
        objetoBeneficio.setTpCobertura("0");
        objetoBeneficio.setQtdeBeneficiario("1");

        final ListaBeneficiariosIncluirRascunhoEnvioVo beneficiario = 
                this.incluirRascunhoEnvio.getBeneficios().get(0).getBeneficiarios().get(0);
        ListaBeneficiariosValidarPropostaInscricaoVo obj = new ListaBeneficiariosValidarPropostaInscricaoVo();

        obj.setNrBeneficiario("1");
        obj.setNmBeneficiario(beneficiario.getBeneficiario());
        obj.setSexoBeneficiario(beneficiario.getSexoBeneficiario());
        obj.setDtNasimentoBeneficiario(beneficiario.getDtNascimentoBeneficiario());
        obj.setGrauParentescoBeneficiario(beneficiario.getGrau());
        obj.setPercentualBeneficiario(beneficiario.getPartBeneficiario());

        objetoBeneficio.getBeneficiarios().add(obj);

        for (int i = 1; i < 5; ++i)
        {
            obj = new ListaBeneficiariosValidarPropostaInscricaoVo();
            obj.setNrBeneficiario("0");
            objetoBeneficio.getBeneficiarios().add(obj);
        }

        listaBeneficios.add(objetoBeneficio);

        // Beneficios 02
        objetoBeneficio = new BeneficioValidarPropostaInscricaoVo();

        objetoBeneficio.setNrBeneficio("2");
        objetoBeneficio.setCodBeneficio(this.incluirRascunhoEnvio.getBeneficios().get(1).getBeneficioPrev());
        objetoBeneficio.setVlrContribRisco(this.incluirRascunhoEnvio.getBeneficios().get(1).getVlrContribuicao());
        objetoBeneficio.setVlrRendaBenRisco(this.telaOfertaRisco.getBeneficios().getVlrPeculio());
        objetoBeneficio.setTpRecRisco(this.incluirRascunhoEnvio.getBeneficios().get(1).getTpRecebimento());
        objetoBeneficio.setTpCobertura("0");
        objetoBeneficio.setQtdeBeneficiario("1");

        obj = new ListaBeneficiariosValidarPropostaInscricaoVo();

        obj.setNrBeneficiario("1");
        obj.setNmBeneficiario(beneficiario.getBeneficiario());
        obj.setSexoBeneficiario(beneficiario.getSexoBeneficiario());
        obj.setDtNasimentoBeneficiario(beneficiario.getDtNascimentoBeneficiario());
        obj.setGrauParentescoBeneficiario(beneficiario.getGrau());
        obj.setPercentualBeneficiario(beneficiario.getPartBeneficiario());

        objetoBeneficio.getBeneficiarios().add(obj);

        for (int i = 1; i < 5; ++i)
        {
            obj = new ListaBeneficiariosValidarPropostaInscricaoVo();
            obj.setNrBeneficiario("0");
            objetoBeneficio.getBeneficiarios().add(obj);
        }

        listaBeneficios.add(objetoBeneficio);

        this.validaPropostaInscricaoEnvio.setBeneficios(listaBeneficios);
        this.validaPropostaInscricaoEnvio.setTelefones(new ArrayList<TelefoneVo>());
        this.validaPropostaInscricaoEnvio.setTelefones(this.incluirRascunhoEnvio.getTelefones());
    }

    public String getSliderValue()
    {
        return this.sliderValue;
    }

    public void setSliderValue(String sliderValue)
    {
        if (this.sliderValue.equals(sliderValue))
        {
            return;
        }
        try
        {
            final double minimo = Double.parseDouble( this.listaTelaOfertaRisco.get( 0 ).getBeneficios().getVlrMinRenda() );
            final double maximo = Double.parseDouble( this.listaTelaOfertaRisco.get( 0 ).getBeneficios().getVlrMaxRenda() );
            final double valorSlider = Double.parseDouble( sliderValue );
            
            if ( valorSlider < minimo || valorSlider > maximo )
            {
                this.erroTela.setOfertaRisco( true );
                this.msgErro = this.converteEncodingTexto( "Valor da coberturar deve obter valor mínimo e máximo" );
                return;
            }
        }
        catch( NumberFormatException e )
        {
            this.erroTela.setOfertaRisco( true );
            this.msgErro = this.converteEncodingTexto( "Valor da coberturar deve obter valor mínimo e máximo" );
            return;
        }

        this.sliderValue = sliderValue;
        this.telaOfertaRisco.setContribuicao(this.contribuicaoRisco);
        this.telaOfertaRisco.setRevalidar(true);
        this.servicosBeneficioRisco();
    }

    public int getModalidade()
    {
        return this.modalidade;
    }

    public void setModalidade(int modalidade)
    {
        if (this.modalidade == modalidade)
        {
            return;
        }

        this.modalidade = modalidade;
        this.ofertaPlanoEnvio.setModalidade(this.modalidade);

        if ( 0 == this.modalidade )
        {
            setExibicaoPgVgbl(false);
            setExibicaoRisco(false);
        } 
        
        else if ( 6 == this.modalidade )
        {
            setExibicaoPgVgbl(false);
            setExibicaoRisco(true);
        } 
        else
        {
            setExibicaoPgVgbl(true);
            setExibicaoRisco(false);
        }
    }

    public boolean getTemRegistro()
    {
        return this.temRegistro;
    }

    public void setTemRegistro(boolean temRegistro)
    {
        this.temRegistro = temRegistro;
    }

	public List<LinhasVo> getListaErro() {
		return listaErro;
	}

	public void setListaErro(List<LinhasVo> listaErro) {
		this.listaErro = listaErro;
	}

    public ModalDetalhe getModalDetalhe()
    {
        return this.modalDetalhe;
    }

	public int getModalidadeRisco() {
		return modalidadeRisco;
	}

	public void setModalidadeRisco(int modalidadeRisco) {
		this.modalidadeRisco = modalidadeRisco;
	}

	public boolean isExibeModalOfertaRisco() {
		return exibeModalOfertaRisco;
	}

	public void setExibeModalOfertaRisco(boolean exibeModalOfertaRisco) {
		this.exibeModalOfertaRisco = exibeModalOfertaRisco;
	}

    public String getSliderValueTela()
    {
        return this.utils.formatarValor( this.sliderValue );
    }

    public void setSliderValueTela(String sliderValueTela)
    {
        this.sliderValueTela = sliderValueTela;
    }

    public int getIdadeMaxBeneficiario()
    {
        return this.parametroSelecao.getIdadeBeneficiario();
    }

    public boolean getConfirmacaoHabilitado()
    {
        return confirmacaoHabilitado;
    }

    public void setConfirmacaoHabilitado(boolean confirmacaoHabilitado)
    {
        this.confirmacaoHabilitado = confirmacaoHabilitado;
    }

	public String getUrlLaminaRent(Long cnpjFundo, Long filialCnpj, Long ctlrCnpj) {
		String cnpjConvertido = adicionaZeroEsquerda(cnpjFundo, 8) + adicionaZeroEsquerda(filialCnpj, 4) + adicionaZeroEsquerda(ctlrCnpj, 2);
		return "https://www.ib4.bradesco.com.br/PDFLaminaTransferenciaInterna/IND" + cnpjConvertido + ".pdf";
	}

	public void setUrlLaminaRent(String urlLaminaRent) {
		this.urlLaminaRent = urlLaminaRent;
	}

	public Map<String,String> getRentabilidadeOferta() {
		return rentabilidadeOferta;
	}

	public void setRentabilidadeOferta(Map<String,String> rentabilidadeOferta) {
		this.rentabilidadeOferta = rentabilidadeOferta;
	}

	public String getCodSucursal() {
		return codSucursal;
	}

	public void setCodSucursal(String codSucursal) {
		this.codSucursal = codSucursal;
	}



	public String getCodSucursalDigitado() {
		return codSucursalDigitado;
	}

	public void setCodSucursalDigitado(String codSucursalDigitado) {
		this.codSucursalDigitado = codSucursalDigitado;
	}

	public boolean isExibeSucursalDigitada() {
		return exibeSucursalDigitada;
	}

	public void setExibeSucursalDigitada(boolean exibeSucursalDigitada) {
		this.exibeSucursalDigitada = exibeSucursalDigitada;
	}

	public boolean isMockTesteSucursal() {
		return mockTesteSucursal;
	}

	public void setMockTesteSucursal(boolean mockTesteSucursal) {
		this.mockTesteSucursal = mockTesteSucursal;
	}

	public boolean isMensagemEstruturaMercado() {
		return mensagemEstruturaMercado;
	}

	public void setMensagemEstruturaMercado(boolean mensagemEstruturaMercado) {
		this.mensagemEstruturaMercado = mensagemEstruturaMercado;
	}

	public boolean isMensagemEstruturaRede() {
		return mensagemEstruturaRede;
	}

	public void setMensagemEstruturaRede(boolean mensagemEstruturaRede) {
		this.mensagemEstruturaRede = mensagemEstruturaRede;
	}

	public boolean isMensagemEstruturaMercadoRisco() {
		return mensagemEstruturaMercadoRisco;
	}

	public void setMensagemEstruturaMercadoRisco(boolean mensagemEstruturaMercadoRisco) {
		this.mensagemEstruturaMercadoRisco = mensagemEstruturaMercadoRisco;
	}

	public boolean isMensagemEstruturaRedeRisco() {
		return mensagemEstruturaRedeRisco;
	}

	public void setMensagemEstruturaRedeRisco(boolean mensagemEstruturaRedeRisco) {
		this.mensagemEstruturaRedeRisco = mensagemEstruturaRedeRisco;
	}

	public boolean isExibirDadosPix() {
		return exibirDadosPix;
	}

	public void setExibirDadosPix(boolean exibirDadosPix) {
		this.exibirDadosPix = exibirDadosPix;
	}

	public boolean isPiloto() {
		return piloto;
	}

	public void setPiloto(boolean piloto) {
		this.piloto = piloto;
	}
}
