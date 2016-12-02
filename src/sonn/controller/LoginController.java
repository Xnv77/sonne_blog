package sonn.controller;

import java.io.IOException;
import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import sonn.entity.User;
import sonn.message.bean.SimpleBackMessage;
import sonn.service.UserService;
import sonn.util.CaptchaUtil;
import sonn.util.MessageUtil;
import sonn.util.Principal;
import sonn.util.RSAUtils;
import sonn.util.StringUtill;
import sun.misc.BASE64Encoder;

import com.alibaba.fastjson.JSONObject;

/**
 * 
* @ClassName: LoginController 
* @Description:Login CONTROLLER
* @author sonne
* @date 2016-4-25 下午2:52:03 2016-05-02 具体编码 2016-05-07验证码相关
*      2016-12-01 rsa
* @version 1.0
 */
@Controller
@RequestMapping("/login")
public class LoginController
{
    @Resource(name = "userServiceImpl")
    private UserService userService;
    
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public String show(HttpServletRequest request, Model model)throws Exception
    {
		HttpSession session = request.getSession();
    	// rsa key pair
    	Map<String, Object> map = RSAUtils.genKeyPair();
    	RSAPublicKey publicKey =  (RSAPublicKey) map.get("RSAPublicKey");
    	RSAPrivateKey privateKey = (RSAPrivateKey)map.get("RSAPrivateKey");
    	String strPublicKey = getKeyString(publicKey);
    	String strPrivateKey = getKeyString(privateKey);
    	// public key send to client
    	model.addAttribute("publicKey",strPublicKey);
    	// private key save in session
    	session.setAttribute("PRIVATE_KEY", strPrivateKey);
        return "loginPage";
    }
    
    /**
     * 得到密钥字符串（经过base64编码）
     * @return
     */
    private String getKeyString(Key key) throws Exception {
          byte[] keyBytes = key.getEncoded();
          String s = (new BASE64Encoder()).encode(keyBytes);
          return s;
    }
    
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    @ResponseBody
    public void captcha(HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException 
    {
    	CaptchaUtil.outputCaptcha(request, response);
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject submit(HttpServletRequest request,User user,String captcha) throws Exception
    {
		JSONObject jo = new JSONObject();	
		HttpSession session = request.getSession();
    	SimpleBackMessage loginMessage = checkUserInfor(user,captcha,session);
    	MessageUtil.setJSONObject(jo,loginMessage);
    	if(!loginMessage.isSuccess())
    	{
            return jo;
    	}
    	session.setAttribute(User.PRINCIPAL_ATTRIBUTE_NAME,
    			              new Principal(user.getId(),user.getUsername()));
        return jo;
    }
    
    private SimpleBackMessage checkUserInfor(User user,String captcha,HttpSession session)
    {
    	SimpleBackMessage backMessage = checkInput(new SimpleBackMessage(),user,captcha);
    	if(!backMessage.isSuccess())
    	{
    		return backMessage;
    	}
    	backMessage = checkCaptcha(backMessage,captcha,session);
    	if(!backMessage.isSuccess())
    	{
    		return backMessage;
    	}
    	backMessage = CheckUserNameAndPassword(backMessage,user,session);
    	if(!backMessage.isSuccess())
    	{
    		return backMessage;
    	}
    	return backMessage;
    }
    
    /*
     * check the messages inputed.
     */
    private SimpleBackMessage checkInput(SimpleBackMessage backMessage,
    		                     User user,String captcha)
    {
    	if(StringUtill.isStringEmpty(captcha))
    	{
    		MessageUtil.setSimpleBackMessage(backMessage, false, "请输入验证码!( ¯ □ ¯ )");
    		return backMessage;
    	}
    	if(null == user||StringUtill.isStringEmpty(user.getPassword())
    			||StringUtill.isStringEmpty(user.getUsername()))
    	{
    		MessageUtil.setSimpleBackMessage(backMessage, false, "输入错误!( ¯ □ ¯ )");
    		return backMessage;
    	}
    	return backMessage;
    }
    
    /*
     * check the captcha.
     */
    private SimpleBackMessage checkCaptcha(SimpleBackMessage backMessage,
    		              String captcha,HttpSession session)
    {
    	String captchaInSession = (String) session.getAttribute("randomString");
    	if(StringUtill.isStringEmpty(captchaInSession))
    	{
    		MessageUtil.setSimpleBackMessage(backMessage, false,"请输入验证码 ( ¯ □ ¯ ) ");
    		return backMessage;
    	}
//    	if(!captchaInSession.equals(captcha))
//    	{
//    		MessageUtil.setSimpleBackMessage(backMessage, false,"验证码错了 …(⊙_⊙;)…⊙﹏⊙‖∣°( ¯ □ ¯ )");
//    		return backMessage;
//    	}
    	char c_input;
    	char c_ssesion;
    	for (int i = 0; i < captchaInSession.length(); i++)
    	{
    		c_input = captcha.charAt(i);
    		c_ssesion = captchaInSession.charAt(i);
    		if(c_ssesion != c_input && c_input != Character.toLowerCase(c_ssesion))
    		{
        		MessageUtil.setSimpleBackMessage(backMessage, false,"验证码错了 …(⊙_⊙;)…⊙﹏⊙‖∣°( ¯ □ ¯ )");
        		return backMessage;
    		}
    	}
    	return backMessage;
    }
    
    /**
     * check the username.
     */
    private SimpleBackMessage CheckUserNameAndPassword(SimpleBackMessage backMessage,User user,HttpSession session)
    {
    	List<User> users = userService.findByUserName(user.getUsername());
    	if(users.isEmpty())
    	{
    		MessageUtil.setSimpleBackMessage(backMessage, false, 
    				                "用户名不存在!‘(*>﹏<*)′ （°ο°）~ @");
    		return backMessage;
    	}
    	User userFromDB = users.get(0);
    	
    	// get private key from session
    	String PRIVATE_KSY = (String) session.getAttribute("PRIVATE_KEY");
    	String passwd = RSAUtils.decryptDataOnJava(user.getPassword(), PRIVATE_KSY);
    	
    	// for compatible with the old version, here md5 or not
    	if(!userFromDB.getPassword().equals(passwd)
    			&& !userFromDB.getPassword().equals(DigestUtils.md5Hex(passwd)))
    	{
    		MessageUtil.setSimpleBackMessage(backMessage, false, 
	                "密码错误!（°ο°）~ @");
    		return backMessage;
    	}
    	MessageUtil.setSimpleBackMessage(backMessage, true, 
    			             "欢迎来到日向博客!(^_^)∠※ 送你一束花 。");
		return backMessage;
    }
}
