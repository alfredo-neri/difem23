package com.gem.sistema.web.bean;

import static com.roonin.utils.UtilDate.getDateSystem;
import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Balancepre;
import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcPeriodo;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.catalogos.BalancepreService;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;
import com.gem.sistema.web.datamodel.DataModelGeneric;

@ManagedBean(name = "balancePresupuestarioMB")
@ViewScoped
public class BalancePresupuestarioMB extends BaseDirectReport {
	final static private String VW1="VI_BALANCE_PRESUPUESTARIO_T1";
	final static private String VW2="VI_BALANCE_PRESUPUESTARIO_T2";
	final static private String VW3="VI_BALANCE_PRESUPUESTARIO_T3";
	final static private String VW4="VI_BALANCE_PRESUPUESTARIO_T4";

	private static final Integer TIPO_PERIODO = 3;

	private static final String VIEW_EDIT_ROW_ACTIVATE_PENCIL = "jQuery('span.ui-icon-pencil').eq(";

	private static final String VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT = ").each(function(){jQuery(this).click()});";

	private static final String FOCUS_BY_ROWID = "PrimeFaces.focus('form1:object:%1$s:trim');";

	private static final String UPDATE_OBJETS = "jQuery('#form1\\\\:hiddenUpdate').click();";

	private DataModelGeneric<Balancepre> dataModelBalancepre;

	private List<Balancepre> listBalancepre;

	private List<TcPeriodo> listTrimestres;

	private Integer trimestre;

	private Balancepre balancepreSelected;

	private Conctb conctb;

	private Integer currentIndex;

	private Integer oldTrimestre;

	private Long oldConsecutivo;

	private Boolean bEdition;

	private Boolean bModifcar;

	private Boolean bAdicionar;

	@ManagedProperty("#{tcPeriodoRepositoy}")
	private TcPeriodoRepositoy periodoRepository;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{balancepreService}")
	private BalancepreService balancepreService;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	@PostConstruct
	public void init() {
		
		jasperReporteName = "balance_presupuestario";
		endFilename = jasperReporteName + ".pdf";
		conctb = conctbRepository.findByIdsectorAndIdRef(this.getUserDetails().getIdSector(), 0);
		listTrimestres = periodoRepository.findByTipoPeriodo(TIPO_PERIODO);

		if (!CollectionUtils.isEmpty(listTrimestres)) {
			trimestre = listTrimestres.get(0).getPeriodo();
		}
		this.refreshData();
		this.bAdicionar = Boolean.FALSE;
	}

	public void refreshData() {
		listBalancepre = balancepreService.findAllByTrimestre(this.getUserDetails().getIdSector(),
				this.getUserDetails().getIdMunicipio(), conctb.getId(), trimestre);
		dataModelBalancepre = new DataModelGeneric<Balancepre>(listBalancepre);
		this.bAdicionar = Boolean.FALSE;
	}

	public void addElement() {

		this.bAdicionar = Boolean.TRUE;
		this.setbEdition(Boolean.TRUE);
		listBalancepre = balancepreService.findAllByTrimestre(this.getUserDetails().getIdSector(),
				this.getUserDetails().getIdMunicipio(), conctb.getId(), trimestre);
		Balancepre balancepreNew = new Balancepre();
		balancepreNew.setConcepto("");
		listBalancepre.add(balancepreNew);

		this.activateRowEdit(listBalancepre.size() - 1);
		RequestContext.getCurrentInstance()
				.execute("PF('balanceWdg').paginator.setPage(" + (this.getPage() - 1) + ");");

		if (listBalancepre.size() > 20) {
			Integer indexOf = this.getRowCurrent(listBalancepre.size());
			this.activateRowEdit(indexOf);

			RequestContext.getCurrentInstance().execute(String.format(FOCUS_BY_ROWID, (listBalancepre.size() - 1)));
		}

		dataModelBalancepre.setListT(listBalancepre);

	}

	public void onRowEdit(RowEditEvent event) {
		balancepreSelected = new Balancepre();
		balancepreSelected = (Balancepre) event.getObject();
		balancepreSelected.setIdSector(this.getUserDetails().getIdSector());
		balancepreSelected.setCapturo(this.getUserDetails().getUsername());
		balancepreSelected.setFeccap(getDateSystem());
		balancepreSelected.setIdAnio(conctb.getId());
		balancepreSelected.setIdRef(this.getUserDetails().getIdMunicipio());

		int lastIndex = dataModelBalancepre.getListT().size() - 1;

		if (bModifcar == Boolean.TRUE) {

			balancepreSelected = balancepreService.modify(balancepreSelected, oldTrimestre, oldConsecutivo);
		} else {
			balancepreSelected.setIndex(lastIndex);
			balancepreSelected = this.balancepreService.save(balancepreSelected);
		}
		if (CollectionUtils.isEmpty(dataModelBalancepre.getListT())) {
			this.activateRowEdit(dataModelBalancepre.getListT().size());
			dataModelBalancepre.getListT().add(lastIndex, balancepreSelected);
		} else {
			if (dataModelBalancepre.getListT().get(lastIndex).isbGuardar() == Boolean.TRUE) {
				listBalancepre = balancepreService.findAllByTrimestre(this.getUserDetails().getIdSector(),
						this.getUserDetails().getIdMunicipio(), conctb.getId(), trimestre);
				RequestContext.getCurrentInstance().execute(UPDATE_OBJETS);
			} else {
				if (bModifcar == Boolean.TRUE && balancepreSelected.isbGuardar() == Boolean.FALSE) {
					this.activateRowEdit(currentIndex);
				} else {
					this.activateRowEdit(lastIndex);
				}

				if (dataModelBalancepre.getListT().size() > 20) {
					int index = this.getRowCurrent(dataModelBalancepre.getListT().size());
					this.activateRowEdit(index);
				}
				dataModelBalancepre.getListT().set(lastIndex, balancepreSelected);
			}
		}
	}

	public void onInitRowEdit(final RowEditEvent event) {
		this.bAdicionar = Boolean.TRUE;
		balancepreSelected = (Balancepre) event.getObject();
		if (null != event.getObject()) {

			if (null != balancepreSelected.getId() && balancepreSelected.getId() != 0) {
				this.setbModifcar(Boolean.TRUE);
				oldTrimestre = this.balancepreSelected.getTrimestre();
				oldConsecutivo = this.balancepreSelected.getConsecutivo();

				for (int i = 0; i < dataModelBalancepre.getListT().size(); i++) {

					if (dataModelBalancepre.getListT().get(i).getId().equals(balancepreSelected.getId())) {
						currentIndex = i;
						break;
					}
				}
			} else {
				this.setbModifcar(Boolean.FALSE);
			}
		}
	}

	public void delete(Integer index) {
		balancepreSelected = this.dataModelBalancepre.getListT().get(index);

		if (null == balancepreSelected.getId() || balancepreSelected.getId() == 0) {
			listBalancepre.remove(balancepreSelected);
		} else {
			listBalancepre.remove(balancepreSelected);
			balancepreService.delete(balancepreSelected);
		}
	}

	public Integer getRowCurrent(Integer index) {
		Double rows = 20.0;
		Double size = index.doubleValue();
		Double row = (size % rows);
		Integer filas = row.intValue() - 1;
		if (filas < 0) {
			filas = 19;
		}

		return filas;
	}

	public int getPage() {
		int rows = listBalancepre.size();
		int de = 1;
		double maxRow = 20;
		double pageActua = (rows * de) / maxRow;
		String page = "" + Math.ceil(pageActua);
		return Integer.parseInt(this.getValue(page)[0]);
	}

	public void goToLastPage() {
		if (this.getbEdition() == Boolean.TRUE)
			if (this.dataModelBalancepre.getListT().size() <= 20) {
				this.activateRowEditOnInsert(this.dataModelBalancepre.getListT().size());

			} else {
				this.activateRowEdit(this.dataModelBalancepre.getListT().size() - 1);

			}
	}

	public void activateRowEditOnInsert(final int index) {

		final StringBuilder text = new StringBuilder();
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL);
		text.append(index);
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT);
		text.append(" ");
		text.append(String.format(FOCUS_BY_ROWID, index));
		RequestContext.getCurrentInstance().execute(text.toString());
	}

	public void activateRowEdit(final int index) {
		final StringBuilder text = new StringBuilder();
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL);
		text.append(index);
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT);
		text.append(" ");
		text.append(String.format(FOCUS_BY_ROWID, index));
		RequestContext.getCurrentInstance().execute(text.toString());
	}

	Map<String, Object> parameters;

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		TrPuestoFirma firma = null;
		Integer sector = this.getUserDetails().getIdSector();
		Conctb conctb = conctbRepository.findByIdsector(sector);
		Object[] meses = this.getMonths(trimestre, conctb.getAnoemp());
		parameters = new HashMap<String, Object>();

		parameters.put("pMesInicial", meses[0]);
		parameters.put("pMesFinal", meses[1]);
		parameters.put("pLastDay", meses[2]);
		parameters.put("pYear", conctb.getAnoemp());
		parameters.put("pNombreMunicipio", conctb.getNomDep());
		parameters.put("pImagen", conctb.getImagePathLeft());
		parameters.put("pImagen2", conctb.getImagePathRigth());
		parameters.put("pTrimestre", trimestre);
		parameters.put("pIdSector", sector);

		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("pL2", firma.getPuesto().getPuesto());
		parameters.put("pN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("pL3", firma.getPuesto().getPuesto());
		parameters.put("pN3", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F11.getValue());
		parameters.put("pL4", firma.getPuesto().getPuesto());
		parameters.put("pN4", firma.getNombre());
		String sql=StringUtils.EMPTY;
		
		sql="SELECT * FROM ";
		switch (trimestre) {
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
		 
		parameters.put("sql", sql);
		System.out.println(sql.toString());
		
		return parameters;
	}

	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

//	public void getFirmas() {
//		List<TrPuestoFirma> puestosFirmas = puestosFirmasService.listPuestosFirmas(this.getUserDetails().getIdSector(),0L);
//		for (int y = 0; y < puestosFirmas.size(); y++) {
//			if (puestosFirmas.get(y).getId() == 1) {
//				this.presidente = puestosFirmas.get(y);
//			}
//			if (puestosFirmas.get(y).getId() == 3) {
//				this.tesorero = puestosFirmas.get(y);
//			}
//		}
//	}
	
	public Object[] getMonths(Integer trimestre, Integer anio) {
		Integer mesFinal = trimestre * 3;
		Integer mesInicial = 1;
		Object[] meses = {
				tcMesRepository.findByMes(org.apache.commons.lang3.StringUtils.leftPad(mesInicial.toString(), 2, "0"))
						.getDescripcion(),
				tcMesRepository.findByMes(org.apache.commons.lang3.StringUtils.leftPad(mesFinal.toString(), 2, "0"))
						.getDescripcion(),
				getLastDayByAnoEmp(mesFinal, anio) };

		return meses;
	}

	public String[] getValue(String data) {
		return data.split("\\.");
	}

	public DataModelGeneric<Balancepre> getDataModelBalancepre() {
		return dataModelBalancepre;
	}

	public void setDataModelBalancepre(DataModelGeneric<Balancepre> dataModelBalancepre) {
		this.dataModelBalancepre = dataModelBalancepre;
	}

	public List<Balancepre> getListBalancepre() {
		return listBalancepre;
	}

	public void setListBalancepre(List<Balancepre> listBalancepre) {
		this.listBalancepre = listBalancepre;
	}

	public List<TcPeriodo> getListTrimestres() {
		return listTrimestres;
	}

	public void setListTrimestres(List<TcPeriodo> listTrimestres) {
		this.listTrimestres = listTrimestres;
	}

	public TcPeriodoRepositoy getPeriodoRepository() {
		return periodoRepository;
	}

	public void setPeriodoRepository(TcPeriodoRepositoy periodoRepository) {
		this.periodoRepository = periodoRepository;
	}

	public BalancepreService getBalancepreService() {
		return balancepreService;
	}

	public void setBalancepreService(BalancepreService balancepreService) {
		this.balancepreService = balancepreService;
	}

	public Balancepre getBalancepreSelected() {
		return balancepreSelected;
	}

	public void setBalancepreSelected(Balancepre balancepreSelected) {
		this.balancepreSelected = balancepreSelected;
	}

	public Integer getOldTrimestre() {
		return oldTrimestre;
	}

	public void setOldTrimestre(Integer oldTrimestre) {
		this.oldTrimestre = oldTrimestre;
	}

	public Long getOldConsecutivo() {
		return oldConsecutivo;
	}

	public void setOldConsecutivo(Long oldConsecutivo) {
		this.oldConsecutivo = oldConsecutivo;
	}

	public Boolean getbEdition() {
		return bEdition;
	}

	public void setbEdition(Boolean bEdition) {
		this.bEdition = bEdition;
	}

	public Boolean getbModifcar() {
		return bModifcar;
	}

	public void setbModifcar(Boolean bModifcar) {
		this.bModifcar = bModifcar;
	}

	public Boolean getbAdicionar() {
		return bAdicionar;
	}

	public void setbAdicionar(Boolean bAdicionar) {
		this.bAdicionar = bAdicionar;
	}

	public Conctb getConctb() {
		return conctb;
	}

	public void setConctb(Conctb conctb) {
		this.conctb = conctb;
	}

	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	public Integer getTrimestre() {
		return trimestre;
	}

	public void setTrimestre(Integer trimestre) {
		this.trimestre = trimestre;
	}

	public Integer getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(Integer currentIndex) {
		this.currentIndex = currentIndex;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public TcMesRepository getTcMesRepository() {
		return tcMesRepository;
	}

	public void setTcMesRepository(TcMesRepository tcMesRepository) {
		this.tcMesRepository = tcMesRepository;
	}

	public PuestosFirmasService getPuestosFirmasService() {
		return puestosFirmasService;
	}

	public void setPuestosFirmasService(PuestosFirmasService puestosFirmasService) {
		this.puestosFirmasService = puestosFirmasService;
	}

}
