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

import org.apache.commons.lang3.StringUtils;

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

@ManagedBean(name = "CNC_ClasificacionAdministrativaMB")
@ViewScoped
public class CNC_ClasificacionAdministrativaMB extends ReportePeriodos {
	final static private String VW1="VI_CLASIFICACION_ECONOMICA_T1";
	final static private String VW2="VI_CLASIFICACION_ECONOMICA_T2";
	final static private String VW3="VI_CLASIFICACION_ECONOMICA_T3";
	final static private String VW4="VI_CLASIFICACION_ECONOMICA_T4";
	private String fileName;
	private StreamedContent file;
	private Integer noDecimales = 2;
	private Integer pesos = 1;
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{generaTxtFilesService}")
	private GeneraTxtFilesService txtFilesService;

	@PostConstruct
	public void init() {
		jasperReporteName = "CNC_ClasificacionAdministrativa";
		endFilename = jasperReporteName + ".pdf";
		changePeriodo();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Integer idSector = this.getUserDetails().getIdSector();
		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TrPuestoFirma firma = new TrPuestoFirma();
		String pSql=StringUtils.EMPTY;
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
		parameters.put("pL2", firma.getPuesto().getPuesto());
		parameters.put("firmaN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("pL3", firma.getPuesto().getPuesto());
		parameters.put("firmaN3", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F11.getValue());
		parameters.put("pL4", firma.getPuesto().getPuesto());
		parameters.put("firmaN4", firma.getNombre());
		pSql="SELECT  SUM(APROBADO) APROBADO,\r\n"
				+ " SUM(AMPLIACION_REDU) AMPL_REDU,\r\n"
				+ " SUM(MODIFICADO)MODIFICADO,\r\n"
				+ " SUM(DEVENGADO) DEVENGADO,\r\n"
				+ " SUM(PAGADO) PAGADO,\r\n"
				+ " SUM(SUBEJERCIDO) SUBEJERCICIO\r\n"
				+ " FROM ";
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
		
		parameters.put("sql", pSql);
		
		System.out.println(pSql.toString());
		return parameters;
	}

	private String generateSql(Integer idSector) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlMiles = new StringBuilder();
		String auto = "SUM(";
		String ejpa = "SUM(";
		String redu = "SUM(";
		String ejxpa = "SUM(";
		String ampli = "SUM(";
		 
		for (int y = getMesInicial(); y <= getMesSelected(); y++) {
			ejpa = ejpa + " PA.EJPA1_" + y + " +";
			redu = redu + " PA.REDU1_" + y + " +";
			ejxpa = ejxpa + " PA.EJXPA1_" + y + " +";
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

		sql.append(" SELECT 	APROBADO,  	(AMPLIACION -REDUCCIONES) AMPL_REDU,   ")
				.append(" 	(APROBADO + (AMPLIACION -REDUCCIONES)) MODIFICADO,  ").append(" 	DEVENGADO,	PAGADO,   ")
				.append(" 	(APROBADO + AMPLIACION -REDUCCIONES) - DEVENGADO SUBEJERCICIO ").append(" FROM (SELECT ")
				.append(auto).append(ejpa).append(redu).append(ejxpa).append(ampli).append(" FROM CATDEP D  ")
				.append(" 	    LEFT JOIN PASO PA  ").append("       ON PA.CLAVE = D.CLVDEP ")
				.append(" 		AND  	D.IDSECTOR = PA.IDSECTOR  ").append(" 		AND   	PA.IDSECTOR = 2 ")
				.append(" WHERE SUBSTR(PA.PARTIDA,2,3) = '000') T1    ");
		
		if (pesos != 1) {
			sqlMiles.append("SELECT 	(T2.APROBADO /1000) APROBADO,(T2.AMPL_REDU /1000) AMPL_REDU,				")
					.append("	(T2.MODIFICADO /1000) MODIFICADO,(T2.DEVENGADO/1000) DEVENGADO,  ")
					.append("	(T2.PAGADO/1000) PAGADO,(T2.SUBEJERCICIO /1000) SUBEJERCICIO               ")
					.append("FROM(                                                                                                      ");
			sql.insert(0, sqlMiles);
			sql.append(")T2");
		}
		System.out.println(sql.toString());
		return sql.toString();
	}

	public StreamedContent getFile() {
		Conctb conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());

		fileName = "EAEPECALDF" + conctb.getClave() + conctb.getAnoemp() + "0" + periodo.getPeriodo() + ".txt";
		InputStream stream = null;
		try {
			stream = new FileInputStream(this.txtFilesService.generaArtivoTxtLDFClasifAdminitrativa(
					this.generateSql(this.getUserDetails().getIdSector()), fileName));
		} catch (FileNotFoundException ex) {

		}
		file = new DefaultStreamedContent(stream, Constants.APPLICCTION_TXT, fileName);
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Se Generó el Archivo: " + fileName);
		return file;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public GeneraTxtFilesService getTxtFilesService() {
		return txtFilesService;
	}

	public void setTxtFilesService(GeneraTxtFilesService txtFilesService) {
		this.txtFilesService = txtFilesService;
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
