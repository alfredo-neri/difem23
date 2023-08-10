package com.gem.sistema.web.bean;

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

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

@ManagedBean(name = "gastoCategProgramaticaMB")
@ViewScoped
public class GastoCategProgramaticaMB extends ReportePeriodos {
	final static private String VW1="VI_GASTO_PROGRAMATICO_T1";
	final static private String VW2="VI_GASTO_PROGRAMATICO_T2";
	final static private String VW3="VI_GASTO_PROGRAMATICO_T3";
	final static private String VW4="VI_GASTO_PROGRAMATICO_T4";
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@PostConstruct
	public void init() {
		jasperReporteName = "gastoporcategoriaprogramatica";
		endFilename = jasperReporteName + ".pdf";
		changePeriodo();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Integer idSector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TrPuestoFirma firma = null;
		String pSql=StringUtils.EMPTY;
		parameters.put("pMun", conctb.getNomDep());
		parameters.put("pDay", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("pMes", getNombreMesSelected().toUpperCase());
		parameters.put("pMesInicial", getNombreMesInicial().toUpperCase());
		parameters.put("pYear", conctb.getAnoemp());
		parameters.put("pImagen", conctb.getImagePathLeft());
		pSql="SELECT PROGRAMA,CONCEPTO,APROBADO, AMP_RED,MODIFICADO,DEVENGADO,PAGADO,SUBEJERCIDO SUB_E FROM ";
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
		pSql=pSql+" ORDER BY PROGRAMA ASC";
		parameters.put("pQuery", pSql);
		
		if (idSector == 1) {
			switch (conctb.getClave().substring(0, 1)) {
			case "0":
				parameters.put("pNoFirmas", 2);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F01.getValue());
				parameters.put("pL1", firma.getPuesto().getPuesto());
				parameters.put("pN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F03.getValue());
				parameters.put("pL3", firma.getPuesto().getPuesto());
				parameters.put("pN3", firma.getNombre());
				break;

			case "2":
				parameters.put("pNoFirmas", 3);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F10.getValue());
				parameters.put("pL1", firma.getPuesto().getPuesto());
				parameters.put("pN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F11.getValue());
				parameters.put("pL2", firma.getPuesto().getPuesto());
				parameters.put("pN2", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F24.getValue());
				parameters.put("pL3", firma.getPuesto().getPuesto());
				parameters.put("pN3", firma.getNombre());
				break;
			case "3":
				parameters.put("pNoFirmas", 3);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F07.getValue());
				parameters.put("pL1", firma.getPuesto().getPuesto());
				parameters.put("pN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F08.getValue());
				parameters.put("pL2", firma.getPuesto().getPuesto());
				parameters.put("pN2", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F09.getValue());
				parameters.put("pL3", firma.getPuesto().getPuesto());
				parameters.put("pN3", firma.getNombre());
				break;

			case "4":
				parameters.put("pNoFirmas", 2);
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F20.getValue());
				parameters.put("pL1", firma.getPuesto().getPuesto());
				parameters.put("pN1", firma.getNombre());
				firma = this.puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
						ConstantsClaveEnnum.CLAVE_F21.getValue());
				parameters.put("pL3", firma.getPuesto().getPuesto());
				parameters.put("pN3", firma.getNombre());
				break;
			}
		} else {
			parameters.put("pImagen", conctb.getImagePathLeft());
			parameters.put("pImagen2", conctb.getImagePathRigth());

			firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
					ConstantsClaveEnnum.CLAVE_F08.getValue());
			parameters.put("pL2", firma.getPuesto().getPuesto());
			parameters.put("pN2", firma.getNombre());
			firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
					ConstantsClaveEnnum.CLAVE_F09.getValue());
			parameters.put("pL3", firma.getPuesto().getPuesto());
			parameters.put("pN3", firma.getNombre());
			firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L,
					ConstantsClaveEnnum.CLAVE_F10.getValue());
			parameters.put("pL4", firma.getPuesto().getPuesto());
			parameters.put("pN4", firma.getNombre());
		}
		
		
	
		return parameters;
	}

	private String generateSQL(Integer idSector) {
		StringBuilder sql = new StringBuilder();

		String auto = "SUM(";
		String ejpa = "SUM(";
		String redu = "SUM(";
		String ejxpa = "SUM(";
		String ampli = "SUM(";

		for (int y = getMesInicial(); y <= getMesSelected(); y++) {
			ejpa = ejpa + " P.EJPA1_" + y + " +";
			redu = redu + " P.REDU1_" + y + " +";
			ejxpa = ejxpa + " P.TOEJE1_" + y + " +";
			ampli = ampli + " P.AMPLI1_" + y + " +";
		}

		auto = auto
				+ " P.AUTO1_1+ P.AUTO1_2+ P.AUTO1_3+ P.AUTO1_4+ P.AUTO1_5+ P.AUTO1_6+ P.AUTO1_7+ P.AUTO1_8+ P.AUTO1_9+ P.AUTO1_10+ P.AUTO1_11+ P.AUTO1_12 ) APROBADO, ";
		ejpa = ejpa.substring(0, ejpa.length() - 2) + " ) PAGADO ";
		redu = redu.substring(0, redu.length() - 2) + " ) REDUCCIONES, ";
		ejxpa = ejxpa.substring(0, ejxpa.length() - 2) + " ) DEVENGADO, ";
		ampli = ampli.substring(0, ampli.length() - 2) + " ) AMPLIACIONES, ";

		sql.append("SELECT RES.PROGRAMA,RES.CONCEPTO, RES.APROBADO, (RES.AMPLIACIONES-RES.REDUCCIONES) AMP_RED, ")
				.append("(RES.APROBADO+RES.AMPLIACIONES-RES.REDUCCIONES) MODIFICADO , RES.DEVENGADO, ")
				.append("RES.PAGADO, (RES.APROBADO+RES.AMPLIACIONES-RES.REDUCCIONES-RES.DEVENGADO) SUB_E ")
				.append("FROM (SELECT  SUBSTR(P.PROGRAMA,1,8) PROGRAMA, M.CAMPO6 CONCEPTO, ").append(auto)
				.append(redu).append(ampli).append(ejxpa).append(ejpa)
				.append("FROM PASO P, MUNINEP M WHERE SUBSTR(P.PROGRAMA,1,8)=M.CAMPO7 AND ")
				.append("SUBSTR(P.PARTIDA,4,1)<>'0' AND ").append("P.IDSECTOR=M.IDSECTOR AND P.IDSECTOR = ")
				.append(idSector).append(" GROUP BY SUBSTR(P.PROGRAMA,1,8), M.CAMPO6 ORDER BY 1,2 )RES");
		System.out.println(sql.toString());
		
		return sql.toString();
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
