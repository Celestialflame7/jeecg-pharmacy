package org.jeecgframework.web.cgform.service.impl.build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jeecgframework.core.common.service.impl.CommonServiceImpl;
import org.jeecgframework.core.constant.DataBaseConstant;
import org.jeecgframework.core.util.ApplicationContextUtil;
import org.jeecgframework.core.util.DBTypeUtil;
import org.jeecgframework.core.util.DateUtils;
import org.jeecgframework.core.util.MyClassLoader;
import org.jeecgframework.core.util.ResourceUtil;
import org.jeecgframework.core.util.SqlInjectionUtil;
import org.jeecgframework.core.util.StringUtil;
import org.jeecgframework.core.util.UUIDGenerator;
import org.jeecgframework.core.util.oConvertUtils;
import org.jeecgframework.minidao.util.FreemarkerParseFactory;
import org.jeecgframework.web.cgform.common.CgAutoListConstant;
import org.jeecgframework.web.cgform.common.CommUtils;
import org.jeecgframework.web.cgform.enhance.CgformEnhanceJavaInter;
import org.jeecgframework.web.cgform.entity.button.CgformButtonSqlEntity;
import org.jeecgframework.web.cgform.entity.config.CgFormFieldEntity;
import org.jeecgframework.web.cgform.entity.config.CgFormHeadEntity;
import org.jeecgframework.web.cgform.entity.enhance.CgformEnhanceJavaEntity;
import org.jeecgframework.web.cgform.exception.BusinessException;
import org.jeecgframework.web.cgform.service.build.DataBaseService;
import org.jeecgframework.web.cgform.service.config.CgFormFieldServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.jdbc.support.incrementer.OracleSequenceMaxValueIncrementer;
import org.springframework.jdbc.support.incrementer.PostgreSQLSequenceMaxValueIncrementer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @ClassName: DataBaseServiceImpl
 * @Description: (????????????????????????service)
 * @author zhoujunfeng
 */
@Service("dataBaseService")
//--author???luobaoli---------date:20150616--------for: ??????spring???????????????????????????????????????BusinessException
@Transactional(rollbackFor=Exception.class)
//--author???luobaoli---------date:20150616--------for: ??????spring???????????????????????????????????????BusinessException
public class DataBaseServiceImpl extends CommonServiceImpl implements DataBaseService{
	private static final Logger logger = Logger.getLogger(DataBaseServiceImpl.class);
	@Autowired
	private CgFormFieldServiceI cgFormFieldService;
	@Autowired
	private AbstractRoutingDataSource dataSource;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**
	 * ????????????
	 * @param tableName ??????
	 * @param data ???????????????map
	 * @throws BusinessException
	 */

	public void insertTable(String tableName, Map<String, Object> data) throws BusinessException {
		CgFormHeadEntity cgFormHeadEntity = cgFormFieldService.getCgFormHeadByTableName(tableName);

		executeJavaExtend(cgFormHeadEntity.getId(),"add",data,"start");

		//???????????????????????????
		fillInsertSysVar(tableName,data);
		//?????????????????????????????????????????????????????????
		keyAdapter(cgFormHeadEntity,data);
		//?????????????????????date???int???double?????????
		dataAdapter(tableName,data);
		String comma = "";
		StringBuffer insertKey = new StringBuffer();
		StringBuffer insertValue = new StringBuffer();
		for (Entry<String, Object> entry : data.entrySet()) {
			// ??????key???????????????????????????
			if(isContainsFieled(tableName,entry.getKey())){
				//??????SQL??????,??????????????????,???????????????
				insertKey.append(comma  + entry.getKey());
				if(entry.getValue()!=null && entry.getValue().toString().length()>0){
					insertValue.append(comma + ":"+entry.getKey());
				}else{
					insertValue.append(comma + "null");
				}
				comma = ", ";

			}
		}
		String sql = "INSERT INTO " + tableName + " (" + insertKey + ") VALUES (" + insertValue + ")";
		Object key = null;

		key = this.executeSqlReturnKey(sql,data);

		if(key!=null && key instanceof Long){
			data.put("id", key);
		}
		if(cgFormHeadEntity!=null){
			executeSqlExtend(cgFormHeadEntity.getId(),"add",data);

			executeJavaExtend(cgFormHeadEntity.getId(),"add",data,"end");

		}
	}
	/**
	 * ???????????????????????????????????????Insert???sql??????
	 * @param cgFormHeadEntity ????????????
	 * @param data	???????????????
	 */
	private void keyAdapter(CgFormHeadEntity cgFormHeadEntity,
			Map<String, Object> data) {
		String pkType = cgFormHeadEntity.getJformPkType();
		String dbType = DBTypeUtil.getDBType();
		if("NATIVE".equalsIgnoreCase(pkType)||"SEQUENCE".equalsIgnoreCase(pkType)){
			if("sqlserver".equalsIgnoreCase(dbType)){
				//????????????????????????,sqlserver???insert?????????????????????????????????
				data.remove("id");
			}
		}
	}
	/**
	 * ??????????????????-?????????????????????????????????????????????????????????map-value????????????????????????
	 * @param tableName ?????????
	 * @param data ??????
	 */
	private Map<String, Object> dataAdapter(String tableName,Map<String, Object> data) {

		SqlInjectionUtil.filterContent(tableName);

		//step.1 ???????????????????????????
		Map<String, CgFormFieldEntity> fieldConfigs =cgFormFieldService.getAllCgFormFieldByTableName(tableName);
		//step.2 ??????????????????????????????
		Iterator it = fieldConfigs.keySet().iterator();
		for(;it.hasNext();){
			Object key = it.next();
			//?????????????????????????????? ?????? ????????????
			Object beforeV = data.get(key.toString().toLowerCase());

			//??????????????????
			if(oConvertUtils.isNotEmpty(beforeV)){
				//??????????????????-????????????
				CgFormFieldEntity fieldConfig = fieldConfigs.get(key);
				String type = fieldConfig.getType();
				//??????????????????????????????
				if("date".equalsIgnoreCase(type)){
					//??????->java.util.Date
					Object newV = String.valueOf(beforeV);
					try {
						//--author???zhoujf---start------date:20170207--------for:?????????????????????
						String dateStr = String.valueOf(beforeV);
						if (dateStr.indexOf(":") == -1 && dateStr.length() == 10) {
							newV = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
						} else if (dateStr.indexOf(":") > 0 && dateStr.length() == 19) {
							newV =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);

						}else if (dateStr.indexOf(":") > 0 && dateStr.indexOf(".0") > 0 && dateStr.length()== 21) {
							newV =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr.substring(0, dateStr.indexOf(".0")));

						}else if(dateStr.indexOf(":") != -1){
							newV =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
						}else{
							newV =  new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
						}
						
						/*String dateType = fieldConfig.getShowType();
						if("datetime".equalsIgnoreCase(dateType)){
							newV =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(String.valueOf(beforeV));
						}else if("date".equalsIgnoreCase(dateType)){
							newV = new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(beforeV));
						}*/
						if(data.containsKey(key)){
							data.put(String.valueOf(key), newV);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}else if("int".equalsIgnoreCase(type)){
					//int->java.lang.Integer
					Object newV = null;
					try{
						newV = Integer.parseInt(String.valueOf(beforeV));
					}catch (Exception e) {
						e.printStackTrace();
					}
					if(data.containsKey(key)){
						data.put(String.valueOf(key), newV);
					}
				}else if("double".equalsIgnoreCase(type)){
					//double->java.lang.Double
					Object newV = new Double(0);
					try{
						newV = Double.parseDouble(String.valueOf(beforeV));
					}catch (Exception e) {
						e.printStackTrace();
					}
					if(data.containsKey(key)){
						data.put(String.valueOf(key), newV);
					}
				}

			} else if(oConvertUtils.isNotEmpty(fieldConfigs.get(key).getFieldDefault())) {
				data.remove(key.toString().toLowerCase());
			}

		}
		return data;
	}

	/**
	 * ????????????
	 * @param tableName ??????
	 * @param id ?????????id
	 * @param data ???????????????map
	 */

	public int updateTable(String tableName, Object id, Map<String, Object> data) throws BusinessException {
		fillUpdateSysVar(tableName,data);
		dataAdapter(tableName,data);
		String comma = "";
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("update ").append(tableName).append(" set ");
		for (Entry<String, Object> entry : data.entrySet()) {
			// ??????key???????????????????????????
			if(isContainsFieled(tableName,entry.getKey())){
				if(entry.getValue()!=null&&entry.getValue().toString().length()>0){
					sqlBuffer.append(comma).append(entry.getKey()).append("=:"+entry.getKey()+" ");
				}else{
					sqlBuffer.append(comma).append(entry.getKey()).append("=null");
				}
				comma = ", ";
			}
		}

//		if(id instanceof java.lang.String){
//			sqlBuffer.append(" where id='").append(id).append("'");
//		}else{
//			sqlBuffer.append(" where id=").append(id);
//		}
		sqlBuffer.append(" where id=:id");
		data.put("id", id);

		CgFormHeadEntity cgFormHeadEntity = cgFormFieldService.getCgFormHeadByTableName(tableName);

		executeJavaExtend(cgFormHeadEntity.getId(),"update",data, "start");

		int num = this.executeSql(sqlBuffer.toString(), data);

		if(cgFormHeadEntity!=null){

			data.put("id", id);

			executeSqlExtend(cgFormHeadEntity.getId(),"update",data);

			executeJavaExtend(cgFormHeadEntity.getId(),"update",data, "end");

		}
		return num;
	}


	/**
	 * ????????????
	 * @param tableName ??????
	 * @param id ?????????id
	 */

	public Map<String, Object> findOneForJdbc(String tableName, String id) {

		SqlInjectionUtil.filterContent(tableName);

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("select * from ").append(tableName);
		sqlBuffer.append(" where id= ? ");
		Map<String, Object> map = commonDao.findOneForJdbc(sqlBuffer.toString(), id);
		return map;
	}
	/**
	 * sql????????????
	 *
	 */
	public void executeSqlExtend(String formId,String buttonCode,Map<String, Object> data) throws BusinessException{
		//??????formId???buttonCode??????
		CgformButtonSqlEntity cgformButtonSqlVo = getCgformButtonSqlByCodeFormId(buttonCode,formId);
		if(cgformButtonSqlVo!=null){
			//??????sql??????????????????
			String sqlPlugin = cgformButtonSqlVo.getCgbSqlStr();
			if(StringUtils.isNotEmpty(sqlPlugin)){

				boolean isMiniDao = false;
				logger.debug("sql plugin -------->"+sqlPlugin);						
				String sqlPluginTemp = formateSQl(sqlPlugin,  data);
				logger.debug("sql formate plugin -------->"+sqlPluginTemp);
				try{
					//???????????????????????????????????????????????????
					data = minidaoReplaceExtendSqlSysVar(data);
					sqlPlugin = FreemarkerParseFactory.parseTemplateContent(sqlPluginTemp, data);
					isMiniDao = true;
				}catch(Exception e){
					logger.debug(e.getMessage());
					logger.debug("minidao??????????????????????????????sql??????");
					sqlPlugin = sqlPluginTemp;
				}

				String [] sqls = sqlPlugin.split(";");
				for(String sql:sqls){

					//?????????????????????????????????sql??????
					if(sql == null || sql.toLowerCase().trim().equals("")){
						continue;
					}

					/*if(sql.toLowerCase().indexOf(CgAutoListConstant.SQL_INSERT)!=-1
							||sql.toLowerCase().indexOf(CgAutoListConstant.SQL_UPDATE)!=-1){*/
					if(true){

						//??????sql

						int num = 0;
						if(isMiniDao){
							try {
								num = namedParameterJdbcTemplate.update(sql, data);
							} catch (Throwable e) {

								throw new BusinessException(e.getMessage());

							}
						}else{
							num = this.executeSql(sql);
						}

						if(num>0){
							logger.debug("sql plugin --execute success------>"+sql);
						}else{
							logger.debug("sql plugin --execute fail------>"+sql);
						}
					}
				}
		}
		}
	}
	
	private CgformButtonSqlEntity getCgformButtonSqlByCodeFormId(String buttonCode, String formId) {
		StringBuilder hql = new StringBuilder("");
		hql.append(" from CgformButtonSqlEntity t");
		hql.append(" where t.formId=?");
		hql.append(" and  t.buttonCode =?");
		List<CgformButtonSqlEntity> list = this.findHql(hql.toString(),formId,buttonCode);
		if(list!=null&&list.size()>0){
			return list.get(0);
		}
		return null;
	}


	/**
	 * sql?????????
	 * @param sql
	 * @param params
	 * @return
	 */
	private String formateSQl(String sql, Map<String, Object> params) {
		sql = replaceExtendSqlSysVar(sql);
		if (params == null) {
			return sql;
		}
		if(sql.toLowerCase().indexOf(CgAutoListConstant.SQL_INSERT)!=-1){
			sql = sql.replace("#{UUID}", UUIDGenerator.generate());
		}
		for (String key : params.keySet()) {

//          sql = sql.replace("${" + key + "}", "'"+String.valueOf(params.get(key))+"'");
			sql = sql.replace("#{" + key + "}",String.valueOf(params.get(key)));

		}
		return sql;
	}

	@SuppressWarnings("unchecked")

	public Map<String, Object> insertTableMore(Map<String, List<Map<String, Object>>> mapMore, String mainTableName) throws BusinessException {
		//??????????????????
		Map<String, Object> mainMap = mapMore.get(mainTableName).get(0);
		//????????????
	    String [] filterName = {"tableName","saveOrUpdateMore"};
	    mainMap = CommUtils.attributeMapFilter(mainMap,filterName);
	    if(mainMap.get("id")==null||"".equals((String)mainMap.get("id"))){
		    Object pkValue = getPkValue(mainTableName);
		    mainMap.put("id", pkValue);
	    }
		insertTable(mainTableName, mainMap);
		//??????????????????
		//??????????????????
		String [] filterMainTable = {mainTableName};
		mapMore = CommUtils.attributeMapFilter(mapMore,filterMainTable);
		Iterator it=mapMore.entrySet().iterator();
		while(it.hasNext()){
	        Map.Entry entry=(Map.Entry)it.next();
	        String ok=(String)entry.getKey();
	        List<Map<String, Object>> ov=(List<Map<String, Object>>)entry.getValue();
	        for(Map<String, Object> fieldMap:ov){
	        	//???????????????
	        	List<Map<String, Object>> fkFieldList =  getFKField(mainTableName, ok);
	        	Object subPkValue = getPkValue(ok);
	        	fieldMap.put("id", subPkValue);
	        	fieldMap = CommUtils.convertFKMap(fieldMap,mainMap,fkFieldList);
	        	insertTable(ok, fieldMap);
	        }
		}
		return mainMap;
	}

	@SuppressWarnings("unchecked")

	public boolean updateTableMore(Map<String, List<Map<String, Object>>> mapMore, String mainTableName) throws BusinessException {
		//??????????????????
		Map<String, Object> mainMap = mapMore.get(mainTableName).get(0);
		Object mainTableId = mainMap.get("id");
		//????????????
	    String [] filterName =  {"tableName","saveOrUpdateMore","id"};
	    mainMap = CommUtils.attributeMapFilter(mainMap,filterName);
		updateTable(mainTableName,mainTableId, mainMap);
		mainMap.put("id", mainTableId);
		//??????????????????
		String [] filterMainTable = {mainTableName};
		mapMore = CommUtils.attributeMapFilter(mapMore,filterMainTable);
		Iterator it=mapMore.entrySet().iterator();
		while(it.hasNext()){
	        Map.Entry entry=(Map.Entry)it.next();
	        String ok=(String)entry.getKey();
	        List<Map<String, Object>> ov=(List<Map<String, Object>>)entry.getValue();
	        //???????????????
        	List<Map<String, Object>> fkFieldList =  getFKField(mainTableName, ok);
        	//????????????id??????????????????
        	Map<Object,Map<String, Object>> subTableDateMap = getSubTableData(fkFieldList, mainTableName, ok, mainTableId);
	        for(Map<String, Object> fieldMap:ov){
	        	Object subId = fieldMap.get("id")==null?"":fieldMap.get("id");
	        	if(subId==null || "".equals(String.valueOf(subId))){
	        		fieldMap = CommUtils.convertFKMap(fieldMap,mainMap,fkFieldList);
		        	fieldMap.put("id", getPkValue(ok));
		        	insertTable(ok, fieldMap);
	        	}else{
	        		fieldMap = CommUtils.convertFKMap(fieldMap,mainMap,fkFieldList);
	        		//????????????
	        	    String [] subFilterName =  {"id"};
	        	    fieldMap = CommUtils.attributeMapFilter(fieldMap,subFilterName);
	        		updateTable(ok,subId,fieldMap);
	        		//??????????????????????????????
	        		if(subTableDateMap.containsKey(subId)){
	        			subTableDateMap.remove(subId);
	        		}
	        	}
	        }
	        //subTableDateMap???????????????????????????????????????
	        if(subTableDateMap.size()>0){
	        	Iterator itSub=subTableDateMap.entrySet().iterator();
		    	while(itSub.hasNext()){
		    		Map.Entry entrySub=(Map.Entry)itSub.next();
		    		Object subId=entrySub.getKey();
		    		deleteSubTableDataById(subId,ok);
		    	}
	        }
		}
		return true;
	}

	/**
	 * ???????????????????????????
	 *
	 * @param mainTableName ?????????
	 * @param subTableName  ?????????
	 * @return
	 */
	private List<Map<String, Object>> getFKField(String mainTableName, String subTableName) {
		StringBuilder sql1 = new StringBuilder("");
		sql1.append("select f.* from cgform_field f ,cgform_head h");
		sql1.append(" where f.table_id = h.id ");
		sql1.append(" and h.table_name=? ");
		sql1.append(" and f.main_table=? ");
		List<Map<String,Object>> list = this.findForJdbc(sql1.toString(), subTableName,mainTableName);
		return list;
	}

	/**
	 * ????????????id??????????????????
	 *
	 * @param fkFieldList ?????????????????????
	 * @param mainTableName ?????????
	 * @param subTableName  ?????????
	 * @param mainTableId   ??????id
	 * @return
	 */
	private Map<Object,Map<String, Object>> getSubTableData(List<Map<String, Object>> fkFieldList,String mainTableName, String subTableName,Object mainTableId) {

		StringBuilder sql2 = new StringBuilder("");
		sql2.append("select sub.* from ").append(subTableName).append(" sub ");
		sql2.append(", ").append(mainTableName).append(" main ");
		sql2.append("where 1=1 ");
		if(fkFieldList!=null&&fkFieldList.size()>0){
			for(Map<String,Object> map :fkFieldList){
				if(map.get("main_field")!=null){
					sql2.append(" and sub.").append((String)map.get("field_name")).append("=").append("main.").append((String)map.get("main_field"));
				}
			}
		}
		sql2.append(" and main.id= ? ");
		List<Map<String,Object>> subTableDataList = this.findForJdbc(sql2.toString(),mainTableId);
		Map<Object,Map<String, Object>> dataMap = new HashMap<Object, Map<String,Object>>();
		if(subTableDataList!=null){
			for(Map<String,Object> map:subTableDataList){

				dataMap.put(map.get("id").toString(), map);

			}
		}
		return dataMap;
	}
	/**
	 * ????????????????????????????????????????????????
	 * @param tableName ????????????
	 * @return
	 */
	public Object getPkValue(String tableName) {
		Object pkValue = null;
		CgFormHeadEntity  cghead = cgFormFieldService.getCgFormHeadByTableName(tableName);
		String dbType = DBTypeUtil.getDBType();
		String pkType = cghead.getJformPkType();
		String pkSequence = cghead.getJformPkSequence();
		if(StringUtil.isNotEmpty(pkType)&&"UUID".equalsIgnoreCase(pkType)){
			pkValue = UUIDGenerator.generate();
		}else if(StringUtil.isNotEmpty(pkType)&&"NATIVE".equalsIgnoreCase(pkType)){
			if(StringUtil.isNotEmpty(dbType)&&"oracle".equalsIgnoreCase(dbType)){
				OracleSequenceMaxValueIncrementer incr = new OracleSequenceMaxValueIncrementer(dataSource, "HIBERNATE_SEQUENCE");
				try{
					pkValue = incr.nextLongValue();
				}catch (Exception e) {
					logger.error(e,e);
				}
			}else if(StringUtil.isNotEmpty(dbType)&&"postgres".equalsIgnoreCase(dbType)){
				PostgreSQLSequenceMaxValueIncrementer incr = new PostgreSQLSequenceMaxValueIncrementer(dataSource, "HIBERNATE_SEQUENCE");
				try{
					pkValue = incr.nextLongValue();
				}catch (Exception e) {
					logger.error(e,e);
				}
			}else{
				pkValue = null;
			}
		}else if(StringUtil.isNotEmpty(pkType)&&"SEQUENCE".equalsIgnoreCase(pkType)){
			if(StringUtil.isNotEmpty(dbType)&&"oracle".equalsIgnoreCase(dbType)){
				OracleSequenceMaxValueIncrementer incr = new OracleSequenceMaxValueIncrementer(dataSource, pkSequence);
				try{
					pkValue = incr.nextLongValue();
				}catch (Exception e) {
					logger.error(e,e);
				}
			}else if(StringUtil.isNotEmpty(dbType)&&"postgres".equalsIgnoreCase(dbType)){
				PostgreSQLSequenceMaxValueIncrementer incr = new PostgreSQLSequenceMaxValueIncrementer(dataSource, pkSequence);
				try{
					pkValue = incr.nextLongValue();
				}catch (Exception e) {
					logger.error(e,e);
				}
			}else{
				pkValue = null;
			}
		}else{
			pkValue = UUIDGenerator.generate();
		}
		return pkValue;
	}
	/**
	 * ??????id??????????????????
	 *
	 * @param subId ????????????
	 * @param subTableName  ?????????
	 * @return
	 */
	private void deleteSubTableDataById(Object subId,String subTableName){
		StringBuilder sql = new StringBuilder("");

		SqlInjectionUtil.filterContent(subTableName);

		sql.append(" delete from ").append(subTableName).append(" where id = ? ");

		this.executeSql(sql.toString(), subId);
	}
	/**
	 * ???????????????????????????????????????????????????
	 * @param data
	 */
	private void fillUpdateSysVar(String tableName,Map<String, Object> data) {

		if (ResourceUtil.getSessionUser()==null){
			return;
		}

		//--author???zhoujf---start------date:20170207--------for:online?????? ??????????????????????????????????????????
		//--author???zhoujf---start------date:20170207--------for:???????????????????????????yyyy-MM-dd HH:mm:ss
		if(data.containsKey(DataBaseConstant.UPDATE_DATE_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_DATE_TABLE)){
			data.put(DataBaseConstant.UPDATE_DATE_TABLE, DateUtils.formatDateTime());
		}
		if(data.containsKey(DataBaseConstant.UPDATE_TIME_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_DATE_TABLE)){
			data.put(DataBaseConstant.UPDATE_TIME_TABLE, DateUtils.formatDateTime());
		}
		if(data.containsKey(DataBaseConstant.UPDATE_BY_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_DATE_TABLE)){
			data.put(DataBaseConstant.UPDATE_BY_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_CODE));
		}
		if(data.containsKey(DataBaseConstant.UPDATE_NAME_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_DATE_TABLE)){
			data.put(DataBaseConstant.UPDATE_NAME_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_NAME));
		}
	}

	/**
	 * ???????????????????????????????????????????????????
	 * @param data
	 */
	private void fillInsertSysVar(String tableName,Map<String, Object> data) {

		if (ResourceUtil.getSessionUser()==null){
			return;
		}

		//--author???zhoujf---start------date:20170207--------for:online?????? Excel????????????????????????????????????????????????
		//--author???zhoujf---start------date:20170207--------for:???????????????????????????yyyy-MM-dd HH:mm:ss
		if(data.containsKey(DataBaseConstant.CREATE_DATE_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_DATE_TABLE)){
			data.put(DataBaseConstant.CREATE_DATE_TABLE, DateUtils.formatDateTime());
		}
		if(data.containsKey(DataBaseConstant.CREATE_TIME_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_TIME_TABLE)){
			data.put(DataBaseConstant.CREATE_TIME_TABLE, DateUtils.formatDateTime());
		}
		if(data.containsKey(DataBaseConstant.CREATE_BY_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_BY_TABLE)){
			data.put(DataBaseConstant.CREATE_BY_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_CODE));
		}
		if(data.containsKey(DataBaseConstant.CREATE_NAME_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.CREATE_NAME_TABLE)){
			data.put(DataBaseConstant.CREATE_NAME_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_NAME));
		}
		if(data.containsKey(DataBaseConstant.SYS_COMPANY_CODE_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.SYS_COMPANY_CODE_TABLE)){
			data.put(DataBaseConstant.SYS_COMPANY_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_COMPANY_CODE));
		}
		if(data.containsKey(DataBaseConstant.SYS_ORG_CODE_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.SYS_ORG_CODE_TABLE)){
			data.put(DataBaseConstant.SYS_ORG_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_ORG_CODE));
		}
		if(data.containsKey(DataBaseConstant.SYS_USER_CODE_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.SYS_USER_CODE_TABLE)){
			data.put(DataBaseConstant.SYS_USER_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_CODE));
		}
		if(data.containsKey(DataBaseConstant.BPM_STATUS_TABLE)
				||getAllFieldByTableName(tableName).containsKey(DataBaseConstant.BPM_STATUS_TABLE)){
			data.put(DataBaseConstant.BPM_STATUS_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.BPM_STATUS_TABLE));
		}
	}
	
	/**
	 * ???Minidao?????????
	 * ???Sql?????????????????????????????????
	 * @param sql
	 * @return
	 */
	private Map<String, Object> minidaoReplaceExtendSqlSysVar(Map<String, Object> data){
		data.put("sys."+DataBaseConstant.SYS_USER_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_CODE));
		data.put("sys."+DataBaseConstant.SYS_USER_NAME_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_NAME));
		data.put("sys."+DataBaseConstant.SYS_ORG_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_ORG_CODE));
		data.put("sys."+DataBaseConstant.SYS_COMPANY_CODE_TABLE, ResourceUtil.getUserSystemData(DataBaseConstant.SYS_COMPANY_CODE));
		data.put("sys."+DataBaseConstant.SYS_DATE_TABLE, DateUtils.formatDate());
		data.put("sys."+DataBaseConstant.SYS_TIME_TABLE, DateUtils.formatTime());
		return data;
	}
	
	/**
	 * ???????????????
	 * ???Sql?????????????????????????????????
	 * @param sql
	 * @return
	 */
	private String replaceExtendSqlSysVar(String sql){
		sql = sql.replace("#{sys."+DataBaseConstant.SYS_USER_CODE_TABLE+"}", ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_CODE))
				.replace("#{sys."+DataBaseConstant.SYS_USER_NAME_TABLE+"}", ResourceUtil.getUserSystemData(DataBaseConstant.SYS_USER_NAME))
				.replace("#{sys."+DataBaseConstant.SYS_ORG_CODE_TABLE+"}", ResourceUtil.getUserSystemData(DataBaseConstant.SYS_ORG_CODE))
				.replace("#{sys."+DataBaseConstant.SYS_COMPANY_CODE_TABLE+"}", ResourceUtil.getUserSystemData(DataBaseConstant.SYS_COMPANY_CODE))
				.replace("#{sys."+DataBaseConstant.SYS_DATE_TABLE+"}",  DateUtils.formatDate())
				.replace("#{sys."+DataBaseConstant.SYS_TIME_TABLE+"}",  DateUtils.formatTime());
		return sql;
	}

	private Map<String,CgFormFieldEntity> getAllFieldByTableName(String tableName){
		//???????????????
        String version = cgFormFieldService.getCgFormVersionByTableName(tableName);
        Map<String,CgFormFieldEntity> map  = cgFormFieldService.getAllCgFormFieldByTableName(tableName, version);
		return map;
	}

	//??????key???????????????????????????
	private boolean isContainsFieled(String tableName,String fieledName){
		boolean flag = false;
		if(getAllFieldByTableName(tableName).containsKey(fieledName)){
			flag = true;
		}
		return flag;
	}

	/**
	 * ??????JAVA???????????????
	 */
	@Override
	public void executeJavaExtend(String formId, String buttonCode,Map<String, Object> data) throws BusinessException{
		CgformEnhanceJavaEntity cgformEnhanceJavaEntity = getCgformEnhanceJavaEntityByCodeFormId(buttonCode,formId);
		if(cgformEnhanceJavaEntity!=null){
			String cgJavaType = cgformEnhanceJavaEntity.getCgJavaType();
			String cgJavaValue = cgformEnhanceJavaEntity.getCgJavaValue();

			if(StringUtil.isNotEmpty(cgJavaValue)){
				Object obj = null;

//				try {
					if("class".equals(cgJavaType)){
						//???????????????????????????????????????????????????????????????????????????????????????????????????
						try {
							obj = MyClassLoader.getClassByScn(cgJavaValue).newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}else if("spring".equals(cgJavaType)){
						obj = ApplicationContextUtil.getContext().getBean(cgJavaValue);
					}
					if(obj instanceof CgformEnhanceJavaInter){

						CgFormHeadEntity head = this.get(CgFormHeadEntity.class, formId);
						CgformEnhanceJavaInter javaInter = (CgformEnhanceJavaInter) obj;
						javaInter.execute(head.getTableName(),data);

					}
//				} catch (Exception e) {
//					logger.error(e.getMessage());
//					e.printStackTrace();
//					throw new BusinessException("??????JAVA?????????????????????");
//				} 

			}

		}
	}
	
	public CgformEnhanceJavaEntity getCgformEnhanceJavaEntityByCodeFormId(String buttonCode, String formId) {
		StringBuilder hql = new StringBuilder("");
		hql.append(" from CgformEnhanceJavaEntity t");

		hql.append(" where t.formId=?");
		hql.append(" and  t.buttonCode =?");

		hql.append(" and  t.activeStatus ='1'");

		hql.append(" and t.event = 'end' ");
		List<CgformEnhanceJavaEntity> list = this.findHql(hql.toString(),formId,buttonCode);

		if(list!=null&&list.size()>0){
			return list.get(0);
		}
		return null;
	}

	public List<CgformEnhanceJavaEntity> getCgformEnhanceJavaEntityByFormId( String formId) {
		StringBuilder hql = new StringBuilder("");
		hql.append(" from CgformEnhanceJavaEntity t");
		hql.append(" where t.formId=?");
		List<CgformEnhanceJavaEntity> list = this.findHql(hql.toString(),formId);
		return list;
	}

	
	@Override
	public void executeJavaExtend(String formId, String buttonCode,
			Map<String, Object> data, String event) throws BusinessException {

		CgformEnhanceJavaEntity cgformEnhanceJavaEntity = getCgformEnhanceJavaEntityByCodeFormId(buttonCode,formId,event);

		if(cgformEnhanceJavaEntity!=null){
			String cgJavaType = cgformEnhanceJavaEntity.getCgJavaType();
			String cgJavaValue = cgformEnhanceJavaEntity.getCgJavaValue();

			if(StringUtil.isNotEmpty(cgJavaValue)){
				Object obj = null;

//				try {
					if("class".equals(cgJavaType)){
						//???????????????????????????????????????????????????????????????????????????????????????????????????
						try {
							obj = MyClassLoader.getClassByScn(cgJavaValue).newInstance();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}else if("spring".equals(cgJavaType)){
						obj = ApplicationContextUtil.getContext().getBean(cgJavaValue);
					}
					if(obj instanceof CgformEnhanceJavaInter){

						CgFormHeadEntity head = this.get(CgFormHeadEntity.class, formId);
						CgformEnhanceJavaInter javaInter = (CgformEnhanceJavaInter) obj;
						javaInter.execute(head.getTableName(),data);

					}
//				} catch (Exception e) {
//					logger.error(e.getMessage());
//					e.printStackTrace();
//					throw new BusinessException("??????JAVA?????????????????????");
//				} 

			}

		}
	}
	
	public CgformEnhanceJavaEntity getCgformEnhanceJavaEntityByCodeFormId(String buttonCode, String formId, String event) {
		StringBuilder hql = new StringBuilder("");
		List<CgformEnhanceJavaEntity> list = null;
		hql.append(" from CgformEnhanceJavaEntity t");

		hql.append(" where t.formId=?");
		hql.append(" and  t.buttonCode =?");

		hql.append(" and  t.activeStatus ='1'");

		if(oConvertUtils.isNotEmpty(event)) {
			hql.append(" and t.event = ?");
			list = this.findHql(hql.toString(),formId,buttonCode,event);
		} else {
			list = this.findHql(hql.toString(),formId,buttonCode);
		}

		if(list!=null&&list.size()>0){
			return list.get(0);
		}
		return null;
	}
}

