package com.dcs.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dcs.gmall.bean.SkuLsInfo;
import com.dcs.gmall.bean.SkuLsParams;
import com.dcs.gmall.bean.SkuLsResult;
import com.dcs.gmall.service.ListService;
import com.dcs.gmall.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void saveSkuLsInfoES(SkuLsInfo skuLsInfo) {

        Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuInfo").id(skuLsInfo.getId()).build();
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult searchSkusES(SkuLsParams skuLsParams) {
        //生成查询DSL
        String searchDSL = generateSearchDSL(skuLsParams);

        Search build = new Search.Builder(searchDSL).addIndex("gmall").addType("SkuInfo").build();

        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //处理查询结果，返回SkuLsResult
        SkuLsResult skuLsResult = generateSkuLsResult(skuLsParams, searchResult);

        return skuLsResult;
    }

    @Override
    public void incryHotScore(String skuId) {

        try {
            Jedis jedis = redisUtil.getJedis();

            String hotScore = "hotScore";
            //注意score是增加的步长
            Double count = jedis.zincrby(hotScore, 1, "skuId:" + skuId);

            if(count % 10 == 0){
                //修改es中gmall->SkuInfo->hotscore字段的值
                updateSkuLsInfoES(count, skuId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 修改es中gmall->SkuInfo->hotscore字段的值
     * @param count
     * @param skuId
     * @throws IOException
     */
    private void updateSkuLsInfoES(Double count, String skuId) throws IOException {

        String updateDsl = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+ count +"\n" +
                "  }\n" +
                "}";
        Update update = new Update.Builder(updateDsl).index("gmall").type("SkuInfo").id(skuId).build();

        jestClient.execute(update);

    }

    /**
     * 将返回结果封装到vo中
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult generateSkuLsResult(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();

        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);

        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;

            Map<String, List<String>> highlight = hit.highlight;
            if (highlight != null && highlight.size() > 0){
                List<String> skuNames = highlight.get("skuName");
                String skuName = skuNames.get(0);
                skuLsInfo.setSkuName(skuName);
            }

            skuLsInfoList.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoList);

        Long total = searchResult.getTotal();
        skuLsResult.setTotal(searchResult.getTotal());

        int pageSize = skuLsParams.getPageSize();
        long totalPages = total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
        skuLsResult.setTotalPages(totalPages);

        List<String> attrValueIdList = new ArrayList<>();
        List<TermsAggregation.Entry> buckets = searchResult.getAggregations().getTermsAggregation("grouby_attrValue").getBuckets();
        if(buckets != null){
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
        }

        skuLsResult.setAttrValueIdList(attrValueIdList);

        return skuLsResult;
    }

    /**
     * 生成在es中检索的dsl语句
     * @param skuLsParams
     * @return
     */
    private String generateSearchDSL(SkuLsParams skuLsParams) {

        SearchSourceBuilder search = new SearchSourceBuilder();
        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        String catalog3Id = skuLsParams.getCatalog3Id();
        if (catalog3Id != null && catalog3Id.length() > 0) {
            TermQueryBuilder term = new TermQueryBuilder("catalog3Id", catalog3Id);
            bool.filter(term);
        }

        String[] valueIds = skuLsParams.getValueId();
        if(valueIds != null && valueIds.length > 0 ){
            for (String valueId : valueIds) {
                TermQueryBuilder term = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                bool.filter(term);
            }
        }

        String keyword = skuLsParams.getKeyword();
        if (keyword != null && keyword.length() > 0){
            MatchQueryBuilder match = new MatchQueryBuilder("skuName", keyword);
            bool.must(match);
        }

        search.query(bool);

        HighlightBuilder highlight = new HighlightBuilder();
        highlight.preTags("<span style='color:red'>");
        highlight.postTags("</span>");
        highlight.field("skuName");

        search.highlight(highlight);

        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        search.from(from);
        search.size(skuLsParams.getPageSize());

        search.sort("hotScore", SortOrder.DESC);

        TermsBuilder terms = AggregationBuilders.terms("grouby_attrValue").field("skuAttrValueList.valueId");
        search.aggregation(terms);

        String queryDSL = search.toString();

        return queryDSL;
    }
}
