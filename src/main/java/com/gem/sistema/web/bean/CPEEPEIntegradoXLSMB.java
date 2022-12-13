package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getLastDay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcMes;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;

// TODO: Auto-generated Javadoc
/**
 * The Class CPEEPEIntegradoXLSMB.
 */
@ManagedBean(name = "cPEEPEIntegradoXLSMB")
@ViewScoped

public class CPEEPEIntegradoXLSMB extends BaseDirectReport {

	/** The mes. */
	private String mes;

	/** The conctb. */
	private Conctb conctb;

	/** The list mes. */
	private List<TcMes> listMes;

	/** The type file. */
	private String typeFile;

	/** The tc mes repository. */
	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{tcPeriodoRepositoy}")
	private TcPeriodoRepositoy periodoRepositoy;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	// Se agregan getters a setters

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

	public TcPeriodoRepositoy getPeriodoRepositoy() {
		return periodoRepositoy;
	}

	public void setPeriodoRepositoy(TcPeriodoRepositoy periodoRepositoy) {
		this.periodoRepositoy = periodoRepositoy;
	}

	/**
	 * Gets the mes.
	 *
	 * @return the mes
	 */
	public String getMes() {
		return mes;
	}

	/**
	 * Sets the mes.
	 *
	 * @param mes the new mes
	 */
	public void setMes(String mes) {
		this.mes = mes;
	}

	/**
	 * Gets the conctb.
	 *
	 * @return the conctb
	 */
	public Conctb getConctb() {
		return conctb;
	}

	/**
	 * Sets the conctb.
	 *
	 * @param conctb the new conctb
	 */
	public void setConctb(Conctb conctb) {
		this.conctb = conctb;
	}

	/**
	 * Gets the list mes.
	 *
	 * @return the list mes
	 */
	public List<TcMes> getListMes() {
		return listMes;
	}

	/**
	 * Sets the list mes.
	 *
	 * @param listMes the new list mes
	 */
	public void setListMes(List<TcMes> listMes) {
		this.listMes = listMes;
	}

	/**
	 * Gets the type file.
	 *
	 * @return the type file
	 */
	public String getTypeFile() {
		return typeFile;
	}

	/**
	 * Sets the type file.
	 *
	 * @param typeFile the new type file
	 */
	public void setTypeFile(String typeFile) {
		this.typeFile = typeFile;
	}

	/**
	 * Gets the tc mes repository.
	 *
	 * @return the tc mes repository
	 */
	public TcMesRepository getTcMesRepository() {
		return tcMesRepository;
	}

	/**
	 * Sets the tc mes repository.
	 *
	 * @param tcMesRepository the new tc mes repository
	 */
	public void setTcMesRepository(TcMesRepository tcMesRepository) {
		this.tcMesRepository = tcMesRepository;
	}


	/**
	 * Inits the.
	 */
	@PostConstruct
	private void init() {
		jasperReporteName = "IntegradoEXLS";
		endFilename = jasperReporteName + ".pdf";

		listMes = tcMesRepository.findAll();
		if (!CollectionUtils.isEmpty(listMes))
			mes = listMes.get(0).getMes();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Integer idSector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(idSector);
		TrPuestoFirma firma = new TrPuestoFirma();

		parameters.put("pImg", conctb.getImagePathLeft());
		parameters.put("pNomMun", this.getUserDetails().getMunicipio());
		parameters.put("pClave", conctb.getClave());
		parameters.put("pLastDay", getLastDay(Integer.valueOf(mes)));
		parameters.put("pMes", tcMesRepository.findByMes(mes).getDescripcion());
		parameters.put("pAnio", conctb.getAnoemp());

		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F27.getValue());
		parameters.put("pL1", firma.getPuesto().getPuesto());
		parameters.put("pN1", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F05.getValue());
		parameters.put("pL2", firma.getPuesto().getPuesto());
		parameters.put("pN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(idSector, 0L, ConstantsClaveEnnum.CLAVE_F03.getValue());
		parameters.put("pL3", firma.getPuesto().getPuesto());
		parameters.put("pN3", firma.getNombre());

		parameters.put("pSql", this.generaQuery(this.getUserDetails().getIdSector(), Integer.valueOf(mes)));

		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#generaReporteSimple(int)
	 */
	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Genera query.
	 *
	 * @param idsector the idsector
	 * @param mes      the mes
	 * @return the string
	 */
	public String generaQuery(int idsector, int mes) {
		StringBuilder sSql = new StringBuilder();
		StringBuilder ayto = new StringBuilder();
		StringBuilder agua = new StringBuilder();
		StringBuilder dif = new StringBuilder();
		StringBuilder deporte = new StringBuilder();
		StringBuilder mavici = new StringBuilder();

		sSql.append("SELECT T1.PARTIDA,").append("T1.NOMGAS,").append(" T1.AYTTO,").append(" T1.AGUA,")
				.append(" T1.DIF,").append(" T1.DEPORTE,").append("T1.MAVICI,")
				.append(" (T1.AYTTO +  T1.AGUA + T1.DIF + T1.DEPORTE + T1.MAVICI) TOTAL")
				.append(" FROM (SELECT I.PARTIDA,").append(" N.NOMGAS,");

		for (int i = 1; i <= mes; i++) {

			ayto.append(" I.AYTTOTOEJE1_" + i + "+");
			agua.append(" I.AGUATOEJE1_" + i + "+");
			dif.append(" I.DIFTOEJE1_" + i + "+");
			deporte.append(" I.DEPORTETOEJE1_" + i + "+");
			mavici.append(" I.MAVICITOEJE1_" + i + "+");

		}

		sSql.append(" SUM(" + ayto.substring(1, ayto.length() - 1)).append(" )AYTTO,")
				.append(" SUM(" + agua.substring(1, agua.length() - 1)).append(" ) AGUA,")
				.append(" SUM(" + dif.substring(1, dif.length() - 1)).append(" ) DIF,")
				.append(" SUM(" + deporte.substring(1, deporte.length() - 1)).append(" ) DEPORTE,")
				.append(" SUM(" + mavici.substring(1, mavici.length() - 1)).append(" )MAVICI")
				.append(" FROM INTEGRADOE I  ")
				.append(" JOIN NATGAS N ON N.CLVGAS = I.PARTIDA AND N.IDSECTOR = I.IDSECTOR")
				.append(" WHERE I.IDSECTOR=" + idsector).append(" GROUP BY I.PARTIDA, N.NOMGAS")
				.append(" ORDER BY I.PARTIDA)T1");

		return sSql.toString();

	}

}
