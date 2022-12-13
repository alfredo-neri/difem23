package com.gem.sistema.web.bean;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.TcPeriodo;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcPeriodoRepositoy;
import com.gem.sistema.business.service.reportador.GeneraTxtFilesService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.Constants;

@ManagedBean(name = "pbrm01dMB")
@ViewScoped
public class Pbrm01dMB extends BaseDirectReport {
	private List<TcPeriodo> listTrimestres;
	private Integer trimestre;
	private StreamedContent file;
	private String fileName;

	@ManagedProperty("#{tcPeriodoRepositoy}")
	private TcPeriodoRepositoy periodoRepositoy;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{generaTxtFilesService}")
	private GeneraTxtFilesService txtFilesService;

	@PostConstruct
	public void init() {
		jasperReporteName = "pbrm01d";
		endFilename = jasperReporteName + ".pdf";

		this.listTrimestres = periodoRepositoy.findByTipoPeriodo(3);

		if (listTrimestres.isEmpty())
			trimestre = listTrimestres.get(0).getPeriodo();
	}

	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {

		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
		Conctb conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());

		parameters.put("year", conctb.getAnoemp());
		parameters.put("sector", this.getUserDetails().getIdSector());
		parameters.put("imagenPath", conctb.getImagePathLeft());
		return parameters;
	}

	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public StreamedContent getFile() {
		fileName = "Pbrm-01d.txt";
		InputStream stream = null;

		try {
			stream = new FileInputStream(this.txtFilesService.generaReporteFichaDiseno01d(fileName,
					this.getUserDetails().getIdSector(), trimestre));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		file = new DefaultStreamedContent(stream, Constants.APPLICCTION_TXT, fileName);
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Success", "Se Generó el Archivo: " + fileName);

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

	public ConctbRepository getConctbRepository() {
		return conctbRepository;
	}

	public void setConctbRepository(ConctbRepository conctbRepository) {
		this.conctbRepository = conctbRepository;
	}

	public GeneraTxtFilesService getTxtFilesService() {
		return txtFilesService;
	}

	public void setTxtFilesService(GeneraTxtFilesService txtFilesService) {
		this.txtFilesService = txtFilesService;
	}

	public List<TcPeriodo> getListTrimestres() {
		return listTrimestres;
	}

	public void setListTrimestres(List<TcPeriodo> listTrimestres) {
		this.listTrimestres = listTrimestres;
	}

	public Integer getTrimestre() {
		return trimestre;
	}

	public void setTrimestre(Integer trimestre) {
		this.trimestre = trimestre;
	}

	public TcPeriodoRepositoy getPeriodoRepositoy() {
		return periodoRepositoy;
	}

	public void setPeriodoRepositoy(TcPeriodoRepositoy periodoRepositoy) {
		this.periodoRepositoy = periodoRepositoy;
	}

}
