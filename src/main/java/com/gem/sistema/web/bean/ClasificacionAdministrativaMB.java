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

@ManagedBean(name = "clasificacionAdministrativaMB")
@ViewScoped
public class ClasificacionAdministrativaMB extends ReportePeriodos {
	final static private String VW1="VI_CLASIFICACION_ADMINISTRATIVA_LDF_T1";
	final static private String VW2="VI_CLASIFICACION_ADMINISTRATIVA_LDF_T2";
	final static private String VW3="VI_CLASIFICACION_ADMINISTRATIVA_LDF_T3";
	final static private String VW4="VI_CLASIFICACION_ADMINISTRATIVA_LDF_T4";
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
		jasperReporteName = "ClasificacionAdministrativa";
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
		String sql=StringUtils.EMPTY;
		sql="SELECT GRUP,APROBADO,AMPL_REDU,MODIFICADO,DEVENGADO,PAGADO,SUBEJERCICIO FROM ";
		switch (periodo.getPeriodo()) {
		      case 1:
		    	  sql=sql+VW1;
		          break;
		      case 2:
		    	  sql=sql+ VW2;
		          break;
		      case  3:
		    	  sql=sql+ VW3;
		           break;
		      case 4:
		    	  sql=sql+VW4;
		            break;   
		      }
		 sql=sql+" ORDER BY GRUP ASC";
		parameters.put("sql", sql);
		

		return parameters;
	}

	private String generateSql(Integer idSector) {
		StringBuilder sql = new StringBuilder();
		StringBuilder sqlMiles = new StringBuilder();
		Integer trimestre=this.periodo.getPeriodo();
		Integer meses = 0;
		String fuente;
		String auto = "SUM(";
		String ejpa = "SUM(";
		String redu = "SUM(";
		String ejxpa = "SUM(";
		String ampli = "SUM(";
		String ampliGrup = "";
		String reduGrup = "";
		String autoGrup="SUM(%s";
		switch (trimestre) {
		case 1:
			meses = 3;
			fuente="5";
			break;
		case 2:
			meses = 6;
			fuente="3";
			break;
		case 3:
			meses = 9;
			fuente="3";
			break;
		case 4:
			meses = 12;
			fuente="3";
			break;
		}
		for (int y = 1; y <= meses; y++) {
			ejpa = ejpa + " PA.EJPA1_" + y + " +";
			redu = redu + " PA.REDU1_" + y + " +";
			ejxpa = ejxpa + " PA.TOEJE1_" + y + " +";
			ampli = ampli + " PA.AMPLI1_" + y + " +";
			ampliGrup = ampliGrup + " PA.AMPLI1_" + y + " +";
			reduGrup = reduGrup + " PA.REDU1_" + y + " +";
		}

		for (int y = 1; y <= 12; y++) {
			auto = auto + " PA.AUTO1_" + y + " +";
			autoGrup=autoGrup + " PA.AUTO1_" + y + " +";
		}

		auto = auto.substring(0, auto.length() - 2) + " ) APROBADO, ";
		autoGrup = autoGrup.substring(0, autoGrup.length() - 2) + " )) APROBADO, ";
		ejpa = ejpa.substring(0, ejpa.length() - 2) + " ) PAGADO, ";
		redu = redu.substring(0, redu.length() - 2) + " ) REDUCCIONES, ";
		ejxpa = ejxpa.substring(0, ejxpa.length() - 2) + " ) DEVENGADO, ";
		ampli = ampli.substring(0, ampli.length() - 2) + " ) AMPLIACION ";
		
		autoGrup=String.format(autoGrup, "DECODE (SUBSTR(PA.PARTIDA,1,1),'4',("+ampliGrup.substring(0, ampliGrup.length() - 2) +") - ("+reduGrup.substring(0, reduGrup.length() - 2)+"),");

		sql.append(" SELECT GRUP,  	DECODE(APROBADO,NULL,0,DECODE(GRUP,1,APROBADO+FN_GET_PAD_FTE3("+trimestre+"),APROBADO))APROBADO,  ")
				.append(" 	DECODE(AMPL_REDU,NULL,0,DECODE(GRUP,2,AMPL_REDU-APROBADO,AMPL_REDU))AMPL_REDU,  ")
				.append(" 	 DECODE(MODIFICADO,NULL,0,DECODE(GRUP, 1,MODIFICADO+FN_GET_PAD_FTE3("+trimestre+"),MODIFICADO))MODIFICADO,  ")
				.append(" 	DECODE(DEVENGADO,NULL,0,DEVENGADO)DEVENGADO,  ")
				.append(" 	DECODE(PAGADO,NULL,0,PAGADO)PAGADO,  ")
				.append(" 	DECODE(SUBEJERCICIO,NULL,0, DECODE(GRUP,1,SUBEJERCICIO+FN_GET_PAD_FTE3("+trimestre+"),SUBEJERCICIO) )SUBEJERCICIO ")
				.append(" FROM( SELECT GRUP,	APROBADO,  	(AMPLIACION -REDUCCIONES) AMPL_REDU,  ")
				.append(" 	DECODE(GRUP,1,(APROBADO + AMPLIACION -REDUCCIONES),AMPLIACION -REDUCCIONES) MODIFICADO,   	DEVENGADO,	PAGADO,  ")
				.append(" 	DECODE(GRUP,1,	(APROBADO + AMPLIACION -REDUCCIONES) - DEVENGADO, (AMPLIACION -REDUCCIONES) - DEVENGADO) SUBEJERCICIO   FROM ((  ")
				.append(" SELECT 2 GRUP,   ").append(autoGrup).append(ejpa).append(redu).append(ejxpa).append(ampli)
				.append(" FROM CATDEP D   		LEFT JOIN PASO PA  ")
				.append(" 		ON PA.CLAVE = D.CLVDEP AND  	D.IDSECTOR = PA.IDSECTOR AND  ")
				.append(" 	PA.IDSECTOR = 2 AND  	(SUBSTR(PA.PROGRAMA,	15,	1)>='4'   ")
				.append(" 	AND	SUBSTR(PA.PROGRAMA,	15,	1)<='5')  AND PA.PROGRAMA<>'040401010101265'     ")
				.append(" WHERE SUBSTR(PA.PARTIDA,	2,	3) = '000')   UNION ALL ( SELECT 1 GRUP,  ").append(auto)
				.append(ejpa).append(redu).append(ejxpa).append(ampli).append(" FROM CATDEP D  ")
				.append(" 		LEFT JOIN PASO PA 	ON PA.CLAVE = D.CLVDEP AND  ")
				.append(" 		D.IDSECTOR = PA.IDSECTOR AND 	PA.IDSECTOR = ").append(idSector).append(" AND  ")
				.append(" 	SUBSTR(PA.PROGRAMA, 15, 1) IN ('1','2','5')  ")
				.append(" WHERE SUBSTR(PA.PARTIDA,2,3) = '000') )   )  ");
		if (pesos != 1) {
			sqlMiles.append(
					"SELECT T2.GRUP,	(T2.APROBADO /1000) APROBADO,(T2.AMPL_REDU /1000) AMPL_REDU,				")
					.append("	(T2.MODIFICADO /1000) MODIFICADO,(T2.DEVENGADO/1000) DEVENGADO,  ")
					.append("	(T2.PAGADO/1000) PAGADO,(T2.SUBEJERCICIO /1000) SUBEJERCICIO               ")
					.append("FROM(                                                                                                      ");
			sql.insert(0, sqlMiles);
			sql.append(")T2");
		}
		System.out.println(sql);

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
