
package com.gem.sistema.web.bean;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcPeriodo;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.catalogos.ValidatePolicyService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

@ManagedBean
@ViewScoped
public class CNCConciliacionIngresoPresupuestalContableMB extends BaseDirectReport {

	private static final String DOWNLOAD_XLS = " jQuery('#form1\\\\:downXls').click();";

	private Integer mesInput;
	@ManagedProperty("#{validatePolicyService}")
	private ValidatePolicyService validatePolicyService;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{tcPeriodoRepositoy}")
	protected TcPeriodoRepositoy periodoRepositoy;

	@PostConstruct
	public void init() {
		LOGGER.info(":: En postconsruct CNCSituacionFinancieraMB ");
		jasperReporteName = "concilacionIngresosPresupuestosContables";
		endFilename = jasperReporteName + ".pdf";

	}

	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	public Map<String, Object> getParametersReports() {

		Map<String, Object> paramsReport = new java.util.HashMap<String, Object>();
		Integer idSector = this.getUserDetails().getIdSector();
		TrPuestoFirma firma = new TrPuestoFirma();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TcPeriodo periodo = periodoRepositoy.findByTipoPeriodoAndPeriodo(1, mesInput);

		paramsReport.put("year", conctb.getAnoemp());
		paramsReport.put("lastDay", getLastDayByAnoEmp(mesInput, conctb.getAnoemp()));
		paramsReport.put("monthName", periodo.getDescripcion());
		paramsReport.put("imagePath", conctb.getImagePathLeft());
		paramsReport.put("nombreDependencia", conctb.getNomDep());

		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F12.getValue());
		paramsReport.put("firmaP1", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN1", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F13.getValue());
		paramsReport.put("firmaP2", firma.getPuesto().getPuesto());
		paramsReport.put("firmaN2", firma.getNombre());

		paramsReport.put("sql", getQuery());

		return paramsReport;
	}

	public void downloadXls() {
		if (this.validatePolicyService.isOpenMonth(mesInput, null,
				this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.validatePolicyService.isAfectMonth(mesInput, null, this.getUserDetails().getIdSector());
			this.validatePolicyService.existPolices(mesInput, null, this.getUserDetails().getIdSector());

			RequestContext.getCurrentInstance().execute(DOWNLOAD_XLS);
		}
	}

	public void viewPdf() {
		if (this.validatePolicyService.isOpenMonth(mesInput, null,
				this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			this.validatePolicyService.isAfectMonth(mesInput, null, this.getUserDetails().getIdSector());
			this.validatePolicyService.existPolices(mesInput, null, this.getUserDetails().getIdSector());
			this.createFilePdfInline();
			;
		}
	}

	private String getQuery() {
		StringBuilder query = new StringBuilder();
		String abonos = getSumaCargoOAbono("ABONOS_");
		String cargos = getSumaCargoOAbono("CARGOS_");

		query.append(" SELECT	1 TIPO, ABONOS - CARGOS CIFRA FROM(\n" + "	SELECT 	" + cargos + " CARGOS, " + abonos
				+ " ABONOS" + "		FROM CUENTA WHERE CUENTA='8150' AND SCTA='' \n" + ")T1 UNION ALL\n"
				+ "SELECT 	3, ABONOS - CARGOS FROM(\n" + "	SELECT 	" + cargos + " CARGOS, " + abonos + " ABONOS\n"
				+ "		FROM CUENTA WHERE CUENTA = '8150' AND SCTA='0000004399' AND SSCTA='000000000000001' AND SSSCTA='0001' AND SSSSCTA='0008'\n"
				+ " )T1 UNION ALL\n" + "SELECT 2, 0.0 FROM SYSIBM.SYSDUMMY1\n" + "UNION ALL\n"
				+ "SELECT 4, 0.0 FROM SYSIBM.SYSDUMMY1 ORDER BY 1 ");
		System.out.println("query: " + query.toString());
		return query.toString();
	}

	private String getSumaCargoOAbono(String nombre) {
		String suma = "";

		for (int i = 1; i <= this.mesInput; i++) {
			suma += " " + nombre + i + " +";
		}
		return suma.substring(0, suma.length() - 1);
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

	public ValidatePolicyService getValidatePolicyService() {
		return validatePolicyService;
	}

	public void setValidatePolicyService(ValidatePolicyService validatePolicyService) {
		this.validatePolicyService = validatePolicyService;
	}

	public Integer getMesInput() {
		return mesInput;
	}

	public void setMesInput(Integer mesInput) {
		this.mesInput = mesInput;
	}

	public TcPeriodoRepositoy getPeriodoRepositoy() {
		return periodoRepositoy;
	}

	public void setPeriodoRepositoy(TcPeriodoRepositoy periodoRepositoy) {
		this.periodoRepositoy = periodoRepositoy;
	}

}
