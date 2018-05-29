package ren.jung.mail

import java.nio.file.AccessDeniedException
import java.util

import com.google.common.collect.Maps
import com.google.gson.Gson
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.commons.lang.ArrayUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.web.client.RestClientException
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.{HttpMediaTypeNotSupportedException, HttpRequestMethodNotSupportedException}
import ren.jung.util.{MailUtil, SpringUtils}


/**
  * 基础的Mvc配置类.
  * Created by Jung on 06/06/2017.
  */
object MailConfiguration {
  def log = org.slf4j.LoggerFactory.getLogger(classOf[MailConfiguration])

  def DISPLAY_ERROR = new util.HashMap[String, Any]()

  def gson = new Gson

  def X_FORWARDED_FOR = "x-forwarded-for"

  def Proxy_Client_IP = "Proxy-Client-IP"

  def WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP"

  def HTTP_CLIENT_IP = "HTTP_CLIENT_IP"

  def HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR"

  DISPLAY_ERROR.put("code", 500)
  DISPLAY_ERROR.put("message", "服务器开了个小差")


}

abstract class MailConfiguration(var env: Environment) extends WebMvcConfigurer with EnvironmentAware {


  override def configureHandlerExceptionResolvers(exceptionResolvers: util.List[HandlerExceptionResolver]): Unit = {
    exceptionResolvers.add((request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception) => {
      def foo(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception) = {

        if (!(ex.isInstanceOf[AccessDeniedException] || ex.isInstanceOf[HttpRequestMethodNotSupportedException]
          || ex.isInstanceOf[HttpMediaTypeNotSupportedException] || ex.isInstanceOf[RestClientException]
          || ex.isInstanceOf[IllegalStateException])) {

          if (MailConfiguration.log.isInfoEnabled) {
            MailConfiguration.log.info("[resolveException([request, response, handler, ex])] Illegal call from ip -> {}, headers -> {}, url -> {},query -> {}", getClientIp(request), MailConfiguration.gson.toJson(getHeaderMap(request)), request.getRequestURI, request.getQueryString)
          }
          val finalEx = ex
          new Thread(() => {
            def foo(): Unit = {
              try {
                val title = "新元认证系统异常提醒"
                val mailUtil = SpringUtils.getBean(classOf[MailUtil])
                //发送邮件
                mailUtil.sendMail2User("lizhiguo@jingcaiwang.cn", title, "新元认证系统遇到异常，请及时处理：\n" + ExceptionUtils.getFullStackTrace(finalEx))
                mailUtil.sendMail2User("zhangwei@jingcaiwang.cn", title, "新元认证系统遇到异常，请及时处理：\n" + ExceptionUtils.getFullStackTrace(finalEx))
                mailUtil.sendMail2User("jialichao@jingcaiwang.cn", title, "新元认证系统遇到异常，请及时处理：\n" + ExceptionUtils.getFullStackTrace(finalEx))
              } catch {
                case e: Exception =>
                  MailConfiguration.log.error("发送邮件过程中产生了异常")
              }
            }

            foo()
          }).start()
        }

        null
      }

      foo(request, response, handler, ex)
    })
  }

  def getClientIp(request: HttpServletRequest): String = first(Array(
    request.getHeader(MailConfiguration.X_FORWARDED_FOR),
    request.getHeader(MailConfiguration.Proxy_Client_IP),
    request.getHeader(MailConfiguration.WL_PROXY_CLIENT_IP),
    request.getHeader(MailConfiguration.HTTP_CLIENT_IP),
    request.getHeader(MailConfiguration.HTTP_X_FORWARDED_FOR),
    request.getRemoteAddr))


  def first(origin: Array[Object]): String = {
    if (ArrayUtils.isEmpty(origin)) null
    else {
      for (item <- origin) {
        if (item != null) return item.toString
      }

      null
    }
  }

  def getHeaderMap(request: HttpServletRequest): util.Map[String, String] = {
    val headers = Maps.newHashMap[String, String]
    val names = request.getHeaderNames
    while ( {
      names.hasMoreElements
    }) {
      val header = names.nextElement
      headers.put(header, request.getHeader(header))
    }
    headers
  }

}
