package com.adbest.smsmarketingfront.controller;

import com.adbest.smsmarketingfront.entity.form.KeywordForm;
import com.adbest.smsmarketingfront.entity.vo.KeywordVo;
import com.adbest.smsmarketingfront.entity.vo.PageDataVo;
import com.adbest.smsmarketingfront.service.KeywordService;
import com.adbest.smsmarketingfront.util.PageBase;
import com.adbest.smsmarketingfront.util.ReturnEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keyword")
public class KeywordController {

    @Autowired
    private KeywordService keywordService;

    @RequestMapping("/view-list")
    public ReturnEntity list(PageBase page){
        PageDataVo vo = keywordService.findAll(page);
        return ReturnEntity.success(vo);
    }

    @RequestMapping("/add")
    public ReturnEntity add(KeywordForm keywordForm){
        KeywordVo save = keywordService.save(keywordForm);
        return ReturnEntity.success(save);
    }

    @RequestMapping("/check")
    public ReturnEntity check(String title){
        Boolean is = keywordService.check(title);
        return ReturnEntity.success(is);
    }

}
