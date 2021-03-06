package com.wang.search.web.solr.service.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.wang.core.ServiceResult;
import com.wang.search.web.solr.bean.SolrTestBean;
import com.wang.search.web.solr.model.SolrTestModel;
import com.wang.search.web.solr.service.SolrTestService;
import com.wang.search.web.utils.SystemConfigureUtil;

/**
 * solr 搜索  service imp
 * 
 * @author HeJiawang
 * @date   2016.11.08
 */
@Service
public class SolrTestServiceImp implements SolrTestService {

	/**
	 * log
	 */
	private final Logger logger = LoggerFactory.getLogger(SolrTestServiceImp.class);
	
	/**
	 * solrTestModel
	 */
    @Autowired
    private SolrTestModel solrTestModel;
	
	/**
	 * solrQuery
	 */
    @Autowired
    private SolrQuery solrQuery;
    
    /**
     * systemConfigureUtil
     */
    @Autowired
    private SystemConfigureUtil systemConfigureUtil;
	
	/**
	 * solr 搜索
	 * 
	 * @param keyWord 搜索关键字
	 * @param pInteger 分页——起始条数
	 * @param condition 其他搜索条件
	 * 
	 * @return ServiceResult
	 * @author HeJiawang
	 * @date   2016.11.08
	 */
	@Override
	public ServiceResult<List<SolrTestBean>> searchTest(String keyWord, Integer pInteger, Map<String, String> condition) {
		Assert.notNull(solrQuery, "Property 'solrQuery' is required.");
		
		ServiceResult<List<SolrTestBean>> result = new ServiceResult<>();
		try {
			
			/**
			 * 设置查询信息
			 */
	        solrQuery.setQuery(this.getQueryFields(keyWord));	//设置基本查询
	        
	        /**
	         * 设置查询条件
	         */
	        solrQuery.setFilterQueries(this.getFielder(condition));
	        
	        /**
	         * 分页
	         */
	        solrQuery.setStart(pInteger);
	        solrQuery.setRows(systemConfigureUtil.getSolrRow());
			
	        /**
	         * 分片检索(分类检索)——
	         */
	        solrQuery.setFacet(systemConfigureUtil.isFacet());
	        solrQuery.add("facet.field", systemConfigureUtil.getFacetStr());	//可以按多个字段分类，用逗号分隔

	        /**
	         * 设置过滤结果——能查询出那些字段
	         */
	        solrQuery.add("fl", systemConfigureUtil.getFilterFields());
	        
	        /**
	         * 排序,注意是有顺序的
	         */
	        solrQuery.setSort(this.getSort(0));	//setSort()会覆盖之前的查询条件
	        solrQuery.addSort("sort", SolrQuery.ORDER.desc);	//addSort()不会覆盖之前的查询条件
	        
	        result  = solrTestModel.searchTest(solrQuery);
		} catch ( Exception e ) {
			logger.error("异常发生在"+this.getClass().getName()+"类的searchTest方法，异常原因是："+e.getMessage(), e.fillInStackTrace());
		}
        
		return result;
	}
	
	/**
	 * 设置查询字段
	 * @param keyword 查询字段
	 * @return 符合格式的查询字段
	 * @author HeJiawang
	 * @date   2016.11.08
	 */
	private String getQueryFields(String keyword) {
    	String[] fields = systemConfigureUtil.searchFields();
        StringBuilder stringBuilder = new StringBuilder();
        for (String strField : fields) {
            stringBuilder.append(strField).append(":").append(keyword).append(" OR ");
        }
        int index = stringBuilder.lastIndexOf("OR");
        return stringBuilder.substring(0, index);
    }
	
	/**
	 * 解析搜索条件
	 * @param condition 搜索条件
	 * @return
	 */
    private String[] getFielder(Map<String, String> condition) {
        ArrayList<String> result = new ArrayList<String>();
        Map<String, String> fieldMap = systemConfigureUtil.getCondition();
        for (String param : fieldMap.keySet()) {
            if (condition.get(param) != null) {
                result.add(fieldMap.get(param) + ":" + condition.get(param));
            }
        }
        return result.toArray(new String[result.size()]);
    }
	
	 /**
     * 设置排序
     * @param sort 第几个排序
     */
    private SolrQuery.SortClause getSort(int sort) {

        Map<Integer, String> ss = systemConfigureUtil.getSort();
        String field = ss.get(sort);

        String[] fieldAndOrder = StringUtils.split(field, " ");
        SolrQuery.SortClause sortClause = new SolrQuery.SortClause(fieldAndOrder[0], fieldAndOrder[1]);
        return sortClause;
    }

}
