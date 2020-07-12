package com.yang.springbootlucene.lucene;

import com.yang.springbootlucene.dao.ItemDao;
import com.yang.springbootlucene.po.Item;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author YYX
 */
@Service
public class LuceneService {

    @Resource
    private ItemDao itemDao;

    @Value("${lucene.index.dir}")
    private String luceneIndexDir;

    /**
     * 将MySQL中的数据，批量写入索引库
     *
     * @throws Exception
     */
    public void add() throws Exception {
        //1. 从DB获取Record列表
        List<Item> itemList = this.itemDao.listAll();

        //2. 将Record列表转换为Document列表
        List<Document> docList = new ArrayList<>(itemList.size());
        itemList.parallelStream().forEach(it -> {
            Document doc = new Document();
            //分字段处理
            doc.add(new TextField("id", it.getId(), Field.Store.YES));
            doc.add(new TextField("name", it.getName(), Field.Store.YES));
            doc.add(new TextField("price", String.valueOf(it.getPrice()), Field.Store.YES));
            doc.add(new TextField("num", String.valueOf(it.getNum()), Field.Store.YES));
            doc.add(new TextField("image", it.getImage(), Field.Store.YES));
            doc.add(new TextField("categoryName", it.getCategoryName(), Field.Store.YES));
            doc.add(new TextField("brandName", it.getBrandName(), Field.Store.YES));
            doc.add(new TextField("spec", it.getSpec(), Field.Store.YES));
            doc.add(new TextField("saleNum", String.valueOf(it.getSaleNum()), Field.Store.YES));
            docList.add(doc);
        });

        //3. 创建分词器（标准分词器），对英文、德文友好，中文会被分为单字
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //4. 创建Directory目录对象，用于存储索引库
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

        //5. 创建indexWriterConfig，并在其中指定分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //6. 创建indexWriter，并为其指定存储目录
        IndexWriter indexWriter = new IndexWriter(directory, config);

        //7. 写入文档到索引库
        docList.parallelStream().forEach(it -> {
            try {
                indexWriter.addDocument(it);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //8. 释放IO对象
        indexWriter.flush();
        indexWriter.close();
        directory.close();
    }

    /**
     * 严格按照哦字段类型，构建索引
     *
     * @throws Exception
     */
    public void typeAdd() throws Exception {
        //1. 从DB获取Record列表
        List<Item> itemList = this.itemDao.listAll();

        //2. 将Record列表转换为Document列表
        List<Document> documentList = itemList.parallelStream().map(it -> {
            Document doc = new Document();

            /**
             * 是否分词：否
             * 是否索引：是
             * 是否存储：是
             */
            doc.add(new StringField("id", it.getId(), Field.Store.YES));

            /**
             * 是否分词：是
             * 是否索引：是
             * 是否存储：是
             */
            doc.add(new TextField("name", it.getName(), Field.Store.YES));

            /**
             * 是否分词：是，要分词（根据Lucene底层算法决定）
             * 是否索引：是
             * 是否存储：是
             */
            doc.add(new IntPoint("price", it.getPrice()));
            doc.add(new StoredField("price", it.getPrice()));

            /**
             * 是否分词：否
             * 是否索引：否
             * 是否存储：是
             */
            doc.add(new TextField("num", String.valueOf(it.getNum()), Field.Store.NO));

            /**
             * 是否分词：否
             * 是否索引：否
             * 是否存储：是
             */
            doc.add(new StoredField("image", it.getImage()));

            /**
             * 是否分词：否
             * 是否索引：是
             * 是否存储：是
             */
            doc.add(new StringField("categoryName", it.getCategoryName(), Field.Store.YES));

            /**
             * 是否分词：否
             * 是否索引：是
             * 是否存储：是
             */
            doc.add(new StringField("brandName", it.getBrandName(), Field.Store.YES));

            return doc;
        }).collect(Collectors.toList());

        //3. 创建分词器（标准分词器），对英文、德文友好，中文会被分为单字
//        Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //4. 创建Directory目录对象，用于存储索引库
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

        //5. 创建indexWriterConfig，并在其中指定分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //6. 创建indexWriter，并为其指定存储目录
        IndexWriter indexWriter = new IndexWriter(directory, config);

        //7. 写入文档到索引库
        documentList.parallelStream().forEach(it -> {
            try {
                indexWriter.addDocument(it);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        //8. 释放IO对象
        indexWriter.flush();
        indexWriter.close();
        directory.close();
    }

    public void search(String key) throws Exception {
        //1. 创建分词器（必须与构建索引库所用的分词器相同）
        Analyzer analyzer = new StandardAnalyzer();

        //2. 创建查询对象
        // 参数1：默认查询字段（优先使用用户指定的字段名，若查询的关键字中未指定了字段，则使用默认字段）
        // 参数2：分词器
        QueryParser parser = new QueryParser("id", analyzer);

        //3. 设置搜索关键字
        Query query = parser.parse(key);

        //4. 创建Directory目录，指定索引库存储位置
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

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
    }

    /**
     * 底层操作是先删除符合条件的Doc，然后再追加传入的Doc
     *
     * @throws IOException
     */
    public void update() throws IOException {
        Document doc = new Document();
        doc.add(new TextField("id", "it.getId()", Field.Store.YES));
        doc.add(new TextField("name", "it.getName()", Field.Store.YES));
        doc.add(new TextField("price", "String.valueOf(it.getPrice())", Field.Store.YES));
        doc.add(new TextField("num", "String.valueOf(it.getNum())", Field.Store.YES));
        doc.add(new TextField("image", "it.getImage()", Field.Store.YES));
        doc.add(new TextField("categoryName", "it.getCategoryName()", Field.Store.YES));
        doc.add(new TextField("brandName", "it.getBrandName()", Field.Store.YES));
        doc.add(new TextField("spec", "it.getSpec()", Field.Store.YES));
        doc.add(new TextField("saleNum", "String.valueOf(it.getSaleNum())", Field.Store.YES));

        //3. 创建分词器（标准分词器），对英文、德文友好，中文会被分为单字
        Analyzer analyzer = new StandardAnalyzer();

        //4. 创建Directory目录对象，用于存储索引库
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

        //5. 创建indexWriterConfig，并在其中指定分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //6. 创建indexWriter，并为其指定存储目录
        IndexWriter indexWriter = new IndexWriter(directory, config);

        indexWriter.updateDocument(new Term("id", "100000003145"), doc);

        indexWriter.flush();
        indexWriter.close();

    }

    /**
     * 条件删除
     */
    public void delete(String key) throws IOException {
        //3. 创建分词器（标准分词器），对英文、德文友好，中文会被分为单字
        Analyzer analyzer = new StandardAnalyzer();

        //4. 创建Directory目录对象，用于存储索引库
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

        //5. 创建indexWriterConfig，并在其中指定分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //6. 创建indexWriter，并为其指定存储目录
        IndexWriter indexWriter = new IndexWriter(directory, config);

        indexWriter.deleteDocuments(new Term("id", key));

        indexWriter.flush();
        indexWriter.close();
    }

    /**
     * 删除索引库目录中的所有索引，清空索引库
     */
    public void deleteAll() throws IOException {
        //3. 创建分词器（标准分词器），对英文、德文友好，中文会被分为单字
        Analyzer analyzer = new StandardAnalyzer();

        //4. 创建Directory目录对象，用于存储索引库
        Directory directory = FSDirectory.open(Paths.get(this.luceneIndexDir));

        //5. 创建indexWriterConfig，并在其中指定分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //6. 创建indexWriter，并为其指定存储目录
        IndexWriter indexWriter = new IndexWriter(directory, config);

        indexWriter.deleteAll();

        indexWriter.flush();
        indexWriter.close();
    }
}
