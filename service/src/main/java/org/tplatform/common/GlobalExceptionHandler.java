package org.tplatform.common;

import com.google.common.base.Throwables;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.tplatform.util.DateUtil;
import org.tplatform.util.Logger;
import org.tplatform.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 全局异常处理
 * Created by tianyi on 2017/2/9.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private ModelAndView handleError(HttpServletRequest req, HttpServletResponse rsp, Exception e, String viewName, HttpStatus status) throws Exception {
    if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
      throw e;
    String errorMsg = e.getMessage();
    String errorStack = Throwables.getStackTraceAsString(e);

    Logger.e("Request: {" + req.getRequestURI() + "} raised {" + errorStack + "}");
    String requestType = req.getHeader("X-Requested-With");
    if (!StringUtil.isEmpty(requestType) && requestType.equalsIgnoreCase("XMLHttpRequest")) {
      return handleAjaxError(rsp, errorMsg, status);
    } else {
      return handleViewError(req.getRequestURL().toString(), errorStack, errorMsg, viewName);
    }
  }

  private ModelAndView handleViewError(String url, String errorStack, String errorMessage, String viewName) {
    ModelAndView mav = new ModelAndView();
    mav.addObject("exception", errorStack);
    mav.addObject("url", url);
    mav.addObject("message", errorMessage);
    mav.addObject("timestamp", DateUtil.getCurrentDate());
    mav.setViewName(viewName);
    return mav;
  }

  private ModelAndView handleAjaxError(HttpServletResponse rsp, String errorMessage, HttpStatus status) throws IOException {
    rsp.setCharacterEncoding("UTF-8");
    rsp.setStatus(status.value());
    PrintWriter writer = rsp.getWriter();
    writer.write(errorMessage);
    writer.flush();
    return null;
  }

  //404的异常就会被这个方法捕获
  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ModelAndView handle404Error(HttpServletRequest req, HttpServletResponse rsp, Exception e) throws Exception {
    return handleError(req, rsp, e, "404.jsp", HttpStatus.NOT_FOUND);
  }

  //500的异常会被这个方法捕获
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ModelAndView handleError(HttpServletRequest req, HttpServletResponse rsp, Exception e) throws Exception {
    return handleError(req, rsp, e, "500.jsp", HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
