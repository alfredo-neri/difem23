package com.gem.sistema.web.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.FirmasRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.catalogos.ValidatePolicyService;
import com.gem.sistema.util.ConstantsClaveEnnum;

// TODO: Auto-generated Javadoc
/**
 * The Class RptEstadoEgresosIngresosMB.
 */
@ManagedBean
@ViewScoped
public class RptEstadoEgresosIngresosMB extends BaseDirectReport {
	private static final String DOWNLOAD_XLS = " jQuery('#form1\\\\:downXls').click();";
	private static final String DOWNLOAD_TXT = " jQuery('#form1\\\\:downTxt').click();";

	/** The mes. */
	private Integer mes;

	/** The list querys. */
	private List<String> listQuerys;


	/** The firmas repository. */
	@ManagedProperty("#{firmasRepository}")
	private FirmasRepository firmasRepository;

	@ManagedProperty("#{validatePolicyService}")
	private ValidatePolicyService validatePolicyService;
	
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;


	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		LOGGER.info(":: En postconsruct RptEstadoEgresosIngresos ");
		jasperReporteName = "RptEstadoEgresosIngresos";
		endFilename = jasperReporteName + ".pdf";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	public Map<String, Object> getParametersReports() {
		Integer idSector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TrPuestoFirma firma = new TrPuestoFirma(); 
		Map<String, Object> paramsReport = new java.util.HashMap<String, Object>();
		listQuerys = this.generateQuerys(this.getUserDetails().getIdSector(), mes);

		paramsReport.put("year", conctb.getAnoemp());
		paramsReport.put("USUARIO", getUserDetails().getUsername());
		paramsReport.put("imagePath1", conctb.getImagePathLeft());
		paramsReport.put("imagePath2", conctb.getImagePathRigth());
		paramsReport.put("mes", mes);
		paramsReport.put("idSector", idSector);
		paramsReport.put("sqlIngresos", listQuerys.get(0));
		paramsReport.put("sqlEgresos", listQuerys.get(1));
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F07.getValue());
		paramsReport.put("firmaP1", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN1", firma.getNombre());
		
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		paramsReport.put("firmaP2", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN2", firma.getNombre());
		
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		paramsReport.put("firmaP3", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN3", firma.getNombre());
		
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F10.getValue());
		paramsReport.put("firmaP4", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN4", firma.getNombre());
		
		paramsReport.put("entidadName", conctb.getNomDep());
		paramsReport.put("entidadRfc", conctb.getRfc());

		return paramsReport;
	}

	public void downloadXls() {
		if (this.validatePolicyService.isOpenMonth(mes, null, this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.validatePolicyService.isAfectMonth(mes, null, this.getUserDetails().getIdSector());
			this.validatePolicyService.existPolices(mes, null, this.getUserDetails().getIdSector());
			RequestContext.getCurrentInstance().execute(DOWNLOAD_XLS);
		}
	}

	public void downloadTxt() {
		if (this.validatePolicyService.isOpenMonth(mes, null, this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.validatePolicyService.isAfectMonth(mes, null, this.getUserDetails().getIdSector());
			this.validatePolicyService.existPolices(mes, null, this.getUserDetails().getIdSector());
			RequestContext.getCurrentInstance().execute(DOWNLOAD_TXT);
		}
	}

	public void viewPdf() {
		if (this.validatePolicyService.isOpenMonth(mes, null, this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.validatePolicyService.isAfectMonth(mes, null, this.getUserDetails().getIdSector());
			this.validatePolicyService.existPolices(mes, null, this.getUserDetails().getIdSector());
			this.createFilePdfInline();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#generaReporteSimple(int)
	 */
	public StreamedContent generaReporteSimple(int type) {
		return null;
		/*
		 * Map<String, Object> paramsQuery = new java.util.HashMap<String, Object>();
		 * paramsQuery.put("ID_REF", new Integer(0)); //FALTA return
		 * reporteadorDirectoImpl.getFileReport(tcReporte,paramsQuery,
		 * reporteName,type);
		 */
	}

	/**
	 * Generate querys.
	 *
	 * @param idSector the id sector
	 * @param mes      the mes
	 * @return the list
	 */
	public List<String> generateQuerys(Integer idSector, Integer mes) {
		List<String> list = new ArrayList<String>();
		StringBuilder carg10 = new StringBuilder();
		StringBuilder abon10 = new StringBuilder();
		StringBuilder carg40 = new StringBuilder();
		StringBuilder abon40 = new StringBuilder();
		StringBuilder carg50 = new StringBuilder();
		StringBuilder abon50 = new StringBuilder();
		StringBuilder cargoc = new StringBuilder();
		StringBuilder abonoc = new StringBuilder();
		StringBuilder cargos = new StringBuilder();
		StringBuilder abonos = new StringBuilder();
		StringBuilder queryIngresos = new StringBuilder();
		StringBuilder queryEgresos = new StringBuilder();
		Integer i = 1;

		while (i <= mes) {
			carg10.append(" NVL(CU110.CARGOS_" + i + ",0) + ");
			abon10.append(" NVL(CU110.ABONOS_" + i + ",0) + ");
			carg40.append(" NVL(CU140.CARGOS_" + i + ",0) + ");
			abon40.append(" NVL(CU140.ABONOS_" + i + ",0) + ");
			carg50.append(" NVL(CU150.CARGOS_" + i + ",0) + ");
			abon50.append(" NVL(CU150.ABONOS_" + i + ",0) + ");
			cargoc.append(" NVL(C.CARGOS_" + i + ",0) + ");
			abonoc.append(" NVL(C.ABONOS_" + i + ",0) + ");
			cargos.append(" NVL(CARGOS_" + i + ",0) + ");
			abonos.append(" NVL(ABONOS_" + i + ",0) + ");
			i++;
		}

		queryIngresos
				.append("SELECT A.CUENTA, A.SCTA, A.NOMCTA_A, SUM(AUTORIZADO) AUTORIZADO, SUM(EJERCIDO) EJERCIDO, SUM(ACUMULADO) ACUMULADO")
				.append(" FROM(SELECT CU110.CUENTA,CU110.SCTA,CU110.SSCTA,CU110.SSSCTA,CU110.NOMCTA NOMCTA_A,")
				.append("(((" + carg10.substring(0, carg10.length() - 2) + ") - ("
						+ abon10.substring(0, abon10.length() - 2) + ")) + NVL(CU110.SALINI,0)) AUTORIZADO, ")
				.append("((" + abon50.substring(0, abon50.length() - 2) + ")-("
						+ carg50.substring(0, carg50.length() - 2) + ")) + (("
						+ abon40.substring(0, abon40.length() - 2) + ")-(" + carg40.substring(0, carg40.length() - 2)
						+ ")) ACUMULADO, ")
				.append("(NVL(CU150.ABONOS_" + mes + ",0) - NVL(CU150.CARGOS_" + mes + ",0)) + (NVL(CU140.ABONOS_" + mes
						+ ",0) - NVL(CU140.CARGOS_" + mes + ",0)) EJERCIDO ")
				.append("FROM CUENTA CU110 ")
				.append("INNER JOIN CUENTA CU150 ON CU110.SCTA=CU150.SCTA AND CU110.SSCTA=CU150.SSCTA AND CU150.IDSECTOR=CU110.IDSECTOR AND CU110.SSSCTA=CU150.SSSCTA AND CU110.SSSSCTA=CU150.SSSSCTA AND CU150.CUENTA='8150' ")
				.append("INNER JOIN CUENTA CU140 ON CU110.SCTA=CU140.SCTA AND CU110.SSCTA=CU140.SSCTA AND CU140.IDSECTOR=CU110.IDSECTOR AND CU110.SSSCTA=CU140.SSSCTA AND CU110.SSSSCTA=CU140.SSSSCTA AND CU140.CUENTA='8140' ")
				.append("WHERE CU110.CUENTA = '8110' AND CU110.IDSECTOR=" + idSector
						+ " AND CU110.SCTA<>'' AND CU110.SSCTA<>'' AND CU110.SSSCTA='' AND UPPER(CU110.NIVCTA)='N' ")
				.append("ORDER BY CU110.CUENTA,CU110.SCTA, CU110.SSCTA ) A ")
				.append("WHERE AUTORIZADO <> 0 OR EJERCIDO <> 0 OR ACUMULADO <>0 ")
				.append("GROUP BY A.CUENTA, A.SCTA, NOMCTA_A ").append("ORDER BY A.CUENTA, A.SCTA");

		queryEgresos
				.append("SELECT	CLVGAS, NOMGAS, SUM(AUTORIZADO) AUTORIZADO, SUM(EJERCIDO) EJERCIDO_MES, SUM(ACUMULADO) EJERCIDO_ACUMULADO ")
				.append("FROM( ")
				.append("SELECT CLVGAS, NOMGAS, SUM((" + abonoc.substring(0, abonoc.length() - 2) + ") - ("
						+ cargoc.substring(0, cargoc.length() - 2)
						+ ") + C.SALINI) AUTORIZADO,0 EJERCIDO, 0 ACUMULADO ")
				.append("FROM NATGAS N LEFT JOIN CUENTA C ON N.CLVGAS = SUBSTR(C.SSSCTA,1,1)||'000' AND SUBSTR(CUENTA,1,3)='821' AND C.IDSECTOR=N.IDSECTOR ")
				.append("WHERE N.IDSECTOR=" + idSector + " AND SUBSTR(N.CLVGAS,2,3)='000'")
				.append("GROUP BY CLVGAS, NOMGAS UNION ALL ").append("SELECT CLVGAS , NOMGAS, 0 AUTORIZADO,")
				.append("SUM(NVL(C.CARGOS_" + mes + ",0) - NVL(C.ABONOS_" + mes + ",0)) EJERCIDO, 0 ACUMULADO ")
				.append("FROM NATGAS N LEFT JOIN CUENTA C ON N.CLVGAS = SUBSTR(C.SSSCTA,1,1)||'000' AND SUBSTR(CUENTA,1,3) IN ('824','825', '827') AND C.IDSECTOR=N.IDSECTOR ")
				.append("WHERE N.IDSECTOR=" + idSector
						+ " AND SUBSTR(N.CLVGAS,2,3)='000' GROUP BY CLVGAS, NOMGAS UNION ALL ")
				.append("SELECT CLVGAS , NOMGAS, 0 AUTORIZADO, 0 EJERCIDO, ")
				.append("SUM((" + cargoc.substring(0, cargoc.length() - 2) + ") - ("
						+ abonoc.substring(0, abonoc.length() - 2) + ")) ACUMULADO ")
				.append("FROM NATGAS N LEFT JOIN CUENTA C ON N.CLVGAS = SUBSTR(C.SSSCTA,1,1)||'000' AND SUBSTR(CUENTA,1,3) IN ('824','825', '827') AND C.IDSECTOR=N.IDSECTOR ")
				.append("WHERE N.IDSECTOR=" + idSector + " AND SUBSTR(N.CLVGAS,2,3)='000' GROUP BY CLVGAS, NOMGAS")
				.append(") T1 GROUP BY CLVGAS, NOMGAS ORDER BY CLVGAS");
		System.out.println(queryIngresos.toString());
		System.out.println(queryEgresos.toString());
		list.add(queryIngresos.toString());
		list.add(queryEgresos.toString());
		return list;

	}

	/**
	 * Gets the mes.
	 *
	 * @return the mes
	 */
	public Integer getMes() {
		return mes;
	}

	/**
	 * Sets the mes.
	 *
	 * @param mes the new mes
	 */
	public void setMes(Integer mes) {
		this.mes = mes;
	}


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
	 * @param firmasRepository the new firmas repository
	 */
	public void setFirmasRepository(FirmasRepository firmasRepository) {
		this.firmasRepository = firmasRepository;
	}

	/**
	 * Gets the list querys.
	 *
	 * @return the list querys
	 */
	public List<String> getListQuerys() {
		return listQuerys;
	}

	/**
	 * Sets the list querys.
	 *
	 * @param listQuerys the new list querys
	 */
	public void setListQuerys(List<String> listQuerys) {
		this.listQuerys = listQuerys;
	}

	public ValidatePolicyService getValidatePolicyService() {
		return validatePolicyService;
	}

	public void setValidatePolicyService(ValidatePolicyService validatePolicyService) {
		this.validatePolicyService = validatePolicyService;
	}

	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}
	

}
