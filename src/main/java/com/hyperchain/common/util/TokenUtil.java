package com.hyperchain.common.util;

import cn.hyperchain.common.log.LogUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.hyperchain.dal.entity.UserEntity;
import com.hyperchain.dal.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldy on 2017/4/9.
 */
@Component
public class TokenUtil {

    @Autowired
    private static UserEntityRepository userEntityRepository;

    public static String generateToken( String address, int roleCode) {
        Map<String, Object> tokenMap = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        tokenMap.put("timestamp", timestamp);
        tokenMap.put("address", address);
        tokenMap.put("roleCode", roleCode);
        String json = JSON.toJSONString(tokenMap);
        return DesUtils.encryptToken(json);
    }

    /**
     * 从cookie中的token获取address
     * @param httpServletRequest
     * @return
     */
    public static String getAddressFromCookie(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    String tokenInfo = DesUtils.decryptToken(token);

                    //token为空
                    if (CommonUtil.isEmpty(token)) {
                        LogUtil.info("token为空");
                        return null;
                    }

                    try {
                        JSONObject jsonObject = JSONObject.parseObject(tokenInfo);
                        String address = jsonObject.getString("address"); //用户地址
                        return address;
                    } catch (JSONException e) {
                        LogUtil.info("token json解析失败");
                        return null;
                    }

                }
            }
        }
        //cookie中无token
        return null;
    }

}
