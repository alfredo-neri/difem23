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

@ManagedBean(name = "cNCEstadoDeudaOtrosPasivosMB")
@ViewScoped
public class CNCEstadoDeudaOtrosPasivosMB extends ReportePeriodos {

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@PostConstruct
	public void init() {
		jasperReporteName = "CNC_EstadoDeudaOtrosPasivos";
		endFilename = jasperReporteName + ".pdf";
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

	public String getQuery() {
		StringBuilder query = new StringBuilder();

		String cargoActual = StringUtils.EMPTY;
		String abonoActual = StringUtils.EMPTY;
		String saldoAnterior = StringUtils.EMPTY;

		for (int i = 1; i <= getMesSelected(); i++) {
			cargoActual += "C.CARGOS_" + i + " +";
			abonoActual += "C.ABONOS_" + i + " +";
			saldoAnterior += "EA.SDO_FINAL_" + i + " +";
		}

		abonoActual = abonoActual.substring(0, abonoActual.length() - 2);
		cargoActual = cargoActual.substring(0, cargoActual.length() - 2);
		saldoAnterior = saldoAnterior.substring(0, saldoAnterior.length() - 2);

		query.append("SELECT 	SUM(SALDO_ANTERIOR) PERIODO_ANTERIOR, \n"
				+ " 		SUM(SALINI - CARGOS_ACTUAL + ABONOS_ACTUAL) PERIODO_ACTUAL\n" + "	FROM (\n"
				+ "		SELECT 	C.CUENTA, \n" + "C.SALINI, \n" + saldoAnterior + " SALDO_ANTERIOR,\n"
				+ "				" + abonoActual + " ABONOS_ACTUAL,\n" + cargoActual + " CARGOS_ACTUAL\n"
				+ "			FROM CUENTA C \n"
				+ "				LEFT JOIN TW_EJERCICIO_ANTERIOR EA ON 	C.CUENTA = EA.CUENTA AND \n"
				+ "														C.SCTA = EA.SCTA AND \n"
				+ "														C.SSCTA = EA.SSCTA AND \n"
				+ "														C.SSSCTA = EA.SSSCTA AND \n"
				+ "														C.SSSSCTA = EA.SSSSCTA \n"
				+ "			WHERE 	C.CUENTA >= '2000' \n" + "				AND C.CUENTA <= '2999' \n"
				+ "				AND C.SCTA ='' \n" + "	AND C.IDSECTOR = " + this.getUserDetails().getIdSector()
				+ "             	AND (SUBSTR(C.CUENTA,3,2) ='00' OR SUBSTR(C.CUENTA,4,1) <>'0')\n" + "	) T1\n"
				+ "WHERE 	SALINI <> 0 \n" + "	OR 	SALDO_ANTERIOR<>0  \n" + "	OR 	CARGOS_ACTUAL <> 0 "
				+ "	OR	ABONOS_ACTUAL <> 0 ");

		System.out.println("sql: " + query.toString());
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
}
