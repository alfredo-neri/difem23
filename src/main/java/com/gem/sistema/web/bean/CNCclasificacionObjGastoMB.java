
package com.gem.sistema.web.bean;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;
import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.GeneraTxtFilesService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.Constants;
import com.gem.sistema.util.ConstantsClaveEnnum;

@ManagedBean(name = "CNCclasificacionObjGastoMB")
@ViewScoped
public class CNCclasificacionObjGastoMB extends ReportePeriodos {

	private String pathName;
	InputStream stream = null;
	private Integer noDecimales = 2;
	private Integer pesos = 1;

	private String fileName;
	private StreamedContent file;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{generaTxtFilesService}")
	private GeneraTxtFilesService txtFilesService;

	@PostConstruct
	public void init() {
		jasperReporteName = "CNC_ClasificacionObjGasto";
		endFilename = jasperReporteName + ".pdf";
		changePeriodo();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {

		Integer idSector = this.getUserDetails().getIdSector();
		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TrPuestoFirma firma = new TrPuestoFirma();

		parameters.put("municipio", conctb.getNomDep());
		parameters.put("imagen", conctb.getImagePathLeft());
		parameters.put("imagen2", conctb.getImagePathRigth());
		parameters.put("firstMonth", getNombreMesInicial().toUpperCase());
		parameters.put("lastMonth", getNombreMesSelected().toUpperCase());
		parameters.put("lastDay", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("anio", conctb.getAnoemp());

		parameters.put("decimalFormat", "%,." + noDecimales + "f");
		parameters.put("pPesos", pesos);

		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("firmaN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("firmaN3", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F11.getValue());
		parameters.put("firmaN4", firma.getNombre());

		parameters.put("sql", this.generateSQL(this.getUserDetails().getIdSector()));

		return parameters;
	}

	public StreamedContent getFile() {

		Conctb conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());

		fileName = "EAEPECOGLDF" + conctb.getClave() + conctb.getAnoemp() + "0" + periodo.getPeriodo() + ".txt";
		InputStream stream = null;
		try {
			stream = new FileInputStream(this.txtFilesService
					.generaArtivoTxtLDFObjGasto(this.generateSQL(this.getUserDetails().getIdSector()), fileName));
		} catch (FileNotFoundException ex) {

		}
		file = new DefaultStreamedContent(stream, Constants.APPLICCTION_TXT, fileName);
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Se Generó el Archivo: " + fileName);
		return file;
	}

	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	private String generateSQL(Integer sector) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlMiles = new StringBuilder();
		Integer trimestre = this.periodo.getPeriodo();
		Integer meses = 0;
		String auto = "SUM(";
		String ejpa = "SUM(";
		String redu = "SUM(";
		String ejxpa = "SUM(";
		String ampli = "SUM(";
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
			
			ejpa = ejpa + " PA.EJPA1_" + y + " +";
			redu = redu + " PA.REDU1_" + y + " +";
			ejxpa = ejxpa + " PA.TOEJE1_" + y + " +";
			ampli = ampli + " PA.AMPLI1_" + y + " +";
		}
		for (int y = 1; y <= 12; y++) {
			auto = auto + " PA.AUTO1_" + y + " +";
		}
		auto = auto.substring(0, auto.length() - 2) + " ) APROBADO, ";
		ejpa = ejpa.substring(0, ejpa.length() - 2) + " ) PAGADO, ";
		redu = redu.substring(0, redu.length() - 2) + " ) REDUCCIONES, ";
		ejxpa = ejxpa.substring(0, ejxpa.length() - 2) + " ) DEVENGADO, ";
		ampli = ampli.substring(0, ampli.length() - 2) + " ) AMPLIACION ";

		sql.append(" SELECT CLVGAS,	NOMGAS, ").append(" 	DECODE(APROBADO,NULL,0,APROBADO)APROBADO, ")
				.append(" 	DECODE(AMPL_REDU,NULL,0,AMPL_REDU)AMPL_REDU, ")
				.append(" 	DECODE(MODIFICADO,NULL,0,MODIFICADO)MODIFICADO, ")
				.append(" 	DECODE(DEVENGADO,NULL,0,DEVENGADO)DEVENGADO, ")
				.append(" 	DECODE(PAGADO,NULL,0,PAGADO)PAGADO, ")
				.append(" 	DECODE(SUBEJERCICIO,NULL,0,SUBEJERCICIO)SUBEJERCICIO ").append(" FROM( ")
				.append(" SELECT CLVGAS,NOMGAS, ")
				.append(" DECODE(SUBSTR (CLVGAS,1,1),'6',(AMPLIACION -REDUCCIONES),DECODE (CLVGAS,'4000',(FN_GET_AMP( "
						+ trimestre + " )+APROBADO),DECODE (CLVGAS,'4400',(FN_GET_AMP( " + trimestre
						+ ")+APROBADO),APROBADO)))APROBADO,\r\n"
						+ "	DECODE(SUBSTR (CLVGAS,1,1),'6',APROBADO,DECODE (CLVGAS,'4000',((AMPLIACION -REDUCCIONES)-FN_GET_AMP( "
						+ trimestre + ")),DECODE (CLVGAS,'4400',((AMPLIACION -REDUCCIONES)-FN_GET_AMP( " + trimestre
						+ ")),(AMPLIACION -REDUCCIONES)))) AMPL_REDU,   ")
				.append(" 	(APROBADO + AMPLIACION -REDUCCIONES) MODIFICADO,	DEVENGADO,	PAGADO,  ")
				.append(" 	(APROBADO + AMPLIACION -REDUCCIONES) - DEVENGADO SUBEJERCICIO  ")
				.append(" FROM ((SELECT NAT.CLVGAS,	NAT.NOMGAS,                                       ").append(auto)
				.append(ejpa).append(redu).append(ejxpa).append(ampli)
				.append(" 	FROM NATGAS NAT LEFT JOIN PASO PA  ON   ")
				.append(" 		NAT.CLVGAS = PA.PARTIDA AND NAT.IDSECTOR = PA.IDSECTOR   ")
				.append(" 		AND	PA.IDSECTOR = 2  ").append(" 	WHERE	SUBSTR(NAT.CLVGAS, 3, 2) = '00'   ")
				.append(" 	GROUP BY NAT.CLVGAS,NAT.NOMGAS  ").append(" 	ORDER BY NAT.CLVGAS ASC) )) ");
		if (pesos != 1) {
			sqlMiles.append(
					"SELECT T2.CLVGAS , T2.NOMGAS, 	(T2.APROBADO /1000) APROBADO,(T2.AMPL_REDU /1000) AMPL_REDU,				")
					.append("	(T2.MODIFICADO /1000) MODIFICADO,(T2.DEVENGADO/1000) DEVENGADO,  ")
					.append("	(T2.PAGADO/1000) PAGADO,(T2.SUBEJERCICIO /1000) SUBEJERCICIO               ")
					.append("FROM(                                                                                                      ");
			sql.insert(0, sqlMiles);
			sql.append(")T2");
		}
		System.out.println(sql.toString());
		return sql.toString();
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public GeneraTxtFilesService getTxtFilesService() {
		return txtFilesService;
	}

	public void setTxtFilesService(GeneraTxtFilesService txtFilesService) {
		this.txtFilesService = txtFilesService;
	}

	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}

	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
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
