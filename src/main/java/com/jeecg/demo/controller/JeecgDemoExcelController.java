package com.jeecg.demo.controller;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;

import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.JxlsExcelExportUtil;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.jeecg.demo.entity.JeecgDemoExcelEntity;
import com.jeecg.demo.entity.JformOrderCustomerEntity;
import com.jeecg.demo.entity.JformOrderMainEntity;
import com.jeecg.demo.page.JformOrderMainPage;
import com.jeecg.demo.service.JeecgDemoExcelServiceI;
import com.jeecg.demo.util.FreemarkerUtil;

import io.swagger.annotations.Api;
import net.sf.jxls.transformer.XLSTransformer;

/**   
 * @Title: Controller  
 * @Description: excel?????????????????????
 * @author onlineGenerator
 * @date 2018-06-15 15:46:09
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/jeecgDemoExcelController")
@Api(value="JeecgDemoExcel",description="excel?????????????????????",tags="jeecgDemoExcelController")
public class JeecgDemoExcelController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(JeecgDemoExcelController.class);

	@Autowired
	private JeecgDemoExcelServiceI jeecgDemoExcelService;
	@Autowired
	private SystemService systemService;
	@Autowired
	private Validator validator;
	


	/**
	 * excel??????????????????????????? ????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("com/jeecg/demo/excel/jeecgDemoExcelList");
	}
	/**
	 * easyui AJAX????????????
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */

	@RequestMapping(params = "datagrid")
	public void datagrid(JeecgDemoExcelEntity jeecgDemoExcel,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(JeecgDemoExcelEntity.class, dataGrid);
		//?????????????????????
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, jeecgDemoExcel, request.getParameterMap());
		try{
		//???????????????????????????
		}catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
		cq.add();
		this.jeecgDemoExcelService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}
	
	/**
	 * ??????excel?????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(JeecgDemoExcelEntity jeecgDemoExcel, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		jeecgDemoExcel = systemService.getEntity(JeecgDemoExcelEntity.class, jeecgDemoExcel.getId());
		message = "excel?????????????????????????????????";
		try{
			jeecgDemoExcelService.delete(jeecgDemoExcel);
			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "excel?????????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ????????????excel?????????????????????
	 * 
	 * @return
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "excel?????????????????????????????????";
		try{
			for(String id:ids.split(",")){
				JeecgDemoExcelEntity jeecgDemoExcel = systemService.getEntity(JeecgDemoExcelEntity.class, 
				id
				);
				jeecgDemoExcelService.delete(jeecgDemoExcel);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "excel?????????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * ??????excel?????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(JeecgDemoExcelEntity jeecgDemoExcel, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "excel?????????????????????????????????";
		try{
			jeecgDemoExcelService.save(jeecgDemoExcel);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "excel?????????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ??????excel?????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(JeecgDemoExcelEntity jeecgDemoExcel, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "excel?????????????????????????????????";
		JeecgDemoExcelEntity t = jeecgDemoExcelService.get(JeecgDemoExcelEntity.class, jeecgDemoExcel.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(jeecgDemoExcel, t);
			jeecgDemoExcelService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "excel?????????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	

	/**
	 * excel???????????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(JeecgDemoExcelEntity jeecgDemoExcel, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(jeecgDemoExcel.getId())) {
			jeecgDemoExcel = jeecgDemoExcelService.getEntity(JeecgDemoExcelEntity.class, jeecgDemoExcel.getId());
			req.setAttribute("jeecgDemoExcelPage", jeecgDemoExcel);
		}
		return new ModelAndView("com/jeecg/demo/excel/jeecgDemoExcel-add");
	}
	/**
	 * excel???????????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(JeecgDemoExcelEntity jeecgDemoExcel, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(jeecgDemoExcel.getId())) {
			jeecgDemoExcel = jeecgDemoExcelService.getEntity(JeecgDemoExcelEntity.class, jeecgDemoExcel.getId());
			req.setAttribute("jeecgDemoExcelPage", jeecgDemoExcel);
		}
		return new ModelAndView("com/jeecg/demo/excel/jeecgDemoExcel-update");
	}
	
	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		req.setAttribute("controller_name","jeecgDemoExcelController");
		return new ModelAndView("common/upload/pub_excel_upload");
	}
	
	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(JeecgDemoExcelEntity jeecgDemoExcel,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
		CriteriaQuery cq = new CriteriaQuery(JeecgDemoExcelEntity.class, dataGrid);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, jeecgDemoExcel, request.getParameterMap());
		List<JeecgDemoExcelEntity> jeecgDemoExcels = this.jeecgDemoExcelService.getListByCriteriaQuery(cq,false);
		modelMap.put(NormalExcelConstants.FILE_NAME,"excel?????????????????????");
		modelMap.put(NormalExcelConstants.CLASS,JeecgDemoExcelEntity.class);
		modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("excel???????????????????????????", "?????????:"+ResourceUtil.getSessionUser().getRealName(),
			"????????????"));
		modelMap.put(NormalExcelConstants.DATA_LIST,jeecgDemoExcels);
		return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	/**
	 * ??????excel ?????????
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public String exportXlsByT(JeecgDemoExcelEntity jeecgDemoExcel,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
    	modelMap.put(NormalExcelConstants.FILE_NAME,"excel?????????????????????");
    	modelMap.put(NormalExcelConstants.CLASS,JeecgDemoExcelEntity.class);
    	modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("excel???????????????????????????", "?????????:"+ResourceUtil.getSessionUser().getRealName(),
    	"????????????"));
    	modelMap.put(NormalExcelConstants.DATA_LIST,new ArrayList());
    	return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "importExcel", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson importExcel(HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();// ????????????????????????
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<JeecgDemoExcelEntity> listJeecgDemoExcelEntitys = ExcelImportUtil.importExcel(file.getInputStream(),JeecgDemoExcelEntity.class,params);
				for (JeecgDemoExcelEntity jeecgDemoExcel : listJeecgDemoExcelEntitys) {
					jeecgDemoExcelService.save(jeecgDemoExcel);
				}
				j.setMsg("?????????????????????");
			} catch (Exception e) {
				j.setMsg("?????????????????????");
				logger.error(e.getMessage());
			}finally{
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return j;
	}

	@RequestMapping("/ftl2word")
	public void velocity2word(JeecgDemoExcelEntity jeecgDemoExcel,HttpServletRequest request,HttpServletResponse response) throws IOException{
		try {
			jeecgDemoExcel = this.jeecgDemoExcelService.getEntity(JeecgDemoExcelEntity.class, jeecgDemoExcel.getId());
			List<Map<String,Object>> departs = this.systemService.findForJdbc("select id,departname from t_s_depart"); 
			String docFileName ="word-??????????????????.doc";
			Map<String,Object> rootMap = new HashMap<String,Object>();
			rootMap.put("info", jeecgDemoExcel);
			rootMap.put("departs", departs);
			FreemarkerUtil.createFile("ftl2doc.ftl", docFileName, rootMap, request, response, FreemarkerUtil.WORD_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping("/jxlsExportXls")
	public void jxlsExportXls(JeecgDemoExcelEntity jeecgDemoExcel,HttpServletRequest request,HttpServletResponse response) throws IOException{
		try {
			//????????????????????? ??????????????????map
			List<Map<String,Object>> departs = this.systemService.findForJdbc("select id,departname from t_s_depart"); 
			Map<String,Object> dptMap = new HashMap<String,Object>();
			for (Map<String, Object> map : departs) {
				dptMap.put(map.get("id").toString(), map.get("departname"));
			}
			//???????????????
			List<JeecgDemoExcelEntity> list = this.jeecgDemoExcelService.loadAll(JeecgDemoExcelEntity.class);
			//???????????????
			for (JeecgDemoExcelEntity temp : list) {
				String sex = temp.getSex();
				if("0".equals(sex)){
					sex = "??????";
				}else if("1".equals(sex)){
					sex = "??????";
				}
				temp.setSex(sex);
				Object depart =dptMap.get(temp.getDepart());
				temp.setDepart(depart==null?"":String.valueOf(depart));
			}
			//JXLS??????workbook
			Map<String,Object> beans =new HashMap<String,Object>();
			beans.put("datac",list);
			XLSTransformer transformer = new XLSTransformer(); 
			String srcFilePath = request.getSession().getServletContext().getRealPath("/")+"export/template/jxls.xls";
			InputStream is = new BufferedInputStream(new FileInputStream(srcFilePath));
	        org.apache.poi.ss.usermodel.Workbook workbook = transformer.transformXLS(is, beans);
	        //????????????
	        response.addHeader("Cache-Control","no-cache");
	        response.setCharacterEncoding("UTF-8");
	        response.setContentType("application/octet-stream;charset=UTF-8");
	        String ua = request.getHeader("user-agent");
	        ua = ua == null ? null : ua.toLowerCase();
	        String docFileName = "jxls??????excel-demo.xls";
	        if(ua != null && (ua.indexOf("firefox") > 0 || ua.indexOf("safari")>0)){
	        	try {
	        		docFileName = new String(docFileName.getBytes(),"ISO8859-1");
	        		response.addHeader("Content-Disposition","attachment;filename=" + docFileName);
				} catch (Exception e) {
				}
	        }else{
	        	try {
					docFileName = URLEncoder.encode(docFileName, "utf-8");
			        response.addHeader("Content-Disposition","attachment;filename=" + docFileName);
				} catch (Exception e) {
				}
	        }
	        ServletOutputStream out = response.getOutputStream();
			workbook.write(out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(params = "jxls")
	public ModelAndView jxls(HttpServletRequest req) {
		return new ModelAndView("com/jeecg/demo/excel/jxlsOne2manyIndex");
	}
	@RequestMapping(params = "mainlist")
	public ModelAndView mainlist(HttpServletRequest req) {
		return new ModelAndView("com/jeecg/demo/excel/jxlsOne2manyMain");
	}
	@RequestMapping(params = "sublist")
	public ModelAndView sublist(HttpServletRequest req) {
		return new ModelAndView("com/jeecg/demo/excel/jxlsOne2manySub");
	}
	@RequestMapping(params = "jxlsExportXlsOne2Many")
	public void jxlsExportXlsOne2Many(JformOrderMainEntity jformOrderMain,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid,ModelMap map) {
		CriteriaQuery cq = new CriteriaQuery(JformOrderMainEntity.class, dataGrid);
    	//?????????????????????
    	org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, jformOrderMain);
    	try{
        	cq.add();
        	List<JformOrderMainEntity> list=this.systemService.getListByCriteriaQuery(cq, false);
        	List<JformOrderMainPage> pageList=new ArrayList<JformOrderMainPage>();
        	if(list!=null&&list.size()>0){
            	for(JformOrderMainEntity entity:list){
            		try{
            		   JformOrderMainPage page=new JformOrderMainPage();
            		   MyBeanUtils.copyBeanNotNull2Bean(entity,page);
                	    Object id0 = entity.getId();
    				    String hql0 = "from JformOrderCustomerEntity where 1 = 1 AND fK_ID = ? ";
            	        List<JformOrderCustomerEntity> jformOrderCustomerEntityList = systemService.findHql(hql0,id0);
                		for (JformOrderCustomerEntity temp : jformOrderCustomerEntityList) {
                			String sex = temp.getSex();
            				if("0".equals(sex)){
            					sex = "??????";
            				}else if("1".equals(sex)){
            					sex = "??????";
            				}
            				temp.setSex(sex);
						}
            	        page.setJformOrderCustomerList(jformOrderCustomerEntityList);
                		pageList.add(page);
                	}catch(Exception e){
                		logger.info(e.getMessage());
                	}
                }
            }
        	Map<String,Object> beans =new HashMap<String,Object>();
    		beans.put("datac",pageList);
    		String repeat = request.getParameter("repeat");
    		String templateFilePath = request.getSession().getServletContext().getRealPath("/")+"export/template/jxlsone2many-"+repeat+".xls";
    		String exportFileName = "jxls??????excel-demo(?????????).xls";
    		JxlsExcelExportUtil.export(beans,exportFileName,templateFilePath, request, response);
    	}catch (Exception e) {
    		throw new BusinessException(e.getMessage());
    	}
	}

}
