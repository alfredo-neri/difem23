package com.gem.sistema.web.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.Muninep;
import com.gem.sistema.business.domain.TcMes;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.FirmasRepository;
import com.gem.sistema.business.repository.catalogs.MuniNepRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.repository.load.PasoRepository;
import com.gem.sistema.business.service.callsp.ExecutePlService;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.catalogos.ValidatePolicyService;
import com.gem.sistema.util.ConstantsClaveEnnum;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;
import static com.roonin.utils.UtilDate.getLastDay;

// TODO: Auto-generated Javadoc
/**
 * The Class RF009122MB.
 */
@ManagedBean(name = "rF009122MB")
@ViewScoped
public class RF009122MB extends BaseDirectReport {

	/** The Constant DOWNLOAD_XLS. */
	private static final String DOWNLOAD_XLS = " jQuery('#form1\\\\:downXls').click();";
	private static final String DOWNLOAD_TXT = " jQuery('#form1\\\\:downTxt').click();";
	
	/** The Constant CAMPO1. */
	private static final String CAMPO1 = "";

	/** The Constant SECTOR_MUNICIPAL. */
	private static final Integer SECTOR_MUNICIPAL = 1;

	/** The Constant SECTOR_CENTRAL. */
	private static final Integer SECTOR_CENTRAL = 2;

	/** The bandera M. */
	private boolean banderaM = Boolean.FALSE;

	/** The bandera C. */
	private boolean banderaC = Boolean.FALSE;

	/** The mes. */
	private String mes;

	/** The conctb. */
	private Conctb conctb;

	/** The txt. */
	private StreamedContent txt;

	/** The excel. */
	private StreamedContent excel;

	/** The Constant PROCEDURE_NAME. */
	private static final String PROCEDURE_NAME = "SP_REPORTE_RF009122_EDOAVANCEPEFIN";

	/** The conctb repository. */
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	/** The tc mes repository. */
	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	/** The paso repository. */
	@ManagedProperty("#{pasoRepository}")
	private PasoRepository pasoRepository;

	@ManagedProperty("#{validatePolicyService}")
	private ValidatePolicyService validatePolicyService;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	/**
	 * Checks if is bandera M.
	 *
	 * @return true, if is bandera M
	 */
	public boolean isBanderaM() {
		return banderaM;
	}

	/**
	 * Sets the bandera M.
	 *
	 * @param banderaM
	 *            the new bandera M
	 */
	public void setBanderaM(boolean banderaM) {
		this.banderaM = banderaM;
	}

	/**
	 * Checks if is bandera C.
	 *
	 * @return true, if is bandera C
	 */
	public boolean isBanderaC() {
		return banderaC;
	}

	/**
	 * Sets the bandera C.
	 *
	 * @param banderaC
	 *            the new bandera C
	 */
	public void setBanderaC(boolean banderaC) {
		this.banderaC = banderaC;
	}

	/**
	 * Gets the mes.
	 *
	 * @return the mes
	 */
	public String getMes() {
		return mes;
	}

	/**
	 * Sets the mes.
	 *
	 * @param mes
	 *            the new mes
	 */
	public void setMes(String mes) {
		this.mes = mes;
	}

	/**
	 * Gets the xdire.
	 *
	 * @return the xdire
	 */
	public String getXdire() {
		return xdire;
	}

	/**
	 * Sets the xdire.
	 *
	 * @param xdire
	 *            the new xdire
	 */
	public void setXdire(String xdire) {
		this.xdire = xdire;
	}

	/**
	 * Gets the list mes.
	 *
	 * @return the list mes
	 */
	public List<TcMes> getListMes() {
		return listMes;
	}

	/**
	 * Sets the list mes.
	 *
	 * @param listMes
	 *            the new list mes
	 */
	public void setListMes(List<TcMes> listMes) {
		this.listMes = listMes;
	}

	/**
	 * Gets the list muninep.
	 *
	 * @return the list muninep
	 */
	public List<Muninep> getListMuninep() {
		return listMuninep;
	}

	/**
	 * Sets the list muninep.
	 *
	 * @param listMuninep
	 *            the new list muninep
	 */
	public void setListMuninep(List<Muninep> listMuninep) {
		this.listMuninep = listMuninep;
	}

	/**
	 * Gets the tcmes repository.
	 *
	 * @return the tcmes repository
	 */
	public TcMesRepository getTcmesRepository() {
		return tcmesRepository;
	}

	/**
	 * Sets the tcmes repository.
	 *
	 * @param tcmesRepository
	 *            the new tcmes repository
	 */
	public void setTcmesRepository(TcMesRepository tcmesRepository) {
		this.tcmesRepository = tcmesRepository;
	}

	/**
	 * Gets the muninep repository.
	 *
	 * @return the muninep repository
	 */
	public MuniNepRepository getMuninepRepository() {
		return muninepRepository;
	}

	/**
	 * Sets the muninep repository.
	 *
	 * @param muninepRepository
	 *            the new muninep repository
	 */
	public void setMuninepRepository(MuniNepRepository muninepRepository) {
		this.muninepRepository = muninepRepository;
	}

	/**
	 * Gets the conctb.
	 *
	 * @return the conctb
	 */
	public Conctb getConctb() {
		return conctb;
	}

	/**
	 * Sets the conctb.
	 *
	 * @param conctb
	 *            the new conctb
	 */
	public void setConctb(Conctb conctb) {
		this.conctb = conctb;
	}

	/**
	 * Gets the conctb repository.
	 *
	 * @return the conctb repository
	 */
	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	/**
	 * Sets the conctb repository.
	 *
	 * @param conctbRepository
	 *            the new conctb repository
	 */
	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	/**
	 * Gets the tc mes repository.
	 *
	 * @return the tc mes repository
	 */
	public TcMesRepository getTcMesRepository() {
		return tcMesRepository;
	}

	/**
	 * Sets the tc mes repository.
	 *
	 * @param tcMesRepository
	 *            the new tc mes repository
	 */
	public void setTcMesRepository(TcMesRepository tcMesRepository) {
		this.tcMesRepository = tcMesRepository;
	}

	/**
	 * Gets the paso repository.
	 *
	 * @return the paso repository
	 */
	public PasoRepository getPasoRepository() {
		return pasoRepository;
	}

	/**
	 * Sets the paso repository.
	 *
	 * @param pasoRepository
	 *            the new paso repository
	 */
	public void setPasoRepository(PasoRepository pasoRepository) {
		this.pasoRepository = pasoRepository;
	}

	/** The xdire. */
	private String xdire;

	/** The list mes. */
	private List<TcMes> listMes;

	/** The list muninep. */
	private List<Muninep> listMuninep;

	/** The tcmes repository. */
	@ManagedProperty("#{tcMesRepository}") // aqui trae el ABC
	private TcMesRepository tcmesRepository;

	/** The muninep repository. */
	@ManagedProperty("#{muniNepRepository}")
	private MuniNepRepository muninepRepository;

	/** The firmas repository. */
	@ManagedProperty("#{firmasRepository}")
	private FirmasRepository firmasRepository;

	/** The execute pl service. */
	@ManagedProperty("#{executePlService}")
	protected ExecutePlService executePlService;

	/**
	 * Gets the firmas repository.
	 *
	 * @return the firmas repository
	 */
	public FirmasRepository getFirmasRepository() {
		return firmasRepository;
	}

	/**
	 * Sets the firmas repository.
	 *
	 * @param firmasRepository
	 *            the new firmas repository
	 */
	public void setFirmasRepository(FirmasRepository firmasRepository) {
		this.firmasRepository = firmasRepository;
	}

	/**
	 * Gets the excel.
	 *
	 * @return the excel
	 */
	public StreamedContent getExcel() {
		return excel;
	}

	/**
	 * Sets the excel.
	 *
	 * @param excel
	 *            the new excel
	 */
	public void setExcel(StreamedContent excel) {
		this.excel = excel;
	}

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {

		jasperReporteName = "RF009122_EdoAvancePEfin";
		endFilename = jasperReporteName + ".pdf";

		// llenar la lista
		listMes = tcmesRepository.findAll();
		listMuninep = muninepRepository.findByIdsectorAndCampo1(this.getUserDetails().getIdSector(), CAMPO1);

		// se inicializa las listas
		if (!CollectionUtils.isEmpty(listMes)) {
			mes = listMes.get(0).getMes();
		}

		if (!CollectionUtils.isEmpty(listMuninep)) {
			xdire = listMuninep.get(0).getCampo0();
		}

		if (this.getUserDetails().getIdSector() == SECTOR_MUNICIPAL) {
			banderaM = Boolean.TRUE;
		} else if (this.getUserDetails().getIdSector() == SECTOR_CENTRAL) {
			banderaC = Boolean.TRUE;
		}

	}

	/**
	 * Validate data.
	 *
	 * @return true, if successful
	 */
	public boolean validateData() {
		list = this.getPasoRepository().getXdireByIdsector(xdire.substring(0, 2), this.getUserDetails().getIdSector());
		if (CollectionUtils.isEmpty(list)) {
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!",
					"No existe la finalidad en el mes seleccionado");
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}

	/** The list. */
	List<String> list;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	@Override
	public Map<String, Object> getParametersReports() {

		Map<String, Object> parameters = new HashMap<String, Object>();
		TrPuestoFirma firma = null;
		Integer idSector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TcMes descripMes = tcMesRepository.findByMes(mes);
		parameters.put("p_IdSector", idSector);
		parameters.put("p_Mes", Integer.valueOf(mes));
		parameters.put("p_NombreFin", xdire.substring(3));
		parameters.put("p_NumFin", xdire.substring(0, 2));
		parameters.put("p_Mun", this.getUserDetails().getMunicipio());
		if(idSector==2){
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F07.getValue());
			parameters.put("p_L1", firma.getPuesto().getPuesto());
			parameters.put("p_N1", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
			parameters.put("p_L2", firma.getPuesto().getPuesto());
			parameters.put("p_N2", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
			parameters.put("p_L3", firma.getPuesto().getPuesto());
			parameters.put("p_N3", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F10.getValue());
			parameters.put("p_L4", firma.getPuesto().getPuesto());
			parameters.put("p_N4", firma.getNombre());
		}else{
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F01.getValue());
			parameters.put("p_L1", firma.getPuesto().getPuesto());
			parameters.put("p_N1", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F02.getValue());
			parameters.put("p_L2", firma.getPuesto().getPuesto());
			parameters.put("p_N2", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F03.getValue());
			parameters.put("p_L3", firma.getPuesto().getPuesto());
			parameters.put("p_N3", firma.getNombre());
		}
	    
		parameters.put("p_Img", conctb.getImagePathLeft());
		parameters.put("p_Img2", conctb.getImagePathRigth());
		parameters.put("p_Query",
				this.generateQuery(idSector, Integer.valueOf(mes), xdire.substring(0, 2)));
		parameters.put("p_An", conctb.getAnoemp());
		parameters.put("p_LastDay", getLastDay(Integer.valueOf(mes)));
		parameters.put("p_DescrpMes", descripMes.getDescripcion());
		parameters.put("p_Dependencia", conctb.getNomDep());

		return parameters;
	}

	public void viewPdf() {
		if (this.validatePolicyService.isOpenMonth(Integer.valueOf(mes), null,
				this.getUserDetails().getIdSector()) == Boolean.TRUE) {

			this.createFilePdfInline();
			;
		}
	}

	/**
	 * Validate finality.
	 */
	public void validateFinality() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#generaReporteSimple(int)
	 */
	@Override
	public StreamedContent generaReporteSimple(int type) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Generate query.
	 *
	 * @param idsector
	 *            the idsector
	 * @param mes
	 *            the mes
	 * @param xdire
	 *            the xdire
	 * @return the string
	 */
	public String generateQuery(Integer idsector, Integer mes, String xdire) {
		StringBuilder sSql = new StringBuilder();
		StringBuilder ejempa = new StringBuilder();
		StringBuilder ejxpa = new StringBuilder();
		StringBuilder toeje = new StringBuilder();
		StringBuilder ampli = new StringBuilder();
		StringBuilder redu = new StringBuilder();
		StringBuilder compro = new StringBuilder();

		sSql.append("SELECT T1.PARTIDA, ").append("T1.DESCRIPCION,").append("T1.A APROBADO,").append("T1.F AMPLIACION,")
				.append("T1.G REDUCCION,").append("(T1.A+T1.F-T1.G) MODIFICADO,").append("(T1.CPT)COMPROMETIDO,")
				.append("T1.C DEVENGADO,").append("T1.B PAGADO,").append("T1.D EJERCIDO,")
				.append("((T1.A +T1.F-T1.G)-D) POR_EJERCER").append(" FROM (SELECT P.PARTIDA PARTIDA,")
				.append("N.NOMGAS DESCRIPCION,")
				.append("SUM(P.AUTO1_1+P.AUTO1_2+P.AUTO1_3+P.AUTO1_4+P.AUTO1_5+P.AUTO1_6+P.AUTO1_7+P.AUTO1_8+P.AUTO1_9+P.AUTO1_10+P.AUTO1_11+P.AUTO1_12)A,");

		for (int i = 1; i <= mes; i++) {

			ejempa.append(" P.EJPA1_" + i + "+");
			ejxpa.append(" P.EJXPA1_" + i + "+");
			toeje.append(" P.TOEJE1_" + i + "+");
			ampli.append(" P.AMPLI1_" + i + "+");
			redu.append(" P.REDU1_" + i + "+");
			compro.append(" P.COMPRO1_" + i + "+");

		}

		sSql.append(" SUM( ").append(ejempa.substring(1, ejempa.length() - 1)).append(")B,").append(" SUM( ")
				.append(ejxpa.substring(1, ejxpa.length() - 1)).append(")C,").append(" SUM( ")
				.append(toeje.substring(1, toeje.length() - 1)).append(")D,").append(" SUM( ")
				.append(ampli.substring(1, ampli.length() - 1)).append(")F,").append(" SUM( ")
				.append(redu.substring(1, redu.length() - 1)).append(")G,").append(" SUM( ")
				.append(compro.substring(1, compro.length() - 1)).append(")CPT ");

		sSql.append(" FROM").append(" PASO P JOIN NATGAS N ").append(" ON N.CLVGAS= P.PARTIDA ")
				.append(" AND N.IDSECTOR=P.IDSECTOR ").append(" WHERE ").append(" P.IDSECTOR = " + idsector)
				.append(" AND SUBSTR(P.PROGRAMA,1,2)='" + xdire + "'").append(" GROUP BY P.PARTIDA,N.NOMGAS)")
				.append("T1 WHERE A+B+C+D+F+G+CPT<>0");
		System.out.println(sSql);
		return sSql.toString();
	}

	/** The out. */
	Map<String, Object> out;

	/** The stream. */
	InputStream stream = null;

	/**
	 * Gets the txt.
	 *
	 * @return the txt
	 */
	public StreamedContent getTxt() {

		MapSqlParameterSource parameter = new MapSqlParameterSource();

		parameter.addValue("i_mes", Integer.valueOf(mes));
		parameter.addValue("i_idsector", this.getUserDetails().getIdSector());
		parameter.addValue("i_sql",
				this.generateQuery(this.getUserDetails().getIdSector(), Integer.valueOf(mes), xdire.substring(0, 2)));
		parameter.addValue("i_nombreFin", xdire.substring(3));
		parameter.addValue("i_numFin", xdire.substring(0, 2));
		parameter.addValue("i_nombreMun", this.getUserDetails().getMunicipio());

		out = executePlService.callProcedure(PROCEDURE_NAME, parameter);

		if (Integer.valueOf(out.get("O_COD_ERROR").toString()) > 0) {
			try {
				stream = new FileInputStream(
						new File(out.get("O_RUTA_FILE").toString() + "/" + out.get("O_NAME_FILE").toString()));
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}
			txt = new DefaultStreamedContent(stream, "application/txt", out.get("O_NAME_FILE").toString());
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", out.get("O_MSG_ERROR").toString());
		} else {
			generateNotificationFront(FacesMessage.SEVERITY_ERROR, "Error!", out.get("O_MSG_ERROR").toString());
		}

		return txt;
	}

	/**
	 * Gets the execute pl service.
	 *
	 * @return the execute pl service
	 */
	public ExecutePlService getExecutePlService() {
		return executePlService;
	}

	/**
	 * Sets the execute pl service.
	 *
	 * @param executePlService
	 *            the new execute pl service
	 */
	public void setExecutePlService(ExecutePlService executePlService) {
		this.executePlService = executePlService;
	}

	/**
	 * Sets the txt.
	 *
	 * @param txt
	 *            the new txt
	 */
	public void setTxt(StreamedContent txt) {
		this.txt = txt;
	}

	/**
	 * Download file.
	 */
	public void downloadFile() {
		this.setExcel(null);

		if (Boolean.TRUE == this.validateData()) {

			this.excel = this.getFileXls();

		}

	}
	
	public void downloadTxt() {
		if (this.validatePolicyService.isOpenMonth(Integer.valueOf(mes), null, this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.getTxt();
			RequestContext.getCurrentInstance().execute(DOWNLOAD_TXT);
		}

	}
	public void downloadXls() {
		if (this.validatePolicyService.isOpenMonth(Integer.valueOf(mes), null, this.getUserDetails().getIdSector()) == Boolean.TRUE && (Boolean.TRUE == this.validateData())) {
			this.downloadFile();
			RequestContext.getCurrentInstance().execute(DOWNLOAD_XLS);
		}
	}
	
	public void validateMonth() {
		this.validatePolicyService.isOpenMonth(Integer.valueOf(mes), null, this.getUserDetails().getIdSector()) ;
	}
	

	public ValidatePolicyService getValidatePolicyService() {
		return validatePolicyService;
	}

	public void setValidatePolicyService(ValidatePolicyService validatePolicyService) {
		this.validatePolicyService = validatePolicyService;
	}
	
	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}


}
