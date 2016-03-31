package com.pay.wechat.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pay.wechat.controller.PaymentController;

@SuppressWarnings("serial")
public class UnifiedOrderServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public UnifiedOrderServlet() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PaymentController controller = new PaymentController();
		
		response.setContentType("text/html;charset=UTF-8");
		response.getOutputStream().write(controller.unifiedOrder(request, response).getBytes("UTF-8"));
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PaymentController controller = new PaymentController();
		
		response.setContentType("text/html;charset=UTF-8");
		response.getOutputStream().write(controller.unifiedOrder(request, response).getBytes("UTF-8"));
	}
}
