package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcMes;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.catalogos.ValidatePolicyService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;

// TODO: Auto-generated Javadoc
/**
 * The Class Reporte91GraficoMB.
 */
@ManagedBean(name = "reporte91GraficoMB")
@ViewScoped
public class Reporte91GraficoMB extends BaseDirectReport {

	/** The mes ini. */
	private String mesIni;

	/** The mes fin. */
	private String mesFin;

	/** The cuenta. */
	private String cuenta;

	/** The partida inicial. */
	private String partidaInicial;

	/** The partida final. */
	private String partidaFinal;

	/** The num firmas. */
	private Integer numFirmas;

	/** The list mes. */
	private List<TcMes> listMes;

	/** The tc mes repository. */
	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	@ManagedProperty("#{validatePolicyService}")
	private ValidatePolicyService validatePolicyService;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	public ValidatePolicyService getValidatePolicyService() {
		return validatePolicyService;
	}

	public void setValidatePolicyService(ValidatePolicyService validatePolicyService) {
		this.validatePolicyService = validatePolicyService;
	}

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {
		jasperReporteName = (this.getUserDetails().getIdSector() == 1) ? "cuentaPartidaMun" : "cuentaxpartida";
		// jasperReporteName = "cuentaxpartida";
		endFilename = jasperReporteName + ".pdf";

		listMes = tcMesRepository.findAll();
		if (!CollectionUtils.isEmpty(listMes)) {
			mesIni = listMes.get(0).getMes();
			mesFin = listMes.get(0).getMes();
		}
		cuenta = "1111";
		partidaInicial = "1111";
		partidaFinal = "9999";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parametrs = new HashMap<String, Object>();
		TrPuestoFirma firma = null;
		Integer sector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(sector);

		parametrs.put("year", conctb.getAnoemp());
		parametrs.put("lastDayMonthF", getLastDayByAnoEmp(Integer.valueOf(mesFin), conctb.getAnoemp()));
		parametrs.put("nameMonthI", tcMesRepository.findByMes(mesIni).getDescripcion());
		parametrs.put("nameMonthF", tcMesRepository.findByMes(mesFin).getDescripcion());
		parametrs.put("entidadName", conctb.getNomDep());
		parametrs.put("imagePath", conctb.getImagePathLeft());
		parametrs.put("cuenta", cuenta);
		parametrs.put("mesI", Integer.valueOf(mesIni));
		parametrs.put("mesF", Integer.valueOf(mesFin));
		parametrs.put("partidaI", partidaInicial);
		parametrs.put("partidaF", partidaFinal);
		parametrs.put("idSector", sector);
		if (this.getUserDetails().getIdSector() == 1) {
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F27.getValue());
			parametrs.put("firmaL1", firma.getPuesto().getPuesto());
			parametrs.put("firmaN1", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F28.getValue());
			parametrs.put("firmaL2", firma.getPuesto().getPuesto());
			parametrs.put("firmaN2", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F05.getValue());
			parametrs.put("firmaL3", firma.getPuesto().getPuesto());
			parametrs.put("firmaN3", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F03.getValue());
			parametrs.put("firmaL4", firma.getPuesto().getPuesto());
			parametrs.put("firmaN4", firma.getNombre());

		} else {
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F04.getValue());
			parametrs.put("firmaL1", firma.getPuesto().getPuesto());
			parametrs.put("firmaN1", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F05.getValue());
			parametrs.put("firmaL2", firma.getPuesto().getPuesto());
			parametrs.put("firmaN2", firma.getNombre());
			firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L,
					ConstantsClaveEnnum.CLAVE_F27.getValue());
			parametrs.put("firmaL3", firma.getPuesto().getPuesto());
			parametrs.put("firmaN3", firma.getNombre());
		}
		parametrs.put("nFirmas", numFirmas);
		parametrs.put("query", this.getQuery(sector, conctb.getAnoemp()));

		return parametrs;
	}

	private String getQuery(Integer sector, Integer anio) {
		StringBuilder query = new StringBuilder();
		String partida = "";
		String direcion = "";
		String condicion = "";
		if ((partidaInicial != null && !partidaInicial.equals(""))
				&& (partidaFinal != null && !partidaFinal.equals(""))) {
			partida = "AND POL.SSSCTA BETWEEN '" + partidaInicial + "' AND '" + partidaFinal+"'";
			direcion = "AND POLI.SSSCTA BETWEEN '" + partidaInicial + "' AND '" + partidaFinal+"'";
			condicion = "AND P.SSSCTA BETWEEN '" + partidaInicial + "' AND '" + partidaFinal+"'";
		}

		query.append(" SELECT	P.CUENTA,").append(" 		P.SCTA,").append(" 		P.SSCTA,")
				.append(" 		P.SSSCTA,").append(" 	    NVL(P.SSSSCTA,'')SSSSCTA, ").append(" 		P.TIPPOL,")
				.append(" 		E.FECPOL,").append(" 		P.MESPOL,").append(" 		P.CONPOL,")
				.append(" 		P.RENPOL,").append(" 		P.CONCEP,").append(" 		P.CANPOL,")
				.append(" 		P.CANPOLH,").append(" 		P.REFPOL,").append(" 		(SELECT COUNT(1) ")
				.append(" 				FROM POLIDE POL ").append(" 			WHERE	POL.IDSECTOR = P.IDSECTOR ")
				.append(" 				AND POL.MESPOL BETWEEN " + mesIni + " AND " + mesFin+" ").append(partida)
				.append(" 				AND POL.CUENTA = P.CUENTA ").append(" 				AND  POL.SCTA =P.SCTA ")
				.append(" 				AND POL.SSCTA = P.SSCTA").append(" 				AND POL.SSSCTA = P.SSSCTA")
				.append(" 				AND NVL(POL.SSSSCTA,'')=NVL(P.SSSSCTA,'')").append(" 		) ROWSPARTIDA,")
				.append(" 		ROW_NUMBER() OVER (PARTITION BY P.CUENTA,P.SCTA,P.SSCTA,P.SSSCTA,NVL(P.SSSSCTA,'')) ROWPARTIDA,")
				.append(" 		(SELECT COUNT(1) ").append(" 				FROM POLIDE POLI ")
				.append(" 			WHERE 	POLI.IDSECTOR =P.IDSECTOR ")
				.append(" 				AND POLI.MESPOL BETWEEN " + mesIni + " AND " + mesFin+" ").append(direcion)
				.append(" 				AND POLI.CUENTA = P.CUENTA ").append(" 				AND POLI.SCTA =P.SCTA")
				.append(" 		) ROWSDIRECCION,")
				.append(" 		ROW_NUMBER() OVER (PARTITION BY P.CUENTA,P.SCTA) ROWDIRECCION")
				.append(" 	FROM POLIDE P")
				.append(" INNER JOIN POLIEN E ON P.ANOPOL=E.ANOPOL AND P.IDSECTOR=E.IDSECTOR AND P.MESPOL=E.MESPOL AND P.CONPOL=E.CONPOL AND P.TIPPOL=E.TIPPOL ")
				.append(" WHERE	P.CUENTA=" + cuenta)
				.append(" 	AND P.MESPOL BETWEEN " + mesIni + " AND " + mesFin).append(" 	AND P.ANOPOL=" + anio)
				.append(" 	AND P.IDSECTOR=" + sector+" ").append(condicion)
				.append(" ORDER BY P.CUENTA,P.SCTA,P.SSCTA,P.SSSCTA");
		System.out.println(query);
		return query.toString();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gem.sistema.web.bean.BaseDirectReport#createFilePdfInline()
	 */
	@Override
	public void createFilePdfInline() {
		if (isValid()) {
			super.createFilePdfInline();
		}
	}

	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		if (Integer.valueOf(mesIni) > Integer.valueOf(mesFin)) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, StringUtils.EMPTY,
					"El mes inicial no puede ser mayor al mes final.");
			FacesContext.getCurrentInstance().addMessage(null, message);
			return false;
		} else if ((partidaInicial == null && partidaFinal == null) || (partidaInicial != null
				&& !partidaInicial.equals("") && partidaFinal != null && !partidaFinal.equals(""))) {
			if (partidaInicial != null && !partidaInicial.equals("") && partidaFinal != null
					&& !partidaFinal.equals("")) {
				if (Integer.valueOf(partidaInicial) > Integer.valueOf(partidaFinal)) {
					FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, StringUtils.EMPTY,
							"La partida inicial no puede ser mayor a la partida final.");
					FacesContext.getCurrentInstance().addMessage(null, message);
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else if ((partidaInicial != null && !partidaInicial.equals(""))
				|| (partidaFinal != null && !partidaFinal.equals(""))) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, StringUtils.EMPTY,
					"La partida final y la partida inical pueden ser vacias o ambas deben de contener una partida valida");
			FacesContext.getCurrentInstance().addMessage(null, message);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Gets the mes ini.
	 *
	 * @return the mes ini
	 */
	public String getMesIni() {
		return mesIni;
	}

	/**
	 * Sets the mes ini.
	 *
	 * @param mesIni the new mes ini
	 */
	public void setMesIni(String mesIni) {
		this.mesIni = mesIni;
	}

	/**
	 * Gets the mes fin.
	 *
	 * @return the mes fin
	 */
	public String getMesFin() {
		return mesFin;
	}

	/**
	 * Sets the mes fin.
	 *
	 * @param mesFin the new mes fin
	 */
	public void setMesFin(String mesFin) {
		this.mesFin = mesFin;
	}

	/**
	 * Gets the cuenta.
	 *
	 * @return the cuenta
	 */
	public String getCuenta() {
		return cuenta;
	}

	/**
	 * Sets the cuenta.
	 *
	 * @param cuenta the new cuenta
	 */
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	/**
	 * Gets the partida inicial.
	 *
	 * @return the partida inicial
	 */
	public String getPartidaInicial() {
		return partidaInicial;
	}

	/**
	 * Sets the partida inicial.
	 *
	 * @param partidaInicial the new partida inicial
	 */
	public void setPartidaInicial(String partidaInicial) {
		this.partidaInicial = partidaInicial;
	}

	/**
	 * Gets the partida final.
	 *
	 * @return the partida final
	 */
	public String getPartidaFinal() {
		return partidaFinal;
	}

	/**
	 * Sets the partida final.
	 *
	 * @param partidaFinal the new partida final
	 */
	public void setPartidaFinal(String partidaFinal) {
		this.partidaFinal = partidaFinal;
	}

	/**
	 * Gets the num firmas.
	 *
	 * @return the num firmas
	 */
	public Integer getNumFirmas() {
		return numFirmas;
	}

	/**
	 * Sets the num firmas.
	 *
	 * @param numFirmas the new num firmas
	 */
	public void setNumFirmas(Integer numFirmas) {
		this.numFirmas = numFirmas;
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

	public void validateMonthInit() {
		this.validatePolicyService.isOpenMonth(Integer.valueOf(mesIni), null, this.getUserDetails().getIdSector());
	}

	public void validateMonthEnd() {
		this.validatePolicyService.isOpenMonth(Integer.valueOf(mesFin), null, this.getUserDetails().getIdSector());
	}

	public void viewPdf() throws ReportValidationException {
		if (this.validatePolicyService.isOpenMonth(Integer.valueOf(mesIni), null,
				this.getUserDetails().getIdSector()) == Boolean.TRUE) {
			if (this.validatePolicyService.isOpenMonth(Integer.valueOf(mesFin), null,
					this.getUserDetails().getIdSector()) == Boolean.TRUE) {
				this.createFilePdfInline();
			}
		}
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

}
