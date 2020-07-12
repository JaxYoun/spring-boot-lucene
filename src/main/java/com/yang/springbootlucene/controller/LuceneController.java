package com.yang.springbootlucene.controller;

import com.yang.springbootlucene.lucene.LuceneService;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author YYX
 */
@RestController
@RequestMapping("/lucene")
public class LuceneController {

    @Resource
    private LuceneService luceneService;

    @SneakyThrows
    @GetMapping("/add")
    public void add() {
        this.luceneService.add();
    }

    @SneakyThrows
    @GetMapping("/typeAdd")
    public void typeAdd() {
        this.luceneService.typeAdd();
    }

    @SneakyThrows
    @GetMapping("/deleteAll")
    public void deleteAll() {
        this.luceneService.deleteAll();
    }

    @SneakyThrows
    @GetMapping("/delete")
    public void delete(String key) {
        this.luceneService.delete(key);
    }

    @SneakyThrows
    @GetMapping("/update")
    public void update() {
        this.luceneService.update();
    }

    @SneakyThrows
    @GetMapping("/search")
    public void search(String key) {
        this.luceneService.search(key);
    }

}
