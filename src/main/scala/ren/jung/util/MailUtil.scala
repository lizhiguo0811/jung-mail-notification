package ren.jung.util

import java.util.Properties

import javax.mail._
import javax.mail.internet.{AddressException, InternetAddress, MimeMessage}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async

object MailUtil {
  def log: Logger = LoggerFactory.getLogger(classOf[MailUtil])

  def USER_NAME = "no-reply@jingcaiwang.cn"

  def PASSWORD = "38WCvGw2rDenA"

  def form = new InternetAddress(USER_NAME)

  val props = new Properties()

  /*
   * 可用的属性： mail.store.protocol / mail.transport.protocol / mail.host /
   * mail.user / mail.from
   */
  // 表示SMTP发送邮件，需要进行身份验证
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.host", "smtp.ym.163.com")
  // 发件人的账号
  props.put("mail.user", USER_NAME)
  // 访问SMTP服务时需要提供的密码
  props.put("mail.password", PASSWORD)

  var mailSession: Session = _

  def apply(): MailUtil = {

    // 构建授权信息，用于进行SMTP进行身份验证
    mailSession = Session.getInstance(props, new Authenticator() {
      override protected def getPasswordAuthentication = new PasswordAuthentication(USER_NAME, PASSWORD)
    })

    new MailUtil()
  }
}

class MailUtil {

  @Value("${spring.profiles.active}")
  private val profile = null

  /**
    * 发送邮件的方法
    *
    * @param userMail 用户的邮箱地址
    * @param title    发送的标题
    * @param content  发送的内容
    * @author Jung
    * @since 2018年05月23日10:28:10
    */
  @Async def sendMail2User(userMail: String, title: String, content: String): Unit = {
    if ("devel".equalsIgnoreCase(profile)) return
    // 创建邮件消息
    val message = new MimeMessage(MailUtil.mailSession)
    // 设置发件人
    try {
      message.setFrom(MailUtil.form)
      // 设置收件人
      val to = new InternetAddress(userMail)
      message.setRecipient(Message.RecipientType.TO, to)
      // 设置邮件标题
      message.setSubject(title)
      // 设置邮件的内容体
      message.setContent(content, "text/html;charset=UTF-8")
      // 发送邮件
      Transport.send(message)

      MailUtil.log.info("邮件发送成功，发件人：{}", userMail)
    } catch {
      case e: MessagingException =>
        MailUtil.log.error("发送失败", e)
    }
  }

}
