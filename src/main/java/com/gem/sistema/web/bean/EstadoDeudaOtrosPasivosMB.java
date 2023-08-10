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

@ManagedBean(name = "estadoDeudaOtrosPasivosMB")
@ViewScoped
public class EstadoDeudaOtrosPasivosMB extends ReportePeriodos {
	/**
	 * Define el tipo de reporte que se contruira <br/>
	 * <li>0 para el reporte acumulado</li>
	 * <li>1 para el reporte con mes anterior sin acumulacion</li>
	 */
	private Integer type = 0;
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@PostConstruct
	public void init() {
		jasperReporteName = "estadoDeudaOtrosPasivos";
		endFilename = jasperReporteName + ".pdf";
		this.mesAnterior = true;
		changePeriodo();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {

		TrPuestoFirma firma = new TrPuestoFirma();
		Integer sector = this.getUserDetails().getIdSector();
		Map<String, Object> parameters = new HashMap<String, Object>();

		Conctb conctb = conctbRepository.findByIdsector(sector);

		parameters.put("pImagenLeft", conctb.getImagePathLeft());
		parameters.put("pImagenRigth", conctb.getImagePathRigth());
		parameters.put("pNomDep", conctb.getNomDep());

		parameters.put("pDia", getLastDayByAnoEmp(getMesSelected(), conctb.getAnoemp()));
		parameters.put("mesFinal", getNombreMesSelected().toUpperCase());
		parameters.put("mesInicial", getNombreMesInicial().toUpperCase());
		parameters.put("pAnio", conctb.getAnoemp());
		parameters.put("p_mes", getMesSelected());

		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("pL2", firma.getPuesto().getPuesto());
		parameters.put("pN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("pL3", firma.getPuesto().getPuesto());
		parameters.put("pN3", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F10.getValue());
		parameters.put("pL4", firma.getPuesto().getPuesto());
		parameters.put("pN4", firma.getNombre());
		parameters.put("query", getQuery());

		return parameters;
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

	public String getQuery() {
		String query = "";
		String header = "SELECT 	SUM(SALINI) SALINI, \r\n" + "		SUM(CARGOS_ANT) CARGOS_ANT, \r\n"
				+ "		SUM(ABONOS_ANT) ABONOS_ANT, \r\n" + "		SUM(ABONOS_ACT) ABONOS_ACT, \r\n"
				+ "		SUM(CARGOS_ACT) CARGOS_ACT\r\n" + "	FROM (\r\n" + "		SELECT 	C.CUENTA,  \r\n"
				+ "                C.SALINI, \r\n";

		String abonoAnterior = "";
		String cargoAnterior = "";

		String abonoActual = "";
		String cargoActual = "";
		if (type == 1) {
			cargoAnterior = " 0  ";
			abonoAnterior = " 0  ";
		} else {
			for (int i = getMesInicial(); i <= getMesSelected() - 1; i++) {
				cargoAnterior += "C.CARGOS_" + i + " +";
				abonoAnterior += "C.ABONOS_" + i + " +";
			}
		}

		for (int i = getMesInicial(); i <= getMesSelected(); i++) {
			cargoActual += "C.CARGOS_" + i + " +";
			abonoActual += "C.ABONOS_" + i + " +";
		}

		if (!abonoAnterior.equals("")) {
			abonoAnterior = abonoAnterior.substring(0, abonoAnterior.length() - 2);
			cargoAnterior = cargoAnterior.substring(0, cargoAnterior.length() - 2);
		} else {
			cargoAnterior = "0";
			abonoAnterior = "0";
		}
		abonoActual = abonoActual.substring(0, abonoActual.length() - 2);
		cargoActual = cargoActual.substring(0, cargoActual.length() - 2);

		String sumas = " SUM(" + cargoAnterior + ") CARGOS_ANT,\r\n" + " SUM(" + abonoAnterior + ") ABONOS_ANT,\r\n"
				+ "	SUM(" + abonoActual + ")ABONOS_ACT,\r\n" + "	SUM(" + cargoActual + ") CARGOS_ACT\r\n";
		String footer = "        FROM   MAYCTA MA\r\n" + "            INNER JOIN CUENTA C ON MA.CUENTA = C.CUENTA \r\n"
				+ "        WHERE  	C.CUENTA >= '2000' \r\n" + "			AND C.CUENTA <= '2999' \r\n"
				+ "            AND C.SCTA ='' \r\n" + "            AND C.IDSECTOR = 2\r\n"
				+ "            AND (SUBSTR(C.CUENTA,3,2) ='00' OR SUBSTR(C.CUENTA,4,1) <>'0')\r\n"
				+ "        GROUP BY \r\n" + "				MA.NATCTA,\r\n" + "			    C.NOMCTA, \r\n"
				+ "			    C.CUENTA,  \r\n" + "                C.SALINI, \r\n"
				+ "                SUBSTR(C.CUENTA,1,2) \r\n" + ") T1\r\n"
				+ "WHERE (SALINI <> 0 OR CARGOS_ANT<>0 OR ABONOS_ANT <>0 OR CARGOS_ACT <> 0 OR ABONOS_ACT <> 0 )";
		query = header + sumas + footer;
		
		System.out.println("sql: " + query);
		return query;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
