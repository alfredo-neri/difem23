package com.gem.sistema.web.bean;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;
import static com.roonin.utils.UtilDate.getDateSystem;
import static com.roonin.utils.UtilDate.getLastDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcPeriodo;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.dto.CompetenciaLaboralGenericDTO;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.catalogos.CompetenciasLaboralesService;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;
import com.gem.sistema.util.CreateFileUtils;
import com.google.gson.Gson;


/**
 * @author Alfredo Neri
 *
 */
@ManagedBean(name = "reporte142MB")
@ViewScoped
public class Reporte142MB extends BaseDirectReport {

	private static final String TABLE_NAME = "PM5811";
	private static final String JASPER_NAME = "CompetenciaLaboral";
	private String COMPETENCIA_NAME = "Director de Desarrollo Económico";
	private String TITLE_LABEL = "Título profesional en el área económico - administrativa";

	private List<TcPeriodo> listSemestres;
	private List<CompetenciaLaboralGenericDTO> listPm5811;
	private Integer semestre;
	private CompetenciaLaboralGenericDTO pm5811Selected;

	private boolean editando = false;
	private boolean bAdicionar = false;
	private boolean bModificar = false;
	private boolean bBorrar;
	private boolean bCancelar;
	private int currentPage = 0;
	private Boolean isNew = null;

	@ManagedProperty("#{competenciasLaboralesService}")
	private CompetenciasLaboralesService competenciasLaboralesService;

	@ManagedProperty("#{tcPeriodoRepositoy}")
	private TcPeriodoRepositoy periodoRepository;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	/** The tc mes repository. */
	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;
	
	@PostConstruct
	public void init() {

		jasperReporteName = JASPER_NAME;
		endFilename = jasperReporteName + ".pdf";

		listPm5811 = competenciasLaboralesService.findAll(TABLE_NAME);
		listSemestres = periodoRepository.findByTipoPeriodoOrderByPeriodo(6);
		bBorrar = false;

		if (CollectionUtils.isNotEmpty(listSemestres)) {
			semestre = listSemestres.get(0).getPeriodo();
		}

		if (CollectionUtils.isEmpty(listPm5811))
			bBorrar = true;
		bModificar = true;
		if (CollectionUtils.isNotEmpty(listPm5811)) {
			pm5811Selected = listPm5811.get(0);
			semestre = pm5811Selected.getSemestre();
			bModificar = false;
		}
		editando = false;
		currentPage = 0;
		bCancelar = true;

		this.addElement();
		blockBtnAdicionar();
	}

	public void blockBtnAdicionar() {

		if (listPm5811.size() >= 2) {
			bAdicionar = false;
		} else {
			bAdicionar = true;
		}
	}

	public void salvar() {
		CompetenciaLaboralGenericDTO nuevo = listPm5811.get(currentPage);

		nuevo.setIdanio(0);
		nuevo.setIdref(0);
		nuevo.setIdsector(this.getUserDetails().getIdSector());
		nuevo.setCapturo(this.getUserDetails().getUsername());
		bModificar = true;
		if (isNew) {
			Integer existe = this.competenciasLaboralesService.countSemestres(TABLE_NAME, nuevo.getSemestre());

			if (editando && this.notNull(nuevo) && existe == 0) {
				editando = false;

				this.competenciasLaboralesService.save(TABLE_NAME, nuevo);
				listPm5811 = this.competenciasLaboralesService.findAll(TABLE_NAME);
				bModificar = false;
				bCancelar = true;
				bBorrar = false;

				blockBtnAdicionar();
				generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Se ha guardado con exito el registro");
			} else {
				nuevo.setFechaing(StringUtils.EMPTY);
				generateNotificationFront(FacesMessage.SEVERITY_WARN, "Error!",
						"Ya existe un registro con el semestre " + nuevo.getSemestre());
			}
		} else if (!isNew) {
			this.notNull(nuevo);
			this.competenciasLaboralesService.update(TABLE_NAME, nuevo);
			this.listPm5811 = this.competenciasLaboralesService.findAll(TABLE_NAME);
			bModificar = false;
			editando = false;
			bCancelar = true;
			bBorrar = false;

			blockBtnAdicionar();
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "!Se ha modificado con exito el registro!");
		}
	}

	public void modificar() {
		pm5811Selected = listPm5811.get(currentPage);
		listPm5811.set(currentPage, pm5811Selected);

		editando = true;
		bCancelar = false;
		isNew = false;
		bBorrar = true;
	}

	public void reset() {
		listPm5811 = competenciasLaboralesService.findAll(TABLE_NAME);
		if (CollectionUtils.isEmpty(listPm5811)) {
			listPm5811.add(new CompetenciaLaboralGenericDTO());
		}
		editando = false;
		bBorrar = false;
		bCancelar = true;
	}

	public void clean() {
		if (listPm5811.get(currentPage).getIdrow() == 0) {

			listPm5811.get(currentPage).setSemestre(listSemestres.get(0).getPeriodo());
			listPm5811.get(currentPage).setExperiencia(StringUtils.EMPTY);
			listPm5811.get(currentPage).setFechaing(StringUtils.EMPTY);
			listPm5811.get(currentPage).setNombre(StringUtils.EMPTY);
			listPm5811.get(currentPage).setCargo(StringUtils.EMPTY);
			listPm5811.get(currentPage).setCertificacion(0);
			listPm5811.get(currentPage).setAntecedentes(0);
			listPm5811.get(currentPage).setConstancia(0);
			listPm5811.get(currentPage).setIssemym(0);
			listPm5811.get(currentPage).setTitulo(0);

		} else {
			listPm5811 = this.competenciasLaboralesService.findAll(TABLE_NAME);
		}

	}

	public void adicionar() {

		if (StringUtils.EMPTY == listPm5811.get(0).getFechaing()) {
			for (int i = 0; i < listPm5811.size(); i++) {
				listPm5811.remove(i);
			}
		}

		listPm5811.add(new CompetenciaLaboralGenericDTO(this.getUserDetails().getUsername(),
				this.getUserDetails().getIdSector()));

		editando = true;
		bCancelar = false;
		bBorrar = true;
		isNew = true;
		if (listPm5811.size() == 2) {
			RequestContext.getCurrentInstance()
					.execute("PF('dataGrid').paginator.setPage(" + (listPm5811.size() - 1) + ");");

		}
		if (listPm5811.size() > 2) {
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Solo puede agregar 2 semestres.");
			return;
		}
	}

	public void borrar() {
		if (listPm5811.size() == 1) {
			currentPage = 0;
		}

		pm5811Selected = listPm5811.get(currentPage);

		this.competenciasLaboralesService.deleteRow(TABLE_NAME, pm5811Selected.getIdrow());
		listPm5811 = this.competenciasLaboralesService.findAll(TABLE_NAME);

		if (this.listPm5811.isEmpty()) {
			this.listPm5811.add(new CompetenciaLaboralGenericDTO());
			if (StringUtils.isEmpty(listPm5811.get(0).getFechaing()))
				bBorrar = true;
			bModificar = true;
		}

		this.actualizaSeleccionado();
		this.blockBtnAdicionar();
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "El registro se eliminó de manera correcta");
	}

	public void cambiarPagina(PageEvent event) {
		currentPage = event.getPage();
		actualizaSeleccionado();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		Integer sector = this.getUserDetails().getIdSector();
		TrPuestoFirma firma = null;
		Conctb conctb = conctbRepository.findByIdsector(getUserDetails().getIdSector());
		String[] mesArray = this.getMonths(Integer.valueOf(semestre)).split(",");
		
		parameters.put("pNombreMunicipio", this.getUserDetails().getMunicipio());
		parameters.put("pNumMunicipio", conctb.getClave().substring(1));
		parameters.put("pDiaUltimo", getLastDay(Integer.valueOf(mesArray[1])));
		parameters.put("pNombreMes", tcMesRepository.findByMes(mesArray[1]).getDescripcion());
		parameters.put("pAnio", conctb.getAnoemp());
		parameters.put("pLogo", getUserDetails().getPathImgCab1());
		firma = this.puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F26.getValue());
		parameters.put("pFirmaLabel", firma.getPuesto().getPuesto());
		parameters.put("pFirmaName", firma.getNombre());
		parameters.put("competenciaName", COMPETENCIA_NAME.toUpperCase());
		parameters.put("titleLabel", TITLE_LABEL);
		return parameters;
	}

	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void generatePDF() {

		CompetenciaLaboralGenericDTO dto = null;

		for (CompetenciaLaboralGenericDTO genericDTO : listPm5811) {
			if (genericDTO.getSemestre() == semestre) {
				dto = genericDTO;
				break;
			}
		}

		if (dto != null) {
			Gson gson = new Gson();
			String jsonString = " {\"competencia\":[" + gson.toJson(dto) + "]}";
			String jsonName = TABLE_NAME + UUID.randomUUID() + ".json";
			createFilePdfInlineJson(CreateFileUtils.createJsonFile(jsonName, jsonString));
		} else {
			uuid = null;
			generateNotificationFront(FacesMessage.SEVERITY_WARN, "Warning!",
					"No existe información del semestre: " + semestre);
		}

	}

	public String getMonths(Integer semes) {
		if (semes == 1) {
			return "01,06";
		} else {
			return "07,12";
		}
	}

	private boolean notNull(CompetenciaLaboralGenericDTO pm3711) {
		if (null == pm3711.getFechaing())
			pm3711.setFechaing(getDateSystem().toString());

		if (null == pm3711.getTitulo())
			pm3711.setTitulo(0);

		if (null == pm3711.getCertificacion())
			pm3711.setCertificacion(0);

		if (null == pm3711.getIssemym())
			pm3711.setIssemym(0);

		if (null == pm3711.getConstancia())
			pm3711.setConstancia(0);

		if (null == pm3711.getAntecedentes())
			pm3711.setAntecedentes(0);

		if (null == pm3711.getExperiencia())
			pm3711.setExperiencia(StringUtils.EMPTY);

		return true;
	}

	private void actualizaSeleccionado() {
		if (CollectionUtils.isNotEmpty(listPm5811)) {

			pm5811Selected = listPm5811.get(listPm5811.size() - 1);
			semestre = pm5811Selected.getSemestre();
		}

	}

	public void addElement() {
		CompetenciaLaboralGenericDTO dto = new CompetenciaLaboralGenericDTO();
		dto.setCapturo(this.getUserDetails().getUsername());
		dto.setIdsector(this.getUserDetails().getIdSector());
		salida: for (CompetenciaLaboralGenericDTO pm : listPm5811) {
			if (StringUtils.isEmpty(pm.getFechaing())) {
				listPm5811.remove(pm);
				break salida;
			}
		}
		if (CollectionUtils.isEmpty(listPm5811)) {

			listPm5811 = new ArrayList<CompetenciaLaboralGenericDTO>();
			listPm5811.add(0, dto);
		}
	}

	public String getCOMPETENCIA_NAME() {
		return COMPETENCIA_NAME;
	}

	public String getTITLE_LABEL() {
		return TITLE_LABEL;
	}

	public List<TcPeriodo> getListSemestres() {
		return listSemestres;
	}

	public void setListSemestres(List<TcPeriodo> listSemestres) {
		this.listSemestres = listSemestres;
	}

	public List<CompetenciaLaboralGenericDTO> getListPm5811() {
		return listPm5811;
	}

	public void setListPm5811(List<CompetenciaLaboralGenericDTO> listPm5811) {
		this.listPm5811 = listPm5811;
	}

	public Integer getSemestre() {
		return semestre;
	}

	public void setSemestre(Integer semestre) {
		this.semestre = semestre;
	}

	public CompetenciaLaboralGenericDTO getPm5811Selected() {
		return pm5811Selected;
	}

	public void setPm5811Selected(CompetenciaLaboralGenericDTO pm5811Selected) {
		this.pm5811Selected = pm5811Selected;
	}

	public boolean isEditando() {
		return editando;
	}

	public void setEditando(boolean editando) {
		this.editando = editando;
	}

	public boolean isbAdicionar() {
		return bAdicionar;
	}

	public void setbAdicionar(boolean bAdicionar) {
		this.bAdicionar = bAdicionar;
	}

	public boolean isbModificar() {
		return bModificar;
	}

	public void setbModificar(boolean bModificar) {
		this.bModificar = bModificar;
	}

	public boolean isbBorrar() {
		return bBorrar;
	}

	public void setbBorrar(boolean bBorrar) {
		this.bBorrar = bBorrar;
	}

	public boolean isbCancelar() {
		return bCancelar;
	}

	public void setbCancelar(boolean bCancelar) {
		this.bCancelar = bCancelar;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public CompetenciasLaboralesService getCompetenciasLaboralesService() {
		return competenciasLaboralesService;
	}

	public void setCompetenciasLaboralesService(CompetenciasLaboralesService competenciasLaboralesService) {
		this.competenciasLaboralesService = competenciasLaboralesService;
	}

	public TcPeriodoRepositoy getPeriodoRepository() {
		return periodoRepository;
	}

	public void setPeriodoRepository(TcPeriodoRepositoy periodoRepository) {
		this.periodoRepository = periodoRepository;
	}

	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	public TcMesRepository getTcMesRepository() {
		return tcMesRepository;
	}

	public void setTcMesRepository(TcMesRepository tcMesRepository) {
		this.tcMesRepository = tcMesRepository;
	}

	public Boolean getIsNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}
	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}

}
