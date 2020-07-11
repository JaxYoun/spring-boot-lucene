package com.yang.springbootlucene.analyzer;

import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;

/**
 * @author: Yang
 * @date: 2020/7/12 00:05
 * @description:
 */
public class Analyzer {

    public static void main(String[] args) throws Exception {
        whiteSpaceAnalyzerTest();
    }

    /**
     * Lucene原生分词器：
     * 1. StandardAnalyzer：对英文和德文友好，中文会被切分为单字。
     * 2. WhitespaceAnalyzer：仅按空格切分，对中文不友好。
     * 3. SimpleAnalyzer：只保留字母，去除空格、数字、标点，不支持中文。
     * 4. CJKAnalyzer：中日韩文，对中文进行二分法切分，去掉空格、标点（二分功能很弱智）。
     * <p>
     * 第三方分词器：
     * 1. paoding：已过时，功能和兼容性都不好。
     * 2. IK：支持扩展词库、停顿词库（推荐使用）。
     * 3. mmseg4j：尚可，但不好。
     * @throws Exception
     */
    private static void whiteSpaceAnalyzerTest() throws Exception {
        //1. 创建分词器对象
//        WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
//        SimpleAnalyzer analyzer = new SimpleAnalyzer();
//        CJKAnalyzer analyzer = new CJKAnalyzer();

        IKAnalyzer analyzer = new IKAnalyzer();

        //2. 创建Directory对象（MMapDirectory基于MemoryMap技术，性能更好）
        FSDirectory directory = MMapDirectory.open(Paths.get("G:\\luceneDir"));

        //3. 创建IndexWriterConfig对象
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //4. 创建IndexWriter对象
        IndexWriter writer = new IndexWriter(directory, config);

        //5. 创建Doc
        Document document = new Document();
        document.add(new TextField("title", "中国孔子派出所 天经地义 即 China 有朋自   1234 666  777    远方来，不亦    说乎", Field.Store.YES));

        //6. 将Doc写入索引库
        writer.addDocument(document);

        //7. 释放IO
        writer.flush();
        writer.close();
        directory.close();
    }

}
