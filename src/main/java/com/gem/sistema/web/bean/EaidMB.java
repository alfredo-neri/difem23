package com.gem.sistema.web.bean;

import static com.gem.sistema.util.UtilFront.generateNotificationFront;
import static com.roonin.utils.UtilDate.getLastDayByAnoEmp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.StreamedContent;

import com.gem.sistema.business.domain.Conctb;
import com.gem.sistema.business.domain.Eaid;
import com.gem.sistema.business.domain.TcPeriodo;
import com.gem.sistema.business.domain.TrPuestoFirma;
import com.gem.sistema.business.repository.catalogs.ConctbRepository;
import com.gem.sistema.business.repository.catalogs.TcMesRepository;
import com.gem.sistema.business.service.catalogos.EaidService;
import com.gem.sistema.business.service.catalogos.PuestosFirmasService;
import com.gem.sistema.business.service.catalogos.TcPeriodoService;
import com.gem.sistema.business.service.reportador.ReportValidationException;
import com.gem.sistema.util.ConstantsClaveEnnum;
import com.gem.sistema.web.datamodel.DataModelGeneric;
import com.roonin.utils.UtilDate;

@ManagedBean(name = "eaidMB")
@ViewScoped
public class EaidMB extends ReportePeriodos{

	private static final Log LOG = LogFactory.getLog(EaidMB.class);

	private static final Boolean FALSE = Boolean.FALSE;
	private static final Boolean TRUE = Boolean.TRUE;

	private static final Integer TRIMESTRE = 3;
	/** The Constant VIEW_EDIT_ROW_ACTIVATE_PENCIL. */
	private static final String VIEW_EDIT_ROW_ACTIVATE_PENCIL = "jQuery('span.ui-icon-pencil').eq(";

	/** The Constant VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT. */
	private static final String VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT = ").each(function(){jQuery(this).click()});";

	/** The Constant FOCUS_BY_ROWID. */
	private static final String FOCUS_BY_ROWID = "PrimeFaces.focus('form1:object:%1$s:trim');";

	/** The Constant UPDATE_OBJETS. */
	private static final String UPDATE_OBJETS = "jQuery('#form1\\\\:hiddenUpdate').click();";

	private static final String CLICK_UPDATE = "jQuery('#form1\\\\\\\\:lasPage').click();";

	private static final String REPORT_NAME = "EAID";

	@ManagedProperty("#{tcPeriodoService}")
	private TcPeriodoService tcPeriodoService;

	@ManagedProperty("#{eaidService}")
	private EaidService eaidService;

	@ManagedProperty("#{puestosFirmasService}")
	private PuestosFirmasService puestosFirmasService;

	@ManagedProperty("#{conctbRepository}")
	private ConctbRepository conctbRepository;

	@ManagedProperty("#{tcMesRepository}")
	private TcMesRepository tcMesRepository;

	private TrPuestoFirma presidente;
	private TrPuestoFirma tesorero;

	private Eaid eaid;
	private TcPeriodo tcPeriodo;
	private Conctb conctb;

	private String operador;
	private Integer oldValue;
	private Integer lastTrimestre;
	private Integer lastConse;
	private Integer trimestre;

	private Boolean bSalvar;
	private Boolean bModificar;
	private Boolean bCancelar;
	private Boolean bAdicionar;
	private Boolean bEdicion;

	private Integer idSector;
	private Integer indexCurrent;

	private Integer anio;

	private List<Eaid> listEadi;
	private List<TcPeriodo> listPeriodo;

	private DataModelGeneric<Eaid> dataModelEaid;
	private Integer noDecimales;
	private Integer pesos;
	@PostConstruct
	public void init() {
		LOG.info("INICIA EL PROCESO DE CAPTURA DE EAID");
		noDecimales = 2;
		pesos = 1;
		jasperReporteName = "EAID";
		endFilename = jasperReporteName + ".pdf";
		this.setIdSector(this.getUserDetails().getIdSector());
		conctb = this.eaidService.getAnioContable(idSector, 0l);
		eaid = new Eaid();

		this.listPeriodo = this.tcPeriodoService.findByPeriodo(TRIMESTRE);
		
		this.iniciarlizarbandera();
		this.loadData();

	}
	@Override
	public Map<String, Object> getParametersReports() throws ReportValidationException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		Conctb conctb = conctbRepository.findByIdsector(this.getUserDetails().getIdSector());
		Integer sector = this.getUserDetails().getIdSector();
		TrPuestoFirma firma = null;
		Object[] meses = this.getMonths(trimestre, conctb.getAnoemp());

		parameters.put("pMesInicial", meses[0]);
		parameters.put("pMesFinal", meses[1]);
		parameters.put("pLastDay", meses[2]);
		parameters.put("pYear", conctb.getAnoemp());
		parameters.put("pNombreMunicipio", conctb.getNomDep());
		parameters.put("pImagen1", conctb.getImagePathLeft());
		parameters.put("pImagen2", conctb.getImagePathRigth());
		parameters.put("trimestre", trimestre);
		parameters.put("idSector", sector);
		parameters.put("sql", this.generaQuery(sector));
		
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F08.getValue());
		parameters.put("pL2", firma.getPuesto().getPuesto());
		parameters.put("pN2", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F09.getValue());
		parameters.put("pL3", firma.getPuesto().getPuesto());
		parameters.put("pN3", firma.getNombre());
		firma = puestosFirmasService.getFirmaBySectorAnioClave(sector, 0L, ConstantsClaveEnnum.CLAVE_F11.getValue());
		parameters.put("pL4", firma.getPuesto().getPuesto());
		parameters.put("pN4", firma.getNombre());

		parameters.put("trimestre", trimestre);
		return parameters;
	}

	public String generaQuery(Integer idsector) {
		
		StringBuilder sSql1 = new StringBuilder();
		StringBuilder sqlMiles = new StringBuilder();
		
		
		sSql1.append("SELECT GRUP, NIVEL,TIPO,NOMCTA,ESTIMADO,AMPLIACION_REDUCCION,MODIFICADO,DEVENGADO,RECAUDADO,DIFERENCIA  "
				+ "    FROM ( "
				+ "	SELECT 1 GRUP,NIVEL, TIPO,NOMCTA,ESTIMADO,AMPLIACION_REDUCCION,MODIFICADO,DEVENGADO,RECAUDADO,DIFERENCIA  "
				+ "         FROM ( "
				+ "             SELECT 0 NIVEL,0 TIPO ,('Ingresos de Libre Disposición')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "             SELECT 1 NIVEL,1 TIPO ,('  A. Impuestos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "             SELECT 1 NIVEL,2 TIPO ,('  B. Cuotas y Aportaciones de Seguridad Social')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "             SELECT 1 NIVEL,3 TIPO ,('  C. Contribuciones de Mejoras')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "             SELECT 1 NIVEL,4 TIPO ,('  D. Derechos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "             SELECT 1 NIVEL, 5 TIPO ,('  E. Productos') NOMCTA , T1.SALINI ESTIMADO, "
				+ "                     FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4151') AMPLIACION_REDUCCION, "
				+ "                     T1.SALINI+FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4151') MODIFICADO, "
				+ "                     T1.DEVENGADO, "
				+ "                     T1.RECAUDADO, "
				+ "                     T1.SALINI+FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4151') -  T1.DEVENGADO DIFERENCIA "
				+ "                  FROM ( "
				+ "                        SELECT 5 TIPO,NOMCTA,SALINI, 0 AMPLIACION_REDUCCION, "
				+ "                           0 MODIFICADO, "
				+ "                           FN_GET_DEVREC_ING("+trimestre+",'8150', '4151')DEVENGADO, "
				+ "                           FN_GET_DEVREC_ING("+trimestre+",'8150', '4151') RECAUDADO "
				+ "                         FROM CUENTA CU "
				+ "                         WHERE  CU.CUENTA IN ('8110') "
				+ "                                AND SUBSTR(SCTA, 7,4) IN( '4151') "
				+ "                                AND SSCTA=''  "
				+ "                       ) T1 "
				+ "        UNION ALL "
				+ "                SELECT 1 NIVEL, 6 TIPO ,('  F. Aprovechamientos') NOMCTA , T1.SALINI ESTIMADO, "
				+ "                       T1.AMPLIACION_REDUCCION AMPLIACION_REDUCCION, "
				+ "                       T1.SALINI+T1.AMPLIACION_REDUCCION MODIFICADO, "
				+ "                       T1.DEVENGADO, "
				+ "                       T1.RECAUDADO, "
				+ "                       T1.SALINI- T1.DEVENGADO DIFERENCIA "
				+ "                    FROM ( "
				+ "                          SELECT 5 TIPO,NOMCTA,SALINI,  "
				+ "                                 FN_GET_AMPL_REDU_APROVE("+trimestre+",'8110', '4399')  AMPLIACION_REDUCCION, "
				+ "                                 0 MODIFICADO, "
				+ "                                 FN_GET_DEVREC_ING("+ trimestre +",'8150', '4399') - FN_GET_DISPONIBILIDAD_PRESUPUESTARIA("+trimestre+")DEVENGADO, "
				+ "                                 FN_GET_DEVREC_ING("+ trimestre+",'8150', '4399') - FN_GET_DISPONIBILIDAD_PRESUPUESTARIA("+trimestre+")RECAUDADO        "
				+ "                           FROM CUENTA CU "
				+ "                              WHERE  CU.CUENTA IN ('8110') "
				+ "                                     AND SUBSTR(SCTA, 7,4) IN( '4399') "
				+ "                                     AND SSCTA=''  "
				+ "                          ) T1 "
				+ "        UNION ALL "
				+ "                SELECT 1 NIVEL, 7 TIPO ,('  G. Ingresos por Ventas de Bienes y Servicios') NOMCTA , T1.SALINI ESTIMADO, "
				+ "                       FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4173') AMPLIACION_REDUCCION, "
				+ "                       T1.SALINI+FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4173') MODIFICADO, "
				+ "                       T1.DEVENGADO, "
				+ "                       T1.RECAUDADO, "
				+ "                       T1.SALINI -  T1.DEVENGADO DIFERENCIA "
				+ "                    FROM ( "
				+ "                          SELECT 5 TIPO,NOMCTA,SALINI, 0 AMPLIACION_REDUCCION, "
				+ "                                 0 MODIFICADO, "
				+ "                                 FN_GET_DEVREC_ING("+trimestre+",'8150', '4173')DEVENGADO, "
				+ "                                 FN_GET_DEVREC_ING("+trimestre+",'8150', '4173') RECAUDADO "
				+ "                               FROM CUENTA CU "
				+ "                               WHERE  CU.CUENTA IN ('8110') "
				+ "                                     AND SUBSTR(SCTA, 7,4) IN( '4173') "
				+ "                                     AND SSCTA=''  "
				+ "                         ) T1 "
				+ "        UNION ALL "
				+ "               SELECT 1 NIVEL, 9 TIPO ,('  H. Participaciones   (H=h1+h2+h3+h4+h5+h6+h7+h8+h9+h10+h11)')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,10 TIPO ,('    h1) Fondo General de Participaciones ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,11 TIPO ,('    h2) Fondo de Fomento Municipal ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,12 TIPO ,('    h3) Fondo de Fiscalización y Recaudación ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL, 13 TIPO ,('   h4) Fondo de Compensación ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,14 TIPO ,('    h5) Fondo de Extracción de Hidrocarburos ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,15 TIPO ,('    h6) Impuesto Especial Sobre Producción y Servicios ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,16 TIPO ,('    h7) 0.136% de la Recaudación Federal Participable ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,17 TIPO ,('    h8) 3.17% Sobre Extracción de Petróleo')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,18 TIPO ,('    h9) Gasolinas y Diésel ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,19 TIPO ,('    h10) Fondo del Impuesto Sobre la Renta')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,20 TIPO ,('    h11) Fondo de Estabilización de los Ingresos de las Entidades Federativas')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 1 NIVEL,21 TIPO ,('  I. Incentivos Derivados de la Colaboración Fiscal (I=i1+i2+i3+i4+i5) ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,22 TIPO ,('    i1) Tenencia o Uso de Vehículos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,23 TIPO ,('    i2) Fondo de Compensación ISAN')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,24 TIPO ,('    i3) Impuesto Sobre Automóviles Nuevos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,25 TIPO ,('    i4) Fondo de Compensación de Repecos-Intermedios')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 0 NIVEL,26 TIPO ,('    i5) Otros Incentivos Económicos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "               SELECT 1 NIVEL,27 TIPO ,(' J. Transferencias') NOMCTA , "
				+ "                       SUM(T2.ESTIMADO)+FN_GET_PAD_FTE3("+trimestre+") ESTIMADO, "
				+ "                       (-1*FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4173'))AMPLIACION_REDUCCION, "
				+ "                       SUM(T2.ESTIMADO)+FN_GET_PAD_FTE3("+trimestre+")+(-1*FN_GET_AMPREDU_EFG("+trimestre+",'8110', '4173')) MODIFICADO, "
				+ "                       FN_GET_DEVREC_TRANSFERENCIAS("+trimestre+",'8150','4223') DEVENGADO, "
				+ "                       FN_GET_DEVREC_TRANSFERENCIAS("+trimestre+",'8150','4223') RECAUDADO, "
				+ "                       SUM(T2.ESTIMADO)+FN_GET_PAD_FTE3("+trimestre+")- FN_GET_DEVREC_TRANSFERENCIAS("+trimestre+",'8150','4223')  DIFERENCIA "
				+ "                 FROM ( "
				+ "                       SELECT 27 TIPO,T1.NOMCTA,T1.SALINI ESTIMADO, "
				+ "                              T1.AMPLIACION - T1.REDUCCIONES AMPLI_REDU, "
				+ "                              (T1.SALINI + T1.AMPLIACION ) - T1.REDUCCIONES MODIFICADO, "
				+ "                              T1.REDUCCIONES    +  T1.AMPLIACION DEVENGADO, "
				+ "                              T1.REDUCCIONES    -  T1.AMPLIACION RECAUDADO "
				+ "                           FROM  ( "
				+ "                                SELECT 27 TIPO, CU.NOMCTA,CU.SALINI,0 AMPLIACION, 0 REDUCCIONES  "
				+ "                                     FROM CUENTA CU "
				+ "                                     WHERE  CU.CUENTA = '8110' "
				+ "                                            AND SUBSTR(CU.SCTA, 7,4) IN( '4223') "
				+ "                                            AND SUBSTR(CU.SSCTA,15,1)='1'  "
				+ "                                            AND SUBSTR(CU.SSSCTA,4,1)='1' "
				+ "                                            AND SUBSTR(CU.SSSSCTA,4,1) IN('1','2','4')  "
				+ "                                   )T1 "
				+ "                          )T2 "
				+ "         UNION ALL "
				+ "                 SELECT 1 NIVEL, 28 TIPO ,('  K. Convenios')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "         UNION ALL "
				+ "                 SELECT 0 NIVEL,29 TIPO ,('    k1) Otros Convenios y Subsidios')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "         UNION ALL "
				+ "                 SELECT 1 NIVEL,30 TIPO ,('  L. Otros Ingresos de Libre Disposición (L=l1+l2)')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "         UNION ALL "
				+ "                 SELECT 0 NIVEL,31 TIPO ,('    l1) Participaciones en Ingresos Locales')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "         UNION ALL "
				+ "                 SELECT 0 NIVEL,32 TIPO ,('    l2) Otros Ingresos de Libre Disposición')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "            ) "
				
				+ "   UNION ALL  "
				+ "   SELECT 2 GRUP,NIVEL, TIPO,NOMCTA,ESTIMADO,AMPLIACION_REDUCCION,MODIFICADO,DEVENGADO,RECAUDADO,DIFERENCIA  "
				+ "          FROM ( "
				+ "                  SELECT 0 NIVEL,33 TIPO ,('Ingresos Excedentes de Ingresos de Libre Disposición')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "          UNION ALL "
				+ "                  SELECT 0 NIVEL,34 TIPO ,('  Transferencias Federales Etiquetadas ')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "          UNION ALL "
				+ "                  SELECT 1 NIVEL,35 TIPO ,(' A. Aportaciones (A=a1+a2+a3+a4+a5+a6+a7+a8)') NOMCTA , "
				+ "                     T2.AMPLI_REDU  ESTIMADO, "
				+ "                     T2.ESTIMADO AMPLIACION_REDUCCION, "
				+ "                     T2.MODIFICADO, "
				+ "                     T2.DEVENGADO, "
				+ "                     T2.RECAUDADO, "
				+ "                     T2.MODIFICADO - T2.RECAUDADO DIFERENCIA "
				+ "                  FROM ( "
				+ "                       SELECT 35 TIPO,T1.NOMCTA,T1.SALINI ESTIMADO, "
				+ "                               T1.AMPLI_REDU AMPLI_REDU, "
				+ "                              (T1.SALINI + AMPLI_REDU) MODIFICADO, "
				+ "                               T1.DEVENGADO DEVENGADO, "
				+ "                               T1.RECAUDADO RECAUDADO "
				+ "                            FROM  ( "
				+ "                                  SELECT 35 TIPO, CU.NOMCTA,CU.SALINI, FN_GET_AMPRED_Aab_INGRESOS("+trimestre+",'8110','4223','5') AMPLI_REDU, "
				+ "                                      0 MODIFICADO, "
				+ "                                      FN_GET_DEVREC_NIVEL5("+trimestre+",'8140','4223','5') DEVENGADO, "
				+ "                                      FN_GET_DEVREC_NIVEL5("+trimestre+",'8140','4223','5') RECAUDADO "
				+ "                                   FROM CUENTA CU "
				+ "                                   WHERE  CU.CUENTA = '8110' "
				+ "                                          AND SUBSTR(CU.SCTA, 7,4) IN( '4223') "
				+ "                                          AND SUBSTR(CU.SSCTA,15,1)='1'  "
				+ "                                          AND SUBSTR(CU.SSSCTA,4,1)='1' "
				+ "                                          AND SUBSTR(CU.SSSSCTA,4,1) IN('5')  "
				+ "                                )T1 "
				+ "                          )T2 "
				+ "       UNION ALL "
				+ "               SELECT 0 NIVEL, 36 TIPO ,('    a1) Fondo de Aportaciones para la Nómina Educativa y Gasto Operativo')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "       UNION ALL "
				+ "               SELECT 0 NIVEL,37 TIPO ,('    a2) Fondo de Aportaciones para los Servicios de Salud')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "       UNION ALL "
				+ "               SELECT 0 NIVEL,38 TIPO ,('    a3) Fondo de Aportaciones para la Infraestructura Social')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "       UNION ALL "
				+ "               SELECT 0 NIVEL,39 TIPO ,('    a4) Fondo de Aportaciones para el Fortalecimiento de los Municipios y de las Demarcaciones Territoriales del Distrito Federal')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "       UNION ALL "
				+ "               SELECT 0 NIVEL,40 TIPO ,('    a5) Fondo de Aportaciones Múltiples')NOMCTA,  "
				+ "                     T2.AMPLI_REDU  ESTIMADO, "
				+ "                     T2.ESTIMADO AMPLIACION_REDUCCION, "
				+ "                     T2.MODIFICADO, "
				+ "                     T2.DEVENGADO, "
				+ "                     T2.RECAUDADO, "
				+ "                     T2.MODIFICADO - T2.RECAUDADO DIFERENCIA "
				+ "              FROM ( "
				+ "                    SELECT 35 TIPO,T1.NOMCTA,T1.SALINI ESTIMADO, "
				+ "                           T1.AMPLI_REDU AMPLI_REDU, "
				+ "                          (T1.SALINI + AMPLI_REDU) MODIFICADO, "
				+ "                          T1.DEVENGADO DEVENGADO, "
				+ "                          T1.RECAUDADO RECAUDADO "
				+ "                       FROM ( "
				+ "                             SELECT 35 TIPO, CU.NOMCTA,CU.SALINI, FN_GET_AMPRED_Aab_INGRESOS("+trimestre+",'8110','4223','5') AMPLI_REDU, "
				+ "                                  0 MODIFICADO, "
				+ "                                  FN_GET_DEVREC_NIVEL5("+trimestre+",'8140','4223','5') DEVENGADO, "
				+ "                                  FN_GET_DEVREC_NIVEL5("+trimestre+",'8140','4223','5') RECAUDADO "
				+ "                              FROM CUENTA CU "
				+ "                              WHERE  CU.CUENTA = '8110' "
				+ "                                     AND SUBSTR(CU.SCTA, 7,4) IN( '4223') "
				+ "                                     AND SUBSTR(CU.SSCTA,15,1)='1'  "
				+ "                                     AND SUBSTR(CU.SSSCTA,4,1)='1' "
				+ "                                     AND SUBSTR(CU.SSSSCTA,4,1) IN('5')  "
				+ "                             ) T1 "
				+ "                     )T2 "
				+ " "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,41 TIPO ,('    a6) Fondo de Aportaciones para la Educación Tecnológica y de Adultos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,42 TIPO ,('    a7) Fondo de Aportaciones para la Seguridad Pública de los Estados y del Distrito Federal')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,43 TIPO ,('    a8) Fondo de Aportaciones para el Fortalecimiento de las Entidades Federativas')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 1 NIVEL, 44 TIPO ,('  B. Convenios (B=b1+b2+b3+b4)')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,45 TIPO ,('    b1) Convenios de Protección Social en Salud')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,46 TIPO ,('    b2) Convenios de Descentralización')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,47 TIPO ,('    b3) Convenios de Reasignación')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "        UNION ALL "
				+ "                SELECT 0 NIVEL,48 TIPO ,(' b4) Otros Convenios y Subsidios') NOMCTA , "
				+ "                       T2.ESTIMADO ESTIMADO, "
				+ "                       T2.AMPLIACION_REDUCCION AMPLIACION_REDUCCION, "
				+ "                       T2.ESTIMADO + T2.AMPLIACION_REDUCCION MODIFICADO, "
				+ "                       0 DEVENGADO, "
				+ "                       0 RECAUDADO, "
				+ "                       0 DIFERENCIA "
				+ "                    FROM ( "
				+ "                       SELECT 7 TIPO,SUM(T1.SALINI) ESTIMADO, "
				+ "                             FN_GET_AMPRED_B4_INGRESOS("+trimestre+",'8110','4223','3') AMPLIACION_REDUCCION "
				+ "                           FROM  ( "
				+ "                             SELECT 7 TIPO, CU.SALINI "
				+ "                                  FROM CUENTA CU "
				+ "                                    WHERE  CU.CUENTA = '8110' "
				+ "                                          AND SUBSTR(CU.SCTA, 7,4) IN( '4223') "
				+ "                                          AND SUBSTR(CU.SSCTA,15,1)='1'  "
				+ "                                          AND SUBSTR(CU.SSSCTA,4,1)='1' "
				+ "                                          AND SUBSTR(CU.SSSSCTA,4,1) IN('3')  "
				+ "                                  ) T1 "
				+ "                            )T2 "
				+ " "
				+ "            UNION ALL "
				+ "                   SELECT 1 NIVEL,49 TIPO ,('  C. Fondos Distintos de Aportaciones (C=c1+c2)')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "            UNION ALL "
				+ "                   SELECT 0 NIVEL,50 TIPO ,('    c1) Fondo para Entidades Federativas y Municipios Productores de Hidrocarburos')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "            UNION ALL "
				+ "                   SELECT 0 NIVEL,51 TIPO ,('    c2) Fondo Minero')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "            UNION ALL "
				+ "                   SELECT 1 NIVEL,52 TIPO ,('  D. Transferencias, Subsidios y Subvenciones, y Pensiones y Jubilaciones')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "            UNION ALL "
				+ "                   SELECT 1 NIVEL, 53 TIPO ,('  E. Otras Transferencias Federales Etiquetadas')NOMCTA, 0 ESTIMADO,0 AMPLIACION_REDUCCION,0 MODIFICADO,0 DEVENGADO,0 RECAUDADO,0 DIFERENCIA FROM SYSIBM.SYSDUMMY1 "
				+ "       ) "
				
				+ "   UNION ALL "
				
				+ "   SELECT 3 GRUP,NIVEL, TIPO,NOMCTA,ESTIMADO,AMPLIACION_REDUCCION,MODIFICADO,DEVENGADO,RECAUDADO,DIFERENCIA  "
				+ "FROM ( "
				+ "       SELECT 0 NIVEL, 54 TIPO ,('III. Ingresos Derivados de Financiamientos (III = A) ') NOMCTA , "
				+ "                    T1.ESTIMADO ESTIMADO, "
				+ "                   FN_GET_AMPRED_ING_DERIVADOS("+trimestre+",'8110','4399') AMPLIACION_REDUCCION, "
				+ "                  T1.ESTIMADO +FN_GET_AMPRED_ING_DERIVADOS("+trimestre+",'8110','4399')MODIFICADO, "
				+ "                  T1.DEVENGADO DEVENGADO, "
				+ "                  T1.RECAUDADO RECAUDADO, "
				+ "                 T1.ESTIMADO + DEVENGADO DIFERENCIA "
				+ "               FROM  ( "
				+ "            SELECT 7 TIPO,SUM(CU.SALINI)ESTIMADO,FN_GET_DEVREC_INGDERIVADOS("+trimestre+",'8140','4399')DEVENGADO, "
				+ "                  FN_GET_DEVREC_INGDERIVADOS("+trimestre+",'8140','4399') RECAUDADO "
				+ " "
				+ "                      FROM CUENTA CU "
				+ "                     WHERE  CU.CUENTA = '8110' "
				+ "                       AND SUBSTR(CU.SCTA, 7,4) IN( '4399') "
				+ "                        AND SUBSTR(CU.SSCTA,15,1)='1'  "
				+ "                       AND SUBSTR(CU.SSSCTA,4,1)='1' "
				+ "                       AND SUBSTR(CU.SSSSCTA,4,1) IN('8','9') "
				+ "                       "
				+ "               ) T1) "
				+ "       )ORDER BY TIPO ASC ");
		System.out.println(sSql1.toString());
		
		if (pesos != 1) {
			sqlMiles.append(
					"SELECT T2.GRUP,T2.CAMPO7 , T2.CAMPO6, 	(T2.APROBADO /1000) APROBADO,(T2.APMLIREDU /1000) APMLIREDU,				")
					.append("	(T2.MODIFICADO /1000) MODIFICADO,(T2.DEVENGADO/1000) DEVENGADO,  ")
					.append("	(T2.PAGADO/1000) PAGADO,(T2.SUBEJERCICIO /1000) SUBEJERCICIO               ")
					.append("FROM(                                                                                                      ");
			sSql1.insert(0, sqlMiles);
			sSql1.append(")T2");
			System.out.println(sSql1);
		}
		System.out.println(sSql1);
		return sSql1.toString();

	}

	public void loadData() {
		listEadi = this.eaidService.findByIdSector(this.getIdSector());
		dataModelEaid = new DataModelGeneric<Eaid>(listEadi);
		this.setbAdicionar(FALSE);
	}

	public void findByTrimestre() {
		listEadi = this.eaidService.findByTrimestre(trimestre, idSector);
		this.getDataModelEaid().setListT(listEadi);
	}

	public void addElement() {
		this.setbAdicionar(TRUE);
		Eaid eaid = new Eaid();

		listEadi.add(eaid);

		this.activateRowEdit(listEadi.size() - 1);
		RequestContext.getCurrentInstance().execute("PF('eaidWdg').paginator.setPage(" + (this.getPage() - 1) + ");");

		if (listEadi.size() > 20) {
			Integer indexOf = this.getRowCurrent(listEadi.size());
			this.activateRowEdit(indexOf);

			RequestContext.getCurrentInstance().execute(String.format(FOCUS_BY_ROWID, (listEadi.size() - 1)));
			RequestContext.getCurrentInstance().execute(CLICK_UPDATE);

		}

		dataModelEaid.setListT(listEadi);

	}

	public void delete(Integer index) {
		eaid = this.getDataModelEaid().getListT().get(index);
		if (null == eaid.getId() || eaid.getId() == 0) {
			listEadi.remove(eaid);

		} else {
			listEadi.remove(eaid);
			eaidService.delete(eaid);
		}
		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Información!",
				"¡Se Eliminico Correctamente el Registro!");
	}

	public void iniciarlizarbandera() {
		this.bModificar = TRUE;
		this.bAdicionar = FALSE;
		this.bSalvar = FALSE;
		this.bCancelar = FALSE;
		this.bEdicion = FALSE;
	}

	/**
	 * Persiste la edicion de un registro.
	 *
	 * @param event the event
	 */
	public void onInitRowEdit(final RowEditEvent event) {
		eaid = (Eaid) event.getObject();
		if (null != event.getObject()) {

			if (null != eaid.getId() && eaid.getId() != 0) {
				this.setbAdicionar(TRUE);
				lastTrimestre = this.eaid.getTrimestre();
				lastConse = this.eaid.getConsecutivo();

				for (int i = 0; i < this.getDataModelEaid().getListT().size(); i++) {
					if (this.getDataModelEaid().getListT().get(i) == eaid) {
						indexCurrent = i;
						break;
					}

				}
			} else {
				this.setbAdicionar(FALSE);
			}
		}
	}

	/**
	 * On row edit.
	 *
	 * @param event the event
	 */
	public void onRowEdit(RowEditEvent event) {
		Eaid obj = (Eaid) event.getObject();

		Eaid existe = this.eaidService.findByIdSectorAndTrimestreAndCoonsecutivo(this.getIdSector(), obj.getTrimestre(),
				obj.getConsecutivo());
		Integer lastIndex = this.dataModelEaid.getListT().size() > 0 ? this.dataModelEaid.getListT().size() - 1 : 0;
		obj.setIdSector(this.getIdSector());
		obj.setIdRef(this.getUserDetails().getIdMunicipio());
		obj.setFechaCptura(UtilDate.getDateSystem());
		obj.setIdAnio(conctb.getId());
		obj.setCapturo(this.getUserDetails().getUsername());
		if (null == existe && !this.getbAdicionar()) {

			this.eaidService.save(obj);
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Información!",
					"Los Datos se Guardaron Correctamente");
			RequestContext.getCurrentInstance().execute(UPDATE_OBJETS);
			this.setbAdicionar(FALSE);

		} else if (this.getbAdicionar() && lastTrimestre == obj.getTrimestre() && lastConse == obj.getConsecutivo()) {
			this.eaidService.update(obj);
			this.setbAdicionar(FALSE);
			generateNotificationFront(FacesMessage.SEVERITY_INFO, "Información!",
					"Los Datos se Modificaron Correctamente");
			RequestContext.getCurrentInstance().execute(UPDATE_OBJETS);
		} else {
			generateNotificationFront(FacesMessage.SEVERITY_WARN, "Error!", "El trimestre : " + obj.getTrimestre()
					+ " Con Consecutivo: " + obj.getConsecutivo() + " \n¡Ya existen en la Base de Datos!");

			if (this.getbAdicionar() && (lastTrimestre != obj.getTrimestre() || lastConse != obj.getConsecutivo()))

				this.activateRowEdit(indexCurrent);
			else
				this.activateRowEdit(lastIndex);
		}

	}

	public void onRowCancel(RowEditEvent event) {

		generateNotificationFront(FacesMessage.SEVERITY_INFO, "Info!", "Edición Cancelada");
	}

	/**
	 * @param index
	 * @return
	 */
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

	/**
	 * Activate row edit.
	 *
	 * @param index the index
	 */
	public void activateRowEdit(final int index) {
		LOG.info(":: Cerrar cuadro de dialogo ");
		final StringBuilder text = new StringBuilder();
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL);
		text.append(index);
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT);
		text.append(" ");
		text.append(String.format(FOCUS_BY_ROWID, index));
		RequestContext.getCurrentInstance().execute(text.toString());
	}

	/**
	 * Activa el modo de edicion en una fila.
	 * 
	 * @param index fila a ser activada.
	 */
	/**
	 * @param index
	 */
	public void activateRowEditOnInsert(final int index) {
		LOG.info(":: Cerrar cuadro de dialogo ");
		final StringBuilder text = new StringBuilder();
		// text.append("PF('polizasWdg').filter();PF('polizasWdg').paginator.setPage(PF('polizasWdg').paginator.cfg.pageCount
		// - 1);");
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL);
		text.append(index);
		text.append(VIEW_EDIT_ROW_ACTIVATE_PENCIL_COMPLEMENT);
		text.append(" ");
		text.append(String.format(FOCUS_BY_ROWID, index));
		RequestContext.getCurrentInstance().execute(text.toString());
	}

	/**
	 * Gets the page.
	 *
	 * @return the page
	 */
	public int getPage() {
		int rows = dataModelEaid.getListT().size();
		int de = 1;
		double maxRow = 20;
		double pageActua = (rows * de) / maxRow;
		String page = "" + Math.ceil(pageActua);
		return Integer.parseInt(this.getValue(page)[0]);
	}

	/**
	 * Gets the value.
	 *
	 * @param data the data
	 * @return the value
	 */
	public String[] getValue(String data) {
		return data.split("\\.");
	}

	/**
	 * Go to last page.
	 */
	

	
	@Override
	public StreamedContent generaReporteSimple(int type) throws ReportValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void getFirmas() {
		List<TrPuestoFirma> puestosFirmas = puestosFirmasService.listPuestosFirmas(this.getUserDetails().getIdSector(),
				0L);
		for (int y = 0; y < puestosFirmas.size(); y++) {
			if (puestosFirmas.get(y).getId() == 1) {
				this.presidente = puestosFirmas.get(y);
			}
			if (puestosFirmas.get(y).getId() == 3) {
				this.tesorero = puestosFirmas.get(y);
			}
		}
	}
	public void goToLastPage() {
		Integer size = this.dataModelEaid.getListT().size();
		if (this.getbAdicionar() == Boolean.TRUE)
			if (size >= 20) {
				this.activateRowEditOnInsert(size);

			} else {

				this.activateRowEdit(size - 1);

			}
	}

	public void activeRowPostPage() {
		this.activateRowEdit(this.getDataModelEaid().getListT().size() - 1);
	}

	public EaidService getEaidService() {
		return eaidService;
	}

	public void setEaidService(EaidService eaidService) {
		this.eaidService = eaidService;
	}

	public void save() {

	}

	public Eaid getEaid() {
		return eaid;
	}

	public void setEaid(Eaid eaid) {
		this.eaid = eaid;
	}

	public List<Eaid> getListEadi() {
		return listEadi;
	}

	public void setListEadi(List<Eaid> listEadi) {
		this.listEadi = listEadi;
	}

	public DataModelGeneric<Eaid> getDataModelEaid() {
		return dataModelEaid;
	}

	public void setDataModelEaid(DataModelGeneric<Eaid> dataModelEaid) {
		this.dataModelEaid = dataModelEaid;
	}

	public TcPeriodoService getTcPeriodoService() {
		return tcPeriodoService;
	}

	public void setTcPeriodoService(TcPeriodoService tcPeriodoService) {
		this.tcPeriodoService = tcPeriodoService;
	}

	public TcPeriodo getTcPeriodo() {
		return tcPeriodo;
	}

	public void setTcPeriodo(TcPeriodo tcPeriodo) {
		this.tcPeriodo = tcPeriodo;
	}

	public List<TcPeriodo> getListPeriodo() {
		return listPeriodo;
	}

	public void setListPeriodo(List<TcPeriodo> listPeriodo) {
		this.listPeriodo = listPeriodo;
	}

	public String getOperador() {
		return operador;
	}

	public void setOperador(String operador) {
		this.operador = operador;
	}

	public Integer getOldValue() {
		return oldValue;
	}

	public void setOldValue(Integer oldValue) {
		this.oldValue = oldValue;
	}

	public Boolean getbSalvar() {
		return bSalvar;
	}

	public void setbSalvar(Boolean bSalvar) {
		this.bSalvar = bSalvar;
	}

	public Boolean getbModificar() {
		return bModificar;
	}

	public void setbModificar(Boolean bModificar) {
		this.bModificar = bModificar;
	}

	public Boolean getbCancelar() {
		return bCancelar;
	}

	public void setbCancelar(Boolean bCancelar) {
		this.bCancelar = bCancelar;
	}

	public Boolean getbAdicionar() {
		return bAdicionar;
	}

	public void setbAdicionar(Boolean bAdicionar) {
		this.bAdicionar = bAdicionar;
	}

	public Boolean getbEdicion() {
		return bEdicion;
	}

	public void setbEdicion(Boolean bEdicion) {
		this.bEdicion = bEdicion;
	}

	public Integer getIdSector() {
		return idSector;
	}

	public void setIdSector(Integer idSector) {
		this.idSector = idSector;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public Conctb getConctb() {
		return conctb;
	}

	public void setConctb(Conctb conctb) {
		this.conctb = conctb;
	}

	public Integer getTrimestre() {
		return trimestre;
	}

	public void setTrimestre(Integer trimestre) {
		this.trimestre = trimestre;
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

	public TcMesRepository getTcMesRepository() {
		return tcMesRepository;
	}

	public void setTcMesRepository(TcMesRepository tcMesRepository) {
		this.tcMesRepository = tcMesRepository;
	}

	public TrPuestoFirma getPresidente() {
		return presidente;
	}

	public void setPresidente(TrPuestoFirma presidente) {
		this.presidente = presidente;
	}

	public TrPuestoFirma getTesorero() {
		return tesorero;
	}

	public void setTesorero(TrPuestoFirma tesorero) {
		this.tesorero = tesorero;
	}


	public Object[] getMonths(Integer trimestre, Integer anio) {
		Integer mesFinal = trimestre * 3;
		Integer mesInicial = mesFinal - 2;
		Object[] meses = {
				tcMesRepository.findByMes(org.apache.commons.lang3.StringUtils.leftPad(mesInicial.toString(), 2, "0"))
						.getDescripcion(),
				tcMesRepository.findByMes(org.apache.commons.lang3.StringUtils.leftPad(mesFinal.toString(), 2, "0"))
						.getDescripcion(),
				getLastDayByAnoEmp(mesFinal, anio) };

		return meses;
	}
}
