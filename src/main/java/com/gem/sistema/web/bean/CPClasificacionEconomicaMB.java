package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;

/**
 * @author Alfredo Neri
 *
 */
@ManagedBean(name = "cPClasificacionEconomicaMB")
@ViewScoped
public class CPClasificacionEconomicaMB extends ReportePeriodos {
	final static private String VW1="VI_CLASIFICACION_ECONOMICA_T1";
	final static private String VW2="VI_CLASIFICACION_ECONOMICA_T2";
	final static private String VW3="VI_CLASIFICACION_ECONOMICA_T3";
	final static private String VW4="VI_CLASIFICACION_ECONOMICA_T4";
	private String reporte;
	private Integer noDecimales;
	private Integer pesos;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		noDecimales = 2;
		reporte = "G";
		pesos = 1;
		jasperReporteName = "CNC_CPClasificacionEconomica";
		endFilename = jasperReporteName + ".pdf";
		changePeriodo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Integer sector = this.getUserDetails().getIdSector();
		TrPuestoFirma firma = null;
		Conctb conctb = conctbRepository.findByIdsector(sector);
		String pSql=StringUtils.EMPTY;
		parameters.put("decimalFormat", "%,." + noDecimales + "f");
		parameters.put("imagePath", conctb.getImagePathRigth());
		parameters.put("entidadName", conctb.getNomDep());
		pSql="SELECT TIPO,NOMGAS,APROBADO,AMPLIACION_REDU AMPLI_REDU,MODIFICADO,DEVENGADO,PAGADO,SUBEJERCIDO SUBEJERCICIO FROM ";
		switch (periodo.getPeriodo()) {
		      case 1:
		          pSql=pSql+VW1;
		          break;
		      case 2:
		    	  pSql=pSql+ VW2;
		          break;
		      case  3:
		    	  pSql=pSql+ VW3;
		           break;
		      case 4:
		    	  pSql=pSql+VW4;
		            break;   
		      }
		pSql=pSql+" ORDER BY TIPO ASC";
		parameters.put("query", pSql);
		parameters.put("tipoReporte", reporte);
		parameters.put("pPesos", pesos);
		parameters.put("year", conctb.getAnoemp());
		parameters.put("pDia", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("mesFinal", getNombreMesSelected().toUpperCase());
		parameters.put("mesInicial", getNombreMesInicial().toUpperCase());
		parameters.put("pAnio", conctb.getAnoemp());
		if (sector == 1) {

			switch (conctb.getClave().substring(0, 1)) {
			case "0": // Ayuntamiento
				parameters.put("noFirmas", 3);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F01.getValue());
				parameters.put("firmaP1", firma.getPuesto().getPuesto());
				parameters.put("firmaN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F02.getValue());
				parameters.put("firmaP2", firma.getPuesto().getPuesto());
				parameters.put("firmaN2", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F03.getValue());
				parameters.put("firmaP3", firma.getPuesto().getPuesto());
				parameters.put("firmaN3", firma.getNombre());

				break;

			case "2": // ODAS
				parameters.put("noFirmas", 2);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F10.getValue());
				parameters.put("firmaP1", firma.getPuesto().getPuesto());
				parameters.put("firmaN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F11.getValue());
				parameters.put("firmaP3", firma.getPuesto().getPuesto());
				parameters.put("firmaN3", firma.getNombre());
				break;

			case "3": // DIF
				parameters.put("noFirmas", 2);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F08.getValue());
				parameters.put("firmaP1", firma.getPuesto().getPuesto());
				parameters.put("firmaN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F09.getValue());
				parameters.put("firmaP3", firma.getPuesto().getPuesto());
				parameters.put("firmaN3", firma.getNombre());

				break;

			case "4": // IMCUFIDE
				parameters.put("noFirmas", 2);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F20.getValue());
				parameters.put("firmaP1", firma.getPuesto().getPuesto());
				parameters.put("firmaN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
						ConstantsClaveEnnum.CLAVE_F21.getValue());
				parameters.put("firmaP3", firma.getPuesto().getPuesto());
				parameters.put("firmaN3", firma.getNombre());

				break;

			}
		} else {
			parameters.put("noFirmas", 3);
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F08.getValue());
			parameters.put("firmaP1", firma.getPuesto().getPuesto());
			parameters.put("firmaN1", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F09.getValue());
			parameters.put("firmaP2", firma.getPuesto().getPuesto());
			parameters.put("firmaN2", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F11.getValue());
			parameters.put("firmaP3", firma.getPuesto().getPuesto());
			parameters.put("firmaN3", firma.getNombre());
		}

		return parameters;
	}

	private String generaQuery() {
		StringBuilder query = new StringBuilder();

		String aprobado = "SUM(";
		String ampliacion = "SUM(";
		String reduccion = "SUM(";
		String comprometido = "SUM(";
		String devengado = "SUM(";
		String ejercido = "SUM(";
		String pagado = "SUM(";

		for (int y = 1; y <= 12; y++) {
			aprobado = aprobado + " PA.AUTO1_" + y + " +";
		}

		for (int y = getMesInicial(); y <= getMesSelected(); y++) {
			ampliacion = ampliacion + " PA.AMPLI1_" + y + " +";
			reduccion = reduccion + " PA.REDU1_" + y + " +";
			comprometido = comprometido + " PA.COMPRO1_" + y + " +";
			devengado = devengado + " PA.EJXPA1_" + y + " +";
			ejercido = ejercido + " PA.TOEJE1_" + y + " +";
			pagado = pagado + " PA.EJPA1_" + y + " +";
		}
		aprobado = aprobado.substring(0, aprobado.length() - 2) + " ) APROBADO, ";
		ampliacion = ampliacion.substring(0, ampliacion.length() - 2) + " ) AMPLIACION, ";
		reduccion = reduccion.substring(0, reduccion.length() - 2) + " ) REDUCCION, ";
		comprometido = comprometido.substring(0, comprometido.length() - 2) + " ) COMPROMETIDO, ";
		devengado = devengado.substring(0, devengado.length() - 2) + " ) DEVENGADO, ";
		ejercido = ejercido.substring(0, ejercido.length() - 2) + " ) EJERCIDO, ";
		pagado = pagado.substring(0, pagado.length() - 2) + " ) PAGADO ";
		if (reporte.equals("G")) {

			query.append(
					"SELECT 0 TIPO, DECODE(TIPO, 1, 'GASTO CORRIENTE', 2, 'GASTO DE CAPITAL', 3, 'AMORTIZACION DE LA DEUDA Y DISMINUCION DE LOS PASIVOS', 4, 'PENSIONES Y JUBILACIONES', 5, 'PARTICIPACIONES') NOMGAS,\r\n"
							+ "		TIPO PARTIDA, '' TIPO_NAME, SUM(APROBADO) APROBADO, SUM(AMPLI_REDU) AMPLI_REDU,\r\n"
							+ "		SUM(MODIFICADO) MODIFICADO, SUM(COMPROMETIDO) COMPROMETIDO, SUM(DEVENGADO) DEVENGADO, \r\n"
							+ "		SUM(EJERCIDO) EJERCIDO, SUM(PAGADO) PAGADO, SUM(SUBEJERCICIO) SUBEJERCICIO\r\n"
							+ " FROM (\r\n"
							+ "		SELECT DECODE(SUBSTR(T1.PARTIDA,1,1), '1', 1, '2', 1, '3', 1, '4', \r\n"
							+ "				DECODE(SUBSTR(T1.PARTIDA,1,2), '45', 4, 1), '5', 2, '6', 2, '7', 2, '8', 5, '9', 3) TIPO ,\r\n"
							+ "				T1.PARTIDA, T1.NOMGAS, T1.APROBADO, (T1.AMPLIACION - T1.REDUCCION) AMPLI_REDU, (T1.APROBADO + T1.AMPLIACION - T1.REDUCCION ) MODIFICADO, T1.COMPROMETIDO, T1.DEVENGADO, T1.EJERCIDO, T1.PAGADO, ((T1.APROBADO + T1.AMPLIACION - T1.REDUCCION ) - T1.EJERCIDO) SUBEJERCICIO\r\n"
							+ "			FROM(\r\n" + "				SELECT	PA.PARTIDA, NA.NOMGAS, \r\n" + aprobado
							+ ampliacion + reduccion + comprometido + devengado + ejercido + pagado
							+ "					FROM PASO PA\r\n"
							+ "						INNER JOIN NATGAS NA ON PA.PARTIDA = NA.CLVGAS AND PA.IDSECTOR = NA.IDSECTOR\r\n"
							+ "				WHERE PA.IDSECTOR = " + this.getUserDetails().getIdSector()
							+ "				GROUP BY PA.PARTIDA, NA.NOMGAS\r\n" + "			)T1\r\n"
							+ "		WHERE SUBSTR(T1.PARTIDA,3) ='00' AND SUBSTR(T1.PARTIDA,2,1) <> '0'\r\n"
							+ "		)T2\r\n"
							+ "GROUP BY TIPO, DECODE(TIPO, 1, 'GASTO CORRIENTE', 2, 'GASTO DE CAPITAL', 3, 'AMORTIZACION DE LA DEUDA Y DISMINUCION DE LOS PASIVOS', 4, 'PENSIONES Y JUBILACIONES', 5, 'PARTICIPACIONES'),\r\n"
							+ "SUBSTR(PARTIDA,4), '' \r\n" + "ORDER BY TIPO");
		} else {
			query.append(
					"SELECT TIPO, DECODE(TIPO, 1, 'GASTO CORRIENTE', 2, 'GASTO DE CAPITAL', 3, 'AMORTIZACION DE LA DEUDA Y DISMINUCION DE LOS PASIVOS', 4, 'PENSIONES Y JUBILACIONES', 5, 'PARTICIPACIONES') TIPO_NAME,\r\n"
							+ "		PARTIDA, NOMGAS, SUM(APROBADO) APROBADO, SUM(AMPLI_REDU) AMPLI_REDU,\r\n"
							+ "		SUM(MODIFICADO) MODIFICADO, SUM(COMPROMETIDO) COMPROMETIDO, SUM(DEVENGADO) DEVENGADO, \r\n"
							+ "		SUM(EJERCIDO) EJERCIDO, SUM(PAGADO) PAGADO, SUM(SUBEJERCICIO) SUBEJERCICIO\r\n"
							+ " FROM (\r\n"
							+ "		SELECT DECODE(SUBSTR(T1.PARTIDA,1,1), '1', 1, '2', 1, '3', 1, '4', \r\n"
							+ "				DECODE(SUBSTR(T1.PARTIDA,1,2), '45', 4, 1), '5', 2, '6', 2, '7', 2, '8', 5, '9', 3) TIPO ,\r\n"
							+ "				T1.PARTIDA, T1.NOMGAS, T1.APROBADO, (T1.AMPLIACION - T1.REDUCCION) AMPLI_REDU, (T1.APROBADO + T1.AMPLIACION - T1.REDUCCION ) MODIFICADO, T1.COMPROMETIDO, T1.DEVENGADO, T1.EJERCIDO, T1.PAGADO, ((T1.APROBADO + T1.AMPLIACION - T1.REDUCCION ) - T1.EJERCIDO) SUBEJERCICIO\r\n"
							+ "			FROM(\r\n" + "				SELECT	PA.PARTIDA, NA.NOMGAS, \r\n" + aprobado
							+ ampliacion + reduccion + comprometido + devengado + ejercido + pagado
							+ "					FROM PASO PA\r\n"
							+ "						INNER JOIN NATGAS NA ON PA.PARTIDA = NA.CLVGAS AND PA.IDSECTOR = NA.IDSECTOR\r\n"
							+ "				WHERE PA.IDSECTOR = " + this.getUserDetails().getIdSector()
							+ "				GROUP BY PA.PARTIDA, NA.NOMGAS\r\n" + "			)T1\r\n" + "		)T2\r\n"
							+ "GROUP BY TIPO, DECODE(TIPO, 1, 'GASTO CORRIENTE', 2, 'GASTO DE CAPITAL', 3, 'AMORTIZACION DE LA DEUDA Y DISMINUCION DE LOS PASIVOS', 4, 'PENSIONES Y JUBILACIONES', 5, 'PARTICIPACIONES'),\r\n"
							+ "PARTIDA, NOMGAS\r\n" + "ORDER BY TIPO, PARTIDA");
			System.out.println(query.toString());
		}

		if (pesos != 1) {
			query.insert(0,
					"SELECT TIPO, TIPO_NAME, PARTIDA, NOMGAS, (APROBADO/1000) APROBADO, (AMPLI_REDU/1000) AMPLI_REDU, (MODIFICADO/1000) MODIFICADO, (COMPROMETIDO/1000) COMPROMETIDO, (DEVENGADO/1000) DEVENGADO, (EJERCIDO/1000) EJERCIDO, (PAGADO/1000) PAGADO, (SUBEJERCICIO/1000) SUBEJERCICIO FROM( ");
			query.append(" )T3");
		}

		return query.toString();
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

	public Integer getNoDecimales() {
		return noDecimales;
	}

	public void setNoDecimales(Integer noDecimales) {
		this.noDecimales = noDecimales;
	}

	public Integer getPesos() {
		return pesos;
	}

	public void setPesos(Integer pesos) {
		this.pesos = pesos;
	}

	public String getReporte() {
		return reporte;
	}

	public void setReporte(String reporte) {
		this.reporte = reporte;
	}

}
