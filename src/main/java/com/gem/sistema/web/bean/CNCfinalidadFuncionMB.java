


package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

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
@ManagedBean(name = "CNCfinalidadFuncionMB")
@ViewScoped
public class CNCfinalidadFuncionMB extends ReportePeriodos {

	private Integer noDecimales;
	private Integer pesos;
	Conctb conctb = null;

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
		pesos = 1;
		conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());
		jasperReporteName = "CNC_ClasificacionFinalidadFuncion";
		endFilename = "CNC_EAEPECF" + conctb.getClave().substring(0, 1) + conctb.getClave1() + conctb.getAnoemp() + ".pdf";
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

		parameters.put("municipio", conctb.getNomDep());
		parameters.put("imagen", conctb.getImagePathLeft());
		parameters.put("imagen2", conctb.getImagePathRigth());
		parameters.put("firstMonth", getNombreMesInicial().toUpperCase());
		parameters.put("lastMonth", getNombreMesSelected().toUpperCase());
		parameters.put("lastDay", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("anio", conctb.getAnoemp());
		parameters.put("decimalFormat", "%,." + noDecimales + "f");
		parameters.put("pPesos", pesos);
		parameters.put("sql", this.generaQuery(sector));

		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("firmaL2", firma.getPuesto().getPuesto());
		parameters.put("firmaN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("firmaN3", firma.getNombre());
		parameters.put("firmaL3", firma.getPuesto().getPuesto());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F11.getValue());
		parameters.put("firmaN4", firma.getNombre());
		parameters.put("firmaL4", firma.getPuesto().getPuesto());
		return parameters;
	}

	public String generaQuery(Integer idsector) {

		StringBuilder sSql1 = new StringBuilder();
		StringBuilder sqlMiles = new StringBuilder();
		 Integer trimestre=this.periodo.getPeriodo();
		 Integer meses = 0;
		String aprobado = "SUM(";
		String ampliacion = "SUM(";
		String reduccion = "SUM(";
		String devengado = "SUM(";
		String pagado = "SUM(";
		switch (trimestre) {
		case 1:
			meses = 3;
			break;
		case 2:
			meses = 6;
			break;
		case 3:
			meses = 9;
			break;
		case 4:
			meses = 12;
			break;
		}

		for (int y = 1; y <= meses; y++) {
			ampliacion = ampliacion + " PA.AMPLI1_" + y + " +";
			reduccion = reduccion + " PA.REDU1_" + y + " +";
			devengado = devengado + " PA.TOEJE1_" + y + " +";
			pagado = pagado + " PA.EJPA1_" + y + " +";
		}

		for (int y = 1; y <= 12; y++) {
			aprobado = aprobado + " PA.AUTO1_" + y + " +";
		}

		aprobado = aprobado.substring(0, aprobado.length() - 2) + " ) APROBADO, ";
		ampliacion = ampliacion.substring(0, ampliacion.length() - 2) + " ) AMPLIACIONES, ";
		reduccion = reduccion.substring(0, reduccion.length() - 2) + " ) REDUCCIONES, ";
		devengado = devengado.substring(0, devengado.length() - 2) + " ) DEVENGADO, ";
		pagado = pagado.substring(0, pagado.length() - 2) + " ) PAGADO ";

		sSql1.append("	SELECT GRUP,CAMPO7,	CAMPO6,				")
				.append("		DECODE(APROBADO, NULL, 0,DECODE(CAMPO7,'02',FN_GET_DESAROLLOSOCIAL_APROBADO("+ trimestre +"), APROBADO))APROBADO, 				")
				.append("		DECODE(APMLIREDU, NULL, 0,DECODE(CAMPO7,'02',FN_GET_AMP_REDU_DESARROLLOSOCIAL("+ trimestre +"), 	APMLIREDU))APMLIREDU,				")
				.append("		DECODE(MODIFICADO, NULL, 0, MODIFICADO)MODIFICADO,				")
				.append("		DECODE(DEVENGADO, NULL, 0,DEVENGADO)DEVENGADO, 				")
				.append("		DECODE(PAGADO, NULL, 0,PAGADO)PAGADO,  				")
				.append("		DECODE(SUBEJERCICIO, NULL, 0, SUBEJERCICIO)SUBEJERCICIO				")
				.append("	FROM(SELECT GRUP,	T1.CAMPO7,	CAMPO6 ,DECODE (CAMPO7,'0206',FN_GET_APROB_FUN("+ trimestre +"),T1.APROBADO)APROBADO,				")
				.append("			DECODE (CAMPO7,'0206',FN_GET_AMPL_REDU_CLASFUN("+ trimestre +"),(T1.AMPLIACIONES - T1.REDUCCIONES)) APMLIREDU,				")
				.append("			(T1.APROBADO + T1.AMPLIACIONES -T1.REDUCCIONES) MODIFICADO,				")
				.append("			T1.DEVENGADO,	T1.PAGADO,				")
				.append("			((T1.APROBADO + T1.AMPLIACIONES -T1.REDUCCIONES) - T1.DEVENGADO) SUBEJERCICIO				")
				.append("		FROM ((	SELECT * FROM(				")
				.append("				(SELECT NA.CAMPO6,1 GRUP,NA.CAMPO7,				").append(aprobado)
				.append(ampliacion).append(reduccion).append(devengado).append(pagado)
				.append("						FROM PASO PA RIGHT JOIN MUNINEP NA		ON 				")
				.append("							NA.CAMPO7 = SUBSTR(PA.PROGRAMA,	1,	4) AND				")
				.append("							SUBSTR(PA.PARTIDA,	4) <> '0' AND				")
				.append("							NA.IDSECTOR = PA.IDSECTOR AND				")
				.append("							PA.IDSECTOR = 2 				")
				.append("						WHERE NA.CAMPO1<>'' AND NA.CAMPO2=''				")
				.append("						GROUP BY NA.CAMPO0,	NA.CAMPO7,	NA.CAMPO6)				")
				.append("					UNION ALL(SELECT M.CAMPO6 ,T1.*  FROM (				")
				.append("				SELECT 1 GRUP,	NA.CAMPO0,				").append(aprobado)
				.append(ampliacion).append(reduccion).append(devengado).append(pagado)
				.append("				FROM PASO PA RIGHT JOIN MUNINEP NA		ON 				")
				.append("					NA.CAMPO7 = SUBSTR(PA.PROGRAMA,	1,	4) AND				")
				.append("					SUBSTR(PA.PARTIDA,	4) <> '0' AND				")
				.append("					NA.IDSECTOR = PA.IDSECTOR AND				")
				.append("					PA.IDSECTOR = 2 			")				
				.append("				WHERE NA.CAMPO1<>'' AND NA.CAMPO2=''				")
				.append("				GROUP BY NA.CAMPO0) T1, MUNINEP M				")
				.append("			WHERE M.CAMPO0=T1.CAMPO0 AND M.IDSECTOR=2	AND M.CAMPO1='' )				")
				.append("			) ORDER BY SUBSTR(CAMPO7,1,2)	)			")
				.append("	) T1) ORDER BY 1,2				");
		System.out.println(sSql1.toString());
		if (pesos != 1) {
			sqlMiles.append(
					"SELECT T2.GRUP,T2.CAMPO7 , T2.CAMPO6, 	(T2.APROBADO /1000) APROBADO,(T2.APMLIREDU /1000) APMLIREDU,				")
					.append("	(T2.MODIFICADO /1000) MODIFICADO,(T2.DEVENGADO/1000) DEVENGADO,  ")
					.append("	(T2.PAGADO/1000) PAGADO,(T2.SUBEJERCICIO /1000) SUBEJERCICIO               ")
					.append("FROM(                                                                                                      ");
			sSql1.insert(0, sqlMiles);
			sSql1.append(")T2");
		}
		System.out.println(sqlMiles.toString());
		System.out.println(sSql1.toString());
		return sSql1.toString();

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

}
