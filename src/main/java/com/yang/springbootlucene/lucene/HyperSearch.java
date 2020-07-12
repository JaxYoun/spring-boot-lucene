package com.yang.springbootlucene.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author: Yang
 * @date: 2020/7/12 10:32
 * @description:
 */
@Service
public class HyperSearch {

    public static void main(String[] args) throws Exception {
        //1. 逻辑连接词OR和AND大写，且和其他词之间用空格分隔
//        multiCriteria("小米 AND 手机");
//        multiCriteria("小米 OR 华为");

        rangeSearch("华为");
    }

    public static void multiCriteria(String key) throws Exception {
        //1. 创建分词器（必须与构建索引库所用的分词器相同）
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //2. 创建查询对象
        // 参数1：默认查询字段（优先使用用户指定的字段名，若查询的关键字中未指定了字段，则使用默认字段）
        // 参数2：分词器
        QueryParser parser = new QueryParser("name", analyzer);

        //3. 设置搜索关键字
        Query query = parser.parse(key);

        //4. 创建Directory目录，指定索引库存储位置
        Directory directory = FSDirectory.open(Paths.get("G:\\luceneDir"));

        //5. 创建输入流对象
        DirectoryReader directoryReader = DirectoryReader.open(directory);

        //6. 创建搜索对象
        IndexSearcher searcher = new IndexSearcher(directoryReader);

        //7. 执行搜索，并返回结果，TopN
        TopDocs search = searcher.search(query, 10);
        System.err.println("total: " + search.totalHits);

        //8. 获取结果集
        ScoreDoc[] docArr = search.scoreDocs;
        System.err.println("size: " + docArr.length);

        //9. 结果集遍历
        if (docArr != null) {
            Stream.of(docArr).forEach(it -> {
                //获取结果的文档id，这是构建索引库时Lucene自动为文档分配的
                int docId = it.doc;
                //通过docId，读取文档
                try {
                    Document doc = searcher.doc(docId);
                    System.err.println("-----------------------------------");
                    System.out.println("id : " + doc.get("id"));
                    System.out.println("price : " + doc.get("price"));
                    System.out.println("image : " + doc.get("image"));
                    System.out.println("name : " + doc.get("name"));
                    System.out.println("spec : " + doc.get("spec"));
                    System.out.println("brandName : " + doc.get("brandName"));
                    System.out.println("categoryName : " + doc.get("categoryName"));
                    System.out.println("num : " + doc.get("num"));
                    System.out.println("saleNum : " + doc.get("saleNum"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        //10. 释放IO对象
        directoryReader.close();
        directory.close();
    }

    public static void rangeSearch(String key) throws Exception {
        //1. 创建分词器（必须与构建索引库所用的分词器相同）
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //2. 创建查询对象
        // 参数1：默认查询字段（优先使用用户指定的字段名，若查询的关键字中未指定了字段，则使用默认字段）
        // 参数2：分词器
        QueryParser parser = new QueryParser("name", analyzer);
        //3. 设置搜索关键字，如果区间是数字，则必须对使用数字型Query对象
        Query query = parser.parse(key);

        //构建范围查询对象
        Query rangeQuery = IntPoint.newRangeQuery("price", 100, 1000);

        //组合多个查询对象，并指定他们间的逻辑关系，【且或非】
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(rangeQuery, BooleanClause.Occur.MUST);
        builder.add(query, BooleanClause.Occur.MUST);

        //4. 创建Directory目录，指定索引库存储位置
        Directory directory = FSDirectory.open(Paths.get("G:\\luceneDir"));

        //5. 创建输入流对象
        DirectoryReader directoryReader = DirectoryReader.open(directory);

        //6. 创建搜索对象
        IndexSearcher searcher = new IndexSearcher(directoryReader);

        //7. 执行搜索，并返回结果，TopN
        TopDocs search = searcher.search(builder.build(), 10);
        System.err.println("total: " + search.totalHits);

        //8. 获取结果集
        ScoreDoc[] docArr = search.scoreDocs;
        System.err.println("size: " + docArr.length);

        //9. 结果集遍历
        if (docArr != null) {
            Stream.of(docArr).forEach(it -> {
                //获取结果的文档id，这是构建索引库时Lucene自动为文档分配的
                int docId = it.doc;
                //通过docId，读取文档
                try {
                    Document doc = searcher.doc(docId);
                    System.err.println("-----------------------------------");
                    System.out.println("id : " + doc.get("id"));
                    System.out.println("price : " + doc.get("price"));
                    System.out.println("image : " + doc.get("image"));
                    System.out.println("name : " + doc.get("name"));
                    System.out.println("spec : " + doc.get("spec"));
                    System.out.println("brandName : " + doc.get("brandName"));
                    System.out.println("categoryName : " + doc.get("categoryName"));
                    System.out.println("num : " + doc.get("num"));
                    System.out.println("saleNum : " + doc.get("saleNum"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        //10. 释放IO对象
        directoryReader.close();
        directory.close();
    }

}
