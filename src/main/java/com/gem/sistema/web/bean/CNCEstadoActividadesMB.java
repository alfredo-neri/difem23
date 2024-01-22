package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;

@ManagedBean
@ViewScoped
public class CNCEstadoActividadesMB extends ReportePeriodos {

	private String fideicomiso;
	private String labelPeriodo;

	/** The listquery. */
	private List<String> listquery;

	/** The conctb repository. */
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		jasperReporteName = "CNC_EstadoActividades";
		endFilename = jasperReporteName + ".pdf";
		this.acumulado = true;
		tipoPeriodo = TRIMESTRE;
		changePeriodo();
	}

	@Override
	public void changePeriodo() {
		super.changePeriodo();
		switch (this.periodos.size()) {
		case 12:
			this.labelPeriodo = "Mes: ";
			break;
		case 2:
			this.labelPeriodo = "Semestre: ";
			break;
		case 4:
			this.labelPeriodo = "Trimestre: ";
			break;
		default:
			this.labelPeriodo = "Periodo: ";
			break;
		}
	}

	/**
	 * Generate querys.
	 *
	 * @param mes    the mes
	 * @param sector the sector
	 * @return the list
	 */
	public List<String> generateQuerys(Integer sector, String fideicomisos) {
		List<String> list = new ArrayList<String>();
		StringBuilder cargos = new StringBuilder();
		StringBuilder abonos = new StringBuilder();
		StringBuilder anterior = new StringBuilder();
		StringBuilder queryEgresos = new StringBuilder();
		StringBuilder queryIngresos = new StringBuilder();
		Integer i = 1;

		while (i <= getMesSelected()) {
			cargos.append(" CU.CARGOS_" + i + " +");
			abonos.append(" CU.ABONOS_" + i + " +");
			anterior.append(" EA.SDO_FINAL_" + i + " +");
			i++;
		}

		if (fideicomisos.equals("N")) {
			queryEgresos.append("SELECT	CLVGAS, ").append(
					"DECODE(CLVGAS,'1000','5100','2000','5100','3000','5100','4000','5200','5000','5700','6000','5600','8000','5300','9000','5400','')")
					.append("||' '||CLVGAS || ' ' || NOMGAS NOMBRE, ")
					.append(" NVL(ACUMULADO_ACTUAL,0) ACUMULADO_ACTUAL, NVL(ACUMULADO_ANTERIOR,0) ACUMULADO_ANTERIOR ")
					.append("FROM(SELECT CLVGAS, NOMGAS, ")
					.append(" SUM( (" + abonos.substring(0, abonos.length() - 1) + ") - ("
							+ cargos.substring(0, cargos.length() - 1) + ") + CU.SALINI) ACUMULADO_ACTUAL, ")
					.append(" SUM(" + anterior.substring(0, anterior.length() - 1) + " ) ACUMULADO_ANTERIOR ")
					.append("FROM NATGAS N LEFT JOIN CUENTA CU ON N.CLVGAS = SUBSTR(CU.SSSCTA,1,1)||'000' ")
					.append("AND CU.CUENTA IN ('5100', '5200', '5300', '5400','5600','5700') AND CU.IDSECTOR=N.IDSECTOR ")
					.append(" LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON CU.CUENTA = EA.CUENTA AND EA.SCTA='' ")
					.append("WHERE N.IDSECTOR=" + sector + " AND SUBSTR(N.CLVGAS,2,3)='000' ")
					.append("GROUP BY CLVGAS, NOMGAS ) T1 ").append("WHERE CLVGAS <> '7000' ").append(" UNION ALL ")
					.append("SELECT '9999', CU.CUENTA||' '||CU.NOMCTA, ")
					.append(" SUM( (" + abonos.substring(0, abonos.length() - 1) + ") - ("
							+ cargos.substring(0, cargos.length() - 1) + ") + CU.SALINI) ACUMULADO_ACTUAL, ")
					.append(" SUM(" + anterior.substring(0, anterior.length() - 1) + " ) ACUMULADO_ANTERIOR ")
					.append(" FROM CUENTA CU LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON CU.CUENTA = EA.CUENTA AND EA.SCTA=''")
					.append(" WHERE CU.CUENTA IN ('5500') AND CU.SCTA='' AND CU.IDSECTOR=" + sector
							+ " GROUP BY '9999', CU.CUENTA||' '||CU.NOMCTA ORDER BY CLVGAS");

			queryIngresos
					.append("SELECT	2,SUBSTR(CU.CUENTA,1,3)||'0' CUENTA, MA.NOMMAY, SUM(("
							+ abonos.substring(0, abonos.length() - 1) + ") - ("
							+ cargos.substring(0, cargos.length() - 1) + ") + CU.SALINI) ACUMULADO_ACTUAL, SUM("
							+ anterior.substring(0, anterior.length() - 1) + ") ACUMULADO_ANTERIOR FROM CUENTA CU ")
					.append(" INNER JOIN	MAYCTA MA ON SUBSTR(CU.CUENTA,	1,	3)||'0'=MA.CUENTA ")
					.append(" LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON MA.CUENTA = EA.CUENTA AND EA.SCTA='' ")
					.append(" WHERE SUBSTR(CU.CUENTA,1,4) = '4151' AND CU.SSSCTA <> '' AND CU.IDSECTOR= " + sector
							+ " GROUP BY SUBSTR(CU.CUENTA,1,3)||'0', MA.NOMMAY ")
					.append("UNION SELECT	2,SUBSTR(CU.CUENTA,1,3)||'0' , MA.NOMMAY, SUM(("
							+ abonos.substring(0, abonos.length() - 1) + ") - ("
							+ cargos.substring(0, cargos.length() - 1) + ") + CU.SALINI) ACUMULADO_ACTUAL, SUM("
							+ anterior.substring(0, anterior.length() - 1) + ") ACUMULADO_ANTERIOR FROM CUENTA CU ")
					.append(" INNER JOIN MAYCTA MA ON SUBSTR(CU.CUENTA,	1,	3)||'0'=MA.CUENTA ")
					.append(" LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON MA.CUENTA = EA.CUENTA AND EA.SCTA='' ")
					.append(" WHERE SUBSTR(CU.CUENTA,1,2) = '43' AND CU.SSSCTA <> '' AND CU.IDSECTOR= " + sector
							+ " GROUP BY SUBSTR(CU.CUENTA,1,3)||'0', MA.NOMMAY ")
					.append("UNION SELECT	3,SUBSTR(CU.CUENTA,1,3)||'0' , MA.NOMMAY, SUM(("
							+ abonos.substring(0, abonos.length() - 1) + ") - ("
							+ cargos.substring(0, cargos.length() - 1) + ") + CU.SALINI) ACUMULADO_ACTUAL, SUM("
							+ anterior.substring(0, anterior.length() - 1) + ") ACUMULADO_ANTERIOR FROM CUENTA CU ")
					.append(" INNER JOIN	MAYCTA MA ON SUBSTR(CU.CUENTA,	1,	3)||'0'=MA.CUENTA ")
					.append("	LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON MA.CUENTA = EA.CUENTA AND EA.SCTA=''")
					.append(" WHERE SUBSTR(CU.CUENTA,1,2) = '42' AND CU.SSSCTA <> '' AND CU.IDSECTOR= " + sector
							+ " GROUP BY SUBSTR(CU.CUENTA,1,3)||'0', MA.NOMMAY ORDER BY 1,2");
		} else {
			queryEgresos.append("SELECT 0 CLVGAS, SCTA||' '||CLVGAS || ' ' || NOMCTA NOMBRE, MES, ACUMULADO FROM ( ")
					.append("	SELECT	C.SCTA, C.NOMCTA, CUENTA CLVGAS,")
					.append(" SUM(NVL(C.CARGOS_" + getMesSelected() + ",0) - NVL(C.ABONOS_" + getMesSelected()
							+ ",0) + NVL(C.SALINI,0)) MES, ")
					.append(" SUM(( " + cargos.substring(0, cargos.length() - 2) + " ) - " + "( "
							+ abonos.substring(0, abonos.length() - 2) + " ) + " + " NVL(C.SALINI,0)) ACUMULADO ")
					.append("		FROM CUENTA C ")
					.append("	WHERE C.CUENTA IN ('5100', '5200', '5300', '5400','5500','5600','5700') ")
					.append("		AND C.IDSECTOR=" + sector + " AND C.SCTA <>'' AND C.SSCTA='' ")
					.append("	GROUP BY C.SCTA, C.NOMCTA,CUENTA ORDER BY C.SCTA ) T1");

			queryIngresos.append("SELECT 0, SCTA CUENTA, NOMCTA NOMMAY, ACUMULADO, MENSUAL FROM ( ")
					.append("	SELECT	CU.SCTA, MAX(CU.NOMCTA) NOMCTA, ")
					.append(" SUM((ABONOS_" + getMesSelected() + " - CARGOS_" + getMesSelected()
							+ ") + NVL(SALINI,0)) MENSUAL, ")
					.append(" SUM(( " + abonos.substring(0, abonos.length() - 2) + " ) - " + "( "
							+ cargos.substring(0, cargos.length() - 2) + " ) + " + "NVL(SALINI,0)) ACUMULADO ")
					.append("		FROM CUENTA CU ").append("	WHERE CU.IDSECTOR=").append(sector)
					.append("		AND SUBSTR(CU.CUENTA,1,2) IN ('41', '42', '43') ")
					.append("		AND CU.SCTA<>'' AND CU.SSCTA='' ").append("	GROUP BY CU.SCTA ORDER BY CU.SCTA )T1");
		}

		list.add(queryEgresos.toString());
		list.add(queryIngresos.toString());
		System.out.println("Egresos: " + queryEgresos.toString());
		System.out.println("Ingresos: " + queryIngresos.toString());
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		TrPuestoFirma firma = new TrPuestoFirma();
		Integer idSector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		listquery = generateQuerys(this.getUserDetails().getIdSector(), fideicomiso);

		parameters.put("lastDayOfMonth", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("mesName", getNombreMesSelected().toUpperCase());
		parameters.put("year", conctb.getAnoemp());
		parameters.put("pathImage1", conctb.getImagePathLeft());
		parameters.put("pathImage2", conctb.getImagePathRigth());
		parameters.put("queryEgresos", listquery.get(0));
		parameters.put("queryIngresos", listquery.get(1));
		parameters.put("usuario", getUserDetails().getUsername());
		parameters.put("entidadName", conctb.getNomDep());
		parameters.put("entidadRfc", conctb.getRfc());

		firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
				ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("firmaP1", firma.getPuesto().getPuesto());
		parameters.put("firmaN1", firma.getNombre());
		firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
				ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("firmaP2", firma.getPuesto().getPuesto());
		parameters.put("firmaN2", firma.getNombre());
		firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
				ConstantsClaveEnnum.CLAVE_F10.getValue());
		parameters.put("firmaP3", firma.getPuesto().getPuesto());
		parameters.put("firmaN3", firma.getNombre());
		firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
				ConstantsClaveEnnum.CLAVE_F12.getValue());
		parameters.put("firmaP4", firma.getPuesto().getPuesto());
		parameters.put("firmaN4", firma.getNombre());
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#generaReporteSimple(int)
	 */
	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {

		return null;
	}

	public String getFideicomiso() {
		return fideicomiso;
	}

	public void setFideicomiso(String fideicomiso) {
		this.fideicomiso = fideicomiso;
	}

	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}

	/**
	 * Gets the listquery.
	 *
	 * @return the listquery
	 */
	public List<String> getListquery() {
		return listquery;
	}

	/**
	 * Sets the listquery.
	 *
	 * @param listquery the new listquery
	 */
	public void setListquery(List<String> listquery) {
		this.listquery = listquery;
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
	 * @param conctbRepository the new conctb repository
	 */
	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	public String getLabelPeriodo() {
		return labelPeriodo;
	}

	public void setLabelPeriodo(String labelPeriodo) {
		this.labelPeriodo = labelPeriodo;
	}
}
