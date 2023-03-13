package com.yunze.LibraryManagementSystem.modules.evaluate.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunze.LibraryManagementSystem.modules.evaluate.entity.Collection;
import com.yunze.LibraryManagementSystem.modules.evaluate.entity.Evaluate;
import com.yunze.LibraryManagementSystem.modules.evaluate.service.CollectionService;
import com.yunze.LibraryManagementSystem.modules.evaluate.service.EvaluateService;
import com.yunze.LibraryManagementSystem.modules.evaluate.service.impl.CollectionServiceImpl;
import com.yunze.LibraryManagementSystem.modules.evaluate.service.impl.EvaluateServiceImpl;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 收藏书评
 */
@WebServlet(name = "collectEvaluateController", value = "/safe/collectEvaluateController")
public class CollectEvaluateController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 读取前端发送的 JSON 数据
        BufferedReader reader = request.getReader();
        StringBuilder json = new StringBuilder();//拼接字符串
        String line = null;
        while ((line = reader.readLine()) != null) {//逐行读取请求体的数据
            json.append(line);
        }
        reader.close();
        // 将 JSON 数据转换为 Java 对象
        ObjectMapper mapper = new ObjectMapper();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(dateFormat);
        Map<String, Object> jsonMap = mapper.readValue(json.toString(), Map.class);
        //获取int型数据
        int evaluateId = (int) jsonMap.get("evaluateId");
        HttpSession session = request.getSession();
        int readerId = (int)session.getAttribute("reader_id");
        //处理数据
        CollectionService collectionService = new CollectionServiceImpl();
        Collection collection = new Collection(evaluateId,readerId, new Date());
        int result1 = collectionService.collect(collection);
        //书评的收藏量+1
        EvaluateService evaluateService = new EvaluateServiceImpl();
        Evaluate evaluate = evaluateService.search(evaluateId);
        Map<String, Object> responseMap = new HashMap<>();
        if(evaluate == null){
            responseMap.put("status", "failure");
            responseMap.put("code", 1000);
            responseMap.put("massage", "错误参数，书评不存在");
        }else {
            evaluate.setCollection(evaluate.getCollection() + 1);
            int result2 = evaluateService.updateEvaluate(evaluate);

            if (result1 == 0 || result2 == 0) {
                responseMap.put("status", "failure");
                responseMap.put("code", 4001);
                responseMap.put("massage", "收藏失败");
            } else {
                responseMap.put("status", "success");
                responseMap.put("code", 2000);
                responseMap.put("massage", "收藏成功");
            }
        }
        response.getWriter().write(mapper.writeValueAsString(responseMap));
    }
}
