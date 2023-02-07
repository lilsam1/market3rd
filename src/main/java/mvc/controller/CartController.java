package mvc.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import market.dao.CartDAO;
import market.dao.ProductDAO;
import market.dto.Cart;
import market.dto.Product;

@WebServlet(urlPatterns = {"/market2nd/shop_db/addCart.jsp", "/market2nd/shop_db/cart.jsp"})
public class CartController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String RequestURI = req.getRequestURI();	
		String contextPath = req.getContextPath();	
		String command = RequestURI.substring(contextPath.length());	
		
		resp.setContentType("text/html; charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		
		System.out.println("cart command : " + command);
		
		if (command.contains("addCart.jsp")) {	// ��ٱ��� ���
			// �Ķ���ͷ� �Ѿ�� ���̵� ���� Ȯ��
			String id = req.getParameter("id");
			if (id == null || id.trim().equals("")) {
				resp.sendRedirect("products.jsp");
				return;
			}
			
			HttpSession session = req.getSession();	// ���� ����� ���� ����
			String orderNo = session.getId();
			String sessionMemberId = (String) session.getAttribute("sessionMemberId");
			ProductDAO productDAO = new ProductDAO();
			CartDAO cartDAO = new CartDAO();
			
			// ���̵� ���� �������� ��ü�� ��� ��
			Product product = productDAO.getProductById(sessionMemberId);
			if (product == null) {
				resp.sendRedirect("../exception/exceptionNoProductId.jsp");
			}
			
			boolean flag = cartDAO.updateCart(product, orderNo, sessionMemberId);
			
			resp.sendRedirect("product.jsp?id=" + id);
		}
		else if (command.contains("cart.jsp")) {
			CartDAO cartDAO = new CartDAO();
			
			HttpSession session = req.getSession();
			String orderNo = session.getId();
			
			ArrayList<Cart> carts = cartDAO.getCartList(orderNo);
			req.setAttribute("carts", carts);
			System.out.println(carts);
			System.out.println(orderNo);
			RequestDispatcher rd = req.getRequestDispatcher("../shop_db/cart.jsp");
			rd.forward(req, resp);
		}
		
	}
	

}
