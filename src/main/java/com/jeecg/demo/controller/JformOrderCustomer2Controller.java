package com.jeecg.demo.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecgframework.core.beanvalidator.BeanValidators;
import org.jeecgframework.core.common.controller.BaseController;
import org.jeecgframework.core.common.exception.BusinessException;
import org.jeecgframework.core.common.hibernate.qbc.CriteriaQuery;
import org.jeecgframework.core.common.model.json.AjaxJson;
import org.jeecgframework.core.common.model.json.DataGrid;
import org.jeecgframework.core.constant.Globals;
import org.jeecgframework.core.util.ExceptionUtil;
import org.jeecgframework.core.util.MyBeanUtils;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.jwt.util.ResponseMessage;
import org.jeecgframework.jwt.util.Result;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.jeecgframework.tag.core.easyui.TagUtil;
import org.jeecgframework.web.system.service.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.fastjson.JSONArray;
import com.jeecg.demo.entity.JformOrderCustomer2Entity;
import com.jeecg.demo.page.JformOrderMain2Page;
import com.jeecg.demo.service.JformOrderMain2ServiceI;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**   
 * @Title: Controller  
 * @Description: ??????????????????
 * @author onlineGenerator
 * @date 2018-03-27 17:02:39
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("/jformOrderCustomer2Controller")
@Api(value="JformOrderCustomer2",description="??????????????????",tags="jformOrderCustomer2Controller")
public class JformOrderCustomer2Controller extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(JformOrderCustomer2Controller.class);

	@Autowired
	private JformOrderMain2ServiceI jformOrderMain2Service;
	@Autowired
	private SystemService systemService;
	@Autowired
	private Validator validator;
	


	/**
	 * ???????????????????????? ????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("com/jeecg/demo/jformOrderMain2/jformOrderCustomer2/list");
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
	public void datagrid(JformOrderCustomer2Entity jformOrderCustomer2,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(JformOrderCustomer2Entity.class, dataGrid);
		String mainId = request.getParameter("mainId");
		if(oConvertUtils.isNotEmpty(mainId)){
			//?????????????????????
			org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, jformOrderCustomer2, request.getParameterMap());
			try{
			//???????????????????????????
				cq.eq("fkId", mainId);
			String query_money_begin = request.getParameter("money_begin");
			String query_money_end = request.getParameter("money_end");
			if(StringUtil.isNotEmpty(query_money_begin)){
				cq.ge("money", Double.parseDouble(query_money_begin));
			}
			if(StringUtil.isNotEmpty(query_money_end)){
				cq.le("money", Double.parseDouble(query_money_end));
			}
			}catch (Exception e) {
				throw new BusinessException(e.getMessage());
			}
			cq.add();
			this.jformOrderMain2Service.getDataGridReturn(cq, true);
		}
		TagUtil.datagrid(response, dataGrid);
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson doDel(JformOrderCustomer2Entity jformOrderCustomer2, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		jformOrderCustomer2 = systemService.getEntity(JformOrderCustomer2Entity.class, jformOrderCustomer2.getId());
		message = "??????????????????????????????";
		try{
			if(jformOrderCustomer2!=null){
				jformOrderMain2Service.delete(jformOrderCustomer2);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "??????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ??????????????????????????????
	 * 
	 * @return
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson doBatchDel(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "??????????????????????????????";
		try{
			for(String id:ids.split(",")){
				JformOrderCustomer2Entity jformOrderCustomer2 = systemService.getEntity(JformOrderCustomer2Entity.class, 
				id
				);
				if(jformOrderCustomer2!=null){
					jformOrderMain2Service.delete(jformOrderCustomer2);
					systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "??????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * ????????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson doAdd(JformOrderCustomer2Entity jformOrderCustomer2, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "??????????????????????????????";
		try{
			jformOrderMain2Service.save(jformOrderCustomer2);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "??????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * ????????????????????????
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson doUpdate(JformOrderCustomer2Entity jformOrderCustomer2, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "??????????????????????????????";
		JformOrderCustomer2Entity t = jformOrderMain2Service.get(JformOrderCustomer2Entity.class, jformOrderCustomer2.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(jformOrderCustomer2, t);
			jformOrderMain2Service.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "??????????????????????????????";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	

	/**
	 * ????????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goAdd")
	public ModelAndView goAdd(JformOrderCustomer2Entity jformOrderCustomer2, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(jformOrderCustomer2.getId())) {
			jformOrderCustomer2 = jformOrderMain2Service.getEntity(JformOrderCustomer2Entity.class, jformOrderCustomer2.getId());
			req.setAttribute("jformOrderCustomer2Page", jformOrderCustomer2);
		}
		req.setAttribute("mainId", req.getParameter("mainId"));
		return new ModelAndView("com/jeecg/demo/jformOrderMain2/jformOrderCustomer2/add");
	}
	/**
	 * ????????????????????????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "goUpdate")
	public ModelAndView goUpdate(JformOrderCustomer2Entity jformOrderCustomer2, HttpServletRequest req) {
		if (StringUtil.isNotEmpty(jformOrderCustomer2.getId())) {
			jformOrderCustomer2 = jformOrderMain2Service.getEntity(JformOrderCustomer2Entity.class, jformOrderCustomer2.getId());
			req.setAttribute("jformOrderCustomer2Page", jformOrderCustomer2);
		}
		return new ModelAndView("com/jeecg/demo/jformOrderMain2/jformOrderCustomer2/update");
	}
	
	/**
	 * ??????????????????
	 * 
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		req.setAttribute("controller_name","jformOrderCustomer2Controller");
		return new ModelAndView("common/upload/pub_excel_upload");
	}
	
	/**
	 * ??????excel
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(JformOrderCustomer2Entity jformOrderCustomer2,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
		CriteriaQuery cq = new CriteriaQuery(JformOrderCustomer2Entity.class, dataGrid);
		org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq, jformOrderCustomer2, request.getParameterMap());
		List<JformOrderCustomer2Entity> jformOrderCustomer2s = this.jformOrderMain2Service.getListByCriteriaQuery(cq,false);
		modelMap.put(NormalExcelConstants.FILE_NAME,"??????????????????");
		modelMap.put(NormalExcelConstants.CLASS,JformOrderCustomer2Entity.class);
		modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("????????????????????????", "?????????:"+ResourceUtil.getSessionUser().getRealName(),
			"????????????"));
		modelMap.put(NormalExcelConstants.DATA_LIST,jformOrderCustomer2s);
		return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}
	/**
	 * ??????excel ?????????
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public String exportXlsByT(JformOrderCustomer2Entity jformOrderCustomer2,HttpServletRequest request,HttpServletResponse response
			, DataGrid dataGrid,ModelMap modelMap) {
    	modelMap.put(NormalExcelConstants.FILE_NAME,"??????????????????");
    	modelMap.put(NormalExcelConstants.CLASS,JformOrderCustomer2Entity.class);
    	modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("????????????????????????", "?????????:"+ResourceUtil.getSessionUser().getRealName(),
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
				List<JformOrderCustomer2Entity> listJformOrderCustomer2Entitys = ExcelImportUtil.importExcel(file.getInputStream(),JformOrderCustomer2Entity.class,params);
				for (JformOrderCustomer2Entity jformOrderCustomer2 : listJformOrderCustomer2Entitys) {
					jformOrderMain2Service.save(jformOrderCustomer2);
				}
				j.setMsg("?????????????????????");
			} catch (Exception e) {
				j.setMsg("?????????????????????");
				logger.error(ExceptionUtil.getExceptionMessage(e));
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
	
	
	/**
	 * ???????????????
	 * @param page
	 * @param req
	 * @return
	 */
	@RequestMapping(params = "saveRows")
	@ResponseBody
	public AjaxJson saveRows(JformOrderMain2Page page,HttpServletRequest req){
		String message = "???????????????";
		List<JformOrderCustomer2Entity> lists=page.getJformOrderCustomer2List();
		AjaxJson j = new AjaxJson();
		String mainId = req.getParameter("mainId");
		if(CollectionUtils.isNotEmpty(lists)){
			for(JformOrderCustomer2Entity temp:lists){
				if (StringUtil.isNotEmpty(temp.getId())) {
					JformOrderCustomer2Entity t =this.systemService.get(JformOrderCustomer2Entity.class, temp.getId());
					try {
						MyBeanUtils.copyBeanNotNull2Bean(temp, t);
						systemService.saveOrUpdate(t);
						systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						//temp.setDelFlag(0);??????????????????
						temp.setFkId(mainId);
						systemService.save(temp);
						systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return j;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="??????????????????????????????",produces="application/json",httpMethod="GET")
	public ResponseMessage<List<JformOrderCustomer2Entity>> list() {
		List<JformOrderCustomer2Entity> listJformOrderCustomer2s=jformOrderMain2Service.getList(JformOrderCustomer2Entity.class);
		return Result.success(listJformOrderCustomer2s);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	@ApiOperation(value="??????ID??????????????????????????????",notes="??????ID??????????????????????????????",httpMethod="GET",produces="application/json")
	public ResponseMessage<?> get(@ApiParam(required=true,name="id",value="ID")@PathVariable("id") String id) {
		JformOrderCustomer2Entity task = jformOrderMain2Service.get(JformOrderCustomer2Entity.class, id);
		if (task == null) {
			return Result.error("??????ID????????????????????????????????????");
		}
		return Result.success(task);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="????????????????????????")
	public ResponseMessage<?> create(@ApiParam(name="????????????????????????")@RequestBody JformOrderCustomer2Entity jformOrderCustomer2, UriComponentsBuilder uriBuilder) {
		//??????JSR303 Bean Validator????????????????????????????????????400????????????json?????????????????????.
		Set<ConstraintViolation<JformOrderCustomer2Entity>> failures = validator.validate(jformOrderCustomer2);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//??????
		try{
			jformOrderMain2Service.save(jformOrderCustomer2);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("????????????????????????????????????");
		}
		return Result.success(jformOrderCustomer2);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ApiOperation(value="????????????????????????",notes="????????????????????????")
	public ResponseMessage<?> update(@ApiParam(name="????????????????????????")@RequestBody JformOrderCustomer2Entity jformOrderCustomer2) {
		//??????JSR303 Bean Validator????????????????????????????????????400????????????json?????????????????????.
		Set<ConstraintViolation<JformOrderCustomer2Entity>> failures = validator.validate(jformOrderCustomer2);
		if (!failures.isEmpty()) {
			return Result.error(JSONArray.toJSONString(BeanValidators.extractPropertyAndMessage(failures)));
		}

		//??????
		try{
			jformOrderMain2Service.saveOrUpdate(jformOrderCustomer2);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("????????????????????????????????????");
		}

		//???Restful???????????????204?????????, ?????????. ???????????????200?????????.
		return Result.success("????????????????????????????????????");
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation(value="????????????????????????")
	public ResponseMessage<?> delete(@ApiParam(name="id",value="ID",required=true)@PathVariable("id") String id) {
		logger.info("delete[{}]" , id);
		// ??????
		if (StringUtils.isEmpty(id)) {
			return Result.error("ID????????????");
		}
		try {
			jformOrderMain2Service.deleteEntityById(JformOrderCustomer2Entity.class, id);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("??????????????????????????????");
		}

		return Result.success();
	}
}
