package com.gem.sistema.web.bean;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.Firmas;
import com.gem.sistema.business.domain.Pm4911;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.FirmasRepository;
import com.gem.sistema.business.service.catalogos.Pm4911Service;
import com.gem.sistema.business.service.reportador.ReportValidationException;

// TODO: Auto-generated Javadoc
/**
 * The Class IndicadorTasaRecaudacionImpuestoPredialMB.
 */
@ManagedBean
@ViewScoped
public class IndicadorTasaRecaudacionImpuestoPredialMB extends BaseDirectReport {

	/** The firmas repository. */
	@ManagedProperty("#{firmasRepository}")
	private FirmasRepository firmasRepository;

	/** The service. */
	@ManagedProperty("#{pm4911Service}")
	private Pm4911Service service;

	/** The conctb repository. */
	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	/** The master list. */
	private List<Pm4911> masterList;

	/** The selected. */
	private Pm4911 selected = new Pm4911();
	
	/** The mes. */
	private Integer mes;
	
	/** The current page. */
	private int currentPage = 0;
	
	/** The editando. */
	private boolean editando = false;
	
	/** The selectable month. */
	private List<Integer> selectableMonth;
	
	/** The valid months. */
	private List<Integer> validMonths = new ArrayList<Integer>();
	
	/** The conctb. */
	private Conctb conctb;

	/**
	 * Inits the.
	 */
	@PostConstruct
	public void init() {

		LOGGER.info(":: En postconsruct IndicadorTasaRecaudacionImpuestoPredialMB ");
		// reportId = 2;
		jasperReporteName = "IndicadordeTasadeRecaudaciondelImpuestoPredial";
		endFilename = jasperReporteName + ".pdf";

		inicializar();

	}

	/* (non-Javadoc)
	 * @see com.gem.sistema.web.bean.BaseDirectReport#getParametersReports()
	 */
	public Map<String, Object> getParametersReports() {
		Firmas firmas = firmasRepository.findAllByIdsector(this.getUserDetails().getIdSector());
		conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());
		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		parameters.put("SECTOR", getUserDetails().getIdSector());
		parameters.put("MES", mes);
		parameters.put("imagen", getUserDetails().getPathImgCab1());
		parameters.put("NoFIRMAS", 3);
		parameters.put("nomMunicipio", firmas.getCampo1());
		parameters.put("clveMunicipio", conctb.getClave());
		parameters.put("anio", firmas.getCampo3());
		parameters.put("firmaP1", firmas.getL4());
		parameters.put("firmaP2", firmas.getL5());
		parameters.put("firmaP3", firmas.getL3());
		parameters.put("firmaN1", firmas.getN4());
		parameters.put("firmaN2", firmas.getN5());
		parameters.put("firmaN3", firmas.getN3());
		return parameters;
	}

	/**
	 * Inicializar.
	 */
	public void inicializar() {
		for (int i = 1; i <= 12; i++) {
			validMonths.add(i);
		}
		masterList = service.findAllBySector(getUserDetails().getIdSector());
		if (masterList == null || masterList.isEmpty()) {
			masterList = new ArrayList<Pm4911>();
			Pm4911 entity = new Pm4911();
			entity.setMensual(1);
			entity.setIdsector(getUserDetails().getIdSector());
			entity.setCapturo(getUserDetails().getUsername());
			entity.setFeccap(new Date());
			entity.setIdRef(getUserDetails().getIdSector() - 1l);
			entity.setUserid(getUserDetails().getUsername());
			masterList.add(entity);
		}
		currentPage = 0;
		actualizaSeleccionado();
	}

	// private void selectLastRecord() {
	// int lastRecord = masterList.size() - 1;
	// if(currentPage == lastRecord){
	// actualizaSeleccionado();
	// }
	// RequestContext.getCurrentInstance().execute("PF('dataGridPm4911').getPaginator().setPage("
	// + lastRecord + ");");
	// }

	/**
	 * Adicionar.
	 */
	public void adicionar() {
		Pm4911 last = masterList.isEmpty() ? null : masterList.get(masterList.size() - 1);

		if (Objects.nonNull(last) && last.getId() == null) {
			masterList.remove(last);
		}

		// Si no existe registros este años crea el mes 1
		Pm4911 entity = new Pm4911();
		entity.setMensual(1);
		entity.setAcuncpip(0);
		entity.setAcutcrpip(0);

		List<Integer> currentMonths = masterList.stream().map(Pm4911::getMensual).collect(Collectors.toList());
		selectableMonth = currentMonths.isEmpty() ? validMonths
				: validMonths.stream().filter(p -> !currentMonths.contains(p)).collect(Collectors.toList());

		if (selectableMonth.isEmpty()) {
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Solo puede agregar hasta el mes 12.");
			return;
		}

		selected = entity;
		editando = true;
		entity.setMensual(selectableMonth.get(0));

		masterList.add(entity);
		RequestContext.getCurrentInstance()
				.execute("PF('dataGridPm4911').getPaginator().setPage(" + (masterList.size() - 1) + ");");
		if (currentPage == -1) {
			currentPage = 0;
			actualizaSeleccionado();
		}
	}

	/**
	 * Modificar.
	 */
	public void modificar() {
		editando = true;
	}

	/**
	 * Salvar.
	 */
	public void salvar() {
		Pm4911 nuevo = masterList.get(currentPage);
		if (editando && validarNonNull(nuevo)) {
			editando = false;

			// Actualiza algunos campos autogenerados
			nuevo.setUserid(getUserDetails().getUsername());
			nuevo.setCapturo(getUserDetails().getUsername());
			nuevo.setIdsector(getUserDetails().getIdSector());
			nuevo.setIdRef(getUserDetails().getIdSector() - 1l);
			nuevo.setFeccap(new Date());

			boolean modificando = selected != null && selected.getId() != null && selected.getId() > 0;
			String msg = "El registro se ha " + (modificando ? "modificado" : "insertado") + " con éxito!";
			boolean isNotLastMonth = masterList.stream().anyMatch(p -> p.getMensual() > nuevo.getMensual());

			sortMasterList();
			RequestContext.getCurrentInstance()
					.execute("PF('dataGridPm4911').getPaginator().setPage(" + (selected.getMensual() - 1) + ");");

			// Si esta modificando y no es el último registro se debe recalcular
			// desde el mes modificado has el último mes
			if (isNotLastMonth) {
				for (int i = masterList.indexOf(selected); i < masterList.size(); i++) {
					Pm4911 currentRecord = masterList.get(i);
					calculaAcumulados(currentRecord);
					Pm4911 pm4911 = service.save(currentRecord);
					masterList.set(i, pm4911);
					if (i == masterList.indexOf(selected)) {
						selected = pm4911;
					}
				}
			} else {
				calculaAcumulados(nuevo);
				Pm4911 pm4911 = service.save(nuevo);
				masterList.set(currentPage, pm4911);
			}
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, StringUtils.EMPTY, msg);
			FacesContext.getCurrentInstance().addMessage(null, message);
		}

	}

	/**
	 * Cancelar.
	 */
	public void cancelar() {
		editando = false;
		if (selected.getId() == null) {
			masterList.remove(selected);
			currentPage--;
			actualizaSeleccionado();
		}
	}

	/**
	 * Validar non null.
	 *
	 * @param nuevo the nuevo
	 * @return true, if successful
	 */
	private boolean validarNonNull(Pm4911 nuevo) {
		if (null == nuevo.getObsncpip())
			nuevo.setObsncpip("");
		
		if (null == nuevo.getObstcrpip())
			nuevo.setObstcrpip("");
		
		return true;
	}

	/**
	 * Sort master list.
	 */
	private void sortMasterList() {
		Collections.sort(masterList, new Comparator<Pm4911>() {
			@Override
			public int compare(Pm4911 h1, Pm4911 h2) {
				return h1.getMensual().compareTo(h2.getMensual());
			}
		});
	}

	/**
	 * Calcula acumulados.
	 *
	 * @param currentRecord the current record
	 */
	private void calculaAcumulados(Pm4911 currentRecord) {
		Pm4911 beforeSelected;
		// Agarramos el registro anterior al registro seleccionado
		int lastIndex = masterList.indexOf(currentRecord) - 1;
		beforeSelected = lastIndex > -1 ? masterList.get(lastIndex) : null;
		// Si no existe anterior instanciamos un nuevo registro
		if (Objects.isNull(beforeSelected)) {
			if (Objects.isNull(beforeSelected)) {
				beforeSelected = new Pm4911();
				beforeSelected.setAcuncpip(0);
				beforeSelected.setAcutcrpip(0);
			}
		}
		if (Objects.isNull(currentRecord.getNcpip()))
			currentRecord.setNcpip(0);
		if (Objects.isNull(currentRecord.getTcrpip()))
			currentRecord.setTcrpip(0);
		currentRecord.setAcuncpip(beforeSelected.getAcuncpip() + currentRecord.getNcpip());
		currentRecord.setAcutcrpip(beforeSelected.getAcutcrpip() + currentRecord.getTcrpip());

	}

	/**
	 * Cambiar pagina.
	 *
	 * @param event the event
	 */
	public void cambiarPagina(PageEvent event) {
		currentPage = event.getPage();
		actualizaSeleccionado();
	}

	/**
	 * Actualiza seleccionado.
	 */
	private void actualizaSeleccionado() {
		if (!masterList.isEmpty()) {
			selected = masterList.get(currentPage);
			mes = selected.getMensual();
		} else {
			inicializar();
		}
	}

	/**
	 * Reset.
	 */
	public void reset() {
		if (Objects.isNull(selected.getId())) {
			selected.setNcpip(null);
			selected.setTcrpip(null);
			selected.setObsncpip("");
			selected.setObstcrpip("");
		} else {
			selected = service.findById(selected.getId());
			masterList.set(currentPage, selected);
		}
	}

	/**
	 * Delete.
	 */
	public void delete() {
		service.delete(selected.getId());
		masterList.remove(currentPage);
		currentPage = currentPage > 0 ? currentPage - 1 : 0;
		for (int i = currentPage; i >= 0 && i < masterList.size(); i++) {
			Pm4911 currentRecord = masterList.get(i);
			if (Objects.nonNull(currentRecord) && currentRecord.getId() > 0) {
				calculaAcumulados(currentRecord);
				Pm4911 pm4911 = service.save(currentRecord);
				masterList.set(i, pm4911);
				if (i == currentPage) {
					selected = pm4911;
				}
			}
		}
		actualizaSeleccionado();
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "El registro se eliminó de manera correcta");
	}

	/**
	 * Gets the mes.
	 *
	 * @return the mes
	 */
	public Integer getMes() {
		return mes;
	}

	/**
	 * Sets the mes.
	 *
	 * @param mes the new mes
	 */
	public void setMes(Integer mes) {
		this.mes = mes;
	}

	/**
	 * Gets the firmas repository.
	 *
	 * @return the firmas repository
	 */
	public FirmasRepository getFirmasRepository() {
		return firmasRepository;
	}

	/**
	 * Sets the firmas repository.
	 *
	 * @param firmasRepository the new firmas repository
	 */
	public void setFirmasRepository(FirmasRepository firmasRepository) {
		this.firmasRepository = firmasRepository;
	}

	/**
	 * Gets the master list.
	 *
	 * @return the master list
	 */
	public List<Pm4911> getMasterList() {
		return masterList;
	}

	/**
	 * Sets the master list.
	 *
	 * @param masterList the new master list
	 */
	public void setMasterList(List<Pm4911> masterList) {
		this.masterList = masterList;
	}

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public Pm4911Service getService() {
		return service;
	}

	/**
	 * Sets the service.
	 *
	 * @param service the new service
	 */
	public void setService(Pm4911Service service) {
		this.service = service;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Checks if is editando.
	 *
	 * @return true, if is editando
	 */
	public boolean isEditando() {
		return editando;
	}

	/**
	 * Sets the editando.
	 *
	 * @param editando the new editando
	 */
	public void setEditando(boolean editando) {
		this.editando = editando;
	}

	/**
	 * Gets the selected.
	 *
	 * @return the selected
	 */
	public Pm4911 getSelected() {
		return selected;
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected the new selected
	 */
	public void setSelected(Pm4911 selected) {
		this.selected = selected;
	}

	/**
	 * Gets the selectable month.
	 *
	 * @return the selectable month
	 */
	public List<Integer> getSelectableMonth() {
		return selectableMonth;
	}

	/**
	 * Sets the selectable month.
	 *
	 * @param selectableMonth the new selectable month
	 */
	public void setSelectableMonth(List<Integer> selectableMonth) {
		this.selectableMonth = selectableMonth;
	}

	/**
	 * Gets the conctb repository.
	 *
	 * @return the conctb repository
	 */
	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	/**
	 * Sets the conctb repository.
	 *
	 * @param conctbRepository the new conctb repository
	 */
	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
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

	/* (non-Javadoc)
	 * @see com.gem.sistema.web.bean.BaseDirectReport#generaReporteSimple(int)
	 */
	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		return null;
	}

}
