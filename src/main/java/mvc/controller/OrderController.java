package mvc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import market.dao.CartDAO;
import market.dto.Cart;
import mvc.model.OrderDAO;
import mvc.model.OrderDataDTO;
import mvc.model.OrderInfoDTO;
import mvc.service.OrderStep;

@WebServlet("/order/*")
public class OrderController extends HttpServlet {

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
		req.setCharacterEncoding("utf-8");
		
		System.out.println("command : " + command);
		
		if (command.contains("form.do")) { // �ֹ��� / ������� �Է� ������
			setOrderData(req);
			// ��ܿ� ��ٱ��� ���
			
			// ��ܿ� ����� ��ٱ��� 
			ArrayList<OrderDataDTO> datas = getOrderData(getOrderNo(req));
			req.setAttribute("datas", datas);
			
			// ��ٱ��� �հ� �ݾ�
			int totalPrice = getTotalPrice(getOrderNo(req));
			req.setAttribute("totalPrice", totalPrice);
			
			req.getRequestDispatcher("/WEB-INF/order/form.jsp").forward(req, resp);
		}
		
		else if (command.contains("pay.do")) {	// �ֹ��� ���� ���� �� ���� ���� ���
			setOrderInfo(req);	// �ֹ����� ����
			
			// ��ٱ��� �հ� �ݾ�
			int totalPrice = getTotalPrice(getOrderNo(req));
			req.setAttribute("totalPrice", totalPrice);
			
			// �ֹ��� ���� ������
			OrderInfoDTO info = getOrderInfo(getOrderNo(req));
			req.setAttribute("info", info);
			
			// �ֹ���ǰ ���� �������� (ex: iPhone 6S �� 1��)
			String orderProductName = getOrderProductName(getOrderNo(req));
			req.setAttribute("orderProductName", orderProductName);
			
			req.getRequestDispatcher("/WEB-INF/order/pay.jsp").forward(req, resp);
			
		}
		
		else if (command.contains("success.do")) {	// ���� ������ ���������� �� ���
			// ���� ��ħ�ÿ� ���� ���� API�� ����� ��û�� �ؼ� ���� �޼����� ���� �� ������ ó���� �Ŀ��� sendRedirect
			try {
				processSuccess(req);	// ó��
				resp.sendRedirect("/order/orderDone.do");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		else if (command.contains("orderDone.do")) {	// ���� �Ϸ�
			// order_data�� �ִ� cartId �������� ��ٱ��Ͽ� �ִ� ��ǰ�� ����
			deleteCartWhenOrderDone(getOrderNo(req));
			
			// ��ܿ� ����� ��ٱ��� ���
			ArrayList<OrderDataDTO> datas = getOrderData(getOrderNo(req));
			req.setAttribute("datas", datas);
			
			// �ֹ� ���� ������
			OrderInfoDTO info = getOrderInfo(getOrderNo(req));
			// �ֹ��ܰ踦 �ѱ۷�
			OrderStep orderStep = OrderStep.valueOf(info.getOrderStep());
			info.setOrderStep(orderStep.getStep());
			req.setAttribute("info", info);
			
			req.getRequestDispatcher("/WEB-INF/order/orderDone.jsp").forward(req, resp);
		}
	}

	private String getOrderNo(HttpServletRequest req) {
		// TODO Auto-generated method stub
		/* �ֹ� ��ȣ ��ȯ
		 1. �ֹ���ȣ ��� ������ �ڵ� �ݺ��� �Ǿ
		 2. �ֹ���ȣ ü�谡 ���� ��츦 ����� �޼���ȭ
		 */
		
		HttpSession session = req.getSession();	// ���� ����� ���� ����
		return session.getId();
	}
	
	private ArrayList<OrderDataDTO> getOrderData(String orderNo) {
		OrderDAO dao = OrderDAO.getInstance();
		ArrayList<OrderDataDTO> dtos = dao.selectAllOrderData(orderNo);
		return dtos;
	}

	private void setOrderData(HttpServletRequest req) {
		// TODO Auto-generated method stub
		// ��ٱ��Ͽ� �ִ� ��ǰ�� �ֹ������Ϳ� ����
		// ���� �ݾ��� ��ٱ��ϰ� �ƴ϶� �ֹ������� �������� ���
		
		OrderDAO dao = OrderDAO.getInstance();
		
		// �ֹ� ��ȣ ��������
		String orderNo = getOrderNo(req);
		
		// 1. �ߺ��� ���� ���� �ֹ���ȣ�� ����� ������ ����
		dao.clearOrderData(orderNo);
		
		// 2. �ֹ���ȣ �������� ��ٱ��Ͽ� �ִ� ��ǰ�� ������ ��
		CartDAO cartDAO = new CartDAO();
		ArrayList<Cart> carts = cartDAO.getCartList(orderNo);
		System.out.println(carts);
		
		// 3. CartList�� OrderData List�� ����
		ArrayList<OrderDataDTO> dtos = changeCartData(carts, orderNo);
		System.out.println(dtos);
		
		// 4. OrderData List�� ������ ���̽��� ����
		for(OrderDataDTO dto : dtos) {
			dao.insertOrderData(dto);
		}
	}

	private ArrayList<OrderDataDTO> changeCartData(ArrayList<Cart> carts, String orderNo) {
		// TODO Auto-generated method stub
		ArrayList<OrderDataDTO> datas = new ArrayList<>();
		for(Cart cart : carts) {
			OrderDataDTO dto = new OrderDataDTO();
			dto.setOrderNo(orderNo);
			dto.setCartId(cart.getCartId());
			dto.setP_id(cart.getP_id());
			dto.setP_name(cart.getP_name());
			dto.setUnitPrice(cart.getP_unitPrice());
			dto.setCnt(cart.getCnt());
			dto.setSumPrice(cart.getP_unitPrice() * cart.getCnt());
			datas.add(dto);
		}
		return datas;
	}
	
	private int getTotalPrice(String orderNo) {
		OrderDAO dao = OrderDAO.getInstance();
		return dao.getTotalPrice(orderNo);
	}
	
	private void setOrderInfo(HttpServletRequest request) {
		OrderDAO dao = OrderDAO.getInstance();
		
		// 1. �ߺ��� ���� ���� �ֹ���ȣ�� ����� ������ ����
		dao.clearOrderInfo(getOrderNo(request));
		
		// 2. request�� ���� dto�� �����ؼ� dao�� ����
		OrderInfoDTO orderInfoDTO = new OrderInfoDTO();
		
		orderInfoDTO.setOrderNo(getOrderNo(request));
		orderInfoDTO.setMemberId(getMemberId(request));
		orderInfoDTO.setOrderName(request.getParameter("orderName"));
		orderInfoDTO.setOrderTel(request.getParameter("orderTel"));
		orderInfoDTO.setOrderEmail(request.getParameter("orderEmail"));
		orderInfoDTO.setReceiveName(request.getParameter("receiveName"));
		orderInfoDTO.setReceiveTel(request.getParameter("receiveTel"));
		orderInfoDTO.setReceiveAddress(request.getParameter("receiveAddress"));
		orderInfoDTO.setPayAmount(getTotalPrice(getOrderNo(request)));
		
		dao.insertOrderInfo(orderInfoDTO);
	}
	
	private String getMemberId(HttpServletRequest request) {
		// ���ǿ� ����� ���̵� ������
		HttpSession session = request.getSession();
		return (String) session.getAttribute("sessionId");
	}
	
	private String getOrderProductName(String orderNo) {
		OrderDAO dao = OrderDAO.getInstance();
		return dao.getOrderProductName(orderNo);
	}
	
	private OrderInfoDTO  getOrderInfo(String orderNo) {
		OrderDAO dao = OrderDAO.getInstance();
		return dao.getOrderInfo(orderNo);
	}
	
	private void processSuccess(HttpServletRequest request) throws Exception {
		// ������ ���������� ���� ��� ȣ��
		// ���� �õ� �������� paymentData ��ü�� successUrl �Ӽ����� ����
		// ���ٽÿ� orderId, paymentKey, amount �Ķ���� ������ ���� (���� : ���� url���� ���������� ������ ���ԵǾ� ���� ����)
		
		// 1. �Ķ���� ����
		// paymentKey : ������ Ű ��
		// orderId : �ֹ� ID�Դϴ�. ����â�� �� �� requestPayment()�� ��� ���� ��
		// amount : ������ ������ �ݾ�
		String orderId = request.getParameter("orderId");
//		System.out.println("orderId : " + orderId);
		String paymentKey = request.getParameter("paymentKey");
//		System.out.println("paymentKey : " + paymentKey);
		String amount = request.getParameter("amount");
//		System.out.println(" :" + amount);
		
		// 2. �佺���� �̸� ���� ������ secretKey�� ���. �佺�ʿ����� �ش� ������ ������ ����
		String secretKey = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R" + ":";
		
		// secretKey�� ���ڵ�
		Base64.Encoder encoder = Base64.getEncoder(); 
		byte[] encodedBytes = encoder.encode(secretKey.getBytes("UTF-8"));
		String authorizations = "Basic "+ new String(encodedBytes, 0, encodedBytes.length);
		
		// 3. �佺 ���� ���� API ȣ���ϱ�
		// REST API ������� ó��
		
		// ���� url�� paymentkey ����
		URL url = new URL("https://api.tosspayments.com/v1/payments/" + paymentKey);
		  
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// secretKey�� ����
		connection.setRequestProperty("Authorization", authorizations);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
	
		JSONObject obj = new JSONObject();
		obj.put("orderId", orderId);
		obj.put("amount", amount);
		 
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(obj.toString().getBytes("UTF-8"));
		// ȣ�� �� ��� �ڵ� ��������  
		int code = connection.getResponseCode();
		
		// 4. ��� �ڵ�� ���� ��� ó��
		// code�� 200�����̸� �������� ó��
		boolean isSuccess = code >= 200 && code < 300 ? true : false;
		System.out.println("isSuccess : " + isSuccess);
		  
		InputStream responseStream = isSuccess? connection.getInputStream(): connection.getErrorStream();
		  
		Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
	
		JSONParser parser = new JSONParser();
		// response ���� jsonObject�� ����
		JSONObject jsonObject = (JSONObject) parser.parse(reader);
		System.out.println("��� ������ : " + jsonObject.toJSONString());
		responseStream.close();
		
		if (isSuccess) {	// ������ ó��
			System.out.println("�ֹ���ȣ orderId : " + jsonObject.get("orderId"));
			System.out.println("������� method : " + jsonObject.get("method"));
			System.out.println("���������Ͻ� approvedAt : " + jsonObject.get("approvedAt"));
			// ������ ��������� ���� ó��. ������ ������¿� ��Ÿ ���� �������� ��������
			String method = (String) jsonObject.get("method");
			
			OrderInfoDTO dto = new OrderInfoDTO();
			dto.setOrderNo((String) jsonObject.get("orderId"));	// �ֹ���ȣ
			dto.setPayMethod(method);
			
			if (method.equals("�������")) {
				dto.setOrderStep(String.valueOf(OrderStep.orderReceive));
			}
			else {
				dto.setOrderStep(String.valueOf(OrderStep.payReceive));
				dto.setDatePay((String) jsonObject.get("approvedAt"));	// �Ա��Ͻ�
			}
			processSuccessUpdate(dto);
		}
	}
	
	private boolean processSuccessUpdate(OrderInfoDTO dto) {
		// dto �������� �ֹ� ���� ������Ʈ ����
		OrderDAO dao = OrderDAO.getInstance();
		return dao.updateOrderInfoWhenProcessSuccess(dto);
	}
	
	private void deleteCartWhenOrderDone (String orderNo) {
		// �ֹ� ó���� �Ϸ�� �� order_data�� �ִ� cartId �������� ��ٱ��� ����
		OrderDAO orderDAO = OrderDAO.getInstance();
		CartDAO cartDAO = new CartDAO();
		
		ArrayList<OrderDataDTO> dtos = orderDAO.selectAllOrderData(orderNo);
		for (OrderDataDTO dto : dtos) {
			try {
				cartDAO.deleteCartById(orderNo, dto.getCartId());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
